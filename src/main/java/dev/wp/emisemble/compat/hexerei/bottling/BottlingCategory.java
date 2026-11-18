package dev.wp.emisemble.compat.hexerei.bottling;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.wp.emisemble.porting.TranslatedCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import net.joefoxe.hexerei.data.recipes.CauldronEmptyingRecipe;
import net.joefoxe.hexerei.data.recipes.FluidMixingRecipe;
import net.joefoxe.hexerei.data.recipes.ModRecipeTypes;
import net.joefoxe.hexerei.fluid.PotionFluidHandler;
import net.joefoxe.hexerei.fluid.PotionMixingRecipes;
import net.joefoxe.hexerei.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/** {@code hexerei:bottling}, plus one synthetic page per potion the brew list can produce. */
public final class BottlingCategory {
    public static final String NAMESPACE = "hexerei";
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "bottling");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/bottling_gui_jei.png");
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID, EmiStack.of(ModItems.BLOOD_BOTTLE.get()), "gui.jei.category.bottling");

    private static final int BOTTLE_AMOUNT = 250;

    private BottlingCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);

            registry.addWorkstation(CATEGORY, EmiStack.of(ModItems.MIXING_CAULDRON.get()));
        } catch (Throwable t) {
            Emisemble.LOGGER.error("hexerei: could not register the bottling category", t);
            return 0;
        }

        RecipeType<CauldronEmptyingRecipe> type = ModRecipeTypes.CAULDRON_EMPTYING_TYPE.get();
        List<RecipeHolder<CauldronEmptyingRecipe>> holders;
        try {
            holders = registry.getRecipeManager().getAllRecipesFor(type);
        } catch (Throwable t) {
            Emisemble.LOGGER.error("hexerei: could not read cauldron emptying recipes", t);
            return 0;
        }

        int added = EmiRecipes.addAll(registry, type,
                holder -> new BottlingEmiRecipe(CATEGORY, holder.id(), holder.value().getInput(),
                        holder.value().getFluid(), holder.value().getResultItem()));

        List<FluidStack> bottled = new ArrayList<>(holders.size());
        for (RecipeHolder<CauldronEmptyingRecipe> holder : holders) {
            try {
                bottled.add(holder.value().getFluid());
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("hexerei: skipping the fluid of bottling recipe {}", holder.id(), t);
            }
        }

        added += addPotionPages(registry, bottled);

        Emisemble.LOGGER.info("{}: {} bottling recipes registered", NAMESPACE, added);
        return added;
    }

    // The host lists bottled potions from the runtime brew list, so these pages have no json and take a made-up id.
    private static int addPotionPages(EmiRegistry registry, List<FluidStack> bottled) {
        List<FluidMixingRecipe> mixes = PotionMixingRecipes.ALL;
        if (mixes == null || mixes.isEmpty()) {
            Level level = Minecraft.getInstance().level;
            if (level == null) {
                Emisemble.LOGGER.warn("hexerei bottling needs a client level for the potion mixes; none yet, skipping");
                return 0;
            }
            try {
                mixes = PotionMixingRecipes.createRecipes(level.potionBrewing());
                PotionMixingRecipes.ALL = mixes;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("hexerei: could not build the potion mixing recipes for bottling", t);
                return 0;
            }
        }

        int added = 0;
        int index = 0;
        for (FluidMixingRecipe mix : mixes) {
            try {
                FluidStack output = mix.getLiquidOutput();
                if (output == null || output.isEmpty() || alreadyBottled(bottled, output)) {
                    continue;
                }

                bottled.add(output);

                ItemStack bottle = PotionFluidHandler.fillBottle(output);
                Ingredient input = Ingredient.of(Items.GLASS_BOTTLE);
                FluidStack fluid = output.copyWithAmount(BOTTLE_AMOUNT);

                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "/bottling/potion_" + index++);
                if (EmiRecipes.add(registry, new BottlingEmiRecipe(CATEGORY, id, input, fluid, bottle))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("hexerei: skipping a bottling page for a potion mix", t);
            }
        }
        return added;
    }

    private static boolean alreadyBottled(List<FluidStack> bottled, FluidStack fluid) {
        for (FluidStack existing : bottled) {
            if (FluidStack.isSameFluidSameComponents(existing, fluid)) {
                return true;
            }
        }
        return false;
    }
}
