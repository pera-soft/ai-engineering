package tr.velzon.aiogretmen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tr.velzon.aiogretmen.repo.KullanimRepo;
import tr.velzon.aiogretmen.repo.ZiyaretciRepo;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Terk edilmiş ziyaretçi verisinin temizliği (Faz 10, ADR-0017): {@value #SAKLAMA_GUN} gündür gelmeyen
 * ziyaretçinin tüm verisi (ilerleme, gönderim, quiz, sohbet, mülakat, ai_kullanim, hafıza/rapor dizinleri) silinir.
 *
 * <p>ShedLock YOK: tek JVM çalışır ve iş idempotenttir — {@code ziyaretci} satırı EN SONDA silindiği için
 * ortada çöken bir tur sonraki gece aynı ziyaretçiyi yeniden dener.
 */
@Service
@Profile("site")
public class ZiyaretciTemizlikService {

    static final long SAKLAMA_GUN = 180;
    private static final Logger log = LoggerFactory.getLogger(ZiyaretciTemizlikService.class);

    private final ZiyaretciRepo ziyaretciRepo;
    private final SifirlamaService sifirlamaService;
    private final KullanimRepo kullanimRepo;
    private final TransactionTemplate islem;
    private final Clock saat;

    @Autowired
    public ZiyaretciTemizlikService(ZiyaretciRepo ziyaretciRepo, SifirlamaService sifirlamaService,
                                    KullanimRepo kullanimRepo, PlatformTransactionManager islemYoneticisi) {
        this(ziyaretciRepo, sifirlamaService, kullanimRepo, islemYoneticisi, Clock.systemUTC());
    }

    ZiyaretciTemizlikService(ZiyaretciRepo ziyaretciRepo, SifirlamaService sifirlamaService,
                             KullanimRepo kullanimRepo, PlatformTransactionManager islemYoneticisi,
                             Clock saat) {
        this.ziyaretciRepo = ziyaretciRepo;
        this.sifirlamaService = sifirlamaService;
        this.kullanimRepo = kullanimRepo;
        // Spring Data JDBC depoları sınıf düzeyinde readOnly=true: deleteBy… yalnız dıştaki OKUMA-YAZMA işleme
        // katılarak çalışır (PostgreSQL salt-okunur işlemde DELETE'i reddeder). Üç adım TEK işlemde.
        this.islem = new TransactionTemplate(islemYoneticisi);
        this.saat = saat;
    }

    /** Her gece 03:30 (sunucu saati). Hata zamanlayıcı iş parçacığını öldürmez; sonraki gece yeniden denenir. */
    @Scheduled(cron = "0 30 3 * * *")
    public void zamanlanmisTemizlik() {
        try {
            int silinen = temizle();
            log.info("Ziyaretçi temizliği tamam: {} ziyaretçi silindi", silinen);
        } catch (RuntimeException e) {
            log.error("Ziyaretçi temizliği başarısız: {}", e.toString());
        }
    }

    /** @return verisi tamamen silinen ziyaretçi sayısı (tek ziyaretçideki hata turu durdurmaz). */
    public int temizle() {
        Instant esik = saat.instant().minus(Duration.ofDays(SAKLAMA_GUN));
        int silinen = 0;
        int basarisiz = 0;
        for (String kullaniciId : ziyaretciRepo.eskiler(esik)) {
            try {
                islem.executeWithoutResult(durum -> {
                    sifirlamaService.sifirla(kullaniciId);
                    kullanimRepo.deleteByKullaniciId(kullaniciId);
                    ziyaretciRepo.sil(kullaniciId); // EN SONDA; hata olursa üçü birden geri alınır, sonraki tur dener
                });
                silinen++;
            } catch (RuntimeException e) {
                basarisiz++;
                log.warn("Ziyaretçi silinemedi ({}…): {}", onEk(kullaniciId), e.toString());
            }
        }
        if (basarisiz > 0) {
            log.warn("Ziyaretçi temizliği: {} silindi, {} başarısız (sonraki turda yeniden denenecek)", silinen,
                    basarisiz);
        }
        return silinen;
    }

    private static String onEk(String kullaniciId) {
        return kullaniciId.substring(0, Math.min(6, kullaniciId.length()));
    }
}
