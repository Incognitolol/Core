package rip.alpha.core.voter.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.FastThreadLocalThread;
import rip.alpha.core.voter.VotifierCore;
import rip.alpha.core.voter.net.protocol.VoteInboundHandler;
import rip.alpha.core.voter.net.protocol.VotifierGreetingHandler;
import rip.alpha.core.voter.net.protocol.VotifierProtocolDifferentiator;
import rip.alpha.libraries.logging.LogLevel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;


public class VotifierBootstrap {
    private static final boolean USE_EPOLL = Epoll.isAvailable();
    public static final AttributeKey<VotifierCore> KEY = AttributeKey.valueOf("core_votifier");
    private final String host;
    private final int port;

    private final EventLoopGroup eventLoopGroup;
    private final EventLoopGroup bossLoopGroup;

    private Channel serverChannel;

    public VotifierBootstrap(String host, int port) {
        this.host = host;
        this.port = port;
        if(USE_EPOLL) {
            this.bossLoopGroup = new EpollEventLoopGroup(1, createThreadFactory("Votifier epoll boss"));
            this.eventLoopGroup = new EpollEventLoopGroup(3, createThreadFactory("Votifier epoll worker"));
            VotifierCore.LOGGER.log("Using epoll transport to accept votes.", LogLevel.DEBUG);
        } else {
            this.bossLoopGroup = new NioEventLoopGroup(1, createThreadFactory("Votifier NIO boss"));
            this.eventLoopGroup = new NioEventLoopGroup(3, createThreadFactory("Votifier NIO worker"));
            VotifierCore.LOGGER.log("Using NIO transport to accept votes.", LogLevel.DEBUG);
        }
    }

    private static ThreadFactory createThreadFactory(String name) {
        return runnable -> {
            FastThreadLocalThread thread = new FastThreadLocalThread(runnable, name);
            thread.setDaemon(true);
            return thread;
        };
    }

    public void start() {
        VoteInboundHandler voteInboundHandler = new VoteInboundHandler();
        new ServerBootstrap()
                .channel(USE_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .group(bossLoopGroup, eventLoopGroup)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.attr(VotifierSession.KEY).set(new VotifierSession());
                        channel.attr(KEY).set(VotifierCore.getInstance());
                        channel.pipeline().addLast("greetingHandler", VotifierGreetingHandler.INSTANCE);
                        channel.pipeline().addLast("protocolDifferentiator", new VotifierProtocolDifferentiator(false, true));
                        channel.pipeline().addLast("voteHandler", voteInboundHandler);
                    }
                })
                .bind(host, port)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        serverChannel = future.channel();
                        VotifierCore.LOGGER.log("enabled on socket " + serverChannel.localAddress() + ".", LogLevel.BASIC);
                    } else {
                        SocketAddress socketAddress = future.channel().localAddress();
                        if (socketAddress == null) {
                            socketAddress = new InetSocketAddress(host, port);
                        }

                        VotifierCore.LOGGER.severe("was not able to bind to " + socketAddress.toString(), LogLevel.BASIC);

                    }
                });
    }

    public void shutdown() {
        if (serverChannel != null) {
            try {
                serverChannel.close().syncUninterruptibly();
            } catch (Exception e) {
                e.printStackTrace();
              VotifierCore.LOGGER.log("Unable to shutdown server channel", LogLevel.BASIC);
            }
        }
        eventLoopGroup.shutdownGracefully();
        bossLoopGroup.shutdownGracefully();

        try {
            bossLoopGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            eventLoopGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
