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
import tr.velzon.aiogretmen.config.CalismaModu;
import tr.velzon.aiogretmen.config.KullaniciBaglami;
import tr.velzon.aiogretmen.repo.KullanimRepo;
import tr.velzon.aiogretmen.types.AiDevreDisiException;
import tr.velzon.aiogretmen.types.BenzerNot;
import tr.velzon.aiogretmen.types.NotParcasi;

import org.springframework.ai.retry.TransientAiException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.CompositeRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.io.File;
import java.time.Duration;
import java.util.Map;
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
            AppProperties props, KullanimRepo kullanimRepo, KullaniciBaglami kullaniciBaglami) {

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
        int deneme = denemeSayisi(props);
        OpenAiChatModel chatModel = sohbetModeli(api, yenidenDenemeSablonu(deneme, YENIDEN_DENEME_PENCERESI, ILK_BEKLEME));
        log.info("AI öğretmen aktif — sağlayıcı={} guclu={} hizli={} deneme={} okumaZamanAsimi={}sn",
                baseUrl, props.ai().model().guclu(), props.ai().model().hizli(), deneme, OKUMA_ZAMAN_ASIMI.toSeconds());
        return new SpringAiOgretmenClient(chatModel, props, kullanimRepo, kullaniciBaglami);
    }

    /**
     * Anlam-eşleşmeli bağlam (ADR-0005): OpenAiEmbeddingModel + SimpleVectorStore (bellek-içi,
     * JSON persist). Anahtar yoksa no-op — {@link AnlamArama#benzerleri} boş döner ve
     * KitapService hafta-etiketi eşleşmesine düşer (uygulama anahtarsız da açılır).
     *
     * <p>Faz 9b: {@code site} profilinde de no-op — kitap notları ve vektör deposu kullanıcıya AYRILMAMIŞ
     * (ortak) olduğundan çok kullanıcılı sitede yüklenmez/indekslenmez/sorgulanmaz (ADR-0012).
     */
    @Bean
    public AnlamArama anlamArama(
            @Value("${spring.ai.openai.api-key:}") String apiKey,
            @Value("${spring.ai.openai.base-url:https://openrouter.ai/api}") String baseUrl,
            AppProperties props, CalismaModu calismaModu) {

        if (calismaModu.siteMi()) {
            log.info("site profili — ortak kitap/vektör deposu KAPALI (kullanıcıya ayrılmamış veri).");
            return kapaliAnlamArama();
        }
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OPENROUTER_API_KEY yok — anlam araması devre dışı; hafta-etiketi eşleşmesine düşülür.");
            return kapaliAnlamArama();
        }

        String embModel = props.ai() != null && props.ai().model() != null
                && props.ai().model().embedding() != null && !props.ai().model().embedding().isBlank()
                ? props.ai().model().embedding() : "google/gemini-embedding-001";

        // Chat ile AYNI timeout: açılışta toplu embed (baslangictaIndeksle) asılı kalmasın (mimari inceleme).
        OpenAiApi embApi = OpenAiApi.builder().baseUrl(baseUrl).apiKey(apiKey)
                .restClientBuilder(org.springframework.web.client.RestClient.builder()
                        .requestFactory(zamanAsimliFabrika()))
                .build();
        // Varsayılan (10 deneme, 3dk'ya kadar bekleme) sohbet isteğini asılı bırakırdı: aynı kısa politika
        OpenAiEmbeddingModel embeddingModel = new OpenAiEmbeddingModel(embApi, MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder().model(embModel).build(),
                yenidenDenemeSablonu(denemeSayisi(props), YENIDEN_DENEME_PENCERESI, ILK_BEKLEME));

        SimpleVectorStore vektorDepo = SimpleVectorStore.builder(embeddingModel).build();

        String yol = props.vektorDosyasi() != null && !props.vektorDosyasi().isBlank()
                ? props.vektorDosyasi()
                : Path.of(props.hafizaDir()).resolve("vektor.json").toString();

        log.info("Anlam araması aktif — embedding={} depo={}", embModel, yol);
        return new SpringAiAnlamArama(vektorDepo, new File(yol));
    }

    private static AnlamArama kapaliAnlamArama() {
        return new AnlamArama() {
            @Override public boolean aktif() { return false; }
            @Override public void indexle(List<NotParcasi> parcalar) { /* no-op */ }
            @Override public List<BenzerNot> benzerleri(String sorgu, int k) { return List.of(); }
            @Override public void temizle() { /* no-op */ }
            @Override public int sayac() { return 0; }
        };
    }

    // ---- Zaman bütçesi: frontend AI isteğini 180sn'de keser; en kötü durum ~157sn kalmalı ----
    // Yeni deneme YALNIZ ilk denemenin başından itibaren 30sn içinde başlar (hızlı 5xx/bağlantı hataları
    // için); okuma zaman aşımı (120sn) pencereyi zaten aştığından YENİDEN DENENMEZ → kullanıcı YANIT_YOK
    // mesajını görür. En kötü: 30sn (pencere) + 2sn (bekleme) + 5sn (bağlantı) + 120sn (okuma) = 157sn.
    // Okuma 120sn: 3000-4000 token'lık rapor/quiz/görsel üretimi (~60-80sn) kesilmesin diye pay bırakıldı.
    static final Duration BAGLANTI_ZAMAN_ASIMI = Duration.ofSeconds(5);
    static final Duration OKUMA_ZAMAN_ASIMI = Duration.ofSeconds(120);
    static final Duration YENIDEN_DENEME_PENCERESI = Duration.ofSeconds(30);
    static final Duration ILK_BEKLEME = Duration.ofSeconds(1);
    static final Duration EN_UZUN_BEKLEME = Duration.ofSeconds(4);

    /**
     * Spring AI'nin varsayılan RetryTemplate'i (RetryUtils.DEFAULT_RETRY_TEMPLATE: 10 deneme, 2sn×5 üstel,
     * 180sn'ye kadar bekleme) yerine kısa politika. {@code spring.ai.retry.*} ayarları burada ETKİSİZDİR:
     * model elle (builder) kurulduğu için oto-yapılandırmanın RetryTemplate bean'i kullanılmaz.
     * Yalnız geçici hatalar denenir (TransientAiException = 5xx, ResourceAccessException = bağlantı/zaman
     * aşımı); 4xx (NonTransientAiException: 401/402/429…) hemen düşer. Bekleme: ilk, ×2, en çok 4sn.
     */
    static RetryTemplate yenidenDenemeSablonu(int denemeSayisi, Duration pencere, Duration ilkBekleme) {
        SimpleRetryPolicy deneme = new SimpleRetryPolicy(denemeSayisi, Map.of(
                TransientAiException.class, true,
                org.springframework.web.client.ResourceAccessException.class, true), false);
        CompositeRetryPolicy politika = new CompositeRetryPolicy(); // iyimser DEĞİL: ikisi de izin vermeli
        politika.setPolicies(new RetryPolicy[] {deneme, new TimeoutRetryPolicy(pencere.toMillis())});

        ExponentialBackOffPolicy bekleme = new ExponentialBackOffPolicy();
        bekleme.setInitialInterval(ilkBekleme.toMillis());
        bekleme.setMultiplier(2.0);
        bekleme.setMaxInterval(Math.max(ilkBekleme.toMillis(), EN_UZUN_BEKLEME.toMillis()));

        RetryTemplate sablon = new RetryTemplate();
        sablon.setRetryPolicy(politika);
        sablon.setBackOffPolicy(bekleme);
        sablon.registerListener(new RetryListener() {
            @Override
            public <T, E extends Throwable> void onError(RetryContext ctx, RetryCallback<T, E> cb, Throwable hata) {
                log.warn("AI çağrısı geçici hata — deneme {}/{}: {}", ctx.getRetryCount(), denemeSayisi,
                        hata.getClass().getSimpleName()); // gövde/anahtar loglanmaz
            }
        });
        return sablon;
    }

    /** Sohbet modeli — test, gerçek modelin bu şablonla yeniden denediğini buradan doğrular. */
    static OpenAiChatModel sohbetModeli(OpenAiApi api, RetryTemplate sablon) {
        return OpenAiChatModel.builder().openAiApi(api).retryTemplate(sablon).build();
    }

    private static int denemeSayisi(AppProperties props) {
        return props.ai() == null ? AppProperties.Ai.VARSAYILAN_DENEME : props.ai().denemeSayisi();
    }

    /** Ortak zaman aşımlı HTTP istek fabrikası — hem chat hem embedding istemcisi kullanır. */
    private static org.springframework.http.client.SimpleClientHttpRequestFactory zamanAsimliFabrika() {
        var f = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        f.setConnectTimeout(BAGLANTI_ZAMAN_ASIMI);
        f.setReadTimeout(OKUMA_ZAMAN_ASIMI);
        return f;
    }
}
