# Tech Debt Tracker

Küçük, sürekli ödemeler ilkesi: borç birikmeden hafta kapanışlarında 1 kalem ele al.

| # | Borç | Neden ertelendi | Faiz (risk) | Plan |
|---|---|---|---|---|
| 1 | Java 17 → 21 yükseltme | Makinede 17 kuruluydu, kurulum istenmedi | Düşük — record patterns / virtual threads kullanılamıyor | İstenirse `java.version` + JDK kurulumu |
| 2 | Frontmatter parser elle yazıldı (YAML kütüphanesi yok) | Bağımlılık eklememek (ADR gerektirir) | Düşük — format bizim kontrolümüzde | Format karmaşıklaşırsa snakeyaml ADR'si aç |
| 3 | Haiku'nun quiz üretim kalitesi izlenmedi | Faz 3'te ölçülecek | Orta — kötü quiz öğrenmeyi bozar | Faz 3'te 2 hafta örneklem karşılaştır (ADR-0003) |
| 4 | content/ hot-reload yok (restart gerekir) | Basitlik (ADR-0002) | Düşük | İhtiyaç doğarsa dosya izleyici ekle |
| 5 | KitapService.baglamIcin/baglamIcinAnlam HÂLÂ kaba `setLength(6000)` ile kesiyor (blok ortasından); KaynakService (ADR-0005 sonrası) per-parça öz + toplam + blok-sınırı kırpma kazandı ama KitapService'e taşınmadı — **iki serviste kopya kırpma mantığı, `BAGLAM_KARAKTER_SINIRI` sabiti iki sınıfta aynı adla farklı değerde (6000 vs 14000)** | Basitlik; ADR-0005 asıl yolu anlam-aramasına çevirdi (eski "hafta-etiketli seçim" planı SUPERSEDE edildi) | Orta — markdown blok ortadan kesilir; DRY ihlali; sağlayıcı context penceresi değişince 2+ yer elle | Ortak `BaglamKirpici` (per-parça sınır + toplam + blok-sınırında kes) çıkar; KaynakService+KitapService(2 metot) onu kullansın (mimari inceleme, 2026-07-11) |
| 6 | Rapor cache'i el ile yenileniyor (hafta verisi değişince otomatik düşmez) | Basitlik | Düşük — "Yenile" düğmesi var | Gerekirse gonderim/quiz yazımlarında cache dosyasını sil |
| 7 | Kitap notu işlemede haiku bazen etiket/kavram alanlarını boş dönüyor (özet sağlam) | Nadir; elle düzeltilebilir | Düşük-Orta — yanlış etiket = yanlış haftaya enjeksiyon | Tekrarlarsa: etiketler boşsa 1 düzeltme çağrısı ekle (FoSA Bölüm 6 vakası, 2026-07-03) |


- #8 (Tem 2026): SpringAiAnlamArama.sayac()/temizle() SimpleVectorStore protected `store` alanına REFLECTION ile erişir. Spring AI sürüm yükseltmesinde kırılabilir; yerel sayaç fallback var. Kalıcı çözüm: kendi id kümemizi tutmak.

## MCP araç hata sözleşmesi — kategorili yapıya yükseltme (2026-07-19)
- **Durum:** Araçlar zaten zarif `isError:true` dönüyor (çıplak istisna sızmıyor) — sözleşmenin
  çekirdeği yerinde. Borç: mesajlar düz metin; `error_category`/`retryable`/kurtarma-yolu alanlı
  yapıya yükseltilebilir (bkz. `mcp-adapter.md` §7). Ayrıca `gorev_isaretle` toggle semantiği,
  zaman aşımı + istemci retry senaryosunda işareti geri alabilir — açık `hedef_durum` parametresi
  (idempotent set) değerlendirilebilir. İkisi de davranış değişikliği → test + ADR notuyla.
