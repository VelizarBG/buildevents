package velizarbg.buildevents.data;

import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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

	public static BuildEvent createBuildEvent(String eventName, ServerWorld world, BlockPos from, BlockPos to, String type) {
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
		return new BuildEvent(world, from, to, placeObjective, breakObjective);
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
