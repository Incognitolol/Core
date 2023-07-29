package rip.alpha.core.bukkit.modsuite.modmode.event;

import org.bukkit.entity.Player;
import rip.alpha.libraries.event.PlayerEvent;

public class PlayerExitModModeEvent extends PlayerEvent {
    public PlayerExitModModeEvent(Player player) {
        super(player);
    }
}
