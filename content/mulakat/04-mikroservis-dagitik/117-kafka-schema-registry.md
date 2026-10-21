---
slug: kafka-schema-registry
baslik: "Schema Registry: Avro/Protobuf, Wire Format ve Uyumluluk Modları"
kademe: Orta
sira: 117
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Kafka'da mesaj yazan program ile okuyan program birbirini hiç tanımaz. Çoğu zaman farklı ekiplerin, farklı dillerde yazdığı, farklı zamanlarda güncellenen uygulamalardır. Aralarındaki tek anlaşma, mesajın **hangi alanlardan oluştuğudur**. Bu anlaşma yazılı ve denetlenir olmazsa şu olur: biri bir alanın adını değiştirdiği gün diğerinin kodu sessizce bozulur. **Schema Registry** tam olarak bu anlaşmayı saklayan ve bozulmasına izin vermeyen servistir.

### Temel kavramlar

- **Şema (schema):** bir mesajın hangi alanları, hangi tiplerde taşıdığını anlatan tarif. Örneğin "müşteri: ad (metin), yaş (tam sayı)". Sözleşmenin (contract) kendisidir.
- **Serileştirme (serialization):** nesneyi ağdan geçebilecek **bayt dizisine** çevirmek. Tersi **deserialization**.
- **Schema Registry:** şemaları merkezî bir sunucuda saklayan ve her şemaya benzersiz bir **schema id** veren servis (referans uygulama: Confluent Schema Registry).
- **Wire format (tel formatı):** mesaj baytlarının kablo üzerinde hangi düzende dizildiği.
- **Gövde (payload):** mesajın asıl veri kısmı, yani çerçeve baytları çıkarıldıktan sonra kalan içerik.
- **Subject (özne):** registry'de bir şemanın sürüm geçmişinin tutulduğu isim; uyumluluk kontrolleri subject bazında yapılır.
- **Şema evrimi (schema evolution):** şemanın zamanla değişmesidir; alan eklenir, alan silinir, tip değişir.
- **Uyumluluk modu (compatibility mode):** bu değişimin hangi kuralla denetleneceğini belirler.

### Nasıl çalışır?

Temel fikir **kimlikle referans vermektir**. Üretici (producer) tam şemayı her mesajın içine gömmez. Şemayı **bir kez** registry'ye kaydeder ve registry ona benzersiz bir **schema id** (şema kimliği) döner. Üretici de mesaja yalnız bu kimliği iliştirir. Tüketici (consumer) mesajdaki kimliği okur, şemayı registry'den çeker ve baytları buna göre çözer.

Kazanç büyüktür. Tam şema kilobaytlarca yer tutabilir; kimlik ise yalnız 4 bayttır. Milyonlarca mesajda bu, ciddi bir bant genişliği ve depolama tasarrufu demektir. Ayrıca üretici ile tüketici birbirine hiç doğrudan bağlanmaz. Şema mesajın içinde ham hâlde geçmez; TCP ile karşı tarafa elden de gönderilmez.

Confluent'in serileştiricileri hepsinde aynı **wire format** çerçevesini kullanır. Çerçeve şu üç parçadan oluşur: **1 sihirli bayt (magic byte, `0x00`)**, **4 bayt uzunluğunda ve büyük-uçlu (big-endian) yazılmış schema id**, ardından serileştirilmiş **gövde**.

