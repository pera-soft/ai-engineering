package tr.velzon.aiogretmen.config;

import org.springaicommunity.mcp.security.server.oauth2.authentication.BearerResourceMetadataTokenAuthenticationEntryPoint;
import org.springaicommunity.mcp.security.server.oauth2.jwt.JwtResourceValidator;
import org.springaicommunity.mcp.security.server.oauth2.metadata.OAuth2ProtectedResourceMetadataEndpointFilter;
import org.springaicommunity.mcp.security.server.oauth2.metadata.ResourceIdentifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code mcp-http} profilinde {@code /mcp} endpoint'ini <strong>OAuth 2.1 resource server (JWT)</strong>
 * ile korur (ADR-0007, Faz 2a-2). MCP spec: korunan sunucu kendi AS'i DEĞİL, harici AS'in (Auth0 vb.)
 * verdiği bearer token'ı doğrulayan bir resource server'dır. Token'daki kullanıcı kimliği (email/sub)
 * {@code mcp/McpKullaniciKimligi} ile erişilir.
 *
 * <p><strong>Topluluk modülü {@code org.springaicommunity:mcp-server-security:0.0.6} bileşenleri elle
 * bağlanır</strong> — {@code McpServerOAuth2Configurer.mcpServerOAuth2()} tek-satır DSL'i yerine. Neden:
 * o configurer JWT decoder'ı <em>daima</em> {@code NimbusJwtDecoder.withIssuerLocation(issuer)} ile kurar
 * ve bu <strong>açılışta (eager) issuer'a ağ çağrısı yapar</strong>; gerçek AS bağlı olmadan (yerel test)
 * context boot edemez ve decoder'ı dışarıdan enjekte edecek bir kanca yoktur. Aynı garantileri veren
 * modül bileşenlerini elle kurarak (metadata endpoint + entry point + audience validator) hem prod'da
 * doğru davranışı hem de {@code jwk-set-uri} ile <em>lazy</em> (ağsız) yerel testi sağlıyoruz:
 * <ul>
 *   <li>{@link OAuth2ProtectedResourceMetadataEndpointFilter} — korumasız {@code /.well-known/oauth-protected-resource}
 *       (RFC 9728); istemci hangi AS'e gideceğini buradan öğrenir.</li>
 *   <li>{@link BearerResourceMetadataTokenAuthenticationEntryPoint} — 401'de {@code WWW-Authenticate}
 *       başlığı bu metadata'yı işaret eder.</li>
 *   <li>{@link JwtResourceValidator} (RFC 8707) — {@code aud} claim'i bu sunucunun resource kimliğini
 *       (istek origin'i + {@code resource-path}) içermeli; başka sunucuya verilmiş token reddedilir.</li>
 * </ul>
 *
 * <p><strong>Decoder seçimi:</strong> {@code jwk-set-uri} verilmişse lazy {@code withJwkSetUri} (yerel test/dev,
 * açılışta ağ yok), verilmemişse {@code withIssuerLocation(issuer)} (prod — fail-fast discovery).
 *
 * <p><strong>Katman/ArchUnit:</strong> {@code config} katmanı; hiçbir uygulama katmanına (web/mcp/service)
 * bağımlı değil. Yalnız Spring Security + {@code org.springaicommunity..} (Spring AI DEĞİL) import edilir →
 * {@code claude_yalniz_ai_paketinden} ve {@code katman_kurali} YEŞİL kalır.
 */
@Configuration
@Profile("mcp-http")
public class McpHttpGuvenlikYapilandirma {

    private final String issuerUri;
    private final String jwkSetUri;
    private final String resourcePath;
    private final String resourceName;
    private final boolean audienceDogrula;

    public McpHttpGuvenlikYapilandirma(
            @Value("${mcp.oauth2.issuer-uri:}") String issuerUri,
            @Value("${mcp.oauth2.jwk-set-uri:}") String jwkSetUri,
            @Value("${mcp.oauth2.resource-path:/mcp}") String resourcePath,
            @Value("${mcp.oauth2.resource-name:AI Öğretmen MCP}") String resourceName,
            @Value("${mcp.oauth2.validate-audience:true}") boolean audienceDogrula) {
        this.issuerUri = issuerUri;
        this.jwkSetUri = jwkSetUri;
        this.resourcePath = resourcePath;
        this.resourceName = resourceName;
        this.audienceDogrula = audienceDogrula;
    }

    @Bean
    SecurityFilterChain mcpGuvenlikZinciri(HttpSecurity http) throws Exception {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier(resourcePath);

        var metadataFiltresi = new OAuth2ProtectedResourceMetadataEndpointFilter(resourceIdentifier);
        metadataFiltresi.setProtectedResourceMetadataCustomizer(md -> md
                .authorizationServer(issuerUri)
                .resourceName(resourceName)
                .bearerMethod("header"));

        var girisNoktasi = new BearerResourceMetadataTokenAuthenticationEntryPoint(resourceIdentifier);
        var decoder = jwtDecoder(resourceIdentifier);

        http
                .authorizeHttpRequests(yetki -> yetki
                        // RFC 9728 metadata korumasız; geri kalan (yani /mcp) JWT ister
                        .requestMatchers("/.well-known/**").permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())   // MCP istemcileri CSRF token göndermez; POST /mcp bloke edilmemeli
                .oauth2ResourceServer(rs -> rs
                        .authenticationEntryPoint(girisNoktasi)
                        .jwt(jwt -> jwt.decoder(decoder)))
                .addFilterBefore(metadataFiltresi, AbstractPreAuthenticatedProcessingFilter.class);

        return http.build();
    }

    /**
     * Lazy ({@code jwk-set-uri}) ya da eager ({@code issuer-uri}) JWT decoder; ardından imza sonrası
     * doğrulayıcılar: varsayılan (zaman) + issuer + (opsiyonel) audience (RFC 8707).
     */
    private NimbusJwtDecoder jwtDecoder(ResourceIdentifier resourceIdentifier) {
        NimbusJwtDecoder decoder = (jwkSetUri != null && !jwkSetUri.isBlank())
                ? NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()          // lazy: açılışta ağ yok (yerel test/dev)
                : NimbusJwtDecoder.withIssuerLocation(issuerUri).build();    // eager: prod discovery (fail-fast)

        List<OAuth2TokenValidator<Jwt>> dogrulayicilar = new ArrayList<>();
        if (issuerUri != null && !issuerUri.isBlank()) {
            dogrulayicilar.add(new JwtIssuerValidator(issuerUri));
        }
        if (audienceDogrula) {
            dogrulayicilar.add(new JwtResourceValidator(resourceIdentifier));
        }
        decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(dogrulayicilar));
        return decoder;
    }
}
