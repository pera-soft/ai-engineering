---
slug: jpa-hibernate-mapping
baslik: "JPA/Hibernate Eşleme: Kimlik Üretimi, Kalıtım, @Version ve equals/hashCode Tuzağı"
kademe: İleri
sira: 81
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kütüphanenin fişleme dolabını düşün: her kitabın rafta bir yeri, dolapta da o yeri anlatan bir fişi vardır. Fişi değiştirdiğinde kitabın yeri de değişmelidir; iki taraf hep uyumlu kalmalıdır. Java'da da bellekteki nesneler ile veritabanındaki satırlar iki ayrı dünyadır ve birinin değişimi ötekine yansıtılmalıdır. **Eşleme** (mapping) bu iki dünyayı birbirine bağlama işidir. Yüzeyde basit görünür — `@Entity`, `@Table`, `@Id` — ama asıl mesele bu bağın **davranışsal** sonuçlarıdır: kimlik ne zaman verilir, değişiklik ne zaman yazılır, iki kişi aynı satırı aynı anda değiştirirse ne olur.

### Temel kavramlar

- **Varlık (entity):** Bir veritabanı tablosuna eşlenmiş Java sınıfı. Bir nesne = bir satır.
- **Birincil anahtar (primary key) / `@Id`:** Satırı benzersiz kılan alan; genelde otomatik artan bir sayı.
- **`EntityManager` ve kalıcı bağlam (persistence context):** Hibernate'in o an "elinde tuttuğu" varlıkların defteri. Aynı zamanda **birinci seviye önbellektir**: aynı kimliği iki kez istersen veritabanına ikinci kez gitmez.
- **`flush` (belleği veri tabanına boşaltma):** Bağlamdaki değişikliklerin SQL'e çevrilip veritabanına gönderilmesi anı — genelde commit'ten (kalıcılaştırmadan) hemen önce.
- **İşlem (transaction):** Ya tamamı olan ya hiçbiri olmayan iş bloğu. `commit` ile kalıcılaşır.
- **Yansıma (reflection):** Java'nın çalışma-zamanında sınıfları inceleyip nesne yaratabilme yeteneği. Hibernate varlıkları böyle üretir.
- **Vekil nesne (proxy):** Gerçek nesnenin yerine geçen, ilk erişimde asıl veriyi yükleyen ara nesne. Tembel yükleme (lazy loading — ihtiyaç olunca yükleme) bununla çalışır.
- **Kirlilik takibi (dirty checking):** Bağlamdaki varlığın alanı değişti mi diye kontrol edip otomatik `UPDATE` üretme.
- **İyimser kilitleme (optimistic locking):** Kilit tutmadan, çakışmayı yazma anında yakalama.

Bir sınıfın varlık sayılması için üç şey gerekir: sınıfta `@Entity`, birincil anahtar alanında `@Id` ve **parametresiz (no-arg) bir yapıcı** (en az `protected` görünürlükte) — Hibernate nesneyi yansımayla bundan üretir. Sınıf `final` **olmamalıdır**; tembel yükleme için ondan bir alt sınıf proxy'si türetilir ve `final` bunu engeller. `Serializable` iyi bir alışkanlıktır ama zorunlu değildir, `Comparable` ise konuyla ilgisizdir; zorunlu bir Hibernate temel sınıfı da yoktur — `@MappedSuperclass` isteğe bağlı bir kolaylıktır.

