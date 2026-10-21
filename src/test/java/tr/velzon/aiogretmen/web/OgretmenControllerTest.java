package tr.velzon.aiogretmen.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import tr.velzon.aiogretmen.config.VarsayilanGuvenlikYapilandirma;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tr.velzon.aiogretmen.service.DegerlendirmeService;
import tr.velzon.aiogretmen.service.KullanimService;
import tr.velzon.aiogretmen.service.SohbetService;
import tr.velzon.aiogretmen.types.AiDevreDisiException;
import tr.velzon.aiogretmen.types.Degerlendirme;
import tr.velzon.aiogretmen.types.SohbetMesaji;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(VarsayilanGuvenlikYapilandirma.class)
@WebMvcTest(OgretmenController.class)
class OgretmenControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    DegerlendirmeService degerlendirmeService;

    @MockitoBean
    SohbetService sohbetService;

    @MockitoBean
    KullanimService kullanimService;

    @MockitoBean
    tr.velzon.aiogretmen.service.QuizUretimService quizUretimService;

    @MockitoBean
    tr.velzon.aiogretmen.service.RaporService raporService;

    @MockitoBean
    tr.velzon.aiogretmen.service.OrnekService ornekService;

    @Test
    void ornekTaslakDoner() throws Exception {
        when(ornekService.ornekUret(anyInt(), any(), anyString())).thenReturn("💡 Örnek: 1. ...");

        mvc.perform(post("/api/ornek")
                        .contentType("application/json")
                        .content("{\"hafta\":1,\"gun\":1,\"gorevMetni\":\"5 maddede yaz\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ornek").value("💡 Örnek: 1. ..."));
    }

    @Test
    void aciklamaGoreviAcar() throws Exception {
        when(ornekService.aciklaUret(anyInt(), any(), anyString())).thenReturn("Bu görevde şunu yapman bekleniyor…");

        mvc.perform(post("/api/aciklama")
                        .contentType("application/json")
                        .content("{\"hafta\":1,\"gun\":1,\"gorevMetni\":\"5 maddede yaz\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aciklama").value("Bu görevde şunu yapman bekleniyor…"));
    }

    @Test
    void degerlendirYapilandirilmisSonucDoner() throws Exception {
        var d = new Degerlendirme(72, "İyi.", List.of("a"), List.of("b"), List.of(), "adım", null);
        when(degerlendirmeService.degerlendir(anyInt(), any(), anyString(), anyString(), any()))
                .thenReturn(new DegerlendirmeService.Sonuc(5L, d));

        mvc.perform(post("/api/degerlendir")
                        .contentType("application/json")
                        .content("{\"hafta\":1,\"gun\":2,\"tur\":\"OZET\",\"icerik\":\"özetim\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gonderimId").value(5))
                .andExpect(jsonPath("$.degerlendirme.puan").value(72))
                .andExpect(jsonPath("$.degerlendirme.sonraki_adim").value("adım"));
    }

    @Test
    void aiKapaliyken503VeSozlesmeliHata() throws Exception {
        when(sohbetService.mesajGonder(anyInt(), any(), anyString()))
                .thenThrow(new AiDevreDisiException());

        mvc.perform(post("/api/sohbet")
                        .contentType("application/json")
                        .content("{\"hafta\":1,\"mesaj\":\"selam\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.hata").value("AI özellikleri devre dışı"))
                .andExpect(jsonPath("$.detay").exists());
    }

    @Test
    void sohbetGecmisiDoner() throws Exception {
        when(sohbetService.gecmis(1)).thenReturn(List.of(
                new SohbetMesaji(1L, "yerel", 1, "USER", "soru", Instant.now()),
                new SohbetMesaji(2L, "yerel", 1, "ASSISTANT", "cevap", Instant.now())));

        mvc.perform(get("/api/sohbet/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rol").value("USER"))
                .andExpect(jsonPath("$[1].icerik").value("cevap"));
    }

    @Test
    void quizUretSorulariDoner() throws Exception {
        when(quizUretimService.uret(1, 5)).thenReturn(new tr.velzon.aiogretmen.types.UretilenQuiz(List.of(
                new tr.velzon.aiogretmen.types.UretilenQuiz.Soru("S?", List.of("a", "b", "c", "d"), 1, "çünkü"))));

        mvc.perform(post("/api/quiz/uret")
                        .contentType("application/json")
                        .content("{\"hafta\":1,\"adet\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sorular[0].dogru_index").value(1));
    }

    @Test
    void quizUretilemezse502Sozlesmesi() throws Exception {
        when(quizUretimService.uret(anyInt(), any()))
                .thenThrow(new tr.velzon.aiogretmen.types.AiCiktisiBozukException("Quiz üretilemedi"));

        mvc.perform(post("/api/quiz/uret")
                        .contentType("application/json")
                        .content("{\"hafta\":1}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.hata").exists());
    }

    @Test
    void raporHtmlVeMarkdownDoner() throws Exception {
        when(raporService.rapor(1, false)).thenReturn(
                new tr.velzon.aiogretmen.service.RaporService.Rapor(1, "# R", "<h1>R</h1>", true));

        mvc.perform(get("/api/rapor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cacheden").value(true))
                .andExpect(jsonPath("$.html").value("<h1>R</h1>"));
    }

    @Test
    void kullanimIstatistigiDoner() throws Exception {
        when(kullanimService.istatistik()).thenReturn(Map.of(
                "aiAktif", true, "toplamCagri", 3, "toplamInputToken", 1200, "toplamOutputToken", 400));

        mvc.perform(get("/api/kullanim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiAktif").value(true))
                .andExpect(jsonPath("$.toplamCagri").value(3));
    }
}
