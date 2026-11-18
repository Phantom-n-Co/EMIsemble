package dev.wp.emisemble.compat.thaumaturge.dust;

import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSimpleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerTagRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.thaumaturge.ThaumaturgeCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/** Salis mundus onto a block: no page texture, just the host's result plate and two doubled glyphs. */
public final class DustTriggerEmiRecipe extends BasicEmiRecipe {
    private static final int WIDTH = 144;
    private static final int HEIGHT = 54;

    // EMI-space: JEI draws at 7,19 and EMI puts the item at origin + 1, so both axes drop by one.
    private static final int DUST_X = 6;
    private static final int DUST_Y = 18;
    private static final int TARGET_X = 56;
    private static final int TARGET_Y = 18;
    private static final int RESULT_X = 118;
    private static final int RESULT_Y = 18;
    private static final int PLATE_X = 112;
    private static final int PLATE_Y = 12;
    private static final int PLATE_SIZE = 30;
    private static final int PLATE_U = 41;
    private static final int PLATE_V = 7;
    private static final int LOCK_X = 87;
    private static final int LOCK_Y = 20;

    private static final Component USAGE = Component.translatable("jei.thaumaturge.dust_trigger.usage");
    // JEI draws the glyphs through a pose scaled by 2, dark grey and without a shadow.
    private static final int GLYPH_COLOUR = 0xFF000000 | ChatFormatting.DARK_GRAY.getColor();

    private final DustTrigger recipe;
    private final EmiIngredient target;
    private final ItemStack result;
    private final Component targetNote;

    public DustTriggerEmiRecipe(RecipeHolder<DustTrigger> holder) {
        super(DustTriggerCompat.DUST_TRIGGER, holder.id(), WIDTH, HEIGHT);
        this.recipe = holder.value();
        this.target = target(this.recipe);
        this.result = DustTriggerCompat.result(this.recipe);
        this.targetNote = note(this.recipe);

        inputs.add(DustTriggerCompat.salisMundus());
        if (!target.isEmpty()) {
            catalysts.add(target);
        }
        if (!result.isEmpty()) {
            outputs.add(EmiStack.of(result));
        }
    }

    private static EmiIngredient target(DustTrigger recipe) {
        if (recipe instanceof DustTriggerSimpleRecipe simple) {
            ItemStack stack = new ItemStack(simple.target());
            return stack.isEmpty() ? EmiStack.EMPTY : EmiStack.of(stack);
        }
        if (recipe instanceof DustTriggerTagRecipe tag) {
            List<EmiStack> stacks = new ArrayList<>();
            for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag.targetTag())) {
                ItemStack stack = new ItemStack(holder.value());
                if (!stack.isEmpty()) {
                    stacks.add(EmiStack.of(stack));
                }
            }
            return stacks.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(stacks);
        }
        return EmiStack.EMPTY;
    }

    /** A block tag has no stack of its own, so the host names it in the target's tooltip. */
    private static Component note(DustTrigger recipe) {
        if (recipe instanceof DustTriggerTagRecipe tag) {
            return Component.translatable("jei.thaumaturge.dust_trigger.target.tag",
                    Component.literal("#" + tag.targetTag().location()));
        }
        return null;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        ThaumaturgeCompat.art(widgets, PLATE_X, PLATE_Y, PLATE_SIZE, PLATE_SIZE, PLATE_U, PLATE_V);

        widgets.addDrawable(0, 0, WIDTH, HEIGHT, (draw, mouseX, mouseY, delta) -> {
            draw.pose().pushPose();
            draw.pose().scale(2.0F, 2.0F, 2.0F);
            draw.drawString(Minecraft.getInstance().font, "+", 17, 10, GLYPH_COLOUR, false);
            draw.drawString(Minecraft.getInstance().font, "=", 45, 10, GLYPH_COLOUR, false);
            draw.pose().popPose();
        });

        widgets.addSlot(DustTriggerCompat.salisMundus(), DUST_X, DUST_Y).drawBack(false).appendTooltip(USAGE);
        if (!target.isEmpty()) {
            SlotWidget slot = widgets.addSlot(target, TARGET_X, TARGET_Y).drawBack(false);
            if (targetNote != null) {
                slot.appendTooltip(targetNote);
            }
        }
        if (!result.isEmpty()) {
            widgets.addSlot(EmiStack.of(result), RESULT_X, RESULT_Y).drawBack(false).recipeContext(this);
        }

        if (!recipe.doesPassGate(Minecraft.getInstance().player)) {
            widgets.addDrawable(LOCK_X, LOCK_Y, 16, 16, (draw, mouseX, mouseY, delta) ->
                    draw.renderItem(Items.BARRIER.getDefaultInstance(), 0, 0));
        }
        widgets.addTooltip(this::tooltip, LOCK_X, LOCK_Y, 18, 16);
    }

    private List<ClientTooltipComponent> tooltip(int mouseX, int mouseY) {
        if (recipe.doesPassGate(Minecraft.getInstance().player)) {
            return List.of();
        }
        return recipe.researchGate()
                .map(gate -> EmiRecipes.tooltip(ThaumaturgeCompat.missingResearch(gate)))
                .orElse(List.of());
    }
}
