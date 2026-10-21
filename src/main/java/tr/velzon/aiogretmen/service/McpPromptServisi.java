package tr.velzon.aiogretmen.service;

import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.repo.AtolyeRepo;
import tr.velzon.aiogretmen.repo.ContentRepo;
import tr.velzon.aiogretmen.types.AtolyeAlistirma;
import tr.velzon.aiogretmen.types.BulunamadiException;
import tr.velzon.aiogretmen.types.Hafta;
import tr.velzon.aiogretmen.types.QuizSorusu;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP prompt primitifleri için öğretmenlik talimatı METNİNİ üretir (ADR-0007). Mevcut LLM-çağıran
 * servisler (SohbetService, DegerlendirmeService, AtolyeService, QuizUretimService) bu metinleri
 * kendi içinde kurup Claude'a gönderir; MCP ise aynı metni <em>çalıştırmadan</em> istemciye sunar —
 * muhakeme (ve token maliyeti) bağlanan kullanıcının Claude'unda yapılır.
 *
 * <p>LLM/embedding'e HİÇ dokunmaz: {@link BaglamServisi#sistemBaglamiEmbeddingsiz} kullanılır,
 * anlam-eşleşmeli kitap bağlamı devre dışıdır. Bu servis {@code service} katmanındadır; {@code mcp}
 * adaptörü yalnız bu servisi çağırır (ArchUnit: mcp → service).
 */
@Service
public class McpPromptServisi {

    private final BaglamServisi baglamServisi;
    private final PromptFactory promptFactory;
    private final ContentRepo contentRepo;
    private final AtolyeRepo atolyeRepo;

    public McpPromptServisi(BaglamServisi baglamServisi, PromptFactory promptFactory,
                            ContentRepo contentRepo, AtolyeRepo atolyeRepo) {
        this.baglamServisi = baglamServisi;
        this.promptFactory = promptFactory;
        this.contentRepo = contentRepo;
        this.atolyeRepo = atolyeRepo;
    }

    /** Öğretmen sistem kimliği + aktif hafta/gün bağlamı (embeddingsiz). */
    public String ogretmenSistem(int hafta, Integer gun) {
        return baglamServisi.sistemBaglamiEmbeddingsiz(hafta, gun);
    }

    /** Sistem kimliği + sokratik sohbet davranış kuralları (brief 7.3). */
    public String sokratik(int hafta, Integer gun) {
        return ogretmenSistem(hafta, gun) + "\n\n"
                + promptFactory.doldur("sokratik-sohbet", Map.of());
    }

    /** Sistem kimliği + değerlendirme rubriği; öğrencinin gönderimi gömülür (brief 7.2). */
    public String degerlendir(int hafta, Integer gun, String tur, String gonderim) {
        String talimat = promptFactory.doldur("degerlendirme", Map.of(
                "tur", tur == null || tur.isBlank() ? "yazılı gönderim" : tur,
                "gorev", "",
                "icerik", gonderim == null ? "" : gonderim));
        return ogretmenSistem(hafta, gun) + "\n\n" + talimat;
    }

    /**
     * Prompt Atölyesi değerlendirme talimatı — {@link AtolyeService#degerlendir}'in kurduğu metnin
     * LLM'siz eşdeğeri. Nötr sistem kimliği (haftadan bağımsız) + rubrik + öğrencinin prompt'u.
     *
     * @throws BulunamadiException alıştırma id'si yoksa
     */
    public String atolyeDegerlendir(String alistirmaId, String ogrenciPrompt) {
        AtolyeAlistirma a = atolyeRepo.kategoriler().stream()
                .flatMap(k -> k.alistirmalar().stream())
                .filter(x -> x.id().equals(alistirmaId))
                .findFirst()
                .orElseThrow(() -> new BulunamadiException("Alıştırma bulunamadı: " + alistirmaId));

        String sistem = promptFactory.doldur("sistem-kimlik", Map.of(
                "hafta_icerik", "(Prompt Atölyesi — belirli bir haftaya bağlı değil, serbest pratik)",
                "gun_icerik", "(atölye)",
                "ilerleme_ozeti", "(atölye: öğrenci teknikleri deneyerek öğreniyor)",
                "hafiza", "", "kitaplar", "", "kaynaklar", ""));

        String istek = promptFactory.doldur("prompt-degerlendirme", Map.of(
                "teknik", a.teknik(),
                "gorev", a.gorev(),
                "ideal_cozum", a.idealCozum(),
                "neden", a.neden(),
                "ogrenci_prompt", ogrenciPrompt == null ? "" : ogrenciPrompt));

        return sistem + "\n\n" + istek;
    }

    /**
     * AI quiz üretim talimatı (brief 7.4) — haftanın tüm ders içeriği + statik quizde sorulmuş
     * soruların tekrar yasağı gömülür.
     *
     * @throws BulunamadiException hafta yoksa
     */
    public String quizUret(int hafta, int adet) {
        Hafta h = contentRepo.hafta(hafta)
                .orElseThrow(() -> new BulunamadiException("Hafta bulunamadı: " + hafta));
        String haftaIcerik = h.gunler().stream()
                .map(g -> "## " + g.baslik() + "\n" + g.ders())
                .collect(Collectors.joining("\n\n"));
        String mevcutSorular = h.quiz().isEmpty()
                ? "(statik quiz yok)"
                : h.quiz().stream().map(QuizSorusu::soru).map(s -> "- " + s)
                        .collect(Collectors.joining("\n"));
        return promptFactory.doldur("quiz-uret", Map.of(
                "adet", String.valueOf(adet <= 0 ? 5 : adet),
                "mevcut_sorular", mevcutSorular,
                "hafta_icerik", haftaIcerik));
    }
}
