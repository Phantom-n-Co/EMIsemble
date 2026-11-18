package dev.wp.emisemble.porting;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.wp.emisemble.porting.TranslatedCategory;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** A category whose title comes from the host mod's own translation key instead of a string of ours. */
public class TranslatedCategory extends EmiRecipeCategory {
    private final Component title;

    public TranslatedCategory(ResourceLocation id, EmiRenderable icon, String translationKey) {
        this(id, icon, Component.translatable(translationKey));
    }

    public TranslatedCategory(ResourceLocation id, EmiRenderable icon, Component title) {
        super(id, icon);
        this.title = title;
    }

    @Override
    public Component getName() {
        return title;
    }
}
