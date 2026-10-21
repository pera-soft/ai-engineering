package tr.velzon.aiogretmen.repo;

import tr.velzon.aiogretmen.types.Gun;
import tr.velzon.aiogretmen.types.Hafta;
import tr.velzon.aiogretmen.types.QuizSorusu;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * content/ altındaki frontmatter'lı markdown müfredatını açılışta okuyup bellekte tutar.
 * Format sözleşmesi: CLAUDE_CODE_BRIEF.md §5 ve docs/adr/0002.
 */
@org.springframework.stereotype.Repository
public class ContentRepo {

    private static final Pattern HAFTA_DIZIN = Pattern.compile("hafta-(\\d{2})");
    private static final Pattern GUN_DOSYA = Pattern.compile("gun-(\\d+)\\.md");
    private static final Pattern QUIZ_SORU = Pattern.compile("^### S\\d+: (.+)$");
    private static final Pattern QUIZ_SECENEK = Pattern.compile("^- \\[([ x])] (.+)$");
    private static final Pattern QUIZ_ACIKLAMA = Pattern.compile("^> Açıklama: (.+)$");

    private final List<Hafta> haftalar;
    private final Path contentDir;

    @org.springframework.beans.factory.annotation.Autowired
    public ContentRepo(tr.velzon.aiogretmen.config.AppProperties props) {
        this(Path.of(props.contentDir()));
    }

    public ContentRepo(Path contentDir) {
        if (!Files.isDirectory(contentDir)) {
            throw new IllegalStateException(
                    "content dizini bulunamadı: " + contentDir.toAbsolutePath()
                    + " — önce `node tools/html-to-content.mjs` çalıştırın.");
        }
        this.contentDir = contentDir;
        this.haftalar = yukle(contentDir);
    }

    /** Ham hafta.md içeriği — AI sistem bağlamına enjeksiyon için (brief 7.1). */
    public String hamHafta(int no) {
        return hamOku(haftaDizini(no).resolve("hafta.md"));
    }

    /** Ham gun-N.md içeriği; gün yoksa boş string. */
    public String hamGun(int hafta, int gun) {
        Path dosya = haftaDizini(hafta).resolve("gun-" + gun + ".md");
        return Files.isRegularFile(dosya) ? hamOku(dosya) : "";
    }

    private Path haftaDizini(int no) {
        return contentDir.resolve(String.format("hafta-%02d", no));
    }

    private static String hamOku(Path dosya) {
        try {
            return Files.readString(dosya);
        } catch (IOException e) {
            throw new UncheckedIOException("dosya okunamadı: " + dosya, e);
        }
    }

    public List<Hafta> haftalar() {
        return haftalar;
    }

    public Optional<Hafta> hafta(int no) {
        return haftalar.stream().filter(h -> h.no() == no).findFirst();
    }

    private static List<Hafta> yukle(Path contentDir) {
        try (Stream<Path> dirs = Files.list(contentDir)) {
            return dirs.filter(Files::isDirectory)
                    .filter(d -> HAFTA_DIZIN.matcher(d.getFileName().toString()).matches())
                    .sorted(Comparator.comparing(d -> d.getFileName().toString()))
                    .map(ContentRepo::haftaOku)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("content dizini okunamadı: " + contentDir, e);
        }
    }

    private static Hafta haftaOku(Path haftaDir) {
        Belge haftaMd = Belge.oku(haftaDir.resolve("hafta.md"));
        String haftaNo = haftaMd.frontmatter().get("hafta");
        if (haftaNo == null) {
            throw new IllegalStateException("frontmatter'da 'hafta:' alanı eksik: "
                    + haftaDir.resolve("hafta.md").toAbsolutePath());
        }
        int no = Integer.parseInt(haftaNo);

        List<Gun> gunler = gunleriOku(haftaDir);
        List<QuizSorusu> quiz = quizParse(haftaMd.bolum("Quiz"));

        return new Hafta(no,
                haftaMd.frontmatter().getOrDefault("baslik", ""),
                haftaMd.frontmatter().getOrDefault("hedef", ""),
                haftaMd.frontmatter().getOrDefault("kaynak", ""),
                gunler, quiz);
    }

