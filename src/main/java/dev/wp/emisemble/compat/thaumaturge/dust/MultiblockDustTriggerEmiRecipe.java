package dev.wp.emisemble.compat.thaumaturge.dust;

import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintSource;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintTarget;
import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.thaumaturge.ThaumaturgeCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Salis mundus onto a structure: turning blueprint model, block bill along the bottom, plate on the right. */
public final class MultiblockDustTriggerEmiRecipe extends BasicEmiRecipe {
    private static final int WIDTH = 144;
    private static final int HEIGHT = 108;

    // EMI-space: JEI draws at 22,-2 and 119,46, so the 18x18 bounds sit one pixel up and left.
    private static final int DUST_X = 21;
    private static final int DUST_Y = -3;
    private static final int RESULT_X = 118;
    private static final int RESULT_Y = 45;
    private static final int PLATE_X = 112;
    private static final int PLATE_Y = 39;
    private static final int ARROW_X = 39;
    private static final int ARROW_Y = 0;
    private static final int ARROW_SIZE = 26;
    private static final int PARTS_X = 4;
    private static final int PARTS_Y = 87;
    private static final int PARTS_STEP = 20;
    private static final int LOCK_X = 45;
    private static final int LOCK_Y = 4;

    private static final Component USAGE =
            Component.translatable("jei.thaumaturge.dust_trigger.target.multiblock");

    private final DustTrigger recipe;
    private final List<EmiIngredient> parts;
    private final Map<BlockPos, BlockState> blocks;
    private final ItemStack result;
    private int rotation;

    public MultiblockDustTriggerEmiRecipe(RecipeHolder<DustTrigger> holder) {
        super(DustTriggerCompat.MULTIBLOCK, holder.id(), WIDTH, HEIGHT);
        this.recipe = holder.value();
        Blueprint blueprint = recipe instanceof DustTriggerMultiblockRecipe multi
                ? lookup(multi.blueprintId())
                : null;
        ItemStack raw = DustTriggerCompat.result(recipe);
        this.result = raw.isEmpty() || keepsResult(blueprint, raw) ? ItemStack.EMPTY : raw;
        this.parts = inputs(blueprint);
        this.blocks = blueprint == null ? Map.of() : Preview.blocks(blueprint);

        inputs.add(DustTriggerCompat.salisMundus());
        inputs.addAll(parts);
        if (!result.isEmpty()) {
            outputs.add(EmiStack.of(result));
        }
    }

