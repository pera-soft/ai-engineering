---
slug: guvenlik-mimari-owasp
baslik: "Güvenlik Mimarisi: STRIDE, OWASP Top 10, BOLA/IDOR, En Az Yetki ve Zero Trust"
kademe: İleri
sira: 189
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

**Tehdit** (threat), bir saldırganın sisteminde yapmak isteyeceği kötü şeydir: başkasının hesabına girmek, veriyi değiştirmek, gizli bilgiyi okumak, sistemi çalışamaz hâle getirmek. Bir eve benzet: hırsızın kapıyı zorlaması, kilidi kopyalaması, camdan girmesi ayrı ayrı tehditlerdir. **Güvenlik mimarisi** de tam bunu yapar — evi yaptıktan sonra kapıya kilit takmak değil, **planı çizerken** hangi kapıların nereye açıldığını düşünmektir.

Neden mimari bir konu? Çünkü güvenlik sonradan yapıştırılamaz. Yanlış yerde verilen bir yetki, yanlış kurulan bir güven varsayımı ya da yanlış yere yazılmış bir parola, kodun her yerine sızar. Bu bölüm, tehditleri **sistematik** aramanın (threat modeling) ve en sık yapılan hataların listesini (OWASP) bilmenin yoludur.

### Temel kavramlar

- **Kimlik doğrulama** (authentication, kısaca **authn**): "**Sen kimsin?**" Kimliği parola, **yetki anahtarı (token)** ya da çok faktörlü doğrulama (MFA) ile kanıtlarsın. Kimliğini kanıtlaman, her şeyi yapabileceğin anlamına gelmez.
- **Yetkilendirme** (authorization, kısaca **authz**): "**Neyi yapabilirsin?**" Rol, sahiplik ve kapsam kontrolü. Otelde kimliğinle giriş yaparsın (authn), ama kart anahtarın yalnız **kendi odanı** açar (authz).
- **STRIDE**: tehdit modellemenin en yaygın kontrol listesidir. Altı tehdit türünün baş harflerinden oluşur. **S**poofing (kimlik taklidi), **T**ampering (veriyi kurcalama), **R**epudiation (yaptığını inkâr etme), **I**nformation disclosure (bilgi sızması), **D**enial of service (hizmeti durdurma), **E**levation of privilege (yetki yükseltme). Her tehdidin kendi karşılığı vardır; aşağıdaki diyagramda eşleştirilmiştir.
- **OWASP Top 10**: web güvenliğinde en sık görülen on zafiyet sınıfının fikir birliğiyle hazırlanan listesi. 2021 sürümünde bir numara **A01 Broken Access Control**'dür (bozuk erişim denetimi).
- **BOLA/IDOR**: A01'in en kritik alt sınıfı. Kullanıcının **kimliği doğrulanmıştır**, ama sunucu istenen **nesnenin** ona ait olduğunu kontrol etmez; `/fatura/1005` yerine `/fatura/1006` yazan biri başkasının faturasını okur.
- **En az yetki** (least privilege): her bileşen yalnız işini görecek kadar yetki alır — fazlası, ihlal anındaki **etki alanını** (blast radius) büyütür.
- **Sır yönetimi** (secrets management): parola, yetki anahtarı ve şifreleme anahtarı asla kodda veya git'te durmaz; bir **kasada** (vault) tutulur.
- **Sıfır güven** (zero trust): "ağın içinden geldi" bir güven gerekçesi değildir; her istek kimlik ve bağlamla yeniden doğrulanır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Authentication vs Authorization ayrımı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Authn ≠ Authz: iki ayrı kapı</text>
  <rect x="30" y="46" width="270" height="120" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="76" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">Authentication</text>
  <text x="165" y="100" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">"Sen kimsin?"</text>
  <text x="165" y="124" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">parola, token, MFA</text>
  <text x="165" y="144" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">kimliği kanıtla</text>
  <rect x="340" y="46" width="270" height="120" rx="12" fill="#F3E7DA" stroke="#B0642A" stroke-width="1.5"/>
  <text x="475" y="76" text-anchor="middle" font-size="14" font-weight="700" fill="#B0642A" font-family="system-ui">Authorization</text>
  <text x="475" y="100" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">"Neyi yapabilirsin?"</text>
  <text x="475" y="124" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">rol, sahiplik, kapsam</text>
  <text x="475" y="144" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">nesne düzeyinde kontrol</text>
  <text x="320" y="192" text-anchor="middle" font-size="11.5" fill="#B5504F" font-family="system-ui">Kimlik doğru ama yetki kontrol edilmezse → BOLA/IDOR</text>
