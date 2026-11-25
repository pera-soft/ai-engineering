package tr.velzon.aiogretmen;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiYanit;
import tr.velzon.aiogretmen.config.ZiyaretciCerezi;
import tr.velzon.aiogretmen.config.ZiyaretciKullaniciBaglami;
import tr.velzon.aiogretmen.service.BaglamServisi;
import tr.velzon.aiogretmen.service.KisiselDizin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Faz 9b — {@code site} profilinde KİŞİSEL DOSYA VERİSİ + kullanım izolasyonu.
 * Başarısızlık ölçütü (plan): A'nın hafızası/raporu/kullanımı B'ye GÖRÜNMEMELİ; sıfırlama
 * yalnız sahibini etkilemeli; ortak kitap/vektör deposu sitede kapalı olmalı.
 *
 * <p>Girişsiz site (ADR-0017): kullanıcılar {@code aio_ziyaretci} çereziyle ayrışır ({@code kullanici_id = "z-" + kimlik}).
 *
 * <p>AI istemcisi sahte: prompt'lar yakalanır → "A'nın OGRENCI.md'si B'nin prompt'una girmez" HTTP
 * düzeyinde kanıtlanır.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sitekisisel;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key="
})
@ActiveProfiles("site")
@AutoConfigureMockMvc
class SiteKisiselVeriTest {

    private static Path kok;

