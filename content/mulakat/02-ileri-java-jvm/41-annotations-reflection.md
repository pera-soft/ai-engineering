---
slug: annotations-reflection
baslik: "Anotasyonlar ve Reflection: Meta-veri ve Runtime İşleme"
kademe: İleri
sira: 41
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir bavula "kırılacak eşya" etiketi yapıştırdığını düşün. Etiket bavulu korumaz; hiçbir şey yapmaz. Ama yükleme görevlisi onu **okur** ve davranışını ona göre değiştirir: bavulu üste, dikkatli koyar. Etiket bilgi verir, işi yapan okuyandır. Java'da **anotasyon** bu etikettir; **reflection** ise etiketleri okuyup ona göre davranan görevlidir. Spring, JPA/Hibernate ve Jackson gibi kütüphanelerin "sihirli" görünen davranışının tamamı bu iki basit parçadan çıkar.

### Temel kavramlar

- **Meta-veri (metadata):** veri hakkında veri. Kodun kendisi değil, kod hakkında söylenen bir şey.
- **Anotasyon (annotation):** program öğelerine (sınıf, metot, alan, parametre) iliştirilen meta-veri: `@Override`, `@Deprecated`, `@Entity`, `@Autowired`. **Kendi başına hiçbir davranış üretmez.** Sıradan bir yorum satırı da değildir — yorumu derleyici atar, anotasyonu görür ve işleyebilir. `final` gibi bir değişmezlik anahtarı da değildir.
- **İşleyici (processor):** anotasyonu okuyup anlam yükleyen taraf: derleyicinin kendisi, bir **annotation processor** (derleme zamanı aracı, `javax.annotation.processing`) ya da reflection kullanan bir framework. `@Entity` bir sınıfı sihirle veritabanına bağlamaz; bir işleyici o etiketi okuduğunda anlam kazanır.
- **Meta-anotasyon:** anotasyonun kendisine takılan anotasyon; "bu etiket nasıl davransın?" sorusunu yanıtlar.
- **Yansıma (reflection):** çalışma zamanında sınıfların, metotların, alanların ve anotasyonların yapısını inceleyip onlarla etkileşme (metot çağırma, alan okuma) API'si: `Class`, `Method`, `Field`.

Dört meta-anotasyonu ayırt etmek gerekir:

- **`@Retention`** — anotasyonun ne kadar "yaşayacağını" söyler: `SOURCE` (yalnız derlemede, bytecode'a bile yazılmaz — ör. `@Override`), `CLASS` (bytecode'da var ama JVM'e yüklenmez — **varsayılan**), `RUNTIME` (JVM'e yüklenir, reflection okuyabilir).
- **`@Target`** — anotasyonun **nereye konabileceğini** kısıtlar: `TYPE`, `METHOD`, `FIELD`, `PARAMETER`... Birden çok hedef verilebilir: `@Target({ElementType.FIELD, ElementType.METHOD})`.
- **`@Documented`** — yalnız Javadoc'ta görünmeyi etkiler.
- **`@Inherited`** — anotasyonun alt sınıflara miras kalmasını sağlar.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="retention seviyeleri ve reflection ile okunabilirlik">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">@Retention: anotasyon nereye kadar yaşar?</text>
  <rect x="30" y="48" width="185" height="140" rx="12" fill="#EEF0F2" stroke="#8A97A3" stroke-width="1.5"/>
  <text x="122" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#5D7789" font-family="system-ui">SOURCE</text>
  <text x="122" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yalnız derlemede</text>
  <text x="122" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bytecode'da YOK</text>
  <text x="122" y="150" text-anchor="middle" font-size="10.5" fill="#B4322B" font-family="system-ui">reflection: göremez</text>
  <rect x="227" y="48" width="185" height="140" rx="12" fill="#FBF3E6" stroke="#B5852A" stroke-width="1.5"/>
  <text x="319" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#B5852A" font-family="system-ui">CLASS (varsayılan)</text>
  <text x="319" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bytecode'da VAR</text>
  <text x="319" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">runtime'da yüklenmez</text>
  <text x="319" y="150" text-anchor="middle" font-size="10.5" fill="#B4322B" font-family="system-ui">reflection: göremez</text>
  <rect x="424" y="48" width="186" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="517" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">RUNTIME</text>
  <text x="517" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">JVM'e yüklenir</text>
  <text x="517" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">framework'ler okur</text>
  <text x="517" y="150" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">reflection: GÖRÜR</text>
  <text x="320" y="220" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Spring/JPA anotasyonları RUNTIME olmalı ki reflection okuyabilsin.</text>
