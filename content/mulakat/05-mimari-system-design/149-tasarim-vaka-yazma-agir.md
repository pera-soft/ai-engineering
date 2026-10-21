---
slug: tasarim-vaka-yazma-agir
baslik: "Tasarım Vakası (Yazma-Ağır): URL Kısaltıcı, Rate Limiter, Fanout ve Write Amplification"
kademe: İleri
sira: 149
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir uygulamayı kullanırken telefonun sunucuya küçük bir not gönderir: "şu uzun bağlantıyı kısalt", "şu gönderiyi kaydet". Bu nota **istek** (request), sunucunun geri gönderdiği cevaba **yanıt** (response) denir. İstek gidip yanıt gelene kadar geçen süreye **gecikme (latency)** denir. Sistemin bir saniyede bitirebildiği istek sayısına ise **iş hacmi (throughput)** denir.

İstekler kabaca ikiye ayrılır: veri **okuyanlar** ("bana şunu göster") ve veri **yazanlar** ("şunu kaydet"). Yazma isteklerinin baskın olduğu sistemlere **yazma-ağır (write-heavy)** denir. URL kısaltıcı, bildirim servisi, tıklama sayacı ve gönderi akışı böyledir. Yazma, okumadan pahalıdır. Çünkü veri hem diske kalıcı yazılır (**kalıcılaştırma**) hem de yedek kopyalara taşınır (**kopyalama / replikasyon**). Bu konu, yazmanın pahalı olduğu bir sistemin nasıl ayakta kaldığını anlatır.

### Temel kavramlar

- **Ekleme günlüğü (append-only log / WAL)**: veriyi defterin ortasına sıkıştırmak yerine yalnız sonuna satır eklemek. Disk için en ucuz ve maliyeti sabit yazma budur.
- **Eşzamansız (asenkron) iş**: "şimdi değil, arkada yap" demektir. Kullanıcı hemen yanıt alır, ağır iş arka planda tamamlanır. Bedeli şudur: veri bir süre her yerde aynı olmaz. Buna **nihai tutarlılık (eventual consistency)** denir — kopyalar bir süre sonra eşitlenir, ama o ana kadar farklı cevaplar dönebilir.
- **Fanout (dağıtım)**: tek bir olayı çok sayıda alıcıya ulaştırmak.
- **Yazma çoğullanması (write amplification)**: kullanıcı için tek olan bir yazmanın diske birden çok kez düşmesi.
- **Tekrar edilse de sonucu değişmezlik (idempotentlik)**: aynı isteği ikinci kez işlemenin sonucu birincisiyle aynıdır. İkinci bir kayıt oluşmaz.
- **SPOF (tek arıza noktası)**: düştüğünde tüm sistemi durduran tekil bileşen; merkezî tek sayaç bunun klasik örneğidir.

### Nasıl çalışır?

İlk büyük karar **dağıtım (fanout)** yönüdür. Bir gönderi milyonlarca takipçiye ulaşacaksa iki uç vardır.

**Yazarken dağıt (fanout-on-write / push)**: gönderi anında her takipçinin kutusuna kopyalanır. Okuma çok ucuzdur; ama tek bir gönderi milyonlarca yazma tetikler. **Okurken topla (fanout-on-read / pull)**: yazma tek satırdır. Buna karşılık akış her açıldığında içerik kaynaklardan toplanır.

Ünlü hesap (celebrity) sorununda pratik çözüm **hibrit** yaklaşımdır: sıradan hesaplar için yazarken dağıt, çok takipçili hesaplar için okurken topla.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Fanout: yazarken dağıt vs okurken topla">
  <defs>
    <marker id="wca-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0,0 L6,3 L0,6 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Fanout kararı: yükü nereye koyarsın?</text>
  <rect x="28" y="44" width="276" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="166" y="70" text-anchor="middle" font-size="13.5" font-weight="700" fill="#186399" font-family="system-ui">Yazarken dağıt (push)</text>
  <text x="166" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">1 gönderi → N takipçi kutusuna yaz</text>
  <text x="166" y="116" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">okuma UCUZ (hazır akış)</text>
  <text x="166" y="136" text-anchor="middle" font-size="11" fill="#B4531F" font-family="system-ui">yazma PAHALI (amplification)</text>
  <text x="166" y="164" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ünlü hesapta patlar</text>
  <rect x="336" y="44" width="276" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="474" y="70" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">Okurken topla (pull)</text>
  <text x="474" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">akış istenince kaynaklardan çek</text>
  <text x="474" y="116" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">yazma UCUZ (tek satır)</text>
  <text x="474" y="136" text-anchor="middle" font-size="11" fill="#B4531F" font-family="system-ui">okuma PAHALI (her seferinde)</text>
  <text x="474" y="164" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hibrit: ünlüler pull, gerisi push</text>
