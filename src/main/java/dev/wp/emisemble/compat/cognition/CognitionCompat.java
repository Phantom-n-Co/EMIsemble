package dev.wp.emisemble.compat.cognition;

import com.cyanogen.cognition.gui.MolecularMetamorpherMenu;
import com.cyanogen.cognition.registries.RegisterItems;
import com.cyanogen.cognition.registries.RegisterMenus;
import com.cyanogen.cognition.utils.ExperienceUtils;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.cognition.emptying.EmptyingCategory;
import dev.wp.emisemble.compat.cognition.filling.FillingCategory;
import dev.wp.emisemble.compat.cognition.infecting.InfectingCategory;
import dev.wp.emisemble.compat.cognition.metamorphosis.MetamorphosisCategory;
import dev.wp.emisemble.compat.cognition.metamorphosis.MetamorphosisEmiRecipe;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.TransferSlots;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Function;

/** Cognition's four categories plus the three registrations its JEI plugin makes beside them. */
public final class CognitionCompat {
    static final ResourceLocation COGNITIUM =
            ResourceLocation.fromNamespaceAndPath("cognition", "textures/block/cognitium_still.png");

    private CognitionCompat() {}

    static void register(EmiRegistry registry) {
        // EMI discards a plugin's whole registration set on one throw, so each category stands alone.
        int added = guard(registry, "emptying", EmptyingCategory::register)
                + guard(registry, "filling", FillingCategory::register)
                + guard(registry, "infecting", InfectingCategory::register)
                + guard(registry, "molecular metamorphosis", MetamorphosisCategory::register);
        Emisemble.LOGGER.info("cognition: {} recipes registered across 4 categories", added);

        forgottenDust(registry);
        hideDummySword(registry);
        MetamorpherTransfer.register(registry);
    }

    private static int guard(EmiRegistry registry, String name, Function<EmiRegistry, Integer> register) {
        try {
            return register.apply(registry);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("cognition: could not register the {} category", name, t);
            return 0;
        }
    }

    /** The cognitium a page spends or yields: the host draws the still icon and a tooltip, never a fluid stack. */
    public static void cognitium(WidgetHolder widgets, int x, int y, int mB) {
        if (mB <= 0) {
            return;
        }
        AlphaTextures.add(widgets, COGNITIUM, 16, x, y, 16, 16, 0, 0);
        long xp = mB / 20L;
        widgets.addTooltipText(List.of(
                Component.translatable("jei.cognition.shared.cognitium_amount.mb", green(mB)),
                Component.translatable("jei.cognition.shared.cognitium_amount.levels_xp",
                        green(ExperienceUtils.xpToLevels(xp)), green(xp))), x, y, 16, 16);
    }

    public static Component green(long value) {
        return Component.literal(String.valueOf(value)).withStyle(ChatFormatting.GREEN);
    }

    private static void forgottenDust(EmiRegistry registry) {
        try {
            registry.addRecipe(new EmiInfoRecipe(
                    List.<EmiIngredient>of(EmiStack.of(RegisterItems.FORGOTTEN_DUST.get())),
                    List.of(Component.translatable("jei.cognition.description.forgotten_dust")),
                    ResourceLocation.fromNamespaceAndPath("cognition", "/forgotten_dust_info")));
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("cognition: could not add the forgotten dust info page", t);
        }
    }

    // The dummy sword exists only to carry the invented item names of the name formatting page.
    private static void hideDummySword(EmiRegistry registry) {
        try {
            Item dummy = RegisterItems.DUMMY_SWORD.get();
            registry.removeEmiStacks(stack -> stack.getKey() == dummy);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("cognition: could not hide the dummy sword", t);
        }
    }

    private static final class MetamorpherTransfer implements StandardRecipeHandler<MolecularMetamorpherMenu> {
        private static final int PLAYER_START = 4;
        private static final int INPUT_SLOTS = 3;

        static void register(EmiRegistry registry) {
            try {
                registry.addRecipeHandler(RegisterMenus.MOLECULAR_METAMORPHER_MENU.get(), new MetamorpherTransfer());
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("cognition: could not register the molecular metamorpher transfer handler", t);
            }
        }

        @Override
        public List<Slot> getInputSources(MolecularMetamorpherMenu menu) {
            // Its own three inputs first: EMI empties them into the inventory before it fills.
            return TransferSlots.sources(getCraftingSlots(menu), menu.slots.subList(PLAYER_START, menu.slots.size()));
        }

        @Override
        public List<Slot> getCraftingSlots(MolecularMetamorpherMenu menu) {
            return menu.slots.subList(0, INPUT_SLOTS);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe instanceof MetamorphosisEmiRecipe metamorphosis && !metamorphosis.isNameFormatting();
        }
    }
}
