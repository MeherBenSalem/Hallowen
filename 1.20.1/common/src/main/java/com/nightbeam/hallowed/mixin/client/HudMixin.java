package com.nightbeam.hallowed.mixin.client;

import com.nightbeam.hallowed.client.NightOverlay;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the Hallowed vignette under the HUD and the title cards over it. Like vanilla's own overlays, both stay away
 * while the HUD is hidden (F1). Forge replaces this method with its own overlay system, so there the Forge client
 * registers the same two calls as GUI overlays instead.
 */
@Mixin(Gui.class)
public abstract class HudMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void hallowed$under(GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        NightOverlay.renderUnder(graphics, partialTick);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void hallowed$over(GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        NightOverlay.renderOver(graphics, partialTick);
    }
}
