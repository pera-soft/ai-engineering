---
baslik: "Hafta 6 Görsel Özet: Mimari Stiller ve C4 Diyagramlama"
yazar: "FoSA 2e (Richards & Ford) — kavram özeti"
tarih: "2026-07-10"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 6
---

### Bu metin ne anlatıyor?
Bir yazılımı parçalara nasıl böleceğine ve bu parçaların birbiriyle nasıl konuşacağına
verdiğin karara **mimari stil** denir. Bu metin en çok kullanılan altı stili tanıtır ve
her birinin neyi kolaylaştırdığını, bunun karşılığında hangi bedeli ödettiğini gösterir.
Sonra bu kararı başkasına anlatmanın standart yolunu — **C4 diyagramları** — anlatır ve
sonunda kendini sınayacağın sorularla biter.

## Neden bu özet
Hafta 6 mimari stilleri ve C4 ile diyagramlamayı işliyor. Üç görsel, kitabın anlattığı
ayrımların kafada netleşmesi için bir çırpıda bakılacak referanstır; aradaki metin ise
stilleri tek tek tanımlar ve hiçbir terimi "zaten bilirsin" varsaymaz.

## Mimari stil nedir — ve neden "en iyisi" yoktur
**Mimari stil (architectural style)**, bir sistemin parçalara bölünme biçimi ve bu
parçaların birbirine bağlanma kuralıdır. İki soruya verdiğin cevaptır: *sınırları nereden
geçireyim?* ve *sınırın iki yanı nasıl haberleşsin?* Önce üç terim, çünkü aşağıda sürekli
geçecekler:

- **Dağıtım (deployment):** yazdığın kodun, çalışacağı sunucuya kurulup ayağa
  kaldırılması. "Tek dağıtım birimi" demek, uygulamanın tek bir paket hâlinde tek seferde
  kurulması demektir.
- **Bağlaşım (coupling):** iki parçanın birbirine ne kadar yapışık olduğu. Yüksek
  bağlaşımda birini değiştirince öteki de kırılır.
- **Uyum (cohesion):** bir parçanın içindeki şeylerin ne kadar aynı işe hizmet ettiği.
  Yüksek uyum iyidir: parça tek bir konu hakkındadır.

Hedef her zaman aynıdır: **yüksek uyum, düşük bağlaşım.** Stiller bu hedefe farklı
yollardan gider ve her yol bir bedelle gelir. "Hangi stil en iyisi?" sorusunun cevabı
yoktur; doğru soru şudur: **hangi kuvveti kazanmak için hangi faturayı ödeyebilirim?**

## Altı stil: neyi optimize eder, faturasını nereden keser

**1. Monolit (monolith) — tek parça.** Bütün kod tek projede derlenir, tek birim olarak
dağıtılır. *Optimize ettiği:* basitlik. Tek yerde hata ayıklarsın, çağrılar ağa çıkmadan
bellekte olur, veritabanı işlemleri (transaction) doğal olarak tek parçadır. *Faturası:*
büyüdükçe içerideki sınırlar erir; tek satırlık düzeltme için bütün uygulamayı yeniden
dağıtırsın; bir modülün bellek sızıntısı hepsini birden düşürür. Küçük ekip ve belirsiz
alan için doğru başlangıç budur.

**2. Katmanlı (layered) — teknik role göre yatay dilimler.** Sunum, iş mantığı, kalıcılık.
*Optimize ettiği:* öğrenme kolaylığı ve teknik uzmanlığın ayrılması; en düşük başlangıç
maliyeti. *Faturası:* bir iş özelliği (feature) neredeyse hep bütün katmanları keser, yani
tek istek için üç dört dosyayı birden değiştirirsin. Alan (domain) sınırları görünmez olur.
Ayrıca **çukur karşı-deseni (sinkhole anti-pattern)** riski vardır: isteklerin bir kısmı
hiçbir iş yapmadan katmanlardan aşağı düşer, sadece veri taşır.

**3. Modüler monolit (modular monolith) — tek dağıtım, içeride sıkı sınırlar.** Hâlâ tek
birim olarak kurulur; ama içerisi alan sınırlarına göre modüllere ayrılmıştır ve modüller
birbirinin içine elini sokamaz. *Optimize ettiği:* alan sınırlarının netliği ile dağıtım
basitliğini aynı anda elde etmek; ileride mikroservise geçmen gerekirse kesme çizgileri
hazırdır. *Faturası:* sınırlar kendiliğinden durmaz. Onları otomatik zorlayan bir kontrol
(bu projedeki ArchUnit testleri gibi) yoksa altı ayda erir. Ayrıca modüller aynı süreçte
çalıştığı için bağımsız ölçeklenemez.

