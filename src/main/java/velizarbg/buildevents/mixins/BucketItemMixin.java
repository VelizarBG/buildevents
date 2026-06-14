/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.class_1269 *  net.minecraft.class_1657
 *  net.minecraft.class_1755
 *  net.minecraft.class_1799
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package velizarbg.buildevents.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1755;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import velizarbg.buildevents.BuildEventsMod;

@Mixin(value={class_1755.class})
public class BucketItemMixin {
    @Inject(method={"use"}, at={@At(value="INVOKE", target="Lnet/minecraft/advancement/criterion/ItemCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V")})
    private void onBlockPlaced(CallbackInfoReturnable<class_1269> cir, @Local(ordinal=0) class_1799 stack, @Local(argsOnly=true) class_1937 world, @Local(argsOnly=true) class_1657 player, @Local(ordinal=2) class_2338 pos) {
        BuildEventsMod.onPlace(world, player, pos, stack);
    }
}



