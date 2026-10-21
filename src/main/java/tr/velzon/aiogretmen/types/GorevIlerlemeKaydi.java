package tr.velzon.aiogretmen.types;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Bir görevin bir kullanıcı için tamamlanma durumu. {@code kullaniciId} izolasyon anahtarıdır
 * (Faz 2a-3, ADR-0008): web profilinde sabit {@code "yerel"}, mcp-http profilinde JWT email/sub.
 */
@Table("GOREV_ILERLEME")
public record GorevIlerlemeKaydi(@Id Long id, String kullaniciId, int hafta, int gun, int gorevIndex,
                                 boolean tamamlandi, Instant tarih) {

    public static GorevIlerlemeKaydi yeni(String kullaniciId, int hafta, int gun, int gorevIndex, boolean tamamlandi) {
        return new GorevIlerlemeKaydi(null, kullaniciId, hafta, gun, gorevIndex, tamamlandi, Instant.now());
    }

    public GorevIlerlemeKaydi durumla(boolean yeniDurum) {
        return new GorevIlerlemeKaydi(id, kullaniciId, hafta, gun, gorevIndex, yeniDurum, Instant.now());
    }
}
