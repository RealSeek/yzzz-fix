package me.realseek.yzzzfix.mixin.immortalers_delight;

import com.renyigesai.immortalers_delight.block.enchantal_cooler.EnchantalCoolerBlockEntity;
import me.realseek.yzzzfix.module.immortalers_delight.EnchantalCoolerInventoryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复魔凝机新旧物品栏格式识别错误导致的物品丢失。
 *
 * <p>原模组使用 {@code newVersion} 控制五槽到七槽的迁移，但新建方块实体的字段初值为
 * {@code false}，且原模组不会持久化该字段。这里让新建机器直接使用七槽格式，并依据 NBT
 * 中的槽位数量和旧版独立槽标签识别真正需要迁移的旧数据。</p>
 *
 * <p>同时将原始物品栏替换为带变更通知的实现，使菜单、漏斗和物品能力产生的修改能够被
 * 方块实体保存并同步。</p>
 */
@Mixin(value = EnchantalCoolerBlockEntity.class, remap = false)
public abstract class EnchantalCoolerBlockEntityMixin {

    @Shadow
    @Final
    @Mutable
    private ItemStackHandler inventory;

    @Shadow
    private boolean newVersion;

    @Unique
    private static final String yzzzfix$VERSION_KEY = "yzzzfix_NewVersion";

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void yzzzfix$initializeCurrentInventory(BlockPos pos, BlockState state, CallbackInfo ci) {
        EnchantalCoolerBlockEntity owner = (EnchantalCoolerBlockEntity) (Object) this;
        this.inventory = new EnchantalCoolerInventoryHandler(owner);
        this.newVersion = true;
    }

    @Inject(method = "m_142466_", at = @At("HEAD"), remap = false)
    private void yzzzfix$restoreNewVersion(CompoundTag tag, CallbackInfo ci) {
        CompoundTag inventoryTag = tag.getCompound("Inventory");
        boolean legacyInventorySize = inventoryTag.contains("Size", Tag.TAG_INT)
                && inventoryTag.getInt("Size") == 5;
        boolean hasLegacySeparateSlots = tag.contains("Containerslot", Tag.TAG_COMPOUND)
                || tag.contains("Fuelslot", Tag.TAG_COMPOUND);
        this.newVersion = !legacyInventorySize && !hasLegacySeparateSlots;
    }

    @Inject(method = "m_183515_", at = @At("TAIL"), remap = false)
    private void yzzzfix$saveNewVersion(CompoundTag tag, CallbackInfo ci) {
        this.newVersion = true;
        tag.putBoolean(yzzzfix$VERSION_KEY, true);
    }
}
