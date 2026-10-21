---
slug: jdbc-temel
baslik: "JDBC: PreparedStatement, Connection Pool, Transaction ve Kaynak Yönetimi"
kademe: Orta
sira: 30
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Veritabanı ayrı bir programdır ve genelde ayrı bir makinede çalışır; Java uygulaman ona ancak ağ üzerinden konuşarak ulaşabilir. Her veritabanı markasının (PostgreSQL, MySQL, Oracle) kendi konuşma biçimi vardır. JDBC (Java Database Connectivity) bu markaların hepsinin arkasına takıldığı **ortak priz**dir: sen prize takarsın, hangi marka olduğu kodunu değiştirmez.

Benzetmeyi biraz daha götürelim: bir bankadan bilgi almak gibi. Önce bir görüşme açılır (bağlantı), sonra soru sorulur (sorgu), cevap sayfa sayfa okunur (sonuç), ve görüşme mutlaka kapatılır — kapatmazsan bankanın gişesi sonsuza dek meşgul kalır.

### Temel kavramlar

- **`Connection`** — veritabanıyla açılmış oturum. Açmak pahalıdır: TCP el sıkışması + kimlik doğrulama + oturum kurulumu.
- **`Statement` / `PreparedStatement`** — SQL çalıştıran nesneler. İkincisi parametreleri `?` yer tutucusuyla alır.
- **`ResultSet`** — `SELECT` sonucunu satır satır gezdiren **imleç** (cursor).
- **`executeQuery()`** bir `ResultSet` döndürür; **`executeUpdate()`** (`INSERT`/`UPDATE`/`DELETE` için) etkilenen satır sayısını `int` olarak döndürür.
- **Transaction (işlem)** — birden çok SQL ifadesini tek "ya hep ya hiç" birimi yapan yapı.
- **`AutoCloseable`** — try-with-resources'ın otomatik kapatabildiği kaynak arayüzü.

`ResultSet` bir `List` DEĞİLDİR ve tüm satırları zorunlu belleğe yüklemez — bu, büyük sonuçlarda bellek dostudur. Başlangıçta ilk satırın ÖNCESİNDE durur; her `next()` çağrısı bir sonraki satıra ilerler ve satır varsa `true`, sonuç bittiyse `false` döner. Sütunlar `getString`/`getInt` gibi metotlarla okunur. Kullanımdan sonra kapatılmalıdır.

```java
String sql = "SELECT ad, maas FROM calisanlar WHERE bolum = ?";
try (Connection c = ds.getConnection();
     PreparedStatement ps = c.prepareStatement(sql)) {
    ps.setString(1, bolum);
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {                 // önce ilerlet, sonra oku
            System.out.println(rs.getString("ad") + " " + rs.getInt("maas"));
        }
    }
}
```

```svg
<svg viewBox="0 0 680 154" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JDBC nesne akışı: DriverManager, Connection, PreparedStatement, ResultSet">
  <defs>
    <marker id="jdbc-flow-ok" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto">
      <path d="M0,0 L8,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="340" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">JDBC nesne akışı</text>
  <rect x="12" y="40" width="152" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="88" y="62" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">DriverManager /</text>
  <text x="88" y="76" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">DataSource</text>
  <text x="88" y="89" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">bağlantı üretir (pahalı)</text>
  <line x1="166" y1="66" x2="182" y2="66" stroke="#5D7789" stroke-width="1.5" marker-end="url(#jdbc-flow-ok)"/>
  <rect x="186" y="40" width="120" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="246" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Connection</text>
  <text x="246" y="82" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">DB oturumu</text>
  <line x1="308" y1="66" x2="324" y2="66" stroke="#5D7789" stroke-width="1.5" marker-end="url(#jdbc-flow-ok)"/>
  <rect x="328" y="40" width="164" height="52" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="410" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">PreparedStatement</text>
  <text x="410" y="82" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">? ile SQL çalıştırır</text>
  <line x1="494" y1="66" x2="510" y2="66" stroke="#5D7789" stroke-width="1.5" marker-end="url(#jdbc-flow-ok)"/>
  <rect x="514" y="40" width="154" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="591" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">ResultSet</text>
  <text x="591" y="82" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">satır imleci (cursor)</text>
  <text x="340" y="128" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Hepsi AutoCloseable → try-with-resources ile ters sırada kapatılır (sızıntıyı önler)</text>
</svg>
```

### Nasıl çalışır?

**PreparedStatement ve SQL enjeksiyonu (SQL injection).** Kullanıcı girdisini string birleştirmeyle SQL'e yapıştırmak klasik güvenlik açığıdır:

