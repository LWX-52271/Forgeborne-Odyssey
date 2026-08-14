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
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ToolShapedRecipe extends ShapedRecipe {

    private final Ingredient toolIngredient;

    public ToolShapedRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                            int width, int height, NonNullList<Ingredient> ingredients,
                            ItemStack result, Ingredient toolIngredient) {
        super(id, group, category, width, height, ingredients, result);
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
        return ModRecipes.TOOL_SHAPED_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    public static class Serializer implements RecipeSerializer<ToolShapedRecipe> {

        @Override
        public ToolShapedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");

            JsonArray patternArray = GsonHelper.getAsJsonArray(json, "pattern");
            int height = patternArray.size();
            int width = 0;
            for (int i = 0; i < height; i++) {
                int len = patternArray.get(i).getAsString().length();
                if (len > width) width = len;
            }

            String[] pattern = new String[height];
            for (int i = 0; i < height; i++) {
                pattern[i] = patternArray.get(i).getAsString();
                while (pattern[i].length() < width) {
                    pattern[i] = pattern[i] + " ";
                }
            }

            JsonObject keyJson = GsonHelper.getAsJsonObject(json, "key");
            Map<Character, Ingredient> keyMap = new HashMap<>();
            for (Map.Entry<String, com.google.gson.JsonElement> entry : keyJson.entrySet()) {
                String key = entry.getKey();
                if (key.length() != 1) {
                    throw new JsonSyntaxException("Key entry must be a single character: " + key);
                }
                char c = key.charAt(0);
                Ingredient ingredient = Ingredient.fromJson(entry.getValue());
                keyMap.put(c, ingredient);
            }

            String toolKey = GsonHelper.getAsString(json, "tool", "");
            if (toolKey.length() != 1) {
                throw new JsonSyntaxException("'tool' must be a single character key");
            }
            char toolChar = toolKey.charAt(0);
            Ingredient toolIngredient = keyMap.get(toolChar);
            if (toolIngredient == null) {
                throw new JsonSyntaxException("Tool key '" + toolKey + "' not found in key definitions");
            }

            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    char c = pattern[y].charAt(x);
                    if (c != ' ') {
                        Ingredient ing = keyMap.get(c);
                        if (ing == null) {
                            throw new JsonSyntaxException("Pattern character '" + c + "' not defined in key");
                        }
                        ingredients.set(y * width + x, ing);
                    }
                }
            }

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

            return new ToolShapedRecipe(recipeId, group, CraftingBookCategory.MISC, width, height, ingredients, result, toolIngredient);
        }

        @Nullable
        @Override
        public ToolShapedRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int width = buffer.readInt();
            int height = buffer.readInt();

            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for (int i = 0; i < width * height; i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }

            ItemStack result = buffer.readItem();
            Ingredient toolIngredient = Ingredient.fromNetwork(buffer);

            return new ToolShapedRecipe(recipeId, group, CraftingBookCategory.MISC, width, height, ingredients, result, toolIngredient);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ToolShapedRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeInt(recipe.getWidth());
            buffer.writeInt(recipe.getHeight());

            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buffer);
            }

            buffer.writeItemStack(recipe.getResultItem(net.minecraft.core.RegistryAccess.EMPTY), false);
            recipe.getToolIngredient().toNetwork(buffer);
        }
    }
}