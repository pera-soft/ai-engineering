---
slug: secure-coding-kripto
baslik: "Güvenli Kodlama & Kriptografi: Injection, Parola Hashleme, Simetrik/Asimetrik ve TLS"
kademe: İleri
sira: 190
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

**Şifreleme** (encryption), bir metni yalnız doğru **anahtarı** olanın geri okuyabileceği anlaşılmaz bir hâle çevirmektir. Anahtar varsa geri çevrilir — mektubu kilitli bir kutuya koymak gibi. **Hash** (özet) ise başka bir şeydir: girdiyi sabit uzunlukta bir damgaya çevirir ve **geri döndürülemez**. Eti kıymaya çevirmek gibi: kıymadan ete dönemezsin, ama aynı etten hep aynı kıyma çıkar, dolayısıyla iki şeyin aynı olup olmadığını anlarsın. Bu ayrım mülakatın en sık takıldığı yerdir.

Güvenli kodlama bunları doğru yerde kullanmayı öğretir. En temel refleks tek cümledir: **veri, koddan ayrıdır**. Kullanıcının yazdığı metni asla çalıştırılabilir bir komutun/sorgunun parçası hâline getirmezsin. Parolayı asla geri çözülebilir biçimde saklamazsın. Anahtarı asla kodun içine gömmezsin.

### Temel kavramlar

- **Injection** (enjeksiyon): kullanıcı verisinin, çalıştırılan bir metnin (SQL sorgusu, kabuk komutu) içine yapışıp **anlamını değiştirmesi**. `ad` alanına `' OR '1'='1` yazan biri sorgunun tüm satırları döndürmesini sağlar.
- **Parametreli sorgu** (prepared statement): sorguda değerin yerine `?` bırakıp değeri ayrıca bağlamak (`setString(1, ad)`). Sürücü değeri daima **veri** olarak taşır, asla SQL olarak yorumlamaz — enjeksiyon yapısal olarak kapanır. Kaçış karakteri temizlemek kırılgan bir yamadır; parametreleştirme yapısal çözümdür.
- **Command injection**: aynı hatanın kabuk (shell) hâli. `exec("convert " + dosyaAdi)` çağrısına `resim.png; rm -rf /` gelirse ikinci komut da çalışır. Çözüm komutu ve argümanları **dizi olarak** vermektir (`ProcessBuilder("convert", dosyaAdi, "out.png")`); bu, kabuk ayrıştırmasını tümüyle atlar.
- **Allowlist / denylist**: "yalnız beklenen geçerli biçimi kabul et" (izin listesi) ile "bilinen kötüleri engelle" (yasak listesi).
- **Salt** (tuz): her parolaya eklenen, kullanıcıya özgü rastgele değer.
- **Simetrik şifreleme** (AES): tek anahtar hem şifreler hem çözer; çok hızlı, büyük veri için.
- **Asimetrik şifreleme** (RSA/ECC): açık ve gizli anahtardan oluşan çift; anahtar dağıtımını ve dijital imzayı çözer ama yavaştır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Parola: hızlı hash yanlış, yavaş hash doğru">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Parola saklama: hız düşmandır</text>
  <rect x="30" y="46" width="270" height="126" rx="12" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.5"/>
  <text x="165" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#B5504F" font-family="system-ui">MD5 / SHA-256 (tuzsuz)</text>
  <text x="165" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">çok hızlı → GPU dostu</text>
  <text x="165" y="122" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">milyar deneme/sn</text>
  <text x="165" y="144" text-anchor="middle" font-size="11" fill="#B5504F" font-family="system-ui">→ kaba kuvvet ucuz</text>
  <rect x="340" y="46" width="270" height="126" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Argon2 / bcrypt (+ salt)</text>
  <text x="475" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">kasıtlı yavaş + hafıza-zorlu</text>
  <text x="475" y="122" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">her parola ayrı tuz</text>
  <text x="475" y="144" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">→ kaba kuvvet ekonomik değil</text>
