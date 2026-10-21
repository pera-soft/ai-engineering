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
    HafizaService servis;

    @BeforeEach
    void kur() {
        AppProperties props = new AppProperties("./content", "prompts",
                tempDir.resolve("hafiza").toString(), null, null, null, null);
        servis = new HafizaService(props, aiClient, new PromptFactory(Path.of("prompts")), new JsonCevapParser());
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

        servis.ogrenciNotlariniSil();

        assertThat(hafiza.resolve("OGRENCI.md")).doesNotExist();
        assertThat(hafiza.resolve("hafta-01")).doesNotExist();
        assertThat(hafiza.resolve("kitaplar/ai-engineering.md")).exists();
        assertThat(hafiza.resolve("vektor.json")).exists();
    }

    @Test
    void ogrenciNotlariniSilBosDizindeHataAtmaz() {
        servis.ogrenciNotlariniSil(); // hafiza dizini henüz yokken bile idempotent (patlamamalı)
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
}
