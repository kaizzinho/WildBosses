package com.kaizzinho.wildbosses.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.kaizzinho.wildbosses.boss.WildBossEntityData;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
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

    // Bosses use WildBosses's own custom loot tables (see BossBattleResultListener), applied
    // manually on BATTLE_VICTORY. Cancel Cobblemon's normal wild-Pokémon death loot for bosses
    // specifically, so they don't drop both the custom loot AND the species' default drop.
    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    private void wildbosses$cancelDefaultLootForBosses(net.minecraft.server.level.ServerLevel world, DamageSource source, CallbackInfo ci) {
        PokemonEntity self = (PokemonEntity) (Object) this;
        if (self.getTags().contains("wildbosses:is_boss")) {
            ci.cancel();
        }
    }
}