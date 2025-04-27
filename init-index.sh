#!/bin/bash
set -e

ES_HOST="${ES_HOST}"
INDEX_NAME="${INDEX_NAME}"

echo "🌍 Elasticsearch 호스트: $ES_HOST"
echo "📦 인덱스 이름: $INDEX_NAME"
echo "📝 설정 JSON 경로: ${INDEX_CONFIG_PATH:-/usr/share/logstash/index-config.json}"

# Elasticsearch 기동 대기
echo "⌛ Elasticsearch 기동 대기 중..."
until curl -s --head --fail "$ES_HOST" > /dev/null; do
  echo "❗ Elasticsearch 아직 준비되지 않음. 2초 후 재시도..."
  sleep 2
done

# 인덱스 존재 여부 확인
echo "🔍 인덱스 존재 여부 확인: $INDEX_NAME"
if curl -s --head --fail "$ES_HOST/$INDEX_NAME" > /dev/null; then
  echo "⚠️  인덱스 '$INDEX_NAME' 이미 존재합니다. 초기 인덱싱을 생략합니다."
  exit 0
fi

# 기존 인덱스 삭제 (예외 무시)
echo "🧹 기존 인덱스 삭제 시도 (존재하지 않아도 무시됨)"
curl -X DELETE "$ES_HOST/$INDEX_NAME" -s -o /dev/null || true

# 설정 JSON 존재 확인
CONFIG_PATH="${INDEX_CONFIG_PATH:-/usr/share/logstash/index-config.json}"
if [ ! -f "$CONFIG_PATH" ]; then
  echo "❌ 설정 파일이 존재하지 않습니다: $CONFIG_PATH"
  exit 1
fi

# 파일 내용이 비어있는지 확인
if [ ! -s "$CONFIG_PATH" ]; then
  echo "❌ 설정 파일이 비어 있습니다: $CONFIG_PATH"
  exit 1
fi

# 인덱스 설정 및 매핑 등록
echo "📦 인덱스 설정 및 매핑 등록"
curl -v -X PUT "$ES_HOST/$INDEX_NAME" \
     -H "Content-Type: application/json" \
     -d @"$CONFIG_PATH"

# Logstash 실행
echo "🚀 Logstash 실행으로 초기 인덱싱 시작"
/usr/share/logstash/bin/logstash -f /usr/share/logstash/pipelines/logstash.conf

echo "✅ 초기 인덱싱 완료"