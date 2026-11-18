package dev.wp.emisemble.compat.embers.melting_bonus;

import com.rekindled.embers.RegistryManager;
import com.rekindled.embers.recipe.IMeltingRecipe;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.embers.melting.MeltingCategory;
import dev.wp.emisemble.compat.embers.melting.MeltingPage;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.world.item.crafting.RecipeHolder;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

/** The separator's page: the melting type shown again, with the bonus fluid the melter alone does not. */
public final class MeltingBonusCategory {
    // the host invents a second JEI uid over the melting type, so this id is its own and not the type's key
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MeltingCategory.NAMESPACE, "melting_bonus");
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RegistryManager.GEOLOGIC_SEPARATOR_ITEM.get()), "embers.jei.recipe.geologic_separator");

    private MeltingBonusCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.MELTER_ITEM.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.GEOLOGIC_SEPARATOR_ITEM.get()));

        // Only the recipes that actually carry a bonus: the host filters on the same check for this uid.
        int added = 0;
        for (RecipeHolder<IMeltingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(RegistryManager.MELTING.get())) {
            if (holder.value().getBonus().isEmpty()) {
                continue;
            }
            added += EmiRecipes.add(registry, MeltingPage.bonus(CATEGORY, holder)) ? 1 : 0;
        }
        Emisemble.LOGGER.info("embers: {} melting bonus recipes registered", added);
        return added;
    }
}
