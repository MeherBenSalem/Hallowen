package com.nightbeam.hallowed.mixin;

import com.nightbeam.hallowed.spawn.HallowedSpawns;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Costumed monsters on Hallowed Nights. */
@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void hallowed$costume(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData groupData,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        HallowedSpawns.onFinalizeSpawn((Mob) (Object) this, level.getLevel(), reason);
    }
}
