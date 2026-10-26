# ADR-0007: Yerel MCP sunucusu (stdio) — muhakeme istemcide

## Durum (Status)
Accepted — 2026-07-16 (Faz 1: yerel/stdio). Faz 2 ilk adım (streamable-HTTP transport, AUTH'SUZ) —
2026-07-16, bkz "Faz 2 — Transport adımı". Faz 2 kalanı (auth + çok-kullanıcı + deploy) ERTELENDİ.

## Bağlam (Context)
İki hedef birleşiyor: (1) MCP'yi (Model Context Protocol) öğrenmek ve uygulamayı "öğrettiğinin
canlı örneği" yapmak (Hafta 9 — hexagonal/adapter), (2) uygulamayı başkalarına açmak — **ama sunucu
sahibinin OpenRouter token'ı yanmadan.** Önceki "çok kullanıcıda token yetmez" sorununun doğru çözümü:
uygulamayı bir **MCP sunucusu** yaparsak, bağlanan kişi kendi Claude'uyla (Claude Desktop/Code)
bağlanır ve **öğretmenlik muhakemesini kendi token'ıyla** yapar; sunucu yalnızca içerik + araç +
prompt sunar.

Spring AI 1.1.8'de kalıyoruz; 2.0'a yükseltmiyoruz (kırıcı değişiklik riski — projenin ChatModel
kurulumu, OpenRouter geçişi ve mevcut testleri 1.1.x'e göre).

## Karar (Decision)
- **Yeni katman `..aiogretmen.mcp..`**: `web` ile aynı seviyede dış-arayüz adaptörü; yalnız `service`
  (ve altındaki `types`/`config`) katmanına bağımlı. `MimariTestleri.katman_kurali`'na `Mcp` katmanı,
  `Service`'e `mayOnlyBeAccessedByLayers("Web","Mcp")` eklendi.
- **MERKEZÎ İLKE — MCP adaptörü LLM ÇAĞIRMAZ.** Bu bir niyet değil, **derleme-zamanı garantisi**:
  `MimariTestleri.claude_yalniz_ai_paketinden` kuralı `org.springframework.ai..` / `com.anthropic..`
  bağımlılığına yalnız `..aiogretmen.ai..` paketinin izin verir. `mcp` paketi Spring AI'yı import
  etmediği sürece LLM/embedding çağıramaz → maliyet bağlanan kullanıcıda kalır.
- **Bunun API sonucu:** Araçlar Spring AI'nın `@Tool` + `ToolCallbackProvider`/`MethodToolCallbackProvider`
  deseniyle **tanımlanamaz** — çünkü `@Tool` = `org.springframework.ai.tool.annotation.Tool` olup yukarıdaki
  kuralı ihlal ederdi. Bunun yerine araç/kaynak/prompt'lar doğrudan **MCP Java SDK'sının**
  (`io.modelcontextprotocol..`) `McpServerFeatures.Sync{Tool,Resource,ResourceTemplate,Prompt}Specification`
  bean'leriyle tanımlanır. Spring AI MCP server auto-config (`McpServerAutoConfiguration`) bu bean listelerini
  `ToolCallbackProvider`'lara ek olarak **zaten toplar** (doğrulandı: `mcpSyncServer` imzası
  `ObjectProvider<List<SyncToolSpecification>>` vb. alır). Böylece 2.0'ın `@McpTool` anotasyonuna da gerek yok.
- **MCP adaptörüne `OgretmenAiClient` ve `AnlamArama` (embedding) ENJEKTE EDİLMEZ.** Yalnız LLM'siz
  servisler kullanılır (`MufredatService`, `KaynakService`, `AtolyeService.kategoriler`, `IlerlemeService`,
  `KullanimService`). Öğretmenlik prompt'ları `PromptFactory` + `BaglamServisi.sistemBaglamiEmbeddingsiz`
  ile (anlam-eşleşmeli kitap bağlamı KAPALI — embedding çağrısı yok) **metin olarak** üretilip MCP prompt
  primitifiyle sunulur; muhakeme istemcide.
- **Üç primitif de sunulur** (SDK 0.18.3, Spring AI 1.1.8 auto-config hepsini destekler):
  - **Tools** (8, az sayıda — MCP "context tax"): `program_ozeti`, `hafta_detay`, `kaynaklar`, `kaynak`,
    `atolye_kategorileri`, `gorev_isaretle`, `statik_quiz_kaydet`, `kullanim_istatistik`.
  - **Resources**: somut `content://hafta/{no}`, `kaynak://{slug}`, `atolye://{slug}`; şablon
    `content://hafta/{no}/gun/{gun}` (gün sayısı çok — tek tek listelenmez).
  - **Prompts**: `ogretmen-sistem`, `sokratik`, `degerlendir`, `atolye-degerlendir`, `quiz-uret`.
- **Transport = stdio, ayrı profil.** `mcp` Spring profili (`application-mcp.yml`):
  `spring.ai.mcp.server.{enabled:true, stdio:true, type:SYNC}`, web sunucusu kapalı
  (`web-application-type: none`), banner + konsol log kapalı (stdout yalnız JSON-RPC'ye ayrılır),
  loglar dosyaya. Varsayılan/web profilinde `spring.ai.mcp.server.enabled: false` — normal açılış
  MCP transport'u aramaz, mevcut web davranışı/testleri değişmez.

## Değerlendirilen alternatifler
- **Spring AI `@Tool` + `MethodToolCallbackProvider`** (dokümanlardaki yaygın yol) — reddedildi:
  `@Tool` `org.springframework.ai` paketindedir; `mcp` paketine girseydi `claude_yalniz_ai_paketinden`
  kuralını ihlal eder ve "sunucu LLM çağırmaz" mimari mührünü delerdi. SDK spesifikasyon bean'leri aynı
  işlevi Spring AI import etmeden verir.
- **Spring AI 2.0 + `@McpTool` anotasyonu** — reddedildi: kırıcı yükseltme; kullanıcı 1.1.8'de kalmayı seçti.
- **MCP'yi web ile aynı process/portta açmak** — Faz 1'de reddedildi: stdio stdout'u kullanır, web log'u
  protokolü bozar; ayrı profil daha temiz. (Faz 2'de streamable-HTTP ile birlikte çalışabilir.)

## Sonuçlar (Consequences)
- (+) Bağlanan kullanıcı kendi token'ıyla muhakeme eder → sunucu sahibinin OpenRouter anahtarı yanmaz;
  "çok kullanıcıda token yetmez" sorunu ortadan kalkar.
- (+) "Sunucu LLM çağırmaz" ilkesi ArchUnit ile **derleme zamanında** mühürlendi; programın öğrettiği
  guardrail fikrinin canlı örneği.
- (+) Katman mimarisi genişledi ama yönü korundu (types → … → {web, mcp}); değişiklik `mcp` + ince service
  yardımcılarıyla sınırlı.
- (−) Araçlar SDK tipleriyle elle kurulur (`@Tool` otomasyonu yok) → biraz daha fazla iskele kod
  (`OgretmenMcpAraclari/Kaynaklari/Promptlari`). Kabul edilen bedel: LLM-izolasyonu garantisi.
- (−) stdio ayrı profil → aynı process'te hem web hem MCP çalışmaz (Faz 1). Faz 2'de streamable-HTTP ile giderilir.
- (−) MCP Java SDK 1.1.8 üzerinden gelen sürüm 0.18.3'e bağımlıyız; SDK API'si sürümler arası değişebilir
  (bu yüzden tipler doğrudan jar'dan doğrulandı).

## Faz 2 — Transport adımı (streamable-HTTP, AUTH'SUZ — eklendi 2026-07-16)
MCP artık ağ üzerinden de erişilebilir. **Auth bu adımda YOK** (bir sonraki adım) → endpoint yalnız
`127.0.0.1`'e bind, public açılmaz.
- **Bağımlılık:** `spring-ai-starter-mcp-server-webmvc` (WebFlux DEĞİL — Faz 2 auth için `mcp-security`
  WebMVC ister; BOM 1.1.8). stdio starter (`spring-ai-starter-mcp-server`) ile **birlikte durur**: ikisi
  aynı `spring-ai-autoconfigure-mcp-server-common` auto-config'ini paylaşır, webmvc starter yalnızca
  streamable-HTTP transport'unu (`WebMvcStreamableServerTransportProvider`) ekler. Aynı
  `Sync{Tool,Resource,Prompt}Specification` bean'leri toplanır → **Faz 1 sağlayıcıları transport-agnostik,
  değişmedi.**
- **Üç mod, profil ile ayrık** (autoconfig çakışması property/profil ile önlenir):
  - `web` (varsayılan): `spring.ai.mcp.server.enabled: false` → tüm MCP transport'ları KAPALI, 8090 web UI.
  - `mcp` (stdio, Faz 1): `stdio: true` → `McpServerStdioDisabledCondition` HTTP transport'larını bastırır;
    `web-application-type: none`. Değişmedi.
  - `mcp-http` (YENİ, `application-mcp-http.yml`): `enabled: true`, `stdio: false`,
    `protocol: STREAMABLE`, `streamable-http.mcp-endpoint: /mcp`, `type: SYNC`.
- **Port kararı: ayrı port 8091 (127.0.0.1).** Web UI 8090'da kalır; `mcp-http` MCP'yi 8091'de açar. Ayrı
  port seçildi (aynı context'te /mcp yerine) çünkü iki mod aynı makinede yan yana çalışabilsin ve web
  davranışı hiç etkilenmesin. Default `protocol` = `SSE` olduğundan streamable yalnız açıkça
  `STREAMABLE` verilince etkinleşir; stdio profilinde HTTP transport iki kez güvenceyle kapalı
  (stdio-disabled + servlet yok). **Token modeli mührü aynen geçerli:** webmvc starter da yalnız MCP SDK
  transport'u ekler, `mcp` paketi hâlâ Spring AI import etmez → `claude_yalniz_ai_paketinden` YEŞİL,
  muhakeme istemcide, `model:*: none` ile OpenRouter'a sıfır çağrı.
- **Doğrulandı:** `mcp-http` profilinde `initialize` + `tools/list` → 8 tool; OPENROUTER_API_KEY olmadan
  çalışır (sunucu LLM çağırmaz). Web profili hâlâ yalnız 127.0.0.1:8090, MCP kapalı.

## Faz 2a-2 — OAuth 2.1 resource server (eklendi 2026-07-16)
`/mcp` artık **bearer JWT** ile korunuyor. MCP spec: korunan sunucu kendi AS'i DEĞİL, harici AS'in
(Auth0 vb.) verdiği token'ı doğrulayan bir **OAuth 2.1 resource server**'dır. SAĞLAYICI-BAĞIMSIZ: issuer
bir env değeri; gerçek AS deploy adımında bağlanır. **Auth SADECE `mcp-http` profilinde**; web (8090) auth'suz kalır.

- **Bağımlılıklar:** `spring-boot-starter-security`, `spring-boot-starter-oauth2-resource-server`,
  `org.springaicommunity:mcp-server-security:**0.0.6**`. **Sürüm kısıtı:** modülün `0.1.x` hattı Spring AI
  **2.0**'ı ister; biz 1.1.8'deyiz → `0.0.6` (Spring AI 1.1.x için son uyumlu). Modül Spring **resmi değil**
  (topluluk); WebMVC-only, **JWT-only** (opaque token yok), WebFlux desteklenmez, SSE transport desteklenmez.
- **`spring-boot-starter-security` TÜM endpoint'leri otomatik korur.** Bu yüzden `config/VarsayilanGuvenlikYapilandirma`
  (`@Profile("!mcp-http")`) tüm diğer profillerde **permitAll** zinciri sağlar (web davranışı + testler bozulmaz);
  Spring'in kendi CSRF'i kapalı (uygulamanın `web/CsrfKoruma` filtresi zaten var). @WebMvcTest slice'ları bu
  config'i `@Import` eder (aksi halde slice default korumalı zincirle 401 verirdi).
- **Katman/ArchUnit:** İki güvenlik `@Configuration`'ı **`config` katmanına** kondu; hiçbir uygulama katmanına
  (web/mcp/service) bağımlı değiller → `katman_kurali` ve `claude_yalniz_ai_paketinden` **değişmeden YEŞİL**
  (ArchUnit'e ekleme GEREKMEDİ). Kullanıcı-kimliği yardımcısı `mcp/McpKullaniciKimligi` **`mcp` paketinde**;
  Spring Security kullanır ama Spring AI/LLM DEĞİL → LLM-izolasyon grep BOŞ kalır.
- **mcp-server-security elle bağlandı** (tek-satır `McpServerOAuth2Configurer.mcpServerOAuth2()` DSL'i yerine):
  o configurer JWT decoder'ı **daima `NimbusJwtDecoder.withIssuerLocation(issuer)`** ile kurar ve bu
  **açılışta (eager) issuer'a ağ çağrısı** yapar → gerçek AS olmadan (yerel test) context boot edemez ve
  decoder'ı dışarıdan verecek kanca yok. Aynı garantileri veren modül bileşenleri elle kuruldu
  (`OAuth2ProtectedResourceMetadataEndpointFilter` + `BearerResourceMetadataTokenAuthenticationEntryPoint`
  + `JwtResourceValidator`), decoder ise `jwk-set-uri` verilirse **lazy `withJwkSetUri`** (yerel test/dev,
  açılışta ağ yok), yoksa `withIssuerLocation` (prod, fail-fast discovery).
- **`.well-known/oauth-protected-resource` (RFC 9728):** modül SAĞLIYOR (`OAuth2ProtectedResourceMetadataEndpointFilter`);
  korumasız, 401'de `WWW-Authenticate` bu metadata'yı işaret eder.
- **Audience (RFC 8707):** modülün `JwtResourceValidator`'ı `aud` claim'inin **resource kimliğini** —
  istek origin'i + `resource-path` (ör. `https://sunucu/mcp`) — içermesini şart koşar. Yani audience,
  ayrı bir static client-id değil, **MCP resource URL'i**dir (spec-uyumlu). `validate-audience:true` varsayılan.
- **Env değişkenleri (deploy):** `MCP_ISSUER_URI` (gerçek issuer, ör. `https://<tenant>.eu.auth0.com/`),
  `MCP_JWK_SET_URI` (prod'da boş → issuer discovery; test/dev'de lazy decoder için verilir),
  `MCP_RESOURCE_PATH` (vars. `/mcp`), `MCP_RESOURCE_NAME`, `MCP_VALIDATE_AUD` (vars. `true`). Config namespace
  bilinçli **`mcp.oauth2.*`** (Boot'un `spring.security.oauth2.resourceserver.jwt.*` auto-config'i EAGER
  JwtDecoder kurup açılışta ağ çağrısı yapardı).
- **Kullanıcı kimliği:** `McpKullaniciKimligi.kullaniciKimligi()` doğrulanmış `email` (yoksa `sub`) döndürür;
  ŞU AN yalnız erişilebilir + loglanır + test edilir. **Tool'lar HENÜZ kullanıcı-kimlikli DEĞİL** — sonraki
  adım (şema migration, `kullanici_id`) bunu kullanacak.
- **Doğrulandı (TDD):** `mcp-http` güvenlik context'inde token'sız `/mcp` → **401**, geçerli (mockJwt) JWT → **200**
  (8 tool kayıtlı). `./mvnw verify` YEŞİL (167 test, ArchUnit dahil); web profili 8090'da auth'suz.

## Faz 2b — Deploy: CANLI (`https://ai.velzon.tr/mcp`) — eklendi 2026-07-16
Faz 2'nin kalanı (uzak + çok-kullanıcı) tamamlandı ve yayına alındı. Kararlar ve kanıtlar:

- **Barındırma:** jar + systemd (`ai-ogretmen.service`), izole sistem kullanıcısı `aiogretmen`,
  `/opt/ai-ogretmen`, `MemoryMax=768M` + `-Xmx512m`, `ProtectSystem=strict`/`NoNewPrivileges`.
  Sunucu paylaşımlı (başka canlı projeler var) → port **8091** seçildi (8090 doluydu), mevcut hiçbir
  servise dokunulmadı.
- **Bind kuralı KORUNDU:** uygulama hâlâ `127.0.0.1`'e bind eder. Public olan tek şey nginx'in TLS ile
  terminate ettiği `ai.velzon.tr` → `proxy_pass 127.0.0.1:8091`. `/mcp` için `proxy_buffering off` +
  uzun `proxy_read_timeout` (streamable-HTTP/SSE). Sertifika: Let's Encrypt (certbot, oto-yenileme).
- **`server.forward-headers-strategy: framework` (KRİTİK):** RFC 9728 metadata ve RFC 8707 resource
  identifier (`aud`) **istek origin'inden** kurulur. Forward olmadan origin `http://127.0.0.1:8091`
  kalır, token audience'ı `https://ai.velzon.tr/mcp` ile eşleşmez ve OAuth doğrulaması patlar.
- **OAuth sağlayıcı: Auth0** (hazır; kendi AS'imizi yazmadık). Bağlanan istemci **DCR** ile kendini
  kaydeder (`tpc_*` third-party client). Öğrenilen üç tuzak:
  1. **`resource_parameter_profile=compatibility` şart** — Auth0 varsayılanda MCP'nin gönderdiği
     `resource=` parametresini yok sayar (yalnız `audience=` bakar); bu profil `resource`'u `aud`'a map eder.
  2. **CIMD KAPALI tutulmalı** — açıkken istemci CIMD'yi tercih ediyor, Auth0 `Unknown client` ile reddediyor.
     Kapatınca istemci çalışan DCR yoluna düşer.
  3. **Third-party client'lara API erişimi** tek tek grant ile verilemez (client'lar dinamik) → tek bir
     **varsayılan grant**: `POST /api/v2/client-grants` `{audience: <resource>, default_for:
     "third_party_clients", subject_type: "user"}`.
- **Env (sunucuda, git'te DEĞİL):** `MCP_ISSUER_URI` (sonda slash şart), `MCP_JWK_SET_URI`,
  `MCP_RESOURCE_PATH=/mcp`, `MCP_VALIDATE_AUD=true`. **`OPENROUTER_API_KEY` YOK** — bilinçli.
- **Token modeli KANITLANDI (prod):** çalışan süreçte `OPENROUTER_API_KEY` **yok** (`/proc/<pid>/environ`
  boş), log'da OpenRouter çağrısı **0**; buna rağmen istemci `program_ozeti` çağırıp 12 haftalık programı
  aldı. Yani muhakeme istemcinin token'ında; sunucu sahibinin token'ı yanmıyor. Bu ADR'nin merkezî
  iddiası artık teorik değil, ölçülmüş.
- **Kullanıcı izolasyonu KANITLANDI:** Auth0 kimliğiyle bağlanan yeni kullanıcı, yereldeki (`yerel`)
  ilerlemeden bağımsız temiz sayfa gördü (`kullanici_id`, ADR-0008).
- **KVKK:** `content/kaynaklar/ai-yonetisim-uyum.md` ilkeleri; saklanan tek PII kullanıcı kimliği
  (email/sub) + ilerleme. Not: tenant **US** bölgesinde açıldı (EU tercih edilirdi) — düşük PII
  nedeniyle kabul edildi; gerekirse EU tenant'a taşınır (issuer değişir, env güncellenir).

### Bilinen dış kısıt (bizim değil)
claude.ai **web** istemcisi custom connector araçlarını modele enjekte etmeyebiliyor (Anthropic'in
bilinen hatası; ~10+ connector'da context tavanı da doluyor). Aynı sunucu **Claude Code / ChatGPT /
Cursor**'da sorunsuz çalışır — doğrulandı. Sunucu tarafında yapılacak bir şey yok.

## İlgili
- Doğrulama: `./mvnw verify` (ArchUnit `Mcp` katmanı + `claude_yalniz_ai_paketinden` YEŞİL) ve
  `grep -rn "org.springframework.ai\|OgretmenAiClient\|AnlamArama" src/main/java/tr/velzon/aiogretmen/mcp/` → BOŞ.
- ADR-0004 (OpenRouter), ADR-0005 (anlam-eşleşmeli bağlam — MCP'de KAPALI), ADR-0001 (Spring Boot + Spring AI).

## Not (2026-10-26): `gorev_isaretle` idempotent set
`gorev_isaretle` artık toggle değil, hedef-durum ayarlar: opsiyonel `hedef_durum` (boolean, verilmezse
`true`). Aynı çağrı N kez = aynı sonuç; istemcinin zaman aşımı sonrası tekrar denemesi işareti geri almaz.
Web tarafında `POST /api/gorev/toggle` yerine `POST /api/gorev/durum` (`tamamlandi` zorunlu). Gerekçe: ADR-0016.

## Kapanış (2026-10-21)
Canlı sunucu kapatıldı; bu ADR'deki dağıtım adımları artık uygulanmaz. Uygulama yalnız yerelde
(127.0.0.1:8090) çalışır; `tools/deploy.sh` kaldırıldı (git geçmişinde duruyor).