    @DynamicPropertySource
    static void dizinler(DynamicPropertyRegistry r) throws IOException {
        kok = Files.createTempDirectory("site-kisisel");
        r.add("app.hafiza-dir", () -> kok.resolve("hafiza").toString());
        r.add("app.rapor-dir", () -> kok.resolve("raporlar").toString());
        r.add("app.vektor-dosyasi", () -> kok.resolve("hafiza/vektor.json").toString());
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    BaglamServisi baglamServisi;

    @Autowired
    tr.velzon.aiogretmen.service.RaporService raporService;

    @MockitoBean
    OgretmenAiClient ai;

    @BeforeEach
    void kur() {
        when(ai.aktif()).thenReturn(true);
        when(ai.cagir(any())).thenReturn(new AiYanit("cevap", 1, 1, "m"));
    }

    // ---------- yardımcılar ----------

    private static Path hafiza(String kullanici) {
        return KisiselDizin.coz(kok.resolve("hafiza"), kullanici);
    }

    private static Path rapor(String kullanici) {
        return KisiselDizin.coz(kok.resolve("raporlar"), kullanici);
    }

    private static void yaz(Path dosya, String icerik) throws IOException {
        Files.createDirectories(dosya.getParent());
        Files.writeString(dosya, icerik);
    }

    /** Etiketten geçerli (22 karakter) ziyaretçi kimliği: testler okunur kalsın. */
    private static String kimlik(String etiket) {
        return (etiket + "x".repeat(22)).substring(0, 22);
    }

    /** Ziyaretçinin {@code kullanici_id}'si ({@code "z-" + kimlik}). */
    private static String kullanici(String etiket) {
        return ZiyaretciKullaniciBaglami.ONEK + kimlik(etiket);
    }

    private static Cookie ziyaretci(String etiket) {
        return new Cookie(ZiyaretciCerezi.AD, kimlik(etiket));
    }

    private List<AiIstek> yakalananIstekler() {
        ArgumentCaptor<AiIstek> c = ArgumentCaptor.forClass(AiIstek.class);
        verify(ai, atLeastOnce()).cagir(c.capture());
        return c.getAllValues();
    }

    // ---------- hafıza ----------

    @Test
    void aninHafizasiBninPromptunaGirmez_http() throws Exception {
        Cookie a = ziyaretci("hafiza-a");
        Cookie b = ziyaretci("hafiza-b");
        yaz(hafiza(kullanici("hafiza-a")).resolve("OGRENCI.md"), "A_GIZLI_PROFIL");
        yaz(hafiza(kullanici("hafiza-a")).resolve("hafta-01/notlar.md"), "A_GIZLI_NOT");
        yaz(hafiza(kullanici("hafiza-b")).resolve("OGRENCI.md"), "B_PROFIL");
        yaz(kok.resolve("hafiza/OGRENCI.md"), "YEREL_GIZLI"); // eski tek-kullanıcı kökü de sızmamalı

        clearInvocations(ai);
        mvc.perform(post("/api/sohbet").cookie(b).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"hafta\":1,\"mesaj\":\"merhaba\"}")).andExpect(status().isOk());
        List<AiIstek> bIstekleri = yakalananIstekler();
        assertThat(bIstekleri).anySatisfy(i -> assertThat(i.sistemPrompt()).contains("B_PROFIL"));
        assertThat(bIstekleri).allSatisfy(i -> assertThat(i.sistemPrompt() + i.kullaniciMesaji())
                .doesNotContain("A_GIZLI").doesNotContain("YEREL_GIZLI"));

        clearInvocations(ai);
        mvc.perform(post("/api/sohbet").cookie(a).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"hafta\":1,\"mesaj\":\"selam\"}")).andExpect(status().isOk());
        assertThat(yakalananIstekler()).anySatisfy(i -> assertThat(i.sistemPrompt())
                .contains("A_GIZLI_PROFIL").contains("A_GIZLI_NOT").doesNotContain("B_PROFIL"));
    }

    @Test
    void aninHafizasiBninBaglamindaYok_servisKatmani() throws Exception {
        yaz(hafiza(kullanici("servis-a")).resolve("OGRENCI.md"), "SERVIS_A_SIRRI");
        MockHttpServletRequest istek = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(istek));
        try {
            istek.setAttribute(ZiyaretciCerezi.ISTEK_OZNITELIGI, kimlik("servis-b"));
            assertThat(baglamServisi.sistemBaglami(1, 1)).doesNotContain("SERVIS_A_SIRRI");
            istek.setAttribute(ZiyaretciCerezi.ISTEK_OZNITELIGI, kimlik("servis-a"));
            assertThat(baglamServisi.sistemBaglami(1, 1)).contains("SERVIS_A_SIRRI");
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    // ---------- rapor ----------

    @Test
    void raporOnbellegiKullaniciyaOzel_servisKatmani() throws Exception {
        // /api/rapor/** sitede kapalı (Faz 10); önbellek izolasyonu servis katmanında hâlâ doğrulanır
        MockHttpServletRequest istek = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(istek));
        try {
            istek.setAttribute(ZiyaretciCerezi.ISTEK_OZNITELIGI, kimlik("rapor-a"));
            when(ai.cagir(any())).thenReturn(new AiYanit("# A RAPORU", 1, 1, "m"));
            assertThat(raporService.rapor(1, false).cacheden()).isFalse();

            istek.setAttribute(ZiyaretciCerezi.ISTEK_OZNITELIGI, kimlik("rapor-b"));
            when(ai.cagir(any())).thenReturn(new AiYanit("# B RAPORU", 1, 1, "m"));
            var b = raporService.rapor(1, false);
            assertThat(b.cacheden()).isFalse();
            assertThat(b.markdown()).isEqualTo("# B RAPORU");

            istek.setAttribute(ZiyaretciCerezi.ISTEK_OZNITELIGI, kimlik("rapor-a"));
            var a = raporService.rapor(1, false);
            assertThat(a.cacheden()).isTrue();
            assertThat(a.markdown()).isEqualTo("# A RAPORU");
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
        assertThat(kok.resolve("raporlar/hafta-01.md")).doesNotExist();
    }

    // ---------- kullanım ----------

    @Test
    void kullanimYalnizKendiCagrilariniGosterir() throws Exception {
        Cookie b = ziyaretci("kullanim-b");
        jdbc.update("INSERT INTO ai_kullanim (kullanici_id, islem, model, input_tokens, output_tokens) "
                + "VALUES (?,'SOHBET','m',1000,1000)", kullanici("kullanim-a"));
        jdbc.update("INSERT INTO ai_kullanim (kullanici_id, islem, model, input_tokens, output_tokens) "
                + "VALUES (?,'SOHBET','m',7,3)", kullanici("kullanim-b"));

        mvc.perform(get("/api/kullanim").cookie(b))
                .andExpect(jsonPath("$.toplamCagri").value(1))
                .andExpect(jsonPath("$.toplamInputToken").value(7))
                .andExpect(jsonPath("$.toplamOutputToken").value(3));
    }

    // ---------- sıfırlama ----------

    @Test
    void sifirlamaYalnizKendiDosyalariniSiler() throws Exception {
        Cookie b = ziyaretci("sifir-b");
        yaz(hafiza(kullanici("sifir-a")).resolve("OGRENCI.md"), "A");
        yaz(rapor(kullanici("sifir-a")).resolve("hafta-01.md"), "A rapor");
        yaz(hafiza(kullanici("sifir-b")).resolve("OGRENCI.md"), "B");
        yaz(hafiza(kullanici("sifir-b")).resolve("hafta-02/notlar.md"), "B not");
        yaz(rapor(kullanici("sifir-b")).resolve("hafta-01.md"), "B rapor");

        mvc.perform(post("/api/sifirla").cookie(b).with(csrf())).andExpect(status().isOk());

        assertThat(hafiza(kullanici("sifir-b"))).doesNotExist();
        assertThat(rapor(kullanici("sifir-b"))).doesNotExist();
        assertThat(hafiza(kullanici("sifir-a")).resolve("OGRENCI.md")).hasContent("A");
        assertThat(rapor(kullanici("sifir-a")).resolve("hafta-01.md")).hasContent("A rapor");
    }

    // ---------- ortak kitap/vektör deposu sitede kapalı ----------

    @Test
    void kitapUclariSitede404() throws Exception {
        Cookie a = ziyaretci("kitap-a");
        mvc.perform(get("/api/kitaplar").cookie(a)).andExpect(status().isNotFound());
        mvc.perform(post("/api/kitap-notu").cookie(a).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"kitap\":\"K\",\"not\":\"n\"}")).andExpect(status().isNotFound());
        mvc.perform(post("/api/kitap-notu/goruntu").cookie(a).with(csrf())).andExpect(status().isNotFound());
    }
}
