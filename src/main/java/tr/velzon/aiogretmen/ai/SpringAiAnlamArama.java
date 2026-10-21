package tr.velzon.aiogretmen.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import tr.velzon.aiogretmen.types.BenzerNot;
import tr.velzon.aiogretmen.types.NotParcasi;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring AI SimpleVectorStore (bellek-içi, brute-force cosine) + OpenAI-uyumlu embedding
 * sarmalayıcısı (ADR-0005). JSON'a save/load ile kalıcılaşır; ~133 not için flat cosine
 * yeterli (DDIA B4: HNSW milyonlar için). Embedding sağlayıcısı sohbetle aynı: OpenRouter.
 *
 * <p>Indekslenen id'ler için KENDİ bağımsız doğruluk kaynağını ({@link #idKumesi}) tutar ve
 * bir yan-dosyaya (.idler) kalıcılaştırır — böylece {@code sayac()}/{@code temizle()} Spring AI'nin
 * iç {@code store} alanına REFLECTION ile bakmaz (eski borç #8 kapandı; sağlayıcı sürümüne
 * kancasız). Sidecar yoksa (ör. eski depo) açılışta {@code sayac()==0} olur ve KitapService
 * bir kez yeniden indeksleyip sidecar'ı yazar (tek seferlik).
 */
public class SpringAiAnlamArama implements AnlamArama {

    private static final Logger log = LoggerFactory.getLogger(SpringAiAnlamArama.class);

    private final SimpleVectorStore vektorDepo;
    private final File dosya;
    /** İndekslenen doküman id'leri — otorite BİZ'iz, Spring AI iç map'i değil. */
    private final Set<String> idKumesi = ConcurrentHashMap.newKeySet();

    /** Kurulumda JSON + id sidecar'ı varsa yükler (açılışta load — brief/ADR-0005). */
    public SpringAiAnlamArama(SimpleVectorStore vektorDepo, File dosya) {
        this.vektorDepo = vektorDepo;
        this.dosya = dosya;
        if (dosya != null && dosya.isFile() && dosya.length() > 0) {
            try {
                vektorDepo.load(dosya);
                idKumesiniYukle();
                log.info("Anlam deposu yüklendi: {} not ({})", idKumesi.size(), dosya);
            } catch (Exception e) {
                log.warn("Anlam deposu yüklenemedi ({}) — boş başlanıyor: {}", dosya, e.getMessage());
            }
        }
    }

    @Override
    public boolean aktif() {
        return true;
    }

    @Override
    public synchronized void indexle(List<NotParcasi> parcalar) {
        if (parcalar == null || parcalar.isEmpty()) {
            return;
        }
        try {
            List<Document> belgeler = new ArrayList<>(parcalar.size());
            for (NotParcasi p : parcalar) {
                belgeler.add(Document.builder()
                        .id(p.id())
                        .text(p.metin())
                        .metadata("kaynak", p.kaynak() == null ? "" : p.kaynak())
                        .metadata("tarih", p.tarih() == null ? "" : p.tarih())
                        .build());
            }
            vektorDepo.add(belgeler); // aynı id üzerine yazılır → idempotent
            parcalar.forEach(p -> idKumesi.add(p.id()));
            kaydet();
        } catch (Exception e) {
            // belt-and-suspenders: indeksleme hatası not eklemeyi bozmasın
            log.warn("Anlam indeksleme başarısız ({} parça): {}", parcalar.size(), e.getMessage());
        }
    }

    @Override
    public List<BenzerNot> benzerleri(String sorgu, int k) {
        if (sorgu == null || sorgu.isBlank() || sayac() == 0) {
            return List.of();
        }
        try {
            SearchRequest istek = SearchRequest.builder()
                    .query(sorgu)
                    .topK(k)
                    .similarityThresholdAll()
                    .build();
            return vektorDepo.similaritySearch(istek).stream()
                    .map(d -> new BenzerNot(d.getText(), d.getScore() == null ? 0.0 : d.getScore()))
                    .toList();
        } catch (Exception e) {
            // embedding/ağ hatası → çağıran hafta-etiketi eşleşmesine düşer
            log.warn("Anlam araması başarısız — fallback: {}", e.getMessage());
            return List.of();
        }
    }

    /** synchronized: kaydet()/indexle() ile yarışmasın; id'ler bizde olduğu için silme artık TAM. */
    @Override
    public synchronized void temizle() {
        if (!idKumesi.isEmpty()) {
            vektorDepo.delete(new ArrayList<>(idKumesi)); // reflection YOK — kendi id kümemiz
        }
        idKumesi.clear();
        if (dosya != null) {
            dosya.delete();
            sidecarYolu().toFile().delete();
        }
    }

    @Override
    public int sayac() {
        return idKumesi.size();
    }

    // --- iç işler ---

    /**
     * id sidecar'ını + vektor.json'ı atomik yazar (idempotent-redundancy). SIRA ÖNEMLİ (inceleme UYARI):
     * ÖNCE .idler, SONRA vektor.json. Yazımlardan biri (disk/kill) başarısız olursa diverjans yönü
     * "idKumesi ≥ vektorDepo" olur → vektorDepo hiçbir zaman idKumesi'nde olmayan id içermez →
     * temizle() DAİMA tam siler, orphan (silinemez bayat belge) birikmez. En kötü hâl: bir not geçici
     * olarak aranamaz (sonraki indekslemede döner) — zararsız; ters sıra ise kalıcı orphan üretirdi.
     */
    private synchronized void kaydet() {
        if (dosya == null) {
            return;
        }
        try {
            Path hedef = dosya.toPath().toAbsolutePath();
            if (hedef.getParent() != null) {
                Files.createDirectories(hedef.getParent());
            }
            atomikTasi(sidecarYolu(), tmp -> Files.write(tmp, idKumesi));
            atomikTasi(hedef, tmp -> vektorDepo.save(tmp.toFile()));
        } catch (Exception e) {
            log.warn("Anlam deposu kaydedilemedi ({}): {}", dosya, e.getMessage());
        }
    }

    /** temp'e yazdır, sonra hedefin üzerine ATOMIC_MOVE ile taşı (yarım dosya kalmasın). */
    private static void atomikTasi(Path hedef, TmpYazici yazici) throws Exception {
        Path tmp = Path.of(hedef + ".tmp");
        yazici.yaz(tmp);
        try {
            Files.move(tmp, hedef, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException atomik) {
            Files.move(tmp, hedef, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @FunctionalInterface
    private interface TmpYazici {
        void yaz(Path tmp) throws Exception;
    }

    private Path sidecarYolu() {
        return Path.of(dosya.getPath() + ".idler");
    }

    private void idKumesiniYukle() {
        Path p = sidecarYolu();
        if (Files.isRegularFile(p)) {
            try {
                Files.readAllLines(p).stream().filter(s -> !s.isBlank()).forEach(idKumesi::add);
            } catch (Exception e) {
                log.warn("id sidecar'ı yüklenemedi ({}): {}", p, e.getMessage());
            }
        }
    }
}
