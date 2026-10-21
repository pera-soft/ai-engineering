package tr.velzon.aiogretmen.web;

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
import tr.velzon.aiogretmen.service.EpostaGonderici;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Faz 9a (ADR-0012) — {@code site} profili kimlik sözleşmesi: kayıt yalnız {@code @pera.software},
 * doğrulanmamış hesap giremez, kaba kuvvet kilidi, CSRF token zorunlu, kimliksiz istek reddedilir ve
 * iki oturum açmış kullanıcı birbirinin ilerlemesini/quiz'ini/sohbetini GÖREMEZ.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sitekimlik;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-site",
        "app.rapor-dir=target/test-raporlar-site"
})
@ActiveProfiles("site")
@AutoConfigureMockMvc
class SiteKimlikTest {

    private static final String SIFRE = "cok-gizli-sifre-123";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @MockitoBean
    EpostaGonderici eposta;

    // ---------- yardımcılar ----------

    private org.springframework.test.web.servlet.ResultActions kayit(String email, String sifre) throws Exception {
        return mvc.perform(post("/api/kimlik/kayit").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"sifre\":\"" + sifre + "\"}"));
    }

    /** Kayıt olur ve doğrulama bağlantısını (sahte e-posta göndericisinden) döndürür. */
    private String kayitVeBaglanti(String email) throws Exception {
        kayit(email, SIFRE).andExpect(status().isOk());
        ArgumentCaptor<String> baglanti = ArgumentCaptor.forClass(String.class);
        verify(eposta).dogrulamaBaglantisiGonder(eq(email.trim().toLowerCase()), baglanti.capture());
        return baglanti.getValue();
    }

    private static String token(String baglanti) {
        return baglanti.substring(baglanti.indexOf("token=") + "token=".length());
    }

    private void dogrula(String baglanti) throws Exception {
        mvc.perform(get("/api/kimlik/dogrula").param("token", token(baglanti)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dogrulama-sonuc.html?durum=basarili"));
    }

    private org.springframework.test.web.servlet.ResultActions giris(String email, String sifre) throws Exception {
        return mvc.perform(post("/api/kimlik/giris").with(csrf())
                .param("email", email).param("sifre", sifre));
    }

    private MockHttpSession oturumAc(String email) throws Exception {
        dogrula(kayitVeBaglanti(email));
        MvcResult r = giris(email, SIFRE).andExpect(redirectedUrl("/")).andReturn();
        return (MockHttpSession) r.getRequest().getSession(false);
    }

    // ---------- kayıt ----------

    @Test
    void peraDisiAlanAdiReddedilir() throws Exception {
        kayit("ali@ornek.com", SIFRE).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.hata").exists());
        kayit("ali@pera.software.evil.com", SIFRE).andExpect(status().isBadRequest());
        kayit("ali@notpera.software", SIFRE).andExpect(status().isBadRequest());
        kayit("@pera.software", SIFRE).andExpect(status().isBadRequest());
        verify(eposta, never()).dogrulamaBaglantisiGonder(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM kullanici WHERE email LIKE 'ali@%'", Integer.class))
                .isZero();
    }

    @Test
    void kisaSifreReddedilir() throws Exception {
        kayit("kisa@pera.software", "123456789").andExpect(status().isBadRequest());
    }

    @Test
    void alanAdiBuyukKucukHarfVeBoslukDuyarsiz() throws Exception {
        String baglanti = kayitVeBaglanti("  Ayse.Yilmaz@PERA.Software ");
        assertThat(baglanti).contains("/api/kimlik/dogrula?token=");
        assertThat(jdbc.queryForObject("SELECT email FROM kullanici WHERE email = 'ayse.yilmaz@pera.software'",
                String.class)).isEqualTo("ayse.yilmaz@pera.software");
        assertThat(jdbc.queryForObject("SELECT sifre_hash FROM kullanici WHERE email = 'ayse.yilmaz@pera.software'",
                String.class)).startsWith("$2").doesNotContain(SIFRE); // BCrypt
    }

    // ---------- doğrulama + giriş ----------

    @Test
    void dogrulanmamisHesapGiremez() throws Exception {
        kayitVeBaglanti("dogrulanmamis@pera.software");
        giris("dogrulanmamis@pera.software", SIFRE)
                .andExpect(redirectedUrl("/giris.html?hata=dogrulanmamis"));
    }

    @Test
    void dogrulanmisHesapGirer() throws Exception {
        dogrula(kayitVeBaglanti("dogru@pera.software"));
        giris("dogru@pera.software", SIFRE).andExpect(redirectedUrl("/"));
    }

    @Test
    void dogrulamaTokeniTekKullanimlik() throws Exception {
        String baglanti = kayitVeBaglanti("tek@pera.software");
        dogrula(baglanti);
        mvc.perform(get("/api/kimlik/dogrula").param("token", token(baglanti)))
                .andExpect(redirectedUrl("/dogrulama-sonuc.html?durum=gecersiz"));
    }

    @Test
    void suresiDolmusTokenGecersiz() throws Exception {
        String baglanti = kayitVeBaglanti("eski@pera.software");
        jdbc.update("UPDATE kullanici_token SET son_gecerlilik = ?",
                Timestamp.from(Instant.now().minus(1, ChronoUnit.MINUTES)));
        mvc.perform(get("/api/kimlik/dogrula").param("token", token(baglanti)))
                .andExpect(redirectedUrl("/dogrulama-sonuc.html?durum=gecersiz"));
        giris("eski@pera.software", SIFRE).andExpect(redirectedUrl("/giris.html?hata=dogrulanmamis"));
    }

    @Test
    void yanlisTokenGecersiz() throws Exception {
        mvc.perform(get("/api/kimlik/dogrula").param("token", "uydurma"))
                .andExpect(redirectedUrl("/dogrulama-sonuc.html?durum=gecersiz"));
    }

    // ---------- kaba kuvvet kilidi ----------

    @Test
    void besHataliDenemedenSonraHesapKilitlenir() throws Exception {
        dogrula(kayitVeBaglanti("kilit@pera.software"));
        for (int i = 0; i < 4; i++) {
            giris("kilit@pera.software", "yanlis-sifre-000").andExpect(redirectedUrl("/giris.html?hata=bilgi"));
        }
        assertThat(jdbc.queryForObject("SELECT basarisiz_deneme FROM kullanici WHERE email='kilit@pera.software'",
                Integer.class)).isEqualTo(4);
        giris("kilit@pera.software", "yanlis-sifre-000").andExpect(redirectedUrl("/giris.html?hata=bilgi"));

        // 5. hatadan sonra DOĞRU şifre de reddedilir (15 dk kilit)
        giris("kilit@pera.software", SIFRE).andExpect(redirectedUrl("/giris.html?hata=kilitli"));

        // kilit süresi dolunca (simüle) doğru şifre geçer ve sayaç sıfırlanır
        jdbc.update("UPDATE kullanici SET kilit_bitis = ? WHERE email='kilit@pera.software'",
                Timestamp.from(Instant.now().minus(1, ChronoUnit.MINUTES)));
        giris("kilit@pera.software", SIFRE).andExpect(redirectedUrl("/"));
        assertThat(jdbc.queryForObject("SELECT basarisiz_deneme FROM kullanici WHERE email='kilit@pera.software'",
                Integer.class)).isZero();
    }

    @Test
    void basariliGirisSayaciSifirlar() throws Exception {
        dogrula(kayitVeBaglanti("sayac@pera.software"));
        giris("sayac@pera.software", "yanlis-sifre-000");
        giris("sayac@pera.software", "yanlis-sifre-000");
        giris("sayac@pera.software", SIFRE).andExpect(redirectedUrl("/"));
        assertThat(jdbc.queryForObject("SELECT basarisiz_deneme FROM kullanici WHERE email='sayac@pera.software'",
                Integer.class)).isZero();
    }

    @Test
    void kapatilmisHesapGiremez() throws Exception {
        dogrula(kayitVeBaglanti("ayrildi@pera.software"));
        jdbc.update("UPDATE kullanici SET etkin = FALSE WHERE email='ayrildi@pera.software'");
        giris("ayrildi@pera.software", SIFRE).andExpect(redirectedUrl("/giris.html?hata=kapali"));
    }

    @Test
    void acikOturumHesapKapatilincaHemenDuser() throws Exception {
        MockHttpSession a = oturumAc("kapanan@pera.software");
        mvc.perform(get("/api/program").session(a)).andExpect(status().isOk());

        jdbc.update("UPDATE kullanici SET etkin = FALSE WHERE email='kapanan@pera.software'");

        mvc.perform(get("/api/program").session(a)).andExpect(status().isUnauthorized());
        assertThat(a.isInvalid()).isTrue();
        // hesap yeniden açılsa bile düşen oturum geri gelmez
        jdbc.update("UPDATE kullanici SET etkin = TRUE WHERE email='kapanan@pera.software'");
        mvc.perform(get("/api/program").session(a)).andExpect(status().isUnauthorized());
    }

    @Test
    void hesapKapatilincaSayfaIstegiGiriseKapaliKoduylaYonlenir() throws Exception {
        MockHttpSession a = oturumAc("kapanan2@pera.software");
        jdbc.update("UPDATE kullanici SET etkin = FALSE WHERE email='kapanan2@pera.software'");
        mvc.perform(get("/").session(a)).andExpect(redirectedUrl("/giris.html?hata=kapali"));
        assertThat(a.isInvalid()).isTrue();
    }

    @Test
    void hesapSilininceAcikOturumDuser() throws Exception {
        MockHttpSession a = oturumAc("silinen@pera.software");
        jdbc.update("DELETE FROM kullanici_token WHERE hesap_id = "
                + "(SELECT id FROM kullanici WHERE email='silinen@pera.software')");
        jdbc.update("DELETE FROM kullanici WHERE email='silinen@pera.software'");
        mvc.perform(get("/api/program").session(a)).andExpect(status().isUnauthorized());
        assertThat(a.isInvalid()).isTrue();
    }

    @Test
    void etkinKullaniciEtkilenmezBaskasininKapatilmasiDahil() throws Exception {
        MockHttpSession a = oturumAc("kalan@pera.software");
        MockHttpSession b = oturumAc("giden@pera.software");
        jdbc.update("UPDATE kullanici SET etkin = FALSE WHERE email='giden@pera.software'");
        mvc.perform(get("/api/program").session(a)).andExpect(status().isOk());
        mvc.perform(get("/api/program").session(a)).andExpect(status().isOk());
        assertThat(a.isInvalid()).isFalse();
        mvc.perform(get("/api/program").session(b)).andExpect(status().isUnauthorized());
    }

    // ---------- kimliksiz erişim + CSRF ----------

    @Test
    void kimliksizApiIstegi401() throws Exception {
        mvc.perform(get("/api/program")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/sohbet/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void kimliksizAnaSayfaGiriseYonlenir() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/giris.html")));
        mvc.perform(get("/index.html")).andExpect(status().is3xxRedirection());
    }

    @Test
    void girisSayfalariKimliksizAcilir() throws Exception {
        mvc.perform(get("/giris.html")).andExpect(status().isOk());
        mvc.perform(get("/kayit.html")).andExpect(status().isOk());
        mvc.perform(get("/dogrulama-bekleniyor.html")).andExpect(status().isOk());
        mvc.perform(get("/dogrulama-sonuc.html")).andExpect(status().isOk());
        mvc.perform(get("/kimlik.js")).andExpect(status().isOk());
        mvc.perform(get("/style.css")).andExpect(status().isOk());
    }

    @Test
    void csrfTokenSizPostReddedilir() throws Exception {
        MockHttpSession a = oturumAc("csrf@pera.software");
        mvc.perform(post("/api/gorev/toggle").session(a)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hafta\":1,\"gun\":1,\"gorevIndex\":0}"))
                .andExpect(status().isForbidden());
        // token'sız login de reddedilir (login CSRF)
        mvc.perform(post("/api/kimlik/giris").param("email", "csrf@pera.software").param("sifre", SIFRE))
                .andExpect(status().isForbidden());
    }

    // Çerez → X-XSRF-TOKEN başlığı akışı GERÇEK Tomcat'te: SiteCerezTest (MockMvc'nin csrf() işlemcisi
    // paylaşılan filtredeki token deposunu kalıcı olarak test deposuyla değiştirir).

    // ---------- özellikler + çıkış ----------

    @Test
    void ozelliklerSiteModunuVeKullaniciyiSoyler() throws Exception {
        MockHttpSession a = oturumAc("ozellik@pera.software");
        mvc.perform(get("/api/ozellikler").session(a))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.site").value(true))
                .andExpect(jsonPath("$.kullanici").value("ozellik@pera.software"))
                .andExpect(jsonPath("$.mulakat").value(false));
    }

    @Test
    void cikisOturumuKapatir() throws Exception {
        MockHttpSession a = oturumAc("cikis@pera.software");
        mvc.perform(post("/api/kimlik/cikis").session(a).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/program").session(a)).andExpect(status().isUnauthorized());
    }

    // ---------- izolasyon (başarısızlık ölçütü: A, B'nin verisini görmemeli) ----------

    @Test
    void kullaniciAKullaniciBninVerisiniGoremez() throws Exception {
        MockHttpSession a = oturumAc("a@pera.software");
        MockHttpSession b = oturumAc("b@pera.software");

        // A: görev işaretler, quiz kaydeder, sohbet geçmişi olur
        mvc.perform(post("/api/gorev/toggle").session(a).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hafta\":1,\"gun\":1,\"gorevIndex\":0}"))
                .andExpect(jsonPath("$.tamamlandi").value(true));
        mvc.perform(post("/api/quiz/statik-sonuc").session(a).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hafta\":1,\"puan\":80,\"detay\":\"{}\"}"))
                .andExpect(status().isOk());
        // sohbet AI ister (anahtarsız test) → doğrudan A'nın kovasına yazılır
        jdbc.update("INSERT INTO sohbet_mesaj (kullanici_id, hafta, rol, icerik) VALUES (?, 1, 'USER', 'A sırrı')",
                "a@pera.software");

        // A kendi verisini görür
        mvc.perform(get("/api/hafta/1").session(a))
                .andExpect(jsonPath("$.gunler[0].gorevler[0].tamamlandi").value(true))
                .andExpect(jsonPath("$.sonQuizPuan").value(80));
        mvc.perform(get("/api/sohbet/1").session(a)).andExpect(jsonPath("$.length()").value(1));

        // B göremez
        mvc.perform(get("/api/hafta/1").session(b))
                .andExpect(jsonPath("$.gunler[0].gorevler[0].tamamlandi").value(false))
                .andExpect(jsonPath("$.sonQuizPuan").doesNotExist());
        mvc.perform(get("/api/program").session(b))
                .andExpect(jsonPath("$[?(@.no == 1)].tamamlananGorev").value(0));
        mvc.perform(get("/api/sohbet/1").session(b)).andExpect(jsonPath("$.length()").value(0));

        // B aynı görevi işaretleyince kendi kaydı oluşur; A'nınki değişmez
        mvc.perform(post("/api/gorev/toggle").session(b).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hafta\":1,\"gun\":1,\"gorevIndex\":0}"))
                .andExpect(jsonPath("$.tamamlandi").value(true));
        mvc.perform(get("/api/hafta/1").session(a))
                .andExpect(jsonPath("$.gunler[0].gorevler[0].tamamlandi").value(true));

        // kimlik e-postadır (ADR-0012): DB kovası doğrudan e-posta
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM gorev_ilerleme WHERE kullanici_id IN ('a@pera.software','b@pera.software')",
                Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gorev_ilerleme WHERE kullanici_id = 'yerel'",
                Integer.class)).isZero();
    }
}
