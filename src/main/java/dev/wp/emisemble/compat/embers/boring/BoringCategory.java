package dev.wp.emisemble.compat.embers.boring;

import com.rekindled.embers.RegistryManager;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

/** {@code embers:boring}: what the ember bore digs out of plain stone. */
public final class BoringCategory {
    public static final String NAMESPACE = "embers";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RegistryManager.BORING.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RegistryManager.EMBER_BORE_ITEM.get()), "embers.jei.recipe.boring");

    private BoringCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.EMBER_BORE_ITEM.get()));

        int added = EmiRecipes.addAll(registry, RegistryManager.BORING.get(), holder -> new BoringPage(CATEGORY, holder));
        Emisemble.LOGGER.info("embers: {} boring recipes registered", added);
        return added;
    }
}
