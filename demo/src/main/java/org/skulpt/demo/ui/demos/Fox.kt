package org.skulpt.demo.ui.demos

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.filament.Colors
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.lookAt
import com.google.android.filament.utils.rotation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.skulpt.components.DirectionalLight
import org.skulpt.components.GltfAnimatable
import org.skulpt.components.GltfModel
import org.skulpt.components.LightConfig
import org.skulpt.components.rememberGltfModel
import org.skulpt.core.Camera
import org.skulpt.core.Scene
import org.skulpt.core.Skulpt
import org.skulpt.core.rememberCamera
import org.skulpt.core.rememberScene
import org.skulpt.render.RenderScene
import org.skulpt.render.rememberView
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FoxDemo() {
    Skulpt {
        val transition = rememberInfiniteTransition()
        val rotation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * PI.toFloat(),
            animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing))
        )
        val scene = rememberScene()
        val camera = rememberCamera()
        val sceneView = rememberView(scene = scene, camera = camera)
        val foxModel by rememberGltfModel("Fox.glb")
        Scene(scene) {
            val (r, g, b) = Colors.cct(6_500.0f)
            DirectionalLight(
                LightConfig(intensity = 100_000f, true, Color(r, g, b)),
                Float3(x = 1f, z = 0f, y = -1f)
            )
            Camera(camera = camera, transform = lookAt(Float3(y = 80f, z = sin(rotation) * 400f, x = cos(rotation) * 400f), Float3(), Float3(y = 1f)))
            foxModel?.let { GltfModel(asset = it, rotation(Float3(y = 1f), 90f)) }
        }
        val coroutineScope = rememberCoroutineScope()
        val animatiable = remember(foxModel?.animators?.get(1)) { foxModel?.animators?.get(1)?.let(::GltfAnimatable) }
        RenderScene(
            sceneView = sceneView,
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    coroutineScope.launch {
                        while (currentCoroutineContext().isActive) {
                            animatiable?.animateTo(1f, tween(durationMillis = 1000))
                            animatiable?.snapTo(0f)
                        }
                    }
                }
        )
    }
}

@Preview
@Composable
fun FoxDemoPreview() {
    FoxDemo()
}
