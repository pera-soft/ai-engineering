---
slug: kafka-siralama-garantisi
baslik: "Kafka Sıralama Garantisi: Partition İçi Sıra, Key→Partition ve Paralellik Tavizi"
kademe: İleri
sira: 114
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir sipariş için önce "oluşturuldu", sonra "ödendi", sonra "iptal edildi" olayları geliyor. Bunlar yanlış sırada işlenirse iptal edilmiş bir sipariş ödenmiş görünebilir. Yani **sıra** çoğu iş kuralının sessiz ön şartıdır. Kafka sıra konusunda çok net ama çok da dar bir söz verir; bu sözü tam olarak bilmezseniz sistemde en zor bulunan hataları üretirsiniz.

### Temel kavramlar

- **Konu (topic):** aynı türden mesajların biriktiği isimli defter, örneğin `siparisler`.
- **Bölüm (partition):** o defterin bağımsız parçalarından biri. Her bölüm **yalnızca sonuna eklenen (append-only)** bir listedir; ortasına kayıt sokulmaz.
- **Okuma konumu (offset):** bir bölüm içinde her mesaja verilen, birer birer artan sıra numarası. "Nereye kadar okudum" bilgisi budur.
- **Üretici (producer):** mesajı yazan program. **Tüketici (consumer):** mesajı okuyan program.
- **Mesaj anahtarı (message key):** mesaja iliştirilen küçük bir etiket, örneğin `orderId`. Mesajın hangi bölüme düşeceğini bu belirler.
- **Bölüm seçici (partitioner):** anahtarı bir bölüm numarasına çeviren kural.
- **`max.in.flight.requests.per.connection` (yoldaki istek sayısı):** üreticinin, cevabını beklemeden aynı bağlantı üzerinde "havada" tutabildiği istek sayısı.
- **DLT (Dead Letter Topic / ölü mektup konusu):** işlenemeyen mesajların kenara alındığı ayrı topic.

### Nasıl çalışır?

Kafka'nın verdiği söz şudur: **sıra yalnızca bir bölüm içinde garanti edilir.** Bir bölüme yazılan mesajlar artan offset alır ve tüketici onları hep bu sırayla okur.

Bir konu genelde birden çok bölüme yayılır. Bu yüzden **konu düzeyinde bütünsel (global) bir sıra YOKTUR**. İki farklı bölümdeki mesajın hangisinin "önce" olduğu tanımsızdır. İkisinin aynı **Kafka sunucusunda (broker)** durması da bunu değiştirmez; her bölüm ayrı bir log'dur.

O hâlde soru şu: bir mesaj hangi bölüme düşer? Varsayılan bölüm seçici, anahtar varken `hash(key) % bolumSayisi` hesabını yapar. Sonuç her seferinde aynıdır. Yani **aynı anahtarı taşıyan bütün mesajlar hep aynı bölüme** düşer ve aralarındaki sıra korunur.

