package app.grapheneos.camera.data.camera.mapper

import app.grapheneos.camera.data.camera.model.CameraLens
import app.grapheneos.camera.data.camera.model.RearCameraDescription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CameraLensMapMapperTest {

    private val mapper: CameraLensMapMapper = CameraLensMapMapperImpl(
        lensZoomRatioMapper = LensZoomRatioMapperImpl(),
    )

    @Test
    fun cameras_areLaidOutByHowNarrowTheySee_aroundTheFirstOne() {
        val map = requireNotNull(
            mapper.map(
                descriptions = listOf(
                    camera(cameraId = "0", focalLengthMm = 6f),
                    camera(cameraId = "2", focalLengthMm = 18f),
                    camera(cameraId = "3", focalLengthMm = 3f),
                ),
            ),
        )

        assertEquals("0", map.mainCameraId)
        assertEquals(listOf("3", "0", "2"), map.cameras.map { it.cameraId })
        assertEquals(0.5f, map.takeoverRatios[0], TOLERANCE)
        assertEquals(1f, map.takeoverRatios[1], TOLERANCE)
        assertEquals(3f, map.takeoverRatios[2], TOLERANCE)
    }

    @Test
    fun aSingleCamera_isAMapOfItself() {
        val map = requireNotNull(
            mapper.map(descriptions = listOf(camera(cameraId = "0", focalLengthMm = 6f))),
        )

        assertEquals(listOf("0"), map.cameras.map { it.cameraId })
        assertEquals(1f, map.minRatio)
        assertEquals(8f, map.maxRatio)
    }

    @Test
    fun aCameraThatIsPartOfAnother_isNotOfferedBesideIt() {
        val map = requireNotNull(
            mapper.map(
                descriptions = listOf(
                    camera(cameraId = "0", focalLengthMm = 6f, memberCameraIds = setOf("2", "3")),
                    camera(cameraId = "2", focalLengthMm = 18f),
                    camera(cameraId = "3", focalLengthMm = 3f),
                ),
            ),
        )

        assertEquals(listOf("0"), map.cameras.map { it.cameraId })
    }

    @Test
    fun aTwinOfTheMainCamera_isLeftOut() {
        val map = requireNotNull(
            mapper.map(
                descriptions = listOf(
                    camera(cameraId = "0", focalLengthMm = 6f),
                    camera(cameraId = "4", focalLengthMm = 6.2f),
                    camera(cameraId = "2", focalLengthMm = 18f),
                ),
            ),
        )

        assertEquals(listOf("0", "2"), map.cameras.map { it.cameraId })
    }

    @Test
    fun aCameraThatDoesNotDescribeItsLens_isLeftOut() {
        val map = requireNotNull(
            mapper.map(
                descriptions = listOf(
                    camera(cameraId = "0", focalLengthMm = 6f),
                    camera(cameraId = "2", focalLengthMm = 18f).copy(lens = null),
                    camera(cameraId = "3", focalLengthMm = 0f),
                ),
            ),
        )

        assertEquals(listOf("0"), map.cameras.map { it.cameraId })
    }

    @Test
    fun aMainCameraThatDoesNotDescribeItsLens_standsAlone() {
        val map = requireNotNull(
            mapper.map(
                descriptions = listOf(
                    camera(cameraId = "0", focalLengthMm = 6f).copy(lens = null),
                    camera(cameraId = "2", focalLengthMm = 18f),
                ),
            ),
        )

        assertEquals(listOf("0"), map.cameras.map { it.cameraId })
    }

    @Test
    fun noCameras_areNoMap() {
        assertNull(mapper.map(descriptions = emptyList()))
    }

    private fun camera(
        cameraId: String,
        focalLengthMm: Float,
        memberCameraIds: Set<String> = emptySet(),
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
            memberCameraIds = memberCameraIds,
        )
    }

    private companion object {
        private const val TOLERANCE = 0.001f
    }
}
