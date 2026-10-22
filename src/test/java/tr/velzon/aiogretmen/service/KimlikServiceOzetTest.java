package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * {@code tools/sifre-baglantisi.sh} (ADR-0015) token'ı veritabanına {@link KimlikService#ozet} ile AYNI biçimde
 * yazmalı: SHA-256(UTF-8 baytları), küçük harf hex, 64 karakter. Bu test sözleşmeyi sabitler ve (kabuk
 * araçları varsa) betikteki boru hattının — {@code printf %s TOKEN | sha256sum} ya da {@code shasum -a 256} —
 * aynı sonucu verdiğini çalıştırarak doğrular. Gerçek PostgreSQL'e karşı uçtan uca deneme:
 * {@code SitePostgresEntegrasyonTest#isletmenBetigiSifirlamaBaglantisiUretir} (opt-in).
 */
class KimlikServiceOzetTest {

    /** base64url, dolgusuz, 43 karakter — betiğin ürettiği biçimde sabit örnek. */
    private static final String TOKEN = "q2-Zx_9aB3cD4eF5gH6iJ7kL8mN9oP0qR1sT2uV3wX4";

    @Test
    void bilinenSha256VektoruKucukHarfHex() {
        assertThat(KimlikService.ozet("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        assertThat(KimlikService.ozet(TOKEN)).matches("[0-9a-f]{64}");
    }

    @Test
    void betiktekiKabukOzetiJavaIleAyni() throws Exception {
        String komut = komutVarMi("sha256sum") ? "sha256sum" : komutVarMi("shasum") ? "shasum -a 256" : null;
        assumeTrue(komut != null, "sha256sum/shasum yok");
        Process p = new ProcessBuilder("bash", "-c", "printf '%s' \"$1\" | " + komut + " | cut -d' ' -f1", "_", TOKEN)
                .redirectErrorStream(true).start();
        String kabuk = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        assertThat(p.waitFor()).isZero();
        assertThat(kabuk).isEqualTo(KimlikService.ozet(TOKEN));
    }

    @Test
    void betikAyniBoruHattiniKullanir() throws IOException {
        // betik değişip Java'dan ayrışmasın: kullandığı özet boru hattı ve token biçimi burada sabit
        String betik = Files.readString(Path.of("tools/sifre-baglantisi.sh"), StandardCharsets.UTF_8);
        assertThat(betik).contains("printf '%s' \"$token\" | sha256sum | cut -d' ' -f1")
                .contains("printf '%s' \"$token\" | shasum -a 256 | cut -d' ' -f1")
                .contains("head -c 32 /dev/urandom | base64 | tr -d '\\n=' | tr '+/' '-_'")
                .contains("'SIFRE_SIFIRLAMA'")
                .contains("INTERVAL '1 hour'");
    }

    private static boolean komutVarMi(String ad) throws Exception {
        return new ProcessBuilder("bash", "-c", "command -v " + ad).start().waitFor() == 0;
    }
}
