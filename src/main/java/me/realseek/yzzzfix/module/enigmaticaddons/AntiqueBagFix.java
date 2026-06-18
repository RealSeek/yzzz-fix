package me.realseek.yzzzfix.module.enigmaticaddons;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;

/**
 * 修复古旧书袋跨维度/死亡后能力数据丢失的问题。
 * <p>通过反射调用 {@code IAntiqueBagHandler.writeTag/readTag} 在玩家 NBT 中持久化数据，
 * 并在玩家克隆、登录、登出及每 20 tick 时同步 {@code hasFlower} 状态。</p>
 */
public final class AntiqueBagFix {

    private static final String KEY = "aec_antique_bag_persist";
    private static final String CAP_CLASS = "auviotre.enigmatic.addon.contents.objects.bookbag.AntiqueBagCapability";
    private static final String HANDLER_CLASS = "auviotre.enigmatic.addon.contents.objects.bookbag.IAntiqueBagHandler";
    private static final ResourceLocation BAG_ID = new ResourceLocation("enigmaticaddons", "antique_bag");

    private static Capability<?> yzzzfix$inventoryCap;

    private AntiqueBagFix() {
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();

        original.revive();
        Capability<?> cap = getInventoryCapability();
        if (cap == null) return;

        original.getCapability(cap).ifPresent(oldHandler -> {
            Tag data = writeHandlerTag(oldHandler);
            if (data != null) {
                player.getPersistentData().put(KEY, data);
                player.getCapability(cap).ifPresent(newHandler -> readHandlerTag(newHandler, data));
            }
        });

        syncFlowerNbt(player, cap);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        Capability<?> cap = getInventoryCapability();
        if (cap == null) return;

        CompoundTag persist = player.getPersistentData();
        if (persist.contains(KEY, Tag.TAG_COMPOUND)) {
            Tag data = persist.get(KEY);
            player.getCapability(cap).ifPresent(handler -> readHandlerTag(handler, data));
        }

        syncFlowerNbt(player, cap);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        Capability<?> cap = getInventoryCapability();
        if (cap == null) return;

        player.getCapability(cap).ifPresent(handler -> {
            Tag data = writeHandlerTag(handler);
            if (data != null) {
                player.getPersistentData().put(KEY, data);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        if (player.tickCount % 20 != 0) return;
        Capability<?> cap = getInventoryCapability();
        if (cap == null) return;
        syncFlowerNbt(player, cap);
    }

    private static void syncFlowerNbt(Player player, Capability<?> cap) {
        boolean hasFlower = player.getCapability(cap)
                .map(AntiqueBagFix::handlerHasFlower)
                .orElse(false);

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty() && BAG_ID.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) {
                if (stack.getOrCreateTag().getBoolean("hasFlower") != hasFlower) {
                    ItemStack copy = stack.copy();
                    copy.getOrCreateTag().putBoolean("hasFlower", hasFlower);
                    player.getInventory().setItem(i, copy);
                }
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && BAG_ID.equals(ForgeRegistries.ITEMS.getKey(offhand.getItem()))) {
            if (offhand.getOrCreateTag().getBoolean("hasFlower") != hasFlower) {
                ItemStack copy = offhand.copy();
                copy.getOrCreateTag().putBoolean("hasFlower", hasFlower);
                player.setItemInHand(InteractionHand.OFF_HAND, copy);
            }
        }
    }

    private static Capability<?> getInventoryCapability() {
        if (yzzzfix$inventoryCap != null) return yzzzfix$inventoryCap;
        try {
            Class<?> capClass = Class.forName(CAP_CLASS);
            yzzzfix$inventoryCap = (Capability<?>) capClass.getField("INVENTORY").get(null);
        } catch (ReflectiveOperationException ignored) {
        }
        return yzzzfix$inventoryCap;
    }

    private static boolean handlerHasFlower(Object handler) {
        try {
            Method m = Class.forName(HANDLER_CLASS).getMethod("hasFlower");
            return (boolean) m.invoke(handler);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static Tag writeHandlerTag(Object handler) {
        try {
            Method m = Class.forName(HANDLER_CLASS).getMethod("writeTag");
            return (Tag) m.invoke(handler);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static void readHandlerTag(Object handler, Tag tag) {
        try {
            Method m = Class.forName(HANDLER_CLASS).getMethod("readTag", Tag.class);
            m.invoke(handler, tag);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