Buradan mülakatta çok sorulan bir arıza doğar. Ham baytları, Confluent'ten habersiz ("çıplak" / vanilla) bir Avro deserializer'a verdiğinizi düşünün. O deserializer baştaki **5 baytlık çerçeveyi** de veri sanır. Sonuç olarak şemayla hizalanamaz ve çıktıyı bozar. Sorunun sebebi ne şifreleme, ne format uyumsuzluğu, ne de bellek taşmasıdır; sebep, çerçevenin atlanmamış olmasıdır. Doğru çözüm Confluent deserializer kullanmaktır: o önce kimliği okur, şemayı çeker ve gövdeyi kalan baytlardan çözer.

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Confluent wire format ve producer/consumer'ın Schema Registry ile şema id alışverişi">
  <defs>
    <marker id="kschema-ok" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto" markerUnits="strokeWidth">
      <path d="M0,0 L7,3 L0,6 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Şema id ile referans, payload ile veri</text>

  <rect x="24" y="44" width="150" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="99" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Producer</text>
  <text x="99" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">şemayı kaydeder,</text>
  <text x="99" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">id'yi gömer</text>

  <rect x="245" y="44" width="150" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Schema Registry</text>
  <text x="320" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">id 42 → Avro şema</text>
  <text x="320" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">uyumluluk kontrolü</text>

  <rect x="466" y="44" width="150" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="541" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Consumer</text>
  <text x="541" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">id'yle şemayı çeker,</text>
  <text x="541" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">deserialize eder</text>

  <line x1="174" y1="66" x2="243" y2="66" stroke="#186399" stroke-width="1.5" marker-end="url(#kschema-ok)"/>
  <text x="208" y="60" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">register</text>
  <line x1="243" y1="92" x2="176" y2="92" stroke="#186399" stroke-width="1.5" marker-end="url(#kschema-ok)"/>
  <text x="208" y="106" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">id = 42</text>

  <line x1="466" y1="92" x2="397" y2="92" stroke="#186399" stroke-width="1.5" marker-end="url(#kschema-ok)"/>
  <text x="431" y="106" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">id 42 sorgu</text>
  <line x1="395" y1="66" x2="464" y2="66" stroke="#186399" stroke-width="1.5" marker-end="url(#kschema-ok)"/>
  <text x="431" y="60" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">şema</text>

  <text x="320" y="168" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C3247" font-family="system-ui">Wire format (mesajın baytları)</text>
  <rect x="70" y="184" width="90" height="52" rx="8" fill="#186399" stroke="#1C3247" stroke-width="1.5"/>
  <text x="115" y="208" text-anchor="middle" font-size="12" font-weight="700" fill="#EAF1F7" font-family="system-ui">0x00</text>
  <text x="115" y="224" text-anchor="middle" font-size="9.5" fill="#EAF1F7" font-family="system-ui">1 magic byte</text>

  <rect x="168" y="184" width="130" height="52" rx="8" fill="#25804C" stroke="#1C3247" stroke-width="1.5"/>
  <text x="233" y="208" text-anchor="middle" font-size="12" font-weight="700" fill="#E9F4EE" font-family="system-ui">schema id</text>
  <text x="233" y="224" text-anchor="middle" font-size="9.5" fill="#E9F4EE" font-family="system-ui">4 byte big-endian</text>

  <rect x="306" y="184" width="264" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="438" y="208" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">serialized payload</text>
  <text x="438" y="224" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Avro / Protobuf / JSON Schema ikili gövde</text>

  <text x="320" y="266" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">İlk 5 bayt çerçevedir: çıplak deserializer bunu payload sanıp veriyi bozar.</text>
  <text x="320" y="284" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Consumer önce id'yi okur, sonra doğru şemayla payload'ı çözer.</text>
</svg>
```

**Format seçimi** bir denge işidir. **Avro** küçük boyutlu ikili çıktı üretir, şemayı veriden ayrı tutar ve Kafka dünyasında en yaygın olanıdır. **Protobuf** güçlü bir arayüz tanım dili (IDL) ve olgun bir çok-dilli araç zinciri sunar; özellikle gRPC kullanan sistemlerde öne çıkar. **JSON Schema** insan gözüyle okunabilir olmasıyla iyidir. Ama metin tabanlı olduğu için kablo üzerinde en **şişkin (verbose)** seçenektir. Registry üçünü de destekler. Bu yüzden "yalnız Avro çalışır" veya "üçü aynı çıktıyı verir, fark yalnız uzantıdır" iddiaları yanlıştır.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Serileştirme formatı dengesi: Avro kompakt, Protobuf çok-dilli IDL, JSON Schema okunur ama şişkin">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Format dengesi: tel üzerindeki boyut</text>

  <text x="30" y="68" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Avro</text>
  <rect x="120" y="56" width="140" height="20" rx="4" fill="#25804C"/>
  <text x="272" y="71" font-size="10.5" fill="#5D7789" font-family="system-ui">kompakt ikili · Kafka'da en yaygın</text>

  <text x="30" y="114" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Protobuf</text>
  <rect x="120" y="102" width="180" height="20" rx="4" fill="#186399"/>
  <text x="312" y="117" font-size="10.5" fill="#5D7789" font-family="system-ui">güçlü IDL · çok-dilli · gRPC</text>

  <text x="30" y="160" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">JSON</text>
  <rect x="120" y="148" width="360" height="20" rx="4" fill="#B26B00"/>
  <text x="492" y="163" font-size="10.5" fill="#5D7789" font-family="system-ui">okunur ama şişkin</text>

  <text x="120" y="200" font-size="10.5" fill="#5D7789" font-family="system-ui">← daha kompakt          daha şişkin →</text>
  <text x="320" y="230" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Confluent Schema Registry üçünü de destekler; seçim kompaktlık/okunabilirlik/dil dengesi.</text>
</svg>
```

