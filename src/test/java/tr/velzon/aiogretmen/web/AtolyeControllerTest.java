package tr.velzon.aiogretmen.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import tr.velzon.aiogretmen.config.VarsayilanGuvenlikYapilandirma;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tr.velzon.aiogretmen.service.AtolyeService;
import tr.velzon.aiogretmen.types.AtolyeAlistirma;
import tr.velzon.aiogretmen.types.AtolyeDegerlendirme;
import tr.velzon.aiogretmen.types.AtolyeKategori;
import tr.velzon.aiogretmen.types.AiDevreDisiException;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(VarsayilanGuvenlikYapilandirma.class)
@WebMvcTest(AtolyeController.class)
class AtolyeControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AtolyeService atolyeService;

    @Test
    void kategorileriDoner() throws Exception {
        when(atolyeService.kategoriler()).thenReturn(List.of(
                new AtolyeKategori("temel-yapi", "Temel Yapı", "İskelet",
                        List.of(new AtolyeAlistirma("net-gorev", "Net görev", "Görev metni",
                                "kötü", "ipucu", "ideal", "neden", false)))));

        mvc.perform(get("/api/atolye"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("temel-yapi"))
                .andExpect(jsonPath("$[0].alistirmalar[0].id").value("net-gorev"))
                .andExpect(jsonPath("$[0].alistirmalar[0].aiDegerlendir").value(false));
    }

    @Test
    void degerlendirSonucDoner() throws Exception {
        when(atolyeService.degerlendir(eq("birlestir"), anyString()))
                .thenReturn(new AtolyeDegerlendirme(80, "İyi prompt.", "Format ekle."));

        mvc.perform(post("/api/atolye/degerlendir")
                        .contentType("application/json")
                        .content("{\"alistirmaId\":\"birlestir\",\"ogrenciPrompt\":\"benim prompt'um\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puan").value(80))
                .andExpect(jsonPath("$.geriBildirim").value("İyi prompt."))
                .andExpect(jsonPath("$.iyilestirme").value("Format ekle."));
    }

    @Test
    void bosPromptta400() throws Exception {
        mvc.perform(post("/api/atolye/degerlendir")
                        .contentType("application/json")
                        .content("{\"alistirmaId\":\"birlestir\",\"ogrenciPrompt\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.hata").value("Geçersiz istek"));
    }

    @Test
    void anahtarYoksa503VeSozlesmeliHata() throws Exception {
        when(atolyeService.degerlendir(anyString(), anyString()))
                .thenThrow(new AiDevreDisiException());

        mvc.perform(post("/api/atolye/degerlendir")
                        .contentType("application/json")
                        .content("{\"alistirmaId\":\"birlestir\",\"ogrenciPrompt\":\"prompt\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.hata").value("AI özellikleri devre dışı"))
                .andExpect(jsonPath("$.detay").exists());
    }
}
