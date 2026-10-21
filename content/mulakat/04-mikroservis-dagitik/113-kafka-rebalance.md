---
slug: kafka-rebalance
baslik: "Kafka Rebalance: Eager vs Cooperative, Assignor'lar ve Static Membership"
kademe: İleri
sira: 113
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir posta kutusuna sürekli mektup düşüyor ve bunları okuyup işleyecek birkaç kişi var. Kimin hangi mektupları okuyacağını birinin paylaştırması gerekir; biri işe girdiğinde ya da işten ayrıldığında da bu paylaşımı yenilemek gerekir. Kafka'da bu yeniden paylaştırma işine **yeniden dengeleme (rebalance)** denir. Konuyu bilmek şu iki soruyu cevaplar: "Uygulamam neden ara ara birkaç saniye hiç mesaj işlemedi?" ve "Neden daha çok tüketici ekledim de hız artmadı?"

### Temel kavramlar

Hiçbirini bilmediğinizi varsayarak sıfırdan:

- **Topic (konu):** aynı türden mesajların biriktiği isimli bir defter, örneğin `siparisler`.
- **Partition (bölme):** o defterin parçalara ayrılmış hâli. Her parça, sona yeni satır eklenen bağımsız bir listedir. Kafka'da işi paralel yürütmenin tek birimi budur.
- **Consumer (tüketici):** mesajları okuyup işleyen program. Sürekli `poll()` (getir) çağırarak yeni mesaj ister.
- **Tüketici grubu (consumer group):** aynı `group.id` etiketini taşıyan tüketiciler kümesi. Kafka bu kümeyi **tek bir okuyucu** gibi görür: her partition, grubun **yalnız bir** üyesine verilir. Böylece aynı mesaj grup içinde iki kez işlenmez ama iş üyeler arasında bölüşülür.
- **Group coordinator (grup koordinatörü):** grubun üyelerini takip eden ve partition dağıtımını yöneten broker (sunucu).
- **Yeniden dengeleme (rebalance):** partition'ların grup üyeleri arasında yeniden paylaştırılması.
- **Assignor (atayıcı):** dağıtımı hangi kurala göre yapacağını belirleyen bileşen; `partition.assignment.strategy` ayarıyla seçilir.
- **Heartbeat (kalp atışı):** tüketicinin arka planda gönderdiği "hayattayım" sinyali.

### Nasıl çalışır?

Rebalance kendiliğinden olmaz. Onu **üyelik ya da üst veri (metadata) değişikliği** tetikler. Tetikleyen durumlar şunlardır:

- gruba yeni bir tüketici katılır;
- bir tüketici kapanır veya çöker;
- bir tüketici `session.timeout.ms` süresi boyunca kalp atışı gönderemez;
- bir tüketici iki `poll()` çağrısı arasında `max.poll.interval.ms` sınırını aşar;
- topic'in partition sayısı değişir.

Buna karşılık **zamanında `poll()` eden sağlıklı bir tüketici hiçbir şey tetiklemez**. O, işlerin yolunda gittiği normal durumdur.

```svg
<svg viewBox="0 0 640 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Rebalance tetikleyicileri grup koordinatörünü partition'ları yeniden paylaştırmaya yöneltir; sağlıklı poll tetiklemez">
  <defs>
    <marker id="kreb-trig-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Rebalance'ı ne tetikler?</text>

  <rect x="20" y="44" width="234" height="160" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="137" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Üyelik / metadata değişimi</text>
  <text x="36" y="90" font-size="10.5" fill="#1C3247" font-family="system-ui">• yeni tüketici katılır</text>
  <text x="36" y="112" font-size="10.5" fill="#1C3247" font-family="system-ui">• tüketici ayrılır / çöker</text>
  <text x="36" y="134" font-size="10.5" fill="#1C3247" font-family="system-ui">• session.timeout.ms aşımı</text>
  <text x="36" y="156" font-size="10.5" fill="#1C3247" font-family="system-ui">• max.poll.interval.ms aşımı</text>
  <text x="36" y="178" font-size="10.5" fill="#1C3247" font-family="system-ui">• topic partition sayısı değişir</text>

  <line x1="254" y1="124" x2="290" y2="124" stroke="#5D7789" stroke-width="1.6" marker-end="url(#kreb-trig-ok)"/>

  <rect x="292" y="96" width="150" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="367" y="120" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Group Coordinator</text>
  <text x="367" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yeni atamayı hesaplar</text>

  <line x1="442" y1="124" x2="478" y2="124" stroke="#5D7789" stroke-width="1.6" marker-end="url(#kreb-trig-ok)"/>

  <rect x="480" y="96" width="146" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="553" y="120" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Partition'lar</text>
  <text x="553" y="138" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yeniden paylaştırılır</text>

  <text x="320" y="226" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Zamanında poll() eden sağlıklı tüketici tetiklemez — normal işleyiştir.</text>
</svg>
```

