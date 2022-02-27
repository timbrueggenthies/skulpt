package org.skulpt.core

import com.google.android.filament.Engine
import com.google.android.filament.Skybox
import org.skulpt.render.ActualSceneView
import org.skulpt.render.SceneView
import org.skulpt.render.StubSceneView

sealed interface SkulptScene {
    fun createView(): SceneView
    fun addNode(node: SkulptNode)
    fun removeNode(node: SkulptNode)
}

object StubSkulptScene : SkulptScene {
    override fun createView(): SceneView = StubSceneView
    override fun addNode(node: SkulptNode) {}
    override fun removeNode(node: SkulptNode) {}
}

class ActualSkulptScene internal constructor(internal val engine: Engine) : SkulptScene {
    internal val scene = engine.createScene()

    private val rootNode =
        SkulptNode(engine.entityManager.create(), engine.transformManager).also { it.attach(this) }

    var skybox: Skybox?
        get() = scene.skybox
        set(value) {
            scene.skybox = value
        }

    init {
        skybox = Skybox.Builder().color(0.05f, 0.05f, 0.1f, 1f).build(engine)
    }

    override fun addNode(node: SkulptNode) {
        rootNode.addChild(node)
    }

    override fun removeNode(node: SkulptNode) {
        require(node !== rootNode) { "Cannot remove root node from scene" }
        removeNode(rootNode.children, node)
    }

    override fun createView(): SceneView {
        val view = engine.createView().apply {
            scene = this@ActualSkulptScene.scene
        }
        return ActualSceneView(engine, view)
    }

    private tailrec fun removeNode(nodes: List<SkulptNode>, removeNode: SkulptNode) {
        if (nodes.isEmpty()) return
        nodes.forEach {
            if (removeNode === it) {
                it.parent?.removeChild(it)
                return
            }
        }
        removeNode(nodes.flatMap { it.children }, removeNode)
    }
}
