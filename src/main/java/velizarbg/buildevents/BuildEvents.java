package velizarbg.buildevents;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import velizarbg.buildevents.commands.BuildEventCommand;
import velizarbg.buildevents.data.BuildEventsState;

public class BuildEvents implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("buildevents");
	public static BuildEventsState buildEventsState;

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			BuildEventCommand.register(dispatcher)
		);
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			for (BuildEventsState.BuildEvent event : buildEventsState.breakEvents) {
				if (event.world() == world && event.box().contains(pos.getX(), pos.getY(), pos.getZ())) {
					world.getScoreboard().getPlayerScore(player.getEntityName(), event.breakObjective()).incrementScore();
				}
			}
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server ->
			buildEventsState = BuildEventsState.loadBuildEvents(server)
		);
	}
}
