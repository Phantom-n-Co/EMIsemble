package dev.wp.emisemble.compat.horseman;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import io.github.mortuusars.horseman.Config;
import io.github.mortuusars.horseman.Horseman;
import io.github.mortuusars.horseman.world.item.crafting.recipe.ComponentTransferringRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;

/** Horseman ships no category: a copper horn info page, its component-transferring craft and the instrument split. */
public final class HorsemanCompat {
    private HorsemanCompat() {}

    public static void register(EmiRegistry registry) {
        Item horn = Horseman.Items.COPPER_HORN.get();
        instruments(registry, horn);
        int pages = pages(registry);
        int info = info(registry, horn);
        Emisemble.LOGGER.info("horseman: {} component transferring pages and {} info page(s) registered", pages, info);
    }

    private static void instruments(EmiRegistry registry, Item horn) {
        HolderSet<Instrument> goatHorns = BuiltInRegistries.INSTRUMENT.getTag(InstrumentTags.GOAT_HORNS).orElse(null);
        if (goatHorns == null) {
            return;
        }
        // The item's own creative entry carries no instrument, so the index has no variant to deduplicate.
        List<EmiStack> indexed = EmiApi.getIndexStacks();
        int added = 0;
        int known = 0;
        for (Holder<Instrument> instrument : goatHorns) {
            EmiStack variant = EmiStack.of(InstrumentItem.create(horn, instrument));
            if (variant.isEmpty()) {
                continue;
            }
            if (indexed.stream().anyMatch(stack -> stack.isEqual(variant))) {
                known++;
                continue;
            }
            registry.addEmiStack(variant);
            added++;
        }
        Emisemble.LOGGER.info("horseman: {} copper horn variants added, {} already indexed", added, known);
    }

    private static int pages(EmiRegistry registry) {
        // EMI skips CustomRecipe outright, so these ids are unclaimed: the pages are additions, not replacements.
        int pages = 0;
        for (RecipeHolder<CraftingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            if (!(holder.value() instanceof ComponentTransferringRecipe recipe)) {
                continue;
            }
            try {
                registry.addRecipe(new EmiCraftingRecipe(
                        inputs(recipe), EmiStack.of(recipe.getResult().copy()), holder.id(), true));
                pages++;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("horseman: skipping the component transferring page {}", holder.id(), t);
            }
        }
        return pages;
    }

    // JEI's extension prepends the source ingredient; the recipe's own list leaves the horn out of the grid.
    private static List<EmiIngredient> inputs(ComponentTransferringRecipe recipe) {
        List<EmiIngredient> inputs = new ArrayList<>(recipe.getIngredients().size() + 1);
        inputs.add(EmiIngredient.of(recipe.getSourceIngredient()));
        for (Ingredient ingredient : recipe.getIngredients()) {
            inputs.add(EmiIngredient.of(ingredient));
        }
        return inputs;
    }

    private static int info(EmiRegistry registry, Item horn) {
        if (!Config.Client.SHOW_JEI_INFORMATION.get()) {
            return 0;
        }
        return EmiRecipes.add(registry, new EmiInfoRecipe(
                List.<EmiIngredient>of(EmiStack.of(horn)),
                List.of(
                        Component.translatable("horseman.jei.info.copper_horn1"),
                        Component.translatable("horseman.jei.info.copper_horn2"),
                        Component.translatable("horseman.jei.info.copper_horn3")),
                ResourceLocation.fromNamespaceAndPath("horseman", "/copper_horn_info"))) ? 1 : 0;
    }
}