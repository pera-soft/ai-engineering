package tr.velzon.aiogretmen.web;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Faz 10 (ADR-0017) ön yüz sözleşmesi — statik dosyaların içerik iddiaları.
 * Tarayıcı davranışı elle doğrulanır; burada yalnız kırılması kolay ve pahalı kurallar tutulur:
 * ziyaretçi anahtarı TEK çağrı noktasından (sohbet) gider, açılış sunucu kullanım ucuna bağlı değildir,
 * site metinleri nötrdür ve kaldırılan kimlik sistemine ait ölü kalıntı yoktur.
 */
class SiteOnYuzIcerikTest {

    private static final Path STATIK = Path.of("src/main/resources/static");

    private static String oku(String ad) throws IOException {
        return Files.readString(STATIK.resolve(ad));
    }

    private static int say(String metin, String aranan) {
        Matcher m = Pattern.compile(Pattern.quote(aranan)).matcher(metin);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    /** Anahtar başlığı app.js'te tek yerde kurulur — başka bir uca sızmanın en ucuz bekçisi. */
    @Test
    void anahtarBasligiTekCagriNoktasinda() throws IOException {
        String app = oku("app.js");
        assertThat(say(app, "X-OpenRouter-Anahtar")).isEqualTo(1);
        // tek çağrı noktası sohbet isteğinin yanında olmalı
        int baslik = app.indexOf("X-OpenRouter-Anahtar");
        int sohbet = app.lastIndexOf("\"/api/sohbet\"", baslik);
        assertThat(sohbet).isPositive();
        assertThat(baslik - sohbet).isLessThan(400);
        // mülakat modülü anahtarı hiç bilmez
        assertThat(oku("mulakat.js")).doesNotContain("X-OpenRouter-Anahtar").doesNotContain("aio.openrouter");
    }

    /**
     * Geç dönen 401 (eski anahtarla gönderilmiş istek) bu arada kaydedilmiş YENİ anahtarı silmez:
     * silme, gönderim anındaki anahtar hâlâ kayıtlıysa yapılır (compare-and-clear).
     */
    @Test
    void gecDonen401YeniAnahtariSilmez() throws IOException {
        String app = oku("app.js");
        int hataDali = app.indexOf("h.status === 401 ||");
        assertThat(hataDali).isPositive();
        int karsilastir = app.indexOf("if (anahtarOku() === anahtar)", hataDali);
        int sil = app.indexOf("anahtarSil();", hataDali);
        assertThat(karsilastir).isPositive();
        assertThat(karsilastir).isLessThan(sil); // silme yalnız karşılaştırmanın içinde
    }

    /** Anahtar hataları mesaj metniyle değil, sunucunun kararlı {@code neden} koduyla ayırt edilir. */
    @Test
    void anahtarHatasiNedenKoduylaAyirtEdilir() throws IOException {
        String app = oku("app.js");
        assertThat(app).doesNotContain("/anahtar/i");
        assertThat(app).contains("\"ANAHTAR_GEREKLI\"").contains("\"ANAHTAR_BICIMI\"");
    }

    /**
     * Sitede anahtarla: kutucuk yalnız AI kapısının işaretlediği oku/tartış görevinde kilitli; kendin
     * işaretlenen (yaz) görev geri alınabilsin.
     */
    @Test
    void kendinIsaretlenenGorevKutusuKilitlenmez() throws IOException {
        assertThat(oku("app.js")).contains("(durum.sohbetAcik && !kendinIsaretler(tip))")
                .doesNotContain("kilitli || durum.sohbetAcik;");
    }

    /** Anahtar hiçbir zaman konsola yazılmaz (ön yüzde console çağrısı yok). */
    @Test
    void konsolaYazimYok() throws IOException {
        assertThat(oku("app.js")).doesNotContain("console.");
        assertThat(oku("mulakat.js")).doesNotContain("console.");
    }

    /** Eski tek bayrak (sunucu anahtarı → /api/kullanim) kalktı; ikiye ayrıldı: sunucuAi + sohbetAcik. */
    @Test
    void acilisSunucuKullaniminaBagliDegil() throws IOException {
        String app = oku("app.js");
        assertThat(app).doesNotContain("aiAktif").doesNotContain("/api/kullanim");
        assertThat(app).contains("sunucuAi").contains("sohbetAcik");
        assertThat(oku("mulakat.js")).doesNotContain("aiAktif");
    }

    /** Girişsiz site: 401 artık "girişe git" anlamına gelmez; eski "AI henüz açılmadı" metni yok. */
    @Test
    void eskiSiteMetinleriYok() throws IOException {
        String app = oku("app.js");
        assertThat(app).doesNotContain("henüz açılmadı").doesNotContain("giris.html").doesNotContain("/giris");
        assertThat(app).contains("AI öğretmenle sohbet (isteğe bağlı)")
                .contains("Okudum ✓").contains("Yaptım ✓").contains("Tartıştım ✓")
                .contains("İlerleme kodum").contains("Geri yükle");
    }

    /** Ana sayfa nötr; kılavuzda sitede gizlenen yerel-mod paragrafları işaretli. */
    @Test
    void indexNotrVeKilavuzModaGore() throws IOException {
        String index = oku("index.html");
        assertThat(index.toLowerCase()).doesNotContain("pera");
        assertThat(index).contains("data-yalniz-yerel").contains("data-yalniz-site");
    }

    /** Silinen kimlik sayfalarının CSS'i kalmadı; hata sayfalarının kullandığı kart kaldı. */
    @Test
    void oluKimlikCssYok() throws IOException {
        String css = oku("style.css");
        assertThat(css).doesNotContain(".kimlik-kvkk").doesNotContain(".kimlik-form")
                .doesNotContain(".kimlik-mesaj").doesNotContain(".goz-btn");
        assertThat(css).contains(".kimlik-kart");
    }
}
