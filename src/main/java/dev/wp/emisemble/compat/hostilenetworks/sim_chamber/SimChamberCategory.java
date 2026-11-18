package dev.wp.emisemble.compat.hostilenetworks.sim_chamber;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.shadowsoffire.hostilenetworks.data.ModelTier;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.hostilenetworks.HostileNeuralNetworksCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** {@code hostilenetworks:sim_chamber}: one page per model, its simulation cost and its two drops. */
public final class SimChamberCategory {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("hostilenetworks", "textures/jei/sim_chamber.png");
    static final int WIDTH = 116;
    static final int HEIGHT = 43;

    static final int MODEL_X = 4;
    static final int MODEL_Y = 4;
    static final int INPUT_X = 28;
    static final int INPUT_Y = 4;
    static final int DROP_X = 96;
    static final int DROP_Y = 4;
    static final int PREDICTION_X = 66;
    static final int PREDICTION_Y = 26;
    static final int BAR_X = 52;
    static final int BAR_Y = 9;
    static final int BAR_WIDTH = 35;
    static final int BAR_HEIGHT = 6;
    static final int BAR_V = 43;
    static final int BAR_PERIOD = 40;
    static final int LABEL_Y = 30;

    private static final EmiStack ICON = EmiStack.of(new ItemStack(Hostile.Items.SIM_CHAMBER));

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            HostileNeuralNetworksCompat.categoryId("sim_chamber"), ICON,
            Hostile.Blocks.SIM_CHAMBER.value().getDescriptionId());

    private SimChamberCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, EmiStack.of(new ItemStack(Hostile.Blocks.SIM_CHAMBER.value())));
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("hostilenetworks: could not register the sim chamber category", t);
            return 0;
        }

        List<ModelTier> tiers = SimChamberEmiRecipe.simTiers();
        if (tiers.isEmpty()) {
            Emisemble.LOGGER.warn("hostilenetworks: no sim-capable model tiers, skipping the sim chamber recipes");
            return 0;
        }

        int added = 0;
        for (DataModel model : List.copyOf(DataModelRegistry.INSTANCE.getValues())) {
            try {
                ResourceLocation modelId = DataModelRegistry.INSTANCE.getKey(model);
                ResourceLocation id = EmiRecipes.pageId(HostileNeuralNetworksCompat.categoryId(
                        "sim_chamber/" + modelId.getNamespace() + "/" + modelId.getPath()));
                if (EmiRecipes.add(registry, new SimChamberEmiRecipe(CATEGORY, id, model, tiers))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("hostilenetworks: skipping a sim chamber page", t);
            }
        }
        Emisemble.LOGGER.info("hostilenetworks: {} sim chamber recipes", added);
        return added;
    }
}
