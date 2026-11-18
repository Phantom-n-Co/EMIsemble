package dev.wp.emisemble.porting;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Comparator;

/**
 * A fluid stack's identity with one component blanked out. Collapsing a host's generated recipe family into one
 * cycling page needs a key that answers "do these two recipes differ in nothing but the thing the page cycles?".
 */
public final class FluidShapes {
    private FluidShapes() {}

    /** The fluid, its amount and every component but {@code variant}, as a comparable key. */
    public static String key(FluidStack stack, DataComponentType<?> variant) {
        StringBuilder key = new StringBuilder(BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString())
                .append('@').append(stack.getAmount());
        stack.getComponentsPatch().entrySet().stream()
                .sorted(Comparator.comparing(entry -> name(entry.getKey())))
                .forEach(entry -> key.append('|')
                        .append(name(entry.getKey()))
                        .append('=')
                        .append(entry.getKey() == variant ? "~" : String.valueOf(entry.getValue())));
        return key.toString();
    }

    /** Whether the stack carries {@code variant}, i.e. whether a page has anything to cycle. */
    public static boolean has(FluidStack stack, DataComponentType<?> variant) {
        for (var entry : stack.getComponentsPatch().entrySet()) {
            if (entry.getKey() == variant) {
                return entry.getValue().isPresent();
            }
        }
        return false;
    }

    private static String name(DataComponentType<?> type) {
        return String.valueOf(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type));
    }
}
