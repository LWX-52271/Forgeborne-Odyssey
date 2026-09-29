package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.menu.StoragePotMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class StoragePotBlock extends BaseEntityBlock {

    public static final IntegerProperty WATER_LEVEL = IntegerProperty.create("water_level", 0, 8);
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public StoragePotBlock() {
        super(Properties.of()
                .mapColor(MapColor.TERRACOTTA_ORANGE)
                .strength(1.0F, 3.0F)
                .sound(SoundType.DECORATED_POT)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(WATER_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATER_LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StoragePotBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlocks.STORAGE_POT_BLOCK_ENTITY.get()) {
            return (lvl, pos, st, blockEntity) -> StoragePotBlockEntity.tick(lvl, pos, st, (StoragePotBlockEntity) blockEntity);
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof StoragePotBlockEntity storagePot)) return InteractionResult.PASS;

        if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            if (level.isClientSide) return InteractionResult.SUCCESS;

            ItemStack potStack = new ItemStack(ModItems.STORAGE_POT.get());
            storagePot.saveToItem(potStack);
            storagePot.markPickedUp();

            if (!player.getInventory().add(potStack)) {
                player.drop(potStack, false);
            }

            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundType.DECORATED_POT.getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.CONSUME;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        boolean hasFluid = !storagePot.getFluidStorage().isEmpty();

        if (heldItem.is(ModItems.QUICKLIME.get()) && hasFluid) {
            if (level.isClientSide) return InteractionResult.SUCCESS;

            var potTank = storagePot.getFluidStorage();
            int drained = potTank.drain(new FluidStack(Fluids.WATER, 500), IFluidHandler.FluidAction.SIMULATE).getAmount();
            if (drained >= 500) {
                potTank.drain(new FluidStack(Fluids.WATER, 500), IFluidHandler.FluidAction.EXECUTE);
                heldItem.shrink(1);
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        new ItemStack(ModItems.SLAKED_LIME.get()));
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.5F);
                return InteractionResult.CONSUME;
            }
        }

        if (!heldItem.isEmpty()) {
            var itemHandlerOpt = heldItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
            if (itemHandlerOpt.isPresent()) {
                IFluidHandlerItem itemHandler = itemHandlerOpt.get();
                var potTank = storagePot.getFluidStorage();

                FluidStack drained = itemHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                if (!drained.isEmpty()) {
                    int filled = potTank.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                    if (filled > 0) {
                        FluidStack toTransfer = new FluidStack(drained.getFluid(), filled);
                        itemHandler.drain(toTransfer, IFluidHandler.FluidAction.EXECUTE);
                        potTank.fill(toTransfer, IFluidHandler.FluidAction.EXECUTE);
                        player.setItemInHand(hand, itemHandler.getContainer());
                        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                }

                FluidStack potDrained = potTank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                if (!potDrained.isEmpty()) {
                    int itemFilled = itemHandler.fill(potDrained, IFluidHandler.FluidAction.SIMULATE);
                    if (itemFilled > 0) {
                        FluidStack toTransfer = new FluidStack(potDrained.getFluid(), itemFilled);
                        potTank.drain(toTransfer, IFluidHandler.FluidAction.EXECUTE);
                        itemHandler.fill(toTransfer, IFluidHandler.FluidAction.EXECUTE);
                        player.setItemInHand(hand, itemHandler.getContainer());
                        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                }
            }

            if (hasFluid) {
                return InteractionResult.PASS;
            }

            if (level.isClientSide) return InteractionResult.SUCCESS;

            NetworkHooks.openScreen((ServerPlayer) player,
                    new SimpleMenuProvider(
                            (windowId, inventory, p) -> new StoragePotMenu(windowId, inventory, storagePot.getContainer(), pos),
                            storagePot.getDisplayName()
                    ), pos);
            return InteractionResult.CONSUME;
        }

        if (hasFluid) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) return InteractionResult.SUCCESS;

        NetworkHooks.openScreen((ServerPlayer) player,
                new SimpleMenuProvider(
                        (windowId, inventory, p) -> new StoragePotMenu(windowId, inventory, storagePot.getContainer(), pos),
                        storagePot.getDisplayName()
                ), pos);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof StoragePotBlockEntity storagePot) {
                if (!storagePot.isPickedUp()) {
                    Containers.dropContents(level, pos, storagePot.getContainer());
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            new ItemStack(ModItems.KILN_WASTE_SHARD.get(), 1 + level.random.nextInt(3)));
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}