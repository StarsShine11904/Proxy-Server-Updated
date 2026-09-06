package ru.fiw.proxyserver;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ProxyServer implements ModInitializer {

    public static final String MOD_ID = "proxyserver";
    public static final String DEFAULT_NONE_IP = "none";
    public static final String TRANSLATION_KEY_NONE = "gui.proxyserver.status.none";

    public static boolean proxyEnabled = false;
    public static Proxy proxy = new Proxy();
    public static Proxy lastUsedProxy = new Proxy();

    public static Button proxyMenuButton;

    public static String getLastUsedProxyIp() {
        if (lastUsedProxy == null || lastUsedProxy.ipPort == null || lastUsedProxy.ipPort.isBlank()) {
            return DEFAULT_NONE_IP;
        }
        String ip = lastUsedProxy.getIp();
        return ip.isBlank() ? DEFAULT_NONE_IP : ip;
    }

    public static Component getLastUsedProxyDisplayComponent() {
        if (lastUsedProxy == null || lastUsedProxy.ipPort == null || lastUsedProxy.ipPort.isBlank()) {
            return Component.translatable(TRANSLATION_KEY_NONE);
        }
        String ip = lastUsedProxy.getIp();
        return ip.isBlank() ? Component.translatable(TRANSLATION_KEY_NONE) : Component.literal(ip);
    }

    @Override
    public void onInitialize() {
        Config.loadConfig();
    }
}
