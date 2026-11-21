package dev.wp.emisemble.porting;

import dev.wp.emisemble.Emisemble;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

/** Runs work a few client ticks later, for a fill that has to wait for a menu to rebuild its slots. */
public final class ClientTicks {
    private static final List<Pending> PENDING = new ArrayList<>();

    private ClientTicks() {}

    public static void install() {
        NeoForge.EVENT_BUS.addListener(ClientTicks::tick);
    }

    public static void after(int ticks, Runnable action) {
        PENDING.add(new Pending(Math.max(1, ticks), action));
    }

    private static void tick(ClientTickEvent.Post event) {
        if (PENDING.isEmpty() || Minecraft.getInstance().level == null) {
            return;
        }
        for (int i = PENDING.size() - 1; i >= 0; i--) {
            Pending pending = PENDING.get(i);
            if (--pending.remaining > 0) {
                continue;
            }
            PENDING.remove(i);
            try {
                pending.action.run();
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Deferred client action failed", t);
            }
        }
    }

    private static final class Pending {
        private int remaining;
        private final Runnable action;

        private Pending(int remaining, Runnable action) {
            this.remaining = remaining;
            this.action = action;
        }
    }
}
