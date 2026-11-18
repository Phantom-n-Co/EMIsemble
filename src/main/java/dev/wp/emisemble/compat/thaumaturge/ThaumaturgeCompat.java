package dev.wp.emisemble.compat.thaumaturge;

import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.thaumaturge.aspectpages.AspectPages;
import dev.wp.emisemble.compat.thaumaturge.crucible.CrucibleCompat;
import dev.wp.emisemble.compat.thaumaturge.dust.DustTriggerCompat;
import dev.wp.emisemble.compat.thaumaturge.index.IndexAdditions;
import dev.wp.emisemble.compat.thaumaturge.infernal.InfernalFurnaceCompat;
import dev.wp.emisemble.compat.thaumaturge.info.InfoPages;
import dev.wp.emisemble.compat.thaumaturge.infusion.InfusionCompat;
import dev.wp.emisemble.compat.thaumaturge.research.ResearchConstructCategory;
import dev.wp.emisemble.compat.thaumaturge.screens.ScreenProviders;
import dev.wp.emisemble.compat.thaumaturge.subtypes.SubtypeComparisons;
import dev.wp.emisemble.compat.thaumaturge.vanilla.SalisMundusPage;
import dev.wp.emisemble.compat.thaumaturge.workbench.ArcaneWorkbenchCompat;
import dev.wp.emisemble.porting.AlphaTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/** {@code thaumaturge}: every category the host ships, plus the page art and research tooltip they share. */
public final class ThaumaturgeCompat {
    static final ResourceLocation PAGE =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "textures/gui/gui_researchbook_overlay.png");
    static final int PAGE_SHEET = 512;

    private ThaumaturgeCompat() {}

    public static void register(EmiRegistry registry) {
        int arcane = ArcaneWorkbenchCompat.register(registry);
        int crucible = CrucibleCompat.register(registry);

        SubtypeComparisons.register(registry);
        InfoPages.register(registry);
        SalisMundusPage.register(registry);
        ScreenProviders.register(registry);

        InfusionCompat.register(registry);
        DustTriggerCompat.register(registry);
        AspectPages.register(registry);
        InfernalFurnaceCompat.register(registry);
        int constructs = ResearchConstructCategory.register(registry);

        // Last: the aspect pages walk EMI's index, so the aspect stacks have to be in it first.
        IndexAdditions.register(registry);

        Emisemble.LOGGER.info("thaumaturge: {} arcane, {} crucible and {} construct recipes registered",
                arcane, crucible, constructs);
    }

    /** One page piece: {@code gui_researchbook_overlay.png} is a 512x512 sheet shared by every category here. */
    public static void art(WidgetHolder widgets, int x, int y, int width, int height, int u, int v) {
        AlphaTextures.add(widgets, PAGE, PAGE_SHEET, x, y, width, height, u, v);
    }

    /** Rebuilt, not reused: the host's helper sits in its JEI compat package, absent on an install without JEI. */
    public static List<Component> missingResearch(ResearchGate gate) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("jei.thaumaturge.research.missing_research").withStyle(ChatFormatting.GOLD));

        RegistryAccess access = Minecraft.getInstance().level == null
                ? null
                : Minecraft.getInstance().level.registryAccess();
        IResearchEntry entry = access == null
                ? null
                : access.registryOrThrow(IResearchEntry.REGISTRY_KEY)
                        .getHolder(gate.entry())
                        .map(holder -> holder.value())
                        .orElse(null);

        lines.add(Component.literal("- ")
                .append(entry != null ? Component.translatable(entry.nameKey()) : Component.literal(gate.entry().toString()))
                .withStyle(ChatFormatting.RED));
        return lines;
    }
}