Bir sınıf hiyerarşisini (örn. `Odeme` → `KrediKarti`, `Havale`) tabloya dökmenin üç yolu vardır ve üçü de bir takas sunar: `SINGLE_TABLE` (varsayılan), `JOINED` ve `TABLE_PER_CLASS`.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kalıtım stratejileri: tek tablo, birleştirilmiş, sınıf başına tablo">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kalıtım eşleme stratejileri — üç takas</text>
  <rect x="20" y="40" width="195" height="160" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="117" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">SINGLE_TABLE</text>
  <text x="117" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Tek tablo + discriminator</text>
  <text x="117" y="104" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">+ En hızlı okuma (JOIN yok)</text>
  <text x="117" y="124" text-anchor="middle" font-size="10.5" fill="#B4442E" font-family="system-ui">− Alt sınıf alanı NOT NULL</text>
  <text x="117" y="140" text-anchor="middle" font-size="10.5" fill="#B4442E" font-family="system-ui">  olamaz (seyrek tablo)</text>
  <rect x="223" y="40" width="195" height="160" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">JOINED</text>
  <text x="320" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Sınıf başına tablo + FK</text>
  <text x="320" y="104" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">+ Normalize, NOT NULL olur</text>
  <text x="320" y="124" text-anchor="middle" font-size="10.5" fill="#B4442E" font-family="system-ui">− Her okuma JOIN gerektirir</text>
  <rect x="426" y="40" width="195" height="160" rx="10" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.3"/>
  <text x="523" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#B67A1A" font-family="system-ui">TABLE_PER_CLASS</text>
  <text x="523" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Somut sınıf başına tam tablo</text>
  <text x="523" y="104" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">+ Alt sınıf okuması JOIN'siz</text>
  <text x="523" y="124" text-anchor="middle" font-size="10.5" fill="#B4442E" font-family="system-ui">− Polimorfik sorgu UNION ister</text>
