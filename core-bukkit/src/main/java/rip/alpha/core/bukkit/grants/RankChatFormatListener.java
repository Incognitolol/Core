package rip.alpha.core.bukkit.grants;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.util.message.MessageColor;

public class RankChatFormatListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        AlphaProfile snapshot = AlphaProfileManager.profiles().getCachedValue(event.getPlayer().getUniqueId());
        Rank rank = snapshot.getHighestRank();
        event.setFormat(rank.getPrefix() + rank.getColor() + "%s" + MessageColor.RESET + ": %s");
    }
}