```java
String q = "SELECT * FROM kullanicilar WHERE ad = '" + girdi + "'";
stmt.executeQuery(q);
```

`girdi` değeri `' OR '1'='1` olursa sorgu `... WHERE ad = '' OR '1'='1'` haline gelir: koşul daima TRUE olur ve TÜM kullanıcılar döner. Sorgu hata vermez — sessizce ele geçirilir. JDBC sürücüsü string birleştirmede girdiyi otomatik temizlemez (sanitize/escape etmez), derleyici de bunu göremez.

`PreparedStatement` sorguyu `WHERE ad = ?` olarak önceden derler ve `setString(1, girdi)` ile değeri AYRI, veri kanalıyla gönderir. Değer hiçbir zaman SQL metninin parçası olarak yorumlanmaz; girdi ne olursa olsun sorgunun YAPISI değişemez — enjeksiyon yapısal olarak imkânsızlaşır. Ek olarak aynı sorgu tekrar çalıştırıldığında derlenmiş plan yeniden kullanılabildiği için performans da kazandırabilir; ama bu kazanç sürücüye ve veritabanına göre değişir, asıl gerekçe güvenliktir. Kural: kullanıcı girdisi içeren HER sorguda PreparedStatement.

**Bağlantı havuzu (connection pool).** `Connection` açmak pahalı olduğundan her istekte yeniden açmak yavaştır. Havuz, önceden açılmış bağlantıları tutar, ödünç verir ve iade alır. Kritik ayrıntı: havuzdan alınmış bir bağlantıda `close()` çağırmak onu fiziksel olarak KAPATMAZ; havuza **iade eder** ki başka bir istek yeniden kullansın. Hata da fırlatmaz, havuzun tamamını da kapatmaz — havuzu kapatmak ayrı bir işlemdir (`dataSource.close()`). **HikariCP** bugün fiili standarttır ve Spring Boot'un varsayılan havuzudur; "en hızlı havuz" kıyaslamaları büyük ölçüde projenin kendi ölçümlerine dayanır, gerçek kazanç iş yüküne göre değişir.

**try-with-resources.** `Connection`, `Statement`, `ResultSet` hepsi `AutoCloseable`'dır ve MUTLAKA kapatılmalıdır; kapatılmazsa — özellikle havuzlu ortamda — bağlantı sızıntısı (leak) birikir, havuz tükenir, uygulama kilitlenir. Elle `finally` yazmak hataya açıktır (kapatma sırasında istisna, iç içe `finally`'ler, unutma). try-with-resources kaynakları, gövde başarılı olsa da istisna fırlasa da, açılışın TERS sırasında otomatik kapatır. Yaptığı iş yalnız budur. Sorguları toplu (batch) hale getirmez, SQL injection'ı engellemez (o `PreparedStatement`'ın işidir) ve işlemi kalıcılaştırma/geri alma (commit/rollback) yönetimi yapmaz.

**Transaction ve autocommit.** JDBC'de bir bağlantı varsayılan olarak `autocommit=true` gelir: her ifade çalışır çalışmaz ayrı bir transaction gibi anında commit edilir, kalıcı olur. Böyle bir kavram yok demek ya da "ilk SELECT'e kadar kapalıdır" demek yanlıştır. Bunun sonucu ciddidir: para transferinde "hesaptan düş" başarılı olup "diğerine ekle" patlarsa, ilk ifade çoktan kalıcı olmuştur — para buharlaşır.

Birden çok ifadeyi tek atomik birim yapmak için açık transaction başlatılır:

```java
conn.setAutoCommit(false);
try {
    hesaptanDus(conn, 100);
    hesabaEkle(conn, 100);
    conn.commit();
} catch (SQLException e) {
    conn.rollback();     // ŞART — otomatik geri alma YOKTUR
}
```

`setAutoCommit(false)` ifadeleri `commit()`'e dek kalıcı yapmaz, ama otomatik geri de almaz. `catch` içinde `rollback()` (geri alma) çağrılmazsa 1. ifadenin etkisi transaction içinde asılı kalır. Sonuç iki türlü olabilir: ya bağlantı kapanınca sürücüye göre belirsiz ele alınır ya da yanlışlıkla commit edilip tutarsızlık kalıcı olur. Kısmi durumu `commit()` etmek en kötü seçenektir; yeni bir `Connection` açıp işlemi tekrarlamak da eski yarım transaction'ı geri almadığı için yetmez.

