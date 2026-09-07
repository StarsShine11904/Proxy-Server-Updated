package ru.fiw.proxyserver;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ProxyServer implements ModInitializer, ClientModInitializer {

    public static final String MOD_ID = "proxyserver";
    public static final String TRANSLATION_KEY_NONE = "gui.proxyserver.status.none";

    public static boolean proxyEnabled = false;
    public static Proxy proxy = new Proxy();
    public static Proxy lastUsedProxy = new Proxy();

    public static Button proxyMenuButton;

    public static String getLastUsedProxyIp() {
        if (lastUsedProxy == null || lastUsedProxy.ipPort == null || lastUsedProxy.ipPort.isBlank()) {
            return Component.translatable(TRANSLATION_KEY_NONE).getString();
        }
        String ip = lastUsedProxy.getIp();
        return ip.isBlank() ? Component.translatable(TRANSLATION_KEY_NONE).getString() : ip;
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

    @Override
    public void onInitializeClient() {
        Config.loadConfig();
    }
}
