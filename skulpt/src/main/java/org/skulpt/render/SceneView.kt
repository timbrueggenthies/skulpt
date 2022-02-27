package org.skulpt.render

import androidx.compose.ui.geometry.Size
import com.google.android.filament.Engine
import com.google.android.filament.Renderer
import com.google.android.filament.View
import com.google.android.filament.Viewport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.skulpt.core.ActualSkulptCamera
import org.skulpt.core.SkulptCamera

sealed interface SceneView {
    val size: StateFlow<Size>
    fun setCamera(camera: SkulptCamera)
    fun render(renderer: Renderer)
    fun onResized(width: Int, height: Int)
    fun destroy()
}

object StubSceneView : SceneView {
    override val size: StateFlow<Size> = MutableStateFlow(Size.Zero)
    override fun render(renderer: Renderer) {}
    override fun setCamera(camera: SkulptCamera) {}
    override fun onResized(width: Int, height: Int) {}
    override fun destroy() {}
}

class ActualSceneView(private val engine: Engine, private val view: View) : SceneView {
    private val _size = MutableStateFlow(Size.Zero)
    override val size: StateFlow<Size> = _size.asStateFlow()

    override fun setCamera(camera: SkulptCamera) {
        if (camera !is ActualSkulptCamera) return
        view.camera = camera.camera
    }

    override fun onResized(width: Int, height: Int) {
        view.viewport = Viewport(0, 0, width, height)
        _size.value = Size(width.toFloat(), height.toFloat())
    }

    override fun render(renderer: Renderer) {
        renderer.render(view)
    }

    override fun destroy() {
        engine.destroyView(view)
    }
}
