---
slug: jpa-iliskiler-nplusbir
baslik: "JPA İlişkileri ve N+1 Problemi: Teşhis, Çözümler ve Sayfalama Tuzağı"
kademe: İleri
sira: 82
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Markete gidip 100 ürünün fiyatını öğrenmek istiyorsun. Bir yol var: listeyi alıp her ürün için ayrı ayrı kasaya gidip sormak — 101 gidiş-geliş. Bir yol daha var: listeyi verip hepsinin fiyatını **tek seferde** almak. Yazdığın kod aynı görünür, ama biri saniyeler, öteki milisaniyeler sürer. Veritabanı ilişkilerinde de tam olarak bu durum yaşanır: iki nesne birbirine bağlıysa (sipariş ve kalemleri gibi) bağlı verinin **ne zaman** ve **kaç sorguyla** getirileceği kararı, uygulamanın hızını belirler. Bu konu o kararı ve en meşhur tuzağını anlatır.

### Temel kavramlar

- **İlişki (association):** İki varlığın birbirine bağlanması. Bir siparişin kalemleri, bir kalemin siparişi.
- **`@ManyToOne`:** Çok kayıt tek kayda bakar (çok kalem → bir sipariş). Yabancı anahtarı (foreign key) taşıyan taraf genelde budur.
- **`@OneToMany`:** Bir kayıt çok kayda bakar (bir sipariş → çok kalem). Java tarafında bir **koleksiyondur** (`List`, `Set`).
- **`@OneToOne` / `@ManyToMany`:** Bire bir ve çoka çok bağlar.
- **`mappedBy`:** İlişkinin **sahibinin karşı taraf** olduğunu söyler; yabancı anahtarı o taraf yönetir, bu taraf yalnız ayna görüntüsüdür.
- **Yükleme stratejisi (fetch type):** Bağlı verinin ne zaman okunacağı. **EAGER** = hevesli (peşinen) yükleme; bağlı veri ana varlıkla birlikte hemen gelir. **LAZY** = tembel (ihtiyaç olunca) yükleme; bağlı veri ancak o alana erişilince gelir.
- **Kalıcı bağlam (persistence context):** Hibernate'in varlıkları takip ettiği, işlem (transaction) boyunca açık kalan defter. Tembel yükleme ancak bu defter açıkken çalışır. Gerçek nesnenin yerine geçen **vekil nesne (proxy)** asıl veriyi bu defterden çeker.
- **DTO (Data Transfer Object):** Varlığın kendisi yerine, dışarı verilecek alanları taşıyan sade nesne.
- **N+1 problemi:** Tek sorgu yerine N ek sorgu atma sorunu. Bir liste sorgusu (1) + listedeki her kayıt için bir ek sorgu (N). 100 sipariş → 101 sorgu. Bu, satır sayısıyla değil **sorgu sayısıyla** ilgili bir **kötü örüntüdür (anti-pattern)**.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="N+1: bir liste sorgusu artı her kayıt için bir ek sorgu">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">N+1: gizli sorgu yağmuru</text>
  <rect x="30" y="40" width="230" height="150" rx="10" fill="#FBECEC" stroke="#B4442E" stroke-width="1.5"/>
  <text x="145" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#B4442E" font-family="system-ui">Naif: 1 + N sorgu</text>
  <text x="145" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">SELECT * FROM siparis  (1)</text>
  <text x="145" y="108" text-anchor="middle" font-size="10.5" fill="#B4442E" font-family="monospace">SELECT ... kalem WHERE sid=1</text>
  <text x="145" y="126" text-anchor="middle" font-size="10.5" fill="#B4442E" font-family="monospace">SELECT ... kalem WHERE sid=2</text>
  <text x="145" y="144" text-anchor="middle" font-size="10.5" fill="#B4442E" font-family="monospace">... (N kez)</text>
  <text x="145" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">100 sipariş → 101 sorgu</text>
  <text x="300" y="120" text-anchor="middle" font-size="20" fill="#25804C" font-family="system-ui">→</text>
  <rect x="340" y="40" width="270" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Çözüm seçenekleri</text>
  <text x="475" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">JOIN FETCH — tek sorguda getir</text>
  <text x="475" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@EntityGraph — bildirimsel fetch</text>
  <text x="475" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@BatchSize — IN(...) ile grupla</text>
  <text x="475" y="146" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">DTO projeksiyonu — sadece gerekeni</text>
  <text x="475" y="170" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Duruma göre seç; hepsi aynı değil</text>
