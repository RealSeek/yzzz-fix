package me.realseek.yzzzfix.mixin.celestial_forge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.UUID;

/**
 * 修复 Celestial Forge 的 Curios 修饰符 UUID 生成冲突问题。
 *
 * <p>原始实现中使用 OR 运算组合 UUID 位，导致不同饰品槽或修饰符之间可能产生重复 UUID，
 * 使属性修饰符互相覆盖。该 Mixin 使用 ADD 运算替代，确保 UUID 唯一性。</p>
 */
@Mixin(targets = "com.xiaoyue.celestial_forge.utils.ModifierUtils", remap = false)
public class ModifierUtilsMixin {

    /**
     * @author yzzz-fix
     * @reason Fix UUID collision caused by using OR instead of ADD.
     */
    @Overwrite
    public static UUID getCurioUuid(String slotIdentifier, int slotIndex, int attributeIndex) {
        long leastSigBits = ((long) attributeIndex << 32) | (slotIdentifier.hashCode() & 0xFFFFFFFFL);
        long mostSigBits = 8821609852410265600L + ((long) slotIndex << 32);
        return new UUID(mostSigBits, leastSigBits);
    }
}
