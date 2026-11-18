package dev.wp.emisemble.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;

public final class EmisembleConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_MODS = BUILDER
            .comment("Host mod ids whose native EMI categories stay off. Every other compat is active as soon as its host mod is installed.")
            .defineListAllowEmpty("disabledMods", List.of(), () -> "", o -> o instanceof String);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private EmisembleConfig() {}

    public static Set<String> disabled() {
        return SPEC.isLoaded() ? Set.copyOf(DISABLED_MODS.get()) : Set.of();
    }
}
