package dev.wp.emisemble.compat.arselemental.netherite_upgrade;

import alexthw.ars_elemental.common.components.ElementProtectionFlag;
import alexthw.ars_elemental.recipe.NetheriteUpgradeRecipe;
import alexthw.ars_elemental.registry.ModRegistry;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.RingSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/** {@code ars_elemental:netherite_upgrade}: a spellbook and its pedestals gain element protection. */
public final class NetheriteUpgradeCategory {
    private static final String NAMESPACE = "ars_elemental";
    // The host's category overrides only its layout, so it keeps Ars' own apparatus title and icon.
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(ModRegistry.NETHERITE_UP.get()),
            EmiStack.of(BlockRegistry.ENCHANTING_APP_BLOCK), "ars_nouveau.enchanting_apparatus");

    private NetheriteUpgradeCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(BlockRegistry.ENCHANTING_APP_BLOCK));

        int added = EmiRecipes.addAll(registry, ModRegistry.NETHERITE_UP.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} netherite_upgrade recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        // The host's page is blank, so every slot keeps EMI's frame.
        private static final int WIDTH = 114;
        private static final int HEIGHT = 108;
        private static final int CENTRE_X = 48;
        private static final int CENTRE_Y = 45;
        private static final int RING_X = 48;
        private static final int RING_Y = 13;
        private static final int OUTPUT_X = 86;
        private static final int OUTPUT_Y = 10;
        private static final int TEXT_Y = 100;

        // The host passes 10 as the colour, whose zero alpha the font widens to opaque.
        private static final int TEXT_COLOUR = 10;

        private final List<EmiIngredient> ring = new ArrayList<>();
        private final EmiIngredient reagent;
        private final EmiIngredient upgraded;
        private final int sourceCost;

        private Page(RecipeHolder<NetheriteUpgradeRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            NetheriteUpgradeRecipe recipe = holder.value();

            Ingredient book = recipe.reagent();
            this.reagent = book == null ? EmiStack.EMPTY : EmiIngredient.of(book);
            for (Ingredient ingredient : recipe.pedestalItems()) {
                ring.add(EmiIngredient.of(ingredient));
            }
            if (!reagent.isEmpty()) {
                inputs.add(reagent);
            }
            inputs.addAll(ring);

            this.sourceCost = recipe.consumesSource() ? recipe.sourceCost() : 0;
            // The host draws the upgrade as the reagent with the protection flag set, not as the recipe's own result.
            List<EmiStack> books = new ArrayList<>();
            for (ItemStack stack : book == null ? List.<ItemStack>of() : List.of(book.getItems())) {
                ItemStack upgraded = stack.copy();
                upgraded.set(ModRegistry.P4E.get(), new ElementProtectionFlag(true));
                books.add(EmiStack.of(upgraded));
            }
            this.upgraded = EmiIngredient.of(books);
            outputs.addAll(books);
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            RingSlots.add(widgets, ring, RING_X, RING_Y, CENTRE_X, CENTRE_Y);
            RecipeSlots.addFramed(widgets, reagent, CENTRE_X, CENTRE_Y);
            // The host cycles every book variant in this one slot rather than stacking them.
            RecipeSlots.addFramed(widgets, upgraded, OUTPUT_X, OUTPUT_Y).recipeContext(this);
            if (sourceCost > 0) {
                widgets.addText(Component.translatable("ars_nouveau.source", sourceCost), 0, TEXT_Y, TEXT_COLOUR,
                        false);
            }
        }
    }
}
