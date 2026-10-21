package tr.velzon.aiogretmen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Mülakat testleri için BELİRLENİMCİ sabit içerik fixture'ı. Doğrulama testleri üretim
 * {@code content/mulakat}'a bağlanmaz (kırılgan) — bu bilinen ağacı @TempDir'e yazıp kullanır.
 *
 * <p>Ağaç: tek alan {@code fixture-alan}; iki konu — {@code birinci} (Temel, sira 1: f-1, f-2)
 * ve {@code ikinci} (Orta, sira 2: f-3). Dosya adları (a-konu, b-konu) sira'nın TERSİ sırada,
 * böylece sira'ya göre sıralamanın çalıştığı kanıtlanır. {@code f-2}'nin {@code ## Soru}'su
 * içine gömülü ```` ```java ```` bloğunda SAHTE {@code ## Doğru} ve {@code # fake | Temel} vardır —
 * fence-tracking bunları saymamalı (adversarial fence).
 */
public final class MulakatFixture {

    public static final String ALAN = "fixture-alan";
    public static final String KONU_BIR = "birinci";
    public static final String KONU_IKI = "ikinci";

    // f-1 bilinen değerleri
    public static final String F1 = "f-1";
    public static final int F1_DOGRU = 1;            // B
    public static final String F1_A = "A şıkkı metni";
    public static final String F1_B = "B şıkkı doğru cevap";

    private MulakatFixture() {
    }

    /** Fixture ağacını {@code root} altına yazar ve {@code root}'u döner (MulakatRepo(root)). */
    public static Path yaz(Path root) throws IOException {
        Path alanDir = Files.createDirectories(root.resolve("01-fixture-alan"));
        Files.writeString(alanDir.resolve("alan.md"), """
                ---
                slug: fixture-alan
                baslik: "Fixture Alan"
                aciklama: "Fixture alan açıklaması"
                ---
                """);
        // b-konu.md = sira 1 (dosya adı b, sira sıralaması dosya adından bağımsız kanıtı)
        Files.writeString(alanDir.resolve("b-konu.md"), """
                ---
                slug: birinci
                baslik: "Birinci Konu"
                kademe: Temel
                sira: 1
                etiketler: 2
                ---

                ## Konu anlatımı
                Fixture ders metni.

                ```svg
                <svg viewBox="0 0 10 10" xmlns="http://www.w3.org/2000/svg"><rect x="0" y="0" width="10" height="10" fill="#123456"/></svg>
                ```

                # f-1 | Temel
                ## Soru
                f-1 sorusu: hangisi doğru?
                ## A
                A şıkkı metni
                🤖 A şıkkı için not
                ## B
                B şıkkı doğru cevap
                🤖 B şıkkı için not
                ## C
                C şıkkı metni
                🤖 C şıkkı için not
                ## D
                D şıkkı metni
                🤖 D şıkkı için not
                ## Doğru
                B
                ## Açıklama
                f-1 açıklaması: B doğrudur çünkü fixture böyle tanımlar.

                # f-2 | İleri
                ## Soru
                f-2 sorusu bir kod bloğu içerir; blok içindeki başlıklar SAYILMAMALI:

                ```java
                // adversarial: fence içindeki bu satırlar yapı DEĞİL
                ## Doğru
                D
                # fake-soru | Temel
                ## A
                ```
                ## A
                a2 metni
                🤖 a2 notu
                ## B
                b2 metni
                🤖 b2 notu
                ## C
                c2 doğru cevap
                🤖 c2 notu
                ## D
                d2 metni
                🤖 d2 notu
                ## Doğru
                C
                ## Açıklama
                f-2 açıklaması.
                """);
        // a-konu.md = sira 2 (dosya adı a → sira'ya göre sonra gelmeli)
        Files.writeString(alanDir.resolve("a-konu.md"), """
                ---
                slug: ikinci
                baslik: "İkinci Konu"
                kademe: Orta
                sira: 2
                etiketler: 1
                ---

                ## Konu anlatımı
                İkinci konu ders metni.

                # f-3 | Orta
                ## Soru
                f-3 sorusu?
                ## A
                a3 doğru cevap
                🤖 a3 notu
                ## B
                b3 metni
                🤖 b3 notu
                ## C
                c3 metni
                🤖 c3 notu
                ## D
                d3 metni
                🤖 d3 notu
                ## Doğru
                A
                ## Açıklama
                f-3 açıklaması.
                """);
        return root;
    }

    /**
     * Geçerli bir tek-konu ağacına, verilen konu gövdesini yazar — fail-fast (bozuk) senaryolar
     * için. {@code konuGovde} frontmatter dahil TAM konu dosyası içeriğidir.
     */
    public static Path yazBozuk(Path root, String konuGovde) throws IOException {
        Path alanDir = Files.createDirectories(root.resolve("01-bozuk"));
        Files.writeString(alanDir.resolve("alan.md"), """
                ---
                slug: bozuk
                baslik: "Bozuk"
                aciklama: "bozuk fixture"
                ---
                """);
        Files.writeString(alanDir.resolve("konu.md"), konuGovde);
        return root;
    }
}
