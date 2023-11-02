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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record BuildEvent(ServerWorld world, Box box, @Nullable ScoreboardObjective placeObjective, @Nullable ScoreboardObjective breakObjective, @Nullable Identifier predicate) {
	public static final LootContextType BUILD_EVENT_ACTION = new LootContextType.Builder()
		.require(LootContextParameters.ORIGIN)
		.require(LootContextParameters.THIS_ENTITY)
		.require(LootContextParameters.TOOL)
		.build();

	public BuildEvent(ServerWorld world, BlockPos from, BlockPos to, @Nullable ScoreboardObjective placeObjective, @Nullable ScoreboardObjective breakObjective, @Nullable Identifier predicate) {
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
			predicate
		);
	}

	public BuildEvent withPredicate(Identifier predicate) {
		return new BuildEvent(this.world, this.box, this.placeObjective, this.breakObjective, predicate);
	}

	public boolean testPredicate(PlayerEntity player, BlockPos pos, ItemStack stack) {
		LootCondition predicate = this.world.getServer().getPredicateManager().get(this.predicate);
		if (predicate == null) {
			return false;
		} else {
			LootContext.Builder builder = new LootContext.Builder(this.world)
				.parameter(LootContextParameters.ORIGIN, Vec3d.of(pos))
				.parameter(LootContextParameters.THIS_ENTITY, player)
				.parameter(LootContextParameters.TOOL, stack);
			return predicate.test(builder.build(BUILD_EVENT_ACTION));
		}
	}

	public static BuildEvent createBuildEvent(String eventName, ServerWorld world, BlockPos from, BlockPos to, String type, Identifier predicate) {
		ScoreboardObjective placeObjective = null;
		ScoreboardObjective breakObjective = null;
		ServerScoreboard scoreboard = world.getScoreboard();
		if (type.equals("both") || type.equals("place")) {
			String objectiveName = eventName + "_place";
			placeObjective = getOrCreateObjective(scoreboard, objectiveName);
		}
		if (type.equals("both") || type.equals("break")) {
			String objectiveName = eventName + "_break";
			breakObjective = getOrCreateObjective(scoreboard, objectiveName);
		}
		return new BuildEvent(world, from, to, placeObjective, breakObjective, predicate);
	}

	private static ScoreboardObjective getOrCreateObjective(ServerScoreboard scoreboard, String objective) {
		return Optional
			.ofNullable(scoreboard.getNullableObjective(objective))
			.orElseGet(() -> scoreboard.addObjective(
				objective,
				ScoreboardCriterion.DUMMY,
				Text.literal(objective),
				ScoreboardCriterion.RenderType.INTEGER
			));
	}
}
