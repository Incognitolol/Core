package rip.alpha.core.shared.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rip.alpha.core.shared.ranks.Rank;

import java.util.LinkedHashSet;
import java.util.Set;

@RequiredArgsConstructor
public class PermissionData {

    @Getter
    private final Rank rank;
    private final Set<String> permissions = new LinkedHashSet<>();

    public Set<String> getPermissions() {
        return Set.copyOf(this.permissions);
    }

    public boolean containsPermission(String permission) {
        return this.permissions.contains(permission);
    }

    public void addPermission(String permission) {
        this.permissions.add(permission);
    }

    public void removePermission(String permission) {
        this.permissions.remove(permission);
    }

}
