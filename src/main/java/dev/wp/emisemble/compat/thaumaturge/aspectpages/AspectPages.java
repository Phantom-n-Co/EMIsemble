package dev.wp.emisemble.compat.thaumaturge.aspectpages;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.thaumaturge.AspectEmiStack;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** The two registry-driven aspect pages: what an aspect is made of, and which items carry it. Both are synthetic. */
public final class AspectPages {
    public static final ResourceLocation COMPOSITION =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "aspect_composition");
    public static final ResourceLocation FROM_STACKS =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "aspect_from_stacks");

    private static final String COMPOSITION_TITLE = "jei.thaumaturge.category.aspect_composition";
    private static final String FROM_STACKS_TITLE = "jei.thaumaturge.category.aspect_from_stacks";

    // JEI lays the sources out nine across, four rows down: 36 to a page.
    private static final int PAGE_INPUTS = 36;

    private AspectPages() {}

    public static void register(EmiRegistry registry) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            Emisemble.LOGGER.warn("thaumaturge: no client level, skipping the aspect pages");
            return;
        }

        Registry<IAspect> aspects = minecraft.level.registryAccess().registry(IAspect.REGISTRY_KEY).orElse(null);
        if (aspects == null) {
            Emisemble.LOGGER.warn("thaumaturge: no aspect registry on the client, skipping the aspect pages");
            return;
        }

        int compositions = addCompositions(registry, aspects);
        int sources = addFromStacks(registry, aspects);
        Emisemble.LOGGER.info("thaumaturge: {} aspect composition and {} aspect-from-stacks pages registered",
                compositions, sources);
    }

    // One page per non-primal aspect with exactly two components, unfiltered by knowledge: the chip masks itself.
    private static int addCompositions(EmiRegistry registry, Registry<IAspect> aspects) {
        EmiRecipeCategory category = new TranslatedCategory(COMPOSITION, compositionIcon(aspects), COMPOSITION_TITLE);
        registry.addCategory(category);
        registry.addWorkstation(category, EmiStack.of(TTItems.THAUMONOMICON.get()));

        int added = 0;
        for (Holder.Reference<IAspect> result : aspects.holders().toList()) {
            List<Holder<IAspect>> parts = result.value().components();
            if (parts.size() != 2) {
                continue;
            }
            try {
                registry.addRecipe(new AspectCompositionEmiRecipe(category, compositionId(result), parts.get(0),
                        parts.get(1), result));
                added++;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping aspect composition {}", pathOf(result), t);
            }
        }
        return added;
    }

    private static int addFromStacks(EmiRegistry registry, Registry<IAspect> aspects) {
        EmiRecipeCategory category =
                new TranslatedCategory(FROM_STACKS, EmiStack.of(TTItems.THAUMONOMICON.get()), FROM_STACKS_TITLE);
        registry.addCategory(category);
        // JEI hands this page to the book and to all four essentia sources.
        registry.addWorkstation(category, EmiStack.of(TTItems.THAUMONOMICON.get()));
        registry.addWorkstation(category, EmiStack.of(TTItems.SMELTER_BASIC.get()));
        registry.addWorkstation(category, EmiStack.of(TTItems.SMELTER_THAUMIUM.get()));
        registry.addWorkstation(category, EmiStack.of(TTItems.SMELTER_VOID.get()));
        registry.addWorkstation(category, EmiStack.of(TTItems.ADVANCED_ALCHEMICAL_FURNACE.get()));

        Map<ResourceKey<IAspect>, List<ItemStack>> sources = collectSources();
        int added = 0;
        for (Holder.Reference<IAspect> aspect : orderedAspects(aspects, sources)) {
            List<ItemStack> stacks = sources.get(aspect.key()).stream()
                    .sorted(Comparator.comparingInt(ItemStack::getCount).reversed())
                    .toList();
            for (int start = 0, page = 0; start < stacks.size(); start += PAGE_INPUTS, page++) {
                List<ItemStack> pageStacks = stacks.subList(start, Math.min(start + PAGE_INPUTS, stacks.size()));
                try {
                    registry.addRecipe(new AspectFromStacksEmiRecipe(category, pageId(aspect, page), aspect,
                            pageStacks));
                    added++;
                } catch (Throwable t) {
                    Emisemble.LOGGER.warn("Skipping aspect source page {} page {}", pathOf(aspect), page, t);
                }
            }
        }
        return added;
    }

