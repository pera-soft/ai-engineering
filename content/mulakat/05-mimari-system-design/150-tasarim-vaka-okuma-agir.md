---
slug: tasarim-vaka-okuma-agir
baslik: "Tasarım Vakası (Okuma-Ağır): Mesajlaşma, Otomatik Tamamlama, Canlı Skor, Dosya Depolama ve WebSocket Ölçeği"
kademe: İleri
sira: 150
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Sunucuya gönderdiğin her not bir **istek** (request), geri gelen cevap bir **yanıt** (response) sayılır. İsteğin gidip yanıtın gelmesi arasında geçen süreye **gecikme** (latency) denir. Bazı sistemlerde istekler ağırlıklı olarak "bana şunu göster" der: bir sohbetin son mesajları, arama kutusunun önerileri, canlı maç skoru, bir dosyanın indirilmesi. Bunlara **okuma-ağır (read-heavy)** sistem denir. Tipik olarak bir yazmaya karşılık binlerce, bazen milyonlarca okuma düşer.

Okuma-ağır tasarımın tek bir amacı vardır: **aynı veriyi milyonlarca kez üretmenin maliyetini düşürmek**. Cevabı her seferinde ana kaynaktan (**origin**) hesaplamak yerine, hazır kopyayı kullanıcıya yakın bir yerde tutup oradan sunarsın. Kazanç çift yönlüdür: kullanıcının beklediği süre kısalır ve ana kaynağın taşıdığı yük düşer.

### Temel kavramlar

- **Önbellek (cache)**: sık istenen cevabın hızlı erişilen bir kopyasıdır. Cevap önbellekte bulunursa buna **isabet (hit)**, bulunmazsa **ıska (miss)** denir. İsteklerin yüzde kaçının isabet ettiğini gösteren **isabet oranı**, ana kaynağın yükünü doğrudan belirler.
- **CDN / kenar (edge)**: farklı şehirlerde duran, kullanıcıya fiziksel olarak yakın sunucu ağı.
- **Okuma kopyası (read replica)**: ana veritabanının yalnız okuma için kullanılan kopyasıdır. Veri kopyaya gecikmeli ulaşır. Bu farka **kopyalama gecikmesi (replication lag)** denir. Sonucu şudur: kullanıcı az önce kendi yazdığı veriyi göremeyebilir. Bunu önlemek için o okuma ana kopyaya yönlendirilir; buna **kendi yazdığını okuma (read-your-writes)** denir.
- **TTL (time to live)**: bir kopyanın kaç saniye geçerli sayılacağı. Kısa TTL = daha taze veri + daha çok origin yükü.
- **Bölümleme (partition / shard)** ve **bölüm anahtarı**: veriyi hangi kurala göre parçalara ayırdığındır.
- **Önceden hesaplama (precompute)**: cevabı sorgu anında değil, öncesinde hazırlamak.

### Nasıl çalışır?

Temel refleks şudur: **okumayı ana kaynağa varmadan karşıla.** İstek ne kadar erken ve kullanıcıya ne kadar yakın bir katmanda yanıtlanırsa ana kaynak o kadar rahatlar.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Okuma yolu: istek ne kadar erken karşılanırsa origin o kadar rahat">
  <defs>
    <marker id="rca-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Okuma katmanları: soldan sağa origin'e in</text>
  <rect x="24" y="58" width="112" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="80" y="88" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">İstemci</text>
  <text x="80" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yerel önbellek</text>
  <rect x="168" y="58" width="112" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="224" y="88" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">CDN / Edge</text>
  <text x="224" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">fanout'u emer</text>
  <rect x="312" y="58" width="112" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="368" y="88" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Cache</text>
  <text x="368" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Redis / replika</text>
  <rect x="456" y="58" width="112" height="70" rx="10" fill="#FBEDE4" stroke="#B4531F" stroke-width="1.5"/>
  <text x="512" y="88" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B4531F" font-family="system-ui">Origin DB</text>
  <text x="512" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">en pahalı</text>
  <line x1="138" y1="93" x2="164" y2="93" stroke="#5D7789" stroke-width="1.5" marker-end="url(#rca-ar)"/>
  <line x1="282" y1="93" x2="308" y2="93" stroke="#5D7789" stroke-width="1.5" marker-end="url(#rca-ar)"/>
  <line x1="426" y1="93" x2="452" y2="93" stroke="#5D7789" stroke-width="1.5" marker-end="url(#rca-ar)"/>
  <text x="320" y="160" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">her katman origin'e giden trafiği azaltır</text>
