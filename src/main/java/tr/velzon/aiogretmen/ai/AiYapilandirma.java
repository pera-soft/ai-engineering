package tr.velzon.aiogretmen.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.repo.KullanimRepo;
import tr.velzon.aiogretmen.types.AiDevreDisiException;
import tr.velzon.aiogretmen.types.BenzerNot;
import tr.velzon.aiogretmen.types.NotParcasi;

import java.io.File;
import java.util.List;
import java.nio.file.Path;

/**
 * ChatModel bean'ini Spring AI auto-config DEĞİL, burası kurar (application.yml:
 * spring.ai.model.chat=none). Sağlayıcı: OpenRouter (OpenAI-uyumlu; ADR-0004).
 * Anahtar boşsa uygulama yine açılır; AI uçları 503 döner.
 */
@Configuration
public class AiYapilandirma {

    private static final Logger log = LoggerFactory.getLogger(AiYapilandirma.class);

    @Bean
    public OgretmenAiClient ogretmenAiClient(
            @Value("${spring.ai.openai.api-key:}") String apiKey,
            @Value("${spring.ai.openai.base-url:https://openrouter.ai/api}") String baseUrl,
            AppProperties props, KullanimRepo kullanimRepo) {

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OPENROUTER_API_KEY tanımlı değil — AI özellikleri devre dışı; müfredat+ilerleme çalışıyor.");
            return new OgretmenAiClient() {
                @Override
                public boolean aktif() {
                    return false;
                }

                @Override
                public AiYanit cagir(AiIstek istek) {
                    throw new AiDevreDisiException();
                }

                @Override
                public AiYanit cagirGoruntulu(Islem islem, ModelTuru tur, String sistem,
                                              String mesaj, byte[] goruntu, String mime, int maxTokens) {
                    throw new AiDevreDisiException();
                }
            };
        }

        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .restClientBuilder(org.springframework.web.client.RestClient.builder()
                        .requestFactory(zamanAsimliFabrika()))
                .build();
        OpenAiChatModel chatModel = OpenAiChatModel.builder().openAiApi(api).build();
        log.info("AI öğretmen aktif — sağlayıcı={} guclu={} hizli={}",
                baseUrl, props.ai().model().guclu(), props.ai().model().hizli());
        return new SpringAiOgretmenClient(chatModel, props, kullanimRepo);
    }

    /**
     * Anlam-eşleşmeli bağlam (ADR-0005): OpenAiEmbeddingModel + SimpleVectorStore (bellek-içi,
     * JSON persist). Anahtar yoksa no-op — {@link AnlamArama#benzerleri} boş döner ve
     * KitapService hafta-etiketi eşleşmesine düşer (uygulama anahtarsız da açılır).
     */
    @Bean
    public AnlamArama anlamArama(
            @Value("${spring.ai.openai.api-key:}") String apiKey,
            @Value("${spring.ai.openai.base-url:https://openrouter.ai/api}") String baseUrl,
            AppProperties props) {

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OPENROUTER_API_KEY yok — anlam araması devre dışı; hafta-etiketi eşleşmesine düşülür.");
            return new AnlamArama() {
                @Override public boolean aktif() { return false; }
                @Override public void indexle(List<NotParcasi> parcalar) { /* no-op */ }
                @Override public List<BenzerNot> benzerleri(String sorgu, int k) { return List.of(); }
                @Override public void temizle() { /* no-op */ }
                @Override public int sayac() { return 0; }
            };
        }

        String embModel = props.ai() != null && props.ai().model() != null
                && props.ai().model().embedding() != null && !props.ai().model().embedding().isBlank()
                ? props.ai().model().embedding() : "google/gemini-embedding-001";

        // Chat ile AYNI timeout: açılışta toplu embed (baslangictaIndeksle) asılı kalmasın (mimari inceleme).
        OpenAiApi embApi = OpenAiApi.builder().baseUrl(baseUrl).apiKey(apiKey)
                .restClientBuilder(org.springframework.web.client.RestClient.builder()
                        .requestFactory(zamanAsimliFabrika()))
                .build();
        OpenAiEmbeddingModel embeddingModel = new OpenAiEmbeddingModel(embApi, MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder().model(embModel).build());

        SimpleVectorStore vektorDepo = SimpleVectorStore.builder(embeddingModel).build();

        String yol = props.vektorDosyasi() != null && !props.vektorDosyasi().isBlank()
                ? props.vektorDosyasi()
                : Path.of(props.hafizaDir()).resolve("vektor.json").toString();

        log.info("Anlam araması aktif — embedding={} depo={}", embModel, yol);
        return new SpringAiAnlamArama(vektorDepo, new File(yol));
    }

    /**
     * Ortak zaman aşımlı HTTP istek fabrikası — hem chat hem embedding istemcisi kullanır.
     * Timeout'suz istemci asılı kalabiliyor (bağlantı 10sn, okuma 150sn; frontend 180sn'de keser).
     */
    private static org.springframework.http.client.SimpleClientHttpRequestFactory zamanAsimliFabrika() {
        var f = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        f.setConnectTimeout(java.time.Duration.ofSeconds(10));
        f.setReadTimeout(java.time.Duration.ofSeconds(150));
        return f;
    }
}
