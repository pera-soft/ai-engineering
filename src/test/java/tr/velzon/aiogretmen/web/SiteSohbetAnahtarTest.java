package tr.velzon.aiogretmen.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tr.velzon.aiogretmen.config.ZiyaretciCerezi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Faz 10 (ADR-0017): sitede sohbet, ziyaretçinin {@code X-OpenRouter-Anahtar} başlığıyla gelen kendi anahtarıyla
 * yapılır. OpenRouter yerine yerel sahte HTTP sunucusu ({@code com.sun.net.httpserver}; yeni bağımlılık yok):
 * giden {@code Authorization} başlıkları ve istek gövdeleri (model adı) yakalanır.
 *
 * <p>Sunucu anahtarı ({@code spring.ai.openai.api-key}) bu sınıfta BİLEREK dolu verilir: sitede asla
 * kullanılmadığı her testte kanıtlanır.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sitesohbetanahtar;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=" + SiteSohbetAnahtarTest.SUNUCU_ANAHTARI,
        "app.ai.model.guclu=guclu-test",
        "app.ai.model.hizli=hizli-test",
        "app.rapor-dir=target/test-raporlar-sitesohbetanahtar"
})
@ActiveProfiles("site")
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class SiteSohbetAnahtarTest {

    static final String SUNUCU_ANAHTARI = "sk-or-v1-test-sunucu-asla-kullanilmaz";
    private static final String ANAHTAR_A = "sk-or-v1-test-ziyaretci-aaaa";
    private static final String ANAHTAR_B = "sk-or-v1-test-ziyaretci-bbbb";
    private static final String GECERSIZ = "sk-or-v1-test-iptal-edilmis-99";
    private static final String MESAJ = "{\"hafta\":1,\"mesaj\":\"Embedding nedir?\"}";
    private static final ObjectMapper JSON = new ObjectMapper();

    /** Sahte OpenRouter'ın gördüğü istek. */
    record Giden(String yetki, String govde) {
    }

    static final ConcurrentLinkedQueue<Giden> GIDENLER = new ConcurrentLinkedQueue<>();
    static volatile int durum = 200;
    static volatile CountDownLatch bulusma;
    /** Her sahte-sunucu isteğinin buluşmayı gerçekten görüp görmediği (await sonucu). */
    static final ConcurrentLinkedQueue<Boolean> BULUSMA_SONUCLARI = new ConcurrentLinkedQueue<>();
    static final HttpServer SUNUCU;
    static final Path HAFIZA;

    static {
        try {
            HAFIZA = Files.createTempDirectory("sitesohbetanahtar-hafiza");
            SUNUCU = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        SUNUCU.setExecutor(Executors.newCachedThreadPool());
        SUNUCU.createContext("/", SiteSohbetAnahtarTest::cevapla);
        SUNUCU.start();
    }

    @DynamicPropertySource
    static void ozellikler(DynamicPropertyRegistry r) {
        r.add("spring.ai.openai.base-url", () -> "http://127.0.0.1:" + SUNUCU.getAddress().getPort());
        r.add("app.hafiza-dir", HAFIZA::toString);
    }

    @AfterAll
    static void kapat() {
        SUNUCU.stop(0);
    }

    private static void cevapla(HttpExchange x) throws IOException {
        String govde;
        try (InputStream in = x.getRequestBody()) {
            govde = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        String yetki = x.getRequestHeaders().getFirst("Authorization");
        GIDENLER.add(new Giden(yetki, govde));
        CountDownLatch b = bulusma;
        if (b != null) { // iki isteğin gerçekten aynı anda uçuşta olduğunu garanti et
            b.countDown();
            try {
                BULUSMA_SONUCLARI.add(b.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                BULUSMA_SONUCLARI.add(false);
                Thread.currentThread().interrupt();
            }
        }
        byte[] cevap;
        int kod = durum;
        if (kod != 200) {
            // gerçek OpenRouter gövdesi anahtarı yansıtsa bile sızmamalı
            cevap = ("{\"error\":{\"message\":\"User not found: " + yetki + "\",\"code\":" + kod + "}}")
                    .getBytes(StandardCharsets.UTF_8);
        } else {
            String icerik;
            if (govde.contains("hizli-test")) {
                icerik = JSON.writeValueAsString(Map.of("ogrenci_profili", "# Öğrenci profili\nEmbedding sordu.",
                        "hafta_notlari", "- embedding konuşuldu"));
            } else if (yetki != null && yetki.endsWith(ANAHTAR_B)) {
                icerik = "cevap-B";
            } else {
                icerik = "cevap-A";
            }
            cevap = JSON.writeValueAsBytes(Map.of(
                    "id", "gen-1", "object", "chat.completion", "created", 1, "model", "saglayici-model",
                    "choices", List.of(Map.of("index", 0, "finish_reason", "stop",
                            "message", Map.of("role", "assistant", "content", icerik))),
                    "usage", Map.of("prompt_tokens", 7, "completion_tokens", 3, "total_tokens", 10)));
        }
        x.getResponseHeaders().add("Content-Type", "application/json");
        x.sendResponseHeaders(kod, cevap.length);
        try (OutputStream out = x.getResponseBody()) {
            out.write(cevap);
        }
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void sifirla() {
        GIDENLER.clear();
        durum = 200;
        bulusma = null;
        BULUSMA_SONUCLARI.clear();
    }

    private MvcResult sohbet(String ip, String cerez, String anahtar) throws Exception {
        var istek = post("/api/sohbet").with(r -> {
                    r.setRemoteAddr(ip);
                    return r;
                }).with(csrf()).cookie(new Cookie(ZiyaretciCerezi.AD, cerez))
                .contentType(MediaType.APPLICATION_JSON).content(MESAJ);
        if (anahtar != null) {
            istek.header("X-OpenRouter-Anahtar", anahtar);
        }
        return mvc.perform(istek).andReturn();
    }

    private int sohbetSatiri(String cerez) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM sohbet_mesaj WHERE kullanici_id = ?", Integer.class,
                "z-" + cerez);
    }

    private String kullanimSatirlari() {
        return jdbc.queryForList("SELECT * FROM ai_kullanim").toString();
    }

    @Test
    void basliksizSohbet400VeAiCagrilmaz() throws Exception {
        String cerez = "BBBBBBBBBBBBBBBBBBBBBa";
        MvcResult r = sohbet("10.6.0.1", cerez, null);

        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(r.getResponse().getContentAsString())
                .isEqualTo("{\"hata\":\"AI öğretmenle sohbet için kendi OpenRouter anahtarını eklemelisin.\","
                        + "\"neden\":\"ANAHTAR_GEREKLI\"}");
        assertThat(GIDENLER).as("sahte OpenRouter'a istek düşmez").isEmpty();
        assertThat(sohbetSatiri(cerez)).isZero();
    }

    @Test
    void bozukBicimliBaslik400() throws Exception {
        String cerez = "BBBBBBBBBBBBBBBBBBBBBb";
        MvcResult r = sohbet("10.6.0.7", cerez, "sk-ant-GIZLIBICIMSIZ123456");

        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(r.getResponse().getContentAsString()).contains("sk-or-").doesNotContain("GIZLIBICIMSIZ")
                .contains("\"hata\":\"Geçersiz istek\"").contains("\"neden\":\"ANAHTAR_BICIMI\"");
        assertThat(GIDENLER).isEmpty();
        assertThat(sohbetSatiri(cerez)).isZero();
    }

    @Test
    void baslikliSohbetZiyaretciAnahtariniKullanir(CapturedOutput cikti) throws Exception {
        String cerez = "CCCCCCCCCCCCCCCCCCCCCa";
        MvcResult r = sohbet("10.6.0.2", cerez, "  " + ANAHTAR_A + " ");

        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        assertThat(r.getResponse().getContentAsString()).contains("cevap-A").doesNotContain(ANAHTAR_A);
        assertThat(GIDENLER).hasSize(1);
        Giden g = GIDENLER.peek();
        assertThat(g.yetki()).isEqualTo("Bearer " + ANAHTAR_A);
        assertThat(g.govde()).contains("\"guclu-test\"");
        assertThat(sohbetSatiri(cerez)).isEqualTo(2);
        assertThat(kullanimSatirlari()).contains("z-" + cerez).doesNotContain(ANAHTAR_A);
        assertThat(cikti.getAll()).doesNotContain(ANAHTAR_A);
    }

    @Test
    void gecersizAnahtar401TurkceVeSizintisiz(CapturedOutput cikti) throws Exception {
        String cerez = "DDDDDDDDDDDDDDDDDDDDDa";
        durum = 401;
        MvcResult r = sohbet("10.6.0.3", cerez, GECERSIZ);

        assertThat(r.getResponse().getStatus()).isEqualTo(401);
        String govde = r.getResponse().getContentAsString();
        assertThat(govde).contains("geçersiz").doesNotContain(GECERSIZ).doesNotContain("User not found");
        assertThat(GIDENLER).as("Spring AI 401'i yeniden denemez").hasSize(1);
        assertThat(cikti.getAll()).doesNotContain(GECERSIZ);
        assertThat(kullanimSatirlari()).doesNotContain(GECERSIZ);
        assertThat(sohbetSatiri(cerez)).isZero();
        // istisna zincirinde ham sağlayıcı gövdesi taşınmaz — ileride bir log.error(..., e) ya da toString
        // zinciri anahtarı sızdırmasın. (JDK HttpURLConnection 401 gövdesini düşürür; gövdeli kanıt 402'de
        // ve AiHataCeviriciTest'te.)
        assertThat(r.getResolvedException()).isNotNull();
        for (Throwable t = r.getResolvedException(); t != null; t = t.getCause() == t ? null : t.getCause()) {
            assertThat(String.valueOf(t.getMessage())).as(t.getClass().getName()).doesNotContain("sk-or-");
            assertThat(t.toString()).doesNotContain("sk-or-");
        }
    }

    @Test
    void krediBitti402VeCokIstek429() throws Exception {
        durum = 402;
        MvcResult r = sohbet("10.6.0.8", "DDDDDDDDDDDDDDDDDDDDDb", GECERSIZ);
        assertThat(r.getResponse().getStatus()).isEqualTo(402);
        assertThat(r.getResponse().getContentAsString()).contains("kredin bitti");
        for (Throwable t = r.getResolvedException(); t != null; t = t.getCause() == t ? null : t.getCause()) {
            assertThat(t.toString()).as("402 gövdesi anahtarı yansıtır; zincirde olmamalı").doesNotContain("sk-or-");
        }

        durum = 429;
        r = sohbet("10.6.0.8", "DDDDDDDDDDDDDDDDDDDDDb", GECERSIZ);
        assertThat(r.getResponse().getStatus()).isEqualTo(429);
        assertThat(r.getResponse().getContentAsString()).contains("biraz bekleyip");
    }

    @Test
    void dorduncuMesajdaHafizaZiyaretciAnahtariylaGuncellenir(CapturedOutput cikti) throws Exception {
        String cerez = "EEEEEEEEEEEEEEEEEEEEEa";
        for (int i = 1; i <= 4; i++) {
            assertThat(sohbet("10.6.0.4", cerez, ANAHTAR_A).getResponse().getStatus()).as("mesaj " + i)
                    .isEqualTo(200);
        }

        List<Giden> liste = List.copyOf(GIDENLER);
        assertThat(liste).hasSize(5);
        assertThat(liste.get(4).govde()).contains("\"hizli-test\"");
        assertThat(liste).allSatisfy(g -> assertThat(g.yetki()).isEqualTo("Bearer " + ANAHTAR_A));
        try (Stream<Path> s = Files.walk(HAFIZA)) {
            List<Path> profiller = s.filter(p -> p.getFileName().toString().equals("OGRENCI.md")).toList();
            assertThat(profiller).hasSize(1);
            assertThat(Files.readString(profiller.get(0))).contains("Embedding sordu.").doesNotContain(ANAHTAR_A);
        }
        assertThat(kullanimSatirlari()).contains("HAFIZA").doesNotContain(ANAHTAR_A);
        assertThat(cikti.getAll()).doesNotContain(ANAHTAR_A);
    }

    @Test
    void ikiZiyaretciAnahtarlariKarismaz() throws Exception {
        bulusma = new CountDownLatch(2);
        CompletableFuture<MvcResult> a = CompletableFuture.supplyAsync(() -> calistir("10.6.0.5", "FFFFFFFFFFFFFFFFFFFFFa", ANAHTAR_A));
        CompletableFuture<MvcResult> b = CompletableFuture.supplyAsync(() -> calistir("10.6.0.6", "FFFFFFFFFFFFFFFFFFFFFb", ANAHTAR_B));

        MvcResult ra = a.get(30, TimeUnit.SECONDS);
        MvcResult rb = b.get(30, TimeUnit.SECONDS);

        assertThat(ra.getResponse().getStatus()).isEqualTo(200);
        assertThat(rb.getResponse().getStatus()).isEqualTo(200);
        assertThat(BULUSMA_SONUCLARI).as("iki istek sahte sunucuda gerçekten aynı anda bekledi")
                .containsExactly(true, true);
        assertThat(ra.getResponse().getContentAsString()).contains("cevap-A");
        assertThat(rb.getResponse().getContentAsString()).contains("cevap-B");
        assertThat(GIDENLER).extracting(Giden::yetki)
                .containsExactlyInAnyOrder("Bearer " + ANAHTAR_A, "Bearer " + ANAHTAR_B);
    }

    private MvcResult calistir(String ip, String cerez, String anahtar) {
        try {
            return sohbet(ip, cerez, anahtar);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void sunucuAnahtariSitedeKullanilmaz() throws Exception {
        // sınıf düzeyinde spring.ai.openai.api-key DOLU verildi; başlıksız istek yine 400, giden çağrı yok
        MvcResult r = sohbet("10.6.0.9", "GGGGGGGGGGGGGGGGGGGGGa", null);

        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(GIDENLER).isEmpty();

        sohbet("10.6.0.9", "GGGGGGGGGGGGGGGGGGGGGa", ANAHTAR_A);
        assertThat(GIDENLER).extracting(Giden::yetki).doesNotContain("Bearer " + SUNUCU_ANAHTARI)
                .containsExactly("Bearer " + ANAHTAR_A);
        assertThat(mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/ozellikler")
                .with(r2 -> {
                    r2.setRemoteAddr("10.6.0.9");
                    return r2;
                })).andReturn().getResponse().getContentAsString()).contains("\"sunucuAi\":false");
    }
}
