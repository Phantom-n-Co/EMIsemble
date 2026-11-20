package dev.wp.emisemble;

import dev.wp.emisemble.compat.malum.spirit_drops.SpiritDropsData;
import dev.wp.emisemble.compat.malum.spirit_drops.SpiritDropsSync;
import dev.wp.emisemble.compat.mythsandlegends.research.ResearchSync;
import dev.wp.emisemble.config.EmisembleConfig;
import dev.wp.emisemble.network.ResearchPayload;
import dev.wp.emisemble.network.SpiritDropsPayload;
import dev.wp.emisemble.porting.ClientTicks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Emisemble.MOD_ID)
public final class Emisemble {
    public static final String MOD_ID = "emisemble";
    public static final Logger LOGGER = LoggerFactory.getLogger("EMIsemble");

    public Emisemble(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, EmisembleConfig.SPEC);
        bus.addListener(Emisemble::registerPayloads);
        NeoForge.EVENT_BUS.addListener(SpiritDropsSync::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(ResearchSync::onDatapackSync);
        if (FMLEnvironment.dist.isClient()) {
            ClientTicks.install();
        }
    }

    /** Optional: a server may run this mod while its clients do not, and the payloads are server to client only. */
    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(SpiritDropsPayload.TYPE, SpiritDropsPayload.STREAM_CODEC,
                (payload, context) -> SpiritDropsData.accept(payload));
        registrar.playToClient(ResearchPayload.TYPE, ResearchPayload.STREAM_CODEC,
                (payload, context) -> ResearchSync.accept(payload));
    }

    /**
     * Gate for every compat entry point: the host must be loaded and the user must not have switched it off.
     */
    public static boolean isAvailable(String modId) {
        return ModList.get().isLoaded(modId) && !EmisembleConfig.disabled().contains(modId);
    }
}
