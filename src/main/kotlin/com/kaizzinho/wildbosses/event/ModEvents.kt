package com.kaizzinho.wildbosses.event

import com.kaizzinho.wildbosses.spawn.BossSpawnListener
import com.kaizzinho.wildbosses.battle.BattleTierScalingListener
import com.kaizzinho.wildbosses.battle.BossBattleResultListener
import com.kaizzinho.wildbosses.boss.BossLifecycleTicker
import com.kaizzinho.wildbosses.boss.BossOrphanCleanupListener
import com.kaizzinho.wildbosses.battle.PlayerLevelCapListener
import com.kaizzinho.wildbosses.command.WildBossCommands

object ModEvents {
    fun register() {
        PlayerLevelCapListener.register()
        BossSpawnListener.register()
        BattleTierScalingListener.register()
        BossBattleResultListener.register()
        BossLifecycleTicker.register()
        BossOrphanCleanupListener.register()
        WildBossCommands.register()
    }
}