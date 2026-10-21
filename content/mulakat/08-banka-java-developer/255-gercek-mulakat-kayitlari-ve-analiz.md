---
slug: gercek-mulakat-kayitlari-ve-analiz
baslik: "Gerçek Mülakat Kayıtları: Sorulan 45 Soru, Konu Eşlemesi ve Eksik Kalan Cevapların Analizi"
kademe: Orta
sira: 255
etiketler: 10
---

## Konu anlatımı

### Bu ne işe yarar?

Bu setteki diğer konular "muhtemelen sorulur" varsayımıyla yazılmıştır. Bu konu farklıdır: burada anlatılan sorular **gerçekten soruldu**. 4 Ağustos 2026'da bir teknik görüşme yapıldı. Görüşme, bir danışmanlık firması (BilgeAdam) üzerinden, bir bankanın kredilendirme ekibi içindi. O görüşmede sorulan 45 sorunun tamamı kayıt altına alındı. Görüşmeci ayrıca sözlü bir geri bildirim verdi.

Elinde böyle bir kayıt olduğunda hazırlık artık tahmin değil, **ölçüm** üzerine kurulur. Hangi alandan kaç soru geldiğini bilirsin. Hangi alanın hiç sorulmadığını bilirsin. Hangi cevabın yetersiz bulunduğunu ise doğrudan görüşmecinin ağzından bilirsin.

Bu konunun iki işi var. Birincisi, 45 sorunun her birini bu setteki konu numarasına eşlemektir. Böylece "neyi tekrar edeceğim?" sorusu bir listeye dönüşür. İkincisi, sürecin kendisini okumaktır: danışmanlık firması üzerinden ilerleyen bir başvurunun kaç aşaması vardır, teknik görüşme neye benzer, verilen geri bildirim nasıl okunur ve bir sonraki görüşmede nasıl telafi edilir. Teknik derinlik burada yoktur; her soru kendi konusuna yönlendirilir. Burada olan şey **saha verisidir**.

### Temel kavramlar

- **Görüşme dökümü (debrief):** görüşmeden hemen sonra, hafıza tazeyken sorulan soruların ve alınan tepkilerin yazıya dökülmesidir. Aradan bir gün geçtiğinde soruların yarısı kaybolur. Bu yüzden döküm, görüşmeyle aynı gün yapılır.
- **Konu eşlemesi:** sözlü olarak sorulan bir sorunun, çalışma materyalindeki numaralı konuya bağlanmasıdır. "Vekil nesne (proxy bean) nedir?" sorusunun karşılığı `72`'dir. Eşleme yapılmazsa soru listesi tekrar planına dönüşmez.
- **Dağılım analizi:** soruların alanlara göre sayısal ağırlığıdır. Görüşmecinin neye önem verdiğini niyetinden değil, **soru sayısından** okursun.
- **Danışmanlık (consultancy) modeli:** adayın, danışmanlık firmasının bordrosunda çalışmasıdır. Fiilen ise müşteri şirkette, yani burada bankada, yerleşik olarak görev yapar.
- **İki aşamalı teknik değerlendirme:** danışmanlık firmasının kendi teknik görüşmesi ile müşteri şirketin ayrıca yaptığı teknik görüşmedir. İkisi aynı görüşme değildir ve ikincisi genellikle daha derindir.
- **Telafi cevabı:** ilk görüşmede eksik bulunan bir konunun, ikinci görüşmede kendiliğinden ve açıkça kapatılmasıdır. "Geçen sefer bunu eksik buldunuz, çalıştım" cümlesi bir zayıflık itirafı değildir; olgunluk sinyalidir.

### Sürecin yapısı: İK, teknik, teklif

**Kaynak ayrımı — bu bölümü okurken dikkat et.** Aşağıda iki farklı cinsten bilgi var ve bunlar karıştırılmamalıdır. **Kayıt** diye işaretlenenler 4 Ağustos görüşmesinin dökümünden gelir; doğrudan yaşanmıştır. **Genel** diye işaretlenenler ise bu görüşmeden gelmez. Onlar danışmanlık modeli hakkındaki yaygın sektör pratikleridir; bu firma için doğrulanmış değildir ve öyle sunulmamalıdır. Mülakatta yalnızca birinci gruba dayan.

**Kayıt.** Süreç üç ana adımdan oluşur: **İK görüşmesi → teknik görüşme → teklif**. Teknik görüşme yaklaşık bir saat sürdü ve tamamen **sözlü sohbet tarzındaydı**. Sorular sırayla soruldu ve verilen cevaba göre derinleşti. Canlı kodlama (live coding) bu görüşmede yoktu; ekran paylaşımıyla algoritma yazdırma adımı raporlanmadı.

**Genel.** Danışmanlık firmaları üzerinden yürüyen süreçlerde teknik görüşmenin öncesine veya sonrasına bir **çevrim içi değerlendirme adımı** girebilir. Bu adım bir yetenek testi ya da standart bir teknik test olabilir. Bu süreçte böyle bir adım olup olmayacağı bilinmiyor. Varsayma, **sor**: "Kaç aşama var, aralarında test var mı?" Sürecin başında sorulan bu tek soru belirsizliği kapatır.

Sorulan sorular kısa ve kesin cevap bekleyen türdendi. Örnekler: "primitive tipler nelerdir", "abstract sınıf ile interface farkı nedir", "proxy bean nedir". Verilen cevap doyurucu geldiğinde görüşmeci bir üst basamağa çıkıyordu. Tipik örneği şudur: `@Transactional` sorusundan sonra propagation, oradan isolation, oradan self-invocation geldi. Bu format bir şeyi çok net söyler: **yüzeysel tanım bilmek yetmez.** Çünkü tanımı doğru verdiğin her soru seni bir sonraki, daha derin soruya taşır. Zincirin nerede koptuğu da senin gerçek seviyeni gösterir.

**Genel.** Görüşmeci tavrı konusunda tek bir beklenti kurma. Aynı firmada bile görüşmeciden görüşmeciye üslup değişir. Kimi rahat ve yönlendiricidir, kimi mesafeli hatta serttir. Sert üslup bir değerlendirme sinyali değildir; çoğu zaman kişisel bir tarzdır. Bu görüşmede böyle bir şey yaşanmadı ama ikinci aşamada başka biriyle oturacaksın.

