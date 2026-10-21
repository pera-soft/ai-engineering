---
slug: api-testi-postman-restassured
baslik: "API Testi: Postman, REST Assured, Sözleşme Doğrulama ve Test Piramidindeki Yeri"
kademe: Orta
sira: 232
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir restoranı denetlediğini düşün. Vitrine bakıp "tabak güzel sunulmuş" demek bir denetimdir, ama sığ bir denetimdir. Asıl soru, mutfakta pişen yemeğin tarifle uyup uymadığıdır. **Arayüz (UI) testi** vitrini kontrol eder: buton görünüyor mu, tutar ekranda doğru yazıyor mu? **API testi** ise doğrudan mutfağa girer. Sorduğu sorular şunlardır: sepete ekleme isteği gönderildiğinde sunucu gerçekten ne yapıyor, kupon iş kuralı nasıl işliyor, stok yetersizken hangi cevabı veriyor?

Aynı iş kuralını API seviyesinde doğrulamak saniyeler yerine milisaniyeler alır. Tarayıcı açmaz ve CSS seçicisi değişti diye kırılmaz. Bu yüzden API testi test piramidinin **orta katmanıdır**: birim testten daha bütünseldir, arayüz testinden kat kat hızlı ve kararlıdır. Burada API bir kurulum aracı değil, **test nesnesinin kendisidir**.

### Temel kavramlar

- **API** (uygulama programlama arayüzü, application programming interface): bir sistemin dışarıya açtığı sözleşmeli giriş noktaları. Web dünyasında genellikle HTTP üzerinden JSON konuşulur.
- **Uç nokta (endpoint)**: tek bir kaynağa karşılık gelen adres — `GET /urunler`, `POST /sepet/satirlar`.
- **Güvenli metot (safe)**: sunucudaki durumu değiştirmeyen metot. `GET`, `HEAD`, `OPTIONS` güvenlidir; salt okumadır.
- **Etkisiz-tekrarlı metot (idempotent, tekrar edilse de sonucu değişmeyen)**: aynı isteği 1 kez de 10 kez de göndersen sunucuda **aynı durumu** bırakan metottur. `GET`, `PUT`, `DELETE` ve `HEAD` böyledir; `POST` **değildir**. Kritik ayrım şudur: bu özellik *sunucu durumu* hakkındadır, *yanıt kodu* hakkında değil. İkinci `DELETE` çağrısı `404` dönebilir; kayıt yine yoktur ve metot yine idempotenttir.
- **Idempotency-Key (tekrar anahtarı)**: `POST` gibi tekrar güvenli olmayan isteklerde istemcinin ürettiği benzersiz anahtardır. Ağ zaman aşımından sonra istemci aynı anahtarla tekrar dener. Sunucu bunun *aynı* işlem olduğunu tanır ve ikinci siparişi oluşturmaz.
- **Durum kodu aileleri**: `2xx` başarı, `3xx` yönlendirme veya önbellek, `4xx` istemci hatası, `5xx` sunucu hatası. Sık karışanlar aşağıda ayrı ele alınıyor.
- **Başlık (header)**: `Content-Type` gönderilen gövdenin tipini bildirir. `Accept` istenen tipi bildirir. `Authorization` kimlik bilgisini taşır (`Bearer <token>`). `Location` yeni oluşturulan kaynağın adresidir ve `201` ile birlikte gelir. `ETag` ile `If-None-Match` ise kaynağın sürüm parmak izini taşır; kaynak değişmemişse sunucu `304 Not Modified` döner. Bu ikili iyimser kilitlemede de kullanılır.
- **Postman**: istekleri **koleksiyon (collection)** hâlinde saklayan bir araçtır. **Ortam (environment)** ve değişkenler sayesinde aynı koleksiyonu farklı sunuculara yönlendirir. Ayrıca **ön-istek (pre-request) betiği** ve **test betiği** yazmanıza izin verir.
- **Newman**: Postman koleksiyonunu tarayıcısız, komut satırından koşturan çalıştırıcıdır. CI'da kullanılan budur.
- **REST Assured**: Java'da API testi yazmak için kullanılan kütüphanedir. `given()` (hazırlık), `when()` (istek) ve `then()` (doğrulama) yapısıyla okunur testler kurar.
- **JSON şema doğrulama (JSON Schema validation)**: yanıtın *alanlarını ve tiplerini* tek tek yazmak yerine bir şema dosyasına sözleşme olarak yazmaktır. Sonra tüm gövde bu şemaya karşı doğrulanır.
- **JsonPath**: JSON gövdesinden alan çıkarma dilidir. Örnekler: `satirlar[0].adet`, `urunler.find { it.sku == 'SKU-1001' }.fiyat`.
- **Taklit nesne / sanal servis (mock/stub, örneğin WireMock)**: gerçek bir bağımlılığın yerine geçen, önceden tanımlı cevaplar veren sahte sunucudur.
- **Sözleşme testi (contract test)**: tüketici ile sağlayıcının uyumunu, iki servisi birlikte ayağa kaldırmadan doğrulama yöntemidir. Ayrıntısı 187 numaralı konudadır. Burada yalnızca taklit nesne kullanımının sınırını göstermek için anılıyor.

