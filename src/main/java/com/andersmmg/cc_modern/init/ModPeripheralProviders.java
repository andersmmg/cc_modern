package com.andersmmg.cc_modern.init;

import com.andersmmg.cc_modern.peripheral.WallMonitorPeripheral;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import dan200.computercraft.shared.peripheral.monitor.MonitorPeripheral;
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
        event.registerBlockEntity(
                PeripheralCapability.get(),
                ModBlockEntities.SERVER_BE.get(),
                (be, side) -> be.peripheral()
        );
        event.registerBlockEntity(
                PeripheralCapability.get(),
                ModBlockEntities.SERVER_ADVANCED_BE.get(),
                (be, side) -> be.peripheral()
        );
        event.registerBlockEntity(
                PeripheralCapability.get(),
                ModBlockEntities.ANGLED_MONITOR_BE.get(),
                (be, side) -> {
                    be.peripheral();
                    return new MonitorPeripheral(be);
                }
        );
        event.registerBlockEntity(
                PeripheralCapability.get(),
                ModBlockEntities.ANGLED_MONITOR_ADVANCED_BE.get(),
                (be, side) -> {
                    be.peripheral();
                    return new MonitorPeripheral(be);
                }
        );
    }
}
