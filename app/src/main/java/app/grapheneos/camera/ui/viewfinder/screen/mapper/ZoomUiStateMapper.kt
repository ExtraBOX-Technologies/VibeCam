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
                stops = stopsOf(zoom = zoom),
            )
        }
    }

    /** The widest the camera goes when that is wider than its main lens, then whole ratios. */
    private fun stopsOf(zoom: CameraZoom): ImmutableList<Float> {
        val wholeRatios = WHOLE_RATIO_STOPS.filter { stop ->
            stop >= zoom.minZoomRatio && stop <= zoom.maxZoomRatio
        }
        val stops = when {
            zoom.minZoomRatio < wholeRatios.firstOrNull() ?: Float.MAX_VALUE -> {
                listOf(zoom.minZoomRatio) + wholeRatios
            }

            else -> wholeRatios
        }

        return stops.toImmutableList()
    }

    private companion object {
        private val WHOLE_RATIO_STOPS = listOf(1f, 2f)
    }
}
