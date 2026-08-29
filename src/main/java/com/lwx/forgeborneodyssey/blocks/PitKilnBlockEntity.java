package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class PitKilnBlockEntity extends BlockEntity {

    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final LazyOptional<IItemHandler> inventoryHandler = LazyOptional.of(() -> inventory);

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public float temperature = 20.0F;
    public float peakTemperature = 20.0F;
    public int oxygenAccumulator = 0;
    public int fuelStack = 0;
    public int fuelBurnTicks = 0;
    public boolean ignited = false;
    public int highTempTicks = 0;
    public int coolDownTicks = 0;
    public ItemStack fuelItem = ItemStack.EMPTY;

    public static final float MAX_TEMPERATURE = 900.0F;
    public static final float BLOWPIPE_MAX_TEMPERATURE = 1200.0F;
    public static final float ROOM_TEMPERATURE = 20.0F;
    public static final int OXYGEN_MIN = -100;
    public static final int OXYGEN_MAX = 100;
    public static final int COOL_DOWN_REQUIRED = 1200;
    public static final int MAX_FUEL_STACK = 24;

    public int blowBoostTicks = 0;

    private static final Set<Block> EARTH_BLOCKS = Set.of(
            Blocks.DIRT,
            Blocks.GRASS_BLOCK,
            Blocks.COARSE_DIRT,
            Blocks.ROOTED_DIRT,
            Blocks.PODZOL,
            Blocks.MYCELIUM,
            Blocks.MUD,
            Blocks.PACKED_MUD,
            Blocks.MUDDY_MANGROVE_ROOTS
    );

    public static int getInsulationCount(Level level, BlockPos pos, Direction facing) {
        int count = 0;
        if (isEarthBlock(level.getBlockState(pos.below()))) count++;
        if (isEarthBlock(level.getBlockState(pos.relative(facing.getOpposite())))) count++;
        if (isEarthBlock(level.getBlockState(pos.relative(facing.getClockWise())))) count++;
        if (isEarthBlock(level.getBlockState(pos.relative(facing.getCounterClockWise())))) count++;
        return count;
    }

    public static float getInsulationFactor(int count) {
        return switch (count) {
            case 4 -> 1.0F;
            case 3 -> 0.75F;
            case 2 -> 0.50F;
            case 1 -> 0.30F;
            default -> 0.15F;
        };
    }

    public static float getInsulatedMaxTemp(int count) {
        return switch (count) {
            case 4 -> 900.0F;
            case 3 -> 750.0F;
            case 2 -> 600.0F;
            case 1 -> 410.0F;
            default -> 260.0F;
        };
    }

    private static boolean isEarthBlock(BlockState state) {
        return EARTH_BLOCKS.contains(state.getBlock());
    }

    private static boolean isFireMouthOpen(Level level, BlockPos pos, Direction facing) {
        BlockState fmState = level.getBlockState(pos.relative(facing));
        return fmState.is(ModBlocks.FIRE_MOUTH.get()) && fmState.getValue(FireMouthBlock.OPEN);
    }

    @Nullable
    public static PitKilnBlockEntity findKilnBehindFireMouth(Level level, BlockPos fireMouthPos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos candidatePos = fireMouthPos.relative(dir);
            BlockState candidateState = level.getBlockState(candidatePos);
            if (candidateState.is(ModBlocks.PIT_KILN.get())) {
                Direction kilnFacing = candidateState.getValue(PitKilnBlock.FACING);
                if (dir == kilnFacing.getOpposite()) {
                    if (level.getBlockEntity(candidatePos) instanceof PitKilnBlockEntity k) {
                        return k;
                    }
                }
            }
        }
        return null;
    }

    public static float getOxygenDelta(PitKilnBlock.VentState vent, boolean fireMouthOpen) {
        float delta = 0;
        if (fireMouthOpen) {
            delta += 1;
        } else {
            delta -= 1;
        }
        switch (vent) {
            case OPEN -> delta += 1;
            case HALF -> delta += 0.5F;
            case CLOSED -> delta -= 2;
        }
        return delta;
    }

    public PitKilnBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.PIT_KILN_BLOCK_ENTITY.get(), pos, state);
    }

    public int getGreenwareCount() {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) count++;
        }
        return count;
    }

    public int getEmptySlot() {
        for (int i = 0; i < 4; i++) {
            if (inventory.getStackInSlot(i).isEmpty()) return i;
        }
        return -1;
    }

    public int getLastFilledSlot() {
        for (int i = 3; i >= 0; i--) {
            if (!inventory.getStackInSlot(i).isEmpty()) return i;
        }
        return -1;
    }

    public boolean isGreenware(ItemStack stack) {
        return stack.is(ModItems.GREENWARE_CRUCIBLE.get()) ||
                stack.is(ModItems.GREENWARE_MOLD.get()) ||
                stack.is(ModItems.GREENWARE_BRICK.get()) ||
                stack.is(ModItems.GREENWARE_BLOWPIPE.get());
    }

    public ItemStack getResultForSlot(ItemStack greenware, RandomSource random) {
        int oxy = oxygenAccumulator;
        float temp = peakTemperature;

        boolean isDried = greenware.hasTag() && greenware.getTag().getBoolean("Dried");

        if (greenware.is(ModItems.GREENWARE_CRUCIBLE.get())) {
            if (oxy < -30 && temp > 900) {
                if (!isDried && random.nextFloat() > 0.15F) {
                    return new ItemStack(ModItems.KILN_WASTE_SHARD.get(), 2);
                }
                return new ItemStack(ModItems.GRAY_CRUCIBLE.get());
            }
            return new ItemStack(ModItems.KILN_WASTE_SHARD.get(), 2);
        }

        if (greenware.is(ModItems.GREENWARE_MOLD.get())) {
            if (oxy > 30 && temp > 850) {
                if (!isDried && random.nextFloat() > 0.15F) {
                    return new ItemStack(ModItems.KILN_WASTE_SHARD.get(), 1);
                }
                return new ItemStack(ModItems.RED_MOLD.get());
            }
            return new ItemStack(ModItems.KILN_WASTE_SHARD.get(), 1);
        }

        if (greenware.is(ModItems.GREENWARE_BRICK.get())) {
            if (temp > 1000 && highTempTicks > 300) {
                if (!isDried && random.nextFloat() > 0.15F) {
                    return new ItemStack(ModItems.KILN_WASTE_SHARD.get(), 4);
                }
                return new ItemStack(ModItems.FIRED_BRICK.get(), 2);
            }
            return new ItemStack(ModItems.KILN_WASTE_SHARD.get(), 4);
        }

        if (greenware.is(ModItems.GREENWARE_BLOWPIPE.get())) {
            if (oxy < -50 && temp > 950) {
                if (!isDried && random.nextFloat() > 0.15F) {
                    return new ItemStack(ModItems.KILN_WASTE_SHARD.get(), 1);
                }
                return new ItemStack(ModItems.CERAMIC_BLOWPIPE.get());
            }
            return new ItemStack(ModItems.KILN_WASTE_SHARD.get(), 1);
        }

        return ItemStack.EMPTY;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PitKilnBlockEntity entity) {
        if (level.isClientSide) return;

        int stage = state.getValue(PitKilnBlock.STAGE);
        PitKilnBlock.VentState vent = state.getValue(PitKilnBlock.VENT);
        boolean hasGrate = state.getValue(PitKilnBlock.HAS_GRATE);

        // ========== Stage 2: 升温中（点火后，温度 < 100 时） ==========
        if (stage == 2 && entity.ignited) {
            if (entity.fuelStack > 0) {
                entity.fuelBurnTicks--;
                if (entity.fuelBurnTicks <= 0) {
                    entity.fuelStack--;
                    if (entity.fuelStack <= 0) entity.fuelItem = ItemStack.EMPTY;
                    entity.fuelBurnTicks = 1800;
                }

                // 吹管助推衰减
                if (entity.blowBoostTicks > 0) {
                    entity.blowBoostTicks--;
                }

                // 每10tick升温
                if (level.getGameTime() % 10 == 0) {
                    Direction facing = state.getValue(PitKilnBlock.FACING);
                    int insulation = getInsulationCount(level, pos, facing);
                    float insulationFactor = getInsulationFactor(insulation);
                    float effectiveMaxTemp = getInsulatedMaxTemp(insulation);

                    // 吹管助推：火门开启 + 吹管活跃 → 升温速度提升50%
                    boolean fmOpen = isFireMouthOpen(level, pos, facing);
                    float blowpipeBonus = (entity.blowBoostTicks > 0 && fmOpen) ? 1.5F : 1.0F;

                    float efficiency = hasGrate ? 1.0F : 0.3F;
                    float ventBonus = (vent == PitKilnBlock.VentState.CLOSED) ? 1.2F : 0.8F;
                    float addedTemp = 1.0F * efficiency * ventBonus * insulationFactor * blowpipeBonus;
                    entity.temperature = Math.min(entity.temperature + addedTemp, effectiveMaxTemp);
                    if (entity.temperature > entity.peakTemperature) {
                        entity.peakTemperature = entity.temperature;
                    }
                }

                // 黑烟粒子（Stage 2 特征），每5tick生成一次
                if (level.getGameTime() % 5 == 0) {
                    spawnSmokeParticles(level, pos, state);
                    spawnFlameParticles(level, pos, vent);
                }

                // 氧气累计
                if (level.getGameTime() % 10 == 0) {
                    Direction facing = state.getValue(PitKilnBlock.FACING);
                    boolean fmOpen = isFireMouthOpen(level, pos, facing);
                    float oxyDelta = getOxygenDelta(vent, fmOpen);
                    entity.oxygenAccumulator = (int) Mth.clamp(entity.oxygenAccumulator + oxyDelta, OXYGEN_MIN, OXYGEN_MAX);
                }

                // 温度超过100℃ → 进入 Stage 3
                if (entity.temperature > 100) {
                    level.setBlock(pos, state.setValue(PitKilnBlock.STAGE, 3), 3);
                }
            } else {
                // 燃料耗尽：缓慢降温，火焰熄灭
                entity.coolDown(0.3F);
                if (entity.temperature <= ROOM_TEMPERATURE + 1) {
                    entity.ignited = false;
                }
            }
            entity.setChanged();
            return;
        }
        // ========== Stage 3: 高温/还原期 ==========
        if (stage == 3) {
            if (level.isRainingAt(pos.above(2))) {
                explodeKiln(level, pos, entity);
                return;
            }

            // 吹管助推衰减
            if (entity.blowBoostTicks > 0) {
                entity.blowBoostTicks--;
            }

            // 消耗燃料
            if (entity.fuelStack > 0) {
                entity.fuelBurnTicks--;
                if (entity.fuelBurnTicks <= 0) {
                    entity.fuelStack--;
                    if (entity.fuelStack <= 0) entity.fuelItem = ItemStack.EMPTY;
                    entity.fuelBurnTicks = 1800;

                    // 30%几率产生灰烬
                    if (level.getRandom().nextFloat() < 0.3F) {
                        spawnAshParticles(level, pos, state);
                    }
                }

                // 每10tick向窑坑发送热脉冲
                if (level.getGameTime() % 10 == 0) {
                    Direction facing = state.getValue(PitKilnBlock.FACING);
                    int insulation = getInsulationCount(level, pos, facing);
                    float insulationFactor = getInsulationFactor(insulation);
                    float effectiveMaxTemp = getInsulatedMaxTemp(insulation);

                    // 吹管助推：火门开启 + 吹管活跃 → 温度上限提升至1200，升温速度提升50%
                    boolean fmOpen = isFireMouthOpen(level, pos, facing);
                    float currentMaxTemp = effectiveMaxTemp;
                    float blowpipeBonus = 1.0F;
                    if (entity.blowBoostTicks > 0 && fmOpen) {
                        currentMaxTemp = BLOWPIPE_MAX_TEMPERATURE;
                        blowpipeBonus = 1.5F;
                    }

                    float efficiency = hasGrate ? 1.0F : 0.3F;
                    float ventBonus = (vent == PitKilnBlock.VentState.CLOSED) ? 1.2F : 0.8F;
                    float addedTemp = 1.0F * efficiency * ventBonus * insulationFactor * blowpipeBonus;

                    if (entity.temperature < currentMaxTemp) {
                        entity.temperature = Math.min(entity.temperature + addedTemp, currentMaxTemp);
                    } else if (entity.temperature > currentMaxTemp) {
                        entity.temperature = Math.max(currentMaxTemp, entity.temperature - 0.5F);
                    }
                    if (entity.temperature > entity.peakTemperature) {
                        entity.peakTemperature = entity.temperature;
                    }

                    // 氧气累计
                    float oxyDelta = getOxygenDelta(vent, fmOpen);
                    entity.oxygenAccumulator = (int) Mth.clamp(entity.oxygenAccumulator + oxyDelta, OXYGEN_MIN, OXYGEN_MAX);

                    // 高温计时
                    if (entity.temperature > 900) {
                        entity.highTempTicks++;
                    }

                    // 粒子效果
                    if (entity.temperature > 100) {
                        spawnFlameParticles(level, pos, vent);
                        if (vent != PitKilnBlock.VentState.CLOSED) {
                            spawnSmokeParticles(level, pos, state);
                        }
                        // 吹管助推时额外火焰粒子
                        if (entity.blowBoostTicks > 0 && fmOpen) {
                            spawnBlowpipeBoostParticles(level, pos, facing);
                        }
                    }
                }
            } else {
                // 无燃料：降温
                entity.coolDown(0.5F);
            }

            // 燃料耗尽 + 温度降到50以下 → 进入冷却
            if (entity.fuelStack <= 0 && entity.temperature < 50) {
                level.setBlock(pos, state.setValue(PitKilnBlock.STAGE, 4), 3);
                entity.coolDownTicks = 0;
            }

            entity.setChanged();
            return;
        }

        // ========== Stage 4: 冷却中 ==========
        if (stage == 4) {
            entity.coolDownTicks++;
            entity.coolDown(1.0F);

            if (level.getRandom().nextInt(4) == 0) {
                spawnSteamParticles(level, pos);
            }

            if (entity.temperature < 50 && entity.coolDownTicks > COOL_DOWN_REQUIRED) {
                for (int i = 0; i < 4; i++) {
                    ItemStack greenware = entity.getInventory().getStackInSlot(i);
                    if (!greenware.isEmpty()) {
                        ItemStack result = entity.getResultForSlot(greenware, level.getRandom());
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, result);
                        entity.getInventory().setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
                ItemStack ash = new ItemStack(ModItems.PLANT_ASH.get(), 2 + level.getRandom().nextInt(3));
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, ash);
                if (level.getBlockState(pos.above()).is(ModBlocks.KILN_LID.get())) {
                    level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
                }
                level.setBlock(pos, ModBlocks.KILN_ASH_PILE.get().defaultBlockState()
                        .setValue(PitKilnBlock.FACING, state.getValue(PitKilnBlock.FACING)), 3);
                level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.0F, 0.8F);
            }
            entity.setChanged();
            return;
        }
    }

    public void coolDown(float factor) {
        temperature = Math.max(ROOM_TEMPERATURE, temperature - (0.5F * factor));
    }

    static void explodeKiln(Level level, BlockPos pos, PitKilnBlockEntity entity) {
        for (int i = 0; i < 4; i++) {
            entity.getInventory().setStackInSlot(i, ItemStack.EMPTY);
        }
        level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
        if (level.getBlockState(pos.above()).is(ModBlocks.KILN_LID.get())) {
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
        }
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.5F, Level.ExplosionInteraction.NONE);
        ItemStack waste = new ItemStack(ModItems.KILN_WASTE_SHARD.get(), 2 + level.getRandom().nextInt(3));
        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, waste);
    }

    private static void spawnFlameParticles(Level level, BlockPos pos, PitKilnBlock.VentState vent) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        double bx = pos.getX() + 0.5;
        double by = pos.getY() + 0.15;
        double bz = pos.getZ() + 0.5;

        if (vent == PitKilnBlock.VentState.CLOSED) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    bx, by, bz, 4, 0.12, 0.05, 0.12, 0.04);
        } else {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    bx, by, bz, 4, 0.12, 0.05, 0.12, 0.04);
        }
    }

    private static void spawnBlowpipeBoostParticles(Level level, BlockPos pos, Direction facing) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // 火门位置喷射大量火焰
        BlockPos fmPos = pos.relative(facing);
        double fmX = fmPos.getX() + 0.5;
        double fmY = fmPos.getY() + 0.5;
        double fmZ = fmPos.getZ() + 0.5;

        // 向火门前方喷射火焰粒子
        double dx = facing.getStepX() * 0.4;
        double dz = facing.getStepZ() * 0.4;

        serverLevel.sendParticles(ParticleTypes.FLAME,
                fmX, fmY, fmZ, 8, 0.25, 0.15, 0.25, 0.08);
        serverLevel.sendParticles(ParticleTypes.FLAME,
                fmX + dx, fmY + 0.1, fmZ + dz, 5, 0.15, 0.1, 0.15, 0.12);

        // 窑内火焰翻腾
        double bx = pos.getX() + 0.5;
        double by = pos.getY() + 0.2;
        double bz = pos.getZ() + 0.5;
        serverLevel.sendParticles(ParticleTypes.FLAME,
                bx, by, bz, 6, 0.2, 0.1, 0.2, 0.06);
        serverLevel.sendParticles(ParticleTypes.SMOKE,
                bx, by + 0.3, bz, 3, 0.15, 0.05, 0.15, 0.02);
    }

    private static void spawnSteamParticles(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.2;
        double z = pos.getZ() + 0.5;
        serverLevel.sendParticles(ParticleTypes.CLOUD,
                x, y, z, 1, 0.3, 0.1, 0.3, 0.02);
    }

    private static void spawnSmokeParticles(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Direction facing = state.getValue(PitKilnBlock.FACING);
        PitKilnBlock.VentState vent = state.getValue(PitKilnBlock.VENT);

        // 火门打开时从火门出烟
        if (isFireMouthOpen(level, pos, facing)) {
            BlockPos fireMouthPos = pos.relative(facing);
            double fmX = fireMouthPos.getX() + 0.5;
            double fmY = fireMouthPos.getY() + 0.6;
            double fmZ = fireMouthPos.getZ() + 0.5;
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    fmX, fmY, fmZ, 3, 0.15, 0.05, 0.15, 0.03);
        }

        // 通风口未关闭时从顶部出烟
        if (vent != PitKilnBlock.VentState.CLOSED) {
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 1.3;
            double cz = pos.getZ() + 0.5;
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    cx, cy, cz, 3, 0.2, 0.05, 0.2, 0.03);
        }
    }

    private static void spawnAshParticles(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Direction facing = state.getValue(PitKilnBlock.FACING);
        PitKilnBlock.VentState vent = state.getValue(PitKilnBlock.VENT);

        // 火门打开时从火门飘出灰烬
        if (isFireMouthOpen(level, pos, facing)) {
            BlockPos fireMouthPos = pos.relative(facing);
            double fmX = fireMouthPos.getX() + 0.5;
            double fmY = fireMouthPos.getY() + 0.5;
            double fmZ = fireMouthPos.getZ() + 0.5;
            serverLevel.sendParticles(ParticleTypes.WHITE_ASH,
                    fmX, fmY, fmZ, 2, 0.15, 0.05, 0.15, 0.01);
        }

        // 通风口未关闭时从顶部飘出灰烬
        if (vent != PitKilnBlock.VentState.CLOSED) {
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 1.2;
            double cz = pos.getZ() + 0.5;
            serverLevel.sendParticles(ParticleTypes.WHITE_ASH,
                    cx, cy, cz, 2, 0.2, 0.05, 0.2, 0.01);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putFloat("Temperature", temperature);
        tag.putFloat("PeakTemperature", peakTemperature);
        tag.putInt("OxygenAccum", oxygenAccumulator);
        tag.putInt("FuelStack", fuelStack);
        tag.putInt("FuelBurnTicks", fuelBurnTicks);
        tag.putBoolean("Ignited", ignited);
        tag.putInt("HighTempTicks", highTempTicks);
        tag.putInt("CoolDownTicks", coolDownTicks);
        tag.putInt("BlowBoostTicks", blowBoostTicks);
        if (!fuelItem.isEmpty()) {
            tag.put("FuelItem", fuelItem.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        temperature = tag.getFloat("Temperature");
        peakTemperature = tag.getFloat("PeakTemperature");
        oxygenAccumulator = tag.getInt("OxygenAccum");
        fuelStack = tag.getInt("FuelStack");
        fuelBurnTicks = tag.getInt("FuelBurnTicks");
        ignited = tag.getBoolean("Ignited");
        highTempTicks = tag.getInt("HighTempTicks");
        coolDownTicks = tag.getInt("CoolDownTicks");
        blowBoostTicks = tag.getInt("BlowBoostTicks");
        if (tag.contains("FuelItem")) {
            fuelItem = ItemStack.of(tag.getCompound("FuelItem"));
        } else {
            fuelItem = ItemStack.EMPTY;
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryHandler.invalidate();
    }
}