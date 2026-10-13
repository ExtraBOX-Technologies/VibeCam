package app.grapheneos.camera.data.camera.session

import android.hardware.camera2.CameraCharacteristics
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import app.grapheneos.camera.data.camera.model.CameraLens
import app.grapheneos.camera.data.camera.model.CameraLenses
import app.grapheneos.camera.data.camera.model.RearCameraDescription
import javax.inject.Inject

/**
 * The lenses behind a camera, as the device reports them. Null when the camera carries no camera2
 * characteristics or does not describe its own lens; a physical lens it does not describe is left
 * out.
 */
interface CameraLensSource {
    fun lensesOf(cameraInfo: CameraInfo): CameraLenses?

    /** The rear cameras among [cameraInfos], in the order given. */
    fun rearCamerasOf(cameraInfos: List<CameraInfo>): List<RearCameraDescription>
}

internal class CameraLensSourceImpl @Inject constructor() : CameraLensSource {

    override fun lensesOf(cameraInfo: CameraInfo): CameraLenses? {
        val main = lensOf(cameraInfo = cameraInfo)

        return when (main) {
            null -> null
            else -> CameraLenses(
                main = main,
                physical = cameraInfo.physicalCameraInfos.mapNotNull { physical ->
                    lensOf(cameraInfo = physical)
                },
            )
        }
    }

    override fun rearCamerasOf(cameraInfos: List<CameraInfo>): List<RearCameraDescription> {
        return cameraInfos
            .filter { cameraInfo -> cameraInfo.lensFacing == CameraSelector.LENS_FACING_BACK }
            .mapNotNull { cameraInfo -> describe(cameraInfo = cameraInfo) }
    }

    private fun describe(cameraInfo: CameraInfo): RearCameraDescription? {
        val cameraId = cameraIdOf(cameraInfo = cameraInfo)
        val zoomState = cameraInfo.zoomState.value

        return when {
            cameraId == null || zoomState == null -> null
            else -> RearCameraDescription(
                cameraId = cameraId,
                lens = lensOf(cameraInfo = cameraInfo),
                minZoomRatio = zoomState.minZoomRatio,
                maxZoomRatio = zoomState.maxZoomRatio,
                memberCameraIds = cameraInfo.physicalCameraInfos
                    .mapNotNull { physical -> cameraIdOf(cameraInfo = physical) }
                    .toSet(),
            )
        }
    }

    @OptIn(ExperimentalCamera2Interop::class)
    private fun cameraIdOf(cameraInfo: CameraInfo): String? {
        return try {
            Camera2CameraInfo.from(cameraInfo).cameraId
        } catch (exception: IllegalArgumentException) {
            Log.w(TAG, "Camera info carries no camera2 id", exception)
            null
        }
    }

    @OptIn(ExperimentalCamera2Interop::class)
    private fun lensOf(cameraInfo: CameraInfo): CameraLens? {
        return try {
            val camera2Info = Camera2CameraInfo.from(cameraInfo)
            val focalLength = camera2Info
                .getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                ?.firstOrNull()
            val sensorSize = camera2Info
                .getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)

            when {
                focalLength == null || sensorSize == null -> null
                else -> CameraLens(
                    focalLengthMm = focalLength,
                    sensorWidthMm = sensorSize.width,
                    sensorHeightMm = sensorSize.height,
                )
            }
        } catch (exception: IllegalArgumentException) {
            Log.w(TAG, "Camera info carries no camera2 characteristics", exception)
            null
        }
    }

    private companion object {
        const val TAG = "CameraLensSource"
    }
}
