package dev.wp.emisemble.compat.thaumaturge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
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

    /** True for another provider's stack for an aspect: EMI's JEI bridge wraps the host's aspect ingredient itself. */
    public static boolean isForeignAspect(EmiStack stack) {
        if (stack instanceof AspectEmiStack) {
            return false;
        }
        try {
            return stack.getKey() instanceof Holder<?> holder
                    && holder.unwrapKey().map(key -> key.isFor(IAspect.REGISTRY_KEY)).orElse(false);
        } catch (Throwable t) {
            return false;
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
        List<Component> lines = getTooltipText();
        List<ClientTooltipComponent> tooltip = new ArrayList<>(lines.size() + 2);
        for (Component line : lines) {
            tooltip.add(ClientTooltipComponent.create(line.getVisualOrderText()));
        }
        EmiTooltipComponents.appendModName(tooltip, getId().getNamespace());
        tooltip.addAll(super.getTooltip());
        return tooltip;
    }

    @Override
    public Component getName() {
        return AspectComponents.name(aspect);
    }

    /** Favorites are saved as JSON and read back through this, so a stack type without one cannot be favorited. */
    public static final class Serializer implements EmiIngredientSerializer<AspectEmiStack> {
        private static final String TYPE = "emisemble:aspect";

        @Override
        public String getType() {
            return TYPE;
        }

        @Override
        public JsonElement serialize(AspectEmiStack stack) {
            JsonObject json = new JsonObject();
            json.addProperty("type", TYPE);
            json.addProperty("aspect", stack.getId().toString());
            json.addProperty("amount", stack.getAmount());
            return json;
        }

        // Saved favorites load during an EMI reload, which only runs with a level, so the registry is reachable.
        @Override
        public EmiIngredient deserialize(JsonElement element) {
            if (!element.isJsonObject()) {
                return EmiStack.EMPTY;
            }
            JsonObject json = element.getAsJsonObject();
            ResourceLocation id = json.has("aspect") ? ResourceLocation.tryParse(json.get("aspect").getAsString()) : null;
            ClientLevel level = Minecraft.getInstance().level;
            if (id == null || level == null) {
                return EmiStack.EMPTY;
            }
            Registry<IAspect> aspects = level.registryAccess().registry(IAspect.REGISTRY_KEY).orElse(null);
            Holder.Reference<IAspect> holder = aspects == null ? null : aspects.getHolder(id).orElse(null);
            if (holder == null) {
                return EmiStack.EMPTY;
            }
            return new AspectEmiStack(holder, json.has("amount") ? json.get("amount").getAsLong() : 1);
        }
    }
}
