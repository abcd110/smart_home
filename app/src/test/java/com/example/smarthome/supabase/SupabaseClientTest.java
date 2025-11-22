package com.example.smarthome.supabase;

import org.junit.Test;
import static org.junit.Assert.*;

public class SupabaseClientTest {
    @Test
    public void buildLatestSensorUrl_temperature_includesAliases() {
        String url = SupabaseClient.buildLatestSensorUrl("temperature");
        assertTrue(url.contains("sensor_type=in.("));
        assertTrue(url.contains("temperature"));
        assertTrue(url.contains("%E6%B8%A9%E5%BA%A6"));
    }

    @Test
    public void buildLatestSensorUrl_humidity_includesAliases() {
        String url = SupabaseClient.buildLatestSensorUrl("humidity");
        assertTrue(url.contains("humidity"));
        assertTrue(url.contains("%E6%B9%BF%E5%BA%A6"));
    }

    @Test
    public void buildLatestSensorUrl_gas_includesAliases() {
        String url = SupabaseClient.buildLatestSensorUrl("gas");
        assertTrue(url.contains("gas"));
        assertTrue(url.contains("%E5%8F%AF%E7%87%83%E6%B0%94%E4%BD%93"));
        assertTrue(url.contains("%E7%85%A4%E6%B0%94%E6%B5%93%E5%BA%A6"));
    }
}