</svg>
```

İkinci karar **kritik yolu kısaltmaktır**. İstek geldiğinde yapılması şart olan iş ile ertelenebilir iş birbirinden ayrılır. Veri önce hızlı bir günlüğe (WAL / commit log) tek satır olarak eklenir ve istemciye hemen onay (ack) döner. Kopyalama, indeksleme ve dağıtım gibi ağır işler eşzamanlı yoldan çıkarılıp arkada işlenir. Gecikme düşer, iş hacmi artar. Karşılığında tutarlılık nihai olur.

```svg
<svg viewBox="0 0 640 208" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yazma yolu: senkron sıralı ekleme, ağır iş asenkron">
  <defs>
    <marker id="wca-wp-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yazma yolu: hızlı ekle, ağırı ertele</text>
  <rect x="20" y="52" width="96" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="68" y="76" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İstemci</text>
  <text x="68" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yazma isteği</text>
  <rect x="150" y="52" width="96" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="198" y="76" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">API</text>
  <text x="198" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">doğrula</text>
  <rect x="280" y="52" width="110" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="335" y="76" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">WAL / log</text>
  <text x="335" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sıralı ekle O(1)</text>
  <line x1="116" y1="78" x2="146" y2="78" stroke="#5D7789" stroke-width="1.5" marker-end="url(#wca-wp-ar)"/>
  <line x1="246" y1="78" x2="276" y2="78" stroke="#5D7789" stroke-width="1.5" marker-end="url(#wca-wp-ar)"/>
  <path d="M335,104 L335,128 L200,128 L200,110" fill="none" stroke="#25804C" stroke-width="1.5" marker-end="url(#wca-wp-ar)"/>
  <text x="252" y="144" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">ack hemen döner (senkron biter)</text>
  <line x1="390" y1="78" x2="428" y2="78" stroke="#5D7789" stroke-width="1.5" stroke-dasharray="5 3" marker-end="url(#wca-wp-ar)"/>
  <rect x="432" y="44" width="188" height="30" rx="7" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="526" y="63" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">async: replikasyon</text>
  <rect x="432" y="80" width="188" height="30" rx="7" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="526" y="99" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">async: indeksleme / fanout</text>
  <text x="526" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ağır iş kritik yoldan çıkar</text>
  <text x="320" y="180" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Kritik yol = ekleme + ack; çoğaltma/fanout arkada (kesikli ok)</text>
  <text x="320" y="198" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">gecikme düşer, throughput artar — tutarlılık nihai (eventual) olur</text>