    private static List<Gun> gunleriOku(Path haftaDir) {
        try (Stream<Path> files = Files.list(haftaDir)) {
            return files.filter(f -> GUN_DOSYA.matcher(f.getFileName().toString()).matches())
                    .sorted(Comparator.comparingInt(f -> {
                        Matcher m = GUN_DOSYA.matcher(f.getFileName().toString());
                        m.matches();
                        return Integer.parseInt(m.group(1));
                    }))
                    .map(ContentRepo::gunOku)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("hafta dizini okunamadı: " + haftaDir, e);
        }
    }

    private static Gun gunOku(Path gunMd) {
        Belge belge = Belge.oku(gunMd);
        Map<String, String> fm = belge.frontmatter();
        List<String> gorevler = belge.bolum("Görevler").stream()
                .map(s -> s.replaceFirst("^- \\[[ x]] ", ""))
                .filter(s -> !s.isBlank())
                .toList();
        return new Gun(
                Integer.parseInt(fm.get("hafta")),
                Integer.parseInt(fm.get("gun")),
                fm.getOrDefault("baslik", ""),
                String.join("\n", belge.bolum("Ders")).strip(),
                gorevler);
    }

    private static List<QuizSorusu> quizParse(List<String> satirlar) {
        List<QuizSorusu> sorular = new ArrayList<>();
        String soru = null, aciklama = "";
        List<String> secenekler = new ArrayList<>();
        int dogruIndex = -1;

        for (String satir : satirlar) {
            Matcher mSoru = QUIZ_SORU.matcher(satir);
            if (mSoru.matches()) {
                if (soru != null) {
                    sorular.add(new QuizSorusu(soru, List.copyOf(secenekler), dogruIndex, aciklama));
                }
                soru = mSoru.group(1);
                secenekler = new ArrayList<>();
                dogruIndex = -1;
                aciklama = "";
                continue;
            }
            Matcher mSec = QUIZ_SECENEK.matcher(satir);
            if (mSec.matches()) {
                if ("x".equals(mSec.group(1))) {
                    dogruIndex = secenekler.size();
                }
                secenekler.add(mSec.group(2));
                continue;
            }
            Matcher mAcik = QUIZ_ACIKLAMA.matcher(satir);
            if (mAcik.matches()) {
                aciklama = mAcik.group(1);
            }
        }
        if (soru != null) {
            sorular.add(new QuizSorusu(soru, List.copyOf(secenekler), dogruIndex, aciklama));
        }
        return sorular;
    }

    /** Frontmatter + "## Başlık" bölümlerine ayrılmış tek bir markdown dosyası. */
    private record Belge(Map<String, String> frontmatter, Map<String, List<String>> bolumler) {

        static Belge oku(Path dosya) {
            List<String> satirlar;
            try {
                satirlar = Files.readAllLines(dosya);
            } catch (IOException e) {
                throw new UncheckedIOException("dosya okunamadı: " + dosya, e);
            }

            Map<String, String> fm = new HashMap<>();
            Map<String, List<String>> bolumler = new HashMap<>();
            int i = 0;

            if (!satirlar.isEmpty() && satirlar.get(0).equals("---")) {
                i = 1;
                while (i < satirlar.size() && !satirlar.get(i).equals("---")) {
                    String satir = satirlar.get(i);
                    int ayrac = satir.indexOf(':');
                    if (ayrac > 0) {
                        fm.put(satir.substring(0, ayrac).strip(), yamlDeger(satir.substring(ayrac + 1).strip()));
                    }
                    i++;
                }
                i++; // kapanış "---"
            }

            String aktifBolum = null;
            for (; i < satirlar.size(); i++) {
                String satir = satirlar.get(i);
                if (satir.startsWith("## ")) {
                    aktifBolum = satir.substring(3).strip();
                    bolumler.put(aktifBolum, new ArrayList<>());
                } else if (aktifBolum != null) {
                    bolumler.get(aktifBolum).add(satir);
                }
            }
            return new Belge(fm, bolumler);
        }

        List<String> bolum(String ad) {
            return bolumler.getOrDefault(ad, List.of());
        }

        /** Çift tırnaklı YAML skalarını çözer: "a \"b\" \\ c" → a "b" \ c */
        private static String yamlDeger(String ham) {
            if (ham.length() >= 2 && ham.startsWith("\"") && ham.endsWith("\"")) {
                return ham.substring(1, ham.length() - 1)
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");
            }
            return ham;
        }
    }
}
