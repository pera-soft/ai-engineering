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
import tr.velzon.aiogretmen.repo.ZiyaretciRepo.EskiZiyaretci;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
    Path raporKok;
    AppProperties props;

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
        raporKok = tempDir.resolve("raporlar");
        props = new AppProperties("./content", "prompts", hafizaKok.toString(), raporKok.toString(), null, null,
                null);
        ziyaretciRepo = new ZiyaretciRepo(jdbc);
        servis = servis(sifirlamaYap(), kullanimRepo, ZiyaretciTemizlikService.PARTI);
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
        SifirlamaService gercek = sifirlamaYap();
        SifirlamaService bozuk = mock(SifirlamaService.class);
        doAnswer(c -> {
            gercek.sifirla(c.getArgument(0));   // diğerleri gerçekten silinir (dizin dahil)
            return null;
        }).when(bozuk).sifirla(anyString());
        doThrow(new IllegalStateException("IO")).when(bozuk).sifirla(ESKI);
        var s = servis(bozuk, kullanimRepo, ZiyaretciTemizlikService.PARTI);

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
        var s = servis(sifirlamaYap(), gozcu, ZiyaretciTemizlikService.PARTI);

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
        var s = servis(sifirlamaYap(), bozuk, ZiyaretciTemizlikService.PARTI);

        assertThat(s.temizle()).isZero();

        assertThat(ziyaretciRepo.varMi(ESKI)).isTrue();
        assertThat(say("gorev_ilerleme", ESKI)).isEqualTo(1); // sifirla geri alındı
        assertThat(say("ai_kullanim", ESKI)).isEqualTo(1);
    }

    /** Tech-debt #16: kişisel dizin silinemezse işlem geri alınır; engel kalkınca sonraki tur tamamen siler. */
    @Test
    void dizinSilinemezseSatirlarKalirSonrakiTurSiler() throws Exception {
        assumeFalse("root".equals(System.getProperty("user.name")), "chmod root'ta etkisiz");
        veriYaz(ESKI, 181);
        Path dizin = KisiselDizin.coz(hafizaKok, ESKI);
        Set<PosixFilePermission> eski = Files.getPosixFilePermissions(dizin);
        Files.setPosixFilePermissions(dizin, PosixFilePermissions.fromString("r-xr-xr-x")); // içindeki silinemez
        try {
            assertThat(servis.temizle()).isZero();

            assertThat(ziyaretciRepo.varMi(ESKI)).as("satır kalır → sonraki gece yeniden denenir").isTrue();
            assertThat(say("gorev_ilerleme", ESKI)).isEqualTo(1);
            assertThat(say("ai_kullanim", ESKI)).isEqualTo(1);
            assertThat(Files.exists(dizin)).isTrue();
        } finally {
            Files.setPosixFilePermissions(dizin, eski);
        }

        assertThat(servis.temizle()).isEqualTo(1);

        assertThat(sifirKalan(ESKI)).isTrue();
        assertThat(ziyaretciRepo.varMi(ESKI)).isFalse();
        assertThat(Files.exists(dizin)).isFalse();
    }

    /** Rapor dizini de denetlenir (RaporService taklit: hiçbir şey silmez → dizin kalır). */
    @Test
    void raporDiziniKalirsaSatirlarKalir() throws Exception {
        veriYaz(ESKI, 181);
        Path rapor = KisiselDizin.coz(raporKok, ESKI);
        Files.createDirectories(rapor);
        Files.writeString(rapor.resolve("hafta-01.md"), "rapor");

        assertThat(servis.temizle()).isZero();   // sifirlamaYap'taki RaporService taklit → dizin silinmez

        assertThat(ziyaretciRepo.varMi(ESKI)).isTrue();
        assertThat(say("gorev_ilerleme", ESKI)).isEqualTo(1);
    }

    /** Tech-debt #17: 3'lük partilerle 7 eski ziyaretçi tamamen silinir. */
    @Test
    void partiPartiYediEskiZiyaretciSilinir() throws Exception {
        for (int i = 1; i <= 7; i++) {
            veriYaz(String.format("z-P%021d", i), 180 + i);
        }
        veriYaz(YENI, 3);

        assertThat(servis(sifirlamaYap(), kullanimRepo, 3).temizle()).isEqualTo(7);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ziyaretci", Integer.class)).isEqualTo(1);
        assertThat(ziyaretciRepo.varMi(YENI)).isTrue();
    }

    /** Hep başarısız olan (en eski) ziyaretçi döngüyü kilitlemez; tek turda bir kez denenir, diğerleri silinir. */
    @Test
    void hepBasarisizZiyaretciDonguyuKilitlemez() throws Exception {
        veriYaz(ESKI, 400);                                  // en eski → her partinin başında gelir
        for (int i = 1; i <= 4; i++) {
            veriYaz(String.format("z-Q%021d", i), 200 + i);
        }
        SifirlamaService gercek = sifirlamaYap();
        SifirlamaService bozuk = mock(SifirlamaService.class);
        doAnswer(c -> {
            String id = c.getArgument(0);
            if (ESKI.equals(id)) {
                throw new IllegalStateException("IO");
            }
            gercek.sifirla(id);
            return null;
        }).when(bozuk).sifirla(anyString());

        int silinen = assertTimeoutPreemptively(Duration.ofSeconds(10),
                () -> servis(bozuk, kullanimRepo, 3).temizle());

        assertThat(silinen).isEqualTo(4);
        assertThat(ziyaretciRepo.varMi(ESKI)).isTrue();
        verify(bozuk, times(1)).sifirla(ESKI);
    }

    /**
     * Son inceleme #1: hep başarısız olan TAM bir parti turu durdurmaz — başarısızlar OFFSET ile atlanır, arkalarındaki
     * daha yeni eski ziyaretçi (z-H…9) aynı turda silinir.
     */
    @Test
    void tamPartiBasarisizOlsaBileArkasindakiSilinir() throws Exception {
        for (int i = 1; i <= 3; i++) {
            veriYaz(String.format("z-H%021d", i), 300 + i);
        }
        String arkadaki = String.format("z-H%021d", 9);
        veriYaz(arkadaki, 200);
        SifirlamaService gercek = sifirlamaYap();
        SifirlamaService bozuk = mock(SifirlamaService.class);
        doAnswer(c -> {
            String id = c.getArgument(0);
            if (!arkadaki.equals(id)) {
                throw new IllegalStateException("IO");
            }
            gercek.sifirla(id);
            return null;
        }).when(bozuk).sifirla(anyString());

        var sonuc = assertTimeoutPreemptively(Duration.ofSeconds(10),
                () -> servis(bozuk, kullanimRepo, 3).temizleAyrintili());

        assertThat(sonuc).isEqualTo(new ZiyaretciTemizlikService.TemizlikSonucu(1, 0, 3));
        assertThat(ziyaretciRepo.varMi(arkadaki)).isFalse();
        assertThat(sifirKalan(arkadaki)).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ziyaretci", Integer.class)).isEqualTo(3);
        verify(bozuk, times(1)).sifirla(String.format("z-H%021d", 1));
    }

    /**
     * Son inceleme #2: {@code eskiler} ile silme arasında geri dönen (son_gorulme tazelenen) ziyaretçinin hiçbir
     * verisi silinmez — silme anında yaş yeniden denetlenir, tutmazsa işlem geri alınır.
     */
    @Test
    void aradaGeriDonenZiyaretcininVerisiKalir() throws Exception {
        veriYaz(ESKI, 181);
        ziyaretciRepo = new ZiyaretciRepo(jdbc) {
            @Override
            public java.util.List<EskiZiyaretci> eskiler(Instant esik, int enCok, Instant sonZaman, String sonId) {
                java.util.List<EskiZiyaretci> sonuc = super.eskiler(esik, enCok, sonZaman, sonId);
                jdbc.update("UPDATE ziyaretci SET son_gorulme = ? WHERE id = ?", Timestamp.from(SIMDI), ESKI);
                return sonuc;
            }
        };

        var sonuc = servis(sifirlamaYap(), kullanimRepo, ZiyaretciTemizlikService.PARTI).temizleAyrintili();

        // kalıntı #2: geri dönen ziyaretçi başarısızlık değil, ayrı sayılır
        assertThat(sonuc).isEqualTo(new ZiyaretciTemizlikService.TemizlikSonucu(0, 1, 0));
        assertThat(ziyaretciRepo.varMi(ESKI)).isTrue();
        assertThat(say("gorev_ilerleme", ESKI)).isEqualTo(1);
        assertThat(say("sohbet_mesaj", ESKI)).isEqualTo(1);
        assertThat(say("ai_kullanim", ESKI)).isEqualTo(1);
        assertThat(Files.exists(KisiselDizin.coz(hafizaKok, ESKI).resolve("OGRENCI.md")))
                .as("yaş denetimi dosya silmeden önce").isTrue();
    }

    /**
     * Kalıntı #1: yaş denetiminde reddedilen (geri dönen) ziyaretçi artık "eski" sorgusuna uymaz; OFFSET onu sayıp
     * gerçek bir eskiyi atlardı. Sayfalama imleçli (keyset): [A(geri döndü), B, C, D], parti 3 → B, C, D aynı turda.
     */
    @Test
    void geriDonenZiyaretciArkasindakiEskiyiAtlatmaz() throws Exception {
        String a = String.format("z-K%021d", 1);
        veriYaz(a, 400);
        for (int i = 2; i <= 4; i++) {
            veriYaz(String.format("z-K%021d", i), 300 - i);
        }
        boolean[] ilk = {true};
        ziyaretciRepo = new ZiyaretciRepo(jdbc) {
            @Override
            public java.util.List<EskiZiyaretci> eskiler(Instant esik, int enCok, Instant sonZaman, String sonId) {
                java.util.List<EskiZiyaretci> sonuc = super.eskiler(esik, enCok, sonZaman, sonId);
                if (ilk[0]) {
                    ilk[0] = false;
                    jdbc.update("UPDATE ziyaretci SET son_gorulme = ? WHERE id = ?", Timestamp.from(SIMDI), a);
                }
                return sonuc;
            }
        };

        assertThat(servis(sifirlamaYap(), kullanimRepo, 3).temizleAyrintili())
                .isEqualTo(new ZiyaretciTemizlikService.TemizlikSonucu(3, 1, 0));

        assertThat(ziyaretciRepo.varMi(a)).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ziyaretci", Integer.class)).isEqualTo(1);
    }

    private ZiyaretciTemizlikService servis(SifirlamaService sifirlama, KullanimRepo kullanim, int parti) {
        return new ZiyaretciTemizlikService(ziyaretciRepo, sifirlama, kullanim, islemYoneticisi, props,
                Clock.fixed(SIMDI, ZoneOffset.UTC), parti);
    }

    private SifirlamaService sifirlamaYap() {
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
