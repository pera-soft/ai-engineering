---
baslik: "Temeller Tazeleme: Secure Coding 101"
yazar: "Faz 0 — Mühendislik Temelleri"
tarih: "2026-07-10"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 4
---

### Bu metin ne anlatıyor?
Bu metin, yazdığın programın kötü niyetli birinin eline geçtiğinde ne olacağını düşünerek
kod yazmayı anlatıyor. Ana fikir tek cümlede: **dışarıdan gelen hiçbir veriye güvenme, ve
bir şey ters gittiğinde saldırgana bilgi verme.** Sırasıyla şunları göreceksin: gelen veriyi
denetleme, güvendiğin bölgeyle güvenmediğin bölgenin sınırı, en yaygın saldırı olan enjeksiyon
ve ona karşı parametreli sorgu, hataları güvenli yönetme, güvenli varsayılanlar, parolaların
nasıl saklanacağı ve şifrelemenin iki temel türüyle TLS.

## Neden bu özet
Güvenlik (security) sonradan eklenen bir özellik değildir; kodun **tasarımında** başlar.
Bir uygulamayı çalışır hale getirdikten sonra "üstüne" güvenlik eklemek genellikle daha
pahalıdır ve daha çok delik bırakır. Bu özet, her satır kod yazarken içselleştirilmesi gereken
temel refleksleri tazeler: girdiyi doğrula, güven sınırını bil, veriyi komuttan ayır, hatayı
sızdırmadan yönet, varsayılanları güvenli kur, sırları doğru sakla.

## Girdi doğrulama (input validation)
Dışarıdan gelen her veri — form alanı, URL parametresi, dosya adı, API gövdesi — aksi
kanıtlanana kadar **kötü niyetli** sayılır. Doğrulamanın altın kuralı: **izin-listesi
(allowlist) her zaman kara-listeden (denylist/blacklist) iyidir.**

- **Kara-liste** "şu kötü şeyleri engelle" der. Ama saldırganın bulacağı yeni bir kötü
  girdi biçimini asla önceden bilemezsin; liste hep eksik kalır.
- **İzin-listesi** "yalnızca şu geçerli biçime izin ver, gerisini reddet" der. Kapsam
  dar ve kapalıdır; bilinmeyen saldırı biçimleri de otomatik olarak dışarıda kalır.

Örnek: bir kullanıcı adı için "şu özel karakterleri yasakla" (kara-liste) yerine
"yalnızca `[a-z0-9_]` ve 3-20 karakter" (izin-listesi) kuralını uygula.

**Şerh:** "İzin-listesi her zaman iyidir" kuralının sınırı vardır. Serbest metin alanlarında
(yorum, mesaj, ürün açıklaması) kullanıcı zaten her karakteri yazabilmelidir; orada dar bir
izin-listesi kurulamaz. Bu alanlarda güvenlik girdiyi kısıtlayarak değil, veriyi
**kullanıldığı yere göre doğru biçimde kaçırarak** (escaping / output encoding) ve komuttan
ayırarak sağlanır — bir sonraki bölümün konusu. Ayrıca doğrulama tek başına yeterli bir
savunma değildir; katmanlardan yalnızca biridir.

## Güven sınırı (trust boundary)
**Güven sınırı**, güvenmediğin bir bölge ile güvendiğin çekirdeğin arasındaki çizgidir.
İstemci (client), ağ (network), üçüncü taraf servisler — hepsi sınırın **dışındadır** ve
oradan gelen hiçbir şeye güvenilmez. Doğrulama, tam da bu sınırın üstünde yapılır: veri
çekirdeğe girmeden önce temizlenir.

Kritik hata deseni: doğrulamayı istemci tarafında (tarayıcıda JavaScript ile) yapıp
sunucuda tekrar yapmamak. İstemci tarafı doğrulama yalnızca kullanıcı deneyimi (UX)
içindir; **gerçek güven sınırı sunucudadır.** Saldırgan istemciyi tümüyle atlayabilir.

