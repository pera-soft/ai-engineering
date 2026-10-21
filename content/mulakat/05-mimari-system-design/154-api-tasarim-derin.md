---
slug: api-tasarim-derin
baslik: "Derin API Tasarımı: Richardson Olgunluğu, Versiyonlama, Idempotency, Pagination ve Hata Sözleşmesi"
kademe: İleri
sira: 154
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

**API**, iki programın birbiriyle konuşma sözleşmesidir. Bir program diğerine "şu kullanıcıyı getir" ya da "şu siparişi oluştur" diye bir not gönderir; bu nota **istek** (request), geri gelen cevaba **yanıt** (response) denir. Web dünyasında bu notun üstünde iki bilgi bulunur: hangi **kaynak** (resource) üzerinde çalışılacağı (ör. `/siparisler/42`) ve ne yapılacağını söyleyen **fiil** (method): `GET` (getir), `POST` (yarat), `PUT` (yerine koy), `DELETE` (sil).

API tasarımı "çalışsın yeter" işi değildir, çünkü API'yi senin yazmadığın programlar kullanır ve sen değiştirdiğinde onlar bozulur. Bu yüzden konu üç ekseni birden içerir: **anlamlı kaynak ve fiil kullanımı**, **yeniden denemede güvenlik** ve **kırmadan evrilebilme**.

### Temel kavramlar

- **Richardson olgunluk modeli**: bir API'nin ne kadar REST'e yaklaştığını dört seviyede ölçer. **Seviye 0**: her şey tek bir uç üzerinden, uzaktan yordam çağrısı (RPC) gibi yapılır. **Seviye 1**: her kaynağın kendi adresi (URI) olur. **Seviye 2**: HTTP fiilleri ve durum kodları doğru kullanılır. **Seviye 3**: **HATEOAS**, yani yanıtın içine sonraki eylemlerin bağlantıları konur.
- **Tekrar edilse de sonucu değişmezlik (idempotentlik)**: aynı isteği ikinci kez göndermenin ek bir yan etkisi olmamasıdır. `GET` hem güvenlidir hem idempotenttir. `PUT` kaynağı verilen hâle getirdiği için idempotenttir. `DELETE` de ikinci silmede kaynağı yine "silinmiş" bıraktığı için idempotenttir. `POST` ise her çağrıda yeni bir kayıt yaratma niyeti taşır; bu yüzden idempotent **değildir**.
- **Idempotency-Key**: istemcinin ürettiği benzersiz bir anahtardır. Sunucu aynı anahtarla gelen ikinci isteği tekrar işlemez; ilk sonucu geri döner. `POST` isteğinin **yeniden denenmesini (retry)** güvenli kılan mekanizma budur.
- **Sayfalama (pagination)**: 10 milyon kayıtlık bir listeyi tek yanıtta göndermek mümkün olmadığı için listeyi parça parça (sayfa sayfa) vermek.
- **Kırıcı (breaking) vs ekleyici (additive) değişiklik**: mevcut istemcileri bozan değişiklik ile bozmayan değişiklik.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Richardson olgunluk merdiveni: RPC'den HATEOAS'a">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Richardson olgunluk merdiveni</text>
  <rect x="40" y="150" width="130" height="30" rx="6" fill="#FBEDE4" stroke="#B4531F" stroke-width="1.4"/>
  <text x="105" y="170" text-anchor="middle" font-size="11" font-weight="700" fill="#B4531F" font-family="system-ui">Sv0: RPC / tek uç</text>
  <rect x="180" y="114" width="130" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="245" y="134" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Sv1: kaynaklar</text>
  <rect x="320" y="78" width="130" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="385" y="98" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Sv2: HTTP fiilleri</text>
  <rect x="460" y="42" width="140" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="530" y="62" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Sv3: HATEOAS</text>
  <line x1="105" y1="150" x2="105" y2="144" stroke="#5D7789" stroke-width="1"/>
  <text x="320" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her basamak öncekinin üstüne bir REST kısıtı ekler</text>
