package com.andersmmg.cc_modern;

import com.andersmmg.cc_modern.client.render.WallMonitorHighlightRenderer;
import com.andersmmg.cc_modern.init.ModBlockEntities;
import com.andersmmg.cc_modern.client.render.WallMonitorBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

@EventBusSubscriber(modid = CCModern.MODID, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(ModBlockEntities.WALL_MONITOR_BE.get(), WallMonitorBlockEntityRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.WALL_MONITOR_ADVANCED_BE.get(), WallMonitorBlockEntityRenderer::new);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDrawHighlight(RenderHighlightEvent.Block event) {
        if (WallMonitorHighlightRenderer.drawHighlight(event.getPoseStack(), event.getMultiBufferSource(), event.getCamera(), event.getTarget())) {
            event.setCanceled(true);
        }
    }
}
