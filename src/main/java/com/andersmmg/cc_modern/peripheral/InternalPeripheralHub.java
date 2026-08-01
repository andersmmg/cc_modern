package com.andersmmg.cc_modern.peripheral;

import dan200.computercraft.api.filesystem.Mount;
import dan200.computercraft.api.filesystem.WritableMount;
import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.NotAttachedException;
import dan200.computercraft.api.peripheral.WorkMonitor;
import dan200.computercraft.core.computer.GuardedLuaContext;
import dan200.computercraft.core.methods.MethodSupplier;
import dan200.computercraft.core.methods.PeripheralMethod;
import dan200.computercraft.core.util.LuaUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The virtual "internal" peripheral of a server block. Exposes the block's
 * internal peripherals (disk drive, wireless modem) as remote-style names via
 * the standard CC:Tweaked {@code peripheral_hub} methods, without consuming a
 * directional slot.
 */
public final class InternalPeripheralHub implements IPeripheral {
    public static final String NAME = "internal";
    public static final String TYPE = "server_internals";

    private final Map<String, IPeripheral> internals;
    private final MethodSupplier peripheralMethods;
    private final Map<IComputerAccess, ConcurrentMap<String, RemoteWrapper>> remoteWrappers = new HashMap<>();

    public InternalPeripheralHub(Map<String, IPeripheral> internals, MethodSupplier peripheralMethods) {
        this.internals = Collections.unmodifiableMap(new LinkedHashMap<>(internals));
        this.peripheralMethods = peripheralMethods;
    }

    private static void detachAll(ConcurrentMap<String, RemoteWrapper> wrappers) {
        while (!wrappers.isEmpty()) {
            for (Map.Entry<String, RemoteWrapper> entry : new LinkedHashMap<>(wrappers).entrySet()) {
                if (wrappers.remove(entry.getKey(), entry.getValue())) entry.getValue().detach();
            }
        }
    }