</svg>
```

### Nasıl çalışır?

**Erişim kararı nerede verilir?** Her erişim kararı **sunucu tarafında** ve **nesne düzeyinde** verilir. İstemciden gelen `kullaniciId`, `rol` veya `X-Rol` gibi bir başlık **asla** güven kaynağı olamaz — istemci her şeyi gönderebilir. Yetki, sunucunun doğruladığı oturum kimliğinden (yetki anahtarının içindeki claim alanından) türetilir.

BOLA'nın düzeltmesi de budur: `fatura.sahibiId == oturum.kullaniciId` kontrolünü sunucuda yapmak, ya da sorguyu doğrudan `findByIdAndSahibiId(...)` biçimine çevirmek. Kimlik numarasını tahmin edilemez yapmak (UUID) bir çözüm değildir. Buna "belirsizlikle güvenlik" (security by obscurity) denir; numara loglardan sızarsa erişim yine açıktır.

**STRIDE nasıl uygulanır?** Tasarım diyagramını çizersin, veri akışlarının güven sınırlarını (trust boundary) işaretlersin ve her sınırda altı harfi tek tek sorarsın: "Burada biri kimlik taklidi yapabilir mi? Veriyi kurcalayabilir mi?..."

Her tehdidi doğru harfe eşlemek, doğru kontrolü seçmenin ön koşuludur. Başkasının kimliğine bürünme **Spoofing**'dir; karşılığı kimlik doğrulamadır. **Elevation of privilege** ise kimliği zaten belli olan bir aktörün fazladan hak kazanmasıdır; karşılığı yetkilendirmedir.

**En az yetki neden bu kadar önemli?** Bir servis veritabanına yönetici (admin) yetkisiyle bağlanıyorsa, o servis ele geçirildiğinde saldırgan **tüm veritabanını** okuyup silebilir. Aynı servis yalnız birkaç tabloda `SELECT`/`INSERT` yetkisiyle bağlansaydı, aynı ihlalin etkisi o birkaç tabloyla sınırlı kalırdı. İlke yetkiyi engellemek değil, **ihlal anındaki etki alanını küçültmektir**.

**Sıfır güven ne değiştirir?** Geleneksel **çevre (perimeter) güvenliği** "iç ağ güvenlidir, dışarısı tehlikelidir" varsayar. Bu varsayımı sömüren saldırının adı **yanal harekettir** (lateral movement): saldırgan tek bir zayıf iç servise girer, sonra kimse kimliğini sormadığı için ağda serbestçe yayılır.

Sıfır güvenin özü şudur: "**asla güvenme, her zaman doğrula**". Pratikte üç şey demektir: servisler arası **karşılıklı TLS (mTLS)**, kısa ömürlü kimlik anahtarları ve her çağrıda yetki kontrolü. Trafiği şifrelemek tek başına yetmez; şifreleme gizlilik sağlar, kimlik doğrulamanın yerine geçmez.

**Sırlar.** Bir parola git'e girdiyse `.gitignore` eklemek geçmişi temizlemez, base64 ise şifreleme değil geri çevrilebilir kodlamadır. Doğru yön iki adımlıdır: sırrı kasaya taşıyıp çalışma zamanında enjekte etmek **ve** sızmış sırrı döndürmek (rotate).

```svg
<svg viewBox="0 0 640 296" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="STRIDE: altı tehdit ve karşılıkları">
  <defs>
    <marker id="owasp-map" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">STRIDE: her tehdide bir kontrol</text>
  <text x="160" y="46" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">Tehdit</text>
  <text x="480" y="46" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Karşılık</text>
  <rect x="24" y="56" width="272" height="30" rx="6" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="160" y="76" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">S — Spoofing (kimlik taklidi)</text>
  <line x1="296" y1="71" x2="340" y2="71" stroke="#5D7789" stroke-width="1.4" marker-end="url(#owasp-map)"/>
  <rect x="344" y="56" width="272" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="480" y="76" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Kimlik doğrulama (MFA)</text>
  <rect x="24" y="94" width="272" height="30" rx="6" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="160" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">T — Tampering (kurcalama)</text>
  <line x1="296" y1="109" x2="340" y2="109" stroke="#5D7789" stroke-width="1.4" marker-end="url(#owasp-map)"/>
  <rect x="344" y="94" width="272" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="480" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Bütünlük / imza-hash</text>
  <rect x="24" y="132" width="272" height="30" rx="6" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="160" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">R — Repudiation (inkâr)</text>
  <line x1="296" y1="147" x2="340" y2="147" stroke="#5D7789" stroke-width="1.4" marker-end="url(#owasp-map)"/>
  <rect x="344" y="132" width="272" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="480" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Denetim günlüğü (audit log)</text>
  <rect x="24" y="170" width="272" height="30" rx="6" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="160" y="190" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">I — Information disclosure</text>
  <line x1="296" y1="185" x2="340" y2="185" stroke="#5D7789" stroke-width="1.4" marker-end="url(#owasp-map)"/>
  <rect x="344" y="170" width="272" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="480" y="190" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Şifreleme</text>
  <rect x="24" y="208" width="272" height="30" rx="6" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="160" y="228" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">D — Denial of service</text>
  <line x1="296" y1="223" x2="340" y2="223" stroke="#5D7789" stroke-width="1.4" marker-end="url(#owasp-map)"/>
  <rect x="344" y="208" width="272" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="480" y="228" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Hız sınırlama / kota</text>
  <rect x="24" y="246" width="272" height="30" rx="6" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="160" y="266" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">E — Elevation of privilege</text>
  <line x1="296" y1="261" x2="340" y2="261" stroke="#5D7789" stroke-width="1.4" marker-end="url(#owasp-map)"/>
  <rect x="344" y="246" width="272" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="480" y="266" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Yetkilendirme (authz)</text>