İki farklı rebalance protokolü vardır. Klasik olanın adı **eager**'dır, yani "hevesli". Rebalance başlar başlamaz **bütün** üyeler ellerindeki **bütün** partition'ları bırakır (revoke). Ardından koordinatör yeni dağıtımı hesaplar ve herkes yeni setini alır. Bu süre boyunca **hiçbir partition işlenmez**; buna "dünyayı durdur" (stop-the-world) denir. Tek bir partition el değiştirecek olsa bile tüm grup durur.

Kafka 2.4 ile gelen **cooperative (incremental)**, yani **kademeli** protokol bu maliyeti düşürür. Rebalance iki tura yayılır. Birinci turda yalnız **sahibi değişecek** partition'lar bırakılır. İkinci turda bunlar yeni sahiplerine verilir. Taşınmayan partition'lar hiç bırakılmadığı için **kesintisiz işlenmeye devam eder**. Bu davranışı `CooperativeStickyAssignor` (KIP-429) sağlar.

Dikkat edilecek nokta: diğer atayıcılar hâlâ **eager** protokolle çalışır. `RangeAssignor` her topic için ayrı bir aralık verir ve çok sayıda topic'te dağılım dengesizleşebilir. `RoundRobinAssignor` partition'ları sırayla dağıtır. `StickyAssignor` ise eski dağılımı olabildiğince korumaya çalışır. Şunu karıştırmayın: "sticky" (yapışkan dağıtım) ile "cooperative" (kademeli bırakma) **ayrı** şeylerdir. İkisini tek başına yalnız `CooperativeStickyAssignor` birleştirir.

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Eager rebalance tüm partition'ları bırakır, cooperative yalnız taşınanları bırakır">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Eager vs Cooperative (Incremental) Rebalance</text>

  <rect x="24" y="46" width="288" height="234" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="168" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">Eager (stop-the-world)</text>

  <rect x="48" y="88" width="112" height="40" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="104" y="112" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">C1: P0 P1 P2</text>
  <rect x="176" y="88" width="112" height="40" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="232" y="112" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">C2: P3 P4 P5</text>

  <text x="168" y="150" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">HEPSİ bırakılır ↓ işleme durur</text>

  <rect x="48" y="162" width="240" height="34" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2" stroke-dasharray="4 3"/>
  <text x="168" y="184" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">C1: —   C2: —   (sahipsiz)</text>

  <text x="168" y="216" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">yeniden ata ↓</text>

  <rect x="48" y="228" width="112" height="40" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="104" y="252" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">C1: P0 P1</text>
  <rect x="176" y="228" width="112" height="40" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="232" y="252" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">C2: P3 P4 P2</text>

  <rect x="328" y="46" width="288" height="234" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">Cooperative (incremental)</text>

  <rect x="352" y="88" width="112" height="40" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="408" y="112" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">C1: P0 P1 P2</text>
  <rect x="480" y="88" width="112" height="40" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="536" y="112" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">C2: P3 P4 P5</text>

  <text x="472" y="150" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">yalnız P2 bırakılır ↓</text>

  <rect x="352" y="162" width="240" height="34" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="472" y="184" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">C1: P0 P1 (işler)   C2: P3 P4 (işler)</text>

  <text x="472" y="216" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">yalnız P2'yi ata ↓</text>

  <rect x="352" y="228" width="112" height="40" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="408" y="252" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">C1: P0 P1</text>
  <rect x="480" y="228" width="112" height="40" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="536" y="252" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">C2: P3 P4 P2</text>