    private static Map<Integer, String> asLuaList(Collection<String> values) {
        Map<Integer, String> result = new LinkedHashMap<>();
        int index = 1;
        for (String value : values) result.put(index++, value);
        return result;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Set<String> getAdditionalTypes() {
        return Set.of("peripheral_hub");
    }

    @Override
    public Object getTarget() {
        return this;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return this == other;
    }

    @Override
    public void attach(IComputerAccess computer) {
        synchronized (remoteWrappers) {
            remoteWrappers.computeIfAbsent(computer, ignored -> new ConcurrentHashMap<>());
        }
        syncWrappers(computer);
    }

    @Override
    public void detach(IComputerAccess computer) {
        ConcurrentMap<String, RemoteWrapper> wrappers;
        synchronized (remoteWrappers) {
            wrappers = remoteWrappers.remove(computer);
        }
        if (wrappers != null) detachAll(wrappers);
    }

    @LuaFunction
    public final Map<Integer, String> getNamesRemote(IComputerAccess computer) {
        return asLuaList(syncWrappers(computer).keySet());
    }

    @LuaFunction
    public final boolean isPresentRemote(IComputerAccess computer, String name) {
        return getWrapper(computer, name) != null;
    }

    @LuaFunction
    public final Object @Nullable [] getTypeRemote(IComputerAccess computer, String name) {
        RemoteWrapper wrapper = getWrapper(computer, name);
        return wrapper == null ? null : LuaUtil.consArray(wrapper.getType(), wrapper.getAdditionalTypes());
    }

    @LuaFunction
    public final Object @Nullable [] hasTypeRemote(IComputerAccess computer, String name, String type) {
        RemoteWrapper wrapper = getWrapper(computer, name);
        return wrapper == null ? null : new Object[]{wrapper.hasType(type)};
    }

    @LuaFunction
    public final Object @Nullable [] getMethodsRemote(IComputerAccess computer, String name) {
        RemoteWrapper wrapper = getWrapper(computer, name);
        return wrapper == null ? null : new Object[]{asLuaList(wrapper.getMethodNames())};
    }

    @LuaFunction
    public final MethodResult callRemote(IComputerAccess computer, ILuaContext context, IArguments arguments)
            throws LuaException {
        String remoteName = arguments.getString(0);
        String methodName = arguments.getString(1);
        RemoteWrapper wrapper = getWrapper(computer, remoteName);
        if (wrapper == null) throw new LuaException("No peripheral: " + remoteName);
        return wrapper.callMethod(context, methodName, arguments.drop(2));
    }

    private ConcurrentMap<String, RemoteWrapper> syncWrappers(IComputerAccess computer) {
        ConcurrentMap<String, RemoteWrapper> wrappers;
        synchronized (remoteWrappers) {
            wrappers = remoteWrappers.computeIfAbsent(computer, ignored -> new ConcurrentHashMap<>());
        }
        return syncWrappers(computer, wrappers);
    }

    private ConcurrentMap<String, RemoteWrapper> syncWrappers(
            IComputerAccess computer,
            ConcurrentMap<String, RemoteWrapper> wrappers
    ) {
        for (Map.Entry<String, RemoteWrapper> entry : new LinkedHashMap<>(wrappers).entrySet()) {
            IPeripheral peripheral = internals.get(entry.getKey());
            if (peripheral == null || !entry.getValue().isFor(peripheral)) {
                RemoteWrapper removed = wrappers.remove(entry.getKey());
                if (removed != null) removed.detach();
            }
        }

        for (Map.Entry<String, IPeripheral> entry : internals.entrySet()) {
            wrappers.computeIfAbsent(entry.getKey(), name -> {
                RemoteWrapper wrapper = new RemoteWrapper(
                        entry.getValue(),
                        computer,
                        name,
                        peripheralMethods.getSelfMethods(entry.getValue())
                );
                wrapper.attach();
                return wrapper;
            });
        }

        boolean stillAttached;
        synchronized (remoteWrappers) {
            stillAttached = remoteWrappers.get(computer) == wrappers;
        }
        if (!stillAttached) detachAll(wrappers);
        return wrappers;
    }

    private @Nullable RemoteWrapper getWrapper(IComputerAccess computer, String name) {
        return syncWrappers(computer).get(name);
    }

    private final class RemoteWrapper implements IComputerAccess, GuardedLuaContext.Guard {
        private final IPeripheral peripheral;
        private final IComputerAccess computer;
        private final String name;
        private final String type;
        private final Set<String> additionalTypes;
        private final Map<String, PeripheralMethod> methodMap;
        private final Set<String> mounts = ConcurrentHashMap.newKeySet();
        private volatile boolean attached;
        private @Nullable GuardedLuaContext contextWrapper;

        private RemoteWrapper(IPeripheral peripheral, IComputerAccess computer, String name,
                              Map<String, PeripheralMethod> methodMap) {
            this.peripheral = peripheral;
            this.computer = computer;
            this.name = name;
            this.type = peripheral.getType();
            Set<String> types = peripheral.getAdditionalTypes();
            this.additionalTypes = types == null || types.isEmpty() ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(types));
            this.methodMap = Collections.unmodifiableMap(new LinkedHashMap<>(methodMap));
        }

        private boolean isFor(IPeripheral other) {
            return peripheral == other || peripheral.equals(other) || other.equals(peripheral);
        }

        private void attach() {
            attached = true;
            peripheral.attach(this);
            computer.queueEvent("peripheral", name);
        }

        private void detach() {
            if (!attached) return;
            peripheral.detach(this);
            computer.queueEvent("peripheral_detach", name);
            attached = false;
            synchronized (this) {
                for (String mount : mounts) computer.unmount(mount);
                mounts.clear();
            }
        }

        private String getType() {
            return type;
        }

        private Set<String> getAdditionalTypes() {
            return additionalTypes;
        }

        private boolean hasType(String type) {
            return this.type.equals(type) || additionalTypes.contains(type);
        }

        private Collection<String> getMethodNames() {
            return methodMap.keySet();
        }

        private MethodResult callMethod(ILuaContext context, String methodName, IArguments arguments) throws LuaException {
            PeripheralMethod method = methodMap.get(methodName);
            if (method == null) throw new LuaException("No such method " + methodName);
            GuardedLuaContext wrapper = contextWrapper;
            if (wrapper == null || !wrapper.wraps(context)) {
                wrapper = contextWrapper = new GuardedLuaContext(context, this);
            }
            return method.apply(peripheral, wrapper, this, arguments);
        }

        private void requireAttached() {
            if (!attached) throw new NotAttachedException();
        }

        @Override
        public boolean checkValid() {
            return attached;
        }

        @Override
        public synchronized @Nullable String mount(String desiredLocation, Mount mount) {
            requireAttached();
            String mounted = computer.mount(desiredLocation, mount, name);
            if (mounted != null) mounts.add(mounted);
            return mounted;
        }

        @Override
        public synchronized @Nullable String mount(String desiredLocation, Mount mount, String driveName) {
            requireAttached();
            String mounted = computer.mount(desiredLocation, mount, driveName);
            if (mounted != null) mounts.add(mounted);
            return mounted;
        }

        @Override
        public synchronized @Nullable String mountWritable(String desiredLocation, WritableMount mount) {
            requireAttached();
            String mounted = computer.mountWritable(desiredLocation, mount, name);
            if (mounted != null) mounts.add(mounted);
            return mounted;
        }

        @Override
        public synchronized @Nullable String mountWritable(String desiredLocation, WritableMount mount, String driveName) {
            requireAttached();
            String mounted = computer.mountWritable(desiredLocation, mount, driveName);
            if (mounted != null) mounts.add(mounted);
            return mounted;
        }

        @Override
        public synchronized void unmount(@Nullable String location) {
            requireAttached();
            computer.unmount(location);
            if (location != null) mounts.remove(location);
        }

        @Override
        public int getID() {
            requireAttached();
            return computer.getID();
        }

        @Override
        public void queueEvent(String event, @Nullable Object... arguments) {
            requireAttached();
            computer.queueEvent(event, arguments);
        }

        @Override
        public String getAttachmentName() {
            requireAttached();
            return name;
        }

        @Override
        public Map<String, IPeripheral> getAvailablePeripherals() {
            requireAttached();
            return Collections.unmodifiableMap(new LinkedHashMap<>(internals));
        }

        @Override
        public @Nullable IPeripheral getAvailablePeripheral(String name) {
            requireAttached();
            return internals.get(name);
        }

        @Override
        public WorkMonitor getMainThreadMonitor() {
            requireAttached();
            return computer.getMainThreadMonitor();
        }
    }
}
