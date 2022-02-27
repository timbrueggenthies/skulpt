package org.skulpt.core

import com.google.android.filament.Engine
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.Gltfio
import com.google.android.filament.gltfio.UbershaderLoader

class SkulptInstance {

    init {
        Gltfio.init()
    }

    internal val engine: Engine = Engine.create()

    internal val assetLoader = AssetLoader(engine, UbershaderLoader(engine), engine.entityManager)

    fun destroy() {
        engine.destroy()
    }
}
