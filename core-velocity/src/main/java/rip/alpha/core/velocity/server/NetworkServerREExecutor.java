package rip.alpha.core.velocity.server;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NetworkServerREExecutor implements CommandSource {
    @Override
    public boolean hasPermission(@NonNull String permission) {
        return true;
    }

    @Override
    public Tristate getPermissionValue(String s) {
        return Tristate.TRUE;
    }
}
