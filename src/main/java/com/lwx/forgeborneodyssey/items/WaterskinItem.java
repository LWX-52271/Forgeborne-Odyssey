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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WaterskinItem extends Item {

    public static final int CAPACITY = 250;
    public static final int MAX_DURABILITY = 20;

    public WaterskinItem() {
        super(new Item.Properties().stacksTo(1).durability(MAX_DURABILITY));
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
                if (current.isEmpty() || current.getAmount() < CAPACITY) {
                    if (!level.isClientSide) {
                        int needed = current.isEmpty() ? CAPACITY : CAPACITY - current.getAmount();
                        if (current.isEmpty()) {
                            setFluid(stack, new FluidStack(Fluids.WATER, needed));
                        } else {
                            current.grow(needed);
                            setFluid(stack, current);
                        }
                        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
                return InteractionResultHolder.pass(stack);
            }
        }

        if (PlayerThirstManager.isSystemEnabled()) {
            FluidStack current = getFluid(stack);
            if (!current.isEmpty()) {
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
        if (!current.isEmpty()) {
            float thirst = PlayerThirstManager.getThirst(player);
            if (thirst < PlayerThirstManager.MAX_THIRST) {
                if (!level.isClientSide) {
                    current.shrink(250);
                    if (current.isEmpty()) {
                        setFluid(stack, FluidStack.EMPTY);
                    } else {
                        setFluid(stack, current);
                    }
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
                    float toRestore = Math.min(PlayerThirstManager.THIRST_PER_WATERSKIN,
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
                    return LazyOptional.of(() -> new WaterskinFluidHandler(stack)).cast();
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
            if (current.isEmpty() || current.getAmount() < CAPACITY) {
                if (!level.isClientSide) {
                    int needed = current.isEmpty() ? CAPACITY : CAPACITY - current.getAmount();
                    if (current.isEmpty()) {
                        setFluid(stack, new FluidStack(Fluids.WATER, needed));
                    } else {
                        current.grow(needed);
                        setFluid(stack, current);
                    }
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
                    level.playSound(null, waterPos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        FluidStack fluid = getFluid(stack);
        if (!fluid.isEmpty()) {
            return true;
        }
        return stack.isDamaged();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        FluidStack fluid = getFluid(stack);
        if (!fluid.isEmpty()) {
            return Math.round(13.0F * (float) fluid.getAmount() / CAPACITY);
        }
        return Math.round(13.0F * (1.0F - (float) stack.getDamageValue() / MAX_DURABILITY));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        FluidStack fluid = getFluid(stack);
        if (!fluid.isEmpty()) {
            return 0x3F76E4;
        }
        return super.getBarColor(stack);
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

    private static class WaterskinFluidHandler implements IFluidHandlerItem {

        private final ItemStack container;

        WaterskinFluidHandler(ItemStack container) {
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
            return WaterskinItem.getFluid(container);
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