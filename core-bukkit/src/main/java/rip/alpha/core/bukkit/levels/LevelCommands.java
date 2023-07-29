package rip.alpha.core.bukkit.levels;

import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.data.player.CorePlayerProfile;
import rip.alpha.core.bukkit.data.player.CorePlayerProfileManager;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.message.MessageBuilder;

public class LevelCommands {

    @CommandUsage("<target> <amount>")
    @Command(names = {"xp add"}, permission = "core.command.xp.add")
    public static void onAdd(Player sender, Player player, int amount) {
        CorePlayerProfile profile = CorePlayerProfileManager.profiles().getData(player.getUniqueId());
        profile.getLevelData().addExperience(amount);
        player.sendMessage(MessageBuilder.standard("{} exp where added to you. Your level is now {}.")
                .element(amount)
                .element(profile.getLevelData().getLevel())
                .build());
        sender.sendMessage(MessageBuilder.standard("Added {} exp to {}. His level is now {}.")
                .element(amount)
                .element(player.getName())
                .element(profile.getLevelData().getLevel())
                .build());
    }

    @CommandUsage("<target> <amount>")
    @Command(names = {"xp remove"}, permission = "core.command.xp.remove")
    public static void onRemove(Player sender, Player player, int amount) {
        CorePlayerProfile profile = CorePlayerProfileManager.profiles().getData(player.getUniqueId());
        profile.getLevelData().removeExperience(amount);
        player.sendMessage(MessageBuilder.standard("{} exp where removed from you. Your level is now {}.")
                .element(amount)
                .element(profile.getLevelData().getLevel())
                .build());
        sender.sendMessage(MessageBuilder.standard("Removed {} exp from {}. His level is now {}.")
                .element(amount)
                .element(player.getName())
                .element(profile.getLevelData().getLevel())
                .build());
    }

    @CommandUsage("<target>")
    @Command(names = {"xp info"}, permission = "core.command.xp.info")
    public static void onInfo(Player sender, Player player) {
        CorePlayerProfile profile = CorePlayerProfileManager.profiles().getData(player.getUniqueId());
        sender.sendMessage(MessageBuilder.standard("{} has {} exp and is therefor level {}.")
                .element(player.getName())
                .element(profile.getLevelData().getExperience())
                .element(profile.getLevelData().getLevel())
                .build());
    }

    @CommandUsage("<lvl>")
    @Command(names = {"xp lvlinfo"}, permission = "core.command.xp.lvlinfo")
    public static void onLvlInfo(Player sender, int lvl) {
        lvl = Math.max(1, Math.min(lvl, 100));
        int current = LevelManager.getInstance().getTotalExpToLevel(lvl);
        int next = LevelManager.getInstance().getTotalExpToLevel(lvl) - 1;
        sender.sendMessage(MessageBuilder.construct("Level {} is between {} and {}.", lvl, current, next));
    }

    @CommandUsage("<xp>")
    @Command(names = {"xp xpinfo"}, permission = "core.command.xp.xpinfo")
    public static void onXpInfo(Player sender, int xp) {
        int lvl = LevelManager.getInstance().getLevelOfTotalExp(xp);
        sender.sendMessage(MessageBuilder.construct("With {} exp you should be level {}.", xp, lvl));
    }

}
