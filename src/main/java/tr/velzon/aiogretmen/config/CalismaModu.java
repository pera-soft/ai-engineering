package tr.velzon.aiogretmen.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Uygulamanın çalışma modu (Faz 9b, ADR-0012): tek kişilik yerel mod mu, çok kullanıcılı {@code site}
 * profili mi? Sitede ORTAK (kullanıcıya ayrılmamış) modüller zorla kapanır: kitap notu bağlamı + vektör
 * deposu, mülakat, MCP. Profil kontrolü tek yerde toplanır; servisler {@code Environment}'a dağılmaz.
 */
@Component
public class CalismaModu {

    private final boolean site;

    @Autowired
    public CalismaModu(Environment ortam) {
        this(ortam.matchesProfiles("site"));
    }

    private CalismaModu(boolean site) {
        this.site = site;
    }

    /** Elle kurulan (birim test) örnekler için. */
    public static CalismaModu yerelIcin() {
        return new CalismaModu(false);
    }

    public static CalismaModu siteIcin() {
        return new CalismaModu(true);
    }

    /** Çok kullanıcılı site profili açık mı? */
    public boolean siteMi() {
        return site;
    }
}
