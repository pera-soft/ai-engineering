---
slug: test-modern-araclar
baslik: "Modern Test Araçları: JUnit 5, Mockito, Testcontainers, AssertJ, Mutation & Load Test"
kademe: Orta
sira: 188
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

**Test**, kodunun doğru çalıştığını senin yerine kontrol eden ikinci bir kod parçasıdır. Bu testleri elle yazmak yerine kullandığımız hazır kütüphanelere **test araçları** denir. Bir marangozun çekiç, testere ve su terazisi olması gibi: her araç ayrı bir işi yapar, hepsi birlikte bir takım oluşturur.

Bu bölümde 2026'nın tipik Java test takımını tanıyoruz. Buradaki soru şudur: testi **kim koşturur**, bağımlılıkları **kim taklit eder**, sonucu **kim kontrol eder**, gerçek veritabanını **kim ayağa kaldırır**, testlerin ne kadar iyi olduğunu **kim ölçer** ve sistem yük altında ne yapıyor onu **kim gösterir**.

### Temel kavramlar

- **JUnit 5** (Jupiter): testleri bulup çalıştıran motor. `@Test` ile bir metodu test ilan edersin, `@BeforeEach` ile her testten önce hazırlık yaparsın, `@Nested` ile testleri gruplarsın. JUnit 4'ün `@RunWith`'i yerine artık `@ExtendWith` (extension modeli) kullanılır.
- **Sahte nesne (mock)** ve **Mockito**: test edilen sınıfın bağımlılıklarının yerine geçen taklit nesnedir. Mockito bunu üretir. `mock()` taklidi yaratır. `when(...).thenReturn(...)` ona ne cevap vereceğini öğretir. `verify(...)` ise "kaç kez ve hangi argümanla çağrıldı?" diye sorar. Bir sınırı vardır: standart Mockito `static` ve `final` metotları taklit edemez; bunun için ayrı bir `mockito-inline` eklentisi gerekir. Bu sınır, kodu dışarıdan bağımlılık verilebilir (enjekte edilebilir) biçimde tasarlamak için bir baskı yaratır.
- **AssertJ**: sonucu kontrol eden akıcı doğrulama kütüphanesidir. Örnek: `assertThat(liste).extracting("ad").containsExactly("Ada", "Bora")`. Çıplak `assertEquals`'ten iki farkı vardır. Birincisi, daha ifade gücü yüksek kontroller yazdırır. İkincisi, test kaldığında hangi elemanın nerede farklılaştığını gösteren okunur bir mesaj üretir.
- **Parametrik test**: aynı mantığı çok sayıda girdiyle koşmanın yoludur. `@ParameterizedTest` ile birlikte `@CsvSource`, `@ValueSource` veya `@MethodSource` kullanılır. Her satır **bağımsız bir test örneği** olur: biri kalsa bile diğerleri koşar ve ayrı ayrı raporlanır.
- **Testcontainers**: entegrasyon testinde üretimdeki **gerçek** PostgreSQL, Kafka veya Redis'i Docker konteynerinde ayağa kaldıran kütüphanedir.
- **Mutasyon testi (mutation testing)**, örneğin PIT: testlerinin ne kadar iyi olduğunu ölçen araçtır.
- **Yük testi (load test)**, örneğin k6 veya Gatling: sistemi aynı anda çok sayıda sanal kullanıcıyla yükleyip gecikmeyi ve hata oranını ölçen testtir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Testcontainers gerçek motor, H2 sahte motor">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Entegrasyon testi: hangi veritabanı?</text>
  <rect x="30" y="46" width="270" height="128" rx="12" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.5"/>
  <text x="165" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#B5504F" font-family="system-ui">H2 (in-memory taklit)</text>
  <text x="165" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">hızlı ama farklı lehçe</text>
  <text x="165" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">JSONB, upsert, kilit</text>
  <text x="165" y="140" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">davranışı üretimden sapar</text>
  <text x="165" y="160" text-anchor="middle" font-size="11" fill="#B5504F" font-family="system-ui">→ yanlış güven</text>
  <rect x="340" y="46" width="270" height="128" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Testcontainers (gerçek PG)</text>
  <text x="475" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Docker'da üretim motoru</text>
  <text x="475" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">gerçek lehçe + indeks</text>
  <text x="475" y="140" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">saniyeler mertebesinde başlar</text>
  <text x="475" y="160" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">→ üretimle aynı davranış</text>
