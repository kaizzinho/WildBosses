package com.kaizzinho.wildbosses.boss

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.saveddata.SavedData
import java.util.UUID

class MegaStoneGrantData : SavedData() {

    private val granted = mutableMapOf<UUID, MutableSet<String>>()

    fun hasBeenGranted(playerUuid: UUID, speciesLowercase: String): Boolean =
        granted[playerUuid]?.contains(speciesLowercase) == true

    fun markGranted(playerUuid: UUID, speciesLowercase: String) {
        granted.getOrPut(playerUuid) { mutableSetOf() }.add(speciesLowercase)
        setDirty()
    }

    override fun save(compoundTag: CompoundTag, provider: HolderLookup.Provider): CompoundTag {
        val listTag = ListTag()
        granted.forEach { (uuid, species) ->
            val entry = CompoundTag()
            entry.putUUID("Player", uuid)
            val speciesTag = ListTag()
            species.forEach { speciesTag.add(StringTag.valueOf(it)) }
            entry.put("Species", speciesTag)
            listTag.add(entry)
        }
        compoundTag.put("Granted", listTag)
        return compoundTag
    }

    companion object {
        private const val DATA_NAME = "wildbosses_mega_stone_grants"
        private const val COMPOUND_TAG_TYPE_ID = 10
        private const val STRING_TAG_TYPE_ID = 8

        private val FACTORY = Factory(
            { MegaStoneGrantData() },
            { tag, _ -> load(tag) },
            DataFixTypes.LEVEL
        )

        private fun load(tag: CompoundTag): MegaStoneGrantData {
            val data = MegaStoneGrantData()
            val listTag = tag.getList("Granted", COMPOUND_TAG_TYPE_ID)
            for (i in 0 until listTag.size) {
                val entry = listTag.getCompound(i)
                val uuid = entry.getUUID("Player")
                val speciesTag = entry.getList("Species", STRING_TAG_TYPE_ID)
                val speciesSet = (0 until speciesTag.size).map { speciesTag.getString(it) }.toMutableSet()
                data.granted[uuid] = speciesSet
            }
            return data
        }

        fun get(overworld: ServerLevel): MegaStoneGrantData {
            return overworld.dataStorage.computeIfAbsent(FACTORY, DATA_NAME)
        }
    }
}
