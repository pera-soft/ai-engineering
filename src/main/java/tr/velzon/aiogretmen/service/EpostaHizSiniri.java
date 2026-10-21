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

/**
 * Hesap e-postası hız sınırı (Faz 9c, ADR-0013 §7): aynı adrese, tür başına, kayan 1 saatlik pencerede
 * en çok {@value #EN_COK} e-posta — "şifremi unuttum"/"yeniden gönder" ile posta seli yapılamasın.
 *
 * <p>Bellek içi ve tek örnek için: çağıran yalnız VAR OLAN hesaplar için sorar (boyut hesap sayısıyla
 * sınırlı); yeniden başlatma sayacı sıfırlar (bilinçli kabul — güvenlik sınırı değil, nezaket/maliyet
 * sınırı). Reddedilen deneme pencereye yazılmaz (sınırı uzatmaz).
 */
@Component
@Profile("site")
public class EpostaHizSiniri {

    public static final int EN_COK = 3;
    public static final Duration PENCERE = Duration.ofHours(1);

    private final Clock saat;
    private final Map<String, Deque<Instant>> gonderimler = new ConcurrentHashMap<>();

    @Autowired
    public EpostaHizSiniri() {
        this(Clock.systemUTC());
    }

    EpostaHizSiniri(Clock saat) {
        this.saat = saat;
    }

    /** İzin varsa gönderimi kaydeder ve {@code true} döner; pencere doluysa {@code false}. */
    public boolean izinVer(String tur, String email) {
        Instant simdi = saat.instant();
        Instant sinir = simdi.minus(PENCERE);
        boolean[] izin = {false};
        gonderimler.compute(tur + '|' + email, (anahtar, zamanlar) -> {
            Deque<Instant> z = zamanlar == null ? new ArrayDeque<>() : zamanlar;
            while (!z.isEmpty() && !z.peekFirst().isAfter(sinir)) {
                z.pollFirst();
            }
            if (z.size() < EN_COK) {
                z.addLast(simdi);
                izin[0] = true;
            }
            return z;
        });
        return izin[0];
    }
}
