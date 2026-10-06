package com.kaizzinho.wildbosses.mixin.client;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityLabelMixin {

    @Inject(method = "labelLevel", at = @At("HEAD"), cancellable = true, remap = false)
    private void wildbosses$hideLevelForBosses(CallbackInfoReturnable<Integer> cir) {
        PokemonEntity self = (PokemonEntity) (Object) this;
        String teamName = self.getTeam() != null ? self.getTeam().getName() : "";
        if (teamName.startsWith("wildbosses_")) {
            cir.setReturnValue(-1);
        }
    }
}