```svg
<svg viewBox="0 0 660 262" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Test piramidinde API testinin yeri: orta katman, hizli ve kararli">
  <text x="330" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">API testi piramidin orta katmanıdır</text>
  <polygon points="270,42 325,100 215,100" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <polygon points="215,100 325,100 385,158 155,158" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <polygon points="155,158 385,158 445,216 95,216" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="270" y="80" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">UI</text>
  <text x="270" y="127" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">API / servis</text>
  <text x="270" y="145" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">bu konu</text>
  <text x="270" y="192" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Birim</text>
  <text x="470" y="70" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">az sayıda</text>
  <text x="470" y="86" font-size="10" fill="#5D7789" font-family="system-ui">tarayıcı, render, seçici</text>
  <text x="470" y="100" font-size="10" fill="#5D7789" font-family="system-ui">yavaş ve kırılgan</text>
  <text x="470" y="124" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">iş kuralına doğrudan erişim</text>
  <text x="470" y="140" font-size="10" fill="#5D7789" font-family="system-ui">saniye değil milisaniye</text>
  <text x="470" y="154" font-size="10" fill="#5D7789" font-family="system-ui">hata yollarını kolay üretir</text>
  <text x="470" y="178" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">çok sayıda</text>
  <text x="470" y="194" font-size="10" fill="#5D7789" font-family="system-ui">tek sınıf, tek karar</text>
  <text x="330" y="240" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">Tuzak: "API testi varsa UI'a gerek yok" — API yeşilken buton görünmüyor/tıklanamıyor olabilir.</text>
  <text x="330" y="254" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">API testi UI testini AZALTIR, ortadan kaldırmaz.</text>
</svg>
```

### Nasıl çalışır? / Pratikte nasıl yapılır?

**Önce HTTP'yi testçi gözüyle okumak.** Bir API testçisinin metot tablosunu ezberden bilmesi beklenir, çünkü tekrar denemenin (retry) güvenli olup olmadığı buna bağlıdır:

```svg
<svg viewBox="0 0 660 254" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="HTTP metotlari: guvenli ve idempotent ayrimi tablosu">
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Güvenli (safe) ve etkisiz-tekrarlı (idempotent) ayrımı</text>
  <text x="330" y="38" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">"Bu isteği ağ hatasında tekrar göndersem ne olur?" sorusunun cevabı buradadır</text>
  <rect x="25" y="50" width="610" height="28" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <rect x="25" y="78" width="610" height="140" fill="none" stroke="#5D7789" stroke-width="1.1"/>
  <line x1="140" y1="50" x2="140" y2="218" stroke="#5D7789" stroke-width="1"/>
  <line x1="255" y1="50" x2="255" y2="218" stroke="#5D7789" stroke-width="1"/>
  <line x1="380" y1="50" x2="380" y2="218" stroke="#5D7789" stroke-width="1"/>
  <line x1="25" y1="106" x2="635" y2="106" stroke="#5D7789" stroke-width="0.7"/>
  <line x1="25" y1="134" x2="635" y2="134" stroke="#5D7789" stroke-width="0.7"/>
  <line x1="25" y1="162" x2="635" y2="162" stroke="#5D7789" stroke-width="0.7"/>
  <line x1="25" y1="190" x2="635" y2="190" stroke="#5D7789" stroke-width="0.7"/>
  <text x="82" y="69" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Metot</text>
  <text x="197" y="69" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Güvenli (safe)</text>
  <text x="317" y="69" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Idempotent</text>
  <text x="507" y="69" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">e-ticaret karşılığı</text>
  <text x="82" y="97" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">GET</text>
  <text x="197" y="97" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">evet</text>
  <text x="317" y="97" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">evet</text>
  <text x="392" y="97" font-size="9.5" fill="#5D7789" font-family="system-ui">ürün listesi çek — kaç kez çekersen çek değişmez</text>
  <text x="82" y="125" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">POST</text>
  <text x="197" y="125" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">hayır</text>
  <text x="317" y="125" text-anchor="middle" font-size="10" font-weight="700" fill="#B23B3B" font-family="system-ui">HAYIR</text>
  <text x="392" y="125" font-size="9.5" fill="#B23B3B" font-family="system-ui">sipariş oluştur — iki kez gönderirsen İKİ sipariş</text>
  <text x="82" y="153" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">PUT</text>
  <text x="197" y="153" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">hayır</text>
  <text x="317" y="153" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">evet</text>
  <text x="392" y="153" font-size="9.5" fill="#5D7789" font-family="system-ui">sepet satırını "adet = 3" yap — 5 kez gönder, adet 3</text>
  <text x="82" y="181" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">DELETE</text>
  <text x="197" y="181" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">hayır</text>
  <text x="317" y="181" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">evet</text>
  <text x="392" y="181" font-size="9.5" fill="#5D7789" font-family="system-ui">satırı sil — ikinci çağrı 404 dönebilir, durum aynı</text>
  <text x="82" y="209" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">PATCH</text>
  <text x="197" y="209" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">hayır</text>
  <text x="317" y="209" text-anchor="middle" font-size="10" fill="#B07D20" font-family="system-ui">garanti yok</text>
  <text x="392" y="209" font-size="9.5" fill="#5D7789" font-family="system-ui">"adedi 1 artır" biçimindeyse idempotent DEĞİL</text>
  <text x="330" y="238" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Idempotentlik SUNUCU DURUMU hakkındadır, yanıt kodunun aynı olması şart değildir.</text>
  <text x="330" y="250" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">POST'u tekrar güvenli kılmak için Idempotency-Key başlığı kullanılır.</text>
</svg>
```

**Durum kodlarında sık karışan üçlü.**

