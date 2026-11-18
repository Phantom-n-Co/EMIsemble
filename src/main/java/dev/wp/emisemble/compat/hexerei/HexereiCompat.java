package dev.wp.emisemble.compat.hexerei;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.widget.Bounds;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.hexerei.add_to_candle.AddToCandleCategory;
import dev.wp.emisemble.compat.hexerei.blood_sigil.BloodSigilCategory;
import dev.wp.emisemble.compat.hexerei.book_of_shadows_dye.BookOfShadowsCategory;
import dev.wp.emisemble.compat.hexerei.bottling.BottlingCategory;
import dev.wp.emisemble.compat.hexerei.cauldron.CauldronTransfer;
import dev.wp.emisemble.compat.hexerei.cauldron.MixingCauldronCategory;
import dev.wp.emisemble.compat.hexerei.crow_flute_dye.CrowFluteCategory;
import dev.wp.emisemble.compat.hexerei.dipper.DipperCategory;
import dev.wp.emisemble.compat.hexerei.drying_rack.DryingRackCategory;
import dev.wp.emisemble.compat.hexerei.fluidmixing.FluidMixingCompat;
import dev.wp.emisemble.compat.hexerei.keychain_apply.KeychainApplyCategory;
import dev.wp.emisemble.compat.hexerei.pestle_and_mortar.PestleAndMortarCategory;
import dev.wp.emisemble.compat.hexerei.plant_picking.PlantPickingCategory;
import dev.wp.emisemble.compat.hexerei.woodcutter.WoodcutterCategory;
import net.joefoxe.hexerei.fluid.ModFluids;
import net.joefoxe.hexerei.screen.BroomScreen;
import net.joefoxe.hexerei.screen.CofferScreen;
import net.joefoxe.hexerei.screen.CrowScreen;
import net.joefoxe.hexerei.screen.MixingCauldronScreen;
import net.joefoxe.hexerei.screen.OwlScreen;

/** Hexerei's fourteen categories, plus the two registrations its plugin makes beside them. */
public final class HexereiCompat {
    private HexereiCompat() {}

    static void register(EmiRegistry registry) {
        subtypePotionFluid(registry);
        exclusionAreas(registry);
        CauldronTransfer.register(registry);

        int added = AddToCandleCategory.register(registry)
                + BookOfShadowsCategory.register(registry)
                + CrowFluteCategory.register(registry)
                + KeychainApplyCategory.register(registry)
                + DryingRackCategory.register(registry)
                + WoodcutterCategory.register(registry)
                + PlantPickingCategory.register(registry)
                + PestleAndMortarCategory.register(registry)
                + BloodSigilCategory.register(registry)
                + BottlingCategory.register(registry)
                + DipperCategory.register(registry)
                + MixingCauldronCategory.register(registry)
                + FluidMixingCompat.register(registry);
        Emisemble.LOGGER.info("hexerei: {} recipes registered across 14 categories", added);
    }

    private static void subtypePotionFluid(EmiRegistry registry) {
        try {
            registry.setDefaultComparison(ModFluids.POTION.get(), Comparison.compareComponents());
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("hexerei: could not subtype the potion fluid; brews will collapse to one stack", t);
        }
    }

    private static void exclusionAreas(EmiRegistry registry) {
        try {
            registry.addExclusionArea(MixingCauldronScreen.class, (screen, add) -> {
                add.accept(new Bounds(screen.getGuiLeft() + 23, screen.getGuiTop(), 142, 97));
                add.accept(new Bounds(screen.getGuiLeft() + 160, screen.getGuiTop() + 32, 49, 48));
                add.accept(new Bounds(screen.getGuiLeft(), screen.getGuiTop() + 97, 188, 30));
            });
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("hexerei: could not add the mixing cauldron screen areas", t);
        }
        try {
            registry.addExclusionArea(CrowScreen.class, (screen, add) -> {
                add.accept(new Bounds(screen.getGuiLeft(), screen.getGuiTop() - 28, 188, 153));
                add.accept(new Bounds(screen.getGuiLeft() + 184 - 28 + (int) screen.whitelistOffset,
                        screen.getGuiTop() + 17 - 28 + 3, 39, 101));
                add.accept(new Bounds(screen.getGuiLeft() - 5 - (int) screen.leftPanelOffset,
                        screen.getGuiTop() + 17 - 28 + 3, 39, 101));
            });
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("hexerei: could not add the crow screen areas", t);
        }
        try {
            registry.addExclusionArea(OwlScreen.class, (screen, add) -> {
                if (!screen.quirkSideBarHidden) {
                    add.accept(new Bounds(screen.getGuiLeft() + 174, screen.getGuiTop() + 37, 26, 26));
                }
            });
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("hexerei: could not add the owl screen areas", t);
        }
        try {
            registry.addExclusionArea(CofferScreen.class, (screen, add) -> {
                add.accept(new Bounds(screen.getGuiLeft(), screen.getGuiTop() - 28, 214, 157));
                if (screen.getToggled()) {
                    add.accept(new Bounds(screen.getGuiLeft() + 211, screen.getGuiTop() - 28 + 62, 62, 62));
                }
            });
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("hexerei: could not add the coffer screen areas", t);
        }
        try {
            registry.addExclusionArea(BroomScreen.class, (screen, add) -> {
                add.accept(new Bounds(screen.getGuiLeft(), screen.getGuiTop() - 34, 214, 82 + screen.offset));
                add.accept(new Bounds(screen.getGuiLeft(), screen.getGuiTop() + 79 + screen.offset - 34, 214, 34));
                add.accept(new Bounds(screen.getGuiLeft() + 184,
                        screen.getGuiTop() + 55 + screen.offset + (int) screen.dropdownOffset - 34, 26, 58));
            });
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("hexerei: could not add the broom screen areas", t);
        }
    }
}