**Şema evrimi ve uyumluluk** bu sistemin asıl gücüdür. Yeni bir şema kaydedilirken registry onu eskisiyle kıyaslar ve sözleşmeyi bozacak değişikliği **reddeder**:

- **BACKWARD (geriye dönük uyum):** yeni şema **eski veriyi** okuyabilmelidir. Bu yüzden güvenli dağıtım sırası **önce tüketicidir**. Tüketiciler yeni şemaya geçer ve hâlâ akan eski mesajları sorunsuz okur; üreticiler sonra geçer.
- **FORWARD (ileriye dönük uyum):** eski şema **yeni veriyi** okuyabilmelidir. Bu yüzden sıra terstir: **önce üretici**.
- **FULL:** ikisini birden ister, her iki yön de güvenlidir.
- **NONE:** hiç kontrol etmez; sözleşme sessizce bozulabilir.

Her modun bir de **TRANSITIVE** (geçişli) varyantı vardır. Normal mod yalnız bir önceki sürümle kıyaslar; geçişli varyant **tüm geçmişi** kapsar. Bir yanlış anlamayı düzeltelim: BACKWARD "yalnız arşivleme" demek değildir. Üretici ile tüketicinin aynı anda güncellenmesini de gerektirmez. Tam tersine amacı, kesintisiz ve sırayla güncellemeye izin vermektir.

```svg
<svg viewBox="0 0 640 260" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Uyumluluk modları: BACKWARD önce tüketici, FORWARD önce üretici, FULL ikisi, NONE kontrolsüz">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Uyumluluk modları ve dağıtım sırası</text>

  <rect x="20" y="44" width="290" height="90" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="36" y="66" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">BACKWARD</text>
  <text x="36" y="88" font-size="10.5" fill="#1C3247" font-family="system-ui">yeni şema eski veriyi okur</text>
  <text x="36" y="108" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">→ önce TÜKETİCİYİ güncelle</text>
  <text x="36" y="126" font-size="10" fill="#5D7789" font-family="system-ui">alan eklerken default şart</text>

  <rect x="330" y="44" width="290" height="90" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="346" y="66" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">FORWARD</text>
  <text x="346" y="88" font-size="10.5" fill="#1C3247" font-family="system-ui">eski şema yeni veriyi okur</text>
  <text x="346" y="108" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">→ önce ÜRETİCİYİ güncelle</text>
  <text x="346" y="126" font-size="10" fill="#5D7789" font-family="system-ui">alan silerken güvenli yön</text>

  <rect x="20" y="146" width="290" height="76" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="36" y="168" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">FULL</text>
  <text x="36" y="190" font-size="10.5" fill="#1C3247" font-family="system-ui">BACKWARD + FORWARD birden</text>
  <text x="36" y="210" font-size="10.5" fill="#5D7789" font-family="system-ui">her iki yön de güvenli</text>

  <rect x="330" y="146" width="290" height="76" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="346" y="168" font-size="12.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">NONE</text>
  <text x="346" y="190" font-size="10.5" fill="#1C3247" font-family="system-ui">hiç kontrol etmez</text>
  <text x="346" y="210" font-size="10.5" fill="#8A2E2E" font-family="system-ui">sözleşme bozulabilir — riskli</text>

  <text x="320" y="244" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Her modun tüm geçmişi kapsayan bir TRANSITIVE varyantı vardır.</text>
</svg>
```

Somut kural şudur: **BACKWARD modda yeni alan eklerken o alanın bir `default` (varsayılan) değeri olmalıdır.** Sebebi basittir. Eski kayıtlarda o alan fiziksel olarak yoktur. Yeni şemayla okurken bu boşluğu bir yerden doldurmak gerekir; varsayılan değer tam olarak bunu yapar. Varsayılansız zorunlu (`required`) bir alan eklerseniz eski veri okunamaz ve registry değişikliği reddeder.

