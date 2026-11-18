package dev.wp.emisemble.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;

/** The numbers IE writes on its pages, each inside the host's own translated sentence. */
public final class IeText {
    private static final String SECONDS = Lib.DESC_INFO + "seconds";
    private static final String ENERGY_PER_TICK = Lib.DESC_INFO + "ift";
    private static final String BATCHED = Lib.DESC_INFO + "batched";

    private IeText() {}

    /** {@code ticks} is a float in the host's call site, so a fractional tick keeps its fraction. */
    public static Component seconds(double ticks) {
        return Component.translatable(SECONDS, format(ticks / 20.0, "#.##"));
    }

    /** The fuel page divides ticks by 20 as ints first, so the fraction is already gone by the time it prints. */
    public static Component wholeSeconds(int seconds) {
        return Component.translatable(SECONDS, format(seconds, "#.##"));
    }

    public static Component energyPerTick(double ifPerTick) {
        return Component.translatable(ENERGY_PER_TICK, format(ifPerTick, "#.##"));
    }

    /** The coke oven's "Up to 16 at a time" — the only place its batch size is stated. */
    public static Component batched(int count) {
        return Component.translatable(BATCHED, count).withStyle(ChatFormatting.GOLD);
    }

    public static String format(double value, String pattern) {
        return new DecimalFormat(pattern).format(value);
    }
}
