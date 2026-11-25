package tr.velzon.aiogretmen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.repo.KullanimRepo;
import tr.velzon.aiogretmen.repo.ZiyaretciRepo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Terk edilmiş ziyaretçi verisinin temizliği (Faz 10, ADR-0017): {@value #SAKLAMA_GUN} gündür gelmeyen
 * ziyaretçinin tüm verisi (ilerleme, gönderim, quiz, sohbet, mülakat, ai_kullanim, hafıza/rapor dizinleri) silinir.
 *
 * <p>ShedLock YOK: tek JVM çalışır ve iş idempotenttir — ziyaretçi başına tüm adımlar TEK işlemde olduğundan
 * ortada çöken bir tur (ya da silinemeyen kişisel dizin) her şeyi geri alır, sonraki gece aynı ziyaretçi yeniden
 * denenir.
 */
@Service
@Profile("site")
public class ZiyaretciTemizlikService {

    static final long SAKLAMA_GUN = 180;
    /** Bir partide okunan en çok ziyaretçi (tech-debt #17: liste sınırsız büyümesin). */
    static final int PARTI = 500;
    private static final Logger log = LoggerFactory.getLogger(ZiyaretciTemizlikService.class);

    private final ZiyaretciRepo ziyaretciRepo;
    private final SifirlamaService sifirlamaService;
    private final KullanimRepo kullanimRepo;
    private final TransactionTemplate islem;
    private final Clock saat;
    private final int parti;
    /** Ziyaretçi başına dizin açılan kökler: öğrenci hafızası + rapor önbelleği (servislerle aynı çözüm). */
    private final List<Path> kisiselKokler;

    @Autowired
    public ZiyaretciTemizlikService(ZiyaretciRepo ziyaretciRepo, SifirlamaService sifirlamaService,
                                    KullanimRepo kullanimRepo, PlatformTransactionManager islemYoneticisi,
                                    AppProperties props) {
        this(ziyaretciRepo, sifirlamaService, kullanimRepo, islemYoneticisi, props, Clock.systemUTC(), PARTI);
    }

    ZiyaretciTemizlikService(ZiyaretciRepo ziyaretciRepo, SifirlamaService sifirlamaService,
                             KullanimRepo kullanimRepo, PlatformTransactionManager islemYoneticisi,
                             AppProperties props, Clock saat, int parti) {
        this.ziyaretciRepo = ziyaretciRepo;
        this.sifirlamaService = sifirlamaService;
        this.kullanimRepo = kullanimRepo;
        // Spring Data JDBC depoları sınıf düzeyinde readOnly=true: deleteBy… yalnız dıştaki OKUMA-YAZMA işleme
        // katılarak çalışır (PostgreSQL salt-okunur işlemde DELETE'i reddeder). Üç adım TEK işlemde.
        this.islem = new TransactionTemplate(islemYoneticisi);
        this.saat = saat;
        this.parti = parti;
        this.kisiselKokler = List.of(Path.of(props.hafizaDir()),
                Path.of(props.raporDir() == null ? RaporService.VARSAYILAN_RAPOR_DIR : props.raporDir()));
    }

    /**
     * Bir turun sonucu: {@code atlanan} = listeleme ile silme arasında geri dönen ziyaretçi (hata DEĞİL, verisi
     * korundu); {@code basarisiz} = silinemeyen, sonraki gece yeniden denenecek ziyaretçi.
     */
    record TemizlikSonucu(int silinen, int atlanan, int basarisiz) {
    }

    private enum SilmeSonucu { SILINDI, GERI_DONDU, BASARISIZ }

    /** Yaş denetimi tutmadı: işlemi geri aldırmak için fırlatılır, başarısızlık sayılmaz. */
    private static final class GeriDondu extends RuntimeException {
        GeriDondu() {
            super(null, null, false, false);
        }
    }

    /** Her gece 03:30 (sunucu saati). Hata zamanlayıcı iş parçacığını öldürmez; sonraki gece yeniden denenir. */
    @Scheduled(cron = "0 30 3 * * *")
    public void zamanlanmisTemizlik() {
        try {
            TemizlikSonucu s = temizleAyrintili();
            log.info("Ziyaretçi temizliği tamam: {} ziyaretçi silindi, {} geri döndüğü için atlandı, {} başarısız",
                    s.silinen(), s.atlanan(), s.basarisiz());
        } catch (RuntimeException e) {
            log.error("Ziyaretçi temizliği başarısız: {}", e.toString());
        }
    }

    /** @return verisi tamamen silinen ziyaretçi sayısı — bkz. {@link #temizleAyrintili()}. */
    public int temizle() {
        return temizleAyrintili().silinen();
    }

    /**
     * Eski ziyaretçileri en eskiden başlayarak {@code parti}'lik partiler hâlinde siler (tech-debt #17). Sayfalama
     * imleçlidir (keyset: önceki partinin son {@code (son_gorulme, id)}'sinden sonrası): silinemeyen ya da arada
     * geri dönen kimlik sayfayı kaydırmaz, o turda yeniden denenmez, arkasındaki eskileri de atlatmaz. İmleç her
     * partide kesin ilerlediği için döngü sonludur. Parti {@code parti}'den kısaysa (boş dahil) tur biter.
     */
    TemizlikSonucu temizleAyrintili() {
        Instant esik = saat.instant().minus(Duration.ofDays(SAKLAMA_GUN));
        int silinen = 0;
        int atlanan = 0;
        int basarisiz = 0;
        Instant sonZaman = null;
        String sonId = null;
        while (true) {
            List<ZiyaretciRepo.EskiZiyaretci> sayfa = ziyaretciRepo.eskiler(esik, parti, sonZaman, sonId);
            for (ZiyaretciRepo.EskiZiyaretci z : sayfa) {
                switch (sil(z.id(), esik)) {
                    case SILINDI -> silinen++;
                    case GERI_DONDU -> atlanan++;
                    case BASARISIZ -> basarisiz++;
                }
            }
            if (sayfa.size() < parti) {
                break;
            }
            ZiyaretciRepo.EskiZiyaretci son = sayfa.get(sayfa.size() - 1);
            sonZaman = son.sonGorulme();
            sonId = son.id();
        }
        if (basarisiz > 0) {
            log.warn("Ziyaretçi temizliği: {} silindi, {} başarısız (sonraki turda yeniden denenecek)", silinen,
                    basarisiz);
        }
        return new TemizlikSonucu(silinen, atlanan, basarisiz);
    }

    /**
     * Tek ziyaretçi, TEK okuma-yazma işlemi. Önce {@code ziyaretci} satırı YAŞ YENİDEN DENETLENEREK silinir
     * (son inceleme #2): {@code eskiler} ile bu an arasında geri dönen (son_gorulme tazelenen) ziyaretçide 0 satır
     * → işlem geri alınır, hiçbir veri ya da dosya silinmez; bu hata değildir (INFO, "atlanan"). Dosya silme
     * {@link SifirlamaService} içinde hata yutar (yerel "temiz başlangıç" için doğru); burada kişisel hafıza/rapor
     * dizini hâlâ duruyorsa istisna → işlem geri alınır, {@code ziyaretci} satırı kalır ve sonraki gece yeniden
     * denenir (tech-debt #16).
     */
    private SilmeSonucu sil(String kullaniciId, Instant esik) {
        try {
            islem.executeWithoutResult(durum -> {
                if (ziyaretciRepo.sil(kullaniciId, esik) == 0) {
                    throw new GeriDondu();
                }
                sifirlamaService.sifirla(kullaniciId);
                for (Path kok : kisiselKokler) {
                    if (Files.exists(KisiselDizin.coz(kok, kullaniciId))) {
                        throw new IllegalStateException("kişisel dizin silinemedi (" + kok.getFileName() + ")");
                    }
                }
                kullanimRepo.deleteByKullaniciId(kullaniciId);
            });
            return SilmeSonucu.SILINDI;
        } catch (GeriDondu e) {
            log.info("Ziyaretçi geri döndü, atlandı ({}…)", onEk(kullaniciId));
            return SilmeSonucu.GERI_DONDU;
        } catch (RuntimeException e) {
            log.warn("Ziyaretçi silinemedi ({}…): {}", onEk(kullaniciId), e.toString());
            return SilmeSonucu.BASARISIZ;
        }
    }

    private static String onEk(String kullaniciId) {
        return kullaniciId.substring(0, Math.min(6, kullaniciId.length()));
    }
}
