package dev.wp.emisemble.compat.hexerei.pestle_and_mortar;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.joefoxe.hexerei.block.ModBlocks;
import net.joefoxe.hexerei.data.recipes.PestleAndMortarRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** {@code hexerei:pestle_and_mortar}: up to five reagents ground into one output. */
public final class PestleAndMortarCategory {
    public static final String NAMESPACE = "hexerei";
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "pestle_and_mortar");
    public static final EmiRecipeCategory CATEGORY =
            new TranslatedCategory(ID, EmiStack.of(ModBlocks.PESTLE_AND_MORTAR.get()), "block.hexerei.pestle_and_mortar");

    private PestleAndMortarCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(ModBlocks.PESTLE_AND_MORTAR.get()));

        int added = EmiRecipes.addAll(registry, PestleAndMortarRecipe.Type.INSTANCE, Page::new);

        Emisemble.LOGGER.info("{}: {} pestle_and_mortar recipes registered", NAMESPACE, added);
        return added;
    }

    /** The five cells are fixed in the host class; a recipe with fewer reagents leaves the later cells empty. */
    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/pestle_and_mortar_jei.png");
        private static final int SHEET = 256;

        // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
        private static final int WIDTH = 135;
        private static final int HEIGHT = 72;

        private static final int[][] INPUT_SLOTS = {{7, 10}, {16, 32}, {38, 41}, {60, 32}, {69, 10}};
        private static final int OUTPUT_X = 113;
        private static final int OUTPUT_Y = 27;

        private static final float TEXT_SCALE = 0.6F;
        private static final int NAME_COLOUR = 0xFF404040;
        private static final int TIME_COLOUR = 0xFF808080;

        // The host's floats, which only land right inside the 0.6 pose.
        private static final float NAME_X = 8.33F;
        private static final float NAME_Y = 6.664F;
        private static final float LABEL_X = 9.996F;
        private static final float SECONDS_X = 96.628F;
        private static final float TIME_Y = 114.121F;

        private static final String GRIND_TIME_KEY = "gui.jei.category.pestle_and_mortar.grind_time_1";
        private static final String RESULT_SECONDS_KEY = "gui.jei.category.dipper.resultSeconds";

        private final ItemStack outputStack;
        private final EmiStack output;
        private final int grindingTime;

        private Page(RecipeHolder<PestleAndMortarRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            PestleAndMortarRecipe recipe = holder.value();

            List<Ingredient> ingredients = recipe.getIngredients();
            for (int i = 0; i < INPUT_SLOTS.length && i < ingredients.size(); i++) {
                inputs.add(EmiIngredient.of(ingredients.get(i)));
            }

            this.outputStack = recipe.getOutput();
            this.output = EmiStack.of(outputStack);
            this.grindingTime = recipe.getGrindingTime();

            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, -4, -4, WIDTH + 8, HEIGHT + 8, 0, 0);
            widgets.addDrawable(-4, -4, WIDTH, HEIGHT, this::drawText);

            for (int i = 0; i < inputs.size() && i < INPUT_SLOTS.length; i++) {
                RecipeSlots.add(widgets, inputs.get(i), INPUT_SLOTS[i][0], INPUT_SLOTS[i][1]);
            }
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        }

        private void drawText(GuiGraphics draw, int mouseX, int mouseY, float delta) {
            Font font = Minecraft.getInstance().font;

            draw.pose().pushPose();
            draw.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
            draw.drawString(font, Component.translatable(GRIND_TIME_KEY).getVisualOrderText(),
                    LABEL_X, TIME_Y, TIME_COLOUR, false);
            draw.drawString(font, Component.translatable(RESULT_SECONDS_KEY, seconds(grindingTime)).getVisualOrderText(),
                    SECONDS_X, TIME_Y, TIME_COLOUR, false);
            // The host flattens the name to a plain string, so styling on a renamed item is lost in JEI as well.
            draw.drawString(font, outputStack.getHoverName().getString(), NAME_X, NAME_Y, NAME_COLOUR, false);
            draw.pose().popPose();
        }

        /** The host's formatter: the tick remainder is appended, then a single trailing '0' trimmed. */
        private static String seconds(int ticks) {
            if (ticks >= Integer.MAX_VALUE) {
                return "?";
            }
            String text = ticks / 20 + (ticks % 20 == 0 ? "" : "." + ticks % 20);
            if (!text.isEmpty() && text.charAt(text.length() - 1) == '0' && ticks != 0 && ticks % 20 != 0) {
                text = text.substring(0, text.length() - 1);
            }
            return text;
        }
    }
}
