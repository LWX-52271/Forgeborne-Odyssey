package com.lwx.forgeborneodyssey.client.jei;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.FoodCookingRecipes;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.core.registration.ModRecipes;
import com.lwx.forgeborneodyssey.recipe.ForgingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID = new ResourceLocation(ForgeborneOdyssey.MOD_ID, "jei_plugin");

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new ForgingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new OreCrushingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new FirePitCookingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new FirePitSmeltingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new QuernGrindingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new DryingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new PitKilnFiringRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new AxeBendingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new ChiselCarvingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new KnappingRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        RecipeManager recipeManager = minecraft.level.getRecipeManager();

        List<ForgingRecipe> forgingRecipes = recipeManager.getAllRecipesFor(ModRecipes.FORGING_RECIPE_TYPE.get());
        registration.addRecipes(ForgingRecipeCategory.RECIPE_TYPE, forgingRecipes);

        registration.addRecipes(OreCrushingRecipeCategory.RECIPE_TYPE, buildOreCrushingRecipes());
        registration.addRecipes(FirePitCookingRecipeCategory.RECIPE_TYPE, buildFirePitCookingRecipes());
        registration.addRecipes(FirePitSmeltingRecipeCategory.RECIPE_TYPE, buildFirePitSmeltingRecipes());
        registration.addRecipes(QuernGrindingRecipeCategory.RECIPE_TYPE, buildQuernGrindingRecipes());
        registration.addRecipes(DryingRecipeCategory.RECIPE_TYPE, buildDryingRecipes());
        registration.addRecipes(PitKilnFiringRecipeCategory.RECIPE_TYPE, buildPitKilnFiringRecipes());
        registration.addRecipes(AxeBendingRecipeCategory.RECIPE_TYPE, buildAxeBendingRecipes());
        registration.addRecipes(ChiselCarvingRecipeCategory.RECIPE_TYPE, buildChiselCarvingRecipes());
        registration.addRecipes(KnappingRecipeCategory.RECIPE_TYPE, buildKnappingRecipes());

        registration.addIngredientInfo(
                new ItemStack(ModBlocks.GRANITE_ANVIL.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.forgeborneodyssey.anvil_usage.desc")
        );

        registration.addIngredientInfo(
                new ItemStack(ModBlocks.FIRE_PIT_BLOCK.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.forgeborneodyssey.fire_pit_usage.desc")
        );

        registration.addIngredientInfo(
                new ItemStack(ModBlocks.QUERN.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.forgeborneodyssey.quern_usage.desc")
        );

        registration.addIngredientInfo(
                new ItemStack(ModBlocks.DRYING_RACK.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.forgeborneodyssey.drying_rack_usage.desc")
        );

        registration.addIngredientInfo(
                List.of(
                        new ItemStack(ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get())
                ),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.forgeborneodyssey.knapping.desc")
        );

        registration.addIngredientInfo(
                new ItemStack(ModBlocks.PIT_KILN.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.forgeborneodyssey.pit_kiln.desc")
        );

        registration.addIngredientInfo(
                List.of(
                        new ItemStack(ModItems.COPPER_BILLET.get()),
                        new ItemStack(ModItems.SILVER_BILLET.get()),
                        new ItemStack(ModItems.GOLD_BILLET.get())
                ),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.forgeborneodyssey.fire_pit_smelting.desc")
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ItemStack graniteAnvil = new ItemStack(ModBlocks.GRANITE_ANVIL.get());
        ItemStack limestoneAnvil = new ItemStack(ModBlocks.LIMESTONE_ANVIL.get());
        ItemStack polishedGraniteAnvil = new ItemStack(ModBlocks.POLISHED_GRANITE_ANVIL.get());
        ItemStack polishedLimestoneAnvil = new ItemStack(ModBlocks.POLISHED_LIMESTONE_ANVIL.get());

        registration.addRecipeCatalyst(graniteAnvil, ForgingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(limestoneAnvil, ForgingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(polishedGraniteAnvil, ForgingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(polishedLimestoneAnvil, ForgingRecipeCategory.RECIPE_TYPE);

        registration.addRecipeCatalyst(graniteAnvil, OreCrushingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(limestoneAnvil, OreCrushingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(polishedGraniteAnvil, OreCrushingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(polishedLimestoneAnvil, OreCrushingRecipeCategory.RECIPE_TYPE);

        ItemStack firePit = new ItemStack(ModBlocks.FIRE_PIT_BLOCK.get());
        registration.addRecipeCatalyst(firePit, FirePitCookingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(firePit, FirePitSmeltingRecipeCategory.RECIPE_TYPE);

        ItemStack quern = new ItemStack(ModBlocks.QUERN.get());
        registration.addRecipeCatalyst(quern, QuernGrindingRecipeCategory.RECIPE_TYPE);

        ItemStack dryingRack = new ItemStack(ModBlocks.DRYING_RACK.get());
        registration.addRecipeCatalyst(dryingRack, DryingRecipeCategory.RECIPE_TYPE);

        ItemStack pitKiln = new ItemStack(ModBlocks.PIT_KILN.get());
        registration.addRecipeCatalyst(pitKiln, PitKilnFiringRecipeCategory.RECIPE_TYPE);

        registration.addRecipeCatalyst(graniteAnvil, AxeBendingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(limestoneAnvil, AxeBendingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(polishedGraniteAnvil, AxeBendingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(polishedLimestoneAnvil, AxeBendingRecipeCategory.RECIPE_TYPE);

        registration.addRecipeCatalyst(graniteAnvil, ChiselCarvingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(limestoneAnvil, ChiselCarvingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(polishedGraniteAnvil, ChiselCarvingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(polishedLimestoneAnvil, ChiselCarvingRecipeCategory.RECIPE_TYPE);

        registration.addRecipeCatalyst(graniteAnvil, KnappingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(limestoneAnvil, KnappingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(polishedGraniteAnvil, KnappingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(polishedLimestoneAnvil, KnappingRecipeCategory.RECIPE_TYPE);
    }

    private static List<FirePitCookingRecipe> buildFirePitCookingRecipes() {
        List<FirePitCookingRecipe> recipes = new ArrayList<>();
        Map<Item, Item> cookingMap = FoodCookingRecipes.getAllRecipes();
        for (Map.Entry<Item, Item> entry : cookingMap.entrySet()) {
            ItemStack input = new ItemStack(entry.getKey());
            ItemStack output = new ItemStack(entry.getValue());
            int time = FoodCookingRecipes.getCookingTime(input);
            recipes.add(new FirePitCookingRecipe(input, output, time));
        }
        return recipes;
    }

    private static List<QuernGrindingRecipe> buildQuernGrindingRecipes() {
        List<QuernGrindingRecipe> recipes = new ArrayList<>();

        recipes.add(new QuernGrindingRecipe(
                new ItemStack(Items.WHEAT),
                new ItemStack(ModItems.FLOUR.get(), 2)
        ));
        recipes.add(new QuernGrindingRecipe(
                new ItemStack(Items.BONE),
                new ItemStack(Items.BONE_MEAL, 4)
        ));
        recipes.add(new QuernGrindingRecipe(
                new ItemStack(Items.BONE_BLOCK),
                new ItemStack(Items.BONE_MEAL, 9)
        ));
        recipes.add(new QuernGrindingRecipe(
                new ItemStack(Items.FLINT),
                new ItemStack(ModItems.POLISHED_AXE_HEAD.get(), 1)
        ));

        return recipes;
    }

    private static List<DryingRecipe> buildDryingRecipes() {
        List<DryingRecipe> recipes = new ArrayList<>();

        recipes.add(new DryingRecipe(
                new ItemStack(ModItems.GREENWARE_CRUCIBLE.get()),
                new ItemStack(ModItems.GREENWARE_CRUCIBLE.get()),
                6000
        ));
        recipes.add(new DryingRecipe(
                new ItemStack(ModItems.GREENWARE_MOLD.get()),
                new ItemStack(ModItems.GREENWARE_MOLD.get()),
                6000
        ));
        recipes.add(new DryingRecipe(
                new ItemStack(ModItems.GREENWARE_BRICK.get()),
                new ItemStack(ModItems.GREENWARE_BRICK.get()),
                6000
        ));
        recipes.add(new DryingRecipe(
                new ItemStack(ModItems.GREENWARE_BLOWPIPE.get()),
                new ItemStack(ModItems.CERAMIC_BLOWPIPE.get()),
                2000
        ));
        recipes.add(new DryingRecipe(
                new ItemStack(Items.WET_SPONGE),
                new ItemStack(Items.SPONGE),
                2000
        ));
        recipes.add(new DryingRecipe(
                new ItemStack(Items.KELP),
                new ItemStack(Items.DRIED_KELP),
                2000
        ));
        recipes.add(new DryingRecipe(
                new ItemStack(Items.SUGAR_CANE),
                new ItemStack(Items.PAPER),
                2000
        ));
        recipes.add(new DryingRecipe(
                new ItemStack(ModItems.RAWHIDE.get()),
                new ItemStack(ModItems.DRIED_HIDE.get()),
                1200
        ));
        recipes.add(new DryingRecipe(
                new ItemStack(Items.OAK_LEAVES),
                new ItemStack(Items.DEAD_BUSH),
                2000
        ));
        recipes.add(new DryingRecipe(
                new ItemStack(Items.SPRUCE_LEAVES),
                new ItemStack(Items.DEAD_BUSH),
                2000
        ));
        recipes.add(new DryingRecipe(
                new ItemStack(Items.BIRCH_LEAVES),
                new ItemStack(Items.DEAD_BUSH),
                2000
        ));
        recipes.add(new DryingRecipe(
                new ItemStack(Items.JUNGLE_LEAVES),
                new ItemStack(Items.DEAD_BUSH),
                2000
        ));
        recipes.add(new DryingRecipe(
                new ItemStack(Items.ACACIA_LEAVES),
                new ItemStack(Items.DEAD_BUSH),
                2000
        ));
        recipes.add(new DryingRecipe(
                new ItemStack(Items.DARK_OAK_LEAVES),
                new ItemStack(Items.DEAD_BUSH),
                2000
        ));

        return recipes;
    }

    private static List<OreCrushingRecipe> buildOreCrushingRecipes() {
        List<OreCrushingRecipe> recipes = new ArrayList<>();

        Map<Item, Item> oreToGrain = new LinkedHashMap<>();
        oreToGrain.put(ModItems.RAW_CHALCOPYRITE.get(), ModItems.CHALCOPYRITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_BORNITE.get(), ModItems.BORNITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_CHALCOCITE.get(), ModItems.CHALCOCITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_COVELLITE.get(), ModItems.COVELLITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_CUBANITE.get(), ModItems.CUBANITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_MALACHITE.get(), ModItems.MALACHITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_AZURITE.get(), ModItems.AZURITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_CUPRITE.get(), ModItems.CUPRITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_TENORITE.get(), ModItems.TENORITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_CHALCANTHITE.get(), ModItems.CHALCANTHITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_BROCHANTITE.get(), ModItems.BROCHANTITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_MIXED_COPPER.get(), ModItems.MIXED_COPPER_GRAIN.get());
        oreToGrain.put(ModItems.RAW_NATIVE_COPPER.get(), ModItems.NATIVE_COPPER_GRAIN.get());
        oreToGrain.put(ModItems.RAW_TETRAHEDRITE.get(), ModItems.TETRAHEDRITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_TENNANTITE.get(), ModItems.TENNANTITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_TORBERNITE.get(), ModItems.TORBERNITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_CUPROVANADITE.get(), ModItems.CUPROVANADITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_CHRYSOCOLLA.get(), ModItems.CHRYSOCOLLA_GRAIN.get());
        oreToGrain.put(ModItems.RAW_MAGNETITE.get(), ModItems.MAGNETITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_SCHEELITE.get(), ModItems.SCHEELITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_GALENA.get(), ModItems.GALENA_GRAIN.get());
        oreToGrain.put(ModItems.RAW_SPHALERITE.get(), ModItems.SPHALERITE_GRAIN.get());
        oreToGrain.put(ModItems.RAW_MOLYBDENITE.get(), ModItems.MOLYBDENITE_GRAIN.get());

        for (Map.Entry<Item, Item> entry : oreToGrain.entrySet()) {
            recipes.add(new OreCrushingRecipe(
                    new ItemStack(entry.getKey()),
                    new ItemStack(entry.getValue()),
                    new ItemStack(ModItems.TEMPER_GROG.get())
            ));
        }

        return recipes;
    }

    private static List<FirePitSmeltingRecipe> buildFirePitSmeltingRecipes() {
        List<FirePitSmeltingRecipe> recipes = new ArrayList<>();

        recipes.add(new FirePitSmeltingRecipe(
                new ItemStack(ModItems.COPPER_BILLET.get()),
                new ItemStack(ModItems.SOFT_COPPER_BILLET.get()),
                200,
                true
        ));

        return recipes;
    }

    private static List<PitKilnFiringRecipe> buildPitKilnFiringRecipes() {
        List<PitKilnFiringRecipe> recipes = new ArrayList<>();

        recipes.add(new PitKilnFiringRecipe(
                new ItemStack(ModItems.GREENWARE_CRUCIBLE.get()),
                new ItemStack(ModItems.GRAY_CRUCIBLE.get()),
                900,
                -30,
                "jei.forgeborneodyssey.pit_kiln.oxygen_reducing",
                true,
                0
        ));

        recipes.add(new PitKilnFiringRecipe(
                new ItemStack(ModItems.GREENWARE_MOLD.get()),
                new ItemStack(ModItems.RED_MOLD.get()),
                850,
                30,
                "jei.forgeborneodyssey.pit_kiln.oxygen_oxidizing",
                true,
                0
        ));

        recipes.add(new PitKilnFiringRecipe(
                new ItemStack(ModItems.GREENWARE_BRICK.get()),
                new ItemStack(ModItems.FIRED_BRICK.get()),
                1000,
                0,
                "jei.forgeborneodyssey.pit_kiln.oxygen_any",
                true,
                300
        ));

        recipes.add(new PitKilnFiringRecipe(
                new ItemStack(ModItems.GREENWARE_BLOWPIPE.get()),
                new ItemStack(ModItems.CERAMIC_BLOWPIPE.get()),
                950,
                -50,
                "jei.forgeborneodyssey.pit_kiln.oxygen_strong_reducing",
                true,
                0
        ));

        return recipes;
    }

    private static List<AxeBendingRecipe> buildAxeBendingRecipes() {
        List<AxeBendingRecipe> recipes = new ArrayList<>();

        recipes.add(new AxeBendingRecipe(
                new ItemStack(ModItems.COPPER_SHEET.get()),
                new ItemStack(ModItems.COPPER_CURVE.get()),
                "axe",
                true
        ));
        recipes.add(new AxeBendingRecipe(
                new ItemStack(ModItems.SILVER_SHEET.get()),
                new ItemStack(ModItems.SILVER_CURVE.get()),
                "axe",
                true
        ));
        recipes.add(new AxeBendingRecipe(
                new ItemStack(ModItems.GOLD_SHEET.get()),
                new ItemStack(ModItems.GOLD_CURVE.get()),
                "axe",
                true
        ));

        recipes.add(new AxeBendingRecipe(
                new ItemStack(ModItems.COPPER_CURVE.get()),
                new ItemStack(ModItems.COPPER_SLOT.get()),
                "axe",
                true
        ));
        recipes.add(new AxeBendingRecipe(
                new ItemStack(ModItems.SILVER_CURVE.get()),
                new ItemStack(ModItems.SILVER_SLOT.get()),
                "axe",
                true
        ));
        recipes.add(new AxeBendingRecipe(
                new ItemStack(ModItems.GOLD_CURVE.get()),
                new ItemStack(ModItems.GOLD_SLOT.get()),
                "axe",
                true
        ));

        recipes.add(new AxeBendingRecipe(
                new ItemStack(ModItems.COPPER_SLOT.get()),
                new ItemStack(ModItems.COPPER_RING.get()),
                "axe",
                true
        ));

        recipes.add(new AxeBendingRecipe(
                new ItemStack(ModItems.COPPER_RING.get()),
                new ItemStack(ModItems.COPPER_HOOK.get()),
                "axe",
                true
        ));

        recipes.add(new AxeBendingRecipe(
                new ItemStack(ModItems.COPPER_HOOK.get()),
                new ItemStack(ModItems.COPPER_PIN.get()),
                "axe",
                true
        ));

        recipes.add(new AxeBendingRecipe(
                new ItemStack(ModItems.SILVER_SLOT.get()),
                new ItemStack(ModItems.SILVER_PIN.get()),
                "axe",
                true
        ));

        recipes.add(new AxeBendingRecipe(
                new ItemStack(ModItems.GOLD_SLOT.get()),
                new ItemStack(ModItems.GOLD_PIN.get()),
                "axe",
                true
        ));

        return recipes;
    }

    private static List<ChiselCarvingRecipe> buildChiselCarvingRecipes() {
        List<ChiselCarvingRecipe> recipes = new ArrayList<>();

        recipes.add(new ChiselCarvingRecipe(
                new ItemStack(ModItems.COPPER_PIN.get()),
                new ItemStack(ModItems.COPPER_PIN_CHESTPLATE.get()),
                10
        ));
        recipes.add(new ChiselCarvingRecipe(
                new ItemStack(ModItems.SILVER_PIN.get()),
                new ItemStack(ModItems.SILVER_PIN_CHESTPLATE.get()),
                10
        ));
        recipes.add(new ChiselCarvingRecipe(
                new ItemStack(ModItems.GOLD_PIN.get()),
                new ItemStack(ModItems.GOLD_PIN_CHESTPLATE.get()),
                10
        ));

        recipes.add(new ChiselCarvingRecipe(
                new ItemStack(ModItems.COPPER_SLOT.get()),
                new ItemStack(ModItems.COPPER_KNIFE.get()),
                6
        ));
        recipes.add(new ChiselCarvingRecipe(
                new ItemStack(ModItems.SILVER_SLOT.get()),
                new ItemStack(ModItems.SILVER_KNIFE.get()),
                6
        ));
        recipes.add(new ChiselCarvingRecipe(
                new ItemStack(ModItems.GOLD_SLOT.get()),
                new ItemStack(ModItems.GOLD_KNIFE.get()),
                6
        ));

        return recipes;
    }

    private static List<KnappingRecipe> buildKnappingRecipes() {
        List<KnappingRecipe> recipes = new ArrayList<>();

        List<KnappingRecipe.KnappingOutputEntry> retouchOutputs = new ArrayList<>();
        retouchOutputs.add(new KnappingRecipe.KnappingOutputEntry(
                new ItemStack(ModItems.FLINT_KNIFE_HEAD.get()), 40, 100));
        retouchOutputs.add(new KnappingRecipe.KnappingOutputEntry(
                new ItemStack(ModItems.FLINT_ARROWHEAD.get(), 2), 25, 100));
        retouchOutputs.add(new KnappingRecipe.KnappingOutputEntry(
                new ItemStack(ModItems.FLINT_SPEARHEAD.get()), 15, 100));
        retouchOutputs.add(new KnappingRecipe.KnappingOutputEntry(
                new ItemStack(ModItems.FLINT_SHOVEL_HEAD.get()), 10, 100));
        retouchOutputs.add(new KnappingRecipe.KnappingOutputEntry(
                new ItemStack(ModItems.FLINT_SICKLE_HEAD.get()), 5, 100));
        retouchOutputs.add(new KnappingRecipe.KnappingOutputEntry(
                new ItemStack(ModItems.STONE_AXE_HEAD.get()), 5, 100));

        recipes.add(new KnappingRecipe(
                new ItemStack(ModItems.FLINT_FLAKE.get()),
                retouchOutputs,
                "jei.forgeborneodyssey.knapping.flint_flake_desc",
                3
        ));

        List<KnappingRecipe.KnappingOutputEntry> coreShapingOutputs = new ArrayList<>();
        coreShapingOutputs.add(new KnappingRecipe.KnappingOutputEntry(
                new ItemStack(ModItems.STONE_AXE_HEAD.get()), 40, 100));
        coreShapingOutputs.add(new KnappingRecipe.KnappingOutputEntry(
                new ItemStack(ModItems.FLINT_SHOVEL_HEAD.get()), 30, 100));
        coreShapingOutputs.add(new KnappingRecipe.KnappingOutputEntry(
                new ItemStack(ModItems.STONE_HOE_HEAD.get()), 30, 100));

        recipes.add(new KnappingRecipe(
                new ItemStack(ModItems.STONE_CORE.get()),
                coreShapingOutputs,
                "jei.forgeborneodyssey.knapping.stone_core_desc",
                4
        ));

        return recipes;
    }
}