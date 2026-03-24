package com.bikeparts.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Proxy;

@ConfigurationProperties(prefix = "scraping.proxy")
@Component
@Data
public class ProxyConfig {
    private boolean enabled = false;
    private String host;
    private int port = 3128;
    private String username;
    private String password;
    private ProxyType type = ProxyType.HTTP;

    public enum ProxyType { HTTP, SOCKS5 }

    public Proxy toProxy() {
        Proxy.Type javaType = (type == ProxyType.SOCKS5)
                ? Proxy.Type.SOCKS
                : Proxy.Type.HTTP;
        return new Proxy(javaType, new InetSocketAddress(host, port));
    }

    public boolean hasAuth() {
        return username != null && !username.isBlank();
    }
}