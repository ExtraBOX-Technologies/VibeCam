package app.grapheneos.camera.data.camera.session

import app.grapheneos.camera.data.camera.mapper.CameraLensMapMapper
import app.grapheneos.camera.data.camera.model.CameraLensMap
import app.grapheneos.camera.data.camera.model.CameraZoom
import app.grapheneos.camera.data.camera.model.LensFacing
import javax.inject.Inject

/**
 * Zoom in ratios of the main rear camera, over every rear camera the device opens separately. A
 * device with a single rear camera, the front camera and a session without a provider all pass
 * straight through: the ratio is the bound camera's own.
 */
interface ZoomLensRouter {

    /** The session's zoom on the shared scale, or null while no camera reports one. */
    fun zoomOf(session: CameraSession): CameraZoom?

    /** Where each rear camera takes over, the main one included. Empty when passing through. */
    fun takeoverRatios(session: CameraSession): List<Float>

    /** What a ratio the bound camera reports about itself is on the shared scale. */
    fun shared(
        session: CameraSession,
        zoomRatio: Float,
    ): Float

    /**
     * Zooms to [zoomRatio]. When that belongs to another camera and [canSwitchCamera], that
     * camera is selected and true is returned: the caller rebinds, and [applyPending] zooms the
     * new camera once it reports its zoom. Otherwise the bound camera goes as close as it can.
     */
    fun request(
        session: CameraSession,
        zoomRatio: Float,
        canSwitchCamera: Boolean,
    ): Boolean

    fun applyPending(session: CameraSession)

    /** Back to the main rear camera, for a mode that only it is known to support. */
    fun selectMainCamera()
}

internal class ZoomLensRouterImpl @Inject constructor(
    private val cameraLensSource: CameraLensSource,
    private val cameraLensMapMapper: CameraLensMapMapper,
    private val cameraSelectorSource: CameraSelectorSource,
) : ZoomLensRouter {

    private var lensMap: CameraLensMap? = null
    private var pendingZoomRatio: Float? = null

    override fun zoomOf(session: CameraSession): CameraZoom? {
        val zoom = session.zoom
        val map = switchingMap(session = session)

        return when {
            zoom == null || map == null -> zoom
            else -> zoom.copy(
                zoomRatio = shared(session = session, zoomRatio = zoom.zoomRatio),
                minZoomRatio = map.minRatio,
                maxZoomRatio = map.maxRatio,
            )
        }
    }

    override fun takeoverRatios(session: CameraSession): List<Float> {
        return switchingMap(session = session)?.takeoverRatios.orEmpty()
    }

    override fun shared(
        session: CameraSession,
        zoomRatio: Float,
    ): Float {
        val map = switchingMap(session = session)

        return when (map) {
            null -> zoomRatio
            else -> zoomRatio * map.camera(cameraId = cameraSelectorSource.rearCameraId).baseRatio
        }
    }

    override fun request(
        session: CameraSession,
        zoomRatio: Float,
        canSwitchCamera: Boolean,
    ): Boolean {
        val map = switchingMap(session = session)
        val isBetweenBinds = session.zoom == null

        return when {
            map == null -> {
                session.setZoomRatio(zoomRatio)
                false
            }

            canSwitchCamera && !isBetweenBinds -> route(
                session = session,
                map = map,
                zoomRatio = zoomRatio,
            )

            else -> {
                zoomWithin(
                    session = session,
                    cameraZoomRatio = map.within(
                        camera = map.camera(cameraId = cameraSelectorSource.rearCameraId),
                        ratio = zoomRatio,
                    ),
                )
                false
            }
        }
    }

    override fun applyPending(session: CameraSession) {
        pendingZoomRatio?.let { zoomRatio -> session.setZoomRatio(zoomRatio) }
        pendingZoomRatio = null
    }

    override fun selectMainCamera() {
        cameraSelectorSource.rearCameraId = null
        pendingZoomRatio = null
    }

    private fun route(
        session: CameraSession,
        map: CameraLensMap,
        zoomRatio: Float,
    ): Boolean {
        val current = map.camera(cameraId = cameraSelectorSource.rearCameraId)
        val target = map.target(
            ratio = zoomRatio,
            currentCameraId = current.cameraId,
        )
        val switches = target.cameraId != current.cameraId

        when {
            switches -> {
                cameraSelectorSource.rearCameraId = target.cameraId
                pendingZoomRatio = target.zoomRatio
            }

            else -> session.setZoomRatio(target.zoomRatio)
        }

        return switches
    }

    /** A camera between binds takes no zoom: it is kept for the one that is being bound. */
    private fun zoomWithin(
        session: CameraSession,
        cameraZoomRatio: Float,
    ) {
        when (session.zoom) {
            null -> pendingZoomRatio = cameraZoomRatio
            else -> session.setZoomRatio(cameraZoomRatio)
        }
    }

    /** The lens map when there is more than one rear camera to route between, else null. */
    private fun switchingMap(session: CameraSession): CameraLensMap? {
        val map = lensMap ?: readLensMap(session = session)

        return map?.takeIf { session.lensFacing == LensFacing.BACK && it.cameras.size > 1 }
    }

    private fun readLensMap(session: CameraSession): CameraLensMap? {
        val cameraInfos = session.cameraProvider?.availableCameraInfos
        val descriptions = cameraInfos?.let { cameraLensSource.rearCamerasOf(cameraInfos = it) }
        val map = descriptions?.let { cameraLensMapMapper.map(descriptions = it) }

        lensMap = map

        return map
    }
}
