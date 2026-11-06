package com.nightbeam.hallowed.forge;

import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.client.HallowedClient;
import com.nightbeam.hallowed.client.HallowedItemModels;
import com.nightbeam.hallowed.client.HallowedParticles;
import com.nightbeam.hallowed.client.NightOverlay;
import com.nightbeam.hallowed.client.render.HorsemanModel;
import com.nightbeam.hallowed.client.render.HorsemanRenderer;
import com.nightbeam.hallowed.client.render.ModelLayersHallowed;
import com.nightbeam.hallowed.client.render.PumpkinBombRenderer;
import com.nightbeam.hallowed.client.render.SpiritModel;
import com.nightbeam.hallowed.client.render.SpiritRenderer;
import com.nightbeam.hallowed.client.render.WispRenderer;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.network.FxPayload;
import com.nightbeam.hallowed.network.NightPayload;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client wiring; only loaded on the client. */
final class HallowedForgeClient {
    private HallowedForgeClient() {
    }

    static void init(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {
            event.registerLayerDefinition(ModelLayersHallowed.SPIRIT, SpiritModel::createBodyLayer);
            event.registerLayerDefinition(ModelLayersHallowed.HORSEMAN, HorsemanModel::createBodyLayer);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(ModContent.WISP.get(), WispRenderer::new);
            event.registerEntityRenderer(ModContent.SPIRIT.get(), SpiritRenderer::new);
            event.registerEntityRenderer(ModContent.HOLLOW_HORSEMAN.get(), HorsemanRenderer::new);
            event.registerEntityRenderer(ModContent.PUMPKIN_BOMB.get(), PumpkinBombRenderer::new);
        });
        modBus.addListener((RegisterParticleProvidersEvent event) -> {
            event.registerSpriteSet(ModContent.EMBER.get(), HallowedParticles.EmberProvider::new);
            event.registerSpriteSet(ModContent.AUTUMN_LEAF.get(), HallowedParticles.LeafProvider::new);
            event.registerSpriteSet(ModContent.WISP_FLAME.get(), HallowedParticles.WispFlameProvider::new);
            event.registerSpriteSet(ModContent.SOUL_MOTE.get(), HallowedParticles.SoulMoteProvider::new);
            event.registerSpriteSet(ModContent.CANDY_SPARKLE.get(), HallowedParticles.CandySparkleProvider::new);
        });
        // Forge draws the HUD through its overlay list rather than Gui#render, so the Hallowed layers join that list.
        modBus.addListener((RegisterGuiOverlaysEvent event) -> {
            event.registerBelowAll("night_under", (gui, graphics, partialTick, width, height) -> NightOverlay.renderUnder(graphics, partialTick));
            event.registerAboveAll("night_over", (gui, graphics, partialTick, width, height) -> NightOverlay.renderOver(graphics, partialTick));
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> HallowedItemModels.register(ItemProperties::register)));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) HallowedClient.tick();
        });
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> HallowedClient.reset());
        Hallowed.LOG.debug("Hallowed client set up");
    }

    static void handleNight(NightPayload payload) {
        HallowedClient.handleNight(payload);
    }

    static void handleFx(FxPayload payload) {
        HallowedClient.handleFx(payload);
    }
}
