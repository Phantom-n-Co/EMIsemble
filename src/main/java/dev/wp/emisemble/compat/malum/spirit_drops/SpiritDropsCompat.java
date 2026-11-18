package dev.wp.emisemble.compat.malum.spirit_drops;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.client.Minecraft;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Spirit drops: the entries Malum's reload listener holds on an integrated server, or the copy a server running this
 * mod sends on join. A server without the mod leaves the category empty, as JEI's own plugins do.
 */
public final class SpiritDropsCompat {
    /** Own namespace: the page is ours, built from Malum's data, and owes nothing to a JEI-only addon's ids. */
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("emisemble", "spirit_drops");

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("malum", "arcane_spirit"))
                    .map(ItemStack::new).orElse(ItemStack.EMPTY)),
            "emisemble.category.spirit_drops");

    private SpiritDropsCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        int added = 0;
        try {
            List<Map.Entry<ResourceLocation, List<ItemStack>>> entries =
                    new ArrayList<>(SpiritDropsData.drops().entrySet());
            entries.sort(Comparator.comparing(e -> e.getKey().toString()));
            for (Map.Entry<ResourceLocation, List<ItemStack>> entry : entries) {
                if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entry.getKey())) {
                    continue;
                }
                try {
                    registry.addRecipe(new Page(entry.getKey(), entry.getValue()));
                    added++;
                } catch (Throwable t) {
                    Emisemble.LOGGER.warn("Skipping spirit drop page for {}", entry.getKey(), t);
                }
            }
            if (entries.isEmpty()) {
                Emisemble.LOGGER.warn("malum: no spirit drop data; a remote server needs this mod to send it");
            }
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("malum: spirit drop data unavailable", t);
        }
        Emisemble.LOGGER.info("malum: {} spirit drop recipes registered", added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private static final int WIDTH = 132;
        private static final int HEIGHT = 54;
        private static final int COLUMNS = 4;
        private static final int SLOT = 18;
        private static final float CENTER_X = 23.0F;
        private static final float CENTER_Y = 27.0F;
        private static final Map<EntityType<?>, LivingEntity> CACHE = new WeakHashMap<>();

        private final ResourceLocation entityId;
        private final ItemStack egg;

        Page(ResourceLocation entityId, List<ItemStack> drops) {
            super(CATEGORY, ResourceLocation.fromNamespaceAndPath(ID.getNamespace(),
                    "/" + entityId.toLanguageKey()), WIDTH, HEIGHT);
            this.entityId = entityId;
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
            SpawnEggItem spawnEgg = type == null ? null : SpawnEggItem.byId(type);
            this.egg = spawnEgg == null ? ItemStack.EMPTY : new ItemStack(spawnEgg);
            // Not drawn, exactly as the host does it: the egg only makes the page findable from an item search.
            if (!egg.isEmpty()) {
                inputs.add(EmiStack.of(egg));
            }
            List<EmiStack> stacks = drops.stream().map(EmiStack::of).toList();
            inputs.addAll(stacks);
            outputs.addAll(stacks);
        }

        // The drops are both inputs and outputs, and the egg is a lookup key, so there is nothing to root a tree in.
        @Override
        public boolean supportsRecipeTree() {
            return false;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            List<ItemStack> drops = getOutputs().stream().map(EmiStack::getItemStack).toList();
            int cols = Math.min(drops.size(), COLUMNS);
            int rows = Math.max(1, (drops.size() + COLUMNS - 1) / COLUMNS);
            int startX = 88 - cols * SLOT / 2;
            int startY = (HEIGHT - rows * SLOT) / 2;
            // The drop cells are the page's outputs, so they carry the context: a lookup on a spirit opens the page.
            for (int i = 0; i < drops.size(); i++) {
                RecipeSlots.addFramed(widgets, EmiStack.of(drops.get(i)), startX + (i % COLUMNS) * SLOT,
                        startY + (i / COLUMNS) * SLOT).recipeContext(this);
            }
            widgets.addDrawable(3, 2, 40, 50, (GuiGraphics draw, int mouseX, int mouseY, float delta) ->
                    renderEntity(draw, entityId, mouseX, mouseY));
            widgets.addTooltip((mouseX, mouseY) -> {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
                return type == null ? List.of()
                        : List.of(ClientTooltipComponent.create(type.getDescription().getVisualOrderText()));
            }, 3, 2, 40, 50);
        }

/**
 * The host's transform: InventoryScreen's helper, sized by the entity's larger dimension and looking at the
 * cursor.
 */
        private static void renderEntity(GuiGraphics draw, ResourceLocation entityId, float mouseX, float mouseY) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
            Minecraft client = Minecraft.getInstance();
            if (type == null || client.level == null) {
                drawName(draw, type);
                return;
            }
            LivingEntity entity;
            try {
                entity = CACHE.computeIfAbsent(type, t -> t.create(client.level) instanceof LivingEntity living ? living : null);
            } catch (Throwable t) {
                entity = null;
            }
            if (entity == null) {
                drawName(draw, type);
                return;
            }
            float maxDim = Math.max(entity.getBbWidth(), entity.getBbHeight());
            float scale = Mth.clamp(44.0F / Math.max(maxDim, 0.1F), 6.0F, 45.0F);
            float lookX = (float) Math.atan((CENTER_X - mouseX) / 40.0);
            float lookY = (float) Math.atan((CENTER_Y - mouseY) / 40.0);
            Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
            Quaternionf camera = new Quaternionf().rotateX(lookY * 20.0F * (float) (Math.PI / 180.0));
            pose.mul(camera);
            float bodyRot = entity.yBodyRot;
            float yRot = entity.getYRot();
            float xRot = entity.getXRot();
            float headRotO = entity.yHeadRotO;
            float headRot = entity.yHeadRot;
            entity.yBodyRot = 180.0F + lookX * 20.0F;
            entity.setYRot(180.0F + lookX * 40.0F);
            entity.setXRot(-lookY * 20.0F);
            entity.yHeadRot = entity.getYRot();
            entity.yHeadRotO = entity.getYRot();
            Vector3f translate = new Vector3f(0.0F, entity.getBbHeight() / 2.0F, 0.0F);
            InventoryScreen.renderEntityInInventory(draw, CENTER_X, CENTER_Y, scale / entity.getScale(), translate,
                    pose, camera, entity);
            entity.yBodyRot = bodyRot;
            entity.setYRot(yRot);
            entity.setXRot(xRot);
            entity.yHeadRotO = headRotO;
            entity.yHeadRot = headRot;
        }

        private static void drawName(GuiGraphics draw, EntityType<?> type) {
            if (type == null) {
                return;
            }
            Font font = Minecraft.getInstance().font;
            Component name = type.getDescription();
            int width = font.width(name);
            if (width <= 38) {
                draw.drawString(font, name, (int) CENTER_X - width / 2, (int) CENTER_Y - 4, 0xFFFFFF);
                return;
            }
            draw.pose().pushPose();
            float scale = 38.0F / width;
            draw.pose().translate(CENTER_X, CENTER_Y, 0.0F);
            draw.pose().scale(scale, scale, 1.0F);
            draw.drawString(font, name, -width / 2, -4, 0xFFFFFF);
            draw.pose().popPose();
        }
    }
}
