package app.grapheneos.camera.data.camera.model

data class CameraLens(
    val focalLengthMm: Float,
    val sensorWidthMm: Float,
    val sensorHeightMm: Float,
)

/**
 * @param main the lens a zoom ratio of 1 stands for
 * @param physical every lens behind the camera, the main one and crops of a sensor included
 */
data class CameraLenses(
    val main: CameraLens,
    val physical: List<CameraLens>,
)
