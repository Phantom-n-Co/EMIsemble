package dev.wp.emisemble.compat.embers.stamping;

import com.rekindled.embers.RegistryManager;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

/** {@code embers:stamping}: a stamp pressed onto an item, optionally over a fluid. */
public final class StampingCategory {
    public static final String NAMESPACE = "embers";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RegistryManager.STAMPING.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RegistryManager.STAMPER_ITEM.get()), "embers.jei.recipe.stamping");

    private StampingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.STAMPER_ITEM.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.STAMP_BASE_ITEM.get()));

        int added = EmiRecipes.addAll(registry, RegistryManager.STAMPING.get(), StampingPage::new);
        Emisemble.LOGGER.info("embers: {} stamping recipes registered", added);
        return added;
    }
}
