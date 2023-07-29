package rip.alpha.core.buycraft;

import lombok.SneakyThrows;
import rip.alpha.core.buycraft.response.CommandsDueResponse;
import rip.alpha.core.buycraft.response.InformationResponse;
import rip.alpha.core.buycraft.response.PaymentResponse;
import rip.alpha.core.buycraft.response.PlayersDueResponse;
import rip.alpha.core.shared.AlphaCore;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.data.AlphaProfileUtilities;
import rip.alpha.core.shared.discord.DiscordLogEvent;
import rip.alpha.core.shared.economy.EconomyManager;
import rip.alpha.core.shared.economy.TokenType;
import rip.alpha.core.shared.punishments.ProfilePunishmentRemoveEvent;
import rip.alpha.core.shared.punishments.Punishment;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.core.shared.server.event.NetworkServerREBroadcastEvent;
import rip.alpha.libraries.Libraries;
import rip.alpha.libraries.json.GsonProvider;
import rip.alpha.libraries.logging.AlphaLogger;
import rip.alpha.libraries.logging.AlphaLoggerFactory;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BuycraftCore {
    private static final AlphaLogger LOGGER = AlphaLoggerFactory.createLogger("Buycraft-Core");

    private final BuycraftAPI buycraftAPI;
    private final ScheduledExecutorService executor;
    private boolean enabled = true;


    public BuycraftCore(String key) throws IOException {
        this.buycraftAPI = new BuycraftAPI(key);

        try {
            InformationResponse response = this.buycraftAPI.fetchInformation();
            System.out.println(
                    "Store Info\n" +
                    "Name: " + response.name() + "\n" + "" +
                            "Domain: " + response.domain() + "\n"
            );
        } catch (BuycraftException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Bad buycraft key?");
        }

        this.executor = Executors.newSingleThreadScheduledExecutor();

        //checkPayment();
        //this.executor.scheduleAtFixedRate(this::check, 1L, 1L, TimeUnit.SECONDS);
        //this.hook();
        buycraftAPI.fetchPlayerLookup("2fe3185d-6ee4-41ed-a48e-eed2baface7f");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.executor.shutdown();
                this.buycraftAPI.close();
                AlphaCore.disable();
                Libraries.getInstance().disable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
    @SneakyThrows
    public void checkPayment() {
        PaymentResponse paymentResponse = buycraftAPI.fetchPayment("tbx-31533022a46710-3aafc6");
        System.out.println("Bought By: " + paymentResponse.player().name());
        for (PaymentResponse.Package aPackage : paymentResponse.packages()) {
            System.out.println("Package: " + aPackage.name());
        }
    }
    private void hook() {
        Libraries.getInstance().enable();
        AlphaCore.enable();
        Scanner command = new Scanner(System.in);

        while (enabled) {
            try {
                switch (command.nextLine().toLowerCase().trim()) {
                    case "forcecheck", "check" -> this.executor.execute(this::check);
                    case "close", "stop" -> enabled = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        command.close();
    }

    protected void check() {
        try {
            LOGGER.log("Fetching due players...", LogLevel.BASIC);
            PlayersDueResponse playersDueResponse = buycraftAPI.fetchDuePlayers();
            if (playersDueResponse.priorityQueue().isEmpty()) {
                LOGGER.log("No due players found...", LogLevel.BASIC);
                return;
            }
            LOGGER.log("Found %s due players".formatted(playersDueResponse.priorityQueue().size()), LogLevel.BASIC);
            int delay = playersDueResponse.nextCheck();
            LOGGER.log("Fetching queued commands...", LogLevel.BASIC);
            CommandsDueResponse response = buycraftAPI.fetchDueCommands();
            List<Integer> ids = new ArrayList<>();
            while (!response.queuedCommands().isEmpty()) {
                CommandsDueResponse.QueuedCommand command = response.queuedCommands().poll();
                String commandName = command.command();
                UUID uuid = command.player().formatUUID();
                if (commandName.startsWith("tokens:")) {
                    int tokens = Integer.parseInt(commandName.split(":")[1]);
                    this.giveTokens(uuid, tokens);
                    ids.add(command.id());
                } else if (commandName.startsWith("rank:")) {
                    String[] args = commandName.split(":")[1].split(","); //rank:OWNER,10m
                    try {
                        Rank rank = Rank.valueOf(args[0].toUpperCase());

                        long duration;
                        String durationName = args[1];
                        if (durationName.equalsIgnoreCase("perm")) {
                            duration = -1;
                        } else {
                            duration = TimeUtil.parseTime(durationName);
                        }

                        this.giveRank(uuid, rank, duration);
                        ids.add(command.id());
                    } catch (IllegalArgumentException exception) {
                        LOGGER.severe("Tried to grant a rank that doesnt exist", LogLevel.BASIC);
                    }
                } else if (commandName.startsWith("blacklist")) {
                    this.blacklist(uuid);
                    ids.add(command.id());
                } else if (commandName.startsWith("unban:")) {
                    String reason = commandName.replace("unban:", ""); //Reason
                    unban(uuid, reason);
                    ids.add(command.id());
                }
            }
            LOGGER.log("Found and preformed %s due commands".formatted(ids.size()), LogLevel.BASIC);
            try {
                if (!ids.isEmpty()) {
                    this.buycraftAPI.queueDelete(ids);
                    LOGGER.log("Deleted %s commands from buycraft".formatted(ids.size()), LogLevel.BASIC);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            LOGGER.log("Waiting %s seconds as requested by call".formatted(delay), LogLevel.BASIC);
            Thread.sleep(delay * 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void unban(UUID playerId, String reason) {
        AlphaProfileManager.profiles().applyToData(playerId, profile -> {
            if (!profile.removePunishmentForType(Punishment.Type.SERVER_BAN, ProfilePunishmentRemoveEvent.Reason.LIFTED, "Buycraft", reason)) {
                new DiscordLogEvent("Buycraft", "Store Purchase",
                        profile.getLastSeenName() + " has purchased a unban while not being banned (UUID: " + playerId + ")",
                        Color.CYAN).callEvent();
            }

        });
    }
    private void blacklist(UUID playerId) {
        AlphaProfileManager.profiles().applyToData(playerId, profile -> {
            Punishment punishment = new Punishment(Punishment.Type.BLACKLIST, "Buycraft", "Refund / Chargeback", -1);
            profile.addPunishment(punishment);
        });
    }

    private void giveRank(UUID playerId, Rank rank, long duration) {
        AlphaProfileUtilities.addGrant(playerId, rank, "Buycraft", "Purchased", duration);
        AlphaProfileUtilities.getColoredName(playerId).thenAccept(coloredName -> {
            String message = MessageBuilder
                    .standard("{} has purchased {} from our store!")
                    .prefix("Store")
                    .element(coloredName)
                    .element(rank.getDisplayName())
                    .build();
            new NetworkServerREBroadcastEvent("bukkit", message, "").callEvent();
        });
    }

    private void giveTokens(UUID playerId, int tokens) {
        EconomyManager.addToFunds("Buycraft", playerId, TokenType.BOUGHT, tokens);
        AlphaProfileUtilities.getColoredName(playerId).thenAccept(coloredName -> {
            String message = MessageBuilder
                    .standard("{} has purchased ⛃{} from our store!")
                    .prefix("Store")
                    .element(coloredName)
                    .element(tokens)
                    .build();
            new NetworkServerREBroadcastEvent("bukkit", message, "").callEvent();
        });
    }
}
