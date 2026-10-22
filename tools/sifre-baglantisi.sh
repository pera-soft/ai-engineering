#!/usr/bin/env bash
# Site sorumlusu için TEK KULLANIMLIK şifre sıfırlama bağlantısı üretir (ADR-0015, ekip kodu modu).
# E-posta gönderilmez: bağlantıyı kullanıcıya güvenli bir kanaldan (ör. şirket içi mesaj) sen iletirsin.
#
# Kullanım (uygulamanın çalıştığı SUNUCUDA; saat dilimi uygulamayla aynı olmalı):
#   sudo bash tools/sifre-baglantisi.sh ad.soyad@pera.software /etc/ai-ogretmen/site.env
#   bash tools/sifre-baglantisi.sh ad.soyad@pera.software          # değişkenler ortamdan
#
# Gerekenler: psql; SPRING_DATASOURCE_URL (jdbc:postgresql://...), SPRING_DATASOURCE_USERNAME,
# SPRING_DATASOURCE_PASSWORD ve APP_SITE_TEMEL_URL — env dosyası verilirse ondan, yoksa ortamdan.
#
# Java ile BİREBİR aynı biçim (service/KimlikService):
#   token  = 32 rastgele bayt, base64url, dolgusuz (43 karakter) — yalnız bağlantıda yaşar
#   özet   = SHA-256(token'ın UTF-8 baytları), küçük harf hex → kullanici_token.token_hash
#   tür    = SIFRE_SIFIRLAMA, son_gecerlilik = şimdi + 1 saat (uygulamanın yerel saatiyle; TIMESTAMP)
#   hesabın önceki sıfırlama token'ları silinir (eski bağlantılar geçersiz) — tek kullanımlıktır.
# Yalnız VAR, etkin (etkin=true) ve doğrulanmış hesaba üretilir. Şifre/token ekrana yazılmaz; çıktı
# (stdout) yalnız bağlantıdır.
set -euo pipefail

kullanim() {
  echo "Kullanım: $0 <ad.soyad@pera.software> [env-dosyasi]" >&2
  exit 2
}
hata() {
  echo "HATA: $*" >&2
  exit 1
}