1. **400 ile 422 farkı.** `400 Bad Request`, isteğin *biçiminin* bozuk olduğunu söyler: JSON ayrıştırılamıyor, zorunlu alan hiç yok, tip tamamen yanlış. `422 Unprocessable Content` ise "isteğini anladım ama iş kuralı reddediyor" demektir; adet `-3`, kupon süresi dolmuş, kargo adresi teslimat bölgesi dışında gibi. Şerh: pratikte pek çok gerçek API ikisini de `400` ile döner. Bu yüzden testi *kitaba* göre değil, **API'nin kendi sözleşmesine** göre yazarsın; sözleşme OpenAPI/Swagger belgesidir. Sözleşme yoksa, sözleşmenin yokluğu zaten bir bulgudur.
2. **401 ile 403 farkı.** `401 Unauthorized` aslında "kimliğin *doğrulanamadı*" demektir; token yoktur, bozuktur ya da süresi dolmuştur. Standarda göre bu yanıta `WWW-Authenticate` başlığı eşlik eder. `403 Forbidden` ise "kim olduğun belli ama bu kaynağa yetkin yok" demektir. Süresi dolmuş bir token'a `403` dönen API sözleşme hatası yapıyordur; bu bir bulgudur.
3. **404 ile 410 farkı.** `404 Not Found` "bulamadım" der. Kaynak hiç var olmamış olabilir; bilerek gizleniyor da olabilir. `410 Gone` ise "vardı, kalıcı olarak kaldırıldı" der. Kaldırılan bir kampanya ürününde `410` dönmek, istemciye ve arama motoruna daha fazla bilgi verir.

Ek olarak `201 Created` yanıtında `Location` başlığını doğrulamak, `204 No Content` yanıtında ise **gövde doğrulaması yapmamak** gerekir — orada gövde yoktur.

**Postman ile adım adım.**

1. **Koleksiyon ve ortam ayrımı.** İstekler koleksiyonda, adres ve kimlik bilgileri ortamda durur: `{{baseUrl}}`, `{{token}}`, `{{apiKey}}`. Aynı koleksiyon `dev`, `test` ve `staging` ortamlarında değiştirilmeden koşar.
2. **Sır yönetimi.** API anahtarı, şifre ve token **koleksiyona gömülmez**. Koleksiyonda yalnızca `{{apiKey}}` yer tutucusu bulunur; değeri CI'nın gizli anahtar kasasından gelir. Bir kez depoya girmiş anahtar, sonradan silinse bile git geçmişinde durur. Bu yüzden iptal edilip yenilenmesi gerekir.
3. **Ön-istek (pre-request) betiği** kimlik doğrulamayı hazırlar; token süresi dolmuşsa tazeler.
4. **Test betiği** `pm.test` + `pm.expect` ile yanıtı doğrular.
5. **Koleksiyon çalıştırıcısı (Collection Runner)** ve **veri dosyası** birlikte kullanılır. Böylece aynı istek, CSV'deki her satır için tekrar koşar: kupon kodları, geçersiz adetler, farklı ülke kodları.
6. **Newman** aynı koleksiyonu CI'da tarayıcısız koşar ve JUnit XML üretir. Böylece sonuçlar boru hattı raporuna düşer.

```javascript
// Ön-istek (pre-request) betiği: token yoksa veya yaşlandıysa tazele
const sonAlim = Number(pm.environment.get("tokenZamani") || 0);
if (Date.now() - sonAlim > 50 * 60 * 1000) {
    pm.sendRequest({
        url: pm.environment.get("authUrl"),
        method: "POST",
        header: { "Content-Type": "application/x-www-form-urlencoded" },
        body: { mode: "urlencoded", urlencoded: [
            { key: "grant_type", value: "password" },
            { key: "username", value: pm.environment.get("kullanici") },
            { key: "password", value: pm.environment.get("sifre") }   // değer CI secret'ından gelir
        ]}
    }, function (hata, yanit) {
        pm.environment.set("token", yanit.json().access_token);
        pm.environment.set("tokenZamani", Date.now());
    });
}
```

```javascript
// Test betiği: sepete ekleme yanıtı — sadece 200'e bakmıyoruz
pm.test("201 Created ve Location başlığı dönüyor", function () {
    pm.response.to.have.status(201);
    pm.expect(pm.response.headers.get("Location")).to.include("/sepet/satirlar/");
});

pm.test("Gövde alanları, tipleri ve iş kuralı doğru", function () {
    const govde = pm.response.json();
    pm.expect(govde.satirlar[0].adet).to.eql(2);
    pm.expect(govde.toplam).to.be.a("number");     // string "259.80" gelirse yakala
    pm.expect(govde.toplam).to.eql(259.80);        // 2 x 129.90 iş kuralı
});

pm.test("Yanıt süresi 800 ms altında", function () {
    pm.expect(pm.response.responseTime).to.be.below(800);
});
```

CI'da koşum, anahtar dosyaya yazılmadan:

```bash
newman run sepet.postman_collection.json \
  -e ci.postman_environment.json \
  --env-var "apiKey=$API_KEY" \
  --reporters cli,junit --reporter-junit-export sonuc/newman.xml
```

**REST Assured ile Java tarafı.** Postman, keşif ve hızlı deneme için mükemmeldir. Kalıcı regresyon paketi ise çoğu Java ekibinde REST Assured ile yazılır. Sebebi şudur: aynı derleyici, aynı bağımlılık yönetimi, aynı sürüm kontrolü ve aynı IDE desteği geçerli olur. Yapı `given` / `when` / `then` üçlüsüdür ve TestNG ya da JUnit ile birleşir:

