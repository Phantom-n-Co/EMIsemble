package dev.wp.emisemble.compat.arsadditions.charm_charging;

import com.github.jarva.arsadditions.common.recipe.imbuement.CharmChargingRecipe;
import com.github.jarva.arsadditions.setup.registry.AddonRecipeRegistry;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/** {@code ars_additions:charm_charging}: an imbuement chamber refills a charm for Source. */
public final class CharmChargingCategory {
    private static final String NAMESPACE = "ars_additions";
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(AddonRecipeRegistry.CHARM_CHARGING_TYPE.get()),
            EmiStack.of(BlockRegistry.IMBUEMENT_BLOCK),
            "ars_additions.charm_recharging");

    private CharmChargingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(BlockRegistry.IMBUEMENT_BLOCK));

        int added = EmiRecipes.addAll(registry, AddonRecipeRegistry.CHARM_CHARGING_TYPE.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} charm_charging recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        // The host's page is blank apart from these two slots, so both keep EMI's frame.
        private static final int INPUT_X = 48;
        private static final int INPUT_Y = 45;
        private static final int OUTPUT_X = 86;
        private static final int OUTPUT_Y = 10;
        private static final int WIDTH = 126;
        private static final int HEIGHT = 108;

        // The host draws the cost at y = 100 and passes 10 as the colour, whose zero alpha the font widens to opaque.
        private static final int TEXT_Y = 100;
        private static final int TEXT_COLOUR = 10;

        private final EmiStack charm;
        private final Component cost;

        private Page(RecipeHolder<CharmChargingRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            CharmChargingRecipe recipe = holder.value();

            this.charm = EmiStack.of(new ItemStack(recipe.input()));
            this.cost = Component.translatable("ars_additions.source_per_charge", recipe.costPerCharge());
            if (!charm.isEmpty()) {
                inputs.add(charm);
                outputs.add(charm);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            RecipeSlots.addFramed(widgets, charm, INPUT_X, INPUT_Y);
            RecipeSlots.addFramed(widgets, charm, OUTPUT_X, OUTPUT_Y).recipeContext(this);
            widgets.addText(cost, 0, TEXT_Y, TEXT_COLOUR, false);
        }
    }
}
