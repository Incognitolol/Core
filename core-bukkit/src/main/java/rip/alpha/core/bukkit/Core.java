package rip.alpha.core.bukkit;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import rip.alpha.core.bukkit.common.PlayerSessionListener;
import rip.alpha.core.bukkit.data.server.LocalServerDataManager;
import rip.alpha.core.bukkit.economy.EconomyCommands;
import rip.alpha.core.bukkit.economy.shop.ShopCommand;
import rip.alpha.core.bukkit.essentials.backtrack.BackTrackListener;
import rip.alpha.core.bukkit.grants.GrantCommands;
import rip.alpha.core.bukkit.grants.GrantMessageListener;
import rip.alpha.core.bukkit.grants.RankChatFormatListener;
import rip.alpha.core.bukkit.levels.LevelCommands;
import rip.alpha.core.bukkit.modsuite.alts.AltCommand;
import rip.alpha.core.bukkit.modsuite.chat.*;
import rip.alpha.core.bukkit.modsuite.modmode.ModModeCommand;
import rip.alpha.core.bukkit.modsuite.modmode.ModModeListener;
import rip.alpha.core.bukkit.modsuite.reports.PlayerReportEvent;
import rip.alpha.core.bukkit.modsuite.reports.ReportBridgeListener;
import rip.alpha.core.bukkit.modsuite.reports.ReportCommands;
import rip.alpha.core.bukkit.modsuite.requests.PlayerRequestHelpEvent;
import rip.alpha.core.bukkit.modsuite.requests.RequestBridgeListener;
import rip.alpha.core.bukkit.modsuite.requests.RequestsCommands;
import rip.alpha.core.bukkit.npc.NPC;
import rip.alpha.core.bukkit.npc.NPCCommand;
import rip.alpha.core.bukkit.npc.NPCContextResolver;
import rip.alpha.core.bukkit.npc.NPCManager;
import rip.alpha.core.bukkit.punishments.PunishmentEventListener;
import rip.alpha.core.bukkit.punishments.PunishmentMessageListener;
import rip.alpha.core.bukkit.queue.NetworkQueueCommands;
import rip.alpha.core.bukkit.queue.NetworkQueueRunnable;
import rip.alpha.core.bukkit.reboot.BukkitRebootStateChangeConsumer;
import rip.alpha.core.bukkit.reboot.RebootCommand;
import rip.alpha.core.bukkit.reboot.RebootListener;
import rip.alpha.core.bukkit.server.context.NetworkServerEntityContextResolver;
import rip.alpha.core.bukkit.server.context.NetworkServerSnapshotContextResolver;
import rip.alpha.core.bukkit.server.NetworkServerListener;
import rip.alpha.core.bukkit.totp.TotpAuthenticatedCache;
import rip.alpha.core.bukkit.totp.TotpCommand;
import rip.alpha.core.bukkit.totp.TotpListener;
import rip.alpha.core.bukkit.totp.TotpResetBridgeEvent;
import rip.alpha.core.bukkit.util.json.ItemStackSerializer;
import rip.alpha.core.bukkit.util.json.LocationSerializer;
import rip.alpha.core.bukkit.util.json.MathematicalExpressionSerializer;
import rip.alpha.core.bukkit.util.logging.CommandLoggingListener;
import rip.alpha.core.bukkit.util.math.MathematicalExpression;
import rip.alpha.core.bukkit.warps.WarpCommands;
import rip.alpha.core.shared.AlphaCore;
import rip.alpha.core.shared.grants.ProfileGrantAddEvent;
import rip.alpha.core.shared.grants.ProfileGrantRemoveEvent;
import rip.alpha.core.shared.punishments.ProfilePunishmentAddEvent;
import rip.alpha.core.shared.punishments.ProfilePunishmentRemoveEvent;
import rip.alpha.core.shared.reboot.RebootHandler;
import rip.alpha.core.shared.server.NetworkServer;
import rip.alpha.core.shared.server.NetworkServerEntity;
import rip.alpha.core.shared.server.event.*;
import rip.alpha.libraries.LibrariesPlugin;
import rip.alpha.libraries.command.CommandFramework;
import rip.alpha.libraries.json.GsonProvider;
import rip.alpha.libraries.logging.AlphaLogger;
import rip.alpha.libraries.logging.AlphaLoggerFactory;
import rip.alpha.libraries.logging.LogLevel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Core extends JavaPlugin {

    public static final AlphaLogger LOGGER = AlphaLoggerFactory.createLogger("Core");

    @Getter
    private static Core instance;

    private ScheduledFuture<?> queueRunnable;

    public Core() {
        instance = this;
        LogLevel logLevel = CoreConfig.getInstance().getLogLevel();
        AlphaLoggerFactory.setCurrentLogLevel(logLevel);
        LOGGER.log("LogLevel is set to %s".formatted(logLevel), LogLevel.BASIC);
        LOGGER.log("Registering serializers...", LogLevel.BASIC);
        this.registerSerializers();
    }

    @Override
    public void onEnable() {
        LOGGER.log("Enabling 'Libraries'...", LogLevel.BASIC);
        LibrariesPlugin.getInstance().enable(this);
        LOGGER.log("Enabling 'AlphaCore'...", LogLevel.BASIC);
        AlphaCore.enable();
        LOGGER.log("Loading local server data...", LogLevel.BASIC);
        LocalServerDataManager.loadCurrent();
        LOGGER.log("Registering commands...", LogLevel.BASIC);
        this.registerCommands();
        LOGGER.log("Registering event listeners...", LogLevel.BASIC);
        this.registerEventListeners();
        LOGGER.log("Registering bridge listeners...", LogLevel.BASIC);
        this.registerBridgeListeners();
        LOGGER.log("Registering NPCs...", LogLevel.BASIC);
        NPCManager.getInstance().registerAllNPCs();
        LOGGER.log("Registering queue task...", LogLevel.BASIC);
        this.queueRunnable = Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(new NetworkQueueRunnable(), 500L, 500L, TimeUnit.MILLISECONDS);
        LOGGER.log("Core is fully loaded.", LogLevel.BASIC);
    }

    @Override
    public void onDisable() {
        if (this.queueRunnable != null) {
            LOGGER.log("Disabling 'queue runnable'...", LogLevel.BASIC);
            this.queueRunnable.cancel(true);
        }
        NPCManager.getInstance().saveAllNPCs();
        LOGGER.log("Unloading local server data...", LogLevel.BASIC);
        LocalServerDataManager.unLoadCurrent();
        LOGGER.log("Disabling 'AlphaCore'...", LogLevel.BASIC);
        AlphaCore.disable();
        LOGGER.log("Disabling 'Libraries'...", LogLevel.BASIC);
        LibrariesPlugin.getInstance().disable();
    }

    private void registerCommands() {
        CommandFramework commandFramework = LibrariesPlugin.getCommandFramework();
        commandFramework.registerContextResolver(new NetworkServerSnapshotContextResolver(), NetworkServer.NetworkServerSnapshot.class);
        commandFramework.registerContextResolver(new NetworkServerEntityContextResolver(), NetworkServerEntity.class);
        commandFramework.registerContextResolver(new NPCContextResolver(), NPC.class);
        commandFramework.registerPackage("rip.alpha.core.bukkit.server.command");
        commandFramework.registerPackage("rip.alpha.core.bukkit.essentials");
        commandFramework.registerPackage("rip.alpha.core.bukkit.punishments.commands");
        commandFramework.registerClass(GrantCommands.class);
        commandFramework.registerClass(WarpCommands.class);
        commandFramework.registerClass(LevelCommands.class);
        commandFramework.registerClass(ChatCommands.class);
        commandFramework.registerClass(AltCommand.class);
        commandFramework.registerClass(RequestsCommands.class);
        commandFramework.registerClass(ReportCommands.class);
        commandFramework.registerClass(RebootCommand.class);
        commandFramework.registerClass(NPCCommand.class);
        commandFramework.registerClass(TotpCommand.class);
        commandFramework.registerClass(ModModeCommand.class);
        commandFramework.registerClass(EconomyCommands.class);
        commandFramework.registerClass(NetworkQueueCommands.class);
        commandFramework.registerClass(ShopCommand.class);
    }

    private void registerSerializers() {
        GsonProvider.registerTypeHierarchyAdapter(ItemStack.class, new ItemStackSerializer());
        GsonProvider.registerTypeAdapter(MathematicalExpression.class, new MathematicalExpressionSerializer());
        GsonProvider.registerTypeAdapter(Location.class, new LocationSerializer());
    }

    private void registerEventListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerSessionListener(), this);
        pluginManager.registerEvents(new PunishmentEventListener(), this);
        pluginManager.registerEvents(new NetworkServerListener(), this);
        pluginManager.registerEvents(new BackTrackListener(), this);
        pluginManager.registerEvents(new RebootListener(), this);
        pluginManager.registerEvents(new TotpListener(), this);
        pluginManager.registerEvents(new ModModeListener(), this);
        pluginManager.registerEvents(new ChatListener(), this);
        pluginManager.registerEvents(new RankChatFormatListener(), this);
        pluginManager.registerEvents(new CommandLoggingListener(), this);
        RebootHandler.getInstance().registerListener(new BukkitRebootStateChangeConsumer());
    }

    private void registerBridgeListeners() {
        AlphaCore.registerListener(ProfileGrantAddEvent.class, GrantMessageListener::onGrantGained);
        AlphaCore.registerListener(ProfileGrantRemoveEvent.class, GrantMessageListener::onGrantLost);
        AlphaCore.registerListener(NetworkServerAddEvent.class, NetworkServerListener::handleAddServer);
        AlphaCore.registerListener(NetworkServerRemoveEvent.class, NetworkServerListener::handleRemoveServer);
        AlphaCore.registerListener(NetworkServerREShutdownEvent.class, NetworkServerListener::handleREShutdown);
        AlphaCore.registerListener(NetworkServerRECommandEvent.class, NetworkServerListener::handleRECommand);
        AlphaCore.registerListener(NetworkServerREBroadcastEvent.class, NetworkServerListener::handleREBroadcast);
        AlphaCore.registerListener(ProfilePunishmentAddEvent.class, PunishmentMessageListener::onPunishmentAdd);
        AlphaCore.registerListener(ProfilePunishmentRemoveEvent.class, PunishmentMessageListener::onPunishmentRemoved);
        AlphaCore.registerListener(PlayerRequestHelpEvent.class, RequestBridgeListener::onRequest);
        AlphaCore.registerListener(PlayerReportEvent.class, ReportBridgeListener::onReport);
        AlphaCore.registerListener(StaffChatEvent.class, InternalChatBridgeListeners::onStaffChat);
        AlphaCore.registerListener(AdminChatEvent.class, InternalChatBridgeListeners::onAdminChat);
        AlphaCore.registerListener(TotpResetBridgeEvent.class, TotpAuthenticatedCache::handleTotpResetBridge);
    }
}
