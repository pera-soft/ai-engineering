package tr.velzon.aiogretmen.repo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tr.velzon.aiogretmen.MulakatFixture;
import tr.velzon.aiogretmen.types.MulakatKonu;
import tr.velzon.aiogretmen.types.MulakatSoru;
import tr.velzon.aiogretmen.types.MulakatSoruBulgusu;
import tr.velzon.aiogretmen.types.MulakatZorluk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Parser doğrulaması BELİRLENİMCİ @TempDir fixture üzerinde yapılır (üretim içeriğine bağlanmaz).
 * Üretim {@code content/mulakat}'a yalnız tek bir "smoke" testi bağlıdır ({@link #uretimIcerigiYuklenir}).
 */
class MulakatRepoTest {

    private static MulakatRepo fixtureRepo(Path tmp) throws IOException {
        return new MulakatRepo(MulakatFixture.yaz(tmp));
    }

    // --- Fixture tabanlı doğrulama ---

    @Test
    void konulariSiraAlaninaGoreSiralar(@TempDir Path tmp) throws IOException {
        List<MulakatKonu> konular = fixtureRepo(tmp).alan(MulakatFixture.ALAN).orElseThrow().konular();
        // Dosya adları a-konu(sira2), b-konu(sira1) → sira'ya göre birinci ÖNCE gelmeli (dosya adı değil)
        assertThat(konular).extracting(MulakatKonu::slug)
                .containsExactly(MulakatFixture.KONU_BIR, MulakatFixture.KONU_IKI);
        assertThat(konular).extracting(MulakatKonu::sira).containsExactly(1, 2);
    }

    @Test
    void konuAnlatimiHamMarkdownVeSvgIcerir(@TempDir Path tmp) throws IOException {
        MulakatKonu konu = fixtureRepo(tmp).konu(MulakatFixture.ALAN, MulakatFixture.KONU_BIR).orElseThrow();
        assertThat(konu.kademe()).isEqualTo(MulakatZorluk.TEMEL);
        assertThat(konu.konuAnlatimi()).contains("```svg").contains("</svg>");
    }

    @Test
    void herSoruTamDortSikTekDoguVeAiNotIcerir(@TempDir Path tmp) throws IOException {
        List<MulakatSoru> hepsi = fixtureRepo(tmp).alanlar().stream()
                .flatMap(a -> a.konular().stream())
                .flatMap(k -> k.sorular().stream())
                .toList();
        assertThat(hepsi).extracting(MulakatSoru::id).containsExactlyInAnyOrder("f-1", "f-2", "f-3");
        assertThat(hepsi).allSatisfy(s -> {
            assertThat(s.siklar()).hasSize(4);
            assertThat(s.dogruIndex()).isBetween(0, 3);
            assertThat(s.soru()).isNotBlank();
            assertThat(s.aciklama()).isNotBlank();
            assertThat(s.siklar()).allSatisfy(sik -> {
                assertThat(sik.metin()).isNotBlank();
                assertThat(sik.aiNot()).isNotBlank();
            });
        });
    }

    @Test
    void soruIcerigiVeDogruIndexDogruParseEdilir(@TempDir Path tmp) throws IOException {
        MulakatSoruBulgusu b = fixtureRepo(tmp).soruBul(MulakatFixture.F1).orElseThrow();
        assertThat(b.alanSlug()).isEqualTo(MulakatFixture.ALAN);
        assertThat(b.konuSlug()).isEqualTo(MulakatFixture.KONU_BIR);
        MulakatSoru s = b.soru();
        assertThat(s.zorluk()).isEqualTo(MulakatZorluk.TEMEL);
        assertThat(s.dogruIndex()).isEqualTo(MulakatFixture.F1_DOGRU); // "## Doğru: B"
        assertThat(s.siklar().get(0).metin()).isEqualTo(MulakatFixture.F1_A);
        assertThat(s.siklar().get(1).metin()).isEqualTo(MulakatFixture.F1_B);
    }

    /** Fence-tracking'in VAR OLMA SEBEBİ: ```java bloğundaki sahte ## / # başlıkları sayılmamalı. */
    @Test
    void adversarialFenceIcindekiSahteBasliklarSayilmaz(@TempDir Path tmp) throws IOException {
        MulakatRepo repo = fixtureRepo(tmp);
        MulakatKonu konu = repo.konu(MulakatFixture.ALAN, MulakatFixture.KONU_BIR).orElseThrow();
        // f-1, f-2 yalnız — gömülü "# fake-soru | Temel" YENİ soru olarak sayılmadı
        assertThat(konu.sorular()).extracting(MulakatSoru::id).containsExactly("f-1", "f-2");
        assertThat(repo.soruBul("fake-soru")).isEmpty();
        MulakatSoru f2 = repo.soruBul("f-2").orElseThrow().soru();
        assertThat(f2.dogruIndex()).isEqualTo(2); // fence dışındaki gerçek "## Doğru: C" — sahte "D" değil
        assertThat(f2.siklar()).hasSize(4);        // fence içindeki "## A" fazladan şık üretmedi
        assertThat(f2.soru()).contains("```java").contains("fake-soru"); // fence içeriği Soru gövdesinde
    }

