package tr.velzon.aiogretmen.service;

import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.repo.KullanimSorgulari;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Token/maliyet görünürlüğü (brief 7.6): GET /api/kullanim'ın veri kaynağı. */
@Service
public class KullanimService {

    private final KullanimSorgulari kullanimSorgulari;
    private final OgretmenAiClient aiClient;

    public KullanimService(KullanimSorgulari kullanimSorgulari, OgretmenAiClient aiClient) {
        this.kullanimSorgulari = kullanimSorgulari;
        this.aiClient = aiClient;
    }

    public Map<String, Object> istatistik() {
        List<Map<String, Object>> islemler = kullanimSorgulari.islemBazindaToplam();

        long toplamCagri = 0, toplamInput = 0, toplamOutput = 0;
        for (Map<String, Object> satir : islemler) {
            toplamCagri += sayi(satir.get("cagri"));
            toplamInput += sayi(satir.get("input_tokens"));
            toplamOutput += sayi(satir.get("output_tokens"));
        }

        Map<String, Object> sonuc = new LinkedHashMap<>();
        sonuc.put("aiAktif", aiClient.aktif());
        sonuc.put("toplamCagri", toplamCagri);
        sonuc.put("toplamInputToken", toplamInput);
        sonuc.put("toplamOutputToken", toplamOutput);
        sonuc.put("islemler", islemler);
        return sonuc;
    }

    private static long sayi(Object o) {
        return o instanceof Number n ? n.longValue() : 0;
    }
}
