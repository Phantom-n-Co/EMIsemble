package dev.wp.emisemble.compat.nutritional;

import com.breakinblocks.nutritional.common.InvertedNutrientIndex;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.datamap.NutrientScales;
import com.breakinblocks.nutritional.data.datamap.NutritionalDataMaps;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Nutritional's single info call: one page per food, its visible nutrients and their scales. */
public final class NutritionalCompat {
    private NutritionalCompat() {}

    static void register(EmiRegistry registry) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        // The host reads the nutrient registry from the connection and gives up without one.
        if (connection == null) {
            return;
        }
        Registry<NutrientDefinition> nutrients;
        try {
            nutrients = NutritionalDatapack.nutrients(connection.registryAccess());
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("nutritional: could not read the nutrient registry", t);
            return;
        }

        List<Item> items = sortedItems();
        if (items.isEmpty()) {
            // Registration can run before the host's join listener fills the index; rebuild it from the same access.
            InvertedNutrientIndex.rebuild(connection.registryAccess());
            items = sortedItems();
        }
        if (items.isEmpty()) {
            Emisemble.LOGGER.warn("nutritional: the nutrient index is empty, no info pages registered");
            return;
        }

        Map<String, Page> pages = new LinkedHashMap<>();
        int foods = 0;
        for (Item item : items) {
            try {
                List<Component> text = describe(item, nutrients);
                if (text == null) {
                    continue;
                }
                pages.computeIfAbsent(flat(text), key -> new Page(text)).stacks().add(EmiStack.of(item));
                foods++;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("nutritional: skipping the info page for " + item, t);
            }
        }

        int added = 0;
        for (Page page : pages.values()) {
            if (EmiRecipes.add(registry, new EmiInfoRecipe(
                    List.of(EmiIngredient.of(page.stacks())),
                    page.text(),
                    id(page.stacks().get(0))))) {
                added++;
            }
        }
        Emisemble.LOGGER.info("nutritional: {} info pages over {} foods", added, foods);
    }

    // The host's text: a dark-green header, then "Name (1.00x), Name (2.00x)" built from the food's own values.
    private static List<Component> describe(Item item, Registry<NutrientDefinition> nutrients) {
        List<ResourceLocation> ids = InvertedNutrientIndex.nutrientsFor(item);
        if (ids.isEmpty()) {
            return null;
        }
        NutrientScales scales = BuiltInRegistries.ITEM.wrapAsHolder(item).getData(NutritionalDataMaps.NUTRIENT_SCALES);
        StringBuilder list = new StringBuilder();
        for (ResourceLocation id : ids) {
            NutrientDefinition nutrient = nutrients.get(id);
            if (nutrient == null || !nutrient.visible()) {
                continue;
            }
            float scale = scales == null ? 1.0F : scales.scaleFor(id);
            if (!list.isEmpty()) {
                list.append(',');
            }
            list.append(Component.translatable(nutrientKey(id)).getString())
                    .append(" (")
                    .append(String.format("%.2fx", scale))
                    .append(')');
        }
        if (list.isEmpty()) {
            return null;
        }
        // Two components, not one flattened: InfoText.lines would turn the header into plain text.
        return List.of(Component.translatable("jei.nutritional.info.header").withStyle(ChatFormatting.DARK_GREEN),
                Component.literal(list.toString()));
    }

    private static String nutrientKey(ResourceLocation id) {
        return "nutrient." + id.getNamespace() + "." + id.getPath();
    }

    private static String flat(List<Component> text) {
        StringBuilder key = new StringBuilder();
        for (Component line : text) {
            key.append(line.getString()).append('\n');
        }
        return key.toString();
    }

    private static ResourceLocation id(EmiStack stack) {
        return ResourceLocation.fromNamespaceAndPath("nutritional", "/info/"
                + BuiltInRegistries.ITEM.getKey(stack.getItemStack().getItem()).getPath());
    }

    // The host's index is a hash map's key set, so sorting keeps the synthetic page ids stable between runs.
    private static List<Item> sortedItems() {
        List<Item> items = new ArrayList<>(InvertedNutrientIndex.items());
        items.sort(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));
        return items;
    }

    private record Page(List<Component> text, List<EmiStack> stacks) {
        Page(List<Component> text) {
            this(text, new ArrayList<>());
        }
    }
}
