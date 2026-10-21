---
baslik: "System Design Interview Hazırlığı"
yazar: "Serpiştirme — Capstone yanı"
tarih: "2026-07-10"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 12
---

### Bu metin ne anlatıyor?
İş görüşmelerinde bazen şöyle bir soru sorulur: "Milyonlarca insanın kullanacağı bir sistemi
nasıl kurardın?" Tek bir doğru cevabı olmayan bu soruda ölçülen şey, bilgi değil **düşünme
düzenidir**. Bu metin o düzeni sabit bir sıraya bağlıyor: önce ne istendiğini netleştir,
sonra kaba bir büyüklük hesabı yap, kutuları çiz, birini derinleştir, en sonda "trafik on
kat artarsa ne kırılır" diye sor. Aynı sıra, kendi bitirme projeni tasarlarken de işine
yarar.

### Bu metinde geçen temel terimler
- **Gecikme** (latency): Bir isteğin cevabının gelmesi ne kadar sürüyor. Milisaniye ile
  ölçülür.
- **Verim / hacim** (throughput): Sistemin birim zamanda kaç işi bitirebildiği.
- **QPS** (queries per second, saniyedeki istek): En sık kullanılan hacim ölçüsü.
- **p95 / p99**: "En kötü yüzde 5'i / yüzde 1'i dışarıda bırakınca gecikme kaç?" Ortalama
  yanıltır; kullanıcı deneyimini bu uç değerler belirler. "p95 < 1 sn" demek, isteklerin
  %95'i bir saniyenin altında yanıtlanıyor demektir.
- **Kullanılabilirlik** (availability): Sistemin ayakta olduğu zamanın oranı (%99,9 gibi).
- **Tutarlılık** (consistency): Aynı anda bakan iki kullanıcının aynı veriyi görüp görmemesi.
- **Ödünleşim / takas** (trade-off): Bir şeyi kazanmak için başka bir şeyden vazgeçmek.
  Mimaride her kararın bir takası vardır; takassız cevap, düşünülmemiş cevaptır.

## Neden bu özet
Sistem tasarımı mülakatı (system design interview) tek bir "doğru cevabı" ölçmez;
**kısıt altında trade-off (ödünleşim) gösterebilme** yetini ölçer. İyi aday, mükemmel
mimariyi ezberden dökmez — belirsizliği soruyla daraltır, birkaç seçeneği tartar,
birini gerekçesiyle seçer. Bu özet, o düşünme sürecini bir iskelet + üç diyagramla
verir; capstone projeni tasarlarken de aynı adımlar işe yarar.

## Türkçe çalışma metni

### 1. Beş adımlık iskelet
Panik yapmadan ilerlemenin yolu sabit bir sıra izlemektir. Her adım bir öncekinin
üstüne biner:

