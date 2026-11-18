package dev.wp.emisemble.compat.embers.catalysis_combustion;

import java.text.DecimalFormat;

import com.rekindled.embers.Embers;
import com.rekindled.embers.RegistryManager;
import com.rekindled.embers.recipe.ICatalysisCombustionRecipe;
import com.rekindled.embers.util.DecimalFormats;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

/** {@code embers:catalysis_combustion}: a catalyst burnt inside an ignem reactor. */
public final class CatalysisCombustionCategory {
    public static final String NAMESPACE = "embers";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RegistryManager.CATALYSIS_COMBUSTION.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RegistryManager.IGNEM_REACTOR_ITEM.get()), "embers.jei.recipe.catalysis_combustion");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/jei_catalysis_combustion.png");
    private static final int SHEET = 256;
    private static final int WIDTH = 126;
    private static final int HEIGHT = 46;
    private static final int SLOT_X = 6;
    private static final int MACHINE_Y = 6;
    private static final int INPUT_Y = 24;
    private static final int TEXT_X = 28;
    private static final int MULTIPLIER_Y = 10;
    private static final int BURN_TIME_Y = 28;

    private CatalysisCombustionCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);

        int added = EmiRecipes.addAll(registry, RegistryManager.CATALYSIS_COMBUSTION.get(), Page::new);
        Emisemble.LOGGER.info("embers: {} catalysis combustion recipes registered", added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private final ICatalysisCombustionRecipe recipe;

        private Page(RecipeHolder<ICatalysisCombustionRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            this.recipe = holder.value();
            this.catalysts.add(EmiIngredient.of(recipe.getDisplayMachine()));
            this.inputs.add(EmiIngredient.of(recipe.getDisplayInput()));
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
            RecipeSlots.addFramed(widgets, catalysts.get(0), SLOT_X, MACHINE_Y);
            RecipeSlots.addFramed(widgets, inputs.get(0), SLOT_X, INPUT_Y);

            DecimalFormat multiplierFormat =
                    DecimalFormats.getDecimalFormat(Embers.MODID + ".decimal_format.ember_multiplier");
            widgets.addText(Component.translatable(Embers.MODID + ".jei.recipe.catalysis_combustion.multiplier",
                    multiplierFormat.format(recipe.getDisplayMultiplier())), TEXT_X, MULTIPLIER_Y, 0xFFFFFF, true);
            widgets.addText(Component.translatable(Embers.MODID + ".jei.recipe.catalysis_combustion.burn_time",
                    recipe.getDisplayTime()), TEXT_X, BURN_TIME_Y, 0xFFFFFF, true);
        }

        @Override
        public boolean hideCraftable() {
            return true;
        }
    }
}
