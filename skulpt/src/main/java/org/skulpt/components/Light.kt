package org.skulpt.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.android.filament.LightManager
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.lookTowards
import com.google.android.filament.utils.transpose
import org.skulpt.core.LocalInstance
import org.skulpt.core.NodeWithTransform

@Composable
fun DirectionalLight(config: LightConfig, direction: Float3) {
    val engine = LocalInstance.current.engine
    val entityId = remember { engine.entityManager.create() }
    NodeWithTransform(entityId = entityId, transform = transpose(lookTowards(Float3(), direction)))
    DisposableEffect(Unit) {
        val lightBuilder = LightManager.Builder(LightManager.Type.DIRECTIONAL)
        lightBuilder.build(engine, entityId)
        onDispose {
            val light = engine.lightManager.getInstance(entityId)
            engine.lightManager.destroy(light)
            engine.entityManager.destroy(entityId)
        }
    }
    LaunchedEffect(key1 = config) {
        val light = engine.lightManager.getInstance(entityId)
        with(config.color) {
            engine.lightManager.setColor(light, red, green, blue)
        }
        engine.lightManager.setShadowCaster(light, config.castShadows)
        engine.lightManager.setIntensity(light, config.intensity)
    }
}

data class LightConfig(
    val intensity: Float,
    val castShadows: Boolean,
    val color: Color
)
