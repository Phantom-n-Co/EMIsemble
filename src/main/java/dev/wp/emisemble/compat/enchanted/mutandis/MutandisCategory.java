package dev.wp.emisemble.compat.enchanted.mutandis;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.favouriteless.enchanted.common.blocks.crops.CropBlockAgeFive;
import net.favouriteless.enchanted.common.enchanted.mutandis.MutagenInfo;
import net.favouriteless.enchanted.common.init.EData;
import net.favouriteless.enchanted.common.init.EItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;

/** {@code enchanted:mutandis}: the mutagen registry as the guide book draws it. */
public final class MutandisCategory {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("enchanted", "mutandis");
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(EItems.MUTANDIS.get()), "jei.category.enchanted.mutandis");

    private static final int WIDTH = 90;
    private static final int HEIGHT = 122;
    private static final int REGION_Y = 12;
    private static final int REGION_HEIGHT = 88;
    private static final int CENTRE_X = WIDTH / 2;
    private static final int CENTRE_Y = REGION_Y + REGION_HEIGHT / 2;
    private static final int RING_RADIUS = 23;
    private static final float MUTEE_SCALE = 16;
    private static final float MUTAGEN_SCALE = 10;
    private static final int EXTREMIS_Y = 2;
    private static final int WEIGHT_Y = REGION_Y + REGION_HEIGHT + 2;
    private static final int LIGHT = 0xF000F0;
    private static final int TEXT = 0xFF404040;
    private static final int EXTREMIS_COLOR = 0xFF732D2D;

    private MutandisCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(EItems.MUTANDIS.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(EItems.MUTANDIS_EXTREMIS.get()));

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            Emisemble.LOGGER.warn("enchanted: no client level, skipping the mutandis pages");
            return 0;
        }
        Registry<MutagenInfo> mutagens = level.registryAccess().registryOrThrow(EData.MUTAGEN_REGISTRY);

        int added = 0;
        int noItem = 0;
        for (var entry : mutagens.entrySet()) {
            ResourceLocation mutee = entry.getKey().location();
            Block input = BuiltInRegistries.BLOCK.get(mutee);
            List<MutagenInfo.MutagenSet> sets = entry.getValue().sets();
            for (int i = 0; i < sets.size(); i++) {
                MutagenInfo.MutagenSet set = sets.get(i);
                try {
                    MutandisEmiRecipe page = MutandisEmiRecipe.of(mutee, i, input, set);
                    if (page == null) {
                        noItem++;
                        continue;
                    }
                    registry.addRecipe(page);
                    added++;
                } catch (Throwable t) {
                    Emisemble.LOGGER.warn("enchanted: skipping the mutandis page {} #{}", mutee, i, t);
                }
            }
        }

        Emisemble.LOGGER.info("enchanted: {} mutandis pages registered, {} skipped for having no item", added, noItem);
        return added;
    }

    private static final class MutandisEmiRecipe extends BasicEmiRecipe {
        private final BlockState mutee;
        private final List<BlockState> mutagens;
        private final EmiStack output;
        private final boolean extremis;
        private final int weight;

        private MutandisEmiRecipe(ResourceLocation id, Block input, List<Block> mutagens, Block result, EmiStack tool,
                                  boolean extremis, int weight) {
            super(CATEGORY, id, WIDTH, HEIGHT);
            this.mutee = displayState(input);
            this.mutagens = new ArrayList<>(mutagens.size());
            for (Block mutagen : mutagens) {
                this.mutagens.add(displayState(mutagen));
            }
            this.output = stackOf(result);
            this.extremis = extremis;
            this.weight = weight;

            inputs.add(stackOf(input));
            inputs.add(tool);
            for (Block mutagen : mutagens) {
                catalysts.add(stackOf(mutagen));
            }
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        private static MutandisEmiRecipe of(ResourceLocation muteeId, int index, Block input, MutagenInfo.MutagenSet set) {
            EmiStack result = stackOf(set.result());
            if (result.isEmpty()) {
                return null;
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("enchanted",
                    "/mutandis/" + muteeId.getNamespace() + "/" + muteeId.getPath() + "/" + index);
            EmiStack tool = EmiStack.of(set.extremis() ? EItems.MUTANDIS_EXTREMIS.get() : EItems.MUTANDIS.get());
            return new MutandisEmiRecipe(id, input, set.mutagens(), set.result(), tool, set.extremis(), set.weight());
        }

        private static EmiStack stackOf(Block block) {
            return block.asItem() == Items.AIR ? EmiStack.EMPTY : EmiStack.of(new ItemStack(block));
        }

        // Crops grown, lamps lit, vines berried: a default state reads as a sapling.
        private static BlockState displayState(Block block) {
            return block.defaultBlockState()
                    .trySetValue(CropBlock.AGE, CropBlock.MAX_AGE)
                    .trySetValue(CropBlockAgeFive.AGE_FIVE, 4)
                    .trySetValue(BlockStateProperties.LIT, true)
                    .trySetValue(BlockStateProperties.BERRIES, true);
        }

        @Override
        public boolean supportsRecipeTree() {
            return false;
        }

        @Override
        public boolean hideCraftable() {
            return true;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            if (extremis) {
                widgets.addText(Component.translatable("tooltip.enchanted.mutagen_extremis")
                                .withStyle(ChatFormatting.BOLD), CENTRE_X, EXTREMIS_Y, EXTREMIS_COLOR, false)
                        .horizontalAlign(TextWidget.Alignment.CENTER);
            }
            widgets.addDrawable(0, REGION_Y, WIDTH, REGION_HEIGHT, this::renderBlocks);
            widgets.addTooltip(this::tooltipLines, 0, REGION_Y, WIDTH, REGION_HEIGHT);

            widgets.addText(Component.translatable("tooltip.enchanted.mutagen_weight")
                            .withStyle(ChatFormatting.BOLD), CENTRE_X, WEIGHT_Y, TEXT, false)
                    .horizontalAlign(TextWidget.Alignment.CENTER);
            widgets.addText(Component.literal(String.valueOf(weight)), CENTRE_X,
                            WEIGHT_Y + Minecraft.getInstance().font.lineHeight, TEXT, false)
                    .horizontalAlign(TextWidget.Alignment.CENTER);
        }

        private void renderBlocks(GuiGraphics draw, int mouseX, int mouseY, float delta) {
            float ticks = ticks() + delta;
            block(draw, mutee, CENTRE_X, CENTRE_Y, MUTEE_SCALE, ticks / 2.0F);

            int count = mutagens.size();
            if (count == 0) {
                return;
            }
            float step = 360.0F / count * Mth.DEG_TO_RAD;
            float sin = Mth.sin(step);
            float cos = Mth.cos(step);
            float start = ticks / 8.0F * -Mth.DEG_TO_RAD;
            float x = RING_RADIUS * Mth.sin(start);
            float y = RING_RADIUS * Mth.cos(start);
            for (BlockState mutagen : mutagens) {
                block(draw, mutagen, CENTRE_X + x, CENTRE_Y + y, MUTAGEN_SCALE, ticks / 2.0F);
                float nextX = x * cos - y * sin;
                y = x * sin + y * cos;
                x = nextX;
            }
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
        }

        private static void block(GuiGraphics draw, BlockState state, float x, float y, float scale, float spin) {
            PoseStack pose = draw.pose();
            pose.pushPose();
            pose.translate(x, y, 100);
            pose.translate(0.5F * scale, 0.5F * scale, 0.5F * scale);
            pose.scale(-scale, -scale, -scale);
            pose.translate(0.5F, 0.5F, 0.5F);
            pose.mulPose(Axis.XN.rotationDegrees(30));
            pose.mulPose(Axis.YP.rotationDegrees(spin));
            pose.translate(-0.5F, -0.5F, -0.5F);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, pose,
                    Minecraft.getInstance().renderBuffers().bufferSource(), LIGHT, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }

        private List<ClientTooltipComponent> tooltipLines(int mouseX, int mouseY) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("tooltip.enchanted.mutee"));
            lines.add(name(mutee).copy().withStyle(ChatFormatting.GREEN));
            lines.add(Component.literal(""));
            lines.add(Component.translatable("tooltip.enchanted.mutagens"));
            for (BlockState mutagen : mutagens) {
                lines.add(name(mutagen).copy().withStyle(ChatFormatting.GREEN));
            }
            return lines.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
        }

        private static Component name(BlockState state) {
            return state.getBlock().getName();
        }

        private static long ticks() {
            ClientLevel level = Minecraft.getInstance().level;
            return level == null ? 0L : level.getGameTime();
        }
    }
}
