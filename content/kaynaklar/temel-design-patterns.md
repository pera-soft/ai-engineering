---
baslik: "Temeller Tazeleme: Tasarım Kalıpları (Factory, Strategy, Proxy)"
yazar: "Faz 0 — Mühendislik Temelleri"
tarih: "2026-07-10"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 5,6
---

### Bu metin ne anlatıyor?
Bu metin, yazılımda sık karşılaşılan üç soruna verilmiş hazır çözüm reçetelerini anlatıyor:
nesneleri kim yaratsın (Factory), aynı işin birden çok yapılış yolu varken hangisini nasıl
seçelim (Strategy), bir nesneye erişimi nasıl denetleyelim (Proxy). Her biri için hangi
problemi çözdüğü, iskeleti ve **ne zaman kullanılmaması gerektiği** var. Sonunda birbirine
benzeyen ve sınavda en çok karıştırılan kalıpların farkını ayıran kısa bir bölüm bulacaksın.

## Neden bu özet
Tasarım kalıbı (design pattern) = **tekrar eden bir tasarım sorununa verilmiş, isimli
ve denenmiş çözüm.** Yeni bir icat değil; usta yazılımcıların defalarca karşılaştığı
problemlere buldukları ortak çözümlerin bir isim altında toplanmış hâlidir. İsimli
olması iletişimi kısaltır: "burada bir Factory kullandım" demek, on satır açıklamanın
yerine geçer. Bu özet üç temel kalıbı — Factory, Strategy, Proxy — hangi problemi
çözdükleri, iskeletleri ve **ne zaman KULLANMAMAK gerektiği** ile birlikte tazeler.
Amaç kalıp ezberlemek değil; problemi görüp doğru kalıbı (veya hiç kalıp olmamasını)
seçebilmektir. Kalıpların bugün kullanılan ortak adları büyük ölçüde 1994'te yayımlanan
*Design Patterns* kitabından gelir; dört yazarı yüzünden literatürde **GoF (Gang of Four —
Dörtlü Çete)** kalıpları diye anılır.

### Bu metinde geçen temel terimler
- **Nesne (object):** Programın çalışırken bellekte tuttuğu, verisi ve davranışı olan somut
  birim. **Sınıf (class)** onun kalıbı, nesne o kalıptan üretilmiş örnektir.
- **Arayüz (interface):** Bir nesnenin dışarıya söz verdiği işlemlerin listesi; işin nasıl
  yapıldığını değil, hangi işlemlerin çağrılabileceğini söyler.
- **Somut sınıf (concrete class):** Arayüzdeki sözü fiilen yerine getiren, `new` ile nesnesi
  üretilebilen sınıf.
- **İstemci (client):** Bu yapıyı kullanan taraftaki kod.
- **Çalışma anı (runtime):** Programın çalıştığı an. **Derleme anı (compile time):** kodun
  makine diline çevrildiği, henüz çalışmadığı an. "Çalışma anında seçilir" demek, kararın
  ancak program çalışırken belli olan bilgiye (kullanıcı seçimi, ayar dosyası) dayandığı
  anlamına gelir.
- **Sarmalama (wrapping):** Bir nesneyi başka bir nesnenin içine koyup çağrıları onun
  üzerinden geçirmek; araya davranış eklemenin standart yolu.

## 1. Factory (Fabrika) — nesne yaratımını merkezileştir

**Hangi problemi çözer?** `new` çağrısı sınıfın adını sabitler. Yaratılacak somut
sınıf koşula göre değişiyorsa (girdiye, ayara, ortama göre) ve bu karar kodun her
yerine `if/else new ...` olarak dağılıyorsa, tek bir tip eklemek onlarca yeri
değiştirmeyi gerektirir. Factory bu "hangi somut sınıfı yaratayım?" kararını **tek bir
yere** toplar; istemci (client) yalnızca arayüzü (interface) tanır.

**Yapı:** İstemci → Factory → Ürün arayüzü → {SomutÜrünA, SomutÜrünB}.

```java
interface Odeme { void ode(double tutar); }
class OdemeFabrikasi {
    static Odeme olustur(String tip) {                 // karar tek yerde
        return "kart".equals(tip) ? new KartOdeme() : new HavaleOdeme();
    }
}
```

Not: Yukarıdaki, günlük dilde "fabrika" denen en yalın biçimdir — **basit fabrika (simple
factory)**: kararı veren tek bir statik metot. GoF kitabındaki *Factory Method* ve *Abstract
Factory* kalıpları bunun daha yapılı akrabalarıdır; farkları aşağıdaki 5. bölümde.

