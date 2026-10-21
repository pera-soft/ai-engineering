package tr.velzon.aiogretmen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Web/stdio (mcp-http DIŞI) profillerinde güvenlik zinciri — <strong>auth YOK, permitAll</strong>.
 *
 * <p><strong>Neden gerekli:</strong> {@code spring-boot-starter-security} classpath'e girince Spring Boot
 * TÜM endpoint'leri varsayılan olarak korur (401 + üretilen parola). Web arayüzü (127.0.0.1:8090) ve
 * mevcut testler auth'suz kalmalı (AGENTS.md: mevcut davranış bozulmaz). Bu zincir, {@code !mcp-http}
 * profillerinde açıkça permitAll uygulayarak varsayılan korumalı zinciri devre dışı bırakır.
 *
 * <p>Spring Security'nin kendi CSRF'i KAPALI: uygulamanın kendi {@code web/CsrfKoruma} filtresi
 * (Sec-Fetch-Site/Origin) siteler-arası korumayı zaten sağlıyor; Spring CSRF açık kalsaydı token'sız
 * POST'ları 403'lerdi. httpBasic/formLogin de kapalı — permitAll'da tetiklenmez, temizlik için kapatıldı.
 *
 * <p>Auth SADECE {@code mcp-http} profilinde {@code /mcp} için: bkz {@link McpHttpGuvenlikYapilandirma}.
 */
@Configuration
@Profile("!mcp-http")
public class VarsayilanGuvenlikYapilandirma {

    @Bean
    SecurityFilterChain varsayilanGuvenlikZinciri(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(yetki -> yetki.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())      // uygulamanın kendi CsrfKoruma filtresi devrede
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());
        return http.build();
    }
}
