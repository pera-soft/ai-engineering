package tr.velzon.aiogretmen.ai;

import tr.velzon.aiogretmen.config.AppProperties;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * prompts/*.st şablonlarını okur; "# " ile başlayan sürüm-yorum satırlarını atar,
 * {yer_tutucu} değerlerini doldurur. Prompt da koddur — şablonlar git'te yaşar.
 */
@org.springframework.stereotype.Component
public class PromptFactory {

    private final Path promptDir;
    private final Map<String, String> onbellek = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Autowired
    public PromptFactory(AppProperties props) {
        this(Path.of(props.promptDir()));
    }

    public PromptFactory(Path promptDir) {
        this.promptDir = promptDir;
    }

    public String doldur(String sablonAdi, Map<String, String> degerler) {
        String sablon = onbellek.computeIfAbsent(sablonAdi, this::yukle);
        for (Map.Entry<String, String> e : degerler.entrySet()) {
            sablon = sablon.replace("{" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        return sablon;
    }

    private String yukle(String ad) {
        Path dosya = promptDir.resolve(ad + ".st");
        if (!Files.isRegularFile(dosya)) {
            throw new IllegalArgumentException("Prompt şablonu bulunamadı: " + ad + " (" + dosya.toAbsolutePath() + ")");
        }
        try {
            return Files.readAllLines(dosya).stream()
                    .filter(satir -> !satir.startsWith("# ")) // sürüm yorumları; "## " markdown başlığı kalır
                    .reduce(new StringBuilder(), (sb, s) -> sb.append(s).append('\n'), StringBuilder::append)
                    .toString()
                    .strip();
        } catch (IOException e) {
            throw new UncheckedIOException("Prompt şablonu okunamadı: " + dosya, e);
        }
    }
}