</svg>
```

Üçüncü karar **kimlik (ID) üretimidir**. URL kısaltıcıda her bağlantıya kısa ve benzersiz bir anahtar gerekir. Anahtarı içeriğin özetinden (MD5/SHA) kırpmak çakışma riski taşır. Ayrıca her yazmada "bu anahtar zaten var mı?" diye **ek bir okuma** gerektirir. Rastgele UUID benzersizdir; ama 122 bitlik değeri 22 karakterden uzun yazılır, yani "kısa" sayılmaz. Tek bir merkezî sayaç ise her yazmada koordinasyon ister; darboğaz ve tek arıza noktası olur.

Ölçekte tercih edilen desen **KGS**'tir (Key Generation Service — anahtar üretim servisi). Her düğüm önceden bir ID **bloğu** çeker. Blok içindeki sayıları yerelde base62 ile kısa koda çevirir. Blok tükenince yenisini alır. Böylece koordinasyon yazma başına değil, blok başına yapılır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Merkezî sayaç darboğazı vs KGS blok dağıtımı">
  <defs>
    <marker id="wca-kgs-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">ID üretimi: her yazmada mı, blok başına mı?</text>
  <rect x="24" y="42" width="286" height="150" rx="12" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="167" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#8A2E2E" font-family="system-ui">Merkezî sayaç (SPOF)</text>
  <rect x="70" y="80" width="70" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="105" y="102" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Düğüm A</text>
  <rect x="194" y="80" width="70" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="229" y="102" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Düğüm B</text>
  <rect x="132" y="146" width="70" height="30" rx="7" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="167" y="165" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">sayaç</text>
  <line x1="105" y1="114" x2="150" y2="144" stroke="#5D7789" stroke-width="1.4" marker-end="url(#wca-kgs-ar)"/>
  <line x1="229" y1="114" x2="184" y2="144" stroke="#5D7789" stroke-width="1.4" marker-end="url(#wca-kgs-ar)"/>
  <text x="167" y="188" text-anchor="middle" font-size="10" fill="#8A2E2E" font-family="system-ui">HER yazma sayaca gider → tıkanır</text>
  <rect x="330" y="42" width="286" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="473" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">KGS: önceden blok dağıt</text>
  <rect x="415" y="80" width="116" height="34" rx="7" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="473" y="101" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">KGS</text>
  <rect x="356" y="140" width="112" height="42" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="412" y="158" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Düğüm A</text>
  <text x="412" y="174" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">blok 1000–1999</text>
  <rect x="478" y="140" width="112" height="42" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="534" y="158" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Düğüm B</text>
  <text x="534" y="174" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">blok 2000–2999</text>
  <line x1="445" y1="114" x2="415" y2="138" stroke="#5D7789" stroke-width="1.4" marker-end="url(#wca-kgs-ar)"/>
  <line x1="501" y1="114" x2="531" y2="138" stroke="#5D7789" stroke-width="1.4" marker-end="url(#wca-kgs-ar)"/>
  <text x="473" y="132" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">blok tükenince yeni blok</text>
</svg>
```

Yazmayı ucuzlatmanın üç tamamlayıcı refleksi vardır.

**Bir: sıcak sayaçları belleğe al.** Çok sık yazılan, kısa ömürlü sayaçlar (örneğin bir hız sınırlayıcının kullanıcı sayacı) diske değil belleğe yazılır. Redis'in `INCR` komutu okuma ile artırmayı **tek bölünmez adımda** yapar. `TTL` ise değeri süre dolunca kendiliğinden siler; ayrı bir temizlik işi gerekmez.

**İki: tüketiciyi tekrara dayanıklı yap.** Dağıtık kuyruklar pratikte **en az bir kez (at-least-once)** teslim eder. **Yeniden deneme (retry)** bazen aynı olayı iki kez getirir. Çözüm yeniden denemeyi kapatmak değildir; o zaman olay tümüyle kaybolabilir. Çözüm tüketiciyi **tekrar edilse de sonucu değişmez (idempotent)** kılmaktır. Bunun için her `(kullanıcı, olay)` çifti için bir **idempotency anahtarı** saklanır. Aynı anahtar ikinci kez gelirse iş tekrarlanmaz. Mesaj sıralaması garantisi bunun yerine geçmez: sıralı gelen bir mesaj da çift gelebilir.

**Üç: sayma sorularında doğru aracı seç.** "Kaç farklı ziyaretçi geldi" gibi sorular milyarlarca kimliği tutmayı gerektirir. `HashSet` kesin cevap verir; ama bellek kullanımı kimlik sayısıyla birlikte büyür. `COUNT(DISTINCT)` de kesindir; ama her sorguda tüm satırları tarar. Küçük bir hata payına razıysan **HyperLogLog** kullanılır: birkaç kilobayt bellekle, yaklaşık %2 hatayla **kardinaliteyi** (kaç farklı öğe var) tahmin eder ve ayrı parçaların sonuçları birleştirilebilir. **Bloom filtresi** ise başka bir soruya cevap verir: "bu öğeyi daha önce gördüm mü?" Yani üyelik sorar, "kaç tane?" diye sormaz.

