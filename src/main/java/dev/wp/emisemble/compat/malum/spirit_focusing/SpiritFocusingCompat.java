package dev.wp.emisemble.compat.malum.spirit_focusing;

import com.sammy.malum.common.recipe.SpiritFocusingRecipe;
import com.sammy.malum.registry.common.item.MalumItems;
import com.sammy.malum.registry.common.recipe.MalumRecipeTypes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

/** {@code malum:spirit_focusing}. */
public final class SpiritFocusingCompat {
    // The page art is opaque paper with the input and result sockets painted in, so only the spirit row needs frames.
    static final ResourceLocation PAGE =
            ResourceLocation.fromNamespaceAndPath("malum", "textures/gui/spirit_focusing_jei.png");
    static final ResourceLocation SOCKETS =
            ResourceLocation.fromNamespaceAndPath("malum", "textures/gui/book/entry_elements/item_sockets.png");
    static final int SHEET = 256;
    static final int SOCKET_SHEET = 64;
    // The host's 142x185 art is blitted from u=1 as 140x183, and the page is that less the 4px EMI insets each side.
    static final int WIDTH = 132;
    static final int HEIGHT = 175;

    private static final RecipeType<SpiritFocusingRecipe> TYPE = MalumRecipeTypes.SPIRIT_FOCUSING.get();

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(TYPE),
            EmiStack.of(MalumItems.SPIRIT_CRUCIBLE.get()),
            "malum.jei.spirit_focusing");

    private SpiritFocusingCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(MalumItems.SPIRIT_CRUCIBLE.get()));

        int added = 0;
        int skipped = 0;
        for (RecipeHolder<SpiritFocusingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(TYPE)) {
            try {
                // The host hides a resultless recipe from JEI, where it would be an empty output socket.
                if (holder.value().output.isEmpty()) {
                    skipped++;
                    continue;
                }
                if (EmiRecipes.add(registry, new SpiritFocusingEmiRecipe(holder))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping malum spirit focusing recipe {}", holder.id(), t);
            }
        }
        Emisemble.LOGGER.info("malum: {} spirit focusing recipes registered, {} without a result skipped",
                added, skipped);
        return added;
    }
}
