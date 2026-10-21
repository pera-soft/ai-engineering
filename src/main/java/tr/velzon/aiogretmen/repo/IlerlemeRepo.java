package tr.velzon.aiogretmen.repo;

import org.springframework.data.repository.ListCrudRepository;
import tr.velzon.aiogretmen.types.GorevIlerlemeKaydi;

import java.util.List;
import java.util.Optional;

/**
 * Görev ilerleme deposu. TÜM sorgular {@code kullaniciId} ile filtrelenir (Faz 2a-3, ADR-0008):
 * bir kullanıcının kaydı başka kullanıcının sorgusunda GÖRÜNMEZ.
 */
public interface IlerlemeRepo extends ListCrudRepository<GorevIlerlemeKaydi, Long> {

    Optional<GorevIlerlemeKaydi> findByKullaniciIdAndHaftaAndGunAndGorevIndex(
            String kullaniciId, int hafta, int gun, int gorevIndex);

    List<GorevIlerlemeKaydi> findByKullaniciIdAndHafta(String kullaniciId, int hafta);

    /** Yalnız verilen kullanıcının ilerlemesini siler ("temiz başlangıç" izole). */
    void deleteByKullaniciId(String kullaniciId);
}
