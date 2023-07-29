package rip.alpha.core.bukkit.npc;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.libraries.LibrariesPlugin;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandDescription;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Wildcard;
import rip.alpha.libraries.skin.MojangSkin;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageColor;
import rip.alpha.libraries.util.message.MessageTranslator;
import rip.alpha.libraries.util.uuid.UUIDFetcher;

import java.util.ArrayList;
import java.util.UUID;

public class NPCCommand {

    @CommandDescription("Create an npc")
    @CommandUsage("<id> [displayName...]")
    @Command(names = {"npc create"}, permission = "core.command.npc", async = true)
    public static void npcCreateCommand(Player player, int id, @Wildcard String displayName) {
        if (displayName.length() > 16) {
            player.sendMessage(MessageColor.RED + "That name is too long!");
            return;
        }

        if (LibrariesPlugin.getInstance().getFakeEntityHandler().getEntityById(id) != null) {
            player.sendMessage(MessageColor.RED + "There is already a fake entity with that id registered");
            return;
        }

        NPCEntry npcEntry = new NPCEntry(id, MessageTranslator.translate(displayName),
                null, player.getLocation(), new ItemStack[5], null, new ArrayList<>(), false);
        NPCManager.getInstance().registerNPC(npcEntry);

        String message = MessageBuilder.standard("You have successfully created an npc named {} with the id {}.")
                .element(displayName)
                .element(id)
                .build();
        player.sendMessage(message);
    }

    @CommandDescription("Remove / Delete an npc")
    @CommandUsage("<npcId>")
    @Command(names = {"npc remove"}, permission = "core.command.npc", async = true)
    public static void npcRemoveCommand(Player player, NPC npc) {
        LibrariesPlugin.getInstance().getFakeEntityHandler().removeFakeEntity(npc.getEntityId());
        npc.hideFromAll();
        player.sendMessage(MessageBuilder.construct("You have successfully removed the npc with the id {}.", npc.getId()));
    }

    @CommandDescription("Set the held item of the npc")
    @CommandUsage("<npcId>")
    @Command(names = {"npc sethelditem"}, permission = "core.command.npc", async = true)
    public static void npcSetHeldItemCommand(Player player, NPC npc) {
        npc.setEquipment(0, player.getItemInHand());
        player.sendMessage(MessageBuilder.construct("NPC {} is now holding a {}.", npc.getId(), player.getItemInHand()));
    }

    @CommandDescription("Set the helmet item of the npc")
    @CommandUsage("<npcId>")
    @Command(names = {"npc sethelmet"}, permission = "core.command.npc", async = true)
    public static void setHelmetCommand(Player player, NPC npc) {
        npc.setEquipment(4, player.getItemInHand());
        player.sendMessage(MessageBuilder.construct("NPC {} is now wearing a {}.", npc.getId(), player.getItemInHand()));
    }

    @CommandDescription("Set the chestplate item of the npc")
    @CommandUsage("<npcId>")
    @Command(names = {"npc setchestplate"}, permission = "core.command.npc", async = true)
    public static void setChestplateCommand(Player player, NPC npc) {
        npc.setEquipment(3, player.getItemInHand());
        player.sendMessage(MessageBuilder.construct("NPC {} is now wearing a {}.", npc.getId(), player.getItemInHand()));
    }

    @CommandDescription("Set the leggings item of the npc")
    @CommandUsage("<npcId>")
    @Command(names = {"npc setleggings"}, permission = "core.command.npc", async = true)
    public static void setLegginsCommand(Player player, NPC npc) {
        npc.setEquipment(2, player.getItemInHand());
        player.sendMessage(MessageBuilder.construct("NPC {} is now wearing a {}.", npc.getId(), player.getItemInHand()));
    }

    @CommandDescription("Set the boots item of the npc")
    @CommandUsage("<npcId>")
    @Command(names = {"npc setboots"}, permission = "core.command.npc", async = true)
    public static void setBootsCommand(Player player, NPC npc) {
        npc.setEquipment(1, player.getItemInHand());
        player.sendMessage(MessageBuilder.construct("NPC {} is now wearing a {}.", npc.getId(), player.getItemInHand()));
    }

    @CommandDescription("Setup the command executed when interacted with the npc")
    @CommandUsage("<npcId> <command>")
    @Command(names = {"npc setcommand"}, permission = "core.command.npc", async = true)
    public static void npcSetCommand(Player player, NPC npc, @Wildcard String command) {
        npc.setCommand(command);
        player.sendMessage(MessageBuilder.construct("You have updated the command of npc {}.", npc.getId()));
    }

    @CommandDescription("Update npcs skin")
    @CommandUsage("<npcId> <skinName>")
    @Command(names = {"npc setskin"}, permission = "core.command.npc", async = true)
    public static void npcSetSkinCommand(Player player, NPC npc, String skinName) {
        UUID uuid = UUIDFetcher.getUUID(skinName);
        player.sendMessage(MessageBuilder.construct("Attempting to fetch skin with name {}.", skinName));
        MojangSkin skin = LibrariesPlugin.getInstance().getMojangSkinHandler().getMojangSkin(uuid);

        if (skin == null) {
            player.sendMessage(MessageBuilder.construct("Failed to find a skin with the name {}.", skinName));
            return;
        }

        npc.setMojangSkin(skin);
        npc.showToAll();
        player.sendMessage(MessageBuilder.construct("Successfully set npc {} skin to {}.", npc.getId(), skinName));
    }

    @CommandDescription("Teleport an npc to you")
    @CommandUsage("<npcId>")
    @Command(names = "npc teleport", permission = "core.command.npc", async = true)
    public static void npcTeleportCommand(Player player, NPC npc) {
        npc.teleport(player.getLocation());
        player.sendMessage(MessageBuilder.construct("You have teleported npc {} to your location.", npc.getId()));
    }

    @CommandDescription("Add a hologram line to an npc")
    @CommandUsage("<npcId> [line...]")
    @Command(names = "npc addline", permission = "core.command.npc", async = true)
    public static void npcAddLineCommand(Player player, NPC npc, @Wildcard String line) {
        npc.addLine(line);
        player.sendMessage(MessageBuilder.construct("You have added a hologram line to npc {}.", npc.getId()));
    }

    @CommandDescription("Remove a hologram line from an npc")
    @CommandUsage("<npcId> <index>")
    @Command(names = "npc removeline", permission = "core.command.npc", async = true)
    public static void npcRemoveLineCommand(Player player, NPC npc, int index) {
        npc.removeLine(index);
        player.sendMessage(MessageBuilder.construct("You have removed a hologram line from npc {}.", npc.getId()));
    }

    @CommandDescription("Make an npc sitdown or standup")
    @CommandUsage("<npcId>")
    @Command(names = "npc sit", permission = "core.command.npc", async = true)
    public static void sitCommand(Player player, NPC npc) {
        npc.setSitting(!npc.isSitting());
        player.sendMessage(MessageBuilder.construct("Successfully {} this npc.", (npc.isSitting() ? "sat" : "stood")));
    }
}
