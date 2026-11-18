package dev.wp.emisemble.compat.malum.spirit_repair;

import com.sammy.malum.common.recipe.spirit_repair.SpiritRepairRecipe;
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

/** {@code malum:spirit_repair}. */
public final class SpiritRepairCompat {
    // The page art is opaque paper with its three item sockets painted in, so only the spirit row needs frames.
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("malum", "textures/gui/spirit_repair_jei.png");
    static final ResourceLocation SOCKETS =
            ResourceLocation.fromNamespaceAndPath("malum", "textures/gui/book/entry_elements/item_sockets.png");
    static final int SHEET = 256;
    static final int SOCKET_SHEET = 64;
    // The host's 142x185 art is blitted from u=1 as 140x183, and the page is that less the 4px EMI insets each side.
    static final int WIDTH = 132;
    static final int HEIGHT = 175;

    private static final RecipeType<SpiritRepairRecipe> TYPE = MalumRecipeTypes.SPIRIT_REPAIR.get();

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(TYPE),
            EmiStack.of(MalumItems.REPAIR_PYLON.get()),
            "malum.jei.spirit_repair");

    private SpiritRepairCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(MalumItems.REPAIR_PYLON.get()));

        int added = 0;
        int skipped = 0;
        for (RecipeHolder<SpiritRepairRecipe> holder : registry.getRecipeManager().getAllRecipesFor(TYPE)) {
            try {
                // The host hides an itemless recipe from JEI, where both of its item slots would be empty.
                if (holder.value().validItems.isEmpty()) {
                    skipped++;
                    continue;
                }
                if (EmiRecipes.add(registry, new SpiritRepairEmiRecipe(holder))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping malum spirit repair recipe {}", holder.id(), t);
            }
        }
        Emisemble.LOGGER.info("malum: {} spirit repair recipes registered, {} without a valid item skipped",
                added, skipped);
        return added;
    }
}
