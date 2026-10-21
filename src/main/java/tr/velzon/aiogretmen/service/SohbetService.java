package tr.velzon.aiogretmen.service;

import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.config.LlmKullanan;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.GecmisMesaj;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.ModelTuru;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.Islem;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.config.KullaniciBaglami;
import tr.velzon.aiogretmen.repo.SohbetRepo;
import tr.velzon.aiogretmen.types.SohbetMesaji;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Sokratik sohbet (brief 7.3): hafta bazlı geçmiş DB'de; son 20 mesaj bağlama eklenir.
 * Her 4 kullanıcı mesajında bir öğrenci hafızası güncellenir (kullanıcı eklemesi).
 */
@Service
@LlmKullanan
public class SohbetService {

    static final int MAX_TOKENS = 1500;
    static final int HAFIZA_GUNCELLEME_ARALIGI = 4;

    private final OgretmenAiClient aiClient;
    private final BaglamServisi baglamServisi;
    private final PromptFactory promptFactory;
    private final SohbetRepo sohbetRepo;
    private final HafizaService hafizaService;
    private final KullaniciBaglami kullaniciBaglami;

    public SohbetService(OgretmenAiClient aiClient, BaglamServisi baglamServisi,
                         PromptFactory promptFactory, SohbetRepo sohbetRepo,
                         HafizaService hafizaService, KullaniciBaglami kullaniciBaglami) {
        this.aiClient = aiClient;
        this.baglamServisi = baglamServisi;
        this.promptFactory = promptFactory;
        this.sohbetRepo = sohbetRepo;
        this.hafizaService = hafizaService;
        this.kullaniciBaglami = kullaniciBaglami;
    }

    public SohbetMesaji mesajGonder(int hafta, Integer gun, String mesaj) {
        String kullaniciId = kullaniciBaglami.aktifKullanici();
        List<GecmisMesaj> gecmis = songecmis(kullaniciId, hafta);
        // Anlam-eşleşmeli kitap bağlamı öğrencinin son mesajına göre getirilir (ADR-0005 HİBRİT).
        String sistem = baglamServisi.sistemBaglami(hafta, gun, mesaj)
                + "\n\n" + promptFactory.doldur("sokratik-sohbet", Map.of());

        var yanit = aiClient.cagir(new AiIstek(Islem.SOHBET, ModelTuru.GUCLU, sistem, gecmis, mesaj, MAX_TOKENS));

        // AI başarısız olursa (503/hata) yarım konuşma kalmasın diye kayıt ÇAĞRIDAN SONRA yapılır
        sohbetRepo.save(SohbetMesaji.kullanici(kullaniciId, hafta, mesaj));
        SohbetMesaji asistanMesaji = sohbetRepo.save(SohbetMesaji.asistan(kullaniciId, hafta, yanit.metin()));

        hafizaGerekirseGuncelle(kullaniciId, hafta, mesaj, yanit.metin());
        return asistanMesaji;
    }

    public List<SohbetMesaji> gecmis(int hafta) {
        return sohbetRepo.findByKullaniciIdAndHaftaOrderByTarihAsc(kullaniciBaglami.aktifKullanici(), hafta);
    }

    /** Son 20 mesaj, eskiden yeniye (brief 7.3). */
    private List<GecmisMesaj> songecmis(String kullaniciId, int hafta) {
        return sohbetRepo.findTop20ByKullaniciIdAndHaftaOrderByTarihDesc(kullaniciId, hafta).stream()
                .sorted(Comparator.comparing(SohbetMesaji::tarih))
                .map(m -> new GecmisMesaj(m.rol(), m.icerik()))
                .toList();
    }

    private void hafizaGerekirseGuncelle(String kullaniciId, int hafta, String kullaniciMesaji, String asistanYaniti) {
        long kullaniciMesajSayisi = sohbetRepo.countByKullaniciIdAndHaftaAndRol(kullaniciId, hafta, "USER");
        if (kullaniciMesajSayisi % HAFIZA_GUNCELLEME_ARALIGI == 0) {
            hafizaService.guncelle(hafta, "SOHBET",
                    "Öğrenci: %s\n\nÖğretmen: %s".formatted(kullaniciMesaji, asistanYaniti));
        }
    }
}
