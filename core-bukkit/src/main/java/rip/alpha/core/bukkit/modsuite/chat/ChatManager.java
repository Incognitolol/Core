package rip.alpha.core.bukkit.modsuite.chat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatManager {

    @Getter
    private static final ChatManager instance = new ChatManager();

    private boolean muteChat = false;
    private long millisForSlowChat = 3000;
}
