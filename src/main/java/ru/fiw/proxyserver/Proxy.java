package ru.fiw.proxyserver;

import com.google.gson.annotations.SerializedName;

public class Proxy {

    public static final String FIELD_IP_PORT = "IP:PORT";
    public static final String ADDRESS_DELIMITER = ":";
    public static final int DEFAULT_PROXY_PORT = 1080;
    public static final String DEFAULT_EMPTY_STRING = "";

    @SerializedName(FIELD_IP_PORT)
    public String ipPort = DEFAULT_EMPTY_STRING;
    public ProxyType type = ProxyType.SOCKS5;
    public String username = DEFAULT_EMPTY_STRING;
    public String password = DEFAULT_EMPTY_STRING;

    public Proxy() {
    }

    public Proxy(boolean isSocks4, String ipPort, String username, String password) {
        this.type = isSocks4 ? ProxyType.SOCKS4 : ProxyType.SOCKS5;
        this.ipPort = ipPort != null ? ipPort : DEFAULT_EMPTY_STRING;
        this.username = username != null ? username : DEFAULT_EMPTY_STRING;
        this.password = password != null ? password : DEFAULT_EMPTY_STRING;
    }

    public int getPort() {
        if (ipPort == null || !ipPort.contains(ADDRESS_DELIMITER)) {
            return DEFAULT_PROXY_PORT;
        }
        String[] parts = ipPort.split(ADDRESS_DELIMITER, 2);
        if (parts.length < 2) {
            return DEFAULT_PROXY_PORT;
        }
        try {
            return Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return DEFAULT_PROXY_PORT;
        }
    }

    public String getIp() {
        if (ipPort == null || ipPort.isBlank()) {
            return DEFAULT_EMPTY_STRING;
        }
        if (!ipPort.contains(ADDRESS_DELIMITER)) {
            return ipPort.trim();
        }
        return ipPort.split(ADDRESS_DELIMITER, 2)[0].trim();
    }

    public enum ProxyType {
        SOCKS4,
        SOCKS5
    }
}
