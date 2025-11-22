1.项目采用前端Android应用，后端使用supabase。
2.git提交远程仓库时，不提交文档、以及密钥文件C:\Users\Administrator\AndroidStudioProjects\Smarthome\ecs.pem。
3.每个任务完成后，都在任务清单中标记为完成。
4.当完成的任务涉及到配置，需要在任务清单中记录下配置的内容。
5.Project URL:https://znarfgnwmbsawgndeuzh.supabase.co
//用于查询和管理数据库的RESTful端点
6.Publishable key:sb_publishable_MMGYn93wCO4nsFuAWIzWNw_IaFHMO4W
## 服务器配置
**服务器地址**：8.134.63.151
**服务器系统**：ubuntu 20.04
**密钥文件**：c:\Users\Administrator\AndroidStudioProjects\Smarthome\ecs.pem

数据库表结构：
create table public.sensor_data (
  id uuid not null default gen_random_uuid (),
  device_id uuid not null,
  sensor_type character varying not null,
  value double precision not null default '0'::double precision,
  unit character varying null,
  is_alert boolean not null default false,
  timestamp timestamp with time zone null default now(),
  created_at timestamp with time zone null default now(),
  constraint sensor_data_pkey primary key (id)
) TABLESPACE pg_default;

create index IF not exists idx_sensor_data_d_s_t on public.sensor_data using btree (device_id, sensor_type, "timestamp" desc) TABLESPACE pg_default;

create table public.sensor_latest (
  id uuid not null default gen_random_uuid (),
  device_id uuid not null,
  sensor_type text not null,
  value numeric not null,
  unit text null,
  is_alert boolean not null default false,
  timestamp timestamp with time zone not null,
  updated_at timestamp with time zone not null default now(),
  constraint sensor_latest_pkey primary key (id),
  constraint sensor_latest_device_id_sensor_type_key unique (device_id, sensor_type)
) TABLESPACE pg_default;

create index IF not exists idx_sensor_latest_s_t on public.sensor_latest using btree (sensor_type, "timestamp" desc) TABLESPACE pg_default;

CA证书：
-----BEGIN CERTIFICATE-----
MIIDjjCCAnagAwIBAgIQAzrx5qcRqaC7KGSxHQn65TANBgkqhkiG9w0BAQsFADBh
MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3
d3cuZGlnaWNlcnQuY29tMSAwHgYDVQQDExdEaWdpQ2VydCBHbG9iYWwgUm9vdCBH
MjAeFw0xMzA4MDExMjAwMDBaFw0zODAxMTUxMjAwMDBaMGExCzAJBgNVBAYTAlVT
MRUwEwYDVQQKEwxEaWdpQ2VydCBJbmMxGTAXBgNVBAsTEHd3dy5kaWdpY2VydC5j
b20xIDAeBgNVBAMTF0RpZ2lDZXJ0IEdsb2JhbCBSb290IEcyMIIBIjANBgkqhkiG
9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuzfNNNx7a8myaJCtSnX/RrohCgiN9RlUyfuI
2/Ou8jqJkTx65qsGGmvPrC3oXgkkRLpimn7Wo6h+4FR1IAWsULecYxpsMNzaHxmx
1x7e/dfgy5SDN67sH0NO3Xss0r0upS/kqbitOtSZpLYl6ZtrAGCSYP9PIUkY92eQ
q2EGnI/yuum06ZIya7XzV+hdG82MHauVBJVJ8zUtluNJbd134/tJS7SsVQepj5Wz
tCO7TG1F8PapspUwtP1MVYwnSlcUfIKdzXOS0xZKBgyMUNGPHgm+F6HmIcr9g+UQ
vIOlCsRnKPZzFBQ9RnbDhxSJITRNrw9FDKZJobq7nMWxM4MphQIDAQABo0IwQDAP
BgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBhjAdBgNVHQ4EFgQUTiJUIBiV
5uNu5g/6+rkS7QYXjzkwDQYJKoZIhvcNAQELBQADggEBAGBnKJRvDkhj6zHd6mcY
1Yl9PMWLSn/pvtsrF9+wX3N3KjITOYFnQoQj8kVnNeyIv/iPsGEMNKSuIEyExtv4
NeF22d+mQrvHRAiGfzZ0JFrabA0UWTW98kndth/Jsw1HKj2ZL7tcu7XUIOGZX1NG
Fdtom/DzMNU+MeKNhJ7jitralj41E6Vf8PlwUHBHQRFXGU7Aj64GxJUTFy8bJZ91
8rGOmaFvE7FBcf6IKshPECBV1/MUReXgRPTqh5Uykw7+U0b6LJ3/iyK5S9kJRaTe
pLiaWN0bfVKfjllDiIGknibVb63dDcY3fe0Dkhvld1927jyNxF1WW6LZZm6zNTfl
MrY=
-----END CERTIFICATE-----
