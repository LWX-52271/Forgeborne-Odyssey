package com.lwx.forgeborneodyssey.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.lwx.forgeborneodyssey.core.registration.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import javax.annotation.Nullable;

public class ToolShapelessRecipe extends ShapelessRecipe {

    private final Ingredient toolIngredient;

    public ToolShapelessRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                               NonNullList<Ingredient> ingredients, ItemStack result,
                               Ingredient toolIngredient) {
        super(id, group, category, result, ingredients);
        this.toolIngredient = toolIngredient;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && toolIngredient.test(stack)) {
                remaining.set(i, stack.copy());
            }
        }
        return remaining;
    }

    public Ingredient getToolIngredient() {
        return toolIngredient;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TOOL_SHAPELESS_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    public static class Serializer implements RecipeSerializer<ToolShapelessRecipe> {

        @Override
        public ToolShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");

            JsonArray ingredientsArray = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (int i = 0; i < ingredientsArray.size(); i++) {
                ingredients.add(Ingredient.fromJson(ingredientsArray.get(i)));
            }

            String toolKey = GsonHelper.getAsString(json, "tool", "");
            if (toolKey.isEmpty()) {
                throw new JsonSyntaxException("'tool' field is required for tool_shapeless recipe");
            }

            int toolIndex;
            try {
                toolIndex = Integer.parseInt(toolKey);
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException("'tool' must be an integer index for tool_shapeless recipe");
            }

            if (toolIndex < 0 || toolIndex >= ingredients.size()) {
                throw new JsonSyntaxException("Tool index " + toolIndex + " out of bounds for ingredients (size: " + ingredients.size() + ")");
            }

            Ingredient toolIngredient = ingredients.get(toolIndex);

            ItemStack result = ItemStack.EMPTY;
            if (json.has("result")) {
                com.google.gson.JsonElement resultElem = json.get("result");
                if (resultElem.isJsonObject()) {
                    JsonObject resultObj = resultElem.getAsJsonObject();
                    String itemId = GsonHelper.getAsString(resultObj, "item");
                    int count = GsonHelper.getAsInt(resultObj, "count", 1);
                    net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(new ResourceLocation(itemId));
                    result = new ItemStack(item, count);
                }
            }

            return new ToolShapelessRecipe(recipeId, group, CraftingBookCategory.MISC, ingredients, result, toolIngredient);
        }

        @Nullable
        @Override
        public ToolShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int ingredientCount = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientCount, Ingredient.EMPTY);
            for (int i = 0; i < ingredientCount; i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }
            ItemStack result = buffer.readItem();
            Ingredient toolIngredient = Ingredient.fromNetwork(buffer);
            return new ToolShapelessRecipe(recipeId, group, CraftingBookCategory.MISC, ingredients, result, toolIngredient);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ToolShapelessRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buffer);
            }
            buffer.writeItemStack(recipe.getResultItem(net.minecraft.core.RegistryAccess.EMPTY), false);
            recipe.getToolIngredient().toNetwork(buffer);
        }
    }
}