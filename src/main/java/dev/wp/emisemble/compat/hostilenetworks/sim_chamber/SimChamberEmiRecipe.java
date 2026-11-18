package dev.wp.emisemble.compat.hostilenetworks.sim_chamber;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.ModelTier;
import dev.shadowsoffire.hostilenetworks.data.ModelTierRegistry;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.util.List;

/** One sim chamber page: model and input in, base drop and a prediction out, with the tier the host cycles beside them. */
public final class SimChamberEmiRecipe extends BasicEmiRecipe {
    private static final int TIER_TICKS = 50;
    private static final int LABEL_CENTER = 33;
    private static final int ACCURACY_RIGHT = 114;
    private static final DecimalFormat ACCURACY = new DecimalFormat("##.##%");

    private final DataModel model;
    private final List<ModelTier> tiers;
    private final EmiIngredient input;
    private final EmiStack baseDrop;
    private final EmiStack prediction;

    public SimChamberEmiRecipe(EmiRecipeCategory category, ResourceLocation id, DataModel model,
                               List<ModelTier> tiers) {
        super(category, id, SimChamberCategory.WIDTH, SimChamberCategory.HEIGHT);
        this.model = model;
        this.tiers = tiers;
        this.input = EmiIngredient.of(model.input());
        this.baseDrop = EmiStack.of(model.baseDrop().copy());
        this.prediction = EmiStack.of(model.getPredictionDrop().copy());

        inputs.add(EmiIngredient.of(List.of(EmiStack.of(modelStack(model)))));
        if (!input.isEmpty()) {
            inputs.add(input);
        }
        if (!baseDrop.isEmpty()) {
            outputs.add(baseDrop);
        }
        if (!prediction.isEmpty()) {
            outputs.add(prediction);
        }
    }

    /** The host cycles only the tiers that can simulate, skipping the faulty one it starts on. */
    static List<ModelTier> simTiers() {
        return ModelTierRegistry.getSortedTiers().stream().filter(ModelTier::canSim).toList();
    }

    /** The model as the index and this page's input carry it: data 0, the faulty tier the host starts from. */
    public static ItemStack modelStack(DataModel model) {
        ItemStack stack = new ItemStack(Hostile.Items.DATA_MODEL);
        DataModelItem.setStoredModel(stack, model);
        DataModelItem.setData(stack, 0);
        return stack;
    }

    private static ItemStack modelStack(DataModel model, ModelTier tier) {
        ItemStack stack = modelStack(model);
        DataModelItem.setData(stack, model.getRequiredData(tier));
        return stack;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, SimChamberCategory.TEXTURE, 256, 0, 0,
                SimChamberCategory.WIDTH, SimChamberCategory.HEIGHT, 0, 0);
        AlphaTextures.progress(widgets, SimChamberCategory.TEXTURE, 256, SimChamberCategory.BAR_X,
                SimChamberCategory.BAR_Y, SimChamberCategory.BAR_WIDTH, SimChamberCategory.BAR_HEIGHT, 0,
                SimChamberCategory.BAR_V, SimChamberCategory.BAR_PERIOD);
        widgets.add(new TierSlot(this, SimChamberCategory.MODEL_X, SimChamberCategory.MODEL_Y).drawBack(false));
        RecipeSlots.add(widgets, input, SimChamberCategory.INPUT_X, SimChamberCategory.INPUT_Y);
        if (!baseDrop.isEmpty()) {
            RecipeSlots.add(widgets, baseDrop, SimChamberCategory.DROP_X, SimChamberCategory.DROP_Y)
                    .recipeContext(this);
        }
        if (!prediction.isEmpty()) {
            RecipeSlots.add(widgets, prediction, SimChamberCategory.PREDICTION_X, SimChamberCategory.PREDICTION_Y)
                    .recipeContext(this);
        }
        // Full-page bounds: EMI translates a drawable to its own origin, so the label draws in page coordinates like the host.
        widgets.addDrawable(0, 0, SimChamberCategory.WIDTH, SimChamberCategory.HEIGHT,
                (draw, mouseX, mouseY, delta) -> {
                    ModelTier tier = tier(now());
                    Font font = Minecraft.getInstance().font;
                    Component name = tier.getComponent();
                    draw.drawString(font, name, LABEL_CENTER - font.width(name) / 2, SimChamberCategory.LABEL_Y,
                            tier.colorValue());
                    String accuracy = ACCURACY.format(tier.accuracy());
                    draw.drawString(font, accuracy, ACCURACY_RIGHT - font.width(accuracy),
                            SimChamberCategory.LABEL_Y, 0xFFFFFF, true);
                });
    }

    /** One tier every 50 ticks, wrapping; the host's counter read off the level clock so every page stays in step. */
    private ModelTier tier(long ticks) {
        return tiers.get((int) (ticks / TIER_TICKS % tiers.size()));
    }

    private static long now() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? 0L : level.getGameTime();
    }

    /** The model stack carries the data of the tier the label names, the two halves the host keeps in lockstep. */
    private static final class TierSlot extends SlotWidget {
        private final SimChamberEmiRecipe page;
        private ModelTier shown;
        private EmiStack shownStack = EmiStack.EMPTY;

        TierSlot(SimChamberEmiRecipe page, int jeiX, int jeiY) {
            super(EmiStack.EMPTY, jeiX - 1, jeiY - 1);
            this.page = page;
        }

        @Override
        public EmiIngredient getStack() {
            ModelTier tier = page.tier(now());
            if (tier != shown) {
                shown = tier;
                shownStack = EmiStack.of(modelStack(page.model, tier));
            }
            return shownStack;
        }
    }
}
