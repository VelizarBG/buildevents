package velizarbg.buildevents.data;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
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
import net.minecraft.world.PersistentStateType;
import org.jetbrains.annotations.Nullable;
import velizarbg.buildevents.BuildEventsMod;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BuildEventsState extends PersistentState {
	public static final int VERSION = 3;
	private static final Codec<SerializedBuildEvent> SERIALIZED_BUILD_EVENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(SerializedBuildEvent::name),
		Identifier.CODEC.optionalFieldOf("dimension").forGetter(SerializedBuildEvent::dimension),
		BlockPos.CODEC.fieldOf("from").forGetter(SerializedBuildEvent::from),
		BlockPos.CODEC.fieldOf("to").forGetter(SerializedBuildEvent::to),
		Codec.STRING.fieldOf("type").forGetter(SerializedBuildEvent::type),
		Identifier.CODEC.optionalFieldOf("predicate").forGetter(SerializedBuildEvent::predicate),
		Codec.BOOL.optionalFieldOf("total", false).forGetter(SerializedBuildEvent::total)
	).apply(instance, SerializedBuildEvent::new));
	public static final Codec<BuildEventsState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		SERIALIZED_BUILD_EVENT_CODEC.listOf().optionalFieldOf("active_events", List.of()).forGetter(BuildEventsState::serializeActiveEvents),
		SERIALIZED_BUILD_EVENT_CODEC.listOf().optionalFieldOf("paused_events", List.of()).forGetter(BuildEventsState::serializePausedEvents),
		Codec.INT.optionalFieldOf("build_events_version", VERSION).forGetter(state -> VERSION)
	).apply(instance, BuildEventsState::fromSerialized));
	private static final PersistentStateType<BuildEventsState> TYPE = new PersistentStateType<>(
		"buildevents",
		BuildEventsState::new,
		CODEC,
		DataFixTypes.SAVED_DATA_COMMAND_STORAGE
	);

	public final BuildEventMap buildEvents = new BuildEventMap();
	public final Set<BuildEvent> placeEvents = Sets.newHashSet();
	public final Set<BuildEvent> breakEvents = Sets.newHashSet();

	private List<SerializedBuildEvent> serializeActiveEvents() {
		return serialize(buildEvents.activeEvents);
	}

	private List<SerializedBuildEvent> serializePausedEvents() {
		return serialize(buildEvents.pausedEvents);
	}

	private static List<SerializedBuildEvent> serialize(Map<String, BuildEvent> events) {
		return events.entrySet().stream().map(SerializedBuildEvent::fromEntry).toList();
	}

	private static BuildEventsState fromSerialized(List<SerializedBuildEvent> activeEvents, List<SerializedBuildEvent> pausedEvents, int version) {
		BuildEventsState buildEventsState = new BuildEventsState();
		deserialize(activeEvents, buildEventsState.buildEvents.activeEvents, version);
		for (BuildEvent event : buildEventsState.buildEvents.activeEvents.values()) {
			if (event.placeObjective() != null)
				buildEventsState.placeEvents.add(event);
			if (event.breakObjective() != null)
				buildEventsState.breakEvents.add(event);
		}
		deserialize(pausedEvents, buildEventsState.buildEvents.pausedEvents, version);
		return buildEventsState;
	}

	public static BuildEventsState loadBuildEvents(MinecraftServer server) {
		PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();
		return stateManager.getOrCreate(TYPE);
	}

	private static void deserialize(List<SerializedBuildEvent> serializedEvents, Map<String, BuildEvent> map, int version) {
		for (SerializedBuildEvent serializedEvent : serializedEvents) {
			BuildEvent event = serializedEvent.toBuildEvent();
			if (event == null)
				continue;

			map.put(serializedEvent.name(), event);
			if (version == 2 && serializedEvent.total()) {
				updateTotal(BuildEventsMod.server.getScoreboard(), event.placeObjective());
				updateTotal(BuildEventsMod.server.getScoreboard(), event.breakObjective());
			}
		}
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

	private record SerializedBuildEvent(
		String name,
		Optional<Identifier> dimension,
		BlockPos from,
		BlockPos to,
		String type,
		Optional<Identifier> predicate,
		boolean total
	) {
		private static SerializedBuildEvent fromEntry(Map.Entry<String, BuildEvent> stringBuildEventEntry) {
			BuildEvent event = stringBuildEventEntry.getValue();
			Box box = event.box();
			return new SerializedBuildEvent(
				stringBuildEventEntry.getKey(),
				Optional.ofNullable(event.world()).map(ServerWorld::getRegistryKey).map(RegistryKey::getValue),
				new BlockPos((int) box.minX, (int) box.minY, (int) box.minZ),
				new BlockPos((int) box.maxX, (int) box.maxY, (int) box.maxZ),
				getType(event),
				Optional.ofNullable(event.predicate()),
				event.total()
			);
		}

		private @Nullable BuildEvent toBuildEvent() {
			ServerWorld world = dimension
				.map(id -> BuildEventsMod.server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id)))
				.orElse(null);
			if (dimension.isPresent() && world == null)
				return null;

			return BuildEvent.createBuildEvent(name, world, from, to, type, predicate.orElse(null), total);
		}

		private static String getType(BuildEvent event) {
			boolean isPlaceEvent = event.placeObjective() != null;
			boolean isBreakEvent = event.breakObjective() != null;
			if (isPlaceEvent && isBreakEvent) {
				return "both";
			} else if (isPlaceEvent) {
				return "place";
			} else {
				return "break";
			}
		}
	}
}
