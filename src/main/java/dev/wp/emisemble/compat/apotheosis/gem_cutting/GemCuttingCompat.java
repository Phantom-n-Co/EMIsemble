package dev.wp.emisemble.compat.apotheosis.gem_cutting;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.BasicGemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingMenu;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.InfoText;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/** {@code apotheosis:gem_cutting}. */
public final class GemCuttingCompat {
    // The _jei sheet, not the container background, and its art is opaque with the sockets painted on: slots stay frameless.
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("apotheosis", "textures/gui/gem_cutting_jei.png");
    static final int SHEET = 256;
    static final int WIDTH = 148;
    static final int HEIGHT = 78;

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(Apoth.RecipeTypes.GEM_CUTTING),
            EmiStack.of(Apoth.Items.GEM_CUTTING_TABLE.value()),
            "menu.apotheosis.gem_cutting");

    private static final int INPUT_SLOTS = 4;
    private static final int PLAYER_SLOTS = 36;

    private GemCuttingCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(Apoth.Items.GEM_CUTTING_TABLE.value()));
        PurityIndex variants = indexPurities(registry);

        int recipes = EmiRecipes.addAll(registry, Apoth.RecipeTypes.GEM_CUTTING, GemCuttingCompat::page);

        int info = infoPages(registry);
        try {
            registry.addRecipeHandler(Apoth.Menus.GEM_CUTTING, new Handler());
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("apotheosis: could not register the gem cutting transfer handler", t);
        }
        Emisemble.LOGGER.info("apotheosis: {} gem cutting recipes, {} purity variants ({} added) and {} info pages registered",
                recipes, variants.known() + variants.added(), variants.added(), info);
        return recipes;
    }

    private record PurityIndex(int added, int known) {}

/**
 * The gem item fills its creative tab with every legal gem × purity, and EMI's default index source is that tab,
 * so
 */
    private static PurityIndex indexPurities(EmiRegistry registry) {
        registry.setDefaultComparison(Apoth.Items.GEM.value(), Comparison.compareComponents());
        Comparison sameVariant = Comparison.compareComponents();
        List<EmiStack> index = EmiApi.getIndexStacks();
        int added = 0;
        int known = 0;
        for (Gem gem : GemCuttingIngredients.sortedGems()) {
            EmiStack previous = null;
            for (Purity purity : Purity.ALL_PURITIES) {
                if (!purity.isAtLeast(gem.getMinPurity())) {
                    continue;
                }
                EmiStack stack = EmiStack.of(gem.toStack(purity));
                if (stack.isEmpty()) {
                    continue;
                }
                if (alreadyIndexed(index, stack, sameVariant)) {
                    known++;
                } else {
                    if (previous == null) {
                        registry.addEmiStack(stack);
                    } else {
                        // addEmiStackAfter drops a stack with no matching anchor, and the previous purity always
                        // exists by now: either EMI indexed it or the line above just added it.
                        EmiStack anchor = previous;
                        registry.addEmiStackAfter(stack, s -> s.isEqual(anchor, sameVariant));
                    }
                    added++;
                }
                previous = stack;
            }
        }
        return new PurityIndex(added, known);
    }

    private static boolean alreadyIndexed(List<EmiStack> index, EmiStack stack, Comparison comparison) {
        for (EmiStack candidate : index) {
            if (candidate.isEqual(stack, comparison)) {
                return true;
            }
        }
        return false;
    }

    /** The host's JEI type names only the purity shape but the category lays out per recipe class. */
    private static EmiRecipe page(RecipeHolder<GemCuttingRecipe> holder) {
        GemCuttingRecipe recipe = holder.value();
        if (recipe instanceof PurityUpgradeRecipe purity) {
            return GemCuttingEmiRecipe.purityUpgrade(holder, purity);
        }
        if (recipe instanceof BasicGemCuttingRecipe basic) {
            return GemCuttingEmiRecipe.basic(holder, basic);
        }
        throw new UnsupportedOperationException("No gem cutting layout for " + recipe.getClass().getName());
    }

    private static int infoPages(EmiRegistry registry) {
        int added = gemInfoPage(registry);
        added += infoPage(registry, Apoth.Items.GEM_DUST.value(), "info.apotheosis.gem_crushing", "/gem_dust_info");
        added += infoPage(registry, Apoth.Items.SIGIL_OF_UNNAMING.value(), "info.apotheosis.unnaming",
                "/sigil_of_unnaming_info");
        return added;
    }

    private static int gemInfoPage(EmiRegistry registry) {
        List<EmiIngredient> variants = new ArrayList<>();
        for (Gem gem : GemCuttingIngredients.sortedGems()) {
            for (Purity purity : Purity.ALL_PURITIES) {
                if (purity.isAtLeast(gem.getMinPurity())) {
                    variants.add(EmiStack.of(gem.toStack(purity)));
                }
            }
        }
        if (variants.isEmpty()) {
            return 0;
        }

        // EMI shows 18 slots and folds the rest into the last one, so every purity rides a single cycling ingredient.
        boolean added = EmiRecipes.add(registry, new EmiInfoRecipe(
                List.of(EmiIngredient.of(variants)),
                InfoText.lines(Component.translatable("info.apotheosis.socketing")),
                ResourceLocation.fromNamespaceAndPath("apotheosis", "/gem_info")));
        if (added) {
            Emisemble.LOGGER.info("apotheosis: {} gem variants on one socketing info page", variants.size());
        }
        return added ? 1 : 0;
    }

    private static int infoPage(EmiRegistry registry, Item item, String key, String path) {
        return EmiRecipes.add(registry, new EmiInfoRecipe(
                List.<EmiIngredient>of(EmiStack.of(item)),
                InfoText.lines(Component.translatable(key)),
                ResourceLocation.fromNamespaceAndPath("apotheosis", path))) ? 1 : 0;
    }

    /** Slots 0..3 are base, top, left, right; the page's inputs are in that order because the fill is positional. */
    private static final class Handler implements StandardRecipeHandler<GemCuttingMenu> {
        @Override
        public List<Slot> getInputSources(GemCuttingMenu menu) {
            return menu.slots.subList(Math.min(INPUT_SLOTS, menu.slots.size()),
                    Math.min(INPUT_SLOTS + PLAYER_SLOTS, menu.slots.size()));
        }

        @Override
        public List<Slot> getCraftingSlots(GemCuttingMenu menu) {
            return menu.slots.subList(0, Math.min(INPUT_SLOTS, menu.slots.size()));
        }

        @Override
        public List<Slot> getCraftingSlots(EmiRecipe recipe, GemCuttingMenu menu) {
            return recipe != null && recipe.getInputs().size() == INPUT_SLOTS ? getCraftingSlots(menu) : List.of();
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe != null && recipe.getCategory() == CATEGORY;
        }
    }
}
