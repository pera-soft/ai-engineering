---
slug: redis-dagitik-kilit-redlock
baslik: "Redis Dağıtık Kilit: SET NX PX, Atomik Release, Redlock ve Fencing Token"
kademe: Uzman
sira: 160
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Aynı işi yapan iki sunucun (ya da iki kopyası çalışan bir uygulaman) var. İkisi de aynı anda "42 numaralı siparişi işle" derse sipariş iki kez işlenir, paran iki kez çekilir.

**Kilit (lock)** bu sorunun klasik cevabıdır: bir kaynağa dokunmadan önce "burası benim" diyen bir işaret bırakırsın; işaret duruyorken başkası giremez, iş bitince kaldırırsın.

Tek bir programın içinde bunu dilin kendisi sağlar; Java'da `synchronized` anahtar sözcüğü gibi. Ama **farklı makinelerdeki** süreçler ortak bir hafızayı paylaşmaz. O yüzden işareti hepsinin gördüğü ortak bir yere koymak gerekir. İşte **dağıtık kilit (distributed lock)** budur ve Redis bu iş için çok kullanılır.

### Temel kavramlar

- **Kritik bölge**: aynı anda yalnız bir kişinin girmesi gereken kod parçası.
- **Atomik**: bölünmez tek adım; araya başka bir komut giremez.
- **TTL**: kilide verilen ömür. Kilidi alan çökerse kilit sonsuza kadar kalmasın diye şarttır.
- **Token**: kilidi alan istemcinin ürettiği benzersiz değer — "bu kilit gerçekten benim mi?" sorusunun cevabı.
- **Çöp toplama duraklaması (GC pause)**: Java gibi ortamlarda çöp toplayıcının programı saniyelerce durdurabilmesidir. Süreç uyandığında aradan zaman geçtiğini fark etmez.
- **Saat kayması (clock skew)**: farklı makinelerin saatlerinin birbirinden sapması.

### Nasıl çalışır?

**1) Kilidi al — tek atomik komutla.**

```java
// yoksa-yaz (NX) + TTL (PX), tek atomik komut; token benzersiz
String token = UUID.randomUUID().toString();
String ok = jedis.set("lock:siparis:42", token, SetParams.setParams().nx().px(30000));
if ("OK".equals(ok)) { /* kilit bizde */ }
```

`NX` "yalnızca yoksa yaz" (kilit alımı), `PX` ise milisaniye cinsinden TTL demektir — ikisi de **aynı komutta** kurulur. `token` benzersiz olmalıdır; serbest bırakırken sahipliği onunla doğrulayacağız.

Eski ve **yanlış** desen `SETNX` + ayrı `EXPIRE` kullanmaktır; bunlar iki bağımsız komuttur. Diyelim `SETNX` başarılı oldu ve kilit alındı. Hemen ardından, `EXPIRE` çalışmadan istemci çökerse kilit **TTL'siz** kalır. Artık kimse serbest bırakamaz ve kalıcı bir **ölümcül kilitlenme (deadlock)** oluşur. Tehlike komutların kendisinde değil, **aralarındaki boşluktadır**. `SET NX PX` o boşluğu tasarımdan siler. Aynı sebeple `GET` ile boş mu diye bakıp sonra `SET` yazmak da yarış içerir. `INCR` ile sayaç tutmak ise hiç TTL kurmaz.

**2) İşi yap — ama işin TTL'den uzun sürmemesine dikkat et.** Uzarsa TTL dolar, kilit başkasına geçer ve sen hâlâ kilidin sende olduğunu sanırsın.

**3) Kilidi bırak — sahiplik kontrolüyle ve atomik.**

Düz `DEL lock:...` tehlikelidir. Senaryo şöyledir. A kilidi alır, işi uzar ve TTL dolar. Kilit serbest kalır, B onu alır. Sonra A gecikmeli olarak `DEL` çağırır ve **B'nin kilidini** siler. Artık iki istemci korumasız çalışır.