```java
@Test
public void stokYeterliykenSepeteEklemeToplamiGunceller() {
    given()
        .baseUri(TEMEL_ADRES)
        .header("Authorization", "Bearer " + token)
        .contentType(ContentType.JSON)
        .body(new SepetSatiri("SKU-1001", 2))          // POJO seri hâle getirme
        .filter(new RequestLoggingFilter())            // hata ayıklama için istek logu
    .when()
        .post("/sepet/satirlar")
    .then()
        .statusCode(201)
        .header("Location", containsString("/sepet/satirlar/"))
        .body(matchesJsonSchemaInClasspath("sepet-semasi.json"))   // şema doğrulama
        .body("satirlar[0].adet", equalTo(2))
        .body("toplam", equalTo(259.80f))
        .time(lessThan(800L));
}

@Test
public void stokYetersizkenSiparisReddedilir() {
    SiparisYaniti yanit =
        given().baseUri(TEMEL_ADRES).header("Authorization", "Bearer " + token)
               .contentType(ContentType.JSON).body(new Siparis("SKU-9999", 500))
        .when().post("/siparisler")
        .then().statusCode(422)                        // iş kuralı reddi
               .body("hataKodu", equalTo("STOK_YETERSIZ"))
               .body("mesaj", not(emptyOrNullString()))
               .extract().as(SiparisYaniti.class);     // POJO'ya çözme
    assertThat(yanit.getSiparisNo()).isNull();
}
```

**Ne doğrulanmalı — "200 döndü" bir test değildir.** Mülakatta en çok eleyen cevap "durum kodunun 200 olduğunu kontrol ederim" cümlesidir. Doğrulama katmanları şunlardır:

```svg
<svg viewBox="0 0 660 296" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="API testinde dogrulama derinligi katmanlari">
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Doğrulama derinliği: yukarıdan aşağı indikçe test gerçekten test olur</text>
  <text x="330" y="38" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Örnek: POST /sepet/satirlar — 2 adet SKU-1001</text>
  <rect x="30" y="50" width="600" height="32" rx="7" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="44" y="70" font-size="10.5" fill="#B23B3B" font-family="system-ui">1 · Durum kodu — 201 mi? Yalnız buraya bakmak en sık yapılan hatadır</text>
  <rect x="30" y="88" width="600" height="32" rx="7" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="44" y="108" font-size="10.5" fill="#1C3247" font-family="system-ui">2 · Gövde alanları — sepetId, satirlar[0].adet, toplam gerçekten var mı?</text>
  <rect x="30" y="126" width="600" height="32" rx="7" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="44" y="146" font-size="10.5" fill="#1C3247" font-family="system-ui">3 · Tipler ve şema — toplam sayı mı yoksa "259.80" metni mi? JSON şema doğrulaması</text>
  <rect x="30" y="164" width="600" height="32" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="44" y="184" font-size="10.5" fill="#1C3247" font-family="system-ui">4 · İş kuralı — 2 x 129,90 = 259,80; %10 kupon sonrası 233,82</text>
  <rect x="30" y="202" width="600" height="32" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="44" y="222" font-size="10.5" fill="#1C3247" font-family="system-ui">5 · Hata gövdesi biçimi — stok yetersizken hangi kod, hangi mesaj alanı?</text>
  <rect x="30" y="240" width="600" height="32" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="44" y="260" font-size="10.5" fill="#1C3247" font-family="system-ui">6 · Süre ve güvenlik — 800 ms altı; token yanıt gövdesine/loga sızmıyor</text>
  <text x="330" y="288" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">"Durum kodu 200" tek başına bir test değildir: 200 dönüp gövdede hata bildiren API'ler yaygındır.</text>
</svg>
```

**Kimlik doğrulama akışları.** Dört yaygın biçim vardır ve testte davranışları farklıdır. **API anahtarı**: sabit bir değerdir; başlıkta ya da sorgu parametresinde taşınır. Sorgu parametresindeyse sunucu erişim kayıtlarına düşer; bu bir güvenlik bulgusudur. **Basic**: kullanıcı adı ile şifrenin base64 hâlidir. Bu bir şifreleme değil, yalnızca kodlamadır; TLS olmadan açık metin sayılır. **Bearer/JWT**: süreli bir token'dır. Testte *tazelenmesi* gerekir; aksi hâlde uzun koşumun ortasında testler `401`'e düşer. **OAuth2**: token bir uç noktadan alınıp kullanılır. Uzun bir regresyon paketinde token'ı koşum başında bir kez alıp saklamak yaygın bir hatadır. Süre dolunca testler "kırıldı" görünür; oysa kırılan şey testin kendi kurulumudur.

**Test verisi ve durum yönetimi.** Bir API test paketinin en sinsi hastalığı **sıra bağımlılığıdır**. Şöyle olur: 3 numaralı test, 1 numaralı testin oluşturduğu sepete güvenir. O zaman testleri tek başına ya da paralel koşturamazsın. 1 numaralı test kalktığında 3 de "kırılır", ama gerçek sebep başkadır. Kural şudur: her test **kendi verisini kendi kurar** (setup) ve bitince **temizler** (cleanup). Yani sepeti boşaltır, oluşturduğu siparişi iptal eder. Temizlik `finally` benzeri bir yapıda olmalıdır ki doğrulama başarısız olsa bile çalışsın. Paylaşılan bir test ortamında benzersizlik de senin sorumluluğundur: `test+{{zamanDamgasi}}@ornek.com` gibi çakışmayan veri üret.

