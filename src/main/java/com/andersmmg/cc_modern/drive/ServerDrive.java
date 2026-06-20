package com.andersmmg.cc_modern.drive;

import com.andersmmg.cc_modern.block.ServerBlockEntity;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import dan200.computercraft.api.filesystem.Mount;
import dan200.computercraft.api.filesystem.WritableMount;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.computer.ComputerSide;
import dan200.computercraft.core.util.StringUtil;
import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.network.client.PlayRecordClientMessage;
import dan200.computercraft.shared.network.server.ServerNetworking;
import dan200.computercraft.shared.platform.PlatformHelper;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ServerDrive implements Container {
    private static final String NBT_DISK_ITEM = "DiskItem";

    private final ServerBlockEntity owner;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);

    private final AtomicBoolean ejectQueued = new AtomicBoolean(false);
    private final AtomicBoolean stackDirty = new AtomicBoolean(false);
    private final AtomicReference<RecordCommand> recordQueued = new AtomicReference<>(null);
    @GuardedBy("this")
    private final Map<IComputerAccess, String> mounts = new HashMap<>();
    private final Peripheral peripheral = new Peripheral();
    @GuardedBy("this")
    private DiskMedia media = DiskMedia.EMPTY;
    private boolean attachedOnce = false;

    public ServerDrive(ServerBlockEntity owner) {
        this.owner = owner;
    }

    public void attachFirstTime(ServerComputer computer) {
        if (attachedOnce) return;
        computer.setPeripheral(ComputerSide.FRONT, peripheral);
        attachedOnce = true;
    }

    public IPeripheral peripheral() {
        return peripheral;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return inventory.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.get(0);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        var result = inventory.get(0).split(count);
        if (!result.isEmpty()) owner.markDirty();
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.set(0, stack);
        owner.markDirty();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(owner, player);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        var result = inventory.get(0);
        inventory.set(0, ItemStack.EMPTY);
        return result;
    }

    @Override
    public void clearContent() {
        inventory.clear();
        owner.markDirty();
    }

    @Override
    public void setChanged() {
        owner.markDirty();
    }

    public ItemStack getDiskStack() {
        return inventory.get(0);
    }

    public void setDiskStack(ItemStack stack) {
        inventory.set(0, stack);
        owner.markDirty();
    }

    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        var stack = inventory.get(0);
        if (!stack.isEmpty()) nbt.put(NBT_DISK_ITEM, stack.save(registries));
    }

    public void loadServer(CompoundTag nbt, HolderLookup.Provider registries) {
        if (nbt.contains(NBT_DISK_ITEM)) {
            inventory.set(0, ItemStack.parseOptional(registries, nbt.getCompound(NBT_DISK_ITEM)));
            synchronized (this) {
                media = DiskMedia.of(inventory.get(0));
                stackDirty.set(false);
            }
        }
    }

    // ------- Tick (drains cross-thread queues on the main thread) -------

    public void tick() {
        if (owner.getLevel() == null || owner.getLevel().isClientSide) return;

        if (stackDirty.getAndSet(false)) {
            inventory.set(0, media.stack().copy());
            owner.markDirtySkipSetChanged();
        }

        if (ejectQueued.getAndSet(false)) ejectContents();

        var incoming = recordQueued.getAndSet(null);
        if (incoming != null) {
            switch (incoming) {
                case PLAY -> playAudio();
                case STOP -> stopAudio();
            }
        }
    }

    public void ejectDisk() {
        ejectQueued.set(true);
    }

    public synchronized void updateMedia() {
        var newStack = inventory.get(0);
        if (ItemStack.isSameItemSameComponents(newStack, media.stack())) return;

        for (var entry : mounts.entrySet()) {
            unmountFromComputer(entry.getKey(), entry.getValue());
            entry.setValue(null);
        }
        media = DiskMedia.of(newStack);
        stackDirty.set(false);

        if (!newStack.isEmpty()) {
            for (var computer : mounts.keySet()) {
                mountToComputer(computer, true);
            }
        }
    }

    private void ejectContents() {
        var stack = inventory.get(0);
        if (stack.isEmpty()) return;
        inventory.set(0, ItemStack.EMPTY);
        WorldUtil.dropItemStack(owner.getLevel(), owner.getBlockPos(), owner.getDirection(), stack);
        owner.getLevel().levelEvent(LevelEvent.SOUND_DISPENSER_DISPENSE, owner.getBlockPos(), 0);
    }

    private void playAudio() {
        var audio = media.getAudio(owner.getLevel().registryAccess());
        if (audio != null) {
            sendToNearby(new PlayRecordClientMessage(owner.getBlockPos(), Optional.of(audio)));
        }
    }

    private void stopAudio() {
        sendToNearby(new PlayRecordClientMessage(owner.getBlockPos()));
    }

    private void sendToNearby(PlayRecordClientMessage message) {
        ServerNetworking.sendToAllAround(message, (ServerLevel) owner.getLevel(),
                Vec3.atCenterOf(owner.getBlockPos()), 64);
    }

    @GuardedBy("this")
    private void mountToComputer(IComputerAccess computer, boolean immediate) {
        if (media.media() == null) return;
        var mount = getOrCreateMount(immediate);
        if (mount == null) return;

        String path = null;
        if (mount instanceof WritableMount writable) {
            for (int n = 1; path == null && n <= 100; n++) {
                path = computer.mountWritable(n == 1 ? "disk" : "disk" + n, writable);
            }
        } else {
            for (int n = 1; path == null && n <= 100; n++) {
                path = computer.mount(n == 1 ? "disk" : "disk" + n, mount);
            }
        }

        if (path == null) return;
        mounts.put(computer, path);
        computer.queueEvent("disk", computer.getAttachmentName());
    }

    @GuardedBy("this")
    private @Nullable Mount getOrCreateMount(boolean immediate) {
        if (media.media() == null) return null;
        if (!(owner.getLevel() instanceof ServerLevel serverLevel)) return null;

        var stack = media.stack().copy();
        Mount mount = media.media().createDataMount(stack, serverLevel);
        if (mount == null) return null;

        if (!ItemStack.isSameItemSameComponents(stack, media.stack())) {
            updateMediaSlot(stack, immediate);
        }
        return mount;
    }

    @GuardedBy("this")
    private void updateMediaSlot(ItemStack stack, boolean immediate) {
        media = media.withStack(stack);
        if (immediate) {
            inventory.set(0, media.stack().copy());
            owner.markDirtySkipSetChanged();
        } else {
            stackDirty.set(true);
        }
    }

    @GuardedBy("this")
    private void unmountFromComputer(IComputerAccess computer, @Nullable String path) {
        if (path != null) computer.unmount(path);
        computer.queueEvent("disk_eject", computer.getAttachmentName());
    }

    private enum RecordCommand {
        PLAY, STOP
    }

    public record DiskMedia(ItemStack stack, @Nullable IMedia media) {
        public static final DiskMedia EMPTY = new DiskMedia(ItemStack.EMPTY, null);

        public static DiskMedia of(ItemStack stack) {
            if (stack.isEmpty()) return EMPTY;
            return new DiskMedia(stack.copy(), PlatformHelper.get().getMedia(stack));
        }

        public @Nullable Holder<JukeboxSong> getAudio(HolderLookup.Provider access) {
            return media != null ? media.getAudio(access, stack) : null;
        }

        public DiskMedia withStack(ItemStack newStack) {
            return new DiskMedia(newStack, media);
        }
    }

    public class Peripheral implements IPeripheral {
        @Override
        public String getType() {
            return "drive";
        }

        @LuaFunction
        public final boolean isDiskPresent() {
            synchronized (ServerDrive.this) {
                return !media.stack().isEmpty();
            }
        }

        @LuaFunction
        public final @Nullable Object[] getDiskLabel() {
            synchronized (ServerDrive.this) {
                if (media.media() == null) return null;
                var label = media.media().getLabel(owner.getLevel().registryAccess(), media.stack());
                return label == null ? null : new Object[]{label};
            }
        }

        @LuaFunction(mainThread = true)
        public final void setDiskLabel(Optional<String> label) throws LuaException {
            synchronized (ServerDrive.this) {
                if (media.media() == null) return;
                var stack = media.stack().copy();
                if (!media.media().setLabel(stack, label.map(StringUtil::normaliseLabel).orElse(null))) {
                    throw new LuaException("Disk label cannot be changed");
                }
                updateMediaSlot(stack, true);
            }
        }

        @LuaFunction
        public final boolean hasData(IComputerAccess computer) {
            synchronized (ServerDrive.this) {
                return mounts.get(computer) != null;
            }
        }

        @LuaFunction
        public final @Nullable String getMountPath(IComputerAccess computer) {
            synchronized (ServerDrive.this) {
                return mounts.get(computer);
            }
        }

        @LuaFunction
        public final boolean hasAudio() {
            synchronized (ServerDrive.this) {
                return media.getAudio(owner.getLevel().registryAccess()) != null;
            }
        }

        @LuaFunction
        public final @Nullable Object getAudioTitle() {
            synchronized (ServerDrive.this) {
                if (media.media() == null) return null;
                var audio = media.getAudio(owner.getLevel().registryAccess());
                return audio == null ? null : audio.value().description().getString();
            }
        }

        @LuaFunction
        public final void playAudio() {
            recordQueued.set(RecordCommand.PLAY);
        }

        @LuaFunction
        public final void stopAudio() {
            recordQueued.set(RecordCommand.STOP);
        }

        @LuaFunction
        public final void ejectDisk() {
            ejectQueued.set(true);
        }

        @LuaFunction
        public final @Nullable Object[] getDiskID() {
            synchronized (ServerDrive.this) {
                var id = media.stack().get(ModRegistry.DataComponents.DISK_ID.get());
                return id != null ? new Object[]{id.id()} : null;
            }
        }

        @Override
        public void attach(IComputerAccess computer) {
            synchronized (ServerDrive.this) {
                mounts.put(computer, null);
                if (!media.stack().isEmpty()) {
                    // Computer thread: defer inventory mutations to main thread.
                    mountToComputer(computer, false);
                }
            }
        }

        @Override
        public void detach(IComputerAccess computer) {
            synchronized (ServerDrive.this) {
                var path = mounts.remove(computer);
                unmountFromComputer(computer, path);
            }
        }

        @Override
        public boolean equals(IPeripheral other) {
            return this == other;
        }

        @Override
        public Object getTarget() {
            return owner;
        }
    }
}
