package dev.wp.emisemble.compat.dungeonsdelight.sculking;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.yirmiri.dungeonsdelight.core.init.DDTags;
import net.yirmiri.dungeonsdelight.core.registry.DDBlocks;

import java.util.ArrayList;
import java.util.List;

/** {@code dungeonsdelight:sculking}: embedded eggs grow into a heap of ancient eggs beside their activators. */
public final class SculkingCategory {
    public static final String NAMESPACE = "dungeonsdelight";
    // The host invents this uid for a dummy page, so it is also the id JEMI looks for.
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "sculking");
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(new ItemStack(DDBlocks.HEAP_OF_ANCIENT_EGGS.get())), "farmersdelight.jei.sculking");

    private SculkingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(new ItemStack(DDBlocks.EMBEDDED_EGGS.get())));

        // One page for the whole category: the host registers a single dummy recipe, not a recipe type's contents.
        int added = EmiRecipes.add(registry, new Page()) ? 1 : 0;

        Emisemble.LOGGER.info("{}: {} sculking recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/sculking_jei.png");
        private static final int SHEET = 256;

        // The panel is 118x80 but its art stops at row 58; the drawn sockets below it need the rest of the height.
        private static final int WIDTH = 118;
        private static final int HEIGHT = 80;
        private static final int ART_HEIGHT = 58;

        private static final int INPUT_X = 9;
        private static final int INPUT_Y = 26;
        private static final int OUTPUT_X = 93;
        private static final int OUTPUT_Y = 26;

        // The host draws two empty 18x18 sockets from a 22px sprite, so each blit carries 4px of transparent margin.
        private static final int SOCKET_WIDTH = 22;
        private static final int SOCKET_HEIGHT = 22;
        private static final int SOCKET_U = 119;
        private static final int SOCKET_V = 0;
        private static final int SOCKET_LEFT_X = 47;
        private static final int SOCKET_RIGHT_X = 63;
        private static final int SOCKET_Y = 53;

        private static final int ACCELERATOR_X = 64;
        private static final int ACCELERATOR_Y = 54;
        private static final int ACCELERATOR_SIZE = 16;

        private static final int NIGHT_X = 40;
        private static final int SPAWNER_X = 53;
        private static final int ACCELERATORS_X = 67;
        private static final int GLYPH_Y = 38;
        private static final int GLYPH_SIZE = 11;

        private final EmiStack input;
        private final EmiStack output;
        private final List<ItemStack> accelerators;

        private Page() {
            super(CATEGORY, EmiRecipes.synthetic(ID), WIDTH, HEIGHT);

            this.input = EmiStack.of(new ItemStack(DDBlocks.EMBEDDED_EGGS.get()));
            this.output = EmiStack.of(new ItemStack(DDBlocks.HEAP_OF_ANCIENT_EGGS.get()));
            this.accelerators = activatorStacks();

            inputs.add(input);
            outputs.add(output);
        }

        // Read at registration, like the host: the socket shows whatever the pack tagged as an activator.
        private static List<ItemStack> activatorStacks() {
            List<ItemStack> stacks = new ArrayList<>();
            for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(DDTags.BlockT.SCULKING_ACTIVATORS)) {
                stacks.add(new ItemStack(holder.value()));
            }
            return stacks;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, ART_HEIGHT, 0, 0);
            AlphaTextures.add(widgets, TEXTURE, SHEET, SOCKET_LEFT_X, SOCKET_Y, SOCKET_WIDTH, SOCKET_HEIGHT,
                    SOCKET_U, SOCKET_V);
            AlphaTextures.add(widgets, TEXTURE, SHEET, SOCKET_RIGHT_X, SOCKET_Y, SOCKET_WIDTH, SOCKET_HEIGHT,
                    SOCKET_U, SOCKET_V);

            RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);

            if (!accelerators.isEmpty()) {
                // JEI renders this slot without making it an ingredient, so the port draws the item instead of adding one.
                widgets.addDrawable(ACCELERATOR_X, ACCELERATOR_Y, ACCELERATOR_SIZE, ACCELERATOR_SIZE,
                        (GuiGraphics draw, int mouseX, int mouseY, float delta) ->
                                draw.renderFakeItem(currentAccelerator(), 0, 0));
                widgets.addTooltip((mouseX, mouseY) -> EmiStack.of(currentAccelerator()).getTooltip(),
                        ACCELERATOR_X, ACCELERATOR_Y, ACCELERATOR_SIZE, ACCELERATOR_SIZE);
            }

            widgets.addTooltipText(List.of(Component.translatable("dungeonsdelight.jei.sculking.night")),
                    NIGHT_X, GLYPH_Y, GLYPH_SIZE, GLYPH_SIZE);
            widgets.addTooltipText(List.of(Component.translatable("dungeonsdelight.jei.sculking.spawner")),
                    SPAWNER_X, GLYPH_Y, GLYPH_SIZE, GLYPH_SIZE);
            widgets.addTooltipText(List.of(Component.translatable("dungeonsdelight.jei.sculking.accelerators")),
                    ACCELERATORS_X, GLYPH_Y, GLYPH_SIZE, GLYPH_SIZE);
        }

        // Cycles the activator family the way the host's render-only slot does, on the level clock.
        private ItemStack currentAccelerator() {
            ClientLevel level = Minecraft.getInstance().level;
            long time = level == null ? 0L : level.getGameTime();
            return accelerators.get((int) (time / 20L % accelerators.size()));
        }
    }
}
