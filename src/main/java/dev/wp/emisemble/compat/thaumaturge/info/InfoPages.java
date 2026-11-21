package dev.wp.emisemble.compat.thaumaturge.info;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.thaumaturge.AspectEmiStack;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** The deconstruction table blurb and one info page per aspect. */
public final class InfoPages {
    private static final String DECONSTRUCTION_KEY = "jei.thaumaturge.deconstruction.info";

    private InfoPages() {}

    public static void register(EmiRegistry registry) {
        deconstructionTable(registry);
        aspectPages(registry);
    }

    private static void deconstructionTable(EmiRegistry registry) {
        try {
            EmiRecipes.add(registry, new EmiInfoRecipe(
                    List.<EmiIngredient>of(EmiStack.of(TTItems.DECONSTRUCTION_TABLE.get())),
                    List.of(Component.translatable(DECONSTRUCTION_KEY)),
                    ResourceLocation.fromNamespaceAndPath("thaumaturge", "/deconstruction_table_info")));
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("thaumaturge: could not add the deconstruction table info page", t);
        }
    }

    /** Every aspect, primal or not, in registry order; the description masks itself when unresearched. */
    private static void aspectPages(EmiRegistry registry) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            Emisemble.LOGGER.warn("thaumaturge: no client level, skipping the aspect info pages");
            return;
        }

        Registry<IAspect> aspects = minecraft.level.registryAccess().registry(IAspect.REGISTRY_KEY).orElse(null);
        if (aspects == null) {
            Emisemble.LOGGER.warn("thaumaturge: no aspect registry on the client, skipping the aspect info pages");
            return;
        }

        int added = 0;
        for (Holder.Reference<IAspect> aspect : aspects.holders().toList()) {
            ResourceLocation key = aspects.getKey(aspect.value());
            if (key == null) {
                continue;
            }
            try {
                if (EmiRecipes.add(registry, new EmiInfoRecipe(
                        List.<EmiIngredient>of(new AspectEmiStack(aspect, 1)),
                        List.of(AspectComponents.description(aspect)),
                        aspectInfoId(key)))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping the aspect info page for {}", key, t);
            }
        }
        Emisemble.LOGGER.info("thaumaturge: {} aspect info pages registered", added);
    }

    private static ResourceLocation aspectInfoId(ResourceLocation aspect) {
        return ResourceLocation.fromNamespaceAndPath("thaumaturge",
                "/aspect_info/" + aspect.getNamespace() + "/" + aspect.getPath());
    }
}
