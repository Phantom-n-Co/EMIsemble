package dev.wp.emisemble.compat.thaumaturge;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Aspects are neither items nor fluids, so EMI needs a stack type of its own to index and draw them. */
public final class AspectEmiStack extends EmiStack {
    private final Holder<IAspect> aspect;

    public AspectEmiStack(Holder<IAspect> aspect, long amount) {
        this.aspect = aspect;
        this.amount = amount;
    }

    @Override
    public EmiStack copy() {
        EmiStack copy = new AspectEmiStack(aspect, amount);
        copy.setChance(chance);
        copy.setRemainder(getRemainder().copy());
        copy.comparison(comparison);
        return copy;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public DataComponentPatch getComponentChanges() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    public Object getKey() {
        return aspect;
    }

    @Override
    public ResourceLocation getId() {
        return aspect.unwrapKey().map(key -> key.location())
                .orElseGet(() -> ResourceLocation.fromNamespaceAndPath("thaumaturge", "unknown_aspect"));
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        if ((flags & RENDER_ICON) == 0) {
            return;
        }
        // Must stay masked, or the pages leak aspects the player has not researched.
        if (!AspectKnowledgeAccess.isKnown(aspect)) {
            AspectTagRenderer.renderUnknownChip(draw, x, y, aspect);
            return;
        }
        if (amount > 1 && (flags & RENDER_AMOUNT) != 0) {
            AspectTagRenderer.render(draw, Minecraft.getInstance().font, x, y, aspect, (int) amount);
        } else {
            AspectTagRenderer.render(draw, x, y, aspect);
        }
    }

    @Override
    public List<Component> getTooltipText() {
        int colour = aspect.value().color() | 0xFF000000;
        if (!AspectKnowledgeAccess.isKnown(aspect)) {
            return List.of(Component.translatable("tooltip.thaumaturge.aspect.unknown")
                    .withStyle(Style.EMPTY.withColor(colour)));
        }
        return List.of(
                AspectComponents.name(aspect).withStyle(style -> style.withColor(colour)),
                AspectComponents.description(aspect).withStyle(ChatFormatting.GRAY));
    }

    // Only getTooltip reaches a hovered slot; getTooltipText feeds search and the query tooltip.
    @Override
    public List<ClientTooltipComponent> getTooltip() {
        List<ClientTooltipComponent> tooltip = super.getTooltip();
        for (Component line : getTooltipText()) {
            tooltip.add(ClientTooltipComponent.create(line.getVisualOrderText()));
        }
        return tooltip;
    }

    @Override
    public Component getName() {
        return AspectComponents.name(aspect);
    }
}
