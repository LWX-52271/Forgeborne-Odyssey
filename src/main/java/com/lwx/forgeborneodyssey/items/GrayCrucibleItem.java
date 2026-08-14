package com.lwx.forgeborneodyssey.items;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GrayCrucibleItem extends Item {

    public static final int CAPACITY = 1000;

    public GrayCrucibleItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
                    return LazyOptional.of(() -> new CrucibleFluidHandler(stack)).cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    private static class CrucibleFluidHandler implements IFluidHandlerItem {

        private final ItemStack container;

        CrucibleFluidHandler(ItemStack container) {
            this.container = container;
        }

        @Override
        public @NotNull ItemStack getContainer() {
            return container;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            CompoundTag tag = container.getTag();
            if (tag != null && tag.contains("Fluid")) {
                return FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));
            }
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return 0;

            CompoundTag tag = container.getOrCreateTag();
            FluidStack existing = FluidStack.EMPTY;
            if (tag.contains("Fluid")) {
                existing = FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));
            }

            if (existing.isEmpty()) {
                int amount = Math.min(resource.getAmount(), CAPACITY);
                if (action.execute()) {
                    FluidStack newFluid = resource.copy();
                    newFluid.setAmount(amount);
                    tag.put("Fluid", newFluid.writeToNBT(new CompoundTag()));
                }
                return amount;
            }

            if (!existing.isFluidEqual(resource)) return 0;

            int amount = Math.min(resource.getAmount(), CAPACITY - existing.getAmount());
            if (action.execute()) {
                existing.grow(amount);
                tag.put("Fluid", existing.writeToNBT(new CompoundTag()));
            }
            return amount;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return FluidStack.EMPTY;

            CompoundTag tag = container.getTag();
            if (tag == null || !tag.contains("Fluid")) return FluidStack.EMPTY;

            FluidStack existing = FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));
            if (!existing.isFluidEqual(resource)) return FluidStack.EMPTY;

            int amount = Math.min(resource.getAmount(), existing.getAmount());
            FluidStack result = existing.copy();
            result.setAmount(amount);

            if (action.execute()) {
                existing.shrink(amount);
                if (existing.isEmpty()) {
                    tag.remove("Fluid");
                } else {
                    tag.put("Fluid", existing.writeToNBT(new CompoundTag()));
                }
            }
            return result;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            CompoundTag tag = container.getTag();
            if (tag == null || !tag.contains("Fluid")) return FluidStack.EMPTY;

            FluidStack existing = FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));
            if (existing.isEmpty()) return FluidStack.EMPTY;

            int amount = Math.min(maxDrain, existing.getAmount());
            FluidStack result = existing.copy();
            result.setAmount(amount);

            if (action.execute()) {
                existing.shrink(amount);
                if (existing.isEmpty()) {
                    tag.remove("Fluid");
                } else {
                    tag.put("Fluid", existing.writeToNBT(new CompoundTag()));
                }
            }
            return result;
        }
    }
}