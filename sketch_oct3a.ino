#include <WiFi.h>
#include <PubSubClient.h>
#include <WiFiClientSecure.h>
#include <time.h>
#include "DHT11Sensor.h"
#include "RGBLed.h"

// WiFi配置 - 请填写您的WiFi信息
const char* ssid = "Xiaomi17";
const char* password = "1472583690";

// MQTT服务器配置
const char* mqtt_server = "a4e4f08b.ala.cn-hangzhou.emqxsl.cn";
const uint16_t mqtt_port = 8883;

// MQTT认证信息（已添加）
const char* mqtt_username = "APP";      // 用户名
const char* mqtt_password = "APP2025";  // 密码

// MQTT客户端ID - 建议修改为唯一值（例如添加设备MAC后几位）
const char* clientId = "esp32_mqtt_test_001";
const char* device_uuid = "57ef098e-fb85-444a-8346-bf86abc45732";

// 订阅和发布的主题
const char* subscribeTopic = "esp32/test/sub";
const char* publishTopic = "esp32/test/pub";

// 创建WiFi和MQTT客户端
WiFiClientSecure espClient;
PubSubClient client(espClient);

const int DHT_PIN = 2;
DHT11Sensor dht(DHT_PIN);
const int GAS_PIN = 9;
// KY-003霍尔传感器
const int HALL_SENSOR_PIN = 12;

// RGB LED (red=42, green=40, blue=41)
RGBLed led(42, 40, 41);
int currentBrightness = 70; // 0..100
int baseR = 255, baseG = 255, baseB = 255; // default natural
String currentColorTemp = "natural";

// 上一次发布的传感器值，初始值设为-1表示尚未发布过
int lastPublishedTemp = -1;
int lastPublishedHumidity = -1;
int lastPublishedGasValue = -1;

// 霍尔传感器相关变量
bool currentHallState = HIGH;      // 当前霍尔传感器状态
bool lastConfirmedHallState = HIGH; // 上一次确认的霍尔传感器状态
bool lastPublishedHallState = HIGH; // 上一次发布的霍尔传感器状态
int confirmationCount = 0;         // 连续确认计数
const int requiredConfirmations = 3; // 需要的连续确认次数
unsigned long lastHallCheckTime = 0; // 上一次检查时间
const unsigned long hallCheckInterval = 100; // 检查间隔(ms)

static void applyOutput() {
  int r = (int)((float)baseR * currentBrightness / 100.0f);
  int g = (int)((float)baseG * currentBrightness / 100.0f);
  int b = (int)((float)baseB * currentBrightness / 100.0f);
  int rPWM = 255 - constrain(r, 0, 255);
  int gPWM = 255 - constrain(g, 0, 255);
  int bPWM = 255 - constrain(b, 0, 255);
  led.setColor(rPWM, gPWM, bPWM);
}

static void setColorTemp(const String &temp) {
  if (temp == "warm") { baseR = 255; baseG = 180; baseB = 80; }
  else if (temp == "cool") { baseR = 180; baseG = 220; baseB = 255; }
  else { baseR = 255; baseG = 255; baseB = 255; }
  currentColorTemp = temp;
  applyOutput();
}

static bool parseHexColor(const String &hex, int &r, int &g, int &b) {
  String h = hex; h.trim();
  if (h.startsWith("#")) h = h.substring(1);
  if (h.length() != 6) return false;
  char buf[7];
  h.substring(0, 6).toCharArray(buf, 7);
  unsigned long rgb = strtoul(buf, NULL, 16);
  r = (int)((rgb >> 16) & 0xFF);
  g = (int)((rgb >> 8) & 0xFF);
  b = (int)(rgb & 0xFF);
  return true;
}