</svg>
```

### Nasıl çalışır?

**Testcontainers ile H2 karşılaştırması.** H2, "PostgreSQL uyumluluk modu" ile çalışsa bile PostgreSQL değildir; onu **taklit eden başka bir motordur**. Uyumluluk modu SQL sözdiziminin bir kısmını kabul eder. Ama şu davranışlar farklıdır: `JSONB`, `INSERT ... ON CONFLICT` (var olan kaydı güncelleme), pencere fonksiyonları, gerçek indeks seçimi, kilitler ve yalıtım seviyeleri.

Sonuç şudur: test yeşil yanar, üretim kırmızı. Testin verdiği güven **yanlış** güvendir. Testcontainers ise üretimdeki gerçek imajı Docker'da başlatır. Karşılığında iki maliyet gelir: Docker bağımlılığı ve saniyeler süren başlangıç. Kazanç hız veya kurulum kolaylığı değildir; **davranış sadakatidir**.

**Mockito'nun tuzağı.** Bir sahte nesneye "100 gelince 90 dön" diye öğretip sonra "100 verince 90 mı dönüyor?" diye sınamak kısır bir döngüdür. Gerçek üretim mantığı hiç çalışmaz ve test daima geçer. Kural nettir: sahte nesne, test edilen sınıfın **bağımlılıkları** içindir. **Test hedefinin kendisi asla taklit edilmez.**

**Mutasyon testi.** Satır kapsamı (line coverage) yanıltıcıdır. Bir satırın çalıştırılmış olması, sonucunun **doğrulandığı** anlamına gelmez.

PIT gibi araçlar kaynağa küçük ve kasıtlı hatalar enjekte eder; bunlara **mutant** denir. Örnekler: `>` yerine `>=`, `+` yerine `-`, `return true` yerine `false`. Bir test kırmızı yanarsa **mutant öldürülmüştür**; doğrulama gerçekten sonucu kontrol ediyordur. Tüm testler yeşil kalırsa **mutant hayatta kalmıştır**; o satır çalışmış ama hiçbir doğrulama onu sınamamıştır. Zayıf test böyle ifşa olur.

Hedef, kapsam yüzdesi değil **mutasyon skoru** olmalıdır; yani öldürülen mutant oranı. Bedeli yavaşlıktır, çünkü PIT mutantları tek tek koşturur.

**Yük testi ve yüzdelikler.** k6 ile sanal kullanıcı yükleyip ölçüm yaptığında **ortalamaya bakma**. İsteklerin %95'i 60 ms, %5'i 3 saniye sürse bile ortalama düşük çıkar. Ama o %5, gerçek insanların canını yakan kesimdir.

**p95 ve p99 yüzdelikleri** uzun kuyruğu görünür kılar. p99, "isteklerin %99'u şu süreden hızlı" demektir. Hizmet seviyesi hedefleri (SLO) bu yüzden neredeyse hep yüzdelik üzerinden tanımlanır; örneğin "p99 < 500 ms". Yüklü bir sistemde p99, ortalamanın kat kat üstündedir.

Pratik kural: birimde **Mockito + AssertJ**, entegrasyonda **Testcontainers** (H2 değil), kalite kapısında **mutation**, sürüm öncesi **load** test.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Mutation testing: öldürülen ve hayatta kalan mutant">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Mutation testing: satır çalıştı ≠ doğrulandı</text>
  <text x="320" y="46" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Kaynağa küçük mutant (ör. &gt; yerine &gt;=) enjekte et; test yakalıyor mu?</text>
  <rect x="40" y="62" width="250" height="118" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="88" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Mutant öldürüldü ✓</text>
  <text x="165" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bir test kırmızı yandı</text>
  <text x="165" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">assertion sonucu gerçekten</text>
  <text x="165" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kontrol ediyor</text>
  <text x="165" y="170" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">→ güçlü test</text>
  <rect x="350" y="62" width="250" height="118" rx="11" fill="none" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="475" y="88" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Mutant hayatta kaldı ✗</text>
  <text x="475" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tüm testler yeşil kaldı</text>
  <text x="475" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">satır çalıştı ama sonucu</text>
  <text x="475" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hiçbir assertion doğrulamadı</text>
  <text x="475" y="170" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">→ zayıf assertion ifşa</text>
</svg>
```

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Gecikme dağılımı: ortalama kuyruğu gizler, p95/p99 gerçeği gösterir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Ortalama kuyruğu gizler; p95/p99 gerçeği söyler</text>
  <line x1="40" y1="160" x2="600" y2="160" stroke="#5D7789" stroke-width="1.4"/>
  <text x="320" y="192" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">istek gecikmesi (latency) →</text>
  <path d="M60,160 C120,60 180,60 230,110 C300,150 380,158 560,158 L560,160 L60,160 Z" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <line x1="170" y1="70" x2="170" y2="160" stroke="#25804C" stroke-width="2"/>
  <text x="170" y="60" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">ortalama 80ms</text>
  <line x1="430" y1="96" x2="430" y2="160" stroke="#B26B00" stroke-width="2"/>
  <text x="430" y="88" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">p95</text>
  <line x1="520" y1="110" x2="520" y2="160" stroke="#8A2E2E" stroke-width="2"/>
  <text x="520" y="102" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">p99 ~3s</text>
  <text x="500" y="140" text-anchor="middle" font-size="10" fill="#8A2E2E" font-family="system-ui">uzun kuyruk</text>
