package app.grapheneos.camera.ui.viewfinder.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import app.grapheneos.camera.ui.core.CameraTheme
import app.grapheneos.camera.ui.viewfinder.screen.model.ViewfinderAction
import app.grapheneos.camera.ui.viewfinder.screen.model.ViewfinderAction.CameraAction.ZoomRatioRequested
import app.grapheneos.camera.ui.viewfinder.screen.model.ZoomUiState
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ViewfinderZoomDialTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var zoom by mutableStateOf(ADJUSTABLE)
    private var visible by mutableStateOf(true)
    private val actions = mutableListOf<ViewfinderAction>()

    @Test
    fun stopTapped_requestsItsRatio() {
        setContent()

        composeRule.onNodeWithContentDescription("2×").performClick()

        assertEquals(listOf(ZoomRatioRequested.StopClicked(zoomRatio = 2f)), actions)
    }

    @Test
    fun zoomRangeLostWhileShown_fadesOutOnTheLastRange() {
        setContent()

        zoom = ZoomUiState()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("2×").assertDoesNotExist()
    }

    @Test
    fun hiddenWhileTheZoomRangeIsLost_fadesOutWithoutARange() {
        setContent()

        composeRule.mainClock.autoAdvance = false
        visible = false
        zoom = ZoomUiState()
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("2×").assertDoesNotExist()
    }

    @Test
    fun cameraWithoutAZoomRange_showsNoControl() {
        zoom = ZoomUiState()
        setContent()

        composeRule.onNodeWithContentDescription("1×").assertDoesNotExist()
    }

    private fun setContent() {
        composeRule.setContent {
            CameraTheme {
                ViewfinderZoomDial(
                    zoom = zoom,
                    visible = visible,
                    rotationDegrees = 0f,
                    onAction = { action -> actions += action },
                )
            }
        }
    }

    private companion object {
        private val ADJUSTABLE = ZoomUiState(
            zoomRatio = 1f,
            minZoomRatio = 0.5f,
            maxZoomRatio = 8f,
            stops = persistentListOf(0.5f, 1f, 2f),
        )
    }
}
