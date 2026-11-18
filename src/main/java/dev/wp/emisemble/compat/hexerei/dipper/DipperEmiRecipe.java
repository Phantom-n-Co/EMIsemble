package dev.wp.emisemble.compat.hexerei.dipper;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.joefoxe.hexerei.data.recipes.DipperRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

/** One dipper page: the item, the fluid it soaks in and the host's two time lines. */
public final class DipperEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
    private static final int WIDTH = 92;
    private static final int HEIGHT = 84;

    private static final int INPUT_X = 11;
    private static final int INPUT_Y = 10;

    private static final int OUTPUT_X = 65;
    private static final int OUTPUT_Y = 19;

    private static final int FLUID_X = 13;
    private static final int FLUID_Y = 31;
    private static final int FLUID_WIDTH = 12;
    private static final int FLUID_HEIGHT = 10;
    private static final int MAX_AMOUNT = 2000;

    private static final float TEXT_SCALE = 0.6F;
    private static final float LABEL_X = 9.996F;
    private static final float VALUE_X = 108.29F;
    private static final float DIPS_Y = 93.296F;
    private static final float DIP_TIME_Y = 113.288F;
    private static final float DRY_TIME_Y = 133.28F;
    private static final float NAME_X = 8.33F;
    private static final float NAME_Y = 6.664F;
    private static final int LABEL_COLOR = 0xFF808080;
    private static final int NAME_COLOR = 0xFF404040;
    private static final int FULL_BRIGHT = 0xF000F0;

    private final ItemStack output;
    private final EmiIngredient input;
    private final EmiStack fluid;
    private final int dips;
    private final int dippingTime;
    private final int dryingTime;

    public DipperEmiRecipe(EmiRecipeCategory category, ResourceLocation id, DipperRecipe recipe) {
        super(category, id, WIDTH, HEIGHT);
        this.output = recipe.getOutput();
        this.input = EmiIngredient.of(Ingredient.of(recipe.getInput()));
        this.dips = recipe.getNumberOfDips();
        this.dippingTime = recipe.getDippingTime();
        this.dryingTime = recipe.getDryingTime();

        inputs.add(this.input);
        outputs.add(EmiStack.of(output));

        FluidStack liquid = fluid(recipe);
        if (liquid.isEmpty() || liquid.getAmount() <= 0) {
            this.fluid = EmiStack.EMPTY;
        } else {
            this.fluid = EmiStack.of(liquid.getFluid(), liquid.getComponentsPatch(), liquid.getAmount());
            inputs.add(this.fluid);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, DipperCategory.TEXTURE, 256, -4, -4, WIDTH + 8, HEIGHT + 8, 0, 0);

        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        RecipeSlots.add(widgets, EmiStack.of(output), OUTPUT_X, OUTPUT_Y).recipeContext(this);

        if (!fluid.isEmpty()) {
            widgets.addTank(fluid.copy(), FLUID_X - 1, FLUID_Y - 1, FLUID_WIDTH + 2, FLUID_HEIGHT + 2,
                    (int) fluid.getAmount()).drawBack(false);
        }

        addTimes(widgets);
    }

    private void addTimes(WidgetHolder widgets) {
        String dipCount = dips < Integer.MAX_VALUE ? Integer.toString(dips) : "?";
        MutableComponent timesDipped = Component.translatable("gui.jei.category.dipper.times_dipped_1");
        MutableComponent dipCountValue = Component.translatable("gui.jei.category.dipper.result", dipCount);
        MutableComponent dipTime = Component.translatable("gui.jei.category.dipper.dip_time_1");
        MutableComponent dipTimeValue = Component.translatable("gui.jei.category.dipper.resultSeconds", seconds(dippingTime));
        MutableComponent dryTime = Component.translatable("gui.jei.category.dipper.dry_time_1");
        MutableComponent dryTimeValue = Component.translatable("gui.jei.category.dipper.resultSeconds", seconds(dryingTime));
        Component name = output.getHoverName();

        widgets.addDrawable(-4, -4, WIDTH, HEIGHT, (GuiGraphics draw, int mouseX, int mouseY, float delta) -> {
            Minecraft minecraft = Minecraft.getInstance();
            draw.pose().pushPose();
            draw.pose().scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);
            minecraft.font.drawInBatch(timesDipped, LABEL_X, DIPS_Y, LABEL_COLOR, false,
                    draw.pose().last().pose(), draw.bufferSource(), Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
            minecraft.font.drawInBatch(dipCountValue, VALUE_X, DIPS_Y, LABEL_COLOR, false,
                    draw.pose().last().pose(), draw.bufferSource(), Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
            minecraft.font.drawInBatch(dipTime, LABEL_X, DIP_TIME_Y, LABEL_COLOR, false,
                    draw.pose().last().pose(), draw.bufferSource(), Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
            minecraft.font.drawInBatch(dipTimeValue, VALUE_X, DIP_TIME_Y, LABEL_COLOR, false,
                    draw.pose().last().pose(), draw.bufferSource(), Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
            minecraft.font.drawInBatch(dryTime, LABEL_X, DRY_TIME_Y, LABEL_COLOR, false,
                    draw.pose().last().pose(), draw.bufferSource(), Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
            minecraft.font.drawInBatch(dryTimeValue, VALUE_X, DRY_TIME_Y, LABEL_COLOR, false,
                    draw.pose().last().pose(), draw.bufferSource(), Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
            minecraft.font.drawInBatch(name, NAME_X, NAME_Y, NAME_COLOR, false,
                    draw.pose().last().pose(), draw.bufferSource(), Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
            draw.pose().popPose();
            draw.flush();
        });
    }

    private static String seconds(int ticks) {
        String value = ticks < Integer.MAX_VALUE ? ticks / 20 + (ticks % 20 == 0 ? "" : "." + ticks % 20) : "?";
        if (value.charAt(value.length() - 1) == '0' && ticks != 0 && ticks % 20 != 0) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static FluidStack fluid(DipperRecipe recipe) {
        FluidStack fluid = recipe.getFluid();
        if (fluid.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return fluid.copyWithAmount(Mth.clamp(recipe.getFluidLevelsConsumed(), 0, MAX_AMOUNT));
    }
}
