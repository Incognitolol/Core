package rip.alpha.core.bukkit.modsuite.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import rip.alpha.core.bukkit.data.player.CorePlayerProfile;
import rip.alpha.core.bukkit.data.player.CorePlayerProfileManager;
import rip.alpha.libraries.util.message.MessageBuilder;

public class ChatListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (ChatManager.getInstance().isMuteChat() && !event.getPlayer().hasPermission("core.mutechat.bypass")) {
            event.getPlayer().sendMessage(MessageBuilder.constructError("The chat is currently muted."));
            event.setCancelled(true);
            return;
        }
        CorePlayerProfile profile = CorePlayerProfileManager.profiles().getData(event.getPlayer().getUniqueId());
        long now = System.currentTimeMillis();
        long delta = now - profile.getLastChatMessageTime();
        boolean slowed = delta < ChatManager.getInstance().getMillisForSlowChat();
        if (ChatManager.getInstance().getMillisForSlowChat() > 0 && slowed && !event.getPlayer().hasPermission("core.slowchat.bypass")) {
            event.getPlayer().sendMessage(MessageBuilder.constructError("You are talking too quickly."));
            event.setCancelled(true);
            return;
        }
        profile.setLastChatMessageTime(now);
    }
}