1. **Gereksinim netleştir (requirements).** İki tür var:
   - *Fonksiyonel (functional):* sistem NE yapmalı? (ör. "kısa link üret, tıklayınca
     yönlendir").
   - *Fonksiyonel olmayan (non-functional):* NASIL yapmalı? Ölçek, gecikme (latency),
     erişilebilirlik (availability), tutarlılık (consistency). Sayı iste:
     "Günde kaç istek? Okuma/yazma oranı?"
2. **Kaba tahmin (back-of-the-envelope estimation).** Trafiği ve depolamayı kabaca
   hesapla: saniyedeki istek (QPS = queries per second), günlük yeni kayıt, 5 yıllık
   depolama. Amaç kesinlik değil, **büyüklük mertebesini** (order of magnitude)
   bilmek — bu, sonraki kararları yönlendirir.
3. **Üst-seviye tasarım (high-level design).** Ana bileşenleri kutu-ok olarak çiz:
   istemci, API, uygulama katmanı, veri deposu. Önce basit tut.
4. **Derinleş (deep dive).** Mülakatçının ilgilendiği bir parçayı aç: veri şeması,
   API imzası, kritik bir algoritma (ör. benzersiz kısa kod üretimi).
5. **Darboğaz / ölçek (bottleneck & scale).** "Trafik 10 kat artarsa ne kırılır?"
   diye sor ve yapı taşlarını (cache, replika, shard) devreye alarak çöz. Sonda
   **1-2 cümleyle özetle**: hangi kararı neden verdin.

Bu beş adımı akılda tutmanın kolay yolu, altı kelimelik bir kontrol listesidir:

> **gereksinim → kapasite → API → veri → ölçek → takas**

Eşlemesi şudur: *gereksinim* 1. adım; *kapasite* 2. adım; *API* ve *veri* 3-4. adımın
somut çıktılarıdır (kutu-ok çizimi API sözleşmesine ve veri modeline dönüşür); *ölçek*
5. adım; *takas* ise ayrı bir adım değil, **her adımda söylemen gereken cümledir** — ve
sonda özetlenir. Adı geçen altı şeyden birini hiç konuşmadıysan, tasarım eksiktir.

```svg
<svg viewBox="0 0 680 150" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sistem tasarımı mülakatının beş adımı: gereksinim, tahmin, üst-seviye tasarım, derinleş, darboğaz ve ölçek">
  <defs>
    <marker id="sdi-ok1" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="340" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Mülakatın beş adımı — sırayla ilerle</text>
  <rect x="8" y="52" width="118" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="67" y="78" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">1 Gereksinim</text>
  <text x="67" y="96" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">fonk. + fonk-olmayan</text>
  <rect x="144" y="52" width="118" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="203" y="78" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">2 Tahmin</text>
  <text x="203" y="96" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">trafik · depolama</text>
  <rect x="280" y="52" width="118" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="339" y="78" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">3 Üst-seviye</text>
  <text x="339" y="96" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kutu-ok tasarım</text>
  <rect x="416" y="52" width="118" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="475" y="78" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">4 Derinleş</text>
  <text x="475" y="96" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">şema · API · algoritma</text>
  <rect x="552" y="52" width="120" height="60" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="612" y="78" text-anchor="middle" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">5 Darboğaz</text>
  <text x="612" y="96" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ölçek + özet</text>
  <line x1="126" y1="82" x2="142" y2="82" stroke="#186399" stroke-width="1.8" marker-end="url(#sdi-ok1)"/>
  <line x1="262" y1="82" x2="278" y2="82" stroke="#186399" stroke-width="1.8" marker-end="url(#sdi-ok1)"/>
  <line x1="398" y1="82" x2="414" y2="82" stroke="#186399" stroke-width="1.8" marker-end="url(#sdi-ok1)"/>
  <line x1="534" y1="82" x2="550" y2="82" stroke="#186399" stroke-width="1.8" marker-end="url(#sdi-ok1)"/>
  <text x="340" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Her adım bir öncekinin üstüne biner — atlamadan ilerle</text>
</svg>
```

#### 1.1. Kapasite tahmini — çalışılmış örnek
"Kaba tahmin" korkutucu görünür ama üç bölme işleminden ibarettir. URL kısaltıcı örneğiyle:

- **Varsayım (yüksek sesle söyle):** günde 10 milyon yeni link üretiliyor; her linke
  ortalama 10 tıklama geliyor.
- **Yazma hızı:** 10.000.000 ÷ 86.400 saniye ≈ **115 yazma/saniye**.
- **Okuma hızı:** tıklamalar 10 katı → ≈ **1.150 okuma/saniye**. Buradan çıkan tasarım
  sonucu: **sistem okuma ağırlıklı** (yaklaşık 1'e 10). Bu tek cümle, cache'i neden öne
  koyduğunu tek başına gerekçelendirir.
- **Tepe yükü:** ortalama düz değildir; günün yoğun saatinde ortalamanın 2-3 katını bekle
  → ~3.500 okuma/saniye tasarım hedefi.
- **Depolama:** kayıt başına ~500 bayt (kısa kod, uzun adres, sahip, tarih) × günde 10 M =
  ~5 GB/gün → yılda ~1,8 TB, 5 yılda ~9 TB. Sonuç: tek makineye sığmaz, **parçalama
  (sharding) gündeme gelir** — bunu tahmin sayesinde biliyorsun, tahminde bulunmadan değil.

Dikkat: burada amaç doğru sayıyı bulmak değil, **doğru büyüklük mertebesini** bulmaktır.
"115 mi 120 mi" fark etmez; "115 mi 115.000 mi" her şeyi değiştirir. Varsayımlarını açıkça
söyle — yanlış varsayım kabul edilebilir, gizli varsayım kabul edilemez.

#### 1.2. API sözleşmesi — kutuların arasına ne yazıyor?
Kutu-ok çizimi tek başına eksiktir; kutuların birbirine ne söylediğini de yaz. İki-üç satır
yeter:

- `POST /linkler` → gövde: `{ "uzunAdres": "..." }` → yanıt `201` + `{ "kisaKod": "aB3xY" }`
- `GET /{kisaKod}` → yanıt `301` (kalıcı yönlendirme) veya `302` (geçici yönlendirme) +
  hedef adres; bulunamazsa `404`

Bu iki satır bile bir takas doğurur ve mülakatçı tam bunu bekler: **301** tarayıcıya
"bunu ezberle" der — sonraki tıklamalar sunucuna hiç uğramaz, yükün düşer ama **tıklama
sayacın tutmaz**. **302** her tıklamada sunucuya uğrar — istatistiğin doğru olur, yükün
artar. Hangisini seçtiğini ve neden seçtiğini söylemek, tasarımın kendisinden daha
değerlidir.

#### 1.3. Veri modeli — neyi, nerede, hangi anahtarla saklıyorsun?
Üçüncü somut çıktı veri modelidir. Yine birkaç satır yeter:

- Tablo/koleksiyon: `link(kisa_kod PK, uzun_adres, sahip_id, olusturma_tarihi, son_kullanma)`
- **Erişim deseni:** neredeyse tüm okumalar `kisa_kod` üzerinden tekil sorgudur → anahtar-değer
  erişimi baskın; karmaşık birleştirme (join) yok.
- Bu desen, "SQL mi NoSQL mi?" sorusunun cevabını gerekçelendirir: ilişkisel bir veritabanı
  da işi görür, ama tek-anahtarla okuma ağırlıklı bu profil anahtar-değer deposuna doğal
  uyar. **Kararı desene bağla, modaya değil.**
- **Parçalama anahtarı** (shard key) seçimi de burada konuşulur: `kisa_kod`'a göre bölmek
  yükü düzgün dağıtır; `sahip_id`'ye göre bölmek ise bir kullanıcının tüm linklerini bir
  arada tutar ama popüler bir hesap tek parçayı ısıtır (**sıcak parça**, hot shard sorunu).

#### 1.4. Takası cümleye dök
Her büyük karardan sonra tek bir kalıba sadık kal:

> "**X**'i seçtim; **Y**'yi kazandım, **Z**'den vazgeçtim, çünkü **bu sistemde Y daha
> ağır basıyor**."

Örnek: "Okuma yolunda cache kullanıyorum; gecikmeyi ve veritabanı yükünü kazandım, verinin
anlık tazeliğinden vazgeçtim — çünkü bir kısaltılmış link nadiren değişir." Bu cümleyi
kurabiliyorsan kararı gerçekten vermişsindir; kuramıyorsan henüz bir tercih değil, bir
alışkanlık uyguluyorsun.

### 2. Yapı taşları (building blocks)
Neredeyse her tasarım aynı birkaç parçadan kurulur. Ne işe yaradıklarını ve hangi
darboğazı çözdüklerini bilmek yeter:

- **Yük dengeleyici (load balancer):** gelen istekleri birden çok sunucuya dağıtır;
  tek sunucunun boğulmasını ve tek nokta arızasını (single point of failure) önler.
- **Önbellek (cache):** sık okunan veriyi bellekte (RAM) tutar; veritabanı yükünü ve
  gecikmeyi düşürür. Bedeli: bayat veri (stale data) riski — geçersizleştirme
  (cache invalidation) zor bir problemdir.
- **CDN (içerik dağıtım ağı):** statik içeriği (resim, JS, CSS) kullanıcıya coğrafi
  olarak yakın kenar sunuculardan (edge) servis eder; gecikmeyi ve merkezi trafiği azaltır.
- **Kuyruk (queue / message queue):** yazma ve işleme adımlarını birbirinden ayırır
  (decoupling); ani yük tepelerini tamponlar, işleri asenkron yapar (ör. e-posta gönderimi).
- **Replika (replica):** verinin okuma amaçlı kopyaları; okuma trafiğini ölçekler,
  yedeklilik (redundancy) sağlar. Genelde yazma tek düğüme (**primary** — yazmayı kabul eden
  ana kopya), okuma replikalara gider. Bedeli: kopyaya ulaşmak zaman alır, bu yüzden
  replikadan okuyan kullanıcı bir an için **eski veriyi** görebilir (replikasyon gecikmesi).
- **Shard (parçalama / sharding):** veriyi anahtara göre birden çok veritabanına böler
  (ör. kullanıcı kimliğine göre). Tek veritabanına sığmayan veri ve yazma yükünü ölçekler;
  bedeli: parçalar arası sorgu ve yeniden dengeleme (rebalancing) zorlaşır.

Aşağıdaki örnek — bir URL kısaltıcı (URL shortener) — bu taşların çoğunu bir araya getirir.
Okuma yolu (tıklama → yönlendirme) yazma yolundan (yeni link üret) çok daha yoğundur;
bu yüzden okumada cache öne çıkar.

```svg
<svg viewBox="0 0 680 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="URL kısaltıcı örnek mimarisi: istemci, yük dengeleyici, uygulama sunucusu, önbellek ve veritabanı; okuma ve yazma yolları">
  <defs>
    <marker id="sdi-ok2" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="sdi-ok2r" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
    <marker id="sdi-ok2w" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#C77A17"/></marker>
  </defs>
  <text x="340" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Örnek: URL kısaltıcı — okuma/yazma yolu</text>
  <rect x="16" y="98" width="108" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="70" y="122" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">İstemci</text>
  <text x="70" y="140" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(client)</text>
  <rect x="160" y="98" width="120" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="220" y="120" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Yük Dengeleyici</text>
  <text x="220" y="138" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(load balancer)</text>
  <rect x="316" y="98" width="120" height="56" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="376" y="120" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">Uygulama</text>
  <text x="376" y="138" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(app server)</text>
  <rect x="520" y="46" width="144" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="592" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Önbellek (cache)</text>
  <text x="592" y="88" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sıcak linkler</text>
  <rect x="520" y="150" width="144" height="56" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="592" y="174" text-anchor="middle" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">Veritabanı (DB)</text>
  <text x="592" y="192" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kalıcı kayıt</text>
  <line x1="124" y1="126" x2="158" y2="126" stroke="#186399" stroke-width="1.8" marker-end="url(#sdi-ok2)"/>
  <line x1="280" y1="126" x2="314" y2="126" stroke="#186399" stroke-width="1.8" marker-end="url(#sdi-ok2)"/>
  <line x1="436" y1="112" x2="518" y2="80" stroke="#25804C" stroke-width="1.8" marker-end="url(#sdi-ok2r)"/>
  <text x="474" y="88" text-anchor="middle" font-size="10.5" font-weight="600" fill="#25804C" font-family="system-ui">okuma →</text>
  <line x1="436" y1="140" x2="518" y2="172" stroke="#C77A17" stroke-width="1.8" marker-end="url(#sdi-ok2w)"/>
  <text x="474" y="168" text-anchor="middle" font-size="10.5" font-weight="600" fill="#C77A17" font-family="system-ui">yazma →</text>
  <text x="340" y="232" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Okuma önce cache'e bakar (yoksa DB); yazma doğrudan DB'ye — okuma yükü yazmadan çok fazladır</text>
</svg>
```

### 3. Ölçek merdiveni (scaling ladder)
Ölçekleme tek hamlede olmaz; her basamak bir önceki darboğazı çözer ve yenisini açar.
"Önce cache mi, önce shard mı?" sorusunun cevabı: **hangi darboğaz önce yanıyor?**

- **Tek sunucu:** basit, ucuz. Darboğaz: trafik artınca CPU/bellek doluyor, tek arıza
  her şeyi düşürüyor.
- **+ Cache:** tekrar eden okumaları bellekten ver → veritabanı rahatlar. Darboğaz:
  yazma ve soğuk okuma hâlâ tek DB'ye gidiyor.
- **+ Okuma replikası:** okumayı kopyalara dağıt → okuma ölçeklenir. Darboğaz: tüm
  yazma hâlâ tek primary'de birikiyor.
- **+ Sharding:** veriyi anahtara göre böl → yazma da ölçeklenir, veri tek makineye
  sığmasa da olur. Darboğaz: statik içerik ve coğrafi gecikme.
- **+ CDN:** statik varlıkları kullanıcıya yakın kenardan ver → gecikme düşer, merkez
  trafiği azalır.

```svg
<svg viewBox="0 0 680 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ölçek merdiveni: tek sunucu, artı önbellek, artı okuma replikası, artı sharding, artı CDN; her basamak bir darboğazı çözer">
  <defs>
    <marker id="sdi-ok3" markerWidth="9" markerHeight="9" refX="6" refY="3" orient="auto"><path d="M0,0 L6,3 L0,6 Z" fill="#C77A17"/></marker>
  </defs>
  <text x="340" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Ölçek merdiveni — her basamak bir darboğazı çözer</text>
  <rect x="16" y="208" width="120" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="76" y="230" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Tek sunucu</text>
  <text x="76" y="248" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">basit · tek arıza</text>
  <rect x="148" y="168" width="120" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="208" y="190" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">+ Cache</text>
  <text x="208" y="208" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">okuma yükü ↓</text>
  <rect x="280" y="128" width="120" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="340" y="150" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">+ Okuma replikası</text>
  <text x="340" y="168" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">okuma ölçeklenir</text>
  <rect x="412" y="88" width="120" height="52" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="472" y="110" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">+ Sharding</text>
  <text x="472" y="128" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yazma ölçeklenir</text>
  <rect x="544" y="48" width="120" height="52" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="604" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">+ CDN</text>
  <text x="604" y="88" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">gecikme ↓</text>
  <line x1="136" y1="220" x2="146" y2="212" stroke="#C77A17" stroke-width="1.8" marker-end="url(#sdi-ok3)"/>
  <line x1="268" y1="180" x2="278" y2="172" stroke="#C77A17" stroke-width="1.8" marker-end="url(#sdi-ok3)"/>
  <line x1="400" y1="140" x2="410" y2="132" stroke="#C77A17" stroke-width="1.8" marker-end="url(#sdi-ok3)"/>
  <line x1="532" y1="100" x2="542" y2="92" stroke="#C77A17" stroke-width="1.8" marker-end="url(#sdi-ok3)"/>
  <text x="340" y="286" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Basamağı ancak alttaki darboğaz yandığında ekle — erken karmaşıklık maliyettir</text>
</svg>
```

### 4. Sık tuzaklar (yaygın hatalar)
Mülakatı kaybettiren şey genelde bilgi eksikliği değil, süreç hatasıdır:

- **Netleştirmeden dalma:** soruyu duyar duymaz kutu çizmeye başlamak. Önce gereksinimi
  ve ölçeği sor; yanlış problemi mükemmel çözmek sıfır puandır.
- **Sayı vermeme:** "çok trafik olur" demek yetmez. Kaba tahmin yap (QPS, depolama) —
  sayılar kararlarını gerekçelendirir ve mühendislik olgunluğu gösterir.
- **Tek çözüme saplanma:** ilk aklına geleni savunmak. En az iki seçenek sun, trade-off'ları
  söyle (ör. SQL vs NoSQL, güçlü vs nihai tutarlılık) ve **neden** birini seçtiğini belirt.
- **Sessiz düşünme:** mülakatçı kafandaki akıl yürütmeyi göremez; yüksek sesle düşün.
- **Erken aşırı mühendislik:** daha ilk dakikada global sharding + Kafka çizmek. Basit
  başla, darboğaz çıktıkça büyüt (bkz. ölçek merdiveni).
- **API ve veri modelini atlamak:** yalnızca kutu-ok çizip "bileşenler bunlar" demek.
  Kutuların arasında hangi çağrının gittiğini ve verinin hangi anahtarla saklandığını
  söylemedin mi, tasarım havada kalır (§1.2, §1.3).
- **Takası söylememek:** doğru bileşeni seçip neden seçtiğini söylememek. Mülakatçı seçimi
  değil, **gerekçeyi** puanlar.

## Öz-sınav (kendini test et)
1. Fonksiyonel ve fonksiyonel olmayan gereksinim arasındaki farkı bir URL kısaltıcı
   örneğiyle açıkla.
2. Kaba tahmin (back-of-the-envelope) adımını neden atlamamalısın? Hangi kararları etkiler?
3. Cache, okuma replikası ve sharding sırasıyla hangi farklı darboğazı çözer?
4. Bir sistemde "önce cache mi, önce shard mı eklerim?" kararını neye göre verirsin?
5. Mülakatta tek bir çözümü savunmak neden risktir; bunun yerine ne yaparsın?
6. "gereksinim → kapasite → API → veri → ölçek → takas" listesini ezberden say ve her
   maddenin beş adımın hangisine denk geldiğini eşle.
7. Günde 10 milyon yeni kayıt ve kayıt başına 10 okuma varsayımıyla yazma ve okuma hızını
   (saniyede) hesapla. Çıkan orandan tasarım için hangi tek sonucu çıkarırsın?
8. Kısa link yönlendirmesinde `301` yerine `302` seçmenin kazandırdığı ve kaybettirdiği
   nedir? Bu kararı hangi gereksinim belirler?
9. Parçalama anahtarını (shard key) `sahip_id` seçersen "sıcak parça" (hot shard) sorunu
   nasıl doğar? Alternatif anahtar seçimi bunu nasıl değiştirir?
10. "X'i seçtim; Y'yi kazandım, Z'den vazgeçtim, çünkü…" kalıbını kendi projendeki bir
    karara uygula ve tek cümlede yaz.
