package tr.velzon.aiogretmen.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import tr.velzon.aiogretmen.service.KimlikService;
import tr.velzon.aiogretmen.types.KayitGecersizException;

import java.util.Map;

/**
 * {@code site} profili hesap uçları (Faz 9a, ADR-0012). Giriş/çıkış uçları Spring Security
 * filtrelerindedir ({@code POST /api/kimlik/giris}, {@code POST /api/kimlik/cikis}); burada yalnız:
 * <ul>
 *   <li>{@code POST /api/kimlik/kayit} {email, sifre} → 200 (hesap var/yok ayırt edilmez) | 400 kural ihlali</li>
 *   <li>{@code GET /api/kimlik/dogrula?token=…} → e-postadaki bağlantı; sonuç sayfasına yönlendirir</li>
 *   <li>Faz 9c (ADR-0013), kimliksiz + CSRF'li:
 *     <ul>
 *       <li>{@code POST /api/kimlik/dogrulama-yeniden} {email} → 200 nötr {@code {"durum":"istek-alindi"}}</li>
 *       <li>{@code POST /api/kimlik/sifremi-unuttum} {email} → 200 nötr (hesap var/yok ayırt edilmez)</li>
 *       <li>{@code POST /api/kimlik/sifre-sifirla} {token, sifre} → 200 {@code {"durum":"sifre-guncellendi"}}
 *           (kullanıcının BÜTÜN oturumları düşer) | 400 {hata, detay} (geçersiz/süresi dolmuş bağlantı, şifre kuralı)</li>
 *     </ul></li>
 * </ul>
 */
@RestController
@RequestMapping("/api/kimlik")
@Profile("site")
public class KimlikController {

    private final KimlikService kimlikService;
    private final SessionRegistry oturumKaydi;

    public KimlikController(KimlikService kimlikService, SessionRegistry oturumKaydi) {
        this.kimlikService = kimlikService;
        this.oturumKaydi = oturumKaydi;
    }

    public record KayitIstek(String email, String sifre) {
    }

    @PostMapping("/kayit")
    public Map<String, Object> kayit(@RequestBody KayitIstek istek) {
        kimlikService.kayit(istek.email(), istek.sifre());
        return Map.of("durum", "dogrulama-bekleniyor");
    }

    public record EpostaIstek(String email) {
    }

    public record SifirlaIstek(String token, String sifre) {
    }

    private static final Map<String, Object> NOTR = Map.of("durum", "istek-alindi");

    @PostMapping("/dogrulama-yeniden")
    public Map<String, Object> dogrulamaYeniden(@RequestBody EpostaIstek istek) {
        kimlikService.dogrulamaYenidenGonder(istek.email());
        return NOTR;
    }

    @PostMapping("/sifremi-unuttum")
    public Map<String, Object> sifremiUnuttum(@RequestBody EpostaIstek istek) {
        kimlikService.sifreSifirlamaIste(istek.email());
        return NOTR;
    }

    @PostMapping("/sifre-sifirla")
    public Map<String, Object> sifreSifirla(@RequestBody SifirlaIstek istek) {
        String email = kimlikService.sifreSifirla(istek.token(), istek.sifre())
                .orElseThrow(() -> new KayitGecersizException("Şifre sıfırlama bağlantısı geçersiz, kullanılmış ya da "
                        + "süresi (1 saat) dolmuş. \"Şifremi unuttum\" sayfasından yeni bağlantı iste."));
        tumOturumlariDusur(email); // eski şifreyle açılmış oturumlar (ele geçirilmiş olabilir) kapansın
        return Map.of("durum", "sifre-guncellendi");
    }

    @GetMapping("/dogrula")
    public ResponseEntity<Void> dogrula(@RequestParam(name = "token", required = false) String token) {
        String durum = kimlikService.dogrula(token) ? "basarili" : "gecersiz";
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/dogrulama-sonuc.html?durum=" + durum)
                .build();
    }

    /** Kullanıcının tüm oturumları: bir sonraki istekte düşürülür (ConcurrentSessionFilter → 401/giriş). */
    private void tumOturumlariDusur(String email) {
        oturumKaydi.getAllPrincipals().stream()
                .filter(p -> p instanceof UserDetails u && u.getUsername().equals(email))
                .flatMap(p -> oturumKaydi.getAllSessions(p, false).stream())
                .forEach(o -> o.expireNow());
    }
}
