package dev.wp.emisemble.compat.hostilenetworks.loot_fabricator;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.hostilenetworks.HostileNeuralNetworksCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** {@code hostilenetworks:loot_fabricator}: one page per drop a model can fabricate. */
public final class LootFabricatorCategory {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("hostilenetworks", "textures/jei/loot_fabricator.png");
    static final int WIDTH = 103;
    static final int HEIGHT = 30;

    static final int INPUT_X = 9;
    static final int INPUT_Y = 7;
    static final int OUTPUT_X = 79;
    static final int OUTPUT_Y = 7;
    static final int BAR_X = 34;
    static final int BAR_Y = 12;
    static final int BAR_WIDTH = 36;
    static final int BAR_HEIGHT = 6;
    static final int BAR_V = 30;
    static final int BAR_PERIOD = 40;

    private static final EmiStack ICON = EmiStack.of(new ItemStack(Hostile.Items.LOOT_FABRICATOR));

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            HostileNeuralNetworksCompat.categoryId("loot_fabricator"), ICON,
            Hostile.Blocks.LOOT_FABRICATOR.value().getDescriptionId());

    private LootFabricatorCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, EmiStack.of(new ItemStack(Hostile.Blocks.LOOT_FABRICATOR.value())));
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("hostilenetworks: could not register the loot fabricator category", t);
            return 0;
        }

        int added = 0;
        for (DataModel model : List.copyOf(DataModelRegistry.INSTANCE.getValues())) {
            try {
                ResourceLocation modelId = DataModelRegistry.INSTANCE.getKey(model);
                List<ItemStack> drops = model.fabDrops();
                for (int i = 0; i < drops.size(); i++) {
                    if (drops.get(i).isEmpty()) {
                        continue;
                    }
                    // A model id carries its own namespace, which the page path has to keep to stay unique.
                    ResourceLocation id = EmiRecipes.pageId(HostileNeuralNetworksCompat.categoryId(
                            "loot_fabricator/" + modelId.getNamespace() + "/" + modelId.getPath() + "/" + i));
                    if (EmiRecipes.add(registry, new LootFabricatorEmiRecipe(CATEGORY, id, model, i))) {
                        added++;
                    }
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("hostilenetworks: skipping the loot fabricator pages of a model", t);
            }
        }
        Emisemble.LOGGER.info("hostilenetworks: {} loot fabricator recipes", added);
        return added;
    }
}
