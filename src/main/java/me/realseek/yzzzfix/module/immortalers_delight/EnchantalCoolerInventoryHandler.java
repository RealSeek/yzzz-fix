package me.realseek.yzzzfix.module.immortalers_delight;

import com.renyigesai.immortalers_delight.block.enchantal_cooler.EnchantalCoolerBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

/**
 * 将魔凝机物品栏的标准修改操作连接到方块实体的保存和客户端同步链路。
 *
 * <p>客户端菜单应用槽位数据时不会反向发送更新；只有服务端的实际物品变更会标记区块并
 * 广播方块实体更新。</p>
 */
public final class EnchantalCoolerInventoryHandler extends ItemStackHandler {

    private final EnchantalCoolerBlockEntity owner;

    public EnchantalCoolerInventoryHandler(EnchantalCoolerBlockEntity owner) {
        super(7);
        this.owner = owner;
    }

    @Override
    protected void onContentsChanged(int slot) {
        notifyOwner(this.owner);
    }

    public static void notifyOwner(EnchantalCoolerBlockEntity owner) {
        Level level = owner.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }

        owner.setChanged();
        BlockState state = owner.getBlockState();
        level.sendBlockUpdated(owner.getBlockPos(), state, state, 3);
    }
}
