package org.skulpt.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.remember
import com.google.android.filament.utils.Mat4
import com.google.android.filament.utils.transpose

@Composable
fun NodeWithTransform(
    transform: Mat4 = Mat4.identity(),
    childContent: @Composable () -> Unit = { }
) {
    val engine = LocalInstance.current.engine
    val entityId = remember { engine.entityManager.create() }
    NodeWithTransform(entityId = entityId, transform = transform, childContent = childContent)
    DisposableEffect(Unit) {
        onDispose { engine.entityManager.destroy(entityId) }
    }
}

@Composable
fun NodeWithTransform(
    entityId: Int,
    transform: Mat4 = Mat4.identity(),
    childContent: @Composable () -> Unit = { }
) {
    val engine = LocalInstance.current.engine
    ReusableComposeNode<SkulptNode, SkulptNodeApplier>(
        factory = { SkulptNode(entityId, engine.transformManager) },
        update = {
            set(transform) {
                localMatrix = transpose(transform)
            }
        },
        content = { childContent() }
    )
}

@Composable
fun Node(
    entityId: Int,
    childContent: @Composable () -> Unit = { }
) {
    val engine = LocalInstance.current.engine
    ReusableComposeNode<SkulptNode, SkulptNodeApplier>(
        factory = { SkulptNode(entityId, engine.transformManager, true) },
        update = { },
        content = { childContent() }
    )
}