**4. Mikroservis (microservices) — her yetenek ayrı dağıtılabilir servis.** Her servisin
kendi veritabanı vardır ve servisler ağ üzerinden konuşur. *Optimize ettiği:* bağımsız
dağıtım, bağımsız ölçekleme, ekip özerkliği ve hata yalıtımı — bir servis düşer, ötekiler
ayakta kalır. *Faturası:* ağ artık tasarımının parçasıdır. Gecikme (latency) eklenir,
kısmi arıza normalleşir, tek bir veritabanı işlemiyle iki servisi birden tutarlı
tutamazsın; **nihai tutarlılığı (eventual consistency)** kabul etmek zorundasın. Üstüne
işletme yükü katlanır: izleme, dağıtık iz sürme (distributed tracing), onlarca dağıtım
hattı.

**5. Olay güdümlü (event-driven) — parçalar birbirini doğrudan çağırmaz.** Bir şey olduğunda
parça bunu bir **olay (event)** olarak bir kanala ya da kuyruğa (message queue) yazar;
ilgilenen ne varsa dinler. *Optimize ettiği:* gevşek bağlaşım ve ani yük dalgalarını yutma
— kuyruk tampon görevi görür. Yeni bir tüketici eklemek, üreticiyi hiç değiştirmeden olur.
*Faturası:* akışı gözle takip etmek zorlaşır ("bu olay kime gitti, kim ne yaptı?"); olayın
iki kez gelmesi (duplicate) ve sıra bozulması normal kabul edilip elle ele alınmalıdır.

**6. Uzay-tabanlı (space-based) — veritabanını sıcak yoldan çıkarır.** Veri, sunucuların
belleğinde çoğaltılmış bir **veri ızgarasında (data grid)** tutulur; işlem birimleri yük
arttıkça çoğalır; veritabanına yazma arka planda, asenkron yapılır. *Optimize ettiği:*
öngörülemeyen ani yükte çok yüksek eşzamanlı kullanıcı sayısı — bilet satışı, kampanya
açılışı. *Faturası:* listenin en karmaşık stili. Bellekteki kopyalar arasında tutarlılık
zordur, çökme anında henüz diske yazılmamış veri kaybolabilir ve bellek maliyeti yüksektir.

> **Şerh:** "Mikroservise geçersen sistemin ölçeklenir" cümlesi, çoğu bulut ve araç
> satıcısının pazarlama cümlesidir. Mikroservis ölçeği *garanti etmez*; yalnızca parçaları
> **bağımsız** ölçekleyebilme *imkânı* verir ve karşılığında ağ, işletme ve tutarlılık
> maliyeti yükler. Kötü sınırlanmış bir monolit, mikroservise bölündüğünde "dağıtık
> monolit"e döner: bütün eski bağımlılıklar durur, üstüne bir de ağ gecikmesi eklenir.

## Diyagram 1 — Katmanlı mimari (Layered)
En yaygın başlangıç stili: sorumluluklar yatay katmanlara ayrılır ve her katman yalnızca
bir altındakiyle konuşur. "Kapalı katman (closed layer)": her istek sırayla iner, bir
katmanı atlayamaz — bu yalıtım, katmanları birbirinden bağımsız değiştirilebilir kılar.

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Katmanlı mimari: Sunum, İş Mantığı, Kalıcılık ve Veritabanı katmanları yukarıdan aşağıya">
  <defs>
    <marker id="h6ok1" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Katmanlı Mimari (Layered) — kapalı katmanlar</text>
  <rect x="160" y="46" width="320" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Sunum (UI)</text>
  <text x="320" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">arayüz · ekranlar</text>
  <rect x="160" y="110" width="320" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="132" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">İş Mantığı (Business)</text>
  <text x="320" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kurallar · iş akışı</text>
  <rect x="160" y="174" width="320" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="196" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Kalıcılık (Persistence)</text>
  <text x="320" y="212" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">veri erişimi · sorgular</text>
  <rect x="160" y="238" width="320" height="46" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="320" y="260" text-anchor="middle" font-size="13" font-weight="700" fill="#4F46E5" font-family="system-ui">Veritabanı (Database)</text>
  <text x="320" y="276" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kalıcı depolama</text>
  <line x1="150" y1="69" x2="150" y2="133" stroke="#186399" stroke-width="1.8" marker-end="url(#h6ok1)"/>
  <line x1="150" y1="133" x2="150" y2="197" stroke="#186399" stroke-width="1.8" marker-end="url(#h6ok1)"/>
  <line x1="150" y1="197" x2="150" y2="261" stroke="#186399" stroke-width="1.8" marker-end="url(#h6ok1)"/>
  <text x="502" y="150" font-size="10.5" fill="#5D7789" font-family="system-ui">Kapalı katman:</text>
  <text x="502" y="166" font-size="10.5" fill="#5D7789" font-family="system-ui">her istek bir</text>
  <text x="502" y="182" font-size="10.5" fill="#5D7789" font-family="system-ui">alttakine iner,</text>
  <text x="502" y="198" font-size="10.5" fill="#5D7789" font-family="system-ui">atlayamaz.</text>
