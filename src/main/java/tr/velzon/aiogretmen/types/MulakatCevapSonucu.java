package tr.velzon.aiogretmen.types;

import java.util.List;

/**
 * Cevap gönderiminin sonucu — GÖNDERİMDEN SONRA döner (soru listelenirken değil). Doğruluk,
 * doğru şıkkın indeksi, yazılı açıklama ve şıkların tam listesi ({@code metin} + {@code aiNot})
 * içerir. Bu bilgi cevap verildikten SONRA sunulduğu için sızıntı değildir; {@code aiNot}'lar
 * istemcinin cevap-sonrası "🤖" reveal'ında (Faz 2) kullanılacaktır.
 */
public record MulakatCevapSonucu(boolean dogru, int dogruIndex, String aciklama,
                                 List<MulakatSik> siklar) {
}