Son olarak **yazma çoğullanması** depolama motorunun içinde de karşına çıkar. LSM ağaçlarında veri önce belleğe (memtable), sonra disk katmanlarına yazılır. Katmanlar birleştirilirken (**compaction / sıkıştırma-birleştirme**) aynı kayıt defalarca yeniden yazılır. Yani tek mantıksal yazma, diskte N fiziksel yazmaya dönüşür. Bu, ağ üzerindeki kopyalamadan da istemcinin isteği tekrar göndermesinden de farklıdır; motorun kendi iç davranışıdır.

### Mülakatta ne sorulur?

- "Yüksek hızda kısa ve benzersiz anahtarı nasıl üretirsin?" → Merkezî sayacın darboğaz olduğunu söyle ve KGS bloklarını anlat. Özet kırpmanın ek okuma getirdiğini, UUID'nin ise uzunluk sorunu yarattığını ekle.
- "Ünlü bir hesap 50 milyon takipçiye gönderi atıyor, ne kırılır?" → Tek gönderi 50 milyon akış yazmasına dönüşür; yazma çoğullanması yaşanır. Çözüm hibrit dağıtımdır.
- "Aynı bildirim iki kez gitti, ne yaparsın?" → "En az bir kez teslim" gerçeğini kabul et ve idempotency anahtarıyla tekilleştir.
- "Sınırlı bellekle benzersiz ziyaretçi sayısı?" → HyperLogLog; Bloom filtresiyle farkını mutlaka belirt.
- "Rate limiter sayacını nerede tutarsın?" → bellek-içi atomik `INCR` + TTL.
- "LSM'de yazma çoğullanması nedir?" → Birleştirme (compaction) yüzünden aynı kaydın defalarca yeniden yazılmasıdır.

### Tek cümlede

Yazma-ağır tasarımda kazanan yaklaşım şudur: yazmayı tek bir sıralı eklemeye indir, ağır işi arkaya at, koordinasyonu yazma başına değil blok başına yap ve yeniden denemeyi zararsız (idempotent) kıl.

# wcase-1 | İleri
## Soru
Bir URL kısaltıcı, yüksek yazma hızında kısa ve benzersiz anahtarlar üretmeli. Ölçekte koordinasyonu ve çakışmayı en aza indiren yaklaşım hangisidir?
## A
URL'nin MD5/SHA özetini alıp ilk 7 karakterini kısa anahtar olarak kullanmak
🤖 Özet kısaltması çakışabilir; her yazmada "bu anahtar var mı?" diye EK okuma gerektirir — gizli maliyet ve yarış.
## B
Tek bir global otomatik-artan sayacı her yazmada okuyup base62 ile kodlamak
🤖 Çalışır ama tek sayaç her yazmada koordinasyon isteyen bir darboğaz/SPOF olur; ölçekte tıkanır.
## C
Bir Key Generation Service'ten (KGS) düğüm başına önceden ID bloğu alıp yerelde base62 kodlamak
🤖 Doğru: her düğüm bir ID bloğu kapar, yerelde kodlar; ne yazma başına koordinasyon ne de çakışma kontrolü gerekir.
## D
Her URL için rastgele bir UUID üretip onu doğrudan kısa kod olarak kullanmak
🤖 UUID 122 bit / 22+ karakterdir — "kısa" değildir; kısaltıcının amacını bozar.
## Doğru
C
## Açıklama
Yazma-ağır bir kısaltıcıda amaç, **yazma başına koordinasyonu ve çakışma kontrolünü ortadan kaldırmaktır**. KGS deseninde her düğüm önceden bir ID aralığı (blok) çeker, base62 ile kısa koda çevirir ve blok tükenince yenisini alır. Böylece anahtarlar tasarımca benzersizdir; ne merkezî sayaç darboğazı (B) ne de özet-çakışması için ek okuma (A) olur. UUID (D) benzersizdir ama kısa değildir. Bu, "yazmayı ucuzlat ve dağıt" ilkesinin somut örneğidir.