**Taklit nesne / sanal servis ne zaman doğru?** WireMock gibi bir sanal servis üç durumda **doğru** araçtır. (1) Bağımlılık üçüncü partidir ve her koşumda gerçek para veya SMS harcanamaz; ödeme sağlayıcısı ve kargo firması gibi. (2) Gerçekte üretilmesi çok zor olan hata durumlarını tetiklemen gerekir: `503`, zaman aşımı, bozuk gövde. (3) Bağımlı servis henüz yazılmamıştır ve paralel geliştirme yapılıyordur. **Yalancı güven** ise şurada başlar: taklit nesne senin *varsayımını* yansıtır, gerçek sağlayıcının davranışını değil. Sağlayıcı sürüm yükseltip bir alan adını değiştirdiğinde taklit eski cevabı vermeye devam eder. Testler yeşil kalır, üretim kırılır. Bu boşluğu kapatan şey **sözleşme testidir** (contract test, 187). Taklidin yansıttığı beklenti bir sözleşmeye yazılır. Sağlayıcı da kendi boru hattında bu sözleşmeye karşı **bağımsız** olarak doğrulanır.

**Sayfalama, filtre ve sınır değerler.** `GET /urunler?kategori=bebek-bezi&sayfa=2&adet=20&sirala=fiyat_artan` gibi bir uç noktada şunlar doğrulanır. Dönen eleman sayısı `adet` ile uyuşuyor mu? `toplamKayit` ile sayfa sayısı tutarlı mı? `sayfa=0` ve `adet=100000` gibi sınır değerlerde ne oluyor — `400`/`422` mi dönüyor, yoksa sessizce üst sınıra mı çekiliyor? Filtre gerçekten uygulanıyor mu; yani dönen her ürünün kategorisi doğru mu? Sıralama gerçekten artan mı? Son olarak: sayfa 1 ile sayfa 2 arasında **tekrar eden kayıt** var mı? Sıralama alanı benzersiz değilse bu klasik bir hatadır.

### Mülakatta ne sorulur / tuzaklar

- **"API testi arayüz testinin yerine geçer mi?"** Beklenen: hayır; azaltır ama kaldırmaz. "API yeşilse arayüz de çalışıyordur" derseniz elenirsiniz. Buton görünmeyebilir, tıklanamayabilir veya tutar ekranda yanlış biçimlenmiş olabilir.
- **"POST idempotent midir?"** Beklenen: hayır. Ağ zaman aşımında tekrar denemek ikinci siparişi oluşturur; çözüm `Idempotency-Key` başlığıdır. "DELETE ikinci çağrıda 404 dönüyor, o hâlde idempotent değil" demek de bir tuzaktır. Bu özellik sunucu durumu hakkındadır.
- **"Bu API testinde neyi doğrularsın?"** Beklenen sıra: durum kodu → gövde alanları → tipler ve şema → iş kuralı → hata gövdesi biçimi → süre ve güvenlik. Sadece `200` demek en hızlı eleme sebebidir.
- **"401 mi 403 mü?"** Beklenen: `401` kimliğin doğrulanamadığını söyler; token yoktur, bozuktur veya süresi dolmuştur. `403` ise kimliğin belli ama yetkinin olmadığını söyler. Süresi dolmuş token'a `403` dönmesi bulgu olarak raporlanır.
- **"Postman koleksiyonunu CI'da nasıl koşarsın, anahtarı nereye koyarsın?"** Beklenen: Newman ile koşarım. Anahtar koleksiyonda durmaz; `{{apiKey}}` yer tutucusu olarak durur. Değeri `--env-var` ile CI'nın gizli anahtar kasasından gelir.
- **"Şema doğrulamayı neden alan alan doğrulama yerine tercih edersin?"** Beklenen: şema, sözleşmenin tamamını tek yerde tutar. Yeni alan eklenince veya tip değişince tek noktadan yakalar. Alan alan yazılan doğrulamalar ise dağınık ve eksik kalır.
- **"Testlerini paralel koşabiliyor musun?"** Beklenen: evet. Çünkü her test kendi verisini kuruyor ve temizliyor. Sıra bağımlılığı yok ve benzersiz test verisi üretiliyor.
- **"Taklit nesne (mock) kullanır mısın?"** Beklenen: üçüncü parti servis, üretilmesi zor hata durumu ve henüz yazılmamış servis için evet. Ama taklit, gerçek sağlayıcıyı garanti etmez. Bu boşluk sözleşme testiyle kapatılır.

### Tek cümlede

API testi, iş kuralını arayüzün kırılgan katmanına çıkmadan doğrudan sınamaktır. Bunun için HTTP'nin güvenli/idempotent ayrımını ve durum kodlarının anlamını bilmek gerekir. Postman keşif için, Newman CI için, REST Assured ise kalıcı regresyon için kullanılır. `200` ile yetinilmez; gövde, tip, iş kuralı, hata biçimi ve süre de doğrulanır. Son olarak her test kendi verisini kurup temizler ve taklit nesnenin nerede bittiği (sözleşme testi) bilinir.

