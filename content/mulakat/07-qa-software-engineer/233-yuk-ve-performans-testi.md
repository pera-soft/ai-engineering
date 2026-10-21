---
slug: yuk-ve-performans-testi
baslik: "Yük ve Performans Testi: Test Türleri, JMeter/k6/Gatling, Yüzdelikler ve Darboğaz Avı"
kademe: İleri
sira: 233
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Yeni açılan bir köprüden bir araba geçirip "sağlam" demek kimseyi ikna etmez. Köprü mühendisleri köprüyü dört ayrı biçimde sınar: **beklenen** trafikle (yük), **kırılana kadar** artan ağırlıkla (stres), **günlerce** süren titreşimle (dayanıklılık) ve ani bir kamyon konvoyuyla (ani yük). Yazılımda da aynı ayrım vardır. Fonksiyonel test "doğru cevabı veriyor mu?" diye sorar. Performans testi ise "**kaç kişi aynı anda sorduğunda** hâlâ doğru cevabı zamanında veriyor mu?" diye sorar.

Bir e-ticaret sitesi için bu soru akademik değildir. Yılın cirosunun kayda değer bir kısmı Black Friday'in ilk saatinde geçer. O saatte ödeme adımı 8 saniye sürüyorsa kaybedilen şey bir hata kaydı değil, doğrudan gelirdir. Performans testinin amacı "sistem hızlı mı?" sorusuna evet demek değildir. Amaç, **hangi yükte hangi metriğin nerede bozulduğunu** sürümden önce isimlendirmektir.

### Temel kavramlar

