---
slug: spring-data-jpa
baslik: "Spring Data JPA: Repository Soyutlaması, Türetilmiş Sorgular ve Projeksiyon"
kademe: Orta
sira: 80
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kütüphanede her kitabı raftan kendin aramak yerine görevliye "Deniz'in 2020 sonrası kitapları" dersin; görevli aramayı nasıl yapacağını bilir, sen yalnız **ne istediğini** söylersin. Spring Data JPA da veritabanı işlerinde bu görevlidir: "şu koşula uyan kayıtları getir" dersin, bağlantı açma, sorgu yazma, sonucu nesneye çevirme gibi tekrar eden işleri o yapar. Böylece her tablo için elle yazılan yüzlerce satırlık **şablon kod** (boilerplate) ortadan kalkar.

### Temel kavramlar

- **Veritabanı ve tablo:** Verinin satır-sütun hâlinde saklandığı yer. Bir satır bir kaydı (bir kullanıcıyı), bir sütun bir özelliği (e-postayı) tutar.
- **SQL:** Veritabanına sorgu sorma dili — `SELECT * FROM kullanici WHERE aktif = true`.
- **ORM (Object-Relational Mapping):** Tablodaki satırla Java nesnesi arasındaki çeviri. `kullanici` tablosunun bir satırı ↔ bir `Kullanici` nesnesi. JPA bu çevirinin Java standardıdır.
- **Varlık (entity):** Bir tabloya eşlenmiş Java sınıfı; `@Entity` ile işaretlenir.
- **`EntityManager`:** JPA'nın asıl motoru; varlıkları kaydeder, bulur, siler.
- **Arayüz (interface):** Java'da "hangi metotlar var" listesini gövdesiz tanımlayan yapı. Gövdeyi normalde bir sınıf yazar — burada **Spring yazar**.
- **Repository:** Bir varlık tipine ait veri erişim metotlarını toplayan arayüz. `JpaRepository<Kullanici, Long>` genişleten bir arayüz yazmak, `save`, `findById`, `findAll`, `delete` gibi düzinelerce metodu bedavaya getirir — sen hiçbir gövde yazmazsın.
- **Vekil nesne (proxy):** Çalışma zamanında üretilen ara nesne. Senin arayüzünü uygular ve çağrıları asıl işi yapan koda yönlendirir.
- **JPQL:** SQL'e benzeyen ama tablo/sütun yerine **varlık ve alan adlarıyla** yazılan sorgu dili.
- **Sayfalama (pagination):** Binlerce kaydı tek seferde değil, sayfa sayfa (örn. 20'şer) getirmek.
- **Projeksiyon (projection):** Varlığın tamamı yerine yalnız gereken alanları okumak.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Repository arayüzü, çalışma-zamanı proxy'si ve üç sorgu yolu">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Arayüz yazarsın, Spring implementasyonu üretir</text>
  <rect x="24" y="44" width="180" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="114" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="monospace">interface KullaniciRepo</text>
  <text x="114" y="92" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">extends JpaRepository</text>
  <text x="114" y="107" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(gövde YOK)</text>
  <text x="238" y="84" text-anchor="middle" font-size="20" fill="#25804C" font-family="system-ui">→</text>
  <rect x="266" y="44" width="180" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="356" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Proxy (runtime)</text>
  <text x="356" y="92" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Spring üretir; JPA</text>
  <text x="356" y="107" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">EntityManager'a delege eder</text>
  <rect x="24" y="132" width="592" height="62" rx="10" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.3"/>
  <text x="320" y="153" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B67A1A" font-family="system-ui">Üç sorgu yolu</text>
  <text x="150" y="176" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Türetilmiş metot adı</text>
  <text x="320" y="176" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@Query (JPQL / native)</text>
  <text x="500" y="176" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Specification (dinamik)</text>
</svg>
```

### Nasıl çalışır?

Uygulama açılışında Spring, `JpaRepository`'yi genişleten **her arayüz** için bir **proxy** (JDK dynamic proxy) üretir. `findById`, `save` gibi standart metotlar `SimpleJpaRepository` üzerinden `EntityManager`'a delege edilir; senin yazdığın özel metotlar ise ya metot adından üretilen sorguya ya da `@Query`'ye bağlanır. Bu yüzden ne bir implementasyon sınıfı yazman gerekir ne de metot adının arandığı bir SQL dosyası vardır.

Sorgu tanımlamanın üç yolu var:

- **Türetilmiş sorgu metotları** (derived query methods): sorgu metodun **adından** üretilir. Ad, sabit bir **dilbilgisine** (grammar) göre ayrıştırılır:

```java
List<Kullanici> findByAktifTrueAndYasGreaterThanOrderByYasDesc(int yas);
// findBy → giriş | AktifTrue → aktif = true | And → VE
// YasGreaterThan → yas > ? | OrderByYasDesc → ORDER BY yas DESC
```

  Üretilen SQL koşulu ve sıralamayı **veritabanında** uygular; hiçbir şey Java belleğine çekilip elle filtrelenmez. `And` gerçek bir bağlaçtır, yok sayılmaz; ad uzunluğu geçerliliği bozmaz — ama ad uzadıkça okunabilirlik düşer, o noktada `@Query`'ye geçilir.

- **`@Query`**: JPQL (varlık/alan adlarıyla) veya `nativeQuery = true` ile ham SQL (tablo/sütun adlarıyla — bu yüzden veritabanı değişikliklerine daha kırılgandır). Karmaşık sorgular, JOIN'ler ve veritabanına özgü fonksiyonlar için. Veri değiştiren (`UPDATE`/`DELETE`) bir `@Query` metoduna **`@Modifying`** konur ve genelde bir `@Transactional` sınırı gerekir. `Page` döndüren **native** sorguda Spring ham SQL'i "anlamadığından" sayım (count) sorgusunu güvenilir türetemez; `@Query(value = ..., countQuery = "SELECT count(*) FROM ...", nativeQuery = true)` ile **elle** verilir. Native sorgu `Pageable` ile pekâlâ kullanılır; `LIMIT`/`OFFSET` kısmını Spring ekler. Bağlı parametre kullanıldığında native olmak ekstra bir güvenlik de getirmez — JPQL de aynı ölçüde korumalıdır.

- **`Specification` / Querydsl**: koşulların çalışma-zamanında birleştirildiği dinamik filtreler için.

Pratik kural: basit koşullar için türetilmiş metot, karmaşık okuma için `@Query`, çok değişkenli dinamik filtre için `Specification`. Sayfalama, sıralama ve projeksiyon üçünde de kullanılabilir — ve kıdemli geliştirici mülakatında asıl fark bu ayrıntılarda çıkar.

Sayfalamada dönüş tipi seçimi de bir performans kararıdır. **`Page<T>`** iki sorgu çalıştırır: (1) sayfa verisi (`LIMIT`/`OFFSET`) ve (2) toplam eleman sayısı için bir **sayım (count) sorgusu**. Böylece toplam kayıt ve toplam sayfa sayısını bilir; ama büyük tabloda bu sayım pahalıdır.

**`Slice<T>`** sayım sorgusu **atmaz**; istenen boyuttan bir kayıt fazla çekerek yalnız "sonraki sayfa var mı" (`hasNext`) bilgisini üretir — sonsuz kaydırma (infinite scroll) için idealdir. `Slice` de sonuçları sayfa sayfa çeker (hepsini belleğe almaz), `Sort`'u destekler ve önbellekle bir ilgisi yoktur; tek farkı atlanan sayım sorgusudur. Toplam sayfa sayısı gerekmiyorsa `Slice` seçmek doğrudan bir sorgu tasarrufudur.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Türetilmiş sorgu metot adının parçalara ayrıştırılması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Metot adı bir dilbilgisiyle ayrıştırılır</text>
  <text x="320" y="52" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="monospace">findBy · AktifTrue · And · YasGreaterThan · OrderByYasDesc</text>
  <rect x="20" y="72" width="118" height="44" rx="8" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="79" y="92" text-anchor="middle" font-size="11" font-weight="700" fill="#5D7789" font-family="monospace">findBy</text>
  <text x="79" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">giriş</text>
  <rect x="144" y="72" width="118" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="203" y="92" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="monospace">AktifTrue</text>
  <text x="203" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">aktif = true</text>
  <rect x="268" y="72" width="70" height="44" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="303" y="92" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="monospace">And</text>
  <text x="303" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">VE</text>
  <rect x="344" y="72" width="150" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="419" y="92" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="monospace">YasGreaterThan</text>
  <text x="419" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">yas &gt; :yas</text>
  <rect x="500" y="72" width="120" height="44" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="560" y="92" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="monospace">OrderByYasDesc</text>
  <text x="560" y="108" text-anchor="middle" font-size="10" fill="#1C3247" font-family="monospace">ORDER BY yas DESC</text>
  <rect x="60" y="140" width="520" height="42" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/>
  <text x="320" y="160" text-anchor="middle" font-size="10.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Üretilen SQL (koşul + sıralama veritabanında)</text>
  <text x="320" y="176" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">WHERE aktif = true AND yas &gt; ? ORDER BY yas DESC</text>
</svg>
```

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Page ile Slice arasındaki sorgu farkı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Page vs Slice: fark count sorgusunda</text>
  <rect x="24" y="42" width="288" height="150" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="168" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#B26B00" font-family="monospace">Page&lt;T&gt;</text>
  <text x="168" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">1) veri: LIMIT / OFFSET</text>
  <text x="168" y="114" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">2) count: SELECT count(*)</text>
  <text x="168" y="136" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">→ toplam sayfa sayısı bilinir</text>
  <text x="168" y="162" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">2 sorgu</text>
  <text x="168" y="182" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">büyük tabloda count pahalı</text>
  <rect x="328" y="42" width="288" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="monospace">Slice&lt;T&gt;</text>
  <text x="472" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">veri: boyut + 1 kayıt çeker</text>
  <text x="472" y="114" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">count sorgusu YOK</text>
  <text x="472" y="136" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">→ yalnız hasNext bilinir</text>
  <text x="472" y="162" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">1 sorgu</text>
  <text x="472" y="182" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sonsuz kaydırma için ideal</text>
</svg>
```

### Mülakatta ne sorulur?

- **Gövdesiz arayüz nasıl çalışıyor?** Spring açılışta arayüz için bir proxy üretir ve çağrıyı `SimpleJpaRepository` üzerinden `EntityManager`'a delege eder. Implementasyon sınıfı yazmak, `@Query` koymak veya bir SQL eşleme dosyası tutmak gerekmez.
- **Türetilmiş metot ne üretir?** Ad dilbilgisine göre ayrıştırılır; koşul ve sıralama **SQL'e** çevrilip veritabanında uygulanır, Java'da elle filtreleme yapılmaz.
- **`@Modifying` ne zaman?** `UPDATE`/`DELETE` yapan bir `@Query` metodunda zorunludur; yanında bir `@Transactional` sınırı gerekir.
- **Native + `Page` tuzağı.** Sayım sorgusu güvenilir türetilemez; `countQuery` elle verilir. `LIMIT`/`OFFSET`'i Spring ekler, native olmak güvenlik avantajı sağlamaz.
- **`Page` mi `Slice` mı?** `Page` ek `count` sorgusu çalıştırır ve toplam sayfayı bilir; `Slice` bu pahalı sayımı atlar, yalnız `hasNext` taşır.
- **Kapalı mı açık mı projeksiyon?** Arayüz tabanlı **kapalı** (closed) projeksiyon yalnız getter içerir; Spring hangi alanların istendiğini bildiği için `SELECT ad, email` gibi **daraltılmış** bir sorgu üretebilir. `@Value("#{target.ad + ' ' + target.email}")` gibi SpEL ifadesi içeren **açık** (open) projeksiyonda ise gereken alanlar statik olarak bilinemez: Spring **varlığın tamamını çeker** ve ifadeyi bellekte değerlendirir — SpEL veritabanında değil bellekte çalışır. Yani tercih mümkün olduğunca kapalı projeksiyondur; projeksiyon `SELECT *`'a mahkûm değildir ve `LazyInitializationException` fırlatmaz.
- **Denetim alanları neden `null`?** `@CreatedDate` / `@LastModifiedDate` **iki parçanın** birlikte kurulmasını ister: bir konfigürasyon sınıfında `@EnableJpaAuditing` ve varlıkta `@EntityListeners(AuditingEntityListener.class)` (ya da ortak bir `@MappedSuperclass` temel sınıf üzerinden). Bu dinleyici `persist`/`update` yaşam döngüsü olaylarını yakalayıp alanları doldurur; biri eksikse alanlar **sessizce** `null` kalır. Denetim sorgu türüne, `@Version` alanına veya alan tipine bağlı değildir (`Instant`, `LocalDateTime`, `Date` desteklenir); kullanıcı bilgisini doldurmak için ayrıca bir `AuditorAware` nesnesi (bean — Spring'in üretip yönettiği nesne) tanımlanır.

### Tek cümlede

Repository arayüzünü sen tanımlarsın, implementasyonu Spring üretir; işin ustalığı sorguyu nereden alacağını (ad, `@Query`, `Specification`) ve `Page`/`Slice` ile projeksiyon seçimlerinin kaç sorguya ve kaç sütuna mal olduğunu bilmektir.

# sdjpa-1 | Temel
## Soru
`JpaRepository<Kullanici, Long>` genişleten bir arayüz tanımladın ama hiçbir metot gövdesi yazmadın. `kullaniciRepo.findById(5L)` çağrısı çalışma-zamanında nasıl çalışır?
## A
Çağrı derlenmez; `@Repository` ile işaretli, her metodu elle uygulayan bir sınıf yazmak zorunludur.
🤖 Tam tersi: gövde yazmak Spring Data'nın var oluş amacına aykırı; implementasyonu o üretir.
## B
Yalnızca `@Query` ile işaretlenmiş metotlar çalışır; kalanlar `UnsupportedOperationException` fırlatır.
🤖 `findById` gibi `JpaRepository` metotları da türetilmiş metotlar da çalışır; @Query şart değil.
## C
Spring, arayüz için çalışma-zamanında bir proxy üretip çağrıyı `EntityManager`'a delege eder.
🤖 Doğru: sen arayüzü verirsin, Spring proxy'yi üretip JPA'ya bağlar.
## D
Metot adı bir SQL dosyasında aranır; bulunamazsa uygulama açılışta çöker.
🤖 Böyle bir SQL dosyası eşlemesi yok; sorgu ya metot adından türetilir ya `@Query`'den gelir.
## Doğru
C
## Açıklama
Spring Data JPA'nın çekirdek fikri: **repository arayüzünü sen tanımlarsın, implementasyonu framework üretir.** Uygulama açılışında Spring, `JpaRepository`'yi genişleten her arayüz için bir **proxy** (JDK dynamic proxy) oluşturur; bu proxy `findById`, `save` gibi standart metotları `SimpleJpaRepository` üzerinden `EntityManager`'a delege eder. Türetilmiş metotları ise metot adından ürettiği sorguya bağlar. Bu yüzden ne bir implementasyon sınıfı (A) ne de bir SQL eşleme dosyası (D) gerekir; `@Query` de zorunlu değildir (B) — yalnız ad dilbilgisinin yetmediği yerlerde devreye girer.

# sdjpa-2 | Temel
## Soru
Aşağıdaki türetilmiş sorgu metodu hangi sorguyu üretir?

```java
List<Kullanici> findByAktifTrueAndYasGreaterThanOrderByYasDesc(int yas);
```
## A
Tüm kullanıcıları belleğe çeker; filtrelemeyi ve sıralamayı Java tarafında `Stream` ile yapar.
🤖 Türetilmiş metotlar koşulu ve sıralamayı SQL'e çevirir; Java'da elle filtreleme yapmaz.
## B
`aktif = true VE yas > :yas` koşulunu ve azalan `yas` sıralamasını veritabanında uygular.
🤖 Doğru: `AktifTrue` + `YasGreaterThan` koşul, `OrderByYasDesc` sıralama olur.
## C
`aktif = true VEYA yas > :yas` koşulunu çalıştırır; `And` kelimesi yok sayılır.
🤖 `And` gerçek bir bağlaçtır ve VE anlamına gelir; yok sayılmaz.
## D
Derleme hatası verir; metot adı çok uzun olduğu için ayrıştırıcı reddeder.
🤖 Ad uzunluğu sorun değil; dilbilgisine uyduğu sürece geçerli bir türetilmiş metottur.
## Doğru
B
## Açıklama
Türetilmiş sorgu metotları belirli bir **dilbilgisiyle** ayrıştırılır: `findBy` sonrası gelen özellik adları koşula, `OrderBy...` son eki sıralamaya çevrilir. Burada `AktifTrue` → `aktif = true`, `And` → mantıksal VE, `YasGreaterThan` → `yas > :yas`, `OrderByYasDesc` → `ORDER BY yas DESC`. Üretilen SQL koşulu ve sıralamayı **veritabanında** uygular; hiçbir şey Java belleğine çekilip elle filtrelenmez (A). `And` gerçek bir bağlaçtır (C), ad uzunluğu geçerliliği bozmaz (D). Not: ad çok uzayınca okunabilirlik düşer — bu noktada `@Query` tercih edilir.

# sdjpa-3 | Orta
## Soru
`@Query(value = "SELECT * FROM kullanici WHERE ...", nativeQuery = true)` ile `Pageable` alan bir metot yazdın. Native sorguyla sayfalamada dikkat edilmesi gereken en önemli fark nedir?
## A
Native sorgu `Pageable` ile hiç kullanılamaz; sayfalama yalnız JPQL sorgularında mümkündür.
🤖 Native sorgu da `Pageable` alabilir; `LIMIT`/`OFFSET` kısmını Spring ekler.
## B
Native sorguda `Sort` otomatik uygulanır ama `LIMIT` cümlesini her zaman sen elle yazmalısın.
🤖 Sayfalamanın `LIMIT`/`OFFSET` kısmını Spring ekler; elle yazman gerekmez.
## C
Native sorgu JPQL'e göre daha güvenlidir çünkü SQL enjeksiyonuna tümüyle kapalıdır.
🤖 Bağlı parametreler her ikisinde de güvenlidir; native olmak ekstra güvenlik getirmez.
## D
`Page` döndürülüyorsa sayım sorgusu çoğu zaman `countQuery` ile elle verilmelidir.
🤖 Doğru: native + Page'de sayım sorgusu türetilemeyebilir, `countQuery` gerekir.
## Doğru
D
## Açıklama
JPQL'de Spring, `Page` için gereken **sayım (count) sorgusunu** otomatik türetebilir çünkü sorguyu anlar. **Native** sorguda ise Spring ham SQL'i "anlamaz"; karmaşık bir native sorguda sayım sorgusunu güvenilir üretemez ve `@Query(value=..., countQuery="SELECT count(*) FROM ...", nativeQuery=true)` ile **sayım sorgusunu elle** vermen gerekir. Ayrıca native sorgu **tablo/sütun adlarıyla** yazılır (JPQL'deki gibi varlık/alan adlarıyla değil), bu yüzden veritabanı değişikliklerine daha kırılgandır. Native sorgu `Pageable` ile **kullanılabilir** (A yanlış); `LIMIT`/`OFFSET`'i Spring ekler (B); bağlı parametre kullanıldığında güvenlik farkı yoktur (C).

# sdjpa-4 | Orta
## Soru
Bir listeleme ekranında binlerce kayıt var. `Page<Urun>` yerine `Slice<Urun>` döndürmenin temel performans avantajı nedir?
## A
Toplam sayı için ek `count` sorgusu ÇALIŞTIRMAZ; yalnız `hasNext` bilgisini taşır.
🤖 Doğru: Page count sorgusu koşturur, Slice koşturmaz — pahalı sayımı atlar.
## B
Tüm sonuçları tek seferde belleğe yükleyip sayfaları oradan verdiği için daha hızlıdır.
🤖 Tam tersi; Slice de sayfa sayfa çeker, hepsini belleğe almaz.
## C
Sonuçları veritabanı yerine ikinci seviye önbellekten (cache) okuduğu için hızlıdır.
🤖 Slice'ın önbellekle ilgisi yok; fark yalnız count sorgusundadır.
## D
`Slice` ile sıralama (`Sort`) yapılamaz; bu yüzden veritabanı daha az iş yapar.
🤖 Slice de Sort destekler; avantaj sıralamanın kaldırılmasından gelmez.
## Doğru
A
## Açıklama
`Page<T>` iki sorgu çalıştırır: (1) sayfa verisi (`LIMIT/OFFSET`) ve (2) **toplam eleman sayısı** için bir `count` sorgusu — böylece toplam sayfa sayısını bilir. Büyük tablolarda bu `count` sorgusu pahalıdır. `Slice<T>` toplam sayıyı **bilmez**; yalnız "sonraki sayfa var mı?" (`hasNext`) bilgisini, istenen boyuttan bir fazla kayıt çekerek anlar. Sonsuz kaydırma (infinite scroll) gibi "toplam sayfa" gerekmeyen arayüzlerde `Slice` ile pahalı `count` sorgusunu atlarsın. Slice her şeyi belleğe almaz (B), önbellekle ilgisi yoktur (C) ve `Sort`'u destekler (D).

# sdjpa-5 | İleri
## Soru
Bir arayüz tabanlı projeksiyon (interface-based projection) ile yalnız `ad` ve `email` alanlarını okumak istiyorsun. **Kapalı** (closed) projeksiyon ile SpEL/`@Value` kullanan **açık** (open) projeksiyon arasındaki kritik performans farkı nedir?
## A
İkisi de her zaman `SELECT *` üretir; projeksiyonun performansa etkisi yoktur, yalnız API sadeleşir.
🤖 Kapalı projeksiyon yalnız gerekli sütunları seçebilir; performans farkı gerçektir.
## B
Kapalı projeksiyonda yalnız `ad, email` sütunları seçilir; açık projeksiyonda tüm varlık çekilir.
🤖 Doğru: kapalı projeksiyon sütun seçimini daraltır, açık olan tüm varlığı çeker.
## C
Açık projeksiyon her zaman daha hızlıdır çünkü SpEL ifadesi veritabanında değerlendirilir.
🤖 SpEL veritabanında değil bellekte çalışır; bu yüzden açık projeksiyon tüm varlığı çeker.
## D
Kapalı projeksiyon `LazyInitializationException` fırlatır; bu yüzden her zaman açık projeksiyon kullanılmalıdır.
🤖 Kapalı projeksiyon istisna fırlatmaz; aksine daha dar ve verimli sorgu üretir.
## Doğru
B
## Açıklama
**Kapalı projeksiyon** (yalnız getter'ları olan, SpEL içermeyen bir arayüz) Spring'e hangi alanların gerektiğini net söyler; Spring `SELECT ad, email` gibi **daraltılmış** bir sorgu üretebilir — daha az veri, daha hızlı. **Açık projeksiyon** ise `@Value("#{target.ad + ' ' + target.email}")` gibi SpEL ifadeleri içerir; Spring hangi alanların kullanılacağını statik olarak bilemez, bu yüzden **tüm varlığı çeker** ve ifadeyi bellekte (target üzerinde) değerlendirir. Yani senior tercih: mümkün olduğunca **kapalı** projeksiyon kullan. SpEL veritabanında değil bellekte çalışır (C), projeksiyon `SELECT *`'a mahkûm değildir (A) ve istisna fırlatmaz (D).

# sdjpa-6 | İleri
## Soru
`@CreatedDate` ve `@LastModifiedDate` alanlarını ekledin ama kayıtlarda bu alanlar hep `null` kalıyor. En olası sebep nedir?
## A
Denetim alanları yalnız native sorguyla kaydedince dolar; JPQL veya `save` ile boş kalır.
🤖 Denetim alanları sorgu türüne bağlı değil; sorun altyapının etkinleştirilmemesidir.
## B
`@CreatedDate` yalnız varlıkta bir `@Version` alanı da varsa çalışır; sürüm alanı eklenmelidir.
🤖 Denetim `@Version`'a bağlı değil; ayrı bir mekanizmadır.
## C
`@EnableJpaAuditing` ve/veya `@EntityListeners(AuditingEntityListener.class)` eksiktir.
🤖 Doğru: denetimi bir yapılandırma anotasyonu ve varlık dinleyicisi birlikte tetikler.
## D
Alan tipi `Instant` olduğu için dolmaz; denetim yalnız `java.util.Date` ile çalışır.
🤖 `Instant`, `LocalDateTime` gibi tipler desteklenir; tip sorunu değildir.
## Doğru
C
## Açıklama
Spring Data JPA denetimi (auditing) **iki parçanın** birlikte kurulmasını gerektirir: (1) bir konfigürasyon sınıfında `@EnableJpaAuditing` ve (2) denetlenecek varlıkta `@EntityListeners(AuditingEntityListener.class)` (veya bir ortak temel sınıf/`@MappedSuperclass` üzerinden). Bu dinleyici, `persist`/`update` yaşam döngüsü olaylarını yakalayıp `@CreatedDate`/`@LastModifiedDate` alanlarını doldurur. İkisinden biri eksikse alanlar sessizce `null` kalır — sık düşülen tuzak budur. Denetim, sorgu türüne (A), `@Version` varlığına (B) veya alan tipine (D — `Instant`/`LocalDateTime`/`Date` desteklenir) bağlı değildir; kullanıcı bilgisi için ayrıca bir `AuditorAware` bean'i tanımlanır.
