package rip.alpha.core.bukkit.totp;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.libraries.util.message.MessageBuilder;

public class TotpListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!TotpHandler.getInstance().isEnabled()) {
            return;
        }

        if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
            Player player = event.getPlayer();
            AlphaProfile profile = AlphaProfileManager.profiles().getCachedValue(player.getUniqueId());

            if (profile.getTotpSecret() == null) {
                return;
            }

            if (profile.getLastAuthenticatedIp() == null || profile.getLastActiveIp() == null) {
                return;
            }

            if (!profile.getLastActiveIp().equals(profile.getLastAuthenticatedIp())) {
                return;
            }

            TotpAuthenticatedCache.getInstance().add(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!TotpHandler.getInstance().isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        if (TotpAuthenticatedCache.getInstance().exists(player)) {
            return;
        }
        if (!player.hasPermission("core.command.2fa")) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(MessageBuilder.constructError("You cannot interact with the world until you are authenticated"));
        player.sendMessage(MessageBuilder.constructError("Please use /2fa <code>"));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!TotpHandler.getInstance().isEnabled()) {
            return;
        }
        if (event.getMessage().startsWith("/2fa") || event.getMessage().startsWith("/setup2fa")) {
            return;
        }
        Player player = event.getPlayer();
        if (TotpAuthenticatedCache.getInstance().exists(player)) {
            return;
        }
        if (!player.hasPermission("core.command.2fa")) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(MessageBuilder.constructError("You cannot preform commands until you are authenticated"));
        player.sendMessage(MessageBuilder.constructError("Please use /2fa <code>"));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (!TotpHandler.getInstance().isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        if (TotpAuthenticatedCache.getInstance().exists(player)) {
            return;
        }
        if (!player.hasPermission("core.command.2fa")) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(MessageBuilder.constructError("You cannot talk in chat until you are authenticated"));
        player.sendMessage(MessageBuilder.constructError("Please use /2fa <code>"));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!TotpHandler.getInstance().isEnabled()) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockZ() == to.getBlockZ() && from.getBlockX() == to.getBlockX()) {
            return;
        }
        Player player = event.getPlayer();
        if (TotpAuthenticatedCache.getInstance().exists(player)) {
            return;
        }
        if (!player.hasPermission("core.command.2fa")) {
            return;
        }
        event.setTo(event.getFrom());
        player.sendMessage(MessageBuilder.constructError("You cannot move in the world until you are authenticated"));
        player.sendMessage(MessageBuilder.constructError("Please use /2fa <code>"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!TotpHandler.getInstance().isEnabled()) {
            return;
        }
        TotpAuthenticatedCache.getInstance().remove(event.getPlayer().getUniqueId());
    }
}
