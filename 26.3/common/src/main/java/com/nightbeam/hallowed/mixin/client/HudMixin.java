package com.nightbeam.hallowed.mixin.client;

import com.nightbeam.hallowed.client.NightOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the Hallowed vignette under the HUD and the title cards over it, the same way on every loader. */
@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void hallowed$under(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        NightOverlay.renderUnder(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void hallowed$over(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        NightOverlay.renderOver(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
    }
}
