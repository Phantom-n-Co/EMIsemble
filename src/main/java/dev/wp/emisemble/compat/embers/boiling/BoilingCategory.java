package dev.wp.emisemble.compat.embers.boiling;

import java.util.List;

import com.rekindled.embers.RegistryManager;
import com.rekindled.embers.recipe.IBoilingRecipe;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.embers.EmbersCompat;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.FluidTanks;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

/** {@code embers:boiling}: what a mini boiler turns one fluid into. */
public final class BoilingCategory {
    public static final String NAMESPACE = "embers";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RegistryManager.BOILING.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RegistryManager.MINI_BOILER_ITEM.get()), "embers.jei.recipe.boiling");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/jei_boiler.png");
    private static final int SHEET = 256;
    private static final int WIDTH = 126;
    private static final int HEIGHT = 28;
    private static final int TANK_SIZE = 16;
    private static final int INPUT_X = 26;
    private static final int OUTPUT_X = 84;
    private static final int TANK_Y = 6;

    private BoilingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.MINI_BOILER_ITEM.get()));

        int added = EmiRecipes.addAll(registry, RegistryManager.BOILING.get(), Page::new);
        Emisemble.LOGGER.info("embers: {} boiling recipes registered", added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private final IBoilingRecipe recipe;
        private final FluidStack in;

        private Page(RecipeHolder<IBoilingRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            this.recipe = holder.value();
            List<FluidStack> fluids = recipe.getDisplayInput().getFluids();
            this.in = fluids.isEmpty() ? FluidStack.EMPTY : fluids.get(0);
            this.inputs.add(FluidTanks.of(fluids.toArray(FluidStack[]::new)));
            this.outputs.add(FluidTanks.stack(recipe.getDisplayOutput()));
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
            EmbersCompat.ingotTooltip(FluidTanks.add(widgets, inputs.get(0), INPUT_X, TANK_Y, TANK_SIZE, TANK_SIZE,
                    in.getAmount()), in);
            EmbersCompat.ingotTooltip(FluidTanks.add(widgets, outputs.get(0), OUTPUT_X, TANK_Y, TANK_SIZE, TANK_SIZE,
                    recipe.getDisplayOutput().getAmount()), recipe.getDisplayOutput()).recipeContext(this);
        }
    }
}