Yapılacak şey soğukkanlılığı korumaktır. Üsluba değil **soruya** cevap ver ve savunmaya geçme. Bilmediğin bir soruda üslup sertleştiğinde en kötü tepki uydurmaktır. Doğru tepki, bilmediğini söyleyip nasıl akıl yürüteceğini anlatmaktır (`238`).

### Danışmanlık firması modeli: neden iki teknik mülakata hazırlanmalısın?

Danışmanlık firması üzerinden işe girmenin çalışma biçimi şudur: **bordron danışmanlık firmasındadır ama fiilen müşteri bankada, bankanın ekibinin içinde çalışırsın.** Maaşını, izinlerini ve özlük işlerini danışmanlık firması yürütür. Günlük işini, sprint'ini ve kod incelemeni ise banka ekibi yürütür. Yaygın uygulamada bir-iki yıl sonra müşteri şirketin kadrosuna geçiş ihtimali doğar. Ancak bu bir taahhüt değildir ve firmadan firmaya değişir; görüşmede net biçimde sorulması gereken konulardan biridir.

Bunun hazırlık açısından doğrudan sonucu şudur: danışmanlık firmasının teknik görüşmesini geçmek **sürecin sonu değil, ortasıdır**. Firma profili müşteri bankaya iletir ve banka çoğu zaman **kendi teknik görüşmesini ayrıca yapar**. İkinci görüşme genellikle aynı konuların derinini sorar. Örnek: ilk görüşmede "Saga nedir?" sorulduysa, ikincisinde "bu akışı tahtada çizer misin, telafi adımı başarısız olursa ne yaparsın?" sorulur. Yani birinci görüşmeyi geçtikten sonra çalışmayı bırakmak, hazırlığın en pahalı hatasıdır.

```svg
<svg viewBox="0 0 660 302" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Danismanlik firmasi modeli ve iki asamali teknik mulakat akisi">
  <defs>
    <marker id="gercek-akis-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Danışmanlık firması üzerinden süreç — iki ayrı teknik görüşme</text>
  <rect x="20" y="40" width="108" height="52" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="74" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">1 · İK</text>
  <text x="74" y="78" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">beklenti, süreç</text>
  <path d="M130 66 L144 66" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#gercek-akis-arw)"/>
  <rect x="148" y="40" width="108" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="202" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">2 · Teknik-1</text>
  <text x="202" y="78" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">danışmanlık · ~1 saat</text>
  <path d="M258 66 L272 66" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#gercek-akis-arw)"/>
  <rect x="276" y="40" width="108" height="52" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="330" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">3 · Online test</text>
  <text x="330" y="78" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">girebilir · garanti değil</text>
  <path d="M386 66 L400 66" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#gercek-akis-arw)"/>
  <rect x="404" y="40" width="108" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="458" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">4 · Teknik-2</text>
  <text x="458" y="78" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">müşteri banka · derin</text>
  <path d="M514 66 L528 66" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#gercek-akis-arw)"/>
  <rect x="532" y="40" width="108" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="586" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">5 · Teklif</text>
  <text x="586" y="78" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ücret, başlangıç</text>
  <text x="330" y="118" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">ÇALIŞMA MODELİ</text>
  <rect x="60" y="130" width="250" height="58" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="185" y="152" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Bordro: danışmanlık firması</text>
  <text x="185" y="170" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">maaş · özlük · izin · sözleşme</text>
  <rect x="350" y="130" width="250" height="58" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="152" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Çalışma yeri: müşteri banka</text>
  <text x="475" y="170" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ekip · sprint · kod inceleme</text>
  <rect x="60" y="198" width="540" height="34" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="330" y="219" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">1-2 yıl sonra müşteri kadrosuna geçiş ihtimali — taahhüt değil, görüşmede net sorulmalı</text>
  <rect x="60" y="242" width="540" height="44" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="330" y="262" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Teknik-1'i geçmek bitiş değil: aynı konuların derini Teknik-2'de gelir</text>
  <text x="330" y="278" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">aracılı süreçte haber yavaşlar → görüşme sonunda net takvim iste</text>
</svg>
```

**Genel.** Aracılı süreçlerin bilinen bir zayıf noktası **geri bildirim gecikmesidir**. Görüşme yapılır, sonra sessizlik olur. Çünkü top artık aracıda değil, müşteri şirkettedir; zincir uzadıkça haber yavaşlar. Bunun basit ve tamamen meşru bir panzehiri var: görüşmenin son beş dakikasında, soru sorma faslında net takvim istemek. Şöyle sorabilirsin: "Sonucu ne zaman öğrenebilirim? Sonraki adım banka görüşmesi mi? O görüşme ne zaman planlanır?" Tarih verildiğinde takip etmek de artık nezaketsizlik değildir; konuşulmuş bir sürecin hatırlatılmasıdır.

### Sorulan 45 sorunun tam listesi ve konu eşlemesi

Aşağıdaki liste, görüşmede sorulan soruların tamamıdır. Her sorunun karşısında bu setteki konu numarası vardır. Kalın yazılanlar, görüşme öncesi hazırlıkta **eksik veya zayıf** olduğu tespit edilen sorulardır.

