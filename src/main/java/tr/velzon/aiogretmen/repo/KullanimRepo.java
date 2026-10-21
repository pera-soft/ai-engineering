package tr.velzon.aiogretmen.repo;

import org.springframework.data.repository.ListCrudRepository;
import tr.velzon.aiogretmen.types.AiKullanim;

public interface KullanimRepo extends ListCrudRepository<AiKullanim, Long> {
}