/**
 * Inverts the aspect index over EMI's stacks, not the item registry, so component variants count as separate
 * items.
 */
    private static Map<ResourceKey<IAspect>, List<ItemStack>> collectSources() {
        Map<ResourceKey<IAspect>, List<ItemStack>> sources = new HashMap<>();
        IAspectIndex index;
        try {
            index = AspectIndexAccess.index();
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("thaumaturge: aspect index not bound, skipping the aspect-from-stacks pages", t);
            return sources;
        }

        Set<String> seen = new HashSet<>();
        for (EmiStack emiStack : EmiApi.getIndexStacks()) {
            if (emiStack.isEmpty()) {
                continue;
            }
            ItemStack stack = emiStack.getItemStack();
            if (stack.isEmpty()) {
                continue;
            }
            String key = BuiltInRegistries.ITEM.getKey(stack.getItem()) + "|" + stack.getComponents();
            if (!seen.add(key)) {
                continue;
            }

            AspectList aspects;
            try {
                aspects = index.of(stack);
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping stack {} while reading its aspects", key, t);
                continue;
            }

            for (AspectInstance instance : aspects.entries()) {
                ResourceKey<IAspect> aspectKey = instance.aspect().unwrapKey().orElse(null);
                if (aspectKey == null) {
                    continue;
                }
                ItemStack counted = stack.copyWithCount(instance.amount());
                if (counted.isEmpty()) {
                    continue;
                }
                sources.computeIfAbsent(aspectKey, k -> new ArrayList<>()).add(counted);
            }
        }
        return sources;
    }

    /** Primals first, then the rest, each group in registry order — JEI's stable sort expressed as two passes. */
    private static List<Holder.Reference<IAspect>> orderedAspects(
            Registry<IAspect> aspects, Map<ResourceKey<IAspect>, List<ItemStack>> sources) {
        List<Holder.Reference<IAspect>> holders = aspects.holders().toList();
        List<Holder.Reference<IAspect>> ordered = new ArrayList<>();
        for (boolean primal : new boolean[] {true, false}) {
            for (Holder.Reference<IAspect> holder : holders) {
                if (holder.value().isPrimal() == primal && sources.containsKey(holder.key())) {
                    ordered.add(holder);
                }
            }
        }
        return ordered;
    }

    /** JEI's tab icon: praecantatio, else the first registered aspect, else a rune of salis mundus. */
    private static EmiRenderable compositionIcon(Registry<IAspect> aspects) {
        Optional<Holder.Reference<IAspect>> preferred = aspects.getHolder(TTAspects.PRAECANTATIO);
        if (preferred.isPresent()) {
            return new AspectEmiStack(preferred.get(), 1);
        }
        Optional<Holder.Reference<IAspect>> first = aspects.holders().findFirst();
        if (first.isPresent()) {
            return new AspectEmiStack(first.get(), 1);
        }
        return EmiStack.of(TTItems.SALIS_MUNDUS.get());
    }

    // The leading slash marks a synthetic id, the way JEMI ids its shimmed recipes.
    private static ResourceLocation compositionId(Holder<IAspect> result) {
        return ResourceLocation.fromNamespaceAndPath("thaumaturge", "/aspect_composition/" + pathOf(result));
    }

    private static ResourceLocation pageId(Holder<IAspect> aspect, int page) {
        return ResourceLocation.fromNamespaceAndPath("thaumaturge",
                "/aspect_from_stacks/" + pathOf(aspect) + "/" + page);
    }

    private static String pathOf(Holder<IAspect> aspect) {
        return aspect.unwrapKey().map(key -> key.location().getPath()).orElse("unknown");
    }
}
