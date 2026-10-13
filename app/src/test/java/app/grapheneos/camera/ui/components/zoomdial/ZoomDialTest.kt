package app.grapheneos.camera.ui.components.zoomdial

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import app.grapheneos.camera.ui.core.CameraTheme
import kotlin.math.pow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ZoomDialTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var value by mutableFloatStateOf(1f)
    private var enabled by mutableStateOf(true)
    private var touchSlop = 0f
    private val changes = mutableListOf<Float>()
    private var finishes = 0

    @Test
    fun zoomDial_reportsAContinuousRange() {
        setContent()

        dial().assertRangeInfoEquals(
            ProgressBarRangeInfo(
                current = 1f,
                range = 0.5f..30f,
            ),
        )
    }

    @Test
    fun zoomDial_draggedLeftByTwoTickArcs_turnsTwoTicksTowardTheEnd() {
        value = 1.2f
        setContent()

        drag(ticks = -2f)

        assertTrue(changes.size > 1)
        assertTrue(changes.zipWithNext().all { (previous, next) -> next > previous })
        assertEquals(1.2f * 2f.pow(2f / 5f), changes.last(), 0.01f)
        assertEquals(1, finishes)
    }

    @Test
    fun zoomDial_draggedOntoAStop_holdsThere() {
        value = 1.2f
        setContent()

        drag(ticks = 1.8f)

        assertEquals(1f, changes.last(), 0.001f)
    }

    @Test
    fun zoomDial_disabled_ignoresDrags() {
        enabled = false
        setContent()

        drag(ticks = -3f)

        assertEquals(emptyList<Float>(), changes)
    }

    @Test
    fun zoomDial_setProgress_clampsToTheRange() {
        setContent()

        dial().performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
            setProgress(50f)
        }

        assertEquals(listOf(30f), changes)
        assertEquals(1, finishes)
    }

    @Test
    fun zoomDial_arrowKey_stepsOneTick() {
        setContent()

        dial().requestFocus()
        dial().performKeyInput { pressKey(Key.DirectionRight) }

        assertEquals(TICK_RATIO, changes.single(), 0.001f)
    }

    private fun setContent() {
        composeRule.setContent {
            touchSlop = LocalViewConfiguration.current.touchSlop
            CameraTheme {
                ZoomDial(
                    value = value,
                    onValueChange = { newValue ->
                        changes += newValue
                        value = newValue
                    },
                    valueRange = 0.5f..30f,
                    stops = listOf(0.5f, 1f, 2f, 5f),
                    valueSuffix = "×",
                    modifier = Modifier.testTag(TAG),
                    enabled = enabled,
                    onValueChangeFinished = { finishes += 1 },
                )
            }
        }
    }

    private fun drag(ticks: Float) {
        dial().performTouchInput {
            val arc = TICK_ARC.toPx()

            down(center)
            moveBy(Offset(x = -touchSlop, y = 0f))
            repeat(DRAG_MOVES) {
                moveBy(Offset(x = ticks * arc / DRAG_MOVES, y = 0f))
            }
            up()
        }
    }

    private fun dial(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TAG)
    }

    private companion object {
        private const val TAG = "zoomDial"
        private const val DRAG_MOVES = 20
        private const val TICK_RATIO = 1.1487f
        private val TICK_ARC = 24.dp
    }
}
