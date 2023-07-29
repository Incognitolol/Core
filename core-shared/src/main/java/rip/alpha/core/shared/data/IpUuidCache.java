package rip.alpha.core.shared.data;

import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import rip.alpha.libraries.Libraries;
import rip.alpha.libraries.model.DataManager;
import rip.alpha.libraries.model.DomainContext;
import rip.alpha.libraries.model.GlobalDataDomain;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IpUuidCache {

    @Getter
    private static final IpUuidCache instance = new IpUuidCache();

    private final GlobalDataDomain<String, UUIDStash> ipToUuidDomain;
    private final GlobalDataDomain<UUID, EncryptedIpStash> uuidToIpDomain;

    private IpUuidCache() {
        MongoDatabase database = Libraries.getMongoClient().getDatabase("global-caches");
        DomainContext<String, UUIDStash> ipToUuidContext = DomainContext.<String, UUIDStash>builder()
                .mongoDatabase(database)
                .namespace("ip-to-uuid")
                .creator(ip -> new UUIDStash(ip, new HashSet<>()))
                .valueClass(UUIDStash.class)
                .keyClass(String.class)
                .keyFunction(UUIDStash::encryptedIP)
                .redissonClient(Libraries.getRedissonClient())
                .build();
        DomainContext<UUID, EncryptedIpStash> uuidToIpContext = DomainContext.<UUID, EncryptedIpStash>builder()
                .mongoDatabase(database)
                .namespace("uuid-to-ip")
                .creator(id -> new EncryptedIpStash(id, new HashSet<>()))
                .valueClass(EncryptedIpStash.class)
                .keyClass(UUID.class)
                .keyFunction(EncryptedIpStash::keyID)
                .redissonClient(Libraries.getRedissonClient())
                .build();
        this.ipToUuidDomain = DataManager.getInstance().getOrCreateGlobalDomain(ipToUuidContext);
        this.uuidToIpDomain = DataManager.getInstance().getOrCreateGlobalDomain(uuidToIpContext);
    }

    public void removeIdFromIp(UUID uuid, String encryptedIP) {
        this.ipToUuidDomain.applyToData(encryptedIP, stash -> stash.uuids.remove(uuid));
    }

    public void addEntry(UUID uuid, String encryptedIP) {
        this.ipToUuidDomain.applyToData(encryptedIP, data -> data.uuids.add(uuid));
        this.uuidToIpDomain.applyToData(uuid, data -> data.addresses.add(encryptedIP));
    }

    public void addEntryAsync(UUID uuid, String encryptedIP) {
        this.ipToUuidDomain.applyToDataAsync(encryptedIP, data -> data.uuids.add(uuid));
        this.uuidToIpDomain.applyToDataAsync(uuid, data -> data.addresses.add(encryptedIP));
    }

    public EncryptedIpStash getIpsOf(UUID uuid) {
        return this.uuidToIpDomain.getOrCreateRealTimeData(uuid);
    }

    public void clearIpsOf(UUID uuid) {
        this.uuidToIpDomain.deleteDataGloballySync(uuid);
    }

    public UUIDStash getIdsOf(String encryptedIP) {
        return this.ipToUuidDomain.getOrCreateRealTimeData(encryptedIP);
    }

    public CompletableFuture<EncryptedIpStash> getIpsOfAsync(UUID uuid) {
        return this.uuidToIpDomain.getOrCreateRealTimeDataAsync(uuid);
    }

    public CompletableFuture<UUIDStash> getIdsOfAsync(String encryptedIP) {
        return this.ipToUuidDomain.getOrCreateRealTimeDataAsync(encryptedIP);
    }

    public record EncryptedIpStash(UUID keyID, Set<String> addresses) {

    }

    public record UUIDStash(String encryptedIP, Set<UUID> uuids) {

    }

}
