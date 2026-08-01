package com.andersmmg.cc_modern.util;

import dan200.computercraft.api.filesystem.Mount;
import dan200.computercraft.api.filesystem.WritableMount;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.NotAttachedException;
import dan200.computercraft.api.peripheral.WorkMonitor;
import dan200.computercraft.core.apis.ComputerAccess;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.computer.GuardedLuaContext;
import dan200.computercraft.core.methods.MethodSupplier;
import dan200.computercraft.core.methods.PeripheralMethod;
import dan200.computercraft.core.metrics.Metrics;
import dan200.computercraft.core.util.LuaUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Attaches a virtual (side-less) peripheral to a computer's Lua environment.
 * Backed by a real {@link ComputerAccess}, so mounts and events behave like a
 * normal side peripheral.
 */
public final class PeripheralAccessWrapper extends ComputerAccess implements GuardedLuaContext.Guard {
    private final IAPIEnvironment environment;
    private final IPeripheral peripheral;
    private final String name;
    private final String type;
    private final Set<String> additionalTypes;
    private final Map<String, PeripheralMethod> methodMap;
    private boolean attached = false;
    private @Nullable GuardedLuaContext contextWrapper;

    public PeripheralAccessWrapper(
            IAPIEnvironment environment,
            MethodSupplier peripheralMethods,
            IPeripheral peripheral,
            String name
    ) {
        super(environment);
        this.environment = environment;
        this.peripheral = peripheral;
        this.name = name;
        type = Objects.requireNonNull(peripheral.getType(), "Peripheral type cannot be null");
        additionalTypes = peripheral.getAdditionalTypes();
        methodMap = peripheralMethods.getSelfMethods(peripheral);
    }

    public boolean isFor(IPeripheral other) {
        return peripheral == other || peripheral.equals(other) || other.equals(peripheral);
    }

    public synchronized void attach() {
        if (attached) return;
        attached = true;
        peripheral.attach(this);
        environment.queueEvent("peripheral", name);
    }

    public synchronized void detach() {
        if (!attached) return;
        peripheral.detach(this);
        synchronized (this) {
            unmountAll();
        }
        attached = false;
        environment.queueEvent("peripheral_detach", name);
    }

    private synchronized boolean isAttached() {
        return attached;
    }

    public Object[] typesArray() {
        return LuaUtil.consArray(type, additionalTypes);
    }

    public boolean hasType(String type) {
        return this.type.equals(type) || additionalTypes.contains(type);
    }

    public Collection<String> getMethods() {
        return methodMap.keySet();
    }

    public MethodResult call(ILuaContext context, String methodName, IArguments arguments) throws LuaException {
        PeripheralMethod method;
        synchronized (this) {
            method = methodMap.get(methodName);
        }
        if (method == null) throw new LuaException("No such method " + methodName);

        GuardedLuaContext contextWrapper = this.contextWrapper;
        if (contextWrapper == null || !contextWrapper.wraps(context)) {
            contextWrapper = this.contextWrapper = new GuardedLuaContext(context, this);
        }
        try (var ignored = environment.time(Metrics.PERIPHERAL_OPS)) {
            return method.apply(peripheral, contextWrapper, this, arguments);
        }
    }

    @Override
    public boolean checkValid() {
        return isAttached();
    }

    @Nullable
    @Override
    public synchronized String mount(String desiredLoc, Mount mount, String driveName) {
        if (!attached) throw new NotAttachedException();
        return super.mount(desiredLoc, mount, driveName);
    }

    @Nullable
    @Override
    public synchronized String mountWritable(String desiredLoc, WritableMount mount, String driveName) {
        if (!attached) throw new NotAttachedException();
        return super.mountWritable(desiredLoc, mount, driveName);
    }

    @Override
    public synchronized void unmount(@Nullable String location) {
        if (!attached) throw new NotAttachedException();
        super.unmount(location);
    }

    @Override
    public int getID() {
        if (!attached) throw new NotAttachedException();
        return super.getID();
    }

    @Override
    public void queueEvent(String event, @Nullable Object... arguments) {
        if (!attached) throw new NotAttachedException();
        super.queueEvent(event, arguments);
    }

    @Override
    public String getAttachmentName() {
        if (!attached) throw new NotAttachedException();
        return name;
    }

    @Override
    public Map<String, IPeripheral> getAvailablePeripherals() {
        if (!attached) throw new NotAttachedException();
        return Collections.unmodifiableMap(Map.of(name, peripheral));
    }

    @Nullable
    @Override
    public IPeripheral getAvailablePeripheral(String name) {
        if (!attached) throw new NotAttachedException();
        return this.name.equals(name) ? peripheral : null;
    }

    @Override
    public WorkMonitor getMainThreadMonitor() {
        if (!attached) throw new NotAttachedException();
        return super.getMainThreadMonitor();
    }
}
