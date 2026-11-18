package dev.wp.emisemble.compat.cognition.metamorphosis;

import com.cyanogen.cognition.registries.RegisterItems;
import com.cyanogen.cognition.registries.RegisterRecipes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

/** The molecular metamorpher, which also lists the fake name formatting page by hand. */
public final class MetamorphosisCategory {
    static final int WIDTH = 176;
    static final int HEIGHT = 87;
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("cognition", "textures/gui/recipes/molecular_metamorpher_jei.png");
    static final EmiStack METAMORPHER = EmiStack.of(RegisterItems.MOLECULAR_METAMORPHER_ITEM.get());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(RegisterRecipes.MOLECULAR_METAMORPHER_TYPE.get()), METAMORPHER,
            "title.cognition.molecular_metamorpher");

    private MetamorphosisCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, METAMORPHER);
        int added = EmiRecipes.addAll(registry, RegisterRecipes.MOLECULAR_METAMORPHER_TYPE.get(),
                holder -> MetamorphosisEmiRecipe.of(CATEGORY, holder));
        if (EmiRecipes.add(registry, MetamorphosisEmiRecipe.nameFormatting(CATEGORY))) {
            added++;
        }
        return added;
    }
}
