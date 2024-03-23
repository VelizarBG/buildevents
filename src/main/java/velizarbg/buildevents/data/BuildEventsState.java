package velizarbg.buildevents.data;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import velizarbg.buildevents.BuildEventsMod;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BuildEventsState extends PersistentState {
	public static final int VERSION = 3;

	public final BuildEventMap buildEvents = new BuildEventMap();
	public final Set<BuildEvent> placeEvents = Sets.newHashSet();
	public final Set<BuildEvent> breakEvents = Sets.newHashSet();

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		Function<Map.Entry<String, BuildEvent>, NbtCompound> serializer = stringBuildEventEntry -> {
			String eventName = stringBuildEventEntry.getKey();
			BuildEvent event = stringBuildEventEntry.getValue();
			NbtCompound eventNbt = new NbtCompound();
			eventNbt.putString("name", eventName);
			if (event.world() != null)
				eventNbt.putString("dimension", event.world().getRegistryKey().getValue().toString());
			Box box = event.box();
			NbtElement from = BlockPos.CODEC
				.encodeStart(NbtOps.INSTANCE, new BlockPos((int) box.minX, (int) box.minY, (int) box.minZ))
				.getOrThrow(false, BuildEventsMod.LOGGER::warn);
			NbtElement to = BlockPos.CODEC
				.encodeStart(NbtOps.INSTANCE, new BlockPos((int) box.maxX, (int) box.maxY, (int) box.maxZ))
				.getOrThrow(false, BuildEventsMod.LOGGER::warn);
			eventNbt.put("from", from);
			eventNbt.put("to", to);
			boolean isPlaceEvent = event.placeObjective() != null;
			boolean isBreakEvent = event.breakObjective() != null;
			String type;
			if (isPlaceEvent && isBreakEvent) {
				type = "both";
			} else if (isPlaceEvent) {
				type = "place";
			} else {
				type = "break";
			}
			eventNbt.putString("type", type);
			if (event.predicate() != null)
				eventNbt.putString("predicate", event.predicate().toString());
			if (event.total())
				eventNbt.putBoolean("total", true);
			return eventNbt;
		};
		NbtList activeEvents = new NbtList();
		activeEvents.addAll(buildEvents.activeEvents.entrySet().stream().map(serializer).toList());
		nbt.put("active_events", activeEvents);
		NbtList pausedEvents = new NbtList();
		pausedEvents.addAll(buildEvents.pausedEvents.entrySet().stream().map(serializer).toList());
		nbt.put("paused_events", pausedEvents);
		nbt.putInt("build_events_version", VERSION);
		return nbt;
	}

	public static BuildEventsState readNbt(NbtCompound nbt, MinecraftServer server) {
		int version = nbt.getInt("build_events_version");
		BiConsumer<NbtList, Map<String, BuildEvent>> deserializer = (nbtList, map) -> {
			for (NbtElement element : nbtList) {
				if (element instanceof NbtCompound eventNbt) {
					String eventName = eventNbt.getString("name");
					String dimension = eventNbt.getString("dimension");
					BlockPos from = BlockPos.CODEC.decode(NbtOps.INSTANCE, eventNbt.get("from")).map(Pair::getFirst)
						.getOrThrow(false, BuildEventsMod.LOGGER::warn);
					BlockPos to = BlockPos.CODEC.decode(NbtOps.INSTANCE, eventNbt.get("to")).map(Pair::getFirst)
						.getOrThrow(false, BuildEventsMod.LOGGER::warn);
					String type = eventNbt.getString("type");
					String predicate = eventNbt.getString("predicate");
					Identifier predicateId = predicate.isEmpty() ? null : Identifier.tryParse(predicate);
					boolean total = eventNbt.getBoolean("total");

					ServerWorld world;
					if (dimension.isEmpty()) {
						world = null;
					} else {
						world = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(dimension)));
						if (world == null)
							continue;
					}

					map.put(eventName, BuildEvent.createBuildEvent(eventName, world, from, to, type, predicateId, total));
					if (version == 2 && total) {
						BuildEvent event = map.get(eventName);
						updateTotal(server.getScoreboard(), event.placeObjective());
						updateTotal(server.getScoreboard(), event.breakObjective());
					}
				}
			}
		};
		BuildEventsState buildEventsState = new BuildEventsState();
		NbtList activeEventsList = nbt.getList(version >= 1 ? "active_events" : "build_events", NbtElement.COMPOUND_TYPE);
		NbtList pausedEventsList = nbt.getList("paused_events", NbtElement.COMPOUND_TYPE);
		deserializer.accept(activeEventsList, buildEventsState.buildEvents.activeEvents);
		for (BuildEvent event : buildEventsState.buildEvents.activeEvents.values()) {
			if (event.placeObjective() != null)
				buildEventsState.placeEvents.add(event);
			if (event.breakObjective() != null)
				buildEventsState.breakEvents.add(event);
		}
		deserializer.accept(pausedEventsList, buildEventsState.buildEvents.pausedEvents);
		return buildEventsState;
	}

	public static BuildEventsState loadBuildEvents(MinecraftServer server) {
		PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();
		return stateManager.getOrCreate(new PersistentState.Type<>(BuildEventsState::new, compound -> readNbt(compound, server), null), "buildevents");
	}
	
	private static void updateTotal(Scoreboard scoreboard, ScoreboardObjective objective) {
		if (objective == null)
			return;
		ScoreHolder oldTotal = ScoreHolder.fromName(Formatting.BOLD + "Total");
		ReadableScoreboardScore oldScore = scoreboard.getScore(oldTotal, objective);
		if (oldScore == null)
			return;
		scoreboard.removeScore(oldTotal, objective);
		ScoreAccess newScore = scoreboard.getOrCreateScore(BuildEventsMod.TOTAL, objective);
		newScore.setScore(oldScore.getScore());
		newScore.setDisplayText(BuildEventsMod.TOTAL.getDisplayName());
	}
}
