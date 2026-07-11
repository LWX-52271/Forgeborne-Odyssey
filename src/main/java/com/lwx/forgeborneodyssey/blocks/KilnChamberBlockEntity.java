package com.lwx.forgeborneodyssey.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.lwx.forgeborneodyssey.menu.KilnMenu;

import javax.annotation.Nullable;

public class KilnChamberBlockEntity extends BlockEntity implements MenuProvider {
    private int processTime;
    private int totalProcessTime = 400;
    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private boolean isProcessing = false;

    public KilnChamberBlockEntity(BlockPos pos, BlockState state) {
        super(null, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.forgeborneodyssey.kiln_chamber");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new KilnMenu(id, inventory, this);
    }

    public void tick() {
        if (this.level == null) return;

        boolean lit = this.getBlockState().getValue(BlockStateProperties.LIT);
        boolean needsUpdate = false;

        if (isProcessing && !this.input.isEmpty()) {
            this.processTime++;
            if (this.processTime >= this.totalProcessTime) {
                this.processTime = 0;
                processItem();
                needsUpdate = true;
            }
        } else {
            this.processTime = 0;
        }

        if (needsUpdate) {
            this.setChanged();
        }
    }

    private void processItem() {
        if (this.output.isEmpty()) {
            this.output = this.input.copy();
            this.output.setCount(1);
        } else {
            this.output.grow(1);
        }
        this.input.shrink(1);
        this.isProcessing = false;
        this.level.setBlock(this.worldPosition, this.getBlockState().setValue(BlockStateProperties.LIT, false), 3);
    }

    public void interact(Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (this.input.isEmpty() && !heldItem.isEmpty()) {
            this.input = heldItem.split(1);
            this.isProcessing = true;
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(BlockStateProperties.LIT, true), 3);
            this.setChanged();
        } else if (!this.output.isEmpty()) {
            player.addItem(this.output);
            this.output = ItemStack.EMPTY;
            this.setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("ProcessTime", this.processTime);
        tag.putInt("TotalProcessTime", this.totalProcessTime);
        tag.putBoolean("IsProcessing", this.isProcessing);
        tag.put("Input", this.input.save(new CompoundTag()));
        tag.put("Output", this.output.save(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.processTime = tag.getInt("ProcessTime");
        this.totalProcessTime = tag.getInt("TotalProcessTime");
        this.isProcessing = tag.getBoolean("IsProcessing");
        this.input = ItemStack.of(tag.getCompound("Input"));
        this.output = ItemStack.of(tag.getCompound("Output"));
    }

    public void dropContents() {
        if (this.level == null) return;
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.input);
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.output);
    }

    public ItemStack getInput() { return input; }
    public ItemStack getOutput() { return output; }
    public void setInput(ItemStack stack) { this.input = stack; }
    public void setOutput(ItemStack stack) { this.output = stack; }
    public int getProcessTime() { return processTime; }
    public int getTotalProcessTime() { return totalProcessTime; }
    public boolean isProcessing() { return isProcessing; }
}