</svg>
```

### Nasıl çalışır?

**Parola neden şifrelenmez, hashlenir?** Şifreleme geri çözülebilir: anahtar sızarsa tüm parolalar açığa çıkar. Hash tek yönlüdür — sistem parolayı hiç bilmeden, girilen parolanın özetini kayıtlı özetle karşılaştırarak doğrular. Ama her hash uygun değildir: `MD5` ve `SHA-256` **genel amaçlı ve çok hızlıdır**; saldırgan sızdırdığı özet listesine karşı GPU ile saniyede milyarlarca tahmin dener. Parolada **hız düşmandır**. Doğru araç **kasıtlı yavaş çalışan ve bol bellek isteyen** (memory-hard) algoritmalardır: **Argon2** (güncel tercih), **bcrypt**, **scrypt**. Bol bellek istemeleri önemlidir, çünkü GPU'ların güçlü yanı hesap gücüdür, bellek değil. Bunlar her denemeyi pahalı kılarak kaba kuvvet denemesini (brute force) ekonomik olmaktan çıkarır.

**Tuz (salt) neden şart?** Tuzsuz hashte aynı parolayı kullanan iki kişi aynı özete düşer ve önceden hesaplanmış devasa tablolarla (**rainbow table**) özet anında çözülür. Her kullanıcıya özgü rastgele tuz, aynı parolaları farklı özetlere dağıtır ve hazır tabloları işe yaramaz kılar.

**Allowlist neden denylist'e üstün?** Kötü girdilerin kümesi **açık uçludur**: saldırgan URL kodlaması, çift kodlama veya Unicode varyantıyla listede olmayan bir biçim bulur; yasak listesi hep bir adım geride kalır. İzin listesi ise **kapalı bir kümedir** ("yalnız `[a-z0-9_]`, 3-20 karakter") ve tanımı gereği dışındaki her şeyi reddeder. Gerekçe performans veya kod miktarı değil, **kapsamdır**.

**TLS ikisini neden birleştirir?** Simetrik şifreleme hızlıdır ama iki tarafın **aynı gizli anahtarı** güvenle paylaşması gerekir — internette bu tek başına imkânsızdır. Asimetrik şifreleme tam bu boşluğu doldurur: sertifikayla sunucunun kimliğini doğrular ve güvensiz kanalda anahtar üzerinde anlaşmayı mümkün kılar, ama yavaştır. **TLS el sıkışması** (handshake) önce asimetrik kriptoyla (ya da Diffie-Hellman anahtar değişimiyle) bir **simetrik oturum anahtarı** üzerinde anlaşır; sonra tüm uygulama verisi bu anahtarla hızlı simetrik şifreyle akar. Her biri güçlü olduğu işte kullanılır.

**Anahtar yönetimi.** Anahtar kodda ya da JAR içinde tutulmaz. Derlemek gizlemek değildir; bytecode'dan sabitler kolayca çıkarılır. Üstelik koda gömülü anahtar **değiştirilemez**, çünkü değiştirmek için yeniden derleyip dağıtmak gerekir.

Doğru yol dört adımlıdır. Anahtarı bir **KMS/HSM**'de veya kasada sakla. (KMS = Key Management Service, anahtar yönetim servisi; HSM = Hardware Security Module, anahtarı içinde tutan özel donanım.) Anahtarı uygulamaya çalışma zamanında ver. Erişimi en az yetkiyle sınırla. Son olarak belirli aralıklarla **yenile** (rotate). Anahtarın uzunluğunu artırmak, koda gömülü olma sorununu çözmez.

Pratik kural: girdiyi **allowlist** ile doğrula, sorguyu **parametreleştir**, parolayı **Argon2/bcrypt** ile tuzlayarak hashle, anahtarı **KMS**'te tut ve döndür.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="SQL injection: string birleştirme yerine parametreli sorgu">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Injection: veriyi koddan ayır</text>
  <rect x="30" y="42" width="270" height="132" rx="12" fill="none" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="165" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">String birleştirme</text>
  <text x="165" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"... WHERE ad = '" + ad</text>
  <text x="165" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">girdi: ' OR '1'='1</text>
  <text x="165" y="136" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">veri → SQL olarak yorumlanır</text>
  <text x="165" y="160" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">→ sorgunun anlamı değişir</text>
  <rect x="340" y="42" width="270" height="132" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Parametreli sorgu</text>
  <text x="475" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"... WHERE ad = ?"</text>
  <text x="475" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">setString(1, ad)</text>
  <text x="475" y="136" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">değer daima veri olarak bağlanır</text>
  <text x="475" y="160" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">→ enjeksiyon yapısal olarak kapalı</text>
</svg>
```

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="TLS: asimetrik el sıkışma, simetrik veri akışı">
  <defs>
    <marker id="seccode-flow" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">TLS: asimetrik kurar, simetrik taşır</text>
  <text x="90" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">İstemci</text>
  <text x="550" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Sunucu</text>
  <line x1="90" y1="66" x2="90" y2="190" stroke="#5D7789" stroke-width="1.3"/>
  <line x1="550" y1="66" x2="550" y2="190" stroke="#5D7789" stroke-width="1.3"/>
  <rect x="150" y="74" width="340" height="46" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/>
  <text x="320" y="94" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">1) Asimetrik el sıkışma (yavaş, güvenli)</text>
  <text x="320" y="111" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sertifikayla kimlik + simetrik oturum anahtarı üzerinde anlaş</text>
  <line x1="90" y1="132" x2="548" y2="132" stroke="#6B3FA0" stroke-width="1.4" marker-end="url(#seccode-flow)"/>
  <rect x="150" y="146" width="340" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="320" y="166" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">2) Simetrik veri akışı (hızlı)</text>
  <text x="320" y="183" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tüm uygulama verisi oturum anahtarıyla AES ile şifrelenir</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Bu JDBC kodundaki açık ne?"** Beklenen: SQL injection; çözüm `PreparedStatement` + `?`. `getParameter` hiçbir temizleme yapmaz; kaçış karakteri yamaları yapısal çözüm değildir.