</svg>
```

### Nasıl çalışır?

Bir varlık nesnesi dört **yaşam döngüsü** (lifecycle) durumundan birindedir. `new` ile yaratılan nesne **geçicidir (transient)** — Hibernate onu tanımaz. `persist` çağrılınca **yönetilen (persistent)** duruma geçer ve kalıcı bağlamda takip edilir. Bağlam kapanınca (`detach`/`close`) **kopmuş (detached)** olur; `remove` sonrası **silinmiş (removed)** sayılır.

`persist` yalnız geçici nesne içindir. Kopmuş bir nesneyi yeniden yönetime almak için **`merge`** kullanılır. Dikkat: `merge` sana verdiğin nesneyi yönetmez, **yönetilen bir kopya döndürür**; bundan sonra o kopya üzerinden çalışılır.

```java
@Transactional
void adDegistir(Long id) {
  Musteri m = repo.findById(id).get(); // persistent: bağlam onu takip ediyor
  m.setAd("Yeni");                     // yalnız bellekte değişti
}                                      // commit → flush → otomatik UPDATE
```

Burada `repo.save(m)` **gereksizdir**. Hibernate bağlama giren her varlığın bir anlık görüntüsünü (snapshot) tutar ve `flush` anında mevcut durumla karşılaştırır; bir alan değişmişse `UPDATE`'i kendisi üretir. Buna **kirlilik takibi** denir. `@DynamicUpdate` yalnız `UPDATE`'e hangi sütunların konacağını (tümü mü, yalnız değişenler mi) etkiler, takibi açıp kapatmaz. Dikkat: bu davranış varlık **detached** ise geçerli değildir — o durumda `merge`/`save` gerekir.

Kimlik üretimi ise sessiz bir performans kararıdır. `GenerationType.IDENTITY` ile `id`, satır veritabanına yazıldığı **anda** üretilir (auto-increment — kendiliğinden artan sütun). Hibernate yönetilen nesnenin kimliğini hemen bilmek zorundadır. Bu yüzden her `insert`'i tek tek çalıştırır ve **JDBC toplu eklemeyi (batch insert) devre dışı bırakır**. `GenerationType.SEQUENCE` ise bir veritabanı dizisinden `allocationSize` kadar değeri **önceden blok hâlinde ayırır**; kimlikleri uygulama tarafında atayabildiği için `insert`'ler gruplanıp toplu gönderilebilir (Spring Boot'ta ayar adı `spring.jpa.properties.hibernate.jdbc.batch_size`; öneki unutulursa ayar sessizce etkisiz kalır). On binlerce satırlık bir içe aktarımda fark saatlere varır. Modern veritabanları (PostgreSQL, H2 ...) diziyi destekler; "SEQUENCE yalnız Oracle'da çalışır" iddiası doğru değildir.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Varlık yaşam döngüsü: transient, persistent, detached, removed durumları">
  <defs>
    <marker id="jpamap-life-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Varlık yaşam döngüsü durumları</text>
  <rect x="30" y="80" width="150" height="60" rx="10" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="105" y="106" text-anchor="middle" font-size="12" font-weight="700" fill="#5D7789" font-family="system-ui">Transient</text>
  <text x="105" y="126" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">new · bağlam dışı</text>
  <rect x="245" y="80" width="150" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="106" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Persistent</text>
  <text x="320" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yönetilen · takipli</text>
  <rect x="460" y="80" width="150" height="60" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="535" y="106" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Detached</text>
  <text x="535" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bağlam kapandı</text>
  <rect x="245" y="168" width="150" height="44" rx="10" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="320" y="195" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Removed</text>
  <line x1="180" y1="104" x2="243" y2="104" stroke="#5D7789" stroke-width="1.5" marker-end="url(#jpamap-life-ar)"/>
  <text x="211" y="97" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="monospace">persist</text>
  <line x1="395" y1="104" x2="458" y2="104" stroke="#5D7789" stroke-width="1.5" marker-end="url(#jpamap-life-ar)"/>
  <text x="426" y="97" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="monospace">detach/close</text>
  <line x1="458" y1="122" x2="397" y2="122" stroke="#5D7789" stroke-width="1.5" marker-end="url(#jpamap-life-ar)"/>
  <text x="427" y="137" text-anchor="middle" font-size="10.5" fill="#186399" font-family="monospace">merge</text>
  <line x1="320" y1="140" x2="320" y2="166" stroke="#5D7789" stroke-width="1.5" marker-end="url(#jpamap-life-ar)"/>
  <text x="352" y="157" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="monospace">remove</text>
</svg>
```

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="IDENTITY ve SEQUENCE kimlik üretiminin toplu eklemeye etkisi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kimlik üretimi ve toplu ekleme (batch)</text>
  <rect x="24" y="42" width="288" height="150" rx="10" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="168" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="monospace">IDENTITY</text>
  <text x="168" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">id = insert anında (auto-inc)</text>
  <text x="168" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">id'yi hemen bilmek şart</text>
  <text x="168" y="134" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">→ her insert TEK TEK</text>
  <text x="168" y="160" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">JDBC batch DEVRE DIŞI</text>
  <text x="168" y="180" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">büyük import'ta yavaş</text>
  <rect x="328" y="42" width="288" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="monospace">SEQUENCE</text>
  <text x="472" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">diziden allocationSize blok</text>
  <text x="472" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">id'ler uygulamada ön-atanır</text>
  <text x="472" y="134" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">→ insert'ler GRUPLANIR</text>
  <text x="472" y="160" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">JDBC batch ÇALIŞIR</text>
  <text x="472" y="180" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">batch_size ile toplu gönderim</text>
