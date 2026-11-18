package dev.wp.emisemble.compat.hexerei.blood_sigil;

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
import net.joefoxe.hexerei.fluid.ModFluids;
import net.joefoxe.hexerei.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/** {@code hexerei:blood_sigil}: the sigil in, 250 mB of blood out — one page, built by the host's plugin. */
public final class BloodSigilCategory {
    public static final String NAMESPACE = "hexerei";
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "blood_sigil");
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/blood_sigil_gui_jei.png");
    public static final EmiRecipeCategory CATEGORY =
            new TranslatedCategory(ID, EmiStack.of(ModItems.BLOOD_SIGIL.get()), "gui.jei.category.blood_sigil");

    // The leading slash marks a page with no recipe file behind it.
    private static final ResourceLocation PAGE_ID =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "/blood_sigil/blood");
    private static final int BLOOD_AMOUNT = 250;

    private BloodSigilCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            // The cauldron block has no item, so the block form would stack to air; the item is what JEI's catalyst means.
            registry.addWorkstation(CATEGORY, EmiStack.of(ModItems.MIXING_CAULDRON.get()));
        } catch (Throwable t) {
            Emisemble.LOGGER.error("hexerei: could not register the blood sigil category", t);
            return 0;
        }

        int added = 0;
        try {
            FluidStack blood = new FluidStack(ModFluids.BLOOD_FLUID.get(), BLOOD_AMOUNT);
            ItemStack sigil = new ItemStack(ModItems.BLOOD_SIGIL.get());
            if (EmiRecipes.add(registry, new Page(PAGE_ID, sigil, blood))) {
                added++;
            }
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("hexerei: skipping the blood sigil page", t);
        }

        Emisemble.LOGGER.info("{}: {} blood_sigil recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
        private static final int WIDTH = 118;
        private static final int HEIGHT = 51;

        private static final int INPUT_X = 10;
        private static final int INPUT_Y = 20;

        private static final int FLUID_X = 92;
        private static final int FLUID_Y = 20;
        private static final int FLUID_SIZE = 16;
        private static final int FLUID_CAPACITY = 500;

        private static final int TOOLTIP_1_X = 29;
        private static final int TOOLTIP_1_Y = 21;
        private static final int TOOLTIP_1_W = 24;
        private static final int TOOLTIP_1_H = 15;
        private static final int TOOLTIP_2_X = 54;
        private static final int TOOLTIP_2_Y = 14;
        private static final int TOOLTIP_2_W = 24;
        private static final int TOOLTIP_2_H = 21;

        private static final int TEXT_X = 7;
        private static final int TEXT_Y = 5;
        // The host writes this as 9.0F / 2.0F; the two insets below assume that value.
        private static final float TEXT_LINE_HEIGHT = 9.0F / 2.0F;
        private static final int TEXT_MAX_WIDTH = 80;
        private static final int TEXT_COLOR = 0xFF404040;
        private static final int FULL_BRIGHT = 0xF000F0;

        private final FluidStack output;
        private final EmiIngredient input;
        private final EmiStack fluid;

        private Page(ResourceLocation id, ItemStack input, FluidStack output) {
            super(CATEGORY, id, WIDTH, HEIGHT);
            this.output = output;
            this.input = EmiIngredient.of(Ingredient.of(input));
            // The amount has to ride on the stack too: the tank divides by the capacity, so 250 mB fills half the socket.
            this.fluid = EmiStack.of(output.getFluid(), output.getComponentsPatch(), output.getAmount());

            inputs.add(this.input);
            outputs.add(this.fluid);
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, 256, -4, -4, WIDTH + 8, HEIGHT + 8, 0, 0);

            RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
            // The tank's frame and one-pixel inset cancel, so the fluid lands on JEI's 16x16 rect.
            widgets.addTank(fluid.copy(), FLUID_X - 1, FLUID_Y - 1, FLUID_SIZE + 2, FLUID_SIZE + 2, FLUID_CAPACITY)
                    .drawBack(false).recipeContext(this);

            widgets.addTooltipText(List.of(Component.translatable("gui.jei.category.blood_sigil_tooltip1")),
                    TOOLTIP_1_X, TOOLTIP_1_Y, TOOLTIP_1_W, TOOLTIP_1_H);
            widgets.addTooltipText(List.of(Component.translatable("gui.jei.category.blood_sigil_tooltip2")),
                    TOOLTIP_2_X, TOOLTIP_2_Y, TOOLTIP_2_W, TOOLTIP_2_H);

            // addText cannot scale and an unscaled name runs off the 126px art, so the shrink is replayed here.
            Component name = output.getHoverName();
            widgets.addDrawable(-4, -4, WIDTH, HEIGHT, (GuiGraphics draw, int mouseX, int mouseY, float delta) -> {
                Minecraft minecraft = Minecraft.getInstance();
                float width = minecraft.font.width(name);
                float percent = width > TEXT_MAX_WIDTH ? width / TEXT_MAX_WIDTH : 1.0F;
                draw.pose().pushPose();
                draw.pose().scale(1.0F / percent, 1.0F / percent, 1.0F / percent);
                minecraft.font.drawInBatch(name, TEXT_X * percent, (TEXT_Y + TEXT_LINE_HEIGHT) * percent - 4.5F,
                        TEXT_COLOR, false, draw.pose().last().pose(), draw.bufferSource(),
                        Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
                draw.pose().popPose();
                draw.flush();
            });
        }
    }
}
