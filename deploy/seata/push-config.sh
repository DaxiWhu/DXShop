#!/bin/sh
# 把 Seata 统一配置发布到 Nacos（dataId=seataServer.properties, group=SEATA_GROUP）。
# 由 docker-compose 的一次性服务 seata-nacos-config 执行；成功后 seata / 各应用才启动。
set -eu

NACOS_ADDR="${NACOS_ADDR:-nacos:8848}"
GROUP="${SEATA_GROUP:-SEATA_GROUP}"
DATA_ID="${SEATA_DATA_ID:-seataServer.properties}"
NACOS_USER="${NACOS_USER:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"
SRC="/seata/seataServer.properties"
TMP="/tmp/seataServer.properties"

echo "[seata-config] waiting for nacos at ${NACOS_ADDR} ..."
i=0
until curl -sf "http://${NACOS_ADDR}/nacos/v1/console/health/readiness" >/dev/null 2>&1 \
   || curl -sf "http://${NACOS_ADDR}/nacos/v1/console/server/state"      >/dev/null 2>&1 \
   || curl -sf "http://${NACOS_ADDR}/nacos/health"                       >/dev/null 2>&1; do
  i=$((i+1))
  if [ "$i" -gt 60 ]; then echo "[seata-config] nacos not ready after 180s, abort"; exit 1; fi
  sleep 3
done
echo "[seata-config] nacos is ready."

# 用运行时变量替换占位符（数据库主机/端口/密码）
sed -e "s|__MYSQL_HOST__|${MYSQL_HOST:-mysql}|g" \
    -e "s|__MYSQL_PORT__|${MYSQL_PORT:-3306}|g" \
    -e "s|__MYSQL_PASSWORD__|${MYSQL_ROOT_PASSWORD:-123456}|g" \
    "$SRC" > "$TMP"

# 若 Nacos 开启鉴权则登录取 accessToken；未开启时该请求失败/为空，直接匿名发布
TOKEN="$(curl -s -X POST "http://${NACOS_ADDR}/nacos/v1/auth/login" \
          -d "username=${NACOS_USER}&password=${NACOS_PASSWORD}" 2>/dev/null \
          | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')"

URL="http://${NACOS_ADDR}/nacos/v1/cs/configs"
if [ -n "${TOKEN}" ]; then
  URL="${URL}?accessToken=${TOKEN}"
  echo "[seata-config] nacos auth enabled, got accessToken."
else
  echo "[seata-config] no accessToken (auth likely disabled), publishing anonymously."
fi

echo "[seata-config] publishing dataId=${DATA_ID} group=${GROUP} ..."
HTTP="$(curl -s -o /tmp/resp.txt -w '%{http_code}' -X POST "${URL}" \
  --data-urlencode "dataId=${DATA_ID}" \
  --data-urlencode "group=${GROUP}" \
  --data-urlencode "type=properties" \
  --data-urlencode "content@${TMP}")"

echo "[seata-config] HTTP ${HTTP}, response: $(cat /tmp/resp.txt 2>/dev/null)"
if [ "${HTTP}" != "200" ]; then
  echo "[seata-config] publish FAILED"
  exit 1
fi
echo "[seata-config] Seata config published to Nacos successfully."
