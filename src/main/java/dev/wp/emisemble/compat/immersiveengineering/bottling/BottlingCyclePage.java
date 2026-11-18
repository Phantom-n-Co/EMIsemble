package dev.wp.emisemble.compat.immersiveengineering.bottling;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
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
 * One bottling page for a family that differs only in which fluid goes in, cycling the tank and the filled container
 * together: IE generates a recipe per potion conversion, and the bucket pages come from the fluid registry.
 */
public final class BottlingCyclePage extends BasicEmiRecipe {
    private final List<Variant> variants;
    private final List<EmiIngredient> itemInputs;
    private final int tankCapacity;
    private final int unique;

    private record Variant(EmiIngredient input, EmiStack output) {}

    private BottlingCyclePage(EmiRecipeCategory category, ResourceLocation id, List<Variant> variants,
            List<EmiIngredient> itemInputs, int tankCapacity) {
        super(category, id, BottlingCategory.WIDTH, BottlingCategory.HEIGHT);
        // EMI re-seeds a generated slot from (second ^ unique); one value per page is what keeps the tank and slot in step.
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

/** The family key of a generated potion bottling recipe, or {@code null} for a recipe that stands alone. */
    static String shape(BottlingMachineRecipe recipe) {
        if (recipe.fluidInput == null) {
            return null;
        }
        boolean potion = false;
        StringBuilder key = new StringBuilder();
        for (FluidStack input : recipe.fluidInput.getFluids()) {
            potion |= FluidShapes.has(input, DataComponents.POTION_CONTENTS);
            key.append('>').append(FluidShapes.key(input, DataComponents.POTION_CONTENTS));
        }
        // One filled container per variant: a family member with two would need two slots that cycle apart.
        if (!potion || recipe.output.get().size() != 1) {
            return null;
        }
        for (IngredientWithSize item : recipe.inputs) {
            key.append('|').append(item.getCount()).append('x');
            for (ItemStack stack : item.getBaseIngredient().getItems()) {
                key.append(BuiltInRegistries.ITEM.getKey(stack.getItem())).append(',');
            }
        }
        return key.toString();
    }

    static BottlingCyclePage of(EmiRecipeCategory category, ResourceLocation id, List<BottlingMachineRecipe> family) {
        List<Variant> variants = new ArrayList<>(family.size());
        int capacity = 0;
        for (BottlingMachineRecipe recipe : family) {
            List<EmiStack> outputs = BottlingEmiRecipe.itemOutputs(recipe);
            if (outputs.size() != 1) {
                continue;
            }
            variants.add(new Variant(BottlingEmiRecipe.fluidInput(recipe), outputs.get(0)));
            capacity = Math.max(capacity, BottlingEmiRecipe.tankCapacity(recipe));
        }
        return new BottlingCyclePage(category, id, variants, BottlingEmiRecipe.itemInputs(family.get(0)), capacity);
    }

    // The only draw either widget makes: same unique and same second means the fluid in is the container out.
    private Variant variant(Random random) {
        return this.variants.get(random.nextInt(this.variants.size()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        BottlingCategory.machine(widgets);

        FluidTanks.socket(widgets, BottlingEmiRecipe.TANK_X, BottlingEmiRecipe.TANK_Y,
                BottlingEmiRecipe.TANK_WIDTH, BottlingEmiRecipe.TANK_HEIGHT);
        FluidTanks.addGenerated(widgets, random -> variant(random).input(), this.unique, BottlingEmiRecipe.TANK_X,
                BottlingEmiRecipe.TANK_Y, BottlingEmiRecipe.TANK_WIDTH, BottlingEmiRecipe.TANK_HEIGHT,
                this.tankCapacity);

        int inputY = BottlingEmiRecipe.rowY(this.itemInputs.size());
        for (int i = 0; i < this.itemInputs.size(); i++) {
            RecipeSlots.addFramed(widgets, this.itemInputs.get(i), BottlingEmiRecipe.INPUT_X,
                    inputY + i * BottlingEmiRecipe.ROW_SPACING);
        }

        // One output slot for the whole family, so it has to be the generated form to follow the tank.
        RecipeSlots.addGenerated(widgets, random -> variant(random).output(), this.unique, BottlingEmiRecipe.OUTPUT_X,
                BottlingEmiRecipe.rowY(1)).recipeContext(this);
    }
}
