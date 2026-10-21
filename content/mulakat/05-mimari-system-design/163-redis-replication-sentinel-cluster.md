---
slug: redis-replication-sentinel-cluster
baslik: "Redis Replication, Sentinel ve Cluster: Hash Slot, MOVED/ASK ve Async Failover Kaybı"
kademe: Uzman
sira: 163
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Tek bir Redis sunucusu iki noktada yetersiz kalır. Sunucu çökerse **hizmet durur**. Veri belleğe sığmazsa **büyüyemezsin**. Redis bu iki derdi üç ayrı araçla çözer. **Kopyalama (replication)** veriyi çoğaltır. **Sentinel**, çöken asıl sunucunun yerine kopyayı geçirir. **Cluster** ise veriyi birden çok sunucuya böler.

### Temel kavramlar

- **Replikasyon (çoğaltma)**: Bir sunucu **master** (asıl), diğerleri **replica** (kopya) olur; master'a yazılan her şey replikalara aktarılır. Replikalar varsayılan olarak **salt-okunur**dur.
- **Eşzamansız (asenkron) olmak**: Ana sunucu yazıyı uygular, istemciye hemen `OK` döner ve kopyalamayı **arka planda** yapar. Aradaki farka **gecikme (lag)** denir. Bu yüzden kopyadan okuyan biri az önce yazılmış değeri göremeyebilir; buna **bayat okuma (stale read)** denir.
- **Failover (nöbet devri)**: Master çökünce bir replikanın yeni master olarak **terfi ettirilmesi**.
- **Sentinel (nöbetçi)**: Ana sunucuyu izleyen ayrı süreçlerdir. Bir Sentinel ana sunucuya ulaşamayınca onu **kendi açısından** "down" işaretler. Nöbet devrinin başlaması için en az **yeter sayı (quorum)** kadar Sentinel'in hemfikir olması gerekir. Bu kural, tek bir Sentinel'in kendi ağ arızası yüzünden gereksiz nöbet devri başlatmasını önler. Sentinel veriyi **paylaştırmaz** ve `fsync` sıklığıyla ilgisi yoktur.
- **Cluster ve özet yuvası (hash slot)**: Cluster, anahtar uzayını **16384 sabit yuvaya** böler. Bir anahtarın yuvası **`CRC16(key) mod 16384`** ile hesaplanır. Bu hesap **belirlenimcidir (deterministik)**: aynı anahtar hep aynı yuvaya, dolayısıyla aynı düğüme düşer. Bu bir **tutarlı özet halkası (consistent hashing ring) değildir**. Yuva sayısının sabit olması, yeniden bölüştürmede (resharding) yuvaların bütün hâlinde taşınmasını kolaylaştırır.
- **Özet etiketi (hash tag)**: Anahtar adında `{...}` içine alınmış bir alt dize varsa yuva **yalnız o alt dizeden** hesaplanır. Böylece `user:{42}:name` ve `user:{42}:age` aynı yuvaya düşer. Cluster'da çok anahtarlı komutlar (`MGET`, `SUNION`…) ancak tüm anahtarlar aynı yuvadaysa çalışır; aksi hâlde `CROSSSLOT` hatası gelir.
- **MOVED ve ASK**: İstemci yanlış düğüme giderse **MOVED** yanıtı gelir. Bu, yuva sahipliğinin **kalıcı olarak** değiştiğini bildirir ve istemci yuva→düğüm haritasını günceller. **ASK** ise yuva **taşınırken** verilen, yalnız **o tek istek için** geçerli bir yönlendirmedir; harita değişmez.

```svg
<svg viewBox="0 0 640 216" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Replication, Sentinel, Cluster katmanları">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç ölçek/erişilebilirlik katmanı</text>
  <rect x="20" y="44" width="192" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="116" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Replication</text>
  <text x="116" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">master → replica</text>
  <text x="116" y="114" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ASENKRON</text>
  <text x="116" y="132" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">lag → stale read</text>
  <text x="116" y="160" text-anchor="middle" font-size="11" fill="#B4472B" font-family="system-ui">onaylı yazı</text>
  <text x="116" y="176" text-anchor="middle" font-size="11" fill="#B4472B" font-family="system-ui">kaybolabilir</text>
  <rect x="224" y="44" width="192" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Sentinel</text>
  <text x="320" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">izleme + failover</text>
  <text x="320" y="114" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">quorum ile 'down'</text>
  <text x="320" y="132" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">replikayı terfi eder</text>
  <text x="320" y="160" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">otomatik master</text>
  <text x="320" y="176" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">değişimi</text>
  <rect x="428" y="44" width="192" height="150" rx="12" fill="#F4EEF9" stroke="#6A3FA0" stroke-width="1.5"/>
  <text x="524" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#6A3FA0" font-family="system-ui">Cluster</text>
  <text x="524" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">16384 hash slot</text>
  <text x="524" y="114" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">CRC16(key) mod 16384</text>
  <text x="524" y="132" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">multi-key → hash tag</text>
  <text x="524" y="160" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">MOVED (kalıcı)</text>
  <text x="524" y="176" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ASK (geçici)</text>
</svg>
```