```svg
<svg viewBox="0 0 660 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Transaction akışı: setAutoCommit false, başarılıysa commit, istisnada rollback">
  <defs>
    <marker id="jdbc-tx-ok" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto">
      <path d="M0,0 L8,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Transaction: "ya hep ya hiç" (atomicity)</text>
  <rect x="220" y="36" width="220" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">setAutoCommit(false)</text>
  <line x1="330" y1="70" x2="330" y2="86" stroke="#5D7789" stroke-width="1.6" marker-end="url(#jdbc-tx-ok)"/>
  <rect x="200" y="90" width="260" height="46" rx="8" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.4"/>
  <text x="330" y="110" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">1. hesaptanDus(100)</text>
  <text x="330" y="128" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">2. hesabaEkle(100)</text>
  <line x1="272" y1="136" x2="182" y2="168" stroke="#25804C" stroke-width="1.6" marker-end="url(#jdbc-tx-ok)"/>
  <text x="196" y="158" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">başarılı</text>
  <line x1="388" y1="136" x2="478" y2="168" stroke="#B26B00" stroke-width="1.6" marker-end="url(#jdbc-tx-ok)"/>
  <text x="466" y="158" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">istisna</text>
  <rect x="40" y="172" width="240" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="160" y="194" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">commit()</text>
  <text x="160" y="212" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ikisi de kalıcı olur</text>
  <rect x="380" y="172" width="240" height="52" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="500" y="194" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">rollback()</text>
  <text x="500" y="212" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ikisi de geri alınır (kısmi kalmaz)</text>
</svg>
```

### Mülakatta ne sorulur?

- **"`ResultSet` nedir?"** → İmleç; `List` değil, `int` değil. Başta ilk satırın öncesindedir, `next()` ilerletir.
- **"`PreparedStatement` enjeksiyonu nasıl engeller?"** → Değeri SQL metninden ayrı, veri kanalıyla gönderir; sorgunun yapısı değişemez. Otomatik "temizleme" mitine dikkat.
- **"Havuzda `close()` ne yapar?"** → Fiziksel kapatmaz, havuza iade eder.
- **"try-with-resources'ın kritik faydası?"** → İstisna olsa bile kaynakların ters sırada garanti kapatılması; sızıntı önleme.
- **"Varsayılan autocommit?"** → `true`; her ifade anında kalıcı.
- **"İstisnada ne yapılır?"** → `rollback()`; otomatik geri alma yoktur.

### Tek cümlede

JDBC'de dört şeyi doğru yapmak yeterlidir: parametreleri `?` ile gönder (güvenlik), bağlantıyı havuzdan al ve `close()` ile iade et, kaynakları try-with-resources'a bırak, ve çok adımlı işlerde `setAutoCommit(false)` + `commit()`/`rollback()` ile atomikliği kendin kur.

# jdbc-1 | Temel
## Soru
JDBC'de bir `SELECT` sorgusu `executeQuery()` ile çalıştırıldığında dönen `ResultSet` nedir?
## A
Sonuç kümesi üzerinde `next()` ile satır satır ilerleyen bir imleç (cursor)
🤖 Doğru: başlangıçta ilk satırdan öncedir, `next()` bir sonraki satıra ilerler.
## B
Tüm satırları bir kerede belleğe yükleyen, indeksle erişilen bir `List`
🤖 `ResultSet` bir `List` değildir; satırları gezen bir imleçtir, tümünü zorunlu belleğe almaz.
## C
Sorgudan etkilenen satır sayısını veren bir `int` değer
🤖 O `executeUpdate()` dönüşüdür; `executeQuery` `ResultSet` döndürür.
## D
Çalıştırılan sorgunun metnini tutan bir `String`
🤖 `ResultSet` veriyi taşır, sorgu metnini değil.
## Doğru
A
## Açıklama
`ResultSet`, `SELECT` sonucu üzerinde gezinen bir **imleçtir** (cursor): başlangıçta ilk satırdan öncedir, `next()` her çağrıda bir sonraki satıra ilerler ve satır varsa `true` döner; sütunlar `getString`/`getInt` gibi metotlarla okunur (A). Tüm satırları zorunlu belleğe yükleyen bir koleksiyon değildir — bu, büyük sonuçlarda bellek dostudur. `executeUpdate()` ise `INSERT`/`UPDATE`/`DELETE` için etkilenen satır sayısını (`int`) döndürür (C). `ResultSet` de kullanımdan sonra kapatılmalıdır.

# jdbc-2 | Orta
## Soru
`girdi` değeri `' OR '1'='1` olursa aşağıdaki kodda ne olur ve `PreparedStatement` bunu nasıl engeller?