</svg>
```

### Nasıl çalışır?

Fiilin anlamı süs değildir; yeniden deneme güvenliğinin temelidir. Ağ koptuğunda istemci isteği yeniden gönderir. Ama sunucunun ilk isteği zaten işleyip yanıtın yolda kaybolduğunu bilemez. `PUT` ve `DELETE` tekrarlansa da sonuç aynıdır. `POST` tekrarlanırsa **çift kayıt** oluşur. Bu yüzden ödeme, sipariş, para transferi gibi `POST` uçlarında Idempotency-Key zorunlu hale gelir.

Sayfalamada iki yaklaşım vardır. **Offset** yöntemi "baştan N kaydı atla, sonraki 10'unu ver" der (`LIMIT 10 OFFSET 10`). Liste değişmiyorsa sorun yoktur. Ama araya yeni kayıt eklendiğinde tüm satırlar bir kayar. O zaman kullanıcı aynı kaydı **iki kez görür ya da bazılarını hiç görmez**. Ayrıca büyük atlama (offset) değerlerinde veritabanı atlanan satırları da taramak zorunda kalır ve sorgu yavaşlar. **İmleç / anahtar-kümesi (cursor / keyset)** yöntemi ise sabit bir çapa kullanır: "son gördüğün anahtardan sonrasını getir" (`WHERE id > son_anahtar LIMIT 10`). Araya ekleme/silme olsa bile pencere kaymaz ve sorgu indeksten hızlıca ilerler.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Offset pagination kayar, cursor pagination sabit çapaya göre ilerler">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Offset vs Cursor sayfalama (araya kayıt eklenince)</text>
  <text x="30" y="56" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">OFFSET 10</text>
  <text x="150" y="56" font-size="10.5" fill="#5D7789" font-family="system-ui">LIMIT 10 OFFSET 10 → yeni satır tümünü kaydırır</text>
  <rect x="150" y="66" width="44" height="30" rx="4" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="172" y="85" text-anchor="middle" font-size="9.5" fill="#8A2E2E" font-family="system-ui">#11</text>
  <rect x="198" y="66" width="44" height="30" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="220" y="85" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">#12</text>
  <rect x="246" y="66" width="44" height="30" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="268" y="85" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">#13</text>
  <text x="330" y="86" font-size="10" fill="#8A2E2E" font-family="system-ui">#11 zaten görülmüştü → TEKRAR / ATLAMA</text>
  <text x="30" y="132" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">CURSOR</text>
  <text x="150" y="132" font-size="10.5" fill="#5D7789" font-family="system-ui">WHERE id &gt; son_anahtar LIMIT 10 → çapa sabit</text>
  <rect x="150" y="142" width="60" height="30" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="180" y="161" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">id=142</text>
  <text x="214" y="161" font-size="14" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="234" y="142" width="60" height="30" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="264" y="161" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">143</text>
  <rect x="298" y="142" width="60" height="30" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="328" y="161" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">144</text>
  <text x="380" y="161" font-size="10" fill="#25804C" font-family="system-ui">ekleme/silme pencereyi kaydırmaz + indeksten hızlı</text>
  <text x="320" y="198" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">büyük OFFSET taranan satırları atlayarak ayrıca yavaşlar</text>
</svg>
```

Hataların da bir sözleşmesi olmalıdır. Her uç kendi keyfine göre farklı biçimde hata JSON'u dönerse istemci hepsini ayrı ayrı ele almak zorunda kalır. Standart yaklaşım `application/problem+json`'dur (RFC 9457, eski adıyla 7807). Gövde tek biçimlidir: `type`, `title`, `status`, `detail` alanlarını, gerekiyorsa alan bazlı hata listesini taşır. Yanında doğru HTTP durum kodu döner ve tamamı **OpenAPI** şemasında belgelenir.

İki yaygın hata vardır. Gövdede "error" yazıp `200 OK` dönmek, hatayı durum kodundan gizler; aradaki vekil sunucular ve istemci isteği başarılı sanır. Yalnız durum kodu dönüp gövdeyi boş bırakmak ise "hangi alan, neden" gibi işe yarar ayrıntıyı kaybettirir.

