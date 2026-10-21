---
slug: soa-esb-kurumsal-entegrasyon
baslik: "SOA ve ESB: Kurumsal Entegrasyon ve Mikroservise Geçiş"
kademe: İleri
sira: 258
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Eski telefon santralini düşün: her abone her aboneye ayrı kablo çekmez, herkes tek bir santrale bağlanır. Santral hattı kurar, gerekirse dili çevirir ve konuşmayı kaydeder. Kurumsal servis veri yolu (Enterprise Service Bus, ESB) tam olarak bu santraldir.

Büyük kurumlarda onlarca sistem yan yana yaşar: ana bankacılık, muhasebe, kart, kampanya, raporlama. Her biri farklı yaşta olduğu için farklı dil konuşur; biri SOAP ister, biri kuyruk ister, biri gün sonu dosyası ister. Bunları ikişer ikişer bağlarsan bağlantı sayısı patlar: on sistem için 45, yüz sistem için binlerce olası bağlantı çıkar.

Servis yönelimli mimari (Service Oriented Architecture, SOA) bu karmaşayı düzenleme girişimidir; ESB de onun taşıyıcı katmanıdır. Bugün herkes mikroservis konuşuyor, ama havacılık, bankacılık ve telekom ortamlarında ESB hâlâ çalışıyor. Mülakatta "ESB kötüdür" demek yüzeysel kalır; beklenen cevap, neyi çözdüğünü ve nerede tıkandığını bilmektir.

Bu konu üstteki katmanı anlatır; SOAP zarfı, JMS kuyruğu ve MQ ayrıntıları 221 numaralı konudadır ve burada tekrarlanmaz.

### Temel kavramlar

- **SOA:** iş yeteneklerini sözleşmeli servisler olarak dışarı açan mimari yaklaşım.
- **Açık sözleşme (explicit contract):** servisin ne kabul ettiğini ve ne döndürdüğünü yazan belge. SOAP dünyasında bu WSDL ve XSD'dir.
- **Gevşek bağlılık (loose coupling):** çağıran tarafın, çağrılanın iç yapısını bilmemesi. Sözleşme durduğu sürece iç değişiklik kimseyi kırmaz.
- **Yeniden kullanılabilirlik (reusability):** tek bir "Müşteri Sorgula" servisini beş farklı kanalın kullanabilmesi.
- **Birleştirilebilirlik (composability):** küçük servisleri sıralayıp daha büyük bir iş akışı kurabilmek.
- **Keşfedilebilirlik (discoverability):** servisi bir katalogda arayıp bulabilmek. Eski standardın adı UDDI'dir. Bugün buna kurum içi servis kataloğu denir.
- **Merkezî yönetişim (governance):** sözleşme, sürüm ve erişim kurallarını tek bir kurulun karara bağlaması.
- **ESB:** sistemlerin arasında duran merkezî aracı katman.
- **Kanonik veri modeli (canonical data model):** tüm sistemlerin üzerinde anlaştığı ortak veri şeması.
- **Orkestrasyon (orchestration):** birden çok servisi tek bir merkezden sırayla çağırıp akışı yönetmek.
- **Koreografi (choreography):** merkez olmadan, olaylara tepki vererek ilerleyen akış.
- **Denetim izi (audit trail):** kim neyi ne zaman geçirdi sorusunun cevabını tutan kayıt.

### SOA mikroservisten nerede ayrılır?

İkisi de "büyük sistemi servislere böl" dediği için sık karıştırılır; oysa ayrım teknolojide değil, sahiplikte ve ölçektedir.

SOA **kurum ölçeğinde paylaşılan** servisler kurar: tek bir "Müşteri Servisi" olur ve onu on ayrı uygulama kullanır. Sahibi merkezî bir entegrasyon ekibidir, sözleşme değişikliği de bir kurul kararına bağlıdır. Veri yolu ortaktır ve akıllıdır; amaç tekrarı azaltmak ve tek elden denetlemektir.

Mikroservis ise **tek takımın sahiplendiği** küçük servisler kurar; her servis kendi verisini tutar ve takım kendi dağıtımını kimseye sormadan yapar. Veri yolu aptaldır, zekâ servislerin içindedir; amaç bağımsız hızdır. Ayrıntısı 95 ve 97 numaralı konulardadır.

Kısa kural şudur: SOA merkezî yönetişimle ölçek arar, mikroservis takım özerkliğiyle hız arar. SOA belirli bir teknolojiyi şart koşmaz, ama pratikte SOAP, WSDL ve ESB üçlüsüyle birlikte geldi.

### ESB ne iş yapar?

ESB'nin beş klasik sorumluluğu vardır ve hepsi tek bir merkezî katmanda toplanır.

