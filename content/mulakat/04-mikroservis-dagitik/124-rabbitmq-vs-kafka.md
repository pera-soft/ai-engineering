---
slug: rabbitmq-vs-kafka
baslik: "RabbitMQ vs Kafka: Push/Pull, Retention, Karar Matrisi ve Spring AMQP"
kademe: İleri
sira: 124
etiketler: 10,11
---

## Konu anlatımı

### Bu ne işe yarar?

İki farklı iş düşün. Birincisi: bir restoranın mutfağı. Garson sipariş fişini mutfağa asar, boşta olan aşçı fişi alır, yemeği yapar, fiş çöpe gider. Fişin saklanmasına gerek yok; iş bitince biter. İkincisi: bir bankanın hesap defteri. Her işlem satır satır deftere yazılır ve **hiç silinmez**; ay sonunda muhasebeci defteri baştan okur, denetçi ayrı bir yerden başlayıp yine baştan okur.

**RabbitMQ** birinci işin aracıdır: bir **iş kuyruğu**. **Kafka** ikinci işin aracıdır: bir **kayıt defteri (log)**. Mülakatta "hangisi daha iyi?" sorusunun doğru cevabı yoktur; doğru soru **"hangi iş için?"**dir. Bu konu, o kararı dört ölçüte oturtur.

### Temel kavramlar

- **İtme (push)**: mesajı **aracı sunucu (broker)** kendisi tüketiciye gönderir. RabbitMQ böyledir; akış, **tüketiciye peşin verilen mesaj sayısı (prefetch)** tavanıyla denetlenir.
- **Çekme (pull)**: tüketici hazır olduğunda broker'dan **kendisi ister** (`poll()`). Kafka böyledir.
- **Geri basınç (backpressure)**: tüketici yetişemediğinde akışın yavaşlaması. Çekme modelinde bu denetim doğal olarak tüketicinin elindedir.
- **Saklama süresi (retention)**: Kafka'da mesajın kayıt defterinde kaç gün / kaç GB tutulacağı.
- **Tekrar okuma (replay)**: aynı mesajları geçmişten yeniden okumak.
- **Okuma konumu (offset)**: kayıt defterindeki satır numarası; her tüketici grubu kendi konumunu taşır.
- **Bölüm (partition)**: Kafka'da bir konu başlığının (topic) paralel çalışan parçası. Paralellik ve sıra garantisi buna bağlıdır.
- **Kaynak-doğruluk (source of truth)**: sistemin "asıl gerçek" saydığı, uzun süre saklanan kayıt.
- **Ölü mektup kuyruğu (DLQ)**: işlenemeyen mesajların incelenmek üzere biriktiği kuyruk.

### Nasıl çalışır?

Dört eksende düşün:

1. **Teslim modeli**: RabbitMQ **iter (push)**, Kafka **çektirir (pull)**. İtme, düşük gecikme ve klasik iş kuyruğu için iyidir. Çekme ise tüketicinin kendi hızını dayatmasını ve toplu (batch) okumayı kolaylaştırır. Bu ayrımın ne depolamayla ne de protokolle ilgisi vardır.
2. **Saklama**: RabbitMQ'nun klasik ve çoğunluk (quorum) kuyruğunda mesaj **onaylanınca (ack) silinir**. Aynı mesajı ikinci bir tüketicinin baştan okuması için yerleşik bir yol yoktur. Kafka ise bir **yazıldığı gibi kalan kayıt defteridir (commit log)**: mesaj saklama süresi boyunca **tutulur** ve farklı tüketiciler farklı okuma konumlarından **tekrar okur (replay)**. RabbitMQ tarafında bu ihtiyacı **Streams** karşılar.
3. **Yönlendirme mi verim mi**: RabbitMQ'nun gücü **esnek yönlendirmedir** — topic/headers exchange ile "şu desene uyan, şu başlığı taşıyan mesaj şu kuyruğa" kuralı **broker düzeyinde**, uygulama kodu yazmadan kurulur. Kafka'nın gücü **çok yüksek verim + sıralı log + replay**tır; içerik-tabanlı yönlendirme için tüketici kodu veya Kafka Streams/ksqlDB gerekir.
4. **Sıralama ve ölçek**: Kafka sırayı **bölüm (partition) içinde** garanti eder ve bölüm sayısıyla yatay ölçeklenir. RabbitMQ ise sırayı **tek kuyruk içinde** korur. Aynı kuyruğa birden çok tüketici bağlanınca (**yarışan tüketiciler / competing consumers**) sıkı sıra doğal olarak zayıflar. Ölçek de tüketici sayısıyla ya da kuyruğu parçalara bölerek (sharding) büyütülür. Hiçbiri pratikte sistem genelinde **tek ve toplu bir sıra** vermez.

