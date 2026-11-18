package dev.wp.emisemble.mixin;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiStackList;
import dev.wp.emisemble.porting.EmiVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

/** Rebuilds the searchable list, so filtering here is live without touching the bake. */
@Mixin(EmiStackList.class)
public abstract class EmiStackListMixin {
    @Shadow
    public static List<EmiStack> filteredStacks;

    @Inject(method = "bakeFiltered", at = @At("TAIL"))
    private static void emiNatives$filter(CallbackInfo ci) {
        Predicate<EmiStack> hidden = EmiVisibility.hiddenStack();
        if (hidden != null) {
            filteredStacks = filteredStacks.stream().filter(hidden.negate()).toList();
        }
    }
}
