package tr.velzon.aiogretmen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Genel kayan pencere hız sınırı (Faz 10, ADR-0017; Faz 9c'deki e-posta sınırının genellemesi): anahtar başına,
 * verilen pencerede en çok {@code enCok} izin. Reddedilen deneme pencereye yazılmaz (sınırı uzatmaz).
 *
 * <p>Bellek içi ve tek örnek için; yeniden başlatma sayaçları sıfırlar (bilinçli kabul — nezaket/maliyet sınırı,
 * güvenlik sınırı değil). Bellek sızıntısına karşı: pencere dışına çıkıp boşalan kuyruk haritadan silinir.
 */
@Component
@Profile("site")
public class HizSiniri {

    private final Clock saat;
    /** Her bu kadar çağrıda bir bayat anahtarlar süpürülür (zamanlayıcı yok). */
    static final int SUPURME_ARALIGI = 256;

    private final Map<String, Deque<Instant>> kayitlar = new ConcurrentHashMap<>();
    private final AtomicLong cagriSayaci = new AtomicLong();
    private volatile Duration enBuyukPencere = Duration.ZERO;

    @Autowired
    public HizSiniri() {
        this(Clock.systemUTC());
    }

    HizSiniri(Clock saat) {
        this.saat = saat;
    }

    /** İzin varsa denemeyi kaydeder ve {@code true} döner; pencere doluysa {@code false}. */
    public boolean izinVer(String anahtar, int enCok, Duration pencere) {
        Instant simdi = saat.instant();
        Instant sinir = simdi.minus(pencere);
        if (pencere.compareTo(enBuyukPencere) > 0) {
            enBuyukPencere = pencere;
        }
        if (cagriSayaci.incrementAndGet() % SUPURME_ARALIGI == 0) {
            supur(simdi);
        }
        boolean[] izin = {false};
        kayitlar.compute(anahtar, (k, zamanlar) -> {
            Deque<Instant> z = zamanlar == null ? new ArrayDeque<>() : zamanlar;
            while (!z.isEmpty() && !z.peekFirst().isAfter(sinir)) {
                z.pollFirst();
            }
            if (z.size() < enCok) {
                z.addLast(simdi);
                izin[0] = true;
            }
            return z.isEmpty() ? null : z;
        });
        return izin[0];
    }

    /**
     * Bir daha hiç gelmeyen anahtarların (taze çerezli betikler, IPv6 dönüşümü) bellekte kalmaması için: en yeni
     * kaydı bilinen en büyük pencereden eski olan kuyruklar silinir. Eşzamanlı {@code izinVer}'le {@code compute}
     * üzerinden güvenli yarışır.
     */
    private void supur(Instant simdi) {
        Instant sinir = simdi.minus(enBuyukPencere);
        for (String anahtar : kayitlar.keySet()) {
            kayitlar.computeIfPresent(anahtar, (k, z) -> z.isEmpty() || !z.peekLast().isAfter(sinir) ? null : z);
        }
    }

    /** Haritada tutulan anahtar sayısı (test/gözlem). */
    public int anahtarSayisi() {
        return kayitlar.size();
    }
}
