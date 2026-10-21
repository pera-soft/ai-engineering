package tr.velzon.aiogretmen.ai;

import tr.velzon.aiogretmen.types.BenzerNot;
import tr.velzon.aiogretmen.types.NotParcasi;

import java.util.List;

/**
 * Anlam-eşleşmeli (embedding/RAG) not araması — tek çıkış noktası (ADR-0005).
 * Spring AI VectorStore/EmbeddingModel yalnız {@link SpringAiAnlamArama}'da yaşar;
 * service katmanı SADECE bu arayüzü görür (ArchUnit zorlar).
 *
 * <p>Belt-and-suspenders: anahtar yoksa / embedding hata verirse implementasyon
 * boş liste döndürür; çağıran (KitapService) hafta-etiketi eşleşmesine düşer.
 */
public interface AnlamArama {

    /** false → no-op (anahtar yok); çağıran fallback yapmalı. */
    boolean aktif();

    /** Notları embed'leyip depoya ekler ve diske kalıcılaştırır (index-at-creation, idempotent id). */
    void indexle(List<NotParcasi> parcalar);

    /** Sorguya en yakın k notu benzerlik sırasına göre döndürür; hata/boş depoda boş liste. */
    List<BenzerNot> benzerleri(String sorgu, int k);

    /** Depoyu boşaltır (yeniden indeksleme için). */
    void temizle();

    /** İndekslenmiş not sayısı (0 → @PostConstruct toplu indeksleme tetiklenir). */
    int sayac();
}
