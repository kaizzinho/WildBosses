package com.kaizzinho.wildbosses.mixin.client;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityGlowMixin {

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void wildbosses$forceBossTeamGlow(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof PokemonEntity pokemon)) {
            return;
        }
        if (pokemon.getTeam() != null && pokemon.getTeam().getName().startsWith("wildbosses_")) {
            cir.setReturnValue(true);
        }
    }
}
