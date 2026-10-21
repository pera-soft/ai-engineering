package tr.velzon.aiogretmen.repo;

import tr.velzon.aiogretmen.config.MulakatEtkin;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.types.MulakatAlan;
import tr.velzon.aiogretmen.types.MulakatKonu;
import tr.velzon.aiogretmen.types.MulakatSik;
import tr.velzon.aiogretmen.types.MulakatSoru;
import tr.velzon.aiogretmen.types.MulakatSoruBulgusu;
import tr.velzon.aiogretmen.types.MulakatZorluk;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Mülakat içeriğini (content/mulakat/&lt;NN-alan&gt;/{alan.md + konu dosyaları}) açılışta okuyup
 * bellekte tutar (AtolyeRepo deseni). İçerik = veri; alan sırası dizin adı önekine (01, 02…),
 * konu sırası frontmatter {@code sira} alanına göredir.
 *
 * <p><strong>Dosya formatı:</strong> {@code alan.md} frontmatter (slug, baslik, aciklama);
 * konu dosyası frontmatter (slug, baslik, kademe, sira, etiketler) + gövde: ilk soru başlığına
 * kadar ders metni ({@code konuAnlatimi}), ardından her soru
 * "# &lt;id&gt; | &lt;Temel|Orta|İleri|Uzman&gt;" ile başlar; "## Soru", "## A"…"## D",
 * "## Doğru", "## Açıklama" bölümleri. Şık bölümünde 🤖 ile başlayan satır o şıkkın AI notudur.
 *
 * <p><strong>Fence-tracking ZORUNLU:</strong> ``` içindeki {@code #}/{@code ##} yapı sayılmaz
 * (kod güvenliği). Yükleme-anı fail-fast: her soru tam 4 şık, {@code ## Doğru} ∈ {A,B,C,D},
 * Soru/Açıklama dolu, zorluk geçerli, soru-id'ler TÜM ağaçta benzersiz — ihlalde
 * {@link IllegalStateException}.
 */
@MulakatEtkin
@org.springframework.stereotype.Repository
public class MulakatRepo {

    /** "# eq-1 | Temel" satırını yakalar: grup1 = soru id, grup2 = zorluk. */
    private static final Pattern SORU_BAS =
            Pattern.compile("^#\\s+(\\S.*?)\\s*\\|\\s*(Temel|Orta|İleri|Uzman)\\s*$");
    private static final String MULAKAT_DIZINI = "mulakat";
    /** U+1F916 robot emoji — bir şık gövdesinde bu ile başlayan satır o şıkkın AI notudur. */
    private static final String ROBOT = "🤖";

    private final List<MulakatAlan> alanlar;
    /** soruId → bulgu (O(1) soruBul). Kurulurken global soru-id benzersizliği de zorlanır. */
    private final Map<String, MulakatSoruBulgusu> soruIndeksi;

    @org.springframework.beans.factory.annotation.Autowired
    public MulakatRepo(AppProperties props) {
        this(Path.of(props.contentDir()).resolve(MULAKAT_DIZINI));
    }

    public MulakatRepo(Path mulakatDir) {
        if (!Files.isDirectory(mulakatDir)) {
            throw new IllegalStateException(
                    "Mülakat içerik dizini bulunamadı: " + mulakatDir.toAbsolutePath());
        }
        this.alanlar = yukle(mulakatDir);
        this.soruIndeksi = soruIndeksiKur(this.alanlar);
    }

    public List<MulakatAlan> alanlar() {
        return alanlar;
    }

    public Optional<MulakatAlan> alan(String slug) {
        return alanlar.stream().filter(a -> a.slug().equals(slug)).findFirst();
    }

    public Optional<MulakatKonu> konu(String alanSlug, String konuSlug) {
        return alan(alanSlug).flatMap(a -> a.konular().stream()
                .filter(k -> k.slug().equals(konuSlug)).findFirst());
    }

    /** Soru id'sini O(1) indeksten döndürür (foreign-key gibi kullanılır). */
    public Optional<MulakatSoruBulgusu> soruBul(String soruId) {
        return Optional.ofNullable(soruIndeksi.get(soruId));
    }

    /**
     * soruId → bulgu indeksini kurar; aynı put'ta global soru-id benzersizliğini zorlar
     * (çakışma → fail-fast, foreign-key bütünlüğü). {@link HashMap} put dönüşüyle O(n) tek geçiş.
     */
    private static Map<String, MulakatSoruBulgusu> soruIndeksiKur(List<MulakatAlan> alanlar) {
        Map<String, MulakatSoruBulgusu> indeks = new HashMap<>();
        for (MulakatAlan a : alanlar) {
            for (MulakatKonu k : a.konular()) {
                for (MulakatSoru s : k.sorular()) {
                    MulakatSoruBulgusu onceki =
                            indeks.put(s.id(), new MulakatSoruBulgusu(a.slug(), k.slug(), s));
                    if (onceki != null) {
                        throw new IllegalStateException(
                                "Soru id'si ağaçta benzersiz değil: '" + s.id() + "'");
                    }
                }
            }
        }
        return indeks;
    }

    private static List<MulakatAlan> yukle(Path dir) {
        List<MulakatAlan> alanlar;
        try (Stream<Path> altlar = Files.list(dir)) {
            alanlar = altlar
                    .filter(Files::isDirectory)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .map(MulakatRepo::alanOku)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Mülakat dizini okunamadı: " + dir, e);
        }
        Set<String> alanSluglari = new HashSet<>();
        for (MulakatAlan a : alanlar) {
            if (!alanSluglari.add(a.slug())) {
                throw new IllegalStateException("Alan slug'ı benzersiz değil: '" + a.slug() + "'");
            }
        }
        return alanlar;
    }

    private static MulakatAlan alanOku(Path alanDir) {
        Path alanMd = alanDir.resolve("alan.md");
        if (!Files.isRegularFile(alanMd)) {
            throw new IllegalStateException("alan.md bulunamadı: " + alanDir.toAbsolutePath());
        }
        Map<String, String> fm = new HashMap<>();
        frontmatterOku(satirlar(alanMd), fm, alanMd);
        String alanSlug = fm.getOrDefault("slug", "");

        List<MulakatKonu> konular;
        try (Stream<Path> dosyalar = Files.list(alanDir)) {
            konular = dosyalar
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().endsWith(".md"))
                    .filter(f -> !f.getFileName().toString().equals("alan.md"))
                    .map(f -> konuOku(f, alanSlug))
                    .sorted(Comparator.comparingInt(MulakatKonu::sira))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Alan dizini okunamadı: " + alanDir, e);
        }
        Set<String> konuSluglari = new HashSet<>();
        for (MulakatKonu k : konular) {
            if (!konuSluglari.add(k.slug())) {
                throw new IllegalStateException("Alan '%s' içinde konu slug'ı benzersiz değil: '%s'"
                        .formatted(alanSlug, k.slug()));
            }
        }
        return new MulakatAlan(alanSlug, fm.getOrDefault("baslik", ""),
                fm.getOrDefault("aciklama", ""), konular);
    }

    private static MulakatKonu konuOku(Path dosya, String alanSlug) {
        List<String> satirlar = satirlar(dosya);
        Map<String, String> fm = new HashMap<>();
        int bas = frontmatterOku(satirlar, fm, dosya);

        List<String> anlatim = new ArrayList<>();
        List<MulakatSoru> sorular = new ArrayList<>();
        String soruId = null, zorluk = null, aktifBolum = null;
        Map<String, List<String>> bolumler = null;
        boolean fenceIci = false, soruBasladi = false;

        for (int i = bas; i < satirlar.size(); i++) {
            String satir = satirlar.get(i);
            boolean fenceCizgi = satir.strip().startsWith("```");
            if (fenceCizgi) {
                fenceIci = !fenceIci;
            } else if (!fenceIci) {
                Matcher m = SORU_BAS.matcher(satir);
                if (m.matches()) {
                    if (soruId != null) {
                        sorular.add(soruKur(soruId, zorluk, bolumler, dosya));
                    }
                    soruId = m.group(1).strip();
                    zorluk = m.group(2).strip();
                    bolumler = new LinkedHashMap<>();
                    aktifBolum = null;
                    soruBasladi = true;
                    continue;
                }
                if (soruBasladi && satir.startsWith("## ")) {
                    aktifBolum = satir.substring(3).strip();
                    if (bolumler.containsKey(aktifBolum)) {
                        throw new IllegalStateException("Soru '%s' bölümü '## %s' iki kez tanımlı (%s)"
                                .formatted(soruId, aktifBolum, dosya));
                    }
                    bolumler.put(aktifBolum, new ArrayList<>());
                    continue;
                }
            }
            if (!soruBasladi) {
                anlatim.add(satir);
            } else if (aktifBolum != null) {
                bolumler.get(aktifBolum).add(satir);
            } else if (!satir.isBlank()) {
                // Soru başlığı ile ilk '## ' bölümü arası içerik sessizce düşmesin (fail-fast).
                throw new IllegalStateException(
                        "Soru '%s' başlığı ile ilk '## ' bölümü arasında beklenmeyen içerik (%s)"
                                .formatted(soruId, dosya));
            }
        }
        if (fenceIci) {
            throw new IllegalStateException("Kapanmayan kod çiti (```) var: " + dosya);
        }
        if (soruId != null) {
            sorular.add(soruKur(soruId, zorluk, bolumler, dosya));
        }
        if (sorular.isEmpty()) {
            throw new IllegalStateException("Konu hiç soru içermiyor: " + dosya);
        }
        return new MulakatKonu(alanSlug, fm.getOrDefault("slug", ""), fm.getOrDefault("baslik", ""),
                MulakatZorluk.ayikla(fm.getOrDefault("kademe", "")),
                Integer.parseInt(fm.getOrDefault("sira", "0").strip()),
                String.join("\n", anlatim).strip(), sorular);
    }

    private static MulakatSoru soruKur(String id, String zorlukEtiket,
                                       Map<String, List<String>> bolumler, Path dosya) {
        MulakatZorluk zorluk = MulakatZorluk.ayikla(zorlukEtiket);
        String soru = bolum(bolumler, "Soru");
        String aciklama = bolum(bolumler, "Açıklama");
        if (soru.isBlank()) {
            throw new IllegalStateException("Soru '%s' boş '## Soru' içeriyor (%s)".formatted(id, dosya));
        }
        if (aciklama.isBlank()) {
            throw new IllegalStateException("Soru '%s' boş '## Açıklama' içeriyor (%s)".formatted(id, dosya));
        }
        List<MulakatSik> siklar = new ArrayList<>();
        for (String harf : List.of("A", "B", "C", "D")) {
            List<String> ham = bolumler.get(harf);
            if (ham == null) {
                throw new IllegalStateException(
                        "Soru '%s' eksik şık: '## %s' yok (%s)".formatted(id, harf, dosya));
            }
            MulakatSik sik = sikKur(ham);
            if (sik.metin().isBlank()) {
                throw new IllegalStateException(
                        "Soru '%s' şıkkı '## %s' boş metin içeriyor (%s)".formatted(id, harf, dosya));
            }
            siklar.add(sik);
        }
        String dogruHarf = bolum(bolumler, "Doğru").strip();
        // TEK harf A|B|C|D olmalı — "ABCD".indexOf("AB")==0 tuzağına düşme (çok-karakter reddi).
        int dogruIndex = dogruHarf.length() == 1 ? "ABCD".indexOf(dogruHarf) : -1;
        if (dogruIndex < 0) {
            throw new IllegalStateException(
                    "Soru '%s' geçersiz '## Doğru' (tek harf A|B|C|D olmalı, bulunan: '%s') (%s)"
                            .formatted(id, dogruHarf, dosya));
        }
        return new MulakatSoru(id, soru, siklar, dogruIndex, aciklama, zorluk);
    }

    /** Şık gövdesinden 🤖 satırını AI notu, kalanı şık metni olarak ayırır. */
    private static MulakatSik sikKur(List<String> ham) {
        List<String> metin = new ArrayList<>();
        List<String> notlar = new ArrayList<>();
        for (String s : ham) {
            String t = s.stripLeading();
            if (t.startsWith(ROBOT)) {
                notlar.add(t.substring(ROBOT.length()).strip());
            } else {
                metin.add(s);
            }
        }
        return new MulakatSik(String.join("\n", metin).strip(), String.join("\n", notlar).strip());
    }

    /**
     * "---" ile çevrili frontmatter'ı fm'e yazar; gövdenin başladığı satır indeksini döner.
     * Frontmatter YOKSA (dosya "---" ile başlamıyorsa) veya AÇILIP kapanmıyorsa fail-fast.
     */
    private static int frontmatterOku(List<String> satirlar, Map<String, String> fm, Path dosya) {
        if (satirlar.isEmpty() || !satirlar.get(0).equals("---")) {
            throw new IllegalStateException("Frontmatter (---) eksik: " + dosya);
        }
        int i = 1;
        while (i < satirlar.size() && !satirlar.get(i).equals("---")) {
            String satir = satirlar.get(i);
            int ayrac = satir.indexOf(':');
            if (ayrac > 0) {
                String deger = satir.substring(ayrac + 1).strip();
                if (deger.length() >= 2 && deger.startsWith("\"") && deger.endsWith("\"")) {
                    deger = deger.substring(1, deger.length() - 1);
                }
                fm.put(satir.substring(0, ayrac).strip(), deger);
            }
            i++;
        }
        if (i >= satirlar.size()) {
            throw new IllegalStateException("Frontmatter kapanışı (---) eksik: " + dosya);
        }
        return i + 1; // kapanış "---" satırından sonrası
    }

    private static String bolum(Map<String, List<String>> bolumler, String ad) {
        List<String> satirlar = bolumler.get(ad);
        return satirlar == null ? "" : String.join("\n", satirlar).strip();
    }

    private static List<String> satirlar(Path dosya) {
        try {
            return Files.readAllLines(dosya);
        } catch (IOException e) {
            throw new UncheckedIOException("dosya okunamadı: " + dosya, e);
        }
    }
}