[ $# -ge 1 ] && [ $# -le 2 ] || kullanim

# Java normallestir(): kırp + küçük harf (Locale.ROOT — yalnız ASCII; Türkçe İ/ı dönüşümü yok)
email=$(printf '%s' "$1" | LC_ALL=C tr -d '[:space:]' | LC_ALL=C tr 'A-Z' 'a-z')
[[ "$email" =~ ^[a-z0-9._%+-]{1,64}@pera\.software$ ]] || hata "geçersiz adres: yalnız @pera.software hesapları"

# systemd EnvironmentFile satırı: ANAHTAR=değer (source EDİLMEZ — değerlerde boşluk/< > olabilir)
dosyadan() {  # $1 anahtar, $2 dosya
  local satir deger
  satir=$(grep -E "^[[:space:]]*$1=" "$2" | tail -n 1 || true)
  [ -n "$satir" ] || return 0
  deger=${satir#*=}
  deger=${deger%$'\r'}
  if [[ "$deger" =~ ^\"(.*)\"$ ]] || [[ "$deger" =~ ^\'(.*)\'$ ]]; then
    deger=${BASH_REMATCH[1]}
  fi
  printf '%s' "$deger"
}
if [ $# -eq 2 ]; then
  [ -r "$2" ] || hata "env dosyası okunamıyor: $2 (sudo gerekebilir)"
  for anahtar in SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD APP_SITE_TEMEL_URL; do
    deger=$(dosyadan "$anahtar" "$2")
    if [ -n "$deger" ]; then
      printf -v "$anahtar" '%s' "$deger"
    fi
  done
fi

jdbc_url=${SPRING_DATASOURCE_URL:-}
db_kullanici=${SPRING_DATASOURCE_USERNAME:-}
db_sifre=${SPRING_DATASOURCE_PASSWORD:-}
temel_url=${APP_SITE_TEMEL_URL:-}
[ -n "$jdbc_url" ] || hata "SPRING_DATASOURCE_URL yok (ortamda ya da env dosyasında)"
[[ "$jdbc_url" == jdbc:postgresql://* ]] || hata "SPRING_DATASOURCE_URL jdbc:postgresql://... olmalı (betik yalnız PostgreSQL)"
[ -n "$temel_url" ] || hata "APP_SITE_TEMEL_URL yok (ör. https://egitim.pera.software)"
temel_url=${temel_url%/}
command -v psql >/dev/null || hata "psql bulunamadı (postgresql-client kur)"

# jdbc:postgresql://host:port/db?sslmode=... → libpq URI (yalnız libpq'nun bildiği parametreler geçer)
baglanti="postgresql://${jdbc_url#jdbc:postgresql://}"

sql() {  # stdin: SQL; değişkenler psql -v ile (:'ad' → güvenli tırnaklama, SQL enjeksiyonu yok)
  local ortam=()
  [ -n "$db_kullanici" ] && ortam+=("PGUSER=$db_kullanici")
  [ -n "$db_sifre" ] && ortam+=("PGPASSWORD=$db_sifre")
  env ${ortam[@]+"${ortam[@]}"} psql "$baglanti" -X -q -t -A -v ON_ERROR_STOP=1 "$@"
}

durum=$(sql -v email="$email" <<'SQL'
SELECT CASE WHEN NOT etkin THEN 'kapali' WHEN NOT dogrulandi THEN 'dogrulanmamis' ELSE 'tamam' END
  FROM kullanici WHERE email = :'email';
SQL
)
case "$durum" in
  tamam) ;;
  kapali) hata "$email hesabı kapalı (etkin=false) — bağlantı üretilmedi" ;;
  dogrulanmamis) hata "$email hesabı doğrulanmamış — önce doğrula (UPDATE kullanici SET dogrulandi = TRUE ...)" ;;
  "") hata "$email adında hesap yok — kullanıcı önce ekip koduyla kayıt olmalı" ;;
  *) hata "beklenmeyen veritabanı yanıtı" ;;
esac

# Token + özet (KimlikService.tokenUret/ozet ile aynı). printf kabuk iç komutu → token süreç listesinde görünmez.
token=$(head -c 32 /dev/urandom | base64 | tr -d '\n=' | tr '+/' '-_')
[ ${#token} -eq 43 ] || hata "token üretilemedi"
if command -v sha256sum >/dev/null; then
  ozet=$(printf '%s' "$token" | sha256sum | cut -d' ' -f1)
else
  ozet=$(printf '%s' "$token" | shasum -a 256 | cut -d' ' -f1)
fi
[[ "$ozet" =~ ^[0-9a-f]{64}$ ]] || hata "SHA-256 özeti üretilemedi"

# Uygulama Instant'ı TIMESTAMP (saat dilimsiz) kolona JVM'in YEREL saatiyle yazar/okur → aynı yerel saat:
# şimdiki UTC farkı (date +%z, ör. +0300) ile now() yerel duvar saatine çevrilir.
z=$(date +%z)
fark="${z:0:3}:${z:3:2}"

sonuc=$(sql -v email="$email" -v ozet="$ozet" -v fark="$fark" <<'SQL'
BEGIN;
DELETE FROM kullanici_token
 WHERE tur = 'SIFRE_SIFIRLAMA' AND hesap_id = (SELECT id FROM kullanici WHERE email = :'email');
INSERT INTO kullanici_token (hesap_id, tur, token_hash, son_gecerlilik, olusturma)
SELECT id, 'SIFRE_SIFIRLAMA', :'ozet',
       (now() AT TIME ZONE INTERVAL :'fark') + INTERVAL '1 hour',
       now() AT TIME ZONE INTERVAL :'fark'
  FROM kullanici WHERE email = :'email' AND etkin AND dogrulandi
RETURNING 'eklendi';
COMMIT;
SQL
)
[[ "$sonuc" == *eklendi* ]] || hata "token kaydedilemedi (hesap bu arada kapatılmış olabilir)"

echo "Bağlantı $email için üretildi: 1 saat geçerli, tek kullanımlık; önceki sıfırlama bağlantıları geçersiz." >&2
echo "Kullanıcıya güvenli bir kanaldan ilet (bu bağlantıyı açan şifreyi değiştirebilir)." >&2
printf '%s/sifre-sifirla.html?token=%s\n' "$temel_url" "$token"
