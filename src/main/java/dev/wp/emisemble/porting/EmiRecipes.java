package dev.wp.emisemble.porting;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.wp.emisemble.Emisemble;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.function.Function;

/**
 * Registration helpers shared by every compat. The category id must be the host's recipe type key — JEMI drops a JEI
 * category whose uid is already registered, so that is what suppresses its shim without config.
 */
public final class EmiRecipes {
    private static final ResourceLocation FALLBACK =
            ResourceLocation.fromNamespaceAndPath(Emisemble.MOD_ID, "unknown_recipe_type");

    private EmiRecipes() {}

/** The host recipe type's registry key, which is also the JEI category uid. */
    public static ResourceLocation categoryId(RecipeType<?> type) {
        ResourceLocation id = BuiltInRegistries.RECIPE_TYPE.getKey(type);
        if (id == null) {
            Emisemble.LOGGER.warn("Recipe type {} is not registered; its category id will not suppress any shim", type);
            return FALLBACK;
        }
        return id;
    }

    /**
     * An id EMI will not report as missing: the host's own id when it resolves, a leading slash when it does not — pages
     * built from a host's private map (Myths' research, IE's generated lists) have ids no recipe manager knows.
     */
    public static ResourceLocation pageId(ResourceLocation id) {
        Level level = Minecraft.getInstance().level;
        if (level == null || level.getRecipeManager().byKey(id).isPresent()) {
            return id;
        }
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "/" + id.getPath());
    }

    /** A page EMI keeps as its own, for a page derived from something that already has a recipe under that id. */
    public static ResourceLocation synthetic(ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "/" + id.getPath());
    }

    /**
     * Adds every recipe of {@code type}, because EMI treats a throw inside a plugin as fatal for that plugin's whole
     * registration set — one hostile recipe would otherwise cost the entire category.
     */
    public static <C extends RecipeInput, T extends Recipe<C>> int addAll(
            EmiRegistry registry, RecipeType<T> type, Function<RecipeHolder<T>, EmiRecipe> factory) {
        int added = 0;
        for (RecipeHolder<T> holder : registry.getRecipeManager().getAllRecipesFor(type)) {
            try {
                registry.addRecipe(factory.apply(holder));
                added++;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping {} recipe {}", type, holder.id(), t);
            }
        }
        return added;
    }

    /** Adds one synthetic recipe, guarded. Returns whether it landed. */
    public static boolean add(EmiRegistry registry, EmiRecipe recipe) {
        try {
            registry.addRecipe(recipe);
            return true;
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("Skipping generated recipe {}", recipe.getId(), t);
            return false;
        }
    }

    /** EMI tooltips are {@link ClientTooltipComponent}s, not components. */
    public static List<ClientTooltipComponent> tooltip(List<Component> lines) {
        return lines.stream()
                .<ClientTooltipComponent>map(line -> ClientTooltipComponent.create(line.getVisualOrderText()))
                .toList();
    }
}
