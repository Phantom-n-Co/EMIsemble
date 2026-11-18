package dev.wp.emisemble.mixin;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiStackList;
import dev.wp.emisemble.Emisemble;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** The Apotheosis family registers its items across the vanilla creative tabs, so EMI's index scatters them through This cannot be expressed as a plugin: the index data file can only add and remove, and `addEmiStackAfter` appends a new entry instead of moving an existing one, so regrouping through either route duplicates stacks. */
@Mixin(EmiStackList.class)
public abstract class EmiStackIndexGroupMixin {
    @Shadow
    public static List<EmiStack> stacks;

    private static final String[] FAMILY = {"apotheosis", "apothic_enchanting", "apothic_spawners"};

    @Inject(method = "bake", at = @At("HEAD"))
    private static void emiNatives$groupApotheosis(CallbackInfo ci) {
        boolean[] enabled = new boolean[FAMILY.length];
        for (int i = 0; i < FAMILY.length; i++) {
            enabled[i] = Emisemble.isAvailable(FAMILY[i]);
        }
        List<EmiStack> family = new ArrayList<>();
        List<EmiStack> rest = new ArrayList<>(stacks.size());
        int at = -1;
        for (int i = 0; i < stacks.size(); i++) {
            EmiStack stack = stacks.get(i);
            if (rank(stack, enabled) < 0) {
                rest.add(stack);
            } else {
                if (at < 0) {
                    at = i;
                }
                family.add(stack);
            }
        }
        if (family.size() < 2) {
            return;
        }
        // Group by mod only: the list is stable, so each mod keeps the creative-tab order it had in the index.
        family.sort(Comparator.comparingInt(stack -> rank(stack, enabled)));
        rest.addAll(at, family);
        stacks = rest;
    }

    private static int rank(EmiStack stack, boolean[] enabled) {
        ResourceLocation id = stack.getId();
        if (id == null) {
            return -1;
        }
        for (int i = 0; i < FAMILY.length; i++) {
            if (enabled[i] && FAMILY[i].equals(id.getNamespace())) {
                return i;
            }
        }
        return -1;
    }
}