</svg>
```

Üçüncü kaldıraç **statik üyeliktir** (static membership, `group.instance.id`, KIP-345). Burada tüketiciye kalıcı bir kimlik verirsiniz. Kubernetes'te bir pod'un yeni sürüm yüklenirken yeniden başladığını düşünün. Tüketici `session.timeout.ms` dolmadan **aynı kimlikle** geri dönerse, koordinatör onu hâlâ aynı üye sayar. Bu durumda **rebalance tetiklenmez** ve tüketici eski partition'larıyla kaldığı yerden devam eder. Tek şart şudur: `session.timeout.ms`, tipik yeniden başlatma süresinden uzun olmalıdır.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Statik üyelik: kısa restart'ta aynı group.instance.id ile dönen tüketici rebalance tetiklemez">
  <defs>
    <marker id="kreb-static-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Statik üyelik kısa restart'ta rebalance'ı önler</text>

  <rect x="20" y="42" width="600" height="80" rx="10" fill="#EAF1F7" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="36" y="64" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">group.instance.id YOK</text>
  <rect x="40" y="74" width="120" height="34" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.1"/>
  <text x="100" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Pod restart</text>
  <line x1="160" y1="91" x2="196" y2="91" stroke="#5D7789" stroke-width="1.5" marker-end="url(#kreb-static-ok)"/>
  <rect x="198" y="74" width="150" height="34" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.1"/>
  <text x="273" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yeni üye kimliği</text>
  <line x1="348" y1="91" x2="384" y2="91" stroke="#5D7789" stroke-width="1.5" marker-end="url(#kreb-static-ok)"/>
  <rect x="386" y="74" width="220" height="34" rx="7" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.3"/>
  <text x="496" y="96" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">REBALANCE tetiklenir</text>

  <rect x="20" y="134" width="600" height="80" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="36" y="156" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">group.instance.id SABİT (session.timeout içinde döner)</text>
  <rect x="40" y="166" width="120" height="34" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.1"/>
  <text x="100" y="188" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Pod restart</text>
  <line x1="160" y1="183" x2="196" y2="183" stroke="#5D7789" stroke-width="1.5" marker-end="url(#kreb-static-ok)"/>
  <rect x="198" y="166" width="150" height="34" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.1"/>
  <text x="273" y="188" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">aynı kimlikle döner</text>
  <line x1="348" y1="183" x2="384" y2="183" stroke="#5D7789" stroke-width="1.5" marker-end="url(#kreb-static-ok)"/>
  <rect x="386" y="166" width="220" height="34" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="496" y="188" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">rebalance YOK, devam eder</text>

  <text x="320" y="236" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Koşul: session.timeout.ms restart süresinden uzun olmalı.</text>
</svg>
```

### Mülakatta ne sorulur?

- **Rebalance'ı ne tetikler, ne tetiklemez?** Üyelik/metadata değişimi tetikler; zamanında `poll()` eden sağlıklı tüketici tetiklemez.
- **Eager mi cooperative mi?** Eager hepsini bırakır ve tüm grup durur; cooperative yalnız taşınanları bırakır. Rebalance ne cooperative ile ortadan kalkar ne de "bırakmayı hızlandırarak" ucuzlar; partition'lar da hiçbir zaman broker'lara değil grup üyelerine atanır.
- **Kubernetes'te her deploy'da rebalance oluyor, ne yaparsın?** Kalıcı `group.instance.id` + restart süresini kapsayan `session.timeout.ms`. `session.timeout.ms=0` işe yaramaz: broker `group.min.session.timeout.ms` alt sınırını (varsayılan 6 saniye) uygular, sıfır değeri reddedilir ve tüketici gruba hiç katılamaz. `enable.auto.commit` ve `max.poll.records` ise üyelik kimliğiyle ilgisizdir.
- **"Rebalance fırtınası" (storm) nasıl kırılır?** Loglarda `max.poll.interval.ms` aşımı varsa kök neden **yavaş işlemedir**. Çözüm: işi hızlandırın veya parçalayın, `max.poll.records` değerini düşürün, gerekirse sınırı gerçekçi bir süreye çıkarın, ağır işi ayrı bir thread havuzuna alın. Yanlış çözümler de vardır. `session.timeout.ms` değerini milisaniyelere indirmek sağlıklı üyeleri de gruptan düşürür. Partition silmek genelde ne mümkündür ne de konuyla ilgilidir. Cooperative protokol maliyeti azaltır ama poll aşımının kök nedenini **çözmez**.

### Tek cümlede