```svg
<svg viewBox="0 0 660 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="RabbitMQ push ve sil, Kafka pull ve tut karşılaştırması">
  <defs>
    <marker id="rvsk-ok" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki model, iki güç ekseni</text>
  <rect x="24" y="42" width="300" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="174" y="66" text-anchor="middle" font-size="13.5" font-weight="700" fill="#186399" font-family="system-ui">RabbitMQ</text>
  <text x="174" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">push (broker iter)</text>
  <text x="174" y="110" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">tüketilince SİLER</text>
  <text x="174" y="130" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">esnek yönlendirme (exchange)</text>
  <text x="174" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iş kuyruğu, RPC, karmaşık routing</text>
  <text x="174" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">düşük gecikme, esneklik</text>
  <rect x="336" y="42" width="300" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="486" y="66" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">Kafka</text>
  <text x="486" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">pull (tüketici çeker)</text>
  <text x="486" y="110" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">retention boyunca TUTAR (replay)</text>
  <text x="486" y="130" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">partition ile yüksek verim + sıra</text>
  <text x="486" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">olay akışı, log, stream işleme</text>
  <text x="486" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">throughput, replay, ölçek</text>
  <line x1="30" y1="208" x2="630" y2="208" stroke="#5D7789" stroke-width="1" marker-start="url(#rvsk-ok)" marker-end="url(#rvsk-ok)"/>
  <text x="330" y="204" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">karar: "hangisi daha iyi" değil, "hangi iş için"</text>
</svg>
```

İtme ile çekme farkının özü **inisiyatifin kimde olduğudur**. RabbitMQ'da broker iter; ne kadar iteceğine prefetch karar verir. Kafka'da ise tüketici çeker; ne zaman çekeceğine kendisi karar verir.

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="RabbitMQ push modeli broker iter, Kafka pull modeli tüketici çeker">
  <defs>
    <marker id="rvsk-pp" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
    <marker id="rvsk-pp2" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İnisiyatif kimde? push (RabbitMQ) vs pull (Kafka)</text>
  <text x="165" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">RabbitMQ — push</text>
  <rect x="40" y="74" width="110" height="48" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="95" y="103" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Broker</text>
  <rect x="220" y="74" width="110" height="48" rx="9" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="275" y="103" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Tüketici</text>
  <line x1="150" y1="98" x2="216" y2="98" stroke="#186399" stroke-width="2.2" marker-end="url(#rvsk-pp)"/>
  <text x="185" y="90" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">iter</text>
  <text x="185" y="140" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">akış: prefetch tavanı</text>
  <text x="495" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Kafka — pull</text>
  <rect x="370" y="74" width="110" height="48" rx="9" fill="#E9F4EE" stroke="#5D7789" stroke-width="1.2"/>
  <text x="425" y="103" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Broker (log)</text>
  <rect x="550" y="74" width="90" height="48" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="595" y="103" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Tüketici</text>
  <line x1="546" y1="98" x2="482" y2="98" stroke="#25804C" stroke-width="2.2" marker-end="url(#rvsk-pp2)"/>
  <text x="514" y="90" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">poll() çeker</text>
  <text x="505" y="140" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">hız/backpressure tüketicide</text>
  <text x="330" y="184" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">push: düşük gecikme, iş kuyruğu. pull: kendi hızını dayat, toplu (batch) okuma.</text>