# apitest-1 | Temel
## Soru
"Sepet toplamına %10 kupon uygulandığında 259,80 TL yerine 233,82 TL yazmalı" iş kuralı hem tarayıcıdan (UI) hem doğrudan `POST /sepet/kupon` çağrılarak (API) test edilebiliyor. Test piramidi bu iş kuralı için hangisini önerir ve neden?
## A
API seviyesi tercih edilir; aynı kural saniyeler yerine milisaniyelerde, tarayıcı ve CSS seçici kırılganlığı olmadan doğrulanır, UI testi kupon kutusunun görünürlüğü gibi arayüze özgü davranışa saklanır
🤖 Doğru: iş kuralı en ucuz ve en kararlı seviyede doğrulanır; UI testi yalnızca gerçekten arayüze ait olan davranış için kullanılır.
## B
UI seviyesi tercih edilir; kullanıcı gerçekte tarayıcıyı kullandığı için API testi kullanıcı deneyimini temsil etmez ve iş kuralı doğrulaması için geçerli sayılmaz
🤖 Kullanıcının UI kullanması, iş kuralının UI'dan test edilmesini gerektirmez; aynı kural API'de hem daha hızlı hem daha kararlı doğrulanır.
## C
İkisi arasında teknik bir fark yoktur; seçim tamamen ekibin alışkanlığına ve hangi aracı daha iyi bildiğine bağlıdır
🤖 Fark gerçektir ve ölçülebilir: koşum süresi, kararlılık ve hata yollarını üretme kolaylığı belirgin biçimde farklıdır.
## D
API testi geliştiricilerin sorumluluğundadır; QA mühendisi yalnızca UI seviyesinde test yazar, iş kuralları API testine bırakılmaz
🤖 API testi QA'in çekirdek işlerinden biridir; piramitteki katman ayrımı rollere göre değil, doğrulamanın maliyetine ve kararlılığına göre yapılır.
## Doğru
A
## Açıklama
Test piramidinin tek kuralı, bir doğrulamayı **yapılabilecek en ucuz ve en kararlı seviyede** yapmaktır. Kupon indirimi bir iş kuralıdır ve sunucuda hesaplanır; API'den doğrulandığında tarayıcı açılmaz, sayfa render edilmez, seçici değişikliğinden etkilenmez ve koşum milisaniyeler sürer. UI testi ise gerçekten arayüze ait olan şeyler için saklanır: kupon kutusu görünüyor mu, hata mesajı kullanıcıya gösteriliyor mu, tutar doğru biçimlendirilmiş mi. B'deki "kullanıcı UI kullanıyor" gerekçesi yaygın bir yanlış inanıştır — kullanıcının arayüzü kullanması, her doğrulamanın arayüzden yapılmasını gerektirmez. C, ölçülebilir bir farkı tercih meselesine indirger. D ise katman ayrımını role bağlar; oysa API testi QA'in çekirdek yetkinliğidir. Yine de dikkat: API testi UI testini **azaltır**, ortadan kaldırmaz.

# apitest-2 | Orta
## Soru
Sipariş oluşturan `POST /siparisler` isteği ağ zaman aşımına uğradı; istemci aynı isteği tekrar gönderdi ve müşteriye **iki sipariş** açıldı. HTTP semantiği açısından doğru tespit ve çözüm hangisidir?
## A
`POST` idempotenttir; aynı gövdeyle gelen ikinci istek sunucu tarafından otomatik olarak yok sayılmalıydı, bu bir sunucu gerçekleme hatasıdır
🤖 `POST` idempotent değildir ve HTTP sunuculara aynı gövdeyi otomatik tekilleştirme yükümlülüğü getirmez.
## B
Hiçbir HTTP metodu tekrar güvenliği vermez; `PUT` ve `DELETE` de idempotent olmadığı için metot seçiminin bu sorunla ilgisi yoktur
🤖 `PUT` ve `DELETE` tanım gereği idempotenttir; metot seçimi tekrar güvenliğini doğrudan belirler.
## C
`POST` idempotent değildir; tekrar güvenliği için istemcinin ürettiği bir `Idempotency-Key` başlığıyla sunucunun aynı işlemi tanıması gerekir
🤖 Doğru: tekrar denenebilirlik `POST` için ancak açık bir idempotentlik anahtarıyla sağlanır.
## D
Sorun testin kendisindedir; testler `POST` isteğini asla tekrar göndermemelidir, üretimde zaman aşımı sonrası tekrar deneme zaten yaşanmaz
🤖 Zaman aşımı sonrası tekrar deneme üretimde çok yaygındır; testin görevi tam da bu davranışı önceden ortaya çıkarmaktır.
## Doğru
C
## Açıklama
`POST` ne güvenli (safe) ne de etkisiz-tekrarlı (idempotent) bir metottur: her çağrı yeni bir kaynak yaratmak üzere tasarlanmıştır, dolayısıyla iki çağrı iki sipariş demektir. Ağ zaman aşımında istemci ilk isteğin sunucuya ulaşıp ulaşmadığını **bilemez**, bu yüzden tekrar denemek gerçek hayatta kaçınılmazdır. Çözüm, istemcinin her mantıksal işlem için benzersiz bir `Idempotency-Key` üretip başlıkta göndermesi; sunucunun bu anahtarı kaydedip aynı anahtarla gelen ikinci isteğe **yeni kaynak yaratmadan** ilk yanıtı döndürmesidir. A, `POST`'un tanımını yanlış aktarır. B ise `PUT`/`DELETE`'in idempotentliğini reddeder; oysa `PUT` "durumu şu hâle getir" dediği, `DELETE` de "yok et" dediği için tekrar edildiklerinde sunucu durumu değişmez (ikinci `DELETE` `404` dönse bile durum aynıdır). D, üretimde çok yaygın olan bir senaryoyu yok sayar.

