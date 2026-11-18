package dev.wp.emisemble.compat.hexerei.keychain_apply;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.joefoxe.hexerei.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/** One keychain page; the attached item is a random draw, as in the host's own JEI page. */
public final class KeychainApplyEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
    public static final int WIDTH = 136;
    public static final int HEIGHT = 78;

    private static final int INPUT_X = 11;
    private static final int INPUT_Y = 15;
    private static final int PREVIEW_X = 29;
    private static final int PREVIEW_Y = 15;
    private static final int OUTPUT_X = 105;
    private static final int OUTPUT_Y = 33;

    private static final int TITLE_X = 7;
    private static final int TITLE_Y = 5;
    private static final int TITLE_MAX_WIDTH = 131;
    private static final int TITLE_COLOR = 0xFF404040;
    private static final float LINE_HEIGHT = 4.5F;

    private static final int PREVIEW_HIGHLIGHT = 0x66FFFFFF;
    private static final int TOOLTIP_COLOR = 0xFF999999;
    private static final int TOOLTIP_ITEM_COLOR = 0xFFCC5522;

    private static final RandomSource RANDOM = RandomSource.create();

    private final EmiStack keychain;
    private final ItemStack shown;
    private final EmiStack output;
    private final Component title;

    public KeychainApplyEmiRecipe(EmiRecipeCategory category, ResourceLocation id) {
        super(category, id, WIDTH, HEIGHT);

        this.keychain = EmiStack.of(new ItemStack(ModItems.BROOM_KEYCHAIN.get()));
        inputs.add(keychain);

        this.shown = pickShownItem();
        this.output = EmiStack.of(attach(shown));
        outputs.add(output);

        this.title = category.getName();
    }

    private static ItemStack pickShownItem() {
        for (int attempt = 0; attempt < 16; attempt++) {
            Optional<Holder.Reference<Item>> pick = BuiltInRegistries.ITEM.getRandom(RANDOM);
            if (pick.isEmpty()) {
                break;
            }
            ItemStack stack = new ItemStack(pick.get().value());
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return new ItemStack(ModItems.BROOM_KEYCHAIN.get());
    }

    private static ItemStack attach(ItemStack item) {
        ItemStack stack = new ItemStack(ModItems.BROOM_KEYCHAIN.get());
        Level level = Minecraft.getInstance().level;
        if (level == null || item.isEmpty()) {
            return stack;
        }
        try {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            ListTag items = new ListTag();
            items.add(item.save(level.registryAccess()));
            tag.put("Items", items);
            // The component must be set, not just the tag written: the keychain draws its stored item from it.
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("Could not attach {} to the keychain preview", item, t);
        }
        return stack;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, KeychainApplyCategory.TEXTURE, 256, -4, -4, WIDTH + 8, HEIGHT + 8, 0, 0);

        RecipeSlots.add(widgets, keychain, INPUT_X, INPUT_Y);

        widgets.addDrawable(PREVIEW_X, PREVIEW_Y, 16, 16, (draw, mouseX, mouseY, delta) -> {
            if (mouseX >= PREVIEW_X && mouseX < PREVIEW_X + 16 && mouseY >= PREVIEW_Y && mouseY < PREVIEW_Y + 16) {
                draw.fill(0, 0, 16, 16, PREVIEW_HIGHLIGHT);
            }
            draw.renderFakeItem(shown, 0, 0);
        });

        RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);

        widgets.addTooltipText(List.of(Component.translatable("Any Item"), shownLine()),
                PREVIEW_X, PREVIEW_Y, 16, 16);

        addTitle(widgets);
    }

    private Component shownLine() {
        Component name = shown.getHoverName().copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TOOLTIP_ITEM_COLOR)));
        return Component.translatable("item shown: - %s", name)
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TOOLTIP_COLOR)));
    }

    private void addTitle(WidgetHolder widgets) {
        widgets.addDrawable(-4, -4, WIDTH, 14, (draw, mouseX, mouseY, delta) -> {
            Font font = Minecraft.getInstance().font;
            FormattedCharSequence text = title.getVisualOrderText();
            int width = font.width(text);
            if (width <= TITLE_MAX_WIDTH) {
                draw.drawString(font, text, TITLE_X, TITLE_Y, TITLE_COLOR, false);
                return;
            }
            float percent = width / (float) TITLE_MAX_WIDTH;
            draw.pose().pushPose();
            draw.pose().scale(1.0F / percent, 1.0F / percent, 1.0F);
            draw.drawString(font, text, TITLE_X * percent, (TITLE_Y + LINE_HEIGHT) * percent - LINE_HEIGHT,
                    TITLE_COLOR, false);
            draw.pose().popPose();
        });
    }
}
