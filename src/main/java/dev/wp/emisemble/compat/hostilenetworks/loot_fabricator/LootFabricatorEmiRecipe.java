package dev.wp.emisemble.compat.hostilenetworks.loot_fabricator;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;

/** One loot fabricator page: a prediction in, one of the model's drops out, with the machine's progress bar between. */
public final class LootFabricatorEmiRecipe extends BasicEmiRecipe {
    private final EmiStack prediction;
    private final EmiStack drop;

    public LootFabricatorEmiRecipe(EmiRecipeCategory category, ResourceLocation id, DataModel model, int index) {
        super(category, id, LootFabricatorCategory.WIDTH, LootFabricatorCategory.HEIGHT);
        this.prediction = EmiStack.of(model.getPredictionDrop().copy());
        this.drop = EmiStack.of(model.fabDrops().get(index).copy());
        inputs.add(prediction);
        outputs.add(drop);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, LootFabricatorCategory.TEXTURE, 256, 0, 0,
                LootFabricatorCategory.WIDTH, LootFabricatorCategory.HEIGHT, 0, 0);
        AlphaTextures.progress(widgets, LootFabricatorCategory.TEXTURE, 256, LootFabricatorCategory.BAR_X,
                LootFabricatorCategory.BAR_Y, LootFabricatorCategory.BAR_WIDTH, LootFabricatorCategory.BAR_HEIGHT, 0,
                LootFabricatorCategory.BAR_V, LootFabricatorCategory.BAR_PERIOD);
        RecipeSlots.add(widgets, prediction, LootFabricatorCategory.INPUT_X, LootFabricatorCategory.INPUT_Y);
        RecipeSlots.add(widgets, drop, LootFabricatorCategory.OUTPUT_X, LootFabricatorCategory.OUTPUT_Y)
                .recipeContext(this);
    }
}
