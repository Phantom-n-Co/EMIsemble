package dev.wp.emisemble.compat.immersiveengineering.cokeoven;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
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
import net.neoforged.neoforge.fluids.FluidType;

/**
 * {@code immersiveengineering:coke_oven}. The page is the coke oven gui's 123x55 body, so the item slots are
 * frameless; the creosote tank is a separate 16x47 overlay in the same sheet, drawn on top of EMI's tank fill.
 */
public final class CokeOvenCategory {
    static final int WIDTH = 123;
    static final int HEIGHT = 55;
    static final int PAGE_U = 26;
    static final int PAGE_V = 16;
    static final ResourceLocation TEXTURE = IEApi.ieLoc("textures/gui/coke_oven.png");

    static final int TANK_X = 103;
    static final int TANK_Y = 4;
    static final int TANK_WIDTH = 16;
    static final int TANK_HEIGHT = 47;
    static final int TANK_U = 178;
    static final int TANK_V = 33;
    /** The tank the coke oven block holds: twelve buckets, so a full creosote batch reads as a third of it. */
    static final int TANK_CAPACITY = 12 * FluidType.BUCKET_VOLUME;

    private static final EmiStack ICON = EmiStack.of(IEMultiblockLogic.COKE_OVEN.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.COKE_OVEN.get()), ICON, "block.immersiveengineering.coke_oven");

    private CokeOvenCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the coke oven category", t);
            return 0;
        }
        int added = IeRecipes.addAll(registry, CokeOvenRecipe.RECIPES,
                holder -> new CokeOvenEmiRecipe(CATEGORY, IeIds.of(holder), holder.value()));
        Emisemble.LOGGER.info("immersiveengineering: {} coke oven recipes", added);
        return added;
    }
}
