package app.grapheneos.camera.ui.viewfinder.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.grapheneos.camera.R
import app.grapheneos.camera.ui.components.motion.FADE_IN
import app.grapheneos.camera.ui.components.motion.FADE_OUT
import app.grapheneos.camera.ui.components.motion.ProvideContentRotation
import app.grapheneos.camera.ui.components.zoomdial.ZoomDialControl
import app.grapheneos.camera.ui.viewfinder.screen.model.ViewfinderAction
import app.grapheneos.camera.ui.viewfinder.screen.model.ViewfinderAction.CameraAction.ZoomRatioRequested
import app.grapheneos.camera.ui.viewfinder.screen.model.ZoomUiState
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

private val COLLAPSE_DELAY = 1.seconds

/** How far the camera's ratio may land from a stop and still count as the jump to it. */
private const val STOP_TOLERANCE = 0.01f

/**
 * The zoom stops over the bottom edge of the preview, and the dial they open into. The dial also
 * opens when the zoom changes by other means, a pinch or a key, and closes a moment after the last
 * change once no finger is on it. A tap on a stop moves the zoom without opening it.
 */
@Composable
internal fun ViewfinderZoomDial(
    zoom: ZoomUiState,
    visible: Boolean,
    rotationDegrees: Float,
    onAction: (ViewfinderAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expansion = remember { ZoomDialExpansion() }
    val shownZoom = remember { LastAdjustableZoom() }.update(zoom = zoom)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(zoom.zoomRatio) {
        expansion.onZoomChanged(zoomRatio = zoom.zoomRatio)
    }
    LaunchedEffect(expansion.isExpanded, zoom.zoomRatio, isPressed) {
        if (expansion.isExpanded && !isPressed) {
            delay(COLLAPSE_DELAY)
            expansion.collapse()
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = visible && zoom.isAdjustable,
            enter = FADE_IN,
            exit = FADE_OUT,
        ) {
            if (shownZoom != null) {
                ProvideContentRotation(degrees = rotationDegrees) {
                    ZoomDialControl(
                        value = shownZoom.zoomRatio.coerceIn(
                            shownZoom.minZoomRatio,
                            shownZoom.maxZoomRatio,
                        ),
                        onValueChange = { zoomRatio ->
                            expansion.onDialTurned(zoomRatio = zoomRatio)
                            onAction(ZoomRatioRequested.DialTurned(zoomRatio = zoomRatio))
                        },
                        valueRange = shownZoom.minZoomRatio..shownZoom.maxZoomRatio,
                        stops = shownZoom.stops,
                        valueSuffix = stringResource(R.string.zoom_ratio_suffix),
                        expanded = expansion.isExpanded,
                        onExpand = expansion::expand,
                        onStopClick = { stop ->
                            expansion.onStopClicked(zoomRatio = stop)
                            onAction(ZoomRatioRequested.StopClicked(zoomRatio = stop))
                        },
                        expandLabel = stringResource(R.string.zoom_adjust),
                        interactionSource = interactionSource,
                    )
                }
            }
        }
    }
}

@Stable
private class ZoomDialExpansion {

    var isExpanded by mutableStateOf(false)
        private set

    private var expectedZoomRatio: Float? = null

    fun expand() {
        isExpanded = true
    }

    fun collapse() {
        isExpanded = false
    }

    fun onDialTurned(zoomRatio: Float) {
        expectedZoomRatio = zoomRatio
    }

    fun onStopClicked(zoomRatio: Float) {
        expectedZoomRatio = zoomRatio
    }

    /** The first ratio is the camera's own, and one this control asked for is not news. */
    fun onZoomChanged(zoomRatio: Float) {
        val expected = expectedZoomRatio
        val isOwnChange = expected == null || abs(zoomRatio - expected) <= STOP_TOLERANCE

        expectedZoomRatio = zoomRatio
        if (!isOwnChange) {
            expand()
        }
    }
}

/**
 * The control goes on being drawn while it fades out, and it fades out exactly when the camera is
 * between binds and has no zoom range to draw: it keeps the last range it could.
 */
private class LastAdjustableZoom {

    private var last: ZoomUiState? = null

    fun update(zoom: ZoomUiState): ZoomUiState? {
        if (zoom.isAdjustable) {
            last = zoom
        }

        return last
    }
}
