package com.nightbeam.hallowed.fabric;

import com.nightbeam.hallowed.client.HallowedClient;
import com.nightbeam.hallowed.client.HallowedParticles;
import com.nightbeam.hallowed.client.ItemModels;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.Minecraft;
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
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public final class HallowedFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NightPayload.TYPE, (payload, context) -> HallowedClient.handleNight(payload));
        ClientPlayNetworking.registerGlobalReceiver(FxPayload.TYPE, (payload, context) -> HallowedClient.handleFx(payload));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> HallowedClient.reset());
        ClientTickEvents.END_CLIENT_TICK.register(client -> HallowedClient.tick());

        ItemModels.registerProperties();
        ModelLoadingPlugin.register(context -> context.addModels(ItemModels.WITCH_HAT_WORN));
        ItemModels.lookup = id -> Minecraft.getInstance().getModelManager().getModel(id);

        EntityModelLayerRegistry.registerModelLayer(ModelLayersHallowed.SPIRIT, SpiritModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ModelLayersHallowed.HORSEMAN, HorsemanModel::createBodyLayer);
        EntityRendererRegistry.register(ModContent.WISP.get(), WispRenderer::new);
        EntityRendererRegistry.register(ModContent.SPIRIT.get(), SpiritRenderer::new);
        EntityRendererRegistry.register(ModContent.HOLLOW_HORSEMAN.get(), HorsemanRenderer::new);
        EntityRendererRegistry.register(ModContent.PUMPKIN_BOMB.get(), PumpkinBombRenderer::new);

        ParticleFactoryRegistry particles = ParticleFactoryRegistry.getInstance();
        particles.register(ModContent.EMBER.get(), HallowedParticles.EmberProvider::new);
        particles.register(ModContent.AUTUMN_LEAF.get(), HallowedParticles.LeafProvider::new);
        particles.register(ModContent.WISP_FLAME.get(), HallowedParticles.WispFlameProvider::new);
        particles.register(ModContent.SOUL_MOTE.get(), HallowedParticles.SoulMoteProvider::new);
        particles.register(ModContent.CANDY_SPARKLE.get(), HallowedParticles.CandySparkleProvider::new);
    }
}