Alan **silmek** BACKWARD uyumunu bozabilir, çünkü eski üretici o alanı hâlâ gönderiyor olabilir. Alan adını değiştirmek de risklidir; çünkü sistem bunu "sil ve ekle" gibi görür ve iki yönü birden etkiler. Buna karşılık alan adının büyük ya da küçük harfle başlaması uyumlulukla ilgili değildir.

Son konu **subject adlandırma stratejisidir**. Varsayılan olan **`TopicNameStrategy`**, subject adını topic adından türetir (`<topic>-value`). Yani bir topic tek bir şema ailesi taşır; tek tip mesaj akan topic'ler için en sade seçenektir.

**`RecordNameStrategy`** ise subject adını kaydın tam nitelikli adından türetir. Böylece tek bir topic'te **birden çok kayıt tipi** akabilir ve her tip kendi uyumluluk geçmişine sahip olur. `TopicRecordNameStrategy` bu ikisini birleştirir. Strateji seçimi formattan bağımsızdır ve her ikisi de şemaları normal biçimde kaydeder.

### Mülakatta ne sorulur?

Üretici şemayla ne yapar (kimliği gömer)? Avro, Protobuf ve JSON Schema arasındaki denge nedir? Çıplak deserializer neden bozulur (5 baytlık çerçeve)? BACKWARD neyi garanti eder ve dağıtım sırasını nasıl belirler? Yeni alan eklerken kural nedir (varsayılan değer)? `TopicNameStrategy` ile `RecordNameStrategy` arasındaki fark nedir?

### Tek cümlede

Schema Registry şemayı merkezde tutup mesaja yalnız **id**'sini gömdürür ve uyumluluk moduyla şema evrimini denetler; **BACKWARD → önce tüketici**, **FORWARD → önce üretici**.

# kschema-1 | Temel
## Soru
Confluent Schema Registry kullanan bir kurulumda, üretici (producer) bir mesajı Kafka'ya yazarken şemayla ilgili ne yapar?
## A
Şemayı registry'ye kaydeder, dönen schema id'yi alır ve tam şema yerine bu id'yi mesaja gömer
🤖 Doğru: id ile referans, tam şemayı her mesaja gömmeden bant genişliği tasarrufu sağlar.
## B
Tam Avro şemasını JSON olarak her mesajın başına ekler; tüketici de bunu okuyup çözer
🤖 Amaç tam olarak bunu yapmamak; şema her mesaja gömülseydi registry gereksiz olur ve bant genişliği israf olurdu.
## C
Şemayı tüketiciye doğrudan TCP bağlantısıyla gönderir; registry yalnızca yedek kopya tutar
🤖 Üretici ile tüketici birbirini tanımaz; sözleşme registry üzerinden id ile paylaşılır, doğrudan bağlantı yoktur.
## D
Şemayı hiç kullanmaz; Kafka baytları ham geçirir ve hiçbir tip bilgisi tutmaz
🤖 Serializer şemayı kullanır ve id'yi gömer; ham geçiş olsaydı uyumluluk denetimi de mümkün olmazdı.
## Doğru
A
## Açıklama
Schema Registry'nin temel fikri **id ile referans**tır: üretici şemayı bir kez registry'ye kaydeder (register), registry benzersiz bir **schema id** döner, üretici de tam şema yerine bu id'yi mesaja gömer. Böylece her mesaj küçük kalır (tam şema kilobaytlarca yer tutabilir) ve üretici ile tüketici hiç doğrudan konuşmadan aynı sözleşmeyi paylaşır. Tüketici mesajdaki id'yi okuyup şemayı registry'den çeker.

