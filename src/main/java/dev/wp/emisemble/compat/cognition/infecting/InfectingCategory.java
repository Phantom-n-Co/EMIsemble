package dev.wp.emisemble.compat.cognition.infecting;

import com.cyanogen.cognition.registries.RegisterItems;
import com.cyanogen.cognition.registries.RegisterRecipes;
import com.cyanogen.cognition.utils.RecipeUtils;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** The bibliophage infecting blocks from a neighbouring agar. */
public final class InfectingCategory {
    static final int WIDTH = 136;
    static final int HEIGHT = 66;
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("cognition", "textures/gui/recipes/infecting_jei.png");
    static final EmiStack BIBLIOPHAGE = EmiStack.of(RegisterItems.BIBLIOPHAGE.get());
    /** Anything the host lets the bibliophage work from, in the host's own order. */
    static final List<EmiStack> CATALYSTS = RecipeUtils.getCatalysts().stream().map(EmiStack::of).toList();

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(RegisterRecipes.INFECTING_TYPE.get()), BIBLIOPHAGE, "jei.cognition.infecting.title");

    private InfectingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        for (EmiStack catalyst : CATALYSTS) {
            registry.addWorkstation(CATEGORY, catalyst);
        }
        return EmiRecipes.addAll(registry, RegisterRecipes.INFECTING_TYPE.get(),
                holder -> new InfectingEmiRecipe(CATEGORY, holder));
    }
}
