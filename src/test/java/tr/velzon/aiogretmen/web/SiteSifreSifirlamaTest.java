package tr.velzon.aiogretmen.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import tr.velzon.aiogretmen.service.EpostaGonderici;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Faz 9c (ADR-0013): şifremi unuttum / şifre sıfırlama / doğrulama e-postasını yeniden gönder —
 * {@code site} profilinde uçtan uca. Nötr yanıt (hesap varlığı sızmaz), tek kullanımlık ve süreli token,
 * eski oturumların düşmesi, kilidin kalkması, hız sınırı, SMTP hatasında isteğin bozulmaması.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sitesifirlama;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-sifirlama",
        "app.rapor-dir=target/test-raporlar-sifirlama",
        "app.site.temel-url=https://egitim.pera.software",
        "app.site.kayit=eposta"  // "şifremi unuttum" e-posta akışı (ADR-0013)
})
@ActiveProfiles("site")
@AutoConfigureMockMvc
class SiteSifreSifirlamaTest {

    private static final String SIFRE = "cok-gizli-sifre-123";
    private static final String YENI = "yepyeni-sifre-456";
    private static final String NOTR = "{\"durum\":\"istek-alindi\"}";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @MockitoBean
    EpostaGonderici eposta;

    @BeforeEach
    void temiz() {
        clearInvocations(eposta);
    }

    // ---------- yardımcılar ----------

    private ResultActions json(String yol, String govde) throws Exception {
        return mvc.perform(post(yol).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(govde));
    }

    private static String token(String baglanti) {
        return baglanti.substring(baglanti.indexOf("token=") + "token=".length());
    }

    private String kayitBaglantisi(String email) throws Exception {
        json("/api/kimlik/kayit", "{\"email\":\"" + email + "\",\"sifre\":\"" + SIFRE + "\"}")
                .andExpect(status().isOk());
        ArgumentCaptor<String> b = ArgumentCaptor.forClass(String.class);
        verify(eposta).dogrulamaBaglantisiGonder(eq(email), b.capture());
        clearInvocations(eposta);
        return b.getValue();
    }

    private void dogrulanmisHesap(String email) throws Exception {
        mvc.perform(get("/api/kimlik/dogrula").param("token", token(kayitBaglantisi(email))))
                .andExpect(redirectedUrl("/dogrulama-sonuc.html?durum=basarili"));
    }

    private ResultActions giris(String email, String sifre) throws Exception {
        return mvc.perform(post("/api/kimlik/giris").with(csrf()).param("email", email).param("sifre", sifre));
    }

    private MockHttpSession oturum(String email, String sifre) throws Exception {
        MvcResult r = giris(email, sifre).andExpect(redirectedUrl("/")).andReturn();
        return (MockHttpSession) r.getRequest().getSession(false);
    }

    private ResultActions unuttum(String email) throws Exception {
        return json("/api/kimlik/sifremi-unuttum", "{\"email\":\"" + email + "\"}");
    }

    /** Sıfırlama ister ve gönderilen bağlantıyı döndürür. */
    private String sifirlamaBaglantisi(String email) throws Exception {
        unuttum(email).andExpect(status().isOk()).andExpect(content().json(NOTR));
        ArgumentCaptor<String> b = ArgumentCaptor.forClass(String.class);
        verify(eposta).sifreSifirlamaBaglantisiGonder(eq(email), b.capture());
        clearInvocations(eposta);
        return b.getValue();
    }

    private ResultActions sifirla(String token, String sifre) throws Exception {
        return json("/api/kimlik/sifre-sifirla", "{\"token\":\"" + token + "\",\"sifre\":\"" + sifre + "\"}");
    }

    // ---------- şifremi unuttum: nötr yanıt ----------

    @Test
    void bilinmeyenEpostaNotrYanitAlirVeEpostaGitmez() throws Exception {
        unuttum("yok@pera.software").andExpect(status().isOk()).andExpect(content().json(NOTR));
        unuttum("biri@ornek.com").andExpect(status().isOk()).andExpect(content().json(NOTR));
        unuttum("").andExpect(status().isOk()).andExpect(content().json(NOTR));
        verify(eposta, never()).sifreSifirlamaBaglantisiGonder(anyString(), anyString());
    }

    @Test
    void dogrulanmamisVeKapaliHesabaBaglantiGitmez() throws Exception {
        kayitBaglantisi("bekleyen@pera.software");
        unuttum("bekleyen@pera.software").andExpect(status().isOk()).andExpect(content().json(NOTR));

        dogrulanmisHesap("kapali2@pera.software");
        jdbc.update("UPDATE kullanici SET etkin = FALSE WHERE email = 'kapali2@pera.software'");
        unuttum("kapali2@pera.software").andExpect(status().isOk()).andExpect(content().json(NOTR));

        verify(eposta, never()).sifreSifirlamaBaglantisiGonder(anyString(), anyString());
    }

