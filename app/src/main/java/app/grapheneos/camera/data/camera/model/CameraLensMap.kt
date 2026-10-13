package app.grapheneos.camera.data.camera.model

/**
 * A rear camera the device opens on its own rather than behind another one.
 *
 * @param baseRatio how much narrower it sees than the main camera at its own zoom ratio of 1
 */
data class RearCamera(
    val cameraId: String,
    val baseRatio: Float,
    val minZoomRatio: Float,
    val maxZoomRatio: Float,
) {

    val widestRatio: Float
        get() {
            return baseRatio * minZoomRatio
        }

    val narrowestRatio: Float
        get() {
            return baseRatio * maxZoomRatio
        }
}

data class LensTarget(
    val cameraId: String,
    val zoomRatio: Float,
)

/**
 * One zoom scale over the rear cameras, in ratios of the main camera. A ratio belongs to the
 * narrowest camera that still reaches it, so each camera is used from where it takes over up to
 * where the next one does. A camera in use keeps a ratio that falls a little short of its own
 * range: without that slack a finger resting on the boundary would switch cameras back and forth.
 */
data class CameraLensMap(
    val cameras: List<RearCamera>,
    val mainCameraId: String,
) {

    val minRatio: Float
    val maxRatio: Float

    init {
        require(cameras.isNotEmpty()) {
            "a lens map needs a camera"
        }
        val isAscending = cameras.zipWithNext().all { (previous, next) ->
            previous.baseRatio < next.baseRatio
        }

        require(isAscending) {
            "cameras must be ascending by base ratio, were $cameras"
        }

        minRatio = cameras.minOf { camera -> camera.widestRatio }
        maxRatio = cameras.maxOf { camera -> camera.narrowestRatio }
    }

    val takeoverRatios: List<Float>
        get() {
            return cameras.map { camera -> camera.baseRatio }
        }

    fun camera(cameraId: String?): RearCamera {
        return cameras.firstOrNull { camera -> camera.cameraId == cameraId }
            ?: cameras.first { camera -> camera.cameraId == mainCameraId }
    }

    fun target(
        ratio: Float,
        currentCameraId: String?,
    ): LensTarget {
        val current = camera(cameraId = currentCameraId)
        val reaching = cameras.lastOrNull { camera -> camera.widestRatio <= ratio }
            ?: cameras.first()
        val keepsCurrent = reaching.baseRatio < current.baseRatio &&
            ratio >= current.widestRatio * SWITCH_BACK_FACTOR
        val camera = when {
            keepsCurrent -> current
            else -> reaching
        }

        return LensTarget(
            cameraId = camera.cameraId,
            zoomRatio = within(camera = camera, ratio = ratio),
        )
    }

    /** The ratio as [camera] gets closest to it, for when the camera cannot be changed. */
    fun within(
        camera: RearCamera,
        ratio: Float,
    ): Float {
        return (ratio / camera.baseRatio).coerceIn(camera.minZoomRatio, camera.maxZoomRatio)
    }

    private companion object {
        private const val SWITCH_BACK_FACTOR = 0.97f
    }
}
