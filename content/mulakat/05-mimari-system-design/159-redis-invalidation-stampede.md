---
slug: redis-invalidation-stampede
baslik: "Cache Invalidation: Stampede, Penetration ve Avalanche Savunmaları"
kademe: Uzman
sira: 159
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Önbellek işe yaradığı sürece harikadır. Sorun, **önbelleğin boşaldığı anlarda** çıkar. Bu bölüm, önbellek çalışmadığında sistemin nasıl çöktüğünü ve bunu önlemenin yollarını anlatır.

Basit bir benzetme yapalım. Bir sinema gişesi düşün. Bilet fiyatı bir tabelaya yazılmış; tabela burada önbellektir. Tabela indirildiği anda kuyruktaki 500 kişi aynı anda gişeye "fiyat ne?" diye sorarsa gişe kilitlenir. Aşağıdaki üç felaket bu resmin üç farklı hâlidir. Üçü de önbelleğin **dolu olduğu** anla değil, **boş olduğu** anla ilgilidir.

### Temel kavramlar

- **Miss (ıskalama)**: aranan değer önbellekte yok; asıl kaynağa (DB) gitmek gerekir.
- **Expire (süre dolumu)**: TTL bitti, anahtar silindi. O andan sonraki ilk istek zorunlu olarak miss'tir.
- **Yeniden hesaplama (recompute)**: miss sonrası değeri DB'den ya da hesaptan yeniden üretmek. Pahalı olan budur.
- **Jitter**: bir süreye eklenen küçük **rastgele sapma** — "hepsi aynı anda olmasın" hilesi.
- **Bloom filtresi**: elemanları saklamadan "bu küme içinde var mı?" sorusuna kompakt cevap veren olasılıksal yapı.

### Nasıl çalışır?

**1) İzdiham (stampede / thundering herd / dogpile).** Çok okunan bir anahtarın TTL'i dolduğu **tam anda**, o anahtarı isteyen yüzlerce eşzamanlı istek birden ıska alır. **Hepsi** aynı pahalı hesabı veritabanında yapar. Tek bir anahtarın düşmesi veritabanına N katlı ani bir yük bindirir; gecikme patlar, bazen veritabanı çöker. Bunun ne kalıcılıkla, ne tahliyeyle, ne de ağ arızasıyla ilgisi vardır. Tamamen önbellek mantığından doğan bir yük problemidir.

Savunmalar:

- **Karşılıklı dışlama / tek uçuş (mutex / single-flight)**: Iska anında istekler bir kilit anahtarı için yarışır: `SET lock:key <token> NX PX 5000`. **Yalnız kazanan** değeri hesaplar ve önbelleği doldurur. Diğerleri kısa süre bekleyip önbelleği tekrar okur; elde varsa **bayat** değeri döner. Böylece veritabanına N yerine **1** istek gider. İki inceliği vardır: kilide mutlaka TTL koy (sahibi çökerse diye) ve bekleyenleri sonsuza kadar bekletme.
- **Olasılığa dayalı erken yenileme (PER / XFetch)**: Değerle birlikte iki bilgiyi sakla — hesaplama süresi (`delta`) ve son kullanma zamanı. Her okumada, süre dolumuna **yaklaştıkça artan** bir olasılıkla **tek bir** istek değeri arka planda yeniler; eşik `delta * beta * ln(rand())` biçiminde hesaplanır. Böylece anahtar "sıfır anını" hiç yaşamaz ve kitlesel eşzamanlı ıska oluşmaz. (Vattani ve arkadaşlarının "optimal probabilistic cache stampede prevention" çalışması.)
- **Jitter'lı TTL**: düşüşleri zamana yayarak aynı anı seyrekleştirmek.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cache stampede: expire anında herkes DB'ye gider">
  <defs>
    <marker id="rinval-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B2E"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Stampede: expire anı = kitlesel miss</text>
  <rect x="30" y="52" width="150" height="120" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="105" y="74" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">yüzlerce istek</text>
  <text x="105" y="104" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">req · req · req</text>
  <text x="105" y="124" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">req · req · req</text>
  <text x="105" y="144" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">req · req · req</text>
  <rect x="245" y="82" width="150" height="60" rx="10" fill="#FBE9E7" stroke="#B23B2E" stroke-width="1.4"/>
  <text x="320" y="108" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B2E" font-family="system-ui">cache MISS</text>
  <text x="320" y="128" text-anchor="middle" font-size="10.5" fill="#B23B2E" font-family="system-ui">key expire oldu</text>
  <rect x="460" y="82" width="150" height="60" rx="10" fill="#FBEDD9" stroke="#B26B0E" stroke-width="1.4"/>
  <text x="535" y="108" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B0E" font-family="system-ui">Veritabanı</text>
  <text x="535" y="128" text-anchor="middle" font-size="10.5" fill="#B26B0E" font-family="system-ui">ezilir</text>
  <line x1="180" y1="112" x2="241" y2="112" stroke="#B23B2E" stroke-width="1.6" marker-end="url(#rinval-ok)"/>
  <line x1="395" y1="112" x2="456" y2="112" stroke="#B23B2E" stroke-width="1.6" marker-end="url(#rinval-ok)"/>
  <rect x="90" y="184" width="460" height="30" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="320" y="204" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">Savunma: mutex · PER (erken yenile) · jitter'lı TTL</text>
