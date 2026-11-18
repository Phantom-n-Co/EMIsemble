package dev.wp.emisemble.compat.malum.extras;

import com.sammy.malum.registry.common.item.MalumItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import java.util.List;

/** Everything Malum's JEI plugin registers besides its categories: one info page, and the progression hiding. */
public final class ExtrasCompat {
    // What the host's call, farmersdelight's TextUtils#getTranslation("jei.info.knife"), resolves to.
    private static final String KNIFE_INFO_KEY = "jei.farmersdelight.info.knife";

    private ExtrasCompat() {}

    public static int register(EmiRegistry registry) {
        MalumVisibility.install();
        int info = knifeInfo(registry);
        Emisemble.LOGGER.info("malum: {} info pages registered", info);
        return info;
    }

    /** The host only adds this page when Farmer's Delight is present, because the knife cuts food only with it. */
    private static int knifeInfo(EmiRegistry registry) {
        if (!ModList.get().isLoaded("farmersdelight")) {
            return 0;
        }
        return EmiRecipes.add(registry, new EmiInfoRecipe(
                List.<EmiIngredient>of(EmiStack.of(MalumItems.SOUL_STAINED_STEEL_KNIFE.get())),
                List.of(Component.translatable(KNIFE_INFO_KEY)),
                ResourceLocation.fromNamespaceAndPath("malum", "/info/soul_stained_steel_knife"))) ? 1 : 0;
    }
}
