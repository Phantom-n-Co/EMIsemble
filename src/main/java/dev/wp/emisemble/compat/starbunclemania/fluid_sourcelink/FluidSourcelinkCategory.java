package dev.wp.emisemble.compat.starbunclemania.fluid_sourcelink;

import alexthw.starbunclemania.recipe.FluidSourcelinkRecipe;
import alexthw.starbunclemania.registry.ModRegistry;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.FluidTanks;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/** {@code starbunclemania:fluid_sourcelink}: a bucket of fluid becomes Source. */
public final class FluidSourcelinkCategory {
    private static final String NAMESPACE = "starbunclemania";
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(ModRegistry.FLUID_SOURCELINK_RT.get()),
            EmiStack.of(ModRegistry.FLUID_SOURCELINK.get()),
            // The host passes a literal, so the page is untranslated in every language.
            Component.literal("Fluid Sourcelink conversion"));

    private FluidSourcelinkCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(ModRegistry.FLUID_SOURCELINK.get()));

        int added = EmiRecipes.addAll(registry, ModRegistry.FLUID_SOURCELINK_RT.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} fluid_sourcelink recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private static final int WIDTH = 120;
        private static final int HEIGHT = 32;

        // The host's fluid rect, 16x24; it is the whole page art, so it takes the tank socket on a blank page.
        private static final int TANK_X = 6;
        private static final int TANK_Y = 5;
        private static final int TANK_WIDTH = 16;
        private static final int TANK_HEIGHT = 24;
        private static final int CAPACITY = 1000;

        // JEI's createAnimatedRecipeArrow(40) is a viewer sprite of the same size as EMI's filling arrow, in ticks.
        private static final int ARROW_X = 25;
        private static final int ARROW_Y = 10;
        private static final int ARROW_MILLIS = 40 * 50;

        private static final int TEXT_X = 55;
        private static final int TEXT_Y = 12;

        private final EmiIngredient fluid;
        private final Item bucket;
        private final Component label;

        private Page(RecipeHolder<FluidSourcelinkRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            FluidSourcelinkRecipe recipe = holder.value();

            Fluid found = recipe.fluidType() == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.get(recipe.fluidType());
            found = found == null ? Fluids.EMPTY : found;
            this.fluid = found == Fluids.EMPTY ? EmiStack.EMPTY : FluidTanks.of(new FluidStack(found, CAPACITY));
            this.bucket = found == Fluids.EMPTY ? Items.AIR : found.getBucket();
            this.label = Component.literal(String.format("%.0f Source", recipe.conversion_ratio() * 1000.0));

            if (!fluid.isEmpty()) {
                inputs.add(fluid);
            }
            // JEI hides the bucket from the page but looks the recipe up by it, which EMI's catalysts list also does.
            if (bucket != Items.AIR) {
                catalysts.add(EmiStack.of(new ItemStack(bucket)));
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            if (fluid.isEmpty()) {
                return;
            }
            FluidTanks.socket(widgets, TANK_X - 1, TANK_Y - 1, TANK_WIDTH + 2, TANK_HEIGHT + 2);
            // TankWidget insets a pixel on every side, so the widget is grown by two to make the fluid one pixel larger
            // on each side than the host's rect, filling the socket rather than sitting inside its rim.
            FluidTanks.addExact(widgets, fluid, TANK_X - 2, TANK_Y - 2, TANK_WIDTH + 4, TANK_HEIGHT + 4, CAPACITY);
            widgets.addFillingArrow(ARROW_X, ARROW_Y, ARROW_MILLIS);
            widgets.addText(label, TEXT_X, TEXT_Y, 0, false);
        }
    }
}
