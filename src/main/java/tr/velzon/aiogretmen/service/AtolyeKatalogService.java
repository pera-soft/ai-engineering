package tr.velzon.aiogretmen.service;

import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.repo.AtolyeRepo;
import tr.velzon.aiogretmen.types.AtolyeKategori;

import java.util.List;

/**
 * Prompt Atölyesi kategori ağacının TOKEN-SIZ okuma seam'i (ADR-0009). {@link AtolyeService}
 * hem statik katalogu hem de LLM'li değerlendirmeyi taşıdığı için {@code @LlmKullanan}'dır;
 * {@code mcp/} paketi LLM-mühürü gereği ona bağımlı olamaz. mcp katalogu bu işaretsiz servisten
 * alır — böylece MCP hiçbir token üreten koda bağlanmaz (muhakeme bağlanan istemcide, ADR-0007).
 */
@Service
public class AtolyeKatalogService {

    private final AtolyeRepo atolyeRepo;

    public AtolyeKatalogService(AtolyeRepo atolyeRepo) {
        this.atolyeRepo = atolyeRepo;
    }

    public List<AtolyeKategori> kategoriler() {
        return atolyeRepo.kategoriler();
    }
}
