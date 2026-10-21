---
slug: redis-atomiklik-lua-multi
baslik: "Redis Atomiklik: MULTI/EXEC/WATCH, Lua ve Pipeline (Rollback YOK)"
kademe: İleri
sira: 164
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

İki kişi aynı anda aynı bakiyeden para çekmeye kalkarsa ne olur? Programlarda bu tür "aynı anda aynı veriye dokunma" durumuna **yarış koşulu (race condition)** denir. **Atomiklik** ise bir işin **bölünmeden**, ortasına başkası giremeden yapılmasıdır. Redis'te atomikliğin dört ayrı aracı vardır. Mülakatta en çok karıştırılan konu da hangisinin neyi garanti ettiğidir.

### Temel kavramlar

- **Tek iş parçacıklı yürütme**: Redis komutları **tek bir olay döngüsünde** sırayla işler. Bir komut çalışırken araya başka istemcinin komutu **giremez**. Bu yüzden `INCR`, `SETNX` ve `LPUSH` gibi **tek komutlar** hiçbir sarmalayıcıya gerek olmadan atomiktir. Redis komut başına **kilit almaz**; atomiklik de Lua'ya özgü değildir.
- **MULTI/EXEC**: Komutlar `MULTI` ile kuyruğa alınır ve `EXEC` ile **kesintisiz** yürütülür. Ama burada **geri alma (rollback) YOKTUR**. Kuyruğa doğru alınmış bir komut çalışma anında hata verse bile — örneğin bir metne `INCR` uygulamak — yalnız o komut hata döner. Öncekiler ve sonrakiler **yine uygulanır**; yürütme durmaz, süreç de çökmez. Yani Redis'in "transaction" dediği şey **ACID değildir**. (Yalnızca kuyruğa alma anındaki bir sözdizimi hatası tüm bloğu iptal ettirir.)
- **WATCH (iyimser kilit / CAS)**: `WATCH anahtar` kimseyi bekletmez; yalnız izler. İzlenen anahtar `WATCH` ile `EXEC` **arasında** başka bir istemci tarafından değiştirilmişse `EXEC` çalışmaz ve `nil` döner. İstemci de işlemi baştan dener. Bu bir **kötümser kilit değildir**: anahtarı silmez, ona TTL de eklemez.
- **Lua betiği (`EVAL`)**: Betik baştan sona bölünmeden çalışır; ortasına başka komut giremez. Asıl farkı şudur: betik **önceki komutun sonucunu görüp ona göre dallanabilir**. `MULTI` kuyruğundaki komutlar bunu yapamaz. Bu yüzden koşullu **oku-değiştir-yaz (read-modify-write)** işinin en sağlam yolu Lua'dır. Lua da **geri alma sağlamaz**.
- **Boru hattı (pipeline)**: Birden çok komutu tek seferde gönderip yanıtları toplu almaktır. Amacı ağdaki **gidiş-dönüş (round-trip)** sayısını azaltmaktır. **Atomik değildir**: boru hattındaki komutların arasında başka istemcilerin komutları çalışabilir. Her Redis kurulumunda kullanılır; Cluster'a özgü değildir.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Atomiklik mekanizmaları: MULTI, WATCH, Lua, pipeline">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Atomiklik: neyin garantisi ne?</text>
  <rect x="24" y="44" width="290" height="76" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="169" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Atomik yürütür</text>
  <text x="169" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Tek komut · MULTI/EXEC · Lua (EVAL)</text>
  <text x="169" y="108" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">araya başka komut giremez</text>
  <rect x="326" y="44" width="290" height="76" rx="10" fill="#FBEDE9" stroke="#B4472B" stroke-width="1.5"/>
  <text x="471" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#B4472B" font-family="system-ui">Atomik DEĞİL</text>
  <text x="471" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Pipeline</text>
  <text x="471" y="108" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yalnız round-trip azaltır</text>
  <rect x="24" y="130" width="290" height="72" rx="10" fill="#FDF3E3" stroke="#B07A1E" stroke-width="1.5"/>
  <text x="169" y="154" text-anchor="middle" font-size="13" font-weight="700" fill="#B07A1E" font-family="system-ui">ROLLBACK YOK</text>
  <text x="169" y="176" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">MULTI/EXEC hata durumunda</text>
  <text x="169" y="192" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">geri almaz → ACID değil</text>
  <rect x="326" y="130" width="290" height="72" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="471" y="154" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">WATCH = optimistic</text>
  <text x="471" y="176" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">izlenen anahtar değişirse</text>
  <text x="471" y="192" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">EXEC iptal (nil) — CAS</text>