</svg>
```

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="En az yetki: ele geçirme anında etki alanı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">En az yetki: ihlal anında etki alanı (blast radius)</text>
  <text x="165" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Admin yetkisiyle bağlan</text>
  <circle cx="165" cy="120" r="52" fill="none" stroke="#8A2E2E" stroke-width="1.6" stroke-dasharray="5 4"/>
  <circle cx="165" cy="120" r="20" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="165" y="124" text-anchor="middle" font-size="10" fill="#B26B00" font-family="system-ui">servis</text>
  <text x="165" y="192" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">ele geçince tüm DB açık</text>
  <text x="475" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Yalnız gereken yetki</text>
  <circle cx="475" cy="120" r="24" fill="none" stroke="#25804C" stroke-width="1.6" stroke-dasharray="5 4"/>
  <circle cx="475" cy="120" r="20" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="124" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">servis</text>
  <text x="475" y="192" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">etki birkaç tabloyla sınırlı</text>
</svg>
```

### Mülakatta ne sorulur?

- **"`{id}`'yi değiştiren herkes başkasının faturasını okuyor — bu ne, nasıl düzeltilir?"** Beklenen: BOLA/IDOR; nesne düzeyinde sahiplik kontrolü. Enjeksiyon değil, token ömrü değil, UUID değil.
- **"'Saldırgan başka kullanıcının kimliğine bürünüyor' STRIDE'da hangi harf?"** Beklenen: Spoofing → authentication/MFA. Tampering veriyle, Repudiation inkârla, Elevation of privilege fazladan yetkiyle ilgilidir.
- **"Servis DB'ye admin ile bağlanıyor, sorun ne?"** Beklenen: en az yetki ihlali; ihlal anında blast radius devasa büyür.
- **"'İç ağdan geldiyse güvenilir' varsayımı hangi modele aykırı?"** Beklenen: sıfır güven (zero trust); ağ konumu güven kaynağı değildir, yanal hareket bu varsayımı sömürür.
- **"Yetkiyi neye göre veriyorsun?"** Beklenen: istemcinin gönderdiği role/id'ye değil, sunucuda doğrulanmış oturum kimliğine göre.
- **"Parola `application.yml`'de commit'li, ne yaparsın?"** Beklenen: kasaya taşı **ve** parolayı döndür; `.gitignore` geçmişi silmez, base64 gizlemez.

### Tek cümlede

Güvenlik mimarisi altı işten oluşur. Tehditleri **STRIDE** ile sistematik aramak. Kimlik doğrulamayı (authn) yetkilendirmeden (authz) ayırmak. Her erişim kararını **sunucuda ve nesne düzeyinde** vermek. Yetkiyi **en aza** indirmek. Sırrı **kasada** tutmak. Ve güveni ağ konumundan değil **doğrulanmış kimlikten** almak.