Rebalance, tüketici grubunun partition'ları yeniden paylaştırmasıdır; **cooperative** protokol duruşu yalnız el değiştiren partition'larla sınırlar, **statik üyelik** ise kısa restart'larda rebalance'ı baştan engeller.

# kreb-1 | Orta
## Soru
Aşağıdakilerden hangisi bir tüketici grubunda rebalance'ı **tetiklemez**?
## A
Gruba aynı `group.id` ile yeni bir tüketici katılır
🤖 Yeni üye eklenmesi partition'ların yeniden paylaşılmasını gerektirir; rebalance tetikler.
## B
Bir tüketici `poll()` çağrılarını `max.poll.interval.ms` içinde düzenli olarak sürdürür
🤖 Doğru: zamanında poll eden sağlıklı tüketici gruptan düşmez; hiçbir rebalance gerekmez.
## C
Bir tüketici `session.timeout.ms` boyunca kalp atışı (heartbeat) gönderemez
🤖 Koordinatör tüketiciyi ölü sayıp gruptan atar; bu rebalance tetikler.
## D
Abone olunan topic'in partition sayısı artırılır ve metadata değişir
🤖 Partition sayısı değişince metadata güncellenir ve yeni partition'ı atamak için rebalance olur.
## Doğru
B
## Açıklama
Rebalance; **üyelik veya metadata değişince** olur: yeni üye katılması (A), bir üyenin `session.timeout.ms` içinde heartbeat gönderememesi (C) veya topic partition sayısının değişmesi (D). Buna karşın **sağlıklı, zamanında `poll()` eden** bir tüketici (B) grupta kalır ve hiçbir yeniden paylaşım gerektirmez — bu normal işleyen durumdur. Not: heartbeat arka planda ayrı bir thread'de gider ama tüketici `max.poll.interval.ms` içinde `poll` çağırmazsa "canlı görünüp işlemeyi bırakmış" sayılır ve gruptan atılır; B seçeneği tam da bu sınırın içinde kalmayı tarif ettiği için tetiklemez.

# kreb-2 | Orta
## Soru
**Eager rebalance** stratejisinde bir rebalance başladığında partition işleme davranışı ne olur?
## A
Yalnız yeni katılan tüketici bekler; mevcut tüketiciler partition'larını işlemeye devam eder
🤖 Bu cooperative davranışına benzer; eager'da böyle seçici bir devam yoktur.
## B
Sadece koordinatörün bulunduğu broker'daki partition'lar durur, diğerleri işlemeye devam eder
🤖 Rebalance broker konumuna göre değil grup üyeliğine göre çalışır; böyle bir ayrım yoktur.
## C
Partition'lar hiç bırakılmaz; atama arka planda sessizce güncellenir ve işleme etkilenmez
🤖 Eager tam tersini yapar: tüm partition'lar bırakılır, "sessiz" bir güncelleme değildir.
## D
Tüm tüketiciler tüm partition'larını bırakır (revoke) ve yeni atama gelene kadar işleme durur
🤖 Doğru: eager "stop-the-world"dur; sahiplik topluca sıfırlanır ve işleme durur.
## Doğru
D
## Açıklama
**Eager rebalance** "dünyayı durdur" (stop-the-world) modelidir: rebalance başlar başlamaz grubun **tüm** üyeleri ellerindeki **tüm** partition'ları bırakır, koordinatör yeni atamayı hesaplar ve dağıtır; bu süre boyunca **hiçbir partition işlenmez**. Yalnız bir tek partition taşınacak olsa bile tüm grup durur — cooperative rebalance'ın çözdüğü asıl maliyet budur. A cooperative davranışını, B uydurma bir broker-lokal davranışını, C ise "bırakma yok" iddiasını tarif ettiği için yanlıştır.

