/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  net.fabricmc.api.ModInitializer
 *  net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
 *  net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
 *  net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
 *  net.minecraft.class_124
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1937
 *  net.minecraft.class_2168
 *  net.minecraft.class_2338
 *  net.minecraft.class_2561
 *  net.minecraft.class_2995
 *  net.minecraft.class_9015
 *  net.minecraft.server.MinecraftServer
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package velizarbg.buildevents;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.class_124;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2168;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2995;
import net.minecraft.class_9015;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import velizarbg.buildevents.commands.BuildEventCommand;
import velizarbg.buildevents.data.BuildEvent;
import velizarbg.buildevents.data.BuildEventsState;

public class BuildEventsMod
implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"buildevents");
    public static final class_9015 TOTAL = new class_9015(){
        private final class_2561 displayName = class_2561.method_43470((String)"Total").method_27692(class_124.field_1067);

        public String method_5820() {
            return "$total";
        }

        public class_2561 method_5476() {
            return this.displayName;
        }
    };
    public static MinecraftServer server;
    public static BuildEventsState buildEventsState;
    private static class_2995 scoreboard;

    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> BuildEventCommand.register((CommandDispatcher<class_2168>)dispatcher));
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            for (BuildEvent event : BuildEventsMod.buildEventsState.breakEvents) {
                if (event.world() != null && event.world() != world || !event.box().method_1008((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.method_10260()) || event.predicate() != null && !event.testPredicate(world, player, pos, player.method_6047())) continue;
                scoreboard.method_1180((class_9015)player, event.breakObjective()).method_55413();
                if (!event.total()) continue;
                scoreboard.method_1180(TOTAL, event.breakObjective()).method_55413();
            }
            return true;
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            BuildEventsMod.server = server;
            scoreboard = server.method_3845();
            buildEventsState = BuildEventsState.loadBuildEvents(server);
        });
    }

    public static void onPlace(class_1937 world, class_1657 player, class_2338 pos, class_1799 stack) {
        for (BuildEvent event : BuildEventsMod.buildEventsState.placeEvents) {
            if (event.world() != null && event.world() != world || !event.box().method_1008((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.method_10260()) || event.predicate() != null && !event.testPredicate(world, player, pos, stack)) continue;
            scoreboard.method_1180((class_9015)player, event.placeObjective()).method_55413();
            if (!event.total()) continue;
            scoreboard.method_1180(TOTAL, event.placeObjective()).method_55413();
        }
    }
}