# owasp-1 | İleri
## Soru
Bir REST API'de `GET /api/fatura/{id}` uç noktası, geçerli JWT taşıyan her kullanıcıya isteği yanıtlıyor; `{id}` değiştiren biri başkalarının faturalarını okuyabiliyor. Bu hangi zafiyettir ve doğru düzeltme nedir?
## A
BOLA/IDOR — sunucu, faturanın sahibinin istek sahibi olduğunu nesne düzeyinde doğrulamalı
🤖 Doğru: kimlik doğrulanmış ama nesne düzeyinde yetkilendirme yok; sahiplik sunucuda kontrol edilmeli.
## B
SQL injection — sorgu parametreleştirilip `PreparedStatement` ile bağlanmalı
🤖 Burada enjeksiyon yok; sorun kimliğin başkasının nesnesine erişebilmesi, yani erişim denetimi.
## C
Broken authentication — JWT'nin ömrü kısaltılıp yenileme akışı eklenmeli
🤖 Kimlik doğrulama çalışıyor (JWT geçerli); eksik olan yetkilendirme, token ömrü değil.
## D
Güvenli değil ama kabul edilebilir; `{id}` tahmin edilemezse (UUID) sorun kalmaz
🤖 UUID yalnız "belirsizlikle güvenlik"tir; yetki kontrolü yoksa sızan/tahmin edilen id yine erişim verir.
## Doğru
A
## Açıklama
Bu, OWASP Top 10'un bir numarası **A01 Broken Access Control**'ün en yaygın biçimi olan **BOLA/IDOR**'dur: kullanıcı **kimliği** doğrulanmıştır (geçerli JWT) ama sunucu, istenen **nesnenin** o kullanıcıya ait olduğunu doğrulamaz. Düzeltme, her istekte **nesne düzeyinde yetkilendirme**: `fatura.sahibiId == oturum.kullaniciId` kontrolünü sunucuda yapmak. UUID kullanmak (D) yalnız "belirsizlikle güvenlik"tir — id sızarsa veya loglardan görülürse erişim yine açıktır. Sorun enjeksiyon (B) veya kimlik doğrulama (C) değil, **yetkilendirmedir**.

# owasp-2 | İleri
## Soru
Yeni bir ödeme mikroservisini tasarlarken güvenliği en baştan düşünmek için STRIDE tehdit modellemesi yapıyorsun. "Bir saldırgan başka kullanıcının kimliğine bürünüp onun adına işlem yapabilir" tehdidi STRIDE'ın hangi harfine denk gelir ve birincil karşılığı nedir?
## A
Tampering — verinin imzayla bütünlüğünü korumak
🤖 Tampering veriyi kurcalamaktır; kimliğe bürünme değil. Yanlış eşleme.
## B
Spoofing — güçlü kimlik doğrulama (authentication, MFA)
🤖 Doğru: kimliğe bürünme (spoofing) tam da authentication ile karşılanır.
## C
Repudiation — denetim günlüğü tutmak
🤖 Repudiation işlemi inkâr etmektir; karşılığı audit log'dur, kimliğe bürünme değil.
## D
Elevation of privilege — rol tabanlı yetki
🤖 Yetki yükseltme mevcut kimlikle daha fazla hak almaktır; başkasının kimliğine bürünme spoofing'dir.
## Doğru
B
## Açıklama
STRIDE'da başka kullanıcının **kimliğine bürünmek** **Spoofing**'dir ve birincil karşı önlemi güçlü **kimlik doğrulamadır** (authentication, çok faktörlü doğrulama/MFA, güvenli oturum yönetimi). Diğer harfler farklı tehditlerdir: **Tampering** veri bütünlüğü (imza/hash), **Repudiation** inkâr edememe (audit log), **Elevation of privilege** ise zaten kimliği belli bir aktörün **fazladan yetki** kazanmasıdır — bunun karşılığı yetkilendirme kontrolleridir. Tehdidi doğru harfe eşlemek, doğru kontrolü seçmenin ön koşuludur.

