package velizarbg.buildevents.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.ScoreAccess;
import org.jetbrains.annotations.Nullable;
import velizarbg.buildevents.data.BuildEvent;
import velizarbg.buildevents.utils.ThrowingFunction;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static velizarbg.buildevents.BuildEventsMod.TOTAL;
import static velizarbg.buildevents.BuildEventsMod.buildEventsState;
import static velizarbg.buildevents.BuildEventsMod.server;

public class BuildEventCommand {
	public static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = (_, builder) -> (
		SharedSuggestionProvider.suggest(buildEventsState.buildEvents.keySet(), builder)
	);
	public static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER_ACTIVE = (_, builder) -> (
		SharedSuggestionProvider.suggest(buildEventsState.buildEvents.activeEvents.keySet(), builder)
	);
	public static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER_PAUSED = (_, builder) -> (
		SharedSuggestionProvider.suggest(buildEventsState.buildEvents.pausedEvents.keySet(), builder)
	);
	private static final SuggestionProvider<CommandSourceStack> PREDICATE_SUGGESTION_PROVIDER = (context, builder) -> (
		context.getSource().suggestRegistryElements(Registries.PREDICATE, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS, builder, context)
	);
	private static final DynamicCommandExceptionType EVENT_EXISTS_EXCEPTION = new DynamicCommandExceptionType(event -> Component.translatableEscape("commands.buildevents.event_exists", event));
	private static final DynamicCommandExceptionType EVENT_NOT_EXIST_EXCEPTION = new DynamicCommandExceptionType(event -> Component.translatableEscape("commands.buildevents.event_not_exist", event));
	private static final DynamicCommandExceptionType UNKNOWN_PREDICATE_EXCEPTION = new DynamicCommandExceptionType(predicate -> Component.translatableEscape("commands.buildevents.set.predicate.unknown", predicate));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		BiFunction<
			LiteralArgumentBuilder<CommandSourceStack>,
			Function<
				ThrowingFunction<
					CommandContext<CommandSourceStack>,
					@Nullable ServerLevel,
					CommandSyntaxException
				>,
				Command<CommandSourceStack>
			>,
			LiteralArgumentBuilder<CommandSourceStack>
		> attachWorldArgs =
			(literal, commandGetter) -> literal
				.then(argument("dimension", DimensionArgument.dimension())
					.executes(commandGetter.apply(context -> DimensionArgument.getDimension(context, "dimension")))
				)
				.then(literal("!!global")
					.executes(commandGetter.apply(_ -> null))
				);
		UnaryOperator<LiteralArgumentBuilder<CommandSourceStack>> constructor =
			(literal) -> {
				Function<
					ThrowingFunction<
						CommandContext<CommandSourceStack>,
							ServerLevel,
						CommandSyntaxException
					>,
					Command<CommandSourceStack>
				> getEventAdder =
					(worldGetter) ->
						(context) -> addBuildEvent(
							context.getSource(),
							StringArgumentType.getString(context, "eventName"),
							worldGetter.apply(context),
							BlockPosArgument.getBlockPos(context, "from"),
							BlockPosArgument.getBlockPos(context, "to"),
							literal.getLiteral()
						);
				return literal
					.executes(getEventAdder.apply(context -> context.getSource().getLevel()))
					.then(attachWorldArgs.apply(literal("in"), getEventAdder));
			};

