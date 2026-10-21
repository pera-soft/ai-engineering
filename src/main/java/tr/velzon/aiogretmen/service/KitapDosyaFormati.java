package tr.velzon.aiogretmen.service;

import tr.velzon.aiogretmen.types.NotParcasi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Kitap notu markdown FORMATI — parse + serialize + slug + parçalama için TEK doğruluk kaynağı.
 * Eskiden bu format bilgisi KitapService içinde 4 metoda dağılmıştı (yazan / RAG-parse eden /
 * listeleyen / bağlam kuran); format değişince sınıf-içi 4 yer açılıyordu (connascence of format).
 * Mimari inceleme: tek tipe toplandı — format değişimi artık burada lokalize.
 */
final class KitapDosyaFormati {

    private KitapDosyaFormati() {
    }

    static final Pattern ETIKET_SATIRI = Pattern.compile("(?m)^etiketler: ([0-9,\\s]+)$");
    /** Bir "## Not — TARİH" bloğunu yakalar: g1=tarih, g2=gövde (sonraki başlığa/EOF'a kadar). */
    static final Pattern NOT_BLOK = Pattern.compile(
            "(?ms)^## Not(?: — (\\S+))?[^\\n]*\\n(.*?)(?=^## Not|\\z)");
    static final Pattern ETIKET_ILK_SATIR = Pattern.compile("(?m)^etiketler:[^\\n]*\\n?");

    static String slugla(String kitap) {
        String s = kitap.toLowerCase(Locale.forLanguageTag("tr"))
                .replace('ç', 'c').replace('ğ', 'g').replace('ı', 'i')
                .replace('ö', 'o').replace('ş', 's').replace('ü', 'u');
        s = s.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return s.isBlank() ? "kitap" : s;
    }

    /** Idempotent kimlik: aynı blok tekrar indekslenirse üzerine yazılır (dup yok). */
    static String parcaId(String kaynak, String tarih, String metin) {
        return slugla(kaynak) + "#" + tarih + "#" + Integer.toHexString(metin.hashCode());
    }

    /** Bir kitap dosyasının içeriğindeki tüm "## Not" bloklarını embed'lenebilir parçalara ayırır. */
    static List<NotParcasi> parcalariAyikla(String icerik, String kaynak) {
        List<NotParcasi> sonuc = new ArrayList<>();
        Matcher m = NOT_BLOK.matcher(icerik);
        while (m.find()) {
            String tarih = m.group(1) == null ? "" : m.group(1);
            String govde = m.group(2) == null ? "" : m.group(2);
            String metin = ETIKET_ILK_SATIR.matcher(govde).replaceFirst("").strip();
            if (metin.isBlank()) {
                continue;
            }
            sonuc.add(new NotParcasi(parcaId(kaynak, tarih, metin), kaynak, tarih, metin));
        }
        return sonuc;
    }

    static List<Integer> tumEtiketler(String icerik) {
        TreeSet<Integer> sonuc = new TreeSet<>();
        Matcher m = ETIKET_SATIRI.matcher(icerik);
        while (m.find()) {
            for (String parca : m.group(1).split(",")) {
                try {
                    sonuc.add(Integer.parseInt(parca.strip()));
                } catch (NumberFormatException ignored) {
                    // geçersiz etiket atlanır
                }
            }
        }
        return new ArrayList<>(sonuc);
    }

    static List<Integer> etiketleriSinirla(List<Integer> ham) {
        if (ham == null || ham.isEmpty()) {
            return List.of(1);
        }
        return ham.stream().filter(e -> e >= 1 && e <= 12).distinct().limit(4).toList();
    }

    static String frontmatterDeger(String icerik, String anahtar, String varsayilan) {
        Matcher m = Pattern.compile("(?m)^" + anahtar + ": \"(.+)\"$").matcher(icerik);
        return m.find() ? m.group(1) : varsayilan;
    }

    static String oku(Path dosya) {
        try {
            return Files.readString(dosya);
        } catch (IOException e) {
            throw new UncheckedIOException("dosya okunamadı: " + dosya, e);
        }
    }

    /** Dosya ilk kez oluşturulurken yazılacak frontmatter başlığı. */
    static String dosyaBasligi(String kitap) {
        return "---\nkitap: \"" + kitap.replace("\"", "'") + "\"\n---\n";
    }

    /** Yeni bir not bloğunun markdown metni ("## Not — TARİH" + etiketler + özet + kavramlar). */
    static String blokMetni(String ozet, List<String> kavramlar, List<Integer> etiketler, LocalDate tarih) {
        return "\n## Not — " + tarih + "\netiketler: "
                + etiketler.stream().map(String::valueOf).collect(Collectors.joining(","))
                + "\n\n" + ozet
                + (kavramlar == null || kavramlar.isEmpty() ? ""
                        : "\n\nAnahtar kavramlar: " + String.join(", ", kavramlar))
                + "\n";
    }
}
