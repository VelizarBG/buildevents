package velizarbg.buildevents.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static velizarbg.buildevents.BuildEventsMod.server;

public record BuildEvent(@Nullable ServerLevel world, AABB box, @Nullable Objective placeObjective, @Nullable Objective breakObjective, @Nullable Identifier predicate, boolean total) {
	public static final ContextKeySet BUILD_EVENT_ACTION = new ContextKeySet.Builder()
		.required(LootContextParams.ORIGIN)
		.required(LootContextParams.THIS_ENTITY)
		.required(LootContextParams.TOOL)
		.build();

	public BuildEvent(@Nullable ServerLevel world, BlockPos from, BlockPos to, @Nullable Objective placeObjective, @Nullable Objective breakObjective, @Nullable Identifier predicate, boolean total) {
		this(
			world,
			new AABB(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ()) {
				@Override
				public boolean contains(double x, double y, double z) {
					return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
				}
			},
			placeObjective,
			breakObjective,
			predicate,
			total
		);
	}

	public BuildEvent withPredicate(Identifier predicate) {
		return new BuildEvent(this.world, this.box, this.placeObjective, this.breakObjective, predicate, this.total);
	}

	public BuildEvent withWorld(@Nullable ServerLevel world) {
		return new BuildEvent(world, this.box, this.placeObjective, this.breakObjective, this.predicate, this.total);
	}

	public BuildEvent withTotal(boolean total) {
		return new BuildEvent(world, this.box, this.placeObjective, this.breakObjective, this.predicate, total);
	}

	public boolean testPredicate(Level world, Player player, BlockPos pos, ItemStack stack) {
		LootItemCondition predicate = server.reloadableRegistries().lookup()
			.get(ResourceKey.create(Registries.PREDICATE, this.predicate))
			.map(Holder::value)
			.orElse(null);
		if (predicate == null) {
			return false;
		} else {
			LootParams lootParams = new LootParams.Builder((ServerLevel) world)
				.withParameter(LootContextParams.ORIGIN, Vec3.atLowerCornerOf(pos))
				.withParameter(LootContextParams.THIS_ENTITY, player)
				.withParameter(LootContextParams.TOOL, stack)
				.create(BUILD_EVENT_ACTION);
			LootContext lootContext = new LootContext.Builder(lootParams).create(Optional.empty());
			lootContext.pushVisitedElement(LootContext.createVisitedEntry(predicate));
			return predicate.test(lootContext);
		}
	}

	public static BuildEvent createBuildEvent(String eventName, ServerLevel world, BlockPos from, BlockPos to, String type, Identifier predicate, boolean total) {
		Objective placeObjective = null;
		Objective breakObjective = null;
		ServerScoreboard scoreboard = server.getScoreboard();
		if (type.equals("both") || type.equals("place")) {
			String objectiveName = eventName + "_place";
			placeObjective = getOrCreateObjective(scoreboard, objectiveName);
		}
		if (type.equals("both") || type.equals("break")) {
			String objectiveName = eventName + "_break";
			breakObjective = getOrCreateObjective(scoreboard, objectiveName);
		}
		return new BuildEvent(world, from, to, placeObjective, breakObjective, predicate, total);
	}

	private static Objective getOrCreateObjective(ServerScoreboard scoreboard, String objective) {
		return Optional
			.ofNullable(scoreboard.getObjective(objective))
			.orElseGet(() -> scoreboard.addObjective(
				objective,
				ObjectiveCriteria.DUMMY,
				Component.literal(objective),
				ObjectiveCriteria.RenderType.INTEGER,
				false,
				null
			));
	}
}
