package com.example.smarthome.utils;

import android.util.Log;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class MqttBridge {
    private static final String TAG = "MqttBridge";
    private static final String BROKER = "ssl://a4e4f08b.ala.cn-hangzhou.emqxsl.cn:8883";
    private static final String USERNAME = "APP";
    private static final String PASSWORD = "APP2025";
    private static final String CLIENT_ID = "smarthome-app";

    private static final String CA_CERT = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDjjCCAnagAwIBAgIQAzrx5qcRqaC7KGSxHQn65TANBgkqhkiG9w0BAQsFADBh\n"
            + "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3\n"
            + "d3cuZGlnaWNlcnQuY29tMSAwHgYDVQQDExdEaWdpQ2VydCBHbG9iYWwgUm9vdCBH\n"
            + "MjAeFw0xMzA4MDExMjAwMDBaFw0zODAxMTUxMjAwMDBaMGExCzAJBgNVBAYTAlVT\n"
            + "MRUwEwYDVQQKEwxEaWdpQ2VydCBJbmMxGTAXBgNVBAsTEHd3dy5kaWdpY2VydC5j\n"
            + "b20xIDAeBgNVBAMTF0RpZ2lDZXJ0IEdsb2JhbCBSb290IEcyMIIBIjANBgkqhkiG\n"
            + "9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuzfNNNx7a8myaJCtSnX/RrohCgiN9RlUyfuI\n"
            + "2/Ou8jqJkTx65qsGGmvPrC3oXgkkRLpimn7Wo6h+4FR1IAWsULecYxpsMNzaHxmx\n"
            + "1x7e/dfgy5SDN67sH0NO3Xss0r0upS/kqbitOtSZpLYl6ZtrAGCSYP9PIUkY92eQ\n"
            + "q2EGnI/yuum06ZIya7XzV+hdG82MHauVBJVJ8zUtluNJbd134/tJS7SsVQepj5Wz\n"
            + "tCO7TG1F8PapspUwtP1MVYwnSlcUfIKdzXOS0xZKBgyMUNGPHgm+F6HmIcr9g+UQ\n"
            + "vIOlCsRnKPZzFBQ9RnbDhxSJITRNrw9FDKZJobq7nMWxM4MphQIDAQABo0IwQDAP\n"
            + "BgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBhjAdBgNVHQ4EFgQUTiJUIBiV\n"
            + "5uNu5g/6+rkS7QYXjzkwDQYJKoZIhvcNAQELBQADggEBAGBnKJRvDkhj6zHd6mcY\n"
            + "1Yl9PMWLSn/pvtsrF9+wX3N3KjITOYFnQoQj8kVnNeyIv/iPsGEMNKSuIEyExtv4\n"
            + "NeF22d+mQrvHRAiGfzZ0JFrabA0UWTW98kndth/Jsw1HKj2ZL7tcu7XUIOGZX1NG\n"
            + "Fdtom/DzMNU+MeKNhJ7jitralj41E6Vf8PlwUHBHQRFXGU7Aj64GxJUTFy8bJZ91\n"
            + "8rGOmaFvE7FBcf6IKshPECBV1/MUReXgRPTqh5Uykw7+U0b6LJ3/iyK5S9kJRaTe\n"
            + "pLiaWN0bfVKfjllDiIGknibVb63dDcY3fe0Dkhvld1927jyNxF1WW6LZZm6zNTfl\n"
            + "MrY=\n"
            + "-----END CERTIFICATE-----\n";

    private static MqttBridge instance;
    private MqttClient client;
    private SSLSocketFactory sslFactory;

    public static synchronized MqttBridge getInstance() {
        if (instance == null) instance = new MqttBridge();
        return instance;
    }

    private SSLSocketFactory buildSocketFactory() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate ca = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(CA_CERT.getBytes(StandardCharsets.UTF_8)));
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("ca", ca);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        ctx.init(null, tmf.getTrustManagers(), new SecureRandom());
        return ctx.getSocketFactory();
    }

    private String brokerHost() {
        try {
            String s = BROKER.replace("ssl://", "");
            int idx = s.indexOf(":");
            return idx > 0 ? s.substring(0, idx) : s;
        } catch (Exception e) { return "a4e4f08b.ala.cn-hangzhou.emqxsl.cn"; }
    }

    static class SniSSLSocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;
        private final String host;
        SniSSLSocketFactory(SSLSocketFactory d, String h){ this.delegate=d; this.host=h; }
        private java.net.Socket withSni(java.net.Socket s) throws java.io.IOException {
            if (s instanceof javax.net.ssl.SSLSocket) {
                javax.net.ssl.SSLSocket ss = (javax.net.ssl.SSLSocket) s;
                javax.net.ssl.SSLParameters params = ss.getSSLParameters();
                java.util.List<javax.net.ssl.SNIServerName> sni = java.util.Collections.<javax.net.ssl.SNIServerName>singletonList(new javax.net.ssl.SNIHostName(host));
                try { params.setServerNames(sni); } catch (Throwable ignored) {}
                try { ss.setEnabledProtocols(new String[]{"TLSv1.3","TLSv1.2"}); } catch (Throwable ignored) {}
                ss.setSSLParameters(params);
            }
            return s;
        }
        public String[] getDefaultCipherSuites(){ return delegate.getDefaultCipherSuites(); }
        public String[] getSupportedCipherSuites(){ return delegate.getSupportedCipherSuites(); }
        public java.net.Socket createSocket() throws java.io.IOException { return withSni(delegate.createSocket()); }
        public java.net.Socket createSocket(java.net.Socket s, String host, int port, boolean autoClose) throws java.io.IOException { return withSni(delegate.createSocket(s, host, port, autoClose)); }
        public java.net.Socket createSocket(String host, int port) throws java.io.IOException { return withSni(delegate.createSocket(host, port)); }
        public java.net.Socket createSocket(String host, int port, java.net.InetAddress localHost, int localPort) throws java.io.IOException { return withSni(delegate.createSocket(host, port, localHost, localPort)); }
        public java.net.Socket createSocket(java.net.InetAddress host, int port) throws java.io.IOException { return withSni(delegate.createSocket(host, port)); }
        public java.net.Socket createSocket(java.net.InetAddress address, int port, java.net.InetAddress localAddress, int localPort) throws java.io.IOException { return withSni(delegate.createSocket(address, port, localAddress, localPort)); }
    }

    private java.util.concurrent.CopyOnWriteArrayList<LightStatusListener> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    public interface LightStatusListener {
        void onLightStatus(String deviceId, int brightness, String colorTemp, String power);
    }

    public void addLightStatusListener(LightStatusListener l){ if (!listeners.contains(l)) listeners.add(l); }
    public void removeLightStatusListener(LightStatusListener l){ listeners.remove(l); }

    private synchronized void ensureConnected() throws Exception {
        if (client != null && client.isConnected()) return;
        if (sslFactory == null) sslFactory = buildSocketFactory();
        if (client == null) client = new MqttClient(BROKER, CLIENT_ID, new MemoryPersistence());
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setUserName(USERNAME);
        opts.setPassword(PASSWORD.toCharArray());
        opts.setKeepAliveInterval(60);
        opts.setConnectionTimeout(30);
        opts.setSocketFactory(sslFactory);
        opts.setAutomaticReconnect(true);
        try { opts.setServerURIs(new String[]{BROKER}); } catch (Exception ignored) {}
        client.setCallback(new MqttCallback() {
            public void connectionLost(Throwable cause) { try { Log.e(TAG, "connectionLost", cause); } catch (Exception ignored) {} }
            public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
                try {
                    try { Log.d(TAG, "arrived topic=" + topic + " payload=" + new String(message.getPayload(), StandardCharsets.UTF_8)); } catch (Exception ignored) {}
                    String t = topic==null?"":topic;
                    if (t.contains("/in/status")) {
                        String s = new String(message.getPayload(), StandardCharsets.UTF_8);
                        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(s).getAsJsonObject();
                        String deviceId = obj.has("device_id")?obj.get("device_id").getAsString():"";
                        int b = obj.has("brightness_current")?obj.get("brightness_current").getAsInt():-1;
                        String c = obj.has("color_temp_current")?obj.get("color_temp_current").getAsString():null;
                        String p = obj.has("power_current")?obj.get("power_current").getAsString():null;
                        for (LightStatusListener l:listeners){ try { l.onLightStatus(deviceId, b, c, p);} catch(Exception ignored){} }
                    }
                } catch (Exception ignored) {}
            }
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        try {
            client.connect(opts);
            try { Log.d(TAG, "connected to broker " + BROKER + " as " + CLIENT_ID); } catch (Exception ignored) {}
        } catch (Exception e) {
            try { Log.e(TAG, "connect failed: " + e.getMessage()); Log.e(TAG, android.util.Log.getStackTraceString(e)); } catch (Exception ignored) {}
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init((KeyStore) null);
                ctx.init(null, tmf.getTrustManagers(), new SecureRandom());
                SSLSocketFactory fallback = ctx.getSocketFactory();
                MqttConnectOptions alt = new MqttConnectOptions();
                alt.setUserName(USERNAME);
                alt.setPassword(PASSWORD.toCharArray());
                alt.setKeepAliveInterval(60);
                alt.setConnectionTimeout(30);
                alt.setSocketFactory(fallback);
                alt.setAutomaticReconnect(true);
                client.connect(alt);
                try { Log.d(TAG, "connected to broker with default trust store"); } catch (Exception ignored2) {}
            } catch (Exception e2) {
                try { Log.e(TAG, "fallback connect failed: " + e2.getMessage()); Log.e(TAG, android.util.Log.getStackTraceString(e2)); } catch (Exception ignored3) {}
                throw e;
            }
        }
        try { client.subscribe("smarthome/+/in/status", 1); } catch (Exception ignored) {}
        try { client.subscribe("smarthome/+/out/control", 0); } catch (Exception ignored) {}
    }

    public void connectAsync() {
        new Thread(() -> {
            try { ensureConnected(); } catch (Exception ignored) {}
        }, "mqtt-connect").start();
    }

    public boolean publishControl(String deviceId, String json) {
        try {
            ensureConnected();
            String topic = "smarthome/" + deviceId + "/out/control";
            try { Log.d(TAG, "publish topic=" + topic + " payload=" + json); } catch (Exception ignored) {}
            MqttMessage msg = new MqttMessage(json.getBytes(StandardCharsets.UTF_8));
            msg.setQos(0);
            msg.setRetained(false);
            client.publish(topic, msg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConnected(){ return client != null && client.isConnected(); }
}