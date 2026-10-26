package tr.velzon.aiogretmen.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import tr.velzon.aiogretmen.config.VarsayilanGuvenlikYapilandirma;
import tr.velzon.aiogretmen.config.YerelKullaniciBaglami;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tr.velzon.aiogretmen.service.IlerlemeService;
import tr.velzon.aiogretmen.service.MufredatService;
import tr.velzon.aiogretmen.types.BulunamadiException;
import tr.velzon.aiogretmen.types.HaftaOzeti;
import tr.velzon.aiogretmen.types.QuizSonucKaydi;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({VarsayilanGuvenlikYapilandirma.class, YerelKullaniciBaglami.class}) // KullaniciBaglami = "yerel"
@WebMvcTest(ProgramController.class)
class ProgramControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    MufredatService mufredatService;

    @MockitoBean
    IlerlemeService ilerlemeService;

    @MockitoBean
    tr.velzon.aiogretmen.service.KaynakService kaynakService;

    @MockitoBean
    tr.velzon.aiogretmen.service.SifirlamaService sifirlamaService;

    @Test
    void programListesiDoner() throws Exception {
        when(mufredatService.programOzeti("yerel")).thenReturn(List.of(
                new HaftaOzeti(1, "Harness", "hedef", 15, 3, 20, null)));

        mvc.perform(get("/api/program"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].no").value(1))
                .andExpect(jsonPath("$[0].ilerlemeYuzde").value(20));
    }

    @Test
    void olmayanHaftaHataSozlesmesiyleDoner() throws Exception {
        when(mufredatService.haftaDetay("yerel", 99)).thenThrow(new BulunamadiException("Hafta bulunamadı: 99"));

        mvc.perform(get("/api/hafta/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.hata").exists())
                .andExpect(jsonPath("$.detay").value("Hafta bulunamadı: 99"));
    }

    @Test
    void gorevDurumuAyarlanirVeYeniDurumDoner() throws Exception {
        when(ilerlemeService.gorevDurumuAyarla("yerel", 1, 2, 0, false)).thenReturn(false);

        mvc.perform(post("/api/gorev/durum")
                        .contentType("application/json")
                        .content("{\"hafta\":1,\"gun\":2,\"gorevIndex\":0,\"tamamlandi\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tamamlandi").value(false));
    }

    @Test
    void gorevDurumundaTamamlandiZorunlu_yoksa400() throws Exception {
        // hedef durum belirtilmezse sessizce varsayılan seçilmez — istemci hatası açıkça döner
        mvc.perform(post("/api/gorev/durum")
                        .contentType("application/json")
                        .content("{\"hafta\":1,\"gun\":2,\"gorevIndex\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.hata").exists())
                .andExpect(jsonPath("$.detay").value(org.hamcrest.Matchers.containsString("tamamlandi")));
        org.mockito.Mockito.verifyNoInteractions(ilerlemeService);
    }

    @Test
    void eskiToggleUcuKaldirildi() throws Exception {
        mvc.perform(post("/api/gorev/toggle")
                        .contentType("application/json")
                        .content("{\"hafta\":1,\"gun\":2,\"gorevIndex\":0}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void quizSonucuKaydeder() throws Exception {
        when(ilerlemeService.statikQuizKaydet(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(new QuizSonucKaydi(7L, "yerel", 1, "STATIK", 80, "{}", Instant.now()));

        mvc.perform(post("/api/quiz/statik-sonuc")
                        .contentType("application/json")
                        .content("{\"hafta\":1,\"puan\":80,\"detay\":\"{}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.puan").value(80));
    }

    @Test
    void bozukGovde400VeSozlesmeliDoner() throws Exception {
        mvc.perform(post("/api/gorev/durum")
                        .contentType("application/json")
                        .content("{bozuk json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.hata").exists());
    }
}