Çözüm "bu token benim mi? öyleyse sil" kontrolüdür. Ama bu iki adımdır: önce `GET`, sonra `DEL`. Tam o iki adımın **arasında** TTL dolabilir. Bu yüzden ikisini tek bölünmez adıma çeviren bir **Lua betiği** kullanılır. Betik, Redis'in tek komut iş parçacığında bölünmeden çalışır; araya başka komut giremez:

```lua
if redis.call("get", KEYS[1]) == ARGV[1] then
  return redis.call("del", KEYS[1])
else return 0 end
```

Redisson gibi kütüphaneler bunu perde arkasında tam olarak böyle yapar. Lua zorunlu değildir ama atomikliği sağlamanın en pratik yoludur; ağ turlarını azaltması yalnızca yan faydadır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kilidi SET NX PX ile al, işi yap, Lua ile token doğrulayıp sil">
  <defs>
    <marker id="rlock-life-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kilit yaşam döngüsü: al → çalış → bırak</text>
  <rect x="26" y="56" width="170" height="86" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="111" y="80" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">1. AL (atomik)</text>
  <text x="111" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">SET lock token</text>
  <text x="111" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">NX PX 30000</text>
  <text x="111" y="137" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yoksa-yaz + TTL birlikte</text>
  <line x1="196" y1="99" x2="234" y2="99" stroke="#186399" stroke-width="1.6" marker-end="url(#rlock-life-ok)"/>
  <rect x="238" y="56" width="164" height="86" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="80" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">2. ÇALIŞ</text>
  <text x="320" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kritik bölge</text>
  <text x="320" y="122" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">iş &gt; TTL olmasın!</text>
  <text x="320" y="138" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">TTL dolarsa kilit el değiştirir</text>
  <line x1="402" y1="99" x2="440" y2="99" stroke="#186399" stroke-width="1.6" marker-end="url(#rlock-life-ok)"/>
  <rect x="444" y="56" width="172" height="86" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="530" y="80" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">3. BIRAK (atomik)</text>
  <text x="530" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Lua: token benim mi?</text>
  <text x="530" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">öyleyse DEL</text>
  <text x="530" y="137" text-anchor="middle" font-size="10" fill="#8A2E2E" font-family="system-ui">düz DEL başkasınınkini siler</text>
  <text x="320" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Üç adım da atomik; "kilit var ama TTL yok" ve "yanlış kilidi sil" tasarımdan silinir</text>
</svg>
```

**4) Redlock ve Kleppmann eleştirisi.** **Redlock**, kilidi tek bir Redis yerine **N bağımsız düğümün çoğunluğundan** almayı öneren bir algoritmadır. Martin Kleppmann'ın "How to do distributed locking" başlıklı eleştirisi **performansla değil, doğrulukla** ilgilidir.

Eleştirinin özü şudur: Redlock, kilit süresinin anlamlı olması için sistemin **zamanlama varsayımlarına** güvenir. Gerçek sistemlerde ise çöp toplama duraklaması, sürecin askıya alınması, disk ya da ağ gecikmesi veya **saat kayması** yaşanır. Senaryo şöyle işler: A kilidi alır, uzun bir duraklamaya girer, bu sırada TTL dolar ve B kilidi alır. A uyandığında hâlâ "kilit bende" sanır. **Böylece iki istemci aynı anda kritik bölgededir.** Daha çok düğüm eklemek bunu çözmez; sorun düğüm sayısında değil, zaman varsayımındadır.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="GC pause kilidi bayatlatır, fencing token korur">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Neden fencing token şart?</text>
  <line x1="60" y1="70" x2="600" y2="70" stroke="#5D7789" stroke-width="1.3"/>
  <text x="60" y="60" font-size="10.5" fill="#5D7789" font-family="system-ui">zaman →</text>
  <rect x="70" y="80" width="140" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="140" y="100" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">İstemci A kilit alır (t33)</text>
  <rect x="230" y="80" width="150" height="30" rx="6" fill="#FBE9E7" stroke="#B23B2E" stroke-width="1.3"/>
  <text x="305" y="100" text-anchor="middle" font-size="10.5" fill="#B23B2E" font-family="system-ui">A: uzun GC pause</text>
  <text x="305" y="128" text-anchor="middle" font-size="10" fill="#B23B2E" font-family="system-ui">TTL bu sırada dolar</text>
  <rect x="400" y="80" width="150" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="475" y="100" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">İstemci B kilit alır (t34)</text>
  <text x="475" y="128" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">A hâlâ "kilit bende" sanır</text>
  <rect x="90" y="150" width="200" height="40" rx="8" fill="#FBEDD9" stroke="#B26B0E" stroke-width="1.3"/>
  <text x="190" y="167" text-anchor="middle" font-size="10.5" fill="#B26B0E" font-family="system-ui">A yazar: token=33</text>
  <text x="190" y="183" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B2E" font-family="system-ui">REDDEDİLİR (33 &lt; 34)</text>
  <rect x="350" y="150" width="200" height="40" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="450" y="167" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">B yazar: token=34</text>
  <text x="450" y="183" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">KABUL (en yüksek)</text>
  <text x="320" y="214" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Kaynak yalnız gördüğü en yüksek token'dan büyük yazmayı kabul eder</text>
</svg>
```

