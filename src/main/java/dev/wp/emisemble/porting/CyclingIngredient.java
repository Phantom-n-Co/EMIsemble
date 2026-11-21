package dev.wp.emisemble.porting;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * An ingredient whose options carry their own counts: EMI folds a list of ingredients into one amount, so options that
 * need different counts all render as the largest of them.
 */
public final class CyclingIngredient implements EmiIngredient {
    private final List<EmiIngredient> options;
    private final List<EmiStack> emiStacks;
    private final long amount;
    private float chance = 1;

    public CyclingIngredient(List<? extends EmiIngredient> options) {
        this.options = List.copyOf(options);
        List<EmiStack> stacks = new ArrayList<>();
        long largest = 1;
        for (EmiIngredient option : options) {
            stacks.addAll(option.getEmiStacks());
            largest = Math.max(largest, option.getAmount());
        }
        this.emiStacks = List.copyOf(stacks);
        this.amount = largest;
    }

    private EmiIngredient current() {
        return options.get((int) (System.currentTimeMillis() / 1000 % options.size()));
    }

    @Override
    public List<EmiStack> getEmiStacks() {
        return emiStacks;
    }

    @Override
    public EmiIngredient copy() {
        CyclingIngredient copy = new CyclingIngredient(options);
        copy.chance = chance;
        return copy;
    }

    // The largest option: EMI's batch arithmetic must not under-count a recipe that takes the priciest one.
    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public EmiIngredient setAmount(long amount) {
        return this;
    }

    @Override
    public float getChance() {
        return chance;
    }

    @Override
    public EmiIngredient setChance(float chance) {
        this.chance = chance;
        return this;
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        current().render(draw, x, y, delta, flags);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        return current().getTooltip();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CyclingIngredient other && other.emiStacks.equals(this.emiStacks);
    }

    @Override
    public int hashCode() {
        return emiStacks.hashCode();
    }
}
