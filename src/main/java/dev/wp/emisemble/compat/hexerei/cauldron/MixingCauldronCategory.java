package dev.wp.emisemble.compat.hexerei.cauldron;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.joefoxe.hexerei.block.ModBlocks;
import net.joefoxe.hexerei.data.recipes.MixingCauldronRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * {@code hexerei:mixingcauldron}: items dropped into a cauldron of fluid, over a heat source and under a moon
 * phase.
 */
public final class MixingCauldronCategory {
    public static final String NAMESPACE = "hexerei";
    public static final ResourceLocation ID = EmiRecipes.categoryId(MixingCauldronRecipe.Type.INSTANCE);
    // The host titles this category with a literal used as a translation key, so both viewers show "Item Mixing".
    private static final Component TITLE = Component.translatable("Item Mixing");

    public static final EmiRecipeCategory CATEGORY =
            new TranslatedCategory(ID, EmiStack.of(ModBlocks.MIXING_CAULDRON.get()), TITLE);

    private MixingCauldronCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, EmiStack.of(ModBlocks.MIXING_CAULDRON.get()));
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("Could not register hexerei category {}", ID, t);
        }

        int added = EmiRecipes.addAll(registry, MixingCauldronRecipe.Type.INSTANCE,
                holder -> new MixingCauldronEmiRecipe(CATEGORY, holder.id(), holder.value()));

        Emisemble.LOGGER.info("{}: {} mixingcauldron recipes registered", NAMESPACE, added);
        return added;
    }
}
