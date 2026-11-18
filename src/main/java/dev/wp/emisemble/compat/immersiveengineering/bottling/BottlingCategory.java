package dev.wp.emisemble.compat.immersiveengineering.bottling;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.TagOutputList;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersiveengineering.IeIds;
import dev.wp.emisemble.compat.immersiveengineering.IeRecipes;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@code immersiveengineering:bottling_machine}. The host's page is a blank sheet holding nothing but the machine
 * icon and its three fluid sockets, so every widget here is drawn by the port.
 */
public final class BottlingCategory {
    static final int WIDTH = 120;
    static final int HEIGHT = 56;

    private static final int BUCKET_AMOUNT = 1000;
    private static final String BUCKET_ID = "bottling_bucket";
    private static final String POTION_ID_PREFIX = "bottling_potion/";
    private static final int ICON_X = 14;
    private static final float ICON_SCALE = 3.0F;

    static final EmiStack ICON = EmiStack.of(IEMultiblockLogic.BOTTLING_MACHINE.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.BOTTLING_MACHINE.get()), ICON,
            "block.immersiveengineering.bottling_machine");
    // JEI registers the generated potion mixes and the bucket filling as two more categories, each with its own uid and
    // the machine's name plus the host's suffix, as the recycling category does.
    public static final EmiRecipeCategory POTIONS = new TranslatedCategory(IEApi.ieLoc("bottling_potions"), ICON,
            Component.translatable("block.immersiveengineering.bottling_machine")
                    .append(Component.translatable("desc.immersiveengineering.jei.category.potions")));
    public static final EmiRecipeCategory BUCKETS = new TranslatedCategory(IEApi.ieLoc("bottling_buckets"), ICON,
            Component.translatable("block.immersiveengineering.bottling_machine")
                    .append(Component.translatable("desc.immersiveengineering.jei.category.buckets")));

    private BottlingCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
            registry.addCategory(POTIONS);
            registry.addWorkstation(POTIONS, ICON);
            registry.addCategory(BUCKETS);
            registry.addWorkstation(BUCKETS, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the bottling machine category", t);
            return 0;
        }
        // The cache, not the manager: the potion list is a generated-list placeholder there and would be skipped.
        Map<String, List<RecipeHolder<BottlingMachineRecipe>>> families = new TreeMap<>();
        int added = 0;
        for (RecipeHolder<BottlingMachineRecipe> holder : IeRecipes.all(BottlingMachineRecipe.RECIPES)) {
            try {
                String shape = BottlingCyclePage.shape(holder.value());
                if (shape == null) {
                    if (EmiRecipes.add(registry, new BottlingEmiRecipe(CATEGORY, IeIds.of(holder), holder.value()))) {
                        added++;
                    }
                } else {
                    families.computeIfAbsent(shape, key -> new ArrayList<>()).add(holder);
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("immersiveengineering: skipping bottling recipe {}", holder.id(), t);
            }
        }

        int collapsed = 0;
        int folded = 0;
        for (List<RecipeHolder<BottlingMachineRecipe>> family : families.values()) {
            try {
                // A family of one has nothing to cycle, and keeping its own id keeps that page where it always was.
                if (family.size() == 1) {
                    RecipeHolder<BottlingMachineRecipe> only = family.get(0);
                    if (EmiRecipes.add(registry, new BottlingEmiRecipe(CATEGORY, IeIds.of(only), only.value()))) {
                        added++;
                    }
                    continue;
                }
                ResourceLocation id = IeIds.synthetic(CATEGORY.getId().getNamespace(), POTION_ID_PREFIX + collapsed);
                if (EmiRecipes.add(registry, BottlingCyclePage.of(POTIONS, id, values(family)))) {
                    added++;
                    folded += family.size();
                    collapsed++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("immersiveengineering: skipping a bottling potion family", t);
            }
        }

        // One page per bucket now that the uid is its own category: folding 175 of them into one cycling page was the
        // price of sharing the bottling machine's uid, and it is no longer owed.
        Map<ResourceLocation, BottlingMachineRecipe> buckets = bucketRecipes();
        int bucketPages = 0;
        for (Map.Entry<ResourceLocation, BottlingMachineRecipe> bucket : buckets.entrySet()) {
            try {
                String path = BUCKET_ID + "/" + bucket.getKey().getNamespace() + "_" + bucket.getKey().getPath();
                ResourceLocation id = IeIds.synthetic(CATEGORY.getId().getNamespace(), path);
                if (EmiRecipes.add(registry, new BottlingEmiRecipe(BUCKETS, id, bucket.getValue()))) {
                    bucketPages++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("immersiveengineering: skipping the bucket page for {}", bucket.getKey(), t);
            }
        }
        Emisemble.LOGGER.info(
                "immersiveengineering: {} bottling machine recipes ({} potion families cycle {} generated mixes, "
                        + "{} bucket pages holding {})",
                added + bucketPages, collapsed, folded, bucketPages, buckets.size());
        return added + bucketPages;
    }

    // JEIHelper#getFluidBucketRecipes invents these from the fluid registry: every source fluid with a bucket item
    // becomes "empty bucket + 1,000 mB -> that bucket", built as a real recipe so the page lays out like the others.
    private static Map<ResourceLocation, BottlingMachineRecipe> bucketRecipes() {
        Map<ResourceLocation, BottlingMachineRecipe> recipes = new LinkedHashMap<>();
        for (Holder.Reference<Fluid> holder : BuiltInRegistries.FLUID.holders().toList()) {
            try {
                Fluid fluid = holder.value();
                if (!fluid.isSource(fluid.defaultFluidState())) {
                    continue;
                }
                ItemStack filled = fluid.getBucket().getDefaultInstance();
                if (filled.isEmpty()) {
                    continue;
                }
                recipes.put(holder.key().location(), new BottlingMachineRecipe(
                        new TagOutputList(new TagOutput(filled)),
                        IngredientWithSize.of(new ItemStack(Items.BUCKET)),
                        SizedFluidIngredient.of(fluid, BUCKET_AMOUNT)));
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("immersiveengineering: skipping the bucket recipe for {}",
                        holder.key().location(), t);
            }
        }
        return recipes;
    }

    private static List<BottlingMachineRecipe> values(List<RecipeHolder<BottlingMachineRecipe>> family) {
        List<BottlingMachineRecipe> recipes = new ArrayList<>(family.size());
        for (RecipeHolder<BottlingMachineRecipe> holder : family) {
            recipes.add(holder.value());
        }
        return recipes;
    }

    static void machine(WidgetHolder widgets) {
        widgets.addDrawable(0, 0, WIDTH, HEIGHT, (draw, mouseX, mouseY, delta) -> {
            draw.pose().pushPose();
            draw.pose().scale(ICON_SCALE, ICON_SCALE, 1.0F);
            ICON.render(draw, ICON_X, 0, delta);
            draw.pose().popPose();
        });
    }
}
