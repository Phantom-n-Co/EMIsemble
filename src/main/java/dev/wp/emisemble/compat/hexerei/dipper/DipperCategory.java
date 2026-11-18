package dev.wp.emisemble.compat.hexerei.dipper;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.wp.emisemble.porting.TranslatedCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import net.joefoxe.hexerei.block.ModBlocks;
import net.joefoxe.hexerei.data.recipes.DipperRecipe;
import net.joefoxe.hexerei.item.ModItems;
import net.minecraft.resources.ResourceLocation;

/** {@code hexerei:dipper}. */
public final class DipperCategory {
    public static final String NAMESPACE = "hexerei";
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "dipper");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/dipper_jei.png");
    public static final EmiRecipeCategory CATEGORY =
            new TranslatedCategory(ID, EmiStack.of(ModBlocks.CANDLE_DIPPER.get()), "block.hexerei.candle_dipper");

    private DipperCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);

            registry.addWorkstation(CATEGORY, EmiStack.of(ModItems.MIXING_CAULDRON.get()));
            registry.addWorkstation(CATEGORY, EmiStack.of(ModBlocks.CANDLE_DIPPER.get()));
        } catch (Throwable t) {
            Emisemble.LOGGER.error("hexerei: could not register the dipper category", t);
            return 0;
        }

        int added = EmiRecipes.addAll(registry, DipperRecipe.Type.INSTANCE,
                holder -> new DipperEmiRecipe(CATEGORY, holder.id(), holder.value()));

        Emisemble.LOGGER.info("{}: {} dipper recipes registered", NAMESPACE, added);
        return added;
    }
}