**Ne zaman KULLANMA:** Tek bir somut sınıfın varsa ve ileride çeşitlenme belirtisi
yoksa, düz `new` daha okunaklıdır. "İleride belki gerekir" diye fabrika koymak aşırı
mühendisliktir (over-engineering) — bir katman soyutlama borcu ekler, karşılığında
hiçbir esneklik alamazsın. Kalıbı, ikinci somut tip gerçekten belirdiğinde ekle.

Aşağıdaki diyagram istemcinin somut sınıfları hiç tanımadan yalnızca arayüz + fabrika
üzerinden çalıştığını gösterir.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Factory kalıbı: istemci fabrika üzerinden ürün arayüzü alır, somut sınıfları bilmez">
  <defs>
    <marker id="dp-fok" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Factory (Fabrika): nesne yaratmayı tek yerde topla</text>
  <text x="320" y="43" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">İstemci somut sınıfı bilmez — yalnız arayüzü ve fabrikayı tanır</text>
  <rect x="16" y="106" width="104" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="68" y="138" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">İstemci</text>
  <rect x="156" y="106" width="122" height="52" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="217" y="130" text-anchor="middle" font-size="13" font-weight="700" fill="#C77A17" font-family="system-ui">Factory</text>
  <text x="217" y="147" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">olustur()</text>
  <rect x="314" y="106" width="126" height="52" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="377" y="128" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">«arayüz»</text>
  <text x="377" y="146" text-anchor="middle" font-size="13" font-weight="700" fill="#4F46E5" font-family="system-ui">Ürün</text>
  <rect x="480" y="60" width="144" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="552" y="88" text-anchor="middle" font-size="12" font-weight="600" fill="#25804C" font-family="system-ui">SomutÜrünA</text>
  <rect x="480" y="158" width="144" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="552" y="186" text-anchor="middle" font-size="12" font-weight="600" fill="#25804C" font-family="system-ui">SomutÜrünB</text>
  <line x1="120" y1="132" x2="152" y2="132" stroke="#186399" stroke-width="1.8" marker-end="url(#dp-fok)"/>
  <line x1="278" y1="132" x2="310" y2="132" stroke="#186399" stroke-width="1.8" marker-end="url(#dp-fok)"/>
  <line x1="440" y1="120" x2="480" y2="86" stroke="#25804C" stroke-width="1.4" stroke-dasharray="5,4"/>
  <line x1="440" y1="144" x2="480" y2="178" stroke="#25804C" stroke-width="1.4" stroke-dasharray="5,4"/>
  <text x="512" y="128" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">gerçekleştirir</text>
  <text x="320" y="232" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Yeni bir ürün tipi = yalnız fabrikaya tek satır; istemci hiç değişmez.</text>
</svg>
```

## 2. Strategy (Strateji) — davranışı çalışma anında değiştir

**Hangi problemi çözer?** Aynı işi yapmanın birden fazla yolu (algoritma) vardır ve
hangisinin kullanılacağı çalışma anında (runtime) belirlenir. Bunu dev bir `if/else`
ya da `switch` zincirine gömersen, her yeni yol o zinciri şişirir ve sınıf sürekli
değişir. Strategy her algoritmayı ortak bir arayüzün arkasında ayrı bir nesneye alır;
kullanan sınıf (Context) hangi nesneyi tuttuğuna göre farklı davranır.

**Yapı:** Context, bir Strategy arayüzüne bağlıdır; {StratejiA, StratejiB, StratejiC}
bu arayüzü gerçekleştirir. Context çalışma anında hangi stratejiyi tutacağını değiştirir.

```java
interface Indirim { double uygula(double fiyat); }
class Sepet {
    private Indirim strateji;                          // çalışma anında set edilir
    void stratejiSec(Indirim s) { this.strateji = s; }
    double ode(double fiyat) { return strateji.uygula(fiyat); }
}
```

**Ne zaman KULLANMA:** Yalnızca iki basit dal varsa ve değişme ihtimali düşükse, düz
bir `if/else` daha az dosya, daha az dolaylılıkla (indirection) aynı işi görür. Ayrıca
seçim aslında derleme anında (compile time) sabitse strateji nesnesi gereksizdir.
Kalıbı, dallar çoğalıp bağımsız test edilmesi/değiştirilmesi gereken gerçek
algoritmalara dönüştüğünde uygula.

Diyagram: tek Context, arayüz üzerinden birbirinin yerine geçebilen üç stratejiye bakar.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Strategy kalıbı: Context bir strateji arayüzüne bağlıdır, üç somut strateji bu arayüzü gerçekleştirir">
  <defs>
    <marker id="dp-sok" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Strategy (Strateji): davranışı çalışma anında değiştir</text>
  <text x="320" y="43" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">Aynı Context, farklı strateji nesnesiyle farklı davranır</text>
  <rect x="16" y="104" width="122" height="54" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="77" y="128" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Context</text>
  <text x="77" y="146" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">(Sepet)</text>
  <rect x="256" y="104" width="140" height="54" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="326" y="127" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">«arayüz»</text>
  <text x="326" y="145" text-anchor="middle" font-size="13" font-weight="700" fill="#4F46E5" font-family="system-ui">Indirim</text>
  <line x1="138" y1="131" x2="252" y2="131" stroke="#186399" stroke-width="1.8" marker-end="url(#dp-sok)"/>
  <text x="195" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kullanır</text>
  <rect x="470" y="44" width="154" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="547" y="71" text-anchor="middle" font-size="12" font-weight="600" fill="#25804C" font-family="system-ui">YuzdeIndirim</text>
  <rect x="470" y="109" width="154" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="547" y="136" text-anchor="middle" font-size="12" font-weight="600" fill="#25804C" font-family="system-ui">SabitIndirim</text>
  <rect x="470" y="174" width="154" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="547" y="201" text-anchor="middle" font-size="12" font-weight="600" fill="#25804C" font-family="system-ui">IndirimYok</text>
  <line x1="466" y1="66" x2="396" y2="120" stroke="#25804C" stroke-width="1.4" stroke-dasharray="5,4"/>
  <line x1="466" y1="131" x2="398" y2="131" stroke="#25804C" stroke-width="1.4" stroke-dasharray="5,4"/>
  <line x1="466" y1="196" x2="396" y2="142" stroke="#25804C" stroke-width="1.4" stroke-dasharray="5,4"/>
  <text x="320" y="238" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">if/else zinciri yerine: her algoritma bağımsız, ayrı test edilebilir bir nesne.</text>
</svg>
```

