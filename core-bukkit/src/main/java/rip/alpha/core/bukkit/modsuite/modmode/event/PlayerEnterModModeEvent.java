package rip.alpha.core.bukkit.modsuite.modmode.event;

import org.bukkit.entity.Player;
import rip.alpha.libraries.event.PlayerEvent;

public class PlayerEnterModModeEvent extends PlayerEvent {
    public PlayerEnterModModeEvent(Player player) {
        super(player);
    }
}
