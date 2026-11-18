package dev.wp.emisemble.porting;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/** Fluid ingredients and tanks placed with JEI's coordinates. */
public final class FluidTanks {
    /** The three colours {@code GuiHelper.drawSlot} uses for a tank socket. */
    private static final int RIM_DARK = 0xFF373737;
    private static final int RIM_MAIN = 0xFF8B8B8B;
    private static final int RIM_LIGHT = 0xFFFFFFFF;

    private FluidTanks() {}

    /** The fluids an ingredient accepts, or {@code EMPTY} when it accepts none. */
    public static EmiIngredient of(FluidStack[] stacks) {
        List<EmiStack> variants = new ArrayList<>(stacks.length);
        for (FluidStack stack : stacks) {
            if (!stack.isEmpty()) {
                variants.add(EmiStack.of(stack.getFluid(), stack.getComponentsPatch(), stack.getAmount()));
            }
        }
        if (variants.isEmpty()) {
            return EmiStack.EMPTY;
        }
        return EmiIngredient.of(variants);
    }

    public static EmiIngredient of(FluidStack stack) {
        return stack.isEmpty() ? EmiStack.EMPTY : EmiStack.of(stack.getFluid(), stack.getComponentsPatch(), stack.getAmount());
    }

    /** The single stack form, for a page's declared outputs — {@code BasicEmiRecipe.outputs} only holds EmiStacks. */
    public static EmiStack stack(FluidStack stack) {
        return stack.isEmpty() ? EmiStack.EMPTY : EmiStack.of(stack.getFluid(), stack.getComponentsPatch(), stack.getAmount());
    }

    public static EmiIngredient of(FluidIngredient ingredient) {
        return of(ingredient.getStacks());
    }

    public static EmiIngredient of(SizedFluidIngredient ingredient) {
        return of(ingredient.getFluids());
    }

    /** IE's rect is the inner area and its art draws the frame outside it, so the fluid is grown by a pixel. */
    public static SlotWidget add(WidgetHolder widgets, EmiIngredient fluid, int jeiX, int jeiY, int jeiWidth,
                                 int jeiHeight, int capacity) {
        return addExact(widgets, fluid, jeiX - 1, jeiY - 1, jeiWidth + 2, jeiHeight + 2, capacity);
    }

    /** Explicit bounds: TankWidget insets a pixel on every side, so callers pass the art's fluid area grown by 1. */
    public static SlotWidget addExact(WidgetHolder widgets, EmiIngredient fluid, int x, int y, int width, int height,
                                      int capacity) {
        return widgets.addTank(fluid, x, y, width, height, capacity).drawBack(false);
    }

    /**
     * The fluid form of {@code GeneratedSlotWidget}: two tanks sharing a {@code unique} roll the same value, which keeps a
     * collapsed page's fluid in step with its output. EMI has no fluid variant, so the seeding is repeated here.
     */
    public static SlotWidget addGenerated(WidgetHolder widgets, Function<Random, EmiIngredient> supplier, int unique,
                                          int jeiX, int jeiY, int jeiWidth, int jeiHeight, int capacity) {
        return widgets.add(new GeneratedTank(supplier, unique, jeiX - 1, jeiY - 1, jeiWidth + 2, jeiHeight + 2,
                capacity)).drawBack(false);
    }

    private static final class GeneratedTank extends TankWidget {
        private static final int INCREMENT = 1000;

        private final Function<Random, EmiIngredient> supplier;
        private final int unique;
        private long lastRoll;
        private EmiIngredient rolled;

        private GeneratedTank(Function<Random, EmiIngredient> supplier, int unique, int x, int y, int width, int height,
                              int capacity) {
            super(EmiStack.EMPTY, x, y, width, height, capacity);
            this.supplier = supplier;
            this.unique = unique;
        }

        @Override
        public EmiIngredient getStack() {
            long time = System.currentTimeMillis() / INCREMENT;
            if (this.rolled == null || time > this.lastRoll) {
                this.lastRoll = time;
                this.rolled = this.supplier.apply(new Random(new Random(time ^ this.unique).nextInt()));
            }
            return this.rolled;
        }
    }

/** The tank socket {@code GuiHelper.drawSlot} draws, in the same two-pixel border and grey interior. */
    public static void socket(WidgetHolder widgets, int jeiX, int jeiY, int width, int height) {
        widgets.addDrawable(jeiX, jeiY, width, height, (graphics, mouseX, mouseY, delta) -> {
            graphics.fill(0, -1, width, 0, RIM_DARK);
            graphics.fill(-1, -1, 0, height, RIM_DARK);
            graphics.fill(0, 0, width, height, RIM_MAIN);
            graphics.fill(0, height, width + 1, height + 1, RIM_LIGHT);
            graphics.fill(width, 0, width + 1, height, RIM_LIGHT);
        });
    }
}