</svg>
```

### Mülakatta ne sorulur?

- **"H2 uyumluluk modu varken neden Testcontainers?"** Beklenen: uyumluluk modu lehçeyi tam taşımaz; gerçek motor olmadan `JSONB`/upsert/kilit davranışı doğrulanamaz. Kazanç hız (konteyner daha yavaştır) veya kurulum kolaylığı (Docker gerekir) değil, sadakattir.
- **"AssertJ neden çıplak `assertEquals`'ten iyi?"** Beklenen: ifade gücü + hata mesajı kalitesi. `assertEquals` listelerle de çalışır; fark çalışabilirlik değildir.
- **"Aynı mantığı 6 girdiyle nasıl test edersin?"** Beklenen: `@ParameterizedTest` + `@CsvSource`. `for` döngüsü veya arka arkaya assertion ilk hatada durur ve girdileri ayrı raporlamaz.
- **"Kapsam %90 ama üretimde hata çıkıyor, ne eklersin?"** Beklenen: mutation testing; hayatta kalan mutant zayıf assertion'ı gösterir. Kapsamı yükseltmez, ölü kod bulmaz, boru hattını hızlandırmaz.
- **"Bu Mockito testi neyi doğruluyor?"** Beklenen: stub'ı kurup aynı stub'ı sınamak tautolojidir; test hedefi mock'lanmaz.
- **"Neden ortalama değil p95/p99?"** Beklenen: ortalama uzun kuyruğu gizler; SLO yüzdelikle tanımlanır.

### Tek cümlede

Modern test takımı dört ilkeye dayanır: **gerçek motora** karşı koş (Testcontainers), **bağımlılığı** taklit et ama hedefi asla taklit etme (Mockito), sonucu **ifade gücü yüksek** biçimde kontrol et (AssertJ). Kaliteyi kapsam yüzdesiyle değil **öldürülen mutantla**, performansı da ortalamayla değil **yüzdelikle** ölç.

# testtool-1 | Orta
## Soru
Ekip entegrasyon testlerini H2 in-memory veritabanıyla koşuyor; üretimde PostgreSQL kullanılıyor. `JSONB` sorgusu ve `INSERT ... ON CONFLICT` (upsert) içeren bir repository H2'de yeşil, üretimde patlıyor. Testcontainers'a geçmenin temel kazancı nedir?
## A
Testler üretimdeki gerçek PostgreSQL motoruna karşı koşar, lehçe farkları test zamanında yakalanır
🤖 Doğru: gerçek motor, H2 uyumluluk modunun gizlediği üretim davranışını test zamanında ortaya çıkarır.
## B
Kapsam yüzdesi (coverage) otomatik yükselir, aynı testler daha fazla kod yolunu kapsar
🤖 Testcontainers kapsam yüzdesini değiştirmez; aynı testler, farklı — gerçek — veritabanı motoru.
## C
Testler bellek içinde kaldığı için daha da hızlanır ve boru hattı süresi kısalır
🤖 Konteyner başlatma H2'den yavaştır; kazanç hız değil, üretimle aynı davranış.
## D
Docker gerekmediği için CI kurulumu ve yerel geliştirme ortamı basitleşir
🤖 Tersine, Testcontainers çalışan bir Docker gerektirir; basitlik değil, doğruluk kazancı vardır.
## Doğru
A
## Açıklama
H2, PostgreSQL'i **taklit eden** farklı bir motordur; `JSONB`, `ON CONFLICT`, gerçek kilit/izolasyon davranışı ve fonksiyon lehçesi farklıdır. Bu yüzden H2'de yeşil yanan test üretimde patlayabilir — testin verdiği güven **yanlış**tır. Testcontainers, üretimdeki **gerçek PostgreSQL imajını** Docker'da ayağa kaldırır; testler üretimle aynı motora karşı koşar ve lehçe kaynaklı hatalar test zamanında yakalanır. Karşılığında Docker bağımlılığı ve saniyeler mertebesinde başlangıç maliyeti gelir — hız (C) veya kurulum basitliği (D) değil, **davranış sadakati** kazanılır. Kapsam yüzdesi (B) ise hiç değişmez; koşan testler aynıdır, altlarındaki motor farklıdır.

# testtool-2 | Orta
## Soru
Aşağıdaki iki assertion aynı listeyi kontrol ediyor. AssertJ (ikinci) satırının çıplak JUnit'e (birinci) göre temel avantajı nedir?

```java
assertEquals(3, kullanicilar.size());               // JUnit
assertThat(kullanicilar).extracting("ad")
        .containsExactly("Ada", "Bora", "Cem");      // AssertJ
