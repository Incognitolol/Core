package rip.alpha.core.shared.data;

import lombok.Data;

@Data
public class AlphaProfileSettings {

    private boolean allowDirectMessages = true;
    private boolean notifyOnDM = true;
    private boolean notifyOnModerationBroadcast = false;
    private boolean showStaff = true;

}
