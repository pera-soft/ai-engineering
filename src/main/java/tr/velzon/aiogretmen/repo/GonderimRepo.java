package tr.velzon.aiogretmen.repo;

import org.springframework.data.repository.ListCrudRepository;
import tr.velzon.aiogretmen.types.Gonderim;

import java.util.List;

/** Gönderim deposu — TÜM sorgular {@code kullaniciId} ile filtrelenir (Faz 2a-3, ADR-0008). */
public interface GonderimRepo extends ListCrudRepository<Gonderim, Long> {

    List<Gonderim> findByKullaniciIdAndHaftaOrderByTarihDesc(String kullaniciId, int hafta);

    /** Yalnız verilen kullanıcının gönderimlerini siler. */
    void deleteByKullaniciId(String kullaniciId);
}
