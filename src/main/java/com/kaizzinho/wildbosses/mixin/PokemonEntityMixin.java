package com.kaizzinho.wildbosses.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.kaizzinho.wildbosses.boss.WildBossEntityData;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin {

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void wildbosses$defineSyncedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(WildBossEntityData.IS_BOSS, false);
        builder.define(WildBossEntityData.TIER, "");
    }
}