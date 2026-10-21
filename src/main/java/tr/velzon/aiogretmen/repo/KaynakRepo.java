package tr.velzon.aiogretmen.repo;

import tr.velzon.aiogretmen.types.Kaynak;

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
import java.util.stream.Stream;

/**
 * content/kaynaklar/*.md — Türkçe çalışma metinleri (kullanıcı İngilizce bilmiyor;
 * verdiği kaynaklar çevrilip buraya konur, bkz AGENTS.md). Açılışta okunur.
 */
@org.springframework.stereotype.Repository
public class KaynakRepo {

    private final List<Kaynak> kaynaklar;

    @org.springframework.beans.factory.annotation.Autowired
    public KaynakRepo(tr.velzon.aiogretmen.config.AppProperties props) {
        this(Path.of(props.contentDir()).resolve("kaynaklar"));
    }

    public KaynakRepo(Path kaynakDir) {
        this.kaynaklar = yukle(kaynakDir);
    }

    public List<Kaynak> hepsi() {
        return kaynaklar;
    }

    public Optional<Kaynak> bul(String slug) {
        return kaynaklar.stream().filter(k -> k.slug().equals(slug)).findFirst();
    }

    public List<Kaynak> haftaninKaynaklari(int hafta) {
        return kaynaklar.stream().filter(k -> k.etiketler().contains(hafta)).toList();
    }

    private static List<Kaynak> yukle(Path dir) {
        if (!Files.isDirectory(dir)) {
            return List.of(); // kaynaklar isteğe bağlı — dizin yoksa boş
        }
        try (Stream<Path> dosyalar = Files.list(dir)) {
            return dosyalar.filter(f -> f.getFileName().toString().endsWith(".md"))
                    .sorted(Comparator.comparing(f -> f.getFileName().toString()))
                    .map(KaynakRepo::parse)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("kaynaklar dizini okunamadı: " + dir, e);
        }
    }

    private static Kaynak parse(Path dosya) {
        String icerik;
        try {
            icerik = Files.readString(dosya);
        } catch (IOException e) {
            throw new UncheckedIOException("kaynak okunamadı: " + dosya, e);
        }

        Map<String, String> fm = new HashMap<>();
        String govde = icerik;
        if (icerik.startsWith("---")) {
            int kapanis = icerik.indexOf("\n---", 3);
            if (kapanis > 0) {
                for (String satir : icerik.substring(3, kapanis).split("\n")) {
                    int ayrac = satir.indexOf(':');
                    if (ayrac > 0) {
                        String deger = satir.substring(ayrac + 1).strip();
                        if (deger.length() >= 2 && deger.startsWith("\"") && deger.endsWith("\"")) {
                            deger = deger.substring(1, deger.length() - 1);
                        }
                        fm.put(satir.substring(0, ayrac).strip(), deger);
                    }
                }
                govde = icerik.substring(kapanis + 4).strip();
            }
        }

        List<Integer> etiketler = new ArrayList<>();
        for (String parca : fm.getOrDefault("etiketler", "").split(",")) {
            try {
                etiketler.add(Integer.parseInt(parca.strip()));
            } catch (NumberFormatException ignored) {
            }
        }

        String slug = dosya.getFileName().toString().replaceFirst("\\.md$", "");
        return new Kaynak(slug,
                fm.getOrDefault("baslik", slug),
                fm.getOrDefault("yazar", ""),
                fm.getOrDefault("tarih", ""),
                fm.getOrDefault("url", ""),
                List.copyOf(etiketler),
                govde);
    }
}
