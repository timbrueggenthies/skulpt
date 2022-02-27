package org.skulpt.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

class GltfAnimatable(
    private val animationState: AnimationState
) {
    private val duration: Float = animationState.duration
    private val durationMillis: Int = (duration * 1000f).toInt()

    suspend fun animateTo(
        progress: Float,
        animationSpec: AnimationSpec<Float> = tween(
            durationMillis = durationMillis,
            easing = LinearEasing
        )
    ) {
        val animation = TargetBasedAnimation(
            animationSpec = animationSpec,
            typeConverter = Float.VectorConverter,
            initialValue = animationState.progress,
            targetValue = progress
        )
        val startTime = withFrameNanos { it }
        while (animationState.progress != animation.targetValue && currentCoroutineContext().isActive) {
            val elapsed = withFrameNanos { it } - startTime
            val newProgress = animation.getValueFromNanos(elapsed)
            animationState.progress = newProgress
        }
    }

    fun snapTo(progress: Float) {
        animationState.progress = progress
    }
}
