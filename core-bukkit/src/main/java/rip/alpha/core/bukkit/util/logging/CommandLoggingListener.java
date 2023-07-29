package rip.alpha.core.bukkit.util.logging;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import rip.alpha.core.bukkit.Core;
import rip.alpha.libraries.logging.LogLevel;

public class CommandLoggingListener implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Core.LOGGER.log("%s issued command %s".formatted(event.getPlayer().getName(), event.getMessage()), LogLevel.EXTENDED);
    }

}
