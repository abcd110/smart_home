package com.smarthome.mqtt.config;

import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

/**
 * MQTT配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttConfig {
    
    private String brokerUrl = "tcp://localhost:1883";
    private String username;
    private String password;
    private String clientId = "smarthome-mqtt-server";
    private int connectionTimeout = 30;
    private int keepAliveInterval = 60;
    private boolean cleanSession = true;
    private boolean automaticReconnect = true;
    
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        
        options.setServerURIs(new String[]{brokerUrl});
        if (username != null && !username.isEmpty()) {
            options.setUserName(username);
        }
        if (password != null && !password.isEmpty()) {
            options.setPassword(password.toCharArray());
        }
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setCleanSession(cleanSession);
        options.setAutomaticReconnect(automaticReconnect);
        
        factory.setConnectionOptions(options);
        return factory;
    }
}