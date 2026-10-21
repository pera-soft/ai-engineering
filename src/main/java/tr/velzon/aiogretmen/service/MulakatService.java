package tr.velzon.aiogretmen.service;

import tr.velzon.aiogretmen.config.MulakatEtkin;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.repo.MulakatCevapRepo;
import tr.velzon.aiogretmen.repo.MulakatRepo;
import tr.velzon.aiogretmen.types.BulunamadiException;
import tr.velzon.aiogretmen.types.MulakatAlanOzeti;
import tr.velzon.aiogretmen.types.MulakatCevapKaydi;
import tr.velzon.aiogretmen.types.MulakatCevapSonucu;
import tr.velzon.aiogretmen.types.MulakatKonu;
import tr.velzon.aiogretmen.types.MulakatKonuGorunumu;
import tr.velzon.aiogretmen.types.MulakatKonuOzeti;
import tr.velzon.aiogretmen.types.MulakatSoru;
import tr.velzon.aiogretmen.types.MulakatSoruBulgusu;
import tr.velzon.aiogretmen.types.MulakatSoruGorunumu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mülakat modülü (Faz 1, AI YOK): alan/konu listeleme, konu dersini + CEVAPSIZ soruları sunma,
 * cevap gönderimini SUNUCUDA değerlendirip kaydetme, hatalı soruları tekrar için toplama.
 *
 * <p><strong>Cevap-sızıntısı savunması:</strong> {@link #konuGetir} ve {@link #hataliSorularTekrar}
 * yalnız {@link MulakatSoruGorunumu} döner — sorunun kendi dogruIndex/aciklama'sı bu tipte YOKTUR.
 * Doğruluk hesabı ({@code secilenIndex == dogruIndex}) SADECE {@link #cevapGonder} içinde,
 * sunucudaki kanonik soru üzerinden yapılır; istemcinin "doğru" iddiası dikkate alınmaz.
 *
 * <p>Tek istisna {@code sonDeneme}'dir ve istisna değil, KULLANICININ KENDİ geçmişidir: hiç
 * cevaplanmamış soruda null; cevaplanmışta ise ancak kullanıcının cevap anında zaten gördüğü
 * bilgiyi geri verir. Tekrar listesinde bu bile GİZLİ kiptedir (bkz. {@link tr.velzon.aiogretmen.types.MulakatSonDeneme}).
 */
@MulakatEtkin
@Service
public class MulakatService {

    private static final List<Extension> MD_EKLENTILER = List.of(TablesExtension.create());
    private final Parser mdParser = Parser.builder().extensions(MD_EKLENTILER).build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder()
            .extensions(MD_EKLENTILER).escapeHtml(true).build();

    private final MulakatRepo mulakatRepo;
    private final MulakatCevapRepo cevapRepo;

    public MulakatService(MulakatRepo mulakatRepo, MulakatCevapRepo cevapRepo) {
        this.mulakatRepo = mulakatRepo;
        this.cevapRepo = cevapRepo;
    }

    /**
     * Alan ağacı — listeleme projeksiyonu (soru cevabı yok). Her konu için kullanıcının iki
     * sayısını DB'den ekler: kaç soruya CEVAP verdi ve kaçı DOĞRU. Böylece ilerleme sayfa
     * yenilemesinden sağ çıkar (yalnız SAYI; cevap sızmaz).
     */
    public List<MulakatAlanOzeti> alanlar(String kullaniciId) {
        Map<String, int[]> ilerleme = konuBasiIlerleme(kullaniciId);
        return mulakatRepo.alanlar().stream()
                .map(a -> new MulakatAlanOzeti(a.slug(), a.baslik(), a.aciklama(),
                        a.konular().stream()
                                .map(k -> {
                                    int[] s = ilerleme.getOrDefault(a.slug() + "/" + k.slug(), SIFIR);
                                    return new MulakatKonuOzeti(k.slug(), k.baslik(), k.kademe(),
                                            k.sira(), k.sorular().size(), s[0], s[1]);
                                })
                                .toList()))
                .toList();
    }

    private static final int[] SIFIR = {0, 0}; // hiç cevaplanmamış konu (paylaşılan, salt-okunur)

    /**
     * Her {@code "alan/konu"} için {@code [cevaplanan, dogru]}: kullanıcının kaç FARKLI sorusuna
     * cevap verdiği ve bunların kaçında SON denemenin doğru olduğu. Bir soru için yalnız EN YENİ
     * deneme belirleyicidir (yanlıştan doğruya dönen soru "cevaplanan"da bir kez sayılır).
     *
     * <p>İkisi ayrı ölçüdür: "başladım mı" (cevaplanan) ile "ustalaştım mı" (doğru) farklı
     * sorulardır — sol menü ilkini, {@code .done} ikincisini kullanır.
     */
    private Map<String, int[]> konuBasiIlerleme(String kullaniciId) {
        Map<String, int[]> sayac = new HashMap<>();
        for (MulakatCevapKaydi kayit : sonDenemeler(kullaniciId).values()) {
            int[] s = sayac.computeIfAbsent(kayit.alanSlug() + "/" + kayit.konuSlug(),
                    anahtar -> new int[2]);
            s[0]++;                       // cevaplanan (doğru/yanlış fark etmez)
            if (kayit.dogru()) s[1]++;    // bunlardan doğru olanlar
        }
        return sayac;
    }

    /**
     * soruId → o sorunun EN YENİ cevap kaydı. TEK dedup noktası: ilerleme sayımı, konu geri
     * yüklemesi ve tekrar/doğru listeleri hep buradan beslenir (üç yerde ayrı ayrı yazılan
     * "en yeni deneme" mantığının tek kaynağa indirgenmiş hâli — drift edemez).
     *
     * <p>{@link LinkedHashMap}: repo sırası (en yeni önce) korunur, listelerin sıralaması buna dayanır.
     */
    private Map<String, MulakatCevapKaydi> sonDenemeler(String kullaniciId) {
        Map<String, MulakatCevapKaydi> enYeni = new LinkedHashMap<>();
        for (MulakatCevapKaydi kayit : cevapRepo.findByKullaniciIdOrderByTarihDescIdDesc(kullaniciId)) {
            enYeni.putIfAbsent(kayit.soruId(), kayit); // ilk görülen = en yeni (sıralama repo'dan)
        }
        return enYeni;
    }

    /**
     * Konu dersi (HTML) + sorular. dersHtml = commonmark render + ```svg açma (KaynakService
     * deseni). Sorular cevapsızdır; YALNIZ kullanıcının daha önce cevapladıklarına kendi son
     * denemesi AÇIK kipte eklenir — böylece konu yeniden açıldığında işaretlediği şık, sonuç
     * ve açıklama geri gelir ("cevaplarım kayboldu" hatasının düzeltmesi). Hiç cevaplanmamış
     * soruda {@code sonDeneme} null'dır → doğru cevap sızmaz.
     *
     * @throws BulunamadiException alan/konu yoksa (web → 404)
     */
    public MulakatKonuGorunumu konuGetir(String kullaniciId, String alanSlug, String konuSlug) {
        MulakatKonu k = mulakatRepo.konu(alanSlug, konuSlug)
                .orElseThrow(() -> new BulunamadiException(
                        "Konu bulunamadı: " + alanSlug + "/" + konuSlug));
        String dersHtml = KaynakService.semalariAc(
                htmlRenderer.render(mdParser.parse(k.konuAnlatimi())));
        Map<String, MulakatCevapKaydi> denemeler = sonDenemeler(kullaniciId);
        return new MulakatKonuGorunumu(k.baslik(), k.kademe(), dersHtml,
                k.sorular().stream().map(s -> {
                    MulakatCevapKaydi d = denemeler.get(s.id());
                    return d == null ? s.gorunum() : s.gorunumAcik(d.secilenIndex(), d.dogru());
                }).toList());
    }

    /**
     * Cevabı SUNUCUDA değerlendirir, kaydeder ve sonucu (doğru şık + açıklama) döner.
     *
     * @throws BulunamadiException soru id'si yoksa (web → 404)
     * @throws IllegalArgumentException {@code secilenIndex} şık aralığı dışındaysa (web → 400)
     */
    public MulakatCevapSonucu cevapGonder(String kullaniciId, String soruId, int secilenIndex) {
        MulakatSoruBulgusu bulgu = mulakatRepo.soruBul(soruId)
                .orElseThrow(() -> new BulunamadiException("Soru bulunamadı: " + soruId));
        MulakatSoru soru = bulgu.soru();
        if (secilenIndex < 0 || secilenIndex >= soru.siklar().size()) {
            throw new IllegalArgumentException(
                    "Seçilen şık aralık dışı: " + secilenIndex + " (0.." + (soru.siklar().size() - 1) + ")");
        }
        boolean dogru = secilenIndex == soru.dogruIndex(); // SUNUCUDA — istemci iddiası değil
        cevapRepo.save(MulakatCevapKaydi.yeni(
                kullaniciId, soruId, bulgu.alanSlug(), bulgu.konuSlug(), secilenIndex, dogru));
        // Şıklar (metin + aiNot) GÖNDERİM SONRASI döner — cevap-sonrası "🤖" reveal için (sızıntı değil).
        return new MulakatCevapSonucu(dogru, soru.dogruIndex(), soru.aciklama(), soru.siklar());
    }

    /**
     * Son denemesi YANLIŞ olan soruların cevapsız görünümleri (soruId'e göre en yeni deneme
     * yanlışsa dahil). En yeni yanlış önce. Bulunamayan eski id'ler (içerik silinmiş) sessiz atlanır.
     */
    public List<MulakatSoruGorunumu> hataliSorularTekrar(String kullaniciId) {
        List<MulakatSoruGorunumu> sonuc = new ArrayList<>();
        for (MulakatCevapKaydi kayit : sonDenemeler(kullaniciId).values()) {
            if (kayit.dogru()) {
                continue; // en yeni deneme doğru → tekrara girmez
            }
            // GİZLİ kip: işaretlediği yanlış şık görünür, doğru cevap saklı → soru yeniden çözülebilir.
            mulakatRepo.soruBul(kayit.soruId())
                    .ifPresent(b -> sonuc.add(b.soru().gorunumGizli(kayit.secilenIndex(), false)));
        }
        return sonuc;
    }

    /**
     * Son denemesi DOĞRU olan soruların cevapsız görünümleri (soruId'e göre en yeni deneme
     * doğruysa dahil). En yeni doğru önce. "Doğru Cevaplar" takip bölümü — doğru cevapladıkça büyür.
     * {@link #hataliSorularTekrar}'ın birebir simetriği; aynı en-yeni-deneme dedup mantığı.
     */
    public List<MulakatSoruGorunumu> dogruCevaplananlar(String kullaniciId) {
        List<MulakatSoruGorunumu> sonuc = new ArrayList<>();
        for (MulakatCevapKaydi kayit : sonDenemeler(kullaniciId).values()) {
            if (!kayit.dogru()) {
                continue; // en yeni deneme yanlış → doğrular listesine girmez
            }
            // AÇIK kip: seçim zaten doğru şıkkın kendisi; açıklama pekiştirme için döner.
            mulakatRepo.soruBul(kayit.soruId())
                    .ifPresent(b -> sonuc.add(b.soru().gorunumAcik(kayit.secilenIndex(), true)));
        }
        return sonuc;
    }
}
