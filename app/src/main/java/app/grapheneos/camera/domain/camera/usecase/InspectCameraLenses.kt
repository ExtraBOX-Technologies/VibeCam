package app.grapheneos.camera.domain.camera.usecase

import app.grapheneos.camera.data.camera.mapper.LensDataStatusMapper
import app.grapheneos.camera.data.camera.mapper.LensZoomRatioMapper
import app.grapheneos.camera.data.camera.model.CameraLensReport
import app.grapheneos.camera.data.camera.repository.LensReportRepository
import app.grapheneos.camera.data.camera.session.CameraLensSource
import app.grapheneos.camera.data.camera.session.CameraSession
import javax.inject.Inject

/**
 * Reads the lenses behind the bound camera, works out where each takes over and whether the data
 * can be trusted, and leaves the report where the settings screen can find it.
 */
interface InspectCameraLenses {
    operator fun invoke(session: CameraSession): CameraLensReport
}

internal class InspectCameraLensesImpl @Inject constructor(
    private val cameraLensSource: CameraLensSource,
    private val lensZoomRatioMapper: LensZoomRatioMapper,
    private val lensDataStatusMapper: LensDataStatusMapper,
    private val lensReportRepository: LensReportRepository,
) : InspectCameraLenses {

    override fun invoke(session: CameraSession): CameraLensReport {
        val cameraInfo = session.camera?.cameraInfo
        val lenses = when (cameraInfo) {
            null -> null
            else -> cameraLensSource.lensesOf(cameraInfo = cameraInfo)
        }
        val zoomRatios = when (lenses) {
            null -> emptyList()
            else -> lensZoomRatioMapper.map(lenses = lenses)
        }
        val zoom = session.zoom
        val report = CameraLensReport(
            lenses = lenses,
            zoom = zoom,
            zoomRatios = zoomRatios,
            status = lensDataStatusMapper.map(
                lenses = lenses,
                zoomRatios = zoomRatios,
                zoom = zoom,
            ),
        )

        lensReportRepository.publish(report = report)

        return report
    }
}
