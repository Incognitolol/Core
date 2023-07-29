package rip.alpha.core.bukkit.essentials.backtrack;

import lombok.Getter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationTrack {

    @Getter
    private final int size = 5;
    private final ArrayList<Location> trackedLocations = new ArrayList<>();

    public void addTrack(Location location) {
        while (this.trackedLocations.size() > this.size) {
            this.trackedLocations.remove(0);
        }
        this.trackedLocations.add(location);
    }

    public List<Location> getAllLocations() {
        return List.copyOf(this.trackedLocations);
    }

    public Location getTrackedLocation(int index) {
        if (this.trackedLocations.isEmpty()) {
            return null;
        }
        index = Math.max(0, Math.min(this.trackedLocations.size() - 1, index));
        return this.trackedLocations.get(this.size - index - 1);
    }
}
