package tr.velzon.aiogretmen.repo;

import org.springframework.data.repository.ListCrudRepository;
import tr.velzon.aiogretmen.types.MulakatCevapKaydi;

import java.util.List;

/** Mülakat cevap deposu — TÜM sorgular {@code kullaniciId} ile filtrelenir (Faz 2a-3, ADR-0008). */
public interface MulakatCevapRepo extends ListCrudRepository<MulakatCevapKaydi, Long> {

    /** En yeni önce; aynı tick'te iki cevap olursa {@code id} (IDENTITY) kesin tie-break. */
    List<MulakatCevapKaydi> findByKullaniciIdOrderByTarihDescIdDesc(String kullaniciId);

    /** Yalnız verilen kullanıcının cevaplarını siler ("temiz başlangıç" izole). */
    void deleteByKullaniciId(String kullaniciId);
}