Anahtar `null` ise mesajlar sırayla farklı bölümlere dağıtılır (sticky / round-robin). O zaman belirli bir varlığa ait sıra garantisi kaybolur. Bu yüzden "bir siparişin olaylarını sırayla işle" ihtiyacında anahtar olarak `orderId` seçilir. Zaman damgası ya da mesaj boyutu bölüm seçiminde hiçbir rol oynamaz.

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Aynı key aynı partition'a gider; sıra yalnız partition içinde korunur, partition'lar arası global sıra yoktur">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sıra yalnız partition içinde korunur</text>
  <text x="320" y="44" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">key → hash(key) % partitionSayisi → hep aynı partition</text>

  <rect x="24" y="62" width="150" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="99" y="86" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Producer</text>
  <text x="99" y="108" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">A1 A2  (key=A)</text>
  <text x="99" y="126" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">B1 B2  (key=B)</text>

  <defs>
    <marker id="ksira-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0,0 L6,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>

  <line x1="174" y1="96" x2="360" y2="96" stroke="#5D7789" stroke-width="1.5" marker-end="url(#ksira-ok)"/>
  <line x1="174" y1="120" x2="360" y2="200" stroke="#5D7789" stroke-width="1.5" marker-end="url(#ksira-ok)"/>

  <rect x="366" y="70" width="250" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="384" y="92" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Partition 0</text>
  <text x="384" y="112" font-size="11.5" fill="#1C3247" font-family="system-ui">A1 → A2   (sıra korunur)</text>

  <rect x="366" y="176" width="250" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="384" y="198" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Partition 1</text>
  <text x="384" y="218" font-size="11.5" fill="#1C3247" font-family="system-ui">B1 → B2   (sıra korunur)</text>

  <line x1="491" y1="122" x2="491" y2="176" stroke="#5D7789" stroke-width="1.2" stroke-dasharray="4 4"/>
  <text x="500" y="152" font-size="10.5" fill="#5D7789" font-family="system-ui">P0 vs P1 arası</text>
  <text x="500" y="166" font-size="10.5" fill="#5D7789" font-family="system-ui">global sıra YOK</text>

  <text x="320" y="262" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">A1'in B1'den önce mi geldiği tanımsızdır — yalnız aynı partition içi sıra bellidir.</text>
  <text x="320" y="282" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">partitionSayisi değişirse aynı key farklı partition'a düşebilir → tarihsel sıra kırılır.</text>
</svg>
```

Buradan iki taviz çıkar.

**Birincisi paralellik ile sıra arasındaki tavizdir.** Kafka'da paralelliğin birimi bölümdür; bir grupta en fazla bölüm sayısı kadar tüketici aynı anda çalışabilir. Bölüm sayısı arttıkça paralellik artar, ama sıra garantisinin kapsamı **daralır**. Uçtaki örnek nettir: tek bölüm tam sıra verir, buna karşılık paralelliği sıfırlar. İkisi bağımsız değildir; aynı ayarla ters yönde belirlenirler.

**İkincisi bölüm sayısını sonradan değiştirmektir.** `hash(key) % bolumSayisi` hesabındaki bölen değişince aynı anahtar artık **başka bir bölüme** düşer. Kafka geçmiş mesajları taşımaz. Eski olaylar eski bölümde kalır, yeni olaylar başka bölüme gider ve **tarihsel sıra kırılır**.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Partition sayısı 3'ten 6'ya çıkınca aynı key farklı partition'a düşer, tarihsel sıra kırılır">
  <defs>
    <marker id="ksira-pc-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Partition sayısını artırmak tarihsel sırayı kırar</text>

  <text x="160" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Önce: partitionSayisi = 3</text>
  <rect x="60" y="70" width="200" height="42" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="160" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hash(order42) % 3 = 1</text>
  <text x="160" y="105" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">eski olaylar → P1</text>

  <text x="480" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Sonra: partitionSayisi = 6</text>
  <rect x="380" y="70" width="200" height="42" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="480" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hash(order42) % 6 = 4</text>
  <text x="480" y="105" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">yeni olaylar → P4</text>

  <line x1="260" y1="91" x2="378" y2="91" stroke="#5D7789" stroke-width="1.5" stroke-dasharray="4 3" marker-end="url(#ksira-pc-ok)"/>
  <text x="319" y="83" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">bölen değişti</text>

  <rect x="80" y="140" width="200" height="44" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="180" y="160" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">P1: order42 eski olaylar</text>
  <text x="180" y="177" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(Kafka geçmişi taşımaz)</text>

  <rect x="360" y="140" width="200" height="44" rx="8" fill="#FFFFFF" stroke="#B26B00" stroke-width="1.2"/>
  <text x="460" y="160" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">P4: order42 yeni olaylar</text>
  <text x="460" y="177" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iki ayrı log'a bölündü</text>

  <text x="320" y="214" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">Aynı order42 iki partition'a dağıldı → tarihsel sıra kırıldı</text>
  <text x="320" y="234" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Sıra-bağımlı sistemlerde partition sayısını baştan yeterli seç.</text>
</svg>
```

