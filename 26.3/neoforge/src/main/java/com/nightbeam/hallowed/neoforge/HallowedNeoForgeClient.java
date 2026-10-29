package com.nightbeam.hallowed.neoforge;

import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.client.HallowedClient;
import com.nightbeam.hallowed.client.HallowedParticles;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Hallowed.MOD_ID, dist = Dist.CLIENT)
public final class HallowedNeoForgeClient {
    public HallowedNeoForgeClient(IEventBus modBus) {
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> {
            event.register(NightPayload.TYPE, (payload, context) -> HallowedClient.handleNight(payload));
            event.register(FxPayload.TYPE, (payload, context) -> HallowedClient.handleFx(payload));
        });
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
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> HallowedClient.tick());
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> HallowedClient.reset());
    }
}
