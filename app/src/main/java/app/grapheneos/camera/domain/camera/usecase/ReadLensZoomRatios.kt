package app.grapheneos.camera.domain.camera.usecase

import app.grapheneos.camera.data.camera.mapper.LensZoomRatioMapper
import app.grapheneos.camera.data.camera.session.CameraLensSource
import app.grapheneos.camera.data.camera.session.CameraSession
import javax.inject.Inject

/**
 * The zoom ratios at which the bound camera's lenses take over, ascending. Empty while no camera
 * is bound and when the device does not describe its lenses.
 */
interface ReadLensZoomRatios {
    operator fun invoke(session: CameraSession): List<Float>
}

internal class ReadLensZoomRatiosImpl @Inject constructor(
    private val cameraLensSource: CameraLensSource,
    private val lensZoomRatioMapper: LensZoomRatioMapper,
) : ReadLensZoomRatios {

    override fun invoke(session: CameraSession): List<Float> {
        val cameraInfo = session.camera?.cameraInfo
        val lenses = when (cameraInfo) {
            null -> null
            else -> cameraLensSource.lensesOf(cameraInfo = cameraInfo)
        }

        return when (lenses) {
            null -> emptyList()
            else -> lensZoomRatioMapper.map(lenses = lenses)
        }
    }
}