### Nasıl çalışır?

En sık atlanan tehlike şudur. Kopyalama **eşzamansız** olduğu için ana sunucu, yazıyı kopyaya aktarmadan istemciye `OK` dönebilir. Tam bu aralıkta ana sunucu çökerse ve Sentinel bir kopyayı terfi ettirirse, terfi eden kopyada o yazı **yoktur**. Yani **onay almış bir yazı kaybolur**.

Dahası, eski ana sunucu geri geldiğinde genellikle yeni ana sunucunun **kopyası** olur. Kendisindeki fazladan yazının üzerine yeni veri yazılır ve o yazı kaybolur; kendiliğinden bir kurtarma olmaz. `WAIT numreplicas timeout` komutu, yazının belirli sayıda kopyaya ulaşmasını bekleyerek riski **azaltır**; ama **tam ACID dayanıklılığı vermez**.

Kaybın nasıl oluştuğunu adım adım görmek gerekir. Ana sunucu `OK` döner, ama yazı henüz kopyaya aktarılmamıştır. Tam bu aralıkta ana sunucu çöker. Sentinel bir kopyayı terfi ettirir ve terfi eden kopyada o yazı bulunmaz:

```svg
<svg viewBox="0 0 640 176" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Async failover onaylı yazı kaybı zaman çizelgesi">
  <defs>
    <marker id="rrepl-step" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Async failover: onaylı yazı nasıl kaybolur</text>
  <rect x="10" y="52" width="142" height="86" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="81" y="76" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">1) yaz → OK</text>
  <text x="81" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">master onayladı</text>
  <text x="81" y="116" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">replikaya GİTMEDİ</text>
  <rect x="166" y="52" width="142" height="86" rx="9" fill="#FBEDE9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="237" y="86" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">2) master çöker</text>
  <text x="237" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">o an erişilemez</text>
  <rect x="322" y="52" width="142" height="86" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="393" y="80" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">3) Sentinel</text>
  <text x="393" y="100" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">replikayı terfi</text>
  <text x="393" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ettirir</text>
  <rect x="478" y="52" width="152" height="86" rx="9" fill="#FBEDE9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="554" y="80" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">4) yazı YOK</text>
  <text x="554" y="100" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yeni master'da</text>
  <text x="554" y="116" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">onaylı yazı kaybı</text>
  <line x1="153" y1="95" x2="164" y2="95" stroke="#5D7789" stroke-width="2" marker-end="url(#rrepl-step)"/>
  <line x1="309" y1="95" x2="320" y2="95" stroke="#5D7789" stroke-width="2" marker-end="url(#rrepl-step)"/>
  <line x1="465" y1="95" x2="476" y2="95" stroke="#5D7789" stroke-width="2" marker-end="url(#rrepl-step)"/>
  <text x="320" y="164" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">azaltma: WAIT numreplicas timeout (yine de tam ACID değil)</text>
</svg>
```

Cluster'ın anahtar yerleşimi ise belirlenimcidir. Her anahtar `CRC16(key) mod 16384` ile bir yuvaya düşer ve 16384 yuva düğümlere bölüştürülür. `{...}` özet etiketi ile farklı anahtarlar aynı yuvaya, dolayısıyla aynı düğüme getirilebilir. Yerleşim ne sırayla dağıtmaya (round-robin) ne de anahtarın bayt uzunluğuna bakar. `CLUSTER MEET` gibi topoloji komutları da bir anahtarın hangi yuvaya düşeceğini değiştirmez:

