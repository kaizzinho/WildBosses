package com.kaizzinho.wildbosses.event

import com.kaizzinho.wildbosses.spawn.BossSpawnListener
import com.kaizzinho.wildbosses.battle.BattleTierScalingListener
import com.kaizzinho.wildbosses.battle.BossBattleResultListener
import com.kaizzinho.wildbosses.boss.BossLifecycleTicker

object ModEvents {
    fun register() {
        BossSpawnListener.register()
        BattleTierScalingListener.register()
        BossBattleResultListener.register()
        BossLifecycleTicker.register()
    }
}