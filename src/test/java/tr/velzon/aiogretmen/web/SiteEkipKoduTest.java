package tr.velzon.aiogretmen.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ADR-0015 — {@code site} profilinin VARSAYILAN kayıt yöntemi: ortak ekip kodu. E-posta göndericisi
 * sahtelenmez ve SMTP/{@code eposta-zorunlu=false} verilmez → bağlamın açılması "ekip kodu modunda SMTP
 * gerekmez"i de kanıtlar.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:siteekipkodu;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-ekipkodu",
        "app.rapor-dir=target/test-raporlar-ekipkodu",
        "app.site.ekip-kodu=" + SiteEkipKoduTest.KOD
})
@ActiveProfiles("site")
@AutoConfigureMockMvc
class SiteEkipKoduTest {

    static final String KOD = "Ekip-Kodu-Deneme-2026";
    private static final String SIFRE = "cok-gizli-sifre-123";
    private static final String KOD_HATASI = "Ekip kodu hatalı. Kodu site sorumlusundan al.";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private ResultActions kayit(String email, String sifre, String kod) throws Exception {
        String kodAlani = kod == null ? "" : ",\"ekipKodu\":\"" + kod + "\"";
        return mvc.perform(post("/api/kimlik/kayit").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"sifre\":\"" + sifre + "\"" + kodAlani + "}"));
    }

    private ResultActions giris(String email, String sifre) throws Exception {
        return mvc.perform(post("/api/kimlik/giris").with(csrf()).param("email", email).param("sifre", sifre));
    }

    private int hesapSayisi(String email) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM kullanici WHERE email = ?", Integer.class, email);
    }

    @Test
    void kayitYontemiKimliksizOkunur() throws Exception {
        mvc.perform(get("/api/kimlik/ayarlar")).andExpect(status().isOk())
                .andExpect(jsonPath("$.kayit").value("ekip-kodu"));
    }

    @Test
    void dogruKodlaHesapDogrulanmisAcilirVeHemenGirilir() throws Exception {
        kayit("Yeni.Kisi@PERA.software ", SIFRE, KOD).andExpect(status().isOk())
                .andExpect(jsonPath("$.durum").value("kayit-tamam"));
        String email = "yeni.kisi@pera.software";
        assertThat(jdbc.queryForObject("SELECT dogrulandi FROM kullanici WHERE email = ?", Boolean.class, email))
                .isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM kullanici_token t JOIN kullanici k ON k.id = t.hesap_id "
                + "WHERE k.email = ?", Integer.class, email)).isZero(); // token/e-posta yok
        giris(email, SIFRE).andExpect(redirectedUrl("/"));
    }

    @Test
    void koddakiBasSonBosluklarKirpilir() throws Exception {
        kayit("bosluk@pera.software", SIFRE, "  " + KOD + " ").andExpect(status().isOk());
        assertThat(hesapSayisi("bosluk@pera.software")).isEqualTo(1);
    }

    @Test
    void yanlisEksikVeBosKodReddedilirHesapAcilmaz() throws Exception {
        kayit("yanlis@pera.software", SIFRE, KOD + "x").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detay").value(KOD_HATASI));
        kayit("yanlis@pera.software", SIFRE, KOD.toLowerCase()).andExpect(status().isBadRequest());
        kayit("yanlis@pera.software", SIFRE, null).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detay").value(KOD_HATASI));
        kayit("yanlis@pera.software", SIFRE, "").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detay").value(KOD_HATASI));
        assertThat(hesapSayisi("yanlis@pera.software")).isZero();
    }

    @Test
    void yanlisKodBaskaKuralSizdirmaz() throws Exception {
        // kod yanlışken alan adı/şifre kuralı ya da hesap varlığı hakkında bilgi DÖNMEZ — hep aynı mesaj
        kayit("ali@ornek.com", "kisa", "yanlis-kod-000000").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detay").value(KOD_HATASI));
        kayit("var@pera.software", SIFRE, KOD).andExpect(status().isOk());
        kayit("var@pera.software", SIFRE, "yanlis-kod-000000").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detay").value(KOD_HATASI));
    }

    @Test
    void dogruKoddaAlanAdiVeSifreKurallariSurer() throws Exception {
        kayit("ali@ornek.com", SIFRE, KOD).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detay").value(containsString("@pera.software")));
        kayit("ali@pera.software.evil.com", SIFRE, KOD).andExpect(status().isBadRequest());
        kayit("kisa@pera.software", "123456789", KOD).andExpect(status().isBadRequest());
        assertThat(hesapSayisi("kisa@pera.software")).isZero();
    }

    @Test
    void mevcutHesabaYenidenKayitAyniYanitSifreDegismez() throws Exception {
        String email = "mevcut@pera.software";
        kayit(email, SIFRE, KOD).andExpect(status().isOk());
        kayit(email, "baska-bir-sifre-456", KOD).andExpect(status().isOk())
                .andExpect(jsonPath("$.durum").value("kayit-tamam"));
        giris(email, "baska-bir-sifre-456").andExpect(redirectedUrl("/giris.html?hata=bilgi"));
        giris(email, SIFRE).andExpect(redirectedUrl("/"));
        assertThat(hesapSayisi(email)).isEqualTo(1);
    }

    @Test
    void epostaUclariKapali() throws Exception {
        mvc.perform(post("/api/kimlik/sifremi-unuttum").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"biri@pera.software\"}")).andExpect(status().isNotFound());
        mvc.perform(post("/api/kimlik/dogrulama-yeniden").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"biri@pera.software\"}")).andExpect(status().isNotFound());
        mvc.perform(get("/api/kimlik/dogrula").param("token", "herhangi")).andExpect(status().isNotFound());
    }

    @Test
    void isletmenTokeniyleSifreSifirlanir() throws Exception {
        // tools/sifre-baglantisi.sh'nin yaptığının aynısı: SHA-256 hex özet + SIFRE_SIFIRLAMA + 1 saat
        String email = "unutkan@pera.software";
        kayit(email, SIFRE, KOD).andExpect(status().isOk());
        String token = "isletmen-uretti_" + "A".repeat(27);
        String ozet = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8)));
        jdbc.update("INSERT INTO kullanici_token (hesap_id, tur, token_hash, son_gecerlilik) "
                        + "SELECT id, 'SIFRE_SIFIRLAMA', ?, ? FROM kullanici WHERE email = ?",
                ozet, Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)), email);

        String yeni = "yepyeni-sifre-789";
        mvc.perform(post("/api/kimlik/sifre-sifirla").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"sifre\":\"" + yeni + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.durum").value("sifre-guncellendi"));
        giris(email, SIFRE).andExpect(redirectedUrl("/giris.html?hata=bilgi"));
        giris(email, yeni).andExpect(redirectedUrl("/"));

        // tek kullanımlık; hata mesajı "şifremi unuttum"a değil site sorumlusuna yönlendirir
        mvc.perform(post("/api/kimlik/sifre-sifirla").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"sifre\":\"" + yeni + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detay").value(containsString("Site sorumlusundan")));
    }
}