| Alan | Sorulan sorular → konu numarası | Adet |
|---|---|---|
| **Java temelleri** | primitive tipler `02` · final `15` · exception `16` · exception handling `16`,`17` · checked/unchecked `17` · **rollbackFor** `83` | 6 |
| **OOP ve kalıplar** | encapsulation `13` · abstract vs interface `12` · çoklu kalıtım `12` · Java 8 default metotlar `32` · functional interface `32` · Strategy `135` · **Factory vs Strategy** `133`,`135` | 7 |
| **Stream API** | map vs flatMap `34`,`35` · parallel stream `53` | 2 |
| **Spring** | IoC `68` · DI `68` · **proxy bean** `72` · bean scope singleton/prototype `69` · AOP `72` · **Interceptor** `76` (Filter↔Interceptor ayrımı) · `@Transactional` `83` · propagation `84` · isolation `224` · **self-invocation** `83` | 10 |
| **JPA** | N+1 `82` · lazy/eager `82` · JPQL/HQL/native `80`,`81` | 3 |
| **Mikroservis** | Kafka `107`-`118` · RabbitMQ `119`-`124` · Redis `155`-`166` · **Saga** `126` · **Outbox** `130`,`116` · circuit breaker `101` · **OpenFeign** `98`,`100` | 7 |
| **Eşzamanlılık** | **race condition** `46` · **thread-safe** `46` · **synchronized** `46`, `47` · **AtomicInteger** `51` · **volatile** `48`, `45` | 5 |
| **SQL** | şehir bazında ortalama maaş sorgusu `29` · index ve dezavantajları `146` · **partitioning vs sharding** `147` | 3 |
| **REST** | idempotent `130` · rate limit `153` | 2 |

Listeyi okurken dikkat edilecek üç şey var.

Birincisi, soruların çoğu **kısa ve tanım isteyen** sorulardır; egzotik hiçbir şey sorulmamıştır. İkincisi, aynı konunun etrafında halka halka dönen sorular vardır. Spring tarafındaki altı soruya bak: proxy bean, AOP, `@Transactional`, propagation, isolation ve self-invocation. Bunlar aslında **tek bir mekanizmanın** (`72`, `83`) altı ayrı yüzüdür.

Üçüncüsü, `@Transactional` ile ilgili `rollbackFor` sorusu, Java istisna hiyerarşisiyle (`16`, `17`) birleşen bir sorudur. Varsayılan davranış, yalnızca denetlenmeyen (unchecked) istisnalarda geri alma yapar. Denetlenen (checked) bir istisnada da geri alma istiyorsan, `rollbackFor` yazman gerekir.

### Dağılım analizi: 45 soru neyi anlatıyor?

```svg
<svg viewBox="0 0 660 306" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sorulan 45 sorunun konu alanlarina gore dagilimi">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">45 sorunun konu alanlarına göre dağılımı</text>
  <text x="150" y="42" text-anchor="end" font-size="10" fill="#5D7789" font-family="system-ui">alan</text>
  <text x="164" y="42" font-size="10" fill="#5D7789" font-family="system-ui">soru sayısı (1 soru = 30 px)</text>
  <text x="150" y="66" text-anchor="end" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Spring</text>
  <rect x="158" y="54" width="300" height="17" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="468" y="66" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">10</text>
  <text x="150" y="92" text-anchor="end" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">OOP / kalıplar</text>
  <rect x="158" y="80" width="210" height="17" rx="4" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="378" y="92" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">7</text>
  <text x="150" y="118" text-anchor="end" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Mikroservis</text>
  <rect x="158" y="106" width="210" height="17" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="378" y="118" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">7</text>
  <text x="150" y="144" text-anchor="end" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Java temelleri</text>
  <rect x="158" y="132" width="180" height="17" rx="4" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="348" y="144" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">6</text>
  <text x="150" y="170" text-anchor="end" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Eşzamanlılık</text>
  <rect x="158" y="158" width="150" height="17" rx="4" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="318" y="170" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">5 — beklenmiyordu</text>
  <text x="150" y="196" text-anchor="end" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">JPA</text>
  <rect x="158" y="184" width="90" height="17" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="258" y="196" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">3</text>
  <text x="150" y="222" text-anchor="end" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">SQL</text>
  <rect x="158" y="210" width="90" height="17" rx="4" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="258" y="222" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">3</text>
  <text x="150" y="248" text-anchor="end" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Stream API</text>
  <rect x="158" y="236" width="60" height="17" rx="4" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="228" y="248" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">2</text>
  <text x="150" y="274" text-anchor="end" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">REST</text>
  <rect x="158" y="262" width="60" height="17" rx="4" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="228" y="274" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">2</text>
  <text x="330" y="296" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Spring + JPA = 13 soru (%29) · mikroservis 7 · eşzamanlılık 5 — hazırlıkta en zayıf halka</text>
</svg>
```

Sayılar üç şey söylüyor.

**Spring ve veri katmanı merkezdedir.** Spring'den 10, JPA'dan 3 soru geldi. Toplam 13 soru, yani tüm görüşmenin yaklaşık üçte biri. Üstelik Spring soruları rastgele dağılmamıştır. Hepsi **vekil nesne (proxy) mekaniğinin etrafında** kümelenmiştir: proxy bean, AOP, `@Transactional`, propagation, isolation, self-invocation ve `rollbackFor` art arda gelmiştir.

Bu, görüşmecinin neyi ölçtüğünü gösterir. Ölçülen şey "Spring biliyor mu?" değildir; "**Spring'in işi nasıl yaptığını** biliyor mu?" sorusudur. Vekil nesnenin ne olduğunu bilmeyen biri, self-invocation sorusunu ezberden bile cevaplayamaz. Çünkü ikisi aynı mekanizmanın iki ucudur (`72`, `83`).

**Mikroservis ikinci sıradadır ve desen ağırlıklıdır.** 7 sorunun içinde Kafka, RabbitMQ ve Redis gibi araçlar var. Ama asıl ağırlık desenlerdedir: saga, giden kutusu tablosu (outbox), devre kesici, OpenFeign. Bu şu anlama gelir: ilanda yazan "mikroservis" kelimesi, görüşmede araç adı ezberi olarak sorulmadı. **Dağıtık tutarlılık problemi** olarak soruldu (`126`, `130`, `116`, `101`).

**Eşzamanlılık beklenmedik biçimde geldi ve hazırlığın kör noktasıydı.** Beş soru art arda soruldu: yarış koşulu (race condition), iş parçacığı güvenliği (thread-safe), `synchronized`, `AtomicInteger`, `volatile`. Bu blok hazırlık planında öncelikli değildi; en zayıf performans da bu yüzden burada verildi.