1. **Protokol dönüştürme.** Bir uç SOAP konuşur, biri MQ kuyruğu, biri dosya, biri REST kullanır. ESB araya girip taşıma biçimini çevirir, böylece uçlar birbirinin protokolünü öğrenmek zorunda kalmaz.
2. **Mesaj dönüşümü.** Aynı bilgi her sistemde farklı isim ve yapıyla durduğu için ESB alanları birbirine eşler; XML tarafında bu işi genelde XSLT adlı dönüşüm dili yapar.
3. **İçerik tabanlı yönlendirme (content-based routing).** ESB mesajın içindeki bir alana bakar ve örneğin ürün koduna göre hedefi seçer; gönderen mesajın kime gittiğini bilmez.
4. **Orkestrasyon.** Bir iş için üç servisi sırayla çağırmak gerekebilir; ESB bu sırayı, hata durumunu ve geri alma adımını yönetir.
5. **İzleme ve denetim.** Tüm trafik tek noktadan geçtiği için kayıt, ölçüm ve denetim izi tek yerde toplanır; bu, düzenleyici kurum karşısında değerli bir özelliktir.

Ürün örnekleri genel olarak şunlardır: IBM Integration Bus, Oracle Service Bus, MuleSoft, WSO2, Apache Camel. Camel biraz farklıdır: ayrı bir sunucu kurmadan, kütüphane olarak uygulamanın içine de gömülebilir.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ESB merkezi veri yolu ve beş sorumluluğu">
  <defs>
    <marker id="soaesb-ok1" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">ESB: herkes santrale bağlanır, santral çevirir</text>
  <rect x="16" y="38" width="148" height="44" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="90" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Ana sistem</text>
  <text x="90" y="74" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">SOAP / XML</text>
  <rect x="175" y="38" width="148" height="44" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="249" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kart sistemi</text>
  <text x="249" y="74" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">MQ kuyruğu</text>
  <rect x="334" y="38" width="148" height="44" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="408" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Muhasebe</text>
  <text x="408" y="74" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">gün sonu dosyası</text>
  <rect x="493" y="38" width="148" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="567" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Yeni kanal</text>
  <text x="567" y="74" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">REST / JSON</text>
  <line x1="90" y1="84" x2="90" y2="102" stroke="#5D7789" stroke-width="1.4" marker-end="url(#soaesb-ok1)"/>
  <line x1="249" y1="84" x2="249" y2="102" stroke="#5D7789" stroke-width="1.4" marker-end="url(#soaesb-ok1)"/>
  <line x1="408" y1="84" x2="408" y2="102" stroke="#5D7789" stroke-width="1.4" marker-end="url(#soaesb-ok1)"/>
  <line x1="567" y1="84" x2="567" y2="102" stroke="#5D7789" stroke-width="1.4" marker-end="url(#soaesb-ok1)"/>
  <rect x="16" y="106" width="625" height="42" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <text x="328" y="132" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Kurumsal servis veri yolu (ESB)</text>
  <text x="328" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">beş sorumluluk</text>
  <rect x="16" y="174" width="120" height="70" rx="9" fill="#F2F5F7" stroke="#186399" stroke-width="1.2"/>
  <text x="76" y="194" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">Protokol</text>
  <text x="76" y="208" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">dönüştürme</text>
  <text x="76" y="230" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">SOAP · MQ · dosya</text>
  <rect x="142" y="174" width="120" height="70" rx="9" fill="#F2F5F7" stroke="#186399" stroke-width="1.2"/>
  <text x="202" y="194" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">Mesaj</text>
  <text x="202" y="208" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">dönüşümü</text>
  <text x="202" y="230" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">XSLT · alan eşleme</text>
  <rect x="268" y="174" width="120" height="70" rx="9" fill="#F2F5F7" stroke="#186399" stroke-width="1.2"/>
  <text x="328" y="194" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">Yönlendirme</text>
  <text x="328" y="208" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">içerik tabanlı</text>
  <text x="328" y="230" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">alana bakıp seçer</text>
  <rect x="394" y="174" width="120" height="70" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.2"/>
  <text x="454" y="194" text-anchor="middle" font-size="10" font-weight="700" fill="#B07D20" font-family="system-ui">Orkestrasyon</text>
  <text x="454" y="208" text-anchor="middle" font-size="10" font-weight="700" fill="#B07D20" font-family="system-ui">akış yönetimi</text>
  <text x="454" y="230" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">çok servisi sıralar</text>
  <rect x="520" y="174" width="120" height="70" rx="9" fill="#F2F5F7" stroke="#186399" stroke-width="1.2"/>
  <text x="580" y="194" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">İzleme ve</text>
  <text x="580" y="208" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">denetim</text>
  <text x="580" y="230" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">tek noktadan iz</text>
  <text x="330" y="266" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Kazanç: ikişer ikişer kablo yerine tek merkez; çeviri ve denetim tek yerde.</text>
  <text x="330" y="286" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Bedel: amber kutu büyüdükçe iş mantığı merkeze sızar ve merkez darboğaz olur.</text>
