package velizarbg.buildevents.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.BlockPos;
import velizarbg.buildevents.data.BuildEventsState.BuildEvent;

import java.util.Set;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static velizarbg.buildevents.BuildEvents.buildEventsState;

public class BuildEventCommand {
	public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> (
		CommandSource.suggestMatching(buildEventsState.buildEvents.keySet(), builder)
	);
	private static final DynamicCommandExceptionType ADD_FAILED_EXCEPTION = new DynamicCommandExceptionType(event -> Text.translatable("commands.buildevents.add.failed", event));
	private static final DynamicCommandExceptionType REMOVE_FAILED_EXCEPTION = new DynamicCommandExceptionType(event -> Text.translatable("commands.buildevents.remove.failed", event));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		Function<LiteralArgumentBuilder<ServerCommandSource>, LiteralArgumentBuilder<ServerCommandSource>> constructor =
			(literal) -> literal
				.executes(context -> addBuildEvent(
					context.getSource(),
					StringArgumentType.getString(context, "eventName"),
					BlockPosArgumentType.getBlockPos(context, "from"),
					BlockPosArgumentType.getBlockPos(context, "to"),
					context.getSource().getWorld(),
					literal.getLiteral()
				))
				.then(literal("in")
					.then(argument("dimension", DimensionArgumentType.dimension())
						.executes(context -> addBuildEvent(
							context.getSource(),
							StringArgumentType.getString(context, "eventName"),
							BlockPosArgumentType.getBlockPos(context, "from"),
							BlockPosArgumentType.getBlockPos(context, "to"),
							DimensionArgumentType.getDimensionArgument(context, "dimension"),
							literal.getLiteral()
						))
					)
				);

		dispatcher.register(
			literal("buildevents").requires(source -> source.hasPermissionLevel(2))
				.then(literal("add")
					.then(argument("eventName", StringArgumentType.word())
						.then(argument("from", BlockPosArgumentType.blockPos())
							.then(argument("to", BlockPosArgumentType.blockPos())
								.then(constructor.apply(literal("place")))
								.then(constructor.apply(literal("break")))
								.then(constructor.apply(literal("both")))
							)
						)
					)
				)
				.then(literal("remove")
					.then(argument("eventName", StringArgumentType.word())
						.suggests(SUGGESTION_PROVIDER)
						.executes(context -> removeBuildEvent(context.getSource(), StringArgumentType.getString(context, "eventName"), false))
						.then(literal("remove_objectives")
							.executes(context -> removeBuildEvent(context.getSource(), StringArgumentType.getString(context, "eventName"), true))
						)
					)
				)
				.then(literal("list")
					.executes(context -> {
						Set<String> events = buildEventsState.buildEvents.keySet();
						if (events.isEmpty()) {
							context.getSource().sendFeedback(() -> Text.translatable("commands.buildevents.list.empty"), false);
						} else {
							context.getSource().sendFeedback(() -> Text.translatable("commands.buildevents.list.success", events.size(), Texts.joinOrdered(events)), false);
						}
						return events.size();
					})
				)
		);
	}

	private static int addBuildEvent(ServerCommandSource source, String eventName, BlockPos from, BlockPos to, ServerWorld world, String eventType) throws CommandSyntaxException {
		if (buildEventsState.buildEvents.containsKey(eventName))
			throw ADD_FAILED_EXCEPTION.create(eventName);

		ScoreboardObjective placeObjective = null;
		ScoreboardObjective breakObjective = null;
		if (eventType.equals("both") || eventType.equals("place")) {
			placeObjective = world.getScoreboard().addObjective(
				eventName + "_place",
				ScoreboardCriterion.DUMMY,
				Text.literal(eventName + "_place"),
				ScoreboardCriterion.RenderType.INTEGER
			);
		}
		if (eventType.equals("both") || eventType.equals("break")) {
			breakObjective = world.getScoreboard().addObjective(
				eventName + "_break",
				ScoreboardCriterion.DUMMY,
				Text.literal(eventName + "_break"),
				ScoreboardCriterion.RenderType.INTEGER
			);
		}
		BuildEvent event = new BuildEvent(world, from, to, placeObjective, breakObjective);
		buildEventsState.buildEvents.put(eventName, event);
		if (placeObjective != null)
			buildEventsState.placeEvents.add(event);
		if (breakObjective != null)
			buildEventsState.breakEvents.add(event);

		buildEventsState.markDirty();
		source.sendFeedback(() -> Text.translatable("commands.buildevents.add.success", eventName), false);
		return 1;
	}

	private static int removeBuildEvent(ServerCommandSource source, String eventName, boolean removeObjectives) throws CommandSyntaxException {
		if (!buildEventsState.buildEvents.containsKey(eventName))
			throw REMOVE_FAILED_EXCEPTION.create(eventName);

		BuildEvent event = buildEventsState.buildEvents.remove(eventName);
		buildEventsState.placeEvents.remove(event);
		buildEventsState.breakEvents.remove(event);
		if (removeObjectives) {
			ServerScoreboard scoreboard = event.world().getScoreboard();
			if (event.placeObjective() != null)
				scoreboard.removeObjective(event.placeObjective());
			if (event.breakObjective() != null)
				scoreboard.removeObjective(event.breakObjective());
		}

		buildEventsState.markDirty();
		source.sendFeedback(() -> Text.translatable("commands.buildevents.remove.success", eventName), false);
		return 1;
	}
}