**5) Kilit jetonu (fencing token).** Kleppmann'ın çözümü şudur: kilit her verildiğinde **sürekli artan** bir sayı üret. Redis'te bunu `INCR`, ZooKeeper'da `zxid` sağlar. İstemci korunan kaynağa yazarken bu jetonu da taşır. **Kaynak** (veritabanı, dosya deposu) ise yalnız o ana kadar gördüğü **en yüksek** jetondan büyük olan yazmayı kabul eder. Böylece gecikmiş eski sahibin düşük jetonlu yazması **reddedilir**. Kritik ayrıntı: bu korumayı kilit değil, **kaynağın kendisi** uygular.

> **[TUZAK]** Redis kilidi tek başına bir **verimlilik (efficiency)** aracıdır: "aynı pahalı işi iki kez yapmayalım" der. Nadir bir çift çalışma zararsızsa bu yeter. Ama **doğruluk (correctness)** için, yani "aynı kaynağa iki yazar asla dokunmasın" garantisi için Redis kilidi **yetmez**. Orada **çit jetonu** şarttır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Redis kilidi efficiency sağlar correctness için fencing token gerekir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Efficiency vs Correctness</text>
  <rect x="34" y="46" width="270" height="132" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Verimlilik (efficiency)</text>
  <text x="169" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"aynı işi iki kez yapmayalım"</text>
  <text x="169" y="118" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Redis kilidi (SET NX PX) YETER</text>
  <text x="169" y="140" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">nadir çift-çalışma zararsız</text>
  <text x="169" y="162" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ör. pahalı raporu bir kez üret</text>
  <rect x="336" y="46" width="270" height="132" rx="10" fill="#F6E4E4" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="471" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Doğruluk (correctness)</text>
  <text x="471" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"iki yazar asla aynı kaynağa"</text>
  <text x="471" y="118" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">Redis kilidi tek başına YETMEZ</text>
  <text x="471" y="140" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">GC pause / clock skew riski</text>
  <text x="471" y="162" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">→ fencing token ŞART</text>