# owasp-3 | İleri
## Soru
Bir servis, veritabanına tam yönetici (admin) yetkisiyle bağlanan tek bir kimlik bilgisi kullanıyor; oysa yalnız birkaç tabloda `SELECT`/`INSERT` yapıyor. Bu hangi ilkeyi ihlal eder ve neden risklidir?
## A
Zero trust — servisler ağ içinden gelen çağrılara körü körüne güvenmemeli
🤖 Zero trust ağ güveniyle ilgilidir; buradaki sorun verilen yetkinin gereğinden geniş olması.
## B
Defense in depth — tek bir güvenlik katmanı hiçbir zaman yeterli değildir
🤖 İlgili bir ilke ama asıl ihlal, ihtiyaçtan fazla (admin) yetki verilmesidir: en az yetki.
## C
En az yetki (least privilege) — yetki, servisin gerçekten yaptığı işe göre daraltılmalı
🤖 Doğru: bileşen yalnız gerektiği kadar yetki almalı; fazla yetki ihlal anında etki yarıçapını (blast radius) büyütür.
## D
Fail-safe defaults — sistem hata anında güvenli tarafa düşecek biçimde kurulmalı
🤖 Bu ayrı bir ilkedir; buradaki kusur varsayılan davranış değil, kalıcı olarak geniş verilen yetkidir.
## Doğru
C
## Açıklama
Bu, **en az yetki** (least privilege) ilkesinin ihlalidir: bir bileşen yalnız görevini yapacak **asgari** yetkiyi almalı. Servis sadece birkaç tabloda `SELECT`/`INSERT` yapıyorsa, DB kimliğine yalnız o hakları vermek gerekir. Admin yetkisiyle bağlanmak, servis ele geçirildiğinde (ör. injection veya sızan kimlik) saldırganın **tüm veritabanını** okumasına/silmesine kapı açar — yani ihlalin **etki yarıçapını** (blast radius) devasa büyütür. Zero trust (A) ve fail-safe defaults (D) farklı ilkelerdir; defense in depth (B) ilişkili olsa da buradaki doğrudan kusur aşırı yetkidir.

# owasp-4 | İleri
## Soru
"Kurumsal ağın içinden gelen istekler güvenilir sayılır, iç servisler birbirini doğrulamadan çağırır" varsayımı hangi modele aykırıdır ve zero trust bunun yerine ne önerir?
## A
Zero trust'a aykırıdır; güven ağ konumundan değil, her istekte doğrulanan kimlikten gelir
🤖 Doğru: "asla güvenme, her zaman doğrula"; iç/dış ayrımı değil, her çağrının kimliği esastır.
## B
Zero trust'a uygundur; kurumsal iç ağ zaten sistemin asıl güvenlik sınırıdır
🤖 Tam tersi: iç ağı güvenli saymak geleneksel çevre (perimeter) modelidir, zero trust bunu reddeder.
## C
Least privilege'e aykırıdır; çözüm tüm iç trafiği uçtan uca şifrelemektir
🤖 Şifreleme tek başına kimliği doğrulamaz; asıl ihlal ağ konumuna güvenmek — bir zero trust konusu.
## D
Hiçbir ilkeye aykırı değildir; iç ağ trafiği pratikte düşük riskli sayılır
🤖 Yanal hareket (lateral movement) saldırıları tam da bu varsayımı sömürür; iç ağ otomatik güvenli değildir.
## Doğru
A
## Açıklama
İç ağı güvenli sayıp iç çağrıları doğrulamadan kabul etmek **geleneksel çevre (perimeter) güvenliği** varsayımıdır ve **zero trust** ilkesine doğrudan aykırıdır. Zero trust'ın özü "**asla güvenme, her zaman doğrula**": güven ağ konumundan (iç/dış) değil, her isteğin **doğrulanabilir kimliğinden ve bağlamından** gelir. Pratikte bu, servisler arası **karşılıklı TLS (mTLS)**, kısa ömürlü kimlik token'ları ve her çağrıda yetki kontrolü demektir. Perimeter varsayımının tehlikesi **yanal hareket**: saldırgan tek bir iç servise girdiğinde, doğrulama olmadığı için ağda serbestçe yayılır (D'nin yanlışlığı). Şifreleme (C) gizliliği sağlar ama kimlik doğrulamanın yerine geçmez.

# owasp-5 | İleri
## Soru
Aşağıdaki Spring kontrolcüsü hangi güvenlik hatasını içerir?

```java
@GetMapping("/api/rapor")
public List<Rapor> raporlar(@RequestHeader("X-Rol") String rol,
                            @RequestParam Long kullaniciId) {
    if ("ADMIN".equals(rol)) return raporRepo.findAll();   // (*)
    return raporRepo.findByKullaniciId(kullaniciId);
}
```
## A
(*) satırı yetki kararını istemcinin gönderdiği `X-Rol` başlığına dayandırıyor
🤖 Doğru: istemci her başlığı uydurabilir; `X-Rol: ADMIN` gönderen herkes tüm raporları alır.
## B
Kusur yok; başlık HTTPS ile şifrelendiği için yolda değiştirilemez, güvenlidir
🤖 TLS başlığı yoldaki üçüncü kişiden korur, ama o başlığı yazan zaten istemcinin kendisidir.
## C
`findByKullaniciId` parametreleştirilmediği için SQL injection riski taşıyor
🤖 Spring Data'nın türetilmiş sorgusu parametrelidir; kusur enjeksiyon değil, güvenin kaynağı.
## D
Dönüş listesi sayfalanmadığı için büyük veride bellek ve gecikme sorunu doğuyor
🤖 Gerçek bir performans notu ama güvenlik kusuru değil; soru güvenlik hatasını soruyor.
## Doğru
A
## Açıklama
Kod, "bu kullanıcı yönetici mi?" sorusunu **istemcinin gönderdiği bir HTTP başlığına** sorarak yanıtlıyor. Oysa başlığı dolduran istemcinin kendisidir: `curl -H "X-Rol: ADMIN"` yazan herkes tüm raporları okur. Aynı kusur `kullaniciId`'nin sorgu parametresinden alınmasında da vardır — kimliğini kanıtlamış olmak, başkasının `kullaniciId`'siyle istek atmanı engellemez. Bu, **authn ile authz'yi karıştırmanın** klasik biçimidir: yetki kararı istemciden gelen veriye değil, **sunucunun doğruladığı oturum kimliğine** dayanmalıdır (`@AuthenticationPrincipal` / `SecurityContext` üzerinden token claim'i, ya da `@PreAuthorize("hasRole('ADMIN')")`). HTTPS (B) veriyi yolda korur ama başlığın yazarını değiştirmez; Spring Data türetilmiş sorgusu parametrelidir, enjeksiyon (C) yoktur; sayfalama (D) ayrı bir performans konusudur.

