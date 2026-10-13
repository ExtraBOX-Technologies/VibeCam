package app.grapheneos.camera.data.camera.model

data class CameraLens(
    val focalLengthMm: Float,
    val sensorWidthMm: Float,
    val sensorHeightMm: Float,
)

/**
 * @param main the lens a zoom ratio of 1 stands for
 * @param physical every lens behind the camera, the main one and crops of a sensor included
 */
data class CameraLenses(
    val main: CameraLens,
    val physical: List<CameraLens>,
)

enum class LensDataStatus {
    /** The camera does not describe its lenses, which is no fault: most cameras have one. */
    UNAVAILABLE,
    CONSISTENT,

    /** What the camera says about its lenses disagrees with itself or with its zoom range. */
    INCONSISTENT,
}

/**
 * @param zoomRatios where each lens takes over, as far as the lens data can be made sense of
 */
data class CameraLensReport(
    val lenses: CameraLenses?,
    val zoom: CameraZoom?,
    val zoomRatios: List<Float>,
    val status: LensDataStatus,
) {

    /** Lens data that does not add up places no stops: the zoom range alone does. */
    val stopRatios: List<Float>
        get() {
            return when (status) {
                LensDataStatus.CONSISTENT -> zoomRatios
                LensDataStatus.UNAVAILABLE, LensDataStatus.INCONSISTENT -> emptyList()
            }
        }

    fun details(): String {
        val lensLines = lenses?.physical.orEmpty().map { lens -> "lens: ${describe(lens)}" }

        return buildList {
            add("lens data: $status")
            add("zoom range: ${zoom?.minZoomRatio}..${zoom?.maxZoomRatio}")
            add("main lens: ${lenses?.main?.let(::describe)}")
            addAll(lensLines)
            add("lens zoom ratios: $zoomRatios")
        }.joinToString(separator = "\n")
    }

    private fun describe(lens: CameraLens): String {
        return "${lens.focalLengthMm} mm on ${lens.sensorWidthMm} x ${lens.sensorHeightMm} mm"
    }
}