</svg>
```

## Diyagram 2 — Üç stil karşılaştırması
Aynı problemi üç farklı topolojiyle çözebilirsin. Fark; parçaların ne kadar sıkı bağlı
(coupling) olduğunda, nasıl dağıtıldığında (deployment) ve hangi bağlamda tercih
edildiğinde ortaya çıkar.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Üç mimari stilin karşılaştırması: Katmanlı, Altıgen ve Mikroservis">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç Stil Karşılaştırması</text>
  <rect x="26" y="48" width="182" height="86" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="117" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Katmanlı</text>
  <text x="117" y="98" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">(Layered)</text>
  <text x="117" y="118" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tek parça (monolit)</text>
  <rect x="229" y="48" width="182" height="86" rx="12" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="320" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#4F46E5" font-family="system-ui">Altıgen</text>
  <text x="320" y="98" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">(Hexagonal)</text>
  <text x="320" y="118" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">port · adaptör</text>
  <rect x="432" y="48" width="182" height="86" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="523" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Mikroservis</text>
  <text x="523" y="98" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">(Microservices)</text>
  <text x="523" y="118" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">dağıtık servisler</text>
  <text x="117" y="164" text-anchor="middle" font-size="10.5" font-weight="600" fill="#1C3247" font-family="system-ui">Bağlantı: sıkı</text>
  <text x="117" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Dağıtım: tek birim</text>
  <text x="117" y="204" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Ne zaman: küçük /</text>
  <text x="117" y="220" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">basit uygulama</text>
  <text x="320" y="164" text-anchor="middle" font-size="10.5" font-weight="600" fill="#1C3247" font-family="system-ui">Bağlantı: orta</text>
  <text x="320" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Dağıtım: tek birim</text>
  <text x="320" y="204" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Ne zaman: dış</text>
  <text x="320" y="220" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bağımlılık yalıtımı</text>
  <text x="523" y="164" text-anchor="middle" font-size="10.5" font-weight="600" fill="#1C3247" font-family="system-ui">Bağlantı: gevşek</text>
  <text x="523" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Dağıtım: bağımsız</text>
  <text x="523" y="204" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Ne zaman: bağımsız</text>
  <text x="523" y="220" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ölçek / ekipler</text>
</svg>
```

## Conway yasası — mimariyi ekip şeması da çizer
1968'de Melvin Conway şunu gözlemledi: **bir sistemi tasarlayan kuruluşlar, kaçınılmaz
olarak kendi iletişim yapılarının kopyası olan tasarımlar üretir.** Sebebi basit: aynı
odadakiler konuşarak anlaşır, farklı odadakiler arayüz tanımlayarak. Arayüzler de ekip
sınırlarının olduğu yere düşer.

Sonucu iki türlüdür. Birincisi bir **uyarıdır**: ekip yapına uymayan bir mimariyi kâğıt
üzerinde çizersen, kod zamanla ekip yapına geri kayar. İkincisi bir **manevradır** — buna
**ters Conway manevrası (Inverse Conway Maneuver)** denir: istediğin mimariyi elde etmek
için önce ekip yapını ona benzetirsin. "Her servisi tek bir ekip sahiplensin" tavsiyesi
buradan gelir.

> **Şerh:** Conway yasası bir fizik yasası değil, tekrarlanan bir **gözlemdir**. "Ekipleri
> böl, mimari kendiliğinden düzelir" biçiminde bir reçeteye çevrilmesi fazla iddialıdır.

