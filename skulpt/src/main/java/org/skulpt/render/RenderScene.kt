package org.skulpt.render

import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.collect
import org.skulpt.core.LocalInstance
import org.skulpt.core.SkulptCamera
import org.skulpt.core.SkulptScene

@Composable
fun rememberView(scene: SkulptScene, camera: SkulptCamera): SceneView {
    val sceneView = remember(scene) { scene.createView() }
    LaunchedEffect(camera) { sceneView.setCamera(camera) }
    DisposableEffect(key1 = sceneView) {
        val currentView = sceneView
        onDispose { currentView.destroy() }
    }
    LaunchedEffect(sceneView, camera) {
        sceneView.size.collect {
            camera.setAspectRatio(it.width.toDouble() / it.height.toDouble())
        }
    }
    return sceneView
}

@Composable
fun RenderScene(sceneView: SceneView, modifier: Modifier = Modifier) {
    when (sceneView) {
        is ActualSceneView -> RenderActualScene(sceneView, modifier)
        StubSceneView -> RenderPreviewPlaceholder()
    }
}

@Composable
private fun RenderActualScene(sceneView: SceneView, modifier: Modifier) {
    val instance = LocalInstance.current
    val engine = instance.engine
    var filamentWindow: FilamentWindow? by remember { mutableStateOf(null) }
    AndroidView(
        factory = { context ->
            SurfaceView(context).apply {
                filamentWindow = FilamentWindow(sceneView, engine, this)
            }
        },
        modifier = modifier
    )
    LaunchedEffect(filamentWindow) {
        if (filamentWindow == null) return@LaunchedEffect
        filamentWindow?.resumeUntilCancelled()
    }
}
