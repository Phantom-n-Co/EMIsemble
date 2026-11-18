package dev.wp.emisemble.compat.gateways;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.gateways.item.GatePearlItem;
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
        List<ItemStack> pearls = new ArrayList<>();
        try {
            GatePearlItem.generateGatePearlStacks(pearls::add);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("gateways: could not enumerate the gate pearls", t);
            return;
        }

        List<EmiIngredient> variants = new ArrayList<>();
        for (ItemStack pearl : pearls) {
            if (!pearl.isEmpty()) {
                variants.add(EmiStack.of(pearl));
            }
        }
        if (variants.isEmpty()) {
            Emisemble.LOGGER.warn("gateways: no gate pearls in the gateway registry, no info page registered");
            return;
        }

        List<Component> text = new ArrayList<>();
        text.addAll(InfoText.lines(Component.translatable("info.gateways.gate_pearl")));
        text.addAll(InfoText.lines(Component.translatable("info.gateways.gate_pearl.2")));

        // One page, because the host passes one identical text for the whole family and EMI cycles the pearls.
        boolean added = EmiRecipes.add(registry, new EmiInfoRecipe(
                List.of(EmiIngredient.of(variants)),
                text,
                ResourceLocation.fromNamespaceAndPath("gateways", "/info/gate_pearl")));
        Emisemble.LOGGER.info("gateways: gate pearl info page over {} gateways {}", variants.size(),
                added ? "registered" : "failed");
    }
}
