/*
 * Decompiled with CFR 0.152.
 */
package velizarbg.buildevents.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import velizarbg.buildevents.data.BuildEvent;

public class BuildEventMap {
    public final Map<String, BuildEvent> activeEvents = new HashMap<String, BuildEvent>();
    public final Map<String, BuildEvent> pausedEvents = new HashMap<String, BuildEvent>();

    public int size() {
        return this.activeEvents.size() + this.pausedEvents.size();
    }

    public boolean containsKey(String key) {
        return this.activeEvents.containsKey(key) || this.pausedEvents.containsKey(key);
    }

    public BuildEvent get(String key) {
        return Optional.ofNullable(this.activeEvents.get(key)).orElseGet(() -> this.pausedEvents.get(key));
    }

    public BuildEvent remove(String key) {
        return Optional.ofNullable(this.activeEvents.remove(key)).orElseGet(() -> this.pausedEvents.remove(key));
    }

    public Set<String> keySet() {
        HashSet<String> combinedSet = new HashSet<String>(this.activeEvents.keySet());
        combinedSet.addAll(this.pausedEvents.keySet());
        return combinedSet;
    }

    public BuildEvent replace(String key, BuildEvent value) {
        return Optional.ofNullable(this.activeEvents.replace(key, value)).orElseGet(() -> this.pausedEvents.replace(key, value));
    }
}

