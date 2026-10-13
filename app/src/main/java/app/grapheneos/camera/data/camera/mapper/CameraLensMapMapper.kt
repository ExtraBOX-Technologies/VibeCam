package app.grapheneos.camera.data.camera.mapper

import app.grapheneos.camera.data.camera.model.CameraLensMap
import app.grapheneos.camera.data.camera.model.RearCamera
import app.grapheneos.camera.data.camera.model.RearCameraDescription
import javax.inject.Inject

/**
 * Lays the rear cameras a device opens separately on one zoom scale. The first camera is the main
 * one, as it is the one a plain rear selector binds. A camera that is part of another, that does
 * not describe its lens, or that sees what a camera already kept sees (a depth or monochrome
 * twin of the main one) is left out. Null without cameras.
 */
interface CameraLensMapMapper {
    fun map(descriptions: List<RearCameraDescription>): CameraLensMap?
}

internal class CameraLensMapMapperImpl @Inject constructor(
    private val lensZoomRatioMapper: LensZoomRatioMapper,
) : CameraLensMapMapper {

    override fun map(descriptions: List<RearCameraDescription>): CameraLensMap? {
        val main = descriptions.firstOrNull()

        return when (main) {
            null -> null
            else -> CameraLensMap(
                cameras = camerasBeside(main = main, descriptions = descriptions),
                mainCameraId = main.cameraId,
            )
        }
    }

    private fun camerasBeside(
        main: RearCameraDescription,
        descriptions: List<RearCameraDescription>,
    ): List<RearCamera> {
        val memberIds = descriptions.flatMap { description -> description.memberCameraIds }
        val others = descriptions
            .drop(1)
            .filter { description -> description.cameraId !in memberIds }
            .mapNotNull { description -> rearCamera(description = description, main = main) }
            .sortedBy { camera -> camera.baseRatio }
        val mainCamera = RearCamera(
            cameraId = main.cameraId,
            baseRatio = MAIN_BASE_RATIO,
            minZoomRatio = main.minZoomRatio,
            maxZoomRatio = main.maxZoomRatio,
        )

        return (listOf(mainCamera) + others)
            .fold(emptyList<RearCamera>()) { kept, camera ->
                when {
                    kept.any { other -> isSame(camera.baseRatio, other.baseRatio) } -> kept
                    else -> kept + camera
                }
            }
            .sortedBy { camera -> camera.baseRatio }
    }

    private fun rearCamera(
        description: RearCameraDescription,
        main: RearCameraDescription,
    ): RearCamera? {
        val baseRatio = when {
            description.lens == null || main.lens == null -> null
            else -> lensZoomRatioMapper.ratio(lens = description.lens, main = main.lens)
        }

        return when (baseRatio) {
            null -> null
            else -> RearCamera(
                cameraId = description.cameraId,
                baseRatio = baseRatio,
                minZoomRatio = description.minZoomRatio,
                maxZoomRatio = description.maxZoomRatio,
            )
        }
    }

    private fun isSame(
        ratio: Float,
        other: Float,
    ): Boolean {
        return ratio <= other * SAME_LENS_FACTOR && other <= ratio * SAME_LENS_FACTOR
    }

    private companion object {
        private const val MAIN_BASE_RATIO = 1f
        private const val SAME_LENS_FACTOR = 1.1f
    }
}
