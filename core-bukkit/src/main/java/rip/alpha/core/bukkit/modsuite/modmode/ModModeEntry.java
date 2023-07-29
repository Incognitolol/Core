package rip.alpha.core.bukkit.modsuite.modmode;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED)
public class ModModeEntry {

    private ModModeStatus modModeStatus;
    private boolean vanished, build, frozen;

    protected ModModeEntry() {
        this.modModeStatus = ModModeStatus.NONE;
        this.vanished = false;
        this.build = false;
        this.frozen = false;
    }
}
