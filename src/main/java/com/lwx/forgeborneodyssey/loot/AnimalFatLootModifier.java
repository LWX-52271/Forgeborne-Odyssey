package com.lwx.forgeborneodyssey.loot;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class AnimalFatLootModifier extends LootModifier {

    public static final Codec<AnimalFatLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, AnimalFatLootModifier::new));

    public AnimalFatLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.hasParam(LootContextParams.THIS_ENTITY)) {
            Entity entity = context.getParam(LootContextParams.THIS_ENTITY);
            if (isMeatAnimal(entity)) {
                int count = context.getRandom().nextInt(2);
                if (count > 0) {
                    generatedLoot.add(new ItemStack(ModItems.ANIMAL_FAT.get(), count));
                }
            }
        }
        return generatedLoot;
    }

    private static boolean isMeatAnimal(Entity entity) {
        return entity instanceof Cow
                || entity instanceof Pig
                || entity instanceof Sheep
                || entity instanceof Chicken
                || entity instanceof Rabbit
                || entity instanceof Goat;
    }
}