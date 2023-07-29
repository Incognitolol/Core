package rip.alpha.core.bukkit.modsuite.requests;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import rip.alpha.core.bukkit.Core;
import rip.alpha.core.bukkit.modsuite.ModerationLocation;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.util.message.MessageBuilder;

public class RequestBridgeListener {

    public static void onRequest(PlayerRequestHelpEvent event) {
        broadcast(event.request());
    }

    private static void broadcast(Request request) {
        ModerationLocation loc = request.requestLocation();
        String line = "§r§7§m------------------------------------";
        String text = MessageBuilder.standard("{}\n{} asks for help.\n{}\n- Server: {}\n- Location: {}\n- Reason: {}")
                .element(line)
                .element(request.requesterName())
                .element(line)
                .element(request.serverID())
                .element(loc)
                .element(request.requestMessage())
                .build();

        Core.LOGGER.log(text, LogLevel.EXTENDED);

        String currentServer = NetworkServerHandler.getInstance().getCurrentServer().getServerId();
        BaseComponent[] message;
        if (currentServer.equals(request.serverID())) {
            BaseComponent[] hover = new ComponentBuilder("§7[Click to tp]").create();
            message = new ComponentBuilder(text)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + request.requesterName()))
                    .create();
        } else {
            BaseComponent[] hover = new ComponentBuilder("§7[Not on the same server]").create();
            message = new ComponentBuilder(text)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                    .create();
        }
        Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> player.hasPermission("core.requests.broadcast"))
                .forEach(player -> {
                    player.spigot().sendMessage(message);
                    if (AlphaProfileManager.profiles().isLocallyCached(player.getUniqueId())) {
                        AlphaProfile profile = AlphaProfileManager.profiles().getCachedValue(player.getUniqueId());
                        if (profile.getProfileSettings().isNotifyOnModerationBroadcast()) {
                            player.playSound(player.getEyeLocation(), Sound.NOTE_BASS, 0.5F, 0.5F);
                        }
                    }
                });
    }

}
