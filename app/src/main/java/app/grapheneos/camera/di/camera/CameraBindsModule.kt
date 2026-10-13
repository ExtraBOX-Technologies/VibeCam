package app.grapheneos.camera.di.camera

import app.grapheneos.camera.data.camera.mapper.CameraLensMapMapper
import app.grapheneos.camera.data.camera.mapper.CameraLensMapMapperImpl
import app.grapheneos.camera.data.camera.mapper.CameraXConstantsMapper
import app.grapheneos.camera.data.camera.mapper.CameraXConstantsMapperImpl
import app.grapheneos.camera.data.camera.mapper.CameraXStateMapper
import app.grapheneos.camera.data.camera.mapper.CameraXStateMapperImpl
import app.grapheneos.camera.data.camera.mapper.LensDataStatusMapper
import app.grapheneos.camera.data.camera.mapper.LensDataStatusMapperImpl
import app.grapheneos.camera.data.camera.mapper.LensZoomRatioMapper
import app.grapheneos.camera.data.camera.mapper.LensZoomRatioMapperImpl
import app.grapheneos.camera.data.camera.mapper.RecordingEventMapper
import app.grapheneos.camera.data.camera.mapper.RecordingEventMapperImpl
import app.grapheneos.camera.data.camera.mapper.VideoQualityFeatureMapper
import app.grapheneos.camera.data.camera.mapper.VideoQualityFeatureMapperImpl
import app.grapheneos.camera.data.camera.repository.CameraProviderSource
import app.grapheneos.camera.data.camera.repository.CameraProviderSourceImpl
import app.grapheneos.camera.data.camera.repository.ExtensionAvailabilityRepository
import app.grapheneos.camera.data.camera.repository.ExtensionAvailabilityRepositoryImpl
import app.grapheneos.camera.data.camera.repository.LensReportRepository
import app.grapheneos.camera.data.camera.repository.LensReportRepositoryImpl
import app.grapheneos.camera.data.camera.session.CameraLensSource
import app.grapheneos.camera.data.camera.session.CameraLensSourceImpl
import app.grapheneos.camera.data.camera.session.CameraSessionPlanFactory
import app.grapheneos.camera.data.camera.session.CameraSessionPlanFactoryImpl
import app.grapheneos.camera.data.camera.session.FeatureCombinationSupport
import app.grapheneos.camera.data.camera.session.FeatureCombinationSupportImpl
import app.grapheneos.camera.data.camera.session.InVideoSnapshotSupportResolver
import app.grapheneos.camera.data.camera.session.InVideoSnapshotSupportResolverImpl
import app.grapheneos.camera.data.camera.session.JpegExtractor
import app.grapheneos.camera.data.camera.session.JpegExtractorImpl
import app.grapheneos.camera.data.camera.session.SnapshotProbeCache
import app.grapheneos.camera.data.camera.session.SnapshotProbeCacheImpl
import app.grapheneos.camera.domain.camera.usecase.InspectCameraLenses
import app.grapheneos.camera.domain.camera.usecase.InspectCameraLensesImpl
import app.grapheneos.camera.domain.camera.usecase.ResolveAvailableModes
import app.grapheneos.camera.domain.camera.usecase.ResolveAvailableModesImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CameraBindsModule {

    @Binds
    @Singleton
    abstract fun bindExtensionAvailabilityRepository(
        impl: ExtensionAvailabilityRepositoryImpl,
    ): ExtensionAvailabilityRepository

    @Binds
    @Reusable
    abstract fun bindCameraProviderSource(
        impl: CameraProviderSourceImpl,
    ): CameraProviderSource

    @Binds
    @Reusable
    abstract fun bindCameraLensSource(
        impl: CameraLensSourceImpl,
    ): CameraLensSource

    @Binds
    @Reusable
    abstract fun bindLensZoomRatioMapper(
        impl: LensZoomRatioMapperImpl,
    ): LensZoomRatioMapper

    @Binds
    @Reusable
    abstract fun bindFeatureCombinationSupport(
        impl: FeatureCombinationSupportImpl,
    ): FeatureCombinationSupport

    @Binds
    @Reusable
    abstract fun bindCameraXConstantsMapper(
        impl: CameraXConstantsMapperImpl,
    ): CameraXConstantsMapper

    @Binds
    @Reusable
    abstract fun bindCameraXStateMapper(
        impl: CameraXStateMapperImpl,
    ): CameraXStateMapper

    @Binds
    @Reusable
    abstract fun bindRecordingEventMapper(
        impl: RecordingEventMapperImpl,
    ): RecordingEventMapper

    @Binds
    @Reusable
    abstract fun bindVideoQualityFeatureMapper(
        impl: VideoQualityFeatureMapperImpl,
    ): VideoQualityFeatureMapper

    @Binds
    @Reusable
    abstract fun bindCameraSessionPlanFactory(
        impl: CameraSessionPlanFactoryImpl,
    ): CameraSessionPlanFactory

    @Binds
    @Reusable
    abstract fun bindInspectCameraLenses(
        impl: InspectCameraLensesImpl,
    ): InspectCameraLenses

    @Binds
    @Reusable
    abstract fun bindCameraLensMapMapper(
        impl: CameraLensMapMapperImpl,
    ): CameraLensMapMapper

    @Binds
    @Reusable
    abstract fun bindLensDataStatusMapper(
        impl: LensDataStatusMapperImpl,
    ): LensDataStatusMapper

    @Binds
    @Singleton
    abstract fun bindLensReportRepository(
        impl: LensReportRepositoryImpl,
    ): LensReportRepository

    @Binds
    @Reusable
    abstract fun bindResolveAvailableModes(
        impl: ResolveAvailableModesImpl,
    ): ResolveAvailableModes

    @Binds
    @Reusable
    abstract fun bindInVideoSnapshotSupportResolver(
        impl: InVideoSnapshotSupportResolverImpl,
    ): InVideoSnapshotSupportResolver

    @Binds
    @Reusable
    abstract fun bindJpegExtractor(
        impl: JpegExtractorImpl,
    ): JpegExtractor

    @Binds
    @Singleton
    abstract fun bindSnapshotProbeCache(
        impl: SnapshotProbeCacheImpl,
    ): SnapshotProbeCache
}
