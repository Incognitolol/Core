package rip.alpha.core.bukkit.punishments;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.punishments.Punishment;
import rip.alpha.libraries.util.message.MessageBuilder;

public class PunishmentEventListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        AlphaProfile snapshot = AlphaProfileManager.profiles().getCachedValue(event.getPlayer().getUniqueId());

        if (!snapshot.hasPunishment(Punishment.Type.SERVER_MUTE)) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(MessageBuilder.constructError("You are currently muted."));
    }
}
