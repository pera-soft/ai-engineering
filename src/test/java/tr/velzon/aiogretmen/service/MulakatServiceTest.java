package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import tr.velzon.aiogretmen.MulakatFixture;
import tr.velzon.aiogretmen.repo.MulakatCevapRepo;
import tr.velzon.aiogretmen.repo.MulakatRepo;
import tr.velzon.aiogretmen.types.MulakatCevapKaydi;
import tr.velzon.aiogretmen.types.MulakatCevapSonucu;
import tr.velzon.aiogretmen.types.MulakatKonuGorunumu;
import tr.velzon.aiogretmen.types.MulakatKonuOzeti;
import tr.velzon.aiogretmen.types.MulakatSik;
import tr.velzon.aiogretmen.types.MulakatSonDeneme;
import tr.velzon.aiogretmen.types.MulakatSoruGorunumu;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Gerçek H2 + schema.sql (mulakat_cevap) üzerinde; içerik BELİRLENİMCİ fixture'dan gelir
 * (üretim content'e bağlanmaz). Doğrulanan: doğruluğun SUNUCUDA hesaplanması, cevap-sonrası
 * aiNot reveal'ı, konuGetir çıktısının CEVAPSIZ olması, hatalıSorularTekrar sıralaması/izolasyonu.
 * Fixture doğru cevapları: f-1→B(1), f-2→C(2), f-3→A(0).
 */
@DataJdbcTest
class MulakatServiceTest {

    @Autowired
    MulakatCevapRepo cevapRepo;

    @TempDir
    Path tmp;

    MulakatService service;

    @BeforeEach
    void init() throws IOException {
        service = new MulakatService(new MulakatRepo(MulakatFixture.yaz(tmp)), cevapRepo);
    }

    @Test
    void dogrulukSunucudaHesaplanirVeKaydedilir() {
        MulakatCevapSonucu dogruSecim = service.cevapGonder("yerel", "f-1", 1); // B = doğru
        assertThat(dogruSecim.dogru()).isTrue();
        assertThat(dogruSecim.dogruIndex()).isEqualTo(1);
        assertThat(dogruSecim.aciklama()).isNotBlank();

        MulakatCevapSonucu yanlisSecim = service.cevapGonder("yerel", "f-1", 0); // A = yanlış
        assertThat(yanlisSecim.dogru()).isFalse();
        assertThat(yanlisSecim.dogruIndex()).isEqualTo(1);

        assertThat(cevapRepo.findByKullaniciIdOrderByTarihDescIdDesc("yerel")).hasSize(2);
    }

    @Test
    void cevapSonucuSikAiNotlariniIcerir() {
        // Cevap-sonrası "🤖" reveal için: sonuç şıkların metin + aiNot'unu taşır (sızıntı değil).
        MulakatCevapSonucu sonuc = service.cevapGonder("yerel", "f-1", 0);
        assertThat(sonuc.siklar()).hasSize(4);
        assertThat(sonuc.siklar()).extracting(MulakatSik::aiNot).allSatisfy(n -> assertThat(n).isNotBlank());
    }

    @Test
    void aralikDisiSecimIllegalArgument() {
        assertThatThrownBy(() -> service.cevapGonder("yerel", "f-1", 9))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.cevapGonder("yerel", "f-1", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void konuGetirCevapsizGorunumVeDersHtmlDoner() {
        MulakatKonuGorunumu konu = service.konuGetir("yerel", MulakatFixture.ALAN, MulakatFixture.KONU_BIR);

        assertThat(konu.baslik()).isNotBlank();
        // ```svg CANLI inline SVG'ye açılmış (semalariAc)
        assertThat(konu.dersHtml()).contains("<figure").contains("<svg");
        // Tip düzeyinde CEVAPSIZ: sorular yalnız MulakatSoruGorunumu (dogruIndex/aciklama alanı YOK)
        assertThat(konu.sorular()).extracting(MulakatSoruGorunumu::id).containsExactly("f-1", "f-2");
        assertThat(konu.sorular()).allSatisfy(s -> assertThat(s.secenekler()).hasSize(4));
        // Hiç cevaplanmamış soruda deneme YOK → doğru cevap hiçbir biçimde sızmaz
        assertThat(konu.sorular()).extracting(MulakatSoruGorunumu::sonDeneme).containsOnlyNulls();
    }

    @Test
    void konuGetirCevaplanmisSoruyuSonDenemeIleGeriYukler() {
        // "Cevaplarım kayboldu" hatası: kayıt DB'deydi ama istemciye hiç gitmiyordu.
        service.cevapGonder("yerel", "f-1", 0); // A = yanlış (doğru B/1)

        MulakatKonuGorunumu konu = service.konuGetir("yerel", MulakatFixture.ALAN, MulakatFixture.KONU_BIR);
        MulakatSonDeneme deneme = soru(konu, "f-1").sonDeneme();

        assertThat(deneme).isNotNull();
        assertThat(deneme.secilenIndex()).isZero();
        assertThat(deneme.dogru()).isFalse();
        // Konu görünümü AÇIK kip: doğru şık + açıklama geri gelir (kullanıcı bunu cevap anında gördü)
        assertThat(deneme.dogruIndex()).isEqualTo(MulakatFixture.F1_DOGRU);
        assertThat(deneme.aciklama()).isNotBlank();
        // Şık-başı 🤖 notları da döner → ekran cevap anındakiyle BİREBİR aynı boyanabilir
        assertThat(deneme.siklar()).hasSize(4)
                .extracting(MulakatSik::aiNot).allSatisfy(n -> assertThat(n).isNotBlank());
        // Cevaplanmamış komşu soru etkilenmez
        assertThat(soru(konu, "f-2").sonDeneme()).isNull();
    }

    @Test
    void konuGetirSonDenemeSadeceEnYenisiniVerir() {
        service.cevapGonder("yerel", "f-1", 0); // önce yanlış
        service.cevapGonder("yerel", "f-1", 1); // sonra doğru → belirleyici olan BU

        MulakatSonDeneme deneme = soru(service.konuGetir("yerel", MulakatFixture.ALAN,
                MulakatFixture.KONU_BIR), "f-1").sonDeneme();
        assertThat(deneme.secilenIndex()).isEqualTo(1);
        assertThat(deneme.dogru()).isTrue();
    }

    @Test
    void konuGetirSonDenemeKullaniciIzole() {
        service.cevapGonder("ali@example.com", "f-1", 0);

        assertThat(soru(service.konuGetir("veli@example.com", MulakatFixture.ALAN,
                MulakatFixture.KONU_BIR), "f-1").sonDeneme()).isNull();
    }

    @Test
    void tekrarListesiOncekiSecimiTasirAmaDogruCevabiGizler() {
        // "Hangisini yanlış yaptığım belli değil" — seçim gösterilir, doğru cevap GİZLİ kalır
        // ki soru yeniden çözülebilsin (listenin var oluş sebebi).
        service.cevapGonder("yerel", "f-1", 0);

        MulakatSonDeneme deneme = service.hataliSorularTekrar("yerel").get(0).sonDeneme();
        assertThat(deneme).isNotNull();
        assertThat(deneme.secilenIndex()).isZero();
        assertThat(deneme.dogru()).isFalse();
        assertThat(deneme.dogruIndex()).isNull();
        assertThat(deneme.aciklama()).isNull();
        assertThat(deneme.siklar()).isNull(); // şık notları da sızmaz (ipucu verirdi)
    }

    @Test
    void dogrularListesiSecimiVeAciklamayiTasir() {
        service.cevapGonder("yerel", "f-1", 1); // B = doğru

        MulakatSonDeneme deneme = service.dogruCevaplananlar("yerel").get(0).sonDeneme();
        assertThat(deneme).isNotNull();
        assertThat(deneme.secilenIndex()).isEqualTo(1);
        assertThat(deneme.dogru()).isTrue();
        assertThat(deneme.dogruIndex()).isEqualTo(MulakatFixture.F1_DOGRU);
        assertThat(deneme.aciklama()).isNotBlank();
    }

    private static MulakatSoruGorunumu soru(MulakatKonuGorunumu konu, String id) {
        return konu.sorular().stream().filter(s -> s.id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void hataliSorularTekrarEnYeniYanlisOnce() {
        Instant t = Instant.now();
        kaydet("yerel", "f-1", 0, false, t.minusSeconds(20));
        kaydet("yerel", "f-2", 1, false, t.minusSeconds(10));

        assertThat(service.hataliSorularTekrar("yerel"))
                .extracting(MulakatSoruGorunumu::id)
                .containsExactly("f-2", "f-1");
    }

    @Test
    void hataliSorularTekrarSonDenemesiDogruOlaniDislar() {
        Instant t = Instant.now();
        kaydet("yerel", "f-1", 0, false, t.minusSeconds(20)); // önce yanlış
        kaydet("yerel", "f-1", 1, true, t.minusSeconds(10));  // sonra doğru → dışla
        kaydet("yerel", "f-3", 0, false, t.minusSeconds(5));  // yanlış kalmış → dahil

        assertThat(service.hataliSorularTekrar("yerel"))
                .extracting(MulakatSoruGorunumu::id)
                .containsExactly("f-3");
    }

    @Test
    void hataliSorularTekrarBulunamayanEskiIdSessizAtlar() {
        kaydet("yerel", "silinmis-eski-id", 0, false, Instant.now());
        assertThat(service.hataliSorularTekrar("yerel")).isEmpty();
    }

    @Test
    void hataliSorularTekrarKullaniciIzole() {
        Instant t = Instant.now();
        kaydet("ali@example.com", "f-1", 0, false, t);
        kaydet("veli@example.com", "f-2", 0, false, t);

        assertThat(service.hataliSorularTekrar("ali@example.com"))
                .extracting(MulakatSoruGorunumu::id).containsExactly("f-1");
        assertThat(service.hataliSorularTekrar("veli@example.com"))
                .extracting(MulakatSoruGorunumu::id).containsExactly("f-2");
    }

    @Test
    void dogruSayisiSonDenemesiDogruOlanFarkliSorulariSayar() {
        Instant t = Instant.now();
        kaydet("yerel", "f-1", 0, false, t.minusSeconds(30)); // önce yanlış
        kaydet("yerel", "f-1", 1, true, t.minusSeconds(20));  // sonra DOĞRU → sayılır (son deneme belirleyici)
        kaydet("yerel", "f-2", 2, true, t.minusSeconds(10));  // DOĞRU → sayılır

        MulakatKonuOzeti birinci = konuOzeti("yerel", MulakatFixture.KONU_BIR);
        assertThat(birinci.soruSayisi()).isEqualTo(2);
        assertThat(birinci.dogruSayisi()).isEqualTo(2); // f-1 (son doğru) + f-2 → konu tamam (done)
    }

    @Test
    void cevaplananSayisiYanlisiDaSayar() {
        // "Başladığım konu" ölçüsü: doğruluk aranmaz — yanlış cevap da konuya başlandığını gösterir.
        Instant t = Instant.now();
        kaydet("yerel", "f-1", 0, false, t.minusSeconds(20)); // yanlış
        kaydet("yerel", "f-2", 2, true, t.minusSeconds(10));  // doğru

        MulakatKonuOzeti birinci = konuOzeti("yerel", MulakatFixture.KONU_BIR);
        assertThat(birinci.cevaplananSayisi()).isEqualTo(2); // ikisi de cevaplandı
        assertThat(birinci.dogruSayisi()).isEqualTo(1);      // ama yalnız biri doğru
    }

    @Test
    void cevaplananSayisiAyniSoruyuBirKezSayar() {
        // Aynı soruya iki deneme → "cevaplanan" 1'dir (soru sayısını aşamaz, %100'ü geçemez).
        Instant t = Instant.now();
        kaydet("yerel", "f-1", 0, false, t.minusSeconds(20));
        kaydet("yerel", "f-1", 1, true, t.minusSeconds(10));

        assertThat(konuOzeti("yerel", MulakatFixture.KONU_BIR).cevaplananSayisi()).isEqualTo(1);
    }

    @Test
    void cevaplanmamisKonudaHerIkiSayacSifir() {
        assertThat(konuOzeti("yerel", MulakatFixture.KONU_BIR))
                .satisfies(k -> {
                    assertThat(k.cevaplananSayisi()).isZero();
                    assertThat(k.dogruSayisi()).isZero();
                });
    }

    @Test
    void dogruSayisiSonDenemesiYanlisOlaniSaymaz() {
        Instant t = Instant.now();
        kaydet("yerel", "f-1", 1, true, t.minusSeconds(20));  // önce doğru
        kaydet("yerel", "f-1", 0, false, t.minusSeconds(10)); // sonra YANLIŞ → en yeni deneme yanlış, sayılmaz

        assertThat(konuOzeti("yerel", MulakatFixture.KONU_BIR).dogruSayisi()).isZero();
    }

    @Test
    void dogruSayisiKullaniciIzole() {
        Instant t = Instant.now();
        kaydet("ali@example.com", "f-1", 1, true, t);   // ali → birinci konusunda 1 doğru
        kaydet("veli@example.com", "f-2", 2, true, t);  // veli → birinci konusunda 1 doğru (farklı soru)

        assertThat(konuOzeti("ali@example.com", MulakatFixture.KONU_BIR).dogruSayisi()).isEqualTo(1);
        assertThat(konuOzeti("veli@example.com", MulakatFixture.KONU_BIR).dogruSayisi()).isEqualTo(1);
    }

    private MulakatKonuOzeti konuOzeti(String kullanici, String konuSlug) {
        return service.alanlar(kullanici).stream()
                .flatMap(a -> a.konular().stream())
                .filter(k -> k.slug().equals(konuSlug))
                .findFirst().orElseThrow();
    }

    private void kaydet(String kullanici, String soruId, int index, boolean dogru, Instant tarih) {
        cevapRepo.save(new MulakatCevapKaydi(null, kullanici, soruId, MulakatFixture.ALAN,
                MulakatFixture.KONU_BIR, index, dogru, tarih));
    }
}
