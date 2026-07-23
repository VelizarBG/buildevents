package velizarbg.buildevents;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.ScoreHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import velizarbg.buildevents.commands.BuildEventCommand;
import velizarbg.buildevents.data.BuildEvent;
import velizarbg.buildevents.data.BuildEventsState;

public class BuildEventsMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("buildevents");
	public static final ScoreHolder TOTAL = new ScoreHolder() {
		private final Component displayName = Component.literal("Total").withStyle(ChatFormatting.BOLD);

		@Override
		public String getScoreboardName() {
			return "$total";
		}

		@Override
		public Component getDisplayName() {
			return displayName;
		}
	};
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
					&& (event.predicate() == null || event.testPredicate(world, player, pos, player.getMainHandItem()))) {
					scoreboard.getOrCreatePlayerScore(player, event.breakObjective()).increment();
					if (event.total())
						scoreboard.getOrCreatePlayerScore(TOTAL, event.breakObjective()).increment();
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

	public static void onPlace(Level world, Player player, BlockPos pos, ItemStack stack) {
		for (BuildEvent event : buildEventsState.placeEvents) {
			if ((event.world() == null || event.world() == world)
				&& event.box().contains(pos.getX(), pos.getY(), pos.getZ())
				&& (event.predicate() == null || event.testPredicate(world, player, pos, stack))) {
				scoreboard.getOrCreatePlayerScore(player, event.placeObjective()).increment();
				if (event.total())
					scoreboard.getOrCreatePlayerScore(TOTAL, event.placeObjective()).increment();
			}
		}
	}
}
