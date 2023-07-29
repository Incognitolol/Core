package rip.alpha.core.discord.sync.listener;

import lombok.extern.java.Log;
import rip.alpha.core.discord.CoreBot;
import rip.alpha.core.shared.AlphaCore;
import rip.alpha.core.shared.data.events.ProfileSyncEvent;
import rip.alpha.libraries.Libraries;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.util.data.NameCache;

import java.util.function.Consumer;

/**
 * @author Moose1301
 * @date 4/10/2022
 */
public class ProfileSyncListener {
    public static void onProfileSyncCreate(ProfileSyncEvent event) {

        CoreBot.getInstance().getSyncRankHandler().addSyncCode(event.code(), event.profileID());
        NameCache.getInstance().getNameAsync(event.profileID()).thenAccept(new Consumer<String>() {
            @Override
            public void accept(String name) {
                CoreBot.LOGGER.log("Created Sync: " + name + " " + event.code(), LogLevel.BASIC);
            }
        });
    }
}
