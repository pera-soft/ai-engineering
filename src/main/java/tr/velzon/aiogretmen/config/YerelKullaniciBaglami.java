package tr.velzon.aiogretmen.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Auth'suz profillerde ({@code !mcp-http}: web 127.0.0.1:8090 ve stdio MCP) kullanılan kimlik:
 * her zaman {@link #YEREL}. Böylece mevcut TEK-KULLANICI verisi ({@code kullanici_id='yerel'})
 * kesintisiz görünür (Faz 2a-3, ADR-0008).
 */
@Component
@Profile("!mcp-http")
public class YerelKullaniciBaglami implements KullaniciBaglami {

    @Override
    public String aktifKullanici() {
        return YEREL;
    }
}
