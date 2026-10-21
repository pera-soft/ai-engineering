# ExecPlan: Faz 3 — AI quiz üretimi, haftalık rapor, kitap bilgi tabanı

> Yaşayan belge. Kaynak: CLAUDE_CODE_BRIEF.md §7.4-7.5, §12 Faz 3 + kullanıcı eklemeleri
> (kitap notu yapıştırma + sayfa fotoğrafı yükleme — ledger'daki Remaining).

## Neden önemli (kullanıcı bakışı)
Hafta kapanışı tamamlanıyor: statik quize ek AI'dan taze quiz istenebilecek, hafta sonunda
kişisel rapor alınacak. Kullanıcı okuduğu kitaplardan not/sayfa fotoğrafı verecek;
öğretmen bunları işleyip derslerde o kitaplardan örneklerle konuşacak.

## Tasarım kararları
- **Quiz üretimi (7.4):** hizli model; haftanın TÜM gün içerikleri bağlamda; statik quiz
  soruları prompt'a verilir "bunları SORMA" diye. Çıktı `{"sorular":[...]}` NESNESİ
  (JsonCevapParser dizi değil nesne ayıklar). Sonuç quiz_sonuc(tur=AI_URETIM).
- **Rapor (7.5):** guclu model; girdi = hafta içeriği + görevler + gönderimler/değerlendirmeler
  + quiz sonuçları + hafıza. Çıktı markdown → data/raporlar/hafta-NN.md CACHE
  (varsa dosyadan döner; ?yenile=true ile zorla üret). API HTML (commonmark) + ham md döner.
- **Kitap bilgi tabanı (kullanıcı eklemesi):** dosya tabanlı, DB yok.
  - POST /api/kitap-notu {kitap, not} → hizli model işler: özet + hafta etiketleri (1-12)
    → data/hafiza/kitaplar/<slug>.md (frontmatter: kitap, etiketler; gövde: işlenmiş not birikir).
  - POST /api/kitap-notu/goruntu (multipart: kitap + goruntu) → guclu model VISION ile
    sayfa fotoğrafından metin çıkarır → aynı işleme hattı. Boyut sınırı 10MB.
  - BaglamServisi: aktif haftayla etiket-eşleşen kitap bölümleri sistem prompt'una
    {kitaplar} olarak enjekte edilir → öğretmen okuduğun kitaptan örnek verir.
- **ai/ genişletme:** OgretmenAiClient'a görüntülü çağrı eklenir
  (Spring AI Media / OpenAI-uyumlu image_url). DevreDisi impl aynı 503 istisnasını atar.
- Frontend: statik quizin altında "AI'dan yeni quiz" butonu; hafta başlığında "Rapor" butonu
  (inline markdown görünümü); panelde 3. mod "Kitap" (not yapıştır + fotoğraf yükle + kitap listesi).

## Durum
- [x] 3.1 prompts/: quiz-uret.st, haftalik-rapor.st, kitap-notu-isle.st, goruntu-metin.st
- [x] 3.2 ai/: görüntülü çağrı (cagirGoruntulu) + testler
- [x] 3.3 QuizUretimService (TDD): üret, parse, kaydet
- [x] 3.4 RaporService (TDD): veri toplama, cache, yenile
- [x] 3.5 KitapService (TDD): slug, not işleme, görüntü hattı, bağlam enjeksiyonu (BaglamServisi'ne {kitaplar})
- [x] 3.6 web/: /api/quiz/uret, /api/rapor/{hafta}, /api/kitap-notu, /api/kitap-notu/goruntu, /api/kitaplar
- [x] 3.7 Frontend: AI quiz akışı, Rapor görünümü, Kitap modu (yapıştır+yükle)
- [x] 3.8 Doğrulama: testler + canlı (quiz üret, rapor al, not işle, foto yükle) + tech-debt güncel + commit

## Kabul (brief §12 Faz 3 + eklemeler)
1. hafta baştan sona "yaşanabilir": günler okunur, görevler işaretlenir, özet değerlendirilir,
sohbet edilir, EK QUIZ çözülür, RAPOR alınır + kitap notu/fotoğrafı işlenir ve derste kullanılır.
Hata/boş/yükleniyor durumları ele alınmış; docs/tech-debt-tracker.md güncel.

## Doğrulama notu (2026-07-03)
- 73/73 test yeşil (ArchUnit dahil).
- Canlı (OpenRouter): quiz üretimi 3 taze soru (statik quizden farklı, kavrama odaklı); rapor
  kanıta dayalı üretildi + cache (2. çağrı cacheden) + data/raporlar/hafta-01.md; kitap notu
  hafta 8'e etiketlendi; sayfa fotoğrafı vision ile birebir metne çevrilip hafta 4,5,9'a
  etiketlendi; /api/kullanim 6 işlem türünü ayrı ayrı raporluyor.
- Türkçe karakter kontrolü: md dosyaları ve API UTF-8 temiz; kullanıcının gördüğü bozulma
  charset'siz GEÇİCİ test sayfasındandı (silindi). Önlem: server.servlet.encoding force=true.
