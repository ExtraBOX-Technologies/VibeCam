package app.grapheneos.camera.ui.components.zoomdial

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.grapheneos.camera.ui.components.motion.ProvideContentRotation
import app.grapheneos.camera.ui.components.zoom.rememberZoomFormat
import app.grapheneos.camera.ui.core.CameraPreviewControl
import app.grapheneos.camera.ui.core.CameraPreviewSample
import app.grapheneos.camera.ui.core.PreviewRotation
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

private const val ZOOM_SUFFIX = "×"
private const val PINCH_FACTOR = 1.25f

private val COLLAPSE_DELAY = 1.seconds

private val THREE_LENS_STOPS = listOf(0.5f, 1f, 2f, 5f)
private val THREE_LENS_RANGE = 0.5f..30f
private val TWO_LENS_STOPS = listOf(0.5f, 1f, 2f)
private val TWO_LENS_RANGE = 0.5f..8f
private val ONE_LENS_STOPS = listOf(1f, 2f)
private val ONE_LENS_RANGE = 1f..8f

private enum class SampleCamera(
    val label: String,
    val valueRange: ClosedFloatingPointRange<Float>,
    val stops: List<Float>,
) {
    ThreeLenses(
        label = "3 lenses",
        valueRange = THREE_LENS_RANGE,
        stops = THREE_LENS_STOPS,
    ),
    TwoLenses(
        label = "2 lenses",
        valueRange = TWO_LENS_RANGE,
        stops = TWO_LENS_STOPS,
    ),
    OneLens(
        label = "1 lens",
        valueRange = ONE_LENS_RANGE,
        stops = ONE_LENS_STOPS,
    ),
}

@Preview(heightDp = 720)
@Composable
private fun ZoomDialControlSamplePreview() {
    ZoomDialControlSample()
}

@Composable
private fun ZoomDialControlSample() {
    val state = remember { ZoomDialControlSampleState() }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val valueLabel = rememberZoomFormat().format(value = state.zoom) + ZOOM_SUFFIX

    LaunchedEffect(state.isExpanded, state.changes, isPressed) {
        if (state.isExpanded && !isPressed) {
            delay(COLLAPSE_DELAY)
            state.collapse()
        }
    }

    CameraPreviewSample(
        status = "Zoom: $valueLabel",
        controls = {
            CameraPreviewControl(
                text = "Pinch in",
                onClick = state::pinchIn,
            )
            CameraPreviewControl(
                text = "Pinch out",
                onClick = state::pinchOut,
            )
            CameraPreviewControl(
                text = state.camera.label,
                onClick = state::nextCamera,
            )
            state.rotation.Control()
        },
    ) {
        ProvideContentRotation(degrees = state.rotation.degrees) {
            key(state.camera) {
                ZoomDialControl(
                    value = state.zoom,
                    onValueChange = state::change,
                    valueRange = state.camera.valueRange,
                    stops = state.camera.stops,
                    valueSuffix = ZOOM_SUFFIX,
                    expanded = state.isExpanded,
                    onExpand = state::expand,
                    onStopClick = state::change,
                    expandLabel = "Adjust zoom",
                    modifier = Modifier.align(alignment = Alignment.BottomCenter),
                    interactionSource = interactionSource,
                )
            }
        }
    }
}

@Stable
private class ZoomDialControlSampleState {

    val rotation = PreviewRotation()

    var camera by mutableStateOf(SampleCamera.ThreeLenses)
        private set
    var zoom by mutableFloatStateOf(1f)
        private set
    var isExpanded by mutableStateOf(false)
        private set
    var changes by mutableIntStateOf(0)
        private set

    fun change(value: Float) {
        zoom = value
        changes += 1
    }

    fun expand() {
        isExpanded = true
        changes += 1
    }

    fun collapse() {
        isExpanded = false
    }

    fun pinchIn() {
        pinch(factor = 1f / PINCH_FACTOR)
    }

    fun pinchOut() {
        pinch(factor = PINCH_FACTOR)
    }

    fun nextCamera() {
        val cameras = SampleCamera.entries

        camera = cameras[(camera.ordinal + 1) % cameras.size]
        zoom = 1f
        isExpanded = false
    }

    private fun pinch(factor: Float) {
        val range = camera.valueRange

        change(value = (zoom * factor).coerceIn(range.start, range.endInclusive))
        expand()
    }
}
