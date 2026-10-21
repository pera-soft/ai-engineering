package tr.velzon.aiogretmen.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import tr.velzon.aiogretmen.config.VarsayilanGuvenlikYapilandirma;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tr.velzon.aiogretmen.service.KitapService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(VarsayilanGuvenlikYapilandirma.class)
@WebMvcTest(KitapController.class)
class KitapControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    KitapService kitapService;

    @Test
    void notEkleIslenmisSonucDoner() throws Exception {
        when(kitapService.notEkle(anyString(), anyString())).thenReturn(
                new KitapService.IslenmisNot("AI Engineering", "- özet", List.of(7), List.of("RAG"), null));

        mvc.perform(post("/api/kitap-notu")
                        .contentType("application/json")
                        .content("{\"kitap\":\"AI Engineering\",\"not\":\"ham not\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etiketler[0]").value(7));
    }

    @Test
    void bosKitapAdi400() throws Exception {
        mvc.perform(post("/api/kitap-notu")
                        .contentType("application/json")
                        .content("{\"kitap\":\" \",\"not\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.hata").value("Geçersiz istek"));
    }

    @Test
    void goruntuYukleVisionHattiniCagirir() throws Exception {
        when(kitapService.goruntudenEkle(anyString(), any(), anyString())).thenReturn(
                new KitapService.IslenmisNot("K", "- özet", List.of(1), List.of(), "çıkan metin"));

        MockMultipartFile dosya = new MockMultipartFile(
                "goruntu", "sayfa.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mvc.perform(multipart("/api/kitap-notu/goruntu").file(dosya).param("kitap", "K"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cikanMetin").value("çıkan metin"));

        verify(kitapService).goruntudenEkle(eq("K"), any(), eq("image/jpeg"));
    }

    @Test
    void desteklenmeyenMime400() throws Exception {
        MockMultipartFile dosya = new MockMultipartFile(
                "goruntu", "belge.pdf", "application/pdf", new byte[]{1});

        mvc.perform(multipart("/api/kitap-notu/goruntu").file(dosya).param("kitap", "K"))
                .andExpect(status().isBadRequest());

        verify(kitapService, never()).goruntudenEkle(anyString(), any(), anyString());
    }
}
