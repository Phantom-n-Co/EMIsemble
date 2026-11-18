package dev.wp.emisemble.compat.mythsandlegends.stone_sanctum;

import com.ovinter.mythsandlegends.registry.MLBlocks;
import com.ovinter.mythsandlegends.registry.MLItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.mythsandlegends.MythsAndLegendsCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** {@code mythsandlegends:stone_sanctum}. */
public final class StoneSanctumCategory {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mythsandlegends", "textures/gui/container/stone_sanctum.png");
    /** The page is the top of a vanilla-sized container sheet. */
    static final int SHEET = 256;
    // The page is the art less the 4px EMI insets the widgets by, so the art runs to the card edge.
    static final int WIDTH = 168;
    static final int HEIGHT = 72;

    private static final EmiStack TABLE = EmiStack.of(new ItemStack(MLBlocks.STONE_SANCTUM_TABLE.get()));

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            MythsAndLegendsCompat.categoryId("stone_sanctum"), TABLE, "block.mythsandlegends.stone_sanctum_table");

    private StoneSanctumCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, TABLE);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("mythsandlegends: could not register the stone sanctum category", t);
            return 0;
        }
        // The ritual has no recipe type: the host's plugin builds its only page by hand, so this one is synthetic.
        StoneSanctumEmiRecipe recipe = new StoneSanctumEmiRecipe(CATEGORY,
                ResourceLocation.fromNamespaceAndPath("mythsandlegends", "/stone_sanctum"),
                new ItemStack(MLItems.CURSED_CORE.get()),
                List.of(new ItemStack(MLItems.GARGOYLE_HEAD.get()), new ItemStack(MLItems.GARGOYLE_TORSO.get()),
                        new ItemStack(MLItems.GARGOYLE_ARM.get()), new ItemStack(MLItems.GARGOYLE_LEG.get()),
                        new ItemStack(MLItems.GARGOYLE_WING.get())),
                new ItemStack(MLItems.GARGOYLE_EFFIGY.get()));
        int added = EmiRecipes.add(registry, recipe) ? 1 : 0;
        Emisemble.LOGGER.info("mythsandlegends: {} stone sanctum recipes", added);
        return added;
    }
}