</svg>
```

### Nasıl çalışır?

Kendi anotasyonunu tanımlamak `@interface` ile olur. Reflection'ın onu **görebilmesi** için retention'ı açıkça `RUNTIME` yapman gerekir; belirtmezsen varsayılan `CLASS` olur ve anotasyon çalışma zamanında **görünmez**. Bu, klasik bir hatadır.

```java
@Retention(RetentionPolicy.RUNTIME)   // reflection görebilsin diye
@Target(ElementType.METHOD)           // yalnız metotlara
@interface Zamanla {}                 // özel anotasyon

// okuma:
Method m = tip.getMethod("islem");
if (m.isAnnotationPresent(Zamanla.class)) { /* ... */ }
```

`@Target(ElementType.METHOD)` demek: "bu anotasyon **yalnız** metotlara konabilir." Bir alana ya da sınıfa koymaya kalkarsan **derleme hatası** alırsın. `@Target` kullanımı genişletmez, **kısıtlar**; alan değerini değiştirmez (o bir işleyicinin işidir) ve retention'ı belirlemez (o `@Retention`'ın işidir). Hedef hiç belirtilmezse anotasyon çoğu bağlama konabilir; `@Target` bu serbestliği daraltarak API sözleşmesini netleştirir.

Okuma tarafı hep aynı zinciri izler: nesneden `Class`, `Class`'tan `Method`/`Field`, sonra `isAnnotationPresent(Zamanla.class)` ile varlık sorgusu ya da `getAnnotation(Zamanla.class)` ile anotasyon örneği (ve içindeki değerler). Kaynak kodu metin olarak aramak gerekmez — zaten çalışma zamanında kaynak kod yoktur. Annotation processor da bu iş için değildir: o bir **derleme zamanı** aracıdır.

```svg
<svg viewBox="0 0 640 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="reflection ile RUNTIME anotasyon okuma akışı">
  <defs>
    <marker id="annref-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Reflection ile RUNTIME anotasyon okuma akışı</text>
  <rect x="20" y="54" width="130" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="85" y="82" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Class tip</text>
  <text x="85" y="102" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">obj.getClass()</text>
  <rect x="180" y="54" width="130" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="245" y="82" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Method m</text>
  <text x="245" y="102" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">getMethod(...)</text>
  <rect x="340" y="54" width="150" height="60" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="415" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">isAnnotationPresent</text>
  <text x="415" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">/ getAnnotation</text>
  <rect x="520" y="54" width="100" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="570" y="82" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">davranış</text>
  <text x="570" y="102" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">üretilir</text>
  <line x1="150" y1="84" x2="178" y2="84" stroke="#5D7789" stroke-width="1.5" marker-end="url(#annref-ok)"/>
  <line x1="310" y1="84" x2="338" y2="84" stroke="#5D7789" stroke-width="1.5" marker-end="url(#annref-ok)"/>
  <line x1="490" y1="84" x2="518" y2="84" stroke="#5D7789" stroke-width="1.5" marker-end="url(#annref-ok)"/>
  <text x="320" y="150" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Spring/JPA tam olarak bu zinciri kullanır (yalnız RUNTIME görünür)</text>
