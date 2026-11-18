package dev.wp.emisemble.compat.hexerei.keychain_apply;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.wp.emisemble.porting.TranslatedCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import net.joefoxe.hexerei.data.recipes.KeychainRecipe;
import net.joefoxe.hexerei.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

/** {@code hexerei:keychain_apply}: a keychain and the item attached to it. */
public final class KeychainApplyCategory {
    public static final String NAMESPACE = "hexerei";
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "keychain_apply");

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/add_to_candle_gui_jei.png");

    public static final EmiRecipeCategory CATEGORY =
            new TranslatedCategory(ID, EmiStack.of(ModItems.BROOM_KEYCHAIN.get()), "Keychain Attach");

    private KeychainApplyCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);

        int added = 0;
        for (RecipeHolder<CraftingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            if (!(holder.value() instanceof KeychainRecipe)) {
                continue;
            }

            try {
                if (EmiRecipes.add(registry, new KeychainApplyEmiRecipe(CATEGORY, holder.id()))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping hexerei:keychain_apply recipe {}", holder.id(), t);
            }
        }

        Emisemble.LOGGER.info("{}: {} keychain_apply recipes registered", NAMESPACE, added);
        return added;
    }
}
