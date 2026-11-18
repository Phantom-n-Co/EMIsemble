package dev.wp.emisemble.compat.enigmaticlegacyplus.tainting;

import auviotre.enigmatic.legacy.api.item.ITaintable;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** One tainting page, 108x42: the heart in, the same heart with the taintable component set, the ring consumed. */
public final class TaintingEmiRecipe extends BasicEmiRecipe {
    private static final int WIDTH = 108;
    private static final int HEIGHT = 42;

    private static final ResourceLocation SPRITE =
            ResourceLocation.fromNamespaceAndPath(TaintingCategory.NAMESPACE, "jei/tainting_gui");

    private static final int INPUT_X = 13;
    private static final int INPUT_Y = 13;
    private static final int OUTPUT_X = 79;
    private static final int OUTPUT_Y = 13;
    private static final int CATALYST_X = 46;
    private static final int CATALYST_Y = 23;

    private final EmiStack input;
    private final EmiIngredient condition;
    private final EmiStack output;

    TaintingEmiRecipe(ResourceLocation id, EmiStack input, Ingredient condition) {
        super(TaintingCategory.CATEGORY, id, WIDTH, HEIGHT);
        this.input = input;
        this.condition = EmiIngredient.of(condition);
        this.output = EmiStack.of(tainted(input.getItemStack()));

        inputs.add(this.input);
        catalysts.add(this.condition);
        outputs.add(this.output);
    }

    /** The output differs from the input only in the component, which is what makes it a distinct stack. */
    private static ItemStack tainted(ItemStack input) {
        ItemStack output = input.copy();
        if (output.getItem() instanceof ITaintable) {
            output.set(EnigmaticComponents.TAINTABLE.get(), true);
        }
        return output;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // The host's page art is one sprite-atlas blit, so it is already blended and needs no AlphaTextures frame.
        widgets.addDrawable(0, 0, WIDTH, HEIGHT,
                (draw, mouseX, mouseY, delta) -> draw.blitSprite(SPRITE, 0, 0, WIDTH, HEIGHT));

        // The host asks JEI for a standard background on these two only, so the catalyst slot stays bare.
        RecipeSlots.addFramed(widgets, input, INPUT_X, INPUT_Y);
        RecipeSlots.add(widgets, condition, CATALYST_X, CATALYST_Y);
        RecipeSlots.addFramed(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
    }
}