</svg>
```

### Nasıl çalışır?

Pratik seçim şöyle yapılır. Basit bir sayaç ya da bayrak için **tek komut** yeter. Çakışan güncellemeleri kilitsiz yönetmek için **WATCH + MULTI** (CAS) uygundur. Okuduğun değere göre karar verip yazacaksan **Lua** en temiz yoldur. **Boru hattını** ise atomiklik aracı değil, gecikme iyileştirmesi olarak düşün. Ayrı ayrı `GET` ve `SET` göndermek ise tam da yarış koşuluna açık olan yoldur.

"Geri alma yok" ilkesi somut olarak şudur. `MULTI` ile kuyruğa alınan komutlar `EXEC` anında sırayla yürür. Ortadaki bir komut **çalışma anında** hata verse bile — örneğin metne `INCR` uygulamak — yalnız o komut hata döner. Öncekiler ve sonrakiler **yine uygulanır**:

```svg
<svg viewBox="0 0 640 188" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="MULTI EXEC rollback yok: hatalı komut kalanları durdurmaz">
  <defs>
    <marker id="ratomik-exec" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">MULTI/EXEC: hata olsa da geri alma YOK</text>
  <rect x="18" y="40" width="188" height="132" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">kuyruk (MULTI)</text>
  <text x="112" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">SET a 1</text>
  <text x="112" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">INCR a  (a string!)</text>
  <text x="112" y="136" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">SET c 3</text>
  <text x="112" y="160" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">EXEC ile yürütülür</text>
  <line x1="208" y1="106" x2="248" y2="106" stroke="#5D7789" stroke-width="2" marker-end="url(#ratomik-exec)"/>
  <rect x="252" y="46" width="372" height="36" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="438" y="69" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">SET a 1 → OK (uygulandı)</text>
  <rect x="252" y="88" width="372" height="36" rx="8" fill="#FBEDE9" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="438" y="111" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">INCR a → HATA (yalnız bu komut)</text>
  <rect x="252" y="130" width="372" height="36" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="438" y="153" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">SET c 3 → OK (YİNE uygulandı)</text>
</svg>
```

`WATCH` ise kilit almadan çakışmayı yakalar; buna iyimser kilit ya da CAS denir. İzlenen anahtar `WATCH` ile `EXEC` arasında başka bir istemci tarafından değişmişse `EXEC` çalışmaz, `nil` döner ve işlem baştan denenir. Kimse bekletilmez. Sırayı bekleten kötümser kilitten temel farkı da budur:

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="WATCH iyimser kilit CAS akışı ve yeniden deneme">
  <defs>
    <marker id="ratomik-cas" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">WATCH: iyimser kilit (CAS)</text>
  <rect x="14" y="70" width="92" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="60" y="90" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">WATCH k</text>
  <text x="60" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">izle</text>
  <rect x="126" y="70" width="92" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="172" y="97" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">GET k</text>
  <rect x="238" y="70" width="100" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="288" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">MULTI</text>
  <text x="288" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kuyruğa al</text>
  <rect x="358" y="70" width="92" height="44" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="404" y="97" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">EXEC</text>
  <line x1="106" y1="92" x2="124" y2="92" stroke="#5D7789" stroke-width="2" marker-end="url(#ratomik-cas)"/>
  <line x1="218" y1="92" x2="236" y2="92" stroke="#5D7789" stroke-width="2" marker-end="url(#ratomik-cas)"/>
  <line x1="338" y1="92" x2="356" y2="92" stroke="#5D7789" stroke-width="2" marker-end="url(#ratomik-cas)"/>
  <rect x="474" y="42" width="152" height="42" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="550" y="60" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">k değişmedi</text>
  <text x="550" y="76" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">→ başarı</text>
  <rect x="474" y="102" width="152" height="52" rx="8" fill="#FBEDE9" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="550" y="120" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">k başka istemcice değişti</text>
  <text x="550" y="136" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">→ EXEC nil</text>
  <text x="550" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">→ yeniden dene</text>
  <line x1="450" y1="86" x2="472" y2="70" stroke="#5D7789" stroke-width="2" marker-end="url(#ratomik-cas)"/>
  <line x1="450" y1="98" x2="472" y2="120" stroke="#5D7789" stroke-width="2" marker-end="url(#ratomik-cas)"/>
</svg>
```

