package dev.wp.emisemble.compat.gateways;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.gateways.GatewayObjects;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.InfoText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Gateways' single info call: one page over every gate pearl. */
public final class GatewaysCompat {
    private GatewaysCompat() {}

    static void register(EmiRegistry registry) {
        List<EmiStack> pearls = new ArrayList<>();
        for (ItemStack pearl : GatewaysSync.pearls()) {
            if (!pearl.isEmpty()) {
                pearls.add(EmiStack.of(pearl));
            }
        }
        if (pearls.isEmpty()) {
            Emisemble.LOGGER.warn("gateways: no gate pearls, no info page registered");
            return;
        }
        subtypes(registry, pearls);

        List<Component> text = new ArrayList<>();
        text.addAll(InfoText.lines(Component.translatable("info.gateways.gate_pearl")));
        text.addAll(InfoText.lines(Component.translatable("info.gateways.gate_pearl.2")));

        // One page, because the host passes one identical text for the whole family and EMI cycles the pearls.
        boolean added = EmiRecipes.add(registry, new EmiInfoRecipe(
                List.of(EmiIngredient.of(pearls)),
                text,
                ResourceLocation.fromNamespaceAndPath("gateways", "/info/gate_pearl")));
        Emisemble.LOGGER.info("gateways: gate pearl info page over {} gateways {}", pearls.size(),
                added ? "registered" : "failed");
    }

    // Without the host's subtype every pearl compares as one item, and the page's ingredient collapses to a single.
    private static void subtypes(EmiRegistry registry, List<EmiStack> pearls) {
        Comparison sameVariant = Comparison.compareComponents();
        try {
            registry.setDefaultComparison(GatewayObjects.GATE_PEARL.value(), sameVariant);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("gateways: could not subtype the gate pearl; every pearl will compare as one", t);
        }
        List<EmiStack> indexed = EmiApi.getIndexStacks();
        int added = 0;
        for (EmiStack pearl : pearls) {
            if (indexed.stream().noneMatch(stack -> stack.isEqual(pearl, sameVariant))) {
                registry.addEmiStack(pearl);
                added++;
            }
        }
        Emisemble.LOGGER.info("gateways: {} of {} gate pearls added to the index", added, pearls.size());
    }
}
