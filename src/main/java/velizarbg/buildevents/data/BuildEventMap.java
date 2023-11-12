package velizarbg.buildevents.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BuildEventMap {
	public final Map<String, BuildEvent> activeEvents = new HashMap<>();
	public final Map<String, BuildEvent> pausedEvents = new HashMap<>();

	public int size() {
		return activeEvents.size() + pausedEvents.size();
	}

	public boolean containsKey(String key) {
		return activeEvents.containsKey(key) || pausedEvents.containsKey(key);
	}

	public BuildEvent get(String key) {
		return Optional.ofNullable(activeEvents.get(key)).orElseGet(() -> pausedEvents.get(key));
	}

	public BuildEvent remove(String key) {
		return Optional.ofNullable(activeEvents.remove(key)).orElseGet(() -> pausedEvents.remove(key));
	}

	public Set<String> keySet() {
		Set<String> combinedSet = new HashSet<>(activeEvents.keySet());
		combinedSet.addAll(pausedEvents.keySet());
		return combinedSet;
	}
}