```java
String q = "SELECT * FROM kullanicilar WHERE ad = '" + girdi + "'";
stmt.executeQuery(q);
```
## A
Sorgunun söz dizimi bozulup hata fırlatır; `PreparedStatement` kullanılsa da aynı hata alınır
🤖 Sorgu hata vermez, TÜM satırları döndürür (injection); `PreparedStatement` ise değeri veri olarak alıp bunu engeller.
## B
Derleyici bir uyarı verir ama sorgu çalışma-zamanında güvenlidir, ek önlem gerekmez
🤖 Derleyici bunu göremez; açık çalışma-zamanında gerçek ve tehlikelidir.
## C
JDBC sürücüsü girdiyi otomatik temizler (sanitize), tırnaklar kaçırıldığı için sorun oluşmaz
🤖 String birleştirmede otomatik temizlik/kaçış yoktur; açık aynen oluşur.
## D
Koşul her zaman TRUE olur ve tüm kullanıcılar döner (SQL injection); `PreparedStatement` girdiyi `?` ile veri olarak gönderir
🤖 Doğru: değer ayrı bir kanaldan gider, sorgunun yapısı değişemez.
## Doğru
D
## Açıklama
Girdi doğrudan SQL metnine yapıştırıldığında `' OR '1'='1` sorguyu `... WHERE ad = '' OR '1'='1'` haline getirir; koşul daima TRUE olur ve tüm kayıtlar döner — klasik **SQL injection** (D). `PreparedStatement` sorguyu `WHERE ad = ?` olarak önceden derler ve `setString(1, girdi)` ile değeri AYRI, veri kanalıyla gönderir; sürücü onu asla SQL'in bir parçası olarak yorumlamaz — girdi ne olursa olsun sorgunun YAPISI değişemez, enjeksiyon imkânsızlaşır. Ek olarak derlenmiş plan tekrar kullanılabildiğinden performans da kazandırır.

# jdbc-3 | Orta
## Soru
HikariCP gibi bir bağlantı havuzu (connection pool) kullanılırken havuzdan alınmış bir `Connection` üzerinde `close()` çağırmak ne yapar?
## A
TCP soketini kapatarak fiziksel veritabanı bağlantısını gerçekten sonlandırır
🤖 Havuzda `close()` fiziksel kapatmaz; bağlantıyı havuza iade eder.
## B
Havuzdaki tüm bağlantıları kapatır ve havuzu boşaltır
🤖 Yalnız o bağlantı iade edilir; havuz kapanmaz.
## C
Bağlantıyı fiziksel olarak kapatmaz; havuza geri iade eder
🤖 Doğru: `close()` havuza dönüş anlamına gelir, fiziksel kapatma değil.
## D
Hata fırlatır; havuz tarafından yönetilen bir bağlantı kapatılamaz
🤖 `close()` beklenen çağrıdır, hata fırlatmaz; iade anlamına gelir.
## Doğru
C
## Açıklama
`Connection` açmak (TCP el sıkışması + kimlik doğrulama + oturum) pahalıdır; her istekte yeniden açmak yavaştır. Havuz, önceden açılmış bağlantıları tutar ve ödünç verir. Havuzdan alınmış bir bağlantıda `close()` çağırmak onu fiziksel olarak KAPATMAZ; havuza **iade eder** ki başka bir istek yeniden kullansın (C). Bu yüzden try-with-resources ile "kapatmak" havuzlu ortamda güvenlidir — sızıntıyı önler, bağlantıyı geri verir. HikariCP düşük gecikmesi ve sağlamlığıyla fiili standarttır (Spring Boot varsayılanı). Havuzun tümünü kapatmak ayrı bir işlemdir (`dataSource.close()`).

# jdbc-4 | Orta
## Soru
JDBC'de `Connection`/`Statement`/`ResultSet` için try-with-resources kullanmanın, elle açıp kapatmaya göre kritik faydası nedir?
## A
Sorguları toplu (batch) hale getirip otomatik olarak daha hızlı çalıştırır
🤖 Hız ya da toplu çalıştırmayla ilgisi yoktur; fayda kaynakların garanti kapatılmasıdır.
## B
İstisna fırlasa bile kaynakların otomatik kapatılmasını garanti eder; bağlantı sızıntısını (leak) önler
🤖 Doğru: kaynaklar `AutoCloseable`'dır; try-with-resources onları her koşulda kapatır.
## C
Parametreleri kaçırarak (escape) SQL injection saldırılarını engeller
🤖 O `PreparedStatement`'ın işidir; try-with-resources yalnız kaynak yönetimiyle ilgilidir.
## D
Blok başarıyla biterse transaction'ı otomatik commit, hata halinde rollback eder
🤖 try-with-resources commit/rollback yönetmez; yalnız kaynakları kapatır.
## Doğru
B
## Açıklama
`Connection`, `Statement`, `ResultSet` birer `AutoCloseable`'dır ve kapatılmazsa — özellikle havuzlu ortamda — bağlantı sızıntısı (leak) birikir, havuz tükenir ve uygulama kilitlenir. Elle `finally` ile kapatmak hataya açıktır (kapatma sırasında istisna, iç içe `finally`'ler, unutma). try-with-resources kaynakları, gövde başarılı olsa da istisna fırlasa da, açılışın TERS sırasında otomatik kapatır (B). SQL injection (C) `PreparedStatement`'ın, commit (D) transaction yönetiminin konusudur — ayrı sorumluluklar.