Aşağıdaki diyagram, dış dünyadan gelen verinin çekirdeğe ulaşmadan önce sınırda
doğrulanmasını gösterir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Güven sınırı: dış dünyadan gelen veri sınırda doğrulanır, sonra uygulama çekirdeğine geçer">
  <defs>
    <marker id="sc-ok1" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="sc-ok2" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Güven sınırı: dışarıdan gelene güvenme</text>
  <rect x="24" y="70" width="150" height="80" rx="12" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="99" y="102" text-anchor="middle" font-size="13" font-weight="700" fill="#C77A17" font-family="system-ui">DIŞ DÜNYA</text>
  <text x="99" y="122" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">istemci · ağ</text>
  <text x="99" y="138" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">güvenilmez</text>
  <line x1="238" y1="60" x2="238" y2="160" stroke="#5D7789" stroke-width="2" stroke-dasharray="6 5"/>
  <text x="238" y="52" text-anchor="middle" font-size="11" font-weight="600" fill="#5D7789" font-family="system-ui">güven sınırı</text>
  <rect x="262" y="78" width="120" height="64" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="322" y="106" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">DOĞRULA</text>
  <text x="322" y="124" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">izin-listesi</text>
  <rect x="466" y="70" width="150" height="80" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="541" y="102" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">ÇEKİRDEK</text>
  <text x="541" y="122" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">uygulama</text>
  <text x="541" y="138" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">güvenilen bölge</text>
  <line x1="174" y1="110" x2="258" y2="110" stroke="#186399" stroke-width="1.8" marker-end="url(#sc-ok1)"/>
  <line x1="382" y1="110" x2="462" y2="110" stroke="#25804C" stroke-width="1.8" marker-end="url(#sc-ok2)"/>
  <text x="424" y="100" text-anchor="middle" font-size="10.5" font-weight="600" fill="#25804C" font-family="system-ui">temiz veri</text>
</svg>
```

## Girdi doğrulama akışı
Doğrulama tek bir "evet/hayır" kararıdır ama iki sonucu da düzgün ele almak gerekir.
Geçerli girdi işlenir; **geçersiz girdi hem reddedilir hem kayda (log) alınır.** Loglama
önemlidir: tekrarlayan geçersiz girdiler bir saldırı denemesinin erken işaretidir.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Girdi doğrulama akışı: girdi izin-listesiyle kontrol edilir, geçerliyse işlenir, değilse reddedilip loglanır">
  <defs>
    <marker id="sc-ok3" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="sc-ok4" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
    <marker id="sc-ok5" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#C77A17"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Girdi doğrulama akışı</text>
  <rect x="20" y="96" width="120" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="80" y="122" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">GİRDİ</text>
  <text x="80" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">dışarıdan</text>
  <rect x="180" y="90" width="140" height="68" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="250" y="118" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">İZİN-LİSTESİ</text>
  <text x="250" y="136" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">geçerli biçim mi?</text>
  <line x1="140" y1="124" x2="176" y2="124" stroke="#5D7789" stroke-width="1.8" marker-end="url(#sc-ok3)"/>
  <rect x="470" y="42" width="150" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="545" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">İŞLE</text>
  <text x="545" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">güvenli devam</text>
  <rect x="470" y="150" width="150" height="72" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="545" y="176" text-anchor="middle" font-size="12.5" font-weight="700" fill="#C77A17" font-family="system-ui">REDDET</text>
  <text x="545" y="196" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">+ logla</text>
  <text x="545" y="212" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">saldırı işareti</text>
  <line x1="320" y1="108" x2="466" y2="76" stroke="#25804C" stroke-width="1.8" marker-end="url(#sc-ok4)"/>
  <text x="392" y="86" text-anchor="middle" font-size="11" font-weight="600" fill="#25804C" font-family="system-ui">Evet</text>
  <line x1="320" y1="140" x2="466" y2="182" stroke="#C77A17" stroke-width="1.8" marker-end="url(#sc-ok5)"/>
  <text x="392" y="176" text-anchor="middle" font-size="11" font-weight="600" fill="#C77A17" font-family="system-ui">Hayır</text>
</svg>
```

## Enjeksiyon (injection) ve parametreli sorgu
**Enjeksiyon**, kullanıcıdan gelen **verinin**, onu okuyan sistem tarafından **komut** diye
yorumlanmasıdır. En bilineni **SQL enjeksiyonu**dur (veritabanı sorgusuna komut sızdırmak);
aynı mantık işletim sistemi komutlarında (command injection), LDAP ve XPath sorgularında da
geçerlidir.

Kök neden neredeyse her zaman aynıdır: sorgunun metni ile kullanıcı verisi **tek bir metin
içinde birleştirilir** (string birleştirme). O anda veritabanı, hangi kısmın kod hangi kısmın
veri olduğunu ayırt edemez:

```java
// YANLIŞ — veri ile komut aynı metinde birleşiyor
String sql = "SELECT * FROM kullanici WHERE ad = '" + ad + "'";
```

Kullanıcı `ad` alanına `' OR '1'='1` yazarsa sorgunun anlamı değişir ve koşul her satır için
doğru olur. Çözüm veriyi kısıtlamak değil, **veriyi komuttan ayrı bir kanaldan göndermektir**:

