package dev.wp.emisemble.compat.embers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.rekindled.embers.RegistryManager;
import com.rekindled.embers.compat.jei.WorldInteractionRecipe;
import com.rekindled.embers.datagen.EmbersFluidTags;
import com.rekindled.embers.recipe.IDawnstoneAnvilRecipe;
import com.rekindled.embers.recipe.IVisuallySplitRecipe;
import com.rekindled.embers.util.DynamicMetalSeeds;
import com.rekindled.embers.util.FluidAmounts;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.embers.alchemy.AlchemyCategory;
import dev.wp.emisemble.compat.embers.boiling.BoilingCategory;
import dev.wp.emisemble.compat.embers.boring.BoringCategory;
import dev.wp.emisemble.compat.embers.catalysis_combustion.CatalysisCombustionCategory;
import dev.wp.emisemble.compat.embers.dawnstone_anvil.DawnstoneAnvilCategory;
import dev.wp.emisemble.compat.embers.ember_activation.EmberActivationCategory;
import dev.wp.emisemble.compat.embers.excavation.ExcavationCategory;
import dev.wp.emisemble.compat.embers.gaseous_fuel.GaseousFuelCategory;
import dev.wp.emisemble.compat.embers.melting.MeltingCategory;
import dev.wp.emisemble.compat.embers.melting_bonus.MeltingBonusCategory;
import dev.wp.emisemble.compat.embers.metal_coefficient.MetalCoefficientCategory;
import dev.wp.emisemble.compat.embers.mixing.MixingCategory;
import dev.wp.emisemble.compat.embers.stamping.StampingCategory;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.FluidTanks;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;

/** Embers Re-Ignited: thirteen categories, plus the subtype, catalyst and world interaction registrations beside them. */
public final class EmbersCompat {
    private static final String NAMESPACE = "embers";

    private EmbersCompat() {}

    static void register(EmiRegistry registry) {
        seedVariants(registry);
        vanillaWorkstations(registry);
        worldInteraction(registry);

        int added = BoringCategory.register(registry)
                + ExcavationCategory.register(registry)
                + EmberActivationCategory.register(registry)
                + MeltingCategory.register(registry)
                + MeltingBonusCategory.register(registry)
                + StampingCategory.register(registry)
                + MixingCategory.register(registry)
                + MetalCoefficientCategory.register(registry)
                + AlchemyCategory.register(registry)
                + BoilingCategory.register(registry)
                + GaseousFuelCategory.register(registry)
                + CatalysisCombustionCategory.register(registry)
                + DawnstoneAnvilCategory.register(registry);
        Emisemble.LOGGER.info("embers: {} recipes registered across 13 categories", added);
    }

    // The comparison alone changes nothing here: EMI indexes one stack per item, so each metal variant has to be added.
    private static void seedVariants(EmiRegistry registry) {
        try {
            registry.setDefaultComparison(RegistryManager.DYNAMIC_CRYSTAL_SEED_ITEM.get(), Comparison.compareComponents());
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("embers: could not subtype the crystal seed; its metal variants will collapse", t);
        }
        List<ItemStack> seeds = DynamicMetalSeeds.getStacks();
        List<EmiStack> indexed = EmiApi.getIndexStacks();
        int added = 0;
        for (ItemStack stack : seeds) {
            EmiStack candidate = EmiStack.of(stack);
            if (indexed.stream().anyMatch(known -> known.isEqual(candidate))) {
                continue;
            }
            try {
                registry.addEmiStack(candidate);
                added++;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("embers: could not add the crystal seed variant {}", candidate, t);
            }
        }
        Emisemble.LOGGER.info("embers: {} crystal seed variants added, {} already indexed", added, seeds.size() - added);
    }

    // The host's furnaces also stand in on the vanilla smelting pages; EMI's own categories stay null until its pass runs.
    private static void vanillaWorkstations(EmiRegistry registry) {
        workstation(registry, VanillaEmiRecipeCategories.SMELTING, RegistryManager.HEARTH_COIL_ITEM.get());
        workstation(registry, VanillaEmiRecipeCategories.SMOKING, RegistryManager.HEARTH_COIL_ITEM.get());
        workstation(registry, VanillaEmiRecipeCategories.SMOKING, RegistryManager.CHAR_INSTILLER.get());
        workstation(registry, VanillaEmiRecipeCategories.BLASTING, RegistryManager.HEARTH_COIL_ITEM.get());
        workstation(registry, VanillaEmiRecipeCategories.BLASTING, RegistryManager.ATMOSPHERIC_BELLOWS.get());
    }

    private static void workstation(EmiRegistry registry, EmiRecipeCategory category, ItemLike item) {
        if (category == null) {
            return;
        }
        try {
            registry.addWorkstation(category, EmiStack.of(item));
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("embers: could not register a vanilla workstation", t);
        }
    }

