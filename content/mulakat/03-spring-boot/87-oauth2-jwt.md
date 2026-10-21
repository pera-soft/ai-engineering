---
slug: oauth2-jwt
baslik: "OAuth2 ve JWT: Resource Server, PKCE ve Token Doğrulama"
kademe: İleri
sira: 87
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Arabanı otoparkta valeye bırakırken bütün anahtarlığını vermezsin; yalnız kontağı açan, bagajı açmayan sınırlı bir **vale anahtarı** verirsin. OAuth2 aynı fikri yazılıma taşır: bir uygulamanın senin adına başka bir servisteki verilere erişmesine, **parolanı o uygulamaya vermeden** izin verirsin. Uygulamanın eline geçen şey parolan değil, kısa ömürlü ve sınırlı yetkili bir **bilet**tir. "Google ile giriş yap" düğmesinin arkasındaki mekanizma budur.

### Temel kavramlar

- **Yetki devri (delegated authorization)**: Kullanıcının, kendi yetkisinin bir kısmını bir uygulamaya devretmesi.
- **Jeton (token)**: Kimliği ve yetkileri temsil eden, isteğe eklenen dize — bir tür kimlik belgesi. Parolanın yerine geçer ama parolanın kendisi değildir.
- **Erişim token'ı (access token)**: Korunan API'ye giderken taşınan, kısa ömürlü token.
- **`Authorization: Bearer <token>` başlığı**: Token'ın HTTP isteğinde taşındığı standart yer.
- **İddia (claim)**: Token'ın içindeki bilgi parçacığı — kim (`sub`), kim üretti (`iss`), ne zaman dolar (`exp`), kime hitap ediyor (`aud`), hangi yetkiler (`scope`).
- **İmza (signature)**: Token'ın içeriği değiştirilmediğini kanıtlayan kriptografik mühür. **Şifreleme değildir** — mühür içeriği gizlemez, sadece bozulmadığını gösterir.
- **JWKS (JSON Web Key Set)**: Sağlayıcının, imzayı doğrulamaya yarayan **açık anahtarları** yayınladığı uç nokta.
- **`kid` (key id)**: Token'ın hangi anahtarla imzalandığını söyleyen etiket.

OAuth2'nin dört rolü vardır:

- **Kaynak sahibi** (resource owner): kullanıcının kendisi.
- **İstemci** (client): kullanıcı adına erişmek isteyen uygulama.
- **Yetkilendirme sunucusu** (authorization server): jeton üreten kimlik sağlayıcı.
- **Kaynak sunucusu** (resource server): jetonu doğrulayıp korunan veriyi veren API.

OAuth2'nin kimlik doğrulama için genişletilmiş hali **OpenID Connect**'tir.

### Nasıl çalışır?

2026'da tarayıcı ve mobil istemciler için önerilen akış **Authorization Code + PKCE**'dir:

1. İstemci rastgele bir `code_verifier` üretir, onun SHA-256 hash'ini `code_challenge` olarak yetki isteğine ekler.
2. Kullanıcı yetkilendirme sunucusunda oturum açar ve izin verir; sunucu istemciye bir **yetki kodu** verirken challenge'ı hatırlar.
3. İstemci kodu token'a çevirmeye giderken orijinal `code_verifier`'ı gönderir.
4. Sunucu onu hash'leyip challenge ile karşılaştırır; tutuyorsa erişim token'ını verir.

Kod yolda çalınsa bile saldırgan `code_verifier`'ı bilmediği için token alamaz. İstemci sırrı (client secret) saklayamayan **public istemciler** (SPA, mobil) için PKCE zorunlu kabul edilir; 2026'da gizli istemcilerde de önerilir. PKCE bir şifreleme mekanizması değildir, token ömrünü uzatmaz ve veri erişimiyle ilgisi yoktur.

Spring tarafında iki rol karıştırılmamalıdır. **Authorization Server** token'ı **üretir** (Keycloak, Auth0, Okta ya da Spring Authorization Server). **Resource Server** ise gelen `Authorization: Bearer <token>` başlığındaki jetonu **doğrular** ve isteği yetkilendirir.