Üçüncü tuzak üretici tarafındadır. `max.in.flight` değeri 1'den büyükken bir **parti (batch)** başarısız olabilir ve **yeniden gönderilir (retry)**. Bu yeniden gönderim gecikirken, arkadan gelen partiler bölüme önce yazılabilir; sıra bozulur (reordering).

Çözüm `enable.idempotence=true` ayarıdır. **Tekrar edilse de sonucu değişmeyen (idempotent)** üretici her partiye artan bir sıra numarası (sequence number) ekler. Broker bu numaralara bakarak partileri doğru sıraya sokar; böylece `max.in.flight` 5'e kadar güvenli kalır. Kafka 3.x ve 4.x'te bu ayar varsayılan olarak açıktır. Kapatmak, klasik yeniden sıralama riskini geri getirir.

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="max.in.flight birden fazlayken idempotence kapalıysa retry sırayı bozar, açıkken sequence number sırayı korur">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">max.in.flight &gt; 1 + retry: idempotence sırayı belirler</text>

  <rect x="20" y="42" width="290" height="234" rx="12" fill="#EAF1F7" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="165" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">enable.idempotence = false</text>
  <text x="165" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">B1 başarısız → retry gecikir</text>
  <rect x="44" y="100" width="70" height="34" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.1"/>
  <text x="79" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">B1 ✗</text>
  <rect x="128" y="100" width="70" height="34" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.1"/>
  <text x="163" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">B2</text>
  <rect x="212" y="100" width="70" height="34" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.1"/>
  <text x="247" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">B3</text>
  <text x="165" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">partition'a yazılan sıra:</text>
  <rect x="44" y="170" width="238" height="40" rx="7" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.3"/>
  <text x="163" y="195" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">B2 → B3 → B1</text>
  <text x="165" y="236" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">partition içi sıra BOZULDU</text>
  <text x="165" y="258" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">geç yazılan B1 sona düşer</text>

  <rect x="330" y="42" width="290" height="234" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">enable.idempotence = true</text>
  <text x="475" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her batch'e sequence number</text>
  <rect x="354" y="100" width="70" height="34" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="389" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">B1 #1</text>
  <rect x="438" y="100" width="70" height="34" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="473" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">B2 #2</text>
  <rect x="522" y="100" width="70" height="34" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="557" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">B3 #3</text>
  <text x="475" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">broker sıraya sokar:</text>
  <rect x="354" y="170" width="238" height="40" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="473" y="195" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">B1 → B2 → B3</text>
  <text x="475" y="236" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">sıra KORUNUR</text>
  <text x="475" y="258" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">max.in.flight ≤ 5 güvenli</text>
