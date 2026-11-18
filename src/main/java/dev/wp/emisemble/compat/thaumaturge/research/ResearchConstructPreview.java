package dev.wp.emisemble.compat.thaumaturge.research;

import com.leclowndu93150.thaumaturge.api.research.ResearchConstruct;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/** The slowly turning block model of a construct, fitted into the page and rotated a full turn every 36 seconds. */
final class ResearchConstructPreview {
    private static final int FULL_BRIGHT = 15728880;
    private static final long ROTATION_PERIOD_MS = 36000L;
    private static final BlockEntityInfusionMatrix MATRIX = new BlockEntityInfusionMatrix(
            BlockPos.ZERO, TTBlocks.INFUSION_MATRIX.get().defaultBlockState());

    private ResearchConstructPreview() {}

    /** Cells are laid out in the host's order (y up, z and x descending) and lifted so the top layer is y = 0. */
    static Map<BlockPos, BlockState> blocks(ResearchConstruct construct) {
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        int index = 0;

        for (int y = 0; y < construct.ySize(); y++) {
            for (int z = construct.zSize() - 1; z >= 0; z--) {
                for (int x = construct.xSize() - 1; x >= 0; x--) {
                    if (index >= construct.cells().size()) {
                        return blocks;
                    }
                    ItemStack stack = ResearchConstructEmiRecipe.firstStack(construct.cells().get(index++));
                    Block block = Block.byItem(stack.getItem());
                    if (block != Blocks.AIR) {
                        blocks.put(new BlockPos(x, construct.ySize() - y - 1, z), block.defaultBlockState());
                    }
                }
            }
        }

        return blocks;
    }

    /** Recipe-relative coordinates: add the widget at (0,0), since EMI translates a drawable to its own origin. */
    static void render(GuiGraphics graphics, Map<BlockPos, BlockState> blocks,
                       float centreX, float centreY, float maxWidth, float maxHeight, float maxScale) {
        if (blocks.isEmpty()) {
            return;
        }

        int[] bounds = boundsOf(blocks);
        float structureCentreX = (bounds[0] + bounds[3] + 1) / 2.0F;
        float structureCentreY = (bounds[1] + bounds[4] + 1) / 2.0F;
        float structureCentreZ = (bounds[2] + bounds[5] + 1) / 2.0F;
        float width = bounds[3] - bounds[0] + 1;
        float height = bounds[4] - bounds[1] + 1;
        float depth = bounds[5] - bounds[2] + 1;
        // The host's isometric estimate, used only to fit the model inside the page band.
        float projectedWidth = (width + depth) * 0.71F;
        float projectedHeight = height * 0.91F + (width + depth) * 0.3F;
        float scale = Math.min(maxScale, Math.min(maxWidth / projectedWidth, maxHeight / projectedHeight));
        float rotation = (float) (Util.getMillis() % ROTATION_PERIOD_MS) / 100.0F;

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BufferSource buffers = graphics.bufferSource();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(centreX, centreY, 200.0F);
        pose.scale(scale, -scale, scale);
        pose.mulPose(Axis.XP.rotationDegrees(25.0F));
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        pose.translate(-structureCentreX, -structureCentreY, -structureCentreZ);
        Lighting.setupFor3DItems();

        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            pose.pushPose();
            pose.translate(pos.getX(), pos.getY(), pos.getZ());
            // The infusion matrix has no block model, only a block entity renderer; without it the altar is a hole.
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