Bir Spring Boot uygulamasını Resource Server yapmanın standart yolu üç adımdır. Önce `spring-boot-starter-oauth2-resource-server` bağımlılığı eklenir. Sonra sağlayıcı `issuer-uri` ile işaret edilir. Son olarak `http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))` yazılır. Spring `issuer-uri`'den JWKS uç noktasını kendisi keşfeder; JWKS adresini doğrudan vermek istersen `jwk-set-uri` da kullanılabilir. Jetonu controller içinde elle ayrıştırmak gereksiz ve hataya açıktır; doğrulamadan kabul etmek ise taklit edilmesine kapı açar.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="OAuth2 Resource Server JWT doğrulama ve JWKS anahtar akışı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Token üretilir, Resource Server doğrular</text>
  <rect x="20" y="46" width="140" height="60" rx="10" fill="#F3EEE6" stroke="#9A6A1E" stroke-width="1.5"/>
  <text x="90" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#9A6A1E" font-family="system-ui">İstemci</text>
  <text x="90" y="90" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">PKCE ile kod alır</text>
  <rect x="250" y="46" width="150" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="325" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Authorization Server</text>
  <text x="325" y="90" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">JWT üretir + imzalar</text>
  <rect x="480" y="46" width="140" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="550" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Resource Server</text>
  <text x="550" y="90" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">imzayı doğrular</text>
  <path d="M160 76 L248 76" stroke="#5D7789" stroke-width="1.5" marker-end="url(#oauth-ok)"/>
  <path d="M400 76 L478 76" stroke="#5D7789" stroke-width="1.5" marker-end="url(#oauth-ok)"/>
  <text x="220" y="68" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kod+PKCE</text>
  <text x="440" y="68" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Bearer token</text>
  <rect x="250" y="150" width="150" height="48" rx="10" fill="#F1ECF6" stroke="#6B4CA3" stroke-width="1.5"/>
  <text x="325" y="172" text-anchor="middle" font-size="12" font-weight="700" fill="#6B4CA3" font-family="system-ui">JWKS uç noktası</text>
  <text x="325" y="189" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">açık anahtarlar (kid ile)</text>
  <path d="M480 100 C 430 140, 400 150, 400 168" fill="none" stroke="#6B4CA3" stroke-width="1.4" stroke-dasharray="4 3" marker-end="url(#oauth-ok)"/>
  <text x="470" y="150" text-anchor="middle" font-size="9.5" fill="#6B4CA3" font-family="system-ui">anahtar çek</text>
  <text x="325" y="140" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">anahtar rotasyonu: yeni kid otomatik alınır</text>
  <defs>
    <marker id="oauth-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

Resource Server imzayı **JWKS**'ten çektiği açık anahtarla doğrular; bu düzenek **anahtar rotasyonunu** (key rotation) mümkün kılar. Sağlayıcı anahtar çiftini periyodik değiştirir; token başlığındaki `kid` hangi anahtarın kullanıldığını söyler. Resource Server o `kid`'in anahtarını JWKS önbelleğinde arar; bulamazsa JWKS'i yeniden çeker. Spring'in `NimbusJwtDecoder`'ı bunu kendiliğinden yapar.

Rotasyon **kademelidir**: sağlayıcı eski ve yeni anahtarı bir süre birlikte yayınlar, böylece dolaşımdaki eski token'lar süreleri dolana dek doğrulanabilir. Sonuç: uygulamayı yeniden dağıtmaya gerek kalmaz, sistem de çökmez. Anahtarları diskte elle güncellemek ise rotasyonu kırılgan yapar.

Peki token'ın içinde ne var?

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JWT üç parçadan oluşur: header, payload, signature; noktayla ayrılır">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">JWT yapısı: header . payload . signature</text>
  <rect x="20" y="44" width="184" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Header</text>
  <text x="112" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">alg (imza algoritması)</text>
  <text x="112" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kid (key id)</text>
  <text x="214" y="90" text-anchor="middle" font-size="18" font-weight="700" fill="#5D7789" font-family="system-ui">.</text>
  <rect x="228" y="44" width="184" height="80" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Payload</text>
  <text x="320" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">claims: sub, iss, exp</text>
  <text x="320" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">scope / roles</text>
  <text x="422" y="90" text-anchor="middle" font-size="18" font-weight="700" fill="#5D7789" font-family="system-ui">.</text>
  <rect x="436" y="44" width="184" height="80" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="528" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">Signature</text>
  <text x="528" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">gizli/özel anahtarla</text>
  <text x="528" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">imzalanır</text>
  <text x="320" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Header ve payload yalnız Base64Url'dir — ŞİFRELİ DEĞİL, herkes okuyabilir</text>
  <text x="320" y="174" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">Güvenlik imzadan gelir: içerik değişirse imza tutmaz</text>
