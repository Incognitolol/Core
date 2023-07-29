package rip.alpha.core.bukkit.common;

import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import rip.alpha.core.bukkit.Core;
import rip.alpha.core.bukkit.data.player.CorePlayerProfileManager;
import rip.alpha.core.bukkit.grants.RankPermissionEvaluator;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.data.IpUuidCache;
import rip.alpha.core.shared.economy.EconomyManager;
import rip.alpha.core.shared.grants.Grant;
import rip.alpha.core.shared.grants.ProfileGrantRemoveEvent;
import rip.alpha.core.shared.punishments.ProfilePunishmentRemoveEvent;
import rip.alpha.core.shared.punishments.Punishment;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.core.shared.server.NetworkServerType;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.model.GlobalDataDomain;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.data.NameCache;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.util.UUID;

public class PlayerSessionListener implements Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        try {
            UUID playerID = event.getUniqueId();
            String hashedIp = DigestUtils.md5Hex(event.getAddress().getHostAddress());
            Core.LOGGER.log("Adding IP address %s to %s".formatted(hashedIp, playerID), LogLevel.EXTENDED);

            IpUuidCache.getInstance().addEntry(playerID, hashedIp);
            IpUuidCache.UUIDStash stash = IpUuidCache.getInstance().getIdsOf(hashedIp);

            Core.LOGGER.log("Fetched %d IPs for %s".formatted(stash.uuids().size(), playerID), LogLevel.EXTENDED);

            GlobalDataDomain<UUID, AlphaProfile> profiles = AlphaProfileManager.profiles();
            stash.uuids().forEach(mappedID -> profiles.applyToData(mappedID, profile -> {
                if (profile.getMojangID().equals(playerID)) {
                    profile.setLastActiveIp(hashedIp);
                }
                this.checkGrants(profile);
                this.checkPunishments(profile);
                this.checkBans(profile, event);
            }));

            if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                return;
            }

            int altSize = stash.uuids().size();
            if (altSize > 1) {
                String name = NameCache.getInstance().getName(playerID);
                String message = MessageBuilder.construct("{} is attempting to login with {} alts.", name, altSize);
                Bukkit.broadcast(message, "core.command.alts");
            }

            if (!profiles.isLocallyCached(event.getUniqueId())) {
                profiles.enableLocalCacheFor(event.getUniqueId());
            }

            if (!CorePlayerProfileManager.profiles().isDataLoaded(event.getUniqueId())) {
                CorePlayerProfileManager.profiles().loadDataSync(event.getUniqueId());
            }

            EconomyManager.enableLocalCacheFor(event.getUniqueId());
        } catch (Exception exception) {
            exception.printStackTrace();
            String message = "§6There was a problem while loading your data.\n§6Please contact the Team.";
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
            this.terminatePlayer(event.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        PlayerLoginEvent.Result result = event.getResult();
        boolean evaluatedPermissions = false;

        if (result == PlayerLoginEvent.Result.KICK_FULL || result == PlayerLoginEvent.Result.KICK_WHITELIST) {
            RankPermissionEvaluator.reEvaluatePermissionsCached(event.getPlayer());
            evaluatedPermissions = true;
            if (result == PlayerLoginEvent.Result.KICK_WHITELIST && event.getPlayer().hasPermission("core.bypass.whitelist")) {
                event.allow();
            } else if (result == PlayerLoginEvent.Result.KICK_FULL && event.getPlayer().hasPermission("core.bypass.full")) {
                event.allow();
            }
        }

        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            Core.LOGGER.log("Blocked %s from logging in: %s".formatted(event.getPlayer().getName(), event.getKickMessage()), LogLevel.VERBOSE);
            this.terminatePlayer(event.getPlayer().getUniqueId());
            return;
        }

        if (!evaluatedPermissions){
            RankPermissionEvaluator.reEvaluatePermissionsRealTime(event.getPlayer());
        }

        AlphaProfileManager.profiles().applyToDataAsync(event.getPlayer().getUniqueId(), profile -> {
            if (!profile.isJoinedBefore()) {
                profile.setFirstJoin(System.currentTimeMillis());
            }
            profile.setLastJoin(System.currentTimeMillis());
            profile.setLastSeenName(event.getPlayer().getName());
            profile.setLastVisitedServer(NetworkServerHandler.getInstance().getCurrentServer().getDisplayName());
            profile.setJoinedBefore(true);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.terminatePlayer(event.getPlayer().getUniqueId());
    }

    private void terminatePlayer(UUID playerId) {
        ScheduledProfileTaskTracker.terminate(playerId);
        RankPermissionEvaluator.terminate(playerId);

        if (CorePlayerProfileManager.profiles().isDataLoaded(playerId)) {
            CorePlayerProfileManager.profiles().unloadDataAsync(playerId);
        }

        if (AlphaProfileManager.profiles().isLocallyCached(playerId)) {
            AlphaProfileManager.profiles().disableLocalCacheFor(playerId);
        }

        EconomyManager.disableLocalCacheFor(playerId);
    }

    private void checkGrants(AlphaProfile profile) {
        for (Grant grant : profile.getAllGrants()) {
            if (grant.hasExpired()) {
                profile.removeGrant(grant, ProfileGrantRemoveEvent.Reason.RUN_OUT, "CONSOLE", "Expired");
                Core.LOGGER.log("Removed %s from %s on login.".formatted(grant, profile.getMojangID()), LogLevel.VERBOSE);
            } else {
                ScheduledProfileTaskTracker.addTask(profile.getMojangID(), grant);
            }
        }
    }

    private void checkPunishments(AlphaProfile profile) {
        for (Punishment punishment : profile.getAllActivePunishments()) {
            if (punishment.hasExpired()) {
                profile.removePunishment(punishment, ProfilePunishmentRemoveEvent.Reason.RUN_OUT, "CONSOLE", "Expired");
                Core.LOGGER.log("Removed %s from %s on login.".formatted(punishment, profile.getMojangID()), LogLevel.EXTENDED);
            } else {
                ScheduledProfileTaskTracker.addTask(profile.getMojangID(), punishment);
            }
        }
    }

    private void checkBans(AlphaProfile profile, AsyncPlayerPreLoginEvent event) {
        boolean isBlacklisted = profile.hasPunishment(Punishment.Type.BLACKLIST);
        boolean isBanned = profile.hasPunishment(Punishment.Type.SERVER_BAN);

        if (isBlacklisted || isBanned) {
            if (NetworkServerHandler.getInstance().getCurrentServer().getServerType() == NetworkServerType.BANNED_HUB) {
                return;
            }

            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
            Punishment punishment = profile.getActivePunishment(isBlacklisted ? Punishment.Type.BLACKLIST : Punishment.Type.SERVER_BAN);
            String reason = punishment.reason();
            String duration = null;

            if (!punishment.isInfinite()) {
                int seconds = (int) Math.floor(punishment.getTimeLeft() / 1000D);
                duration = TimeUtil.formatIntoDetailedString(seconds);
            }

            String banType = isBlacklisted ? "blacklisted" : "banned";
            String message = MessageBuilder
                    .error("You are {} " + banType + " from the server.\n{}")
                    .element(punishment.isInfinite() ? "permanently" : "temporarily")
                    .element(duration == null ? "" : ("Duration: " + duration))
                    .build();

            Core.LOGGER.log("Blocked player %s from entering because of %s.".formatted(profile.getMojangID(), reason), LogLevel.EXTENDED);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);
        }
    }
}
