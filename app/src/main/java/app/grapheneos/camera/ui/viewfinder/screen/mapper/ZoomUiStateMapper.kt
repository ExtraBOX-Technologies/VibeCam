package app.grapheneos.camera.ui.viewfinder.screen.mapper

import app.grapheneos.camera.data.camera.model.CameraZoom
import app.grapheneos.camera.ui.viewfinder.screen.model.ViewfinderState
import app.grapheneos.camera.ui.viewfinder.screen.model.ZoomUiState
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

interface ZoomUiStateMapper {
    fun map(state: ViewfinderState): ZoomUiState
}

internal class ZoomUiStateMapperImpl @Inject constructor() : ZoomUiStateMapper {

    override fun map(state: ViewfinderState): ZoomUiState {
        return when (val zoom = state.session.zoom) {
            null -> ZoomUiState()
            else -> ZoomUiState(
                zoomRatio = zoom.zoomRatio,
                linearZoom = zoom.linearZoom,
                minZoomRatio = zoom.minZoomRatio,
                maxZoomRatio = zoom.maxZoomRatio,
                stops = stopsOf(
                    zoom = zoom,
                    lensZoomRatios = state.session.lensZoomRatios,
                ),
            )
        }
    }

    /**
     * Where each lens takes over, when the camera says, and otherwise the widest the camera goes
     * and whole ratios. A lens within a tenth of the stop before it is the same stop: a crop of
     * the ultrawide sensor lands a hair off the main lens. The widest stop is the range's own
     * start, which the lens data only approximates.
     */
    private fun stopsOf(
        zoom: CameraZoom,
        lensZoomRatios: List<Float>,
    ): ImmutableList<Float> {
        val candidates = when {
            lensZoomRatios.isEmpty() -> WHOLE_RATIO_STOPS
            else -> lensZoomRatios + MAIN_LENS_RATIO
        }
        val widest = zoom.minZoomRatio.takeIf { it < MAIN_LENS_RATIO }
        val stops = (listOfNotNull(widest) + candidates.sorted())
            .filter { stop -> stop >= zoom.minZoomRatio && stop <= zoom.maxZoomRatio }
            .fold(emptyList<Float>()) { kept, stop ->
                when {
                    kept.isNotEmpty() && stop <= kept.last() * SAME_STOP_FACTOR -> kept
                    else -> kept + stop
                }
            }
            .map { stop -> snapToMainLens(stop = stop) }
            .take(MAX_STOPS)

        return stops.ifEmpty { listOf(zoom.minZoomRatio) }.toImmutableList()
    }

    private fun snapToMainLens(stop: Float): Float {
        val isMainLens = stop <= MAIN_LENS_RATIO * SAME_STOP_FACTOR &&
            stop * SAME_STOP_FACTOR >= MAIN_LENS_RATIO

        return when {
            isMainLens -> MAIN_LENS_RATIO
            else -> stop
        }
    }

    private companion object {
        private const val MAIN_LENS_RATIO = 1f
        private const val SAME_STOP_FACTOR = 1.1f
        private const val MAX_STOPS = 4
        private val WHOLE_RATIO_STOPS = listOf(1f, 2f)
    }
}
