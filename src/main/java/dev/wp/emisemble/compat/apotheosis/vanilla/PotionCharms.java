package dev.wp.emisemble.compat.apotheosis.vanilla;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.item.PotionCharmItem;
import dev.shadowsoffire.apotheosis.recipe.PotionCharmRecipe;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** {@code PotionCharmExtension} and its interpreter: one page per pattern, plus the index entries. */
final class PotionCharms {
    private PotionCharms() {}

    /** Replaces the vanilla page, whose output is a component-less charm that reads "Invalid Potion Charm". */
    static int pages(EmiRegistry registry) {
        List<Holder.Reference<Potion>> potions = validPotions();
        if (potions.isEmpty()) {
            Emisemble.LOGGER.warn("apotheosis: no valid potions found, the potion charm crafting page stays as EMI drew it");
            return 0;
        }

        List<RecipeHolder<CraftingRecipe>> holders;
        try {
            holders = registry.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("apotheosis: could not read the crafting recipes for the potion charm pages", t);
            return 0;
        }

        List<PotionCharmPage> pages = new ArrayList<>();
        Set<ResourceLocation> originals = new HashSet<>();

        for (RecipeHolder<CraftingRecipe> holder : holders) {
            if (!(holder.value() instanceof PotionCharmRecipe recipe)) {
                continue;
            }
            originals.add(holder.id());
            // Built inside the guard: one hostile recipe must not cost the whole registration set.
            try {
                pages.add(PotionCharmPage.of(idOf(holder.id()), recipe, potions));
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("apotheosis: skipping the potion charm page for {}", holder.id(), t);
            }
        }

        if (pages.isEmpty()) {
            return 0;
        }

        // Matched by id and excluding our own type: the predicate runs at bake time, after the pass that adds these.
        registry.removeRecipes(r -> originals.contains(r.getId()) && !(r instanceof PotionCharmPage));

        int added = 0;
        for (PotionCharmPage page : pages) {
            if (EmiRecipes.add(registry, page)) {
                added++;
            }
        }

        Emisemble.LOGGER.info("apotheosis: {} potion charm crafting pages registered", added);
        return added;
    }

    /** EMI indexes one stack per item and compares "always equal" by default, so both halves are needed. */
    static void indexVariants(EmiRegistry registry) {
        Item charm = Apoth.Items.POTION_CHARM.value();
        try {
            // compareData on the interpreter's key, not compareComponents: damage and the charm toggle must not split.
            registry.setDefaultComparison(charm, Comparison.compareData(PotionCharms::subtypeOf));
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("apotheosis: could not subtype the potion charm; every charm will compare as one", t);
        }

        // EMI already indexes a charm variant per potion (the item's creative entries), so only add what is missing.
        List<EmiStack> indexed = EmiApi.getIndexStacks();
        int added = 0;
        int existing = 0;
        for (Holder.Reference<Potion> potion : validPotions()) {
            try {
                EmiStack variant = EmiStack.of(PotionContents.createItemStack(charm, potion));
                if (indexed.stream().anyMatch(stack -> stack.isEqual(variant))) {
                    existing++;
                    continue;
                }
                registry.addEmiStack(variant);
                added++;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("apotheosis: could not index a potion charm variant", t);
            }
        }

        Emisemble.LOGGER.info("apotheosis: {} potion charm variants added, {} already indexed", added, existing);
    }

    /** The host's own test: exactly one effect, and not an instant one. */
    static List<Holder.Reference<Potion>> validPotions() {
        try {
            // Registry order, not iteration order: the pages keep their place across sessions and reloads.
            return BuiltInRegistries.POTION.holders()
                    .filter(PotionCharmItem::isValidPotion)
                    .sorted(Comparator.comparingInt(h -> BuiltInRegistries.POTION.getId(h.value())))
                    .toList();
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("apotheosis: could not enumerate the potion registry", t);
            return List.of();
        }
    }

    /** {@code apotheosis:/potion_charm/<namespace>/<path>} — synthetic, and unique across datapack overrides. */
    private static ResourceLocation idOf(ResourceLocation recipe) {
        return ResourceLocation.fromNamespaceAndPath("apotheosis",
                "/potion_charm/" + recipe.getNamespace() + "/" + recipe.getPath());
    }

    /** {@code effect @ amplifier @ duration}, or {@code ""} for a charm with no effect. */
    private static Object subtypeOf(EmiStack stack) {
        ItemStack itemStack = stack.getItemStack();
        if (!PotionCharmItem.hasEffect(itemStack)) {
            return "";
        }
        MobEffectInstance effect = PotionCharmItem.getEffect(itemStack);
        if (effect == null) {
            return "";
        }
        // Through the registry so an unbound holder degrades to null@… instead of throwing inside EMI's comparison.
        ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
        return id + "@" + effect.getAmplifier() + "@" + effect.getDuration();
    }
}
