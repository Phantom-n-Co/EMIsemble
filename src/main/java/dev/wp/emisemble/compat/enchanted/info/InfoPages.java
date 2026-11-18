package dev.wp.emisemble.compat.enchanted.info;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.InfoText;
import net.favouriteless.enchanted.common.init.EItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;

/** The six items the host gives an ingredient info page, one page each so no two texts share an id. */
public final class InfoPages {
    private InfoPages() {}

    public static void register(EmiRegistry registry) {
        int added = 0;
        added += page(registry, EItems.CHALICE_FILLED.get(), "jei.enchanted.chalice_filled");
        added += page(registry, EItems.WITCH_CAULDRON.get(), "jei.enchanted.witch_cauldron");
        added += page(registry, EItems.FUME_FUNNEL_FILTERED.get(), "jei.enchanted.fume_funnel_filtered");
        added += page(registry, EItems.WOOL_OF_BAT.get(), "jei.enchanted.arthana.bat");
        added += page(registry, EItems.TONGUE_OF_DOG.get(), "jei.enchanted.arthana.wolf");
        added += page(registry, EItems.CREEPER_HEART.get(), "jei.enchanted.arthana.creeper");

        Emisemble.LOGGER.info("enchanted: {} info pages registered", added);
    }

    private static int page(EmiRegistry registry, ItemLike item, String translationKey) {
        ItemStack stack = new ItemStack(item);
        ResourceLocation path = BuiltInRegistries.ITEM.getKey(stack.getItem());
        ResourceLocation id = EmiRecipes.synthetic(
                ResourceLocation.fromNamespaceAndPath(path.getNamespace(), "info/" + path.getPath()));

        EmiInfoRecipe recipe = new EmiInfoRecipe(List.of(EmiStack.of(stack)),
                InfoText.lines(Component.translatable(translationKey)), id);
        return EmiRecipes.add(registry, recipe) ? 1 : 0;
    }
}
