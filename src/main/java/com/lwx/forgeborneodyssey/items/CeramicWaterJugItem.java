package com.lwx.forgeborneodyssey.items;

import com.lwx.forgeborneodyssey.util.PlayerThirstManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CeramicWaterJugItem extends Item {

    public static final int CAPACITY = 1000;
    public static final int FILL_AMOUNT = 1000;

    public CeramicWaterJugItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            BlockState blockState = level.getBlockState(pos);
            if (blockState.getBlock() == Blocks.WATER) {
                FluidStack current = getFluid(stack);
                if (current.getAmount() < CAPACITY) {
                    if (!level.isClientSide) {
                        int needed = Math.min(FILL_AMOUNT, CAPACITY - current.getAmount());
                        if (current.isEmpty()) {
                            setFluid(stack, new FluidStack(Fluids.WATER, needed));
                        } else {
                            current.grow(needed);
                            setFluid(stack, current);
                        }
                        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
                return InteractionResultHolder.pass(stack);
            }
        }

        if (PlayerThirstManager.isSystemEnabled()) {
            FluidStack current = getFluid(stack);
            if (!current.isEmpty() && current.getAmount() >= 250) {
                float thirst = PlayerThirstManager.getThirst(player);
                if (thirst < PlayerThirstManager.MAX_THIRST) {
                    player.startUsingItem(hand);
                    return InteractionResultHolder.consume(stack);
                }
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) return stack;

        FluidStack current = getFluid(stack);
        if (!current.isEmpty() && current.getAmount() >= 250) {
            float thirst = PlayerThirstManager.getThirst(player);
            if (thirst < PlayerThirstManager.MAX_THIRST) {
                if (!level.isClientSide) {
                    int drainedAmount = 250;
                    current.shrink(drainedAmount);
                    if (current.isEmpty()) {
                        setFluid(stack, FluidStack.EMPTY);
                    } else {
                        setFluid(stack, current);
                    }
                    float toRestore = Math.min(
                            PlayerThirstManager.THIRST_PER_WATER_JUG * (drainedAmount / (float) CAPACITY),
                            PlayerThirstManager.MAX_THIRST - thirst);
                    PlayerThirstManager.addThirst(player, toRestore);
                    level.playSound(null, player.blockPosition(),
                            SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5f, 1.0f);
                }
            }
        }
        return stack;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
                    return LazyOptional.of(() -> new JugFluidHandler(stack)).cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockEntity be = level.getBlockEntity(clickedPos);
        if (be != null) {
            if (be.getCapability(ForgeCapabilities.FLUID_HANDLER, context.getClickedFace()).resolve().isPresent()) {
                return InteractionResult.PASS;
            }
        }

        BlockPos waterPos = null;
        BlockState clickedState = level.getBlockState(clickedPos);
        if (clickedState.getBlock() == Blocks.WATER) {
            waterPos = clickedPos;
        } else {
            BlockPos frontPos = clickedPos.relative(context.getClickedFace());
            BlockState frontState = level.getBlockState(frontPos);
            if (frontState.getBlock() == Blocks.WATER) {
                waterPos = frontPos;
            }
        }

        if (waterPos != null) {
            FluidStack current = getFluid(stack);
            if (current.getAmount() < CAPACITY) {
                if (!level.isClientSide) {
                    int needed = Math.min(FILL_AMOUNT, CAPACITY - current.getAmount());
                    if (current.isEmpty()) {
                        setFluid(stack, new FluidStack(Fluids.WATER, needed));
                    } else {
                        current.grow(needed);
                        setFluid(stack, current);
                    }
                    level.playSound(null, waterPos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        FluidStack fluid = getFluid(stack);
        if (!fluid.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.drink"));
        }
    }

    private static FluidStack getFluid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Fluid")) {
            return FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));
        }
        return FluidStack.EMPTY;
    }

    private static void setFluid(ItemStack stack, FluidStack fluid) {
        if (fluid.isEmpty()) {
            stack.removeTagKey("Fluid");
        } else {
            CompoundTag tag = stack.getOrCreateTag();
            tag.put("Fluid", fluid.writeToNBT(new CompoundTag()));
        }
    }

    private static class JugFluidHandler implements IFluidHandlerItem {

        private final ItemStack container;

        JugFluidHandler(ItemStack container) {
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
            return CeramicWaterJugItem.getFluid(container);
        }

        @Override
        public int getTankCapacity(int tank) {
            return CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return stack.getFluid() == Fluids.WATER;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || resource.getFluid() != Fluids.WATER) return 0;

            FluidStack existing = getFluid(container);
            if (existing.isEmpty()) {
                int amount = Math.min(resource.getAmount(), CAPACITY);
                if (action.execute()) {
                    setFluid(container, new FluidStack(Fluids.WATER, amount));
                }
                return amount;
            }

            int amount = Math.min(resource.getAmount(), CAPACITY - existing.getAmount());
            if (action.execute()) {
                existing.grow(amount);
                setFluid(container, existing);
            }
            return amount;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return FluidStack.EMPTY;

            FluidStack existing = getFluid(container);
            if (existing.isEmpty() || !existing.isFluidEqual(resource)) return FluidStack.EMPTY;

            int amount = Math.min(resource.getAmount(), existing.getAmount());
            FluidStack result = new FluidStack(Fluids.WATER, amount);

            if (action.execute()) {
                existing.shrink(amount);
                if (existing.isEmpty()) {
                    setFluid(container, FluidStack.EMPTY);
                } else {
                    setFluid(container, existing);
                }
            }
            return result;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack existing = getFluid(container);
            if (existing.isEmpty()) return FluidStack.EMPTY;

            int amount = Math.min(maxDrain, existing.getAmount());
            FluidStack result = new FluidStack(Fluids.WATER, amount);

            if (action.execute()) {
                existing.shrink(amount);
                if (existing.isEmpty()) {
                    setFluid(container, FluidStack.EMPTY);
                } else {
                    setFluid(container, existing);
                }
            }
            return result;
        }
    }
}