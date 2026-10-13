package app.grapheneos.camera.data.camera.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CameraLensMapTest {

    private val map = CameraLensMap(
        cameras = listOf(ULTRAWIDE, MAIN, TELEPHOTO),
        mainCameraId = MAIN.cameraId,
    )

    @Test
    fun range_runsFromTheWidestCameraToTheNarrowestOnesLimit() {
        assertEquals(0.5f, map.minRatio)
        assertEquals(30f, map.maxRatio)
        assertEquals(listOf(0.5f, 1f, 3f), map.takeoverRatios)
    }

    @Test
    fun target_isTheNarrowestCameraThatReachesTheRatio() {
        assertEquals(
            LensTarget(cameraId = "ultrawide", zoomRatio = 1.6f),
            map.target(ratio = 0.8f, currentCameraId = "ultrawide"),
        )
        assertEquals(
            LensTarget(cameraId = "main", zoomRatio = 2f),
            map.target(ratio = 2f, currentCameraId = "main"),
        )
        assertEquals(
            LensTarget(cameraId = "telephoto", zoomRatio = 2f),
            map.target(ratio = 6f, currentCameraId = "main"),
        )
    }

    @Test
    fun target_switchesUpExactlyWhereTheNextCameraTakesOver() {
        assertEquals("main", map.target(ratio = 2.99f, currentCameraId = "main").cameraId)
        assertEquals("telephoto", map.target(ratio = 3f, currentCameraId = "main").cameraId)
    }

    @Test
    fun target_keepsTheCurrentCameraJustShortOfItsRange_andLetsGoBeyondThat() {
        assertEquals(
            LensTarget(cameraId = "telephoto", zoomRatio = 1f),
            map.target(ratio = 2.95f, currentCameraId = "telephoto"),
        )
        assertEquals("main", map.target(ratio = 2.8f, currentCameraId = "telephoto").cameraId)
    }

    @Test
    fun target_outsideTheRange_staysWithTheCameraAtThatEnd() {
        assertEquals(
            LensTarget(cameraId = "ultrawide", zoomRatio = 1f),
            map.target(ratio = 0.2f, currentCameraId = "main"),
        )
        assertEquals(
            LensTarget(cameraId = "telephoto", zoomRatio = 10f),
            map.target(ratio = 80f, currentCameraId = "main"),
        )
    }

    @Test
    fun camera_unknownOrUnset_isTheMainOne() {
        assertEquals(MAIN, map.camera(cameraId = null))
        assertEquals(MAIN, map.camera(cameraId = "gone"))
        assertEquals(TELEPHOTO, map.camera(cameraId = "telephoto"))
    }

    @Test
    fun within_isTheClosestTheCameraGetsToTheRatio() {
        assertEquals(1f, map.within(camera = MAIN, ratio = 0.5f))
        assertEquals(8f, map.within(camera = MAIN, ratio = 20f))
        assertEquals(2f, map.within(camera = TELEPHOTO, ratio = 6f))
    }

    @Test
    fun map_withCamerasOutOfOrderOrWithoutAny_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            CameraLensMap(cameras = listOf(MAIN, ULTRAWIDE), mainCameraId = "main")
        }
        assertThrows(IllegalArgumentException::class.java) {
            CameraLensMap(cameras = emptyList(), mainCameraId = "main")
        }
    }

    private companion object {
        private val ULTRAWIDE = RearCamera(
            cameraId = "ultrawide",
            baseRatio = 0.5f,
            minZoomRatio = 1f,
            maxZoomRatio = 8f,
        )
        private val MAIN = RearCamera(
            cameraId = "main",
            baseRatio = 1f,
            minZoomRatio = 1f,
            maxZoomRatio = 8f,
        )
        private val TELEPHOTO = RearCamera(
            cameraId = "telephoto",
            baseRatio = 3f,
            minZoomRatio = 1f,
            maxZoomRatio = 10f,
        )
    }
}