```
## A
AssertJ derleme ve çalışma zamanında ölçülebilir biçimde daha hızlıdır
🤖 Fark çalışma/derleme hızı değil; ikisi de milisaniyeler mertebesinde.
## B
Akıcı zincir içeriği ve sırayı doğrular, başarısızlıkta okunur fark mesajı üretir
🤖 Doğru: `extracting/containsExactly` içeriği ve sırayı doğrular; hata mesajı beklenen/gerçek farkı net gösterir.
## C
AssertJ null-güvenliği garanti eder, zincir içinde NPE oluşması imkânsızdır
🤖 AssertJ NPE'yi büsbütün ortadan kaldırmaz; okunabilirlik ve zengin assertion sağlar.
## D
JUnit'in `assertEquals` metodu koleksiyonlarla ve listelerle hiç çalışmaz
🤖 `assertEquals` listelerle de çalışır; fark ifade gücü ve mesaj kalitesidir, çalışabilirlik değil.
## Doğru
B
## Açıklama
Çıplak `assertEquals(3, size())` yalnız **eleman sayısını** doğrular; içerik ve sıra yanlışsa fark etmez ve başarısızlık mesajı `expected 3 but was 2` gibi fakirdir. AssertJ'nin akıcı (fluent) zinciri `extracting("ad").containsExactly(...)` hem alanları çıkarıp **içeriği ve sırayı** doğrular hem de başarısızlıkta hangi elemanın nerede farklılaştığını ayrıntılı gösterir — hata ayıklamayı hızlandırır. `assertEquals` listelerle çalışır (D yanlış); avantaj çalışabilirlik değil **ifade gücü ve mesaj kalitesidir**.

# testtool-3 | Orta
## Soru
Bir `vergiHesapla(gelir)` fonksiyonunu 6 farklı gelir dilimi için aynı mantıkla test etmek istiyorsun. JUnit 5'te en uygun yaklaşım hangisidir?
## A
6 ayrı `@Test` metodunu kopyalayıp gövdelerinde yalnız sayıları değiştirmek
🤖 Kopyala-yapıştır bakım kâbusudur; parametrik test tam bu tekrarı ortadan kaldırmak için var.
## B
Tek `@Test` içinde 6 assertion'ı arka arkaya yazıp hepsini birlikte koşmak
🤖 İlk assertion kalınca gerisi çalışmaz; ayrıca hangi dilimin kaldığı raporlanmaz.
## C
`@ParameterizedTest` + `@CsvSource` ile girdi/beklenen çiftlerini tek metotta koşmak
🤖 Doğru: her satır ayrı test örneği olur; biri kalırsa diğerleri koşmaya ve raporlanmaya devam eder.
## D
Bir `for` döngüsü içinde 6 değeri gezip her biri için assertion yapmak
🤖 Döngü ilk hatada durur ve her dilimi ayrı test örneği olarak raporlamaz; parametrik testin izolasyonunu vermez.
## Doğru
C
## Açıklama
**Parametrik test** (`@ParameterizedTest`) aynı mantığı farklı girdilerle koşmanın idiomatik yoludur. `@CsvSource({"10000, 1500", "50000, 10000", ...})` her satırı **bağımsız bir test örneği** yapar: biri kalsa bile diğerleri koşar, her dilim adıyla raporlanır ve yeni durum eklemek tek satırdır. Kopyalanmış metotlar (A) bakım borcudur; tek metotta ardışık assertion (B) veya `for` döngüsü (D) ilk hatada durur ve dilimleri ayrı ayrı raporlamaz — hangi girdinin bozulduğunu gizler.

# testtool-4 | İleri
## Soru
Projede satır kapsamı (line coverage) %90 ama üretimde mantık hataları çıkıyor. Mutation testing (ör. PIT) eklemenin bu duruma katkısı nedir?
## A
Kapsam yüzdesini otomatik olarak %90'dan %100'e çıkarır, boşlukları kendi doldurur
🤖 Mutation testing kapsamı yükseltmez; testlerin ne kadar iyi hata yakaladığını ölçer.
## B
Testleri paralel koşarak boru hattı süresini belirgin biçimde kısaltır
🤖 Aksine PIT mutantları tek tek koşturduğu için yavaştır; amacı hız değil kalite ölçümü.
## C
Kullanılmayan (dead) kodu tespit edip projeden temizler, karmaşıklığı düşürür
🤖 Ölü kod tespiti ayrı bir statik analiz işidir; mutation testing çalışan kodun test kalitesini ölçer.
## D
Kaynağa kasıtlı mutant hatalar enjekte edip testlerin bunları öldürüp öldürmediğini ölçer
🤖 Doğru: hayatta kalan mutant, o satır çalıştığı halde hiçbir assertion'ın onu gerçekten doğrulamadığını gösterir.
## Doğru
D
## Açıklama
**Satır kapsamı yanıltıcıdır:** bir satır çalıştırılmış olabilir ama hiçbir assertion sonucunu gerçekten kontrol etmiyorsa, o satır aslında test **edilmemiştir**. **Mutation testing** kaynağa küçük değişiklikler (mutant) enjekte eder — `>` yerine `>=`, `+` yerine `-`, `return true` yerine `false` gibi. İyi bir test paketi bu mutantı fark edip kırmızı yanmalı ("mutant öldürüldü"). **Hayatta kalan mutant**, o kodu çalıştıran ama sonucunu doğrulamayan zayıf assertion'ı ifşa eder. Hedef kapsam yüzdesi değil **mutation score**'dur (öldürülen mutant oranı). Hız (B), ölü kod temizliği (C) veya kapsam artışı (A) mutation testing'in işi değildir.

# testtool-5 | İleri
## Soru
Aşağıdaki Mockito testi neden gerçek bir davranışı doğrulamaz (sahte güven verir)?

```java
@Test
void indirimUygulanir() {
    FiyatServisi mock = mock(FiyatServisi.class);
    when(mock.indirimli(100)).thenReturn(90);
    assertEquals(90, mock.indirimli(100));   // (*)
}
```
## A
`mock()` yerine `@Mock` anotasyonu ve `@ExtendWith` kullanılmadığı için kurulum eksik
🤖 `@Mock` ile `mock()` işlevsel olarak eşdeğerdir; sorun kurulum biçimi değil.
## B
`when` içinde `thenReturn` yerine `thenAnswer` ile dinamik cevap verilmesi gerektiği için
🤖 Bu sabit dönüş için `thenReturn` doğrudur; sorun API seçimi değil, neyin test edildiği.
## C
(*) satırı yalnız mock'un kendi hazır cevabını doğruluyor; üretim mantığı hiç çalışmıyor
🤖 Doğru: stub'ı kurup aynı stub'ı sınamak kendini doğrulayan bir döngüdür; gerçek `FiyatServisi` mantığı hiç koşmaz.
## D
Sonuç `assertEquals` yerine `verify` ile etkileşim olarak doğrulanması gerektiği için
🤖 `verify` etkileşim doğrular ama asıl kusur, üretim mantığının hiç çalıştırılmaması; araç değişimi bunu düzeltmez.
## Doğru
C
## Açıklama
Bu klasik bir **mock tautolojisi**: test, mock'a "100 gelince 90 dön" diye öğretiyor, sonra "100 verince 90 mı dönüyor?" diye kendi öğrettiğini sınıyor. Gerçek `FiyatServisi` gerçeklemesi (indirim mantığı) **hiç çalışmıyor**; test her zaman geçer ama hiçbir üretim davranışını doğrulamaz. Mock'lar, test edilen sınıfın **bağımlılıklarını** taklit etmek içindir — test edilen birimin **kendisi** asla mock'lanmaz. Burada `FiyatServisi.indirimli` test hedefiyse gerçek örnekle test edilmeli. `@Mock` (A), `thenAnswer` (B) veya `verify` (D) bu temel kusuru düzeltmez.

# testtool-6 | İleri
## Soru
Sürüm öncesi bir API'yi k6 ile yükleyip p95 gecikmenin (latency) ve hata oranının kabul eşiğinde kalıp kalmadığını ölçmek istiyorsun. Bu load testte "ortalama gecikme 80 ms" yerine **p95/p99 yüzdeliklerine** bakmanın nedeni nedir?
## A
Ortalama gecikme k6 tarafından hiç raporlanmaz, yalnız yüzdelikler üretilir
🤖 k6 ortalamayı da raporlar; sorun destek değil, ortalamanın kuyruğu gizlemesi.
## B
Ortalama, kullanıcıların bir kısmının yaşadığı uzun kuyruk (tail) gecikmelerini gizler
🤖 Doğru: birkaç saniyelik kuyruk, düşük ortalamanın altında saklanır; SLO'lar bu yüzden yüzdelik üzerinden tanımlanır.
## C
Yüzdelik hesabı daha az CPU harcadığı için yük testinin kendisini hızlandırır
🤖 Metrik seçimi test süresini/CPU'yu belirlemez; amaç doğru sinyali okumaktır.
## D
p99 pratikte her zaman ortalamaya eşittir, ikisi arasında anlamlı fark yoktur
🤖 Yüklü sistemde p99 ortalamadan kat kat büyüktür; kuyruk tam da bu farkta yaşar.
## Doğru
B
## Açıklama
**Ortalama gecikme kuyruğu gizler:** isteklerin %95'i 60 ms, %5'i 3 saniye sürse bile ortalama düşük çıkabilir — ama o %5, gerçek kullanıcıların canını yakan kesimdir. **p95/p99 yüzdelikleri** en yavaş isteklerin deneyimini gösterir; bu yüzden SLO'lar (hizmet seviyesi hedefleri) neredeyse her zaman yüzdelik üzerinden tanımlanır ("p99 < 500 ms"). Yüklü bir sistemde p99 ortalamanın kat kat üstündedir (D yanlış). Load testte hata oranıyla birlikte **yüzdelik gecikmeyi** izlemek, üretimde yaşanacak en kötü deneyimi sürümden önce görmeyi sağlar. Metrik seçimi CPU/hız (C) ile ilgili değildir.
