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
        MockHttpServletRequest istek = new MockHttpServletRequest(metod, uri);
        istek.setRequestURI(uri);
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
        MockHttpServletResponse yanit = calistir("/api/gorev/toggle", "POST",
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
    void basliksizIstekGecer_curlVeEskiTarayici() throws Exception {
        // bilinçli fail-open: Sec-Fetch-Site/Origin göndermeyen istemciler (curl, eski tarayıcı)
        // engellenmez — docs/SECURITY.md'de kabul edilen artık risk
        assertThat(calistir("/api/program", "GET", null, null).getStatus()).isEqualTo(200);
    }

    @Test
    void crossSiteAmaApiDisiYolGecer() throws Exception {
        // statik varlıklar (html/css/js) filtre kapsamı dışında — sayfa her yerden açılabilir
        assertThat(calistir("/index.html", "GET", "cross-site", null).getStatus()).isEqualTo(200);
    }
}
