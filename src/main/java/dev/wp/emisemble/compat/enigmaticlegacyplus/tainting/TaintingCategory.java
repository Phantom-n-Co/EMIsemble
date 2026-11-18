package dev.wp.emisemble.compat.enigmaticlegacyplus.tainting;

import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** {@code enigmaticlegacyplus:tainting}: the host builds its four pages in code, so this does too. */
public final class TaintingCategory {
    public static final String NAMESPACE = "enigmaticlegacyplus";
    // The host's tainting type exists only inside its JEI plugin, so this literal is the only id there is to reuse.
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "tainting");
    public static final EmiRecipeCategory CATEGORY =
            new TranslatedCategory(ID, EmiStack.of(EnigmaticItems.EARTH_HEART.get()),
                    "gui.enigmaticlegacy.jei.tainting");

    private TaintingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);

        Ingredient cursedRing = Ingredient.of(EnigmaticItems.CURSED_RING);
        Ingredient eitherRing = Ingredient.of(EnigmaticItems.CURSED_RING, EnigmaticItems.REDEMPTION_RING);

        // The host's own order and conditions: twisted and abyssal take the cursed ring alone.
        int added = page(registry, EnigmaticItems.EARTH_HEART.get(), eitherRing)
                + page(registry, EnigmaticItems.TWISTED_HEART.get(), cursedRing)
                + page(registry, EnigmaticItems.PURE_HEART.get(), eitherRing)
                + page(registry, EnigmaticItems.ABYSSAL_HEART.get(), cursedRing);

        Emisemble.LOGGER.info("{}: {} tainting recipes registered", NAMESPACE, added);
        return added;
    }

    private static int page(EmiRegistry registry, Item heart, Ingredient condition) {
        // The construction is inside the try as well: a throw escaping register costs the plugin its whole set.
        try {
            EmiStack input = EmiStack.of(new ItemStack(heart));
            return EmiRecipes.add(registry, new TaintingEmiRecipe(id(input), input, condition)) ? 1 : 0;
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("Skipping tainting page for {}", heart, t);
            return 0;
        }
    }

/**
 * {@code enigmaticlegacyplus:/tainting/<heart>}: the leading slash marks a page with no recipe file behind it.
 */
    private static ResourceLocation id(EmiStack input) {
        ResourceLocation item = BuiltInRegistries.ITEM.getKey(input.getItemStack().getItem());
        return ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "/tainting/" + item.getPath());
    }
}