</svg>
```

### Mülakatta ne sorulur?

- "Kilidi nasıl alırsın?" → `SET key <token> NX PX <ttl>`; yoksa-yaz ve TTL tek atomik komutta.
- "`SETNX` + `EXPIRE` nesi kötü?" → arada çökme, TTL'siz kilit, kalıcı deadlock.
- "Neden düz `DEL` değil?" → TTL dolmuşsa başkasının kilidini silersin; sahiplik doğrulanmalı.
- "Neden Lua?" → `GET` ile `DEL` arasını tek bölünmez adıma çevirmek için. Ağ tasarrufu yalnız yan faydadır.
- "Kleppmann ne diyor?" → Zamanlama varsayımları (çöp toplama duraklaması, saat kayması) doğruluk garantisini bozar.
- "Ne eklersin?" → Kilit jetonu (fencing token). Kaynak yalnız gördüğü en yüksek jetondan büyük olanı kabul eder.

### Tek cümlede

Redis kilidi doğru kurulduğunda işin iki kez yapılmasını büyük ölçüde önler: `SET NX PX` ile alırsın, Lua ile sahipliği doğrulayıp bırakırsın. Ama "asla iki yazar" garantisi istiyorsan kilide güvenemezsin; kaynağın uyguladığı çit jetonuna güvenmelisin.

# rlock-1 | Temel
## Soru
Redis ile dağıtık kilit almanın DOĞRU, tek komutlu yolu hangisidir?
## A
`SETNX key <token>` çalıştırıp hemen ardından ayrı bir `EXPIRE key 30` çalıştırmak
🤖 İki ayrı komut; aralarındaki boşluk kilidi TTL'siz bırakabilir.
## B
`SET key <token> NX PX 30000` ile yoksa-yaz ve TTL'i birlikte kurmak
🤖 Doğru: kilit ve süresi aynı bölünmez adımda kurulur.
## C
`GET key` ile boş mu diye bakıp, boşsa `SET key <token>` yazmak
🤖 `GET` ile `SET` arası yarış vardır; iki istemci aynı anda "boş" görebilir.
## D
`INCR key` ile sayacı artırıp sonuç 1 ise kilidi almış saymak
🤖 `INCR` TTL kurmaz; temizlenmezse sayaç kilidi kalıcı olarak tutar.
## Doğru
B
## Açıklama
Doğru desen **`SET key token NX PX ttl`**'dir: `NX` "yalnızca yoksa yaz" (kilit alımı), `PX` milisaniye cinsinden TTL'i **aynı atomik komutta** kurar; `token` benzersiz olmalıdır (serbest bırakırken sahiplik kontrolü için). `SETNX` + `EXPIRE` (A) iki komuttur ve arada çökme TTL'siz bir kilit bırakır; `GET` sonra `SET` (C) klasik bir yarış içerir; `INCR` (D) ne kilit semantiği ne de TTL sağlar. Tek komut olması, "kilit var ama süresi yok" durumunu tasarımdan siler.

# rlock-2 | Orta
## Soru
Eski `SETNX key` + ayrı `EXPIRE key 30` deseninin temel kusuru nedir?
## A
İki ayrı komuttur; aralarında istemci çökerse kilit TTL'siz kalır ve deadlock olur
🤖 Doğru: "kilit alındı ama süresi kurulamadı" penceresi ölümcüldür.
## B
`EXPIRE` saniye, `PX` ise milisaniye alır; desenin tek kusuru bu birim farkıdır
🤖 Birim farkı gerçek ama zararsız; asıl kusur iki adımın atomik olmayışıdır.
## C
`EXPIRE` komutu kendi başına atomiktir, bu yüzden desen tümüyle güvenlidir
🤖 Her komut tek tek atomik olsa da tehlike tam aralarındaki boşluktadır.
## D
İki komut art arda gittiği için gereksiz CPU harcar ve Redis'i yavaşlatır
🤖 Sorun hız değil; atomik olmayan iki adımın arasındaki çökme penceresidir.
## Doğru
A
## Açıklama
`SETNX` + `EXPIRE` iki bağımsız komuttur. `SETNX` başarılı olup kilit alındıktan **hemen sonra**, `EXPIRE` çalışmadan istemci çöker (ya da ağ kopar) ise kilit **TTL'siz** kalır — kimse serbest bırakamaz, sonsuza dek tutulur; bu **kalıcı deadlock**'tur. Komutların tek tek atomik olması deseni güvenli yapmaz (C yanlış), çünkü tehlike **aralarındaki** boşluktadır. `EXPIRE`'ın saniye, `PX`'in milisaniye alması gerçek ama zararsız bir ayrıntıdır (B); sorun performans da değildir (D). Bu tam olarak `SET NX PX`'in tek atomik komutla çözdüğü problemdir.

# rlock-3 | Orta
## Soru
Kilidi serbest bırakırken neden düz `DEL key` kullanmak tehlikelidir?
## A
`DEL` büyük anahtarlarda yavaş çalıştığı için kilit geç serbest kalır
🤖 Hız sorunu değil; asıl tehlike başkasının kilidini silme ihtimalidir.
## B
`DEL` kilit anahtarını gerçekten silmez, yalnız üzerindeki TTL'i sıfırlar
🤖 `DEL` anahtarı tümüyle siler; sorun silmemesi değil, YANLIŞ kilidi silebilmesidir.
## C
Hiçbir tehlikesi yoktur; kilidi bırakmanın standart ve önerilen yolu `DEL`'dir
🤖 Sahiplik kontrolü olmadan `DEL` başkasının kilidini silebilir; standart değildir.
## D
TTL'in dolup kilit başkasına geçmişse, düz `DEL` onun kilidini siler
🤖 Doğru: sahiplik doğrulanmadan silmek, yeni sahibi korumasız bırakır.
## Doğru
D
## Açıklama
Kilidi düz `DEL` ile bırakmak sahipliği **doğrulamaz**. Senaryo: A kilidi alır, işi uzar, **TTL dolar**, kilit otomatik serbest kalır ve B onu alır; sonra A gecikmeli biçimde `DEL` çağırır ve **B'nin** kilidini siler — artık iki istemci koruma olmadan kaynağa girebilir. Bu yüzden serbest bırakırken önce "kilitteki token benim token'ım mı?" kontrolü yapılmalı, ancak öyleyse silinmelidir. `DEL` anahtarı gerçekten siler (B yanlış), sorun hız değildir (A) ve bu yol standart kabul edilmez (C). Kontrol ile silme adımının kendisi de atomik olmalıdır — bir sonraki soru bunu ele alıyor.

# rlock-4 | İleri
## Soru
Kilidi bırakırken "token'ı kontrol et, eşleşiyorsa sil" işlemini neden Lua script ile yaparız?
## A
Lua kodu Redis komutlarına göre daha okunaklı ve bakımı kolay olduğu için
🤖 Okunabilirlik değil; asıl neden `GET` ile `DEL` arasını atomikleştirmektir.
## B
Yalnız ağ turlarını (round-trip) azaltmak için; atomiklikle hiçbir ilgisi yoktur
🤖 Ağ tasarrufu yan faydadır; asıl amaç iki adımı bölünmez tek işlem yapmaktır.
## C
`GET` ile `DEL` arasını atomikleştirmek için; arada TTL dolup kilit el değiştirebilir
🤖 Doğru: script tek thread'de bölünmez çalışır, araya başka komut giremez.
## D
Redis bu işlem için Lua'yı zorunlu kılar; başka hiçbir yöntem kabul edilmez
🤖 Zorunlu değildir; Lua yalnızca atomikliği sağlamanın en pratik yoludur.
## Doğru
C
## Açıklama
"Token'ı kontrol et, eşleşiyorsa sil" iki adımdır: `GET` ile sahipliği doğrula, sonra `DEL`. Bunları ayrı komutlar olarak yaparsan **tam aralarında** TTL dolabilir ve kilit başkasına geçebilir; sonra senin `DEL`'in yeni sahibin kilidini siler — aynı tehlike geri gelir. **Lua script** bu iki adımı Redis'in tek komut thread'inde **bölünmez (atomik)** tek işlem olarak çalıştırır; araya başka komut giremez. Okunabilirlik (A) veya ağ tasarrufu (B) yalnız yan etkidir; Lua zorunlu değildir ama en pratik atomik yoldur (D yanlış). Redisson gibi kütüphaneler bunu perde arkasında Lua ile yapar.

# rlock-5 | Uzman
## Soru
Martin Kleppmann'ın Redlock algoritmasına yönelik temel eleştirisi nedir?
## A
Redlock çok yavaştır; her kilit için birden çok node'a yazmak gecikmeyi büyütür
🤖 İtiraz performansla değil, doğruluk (correctness) garantisiyle ilgilidir.
## B
Zamanlama varsayımlarına dayanır; GC pause veya saat kayması iki istemciyi içeri alabilir
🤖 Doğru: zaman garantisi olmayan sistemlerde correctness veremez.
## C
Redis tek node'da kilit desteklemediği için Redlock baştan gereksiz bir çabadır
🤖 Redis tek node'da da kilit sunar; eleştiri çok node'lu doğruluk iddiasınadır.
## D
Redlock Lua script'e bağımlı olduğu için istemciler arasında taşınabilir değildir
🤖 Lua ile ilgisi yok; itiraz zamanlama varsayımlarına ve fencing eksiğinedir.
## Doğru
B
## Açıklama
Kleppmann'ın "How to do distributed locking" eleştirisi **doğruluk (correctness)** üzerinedir, performans üzerine değil (A). Redlock, kilit süresinin anlamlı olması için sistemin **zamanlama varsayımlarına** güvenir. Ama gerçek sistemlerde **GC pause**, sürecin askıya alınması (STW), disk/ağ gecikmesi veya **saat kayması (clock skew)** olabilir: A kilidi alır, uzun bir duraklamaya girer, bu sırada TTL dolar ve B kilidi alır — A uyanınca hâlâ "kilit bende" sanır ve **iki istemci aynı anda** kritik bölgede olur. Redis tek node'da da kilit sunar (C yanlış); itiraz Lua ile ilgili değildir (D). Kleppmann'ın çözümü bir sonraki sorudadır: **fencing token**.

# rlock-6 | Uzman
## Soru
Redis dağıtık kilidini, paylaşılan bir kaynağa yazma DOĞRULUĞU (correctness) için güvenli kılan ekleme nedir?
## A
Fencing token: kaynak yalnız en yüksek token'dan büyük yazmayı kabul eder
🤖 Doğru: gecikmiş eski sahibin düşük token'lı yazması kaynakta reddedilir.
## B
Kilit TTL'ini çok uzun (ör. 10 dakika) tutup el değiştirme ihtimalini azaltmak
🤖 Uzun TTL duraklama riskini büyütür ve kurtarmayı yavaşlatır; garanti vermez.
## C
Redis node sayısını artırıp Redlock çoğunluğunu daha geniş bir kümeye yaymak
🤖 Daha çok node zamanlama sorununu çözmez; itiraz tam da buraya yöneliktir.
## D
Serbest bırakmada `DEL` yerine `UNLINK` kullanıp silme işini arka plana almak
🤖 `UNLINK` yalnız bir silme yöntemidir; kilidin doğruluğuyla ilgisi yoktur.
## Doğru
A
## Açıklama
**[TUZAK]** Redis kilidi tek başına bir **verimlilik (efficiency)** aracıdır: çoğu zaman işin iki kez yapılmasını önler, ama nadir zamanlama arızalarında (GC pause, clock skew) iki istemcinin **aynı anda** kilidi elinde sanması mümkündür. **Doğruluk (correctness)** — yani kaynağa asla iki eşzamanlı yazarın dokunmaması — için **fencing token** şarttır: kilit her verildiğinde **monoton artan** bir sayı üretilir (ör. Redis `INCR` veya ZooKeeper `zxid`); istemci kaynağa yazarken token'ı taşır ve **korunan kaynak** (DB, dosya deposu) yalnız gördüğü **en yüksek** token'dan büyük yazmayı kabul eder. Böylece gecikmiş eski sahibin düşük token'lı yazması **reddedilir**. Uzun TTL (B) riski büyütür; daha çok node (C) zamanlama sorununu çözmez; `UNLINK` (D) yalnız bir silme detayıdır. Kural: kilidi verimlilik için kullan, doğruluğu fencing token'a yaptır.
