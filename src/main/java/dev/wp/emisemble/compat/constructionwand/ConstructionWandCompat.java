package dev.wp.emisemble.compat.constructionwand;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.polaris_light.constructionwand.basics.ConfigClient;
import dev.polaris_light.constructionwand.basics.ConfigServer;
import dev.polaris_light.constructionwand.client.KeybindHandler;
import dev.polaris_light.constructionwand.items.ModItems;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.InfoText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

/** Construction Wand's two info call sites: one page per wand, one per core. */
public final class ConstructionWandCompat {
    private ConstructionWandCompat() {}

    static void register(EmiRegistry registry) {
        // The keybind is a client component and the two shift-mode flags come from the client config, as in the host.
        Component optKey = KeybindHandler.KEY_OPT.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.BLUE);
        Component mode = keyCombo(ConfigClient.SHIFTOPT_MODE.get(), optKey);
        Component gui = keyCombo(ConfigClient.SHIFTOPT_GUI.get(), optKey);

        int added = 0;
        for (DeferredHolder<Item, Item> holder : ModItems.WANDS) {
            added += page(registry, holder.get(), wandText(holder.get(), optKey, mode, gui));
        }
        for (DeferredHolder<Item, Item> holder : ModItems.CORES) {
            added += page(registry, holder.get(), coreText(holder.get(), mode));
        }
        Emisemble.LOGGER.info("constructionwand: {} info pages registered", added);
    }

    // The wand's name, block limit and durability are format arguments, so the four wands cannot share one page.
    private static Component wandText(Item wand, Component optKey, Component mode, Component gui) {
        ConfigServer.WandProperties properties = ConfigServer.getWandProperties(wand);
        String durability = wand == ModItems.WAND_INFINITY.get() ? "unlimited" : "limited";
        return Component.translatable("constructionwand.description.wand",
                Component.translatable("item.constructionwand." + path(wand)),
                properties.getLimit(),
                Component.translatable("constructionwand.description.durability." + durability,
                        properties.getDurability()),
                optKey, mode, gui);
    }

    // The host appends the shared installation paragraph to each core's own name, never the core's own description.
    private static Component coreText(Item core, Component mode) {
        return Component.translatable("item.constructionwand." + path(core))
                .append("\n\n")
                .append(Component.translatable("constructionwand.description.core", mode));
    }

    private static int page(EmiRegistry registry, Item item, Component text) {
        return EmiRecipes.add(registry, new EmiInfoRecipe(
                List.<EmiIngredient>of(EmiStack.of(item)),
                InfoText.lines(text),
                ResourceLocation.fromNamespaceAndPath("constructionwand", "/info/" + path(item)))) ? 1 : 0;
    }

    private static Component keyCombo(boolean shift, Component optKey) {
        return Component.translatable("constructionwand.description.key." + (shift ? "sneak_opt" : "opt"), optKey)
                .withStyle(ChatFormatting.BLUE);
    }

    private static String path(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }
}
