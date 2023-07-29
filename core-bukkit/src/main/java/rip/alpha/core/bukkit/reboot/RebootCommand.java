package rip.alpha.core.bukkit.reboot;

import org.bukkit.command.CommandSender;
import rip.alpha.core.shared.reboot.RebootHandler;
import rip.alpha.core.shared.reboot.RebootTask;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.time.Duration;

public class RebootCommand {

    @CommandUsage("<duration|1h10s>")
    @Command(names = {"reboot", "shutdown"}, permission = "core.command.reboot", async = true)
    public static void rebootCommand(CommandSender sender, Duration duration) {
        RebootTask rebootTask = RebootHandler.getInstance().getRebootTask();

        if (rebootTask != null) {
            String message = MessageBuilder
                    .error("There is currently already a reboot task scheduled with {} remaining.")
                    .element(TimeUtil.formatIntoMMSS(rebootTask.getSeconds()))
                    .build();
            sender.sendMessage(message);
            return;
        }

        RebootHandler.getInstance().startReboot((int) duration.toSeconds());
    }

    @Command(names = {"reboot cancel", "shutdown cancel"}, permission = "core.command.reboot", async = true)
    public static void rebootCancel(CommandSender sender) {
        RebootTask rebootTask = RebootHandler.getInstance().getRebootTask();

        if (rebootTask == null) {
            sender.sendMessage(MessageBuilder.constructError("There is currently no reboot task scheduled"));
            return;
        }

        RebootHandler.getInstance().cancelRebootTask();
        String message = MessageBuilder
                .standard("You have successfully cancelled a reboot that had {} to go.")
                .element(TimeUtil.formatIntoMMSS(rebootTask.getSeconds()))
                .build();
        sender.sendMessage(message);
    }

}