</svg>
```

**Kanonik veri modeli: faydası ve tuzağı.** ESB kırk sistemi çeviriyorsa iki yol vardır: ya her sistem çifti için ayrı çeviri yazarsın, ya da hepsinin uyacağı ortak bir dil tanımlarsın. İkincisinin adı kanonik veri modelidir.

Faydası ekonomiktir: her sistem yalnız kendisi ile ortak model arasında çeviri yazar ve toplam çeviri sayısı çarpımdan toplama iner.

Tuzağı zamanla ortaya çıkar: her yeni sistem kendi alanını ortak modele ekletir ve model yıllar içinde şişer. Sonunda üç yüz alanlı, çoğu alanı boş ve tamamını kimsenin anlamadığı bir dev şema kalır. Bir tek alanı değiştirmek bile tüm tüketicileri yeniden test etmeyi gerektirir; ortak model, ayırmak istediğin sistemleri yeniden birbirine bağlamış olur. Bu tuzağın alan sürüşü hâli 97 numaralı konuda "canonical model" başlığıyla anlatılır.

### Karıştırılan dört şey

Bu ayrımı yapabilmek mülakatta kıdem gösterir. Dört bileşen de "araya giren katman" gibi görünür, ama durdukları eksen ve yaptıkları iş farklıdır.

**ESB** sistemlerin arasında, merkezde durur; işi protokol ve biçim çevirisi, içerik tabanlı yönlendirme ve orkestrasyondur.

**API ağ geçidi (API Gateway)** dış kenarda durur ve dışarıdan içeri gelen trafiğe bakar; buna kuzey-güney (north-south) trafik denir. İşi kimlik doğrulama, yetkilendirme, hız sınırı ve TLS sonlandırmadır; ayrıntısı 99 numaralı konudadır. Ağ geçidi kanonik model çevirisi yapmaz.

**Mesaj aracısı (message broker)** üretici ile tüketici arasında durur; işi kuyruk ve konu tutmak, teslim garantisi vermek ve ölü mektup kuyruğu işletmektir. Kafka ile RabbitMQ ayrıntıları 107 ve 119 numaralı konulardadır. Aracı mesajın içeriğini dönüştürmez.

**Servis ağı (service mesh)** servislerin yanında durur ve servisten servise giden trafiğe bakar; buna doğu-batı (east-west) trafik denir. Her servisin yanına bir yardımcı vekil süreç (sidecar proxy) konur; işi karşılıklı TLS (mTLS), yeniden deneme, devre kesici ve trafik bölmedir. Servis ağı iş verisini dönüştürmez.

Tek cümlelik ayrım şudur: ESB veriyi çevirir, ağ geçidi kapıyı tutar, aracı mesajı saklar, servis ağı ise ağ davranışını yönetir.

```svg
<svg viewBox="0 0 680 292" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ESB, API ağ geçidi, mesaj aracısı ve servis ağı karşılaştırması">
  <text x="340" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Karıştırılan dört katman: kim nerede durur, ne yapar?</text>
  <rect x="12" y="36" width="156" height="188" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="90" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">ESB</text>
  <text x="90" y="76" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sistemlerin arasında, merkez</text>
  <line x1="24" y1="86" x2="156" y2="86" stroke="#186399" stroke-width="0.8"/>
  <text x="90" y="104" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">protokol + biçim çevirisi</text>
  <text x="90" y="120" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">içerik tabanlı yönlendirme</text>
  <text x="90" y="136" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">orkestrasyon, denetim izi</text>
  <text x="90" y="164" text-anchor="middle" font-size="9" font-weight="700" fill="#B23B3B" font-family="system-ui">yapmaz:</text>
  <text x="90" y="182" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">kenar kimlik / kota</text>
  <text x="90" y="198" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">servis başına vekil</text>
  <rect x="180" y="36" width="156" height="188" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="258" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">API ağ geçidi (99)</text>
  <text x="258" y="76" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">dış kenar, kuzey-güney</text>
  <line x1="192" y1="86" x2="324" y2="86" stroke="#25804C" stroke-width="0.8"/>
  <text x="258" y="104" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">kimlik ve yetki</text>
  <text x="258" y="120" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">hız sınırı, kota</text>
  <text x="258" y="136" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">TLS sonlandırma</text>
  <text x="258" y="164" text-anchor="middle" font-size="9" font-weight="700" fill="#B23B3B" font-family="system-ui">yapmaz:</text>
  <text x="258" y="182" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">kanonik model çevirisi</text>
  <text x="258" y="198" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">uzun iş akışı yönetimi</text>
  <rect x="348" y="36" width="156" height="188" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="426" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">Mesaj aracısı</text>
  <text x="426" y="76" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">üretici ile tüketici arası</text>
  <line x1="360" y1="86" x2="492" y2="86" stroke="#B07D20" stroke-width="0.8"/>
  <text x="426" y="104" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">kuyruk ve konu</text>
  <text x="426" y="120" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">teslim garantisi, DLQ</text>
  <text x="426" y="136" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">eşzamansız tampon</text>
  <text x="426" y="164" text-anchor="middle" font-size="9" font-weight="700" fill="#B23B3B" font-family="system-ui">yapmaz:</text>
  <text x="426" y="182" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">biçim dönüştürme</text>
  <text x="426" y="198" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">akış orkestrasyonu</text>
  <rect x="516" y="36" width="156" height="188" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.6"/>
  <text x="594" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">Servis ağı</text>
  <text x="594" y="76" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">servis yanı, doğu-batı</text>
  <line x1="528" y1="86" x2="660" y2="86" stroke="#5D7789" stroke-width="0.8"/>
  <text x="594" y="104" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">yardımcı vekil (sidecar)</text>
  <text x="594" y="120" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">karşılıklı TLS (mTLS)</text>
  <text x="594" y="136" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">yeniden deneme, devre</text>
  <text x="594" y="164" text-anchor="middle" font-size="9" font-weight="700" fill="#B23B3B" font-family="system-ui">yapmaz:</text>
  <text x="594" y="182" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">iş verisi dönüşümü</text>
  <text x="594" y="198" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">iş mantığı taşımak</text>
  <text x="340" y="248" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ESB veriyi çevirir · ağ geçidi kapıyı tutar · aracı mesajı saklar · servis ağı ağı yönetir.</text>
  <text x="340" y="272" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Dördü de iş mantığı taşımamalı; pratikte en sık ESB'ye sızar.</text>
