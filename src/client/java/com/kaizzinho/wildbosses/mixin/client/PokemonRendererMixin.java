package com.kaizzinho.wildbosses.mixin.client;

import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.kaizzinho.wildbosses.boss.BossTier;
import com.kaizzinho.wildbosses.boss.WildBossEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PokemonRenderer.class)
public abstract class PokemonRendererMixin {

    @Inject(method = "resolveBaseLabel", at = @At("HEAD"), cancellable = true, remap = false)
    private void wildbosses$resolveBossLabel(PokemonEntity entity, CallbackInfoReturnable<MutableComponent> cir) {
        if (!entity.getEntityData().get(WildBossEntityData.IS_BOSS)) {
            return;
        }
        String tierName = entity.getEntityData().get(WildBossEntityData.TIER);
        BossTier tier;
        try {
            tier = BossTier.valueOf(tierName);
        } catch (IllegalArgumentException e) {
            return;
        }

        MutableComponent display = Component.translatable(
                "wildbosses.label.boss_full",
                Component.translatable("wildbosses.tier." + tier.name().toLowerCase()),
                entity.getPokemon().getSpecies().getTranslatedName()
        ).setStyle(Style.EMPTY.withColor(tier.getColor()));

        cir.setReturnValue(display);
    }
}