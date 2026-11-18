package dev.wp.emisemble.compat.cognition.filling;

import com.cyanogen.cognition.registries.RegisterItems;
import com.cyanogen.cognition.registries.RegisterRecipes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

/** The experience fountain spending its cognitium to fill an item. */
public final class FillingCategory {
    static final int WIDTH = 136;
    static final int HEIGHT = 66;
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("cognition", "textures/gui/recipes/filling_jei.png");
    static final EmiStack FOUNTAIN = EmiStack.of(RegisterItems.EXPERIENCE_FOUNTAIN_ITEM.get());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(RegisterRecipes.FILLING_TYPE.get()), FOUNTAIN, "jei.cognition.filling.title");

    private FillingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, FOUNTAIN);
        return EmiRecipes.addAll(registry, RegisterRecipes.FILLING_TYPE.get(),
                holder -> new FillingEmiRecipe(CATEGORY, holder));
    }
}
