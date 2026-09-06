package ru.fiw.proxyserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final String DEFAULT_PLAYER_NAME = "";
    
    private static final String CONFIG_DIR_NAME = "config";
    private static final String CONFIG_FILE_NAME = "ProxyServerConfig.json";

    private static final String KEY_LAST_PLAYER_NAME = "lastPlayerName";
    private static final String KEY_PROXY_ENABLED = "proxy-enabled";
    private static final String KEY_PROXY = "proxy";
    private static final String KEY_ACCOUNTS = "accounts";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type ACCOUNTS_TYPE = new TypeToken<HashMap<String, Proxy>>() {}.getType();

    public static Map<String, Proxy> accounts = new HashMap<>();
    public static String lastPlayerName = DEFAULT_PLAYER_NAME;

    public static Path getConfigPath() {
        Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
        return gameDir.resolve(CONFIG_DIR_NAME).resolve(CONFIG_FILE_NAME);
    }

    public static void loadConfig() {
        Path configPath = getConfigPath();
        try {
            if (!Files.exists(configPath)) {
                saveConfig();
                return;
            }

            String configString = Files.readString(configPath, StandardCharsets.UTF_8);
            if (configString.isBlank()) {
                return;
            }

            JsonObject configJson = JsonParser.parseString(configString).getAsJsonObject();

            if (configJson.has(KEY_LAST_PLAYER_NAME)) {
                lastPlayerName = configJson.get(KEY_LAST_PLAYER_NAME).getAsString();
            }

            if (configJson.has(KEY_PROXY_ENABLED)) {
                ProxyServer.proxyEnabled = configJson.get(KEY_PROXY_ENABLED).getAsBoolean();
            }

            if (configJson.has(KEY_PROXY)) {
                ProxyServer.proxy = GSON.fromJson(configJson.get(KEY_PROXY), Proxy.class);
            }

            if (configJson.has(KEY_ACCOUNTS)) {
                accounts = GSON.fromJson(configJson.get(KEY_ACCOUNTS), ACCOUNTS_TYPE);
            }

            if (accounts == null) {
                accounts = new HashMap<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        Path configPath = getConfigPath();
        try {
            JsonObject configJson = new JsonObject();

            configJson.addProperty(KEY_LAST_PLAYER_NAME, lastPlayerName);
            configJson.addProperty(KEY_PROXY_ENABLED, ProxyServer.proxyEnabled);
            configJson.add(KEY_PROXY, GSON.toJsonTree(ProxyServer.proxy));
            configJson.add(KEY_ACCOUNTS, GSON.toJsonTree(accounts));

            if (configPath.getParent() != null) {
                Files.createDirectories(configPath.getParent());
            }

            Files.writeString(configPath, GSON.toJson(configJson), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