- **"Parolayı nasıl saklarsın?"** Beklenen: kullanıcıya özgü tuz + Argon2/bcrypt. SHA-256 kırık değildir ama parola için **fazla hızlıdır**; AES ile şifrelemek daha kötüdür (geri çözülebilir).
- **"TLS neden hem asimetrik hem simetrik kullanır?"** Beklenen: asimetrik anahtar dağıtımı ve kimlik için, simetrik toplu veri için. Roller ters çevrilirse cevap yanlıştır.
- **"Kullanıcı girdisi kabuğa gidiyor, ne yaparsın?"** Beklenen: `ProcessBuilder` ile argüman dizisi + allowlist doğrulama. String `exec` güvenli değildir.
- **"Allowlist mi denylist mi, neden?"** Beklenen: kötünün kümesi açık uçlu, iyinin kümesi kapalı. Gerekçe performans/kod miktarı değil.
- **"Anahtar JAR'a gömülü, sorun ne?"** Beklenen: okunabilir ve döndürülemez; KMS/HSM'e taşı, rotate et. Anahtarı uzatmak çözüm değil.

### Tek cümlede

Güvenli kodlama, **veriyi koddan ayırmak** (parametreli sorgu, argüman dizisi, allowlist), parolayı **tuzlu ve kasıtlı yavaş** hashlemek, şifrelemede her algoritmayı güçlü olduğu işte kullanmak ve anahtarı **koddan çıkarıp döndürmektir**.

# seccode-1 | İleri
## Soru
Aşağıdaki JDBC kodu hangi zafiyeti içerir ve doğru düzeltmesi nedir?