</svg>
```

JWT (JSON Web Token) nokta ile ayrılmış üç parçadır: `header.payload.signature`. Bu parçalar yalnız **Base64Url ile kodlanmıştır — şifreli DEĞİLDİR**. Base64 bir gizleme yöntemi değildir; yalnız metni güvenle taşınabilir karakterlere çeviren bir dönüşümdür. Jetonu eline geçiren herkes gövdedeki (payload) iddiaları okuyabilir. Bu yüzden JWT'ye parola, kimlik numarası gibi **gizli veri konmaz**. Güvenlik imzadan gelir: içerik değiştirilirse imza tutmaz. Doğrulama da imzayla bitmez — `exp` (son kullanma), `iss` (üreten) ve `aud` (hedef kitle) alanları da denetlenmelidir.

Son karar token türündedir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JWT yerelde imzayla doğrulanır; opaque token her istekte introspection'a sorulur">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">JWT vs Opaque: doğrulama nerede?</text>
  <rect x="24" y="44" width="286" height="140" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="167" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">JWT (self-contained)</text>
  <text x="167" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">imza YERELDE doğrulanır</text>
  <text x="167" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">(JWKS açık anahtarı ile)</text>
  <text x="167" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ağ turu YOK → hızlı</text>
  <text x="167" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">bedel: anlık iptal zor</text>
  <text x="167" y="174" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">exp dolana dek geçerli</text>
  <rect x="330" y="44" width="286" height="140" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="473" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Opaque token</text>
  <text x="473" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">anlamsız dize</text>
  <text x="473" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">her istekte /introspect'e</text>
  <text x="473" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sorulur → ağ turu VAR</text>
  <text x="473" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">kazanç: anlık iptal</text>
  <text x="473" y="174" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">sağlayıcıya bağımlı</text>
</svg>
```

### OAuth2 ile OIDC farkı: yetki mi, kimlik mi?

Bu ayrım mülakatın klasik ayırt edici sorusudur ve tek cümlede verilebilir: **OAuth2 "bu uygulama ne yapabilir?" sorusunu, OIDC "bu kullanıcı kim?" sorusunu cevaplar.**

**OpenID Connect (OIDC)**, OAuth2'nin üzerine kurulmuş ince bir kimlik katmanıdır. OAuth2'yi değiştirmez, ona bir şey ekler: **kimlik jetonu (`id_token`)**. Aradaki fark üç noktada somutlaşır.

- **İki ayrı jeton, iki ayrı iş.** `access_token` **API'ye** gider; onu doğrulayan taraf kaynak sunucusudur ve "bu istek yetkili mi?" diye sorar. `id_token` ise **istemcide kalır**; onu doğrulayan taraf uygulamanın kendisidir ve "kullanıcı gerçekten oturum açtı mı, kim?" sorusunu cevaplar. `id_token`'ı API'ye taşımak yaygın bir hatadır.
- **Standart kimlik iddiaları.** OIDC `sub` (kullanıcının değişmeyen kimliği), `email`, `name`, `picture` gibi alanları **standartlaştırır**. Saf OAuth2'de böyle bir standart yoktur; her sağlayıcı kullanıcı bilgisini farklı biçimde verir. OIDC ayrıca bir **`userinfo` ucu** ve sağlayıcının tüm adreslerini yayınladığı bir **keşif belgesi** (`/.well-known/openid-configuration`) tanımlar — Spring'in `issuer-uri` ile her şeyi kendiliğinden bulmasının sebebi budur.
- **`scope` içinde `openid` olması şarttır.** Yetki isteğine `openid` kapsamı eklenmezse sağlayıcı `id_token` üretmez; akış saf OAuth2 olarak kalır.

