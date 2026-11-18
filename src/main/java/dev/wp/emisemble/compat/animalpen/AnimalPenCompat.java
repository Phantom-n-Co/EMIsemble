package dev.wp.emisemble.compat.animalpen;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.InfoText;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;

/** Animal Pens' three capture categories plus the usage blurbs its JEI plugin adds to the same three items. */
public final class AnimalPenCompat {
    private static final String NAMESPACE = "animal_pen";

    private AnimalPenCompat() {}

    public static void register(EmiRegistry registry) {
        Item cage = AnimalPensItemRegistry.ANIMAL_CAGE.get();
        Item catcher = AnimalPensItemRegistry.BIRD_CATCHER.get();
        Item container = AnimalPensItemRegistry.ANIMAL_CONTAINER.get();

        int pages = ItemInfoCategory.register(registry, ItemInfoCategory.ANIMAL_CAGE, cage,
                AnimalPenTags.ANIMAL_CAGE_PICKABLE, "jei.animal_pen.animal_cage");
        pages += ItemInfoCategory.register(registry, ItemInfoCategory.BIRD_CAGE, catcher,
                AnimalPenTags.BIRD_CATCHER_PICKABLE, "jei.animal_pen.bird_catcher");
        pages += ItemInfoCategory.register(registry, ItemInfoCategory.WATER_CONTAINER, container,
                AnimalPenTags.WATER_MOB_CONTAINER_PICKABLE, "jei.animal_pen.water_animal_container");
        Emisemble.LOGGER.info("{}: {} item info recipes registered", NAMESPACE, pages);

        // Each blurb names its own block, so none of the three merges with another.
        info(registry, cage, "animal_cage", "jei.animal_pen.animal_cage.info");
        info(registry, catcher, "bird_catcher", "jei.animal_pen.bird_catcher.info");
        info(registry, container, "water_animal_container", "jei.animal_pen.water_animal_container.info");
    }

    private static void info(EmiRegistry registry, Item item, String name, String key) {
        EmiRecipes.add(registry, new EmiInfoRecipe(List.of(EmiStack.of(item)),
                InfoText.lines(Component.translatable(key)),
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "/info/" + name)));
    }
}
