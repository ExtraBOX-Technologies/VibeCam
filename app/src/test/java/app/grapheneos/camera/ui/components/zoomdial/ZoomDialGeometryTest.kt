package app.grapheneos.camera.ui.components.zoomdial

import kotlin.math.PI
import kotlin.math.hypot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ZoomDialGeometryTest {

    private val geometry = ZoomDialGeometry(
        width = 800f,
        height = 210f,
        rimY = 10f,
        tickArc = 50f,
    )

    @Test
    fun radius_makesTheCapSpanTheWidthAtTheBottom() {
        assertEquals(500f, geometry.radius, TOLERANCE)
        assertEquals(400f, geometry.center.x, TOLERANCE)
        assertEquals(510f, geometry.center.y, TOLERANCE)

        val corner = geometry.point(angle = geometry.edgeAngle)

        assertEquals(800f, corner.x, TOLERANCE)
        assertEquals(210f, corner.y, TOLERANCE)
    }

    @Test
    fun radius_ofACapDeeperThanHalfTheWidth_isHalfTheWidth() {
        val deep = ZoomDialGeometry(
            width = 400f,
            height = 500f,
            rimY = 0f,
            tickArc = 50f,
        )

        assertEquals(200f, deep.radius, TOLERANCE)
        assertEquals((PI / 2).toFloat(), deep.edgeAngle, TOLERANCE)
    }

    @Test
    fun point_atTheMarkUnderThePointer_isTheTopOfTheRim() {
        val angle = geometry.angle(markPosition = 7f, position = 7f)
        val top = geometry.point(angle = angle)

        assertEquals(0f, angle, TOLERANCE)
        assertEquals(400f, top.x, TOLERANCE)
        assertEquals(10f, top.y, TOLERANCE)
    }

    @Test
    fun point_withAnInset_movesTowardTheCenter() {
        val angle = geometry.angle(markPosition = 3f, position = 0f)
        val inner = geometry.point(angle = angle, inset = 40f)

        assertEquals(
            460f,
            hypot(inner.x - geometry.center.x, inner.y - geometry.center.y),
            TOLERANCE,
        )
    }

    @Test
    fun angle_putsNeighboringTicksOneTickArcApartOnTheRim() {
        val first = geometry.point(angle = geometry.angle(markPosition = 4f, position = 4f))
        val second = geometry.point(angle = geometry.angle(markPosition = 5f, position = 4f))
        val chord = hypot(second.x - first.x, second.y - first.y)

        assertEquals(0.1f, geometry.tickAngle, TOLERANCE)
        assertEquals(50f, chord, 0.05f)
    }

    @Test
    fun angle_growsTowardTheEndOfTheScale_andMirrorsInRtl() {
        val ahead = geometry.angle(markPosition = 6f, position = 4f)

        assertEquals(0.2f, ahead, TOLERANCE)
        assertEquals(-0.2f, geometry.angle(markPosition = 2f, position = 4f), TOLERANCE)
        assertEquals(
            -0.2f,
            geometry.angle(markPosition = 6f, position = 4f, isRtl = true),
            TOLERANCE,
        )
        assertEquals(true, geometry.point(angle = ahead).x > geometry.center.x)
    }

    @Test
    fun alpha_fadesOutTowardTheEdgeOfTheDial() {
        val fadeAngle = 0.2f

        assertEquals(1f, geometry.alpha(angle = 0f, fadeAngle = fadeAngle), TOLERANCE)
        assertEquals(
            0.5f,
            geometry.alpha(angle = geometry.edgeAngle - 0.1f, fadeAngle = fadeAngle),
            TOLERANCE,
        )
        assertEquals(
            0.5f,
            geometry.alpha(angle = 0.1f - geometry.edgeAngle, fadeAngle = fadeAngle),
            TOLERANCE,
        )
        assertEquals(0f, geometry.alpha(angle = geometry.edgeAngle + 0.3f, fadeAngle = fadeAngle))
    }

    @Test
    fun visibleMarks_coverTheArcOnBothSidesOfThePointer_clampedToTheScale() {
        val ticksToEdge = geometry.edgeAngle / geometry.tickAngle

        assertEquals(9.27f, ticksToEdge, 0.01f)
        assertEquals(
            3,
            geometry.firstVisibleMark(position = 10f, marksPerTick = 3),
        )
        assertEquals(
            57,
            geometry.lastVisibleMark(position = 10f, marksPerTick = 3, lastTick = 30),
        )
        assertEquals(0, geometry.firstVisibleMark(position = 2f, marksPerTick = 3))
        assertEquals(
            36,
            geometry.lastVisibleMark(position = 10f, marksPerTick = 3, lastTick = 12),
        )
    }

    @Test
    fun geometry_withoutACapBelowTheRim_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ZoomDialGeometry(
                width = 800f,
                height = 10f,
                rimY = 10f,
                tickArc = 50f,
            )
        }
    }

    private companion object {
        private const val TOLERANCE = 0.001f
    }
}
