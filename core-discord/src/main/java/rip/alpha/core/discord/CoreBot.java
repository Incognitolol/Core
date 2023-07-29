package rip.alpha.core.discord;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import rip.alpha.core.discord.bridge.DiscordListener;
import rip.alpha.core.discord.bridge.GrantMessageListener;
import rip.alpha.core.discord.bridge.PunishmentMessageListener;
import rip.alpha.core.discord.command.CommandManager;
import rip.alpha.core.discord.command.commands.developer.TimeTrackerCommand;
import rip.alpha.core.discord.reaction.ReactionHandler;
import rip.alpha.core.discord.sync.SyncRankHandler;
import rip.alpha.core.discord.utils.CoreLangauage;
import rip.alpha.core.shared.AlphaCore;
import rip.alpha.core.shared.discord.DiscordLogEvent;
import rip.alpha.core.shared.grants.ProfileGrantAddEvent;
import rip.alpha.core.shared.grants.ProfileGrantRemoveEvent;
import rip.alpha.core.shared.punishments.ProfilePunishmentAddEvent;
import rip.alpha.core.shared.punishments.ProfilePunishmentRemoveEvent;
import rip.alpha.libraries.Libraries;
import rip.alpha.libraries.logging.AlphaLogger;
import rip.alpha.libraries.logging.AlphaLoggerFactory;
import rip.alpha.libraries.logging.LogLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Moose1301
 * @date 4/9/2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoreBot {
    @Getter
    private static final CoreBot instance = new CoreBot();
    public static final AlphaLogger LOGGER = AlphaLoggerFactory.createLogger("CoreBot");
    public static final ScheduledExecutorService EXECUTOR =  Executors.newScheduledThreadPool(1);
    public static List<String> DEVELOPERS;
    @Getter private JDA jda;
    @Getter private static CommandManager commandManager;

    @Getter private SyncRankHandler syncRankHandler;
    @Getter private ReactionHandler reactionHandler;

    public void onEnable() {
        Libraries.getInstance().enable();
        AlphaCore.enable();
        LogLevel logLevel = CoreConfig.getInstance().getLogLevel();
        AlphaLoggerFactory.setCurrentLogLevel(logLevel);
        LOGGER.log("LogLevel is set to %s".formatted(logLevel), LogLevel.BASIC);
        LOGGER.log("Enabling 'Libraries'...", LogLevel.BASIC);

        try {
            jda = JDABuilder.createDefault(CoreConfig.getInstance().getDiscordConfig().getToken())
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_EMOJIS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.EMOTE)
                    .build();
            jda.awaitReady();
            jda.addEventListener(commandManager = new CommandManager(jda));
            for (Guild guild : jda.getGuilds()) {
                if(!CoreConfig.getInstance().getDiscordConfig().getAllowedGuilds().contains(guild.getId())) {
                    LOGGER.log("Leaving Discord with name: " + guild.getName() + " as it is not whitelisted", LogLevel.DEBUG);
                    guild.leave().queue();
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.severe("Error While Starting the bot: " + ex.getMessage(), LogLevel.BASIC);
            return;
        }

        LOGGER.log("Successfully login in as " + jda.getSelfUser().getName() + "#" + jda.getSelfUser().getDiscriminator(), LogLevel.BASIC);
        LOGGER.log("Registering bridge listeners...", LogLevel.BASIC);
        registerBridgeListeners();
        this.syncRankHandler = new SyncRankHandler();
        this.reactionHandler = new ReactionHandler(jda);
        LOGGER.log("Core is fully loaded.", LogLevel.BASIC);
        Runtime.getRuntime().addShutdownHook(new Thread(this::onDisable));
        EXECUTOR.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Guild guild = jda.getGuildById("962146276525350983");
                Message message = guild.getTextChannelById("962432157635207219").retrieveMessageById("962437664454344817").complete();
                TimeTrackerCommand.update(message);
            }
        }, 0L, 1, TimeUnit.MINUTES);
        EXECUTOR.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                LOGGER.log("Saving All Data", LogLevel.DEBUG);
                save();
            }
        }, 60, 60, TimeUnit.MINUTES);
    }
    public void save() {
        reactionHandler.getReactionRoles().saveCacheSync();
    }
    public void onDisable() {
        save();
        AlphaCore.disable();
        Libraries.getInstance().disable();

    }
    private void registerBridgeListeners() {
        AlphaCore.registerListener(ProfileGrantAddEvent.class, GrantMessageListener::onGrantGained);
        AlphaCore.registerListener(ProfileGrantRemoveEvent.class, GrantMessageListener::onGrantLost);
        AlphaCore.registerListener(ProfilePunishmentAddEvent.class, PunishmentMessageListener::onPunishmentAdd);
        AlphaCore.registerListener(ProfilePunishmentRemoveEvent.class, PunishmentMessageListener::onPunishmentRemoved);
        AlphaCore.registerListener(DiscordLogEvent.class, DiscordListener::onDiscordLog);
    }


    public static void main(String[] args) {
        CoreBot.getInstance().onEnable();
    }
    static {
        DEVELOPERS = new ArrayList<>();
        DEVELOPERS.add("368901169176903691");
        DEVELOPERS.add("897011337450885151");
    }
}
