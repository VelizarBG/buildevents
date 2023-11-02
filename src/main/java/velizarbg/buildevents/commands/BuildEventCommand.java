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
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.BlockPos;
import velizarbg.buildevents.data.BuildEvent;

import java.util.Set;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static velizarbg.buildevents.BuildEventsMod.buildEventsState;

public class BuildEventCommand {
	public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> (
		CommandSource.suggestMatching(buildEventsState.buildEvents.keySet(), builder)
	);
	public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER_ACTIVE = (context, builder) -> (
		CommandSource.suggestMatching(buildEventsState.buildEvents.activeEvents.keySet(), builder)
	);
	public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER_PAUSED = (context, builder) -> (
		CommandSource.suggestMatching(buildEventsState.buildEvents.pausedEvents.keySet(), builder)
	);
	private static final DynamicCommandExceptionType EVENT_EXISTS_EXCEPTION = new DynamicCommandExceptionType(event -> Text.translatable("commands.buildevents.event_exists", event));
	private static final DynamicCommandExceptionType EVENT_NOT_EXIST_EXCEPTION = new DynamicCommandExceptionType(event -> Text.translatable("commands.buildevents.event_not_exist", event));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		Function<LiteralArgumentBuilder<ServerCommandSource>, LiteralArgumentBuilder<ServerCommandSource>> constructor =
			(literal) -> literal
				.executes(context -> addBuildEvent(
					context.getSource(),
					StringArgumentType.getString(context, "eventName"),
					context.getSource().getWorld(),
					BlockPosArgumentType.getBlockPos(context, "from"),
					BlockPosArgumentType.getBlockPos(context, "to"),
					literal.getLiteral()
				))
				.then(literal("in")
					.then(argument("dimension", DimensionArgumentType.dimension())
						.executes(context -> addBuildEvent(
							context.getSource(),
							StringArgumentType.getString(context, "eventName"),
							DimensionArgumentType.getDimensionArgument(context, "dimension"),
							BlockPosArgumentType.getBlockPos(context, "from"),
							BlockPosArgumentType.getBlockPos(context, "to"),
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
				.then(literal("pause")
					.then(argument("eventName", StringArgumentType.word())
						.suggests(SUGGESTION_PROVIDER_ACTIVE)
						.executes(context -> pauseBuildEvent(context.getSource(), StringArgumentType.getString(context, "eventName")))
					)
				)
				.then(literal("unpause")
					.then(argument("eventName", StringArgumentType.word())
						.suggests(SUGGESTION_PROVIDER_PAUSED)
						.executes(context -> unpauseBuildEvent(context.getSource(), StringArgumentType.getString(context, "eventName")))
					)
				)
				.then(literal("list")
					.executes(context -> {
						Set<String> events = buildEventsState.buildEvents.keySet();
						if (events.isEmpty()) {
							context.getSource().sendFeedback(Text.translatable("commands.buildevents.list.empty"), false);
						} else {
							context.getSource().sendFeedback(Text.translatable("commands.buildevents.list.success", events.size(), Texts.joinOrdered(events)), false);
						}
						return events.size();
					})
					.then(literal("active")
						.executes(context -> {
							Set<String> events = buildEventsState.buildEvents.activeEvents.keySet();
							if (events.isEmpty()) {
								context.getSource().sendFeedback(Text.translatable("commands.buildevents.list.active.empty"), false);
							} else {
								context.getSource().sendFeedback(Text.translatable("commands.buildevents.list.active.success", events.size(), Texts.joinOrdered(events)), false);
							}
							return events.size();
						})
					)
					.then(literal("paused")
						.executes(context -> {
							Set<String> events = buildEventsState.buildEvents.pausedEvents.keySet();
							if (events.isEmpty()) {
								context.getSource().sendFeedback(Text.translatable("commands.buildevents.list.paused.empty"), false);
							} else {
								context.getSource().sendFeedback(Text.translatable("commands.buildevents.list.paused.success", events.size(), Texts.joinOrdered(events)), false);
							}
							return events.size();
						})
					)
				)
		);
	}

	private static int addBuildEvent(ServerCommandSource source, String eventName, ServerWorld world, BlockPos from, BlockPos to, String eventType) throws CommandSyntaxException {
		if (buildEventsState.buildEvents.containsKey(eventName))
			throw EVENT_EXISTS_EXCEPTION.create(eventName);

		BuildEvent event = BuildEvent.createBuildEvent(eventName, world, from, to, eventType);
		buildEventsState.buildEvents.activeEvents.put(eventName, event);
		if (event.placeObjective() != null)
			buildEventsState.placeEvents.add(event);
		if (event.breakObjective() != null)
			buildEventsState.breakEvents.add(event);

		buildEventsState.markDirty();
		source.sendFeedback(Text.translatable("commands.buildevents.add.success", eventName), true);
		return buildEventsState.buildEvents.size();
	}

	private static int removeBuildEvent(ServerCommandSource source, String eventName, boolean removeObjectives) throws CommandSyntaxException {
		BuildEvent event = buildEventsState.buildEvents.remove(eventName);
		if (event == null)
			throw EVENT_NOT_EXIST_EXCEPTION.create(eventName);

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
		source.sendFeedback(Text.translatable("commands.buildevents.remove.success", eventName), true);
		return buildEventsState.buildEvents.size();
	}

	private static int pauseBuildEvent(ServerCommandSource source, String eventName) throws CommandSyntaxException {
		BuildEvent event = getOrThrow(eventName);
		if (buildEventsState.buildEvents.activeEvents.remove(eventName) != null) {
			buildEventsState.buildEvents.pausedEvents.put(eventName, event);
			if (event.placeObjective() != null)
				buildEventsState.placeEvents.remove(event);
			if (event.breakObjective() != null)
				buildEventsState.breakEvents.remove(event);

			buildEventsState.markDirty();
			source.sendFeedback(Text.translatable("commands.buildevents.pause.success", eventName), true);
			return 1;
		} else {
			source.sendFeedback(Text.translatable("commands.buildevents.pause.ok", eventName), false);
			return 0;
		}
	}

	private static int unpauseBuildEvent(ServerCommandSource source, String eventName) throws CommandSyntaxException {
		BuildEvent event = getOrThrow(eventName);
		if (buildEventsState.buildEvents.pausedEvents.remove(eventName) != null) {
			buildEventsState.buildEvents.activeEvents.put(eventName, event);
			if (event.placeObjective() != null)
				buildEventsState.placeEvents.add(event);
			if (event.breakObjective() != null)
				buildEventsState.breakEvents.add(event);

			buildEventsState.markDirty();
			source.sendFeedback(Text.translatable("commands.buildevents.unpause.success", eventName), true);
			return 1;
		} else {
			source.sendFeedback(Text.translatable("commands.buildevents.unpause.ok", eventName), false);
			return 0;
		}
	}

	private static BuildEvent getOrThrow(String eventName) throws CommandSyntaxException {
		BuildEvent event = buildEventsState.buildEvents.get(eventName);
		if (event == null)
			throw EVENT_NOT_EXIST_EXCEPTION.create(eventName);

		return event;
	}
}
