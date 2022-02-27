package org.skulpt.core

import com.google.android.filament.TransformManager
import com.google.android.filament.utils.Mat4

class SkulptNode(
    internal val entityId: Int,
    private val transformManager: TransformManager,
    private val fixedParent: Boolean = false
) {

    private val transformInstance: Int get() = transformManager.getInstance(entityId)

    init {
        if (!transformManager.hasComponent(entityId)) {
            transformManager.create(entityId)
        }
    }

    private val tempMatrixArray = FloatArray(16)

    var localMatrix: Mat4
        set(value) {
            transformManager.setTransform(transformInstance, value.toFloatArray())
        }
        get() {
            transformManager.getTransform(transformInstance, tempMatrixArray)
            return Mat4.of(*tempMatrixArray)
        }

    private val _children = mutableListOf<SkulptNode>()
    val children: List<SkulptNode> get() = _children

    var parent: SkulptNode? = null
        set(value) {
            if (!fixedParent) {
                transformManager.setParent(transformInstance, value?.transformInstance ?: 0)
            }
            field = parent
        }

    private var attachedScene: ActualSkulptScene? = null

    fun addChild(child: SkulptNode, index: Int = -1) {
        check(child.parent == null) { "Node already has a parent" }
        if (index == -1) _children.add(child)
        else _children.add(index, child)
        child.parent = this
        attachedScene?.let { child.attach(it) }
    }

    fun removeChild(child: SkulptNode) {
        _children.remove(child)
        child.detach()
        child.parent = null
    }

    fun removeChildren(index: Int, count: Int = 1) {
        children.subList(index, index + count).forEach { removeChild(it) }
    }

    fun moveChildren(from: Int, to: Int, count: Int = 1) {
        val toMove = children.subList(from, from + count)
        toMove.forEach { _children.remove(it) }
        toMove.forEachIndexed { index, node -> _children.add(to - count + index, node) }
    }

    fun clear() {
        children.forEach { removeChild(it) }
    }

    fun attach(scene: ActualSkulptScene) {
        scene.scene.addEntity(entityId)
        attachedScene = scene
        children.forEach { it.attach(scene) }
    }

    fun detach() {
        children.forEach { it.detach() }
        attachedScene?.let { scene ->
            scene.scene.removeEntity(entityId)
        }
        attachedScene = null
    }
}
