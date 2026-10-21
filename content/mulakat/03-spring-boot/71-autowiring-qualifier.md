---
slug: autowiring-qualifier
baslik: "Autowiring: @Autowired Çözümü, @Qualifier/@Primary, Koleksiyon Enjeksiyonu ve Döngüsel Bağımlılık"
kademe: Orta
sira: 71
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Hastanede "kan tahlili lazım" dersin; kimin yapacağını sen seçmezsin, danışma uygun kişiye yönlendirir. Ama iki laborant varsa ve hiçbir ayırt edici bilgi vermediysen danışma kimi göndereceğini bilemez ve sorar. Spring'in bağımlılık bulma işi de böyledir: sen "bana şu **tipte** bir nesne lazım" dersin, container adayları arar. Tek aday varsa takar; çok aday varsa netlik ister; hiç aday yoksa uygulamayı başlatmaz. Bu bölüm, "kim seçilecek?" sorusunun kurallarını anlatır.

### Temel kavramlar

- **Bean**: container'ın kurup yönettiği nesne.
- **Autowiring (otomatik bağlama)**: container'ın enjeksiyon noktasına uygun bean'i kendiliğinden bulup takması.
- **Enjeksiyon noktası**: bağımlılığın verildiği yer — yapıcı parametresi, setter parametresi ya da alan.
- **Tipe göre çözüm (by type)**: aday arama ölçütünün alanın/parametrenin **tipi** olması.
- **`@Primary`**: bir adayı "eşitler arasında birinci" ilan eder; qualifier verilmediğinde varsayılan seçilir.
- **`@Qualifier("ad")`**: enjeksiyon noktasında **tam olarak hangi** bean'i istediğini adla söylemek.
- **`NoUniqueBeanDefinitionException`**: aynı tipte birden çok aday varken ayırt edici bilgi yoksa **başlangıçta** atılan hata.
- **Fail-fast**: hatayı prod'da çalışırken değil, uygulama ayağa kalkarken göstermek.

### Nasıl çalışır?

`@Autowired`ın birincil stratejisi **tiptir**. O tipte tek aday varsa doğrudan enjekte edilir. Birden çok aday varsa Spring şu sırayla ilerler: önce enjeksiyon noktasındaki `@Qualifier` adayları **daraltır**, sonra kalanlar arasında `@Primary` tercih edilir, ardından `@Priority` bakılır, son çare olarak alanın/parametrenin **adı** bir bean adıyla eşleşiyor mu. Hiçbiri çözmezse `NoUniqueBeanDefinitionException` fırlar.

Buradan çıkan sonuçlar önemlidir. Ad **ikincil bir ipucudur**, birincil değil. Spring alfabetik sıralamaya bakmaz. İki bean'i aynı noktaya birlikte enjekte etmez. Belirsizliği sessizce `null` bırakarak da geçiştirmez. Çalışma anında sırayla dönüşümlü (round-robin) seçim de yapmaz.

Enjeksiyon **bean kurulurken** olur. Bunu ilk kullanıma ertelemek ancak `@Lazy` (tembel yükleme) ya da `ObjectProvider` ile mümkündür.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tek tip birden fazla aday ve çözüm">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı tipten iki aday</text>
  <rect x="40" y="46" width="180" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="130" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">KrediKarti (Odeme)</text>
  <text x="130" y="88" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">@Primary</text>
  <rect x="40" y="112" width="180" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="130" y="136" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Havale (Odeme)</text>
  <text x="130" y="154" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">@Qualifier("havale")</text>
  <text x="300" y="92" font-size="18" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="330" y="60" width="280" height="90" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="470" y="86" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">@Autowired Odeme o;  → KrediKarti</text>
  <text x="470" y="108" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">@Qualifier("havale") → Havale</text>
  <text x="470" y="132" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">qualifier @Primary'yi ezer</text>
