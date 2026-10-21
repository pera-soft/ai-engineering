---
slug: mcp-protokol
baslik: "Model Context Protocol (MCP): Mimari, Tool/Resource/Prompt, İstemci-Sunucu ve Güvenlik Sınırı"
kademe: İleri
sira: 180
etiketler: 7
---

## Konu anlatımı

### Bu ne işe yarar?

Eskiden her cihazın kendi şarj kablosu vardı: telefon başka, kamera başka, yazıcı başka. USB-C çıkınca tek bir fiş herkese uydu. Yapay zekâ uygulamaları da aynı dertteydi: her sohbet uygulamasını her araca (dosya sistemi, veritabanı, takvim, hata takip sistemi) bağlamak için ayrı ayrı "yapıştırma kod" yazılıyordu. **MCP (Model Context Protocol / Model Bağlam Protokolü)**, bu bağlantı için ortak bir konuşma dili tanımlar — kısacası **"yapay zekâ uygulamaları için USB-C"**. Bir aracı bir kez MCP'ye uygun yazarsın, MCP konuşan her uygulamayla çalışır.

### Temel kavramlar

- **Protokol**: İki programın birbirini anlaması için önceden anlaşılmış mesaj kuralları. HTTP tarayıcı ile sunucu arasında ne ise, MCP de yapay zekâ uygulaması ile yetenek sağlayıcı arasında odur.
- **Büyük dil modeli (LLM)**: Metnin devamını üreten model. Kendi başına dosya okuyamaz, e-posta gönderemez; ancak **araç** verilirse dış dünyaya dokunabilir.
- **Araç çağrısı (function calling / tool call)**: Modelin "şu aracı, şu argümanlarla çağırmak istiyorum" kararını yapısal bir çıktı olarak üretmesi. Kararı model verir, çağrıyı uygulama yapar.
- **Host (ana uygulama)**: Kullanıcının kullandığı yapay zekâ uygulaması (IDE, sohbet istemcisi). Modeli barındırır ve kullanıcı onaylarını yönetir.
- **Client (istemci)**: Host'un içinde yaşayan, **her sunucuya bire-bir** bağlanıp protokol konuşmasını yürüten bileşen.
- **Server (sunucu)**: Yetenekleri dışa açan bağımsız süreç (dosya sistemi sunucusu, veritabanı sunucusu…).
- **JSON-RPC 2.0**: Mesajların biçimi — "hangi metot, hangi parametreler, hangi cevap" formatını tanımlayan basit bir standart.
- **Taşıma (transport)**: Mesajların hangi kanaldan aktığı. **stdio** yerel süreç boruları, **streamable HTTP** ise ağ üzerinden uzak sunucular içindir.
- **Ajan (agent)**: Araç kullanan, kendi adımını kendi planlayan yapı. Düşünür, aracı çağırır, sonucu görür ve döngüyü tekrarlar.

### Nasıl çalışır?

1. Host açılırken her yapılandırılmış sunucu için bir client başlatır ve **el sıkışma** yapılır.
2. Client sunucuya "neler sunuyorsun?" diye sorar; sunucu **tool/resource/prompt** listesini şemalarıyla döndürür (keşif).
3. Bu tanımlar modele sunulur. Model bir işi yapmak için **araç çağrısı** üretir: "şu aracı şu argümanlarla çağır" der.
4. Host gerekiyorsa **kullanıcı onayı** ister, client çağrıyı JSON-RPC mesajı olarak sunucuya iletir.
5. Sunucu işi yapar, sonucu döndürür; sonuç modelin bağlamına girer ve döngü sürer.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="MCP host-client-server mimarisi ve güven sınırı">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">MCP: Host içinde Client, karşıda Server</text>
  <rect x="26" y="46" width="250" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="151" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Host (AI uygulaması + model)</text>
  <rect x="50" y="86" width="202" height="46" rx="9" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="151" y="106" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Client (1:1 bağlantı)</text>
  <text x="151" y="123" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">JSON-RPC 2.0</text>
  <text x="151" y="160" text-anchor="middle" font-size="11" fill="#B23B3B" font-family="system-ui">kullanıcı onayı burada</text>
  <text x="151" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yan etkili araçlar denetlenir</text>
  <path d="M278 121 L360 121" stroke="#5D7789" stroke-width="2" fill="none" stroke-dasharray="5 4"/>
  <text x="319" y="112" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">güven sınırı</text>
  <path d="M360 121 l-10 -5 l0 10 z" fill="#5D7789"/>
  <rect x="364" y="46" width="250" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="489" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Server (yetenek sunar)</text>
  <rect x="382" y="84" width="214" height="26" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="489" y="101" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Tools — model çağırır (eylem)</text>
  <rect x="382" y="116" width="214" height="26" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="489" y="133" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Resources — bağlam verisi</text>
  <rect x="382" y="148" width="214" height="26" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="489" y="165" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Prompts — şablon/iş akışı</text>
