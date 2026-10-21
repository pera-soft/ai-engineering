package tr.velzon.aiogretmen.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.ai.AnlamArama;
import tr.velzon.aiogretmen.ai.JsonCevapParser;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.config.LlmKullanan;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.ModelTuru;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.Islem;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.repo.ContentRepo;
import tr.velzon.aiogretmen.types.AiCiktisiBozukException;
import tr.velzon.aiogretmen.types.BenzerNot;
import tr.velzon.aiogretmen.types.NotParcasi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Kitap bilgi tabanı (kullanıcı eklemesi): okunan kitaplardan not/sayfa fotoğrafı gelir,
 * AI işleyip hafta etiketleriyle data/hafiza/kitaplar/<slug>.md'ye biriktirir.
 * Aktif haftayla etiket-eşleşen bölümler AI bağlamına enjekte edilir — sistem
 * kendini kullanıcının okuduklarıyla eğitir.
 */
@Service
@LlmKullanan
public class KitapService {

    private static final Logger log = LoggerFactory.getLogger(KitapService.class);
    private static final int BAGLAM_KARAKTER_SINIRI = 6000; // kitap notu bağlamı toplam üst sınırı
    private static final int KITAP_BASI_SINIR = 3000;        // her not per-parça öz (BaglamKirpici)
    private static final int ANLAM_TOPK = 6;
    /** OCR: yoğun bir kitap sayfası 2000 token'a sığmayıp kesilebiliyordu (inceleme bulgusu E). */
    private static final int GORUNTU_MAX_TOKENS = 4000;

    private final Path kitapDir;
    private final OgretmenAiClient aiClient;
    private final ContentRepo contentRepo;
    private final PromptFactory promptFactory;
    private final JsonCevapParser parser;
    private final AnlamArama anlamArama;

    public KitapService(AppProperties props, OgretmenAiClient aiClient, ContentRepo contentRepo,
                        PromptFactory promptFactory, JsonCevapParser parser, AnlamArama anlamArama) {
        this.kitapDir = Path.of(props.hafizaDir()).resolve("kitaplar");
        this.aiClient = aiClient;
        this.contentRepo = contentRepo;
        this.promptFactory = promptFactory;
        this.parser = parser;
        this.anlamArama = anlamArama;
    }

    /**
     * Açılışta anlam deposu boşsa (JSON yok/silinmiş) mevcut tüm not bloklarını embed'ler
     * (index-at-creation'ın toplu karşılığı). Anahtar yoksa no-op AnlamArama → atlanır.
     */
    @PostConstruct
    void baslangictaIndeksle() {
        if (!anlamArama.aktif() || anlamArama.sayac() > 0 || !Files.isDirectory(kitapDir)) {
            return;
        }
        List<NotParcasi> hepsi = new ArrayList<>();
        try (Stream<Path> dosyalar = Files.list(kitapDir)) {
            for (Path f : dosyalar.filter(x -> x.toString().endsWith(".md")).sorted().toList()) {
                String icerik = KitapDosyaFormati.oku(f);
                String kaynak = KitapDosyaFormati.frontmatterDeger(icerik, "kitap", f.getFileName().toString());
                hepsi.addAll(KitapDosyaFormati.parcalariAyikla(icerik, kaynak));
            }
        } catch (IOException e) {
            log.warn("İlk anlam indekslemesi için kitap dizini okunamadı: {}", e.getMessage());
            return;
        }
        if (!hepsi.isEmpty()) {
            anlamArama.indexle(hepsi);
            log.info("Anlam deposu ilk kez dolduruldu: {} not bloğu", hepsi.size());
        }
    }

    public record IslenmisNot(String kitap, String ozet, List<Integer> etiketler,
                              List<String> anahtarKavramlar, String cikanMetin) {
    }

    record AiNotIslem(String ozet, List<Integer> etiketler,
                      @JsonProperty("anahtar_kavramlar") List<String> anahtarKavramlar) {
    }

    /** Ham metin notu işler ve kitap dosyasına ekler. */
    public IslenmisNot notEkle(String kitap, String hamNot) {
        String prompt = promptFactory.doldur("kitap-notu-isle", Map.of(
                "kitap", kitap,
                "program_basliklari", programBasliklari(),
                "not", hamNot));

        var yanit = aiClient.cagir(AiIstek.tekSeferlik(
                Islem.KITAP, ModelTuru.HIZLI,
                "Sen bir eğitim içeriği düzenleyicisisin. Yalnızca geçerli JSON döndürürsün.",
                prompt, 2000));

        AiNotIslem islem = parser.parse(yanit.metin(), AiNotIslem.class)
                .orElseThrow(() -> {
                    // teşhis: hangi çıktı parse edilemedi? (ilk 800 karakter yeter)
                    String ham = yanit.metin();
                    log.warn("Kitap notu parse edilemedi; ham yanıt ({} kr): {}",
                            ham.length(), ham.substring(0, Math.min(800, ham.length())));
                    return new AiCiktisiBozukException("Kitap notu işlenemedi — tekrar deneyin.");
                });

        dosyayaEkle(kitap, islem);
        yeniBlogunuIndeksle(kitap); // index-at-creation: yeni blok oluşturulduğu an embed+add+persist
        return new IslenmisNot(kitap, islem.ozet(), KitapDosyaFormati.etiketleriSinirla(islem.etiketler()),
                islem.anahtarKavramlar() == null ? List.of() : islem.anahtarKavramlar(), null);
    }

