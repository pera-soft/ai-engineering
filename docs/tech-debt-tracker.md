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
| 11 | Ön yüz testleri kaynak metin denetimi (JS test altyapısı yok) (Faz 10) | **Karar:** JS davranış testi için DOM taklidi (jsdom vb.) yeni bağımlılık → ADR + kullanıcı onayı gerekir; o zamana kadar kaynak-metin tripwire testleri (`SiteOnYuzIcerikTest`) | Düşük-Orta — davranış regresyonu yakalanmaz | ADR + kullanıcı onayı gelirse gerçek DOM testlerine çevir |


## Kapanan borçlar
| # | Borç | Nasıl kapandı |
|---|---|---|
| 5 | KitapService/KaynakService'te kopya, blok ortasından kesen bağlam kırpma | Ortak `service/BaglamKirpici` (per-parça + toplam sınır, blok sınırında kesme); iki servis de onu kullanır — `39db3d9`. İki sınıftaki `BAGLAM_KARAKTER_SINIRI` artık bilinçli olarak farklı bütçelerdir (kitap 6000, kaynak 14000), kopya mantık değil. |
| 8 | `SpringAiAnlamArama` SimpleVectorStore'un iç `store` alanına reflection ile erişiyordu | Kendi id kümesi + `.idler` yan dosyası; reflection kaldırıldı — `c984271` |
| 9 | `gorev_isaretle` / `POST /api/gorev/toggle` toggle semantiği: zaman aşımı + retry işareti geri alıyor, eşzamanlı çağrıda güncelleme kayboluyordu | İdempotent hedef-durum: `IlerlemeService.gorevDurumuAyarla`, `POST /api/gorev/durum` (`tamamlandi` zorunlu), MCP `hedef_durum` (varsayılan `true`); toggle kaldırıldı. Yan bulgu: Spring Data JDBC tekil indeks çakışmasını `DbActionExecutionException`'a sardığı için eski "yakala, yeniden oku" gerçek DB'de hiç tetiklenmiyordu — neden zinciri taranıyor (`IlerlemeServiceEszamanliTest`) — 2026-10-26 |
| 10 | Hata nesnesinde stale-anahtar mesajı ayrılmış DOM düğümüne yazılıyordu (Faz 10) | `app.js` "anahtar nesli": `anahtarDegisti` sayacı artırır, `gonder` gönderim anındaki nesli yakalar; değiştiyse geç cevap/hata yeni panele basılmaz, tek satır "Önceki anahtarla gönderilen mesajın cevabı gösterilmedi." eklenir; karşılaştır-ve-sil (401) aynen — `5a0966d` |
| 12 | Bir 402 testinde `resolvedException` boş-değil kontrolü eksikti (Faz 10) | `SiteSohbetAnahtarTest.krediBitti402VeCokIstek429` cause döngüsünden önce `isNotNull()` — `5a0966d` |
| 13 | `sinirla`/`MAX_GIRDI` iki controller'da tekrardı | Ortak `web/GirdiSiniri` (package-private); mesajlar aynı, "Metni bölerek gönder." eki parametre; Ogretmen, OgretmenAiOzellik, Atolye, AtolyeAi onu kullanır; `GirdiSiniriTest` — `5a0966d` |
| 14 | Yerel modda `sunucuAi` için birim testi yoktu (Faz 10) | `OzellikControllerTest`: AI açık/kapalı → `sunucuAi` true/false, `site=false`, `kullanici` alanı yok — `5a0966d` |
| 15 | Reddedilen istek önceki hız kovalarını tüketiyordu; 429'da `Retry-After` yoktu; site testleri 127.0.0.1 kovasını paylaşıyordu (Faz 10) | `HizSiniri.bekleme` (tüketmeden kontrol) + interceptor iki aşamalı (hepsi izin verirse yaz); 429 `Retry-After` (kalan süre, sn, ≥1). Test yalıtımı: her site test sınıfı ayrık H2 URL'siyle kendi bağlamını kurduğundan kova sınıf içinde kalır; `sifirla()` eklenmedi, kural `HizSiniriInterceptor` javadoc'unda ("ayrık remoteAddr") — `5a0966d` |
| 16 | Temizlikte dosya silme hatası yutulunca yetim hafıza dizini kalabiliyordu | Kişisel hafıza/rapor dizini hâlâ duruyorsa istisna → işlem geri alınır, satırlar kalır, sonraki gece yeniden denenir; ADR-0017 sınır cümlesi güncel — `5a0966d` |
| 17 | `ZiyaretciRepo.eskiler` sınırsız listeydi | `eskiler(esik, enCok)` (`ORDER BY son_gorulme, id LIMIT ?`); temizlik 500'lük partilerle, turda başarısız kimlik yeniden denenmez, hiç silme yapamayan parti turu bitirir — `5a0966d` |
| 18 | Faz 10 için bütün dalın son incelemesi (whole-branch review) yapılmamıştı | 2026-11-25 son inceleme: kritik/önemli yok; 9 küçük bulgu bu turda kapandı — `a87528a` (temizlik imleçli/keyset sayfalar, geri dönen ziyaretçi "atlanan" sayılır — `a87528a` sonrası kalıntılar ayrı commit; #17'deki "hiç silme yapamayan parti turu bitirir" kuralı kalktı; silmede yaş yeniden denetlenir; GET `son_gorulme`'yi tazeler; `APP_SITE_HIZ_*` ile ayarlanabilir hız sınırları; belge düzeltmeleri) |

## MCP araç hata sözleşmesi — kategorili yapıya yükseltme (2026-07-19)
- **Durum:** Araçlar zaten zarif `isError:true` dönüyor (çıplak istisna sızmıyor) — sözleşmenin
  çekirdeği yerinde. Borç: mesajlar düz metin; `error_category`/`retryable`/kurtarma-yolu alanlı
  yapıya yükseltilebilir (bkz. `mcp-adapter.md` §7). Davranış değişikliği → test + ADR notuyla.
  (Aynı bölümdeki `gorev_isaretle` toggle notu kapandı — bkz. "Kapanan borçlar" #9.)
