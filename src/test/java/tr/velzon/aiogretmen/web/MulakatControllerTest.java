package tr.velzon.aiogretmen.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tr.velzon.aiogretmen.config.KullaniciBaglami;
import tr.velzon.aiogretmen.config.VarsayilanGuvenlikYapilandirma;
import tr.velzon.aiogretmen.service.MulakatAciklamaService;
import tr.velzon.aiogretmen.service.MulakatService;
import tr.velzon.aiogretmen.types.MulakatAlanOzeti;
import tr.velzon.aiogretmen.types.MulakatCevapSonucu;
import tr.velzon.aiogretmen.types.MulakatKonuGorunumu;
import tr.velzon.aiogretmen.types.MulakatKonuOzeti;
import tr.velzon.aiogretmen.types.MulakatSik;
import tr.velzon.aiogretmen.types.MulakatSonDeneme;
import tr.velzon.aiogretmen.types.MulakatSoruGorunumu;
import tr.velzon.aiogretmen.types.MulakatZorluk;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(VarsayilanGuvenlikYapilandirma.class)
@WebMvcTest(MulakatController.class)
@TestPropertySource(properties = "app.mulakat.etkin=true") // modül varsayılan kapalı (ADR-0011)
class MulakatControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    MulakatService mulakatService;

    @MockitoBean
    MulakatAciklamaService aciklamaService;

    @MockitoBean
    KullaniciBaglami kullaniciBaglami;

    @Test
    void alanlariDoner() throws Exception {
        when(kullaniciBaglami.aktifKullanici()).thenReturn("yerel");
        when(mulakatService.alanlar("yerel")).thenReturn(List.of(
                new MulakatAlanOzeti("cekirdek-java", "Çekirdek Java", "açıklama",
                        List.of(new MulakatKonuOzeti("esitlik-vs-equals", "== vs equals",
                                MulakatZorluk.TEMEL, 1, 6, 4, 2)))));

        mvc.perform(get("/api/mulakat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("cekirdek-java"))
                .andExpect(jsonPath("$[0].konular[0].slug").value("esitlik-vs-equals"))
                .andExpect(jsonPath("$[0].konular[0].soruSayisi").value(6))
                .andExpect(jsonPath("$[0].konular[0].cevaplananSayisi").value(4))
                .andExpect(jsonPath("$[0].konular[0].dogruSayisi").value(2));
    }

    @Test
    void konuGetirCevapSizinlamaz() throws Exception {
        when(kullaniciBaglami.aktifKullanici()).thenReturn("yerel");
        when(mulakatService.konuGetir(eq("yerel"), eq("cekirdek-java"), eq("esitlik-vs-equals")))
                .thenReturn(new MulakatKonuGorunumu("== vs equals", MulakatZorluk.TEMEL, "<p>ders</p>",
                        List.of(new MulakatSoruGorunumu("eq-1", "Ne yazdırır?",
                                List.of("A", "B", "C", "D"), MulakatZorluk.TEMEL, null))));

        String govde = mvc.perform(get("/api/mulakat/cekirdek-java/esitlik-vs-equals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baslik").value("== vs equals"))
                .andExpect(jsonPath("$.sorular[0].id").value("eq-1"))
                .andExpect(jsonPath("$.sorular[0].secenekler[1]").value("B"))
                .andReturn().getResponse().getContentAsString();

        // CEVAPLANMAMIŞ soruda sızıntı YOK: doğru cevap/açıklama alanı gövdede hiç geçmemeli
        assertThat(govde).doesNotContain("dogruIndex").doesNotContain("aciklama");
    }

    @Test
    void konuGetirCevaplanmisSoruyuSonDenemeIleDoner() throws Exception {
        when(kullaniciBaglami.aktifKullanici()).thenReturn("yerel");
        when(mulakatService.konuGetir(eq("yerel"), eq("cekirdek-java"), eq("esitlik-vs-equals")))
                .thenReturn(new MulakatKonuGorunumu("== vs equals", MulakatZorluk.TEMEL, "<p>ders</p>",
                        List.of(new MulakatSoruGorunumu("eq-1", "Ne yazdırır?",
                                List.of("A", "B", "C", "D"), MulakatZorluk.TEMEL,
                                MulakatSonDeneme.acik(0, false, 1, "B doğru çünkü…",
                                        List.of(new MulakatSik("A metni", "A notu"),
                                                new MulakatSik("B metni", "B notu")))))));

        mvc.perform(get("/api/mulakat/cekirdek-java/esitlik-vs-equals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sorular[0].sonDeneme.secilenIndex").value(0))
                .andExpect(jsonPath("$.sorular[0].sonDeneme.dogru").value(false))
                .andExpect(jsonPath("$.sorular[0].sonDeneme.dogruIndex").value(1))
                .andExpect(jsonPath("$.sorular[0].sonDeneme.aciklama").value("B doğru çünkü…"))
                .andExpect(jsonPath("$.sorular[0].sonDeneme.siklar[1].aiNot").value("B notu"));
    }

    @Test
    void konuGetirAktifKullanicininDenemesiniSorar() throws Exception {
        // İzolasyon: uç, ağacı SABİT "yerel" ile değil, aktif kullanıcı kimliğiyle sorar.
        when(kullaniciBaglami.aktifKullanici()).thenReturn("ali@example.com");
        when(mulakatService.konuGetir(eq("ali@example.com"), anyString(), anyString()))
                .thenReturn(new MulakatKonuGorunumu("k", MulakatZorluk.TEMEL, "<p>d</p>", List.of()));

        mvc.perform(get("/api/mulakat/a/b")).andExpect(status().isOk());
        verify(mulakatService).konuGetir("ali@example.com", "a", "b");
    }

    @Test
    void cevapSonucSikAiNotlariylaDoner() throws Exception {
        when(mulakatService.cevapGonder(anyString(), eq("eq-1"), anyInt()))
                .thenReturn(new MulakatCevapSonucu(true, 1, "B doğru çünkü…",
                        List.of(new MulakatSik("A metni", "A notu"), new MulakatSik("B metni", "B notu"),
                                new MulakatSik("C metni", "C notu"), new MulakatSik("D metni", "D notu"))));
        when(kullaniciBaglami.aktifKullanici()).thenReturn("yerel");

        mvc.perform(post("/api/mulakat/cevap")
                        .contentType("application/json")
                        .content("{\"soruId\":\"eq-1\",\"secilenIndex\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dogru").value(true))
                .andExpect(jsonPath("$.dogruIndex").value(1))
                .andExpect(jsonPath("$.aciklama").value("B doğru çünkü…"))
                .andExpect(jsonPath("$.siklar[1].metin").value("B metni"))
                .andExpect(jsonPath("$.siklar[1].aiNot").value("B notu")); // cevap-sonrası reveal
    }

    @Test
    void bosSoruIdde400() throws Exception {
        mvc.perform(post("/api/mulakat/cevap")
                        .contentType("application/json")
                        .content("{\"soruId\":\"  \",\"secilenIndex\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.hata").value("Geçersiz istek"));
    }

    @Test
    void eksikSecilenIndexte400() throws Exception {
        // Jackson primitive olsaydı eksik alanı sessizce 0 (=A) yapardı; Integer null → 400.
        mvc.perform(post("/api/mulakat/cevap")
                        .contentType("application/json")
                        .content("{\"soruId\":\"eq-1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.hata").value("Geçersiz istek"));
    }

    @Test
    void tekrarCevapSizinlamaz() throws Exception {
        when(kullaniciBaglami.aktifKullanici()).thenReturn("yerel");
        when(mulakatService.hataliSorularTekrar("yerel")).thenReturn(List.of(
                new MulakatSoruGorunumu("eq-1", "Ne yazdırır?",
                        List.of("A", "B", "C", "D"), MulakatZorluk.TEMEL,
                        MulakatSonDeneme.gizli(2, false))));

        String govde = mvc.perform(get("/api/mulakat/tekrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("eq-1"))
                // Kullanıcının KENDİ önceki seçimi döner (hangisini yanlış yaptığı belli olsun)…
                .andExpect(jsonPath("$[0].sonDeneme.secilenIndex").value(2))
                .andReturn().getResponse().getContentAsString();

        // …ama doğru cevap GİZLİ: alan adı gövdede hiç geçmez (NON_NULL) → soru yeniden çözülebilir
        assertThat(govde).doesNotContain("dogruIndex").doesNotContain("aciklama");
    }

    @Test
    void aciklaDuzMetinDoner() throws Exception {
        when(aciklamaService.acikla(eq("eq-1"), anyInt())).thenReturn("Seçtiğin şık yanlış çünkü…");

        mvc.perform(post("/api/mulakat/acikla")
                        .contentType("application/json")
                        .content("{\"soruId\":\"eq-1\",\"secilenIndex\":0}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Seçtiğin şık yanlış")));
    }
}
