#!/usr/bin/env bash
# pera-soft/ai-engineering (HERKESE AÇIK) için yayın dalını günceller.
#
# Kullanım:
#   bash tools/yayin-hazirla.sh            # HEAD'in commit'li hâlini pera-soft-yayin'e yeni commit olarak ekler
#   git push pera-soft pera-soft-yayin:main
#
# Neden ayrı dal: çalışma dalının geçmişi gerçek mülakat dökümleri ve eski sunucu
# ayrıntıları içerir; yayın dalı geçmişsiz başladı (tek commit) ve yalnız "ağaç" taşır.
# Yalnız COMMIT EDİLMİŞ hâl gider; commit edilmemiş değişiklikler yayına girmez.
set -euo pipefail

YAYIN_DALI=pera-soft-yayin
KAYNAK="${1:-HEAD}"

cd "$(dirname "$0")/.."

echo "==> Sızıntı taraması ($KAYNAK)"
sizinti=0
tara() {  # $1 açıklama, $2 PCRE
  local bulgu
  bulgu=$(git grep -n -I -P -e "$2" "$KAYNAK" -- . || true)
  if [ -n "$bulgu" ]; then
    echo "  ENGEL — $1:" >&2
    echo "$bulgu" | cut -c1-200 | head -10 >&2
    sizinti=1
  fi
}
# 127.0.0.1, 0.0.0.0 ve özel ağ blokları (10/8, 172.16/12, 192.168/16) ders örneği sayılır; gerisi gerçek sunucu.
tara "genel IP adresi" '(?<![\d.])(?!127\.0\.0\.1(?![\d.]))(?!0\.0\.0\.0(?![\d.]))(?!10\.)(?!192\.168\.)(?!172\.(1[6-9]|2\d|3[01])\.)(\d{1,3}\.){3}\d{1,3}(?![\d.])'
tara "root SSH hedefi"   'root@[A-Za-z0-9.-]+'
tara "e-posta adresi"    '[A-Za-z0-9._%+-]+@(gmail|hotmail|outlook|yahoo|icloud)\.com'
tara "özel anahtar"      '-----BEGIN [A-Z ]*PRIVATE KEY'
tara "API anahtarı"      '(sk-or-v1-[A-Za-z0-9]{20,}|sk-ant-[A-Za-z0-9_-]{20,}|gh[pousr]_[A-Za-z0-9]{30,}|AKIA[0-9A-Z]{16})'
yasak=$(git ls-tree -r --name-only "$KAYNAK" | grep -E '^(thoughts/|data/|tools/deploy\.env$|\.env$)' || true)
if [ -n "$yasak" ]; then
  echo "  ENGEL — git dışı kalması gereken dosyalar:" >&2
  echo "$yasak" | head -10 >&2
  sizinti=1
fi
[ "$sizinti" -eq 0 ] || { echo "HATA: yayın hazırlanmadı; önce yukarıdakileri temizle." >&2; exit 1; }
echo "  temiz"

agac=$(git rev-parse "$KAYNAK^{tree}")
kisa=$(git rev-parse --short "$KAYNAK")
if git rev-parse -q --verify "refs/heads/$YAYIN_DALI" >/dev/null; then
  if [ "$(git rev-parse "$YAYIN_DALI^{tree}")" = "$agac" ]; then
    echo "==> $YAYIN_DALI zaten güncel ($kisa); yeni commit yok."
    exit 0
  fi
  yeni=$(git commit-tree "$agac" -p "$YAYIN_DALI" -m "Yayin guncellemesi (kaynak: $kisa)")
else
  yeni=$(git commit-tree "$agac" -m "AI Ogretmen: ilk yayin (kaynak: $kisa)")
fi
git update-ref "refs/heads/$YAYIN_DALI" "$yeni"
echo "==> $YAYIN_DALI -> $(git rev-parse --short "$yeni")"
echo "Göndermek için: git push pera-soft $YAYIN_DALI:main"
