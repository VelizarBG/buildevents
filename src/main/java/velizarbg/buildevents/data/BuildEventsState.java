/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DynamicOps
 *  net.minecraft.class_124
 *  net.minecraft.class_18
 *  net.minecraft.class_18$class_8645
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_2487
 *  net.minecraft.class_2499
 *  net.minecraft.class_2509
 *  net.minecraft.class_2520
 *  net.minecraft.class_26
 *  net.minecraft.class_266
 *  net.minecraft.class_269
 *  net.minecraft.class_2960
 *  net.minecraft.class_3218
 *  net.minecraft.class_5321
 *  net.minecraft.class_7225$class_7874
 *  net.minecraft.class_7924
 *  net.minecraft.class_9013
 *  net.minecraft.class_9014
 *  net.minecraft.class_9015
 *  net.minecraft.server.MinecraftServer
 */
package velizarbg.buildevents.data;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.class_124;
import net.minecraft.class_18;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2509;
import net.minecraft.class_2520;
import net.minecraft.class_26;
import net.minecraft.class_266;
import net.minecraft.class_269;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_5321;
import net.minecraft.class_7225;
import net.minecraft.class_7924;
import net.minecraft.class_9013;
import net.minecraft.class_9014;
import net.minecraft.class_9015;
import net.minecraft.server.MinecraftServer;
import velizarbg.buildevents.BuildEventsMod;
import velizarbg.buildevents.data.BuildEvent;
import velizarbg.buildevents.data.BuildEventMap;

public class BuildEventsState
extends class_18 {
    public static final int VERSION = 3;
    public final BuildEventMap buildEvents = new BuildEventMap();
    public final Set<BuildEvent> placeEvents = Sets.newHashSet();
    public final Set<BuildEvent> breakEvents = Sets.newHashSet();

    public class_2487 method_75(class_2487 nbt, class_7225.class_7874 registryLookup) {
        Function<Map.Entry, class_2487> serializer = stringBuildEventEntry -> {
            boolean isBreakEvent;
            String eventName = (String)stringBuildEventEntry.getKey();
            BuildEvent event = (BuildEvent)stringBuildEventEntry.getValue();
            class_2487 eventNbt = new class_2487();
            eventNbt.method_10582("name", eventName);
            if (event.world() != null) {
                eventNbt.method_10582("dimension", event.world().method_27983().method_29177().toString());
            }
            class_238 box = event.box();
            class_2520 from = (class_2520)class_2338.field_25064.encodeStart((DynamicOps)class_2509.field_11560, (Object)new class_2338((int)box.field_1323, (int)box.field_1322, (int)box.field_1321)).getOrThrow();
            class_2520 to = (class_2520)class_2338.field_25064.encodeStart((DynamicOps)class_2509.field_11560, (Object)new class_2338((int)box.field_1320, (int)box.field_1325, (int)box.field_1324)).getOrThrow();
            eventNbt.method_10566("from", from);
            eventNbt.method_10566("to", to);
            boolean isPlaceEvent = event.placeObjective() != null;
            boolean bl = isBreakEvent = event.breakObjective() != null;
            String type = isPlaceEvent && isBreakEvent ? "both" : (isPlaceEvent ? "place" : "break");
            eventNbt.method_10582("type", type);
            if (event.predicate() != null) {
                eventNbt.method_10582("predicate", event.predicate().toString());
            }
            if (event.total()) {
                eventNbt.method_10556("total", true);
            }
            return eventNbt;
        };
        class_2499 activeEvents = new class_2499();
        activeEvents.addAll(this.buildEvents.activeEvents.entrySet().stream().map(serializer).toList());
        nbt.method_10566("active_events", (class_2520)activeEvents);
        class_2499 pausedEvents = new class_2499();
        pausedEvents.addAll(this.buildEvents.pausedEvents.entrySet().stream().map(serializer).toList());
        nbt.method_10566("paused_events", (class_2520)pausedEvents);
        nbt.method_10569("build_events_version", 3);
        return nbt;
    }

    public static BuildEventsState readNbt(class_2487 nbt, MinecraftServer server) {
        int version = nbt.method_10550("build_events_version");
        BiConsumer<class_2499, Map> deserializer = (nbtList, map) -> {
            for (class_2520 element : nbtList) {
                class_3218 world;
                if (!(element instanceof class_2487)) continue;
                class_2487 eventNbt = (class_2487)element;
                String eventName = eventNbt.method_10558("name");
                String dimension = eventNbt.method_10558("dimension");
                class_2338 from = (class_2338)class_2338.field_25064.decode((DynamicOps)class_2509.field_11560, (Object)eventNbt.method_10580("from")).map(Pair::getFirst).getOrThrow();
                class_2338 to = (class_2338)class_2338.field_25064.decode((DynamicOps)class_2509.field_11560, (Object)eventNbt.method_10580("to")).map(Pair::getFirst).getOrThrow();
                String type = eventNbt.method_10558("type");
                String predicate = eventNbt.method_10558("predicate");
                class_2960 predicateId = predicate.isEmpty() ? null : class_2960.method_12829((String)predicate);
                boolean total = eventNbt.method_10577("total");
                if (dimension.isEmpty()) {
                    world = null;
                } else {
                    world = server.method_3847(class_5321.method_29179((class_5321)class_7924.field_41223, (class_2960)class_2960.method_12829((String)dimension)));
                    if (world == null) continue;
                }
                map.put(eventName, BuildEvent.createBuildEvent(eventName, world, from, to, type, predicateId, total));
                if (version != 2 || !total) continue;
                BuildEvent event = (BuildEvent)map.get(eventName);
                BuildEventsState.updateTotal((class_269)server.method_3845(), event.placeObjective());
                BuildEventsState.updateTotal((class_269)server.method_3845(), event.breakObjective());
            }
        };
        BuildEventsState buildEventsState = new BuildEventsState();
        class_2499 activeEventsList = nbt.method_10554(version >= 1 ? "active_events" : "build_events", 10);
        class_2499 pausedEventsList = nbt.method_10554("paused_events", 10);
        deserializer.accept(activeEventsList, buildEventsState.buildEvents.activeEvents);
        for (BuildEvent event : buildEventsState.buildEvents.activeEvents.values()) {
            if (event.placeObjective() != null) {
                buildEventsState.placeEvents.add(event);
            }
            if (event.breakObjective() == null) continue;
            buildEventsState.breakEvents.add(event);
        }
        deserializer.accept(pausedEventsList, buildEventsState.buildEvents.pausedEvents);
        return buildEventsState;
    }

    public static BuildEventsState loadBuildEvents(MinecraftServer server) {
        class_26 stateManager = server.method_30002().method_17983();
        return (BuildEventsState)stateManager.method_17924(new class_18.class_8645(BuildEventsState::new, (compound, registryLookup) -> BuildEventsState.readNbt(compound, server), null), "buildevents");
    }

    private static void updateTotal(class_269 scoreboard, class_266 objective) {
        if (objective == null) {
            return;
        }
        class_9015 oldTotal = class_9015.method_55422((String)(String.valueOf(class_124.field_1067) + "Total"));
        class_9013 oldScore = scoreboard.method_55430(oldTotal, objective);
        if (oldScore == null) {
            return;
        }
        scoreboard.method_1155(oldTotal, objective);
        class_9014 newScore = scoreboard.method_1180(BuildEventsMod.TOTAL, objective);
        newScore.method_55410(oldScore.method_55397());
        newScore.method_55411(BuildEventsMod.TOTAL.method_5476());
    }
}

