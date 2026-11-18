package dev.wp.emisemble;

import dev.wp.emisemble.config.EmisembleConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Emisemble.MOD_ID)
public final class Emisemble {
    public static final String MOD_ID = "emisemble";
    public static final Logger LOGGER = LoggerFactory.getLogger("EMIsemble");

    public Emisemble(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, EmisembleConfig.SPEC);
    }

    /**
     * Gate for every compat entry point: the host must be loaded and the user must not have switched it off.
     */
    public static boolean isAvailable(String modId) {
        return ModList.get().isLoaded(modId) && !EmisembleConfig.disabled().contains(modId);
    }
}
