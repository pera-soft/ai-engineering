package tr.velzon.aiogretmen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiYanit;
import tr.velzon.aiogretmen.service.BaglamServisi;
import tr.velzon.aiogretmen.service.EpostaGonderici;
import tr.velzon.aiogretmen.service.KisiselDizin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Faz 9b — {@code site} profilinde KİŞİSEL DOSYA VERİSİ + kullanım izolasyonu.
 * Başarısızlık ölçütü (plan): A'nın hafızası/raporu/kullanımı B'ye GÖRÜNMEMELİ; sıfırlama
 * yalnız sahibini etkilemeli; ortak kitap/vektör deposu sitede kapalı olmalı.
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

    private static final String SIFRE = "cok-gizli-sifre-123";
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

    @MockitoBean
    EpostaGonderici eposta;

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

    private MockHttpSession oturumAc(String email) throws Exception {
        mvc.perform(post("/api/kimlik/kayit").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"sifre\":\"" + SIFRE + "\"}")).andExpect(status().isOk());
        ArgumentCaptor<String> baglanti = ArgumentCaptor.forClass(String.class);
        verify(eposta, atLeastOnce()).dogrulamaBaglantisiGonder(eq(email), baglanti.capture());
        String b = baglanti.getValue();
        mvc.perform(get("/api/kimlik/dogrula").param("token", b.substring(b.indexOf("token=") + 6)))
                .andExpect(redirectedUrl("/dogrulama-sonuc.html?durum=basarili"));
        return girisYap(email);
    }

    private MockHttpSession girisYap(String email) throws Exception {
        MvcResult r = mvc.perform(post("/api/kimlik/giris").with(csrf()).param("email", email).param("sifre", SIFRE))
                .andExpect(redirectedUrl("/")).andReturn();
        return (MockHttpSession) r.getRequest().getSession(false);
    }

    private List<AiIstek> yakalananIstekler() {
        ArgumentCaptor<AiIstek> c = ArgumentCaptor.forClass(AiIstek.class);
        verify(ai, atLeastOnce()).cagir(c.capture());
        return c.getAllValues();
    }

    // ---------- hafıza ----------

    @Test
    void aninHafizasiBninPromptunaGirmez_http() throws Exception {
        MockHttpSession a = oturumAc("hafiza-a@pera.software");
        MockHttpSession b = oturumAc("hafiza-b@pera.software");
        yaz(hafiza("hafiza-a@pera.software").resolve("OGRENCI.md"), "A_GIZLI_PROFIL");
        yaz(hafiza("hafiza-a@pera.software").resolve("hafta-01/notlar.md"), "A_GIZLI_NOT");
        yaz(hafiza("hafiza-b@pera.software").resolve("OGRENCI.md"), "B_PROFIL");
        yaz(kok.resolve("hafiza/OGRENCI.md"), "YEREL_GIZLI"); // eski tek-kullanıcı kökü de sızmamalı

        clearInvocations(ai);
        mvc.perform(post("/api/sohbet").session(b).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"hafta\":1,\"mesaj\":\"merhaba\"}")).andExpect(status().isOk());
        List<AiIstek> bIstekleri = yakalananIstekler();
        assertThat(bIstekleri).anySatisfy(i -> assertThat(i.sistemPrompt()).contains("B_PROFIL"));
        assertThat(bIstekleri).allSatisfy(i -> assertThat(i.sistemPrompt() + i.kullaniciMesaji())
                .doesNotContain("A_GIZLI").doesNotContain("YEREL_GIZLI"));

        clearInvocations(ai);
        mvc.perform(post("/api/sohbet").session(a).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"hafta\":1,\"mesaj\":\"selam\"}")).andExpect(status().isOk());
        assertThat(yakalananIstekler()).anySatisfy(i -> assertThat(i.sistemPrompt())
                .contains("A_GIZLI_PROFIL").contains("A_GIZLI_NOT").doesNotContain("B_PROFIL"));
    }

    @Test
    void aninHafizasiBninBaglamindaYok_servisKatmani() throws Exception {
        yaz(hafiza("servis-a@pera.software").resolve("OGRENCI.md"), "SERVIS_A_SIRRI");
        try {
            SecurityContextHolder.getContext().setAuthentication(
                    UsernamePasswordAuthenticationToken.authenticated("servis-b@pera.software", null, List.of()));
            assertThat(baglamServisi.sistemBaglami(1, 1)).doesNotContain("SERVIS_A_SIRRI");
            SecurityContextHolder.getContext().setAuthentication(
                    UsernamePasswordAuthenticationToken.authenticated("servis-a@pera.software", null, List.of()));
            assertThat(baglamServisi.sistemBaglami(1, 1)).contains("SERVIS_A_SIRRI");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ---------- rapor ----------

    @Test
    void raporOnbellegiKullaniciyaOzel() throws Exception {
        MockHttpSession a = oturumAc("rapor-a@pera.software");
        MockHttpSession b = oturumAc("rapor-b@pera.software");

        when(ai.cagir(any())).thenReturn(new AiYanit("# A RAPORU", 1, 1, "m"));
        mvc.perform(get("/api/rapor/1").session(a)).andExpect(jsonPath("$.cacheden").value(false));

        when(ai.cagir(any())).thenReturn(new AiYanit("# B RAPORU", 1, 1, "m"));
        mvc.perform(get("/api/rapor/1").session(b))
                .andExpect(jsonPath("$.cacheden").value(false))
                .andExpect(jsonPath("$.markdown").value("# B RAPORU"));
        mvc.perform(get("/api/rapor/1").session(a))
                .andExpect(jsonPath("$.cacheden").value(true))
                .andExpect(jsonPath("$.markdown").value("# A RAPORU"));
        assertThat(kok.resolve("raporlar/hafta-01.md")).doesNotExist();
    }

    // ---------- kullanım ----------

    @Test
    void kullanimYalnizKendiCagrilariniGosterir() throws Exception {
        MockHttpSession b = oturumAc("kullanim-b@pera.software");
        jdbc.update("INSERT INTO ai_kullanim (kullanici_id, islem, model, input_tokens, output_tokens) "
                + "VALUES ('kullanim-a@pera.software','SOHBET','m',1000,1000)");
        jdbc.update("INSERT INTO ai_kullanim (kullanici_id, islem, model, input_tokens, output_tokens) "
                + "VALUES ('kullanim-b@pera.software','SOHBET','m',7,3)");

        mvc.perform(get("/api/kullanim").session(b))
                .andExpect(jsonPath("$.toplamCagri").value(1))
                .andExpect(jsonPath("$.toplamInputToken").value(7))
                .andExpect(jsonPath("$.toplamOutputToken").value(3));
    }

    // ---------- sıfırlama ----------

    @Test
    void sifirlamaYalnizKendiDosyalariniSiler() throws Exception {
        MockHttpSession b = oturumAc("sifir-b@pera.software");
        yaz(hafiza("sifir-a@pera.software").resolve("OGRENCI.md"), "A");
        yaz(rapor("sifir-a@pera.software").resolve("hafta-01.md"), "A rapor");
        yaz(hafiza("sifir-b@pera.software").resolve("OGRENCI.md"), "B");
        yaz(hafiza("sifir-b@pera.software").resolve("hafta-02/notlar.md"), "B not");
        yaz(rapor("sifir-b@pera.software").resolve("hafta-01.md"), "B rapor");

        mvc.perform(post("/api/sifirla").session(b).with(csrf())).andExpect(status().isOk());

        assertThat(hafiza("sifir-b@pera.software")).doesNotExist();
        assertThat(rapor("sifir-b@pera.software")).doesNotExist();
        assertThat(hafiza("sifir-a@pera.software").resolve("OGRENCI.md")).hasContent("A");
        assertThat(rapor("sifir-a@pera.software").resolve("hafta-01.md")).hasContent("A rapor");
    }

    // ---------- ortak kitap/vektör deposu sitede kapalı ----------

    @Test
    void kitapUclariSitede404() throws Exception {
        MockHttpSession a = oturumAc("kitap-a@pera.software");
        mvc.perform(get("/api/kitaplar").session(a)).andExpect(status().isNotFound());
        mvc.perform(post("/api/kitap-notu").session(a).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"kitap\":\"K\",\"not\":\"n\"}")).andExpect(status().isNotFound());
        mvc.perform(post("/api/kitap-notu/goruntu").session(a).with(csrf())).andExpect(status().isNotFound());
    }
}
