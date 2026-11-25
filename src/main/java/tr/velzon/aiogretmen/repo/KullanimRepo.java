package tr.velzon.aiogretmen.repo;

import org.springframework.data.repository.ListCrudRepository;
import tr.velzon.aiogretmen.types.AiKullanim;

public interface KullanimRepo extends ListCrudRepository<AiKullanim, Long> {

    /** Yalnız verilen kullanıcının AI kullanım satırlarını siler (terk edilmiş ziyaretçi temizliği, ADR-0017). */
    void deleteByKullaniciId(String kullaniciId);
}