```java
String ad = request.getParameter("ad");
String sql = "SELECT * FROM kullanici WHERE ad = '" + ad + "'";
stmt.executeQuery(sql);
```
## A
Command injection — komut `ProcessBuilder` ile argüman dizisi olarak çalıştırılmalı
🤖 Bu bir shell komutu değil, SQL sorgusu; ilgili zafiyet SQL injection.
## B
Zafiyet yok; `getParameter` gelen girdiyi çerçeve düzeyinde zaten temizler
🤖 `getParameter` ham girdiyi döner, hiçbir temizleme yapmaz; sorgu birleştirmesi açık kapı bırakır.
## C
SQL injection — girdi sorgu metnine birleştiriliyor, `PreparedStatement` kullanılmalı
🤖 Doğru: parametreli sorgu veriyi koddan ayırır; `ad`'ın içeriği artık SQL olarak yorumlanmaz.
## D
XSS — değer ekrana basılmadan önce HTML olarak kaçışlanmalı (escape)
🤖 XSS çıktı tarafındadır; buradaki kusur girdinin SQL'e enjekte edilmesidir.
## Doğru
C
## Açıklama
Kullanıcı girdisi (`ad`) doğrudan SQL metnine **birleştirildiği** için saldırgan `' OR '1'='1` gibi bir değerle sorgunun anlamını değiştirebilir — klasik **SQL injection**. `getParameter` (B) ham veriyi döndürür, hiçbir doğrulama/temizleme yapmaz. Doğru çözüm **parametreli sorgu** (`PreparedStatement` + `WHERE ad = ?` + `setString`): sürücü değeri **veri** olarak bağlar, asla çalıştırılabilir SQL olarak yorumlamaz. String temizleme/kaçışlama kırılgandır; parametreleştirme yapısal çözümdür. Bu shell komutu (A) veya çıktı kaçışı (D XSS) sorunu değildir.

# seccode-2 | İleri
## Soru
Bir sistem kullanıcı parolalarını `SHA-256(parola)` ile saklıyor (tuz yok). Bu neden yetersizdir ve ne yapılmalı?
## A
SHA-256 parola için çok hızlı ve tuzsuz; kullanıcıya özgü tuzla Argon2/bcrypt kullanılmalı
🤖 Doğru: hız ve tuzsuzluk kaba kuvveti ekonomik kılar; çözüm kasıtlı yavaş, tuzlanmış hash.
## B
SHA-256 kriptografik olarak kırılmıştır, hemen SHA-512 ailesine geçilmelidir
🤖 SHA-256 kriptografik olarak kırık değil; sorun parola için fazla hızlı ve tuzsuz olması.
## C
Parola hash yerine AES ile şifrelenmeli, böylece gerektiğinde geri çözülebilir
🤖 Parolayı geri çözülebilir saklamak daha kötüdür; anahtar sızarsa tüm parolalar açılır. Parola tek yönlü hashlenmeli.
## D
Sorun yok; tuz yalnız simetrik şifrelemede gereken ayrı bir kavramdır
🤖 Tuz tam da parola hashlemenin olmazsa olmazıdır; aynı parolaların aynı hash'e düşmesini ve rainbow table'ı engeller.
## Doğru
A
## Açıklama
`SHA-256` genel amaçlı bir **hızlı** özet fonksiyonudur; parola için bu bir kusurdur çünkü saldırgan GPU ile saniyede milyarlarca tahmin dener. Tuz olmaması ise **rainbow table** (önceden hesaplanmış hash tabloları) saldırısını ve aynı parolaların aynı hash'e düşmesini mümkün kılar. Doğru yaklaşım: her kullanıcıya **özgü tuz** + **kasıtlı yavaş, hafıza-zorlu** algoritma (**Argon2** tercih, **bcrypt**/scrypt kabul). Parolayı AES ile **şifrelemek** (C) yanlıştır: şifreleme geri çözülebilir olduğundan anahtar sızarsa tüm parolalar açığa çıkar; parola **tek yönlü** saklanmalıdır. SHA-256 kırık değildir (B), tuz parolada şarttır (D).

