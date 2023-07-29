package rip.alpha.core.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import rip.alpha.core.shared.AlphaCore;
import rip.alpha.core.shared.queue.event.PollQueueEvent;
import rip.alpha.core.shared.server.event.*;
import rip.alpha.core.velocity.listener.ConnectionListener;
import rip.alpha.core.velocity.listener.PlayerSessionListener;
import rip.alpha.core.velocity.permission.GrantPermissionListener;
import rip.alpha.core.velocity.queue.NetworkQueueListener;
import rip.alpha.core.velocity.queue.NetworkQueueMessageRunnable;
import rip.alpha.core.velocity.server.NetworkServerListener;
import rip.alpha.libraries.LibrariesPlugin;
import rip.alpha.libraries.logging.AlphaLogger;
import rip.alpha.libraries.logging.AlphaLoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Plugin(id = "core", name = "Core", version = "1.0.0")
public class Core {
    public static AlphaLogger LOGGER = AlphaLoggerFactory.createLogger("Core");

    @Getter
    private static Core instance;

    @Getter
    private final ProxyServer proxyServer;

    @Getter
    private final Logger logger;

    @Getter
    private final Path path;


    @Inject
    public Core(ProxyServer proxyServer, Logger logger, @DataDirectory Path path) {
        instance = this;
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.path = path;
        LibrariesPlugin.getInstance().enable(this, proxyServer);
        AlphaCore.enable();
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onProxyInitialize(ProxyInitializeEvent event) {
        this.registerEventListeners();
        this.registerBridgeListeners();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new NetworkQueueMessageRunnable(), 15L, 15L, TimeUnit.SECONDS);
        proxyServer.getCommandManager().unregister("shutdown");
        proxyServer.getCommandManager().unregister("send");

    }

    @Subscribe(order = PostOrder.LAST)
    public void onProxyInitialize(ProxyShutdownEvent event) {
        AlphaCore.disable();
        LibrariesPlugin.getInstance().disable();
    }

    private void registerEventListeners() {
        EventManager eventManager = this.proxyServer.getEventManager();
        eventManager.register(this, new NetworkServerListener());
        eventManager.register(this, new PlayerSessionListener());
        eventManager.register(this, new NetworkQueueListener());
        eventManager.register(this, new GrantPermissionListener());
        eventManager.register(this, new ConnectionListener());
    }

    private void registerBridgeListeners() {
        AlphaCore.registerListener(NetworkServerAddEvent.class, NetworkServerListener::handleAddServer);
        AlphaCore.registerListener(NetworkServerSendPlayerEvent.class, NetworkServerListener::handleSendPlayer);
        AlphaCore.registerListener(NetworkServerSendAllEvent.class, NetworkServerListener::handleSendAll);
        AlphaCore.registerListener(NetworkServerSendServerEvent.class, NetworkServerListener::handleSendServer);
        AlphaCore.registerListener(PollQueueEvent.class, NetworkQueueListener::handlePollQueue);
        AlphaCore.registerListener(NetworkServerREShutdownEvent.class, NetworkServerListener::handleREShutdown);
        AlphaCore.registerListener(NetworkServerRECommandEvent.class, NetworkServerListener::handleRECommand);
        AlphaCore.registerListener(NetworkServerREBroadcastEvent.class, NetworkServerListener::handleREBroadcast);
    }
}
