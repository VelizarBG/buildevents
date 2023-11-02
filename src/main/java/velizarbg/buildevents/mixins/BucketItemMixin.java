package velizarbg.buildevents.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import velizarbg.buildevents.data.BuildEvent;

import static velizarbg.buildevents.BuildEventsMod.buildEventsState;

@Mixin(BucketItem.class)
public class BucketItemMixin {
	@Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/PlacedBlockCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V"))
	private void onBlockPlaced(CallbackInfoReturnable<TypedActionResult<ItemStack>> cir, @Local World world, @Local PlayerEntity player, @Local(ordinal = 2) BlockPos pos) {
		for (BuildEvent event : buildEventsState.placeEvents) {
			if (event.world() == world && event.box().contains(pos.getX(), pos.getY(), pos.getZ())) {
				world.getScoreboard().getPlayerScore(player.getEntityName(), event.placeObjective()).incrementScore();
			}
		}
	}
}