**Neden kimlik doğrulama için saf OAuth2 kullanmak yanlıştır?** Erişim jetonu sana **bir uygulamanın bir kaynağa erişebildiğini** söyler; o jetonun **hangi kullanıcı için** üretildiğini kanıtlamaz. Jeton başka bir uygulama için üretilmiş ve sana iletilmiş olabilir; bu, **karışık vekil (confused deputy)** durumudur. `id_token`'ın `aud` (kime hitap ediyor) ve `nonce` alanları tam olarak bunu engellemek için vardır: uygulama, jetonun **kendisi için** ve **kendi başlattığı oturum için** üretildiğini doğrular. "Erişim jetonunu aldım, demek ki kullanıcı doğrulandı" cümlesi bu yüzden yanlıştır.

Pratikte Keycloak, Auth0, Okta ve Google gibi sağlayıcılar zaten OIDC konuşur; sen `openid` kapsamını isteyip `id_token`'ı doğrularsın. Kurumsal tarafta OIDC, eski SAML tabanlı tek oturum açma (single sign-on) kurulumlarının modern karşılığıdır.

### Mülakatta ne sorulur?

- **"OAuth2 ile OIDC farkı nedir?"** → OAuth2 **yetkilendirmedir** (ne yapabilirsin), OIDC onun üzerine kurulu **kimlik doğrulamadır** (kimsin). OIDC `id_token` ekler, kimlik iddialarını standartlaştırır, `userinfo` ve keşif belgesi tanımlar. `id_token` istemcide doğrulanır, `access_token` API'ye gider. Kimlik doğrulamayı saf erişim jetonuyla yapmak **karışık vekil** riskini doğurur.
- **"OAuth2 neyi çözer?"** → **Yetki devrini**: istemci, kullanıcının parolasını hiç görmeden, kısa ömürlü bir erişim token'ıyla onun adına kaynaklara erişir. Parola hash'leme, bağlantı havuzu veya önbellekleme OAuth2'nin konusu değildir.
- **"Resource Server nasıl kurulur?"** → `spring-boot-starter-oauth2-resource-server` + `issuer-uri` (veya `jwk-set-uri`); Spring imzayı ve `iss`/`exp` alanlarını otomatik doğrular. Authorization Server **üretir**, Resource Server **doğrular**.
- **"PKCE ne işe yarar?"** → Authorization Code akışında **yetki kodunun çalınması** (authorization code interception) saldırısını engeller: kod, yalnız doğru `code_verifier`'a sahip istemci tarafından token'a çevrilebilir. Public istemcilerde zorunludur.
- **"JWT şifreli midir?"** → **HAYIR.** JWT **imzalıdır**, şifreli değildir; header ve payload yalnız Base64Url'dür ve herkes okuyabilir. Gizli veri konmaz. Doğrulamada imzanın yanında `exp`, `iss` ve `aud` da denetlenir.
- **"JWT vs opaque token?"** → **JWT** kendi kendini taşır (self-contained): iddiaları ve imzayı içerir, Resource Server imzayı JWKS'ten aldığı açık anahtarla **yerelde** doğrular — ağ turu yok, hızlıdır. Bedeli: **anlık iptal (revocation) zordur**, jeton `exp` dolana dek geçerlidir. **İçi okunamayan jeton** (opaque token) ise rastgele, anlamsız bir dizedir. Her istekte sağlayıcının `/introspect` uç noktasına sorulur; anlık iptal mümkün olur ama her istekte bir ağ turu ve sağlayıcıya bağımlılık doğar. İkisi aynı şeyin iki adı değildir; seçim hız ile iptal edilebilirlik arasındaki dengedir.
- **"`hasAuthority(\"SCOPE_read\")` içindeki `SCOPE_` nedir?"** → Spring'in varsayılan `JwtGrantedAuthoritiesConverter`'ı, JWT'deki `scope` (veya `scp`) iddiasındaki her değeri **`SCOPE_` önekiyle** bir `GrantedAuthority`'ye çevirir: `"scope": "read write"` → `SCOPE_read`, `SCOPE_write`. Kontrol `.requestMatchers("/rapor/**").hasAuthority("SCOPE_read")` ya da `@PreAuthorize("hasAuthority('SCOPE_read')")` ile yapılır. Rollerdeki `ROLE_` önekine benzer bir eşleme kuralıdır — bir hata değil, şema adı da değil, imza algoritmasıyla da ilgisi yok (`alg` başlıktadır). Sağlayıcı yetkileri `roles`/`authorities` gibi başka bir iddiada veriyorsa `JwtAuthenticationConverter` özelleştirilerek önek ve iddia adı değiştirilir.
- **"Anahtar rotasyonunda ne olmalı?"** → Resource Server, token'ın `kid`'ine karşılık gelen anahtarı JWKS'ten alıp doğrulamayı **kesintisiz sürdürür**. Yeniden derleme/dağıtım gerekmez, sistem çökmez (rotasyon kademelidir), anahtarlar elle dosyada tutulmaz.

