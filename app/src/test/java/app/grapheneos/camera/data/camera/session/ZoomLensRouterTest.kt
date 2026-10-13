package app.grapheneos.camera.data.camera.session

import app.grapheneos.camera.data.camera.mapper.CameraLensMapMapperImpl
import app.grapheneos.camera.data.camera.mapper.LensZoomRatioMapperImpl
import app.grapheneos.camera.data.camera.model.CameraLens
import app.grapheneos.camera.data.camera.model.CameraZoom
import app.grapheneos.camera.data.camera.model.LensFacing
import app.grapheneos.camera.data.camera.model.RearCameraDescription
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ZoomLensRouterTest {

    private var rearCameras = listOf(MAIN, TELEPHOTO, ULTRAWIDE)
    private var zoom: CameraZoom? = cameraZoom(zoomRatio = 1f)
    private var lensFacing = LensFacing.BACK

    private val session = mockk<CameraSession>(relaxed = true) {
        every { zoom } answers { this@ZoomLensRouterTest.zoom }
        every { lensFacing } answers { this@ZoomLensRouterTest.lensFacing }
    }
    private val cameraLensSource = mockk<CameraLensSource> {
        every { rearCamerasOf(cameraInfos = any()) } answers { rearCameras }
    }
    private val cameraSelectorSource = CameraSelectorSourceImpl()
    private val router: ZoomLensRouter = ZoomLensRouterImpl(
        cameraLensSource = cameraLensSource,
        cameraLensMapMapper = CameraLensMapMapperImpl(
            lensZoomRatioMapper = LensZoomRatioMapperImpl(),
        ),
        cameraSelectorSource = cameraSelectorSource,
    )

    @Test
    fun aSingleRearCamera_passesStraightThrough() {
        rearCameras = listOf(MAIN)
        zoom = cameraZoom(zoomRatio = 2f)

        val lensChanged = router.request(
            session = session,
            zoomRatio = 5f,
            canSwitchCamera = true,
        )

        assertFalse(lensChanged)
        assertEquals(zoom, router.zoomOf(session = session))
        assertEquals(emptyList<Float>(), router.takeoverRatios(session = session))
        assertEquals(5f, router.shared(session = session, zoomRatio = 5f))
        assertNull(cameraSelectorSource.rearCameraId)
        verify(exactly = 1) { session.setZoomRatio(5f) }
    }

    @Test
    fun theFrontCamera_passesStraightThrough() {
        lensFacing = LensFacing.FRONT

        val lensChanged = router.request(
            session = session,
            zoomRatio = 4f,
            canSwitchCamera = true,
        )

        assertFalse(lensChanged)
        assertEquals(zoom, router.zoomOf(session = session))
        verify(exactly = 1) { session.setZoomRatio(4f) }
    }

    @Test
    fun zoom_isPublishedOnTheScaleSharedByEveryRearCamera() {
        cameraSelectorSource.rearCameraId = "telephoto"
        zoom = cameraZoom(zoomRatio = 2f)

        val shared = requireNotNull(router.zoomOf(session = session))

        assertEquals(6f, shared.zoomRatio, TOLERANCE)
        assertEquals(0.5f, shared.minZoomRatio, TOLERANCE)
        assertEquals(24f, shared.maxZoomRatio, TOLERANCE)
        assertEquals(3, router.takeoverRatios(session = session).size)
    }

    @Test
    fun aRatioOnTheBoundCamera_zoomsItWithoutARebind() {
        val lensChanged = router.request(
            session = session,
            zoomRatio = 2f,
            canSwitchCamera = true,
        )

        assertFalse(lensChanged)
        assertNull(cameraSelectorSource.rearCameraId)
        verify(exactly = 1) { session.setZoomRatio(2f) }
    }

    @Test
    fun aRatioOnAnotherCamera_selectsIt_andZoomsItOnceItIsBound() {
        val lensChanged = router.request(
            session = session,
            zoomRatio = 6f,
            canSwitchCamera = true,
        )

        assertTrue(lensChanged)
        assertEquals("telephoto", cameraSelectorSource.rearCameraId)
        verify(exactly = 0) { session.setZoomRatio(any()) }

        router.applyPending(session = session)
        router.applyPending(session = session)

        verify(exactly = 1) { session.setZoomRatio(range(1.999f, 2.001f)) }
    }

    @Test
    fun aRatioOnAnotherCamera_whenTheCameraMayNotChange_goesAsFarAsTheBoundOneDoes() {
        val lensChanged = router.request(
            session = session,
            zoomRatio = 0.5f,
            canSwitchCamera = false,
        )

        assertFalse(lensChanged)
        assertNull(cameraSelectorSource.rearCameraId)
        verify(exactly = 1) { session.setZoomRatio(1f) }
    }

    @Test
    fun aRatioRequestedBetweenBinds_waitsForTheCameraBeingBound_withoutAnotherSwitch() {
        cameraSelectorSource.rearCameraId = "telephoto"
        zoom = null

        val lensChanged = router.request(
            session = session,
            zoomRatio = 0.5f,
            canSwitchCamera = true,
        )

        assertFalse(lensChanged)
        assertEquals("telephoto", cameraSelectorSource.rearCameraId)
        verify(exactly = 0) { session.setZoomRatio(any()) }

        router.applyPending(session = session)

        verify(exactly = 1) { session.setZoomRatio(1f) }
    }

    @Test
    fun selectMainCamera_forgetsTheSelectedCameraAndItsPendingZoom() {
        router.request(session = session, zoomRatio = 6f, canSwitchCamera = true)

        router.selectMainCamera()
        router.applyPending(session = session)

        assertNull(cameraSelectorSource.rearCameraId)
        verify(exactly = 0) { session.setZoomRatio(any()) }
    }

    private companion object {
        private const val TOLERANCE = 0.001f

        private val MAIN = rearCamera(cameraId = "main", focalLengthMm = 6f)
        private val TELEPHOTO = rearCamera(cameraId = "telephoto", focalLengthMm = 18f)
        private val ULTRAWIDE = rearCamera(cameraId = "ultrawide", focalLengthMm = 3f)

        private fun rearCamera(
            cameraId: String,
            focalLengthMm: Float,
        ): RearCameraDescription {
            return RearCameraDescription(
                cameraId = cameraId,
                lens = CameraLens(
                    focalLengthMm = focalLengthMm,
                    sensorWidthMm = 8f,
                    sensorHeightMm = 6f,
                ),
                minZoomRatio = 1f,
                maxZoomRatio = 8f,
                memberCameraIds = emptySet(),
            )
        }

        private fun cameraZoom(zoomRatio: Float): CameraZoom {
            return CameraZoom(
                zoomRatio = zoomRatio,
                linearZoom = 0f,
                minZoomRatio = 1f,
                maxZoomRatio = 8f,
            )
        }
    }
}