</svg>
```

Spring ve JPA/Hibernate'in çekirdek deseni **bildirimsel (declarative) konfigürasyondur**. Geliştirici sınıflarını `@Component`, `@Service`, `@Entity`, `@Column`, `@Autowired` gibi RUNTIME anotasyonlarla etiketler. Framework ise başlarken sınıfları **tarar (classpath scanning)** ve reflection ile bu etiketleri okuyup davranış üretir. Şunları yapar: framework'ün yönettiği nesneleri (bean) kaydeder, bağımlılıkları enjekte eder (dependency injection), varlıkları (entity) tablolara eşler (O/R mapping), metotları HTTP uç noktalarına bağlar. Anotasyon "ne istediğini" bildirir, framework "nasıl yapılacağını" runtime'da halleder. Bu sayede konfigürasyon XML yığınları yerine kısa etiketlerle yapılır.

Reflection'ın bedeli üç başlıkta toplanır. (1) **Performans**: `Method.invoke` gibi reflektif çağrılar doğrudan çağrıdan yavaştır; JIT bunları aynı ölçüde iyileştiremez. (2) **Tip güvenliği kaybı**: yanlış metot adı ya da yanlış argüman tipi derlemede değil, ancak **çalışma zamanında** patlar. (3) **Kapsülleme (encapsulation) ihlali**: `setAccessible(true)` ile `private` üyelere erişilir, sınıfın gizli durumu delinir. Maliyet yalnız `Class`/`Method` nesnelerinin belleği değildir; ayrıca reflektif çağrılar JVM tarafından senkronize **edilmez**, yani hiçbir thread-safety kazancı yoktur.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="reflection kullanımının üç maliyeti">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Reflection'ın üç maliyeti</text>
  <rect x="24" y="46" width="192" height="130" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="120" y="74" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Performans</text>
  <text x="120" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">invoke, doğrudan</text>
  <text x="120" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çağrıdan yavaş</text>
  <text x="120" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">JIT az optimize eder</text>
  <rect x="224" y="46" width="192" height="130" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="320" y="74" text-anchor="middle" font-size="12.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Tip güvenliği</text>
  <text x="320" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">derleyici denetlemez</text>
  <text x="320" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hatalar runtime'a</text>
  <text x="320" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kayar</text>
  <rect x="424" y="46" width="192" height="130" rx="12" fill="#F3E4E4" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="520" y="74" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Kapsülleme</text>
  <text x="520" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">setAccessible(true)</text>
  <text x="520" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">private'i deler</text>
  <text x="520" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">modül sistemi kısıtlar</text>
</svg>
```

Java 16'dan itibaren **güçlü kapsülleme (strong encapsulation)** varsayılandır: JDK'nın iç paketlerine (`sun.*`, `jdk.internal.*`) reflection ile erişim artık uyarıyla geçiştirilmez, **`InaccessibleObjectException`** ile reddedilir. Modül sisteminde bir paketi dışarıya reflection'a açmak için modül tanımında **`opens`** bildirimi ya da çalıştırırken **`--add-opens`** seçeneği gerekir. Bu yüzden modern framework'ler eriştikleri paketlerin kendilerine `opens` ile açılmasını bekler.

### Mülakatta ne sorulur?

**"Anotasyon nedir?"** — Davranışı doğrudan değiştirmeyen meta-veri. Etiket + işleyici ayrımını söyleyebilmek beklenir. Kendiliğinden metot üretmez (bunu bir annotation processor yapabilir).

**"Reflection'ın anotasyonu görmesi için ne gerekir?"** — `@Retention(RetentionPolicy.RUNTIME)`. Varsayılan `CLASS`'tır ve reflection'a görünmez. `@Target`, `@Documented`, `@Inherited` bu görünürlüğü belirlemez.

**"`@Target(ElementType.FIELD)` ne yapar?"** — Anotasyonu yalnız alanlara konabilir kılar; başka yere konursa derleme hatası.

**"Anotasyon runtime'da nasıl okunur?"** — `method.isAnnotationPresent(X.class)` / `method.getAnnotation(X.class)`.

**"Reflection'ın maliyetleri?"** — Performans + tip güvenliği kaybı + kapsülleme ihlali. Üstüne Java 16 güçlü kapsülleme kısıtı ve `opens`/`--add-opens` gereği. Bu yüzden reflection framework altyapısında yerindedir (ve genelde **önbelleklenir**), uygulama kodunda ölçülü kullanılmalı; mümkünse arayüz/lambda gibi statik alternatifler yeğlenmelidir.

### Tek cümlede

