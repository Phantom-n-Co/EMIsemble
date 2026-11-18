package dev.wp.emisemble.compat.hexerei.woodcutter;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.wp.emisemble.porting.TranslatedCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import net.joefoxe.hexerei.block.ModBlocks;
import net.joefoxe.hexerei.data.recipes.WoodcutterRecipe;
import net.joefoxe.hexerei.data.recipes.WoodcutterRecipes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** {@code hexerei:woodcutter}, including the pages the host generates per wood set. */
public final class WoodcutterCategory {
    public static final String NAMESPACE = "hexerei";
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "woodcutter");
    public static final EmiRecipeCategory CATEGORY =
            new TranslatedCategory(ID, EmiStack.of(ModBlocks.WILLOW_WOODCUTTER.get()), "hexerei.container.woodcutter");

    private WoodcutterCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);

        registry.addWorkstation(CATEGORY, EmiStack.of(ModBlocks.WILLOW_WOODCUTTER.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(ModBlocks.MAHOGANY_WOODCUTTER.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(ModBlocks.WITCH_HAZEL_WOODCUTTER.get()));

        int fromData = EmiRecipes.addAll(registry, WoodcutterRecipe.Type.INSTANCE,
                holder -> new WoodcutterEmiRecipe(CATEGORY, holder));
        int generated = addGenerated(registry);

        Emisemble.LOGGER.info("{}: {} woodcutter recipes registered ({} generated)",
                NAMESPACE, fromData + generated, generated);
        return fromData + generated;
    }

    private static int addGenerated(EmiRegistry registry) {
        List<WoodcutterRecipe> generated = WoodcutterRecipes.ALL;
        if (generated == null) {
            return 0;
        }

        int added = 0;
        for (int i = 0; i < generated.size(); i++) {
            int index = i;
            try {
                if (EmiRecipes.add(registry, new WoodcutterEmiRecipe(CATEGORY, idOf(index), generated.get(index)))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping {} generated woodcutter page {}", NAMESPACE, index, t);
            }
        }
        return added;
    }

    private static ResourceLocation idOf(int index) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, "/woodcutter/" + index);
    }
}