Son eksen **evrimdir**. Aynı sürüm içinde yalnız **ekleyici** değişiklikler güvenlidir: yanıta yeni bir opsiyonel alan eklemek, yeni bir uç açmak, yeni bir enum değeri getirmek. Bunlar güvenlidir; çünkü iyi yazılmış istemciler "hoşgörülü okuyucu" (tolerant reader) gibi davranır ve tanımadıkları alanları yok sayar. Buna karşılık üç şey kırıcıdır: bir alanı **kaldırmak**, **yeniden adlandırmak** (eski ad kaybolur) ya da opsiyonel bir istek alanını **zorunlu** yapmak.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ekleyici değişim güvenli, kırıcı değişim yeni sürüm gerektirir">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Güvenli mi, kırıcı mı?</text>
  <rect x="24" y="42" width="286" height="146" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="167" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Güvenli (aynı sürüm)</text>
  <text x="44" y="94" font-size="11" fill="#1C3247" font-family="system-ui">+ yeni OPSİYONEL yanıt alanı</text>
  <text x="44" y="120" font-size="11" fill="#1C3247" font-family="system-ui">+ yeni opsiyonel uç / metod</text>
  <text x="44" y="146" font-size="11" fill="#1C3247" font-family="system-ui">+ yeni enum değeri (hoşgörülü)</text>
  <text x="44" y="172" font-size="10" fill="#25804C" font-family="system-ui">hoşgörülü okuyucu bilinmeyeni yok sayar</text>
  <rect x="330" y="42" width="286" height="146" rx="12" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="473" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Kırıcı (yeni sürüm gerekir)</text>
  <text x="350" y="94" font-size="11" fill="#1C3247" font-family="system-ui">− alan kaldırmak</text>
  <text x="350" y="120" font-size="11" fill="#1C3247" font-family="system-ui">~ alanı yeniden adlandırmak</text>
  <text x="350" y="146" font-size="11" fill="#1C3247" font-family="system-ui">! opsiyoneli ZORUNLU yapmak</text>
  <text x="350" y="172" font-size="10" fill="#8A2E2E" font-family="system-ui">/v2 + deprecation penceresi ile göç</text>
