# ExecPlan: Faz 2 — AI Öğretmen çekirdeği

> Yaşayan belge. Yalnızca bu belgeden işi yeniden başlatmak mümkün olmalı.
> Kaynak spec: CLAUDE_CODE_BRIEF.md §7 (7.1-7.3, 7.6), §12 Faz 2 + kullanıcı eklemeleri
> (thoughts/ledgers/CONTINUITY_CLAUDE-ai-ogretmen.md → Remaining).

## Neden önemli (kullanıcı bakışı)
Sohbet paneli canlanıyor: kullanıcı çalışırken soru soracak (Sokratik yöntem),
"ben şunu yaptım" diye gönderecek (rubrikli değerlendirme), öğretmen onu
data/hafiza/ markdown ağacına kaydedip her konuşmada tanıyarak devam edecek.

## Mimari kararlar
- `spring.ai.model.chat: none` KALICI — ChatClient bean'ini Spring AI auto-config değil,
  ai/AiYapilandirma kurar (yalnızca anahtar doluysa). Anahtarsız açılış garantisi sürer.
- ai/ paketi tek çıkış: `OgretmenAiClient` arayüzü; anahtar yoksa `DevreDisi` implementasyonu
  (503 + net mesaj), varsa Spring AI ChatClient implementasyonu. ArchUnit kuralı değişmez.
- Prompt şablonları prompts/*.st (kod değil, versiyonlanan varlık); PromptFactory okur.
- JSON sözleşmesi (brief 7.2): bozuk JSON'da 1 düzeltme çağrısı; yine bozuksa puan=null +
  ham metin ozet'e — uygulama çökmez. JsonCevapParser birim testi ZORUNLU (brief §13.3).
- Token kullanımı: AI_KULLANIM tablosu (islem, model, input/output token, tarih);
  GET /api/kullanim toplamları döner. max_tokens: değerlendirme 2000, sohbet 1500, hafıza 1000.
- Hafıza (kullanıcı eklemesi): data/hafiza/OGRENCI.md + hafta-NN/notlar.md.
  Değerlendirme sonrası ve her 4 kullanıcı sohbet mesajında bir, hizli model (haiku)
  hafızayı günceller. Sohbet/değerlendirme sistem bağlamına (7.1) enjekte edilir:
  hafta.md + gün + ilerleme özeti + OGRENCI.md + aktif hafta notları.

## Durum
- [x] 2.1 Şema: AI_KULLANIM tablosu + Gonderim/SohbetMesaj/AiKullanim entity + repo'lar
- [x] 2.2 prompts/: degerlendirme.st, sokratik-sohbet.st, hafiza-guncelle.st
- [x] 2.3 ai/: OgretmenAiClient arayüzü, JsonCevapParser (TDD), PromptFactory (TDD),
      SpringAiClient + DevreDisiClient + AiYapilandirma (koşullu bean)
- [x] 2.4 service/: BaglamServisi (7.1 ortak bağlam), DegerlendirmeService (7.2),
      SohbetService (7.3, son 20 mesaj), HafizaService, KullanimService
- [x] 2.5 web/: POST /api/degerlendir, POST /api/sohbet, GET /api/sohbet/{hafta}, GET /api/kullanim
- [x] 2.6 Frontend: sohbet paneli aktif (Sohbet/Değerlendir modları, puan kartı, geçmiş)
- [x] 2.7 Doğrulama: testler yeşil; anahtarsız 503 sözleşmesi; gerçek anahtarla uçtan uca
      (özet gönder → yapılandırılmış değerlendirme + hafıza dosyası oluştu) + commit

## Kabul (brief §12 Faz 2)
- Gerçek anahtar ile örnek özet → yapılandırılmış değerlendirme döner.
- Bozuk JSON senaryosu zarifçe ele alınır (birim test).
- Sohbet bağlamı hafta içeriğini içerir (test: prompt'ta hafta başlığı geçiyor).
- Token kullanım logu çalışır; /api/kullanim toplamları gösterir.
- + Kullanıcı eklemesi: hafıza md dosyaları oluşur ve sonraki çağrıya enjekte edilir.

## Doğrulama adımları
1. ./mvnw verify — yeni birim testler dahil yeşil
2. Anahtarsız: POST /api/sohbet → 503 {hata, detay}; uygulama açılıyor
3. ANTHROPIC_API_KEY ile: sohbet + değerlendirme + data/hafiza/ dosya kontrolü + /api/kullanim

## Doğrulama notu (2026-07-03)
- 51/51 test yeşil (JSON parse zorunlu testleri dahil).
- Anahtarsız: uygulama açılıyor; /api/sohbet → 503 {hata,detay}; /api/kullanim aiAktif:false. ✓
- Sağlayıcı kullanıcı isteğiyle OpenRouter'a taşındı (ADR-0004): spring-ai-starter-model-openai,
  base-url openrouter.ai/api, OPENROUTER_API_KEY, model kimlikleri anthropic/claude-*.
- CANLI KABUL GEÇTİ (OpenRouter): Sokratik sohbet doğrudan cevap vermeden yönlendirici soru sordu;
  değerlendirme kasıtlı 'harness=model' yanlış anlamasını yakalayıp rubrikle %52 verdi;
  data/hafiza/OGRENCI.md + hafta-01/notlar.md AI tarafından oluştu; /api/kullanim işlem bazında
  token dökümü verdi (3 çağrı, 5169 input / 2257 output).
- Canlıda yakalanan 2 hata düzeltildi: (1) /api/kullanim dolu tabloda 500 (Spring Data JDBC
  @Query Map dönüşü desteklemiyor → JdbcClient'lı KullanimSorgulari + dolu-tablo regresyon testi);
  (2) sohbette kullanıcı mesajı AI hatasında da kaydediliyordu → kayıt çağrı sonrasına alındı.
