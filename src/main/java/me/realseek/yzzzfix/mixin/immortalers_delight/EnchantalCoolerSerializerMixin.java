package me.realseek.yzzzfix.mixin.immortalers_delight;

import com.renyigesai.immortalers_delight.recipe.EnchantalCoolerRecipe;
import me.realseek.yzzzfix.YzzzFix;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantalCoolerRecipe.Serializer.class)
public abstract class EnchantalCoolerSerializerMixin {

    @Inject(
            method = "fromNetwork(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Lcom/renyigesai/immortalers_delight/recipe/EnchantalCoolerRecipe;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void yzzzFix$fixFromNetwork(
            ResourceLocation recipeId,
            FriendlyByteBuf buf,
            CallbackInfoReturnable<EnchantalCoolerRecipe> cir
    ) {
        int ingredientCount = buf.readInt();
        NonNullList<Ingredient> inputs = NonNullList.withSize(ingredientCount, Ingredient.EMPTY);
        for (int index = 0; index < ingredientCount; index++) {
            inputs.set(index, Ingredient.fromNetwork(buf));
        }
        ItemStack output = buf.readItem();
        ItemStack container = buf.readItem();
        cir.setReturnValue(new EnchantalCoolerRecipe(inputs, output, container, recipeId));
        YzzzFix.LOGGER.debug("Fixed Immortalers Delight Enchantal Cooler deserialization for recipe {}.", recipeId);
    }
}
