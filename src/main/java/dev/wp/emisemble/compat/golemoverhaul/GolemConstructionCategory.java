package dev.wp.emisemble.compat.golemoverhaul;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import tech.alexnijjar.golemoverhaul.client.compat.FakeLevel;
import tech.alexnijjar.golemoverhaul.common.recipes.GolemConstructionRecipe;
import tech.alexnijjar.golemoverhaul.common.registry.ModItems;
import tech.alexnijjar.golemoverhaul.common.registry.ModRecipeTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/** {@code golemoverhaul:golem_construction}: a block pattern and the golem it builds, both painted by the host. */
public final class GolemConstructionCategory {
    public static final String NAMESPACE = "golemoverhaul";
    public static final ResourceLocation ID = EmiRecipes.categoryId(ModRecipeTypes.GOLEM_CONSTRUCTION.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(ModItems.CLAY_GOLEM_STATUE.get()), "text.golemoverhaul.golem_construction");

    private GolemConstructionCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        // The host's JEI plugin registers no catalyst for this category, so the port adds no workstation either.
        int added = EmiRecipes.addAll(registry, ModRecipeTypes.GOLEM_CONSTRUCTION.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} golem_construction recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/container/golem_construction.png");
        // A single-purpose 180x76 sprite: the declared texture size has to be the image's, or v stretches the art.
        private static final int SHEET_WIDTH = 180;
        private static final int SHEET_HEIGHT = 76;
        // The art is opaque behind a one-pixel outline, i.e. its own card: the page is the art less EMI's 4px rim.
        private static final int WIDTH = 172;
        private static final int HEIGHT = 68;
        // JEI pins the scene to (47,58) on its 180x76 page; every page coordinate carries the card's 4px shift.
        private static final int SCENE_X = 43;
        private static final int SCENE_Y = 54;
        private static final int ARROW_X = 73;
        private static final int ARROW_Y = 25;
        // The host's two hover rects, which only its own draw() can place: 52x56 flanking the scene anchor.
        private static final int BLOCK_BOX_X = 9;
        private static final int ENTITY_BOX_X = 111;
        private static final int BOX_Y = 6;
        private static final int BOX_WIDTH = 52;
        private static final int BOX_HEIGHT = 56;

        private final Map<BlockPos, BlockState> blocks = new HashMap<>();
        private final FakeLevel fakeLevel;
        private final List<Component> patternLines = new ArrayList<>();
        private final float blockScale;
        private final float entityScale;
        private final boolean single;
        private final LivingEntity entity;
        private final Component entityName;

        private Page(RecipeHolder<GolemConstructionRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            GolemConstructionRecipe recipe = holder.value();
            this.blockScale = recipe.blockScale();
            this.entityScale = recipe.entityScale();

            List<String> pattern = recipe.pattern();
            int width = pattern.getFirst().length();
            int height = pattern.size();
            this.single = width == 1 && height == 1;
            for (int row = 0; row < height; row++) {
                String symbols = pattern.get(row);
                MutableComponent line = Component.empty();
                boolean painted = false;
                for (int column = 0; column < width; column++) {
                    Block block = blockFor(recipe, symbols.charAt(column));
                    if (block == Blocks.AIR) {
                        continue;
                    }
                    // The host mirrors the pattern on both axes, so the golem reads the way it does in world.
                    blocks.put(BlockPos.containing(width - column - 1, height - row - 1, 0.0), block.defaultBlockState());
                    if (painted) {
                        line.append(" ");
                    }
                    line.append(block.getName());
                    painted = true;
                }
                if (painted) {
                    patternLines.add(line);
                }
            }
            this.fakeLevel = new FakeLevel(blocks);

            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(recipe.entity());
            Level level = Minecraft.getInstance().level;
            this.entityName = type == null ? null : type.getDescription();
            Entity created = type == null || level == null ? null : type.create(level);
            this.entity = created instanceof LivingEntity living ? living : null;

            // Every block a key entry names, used or not: JEI indexed the lot, so the page answers any of them.
            Set<Item> keyed = new LinkedHashSet<>();
            recipe.key().values().forEach(entry -> blocksOf(entry)
                    .map(Block::asItem)
                    .filter(item -> item != Items.AIR)
                    .forEach(keyed::add));
            keyed.forEach(item -> inputs.add(EmiStack.of(item)));
            Item result = BuiltInRegistries.ITEM.get(recipe.item());
            if (result != null && result != Items.AIR) {
                outputs.add(EmiStack.of(result));
            }
        }