Anotasyon koda iliştirilen pasif bir etikettir; `@Retention(RUNTIME)` ile işaretlendiğinde reflection onu çalışma zamanında okuyup davranışa çevirir — bu güç, performans, tip güvenliği ve kapsülleme (Java 16'dan beri `opens` gerektiren güçlü kapsülleme) bedeliyle gelir.

# ann-1 | Orta
## Soru
Java'da bir anotasyon (annotation) temelde nedir?
## A
Kodun davranışını doğrudan değiştirmeyen, program öğelerine iliştirilen meta-veri (metadata)
🤖 Doğru: anotasyon kendi başına davranış üretmez; bir işleyici (derleyici/framework/reflection) onu yorumlar.
## B
Çalışma zamanında sınıfa otomatik olarak yeni metot ve alan üreten bir yapı
🤖 Anotasyon kendiliğinden metot üretmez; olsa olsa bir işleyici (annotation processor) üretir.
## C
Bir tür özel yorum satırıdır; derleyici tamamen yok sayar
🤖 Yorumdan farklıdır; derleyici ve araçlar anotasyonu görebilir ve işleyebilir.
## D
Değişkenleri değişmez (final) yapan bir anahtar kelime
🤖 Değişmezlik `final`'ın işidir; anotasyon böyle bir garanti getirmez.
## Doğru
A
## Açıklama
**Anotasyon**, sınıf, metot, alan, parametre gibi program öğelerine iliştirilen **meta-veridir (metadata)**. Kendi başına hiçbir davranış üretmez (A doğru): `@Entity` bir sınıfı sihirli biçimde veritabanına bağlamaz — bir **işleyici** (derleyici, annotation processor ya da reflection kullanan bir framework) o meta-veriyi OKUYUP yorumladığında anlam kazanır. Bu yüzden anotasyon "pasif bir etiket", işleyici ise "etiketi okuyup davranış üreten" taraftır. Anotasyon kendiliğinden metot üretmez (B — bunu bir annotation processor yapabilir), sıradan bir yorum değildir çünkü araçlarca görülür (C yanlış) ve `final` gibi bir değişmezlik anahtarı değildir (D yanlış). Bu ayrım, "anotasyon nasıl çalışır?" sorusunun temelidir: etiket + işleyici.

# ann-2 | Orta
## Soru
Özel bir anotasyonun çalışma zamanında reflection ile OKUNABİLMESİ için hangi meta-anotasyon gerekir?
## A
`@Target(ElementType.METHOD)`
🤖 `@Target` nereye konabileceğini kısıtlar; runtime görünürlüğünü belirleyen o değildir.
## B
`@Retention(RetentionPolicy.RUNTIME)`
🤖 Doğru: yalnız RUNTIME retention'lı anotasyonlar JVM'e yüklenir ve reflection ile okunabilir.
## C
`@Documented`
🤖 `@Documented` yalnız Javadoc'ta görünmeyi etkiler; runtime okunabilirlikle ilgisizdir.
## D
`@Inherited`
🤖 `@Inherited` anotasyonun alt sınıflara miras kalmasıyla ilgilidir; retention'ı belirlemez.
## Doğru
B
## Açıklama
Bir anotasyonun reflection ile çalışma zamanında okunabilmesi, **`@Retention(RetentionPolicy.RUNTIME)`** ile işaretlenmiş olmasına bağlıdır (B doğru). Retention seviyeleri: `SOURCE` (yalnız derleme zamanı, bytecode'a bile yazılmaz — ör. `@Override`), `CLASS` (bytecode'da var ama JVM runtime'da yüklenmez — VARSAYILAN), `RUNTIME` (JVM'e yüklenir ve reflection görebilir). Varsayılan `CLASS` olduğundan, retention belirtmezsen anotasyonun reflection ile GÖRÜNMEZ — bu klasik bir hatadır. `@Target` (A) yalnız anotasyonun nereye konabileceğini kısıtlar; `@Documented` (C) Javadoc görünürlüğüyle, `@Inherited` (D) alt sınıf mirasıyla ilgilidir. Spring/JPA/Jackson anotasyonlarının hepsi RUNTIME'dır; aksi halde framework onları okuyamazdı.

# ann-3 | İleri
## Soru
`@Target(ElementType.FIELD)` bir anotasyon tanımına eklenirse ne olur?
## A
Anotasyon çalışma zamanında otomatik olarak alanların değerini değiştirir
🤖 Değer değiştirmez; @Target yalnız anotasyonun KONABİLECEĞİ yeri kısıtlar.
## B
Anotasyon yalnız `RUNTIME` retention'a sahip olur
🤖 Retention ayrı bir meta-anotasyondur; @Target onu belirlemez.
## C
Anotasyon tüm program öğelerine serbestçe konabilir hale gelir
🤖 Tam tersi; @Target koyulabilecek yerleri KISITLAR, genişletmez.
## D
Anotasyon YALNIZ alanlara (field) konabilir; metoda/sınıfa konulursa derleme hatası olur
🤖 Doğru: @Target izin verilen kullanım yerlerini sınırlar; dışına konulursa derleme hatası verir.
## Doğru
D
## Açıklama
**`@Target`**, bir anotasyonun hangi program öğelerine **konabileceğini** kısıtlayan meta-anotasyondur. `@Target(ElementType.FIELD)` demek: "bu anotasyon YALNIZ alanlara (field) konabilir." Onu bir metoda ya da sınıfa koymaya çalışırsan **derleme hatası** alırsın (D doğru). Bu, anotasyonun yanlış yerde kullanılmasını derleme zamanında engelleyen bir güvenlik mekanizmasıdır. Birden çok hedef verilebilir: `@Target({ElementType.FIELD, ElementType.METHOD})`. `@Target` alan değeri DEĞİŞTİRMEZ (A yanlış — bu bir işleyicinin işi), retention'ı BELİRLEMEZ (B — o `@Retention`'ın işi) ve kullanımı genişletmez, tam tersine KISITLAR (C yanlış). Hedef belirtilmezse anotasyon çoğu bağlama konabilir; `@Target` bu serbestliği daraltıp API sözleşmesini netleştirir.

