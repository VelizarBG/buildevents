package velizarbg.buildevents.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import velizarbg.buildevents.data.BuildEvent;

import static velizarbg.buildevents.BuildEventsMod.buildEventsState;

@Mixin(BlockItem.class)
public class BlockItemMixin {
	@Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/ItemCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V"))
	private void onBlockPlaced(CallbackInfoReturnable<ActionResult> cir, @Local BlockPos pos, @Local World world, @Local PlayerEntity player) {
		for (BuildEvent event : buildEventsState.placeEvents) {
			if (event.world() == world && event.box().contains(pos.getX(), pos.getY(), pos.getZ())) {
				world.getScoreboard().getPlayerScore(player.getEntityName(), event.placeObjective()).incrementScore();
			}
		}
	}
}