        // Nothing on this page is a slot, so a recipe tree built from it would have no leaves to attach.
        @Override
        public boolean supportsRecipeTree() {
            return false;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            // The art's rounded corners are transparent, so this needs the alpha blit rather than addTexture.
            AlphaTextures.add(widgets, TEXTURE, SHEET_WIDTH, SHEET_HEIGHT, -4, -4, SHEET_WIDTH, SHEET_HEIGHT, 0, 0);
            widgets.addDrawable(0, 0, WIDTH, HEIGHT, (draw, mouseX, mouseY, delta) -> {
                renderBlocks(draw);
                renderEntity(draw);
            }).tooltip(this::hoverTooltip);
            widgets.addTexture(EmiTexture.EMPTY_ARROW, ARROW_X, ARROW_Y);
        }

        private void renderBlocks(GuiGraphics draw) {
            PoseStack pose = draw.pose();
            pose.pushPose();
            pose.translate(SCENE_X, SCENE_Y, 100.0F);
            pose.translate(10.0F, -5.0F, 0.0F);
            pose.scale(-20.0F, -20.0F, -20.0F);
            pose.scale(blockScale, blockScale, blockScale);
            if (single) {
                pose.translate(0.1F, 0.2F, 0.0F);
            }
            pose.translate(0.5F, 0.5F, 0.5F);
            pose.mulPose(Axis.XP.rotationDegrees(-30.0F));
            pose.mulPose(Axis.YP.rotationDegrees(45.0F));
            pose.translate(-0.5F, -0.5F, -0.5F);
            fakeLevel.renderBlocks(pose);
            pose.popPose();
        }

        private void renderEntity(GuiGraphics draw) {
            if (entity == null) {
                return;
            }
            entity.setYHeadRot(0.0F);
            PoseStack pose = draw.pose();
            pose.pushPose();
            pose.translate(95.0F, -7.0F, 0.0F);
            InventoryScreen.renderEntityInInventory(draw, SCENE_X, SCENE_Y, 32.0F * entityScale, new Vector3f(),
                    new Quaternionf().rotationXYZ((float) Math.toRadians(30.0), (float) Math.toRadians(135.0),
                            (float) Math.PI),
                    null, entity);
            pose.popPose();
        }

        // JEI had the screen's tooltip hook; here the page owns it, on the same two rects the host tested.
        private List<ClientTooltipComponent> hoverTooltip(int mouseX, int mouseY) {
            if (mouseY < BOX_Y || mouseY >= BOX_Y + BOX_HEIGHT) {
                return List.of();
            }
            if (entityName != null && mouseX >= ENTITY_BOX_X && mouseX < ENTITY_BOX_X + BOX_WIDTH) {
                return EmiRecipes.tooltip(List.of(entityName));
            }
            if (mouseX >= BLOCK_BOX_X && mouseX < BLOCK_BOX_X + BOX_WIDTH) {
                return EmiRecipes.tooltip(patternLines);
            }
            return List.of();
        }

        private static Block blockFor(GolemConstructionRecipe recipe, char symbol) {
            Either<ResourceKey<Block>, TagKey<Block>> entry = recipe.key().get(String.valueOf(symbol));
            // A tag is painted as its first member in registry order, which is what JEI's preview showed too.
            return entry == null ? Blocks.AIR : blocksOf(entry).findFirst().orElse(Blocks.AIR);
        }

        private static Stream<Block> blocksOf(Either<ResourceKey<Block>, TagKey<Block>> entry) {
            return entry.map(key -> {
                Block block = BuiltInRegistries.BLOCK.get(key);
                return block == null ? Stream.<Block>empty() : Stream.of(block);
            }, tag -> BuiltInRegistries.BLOCK.getTag(tag)
                    .map(holders -> holders.stream().map(Holder::value))
                    .orElse(Stream.empty()));
        }
    }
}