</svg>
```

**2) Delinme (penetration).** İstekler **var olmayan** bir veriyi sorar; geçersiz ya da rastgele kimlikler gibi. Önbellek asla dolamaz, çünkü veritabanı da "yok" der. Sonuçta her istek veritabanına iner. Bu bazen kaza, bazen kasıtlı bir saldırı yöntemidir. TTL uzatmanın hiçbir etkisi yoktur: önbelleğe hiç girmeyen bir değerin ömrünü uzatamazsın. `maxmemory` ya da tahliye politikasını değiştirmek de bu soruna dokunmaz.

Savunmalar:

- **Null / tombstone önbelleği**: "bu id yok" sonucunu **kısa** TTL ile önbellekle, tekrarlar DB'yi vurmasın.
- **Bloom filtresi**: var olan kimliklerin kümesini az yer kaplayarak tutan, olasılığa dayalı bir üyelik yapısıdır. İki cevabı vardır: "**kesinlikle yok**" ya da "**muhtemelen var**". Yani **yanlış pozitif** mümkündür: var olmayana "olabilir" der ve nadiren boşa bir veritabanı sorgusu geçer. Ama **yanlış negatif asla** olmaz; var olanı hiçbir zaman "yok" demez. Delinme savunmasında onu güvenli kılan tam da budur: "yok" dediğinde bu kesindir ve veritabanına hiç gitmezsin.

  Ödünleşmesi şudur: az yer kaplar ama olasılığa dayalıdır ve üyelerin kendisini saklamaz. Ayrıca **standart Bloom filtresinden üye silinemez**; silme gerekiyorsa **Counting Bloom** ya da **Cuckoo filtresi** kullanılır. Redis 8 / Redis Stack bunları hazır modül olarak sunar.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bloom filtresi var olmayan idyi DBye ulaşmadan eler, olabilir derse cache ve DB devreye girer">
  <defs>
    <marker id="rinval-bloom-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#6B3FA0"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Penetration savunması: Bloom filtresi</text>
  <rect x="24" y="76" width="104" height="48" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="76" y="98" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">istek</text>
  <text x="76" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">id sorgusu</text>
  <line x1="128" y1="100" x2="188" y2="100" stroke="#6B3FA0" stroke-width="1.5" marker-end="url(#rinval-bloom-ok)"/>
  <rect x="192" y="72" width="120" height="56" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.6"/>
  <text x="252" y="96" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Bloom filtresi</text>
  <text x="252" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">üyelik testi</text>
  <line x1="312" y1="88" x2="392" y2="66" stroke="#25804C" stroke-width="1.5" marker-end="url(#rinval-bloom-ok)"/>
  <text x="352" y="60" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">"kesin YOK"</text>
  <rect x="396" y="48" width="220" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="506" y="70" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">DB'ye HİÇ gitme, hemen dön</text>
  <line x1="312" y1="112" x2="392" y2="134" stroke="#B26B00" stroke-width="1.5" marker-end="url(#rinval-bloom-ok)"/>
  <text x="352" y="140" text-anchor="middle" font-size="10" fill="#B26B00" font-family="system-ui">"olabilir"</text>
  <rect x="396" y="120" width="220" height="34" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="506" y="142" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">cache → miss ise DB'ye sor</text>
  <text x="320" y="184" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">false positive olur (nadir boşa DB), false negative ASLA</text>
  <text x="320" y="202" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">alternatif: "yok" sonucunu kısa TTL ile null/tombstone cache</text>
</svg>
```