    /** Kitap sayfası fotoğrafı: önce vision ile metin çıkar, sonra normal not hattına ver. */
    public IslenmisNot goruntudenEkle(String kitap, byte[] goruntu, String mimeType) {
        if (goruntu == null || goruntu.length == 0) {
            throw new IllegalArgumentException("Görüntü boş — geçerli bir sayfa fotoğrafı yükleyin.");
        }
        String sistem = promptFactory.doldur("goruntu-metin", Map.of("kitap", kitap));
        var metinYaniti = aiClient.cagirGoruntulu(
                Islem.KITAP, ModelTuru.GUCLU, sistem,
                "Bu sayfadaki metni çıkar.", goruntu, mimeType, GORUNTU_MAX_TOKENS);

        String cikanMetin = metinYaniti.metin();
        if (cikanMetin == null || cikanMetin.isBlank()) {
            throw new AiCiktisiBozukException("Fotoğraftan metin çıkarılamadı — daha net bir fotoğraf deneyin.");
        }

        IslenmisNot not = notEkle(kitap, cikanMetin);
        return new IslenmisNot(not.kitap(), not.ozet(), not.etiketler(), not.anahtarKavramlar(), cikanMetin);
    }

    /** Panel için kitap listesi: ad + etiketler. */
    public List<Map<String, Object>> kitaplar() {
        if (!Files.isDirectory(kitapDir)) {
            return List.of();
        }
        try (Stream<Path> dosyalar = Files.list(kitapDir)) {
            return dosyalar.filter(f -> f.toString().endsWith(".md"))
                    .sorted()
                    .map(f -> {
                        String icerik = KitapDosyaFormati.oku(f);
                        return Map.<String, Object>of(
                                "kitap", KitapDosyaFormati.frontmatterDeger(icerik, "kitap", f.getFileName().toString()),
                                "etiketler", KitapDosyaFormati.tumEtiketler(icerik),
                                "notSayisi", icerik.split("(?m)^## Not").length - 1);
                    })
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("kitap dizini okunamadı", e);
        }
    }

    /** Aktif haftayla etiket-eşleşen kitap bölümleri — sistem prompt'una enjekte edilir (BaglamKirpici). */
    public String baglamIcin(int hafta) {
        if (!Files.isDirectory(kitapDir)) {
            return "(Kitap notu yok.)";
        }
        List<String> parcalar = new ArrayList<>();
        try (Stream<Path> dosyalar = Files.list(kitapDir)) {
            for (Path f : dosyalar.filter(x -> x.toString().endsWith(".md")).sorted().toList()) {
                String icerik = KitapDosyaFormati.oku(f);
                if (KitapDosyaFormati.tumEtiketler(icerik).contains(hafta)) {
                    parcalar.add(icerik);
                }
            }
        } catch (IOException | RuntimeException e) {
            // oku() UncheckedIOException (RuntimeException) atabilir — dosya liste/okuma arasında
            // silinirse prompt bağlamı kurulumu ÇÖKMESİN; toplanan parçalarla best-effort devam.
            log.warn("Kitap bağlamı okunamadı: {}", e.getMessage());
        }
        return BaglamKirpici.birlestir(parcalar, KITAP_BASI_SINIR, BAGLAM_KARAKTER_SINIRI,
                "(Bu haftayla eşleşen kitap notu yok.)");
    }

    /**
     * Anlam-eşleşmeli bağlam (ADR-0005): sorguya en yakın {@value #ANLAM_TOPK} not bloğunu
     * getirip birleştirir. Depo boş / anahtar yok / embedding hatası → hafta-etiketi
     * eşleşmesine ({@link #baglamIcin(int)}) düşer (belt-and-suspenders).
     */
    public String baglamIcinAnlam(String sorgu, int hafta) {
        List<BenzerNot> benzerler = anlamArama.benzerleri(sorgu, ANLAM_TOPK);
        if (benzerler.isEmpty()) {
            return baglamIcin(hafta);
        }
        List<String> parcalar = benzerler.stream().map(BenzerNot::metin).toList();
        return BaglamKirpici.birlestir(parcalar, KITAP_BASI_SINIR, BAGLAM_KARAKTER_SINIRI, "").strip();
    }

    // --- iç işler ---

    /** notEkle'den sonra: dosyanın SON bloğunu (yeni eklenen) yeniden parse edip indeksler. */
    private void yeniBlogunuIndeksle(String kitap) {
        if (!anlamArama.aktif()) {
            return;
        }
        Path dosya = kitapDir.resolve(KitapDosyaFormati.slugla(kitap) + ".md");
        if (!Files.isRegularFile(dosya)) {
            return;
        }
        String icerik = KitapDosyaFormati.oku(dosya);
        String kaynak = KitapDosyaFormati.frontmatterDeger(icerik, "kitap", kitap);
        List<NotParcasi> parcalar = KitapDosyaFormati.parcalariAyikla(icerik, kaynak);
        if (!parcalar.isEmpty()) {
            anlamArama.indexle(List.of(parcalar.get(parcalar.size() - 1)));
        }
    }

    private void dosyayaEkle(String kitap, AiNotIslem islem) {
        try {
            Files.createDirectories(kitapDir);
            Path dosya = kitapDir.resolve(KitapDosyaFormati.slugla(kitap) + ".md");
            String blok = KitapDosyaFormati.blokMetni(islem.ozet(), islem.anahtarKavramlar(),
                    KitapDosyaFormati.etiketleriSinirla(islem.etiketler()), java.time.LocalDate.now());

            if (Files.isRegularFile(dosya)) {
                Files.writeString(dosya, KitapDosyaFormati.oku(dosya) + blok);
            } else {
                Files.writeString(dosya, KitapDosyaFormati.dosyaBasligi(kitap) + blok);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("kitap notu yazılamadı", e);
        }
    }

    private String programBasliklari() {
        return contentRepo.haftalar().stream()
                .map(h -> h.no() + ": " + h.baslik())
                .collect(Collectors.joining("\n"));
    }
}
