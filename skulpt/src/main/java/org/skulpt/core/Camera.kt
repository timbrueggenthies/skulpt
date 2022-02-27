package org.skulpt.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.android.filament.Camera
import com.google.android.filament.Entity
import com.google.android.filament.utils.Mat4

@Composable
fun rememberCamera(): SkulptCamera {
    if (LocalInspectionMode.current) return StubSkulptCamera
    val engine = LocalInstance.current.engine
    val entityId = remember { engine.entityManager.create() }
    val cameraInstance = remember(entityId) { engine.createCamera(entityId) }
    DisposableEffect(Unit) {
        onDispose {
            engine.destroyCameraComponent(entityId)
            engine.entityManager.destroy(entityId)
        }
    }
    return remember { ActualSkulptCamera(entityId, cameraInstance) }
}

@Composable
fun Camera(camera: SkulptCamera = rememberCamera(), transform: Mat4 = Mat4.identity()) {
    if (camera !is ActualSkulptCamera) return
    NodeWithTransform(camera.entityId)
    LaunchedEffect(key1 = transform) {
        camera.modelMatrix = transform
    }
}

sealed interface SkulptCamera {
    fun setAspectRatio(aspectRatio: Double)
    fun setExposure(aperture: Float, shutterSpeed: Float, sensitivity: Float)
}

object StubSkulptCamera : SkulptCamera {
    override fun setAspectRatio(aspectRatio: Double) {}
    override fun setExposure(aperture: Float, shutterSpeed: Float, sensitivity: Float) {}
}

internal class ActualSkulptCamera(
    @Entity internal val entityId: Int,
    internal val camera: Camera,
    val name: String? = null
) : SkulptCamera {

    init {
        setExposure(kAperture, kShutterSpeed, kSensitivity)
    }

    private val tempMatrixArray = FloatArray(16)

    var modelMatrix: Mat4
        set(value) {
            val position = value.position
            val target = position + value.forward
            val up = value.up
            camera.lookAt(
                position.x.toDouble(), position.y.toDouble(), position.z.toDouble(),
                target.x.toDouble(), target.y.toDouble(), target.z.toDouble(),
                up.x.toDouble(), up.y.toDouble(), up.z.toDouble()
            )
        }
        get() {
            camera.getModelMatrix(tempMatrixArray)
            return Mat4.of(*tempMatrixArray)
        }

    override fun setAspectRatio(aspectRatio: Double) {
        camera.setProjection(
            kFovDegrees,
            aspectRatio,
            kNearPlane,
            kFarPlane,
            Camera.Fov.VERTICAL
        )
    }

    override fun setExposure(aperture: Float, shutterSpeed: Float, sensitivity: Float) {
        camera.setExposure(aperture, shutterSpeed, sensitivity)
    }

    private companion object CameraConstants {
        const val kNearPlane = 0.5
        const val kFarPlane = 1000000.0
        const val kFovDegrees = 45.0
        const val kAperture = 16f
        const val kShutterSpeed = 1f / 125f
        const val kSensitivity = 100f
    }
}
