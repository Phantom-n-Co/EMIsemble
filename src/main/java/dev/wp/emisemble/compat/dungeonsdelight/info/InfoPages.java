package dev.wp.emisemble.compat.dungeonsdelight.info;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.InfoText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yirmiri.dungeonsdelight.core.registry.DDItems;
import net.yirmiri.dungeonsdelight.integration.content.twilightforest.TFItems;

import java.util.ArrayList;
import java.util.List;

/** The seven texts the host puts on twenty items in JEI, merged by text so no two pages share an id. */
public final class InfoPages {
    private static final String NAMESPACE = "dungeonsdelight";

    private InfoPages() {}

    public static void register(EmiRegistry registry) {
        int added = 0;
        added += page(registry, "stained_scrap", "farmersdelight.jei.info.stained_scrap",
                List.of(DDItems.STAINED_SCRAP.get()));
        added += page(registry, "rock_candy", "farmersdelight.jei.info.rock_candy",
                List.of(DDItems.AMETHYST_ROCK_CANDY.get()));
        added += page(registry, "candied_vex", "farmersdelight.jei.info.candied_vex",
                List.of(DDItems.CANDIED_VEX_SUCKER.get()));
        added += page(registry, "candied_silverfish", "farmersdelight.jei.info.candied_silverfish",
                List.of(DDItems.CANDIED_SILVERFISH_SUCKER.get()));
        List<Item> cleavers = new ArrayList<>(List.of(DDItems.FLINT_CLEAVER.get(), DDItems.GOLDEN_CLEAVER.get(),
                DDItems.IRON_CLEAVER.get(), DDItems.DIAMOND_CLEAVER.get(), DDItems.NETHERITE_CLEAVER.get(),
                DDItems.STAINED_CLEAVER.get()));
        List<Item> knives = new ArrayList<>(List.of(DDItems.STAINED_KNIFE.get()));
        cleavers.addAll(List.of(TFItems.IRONWOOD_CLEAVER.get(), TFItems.KNIGHTMETAL_CLEAVER.get(),
                TFItems.STEELEAF_CLEAVER.get(), TFItems.FIERY_CLEAVER.get()));
        knives.addAll(List.of(TFItems.IRONWOOD_KNIFE.get(), TFItems.KNIGHTMETAL_KNIFE.get(),
                TFItems.STEELEAF_KNIFE.get(), TFItems.FIERY_KNIFE.get()));
        added += page(registry, "cleaver", "farmersdelight.jei.info.cleaver", cleavers);
        added += page(registry, "knife", "jei.farmersdelight.info.knife", knives);
        added += page(registry, "rancid_reduction", "farmersdelight.jei.info.rancid_reduction",
                List.of(DDItems.RANCID_REDUCTION.get()));

        Emisemble.LOGGER.info("{}: {} info pages registered", NAMESPACE, added);
    }

    private static int page(EmiRegistry registry, String name, String translationKey, List<Item> items) {
        List<EmiIngredient> stacks = new ArrayList<>();
        for (Item item : items) {
            stacks.add(EmiStack.of(new ItemStack(item)));
        }

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "/info/" + name);
        EmiInfoRecipe recipe = new EmiInfoRecipe(stacks,
                InfoText.lines(Component.translatable(translationKey)), id);
        return EmiRecipes.add(registry, recipe) ? 1 : 0;
    }
}
