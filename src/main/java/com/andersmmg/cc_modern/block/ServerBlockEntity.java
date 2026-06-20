package com.andersmmg.cc_modern.block;

import com.andersmmg.cc_modern.drive.ServerDrive;
import com.andersmmg.cc_modern.peripheral.ServerModemPeripheral;
import com.andersmmg.cc_modern.peripheral.ServerPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.computer.ComputerSide;
import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.computer.blocks.AbstractComputerBlockEntity;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ComputerState;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.inventory.ComputerMenuWithoutInventory;
import dan200.computercraft.shared.peripheral.diskdrive.DiskDriveMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ServerBlockEntity extends AbstractComputerBlockEntity implements Container {
    private final ServerDrive drive = new ServerDrive(this);
    private final ServerModemPeripheral modem = new ServerModemPeripheral(this);

    @Nullable
    private IPeripheral peripheral;
    private @Nullable ServerComputer modemAttachedTo = null;

    public ServerBlockEntity(BlockEntityType<? extends ServerBlockEntity> type, BlockPos pos, BlockState state, ComputerFamily family) {
        super(type, pos, state, family);
    }

    @Override
    protected ServerComputer createComputer(int id) {
        return new ServerComputer((ServerLevel) getLevel(), getBlockPos(), ServerComputer.properties(id, getFamily())
                .label(getLabel())
                .storageCapacity(storageCapacity)
        );
    }

    /**
     * FACING is the chassis front; chassis convention so {@code peripheral.wrap("front"/"back")} hits {@code FACING}/{@code -FACING}.
     */
    @Override
    public Direction getDirection() {
        return getBlockState().getValue(ServerBlock.FACING);
    }

    /** Mirror LEFT/RIGHT vs the chassis-frame default so {@code peripheral.wrap("left"/"right")} is correct. */
    @Override
    protected ComputerSide remapLocalSide(ComputerSide localSide) {
        return switch (localSide) {
            case LEFT -> ComputerSide.RIGHT;
            case RIGHT -> ComputerSide.LEFT;
            default -> localSide;
        };
    }

    /** FRONT hosts the internal disk drive, BOTTOM the wireless modem; the rest are open. */
    @Override
    protected boolean isPeripheralBlockedOnSide(ComputerSide localSide) {
        return localSide == ComputerSide.FRONT || localSide == ComputerSide.BOTTOM;
    }

    @Override
    protected void updateBlockState(ComputerState newState) {
        var existing = getBlockState();
        if (existing.getValue(ServerBlock.STATE) != newState) {
            getLevel().setBlock(getBlockPos(), existing.setValue(ServerBlock.STATE, newState), ServerBlock.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void serverTick() {
        super.serverTick();
        if (getLevel().isClientSide) return;

        var computer = getServerComputer();

        if (computer != null) {
            drive.attachFirstTime(computer);
            attachModemIfNeeded(computer);
        }

        drive.tick();

        if (computer != null) updateBlockState(computer.getState());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ComputerMenuWithoutInventory(ModRegistry.Menus.COMPUTER.get(), id, inventory,
                this::isUsableByPlayer, createServerComputer());
    }

    protected boolean isUsableByPlayer(Player player) {
        return isUsable(player);
    }

    public AbstractContainerMenu createDiskDriveMenu(int id, Inventory inventory, Player player) {
        return new DiskDriveMenu(id, inventory, drive);
    }

    @Override
    public int getContainerSize() {
        return drive.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return drive.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return drive.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return drive.removeItem(slot, count);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        drive.setItem(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return drive.removeItemNoUpdate(slot);
    }

    @Override
    public void clearContent() {
        drive.clearContent();
    }

    @Override
    public void setChanged() {
        if (level != null && !level.isClientSide) drive.updateMedia();
        super.setChanged();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        modem.removed();
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        drive.saveAdditional(nbt, registries);
    }

    @Override
    protected void loadServer(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadServer(nbt, registries);
        drive.loadServer(nbt, registries);
    }

    public void markDirty() {
        setChanged();
    }

    public void markDirtySkipSetChanged() {
        if (level != null) level.blockEntityChanged(worldPosition);
    }

    private void attachModemIfNeeded(ServerComputer computer) {
        if (modemAttachedTo == computer) return;
        computer.setPeripheral(ComputerSide.BOTTOM, modem);
        modemAttachedTo = computer;
    }

    public IPeripheral peripheral() {
        if (peripheral != null) return peripheral;
        return peripheral = new ServerPeripheral(this);
    }
}