</svg>
```

En yaygın desen **yanda önbellek (cache-aside)** desenidir. Okuma önce önbelleğe bakar. İsabet varsa ana kaynak hiç görülmez. Iska varsa veri ana kaynaktan çekilir, önbelleğe yazılır ve sonraki okumalar isabet eder. Aritmetiği basittir: %95 isabet oranı, ana kaynağın trafiğini yaklaşık 20 kat düşürür.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cache-aside: isabet origin'i atlar, ıska origin'den doldurur">
  <defs>
    <marker id="rca-ca-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Cache-aside: isabet vs ıska</text>
  <rect x="24" y="86" width="100" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="74" y="110" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İstemci</text>
  <text x="74" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">okuma</text>
  <rect x="200" y="86" width="110" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="255" y="110" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Cache</text>
  <text x="255" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Redis</text>
  <rect x="470" y="86" width="120" height="52" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="530" y="110" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Origin DB</text>
  <text x="530" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">en pahalı</text>
  <line x1="124" y1="112" x2="196" y2="112" stroke="#5D7789" stroke-width="1.5" marker-end="url(#rca-ca-ar)"/>
  <path d="M200,100 L150,100 L150,80 L124,80" fill="none" stroke="#25804C" stroke-width="1.6" marker-end="url(#rca-ca-ar)"/>
  <text x="162" y="72" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">HIT → origin'e gitme</text>
  <line x1="310" y1="112" x2="466" y2="112" stroke="#5D7789" stroke-width="1.5" stroke-dasharray="5 3" marker-end="url(#rca-ca-ar)"/>
  <text x="388" y="104" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">MISS → çek</text>
  <path d="M470,128 L388,128 L388,150 L310,150 L310,140" fill="none" stroke="#B26B00" stroke-width="1.5" marker-end="url(#rca-ca-ar)"/>
  <text x="388" y="166" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">geri yaz (populate) → sonraki okuma HIT</text>
  <text x="320" y="196" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yüksek isabet oranı = origin yükü katbekat düşer (%95 → 20×)</text>
</svg>
```

Tek yazan / çok okuyan (single-writer / many-reader) vakalar bu mantığın uç örneğidir. Saniyede bir kez güncellenen canlı skoru 10 milyon kişinin izlediğini düşün. Her istemcinin ana kaynağı yoklaması (poll), saniyede 10 milyon okuma demektir. İstemcilere ana kaynaktan doğrudan kalıcı bağlantı açmak da 10 milyon bağlantıyı ana kaynağa yükler. Oysa güncellenen değeri ~1 saniyelik TTL ile bir CDN/kenar önbelleğine ya da bir yayın-abone (pub/sub) dağıtım katmanına itersen ana kaynak saniyede yalnız 1 yazma görür. 1 saniyelik bayatlık (staleness), 10 milyon okumadan kaçınmanın makul bedelidir.

