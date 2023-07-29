package rip.alpha.core.velocity.permission;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.proxy.Player;

public class GrantPermissionListener {
    @Subscribe(order = PostOrder.FIRST)
    public void onPermissionSetup(PermissionsSetupEvent event) {
        if (!(event.getSubject() instanceof Player player)) {
            return;
        }
        event.setProvider(new GrantPermissionProvider(player));
    }
}
