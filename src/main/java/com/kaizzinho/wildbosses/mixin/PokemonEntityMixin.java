package com.kaizzinho.wildbosses.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.kaizzinho.wildbosses.boss.WildBossEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin {

    private static final String BOSS_TAG = "wildbosses:is_boss";
    private static final String TIER_TAG_PREFIX = "wildbosses:tier_";
    private static final String SPAWN_TICK_TAG_PREFIX = "wildbosses:spawned_at_";
    private static final String NBT_BOSS = "wildbosses:boss";
    private static final String NBT_TIER = "wildbosses:tier";
    private static final String NBT_SPAWNED_AT = "wildbosses:spawned_at";

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void wildbosses$defineSyncedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(WildBossEntityData.IS_BOSS, false);
        builder.define(WildBossEntityData.TIER, "");
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void wildbosses$saveBossState(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        PokemonEntity self = (PokemonEntity) (Object) this;
        boolean isBoss = self.getTags().contains(BOSS_TAG) || self.getEntityData().get(WildBossEntityData.IS_BOSS);
        if (!isBoss) {
            return;
        }

        CompoundTag output = cir.getReturnValue();
        output.putBoolean(NBT_BOSS, true);

        String tier = self.getEntityData().get(WildBossEntityData.TIER);
        if (tier == null || tier.isBlank()) {
            for (String tag : self.getTags()) {
                if (tag.startsWith(TIER_TAG_PREFIX)) {
                    tier = tag.substring(TIER_TAG_PREFIX.length());
                    break;
                }
            }
        }
        if (tier != null && !tier.isBlank()) {
            output.putString(NBT_TIER, tier);
        }

        for (String tag : self.getTags()) {
            if (tag.startsWith(SPAWN_TICK_TAG_PREFIX)) {
                try {
                    output.putLong(NBT_SPAWNED_AT, Long.parseLong(tag.substring(SPAWN_TICK_TAG_PREFIX.length())));
                } catch (NumberFormatException ignored) {
                }
                break;
            }
        }

        System.out.println(
                "[BossPersistence-DEBUG] NBT_SAVE species=" + self.getPokemon().getSpecies().getName() +
                        " entityUuid=" + self.getUUID() +
                        " nbtBoss=" + output.getBoolean(NBT_BOSS) +
                        " nbtTier=" + (output.contains(NBT_TIER) ? output.getString(NBT_TIER) : "<missing>") +
                        " nbtSpawnedAt=" + (output.contains(NBT_SPAWNED_AT) ? output.getLong(NBT_SPAWNED_AT) : -1L)
        );
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void wildbosses$loadBossState(CompoundTag nbt, CallbackInfo ci) {
        PokemonEntity self = (PokemonEntity) (Object) this;
        boolean isBoss = nbt.getBoolean(NBT_BOSS) || self.getTags().contains(BOSS_TAG);
        if (!isBoss) {
            return;
        }

        self.setPersistenceRequired();
        self.getTags().add(BOSS_TAG);
        self.getEntityData().set(WildBossEntityData.IS_BOSS, true);
        self.setGlowingTag(true);

        String tier = nbt.contains(NBT_TIER) ? nbt.getString(NBT_TIER) : "";
        if (tier.isBlank()) {
            for (String tag : self.getTags()) {
                if (tag.startsWith(TIER_TAG_PREFIX)) {
                    tier = tag.substring(TIER_TAG_PREFIX.length());
                    break;
                }
            }
        }

        if (!tier.isBlank()) {
            final String restoredTier = tier;
            self.getTags().removeIf(tag -> tag.startsWith(TIER_TAG_PREFIX));
            self.getTags().add(TIER_TAG_PREFIX + restoredTier);
            self.getEntityData().set(WildBossEntityData.TIER, restoredTier);
        }

        if (nbt.contains(NBT_SPAWNED_AT)) {
            long spawnedAt = nbt.getLong(NBT_SPAWNED_AT);
            self.getTags().removeIf(tag -> tag.startsWith(SPAWN_TICK_TAG_PREFIX));
            self.getTags().add(SPAWN_TICK_TAG_PREFIX + spawnedAt);
        }

        System.out.println(
                "[BossPersistence-DEBUG] NBT_LOAD species=" + self.getPokemon().getSpecies().getName() +
                        " entityUuid=" + self.getUUID() +
                        " nbtBoss=" + nbt.getBoolean(NBT_BOSS) +
                        " legacyBossTag=" + self.getTags().contains(BOSS_TAG) +
                        " restoredTier=" + (tier.isBlank() ? "<missing>" : tier) +
                        " restoredSpawnedAt=" + (nbt.contains(NBT_SPAWNED_AT) ? nbt.getLong(NBT_SPAWNED_AT) : -1L)
        );
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    private void wildbosses$cancelDefaultLootForBosses(net.minecraft.server.level.ServerLevel world, DamageSource source, CallbackInfo ci) {
        PokemonEntity self = (PokemonEntity) (Object) this;
        boolean isBoss = self.getTags().contains(BOSS_TAG);
        System.out.println("[WildBosses-DEBUG] dropAllDeathLoot fired for " + self.getPokemon().getSpecies().getName() + ", isBoss=" + isBoss + ", tags=" + self.getTags());
        if (isBoss) {
            ci.cancel();
        }
    }
}
