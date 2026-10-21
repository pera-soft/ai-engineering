package tr.velzon.aiogretmen.service;

import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Kaba kuvvet kilidi (Faz 9a, ADR-0012): Spring Security'nin kimlik doğrulama olaylarını dinler.
 * Yalnız YANLIŞ ŞİFRE ({@code BadCredentials}) sayılır; kilitli/doğrulanmamış hesaba deneme şifre
 * kontrolüne ulaşmadığından kilidi uzatmaz. Başarılı giriş sayacı sıfırlar.
 */
@Component
@Profile("site")
public class GirisDenemeTakibi {

    private final KimlikService kimlikService;

    public GirisDenemeTakibi(KimlikService kimlikService) {
        this.kimlikService = kimlikService;
    }

    @EventListener
    public void hataliSifre(AuthenticationFailureBadCredentialsEvent olay) {
        kimlikService.basarisizGiris(olay.getAuthentication().getName());
    }

    @EventListener
    public void basarili(AuthenticationSuccessEvent olay) {
        kimlikService.basariliGiris(olay.getAuthentication().getName());
    }
}
