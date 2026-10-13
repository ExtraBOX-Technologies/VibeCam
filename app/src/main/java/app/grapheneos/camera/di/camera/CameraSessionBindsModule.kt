package app.grapheneos.camera.di.camera

import app.grapheneos.camera.data.camera.session.CameraSelectorSource
import app.grapheneos.camera.data.camera.session.CameraSelectorSourceImpl
import app.grapheneos.camera.data.camera.session.VideoRecordingSession
import app.grapheneos.camera.data.camera.session.VideoRecordingSessionImpl
import app.grapheneos.camera.data.camera.session.ZoomLensRouter
import app.grapheneos.camera.data.camera.session.ZoomLensRouterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class CameraSessionBindsModule {

    @Binds
    @ActivityRetainedScoped
    abstract fun bindCameraSelectorSource(
        impl: CameraSelectorSourceImpl,
    ): CameraSelectorSource

    @Binds
    @ActivityRetainedScoped
    abstract fun bindZoomLensRouter(
        impl: ZoomLensRouterImpl,
    ): ZoomLensRouter

    @Binds
    @ActivityRetainedScoped
    abstract fun bindVideoRecordingSession(
        impl: VideoRecordingSessionImpl,
    ): VideoRecordingSession
}
