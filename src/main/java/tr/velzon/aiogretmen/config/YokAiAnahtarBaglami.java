package tr.velzon.aiogretmen.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Yerel/MCP modları: ziyaretçi anahtarı kavramı yok — AI sunucu anahtarıyla çalışır (Faz 10, ADR-0017). */
@Component
@Profile("!site")
public class YokAiAnahtarBaglami implements AiAnahtarBaglami {

    @Override
    public Optional<String> anahtar() {
        return Optional.empty();
    }
}
