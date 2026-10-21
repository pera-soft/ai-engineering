package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tr.velzon.aiogretmen.ai.JsonCevapParser;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiYanit;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.config.AppProperties;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HafizaServiceTest {

    @TempDir
    Path tempDir;

    OgretmenAiClient aiClient = mock(OgretmenAiClient.class);
    /** Aktif kullanıcı test içinde değiştirilebilir (Faz 9b: iki kullanıcı izolasyonu). */
    String aktif = "yerel";
    HafizaService servis;

    @BeforeEach
    void kur() {
        AppProperties props = new AppProperties("./content", "prompts",
                tempDir.resolve("hafiza").toString(), null, null, null, null);
        servis = new HafizaService(props, aiClient, new PromptFactory(Path.of("prompts")), new JsonCevapParser(),
                () -> aktif);
        when(aiClient.aktif()).thenReturn(true);
    }

    @Test
    void gecerliYanitHafizaDosyalariniYazar() throws Exception {
        when(aiClient.cagir(any())).thenReturn(new AiYanit("""
                {"ogrenci_profili": "# Profil\\nHarness kavramını anladı.",
                 "hafta_notlari": "- 2026-07-03: guides/sensors ayrımını sordu."}""", 10, 5, "m"));

        servis.guncelle(1, "SOHBET", "etkileşim metni");

        Path profil = tempDir.resolve("hafiza/OGRENCI.md");
        Path notlar = tempDir.resolve("hafiza/hafta-01/notlar.md");
        assertThat(profil).exists();
        assertThat(Files.readString(profil)).contains("Harness kavramını anladı");
        assertThat(notlar).exists();
        assertThat(Files.readString(notlar)).contains("guides/sensors");
    }

    @Test
    void ogrenciNotlariniSilProfilVeHaftalariSilerKitaplariVeVektoruKorur() throws Exception {
        Path hafiza = tempDir.resolve("hafiza");
        Files.createDirectories(hafiza.resolve("hafta-01"));
        Files.createDirectories(hafiza.resolve("kitaplar"));
        Files.writeString(hafiza.resolve("OGRENCI.md"), "profil");
        Files.writeString(hafiza.resolve("hafta-01/notlar.md"), "not");
        Files.writeString(hafiza.resolve("kitaplar/ai-engineering.md"), "kitap"); // KORUNMALI
        Files.writeString(hafiza.resolve("vektor.json"), "{}");                    // KORUNMALI (RAG)

        servis.ogrenciNotlariniSil("yerel");

        assertThat(hafiza.resolve("OGRENCI.md")).doesNotExist();
        assertThat(hafiza.resolve("hafta-01")).doesNotExist();
        assertThat(hafiza.resolve("kitaplar/ai-engineering.md")).exists();
        assertThat(hafiza.resolve("vektor.json")).exists();
    }

    @Test
    void ogrenciNotlariniSilBosDizindeHataAtmaz() {
        servis.ogrenciNotlariniSil("yerel"); // hafiza dizini henüz yokken bile idempotent (patlamamalı)
    }

    @Test
    void hafizaBaglamiYazilanlariIcerir() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit(
                "{\"ogrenci_profili\": \"PROFIL_X\", \"hafta_notlari\": \"NOT_Y\"}", 1, 1, "m"));
        servis.guncelle(2, "DEGERLENDIRME", "x");

        String baglam = servis.hafizaBaglami(2);

        assertThat(baglam).contains("PROFIL_X").contains("NOT_Y");
    }

    @Test
    void bozukJsonDosyaYazmazAmaFirlatmaz() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit("json değil bu", 1, 1, "m"));

        servis.guncelle(1, "SOHBET", "x");

        assertThat(tempDir.resolve("hafiza/OGRENCI.md")).doesNotExist();
    }

    @Test
    void aiKapaliykenHicCagriYapmaz() {
        when(aiClient.aktif()).thenReturn(false);

        servis.guncelle(1, "SOHBET", "x");

        verify(aiClient, never()).cagir(any());
    }

    @Test
    void aiHatasiAkisiBozmaz() {
        when(aiClient.cagir(any())).thenThrow(new RuntimeException("API çöktü"));

        servis.guncelle(1, "SOHBET", "x"); // fırlatmamalı
    }

    @Test
    void hicDosyaYokkenVarsayilanlarDoner() {
        assertThat(servis.ogrenciProfili()).contains("Henüz gözlem yok");
        assertThat(servis.haftaNotlari(3)).contains("not yok");
    }

    // ---------- Faz 9b: kullanıcı başına hafıza ----------

    private void yanit(String profil, String not) {
        when(aiClient.cagir(any())).thenReturn(new AiYanit(
                "{\"ogrenci_profili\": \"" + profil + "\", \"hafta_notlari\": \"" + not + "\"}", 1, 1, "m"));
    }

    @Test
    void yerelKullaniciEskiYollaraYazar() {
        yanit("YEREL_PROFIL", "YEREL_NOT");
        servis.guncelle(1, "SOHBET", "x");

        assertThat(tempDir.resolve("hafiza/OGRENCI.md")).hasContent("YEREL_PROFIL");
        assertThat(tempDir.resolve("hafiza/hafta-01/notlar.md")).hasContent("YEREL_NOT");
        assertThat(tempDir.resolve("hafiza/kullanicilar")).doesNotExist();
    }

    @Test
    void ikiKullanicininHafizasiBirbirineKarismaz() {
        aktif = "a@pera.software";
        yanit("A_SIRRI_PROFIL", "A_SIRRI_NOT");
        servis.guncelle(1, "SOHBET", "x");

        aktif = "b@pera.software";
        assertThat(servis.hafizaBaglami(1)).doesNotContain("A_SIRRI").contains("Henüz gözlem yok");
        yanit("B_PROFIL", "B_NOT");
        servis.guncelle(1, "SOHBET", "x");
        assertThat(servis.hafizaBaglami(1)).contains("B_PROFIL").doesNotContain("A_SIRRI");

        aktif = "a@pera.software";
        assertThat(servis.hafizaBaglami(1)).contains("A_SIRRI_PROFIL").contains("A_SIRRI_NOT")
                .doesNotContain("B_PROFIL");

        // eski (yerel) kök kirlenmedi; dosyalar kullanicilar/<özet>/ altında
        assertThat(tempDir.resolve("hafiza/OGRENCI.md")).doesNotExist();
        assertThat(KisiselDizin.coz(tempDir.resolve("hafiza"), "a@pera.software").resolve("OGRENCI.md"))
                .hasContent("A_SIRRI_PROFIL");
        aktif = "yerel";
        assertThat(servis.hafizaBaglami(1)).doesNotContain("A_SIRRI").doesNotContain("B_PROFIL");
    }

    @Test
    void silmeYalnizVerilenKullaniciyiEtkiler() throws Exception {
        Path kok = tempDir.resolve("hafiza");
        Files.createDirectories(kok.resolve("kitaplar"));
        Files.writeString(kok.resolve("OGRENCI.md"), "yerel profil");
        aktif = "a@pera.software";
        yanit("A_PROFIL", "A_NOT");
        servis.guncelle(1, "SOHBET", "x");
        aktif = "b@pera.software";
        yanit("B_PROFIL", "B_NOT");
        servis.guncelle(1, "SOHBET", "x");

        servis.ogrenciNotlariniSil("b@pera.software");

        assertThat(KisiselDizin.coz(kok, "b@pera.software")).doesNotExist();
        assertThat(KisiselDizin.coz(kok, "a@pera.software").resolve("OGRENCI.md")).hasContent("A_PROFIL");
        assertThat(kok.resolve("OGRENCI.md")).hasContent("yerel profil");
        assertThat(kok.resolve("kitaplar")).isDirectory();

        // yerel sıfırlama başka kullanıcıların dizinine (kullanicilar/) DOKUNMAZ
        servis.ogrenciNotlariniSil("yerel");
        assertThat(kok.resolve("OGRENCI.md")).doesNotExist();
        assertThat(KisiselDizin.coz(kok, "a@pera.software").resolve("OGRENCI.md")).hasContent("A_PROFIL");
    }
}
