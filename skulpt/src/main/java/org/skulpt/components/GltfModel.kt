package org.skulpt.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.android.filament.Engine
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.utils.Mat4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.skulpt.core.ActualSkulptCamera
import org.skulpt.core.LocalInstance
import org.skulpt.core.Node
import org.skulpt.core.NodeWithTransform
import org.skulpt.core.SkulptCamera
import java.nio.ByteBuffer
import com.google.android.filament.gltfio.Animator as FilamentAnimator

@Composable
fun rememberGltfModel(assetFile: String): State<GltfAsset?> {
    if (LocalInspectionMode.current) return remember { mutableStateOf(null) }
    val instance = LocalInstance.current
    val context = LocalContext.current
    val engine = instance.engine
    val assetLoader = instance.assetLoader
    val gltfAsset = produceState<GltfAsset?>(initialValue = null) {
        val byteBuffer = withContext(Dispatchers.IO) { loadResource(context, assetFile) }
        val resourceLoader = ResourceLoader(engine)
        val asset = assetLoader.createAssetFromJson(byteBuffer) ?: error("Asset could not be loaded")

        for (uri in asset.resourceUris) {
            val buffer = withContext(Dispatchers.IO) { loadResource(context, uri) }
            resourceLoader.addResourceData(uri, buffer)
        }
        resourceLoader.loadResources(asset)
        value = GltfAsset(engine, asset)
        resourceLoader.evictResourceData()
    }

    DisposableEffect(key1 = gltfAsset.value) {
        val currentAsset = gltfAsset.value?.asset
        onDispose { currentAsset?.let(assetLoader::destroyAsset) }
    }
    return gltfAsset
}

private fun loadResource(context: Context, uri: String): ByteBuffer {
    return context.assets.open(uri).use {
        val bytes = it.readBytes()
        ByteBuffer.wrap(bytes)
    }
}

class GltfAsset(
    private val engine: Engine,
    internal val asset: FilamentAsset
) {
    val cameras = generateCameras()
    val animators: List<AnimationState> = generateAnimations()

    private fun generateCameras(): List<SkulptCamera> {
        return asset.cameraEntities.map { cameraEntity ->
            val camera =
                engine.getCameraComponent(cameraEntity) ?: engine.createCamera(cameraEntity)
            val name = asset.getName(cameraEntity)
            ActualSkulptCamera(cameraEntity, camera, name)
        }
    }

    private fun generateAnimations() = (0 until asset.animator.animationCount).map {
        AnimationState(asset.animator, it)
    }
}

class AnimationState(
    private val animator: FilamentAnimator,
    private val index: Int
) {
    val duration: Float get() = animator.getAnimationDuration(index)

    private val _progress = mutableStateOf(0f)
    var progress: Float
        get() = _progress.value
        set(value) {
            applyProgress(value)
            _progress.value = value
        }

    private fun applyProgress(progress: Float) {
        animator.applyAnimation(index, duration * progress)
        animator.updateBoneMatrices()
        _progress.value = progress
    }
}

@Composable
fun GltfModel(asset: GltfAsset, transform: Mat4 = Mat4.identity()) {
    NodeWithTransform(transform = transform) {
        NodeWithTransform(asset.asset.root) {
            asset.asset.entities.forEach { entityId ->
                Node(entityId)
            }
        }
    }
}
