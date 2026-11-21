package dev.wp.emisemble.porting;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Registration helpers shared by every compat. The category id must be the host's recipe type key — JEMI drops a JEI
 * category whose uid is already registered, so that is what suppresses its shim without config.
 */
public final class EmiRecipes {
    private static final ResourceLocation FALLBACK =
            ResourceLocation.fromNamespaceAndPath(Emisemble.MOD_ID, "unknown_recipe_type");
    private static final Field TEXT = infoTextField();
    private static final Pattern KEY = Pattern.compile("[a-z0-9_]+(\\.[a-z0-9_]+){2,}");
    private static final List<EmiRecipe> infoPages = new ArrayList<>();
    private static final Map<EmiRecipe, List<EmiStack>> pageStacks = new IdentityHashMap<>();
    private static final Map<EmiRecipe, String> pageTexts = new IdentityHashMap<>();
    private static EmiRegistry infoRegistry;

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
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("Skipping generated recipe {}", recipe.getId(), t);
            return false;
        }
        if (recipe instanceof EmiInfoRecipe && recipe.getId() != null) {
            claimInfo(registry, recipe);
        }
        return true;
    }

    /**
     * EMI's JEI bridge imports every JEI info page into its own INFO category with no id, so a host we ported shows
     * twice and no category-id rule applies. Nothing in the API stops the import; the copies are dropped at bake time.
     */
    private static void claimInfo(EmiRegistry registry, EmiRecipe page) {
        if (registry != infoRegistry) {
            infoRegistry = registry;
            infoPages.clear();
            pageStacks.clear();
            pageTexts.clear();
            registry.removeRecipes(EmiRecipes::isImportedDuplicate);
        }
        infoPages.add(page);
    }

    /**
     * Only a page whose every stack one of ours carries and whose text our pages account for entirely: an import that
     * states anything we do not stays, because it is then the only page saying it.
     */
    private static boolean isImportedDuplicate(EmiRecipe recipe) {
        if (recipe.getId() != null || recipe.getCategory() != VanillaEmiRecipeCategories.INFO) {
            return false;
        }
        String raw = text(recipe);
        if (raw.isEmpty()) {
            return false;
        }
        List<EmiRecipe> covering = new ArrayList<>();
        for (EmiStack stack : stacks(recipe)) {
            boolean onOne = false;
            for (EmiRecipe page : infoPages) {
                if (stacks(page).contains(stack)) {
                    onOne = true;
                    if (!covering.contains(page)) {
                        covering.add(page);
                    }
                }
            }
            if (!onOne) {
                return false;
            }
        }
        if (covering.isEmpty()) {
            return false;
        }
        // A page whose whole text is a translation key the game cannot resolve renders that key, not content.
        if (isUnresolvedKey(raw)) {
            return true;
        }
        String residue = flat(raw);
        for (EmiRecipe page : covering) {
            String own = flat(text(page));
            if (!own.isEmpty()) {
                residue = residue.replace(own, "");
            }
        }
        return residue.isEmpty();
    }

    private static boolean isUnresolvedKey(String text) {
        return KEY.matcher(text).matches() && !Language.getInstance().has(text);
    }

    private static String flat(String text) {
        return text.replaceAll("\\s+", "");
    }

    private static List<EmiStack> stacks(EmiRecipe recipe) {
        return pageStacks.computeIfAbsent(recipe, page -> {
            List<EmiStack> stacks = new ArrayList<>();
            for (EmiIngredient ingredient : page.getInputs()) {
                stacks.addAll(ingredient.getEmiStacks());
            }
            return stacks;
        });
    }

    /** EMI exposes no accessor for an info page's text, and the whitespace it keeps is not the host's. */
    private static String text(EmiRecipe recipe) {
        if (TEXT == null || !(recipe instanceof EmiInfoRecipe)) {
            return "";
        }
        try {
            StringBuilder text = new StringBuilder();
            for (Object line : (List<?>) TEXT.get(recipe)) {
                ((FormattedCharSequence) line).accept((index, style, codePoint) -> {
                    text.appendCodePoint(codePoint);
                    return true;
                });
            }
            return text.toString().replaceAll("\\s+", " ").trim();
        } catch (Throwable t) {
            return "";
        }
    }

    private static Field infoTextField() {
        try {
            Field field = EmiInfoRecipe.class.getDeclaredField("text");
            field.setAccessible(true);
            return field;
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("Cannot read EMI info text; JEI info pages will stay duplicated", t);
            return null;
        }
    }

    /** EMI tooltips are {@link ClientTooltipComponent}s, not components. */
    public static List<ClientTooltipComponent> tooltip(List<Component> lines) {
        return lines.stream()
                .<ClientTooltipComponent>map(line -> ClientTooltipComponent.create(line.getVisualOrderText()))
                .toList();
    }
}
