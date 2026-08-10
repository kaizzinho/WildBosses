package com.kaizzinho.wildbosses.api

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID

class WildBossLootAwardEvent(
    val entityUuid: UUID,
    val player: ServerPlayer?,
    val level: Level,
    val position: Vec3,
    val speciesName: String,
    val tierName: String,
    val stacks: List<ItemStack>
) {
    var claimed: Boolean = false
        private set

    fun claim() { claimed = true }
}

object WildBossLootEvents {
    private val listeners = mutableListOf<(WildBossLootAwardEvent) -> Unit>()

    fun subscribe(listener: (WildBossLootAwardEvent) -> Unit) {
        listeners.add(listener)
    }

    fun fire(event: WildBossLootAwardEvent) {
        listeners.forEach { it(event) }
    }
}