Alınacak ders şudur: banka tarafındaki bir kredilendirme ekibi için eşzamanlılık **süs değil, işin doğasıdır**. Aynı limitten iki kanaldan aynı anda düşüm yapılması gerçek bir üretim senaryosudur. `volatile` ile `synchronized` farkı da tam olarak bunu konuşabilmek için sorulur (`45`, `46`, `47`, `51`).

Dağılımın söylemediği bir şey de var: **hiç sorulmayan alanlar**. Docker/Kubernetes, güvenlik, gözlemlenebilirlik, JVM/GC ve alan bilgisi (bankacılık ürünleri) bu görüşmede geçmedi. Bunu "sorulmaz" diye okumak hatadır. Bu yalnızca **bir** görüşmenin kaydıdır. Müşteri banka görüşmesinde ilk sorulacak şeyin alan bilgisi olması hiç şaşırtıcı olmaz.

### Görüşmecinin geri bildirimi ve doğru okunuşu

Görüşmeci tek cümlelik bir geri bildirim verdi:

> "Saga pattern yapılarını, microservice vb. yapıları daha iyi bilmelisin."

Bu cümlenin yanlış okunuşu şudur: "Saga'nın tanımını bilmiyormuşum, tanımı ezberleyeyim." Doğru okunuşu ise şudur: **tanım verildi ama akış verilemedi.**

Şu cümleyi düşün: "Saga, dağıtık işlemi yerel işlemler dizisine bölen ve her adım için telafi işlemi tanımlayan desendir." Cümle doğrudur ama hiçbir şey ispatlamaz. Bu cümleyi bir blog yazısı okuyan herkes kurar. Görüşmecinin duymak istediği şey zincirdir:

adım çalışır → **başarısız olur** → hangi telafi işlemi tetiklenir → bu arada kayıt hangi **ara durumda** (`RESERVED`, `PENDING_CONFIRMATION`) bekler → cevap hiç gelmezse **zaman aşımı (timeout)** işini kim koşturur → neden koreografi değil **orkestrasyon** seçildi → orkestratörün kendi durumu nerede saklanır.

Bu zinciri anlatabilen kişi deseni uygulamıştır; anlatamayan kişi okumuştur. Aradaki farkı görüşmeci tek bir takip sorusuyla ölçer: "Peki telafi işlemi de başarısız olursa?"

Aynı şey giden kutusu tablosu (outbox) için de geçerlidir. Üç şeyi anlatabilmek gerekir. "Veritabanına yaz ve Kafka'ya yayınla" ikilisi neden atomik değildir? `@Transactional` içinde yayınlamak neden çift-yazma (dual write) problemi doğurur? Giden kutusu tablosu bunu neden çözer? (`130`, `116`) Devre kesicide de soru "nedir" diye kalmaz. "**Yarı-açık** durumda ne olur, kaç istek geçirilir, geri düşüş (fallback) ne döner" biçiminde derinleşir (`101`).

```svg
<svg viewBox="0 0 660 254" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Eksik bulunan cevabi ikinci gorusmede telafi etme dongusu">
  <defs>
    <marker id="gercek-telafi-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Geri bildirimi telafiye çevirme döngüsü</text>
  <rect x="20" y="42" width="146" height="60" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="93" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">1 · Geri bildirim</text>
  <text x="93" y="80" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">"saga ve mikroservisi</text>
  <text x="93" y="93" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">daha iyi bilmelisin"</text>
  <path d="M168 72 L184 72" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#gercek-telafi-arw)"/>
  <rect x="188" y="42" width="146" height="60" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="261" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">2 · Teşhis</text>
  <text x="261" y="80" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">tanım biliniyordu,</text>
  <text x="261" y="93" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">akış anlatılamadı</text>
  <path d="M336 72 L352 72" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#gercek-telafi-arw)"/>
  <rect x="356" y="42" width="146" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="429" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">3 · Zinciri çalış</text>
  <text x="429" y="80" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">adım → hata → telafi</text>
  <text x="429" y="93" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">→ ara durum → timeout</text>
  <path d="M504 72 L520 72" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#gercek-telafi-arw)"/>
  <rect x="524" y="42" width="116" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="582" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">4 · Kanıt</text>
  <text x="582" y="80" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">çalışan kod,</text>
  <text x="582" y="93" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">gösterilebilir demo</text>
  <path d="M582 104 L582 122 L96 122 L96 138" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#gercek-telafi-arw)"/>
  <rect x="20" y="142" width="620" height="46" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="162" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">5 · İkinci görüşmede kendin aç: "geçen sefer bunu eksik buldunuz, çalıştım, şöyle anlatayım"</text>
  <text x="330" y="180" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">geri bildirimi duyan, üstüne çalışan ve sonucu gösterebilen aday nadirdir — bu bir olgunluk sinyalidir</text>
  <rect x="20" y="198" width="620" height="42" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="330" y="217" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Yapılmayacaklar: geri bildirimi tartışmak · "aslında biliyordum" demek · sessizce geçiştirmek</text>
  <text x="330" y="232" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">savunma refleksi, eksiğin kendisinden daha olumsuz okunur</text>
</svg>
```

İkinci görüşmede bu geri bildirimi **kendin açmanın** değeri küçümsenmemeli. Adayların ezici çoğunluğu olumsuz geri bildirimi ya duymazdan gelir ya da savunmaya geçer. Şu cümleyi kurduğunu düşün: "Geçen görüşmede saga tarafını eksik bulmuştunuz; üstüne çalıştım, şu akışı kurdum, isterseniz anlatayım." Bu cümle üç şeyi aynı anda gösterir: geri bildirimi **dinlediğini**, üzerine **çalıştığını** ve eksiğini konuşabilecek kadar **güvende** olduğunu.

Bunu somut bir kanıta bağla. Örneğin çalışan bir servis ya da telafi adımını canlı tetikleyebileceğin bir gösterim. Böylece cevap iddiadan gösterime döner; teknik ekiplerin en çok değer verdiği geçiş budur. Yapılmaması gereken tek şey, geri bildirimi tartışmaya açmaktır. "Aslında biliyordum ama sormadınız" cümlesi, eksiğin kendisinden daha olumsuz okunur.

### Bu 45 sorudan çıkan tekrar rotası

