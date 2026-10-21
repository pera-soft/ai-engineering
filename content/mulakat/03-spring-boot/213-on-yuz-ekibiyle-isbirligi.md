---
slug: on-yuz-ekibiyle-isbirligi
baslik: "Ön Yüz Ekibiyle Çalışmak: OpenAPI Sözleşmesi, Mock, Contract Test ve Kırıcı Değişiklik"
kademe: İleri
sira: 213
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

İki usta bir evin iki yakasını aynı anda örüyorsa, ortada buluşacakları duvarın ölçüsünü **başlamadan önce** yazıya dökerler. Ölçü yazılı değilse iki taraf da hızlı çalışır ama duvar tutmaz; sökülüp yeniden örülür. Ön yüz (frontend) ekibiyle backend ekibinin arasındaki **API sözleşmesi** tam olarak o ölçüdür: hangi adrese hangi gövde gidecek, hangi alan hangi tipte dönecek, hata nasıl temsil edilecek.

Bu konu bir çerçeve (framework) konusu değil, bir **süreç** konusudur; mülakatta da hem teknik hem davranışsal tarafı sorulur. Senden ön yüz yazman beklenmiyor. Beklenen üç şeyin cevabıdır. Aradaki sözleşmeyi **sen** yönetebiliyor musun? Kırıcı bir değişikliği fark edip zararsızca çıkarabiliyor musun? Anlaşmazlıkta "onların işi" mi diyorsun, yoksa ortak bir belgeye mi işaret ediyorsun?

### Temel kavramlar

