package velizarbg.buildevents;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;
import velizarbg.buildevents.commands.BuildEventCommand;

import java.util.Map;
import java.util.Set;

public class BuildEvents implements ModInitializer {
	public static final Map<String, BuildEvent> BUILD_EVENTS = Maps.newHashMap();
	public static final Set<BuildEvent> PLACE_EVENTS = Sets.newHashSet();
	public static final Set<BuildEvent> BREAK_EVENTS = Sets.newHashSet();

	/**
	 * Runs the mod initializer.
	 */
	@Override
	public void onInitialize() {
		// TODO: do serdes
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			BuildEventCommand.register(dispatcher);
		});
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			for (BuildEvent event : BREAK_EVENTS) {
				if (event.world() == world && event.box().contains(pos.getX(), pos.getY(), pos.getZ())) {
					world.getScoreboard().getPlayerScore(player.getEntityName(), event.breakObjective()).incrementScore();
				}
			}
		});
	}

	public record BuildEvent(ServerWorld world, Box box, @Nullable ScoreboardObjective placeObjective, @Nullable ScoreboardObjective breakObjective) {
		public BuildEvent(ServerWorld world, BlockPos pos1, BlockPos pos2, @Nullable ScoreboardObjective placeObjective, @Nullable ScoreboardObjective breakObjective) {
			this(world, new Box(pos1, pos2).expand(1.0E-7), placeObjective, breakObjective);
		}
	}
}
