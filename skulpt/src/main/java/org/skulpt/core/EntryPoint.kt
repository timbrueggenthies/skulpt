package org.skulpt.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.android.filament.Engine

@Composable
fun rememberScene(): SkulptScene {
    if (LocalInspectionMode.current) return StubSkulptScene
    val instance = LocalInstance.current
    return remember { ActualSkulptScene(instance.engine) }
}

@Composable
fun Skulpt(content: @Composable () -> Unit) {
    if (LocalInspectionMode.current) {
        content()
        return
    }
    val instance = remember { SkulptInstance() }
    CompositionLocalProvider(LocalInstance provides instance, content = content)
    DisposableEffect(instance) {
        onDispose { instance.destroy() }
    }
}

@Composable
fun Scene(scene: SkulptScene = rememberScene(), composable: @Composable () -> Unit) {
    if (LocalInspectionMode.current) return
    val engine = LocalInstance.current.engine
    val compositionContext = rememberCompositionContext()
    DisposableEffect(compositionContext, scene) {
        val composition = composeScene(scene, engine, compositionContext, composable)
        onDispose {
            composition.dispose()
        }
    }
}

private fun composeScene(
    scene: SkulptScene,
    engine: Engine,
    parent: CompositionContext,
    composable: @Composable () -> Unit
): Composition {
    val rootNode = SkulptNode(engine.entityManager.create(), engine.transformManager).apply { scene.addNode(this) }
    val composition = Composition(SkulptNodeApplier(rootNode), parent)
    composition.setContent {
        composable()
    }
    return composition
}
