package rip.alpha.core.bukkit.data.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rip.alpha.core.bukkit.warps.WarpData;

@RequiredArgsConstructor
public class LocalServerData {

    @Getter
    private final String serverName;
    @Getter
    private final WarpData warpData = new WarpData();

}