		dispatcher.register(
			literal("buildevents").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(literal("add")
					.then(argument("eventName", StringArgumentType.word())
						.then(argument("from", BlockPosArgument.blockPos())
							.then(argument("to", BlockPosArgument.blockPos())
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
							.then(argument("predicate", IdentifierArgument.id())
								.suggests(PREDICATE_SUGGESTION_PROVIDER)
								.executes(context -> {
									var predicate = IdentifierArgument.getId(context, "predicate");
									if (context.getSource().getServer().reloadableRegistries().lookup()
										.get(ResourceKey.create(Registries.PREDICATE, predicate))
										.isEmpty())
										throw UNKNOWN_PREDICATE_EXCEPTION.create(predicate);
									return setEventPredicate(
										context.getSource(),
										StringArgumentType.getString(context, "eventName"),
										predicate
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
							context.getSource().sendSuccess(() -> Component.translatable("commands.buildevents.list.empty"), false);
						} else {
							context.getSource().sendSuccess(() -> Component.translatable("commands.buildevents.list.success", events.size(), ComponentUtils.formatList(events)), false);
						}
						return events.size();
					})
					.then(literal("active")
						.executes(context -> {
							Set<String> events = buildEventsState.buildEvents.activeEvents.keySet();
							if (events.isEmpty()) {
								context.getSource().sendSuccess(() -> Component.translatable("commands.buildevents.list.active.empty"), false);
							} else {
								context.getSource().sendSuccess(() -> Component.translatable("commands.buildevents.list.active.success", events.size(), ComponentUtils.formatList(events)), false);
							}
							return events.size();
						})
					)
					.then(literal("paused")
						.executes(context -> {
							Set<String> events = buildEventsState.buildEvents.pausedEvents.keySet();
							if (events.isEmpty()) {
								context.getSource().sendSuccess(() -> Component.translatable("commands.buildevents.list.paused.empty"), false);
							} else {
								context.getSource().sendSuccess(() -> Component.translatable("commands.buildevents.list.paused.success", events.size(), ComponentUtils.formatList(events)), false);
							}
							return events.size();
						})
					)
				)
		);
	}

	private static int addBuildEvent(CommandSourceStack source, String eventName, ServerLevel world, BlockPos from, BlockPos to, String eventType) throws CommandSyntaxException {
		if (buildEventsState.buildEvents.containsKey(eventName))
			throw EVENT_EXISTS_EXCEPTION.create(eventName);

		BuildEvent event = BuildEvent.createBuildEvent(eventName, world, from, to, eventType, null, false);
		buildEventsState.buildEvents.activeEvents.put(eventName, event);
		if (event.placeObjective() != null)
			buildEventsState.placeEvents.add(event);
		if (event.breakObjective() != null)
			buildEventsState.breakEvents.add(event);

		buildEventsState.setDirty();
		source.sendSuccess(() -> Component.translatable("commands.buildevents.add.success", eventName), true);
		return buildEventsState.buildEvents.size();
	}

	private static int removeBuildEvent(CommandSourceStack source, String eventName, boolean removeObjectives) throws CommandSyntaxException {
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

		buildEventsState.setDirty();
		source.sendSuccess(() -> Component.translatable("commands.buildevents.remove.success", eventName), true);
		return buildEventsState.buildEvents.size();
	}

	private static int pauseBuildEvent(CommandSourceStack source, String eventName) throws CommandSyntaxException {
		BuildEvent event = getOrThrow(eventName);
		if (buildEventsState.buildEvents.activeEvents.remove(eventName) != null) {
			buildEventsState.buildEvents.pausedEvents.put(eventName, event);
			if (event.placeObjective() != null)
				buildEventsState.placeEvents.remove(event);
			if (event.breakObjective() != null)
				buildEventsState.breakEvents.remove(event);

			buildEventsState.setDirty();
			source.sendSuccess(() -> Component.translatable("commands.buildevents.pause.success", eventName), true);
			return 1;
		} else {
			source.sendSuccess(() -> Component.translatable("commands.buildevents.pause.ok", eventName), false);
			return 0;
		}
	}

	private static int unpauseBuildEvent(CommandSourceStack source, String eventName) throws CommandSyntaxException {
		BuildEvent event = getOrThrow(eventName);
		if (buildEventsState.buildEvents.pausedEvents.remove(eventName) != null) {
			buildEventsState.buildEvents.activeEvents.put(eventName, event);
			if (event.placeObjective() != null)
				buildEventsState.placeEvents.add(event);
			if (event.breakObjective() != null)
				buildEventsState.breakEvents.add(event);

			buildEventsState.setDirty();
			source.sendSuccess(() -> Component.translatable("commands.buildevents.unpause.success", eventName), true);
			return 1;
		} else {
			source.sendSuccess(() -> Component.translatable("commands.buildevents.unpause.ok", eventName), false);
			return 0;
		}
	}

	private static int setEventPredicate(CommandSourceStack source, String eventName, @Nullable Identifier predicate) throws CommandSyntaxException {
		BuildEvent event = getOrThrow(eventName);
		replaceEvent(eventName, event.withPredicate(predicate));

		if (Objects.equals(event.predicate(), predicate)) {
			source.sendSuccess(() -> Component.translatable("commands.buildevents.set.ok", eventName), false);
			return 0;
		} else {
			buildEventsState.setDirty();
			if (predicate == null) {
				source.sendSuccess(() -> Component.translatable("commands.buildevents.set.predicate.removed", eventName), true);
			} else {
				source.sendSuccess(() -> Component.translatable("commands.buildevents.set.predicate.success", predicate.toString(), eventName), true);
			}
			return 1;
		}
	}

	private static int setEventWorld(CommandSourceStack source, String eventName, @Nullable ServerLevel world) throws CommandSyntaxException {
		BuildEvent event = getOrThrow(eventName);
		replaceEvent(eventName, event.withWorld(world));

		if (event.world() == world) {
			source.sendSuccess(() -> Component.translatable("commands.buildevents.set.ok", eventName), false);
			return 0;
		} else {
			buildEventsState.setDirty();
			if (world == null) {
				source.sendSuccess(() -> Component.translatable("commands.buildevents.set.world.global", eventName), true);
			} else {
				source.sendSuccess(() -> Component.translatable("commands.buildevents.set.world.success", world.dimension().identifier().toString(), eventName), true);
			}
			return 1;
		}
	}

	private static int setEventTotal(CommandSourceStack source, String eventName, boolean total) throws CommandSyntaxException {
		BuildEvent event = getOrThrow(eventName);
		replaceEvent(eventName, event.withTotal(total));

		if (event.total() == total) {
			source.sendSuccess(() -> Component.translatable("commands.buildevents.set.ok", eventName), false);
			return 0;
		} else {
			buildEventsState.setDirty();
			if (total) {
				Consumer<Objective> totalProcessor = objective -> {
					if (objective == null)
						return;
					int totalCount = 0;
					for (PlayerScoreEntry scoreboardEntry : server.getScoreboard().listPlayerScores(objective)) {
						totalCount += scoreboardEntry.value();
					}
					ScoreAccess score = server.getScoreboard().getOrCreatePlayerScore(TOTAL, objective);
					score.set(totalCount);
					score.display(TOTAL.getDisplayName());
				};
				totalProcessor.accept(event.placeObjective());
				totalProcessor.accept(event.breakObjective());
				source.sendSuccess(() -> Component.translatable("commands.buildevents.set.total.true", eventName), true);
			} else {
				Consumer<Objective> totalRemover = objective -> {
					if (objective == null)
						return;
					server.getScoreboard().resetSinglePlayerScore(TOTAL, objective);
				};
				totalRemover.accept(event.placeObjective());
				totalRemover.accept(event.breakObjective());
				source.sendSuccess(() -> Component.translatable("commands.buildevents.set.total.false", eventName), true);
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
