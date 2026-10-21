package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tr.velzon.aiogretmen.ai.AnlamArama;
import tr.velzon.aiogretmen.ai.JsonCevapParser;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiYanit;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.repo.ContentRepo;
import tr.velzon.aiogretmen.types.BenzerNot;
import tr.velzon.aiogretmen.types.NotParcasi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KitapServiceTest {

    static final String ISLEM_JSON = """
            {"ozet": "- Harness modelin dışındaki her şeydir\\n- Guide/sensor ayrımı",
             "etiketler": [1, 4],
             "anahtar_kavramlar": ["harness", "fitness function"]}""";

    @TempDir
    Path tempDir;

    OgretmenAiClient aiClient = mock(OgretmenAiClient.class);
    SahteAnlam anlam = new SahteAnlam();
    KitapService servis;

    @BeforeEach
    void kur() {
        AppProperties props = new AppProperties("src/test/resources/test-content", "prompts",
                tempDir.toString(), null, null, null, null);
        servis = new KitapService(props, aiClient,
                new ContentRepo(Path.of("src/test/resources/test-content")),
                new PromptFactory(Path.of("prompts")), new JsonCevapParser(), anlam);
    }

    @Test
    void goruntudenEkleBosVeyaNullGoruntudeHataAtar() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> servis.goruntudenEkle("AI Engineering", null, "image/png"))
                .isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> servis.goruntudenEkle("AI Engineering", new byte[0], "image/png"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void notEkleDosyaOlusturur_etiketVeOzetle() throws Exception {
        when(aiClient.cagir(any())).thenReturn(new AiYanit(ISLEM_JSON, 10, 5, "m"));

        var not = servis.notEkle("AI Engineering", "ham not metni");

        assertThat(not.etiketler()).containsExactly(1, 4);
        Path dosya = tempDir.resolve("kitaplar/ai-engineering.md");
        assertThat(dosya).exists();
        String icerik = Files.readString(dosya);
        assertThat(icerik).contains("kitap: \"AI Engineering\"");
        assertThat(icerik).contains("etiketler: 1,4");
        assertThat(icerik).contains("Harness modelin dışındaki");
    }

    @Test
    void ikinciNotAyniDosyayaBirikir() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit(ISLEM_JSON, 10, 5, "m"));

        servis.notEkle("AI Engineering", "not 1");
        servis.notEkle("AI Engineering", "not 2");

        var kitaplar = servis.kitaplar();
        assertThat(kitaplar).hasSize(1);
        assertThat(kitaplar.get(0).get("notSayisi")).isEqualTo(2);
    }

    @Test
    void baglamIcinEtiketEslesenKitabiDondurur() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit(ISLEM_JSON, 10, 5, "m")); // etiketler 1,4

        servis.notEkle("AI Engineering", "not");

        assertThat(servis.baglamIcin(1)).contains("Harness modelin dışındaki");
        assertThat(servis.baglamIcin(4)).contains("Harness modelin dışındaki");
        assertThat(servis.baglamIcin(9)).contains("eşleşen kitap notu yok");
    }

    @Test
    void goruntudenEkleOnceMetinCikarirSonraIsler() {
        when(aiClient.cagirGoruntulu(any(), any(), anyString(), anyString(), any(), anyString(), anyInt()))
                .thenReturn(new AiYanit("## Sayfadan çıkan metin\nHarness tanımı...", 50, 30, "m"));
        when(aiClient.cagir(any())).thenReturn(new AiYanit(ISLEM_JSON, 10, 5, "m"));

        var not = servis.goruntudenEkle("AI Engineering", new byte[]{1, 2, 3}, "image/jpeg");

        assertThat(not.cikanMetin()).contains("Sayfadan çıkan metin");
        assertThat(not.etiketler()).containsExactly(1, 4);
        assertThat(tempDir.resolve("kitaplar/ai-engineering.md")).exists();
    }

    @Test
    void hicKitapYokkenBosVarsayilanlar() {
        assertThat(servis.kitaplar()).isEmpty();
        assertThat(servis.baglamIcin(1)).contains("Kitap notu yok");
    }

    // --- ADR-0005: anlam-eşleşmeli bağlam ---

    @Test
    void notEkleAnlamDeposunaIndeksler() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit(ISLEM_JSON, 10, 5, "m"));

        servis.notEkle("AI Engineering", "ham not");

        assertThat(anlam.indekslenen).hasSize(1);
        assertThat(anlam.indekslenen.get(0).metin()).contains("Harness modelin dışındaki");
    }

    @Test
    void baglamIcinAnlamSonuclariBirlestirir() {
        anlam.sonuc = List.of(new BenzerNot("Anlamca en yakın not.", 0.91),
                new BenzerNot("İkinci en yakın not.", 0.80));

        String baglam = servis.baglamIcinAnlam("bir sorgu", 1);

        assertThat(baglam).contains("Anlamca en yakın not.").contains("İkinci en yakın not.");
    }

    @Test
    void baglamIcinAnlamBosDepodaEtiketEslesmesineDuser() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit(ISLEM_JSON, 10, 5, "m")); // etiketler 1,4
        servis.notEkle("AI Engineering", "not");
        anlam.sonuc = List.of(); // anlam araması boş → fallback

        String baglam = servis.baglamIcinAnlam("alakasız sorgu", 1);

        assertThat(baglam).contains("Harness modelin dışındaki"); // hafta-1 etiketli not (baglamIcin fallback)
    }

    /** Yapılandırılabilir sahte AnlamArama (Spring AI'siz — bu sınıf ai/ dışında). */
    static class SahteAnlam implements AnlamArama {
        boolean aktif = true;
        List<BenzerNot> sonuc = List.of();
        final List<NotParcasi> indekslenen = new ArrayList<>();

        @Override public boolean aktif() { return aktif; }
        @Override public void indexle(List<NotParcasi> parcalar) { indekslenen.addAll(parcalar); }
        @Override public List<BenzerNot> benzerleri(String sorgu, int k) { return sonuc; }
        @Override public void temizle() { indekslenen.clear(); }
        @Override public int sayac() { return indekslenen.size(); }
    }
}
