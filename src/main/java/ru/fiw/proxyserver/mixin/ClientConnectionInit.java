package ru.fiw.proxyserver.mixin;

import io.netty.channel.Channel;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fiw.proxyserver.Proxy;
import ru.fiw.proxyserver.ProxyServer;

import java.net.InetSocketAddress;

@Mixin(targets = "net/minecraft/network/Connection$1")
public class ClientConnectionInit {

    @Unique
    private static final String PROXY_HANDLER_NAME = "mod_proxy_handler";

    @Inject(method = "initChannel(Lio/netty/channel/Channel;)V", at = @At("HEAD"))
    private void onInitChannel(Channel channel, CallbackInfo ci) {
        if (ProxyServer.proxyEnabled && ProxyServer.proxy != null) {
            Proxy proxy = ProxyServer.proxy;
            ProxyServer.lastUsedProxy = proxy;

            ProxyHandler proxyHandler = createProxyHandler(proxy);
            if (proxyHandler != null) {
                channel.pipeline().addFirst(PROXY_HANDLER_NAME, proxyHandler);
            }
        } else {
            ProxyServer.lastUsedProxy = new Proxy();
        }

        updateMenuButtonLabel();
    }

    @Unique
    private ProxyHandler createProxyHandler(Proxy proxy) {
        InetSocketAddress proxyAddr = new InetSocketAddress(proxy.getIp(), proxy.getPort());
        String username = (proxy.username != null && !proxy.username.isEmpty()) ? proxy.username : null;
        String password = (proxy.password != null && !proxy.password.isEmpty()) ? proxy.password : null;

        if (proxy.type == Proxy.ProxyType.SOCKS5) {
            return new Socks5ProxyHandler(proxyAddr, username, password);
        } else if (proxy.type == Proxy.ProxyType.SOCKS4) {
            return new Socks4ProxyHandler(proxyAddr, username);
        }
        return null;
    }

    @Unique
    private void updateMenuButtonLabel() {
        if (ProxyServer.proxyMenuButton != null) {
            ProxyServer.proxyMenuButton.setMessage(
                    Component.translatable("gui.proxyserver.button.status", ProxyServer.getLastUsedProxyDisplayComponent())
            );
        }
    }
}