Sıralama keyfî değildir; bir ölçüte göre kurulmuştur. Önce **çok sorulan ve zayıf olan** gelir. Sonra **çok sorulan ve iyi olan** gelir. En sona **az sorulan** kalır.

1. **Eşzamanlılık — `45`, `46`, `47`, `51`.** Beş soru geldi ama hazırlıkta yoktu. En yüksek getiri buradadır. `volatile` görünürlük sağlar ama atomiklik sağlamaz. `synchronized` ikisini birden verir. `AtomicInteger` ise CAS ile kilitsiz çalışır ama tek değişkenle sınırlıdır. Bunları limit düşümü örneğiyle birlikte anlatabilmek şart.
2. **Saga, giden kutusu tablosu, tekrar güvenliği — `126`, `130`, `116`.** Görüşmecinin doğrudan işaret ettiği alan. Tanım değil **zincir** çalışılacak. İkinci görüşmede tahtaya çizilebilecek kadar oturmalı.
3. **Spring vekil nesne ailesi — `72`, `83`, `84`, `224`.** Görüşmenin en yoğun bloğu. Şu zincirin tamamı tek nefeste anlatılabilmeli: proxy bean → AOP → `@Transactional` → propagation → isolation → self-invocation → `rollbackFor`.
4. **Interceptor ve Filter ayrımı — `76`.** Tek soru geldi ama zayıf kaldı; ucuz bir eksik. Sıralama şudur: Filter → DispatcherServlet → Interceptor → Controller. AOP'den farkı ise HTTP katmanına özgü olmasıdır.
5. **Factory ile Strategy ayrımı — `133`, `135`.** Klasik bir ayrım sorusudur. Factory **yaratma** problemini, Strategy **davranış seçme** problemini çözer. Spring'de factory'nin yerini çoğu zaman bağımlılık enjeksiyonu alır.
6. **OpenFeign ve devre kesici — `98`, `100`, `101`.** Kritik nokta şudur: Feign senkron bağımlılık yaratır. Bu yüzden zaman aşımı + devre kesici + geri düşüş (fallback) üçlüsü şarttır.
7. **SQL: bölümleme, parçalama, indeks maliyeti — `146`, `147`, `29`.** Bölümleme (partitioning) tek sunucuda bölmedir; parçalama (sharding) çok sunucuda dağıtmadır. İndeksin ise yazma maliyeti, disk yükü ve bakım yükü vardır.
8. **JPA: N+1, tembel/hevesli yükleme, JPQL/native — `80`, `81`, `82`.** İyi cevaplandı ama ikinci görüşmede derinleşir. `JOIN FETCH` ile sayfalama tuzağı buradan gelir.
9. **Java temelleri ve Stream — `02`, `12`, `13`, `15`, `16`, `17`, `32`, `34`, `35`, `53`.** Bu blok sağlamdı; kısa tekrar yeter. `parallel stream`'in ne zaman zarar verdiğini hatırlamak yeterlidir: ortak ForkJoin havuzu ve küçük veri seti.

### Mülakatta ne sorulur?

- **"Süreç kaç aşamalı, sonraki adım ne?"** Bunu görüşmecinin sana sorması beklenmez; **sen sorarsın**. Danışmanlık firması üzerinden ilerleyen bir süreçte şunu sor: "Müşteri tarafında ayrı bir teknik görüşme olacak mı?" Bu sorunun cevabı, sonraki iki haftanı nasıl planlayacağını belirler.
- **"Neden danışmanlık firması üzerinden çalışmak istiyorsun?"** Doğru cevap, modelin gerçeğini bilmekten geçer. Bordronun firmada, işinin bankada olduğunu söyle. Modelin farklı projelerde çalışma imkânı sunduğunu ekle. Kadroya geçişin bir ihtimal olduğunu da belirt. "Fark etmez, iş olsun yeter" cevabı ilgisizlik olarak okunur.
- **"Saga nedir?"** Tanımla başla ama tanımda kalma. İki cümlede tanım ver, sonra doğrudan akışa geç: adım, başarısızlık, telafi, ara durum, zaman aşımı, orkestrasyon tercihi. Takip sorusu neredeyse kesindir: "Telafi de başarısız olursa ne olur?" (`126`).
- **"Bu görüşmede en zorlandığın soru neydi?"** Bazen doğrudan sorulur. Gerçek bir eksik söyle ve üzerine ne yaptığını ekle. Bu, "hiç zorlanmadım" demekten çok daha güçlüdür.
- **Tuzak: birinci görüşmeyi geçince rahatlamak.** Aynı konuların derini ikinci görüşmede gelir. İlk görüşmede kurtaran yüzeysel cevap, ikincisinde kurtarmaz.
- **Tuzak: görüşmecinin üslubunu değerlendirme sanmak.** Sert veya küçümseyici bir üslup teknik bir sinyal değildir. Üsluba değil soruya cevap ver; savunmaya geçme.
- **Tuzak: geri bildirimi kişisel almak.** "Daha iyi bilmelisin" bir kapı kapatma değildir. **Tam olarak neye çalışman gerektiğini söyleyen** bir yol tarifidir. Çoğu aday bu netlikte bir bilgi hiç alamaz.

### Tek cümlede

Görüşmeyi çıkışta yazıya dök ve her soruyu bir konu numarasına eşle. Dağılıma bakıp neyin gerçekten sorulduğunu gör. Geri bildirimi ezber eksikliği değil, **akış anlatamama** olarak oku. Sonra ikinci teknik görüşmeye, o eksiği kendin açacak şekilde hazırlan; çünkü danışmanlık modelinde neredeyse her zaman ikinci bir görüşme vardır.