# kschema-2 | Orta
## Soru
Bir ekip serileştirme formatı seçiyor. Avro, Protobuf ve JSON Schema arasındaki denge (trade-off) için aşağıdakilerden hangisi doğrudur?
## A
JSON Schema tel üzerinde (on the wire) en kompakt olandır; Avro ise en şişkin çıktıyı üretir
🤖 Tam tersi: JSON Schema metin tabanlı olduğu için en şişkindir, Avro ikili ve kompakttır.
## B
Protobuf şema kavramını desteklemez; Schema Registry ile yalnızca Avro çalışabilir
🤖 Confluent Schema Registry Avro, Protobuf ve JSON Schema'nın üçünü de destekler.
## C
Üç format da aynı ikili çıktıyı üretir; aralarındaki tek fark dosya uzantısıdır
🤖 Çıktılar ve tasarım felsefeleri farklıdır; fark yalnızca uzantı değildir.
## D
Avro kompakt ve Kafka'da en yaygın; Protobuf güçlü IDL ve çok-dilli; JSON Schema okunur ama şişkin
🤖 Doğru: üç formatın klasik denge tablosu tam olarak budur.
## Doğru
D
## Açıklama
**Avro** kompakt ikili çıktı üretir, şemayı veriden ayrı tutar ve Kafka ekosisteminde en yaygın olandır. **Protobuf** güçlü bir IDL (interface definition language) ve olgun çok-dilli (polyglot) araç zinciriyle, özellikle gRPC kullanan sistemlerde tercih edilir. **JSON Schema** insan tarafından okunabilir olması avantajıyla gelir ama metin tabanlı olduğu için tel üzerinde en şişkin (verbose) formattır. Confluent Schema Registry üçünü de destekler; seçim, kompaktlık, okunabilirlik ve dil ekosistemi arasındaki dengeye göre yapılır.

# kschema-3 | Orta
## Soru
Bir geliştirici, Confluent serializer ile yazılmış Kafka mesajlarını **çıplak** (vanilla) bir Avro deserializer'a verince veri bozuluyor. En olası neden nedir?
## A
Avro formatı Kafka ile uyumsuzdur; mesajları çözmeden önce JSON'a çevirmek gerekir
🤖 Avro Kafka'da yaygın çalışır; sorun format uyumsuzluğu değil, mesajın önündeki çerçevedir.
## B
Mesajın başındaki 5 baytlık magic byte + schema id çerçevesini deserializer payload sanar
🤖 Doğru: ilk 5 bayt çerçevedir, önce atlanıp id okunmalıdır.
## C
Schema Registry mesajı şifreler ve çıplak deserializer'ın çözme anahtarı hiç bulunmaz
🤖 Registry şifreleme yapmaz; sorun şifre değil, wire format çerçevesidir.
## D
Avro şeması çok büyük olduğu için deserializer okuma sırasında belleği taşırır
🤖 Şema mesaja gömülmez (yalnız id gömülür); bellek taşması bu senaryonun nedeni değildir.
## Doğru
B
## Açıklama
Confluent serializer'ları her mesajın başına sabit bir **wire format** çerçevesi koyar: **1 magic byte (0x00)** + **4 byte big-endian schema id**, ardından serileştirilmiş **payload** gelir. Çıplak (Confluent-farkında olmayan) bir Avro deserializer bu ilk 5 baytı da payload'ın parçası sanır, şemayla hizalanamaz ve veriyi bozar. Doğru çözüm Confluent deserializer kullanmaktır: o, önce id'yi okur, registry'den ilgili şemayı çeker ve payload'ı geri kalan baytlardan doğru çözer.

# kschema-4 | İleri
## Soru
Uyumluluk modu **BACKWARD** olarak ayarlı. Bu ne garanti eder ve dağıtım (deployment) sırasını nasıl belirler?
## A
Eski şema (old) yeni veriyi okuyabilir; bu yüzden önce **üreticiyi** (producer) güncellemelisin
🤖 Bu FORWARD modunun tanımı; BACKWARD'da güncellenen yeni şema eski veriyi okur ve önce tüketici güncellenir.
## B
Yeni şema (new) eski veriyi okuyabilir; bu yüzden önce **tüketiciyi** (consumer) güncellemelisin
🤖 Doğru: BACKWARD yeni şemanın eski veriyi okumasını garanti eder, güvenli sıra önce tüketicidir.
## C
Üretici ve tüketici tam olarak aynı anda güncellenmelidir; dağıtım sırası hiç fark etmez
🤖 BACKWARD tam da sıralı, kesintisiz güncellemeye izin verir; eşzamanlılık şartı yoktur.
## D
Hiçbir uyumluluk garantisi yoktur; BACKWARD yalnızca şemaların eski sürümlerini arşivler
🤖 Garantisiz mod NONE'dır; BACKWARD açık bir okunabilirlik garantisi verir.
## Doğru
B
## Açıklama
**BACKWARD** uyumluluk, yeni şemayla donatılmış bir tüketicinin **eski şemayla yazılmış verileri** de okuyabilmesini garanti eder. Bu yüzden güvenli dağıtım sırası **önce tüketiciyi güncellemektir**: tüketiciler yeni şemaya geçtiğinde hâlâ akan eski mesajları sorunsuz okurlar, üreticiler sonra geçebilir. Ayna simetri **FORWARD**'dadır: eski şema yeni veriyi okuyabilir, bu yüzden önce üretici güncellenir. **FULL** ikisini birden ister, **NONE** hiç kontrol etmez; her modun ayrıca tüm geçmiş sürümleri kapsayan bir **TRANSITIVE** varyantı vardır.