</svg>
```

`@Primary` **geneldir** ("bu tipte qualifier belirtilmezse beni seç"), `@Qualifier` **nokta-özeldir** ("burada tam olarak şunu istiyorum"). İkisi birlikte gayet kullanılır, çakışma hatası doğurmaz; açık niyet olduğu için **`@Qualifier`, `@Primary`'yi ezer**. Her ikisi de anotasyon tabanlı yapılandırmada birinci sınıf çalışır, XML'e özgü değildir. Pratik kural: yaygın durum için `@Primary`, istisnalar için nokta-özel `@Qualifier`.

Koleksiyon enjeksiyonu güçlü bir desendir:

```java
@Service
class OdemeYonlendirici {
    private final List<Odeme> yontemler;          // Odeme tipindeki TÜM bean'ler
    private final Map<String, Odeme> adaGore;     // bean adı → bean
    OdemeYonlendirici(List<Odeme> yontemler, Map<String, Odeme> adaGore) {
        this.yontemler = yontemler;
        this.adaGore = adaGore;
    }
}
```

Spring `List<Odeme>` (ya da `Set<Odeme>`, `Odeme[]`) gördüğünde bunu "**o tipteki tüm bean'leri topla**" isteği olarak yorumlar. Belirsizlik hatası vermez, listeyi boş bırakmaz ve `@Primary` ile filtrelemez; `@Primary` yalnız tekil enjeksiyonu etkiler.

Sıra gerekiyorsa bean'lere `@Order` (veya `Ordered` arayüzü) verilir. `Map<String, Odeme>` ise **bean adı → bean** eşlemesi verir. Bu yapı, eklenti (plugin) ve strateji mimarilerinin temelidir: yeni bir `Odeme` implementasyonu eklemek, hiçbir yönlendirme kodunu değiştirmeden onu listeye dahil eder.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="List ve Map koleksiyon enjeksiyonu">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı tipin tüm bean'lerini topla</text>
  <rect x="28" y="48" width="150" height="40" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="103" y="72" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">KrediKarti (Odeme)</text>
  <rect x="28" y="96" width="150" height="40" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="103" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Havale (Odeme)</text>
  <rect x="28" y="144" width="150" height="40" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="103" y="168" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Kripto (Odeme)</text>
  <text x="212" y="120" font-size="22" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="250" y="52" width="366" height="60" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="433" y="76" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">List&lt;Odeme&gt; → [hepsi]</text>
  <text x="433" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">@Order ile sıralı · yeni bean otomatik katılır</text>
  <rect x="250" y="122" width="366" height="60" rx="11" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.6"/>
  <text x="433" y="146" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">Map&lt;String,Odeme&gt; → {ad: bean}</text>
  <text x="433" y="168" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bean adı → bean eşlemesi</text>
</svg>
```

Bir bağımlılık **opsiyonel** de olabilir; ilgili bean hiç tanımlı değilse uygulama patlamadan başlamalıdır. Bunun üç yerleşik yolu var:

1. **`ObjectProvider<T>`** — en esneği. `getIfAvailable()` bean yoksa `null` döner, `getIfUnique()` birden çok aday varsa `null` döner. Ayrıca bean'i kullanım anında arar (lazy lookup — geç arama).
2. **`Optional<T>`** — bean yoksa `Optional.empty()` enjekte edilir.
3. **`@Autowired(required = false)`** — alan `null` bırakılır.

Yanlış yollar da var. Container başlatmayı `try/catch`'e sarmak kırılgandır ve kimse böyle yazmaz. `@Qualifier` yalnız var olan adaylar arasından seçer, bean'in hiç olmamasını tolere etmez. `@Primary` de sadece belirsizliği çözer. Modern kodda `ObjectProvider` öne çıkar, çünkü hem opsiyonelliği hem geç aramayı birlikte verir.

Son konu **döngüsel bağımlılıktır** (circular dependency): A kurulmak için B'yi, B de A'yı yapıcıda isterse hiçbiri diğerinden önce var olamaz — döngü **kurulamaz**.

