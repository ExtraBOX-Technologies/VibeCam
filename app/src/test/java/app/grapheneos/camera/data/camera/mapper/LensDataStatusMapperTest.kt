package app.grapheneos.camera.data.camera.mapper

import app.grapheneos.camera.data.camera.model.CameraLens
import app.grapheneos.camera.data.camera.model.CameraLensReport
import app.grapheneos.camera.data.camera.model.CameraLenses
import app.grapheneos.camera.data.camera.model.CameraZoom
import app.grapheneos.camera.data.camera.model.LensDataStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LensDataStatusMapperTest {

    private val mapper: LensDataStatusMapper = LensDataStatusMapperImpl()

    @Test
    fun lensesThatExplainTheZoomRange_areConsistent() {
        assertEquals(
            LensDataStatus.CONSISTENT,
            mapper.map(
                lenses = lenses(count = 3),
                zoomRatios = listOf(0.508f, 1f, 4.5f),
                zoom = zoom(minZoomRatio = 0.5078f),
            ),
        )
    }

    @Test
    fun aRangeThatStartsALittleShortOfTheWidestLens_isStillConsistent() {
        assertEquals(
            LensDataStatus.CONSISTENT,
            mapper.map(
                lenses = lenses(count = 2),
                zoomRatios = listOf(0.53f, 1f),
                zoom = zoom(minZoomRatio = 0.6f),
            ),
        )
    }

    @Test
    fun aCameraThatDoesNotDescribeItsLenses_isUnavailable() {
        assertEquals(
            LensDataStatus.UNAVAILABLE,
            mapper.map(lenses = null, zoomRatios = emptyList(), zoom = zoom(minZoomRatio = 1f)),
        )
        assertEquals(
            LensDataStatus.UNAVAILABLE,
            mapper.map(
                lenses = lenses(count = 0),
                zoomRatios = emptyList(),
                zoom = zoom(minZoomRatio = 1f),
            ),
        )
    }

    @Test
    fun aLensWithoutAUsableDescription_isInconsistent() {
        assertEquals(
            LensDataStatus.INCONSISTENT,
            mapper.map(
                lenses = lenses(count = 3),
                zoomRatios = listOf(0.5f, 1f),
                zoom = zoom(minZoomRatio = 0.5f),
            ),
        )
    }

    @Test
    fun lensesWithoutTheMainOne_areInconsistent() {
        assertEquals(
            LensDataStatus.INCONSISTENT,
            mapper.map(
                lenses = lenses(count = 2),
                zoomRatios = listOf(0.5f, 3f),
                zoom = zoom(minZoomRatio = 0.5f),
            ),
        )
    }

    @Test
    fun aRangeWiderThanEveryLens_isInconsistent() {
        assertEquals(
            LensDataStatus.INCONSISTENT,
            mapper.map(
                lenses = lenses(count = 2),
                zoomRatios = listOf(1f, 5f),
                zoom = zoom(minZoomRatio = 0.5f),
            ),
        )
    }

    @Test
    fun aLensWiderThanTheRange_isInconsistent() {
        assertEquals(
            LensDataStatus.INCONSISTENT,
            mapper.map(
                lenses = lenses(count = 2),
                zoomRatios = listOf(0.5f, 1f),
                zoom = zoom(minZoomRatio = 1f),
            ),
        )
    }

    @Test
    fun report_placesStopsOnlyFromConsistentData_andDescribesItself() {
        val report = CameraLensReport(
            lenses = lenses(count = 2),
            zoom = zoom(minZoomRatio = 1f),
            zoomRatios = listOf(0.5f, 1f),
            status = LensDataStatus.INCONSISTENT,
        )

        assertEquals(emptyList<Float>(), report.stopRatios)
        assertEquals(
            listOf(0.5f, 1f),
            report.copy(status = LensDataStatus.CONSISTENT).stopRatios,
        )
        assertTrue(report.details().contains("lens data: INCONSISTENT"))
        assertTrue(report.details().contains("zoom range: 1.0..8.0"))
        assertTrue(report.details().contains("lens: 6.9 mm on 9.8 x 7.4 mm"))
    }

    private fun lenses(count: Int): CameraLenses {
        return CameraLenses(
            main = LENS,
            physical = List(count) { LENS },
        )
    }

    private fun zoom(minZoomRatio: Float): CameraZoom {
        return CameraZoom(
            zoomRatio = 1f,
            linearZoom = 0f,
            minZoomRatio = minZoomRatio,
            maxZoomRatio = 8f,
        )
    }

    private companion object {
        private val LENS = CameraLens(
            focalLengthMm = 6.9f,
            sensorWidthMm = 9.8f,
            sensorHeightMm = 7.4f,
        )
    }
}