## 3. Proxy (Vekil) — araya kontrol / önbellek / erişim koy

**Hangi problemi çözer?** Bir nesneye erişimi doğrudan vermek istemediğin durumlar
vardır: pahalı olduğu için tembel yüklemek (lazy loading), sonucu önbelleğe almak
(cache), erişim iznini denetlemek, ya da çağrıları loglamak istersin. Proxy, gerçek
nesneyle **aynı arayüzü** uygulayan bir "vekil" nesnedir; istemci onu gerçeğinden
ayırt edemez ama vekil çağrıyı sarmalayıp araya davranış ekler.

**Yapı:** İstemci → Proxy → Gerçek Nesne. Proxy ile Gerçek Nesne aynı arayüzü paylaşır.

```java
class OnbellekliServis implements Servis {              // Servis: ortak arayüz
    private final Servis gercek;
    private final Map<String, Veri> cache = new HashMap<>();
    OnbellekliServis(Servis gercek) { this.gercek = gercek; }   // gerçeği dışarıdan alır
    public Veri getir(String k) { return cache.computeIfAbsent(k, gercek::getir); }
}
```

**Ne zaman KULLANMA:** Araya koyacağın gerçek bir sorumluluk (önbellek, erişim, tembel
yükleme) yoksa, proxy yalnızca çağrıyı olduğu gibi ileten boş bir katmandır — okumayı
ve hata ayıklamayı (debugging) zorlaştırır, hiçbir şey kazandırmaz. Ayrıca loglama, önbellek,
yetki gibi **çapraz kesen** (cross-cutting — tek bir sınıfa ait olmayıp uygulamanın her yerinde
tekrarlanan) ihtiyaçlar için çoğu zaman kullandığın çatının (framework — hazır iskelet
kütüphanesi) hazır çözümü, örneğin Spring'in `@Cacheable` işaretlemesi, elle proxy yazmaktan
daha temizdir.