static void publishLightStatus() {
  String dev = String(device_uuid);
  time_t now; time(&now);
  struct tm timeinfo; getLocalTime(&timeinfo);
  char iso[32];
  snprintf(iso, sizeof(iso), "%04d-%02d-%02dT%02d:%02d:%02d+08:00",
           timeinfo.tm_year + 1900, timeinfo.tm_mon + 1, timeinfo.tm_mday,
           timeinfo.tm_hour, timeinfo.tm_min, timeinfo.tm_sec);
  String topic = "smarthome/" + dev + "/in/status";
  String payload = String("{\"device_id\":\"") + dev +
                   "\",\"brightness_current\":" + String(currentBrightness) +
                   ",\"color_temp_current\":\"" + currentColorTemp +
                   "\",\"power_current\":\"ON\"" +
                   ",\"ts\":" + String((long)now) + ",\"timestamp\":\"" + String(iso) + "\"}";
  client.publish(topic.c_str(), payload.c_str());
}

// CA证书（保持不变）
const char* ca_cert = "-----BEGIN CERTIFICATE-----\n"
"MIIDjjCCAnagAwIBAgIQAzrx5qcRqaC7KGSxHQn65TANBgkqhkiG9w0BAQsFADBh\n"
"MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3\n"
"d3cuZGlnaWNlcnQuY29tMSAwHgYDVQQDExdEaWdpQ2VydCBHbG9iYWwgUm9vdCBH\n"
"MjAeFw0xMzA4MDExMjAwMDBaFw0zODAxMTUxMjAwMDBaMGExCzAJBgNVBAYTAlVT\n"
"MRUwEwYDVQQKEwxEaWdpQ2VydCBJbmMxGTAXBgNVBAsTEHd3dy5kaWdpY2VydC5j\n"
"b20xIDAeBgNVBAMTF0RpZ2lDZXJ0IEdsb2JhbCBSb290IEcyMIIBIjANBgkqhkiG\n"
"9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuzfNNNx7a8myaJCtSnX/RrohCgiN9RlUyfuI\n"
"2/Ou8jqJkTx65qsGGmvPrC3oXgkkRLpimn7Wo6h+4FR1IAWsULecYxpsMNzaHxmx\n"
"1x7e/dfgy5SDN67sH0NO3Xss0r0upS/kqbitOtSZpLYl6ZtrAGCSYP9PIUkY92eQ\n"
"q2EGnI/yuum06ZIya7XzV+hdG82MHauVBJVJ8zUtluNJbd134/tJS7SsVQepj5Wz\n"
"tCO7TG1F8PapspUwtP1MVYwnSlcUfIKdzXOS0xZKBgyMUNGPHgm+F6HmIcr9g+UQ\n"
"vIOlCsRnKPZzFBQ9RnbDhxSJITRNrw9FDKZJobq7nMWxM4MphQIDAQABo0IwQDAP\n"
"BgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBhjAdBgNVHQ4EFgQUTiJUIBiV\n"
"5uNu5g/6+rkS7QYXjzkwDQYJKoZIhvcNAQELBQADggEBAGBnKJRvDkhj6zHd6mcY\n"
"1Yl9PMWLSn/pvtsrF9+wX3N3KjITOYFnQoQj8kVnNeyIv/iPsGEMNKSuIEyExtv4\n"
"NeF22d+mQrvHRAiGfzZ0JFrabA0UWTW98kndth/Jsw1HKj2ZL7tcu7XUIOGZX1NG\n"
"Fdtom/DzMNU+MeKNhJ7jitralj41E6Vf8PlwUHBHQRFXGU7Aj64GxJUTFy8bJZ91\n"
"8rGOmaFvE7FBcf6IKshPECBV1/MUReXgRPTqh5Uykw7+U0b6LJ3/iyK5S9kJRaTe\n"
"pLiaWN0bfVKfjllDiIGknibVb63dDcY3fe0Dkhvld1927jyNxF1WW6LZZm6zNTfl\n"
"MrY=\n"
"-----END CERTIFICATE-----\n";

