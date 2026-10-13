package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.api.ForgeborneAPI;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.world.OreGrade;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StressBlockEntity extends BlockEntity {
    private float stress = 0.0f;
    private int lastDamageStage = 0;
    private float grade = -1.0f;
    private boolean gradeInitialized = false;

    public StressBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.STRESS_BLOCK_ENTITY.get(), pos, blockState);
    }

    public float getStress() {
        return stress;
    }

    public void setStress(float stress) {
        this.stress = stress;
        updateCrackStage();
        setChanged();
    }

    public void addStress(float amount) {
        this.stress += amount;
        updateCrackStage();
        setChanged();
    }

    private void updateCrackStage() {
        Block block = getBlockState().getBlock();
        float maxStress = ForgeborneAPI.getMaxStress(block);
        int calculatedStage;
        if (maxStress > 0) {
            calculatedStage = Math.min((int) ((stress / maxStress) * 10), 9);
        } else {
            calculatedStage = 0;
        }

        if (calculatedStage > lastDamageStage) {
            lastDamageStage = calculatedStage;
            setChanged();
        }
    }

    private void setCrackStage(BlockPos pos, int stage) {
    }

    public void clearCrackStage() {
        lastDamageStage = 0;
        setChanged();
    }

    public int getLastDamageStage() {
        return lastDamageStage;
    }

    public void setLastDamageStage(int stage) {
        this.lastDamageStage = stage;
    }

    public float getGrade() {
        return grade;
    }

    public void setGrade(float grade) {
        this.grade = Math.max(0.0f, Math.min(1.0f, grade));
        this.gradeInitialized = true;
        setChanged();
    }

    public boolean isGradeInitialized() {
        return gradeInitialized;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!gradeInitialized && level != null && !level.isClientSide) {
            if (getBlockState().is(ModBlocks.CASSITERITE_PLACER_BLOCK.get())) {
                setGrade(OreGrade.generatePlacerGradeValue(level.random));
            } else {
                setGrade(OreGrade.generateRandomGradeValue(level.random));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat(StressBlock.STRESS_TAG, stress);
        tag.putInt("lastDamageStage", lastDamageStage);
        tag.putFloat("grade", grade);
        tag.putBoolean("gradeInitialized", gradeInitialized);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(StressBlock.STRESS_TAG)) {
            stress = tag.getFloat(StressBlock.STRESS_TAG);
        }
        lastDamageStage = tag.getInt("lastDamageStage");
        if (tag.contains("grade")) {
            grade = tag.getFloat("grade");
        }
        gradeInitialized = tag.getBoolean("gradeInitialized");
        if (level != null && level.isClientSide) {
            updateCrackStage();
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
        if (tag == null) {
            tag = new CompoundTag();
        }
        load(tag);
    }
}