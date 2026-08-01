package com.andersmmg.cc_modern.mixin;

import com.andersmmg.cc_modern.peripheral.InternalPeripheralHub;
import com.andersmmg.cc_modern.runtime.ServerComputerRegistry;
import com.andersmmg.cc_modern.util.PeripheralAccessWrapper;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.PeripheralAPI;
import dan200.computercraft.core.methods.MethodSupplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects the server block's virtual "internal" peripheral into the Lua
 * {@code peripheral} API, so its internal devices don't consume a directional
 * slot.
 */
@Mixin(value = PeripheralAPI.class, remap = false)
public abstract class MixinPeripheralAPI {
    @Shadow
    @Final
    private IAPIEnvironment environment;

    @Shadow
    @Final
    private MethodSupplier peripheralMethods;

    @Unique
    private PeripheralAccessWrapper cc_modern$wrapper;
    @Unique
    private InternalPeripheralHub cc_modern$hub;

    @Inject(method = "startup", at = @At("RETURN"))
    private void cc_modern$startup(CallbackInfo ci) {
        cc_modern$getHubWrapper();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void cc_modern$shutdown(CallbackInfo ci) {
        cc_modern$detachHub();
    }

    @Inject(method = "isPresent", at = @At("HEAD"), cancellable = true)
    private void cc_modern$isPresent(String name, CallbackInfoReturnable<Boolean> cir) {
        if (InternalPeripheralHub.NAME.equals(name) && cc_modern$getHubWrapper() != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getType", at = @At("HEAD"), cancellable = true)
    private void cc_modern$getType(String name, CallbackInfoReturnable<Object[]> cir) {
        PeripheralAccessWrapper wrapper = InternalPeripheralHub.NAME.equals(name) ? cc_modern$getHubWrapper() : null;
        if (wrapper != null) cir.setReturnValue(wrapper.typesArray());
    }

    @Inject(method = "hasType", at = @At("HEAD"), cancellable = true)
    private void cc_modern$hasType(String name, String type, CallbackInfoReturnable<Object[]> cir) {
        PeripheralAccessWrapper wrapper = InternalPeripheralHub.NAME.equals(name) ? cc_modern$getHubWrapper() : null;
        if (wrapper != null) cir.setReturnValue(new Object[]{wrapper.hasType(type)});
    }

    @Inject(method = "getMethods", at = @At("HEAD"), cancellable = true)
    private void cc_modern$getMethods(String name, CallbackInfoReturnable<Object[]> cir) {
        PeripheralAccessWrapper wrapper = InternalPeripheralHub.NAME.equals(name) ? cc_modern$getHubWrapper() : null;
        if (wrapper != null) cir.setReturnValue(new Object[]{wrapper.getMethods()});
    }

    @Inject(method = "call", at = @At("HEAD"), cancellable = true)
    private void cc_modern$call(ILuaContext context, IArguments args, CallbackInfoReturnable<MethodResult> cir)
            throws LuaException {
        String name = args.getString(0);
        if (!InternalPeripheralHub.NAME.equals(name)) return;
        PeripheralAccessWrapper wrapper = cc_modern$getHubWrapper();
        if (wrapper == null) return;
        String methodName = args.getString(1);
        cir.setReturnValue(wrapper.call(context, methodName, args.drop(2)).adjustError(1));
    }

    @Unique
    private PeripheralAccessWrapper cc_modern$getHubWrapper() {
        var internals = ServerComputerRegistry.get(environment.getComputerID());
        if (internals == null) {
            cc_modern$detachHub();
            return null;
        }

        if (cc_modern$hub == null) {
            cc_modern$hub = new InternalPeripheralHub(internals.asMap(), peripheralMethods);
        }

        if (cc_modern$wrapper != null && cc_modern$wrapper.isFor(cc_modern$hub)) return cc_modern$wrapper;
        if (cc_modern$wrapper != null) cc_modern$wrapper.detach();

        cc_modern$wrapper = new PeripheralAccessWrapper(
                environment,
                peripheralMethods,
                cc_modern$hub,
                InternalPeripheralHub.NAME
        );
        cc_modern$wrapper.attach();
        return cc_modern$wrapper;
    }

    @Unique
    private void cc_modern$detachHub() {
        if (cc_modern$wrapper != null) {
            cc_modern$wrapper.detach();
            cc_modern$wrapper = null;
        }
        cc_modern$hub = null;
    }
}
