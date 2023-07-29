package rip.alpha.core.bukkit.npc;

import rip.alpha.libraries.LibrariesPlugin;
import rip.alpha.libraries.fake.FakeEntity;

public class NPCUpdateArrowTask implements Runnable {
    @Override
    public void run() {
        for (FakeEntity entity : LibrariesPlugin.getInstance().getFakeEntityHandler().getEntities()) {
            if (!(entity instanceof NPC npc)) {
                continue;
            }

            if (!npc.isSitting()) {
                continue;
            }

            npc.setEntityArrow(null);
            npc.setAttachPacket(null);
            npc.updateRideForViewers();
        }
    }
}
