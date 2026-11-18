package dev.wp.emisemble.compat.immersiveengineering.blastfurnace;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersiveengineering.IeIds;
import dev.wp.emisemble.compat.immersiveengineering.IeRecipes;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

/** {@code immersiveengineering:blast_furnace} and its fuel list, a separate recipe type. */
public final class BlastFurnaceCategory {
    static final ResourceLocation TEXTURE = IEApi.ieLoc("textures/gui/blast_furnace.png");
    static final ResourceLocation FURNACE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");

    private static final EmiStack ICON = EmiStack.of(IEMultiblockLogic.BLAST_FURNACE.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.BLAST_FURNACE.get()), ICON, "gui.immersiveengineering.blastFurnace");

/**
 * JEI gives this one a composite icon (the multiblock with a flame on it); an EMI category takes a single stack.
 */
    public static final EmiRecipeCategory FUEL_CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.BLAST_FUEL.get()), ICON, "gui.immersiveengineering.blastFurnace.fuel");

    private BlastFurnaceCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addCategory(FUEL_CATEGORY);
            EmiStack advanced = EmiStack.of(IEMultiblockLogic.ADV_BLAST_FURNACE.iconStack());
            for (EmiRecipeCategory category : new EmiRecipeCategory[]{CATEGORY, FUEL_CATEGORY}) {
                registry.addWorkstation(category, ICON);
                registry.addWorkstation(category, advanced);
            }
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the blast furnace categories", t);
            return 0;
        }

        int added = IeRecipes.addAll(registry, BlastFurnaceRecipe.RECIPES,
                holder -> new BlastFurnaceEmiRecipe(CATEGORY, IeIds.of(holder), holder.value()));
        Emisemble.LOGGER.info("immersiveengineering: {} blast furnace recipes", added);

        int fuels = IeRecipes.addAll(registry, BlastFurnaceFuel.RECIPES,
                holder -> new BlastFuelEmiRecipe(FUEL_CATEGORY, IeIds.of(holder), holder.value()));
        Emisemble.LOGGER.info("immersiveengineering: {} blast furnace fuel recipes", fuels);

        return added + fuels;
    }
}
