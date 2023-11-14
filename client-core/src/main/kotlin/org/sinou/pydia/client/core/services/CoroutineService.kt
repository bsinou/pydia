package org.sinou.pydia.client.core.services

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob

class CoroutineService(
    val uiDispatcher: MainCoroutineDispatcher,
    val ioDispatcher: CoroutineDispatcher,
    val cpuDispatcher: CoroutineDispatcher,
) {

    private val cellsSupervisorJob = SupervisorJob()

    val cellsUiScope = CoroutineScope(uiDispatcher + cellsSupervisorJob)
    val cellsCpuScope = CoroutineScope(cpuDispatcher + cellsSupervisorJob)
    val cellsIoScope = CoroutineScope(ioDispatcher + cellsSupervisorJob)

}