Depolamadaki ikinci desen **baytları üstveriden ayırmaktır**. Büyük dosya içeriği (blob) ucuz ve dayanıklı bir **nesne deposuna** yazılır; CDN ya da imzalı (presigned) bağlantı ile sunulur. Ad, boyut, sahip ve izin gibi **üstveri (metadata)** ise sorgulanabilir bir veritabanında durur. Baytları ilişkisel veritabanına BLOB olarak koymak yedeklemeyi, önbelleği ve sorgu performansını birlikte bozar. Uygulama sunucusunun yerel diski ise ne dayanıklıdır ne de yatay ölçeklenir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Baytlar nesne deposu + CDN, üstveri sorgu DB'sinde">
  <defs>
    <marker id="rca-bm-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Baytları üstveriden ayır</text>
  <rect x="258" y="42" width="124" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İstemci / App</text>
  <text x="320" y="80" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">dosya iste</text>
  <rect x="40" y="124" width="240" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="160" y="147" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Baytlar → Nesne deposu + CDN</text>
  <text x="160" y="166" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ucuz, dayanıklı; presigned URL ile servis</text>
  <text x="160" y="179" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">okuma origin'i hiç görmez</text>
  <rect x="360" y="124" width="240" height="60" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="480" y="147" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Üstveri → Sorgu DB</text>
  <text x="480" y="166" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ad, boyut, sahip, izinler</text>
  <text x="480" y="179" text-anchor="middle" font-size="9.5" fill="#B26B00" font-family="system-ui">küçük, indekslenebilir, sorgulanır</text>
  <line x1="286" y1="80" x2="180" y2="120" stroke="#5D7789" stroke-width="1.5" marker-end="url(#rca-bm-ar)"/>
  <line x1="354" y1="80" x2="460" y2="120" stroke="#5D7789" stroke-width="1.5" marker-end="url(#rca-bm-ar)"/>
  <text x="200" y="104" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">büyük baytlar</text>
  <text x="440" y="104" text-anchor="middle" font-size="9.5" fill="#B26B00" font-family="system-ui">küçük üstveri</text>
