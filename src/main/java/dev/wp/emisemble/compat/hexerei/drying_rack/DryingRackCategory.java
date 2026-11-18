package dev.wp.emisemble.compat.hexerei.drying_rack;

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
import net.joefoxe.hexerei.data.recipes.DryingRackRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

/** {@code hexerei:drying_rack}: one herb in, one dried item out. */
public final class DryingRackCategory {
    public static final String NAMESPACE = "hexerei";
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "drying_rack");
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/drying_rack_jei.png");
    public static final EmiRecipeCategory CATEGORY =
            new TranslatedCategory(ID, EmiStack.of(ModBlocks.HERB_DRYING_RACK.get()), "block.hexerei.herb_drying_rack");

    private DryingRackCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(ModBlocks.HERB_DRYING_RACK.get()));

        int added = EmiRecipes.addAll(registry, DryingRackRecipe.Type.INSTANCE, Page::new);

        Emisemble.LOGGER.info("{}: {} drying_rack recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
        private static final int WIDTH = 92;
        private static final int HEIGHT = 45;

        private static final int INPUT_X = 10;
        private static final int INPUT_Y = 12;
        private static final int OUTPUT_X = 66;
        private static final int OUTPUT_Y = 12;

        private static final float TEXT_SCALE = 0.6F;
        private static final int NAME_COLOUR = 0xFF404040;
        private static final int TIME_COLOUR = 0xFF808080;

        // JEI's text pass is scaled to 0.6, so these floats only land right inside that pose.
        private static final float NAME_X = 8.33F;
        private static final float NAME_Y = 6.664F;
        private static final float LABEL_X = 9.996F;
        private static final float SECONDS_X = 91.63F;
        private static final float TIME_Y = 68.306F;

        private static final String DRY_TIME_KEY = "gui.jei.category.dipper.dry_time_1";
        private static final String RESULT_SECONDS_KEY = "gui.jei.category.dipper.resultSeconds";

        private final EmiIngredient input;
        private final ItemStack outputStack;
        private final EmiStack output;
        private final int dryingTime;

        private Page(RecipeHolder<DryingRackRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            DryingRackRecipe recipe = holder.value();

            // getIngredients() is a NonNullList of exactly one, but a broken recipe file must not take the page down.
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            this.input = ingredients.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(ingredients.get(0));
            this.outputStack = recipe.getOutput();
            this.output = EmiStack.of(outputStack);
            this.dryingTime = recipe.getDryingTime();

            inputs.add(input);
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, 256, -4, -4, WIDTH + 8, HEIGHT + 8, 0, 0);
            widgets.addDrawable(-4, -4, WIDTH, HEIGHT, this::drawText);

            RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        }

        private void drawText(GuiGraphics draw, int mouseX, int mouseY, float delta) {
            Font font = Minecraft.getInstance().font;

            draw.pose().pushPose();
            draw.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
            // The host flattens the name to a plain string, so a renamed item loses its styling in JEI too.
            draw.drawString(font, outputStack.getHoverName().getString(), NAME_X, NAME_Y, NAME_COLOUR, false);
            draw.drawString(font, Component.translatable(DRY_TIME_KEY).getVisualOrderText(),
                    LABEL_X, TIME_Y, TIME_COLOUR, false);
            draw.drawString(font, Component.translatable(RESULT_SECONDS_KEY, seconds(dryingTime)).getVisualOrderText(),
                    SECONDS_X, TIME_Y, TIME_COLOUR, false);
            draw.pose().popPose();
        }

        /** The host's formatter verbatim: the tick remainder is appended, then a single trailing '0' trimmed. */
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