# kreb-3 | İleri
## Soru
`CooperativeStickyAssignor` ile **cooperative (incremental) rebalance**, eager'a göre temel kazancı nasıl sağlar?
## A
Rebalance'ı iki tura yayar; yalnız sahibi değişecek partition'lar bırakılır, diğerleri işlenmeye devam eder
🤖 Doğru: yalnız hareket eden partition'lar revoke edilir, gerisi kesintisiz işlenir.
## B
Tüm partition'ları bırakır ama bırakma ile yeniden atama arasındaki süreyi kısaltarak toplam duruşu azaltır
🤖 Bu hâlâ stop-the-world'dür; cooperative'in amacı bırakmayı seçicileştirmektir, hızlandırmak değil.
## C
Rebalance'ı tamamen ortadan kaldırır; partition'lar bir kez atanır ve bir daha hiç değişmez
🤖 Rebalance yine olur; cooperative onu ucuzlatır, yok etmez.
## D
Partition'ları tüketiciler yerine broker'lara atayarak istemci tarafındaki rebalance ihtiyacını kaldırır
🤖 Partition ataması hep tüketici grubu içindir; broker'lara atama diye bir şey yoktur.
## Doğru
A
## Açıklama
**Cooperative (incremental) rebalance** (Kafka 2.4+, `CooperativeStickyAssignor`, KIP-429) rebalance'ı **iki tura** yayar: ilk turda mevcut atama korunur ve yalnız sahibi değişecek partition'lar tespit edilip **revoke** edilir; ikinci turda bu boşta kalan partition'lar yeni sahiplerine atanır. Taşınmayan partition'lar **hiç bırakılmadığı için kesintisiz işlenir**. Bu, "stop-the-world" duruşunu tüm grup yerine yalnız hareket eden partition'larla sınırlar. B hâlâ hepsini bırakan bir model tarif eder (yanlış), C rebalance'ı tümden kaldırdığını iddia eder (yanlış), D ise partition atamasının tüketici grubu içinde olduğu gerçeğini çiğner.

# kreb-4 | İleri
## Soru
```java
props.put(
  ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
  "org.apache.kafka.clients.consumer.CooperativeStickyAssignor");
```
`RangeAssignor`, `RoundRobinAssignor`, `StickyAssignor` ve `CooperativeStickyAssignor` için hangisi **doğrudur**?
## A
Dördü de incremental (cooperative) protokolü kullanır; aradaki fark yalnız partition dağıtım dengesidir
🤖 Yalnız `CooperativeStickyAssignor` cooperative protokolü kullanır; diğer üçü eager'dır.
## B
`RangeAssignor` partition'ları her zaman en dengeli biçimde dağıtır; çok topic'te bile dengesizlik oluşmaz
🤖 Range topic başına aralık atar ve çok-topic'te dengesizlik üretebilir; "her zaman en dengeli" yanlış.
## C
`RangeAssignor`, `RoundRobinAssignor` ve `StickyAssignor` eager protokol kullanır; `CooperativeStickyAssignor` cooperative kullanır
🤖 Doğru: yalnız sonuncusu cooperative; ilk üçü eager rebalance yapar.
## D
`StickyAssignor` da cooperative protokol kullandığı için `CooperativeStickyAssignor` ile davranış olarak birebir aynıdır
🤖 `StickyAssignor` yapışkanlığı korur ama protokolü eager'dır; ikisi aynı değildir.
## Doğru
C
## Açıklama
`partition.assignment.strategy` ile seçilen atayıcılardan `RangeAssignor` (topic başına aralık), `RoundRobinAssignor` (tüm partition'ları sırayla dağıtır) ve `StickyAssignor` (yeniden atamada mevcut dağılımı olabildiğince korur) **eager protokolle** çalışır — rebalance'da hepsi tüm partition'ları bırakır. Yalnız `CooperativeStickyAssignor` **incremental/cooperative protokolü** kullanır ve StickyAssignor'ın yapışkanlığını cooperative rebalance ile birleştirir. Kritik ayrım: "sticky" (yapışkan dağıtım) ile "cooperative" (kademeli bırakma) **farklı** şeylerdir; D bunları karıştırır. A tümünü cooperative sanır, B ise Range'i abartır (çok topic'te dengesizdir).