```svg
<svg viewBox="0 0 640 208" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Döngüsel bağımlılık kilitlenmesi ve çözümü">
  <defs>
    <marker id="autowiring-arrow" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#8A2E2E"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">A → B → A kilitlenmesi</text>
  <rect x="60" y="52" width="110" height="48" rx="10" fill="#F3EEF9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="115" y="81" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Bean A</text>
  <rect x="60" y="132" width="110" height="48" rx="10" fill="#F3EEF9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="115" y="161" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Bean B</text>
  <path d="M108,100 L108,132" stroke="#8A2E2E" stroke-width="1.6" fill="none" marker-end="url(#autowiring-arrow)"/>
  <path d="M122,132 L122,100" stroke="#8A2E2E" stroke-width="1.6" fill="none" marker-end="url(#autowiring-arrow)"/>
  <text x="188" y="118" font-size="10.5" fill="#8A2E2E" font-family="system-ui">ikisi de önce diğerini ister</text>
  <text x="188" y="136" font-size="10.5" fill="#5D7789" font-family="system-ui">→ hiçbiri kurulamaz (fail-fast)</text>
  <rect x="330" y="48" width="286" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="473" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Çözüm sırası</text>
  <text x="473" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">1 · sorumluluğu 3. bean'e ayır</text>
  <text x="473" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">2 · bağı event ile gevşet</text>
  <text x="473" y="142" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">son çare · @Lazy (kokuyu maskeler)</text>
  <text x="473" y="168" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">ayarı açmak = yanlış çözüm</text>
</svg>
```

Spring Boot **2.6'dan beri** döngüleri **varsayılan olarak yasaklar** (`spring.main.allow-circular-references=false`) ve başlangıçta net bir hata verir. Bu bilinçli bir karardır: döngü genelde **sorumluluk sınırı sorununun** işaretidir.

Doğru çözüm ayarı `true` yapmak değil, **tasarımı düzeltmektir**. İki yolu var: ortak sorumluluğu üçüncü bir bean'e çıkarmak ya da bağı bir olay (event) üzerinden gevşetmek. Son çare `@Lazy` ile bir tarafı vekil nesne (proxy) üzerinden geç bağlamaktır; bu kötü kokuyu maskeler, derdi çözmez.

Şunlar da olmaz: Spring döngüyü sessizce erken referansla çözmez ve `null` enjekte etmez. Yapıcı üzerinden kurulan döngü "kendiliğinden kırılmaz". Döngüyü kırabilecek olan alan/setter enjeksiyonudur, o da zaten kötü bir çözümdür.

### Mülakatta ne sorulur?

- **`@Autowired` neye göre çözer?** → önce tipe; çok aday varsa `@Primary` → `@Qualifier` → ad eşleşmesi; çözülemezse `NoUniqueBeanDefinitionException` (fail-fast).
- **`@Primary` mi `@Qualifier` mı kazanır?** → enjeksiyon noktasındaki `@Qualifier`.
- **`List<T>` enjeksiyonu** → o tipteki tüm bean'ler, `@Order` ile sıralı; `Map<String,T>` ad→bean; belirsizlik değildir.
- **Opsiyonel bağımlılık** → `ObjectProvider.getIfAvailable()`, `Optional<T>`, `@Autowired(required = false)`.
- **Döngüsel bağımlılık** → Boot 2.6+ varsayılan yasak; çözüm tasarım, son çare `@Lazy`, ayarı açmak yanlış cevap.
- **Enjeksiyon ne zaman olur?** → bean kurulurken; ertelenmesi `@Lazy`/`ObjectProvider` ile.

### Tek cümlede

`@Autowired` bağımlılığı tipe göre çözer; belirsizliği `@Primary` (genel varsayılan) ve onu ezen `@Qualifier` (nokta-özel seçim) giderir, koleksiyon enjeksiyonu tüm adayları toplar, döngü ise Boot 2.6+'da baştan reddedilir.

