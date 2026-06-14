/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_176
 *  net.minecraft.class_176$class_177
 *  net.minecraft.class_1799
 *  net.minecraft.class_181
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_266
 *  net.minecraft.class_274
 *  net.minecraft.class_274$class_275
 *  net.minecraft.class_2960
 *  net.minecraft.class_2995
 *  net.minecraft.class_3218
 *  net.minecraft.class_47
 *  net.minecraft.class_47$class_48
 *  net.minecraft.class_5321
 *  net.minecraft.class_5341
 *  net.minecraft.class_6880
 *  net.minecraft.class_7924
 *  net.minecraft.class_8567
 *  net.minecraft.class_8567$class_8568
 *  org.jetbrains.annotations.Nullable
 */
package velizarbg.buildevents.data;

import java.util.Optional;
import net.minecraft.class_1657;
import net.minecraft.class_176;
import net.minecraft.class_1799;
import net.minecraft.class_181;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_274;
import net.minecraft.class_2960;
import net.minecraft.class_2995;
import net.minecraft.class_3218;
import net.minecraft.class_47;
import net.minecraft.class_5321;
import net.minecraft.class_5341;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import net.minecraft.class_8567;
import org.jetbrains.annotations.Nullable;
import velizarbg.buildevents.BuildEventsMod;

public record BuildEvent(@Nullable class_3218 world, class_238 box, @Nullable class_266 placeObjective, @Nullable class_266 breakObjective, @Nullable class_2960 predicate, boolean total) {
    public static final class_176 BUILD_EVENT_ACTION = new class_176.class_177().method_781(class_181.field_24424).method_781(class_181.field_1226).method_781(class_181.field_1229).method_782();

    public BuildEvent(@Nullable class_3218 world, class_2338 from, class_2338 to, @Nullable class_266 placeObjective, @Nullable class_266 breakObjective, @Nullable class_2960 predicate, boolean total) {
        this(world, new class_238(from.method_10263(), from.method_10264(), from.method_10260(), to.method_10263(), to.method_10264(), to.method_10260()){

            public boolean method_1008(double x, double y, double z) {
                return x >= this.field_1323 && x <= this.field_1320 && y >= this.field_1322 && y <= this.field_1325 && z >= this.field_1321 && z <= this.field_1324;
            }
        }, placeObjective, breakObjective, predicate, total);
    }

    public BuildEvent withPredicate(class_2960 predicate) {
        return new BuildEvent(this.world, this.box, this.placeObjective, this.breakObjective, predicate, this.total);
    }

    public BuildEvent withWorld(@Nullable class_3218 world) {
        return new BuildEvent(world, this.box, this.placeObjective, this.breakObjective, this.predicate, this.total);
    }

    public BuildEvent withTotal(boolean total) {
        return new BuildEvent(this.world, this.box, this.placeObjective, this.breakObjective, this.predicate, total);
    }

    public boolean testPredicate(class_1937 world, class_1657 player, class_2338 pos, class_1799 stack) {
        class_5321 registryKey = class_5321.method_29179((class_5321)class_7924.field_50081, (class_2960)this.predicate);
        class_5341 predicate = BuildEventsMod.server.method_58576().method_58294().method_58561(class_7924.field_50081, registryKey).map(class_6880::comp_349).orElse(null);
        if (predicate == null) {
            return false;
        }
        class_8567 lootContextParameterSet = new class_8567.class_8568((class_3218)world).method_51874(class_181.field_24424, (Object)class_243.method_24954((class_2382)pos)).method_51874(class_181.field_1226, (Object)player).method_51874(class_181.field_1229, (Object)stack).method_51875(BUILD_EVENT_ACTION);
        class_47 lootContext = new class_47.class_48(lootContextParameterSet).method_309(Optional.empty());
        lootContext.method_298(class_47.method_51187((class_5341)predicate));
        return predicate.test((Object)lootContext);
    }

    public static BuildEvent createBuildEvent(String eventName, class_3218 world, class_2338 from, class_2338 to, String type, class_2960 predicate, boolean total) {
        String objectiveName;
        class_266 placeObjective = null;
        class_266 breakObjective = null;
        class_2995 scoreboard = BuildEventsMod.server.method_3845();
        if (type.equals("both") || type.equals("place")) {
            objectiveName = eventName + "_place";
            placeObjective = BuildEvent.getOrCreateObjective(scoreboard, objectiveName);
        }
        if (type.equals("both") || type.equals("break")) {
            objectiveName = eventName + "_break";
            breakObjective = BuildEvent.getOrCreateObjective(scoreboard, objectiveName);
        }
        return new BuildEvent(world, from, to, placeObjective, breakObjective, predicate, total);
    }

    private static class_266 getOrCreateObjective(class_2995 scoreboard, String objective) {
        return Optional.ofNullable(scoreboard.method_1170(objective)).orElseGet(() -> scoreboard.method_1168(objective, class_274.field_1468, (class_2561)class_2561.method_43470((String)objective), class_274.class_275.field_1472, false, null));
    }
}

