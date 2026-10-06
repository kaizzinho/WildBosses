package com.kaizzinho.wildbosses.mixin.client;

import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.kaizzinho.wildbosses.boss.BossTier;
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
        if (entity.getTeam() == null) {
            return;
        }

        String teamName = entity.getTeam().getName();
        if (!teamName.startsWith("wildbosses_")) {
            return;
        }

        String tierName = teamName.substring("wildbosses_".length());
        BossTier tier = null;
        for (BossTier candidate : BossTier.values()) {
            if (candidate.name().equalsIgnoreCase(tierName)) {
                tier = candidate;
                break;
            }
        }
        if (tier == null) {
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
