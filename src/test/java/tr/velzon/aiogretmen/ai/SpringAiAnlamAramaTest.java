package tr.velzon.aiogretmen.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import tr.velzon.aiogretmen.types.BenzerNot;
import tr.velzon.aiogretmen.types.NotParcasi;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SpringAiAnlamArama: bilinen vektörlerle sahte EmbeddingModel üzerinden sıralama + JSON
 * round-trip. Canlı OpenRouter çağrısı YOK (ADR-0005). Bu sınıf ai/ paketindedir — Spring AI
 * importları yalnız burada (ArchUnit).
 */
class SpringAiAnlamAramaTest {

    /** Bilinen metin → vektör tablosu; bilinmeyen metin dik (0,0,1) vektör. */
    static final Map<String, float[]> VEKTORLER = new HashMap<>() {{
        put("kedi hakkında not", new float[]{1f, 0f, 0f});
        put("araba hakkında not", new float[]{0f, 1f, 0f});
        put("köpek hakkında not", new float[]{0.9f, 0.1f, 0f}); // kediye yakın
        put("kedi",               new float[]{1f, 0f, 0f});     // sorgu
    }};

    static final List<NotParcasi> PARCALAR = List.of(
            new NotParcasi("id-kedi", "K", "2026-07-09", "kedi hakkında not"),
            new NotParcasi("id-araba", "K", "2026-07-09", "araba hakkında not"),
            new NotParcasi("id-kopek", "K", "2026-07-09", "köpek hakkında not"));

    private SpringAiAnlamArama yeni(File dosya) {
        SimpleVectorStore depo = SimpleVectorStore.builder(new SahteEmbedding(VEKTORLER)).build();
        return new SpringAiAnlamArama(depo, dosya);
    }

    @Test
    void benzerlerBilinenVektorleDoruSiraDoner(@TempDir Path dir) {
        SpringAiAnlamArama arama = yeni(dir.resolve("v.json").toFile());
        arama.indexle(PARCALAR);

        List<BenzerNot> sonuc = arama.benzerleri("kedi", 2);

        assertThat(sonuc).hasSize(2);
        assertThat(sonuc.get(0).metin()).isEqualTo("kedi hakkında not");   // cosine = 1.0
        assertThat(sonuc.get(1).metin()).isEqualTo("köpek hakkında not");  // kediye yakın
        assertThat(sonuc.get(0).skor()).isGreaterThanOrEqualTo(sonuc.get(1).skor()); // azalan sıra
        assertThat(arama.sayac()).isEqualTo(3);
    }

    @Test
    void jsonSaveLoadRoundTrip(@TempDir Path dir) {
        File dosya = dir.resolve("vektor.json").toFile();

        SpringAiAnlamArama yazan = yeni(dosya);
        yazan.indexle(PARCALAR);
        assertThat(dosya).exists();

        // Yeni instance aynı dosyayı açılışta load eder (yeniden embed etmeden)
        SpringAiAnlamArama okuyan = yeni(dosya);
        assertThat(okuyan.sayac()).isEqualTo(3);

        List<BenzerNot> sonuc = okuyan.benzerleri("kedi", 1);
        assertThat(sonuc).hasSize(1);
        assertThat(sonuc.get(0).metin()).isEqualTo("kedi hakkında not");
    }

    @Test
    void bosSorguVeBosDepoBosDoner(@TempDir Path dir) {
        SpringAiAnlamArama arama = yeni(dir.resolve("v.json").toFile());
        assertThat(arama.benzerleri("kedi", 3)).isEmpty();  // depo boş
        arama.indexle(PARCALAR);
        assertThat(arama.benzerleri("  ", 3)).isEmpty();    // boş sorgu
    }

    @Test
    void temizleDepoyuVeDosyayiSiler(@TempDir Path dir) {
        File dosya = dir.resolve("v.json").toFile();
        SpringAiAnlamArama arama = yeni(dosya);
        arama.indexle(PARCALAR);
        assertThat(dosya).exists();

        arama.temizle();

        assertThat(arama.sayac()).isEqualTo(0);
        assertThat(dosya).doesNotExist();
    }

    /** Bilinen vektörler döndüren sahte embedding — canlı çağrı yok. */
    static class SahteEmbedding implements EmbeddingModel {
        private final Map<String, float[]> tablo;

        SahteEmbedding(Map<String, float[]> tablo) {
            this.tablo = tablo;
        }

        @Override
        public float[] embed(String metin) {
            return tablo.getOrDefault(metin, new float[]{0f, 0f, 1f});
        }

        @Override
        public float[] embed(Document belge) {
            return embed(belge.getText());
        }

        @Override
        public int dimensions() {
            return 3;
        }

        @Override
        public EmbeddingResponse call(EmbeddingRequest istek) {
            throw new UnsupportedOperationException("test sahtesi call() kullanmaz");
        }
    }
}
