package org.skulpt.core

import androidx.compose.runtime.staticCompositionLocalOf

val LocalInstance = staticCompositionLocalOf<SkulptInstance> {
    error("You should only call Skulpt components inside the `Skulpt { }` scope.")
}