    @Test
    void baglantiTemelUrldenKurulurHostBasligindanDegil() throws Exception {
        dogrulanmisHesap("host@pera.software");
        mvc.perform(post("/api/kimlik/sifremi-unuttum").with(csrf()).header("Host", "kotu.example.com")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"host@pera.software\"}"))
                .andExpect(status().isOk());
        ArgumentCaptor<String> b = ArgumentCaptor.forClass(String.class);
        verify(eposta).sifreSifirlamaBaglantisiGonder(eq("host@pera.software"), b.capture());
        assertThat(b.getValue()).startsWith("https://egitim.pera.software/sifre-sifirla.html?token=");
        // DB'de ham token yok, yalnız özet; 1 saat geçerli
        String ham = token(b.getValue());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM kullanici_token WHERE token_hash = ?",
                Integer.class, ham)).isZero();
        Timestamp son = jdbc.queryForObject("SELECT t.son_gecerlilik FROM kullanici_token t JOIN kullanici k "
                + "ON k.id = t.hesap_id WHERE k.email = 'host@pera.software' AND t.tur = 'SIFRE_SIFIRLAMA'",
                Timestamp.class);
        assertThat(son.toInstant()).isBetween(Instant.now().plus(55, ChronoUnit.MINUTES),
                Instant.now().plus(61, ChronoUnit.MINUTES));
    }

    // ---------- sıfırlama ----------

    @Test
    void sifreSifirlanirKilitKalkarEskiOturumlarDuser() throws Exception {
        String email = "unutkan@pera.software";
        dogrulanmisHesap(email);
        MockHttpSession eski1 = oturum(email, SIFRE);
        MockHttpSession eski2 = oturum(email, SIFRE);
        mvc.perform(get("/api/program").session(eski1)).andExpect(status().isOk());
        // kilitli + sayaçlı hesap
        jdbc.update("UPDATE kullanici SET basarisiz_deneme = 3, kilit_bitis = ? WHERE email = ?",
                Timestamp.from(Instant.now().plus(10, ChronoUnit.MINUTES)), email);

        String baglanti = sifirlamaBaglantisi(email);
        sifirla(token(baglanti), YENI).andExpect(status().isOk())
                .andExpect(jsonPath("$.durum").value("sifre-guncellendi"));

        // eski oturumlar düştü
        mvc.perform(get("/api/program").session(eski1)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/program").session(eski2)).andExpect(status().isUnauthorized());
        // kilit + sayaç temiz
        assertThat(jdbc.queryForObject("SELECT basarisiz_deneme FROM kullanici WHERE email = ?", Integer.class,
                email)).isZero();
        assertThat(jdbc.queryForObject("SELECT kilit_bitis FROM kullanici WHERE email = ?", Timestamp.class,
                email)).isNull();
        // eski şifre geçmez, yeni geçer
        giris(email, SIFRE).andExpect(redirectedUrl("/giris.html?hata=bilgi"));
        MockHttpSession yeni = oturum(email, YENI);
        mvc.perform(get("/api/program").session(yeni)).andExpect(status().isOk());
    }

    @Test
    void tokenTekKullanimlik() throws Exception {
        dogrulanmisHesap("tekkul@pera.software");
        String t = token(sifirlamaBaglantisi("tekkul@pera.software"));
        sifirla(t, YENI).andExpect(status().isOk());
        sifirla(t, "baska-bir-sifre-789").andExpect(status().isBadRequest()).andExpect(jsonPath("$.detay").exists());
        oturum("tekkul@pera.software", YENI);
    }

    @Test
    void suresiDolmusTokenGecersiz() throws Exception {
        dogrulanmisHesap("suresi@pera.software");
        String t = token(sifirlamaBaglantisi("suresi@pera.software"));
        jdbc.update("UPDATE kullanici_token SET son_gecerlilik = ? WHERE tur = 'SIFRE_SIFIRLAMA'",
                Timestamp.from(Instant.now().minus(1, ChronoUnit.MINUTES)));
        sifirla(t, YENI).andExpect(status().isBadRequest());
        oturum("suresi@pera.software", SIFRE); // şifre değişmedi
    }

    @Test
    void yeniTokenEskisiniGecersizKilar() throws Exception {
        dogrulanmisHesap("ikitoken@pera.software");
        String eski = token(sifirlamaBaglantisi("ikitoken@pera.software"));
        String yeni = token(sifirlamaBaglantisi("ikitoken@pera.software"));
        sifirla(eski, YENI).andExpect(status().isBadRequest());
        sifirla(yeni, YENI).andExpect(status().isOk());
    }

    @Test
    void kisaSifreReddedilirTokenHarcanmaz() throws Exception {
        dogrulanmisHesap("kisa2@pera.software");
        String t = token(sifirlamaBaglantisi("kisa2@pera.software"));
        sifirla(t, "123456789").andExpect(status().isBadRequest());
        sifirla(t, YENI).andExpect(status().isOk());
    }

    @Test
    void uydurmaTokenGecersiz() throws Exception {
        sifirla("uydurma", YENI).andExpect(status().isBadRequest());
        sifirla("", YENI).andExpect(status().isBadRequest());
    }

    @Test
    void sayfalarKimliksizAcilir() throws Exception {
        mvc.perform(get("/sifremi-unuttum.html")).andExpect(status().isOk());
        mvc.perform(get("/sifre-sifirla.html")).andExpect(status().isOk());
    }

    @Test
    void csrfsizSifirlamaIstegiReddedilir() throws Exception {
        mvc.perform(post("/api/kimlik/sifremi-unuttum").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"x@pera.software\"}")).andExpect(status().isForbidden());
    }

    // ---------- hız sınırı ----------

    @Test
    void sifirlamaSaatteUcEpostayaSinirli() throws Exception {
        dogrulanmisHesap("sel@pera.software");
        for (int i = 0; i < 5; i++) {
            unuttum("sel@pera.software").andExpect(status().isOk()).andExpect(content().json(NOTR));
        }
        verify(eposta, times(3)).sifreSifirlamaBaglantisiGonder(eq("sel@pera.software"), anyString());
    }

    // ---------- doğrulama e-postasını yeniden gönder ----------

    @Test
    void dogrulamaYenidenGonderilirEskiBaglantiGecersizlesir() throws Exception {
        String eski = kayitBaglantisi("yeniden@pera.software");
        json("/api/kimlik/dogrulama-yeniden", "{\"email\":\"yeniden@pera.software\"}")
                .andExpect(status().isOk()).andExpect(content().json(NOTR));
        ArgumentCaptor<String> b = ArgumentCaptor.forClass(String.class);
        verify(eposta).dogrulamaBaglantisiGonder(eq("yeniden@pera.software"), b.capture());

        mvc.perform(get("/api/kimlik/dogrula").param("token", token(eski)))
                .andExpect(redirectedUrl("/dogrulama-sonuc.html?durum=gecersiz"));
        mvc.perform(get("/api/kimlik/dogrula").param("token", token(b.getValue())))
                .andExpect(redirectedUrl("/dogrulama-sonuc.html?durum=basarili"));
        oturum("yeniden@pera.software", SIFRE);
    }

    @Test
    void dogrulamaYenidenGonderNotrVeSinirli() throws Exception {
        // bilinmeyen ve zaten doğrulanmış adres: nötr, e-posta yok
        dogrulanmisHesap("zatendogru@pera.software");
        for (String e : List.of("bilinmeyen@pera.software", "zatendogru@pera.software", "x@ornek.com")) {
            json("/api/kimlik/dogrulama-yeniden", "{\"email\":\"" + e + "\"}")
                    .andExpect(status().isOk()).andExpect(content().json(NOTR));
        }
        verify(eposta, never()).dogrulamaBaglantisiGonder(anyString(), anyString());

        // kayıt (1) + yeniden gönder: saatte toplam 3
        kayitBaglantisi("yenidensel@pera.software");
        for (int i = 0; i < 4; i++) {
            json("/api/kimlik/dogrulama-yeniden", "{\"email\":\"yenidensel@pera.software\"}")
                    .andExpect(status().isOk()).andExpect(content().json(NOTR));
        }
        verify(eposta, times(2)).dogrulamaBaglantisiGonder(eq("yenidensel@pera.software"), anyString());
    }

    // ---------- SMTP hatası isteği bozmaz ----------

    @Test
    void gonderimHatasiKayitVeSifirlamayiBozmaz() throws Exception {
        doThrow(new IllegalStateException("SMTP kapalı")).when(eposta)
                .dogrulamaBaglantisiGonder(eq("smtphata@pera.software"), anyString());
        json("/api/kimlik/kayit", "{\"email\":\"smtphata@pera.software\",\"sifre\":\"" + SIFRE + "\"}")
                .andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM kullanici WHERE email = 'smtphata@pera.software'",
                Integer.class)).isEqualTo(1);

        dogrulanmisHesap("smtphata2@pera.software");
        doThrow(new IllegalStateException("SMTP kapalı")).when(eposta)
                .sifreSifirlamaBaglantisiGonder(eq("smtphata2@pera.software"), anyString());
        unuttum("smtphata2@pera.software").andExpect(status().isOk()).andExpect(content().json(NOTR));
    }
}