# ann-4 | İleri
## Soru
Reflection kullanarak bir metodun `@Zamanla` (RUNTIME) anotasyonuyla işaretli olup olmadığı nasıl kontrol edilir?
## A
Anotasyonu okumanın hiçbir yolu yoktur; anotasyonlar yalnız derleyiciye görünür
🤖 Yanlış; RUNTIME retention'lı anotasyonlar reflection ile pekâlâ okunabilir.
## B
Metodun kaynak kodunu metin olarak okuyup `@Zamanla` dizesini aramak
🤖 Kaynak kod runtime'da yoktur; reflection API zaten yapısal, güvenilir bir erişim sunar.
## C
`method.isAnnotationPresent(Zamanla.class)` ya da `method.getAnnotation(Zamanla.class)` ile reflection üzerinden
🤖 Doğru: reflection API, Method üzerinden anotasyon varlığını ve değerlerini sorgulamayı sağlar.
## D
Anotasyonu okumak için mutlaka bir annotation processor yazıp derlemeyi beklemek
🤖 Annotation processor derleme zamanı aracıdır; runtime okuma reflection ile yapılır.
## Doğru
C
## Açıklama
`RUNTIME` retention'lı bir anotasyon, **reflection API** ile çalışma zamanında sorgulanır. Bir `Method` (veya `Class`, `Field`) nesnesi üzerinden: `method.isAnnotationPresent(Zamanla.class)` anotasyonun VAR olup olmadığını, `method.getAnnotation(Zamanla.class)` ise anotasyon örneğini (ve içindeki değerleri) döndürür (C doğru). Anotasyonlar yalnız derleyiciye görünür DEĞİLDİR (A yanlış — RUNTIME olanlar JVM'de yaşar); kaynak kodu metin olarak aramak (B) hem gereksiz hem de kırılgandır çünkü kaynak runtime'da bulunmaz. Annotation processor (D) DERLEME zamanı bir araçtır (`javax.annotation.processing`), runtime okuma için değildir. Spring'in `@Autowired`, JPA'nın `@Column` gibi anotasyonları tam olarak bu reflection tabanlı okuma ile işlenir.

