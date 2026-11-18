package dev.wp.emisemble.compat.malum.spirit_repair;

import com.sammy.malum.common.recipe.spirit_repair.SpiritRepairRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** The spirit row across the top, the repair material beside the damaged item, the repaired item below. */
final class SpiritRepairEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    private static final int SPIRIT_X = 56;
    private static final int SPIRIT_Y = 8;
    private static final int MATERIAL_X = 39;
    private static final int MATERIAL_Y = 53;
    private static final int DAMAGED_X = 77;
    private static final int DAMAGED_Y = 53;
    private static final int REPAIRED_X = 58;
    private static final int REPAIRED_Y = 120;

    private final RecipeHolder<SpiritRepairRecipe> holder;
    private final List<EmiStack> spirits;
    private final EmiIngredient material;
    private final EmiIngredient damaged;
    private final List<EmiStack> repairedStacks;
    private final EmiIngredient repaired;

    SpiritRepairEmiRecipe(RecipeHolder<SpiritRepairRecipe> holder) {
        super(SpiritRepairCompat.CATEGORY, holder.id(), SpiritRepairCompat.WIDTH, SpiritRepairCompat.HEIGHT);
        SpiritRepairRecipe recipe = holder.value();
        this.holder = holder;
        // asItemStack carries the spirit's own shard count, which is what the host's slot shows.
        this.spirits = recipe.spirits.stream().map(spirit -> EmiStack.of(spirit.asItemStack())).toList();
        this.material = collapsed(stacks(List.of(recipe.repairMaterial.getItems())), recipe.repairMaterial.count());
        // Damaged and repaired are stacks carrying a Damage component, and a tag-shaped ingredient would put the
        // pristine item in the slot instead — the wear is the whole point of the page.
        this.damaged = collapsed(stacks(recipe.getDamagedItems()), 1);
        this.repairedStacks = stacks(recipe.getRepairedItems());
        this.repaired = collapsed(repairedStacks, 1);

        inputs.addAll(spirits);
        inputs.add(material);
        inputs.add(damaged);
        outputs.addAll(repairedStacks);
    }

    private static List<EmiStack> stacks(List<ItemStack> stacks) {
        return stacks.stream().map(EmiStack::of).toList();
    }

    /** One stack reads better as the stack itself; a longer list has to cycle through them. */
    private static EmiIngredient collapsed(List<EmiStack> stacks, long amount) {
        return stacks.size() == 1 ? stacks.getFirst() : new ListEmiIngredient(stacks, amount);
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return holder;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, SpiritRepairCompat.TEXTURE, SpiritRepairCompat.SHEET,
                -4, -4, SpiritRepairCompat.WIDTH + 8, SpiritRepairCompat.HEIGHT + 8, 1, 0);
        if (!spirits.isEmpty()) {
            sockets(widgets, spirits.size());
            int left = leftOfRow(spirits.size());
            for (int i = 0; i < spirits.size(); i++) {
                RecipeSlots.add(widgets, spirits.get(i), left + 18 * i, SPIRIT_Y);
            }
        }
        RecipeSlots.add(widgets, material, MATERIAL_X, MATERIAL_Y);
        RecipeSlots.add(widgets, damaged, DAMAGED_X, DAMAGED_Y);
        RecipeSlots.add(widgets, repaired, REPAIRED_X, REPAIRED_Y).recipeContext(this);
    }

    private static int leftOfRow(int slots) {
        return SPIRIT_X - 9 * (slots - 1);
    }

    // The host draws the codex socket sheet behind the spirit row because the page art has no frames there.
    private static void sockets(WidgetHolder widgets, int slots) {
        int left = leftOfRow(slots);
        for (int i = 0; i < slots; i++) {
            AlphaTextures.add(widgets, SpiritRepairCompat.SOCKETS, SpiritRepairCompat.SOCKET_SHEET,
                    left - 1 + 18 * i, SPIRIT_Y - 3, 18, 22, 2, 0);
        }
        AlphaTextures.add(widgets, SpiritRepairCompat.SOCKETS, SpiritRepairCompat.SOCKET_SHEET,
                left - 3, SPIRIT_Y - 3, 2, 22, 0, 0);
        AlphaTextures.add(widgets, SpiritRepairCompat.SOCKETS, SpiritRepairCompat.SOCKET_SHEET,
                left - 1 + 18 * slots, SPIRIT_Y - 3, 2, 22, 20, 0);
    }
}