</svg>
```

### Mülakatta ne sorulur?

- **Varlık olmanın asgari şartı.** `@Entity` + `@Id` + parametresiz yapıcı. `final` sınıf olmaz (proxy engellenir), zorunlu temel sınıf ya da `Serializable`/`Comparable` şartı yoktur.
- **IDENTITY mi SEQUENCE mi?** IDENTITY toplu eklemeyi kırar, SEQUENCE ön-ayırma sayesinde korur. "İkisi arasında fark yok" ya da "SEQUENCE her insert için ayrı gider" ifadeleri yanlıştır; SEQUENCE tam tersine bloklar hâlinde alır.
- **Kalıtım stratejisinin takası.** `SINGLE_TABLE` tüm hiyerarşiyi tek tabloda, bir **ayırt edici sütunla** (`@DiscriminatorColumn`) tutar. Polimorfik sorgu JOIN veya UNION gerektirmez; bu yüzden **en hızlı okuma** budur. Bedeli şudur: bir alt sınıfa özgü sütun diğer satırlarda boş kalır, dolayısıyla o sütuna **`NOT NULL` kısıtı konamaz** (tablo seyrekleşir, veri bütünlüğü zayıflar). `JOINED` sınıf başına tablo + yabancı anahtar kullanır: normalize eder, `NOT NULL` mümkün olur, ama **her okuma bir JOIN** ister. `TABLE_PER_CLASS` her somut sınıf için tam tablo tutar: alt sınıf okuması JOIN'siz, ama polimorfik sorgu **UNION** gerektirir.
- **`@Version` ne yapar?** İyimser kilitleme sağlar, **hiçbir veritabanı kilidi tutmaz** ve `SELECT ... FOR UPDATE` üretmez. Her `UPDATE`'e sürüm koşulu eklenir: `UPDATE hesap SET ..., surum = surum + 1 WHERE id = ? AND surum = ?`. İkinci işlemin elindeki sürüm eskiyse `UPDATE` **0 satır** etkiler ve çakışma istisnası fırlar: JPA katmanında `OptimisticLockException`, Spring Data ile sarılmış hâli `ObjectOptimisticLockingFailureException`. Uygulama bunu yakalayıp "veri değişti, tekrar dene" akışı kurar; kilit tutulmadığı için yüksek eşzamanlılıkta pesimist kilitlemeden daha ölçeklenirdir. `int`, `long`, `short`, `Instant`, `Timestamp` tipleriyle çalışır.
- **`save` çağırmadım, ne olur?** Yönetilen varlıktaki değişiklik kirlilik takibiyle yakalanır ve commit anında otomatik `UPDATE` üretilir; istisna fırlamaz, `save` şart değildir.
- **`equals`/`hashCode` tuzağı.** En sağlam yaklaşım **değişmez ve iş açısından benzersiz bir anahtardır** (business key — e-posta, ISBN, sipariş numarası). Otomatik üretilen `id`'ye dayanmak klasik hatadır. `persist` öncesi `id` değeri `null`'dır. Nesneyi bir `HashSet`'e ekleyip sonra kaydedersen `id` atanır ve `hashCode` değişir; nesne kümede **kaybolur**, çünkü artık aynı kovada aranmaz. Varsayılanı hiç ezmemek, detach/merge sonrası "aynı satır, iki örnek" durumunda yanlış eşitsizlik üretir. Tüm alanları katmak ise tembel koleksiyonları yükletir ve alanlar değiştikçe hash'i kararsızlaştırır. Kararlı bir iş anahtarı yoksa, bir `UUID`'yi **uygulama tarafında persist ÖNCESİ** atamak da bir seçenektir.

### Tek cümlede

Eşleme yalnız hangi alanın hangi sütuna gittiği değil; kimliğin ne zaman verildiği (batch), değişikliğin ne zaman yazıldığı (dirty checking), çakışmanın nasıl yakalandığı (`@Version`) ve varlık kimliğinin nasıl tanımlandığı (`equals`/`hashCode`) kararlarının tamamıdır.

# jpamap-1 | Temel
## Soru
Bir sınıfın JPA varlığı (entity) olması için minimum gereksinim aşağıdakilerden hangisidir?
## A
`@Entity` anotasyonu, bir `@Id` alanı ve parametresiz (no-arg) bir yapıcı (constructor).
🤖 Doğru: @Entity + @Id + parametresiz yapıcı temel şarttır.
## B
Sınıfın `Serializable` ve `Comparable` arayüzlerini birlikte implemente etmesi zorunludur.
🤖 Serializable önerilir ama zorunlu değil; Comparable ise hiç gerekmez.
## C
Sınıfın `final` olması ve tüm alanlarının `private final` olarak tanımlanması gerekir.
🤖 Tam tersi: Hibernate tembel yükleme için alt sınıf proxy'si üretir; `final` bunu engeller.
## D
Sınıfın `AbstractEntity` gibi bir Hibernate temel sınıfından türemesi zorunludur.
🤖 Zorunlu bir temel sınıf yoktur; anotasyonlar yeterlidir.
## Doğru
A
## Açıklama
Bir JPA varlığı için üç şey gerekir: sınıfta `@Entity`, bir birincil anahtar alanında `@Id` ve **parametresiz (no-arg) bir yapıcı** (en az `protected` görünürlükte) — Hibernate nesneyi yansımayla (reflection) bu yapıcıdan üretir. `Serializable` iyi bir uygulamadır ama JPA spesifikasyonu için zorunlu değildir, `Comparable` ise alakasızdır (B). Varlık `final` OLMAMALIDIR; Hibernate tembel yükleme (lazy loading) için alt sınıf proxy'si üretir ve `final` bunu engeller (C). Zorunlu bir temel sınıf da yoktur (D) — `@MappedSuperclass` isteğe bağlı bir kolaylıktır.

# jpamap-2 | Orta
## Soru
Bir toplu içe aktarımda (bulk import) on binlerce satır ekleyeceksin ve JDBC toplu ekleme (batch insert) istiyorsun. Kimlik üretiminde `GenerationType.IDENTITY` ile `SEQUENCE` arasındaki kritik fark nedir?
## A
İkisi arasında toplu ekleme açısından fark yoktur; Hibernate her ikisinde de aynı batch'i uygular.
🤖 Fark belirgindir: IDENTITY batch insert'i engeller, SEQUENCE engellemez.
## B
`SEQUENCE` her `insert` için veritabanına ayrı gidip tek tek değer aldığından toplu eklemeyi imkânsız kılar.
🤖 Tam tersi: SEQUENCE `allocationSize` ile önceden blok halinde değer ayırıp batch'e izin verir.
## C
`IDENTITY` her ortamda taşınabilir tek stratejidir; `SEQUENCE` yalnızca Oracle'da çalışır.
🤖 Modern veritabanları (PostgreSQL, H2...) dizi destekler; taşınabilirlik iddiası yanlış.
## D
`IDENTITY` JDBC toplu eklemeyi devre dışı bırakır; `SEQUENCE` ön-ayırma sayesinde korur.
🤖 Doğru: IDENTITY batch'i kırar, SEQUENCE ön-ayırma sayesinde korur.
## Doğru
D
## Açıklama
`GenerationType.IDENTITY` ile `id`, satır veritabanına yazıldığı **anda** (auto-increment) üretilir. Hibernate, kalıcı bağlamdaki bir nesnenin `id`'sini hemen bilmek zorunda olduğundan, her `insert`'i tek tek çalıştırmak zorunda kalır ve **JDBC toplu eklemeyi devre dışı bırakır** — büyük içe aktarımlarda ciddi yavaşlık. `GenerationType.SEQUENCE` ise bir veritabanı dizisinden `allocationSize` kadar değeri **önceden blok halinde ayırır**; Hibernate `id`'leri uygulama tarafında atayabildiğinden `insert`'leri gruplayıp toplu (batch) gönderebilir. Bu yüzden toplu ekleme yükünde `SEQUENCE` (uygun `allocationSize` ve `hibernate.jdbc.batch_size` ile) tercih edilir. Modern veritabanları diziyi destekler (C yanlış).

# jpamap-3 | Orta
## Soru
Bir `Odeme` üst sınıfının `KrediKarti` ve `Havale` alt sınıfları var. `@Inheritance(strategy = SINGLE_TABLE)` seçersen en belirgin takas (trade-off) nedir?
## A
Her alt sınıf ayrı bir tabloya yazılır ve okuma sırasında hiçbir zaman JOIN gerekmez.
🤖 O tarif TABLE_PER_CLASS'a yakın; SINGLE_TABLE tek tablo kullanır.
## B
Tüm hiyerarşi tek tabloda ayırt edici (discriminator) sütunla tutulur; alt sınıf sütunları `NOT NULL` olamaz.
🤖 Doğru: tek tablo hızlı okuma verir ama zorunlu-olmayan seyrek sütunlar getirir.
## C
Her alt sınıf ayrı tabloya yazılır ve üst sınıf tablosuyla yabancı anahtar (FK) üzerinden birleştirilir; her okuma bir JOIN gerektirir.
🤖 Tarif edilen tam olarak JOINED stratejisidir, SINGLE_TABLE değil.
## D
Polimorfik sorgular her zaman `UNION` üretir ve bu yüzden en yavaş stratejidir.
🤖 UNION ihtiyacı TABLE_PER_CLASS'ta doğar; SINGLE_TABLE'da tek tablo olduğundan UNION yoktur.
## Doğru
B
## Açıklama
`SINGLE_TABLE` (varsayılan strateji) tüm hiyerarşiyi **tek bir tabloda** tutar; hangi satırın hangi alt sınıf olduğunu bir **ayırt edici sütun** (`@DiscriminatorColumn`) belirtir. Avantaj: polimorfik sorgular JOIN veya UNION gerektirmez, **en hızlı okuma**. Dezavantaj: bir alt sınıfa özgü sütun, diğer alt sınıfların satırlarında anlamsızdır ve boş kalır — bu yüzden **`NOT NULL` kısıtı konamaz** (veri bütünlüğü zayıflar, tablo seyrekleşir). JOINED (C'de tarif edilen) normalize eder ama her okumada JOIN ister; TABLE_PER_CLASS (A/D'ye karışan) polimorfik sorguda UNION gerektirir. Seçim, okuma hızıyla normalizasyon arasındaki takastır.

# jpamap-4 | İleri
## Soru
Bir `Hesap` varlığına `@Version private long surum;` alanı ekledin. İki eşzamanlı işlem (transaction) aynı hesabı okuyup güncellemeye çalışırsa ne olur?
## A
`@Version`, satırı okurken kilitler ve ikinci işlemi ilk işlem bitene kadar bloklar (pesimist davranış).
🤖 O pesimist kilitlemedir; @Version kilit tutmaz, çakışmayı yazmada yakalar.
## B
`@Version` her okumada `SELECT ... FOR UPDATE` üretir; bu yüzden okuma da yazma da serileşir.
🤖 @Version FOR UPDATE üretmez; ekstra SELECT kilidi getirmez.
## C
`UPDATE`'e sürüm koşulu eklenir; 0 satır etkilenirse iyimser kilit istisnası fırlar.
🤖 Doğru: sürüm eşleşmezse güncelleme boşa gider ve iyimser kilit istisnası atılır.
## D
`@Version` yalnız `java.sql.Timestamp` tipiyle çalışır; `long` alan sessizce yok sayılır.
🤖 @Version int/long/short/Instant/Timestamp ile çalışır; long geçerlidir.
## Doğru
C
## Açıklama
`@Version` **iyimser kilitleme** (optimistic locking) sağlar — hiçbir veritabanı kilidi tutmaz. Her `UPDATE`'te Hibernate koşula sürümü ekler: `UPDATE hesap SET ..., surum = surum + 1 WHERE id = ? AND surum = ?`. İlk işlem başarıyla güncelleyip sürümü artırınca, ikinci işlemin elindeki sürüm artık eskidir; onun `UPDATE`'i **0 satır** etkiler ve Hibernate bunu çakışma olarak yorumlayıp bir iyimser kilit istisnası fırlatır: JPA katmanında `OptimisticLockException`, Spring Data kullanıyorsan ona sarılmış hâli `ObjectOptimisticLockingFailureException`. Uygulama bunu yakalayıp "veri değişti, tekrar dene" akışı kurar. Bu, kilit tutmadığından yüksek eşzamanlılıkta pesimist kilitlemeye (A/B) göre daha ölçeklenirdir. `@Version` `int/long/short/Instant/Timestamp` gibi tiplerle çalışır (D yanlış).

# jpamap-5 | İleri
## Soru
Bir `@Transactional` metot içinde `Musteri m = repo.findById(id).get(); m.setAd("Yeni");` yazdın ama `repo.save(m)` ÇAĞIRMADIN. Metot bittiğinde ne olur?
## A
Kirlilik takibi (dirty checking) değişikliği yakalar; commit anında otomatik `UPDATE` üretilir.
🤖 Doğru: persistent varlıktaki değişiklik flush/commit anında otomatik yazılır.
## B
Hiçbir değişiklik veritabanına yazılmaz; `save` çağrılmadıkça Hibernate `UPDATE` üretmez.
🤖 save şart değil; yönetilen varlıktaki değişiklik zaten flush'ta yazılır.
## C
Değişiklik yalnız `@DynamicUpdate` varsa yazılır; bu anotasyon yoksa kirlilik takibi devre dışıdır.
🤖 @DynamicUpdate yalnız hangi sütunların güncelleneceğini etkiler; takibi açıp kapatmaz.
## D
Çalışma zamanında istisna fırlar; `save` çağrılmadan yönetilen bir varlık değiştirilemez.
🤖 Böyle bir istisna yoktur; yönetilen varlığı değiştirmek serbesttir.
## Doğru
A
## Açıklama
`findById` ile dönen varlık, açık transaction boyunca **kalıcı bağlamda** (persistence context) yönetilir. Hibernate, bağlama giren varlıkların bir anlık görüntüsünü (snapshot) tutar ve `flush` anında (genellikle commit'ten hemen önce) mevcut durumla karşılaştırır — **kirlilik takibi**. Bir alan değişmişse otomatik `UPDATE` üretir; bu yüzden `repo.save(m)` çağırmak **gereksizdir** (zaten yönetilen varlıkta `save` çoğunlukla no-op'a yakındır). `@DynamicUpdate` yalnız `UPDATE`'e hangi sütunların konacağını (tümü mü, yalnız değişenler mi) etkiler; takibi açıp kapatmaz (C). Dikkat: bu davranış varlık **detached** ise (bağlam kapandıysa) geçerli değildir — o durumda `merge`/`save` gerekir.

# jpamap-6 | İleri
## Soru
Bir JPA varlığında `equals`/`hashCode`'u nasıl uygulamak en güvenlidir?
## A
Otomatik üretilen `id` alanına dayandırmak her zaman en güvenlisidir; iki varlık aynı `id`'ye sahipse eşittir.
🤖 Tehlikeli: persist öncesi id null'dır; nesne kümeye eklendikten sonra id atanınca hash değişir.
## B
Hiç ezme; `Object`'in kimlik tabanlı varsayılan `equals`/`hashCode`'u tüm senaryolarda yeterlidir.
🤖 Kimlik tabanlı eşitlik, detach/merge veya aynı satırın iki örneği durumunda yanlış sonuç verir.
## C
Tüm alanları (tembel/lazy koleksiyonlar dahil) `equals`'e katmak en kapsamlı ve doğru yaklaşımdır.
🤖 Lazy koleksiyonu equals'te dokunmak yüklemeyi tetikler ve alanlar değiştikçe hash kararsızlaşır.
## D
Değişmez bir iş anahtarına (business key — ör. e-posta, ISBN) dayandırmak en güvenlidir.
🤖 Doğru: kararlı, değişmez bir iş anahtarı hash'i sabit tutar.
## Doğru
D
## Açıklama
JPA varlıklarında `equals`/`hashCode` için en sağlam yaklaşım, **değişmez ve iş açısından benzersiz bir anahtar** (business key — e-posta, ISBN, sipariş numarası gibi) kullanmaktır. Otomatik üretilen `id`'ye dayanmak (A) klasik tuzaktır: `persist` öncesi `id` `null`'dır; varlığı bir `HashSet`'e ekleyip sonra kaydedersen `id` atanır, `hashCode` değişir ve nesne kümede **kaybolur** (aynı kova bulunamaz). Varsayılanı hiç ezmemek (B) detach/merge sonrası "aynı satır, iki örnek" durumunda eşitsizlik üretir. Tüm alanları katmak (C) tembel koleksiyonları yükletir ve alanlar değiştikçe hash'i kararsızlaştırır. Kararlı bir iş anahtarı yoksa, dikkatli kullanılan bir `UUID`'yi **uygulama tarafında persist ÖNCESİ** atamak da bir seçenektir.
