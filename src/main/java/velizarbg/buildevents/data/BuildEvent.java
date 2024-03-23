package velizarbg.buildevents.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static velizarbg.buildevents.BuildEventsMod.server;

public record BuildEvent(@Nullable ServerWorld world, Box box, @Nullable ScoreboardObjective placeObjective, @Nullable ScoreboardObjective breakObjective, @Nullable Identifier predicate, boolean total) {
	public static final LootContextType BUILD_EVENT_ACTION = new LootContextType.Builder()
		.require(LootContextParameters.ORIGIN)
		.require(LootContextParameters.THIS_ENTITY)
		.require(LootContextParameters.TOOL)
		.build();

	public BuildEvent(@Nullable ServerWorld world, BlockPos from, BlockPos to, @Nullable ScoreboardObjective placeObjective, @Nullable ScoreboardObjective breakObjective, @Nullable Identifier predicate, boolean total) {
		this(
			world,
			new Box(from, to) {
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

	public BuildEvent withWorld(@Nullable ServerWorld world) {
		return new BuildEvent(world, this.box, this.placeObjective, this.breakObjective, this.predicate, this.total);
	}

	public BuildEvent withTotal(boolean total) {
		return new BuildEvent(world, this.box, this.placeObjective, this.breakObjective, this.predicate, total);
	}

	public boolean testPredicate(World world, PlayerEntity player, BlockPos pos, ItemStack stack) {
		LootCondition predicate = server.getPredicateManager().get(this.predicate);
		if (predicate == null) {
			return false;
		} else {
			LootContext.Builder builder = new LootContext.Builder((ServerWorld) world)
				.parameter(LootContextParameters.ORIGIN, Vec3d.of(pos))
				.parameter(LootContextParameters.THIS_ENTITY, player)
				.parameter(LootContextParameters.TOOL, stack);
			return predicate.test(builder.build(BUILD_EVENT_ACTION));
		}
	}

	public static BuildEvent createBuildEvent(String eventName, ServerWorld world, BlockPos from, BlockPos to, String type, Identifier predicate, boolean total) {
		ScoreboardObjective placeObjective = null;
		ScoreboardObjective breakObjective = null;
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

	private static ScoreboardObjective getOrCreateObjective(ServerScoreboard scoreboard, String objective) {
		return Optional
			.ofNullable(scoreboard.getNullableObjective(objective))
			.orElseGet(() -> scoreboard.addObjective(
				objective,
				ScoreboardCriterion.DUMMY,
				new LiteralText(objective),
				ScoreboardCriterion.RenderType.INTEGER
			));
	}
}
