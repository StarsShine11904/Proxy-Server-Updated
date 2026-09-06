package ru.fiw.proxyserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.ProxyConnectionEvent;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestPing {

    public static final int CONNECT_TIMEOUT_MS = 5000;
    private static final String HANDLER_PROXY = "test_proxy_handler";
    private static final String HANDLER_EVENT = "test_event_handler";

    private static final EventLoopGroup GROUP = new NioEventLoopGroup();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Proxy-Ping-Worker");
        thread.setDaemon(true);
        return thread;
    });

    public volatile Component statusComponent = Component.empty();
    public volatile String state = "";

    public void run(String targetIp, int targetPort, Proxy proxy) {
        EXECUTOR.submit(() -> {
            try {
                updateStatus(
                        Component.translatable("gui.proxyserver.status.connecting").withStyle(ChatFormatting.YELLOW)
                );

                InetSocketAddress proxyAddr = new InetSocketAddress(proxy.getIp(), proxy.getPort());

                Bootstrap b = new Bootstrap()
                        .group(GROUP)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                        .handler(new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel ch) {
                                ProxyHandler proxyHandler = createProxyHandler(proxy, proxyAddr);
                                if (proxyHandler != null) {
                                    ch.pipeline().addLast(HANDLER_PROXY, proxyHandler);
                                }

                                ch.pipeline().addLast(HANDLER_EVENT, new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                                        if (evt instanceof ProxyConnectionEvent) {
                                            updateStatus(
                                                    Component.translatable("gui.proxyserver.status.success").withStyle(ChatFormatting.GREEN)
                                            );
                                            ctx.close();
                                        }
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                        String errorMsg = cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName();
                                        updateStatus(
                                                Component.translatable("gui.proxyserver.status.failed", errorMsg).withStyle(ChatFormatting.RED)
                                        );
                                        ctx.close();
                                    }
                                });
                            }
                        });

                b.connect(targetIp, targetPort).sync();
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                updateStatus(
                        Component.translatable("gui.proxyserver.status.error", errorMsg).withStyle(ChatFormatting.RED)
                );
            }
        });
    }

    private ProxyHandler createProxyHandler(Proxy proxy, InetSocketAddress proxyAddr) {
        String username = proxy.username != null && !proxy.username.isEmpty() ? proxy.username : null;
        String password = proxy.password != null && !proxy.password.isEmpty() ? proxy.password : null;

        if (proxy.type == Proxy.ProxyType.SOCKS5) {
            return new Socks5ProxyHandler(proxyAddr, username, password);
        } else if (proxy.type == Proxy.ProxyType.SOCKS4) {
            return new Socks4ProxyHandler(proxyAddr, username);
        }
        return null;
    }

    private void updateStatus(Component component) {
        this.statusComponent = component;
        this.state = component.getString();
    }
}