# aut-1 | Temel
## Soru
`@Autowired` bir bağımlılığı varsayılan olarak nasıl çözer?
## A
Bean adına göre (by name) çözer; alan adıyla eşleşen bean seçilir, tipe yalnız doğrulama için bakılır
🤖 Varsayılan çözüm tipe göredir; ad ancak belirsizlikte devreye girer.
## B
Aynı tipte birden fazla aday varsa bean adlarını alfabetik sıralayıp ilkini seçer
🤖 Alfabetik sıra kullanılmaz; belirsizliği ad/qualifier ipuçları çözer, çözemezse hata verir.
## C
Bağımlılığı yalnız ilk kullanım anında çözer; container başlarken hiç arama yapmaz
🤖 Enjeksiyon bean kurulurken yapılır; ertelenmiş çözüm ancak `@Lazy`/`ObjectProvider` ile olur.
## D
Tipe göre (by type): o tipte tek aday varsa onu enjekte eder
🤖 Doğru: önce tip eşleşmesi; çok aday varsa qualifier/ad devreye girer.
## Doğru
D
## Açıklama
`@Autowired`'ın birincil çözüm stratejisi **tipe göredir** (by type): Spring, enjekte edilecek alanın/parametrenin tipiyle uyumlu bean'leri arar. O tipte **tek** aday varsa doğrudan enjekte eder. **Birden fazla** aday varsa tip tek başına yetmez; Spring `@Primary`, `@Qualifier` veya son çare olarak **alanın/parametrenin adını** bean adıyla eşleştirmeye bakar — hiçbiri çözmezse `NoUniqueBeanDefinitionException` fırlatır. Yani ad (A) ikincil bir ipucudur, birincil değil; alfabetik sıra (B) kullanılmaz ve çözüm ilk kullanıma ertelenmez (C) — bağımlılık, bean kurulurken çözülür.

