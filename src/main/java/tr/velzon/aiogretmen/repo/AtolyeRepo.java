package tr.velzon.aiogretmen.repo;

import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.types.AtolyeAlistirma;
import tr.velzon.aiogretmen.types.AtolyeKategori;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Prompt Atölyesi içeriğini (content/prompt-atolyesi/NN-*.md) açılışta okuyup bellekte tutar
 * (ContentRepo deseni). İçerik = veri: kategori sırası dosya adı önekine göre (01, 07…).
 *
 * <p>Dosya formatı: frontmatter (slug, baslik, aciklama) + gövdede her alıştırma
 * "# &lt;id&gt; | &lt;teknik&gt; | ai:&lt;true|false&gt;" satırıyla başlar; ardından
 * "## Görev", "## Kötü örnek" (opsiyonel), "## İpucu", "## İdeal çözüm", "## Neden" bölümleri.
 * Bölüm içeriği bir sonraki "## " ya da "# " başlığına kadardır (kod fence dahil, çok satırlı).
 */
@org.springframework.stereotype.Repository
public class AtolyeRepo {

    /** "# net-gorev | Net görev verme | ai:false" satırını yakalar (id tire içerebilir). */
    private static final Pattern ALISTIRMA_BAS =
            Pattern.compile("^#\\s+(.+?)\\s*\\|\\s*(.+?)\\s*\\|\\s*ai:(true|false)\\s*$");
    private static final String ATOLYE_DIZINI = "prompt-atolyesi";

    private final List<AtolyeKategori> kategoriler;

    @org.springframework.beans.factory.annotation.Autowired
    public AtolyeRepo(AppProperties props) {
        this(Path.of(props.contentDir()).resolve(ATOLYE_DIZINI));
    }

    public AtolyeRepo(Path atolyeDir) {
        if (!Files.isDirectory(atolyeDir)) {
            throw new IllegalStateException(
                    "Prompt atölyesi dizini bulunamadı: " + atolyeDir.toAbsolutePath());
        }
        this.kategoriler = yukle(atolyeDir);
    }

    public List<AtolyeKategori> kategoriler() {
        return kategoriler;
    }

    private static List<AtolyeKategori> yukle(Path dir) {
        try (Stream<Path> dosyalar = Files.list(dir)) {
            return dosyalar
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().endsWith(".md"))
                    .sorted(Comparator.comparing(f -> f.getFileName().toString()))
                    .map(AtolyeRepo::kategoriOku)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Prompt atölyesi dizini okunamadı: " + dir, e);
        }
    }

    private static AtolyeKategori kategoriOku(Path dosya) {
        List<String> satirlar;
        try {
            satirlar = Files.readAllLines(dosya);
        } catch (IOException e) {
            throw new UncheckedIOException("dosya okunamadı: " + dosya, e);
        }
        Map<String, String> fm = new HashMap<>();
        int bas = frontmatterOku(satirlar, fm);
        List<AtolyeAlistirma> alistirmalar = alistirmalariParse(satirlar, bas);
        return new AtolyeKategori(
                fm.getOrDefault("slug", ""),
                fm.getOrDefault("baslik", ""),
                fm.getOrDefault("aciklama", ""),
                alistirmalar);
    }

    /** "---" ile çevrili frontmatter'ı fm'e yazar; gövdenin başladığı satır indeksini döner. */
    private static int frontmatterOku(List<String> satirlar, Map<String, String> fm) {
        int i = 0;
        if (!satirlar.isEmpty() && satirlar.get(0).equals("---")) {
            i = 1;
            while (i < satirlar.size() && !satirlar.get(i).equals("---")) {
                String satir = satirlar.get(i);
                int ayrac = satir.indexOf(':');
                if (ayrac > 0) {
                    fm.put(satir.substring(0, ayrac).strip(), satir.substring(ayrac + 1).strip());
                }
                i++;
            }
            i++; // kapanış "---"
        }
        return i;
    }

    private static List<AtolyeAlistirma> alistirmalariParse(List<String> satirlar, int basla) {
        List<AtolyeAlistirma> sonuc = new ArrayList<>();
        String id = null, teknik = null;
        boolean aiDegerlendir = false;
        Map<String, List<String>> bolumler = null;
        String aktifBolum = null;
        boolean fenceIci = false; // kod fence'i (```) içinde başlık algılama devre dışı

        for (int i = basla; i < satirlar.size(); i++) {
            String satir = satirlar.get(i);
            if (satir.strip().startsWith("```")) {
                fenceIci = !fenceIci;
            } else if (!fenceIci) {
                Matcher m = ALISTIRMA_BAS.matcher(satir);
                if (m.matches()) {
                    if (id != null) {
                        sonuc.add(kur(id, teknik, aiDegerlendir, bolumler));
                    }
                    id = m.group(1).strip();
                    teknik = m.group(2).strip();
                    aiDegerlendir = Boolean.parseBoolean(m.group(3));
                    bolumler = new HashMap<>();
                    aktifBolum = null;
                    continue;
                }
                if (satir.startsWith("## ")) {
                    aktifBolum = satir.substring(3).strip();
                    if (bolumler != null) {
                        bolumler.put(aktifBolum, new ArrayList<>());
                    }
                    continue;
                }
            }
            if (aktifBolum != null && bolumler != null) {
                bolumler.get(aktifBolum).add(satir);
            }
        }
        if (id != null) {
            sonuc.add(kur(id, teknik, aiDegerlendir, bolumler));
        }
        return sonuc;
    }

    private static AtolyeAlistirma kur(String id, String teknik, boolean aiDegerlendir,
                                       Map<String, List<String>> bolumler) {
        return new AtolyeAlistirma(id, teknik,
                bolum(bolumler, "Görev"),
                bolum(bolumler, "Kötü örnek"), // opsiyonel — yoksa boş string
                bolum(bolumler, "İpucu"),
                bolum(bolumler, "İdeal çözüm"),
                bolum(bolumler, "Neden"),
                aiDegerlendir);
    }

    private static String bolum(Map<String, List<String>> bolumler, String ad) {
        List<String> satirlar = bolumler.get(ad);
        return satirlar == null ? "" : String.join("\n", satirlar).strip();
    }
}