# apitest-3 | Orta
## Soru
Bir e-ticaret API'sinde şu dört durum oluşuyor: (1) gövde geçersiz JSON olarak gönderildi, (2) gövde geçerli ama `adet` alanı `-3`, (3) `Authorization` başlığı hiç yok, (4) giriş yapmış kullanıcı **başka birinin** siparişini görüntülemek istiyor. Standart HTTP semantiğine göre doğru eşleşme hangisidir?
## A
400 · 400 · 401 · 401
🤖 İş kuralı reddi ile biçim hatasını aynı koda toplar ve yetkisiz erişimi kimlik doğrulama hatasıyla karıştırır.
## B
400 · 422 · 401 · 403
🤖 Doğru: biçim hatası 400, iş kuralı reddi 422, kimlik doğrulanamadı 401, kimlik belli ama yetki yok 403.
## C
422 · 400 · 403 · 401
🤖 İlk iki kod yer değiştirmiş, son ikisi de ters: biçim hatası 422, eksik kimlik 403 olamaz.
## D
404 · 422 · 403 · 403
🤖 Geçersiz JSON gövdesi kaynağın bulunamadığı anlamına gelmez; eksik `Authorization` başlığı da 403 değil 401'dir.
## Doğru
B
## Açıklama
`400 Bad Request` isteğin **biçimini** reddeder: ayrıştırılamayan JSON, eksik zorunlu alan, tamamen yanlış tip. `422 Unprocessable Content` ise "isteği anladım ama **iş kuralı** kabul etmiyor" der — negatif adet, süresi dolmuş kupon, teslimat bölgesi dışı adres. `401 Unauthorized` adı yanıltıcıdır; gerçekte "kimliğin **doğrulanamadı**" demektir (token yok, bozuk ya da süresi dolmuş) ve standarda göre `WWW-Authenticate` başlığı eşlik eder. `403 Forbidden` ise kimliğin bilindiği ama **yetkinin** yetmediği durumdur — başkasının siparişi tam olarak budur. A ve C bu ayrımları karıştırır, D ise geçersiz gövdeyi `404` ile eşler ki bu tamamen farklı bir anlamdır (kaynak yok). Pratik şerh: gerçek dünyada pek çok API `422`'yi `400` içine toplar; testi kitaba göre değil **API'nin kendi belgelenmiş sözleşmesine** göre yazarsın, sözleşme yoksa bunun kendisi raporlanacak bir bulgudur.

# apitest-4 | İleri
## Soru
Postman koleksiyonunu Newman ile CI'ya taşıyorsun. Mevcut koleksiyonda API anahtarı isteklerin `Authorization` başlığına **doğrudan yazılmış** ve koleksiyon dosyası repoya commit'lenmiş durumda. Doğru düzenleme nedir?
## A
Koleksiyonu Postman bulut çalışma alanına (workspace) yükleyip repodan silmek yeterlidir; bulutta saklanan koleksiyonlar erişim denetimiyle korunur
🤖 Anahtar hâlâ koleksiyonun içinde durur ve çalışma alanına erişen herkes görür; ayrıca git geçmişindeki eski kopya silinmez.
## B
Deponun görünürlüğünü private yapmak yeterlidir; özel depoda saklanan anahtarlar dışarıya sızmadığı için ek bir önlem gerekmez
🤖 Private depo anahtarı depoya erişimi olan herkese ve tüm klonlara açık bırakır; sır yönetimi görünürlük ayarıyla çözülmez.
## C
Anahtarı ön-istek betiği içinde base64 ile kodlayıp koleksiyona gömmek; okunamaz hâle geldiği için repoda kalması sorun oluşturmaz
🤖 Base64 şifreleme değil kodlamadır, tek komutla geri çözülür; anahtar hâlâ repoda düz sır olarak durur.
## D
Koleksiyonda yalnızca `{{apiKey}}` yer tutucusu bırakmak, değeri CI secret kasasından `--env-var "apiKey=$API_KEY"` ile vermek ve repoya girmiş eski anahtarı iptal edip yenilemek
🤖 Doğru: sır dosyada değil kasada durur, koşuma dışarıdan verilir ve sızmış anahtar geçerliliğini yitirir.
## Doğru
D
## Açıklama
Postman'de doğru ayrım şudur: **koleksiyon** isteklerin yapısını, **ortam** ise adres ve kimlik değerlerini tutar; isteklerde `{{apiKey}}`, `{{baseUrl}}`, `{{token}}` gibi yer tutucular bulunur. CI'da Newman bu değerleri `--env-var` ile dışarıdan alır ve değer GitHub Actions gibi bir secret kasasından ortam değişkeni olarak gelir; loglarda maskelenir, dosyaya yazılmaz. Kritik ikinci adım çoğu zaman atlanır: anahtar bir kez repoya girdiyse commit'i geri almak yetmez — git geçmişinde ve tüm klonlarda durmaya devam eder, bu yüzden **iptal edilip yeniden üretilmesi** gerekir. A anahtarı koleksiyonun içinde bırakır, B görünürlük ayarını sır yönetimi sanır, C ise base64'ü şifreleme sanar; üçü de anahtarı okunabilir hâlde tutar.