# gercek-1 | Orta
## Soru
Bir danışmanlık firmasının teknik görüşmesini başarıyla geçtin ve firma senin profilini müşteri bankaya ilettiğini söyledi. Görüşmede sorulan 45 sorunun tamamını not aldın ve iki soruda zayıf kaldığını biliyorsun. Önümüzdeki iki haftada ne yapman doğrudur?
## A
Süreç teknik olarak tamamlandığı için hazırlığı durdurup teklif aşamasına odaklanmak ve ücret araştırması yapmak
🤖 danışmanlık modelinde müşteri şirket çoğu zaman kendi teknik görüşmesini ayrıca yapar; hazırlığı durdurmak sürecin ortasında durmak demektir.
## B
Not aldığın 45 soruyu bir kenara bırakıp tamamen yeni konulara geçmek; aynı soruların ikinci kez gelmesi beklenmez
🤖 aynı sorular birebir gelmese de aynı konuların derini gelir; kayıt elindeyken onu kullanmamak en ucuz bilgiyi harcamaktır.
## C
Aynı konuların derinini soracak ikinci bir teknik görüşmeye hazırlanmak; 45 soruyu konu numaralarına eşleyip zayıf kalınanları öne almak
🤖 Doğru: bankanın ayrı teknik görüşmesi beklenmelidir ve elindeki soru kaydı doğrudan tekrar planına çevrilebilir.
## D
Yalnızca zayıf kaldığın iki soruyu çalışıp diğer 43 soruya hiç dönmemek; iyi cevaplananlar zaten güvenli sayılır
🤖 birinci görüşmede yeterli olan yüzeysel cevap, aynı konu derinleştiğinde yetmeyebilir; iyi cevaplanan konular da bir kademe derinleşmelidir.
## Doğru
C
## Açıklama
Danışmanlık firması üzerinden ilerleyen süreçte teknik değerlendirme çoğu zaman **iki aşamalıdır**: önce firmanın kendi teknik görüşmesi, sonra profil iletildikten sonra müşteri şirketin ayrıca yaptığı görüşme. Birinci aşamayı geçmek bu yüzden bitiş değil, ortadır ve hazırlığı orada durdurmak en pahalı hatadır. Elinde sorulan soruların tam listesi varsa bu liste doğrudan bir tekrar planına çevrilir: her soru bir konu numarasına eşlenir, zayıf kalınanlar öne alınır, iyi cevaplananlar bir kademe derinleştirilir. Yalnız iki zayıf soruya odaklanmak eksik bir okumadır, çünkü ikinci görüşme aynı soruyu değil onun **bir üst basamağını** sorar: "Saga nedir?" yerine "bu akışı çizer misin, telafi de başarısız olursa ne olur?" gelir. Not aldığın soruları bırakıp yeni konulara geçmek ise elindeki tek gerçek saha verisini çöpe atmaktır; hiçbir tahmin, fiilen sorulmuş soru listesi kadar değerli değildir. Teklif aşamasına odaklanmak zamansızdır: ücret konuşması teknik değerlendirme tamamlandıktan sonra anlam kazanır.

# gercek-2 | Orta
## Soru
Danışmanlık firmasının İK görüşmesindesin ve pozisyonun bir bankanın kredilendirme ekibinde "yerleşik danışman" olduğu söylendi. Bu çalışma modeliyle ilgili hangi anlayış doğrudur?
## A
Bordro danışmanlık firmasındadır ama günlük iş bankada yürür; kadroya geçiş bir ihtimaldir ve görüşmede açıkça sorulması gerekir
🤖 Doğru: modelin özü budur ve kadro geçişi taahhüt olmadığı için netleştirilmesi gereken bir konudur.
## B
Bordro bankadadır, danışmanlık firması yalnızca aracılık edip komisyon aldığı için sonrasında ilişki tamamen sona erer
🤖 bu bir işe yerleştirme (recruitment) modelidir; yerleşik danışmanlıkta bordro ve özlük işleri danışmanlık firmasında kalır.
## C
Danışmanlık firmasının bordrosunda olduğun için banka ekibinin süreçlerine dahil olmaz, işi uzaktan ve bağımsız teslim edersin
🤖 yerleşik (onsite) model tam tersini ifade eder: müşteri ekibinin içinde, aynı sprint ve kod inceleme düzeninde çalışırsın.
## D
Bir-iki yıl sonra banka kadrosuna geçiş sözleşmeyle garanti altındadır, bu yüzden ayrıca konuşulmasına gerek yoktur
🤖 geçiş yaygın bir ihtimaldir ama otomatik veya garanti değildir; garanti sayıp sormamak sonradan hayal kırıklığı üretir.
## Doğru
A
## Açıklama
Yerleşik danışmanlık modelinde iki taraf iki farklı işi yürütür: **danışmanlık firması** maaş, özlük, izin ve sözleşmeden sorumludur; **müşteri şirket** ise günlük işi, ekibi, sprint'i ve kod incelemesini yürütür. Yani ekip arkadaşların bankanın çalışanlarıdır, ama bordron danışmanlık firmasındadır. Bunu işe yerleştirme modeliyle karıştırmak yaygın bir hatadır: yerleştirmede firma seni bankaya alır ve ilişkisi biter, yerleşik danışmanlıkta firma sürekli işverenin olarak kalır. Modelin uzaktan ve bağımsız çalışma anlamına geldiğini düşünmek de yanlıştır; "yerleşik" kelimesinin anlamı zaten müşterinin ekibinin içinde bulunmaktır. Kadroya geçiş konusuna gelince: bir-iki yıl sonra müşteri şirketin kadrosuna geçmek bu modelde yaygın bir sonuçtur ama **garanti değildir** ve firmadan firmaya, hatta müşteriden müşteriye değişir. Bu yüzden görüşmede açıkça sorulur: geçiş uygulaması var mı, hangi koşullarda, daha önce olmuş mu. Cevabı belirsiz bırakmak, ileride yalnızca senin aleyhine çalışır.