# kschema-5 | İleri
## Soru
BACKWARD uyumlu bir şemaya **yeni bir alan** (field) eklemek istiyorsun. Kural nedir?
## A
Yeni alan eklemek her zaman BACKWARD'ı bozar; alan eklemek yerine yeni bir topic açmalısın
🤖 Alan eklemek default ile BACKWARD-uyumludur; yeni topic gerekmez.
## B
Alanı `required` (zorunlu, default'suz) tanımlaman gerekir; default vermek uyumluluğu bozar
🤖 Tam tersi: default'suz zorunlu alan eski veride okunamaz ve BACKWARD'ı bozar.
## C
Alan adının büyük harfle başlaması yeterlidir; default değerinin uyumlulukla bir ilgisi yoktur
🤖 İsimlendirme biçimi uyumlulukla ilgisiz; belirleyici olan default değeridir.
## D
Yeni alanın bir default değeri olmalıdır; eski kayıtlardaki boşluk okunurken bu değerle doldurulur
🤖 Doğru: default olmadan eski veride eksik alan okunamaz, BACKWARD bozulur.
## Doğru
D
## Açıklama
BACKWARD modda yeni şema eski veriyi okuyabilmelidir. Eski kayıtlarda yeni alan **fiziksel olarak yoktur**; yeni şema onları okurken o alanı bir yerden doldurmak zorundadır. Bu yüzden eklenen alanın bir **default** değeri olması şarttır — okuyucu eksik alanı default ile tamamlar ve BACKWARD korunur. Default'suz zorunlu (required) bir alan eklersen eski veri okunamaz ve uyumluluk kontrolü reddeder. Buna karşılık alan **silmek** BACKWARD'ı bozabilir (eski üretici silinen alanı hâlâ göndermek isteyebilir); yeniden adlandırma (rename) ise sil+ekle gibi davranıp iki yönden de risk taşır.

# kschema-6 | Orta
## Soru
**Subject naming strategy** (konu/özne adlandırma stratejisi) ile ilgili olarak `TopicNameStrategy` ve `RecordNameStrategy` arasındaki fark nedir?
## A
`TopicNameStrategy` subject'i topic adından, `RecordNameStrategy` kaydın tam adından türetir; ikincisiyle topic çok tipli olabilir
🤖 Doğru: biri topic eksenli, diğeri record-tipi eksenli subject üretir.
## B
İkisi de aynı şeydir; yalnızca isimleri farklıdır, ürettikleri subject ve davranış tümüyle özdeştir
🤖 Davranışları farklıdır: subject'in neye (topic'e mi record tipine mi) bağlandığı değişir.
## C
`RecordNameStrategy` şemaları hiç kaydetmez; yalnızca topic silindiğinde temizlik için devreye girer
🤖 RecordNameStrategy de şemaları normal kaydeder; sadece subject adını record adından türetir.
## D
`TopicNameStrategy` yalnızca JSON Schema ile, `RecordNameStrategy` yalnızca Avro ile kullanılabilir
🤖 Strateji seçimi formattan bağımsızdır; her ikisi de üç formatla kullanılabilir.
## Doğru
A
## Açıklama
**Subject**, registry'de bir şemanın sürüm geçmişinin tutulduğu isim alanıdır (namespace) ve uyumluluk kontrolleri subject bazında yapılır. **`TopicNameStrategy`** (varsayılan) subject'i topic adından türetir (ör. `<topic>-value`), yani bir topic tek bir şema ailesi taşır — homojen topic'ler için sadedir. **`RecordNameStrategy`** ise subject'i kaydın tam nitelikli adından (fully-qualified record name) türetir; böylece tek bir topic'te **birden çok farklı kayıt tipi** birlikte akabilir ve her tip kendi uyumluluk geçmişine sahip olur. Olay-güdümlü (event-driven), heterojen topic tasarımlarında ikincisi tercih edilir; `TopicRecordNameStrategy` ikisini birleştirir.
