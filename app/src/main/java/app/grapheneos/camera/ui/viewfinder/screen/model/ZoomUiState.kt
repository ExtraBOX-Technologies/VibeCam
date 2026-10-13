package app.grapheneos.camera.ui.viewfinder.screen.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * @param stops the ratios the zoom control offers as shortcuts, ascending and inside the range.
 */
@Immutable
data class ZoomUiState(
    val zoomRatio: Float = 1f,
    val linearZoom: Float = 0f,
    val minZoomRatio: Float = 1f,
    val maxZoomRatio: Float = 1f,
    val stops: ImmutableList<Float> = persistentListOf(1f),
) {

    val isAdjustable: Boolean
        get() {
            return maxZoomRatio > minZoomRatio
        }
}