Diyagram: istemci vekili çağırır, vekil gerektiğinde gerçeğe iletir.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Proxy kalıbı: istemci proxy'yi çağırır, proxy kontrol veya önbellek ekleyip gerçek nesneye iletir">
  <defs>
    <marker id="dp-pok" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Proxy (Vekil): araya kontrol / önbellek / erişim koy</text>
  <text x="320" y="43" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">İstemci farkı görmez — Proxy ve Gerçek Nesne aynı arayüzü paylaşır</text>
  <rect x="20" y="118" width="132" height="54" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="86" y="150" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">İstemci</text>
  <rect x="252" y="118" width="136" height="54" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="320" y="142" text-anchor="middle" font-size="13" font-weight="700" fill="#C77A17" font-family="system-ui">Proxy</text>
  <text x="320" y="159" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">(Vekil)</text>
  <rect x="488" y="118" width="136" height="54" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="556" y="150" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Gerçek Nesne</text>
  <line x1="152" y1="145" x2="248" y2="145" stroke="#186399" stroke-width="1.8" marker-end="url(#dp-pok)"/>
  <line x1="388" y1="145" x2="484" y2="145" stroke="#186399" stroke-width="1.8" marker-end="url(#dp-pok)"/>
  <rect x="222" y="58" width="196" height="34" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.2"/>
  <text x="320" y="80" text-anchor="middle" font-size="10.5" fill="#C77A17" font-family="system-ui">erişim · önbellek · loglama · tembel yükleme</text>
  <line x1="320" y1="92" x2="320" y2="116" stroke="#C77A17" stroke-width="1.4" stroke-dasharray="4,3"/>
  <text x="320" y="212" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Proxy çağrıyı sarmalar; gerekirse gerçeğe iletir, gerekmezse (önbellek) hiç iletmez.</text>
</svg>
```

## 4. Ne zaman hangisi? — karar rehberi

Kalıbı problem seçer, moda değil. Üç temel soru üç farklı kalıba götürür:

- Sorunun **nesne YARATIMI** mı (hangi somut sınıfı, koşula göre)? → **Factory**
- Sorunun **bir işi yapma YOLU** mu (aynı görev, değişen/seçmeli algoritma)? → **Strategy**
- Sorunun **bir nesneye ERİŞİM** mi (araya denetim/önbellek/sarmalama)? → **Proxy**

```svg
<svg viewBox="0 0 640 272" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Karar rehberi: yaratım sorunu Factory, algoritma sorunu Strategy, erişim sorunu Proxy'ye götürür">
  <defs>
    <marker id="dp-dok" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Ne zaman hangisi? — hızlı karar rehberi</text>
  <rect x="222" y="40" width="196" height="50" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="63" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Tasarım sorunun ne?</text>
  <text x="320" y="80" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">önce problemi adlandır</text>
  <line x1="248" y1="90" x2="110" y2="150" stroke="#5D7789" stroke-width="1.6" marker-end="url(#dp-dok)"/>
  <line x1="320" y1="90" x2="320" y2="150" stroke="#5D7789" stroke-width="1.6" marker-end="url(#dp-dok)"/>
  <line x1="392" y1="90" x2="530" y2="150" stroke="#5D7789" stroke-width="1.6" marker-end="url(#dp-dok)"/>
  <rect x="16" y="154" width="188" height="76" rx="11" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="110" y="180" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Nesne YARATIMI</text>
  <text x="110" y="195" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">koşullu / karmaşık</text>
  <text x="110" y="219" text-anchor="middle" font-size="14" font-weight="700" fill="#C77A17" font-family="system-ui">Factory</text>
  <rect x="226" y="154" width="188" height="76" rx="11" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="320" y="180" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ALGORİTMA seçmeli</text>
  <text x="320" y="195" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">çalışma anında değişir</text>
  <text x="320" y="219" text-anchor="middle" font-size="14" font-weight="700" fill="#4F46E5" font-family="system-ui">Strategy</text>
  <rect x="436" y="154" width="188" height="76" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="530" y="180" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ERİŞİMİ kontrol</text>
  <text x="530" y="195" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sarmala / önbelleğe al</text>
  <text x="530" y="219" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">Proxy</text>
  <text x="320" y="256" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Emin değilsen: en basit çözümle başla; kalıbı ihtiyaç kanıtlanınca ekle.</text>