</svg>
```

Sunucunun sunduğu üç temel yapı taşı (primitive) hem amaç hem de **"kimin denetiminde"** olduğu bakımından ayrışır. **Tools (araçlar)**, modelin çağırabildiği ve dış dünyada yan etki üretebilen eylemlerdir; kararı **model** verir. **Resources (kaynaklar)**, modele bağlam olarak sunulan, genelde salt-okunur veridir; neyin sunulacağına **uygulama veya kullanıcı** karar verir. **Prompts (istem şablonları)** ise yeniden kullanılabilir, parametreli iş akışı şablonlarıdır; genelde kullanıcının yazdığı bir eğik çizgi komutu (slash-komutu) gibi çalışır, yani **kullanıcı** tetikler.

```svg
<svg viewBox="0 0 640 246" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Özel entegrasyonda M çarpı N bağlantı, MCP ile M artı N bağlantı">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Entegrasyon patlaması: M×N yerine M+N</text>
  <rect x="20" y="38" width="290" height="196" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="165" y="60" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">Öncesi: her çift için özel kod</text>
  <line x1="82" y1="94" x2="248" y2="94" stroke="#5D7789" stroke-width="1"/>
  <line x1="82" y1="94" x2="248" y2="138" stroke="#5D7789" stroke-width="1"/>
  <line x1="82" y1="94" x2="248" y2="182" stroke="#5D7789" stroke-width="1"/>
  <line x1="82" y1="138" x2="248" y2="94" stroke="#5D7789" stroke-width="1"/>
  <line x1="82" y1="138" x2="248" y2="138" stroke="#5D7789" stroke-width="1"/>
  <line x1="82" y1="138" x2="248" y2="182" stroke="#5D7789" stroke-width="1"/>
  <line x1="82" y1="182" x2="248" y2="94" stroke="#5D7789" stroke-width="1"/>
  <line x1="82" y1="182" x2="248" y2="138" stroke="#5D7789" stroke-width="1"/>
  <line x1="82" y1="182" x2="248" y2="182" stroke="#5D7789" stroke-width="1"/>
  <circle cx="70" cy="94" r="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <circle cx="70" cy="138" r="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <circle cx="70" cy="182" r="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <circle cx="260" cy="94" r="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <circle cx="260" cy="138" r="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <circle cx="260" cy="182" r="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="165" y="222" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">3 host × 3 sunucu = 9 özel bağlantı</text>
  <rect x="330" y="38" width="290" height="196" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="60" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">MCP ile: tek standart protokol</text>
  <line x1="392" y1="94" x2="470" y2="138" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="392" y1="138" x2="470" y2="138" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="392" y1="182" x2="470" y2="138" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="510" y1="138" x2="588" y2="94" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="510" y1="138" x2="588" y2="138" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="510" y1="138" x2="588" y2="182" stroke="#5D7789" stroke-width="1.2"/>
  <rect x="470" y="86" width="40" height="104" rx="8" fill="#186399"/>
  <text x="490" y="142" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui" transform="rotate(-90 490 138)">MCP</text>
  <circle cx="380" cy="94" r="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <circle cx="380" cy="138" r="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <circle cx="380" cy="182" r="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <circle cx="600" cy="94" r="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <circle cx="600" cy="138" r="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <circle cx="600" cy="182" r="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="475" y="222" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">3 + 3 = 6 bağlantı (bir kez yaz)</text>
