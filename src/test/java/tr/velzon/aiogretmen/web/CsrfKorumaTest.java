package tr.velzon.aiogretmen.web;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/** Güvenlik-kritik filtre — inceleme kuralı gereği davranış testleri zorunlu. */
class CsrfKorumaTest {

    final CsrfKoruma filtre = new CsrfKoruma();

    private MockHttpServletResponse calistir(String uri, String metod,
                                             String fetchSite, String origin) throws ServletException, IOException {
        return calistir(uri, metod, fetchSite, origin, 8090);
    }

    private MockHttpServletResponse calistir(String uri, String metod, String fetchSite,
                                             String origin, int sunucuPortu) throws ServletException, IOException {
        MockHttpServletRequest istek = new MockHttpServletRequest(metod, uri);
        istek.setRequestURI(uri);
        istek.setLocalPort(sunucuPortu);
        if (fetchSite != null) istek.addHeader("Sec-Fetch-Site", fetchSite);
        if (origin != null) istek.addHeader("Origin", origin);
        MockHttpServletResponse yanit = new MockHttpServletResponse();
        filtre.doFilter(istek, yanit, new MockFilterChain());
        return yanit;
    }

    @Test
    void crossSiteApiIstegi403() throws Exception {
        MockHttpServletResponse yanit = calistir("/api/kitap-notu", "POST", "cross-site", null);

        assertThat(yanit.getStatus()).isEqualTo(403);
        assertThat(yanit.getContentAsString()).contains("hata").contains("detay");
    }

    @Test
    void yabanciOriginliApiIstegi403() throws Exception {
        MockHttpServletResponse yanit = calistir("/api/gorev/durum", "POST",
                null, "https://kotu-site.example");

        assertThat(yanit.getStatus()).isEqualTo(403);
    }

    @Test
    void sameOriginIstekGecer() throws Exception {
        assertThat(calistir("/api/program", "GET", "same-origin", null).getStatus()).isEqualTo(200);
        assertThat(calistir("/api/sohbet", "POST", "same-origin", "http://127.0.0.1:8090").getStatus()).isEqualTo(200);
        assertThat(calistir("/api/sohbet", "POST", "same-origin", "http://localhost:8090").getStatus()).isEqualTo(200);
    }

    @Test
    void farkliPorttaCalisanSunucudaKendiOriginiGecer() throws Exception {
        // port 8090'a sabit değil: sunucu hangi porttaysa (server.port) o porttaki yerel origin geçer
        assertThat(calistir("/api/sohbet", "POST", "same-origin", "http://127.0.0.1:9000", 9000).getStatus()).isEqualTo(200);
        assertThat(calistir("/api/sohbet", "POST", "same-origin", "http://localhost:9000", 9000).getStatus()).isEqualTo(200);
    }

    @Test
    void yereldekiBaskaPorttanGelenOrigin403() throws Exception {
        // aynı makinedeki başka bir yerel uygulama (farklı port) da yabancı origin sayılır
        assertThat(calistir("/api/sohbet", "POST", null, "http://127.0.0.1:3000", 8090).getStatus()).isEqualTo(403);
        assertThat(calistir("/api/sohbet", "POST", null, "http://127.0.0.1:8090", 9000).getStatus()).isEqualTo(403);
    }

    @Test
    void basliksizIstekGecer_curlVeEskiTarayici() throws Exception {
        // bilinçli fail-open: Sec-Fetch-Site/Origin göndermeyen istemciler (curl, eski tarayıcı)
        // engellenmez — docs/SECURITY.md'de kabul edilen artık risk
        assertThat(calistir("/api/program", "GET", null, null).getStatus()).isEqualTo(200);
    }

    @Test
    void yapilandirilmisPublicOriginGecer_nginxArkasi() throws Exception {
        // site profili (ADR-0012): nginx/TLS arkasında tarayıcı origin'i https://<alan-adı> olur
        CsrfKoruma siteFiltresi = new CsrfKoruma(java.util.List.of("https://egitim.pera.software", " https://diger.pera.software/ "));
        assertThat(calistir(siteFiltresi, "https://egitim.pera.software", 8090).getStatus()).isEqualTo(200);
        assertThat(calistir(siteFiltresi, "https://diger.pera.software", 8090).getStatus()).isEqualTo(200); // trim + sondaki /
        // yerel origin davranışı korunur
        assertThat(calistir(siteFiltresi, "http://127.0.0.1:8090", 8090).getStatus()).isEqualTo(200);
    }

    @Test
    void listedeOlmayanOriginYineReddedilir() throws Exception {
        CsrfKoruma siteFiltresi = new CsrfKoruma(java.util.List.of("https://egitim.pera.software"));
        assertThat(calistir(siteFiltresi, "https://kotu-site.example", 8090).getStatus()).isEqualTo(403);
        assertThat(calistir(siteFiltresi, "http://egitim.pera.software", 8090).getStatus()).isEqualTo(403); // şema farklı
        assertThat(calistir(siteFiltresi, "https://egitim.pera.software.kotu.example", 8090).getStatus()).isEqualTo(403);
    }

    @Test
    void varsayilanFiltredeListeBos_publicOriginReddedilir() throws Exception {
        // yerel modda (liste boş) hiçbir public origin kabul edilmez — mevcut davranış
        assertThat(calistir(filtre, "https://egitim.pera.software", 8090).getStatus()).isEqualTo(403);
    }

    private static MockHttpServletResponse calistir(CsrfKoruma f, String origin, int port)
            throws ServletException, IOException {
        MockHttpServletRequest istek = new MockHttpServletRequest("POST", "/api/sohbet");
        istek.setLocalPort(port);
        istek.addHeader("Sec-Fetch-Site", "same-origin");
        istek.addHeader("Origin", origin);
        MockHttpServletResponse yanit = new MockHttpServletResponse();
        f.doFilter(istek, yanit, new MockFilterChain());
        return yanit;
    }

    @Test
    void crossSiteAmaApiDisiYolGecer() throws Exception {
        // statik varlıklar (html/css/js) filtre kapsamı dışında — sayfa her yerden açılabilir
        assertThat(calistir("/index.html", "GET", "cross-site", null).getStatus()).isEqualTo(200);
    }
}
