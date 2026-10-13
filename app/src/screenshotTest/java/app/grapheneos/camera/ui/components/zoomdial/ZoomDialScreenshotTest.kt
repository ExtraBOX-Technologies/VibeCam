package app.grapheneos.camera.ui.components.zoomdial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.grapheneos.camera.ui.core.CameraPreviewColumn
import app.grapheneos.camera.ui.core.CameraPreviewViewfinder
import com.android.tools.screenshot.PreviewTest

private val THREE_LENS_STOPS = listOf(0.5f, 1f, 2f, 5f)
private val THREE_LENS_RANGE = 0.5f..30f
private val ONE_LENS_STOPS = listOf(1f, 2f)
private val ONE_LENS_RANGE = 1f..8f
private const val ZOOM_SUFFIX = "×"
private const val VIEWFINDER_ASPECT_RATIO = 2.6f

@PreviewTest
@PreviewLightDark
@Composable
private fun ZoomDialValues() {
    CameraPreviewColumn {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        ) {
            listOf(0.5f, 1f, 1.2f, 5f, 30f).forEach { value ->
                ZoomDialViewfinder(value = value)
            }
        }
    }
}

@PreviewTest
@Preview(
    name = "Persian",
    locale = "fa",
)
@Preview(
    name = "LargeFont",
    fontScale = 2f,
)
@Preview(
    name = "Narrow",
    widthDp = 280,
)
@Composable
private fun ZoomDialLayouts() {
    CameraPreviewColumn {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        ) {
            ZoomDialViewfinder(value = 1.2f)
            ZoomDialViewfinder(
                value = 1f,
                valueRange = ONE_LENS_RANGE,
                stops = ONE_LENS_STOPS,
            )
            ZoomDialViewfinder(
                value = 2f,
                enabled = false,
            )
        }
    }
}

@Composable
private fun ZoomDialViewfinder(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = THREE_LENS_RANGE,
    stops: List<Float> = THREE_LENS_STOPS,
    enabled: Boolean = true,
) {
    CameraPreviewViewfinder(aspectRatio = VIEWFINDER_ASPECT_RATIO) {
        ZoomDial(
            value = value,
            onValueChange = {},
            valueRange = valueRange,
            stops = stops,
            valueSuffix = ZOOM_SUFFIX,
            modifier = Modifier.align(alignment = Alignment.BottomCenter),
            enabled = enabled,
        )
    }
}