</svg>
```

Son bir nokta: fiziksel offset sırası korunsa bile **mantıksal** sıra bozulabilir. Yeniden deneme + DLT deseninde başarısız mesaj kenara alınır ve tüketici ilerler. O zaman aynı anahtarın daha sonraki olayları, başarısız olan olaydan **önce** uygulanmış olur. Somut örnek: "güncelle" olayı DLT'ye giderken "iptal" olayı işlenir.

Üç çare vardır: o anahtarı geçici olarak bloklamak, DLT'yi sırayla yeniden işlemek, ya da kritik akışta mesajı atlamak yerine bölümün tamamını duraklatmak.

### Mülakatta ne sorulur?

- Sıra hangi kapsamda garanti? (Yalnız bölüm içinde — konu geneli, tüketici grubu ya da "aynı broker" ölçekleri değil.)
- Bir mesajın hangi bölüme gideceğini ne belirler? (Anahtarın hash'i; rastgelelik, zaman damgası veya mesaj boyutu değil.)
- Canlıda bölüm sayısını artırmanın riski? (Bölen değişir, tarihsel sıra kırılır; sorun disk ya da grup silinmesi değildir.)
- Paralellik/sıra tavizini anlat.
- `max.in.flight>1` + retry + `idempotence=false` ne yapar?
- Retry/DLT deseni sırayı nasıl bozar?

### Tek cümlede

Kafka sırayı yalnız **bölüm (partition) içinde** garanti eder; sırayı doğru anahtar seçimiyle kazanır, bölüm sayısını değiştirerek veya idempotence'ı kapatarak kaybedersiniz.

# ksira-1 | Orta
## Soru
Kafka'da mesaj sıralaması hangi kapsamda garanti edilir?
## A
Bir topic (konu) genelinde tüm mesajlar global olarak sıralıdır
🤖 Topic birden çok partition'a yayıldığından topic geneli global sıra yoktur.
## B
Aynı consumer group'u (tüketici grubu) okuyan tüm tüketiciler için topic genelinde sıralıdır
🤖 Tüketici grubu sırayı değiştirmez; sıra hâlâ yalnız partition içindedir.
## C
Yalnızca tek bir partition (bölme) içinde; partition'lar arası global sıra yoktur
🤖 Doğru: sıra partition içi offset artışıyla garanti edilir, partition'lar arası tanımsızdır.
## D
Aynı broker (sunucu) üzerindeki tüm partition'lar arasında sıralıdır
🤖 Broker aynı olsa da her partition bağımsız bir log'dur; aralarında sıra garantisi yoktur.
## Doğru
C
## Açıklama
Kafka sıralamayı yalnızca **partition** düzeyinde garanti eder: bir partition'a yazılan mesajlar artan **offset** ile saklanır ve hep bu sırayla okunur. Bir **topic** genelde birden fazla partition'a yayıldığı için topic düzeyinde **global sıra yoktur** (A, B yanlış). Aynı broker üzerinde birden çok partition bulunsa bile her partition bağımsız bir append-only log'dur; aralarında bir sıra ilişkisi tanımlı değildir (D yanlış). Global sıra gerçekten şartsa tek partition kullanılır — ki bu da paralelliği ortadan kaldırır.

# ksira-2 | Orta
## Soru
Varsayılan partitioner (bölmeleyici) kullanıldığında, bir mesajın hangi partition'a gideceğini ne belirler?
## A
Mesaj anahtarı (key); varsayılan olarak `hash(key) % partitionSayisi` ile eşlenir
🤖 Doğru: key'in hash'i partition sayısına göre modlanır, aynı key deterministik olarak aynı partition'a gider.
## B
Her mesaj rastgele bir partition'a atanır; anahtarın seçim üzerinde hiçbir etkisi yoktur
🤖 Key varsa dağıtım rastgele değil deterministiktir; key hash'i partition'ı belirler.
## C
Mesajın timestamp'i (zaman damgası) hangi partition'a gideceğini belirler
🤖 Timestamp partition seçiminde kullanılmaz; belirleyici key hash'idir.
## D
Mesajın bayt cinsinden boyutu hangi partition'a gideceğini belirler
🤖 Boyut partition seçimini etkilemez; eşleme key hash'ine dayanır.
## Doğru
A
## Açıklama
Varsayılan partitioner, key varken mesajı `hash(key) % partitionCount` ile bir partition'a eşler. Bu deterministik olduğu için **aynı key'e sahip tüm mesajlar hep aynı partition'a** düşer ve aralarındaki sıra korunur — sıra-bağımlı sistemlerde key seçimi bu yüzden kritiktir. Key `null` ise mesajlar **sticky/round-robin** dağıtılır (belirli bir varlığa ait sıra garantisi kaybolur). Timestamp veya mesaj boyutu partition seçiminde rol oynamaz (C, D yanlış); dağıtım key varken rastgele değildir (B yanlış).

# ksira-3 | İleri
## Soru
Sıra-bağımlı (aynı `orderId`'nin olaylarını sıralı işleyen) bir sistemde, canlıda partition sayısını 3'ten 6'ya çıkarmanın en önemli riski nedir?
## A
Hiçbir risk yok; Kafka eski mesajları otomatik olarak yeni partition düzenine göre yeniden dağıtır
🤖 Kafka geçmiş mesajları taşımaz; eski mesajlar eski partition'da kalır, bu da sırayı bozar.
## B
Yalnızca disk kullanımı ve depolama maliyeti artar; sıralama garantisi bundan hiç etkilenmez
🤖 Asıl sorun disk değil; bölen değişince key→partition eşlemesi kayar ve sıra kırılır.
## C
Tüketici grubu otomatik olarak silinir ve tüm offset'ler baştan sıfırlanır
🤖 Partition eklemek tüketici grubunu silmez; offset'ler sıfırlanmaz.
## D
`hash(key) % partitionSayisi`'ndaki bölen değişir; aynı key farklı partition'a düşer ve tarihsel sıra kırılır
🤖 Doğru: bölen değişince aynı orderId farklı partition'a düşebilir, eski mesajlar eski partition'da kalır, sıra bozulur.
## Doğru
D
## Açıklama
Varsayılan eşleme `hash(key) % partitionCount` olduğundan, `partitionCount` 3→6 değişince aynı `orderId`'nin hash'i **farklı bir partition'a** düşebilir. Ancak o key'in daha önce yazılmış mesajları **eski partition'da** kalır; Kafka geçmişi yeniden dağıtmaz (A yanlış). Sonuç: aynı varlığın olayları iki farklı partition'a bölünür ve partition'lar arası sıra garantisi olmadığından **tarihsel sıra kırılır**. Bu yüzden sıra-bağımlı sistemlerde partition sayısı baştan yeterli seçilir ve canlıda artırmaktan kaçınılır. Sorun disk (B) veya tüketici grubunun silinmesi (C) değildir.

# ksira-4 | İleri
## Soru
Aşağıdaki ifadelerden hangisi paralellik ile sıralama arasındaki tavizi doğru anlatır?
## A
Daha fazla partition hem daha fazla paralellik hem de daha güçlü global sıra garantisi verir
🤖 Partition artışı paralelliği artırır ama global sırayı zayıflatır; ikisi aynı anda güçlenmez.
## B
Daha fazla partition daha çok paralellik ama daha dar sıra kapsamı verir; tek partition bunun tersidir
🤖 Doğru: partition sayısı arttıkça paralellik artar sıra kapsamı daralır; tek partition tam sıra verir paralellik olmaz.
## C
Tek partition kullanmak hem paralelliği hem sıra garantisini aynı anda maksimuma çıkarır
🤖 Tek partition tam sıra verir ama paralelliği sıfırlar; ikisi birden maksimum olmaz.
## D
Partition sayısı ne sıralamayı ne paralelliği etkiler; bu ikisi tümüyle bağımsız boyutlardır
🤖 İkisi doğrudan ilişkilidir; partition sayısı hem paralelliği hem sıra kapsamını belirler.
## Doğru
B
## Açıklama
Kafka'da paralellik birimi partition'dır: bir tüketici grubunda aynı anda en fazla partition sayısı kadar tüketici aktif çalışabilir. Partition sayısı arttıkça **paralellik (throughput) artar**, ancak sıra yalnız partition içinde korunduğundan **sıra garantisinin kapsamı daralır** — mesajlar daha çok bağımsız log'a yayılır. Bu iki şey aynı anda güçlenemez (A yanlış). Uçta tek partition **tam sıra** verir ama **paralelliği sıfırlar**; ikisini birden maksimuma çıkarmaz (C yanlış). Partition sayısı ikisini birlikte belirlediği için bağımsız da değildirler (D yanlış). Doğru yaklaşım, sıra ihtiyacını key ile en dar varlık düzeyine indirip partition sayısını buna göre seçmektir.

# ksira-5 | İleri
## Soru
```java
props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION_CONFIG, 5);
props.put(ProducerConfig.RETRIES_CONFIG, 3);
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
```
Bu producer yapılandırmasında, bir batch başarısız olup retry (yeniden deneme) edilirse partition içi sıra açısından ne olabilir?
## A
İdempotence kapalı olduğundan yeniden denenen batch sonrakilerin arkasına yazılabilir; partition içi sıra bozulur
🤖 Doğru: uçuşta birden çok istek varken bir batch retry edilince sonrakiler önce yazılabilir, sıra bozulur; idempotence bunu engellerdi.
## B
Retry olsa bile Kafka partition içi sırayı her koşulda otomatik korur; bu ayarların sıraya hiçbir etkisi yoktur
🤖 Idempotence kapalıyken max.in.flight>1 + retry sırayı bozabilir; otomatik koruma yoktur.
## C
`max.in.flight` yalnız verimi etkileyen bir performans ayarıdır; sıralama ile hiçbir ilişkisi yoktur
🤖 max.in.flight uçuştaki istek sayısıdır ve idempotence kapalıyken doğrudan yeniden sıralamaya yol açabilir.
## D
Sıra ancak tüketici tarafında bozulabilir; producer ayarları partition içi yazma sırasını etkilemez
🤖 Yeniden sıralama tam da producer tarafında, yazma sırasında oluşur; ayarlar bunu belirler.
## Doğru
A
## Açıklama
`max.in.flight.requests.per.connection > 1` iken kanal üzerinde aynı anda birden fazla gönderim "uçuşta" olabilir. **`enable.idempotence=false`** ise ve bir batch başarısız olup **retry** edilirse, retry gecikirken sonraki batch'ler partition'a önce yazılabilir → **partition içi sıra bozulur** (reordering). Çözüm **`enable.idempotence=true`**'dur: idempotent producer her batch'e sıra numarası (sequence number) ekler, broker bunları sıraya sokar ve `max.in.flight`'ı 5'e kadar güvenle korur. Modern Kafka'da (3.x/4.x) idempotence varsayılan olarak açıktır; onu kapatmak bu klasik yeniden sıralama riskini geri getirir. Sıralama otomatik korunmaz (B), `max.in.flight` sadece performans değildir (C) ve sorun producer tarafında doğar (D).

# ksira-6 | İleri
## Soru
Bir consumer, başarısız mesajları retry sonrası bir **DLT** (Dead Letter Topic / ölü mektup konusu) altına yönlendiriyor ve sonraki mesajları işlemeye devam ediyor. Partition içi mantıksal sıra açısından temel risk nedir?
## A
DLT deseni partition içi sırayı her zaman korur; başarısız mesaj DLT'ye gitse de mantıksal sıra bozulmaz
🤖 Başarısız mesaj kenara alınıp sonrakiler geçince o varlığa ait mantıksal sıra bozulur; koruma otomatik değildir.
## B
DLT kullanmak ana topic'in partition sayısını otomatik artırır ve sıra bu yüzden bozulur
🤖 DLT partition sayısını değiştirmez; sıra bozulması mesajın kenara alınıp sonrakinin geçmesinden kaynaklanır.
## C
Başarısız mesaj kenara alınıp sonrakiler işlenince, aynı key'in olayları yanlış sırada uygulanır
🤖 Doğru: mesaj kenara alınıp sonrakiler geçince, aynı varlığın olayları yanlış sırada işlenir, mantıksal sıra bozulur.
## D
Tek risk DLT topic'inin diski doldurmasıdır; uygulama düzeyindeki işleme sırası hiç etkilenmez
🤖 Asıl risk disk değil; başarısız mesajın atlanması aynı key'in olay sırasını mantıksal olarak bozar.
## Doğru
C
## Açıklama
Kafka'nın partition içi **fiziksel** offset sırası korunur; ancak **retry + DLT** deseni **mantıksal** sırayı bozabilir. Bir mesaj başarısız olup DLT'ye alınır ve tüketici ilerlemeye devam ederse, **aynı key'in** (örn. aynı `orderId`) daha sonraki olayları, başarısız olan olaydan **önce** işlenmiş olur. Örneğin "sipariş güncelle" başarısız olup DLT'ye giderken "sipariş iptal" işlenirse durum tutarsızlaşır. Bu, offset sırasının değil, uygulama düzeyindeki neden-sonuç sırasının kırılmasıdır. Çözümler: aynı key başarısız olduğunda o key'i geçici bloklamak, DLT'yi sıralı yeniden işlemek veya kritik akışlarda "sonrakini atlama" yerine partition'ı duraklatmaktır. Sorun DLT diskinin dolması (D) veya partition sayısının değişmesi (B) değildir; DLT deseni sırayı otomatik korumaz (A).
