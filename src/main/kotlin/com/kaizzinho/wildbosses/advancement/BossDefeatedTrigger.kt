package com.kaizzinho.wildbosses.advancement

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.kaizzinho.wildbosses.boss.BossTier
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.advancements.critereon.SimpleCriterionTrigger
import net.minecraft.server.level.ServerPlayer
import java.util.Optional

data class BossDefeatedContext(
    val tier: BossTier,
    val wasShiny: Boolean,
    val megaEvolved: Boolean,
    val enraged: Boolean,
    val totalKills: Int,
    val tiersDefeated: Set<String>
)

class BossDefeatedTrigger : SimpleCriterionTrigger<BossDefeatedTrigger.Instance>() {

    override fun codec(): Codec<Instance> = Instance.CODEC

    fun trigger(player: ServerPlayer, context: BossDefeatedContext) {
        this.trigger(player) { it.matches(context) }
    }

    data class Instance(
        val playerPredicate: Optional<ContextAwarePredicate>,
        val tier: Optional<String>,
        val shiny: Optional<Boolean>,
        val megaEvolved: Optional<Boolean>,
        val enraged: Optional<Boolean>,
        val minKills: Optional<Int>,
        val allTiers: Optional<Boolean>
    ) : SimpleInstance {

        override fun player(): Optional<ContextAwarePredicate> = playerPredicate

        fun matches(ctx: BossDefeatedContext): Boolean {
            val tierFilter = tier.orElse(null)
            if (tierFilter != null && !tierFilter.equals(ctx.tier.name, ignoreCase = true)) return false

            val shinyFilter = shiny.orElse(null)
            if (shinyFilter != null && shinyFilter != ctx.wasShiny) return false

            val megaFilter = megaEvolved.orElse(null)
            if (megaFilter != null && megaFilter != ctx.megaEvolved) return false

            val enragedFilter = enraged.orElse(null)
            if (enragedFilter != null && enragedFilter != ctx.enraged) return false

            val minKillsFilter = minKills.orElse(null)
            if (minKillsFilter != null && ctx.totalKills < minKillsFilter) return false

            val allTiersFilter = allTiers.orElse(null)
            if (allTiersFilter == true && ctx.tiersDefeated.size < BossTier.entries.size) return false

            return true
        }

        companion object {
            val CODEC: Codec<Instance> = RecordCodecBuilder.create { builder ->
                builder.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::playerPredicate),
                    Codec.STRING.optionalFieldOf("tier").forGetter(Instance::tier),
                    Codec.BOOL.optionalFieldOf("shiny").forGetter(Instance::shiny),
                    Codec.BOOL.optionalFieldOf("mega_evolved").forGetter(Instance::megaEvolved),
                    Codec.BOOL.optionalFieldOf("enraged").forGetter(Instance::enraged),
                    Codec.INT.optionalFieldOf("min_kills").forGetter(Instance::minKills),
                    Codec.BOOL.optionalFieldOf("all_tiers").forGetter(Instance::allTiers)
                ).apply(builder, ::Instance)
            }
        }
    }
}
