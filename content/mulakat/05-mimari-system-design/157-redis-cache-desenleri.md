---
slug: redis-cache-desenleri
baslik: "Redis Önbellek Desenleri: Cache-Aside, Read/Write-Through ve Invalidate-over-Update"
kademe: İleri
sira: 157
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

**Önbellek (cache)** şu basit fikirdir: pahalı bir işin sonucunu bir kenara yaz, aynı sonuç yine istenince tekrar hesaplama, kenardan ver.

Günlük hayattan: telefon rehberi bir önbellektir. Bir numarayı her seferinde birine sormak yerine bir kez yazıp saklarsın. Hemen iki soru doğar: (1) Rehberde yoksa ne yaparsın? (2) Numara değişince rehberdeki eski kayda ne olur? Önbellek konusunun tamamı bu iki sorunun cevabıdır.

Yazılımda "pahalı iş" genelde bir **veritabanı (DB) sorgusudur**; "kenar" ise Redis. Amaç, aynı sorgunun DB'ye tekrar tekrar gitmemesidir.

### Temel kavramlar

- **Hit (isabet)**: istenen veri önbellekte bulundu. **Miss (ıskalama)**: bulunamadı, asıl kaynağa gitmek gerekir.
- **Bayat (stale) veri**: DB değişti ama önbellekte hâlâ eski değer duruyor.
- **Geçersiz kılma (invalidation)**: önbellekteki girdiyi **silmek** — "bu artık güvenilmez".
- **TTL (time to live)**: anahtara verilen ömür; süre dolunca anahtar kendiliğinden silinir.
- **Tutarlılık (consistency)**: önbelleğin DB ile ne kadar uyumlu olduğu. Önbellek kullanmak, bir miktar tutarlılığı hıza takas etmektir.
- **Desen (pattern)**: okuma ve yazmanın hangi sırayla, kimin sorumluluğunda yapıldığını tarif eden hazır reçete.

### Nasıl çalışır?

**Okuma desenleri**

- **Yanda önbellek (cache-aside / lazy loading)** — en yaygın olanıdır. Uygulama önce Redis'e bakar. İsabet varsa değeri döner. Iska varsa veritabanından okur, sonucu önbelleğe yazar (genelde bir TTL ile) ve çağırana döner. Yükleme mantığı **uygulamadadır**; önbellek burada "aptal" bir depodur. Artısı sadelik ve yalnız istenen verinin yüklenmesidir. Eksisi ilk isteğin daima ıska olmasıdır; buna soğuk başlangıç denir.
- **Baştan geçen okuma (read-through)** — uygulama yalnız önbellekle konuşur. Iskayı **önbellek katmanı ya da kütüphanesi** fark eder ve veritabanını kendisi okur. Akış aynıdır; değişen tek şey **sorumluluğun yeridir**. Mantık tek yerde toplanır, ama bir önbellek soyutlaması (loader) gerekir.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cache-aside okuma akışı">
  <defs>
    <marker id="rcache-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Cache-aside: önce cache, miss'te DB</text>
  <rect x="24" y="86" width="120" height="54" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="84" y="118" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Uygulama</text>
  <rect x="260" y="86" width="120" height="54" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="112" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Redis</text>
  <text x="320" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">cache</text>
  <rect x="496" y="86" width="120" height="54" rx="10" fill="#FBEDD9" stroke="#B26B0E" stroke-width="1.5"/>
  <text x="556" y="118" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B0E" font-family="system-ui">Veritabanı</text>
  <line x1="144" y1="102" x2="256" y2="102" stroke="#186399" stroke-width="1.5" marker-end="url(#rcache-ok)"/>
  <text x="200" y="94" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">1. GET</text>
  <line x1="256" y1="124" x2="146" y2="124" stroke="#25804C" stroke-width="1.5" marker-end="url(#rcache-ok)"/>
  <text x="200" y="140" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">2a. HIT → döner</text>
  <line x1="380" y1="102" x2="492" y2="102" stroke="#B26B0E" stroke-width="1.5" stroke-dasharray="4 3" marker-end="url(#rcache-ok)"/>
  <text x="436" y="94" text-anchor="middle" font-size="10" fill="#B26B0E" font-family="system-ui">2b. MISS → DB</text>
  <line x1="492" y1="124" x2="382" y2="124" stroke="#B26B0E" stroke-width="1.5" stroke-dasharray="4 3" marker-end="url(#rcache-ok)"/>
  <text x="436" y="140" text-anchor="middle" font-size="10" fill="#B26B0E" font-family="system-ui">3. oku + cache'e koy</text>
  <rect x="120" y="176" width="400" height="30" rx="8" fill="#FBE9E7" stroke="#B23B2E" stroke-width="1.2"/>
  <text x="320" y="196" text-anchor="middle" font-size="11" fill="#B23B2E" font-family="system-ui">Yazmada: önce DB, sonra cache'i SİL (invalidate) + TTL</text>
