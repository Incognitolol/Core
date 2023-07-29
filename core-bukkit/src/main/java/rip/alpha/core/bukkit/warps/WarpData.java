package rip.alpha.core.bukkit.warps;

import rip.alpha.core.bukkit.Core;
import rip.alpha.libraries.logging.LogLevel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarpData {

    private final Map<String, Warp> warpMap = new HashMap<>();

    public List<Warp> getAllWarps() {
        return List.copyOf(this.warpMap.values());
    }

    public boolean containsName(String name) {
        return this.warpMap.containsKey(name);
    }

    public Warp getWarp(String warpName) {
        return this.warpMap.get(warpName);
    }

    public void addWarp(Warp warp) {
        Core.LOGGER.log("Adding warp %s @%s".formatted(warp.name(), warp.location()), LogLevel.BASIC);
        this.warpMap.put(warp.name(), warp);
    }

    public void removeWarp(String warpName) {
        Warp warp = this.warpMap.remove(warpName);
        if (warp != null) {
            Core.LOGGER.log("Removing warp %s @%s".formatted(warp.name(), warp.location()), LogLevel.BASIC);
        }
    }

}
