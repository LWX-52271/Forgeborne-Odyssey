package com.lwx.forgeborneodyssey.client.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class KnappingRecipe {

    private final ItemStack input;
    private final List<KnappingOutputEntry> outputs;
    private final String descriptionKey;
    private final int estimatedHits;

    public KnappingRecipe(ItemStack input, List<KnappingOutputEntry> outputs, String descriptionKey, int estimatedHits) {
        this.input = input;
        this.outputs = outputs;
        this.descriptionKey = descriptionKey;
        this.estimatedHits = estimatedHits;
    }

    public ItemStack getInput() {
        return input;
    }

    public List<KnappingOutputEntry> getOutputs() {
        return outputs;
    }

    public Component getDescription() {
        return Component.translatable(descriptionKey);
    }

    public int getEstimatedHits() {
        return estimatedHits;
    }

    public static class KnappingOutputEntry {
        private final ItemStack stack;
        private final int weight;
        private final int totalWeight;

        public KnappingOutputEntry(ItemStack stack, int weight, int totalWeight) {
            this.stack = stack;
            this.weight = weight;
            this.totalWeight = totalWeight;
        }

        public ItemStack getStack() {
            return stack;
        }

        public int getWeight() {
            return weight;
        }

        public int getTotalWeight() {
            return totalWeight;
        }

        public int getPercentage() {
            return totalWeight > 0 ? (weight * 100) / totalWeight : 0;
        }
    }
}