package com.lwx.forgeborneodyssey.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.lwx.forgeborneodyssey.core.registration.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

public class FluidContainerShapelessRecipe extends ShapelessRecipe {

    private final int fluidIngredientIndex;

    public FluidContainerShapelessRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                                         NonNullList<Ingredient> ingredients, ItemStack result,
                                         int fluidIngredientIndex) {
        super(id, group, category, result, ingredients);
        this.fluidIngredientIndex = fluidIngredientIndex;
    }

    @Override
    public boolean matches(CraftingContainer container, net.minecraft.world.level.Level level) {
        if (!super.matches(container, level)) {
            return false;
        }
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && FluidUtil.getFluidContained(stack)
                    .map(fs -> fs.getAmount() > 0).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            var handlerOpt = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
            if (handlerOpt.isPresent()) {
                IFluidHandlerItem handler = handlerOpt.get();
                FluidStack drained = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                if (!drained.isEmpty()) {
                    handler.drain(new FluidStack(drained.getFluid(), drained.getAmount()),
                            IFluidHandler.FluidAction.EXECUTE);
                    remaining.set(i, handler.getContainer().copy());
                continue;
                }
            }

            remaining.set(i, stack.getCraftingRemainingItem());
        }

        return remaining;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return getResultItem(registryAccess).copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FLUID_CONTAINER_SHAPELESS_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    public static class Serializer implements RecipeSerializer<FluidContainerShapelessRecipe> {

        @Override
        public FluidContainerShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");

            JsonArray ingredientsArray = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (int i = 0; i < ingredientsArray.size(); i++) {
                ingredients.add(Ingredient.fromJson(ingredientsArray.get(i)));
            }

            int fluidIndex = GsonHelper.getAsInt(json, "fluid_ingredient", 0);
            if (fluidIndex < 0 || fluidIndex >= ingredients.size()) {
                throw new JsonSyntaxException("fluid_ingredient index " + fluidIndex + " out of bounds for ingredients (size: " + ingredients.size() + ")");
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

            return new FluidContainerShapelessRecipe(recipeId, group, CraftingBookCategory.MISC, ingredients, result, fluidIndex);
        }

        @Nullable
        @Override
        public FluidContainerShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int ingredientCount = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientCount, Ingredient.EMPTY);
            for (int i = 0; i < ingredientCount; i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }
            ItemStack result = buffer.readItem();
            int fluidIndex = buffer.readVarInt();
            return new FluidContainerShapelessRecipe(recipeId, group, CraftingBookCategory.MISC, ingredients, result, fluidIndex);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, FluidContainerShapelessRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buffer);
            }
            buffer.writeItemStack(recipe.getResultItem(net.minecraft.core.RegistryAccess.EMPTY), false);
            buffer.writeVarInt(recipe.fluidIngredientIndex);
        }
    }
}