package com.andersmmg.cc_modern.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.computer.blocks.AbstractComputerBlockEntity;
import dan200.computercraft.shared.computer.blocks.ComputerPeripheral;

import java.util.Set;

public class ServerPeripheral extends ComputerPeripheral {
    private static final Set<String> ADDITIONAL_TYPES = Set.of("server");

    public ServerPeripheral(AbstractComputerBlockEntity owner) {
        super("computer", owner);
    }

    @Override
    public Set<String> getAdditionalTypes() {
        return ADDITIONAL_TYPES;
    }

    @Override
    public boolean equals(IPeripheral other) {
        return other instanceof ServerPeripheral && super.equals(other);
    }
}
