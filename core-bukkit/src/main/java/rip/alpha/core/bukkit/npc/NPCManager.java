package rip.alpha.core.bukkit.npc;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.Core;
import rip.alpha.libraries.LibrariesPlugin;
import rip.alpha.libraries.json.GsonProvider;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.util.task.TaskUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NPCManager {

    @Getter
    private static final NPCManager instance = new NPCManager();

    private NPCManager() {
        TaskUtil.scheduleAtFixedRateOnPool(new NPCUpdateArrowTask(), 45, 45, TimeUnit.SECONDS);
    }

    public void registerAllNPCs() {
        Core.LOGGER.log("Registering all NPCs...", LogLevel.BASIC);
        try {
            String json = Files.readString(this.getNPCFilePath(), StandardCharsets.UTF_8);
            NPCEntryMap npcEntryMap = GsonProvider.fromJson(json, NPCEntryMap.class);
            npcEntryMap.getAllEntries().forEach(this::registerNPC);
            Core.LOGGER.log("Registered %d NPCs".formatted(npcEntryMap.getAllEntries().size()), LogLevel.BASIC);
        } catch (NoSuchFileException e) {
            Core.LOGGER.warn("Couldn't find npcs.json", LogLevel.BASIC);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAllNPCs() {
        Core.LOGGER.log("Saving all NPCs...", LogLevel.BASIC);
        try {
            List<NPCEntry> npcList = new ArrayList<>();
            LibrariesPlugin.getInstance().getFakeEntityHandler().getEntities().iterator().forEachRemaining(entity -> {
                if (entity instanceof NPC) {
                    npcList.add(((NPC) entity).toEntry());
                }
            });

            NPCEntryMap entryMap = new NPCEntryMap();
            entryMap.putAllEntries(npcList);
            Files.writeString(this.getNPCFilePath(), GsonProvider.toJsonPretty(entryMap));
            Core.LOGGER.log("Saved %d NPCs.".formatted(entryMap.getAllEntries().size()), LogLevel.BASIC);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void registerNPC(NPCEntry npcEntry) {
        Core.LOGGER.log("Registering NPC %d @%s".formatted(npcEntry.getId(), npcEntry.getLocation()), LogLevel.BASIC);
        NPC npc = new NPC(npcEntry);
        npc.setCommand(npcEntry.getCommand());
        LibrariesPlugin.getInstance().getFakeEntityHandler().registerFakeEntity(npc);
        npc.showToAll();
        for (Player target : Bukkit.getOnlinePlayers()) {
            npc.addToTeam(target);
        }
    }

    private Path getNPCFilePath() {
        return new File(Core.getInstance().getDataFolder(), "npcs.json").toPath();
    }
}
