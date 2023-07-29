package rip.alpha.core.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.grants.Grant;
import rip.alpha.core.shared.grants.ProfileGrantRemoveEvent;
import rip.alpha.core.shared.punishments.ProfilePunishmentRemoveEvent;
import rip.alpha.core.shared.punishments.Punishment;
import rip.alpha.core.shared.server.*;
import rip.alpha.core.velocity.Core;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.model.GlobalDataDomain;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PlayerSessionListener {
    @Subscribe(order = PostOrder.FIRST)
    public void onLogin(LoginEvent event) {
        try {
            UUID playerID = event.getPlayer().getUniqueId();
            GlobalDataDomain<UUID, AlphaProfile> profiles = AlphaProfileManager.profiles();
            profiles.applyToData(playerID, profile -> {
                this.checkGrants(profile);
                this.checkPunishments(profile);
                profile.setCurrentlyOnline(true);
            });
            if (!profiles.isLocallyCached(playerID)) {
                profiles.enableLocalCacheFor(playerID);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            String message = "§6There was a problem while loading your data.\n§6Please contact the Team.";
            event.setResult(ResultedEvent.ComponentResult.denied(Component.text(message)));
            this.terminatePlayer(event.getPlayer().getUniqueId());
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onServerPreConnect(ServerPreConnectEvent event) {
        String name = event.getOriginalServer().getServerInfo().getName();

        if (!name.equalsIgnoreCase("lobby") && !name.equalsIgnoreCase("hub")) {
            return;
        }

        UUID playerID = event.getPlayer().getUniqueId();
        AlphaProfileManager.profiles().applyToData(playerID, profile -> {
            try {
                this.selectHub(profile, event);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                this.handleServerNotFound(event);
                e.printStackTrace();
            }
        });
    }

    @Subscribe(order = PostOrder.LAST)
    public void onDisconnect(DisconnectEvent event) {
        this.terminatePlayer(event.getPlayer().getUniqueId());
    }

    private void terminatePlayer(UUID playerId) {
        AlphaProfileManager.profiles().disableLocalCacheFor(playerId);
        AlphaProfileManager.profiles().applyToDataAsync(playerId, alphaProfile -> alphaProfile.setCurrentlyOnline(false));
    }

    private void checkGrants(AlphaProfile profile) {
        for (Grant grant : profile.getAllGrants()) {
            if (grant.hasExpired()) {
                profile.removeGrant(grant, ProfileGrantRemoveEvent.Reason.RUN_OUT, "CONSOLE", "Expired");
            }
        }
    }

    private void checkPunishments(AlphaProfile profile) {
        for (Punishment punishment : profile.getAllActivePunishments()) {
            if (punishment.hasExpired()) {
                profile.removePunishment(punishment, ProfilePunishmentRemoveEvent.Reason.RUN_OUT, "CONSOLE", "Expired");
            }
        }
    }

    private void selectHub(AlphaProfile profile, ServerPreConnectEvent event) throws ExecutionException, InterruptedException, TimeoutException {
        boolean isBlacklisted = profile.hasPunishment(Punishment.Type.BLACKLIST);
        boolean isBanned = profile.hasPunishment(Punishment.Type.SERVER_BAN);
        Set<NetworkServer> serverList;

        if (isBlacklisted || isBanned) {
            serverList = NetworkServerHandler.getInstance().getServersByTypeAsync(NetworkServerType.BANNED_HUB).get(5, TimeUnit.SECONDS);
        } else if (event.getPlayer().hasPermission("core.server.restricted")) {
            serverList = NetworkServerHandler.getInstance().getServersByTypeAsync(NetworkServerType.RESTRICTED_HUB).get(5, TimeUnit.SECONDS);
        } else {
            serverList = NetworkServerHandler.getInstance().getServersByTypeAsync(NetworkServerType.HUB).get(5, TimeUnit.SECONDS);
        }

        serverList.removeIf(server -> server.getNetworkServerStatus() == NetworkServerStatus.OFFLINE);
        serverList.removeIf(server -> server.getServerPlatform() != NetworkServerPlatform.BUKKIT);

        if (serverList.size() < 1) {
            this.handleServerNotFound(event);
            return;
        }

        NetworkServer server = new ArrayList<>(serverList).get(ThreadLocalRandom.current().nextInt(serverList.size()));

        Core.getInstance().getProxyServer()
                .getServer(server.getServerId())
                .ifPresentOrElse(foundServer -> this.handleServerFound(event, foundServer), () -> this.handleServerNotFound(event));
    }

    private void handleServerFound(ServerPreConnectEvent event, RegisteredServer registeredServer) {
        ServerPreConnectEvent.ServerResult result = ServerPreConnectEvent.ServerResult.allowed(registeredServer);
        event.setResult(result);
    }

    private void handleServerNotFound(ServerPreConnectEvent event) {
        Core.LOGGER.log("Couldn't find a lobby to place " + event.getPlayer().getUsername() + " in.", LogLevel.EXTENDED);
        event.setResult(ServerPreConnectEvent.ServerResult.denied());
        event.getPlayer().disconnect(Component.text("We couldnt find a lobby to place you into.").color(NamedTextColor.RED));
    }
}