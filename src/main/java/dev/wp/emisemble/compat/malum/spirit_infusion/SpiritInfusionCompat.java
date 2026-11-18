package dev.wp.emisemble.compat.malum.spirit_infusion;

import com.sammy.malum.common.recipe.SpiritInfusionRecipe;
import com.sammy.malum.registry.common.item.MalumItems;
import com.sammy.malum.registry.common.recipe.MalumRecipeTypes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

/** {@code malum:spirit_infusion}. */
public final class SpiritInfusionCompat {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("malum", "textures/gui/spirit_infusion_jei.png");
    // Both ingredient columns are framed from the codex socket sheet, not from the page art, and the run is redrawn per
    // recipe because its length and vertical origin both move.
    static final ResourceLocation SOCKETS =
            ResourceLocation.fromNamespaceAndPath("malum", "textures/gui/book/entry_elements/item_sockets.png");
    static final int SHEET = 256;
    static final int SOCKET_SHEET = 64;
    // The host's 142x185 art is blitted from u=1 as 140x183, and the page is that less the 4px EMI insets each side.
    static final int WIDTH = 132;
    static final int HEIGHT = 175;

    private static final RecipeType<SpiritInfusionRecipe> TYPE = MalumRecipeTypes.SPIRIT_INFUSION.get();

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(TYPE),
            EmiStack.of(MalumItems.SPIRIT_ALTAR.get()),
            "malum.jei.spirit_infusion");

    private SpiritInfusionCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(MalumItems.SPIRIT_ALTAR.get()));

        int added = EmiRecipes.addAll(registry, TYPE, SpiritInfusionEmiRecipe::new);

        Emisemble.LOGGER.info("malum: {} spirit infusion recipes registered", added);
        return added;
    }
}
