package dev.wp.emisemble.compat.thaumaturge.infusion;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.world.item.crafting.RecipeType;

/** The three infusion categories: one page layout, one icon, three recipe types and titles. */
public final class InfusionCompat {
    public static final EmiRecipeCategory INFUSION = category(TTRecipeTypes.INFUSION.get(),
            "gui.thaumaturge.recipe_type.infusion");
    public static final EmiRecipeCategory INFUSION_ENCHANTMENT = category(TTRecipeTypes.INFUSION_ENCHANTMENT.get(),
            "gui.thaumaturge.recipe_type.infusion_enchantment");
    public static final EmiRecipeCategory RUNIC_AUGMENT = category(TTRecipeTypes.RUNIC_AUGMENT.get(),
            "gui.thaumaturge.recipe_type.runic_augment");

    private InfusionCompat() {}

    public static void register(EmiRegistry registry) {
        for (EmiRecipeCategory category : new EmiRecipeCategory[] {INFUSION, INFUSION_ENCHANTMENT, RUNIC_AUGMENT}) {
            registry.addCategory(category);
            registry.addWorkstation(category, EmiStack.of(TTItems.INFUSION_MATRIX.get()));
        }

        int infusion = EmiRecipes.addAll(registry, TTRecipeTypes.INFUSION.get(),
                holder -> new InfusionEmiRecipe<>(INFUSION, holder));
        int enchantment = EmiRecipes.addAll(registry, TTRecipeTypes.INFUSION_ENCHANTMENT.get(),
                holder -> new InfusionEmiRecipe<>(INFUSION_ENCHANTMENT, holder));
        int runic = EmiRecipes.addAll(registry, TTRecipeTypes.RUNIC_AUGMENT.get(),
                holder -> new InfusionEmiRecipe<>(RUNIC_AUGMENT, holder));

        Emisemble.LOGGER.info("thaumaturge: {} infusion, {} infusion_enchantment and {} runic_augment recipes registered",
                infusion, enchantment, runic);
    }

    private static EmiRecipeCategory category(RecipeType<?> type, String titleKey) {
        return new TranslatedCategory(EmiRecipes.categoryId(type), EmiStack.of(TTItems.INFUSION_MATRIX.get()), titleKey);
    }
}