</svg>
```

### Mülakatta ne sorulur?

**"MCP hangi problemi çözer?"** Entegrasyon patlamasını. M uygulama ile N araç arasında ayrı ayrı kod yazmak **M×N** karmaşıklığı üretir; ortak protokol bunu **M+N**'e indirir. MCP bir eğitim formatı, bir arama algoritması veya bir donanım optimizasyonu değildir — çıkarım anındaki entegrasyon katmanıdır.

**"Araç çağrısı (function calling) ile farkı ne?"** İkisi farklı katmanlardır. Araç çağrısı **model tarafındaki karardır**. MCP ise araçların **nasıl keşfedileceğini, nasıl tanımlanacağını ve host ile sunucu arasında nasıl taşınacağını** standartlaştırır. Biri diğerinin yerini almaz, tamamlar.

**"stdio mu, HTTP mi?"** Sunucu kullanıcının makinesinde çalışacaksa **stdio**: süreç boruları üzerinden, düşük gecikmeli ve ağ yüzeyi olmayan bir kanal. Sunucu ağ üzerinde, ekipçe paylaşılan bir yerdeyse **streamable HTTP**. Önemli olan şu: taşıma değişse de **protokol mesajları (JSON-RPC) ve araç tanımları aynı kalır**; uzak taşımada ise TLS ve kimlik/yetki doğrulaması eklenir.

```svg
<svg viewBox="0 0 640 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="MCP taşıma katmanı: stdio yerel, HTTP uzak">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Taşıma (transport): aynı protokol, iki kanal</text>
  <rect x="26" y="42" width="284" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="168" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">stdio — yerel</text>
  <text x="168" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Host ve sunucu aynı makinede</text>
  <rect x="52" y="104" width="100" height="40" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.1"/>
  <text x="102" y="128" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Client</text>
  <rect x="184" y="104" width="100" height="40" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="234" y="128" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Server</text>
  <line x1="152" y1="124" x2="180" y2="124" stroke="#5D7789" stroke-width="2"/>
  <path d="M184 124 l-9 -4.5 l0 9 z" fill="#5D7789"/>
  <text x="168" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">süreç boruları (pipe) · düşük gecikme</text>
  <rect x="330" y="42" width="284" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">HTTP (streamable) — uzak</text>
  <text x="472" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Sunucu ağ üzerinde, başka barındırıcıda</text>
  <rect x="356" y="104" width="100" height="40" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.1"/>
  <text x="406" y="128" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Client</text>
  <rect x="488" y="104" width="100" height="40" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="538" y="128" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Server</text>
  <line x1="456" y1="124" x2="484" y2="124" stroke="#5D7789" stroke-width="2" stroke-dasharray="5 4"/>
  <path d="M488 124 l-9 -4.5 l0 9 z" fill="#5D7789"/>
  <text x="472" y="170" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">ağ · TLS + kimlik/yetki gerekir</text>
