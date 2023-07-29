package rip.alpha.core.bukkit.grants;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import rip.alpha.core.bukkit.Core;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.data.PermissionData;
import rip.alpha.core.shared.data.PermissionDataManager;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.core.shared.server.NetworkServerType;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.util.task.TaskUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RankPermissionEvaluator {

    private static final Map<UUID, PermissionAttachment> activeAttachmentMap = new Object2ObjectOpenHashMap<>();

    public synchronized static void reEvaluatePermissionsRealTime(Player player) {
        AlphaProfileManager.profiles().getOrCreateRealTimeDataAsync(player.getUniqueId()).thenAccept(alphaProfile ->
                RankPermissionEvaluator.reEvaluatePermission(player, alphaProfile));
    }

    public synchronized static void reEvaluatePermissionsCached(Player player) {
        if (!AlphaProfileManager.profiles().isLocallyCached(player.getUniqueId())){
            return;
        }
        RankPermissionEvaluator.reEvaluatePermission(player, AlphaProfileManager.profiles().getCachedValue(player.getUniqueId()));
    }

    private synchronized static void reEvaluatePermission(Player player, AlphaProfile alphaProfile) {
        Core.LOGGER.log("Evaluating rank permissions for " + player.getName(), LogLevel.EXTENDED);
        PermissionAttachment current = activeAttachmentMap.get(player.getUniqueId());

        if (current != null) {
            current.remove();
        }

        PermissionAttachment attachment = player.addAttachment(Core.getInstance());
        activeAttachmentMap.put(player.getUniqueId(), attachment);

        Set<Rank> ranks = EnumSet.noneOf(Rank.class);
        for (Rank rank : alphaProfile.getGrantedRanks()) {
            ranks.add(rank);
            ranks.addAll(rank.getChildRanks(true));
        }

        Set<String> permissions = new HashSet<>();

        if (NetworkServerHandler.getInstance().getCurrentServer().getServerType() == NetworkServerType.BUILD) {
            permissions.add("worldedit.*");
            permissions.add("voxelsniper.sniper");
            permissions.add("voxelsniper.ignorelimitations");
            permissions.add("voxelsniper.goto");
            permissions.add("voxelsniper.brush.*");
            permissions.add("fawe.admin");
            permissions.add("core.command.gmc");
            permissions.add("core.command.gms");
            permissions.add("core.command.warp.create");
            permissions.add("core.command.warp.delete");
            permissions.add("core.command.warp.tp");
            permissions.add("core.command.warps");
        }

        for (Rank rank : ranks) {
            PermissionData data = PermissionDataManager.permissions().getCachedValue(rank);
            permissions.addAll(data.getPermissions());
        }

        //keep this running on the same thread so the join bypass doesn't get screwed... I LOVE THREADS!
        if (Bukkit.isPrimaryThread()) {
            permissions.forEach(permission -> attachment.setPermission(permission, true));
            Core.LOGGER.log("Added " + permissions.size() + " permissions to " + player.getName(), LogLevel.EXTENDED);
        } else {
            TaskUtil.runSync(() -> {
                permissions.forEach(permission -> attachment.setPermission(permission, true));
                Core.LOGGER.log("Added " + permissions.size() + " permissions to " + player.getName(), LogLevel.EXTENDED);
            });
        }
    }

    public static void terminate(UUID playerId) {
        activeAttachmentMap.remove(playerId);
        Core.LOGGER.log("Removing permission attachment " + playerId.toString(), LogLevel.VERBOSE);
    }

    public static void terminate(Player player) {
        terminate(player.getUniqueId());
    }

}
