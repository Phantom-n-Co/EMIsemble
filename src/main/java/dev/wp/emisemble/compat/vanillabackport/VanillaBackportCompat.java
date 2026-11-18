package dev.wp.emisemble.compat.vanillabackport;

import com.blackgear.vanillabackport.common.api.modules.bundle_ui.BundleColoring;
import com.blackgear.vanillabackport.common.api.modules.bundle_ui.BundleFeatures;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * VanillaBackport ships no category: its only viewer content is the dyed bundle combos its JEI plugin invents, because
 * the bundle-colouring recipe itself is a CustomRecipe and therefore invisible to both viewers.
 */
public final class VanillaBackportCompat {
    private VanillaBackportCompat() {}

    public static int register(EmiRegistry registry) {
        // Same gate as the host: without a bundle-colouring recipe in the book, there is nothing to dye from.
        boolean dyeable = registry.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream()
                .anyMatch(VanillaBackportCompat::isBundleColoring);
        if (!dyeable) {
            return 0;
        }

        Map<DyeColor, List<Item>> dyes = dyesByColor();
        List<Item> bundles = List.copyOf(BundleFeatures.BUNDLES_BY_DYE.values());
        int recipes = 0;
        for (DyeColor color : DyeColor.values()) {
            Item bundle = BundleFeatures.getByColor(color);
            List<Item> colorDyes = dyes.get(color);
            if (bundle == null || colorDyes == null || colorDyes.isEmpty()) {
                continue;
            }
            // The source cannot be the bundle being made, so slot one holds every other bundle the host registered.
            List<EmiStack> sources = bundles.stream()
                    .filter(item -> item != bundle)
                    .distinct()
                    .map(EmiStack::of)
                    .toList();
            if (sources.isEmpty()) {
                continue;
            }
            List<EmiStack> dyeStacks = colorDyes.stream().map(EmiStack::of).toList();
            if (EmiRecipes.add(registry, new EmiCraftingRecipe(
                    List.<EmiIngredient>of(new ListEmiIngredient(sources, 1), new ListEmiIngredient(dyeStacks, 1)),
                    EmiStack.of(bundle), pageId(bundle), true))) {
                recipes++;
            }
        }
        Emisemble.LOGGER.info("vanillabackport: {} dyed bundle recipes registered", recipes);
        return recipes;
    }

    private static boolean isBundleColoring(RecipeHolder<CraftingRecipe> holder) {
        return holder.value() instanceof BundleColoring;
    }

    private static Map<DyeColor, List<Item>> dyesByColor() {
        Map<DyeColor, List<Item>> dyes = new EnumMap<>(DyeColor.class);
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof DyeItem dye) {
                dyes.computeIfAbsent(dye.getDyeColor(), color -> new ArrayList<>()).add(item);
            }
        }
        return dyes;
    }

    // The host's own synthetic id, so the page keeps the key JEI gave it: namespace:/bundle_path.
    private static ResourceLocation pageId(Item bundle) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(bundle);
        return ResourceLocation.fromNamespaceAndPath(key.getNamespace(), "/" + key.getPath());
    }
}
