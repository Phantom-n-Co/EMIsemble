package dev.wp.emisemble.compat.embers.mixing;

import com.rekindled.embers.RegistryManager;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

/** {@code embers:mixing}: up to four fluids combined by the mixer centrifuge. */
public final class MixingCategory {
    public static final String NAMESPACE = "embers";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RegistryManager.MIXING.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RegistryManager.MIXER_CENTRIFUGE_ITEM.get()), "embers.jei.recipe.mixing");

    private MixingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.MIXER_CENTRIFUGE_ITEM.get()));

        int added = EmiRecipes.addAll(registry, RegistryManager.MIXING.get(), MixingPage::new);
        Emisemble.LOGGER.info("embers: {} mixing recipes registered", added);
        return added;
    }
}
