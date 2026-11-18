package dev.wp.emisemble.compat.embers.metal_coefficient;

import java.text.DecimalFormat;

import com.rekindled.embers.Embers;
import com.rekindled.embers.RegistryManager;
import com.rekindled.embers.recipe.IMetalCoefficientRecipe;
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
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

/** {@code embers:metal_coefficient}: how much ember a block of ore is worth to the pressure refinery. */
public final class MetalCoefficientCategory {
    public static final String NAMESPACE = "embers";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RegistryManager.METAL_COEFFICIENT.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RegistryManager.PRESSURE_REFINERY_ITEM.get()), "embers.jei.recipe.metal_coefficient");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/jei_item_text.png");
    private static final int SHEET = 256;
    private static final int WIDTH = 126;
    private static final int HEIGHT = 28;
    private static final int SLOT_X = 6;
    private static final int SLOT_Y = 6;
    private static final int TEXT_X = 28;
    private static final int TEXT_Y = 10;

    private MetalCoefficientCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.PRESSURE_REFINERY_ITEM.get()));

        int added = EmiRecipes.addAll(registry, RegistryManager.METAL_COEFFICIENT.get(), Page::new);
        Emisemble.LOGGER.info("embers: {} metal coefficient recipes registered", added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private final IMetalCoefficientRecipe recipe;

        private Page(RecipeHolder<IMetalCoefficientRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            this.recipe = holder.value();
            this.catalysts.add(EmbersCompat.items(recipe.getDisplayInput()));
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
            RecipeSlots.addFramed(widgets, catalysts.get(0), SLOT_X, SLOT_Y);
            DecimalFormat multiplierFormat =
                    DecimalFormats.getDecimalFormat(Embers.MODID + ".decimal_format.ember_multiplier");
            widgets.addText(Component.literal(multiplierFormat.format(recipe.getDisplayCoefficient())),
                    TEXT_X, TEXT_Y, 0xFFFFFF, true);
        }

        @Override
        public boolean hideCraftable() {
            return true;
        }
    }
}
