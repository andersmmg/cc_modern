package com.andersmmg.cc_modern.runtime;

import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps a computer id to the internal peripherals of its server block, so the
 * "internal" hub can be built lazily from the Lua thread.
 */
public final class ServerComputerRegistry {
    private static final Map<Integer, ServerInternals> INTERNALS = new ConcurrentHashMap<>();

    private ServerComputerRegistry() {
    }

    public static void register(int computerId, IPeripheral drive, IPeripheral modem) {
        INTERNALS.put(computerId, new ServerInternals(drive, modem));
    }

    public static void unregister(int computerId) {
        INTERNALS.remove(computerId);
    }

    public static ServerInternals get(int computerId) {
        return INTERNALS.get(computerId);
    }

    public static void clearAll() {
        INTERNALS.clear();
    }

    public record ServerInternals(IPeripheral drive, IPeripheral modem) {
        public ServerInternals {
            if (drive == null || modem == null) {
                throw new IllegalArgumentException("Server internals cannot have null peripherals");
            }
        }

        public Map<String, IPeripheral> asMap() {
            Map<String, IPeripheral> map = new LinkedHashMap<>();
            map.put("drive", drive);
            map.put("modem", modem);
            return map;
        }
    }
}
