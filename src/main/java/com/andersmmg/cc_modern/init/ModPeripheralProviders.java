package com.andersmmg.cc_modern.init;

import com.andersmmg.cc_modern.block.WallMonitorBlockEntity;
import com.andersmmg.cc_modern.peripheral.WallMonitorPeripheral;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ModPeripheralProviders {
    private ModPeripheralProviders() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.register(ModPeripheralProviders.class);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            PeripheralCapability.get(),
            ModBlockEntities.WALL_MONITOR_BE.get(),
            (be, side) -> {
                be.peripheral();
                return new WallMonitorPeripheral(be);
            }
        );
        event.registerBlockEntity(
            PeripheralCapability.get(),
            ModBlockEntities.WALL_MONITOR_ADVANCED_BE.get(),
            (be, side) -> {
                be.peripheral();
                return new WallMonitorPeripheral(be);
            }
        );
    }
}