```svg
<svg viewBox="0 0 640 196" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cluster 16384 hash slot node dağıtımı ve hash tag">
  <defs>
    <marker id="rrepl-map" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Cluster: 16384 slot → node'lar</text>
  <rect x="24" y="38" width="120" height="40" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="84" y="63" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">anahtar</text>
  <rect x="196" y="38" width="228" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="310" y="63" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">CRC16(key) mod 16384</text>
  <rect x="476" y="38" width="140" height="40" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="546" y="63" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">slot no</text>
  <line x1="145" y1="58" x2="194" y2="58" stroke="#5D7789" stroke-width="2" marker-end="url(#rrepl-map)"/>
  <line x1="425" y1="58" x2="474" y2="58" stroke="#5D7789" stroke-width="2" marker-end="url(#rrepl-map)"/>
  <rect x="24" y="100" width="188" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="118" y="122" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Node A</text>
  <text x="118" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">slot 0–5460</text>
  <rect x="226" y="100" width="188" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="122" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Node B</text>
  <text x="320" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">slot 5461–10922</text>
  <rect x="428" y="100" width="188" height="44" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="522" y="122" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Node C</text>
  <text x="522" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">slot 10923–16383</text>
  <text x="320" y="172" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hash tag: user:{42}:name ve user:{42}:age → AYNI slot, AYNI node</text>
  <text x="320" y="188" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">böylece çok-anahtarlı işlem (MGET) çalışır</text>
</svg>
```

### Mülakatta ne sorulur?

(1) "Kopyadan okumanın riski nedir?" — Gecikme yüzünden bayat okuma. `MOVED` ile ilgisi yoktur; o bir Cluster yönlendirmesidir.

(2) "Yeter sayı (quorum) ne işe yarar?" — Nöbet devri kararı için gereken en az Sentinel mutabakatıdır. Yuva dağıtımı (Cluster) veya `appendfsync` (fsync sıklığı) ile karıştırılmamalıdır.

(3) "Anahtar hangi düğüme gider?" — `CRC16(key) mod 16384` hesabıyla belirlenen düğüme.

(4) "`MGET` neden `CROSSSLOT` verdi?" — Anahtarlar farklı yuvalardadır; çözüm özet etiketi kullanmaktır.

(5) "MOVED mı ASK mı?" — MOVED kalıcı harita güncellemesidir; ASK taşıma sırasında tek istek için verilen geçici yönlendirmedir.

(6) Klasik tuzak: "`OK` aldım, yazım güvende mi?" — Eşzamansız kopyalamada hayır.

### Tek cümlede

Kopyalama eşzamansız çalışır; bedeli bayat okuma ve nöbet devrinde onaylı yazı kaybı riskidir. Sentinel, yeter sayı mutabakatıyla otomatik nöbet devri yapar. Cluster ise veriyi 16384 özet yuvasına böler ve çok anahtarlı işlemleri özet etiketi kullanmaya mecbur eder.

# rrepl-1 | Uzman
## Soru
Master-replica **asenkron** replikasyonda bir replikadan okuma yaparken hangi risk vardır?
## A
Hiçbir risk yoktur; replika her okumada master ile birebir aynı içeriği döndürür
🤖 Asenkron replikasyonda gecikme kaçınılmazdır; replika anlık olarak master'ın gerisinde olabilir.
## B
Replika yazma isteklerini de kabul eder ve master ile çakışan sürümler üretir
🤖 Replikalar varsayılan salt-okunurdur; okuma sorusunda yazma çakışması konu dışıdır.
## C
Replikadan yapılan okumalar her zaman `MOVED` hatasıyla reddedilip master'a atılır
🤖 `MOVED` bir Cluster yönlendirmesidir; replikadan okuma böyle bir hata vermez.
## D
Replikasyon gecikmesi (lag) nedeniyle bayat (stale) veri okunabilir
🤖 Doğru: asenkron kopyalama, replika okumalarında bayat okuma riski yaratır.
## Doğru
D
## Açıklama
Redis replikasyonu **asenkron**tır: master yazıyı uygular ve istemciye onay dönerken, replikaya kopyalama **arka planda** olur. Bu, master ile replika arasında bir **gecikme (lag)** yaratır. Dolayısıyla bir replikadan okuma yapıldığında, master'a az önce yazılmış değer henüz replikaya ulaşmamışsa **bayat (stale)** veri okunabilir (D). Replika hep güncel değildir (A yanlış); replikalar varsayılan salt-okunurdur, yazma çakışması bu senaryonun konusu değildir (B yanlış); `MOVED` (C) Cluster yönlendirme yanıtıdır, replika okumasıyla ilgisizdir.