# gercek-3 | Orta
## Soru
Teknik görüşme bitiyor ve görüşmeci "senin sormak istediğin bir şey var mı?" diyor. Aracılı süreçlerde sonucun haftalarca bildirilmemesi bilinen bir risktir. Bu son dakikalarda hangi davranış doğrudur?
## A
Sonucu sormamak, çünkü sonuç sormak sabırsızlık olarak okunur ve değerlendirmeyi olumsuz etkileyebilir
🤖 süreç takvimi sormak sabırsızlık değil profesyonel bir beklentidir; sormamak yalnızca belirsizliği uzatır.
## B
Sonucun ne zaman bildirileceğini sormak yerine ertesi gün firmaya e-posta atıp aynı soruyu yazılı olarak iletmek
🤖 aynı bilgiyi görüşme içinde bedavaya alabilecekken bir gün kaybetmek ve ayrı bir temas üretmek gereksiz bir dolambaçtır.
## C
Yalnızca ücret aralığını, yan hakları ve çalışma düzenini sorup teknik süreci danışmanlık firmasının inisiyatifine bırakmak
🤖 bu konular meşrudur ama teknik görüşmenin değil İK aşamasının konusudur; ayrıca sürecin devamı hakkında hiçbir bilgi vermez.
## D
Sonucun ne zaman bildirileceğini, sonraki adımın müşteri görüşmesi olup olmadığını ve o görüşmenin ne zaman planlanacağını sormak
🤖 Doğru: hem takvim netleşir hem sonraki teknik görüşmenin varlığı öğrenilir; ikisi de hazırlığını doğrudan biçimlendirir.
## Doğru
D
## Açıklama
Soru sorma faslı hâlâ değerlendirilen bir bölümdür ve bu modelde iki bilgi doğrudan senin planını değiştirir: **sonucun ne zaman geleceği** ve **sonraki adımın ne olduğu**. İkincisi kritiktir çünkü danışmanlık firmasının teknik görüşmesinden sonra müşteri şirketin ayrı bir teknik görüşme yapması yaygındır; bunu öğrenmek, önümüzdeki iki haftanın tamamen farklı geçmesi anlamına gelir. Takvim sormanın sabırsızlık olarak okunacağı endişesi yersizdir: her iki taraf da bir sürecin içindedir ve tarih netleştirmek işin normal parçasıdır. Üstelik tarih verildiğinde takip etmek de artık meşrulaşır — "geçen görüşmede bu hafta içinde dönüş yapılacağı konuşulmuştu" cümlesi rahatsız edici değil, konuşulmuş bir sürecin hatırlatılmasıdır. Aynı soruyu ertesi gün e-postayla sormak bilgiyi bir gün geç ve daha zayıf biçimde alır. Ücret ve yan hak soruları ise meşrudur ama yeri teknik görüşme değil İK aşamasıdır; ayrıca bu sorular sürecin devamı hakkında hiçbir şey söylemez. Soru sorma faslının genel kalıbı `238` numaralı konuda ayrıntılı biçimde verilmiştir.

# gercek-4 | Orta
## Soru
Görüşmede sırasıyla şu sorular geldi: "proxy bean nedir", "AOP nasıl çalışır", "`@Transactional` ne yapar", "propagation türleri neler", "aynı sınıf içinden çağrılan metotta işlem neden başlamaz", "`rollbackFor` ne işe yarar". Bu altı sorunun art arda gelmesi görüşmecinin neyi ölçtüğünü gösterir?
## A
Adayın Spring'in kaç farklı anotasyonunu ezberden sayabildiğini; sorular birbirinden bağımsız bilgi kontrolleridir
🤖 sorular bağımsız değildir; hepsi tek bir çalışma mekanizmasının farklı yüzleridir ve ezber sayımı ölçmezler.
## B
Adayın Spring'in işi hangi mekanizmayla yaptığını, yani proxy üretimi ve metot kesişimi zincirini kavrayıp kavramadığını
🤖 Doğru: altı sorunun tamamı proxy mekaniğinin türevidir ve zincir kavranmadan hiçbiri gerçekten cevaplanamaz.
## C
Adayın veritabanı izolasyon seviyelerini ve kilitleme stratejilerini ne kadar derin bildiğini; sorular veri katmanına hazırlıktır
🤖 izolasyon sorusu bu blokta geçmiştir ama sorular veri katmanına değil Spring'in kesişen mantığı uygulama biçimine yöneliktir.
## D
Adayın hangi Spring Boot sürümünü kullandığını ve sürümler arası davranış farklarını takip edip etmediğini
🤖 sorulan hiçbir soru sürüm farkına dokunmaz; hepsi sürümden bağımsız çekirdek mekanizmayı hedefler.
## Doğru
B
## Açıklama
Bu altı soru rastgele değil, **tek bir mekanizmanın etrafında** dizilmiştir. Spring, `@Transactional` gibi kesişen mantığı bean'in kendisine değil, etrafına sardığı bir **vekile (proxy)** yerleştirir; çağrı önce proxy'den geçer, işlem orada açılır ve kapanır. Bu tek gerçeği bilen aday zinciri kendiliğinden çözer: AOP aynı proxy mekanizmasının genel biçimidir; `@Transactional` bu mekanizmanın hazır bir uygulamasıdır; propagation, proxy'nin var olan bir işlemle karşılaştığında ne yapacağını tanımlar; **self-invocation** ise `this.metot()` çağrısının proxy'ye uğramaması yüzünden işlemin hiç başlamamasıdır; `rollbackFor` de varsayılan davranışın yalnızca denetlenmeyen istisnalarda geri alma yapmasını genişletir. Bu zinciri bilmeyen aday altı sorunun altısını da ayrı ayrı ezberlemek zorunda kalır ve ilk takip sorusunda düşer. Soruları bağımsız bilgi kontrolleri sanmak bu yüzden yanlıştır. İzolasyon sorusu aynı blokta geçmiş olsa da odak veri katmanı değil, Spring'in kesişen mantığı nasıl uyguladığıdır. Sürüm farkı ise hiç sorulmamıştır; bu sorular sürümden bağımsız çekirdek davranışı hedefler. Ayrıntı için `72` ve `83`.