## Diyagram 3 — C4 modeli, dört zoom seviyesi
C4, sistemi tek bir dev diyagrama sığdırmak yerine dört farklı yakınlaşma (zoom)
seviyesinde anlatır: uzaktan bakıştan (Bağlam) koda doğru her adımda bir öncekinin bir
kutusuna girersin. Böylece dinleyici hangi ayrıntı seviyesinde konuştuğunu hep bilir.
Adı da buradan gelir: dört seviyenin İngilizce baş harfleri **C**ontext, **C**ontainer,
**C**omponent, **C**ode.

```svg
<svg viewBox="0 0 640 280" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="C4 modelinin dört seviyesi: Bağlam, Konteyner, Bileşen ve Kod, iç içe kutular">
  <defs>
    <marker id="h6ok2" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#C77A17"/></marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">C4 Modeli — dört zoom seviyesi</text>
  <rect x="40" y="46" width="560" height="210" rx="14" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="60" y="70" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Bağlam (Context)</text>
  <text x="60" y="86" font-size="10" fill="#5D7789" font-family="system-ui">sistem + kullanıcılar + dış sistemler</text>
  <rect x="72" y="96" width="496" height="152" rx="12" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="92" y="120" font-size="12.5" font-weight="700" fill="#4F46E5" font-family="system-ui">2 · Konteyner (Container)</text>
  <text x="92" y="136" font-size="10" fill="#5D7789" font-family="system-ui">uygulamalar · servisler · veritabanları</text>
  <rect x="104" y="146" width="432" height="94" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="124" y="170" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">3 · Bileşen (Component)</text>
  <text x="124" y="186" font-size="10" fill="#5D7789" font-family="system-ui">bir konteyner içindeki mantıksal bloklar</text>
  <rect x="136" y="196" width="368" height="36" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="156" y="218" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">4 · Kod (Code) — sınıf / işlev düzeyi</text>
  <line x1="560" y1="264" x2="180" y2="264" stroke="#C77A17" stroke-width="1.6" marker-end="url(#h6ok2)"/>
  <text x="372" y="278" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">giderek yakınlaşır: Bağlam → Konteyner → Bileşen → Kod</text>
</svg>
```

## Dört seviye tek tek — kime, neyi gösterir
**Seviye 1 · Bağlam (Context).** Ortada tek bir kutu vardır: senin sistemin. Etrafında onu
kullanan **insanlar** ve konuştuğu **dış sistemler** durur; içini hiç açmazsın. Teknik
olmayan bir yöneticiye gösterebileceğin tek diyagram budur.

**Seviye 2 · Konteyner (Container).** Birinci seviyedeki kutunun içini açarsın: **ayrı
çalışan ve ayrı dağıtılan** şeyler görünür — web uygulaması, mobil uygulama, API servisi,
veritabanı, mesaj kuyruğu. **Dikkat:** buradaki "konteyner" Docker konteyneri anlamına
*gelmez*; C4 Docker'dan eskidir ve kelime "kendi başına çalışan bir şey" demektir.
İzleyicisi geliştiriciler ve işletme ekibidir.

**Seviye 3 · Bileşen (Component).** Tek bir konteynerin içindeki mantıksal bloklar ve
aralarındaki bağımlılıklar. Bu projedeki `types → config → repo → service → ai → web`
zinciri bir bileşen görüntüsüdür. İzleyicisi o konteyner üzerinde çalışan ekiptir.

**Seviye 4 · Kod (Code).** Sınıf ve arayüz düzeyi. Pratikte **çoğu zaman hiç çizilmez**: en
hızlı eskiyen seviyedir ve gerektiğinde geliştirme ortamı zaten üretir.

C4'ün bir de destek diyagramları vardır; en sık kullanılanı **dağıtım (deployment)
diyagramıdır**: hangi konteynerin hangi sunucuda koştuğunu gösterir.

## Kutu-ok tuzağı — diyagramı belge yapan şey lejanttır
Çoğu mimari diyagram aslında bir **kutu-ok resmidir (boxes and lines)**: kutular çizilmiş,
aralarına oklar atılmıştır, ama hiçbiri ne olduğunu söylemez. Bir ok en az üç farklı şey
olabilir — senkron çağrı, veri akışı ya da yalnızca derleme zamanı bağımlılığı. Kutu da bir
sunucu, bir sınıf ya da bir ekip olabilir. Herkes kendi anladığını okur ve toplantı
"anlaştık" sanılarak biter.