# seccode-3 | İleri
## Soru
TLS el sıkışması (handshake) neden hem asimetrik hem simetrik şifreleme kullanır? Temel gerekçe nedir?
## A
Asimetrik daha güvenli olduğu için tüm veri onunla şifrelenir, simetrik yalnız yedektir
🤖 Tersi: asimetrik yavaştır, tüm veriyi onunla şifrelemek verimsizdir; asıl veri simetrik akar.
## B
Asimetrik anahtar dağıtımını ve kimliği çözer ama yavaştır; veri simetrik anahtarla akar
🤖 Doğru: asimetrik anahtar kurulumu için, simetrik ise toplu veri için — her biri güçlü olduğu yerde.
## C
İkisi aynı işi yapar; TLS geriye dönük uyumluluk için her ikisini de içerir
🤖 İkisi farklı problemleri çözer (anahtar dağıtımı vs toplu şifreleme); uyumluluk meselesi değil.
## D
Simetrik şifreleme kimlik doğrulama yapar, asimetrik ise toplu veriyi şifreler
🤖 Roller ters: kimlik doğrulama/anahtar değişimi asimetrik tarafta, toplu veri şifreleme simetrik tarafta.
## Doğru
B
## Açıklama
İki şifreleme türü farklı problemleri çözer. **Asimetrik** (RSA/ECC) güvensiz bir kanalda **anahtar dağıtımını** ve sunucunun **kimliğini** (sertifika) çözer — ama hesaplama olarak pahalıdır, büyük veriyi onunla şifrelemek verimsizdir. **Simetrik** (AES) çok hızlıdır ama önce iki tarafın **aynı gizli anahtarı** güvenle paylaşması gerekir. TLS ikisini birleştirir: el sıkışmada asimetrik kriptoyla (veya Diffie-Hellman anahtar değişimiyle) bir **simetrik oturum anahtarı** üzerinde güvenle anlaşılır, ardından tüm uygulama verisi bu anahtarla **hızlı simetrik** şifreyle akar. Böylece hem güvenli anahtar kurulumu hem yüksek verim elde edilir.

# seccode-4 | İleri
## Soru
Bir servis, kullanıcıdan gelen dosya adını doğrudan bir kabuk komutuna geçiriyor:

```java
Runtime.getRuntime().exec("convert " + dosyaAdi + " out.png");
```
Buradaki zafiyet ve en sağlam düzeltme nedir?
## A
Yol geçişi (path traversal); `dosyaAdi` doğrulanıp mutlak yola çevrilmeli
🤖 Path traversal ilgili bir risk olabilir ama buradaki asıl kusur kabuğa girdi enjeksiyonu.
## B
Zafiyet yok; `exec` verilen metni kabuktan geçirmeden güvenle çalıştırır
🤖 String `exec` girdiyi kabuk gibi ayrıştırabilir; `; rm -rf` gibi yükler çalıştırılabilir.
## C
Bellek taşması (buffer overflow); girdi uzunluğu sabit bir sınırla kesilmeli
🤖 Java'da yönetimli bellek buffer overflow'u bu bağlamda üretmez; sorun komut enjeksiyonu.
## D
Command injection; komut ve argümanlar dizi olarak `ProcessBuilder` ile verilmeli
🤖 Doğru: argüman dizisi kabuk ayrıştırmasını atlar; ek olarak girdi izin listesiyle doğrulanır.
## Doğru
D
## Açıklama
Kullanıcı girdisi bir **kabuk komut metnine** birleştirildiği için saldırgan `resim.png; rm -rf /` gibi bir değerle ek komut çalıştırabilir — **command injection**. En sağlam çözüm komutu ve argümanları **dizi olarak** vermektir (`new ProcessBuilder("convert", dosyaAdi, "out.png")`): bu, kabuk yorumlamasını tümüyle atlar, `dosyaAdi` yalnız tek bir argüman olarak geçer, meta karakterlerin özel anlamı kalmaz. Ek katman olarak `dosyaAdi` bir **allowlist** ile (ör. yalnız beklenen uzantı ve karakter kümesi) doğrulanır. String `exec` (B) güvenli değildir; sorun path traversal (A) veya buffer overflow (C) değildir.