### Tek cümlede

OAuth2 parolayı paylaşmadan yetki devretmenin standardıdır; JWT ise bu yetkiyi taşıyan **imzalı ama şifresiz** bir bilettir — herkes okur, kimse değiştiremez, iptali de kolay değildir.

# oauth-1 | Temel
## Soru
OAuth2 çerçevesi temelde neyi çözer?
## A
Bir kullanıcının parolasını daha hızlı ve daha güvenli biçimde hash'leyip saklamayı
🤖 Parola hash'leme OAuth2'nin konusu değil; o `PasswordEncoder`'ın işidir.
## B
Bir istemcinin, kullanıcı izniyle onun kaynaklarına parolayı görmeden erişmesini
🤖 Doğru: OAuth2 yetki devridir; istemci parolayı görmeden token ile erişir.
## C
Veritabanı bağlantılarını havuzlayıp (connection pooling) eşzamanlı erişimi artırmayı
🤖 Bağlantı havuzu OAuth2 ile ilgisizdir; ayrı bir altyapı konusudur.
## D
HTTP isteklerinin yanıtlarını otomatik olarak önbelleğe alıp tekrarını hızlandırmayı
🤖 Önbellekleme OAuth2'nin amacı değildir.
## Doğru
B
## Açıklama
OAuth2 bir **yetki devri** (delegated authorization) çerçevesidir. Temel senaryo: kullanıcı (kaynak sahibi), bir uygulamanın (istemci) kendi adına başka bir servisteki (kaynak sunucusu) verilere erişmesine, **parolasını istemciyle paylaşmadan** izin verir. Kullanıcı kimlik sağlayıcıda oturum açar, istemci de kısa ömürlü bir **erişim token'ı** (access token) alır ve kaynak sunucusuna bu token'la gider. "Google ile giriş yap" düğmesinin arkasındaki mekanizma budur. OAuth2'nin kimlik doğrulama (authentication) için genişletilmiş hali OpenID Connect'tir.

