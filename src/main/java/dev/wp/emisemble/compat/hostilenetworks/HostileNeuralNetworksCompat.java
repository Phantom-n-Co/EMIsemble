package dev.wp.emisemble.compat.hostilenetworks;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.hostilenetworks.loot_fabricator.LootFabricatorCategory;
import dev.wp.emisemble.compat.hostilenetworks.sim_chamber.SimChamberCategory;
import dev.wp.emisemble.compat.hostilenetworks.sim_chamber.SimChamberEmiRecipe;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;
import java.util.List;

/** Both categories the host registers with JEI, plus its model subtypes and its ingredient info. */
public final class HostileNeuralNetworksCompat {
    private static final String NAMESPACE = "hostilenetworks";

    private HostileNeuralNetworksCompat() {}

    /** The host gives its JEI categories their own uids — nothing here is a registered recipe type. */
    public static ResourceLocation categoryId(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    public static void register(EmiRegistry registry) {
        int added = SimChamberCategory.register(registry);
        added += LootFabricatorCategory.register(registry);
        registerSubtypes(registry);
        int info = registerInfo(registry);
        Emisemble.LOGGER.info("{}: {} recipes registered, {} info pages", NAMESPACE, added, info);
    }

    /**
     * The host's subtype interpreter reads the stored model and ignores the data counter, so a half-trained model still
     * finds its pages; both items are creative entries, so EMI has already indexed one stack per model.
     */
    private static void registerSubtypes(EmiRegistry registry) {
        Comparison byModel = Comparison.compareData(HostileNeuralNetworksCompat::storedModel);
        registry.setDefaultComparison(Hostile.Items.DATA_MODEL.value(), byModel);
        registry.setDefaultComparison(Hostile.Items.PREDICTION.value(), byModel);

        List<EmiStack> indexed = EmiApi.getIndexStacks();
        int added = 0;
        int existing = 0;
        for (DataModel model : List.copyOf(DataModelRegistry.INSTANCE.getValues())) {
            for (ItemStack stack : List.of(SimChamberEmiRecipe.modelStack(model), model.getPredictionDrop().copy())) {
                EmiStack emi = EmiStack.of(stack);
                if (emi.isEmpty()) {
                    continue;
                }
                if (indexed.stream().anyMatch(indexedStack -> indexedStack.isEqual(emi))) {
                    existing++;
                    continue;
                }
                registry.addEmiStack(emi);
                added++;
            }
        }
        Emisemble.LOGGER.info("{}: {} model variants added, {} already indexed", NAMESPACE, added, existing);
    }

    private static int registerInfo(EmiRegistry registry) {
        return info(registry, "data_center", Hostile.Blocks.DATA_CENTER.value(),
                "hostilenetworks.jei.data_center.intro", "hostilenetworks.jei.data_center.shell",
                "hostilenetworks.jei.data_center.operation", "hostilenetworks.jei.data_center.io_port")
                + info(registry, "data_center_io_port", Hostile.Blocks.DATA_CENTER_IO_PORT.value(),
                "hostilenetworks.jei.io_port.intro", "hostilenetworks.jei.io_port.modes");
    }

    /** JEI is handed these a component at a time and pages them; one page per item reads better and keeps the text. */
    private static int info(EmiRegistry registry, String path, ItemLike item, String... keys) {
        List<Component> text = Arrays.stream(keys).<Component>map(Component::translatable).toList();
        return EmiRecipes.add(registry, new EmiInfoRecipe(
                List.<EmiIngredient>of(EmiStack.of(item)), text, EmiRecipes.synthetic(categoryId("info/" + path)))) ? 1 : 0;
    }

    private static String storedModel(EmiStack stack) {
        DynamicHolder<DataModel> model = DataModelItem.getStoredModel(stack.getItemStack());
        return model.isBound() ? model.getId().toString() : "";
    }
}
