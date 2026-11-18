package dev.wp.emisemble.compat.immersiveengineering;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

/** Page ids EMI will not mistake for real recipes: a leading slash marks the ones IE fabricates. */
public final class IeIds {
    private IeIds() {}

    /** A page this port invented, carrying the leading slash that marks it synthetic. */
    public static ResourceLocation synthetic(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, "/" + path);
    }

    public static ResourceLocation of(RecipeHolder<?> holder) {
        ResourceLocation id = holder.id();
        Level level = Minecraft.getInstance().level;
        if (level != null && level.getRecipeManager().byKey(id).isEmpty()) {
            return synthetic(id.getNamespace(), id.getPath());
        }
        return id;
    }
}
