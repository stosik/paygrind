@file:JvmName("PaygrindApp")

package pl.stosik.paygrind.app

import arrow.continuations.SuspendApp
import arrow.fx.coroutines.resourceScope
import kotlinx.coroutines.*
import pl.stosik.paygrind.core.infrastracture.adapter.driver.billingJobScheduler
import pl.stosik.paygrind.rest.httpServer

fun main() = SuspendApp {
    val config = parseConfiguration(this::class.java.classLoader.getResourceAsStream("application.yaml"))

    resourceScope {
        val dependencies = dependencies(configuration = config)

        httpServer(
            invoiceService = dependencies.invoiceService,
            customerService = dependencies.customerService,
            billingService = dependencies.billingService,
        ).start()

        billingJobScheduler(
            cronConfiguration = config.cron,
            billingProcessor = dependencies.billingProcessor,
            jobLockService = dependencies.jobLockService,
            timeProvider = dependencies.timeProvider
        ).schedule()

        awaitCancellation()
    }
}