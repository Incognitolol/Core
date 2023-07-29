package rip.alpha.core.bukkit.reboot;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import rip.alpha.core.shared.reboot.RebootHandler;
import rip.alpha.core.shared.reboot.RebootTask;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.core.shared.server.NetworkServerType;

public class RebootListener implements Listener {

    private static final String DENY_MESSAGE = ChatColor.RED + "Please use /shutdown";
    private static final int MIN_SECS_TO_BLOCK = 30;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        RebootTask rebootTask = RebootHandler.getInstance().getRebootTask();

        if (rebootTask == null) {
            return;
        }

        if (rebootTask.getSeconds() > MIN_SECS_TO_BLOCK) {
            return;
        }

        String message = ChatColor.RED + "Server is about to restart, please join back in a minute";
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPreProcessCommand(PlayerCommandPreprocessEvent event) {
        if (NetworkServerHandler.getInstance().getCurrentServer().getServerType() == NetworkServerType.DEVELOPMENT) {
            return;
        }
        Player player = event.getPlayer();
        if (player.hasPermission("core.command.reboot") && event.getMessage().startsWith("/stop")) {
            event.setCancelled(true);
            player.sendMessage(DENY_MESSAGE);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        if (NetworkServerHandler.getInstance().getCurrentServer().getServerType() == NetworkServerType.DEVELOPMENT) {
            return;
        }

        if (event.getCommand().toLowerCase().startsWith("stop")) {
            event.setCommand("");
            event.getSender().sendMessage(DENY_MESSAGE);
        }
    }
}
