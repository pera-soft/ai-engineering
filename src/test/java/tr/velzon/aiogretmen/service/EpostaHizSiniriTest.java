package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/** Faz 9c (ADR-0013 §7): aynı adrese tür başına saatte en çok 3 e-posta. */
class EpostaHizSiniriTest {

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

    @Test
    void saatteUcGonderimSonraRed() {
        ElleSaat saat = new ElleSaat();
        EpostaHizSiniri sinir = new EpostaHizSiniri(saat);
        assertThat(sinir.izinVer("SIFRE_SIFIRLAMA", "a@pera.software")).isTrue();
        assertThat(sinir.izinVer("SIFRE_SIFIRLAMA", "a@pera.software")).isTrue();
        assertThat(sinir.izinVer("SIFRE_SIFIRLAMA", "a@pera.software")).isTrue();
        assertThat(sinir.izinVer("SIFRE_SIFIRLAMA", "a@pera.software")).isFalse();

        // başka adres ve başka tür bağımsız
        assertThat(sinir.izinVer("SIFRE_SIFIRLAMA", "b@pera.software")).isTrue();
        assertThat(sinir.izinVer("DOGRULAMA", "a@pera.software")).isTrue();

        // pencere kayınca yeniden izin (reddedilen deneme pencereyi uzatmaz)
        saat.simdi = saat.simdi.plus(Duration.ofMinutes(61));
        assertThat(sinir.izinVer("SIFRE_SIFIRLAMA", "a@pera.software")).isTrue();
    }

    @Test
    void kayanPencere() {
        ElleSaat saat = new ElleSaat();
        EpostaHizSiniri sinir = new EpostaHizSiniri(saat);
        sinir.izinVer("DOGRULAMA", "a@pera.software");              // 10:00
        saat.simdi = saat.simdi.plus(Duration.ofMinutes(30));
        sinir.izinVer("DOGRULAMA", "a@pera.software");              // 10:30
        sinir.izinVer("DOGRULAMA", "a@pera.software");              // 10:30
        assertThat(sinir.izinVer("DOGRULAMA", "a@pera.software")).isFalse();
        saat.simdi = saat.simdi.plus(Duration.ofMinutes(31));        // 11:01 → 10:00'daki düştü
        assertThat(sinir.izinVer("DOGRULAMA", "a@pera.software")).isTrue();
        assertThat(sinir.izinVer("DOGRULAMA", "a@pera.software")).isFalse();
    }
}
