package rip.alpha.core.bukkit.modsuite.alts;

import rip.alpha.core.bukkit.Core;
import rip.alpha.core.shared.data.IpUuidCache;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.util.data.NameCache;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class AltsFetcher {

    public static Set<String> fetchAltNames(String target, Consumer<String> statusConsumer) {
        statusConsumer.accept(MessageBuilder.construct("Fetching main UUID..."));
        Set<String> names = new LinkedHashSet<>();

        NameCache.getInstance().getIDAsync(target).thenAccept(playerID -> {
            statusConsumer.accept(MessageBuilder.construct("Fetching IPs..."));
            IpUuidCache.EncryptedIpStash ipStash = IpUuidCache.getInstance().getIpsOf(playerID);
            statusConsumer.accept(MessageBuilder.construct("Found {} IPs...", ipStash.addresses().size()));
            Set<UUID> uuids = new HashSet<>();
            statusConsumer.accept(MessageBuilder.construct("Fetching UUIDs..."));
            for (String encIp : ipStash.addresses()) {
                uuids.addAll(IpUuidCache.getInstance().getIdsOf(encIp).uuids());
            }
            statusConsumer.accept(MessageBuilder.construct("Found {} UUIDs...", uuids.size()));
            statusConsumer.accept(MessageBuilder.construct("Fetching names..."));
            for (UUID uuid : uuids) {
                try {
                    names.add(NameCache.getInstance().getNameAsync(uuid).get(2, TimeUnit.SECONDS));
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    e.printStackTrace();
                }
            }

            MessageBuilder messageBuilder = MessageBuilder.standard("");

            String line = "§r§7§m------------------------------------";
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(line).append("\n");
            stringBuilder.append("Profiles associated with {}\n");
            messageBuilder.element(target);
            stringBuilder.append(line).append("\n");

            for (String name : names) {
                stringBuilder.append("- {}\n");
                messageBuilder.element(name);
            }

            Core.LOGGER.log("Fetched %d alts for %s".formatted(uuids.size(), target), LogLevel.EXTENDED);

            messageBuilder.message(stringBuilder.toString());
            statusConsumer.accept(messageBuilder.build());
        }).join();

        return names;
    }

    public static Set<String> fetchAltNames(String target) {
        return fetchAltNames(target, msg -> {
        });
    }
}
