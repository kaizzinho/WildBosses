package com.kaizzinho.wildbosses.mixin.client;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.kaizzinho.wildbosses.boss.WildBossEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityLabelMixin {

    @Inject(method = "labelLevel", at = @At("HEAD"), cancellable = true, remap = false)
    private void wildbosses$hideLevelForBosses(CallbackInfoReturnable<Integer> cir) {
        PokemonEntity self = (PokemonEntity) (Object) this;
        if (self.getEntityData().get(WildBossEntityData.IS_BOSS)) {
            cir.setReturnValue(-1);
        }
    }
}
