package dev.wp.emisemble.compat.embers.gaseous_fuel;

import java.text.DecimalFormat;
import java.util.List;

import com.rekindled.embers.Embers;
import com.rekindled.embers.RegistryManager;
import com.rekindled.embers.recipe.IGaseousFuelRecipe;
import com.rekindled.embers.util.DecimalFormats;

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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

/** {@code embers:gaseous_fuel}: what a gas is worth to a wildfire stirling. */
public final class GaseousFuelCategory {
    public static final String NAMESPACE = "embers";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RegistryManager.GASEOUS_FUEL.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RegistryManager.WILDFIRE_STIRLING_ITEM.get()), "embers.jei.recipe.gaseous_fuel");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/jei_gaseous_fuel.png");
    private static final int SHEET = 256;
    private static final int WIDTH = 126;
    private static final int HEIGHT = 31;
    private static final int TANK_X = 6;
    private static final int TANK_Y = 7;
    private static final int TANK_SIZE = 16;
    private static final int TEXT_X = 28;
    private static final int BURN_TIME_Y = 5;
    private static final int MULTIPLIER_Y = 17;

    private GaseousFuelCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.CATALYTIC_PLUG_ITEM.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.WILDFIRE_STIRLING_ITEM.get()));

        int added = EmiRecipes.addAll(registry, RegistryManager.GASEOUS_FUEL.get(), Page::new);
        Emisemble.LOGGER.info("embers: {} gaseous fuel recipes registered", added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private final IGaseousFuelRecipe recipe;
        private final FluidStack fluid;

        private Page(RecipeHolder<IGaseousFuelRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            this.recipe = holder.value();
            List<FluidStack> fluids = recipe.getDisplayInput().getFluids();
            this.fluid = fluids.isEmpty() ? FluidStack.EMPTY : fluids.get(0);
            this.inputs.add(FluidTanks.of(fluids.toArray(FluidStack[]::new)));
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
            EmbersCompat.ingotTooltip(FluidTanks.add(widgets, inputs.get(0), TANK_X, TANK_Y, TANK_SIZE, TANK_SIZE,
                    fluid.getAmount()), fluid);

            DecimalFormat multiplierFormat =
                    DecimalFormats.getDecimalFormat(Embers.MODID + ".decimal_format.ember_multiplier");
            widgets.addText(Component.translatable(Embers.MODID + ".jei.recipe.gaseous_fuel.burn_time",
                    recipe.getDisplayBurnTime()), TEXT_X, BURN_TIME_Y, 0xFFFFFF, true);
            widgets.addText(Component.translatable(Embers.MODID + ".jei.recipe.gaseous_fuel.power_multiplier",
                    multiplierFormat.format(recipe.getDisplayMultiplier())), TEXT_X, MULTIPLIER_Y, 0xFFFFFF, true);
        }

        @Override
        public boolean hideCraftable() {
            return true;
        }
    }
}
