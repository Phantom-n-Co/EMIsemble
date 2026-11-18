package dev.wp.emisemble.compat.immersiveengineering.mixer;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.FluidShapes;
import dev.wp.emisemble.porting.FluidTanks;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * One mixer page for a whole family of generated potion mixes, cycling the potion it consumes and the potion it
 * makes.
 */
public final class MixerPotionPage extends BasicEmiRecipe {
    private final List<Variant> variants;
    private final List<EmiIngredient> itemInputs;
    private final int tankCapacity;
    private final int unique;

    private record Variant(EmiIngredient input, EmiStack output) {}

    private MixerPotionPage(EmiRecipeCategory category, ResourceLocation id, List<Variant> variants,
            List<EmiIngredient> itemInputs, int tankCapacity) {
        super(category, id, MixerCategory.WIDTH, MixerCategory.HEIGHT);
        // EMI re-seeds a generated slot from (second ^ unique); one value per page is what keeps the two tanks in step.
        this.unique = id.hashCode();
        this.variants = List.copyOf(variants);
        this.itemInputs = List.copyOf(itemInputs);
        this.tankCapacity = tankCapacity;

        for (Variant variant : this.variants) {
            inputs.add(variant.input());
            outputs.add(variant.output());
        }
        inputs.addAll(this.itemInputs);
    }

/**
 * The family key of a generated potion mix, or {@code null} for a mix that carries no potion and stands alone. The
 * potion is blanked out of both fluids, so what is left is the part every member of the family shares.
 */
    static String shape(MixerRecipe recipe) {
        FluidStack output = recipe.fluidOutput;
        if (output == null || output.isEmpty()) {
            return null;
        }
        boolean potion = FluidShapes.has(output, DataComponents.POTION_CONTENTS);
        StringBuilder key = new StringBuilder(FluidShapes.key(output, DataComponents.POTION_CONTENTS));
        if (recipe.fluidInput != null) {
            for (FluidStack input : recipe.fluidInput.getFluids()) {
                potion |= FluidShapes.has(input, DataComponents.POTION_CONTENTS);
                key.append('>').append(FluidShapes.key(input, DataComponents.POTION_CONTENTS));
            }
        }
        if (!potion) {
            return null;
        }
        for (IngredientWithSize item : recipe.itemInputs) {
            key.append('|').append(item.getCount()).append('x');
            for (ItemStack stack : item.getBaseIngredient().getItems()) {
                key.append(BuiltInRegistries.ITEM.getKey(stack.getItem())).append(',');
            }
        }
        return key.toString();
    }

    static MixerPotionPage of(EmiRecipeCategory category, ResourceLocation id, List<MixerRecipe> family) {
        List<Variant> variants = new ArrayList<>(family.size());
        int capacity = 0;
        for (MixerRecipe recipe : family) {
            variants.add(new Variant(MixerEmiRecipe.fluidInput(recipe), MixerEmiRecipe.fluidOutput(recipe)));
            capacity = Math.max(capacity, MixerEmiRecipe.tankCapacity(recipe));
        }
        return new MixerPotionPage(category, id, variants, MixerEmiRecipe.itemInputs(family.get(0)), capacity);
    }

    // The only draw either tank makes: same unique and same second means the potion in is the potion out.
    private Variant variant(Random random) {
        return this.variants.get(random.nextInt(this.variants.size()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        MixerCategory.page(widgets);
        FluidTanks.addGenerated(widgets, random -> variant(random).input(), this.unique, MixerEmiRecipe.FLUID_INPUT_X,
                MixerEmiRecipe.FLUID_INPUT_Y, MixerEmiRecipe.FLUID_INPUT_WIDTH, MixerEmiRecipe.FLUID_INPUT_HEIGHT,
                this.tankCapacity);
        FluidTanks.socket(widgets, MixerEmiRecipe.FLUID_OUTPUT_X, MixerEmiRecipe.FLUID_OUTPUT_Y,
                MixerEmiRecipe.FLUID_OUTPUT_WIDTH, MixerEmiRecipe.FLUID_OUTPUT_HEIGHT);
        FluidTanks.addGenerated(widgets, random -> variant(random).output(), this.unique, MixerEmiRecipe.FLUID_OUTPUT_X,
                MixerEmiRecipe.FLUID_OUTPUT_Y, MixerEmiRecipe.FLUID_OUTPUT_WIDTH, MixerEmiRecipe.FLUID_OUTPUT_HEIGHT,
                this.tankCapacity).recipeContext(this);

        for (int i = 0; i < this.itemInputs.size(); i++) {
            int x = MixerEmiRecipe.ITEM_ORIGIN_X + (i % MixerEmiRecipe.ITEM_COLUMNS) * MixerEmiRecipe.ITEM_SPACING;
            int y = MixerEmiRecipe.ITEM_ORIGIN_Y + (i / MixerEmiRecipe.ITEM_COLUMNS) * MixerEmiRecipe.ITEM_SPACING;
            RecipeSlots.addFramed(widgets, this.itemInputs.get(i), x, y);
        }
    }
}
