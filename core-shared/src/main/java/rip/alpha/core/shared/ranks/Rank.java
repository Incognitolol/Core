package rip.alpha.core.shared.ranks;

import lombok.Getter;
import rip.alpha.core.shared.data.PermissionDataManager;
import rip.alpha.libraries.util.message.MessageTranslator;

import java.awt.*;
import java.util.List;
import java.util.*;


public enum Rank {

    // Staff Ranks
    OWNER(1, "Owner", "&4", "&8[&4Owner&8]", Color.RED, 2),
    DEVELOPER(2, "Developer", "&b", "&8[&bDeveloper&8]", Color.CYAN, 3),
    MANAGER(3, "Manager", "&4", "&8[&4Manager&8]", new Color(170, 0, 0), 4),
    PLATFORM_ADMIN(4, "PlatformAdmin", "&c", "&8[&cPlatAdmin&8]", Color.RED, 5),
    HEAD_ADMIN(5, "HeadAdmin", "&c", "&8[&cAdmin&8]", Color.RED, 6),
    ADMIN(6, "Admin", "&c", "&8[&cAdmin&8]", Color.RED, 7),
    HEAD_MODERATOR(7, "HeadMod", "&3", "&8[&3Mod&8]", Color.BLUE, 8),
    MODERATOR(8, "Moderator", "&3", "&8[&3Mod&8]", Color.BLUE, 9),
    TRAINEE(9, "Trainee", "&e", "&8[&eTrainee&8]", Color.YELLOW, 10),
    BUILDER(10, "Builder", "&2", "&8[&2Builder&8]", Color.GREEN, 11),

    // Media
    FAMOUS(11, "Famous", "&b", "&8[&bFamous&8]", Color.BLUE, 12),
    YOUTUBE(12, "Youtube", "&c", "&8[&cYouTube&8]", Color.RED, 13),
    STREAMER(13, "Streamer", "&d", "&8[&dStreamer&8]", Color.PINK, 14),

    // Donator Ranks
    HOF(14, "HOF", "&5", "&8[&5HOF&8]", Color.MAGENTA, 15),
    MVP_PLUS(15, "MVP+", "&9", "&8[&9MVP+&8]", Color.BLUE, 16),
    MVP(16, "MVP", "&9", "&8[&9MVP&8]", Color.BLUE, 17),
    PRO_PLUS(17, "PRO+", "&6", "&8[&6PRO+&8]", Color.ORANGE, 18),
    PRO(18, "PRO", "&6", "&8[&6PRO&8]", Color.ORANGE, 19),
    VIP_PLUS(19, "VIP+", "&a", "&8[&aVIP+&8]", Color.GREEN, 20),
    VIP(20, "VIP", "&a", "&8[&aVIP&8]", Color.GREEN, 21),

    DEFAULT(21, "Default", "&f", "", Color.WHITE);

    @Getter
    private final int priority;
    @Getter
    private final String name, color, prefix, displayName;
    @Getter
    private final Color javaColor;
    private final int[] childrenPriorities;
    private Set<Rank> childrenCache = null;
    private Set<Rank> deepChildrenCache = null;

    Rank(int priority, String name, String color, String prefix, Color javaColor, int... children) {
        this.priority = priority;
        this.name = name;
        this.color = MessageTranslator.translate(color);
        this.prefix = MessageTranslator.translate(prefix);
        this.javaColor = javaColor;
        this.displayName = this.color + this.name;
        this.childrenPriorities = children;
    }

    public boolean isHigherOrEqualTo(Rank rank) {
        return this.priority <= rank.priority;
    }

    public Set<Rank> getChildRanks(boolean deep) {
        if (this.childrenPriorities.length == 0) {
            return Collections.emptySet();
        }
        EnumSet<Rank> children = EnumSet.noneOf(Rank.class);
        if (deep) {
            if (this.deepChildrenCache != null) {
                return EnumSet.copyOf(this.deepChildrenCache);
            }
            for (int childPriority : this.childrenPriorities) {
                Rank childRank = getByPriority(childPriority);
                if (childRank == null) {
                    throw new IllegalStateException("No rank with priority %d was found.".formatted(childPriority));
                }
                children.add(childRank);
                children.addAll(childRank.getChildRanks(true));
            }
            this.deepChildrenCache = EnumSet.copyOf(children);
        } else {
            if (this.childrenCache != null) {
                return EnumSet.copyOf(this.childrenCache);
            }
            for (int childPriority : this.childrenPriorities) {
                Rank childRank = getByPriority(childPriority);
                if (childRank == null) {
                    throw new IllegalStateException("No rank with priority %d was found.".formatted(childPriority));
                }
                children.add(childRank);
            }
            this.childrenCache = EnumSet.copyOf(children);
        }
        return children;
    }

    public static Rank getByPriority(int priority) {
        for (Rank rank : Rank.values()) {
            if (rank.priority == priority) {
                return rank;
            }
        }
        return null;
    }

    public Map<Rank, List<String>> getPermissionMappingSnapshot() {
        Map<Rank, List<String>> permMap = new LinkedHashMap<>();
        permMap.put(this, new ArrayList<>());
        for (Rank cRank : this.getChildRanks(true)) {
            permMap.put(cRank, new ArrayList<>());
        }
        permMap.forEach((rank, perms) -> perms.addAll(PermissionDataManager.permissions().getCachedValue(rank).getPermissions()));
        return permMap;
    }

    public Set<String> getEffectivePermissionSnapshot() {
        Set<String> effectivePerms = new HashSet<>();
        this.getPermissionMappingSnapshot().values().forEach(effectivePerms::addAll);
        return effectivePerms;
    }

}