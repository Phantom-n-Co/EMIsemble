package dev.wp.emisemble.compat.paganbless;

import com.pigdad.paganbless.content.blocks.BaseHangingHerbBlock;
import com.pigdad.paganbless.content.blocks.HangingHerbBlock;
import com.pigdad.paganbless.content.blocks.WaxedHangingHerbBlock;
import com.pigdad.paganbless.registries.PBItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.compat.paganbless.anvil.AnvilSmashingCategory;
import dev.wp.emisemble.compat.paganbless.bench.BenchCuttingCategory;
import dev.wp.emisemble.compat.paganbless.cauldron.CauldronImbuingCategory;
import dev.wp.emisemble.compat.paganbless.ritual.RunicRitualCategory;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.InfoText;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/** Registers Pagan's Blessing's four categories, its workstations and the five info pages its JEI plugin adds. */
public final class PaganblessCompat {
    private static final String NAMESPACE = "paganbless";

    private PaganblessCompat() {}

    public static void register(EmiRegistry registry) {
        AnvilSmashingCategory.register(registry);
        BenchCuttingCategory.register(registry);
        CauldronImbuingCategory.register(registry);
        RunicRitualCategory.register(registry);

        info(registry, "dried_herbs", herbs(true), "jei_info.paganbless.dried_herbs_info");
        info(registry, "waxed_herbs", herbs(false), "jei_info.paganbless.waxed_herbs_info");
        info(registry, "skeleton_skull", EmiStack.of(Items.SKELETON_SKULL), "jei_info.paganbless.skeleton_skull");
        info(registry, "cinnabar", EmiStack.of(PBItems.CINNABAR.get()), "jei_info.paganbless.cinnabar");
        info(registry, "essence_of_the_forest", EmiStack.of(PBItems.ESSENCE_OF_THE_FOREST.get()),
                "jei_info.paganbless.essence_of_the_forest");
    }

    private static void info(EmiRegistry registry, String path, EmiIngredient stacks, String key) {
        ResourceLocation id = EmiRecipes.synthetic(ResourceLocation.fromNamespaceAndPath(NAMESPACE, "info/" + path));
        EmiRecipes.add(registry, new EmiInfoRecipe(List.of(stacks), InfoText.lines(Component.translatable(key)), id));
    }

    /** One page per family, its ingredient cycling the whole list, which is what JEI's list argument renders. */
    private static EmiIngredient herbs(boolean dried) {
        List<EmiStack> stacks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            boolean match = dried
                    ? block instanceof BaseHangingHerbBlock && !(block instanceof WaxedHangingHerbBlock)
                    : block instanceof HangingHerbBlock || block instanceof WaxedHangingHerbBlock;
            if (match && block.asItem() != Items.AIR) {
                stacks.add(EmiStack.of(block.asItem()));
            }
        }
        return EmiIngredient.of(stacks);
    }
}
