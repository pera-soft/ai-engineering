package tr.velzon.aiogretmen.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String contentDir, String promptDir, String hafizaDir, String raporDir,
                            String vektorDosyasi, Ai ai, @DefaultValue Mulakat mulakat) {

    public record Ai(Model model) {
    }

    /** guclu/hizli: sohbet modelleri; embedding: anlam-eşleşmeli bağlam için (ADR-0005). */
    public record Model(String guclu, String hizli, String embedding) {
    }

    /**
     * Mülakat modülü anahtarı (ADR-0011). Varsayılan KAPALI ({@code false}): bean'ler
     * ({@link MulakatEtkin}) kurulmaz, arayüz bölümü gizlenir. Açmak: {@code app.mulakat.etkin=true}.
     */
    public record Mulakat(@DefaultValue("false") boolean etkin) {
    }

    /** Elle kurulan (test) örneklerde {@code mulakat} null olabilir → kapalı sayılır. */
    public boolean mulakatEtkin() {
        return mulakat != null && mulakat.etkin();
    }
}
