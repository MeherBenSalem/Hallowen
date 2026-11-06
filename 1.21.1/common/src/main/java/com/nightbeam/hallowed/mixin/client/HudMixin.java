package com.nightbeam.hallowed.mixin.client;

import com.nightbeam.hallowed.client.NightOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the Hallowed vignette under the HUD and the title cards over it, the same way on every loader. Like vanilla's
 * own overlays, both stay away while the HUD is hidden (F1).
 */
@Mixin(Gui.class)
public abstract class HudMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void hallowed$under(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!Minecraft.getInstance().options.hideGui) NightOverlay.renderUnder(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void hallowed$over(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!Minecraft.getInstance().options.hideGui) NightOverlay.renderOver(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
    }
}
