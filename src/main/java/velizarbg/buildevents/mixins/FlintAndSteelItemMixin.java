package velizarbg.buildevents.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import velizarbg.buildevents.BuildEventsMod;

@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelItemMixin {
	@Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/criterion/ItemUsedOnLocationTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
	private void onBlockPlaced(CallbackInfoReturnable<?> cir, @Local Player player, @Local Level world, @Local(ordinal = 1) BlockPos pos, @Local ItemStack stack) {
		BuildEventsMod.onPlace(world, player, pos, stack);
	}
}