// MQTT消息回调函数
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("收到消息 [");
  Serial.print(topic);
  Serial.print("] ");
  
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
  // 解析控制命令
  String msg;
  for (unsigned int i = 0; i < length; i++) msg += (char)payload[i];
  String m = msg; m.toLowerCase();
  String cmd = "";
  {
    int cIdx = m.indexOf("\"command\"");
    if (cIdx >= 0) {
      int colon = m.indexOf(":", cIdx);
      if (colon >= 0) {
        int e1 = m.indexOf(",", colon + 1), e2 = m.indexOf("}", colon + 1);
        int e = (e1 >= 0 && e2 >= 0) ? min(e1, e2) : max(e1, e2);
        if (e < 0) e = m.length();
        cmd = m.substring(colon + 1, e);
        cmd.trim();
        if (cmd.startsWith("\"") && cmd.endsWith("\"")) cmd = cmd.substring(1, cmd.length() - 1);
      }
    }
  }
  bool isBrightness = (cmd == "brightness") || m.indexOf("brightness_set") >= 0;
  bool isColorTemp = (cmd == "color_temp") || m.indexOf("color_temp_set") >= 0;
  bool isColorSet = (cmd == "color_set") || m.indexOf("color_set") >= 0;
  bool isPower = (cmd == "power") || m.indexOf("\"power\"") >= 0 || m.indexOf("\"command\":\"power\"") >= 0;
  int idx = m.indexOf("\"value\"");
  String val = "";
  if (idx >= 0) {
    int colon = m.indexOf(":", idx);
    if (colon >= 0) {
      int e1 = m.indexOf(",", colon + 1), e2 = m.indexOf("}", colon + 1);
      int e = (e1 >= 0 && e2 >= 0) ? min(e1, e2) : max(e1, e2);
      if (e < 0) e = m.length();
      val = m.substring(colon + 1, e);
      val.trim();
      if (val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length() - 1);
    }
  }
  // 若顶层未取到，尝试从parameters.value取值
  if (val.length() == 0) {
    int pIdx = m.indexOf("\"parameters\"");
    if (pIdx >= 0) {
      int vIdx = m.indexOf("\"value\"", pIdx);
      if (vIdx >= 0) {
        int colon = m.indexOf(":", vIdx);
        if (colon >= 0) {
          int end1 = m.indexOf(",", colon + 1);
          int end2 = m.indexOf("}", colon + 1);
          int end = (end1 >= 0 && end2 >= 0) ? min(end1, end2) : max(end1, end2);
          if (end < 0) end = m.length();
          val = m.substring(colon + 1, end);
          val.trim();
          if (val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length() - 1);
        }
      }
    }
  }
  if (isBrightness) {
    int v = constrain(val.toInt(), 0, 100);
    currentBrightness = v;
    Serial.print("设置亮度: "); Serial.println(v);
    applyOutput();
    publishLightStatus();
  } else if (isColorTemp || isColorSet) {
    String v = val; v.trim();
    String lc = v; lc.toLowerCase();
    if (lc == "warm" || lc == "\u6696\u5149") { setColorTemp("warm"); }
    else if (lc == "cool" || lc == "\u51b7\u5149") { setColorTemp("cool"); }
    else if (lc == "natural" || lc == "\u81ea\u7136\u5149") { setColorTemp("natural"); }
    else {
      int r, g, b;
      if (parseHexColor(v, r, g, b)) {
        baseR = r; baseG = g; baseB = b;
        currentColorTemp = "custom";
        Serial.print("设置颜色: "); Serial.println(v);
        applyOutput();
        publishLightStatus();
      } else {
        setColorTemp(lc);
        publishLightStatus();
      }
    }
  } else if (isPower) {
    String v = val; v.trim(); v.toUpperCase();
    Serial.print("设置电源: "); Serial.println(v);
    if (v == "OFF") { led.setColor(255, 255, 255); }
    else { applyOutput(); }
    publishLightStatus();
  }
}

// 连接到WiFi
void connectWiFi() {
  Serial.print("连接到 ");
  Serial.println(ssid);
  
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println("");
  Serial.println("WiFi连接成功");
  Serial.println("IP地址: ");
  Serial.println(WiFi.localIP());
}

