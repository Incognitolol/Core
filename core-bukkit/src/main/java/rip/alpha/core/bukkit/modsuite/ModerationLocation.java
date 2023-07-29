package rip.alpha.core.bukkit.modsuite;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public record ModerationLocation(int x, int y, int z, String worldName) {

    public static ModerationLocation of(Location location) {
        return new ModerationLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }

    public Location toBukkit() {
        return new Location(Bukkit.getWorld(this.worldName), this.x, this.y, this.z);
    }

    @Override
    public String toString() {
        return "[%d, %d, %d in %s]".formatted(this.x, this.y, this.z, this.worldName);
    }
}
