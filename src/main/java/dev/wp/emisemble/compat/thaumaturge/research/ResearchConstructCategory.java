package dev.wp.emisemble.compat.thaumaturge.research;

import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchConstruct;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** {@code thaumaturge:research_construct}: one page per research structure, rebuilt from the research registry. */
public final class ResearchConstructCategory {
    // Hardcoded, not read off a registry: the host creates this recipe type inside its JEI plugin.
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("thaumaturge", "research_construct");
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID, EmiStack.of(TTItems.THAUMONOMICON.get()),
            "gui.thaumaturge.recipe_type.construct");

    private ResearchConstructCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);

        List<ResearchConstructEmiRecipe> constructs = new ArrayList<>();
        try {
            collect(constructs);
        } catch (Throwable t) {
            // A throw escaping into EMI costs this plugin's whole registration set, so the walk is guarded as a unit.
            Emisemble.LOGGER.error("Could not read thaumaturge research constructs", t);
        }

        int added = 0;
        for (ResearchConstructEmiRecipe construct : constructs) {
            try {
                registry.addRecipe(construct);
                added++;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping research construct {}", construct.getId(), t);
            }
        }

        Emisemble.LOGGER.info("thaumaturge: {} research construct recipes registered", added);
        return added;
    }

    // Registration runs after a level loads, so the synced datapack entries are in the client's registry access.
    private static void collect(List<ResearchConstructEmiRecipe> constructs) {
        RegistryAccess access = registryAccess();
        if (access == null) {
            Emisemble.LOGGER.debug("thaumaturge: client level has no registry access, no research constructs registered");
            return;
        }

        for (Holder.Reference<IResearchEntry> holder : access.lookupOrThrow(IResearchEntry.REGISTRY_KEY).listElements().toList()) {
            try {
                collectEntry(holder.key().location(), holder.value(), constructs);
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping research construct of {}", holder.key().location(), t);
            }
        }
    }

    private static void collectEntry(ResourceLocation entryId, IResearchEntry entry, List<ResearchConstructEmiRecipe> constructs) {
        List<IResearchStage> stages = entry.stages();
        for (int stageIndex = 0; stageIndex < stages.size(); stageIndex++) {
            Optional<ResearchConstruct> construct = stages.get(stageIndex).construct();
            if (construct.isEmpty()) {
                continue;
            }
            // One page per (entry, structure): a later stage repeating a construct would only duplicate the page.
            boolean duplicate = constructs.stream().anyMatch(existing ->
                    existing.entryId().equals(entryId) && existing.construct().equals(construct.get()));
            if (!duplicate) {
                constructs.add(new ResearchConstructEmiRecipe(entryId, stageIndex, entry, construct.get()));
            }
        }
    }

    private static RegistryAccess registryAccess() {
        return Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.registryAccess();
    }
}