// 重连MQTT（已添加用户名密码认证）
void reconnect() {
  while (!client.connected()) {
    Serial.print("尝试连接MQTT服务器...");
    
    // 使用用户名和密码连接
  if (client.connect(clientId, mqtt_username, mqtt_password)) {
    Serial.println("连接成功");
    String cmdTopic = String("smarthome/") + device_uuid + "/out/control";
    client.subscribe(cmdTopic.c_str());
    Serial.print("已订阅主题: ");
    Serial.println(cmdTopic);
    {
      String testPayload = String("{\"command\":\"ping\",\"value\":\"test\"}");
      client.publish(cmdTopic.c_str(), testPayload.c_str());
    }
  } else {
      Serial.print("连接失败, 错误代码: ");
      Serial.print(client.state());
      Serial.println("，将在5秒后重试");
      delay(5000);
    }
  }
}

void setup() {
  Serial.begin(115200);
  
  espClient.setCACert(ca_cert);
  
  // 连接WiFi
  connectWiFi();
  
  // 配置MQTT服务器和端口
  client.setServer(mqtt_server, mqtt_port);
  client.setBufferSize(1024);
  client.setKeepAlive(60);
  
  // 设置回调函数
  client.setCallback(callback);

  dht.init();
  configTime(8 * 3600, 0, "ntp.aliyun.com");
  pinMode(GAS_PIN, INPUT);
  // 初始化霍尔传感器引脚
  pinMode(HALL_SENSOR_PIN, INPUT_PULLUP); // 设置引脚为上拉输入，稳定检测磁场
  // 初始化LED
  led.init();
  setColorTemp("natural");
  
  // 输出霍尔传感器初始化信息
  Serial.println("KY-003霍尔传感器初始化完成");
  Serial.println("传感器引脚: " + String(HALL_SENSOR_PIN));
  Serial.println("使用方法: 将磁铁靠近传感器，观察串口输出变化");
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  
  client.loop();
  
  // 霍尔传感器状态检测和确认逻辑
  unsigned long currentTime = millis();
  
  // 以100ms间隔检查传感器状态
  if (currentTime - lastHallCheckTime >= hallCheckInterval) {
    lastHallCheckTime = currentTime;
    
    // 读取当前传感器状态
    currentHallState = digitalRead(HALL_SENSOR_PIN);
    
    // 状态确认逻辑
    if (currentHallState == lastConfirmedHallState) {
      // 状态与上次确认的一致，重置确认计数
      confirmationCount = 0;
    } else {
      // 状态与上次确认的不同，增加确认计数
      confirmationCount++;
      
      // 如果达到所需的确认次数
      if (confirmationCount >= requiredConfirmations) {
        // 更新确认的状态
        lastConfirmedHallState = currentHallState;
        confirmationCount = 0;
        
        // 当状态变化且确认3次后，打印状态信息
        if (lastConfirmedHallState == HIGH) {
          Serial.println("[确认3次] 未检测到磁场！");
        } else {
          Serial.println("[确认3次] 检测到磁场");
        }
        
        // 仅在状态变化时发布MQTT消息
        if (lastConfirmedHallState != lastPublishedHallState) {
          String dev = String(device_uuid);
          time_t now; time(&now);
          struct tm timeinfo; getLocalTime(&timeinfo);
          char iso[32];
          snprintf(iso, sizeof(iso), "%04d-%02d-%02dT%02d:%02d:%02d+08:00",
                   timeinfo.tm_year + 1900, timeinfo.tm_mon + 1, timeinfo.tm_mday,
                   timeinfo.tm_hour, timeinfo.tm_min, timeinfo.tm_sec);
          
          // 构建主题和负载
          String topic = "smarthome/" + dev + "/in/sensor/hall";
          int hallValue = (lastConfirmedHallState == HIGH) ? 1 : 0; // HIGH: 磁体缺失(开) / LOW: 磁体检测(关)
          String payload = String("{\"device_id\":\"") + dev + "\",\"sensor_type\":\"hall\",\"value\":" + 
                          String(hallValue) + ",\"status\":\"" + 
                          (hallValue == 1 ? "magnet_missing" : "magnet_detected") + 
                          "\",\"ts\":" + String((long)now) + ",\"timestamp\":\"" + String(iso) + "\"}";
          
          Serial.print("发布霍尔传感器状态: ");
          Serial.println(payload);
          
          if (client.publish(topic.c_str(), payload.c_str())) {
            Serial.println("霍尔传感器状态发布成功");
            lastPublishedHallState = lastConfirmedHallState; // 更新上一次发布的状态
          } else {
            Serial.println("霍尔传感器状态发布失败");
          }
        }
      }
    }
  }
  
  static unsigned long lastPublishTime = 0;
  if (millis() - lastPublishTime > 5000) {
    lastPublishTime = millis();
    int h = dht.readHumidity();
    int t = dht.readTemperature();
    int gasRaw = analogRead(GAS_PIN);
    
    if (!dht.isDataValid(h, t)) {
      dht.printError(h, t);
    } else {
      String dev = String(device_uuid);
      time_t now; time(&now);
      struct tm timeinfo; getLocalTime(&timeinfo);
      char iso[32];
      snprintf(iso, sizeof(iso), "%04d-%02d-%02dT%02d:%02d:%02d+08:00",
               timeinfo.tm_year + 1900, timeinfo.tm_mon + 1, timeinfo.tm_mday,
               timeinfo.tm_hour, timeinfo.tm_min, timeinfo.tm_sec);
      
      // 仅在温度变化或首次发布时发布
      if (t != lastPublishedTemp) {
        String tTopic = "smarthome/" + dev + "/in/sensor/temperature";
        String tPayload = String("{\"device_id\":\"") + dev + "\",\"sensor_type\":\"temperature\",\"value\":" + String(t) + ",\"ts\":" + String((long)now) + ",\"timestamp\":\"" + String(iso) + "\"}";
        Serial.print("发布温度: ");
        Serial.println(tPayload);
        if (client.publish(tTopic.c_str(), tPayload.c_str())) {
          Serial.println("温度发布成功");
          lastPublishedTemp = t; // 更新上一次发布的温度值
        } else {
          Serial.println("温度发布失败");
        }
      }
      
      // 仅在湿度变化或首次发布时发布
      if (h != lastPublishedHumidity) {
        String hTopic = "smarthome/" + dev + "/in/sensor/humidity";
        String hPayload = String("{\"device_id\":\"") + dev + "\",\"sensor_type\":\"humidity\",\"value\":" + String(h) + ",\"ts\":" + String((long)now) + ",\"timestamp\":\"" + String(iso) + "\"}";
        Serial.print("发布湿度: ");
        Serial.println(hPayload);
        if (client.publish(hTopic.c_str(), hPayload.c_str())) {
          Serial.println("湿度发布成功");
          lastPublishedHumidity = h; // 更新上一次发布的湿度值
        } else {
          Serial.println("湿度发布失败");
        }
      }
      
      // 仅在气体传感器值变化或首次发布时发布
      if (gasRaw != lastPublishedGasValue) {
        float voltage = (float)gasRaw * 3.3f / 4095.0f;
        String gTopic = "smarthome/" + dev + "/in/sensor/gas";
        String gPayload = String("{\"device_id\":\"") + dev + "\",\"sensor_type\":\"gas\",\"value\":" + String(gasRaw) + ",\"unit\":\"adc\",\"voltage\":" + String(voltage, 2) + ",\"ts\":" + String((long)now) + ",\"timestamp\":\"" + String(iso) + "\"}";
        Serial.print("发布气体: ");
        Serial.println(gPayload);
        if (client.publish(gTopic.c_str(), gPayload.c_str())) {
          Serial.println("气体发布成功");
          lastPublishedGasValue = gasRaw; // 更新上一次发布的气体值
        } else {
          Serial.println("气体发布失败");
        }
      }
    }
  }
}