# ann-5 | İleri
## Soru
Reflection'ın yoğun kullanımının başlıca maliyetleri nelerdir?
## A
Derleme zamanı hatalarını artırır ama çalışma zamanında doğrudan çağrıyla birebir aynı hızda çalışır, yani runtime'da bedelsizdir
🤖 Tam tersi; reflection hataları ÇALIŞMA zamanına erteler ve doğrudan çağrıdan belirgin biçimde yavaştır.
## B
Performans yükü (doğrudan çağrıdan yavaş), derleme zamanı tip güvenliğinin kaybı ve kapsülleme (encapsulation) ihlali riski
🤖 Doğru: reflection yavaştır, hataları runtime'a erteler ve private erişimle kapsüllemeyi delebilir.
## C
Tek maliyeti `Class`/`Method` nesnelerinin ek bellek kullanımıdır; hız, tip güvenliği ve kapsülleme etkilenmez
🤖 Bellek tek maliyet değil; hız kaybı, tip güvenliği kaybı ve kapsülleme ihlali de vardır.
## D
Reflektif çağrılar JVM tarafından senkronize edildiğinden kod otomatik thread-safe olur; ödenen tek bedel budur
🤖 Reflection çağrıları senkronize edilmez ve thread-safety sağlamaz; üstelik maliyetsiz de değildir.
## Doğru
B
## Açıklama
Reflection güçlüdür ama bedeli vardır (B doğru): (1) **Performans** — reflektif çağrılar (metot bulma, `invoke`) doğrudan çağrılardan yavaştır; JIT bunları aynı ölçüde optimize edemez. (2) **Tip güvenliği kaybı** — `Class`/`Method` üzerinden yapılan işlemler derleyici tarafından denetlenmez; hatalar (yanlış metot adı, yanlış argüman tipi) ancak ÇALIŞMA zamanında ortaya çıkar. (3) **Kapsülleme ihlali** — `setAccessible(true)` ile `private` üyelere erişilebilir; bu, sınıfın gizli durumunu delerek bakımı ve güvenliği zayıflatır. Java 16'dan beri **güçlü kapsülleme** varsayılan olduğu için JDK iç paketlerine bu yolla erişim `InaccessibleObjectException` ile reddedilir; modül tanımında `opens` ya da çalıştırmada `--add-opens` gerekir. Reflection çalışma zamanı denetimlerini artırır ve yavaştır (A yanlış), maliyeti yalnız bellek değildir (C yanlış) ve thread-safety sağlamaz (D yanlış). Bu yüzden reflection, framework altyapısında yerinde olsa da, uygulama kodunda ölçülü kullanılmalı; mümkünse arayüz/lambda gibi statik alternatifler yeğlenmelidir.

# ann-6 | Uzman
## Soru
Spring ve JPA/Hibernate gibi framework'ler anotasyonları ve reflection'ı hangi amaçla birlikte kullanır?
## A
Anotasyonları (ör. `@Component`, `@Entity`) reflection ile tarayıp okuyarak runtime'da bildirimsel konfigürasyon ve davranış üretmek için
🤖 Doğru: framework'ler RUNTIME anotasyonları reflection ile okuyup konfigürasyon/dispatch/eşleme davranışı üretir.
## B
Yalnızca Javadoc ve IDE ipuçları üretmek için; bu anotasyonlar bytecode'a bile yazılmaz ve runtime'da hiçbir işlev görmez
🤖 Yanlış; bu anotasyonlar RUNTIME retention'lıdır, runtime'da reflection ile okunup fiili davranış üretir.
## C
Sınıf dosyalarını şifreleyip tersine mühendisliği (reverse engineering) zorlaştırmak ve kaynak kodu gizlemek için
🤖 Anotasyon/reflection bir şifreleme veya gizleme aracı değildir; amaç meta-veri temelli konfigürasyondur.
## D
Derleme süresini kısaltmak amacıyla kaynak kodu küçültüp gereksiz sınıfları derleme dışı bırakmak için
🤖 Amaç derleme hızı değil; runtime'da bildirimsel konfigürasyon ve davranış üretimidir.
## Doğru
A
## Açıklama
Modern Java framework'lerinin çekirdek deseni: **bildirimsel (declarative) konfigürasyon**. Geliştirici sınıflarını `@Component`, `@Service`, `@Entity`, `@Column`, `@Autowired` gibi RUNTIME anotasyonlarla etiketler; framework başlatılırken bu sınıfları **tarar (classpath scanning)** ve reflection ile anotasyonları OKUYARAK davranış üretir (A doğru): bean'leri kaydedip nesne yaşam döngüsünü (lifecycle) yönetir, bağımlılıkları enjekte eder (dependency injection), varlıkları (entity) tablolara nesne-ilişki eşlemesiyle bağlar (O/R mapping), metotları HTTP uç noktalarına bağlar. Yani anotasyon "ne istediğini" bildirir, framework reflection ile "nasıl yapılacağını" runtime'da halleder. Bu anotasyonlar salt Javadoc (B) ya da şifreleme (C) için değildir ve amaç derleme hızı (D) değildir. Bu desen sayesinde uygulama kodu XML/boilerplate yerine kısa, okunur etiketlerle konfigüre edilir; reflection'ın performans/güvenlik maliyeti ise framework altyapısında bir kez ödenir ve genelde önbelleklenir (cache).
