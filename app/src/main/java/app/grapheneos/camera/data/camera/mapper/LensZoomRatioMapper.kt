package app.grapheneos.camera.data.camera.mapper

import app.grapheneos.camera.data.camera.model.CameraLens
import app.grapheneos.camera.data.camera.model.CameraLenses
import javax.inject.Inject
import kotlin.math.hypot

/**
 * The zoom ratio at which each lens takes over from the main one: how much narrower it sees,
 * which is its focal length over its sensor's diagonal against the same for the main lens. A lens
 * that reports no usable focal length or sensor size is left out, and without a usable main lens
 * there are no ratios at all.
 */
interface LensZoomRatioMapper {
    fun map(lenses: CameraLenses): List<Float>

    /** Null when either lens reports no usable focal length or sensor size. */
    fun ratio(
        lens: CameraLens,
        main: CameraLens,
    ): Float?
}

internal class LensZoomRatioMapperImpl @Inject constructor() : LensZoomRatioMapper {

    override fun map(lenses: CameraLenses): List<Float> {
        val mainReach = reachOf(lens = lenses.main)
        val reaches = lenses.physical.mapNotNull { lens -> reachOf(lens = lens) }

        return when (mainReach) {
            null -> emptyList()
            else -> reaches.map { reach -> reach / mainReach }.sorted()
        }
    }

    override fun ratio(
        lens: CameraLens,
        main: CameraLens,
    ): Float? {
        val reach = reachOf(lens = lens)
        val mainReach = reachOf(lens = main)

        return when {
            reach == null || mainReach == null -> null
            else -> reach / mainReach
        }
    }

    private fun reachOf(lens: CameraLens): Float? {
        val diagonal = hypot(lens.sensorWidthMm, lens.sensorHeightMm)
        val isUsable = lens.focalLengthMm > 0f && lens.sensorWidthMm > 0f &&
            lens.sensorHeightMm > 0f

        return when {
            isUsable -> lens.focalLengthMm / diagonal
            else -> null
        }
    }
}
