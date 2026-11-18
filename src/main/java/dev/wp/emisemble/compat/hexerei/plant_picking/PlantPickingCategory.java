package dev.wp.emisemble.compat.hexerei.plant_picking;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.joefoxe.hexerei.block.ModBlocks;
import net.joefoxe.hexerei.block.custom.PickableDoublePlant;
import net.joefoxe.hexerei.block.custom.PickablePlant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** {@code hexerei:plant_picking}: what each grown herb gives up when it is right-clicked. */
public final class PlantPickingCategory {
    public static final String NAMESPACE = "hexerei";
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "plant_picking");
    public static final EmiRecipeCategory CATEGORY =
            new TranslatedCategory(ID, EmiStack.of(ModBlocks.MANDRAKE_PLANT.get()), "gui.jei.category.plant_picking");

    private PlantPickingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);

        int added = 0;
        for (Pick pick : picks()) {
            try {
                if (EmiRecipes.add(registry, new Page(pick))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping {} plant_picking page for {}", NAMESPACE, pick.plant(), t);
            }
        }

        Emisemble.LOGGER.info("{}: {} plant_picking pages registered", NAMESPACE, added);
        return added;
    }

    /** The host's own order, which is the order the pages are listed in both viewers. */
    private static List<Pick> picks() {
        PickablePlant belladonna = ModBlocks.BELLADONNA_PLANT.get();
        PickablePlant mandrake = ModBlocks.MANDRAKE_PLANT.get();
        PickableDoublePlant mugwort = ModBlocks.MUGWORT_BUSH.get();
        PickableDoublePlant yellowDock = ModBlocks.YELLOW_DOCK_BUSH.get();

        return List.of(Pick.of(belladonna), Pick.of(mandrake), Pick.of(mugwort), Pick.of(yellowDock));
    }

    /** Stored as item keys and looked up at page build time, where the host resolves them too. */
    private static ItemStack stackOf(@Nullable ResourceKey<Item> key) {
        if (key == null) {
            return ItemStack.EMPTY;
        }
        return BuiltInRegistries.ITEM.getOptional(key).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    private static ResourceLocation idOf(Block plant) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE,
                "/plant_picking/" + BuiltInRegistries.BLOCK.getKey(plant).getPath());
    }

    /** The two plant classes share field names but no supertype, so the block and keys are resolved here. */
    private record Pick(Block plant, @Nullable ResourceKey<Item> first, @Nullable ResourceKey<Item> second) {
        static Pick of(PickablePlant plant) {
            return new Pick(plant, plant.firstOutput, plant.secondOutput);
        }

        static Pick of(PickableDoublePlant plant) {
            return new Pick(plant, plant.firstOutput, plant.secondOutput);
        }
    }

    /**
     * No recipe type backs these pages: the host's {@code PlantPickingRecipeJEI} is a client record filled from the four
     * plant blocks, so the id is synthetic and there is no backing recipe.
     */
    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/plant_picking_gui_jei.png");
        private static final int SHEET = 256;

        // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
        private static final int WIDTH = 181;
        private static final int HEIGHT = 51;

        private static final int INPUT_X = 11;
        private static final int INPUT_Y = 20;
        private static final int FIRST_X = 125;
        private static final int SECOND_X = 154;
        private static final int OUTPUT_Y = 20;

        // JEI draws its plant-model overlay at the input slot plus the overlay's (74,0) offset.
        private static final int PLANT_X = INPUT_X + 74;
        private static final int PLANT_Y = INPUT_Y;

        private static final int TOOLTIP_X = 82;
        private static final int TOOLTIP_Y = 19;
        private static final int TOOLTIP_SIZE = 20;
        private static final String TOOLTIP_KEY = "gui.jei.category.plant_picking_tooltip";

        private static final int NAME_COLOUR = 0xFF404040;
        private static final float NAME_X = 39.0F;
        private static final float NAME_Y = 5.0F;
        private static final float LINE_HEIGHT = 9.0F / 2.0F;
        private static final float HALF_LINE = 4.5F;
        private static final int NAME_LIMIT = 80;

        private final ItemStack inputStack;
        private final EmiStack input;
        private final EmiStack first;
        private final EmiStack second;

        private Page(Pick pick) {
            super(CATEGORY, idOf(pick.plant()), WIDTH, HEIGHT);
            this.inputStack = new ItemStack(pick.plant());
            this.input = EmiStack.of(inputStack);
            this.first = EmiStack.of(stackOf(pick.first()));
            this.second = EmiStack.of(stackOf(pick.second()));

            inputs.add(input);
            if (!first.isEmpty()) {
                outputs.add(first);
            }
            if (!second.isEmpty()) {
                outputs.add(second);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, -4, -4, WIDTH + 8, HEIGHT + 8, 0, 0);
            widgets.addDrawable(-4, -4, WIDTH, HEIGHT, this::drawOverlay);
            widgets.addTooltipText(List.of(Component.translatable(TOOLTIP_KEY)),
                    TOOLTIP_X, TOOLTIP_Y, TOOLTIP_SIZE, TOOLTIP_SIZE);

            RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
            RecipeSlots.add(widgets, first, FIRST_X, OUTPUT_Y).recipeContext(this);
            RecipeSlots.add(widgets, second, SECOND_X, OUTPUT_Y).recipeContext(this);
        }

        /** The plant's icon stands in for JEI's 3D block-model overlay; the name shrink is the host's. */
        private void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {
            Font font = Minecraft.getInstance().font;

            input.render(draw, PLANT_X, PLANT_Y, delta);

            Component name = inputStack.getHoverName();
            int width = font.width(name);
            if (width > NAME_LIMIT) {
                float percent = (float) width / NAME_LIMIT;
                draw.pose().pushPose();
                draw.pose().scale(1.0F / percent, 1.0F / percent, 1.0F / percent);
                draw.drawString(font, name.getVisualOrderText(), NAME_X * percent,
                        (NAME_Y + LINE_HEIGHT) * percent - HALF_LINE, NAME_COLOUR, false);
                draw.pose().popPose();
            } else {
                draw.drawString(font, name.getVisualOrderText(), NAME_X, NAME_Y + LINE_HEIGHT - HALF_LINE,
                        NAME_COLOUR, false);
            }
        }
    }
}
