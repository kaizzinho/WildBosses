package com.kaizzinho.wildbosses.mixin;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.kaizzinho.wildbosses.battle.BossBattleRestrictions;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BattleActor.class)
public abstract class BattleActorMixin {
    @ModifyVariable(method = "setActionResponses", at = @At("HEAD"), argsOnly = true)
    private List<? extends ShowdownActionResponse> wildbosses$filterActionResponses(
        List<? extends ShowdownActionResponse> responses
    ) {
        return BossBattleRestrictions.filterActionResponses((BattleActor) (Object) this, responses);
    }
}
