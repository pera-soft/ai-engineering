package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import tr.velzon.aiogretmen.repo.ZiyaretciRepo;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Faz 10 (ADR-0017): ziyaretçi kaydı. Gerçek H2 (schema.sql) üzerinde; sahte saatle "günde en çok bir yazma".
 * Spring bağlamı yok — servis {@code @Profile("site")} olduğu için doğrudan kurulur.
 */
class ZiyaretciServiceTest {

    private static final Instant T0 = Instant.parse("2026-11-25T10:00:00Z");
    private static final String ID = "z-AAAAAAAAAAAAAAAAAAAAAa";

    private JdbcTemplate jdbc;
    private ZiyaretciRepo repo;

    @BeforeEach
    void kur() throws Exception {
        var ds = new DriverManagerDataSource(
                "jdbc:h2:mem:ziyaretci-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        try (Connection c = ds.getConnection()) {
            ScriptUtils.executeSqlScript(c, new ClassPathResource("schema.sql"));
        }
        jdbc = new JdbcTemplate(ds);
        repo = new ZiyaretciRepo(jdbc);
    }

    private ZiyaretciService servis(Instant an) {
        return new ZiyaretciService(repo, Clock.fixed(an, ZoneOffset.UTC));
    }

    private Instant sonGorulme(String id) {
        return jdbc.queryForObject("SELECT son_gorulme FROM ziyaretci WHERE id = ?", Timestamp.class, id).toInstant();
    }

    private int satir(String id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM ziyaretci WHERE id = ?", Integer.class, id);
    }

    @Test
    void ilkDokunmaSatirOlusturur() {
        assertThat(servis(T0).varMi(ID)).isFalse();

        servis(T0).dokun(ID);

        assertThat(satir(ID)).isEqualTo(1);
        assertThat(servis(T0).varMi(ID)).isTrue();
        assertThat(sonGorulme(ID)).isEqualTo(T0);
        assertThat(jdbc.queryForObject("SELECT olusturma FROM ziyaretci WHERE id = ?", Timestamp.class, ID)
                .toInstant()).isEqualTo(T0);
    }

    @Test
    void ayniGunIkinciDokunmaYazmaz() {
        servis(T0).dokun(ID);
        servis(T0.plus(Duration.ofHours(23))).dokun(ID);

        assertThat(sonGorulme(ID)).as("1 günden yeni kayıt güncellenmez").isEqualTo(T0);
    }

    @Test
    void birGunSonraSonGorulmeGuncellenir() {
        servis(T0).dokun(ID);
        Instant sonra = T0.plus(Duration.ofHours(25));
        servis(sonra).dokun(ID);

        assertThat(sonGorulme(ID)).isEqualTo(sonra);
        assertThat(satir(ID)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT olusturma FROM ziyaretci WHERE id = ?", Timestamp.class, ID)
                .toInstant()).as("oluşturma değişmez").isEqualTo(T0);
    }

    @Test
    void esZamanliDokunmaHataVermez() throws Exception {
        int n = 8;
        ExecutorService havuz = Executors.newFixedThreadPool(n);
        CountDownLatch baslat = new CountDownLatch(1);
        ZiyaretciService s = servis(T0);
        try {
            List<Future<?>> sonuclar = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                sonuclar.add(havuz.submit(() -> {
                    baslat.await(); // hepsi aynı anda — INSERT yarışını zorla
                    s.dokun(ID);
                    return null;
                }));
            }
            baslat.countDown();
            for (Future<?> f : sonuclar) {
                f.get(10, TimeUnit.SECONDS); // istisna olsaydı fırlatırdı
            }
        } finally {
            havuz.shutdownNow();
        }
        assertThat(satir(ID)).isEqualTo(1);
    }

    @Test
    void eskilerVeSilTemizlikIcin() {
        servis(T0).dokun(ID);
        servis(T0.plus(Duration.ofDays(10))).dokun("z-BBBBBBBBBBBBBBBBBBBBBb");

        assertThat(repo.eskiler(T0.plus(Duration.ofDays(5)))).containsExactly(ID);
        repo.sil(ID);
        assertThat(repo.varMi(ID)).isFalse();
    }
}
