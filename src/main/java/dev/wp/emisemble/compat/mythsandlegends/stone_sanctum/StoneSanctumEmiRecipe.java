package dev.wp.emisemble.compat.mythsandlegends.stone_sanctum;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** The one Stone Sanctum ritual: the cursed core and the five gargoyle parts in, the effigy out. */
public final class StoneSanctumEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    private static final int[][] PART_POSITIONS = {{19, 25}, {75, 25}, {24, 45}, {47, 52}, {70, 45}};
    private static final int FUEL_X = 47;
    private static final int FUEL_Y = 11;
    private static final int OUTPUT_X = 139;
    private static final int OUTPUT_Y = 35;

    private final EmiStack fuel;
    private final List<EmiStack> parts;
    private final EmiStack output;

    public StoneSanctumEmiRecipe(EmiRecipeCategory category, ResourceLocation id, ItemStack fuel,
                                 List<ItemStack> parts, ItemStack output) {
        super(category, id, StoneSanctumCategory.WIDTH, StoneSanctumCategory.HEIGHT);
        this.fuel = EmiStack.of(fuel);
        this.parts = parts.stream().map(EmiStack::of).toList();
        this.output = EmiStack.of(output);
        inputs.add(this.fuel);
        inputs.addAll(this.parts);
        outputs.add(this.output);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, StoneSanctumCategory.TEXTURE, StoneSanctumCategory.SHEET, -4, -4,
                StoneSanctumCategory.WIDTH + 8, StoneSanctumCategory.HEIGHT + 8, 0, 0);
        // The art already draws every socket, so the slots land on it without chrome of their own.
        RecipeSlots.add(widgets, fuel, FUEL_X, FUEL_Y);
        for (int i = 0; i < parts.size() && i < PART_POSITIONS.length; i++) {
            RecipeSlots.add(widgets, parts.get(i), PART_POSITIONS[i][0], PART_POSITIONS[i][1]);
        }
        RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
    }
}