```java
// DOĞRU — parametreli sorgu (hazırlanmış ifade / prepared statement)
PreparedStatement ps = baglanti.prepareStatement(
        "SELECT * FROM kullanici WHERE ad = ?");
ps.setString(1, ad);          // veri hiçbir zaman kod olarak yorumlanmaz
```

Burada sorgunun iskeleti veritabanına önce ve ayrı gönderilir; `?` yerine konan değer artık
sorgunun yapısını değiştiremez, yalnızca bir değer olarak kalır.

**Şerh:** Parametreli sorgu, SQL enjeksiyonunu "tamamen ortadan kaldıran sihirli düğme"
değildir — yalnızca **değer konulabilen yerleri** korur. Tablo adı, sütun adı ya da
`ORDER BY` yönü gibi sorgunun yapısına ait parçalar parametre olamaz; oraya kullanıcı girdisi
gidecekse tek güvenli yol sabit bir **izin-listesinden** seçim yapmaktır. Aynı şekilde bir ORM
(nesne-ilişki eşleyici) kullanmak da otomatik koruma anlamına gelmez: içinde elle metin
birleştirdiğin her yer yine açıktır.

Aynı "veri ile kod karışmasın" fikri çıktı tarafında da geçerlidir: kullanıcı verisini bir
HTML sayfasına basarken HTML'e uygun biçimde kaçırmazsan (escaping), veri tarayıcıda betik
olarak çalışır — buna **XSS (cross-site scripting)** denir.

## Hata yönetimi (exception handling): sırrı sızdırma
Program beklenmedik bir durumla karşılaşınca çalışmayı kesip bir hata nesnesi fırlatır; buna
**istisna (exception)** denir. Böyle bir hata oluştuğunda kullanıcıya **ne kadar az iç bilgi
verirsen o kadar güvenlisin.** Ham **yığın izi** (stack trace — hatanın hangi metotlardan
geçerek oluştuğunu gösteren döküm), veritabanı hata mesajı, dosya yolu, sürüm bilgisi veya
bağlantı dizesi (connection string) kullanıcıya sızarsa saldırgana harita çizmiş olursun.

- **Kullanıcıya:** genel, nötr bir mesaj ("Bir hata oluştu, lütfen tekrar deneyin").
- **İç loga:** tam ayrıntı — yığın izi (stack trace), hata kodu, bağlam — ama yalnızca
  sunucuda. Loga da sır yazılmaz: parola, token, kart numarası log dosyasında görünmemeli.

**Şerh:** "Az bilgi = çok güvenlik" bir denge sorusudur, sınırsız bir kural değil. Kullanıcının
düzeltebileceği hatalar (eksik alan, geçersiz tarih biçimi) açıkça söylenmelidir; gizlenmesi
gereken şey sistemin **iç yapısıdır**. Ayrıca destek için hata mesajına bir **korelasyon
kimliği** (log kaydıyla eşleşen rastgele numara) koymak, hiçbir şey sızdırmadan sorunu
izlenebilir kılar.

Aşağıdaki diyagram solda **yanlış** (sırrı kullanıcıya döken), sağda **doğru** (genel
mesaj gösterip ayrıntıyı içeride loglayan) yaklaşımı karşılaştırır.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Exception handling karşılaştırması: solda yanlış yaklaşım stack trace ve sır sızdırır, sağda doğru yaklaşım genel mesaj gösterip ayrıntıyı içeride loglar">
  <defs>
    <marker id="sc-ok6" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#C77A17"/></marker>
    <marker id="sc-ok7" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Exception: sır kullanıcıya sızmasın</text>
  <text x="160" y="48" text-anchor="middle" font-size="12.5" font-weight="700" fill="#C77A17" font-family="system-ui">✗ YANLIŞ</text>
  <rect x="34" y="60" width="252" height="150" rx="12" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="160" y="86" text-anchor="middle" font-size="11" font-weight="600" fill="#1C3247" font-family="system-ui">Hata → doğrudan kullanıcıya</text>
  <text x="160" y="112" text-anchor="middle" font-size="10.5" fill="#C77A17" font-family="system-ui">stack trace görünür</text>
  <text x="160" y="132" text-anchor="middle" font-size="10.5" fill="#C77A17" font-family="system-ui">DB mesajı · dosya yolu</text>
  <text x="160" y="152" text-anchor="middle" font-size="10.5" fill="#C77A17" font-family="system-ui">connection string sızar</text>
  <text x="160" y="180" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">saldırgana harita</text>
  <line x1="160" y1="192" x2="160" y2="204" stroke="#C77A17" stroke-width="1.8" marker-end="url(#sc-ok6)"/>
  <text x="480" y="48" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">✓ DOĞRU</text>
  <rect x="354" y="60" width="252" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="480" y="86" text-anchor="middle" font-size="11" font-weight="600" fill="#1C3247" font-family="system-ui">Kullanıcıya genel mesaj</text>
  <text x="480" y="108" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">&quot;Bir hata oluştu&quot;</text>
  <line x1="480" y1="120" x2="480" y2="140" stroke="#25804C" stroke-width="1.5" stroke-dasharray="4 4"/>
  <rect x="384" y="140" width="192" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="480" y="164" text-anchor="middle" font-size="10.5" font-weight="600" fill="#186399" font-family="system-ui">iç log (yalnız sunucu)</text>
  <text x="480" y="181" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tam ayrıntı · stack trace</text>