</svg>
```

**"Güvenlik neden kritik?"** MCP gerçek yetenekler açar ve **güven sınırı sunucuda biter**. Üç kural vardır.

1. Sunucudan gelen araç açıklamaları ve içerik **güvenilmez girdidir**. Kötü niyetli bir sunucu, açıklama metnine gizlediği talimatla modeli kandırabilir. Buna **istem enjeksiyonu (prompt/tool injection — girdiye gizlenmiş komut)** denir.
2. Dış dünyada iz bırakan araç çağrıları **kullanıcı onayına** tabi olmalıdır.
3. Sunucu **en az ayrıcalıkla** (least privilege) ve izole çalışmalı; kimlik bilgisini ya da yetki anahtarını (token) dışarı sızdırmamalıdır.

TLS yalnız taşımayı korur; enjeksiyonu ve yetki suistimalini çözmez.

**"Ajan mimarisine katkısı?"** Ajanın dış dünyayla konuştuğu tek, standart ve keşfedilebilir kapı olur: yetenekler modüler sunuculara ayrılır, aynı sunucu farklı ajanlarla paylaşılır ve tüm trafik tek noktadan denetlenir.

### Tek cümlede

MCP, yapay zekâ uygulamalarını dış araç ve verilere bağlayan ortak fiştir: M×N entegrasyonu M+N'e indirir, ama açtığı kapı gerçek olduğu için onay, en az ayrıcalık ve girdiye şüphe zorunludur.

# mcpp-1 | Orta
## Soru
Model Context Protocol'ün (MCP) çözdüğü temel problem hangisidir?
## A
Dil modellerini sıfırdan eğitmek için ortak bir eğitim verisi formatı tanımlamak
🤖 MCP bir eğitim protokolü değildir; çıkarım-zamanı entegrasyonunu standartlaştırır, model eğitmez.
## B
Uygulama ile dış araç/veri bağlantısını standartlaştırmak; M×N yerine M+N
🤖 Doğru: her entegrasyon için özel yapıştırma kod yazma ihtiyacını kaldırır; "AI için USB-C" benzetmesi budur.
## C
Vektör veritabanlarında yaklaşık en-yakın-komşu (ANN) aramasını hızlandırmak ve recall'ı yükseltmek
🤖 ANN indeksleme vektör DB konusudur; MCP bir entegrasyon protokolüdür, arama algoritması değil.
## D
Model çıkarımı sırasında GPU bellek kullanımını ve toplu işlem verimini optimize etmek
🤖 MCP donanım veya bellek yönetimiyle ilgilenmez; uygulama-yetenek arayüzünü tanımlar.
## Doğru
B
## Açıklama
MCP'nin çözdüğü problem **entegrasyon patlamasıdır**: her AI uygulamasını her araç/veri kaynağına bağlamak için ayrı, özel "yapıştırma kod" yazmak M×N karmaşıklığı üretir. MCP, ortak bir protokol tanımlayarak bunu **M+N**'e indirir — bir sunucu bir kez yazılır, MCP konuşan her host'la çalışır (B). Yaygın benzetme "AI uygulamaları için USB-C"dir. MCP bir eğitim formatı (A), ANN hızlandırıcı (C) veya GPU/bellek yöneticisi (D) değildir; çıkarım-zamanında model uygulamasının dış yeteneklerle konuşma biçimini standartlaştırır.

# mcpp-2 | Orta
## Soru
MCP mimarisinde host, client ve server rolleri nasıl ayrışır?
## A
Üçü de tek bir süreçtir; ayrım yalnızca dokümantasyondaki isimlendirmeden ibarettir
🤖 Roller işlevsel olarak ayrıdır; özellikle server ayrı bir süreç olarak yetenekleri dışa açar.
## B
Server kullanıcının kullandığı uygulamadır; host ise veriyi ve araçları sunan arka süreçtir
🤖 Roller ters verilmiş; host kullanıcının uygulaması, server yeteneği sunan taraftır.
## C
Client yetenekleri sunar, host bunları çağırır, server yalnızca ağ taşıma katmanıdır
🤖 Yanlış eşleşme; yeteneği sunan server'dır, client host içindeki bağlantı yürütücüsüdür.
## D
Host uygulama ve onay merci, client 1:1 bağlantıyı yürütür, server yetenekleri açar
🤖 Doğru: host dış kabuk ve onay noktası, client protokol yürütücüsü, server yetenek sağlayıcısıdır.
## Doğru
D
## Açıklama
MCP üç rolü net ayırır (D): **Host** — kullanıcının etkileşime girdiği AI uygulaması; modeli barındırır ve kullanıcı onaylarını yönetir. **Client** — host içinde yaşayan, **her server'a bire-bir** bağlanıp JSON-RPC konuşmasını yürüten bileşen (bir host birden çok client ile birden çok server'a bağlanabilir). **Server** — dosya sistemi, veritabanı, API gibi yetenekleri **tool/resource/prompt** olarak dışa açan bağımsız süreç. Bu ayrım güvenlik için de önemlidir: onay ve güven kararları host/client tarafında verilir, server güven sınırının **dışındadır**. Rolleri tek sürece indirmek (A) veya ters eşlemek (B, C) yanlıştır.

# mcpp-3 | Orta
## Soru
MCP sunucusunun sunduğu "tools", "resources" ve "prompts" ilkelleri (primitives) nasıl farklılaşır?
## A
Üçü de aynı işi yapar; farklı isimlerle çağrılan salt-okunur veri uçlarından ibarettirler
🤖 İşlevleri ve kontrol modelleri farklıdır; tools eylem yapar, resources veri sunar, prompts şablondur.
## B
Tools salt veri sunar, resources eylem yürütür, prompts ise taşıma/ağ ayarlarını tutar
🤖 Eşleşme ters; tools eylem, resources veri, prompts iş-akışı şablonudur.
## C
Tools model-kontrollü eylem, resources salt-okunur bağlam verisi, prompts iş-akışı şablonudur
🤖 Doğru: üç ilkel farklı amaca ve farklı "kim kontrol eder" modeline sahiptir.
## D
Üçü de yalnızca modelin kendi kararıyla tetiklediği, yan etki üreten işlemlerdir
🤖 Resources ve prompts tipik olarak uygulama/kullanıcı-kontrollüdür; hepsini model-tetiklemeli saymak yanlıştır.
## Doğru
C
## Açıklama
Üç ilkel hem **amaç** hem **kontrol modeli** bakımından ayrışır (C): **Tools** modelin function calling ile **çağırabileceği eylemlerdir** ve yan etki (dosya yazma, sorgu) içerebilir — **model-kontrollü**. **Resources** modele bağlam olarak sunulan **veridir** (dosya, kayıt), genelde **salt-okunur** ve **uygulama/kullanıcı-kontrollü**. **Prompts** parametreli, yeniden kullanılabilir **iş-akışı şablonlarıdır** (ör. bir slash-komutu), genelde **kullanıcı-kontrollü**. Bu ayrımı yok saymak (A), ters eşlemek (B) veya hepsini model-tetiklemeli yan etki saymak (D) yanlıştır. Kontrol modeli, güvenlik onayı tasarımının temelidir: yan etkili tool çağrıları onay gerektirir.

# mcpp-4 | İleri
## Soru
Function calling ile MCP arasındaki ilişki en doğru nasıl tanımlanır?
## A
Function calling model tarafındaki karardır; MCP keşif, tanım ve taşımayı standartlaştırır
🤖 Doğru: biri modelin çağrı kararı, diğeri o çağrının nasıl bulunup taşınacağının standardıdır; tamamlayıcıdırlar.
## B
MCP, function calling'in yerini alır ve modelin araç çağırma yeteneğini tamamen ortadan kaldırır
🤖 MCP function calling'i kaldırmaz; onun ürettiği çağrıyı taşıyıp standartlaştırır, tamamlar.
## C
Function calling yalnızca MCP olmayan sistemlerde çalışır; ikisi aynı anda kullanılamaz
🤖 Aksine birlikte çalışırlar: model çağrı kararı verir, MCP bu çağrıyı server'a ulaştırır.
## D
İkisi tamamen ilgisiz kavramlardır; farklı ekosistemlere ait, kesişmeyen çözümlerdir
🤖 İkisi sıkı ilişkilidir: MCP, function calling'in araç tarafını standart bir protokole oturtur.
## Doğru
A
## Açıklama
İkisi **farklı katmanlarda tamamlayıcıdır** (A): **Function calling**, modelin "şu aracı şu argümanlarla çağırmak istiyorum" kararını (yapısal çıktı olarak) üretmesidir — bu model tarafıdır. **MCP** ise bu araçların **nasıl keşfedileceğini** (server hangi tool'ları sunuyor), **nasıl tanımlanacağını** (şema) ve host↔server arasında **nasıl taşınacağını** (JSON-RPC) standartlaştırır. Sonuç: bir MCP server'ı bir kez yazılır, function calling yapan farklı host ve modellerle çalışır. MCP, function calling'in yerini almaz (B), onsuz kullanılmaz değildir (C) ve ilgisiz değildir (D) — araç ekosistemini standarda bağlayan keşif/taşıma katmanıdır.

# mcpp-5 | İleri
## Soru
Bir geliştirici, üçüncü tarafın yayınladığı bir MCP sunucusunu ajanına bağlıyor. Güvenlik açısından en kritik önlem hangisidir?
## A
Sunucu yayınlanmış olduğuna göre güvenilir sayılmalı; araç çağrıları onaysız yürütülmelidir
🤖 Koşulsuz güven tehlikelidir; güvenilmez sunucu enjeksiyon ve zararlı eylemlere kapı açar.
## B
Sunucu içeriğini güvenilmez say, yan etkili çağrıları onaya bağla, en az ayrıcalıkla çalıştır
🤖 Doğru: güven sınırı sunucuda biter; onay, en az ayrıcalık ve girdiye şüphe temel savunmadır.
## C
Tek gerçek önlem sunucunun düşük gecikmeyle yanıt vermesini ve zaman aşımına düşmemesini sağlamaktır
🤖 Gecikme bir performans konusudur, güvenlik değil; asıl risk yetki, onay ve enjeksiyondur.
## D
Sunucu HTTPS üzerinden konuşuyorsa taşıma güvenli olduğu için başka önlem gerekmez
🤖 Taşıma şifrelemesi gereklidir ama yeterli değildir; enjeksiyon ve yetki suistimali TLS ile çözülmez.
## Doğru
B
## Açıklama
MCP güçlü yetenekler açtığı için **güven sınırı** kritiktir ve sunucu bu sınırın dışındadır. Üçüncü taraf bir sunucuda temel önlemler (B): (1) sunucudan gelen **araç açıklamaları ve içerik güvenilmez girdidir** — kötü niyetli bir sunucu, açıklama metnine gizlediği talimatla modeli kandırabilir (**tool/prompt injection**); (2) **yan etkili araç çağrıları kullanıcı onayına** tabi olmalı — model otomatik olarak zararlı eylem yürütmemeli; (3) sunucu **en az ayrıcalıkla**, izole ve kimlik/token sızdırmadan çalışmalı. Koşulsuz güven (A), yalnız hız (C) veya yalnız TLS (D) yetersizdir — TLS taşımayı korur ama enjeksiyonu ve yetki suistimalini çözmez. Kural: yeteneği aç ama sınırını denetle.

# mcpp-6 | İleri
## Soru
Aynı MCP sunucusu hem geliştiricinin dizüstünde yerel olarak hem de ekip için uzak bir makinede çalıştırılmak isteniyor. Taşıma (transport) seçimi hakkında hangisi doğrudur?
## A
Yerelde stdio, uzakta streamable HTTP kullanılır; JSON-RPC mesajları aynı kalır
🤖 Doğru: taşıma kanalı değişir, protokol mesajları ve araç tanımları değişmez; uzakta TLS ve yetkilendirme eklenir.
## B
Taşıma değişince mesaj biçimi de değiştiğinden sunucunun araç tanımları yeniden yazılmalıdır
🤖 Araç tanımları taşımadan bağımsızdır; MCP'nin amacı zaten bu ayrımı korumaktır.
## C
MCP yalnızca yerel stdio taşımasını destekler; uzak kullanım için ayrı bir protokol gerekir
🤖 MCP ağ üzerinden çalışacak bir HTTP taşıması da tanımlar; ayrı protokol gerekmez.
## D
Uzak taşımada TLS ve kimlik doğrulama gereksizdir; JSON-RPC kendi şifrelemesini taşır
🤖 JSON-RPC yalnızca mesaj biçimidir; şifreleme ve kimlik doğrulama taşıma katmanının sorumluluğudur.
## Doğru
A
## Açıklama
MCP'de **taşıma (transport)** ile **protokol** birbirinden ayrıdır. Sunucu kullanıcının makinesinde çalışıyorsa **stdio** kullanılır: süreç boruları üzerinden konuşulur, gecikme düşüktür ve ağ yüzeyi yoktur. Sunucu ağ üzerinde, ekipçe paylaşılan bir yerdeyse **streamable HTTP** kullanılır. Her iki durumda da mesajlar aynı **JSON-RPC 2.0** biçimindedir ve sunucunun tool/resource/prompt tanımları değişmez (A) — bu yüzden aynı sunucu iki modda da çalışabilir. Değişen şey işletme sorumluluğudur: uzak taşımada **TLS, kimlik doğrulama ve yetkilendirme** eklenir, ağ üzerinden erişim denetlenir. Araç tanımlarının yeniden yazılması gerektiği (B), MCP'nin yalnız yerel olduğu (C) veya JSON-RPC'nin şifreleme sağladığı (D) yanlıştır.
