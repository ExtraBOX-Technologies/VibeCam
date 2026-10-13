package app.grapheneos.camera.ui.viewfinder.screen.mapper

import app.grapheneos.camera.data.camera.model.CameraZoom
import app.grapheneos.camera.data.core.model.CameraMode
import app.grapheneos.camera.ui.viewfinder.screen.model.ViewfinderSessionState
import app.grapheneos.camera.ui.viewfinder.screen.model.ViewfinderState
import app.grapheneos.camera.ui.viewfinder.screen.model.ZoomUiState
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ZoomUiStateMapperTest {

    private val mapper: ZoomUiStateMapper = ZoomUiStateMapperImpl()

    @Test
    fun zoom_isPublishedWithItsRange() {
        val state = stateWith(
            zoom = CameraZoom(
                zoomRatio = 2f,
                linearZoom = 0.5f,
                minZoomRatio = 1f,
                maxZoomRatio = 10f,
            ),
        )

        assertEquals(
            ZoomUiState(
                zoomRatio = 2f,
                linearZoom = 0.5f,
                minZoomRatio = 1f,
                maxZoomRatio = 10f,
                stops = persistentListOf(1f, 2f),
            ),
            mapper.map(state),
        )
    }

    @Test
    fun stops_startAtTheWidestRatio_whenTheCameraGoesWiderThanItsMainLens() {
        val state = stateWith(
            zoom = CameraZoom(
                zoomRatio = 1f,
                linearZoom = 0f,
                minZoomRatio = 0.508f,
                maxZoomRatio = 30f,
            ),
        )

        assertEquals(listOf(0.508f, 1f, 2f), mapper.map(state).stops)
    }

    @Test
    fun stops_leaveOutWholeRatiosOutsideTheRange() {
        val narrow = stateWith(
            zoom = CameraZoom(
                zoomRatio = 1f,
                linearZoom = 0f,
                minZoomRatio = 1f,
                maxZoomRatio = 1.5f,
            ),
        )
        val telephoto = stateWith(
            zoom = CameraZoom(
                zoomRatio = 3f,
                linearZoom = 0f,
                minZoomRatio = 3f,
                maxZoomRatio = 10f,
            ),
        )

        assertEquals(listOf(1f), mapper.map(narrow).stops)
        assertEquals(listOf(3f), mapper.map(telephoto).stops)
    }

    @Test
    fun stops_followTheLenses_whenTheCameraDescribesThem() {
        val state = stateWith(
            zoom = CameraZoom(
                zoomRatio = 1f,
                linearZoom = 0f,
                minZoomRatio = 0.5078f,
                maxZoomRatio = 30f,
            ),
            lensZoomRatios = listOf(0.508f, 1f, 1.016f, 2f, 4.508f, 9.016f),
        )

        assertEquals(listOf(0.5078f, 1f, 2f, 4.508f), mapper.map(state).stops)
    }

    @Test
    fun stops_leaveOutLensesOutsideTheRange() {
        val state = stateWith(
            zoom = CameraZoom(
                zoomRatio = 1f,
                linearZoom = 0f,
                minZoomRatio = 1f,
                maxZoomRatio = 4f,
            ),
            lensZoomRatios = listOf(0.5f, 1.02f, 4.5f),
        )

        assertEquals(listOf(1f), mapper.map(state).stops)
    }

    @Test
    fun zoom_isAdjustableOnlyOverARange() {
        assertEquals(false, ZoomUiState().isAdjustable)
        assertEquals(true, ZoomUiState(minZoomRatio = 1f, maxZoomRatio = 8f).isAdjustable)
    }

    @Test
    fun zoom_beforeTheCameraIsBound_isTheDefault() {
        assertEquals(ZoomUiState(), mapper.map(stateWith(zoom = null)))
    }

    private fun stateWith(
        zoom: CameraZoom?,
        lensZoomRatios: List<Float> = emptyList(),
    ): ViewfinderState {
        return ViewfinderState(
            mode = CameraMode.CAMERA,
            requiresVideoModeOnly = false,
            session = ViewfinderSessionState(
                zoom = zoom,
                lensZoomRatios = lensZoomRatios,
            ),
        )
    }
}
