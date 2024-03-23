package velizarbg.buildevents;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import velizarbg.buildevents.commands.BuildEventCommand;
import velizarbg.buildevents.data.BuildEvent;
import velizarbg.buildevents.data.BuildEventsState;

public class BuildEventsMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("buildevents");
	public static final String TOTAL = Formatting.BOLD + "Total";
	public static MinecraftServer server;
	public static BuildEventsState buildEventsState;
	private static ServerScoreboard scoreboard;

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			BuildEventCommand.register(dispatcher)
		);
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			for (BuildEvent event : buildEventsState.breakEvents) {
				if ((event.world() == null || event.world() == world)
					&& event.box().contains(pos.getX(), pos.getY(), pos.getZ())
					&& (event.predicate() == null || event.testPredicate(world, player, pos, player.getMainHandStack()))) {
					scoreboard.getPlayerScore(player.getEntityName(), event.breakObjective()).incrementScore();
					if (event.total())
						scoreboard.getPlayerScore(TOTAL, event.breakObjective()).incrementScore();
				}
			}
			return true;
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
				BuildEventsMod.server = server;
				scoreboard = server.getScoreboard();
				buildEventsState = BuildEventsState.loadBuildEvents(server);
			}
		);
	}

	public static void onPlace(World world, PlayerEntity player, BlockPos pos, ItemStack stack) {
		for (BuildEvent event : buildEventsState.placeEvents) {
			if ((event.world() == null || event.world() == world)
				&& event.box().contains(pos.getX(), pos.getY(), pos.getZ())
				&& (event.predicate() == null || event.testPredicate(world, player, pos, stack))) {
				scoreboard.getPlayerScore(player.getEntityName(), event.placeObjective()).incrementScore();
				if (event.total())
					scoreboard.getPlayerScore(TOTAL, event.placeObjective()).incrementScore();
			}
		}
	}
}
