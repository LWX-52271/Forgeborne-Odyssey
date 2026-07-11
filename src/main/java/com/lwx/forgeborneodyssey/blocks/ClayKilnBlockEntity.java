package com.lwx.forgeborneodyssey.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.lwx.forgeborneodyssey.menu.KilnMenu;

import javax.annotation.Nullable;

public class ClayKilnBlockEntity extends BlockEntity implements MenuProvider {
    private int burnTime;
    private int totalBurnTime;
    private int cookTime;
    private int totalCookTime = 200;
    private ItemStack fuel = ItemStack.EMPTY;
    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;

    public ClayKilnBlockEntity(BlockPos pos, BlockState state) {
        super(null, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.forgeborneodyssey.clay_kiln");
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

        if (this.burnTime > 0) {
            this.burnTime--;
            if (this.burnTime == 0) {
                this.fuel = ItemStack.EMPTY;
                this.level.setBlock(this.worldPosition, this.getBlockState().setValue(BlockStateProperties.LIT, false), 3);
            }
        }

        if (this.burnTime <= 0 && canBurn()) {
            this.totalBurnTime = this.burnTime = getBurnTime(this.fuel);
            if (this.burnTime > 0) {
                needsUpdate = true;
                this.fuel.shrink(1);
                this.level.setBlock(this.worldPosition, this.getBlockState().setValue(BlockStateProperties.LIT, true), 3);
            }
        }

        if (isBurning() && canCook()) {
            this.cookTime++;
            if (this.cookTime >= this.totalCookTime) {
                this.cookTime = 0;
                cook();
                needsUpdate = true;
            }
        } else {
            this.cookTime = 0;
        }

        if (needsUpdate) {
            this.setChanged();
        }
    }

    private boolean canBurn() {
        return !this.fuel.isEmpty() && getBurnTime(this.fuel) > 0;
    }

    private boolean isBurning() {
        return this.burnTime > 0;
    }

    private boolean canCook() {
        if (this.input.isEmpty()) return false;
        if (this.output.isEmpty()) return true;
        return this.output.isItemEqual(this.input) && this.output.getCount() < this.output.getMaxStackSize();
    }

    private void cook() {
        if (!canCook()) return;
        
        if (this.output.isEmpty()) {
            this.output = this.input.copy();
            this.output.setCount(1);
        } else {
            this.output.grow(1);
        }
        this.input.shrink(1);
    }

    private int getBurnTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) return 1600;
        if (stack.is(Items.WOOD) || stack.is(Items.STICK)) return 300;
        return 0;
    }

    public void toggleFire(Player player) {
        if (isBurning()) {
            this.burnTime = 0;
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(BlockStateProperties.LIT, false), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("BurnTime", this.burnTime);
        tag.putInt("TotalBurnTime", this.totalBurnTime);
        tag.putInt("CookTime", this.cookTime);
        tag.put("Fuel", this.fuel.save(new CompoundTag()));
        tag.put("Input", this.input.save(new CompoundTag()));
        tag.put("Output", this.output.save(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.burnTime = tag.getInt("BurnTime");
        this.totalBurnTime = tag.getInt("TotalBurnTime");
        this.cookTime = tag.getInt("CookTime");
        this.fuel = ItemStack.of(tag.getCompound("Fuel"));
        this.input = ItemStack.of(tag.getCompound("Input"));
        this.output = ItemStack.of(tag.getCompound("Output"));
    }

    public void dropContents() {
        if (this.level == null) return;
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.fuel);
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.input);
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.output);
    }

    public ItemStack getFuel() { return fuel; }
    public ItemStack getInput() { return input; }
    public ItemStack getOutput() { return output; }
    public void setFuel(ItemStack stack) { this.fuel = stack; }
    public void setInput(ItemStack stack) { this.input = stack; }
    public void setOutput(ItemStack stack) { this.output = stack; }
    public int getBurnTime() { return burnTime; }
    public int getTotalBurnTime() { return totalBurnTime; }
    public int getCookTime() { return cookTime; }
    public int getTotalCookTime() { return totalCookTime; }
}