</svg>
```

Bölüm anahtarı **okuma desenine göre** seçilir. Baskın sorgu "şu konuşmanın son N mesajı" ise `conversation_id`'ye göre bölmek doğrudur; böylece bir konuşmanın tüm mesajları tek bölümde toplanır. Zaman damgasına göre kümelemek de son mesajları tek bir sıralı okumayla verir. Kötü seçeneklere bakalım. Rastgele `message_id` ile bölmek sorguyu tüm bölümlere saçar. Tek bir küresel tablo tek düğümü sıcak yapar. `user_id` ise aynı konuşmanın iki tarafını birbirinden ayırır. Kural: **birlikte okunacak veriyi birlikte konumla**.

Çok okunan bazı yollarda cevap sorgu anında hesaplanamaz; **önceden** hazırlanır. Otomatik tamamlamada bir **trie** (önek ağacı) kurulur. Her düğümde o önek için en iyi k öneri hazır bekletilir. Arama, önek uzunluğu kadar adımda biter ve her şey bellekten sunulduğu için gecikme milisaniyeler düzeyinde kalır.

Geçici (**ephemeral**) durumlarda ise başka bir yol izlenir. Örneğin "kim çevrimiçi" bilgisinde kısa TTL'li, bellek içi bir anahtar kullanılır. Bu hem O(1) kontrol hem otomatik temizlik sağlar: kalp atışı (heartbeat) kesilince anahtar kendiliğinden silinir.

Son olarak kalıcı bağlantılara bakalım. Bir WebSocket **tam olarak tek bir sunucuda** yaşar. Milyonlarca bağlantı çok sayıda sunucuya yayıldığında iki soru çıkar. "X'in soketi hangi sunucuda?" sorusunu bir **bağlantı kayıt defteri** (kullanıcı → sunucu eşlemesi) çözer. "Mesajı oraya nasıl taşırım?" sorusunu ise bir **yayın-abone omurgası (backplane)** çözer. Yük dengeleyici bu yönlendirmeyi yapmaz; onun işi gelen bağlantıyı bir sunucuya dağıtmaktır, uygulama seviyesinde soket adresi tutmak değil.

### Mülakatta ne sorulur?

- "Otomatik tamamlamayı 50ms altında nasıl verirsin?" → önceden hesaplanmış trie + düğüm başına top-k, bellekten servis.
- "Mesajları nasıl bölümlersin?" → okuma desenine göre `conversation_id` + zaman sıralaması.
- "Saniyede 1 yazma / 10M okuma nasıl taşınır?" → kenara it, kısa TTL, origin'i tek yazma hızında bırak.
- "Dosya baytları nerede durur?" → nesne deposu + CDN; üstveri ayrı bir DB'de.
- "Presence nasıl tutulur?" → TTL'li geçici anahtar, heartbeat ile tazeleme.
- "Milyonlarca WebSocket nasıl yönlendirilir?" → kayıt defteri + backplane.
- Tuzak soru: "kullanıcı kopyadan okuyunca kendi yazdığını görmedi" → Sebep kopyalama gecikmesidir; çözüm kendi yazdığını okuma (read-your-writes) kuralıdır.

### Tek cümlede

Okuma-ağır tasarım, cevabı kullanıcıya en yakın katmanda hazır tutmak ve ana kaynağı mümkün olduğunca hiç yormamak üzerine kuruludur.

# rcase-1 | İleri
## Soru
Bir tip-anında-öneri (typeahead autocomplete) özelliği, çok yüksek okuma hacminde bir önek için ilk önerileri <50ms'de döndürmeli. En uygun çekirdek yapı/yaklaşım nedir?
## A
Her tuş vuruşunda ilişkisel veritabanında `LIKE 'onek%'` sorgusu çalıştırmak
🤖 Tuş başına DB taraması; çok yavaş ve sıralama (ranking) da yok.
## B
Her tuş vuruşunda bulanık (fuzzy) tam-metin arama motoruna sorgu göndermek
🤖 Gerekenden ağır; tuş başına gecikme yükselir, <50ms hedefini zorlar.
## C
Önceden hesaplanmış bir trie (önek ağacı), her düğümde top-k saklı, bellekten/önbellekten servis edilir
🤖 Doğru: önek gezinmesi O(önek uzunluğu), öneriler hazır — bu tam da düşük gecikmeli okuma için tasarlanmış yapıdır.
## D
Tüm terim sözlüğünü istemciye indirip filtrelemeyi tarayıcıda yapmak
🤖 Veri kümesi çok büyük; milyonlarca terimi istemciye göndermek olanaksız.
## Doğru
C
## Açıklama
Otomatik tamamlama, klasik **son-derece-okunan / seyrek-yazılan** bir problemdir; cevap sorgu anında hesaplanmaz, **önceden hesaplanır**. Bir **trie** öneki karakter karakter gezerek O(önek uzunluğu)'nda ilgili düğüme iner ve o düğümde saklı **top-k** öneriyi anında döndürür; tümü bellekte/önbellekte durduğundan <50ms kolayca tutulur. DB `LIKE` (A) ve bulanık arama (B) tuş başına pahalıdır; her şeyi istemciye indirmek (D) veri boyutu yüzünden imkânsızdır. Okuma-ağır yolda maliyet, okuma anından yazma/derleme anına kaydırılır.

# rcase-2 | Orta
## Soru
Bir 1:1 sohbet uygulaması: mesajlar bir kez yazılır, çok kez okunur; konuşma başına son mesajların hızlı getirilmesi gerekir. En uygun bölümleme (partition) anahtarı / depolama tasarımı nedir?
## A
`conversation_id`'ye göre bölüp zaman damgasına göre (desc) sıralamak
🤖 Doğru: bir konuşma tek bölümde durur, son-mesaj sorgusu tek sıralı okumadır.
## B
`message_id`'ye (rastgele üretilen kimliğe) göre bölmek
🤖 Son-mesaj sorgusu tüm bölümlere dağılır (scatter); yavaş.
## C
Tüm mesajları zamana göre sıralı tek bir global tabloda tutmak
🤖 Bölümleme yok; tek düğüm sıcak olur, okuma ölçeklenmez.
## D
Yalnız `user_id`'ye göre bölüp mesajları kullanıcıya bağlamak
🤖 Bir konuşma iki kullanıcıya yayılır; okuma bölümler-arası birleştirme gerektirir.
## Doğru
A
## Açıklama
Bölümleme anahtarı **okuma desenine** göre seçilir. Baskın sorgu "şu konuşmanın son N mesajı" olduğuna göre, `conversation_id`'ye göre bölmek bir konuşmanın tüm mesajlarını **tek bölümde** toplar; zaman damgasına göre (desc) kümelemek (clustering) son mesajları tek sıralı okumayla verir. `message_id` (B) sorguyu tüm bölümlere saçar; tek global tablo (C) sıcak düğüm yaratır; `user_id` (D) konuşmanın iki tarafını ayırır ve okumayı bölümler-arası birleştirmeye zorlar. Kural: **veriyi birlikte okunacak şekilde birlikte konumla**.

# rcase-3 | İleri
## Soru
Bir canlı skor sayfası: saniyede 1 güncelleme, 10 milyon eşzamanlı izleyici tarafından okunuyor. Origin yükünü en aza indirirken gerçek-zamana yakın kalan teslim + önbellek stratejisi hangisidir?
## A
Her istemci saniyede bir kez doğrudan origin veritabanını yoklar (poll)
🤖 Saniyede 10M DB okuması; origin erir.
## B
Her istemci doğrudan uygulama sunucusuna long-poll bağlantısı açar
🤖 Origin'de 10M açık bağlantı tutulur, önbellek yok; çok pahalı.
## C
Origin'den her istemciye doğrudan SSE/WebSocket; arada kenar (edge) katmanı yok
🤖 Origin 10M bağlantıyı taşır; fanout tier olmadan ölçeklenmez.
## D
Güncellemeyi ~1 sn TTL ile kenar/CDN önbelleğine (ya da pub/sub fanout katmanına) itmek
🤖 Doğru: tek origin yazması, okuma fanout'unu kenar emer.
## Doğru
D
## Açıklama
Bu tam bir **tek-yazan / çok-okuyan** vakasıdır: saniyede 1 yazma, 10M okuma. Çözüm okuma fanout'unu origin'den **kenara** taşımaktır: güncellenen skoru ~1 saniye TTL'li bir CDN/edge önbelleğine (veya bir pub/sub fanout katmanına) itersin; 10M istemci origin yerine kenardaki tek değeri okur. Origin yalnız saniyede 1 yazma görür. Poll (A), long-poll (B) ve kenarsız SSE/WebSocket (C) hepsi 10M yükü doğrudan origin'e bindirir. 1 saniyelik bayatlık (staleness), 10M origin okumasından kaçınmanın kabul edilebilir bedelidir.

# rcase-4 | İleri
## Soru
Bir dosya-depolama servisi (Drive benzeri) tasarlıyorsun. Dosya baytları ile üstveri (metadata) nerede durmalı?
## A
Hem baytlar hem üstveri ilişkisel veritabanında, büyük bir BLOB sütununda
🤖 Büyük BLOB'lar DB'yi şişirir, önbellek/yedeği bozar; klasik anti-desen.
## B
Baytlar nesne depolamada (S3-benzeri), CDN / presigned URL ile servis; üstveri bir DB/dizinde
🤖 Doğru: ucuz ve dayanıklı blob deposu + sorgulanabilir üstveri; okuma CDN'e offload edilir.
## C
Baytlar uygulama sunucusunun yerel diskinde, üstveri aynı sunucunun belleğinde
🤖 Dayanıklı değil, yatay ölçeklenmez, düğüm ölünce kaybolur.
## D
Baytlar da üstveri de tümüyle Redis'te, bellek-içinde tutulur
🤖 Büyük dosyalar için bellek maliyeti orantısız; dayanıklılık (durability) da zayıftır.
## Doğru
B
## Açıklama
Dosya-depolamada **baytlar ile üstveriyi ayırmak** temel desendir. Ham baytlar ucuz, dayanıklı (durable) ve yatay ölçeklenen bir **nesne deposuna** (object storage) yazılır; okuma yükü **CDN / presigned URL** ile origin'den tamamen offload edilir. Üstveri (ad, boyut, sahip, izinler) ise sorgulanabilir bir DB/dizinde tutulur. Baytları ilişkisel DB'ye BLOB olarak koymak (A) DB'yi şişirir ve yedek/önbelleği bozar; yerel disk (C) dayanıksızdır; Redis (D) büyük dosyalar için bellek maliyeti bakımından orantısızdır. Doğru ayrım hem maliyeti hem okuma ölçeğini çözer.

# rcase-5 | İleri
## Soru
Milyonlarca kullanıcı için presence ("kim çevrimiçi") tutuyorsun. En ölçeklenebilir gösterim hangisidir?
## A
Kullanıcı başına bir boolean sütunu her heartbeat'te güncellenir, JOIN ile okunur
🤖 Sürekli yazma + JOIN'li okuma; sıcak ve yavaş.
## B
Her heartbeat'i bir ekleme (append) tablosuna satır olarak yazmak
🤖 Write amplification, sınırsız büyüme; oysa presence geçicidir.
## C
Redis'te kısa TTL'li geçici (ephemeral) anahtarlar; heartbeat anahtarı tazeler
🤖 Doğru: TTL bayat kullanıcıları otomatik düşürür, O(1) kontrol, temizlik işi gerekmez.
## D
Her durum değişikliğini tüm kullanıcılara yayınlayıp istemcide listeyi tutmak
🤖 O(n²) fanout; milyonlarda imkânsız.
## Doğru
C
## Açıklama
Presence **geçici** (ephemeral) bir durumdur — kalıcılaştırmaya değmez, doğal olarak "eskir". En temiz gösterim, Redis'te **kısa TTL'li anahtarlar**dır: kullanıcı her heartbeat'te anahtarı tazeler; heartbeat kesilince TTL anahtarı otomatik siler ve kullanıcı "çevrimdışı" olur. Kontrol O(1) "anahtar var mı?"dır, ayrı temizlik işi (cleanup job) gerekmez. Boolean sütunu güncellemek (A) sürekli yazma + JOIN maliyetidir; heartbeat'leri kalıcı yazmak (B) sınırsız büyür; herkese yayın (D) O(n²) olur. Geçici durum için geçici, kendi-kendini-temizleyen yapı seçilir.

# rcase-6 | Orta
## Soru
Milyonlarca WebSocket bağlantısını çok sayıda sunucuya yayıyorsun. Kullanıcı X'e gidecek bir mesaj, X'in soketini tutan sunucuya ulaşmalı. En uygun yaklaşım nedir?
## A
Her sunucunun tüm kullanıcıların bağlantısını kendinde tutması
🤖 İmkânsız; bir soket tam olarak tek bir sunucuda yaşar.
## B
Bir bağlantı kayıt defteri (kullanıcı→sunucu) ve bir pub/sub omurgası (backplane)
🤖 Doğru: kayıt defteri soketin yerini bilir, backplane mesajı doğru sunucuya taşır.
## C
Tüm bağlantıları tek bir yük dengeleyicide (load balancer) saklamak
🤖 LB uygulama seviyesi soket yönlendirmesi tutmaz; onun işi bu değil.
## D
İtme (push) yerine istemcilerin mesajları veritabanından yoklaması (poll)
🤖 WebSocket'in push avantajını çöpe atar; gecikme ve yük artar.
## Doğru
B
## Açıklama
Kalıcı (stateful) WebSocket bağlantısı **tam olarak tek sunucuda** yaşar; dolayısıyla dağıtık ölçekte sorun "X'in soketi hangi sunucuda?" ve "mesajı oraya nasıl iletirim?" olur. Çözüm iki parçalıdır: bir **bağlantı kayıt defteri** (user→server, örn. Redis) soketin yerini tutar; bir **pub/sub omurgası** (backplane) mesajı sahip sunucuya yönlendirir. Her sunucunun her bağlantıyı tutması (A) fizik olarak imkânsızdır; LB (C) soket-seviyesi yönlendirme yapmaz; DB poll (D) push modelini terk edip gecikmeyi artırır. Kalıcı bağlantı ölçeği = yer kaydı + backplane.