# jdbc-5 | Orta
## Soru
JDBC'de bir `Connection`'ın varsayılan `autocommit` davranışı nedir ve bunun sonucu nedir?
## A
`autocommit=true`; her SQL ifadesi çalışır çalışmaz kendi başına commit edilir, kalıcı olur
🤖 Doğru: varsayılanda her ifade ayrı bir transaction gibi anında kalıcı olur.
## B
`autocommit=false`; hiçbir ifade siz `commit()` demeden kalıcı olmaz
🤖 Varsayılan `true`'dur; `false` davranışını elle etkinleştirirsin.
## C
Autocommit diye bir kavram JDBC'de yoktur
🤖 Vardır ve varsayılan davranışı belirler.
## D
İlk `SELECT`'e kadar autocommit kapalı, sonra açılır
🤖 Böyle bir geçiş yoktur; varsayılan baştan `true`'dur.
## Doğru
A
## Açıklama
JDBC'de bir bağlantı varsayılan olarak `autocommit=true` gelir: her ifade (INSERT/UPDATE/DELETE) çalışır çalışmaz ayrı bir transaction gibi anında commit edilir, kalıcı olur (A). Bu, birden çok ifadeyi tek atomik birim yapmayı engeller — ör. para transferinde "hesaptan düş" başarılı olup "diğerine ekle" patlarsa, ilk ifade çoktan kalıcı olmuştur, para buharlaşır. Bunu önlemek için `setAutoCommit(false)` ile açık bir transaction başlatılır; sonunda `commit()` veya hata halinde `rollback()` çağrılır.

# jdbc-6 | İleri
## Soru
İkinci ifade istisna fırlatırsa "ya hep ya hiç" bütünlüğü için `catch` bloğunda ne yapılmalıdır?

```java
conn.setAutoCommit(false);
try {
    hesaptanDus(conn, 100);      // 1. ifade
    hesabaEkle(conn, 100);       // 2. ifade — burada istisna fırlarsa?
    conn.commit();
} catch (SQLException e) {
    // ???
}
```
## A
Hiçbir şey; `setAutoCommit(false)` istisna anında transaction'ı zaten otomatik geri alır
🤖 Otomatik rollback yoktur; açıkça `rollback()` çağırmalısın.
## B
Yeni bir `Connection` açıp işlemi baştan tekrarlamak yeterlidir; eskisi kendiliğinden düşer
🤖 Eski bağlantıdaki yarım transaction geri alınmazsa tutarsızlık kalır; önce rollback şarttır.
## C
`conn.commit()` çağrılmalı; kısmi de olsa yapılan işin kaydedilmesi veri kaybını önler
🤖 Commit kısmi durumu KALICI yapar (para kaybı); tam tersine rollback gerekir.
## D
`conn.rollback()` çağrılmalı; böylece 1. ifadenin etkisi de geri alınır
🤖 Doğru: rollback tüm bekleyen transaction'ı geri alır, kısmi durum kalmaz.
## Doğru
D
## Açıklama
`setAutoCommit(false)` ile ifadeler `commit()` çağrılana dek kalıcı olmaz ama otomatik geri de alınmaz. 2. ifade patladığında 1. ifade (hesaptan düşme) transaction içinde beklemektedir; `catch` bloğunda `conn.rollback()` çağrılmazsa bu yarım durum tutarsızlık doğurur (ya bağlantı kapanınca sürücüye göre belirsiz ele alınır ya da yanlışlıkla commit edilirse para buharlaşır). Doğru desen: hata halinde `rollback()` ile TÜM transaction'ı geri almaktır — "ya hep ya hiç" (atomicity) (D). Tam desen: `try` içinde iş + `commit()`, `catch` içinde `rollback()`, ve kaynakları try-with-resources ile kapatmak.