### Mülakatta ne sorulur?

(1) "`INCR` neden atomik?" — Tek iş parçacıklı yürütme sayesinde. `MULTI` ile sarmak gerekmez; Lua'ya da ihtiyaç yoktur.

(2) "`MULTI/EXEC` bloğunda ortadaki komut patlarsa ne olur?" — Kalanlar yine uygulanır. Geri alma yoktur ve blok iptal olmaz.

(3) "`WATCH` ne tür bir kilittir?" — İyimser kilittir (CAS). Kimseyi bekletmez; yalnız `EXEC`'i bir koşula bağlar.

(4) "Oku-değiştir-yaz işini yarışsız nasıl yaparım?" — Lua ile tek bölünmez adımda. Ayrı `GET` + `SET` yarışa açıktır, boru hattı atomiklik vermez, Pub/Sub ise bambaşka bir konudur.

(5) "Neden `MULTI` yerine Lua?" — Hız ya da atomiklik farkı için değil; **koşullu dallanma** için.

### Tek cümlede

Redis tek iş parçacıklı olduğu için tek komutlar ve `EXEC`/Lua blokları kesintisiz çalışır. Ama hiçbirinde geri alma yoktur. `WATCH` iyimser bir CAS mekanizmasıdır; boru hattı ise yalnız bir ağ iyileştirmesidir.

# ratomik-1 | İleri
## Soru
Redis'te `INCR` gibi **tek bir komutun** atomikliği neye dayanır?
## A
Komutlar tek thread'li bir olay döngüsünde sırayla işlenir; araya başka komut giremez
🤖 Doğru: tek-thread'li yürütme, tek komutları doğal olarak atomik kılar.
## B
Her komut için sunucu tarafında özel bir kilit (lock) alınıp iş bitince bırakılır
🤖 Redis komut başına kilit almaz; atomiklik tek-thread'li yürütmeden gelir.
## C
Atomik sayılabilmesi için komutun `MULTI/EXEC` bloğuyla sarılması zorunludur
🤖 Tek komut zaten atomiktir; `MULTI` yalnız birden çok komutu birlikte yürütmek içindir.
## D
Yalnızca bir Lua script içine konulan komutlar atomik olarak çalışabilmektedir
🤖 Tek komutlar Lua olmadan da atomiktir; Lua çok-adımlı mantık içindir.
## Doğru
A
## Açıklama
Redis komut yürütmeyi **tek thread**'li bir olay döngüsünde yapar: her komut baştan sona **kesintisiz** işlenir, ortasına başka istemcinin komutu giremez. Bu yüzden `INCR`, `SETNX`, `LPUSH` gibi **tek komutlar** doğal olarak atomiktir (A). Redis komut başına kilit **almaz** (B yanlış); tek komutu atomik yapmak için `MULTI/EXEC` **gerekmez** (C yanlış); atomiklik Lua'ya özgü değildir, Lua yalnız çok-adımlı mantığı tek atomik bloğa alır (D yanlış).

