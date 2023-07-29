package rip.alpha.core.shared;

import com.mongodb.client.MongoDatabase;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import rip.alpha.bridge.BridgeEvent;
import rip.alpha.core.shared.bridge.AlphaBridgeEvent;
import rip.alpha.core.shared.queue.NetworkQueueSnapshotHandler;
import rip.alpha.core.shared.queue.event.NetworkQueueUpdateEvent;
import rip.alpha.core.shared.reboot.RebootHandler;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.core.shared.server.NetworkServerSnapshotHandler;
import rip.alpha.core.shared.server.event.NetworkServerUpdateEvent;
import rip.alpha.libraries.Libraries;
import rip.alpha.libraries.configuration.Configurations;

import java.util.function.Consumer;

public class AlphaCore {
    private static final AlphaCore instance = new AlphaCore();

    private boolean enabled = false;
    private AlphaCoreConfig alphaCoreConfig;
    private MongoDatabase alphaDatabase;

    public static void enable() {
        if (instance.enabled) {
            return;
        }

        instance.alphaCoreConfig = Configurations.computeIfAbsent(new AlphaCoreConfig());
        instance.alphaDatabase = Libraries.getMongoClient().getDatabase("alpha");
        AlphaCore.registerListener(NetworkQueueUpdateEvent.class, NetworkQueueSnapshotHandler::handleNetworkQueueUpdate);
        AlphaCore.registerListener(NetworkServerUpdateEvent.class, NetworkServerSnapshotHandler::handleNetworkServerUpdate);
        instance.enabled = true;
    }

    public static void disable() {
        if (!instance.enabled) {
            return;
        }

        RebootHandler.getInstance().shutdown();
        NetworkServerHandler.getInstance().disable();
        instance.enabled = false;
    }

    public static void callEvent(BridgeEvent event) {
        Libraries.getBridge().callEvent(event);
    }

    public static <M extends AlphaBridgeEvent> void registerListener(Class<M> clazz, MessageListener<M> listener) {
        Libraries.getBridge().registerListener(clazz, listener);
    }

    public static <M extends AlphaBridgeEvent> void registerListener(Class<M> clazz, Consumer<M> consumer) {
        registerListener(clazz, (sequence, message) -> consumer.accept(message));
    }

    public static RedissonClient getRedissonClient() {
        return Libraries.getRedissonClient();
    }

    public static MongoDatabase getDatabase() {
        return instance.alphaDatabase;
    }

    public static AlphaCoreConfig getConfig() {
        return instance.alphaCoreConfig;
    }
}