# oauth-2 | Temel
## Soru
Bir Spring Boot uygulamasını gelen JWT'leri doğrulayan bir "Resource Server" yapmak için en doğrudan yol nedir?
## A
Her controller metodunda token'ı elle ayrıştırıp imzayı kendi yazdığın kodla doğrulamak
🤖 Mümkün ama gereksiz ve hataya açık; framework bunu hazır sunar.
## B
Uygulamayı bir Authorization Server olarak yapılandırıp token üretmesini sağlamak
🤖 Authorization Server token ÜRETİR; burada istenen token DOĞRULAYAN Resource Server'dır.
## C
Güvenliği tamamen kapatıp istemcinin gönderdiği token'ın içeriğine olduğu gibi güvenmek
🤖 Token'ı denetlemeden kabul etmek taklit edilmesine kapı açar; asla yapılmaz.
## D
`spring-boot-starter-oauth2-resource-server` ekleyip `issuer-uri` property'sini vermek
🤖 Doğru: starter ve tek property ile JWT imza/issuer doğrulaması otomatik kurulur.
## Doğru
D
## Açıklama
Spring Boot'ta bir uygulamayı **OAuth2 Resource Server** yapmanın standart yolu `spring-boot-starter-oauth2-resource-server` bağımlılığını eklemek ve yapılandırmada sağlayıcıyı işaret etmektir:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://kimlik.ornek.com/realms/uygulama
```

Ardından güvenlik yapılandırmasında `http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))` denir. JWKS adresini doğrudan vermek istersen `issuer-uri` yerine `jwk-set-uri` de kullanılabilir. Spring, `issuer-uri`'den JWKS uç noktasını keşfeder, gelen `Bearer` token'ın imzasını ve `iss`/`exp` gibi alanlarını otomatik doğrular. Token'ı elle ayrıştırmak (A) gereksiz risktir; Resource Server token üretmez (B).

# oauth-3 | Orta
## Soru
Authorization Code akışına PKCE (Proof Key for Code Exchange) eklemenin amacı nedir?
## A
İstemcinin ürettiği gizli `code_verifier` ile çalınan yetki kodunun kullanılmasını engellemek
🤖 Doğru: kod, yalnızca doğru code_verifier'a sahip istemci tarafından token'a çevrilebilir.
## B
Erişim token'ının ömrünü sonsuza uzatıp yenileme (refresh) ihtiyacını ortadan kaldırmak
🤖 PKCE ömürle ilgili değildir; token süresi ayrı bir ayardır.
## C
JWT'yi uçtan uca şifreleyip yalnız kaynak sunucusunun okuyabileceği hale getirmek
🤖 PKCE bir şifreleme mekanizması değildir; kod değişim güvenliği sağlar.
## D
Resource Server'ın kullanıcı veritabanına doğrudan erişip parolayı kendisinin doğrulamasını sağlamak
🤖 PKCE'nin veri erişimiyle ilgisi yoktur; yetki kodu akışını korur.
## Doğru
A
## Açıklama
**PKCE**, Authorization Code akışında araya girip **yetki kodunu çalma** (authorization code interception) saldırısını engeller. İstemci başta rastgele bir `code_verifier` üretir ve onun SHA-256 hash'ini `code_challenge` olarak yetki isteğine ekler. Authorization Server kodu verirken challenge'ı hatırlar. İstemci kodu token'a çevirirken orijinal `code_verifier`'ı gönderir; sunucu hash'leyip challenge ile eşleştirir. Kod çalınsa bile saldırgan `code_verifier`'ı bilmediği için token alamaz. İstemci sırrı (client secret) saklayamayan **public istemciler** (SPA, mobil) için PKCE artık zorunlu kabul edilir; 2026'da gizli istemcilerde de önerilir.

# oauth-4 | Orta
## Soru
JWT (self-contained) ile opaque token arasındaki temel doğrulama farkı nedir?
## A
JWT her istekte sağlayıcının introspection uç noktasına sorulur; opaque token ise yerelde imzayla doğrulanır
🤖 Ters; yerelde imzayla doğrulanan JWT'dir, sağlayıcıya sorulan opaque token'dır.
## B
İkisi de her istekte sağlayıcının introspection uç noktasına gider; aradaki fark yalnızca token'ın uzunluğudur
🤖 JWT için introspection gerekmez; imza yerelde doğrulanabilir.
## C
JWT imzalı bilgiyi kendi içinde taşır, yerelde doğrulanır; opaque token sağlayıcıya sorulur
🤖 Doğru: JWT self-contained ve yerel doğrulanır, opaque token ağ turu gerektirir.
## D
İkisi arasında davranış farkı yoktur; "JWT" ve "opaque token" aynı şeyin iki farklı adlandırmasıdır
🤖 Fark davranışsaldır: biri yerel doğrulanır, diğeri uzak introspection ister.
## Doğru
C
## Açıklama
**JWT** kendi kendini taşıyan (self-contained) bir token'dır: iddiaları (claims) ve imzayı içerir. Resource Server imzayı JWKS'ten aldığı açık anahtarla **yerelde** doğrular — sağlayıcıya ağ turu gerekmez, bu yüzden hızlıdır. Bedeli: token süresi dolana kadar **anlık iptal zordur**. **Opaque token** ise rastgele, anlamsız bir dizedir; Resource Server onu her istekte sağlayıcının `/introspect` uç noktasına sorarak geçerliliğini ve iddialarını öğrenir. Bu **anlık iptali** mümkün kılar ama her istekte ağ turu ve sağlayıcıya bağımlılık getirir. Seçim, hız ile iptal edilebilirlik arasındaki dengeye göre yapılır.

# oauth-5 | İleri
## Soru
Spring Security'de JWT'deki `scope` iddiasını (claim) yetki kontrolünde kullanmak isteyen bir ekip, `hasAuthority("SCOPE_read")` yazıyor. Buradaki `SCOPE_` öneki neyi ifade eder?
## A
Bir Spring hatasıdır; doğru kullanımda önek yazılmamalı, yalnız `hasAuthority("read")` denmelidir
🤖 Hata değil; Spring scope iddialarını bu önekle authority'ye dönüştürür.
## B
Rol adının veritabanı şemasında hangi tabloda tutulduğunu gösteren bir adlandırma kuralıdır
🤖 Önek şema değil; scope iddiasının authority'ye eşlenme kuralıdır.
## C
Token'ın imzalanmasında kullanılan şifreleme algoritmasının kısaltılmış adını taşır
🤖 Algoritma `alg` başlığındadır; `SCOPE_` öneki ile ilgisizdir.
## D
Spring'in `scope` iddiasındaki değerleri `SCOPE_` önekiyle authority'ye çevirmesini
🤖 Doğru: `scope: read` → `SCOPE_read` authority olur; bu yüzden `hasAuthority("SCOPE_read")` çalışır.
## Doğru
D
## Açıklama
Spring Security'nin varsayılan `JwtGrantedAuthoritiesConverter`'ı, JWT'deki `scope` (veya `scp`) iddiasındaki her değeri **`SCOPE_` önekiyle** bir `GrantedAuthority`'ye çevirir. Yani token'da `"scope": "read write"` varsa kullanıcı `SCOPE_read` ve `SCOPE_write` yetkilerine sahip olur; kontrol de filtre zincirinde `.requestMatchers("/rapor/**").hasAuthority("SCOPE_read")` ile ya da metot düzeyinde `@PreAuthorize("hasAuthority('SCOPE_read')")` ile yapılır. Bu, rollerdeki `ROLE_` önekine benzer bir eşleme kuralıdır. Sağlayıcı yetkileri farklı bir iddiada (örn. `roles`, `authorities`) veriyorsa, `JwtAuthenticationConverter` özelleştirilerek önek ve iddia adı değiştirilebilir.

# oauth-6 | İleri
## Soru
Kimlik sağlayıcı imza anahtarını değiştirdiğinde (key rotation), doğru kurulmuş bir Resource Server'ın davranışı ne olmalıdır?
## A
Her anahtar değişiminde Resource Server'ın yeniden derlenip yeniden dağıtılması gerekir
🤖 JWKS mekanizması tam da bunu gereksiz kılmak için vardır; yeniden dağıtım gerekmez.
## B
Eski anahtarla imzalanmış token'lar dahil bütün doğrulamalar bozulur ve sistem çöker
🤖 Rotasyon kademeli yapılır; sağlayıcı bir süre iki anahtarı da yayınlar, kesinti olmaz.
## C
Token'ın `kid` başlığına karşılık gelen anahtarı JWKS'ten alıp doğrulamayı sürdürür
🤖 Doğru: kid ile doğru anahtar JWKS'ten alınır, uygulama müdahalesiz doğrulama sürer.
## D
Resource Server anahtarları kendi diskinde sabit bir dosyada tutmalı ve elle güncellenmelidir
🤖 Anahtarları elle dosyada tutmak rotasyonu manuel ve kırılgan yapar; JWKS otomatik çözer.
## Doğru
C
## Açıklama
**Anahtar rotasyonu**, sağlayıcının imza anahtar çiftini periyodik değiştirmesidir. Her JWT'nin başlığında bir `kid` (key id) bulunur; **JWKS** uç noktası da o an geçerli açık anahtarları `kid`'leriyle yayınlar. Resource Server bir token'ı doğrularken `kid`'e karşılık gelen anahtarı JWKS önbelleğinden bulur; bulamazsa JWKS'i yeniden çeker (Spring'in `NimbusJwtDecoder`'ı bunu otomatik yapar). Sağlayıcı rotasyonu **kademeli** yürütür: yeni anahtarla imzalamaya başlamadan önce eski ve yeni anahtarı bir süre birlikte yayınlar, böylece dolaşımdaki eski token'lar süreleri dolana dek doğrulanabilir. Sonuç: uygulamayı yeniden dağıtmaya gerek kalmaz — bu, statik anahtar dosyalarına göre JWKS'in en büyük avantajıdır.
