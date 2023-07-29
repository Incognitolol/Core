package rip.alpha.core.bukkit.essentials;

import org.bukkit.command.CommandSender;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SystemTimeCommand {

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @Command(names = {"systemtime", "systime"}, async = true, permission = "core.command.systemtime")
    public static void systemTimeCommand(CommandSender commandSender) {
        String time = DATE_TIME_FORMATTER.format(LocalDateTime.now());
        commandSender.sendMessage(MessageBuilder.construct("The system time is currently {}.", time));
    }

}