</svg>
```

Pratik yüz: **Spring AMQP**, Spring'in RabbitMQ soyutlamasıdır. Tüketici `@RabbitListener` ile tanımlanır, yayım `RabbitTemplate` ile yapılır. Yerel yeniden denemeler tükendiğinde devreye bir **`MessageRecoverer`** girer. Varsayılan `RejectAndDontRequeueRecoverer`, mesajı reddedip (`requeue=false`) kuyruğun DLX'ine bırakır.

Hatanın bağlamını da korumak istiyorsan **`RepublishMessageRecoverer`** kullanılır. Bu sınıf mesajı, istisna türü ve **hata yığın izi (stack trace)** gibi başlıklarla **zenginleştirir**; sonra belirtilen bir DLX/DLQ'ya yeniden yayımlar.

Yanlış alternatifler bellidir. Sonsuz yeniden deneme, zehirli bir mesajla tüketiciyi kilitler. Otomatik onay (autoack) ya da hatayı `try/catch` ile yutmak da mesajı sessizce kaybeder.

```svg
<svg viewBox="0 0 660 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Spring AMQP yerel retry tükenince RepublishMessageRecoverer ile DLQ'ya taşır">
  <defs>
    <marker id="rvsk-sp" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Spring AMQP: retry tükenince kayıpsız DLQ</text>
  <rect x="24" y="66" width="140" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="94" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">@RabbitListener</text>
  <text x="94" y="106" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">işler</text>
  <rect x="200" y="66" width="140" height="52" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="270" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">yerel retry (n kez)</text>
  <text x="270" y="106" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">hata sürüyor</text>
  <rect x="376" y="66" width="150" height="52" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="451" y="86" text-anchor="middle" font-size="10.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">RepublishMessage</text>
  <text x="451" y="102" text-anchor="middle" font-size="10.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Recoverer</text>
  <rect x="556" y="66" width="80" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="596" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">DLQ</text>
  <text x="596" y="106" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">hata başlığı</text>
  <line x1="164" y1="92" x2="196" y2="92" stroke="#5D7789" stroke-width="2" marker-end="url(#rvsk-sp)"/>
  <line x1="340" y1="92" x2="372" y2="92" stroke="#5D7789" stroke-width="2" marker-end="url(#rvsk-sp)"/>
  <text x="358" y="58" text-anchor="middle" font-size="9.5" fill="#8A2E2E" font-family="system-ui">tükendi</text>
  <line x1="526" y1="92" x2="552" y2="92" stroke="#25804C" stroke-width="2" marker-end="url(#rvsk-sp)"/>
  <text x="330" y="160" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Mesaj istisna türü/stack trace başlıklarıyla zenginleşip DLQ'ya yeniden yayımlanır — kayıpsız, incelenebilir.</text>
  <text x="330" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Alternatif: sonsuz retry (poison kilitler) ya da hatayı yutmak (sessiz kayıp) — ikisi de yanlış.</text>
