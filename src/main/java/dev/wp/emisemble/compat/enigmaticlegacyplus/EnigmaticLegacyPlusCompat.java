package dev.wp.emisemble.compat.enigmaticlegacyplus;

import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.enigmaticlegacyplus.crafting.EnigmaticCraftingCompat;
import dev.wp.emisemble.compat.enigmaticlegacyplus.spellstone.SpellstoneCompat;
import dev.wp.emisemble.compat.enigmaticlegacyplus.tainting.TaintingCategory;

/** Everything Enigmatic Legacy Plus contributes: two categories, and the crafting pages JEI renders by extension. */
public final class EnigmaticLegacyPlusCompat {
    private EnigmaticLegacyPlusCompat() {}

    static void register(EmiRegistry registry) {
        int tainting = TaintingCategory.register(registry);
        int spellstone = SpellstoneCompat.register(registry);
        EnigmaticCraftingCompat.register(registry);
        Emisemble.LOGGER.info("enigmaticlegacyplus: {} tainting and {} spellstone recipes registered",
                tainting, spellstone);
    }
}
