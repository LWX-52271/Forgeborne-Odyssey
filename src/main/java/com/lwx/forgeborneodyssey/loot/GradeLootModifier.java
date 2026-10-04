package com.lwx.forgeborneodyssey.loot;

import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.world.OreDropCalculator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class GradeLootModifier extends LootModifier {
    public static final Codec<GradeLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, GradeLootModifier::new));

    private static final double PURITY_VARIATION = 0.1;
    private static final double QUALITY_VARIATION = 0.1;

    public GradeLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.hasParam(LootContextParams.BLOCK_ENTITY)) {
            BlockEntity blockEntity = context.getParam(LootContextParams.BLOCK_ENTITY);
            if (blockEntity instanceof StressBlock.StressBlockEntity stressBE) {
                float grade = stressBE.getGrade();

                if (grade >= 0.0f) {
                    ObjectArrayList<ItemStack> newLoot = new ObjectArrayList<>();
                    Block block = stressBE.getBlockState().getBlock();

                    for (ItemStack stack : generatedLoot) {
                        if (OreDropCalculator.isRawOreItem(stack)) {
                            int count = OreDropCalculator.calculateOreDropCount(grade);
                            for (int i = 0; i < count; i++) {
                                newLoot.add(createFragment(stack, grade, context.getRandom(), block));
                            }
                        } else if (OreDropCalculator.isRubbleItem(stack)) {
                            int count = OreDropCalculator.calculateRubbleDropCount(grade);
                            for (int i = 0; i < count; i++) {
                                newLoot.add(createFragment(stack, grade, context.getRandom(), block));
                            }
                        } else {
                            newLoot.add(stack);
                        }
                    }
                    return newLoot;
                }
            }
        }
        return generatedLoot;
    }

    /**
     * 创建一个带纯度、品质和重量标签的碎片。
     * 普通矿石：纯度和品质基于方块品位，带 ±0.1 随机波动，钳制在 [0, 1]。
     * 砂锡矿：纯度和品质偏高（0.60~1.0），反映水流自然淘洗富集，不受品位影响。
     */
    private static ItemStack createFragment(ItemStack template, float grade, RandomSource random, Block block) {
        ItemStack stack = template.copy();
        stack.setCount(1);
        CompoundTag tag = stack.getOrCreateTag();
        float purity;
        float quality;
        if (block == ModBlocks.CASSITERITE_PLACER_BLOCK.get()) {
            purity = 0.10f + random.nextFloat() * 0.25f;
            quality = 0.10f + random.nextFloat() * 0.25f;
        } else {
            purity = (float) Math.max(0.0, Math.min(1.0, grade + (random.nextFloat() - 0.5) * 2.0 * PURITY_VARIATION));
            quality = (float) Math.max(0.0, Math.min(1.0, grade + (random.nextFloat() - 0.5) * 2.0 * QUALITY_VARIATION));
        }
        tag.putFloat("ore_purity", purity);
        tag.putFloat("ore_quality", quality);
        return stack;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}