#!/usr/bin/env bash
# Yerel başlatma: .env'den OPENROUTER_API_KEY okunur (git-dışı).
# Kullanım: ./baslat.sh   (önce .env dosyasına anahtarını yaz)
set -euo pipefail
cd "$(dirname "$0")"
if [ ! -f .env ]; then
  echo "HATA: .env yok. Şunu yap:  cp .env.ornek .env  ve anahtarını gir." >&2
  exit 1
fi
set -a; source .env; set +a
if [ -z "${OPENROUTER_API_KEY:-}" ] || [ "${OPENROUTER_API_KEY}" = "buraya-YENI-anahtarini-yaz" ]; then
  echo "HATA: .env icinde OPENROUTER_API_KEY ayarli degil (rotate edilmis yeni anahtar)." >&2
  exit 1
fi
exec ./mvnw spring-boot:run
