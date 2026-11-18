package dev.wp.emisemble.compat.immersiveengineering.blueprint;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersiveengineering.IeIds;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** {@code immersiveengineering:blueprint}: the engineer's workbench and its automatic counterpart. */
public final class BlueprintCategory {
    static final int WIDTH = 176;
    static final int HEIGHT = 54;

    static final ResourceLocation TEXTURE = IEApi.ieLoc("textures/gui/workbench.png");

    private static final EmiStack ICON = EmiStack.of(new ItemStack(IEBlocks.WoodenDevices.WORKBENCH));
    private static final EmiStack AUTO_WORKBENCH = EmiStack.of(IEMultiblockLogic.AUTO_WORKBENCH.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.BLUEPRINT.get()), ICON, "block.immersiveengineering.workbench");

    private BlueprintCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
            // The assembler crafts these recipes too, so the host lists both machines as catalysts.
            registry.addWorkstation(CATEGORY, AUTO_WORKBENCH);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the blueprint category", t);
            return 0;
        }

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            Emisemble.LOGGER.warn("immersiveengineering: no client level, skipping the blueprint recipes");
            return 0;
        }

        // RECIPES, not the recipe manager: the host reads the same cache, so the pages match and keep their ids.
        List<RecipeHolder<BlueprintCraftingRecipe>> recipes = new ArrayList<>(BlueprintCraftingRecipe.RECIPES.getRecipes(level));
        // The host sorts by blueprint category, so the pages of one blueprint stay together and in order.
        recipes.sort(Comparator.<RecipeHolder<BlueprintCraftingRecipe>, String>comparing(
                holder -> holder.value().blueprintCategory).thenComparing(RecipeHolder::id));

        registerBlueprints(registry, recipes);

        int added = 0;
        for (RecipeHolder<BlueprintCraftingRecipe> holder : recipes) {
            try {
                if (EmiRecipes.add(registry, new BlueprintEmiRecipe(CATEGORY, IeIds.of(holder), holder.value()))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("immersiveengineering: skipping blueprint recipe {}", holder.id(), t);
            }
        }
        Emisemble.LOGGER.info("immersiveengineering: {} blueprint recipes", added);
        return added;
    }

    /**
     * Every blueprint is the same item carrying its category as a component, so EMI needs both halves of the subtype
     * work the host does: an identity that reads the component, and one indexed stack per category a page can name.
     */
    private static void registerBlueprints(EmiRegistry registry, List<RecipeHolder<BlueprintCraftingRecipe>> recipes) {
        Set<String> categories = new LinkedHashSet<>();
        for (RecipeHolder<BlueprintCraftingRecipe> holder : recipes) {
            categories.add(holder.value().blueprintCategory);
        }
        try {
            registry.setDefaultComparison(IEItems.Misc.BLUEPRINT.get(),
                    Comparison.compareData(stack -> stack.get(IEApiDataComponents.BLUEPRINT_TYPE.get())));
            // The typed blueprints are creative entries, so EMI has already indexed them; adding again doubles the
            // index. Same guard the purity gems and potion charms use.
            List<EmiStack> indexed = EmiApi.getIndexStacks();
            int added = 0;
            for (String category : categories) {
                EmiStack blueprint = EmiStack.of(BlueprintCraftingRecipe.getTypedBlueprint(category));
                if (indexed.stream().anyMatch(stack -> stack.isEqual(blueprint))) {
                    continue;
                }
                registry.addEmiStack(blueprint);
                added++;
            }
            Emisemble.LOGGER.info("immersiveengineering: {} blueprint variants added, {} already indexed",
                    added, categories.size() - added);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not index the blueprint variants", t);
        }
    }
}
