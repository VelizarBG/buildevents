package velizarbg.buildevents.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import velizarbg.buildevents.BuildEventsMod;

@Mixin(BucketItem.class)
public class BucketItemMixin {
	@Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/triggers/ItemUsedOnLocationTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemInstance;)V"))
	private void onBlockPlaced(CallbackInfoReturnable<?> cir, @Local(ordinal = 0) ItemStack stack, @Local(argsOnly = true) Level world, @Local(argsOnly = true) Player player, @Local(ordinal = 2) BlockPos pos) {
		BuildEventsMod.onPlace(world, player, pos, stack);
	}
}
