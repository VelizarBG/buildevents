package velizarbg.buildevents.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.jetbrains.annotations.Nullable;
import velizarbg.buildevents.BuildEvents;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BuildEventsState extends PersistentState {
	public final Map<String, BuildEvent> buildEvents = Maps.newHashMap();
	public final Set<BuildEvent> placeEvents = Sets.newHashSet();
	public final Set<BuildEvent> breakEvents = Sets.newHashSet();

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		NbtList buildEventsList = new NbtList();
		buildEventsList.addAll(buildEvents.entrySet().stream().map(stringBuildEventEntry -> {
			BuildEvent event = stringBuildEventEntry.getValue();
			NbtCompound eventNbt = new NbtCompound();
			eventNbt.putString("name", stringBuildEventEntry.getKey());
			eventNbt.putString("dimension", event.world.getRegistryKey().getValue().toString());
			NbtElement from = BlockPos.CODEC
				.encodeStart(NbtOps.INSTANCE, new BlockPos((int) event.box.minX, (int) event.box.minY, (int) event.box.minZ))
				.getOrThrow(false, BuildEvents.LOGGER::warn);
			NbtElement to = BlockPos.CODEC
				.encodeStart(NbtOps.INSTANCE, new BlockPos((int) event.box.maxX, (int) event.box.maxY, (int) event.box.maxZ))
				.getOrThrow(false, BuildEvents.LOGGER::warn);
			eventNbt.put("from", from);
			eventNbt.put("to", to);
			String type;
			boolean isPlaceEvent = placeEvents.contains(event);
			boolean isBreakEvent = breakEvents.contains(event);
			if (isPlaceEvent && isBreakEvent) {
				type = "both";
			} else if (isPlaceEvent) {
				type = "place";
			} else {
				type = "break";
			}
			eventNbt.putString("type", type);
			return eventNbt;
		}).toList());
		nbt.put("build_events", buildEventsList);
		return nbt;
	}

	public static BuildEventsState readNbt(NbtCompound nbt, MinecraftServer server) {
		NbtList buildEventsList = nbt.getList("build_events", NbtElement.COMPOUND_TYPE);
		BuildEventsState buildEventsState = new BuildEventsState();
		for (NbtElement element : buildEventsList) {
			if (element instanceof NbtCompound eventNbt) {
				String eventName = eventNbt.getString("name");
				String dimension = eventNbt.getString("dimension");
				BlockPos from = BlockPos.CODEC.decode(NbtOps.INSTANCE, eventNbt.get("from")).map(Pair::getFirst)
					.getOrThrow(false, BuildEvents.LOGGER::warn);
				BlockPos to = BlockPos.CODEC.decode(NbtOps.INSTANCE, eventNbt.get("to")).map(Pair::getFirst)
					.getOrThrow(false, BuildEvents.LOGGER::warn);
				String type = eventNbt.getString("type");
				ScoreboardObjective placeObjective = null;
				ScoreboardObjective breakObjective = null;
				if (type.equals("both") || type.equals("place")) {
					String objectiveName = eventName + "_place";
					placeObjective = Optional.ofNullable(server.getScoreboard().getNullableObjective(objectiveName))
						.orElseGet(() -> server.getScoreboard().addObjective(
							objectiveName,
							ScoreboardCriterion.DUMMY,
							Text.literal(objectiveName),
							ScoreboardCriterion.RenderType.INTEGER
						));
				}
				if (type.equals("both") || type.equals("break")) {
					String objectiveName = eventName + "_break";
					breakObjective = Optional.ofNullable(server.getScoreboard().getNullableObjective(objectiveName))
						.orElseGet(() -> server.getScoreboard().addObjective(
							objectiveName,
							ScoreboardCriterion.DUMMY,
							Text.literal(objectiveName),
							ScoreboardCriterion.RenderType.INTEGER
						));
				}
				BuildEvent event = new BuildEvent(
					server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(dimension))),
					from,
					to,
					placeObjective,
					breakObjective
				);
				buildEventsState.buildEvents.put(eventName, event);
				if (placeObjective != null)
					buildEventsState.placeEvents.add(event);
				if (breakObjective != null)
					buildEventsState.breakEvents.add(event);
			}
		}
		return buildEventsState;
	}

	public static BuildEventsState loadBuildEvents(MinecraftServer server) {
		PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();
		return stateManager.getOrCreate((compound) -> readNbt(compound, server), BuildEventsState::new, "buildevents");
	}

	public record BuildEvent(ServerWorld world, Box box, @Nullable ScoreboardObjective placeObjective, @Nullable ScoreboardObjective breakObjective) {
		public BuildEvent(ServerWorld world, BlockPos from, BlockPos to, @Nullable ScoreboardObjective placeObjective, @Nullable ScoreboardObjective breakObjective) {
			this(
				world,
				new Box(from, to) {
					@Override
					public boolean contains(double x, double y, double z) {
						return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
					}
				},
				placeObjective,
				breakObjective
			);
		}
	}
}
