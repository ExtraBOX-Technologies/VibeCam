package app.grapheneos.camera.ui.viewfinder.screen.model

sealed interface ViewfinderCameraEvent {

    /** The zoom moved onto a lens that is another camera, which has to be bound. */
    data object LensChanged : ViewfinderCameraEvent

    data class ProviderReady(
        val forced: Boolean,
    ) : ViewfinderCameraEvent
}