</svg>
```

Kırıcı bir değişiklik gerektiğinde doğru desen **sürümlemedir (versioning)**. Yeni davranış `/v2` altında ya da bir sürüm başlığıyla yayınlanır. `v1` ise bir **kullanımdan kaldırma penceresi** (deprecation window) boyunca çalışmaya devam eder. İstemciler kendi hızlarında geçer; eski sürüm önceden duyurulmuş bir tarihte kapatılır. Alanı silip "herhalde kimse kullanmıyordur" ummak bir strateji değildir.

### Mülakatta ne sorulur?

- "Richardson Seviye 3 ne ekler?" → HATEOAS; istemci sonraki adımı yanıttaki bağlantılardan keşfeder.
- "Hangi metot idempotent değildir?" → `POST`; retry güvenliği için Idempotency-Key.
- "Atlamalı (offset) sayfalama neden bozuluyor?" → Liste kaydıkça kayıtlar tekrarlanır ya da atlanır; büyük offset ayrıca yavaştır. Çözüm imleç/anahtar-kümesi sayfalamasıdır.
- "Zorunlu alan eklemem gerekiyor" → kırıcı değişiklik; yeni sürüm + deprecation penceresi.
- "Hangi değişiklik güvenli?" → yeni opsiyonel alan eklemek; kaldırma/yeniden adlandırma/zorunlu yapma kırıcıdır.
- "Hata sözleşmesi nasıl olmalı?" → `application/problem+json` + doğru durum kodu + OpenAPI belgesi.

### Tek cümlede

İyi API tasarımı fiilin anlamına sadık kalır. `POST` yeniden denemesini idempotency anahtarıyla güvenli kılar, listeleri imleçle sayfalar, hataları standart bir gövdeyle döner. Kırıcı değişikliği ise sürümlemeye taşır ve aynı sürüm içinde yalnız ekleyici değişiklik yapar.

# api-1 | İleri
## Soru
Richardson Maturity Model'de Seviye 3, Seviye 2'nin üstüne ne ekler?
## A
HTTP fiillerini ve durum kodlarını doğru anlamlarıyla kullanmak
🤖 O Seviye 2'dir.
## B
Her varlık için ayrı kaynak URI'leri tanıtmak
🤖 O Seviye 1'dir.
## C
Hypermedia kontrolleri (HATEOAS): yanıt, sonraki eylemlerin bağlantılarını taşır
🤖 Doğru: istemci sonraki adımı yanıttaki bağlantılardan keşfeder.
## D
Her işlem için tek bir uç üzerinden RPC çağrısı yapmak
🤖 O Seviye 0'dır.
## Doğru
C
## Açıklama
Richardson merdiveni dört basamaktır: **Sv0** tek uç üzerinden RPC; **Sv1** kaynak URI'leri; **Sv2** HTTP fiilleri + durum kodlarının doğru kullanımı; **Sv3** ise **HATEOAS** — yanıtların içine sonraki eylemleri/ilişkili kaynakları gösteren **hypermedia bağlantıları** koymak, böylece istemci akışı URL'leri sabit-kodlamak yerine yanıttan keşfeder. Fiiller (A) Sv2, kaynaklar (B) Sv1, tek-uç RPC (D) Sv0'dır. Pek çok "REST" API pratikte Sv2'de kalır; Sv3 en olgun ama en az uygulanan seviyedir.

# api-2 | Orta
## Soru
Hangi HTTP metodu tanımca idempotent DEĞİLDİR ve yeniden denemeleri güvenli kılmak için idempotency-key gerektirir?
## A
PUT
🤖 PUT idempotenttir (tekrarlanınca aynı sonuç).
## B
DELETE
🤖 DELETE idempotenttir (iki kez silmek = yine silinmiş).
## C
GET
🤖 GET güvenli (safe) ve idempotenttir.
## D
POST
🤖 Doğru: POST her seferinde yaratır; idempotency-key olmadan retry çift kayıt üretebilir.
## Doğru
D
## Açıklama
İdempotency: aynı isteğin tekrarı ek yan etki yaratmaz. **PUT** (kaynağı verilen hale getirir) ve **DELETE** (siler; ikinci silme yine "silinmiş" bırakır) tanımca idempotenttir; **GET** hem safe hem idempotenttir. **POST** ise her çağrıda yeni bir kaynak yaratma niyeti taşır — ağ retry'ı ile iki kez giderse **çift kayıt** oluşur. Bunu güvenli kılmak için istemci bir **Idempotency-Key** başlığı gönderir; sunucu bu anahtarı görüp aynı anahtarla gelen ikinci isteği tekrar işlemez, ilk sonucu döner. Fiil semantiği bu yüzden "kozmetik" değil, retry güvenliğinin temelidir.

# api-3 | İleri
## Soru
Büyük ve sık değişen bir liste sayfalanıyor. Offset pagination (LIMIT/OFFSET) satırlar kaydıkça yineleme/atlama gösteriyor. Daha iyi yaklaşım nedir?
## A
Cursor/keyset sayfalama: son görülen anahtardan sonrasını getirerek ilerlemek
🤖 Doğru: sabit bir çapaya göre ilerler; kayma sorununu ortadan kaldırır.
## B
OFFSET adımını (sayfa boyutunu) belirgin biçimde büyütmek
🤖 Kaymayı çözmez; ayrıca büyük OFFSET satırları tarayıp atar (yavaş).
## C
Tüm listeyi tek seferde çekip sayfalamayı istemcide yapmak
🤖 Pagination'ın amacını bozar; sınırsız yük.
## D
Her sayfa isteğinde sıralamayı yeniden rastgeleleştirmek
🤖 Yineleme/atlamayı kötüleştirir; kararlı sıra yok.
## Doğru
A
## Açıklama
**Offset pagination** "baştan N satır atla" der; liste altında satır eklenip silindikçe aynı offset artık farklı satırlara denk gelir → kullanıcı bazı kayıtları **iki kez görür veya atlar**. Ek olarak büyük OFFSET, atılan satırları da tarayarak yavaşlar. **Cursor/keyset pagination** bunun yerine kararlı, sıralı bir anahtara göre "şu anahtardan sonrasını getir" der; araya ekleme/silme olsa bile pencere kaymaz ve sorgu `WHERE key > cursor LIMIT n` ile indeksten hızlı ilerler. OFFSET büyütmek (B), her şeyi istemciye taşımak (C) veya rastgele sıralama (D) sorunu çözmez ya da kötüleştirir.

# api-4 | İleri
## Soru
Zorunlu bir alan eklemen ve yanıt şeklini değiştirmen gerekiyor; ama mevcut istemcileri kırmadan. En doğru strateji nedir?
## A
Mevcut v1 yanıtını yerinde değiştirmek; istemciler zamanla uyum sağlar
🤖 Mevcut istemcileri anında kırar.
## B
Yeni bir sürüm yayınlamak (ör. `/v2`) ve v1'i bir deprecation penceresi boyunca açık tutmak
🤖 Doğru: eski istemciler v1'de kalır, yeni istemciler v2'ye taşınır.
## C
Eski alanı kaldırıp artık kimsenin kullanmadığını varsaymak
🤖 Kırıcı; "ummak" bir strateji değildir.
## D
Tüm değişiklikleri aynı uçta, duyuru yapmadan bir gecede uygulamak
🤖 Kırıcı + göç yolu (migration path) yok.
## Doğru
B
## Açıklama
Kırıcı (breaking) bir değişiklik — zorunlu alan eklemek, yanıt şeklini değiştirmek — mevcut istemcileri **yerinde** yapılırsa (A, D) anında bozar. Doğru desen **versiyonlamadır**: yeni davranışı `/v2` (ya da `Accept`/versiyon başlığı) altında yayınlar, `v1`'i bir **deprecation penceresi** boyunca çalışır tutarsın; istemciler kendi hızında geçer, eskiyi belli bir tarihte kaldırırsın. Alanı silip "kimse kullanmıyordur" ummak (C) bir strateji değildir. Versiyonlama, kırıcı evrimi **kontrollü bir göç yoluna** dönüştürür.

# api-5 | İleri
## Soru
Mevcut bir JSON API v1'inde hangi değişiklik güvenlidir (geriye uyumlu)?
## A
Bazı istemcilerin okuduğu bir yanıt alanını yanıttan kaldırmak
🤖 Kırıcı; o alana bağımlı istemci bozulur.
## B
Yanıttaki bir alanı daha anlaşılır olsun diye yeniden adlandırmak
🤖 Kırıcı; eski ad kaybolur.
## C
Yanıta yeni bir opsiyonel alan eklemek
🤖 Doğru: ekleyici (additive) değişiklik; eski istemciler etkilenmez.
## D
Önceden opsiyonel olan bir istek alanını artık zorunlu hale getirmek
🤖 Kırıcı; alanı göndermeyen istemciler bozulur.
## Doğru
C
## Açıklama
Geriye uyumluluğun altın kuralı **ekleyici (additive) değişikliktir**: yanıta **yeni opsiyonel bir alan** eklemek güvenlidir, çünkü hoşgörülü okuyucular (tolerant reader) bilmedikleri alanları yok sayar; eski istemciler hiç etkilenmez (C). Buna karşılık alan **kaldırmak** (A), **yeniden adlandırmak** (B — eski ad kaybolur) veya opsiyonel bir istek alanını **zorunlu** yapmak (D — o alanı göndermeyen istemciler kırılır) hepsi kırıcıdır ve versiyonlama gerektirir. Kural: **ekle, çıkarma/yeniden adlandırma/sıkılaştırma yapma** (aynı versiyon içinde).

# api-6 | İleri
## Soru
Bir API genelinde tutarlı, makine-okur bir hata sözleşmesi için önerilen yaklaşım nedir?
## A
Gövdede bir `error` string'i olan `200 OK` yanıtı dönmek
🤖 Yanlış durum kodu, hatayı istemci/proxy'lerden gizler.
## B
Standart problem detayı (`application/problem+json`, RFC 9457) ve OpenAPI'de belgelenmiş şema
🤖 Doğru: tekbiçim, makine-okur, dokümante hata sözleşmesi.
## C
Her uç için ayrı, gelişigüzel (ad-hoc) biçimde yazılmış hata JSON'u
🤖 Tutarsız; istemci tekbiçim işleyemez.
## D
Yalnız HTTP durum kodu dönmek, yanıt gövdesini hiç doldurmamak
🤖 Eyleme geçirilebilir ayrıntıyı (hangi alan, neden) kaybeder.
## Doğru
B
## Açıklama
Tutarlı hata sözleşmesi için standart, **`application/problem+json`** (RFC 9457, eski adıyla 7807) formatıdır: `type`, `title`, `status`, `detail` (ve gerekirse alan-bazlı hatalar) içeren tekbiçim bir gövde, uygun HTTP durum koduyla birlikte, ve tümü **OpenAPI** şemasında belgelenir. Böylece tüm istemciler hataları aynı şekilde ayrıştırır. **200 + error string** (A) hatayı durum kodundan gizler (proxy/istemci "başarılı" sanır); **uç-başına ad-hoc JSON** (C) tutarsızdır; **yalnız durum kodu** (D) "hangi alan, neden" gibi eyleme geçirilebilir ayrıntıyı kaybeder. Standart + belgeleme = öngörülebilir, makine-okur hatalar.
