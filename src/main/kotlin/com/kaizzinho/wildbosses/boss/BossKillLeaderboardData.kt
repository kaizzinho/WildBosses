package com.kaizzinho.wildbosses.boss

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.saveddata.SavedData
import java.util.UUID



class BossKillLeaderboardData : SavedData() {

    data class KillRecord(var playerName: String, val killsByTier: MutableMap<String, Int> = mutableMapOf()) {
        fun total(): Int = killsByTier.values.sum()
    }

    private val records = mutableMapOf<UUID, KillRecord>()

    fun recordKill(playerUuid: UUID, playerName: String, tier: BossTier) {
        val record = records.getOrPut(playerUuid) { KillRecord(playerName) }
        record.playerName = playerName // keep it fresh in case the player changed their name
        record.killsByTier[tier.name] = (record.killsByTier[tier.name] ?: 0) + 1
        setDirty()
    }

    fun topByTotal(limit: Int): List<Pair<KillRecord, Int>> =
        records.values.map { it to it.total() }.sortedByDescending { it.second }.take(limit)

    fun topByTier(tier: BossTier, limit: Int): List<Pair<KillRecord, Int>> =
        records.values.mapNotNull { r -> r.killsByTier[tier.name]?.let { r to it } }
            .sortedByDescending { it.second }.take(limit)

    fun totalKillsFor(playerUuid: UUID): Int = records[playerUuid]?.total() ?: 0

    fun tiersDefeatedBy(playerUuid: UUID): Set<String> =
        records[playerUuid]?.killsByTier?.filterValues { it > 0 }?.keys ?: emptySet()

    override fun save(compoundTag: CompoundTag, provider: HolderLookup.Provider): CompoundTag {
        val listTag = ListTag()
        records.forEach { (uuid, record) ->
            val entry = CompoundTag()
            entry.putUUID("Player", uuid)
            entry.putString("Name", record.playerName)
            val tiersTag = CompoundTag()
            record.killsByTier.forEach { (tierName, count) -> tiersTag.putInt(tierName, count) }
            entry.put("Tiers", tiersTag)
            listTag.add(entry)
        }
        compoundTag.put("Records", listTag)
        return compoundTag
    }

    companion object {
        private const val DATA_NAME = "wildbosses_kill_leaderboard"
        private const val COMPOUND_TAG_TYPE_ID = 10

        private val FACTORY = Factory(
            { BossKillLeaderboardData() },
            { tag, _ -> load(tag) },
            DataFixTypes.LEVEL
        )

        private fun load(tag: CompoundTag): BossKillLeaderboardData {
            val data = BossKillLeaderboardData()
            val listTag = tag.getList("Records", COMPOUND_TAG_TYPE_ID)
            for (i in 0 until listTag.size) {
                val entry = listTag.getCompound(i)
                val uuid = entry.getUUID("Player")
                val name = entry.getString("Name")
                val record = KillRecord(name)
                val tiersTag = entry.getCompound("Tiers")
                for (key in tiersTag.allKeys) {
                    record.killsByTier[key] = tiersTag.getInt(key)
                }
                data.records[uuid] = record
            }
            return data
        }

        fun get(overworld: ServerLevel): BossKillLeaderboardData {
            return overworld.dataStorage.computeIfAbsent(FACTORY, DATA_NAME)
        }
    }

}