# rrepl-2 | Uzman
## Soru
Redis **Sentinel**'de `quorum` parametresi ne işe yarar?
## A
Veriyi node'lar arasında hash slot'lara bölüştürüp dağıtımı yönetmek için kullanılır
🤖 Slot bölüştürme Cluster'ın işidir; Sentinel veri paylaştırmaz, izleme ve failover yapar.
## B
Master'ı 'down' ilan edip failover başlatmak için gereken en az Sentinel sayısıdır
🤖 Doğru: quorum, failover kararı için gereken minimum mutabakat sayısıdır.
## C
Bir anahtarın kaç hash slot'a birden yayılacağını belirleyen çoğaltma katsayısıdır
🤖 Anahtar tek slota düşer; bu kavram Sentinel'e değil Cluster'a ve yanlış bir tanıma aittir.
## D
AOF `fsync` işleminin saniyede kaç kez yapılacağını ayarlayan dayanıklılık parametresidir
🤖 fsync sıklığı `appendfsync` ile ayarlanır; quorum ile hiçbir ilgisi yoktur.
## Doğru
B
## Açıklama
**Sentinel**, master'ı izleyen ayrı süreçlerdir. Bir Sentinel master'a ulaşamayınca onu **öznel olarak** down işaretler; ancak **failover** başlatılabilmesi için en az **quorum** kadar Sentinel'in master'ın erişilemez olduğunda **hemfikir** olması gerekir (B). Bu, tek bir Sentinel'in ağ arızası yüzünden gereksiz failover tetiklemesini önler. Quorum, veri paylaştırma (A, Cluster'ın işi), anahtar-slot ilişkisi (C, yanlış tanım) veya `fsync` sıklığı (D, `appendfsync`) ile ilgili değildir.

# rrepl-3 | Uzman
## Soru
Redis **Cluster**, bir anahtarın hangi node'a gideceğini nasıl belirler?
## A
`CRC16(key) mod 16384` ile hash slot hesaplar; anahtar slotun sahibi node'a gider
🤖 Doğru: sabit 16384 slot ve CRC16 tabanlı eşleme Cluster'ın çekirdek mekanizmasıdır.
## B
Tutarlı hash halkası (consistent hashing ring) üzerinde saat yönünde en yakın node'a yerleştirir
🤖 Redis Cluster halka değil, sabit 16384 slotlu bölümleme kullanır; consistent hashing değildir.
## C
Anahtarları yazıldıkları sıraya göre node'lara sırayla (round-robin) dağıtır
🤖 Round-robin deterministik konumlandırma vermez; aynı anahtar hep aynı slota düşmelidir.
## D
Anahtarın bayt uzunluğuna bakıp uzunluk aralığına karşılık gelen node'u seçer
🤖 Konum uzunluğa değil, anahtar içeriğinin CRC16 hash değerine bağlıdır.
## Doğru
A
## Açıklama
Redis Cluster anahtar alanını **16384 sabit hash slot**'a böler. Bir anahtarın slotu **`CRC16(key) mod 16384`** ile hesaplanır; her slot bir master node'a atanmıştır ve anahtar, slotunun **sahibi** node'a yönlendirilir (A). Bu deterministiktir: aynı anahtar her zaman aynı slota, dolayısıyla aynı node'a düşer. Redis **consistent hashing halkası kullanmaz** (B yanlış); dağıtım round-robin (C) veya anahtar uzunluğu (D) tabanlı değildir. Sabit slot sayısı, resharding'de slotların bütün hâlinde node'lar arası taşınmasını kolaylaştırır.

