package tr.velzon.aiogretmen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Zamanlanmış görevler YALNIZ {@code site} profilinde açılır (Faz 10, ADR-0017): yerel mod ve mcp/mcp-http
 * profilleri hiçbir arka plan işi çalıştırmaz.
 */
@Configuration
@Profile("site")
@EnableScheduling
public class SiteZamanlama {
}
