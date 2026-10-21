---
baslik: "Hafta 9 Görsel Özet: Altıgen (Hexagonal) ve Temiz (Clean) Mimari"
yazar: "Get Your Hands Dirty on Clean Architecture 2e (Hombergs) — kavram özeti"
tarih: "2026-07-10"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 9
---

### Bu metin ne anlatıyor?
Bir uygulamanın "asıl işi" (kurallar, hesaplar, kararlar) ile onu dış dünyaya bağlayan
ayrıntıları (web arayüzü, veritabanı, dosya, e-posta) birbirinden ayırmanın yolunu anlatır.
Bu ayrımın iki yaygın adı vardır: **altıgen (hexagonal)** ve **temiz (clean)** mimari.
Metin, ayrımın nasıl kurulduğunu, ayrımı bozmadan veri taşımanın yollarını ve ayrımın
gerçekten durduğunu nasıl testle kanıtlayacağını gösterir.

## Neden bu özet
Bu haftanın hedefi port-adaptör (ports & adapters) mimarisini Java'da uygulamak: iş
mantığını dış dünyadan (web, veritabanı) yalıtmak ve bu yalıtımı ArchUnit ile mühürlemek.
Aşağıdaki üç diyagram fikri kısaca gösterir; aradaki metin kavramları tek tek tanımlar.

## Önce sorun: iş mantığı çerçeveye yapışırsa ne olur
Tipik bir uygulamada iş kuralı, onu taşıyan teknolojiye yapışır: sipariş toplamı bir web
denetleyicisinin (controller) içinde hesaplanır, indirim kuralı bir veritabanı sorgusuna
gömülüdür. Üç sonucu olur. **Test yavaşlar** — tek kuralı sınamak için veritabanı ve sunucu
ayağa kalkar. **Teknoloji değişimi iş kuralını riske atar** — çerçeveyi (framework)
değiştirmek kuralları da elden geçirmek demektir. **Kural görünmez olur** — "bu sistemin
kuralları neler?" sorusunun cevabı hiçbir yerde tek parça durmaz.

Altıgen mimarinin tek amacı budur: **iş kuralını, onu taşıyan teknolojiden koparmak.**

## Port ve adaptör nedir
- **Çekirdek (core / domain):** uygulamanın asıl işi. Ne veritabanı bilir, ne HTTP, ne de
  hangi çerçeveyle koştuğunu. Saf Java sınıflarıdır.
