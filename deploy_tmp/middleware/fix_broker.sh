#!/usr/bin/env bash
set -euo pipefail

perl -pi -e 's|mqtts://z01b0909\.ala\.asia-southeast1\.emqxsl\.com:8883|mqtts://a4e4f08b.ala.cn-hangzhou.emqxsl.cn:8883|g' /root/mqtt-supabase-middleware/mqtt-client.js /root/mqtt-supabase-middleware/index.js
pm2 restart mqtt-supabase-middleware --update-env
sleep 3
echo "==== OUT LOG ===="
tail -n 120 /root/.pm2/logs/mqtt-supabase-middleware-out.log
echo "==== ERR LOG ===="
tail -n 80 /root/.pm2/logs/mqtt-supabase-middleware-error.log || true