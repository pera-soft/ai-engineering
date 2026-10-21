package tr.velzon.aiogretmen.repo;

import org.springframework.data.repository.ListCrudRepository;
import tr.velzon.aiogretmen.types.SohbetMesaji;

import java.util.List;

/** Sohbet deposu — TÜM sorgular {@code kullaniciId} ile filtrelenir (Faz 2a-3, ADR-0008). */
public interface SohbetRepo extends ListCrudRepository<SohbetMesaji, Long> {

    List<SohbetMesaji> findByKullaniciIdAndHaftaOrderByTarihAsc(String kullaniciId, int hafta);

    List<SohbetMesaji> findTop20ByKullaniciIdAndHaftaOrderByTarihDesc(String kullaniciId, int hafta);

    long countByKullaniciIdAndHaftaAndRol(String kullaniciId, int hafta, String rol);

    /** Yalnız verilen kullanıcının sohbetini siler. */
    void deleteByKullaniciId(String kullaniciId);
}
