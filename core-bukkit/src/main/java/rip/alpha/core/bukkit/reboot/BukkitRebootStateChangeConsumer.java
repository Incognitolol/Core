package rip.alpha.core.bukkit.reboot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.Core;
import rip.alpha.core.shared.reboot.RebootStateChangeConsumer;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageTranslator;
import rip.alpha.libraries.util.task.TaskUtil;

public class BukkitRebootStateChangeConsumer implements RebootStateChangeConsumer {

    private static final String line = MessageTranslator.translate("&4&m---------------------------------");
    private static final int secondsForKick = 5;

    @Override
    public void onStateChange(int currentTime) {
        if (currentTime == secondsForKick) {
            Core.LOGGER.log("Sending everyone to hub...", LogLevel.BASIC);
            TaskUtil.runSync(() -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.kickPlayer(ChatColor.RED + "The server was shutdown...");
                }
            });
        }

        if (currentTime > 0 && (currentTime <= 10 || (currentTime <= 60 && currentTime % 5 == 0) || (currentTime % 30 == 0))) {
            String message = MessageBuilder
                    .error("Server is rebooting in {}.")
                    .prefix("⚠")
                    .element(TimeUtil.formatIntoMMSS(currentTime))
                    .build();
            Bukkit.broadcastMessage(line);
            Bukkit.broadcastMessage(message);
            Bukkit.broadcastMessage(line);
        }

        if (currentTime <= 0) {
            TaskUtil.runSync(Bukkit::shutdown);
        }
    }
}