# aut-2 | Temel
## Soru
Aynı arayüzü uygulayan iki bean var ve bir servise bu arayüzü `@Autowired` ile enjekte ettin, qualifier vermedin. Ne olur?
## A
Spring iki adayı birleştirip ikisini de aynı noktaya enjekte eder, çağrıları sırayla dağıtır
🤖 Tek bir bağımlılık noktasına iki nesne enjekte edilmez; belirsizlik hatası olur.
## B
Başlangıçta `NoUniqueBeanDefinitionException` fırlar
🤖 Doğru: iki aday + qualifier yok = belirsizlik; @Primary/@Qualifier çözer.
## C
Uygulama sessizce çalışır; belirsizlik çözülemediği için bağımlılık `null` bırakılır
🤖 `null` bırakmaz; belirsizliği hata olarak bildirir (fail-fast).
## D
Spring çalışma zamanında her çağrıda sırayla farklı bir adayı kullanır (round-robin)
🤖 Çalışma zamanında dönüşümlü seçim yapmaz; başlangıçta belirsizlikten patlar.
## Doğru
B
## Açıklama
Tipe göre çözümde **iki aday** olup hiçbir ayırt edici ipucu (qualifier/primary/ad eşleşmesi) yoksa Spring hangisini enjekte edeceğini bilemez ve **başlangıçta** `NoUniqueBeanDefinitionException` ile başarısız olur (fail-fast — sorunu prod'da değil ayağa kalkarken gösterir). İki çözüm vardır: adaylardan birini `@Primary` işaretleyip "varsayılan bu" demek, ya da enjeksiyon noktasında `@Qualifier("ad")` ile **tam olarak hangisini** istediğini belirtmek. Spring iki bean'i aynı noktaya enjekte etmez (A), sessizce `null` bırakmaz (C) ve runtime'da dönüşümlü seçim yapmaz (D).

# aut-3 | Orta
## Soru
`@Primary` ile `@Qualifier` bir arada kullanıldığında öncelik nasıl işler?
## A
`@Primary` her zaman kazanır; `@Qualifier` yalnız ortada `@Primary` işaretli bir bean yoksa dikkate alınır
🤖 Ters: enjeksiyon noktasındaki `@Qualifier` `@Primary`'yi ezer.
## B
İkisi aynı uygulamada birlikte kullanılırsa Spring başlangıçta çakışma hatası verir
🤖 Birlikte kullanılabilirler; çakışma değil, açık bir öncelik kuralı vardır.
## C
Enjeksiyon noktasındaki `@Qualifier`, `@Primary`'yi ezer
🤖 Doğru: nokta-özel qualifier, genel varsayılan primary'yi geçersiz kılar.
## D
İkisi de yalnız XML yapılandırmada geçerlidir; anotasyon tabanlı yapılandırmada yok sayılırlar
🤖 İkisi de anotasyon tabanlı yapılandırmada birinci sınıf çalışır.
## Doğru
C
## Açıklama
`@Primary` **genel** bir varsayılandır: "bu tipte qualifier belirtilmezse beni seç" der. `@Qualifier("ad")` ise **belirli bir enjeksiyon noktasında** "tam olarak şu bean'i istiyorum" der — nokta-özel ve açık niyet olduğu için **`@Primary`'yi ezer**. Yani genelde `@Primary` işaretli bean gelir, ama bir noktada `@Qualifier` yazarsan orada onu değil, adı verdiğin bean'i alırsın. İkisi birlikte gayet kullanılır (B yanlış), önceliği `@Primary` değil qualifier alır (A ters), ve her ikisi de anotasyon yapılandırmasında çalışır (D yanlış). Pratik kural: yaygın durum için `@Primary`, istisnalar için nokta-özel `@Qualifier`.

# aut-4 | Orta
## Soru
Aşağıdaki enjeksiyon `Odeme` arayüzünü uygulayan 3 bean varken ne yapar?

```java
@Service
class OdemeYonlendirici {
    private final List<Odeme> yontemler;
    OdemeYonlendirici(List<Odeme> yontemler) { this.yontemler = yontemler; }
}
```
## A
`Odeme` tipindeki tüm bean'leri listeye toplayıp enjekte eder
🤖 Doğru: koleksiyon enjeksiyonu o tipteki tüm bean'leri toplar.
## B
Üç aday bulunduğu için belirsizlik doğar ve `NoUniqueBeanDefinitionException` fırlatılır
🤖 `List<Odeme>` istemek belirsizlik değildir; hepsini toplaması beklenen davranıştır.
## C
Yalnız `@Primary` işaretli bean'i tek elemanlı bir listeye koyar; diğer adaylar elenir
🤖 Liste hepsini alır; `@Primary` tekil enjeksiyonu etkiler, koleksiyonu değil.
## D
Boş liste enjekte eder; `List<Odeme>` tipinde tanımlı bir bean olmadığı için eşleşme bulunmaz
🤖 Spring `List<Odeme>`'yi tanır ve eşleşen bean'lerle doldurur; boş kalmaz.
## Doğru
A
## Açıklama
Spring, `List<Odeme>` (ya da `Set<Odeme>`, `Odeme[]`) tipindeki bir bağımlılığı gördüğünde bunu "**o tipteki tüm bean'leri topla**" isteği olarak yorumlar ve eşleşen tüm bean'leri koleksiyona koyar. Sıralama gerekiyorsa bean'lere `@Order` (veya `Ordered` arayüzü) verilir. Bu, **strateji/plugin** mimarilerinin temelidir: yeni bir `Odeme` implementasyonu eklemek, hiçbir yönlendirme kodunu değiştirmeden onu listeye dahil eder. Benzer şekilde `Map<String, Odeme>` **bean adı → bean** eşlemesi verir. Bu yüzden burada belirsizlik (B) yoktur, `@Primary` filtreleme yapmaz (C) ve liste boş kalmaz (D).

# aut-5 | İleri
## Soru
Bir bağımlılık **opsiyonel** olsun; ilgili bean tanımlıysa kullanılsın, değilse `null`/eksik olsun ama uygulama yine de başlasın istiyorsun. En temiz yol hangisidir?
## A
Bean'i normal `@Autowired` ile enjekte edip container başlatmasını `try/catch` içine almak
🤖 Container başlatmayı try/catch'e sarmak kırılgan ve idiomatik değildir; çerçeve zaten çözüm sunar.
## B
Bağımlılığı `@Qualifier` ile adlandırıp bean yoksa Spring'in o noktayı atlamasını sağlamak
🤖 `@Qualifier` yalnız adaylar arasından seçer; bean hiç yoksa enjeksiyon yine başarısız olur.
## C
`ObjectProvider<T>` enjekte edip `getIfAvailable()` ile almak
🤖 Doğru: bunlar opsiyonel bağımlılığın idiomatik yollarıdır.
## D
Bean'i `@Primary` ile işaretleyip yokluğunda varsayılan davranışa düşmesini sağlamak
🤖 `@Primary` belirsizlik çözer, opsiyonelliği sağlamaz; bean yoksa yine hata olur.
## Doğru
C
## Açıklama
Opsiyonel bağımlılık için Spring'in üç idiomatik aracı vardır: (1) **`ObjectProvider<T>`** — en esneği; `getIfAvailable()` bean yoksa `null`, `getIfUnique()` çok aday varsa `null` döner, ayrıca lazy lookup sağlar; (2) **`Optional<T>`** — bean yoksa `Optional.empty()` enjekte edilir; (3) **`@Autowired(required = false)`** — bean yoksa alan `null` bırakılır. Üçü de bean yokluğunda başlangıcı **patlatmaz**. Container başlatmayı `try/catch`'e sarmak (A) kırılgandır ve doğru yaklaşım değildir; `@Qualifier` (B) yalnız var olan adaylar arasından seçim yapar, yokluğu tolere etmez; `@Primary` (D) de yalnız belirsizlik çözer. Modern kodda `ObjectProvider` tercih edilir çünkü hem opsiyonellik hem de tembel çözüm sunar.

# aut-6 | İleri
## Soru
İki bean birbirine **constructor injection** ile bağımlı (A→B, B→A). Spring Boot 2.6+ varsayılan davranışında ne olur ve doğru çözüm nedir?
## A
Spring döngüyü erken referans (early reference) vererek otomatik ve sessizce çözer, hiçbir uyarı vermez
🤖 Constructor döngüsünü sessizce çözemez; Boot 2.6+ zaten varsayılan olarak yasaklar.
## B
Başlangıçta hata verir; doğru çözüm ayarı açmak değil, tasarımı düzeltmektir
🤖 Doğru: fail-fast; kalıcı çözüm tasarım, geçici çare @Lazy.
## C
Uygulama sorunsuz başlar; döngüyü kırmak için Spring her iki bean'e de `null` enjekte eder
🤖 `null` enjekte etmez; döngüyü hata olarak bildirir.
## D
Döngü yalnız field injection'da oluşur; constructor injection döngüyü her zaman otomatik kırar
🤖 Tersine, constructor döngüsü kurulamaz; kırması gereken field/setter'dır ama o da kötü çözümdür.
## Doğru
B
## Açıklama
Constructor injection'da A, kurulmak için B'yi; B de A'yı ister — hiçbiri diğerinden önce var olamaz, dolayısıyla döngü **kurulamaz**. Spring Boot **2.6'dan beri** döngüleri **varsayılan olarak yasaklar** (`spring.main.allow-circular-references=false`) ve başlangıçta net bir hata verir (fail-fast) — bu bilinçli bir tasarım kararıdır, çünkü döngü genelde **sorumluluk sınırı sorununun** işaretidir. Doğru çözüm ayarı `true` yapmak değil, **tasarımı düzeltmektir**: ortak sorumluluğu üçüncü bir bean'e çıkarmak, ya da bağı bir olay/event üzerinden gevşetmek. Son çare `@Lazy` ile bir tarafı proxy üzerinden geç bağlamaktır (kod kokusunu maskeler ama derdi çözmez). Spring döngüyü sessizce çözmez (A), `null` enjekte etmez (C); constructor döngüsü de "otomatik kırılmaz" (D).