</svg>
```

### Nasıl çalışır?

JPA'nın varsayılanları **asimetriktir** ve bu asimetri sık şaşırtır: tekil taraf — `@ManyToOne` ve `@OneToOne` — varsayılan **EAGER**; koleksiyon tarafı — `@OneToMany` ve `@ManyToMany` — varsayılan **LAZY**. Deneyimli geliştiriciler `@ManyToOne`'ları da `fetch = FetchType.LAZY` yaparak kontrolü ele alır, çünkü varsayılan EAGER gizli N+1 ve aşırı yükleme (over-fetch) kaynağıdır.

N+1 zinciri şöyle kurulur:

```java
List<Siparis> hepsi = repo.findAll();      // 1 sorgu: SELECT * FROM siparis
for (Siparis s : hepsi) {
  s.getKalemler().size();                  // her tur: SELECT ... WHERE sid = ?
}                                          // toplam 1 + N sorgu
```

İlk sorgu N siparişi getirir; sonra döngüde tembel koleksiyona her dokunuşta Hibernate **ayrı** bir `SELECT` atar. 100 sipariş için 101 ağ gidiş-dönüşü (round trip) uygulamayı boğar. Teşhis için SQL loglarını izlemek şarttır (`show-sql`, `hibernate.generate_statistics`).

Dört çözüm var. **`JOIN FETCH`** ilişkiyi ana varlıkla **aynı SQL sorgusunda** yükler: `@Query("SELECT s FROM Siparis s JOIN FETCH s.kalemler")`. Böylece 1 + N sorgu tek sorguya iner. Bu bir JPQL/HQL yapısıdır; `nativeQuery` gerektirmez ve koleksiyonlarda da çalışır. İlişkiyi o sorgu için gerçekten hevesle (peşinen) yükler. **`@EntityGraph`** aynı fetch join'i bildirimsel olarak kurar. **`@BatchSize`** N ayrı sorgu yerine `IN (...)` ile gruplanmış birkaç sorgu atar. **DTO projeksiyonu** ise varlığı hiç yüklemeden yalnız gereken sütunları çeker.

Bir koleksiyonu fetch ederken SQL sonucunda üst (parent) satırlar çocuk sayısı kadar **çoğalır** (kartezyen etki). Sürüm notu: **Hibernate 6** (Spring Boot 3) dönen kök varlıkları otomatik tekilleştirdiği için JPQL'de artık `SELECT DISTINCT` yazmak gerekmez — Hibernate 5'te gerekiyordu ve eski kaynaklar hâlâ öyle anlatır.

Asıl kıdemli geliştirici tuzağı ise koleksiyonu `JOIN FETCH` edip **aynı anda sayfalama** (pagination) istemektir. Hibernate `LIMIT`'i çoğalmış satırlara uygulayamaz (tek bir siparişin 10 kalemi tüm sayfayı doldurabilirdi); bu yüzden **tüm** sonucu çekip sayfalamayı bellekte yapar ve `HHH000104` uyarısını basar — bellek patlaması riski. Aynı tuzak `@EntityGraph` ile koleksiyon getirirken de geçerlidir, çünkü o da arka planda fetch join üretir. Sayfalamayı SQL'de tutmak istiyorsan koleksiyonu fetch etmeyip `@BatchSize` kullan ya da önce sayfalı `id`'leri çek, sonra `WHERE id IN (...)` ile fetch et.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İlişki tipine göre varsayılan LAZY ve EAGER yükleme">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Varsayılan fetch: asimetrik</text>
  <rect x="24" y="42" width="288" height="150" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="168" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Tekil taraf → EAGER</text>
  <text x="168" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">@ManyToOne</text>
  <text x="168" y="114" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">@OneToOne</text>
  <text x="168" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">varlıkla birlikte hemen yüklenir</text>
  <text x="168" y="164" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">gizli N+1 / aşırı yükleme kaynağı</text>
  <text x="168" y="182" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">pratikte LAZY'e çekilir</text>
  <rect x="328" y="42" width="288" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Koleksiyon → LAZY</text>
  <text x="472" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">@OneToMany</text>
  <text x="472" y="114" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">@ManyToMany</text>
  <text x="472" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ancak erişilince yüklenir</text>
  <text x="472" y="164" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">döngüde erişim → N+1</text>
  <text x="472" y="182" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">JOIN FETCH / EntityGraph ile getir</text>
</svg>
```

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Koleksiyon JOIN FETCH ile sayfalama tuzağı ve bellekte sayfalama">
  <defs>
    <marker id="nplus-pag-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#8A2E2E"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">JOIN FETCH + Pageable tuzağı</text>
  <rect x="30" y="44" width="250" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="155" y="68" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">JOIN FETCH kalemler</text>
  <text x="155" y="88" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">üst satırlar çoğalır (kartezyen)</text>
  <line x1="280" y1="74" x2="358" y2="74" stroke="#8A2E2E" stroke-width="1.6" marker-end="url(#nplus-pag-ar)"/>
  <rect x="362" y="44" width="252" height="60" rx="10" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="488" y="68" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">LIMIT çoğalmış satıra vurulamaz</text>
  <text x="488" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">→ SQL'de sayfalama YOK</text>
  <rect x="120" y="122" width="400" height="44" rx="10" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="320" y="142" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="monospace">HHH000104: applying in memory</text>
  <text x="320" y="159" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">TÜM sonuç çekilir, bellekte sayfalanır → OOM riski</text>
  <rect x="60" y="180" width="520" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="320" y="201" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Çözüm: önce sayfalı id'leri çek → sonra WHERE id IN (...) JOIN FETCH</text>
