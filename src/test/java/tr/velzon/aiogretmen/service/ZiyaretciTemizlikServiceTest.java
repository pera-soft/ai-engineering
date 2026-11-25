package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tr.velzon.aiogretmen.ai.JsonCevapParser;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.repo.GonderimRepo;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.KullanimRepo;
import tr.velzon.aiogretmen.repo.MulakatCevapRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;
import tr.velzon.aiogretmen.repo.SohbetRepo;
import tr.velzon.aiogretmen.repo.ZiyaretciRepo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Faz 10 (ADR-0017): 180 gündür gelmeyen ziyaretçinin verisi silinir. Gerçek H2 + schema.sql + gerçek dosya sistemi.
 *
 * <p>Sınıf testin KENDİ işlemini kapatır (NOT_SUPPORTED): servis kendi okuma-yazma işlemini açmalı; test işlemi
 * eksikliği maskelemesin. Veri gerçekten commit edilir → her testten sonra tablolar temizlenir.
 */
@DataJdbcTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ZiyaretciTemizlikServiceTest {

    private static final Instant SIMDI = Instant.parse("2026-11-25T03:30:00Z");
    private static final String ESKI = "z-AAAAAAAAAAAAAAAAAAAAAa";
    private static final String YENI = "z-BBBBBBBBBBBBBBBBBBBBBb";

    @TempDir
    Path tempDir;

    @Autowired JdbcTemplate jdbc;
    @Autowired IlerlemeRepo ilerlemeRepo;
    @Autowired GonderimRepo gonderimRepo;
    @Autowired QuizSonucRepo quizSonucRepo;
    @Autowired SohbetRepo sohbetRepo;
    @Autowired MulakatCevapRepo mulakatCevapRepo;
    @Autowired KullanimRepo kullanimRepo;
    @Autowired PlatformTransactionManager islemYoneticisi;

    ZiyaretciRepo ziyaretciRepo;
    ZiyaretciTemizlikService servis;
    Path hafizaKok;

    @BeforeEach
    @AfterEach
    void temizle() {
        for (String t : new String[]{"gorev_ilerleme", "gonderim", "quiz_sonuc", "sohbet_mesaj", "mulakat_cevap",
                "ai_kullanim", "ziyaretci"}) {
            jdbc.update("DELETE FROM " + t);
        }
    }

    @BeforeEach
    void kur() {
        hafizaKok = tempDir.resolve("hafiza");
        AppProperties props = new AppProperties("./content", "prompts", hafizaKok.toString(), null, null, null, null);
        HafizaService hafiza = new HafizaService(props, mock(OgretmenAiClient.class),
                new PromptFactory(Path.of("prompts")), new JsonCevapParser(), () -> "yerel");
        SifirlamaService sifirlama = new SifirlamaService(ilerlemeRepo, gonderimRepo, quizSonucRepo, sohbetRepo,
                mulakatCevapRepo, hafiza, mock(RaporService.class), () -> "yerel");
        ziyaretciRepo = new ZiyaretciRepo(jdbc);
        servis = new ZiyaretciTemizlikService(ziyaretciRepo, sifirlama, kullanimRepo,
                islemYoneticisi, Clock.fixed(SIMDI, ZoneOffset.UTC));
    }

    @Test
    void eskiZiyaretciTamamenSilinir() throws Exception {
        veriYaz(ESKI, 181);

        assertThat(servis.temizle()).isEqualTo(1);

        assertThat(sifirKalan(ESKI)).isTrue();
        assertThat(ziyaretciRepo.varMi(ESKI)).isFalse();
        assertThat(Files.exists(KisiselDizin.coz(hafizaKok, ESKI))).isFalse();
    }

    @Test
    void yeniZiyaretciKalir() throws Exception {
        veriYaz(YENI, 179);

        assertThat(servis.temizle()).isZero();

        assertThat(ziyaretciRepo.varMi(YENI)).isTrue();
        assertThat(say("gorev_ilerleme", YENI)).isEqualTo(1);
        assertThat(say("ai_kullanim", YENI)).isEqualTo(1);
        assertThat(Files.exists(KisiselDizin.coz(hafizaKok, YENI).resolve("OGRENCI.md"))).isTrue();
    }

    @Test
    void ikiKezCalismakZararsiz() throws Exception {
        veriYaz(ESKI, 200);
        veriYaz(YENI, 1);

        assertThat(servis.temizle()).isEqualTo(1);
        assertThat(servis.temizle()).isZero();

        assertThat(sifirKalan(ESKI)).isTrue();
        assertThat(ziyaretciRepo.varMi(YENI)).isTrue();
        assertThat(say("gorev_ilerleme", YENI)).isEqualTo(1);
    }

    @Test
    void birZiyaretcininHatasiDigerleriniDurdurmaz() throws Exception {
        veriYaz(ESKI, 300);
        veriYaz("z-CCCCCCCCCCCCCCCCCCCCCc", 300);
        SifirlamaService bozuk = mock(SifirlamaService.class);
        doThrow(new IllegalStateException("IO")).when(bozuk).sifirla(ESKI);
        var s = new ZiyaretciTemizlikService(ziyaretciRepo, bozuk, kullanimRepo, islemYoneticisi,
                Clock.fixed(SIMDI, ZoneOffset.UTC));

        assertThat(s.temizle()).isEqualTo(1);

        assertThat(ziyaretciRepo.varMi(ESKI)).isTrue(); // satır durur → sonraki tur yeniden dener
        assertThat(ziyaretciRepo.varMi("z-CCCCCCCCCCCCCCCCCCCCCc")).isFalse();
        verify(bozuk).sifirla("z-CCCCCCCCCCCCCCCCCCCCCc");
    }

    /** H2 salt-okunur işlemde DELETE'e izin verir; bu yüzden doğrudan işlem durumu yoklanır (PostgreSQL korumasi). */
    @Test
    void adimlarTekOkumaYazmaIslemindeCalisir() throws Exception {
        veriYaz(ESKI, 181);
        boolean[] gorulen = new boolean[3]; // aktif, salt-okunur-DEGIL, 3 adim aynı işlem adı
        KullanimRepo gozcu = mock(KullanimRepo.class);
        doAnswer(c -> {
            gorulen[0] = TransactionSynchronizationManager.isActualTransactionActive();
            gorulen[1] = !TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            kullanimRepo.deleteByKullaniciId(c.getArgument(0));
            return null;
        }).when(gozcu).deleteByKullaniciId(anyString());
        var s = new ZiyaretciTemizlikService(ziyaretciRepo, sifirlamaYap(), gozcu, islemYoneticisi,
                Clock.fixed(SIMDI, ZoneOffset.UTC));

        assertThat(s.temizle()).isEqualTo(1);

        assertThat(gorulen[0]).as("işlem aktif").isTrue();
        assertThat(gorulen[1]).as("okuma-yazma").isTrue();
        assertThat(sifirKalan(ESKI)).isTrue();
    }

    @Test
    void aiKullanimSilmesiPatlarsaHerSeyGeriAlinir() throws Exception {
        veriYaz(ESKI, 181);
        KullanimRepo bozuk = mock(KullanimRepo.class);
        doThrow(new IllegalStateException("read-only")).when(bozuk).deleteByKullaniciId(anyString());
        var s = new ZiyaretciTemizlikService(ziyaretciRepo, sifirlamaYap(), bozuk, islemYoneticisi,
                Clock.fixed(SIMDI, ZoneOffset.UTC));

        assertThat(s.temizle()).isZero();

        assertThat(ziyaretciRepo.varMi(ESKI)).isTrue();
        assertThat(say("gorev_ilerleme", ESKI)).isEqualTo(1); // sifirla geri alındı
        assertThat(say("ai_kullanim", ESKI)).isEqualTo(1);
    }

    private SifirlamaService sifirlamaYap() {
        AppProperties props = new AppProperties("./content", "prompts", hafizaKok.toString(), null, null, null, null);
        HafizaService hafiza = new HafizaService(props, mock(OgretmenAiClient.class),
                new PromptFactory(Path.of("prompts")), new JsonCevapParser(), () -> "yerel");
        return new SifirlamaService(ilerlemeRepo, gonderimRepo, quizSonucRepo, sohbetRepo, mulakatCevapRepo, hafiza,
                mock(RaporService.class), () -> "yerel");
    }

    private void veriYaz(String id, int gunOnce) throws Exception {
        Timestamp an = Timestamp.from(SIMDI.minus(Duration.ofDays(gunOnce)));
        jdbc.update("INSERT INTO ziyaretci (id, olusturma, son_gorulme) VALUES (?, ?, ?)", id, an, an);
        jdbc.update("INSERT INTO gorev_ilerleme (kullanici_id, hafta, gun, gorev_index, tamamlandi) VALUES (?,1,1,0,TRUE)", id);
        jdbc.update("INSERT INTO gonderim (kullanici_id, hafta, tur, icerik) VALUES (?,1,'OZET','x')", id);
        jdbc.update("INSERT INTO quiz_sonuc (kullanici_id, hafta, tur, puan) VALUES (?,1,'STATIK',3)", id);
        jdbc.update("INSERT INTO sohbet_mesaj (kullanici_id, hafta, rol, icerik) VALUES (?,1,'USER','x')", id);
        jdbc.update("INSERT INTO ai_kullanim (kullanici_id, islem, model) VALUES (?,'SOHBET','m')", id);
        Path d = KisiselDizin.coz(hafizaKok, id);
        Files.createDirectories(d);
        Files.writeString(d.resolve("OGRENCI.md"), "profil");
    }

    private int say(String tablo, String id) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + tablo + " WHERE kullanici_id = ?", Integer.class, id);
        return n == null ? 0 : n;
    }

    private boolean sifirKalan(String id) {
        return say("gorev_ilerleme", id) + say("gonderim", id) + say("quiz_sonuc", id)
                + say("sohbet_mesaj", id) + say("ai_kullanim", id) == 0;
    }
}
