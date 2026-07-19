package com.kaizzinho.wildbosses.event

import com.kaizzinho.wildbosses.boss.BossLifecycleTicker
import com.kaizzinho.wildbosses.spawn.BossSpawnListener
import com.kaizzinho.wildbosses.battle.BattleTierScalingListener

object ModEvents {
    fun register() {
        BossSpawnListener.register()
        BossLifecycleTicker.register()
        BattleTierScalingListener.register()
    }
}