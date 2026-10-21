package tr.velzon.aiogretmen.ai;

import java.util.List;

/**
 * Claude'a tek çıkış noktası (ArchUnit zorlar: Spring AI sınıfları yalnız bu pakette).
 * Servis katmanı AI'a SADECE bu arayüz üzerinden erişir.
 */
public interface OgretmenAiClient {

    boolean aktif();

    /**
     * @param istek sistem prompt'u, geçmiş ve kullanıcı mesajı
     * @return model çıktısı + token sayıları (çağrı ai_kullanim'a loglanır)
     * @throws tr.velzon.aiogretmen.types.AiDevreDisiException anahtar yoksa
     */
    AiYanit cagir(AiIstek istek);

    /**
     * Görüntülü çağrı (vision) — kitap sayfası fotoğrafından metin çıkarma vb.
     * @throws tr.velzon.aiogretmen.types.AiDevreDisiException anahtar yoksa
     */
    AiYanit cagirGoruntulu(Islem islem, ModelTuru modelTuru, String sistemPrompt,
                           String kullaniciMesaji, byte[] goruntu, String mimeType, int maxTokens);

    /** İşlem türü — kullanım logunda (ai_kullanim.islem) görünür; maliyet raporunun etiketi (ADR-0003). */
    enum Islem { DEGERLENDIRME, SOHBET, QUIZ, KITAP, HAFIZA, ORNEK, ACIKLAMA, RAPOR, MULAKAT }

    record AiIstek(Islem islem, ModelTuru modelTuru, String sistemPrompt,
                   List<GecmisMesaj> gecmis, String kullaniciMesaji, int maxTokens) {

        public static AiIstek tekSeferlik(Islem islem, ModelTuru tur, String sistem,
                                          String kullaniciMesaji, int maxTokens) {
            return new AiIstek(islem, tur, sistem, List.of(), kullaniciMesaji, maxTokens);
        }
    }

    record GecmisMesaj(String rol, String icerik) {
    }

    record AiYanit(String metin, int inputTokens, int outputTokens, String model) {
    }

    enum ModelTuru { GUCLU, HIZLI }
}
