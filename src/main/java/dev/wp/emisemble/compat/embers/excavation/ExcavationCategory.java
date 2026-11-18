package dev.wp.emisemble.compat.embers.excavation;

import com.rekindled.embers.RegistryManager;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.embers.boring.BoringCategory;
import dev.wp.emisemble.compat.embers.boring.BoringPage;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

/** {@code embers:excavation}: the same bore page over the buckets that change what a dig returns. */
public final class ExcavationCategory {
    public static final String NAMESPACE = BoringCategory.NAMESPACE;
    public static final ResourceLocation ID = EmiRecipes.categoryId(RegistryManager.EXCAVATION.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RegistryManager.EXCAVATION_BUCKETS_ITEM.get()), "embers.jei.recipe.excavation");

    private ExcavationCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.EMBER_BORE_ITEM.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.EXCAVATION_BUCKETS_ITEM.get()));

        int added = EmiRecipes.addAll(registry, RegistryManager.EXCAVATION.get(),
                holder -> new BoringPage(CATEGORY, holder));
        Emisemble.LOGGER.info("embers: {} excavation recipes registered", added);
        return added;
    }
}
