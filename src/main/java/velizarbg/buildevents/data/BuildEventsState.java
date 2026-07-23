package velizarbg.buildevents.data;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.*;
import velizarbg.buildevents.BuildEventsMod;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BuildEventsState extends SavedData {
	public static final int VERSION = 3;

	public final BuildEventMap buildEvents = new BuildEventMap();
	public final Set<BuildEvent> placeEvents = Sets.newHashSet();
	public final Set<BuildEvent> breakEvents = Sets.newHashSet();

	public CompoundTag encode() {
		var nbt = new CompoundTag();
		Function<Map.Entry<String, BuildEvent>, CompoundTag> serializer = stringBuildEventEntry -> {
			String eventName = stringBuildEventEntry.getKey();
			BuildEvent event = stringBuildEventEntry.getValue();
			CompoundTag eventNbt = new CompoundTag();
			eventNbt.putString("name", eventName);
			if (event.world() != null)
				eventNbt.putString("dimension", event.world().dimension().identifier().toString());
			AABB box = event.box();
			Tag from = BlockPos.CODEC
				.encodeStart(NbtOps.INSTANCE, new BlockPos((int) box.minX, (int) box.minY, (int) box.minZ))
				.getOrThrow();
			Tag to = BlockPos.CODEC
				.encodeStart(NbtOps.INSTANCE, new BlockPos((int) box.maxX, (int) box.maxY, (int) box.maxZ))
				.getOrThrow();
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
		ListTag activeEvents = new ListTag();
		activeEvents.addAll(buildEvents.activeEvents.entrySet().stream().map(serializer).toList());
		nbt.put("active_events", activeEvents);
		ListTag pausedEvents = new ListTag();
		pausedEvents.addAll(buildEvents.pausedEvents.entrySet().stream().map(serializer).toList());
		nbt.put("paused_events", pausedEvents);
		nbt.putInt("build_events_version", VERSION);
		return nbt;
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public static BuildEventsState decode(CompoundTag nbt, MinecraftServer server) {
		int version = nbt.getInt("build_events_version").get();
		BiConsumer<ListTag, Map<String, BuildEvent>> deserializer = (nbtList, map) -> {
			for (Tag element : nbtList) {
				if (element instanceof CompoundTag eventNbt) {
					String eventName = eventNbt.getString("name").get();
					String dimension = eventNbt.getStringOr("dimension", "");
					BlockPos from = BlockPos.CODEC.decode(NbtOps.INSTANCE, eventNbt.get("from")).map(Pair::getFirst)
						.getOrThrow();
					BlockPos to = BlockPos.CODEC.decode(NbtOps.INSTANCE, eventNbt.get("to")).map(Pair::getFirst)
						.getOrThrow();
					String type = eventNbt.getString("type").get();
					String predicate = eventNbt.getStringOr("predicate", "");
					Identifier predicateId = predicate.isEmpty() ? null : Identifier.tryParse(predicate);
					boolean total = eventNbt.getBooleanOr("total", false);

					ServerLevel world;
					if (dimension.isEmpty()) {
						world = null;
					} else {
						world = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.tryParse(dimension)));
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
		ListTag activeEventsList = nbt.getList(version >= 1 ? "active_events" : "build_events").get();
		ListTag pausedEventsList = nbt.getList("paused_events").get();
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

	// TODO move to proper Codec eventually; hack taken from net.fabricmc.fabric.impl.attachment.AttachmentPersistentState
	public static BuildEventsState loadBuildEvents(MinecraftServer server) {
		DimensionDataStorage dataStorage = server.overworld().getDataStorage();
		return dataStorage.computeIfAbsent(new SavedDataType<>("buildevents", BuildEventsState::new, Codec.of(new Encoder<>() {
			@Override
			public <T> DataResult<T> encode(BuildEventsState input, DynamicOps<T> ops, T prefix) {
				return DataResult.success(NbtOps.INSTANCE.convertTo(ops, input.encode()));
			}
		}, new Decoder<>() {
			@Override
			public <T> DataResult<Pair<BuildEventsState, T>> decode(DynamicOps<T> ops, T input) {
				return DataResult.success(Pair.of(BuildEventsState.decode((CompoundTag) ops.convertTo(NbtOps.INSTANCE, input), server), ops.empty()));
			}
		}), null));
	}
	
	private static void updateTotal(Scoreboard scoreboard, Objective objective) {
		if (objective == null)
			return;
		ScoreHolder oldTotal = ScoreHolder.forNameOnly(ChatFormatting.BOLD + "Total");
		ReadOnlyScoreInfo oldScore = scoreboard.getPlayerScoreInfo(oldTotal, objective);
		if (oldScore == null)
			return;
		scoreboard.resetSinglePlayerScore(oldTotal, objective);
		ScoreAccess newScore = scoreboard.getOrCreatePlayerScore(BuildEventsMod.TOTAL, objective);
		newScore.set(oldScore.value());
		newScore.display(BuildEventsMod.TOTAL.getDisplayName());
	}
}
