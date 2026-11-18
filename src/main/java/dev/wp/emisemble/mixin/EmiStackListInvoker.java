package dev.wp.emisemble.mixin;

import dev.emi.emi.registry.EmiStackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Re-runs EMI's index filter: a stream over the baked stacks, not a reload. Needed because EMI only rebuilds the
 * searchable list at bake time, so unlocking content would otherwise leave items hidden until the next reload.
 */
@Mixin(EmiStackList.class)
public interface EmiStackListInvoker {
    @Invoker("bakeFiltered")
    static void emiNatives$bakeFiltered() {
        throw new AssertionError("mixin not applied");
    }
}