# wcase-2 | Orta
## Soru
Dağıtık bir rate limiter, her istekte kullanıcı başına bir sayacı artırıyor (çok yazma-ağır). Bu sayaç için en uygun birincil depo hangisidir?
## A
Bellek-içi depo (Redis) ile atomik `INCR` + TTL
🤖 Doğru: bellek-içi tek adımlık atomik artırma; TTL pencereyi otomatik sıfırlar — tam bu iş için tasarlanmıştır.
## B
İlişkisel DB satırında her istekte `UPDATE SET count=count+1`
🤖 Satır kilidi çekişmesi + istek başına disk yazması; yük altında erir.
## C
Bir log dosyasına ekleyip kontrol anında satır saymak
🤖 Okuma O(n) tarama olur, dosya sınırsız büyür; istek hızında uygulanamaz.
## D
Her kullanıcı için ayrı bir Kafka topic'i açıp sayımı tüketicide yapmak
🤖 Kullanıcı başına topic ölçeklenmez (partition patlaması); kapı-bekçisi kararı için gecikme çok yüksek.
## Doğru
A
## Açıklama
Rate limiter sayacı **çok sık yazılan, kısa ömürlü** bir değerdir. Redis `INCR` tek atomik işlemde hem okur hem artırır (yarış yok), TTL ise pencere sonunda değeri otomatik düşürür — temizlik işi gerekmez. İlişkisel satır güncellemesi (B) kilit çekişmesi ve disk yükü yüzünden bu hızda çöker; log tarama (C) okumayı O(n) yapar; kullanıcı-başına-topic (D) ölçeklenmeyen bir ek yüktür. Yazma-ağır sayaçların doğal yeri bellek-içi atomik operasyonlardır.

# wcase-3 | İleri
## Soru
Bir sosyal akış, fanout-on-write (gönderi anında her takipçinin akışına yazma) kullanıyor. 50 milyon takipçili bir ünlü gönderi paylaşıyor. Ana sorun ve çözümü nedir?
## A
Okumalar yavaşlar; çözüm akış okumalarına bir önbellek katmanı eklemek ve yazma tarafını olduğu gibi bırakmaktır
🤖 Fanout-on-write okumayı zaten ucuzlatır; sorun yazma tarafındadır, okuma değil.
## B
Tek gönderi 50M akış yazması tetikler (write amplification / sıcak fanout); çözüm hibrittir: ünlü hesaplar için pull (fanout-on-read)
🤖 Doğru: amplification'ı yalnız çok takipçili hesaplarda pull'a çevirerek söndürürsün.
## C
Sorun yok; fanout-on-write doğrusal ölçeklenir ve takipçi sayısından bağımsız olarak her hesapta tercih edilir
🤖 Amplification sıçramasını yok sayar; ünlü hesap tam da bu desenin klasik çöküş noktasıdır.
## D
Tutarlılık bozulur; fanout'u senkron yazmaya çevirip gönderiyi bloke etmek gerekir
🤖 Senkron yazma işi kötüleştirir (gönderi 50M yazma bitene kadar bloke olur).
## Doğru
B
## Açıklama
Fanout-on-write okumayı ucuzlatır ama yazmayı takipçi sayısıyla çarpar: 50M takipçi = tek gönderi başına 50M yazma (**write amplification**). Sıradan hesaplarda bu kabul edilebilir, ama ünlü hesaplarda ani ve devasa bir yük sıçraması olur. Endüstri çözümü **hibrittir**: normal hesaplar push edilir, çok takipçili hesapların gönderileri push edilmez; onları takip edenler akışı okurken bu gönderileri **pull** ile toplar. Senkron yazma (D) gönderiyi bloke ederek işi kötüleştirir; okuma önbelleği (A) yanlış tarafı optimize eder.

# wcase-4 | İleri
## Soru
Bir bildirim servisi, yeniden denemeler (retry) yüzünden aynı olayı iki kez teslim edebiliyor. Çift bildirimi önlemek için en sağlam yaklaşım nedir?
## A
Yeniden denemeleri tamamen kapatıp at-most-once teslimata güvenmek
🤖 Geçici hatada bildirimi düşürür; güvenilmez — kaybı çift teslimatla takas etmiş olursun.
## B
Daha büyük ve daha hızlı bir mesaj aracısı (broker) kümesine geçmek
🤖 Broker boyutu ilgisiz; at-least-once teslimat yine yeniden gönderir.
## C
Kuyrukta yalnız mesaj sıralaması (ordering) garantisi vermekle yetinmek
🤖 Sıralama ≠ tekilleştirme; yeniden teslim edilen mesaj sıralı ama yine çifttir.
## D
Gönderim öncesi (kullanıcı, olay) çifti için idempotency anahtarını kontrol edip saklamak
🤖 Doğru: tüketici tarafında tekilleştirme, yeniden teslimi güvenli hale getirir.
## Doğru
D
## Açıklama
Dağıtık kuyruklar pratikte **at-least-once** (en az bir kez) teslim eder; yani yeniden deneme kaçınılmaz olarak bazen çift mesaj üretir. Kaybı önlemek için retry'ı kapatamayacağına (A) göre, çözüm **tüketici tarafında idempotency**'dir: her `(kullanıcı, olay)` için bir anahtar üretip göndermeden önce "bunu daha önce gönderdim mi?" diye kontrol edersin (D). Broker'ı büyütmek (B) sorunu çözmez; sıralama garantisi (C) tekilleştirme değildir. Belt-and-suspenders redundancy ancak **idempotent** olduğunda güvenlidir.

