package rip.alpha.core.discord.command.commands.developer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import rip.alpha.core.discord.command.GenericCommand;
import rip.alpha.core.discord.command.util.CommandContext;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

/**
 * @author Moose1301
 * @date 4/11/2022
 */
public class TimeTrackerCommand extends GenericCommand {
    public TimeTrackerCommand() {
        super("timetracker", "A Used to spawn the time tracker", false, true, true);
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.getChannel().sendMessage(genBuilder()).queue();
    }
    public static String genBuilder() {
        Calendar sydney = Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney"));
        Calendar est = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        Calendar cali = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        Calendar germany = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        Calendar chicago = Calendar.getInstance(TimeZone.getTimeZone("America/Chicago"));
        Calendar mountain = Calendar.getInstance(TimeZone.getTimeZone("America/Phoenix"));
        Calendar utc = toCalendar(OffsetDateTime.now(ZoneOffset.UTC));
        int sydneyHour = sydney.get(Calendar.HOUR_OF_DAY);
        int estHour = est.get(Calendar.HOUR_OF_DAY);
        int caliHour = cali.get(Calendar.HOUR_OF_DAY);
        int germanyHour = germany.get(Calendar.HOUR_OF_DAY);
        int chicagoHour = chicago.get(Calendar.HOUR_OF_DAY);
        int mountainHour = mountain.get(Calendar.HOUR_OF_DAY) + 1;
        int xemahHour = utc.get(Calendar.HOUR_OF_DAY) + 5;

        int sydneyMinute = sydney.get(Calendar.MINUTE);
        int estMinute = est.get(Calendar.MINUTE);
        int caliMinute = cali.get(Calendar.MINUTE);
        int germanyMinute = germany.get(Calendar.MINUTE);
        int chicagoMinute = chicago.get(Calendar.MINUTE);
        int mountainMinute = mountain.get(Calendar.MINUTE);
        int xemahMinute = utc.get(Calendar.MINUTE);

        String parsedSydneyHour = String.valueOf(sydneyHour % 12);
        String parsedEstHour = String.valueOf(estHour % 12);
        String parsedCaliHour = String.valueOf(caliHour % 12);
        String parsedGermanyHour = String.valueOf(germanyHour % 12);
        String parsedChicagoHour = String.valueOf(chicagoHour % 12);
        String parsedMountainHour = String.valueOf(mountainHour % 12);
        String parsedXemahHour = String.valueOf(xemahHour % 12);
        if (parsedSydneyHour.equalsIgnoreCase("0")) {
            parsedSydneyHour = "12";
        }
        if (parsedEstHour.equalsIgnoreCase("0")) {
            parsedEstHour = "12";
        }
        if (parsedCaliHour.equalsIgnoreCase("0")) {
            parsedCaliHour = "12";
        }
        if (parsedGermanyHour.equalsIgnoreCase("0")) {
            parsedGermanyHour = "12";
        }
        if (parsedChicagoHour.equalsIgnoreCase("0")) {
            parsedChicagoHour = "12";
        }
        if (parsedMountainHour.equalsIgnoreCase("0")) {
            parsedMountainHour = "12";
        }
        if (parsedXemahHour.equalsIgnoreCase("0")) {
            parsedXemahHour = "12";
        }
        String sydneyTime = (parsedSydneyHour) + ":" + (sydneyMinute < 10 ? "0" : "") + sydneyMinute + ((sydneyHour>=12) ? "PM" : "AM");
        String estTime = (parsedEstHour) + ":" + (estMinute < 10 ? "0" : "") + estMinute + ((estHour>=12) ? "PM" : "AM");
        String caliTime = (parsedCaliHour) + ":" + (caliMinute < 10 ? "0" : "") + caliMinute + ((caliHour>=12) ? "PM" : "AM");
        String germanyTime = (parsedGermanyHour) + ":" + (germanyMinute < 10 ? "0" : "") + germanyMinute + ((germanyHour>=12) ? "PM" : "AM");
        String chicagoTime = (parsedChicagoHour) + ":" + (chicagoMinute < 10 ? "0" : "") + chicagoMinute + ((chicagoHour>=12) ? "PM" : "AM");
        String mountainTime = (parsedMountainHour) + ":" + (mountainMinute < 10 ? "0" : "") + mountainMinute + ((mountainMinute>=12) ? "PM" : "AM");
        String xemahTime = (parsedXemahHour) + ":" + (xemahMinute < 10 ? "0" : "") + xemahMinute + ((xemahMinute>=12) ? "PM" : "AM");

        StringBuilder sb = new StringBuilder();
        sb.append("**Time Zones**\n");
        sb.append("EST (NA East): " + estTime + "\n");
        sb.append("Sydney (AU): " + sydneyTime + "\n");
        sb.append("Cali (NA West): " + caliTime + "\n");
        sb.append("Germany: " + germanyTime + "\n");
        sb.append("MDT: " + mountainTime + "\n");
        sb.append("CST (NA): " + chicagoTime + "\n");

        sb.append("**Owners** \n");
        sb.append("Tak: " + estTime + "\n");
        sb.append("Zach: " + caliTime + "\n");
        sb.append("Jay: " + estTime + "\n");
        sb.append("**Developers** \n");
        sb.append("TewPingz: " + sydneyTime + "\n");
        sb.append("OJ: " + sydneyTime + "\n");
        sb.append("Moose: " + estTime + "\n");
        sb.append("Incognito: " + estTime + "\n");
        sb.append("Flo: " + germanyTime + "\n");
        sb.append("Xemah: " + xemahTime + "\n");
        sb.append("**Management** \n");
        sb.append("SheepKiller: " + estTime + "\n");
        sb.append("airjuanz: " + estTime + "\n");
        sb.append("Limo: " + caliTime + "\n");
        sb.append("**Platform Admins** \n");
        sb.append("Kited: " + mountainTime + "\n");
        sb.append("**Head Admins**\n");
        sb.append("Grye: " + chicagoTime + "\n");
        sb.append("JackAttack: " + estTime + "\n");

        return sb.toString();
    }
    public static void update(Message message) {
        message.editMessage(genBuilder()).queue();
    }
    @Override
    public CommandData register(JDA jda) {
        return new CommandDataImpl(name, description);
    }
    public static Calendar toCalendar(OffsetDateTime offsetDateTime) {
        Objects.requireNonNull(offsetDateTime, "Offset date time is required");

        TimeZone timeZone = TimeZone.getTimeZone(offsetDateTime.toZonedDateTime().getZone());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(offsetDateTime.toInstant().toEpochMilli());
        calendar.setTimeZone(timeZone);
        return calendar;
    }
}