</svg>
```

## Güvenli varsayılanlar (secure defaults) ve en az yetki
- **Güvenli varsayılanlar (secure defaults):** sistemin "kutudan çıktığı" hâli en
  güvenli hâli olmalı. Bir özellik yanlışlıkla açık kalırsa değil, yanlışlıkla kapalı
  kalırsa güvende olursun. Erişim varsayılan olarak **kapalı**, açmak bilinçli bir karar
  olsun.
- **En az yetki (least privilege):** her bileşen — kullanıcı, servis, süreç, veritabanı
  hesabı — işini yapmaya yeten **en dar** yetkiye sahip olmalı. Bir bileşen ele
  geçirilirse hasar, o dar yetkiyle sınırlı kalır. Uygulama veritabanına `admin` ile
  değil, yalnızca ihtiyaç duyduğu tablolara erişebilen bir hesapla bağlanmalı.
- **Koda sır gömme (hardcoded secrets):** API anahtarı, parola, token asla kaynak koda
  veya git'e yazılmaz. Sırlar ortam değişkeni (environment variable), gizli yönetimi
  (secrets manager) veya güvenli yapılandırmadan gelir. Git geçmişine giren bir sır,
  silinse bile geçmişte kalır ve rotasyon gerektirir.

## Parola saklama: yavaş hash, tuz (salt)
Parolalar veritabanında **asla düz metin** (plaintext) tutulmaz. Geri çevrilebilir bir
şifrelemeyle de tutulmaz: anahtarı ele geçiren herkes tüm parolaları okur. Doğru yöntem
**özetleme (hashing)**: girdiden tek yönlü, sabit uzunlukta bir çıktı üreten işlem. Tek yönlü
demek, çıktıdan girdiye dönmenin pratikte mümkün olmaması demektir. Kullanıcı giriş yaparken
girilen parolanın özeti yeniden hesaplanır ve kayıtlı özetle karşılaştırılır.

Ama her hash parola için uygun değildir:

- **Hızlı hash (MD5, SHA-1, SHA-256):** Dosya bütünlüğü doğrulamak için tasarlanmıştır ve
  bilerek çok hızlıdır. Tam da bu yüzden parola için **yanlıştır**: bir saldırgan sızdırdığı
  özet listesine karşı sıradan bir donanımla saniyede milyarlarca deneme yapabilir.
- **Yavaş hash (bcrypt, scrypt, Argon2, PBKDF2):** Parola için tasarlanmıştır; hesaplanması
  bilerek pahalıdır ve bu pahalılık ayarlanabilir bir **maliyet parametresiyle** (iş faktörü,
  yineleme sayısı, bellek kullanımı) belirlenir. Donanım hızlandıkça maliyet artırılır.
  Bugünün tavsiyesi **Argon2id**; bcrypt de yaygın ve kabul görmüş bir seçenektir.

**Tuz (salt):** Her parola için ayrı üretilen rastgele bir değerdir; parolaya karıştırılıp öyle
özetlenir ve özetin yanında saklanır. Gizli olması gerekmez, **benzersiz** olması gerekir. İki
işe yarar: (1) aynı parolayı kullanan iki kişinin özeti farklı çıkar, yani sızıntıda "bunlar
aynı parolayı kullanıyor" bilgisi verilmez; (2) saldırganın önceden hesaplayıp hazır tuttuğu
tablolar (rainbow table) işe yaramaz olur, her parolayı ayrı ayrı denemek zorunda kalır.
bcrypt ve Argon2 tuzu kendileri üretip çıktının içinde saklar — elle uğraşmana gerek yoktur.
İsteğe bağlı olarak tüm parolalara ortak, veritabanı dışında tutulan gizli bir ek değer daha
kullanılabilir: **biber (pepper)**.

**Şerh:** Doğru hash algoritması parolayı "kırılmaz" yapmaz; yalnızca kırmayı ekonomik olarak
pahalılaştırır. Zayıf bir parola yavaş hash ile bile kırılabilir. Bu yüzden parola saklama
tek başına değil, parola kalitesi kuralları ve çok faktörlü doğrulama (MFA) ile birlikte
düşünülür.

## Şifreleme: simetrik, asimetrik ve TLS
Özetleme (hash) ile **şifreleme (encryption)** karıştırılmamalı: hash tek yönlüdür, geri
dönülmez; şifreleme ise doğru anahtarla **geri çözülmek** üzere yapılır. Base64 gibi
kodlamalar (encoding) ise hiçbir güvenlik sağlamaz, yalnızca veriyi taşınabilir bir biçime
sokar.

- **Simetrik şifreleme (symmetric):** Şifreleme ve çözme için **aynı anahtar** kullanılır
  (yaygın örnek: AES). Çok hızlıdır, büyük veriyi şifrelemeye uygundur. Zor tarafı, anahtarı
  karşı tarafa güvenli biçimde ulaştırmaktır.
- **Asimetrik şifreleme (asymmetric / açık anahtarlı):** Birbirine bağlı bir **anahtar çifti**
  vardır: **açık anahtar (public key)** herkese verilebilir, **gizli anahtar (private key)**
  yalnızca sahibinde durur. Açık anahtarla şifrelenen veri yalnızca gizli anahtarla çözülür;
  gizli anahtarla atılan **imza (signature)** ise açık anahtarla doğrulanır. Simetriğe göre
  çok yavaştır, bu yüzden büyük veriyi doğrudan şifrelemek için kullanılmaz.

**TLS (Transport Layer Security)**, iki taraf arasındaki iletişimi koruyan protokoldür;
HTTPS'in "S"i budur. İki yöntemi birlikte kullanır: **el sıkışma (handshake)** aşamasında
asimetrik kriptografiyle sunucunun kimliği sertifika üzerinden doğrulanır ve taraflar geçici
bir **oturum anahtarı** üzerinde anlaşır; ardından asıl veri hızlı **simetrik** şifrelemeyle
taşınır. TLS üç şey sağlar: gizlilik (yol üstündeki kimse okuyamaz), bütünlük (değiştirilirse
anlaşılır) ve sunucu kimliği. Eski adı SSL'dir; SSL sürümleri ile TLS 1.0/1.1 güvensiz sayılır,
bugün **TLS 1.2 ve 1.3** kullanılır.

**Şerh:** "Sitede HTTPS var, güvenliyiz" yaygın bir yanılgıdır. TLS yalnızca **veriyi yolda**
korur; sunucuya ulaşan girdinin kötü niyetli olmadığını, uygulamanın yetki kontrolünü doğru
yaptığını ya da parolaların doğru saklandığını garanti etmez. Bu metindeki diğer bütün kurallar
HTTPS'in arkasında da aynen geçerlidir.

## Öz-sınav (kendini test et)
1. Bir kullanıcı adı alanını doğrularken izin-listesi (allowlist) mi kara-listesi
   (denylist) mi kullanırsın ve neden?
2. İstemci tarafında (tarayıcıda) yapılan doğrulama neden tek başına yeterli değildir;
   gerçek güven sınırı nerededir?
3. Geçersiz bir girdiyi yalnızca reddetmek yerine neden aynı zamanda loglarsın?
4. Bir exception oluştuğunda kullanıcıya ne göstermeli, iç loga ne yazmalısın?
5. "En az yetki (least privilege)" ilkesi bir bileşen ele geçirildiğinde hasarı nasıl
   sınırlar?
6. SQL enjeksiyonunun kök nedeni nedir ve parametreli sorgu bunu tam olarak nasıl keser?
   Parametreli sorgunun koruyamadığı bir yer söyle.
7. Parola saklarken SHA-256 neden yanlış, bcrypt/Argon2 neden doğru seçimdir? Aradaki fark
   hangi özellikten doğuyor?
8. Tuz (salt) neye yarar, gizli tutulması gerekir mi? Aynı parolayı kullanan iki kullanıcının
   kayıtları tuzla nasıl farklılaşır?
9. Simetrik ve asimetrik şifrelemenin farkı nedir? TLS ikisini birden neden kullanır?
10. Bir sitede HTTPS olması hangi riskleri ortadan kaldırır, hangilerini kaldırmaz?