# seccode-5 | İleri
## Soru
Girdi doğrulamada "izin listesi (allowlist) yaklaşımı, yasak listesine (denylist) tercih edilir" tavsiyesinin temel gerekçesi nedir?
## A
Allowlist yalnız beklenen biçimi kabul eder; denylist açık uçlu kötü kümesini kapatamaz
🤖 Doğru: "iyiyi tanı" kapalı bir kümedir; "kötüyü say" açık uçludur ve daima eksik kalır.
## B
Denylist örüntü eşleme yüzünden daha yavaştır; allowlist performans için tercih edilir
🤖 Fark performans değil güvenlik kapsamıdır; denylist eksik kaldığı için terk edilir.
## C
Allowlist daha az kod gerektirir ve uzun vadede bakımı belirgin biçimde kolaydır
🤖 Gerekçe kod miktarı değil; asıl mesele denylist'in kaçınılmaz eksikliğidir.
## D
İkisi güvenlik açısından eşdeğerdir; seçim tümüyle ekip tercihine kalmıştır
🤖 Eşdeğer değiller: denylist açık uçlu tehdit uzayını kapatamaz, allowlist kapatır.
## Doğru
A
## Açıklama
**Denylist** (yasak listesi) "kötü olanı say ve engelle" der — ama kötü girdilerin kümesi **açık uçludur**: saldırgan yeni kodlamalar (URL-encode, çift kodlama, Unicode varyantları) veya listede olmayan payload'lar bulabilir; liste daima bir adım geride kalır. **Allowlist** (izin listesi) ise "yalnız beklenen geçerli biçimi kabul et" der — bu **kapalı bir kümedir** ve tanımı gereği dışındaki her şeyi reddeder. Bu yüzden güvenli kodlamada varsayılan yaklaşım allowlist'tir (ör. "yalnız `[a-z0-9_]`, 3-20 karakter"). Gerekçe performans (B), kod miktarı (C) değil; ikisi güvenlik açısından **eşdeğer değildir** (D).

# seccode-6 | İleri
## Soru
Bir ekip AES şifreleme anahtarını uygulama JAR'ının içine gömülü bir sabit (constant) olarak tutuyor. Anahtar yönetimi açısından temel sorun ve doğru yön nedir?
## A
AES artık zayıf bir algoritmadır; modern bir şifre olan ChaCha20'ye geçilmeli
🤖 AES güçlüdür; sorun algoritma değil, anahtarın kodda gömülü ve döndürülemez olması.
## B
Anahtar uzunluğu 256-bit'e çıkarılırsa gömülü tutmanın bir sakıncası kalmaz
🤖 Uzunluk gömülü sırrı korumaz; JAR'ı tersine çeviren herkes anahtarı okur.
## C
Kodda gömülü anahtar okunabilir ve döndürülemez; kasada tutulup çalışma zamanında verilmeli
🤖 Doğru: sır koddan ayrılır, dışarıdan enjekte edilir, izlenebilir ve döndürülebilir hale gelir.
## D
Sorun yok; JAR derlenmiş bytecode olduğu için içindeki anahtar okunamaz
🤖 Derleme şifreleme değildir; bytecode'dan sabitler kolayca çıkarılır.
## Doğru
C
## Açıklama
Sorun algoritmada değil **anahtar yönetiminde**. Koda gömülü bir anahtar (1) JAR'a erişen veya onu **tersine çeviren** (decompile) herkese açıktır — derleme gizleme değildir (D yanlış); (2) **döndürülemez** — sızarsa tüm veriyi yeniden şifrelemeden değiştirilemez; (3) sürüm kontrolüne ve yedeklere yayılır. Doğru yaklaşım: anahtarı bir **KMS/HSM** veya gizli kasada saklamak, uygulamaya **çalışma zamanında** vermek, erişimi **en az yetkiyle** sınırlamak ve **periyodik döndürmek**. Anahtar uzunluğunu artırmak (B) gömülü olma sorununu çözmez; AES zaten güçlüdür (A).
