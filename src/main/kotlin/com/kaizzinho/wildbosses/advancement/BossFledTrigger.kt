package com.kaizzinho.wildbosses.advancement

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.advancements.critereon.SimpleCriterionTrigger
import net.minecraft.server.level.ServerPlayer
import java.util.Optional

/** Fired when a player successfully flees a boss battle. No conditions - the act is the achievement. */
class BossFledTrigger : SimpleCriterionTrigger<BossFledTrigger.Instance>() {

    override fun codec(): Codec<Instance> = Instance.CODEC

    fun trigger(player: ServerPlayer) {
        this.trigger(player) { true }
    }

    data class Instance(
        val playerPredicate: Optional<ContextAwarePredicate>
    ) : SimpleInstance {

        override fun player(): Optional<ContextAwarePredicate> = playerPredicate

        companion object {
            val CODEC: Codec<Instance> = RecordCodecBuilder.create { builder ->
                builder.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::playerPredicate)
                ).apply(builder, ::Instance)
            }
        }
    }
}