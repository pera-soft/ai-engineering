# Tech Debt Tracker

Küçük, sürekli ödemeler ilkesi: borç birikmeden hafta kapanışlarında 1 kalem ele al.

| # | Borç | Neden ertelendi | Faiz (risk) | Plan |
|---|---|---|---|---|
| 1 | Java 17 → 21 yükseltme | Makinede 17 kuruluydu, kurulum istenmedi | Düşük — record patterns / virtual threads kullanılamıyor | İstenirse `java.version` + JDK kurulumu |
| 2 | Frontmatter parser elle yazıldı (YAML kütüphanesi yok) | Bağımlılık eklememek (ADR gerektirir) | Düşük — format bizim kontrolümüzde | Format karmaşıklaşırsa snakeyaml ADR'si aç |
| 3 | Haiku'nun quiz üretim kalitesi izlenmedi | Faz 3'te ölçülecek | Orta — kötü quiz öğrenmeyi bozar | Faz 3'te 2 hafta örneklem karşılaştır (ADR-0003) |
| 4 | content/ hot-reload yok (restart gerekir) | Basitlik (ADR-0002) | Düşük | İhtiyaç doğarsa dosya izleyici ekle |
| 6 | Rapor cache'i el ile yenileniyor (hafta verisi değişince otomatik düşmez) | Basitlik | Düşük — "Yenile" düğmesi var | Gerekirse gonderim/quiz yazımlarında cache dosyasını sil |
| 7 | Kitap notu işlemede haiku bazen etiket/kavram alanlarını boş dönüyor (özet sağlam) | Nadir; elle düzeltilebilir | Düşük-Orta — yanlış etiket = yanlış haftaya enjeksiyon | Tekrarlarsa: etiketler boşsa 1 düzeltme çağrısı ekle (FoSA Bölüm 6 vakası, 2026-07-03) |


## Kapanan borçlar
| # | Borç | Nasıl kapandı |
|---|---|---|
| 5 | KitapService/KaynakService'te kopya, blok ortasından kesen bağlam kırpma | Ortak `service/BaglamKirpici` (per-parça + toplam sınır, blok sınırında kesme); iki servis de onu kullanır — `39db3d9`. İki sınıftaki `BAGLAM_KARAKTER_SINIRI` artık bilinçli olarak farklı bütçelerdir (kitap 6000, kaynak 14000), kopya mantık değil. |
| 8 | `SpringAiAnlamArama` SimpleVectorStore'un iç `store` alanına reflection ile erişiyordu | Kendi id kümesi + `.idler` yan dosyası; reflection kaldırıldı — `c984271` |
| 9 | `gorev_isaretle` / `POST /api/gorev/toggle` toggle semantiği: zaman aşımı + retry işareti geri alıyor, eşzamanlı çağrıda güncelleme kayboluyordu | İdempotent hedef-durum: `IlerlemeService.gorevDurumuAyarla`, `POST /api/gorev/durum` (`tamamlandi` zorunlu), MCP `hedef_durum` (varsayılan `true`); toggle kaldırıldı. Yan bulgu: Spring Data JDBC tekil indeks çakışmasını `DbActionExecutionException`'a sardığı için eski "yakala, yeniden oku" gerçek DB'de hiç tetiklenmiyordu — neden zinciri taranıyor (`IlerlemeServiceEszamanliTest`) — 2026-10-26 |

## MCP araç hata sözleşmesi — kategorili yapıya yükseltme (2026-07-19)
- **Durum:** Araçlar zaten zarif `isError:true` dönüyor (çıplak istisna sızmıyor) — sözleşmenin
  çekirdeği yerinde. Borç: mesajlar düz metin; `error_category`/`retryable`/kurtarma-yolu alanlı
  yapıya yükseltilebilir (bkz. `mcp-adapter.md` §7). Davranış değişikliği → test + ADR notuyla.
  (Aynı bölümdeki `gorev_isaretle` toggle notu kapandı — bkz. "Kapanan borçlar" #9.)
