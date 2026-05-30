package com.lwx.forgeborneodyssey.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lwx.forgeborneodyssey.core.registration.ModRecipes;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import com.lwx.forgeborneodyssey.items.tools.WroughtCopperAxeItem;
import com.lwx.forgeborneodyssey.items.tools.WroughtSilverAxeItem;
import com.lwx.forgeborneodyssey.items.tools.WroughtGoldAxeItem;
import com.lwx.forgeborneodyssey.items.weapons.MetalKnifeItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 锻造配方类
 */
public class ForgingRecipe implements Recipe<Container> {
    
    private final ResourceLocation id;
    private final String group;
    private final Ingredient input;
    private final List<ResultEntry> results;
    private final NonNullList<Ingredient> ingredients;
    
    public ForgingRecipe(ResourceLocation id, String group, Ingredient input, List<ResultEntry> results) {
        this.id = id;
        this.group = group;
        this.input = input;
        this.results = results;
        this.ingredients = NonNullList.of(Ingredient.EMPTY, input);
    }
    
    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }
    
    @Override
    public ItemStack assemble(Container container, net.minecraft.core.RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }
    
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 1;
    }
    
    public ItemStack getResultItem() {
        if (results.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return results.get(0).stack.copy();
    }
    
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess registryAccess) {
        return getResultItem();
    }
    
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }
    
    @Override
    public ResourceLocation getId() {
        return id;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FORGING_RECIPE_SERIALIZER.get();
    }
    
    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FORGING_RECIPE_TYPE.get();
    }
    
    public Ingredient getInput() {
        return input;
    }
    
    public List<ResultEntry> getResults() {
        return results;
    }
    
    public boolean inputMatches(ItemStack stack) {
        return input.test(stack);
    }
    
    public static class ResultEntry {
        public final ItemStack stack;
        public final int progressRequired;
        
        public ResultEntry(ItemStack stack, int progressRequired) {
            this.stack = stack;
            this.progressRequired = progressRequired;
        }
        
        public ItemStack getStack() {
            return stack;
        }
        
        public int getProgressRequired() {
            return progressRequired;
        }
        
        /**
         * 创建带有质量和纯度属性的结果物品副本
         * @param inputStack 输入的物品堆（金属胚料）
         * @return 带有质量和纯度属性的结果物品
         */
        public ItemStack createResultWithQuality(ItemStack inputStack) {
            ItemStack result = stack.copy();
            
            // 如果输入是金属胚料，获取其质量等级和纯度
            if (inputStack.getItem() instanceof AbstractMetalBilletItem billetItem) {
                AbstractMetalBilletItem.Quality quality = billetItem.getQuality(inputStack);
                float purity = billetItem.getPurity(inputStack);
                
                // 如果结果是金属刀，设置质量属性
                if (result.getItem() instanceof MetalKnifeItem knifeItem) {
                    knifeItem.setQuality(result, quality);
                    // 金属刀不继承纯度，因为它们不是直接由胚料转化而来
                }
                // 如果结果是打制铜斧，设置质量属性
                else if (result.getItem() instanceof WroughtCopperAxeItem axeItem) {
                    axeItem.setQuality(result, quality);
                }
                // 如果结果是打制银斧，设置质量属性
                else if (result.getItem() instanceof WroughtSilverAxeItem axeItem) {
                    axeItem.setQuality(result, quality);
                }
                // 如果结果是打制金斧，设置质量属性
                else if (result.getItem() instanceof WroughtGoldAxeItem axeItem) {
                    axeItem.setQuality(result, quality);
                }
                // 如果结果是软化金属条，继承质量和纯度
                else if (result.getItem() instanceof com.lwx.forgeborneodyssey.items.softmetalstrips.AbstractSoftMetalStripItem stripItem) {
                    if (inputStack.getItem() instanceof com.lwx.forgeborneodyssey.items.softmetalbillets.AbstractSoftMetalBilletItem softBilletItem) {
                        AbstractMetalBilletItem.Quality softQuality = softBilletItem.getQuality(inputStack);
                        stripItem.setQuality(result, softQuality);
                        stripItem.setPurity(result, purity);
                    }
                }
                // 如果结果是金属珠，继承质量和纯度
                else if (result.getItem() instanceof com.lwx.forgeborneodyssey.items.beads.ThrowableBeadItem) {
                    result.getOrCreateTag().putString("Quality", quality.getName());
                    result.getOrCreateTag().putFloat("Purity", purity);
                }
                // 如果结果是金属条，继承质量和纯度
                else if (result.getItem() instanceof com.lwx.forgeborneodyssey.items.metalbars.GoldBarItem ||
                         result.getItem() instanceof com.lwx.forgeborneodyssey.items.metalbars.SilverBarItem) {
                    result.getOrCreateTag().putString("Quality", quality.getName());
                    result.getOrCreateTag().putFloat("Purity", purity);
                }
            }
            
            return result;
        }
    }
    
    public static class Serializer implements RecipeSerializer<ForgingRecipe> {
        
        @Override
        public ForgingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            
            JsonObject inputJson = GsonHelper.getAsJsonObject(json, "input");
            Ingredient input = Ingredient.fromJson(inputJson);
            
            JsonArray resultsJson = GsonHelper.getAsJsonArray(json, "results");
            List<ResultEntry> results = new ArrayList<>();
            
            for (int i = 0; i < resultsJson.size(); i++) {
                JsonObject resultJson = resultsJson.get(i).getAsJsonObject();
                Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(resultJson, "item"));
                ItemStack stack = ingredient.getItems().length > 0 ? ingredient.getItems()[0].copy() : ItemStack.EMPTY;
                int progressRequired = GsonHelper.getAsInt(resultJson, "progressRequired", 100);
                results.add(new ResultEntry(stack, progressRequired));
            }
            
            return new ForgingRecipe(recipeId, group, input, results);
        }
        
        @Nullable
        @Override
        public ForgingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            Ingredient input = Ingredient.fromNetwork(buffer);
            
            int resultCount = buffer.readInt();
            List<ResultEntry> results = new ArrayList<>();
            
            for (int i = 0; i < resultCount; i++) {
                ItemStack stack = buffer.readItem();
                int progressRequired = buffer.readInt();
                results.add(new ResultEntry(stack, progressRequired));
            }
            
            return new ForgingRecipe(recipeId, group, input, results);
        }
        
        @Override
        public void toNetwork(FriendlyByteBuf buffer, ForgingRecipe recipe) {
            buffer.writeUtf(recipe.group);
            recipe.input.toNetwork(buffer);
            
            buffer.writeInt(recipe.results.size());
            for (ResultEntry result : recipe.results) {
                buffer.writeItemStack(result.stack, false);
                buffer.writeInt(result.progressRequired);
            }
        }
    }
}
