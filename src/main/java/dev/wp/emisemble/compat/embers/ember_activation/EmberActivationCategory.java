package dev.wp.emisemble.compat.embers.ember_activation;

import com.rekindled.embers.Embers;
import com.rekindled.embers.RegistryManager;
import com.rekindled.embers.recipe.IEmberActivationRecipe;

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

/** {@code embers:ember_activation}: how much ember a fuel is worth in one of the host's burners. */
public final class EmberActivationCategory {
    public static final String NAMESPACE = "embers";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RegistryManager.EMBER_ACTIVATION.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RegistryManager.EMBER_ACTIVATOR_ITEM.get()), "embers.jei.recipe.ember_activation");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/jei_item_text.png");
    private static final int SHEET = 256;
    private static final int WIDTH = 126;
    private static final int HEIGHT = 28;
    private static final int SLOT_X = 6;
    private static final int SLOT_Y = 6;
    private static final int TEXT_X = 28;
    private static final int TEXT_Y = 10;

    private EmberActivationCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.EMBER_ACTIVATOR_ITEM.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.PRESSURE_REFINERY_ITEM.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.IGNEM_REACTOR_ITEM.get()));

        int added = EmiRecipes.addAll(registry, RegistryManager.EMBER_ACTIVATION.get(), Page::new);
        Emisemble.LOGGER.info("embers: {} ember activation recipes registered", added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private final IEmberActivationRecipe recipe;

        private Page(RecipeHolder<IEmberActivationRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            this.recipe = holder.value();
            this.inputs.add(EmiIngredient.of(recipe.getDisplayInput()));
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
            RecipeSlots.addFramed(widgets, inputs.get(0), SLOT_X, SLOT_Y);
            widgets.addText(Component.translatable(Embers.MODID + ".jei.recipe.ember_activation.ember",
                    recipe.getDisplayOutput()), TEXT_X, TEXT_Y, 0xFFFFFF, true);
        }

        @Override
        public boolean hideCraftable() {
            return true;
        }
    }
}