</svg>
```

**Yazma desenleri**

- **Baştan geçen yazma (write-through)** — yazma hem önbelleğe hem veritabanına **eşzamanlı** gider. Önbellek veritabanıyla tutarlı kalır. Bedeli, her yazmanın iki yere gitmesi ve gecikmenin artmasıdır.
- **Geriden yazma (write-behind / write-back)** — önce önbelleğe yazılır; veritabanına **eşzamansız ve toplu** aktarılır. Çok hızlıdır. Riski şudur: Redis çökerse veritabanına henüz aktarılmamış yazmalar kaybolur. Bu yüzden yalnız metrik toplama gibi kayba toleranslı yerlerde anlamlıdır.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Write-through senkron iki yere yazar, write-behind önce cache sonra asenkron DB">
  <defs>
    <marker id="rcache-wt-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Write-through vs Write-behind</text>
  <text x="160" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Write-through (senkron)</text>
  <rect x="96" y="64" width="128" height="30" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="160" y="84" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Uygulama yazar</text>
  <line x1="120" y1="94" x2="100" y2="122" stroke="#186399" stroke-width="1.5" marker-end="url(#rcache-wt-ok)"/>
  <line x1="200" y1="94" x2="220" y2="122" stroke="#186399" stroke-width="1.5" marker-end="url(#rcache-wt-ok)"/>
  <rect x="42" y="126" width="96" height="30" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="90" y="146" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Redis</text>
  <rect x="182" y="126" width="96" height="30" rx="7" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <text x="230" y="146" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">DB</text>
  <text x="160" y="180" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">ikisi de eşzamanlı → tutarlı</text>
  <text x="160" y="198" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bedel: her yazma iki yere, yavaş</text>
  <line x1="320" y1="56" x2="320" y2="206" stroke="#5D7789" stroke-width="1" stroke-dasharray="3 3"/>
  <text x="480" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Write-behind (asenkron)</text>
  <rect x="416" y="64" width="128" height="30" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="480" y="84" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Uygulama yazar</text>
  <line x1="480" y1="94" x2="480" y2="122" stroke="#186399" stroke-width="1.5" marker-end="url(#rcache-wt-ok)"/>
  <rect x="432" y="126" width="96" height="30" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="480" y="146" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Redis (hemen)</text>
  <line x1="528" y1="141" x2="566" y2="141" stroke="#B26B00" stroke-width="1.5" stroke-dasharray="4 3" marker-end="url(#rcache-wt-ok)"/>
  <rect x="566" y="126" width="60" height="30" rx="7" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <text x="596" y="146" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">DB</text>
  <text x="480" y="180" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">DB'ye toplu/gecikmeli → hızlı</text>
  <text x="480" y="198" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">risk: çökmede yazılmamış veri kaybı</text>
</svg>
```