# wcase-5 | İleri
## Soru
Milyarlarca olay üzerinden URL başına benzersiz ziyaretçi sayısını, sınırlı bellekle ve ~%1-2 hata payına razı olarak izlemen gerekiyor. En uygun yapı hangisidir?
## A
Görülen tüm ziyaretçi ID'lerini bellekte tutan bir `HashSet`
🤖 Kesin sonuç verir ama bellek kardinaliteyle büyür; milyarlarca ID sığmaz.
## B
Bir Bloom filtresi
🤖 Üyelik ("bu ID görüldü mü?") sorusunu yanıtlar; kardinaliteyi/sayıyı doğrudan vermez.
## C
Ham olay tablosunda `COUNT(DISTINCT)`
🤖 Kesin sayıyı verir ama her sorguda milyarlarca satırı tam tarar; ne hızlıdır ne de sınırlı bellekli.
## D
HyperLogLog
🤖 Doğru: olasılıksal kardinalite; ~KB bellek, ~%2 hata ve shard'lar arası birleştirilebilir (mergeable).
## Doğru
D
## Açıklama
Soru **kardinalite tahmini** (kaç benzersiz) istiyor; üyelik değil. **HyperLogLog** tam bunun için tasarlanmıştır: birkaç KB bellekle milyarlarca farklı öğeyi ~%2 hatayla sayar ve en güzeli, farklı shard'ların HLL'leri **birleştirilebilir** — dağıtık toplama doğaldır. `HashSet` (A) kesin ama belleği kardinaliteyle patlar; Bloom filtresi (B) "gördüm mü?" der, "kaç tane?" demez; `COUNT(DISTINCT)` (C) doğru ama her sorguda tam tarama yapar. Sınırlı bellek + kabul edilebilir hata = olasılıksal veri yapısı.

# wcase-6 | Orta
## Soru
Bir LSM-tree (Log-Structured Merge-tree) depolama motorunda "write amplification" öncelikle neyi ifade eder?
## A
Tek mantıksal yazmanın compaction yüzünden diske birden çok kez yazılması
🤖 Doğru: LSM veriyi katmanlar arası taşırken defalarca yeniden yazar; 1 mantıksal yazma = N fiziksel yazma.
## B
Yazmanın ağ üzerinden replikalara çoğaltılması sırasında oluşan ek trafik
🤖 O replikasyondur; farklı bir kavram (replication amplification).
## C
Yazma işleminin okuma işleminden belirgin biçimde daha yavaş olması
🤖 Bu bir hız gözlemi; write amplification'ın tanımı değil.
## D
İstemcinin yeniden deneme yüzünden aynı yazmayı birden çok kez göndermesi
🤖 O retry/tekrar teslimattır; write amplification depolama motorunun iç davranışıdır.
## Doğru
A
## Açıklama
**Write amplification**, bir depolama motorunda tek bir mantıksal yazmanın diske birden çok kez fiziksel olarak yazılmasıdır. LSM-tree'lerde bunun kaynağı **compaction**'dır: veri önce belleğe (memtable), sonra diskteki katmanlara yazılır ve katmanlar birleştirilirken aynı kayıt tekrar tekrar yeniden yazılır. Bu, yazma-ağır sistemlerde disk I/O bütçesini doğrudan etkileyen kritik bir metriktir. Replikasyon (B) ağ tarafıdır, retry (D) istemci tarafıdır, "yavaşlık" (C) ise sonuçtur — tanım değil.
