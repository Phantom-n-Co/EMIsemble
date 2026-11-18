package dev.wp.emisemble.compat.embers.alchemy;

import com.rekindled.embers.RegistryManager;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.embers.EmbersCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

/** {@code embers:alchemy}: the centre item placed among aspect-tagged inputs on the pedestal. */
public final class AlchemyCategory {
    public static final String NAMESPACE = "embers";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RegistryManager.ALCHEMY.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RegistryManager.ALCHEMY_TABLET_ITEM.get()), "embers.jei.recipe.alchemy");

    private AlchemyCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.ALCHEMY_PEDESTAL_ITEM.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.ALCHEMY_TABLET_ITEM.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.BEAM_CANNON_ITEM.get()));

        int added = EmbersCompat.addSplitRecipes(registry, RegistryManager.ALCHEMY.get(),
                holder -> new AlchemyPage(CATEGORY, holder));
        Emisemble.LOGGER.info("embers: {} alchemy recipes registered", added);
        return added;
    }
}