# ratomik-2 | İleri
## Soru
Bir `MULTI/EXEC` bloğunda bir komut **çalışma-zamanında** hata verirse (ör. bir string'e `INCR` uygulamak) ne olur?
## A
Tüm blok geri alınır (rollback); hiçbir komutun etkisi veride kalmaz
🤖 Redis "transaction"ında rollback yoktur; kalan komutlar yine de uygulanır.
## B
`EXEC` hiç çalışmaz; blok tümüyle iptal edilir ve hiçbir komut yürütülmez
🤖 Blok iptali yalnız KUYRUK-anı (queue-time) hatalarında olur; çalışma-zamanı hatası kalanları durdurmaz.
## C
Diğer komutlar yine de çalışır; Redis "transaction"ında rollback bulunmaz
🤖 Doğru: çalışma-zamanı hatası tek komutu etkiler, blok geri alınmaz.
## D
Yürütme o noktada durur; hatadan sonraki komutlar kuyrukta kalıp hiç çalışmaz
🤖 Yürütme durmaz; hatalı komuttan sonrakiler de sırasıyla uygulanmaya devam eder.
## Doğru
C
## Açıklama
Redis'in `MULTI/EXEC`'i komutları **izole ve kesintisiz yürütür** ama **ACID değildir**: **rollback yoktur**. Kuyruğa alınırken sözdizimi hatası olan komut (queue-time error) tüm bloğu iptal ettirebilir; ancak komut **kuyruğa doğru alınıp çalışma-zamanında** hata verirse (ör. string'e `INCR`), o komut hata döndürür, **diğer komutlar yine de uygulanır** (C). Bu, ilişkisel veritabanı transaction'ından temel farktır. A'nın anlattığı rollback Redis'te yoktur; B'nin anlattığı blok iptali yalnız kuyruk-anı hatalarına aittir; D'nin iddia ettiği gibi yürütme de durmaz — hatalı komuttan sonraki komutlar sırasıyla uygulanmaya devam eder.

# ratomik-3 | İleri
## Soru
`WATCH` komutunun `MULTI/EXEC` ile birlikteki rolü nedir?
## A
İzlenen anahtar üzerinde kötümser kilit alıp diğer istemcileri sıraya sokup bekletir
🤖 `WATCH` kilit almaz; iyimser (optimistic) bir kontrol yapar ve kimseyi bloklamaz.
## B
İyimser kilit: anahtar `EXEC`'ten önce başka istemcice değişirse `EXEC` iptal olur
🤖 Doğru: `WATCH` bir karşılaştır-ve-yaz (CAS) mekanizması sağlar.
## C
İzlenen anahtarı `EXEC` anında otomatik olarak silip yerine yeni değeri yazar
🤖 `WATCH` anahtarı silmez; yalnız değişip değişmediğini gözler.
## D
İzlenen anahtara otomatik bir TTL ekler ve süre dolunca bloğu kendiliğinden tetikler
🤖 `WATCH` TTL eklemez; işlevi anahtarın değişimini izleyip `EXEC`'i koşullamaktır.
## Doğru
B
## Açıklama
`WATCH anahtar`, bir **iyimser eşzamanlılık kontrolü** (optimistic concurrency / CAS) kurar: istemci anahtarı okur, `MULTI` ile komutları kuyruğa alır, `EXEC` çağırır. Eğer izlenen anahtar `WATCH` ile `EXEC` **arasında** başka bir istemci tarafından **değiştirildiyse**, `EXEC` çalışmaz ve `nil` döner; istemci işlemi yeniden dener (B). Bu bir **kötümser kilit değildir** — kimse bloklanmaz (A yanlış). `WATCH` anahtarı silmez (C) veya TTL eklemez (D); yalnız değişimi gözleyip `EXEC`'i koşula bağlar.

# ratomik-4 | İleri
## Soru
Bir **oku-değiştir-yaz** (read-modify-write) mantığını Redis'te tek atomik adımda, gidiş-dönüşler arasında yarış (race) olmadan yapmanın en sağlam yolu nedir?
## A
Önce `GET`, ardından ayrı bir `SET` komutu göndererek değeri hesaplayıp yazmak
🤖 İki ayrı komut arasında başka bir istemci araya girebilir; yarış koşulu doğar.
## B
Komutları tek bir pipeline içinde toplu gönderip yanıtları birlikte almak
🤖 Pipeline atomik değildir; yalnız round-trip azaltır, araya başka komut girebilir.
## C
İşlemi bir Pub/Sub kanalı üzerinden yayınlayıp abonelerin uygulamasını beklemek
🤖 Pub/Sub bir mesajlaşma modelidir; atomik oku-değiştir-yaz garantisi vermez.
## D
Lua script (`EVAL`): okuma, karar ve yazma tek atomik adımda yapılır
🤖 Doğru: Lua tüm mantığı tek atomik yürütmeye alır ve yarışı ortadan kaldırır.
## Doğru
D
## Açıklama
`EVAL` ile çalıştırılan **Lua script**, Redis'in tek-thread'li döngüsünde **kesintisiz** işlenir; okuma, karar (dallanma) ve yazma **tek atomik adımda** olur, ortasına başka komut giremez (D). Bu, oku-değiştir-yaz'ın klasik yarış koşulunu (birinin okuduğu değeri başka istemcinin değiştirmesi) ortadan kaldırır. Ayrı `GET`+`SET` (A) tam da bu yarışa açıktır; **pipeline** (B) yalnız gecikmeyi azaltır, atomiklik vermez; **Pub/Sub** (C) bir mesajlaşma modelidir, RMW garantisiyle ilgisizdir. (Alternatif olarak `WATCH`+`MULTI` de CAS ile çözer, ama koşullu mantıkta Lua daha doğrudandır.)

# ratomik-5 | İleri
## Soru
Redis **pipeline** (komutları toplu gönderme) hakkında hangisi **doğrudur**?
## A
Gidiş-dönüş sayısını azaltır ama atomik değildir; araya başka komutlar girebilir
🤖 Doğru: pipeline bir ağ optimizasyonudur, atomiklik veya izolasyon sağlamaz.
## B
Komutları atomik hâle getirir; arada başka istemcinin komutu kesinlikle çalışamaz
🤖 Pipeline atomiklik vermez; izolasyon için `MULTI/EXEC` veya Lua gerekir.
## C
`MULTI/EXEC` ile tamamen aynı şeydir; ikisi teknik olarak birebir eşdeğerdir
🤖 Farklıdır: `MULTI/EXEC` atomik yürütür, pipeline yalnız toplu gönderim yapar.
## D
Yalnızca Redis Cluster modunda çalışır; tek node kurulumlarda desteklenmez
🤖 Pipeline her Redis kurulumunda çalışır; Cluster'a özgü bir özellik değildir.
## Doğru
A
## Açıklama
**Pipeline**, istemcinin birden çok komutu **tek seferde gönderip** yanıtları toplu almasıdır; amacı ağ **gidiş-dönüş** sayısını azaltıp verimi artırmaktır. Ancak **atomiklik sağlamaz**: pipeline'daki komutlar arasında başka istemcilerin komutları çalışabilir (A). Atomik/izole yürütme için `MULTI/EXEC` (kuyruğu kesintisiz yürütür) veya Lua gerekir; pipeline onlarla **aynı şey değildir** (B, C yanlış). Pipeline Cluster'a özgü de değildir, her kurulumda kullanılabilir (D yanlış).

# ratomik-6 | İleri
## Soru
`MULTI/EXEC` yerine **Lua script** tercih etmenin tipik nedeni nedir?
## A
Lua her zaman `MULTI/EXEC`'ten daha yavaştır ama karşılığında daha güvenli sayılır
🤖 Neden hız değildir; ayrıca Lua tek gidiş-dönüşte çalışıp genelde daha verimli olabilir.
## B
`MULTI/EXEC` atomik değildir, oysa Lua script atomik olarak yürütülmektedir
🤖 İkisi de atomik yürütür; ayrım atomiklik değil, koşullu mantık yeteneğidir.
## C
Lua önceki komutun sonucuna göre dallanabilir; `MULTI` kuyruğu bunu yapamaz
🤖 Doğru: Lua koşullu oku-karar-yaz sağlar, `MULTI` kuyruğu bunu yapamaz.
## D
Lua, hata durumunda otomatik rollback sağlayıp yapılan değişiklikleri geri alabilir
🤖 Lua da rollback sağlamaz; Redis'te transaction rollback'i hiçbir mekanizmada yoktur.
## Doğru
C
## Açıklama
Hem `MULTI/EXEC` hem Lua atomik/kesintisiz yürütür; aralarındaki asıl fark **koşullu mantık**tır. `MULTI/EXEC`'te komutlar önceden **kuyruğa alınır** ve `EXEC` anında yürütülür; kuyruktaki bir komut **önceki komutun sonucunu göremez**, dolayısıyla "değer X ise şunu yap, değilse bunu" gibi **dallanma** yapılamaz. **Lua** ise gerçek bir betik dilidir: bir değeri okur, ona göre **dallanır** ve yazar — hepsi tek atomik adımda (C). Bu yüzden koşullu oku-değiştir-yaz senaryolarında Lua tercih edilir. Neden hız (A) veya atomiklik farkı (B) değildir; Lua da **rollback sağlamaz** (D yanlış).
