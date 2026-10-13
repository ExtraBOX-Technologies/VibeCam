package app.grapheneos.camera.data.camera.mapper

import app.grapheneos.camera.data.camera.model.CameraLenses
import app.grapheneos.camera.data.camera.model.CameraZoom
import app.grapheneos.camera.data.camera.model.LensDataStatus
import javax.inject.Inject

/**
 * Lens data is taken at its word only when it explains the camera: every lens it lists is
 * described, one of them is the main lens, and none reaches wider than the zoom range does, nor
 * does the range reach wider than all of them. The comparison is loose, because a camera crops
 * its wide lenses to correct them and the range then starts a little short of the lens.
 */
interface LensDataStatusMapper {
    fun map(
        lenses: CameraLenses?,
        zoomRatios: List<Float>,
        zoom: CameraZoom?,
    ): LensDataStatus
}

internal class LensDataStatusMapperImpl @Inject constructor() : LensDataStatusMapper {

    override fun map(
        lenses: CameraLenses?,
        zoomRatios: List<Float>,
        zoom: CameraZoom?,
    ): LensDataStatus {
        val hasMainLens = zoomRatios.any { ratio -> isSame(ratio, MAIN_LENS_RATIO) }
        val explainsRange = zoom == null || zoomRatios.isEmpty() ||
            startsWhereTheRangeDoes(zoomRatios, zoom)

        return when {
            lenses == null || lenses.physical.isEmpty() -> LensDataStatus.UNAVAILABLE
            zoomRatios.size < lenses.physical.size -> LensDataStatus.INCONSISTENT
            !hasMainLens || !explainsRange -> LensDataStatus.INCONSISTENT
            else -> LensDataStatus.CONSISTENT
        }
    }

    private fun startsWhereTheRangeDoes(
        zoomRatios: List<Float>,
        zoom: CameraZoom,
    ): Boolean {
        val widestLens = zoomRatios.min()
        val widestOfRange = minOf(zoom.minZoomRatio, MAIN_LENS_RATIO)

        return isSame(widestLens, widestOfRange)
    }

    private fun isSame(
        ratio: Float,
        other: Float,
    ): Boolean {
        return ratio <= other * TOLERANCE_FACTOR && other <= ratio * TOLERANCE_FACTOR
    }

    private companion object {
        private const val MAIN_LENS_RATIO = 1f
        private const val TOLERANCE_FACTOR = 1.15f
    }
}