**Kıdemli ilke: güncelleme, geçersiz kıl (invalidate over update).** Yazmadan sonra önbelleğe yeni değeri yazmak cazip görünür; ama bir yarış (race) doğurur. İki yazar, hesapladıkları değerleri farklı sırada yazarsa önbellekte **bayat** bir değer kalır ve orada takılı durur. Silmek bu yarışı büyük ölçüde kapatır: önbellek boşalır ve ilk okuma veritabanından **taze** değeri yükler. Yan fayda: nadiren okunan veri boşuna hesaplanmaz.

Sıra da önemlidir: **önce DB'ye yaz, sonra önbelleği sil.** Yine de dar bir pencere kalır. Şöyle işler: bir okuyucu ıska alır ve veritabanından eski değeri okur. Tam o sırada yazar veritabanını günceller ve önbelleği siler. Ardından gecikmiş okuyucu **eski** değeri önbelleğe yazar. Bu yüzden geçersiz kılmaya **her zaman bir TTL** eşlik eder; TTL, tutarsızlığın ömrüne üst sınır koyar. (Facebook'un "Scaling Memcache" makalesindeki delete + TTL yaklaşımı.) Hiçbir desen bu pencereyi sıfırlamaz; kabul edip sınırlarsın.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İki yazar cacheyi ters sırada güncellerse bayat değer kalır, silmek bunu önler">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Neden güncelleme değil, geçersiz kılma?</text>
  <text x="170" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Güncelle (SET): yarış</text>
  <rect x="40" y="64" width="120" height="28" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="100" y="83" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">W1 eski değeri hesaplar</text>
  <rect x="180" y="64" width="120" height="28" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="240" y="83" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">W2 yeni değeri hesaplar</text>
  <rect x="40" y="100" width="260" height="28" rx="6" fill="#F6E4E4" stroke="#8A2E2E" stroke-width="1.3"/>
  <text x="170" y="119" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">W2 yazar → sonra W1 yazar (ters sıra)</text>
  <rect x="40" y="136" width="260" height="30" rx="6" fill="#F6E4E4" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="170" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">cache = eski (bayat) ⚠</text>
  <line x1="320" y1="46" x2="320" y2="196" stroke="#5D7789" stroke-width="1" stroke-dasharray="3 3"/>
  <text x="470" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Sil (invalidate): güvenli</text>
  <rect x="360" y="64" width="260" height="28" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="490" y="83" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">DB'ye yaz → cache'i SİL</text>
  <rect x="360" y="100" width="260" height="28" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="490" y="119" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">cache boş → sonraki okuma miss</text>
  <rect x="360" y="136" width="260" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="490" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">DB'den TAZE değer yüklenir</text>
  <text x="320" y="192" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Yine de dar yarış kalır → TTL güvenlik ağı</text>