# gercek-5 | İleri
## Soru
Görüşme sonunda görüşmeci şu geri bildirimi veriyor: "Saga pattern yapılarını, microservice vb. yapıları daha iyi bilmelisin." Görüşmede saga sorulduğunda tanımı doğru vermiştin. Bu geri bildirimi nasıl okuman ve nasıl çalışman gerekir?
## A
Tanım doğru verildiğine göre geri bildirim haksızdır; bir sonraki görüşmede aynı tanımı daha kendinden emin bir tonla tekrarlamak yeterlidir
🤖 tanımın doğruluğu zaten sorgulanmıyor; eksik bulunan şey tanımın ötesine geçen akış anlatımıdır ve ton değişikliği bunu kapatmaz.
## B
Saga'nın koreografi ve orkestrasyon türlerinin akademik tanımlarını, tarihçesini ve literatürdeki kaynaklarını ezberleyerek derinleşmek
🤖 daha fazla tanım ezberlemek aynı hatanın büyütülmüş biçimidir; sorun bilgi hacmi değil, akışı anlatabilme becerisidir.
## C
Tanımın ötesine geçip akış zincirini çalışmak: adım, başarısızlık, telafi işlemi, ara durum, timeout ve orkestrasyon tercihinin gerekçesi
🤖 Doğru: görüşmecinin eksik bulduğu şey tam olarak bu zincirdir ve deseni uygulamış olmakla okumuş olmayı ayıran da budur.
## D
Saga yerine iki fazlı commit (2PC) gibi alternatifleri öğrenip bir sonraki görüşmede konuyu o alternatiflere çekmek
🤖 alternatifi bilmek değerlidir ama sorulan konudan kaçmak eksiği kapatmaz; görüşmeci aynı soruya geri döner.
## Doğru
C
## Açıklama
"Daha iyi bilmelisin" geri bildirimi neredeyse hiçbir zaman "tanımı yanlış söyledin" anlamına gelmez; **tanım verildi ama akış verilemedi** anlamına gelir. Saga'nın "dağıtık işlemi yerel işlemlere bölen ve her adım için telafi tanımlayan desen" olduğunu söylemek doğrudur ve hiçbir şey ispatlamaz — bu cümleyi konuyu okuyan herkes kurar. Görüşmecinin duymak istediği zincirdir: adım çalışır, başarısız olur, hangi telafi işlemi tetiklenir, bu sırada kayıt hangi ara durumda bekler (`RESERVED`, `PENDING_CONFIRMATION`), cevap hiç gelmezse timeout işini kim koşturur, neden koreografi yerine orkestrasyon seçildi ve orkestratörün kendi durumu nerede saklanır. Bu zinciri anlatabilen kişi deseni uygulamıştır; anlatamayan okumuştur ve aradaki fark tek bir takip sorusuyla ("telafi de başarısız olursa ne olur?") ortaya çıkar. Daha fazla akademik tanım ezberlemek aynı hatanın büyütülmüş halidir. Konuyu 2PC gibi alternatiflere çekmek kaçınma olarak okunur ve görüşmeci seni aynı soruya geri getirir. Aynı derinleşme mantığı outbox ve idempotency için de geçerlidir: `@Transactional` içinde veritabanına yazıp Kafka'ya publish etmenin neden atomik olmadığını anlatabilmek gerekir. Ayrıntı için `126`, `130` ve `116`.

# gercek-6 | İleri
## Soru
İlk teknik görüşmede saga tarafı eksik bulundu, aradan üç hafta geçti ve şimdi müşteri bankanın teknik görüşmesindesin. Aynı görüşmeci de odada. Bu eksiği nasıl ele almak doğrudur?
## A
Konu açılmadan sen açmak: eksik bulunduğunu, üzerine çalıştığını söyleyip akışı anlatmak ve mümkünse çalışan bir örneğe bağlamak
🤖 Doğru: geri bildirimi dinleyip üzerine çalıştığını göstermek nadir bir olgunluk sinyalidir ve iddiayı kanıta çevirir.
## B
Konunun açılmasını bekleyip sorulursa cevaplamak; kendiliğinden gündeme getirmek eski bir zayıflığı hatırlatmak olur
🤖 görüşmeci o eksiği zaten hatırlıyor; sessiz kalmak hatırlatmayı engellemez, yalnız telafiyi gösterme fırsatını harcar.
## C
Geri bildirimin yanlış anlaşılmadan kaynaklandığını, konuyu aslında bildiğini ama sorunun net sorulmadığını açıklamak
🤖 savunma refleksi eksiğin kendisinden daha olumsuz okunur ve geri bildirimi tartışmaya açmak ilişkiyi zayıflatır.
## D
Konuyu hiç açmadan, mikroservis başlığı geldiğinde çok sayıda araç ve teknoloji adı sayarak genel bir yetkinlik izlenimi vermek
🤖 araç adı saymak akış anlatımının yerini tutmaz; ilk görüşmede eksik bulunan şey tam olarak bu yüzeysellikti.
## Doğru
A
## Açıklama
Olumsuz geri bildirim alan adayların ezici çoğunluğu iki şeyden birini yapar: duymazdan gelir ya da savunmaya geçer. Üçüncü yol — **eksiği kendin açıp kapattığını göstermek** — nadirdir ve tam da bu yüzden güçlüdür. "Geçen görüşmede saga tarafını eksik bulmuştunuz; üzerine çalıştım, şu akışı kurdum, isterseniz anlatayım" cümlesi aynı anda üç şey söyler: geri bildirimi dinledin, üzerine çalıştın ve eksiğini konuşabilecek kadar güvendesin. Bunu somut bir kanıta bağlamak cevabı iddiadan gösterime çevirir: çalışan bir servis, telafi adımını canlı tetikleyebileceğin bir demo, ya da tahtada çizebileceğin bir akış. Sessiz kalıp sorulmasını beklemek işe yaramaz, çünkü görüşmeci o eksiği zaten hatırlıyor; sessizlik hatırlamayı engellemez, yalnızca telafiyi görünmez kılar. Geri bildirimin yanlış anlaşılmadan kaynaklandığını savunmak en zararlı seçenektir: geri bildirimi tartışmaya açmak, eksiğin kendisinden daha olumsuz okunur ve karşı tarafı haklılığını savunmaya iter. Araç ve teknoloji adı saymak ise ilk görüşmede eksik bulunan yüzeyselliğin aynısıdır; mikroservis başlığında beklenen şey araç envanteri değil, başarısızlık senaryosunu ve telafi zincirini anlatabilmektir.