</svg>
```

### Mülakatta ne sorulur?

- "Teslim modeli farkı?" — push (RabbitMQ) ve pull (Kafka).
- "Aynı olayı ikinci bir tüketici baştan okuyabilir mi?" — klasik RabbitMQ kuyruğunda hayır (ack'lenen silinir), Kafka'da evet; RabbitMQ'da çözüm **Streams**.
- "RabbitMQ'yu ne zaman seçersin?" — karmaşık, içerik/desen tabanlı yönlendirme + düşük gecikmeli iş kuyruğu. Uzun retention, sürekli stream işleme ve kaynak-doğruluk olarak log saklama Kafka'nın alanıdır.
- "Retry bitince mesaj nereye?" — `RepublishMessageRecoverer` ile DLQ'ya, kayıpsız.
- "Sıralama ve ölçek?" — Kafka partition içinde sıra + partition ile ölçek; RabbitMQ tek kuyruk içinde sıra + tüketici/sharding ile ölçek. Partition Kafka'nın kavramıdır, RabbitMQ'nun değil.
- "İki ihtiyaç birden varsa?" — her ihtiyacı güçlü olduğu araca ver; tek araca zorlamak bir tarafı zayıf bırakır ve mesajlaşmayı veritabanı tablolarına indirgemek de çözüm değildir.

### Tek cümlede

RabbitMQ iten, yönlendiren ve tükettikçe silen bir iş kuyruğudur. Kafka ise çekilen, saklayan ve tekrar okunan bir kayıt defteridir. Karar "hangisi daha iyi" değil, "hangi iş için doğru araç" sorusudur.

# rvsk-1 | Temel
## Soru
Teslim modeli açısından RabbitMQ ile Kafka'nın temel farkı nedir?
## A
İkisi de pull modeli kullanır; aralarındaki tek fark depolama biçimindedir
🤖 İkisi aynı teslim modelini kullanmaz; RabbitMQ push, Kafka pull'dur.
## B
RabbitMQ push'tur (broker iter); Kafka pull'dur (tüketici poll ile çeker)
🤖 Doğru: push/pull ayrımı iki sistemin temel teslim modeli farkıdır.
## C
RabbitMQ pull modelidir (tüketici çeker); Kafka ise push modelidir (broker iter)
🤖 Tam ters; RabbitMQ iter (push), Kafka çeker (pull).
## D
İkisi de push modeli kullanır; aralarındaki tek fark kullanılan protokoldedir
🤖 Kafka push değildir; tüketici poll ile çeker, bu pull modelidir.
## Doğru
B
## Açıklama
Temel teslim farkı **push ve pull**'dur. RabbitMQ **push** modelidir: broker mesajı tüketiciye **iter**, akış prefetch ile denetlenir — bu düşük gecikme ve klasik iş kuyruğu (work queue) için idealdir. Kafka **pull** modelidir: tüketici `poll()` ile broker'dan **kendi hızında çeker**, bu da tüketicinin geri-basıncı (backpressure) doğal yönetmesini ve toplu (batch) okumayı kolaylaştırır. Bu ayrım depolama (A) veya protokolden (D) bağımsız, mimari bir tercihtir ve yönü karıştırmak (C) klasik bir hatadır.

# rvsk-2 | Orta
## Soru
Bir olay, **bir tüketici tarafından işlendikten sonra** başka bir tüketicinin de aynı olayı **baştan okuyabilmesi** (replay) gereksinimi var. Klasik RabbitMQ kuyruğu ile Kafka bu konuda nasıl ayrışır?
## A
İkisinde de mesaj tüketilince silinir; replay her iki sistemde de imkânsızdır
🤖 Kafka'da mesaj retention boyunca tutulur ve replay edilebilir; ikisi aynı değildir.
## B
İkisinde de mesaj retention boyunca tutulur; replay her ikisinde de mümkündür
🤖 Klasik RabbitMQ kuyruğunda mesaj tüketilince silinir; replay yerleşik değildir.
## C
Klasik RabbitMQ kuyruğu mesajı retention boyunca tutar; Kafka tüketilen mesajı hemen siler
🤖 İki sistemin davranışı ters yazılmış: tutan Kafka, tüketilince silen klasik RabbitMQ kuyruğudur.
## D
Klasik RabbitMQ kuyruğunda mesaj ack'lenince silinir; Kafka retention boyunca tutup replay verir
🤖 Doğru: RabbitMQ tüketilen mesajı siler, Kafka log'da tutar ve offset'ten tekrar okutur.
## Doğru
D
## Açıklama
Ayrım **saklama modelidir**. Klasik (ve quorum) RabbitMQ kuyruğunda mesaj **tüketilip ack'lenince silinir**; aynı mesajı ikinci bir tüketicinin baştan okuması için yerleşik bir yol yoktur. Kafka bir **commit log**'tur: mesaj retention (süre/boyut) sınırına kadar **tutulur**, her tüketici grubu kendi **offset**'ini taşır ve istenen noktadan **replay** yapabilir. C şıkkı bu iki davranışı ters yazdığı için yanlıştır; A ve B ise iki sistemi yanlış biçimde birbirine eşitler. RabbitMQ tarafında replay gereksinimi **Streams** kuyruğu ile karşılanır.

# rvsk-3 | Orta
## Soru
Aşağıdaki gereksinimlerden hangisi **RabbitMQ**'yu Kafka'ya tercih etmek için en güçlü nedendir?
## A
Günde terabaytlarca olayı en yüksek verimle işleyip günlerce saklamak ve tekrar okumak
🤖 Bu tam olarak Kafka'nın güçlü olduğu alandır (yüksek verim + retention + replay), RabbitMQ'yu tercih nedeni değil.
## B
İçerik/desen tabanlı karmaşık yönlendirmeyi broker düzeyinde, kod yazmadan kurmak
🤖 Doğru: esnek yönlendirme (exchange türleri) RabbitMQ'nun Kafka'ya karşı en belirgin üstünlüğüdür.
## C
Olay akışını gerçek-zamanlı stream işleme ile sürekli dönüştürüp yeni akışlar üretmek
🤖 Sürekli stream işleme Kafka (Streams/ksqlDB) ekosisteminin alanıdır; RabbitMQ tercih nedeni değil.
## D
Uzun süreli olay geçmişini sistemin kaynak-doğruluğu (source of truth) olarak saklamak
🤖 Uzun süreli log/kaynak-doğruluk saklama Kafka'nın modelidir; RabbitMQ klasik kuyruğu bunun için uygun değil.
## Doğru
B
## Açıklama
RabbitMQ'yu seçmenin en güçlü nedeni **esnek yönlendirmedir**: **topic** ve **headers** exchange'leriyle "şu desene uyan veya şu başlığı taşıyan mesajı şu kuyruğa" gibi kararlar **broker düzeyinde, bildirimsel** olarak kurulur ve sonradan uygulama kodu değiştirilmeden yeniden yapılandırılır (ör. `log.*.error` deseni). Diğer şıklar Kafka'nın güçlü olduğu alanları tarif eder: çok yüksek verim + uzun retention + replay (A), sürekli **stream işleme** (C) ve **kaynak-doğruluk** olarak uzun log saklama (D). Karar "hangisi daha iyi" değil "hangi iş için doğru araç" sorusudur; karmaşık yönlendirme + iş kuyruğu + düşük gecikme → RabbitMQ.

# rvsk-4 | İleri
## Soru
Spring AMQP'de bir `@RabbitListener` tüketicisi, yerel yeniden denemeler tükendikten sonra mesajı **kaybetmeden**, hata bilgisiyle birlikte bir **DLQ**'ya taşımak istiyor. En uygun bileşen hangisidir?
## A
`SimpleRetryPolicy` ile deneme sayısını sonsuza ayarlayıp hata geçene kadar denemek
🤖 Sonsuz retry mesajı DLQ'ya taşımaz; poison message tüketiciyi süresiz kilitler.
## B
`RepublishMessageRecoverer` kullanmak; mesajı hata başlıklarıyla DLQ'ya yeniden yayımlar
🤖 Doğru: RepublishMessageRecoverer tam olarak "retry bitince DLQ'ya, kayıpsız" senaryosu içindir.
## C
autoack'i açıp hata durumunda mesajın broker tarafından temizlenmesini sağlamak
🤖 autoack hata durumunda mesajı sessizce kaybeder; DLQ'ya güvenli taşıma sağlamaz.
## D
Tüketicide try/catch ile hatayı yutup (swallow) yalnızca log dosyasına yazmak
🤖 Hatayı yutmak mesajı ack'ler ve etkin biçimde kaybeder; DLQ'ya gitmez.
## Doğru
B
## Açıklama
Spring AMQP'de yerel yeniden denemeler tükendikten sonra devreye giren şey bir **`MessageRecoverer`**'dır. Varsayılan `RejectAndDontRequeueRecoverer` mesajı reddedip (`requeue=false`) kuyruğun DLX'ine bırakır; ancak **hata bağlamını korumak** isteniyorsa **`RepublishMessageRecoverer`** tercih edilir: mesajı **istisna türü, stack trace gibi başlıklarla zenginleştirip** belirtilen bir DLX/DLQ'ya yeniden yayımlar — böylece mesaj kaybolmaz ve DLQ'da neden başarısız olduğu görülebilir. Sonsuz retry (A) poison message'ı çözmez, tüketiciyi kilitler; autoack (C) ve hatayı yutmak (D) mesajı sessizce kaybeder. Desen: sınırlı retry + `RepublishMessageRecoverer` + izlenen bir DLQ.

# rvsk-5 | İleri
## Soru
Sıralama (ordering) ve yatay ölçek açısından Kafka ile RabbitMQ'yu karşılaştıran hangi ifade **en doğrudur**?
## A
Kafka hiçbir sıralama garantisi vermez; RabbitMQ ise tüm kuyruklarda global sıra verir
🤖 Kafka partition İÇİNDE güçlü sıra verir; RabbitMQ ise global sıra garanti etmez.
## B
İkisi de tüm sistem genelinde global toplam sıra (total order) garanti eder
🤖 Hiçbiri pratikte global total order vermez; Kafka partition içi, RabbitMQ tek kuyruk içi sıradadır.
## C
RabbitMQ partition'larla yatay ölçeklenir; Kafka ise yatay olarak hiç ölçeklenmez
🤖 Partition Kafka'nın kavramıdır ve Kafka'yı ölçekler; ifade tersine kurulmuş, yanlıştır.
## D
Kafka sırayı partition içinde garanti eder; RabbitMQ tek kuyruk içinde sırayı korur
🤖 Doğru: her iki sistemin sıra garantisi bu düzeyde tanımlıdır, sistem genelinde değil.
## Doğru
D
## Açıklama
Kafka sırayı **partition içinde** garanti eder (aynı anahtara giden mesajlar aynı partition'da sıralı kalır) ve **partition sayısını artırarak** yatay ölçeklenir — paralellik partition sayısına bağlıdır. RabbitMQ **tek bir kuyruk içinde** teslim sırasını korur; ancak aynı kuyruğa birden çok tüketici bağlanıp paralel çekince (competing consumers) sıkı sıralama doğal olarak zayıflar. RabbitMQ ölçeği genelde tüketici sayısı, ayrı kuyruklar veya uygulama düzeyi sharding ile büyütülür. A Kafka'nın sırasını inkâr eder, B ikisine de olmayan bir global total order atfeder, C ise partition kavramını yanlış sisteme yükler — üçü de yanlıştır.

# rvsk-6 | İleri
## Soru
Bir ekip mimari karar veriyor: (1) mikroservisler arası **komut/iş kuyruğu** (düşük gecikme, karmaşık yönlendirme, tüketilince biten mesajlar) ve ayrıca (2) tüm sistemin **olay geçmişini** uzun süre saklayıp analitik/yeniden-oynatma için kullanmak. En sağlıklı yaklaşım nedir?
## A
Her şeyi tek bir Kafka kümesiyle yapmak; RabbitMQ'nun sunduğu her şeyi Kafka zaten karşılar
🤖 Kafka karmaşık broker-düzeyi yönlendirme ve düşük gecikmeli iş kuyruğunda RabbitMQ kadar rahat değildir.
## B
İki ihtiyacı ayrı araçla karşılamak: iş kuyruğu için RabbitMQ, uzun log/replay için Kafka
🤖 Doğru: iki farklı gereksinim iki farklı güçlü tarafa denk gelir; araçları işe göre eşlemek en sağlıklısıdır.
## C
Her şeyi tek bir RabbitMQ klasik kuyruğuyla yapmak; retention'ı çok uzun tutmak yeterlidir
🤖 Klasik RabbitMQ kuyruğu tüketilince siler ve uzun log/replay için tasarlanmamıştır; (2). ihtiyacı karşılamaz.
## D
Her iki ihtiyaç için ilişkisel veritabanı tabloları kullanıp mesajlaşmayı tamamen bırakmak
🤖 Veritabanı tabloları asenkron mesajlaşmanın (push/pull, yönlendirme, ölçek) yerini tutmaz; problemi çözmez.
## Doğru
B
## Açıklama
En sağlıklı yaklaşım **her iki gereksinimi güçlü olduğu araçla eşlemektir**. (1) Düşük gecikmeli **iş/komut kuyruğu** ve **karmaşık yönlendirme** RabbitMQ'nun güçlü alanıdır (exchange türleri, push, tüketilince-biten mesajlar). (2) Uzun **retention + replay + analitik olay log'u** ise Kafka'nın (veya RabbitMQ **Streams**'in) alanıdır. Bu iki ihtiyacı tek araca zorlamak (A: her şey Kafka; C: her şey klasik RabbitMQ) her zaman bir tarafı zayıf bırakır — Kafka broker-düzeyi karmaşık yönlendirmede, klasik RabbitMQ kuyruğu uzun log/replay'de zorlanır. Mesajlaşmayı tamamen veritabanına indirgemek (D) asenkron modelin (yönlendirme, geri-basınç, ölçek) sağladıklarını kaybettirir. İlke: **"hangisi daha iyi" değil, "hangi iş için doğru araç".**
