package com.kaizzinho.wildbosses.boss

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.saveddata.SavedData
import java.util.UUID

class BossSpawnCooldownData : SavedData() {

    // player UUID -> game tick their cooldown expires at
    private val cooldowns = mutableMapOf<UUID, Long>()

    fun isOnCooldown(playerUuid: UUID, currentTick: Long): Boolean {
        val expiresAt = cooldowns[playerUuid] ?: return false
        return currentTick < expiresAt
    }

    fun startCooldown(playerUuid: UUID, currentTick: Long, cooldownTicks: Long) {
        cooldowns[playerUuid] = currentTick + cooldownTicks
        setDirty()
    }

    override fun save(compoundTag: CompoundTag, provider: HolderLookup.Provider): CompoundTag {
        val list = cooldowns.map { (uuid, expiresAt) ->
            val entry = CompoundTag()
            entry.putUUID("Player", uuid)
            entry.putLong("ExpiresAt", expiresAt)
            entry
        }
        val listTag = net.minecraft.nbt.ListTag()
        list.forEach { listTag.add(it) }
        compoundTag.put("Cooldowns", listTag)
        return compoundTag
    }

    companion object {
        private const val DATA_NAME = "wildbosses_spawn_cooldowns"
        private const val COMPOUND_TAG_TYPE_ID = 10 // NBT type ID for CompoundTag - stable across all Minecraft versions

        private val FACTORY = Factory(
            { BossSpawnCooldownData() },
            { tag, _ -> load(tag) },
            DataFixTypes.LEVEL
        )

        private fun load(tag: CompoundTag): BossSpawnCooldownData {
            val data = BossSpawnCooldownData()
            val listTag = tag.getList("Cooldowns", COMPOUND_TAG_TYPE_ID)
            for (i in 0 until listTag.size) {
                val entry = listTag.getCompound(i)
                val uuid = entry.getUUID("Player")
                val expiresAt = entry.getLong("ExpiresAt")
                data.cooldowns[uuid] = expiresAt
            }
            return data
        }

        fun get(overworld: ServerLevel): BossSpawnCooldownData {
            return overworld.dataStorage.computeIfAbsent(FACTORY, DATA_NAME)
        }
    }
}