# owasp-6 | İleri
## Soru
Bir mikroservisin veritabanı parolası `application.yml` içinde düz metin yazılı ve depoya (repo) commit'lenmiş. En doğru düzeltme yönü nedir?
## A
Parolayı base64 ile kodlayıp dosyada öyle bırakmak ve commit'lemeyi sürdürmek
🤖 Base64 şifreleme değil, geri çevrilebilir kodlamadır; sırrı gizlemez, yalnız gözden kaçırır.
## B
Sırrı gizli kasaya (vault) taşıyıp çalışma zamanında enjekte etmek ve parolayı döndürmek
🤖 Doğru: sır koddan ayrılır, kasadan çalışma zamanında verilir; git geçmişine düştüğü için parola da değiştirilmelidir.
## C
Dosyayı `.gitignore`'a ekleyip içeriği olduğu gibi bırakmak, yeni commit'i engellemek
🤖 `.gitignore` yalnız gelecekteki eklemeleri etkiler; hâlihazırda commit'li sır git geçmişinde kalır.
## D
Parolayı bir ortam değişkenine taşıyıp değeri yine repodaki `.env` dosyasına yazmak
🤖 `.env` de commit'lenirse sır yine depodadır; taşıma yeri değişse de sızma sürer.
## Doğru
B
## Açıklama
Sırlar (secrets) **asla** kaynak koda veya git geçmişine girmemeli. Doğru çözümün iki parçası var: (1) sırrı bir **gizli kasada** (HashiCorp Vault, AWS/GCP Secrets Manager, Kubernetes Secret) tutup uygulamaya **çalışma zamanında** enjekte etmek; (2) parola git geçmişine düştüğü için artık **yakılmış** kabul edilip **döndürülmelidir** (rotate) — çünkü geçmişi klonlayan herkes onu görebilir. Base64 (A) kodlamadır, şifreleme değildir. `.gitignore` (C) geçmişteki commit'i temizlemez. `.env`'i commit'lemek (D) sırrı yine depoda bırakır. Bu, OWASP'ın "Cryptographic Failures / hard-coded secrets" başlığının tipik örneğidir.