    // EMI's own world interaction category covers this shape, so the host's separate JEI page is not ported as one of ours.
    private static void worldInteraction(EmiRegistry registry) {
        try {
            WorldInteractionRecipe recipe = WorldInteractionRecipe.solidifiedMetal();
            EmiRecipes.add(registry, EmiWorldInteractionRecipe.builder()
                    .id(ResourceLocation.fromNamespaceAndPath(NAMESPACE, "/world_interaction/solidified_metal"))
                    .leftInput(FluidTanks.of(recipe.primaryFluids().toArray(FluidStack[]::new)))
                    .rightInput(FluidTanks.of(recipe.secondaryFluid()), false)
                    .output(EmiStack.of(recipe.output()))
                    .build());
        } catch (Throwable t) {
            Emisemble.LOGGER.error("embers: could not register the solidified metal world interaction page", t);
        }
    }

    /** A list, not {@code EmiIngredient.of}: that folds a list matching an item tag into a tag ingredient, and an empty
     * tag renders as a barrier named after the tag. */
    public static EmiIngredient items(List<ItemStack> stacks) {
        List<EmiStack> variants = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                variants.add(EmiStack.of(stack));
            }
        }
        return variants.isEmpty() ? EmiStack.EMPTY : new ListEmiIngredient(variants, 1L);
    }

    /** Mirrors the host's JEI IngotTooltipCallback; EMI already draws the amount itself. */
    public static SlotWidget ingotTooltip(SlotWidget slot, FluidStack stack) {
        if (stack.getFluid().defaultFluidState().is(EmbersFluidTags.INGOT_TOOLTIP)
                && stack.getAmount() >= FluidAmounts.nuggetValue()) {
            slot.appendTooltip(FluidAmounts.getIngotTooltip(stack.getAmount()).withStyle(ChatFormatting.GRAY));
        }
        return slot;
    }

    /**
     * Adds every page of a type, splitting the recipes the host shows as one page per variant and skipping a single bad
     * recipe instead of dropping the category with it.
     */
    public static <C extends RecipeInput, T extends Recipe<C>> int addSplitRecipes(EmiRegistry registry,
            RecipeType<T> type, Function<RecipeHolder<T>, EmiRecipe> factory) {
        List<RecipeHolder<T>> holders;
        try {
            holders = registry.getRecipeManager().getAllRecipesFor(type);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("embers: could not read the {} recipes", type, t);
            return 0;
        }
        int added = 0;
        for (RecipeHolder<T> holder : splitVisualRecipes(holders)) {
            try {
                registry.addRecipe(factory.apply(holder));
                added++;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("embers: skipping page {}", holder.id(), t);
            }
        }
        return added;
    }

    /** The host's visual variants of one anvil recipe: itself when it is not split, its variants when it is. */
    @SuppressWarnings("unchecked")
    public static List<IDawnstoneAnvilRecipe> visualVariants(RecipeHolder<IDawnstoneAnvilRecipe> holder) {
        if (holder.value() instanceof IVisuallySplitRecipe<?> split) {
            try {
                return (List<IDawnstoneAnvilRecipe>) (List<?>) split.getVisualRecipes();
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("embers: could not split {} into its variants", holder.id(), t);
            }
        }
        return List.of(holder.value());
    }

    /** One holder per variant, with the synthetic ids EMI needs because it keys pages by id alone. */
    public static List<RecipeHolder<IDawnstoneAnvilRecipe>> perVariant(RecipeHolder<IDawnstoneAnvilRecipe> holder,
            List<IDawnstoneAnvilRecipe> variants) {
        List<RecipeHolder<IDawnstoneAnvilRecipe>> pages = new ArrayList<>(variants.size());
        for (int index = 0; index < variants.size(); index++) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(holder.id().getNamespace(),
                    "/" + holder.id().getPath() + "/display_" + index);
            pages.add(new RecipeHolder<>(id, variants.get(index)));
        }
        return pages;
    }

    // A variant gets a synthetic id because EMI keys pages by id and two variants of one recipe would collide.
    @SuppressWarnings("unchecked")
    private static <T extends Recipe<?>> List<RecipeHolder<T>> splitVisualRecipes(List<RecipeHolder<T>> holders) {
        List<RecipeHolder<T>> pages = new ArrayList<>(holders.size());
        for (RecipeHolder<T> holder : holders) {
            if (!(holder.value() instanceof IVisuallySplitRecipe<?> split)) {
                pages.add(holder);
                continue;
            }
            List<T> variants;
            try {
                variants = (List<T>) (List<?>) split.getVisualRecipes();
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("embers: could not split {} into one page per variant", holder.id(), t);
                pages.add(holder);
                continue;
            }
            for (int index = 0; index < variants.size(); index++) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(holder.id().getNamespace(),
                        "/" + holder.id().getPath() + "/display_" + index);
                pages.add(new RecipeHolder<>(id, variants.get(index)));
            }
        }
        return pages;
    }
}