# apitest-5 | İleri
## Soru
Stok yetersizken sipariş oluşturmayı sınayan REST Assured testi yalnızca `.then().statusCode(200)` doğrulaması yapıyor ve her koşumda yeşil yanıyor. Üretimde ise stoğu tükenmiş ürün için sipariş açılabildiği fark ediliyor. Bu testin temel kusuru nedir?
## A
Yanıt süresi doğrulaması eksik; `time(lessThan(...))` eklenmediği için stok kontrolünün gecikmeli çalıştığı fark edilememiş
🤖 Süre doğrulaması yararlıdır ama iş kuralının hiç doğrulanmamış olmasıyla ilgisi yoktur.
## B
`statusCode` yerine `statusLine` kullanılmalıydı; durum satırı metni sunucunun gerçek kararını daha güvenilir biçimde taşır
🤖 `statusLine` aynı bilgiyi metin olarak taşır, ek bir doğrulama gücü sağlamaz; sorun kod ile satır arasındaki fark değildir.
## C
API stok yetersizliğini `200` ile birlikte gövdedeki bir hata alanında bildiriyor olabilir; test yalnız durum koduna baktığı için iş kuralı hiç doğrulanmamış oluyor
🤖 Doğru: `200` dönüp gövdede hata bildiren API'ler yaygındır; doğrulama gövdeye, iş kuralına ve hata biçimine inmelidir.
## D
Stok kontrolü bir kullanıcı arayüzü davranışıdır; bu senaryo API testiyle değil yalnızca UI testiyle doğrulanabilir
🤖 Stok kontrolü sunucu tarafı bir iş kuralıdır ve tam olarak API seviyesinde doğrulanması gereken şeydir.
## Doğru
C
## Açıklama
Bir API testinde "durum kodu `200`" tek başına neredeyse hiçbir şey doğrulamaz. Gerçek dünyada pek çok API iş kuralı reddini de `200` ile döner ve hatayı gövdedeki bir alanda (`{"basarili": false, "hataKodu": "STOK_YETERSIZ"}`) bildirir; böyle bir API'de yalnız durum koduna bakan test **her koşulda yeşil yanar**. Doğrulama katmanları sırayla inmelidir: durum kodu, gövde alanlarının varlığı, tipleri (şema doğrulaması), **iş kuralı** (sipariş numarası üretilmemiş olmalı, stok düşmemeli), hata gövdesinin biçimi (`hataKodu` ve `mesaj` alanları dolu mu) ve yanıt süresi. A ve B işe yarar ama ilgisiz eklemelerdir; D ise sunucu tarafındaki bir iş kuralını arayüz davranışı sanarak doğrulamayı yanlış katmana taşır — üstelik UI'dan doğrulamak aynı kuralı daha yavaş ve daha kırılgan biçimde sınamak olurdu.

# apitest-6 | İleri
## Soru
Sipariş akışındaki ödeme sağlayıcısı WireMock ile taklit ediliyor (mock/stub); taklit her zaman `{"durum": "ONAYLANDI"}` dönüyor ve tüm testler aylardır yeşil. Sağlayıcı sürüm yükseltip yanıt alanını `status` olarak değiştirince üretimde ödeme akışı kırılıyor, testler ise yeşil kalmaya devam ediyor. Bu boşluğu kapatmanın doğru yolu nedir?
## A
Taklidin yansıttığı beklentiyi bir sözleşmeye (contract) yazmak ve sağlayıcının kendi boru hattında bu sözleşmeye karşı bağımsız doğrulanmasını sağlamak
🤖 Doğru: taklit yalnız tüketicinin varsayımını sınar; o varsayımın sağlayıcıda hâlâ geçerli olduğunu ancak sözleşme testi garanti eder.
## B
Taklidi kaldırıp testleri doğrudan ödeme sağlayıcısının üretim ortamına yönlendirmek, böylece gerçek yanıt biçimiyle çalışıldığından emin olmak
🤖 Her koşumda gerçek para hareketi yaratır, sağlayıcı kotasını tüketir ve genellikle sözleşmeyle yasaktır; bu bir çözüm değil yeni bir risktir.
## C
Taklidi kaldırıp bu akışı yalnızca uçtan uca UI testiyle doğrulamak, çünkü ödeme akışı ancak tarayıcıdan anlamlı biçimde sınanabilir
🤖 UI testi aynı sağlayıcı bağımlılığını taşır, üstelik daha yavaş ve kırılgandır; sağlayıcının sözleşmeyi bozduğunu da göstermez.
## D
Taklidin yanıt gecikmesini gerçekçi hâle getirmek ve ara sıra hata dönmesini sağlamak, böylece taklidin gerçek sağlayıcıya daha çok benzemesi
🤖 Gecikme ve hata enjeksiyonu dayanıklılık testi için değerlidir ama yanıt **biçimi** sapmasını hiçbir şekilde yakalamaz.
## Doğru
A
## Açıklama
Bir taklit (mock/stub) her zaman **tüketicinin varsayımını** yansıtır, sağlayıcının gerçek davranışını değil. Bu yüzden taklit doğru araç olduğu durumlarda bile (üçüncü parti bağımlılık, üretilmesi zor hata durumları, henüz yazılmamış servis) tek başına bir uyum garantisi vermez: sağlayıcı alan adını `durum`'dan `status`'a çevirdiğinde taklit eski cevabı vermeye devam eder ve test paketi sessizce yalan söyler. **Sözleşme testi** (contract test, 187 numaralı konu) tam bu boşluğu kapatır: tüketicinin beklediği istek/yanıt biçimi bir sözleşmeye yazılır ve sağlayıcı **kendi** boru hattında bu sözleşmeye karşı bağımsız doğrulanır; uyum bozulduğu anda sağlayıcının derlemesi kırmızı yanar. B gerçek para hareketi ve sağlayıcı kotası nedeniyle uygulanamaz, C aynı bağımlılığı daha pahalı bir katmana taşır, D ise gecikme/hata gerçekçiliğini artırır ama yanıt biçimindeki sapmayı görmez.