</svg>
```

### ESB'den nasıl çıkılır?

Önce niye çıkıldığını anlamak gerekir. Sloganı "akıllı uçlar, aptal borular" (smart endpoints, dumb pipes) diye bilinir; yani zekâ servislerde dursun, borular yalnız taşısın. Bunun üç somut nedeni vardır.

**Bir: tek hata noktası ve dağıtım darboğazı.** Tüm akışlar aynı veri yolunu paylaştığı için bir ekibin küçük değişikliği ortak yapılandırmayı değiştirir. Dağıtım penceresi tek olduğundan herkes sıraya girer; üstelik ESB durursa kurumun yarısı birden durur.

**İki: iş mantığının veri yoluna sızması.** Başta yalnız yönlendirme kuralı konur, sonra "tutar şu limitin üstündeyse ek onaya git" eklenir. Bu artık yönlendirme değil, bir iş kuralıdır; sahibi belirsizleşir ve ürün ekibi kendi kuralını ne görebilir ne değiştirebilir.

**Üç: merkezî ekip darboğazı.** Her entegrasyon değişikliği entegrasyon ekibinden geçer, ama talep sayısı ekip kapasitesini aşar ve kuyruk uzar. Ürün ekipleri beklemeyi öğrenir ve zamanla kısayol aramaya başlar.

**ESB hâlâ ne zaman doğru?** Bu soruyu atlamak yüzeysel görünmeye yol açar; ESB dört durumda hâlâ makul bir tercihtir. Birincisi çok sayıda eski sistemin bir arada yaşamasıdır. İkincisi protokol çeşitliliğidir: SOAP, MQ, dosya ve sabit uzunluklu kayıt aynı anda kullanılır. Üçüncüsü düzenleyicinin tek denetim noktası talebidir. Dördüncüsü kurumlar arası entegrasyondur; karşı taraf senin sözleşmeni kabul etmek zorunda değildir.

**Geçiş: boğan incir.** Desenin adı boğan incirdir (strangler fig); adını, ev sahibi ağacı yıllar içinde sarıp yerini alan bitkiden alır. ESB bir gecede kaldırılmaz ve kural üç adımdır: yeni akışları doğrudan servisten servise kur, eski akışları ESB'de bırak, sonra yolları teker teker taşı.

Taşırken bir tuzak vardır: eski kanonik model yeni servislerin sözleşmesine sızabilir. O zaman ESB'yi kaldırmış ama en pahalı mirasını devralmış olursun. Çözüm araya bir bozulma-önleyici katman (anti-corruption layer) koymaktır; bu katman eski modeli yeni servisin diline çevirir. Ayrıntısı 97 ve 221 numaralı konulardadır.

```svg
<svg viewBox="0 0 660 258" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Boğan incir deseniyle ESB'den kademeli çıkış">
  <defs>
    <marker id="soaesb-ok3" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Boğan incir: ESB bir gecede değil, yol yol kaldırılır</text>
  <rect x="16" y="40" width="196" height="132" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="114" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">1) Bugün</text>
  <text x="114" y="86" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tüm akışlar ESB'den geçer</text>
  <text x="114" y="106" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tek dağıtım penceresi</text>
  <text x="114" y="126" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iş kuralları merkeze sızmış</text>
  <text x="114" y="152" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">değişim sırası bekler</text>
  <line x1="216" y1="106" x2="240" y2="106" stroke="#5D7789" stroke-width="1.6" marker-end="url(#soaesb-ok3)"/>
  <rect x="244" y="40" width="196" height="132" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="342" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">2) Geçiş</text>
  <text x="342" y="86" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yeni akış: doğrudan servise</text>
  <text x="342" y="106" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eski akış: ESB'de kalır</text>
  <text x="342" y="126" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yollar teker teker taşınır</text>
  <text x="342" y="152" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">her adımda geri dönüş var</text>
  <line x1="444" y1="106" x2="468" y2="106" stroke="#5D7789" stroke-width="1.6" marker-end="url(#soaesb-ok3)"/>
  <rect x="472" y="40" width="172" height="132" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="558" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">3) Sonra</text>
  <text x="558" y="86" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ESB'de yalnız gerçekten</text>
  <text x="558" y="102" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">gereken eski akışlar kalır</text>
  <text x="558" y="126" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">belki hiç kapanmaz</text>
  <text x="558" y="152" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">ve bu bir başarısızlık değil</text>
  <rect x="16" y="188" width="628" height="54" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="330" y="210" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Araya bozulma-önleyici katman (anti-corruption layer) koy.</text>
  <text x="330" y="230" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Yoksa ESB kalkar ama şişmiş kanonik model yeni servislere miras kalır.</text>