    /** Null when the recipe names a blueprint that is not in the client's registry, exactly like the host. */
    private static Blueprint lookup(ResourceLocation blueprintId) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        ResourceKey<Blueprint> key = ResourceKey.create(Blueprint.REGISTRY_KEY, blueprintId);
        return level.registryAccess()
                .lookup(Blueprint.REGISTRY_KEY)
                .flatMap(lookup -> lookup.get(key))
                .<Blueprint>map(Holder::value)
                .orElse(null);
    }

    // Most-used block first; ties by id, because the host's hash-order tie-break reshuffles between runs.
    private static List<EmiIngredient> inputs(Blueprint blueprint) {
        if (blueprint == null) {
            return List.of();
        }

        Map<BlueprintSource, Integer> counts = new LinkedHashMap<>();
        for (int y = 0; y < blueprint.ySize(); y++) {
            for (int x = 0; x < blueprint.xSize(); x++) {
                for (int z = 0; z < blueprint.zSize(); z++) {
                    BlueprintPart part = blueprint.cell(y, x, z);
                    if (part != null && !part.source().getRepresentations().isEmpty()) {
                        counts.merge(part.source(), 1, Integer::sum);
                    }
                }
            }
        }

        List<Map.Entry<BlueprintSource, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort(Comparator.<Map.Entry<BlueprintSource, Integer>>comparingInt(Map.Entry::getValue)
                .reversed()
                .thenComparing(entry -> stableKey(entry.getKey())));

        List<EmiIngredient> ingredients = new ArrayList<>(sorted.size());
        for (Map.Entry<BlueprintSource, Integer> entry : sorted) {
            List<EmiStack> stacks = entry.getKey().getRepresentations().stream()
                    .filter(stack -> !stack.isEmpty())
                    .<EmiStack>map(EmiStack::of)
                    .toList();
            if (!stacks.isEmpty()) {
                ingredients.add(EmiIngredient.of(stacks, entry.getValue()));
            }
        }
        return ingredients;
    }

    /** True when the blueprint keeps the crafted block, i.e. the host hides the output slot for it. */
    private static boolean keepsResult(Blueprint blueprint, ItemStack result) {
        if (blueprint == null || result.isEmpty()) {
            return false;
        }
        for (int y = 0; y < blueprint.ySize(); y++) {
            for (int x = 0; x < blueprint.xSize(); x++) {
                for (int z = 0; z < blueprint.zSize(); z++) {
                    BlueprintPart part = blueprint.cell(y, x, z);
                    if (part != null && part.target() instanceof BlueprintTarget.Keep) {
                        for (ItemStack representation : part.source().getRepresentations()) {
                            if (ItemStack.isSameItemSameComponents(result, representation)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static String stableKey(BlueprintSource source) {
        ResourceLocation id = null;
        if (source instanceof BlueprintSource.BlockSource block) {
            id = BuiltInRegistries.BLOCK.getKey(block.block());
        } else if (source instanceof BlueprintSource.TagSource tag) {
            id = tag.tag().location();
        } else if (source instanceof BlueprintSource.StateSource state) {
            id = BuiltInRegistries.BLOCK.getKey(state.state().getBlock());
        }
        return source.kind().getSerializedName() + "/" + id;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // First, so the arrow, the slots and the plate stay on top of the blocks.
        if (!blocks.isEmpty()) {
            widgets.addDrawable(0, 0, WIDTH, HEIGHT, (draw, mouseX, mouseY, delta) ->
                    Preview.render(draw, blocks, rotation++));
        }
        ThaumaturgeCompat.art(widgets, ARROW_X, ARROW_Y, ARROW_SIZE, ARROW_SIZE, 199, 168);
        if (!result.isEmpty()) {
            ThaumaturgeCompat.art(widgets, PLATE_X, PLATE_Y, 30, 30, 41, 7);
        }

        widgets.addSlot(DustTriggerCompat.salisMundus(), DUST_X, DUST_Y).drawBack(false).appendTooltip(USAGE);
        for (int i = 0; i < parts.size(); i++) {
            widgets.addSlot(parts.get(i), PARTS_X + i * PARTS_STEP, PARTS_Y).drawBack(false);
        }
        if (!result.isEmpty()) {
            widgets.addSlot(EmiStack.of(result), RESULT_X, RESULT_Y).drawBack(false).recipeContext(this);
        }

        if (!recipe.doesPassGate(Minecraft.getInstance().player)) {
            widgets.addDrawable(LOCK_X, LOCK_Y, 16, 16, (draw, mouseX, mouseY, delta) ->
                    draw.renderItem(Items.BARRIER.getDefaultInstance(), 0, 0));
        }
        widgets.addTooltip(this::tooltip, LOCK_X, LOCK_Y, 18, 16);
    }

    private List<ClientTooltipComponent> tooltip(int mouseX, int mouseY) {
        if (recipe.doesPassGate(Minecraft.getInstance().player)) {
            return List.of();
        }
        return recipe.researchGate()
                .map(gate -> EmiRecipes.tooltip(ThaumaturgeCompat.missingResearch(gate)))
                .orElse(List.of());
    }

    /** The blueprint as a block map, drawn the way the host draws it: layers flipped, tipped back, spun per frame. */
    private static final class Preview {
        private static final int FULL_BRIGHT = 15728880;
        private static final float CENTRE_X = 54.5F;
        private static final float CENTRE_Y = 56.5F;
        private static final float SCALE = 15.0F;
        private static final float DEPTH = 200.0F;
        private static final float TILT = 25.0F;
        // The host spins by rotation++ per drawn frame, i.e. 11.25 degrees, not on a timer.
        private static final float DEGREES_PER_FRAME = 1.0F / 8.0F;
        private static final float BASE_SPIN = 90.0F;
        private static final BlockEntityInfusionMatrix MATRIX = new BlockEntityInfusionMatrix(
                BlockPos.ZERO, TTBlocks.INFUSION_MATRIX.get().defaultBlockState());

        private Preview() {}

        static Map<BlockPos, BlockState> blocks(Blueprint blueprint) {
            Map<BlockPos, BlockState> blocks = new HashMap<>();
            for (int y = 0; y < blueprint.ySize(); y++) {
                for (int x = 0; x < blueprint.xSize(); x++) {
                    for (int z = 0; z < blueprint.zSize(); z++) {
                        BlueprintPart part = blueprint.cell(y, x, z);
                        if (part != null) {
                            blocks.put(new BlockPos(x, -y + (blueprint.ySize() - 1), z), part.source().getState());
                        }
                    }
                }
            }
            return blocks;
        }

        static void render(GuiGraphics graphics, Map<BlockPos, BlockState> blocks, int rotation) {
            if (blocks.isEmpty()) {
                return;
            }

            int[] bounds = boundsOf(blocks);
            float centreX = (bounds[0] + bounds[3] + 1) / 2.0F;
            float centreY = (bounds[1] + bounds[4] + 1) / 2.0F;
            float centreZ = (bounds[2] + bounds[5] + 1) / 2.0F;

            BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            BufferSource buffers = graphics.bufferSource();
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(CENTRE_X, CENTRE_Y, DEPTH);
            pose.scale(SCALE, -SCALE, SCALE);
            pose.mulPose(Axis.XP.rotationDegrees(TILT));
            pose.mulPose(Axis.YP.rotationDegrees(rotation * DEGREES_PER_FRAME + BASE_SPIN));
            pose.translate(-centreX, -centreY, -centreZ);
            Lighting.setupFor3DItems();

            for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState state = entry.getValue();
                pose.pushPose();
                pose.translate(pos.getX(), pos.getY(), pos.getZ());
                // The infusion matrix has no block model of its own; its block entity draws it.
                if (state.is(TTBlocks.INFUSION_MATRIX.get())) {
                    Minecraft.getInstance().getBlockEntityRenderDispatcher()
                            .renderItem(MATRIX, pose, buffers, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                } else {
                    dispatcher.renderSingleBlock(state, pose, buffers, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                }
                pose.popPose();
            }

            buffers.endBatch();
            pose.popPose();
        }

        private static int[] boundsOf(Map<BlockPos, BlockState> blocks) {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;

            for (BlockPos pos : blocks.keySet()) {
                minX = Math.min(minX, pos.getX());
                minY = Math.min(minY, pos.getY());
                minZ = Math.min(minZ, pos.getZ());
                maxX = Math.max(maxX, pos.getX());
                maxY = Math.max(maxY, pos.getY());
                maxZ = Math.max(maxZ, pos.getZ());
            }

            return new int[]{minX, minY, minZ, maxX, maxY, maxZ};
        }
    }
}
