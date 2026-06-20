package com.andersmmg.cc_modern.peripheral;

import com.andersmmg.cc_modern.block.ServerBlockEntity;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.peripheral.modem.ModemState;
import dan200.computercraft.shared.peripheral.modem.wireless.WirelessModemPeripheral;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ServerModemPeripheral extends WirelessModemPeripheral {
    private final ServerBlockEntity owner;

    public ServerModemPeripheral(ServerBlockEntity owner) {
        super(new ModemState(), owner.getFamily() == ComputerFamily.ADVANCED);
        this.owner = owner;
    }

    @Override
    public Level getLevel() {
        return owner.getLevel();
    }

    @Override
    public Vec3 getPosition() {
        return Vec3.atCenterOf(owner.getBlockPos());
    }

    @Override
    public boolean equals(IPeripheral other) {
        return this == other;
    }
}
