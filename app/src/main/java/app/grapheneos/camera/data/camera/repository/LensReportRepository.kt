package app.grapheneos.camera.data.camera.repository

import app.grapheneos.camera.data.camera.model.CameraLensReport
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** What the last bound camera said about its lenses, for the screens that have no camera. */
interface LensReportRepository {

    val report: StateFlow<CameraLensReport?>

    fun publish(report: CameraLensReport)
}

internal class LensReportRepositoryImpl @Inject constructor() : LensReportRepository {

    private val _report = MutableStateFlow<CameraLensReport?>(null)
    override val report: StateFlow<CameraLensReport?> = _report.asStateFlow()

    override fun publish(report: CameraLensReport) {
        _report.value = report
    }
}