# kreb-5 | İleri
## Soru
Kubernetes'te 10 tüketicili bir grup her deploy'da (pod'lar hızlıca yeniden başlıyor) tam rebalance yaşıyor ve işleme birkaç saniye duruyor. Kısa restart'larda bu gereksiz rebalance'ı önlemenin en doğrudan yolu nedir?
## A
`session.timeout.ms` değerini 0'a çekmek, böylece koordinatör tüketicileri hiç zaman aşımına uğratmaz
🤖 0 timeout tüketiciyi anında ölü sayar; rebalance'ı azaltmaz, kötüleştirir.
## B
`enable.auto.commit`'i kapatıp offset'leri elle commit etmek, böylece üyelik kimliği restart boyunca korunur
🤖 Offset commit davranışını değiştirir; üyelik kimliğiyle ilgisi yoktur, rebalance'ı önlemez.
## C
Her tüketiciye `max.poll.records=1` verip poll başına tek kayıt işlemek, böylece restart daha hızlı tamamlanır
🤖 Poll başına kayıt sayısını düşürür; restart kaynaklı rebalance'la ilgisizdir.
## D
Her tüketiciye kalıcı bir `group.instance.id` (statik üyelik) atamak ve `session.timeout.ms`'i restart süresinden uzun tutmak
🤖 Doğru: statik kimlikle dönen tüketici timeout dolmadan aynı partition'ları alır, rebalance olmaz.
## Doğru
D
## Açıklama
**Statik üyelik (static membership, `group.instance.id`, KIP-345)** tüketiciye kalıcı bir kimlik verir. Pod kısa süreli yeniden başlarken **`session.timeout.ms` dolmadan** aynı `group.instance.id` ile geri dönerse, koordinatör onu "hâlâ aynı üye" sayar ve **rebalance tetiklemez**; tüketici eski partition'larıyla devam eder. İşe yaraması için `session.timeout.ms`'in tipik restart süresinden **uzun** olması gerekir. A (`session.timeout.ms=0`) zaten uygulanamaz: broker bu değeri `group.min.session.timeout.ms` alt sınırının (varsayılan 6 saniye) altında bulup reddeder ve tüketici gruba hiç katılamaz; B (auto-commit) ve C (`max.poll.records`) üyelik/rebalance ile ilgisiz ayarlardır.

# kreb-6 | İleri
## Soru
Bir grup sürekli rebalance döngüsüne ("rebalance storm") giriyor: tüketiciler tekrar tekrar gruba girip çıkıyor. Loglarda tüketicilerin `max.poll.interval.ms`'i aştığı görülüyor. Bu fırtınanın **en olası kök nedeni ve doğru düzeltmesi** nedir?
## A
`session.timeout.ms` çok uzun olduğu için ölü üyeler geç fark ediliyor; onu birkaç milisaniyeye düşürmek fırtınayı bitirir
🤖 Timeout'u aşırı kısaltmak sağlıklı tüketicileri de düşürüp fırtınayı büyütür, çözmez.
## B
Poll döngüsündeki işleme süresi `max.poll.interval.ms`'i aşıyor; işlemeyi hızlandırmak ya da bu sınırı gerçekçi süreye çıkarmak gerekir
🤖 Doğru: kök neden yavaş işlemenin poll aralığını aşması; işi küçültmek veya sınırı yükseltmek döngüyü kırar.
## C
Partition sayısı tüketici sayısından fazla olduğu için üyeler zorlanıyor; partition silip sayıyı tüketici sayısına indirmek gerekir
🤖 Partition fazlalığı tek başına storm yaratmaz ve partition silmek genelde mümkün/güvenli değildir.
## D
`CooperativeStickyAssignor` kullanmak, poll aşımı sürse bile her türlü rebalance fırtınasını otomatik olarak imkânsız kılar
🤖 Cooperative maliyeti düşürür ama poll aralığı aşımı kök nedenini çözmez; storm devam eder.
## Doğru
B
## Açıklama
**Rebalance fırtınası (storm)** çoğu zaman şu döngüden doğar: bir tüketici `poll()` çağrıları arasında **`max.poll.interval.ms`'ten uzun** işleme yapar → koordinatör onu ölü sayıp gruptan atar → rebalance → tüketici geri döner → yine yavaş işler → yine atılır. Kök neden **yavaş/uzun işlemedir**, üyelik ayarı değil. Doğru düzeltme: işlemeyi hızlandırmak veya `poll` başına iş yükünü küçültmek (`max.poll.records`'u düşürmek), gerekirse `max.poll.interval.ms`'i gerçekçi işleme süresine göre **artırmak** ve ağır işi ayrı bir thread havuzuna almak. A (`session.timeout.ms`'i milisaniyeye düşürmek) sağlıklı üyeleri de atarak durumu kötüleştirir; C partition silmeyi önerir (genelde imkânsız ve konudan uzak); D ise cooperative'in kök nedeni çözdüğünü sanır — oysa poll aşımı devam ettikçe storm da sürer.
