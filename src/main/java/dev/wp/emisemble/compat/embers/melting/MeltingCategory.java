package dev.wp.emisemble.compat.embers.melting;

import com.rekindled.embers.RegistryManager;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

/** {@code embers:melting}: what a melter turns an item into. */
public final class MeltingCategory {
    public static final String NAMESPACE = "embers";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RegistryManager.MELTING.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RegistryManager.MELTER_ITEM.get()), "embers.jei.recipe.melting");

    private MeltingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.MELTER_ITEM.get()));

        int added = EmiRecipes.addAll(registry, RegistryManager.MELTING.get(),
                holder -> MeltingPage.melting(CATEGORY, holder));
        Emisemble.LOGGER.info("embers: {} melting recipes registered", added);
        return added;
    }
}