- **Yük testi (load test)**: sistemi **beklenen/hedeflenen** yükte koşturup hizmet seviyesi hedeflerini (SLO) tutup tutmadığını ölçer. Cevapladığı soru: "Normal bir yoğun günde iyi miyiz?"
- **Stres testi (stress test)**: yükü kırılma noktasına kadar artırır. Cevapladığı soru: "Nerede **ve nasıl** kırılıyoruz?" Zarifçe mi kırılıyoruz — istek reddi, kuyruk sınırı ile — yoksa çığ gibi mi: bellek taşması, yeniden başlatma döngüsü? Ayrıca yük kalktığında sistem kendiliğinden toparlanıyor mu?
- **Dayanıklılık testi (soak / endurance)**: orta bir yükte **saatlerce** koşar. Cevapladığı soru: "Zamanla ne birikiyor?" Bellek sızıntısı, kapatılmayan bağlantılar, dolan disk, şişen kayıt dosyaları ve önbellek büyümesi burada ortaya çıkar. Bunlar 10 dakikalık testlerde görünmezler.
- **Ani yük testi (spike test)**: yük saniyeler içinde onlarca kat fırlar. Cevapladığı soru: "Kampanya saniyesine ya da televizyon reklamına yetişiyor muyuz?" Otomatik ölçekleme dakikalar sürer. Bu arada saniyeler içinde gelen yük kuyruğu taşırabilir.
- **Ölçeklenebilirlik testi (scalability test)**: kaynak eklendiğinde (sunucu/kopya sayısı) verim orantılı artıyor mu, yoksa paylaşılan bir darboğaza mı takılıyor?
- **Hacim testi (volume test)**: yükü değil **veri boyutunu** büyütür; örneğin 10 milyon ürün, 5 yıllık sipariş geçmişi. Küçük veride hızlı çalışan bir sorgu, büyük veride indeks seçimi değişince çöker.
- **Sanal kullanıcı (VU, virtual user)**: yük üreticinin çalıştırdığı ve bir kullanıcı yolculuğunu tekrar eden iş parçacığıdır. VU bir **girdidir**, sonuç değildir.
- **İş hacmi (throughput, verim)**: birim zamanda tamamlanan iştir; istek/saniye (RPS) veya işlem/saniye (TPS). Sistemin gerçekte ne kadar iş yaptığını söyleyen metrik budur.
- **Eşzamanlılık (concurrency)**: aynı anda sistem içinde işlenmekte ya da beklemekte olan istek sayısıdır.
- **Yanıt süresi (response time) ve gecikme (latency)** ile **yüzdelikler (percentile)**: p50 ortancadır; p95 ve p99 kuyruğu gösterir. Tabloyu iki şey daha tamamlar: **hata oranı (error rate)** ve **kaynak kullanımı** (CPU, bellek, iş parçacığı havuzu, bağlantı havuzu doluluğu).
- **Düşünme süresi (think time)**: gerçek kullanıcının iki isteği arasında geçen süredir; sayfayı okuma, ürün seçme gibi. Senaryoya konmazsa üretilen yük gerçek trafiğe benzemez.
- **Kademeli artış (ramp-up), sabit yük (steady state) ve soğuma (ramp-down)**: bir yük profilinin üç evresidir. Ölçüm sabit yük evresinde alınır. Kademeli artış sırasındaki sayılar kirlidir; çünkü önbellek ve bağlantı havuzu henüz ısınmaktadır.
- **Eşik (threshold)**: koşumun geçti/kaldı kararını veren kural — k6'da `thresholds`, ör. `p(95)<800`.
- **Little yasası (Little's law)**: `L = λ × W` — sistemdeki ortalama eşzamanlı istek sayısı, verim ile ortalama yanıt süresinin çarpımıdır.
- **APM** (uygulama performans izleme, application performance monitoring): sunucu tarafında hangi çağrının ne kadar sürdüğünü gösteren izleme katmanı.

```svg
<svg viewBox="0 0 660 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Performans testi turleri: yuk, stres, dayaniklilik, ani yuk profilleri">
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Dört profil, dört ayrı soru (dikey eksen: yük, yatay eksen: zaman)</text>
  <text x="90" y="40" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Yük (load)</text>
  <line x1="32" y1="150" x2="148" y2="150" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="32" y1="55" x2="32" y2="150" stroke="#5D7789" stroke-width="1.2"/>
  <polyline points="32,150 62,85 118,85 148,142" fill="none" stroke="#25804C" stroke-width="2"/>
  <text x="250" y="40" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Stres (stress)</text>
  <line x1="192" y1="150" x2="308" y2="150" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="192" y1="55" x2="192" y2="150" stroke="#5D7789" stroke-width="1.2"/>
  <polyline points="192,148 216,128 240,106 264,86 288,66 304,58" fill="none" stroke="#B07D20" stroke-width="2"/>
  <text x="308" y="54" text-anchor="middle" font-size="13" font-weight="700" fill="#B23B3B" font-family="system-ui">x</text>
  <text x="410" y="40" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Dayanıklılık (soak)</text>
  <line x1="352" y1="150" x2="468" y2="150" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="352" y1="55" x2="352" y2="150" stroke="#5D7789" stroke-width="1.2"/>
  <polyline points="352,150 372,100 448,100 466,142" fill="none" stroke="#186399" stroke-width="2"/>
  <text x="410" y="92" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">saatlerce</text>
  <text x="570" y="40" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Ani yük (spike)</text>
  <line x1="512" y1="150" x2="628" y2="150" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="512" y1="55" x2="512" y2="150" stroke="#5D7789" stroke-width="1.2"/>
  <polyline points="512,138 558,138 564,62 576,62 582,138 628,138" fill="none" stroke="#B23B3B" stroke-width="2"/>
  <text x="90" y="170" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">beklenen yükte</text>
  <text x="90" y="184" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">SLO tutuyor mu?</text>
  <text x="250" y="170" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">nerede ve NASIL</text>
  <text x="250" y="184" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kırılıyor, toparlanıyor mu?</text>
  <text x="410" y="170" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">zamanla ne birikiyor?</text>
  <text x="410" y="184" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">sızıntı, kaynak tükenmesi</text>
  <text x="570" y="170" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kampanya saniyesi:</text>
  <text x="570" y="184" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ölçekleme yetişiyor mu?</text>
  <text x="330" y="208" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Ayrıca: ölçeklenebilirlik (kaynak eklenince verim orantılı artıyor mu?) ve hacim (veri büyüyünce sorgu planı bozuluyor mu?)</text>
</svg>
```

### Nasıl çalışır? / Pratikte nasıl yapılır?

1. **Hedefi sayıya çevir.** "Site hızlı olsun" test edilemez. Test edilebilir hedef şudur: "Kampanya saatinde 1200 istek/saniye altında, ürün detay p95 < 400 ms, sepete ekleme p95 < 600 ms, ödeme p95 < 800 ms ve p99 < 2 sn, hata oranı < %1." Eşikler yoksa testin sonucu bir tartışma konusudur, bir karar değil.
2. **Kullanıcı yolculuklarını çıkar ve ağırlıklandır.** Gerçek trafik bir hunidir: çok kişi gezinir, daha azı sepete ekler, çok daha azı ödeme yapar. 100 gezinme : 20 sepete ekleme : 5 ödeme gibi bir dağılım gerçek trafiğe benzer. Herkesi ödeme yapan olarak modellemek hem gerçek dışıdır hem de gereksiz pahalıdır. Ama ödeme yolunun **hizmet seviyesi hedefi (SLO)** daha sıkıdır; oradan az kişi geçer ama geçenler para bırakır.
3. **Yük profilini tasarla.** Kademeli artış (ramp-up) → sabit yük (steady state, ölçüm burada alınır) → soğuma. Ani yük senaryosunda ramp-up bilinçli olarak kaldırılır, çünkü test edilen şey tam da hazırlıksız yakalanmaktır.
4. **Düşünme süresini koy.** `sleep(2..6)` gibi rastgele bir bekleme iki iş yapar. Üretilen yükü gerçekçi kılar. Ayrıca bağlantı yeniden kullanımını ve önbellek davranışını gerçek trafiğe yaklaştırır. Bedeli şudur: düşünme süresi iş hacmini düşürür. Bu yüzden hedef istek/saniyeyi tutturmak için gereken VU sayısı artar. Bu hesabı Little yasası verir.
5. **Ortamı ve veriyi gerçekçileştir.** Aşağıda ayrı ele alınıyor; en çok burada yalan söylenir.
6. **Koş, ama sunucu tarafını da izle.** İstemci metrikleri (yanıt süresi, hata oranı) ile sunucu metrikleri (CPU, GC, iş parçacığı havuzu, bağlantı havuzu, yavaş sorgu) **aynı zaman ekseninde** okunmalıdır.
7. **Eşiklerle karar ver, sonucu sakla.** Tek koşumun mutlak sayısı az şey söyler; asıl bilgi sürümler arası eğilimdedir.

**Ortalamanın yalanı ve p99.** Ortalama gecikme, uzun kuyruğu (tail) düzleyerek gizler; bu tek başına bilinen bir konudur (188). Burada iki adım daha derine inelim.

Birincisi: gerçek gecikme dağılımı çoğu zaman **çok modludur (multimodal)**. Önbellekten dönen istekler bir tepe oluşturur (5 ms). Veritabanına inenler ikinci bir tepe yapar (120 ms). Kilit bekleyenler üçüncü bir tepe oluşturur (3 sn). Böyle bir dağılımda ortalama, **hiçbir gerçek kullanıcının yaşadığı deneyime karşılık gelmez**. Ortalama, tam da hiç isteğin olmadığı bir vadinin ortasına düşer.

İkincisi: p99'un neden önemli olduğu "%1 küçük bir sayı" sezgisine ters gelir. Bir sayfa 30 alt istekle oluşuyor diyelim. Tek bir kullanıcının o sayfada en az bir p99 isteğiyle karşılaşma olasılığı `1 - 0.99^30`, yani yaklaşık **%26**'dır. Yani "isteklerin %1'i yavaş" cümlesi pratikte şu demektir: "Kullanıcıların dörtte biri yavaş sayfa görüyor." Kuyruk, kullanıcı deneyiminin istisnası değil, kuralıdır.

**Little yasası ile kapasite planı.**

```svg
<svg viewBox="0 0 660 208" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Little yasasi: esamanlilik verim carpi yanit suresi">
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Little yasası: sanal kullanıcı sayısı bir girdi, verim bir sonuçtur</text>
  <rect x="30" y="34" width="600" height="48" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="330" y="56" text-anchor="middle" font-size="15" font-weight="700" fill="#186399" font-family="system-ui">L  =  λ  ×  W</text>
  <text x="330" y="74" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sistemdeki eşzamanlı istek  =  verim (istek/sn)  ×  ortalama yanıt süresi (sn)</text>
  <rect x="30" y="94" width="290" height="76" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="175" y="114" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kapasite planına çevirmek</text>
  <text x="175" y="132" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Hedef 240 istek/sn, yanıt 0,5 sn</text>
  <text x="175" y="148" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">240 × 0,5 = 120 eşzamanlı istek</text>
  <text x="175" y="164" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iş parçacığı ve bağlantı havuzu bütçesi</text>
  <rect x="340" y="94" width="290" height="76" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="485" y="114" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Mülakat tuzağı</text>
  <text x="485" y="132" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">"500 VU ile test ettim" ≠ 500 istek/sn</text>
  <text x="485" y="148" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">sistem yavaşlarsa aynı VU sayısı</text>
  <text x="485" y="164" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">DAHA AZ verim üretir</text>
  <text x="330" y="192" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Think time eklenince:  verim ≈ VU ÷ (yanıt süresi + düşünme süresi)  —  100 VU, 0,2 sn yanıt, 1 sn think time ≈ 83 istek/sn</text>
</svg>
```

**Araçlar: JMeter, k6, Gatling.**

- **JMeter** (Apache, Java): en yaygın ve en eski araçtır. Test planı arayüzle (GUI) kurulur. **Thread group** modelinde her sanal kullanıcı bir işletim sistemi iş parçacığıdır; bu yüzden çok yüksek VU sayılarında bellek maliyeti hissedilir. Kritik nokta şudur: **arayüzde yük koşulmaz.** Arayüzün kendisi ciddi kaynak tüketir ve ölçümü bozar; JMeter'ın kendi uyarısı da bu yöndedir. Arayüz yalnız tasarım ve hata ayıklama içindir; yük ekransız (headless) koşar. Eklenti ekosistemi çok geniştir. Buna karşılık test planı XML olduğu için sürüm kontrolünde fark (diff) okumak zordur.
- **k6** (Grafana): test **kod olarak** JavaScript ile yazılır; çekirdeği Go'dur. Sanal kullanıcılar işletim sistemi iş parçacığı değildir. Bu yüzden tek makineden yüksek VU sayısına çıkmak daha ucuzdur. En ayırt edici özelliği `thresholds`'tur: geçti/kaldı kararı test dosyasının içinde tanımlıdır, dolayısıyla CI kapısı kurmak doğaldır. `executor` seçimiyle iki model arasında bilinçli seçim yapılır: **kapalı model** (`ramping-vus`) ve **açık model** (`constant-arrival-rate`, `ramping-arrival-rate`). Bunun neden kritik olduğu aşağıda anlatılıyor. k6 protokol seviyesinde çalışır; tarayıcının sayfayı çizme süresini ölçmez.
- **Gatling** (Scala/Java/Kotlin DSL): asenkron ve olay güdümlü bir çekirdek üzerine kuruludur. Bu yüzden VU başına iş parçacığı ayırmaz. JVM ekosisteminde kalarak az kaynakla yüksek yük üretmek isteyen ekipler onu tercih eder. Ürettiği HTML raporu ayrıntılıdır. Bedeli, kendi diline (DSL) alışma süresidir.
- **Hangisi ne zaman?** Ekip JVM'deyse, geniş protokol ile eklenti desteği ve arayüzden hızlı prototip istiyorsa JMeter uygundur. "Kod olarak test", CI kapısı ve açık model öncelikliyse k6 uygundur. JVM'de kalıp yüksek yük ve zengin rapor isteniyorsa Gatling uygundur. **Şerh:** "X aracı Y'den 10 kat fazla yük üretir" tipindeki satıcı ve blog iddialarını olduğu gibi almayın. Ölçülen farkın büyük kısmı çoğu zaman aracın markasından gelmez. Fark; yük üreticinin yapılandırmasından, ölçüm modelinden (kapalı/açık) ve üreticinin kendi doygunluğundan gelir. Aracı seçmeden önce **kendi senaryonuzda** karşılaştırın.

```javascript
// k6: Black Friday kampanya saniyesi — AÇIK model (hedef istek/sn sabitlenir)
import http from 'k6/http';
import { sleep, check } from 'k6';

const urunler = JSON.parse(open('./urunler.json'));   // 5000 farklı SKU, gerçekçi dağılım

export const options = {
  scenarios: {
    kampanya_ani: {
      executor: 'ramping-arrival-rate',
      startRate: 20, timeUnit: '1s',
      preAllocatedVUs: 500, maxVUs: 3000,
      stages: [
        { target: 20,   duration: '2m' },    // sakin dönem (ısınma, ölçüme dahil değil)
        { target: 1200, duration: '10s' },   // kampanya saniyesi: ani yük
        { target: 1200, duration: '5m' },    // sabit yük: ölçüm burada
        { target: 20,   duration: '1m' },    // soğuma
      ],
    },
  },
  thresholds: {
    'http_req_duration{yol:odeme}':   ['p(95)<800', 'p(99)<2000'],
    'http_req_duration{yol:gezinme}': ['p(95)<400'],
    'http_req_failed':                ['rate<0.01'],
  },
};

export default function () {
  const sku = urunler[Math.floor(Math.random() * urunler.length)];
  const yanit = http.get(`${__ENV.BASE_URL}/urun/${sku}`, { tags: { yol: 'gezinme' } });
  check(yanit, { 'ürün sayfası 200 döndü': (r) => r.status === 200 });
  sleep(Math.random() * 4 + 2);   // düşünme süresi (think time): 2-6 sn
}
```

JMeter tarafında karşılığı, aynı planı GUI'de tasarlayıp yükü başsız koşmaktır:

```bash
jmeter -n -t kampanya-plani.jmx -l sonuc/ham.jtl -e -o sonuc/rapor
```

`-n` ekransız (non-GUI) koşum demektir. `-t` test planını, `-l` ham sonuç dosyasını belirtir. `-e -o` ise koşum sonunda HTML rapor üretir. Arayüz **yalnız** tasarım ve hata ayıklama içindir; yük her zaman bu biçimde koşulur.

**Kapalı model, açık model ve koordine edilmiş atlama.** Klasik VU tabanlı modele **kapalı (closed) model** denir. Bu modelde her sanal kullanıcı bir isteği **bitirmeden** yenisini göndermez. Sistem yavaşladığında modelin ürettiği yük de kendiliğinden azalır. Yani yük üreticisi, tam da sistem zorlandığı anda geri çekilir. Sonuç **koordine edilmiş atlamadır (coordinated omission)**: en kötü gecikmeler ölçüme hiç girmez ve rapor gerçekte olduğundan iyi görünür.

**Açık (open, arrival-rate) modelde** ise hedef istek/saniye sabittir. Sistem yavaşlarsa istekler birikir ve kuyruk gecikmesi ölçüme yansır. Gerçek dünyadaki kullanıcı davranışı da budur; çünkü kullanıcılar "sistem yavaşladı" diye tıklamayı bırakmaz. Kampanya saniyesi gibi senaryolarda açık model neredeyse zorunludur.

**Ortam ve veri gerçekçiliği — en çok burada yalan söylenir.**

- **Ortam boyutu.** Üretimin onda biri kaynakla kurulmuş bir test ortamında çıkan sayı doğrusal ölçeklenmez. Bağlantı havuzu, çöp toplayıcı (GC) davranışı ve önbellek isabet oranı farklı çalışır. Küçük ortamda test etmek yine de değerlidir. Ama sonuç **mutlak kapasite** olarak değil, **sürümler arası karşılaştırma** olarak okunmalıdır.
- **Veri hacmi.** 1000 ürünlük bir test veritabanında hemen her sorgu hızlıdır; indeks olmasa bile. 5 milyon üründe aynı sorgu tam tarama (seq scan) yapıp çöker. Üretim benzeri hacim olmadan yapılan yük testi, üretimdeki asıl darboğazı **tanım gereği** göremez.
- **Önbelleğin sonucu güzelleştirmesi.** Tüm sanal kullanıcılar aynı 5 ürünü çekiyorsa isabet oranı %100'e yakın olur. O zaman ölçtüğünüz şey büyük ölçüde önbellek performansıdır; gerçek okuma yolu hiç zorlanmaz. Diğer uçta tamamen düzgün rastgele seçim de gerçek dışıdır; çünkü gerçek trafikte popüler ürünler vardır. Doğru yaklaşım, üretimdeki erişim dağılımına yakın bir seçimdir — yani popüler ürünlerin ağırlıklı olduğu bir seçim.
- **Sıcak nokta (hot spot).** Aynı kampanya ürününü satın alan binlerce kullanıcı, veritabanında **aynı stok satırını** güncellemeye çalışır. Bu satır üzerindeki kilit çekişmesi işlemleri sıraya sokar. Sonuç şudur: CPU boşta görünürken iş hacmi çakılır. Bu, okuma tarafındaki önbellek sorununun yazma tarafındaki aynasıdır. Black Friday'in en klasik arızasıdır ve testte bilinçli olarak modellenmelidir.
- **Üçüncü parti.** Gerçek ödeme sağlayıcısına yük göndermek genellikle hem pahalıdır hem de sözleşmeye aykırıdır; bu yüzden sanal servis kullanılır. Ama sanal servis anında `200` dönerse ölçüm yalan olur. Gerçekçi gecikme ve hata oranı **enjekte edilmelidir**. Aksi hâlde sistemin zaman aşımı ve devre kesici (circuit breaker) davranışı hiç sınanmaz.

**Darboğaz avı.**

```svg
<svg viewBox="0 0 660 232" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Darbogaz zinciri: yuk ureteci, ag, uygulama, baglanti havuzu, veritabani, ucuncu parti">
  <defs>
    <marker id="yuk-arw" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Darboğaz zinciri: şüphe listesi soldan başlar</text>
  <rect x="22" y="34" width="92" height="52" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="68" y="56" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Yük üreteci</text>
  <text x="68" y="72" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kendi CPU'su?</text>
  <rect x="124" y="34" width="92" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="170" y="56" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Ağ / TLS</text>
  <text x="170" y="72" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">keep-alive var mı?</text>
  <rect x="226" y="34" width="92" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="272" y="56" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Uygulama</text>
  <text x="272" y="72" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">iş parçacığı havuzu</text>
  <rect x="328" y="34" width="92" height="52" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="374" y="56" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Bağlantı havuzu</text>
  <text x="374" y="72" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">en sık atlanan</text>
  <rect x="430" y="34" width="92" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="476" y="56" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Veritabanı</text>
  <text x="476" y="72" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">yavaş sorgu, kilit</text>
  <rect x="532" y="34" width="92" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="578" y="56" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">3. parti</text>
  <text x="578" y="72" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">gecikme, zaman aşımı</text>
  <line x1="114" y1="60" x2="122" y2="60" stroke="#5D7789" stroke-width="1.4" marker-end="url(#yuk-arw)"/>
  <line x1="216" y1="60" x2="224" y2="60" stroke="#5D7789" stroke-width="1.4" marker-end="url(#yuk-arw)"/>
  <line x1="318" y1="60" x2="326" y2="60" stroke="#5D7789" stroke-width="1.4" marker-end="url(#yuk-arw)"/>
  <line x1="420" y1="60" x2="428" y2="60" stroke="#5D7789" stroke-width="1.4" marker-end="url(#yuk-arw)"/>
  <line x1="522" y1="60" x2="530" y2="60" stroke="#5D7789" stroke-width="1.4" marker-end="url(#yuk-arw)"/>
  <text x="330" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">İstemci "p99 4 saniye" der ama o 4 saniyenin NEREDE geçtiğini söylemez.</text>
  <text x="330" y="126" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Sunucu tarafı metrik / APM olmadan yük testi yarım kalır.</text>
  <rect x="22" y="142" width="616" height="72" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="164" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Klasik yanlış teşhis</text>
  <text x="330" y="184" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"Veritabanı CPU'su %20, demek ki sorun veritabanı değil."</text>
  <text x="330" y="204" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">İstekler veritabanına hiç ulaşmadan bağlantı havuzunda sıra bekliyor olabilir — bu yüzden veritabanı boşta görünür.</text>
</svg>
```

Sırasıyla şüphelenilecekler:

1. **Yük üreticinin kendisi.** En sık atlanan madde budur. Tek makineden 5000 VU koşarken üreticinin CPU'su %90'a çıkmışsa, ölçülen gecikmenin bir kısmı **sizin makinenizde** üretilmiştir. Ayrıca kısa ömürlü port tükenmesi ve DNS çözümleme maliyeti de aynı yerde ortaya çıkar. Pratik kural şudur: yük üreticinin CPU'su %70'i geçiyorsa dağıtık koşuma geçin ve sonucu şüpheyle okuyun.
2. **Ağ ve bağlantı kurulumu.** Keep-alive kapalıysa her istek yeni bir TCP ve TLS el sıkışması demektir. O zaman ölçtüğünüz şey uygulama değil, el sıkışma maliyeti olur.
3. **Uygulama sunucusu iş parçacığı havuzu.** Havuz dolduğunda istekler kuyrukta bekler. CPU düşük görünür ama yanıt süresi patlar.
4. **Veritabanı bağlantı havuzu.** Klasik yanlış teşhis buradadır. Veritabanı CPU'su %20 diye "sorun veritabanı değil" denir. Oysa istekler veritabanına **hiç ulaşmadan** havuzda sıra bekliyordur. Havuz doluluğu ve bekleme süresi ayrıca ölçülmelidir.
5. **Yavaş sorgu, N+1 ve kilit çekişmesi.** Sıcak satır güncellemeleri burada görünür.
6. **Üçüncü parti servis.** Gecikmesi ve zaman aşımı ayarı; devre kesici davranışı.

**CI'da performans kapısı.** Her pull request'te tam yük testi koşmak hem pahalı hem gürültülüdür. Yaygın ve savunulabilir düzen üç katmanlıdır. Her PR'da çok kısa bir **duman testi (smoke test)** koşulur: 1-5 VU, 1 dakika. Amacı performans ölçmek değil, senaryonun bozulmadığını doğrulamaktır. Gecelik olarak bir **yük testi** koşulur. Sürüm öncesinde ise **stres ve dayanıklılık** testleri koşulur.

Eşik seçiminde iki yaklaşım birlikte kullanılır. **Mutlak eşik** (`p95 < 800 ms`) hedefi temsil eder ama ortam gürültüsüne duyarlıdır. **Regresyon eşiği** — bir önceki sürüme göre p95'te %20'den fazla kötüleşme — aynı ortamda karşılaştırma yaptığı için gürültüye daha dayanıklıdır. Paylaşımlı CI makinelerinde ölçüm zaten oynaktır. Toleransı gerçekçi tutmazsanız performans kapısı **kararsız (flaky)** bir teste dönüşür ve ekip onu kapatır. Kapatılan bir kapı, olmayan bir kapıdan daha kötüdür; çünkü var sanılır. Son olarak: sonuçları sürüm sürüm saklamak ve eğilim grafiği çıkarmak, tek koşumun mutlak sayısından çok daha fazla bilgi verir.

### Mülakatta ne sorulur / tuzaklar

- **"Yük, stres ve dayanıklılık testi arasındaki fark nedir?"** Beklenen: yük testi beklenen yükte SLO'ları ölçer; stres testi kırılma noktasını ve kırılma biçimini bulur; dayanıklılık testi zamanla biriken sızıntıyı ortaya çıkarır. Üçünü "sistemi zorlamak" diye tek başlıkta toplarsanız elenirsiniz.
- **"Kaç sanal kullanıcıyla test ettin?"** Beklenen: VU bir girdidir. Asıl cevap **iş hacmi** (istek/saniye) ve yanıt süresi yüzdelikleridir. "500 VU koştum" tek başına bir sonuç değildir.
- **"200 istek/saniye hedefliyoruz, kaç eşzamanlı istek bekleriz?"** Beklenen: Little yasası, yani iş hacmi × yanıt süresi. Bu sayı iş parçacığı ve bağlantı havuzu bütçesine çevrilir.
- **"Senaryona düşünme süresi koyar mısın?"** Beklenen: evet, gerçekçilik için. Ama düşünme süresi iş hacmini düşürür. Bu yüzden hedef istek/saniyeyi tutturmak için VU sayısı buna göre hesaplanır.
- **"Testte p95 45 ms çıktı, sistem hazır mı?"** Beklenen: veri hacmi, önbellek isabet oranı ve ürün seçimi sorgulanır. Aynı birkaç ürünü çeken bir test önbelleği ölçer, sistemi değil.
- **"Veritabanı CPU'su düşük, o hâlde darboğaz veritabanı değil, değil mi?"** Beklenen: hayır. Bağlantı havuzu doluysa istekler veritabanına hiç ulaşmaz. Havuz doluluğu ve bekleme süresi ayrıca ölçülmelidir.
- **"Yük testinin sonucuna neden tek başına güvenmezsin?"** Beklenen: sunucu tarafı metrik veya APM yoksa gecikmenin nerede geçtiği bilinmez. Ayrıca yük üreticinin kendisi doygun olabilir.
- **"CI'da performans kapısı nasıl kurulur?"** Beklenen: PR'da kısa bir duman testi, gecelik tam yük testi. Eşik olarak mutlak eşik ile önceki sürüme göre regresyon eşiği birlikte kullanılır. Tolerans ise gürültüye dayanıklı seçilir.

### Tek cümlede

Yük ve performans testi dört soruyu bilerek yürütülür: **hangi profilde** (yük, stres, dayanıklılık, ani yük, ölçeklenebilirlik, hacim), **hangi metrikte** (ortalama değil; iş hacmi ve p95/p99), **hangi modelde** (kapalı değil, çoğu zaman açık) ve **hangi gerçeklikte** (üretim benzeri veri, gerçekçi ürün dağılımı, gerçekçi üçüncü parti gecikmesi). Sonra Little yasasıyla sonuç kapasite planına çevrilir. Son olarak darboğaz, yük üreticisinden veritabanı bağlantı havuzuna kadar sunucu tarafı metriklerle isimlendirilir.

# yuk-1 | Orta
## Soru
Uygulama her sürümde yaklaşık iki saat çalıştıktan sonra yavaşlıyor, sonunda `OutOfMemoryError` ile yeniden başlıyor. Sürüm öncesi koşulan 10 dakikalık yük testleri ise her seferinde temiz geçiyor. Bu sorunu ortaya çıkaracak performans testi türü hangisidir?
## A
Ani yük (spike) testi; yükü saniyeler içinde onlarca kat artırarak sistemin hazırlıksız yakalandığı anı ölçer
🤖 Ani yük testi kısa süreli bir tepe uygular; saatler içinde biriken bir sızıntıyı ortaya çıkarmaz.
## B
Dayanıklılık (soak / endurance) testi; orta bir yükte saatlerce koşarak zamanla biriken kaynak sızıntılarını görünür kılar
🤖 Doğru: bellek sızıntısı, kapatılmayan bağlantı ve dolan disk gibi birikimli sorunlar yalnız uzun süreli koşumda ortaya çıkar.
## C
Stres (stress) testi; yükü kırılma noktasına kadar artırıp sistemin nerede ve nasıl çöktüğünü belirler
🤖 Stres testi kapasite sınırını bulur; iki saatlik birikimle oluşan sızıntı bu profilde de görünmez.
## D
Hacim (volume) testi; veri boyutunu üretim seviyesine çıkararak sorgu planlarının bozulup bozulmadığını sınar
🤖 Hacim testi veri büyüklüğünü değiştirir, koşum süresini değil; zamanla biriken bellek sızıntısını hedeflemez.
## Doğru
B
## Açıklama
Anlatılan belirti klasik bir **birikim** sorunudur: bellek sızıntısı, kapatılmayan veritabanı/HTTP bağlantısı, sınırsız büyüyen bir önbellek veya dolan disk. Bu tür arızalar yükün büyüklüğüyle değil **süresiyle** ortaya çıkar; on dakikalık bir koşum, iki saatte dolan bir yığını (heap) doldurmaya yetmez. **Dayanıklılık testi** (soak / endurance) tam bunun içindir: orta, sürdürülebilir bir yükte saatlerce koşulur ve bellek kullanımı, GC davranışı, açık bağlantı sayısı ve disk gibi metriklerin **eğilimi** izlenir — düz bir çizgi bekleniyorsa yukarı tırmanan bir çizgi bulgudur. Ani yük (A) kısa bir tepe uygular, stres (C) kapasite sınırını arar, hacim (D) veri boyutunu değiştirir; üçü de zaman eksenindeki birikimi hedeflemediği için bu arızayı kaçırır.

# yuk-2 | Orta
## Soru
k6 senaryonuzda `sleep()` çağrısı hiç yok; 200 sanal kullanıcı (VU) arka arkaya, hiç beklemeden istek gönderiyor. Ekip "200 eşzamanlı kullanıcıyı simüle ettik" diyor. Bu kurulumun temel sorunu nedir?
## A
k6 `sleep` çağrısı olmadan senaryoyu çalıştırmaz; iterasyon tanımı eksik kaldığı için koşum hata vererek düşer
🤖 k6 `sleep` olmadan sorunsuz koşar; sorun aracın çalışmaması değil, üretilen yükün gerçeği temsil etmemesidir.
## B
Düşünme süresi olmadan k6 istek sürelerini ölçemez; `http_req_duration` metriği toplanamadığı için rapor boş çıkar
🤖 Metrikler think time olsa da olmasa da toplanır; ölçüm yeteneği bekleme süresine bağlı değildir.
## C
Düşünme süresi yalnızca JMeter'ın thread group modelinde anlamlıdır; k6 senaryolarında bu kavramın bir karşılığı bulunmaz
🤖 Düşünme süresi araçtan bağımsız bir modelleme kavramıdır ve k6'da `sleep()` ile doğrudan karşılığı vardır.
## D
200 VU gerçek 200 kullanıcıyı değil, çok daha yoğun ve gerçek dışı bir davranışı temsil eder; üretilen yük profili gerçek trafiğe benzemez
🤖 Doğru: beklemesiz VU, gerçek bir insanın davranışını değil kesintisiz bir yük motorunu taklit eder.
## Doğru
D
## Açıklama
Gerçek bir kullanıcı sayfayı okur, ürünü inceler, karar verir; iki isteği arasında saniyeler geçer. **Düşünme süresi (think time)** bu boşluğu modeller. Beklemesiz koşan bir VU ise saniyede onlarca istek üretir, dolayısıyla 200 VU 200 kullanıcıya değil, belki binlerce kullanıcının ürettiği yüke karşılık gelir — ama ürettiği yükün **profili** de gerçek dışıdır: bağlantı yeniden kullanımı, önbellek isabet oranı ve isteklerin zamana yayılımı gerçek trafikten sapar. Doğru yaklaşım rastgele bir bekleme koymak (`sleep(2..6)`) ve hedef istek/saniyeyi tutturmak için gereken VU sayısını Little yasasıyla hesaplamaktır — think time verimi düşürdüğü için daha çok VU gerekir. A ve B aracın davranışı hakkında yanlış iddialardır; C ise araçtan bağımsız bir modelleme kavramını JMeter'a özgü sanar.

# yuk-3 | İleri
## Soru
Bir servis için hedef iş hacmi (throughput) 240 istek/saniye ve ölçülen ortalama yanıt süresi 0,5 saniye. Little yasasına göre sistemde ortalama olarak kaç istek eşzamanlı bulunur ve bu sayı ne işe yarar?
## A
120 eşzamanlı istek; iş parçacığı havuzu ve veritabanı bağlantı havuzu bütçesi bu sayıyı karşılayacak biçimde planlanır
🤖 Doğru: L = λ × W → 240 × 0,5 = 120; bu sayı doğrudan havuz boyutlandırma kararına çevrilir.
## B
480 eşzamanlı istek; eşzamanlılık verimin yanıt süresine bölünmesiyle bulunur ve havuz boyutu buna göre seçilir
🤖 Little yasasında çarpma vardır, bölme değil; 240 ÷ 0,5 işlemi yasanın yanlış uygulanmasıdır.
## C
240 eşzamanlı istek; yanıt süresi bir saniyenin altında olduğunda hesaba katılmaz, eşzamanlılık verime eşit alınır
🤖 Böyle bir eşik yoktur; yanıt süresi ne olursa olsun çarpana dahildir, 1 saniye özel bir değer değildir.
## D
Hesaplanamaz; Little yasası yalnızca kuyruk teorisi problemlerinde geçerlidir ve HTTP servislerine uygulanamaz
🤖 Little yasası kararlı durumdaki her kuyruk sistemi için geçerlidir; bir HTTP servisi de tam olarak böyle bir sistemdir.
## Doğru
A
## Açıklama
Little yasası `L = λ × W` biçimindedir: sistemdeki ortalama iş sayısı (`L`), varış/tamamlanma hızı (`λ`, burada 240 istek/saniye) ile bir işin sistemde geçirdiği ortalama süreyi (`W`, burada 0,5 saniye) çarpar → **120 eşzamanlı istek**. Bu sayı soyut bir merak değildir: uygulama sunucusunun iş parçacığı havuzu ve veritabanı bağlantı havuzu en az bu eşzamanlılığı taşıyacak (ya da bilinçli olarak sınırlayıp kuyruklayacak) biçimde boyutlandırılır. Aynı yasa ters yönde de okunur: sabit VU sayısıyla koşan bir testte `verim ≈ VU ÷ (yanıt süresi + düşünme süresi)` — bu yüzden sistem yavaşladığında aynı VU sayısı **daha az** verim üretir ve "500 VU ile test ettim" cümlesi tek başına bir kapasite ifadesi değildir. B işlemi tersine çevirir, C uydurma bir eşik ekler, D ise yasanın geçerlilik alanını gereksiz daraltır — kararlı durumdaki bir HTTP servisi de bir kuyruk sistemidir.

# yuk-4 | İleri
## Soru
Yük üreticiniz 500 VU ile **kapalı döngü** (closed model) çalışıyor: her sanal kullanıcı bir isteği bitirmeden yenisini göndermiyor. Sistem yavaşladıkça raporlanan gecikme yüzdelikleri beklenenden **iyi** çıkıyor ve gerçek kullanıcı şikâyetleriyle uyuşmuyor. Nedeni ve doğru düzeltme nedir?
## A
Yük üreticinin saati sunucununkiyle kaymış; NTP senkronizasyonu düzeltilerek ölçüm doğrulanır
🤖 Gecikme tek makinede istek başlangıcı ve bitişi arasından ölçülür; saat kayması bu sistematik iyimserliği açıklamaz.
## B
Sanal kullanıcı sayısı yetersiz; 5000 VU'ya çıkıldığında yüzdelikler gerçeğe yaklaşır ve sorun ortadan kalkar
🤖 VU sayısını artırmak kapalı modelin geri çekilme davranışını değiştirmez; aynı iyimserlik daha yüksek yükte tekrarlanır.
## C
Sistem yavaşlayınca üreteç de yavaşlar; kaçırılan yük ölçüme girmez — hedef istek/saniyeyi sabitleyen açık model (arrival rate) kullanılmalıdır
🤖 Doğru: koordine edilmiş atlama (coordinated omission) tam olarak budur; açık modelde kuyruk gecikmesi ölçüme yansır.
## D
Düşünme süresi fazla yüksek ayarlanmış; `sleep` çağrıları kaldırılırsa yüzdelikler gerçek değerlerine oturur
🤖 Think time'ı kaldırmak yükü artırır ama kapalı modelin yavaşlamaya geri çekilerek tepki vermesini engellemez.
## Doğru
C
## Açıklama
Kapalı (closed) modelde yük, sistemin hızına **bağımlıdır**: her VU bir yanıtı beklerken yeni istek göndermez, dolayısıyla sistem yavaşladıkça üreticinin ürettiği istek/saniye de düşer. Sonuç, tam da sistemin en çok zorlandığı anda ölçüme girmeyen isteklerdir — buna **koordine edilmiş atlama** (coordinated omission) denir ve raporu sistematik olarak iyimser yapar. Gerçek dünyada kullanıcılar böyle davranmaz: site yavaşladı diye tıklamayı bırakmazlar, aksine yenilerler; istekler birikir. Açık (open / arrival-rate) modelde hedef istek/saniye sabittir; sistem yavaşladığında istekler kuyruklanır ve o kuyruk süresi ölçülen gecikmeye **dahil olur**. k6'da bu, `ramping-vus` yerine `constant-arrival-rate` / `ramping-arrival-rate` seçmek demektir. A saat senkronizasyonunu suçlar ama gecikme tek makinede ölçülür; B daha çok VU ekler ama aynı geri çekilme davranışı sürer; D think time'ı kaldırarak yükü artırır, modelin kendisini değiştirmez.

# yuk-5 | İleri
## Soru
Ödeme uç noktasında p99 yanıt süresi 4 saniyeye çıkıyor. Sunucu tarafı metrikler şöyle: uygulama sunucusu CPU %35, veritabanı CPU %20, yavaş sorgu logu boş, ağ doygun değil, yük üreticinin CPU'su %40. Bağlantı havuzunda (HikariCP) `maximumPoolSize=10`, aktif bağlantı sürekli 10 ve bağlantı bekleme kuyruğu dolu. En olası darboğaz hangisidir?
## A
Veritabanı; sorgular indekssiz çalışıyor olmalı, ilgili tablolara indeks eklenerek yanıt süresi düşürülür
🤖 Yavaş sorgu logu boş ve veritabanı CPU'su düşük; sorgular veritabanına ulaştıklarında hızlı dönüyor.
## B
Ağ katmanı; keep-alive kapalı olduğu için her istek yeni TLS el sıkışması yapıyor ve gecikme buradan geliyor
🤖 Ağın doygun olmadığı ölçülmüş; ayrıca el sıkışma maliyeti p99'u 4 saniyeye çıkaracak mertebede değildir.
## C
Yük üreteci; tek makineden üretilen yük üreticinin kendisini doyurmuş, ölçülen gecikme büyük ölçüde istemcide oluşuyor
🤖 Üreticinin CPU'su %40'ta; doygunluk eşiğinin altında olduğu için ölçüm bu nedenle bozulmuyor.
## D
Bağlantı havuzu; istekler veritabanına hiç ulaşmadan havuzda sıra bekliyor, bu yüzden veritabanı boşta görünüyor
🤖 Doğru: havuz sürekli dolu ve bekleme kuyruğu doluysa gecikme veritabanında değil havuz kuyruğunda geçiyordur.
## Doğru
D
## Açıklama
Verilen metrikler bir eleme yapıyor: veritabanı CPU'su düşük ve yavaş sorgu logu boşsa, sorgular veritabanına **ulaştıklarında** hızlı dönüyor demektir; ağ doygun değil; yük üreticinin CPU'su da doygunluk eşiğinin (~%70) altında. Geriye tek tutarlı açıklama kalıyor: `maximumPoolSize=10` olan havuzda aktif bağlantı **sürekli** 10 ve bekleme kuyruğu dolu — yani istekler zamanlarının büyük kısmını bir bağlantı sırasında geçiriyor. Bu, performans avındaki en klasik yanlış teşhisin kaynağıdır: "veritabanı CPU'su %20, sorun veritabanı değil" denir; oysa istekler veritabanına hiç ulaşmadığı için veritabanı **boşta görünür**. Düzeltme kör bir havuz büyütmesi değildir: önce her isteğin bağlantıyı ne kadar süre tuttuğu (uzun süren işlemler, işlem sınırları dışında yapılan uzak çağrılar) incelenir, sonra havuz boyutu veritabanının kaldırabileceği eşzamanlılık ve Little yasasıyla hesaplanan ihtiyaç birlikte değerlendirilerek ayarlanır.

# yuk-6 | Uzman
## Soru
Black Friday hazırlığı için 2000 VU ile yük testi koşuldu; **tüm** sanal kullanıcılar aynı 5 kampanya ürününün detay sayfasını çekiyor. Sonuç p95 = 45 ms çıktı ve ekip "sistem hazır" dedi. Bu sonucun en kritik sorunu nedir?
## A
2000 VU bir yük testi için düşük bir sayıdır; en az 10.000 VU altında yapılan ölçümler kapasite kararı için geçerli sayılmaz
🤖 Böyle bir alt sınır kuralı yoktur; gereken VU sayısı hedef verimden ve yanıt süresinden hesaplanır, sabit bir eşikten değil.
## B
Aynı 5 ürün önbelleğe ısınır; ölçülen şey büyük ölçüde önbellek performansıdır, gerçek okuma yolu ve veritabanı hiç zorlanmaz
🤖 Doğru: isabet oranı %100'e yaklaşınca test sistemin değil önbelleğin hızını raporlar.
## C
p95 yerine ortalama yanıt süresine bakılmalıydı; kampanya senaryolarında ortalama kullanıcı deneyimini daha doğru temsil eder
🤖 Tam tersi: ortalama uzun kuyruğu gizler, yüzdelikler tercih edilir; metrik seçimi burada zaten doğru yapılmış.
## D
Ürün detay sayfası yerine ana sayfa test edilmeliydi; kampanya trafiği ilk olarak ana sayfaya geldiği için ölçüm yanlış sayfada yapılmış
🤖 Kampanya trafiği doğrudan ürün ve sepet yollarına da akar; sorun hangi sayfanın seçildiği değil, verinin gerçekçi olmamasıdır.
## Doğru
B
## Açıklama
Beş ürünü çeken iki bin sanal kullanıcı, önbellek isabet oranını %100'e yakın bir yere sabitler: ilk birkaç istekten sonra hiçbir istek gerçek okuma yoluna, uygulama katmanının derinliklerine veya veritabanına **inmez**. Ölçülen 45 ms, önbelleğin hızını raporlar; sistemin Black Friday'de yapacağı işi değil. Gerçekçi kurulum, üretimin erişim dağılımına yakın bir ürün seçimidir — tamamen düzgün rastgele seçim de gerçek dışıdır, çünkü gerçek trafikte popüler ürünler ağır basar; doğru olan popülerliği ağırlıklandırmaktır. Bu okuma tarafındaki sorunun **yazma tarafındaki aynası** aynı senaryoda gizlidir ve daha tehlikelidir: binlerce kullanıcı aynı kampanya ürününü satın aldığında veritabanında **aynı stok satırı** güncellenir; o satır üzerindeki kilit çekişmesi işlemleri sıraya sokar ve CPU boşta görünürken verim çakılır. Sepet–ödeme adımı Black Friday'in en kritik yoludur ve tam bu sıcak nokta üzerinden geçer. A uydurma bir VU alt sınırı öne sürer, C metrik seçimini tersine çevirir (ortalama kuyruğu gizler), D ise sayfa seçimini suçlayarak asıl kusuru — veri ve önbellek gerçekçiliğini — ıskalar.