</svg>
```

### Mülakatta ne sorulur?

- **Varsayılan fetch nedir?** "Hepsi EAGER" da "hepsi LAZY" de yanlıştır; tekil taraf EAGER, koleksiyon tarafı LAZY'dir ve `fetch` belirtmek zorunlu değildir.
- **N+1 tam olarak nedir?** Sorgu **sayısıyla** ilgilidir: 1 liste sorgusu + her kayıt için 1 ek sorgu. Tek sorgunun fazla satır döndürmesi (kartezyen çoğalma) ya da prepared statement'ın yeniden derlenmesi değildir. Hem EAGER hem LAZY ilişkilerde oluşabilir — "yalnız EAGER'da olur" yanlıştır, çünkü klasik kaynak döngüde LAZY ilişkiye erişmektir.
- **`JOIN FETCH` ne yapar?** İlişkiyi tek sorguda yükleyip ek `SELECT`'leri kaldırır. JPQL/HQL yapısıdır (native gerekmez), koleksiyonlarda da çalışır ve N+1'i gerçekten çözer — "yalnız SQL'i okunur kılar" yanlıştır.
- **`HHH000104` uyarısını görürsen?** `firstResult/maxResults specified with collection fetch; applying in memory` — `LIMIT` çoğalmış satırlara vurulamayacağından Hibernate **TÜM** sonucu çekip sayfalamayı **bellekte** yapıyordur. Derleme hatası değildir, sessizce çalışır; küçük veride fark edilmez, büyük tabloda `OutOfMemoryError` getirir. Bilgilendirme değil, ciddi bir performans/bellek riskidir. Çözüm: (1) iki adımlı sorgu — önce sayfalı `id`'leri fetch'siz çek, sonra `WHERE id IN (...)` ile `JOIN FETCH`; ya da (2) koleksiyonu hiç fetch etmeyip `@BatchSize` ile az sayıda ek sorguda yükle. `@EntityGraph`'a geçmek bu tuzağı **çözmez**.
- **`LazyInitializationException`nın kök nedeni?** Tembel bir ilişkiye **kalıcı bağlam kapandıktan sonra** erişmek — tipik olarak `@Transactional` servis metodu bittikten sonra controller ya da JSON serileştirici `getKalemler()`'e dokununca. Bağlantı havuzu (connection pool) boyutu, sürücü ya da JVM hatası değildir; `@Transactional` içinde olmak da tek başına garanti vermez. Sağlıklı çözüm veriyi bağlam **açıkken** yüklemektir: `JOIN FETCH`, `@EntityGraph` ya da en temizi varlığı hiç dışarı sızdırmadan **DTO'ya projeksiyon**. Her şeyi EAGER yapmak aşırı yükleme ve yeni N+1 doğurur. Open-Session-In-View (OSIV — oturumu istek bitene kadar açık tutma ayarı) istisnayı gizler; ama işlem sınırlarını bulanıklaştırdığı ve gizli sorgu ürettiği için önerilmez.
- **`cascade = REMOVE` ile `orphanRemoval = true` farkı?** Tetikleyicileri farklıdır. `cascade = REMOVE`, **üst varlık silindiğinde** silmeyi alt kayıtlara yayar. `orphanRemoval = true` ise bir alt kayıt **koleksiyondan çıkarıldığında** devreye girer (`siparis.getKalemler().remove(kalem)`); o "öksüz" kaydı üst varlık silinmese bile siler. Eş anlamlı değildirler, birlikte yazmak derleme hatası değildir ve `cascade` tek başına eleman çıkarmayı silmeye çevirmez. Çocuğun varlığı tümüyle üst nesneye bağlıysa (aggregate root / composition) pratikte ikisi birlikte kullanılır: `cascade = ALL, orphanRemoval = true`.

### Tek cümlede

İlişkilerde asıl soru "veri geliyor mu" değil, **kaç sorguyla ve bağlam açıkken mi** geldiğidir; N+1'i `JOIN FETCH`/`@EntityGraph`/`@BatchSize`/DTO ile çöz, ama koleksiyon fetch'ini sayfalamayla birleştirme.

# nplus-1 | Temel
## Soru
JPA'da ilişkilerin varsayılan yükleme (fetch) stratejisi nedir?
## A
Tüm ilişkiler varsayılan olarak EAGER'dır; performans için elle LAZY'e çevrilmelidir.
🤖 koleksiyon ilişkileri (@OneToMany/@ManyToMany) varsayılan LAZY'dir.
## B
`@ManyToOne` ve `@OneToOne` EAGER; `@OneToMany` ve `@ManyToMany` LAZY'dir.
🤖 Doğru: "tekil" taraf EAGER, "çoğul" (koleksiyon) taraf LAZY varsayılır.
## C
Tüm ilişkiler varsayılan olarak LAZY'dir; hiçbiri açılışta yüklenmez.
🤖 @ManyToOne ve @OneToOne varsayılan EAGER'dır; hepsi LAZY değil.
## D
JPA'da varsayılan yoktur; her ilişkide `fetch` belirtmek derleyici tarafından zorunlu kılınır.
🤖 Varsayılanlar bellidir ve fetch belirtmek zorunlu değildir.
## Doğru
B
## Açıklama
JPA spesifikasyonunun varsayılanları asimetriktir ve bu asimetri mülakatta sık sorulur: **tekil taraf** — `@ManyToOne` ve `@OneToOne` — varsayılan **EAGER** (varlıkla birlikte hemen yüklenir); **koleksiyon tarafı** — `@OneToMany` ve `@ManyToMany` — varsayılan **LAZY** (ancak erişildiğinde yüklenir). Pratikte deneyimli geliştiriciler `@ManyToOne`'ları da `fetch = FetchType.LAZY` yaparak kontrolü ele alır, çünkü varsayılan EAGER gizli N+1 ve aşırı yükleme (over-fetch) kaynağıdır. "Hepsi EAGER" (A) veya "hepsi LAZY" (C) yanlıştır; varsayılan da vardır (D).

# nplus-2 | Orta
## Soru
"N+1 sorgu problemi" tam olarak nedir?
## A
Tek bir sorgunun beklenenden çok fazla satır (N+1 satır) döndürmesidir; JOIN'in kartezyen çarpım üretmesidir.
🤖 O ayrı bir sorun (satır çoğalması); N+1 sorgu SAYISIyla ilgilidir.
## B
Aynı prepared statement'ın N+1 kez yeniden derlenmesidir; sorgu planı önbelleğe alınmadığında olur.
🤖 N+1, ifade derlemesiyle değil, atılan ayrı sorgu sayısıyla ilgilidir.
## C
1 sorgu ana listeyi çeker; sonra her kayıt için tembel ilişki ayrı sorguyla yüklenir.
🤖 Doğru: bir üst sorgu artı her kayıt için bir ek sorgu.
## D
N+1 yalnız EAGER ilişkilerde oluşur; LAZY kullanıldığında asla ortaya çıkmaz.
🤖 Tam tersi de olur: LAZY ilişkiye döngüde erişmek klasik N+1 kaynağıdır.
## Doğru
C
## Açıklama
N+1, **sorgu sayısıyla** ilgili bir anti-pattern'dir. İlk sorgu ana koleksiyonu getirir (`SELECT * FROM siparis` → N sipariş). Sonra kod bu siparişlerin her birinin tembel (lazy) ilişkisine (`siparis.getKalemler()`) döngüde erişir; Hibernate her sipariş için **ayrı** bir `SELECT` atar → N ek sorgu. Toplam **1 + N**. 100 sipariş için 101 sorgu — ağ gidiş-dönüşleri (round trip) uygulamayı boğar. Bu, satır çoğalması (A) veya ifade yeniden derlemesi (B) değildir. Hem EAGER (döngüsel/koleksiyon EAGER'da otomatik) hem LAZY (döngüde erişimle) ilişkilerde oluşabilir; "yalnız EAGER'da" (D) yanlıştır. Teşhis için SQL loglarını (`show-sql`, `hibernate.generate_statistics`) izlemek şarttır.

# nplus-3 | Orta
## Soru
N+1'i çözmek için `@Query("SELECT s FROM Siparis s JOIN FETCH s.kalemler")` kullanmanın etkisi nedir?
## A
İlişkiyi ana varlıkla aynı sorguda getirir; 1 + N sorgu tek sorguya iner.
🤖 Doğru: JOIN FETCH ilişkiyi tek sorguda yükleyerek ek sorguları ortadan kaldırır.
## B
`JOIN FETCH` yalnız `nativeQuery = true` ile çalışır; JPQL'de sözdizimi hatası verir.
🤖 JOIN FETCH bir JPQL/HQL yapısıdır; native gerektirmez.
## C
`JOIN FETCH` yalnız `@ManyToOne` ilişkilerde kullanılabilir; koleksiyonlarda desteklenmez.
🤖 Koleksiyonlar da JOIN FETCH edilebilir; böyle bir tip kısıtı yoktur.
## D
`JOIN FETCH` tembel ilişkiyi eager'a çevirmez; bu yüzden N+1'i çözmez, yalnız SQL'i okunur kılar.
🤖 JOIN FETCH ilişkiyi o sorgu için hevesle yükler ve N+1'i çözer.
## Doğru
A
## Açıklama
`JOIN FETCH`, ilişkiyi ana varlıkla **aynı SQL sorgusunda** yükler; Hibernate ek `SELECT`'ler atmaz, böylece 1 + N sorgu **tek** birleştirilmiş sorguya iner. Bu, N+1'in en doğrudan çözümüdür. `JOIN FETCH` bir JPQL/HQL yapısıdır, `nativeQuery` gerektirmez (B) ve koleksiyon ilişkilerinde de kullanılabilir (C yanlış). Bir koleksiyonu (`@OneToMany`) fetch ederken SQL sonucunda üst (parent) satırlar çocuk sayısı kadar **çoğalır** (kartezyen etki). Önemli sürüm notu: **Hibernate 6** (Spring Boot 3) dönen kök varlıkları otomatik tekilleştirdiği için JPQL'de artık `SELECT DISTINCT` yazmak gerekmez — Hibernate 5'te gerekiyordu ve eski kaynaklar hâlâ öyle anlatır. JOIN FETCH ilişkiyi o sorgu için hevesle (eagerly) yükler ve N+1'i gerçekten çözer (D yanlış). Alternatifler: `@EntityGraph` (bildirimsel), `@BatchSize` (IN'li gruplama) ve DTO projeksiyonu (yalnız gerekli sütunlar).

# nplus-4 | İleri
## Soru
`@Query("SELECT s FROM Siparis s JOIN FETCH s.kalemler")` sorgusuna `Pageable` parametresi ekledin. Hibernate loglarında `HHH000104: firstResult/maxResults specified with collection fetch; applying in memory` uyarısını görüyorsun. Ne oluyor?
## A
`Pageable` ile koleksiyon `JOIN FETCH` sorunsuz birleşir; sayfalama tümüyle SQL'de `LIMIT`/`OFFSET` ile yapılır.
🤖 Uyarının söylediği tam tersi: LIMIT SQL'de uygulanamıyor, bellekte yapılıyor.
## B
Koleksiyon fetch'i ile sayfalama birlikte kullanılırsa uygulama açılışta derleme hatası verip kapanır.
🤖 Derleme hatası değil; çalışır ama tehlikeli biçimde bellekte sayfalar.
## C
Sayfalama otomatik olarak üst (parent) satır sayısına göre SQL'de uygulanır; uyarı yalnız bilgilendirmedir, risk yoktur.
🤖 Sayfalama SQL'de uygulanmaz; TÜM sonuç çekildiğinden bellek riski gerçektir.
## D
`LIMIT` çoğalmış satırlara vurulamayacağından Hibernate TÜM sonucu çekip sayfalamayı BELLEKTE yapar.
🤖 Doğru: LIMIT çoğalmış satırlara vurulamayacağından Hibernate hepsini çekip bellekte sayfalar.
## Doğru
D
## Açıklama
Bir koleksiyonu `JOIN FETCH` ettiğinde üst satırlar çocuk sayısı kadar çoğalır. `LIMIT 10`'u bu çoğalmış SQL sonucuna uygulamak, yanlış sayıda üst varlık döndürürdü (örneğin tek bir siparişin 10 kalemi tüm sayfayı doldurabilir). Hibernate bu tehlikeyi bildiğinden `LIMIT`'i SQL'de uygulamaz; **tüm** sonucu çekip sayfalamayı **bellekte** yapar ve `HHH000104` uyarısını basar. Küçük veride sessizce çalışır ama büyük tabloda bellek patlaması (`OutOfMemoryError`) getirir. Çözüm: (1) iki adımlı sorgu — önce sayfalı `id`'leri çek (fetch'siz), sonra `WHERE id IN (...)` ile `JOIN FETCH`; ya da (2) koleksiyonu hiç fetch etmeyip `@BatchSize` ile az sayıda ek sorguda yüklemek — böylece sayfalama SQL'de kalır. Dikkat: `@EntityGraph` ile koleksiyon getirmek bu tuzağı **çözmez**, çünkü o da fetch join üretir ve aynı `HHH000104` davranışına yol açar. Uyarı bilgilendirme değil, ciddi bir performans/bellek riskidir (C yanlış).

# nplus-5 | İleri
## Soru
Bir REST controller'da varlığı serileştirirken (JSON'a çevirirken) `LazyInitializationException` alıyorsun. Kök neden ve en sağlıklı çözüm nedir?
## A
İstisna veritabanı bağlantısının kopmasından kaynaklanır; bağlantı havuzu (connection pool) boyutunu artırmak çözer.
🤖 Sorun bağlantı kopması değil; kapanmış persistence context'e erişimdir.
## B
Kök neden bağlamın kapanmasıdır; en sağlıklı çözüm tüm ilişkileri EAGER yapıp tembel yüklemeyi kaldırmaktır.
🤖 Kök neden doğru ama çözüm yanlış: her şeyi EAGER yapmak aşırı yükleme ve yeni N+1 üretir.
## C
Persistence context KAPANDIKTAN sonra tembel bir ilişkiye erişilmiştir; veri bağlam açıkken yüklenmelidir.
🤖 Doğru: bağlam kapandığı için istisna fırlar; çözüm veriyi bağlam açıkken yüklemektir.
## D
`@Transactional` içindeysen bu istisna asla oluşmaz; oluşuyorsa altta bir JVM veya sürücü hatası vardır.
🤖 Transaction bittikten sonra (ör. controller/serileştirme aşamasında) context kapanır ve istisna oluşabilir.
## Doğru
C
## Açıklama
`LazyInitializationException`, bir tembel (lazy) ilişkiye **persistence context kapandıktan sonra** eriştiğinde fırlar — tipik olarak `@Transactional` servis metodu bittikten sonra, controller veya JSON serileştirici varlığın `getKalemler()`'ine dokununca. Kök neden bağlantı havuzu (A) veya JVM (D) değil, **bağlamın kapanmış olmasıdır**. Sağlıklı çözüm, ihtiyaç duyulan veriyi bağlam **açıkken** yüklemektir: `JOIN FETCH`, `@EntityGraph`, ya da en temizi — varlığı hiç dışarı sızdırmadan doğrudan bir **DTO'ya projeksiyon** yapmak. Her şeyi EAGER yapmak (B) aşırı yükleme ve yeni N+1 doğurur; Open-Session-In-View (OSIV) ise istisnayı gizler ama transaction sınırlarını bulanıklaştırdığı ve gizli sorgu ürettiği için önerilmez.

# nplus-6 | İleri
## Soru
`@OneToMany(mappedBy = "siparis")` koleksiyonunda `cascade = REMOVE` ile `orphanRemoval = true` arasındaki fark nedir?
## A
İkisi aynıdır; `cascade = REMOVE`, üst (parent) silinince alt (child) kayıtları KORUR, `orphanRemoval` ise siler.
🤖 cascade=REMOVE child'ı korumaz; parent silinince child'ları da siler.
## B
`cascade = REMOVE` üst kayıt silinince alt kayıtları siler; `orphanRemoval` ilişki koptuğunda siler.
🤖 Doğru: biri parent silinince, diğeri ilişki koptuğunda child'ı siler — farklı tetikleyiciler.
## C
`orphanRemoval = true` ile `cascade = REMOVE` tamamen eş anlamlıdır; ikisini birlikte yazmak derleme hatasıdır.
🤖 Eş anlamlı değiller ve birlikte yazmak hata değildir; tetikleyicileri farklıdır.
## D
`cascade` zaten "koleksiyondan eleman çıkarınca otomatik silme" demektir; `orphanRemoval` gereksiz bir tekrardır.
🤖 O tanım orphanRemoval'a aittir; cascade eleman çıkarmayı tek başına silmeye çevirmez.
## Doğru
B
## Açıklama
İki mekanizma farklı olayları tetikler: `cascade = REMOVE`, **üst (parent) varlık silindiğinde** silme işlemini alt (child) kayıtlara yayar. `orphanRemoval = true` ise bir alt kaydın **koleksiyondan çıkarılması** (ilişkinin kopması — `siparis.getKalemler().remove(kalem)`) durumunda, o "öksüz" (orphan) kaydı veritabanından siler; üst varlık silinmese bile. Yani `orphanRemoval`, çocuğun varlığı tümüyle üst nesneye bağlı olduğu (aggregate root / composition) modellerde uygundur. İkisi eş anlamlı değildir (C) ve `cascade` tek başına eleman çıkarmayı silmeye çevirmez (D); pratikte güçlü sahiplik (ownership) için ikisi birlikte (`cascade = ALL, orphanRemoval = true`) kullanılır.
