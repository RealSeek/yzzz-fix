package me.realseek.yzzzfix.module.cy3_core;

/**
 * CY3 战斗法师戒指修复使用的桥接接口。
 *
 * <p>该接口由 Mixin 注入到 {@code LivingEntity}，用于在不同优先级的 Mixin 之间传递暂存生命值。
 * 通过桥接接口可以避免直接依赖另一个 Mixin 类的实现细节。</p>
 */
public interface CY3CoreWarMageRingFixBridge {
    float yzzzFix$getSavedHealth();
    void yzzzFix$setSavedHealth(float value);
}
