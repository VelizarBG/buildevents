/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  net.minecraft.class_2168
 *  net.minecraft.class_2172
 *  net.minecraft.class_2338
 *  net.minecraft.class_2561
 *  net.minecraft.class_266
 *  net.minecraft.class_2960
 *  net.minecraft.class_2995
 *  net.minecraft.class_3218
 *  net.minecraft.class_7924
 *  net.minecraft.class_9011
 *  net.minecraft.class_9014
 *  net.minecraft.class_9383$class_9385
 *  org.jetbrains.annotations.Nullable
 */
package velizarbg.buildevents.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.class_2168;
import net.minecraft.class_2172;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_2960;
import net.minecraft.class_2995;
import net.minecraft.class_3218;
import net.minecraft.class_7924;
import net.minecraft.class_9011;
import net.minecraft.class_9014;
import net.minecraft.class_9383;
import org.jetbrains.annotations.Nullable;
import velizarbg.buildevents.BuildEventsMod;
import velizarbg.buildevents.data.BuildEvent;

public class BuildEventCommand {
    public static final SuggestionProvider<class_2168> SUGGESTION_PROVIDER = (context, builder) -> class_2172.method_9265(BuildEventsMod.buildEventsState.buildEvents.keySet(), (SuggestionsBuilder)builder);
    public static final SuggestionProvider<class_2168> SUGGESTION_PROVIDER_ACTIVE = (context, builder) -> class_2172.method_9265(BuildEventsMod.buildEventsState.buildEvents.activeEvents.keySet(), (SuggestionsBuilder)builder);
    public static final SuggestionProvider<class_2168> SUGGESTION_PROVIDER_PAUSED = (context, builder) -> class_2172.method_9265(BuildEventsMod.buildEventsState.buildEvents.pausedEvents.keySet(), (SuggestionsBuilder)builder);
    private static final SuggestionProvider<class_2168> PREDICATE_SUGGESTION_PROVIDER = (context, builder) -> {
        class_9383.class_9385 lookup = ((class_2168)context.getSource()).method_9211().method_58576();
        return class_2172.method_9270((Iterable)lookup.method_58290(class_7924.field_50081), (SuggestionsBuilder)builder);
    };
    private static final DynamicCommandExceptionType EVENT_EXISTS_EXCEPTION = new DynamicCommandExceptionType(event -> class_2561.method_54159((String)"commands.buildevents.event_exists", (Object[])new Object[]{event}));
    private static final DynamicCommandExceptionType EVENT_NOT_EXIST_EXCEPTION = new DynamicCommandExceptionType(event -> class_2561.method_54159((String)"commands.buildevents.event_not_exist", (Object[])new Object[]{event}));
    private static final DynamicCommandExceptionType UNKNOWN_PREDICATE_EXCEPTION = new DynamicCommandExceptionType(predicate -> class_2561.method_54159((String)"commands.buildevents.set.predicate.unknown", (Object[])new Object[]{predicate}));

