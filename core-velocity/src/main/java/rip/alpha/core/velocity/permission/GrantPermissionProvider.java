package rip.alpha.core.velocity.permission;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.data.PermissionData;
import rip.alpha.core.shared.data.PermissionDataManager;
import rip.alpha.core.shared.ranks.Rank;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public record GrantPermissionProvider(Player player) implements PermissionProvider, PermissionFunction {
    @Override
    public Tristate getPermissionValue(String permission) {
        if (AlphaProfileManager.profiles().isLocallyCached(this.player.getUniqueId())) {
            AlphaProfile profile = AlphaProfileManager.profiles().getCachedValue(this.player.getUniqueId());
            if (profile == null) {
                return Tristate.FALSE;
            }
            return Tristate.fromBoolean(this.getPermissions(profile).contains(permission.toLowerCase()));
        }
        return Tristate.FALSE;
    }

    @Override
    public PermissionFunction createFunction(PermissionSubject subject) {
        Preconditions.checkState(subject.equals(this.player), "createFunction called with different argument");
        return this;
    }

    private Set<String> getPermissions(AlphaProfile profile) {
        Set<Rank> ranks = EnumSet.noneOf(Rank.class);

        for (Rank rank : profile.getGrantedRanks()) {
            ranks.add(rank);
            ranks.addAll(rank.getChildRanks(true));
        }

        Set<String> permissions = new HashSet<>();
        for (Rank rank : ranks) {
            PermissionData data = PermissionDataManager.permissions().getCachedValue(rank);
            permissions.addAll(data.getPermissions());
        }

        return permissions;
    }
}
