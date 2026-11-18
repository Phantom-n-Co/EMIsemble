package dev.wp.emisemble.compat.thaumaturge.infernal;

import com.leclowndu93150.thaumaturge.content.infernalfurnace.InfernalBonus;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import java.util.List;

/** {@code thaumaturge:infernal_furnace}: no recipe type, so the table is rebuilt from the infernal_bonus data map. */
public final class InfernalFurnaceCompat {
    public static final EmiRecipeCategory INFERNAL_FURNACE = new TranslatedCategory(
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "infernal_furnace"),
            EmiStack.of(TTItems.INFERNAL_FURNACE.get()), "jei.thaumaturge.category.infernal_furnace");

    private InfernalFurnaceCompat() {}

    public static void register(EmiRegistry registry) {
        registry.addCategory(INFERNAL_FURNACE);
        registry.addWorkstation(INFERNAL_FURNACE, EmiStack.of(TTItems.INFERNAL_FURNACE.get()));
        int added = addBonuses(registry);
        Emisemble.LOGGER.info("thaumaturge: {} infernal furnace recipes registered", added);
    }

    // The item registry, not the stack universe: the input ingredient is per item, so variants would only duplicate.
    private static int addBonuses(EmiRegistry registry) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return 0;
        }

        List<RecipeHolder<SmeltingRecipe>> smelting =
                registry.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING);
        RegistryAccess registries = level.registryAccess();
        int added = 0;

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) {
                continue;
            }
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            Holder.Reference<Item> holder = BuiltInRegistries.ITEM.getHolder(key).orElse(null);
            if (holder == null) {
                continue;
            }
            List<InfernalBonus> bonuses = holder.getData(InfernalBonus.DATA_MAP);
            if (bonuses == null || bonuses.isEmpty()) {
                continue;
            }

            SingleRecipeInput input = new SingleRecipeInput(new ItemStack(item));
            for (RecipeHolder<SmeltingRecipe> recipe : smelting) {
                if (!recipe.value().matches(input, level)) {
                    continue;
                }
                // First match in recipe-manager order, then stop: the same default result JEI picks.
                try {
                    registry.addRecipe(new InfernalFurnaceEmiRecipe(
                            idOf(key),
                            Ingredient.of(item),
                            recipe.value().assemble(input, registries),
                            bonuses));
                    added++;
                } catch (Throwable t) {
                    Emisemble.LOGGER.warn("Skipping infernal furnace page for {}", key, t);
                }
                break;
            }
        }
        return added;
    }

    // Synthetic, so the item's own id has to be in the path to keep the recipe ids unique.
    private static ResourceLocation idOf(ResourceLocation key) {
        return ResourceLocation.fromNamespaceAndPath("thaumaturge",
                "/infernal_furnace/" + key.getNamespace() + "/" + key.getPath());
    }
}
