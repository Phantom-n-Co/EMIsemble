package dev.wp.emisemble.compat.thaumaturge.index;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.content.research.note.NoteGenerator;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.thaumaturge.AspectEmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

/** Aspects and the generated research-note variants, neither of which EMI indexes on its own. */
public final class IndexAdditions {
    private IndexAdditions() {}

    public static void register(EmiRegistry registry) {
        aspects(registry);
        researchNotes(registry);
    }

    private static void aspects(EmiRegistry registry) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            Emisemble.LOGGER.warn("thaumaturge: no client level, skipping the aspect index stacks");
            return;
        }

        Registry<IAspect> aspects = level.registryAccess().registry(IAspect.REGISTRY_KEY).orElse(null);
        if (aspects == null) {
            Emisemble.LOGGER.warn("thaumaturge: no aspect registry on the client, skipping the aspect index stacks");
            return;
        }

        // Both sorts are stable, so this is JEI's two-pass ordering: primals first, id order inside each group.
        List<Holder.Reference<IAspect>> sorted = aspects.holders()
                .sorted(Comparator.comparingInt(holder -> aspects.getId(holder.value())))
                .sorted(Comparator.comparing(holder -> !holder.value().isPrimal()))
                .toList();

        registry.removeEmiStacks(AspectEmiStack::isForeignAspect);

        int added = 0;
        for (Holder.Reference<IAspect> aspect : sorted) {
            registry.addEmiStack(new AspectEmiStack(aspect, 1));
            added++;
        }
        Emisemble.LOGGER.info("thaumaturge: {} aspect ingredients added to the index", added);
    }

    private static void researchNotes(EmiRegistry registry) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            Emisemble.LOGGER.warn("thaumaturge: no client level, skipping the research note variants");
            return;
        }

        RegistryAccess access = level.registryAccess();
        Registry<IResearchEntry> entries = access.registry(IResearchEntry.REGISTRY_KEY).orElse(null);
        if (entries == null) {
            Emisemble.LOGGER.warn("thaumaturge: no research registry on the client, skipping the note variants");
            return;
        }

        int added = 0;
        for (Holder.Reference<IResearchEntry> holder : entries.holders().toList()) {
            ResourceLocation entryId = holder.key().location();
            IResearchEntry entry = holder.value();
            try {
                int theoryRows = ResearchNotes.theoryRowsBefore(entry, entry.stages().size());
                if (theoryRows == 0) {
                    continue;
                }
                AspectList anchors = ResearchNotes.anchors(access, entry);
                int color = NoteGenerator.primaryColor(anchors);
                // One blank and one completed note per theory row: what the research table can actually hold.
                for (int ordinal = 0; ordinal < theoryRows; ordinal++) {
                    registry.addEmiStack(EmiStack.of(displayNote(entryId, ordinal, color, false)));
                    registry.addEmiStack(EmiStack.of(displayNote(entryId, ordinal, color, true)));
                    added += 2;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping the research note variants for {}", entryId, t);
            }
        }
        Emisemble.LOGGER.info("thaumaturge: {} research note variants added to the index", added);
    }

    private static ItemStack displayNote(ResourceLocation entry, int ordinal, int color, boolean complete) {
        ItemStack stack = new ItemStack(TTItems.RESEARCH_NOTE.get());
        stack.set(TTDataComponents.RESEARCH_NOTE.get(),
                new ResearchNoteData(entry, ordinal, color, complete, 0, List.of()));
        if (complete) {
            stack.set(TTDataComponents.NOTE_COMPLETE.get(), true);
        }
        return stack;
    }
}
