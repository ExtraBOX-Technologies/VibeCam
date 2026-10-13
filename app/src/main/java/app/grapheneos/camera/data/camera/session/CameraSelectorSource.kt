package app.grapheneos.camera.data.camera.session

import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import app.grapheneos.camera.data.camera.model.LensFacing
import javax.inject.Inject

/**
 * The selector a lens facing binds with. The rear one can be narrowed to a single camera, for a
 * device that opens its lenses separately; a camera that is not there leaves the selector as wide
 * as it was, so that naming one can never leave the rear without a camera.
 */
interface CameraSelectorSource {

    var rearCameraId: String?

    fun selectorFor(lensFacing: LensFacing): CameraSelector
}

internal class CameraSelectorSourceImpl @Inject constructor() : CameraSelectorSource {

    override var rearCameraId: String? = null

    override fun selectorFor(lensFacing: LensFacing): CameraSelector {
        val cameraId = rearCameraId

        return when {
            lensFacing == LensFacing.FRONT -> FRONT_CAMERA_SELECTOR
            cameraId == null -> REAR_CAMERA_SELECTOR
            else -> rearSelector(cameraId = cameraId)
        }
    }

    private fun rearSelector(cameraId: String): CameraSelector {
        return CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .addCameraFilter { cameraInfos ->
                cameraInfos
                    .filter { cameraInfo -> isCamera(cameraInfo = cameraInfo, cameraId = cameraId) }
                    .ifEmpty { cameraInfos }
            }
            .build()
    }

    @OptIn(ExperimentalCamera2Interop::class)
    private fun isCamera(
        cameraInfo: CameraInfo,
        cameraId: String,
    ): Boolean {
        return try {
            Camera2CameraInfo.from(cameraInfo).cameraId == cameraId
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private companion object {
        private val FRONT_CAMERA_SELECTOR: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        private val REAR_CAMERA_SELECTOR: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
    }
}
