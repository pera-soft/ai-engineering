package tr.velzon.aiogretmen.ai;

import org.junit.jupiter.api.Test;
import tr.velzon.aiogretmen.types.Degerlendirme;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Brief §13.3: değerlendirme JSON parse'ı için birim test ZORUNLU. */
class JsonCevapParserTest {

    final JsonCevapParser parser = new JsonCevapParser();

    record BasitCevap(String ozet, Integer puan) {}

    @Test
    void stringAlaninaDiziGelirseSatirlaraBirlestirir() {
        // Modeller "3-8 maddelik özet" isteyince alanı bazen JSON DİZİSİ yapıyor
        // (AI Engineering önsözü vakası) — string hedefine dizi gelirse birleştir.
        String hamMetin = "{\"ozet\": [\"- birinci madde\", \"- ikinci madde\"], \"puan\": 5}";

        Optional<BasitCevap> sonuc = parser.parse(hamMetin, BasitCevap.class);

        assertThat(sonuc).isPresent();
        assertThat(sonuc.get().ozet()).contains("birinci madde").contains("ikinci madde");
    }

    @Test
    void stringIcindeKacissizSatirSonunuTolereEder() {
        // Modeller uzun özetlerde string içine literal \n koyabiliyor (FoSA Bölüm 20 vakası);
        // katı JSON bunu reddeder — parser toleranslı olmalı.
        String hamMetin = "{\"ozet\": \"birinci satır\nikinci satır\", \"puan\": 5}";

        Optional<BasitCevap> sonuc = parser.parse(hamMetin, BasitCevap.class);

        assertThat(sonuc).isPresent();
        assertThat(sonuc.get().ozet()).contains("ikinci satır");
    }

    static final String GECERLI = """
            {"puan": 72, "ozet": "İyi bir başlangıç.",
             "dogru_noktalar": ["rol tanımı doğru"],
             "eksikler": ["sensör örneği yok"],
             "yanlis_anlamalar": [{"ne": "harness = model", "dogrusu": "harness model dışındaki her şey", "neden_onemli": "kavram temeli"}],
             "sonraki_adim": "Bir sensör örneği yaz.",
             "tekrar_onerisi": null}""";

    @Test
    void duzJsonParseEder() {
        Optional<Degerlendirme> d = parser.parse(GECERLI, Degerlendirme.class);

        assertThat(d).isPresent();
        assertThat(d.get().puan()).isEqualTo(72);
        assertThat(d.get().yanlisAnlamalar()).hasSize(1);
        assertThat(d.get().yanlisAnlamalar().get(0).dogrusu()).contains("model dışındaki");
        assertThat(d.get().tekrarOnerisi()).isNull();
    }

    @Test
    void markdownCitiIcindekiJsonuParseEder() {
        String citli = "```json\n" + GECERLI + "\n```";

        assertThat(parser.parse(citli, Degerlendirme.class))
                .isPresent()
                .hasValueSatisfying(d -> assertThat(d.puan()).isEqualTo(72));
    }

    @Test
    void etrafindaMetinOlanJsonuAyiklar() {
        String konuskan = "Elbette, işte değerlendirmen:\n" + GECERLI + "\nBaşarılar!";

        assertThat(parser.parse(konuskan, Degerlendirme.class))
                .isPresent()
                .hasValueSatisfying(d -> assertThat(d.ozet()).isEqualTo("İyi bir başlangıç."));
    }

    @Test
    void bozukJsonBosDoner() {
        assertThat(parser.parse("{bozuk: json,,,", Degerlendirme.class)).isEmpty();
        assertThat(parser.parse("hiç json yok burada", Degerlendirme.class)).isEmpty();
        assertThat(parser.parse("", Degerlendirme.class)).isEmpty();
        assertThat(parser.parse(null, Degerlendirme.class)).isEmpty();
    }

    @Test
    void tersEgikCizgiyleBitenStringDegeriParseEder() {
        // inceleme bulgusu: "b\\" (kaçışlı tek backslash + kapanış tırnağı) yanlışlıkla
        // "kaçışlı tırnak" sanılıyor ve dengeli kapanış bulunamıyordu
        String json = "{\"puan\": 40, \"ozet\": \"yol: C:\\\\temp\\\\\"}";

        assertThat(parser.parse(json, Degerlendirme.class))
                .isPresent()
                .hasValueSatisfying(d -> assertThat(d.ozet()).isEqualTo("yol: C:\\temp\\"));
    }

    @Test
    void icIcedeKacisliTirnakVeBackslashKarisimi() {
        String json = "{\"puan\": 10, \"ozet\": \"a \\\"b\\\" c\\\\\", \"dogru_noktalar\": []}";

        assertThat(parser.parse(json, Degerlendirme.class))
                .isPresent()
                .hasValueSatisfying(d -> assertThat(d.ozet()).isEqualTo("a \"b\" c\\"));
    }

    @Test
    void bilinmeyenAlanlariYokSayar() {
        String fazlaAlanli = "{\"puan\": 50, \"ozet\": \"x\", \"surpriz_alan\": true}";

        assertThat(parser.parse(fazlaAlanli, Degerlendirme.class))
                .isPresent()
                .hasValueSatisfying(d -> assertThat(d.puan()).isEqualTo(50));
    }
}