Diyagramı belgeye çeviren üç kural:

1. **Her kutunun üç şeyi olsun:** adı, tipi (web uygulaması / veritabanı / servis) ve tek
   cümlelik sorumluluğu.
2. **Her okun üstünde yazsın:** ne için ve hangi yolla (örneğin "sipariş oluşturur —
   HTTPS/JSON" ya da "olay yayınlar — mesaj kuyruğu").
3. **Her diyagramda bir lejant (legend) olsun:** renk ve çizgi tipleri ne demek.

Kendi diyagramını sınamanın ölçütü şudur: **aynı diyagrama bakan iki kişi, sen odada
olmadan aynı cümleyi kuruyor mu?** Kurmuyorsa elindeki bir belge değil, bir resimdir.

## Öz-sınav (kendini test et)
1. Aynı ekip, aynı problemi hem modüler monolit hem mikroservis olarak kurabilir. İkisinin
   *neyi* aynı, *neyi* farklı optimize ettiğini bir cümleyle söyle; farkın nerede
   başladığını dağıtım kelimesini kullanarak açıkla.
2. Katmanlı mimaride "bir özellik eklemek üç dosyayı değiştirmeyi gerektiriyor" şikâyeti
   neden bir arıza değil, stilin doğal sonucudur? Bu maliyeti hangi bölme ölçütü azaltır?
3. Olay güdümlü stilde "gevşek bağlaşım" kazanırken hangi iki yeni sorunu peşinen kabul
   etmiş olursun, ve bunlar neden ağ değil **tasarım** sorunudur?
4. Uzay-tabanlı mimari hangi tek kuvvet için tasarlanmıştır, ve bu kuvveti elde etmek için
   hangi klasik güvenceden vazgeçer?
5. C4'te "konteyner" kelimesi neyi anlatır ve hangi yanlış çağrışıma yol açar? Bir
   veritabanı hangi seviyede görünür, bir sınıf hangi seviyede? Üzerinde hiç yazı olmayan
   bir kutu-ok resmini belgeye çevirmek için hangi üç eklemeyi yaparsın?
6. Conway yasasını kendi cümlelerinle kur. "Ters Conway manevrası" bu yasayı bir uyarıdan
   nasıl bir araca çevirir — ve neden bu bir garanti değildir?

### Cevap anahtarı (önce kendin cevapla, sonra bak)
1. İkisi de alan sınırlarını netleştirir; fark **dağıtımdadır**. Modüler monolit tek birim
   olarak kurulur: bağımsız ölçek yok, ama ağ maliyeti de yok. Mikroservis her modülü ayrı
   dağıtır: bağımsız ölçek ve ekip özerkliği var, karşılığında ağ gecikmesi, kısmi arıza ve
   nihai tutarlılık var.
2. Katmanlı stil **teknik role** göre böler, iş özelliğine göre değil; bir özellik doğal
   olarak üç katmanı birden keser. Alan sınırına göre bölmek değişikliği tek modüle hapseder.
3. Olayın birden çok kez teslim edilmesi ve sıranın bozulması. Tasarım sorunudur, çünkü
   çözüm uygulamada kurulur: işlemi tekrar edilebilir (idempotent) yazmak ve sıraya
   bağımlılığı kaldırmak.
4. Öngörülemeyen ani yükte yüksek eşzamanlılık için tasarlanmıştır. Karşılığında veritabanına
   anında ve kesin yazma güvencesinden vazgeçer; çökme anında henüz aktarılmamış veri
   kaybolabilir.
5. "Ayrı çalışan, ayrı dağıtılan bir şey" demektir; Docker konteyneri sanılır, oysa terim
   daha eskidir. Veritabanı 2. seviyede, sınıf 4. seviyededir. Eklemeler: kutuya ad + tip +
   tek cümlelik sorumluluk, oka amaç + iletişim yolu, diyagrama lejant.
6. Bir kuruluşun ürettiği tasarım, o kuruluşun iletişim yapısını taklit eder. Ters manevra
   hedeflenen mimariye uygun ekip sınırlarını önce kurar. Garanti değildir: yasa bir
   gözlemdir ve mimariyi miras kod, teslim baskısı, araçlar da biçimlendirir.