**3) Çığ (avalanche).** Çok sayıda anahtarın süresi **aynı anda** dolar ve veritabanına tek bir dalga hâlinde yük biner. Klasik sebebi hepsine **aynı sabit TTL** vermektir; örneğin gece 03:00'te toplu ısıtma yapıp hepsine tam 1 saat vermek.

En basit ve etkili ilk önlem **sapmalı (jitter'lı) TTL**'dir: TTL'e rastgele küçük bir sapma ekle (`taban ± %10`) ki süre dolumları zamana yayılsın. Bunu üç şey tamamlar: **çok katmanlı önbellek** (uygulama içi yerel önbellek + Redis), veritabanını koruyan **devre kesici (circuit breaker)** ve kademeli yeniden ısıtma. TTL'i tamamen kaldırmak çözüm değildir; bayat veri ve bellek sızıntısı getirir. "Hepsini saat başında topluca yenile" yaklaşımı ise yeni bir toplu dalga üretir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sabit TTL hepsini aynı anda düşürür, jitterlı TTL expireları zamana yayar">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Avalanche savunması: jitter'lı TTL</text>
  <text x="160" y="50" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Sabit TTL: senkron düşüş</text>
  <line x1="40" y1="120" x2="280" y2="120" stroke="#5D7789" stroke-width="1.2"/>
  <text x="40" y="138" font-size="10" fill="#5D7789" font-family="system-ui">zaman →</text>
  <line x1="150" y1="66" x2="150" y2="120" stroke="#8A2E2E" stroke-width="2"/>
  <line x1="155" y1="66" x2="155" y2="120" stroke="#8A2E2E" stroke-width="2"/>
  <line x1="160" y1="66" x2="160" y2="120" stroke="#8A2E2E" stroke-width="2"/>
  <line x1="165" y1="66" x2="165" y2="120" stroke="#8A2E2E" stroke-width="2"/>
  <line x1="170" y1="66" x2="170" y2="120" stroke="#8A2E2E" stroke-width="2"/>
  <text x="160" y="160" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">hepsi aynı an → DB ezilir ⚠</text>
  <line x1="320" y1="40" x2="320" y2="180" stroke="#5D7789" stroke-width="1" stroke-dasharray="3 3"/>
  <text x="480" y="50" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Jitter'lı TTL: yayılmış</text>
  <line x1="360" y1="120" x2="600" y2="120" stroke="#5D7789" stroke-width="1.2"/>
  <text x="360" y="138" font-size="10" fill="#5D7789" font-family="system-ui">zaman →</text>
  <line x1="378" y1="80" x2="378" y2="120" stroke="#25804C" stroke-width="2"/>
  <line x1="410" y1="80" x2="410" y2="120" stroke="#25804C" stroke-width="2"/>
  <line x1="452" y1="80" x2="452" y2="120" stroke="#25804C" stroke-width="2"/>
  <line x1="500" y1="80" x2="500" y2="120" stroke="#25804C" stroke-width="2"/>
  <line x1="556" y1="80" x2="556" y2="120" stroke="#25804C" stroke-width="2"/>
  <text x="480" y="160" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">taban ± %10 → yük zamana yayılır</text>
</svg>
```

Üçünü birbirine karıştırma. Sapmalı TTL çığı ve kısmen izdihamı hafifletir; ama delinmeye hiç dokunmaz. Bloom filtresi delinmeyi çözer; ama izdihamı çözmez. Her felaketin savunması ayrıdır.

### Mülakatta ne sorulur?

- "İzdiham (stampede) nedir?" → Popüler bir anahtarın süresi dolduğu anda N eşzamanlı isteğin aynı değeri N kez hesaplamasıdır.
- "Nasıl önlersin?" → mutex/tek uçuş, PER ile erken yenileme, jitter'lı TTL.
- "PER ne yapar?" → TTL dolmadan, artan bir olasılıkla tek istek değeri tazeler; anahtar hiç boşalmaz.
- "Penetration savunması?" → null/tombstone önbelleği ve Bloom filtresi. TTL artırmak işe yaramaz.
- "Bloom'un ödünleşmesi?" → false positive var, false negative yok; standart Bloom'dan silme yapılamaz.
- "Avalanche'ın ilk önlemi?" → jitter'lı TTL; ardından çok katmanlı önbellek ve devre kesici.

### Tek cümlede

Önbellek felaketleri, önbelleğin çalıştığı anda değil boşaldığı anda doğar. İzdiham tek anahtarın düşmesidir. Çığ çok sayıda anahtarın birlikte düşmesidir. Delinme ise önbelleğin hiç dolamaması sorunudur. Her birinin savunması ayrı düşünülmelidir.

# rinval-1 | Temel
## Soru
"Cache stampede" (thundering herd / dogpile) nedir?
## A
Önbellekteki verinin belirli aralıklarla diske yazılıp kalıcı hâle getirilmesi işlemi
🤖 Bu kalıcılıktır (persistence); stampede eşzamanlı bir miss fırtınasıdır.
## B
Bellek tavanına çarpıldığında Redis'in yer açmak için eski anahtarları atması işlemi
🤖 O tahliyedir (eviction); stampede yeniden hesaplama yığılmasıdır.
## C
Popüler bir anahtarın expire anında yüzlerce isteğin aynı değeri yeniden hesaplaması
🤖 Doğru: tek anahtarın düşmesi DB'ye ani ve katmerli bir yük bindirir.
## D
Ağ bağlantısının kopması sonucu istemcilerin Redis sunucusuna hiç ulaşamaması durumu
🤖 Bu bir bağlantı arızasıdır; stampede önbellek mantığından doğan bir yük problemidir.
## Doğru
C
## Açıklama
**Stampede** (thundering herd / dogpile): yoğun okunan bir anahtarın TTL'i dolduğu **tam an**, o anahtarı isteyen tüm eşzamanlı istekler aynı anda **miss** alır ve hepsi değeri DB'den **yeniden hesaplar** — DB'ye ani ve katmerli bir yük biner, gecikme patlar, bazen sistem çöker. Bu ne kalıcılıktır (A), ne tahliyedir (B), ne de ağ arızasıdır (D). Sorunun özü, pahalı bir hesaplamanın tek bir anahtarın düşmesiyle **N kez** aynı anda yapılmasıdır. Savunmalar bunu "yalnız bir kez hesapla" veya "hiç aynı anda düşürme" fikrine indirger.

# rinval-2 | Orta
## Soru
Cache stampede'i önlemek için "mutex/lock" (tek uçuş) yaklaşımı nasıl çalışır?
## A
Tüm anahtarları TTL'siz ve kalıcı yaparak süre dolumunu tamamen ortadan kaldırmak
🤖 Bu bellek sızıntısı ve bayat veri yaratır; mutex yaklaşımı bu değildir.
## B
Miss anında gelen tüm istekleri reddedip istemciye doğrudan bir hata döndürmek
🤖 İstek reddi değil; amaç değeri bir kez hesaplayıp herkese servis etmektir.
## C
DB tablosunu kilitleyerek eşzamanlı okumaların tek tek sıraya girmesini sağlamak
🤖 DB'yi kilitlemek yükü çözmez, kuyruğa sokar; mutex önbellek tarafındadır.
## D
Miss'te yalnız bir istek kilidi alıp hesaplar, diğerleri kısa süre bekler
🤖 Doğru: tek yeniden hesaplayıcı, geri kalanı bekler; DB'ye tek yük biner.
## Doğru
D
## Açıklama
**Mutex (tek uçuş) yaklaşımı**: anahtar miss olunca istekler bir kilit anahtarına `SET lock:key token NX PX 5000` ile yarışır. **Yalnız kazanan** DB'den değeri hesaplar ve önbelleği doldurur; diğerleri ya kısa süre bekleyip önbelleği tekrar okur ya da elde varsa **bayat** değeri döner. Böylece DB'ye N yerine **1** istek gider. Bu, tüm anahtarları kalıcı yapmak (A), istekleri reddetmek (B) veya DB tablosunu kilitlemek (C) değildir. Kilit tabanlı çözümün inceliği: kilit sahibi çökerse diye kilide TTL koymak ve bekleyenlerin sonsuza kadar beklememesini sağlamaktır.

# rinval-3 | İleri
## Soru
"Probabilistic Early Recomputation" (PER / XFetch) stampede'i nasıl azaltır?
## A
Süre dolumundan önce, artan bir olasılıkla tek isteğe erken yenileme yaptırır
🤖 Doğru: expire'a yaklaştıkça olasılık artar, yük tek isteğe iner.
## B
TTL'i sonsuza çıkarıp anahtarın hiçbir zaman expire olmamasını garanti eder
🤖 O zaman veri hep bayat kalır; PER erken YENİLER, expire'ı kaldırmaz.
## C
Expire anında gelen tüm istekleri geçici olarak reddederek DB'yi yükten korur
🤖 İstek reddetmez; amaç expire anını hiç yaşamadan değeri tazelemektir.
## D
Değeri birden çok Redis replikasına kopyalayarak okuma yükünü node'lara dağıtır
🤖 Replikasyon okuma yükünü dağıtır ama eşzamanlı yeniden hesaplamayı çözmez.
## Doğru
A
## Açıklama
**PER (Probabilistic Early Recomputation / XFetch)**: değerle birlikte hesaplama süresini (`delta`) ve son kullanma zamanını sakla. Her okumada, süre dolumuna **ne kadar yaklaşıldığına** bağlı artan bir olasılıkla (`delta * beta * ln(rand())` biçiminde bir eşik) **tek bir** istek, TTL **dolmadan önce** değeri arka planda yeniden hesaplar ve önbelleği tazeler. Böylece anahtar hiç "sıfır anını" yaşamaz, kitlesel eşzamanlı miss oluşmaz. TTL'i sonsuz yapmak (B) veriyi bayat bırakır; istek reddi (C) veya replikasyon (D) bu problemi çözmez. Vattani ve arkadaşlarının "optimal probabilistic cache stampede prevention" çalışması bu yaklaşımın temelidir.

# rinval-4 | İleri
## Soru
"Cache penetration" (var olmayan anahtarlara, ör. geçersiz id'lere sürekli sorgu) nasıl savunulur?
## A
İlgili anahtarların TTL değerini belirgin biçimde artırıp önbellekte daha uzun tutmak
🤖 Var olmayan veri önbelleğe hiç girmez; TTL uzatmanın hiçbir etkisi olmaz.
## B
"Yok" sonucunu kısa TTL ile önbelleklemek ve/veya Bloom filtresiyle ön elemek
🤖 Doğru: yokluğu da önbellekle ya da filtreyle ele; böylece DB korunur.
## C
`maxmemory` değerini düşürerek bellek baskısı yaratıp tahliyeyi hızlandırmak
🤖 Bellek ayarı penetration'ı çözmez; sorun var olmayana giden sorgulardır.
## D
Tahliye politikasını `allkeys-lfu`'ya çevirip sıcak anahtarları korumaya almak
🤖 LFU sıcak anahtarları korur ama var olmayana giden sorguları durdurmaz.
## Doğru
B
## Açıklama
**Penetration**'da istekler **var olmayan** veriyi sorar (geçersiz veya rastgele id'ler); önbellek asla dolamaz çünkü DB de "yok" döner ve her istek DB'ye iner — bu bir saldırı vektörü de olabilir. İki savunma vardır: (1) **Null/tombstone önbelleği** — "bu id yok" sonucunu kısa TTL ile önbellekle ki tekrarlar DB'yi vurmasın. (2) **Bloom filtresi** — var olan id'lerin kümesini kompakt tutup, filtrede olmayan id'yi DB'ye **hiç göndermeden** ele. TTL artırmak (A), `maxmemory` düşürmek (C) veya LFU'ya geçmek (D) bu probleme dokunmaz. Redis 8 / Redis Stack'teki **Bloom filtresi** bunun için hazır bir araçtır.

# rinval-5 | Uzman
## Soru
"Cache avalanche" (çığ) — çok sayıda anahtarın aynı anda expire olup DB'yi ezmesi — için en pratik ilk önlem hangisidir?
## A
Tüm anahtarlara aynı sabit TTL vererek süre dolumunu öngörülebilir kılmak
🤖 Bu tam olarak avalanche'ın sebebidir; hepsi aynı anda expire olur.
## B
Anahtarların TTL'ini tamamen kaldırıp süre dolumunu devre dışı bırakmak
🤖 Bu bayat veri ve bellek sızıntısı yaratır; expire'ı yaymaz, yok eder.
## C
TTL'lere rastgele sapma (jitter) ekleyip süre dolumlarını zamana yaymak
🤖 Doğru: jitter senkron düşüşü kırar; en ucuz ve etkili ilk önlemdir.
## D
Tüm anahtarları her saat başında topluca yenileyip önbelleği taze tutmak
🤖 Bu da senkron bir dalga yaratır; avalanche'ı hafifletmez, tetikler.
## Doğru
C
## Açıklama
**Avalanche**, çok sayıda anahtarın **aynı anda** expire olmasıyla DB'ye senkron bir dalga bindirir; genelde hepsine **aynı sabit TTL** verildiğinde olur (A yanlış: sebebin ta kendisidir). En basit ve etkili önlem **jitter**'dır: TTL'e rastgele küçük bir sapma ekle (`taban ± %10`) ki expire anları zamana yayılsın ve senkron düşüş olmasın. Bunu **çok katmanlı önbellek** (yerel + Redis) ve DB'yi koruyan **devre kesici (circuit breaker)** tamamlar. TTL'i kaldırmak (B) bayatlık ve sızıntı yaratır; topluca yenileme (D) yeni bir dalga üretir. Jitter hem avalanche hem stampede için ucuz ve etkili bir varsayılandır.

# rinval-6 | Uzman
## Soru
Bloom filtresini cache penetration savunmasında kullanınca hangi ödünleşmeyi (trade-off) kabul edersin?
## A
Hiç yanılmaz; üyelik sorusuna her koşulda kesin ve doğru bir cevap verir
🤖 Bloom filtresi olasılıksaldır; "muhtemelen var" cevabı yanılabilir.
## B
False negative üretir; gerçekte var olan veriyi "yok" diyerek kaçırabilir
🤖 Standart Bloom filtresi false negative ASLA vermez, yalnız false positive verir.
## C
Kompakt olabilmek için tüm üyeleri sıkıştırılmış biçimde bellekte saklamak zorundadır
🤖 Aksine; amacı üyeleri hiç saklamadan kompakt bir üyelik testi yapmaktır.
## D
False positive mümkündür ama false negative asla; ayrıca standart Bloom'dan silinemez
🤖 Doğru: "kesin yok" veya "olabilir" der; silme için Counting/Cuckoo gerekir.
## Doğru
D
## Açıklama
**Bloom filtresi** olasılıksal bir üyelik yapısıdır: "bu eleman **kesinlikle kümede yok**" veya "**muhtemelen var**" der. Yani **false positive** mümkündür — var olmayana "olabilir" der, nadiren gereksiz bir DB sorgusu geçer — ama **false negative asla** olmaz; var olanı asla "yok" demez (B yanlış). Penetration savunmasında güvenli kılan budur: "yok" dediğinde kesindir, DB'ye hiç gitmezsin. Ödünleşmeler: (1) kompakttır ama olasılıksaldır ve üyeleri saklamaz (C yanlış), yanılmazlık iddiası da yoktur (A yanlış); (2) standart Bloom'dan **silme yapılamaz** — silme gerekiyorsa **Counting Bloom** veya **Cuckoo filtresi** kullanılır. Redis Stack / Redis 8 bunları modül olarak sunar.
