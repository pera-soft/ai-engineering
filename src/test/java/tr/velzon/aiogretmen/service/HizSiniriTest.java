package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/** Genel kayan pencere hız sınırı (Faz 10): anahtar başına pencerede en çok N izin; reddedilen yazılmaz. */
class HizSiniriTest {

    /** Elle ilerletilen saat. */
    static final class ElleSaat extends Clock {
        Instant simdi = Instant.parse("2026-10-22T10:00:00Z");

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return simdi;
        }
    }

    static final Duration SAAT = Duration.ofHours(1);

    @Test
    void saatteUcGonderimSonraRed() {
        ElleSaat saat = new ElleSaat();
        HizSiniri sinir = new HizSiniri(saat);
        assertThat(sinir.izinVer("SIFRE_SIFIRLAMA|a@ornek.com", 3, SAAT)).isTrue();
        assertThat(sinir.izinVer("SIFRE_SIFIRLAMA|a@ornek.com", 3, SAAT)).isTrue();
        assertThat(sinir.izinVer("SIFRE_SIFIRLAMA|a@ornek.com", 3, SAAT)).isTrue();
        assertThat(sinir.izinVer("SIFRE_SIFIRLAMA|a@ornek.com", 3, SAAT)).isFalse();

        // başka adres ve başka tür bağımsız
        assertThat(sinir.izinVer("SIFRE_SIFIRLAMA|b@ornek.com", 3, SAAT)).isTrue();
        assertThat(sinir.izinVer("DOGRULAMA|a@ornek.com", 3, SAAT)).isTrue();

        // pencere kayınca yeniden izin (reddedilen deneme pencereyi uzatmaz)
        saat.simdi = saat.simdi.plus(Duration.ofMinutes(61));
        assertThat(sinir.izinVer("SIFRE_SIFIRLAMA|a@ornek.com", 3, SAAT)).isTrue();
    }

    @Test
    void kayanPencere() {
        ElleSaat saat = new ElleSaat();
        HizSiniri sinir = new HizSiniri(saat);
        sinir.izinVer("DOGRULAMA|a@ornek.com", 3, SAAT);              // 10:00
        saat.simdi = saat.simdi.plus(Duration.ofMinutes(30));
        sinir.izinVer("DOGRULAMA|a@ornek.com", 3, SAAT);              // 10:30
        sinir.izinVer("DOGRULAMA|a@ornek.com", 3, SAAT);              // 10:30
        assertThat(sinir.izinVer("DOGRULAMA|a@ornek.com", 3, SAAT)).isFalse();
        saat.simdi = saat.simdi.plus(Duration.ofMinutes(31));        // 11:01 → 10:00'daki düştü
        assertThat(sinir.izinVer("DOGRULAMA|a@ornek.com", 3, SAAT)).isTrue();
        assertThat(sinir.izinVer("DOGRULAMA|a@ornek.com", 3, SAAT)).isFalse();
    }

    @Test
    void bosKuyrukHaritadanSilinir() {
        ElleSaat saat = new ElleSaat();
        HizSiniri sinir = new HizSiniri(saat);
        sinir.izinVer("a", 1, Duration.ofMinutes(1));
        sinir.izinVer("b", 1, Duration.ofMinutes(1));
        assertThat(sinir.anahtarSayisi()).isEqualTo(2);

        saat.simdi = saat.simdi.plus(Duration.ofMinutes(2));
        sinir.izinVer("a", 0, Duration.ofMinutes(1));   // enCok=0: kuyruk boşalır, yeni kayıt yok

        assertThat(sinir.anahtarSayisi()).as("boşalan kuyruk silinir").isEqualTo(1);
    }

    @Test
    void farkliPencereVeLimitAnahtarBasina() {
        HizSiniri sinir = new HizSiniri(new ElleSaat());
        assertThat(sinir.izinVer("x", 2, SAAT)).isTrue();
        assertThat(sinir.izinVer("x", 2, SAAT)).isTrue();
        assertThat(sinir.izinVer("x", 2, SAAT)).isFalse();
        assertThat(sinir.izinVer("y", 1, SAAT)).isTrue();
        assertThat(sinir.izinVer("y", 1, SAAT)).isFalse();
    }

    @Test
    void bayatAnahtarlarSupurulur() {
        ElleSaat saat = new ElleSaat();
        HizSiniri sinir = new HizSiniri(saat);
        for (int i = 0; i < 100; i++) {
            sinir.izinVer("eski" + i, 5, SAAT);   // bir daha hiç gelmeyecek anahtarlar
        }
        assertThat(sinir.anahtarSayisi()).isEqualTo(100);

        saat.simdi = saat.simdi.plus(Duration.ofMinutes(61));
        for (int i = 0; i < HizSiniri.SUPURME_ARALIGI; i++) {
            sinir.izinVer("canli", 1_000_000, SAAT);   // süpürmeyi tetikler; "canlı" taze kalır
        }

        assertThat(sinir.anahtarSayisi()).as("bayat 100 anahtar silindi, canlı kaldı").isEqualTo(1);
        assertThat(sinir.izinVer("canli", 1_000_000, SAAT)).isTrue();
    }
    /** Tech-debt #15: tüketmeden kontrol — izin varken sıfır, doluyken en eski kaydın pencereden çıkmasına kalan süre. */
    @Test
    void beklemeTuketmedenKalanSureyiVerir() {
        ElleSaat saat = new ElleSaat();
        HizSiniri sinir = new HizSiniri(saat);

        assertThat(sinir.bekleme("k", 3, SAAT)).isEqualTo(Duration.ZERO);
        assertThat(sinir.anahtarSayisi()).as("kontrol, bilinmeyen anahtar için kayıt açmaz").isZero();

        sinir.izinVer("k", 3, SAAT);                                   // 10:00
        saat.simdi = Instant.parse("2026-10-22T10:30:00Z");
        for (int i = 0; i < 5; i++) {
            assertThat(sinir.bekleme("k", 3, SAAT)).isEqualTo(Duration.ZERO);   // tüketmez
        }
        assertThat(sinir.izinVer("k", 3, SAAT)).isTrue();              // 10:30
        assertThat(sinir.izinVer("k", 3, SAAT)).isTrue();              // 10:30

        assertThat(sinir.bekleme("k", 3, SAAT)).isEqualTo(Duration.ofMinutes(30));
        saat.simdi = Instant.parse("2026-10-22T10:59:59.500Z");
        assertThat(sinir.bekleme("k", 3, SAAT)).isEqualTo(Duration.ofMillis(500));
        saat.simdi = Instant.parse("2026-10-22T11:00:00Z");
        assertThat(sinir.bekleme("k", 3, SAAT)).isEqualTo(Duration.ZERO);
    }
}
