package rip.alpha.core.bukkit.modsuite.chat;

import org.bukkit.Bukkit;
import rip.alpha.core.bukkit.Core;
import rip.alpha.libraries.logging.LogLevel;

public class InternalChatBridgeListeners {
    public static void onStaffChat(StaffChatEvent event) {
        Core.LOGGER.log(event.getFormatted(), LogLevel.EXTENDED);
        Bukkit.broadcast(event.getFormatted(), "core.command.staffchat");
    }

    public static void onAdminChat(AdminChatEvent event) {
        Core.LOGGER.log(event.getFormatted(), LogLevel.EXTENDED);
        Bukkit.broadcast(event.getFormatted(), "core.command.adminchat");
    }
}
