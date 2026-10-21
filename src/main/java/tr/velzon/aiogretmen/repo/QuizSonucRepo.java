package tr.velzon.aiogretmen.repo;

import org.springframework.data.repository.ListCrudRepository;
import tr.velzon.aiogretmen.types.QuizSonucKaydi;

import java.util.List;

/** Quiz sonuç deposu — TÜM sorgular {@code kullaniciId} ile filtrelenir (Faz 2a-3, ADR-0008). */
public interface QuizSonucRepo extends ListCrudRepository<QuizSonucKaydi, Long> {

    List<QuizSonucKaydi> findByKullaniciIdAndHaftaOrderByTarihDesc(String kullaniciId, int hafta);

    /** Yalnız verilen kullanıcının quiz sonuçlarını siler. */
    void deleteByKullaniciId(String kullaniciId);
}