</svg>
```

**Ne zaman önbelleklememeli?** Önbelleğin faydası **okuma/yazma oranına** bağlıdır. Çok yazılan ama neredeyse hiç okunmayan bir alanı önbelleklemek net zarardır. Sebebi şu: her yazma önbelleği geçersiz kılar; bu ek yük ve tutarlılık riski getirir. Buna karşılık okuma olmadığı için hiç hız kazancı doğmaz. Doğrusu ya hiç önbelleklememek ya da çok kısa bir TTL vermektir. TTL'siz kalıcı önbellekleme ise bellek sızıntısı ve tahliye (eviction) baskısı üretir. Önce erişim desenini ölç, sonra önbelleğe al.

Çoğu web yükü için pratik varsayılan şudur: **cache-aside + önce DB sonra sil + jitter'lı TTL.**

### Mülakatta ne sorulur?

- "Cache-aside nasıl çalışır?" → önce önbellek; miss'te DB'den oku, önbelleğe yaz, sonucu dön.
- "Cache-aside ile read-through farkı?" → miss'te DB'yi **kim** yükler: uygulama mı, önbellek katmanı mı.
- "Write-through vs write-behind?" → senkron ve tutarlı, karşısında asenkron ve hızlı ama çökmede kayıp riskli.
- "Neden güncelleme değil silme?" → ters sıralı yazmaların bıraktığı bayatlık; silme sonraki okumaya taze değer yükletir.
- "Önce DB sonra sil yaparsam yarış biter mi?" → hayır, dar bir pencere kalır; TTL güvenlik ağıdır.
- "Her şeyi önbellekleyelim mi?" → hayır; yazma ağırlıklı, okuma nadir veride önbellek zarardır.

### Tek cümlede

Önbellek deseni seçmek, üç soruya baştan cevap vermektir: veriyi kim yükler, kim yazar ve bayatlık en fazla ne kadar sürer? Güvenli varsayılan şudur: yanda önbellek deseni kullan, önce veritabanına yaz sonra önbelleği sil ve her anahtara dağıtılmış (jitter'lı) bir TTL ver.

# rcache-1 | Temel
## Soru
En yaygın önbellek deseni "cache-aside" (lazy loading) okuma yolunda nasıl çalışır?
## A
Uygulama yalnız önbelleğe yazar ve okur; DB'nin varlığından hiç haberi olmaz
🤖 Bu write-behind'a yakın bir davranış; cache-aside miss'te DB'yi kendisi okur.
## B
Önbellek kütüphanesi miss'i fark edip DB'yi otomatik yükler, uygulama DB'yi görmez
🤖 Bu read-through tanımıdır; cache-aside'da yükleme mantığı uygulamadadır.
## C
Her okuma önce DB'ye gider, önbellek yalnız DB çöktüğünde devreye giren bir yedektir
🤖 O zaman önbellek hiç hızlandırmaz; cache-aside daima önce önbelleğe bakar.
## D
Uygulama önce önbelleğe bakar; miss olursa DB'den okur, önbelleğe yazar ve döner
🤖 Doğru: klasik cache-aside akışı; önbelleği uygulamanın kendisi doldurur.
## Doğru
D
## Açıklama
**Cache-aside** deseninde okuma mantığı uygulamadadır: önce Redis'e bak → **hit** ise döndür → **miss** ise DB'den oku, önbelleğe yaz (genelde TTL ile), sonucu döndür. Önbelleği uygulama "yanında" (aside) yönetir. Bu, read-through'dan (B — yüklemeyi önbellek katmanı yapar) ve write-behind'dan (A) farklıdır; her okumayı DB'ye göndermek (C) ise önbelleği anlamsızlaştırır. Cache-aside'ın avantajı sadeliği ve yalnız istenen veriyi yüklemesidir; dezavantajı ilk isteğin daima miss olmasıdır (soğuk başlangıç).

# rcache-2 | Orta
## Soru
Read-through ile cache-aside arasındaki temel fark nedir?
## A
İkisi tamamen aynı desendir; yalnız farklı kitaplarda farklı isimlerle anılırlar
🤖 Fark, miss anında DB'yi kimin yüklediğidir; aynı desen değiller.
## B
Read-through veriyi kalıcı olarak diske yazar, cache-aside ise yalnız belleğe
🤖 İkisi de Redis'i, yani belleği kullanır; fark sorumluluğun yerindedir.
## C
Read-through'da miss'te DB'yi önbellek katmanı yükler; cache-aside'da uygulama yükler
🤖 Doğru: sorumluluk biri önbellek sağlayıcısında, diğeri uygulama kodunda.
## D
Cache-aside asenkron çalışır, read-through ise her isteği senkron olarak bekletir
🤖 İkisi de senkron okuyabilir; ayrım eşzamanlılık değil, yükleme sorumluluğudur.
## Doğru
C
## Açıklama
İki desen de "miss'te DB'den yükle, önbelleğe koy" yapar; fark **bu mantığın nerede yaşadığıdır**. **Cache-aside**'da uygulama kodu miss'i görür ve DB'den yükler; önbellek pasif bir depodur. **Read-through**'da uygulama yalnız önbellekle konuşur, miss'i **önbellek sağlayıcısı/kütüphanesi** algılar ve DB'yi kendi loader'ıyla yükler. Bu, yükleme mantığını merkezileştirir ama bir önbellek soyutlaması gerektirir. Disk/bellek ayrımı (B) veya senkron/asenkron ayrımı (D) değildir; aynı desenin iki adı da değildir (A).

# rcache-3 | Orta
## Soru
Write-through ile write-behind (write-back) arasındaki fark nedir?
## A
Write-through DB'ye hiç dokunmaz, veriyi yalnız önbellekte tutar ve orada bırakır
🤖 Aksine; write-through DB'ye de eşzamanlı yazar, kalıcılığı korur.
## B
Write-through önbellek ve DB'yi senkron günceller; write-behind DB'ye asenkron yazar
🤖 Doğru: biri tutarlılık için senkron, diğeri hız için gecikmeli çalışır.
## C
Write-behind, write-through'dan daha tutarlıdır çünkü yazmaları toplu olarak doğrular
🤖 Tam tersi; DB yazması geciktiği için çökmede veri kaybı riski taşır.
## D
Write-behind her yazmayı DB'ye anında iletir, write-through ise yalnız önbelleğe yazar
🤖 İki tanım yer değiştirmiş; senkron olan write-through, gecikmeli olan write-behind'dir.
## Doğru
B
## Açıklama
**Write-through**: her yazma **hem önbelleğe hem DB'ye** eşzamanlı (senkron) gider — önbellek DB ile tutarlı kalır ama her yazma iki yere gittiği için gecikme artar. **Write-behind (write-back)**: yazma önce önbelleğe, DB'ye ise **asenkron/toplu (batched)** gider — çok hızlıdır ama Redis çökerse DB'ye aktarılmamış veriler kaybolur; yani daha az dayanıklıdır (C yanlış). Tanımların yer değiştirmesi de yaygın bir karışıklıktır (D yanlış). Write-behind yüksek yazma hacimli ve kayıp toleransı olan (metrik toplama gibi) senaryolarda, write-through ise tutarlılığın kritik olduğu yerlerde tercih edilir.

# rcache-4 | İleri
## Soru
Yazma sonrası önbelleği "güncellemek" (yeni değeri yazmak) yerine "geçersiz kılmak" (silmek) neden genelde tercih edilir?
## A
İki eşzamanlı yazarın değerleri ters sırada yazıp bayat değer bırakma riskini azaltır
🤖 Doğru: silme bu yarışı kapatır; sonraki okuma taze değeri yükler.
## B
Silme işlemi önbellekte değer yazmaya göre çok daha fazla bellek tüketir
🤖 Aksine; silme bellek boşaltır ve değer yazmaktan daha ucuz bir işlemdir.
## C
Güncelleme önbelleği her zaman DB'den daha taze yapar, silme ise veriyi bir süre kaybeder
🤖 Güncelleme ters sırayla yazılınca kalıcı bayatlık bırakır; silme kaybetmez, yeniden yükletir.
## D
Geçersiz kılma, önbellek girdisiyle birlikte DB'deki asıl kaydı da silip yeri boşaltır
🤖 Hayır; geçersiz kılma yalnız önbellek girdisini siler, DB'ye hiç dokunmaz.
## Doğru
A
## Açıklama
**Invalidate-over-update** ilkesi: yazınca önbelleğe yeni değeri yazmak yerine **sil**. Nedeni bir yarıştır: iki yazar (W1 eski, W2 yeni değeri hesaplar) önbelleğe ters sırada yazarsa — W2 önce, W1 sonra — önbellekte **bayat** değer kalır ve orada durur. Silme bu riski büyük ölçüde kapatır: önbellek boşalır, ilk okuma DB'den **taze** değeri yükler. Ayrıca nadiren okunan veri boşuna hesaplanmaz. Silme belleği azaltır (B yanlış), güncelleme tazelik garantisi vermez (C yanlış) ve geçersiz kılma yalnız önbelleği etkiler, DB'yi değil (D yanlış).

# rcache-5 | İleri
## Soru
Cache-aside yazmada "önce DB'ye yaz, sonra önbelleği sil" yaklaşımını kullanıyorsun. Hangi yarış (race) buna rağmen HÂLÂ mümkündür?
## A
Hiçbir yarış kalmaz; bu sıralama önbellek ile DB arasında tam tutarlılık garanti eder
🤖 Dar da olsa bir pencere kalır; hiçbir geçersiz kılma deseni %100 tutarlı değildir.
## B
Yalnız DB tümüyle çöktüğünde tutarsızlık olur; DB ayaktayken pencere tamamen kapalıdır
🤖 DB çökmese de yalnız zamanlamadan doğan bir yarış penceresi mevcuttur.
## C
Önbellek asla bayat kalamaz, çünkü silme komutu her zaman en son çalışacak biçimde sıralanır
🤖 Silme sırası garanti edilse de araya giren gecikmiş bir okuyucu bayat değer yazabilir.
## D
Gecikmiş bir okuyucu, silmeden hemen sonra DB'den okuduğu eski değeri önbelleğe yazabilir
🤖 Doğru: dar ama gerçek bir pencere; TTL bunun ömrüne üst sınır koyar.
## Doğru
D
## Açıklama
"Önce DB, sonra önbelleği sil" iyi bir varsayılandır ama dar bir yarış kalır: Okuyucu R miss alır ve DB'den **eski** değeri okur; tam bu sırada yazar W DB'yi günceller ve önbelleği siler; sonra R gecikmiş biçimde **eski** değeri önbelleğe yazar. Sonuç bayat bir değerdir ve bir sonraki silmeye ya da **TTL** dolana kadar orada durur. Bu yüzden geçersiz kılmaya **her zaman TTL** eşlik eder; tutarsızlığın ömrüne üst sınır koyar (Facebook'un "Scaling Memcache" makalesindeki delete + TTL yaklaşımı). Hiçbir desen bu pencereyi sıfırlamaz (A, C yanlış) ve sorunun kaynağı DB çökmesi değildir (B).

# rcache-6 | İleri
## Soru
Bir veri alanı çok sık yazılıyor ama neredeyse hiç okunmuyor. Bu alan için en mantıklı önbellek yaklaşımı hangisidir?
## A
Her yazmada önbelleği senkron biçimde güncellemek (write-through) ki değer okunmaya hep hazır dursun
🤖 Nadir okunan veriyi her yazmada önbelleklemek boşa iştir; okunmadan tekrar yazılır.
## B
Write-behind deseniyle agresif biçimde önbellekleyip yazma yükünü DB'den Redis tarafına taşımak
🤖 Yine nadir okunan veriyi tutar; okuma faydası yokken karmaşıklık ve kayıp riski ekler.
## C
Önbelleklememek ya da çok kısa TTL vermek; nadir okunan veri önbellekte net zarardır
🤖 Doğru: önbelleğin faydası okuma/yazma oranına bağlıdır, burada oran aleyhinedir.
## D
TTL'siz kalıcı olarak önbelleklemek ki her ihtimale karşı değer elde hazır olsun
🤖 TTL'siz nadir okunan veri bellek sızıntısına ve tahliye baskısına yol açar.
## Doğru
C
## Açıklama
Önbelleğin faydası **okuma/yazma oranına** bağlıdır: önbellek, tekrar tekrar okunan veriyi hızlandırmak içindir. **Yazma ağırlıklı ama okuma nadir** bir alanda önbelleklemek net zarardır — her yazma önbelleği günceller ya da geçersiz kılar (ek yük ve tutarlılık maliyeti) ama o değer nadiren okunduğu için hız kazancı doğmaz. Doğrusu ya hiç önbelleklememek ya da çok kısa TTL vermektir. Write-through (A) ve write-behind (B) yazma yükünü artırır; TTL'siz kalıcı önbellek (D) bellek sızıntısı ve tahliye baskısı yaratır. Önce erişim desenini ölç, sonra önbelleğe al.
