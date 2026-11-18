package dev.wp.emisemble.compat.apotheosis.gem_cutting;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public final class GemCuttingIngredients {
    private GemCuttingIngredients() {}

    /** Every gem that can exist at {@code purity}, in index order so a page and the index list gems the same way. */
    public static List<Gem> eligibleGems(Purity purity) {
        List<Gem> gems = new ArrayList<>();
        for (Gem gem : sortedGems()) {
            if (purity.isAtLeast(gem.getMinPurity())) {
                gems.add(gem);
            }
        }
        return gems;
    }

    public static List<EmiStack> gemsAt(Purity purity) {
        List<EmiStack> stacks = new ArrayList<>();
        for (Gem gem : eligibleGems(purity)) {
            EmiStack stack = EmiStack.of(gem.toStack(purity));
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    public static EmiIngredient gems(Purity purity) {
        List<EmiStack> stacks = gemsAt(purity);
        return stacks.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(stacks, 1L);
    }

/** One gem per second out of {@code from}'s list, shown at {@code show}. Two widgets built from the same */
    public static Function<Random, EmiIngredient> cycle(Purity from, Purity show) {
        List<Gem> gems = eligibleGems(from);
        return random -> gems.isEmpty() ? EmiStack.EMPTY : EmiStack.of(gems.get(random.nextInt(gems.size())).toStack(show));
    }

    public static List<Gem> sortedGems() {
        return GemRegistry.INSTANCE.getValues().stream()
                .sorted(Comparator.comparing(Gem::getId))
                .toList();
    }

    public static EmiIngredient sized(List<SizedIngredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return EmiStack.EMPTY;
        }
        List<EmiIngredient> options = new ArrayList<>();
        int amount = 1;
        for (SizedIngredient sized : ingredients) {
            if (sized.ingredient().isEmpty()) {
                continue;
            }
            amount = Math.max(amount, sized.count());
            options.add(EmiIngredient.of(sized.ingredient(), sized.count()));
        }
        return options.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(options, amount);
    }
}
