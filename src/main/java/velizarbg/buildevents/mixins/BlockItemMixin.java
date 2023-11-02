package velizarbg.buildevents.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import velizarbg.buildevents.data.BuildEventsState;

import static velizarbg.buildevents.BuildEvents.buildEventsState;

@Mixin(BlockItem.class)
public class BlockItemMixin {
	@Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/ItemCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void onBlockPlaced(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir, ItemPlacementContext itemPlacementContext, BlockState blockState, BlockPos pos, World world, PlayerEntity player) {
		for (BuildEventsState.BuildEvent event : buildEventsState.placeEvents) {
			if (event.world() == world && event.box().contains(pos.getX(), pos.getY(), pos.getZ())) {
				world.getScoreboard().getPlayerScore(player.getEntityName(), event.placeObjective()).incrementScore();
			}
		}
	}
}
