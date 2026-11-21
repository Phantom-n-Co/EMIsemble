package dev.wp.emisemble.compat.mythsandlegends.research;

import com.ovinter.mythsandlegends.api.research.EnchantedBookReward;
import com.ovinter.mythsandlegends.api.research.IngredientDefinition;
import com.ovinter.mythsandlegends.api.research.ItemReward;
import com.ovinter.mythsandlegends.api.research.PotionReward;
import com.ovinter.mythsandlegends.api.research.RecipeReward;
import com.ovinter.mythsandlegends.api.research.ResearchDefinition;
import com.ovinter.mythsandlegends.api.research.ResearchManager;
import com.ovinter.mythsandlegends.api.research.Reward;
import com.ovinter.mythsandlegends.item.custom.ResearchNotesItem;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.network.Payloads;
import dev.wp.emisemble.network.ResearchPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Myths &amp; Legends builds its research table in a server-side reload listener, so an integrated server has it and a
 * remote one has to send it. The page data is pre-built here because the host's definition has no codec.
 */
public final class ResearchSync {
    private static final Map<ResourceLocation, ResearchPayload.Entry> SYNCED = new ConcurrentHashMap<>();

    private ResearchSync() {}

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        // Checked before any host type is touched, so a server without Myths & Legends never loads one.
        if (!Emisemble.isAvailable("mythsandlegends")) {
            return;
        }
        ResearchPayload payload = payload(event.getPlayerList().getServer().registryAccess());
        if (payload.entries().isEmpty()) {
            return;
        }
        Payloads.sync(event, payload);
    }

    private static ResearchPayload payload(RegistryAccess access) {
        List<ResearchPayload.Entry> entries = new ArrayList<>();
        for (ResearchDefinition definition : ResearchManager.getAll()) {
            entries.add(entry(definition, access));
        }
        return new ResearchPayload(entries);
    }

    public static ResearchPayload.Entry entry(ResearchDefinition definition, RegistryAccess access) {
        List<ResearchPayload.Input> inputs = new ArrayList<>();
        for (IngredientDefinition ingredient : definition.ingredients()) {
            ItemStack display = ingredient.item() == null ? ItemStack.EMPTY : ingredient.createDisplayStack();
            inputs.add(new ResearchPayload.Input(display, ingredient.tag(), ingredient.count()));
        }
        List<ItemStack> rewards = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        boolean notesDrawn = false;
        for (Reward reward : definition.rewards()) {
            ItemStack stack;
            int count = 1;
            if (reward instanceof ItemReward item) {
                stack = item.stack().copy();
                count = item.count();
            } else if (reward instanceof RecipeReward) {
                if (notesDrawn) {
                    continue;
                }
                stack = ResearchNotesItem.create(definition.id());
                notesDrawn = true;
            } else if (reward instanceof EnchantedBookReward book) {
                stack = enchantedBook(book, access);
                count = book.count();
            } else if (reward instanceof PotionReward potion) {
                stack = potionStack(potion, access);
                count = potion.count();
            } else {
                continue;
            }
            if (stack.isEmpty()) {
                continue;
            }
            rewards.add(stack);
            counts.add(count);
        }
        return new ResearchPayload.Entry(definition.id(), definition.name(), definition.requiredRank(),
                definition.researchTime(), definition.repeatable(), List.copyOf(inputs), List.copyOf(rewards),
                List.copyOf(counts));
    }

    /** Local definitions on an integrated server, the synced copies otherwise; empty when a remote server has no data. */
    public static List<ResearchPayload.Entry> views() {
        List<ResearchDefinition> local = List.copyOf(ResearchManager.getAll());
        RegistryAccess access = Minecraft.getInstance().level == null
                ? null : Minecraft.getInstance().level.registryAccess();
        if (access != null && !local.isEmpty()) {
            List<ResearchPayload.Entry> entries = new ArrayList<>(local.size());
            for (ResearchDefinition definition : local) {
                entries.add(entry(definition, access));
            }
            return entries;
        }
        return List.copyOf(SYNCED.values());
    }

    /** Stores what the server sent and rebakes EMI when the category is already registered. */
    public static void accept(ResearchPayload payload) {
        try {
            if (!ResearchManager.getAll().isEmpty()) {
                return;
            }
            for (ResearchPayload.Entry entry : payload.entries()) {
                SYNCED.put(entry.id(), entry);
            }
            EmiRecipeManager manager = EmiApi.getRecipeManager();
            if (manager != null && !manager.getCategories().isEmpty()) {
                EmiReloadManager.reload();
            }
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("mythsandlegends: could not apply the synced research data", t);
        }
    }

    private static ItemStack enchantedBook(EnchantedBookReward reward, RegistryAccess access) {
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK, reward.count());
        if (access == null) {
            return stack;
        }
        access.registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(ResourceKey.create(Registries.ENCHANTMENT, reward.enchantment()))
                .ifPresent(holder -> stack.enchant(holder, reward.level()));
        return stack;
    }

    private static ItemStack potionStack(PotionReward reward, RegistryAccess access) {
        ItemStack stack = new ItemStack(reward.container().item(), reward.count());
        if (access == null) {
            return stack;
        }
        if (reward.potionId() != null) {
            access.registryOrThrow(Registries.POTION)
                    .getHolder(ResourceKey.create(Registries.POTION, reward.potionId()))
                    .ifPresent(holder -> stack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder)));
            return stack;
        }
        List<MobEffectInstance> effects = new ArrayList<>();
        for (PotionReward.CustomEffect effect : reward.customEffects()) {
            access.registryOrThrow(Registries.MOB_EFFECT)
                    .getHolder(ResourceKey.create(Registries.MOB_EFFECT, effect.effect()))
                    .ifPresent(holder -> effects.add(new MobEffectInstance(holder, effect.duration(),
                            effect.amplifier())));
        }
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), effects));
        return stack;
    }
}
