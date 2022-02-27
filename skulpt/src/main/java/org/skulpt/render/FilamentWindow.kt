package org.skulpt.render

import android.view.Choreographer
import android.view.Surface
import android.view.SurfaceView
import com.google.android.filament.Engine
import com.google.android.filament.Renderer
import com.google.android.filament.SwapChain
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import kotlinx.coroutines.awaitCancellation

internal class FilamentWindow(
    private val sceneView: SceneView,
    private val engine: Engine,
    private val surfaceView: SurfaceView
) : Choreographer.FrameCallback {

    private val uiHelper: UiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    private var displayHelper: DisplayHelper = DisplayHelper(surfaceView.context)
    private var swapChain: SwapChain? = null
    private lateinit var renderer: Renderer

    private var active: Boolean = false

    init {
        uiHelper.renderCallback = SurfaceCallback()
        uiHelper.attachTo(surfaceView)
        addDetachListener(surfaceView)
    }

    suspend fun resumeUntilCancelled() {
        Choreographer.getInstance().postFrameCallback(this)
        active = true
        try {
            awaitCancellation()
        } finally {
            Choreographer.getInstance().removeFrameCallback(this)
        }
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!active) return
        Choreographer.getInstance().postFrameCallback(this)
        render(frameTimeNanos)
    }

    private fun render(frameTimeNanos: Long) {
        if (!uiHelper.isReadyToRender) {
            return
        }

        // Render the scene, unless the renderer wants to skip the frame.
        if (renderer.beginFrame(swapChain!!, frameTimeNanos)) {
            sceneView.render(renderer)
            renderer.endFrame()
        }
    }

    private fun addDetachListener(view: android.view.View) {
        class AttachListener : android.view.View.OnAttachStateChangeListener {
            var detached = false

            override fun onViewAttachedToWindow(v: android.view.View?) {
                renderer = engine.createRenderer()
                detached = false
            }

            override fun onViewDetachedFromWindow(v: android.view.View?) {
                if (!detached) {
                    uiHelper.detach()
                    engine.destroyRenderer(renderer)

                    detached = true
                }
            }
        }
        view.addOnAttachStateChangeListener(AttachListener())
    }

    inner class SurfaceCallback : UiHelper.RendererCallback {
        override fun onNativeWindowChanged(surface: Surface) {
            swapChain?.let { engine.destroySwapChain(it) }
            swapChain = engine.createSwapChain(surface)
            displayHelper.attach(renderer, surfaceView.display)
        }

        override fun onDetachedFromSurface() {
            displayHelper.detach()
            swapChain?.let {
                engine.destroySwapChain(it)
                engine.flushAndWait()
                swapChain = null
            }
        }

        override fun onResized(width: Int, height: Int) {
            sceneView.onResized(width, height)
        }
    }
}