# rrepl-4 | Uzman
## Soru
Redis Cluster'da `MGET k1 k2` iki anahtar **farklı slotlara** düşünce hata verir. İki anahtarı aynı slotta tutup çok-anahtarlı işlem yapmanın yolu nedir?
## A
Cluster'da çok-anahtarlı işlem hiçbir koşulda mümkün değildir; anahtarlar tek tek okunmalıdır
🤖 Mümkündür: anahtarlar aynı slota düşerse çok-anahtarlı işlem çalışır; çözüm hash tag'dir.
## B
İki anahtarı da tamamen aynı isimle adlandırıp aynı slota düşmelerini sağlamak
🤖 Aynı isim tek anahtar demektir; amaç FARKLI anahtarları aynı slota getirmektir.
## C
Hash tag: `{...}` içine alınan ortak kısım slot hesabına girer
🤖 Doğru: `{...}` ile ortak etiket kullanılan anahtarlar aynı slota yönlenir.
## D
`CLUSTER MEET` komutunu çağırıp node'ları birleştirerek slot sınırlarını tamamen kaldırmak
🤖 `CLUSTER MEET` node'ları cluster'a ekler; anahtarları aynı slota getirmez.
## Doğru
C
## Açıklama
Cluster'da çok-anahtarlı komutlar (`MGET`, `SUNION`, `MULTI` içinde çok anahtar…) yalnız tüm anahtarlar **aynı slotta** ise çalışır; aksi hâlde `CROSSSLOT` hatası döner. Çözüm **hash tag**'dir: anahtar adında **`{...}`** içine alınan alt dize varsa, slot yalnız o alt dizeden hesaplanır. Böylece `user:{42}:name` ve `user:{42}:age` **aynı slota** (dolayısıyla aynı node'a) düşürülüp birlikte işlenebilir (C). Çok-anahtarlı işlem imkânsız değildir (A); aynı isim vermek (B) tek anahtar demektir; `CLUSTER MEET` (D) topoloji komutudur, anahtar yerleşimini değiştirmez.

# rrepl-5 | Uzman
## Soru
Cluster'da istemci yanlış node'a komut gönderince gelen **MOVED** ile **ASK** yanıtları arasındaki fark nedir?
## A
İkisi de tamamen aynı anlama gelir; yalnız Redis sürümlerine göre isimleri değişmiştir
🤖 Anlamları farklıdır: biri kalıcı sahiplik değişimi, diğeri göç sırasında geçici yönlendirmedir.
## B
MOVED geçici bir yönlendirmedir, ASK ise kalıcı sahiplik değişimini bildirir
🤖 Tam tersi doğrudur: MOVED kalıcıdır, ASK ise yalnız o istek için geçicidir.
## C
ASK slot sahipliğinin kalıcı değiştiğini söyler ve istemcinin haritasını güncellemesini ister
🤖 Bu tarif MOVED'e aittir; ASK yalnız o tek istek için geçici bir yönlendirmedir.
## D
MOVED slot sahipliğinin kalıcı değiştiğini, ASK ise göç sırasında geçici yönlendirmeyi bildirir
🤖 Doğru: MOVED kalıcı harita güncellemesi, ASK tek-istek geçici yönlendirmedir.
## Doğru
D
## Açıklama
**MOVED**, slotun sahipliğinin **kalıcı** olarak başka node'a geçtiğini bildirir; istemci kendi **slot→node haritasını günceller** ve sonraki tüm istekleri doğrudan yeni node'a gönderir. **ASK** ise bir slot **göç (resharding) hâlindeyken** ortaya çıkar: sorgulanan anahtar hedef node'a taşınmış olabilir, bu yüzden istemci **yalnız o tek istek için** hedef node'a `ASKING` ile yönlendirilir; haritasını kalıcı olarak değiştirmez, çünkü göç henüz bitmemiştir (D). B yönleri ters çevirir; C, ASK'ı MOVED gibi tanımlar; A ikisini eşitler.

# rrepl-6 | Uzman
## Soru
[TUZAK] Asenkron replikasyonlu bir Redis'te bir anahtara yazdın ve `OK` aldın; hemen ardından master çöktü ve Sentinel bir replikayı terfi ettirdi. **Onay aldığın yazıya** ne olabilir?
## A
Kesinlikle korunur; `OK` yanıtı yazının her koşulda kalıcı olduğunu garanti eder
🤖 Asenkron replikasyonda `OK`, replikaya kopyalanmayı garanti etmez; failover'da yazı kaybolabilir.
## B
Kaybolabilir: onay geldi ama replikaya kopyalanmadan çöktüyse yeni master'da yoktur
🤖 Doğru: asenkron kopyalama nedeniyle onaylı yazı, terfi eden replikada bulunmayabilir.
## C
Eski master geri gelince yazı otomatik olarak yeni master'a aktarılıp kurtarılır
🤖 Eski master genelde yeni master'ın replikası olur ve fazlalık yazısını KAYBEDER; otomatik kurtarma olmaz.
## D
Yazı `MOVED` yanıtı üzerinden yönlendirilen yeni node'da bulunur ve okunabilir
🤖 `MOVED` yalnız bir slotun yerini bildirir; kopyalanmamış yazıyı geri getirmez.
## Doğru
B
## Açıklama
Bu klasik tuzağın özü: replikasyon **asenkron** olduğundan master, yazıyı replikaya **kopyalamadan** istemciye `OK` dönebilir. Master bu aralıkta çöker ve Sentinel bir replikayı terfi ettirirse, terfi eden replikada o yazı **yoktur** — yani **onay almış yazı kaybolur** (B). Dahası, eski master geri geldiğinde genellikle yeni master'ın **replikası** olur ve kendindeki fazladan yazıyı **üzerine yazılarak kaybeder** (C yanlış). `OK` almak dayanıklılık garantisi değildir (A yanlış); `MOVED` (D) yalnız slot konumunu bildirir. Risk, `WAIT numreplicas timeout` ile azaltılabilir (yazının belirli sayıda replikaya ulaşmasını beklemek) ama bu bile tam ACID dayanıklılığı sağlamaz.
