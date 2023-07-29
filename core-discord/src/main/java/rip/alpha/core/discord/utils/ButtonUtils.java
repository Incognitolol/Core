package rip.alpha.core.discord.utils;

import net.dv8tion.jda.api.entities.ISnowflake;


/**
 * @author Moose1301
 * @date 4/13/2022
 */
public class ButtonUtils {
    public static final String PROTECTION_HEADER = ":BP:<SNOWFLAKE_ID>:BP:";

    /**
     * @param snowflake The snowflake implementation that you are adding the spoof protection for. (EG: Channel, Memeber)
     * @param id The button id your going to have (Must call getData when handling)
     * @return The button ID with the header
     */
    public static String getButtonId(ISnowflake snowflake, String id) {
        String header = PROTECTION_HEADER.replace("<SNOWFLAKE_ID>", snowflake.getId());
        if(id.contains(header)) {
            throw new RuntimeException("Button ID Already has the Protection Header? (ID: " + id + ")");
        }
        return header + id;
    }
    /**
     * @param snowflake The snowflake implementation that you are adding the spoof protection for. (EG: Channel, Memeber)
     * @param id The button id that your checking
     * @return The button ID with the header
     */
    public static boolean isValidSnowflake(ISnowflake snowflake, String id) {
        return id.contains(PROTECTION_HEADER.replace("<SNOWFLAKE_ID>", snowflake.getId()));
    }
    /**
     * @param snowflake The snowflake implementation that you are adding the spoof protection for. (EG: Channel, Memeber)
     * @param id The button id that getting the data of
     * @return The button id without the header. Returns null if {@see isValidSnowflake} returns false
     */
    public static String getData(ISnowflake snowflake, String id) {
        if(!isValidSnowflake(snowflake, id)) {
           return null;
        }
        return id.replace(PROTECTION_HEADER.replace("<SNOWFLAKE_ID>", snowflake.getId()), "");
    }
}