</svg>
```

## 5. Karıştırılması kolay yakın kalıplar

Aşağıdaki üç çift, iskeleti birbirine çok benzediği için sürekli karıştırılır. Ayrım
neredeyse hiçbir zaman sınıf şemasında değil, **niyette**dir.

### Factory Method mu, Abstract Factory mi, basit fabrika mı?
- **Basit fabrika (simple factory):** Tek bir metot, bir koşula bakıp hangi somut ürünü
  yaratacağına karar verir — yukarıdaki `OdemeFabrikasi` örneği. GoF listesinde yer almaz,
  ama pratikte en sık kullanılan biçimdir.
- **Factory Method (Fabrika Metodu):** Üst sınıf "burada bir ürün yaratılacak" diyen bir metot
  tanımlar, o metodun gövdesini **alt sınıflara** bıraktırır. Ayırt edici işareti: **kalıtım**
  kullanır ve **tek bir ürün** üretir.
- **Abstract Factory (Soyut Fabrika):** Birbiriyle uyumlu olması gereken **bir ürün ailesini**
  birlikte üretir; örneğin `KoyuTemaFabrikasi` düğmeyi, menüyü ve kaydırma çubuğunu hep aynı
  temada verir. Ayırt edici işareti: **birden çok ilişkili ürün** ve fabrikanın kendisinin bir
  nesne olarak dolaştırılması.
- Tek cümlelik ayraç: kararı **alt sınıf** mı veriyor (Factory Method), yoksa birlikte
  kullanılacak bir **ürün grubu** mu üretiliyor (Abstract Factory)?

### Proxy mi, Decorator mı?
İkisi de gerçek nesneyle aynı arayüzü uygular ve çağrıyı sarmalar; şemaları neredeyse
aynıdır. Fark niyettedir:
- **Proxy (Vekil):** Aynı işi sunar ama erişimi **yönetir** — izin ver/verme, geciktir (tembel
  yükleme), önbellekten dön, uzaktaki nesneye ilet. Sarmaladığı nesneyi çoğunlukla kendisi
  yaratır ve tek katman kalır.
- **Decorator (Dekoratör / Süsleyici):** Erişimi kısıtlamaz, davranışı **zenginleştirir** —
  bir veri akışına önce sıkıştırma, sonra şifreleme ekle. Sarmalanacak nesne dışarıdan verilir
  ve dekoratörler **üst üste** takılabilir.
- Ayraç sorusu: "araya girip erişimi denetliyor mu" → Proxy; "aynı işi yapıp üstüne özellik
  katıyor ve katmanlar üst üste binebiliyor mu" → Decorator.

### Strategy mi, State mi?
Yapı yine aynıdır: bir bağlam nesnesi (context), bir arayüz ve değiştirilebilir gerçeklemeler.
Fark, **değişimi kimin tetiklediğidir**:
- **Strategy:** Hangi algoritmanın kullanılacağını **dışarıdaki istemci** seçer. Seçenekler
  birbirinden habersizdir; biri diğerine geçiş yaptırmaz.
- **State (Durum):** Nesnenin davranışı içinde bulunduğu duruma göre değişir ve durumlar arası
  geçişi **sistemin kendisi** yapar (`Yeni → Ödendi → Kargolandı`). Durum nesneleri bir sonraki
  duruma nasıl geçileceğini bilir.
- Ayraç sorusu: seçimi dışarıdan biri mi yapıyor (Strategy), yoksa nesnenin kendi yaşam döngüsü
  mü sürüklüyor (State)?

## Öz-sınav (kendini test et)

Her senaryoda hangi kalıp (veya "kalıp gerekmez") uygun? Önce kendin cevapla, sonra
gerekçesini bir cümleyle yaz.

1. Bir rapor dışa aktarma özelliğinde PDF, Excel ve CSV için dışa aktarma işleminin kendisi
   baştan sona farklı çalışıyor. Çağıran kod her durumda yalnızca `disaAktar()` diyecek;
   hangi biçimin kullanılacağını kullanıcı çalışma anında seçiyor. Hangi kalıp?
2. Uzak bir API'den dönen veriyi ilk çağrıda çekip sonraki çağrılarda bellekten vermek
   istiyorsun; çağıran kodun bu önbellekten haberi olmasın. Hangi kalıp?
3. Girdideki dosya uzantısına göre uygun `Ayrıştırıcı` (parser) nesnesini üretmek için
   kodun beş ayrı yerinde `if/else new ...` bloğu var. Hangi kalıp bu tekrarı toplar?
4. Bir veri akışına önce sıkıştırma, sonra şifreleme eklemek istiyorsun; arayüz aynı kalacak
   ve iki katman istenirse üst üste takılabilecek. Bu Proxy mi Decorator mı, neden?
5. Projede tek bir `Fatura` sınıfı var, başka bir türevi yok ve öngörülebilir gelecekte
   olmayacak. Onu üretmek için bir `FaturaFabrikasi` yazmalı mısın?
6. Bir siparişin durumu `Yeni → Ödendi → Kargolandı` diye ilerliyor; her durumda `iptalEt()`
   farklı davranıyor ve bir sonraki duruma geçişi siparişin kendisi yapıyor. Strategy mi
   State mi?

*İpucu (cevap değil): Her senaryoda tek soru sor — problem bir nesnenin YARATIMI mı, bir işin
YAPILIŞ YOLU (aynı görev, değişen/seçmeli algoritma) mı, yoksa bir nesneye ERİŞİMİN denetimi
(araya önbellek/yetki/log) mi? Cevabını yukarıdaki "Karar rehberi" diyagramının üç ekseniyle
eşleştir; iki kalıp arasında kaldığında 5. bölümdeki niyet ayraçlarına bak.*