- **Port (port):** çekirdeğin sınırındaki bir **arayüzdür** (Java'da `interface`). Bir
  sözleşmedir: "şu iş yapılabilir" ya da "şu bilgiye ihtiyacım var" der; nasıl yapılacağını
  söylemez.
- **Adaptör (adapter):** o sözleşmeyi belirli bir teknolojiyle **yerine getiren** sınıf. Aynı
  port için birden çok adaptör olabilir: PostgreSQL adaptörü, bellek içi test adaptörü.

Benzetme: priz bir porttur, fiş bir adaptördür. Priz hangi cihazın takılacağını bilmez; cihaz
prizin şekline uyar. Yön böylece tersine döner — **uyum sağlayan taraf her zaman dıştakidir.**

## Diyagram 1 — Altıgen (Hexagonal / Ports & Adapters)
Çekirdek ortadadır; portlar (port) çekirdeğin sınırındaki arayüzlerdir; adaptörler (adapter)
dıştadır. Giriş adaptörü giriş portunu çağırır, çekirdek de çıkış portu üzerinden çıkış
adaptörüne ulaşır.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Altıgen mimari: ortada alan çekirdeği, sınırda giriş ve çıkış portları, dışta adaptörler">
  <defs>
    <marker id="h9ok1" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="h9ok2" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Altıgen Mimari (Ports &amp; Adapters)</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Çekirdek ortada · portlar sınırda arayüz · adaptörler dışta</text>
  <polygon points="220,140 270,90 370,90 420,140 370,190 270,190" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.8"/>
  <text x="320" y="136" text-anchor="middle" font-size="13" font-weight="700" fill="#4F46E5" font-family="system-ui">Alan Çekirdeği</text>
  <text x="320" y="155" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">(Domain / iş mantığı)</text>
  <rect x="18" y="108" width="112" height="64" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="74" y="132" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Web/REST</text>
  <text x="74" y="148" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Adapter</text>
  <text x="74" y="163" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">giriş adaptörü</text>
  <rect x="140" y="116" width="62" height="48" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.5"/>
  <text x="171" y="137" text-anchor="middle" font-size="10.5" font-weight="600" fill="#186399" font-family="system-ui">Giriş</text>
  <text x="171" y="151" text-anchor="middle" font-size="10.5" font-weight="600" fill="#186399" font-family="system-ui">Portu</text>
  <rect x="438" y="116" width="62" height="48" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.5"/>
  <text x="469" y="137" text-anchor="middle" font-size="10.5" font-weight="600" fill="#25804C" font-family="system-ui">Çıkış</text>
  <text x="469" y="151" text-anchor="middle" font-size="10.5" font-weight="600" fill="#25804C" font-family="system-ui">Portu</text>
  <rect x="510" y="108" width="112" height="64" rx="10" fill="#EAF1F7" stroke="#25804C" stroke-width="1.5"/>
  <text x="566" y="132" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Persistence</text>
  <text x="566" y="148" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Adapter</text>
  <text x="566" y="163" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">çıkış adaptörü / DB</text>
  <line x1="130" y1="140" x2="138" y2="140" stroke="#186399" stroke-width="1.8" marker-end="url(#h9ok1)"/>
  <line x1="202" y1="140" x2="218" y2="140" stroke="#186399" stroke-width="1.8" marker-end="url(#h9ok1)"/>
  <line x1="422" y1="140" x2="436" y2="140" stroke="#25804C" stroke-width="1.8" marker-end="url(#h9ok2)"/>
  <line x1="500" y1="140" x2="508" y2="140" stroke="#25804C" stroke-width="1.8" marker-end="url(#h9ok2)"/>
  <text x="320" y="214" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Portlar = çekirdeğin sınırındaki arayüzler (interface); adaptörler bu arayüzleri uygular.</text>
</svg>
```

## Gelen ve giden port — asıl kural "portu kim tanımlar"
İki tür port vardır ve aralarındaki fark bu mimarinin kalbidir.

**Gelen port (incoming / driving port).** Dışarıdan çekirdeğe gelen isteğin kapısıdır: "yeni
sipariş oluştur", "para transfer et". Her biri bir **kullanım senaryosunu (use case)** temsil
eder. Arayüzü çekirdek tanımlar, uygulamasını yine çekirdek yazar; web adaptörü yalnızca
**çağırır**.

**Giden port (outgoing / driven port).** Çekirdeğin dış dünyadan istediği şeydir: "şu hesabı
yükle", "bu e-postayı gönder". Arayüzü **yine çekirdek tanımlar** — ama uygulamasını dıştaki
adaptör yazar.

Kritik nokta ikincisidir. Sezgi "veritabanı katmanı kendi arayüzünü yazar, çekirdek onu
kullanır" der; hexagonal bunun tam tersini söyler. **Sözleşmenin sahibi ihtiyacı olandır,
karşılayan değil.** Bunun adı **bağımlılığın tersine çevrilmesi (Dependency Inversion)**:
`HesapYukle` arayüzü çekirdekte durur, `JpaHesapAdaptoru` onu dışarıda **uygular** — böylece
derleme zamanı oku dışarıdan içeri bakar, çalışma zamanında ise çağrı içeriden dışarı gider.
Ok yönü ile çağrı yönü aynı olmak zorunda değildir; işin bütün numarası budur.

Pratik ölçüt: **çekirdek paketinde `import` satırlarına bak.** Orada bir çerçeve, bir sürücü
ya da bir HTTP sınıfı adı görüyorsan kural çoktan kırılmıştır.

## Diyagram 2 — Bağımlılık Kuralı (Dependency Rule)
Bütün bağımlılık okları İÇE bakar: dış halkalar (adaptörler, uygulama) içteki alan
çekirdeğine bağımlıdır, tersi asla olmaz. Çekirdek dış dünyayı tanımaz.

```svg
<svg viewBox="0 0 640 312" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bağımlılık kuralı: iç içe halkalar, bütün oklar merkeze doğru içe bakar">
  <defs>
    <marker id="h9ok3" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#4F46E5"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bağımlılık Kuralı (Dependency Rule)</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Bütün oklar içe bakar: dış katmanlar çekirdeğe bağımlıdır</text>
  <circle cx="320" cy="155" r="105" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <circle cx="320" cy="155" r="68" fill="#FFFFFF" stroke="#25804C" stroke-width="1.6"/>
  <circle cx="320" cy="155" r="34" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.8"/>
  <text x="320" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Adaptörler (Adapters)</text>
  <text x="320" y="106" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Uygulama (portlar · use case)</text>
  <text x="320" y="152" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">Alan</text>
  <text x="320" y="167" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">(Domain)</text>
  <line x1="168" y1="155" x2="213" y2="155" stroke="#4F46E5" stroke-width="2" marker-end="url(#h9ok3)"/>
  <line x1="472" y1="155" x2="427" y2="155" stroke="#4F46E5" stroke-width="2" marker-end="url(#h9ok3)"/>
  <text x="130" y="150" text-anchor="middle" font-size="11" font-weight="600" fill="#4F46E5" font-family="system-ui">İÇE →</text>
  <text x="510" y="150" text-anchor="middle" font-size="11" font-weight="600" fill="#4F46E5" font-family="system-ui">← İÇE</text>
  <text x="320" y="288" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Çekirdek hiçbir dış katmana bağımlı değildir.</text>
  <text x="320" y="304" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Dış katmanlar çekirdeğe bağımlıdır — oklar hep içe.</text>
</svg>
```

## "Temiz" ile "katmanlı" aynı şey değildir
İki mimari de kutuları üst üste dizer, ama bölme ölçütleri farklıdır:

| | Katmanlı (layered) | Temiz / altıgen (clean) |
|---|---|---|
| Bölme ölçütü | teknik rol (sunum, iş, veri) | değişme hızı ve sebebi |
| Ok yönü | yukarıdan aşağı | dıştan içe |
| En temel şey | veritabanı (en altta) | alan modeli (en içte) |
| Veritabanının statüsü | mimarinin temeli | değiştirilebilir bir **ayrıntı** |

Katmanlıda veritabanı en alta konduğu için farkında olmadan sistemin temeli hâline gelir:
tablolar önce tasarlanır, iş kuralları tabloların şekline uyar. Temiz mimaride en içte **en az
değişen** şey durur — iş kuralı. Veritabanı, en çok değişen şeylerden biri olduğu için en dışa,
adaptörlerin arasına düşer.

**Sık yapılan hata:** paket adlarını `domain / application / adapter` yapıp işin bittiğini
sanmak. İsimler mimari kurmaz; **oklar kurar.** Bir `domain` paketi çerçeve sınıflarını import
ediyorsa, adı ne olursa olsun orası katmanlı bir uygulamadır. Bu yüzden kural göz kararıyla
denetlenmez, testle mühürlenir — üçüncü diyagramın konusu budur.

## Veriyi sınırdan geçirmek: eşleme (mapping) stratejileri
Çekirdeğin modeli ile web/veritabanı modelleri farklıysa, sınırda bir çeviri gerekir. Dört
seçenek vardır ve hiçbiri her yerde doğru değildir:

1. **Eşleme yok (No Mapping).** Aynı sınıf her katmanda kullanılır. En ucuzu; ama web'in
   ihtiyacı (JSON alan adları) ve veritabanının ihtiyacı alan modeline sızar. Basit CRUD
   (oluştur-oku-güncelle-sil) işleri için kabul edilebilir.
2. **İki yönlü eşleme (Two-Way Mapping).** Her katmanın kendi modeli vardır; adaptör girerken
   ve çıkarken çevirir. Çekirdek tertemiz kalır; bedeli çevirici kod yığınıdır.
3. **Tam eşleme (Full Mapping).** Her kullanım senaryosu kendi giriş modelini tanımlar. En net
   sözleşme, en fazla sınıf.
4. **Tek yönlü eşleme (One-Way Mapping).** Katmanlar ortak bir okuma arayüzünü paylaşır, çeviri
   yalnız tek yönde yapılır. Alan nesneleri değişmez (immutable) olduğunda iyi çalışır.

Kural: tek stratejiyi bütün projeye dayatma. **Her sınır geçişi için ayrı seç** ve gerekçeyi
bir karar kaydına (ADR) yaz.

## Test edilebilirlik: mimarinin en somut getirisi
Bu ayrımın en ölçülebilir faydası testtir, çünkü **sınır aynı zamanda bir dikiştir (seam)** —
gerçek uygulamayı çıkarıp sahtesini takabildiğin yer.

- **Çekirdek testi:** hiçbir çerçeve, hiçbir veritabanı; saf birim testi (unit test),
  milisaniyeler sürer. Giden portların yerine sahte (mock/stub) nesne koyarsın; "kayıt
  bulunamadı", "ödeme reddedildi" gibi durumları kurmak bir satırdır.
- **Adaptör testi:** dar dilim. Web adaptörü için yalnız web katmanı, kalıcılık adaptörü için
  yalnız veritabanı katmanı ayağa kalkar.
- **Uçtan uca test (end-to-end):** her şeyi birden çalıştırır; en yavaş ve en kırılgan
  olanıdır, bu yüzden **en az sayıda** yazılır.

## Diyagram 3 — ArchUnit Mührü (katman yönü tek yön)
Bu projede katman bağımlılığı tek yönlüdür: types → config → repo → service → ai → web.
ArchUnit bu yönü zorlar; ters bağımlılık (örn. service → web) derlemede/testte kırmızı olur.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ArchUnit mührü: katmanlar tek yönde types config repo service ai web, ters yön yasak">
  <defs>
    <marker id="h9ok4" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="h9ok5" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#C77A17"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">ArchUnit Mührü: katman yönü tek yön</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">types → config → repo → service → ai → web</text>
  <rect x="12" y="90" width="80" height="54" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="52" y="114" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">types</text>
  <text x="52" y="130" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tipler</text>
  <rect x="116" y="90" width="80" height="54" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="156" y="114" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">config</text>
  <text x="156" y="130" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">yapılandırma</text>
  <rect x="220" y="90" width="80" height="54" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="260" y="114" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">repo</text>
  <text x="260" y="130" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">depo</text>
  <rect x="324" y="90" width="80" height="54" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="364" y="114" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">service</text>
  <text x="364" y="130" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">servis</text>
  <rect x="428" y="90" width="80" height="54" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="468" y="114" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">ai</text>
  <text x="468" y="130" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">yapay zeka</text>
  <rect x="532" y="90" width="80" height="54" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="572" y="114" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">web</text>
  <text x="572" y="130" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">web/REST</text>
  <line x1="92" y1="117" x2="114" y2="117" stroke="#186399" stroke-width="1.8" marker-end="url(#h9ok4)"/>
  <line x1="196" y1="117" x2="218" y2="117" stroke="#186399" stroke-width="1.8" marker-end="url(#h9ok4)"/>
  <line x1="300" y1="117" x2="322" y2="117" stroke="#186399" stroke-width="1.8" marker-end="url(#h9ok4)"/>
  <line x1="404" y1="117" x2="426" y2="117" stroke="#186399" stroke-width="1.8" marker-end="url(#h9ok4)"/>
  <line x1="508" y1="117" x2="530" y2="117" stroke="#186399" stroke-width="1.8" marker-end="url(#h9ok4)"/>
  <path d="M566,150 Q320,205 58,150" fill="none" stroke="#C77A17" stroke-width="1.8" stroke-dasharray="6 4" marker-end="url(#h9ok5)"/>
  <line x1="312" y1="170" x2="328" y2="184" stroke="#C77A17" stroke-width="2.5"/>
  <line x1="328" y1="170" x2="312" y2="184" stroke="#C77A17" stroke-width="2.5"/>
  <text x="320" y="204" text-anchor="middle" font-size="10.5" fill="#C77A17" font-family="system-ui">Ters yön (örn. service → web) yasak: ArchUnit testi KIRMIZI.</text>
</svg>
```

## Bedeli — ve ne zaman kullanmamalı
> **Şerh:** Bu mimariyi anlatan kitaplar ve konuşmalar onu genellikle bedelsiz bir "iyi
> pratik" gibi sunar. Değildir. Port, adaptör ve eşleme sınıfları **sınıf sayısını ve dolaylı
> çağrı sayısını belirgin biçimde artırır**; küçük ve kısa ömürlü bir CRUD servisinde bu net
> bir kayıptır. Kazanç, iki koşul birlikte sağlandığında gelir: iş kuralları zenginse **ve**
> sistemin ömrü uzunsa. "Her projede uygula" tavsiyesini, kendi projenin bu iki ölçütüne
> vurmadan kabul etme.

Ayrıca en çok anılan gerekçe olan "veritabanını kolayca değiştirirsin" pratikte en az
gerçekleşen faydadır. Gündelik getiri **hızlı test ve okunur iş kuralıdır**; gerekçeni buraya
dayandır.

## Öz-sınav (kendini test et)
1. Bir port ile bir adaptör arasındaki farkı, "sözleşme" ve "gerçekleştirme" kelimelerini
   kullanarak tanımla. Aynı port için neden birden fazla adaptör bulunabilir?
2. Giden bir portun arayüzünü **kim** tanımlar, **kim** uygular? Sezginin tersini söyleyen bu
   kural hangi ilkenin uygulanışıdır ve derleme zamanı ok yönü ile çalışma zamanı çağrı yönü
   neden birbirinden farklıdır?
3. Elinde `domain`, `application`, `adapter` adlı üç paket var. Bu isimlendirme mimarinin
   temiz olduğunu kanıtlar mı? Kanıtlamıyorsa, kanıt olarak neye bakarsın?
4. Katmanlı mimaride veritabanı en altta, temiz mimaride en dışta durur. Bu yer değişikliği
   tasarım kararlarını pratikte nasıl değiştirir — bir örnekle anlat.
5. Dört eşleme stratejisinden hangisi çekirdeği en iyi korur, hangisi en ucuzdur? Bir projede
   ikisini birden kullanmak neden savunulabilir bir karardır?
6. Bu mimarinin faturasını bir cümleyle yaz. Hangi iki koşul birlikte sağlanmadığında
   hexagonal uygulamak zarar yazar?

### Cevap anahtarı (önce kendin cevapla, sonra bak)
1. Port bir arayüzdür: ne yapılacağını söyler, nasılını söylemez — sözleşmedir. Adaptör o
   sözleşmeyi belirli bir teknolojiyle gerçekleştirir. Aynı sözleşme farklı teknolojilerle
   karşılanabildiği için birden çok adaptör olur (gerçek veritabanı, bellek içi test kopyası).
2. Arayüzü **çekirdek** tanımlar, dıştaki adaptör **uygular**. Bu, bağımlılığın tersine
   çevrilmesidir (Dependency Inversion). Derleme zamanında adaptör çekirdeğin arayüzüne
   bağımlıdır (ok içe); çalışma zamanında ise çekirdek o arayüz üzerinden adaptörü çağırır
   (akış dışa).
3. Kanıtlamaz. Kanıt bağımlılık yönüdür: çekirdek paketindeki `import` satırlarında çerçeve,
   sürücü ya da HTTP sınıfı geçmemelidir. Gözle denetlenemez, ArchUnit'le mühürlenir.
4. Katmanlıda tablolar önce tasarlanır, iş kuralı tablonun şekline uyar; temiz mimaride önce
   iş kuralı yazılır, tablo ona uyar. Örnek: indirim kuralını önce alan modelinde ifade
   edersin, saklama şemasını sonra adaptörde çözersin — kural sütun tipine göre bükülmez.
5. En iyi koruyan tam eşleme, en ucuz olan eşleme yapmamaktır. Birlikte kullanmak
   savunulabilir çünkü her sınır aynı riski taşımaz — yeter ki gerekçe ADR'ye yazılsın.
6. Faturası artan sınıf ve dolaylı çağrı sayısıdır. İş kuralları zengin **değilse** ya da
   sistemin ömrü kısaysa bu maliyet karşılıksız kalır.