- **API sözleşmesi (API contract)**: İki taraf arasında uçların adresini, istek/yanıt gövdesini, alan tiplerini ve hata biçimini bağlayan anlaşma.
- **OpenAPI belgesi**: API sözleşmesini makinece okunabilir biçimde (YAML/JSON) tanımlayan standart; hem insan hem araç okur.
- **Sözleşme-önce (contract-first)**: Önce OpenAPI belgesini yazıp iki ekibin ona bakarak **paralel** çalışmaya başlaması.
- **Kod-önce (code-first)**: Önce Java kodunu yazıp OpenAPI belgesini koddan üretmek (Spring'de `springdoc-openapi` bunu yapar).
- **Taklit sunucu (mock server)**: Gerçek backend yerine sahte yanıt döndüren sunucu (Prism, WireMock, MSW); ön yüzün backend hazır olmadan çalışmasını sağlar.
- **Kayma (drift)**: Taklit yanıtla gerçek ucun zamanla birbirinden uzaklaşması; ön yüz mock'a göre doğru, gerçeğe göre yanlış çalışır.
- **Sözleşme testi (contract test)**: Tüketicinin (ön yüz) beklentisinin bir dosyaya yazılıp sağlayıcının (backend) CI'ında doğrulanması. Beklentiyi tüketicinin yazdığı biçimine **tüketici-güdümlü sözleşme** (consumer-driven contract, Pact fikri) denir.
- **Kırıcı değişiklik (breaking change)**: Var olan istemcileri bozan sözleşme değişikliği (alan silmek, yeniden adlandırmak, tip değiştirmek). Bozmayan değişikliğe **geriye dönük uyumlu** (backward compatible) denir.
- **Genişlet-ve-daralt (expand and contract)**: Kırıcı değişikliği üç adıma bölme tekniği: önce yeniyi ekle, bir süre ikisini birlikte yayınla, istemciler geçince eskiyi kaldır.
- **Kullanımdan kaldırma ve sürümleme (deprecation, versioning)**: Bir alanın/ucun emekliye ayrılacağını önceden duyurmak (`Deprecation` ve `Sunset` HTTP başlıkları) ve gerekirse API'nin yeni bir sürümünü (URL yolunda ya da medya tipinde) yayınlamak.
- **Özellik bayrağı (feature flag)**: Kodu yayınlayıp ekranı/davranışı bir anahtarla sonradan açma yöntemi.
- **Geri alma (rollback)**: Yeni yayını iptal edip önceki sürüme dönmek.
- **Nesne düzeyi yetki açığı (BOLA/IDOR)**: İstemcinin gönderdiği kimliğe (id) körü körüne güvenip başkasının kaydını döndürme açığı; ayrıntısı `189-guvenlik-mimari-owasp.md` konusunda.

### Nasıl çalışır?

**Sözleşme-önce mi, kod-önce mi?** Kod-önce yaklaşımda önce Java tarafını yazarsın, `springdoc-openapi` sınıflarından OpenAPI belgesini üretir. Hızlıdır ve belge kodla otomatik uyumludur. Ama iki bedeli vardır. Birincisi, ön yüz ekibi belge çıkana kadar **seni bekler**. İkincisi, sözleşmenin tartışmalı yerleri — alan adı, hata biçimi, sayfalama — ancak kod yazıldıktan **sonra** keşfedilir; düzeltmek artık kodu yeniden düzenlemek (refactor) demektir. Sözleşme-önce yaklaşımda ise OpenAPI belgesi önce yazılır. Tartışma en ucuz olduğu anda, hiç kod yokken yapılır; iki ekip belgeye bakarak aynı gün başlar. Bedeli disiplindir: belge kodla birlikte güncellenmezse yalan söylemeye başlar.

Pratik kural: **birden çok tüketicisi olan veya ayrı bir ekibin beklediği** API'lerde sözleşme-önce; yalnız kendi kullandığın dahili bir uçta ya da hızlı bir prototipte kod-önce yeterlidir.

```svg
<svg viewBox="0 0 640 248" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kod-önce yaklaşımda ön yüz bekler, sözleşme-önce yaklaşımda iki ekip paralel çalışır">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kod-önce mi, sözleşme-önce mi? (zaman soldan sağa)</text>
  <text x="16" y="52" font-size="11.5" font-weight="700" fill="#B67A1A" font-family="system-ui">Kod-önce</text>
  <rect x="16" y="60" width="120" height="34" rx="8" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.4"/>
  <text x="76" y="82" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">backend kodlar</text>
  <rect x="144" y="60" width="150" height="34" rx="8" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.4"/>
  <text x="219" y="82" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">belge koddan üretilir</text>
  <rect x="302" y="60" width="150" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="377" y="82" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ön yüz YENİ başlar</text>
  <text x="470" y="82" font-size="10.5" fill="#5D7789" font-family="system-ui">toplam süre uzar</text>
  <rect x="16" y="100" width="278" height="26" rx="7" fill="#FCEDEC" stroke="#B3261E" stroke-width="1.3"/>
  <text x="155" y="117" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B3261E" font-family="system-ui">ön yüz bu süre boyunca BEKLER</text>
  <line x1="16" y1="142" x2="624" y2="142" stroke="#D6DEE4" stroke-width="1"/>
  <text x="16" y="164" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Sözleşme-önce</text>
  <rect x="16" y="172" width="130" height="58" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="81" y="196" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">OpenAPI önce</text>
  <text x="81" y="213" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yazılır</text>
  <rect x="156" y="172" width="290" height="26" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="301" y="189" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">backend uygular</text>
  <rect x="156" y="204" width="290" height="26" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="301" y="221" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ön yüz taklit (mock) ile ilerler</text>
  <text x="462" y="196" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">PARALEL</text>
  <text x="462" y="216" font-size="10" fill="#5D7789" font-family="system-ui">bedeli: disiplin ister</text>
</svg>
```

**Taklit (mock) sunucu ve kayma tuzağı.** Sözleşme yazıldıktan sonra ön yüz, OpenAPI belgesinden üretilen bir taklit sunucuya konuşarak ekranı bitirebilir. Buradaki klasik hata, taklit yanıtları **elle** yazmaktır: gerçek uç birkaç hafta içinde değişir, taklit değişmez ve ikisi birbirinden **kayar (drift)**. Ön yüz taklit karşısında kusursuz çalışır, gerçek backend'e bağlandığı gün patlar. Çare mekaniktir: taklit sunucu **sözleşmeden üretilsin**, elle bakım gerektirmesin; sözleşme değişince taklit de kendiliğinden değişsin.

**Sözleşme testi.** Kaymayı sadece "üretmek" tam kapatmaz; asıl güvence, sözleşmeye uyumun **CI'da otomatik doğrulanmasıdır**. Tüketici-güdümlü sözleşmede ön yüz "şu uçtan şu alanları, şu tiplerde bekliyorum" beklentisini bir dosyaya yazar; backend'in CI hattı bu beklentiyi gerçek uca karşı doğrular. Kırdıysan derlemede kırmızı görürsün — üretimde değil. Uçtan uca (end-to-end) testten farkı üç noktadadır: **hızlıdır** (tüm sistemi ayağa kaldırmaz), **kırılgan değildir** (ekran değişince kırılmaz) ve arıza anında **kimin kırdığını** doğrudan gösterir. Uçtan uca testi bitirmez, sayısını azaltır.

Spring tarafında bunun ucuz karşılığı iki parçalıdır. Birincisi, yanıt şeklini `@WebMvcTest` + MockMvc ile kilitleyen bir test; burada alan adları ve tipleri iddia edilir (dilim testlerinin ayrıntısı `90-spring-boot-test.md` konusunda). İkincisi, `springdoc` ile üretilen OpenAPI belgesini depodaki sürümle karşılaştıran bir **fark (diff) denetimi**. Belge beklenmedik şekilde değiştiyse derleme durur; kırıcı değişiklik gözden kaçarak yayına gidemez.

**Kırıcı değişiklik yönetimi.** Yanıta yeni bir alan **eklemek** genelde zararsızdır: bilinmeyen alanı yok sayan istemciler etkilenmez. Riskli olanlar şunlardır: alan **silmek**, **yeniden adlandırmak**, **tipini değiştirmek** (`"123"` yerine `123`), zorunlu bir istek alanı eklemek ve bir enum'a **yeni değer** eklemek (istemci bilmediği değerde çuvallayabilir). Bunları tek yayında yapmanın adı üretimde kesintidir. Doğru teknik **genişlet-ve-daralt**tır.

```svg
<svg viewBox="0 0 640 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Genişlet ve daralt: yeni alanı ekle, ikisini birlikte yayınla, sonra eskiyi kaldır">
  <defs>
    <marker id="on-yuz-ekibiyle-isbirligi-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Genişlet ve daralt (expand and contract)</text>
  <rect x="16" y="44" width="184" height="120" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="108" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">1 · GENİŞLET</text>
  <text x="108" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yeni alan eklenir</text>
  <text x="108" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">eski alan durur</text>
  <text x="108" y="136" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">uyumlu: kimse kırılmaz</text>
  <line x1="204" y1="104" x2="224" y2="104" stroke="#5D7789" stroke-width="1.5" marker-end="url(#on-yuz-ekibiyle-isbirligi-ok)"/>
  <rect x="228" y="44" width="184" height="120" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">2 · BİRLİKTE</text>
  <text x="320" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">iki alan bir süre yayında</text>
  <text x="320" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istemciler yeniye geçer</text>
  <text x="320" y="136" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Deprecation / Sunset duyurusu</text>
  <line x1="416" y1="104" x2="436" y2="104" stroke="#5D7789" stroke-width="1.5" marker-end="url(#on-yuz-ekibiyle-isbirligi-ok)"/>
  <rect x="440" y="44" width="184" height="120" rx="10" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.5"/>
  <text x="532" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#B67A1A" font-family="system-ui">3 · DARALT</text>
  <text x="532" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">eski alan kaldırılır</text>
  <text x="532" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kullanım ölçülerek</text>
  <text x="532" y="136" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kesintisiz sonuç</text>
  <text x="320" y="190" text-anchor="middle" font-size="10.5" fill="#B3261E" font-family="system-ui">Tek adımda silmek = üretimde kesinti. Her değişiklikte yeni sürüm = n sürümü canlı tutma maliyeti.</text>
</svg>
```

Somut hâli, aynı alanın iki adla bir süre birlikte dönmesidir:

```json
// 1) GENİŞLET — yeni alan eklendi, eski alan hâlâ dolduruluyor
{ "musteriAdi": "Ayse Yilmaz", "adSoyad": "Ayse Yilmaz" }

// 3) DARALT — istemciler geçtikten sonra eski alan kaldırılır
{ "adSoyad": "Ayse Yilmaz" }
```

Sürümleme son çaredir, ilk çare değildir. Sürüm ya URL yolunda verilir (`/v2/...`) ya da medya tipinde (`application/vnd.firma.v2+json`). Her değişiklikte yeni sürüm çıkarmak, **n sürümü aynı anda canlı tutma** maliyetini doğurur: n kat bakım, n kat test, n kat güvenlik yaması. "Nasılsa sürüm atarız" demek maliyeti görmezden gelmektir.

**Dağıtım eşzamansızlığı.** Ön yüz ve backend ayrı yayınlanır; bu yüzden "önce hangisi çıkar?" gerçek bir sorudur. Kural: **geriye dönük uyumlu backend önce çıkar**, ön yüz sonra. Tersini yaparsan ön yüz henüz olmayan bir alanı ister ve ekran, backend yayınlanana kadar bozuk kalır. Ekranı kullanıcıya ne zaman göstereceğini **özellik bayrağıyla** ayır: kod yayında olsun, bayrak hazır olduğunda açılsın. **Geri alma** senaryosunu da düşün: ön yüz eski sürüme dönerse eski istek gövdesini göndermeye başlar — backend bir süre daha o gövdeyi karşılayabilmelidir. Yani uyumluluk yalnız ileri değil, **geriye** doğru da gerekir.

**Sınır tartışmaları: kim ne yapar?** Doğrulama (validation) iki tarafta da yapılır ama amaçları farklıdır. Ön yüzde **kullanıcı deneyimi** için yapılır: anında geri bildirim vermek ve sunucuya boşuna gitmemek. Arka uçta (backend) ise **güvenlik** için yapılır; asıl olan budur. Çünkü istek tarayıcı arayüzünden geçmek zorunda değildir; herkes doğrudan HTTP isteği gönderebilir. Mülakatta "ön yüz zaten kontrol ediyor" cevabı kırmızı bayraktır.

Aynısı yetkilendirme için daha da serttir. Ön yüzde saklanan role bakarak karar verilmez. Ön yüz butonu **gizler**, arka uç erişimi **reddeder**. Gizlemek koruma değildir; gizlenmiş uca doğrudan istek atılırsa nesne düzeyi yetki açığı (BOLA/IDOR) doğar.

Buna karşılık yerelleştirme metni ve biçimlendirme ön yüzün işidir: tarih, para birimi, ondalık ayracı gibi. Arka uç ham ve standart veriyi döndürür — örneğin ISO 8601 tarih, kuruş cinsinden tamsayı tutar — sunumu istemci yapar. İş kuralının tek kaynağı ise arka uçtur.

```svg
<svg viewBox="0 0 640 244" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sorumluluk sınırı tablosu: doğrulama, yetkilendirme, biçimlendirme ve iş kuralı kimde">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sorumluluk sınırı: hangi iş kimde?</text>
  <rect x="16" y="36" width="608" height="30" rx="6" fill="#1C3247"/>
  <text x="26" y="56" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Konu</text>
  <text x="180" y="56" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Ön yüz</text>
  <text x="330" y="56" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Backend</text>
  <text x="452" y="56" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Neden</text>
  <rect x="16" y="68" width="608" height="40" fill="#FCEDEC" stroke="#D6DEE4" stroke-width="1"/>
  <text x="26" y="93" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Doğrulama</text>
  <text x="180" y="93" font-size="10" fill="#5D7789" font-family="system-ui">hızlı geri bildirim</text>
  <text x="330" y="93" font-size="10" font-weight="700" fill="#B3261E" font-family="system-ui">ZORUNLU</text>
  <text x="452" y="93" font-size="10" fill="#1C3247" font-family="system-ui">istek doğrudan gönderilebilir</text>
  <rect x="16" y="110" width="608" height="40" fill="#FCEDEC" stroke="#D6DEE4" stroke-width="1"/>
  <text x="26" y="135" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Yetkilendirme</text>
  <text x="180" y="135" font-size="10" fill="#5D7789" font-family="system-ui">butonu gizler</text>
  <text x="330" y="135" font-size="10" font-weight="700" fill="#B3261E" font-family="system-ui">REDDEDER</text>
  <text x="452" y="135" font-size="10" fill="#1C3247" font-family="system-ui">gizlemek koruma değildir</text>
  <rect x="16" y="152" width="608" height="40" fill="#E9F4EE" stroke="#D6DEE4" stroke-width="1"/>
  <text x="26" y="177" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Biçimlendirme</text>
  <text x="180" y="177" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">SUNAR</text>
  <text x="330" y="177" font-size="10" fill="#5D7789" font-family="system-ui">ham/ISO veri döner</text>
  <text x="452" y="177" font-size="10" fill="#1C3247" font-family="system-ui">yerel ayar istemcide bilinir</text>
  <rect x="16" y="194" width="608" height="40" fill="#EAF1F7" stroke="#D6DEE4" stroke-width="1"/>
  <text x="26" y="219" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">İş kuralı</text>
  <text x="180" y="219" font-size="10" fill="#5D7789" font-family="system-ui">yalnız gösterir</text>
  <text x="330" y="219" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">TEK KAYNAK</text>
  <text x="452" y="219" font-size="10" fill="#1C3247" font-family="system-ui">kural tek yerde yaşamalı</text>
</svg>
```

**Davranışsal boyut.** "Ön yüz ekibiyle anlaşmazlık yaşadın mı, nasıl çözdün?" sorusu bu konunun mülakattaki doğrudan karşılığıdır. Beklenen kalıp şudur. Tartışmayı **koda başlamadan** öne al. Kararı yazılı tut: OpenAPI belgesi, gerekirse bir ADR. "Onların işi" demek yerine ortak sözleşmeye referans ver. Acil durumda geçici bir taklit ya da uyarlayıcı (adapter) ile ilerle, ama doğan borcu **takip listesine** yaz. Yanlış kalıplar da aynı ölçüde bilinir: ön yüzü suçlamak, sözleşmeyi sözlü anlaşmayla yürütmek ve kırıcı değişikliği "nasılsa sürüm atarız" diyerek önemsizleştirmek.

### Mülakatta ne sorulur?

- **"Sözleşme-önce mi kod-önce mi, neden?"** → Ayrı ekip bekliyorsa veya birden çok tüketici varsa sözleşme-önce (paralellik + tartışma erken); dahili/prototip uçta kod-önce yeterli. Kod-önce belge üretebilir, ama belgeyi kod yazıldıktan sonra keşfettirir.
- **"Ön yüz backend hazır değilken nasıl ilerler?"** → OpenAPI'den **üretilen** taklit sunucu. Elle yazılan taklit kayar (drift); üretilen taklit sözleşmeyle birlikte değişir.
- **"Sözleşme testi ile uçtan uca test farkı?"** → Sözleşme testi hızlıdır, kırılgan değildir ve kimin kırdığını gösterir; uçtan uca test sistemi bütün olarak doğrular ama yavaş ve gürültülüdür. Sözleşme testi uçtan ucayı bitirmez, azaltır.
- **"Bir alanı yeniden adlandırman gerekiyor, nasıl?"** → Genişlet-ve-daralt: ekle → bir süre ikisini birlikte yayınla + `Deprecation`/`Sunset` duyur → kullanım bitince kaldır. Tek adımda silmek kesintidir.
- **"Ön yüz mü backend mi önce yayınlanır?"** → Geriye dönük uyumlu backend önce; ön yüz sonra. Ekranı özellik bayrağıyla aç. Geri alma ihtimaline karşı backend eski gövdeyi bir süre daha kabul etmelidir.
- **"Ön yüz doğruluyorsa backend'de tekrar gerekli mi?"** → Evet, zorunlu. Ön yüz doğrulaması deneyim içindir; güvenlik sınırı backend'dir. Yetki kontrolü ise **asla** istemcideki role bırakılmaz.
- **"Ön yüz ekibiyle bir anlaşmazlığı anlat."** → Sözleşmeyi koddan önce tartışmak, kararı yazıya dökmek, ortak belgeye referans vermek, aciliyette geçici çözümle ilerleyip borcu takip etmek.

### Tek cümlede

Ön yüzle iş birliği üç şeydir: aradaki sözleşmeyi koddan önce yazılı hâle getirip taklit sunucu ve sözleşme testiyle canlı tutmak, kırıcı değişikliği genişlet-ve-daralt ile kesintisiz çıkarmak ve güvenlik sınırının her zaman arka uç olduğunu bilmek.

# isbr-1 | Temel
## Soru
Ön yüz ve backend ekipleri aynı özelliğe birlikte başlayacak. **Sözleşme-önce (contract-first)** yaklaşımının kod-önceye göre sağladığı temel kazanç nedir?
## A
Kod-önce yaklaşımda OpenAPI belgesi hiçbir şekilde üretilemez; sözleşme-önce, belge sahibi olmanın tek yoludur.
🤖 Kod-önce de belge üretir (`springdoc-openapi`); fark belgenin ne zaman ortaya çıktığındadır.
## B
Sözleşme kod yazılmadan önce anlaşıldığı için iki ekip aynı gün paralel başlar ve tartışma en ucuz anda yapılır.
🤖 Doğru: belge önce netleşince paralellik doğar, tartışma koddan önceye alınır.
## C
Backend'in daha hızlı kod yazmasını sağlar; ekstra disiplin gerektirmediği için her projede kod-önceden üstündür.
🤖 Sözleşme-önce backend'i hızlandırmaz ve disiplin ister; dahili/prototip uçlarda kod-önce yeterlidir.
## D
Ön yüzün backend'i beklemesini güvence altına alır; sıralı çalışma yanlış anlaşılmaları baştan engeller.
🤖 Bekleme kod-önce yaklaşımın sonucudur; sözleşme-önce tam olarak beklemeyi ortadan kaldırır.
## Doğru
B
## Açıklama
**Sözleşme-önce**, OpenAPI belgesinin koddan önce yazılması demektir: alan adları, tipler, hata biçimi ve sayfalama en ucuz olduğu anda — hiç kod yokken — tartışılır ve iki ekip aynı belgeye bakarak **paralel** başlar. **Kod-önce** yaklaşımda belge yine üretilir (`springdoc-openapi` sınıflardan çıkarır), yani A yanlıştır; ama belge ancak kod yazıldıktan sonra ortaya çıkar, bu yüzden ön yüz **bekler** ve sözleşmenin tartışmalı yerleri geç keşfedilir — düzeltmek artık refactor demektir. D bu bekleme durumunu sözleşme-önceye yanlışlıkla atfeder. C ise iki hata yapar: sözleşme-önce backend'in yazma hızını artırmaz ve **disiplin ister** (belge kodla birlikte güncellenmezse yalan söyler). Pratik kural: ayrı bir ekip bekliyorsa ya da birden çok tüketici varsa sözleşme-önce, yalnız kendi kullandığın dahili uçta kod-önce yeterlidir.

# isbr-2 | Temel
## Soru
Ön yüz ekibi, backend hazır olmadan ilerlemek için bir taklit (mock) sunucu kullanıyor. Bu yaklaşımın en bilinen tuzağı ve doğru çaresi nedir?
## A
Elle yazılan taklit yanıtlarla gerçek uç zamanla birbirinden kayar; çare, taklidi sözleşmeden üretmektir.
🤖 Doğru: kayma (drift) asıl tuzaktır, çaresi taklidi elle yazmayıp OpenAPI'den üretmektir.
## B
Taklit sunucu ön yüzü tembelleştirir; çare, ön yüzün backend tümüyle bitene kadar hiç kod yazmamasıdır.
🤖 Taklidin amacı zaten beklemeyi kaldırmaktır; beklemek çare değil, çözülmek istenen sorundur.
## C
Taklit sunucu HTTP hata kodlarını taşıyamaz; çare, hata senaryolarını yalnız uçtan uca testlerde denemektir.
🤖 Taklit sunucu hata kodlarını da döndürebilir; sorun hata kodları değil sözleşmeden kaymadır.
## D
Taklit sunucu sözleşmenin yerini alır; çare, OpenAPI belgesini bırakıp taklit dosyalarını tek kaynak saymaktır.
🤖 Tek doğruluk kaynağı sözleşmedir; taklit ondan türetilen bir çıktıdır, yerine geçmez.
## Doğru
A
## Açıklama
Taklit (mock) sunucu, ön yüzün backend hazır olmadan ekranı bitirmesini sağlar — amacı tam olarak beklemeyi ortadan kaldırmaktır (bu yüzden B kendi çözmek istediği sorunu geri getirir). Asıl tuzak **kayma (drift)**: taklit yanıtlar elle yazıldığında gerçek uç haftalar içinde değişir, taklit olduğu yerde kalır; ön yüz taklit karşısında kusursuz çalışır, gerçek backend'e bağlandığı gün patlar. Çare mekaniktir: taklit **OpenAPI sözleşmesinden üretilsin** (Prism, WireMock, MSW gibi araçlar bunu yapar), böylece sözleşme değişince taklit de kendiliğinden değişir. C yanlış bir teknik kısıt uydurur; taklit sunucular hata kodlarını da döndürebilir. D ise yönü tersine çevirir: tek doğruluk kaynağı sözleşmedir, taklit ondan türeyen bir çıktıdır.

# isbr-3 | Orta
## Soru
Yayında olan ve ön yüz tarafından kullanılan bir REST ucunun **yanıtında** yapacağın hangi değişiklik istemciyi kırma riski en yüksektir?
## A
Yanıta yeni ve isteğe bağlı bir alan eklemek; bilinmeyen alanları yok sayan istemciler bu eklemeden etkilenmez.
🤖 Alan eklemek genelde geriye dönük uyumludur; bilinmeyen alan yok sayılır.
## B
Var olan bir alanın OpenAPI belgesindeki açıklama (description) metnini daha anlaşılır olacak şekilde güncellemek.
🤖 Açıklama metni belgelemedir; çalışma zamanındaki gövdeyi değiştirmez.
## C
Aynı kaynak için tümüyle yeni bir uç nokta eklemek; eski uç adresi ve gövdesi olduğu gibi yayında kalır.
🤖 Yeni uç eklemek eskiyi bozmaz; mevcut istemciler eski ucu kullanmayı sürdürür.
## D
Var olan bir alanı yeniden adlandırmak veya tipini değiştirmek; istemci alanı bulamaz ya da ayrıştıramaz.
🤖 Doğru: silme, yeniden adlandırma ve tip değişimi klasik kırıcı değişikliklerdir.
## Doğru
D
## Açıklama
Geriye dönük uyumluluğun temel ayrımı şudur: **eklemek** genelde güvenlidir, **çıkarmak veya anlamını değiştirmek** kırıcıdır. Yanıta yeni bir alan eklemek (A) çoğu istemcide zararsızdır çünkü bilinmeyen alanlar yok sayılır; belgedeki açıklama metnini güncellemek (B) çalışma zamanı gövdesine hiç dokunmaz; yeni bir uç eklemek (C) eskiyi olduğu yerde bırakır. Buna karşılık bir alanı **yeniden adlandırmak** istemcide o alanı yok eder, **tipini değiştirmek** (`"123"` yerine `123`) ayrıştırmayı bozar — ikisi de klasik **kırıcı değişikliktir** (breaking change). Aynı sınıfa girenler: alan silmek, zorunlu bir istek alanı eklemek ve bir enum'a yeni değer eklemek (istemci bilmediği değerde çuvallayabilir). Bu tür bir değişiklik tek yayında yapılmaz; genişlet-ve-daralt ile üç adıma bölünür.

# isbr-4 | Orta
## Soru
Yanıttaki `musteriAdi` alanını `adSoyad` olarak yeniden adlandırman gerekiyor; ön yüz ayrı yayınlanıyor. **Genişlet-ve-daralt** (expand and contract) nasıl uygulanır?
## A
Aynı yayında eski alanı silip yenisini eklersin; ön yüz ekibine önceden haber vermen ve tarihi bildirmen yeterlidir.
🤖 Haber vermek yayınları eşzamanlı yapmaz; tek adımda silmek üretimde kesinti demektir.
## B
Önce yalnız ön yüzü yayınlarsın, yeni alanı okumaya başlar; backend bir sonraki sürümde alanı eklemeye geçer.
🤖 Ön yüzü önce çıkarmak henüz olmayan alanı istemek olur; uyumlu backend önce yayınlanır.
## C
Önce yeni alanı eklersin, iki alanı bir süre birlikte yayınlarsın; istemciler geçtikten sonra eskiyi kaldırırsın.
🤖 Doğru: ekle, bir süre birlikte yayınla, kullanım bitince kaldır.
## D
Her yeniden adlandırmada yeni bir API sürümü çıkarırsın; eski sürümü süresiz canlı tutarsan geçişe hiç gerek kalmaz.
🤖 Sürümleme son çaredir; n sürümü canlı tutmak n kat bakım, test ve yama maliyeti doğurur.
## Doğru
C
## Açıklama
**Genişlet-ve-daralt**, kırıcı bir değişikliği kesintisiz üç adıma böler: (1) **genişlet** — yeni `adSoyad` alanı eklenir, `musteriAdi` doldurulmaya devam eder, bu adım geriye dönük uyumludur; (2) **birlikte yayın** — iki alan bir süre birlikte döner, `Deprecation`/`Sunset` başlıklarıyla eskinin emekliliği duyurulur, istemciler kendi hızlarında geçer; (3) **daralt** — kullanım ölçülerek sıfırlandığında eski alan kaldırılır. A tek adımda silmeyi önerir: iki ekip aynı saniyede yayınlayamayacağı için bu üretimde kesintidir; haber vermek bunu değiştirmez. B yayın sırasını tersine çevirir — kural, **geriye dönük uyumlu backend'in önce** çıkması, ön yüzün sonra gelmesidir. D sürümlemeyi ilk çare sanır; oysa her değişiklikte sürüm çıkarmak **n sürümü aynı anda canlı tutma** maliyetini (n kat bakım, test, güvenlik yaması) doğurur ve geçiş ihtiyacını ortadan kaldırmaz.

# isbr-5 | İleri
## Soru
Mülakatçı soruyor: "Ön yüz formu zaten doğruluyor ve yetkisiz kullanıcıya butonu hiç göstermiyor. Backend'de bunları tekrarlamak gereksiz iş değil mi?" En doğru cevap hangisidir?
## A
Ön yüz kontrolü deneyim içindir; istek doğrudan da gönderilebildiğinden doğrulama ve yetki kararı backend'de zorunludur.
🤖 Doğru: güvenlik sınırı backend'dir; istemci kontrolü yalnız deneyimi iyileştirir.
## B
Haklı; ön yüz kontrol ediyorsa backend'de tekrarlamak çift bakım maliyetidir, veritabanı kısıtları güvenlik için yeterlidir.
🤖 Veritabanı kısıtı iş kuralını ve yetkiyi karşılamaz; doğrulama backend'de atlanamaz.
## C
Backend yalnız doğrulamayı tekrarlamalı; yetki kararı ön yüzdeki role bırakılabilir, çünkü rol bilgisi oturumdan gelir.
🤖 Rolün kaynağı ne olursa olsun yetki kararı istemciye bırakılmaz; backend erişimi kendisi reddetmelidir.
## D
Backend hiç doğrulama yapmamalı; tüm isteklerin API ağ geçidinde şemaya göre süzülmesi bu ihtiyacı tamamen karşılar.
🤖 Ağ geçidi şema süzgeci iş kuralı doğrulaması ve nesne düzeyi yetki kontrolü yerine geçmez.
## Doğru
A
## Açıklama
Doğrulama iki tarafta da yapılır ama amaçları farklıdır: ön yüzde **kullanıcı deneyimi** için (anında geri bildirim), backend'de **güvenlik** için — ve belirleyici olan ikincisidir. İstek tarayıcı arayüzünden geçmek zorunda değildir; herkes doğrudan HTTP isteği gönderebilir, dolayısıyla istemcideki hiçbir kontrol güvence sayılmaz. B'nin önerdiği veritabanı kısıtları yalnız veri bütünlüğünün bir kısmını korur, iş kurallarını ve yetkiyi karşılamaz. C daha da tehlikelidir: ön yüz yetkisiz butonu **gizler**, ama gizlemek koruma değildir — gizli uca doğrudan istek atılırsa nesne düzeyi yetki açığı (BOLA/IDOR) doğar; backend erişimi **kendisi reddetmelidir**. D'deki ağ geçidi şema süzgeci gövde biçimini kontrol edebilir ama "bu kayıt bu kullanıcıya mı ait" sorusunu yanıtlayamaz. Mülakatta "ön yüz zaten kontrol ediyor" cevabı kırmızı bayraktır.

# isbr-6 | İleri
## Soru
"Ön yüz ekibiyle bir anlaşmazlık yaşadın mı, nasıl çözdün?" sorusuna verilen hangi cevap kıdemli (senior) sinyali taşır?
## A
"Sözleşme benim işim değil dedim; ön yüz ekibi benim döndürdüğüm gövdeye uyum sağlasın diye konuyu kapatıp geliştirmeye devam ettim."
🤖 "Onların işi" tavrı ortak sözleşmeyi tek taraflı dayatmaktır; iş birliği sinyalini yok eder.
## B
"Tartışmayı koda başlamadan sözleşme üzerinden yaptık; kararı OpenAPI ve ADR'ye yazdık, acilde geçici uyarlayıcıyla ilerleyip borcu takip ettik."
🤖 Doğru: erken tartışma, yazılı karar, ortak sözleşmeye referans ve takip edilen borç.
## C
"Toplantıda sözlü anlaştık, yazıya dökmedik; kırıcı değişiklik çıkınca da nasılsa sürüm atarız diyerek konuyu kapatıp hızlıca yolumuza devam ettik."
🤖 Sözlü sözleşme unutulur ve "sürüm atarız" kırıcı değişikliğin maliyetini görmezden gelir.
## D
"Gecikmenin sorumlusunun ön yüz ekibi olduğunu yöneticiye ilettim; teslimin kaymasını onların hanesine yazdırıp kendi planladığım işime geri döndüm."
🤖 Suçu başka ekibe yıkmak davranışsal mülakatta en zayıf kalıptır; sorunu çözmez, sahiplik göstermez.
## Doğru
B
## Açıklama
Bu soruda ölçülen şey teknik bilgi değil, **sınır yönetimi olgunluğudur**. Beklenen kalıbın dört parçası vardır: tartışmayı **koda başlamadan** öne almak (en ucuz an), kararı **yazılı** tutmak (OpenAPI belgesi, gerekirse bir ADR), "onların işi" demek yerine **ortak sözleşmeye referans vermek**, ve aciliyette geçici bir taklit/uyarlayıcı ile ilerleyip doğan borcu **takip listesine yazmak** — B tam olarak bunu söyler. A ortak sözleşmeyi tek taraflı dayatır ve iş birliği sinyalini siler. C iki bilinen yanlışı birleştirir: sözleşmeyi sözlü anlaşmayla yürütmek (unutulur, "neden"i kaybolur) ve kırıcı değişikliği "nasılsa sürüm atarız" diye önemsizleştirmek (n sürümü canlı tutmanın maliyetini görmezden gelmek). D ise suçu başka ekibe yıkar; sorunu çözmediği gibi sahiplik de göstermez.
