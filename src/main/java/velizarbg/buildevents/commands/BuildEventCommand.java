package velizarbg.buildevents.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import velizarbg.buildevents.data.BuildEvent;
import velizarbg.buildevents.utils.ThrowingFunction;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static velizarbg.buildevents.BuildEventsMod.TOTAL;
import static velizarbg.buildevents.BuildEventsMod.buildEventsState;
import static velizarbg.buildevents.BuildEventsMod.server;

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
	private static final SuggestionProvider<ServerCommandSource> PREDICATE_SUGGESTION_PROVIDER = (context, builder) -> {
		LootManager lootManager = context.getSource().getServer().getLootManager();
		return CommandSource.suggestIdentifiers(lootManager.getIds(LootDataType.PREDICATES), builder);
	};
	private static final DynamicCommandExceptionType EVENT_EXISTS_EXCEPTION = new DynamicCommandExceptionType(event -> Text.translatable("commands.buildevents.event_exists", event));
	private static final DynamicCommandExceptionType EVENT_NOT_EXIST_EXCEPTION = new DynamicCommandExceptionType(event -> Text.translatable("commands.buildevents.event_not_exist", event));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		BiFunction<
			LiteralArgumentBuilder<ServerCommandSource>,
			Function<
				ThrowingFunction<
					CommandContext<ServerCommandSource>,
					@Nullable ServerWorld,
					CommandSyntaxException
				>,
				Command<ServerCommandSource>
			>,
			LiteralArgumentBuilder<ServerCommandSource>
		> attachWorldArgs =
			(literal, commandGetter) -> literal
				.then(argument("dimension", DimensionArgumentType.dimension())
					.executes(commandGetter.apply(context -> DimensionArgumentType.getDimensionArgument(context, "dimension")))
				)
				.then(literal("!!global")
					.executes(commandGetter.apply(context -> null))
				);
		UnaryOperator<LiteralArgumentBuilder<ServerCommandSource>> constructor =
			(literal) -> {
				Function<
					ThrowingFunction<
						CommandContext<ServerCommandSource>,
						ServerWorld,
						CommandSyntaxException
					>,
					Command<ServerCommandSource>
				> getEventAdder =
					(worldGetter) ->
						(context) -> addBuildEvent(
							context.getSource(),
							StringArgumentType.getString(context, "eventName"),
							worldGetter.apply(context),
							BlockPosArgumentType.getBlockPos(context, "from"),
							BlockPosArgumentType.getBlockPos(context, "to"),
							literal.getLiteral()
						);
				return literal
					.executes(getEventAdder.apply(context -> context.getSource().getWorld()))
					.then(attachWorldArgs.apply(literal("in"), getEventAdder));
			};

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
				.then(literal("set")
					.then(argument("eventName", StringArgumentType.word())
						.suggests(SUGGESTION_PROVIDER)
						.then(literal("predicate")
							.executes(context -> setEventPredicate(
								context.getSource(),
								StringArgumentType.getString(context, "eventName"),
								null
							))
							.then(argument("predicate", IdentifierArgumentType.identifier())
								.suggests(PREDICATE_SUGGESTION_PROVIDER)
								.executes(context -> {
									IdentifierArgumentType.getPredicateArgument(context, "predicate");
									return setEventPredicate(
										context.getSource(),
										StringArgumentType.getString(context, "eventName"),
										IdentifierArgumentType.getIdentifier(context, "predicate")
									);
								})
							)
						)
						.then(attachWorldArgs.apply(literal("dimension"), worldGetter ->
							context -> setEventWorld(context.getSource(), StringArgumentType.getString(context, "eventName"), worldGetter.apply(context))
						))
						.then(literal("total")
							.then(literal("true")
								.executes(context -> setEventTotal(context.getSource(), StringArgumentType.getString(context, "eventName"), true))
							)
							.then(literal("false")
								.executes(context -> setEventTotal(context.getSource(), StringArgumentType.getString(context, "eventName"), false))
							)
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
							context.getSource().sendFeedback(() -> Text.translatable("commands.buildevents.list.empty"), false);
						} else {
							context.getSource().sendFeedback(() -> Text.translatable("commands.buildevents.list.success", events.size(), Texts.joinOrdered(events)), false);
						}
						return events.size();
					})
					.then(literal("active")
						.executes(context -> {
							Set<String> events = buildEventsState.buildEvents.activeEvents.keySet();
							if (events.isEmpty()) {
								context.getSource().sendFeedback(() -> Text.translatable("commands.buildevents.list.active.empty"), false);
							} else {
								context.getSource().sendFeedback(() -> Text.translatable("commands.buildevents.list.active.success", events.size(), Texts.joinOrdered(events)), false);
							}
							return events.size();
						})
					)
					.then(literal("paused")
						.executes(context -> {
							Set<String> events = buildEventsState.buildEvents.pausedEvents.keySet();
							if (events.isEmpty()) {
								context.getSource().sendFeedback(() -> Text.translatable("commands.buildevents.list.paused.empty"), false);
							} else {
								context.getSource().sendFeedback(() -> Text.translatable("commands.buildevents.list.paused.success", events.size(), Texts.joinOrdered(events)), false);
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

		BuildEvent event = BuildEvent.createBuildEvent(eventName, world, from, to, eventType, null, false);
		buildEventsState.buildEvents.activeEvents.put(eventName, event);
		if (event.placeObjective() != null)
			buildEventsState.placeEvents.add(event);
		if (event.breakObjective() != null)
			buildEventsState.breakEvents.add(event);

		buildEventsState.markDirty();
		source.sendFeedback(() -> Text.translatable("commands.buildevents.add.success", eventName), true);
		return buildEventsState.buildEvents.size();
	}

	private static int removeBuildEvent(ServerCommandSource source, String eventName, boolean removeObjectives) throws CommandSyntaxException {
		BuildEvent event = buildEventsState.buildEvents.remove(eventName);
		if (event == null)
			throw EVENT_NOT_EXIST_EXCEPTION.create(eventName);

		buildEventsState.placeEvents.remove(event);
		buildEventsState.breakEvents.remove(event);
		if (removeObjectives) {
			ServerScoreboard scoreboard = server.getScoreboard();
			if (event.placeObjective() != null)
				scoreboard.removeObjective(event.placeObjective());
			if (event.breakObjective() != null)
				scoreboard.removeObjective(event.breakObjective());
		}

		buildEventsState.markDirty();
		source.sendFeedback(() -> Text.translatable("commands.buildevents.remove.success", eventName), true);
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
			source.sendFeedback(() -> Text.translatable("commands.buildevents.pause.success", eventName), true);
			return 1;
		} else {
			source.sendFeedback(() -> Text.translatable("commands.buildevents.pause.ok", eventName), false);
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
			source.sendFeedback(() -> Text.translatable("commands.buildevents.unpause.success", eventName), true);
			return 1;
		} else {
			source.sendFeedback(() -> Text.translatable("commands.buildevents.unpause.ok", eventName), false);
			return 0;
		}
	}

	private static int setEventPredicate(ServerCommandSource source, String eventName, @Nullable Identifier predicate) throws CommandSyntaxException {
		BuildEvent event = getOrThrow(eventName);
		replaceEvent(eventName, event.withPredicate(predicate));

		if (Objects.equals(event.predicate(), predicate)) {
			source.sendFeedback(() -> Text.translatable("commands.buildevents.set.ok", eventName), false);
			return 0;
		} else {
			buildEventsState.markDirty();
			if (predicate == null) {
				source.sendFeedback(() -> Text.translatable("commands.buildevents.set.predicate.removed", eventName), true);
			} else {
				source.sendFeedback(() -> Text.translatable("commands.buildevents.set.predicate.success", predicate.toString(), eventName), true);
			}
			return 1;
		}
	}

	private static int setEventWorld(ServerCommandSource source, String eventName, @Nullable ServerWorld world) throws CommandSyntaxException {
		BuildEvent event = getOrThrow(eventName);
		replaceEvent(eventName, event.withWorld(world));

		if (event.world() == world) {
			source.sendFeedback(() -> Text.translatable("commands.buildevents.set.ok", eventName), false);
			return 0;
		} else {
			buildEventsState.markDirty();
			if (world == null) {
				source.sendFeedback(() -> Text.translatable("commands.buildevents.set.world.global", eventName), true);
			} else {
				source.sendFeedback(() -> Text.translatable("commands.buildevents.set.world.success", world.getRegistryKey().getValue().toString(), eventName), true);
			}
			return 1;
		}
	}

	private static int setEventTotal(ServerCommandSource source, String eventName, boolean total) throws CommandSyntaxException {
		BuildEvent event = getOrThrow(eventName);
		replaceEvent(eventName, event.withTotal(total));

		if (event.total() == total) {
			source.sendFeedback(() -> Text.translatable("commands.buildevents.set.ok", eventName), false);
			return 0;
		} else {
			buildEventsState.markDirty();
			if (total) {
				Consumer<ScoreboardObjective> totalProcessor = objective -> {
					if (objective == null)
						return;
					int totalCount = 0;
					for (ScoreboardPlayerScore playerScore : server.getScoreboard().getAllPlayerScores(objective)) {
						totalCount += playerScore.getScore();
					}
					server.getScoreboard().getPlayerScore(TOTAL, objective).setScore(totalCount);
				};
				totalProcessor.accept(event.placeObjective());
				totalProcessor.accept(event.breakObjective());
				source.sendFeedback(() -> Text.translatable("commands.buildevents.set.total.true", eventName), true);
			} else {
				Consumer<ScoreboardObjective> totalRemover = objective -> {
					if (objective == null)
						return;
					server.getScoreboard().resetPlayerScore(TOTAL, objective);
				};
				totalRemover.accept(event.placeObjective());
				totalRemover.accept(event.breakObjective());
				source.sendFeedback(() -> Text.translatable("commands.buildevents.set.total.false", eventName), true);
			}
			return 1;
		}
	}

	private static BuildEvent getOrThrow(String eventName) throws CommandSyntaxException {
		BuildEvent event = buildEventsState.buildEvents.get(eventName);
		if (event == null)
			throw EVENT_NOT_EXIST_EXCEPTION.create(eventName);

		return event;
	}

	private static void replaceEvent(String eventName, BuildEvent event) {
		BuildEvent oldEvent = buildEventsState.buildEvents.replace(eventName, event);
		if (buildEventsState.buildEvents.activeEvents.containsKey(eventName)) {
			if (event.placeObjective() != null) {
				buildEventsState.placeEvents.remove(oldEvent);
				buildEventsState.placeEvents.add(event);
			}
			if (event.breakObjective() != null) {
				buildEventsState.breakEvents.remove(oldEvent);
				buildEventsState.breakEvents.add(event);
			}
		}
	}
}