</svg>
```

### Mülakatta ne sorulur?

- **"SOA nedir, mikroservisten farkı ne?"** Beklenen cevap sahiplik ve ölçek ayrımıdır: SOA kurum ölçeğinde paylaşılan servis ve merkezî yönetişim kurar, mikroservis ise takım özerkliği kurar. Yalnızca "SOAP kullanır" demek yetmez.
- **"ESB tam olarak ne yapar?"** Beş sorumluluğu say: protokol dönüştürme, mesaj dönüşümü, içerik tabanlı yönlendirme, orkestrasyon ve tek noktadan izleme/denetim.
- **"ESB ile API ağ geçidi aynı şey mi?"** Hayır: ağ geçidi kuzey-güney kenar trafiğine, ESB ise sistemler arası çeviri ve orkestrasyona bakar. Mesaj aracısı ve servis ağı da bunlardan ayrı işler yapar.
- **"Neden ESB'den vazgeçildi?"** Üç yapısal sorunu say: ortak dağıtım darboğazı, iş mantığının veri yoluna sızması ve merkezî entegrasyon ekibinin kuyruğu.
- **"ESB kötü müdür?"** Hayır; çok sayıda eski sistem, protokol çeşitliliği ve düzenleyici denetim ihtiyacı bir aradaysa ESB hâlâ doğru araçtır.
- **"Kanonik veri modeli iyi fikir midir?"** Toplam çeviri sayısını düşürür; ama zamanla şişerse tüm tüketicileri birbirine bağlar ve her değişimi kilitler.
- **"ESB'den nasıl çıkarsın?"** Boğan incir deseniyle: yeni akışlar doğrudan kurulur, eskiler yerinde bırakılır ve yollar tek tek taşınır. Araya mutlaka bozulma-önleyici katman konur.

### Tek cümlede

SOA, iş yeteneklerini kurum ölçeğinde paylaşılan sözleşmeli servisler olarak açar. ESB de bu servisleri birbirine bağlayan merkezî santraldir. Değeri protokol ve biçim çevirisinde, orkestrasyonda ve tek noktadan denetimdedir. Bedeli ise dağıtım darboğazı, iş mantığının merkeze sızması ve şişen kanonik modeldir. Doğru duruş "ESB kötüdür" değil, "nerede hâlâ doğru, nereden boğan incirle çıkılır" bilmektir.

# soaesb-1 | Temel
## Soru
Havacılık sektöründe bir Java pozisyonunun ilanında "SOA ve ESB modelleri deneyimi" isteniyor. Mülakatta "servis yönelimli mimari nedir, mikroservisten farkı nedir?" diye soruluyor. En doğru cevap hangisidir?
## A
SOA, XML ve SOAP kullanan sistemlerin ortak adıdır; mikroservis ise JSON ve REST kullanır, yani fark veri biçimindedir
🤖 SOA bir mimari yaklaşımdır, protokol seçimi değil; SOAP yaygındır ama tanımın parçası değildir.
## B
SOA, tek sunucuda çalışan monolit bir uygulamanın iç modüllere bölünmüş hâlidir; servisler ağ üzerinden çağrılmaz
🤖 bu modüler monolit tarifidir (96); SOA servisleri ağ üzerinden sözleşmeyle çağrılır.
## C
SOA iş yeteneklerini sözleşmeli servisler olarak açar; mikroservisten farkı, servislerin kurum ölçeğinde paylaşılması ve merkezî yönetişimle yönetilmesidir
🤖 Doğru: ayrım teknolojide değil, sahiplik ölçeğinde ve yönetişim biçimindedir.
## D
SOA mikroservisin eski adıdır; aralarında mimari bir fark yoktur, yalnızca terim modası değişmiştir
🤖 isim değişikliği değildir; sahiplik, veri ve dağıtım özerkliği farkları gerçek ve ölçülebilirdir.
## Doğru
C
## Açıklama
**SOA (servis yönelimli mimari)**, iş yeteneklerini açık sözleşmeli servisler olarak dışarı açar. İlkeleri şunlardır: açık sözleşme (WSDL/XSD), gevşek bağlılık, yeniden kullanılabilirlik, birleştirilebilirlik ve keşfedilebilirlik. Mikroservisle ortak yanı "büyük sistemi servislere böl" fikridir. Ayrım vurgudadır. SOA **kurum ölçeğinde paylaşılan** servisler kurar: tek "Müşteri Servisi" olur, onu on uygulama kullanır, sahibi merkezî bir entegrasyon ekibidir ve sözleşme değişikliği kurul kararına bağlıdır. Mikroservis ise **tek takımın sahiplendiği** servisler kurar; her servis kendi verisini tutar ve takım kendi dağıtımını bağımsız yapar (95, 97). A şıkkı sık görülen bir yanılgıdır: SOAP ve XML SOA'nın tarihsel eşlikçisidir, tanımı değildir; SOA REST ile de kurulabilir. B şıkkı modüler monolit tarifidir (96) — orada çağrı süreç içidir, SOA'da ağ üzerindedir. D şıkkı "yalnız isim değişti" der; oysa veri sahipliği, dağıtım özerkliği ve yönetişim modeli somut biçimde farklıdır.

# soaesb-2 | Orta
## Soru
Bir kurumda ana bankacılık SOAP, kart sistemi MQ kuyruğu, muhasebe gün sonu dosyası, yeni mobil kanal ise REST konuşuyor. Aradaki ESB'nin sorumlulukları hangi seçenekte doğru verilmiştir?
## A
Protokol dönüştürme, mesaj dönüşümü, içerik tabanlı yönlendirme, orkestrasyon ve tek noktadan izleme/denetim
🤖 Doğru: ESB'nin beş klasik sorumluluğu budur ve hepsi merkezî katmanda toplanır.
## B
Tüm sistemlerin ortak veritabanı olmak; sistemler veriyi tek şemada saklar ve doğrudan buradan okur
🤖 ESB veri deposu değil, geçiş katmanıdır; paylaşılan veritabanı tam da kaçınılan bağlaşımdır.
## C
Yalnız mesajı alıp saklamak ve teslim garantisiyle iletmek; biçim dönüştürme ve yönlendirme yapmamak
🤖 bu mesaj aracısının tarifidir (107, 119); ESB aracıyı kullanır ama ondan ibaret değildir.
## D
Dış istemcilerin girdiği kapı olmak; kimlik doğrulayıp hız sınırı uygulamak, mesaj biçimine karışmamak
🤖 bu API ağ geçidinin tarifidir (99); kenar politikası ESB'nin ayırt edici işi değildir.
## Doğru
A
## Açıklama
ESB'nin beş klasik sorumluluğu vardır. **Protokol dönüştürme**: bir uç SOAP, biri MQ, biri dosya, biri REST konuşur; ESB taşıma biçimini çevirir. **Mesaj dönüşümü**: aynı bilgi her sistemde farklı isim ve yapıyla durur; ESB alanları eşler, XML tarafında bunu genelde XSLT yapar. **İçerik tabanlı yönlendirme**: ESB mesajın içindeki bir alana (ör. ürün kodu) bakıp hedefi seçer, gönderen kime gittiğini bilmez. **Orkestrasyon**: bir iş için birden çok servisi sırayla çağırır, hata ve geri alma adımlarını yönetir. **İzleme ve denetim**: tüm trafik tek noktadan geçtiği için kayıt ve denetim izi tek yerde toplanır. B şıkkı ESB'yi paylaşılan veritabanı sanır; ESB bir geçiş katmanıdır, veri deposu değildir ve paylaşılan şema tam olarak kaçınılmak istenen bağlaşımı üretir. C şıkkı mesaj aracısının işidir (107, 119): kuyruk, konu, teslim garantisi. D şıkkı API ağ geçidinin işidir (99): kimlik, kota, TLS sonlandırma. ESB bu ikisini kullanabilir; ama onu diğerlerinden ayıran şey **dönüşüm ve orkestrasyondur**.

# soaesb-3 | Orta
## Soru
Bir kurumda üç ayrı ihtiyaç var. (1) Dış mobil istemciler için kimlik doğrulama ve kota. (2) Mikroservislerin birbirine yaptığı çağrılarda karşılıklı TLS ve otomatik yeniden deneme. (3) Eski MQ sistemi ile yeni REST servisi arasında protokol ve biçim çevirisi. Hangi eşleme doğrudur?
## A
(1) servis ağı, (2) API ağ geçidi, (3) mesaj aracısı
🤖 ilk ikisi yer değiştirmiş; ağ geçidi dış kenarda, servis ağı ise servisler arasındadır.
## B
(1) API ağ geçidi, (2) mesaj aracısı, (3) servis ağı
🤖 aracı kuyruk ve teslim garantisi verir; mTLS ve yeniden deneme onun sorumluluğu değildir.
## C
(1) ESB, (2) API ağ geçidi, (3) servis ağı
🤖 ESB dış kenar kapısı değildir; protokol çevirisi de servis ağının değil ESB'nin alanıdır.
## D
(1) API ağ geçidi, (2) servis ağı, (3) ESB
🤖 Doğru: ağ geçidi kuzey-güney, servis ağı doğu-batı, ESB ise sistemler arası çeviri.
## Doğru
D
## Açıklama
Dördü de "araya giren katman" gibi görünür; ayırt eden şey **ekseni ve işidir**. **API ağ geçidi** dış kenarda durur ve dışarıdan içeri gelen **kuzey-güney** trafiğe bakar: kimlik doğrulama, yetki, hız sınırı, kota, TLS sonlandırma (99). **Servis ağı (service mesh)** servislerin yanında durur ve servisten servise giden **doğu-batı** trafiğe bakar; her servisin yanına bir yardımcı vekil süreç (sidecar) konur ve karşılıklı TLS, yeniden deneme, devre kesici, trafik bölme orada uygulanır. **ESB** sistemlerin arasında, merkezde durur; işi protokol ve biçim çevirisi, içerik tabanlı yönlendirme ve orkestrasyondur — MQ ile REST arasındaki çeviri tam olarak budur. **Mesaj aracısı (broker)** ise üretici ile tüketici arasında durur: kuyruk ve konu tutar, teslim garantisi ve ölü mektup kuyruğu sağlar (107, 119); mesajın içeriğini dönüştürmez ve mTLS/yeniden deneme politikası yürütmez. A şıkkı ilk ikisini ters çevirir. B şıkkı mesh işini aracıya, çeviri işini mesh'e yükler. C şıkkı ESB'yi kenar kapısı sanar. Tek cümlelik ayrım: ESB veriyi çevirir, ağ geçidi kapıyı tutar, aracı mesajı saklar, servis ağı ağ davranışını yönetir.

# soaesb-4 | İleri
## Soru
ESB üzerinde bir yönlendirme kuralı var: tutar 50.000 TL üstündeyse mesaj ek onay servisine gidiyor. Ürün ekibi bu limiti değiştirmek istiyor. Değişiklik entegrasyon ekibinin sırasını bekliyor ve dağıtım tüm akışları riske atıyor. Buradaki asıl mimari sorun nedir?
## A
Sorun ESB ürününün yavaş çalışmasıdır; daha güçlü donanım ve yatay ölçekleme bekleme süresini ortadan kaldırır
🤖 bekleme sebebi performans değil; paylaşılan dağıtım hattı ve merkezî sahiplik.
## B
İş kuralı, sahibi olan servisin dışına paylaşılan veri yoluna sızmıştır; bu sahipliği belirsizleştirir ve tüm akışları tek dağıtım hattına bağlar
🤖 Doğru: "akıllı uçlar, aptal borular" ilkesi tam olarak bu sızmayı önlemek içindir.
## C
Sorun kuralın ESB'de olması değil, tutarın koda sabit yazılmış olmasıdır; değeri bir yapılandırma dosyasına almak yeterlidir
🤖 değeri dışarı almak ne dağıtım darboğazını ne de kural sahipliğini değiştirir.
## D
Sorun ESB'nin tek hata noktası olmasıdır; ikinci bir ESB kopyası kurmak bekleme ve sahiplik sorununu birlikte giderir
🤖 kopya eklemek erişilebilirliği artırır ama ortak dağıtım hattını ve sahiplik belirsizliğini çözmez.
## Doğru
B
## Açıklama
"Tutar şu limitin üstündeyse ek onaya git" cümlesi bir **yönlendirme kuralı gibi görünür ama iş kuralıdır**. Bu, ESB'nin en sinsi yapısal sorunudur: iş mantığı, sahibi olan servisin dışına, herkesin paylaştığı veri yoluna sızar. İki sonucu vardır. Birincisi **sahiplik belirsizleşir**: ürün ekibi kendi kuralını göremez ve değiştiremez, kural artık entegrasyon ekibinin envanterindedir. İkincisi **dağıtım hattı ortaklaşır**: tek bir kuralın değişmesi ortak yapılandırmayı değiştirir, dağıtım penceresi tektir ve tüm akışlar aynı riski paylaşır. Mikroservis çağının "**akıllı uçlar, aptal borular**" ilkesi tam bu sızmayı önlemek içindir; kural, sahibi olan servisin kodunda durur, boru yalnız taşır. A şıkkı sorunu performansa bağlar; oysa bekleyiş teknik değil, örgütsel ve mimari bir kuyruktur. C şıkkı doğru bir küçük iyileştirme önerir ama asıl sorunu atlar: değer dışarı alınsa bile kural hâlâ yanlış katmandadır ve sahibi yine belirsizdir. D şıkkı erişilebilirlik önlemidir; ikinci kopya ESB düşerse yardımcı olur, fakat ortak dağıtım hattını ve sahiplik sorununu ortadan kaldırmaz.

# soaesb-5 | İleri
## Soru
ESB üzerinde ortak bir "Müşteri" kanonik veri modeli tanımlanmış. Sekiz yılda 40 sisteme hizmet etmiş ve 300 alana çıkmış. Her yeni sistem kendi alanını ekletmiş; alanların çoğu çoğu sistem için boş. Şimdi tek bir alanın değişmesi tüm tüketicilerin yeniden test edilmesini gerektiriyor. Kanonik veri modeli hakkında hangisi doğrudur?
## A
Kanonik model ikili çeviri sayısını azaltır; ama tek şişkin şemaya dönüşünce tüm tüketicileri birbirine bağlar ve değişimi pahalılaştırır
🤖 Doğru: faydası çeviri ekonomisi, bedeli ortak modelin ürettiği bağlaşımdır.
## B
Kanonik model her ölçekte doğru bir yatırımdır; alan sayısı arttıkça değeri de artar, tek gerçek risk depolama maliyetidir
🤖 asıl bedel depolama değil bağlaşımdır; büyüyen ortak model her değişimi yavaşlatır.
## C
Kanonik model baştan yanlış bir fikirdir; her sistem çiftinin kendi çevirisini yazması her ölçekte daha ucuzdur
🤖 az sistemde doğru olabilir; sistem sayısı arttıkça ikili çeviri sayısı hızla katlanır.
## D
Sorunun kaynağı şemanın XML olmasıdır; aynı model JSON şemasına taşınırsa tüketiciler arasındaki bağlaşım ortadan kalkar
🤖 biçim değişikliği bağlaşımı değiştirmez; sorun modelin ortaklığında, sözdiziminde değil.
## Doğru
A
## Açıklama
Kanonik veri modelinin **faydası ekonomiktir**. Kırk sistem birbirine ikişer ikişer çevrilseydi çeviri sayısı çarpım hızında büyürdü; ortak dil sayesinde her sistem yalnız kendisi ile ortak model arasında çeviri yazar ve sayı toplama iner. **Tuzağı ise zamanla ortaya çıkar.** Her yeni sistem kendi alanını ortak modele ekletir, kimse alan silmeye cesaret edemez ve model şişer. Sonunda üç yüz alanlı, çoğu alanı çoğu tüketici için boş, tamamını kimsenin anlamadığı bir şema kalır. Daha kötüsü, tüm tüketiciler aynı modele bağlı olduğu için **bir alan değişikliği hepsini etkiler** — ayırmak istediğin sistemleri ortak model yeniden birbirine bağlamış olur. B şıkkı bedeli depolamaya indirger; asıl maliyet disk değil, değişim hızıdır. C şıkkı ters uca kaçar: sistem sayısı azken ikili çeviri gerçekten daha ucuzdur, ama sayı arttıkça bu yaklaşım yönetilemez hale gelir; sorun fikrin kendisinde değil, sınırsız büyümesindedir. D şıkkı sözdizimini suçlar; XML yerine JSON kullanmak alan sayısını da bağlaşımı da değiştirmez. Pratik çare, tek dev model yerine bağlam başına model tutmaktır (97).

# soaesb-6 | Uzman
## Soru
Kurum ESB'den çıkmak istiyor. Veri yolunda 200 entegrasyon akışı var; bir kısmı düzenleyici nedenle tek noktadan denetim izi gerektiriyor. Kesinti kabul edilmiyor ve akışların çoğunun dokümantasyonu eksik. Hangi yaklaşım doğrudur?
## A
Tüm akışları belirlenen bir hafta sonunda doğrudan servis-servis çağrılara çevirmek ve ESB'yi aynı gece kapatmak
🤖 tek seferlik geçişte 200 akışın yazılı olmayan kuralları aynı anda üretimde patlar.
## B
ESB'yi olduğu gibi bırakıp yeni mikroservisleri de ESB üzerinden bağlamak; tek denetim noktası korunduğu için değişiklik gereksizdir
🤖 bu darboğazı kalıcılaştırır; ESB'ye ihtiyacı olmayan yeni akışlara boşuna bağımlılık ekler.
## C
Yeni akışları doğrudan servis-servis kurup eskileri tek tek taşımak; kanonik modelin yeni servislere sızmaması için araya bozulma-önleyici katman koymak
🤖 Doğru: boğan incir riski parçalara böler, ACL yeni modeli eski şemadan yalıtır.
## D
Kanonik veri modelini tüm yeni mikroservislerin ortak sözleşmesi yapmak; ESB kalksa bile ortak model korunursa uyum sürer
🤖 bu, ESB'nin en pahalı mirasını mikroservislere devretmek olur; bağlaşım aynen kalır.
## Doğru
C
## Açıklama
Doğru yaklaşım **boğan incir (strangler fig)** desenidir: yeni akışlar doğrudan servisten servise kurulur, eski akışlar ESB'de bırakılır ve yollar teker teker taşınır. Böylece risk küçük parçalara bölünür ve her adımda geri dönüş yolu açık kalır. Kritik ek adım **bozulma-önleyici katmandır (anti-corruption layer)**: eski kanonik model yeni servislerin sözleşmesine sızarsa ESB'yi kaldırmış ama mirasını devralmış olursun; ACL eski modeli yeni servisin diline çevirerek bunu engeller (97, 221). A şıkkı tek seferlik büyük geçiştir; dokümante edilmemiş 200 akışın gizli kuralları aynı gece üretimde ortaya çıkar ve geri dönüş yolu kalmaz. B şıkkı hiçbir şey değiştirmez: yeni servisler protokol çevirisine ihtiyaç duymadığı hâlde ESB'ye bağlanır, dağıtım darboğazı kalıcılaşır. D şıkkı en aldatıcısıdır — taşıyıcıyı kaldırıp asıl bağlaşım kaynağını, yani şişmiş ortak modeli korumak, sorunu çözmez yalnız yerini değiştirir. Son olarak şunu da söylemek gerekir: düzenleyici tek denetim noktası, protokol çeşitliliği ve çok sayıda eski sistem varsa ESB'nin bir bölümü **kalıcı olarak** yerinde kalabilir; hedef "ESB'yi yok etmek" değil, iş mantığını ve gereksiz bağımlılığı ondan çıkarmaktır.
