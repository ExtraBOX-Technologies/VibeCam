package app.grapheneos.camera.data.camera.mapper

import app.grapheneos.camera.data.camera.model.CameraLens
import app.grapheneos.camera.data.camera.model.CameraLenses
import org.junit.Assert.assertEquals
import org.junit.Test

class LensZoomRatioMapperTest {

    private val mapper: LensZoomRatioMapper = LensZoomRatioMapperImpl()

    @Test
    fun ratios_compareEachLensWithTheMainOne_byFocalLengthOverSensorDiagonal() {
        val ratios = mapper.map(
            lenses = CameraLenses(
                main = MAIN,
                physical = listOf(MAIN, MAIN_CROP, TELEPHOTO, TELEPHOTO_CROP, ULTRAWIDE),
            ),
        )

        assertEquals(5, ratios.size)
        assertEquals(0.508f, ratios[0], TOLERANCE)
        assertEquals(1f, ratios[1], TOLERANCE)
        assertEquals(2f, ratios[2], TOLERANCE)
        assertEquals(4.508f, ratios[3], TOLERANCE)
        assertEquals(9.016f, ratios[4], TOLERANCE)
    }

    @Test
    fun ratios_leaveOutALensWithoutAUsableFocalLengthOrSensorSize() {
        val ratios = mapper.map(
            lenses = CameraLenses(
                main = MAIN,
                physical = listOf(
                    MAIN,
                    TELEPHOTO.copy(focalLengthMm = 0f),
                    ULTRAWIDE.copy(sensorWidthMm = 0f),
                    ULTRAWIDE.copy(sensorHeightMm = -1f),
                ),
            ),
        )

        assertEquals(listOf(1f), ratios)
    }

    @Test
    fun ratios_withoutAUsableMainLens_areEmpty() {
        val ratios = mapper.map(
            lenses = CameraLenses(
                main = MAIN.copy(sensorWidthMm = 0f),
                physical = listOf(MAIN, TELEPHOTO),
            ),
        )

        assertEquals(emptyList<Float>(), ratios)
    }

    private companion object {
        private const val TOLERANCE = 0.005f

        private val MAIN = CameraLens(
            focalLengthMm = 6.9f,
            sensorWidthMm = 9.792f,
            sensorHeightMm = 7.3728f,
        )
        private val MAIN_CROP = CameraLens(
            focalLengthMm = 6.9f,
            sensorWidthMm = 4.896f,
            sensorHeightMm = 3.6864f,
        )
        private val TELEPHOTO = CameraLens(
            focalLengthMm = 17.906f,
            sensorWidthMm = 5.6448f,
            sensorHeightMm = 4.2336f,
        )
        private val TELEPHOTO_CROP = CameraLens(
            focalLengthMm = 17.906f,
            sensorWidthMm = 2.8224f,
            sensorHeightMm = 2.1168f,
        )
        private val ULTRAWIDE = CameraLens(
            focalLengthMm = 2.02f,
            sensorWidthMm = 5.6448f,
            sensorHeightMm = 4.2336f,
        )
    }
}
