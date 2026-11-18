package dev.wp.emisemble.compat.apotheosis.vanilla;

import dev.emi.emi.api.EmiRegistry;
import dev.shadowsoffire.apotheosis.affix.UnnamingRecipe;
import dev.shadowsoffire.apotheosis.recipe.MaliceRecipe;
import dev.shadowsoffire.apotheosis.recipe.SupremacyRecipe;
import dev.shadowsoffire.apotheosis.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.socket.SocketingRecipe;
import dev.shadowsoffire.apotheosis.socket.WithdrawalRecipe;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Apotheosis' vanilla-category extensions: six smithing pages and the potion charm crafting pages. */
public final class VanillaExtensions {
    private VanillaExtensions() {}

    public static int register(EmiRegistry registry) {
        int pages = smithingPages(registry);
        pages += PotionCharms.pages(registry);
        PotionCharms.indexVariants(registry);
        return pages;
    }

    /**
     * Replaces the pages EMI draws for the six recipes whose real ingredients sit in fields it never reads: the five
     * whose sigil sits in a plain addition slot, and {@code apotheosis:socketing}, whose base and result are both empty.
     */
    private static int smithingPages(EmiRegistry registry) {
        List<RecipeHolder<SmithingRecipe>> holders;
        try {
            holders = registry.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("apotheosis: could not read the smithing recipes, their pages stay as EMI drew them", t);
            return 0;
        }

        List<ApothSmithingPage> pages = new ArrayList<>();
        Set<ResourceLocation> replaced = new HashSet<>();
        int socketing = 0;
        int sockets = 0;
        int withdrawal = 0;
        int unnaming = 0;
        int malice = 0;
        int supremacy = 0;

        for (RecipeHolder<SmithingRecipe> holder : holders) {
            // Construction sits inside the guard: a throw escaping register costs this plugin every category.
            try {
                SmithingRecipe recipe = holder.value();
                ApothSmithingPage page;
                if (recipe instanceof SocketingRecipe) {
                    page = ApothSmithingPage.socketing(holder.id());
                    socketing++;
                } else if (recipe instanceof AddSocketsRecipe addSockets) {
                    page = ApothSmithingPage.addSockets(holder.id(), addSockets);
                    sockets++;
                } else if (recipe instanceof WithdrawalRecipe) {
                    page = ApothSmithingPage.withdrawal(holder.id());
                    withdrawal++;
                } else if (recipe instanceof UnnamingRecipe) {
                    page = ApothSmithingPage.unnaming(holder.id());
                    unnaming++;
                } else if (recipe instanceof MaliceRecipe) {
                    page = ApothSmithingPage.malice(holder.id());
                    malice++;
                } else if (recipe instanceof SupremacyRecipe) {
                    page = ApothSmithingPage.supremacy(holder.id());
                    supremacy++;
                } else {
                    continue;
                }
                pages.add(page);
                replaced.add(holder.id());
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("apotheosis: skipping the smithing page for {}", holder.id(), t);
            }
        }

        if (pages.isEmpty()) {
            return 0;
        }

        // The predicate runs at bake time, after this pass, so it has to spare the replacements: they keep the data id.
        registry.removeRecipes(r -> replaced.contains(r.getId()) && !(r instanceof ApothSmithingPage));

        int added = 0;
        for (ApothSmithingPage page : pages) {
            if (EmiRecipes.add(registry, page)) {
                added++;
            }
        }

        Emisemble.LOGGER.info(
                "apotheosis: {} smithing recipes registered (socketing {}, add_sockets {}, withdrawal {}, unnaming {}, "
                        + "malice {}, supremacy {})",
                added, socketing, sockets, withdrawal, unnaming, malice, supremacy);
        return added;
    }
}
