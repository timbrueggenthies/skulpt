package org.skulpt.core

import androidx.compose.runtime.AbstractApplier
import org.skulpt.core.SkulptNode

/**
 * SceneNodeApplier
 *
 * Create by adesso mobile solutions GmbH on 23.10.2021
 */
internal class SkulptNodeApplier(root: SkulptNode) : AbstractApplier<SkulptNode>(root) {

    override fun onClear() {
        root.clear()
    }

    override fun insertBottomUp(index: Int, instance: SkulptNode) {
        current.addChild(instance, index)
    }

    override fun insertTopDown(index: Int, instance: SkulptNode) {
        // We insert bottom-up
    }

    override fun move(from: Int, to: Int, count: Int) {
        current.moveChildren(from, to, count)
    }

    override fun remove(index: Int, count: Int) {
        current.removeChildren(index, count)
    }
}