    /*
     * Exception decompiling
     */
    public static void register(CommandDispatcher<class_2168> dispatcher) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * java.lang.IndexOutOfBoundsException: Index 1 out of bounds for length 1
         *     at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:100)
         *     at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:106)
         *     at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:302)
         *     at java.base/java.util.Objects.checkIndex(Objects.java:385)
         *     at java.base/java.util.ArrayList.get(ArrayList.java:427)
         *     at org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance$Annotated$Iterator.moveParameterized(JavaGenericRefTypeInstance.java:113)
         *     at org.benf.cfr.reader.entities.attributes.TypePathPartParameterized.apply(TypePathPartParameterized.java:15)
         *     at org.benf.cfr.reader.bytecode.analysis.types.TypeAnnotationHelper.apply(TypeAnnotationHelper.java:54)
         *     at org.benf.cfr.reader.bytecode.analysis.types.TypeAnnotationHelper.apply(TypeAnnotationHelper.java:45)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.handleStatement(TypeAnnotationTransformer.java:141)
         *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment.rewriteExpressions(StructuredAssignment.java:144)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.ExpressionRewriterTransformer.transform(ExpressionRewriterTransformer.java:24)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.transform(Op04StructuredStatement.java:680)
         *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.Block.transformStructuredChildren(Block.java:421)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.ExpressionRewriterTransformer.transform(ExpressionRewriterTransformer.java:25)
         *     at org.benf.cfr.reader.bytecode.analysis.structured.expression.StructuredStatementExpression.applyExpressionRewriter(StructuredStatementExpression.java:60)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.rewriteExpression(TypeAnnotationTransformer.java:67)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.LambdaExpression.applyExpressionRewriter(LambdaExpression.java:75)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.rewriteExpression(TypeAnnotationTransformer.java:67)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterHelper.applyForwards(ExpressionRewriterHelper.java:12)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriterToArgs(AbstractMemberFunctionInvokation.java:101)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriter(AbstractMemberFunctionInvokation.java:88)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.rewriteExpression(TypeAnnotationTransformer.java:67)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression.applyExpressionRewriter(CastExpression.java:128)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.rewriteExpression(TypeAnnotationTransformer.java:67)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriter(AbstractMemberFunctionInvokation.java:87)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.rewriteExpression(TypeAnnotationTransformer.java:67)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression.applyExpressionRewriter(CastExpression.java:128)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.rewriteExpression(TypeAnnotationTransformer.java:67)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriter(AbstractMemberFunctionInvokation.java:87)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.rewriteExpression(TypeAnnotationTransformer.java:67)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterHelper.applyForwards(ExpressionRewriterHelper.java:12)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriterToArgs(AbstractMemberFunctionInvokation.java:101)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriter(AbstractMemberFunctionInvokation.java:88)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.rewriteExpression(TypeAnnotationTransformer.java:67)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression.applyExpressionRewriter(CastExpression.java:128)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.rewriteExpression(TypeAnnotationTransformer.java:67)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterHelper.applyForwards(ExpressionRewriterHelper.java:12)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriterToArgs(AbstractMemberFunctionInvokation.java:101)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriter(AbstractMemberFunctionInvokation.java:88)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.rewriteExpression(TypeAnnotationTransformer.java:67)
         *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredExpressionStatement.rewriteExpressions(StructuredExpressionStatement.java:70)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.transform(TypeAnnotationTransformer.java:61)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.transform(Op04StructuredStatement.java:680)
         *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.Block.transformStructuredChildren(Block.java:421)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.transform(TypeAnnotationTransformer.java:60)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.transform(Op04StructuredStatement.java:680)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypeAnnotationTransformer.transform(TypeAnnotationTransformer.java:55)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.applyTypeAnnotations(Op04StructuredStatement.java:733)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:957)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private static int addBuildEvent(class_2168 source, String eventName, class_3218 world, class_2338 from, class_2338 to, String eventType) throws CommandSyntaxException {
        if (BuildEventsMod.buildEventsState.buildEvents.containsKey(eventName)) {
            throw EVENT_EXISTS_EXCEPTION.create((Object)eventName);
        }
        BuildEvent event = BuildEvent.createBuildEvent(eventName, world, from, to, eventType, null, false);
        BuildEventsMod.buildEventsState.buildEvents.activeEvents.put(eventName, event);
        if (event.placeObjective() != null) {
            BuildEventsMod.buildEventsState.placeEvents.add(event);
        }
        if (event.breakObjective() != null) {
            BuildEventsMod.buildEventsState.breakEvents.add(event);
        }
        BuildEventsMod.buildEventsState.method_80();
        source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.add.success", (Object[])new Object[]{eventName}), true);
        return BuildEventsMod.buildEventsState.buildEvents.size();
    }

    private static int removeBuildEvent(class_2168 source, String eventName, boolean removeObjectives) throws CommandSyntaxException {
        BuildEvent event = BuildEventsMod.buildEventsState.buildEvents.remove(eventName);
        if (event == null) {
            throw EVENT_NOT_EXIST_EXCEPTION.create((Object)eventName);
        }
        BuildEventsMod.buildEventsState.placeEvents.remove(event);
        BuildEventsMod.buildEventsState.breakEvents.remove(event);
        if (removeObjectives) {
            class_2995 scoreboard = BuildEventsMod.server.method_3845();
            if (event.placeObjective() != null) {
                scoreboard.method_1194(event.placeObjective());
            }
            if (event.breakObjective() != null) {
                scoreboard.method_1194(event.breakObjective());
            }
        }
        BuildEventsMod.buildEventsState.method_80();
        source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.remove.success", (Object[])new Object[]{eventName}), true);
        return BuildEventsMod.buildEventsState.buildEvents.size();
    }

    private static int pauseBuildEvent(class_2168 source, String eventName) throws CommandSyntaxException {
        BuildEvent event = BuildEventCommand.getOrThrow(eventName);
        if (BuildEventsMod.buildEventsState.buildEvents.activeEvents.remove(eventName) != null) {
            BuildEventsMod.buildEventsState.buildEvents.pausedEvents.put(eventName, event);
            if (event.placeObjective() != null) {
                BuildEventsMod.buildEventsState.placeEvents.remove(event);
            }
            if (event.breakObjective() != null) {
                BuildEventsMod.buildEventsState.breakEvents.remove(event);
            }
            BuildEventsMod.buildEventsState.method_80();
            source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.pause.success", (Object[])new Object[]{eventName}), true);
            return 1;
        }
        source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.pause.ok", (Object[])new Object[]{eventName}), false);
        return 0;
    }

    private static int unpauseBuildEvent(class_2168 source, String eventName) throws CommandSyntaxException {
        BuildEvent event = BuildEventCommand.getOrThrow(eventName);
        if (BuildEventsMod.buildEventsState.buildEvents.pausedEvents.remove(eventName) != null) {
            BuildEventsMod.buildEventsState.buildEvents.activeEvents.put(eventName, event);
            if (event.placeObjective() != null) {
                BuildEventsMod.buildEventsState.placeEvents.add(event);
            }
            if (event.breakObjective() != null) {
                BuildEventsMod.buildEventsState.breakEvents.add(event);
            }
            BuildEventsMod.buildEventsState.method_80();
            source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.unpause.success", (Object[])new Object[]{eventName}), true);
            return 1;
        }
        source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.unpause.ok", (Object[])new Object[]{eventName}), false);
        return 0;
    }

    private static int setEventPredicate(class_2168 source, String eventName, @Nullable class_2960 predicate) throws CommandSyntaxException {
        BuildEvent event = BuildEventCommand.getOrThrow(eventName);
        BuildEventCommand.replaceEvent(eventName, event.withPredicate(predicate));
        if (Objects.equals(event.predicate(), predicate)) {
            source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.set.ok", (Object[])new Object[]{eventName}), false);
            return 0;
        }
        BuildEventsMod.buildEventsState.method_80();
        if (predicate == null) {
            source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.set.predicate.removed", (Object[])new Object[]{eventName}), true);
        } else {
            source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.set.predicate.success", (Object[])new Object[]{predicate.toString(), eventName}), true);
        }
        return 1;
    }

    private static int setEventWorld(class_2168 source, String eventName, @Nullable class_3218 world) throws CommandSyntaxException {
        BuildEvent event = BuildEventCommand.getOrThrow(eventName);
        BuildEventCommand.replaceEvent(eventName, event.withWorld(world));
        if (event.world() == world) {
            source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.set.ok", (Object[])new Object[]{eventName}), false);
            return 0;
        }
        BuildEventsMod.buildEventsState.method_80();
        if (world == null) {
            source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.set.world.global", (Object[])new Object[]{eventName}), true);
        } else {
            source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.set.world.success", (Object[])new Object[]{world.method_27983().method_29177().toString(), eventName}), true);
        }
        return 1;
    }

    private static int setEventTotal(class_2168 source, String eventName, boolean total) throws CommandSyntaxException {
        BuildEvent event = BuildEventCommand.getOrThrow(eventName);
        BuildEventCommand.replaceEvent(eventName, event.withTotal(total));
        if (event.total() == total) {
            source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.set.ok", (Object[])new Object[]{eventName}), false);
            return 0;
        }
        BuildEventsMod.buildEventsState.method_80();
        if (total) {
            Consumer<class_266> totalProcessor = objective -> {
                if (objective == null) {
                    return;
                }
                int totalCount = 0;
                for (class_9011 scoreboardEntry : BuildEventsMod.server.method_3845().method_1184(objective)) {
                    totalCount += scoreboardEntry.comp_2128();
                }
                class_9014 score = BuildEventsMod.server.method_3845().method_1180(BuildEventsMod.TOTAL, objective);
                score.method_55410(totalCount);
                score.method_55411(BuildEventsMod.TOTAL.method_5476());
            };
            totalProcessor.accept(event.placeObjective());
            totalProcessor.accept(event.breakObjective());
            source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.set.total.true", (Object[])new Object[]{eventName}), true);
        } else {
            Consumer<class_266> totalRemover = objective -> {
                if (objective == null) {
                    return;
                }
                BuildEventsMod.server.method_3845().method_1155(BuildEventsMod.TOTAL, objective);
            };
            totalRemover.accept(event.placeObjective());
            totalRemover.accept(event.breakObjective());
            source.method_9226(() -> class_2561.method_43469((String)"commands.buildevents.set.total.false", (Object[])new Object[]{eventName}), true);
        }
        return 1;
    }

    private static BuildEvent getOrThrow(String eventName) throws CommandSyntaxException {
        BuildEvent event = BuildEventsMod.buildEventsState.buildEvents.get(eventName);
        if (event == null) {
            throw EVENT_NOT_EXIST_EXCEPTION.create((Object)eventName);
        }
        return event;
    }

    private static void replaceEvent(String eventName, BuildEvent event) {
        BuildEvent oldEvent = BuildEventsMod.buildEventsState.buildEvents.replace(eventName, event);
        if (BuildEventsMod.buildEventsState.buildEvents.activeEvents.containsKey(eventName)) {
            if (event.placeObjective() != null) {
                BuildEventsMod.buildEventsState.placeEvents.remove(oldEvent);
                BuildEventsMod.buildEventsState.placeEvents.add(event);
            }
            if (event.breakObjective() != null) {
                BuildEventsMod.buildEventsState.breakEvents.remove(oldEvent);
                BuildEventsMod.buildEventsState.breakEvents.add(event);
            }
        }
    }
}