    @Test
    void olmayanSoruIdBosDoner(@TempDir Path tmp) throws IOException {
        assertThat(fixtureRepo(tmp).soruBul("yok-boyle-id")).isEmpty();
    }

    // --- Yükleme-anı fail-fast delikleri ---

    @Test
    void eksikSikliBozukDosyaIllegalState(@TempDir Path tmp) throws IOException {
        // A,B,C var; D yok → fail-fast
        String govde = """
                ---
                slug: k
                baslik: "K"
                kademe: Temel
                sira: 1
                etiketler: 1
                ---

                # q-1 | Temel
                ## Soru
                soru?
                ## A
                a
                🤖 n
                ## B
                b
                🤖 n
                ## C
                c
                🤖 n
                ## Doğru
                A
                ## Açıklama
                gerekçe
                """;
        assertThatThrownBy(() -> new MulakatRepo(MulakatFixture.yazBozuk(tmp, govde)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("şık");
    }

    @Test
    void bosSikMetniIllegalState(@TempDir Path tmp) throws IOException {
        String govde = """
                ---
                slug: k
                baslik: "K"
                kademe: Temel
                sira: 1
                etiketler: 1
                ---

                # q-1 | Temel
                ## Soru
                soru?
                ## A
                🤖 sadece not var, görünür metin yok
                ## B
                b
                🤖 n
                ## C
                c
                🤖 n
                ## D
                d
                🤖 n
                ## Doğru
                B
                ## Açıklama
                gerekçe
                """;
        assertThatThrownBy(() -> new MulakatRepo(MulakatFixture.yazBozuk(tmp, govde)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("boş metin");
    }

    @Test
    void tekrarBolumBasligiIllegalState(@TempDir Path tmp) throws IOException {
        String govde = """
                ---
                slug: k
                baslik: "K"
                kademe: Temel
                sira: 1
                etiketler: 1
                ---

                # q-1 | Temel
                ## Soru
                soru?
                ## A
                a
                🤖 n
                ## A
                tekrar eden A şıkkı
                🤖 n
                ## B
                b
                🤖 n
                ## C
                c
                🤖 n
                ## D
                d
                🤖 n
                ## Doğru
                B
                ## Açıklama
                gerekçe
                """;
        assertThatThrownBy(() -> new MulakatRepo(MulakatFixture.yazBozuk(tmp, govde)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("iki kez");
    }

    @Test
    void kapanmayanFenceIllegalState(@TempDir Path tmp) throws IOException {
        String govde = """
                ---
                slug: k
                baslik: "K"
                kademe: Temel
                sira: 1
                etiketler: 1
                ---

                # q-1 | Temel
                ## Soru
                soru:

                ```java
                kapanmayan kod bloğu
                ## A
                a
                ## Doğru
                B
                ## Açıklama
                gerekçe
                """;
        assertThatThrownBy(() -> new MulakatRepo(MulakatFixture.yazBozuk(tmp, govde)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Kapanmayan");
    }

    @Test
    void frontmatterKapanmazsaIllegalState(@TempDir Path tmp) throws IOException {
        String govde = """
                ---
                slug: k
                baslik: "K"
                kademe: Temel

                # q-1 | Temel
                ## Soru
                soru?
                """; // kapanış "---" YOK
        assertThatThrownBy(() -> new MulakatRepo(MulakatFixture.yazBozuk(tmp, govde)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("kapanış");
    }

    @Test
    void cokKarakterliDogruIllegalState(@TempDir Path tmp) throws IOException {
        // "## Doğru: AB" → "ABCD".indexOf("AB")==0 tuzağı; tek harf zorunlu, reddedilmeli.
        String govde = """
                ---
                slug: k
                baslik: "K"
                kademe: Temel
                sira: 1
                etiketler: 1
                ---

                # q-1 | Temel
                ## Soru
                soru?
                ## A
                a
                🤖 n
                ## B
                b
                🤖 n
                ## C
                c
                🤖 n
                ## D
                d
                🤖 n
                ## Doğru
                AB
                ## Açıklama
                gerekçe
                """;
        assertThatThrownBy(() -> new MulakatRepo(MulakatFixture.yazBozuk(tmp, govde)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Doğru");
    }

    @Test
    void basliktanOnceIcerikIllegalState(@TempDir Path tmp) throws IOException {
        // Soru başlığı ile ilk '## ' arasındaki içerik sessizce düşmemeli (fail-fast).
        String govde = """
                ---
                slug: k
                baslik: "K"
                kademe: Temel
                sira: 1
                etiketler: 1
                ---

                # q-1 | Temel
                kaçak metin (bölüm başlığı öncesi)
                ## Soru
                soru?
                ## A
                a
                🤖 n
                ## B
                b
                🤖 n
                ## C
                c
                🤖 n
                ## D
                d
                🤖 n
                ## Doğru
                A
                ## Açıklama
                gerekçe
                """;
        assertThatThrownBy(() -> new MulakatRepo(MulakatFixture.yazBozuk(tmp, govde)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("beklenmeyen içerik");
    }

    @Test
    void benzersizOlmayanIdIllegalState(@TempDir Path tmp) throws IOException {
        Files.createDirectories(tmp.resolve("01-alan"));
        Files.writeString(tmp.resolve("01-alan/alan.md"),
                "---\nslug: a\nbaslik: A\naciklama: t\n---\n");
        String konu = """
                ---
                slug: %s
                baslik: "%s"
                kademe: Temel
                sira: %d
                etiketler: 1
                ---

                # dup-1 | Temel
                ## Soru
                soru?
                ## A
                a
                🤖 n
                ## B
                b
                🤖 n
                ## C
                c
                🤖 n
                ## D
                d
                🤖 n
                ## Doğru
                A
                ## Açıklama
                gerekçe
                """;
        Files.writeString(tmp.resolve("01-alan/x.md"), konu.formatted("x", "X", 1));
        Files.writeString(tmp.resolve("01-alan/y.md"), konu.formatted("y", "Y", 2));

        assertThatThrownBy(() -> new MulakatRepo(tmp))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("benzersiz");
    }

    @Test
    void ayniKonuSluguIkiKezIllegalState(@TempDir Path tmp) throws IOException {
        Files.createDirectories(tmp.resolve("01-alan"));
        Files.writeString(tmp.resolve("01-alan/alan.md"),
                "---\nslug: a\nbaslik: A\naciklama: t\n---\n");
        String konu = """
                ---
                slug: ayni-slug
                baslik: "K"
                kademe: Temel
                sira: %d
                etiketler: 1
                ---

                # %s | Temel
                ## Soru
                soru?
                ## A
                a
                🤖 n
                ## B
                b
                🤖 n
                ## C
                c
                🤖 n
                ## D
                d
                🤖 n
                ## Doğru
                A
                ## Açıklama
                gerekçe
                """;
        Files.writeString(tmp.resolve("01-alan/birinci.md"), konu.formatted(1, "q-1"));
        Files.writeString(tmp.resolve("01-alan/ikinci.md"), konu.formatted(2, "q-2")); // aynı konu slug

        assertThatThrownBy(() -> new MulakatRepo(tmp))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("konu slug");
    }

    @Test
    void ayniAlanSluguIkiKezIllegalState(@TempDir Path tmp) throws IOException {
        String konu = """
                ---
                slug: k
                baslik: "K"
                kademe: Temel
                sira: 1
                etiketler: 1
                ---

                # %s | Temel
                ## Soru
                soru?
                ## A
                a
                🤖 n
                ## B
                b
                🤖 n
                ## C
                c
                🤖 n
                ## D
                d
                🤖 n
                ## Doğru
                A
                ## Açıklama
                gerekçe
                """;
        for (String dir : List.of("01-a", "02-a")) {
            Files.createDirectories(tmp.resolve(dir));
            Files.writeString(tmp.resolve(dir + "/alan.md"),
                    "---\nslug: ayni-alan\nbaslik: A\naciklama: t\n---\n"); // aynı alan slug
            Files.writeString(tmp.resolve(dir + "/konu.md"),
                    konu.formatted("q-" + dir)); // soru id'leri de farklı olsun
        }

        assertThatThrownBy(() -> new MulakatRepo(tmp))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Alan slug");
    }

    @Test
    void olmayanDizinNetHataVerir() {
        assertThatThrownBy(() -> new MulakatRepo(Path.of("boyle-bir-dizin-yok")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mülakat");
    }

    // --- Üretim içeriği: tek smoke testi ---

    @Test
    void uretimIcerigiYuklenir() {
        MulakatRepo repo = new MulakatRepo(Path.of("content/mulakat"));
        assertThat(repo.alanlar()).isNotEmpty();
        long soruSayisi = repo.alanlar().stream()
                .flatMap(a -> a.konular().stream())
                .flatMap(k -> k.sorular().stream())
                .count();
        assertThat(soruSayisi).isGreaterThan(0);
    }
}
