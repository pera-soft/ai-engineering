---
slug: izolasyon-seviyeleri-anomaliler
baslik: "İşlem İzolasyonu: Okuma Anomalileri, Standart Seviyeler, MVCC ve Gerçek Veritabanları"
kademe: İleri
sira: 224
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir bankanın şubesinde iki gişe memuru aynı müşterinin dosyasını aynı anda masaya çekiyor. Biri "kalan limit 10.000, o hâlde 8.000 kullandırabilirim" diyor; diğeri de aynı kâğıda bakıp "10.000 var, 6.000 kullandırırım" diyor. İkisi de kendi kararını dosyaya yazınca müşteri 14.000 kullanmış olur, oysa limiti 10.000'di. Kimse hata yapmadı; sorun **kimin ne zaman neyi gördüğü**. Veritabanı işlemlerinde bu soruya verilen cevabın adı **izolasyon** (isolation) — ACID'in "I"sidir. İzolasyonun vaadi şudur: eşzamanlı çalışan işlemler, sanki **tek tek sırayla** çalışmış gibi bir sonuç üretsin. Bu vaat en üst seviyede tamdır; aşağı seviyelerde ise vaadin bir kısmı performans karşılığında bilerek satılır. Mülakatta asıl ölçülen şey, hangi seviyede hangi parçanın satıldığını bilip bilmediğindir.

### Temel kavramlar

- **İzolasyon (isolation)**: Eşzamanlı işlemlerin birbirinin yarım kalmış ya da değişen verisini ne kadar gördüğünü belirleyen düzey.
- **Serileştirilebilirlik (serializability)**: Eşzamanlı çalışan işlemlerin sonucunun, onların bir sırayla teker teker çalıştırılmış hâline **eşdeğer** olması. İzolasyonun tam biçimi budur.
- **Anomali (anomaly)**: Seviye düşürüldüğünde ortaya çıkan, "tek tek çalışsalardı olmayacak" sonuç.
- **Kirli okuma (dirty read)**: Başka bir işlemin henüz commit **etmediği** veriyi okumak. O işlem geri alınırsa hiç var olmamış bir değere göre karar vermiş olursun. Örnek: tahsis edilmekte olan ama commit edilmemiş 50.000'lik limiti okuyup müşteriye "limitiniz arttı" demek, sonra tahsisin geri alınması.
- **Tekrarlanamayan okuma (non-repeatable read)**: Aynı **satırı** bir işlem içinde iki kez okuyunca, arada başkası güncelleyip commit ettiği için **farklı değer** görmek. Örnek: bir raporun başında bakiye 40.000, sonunda 25.000 okunması; rapor kendi içinde tutarsız çıkar.
- **Hayalet okuma (phantom read)**: Aynı **koşullu** sorguyu (`WHERE durum = 'ONAYLI'`) iki kez çalıştırınca, arada eklenen satırlar yüzünden ikinci sonuçta **yeni satırların belirmesi**. Örnek: gün sonu toplamı iki kez alınca satır sayısının artması.
- **Kayıp güncelleme (lost update)**: İki işlem de **aynı satırı** okuyup, okuduğu değere göre hesap yapıp geri yazınca, önce yazanın etkisinin sessizce silinmesi. Hiçbir hata mesajı yoktur; sadece para tutmaz.
- **Yazma çarpıklığı (write skew)**: İki işlem, aralarında bir kural bulunan **farklı satırları** okuyup her biri **kendi satırını** güncellediğinde, tek tek bakıldığında kural sağlanırken toplamda **ihlal edilmesi**. Aynı satıra yazılmadığı için veritabanı çakışmayı fark etmez.
- **Anlık görüntü izolasyonu (snapshot isolation)**: İşlemin, başladığı andaki tutarlı bir görüntüyü baştan sona okuması. Kirli/tekrarlanamayan/hayalet okumayı keser ama **yazma çarpıklığını kesmez**.
- **MVCC (çok sürümlü eşzamanlılık denetimi)**: Bir satırın birden çok sürümünü tutup okuyuculara eski sürümü göstererek okumayı kilitsiz yapan mekanizma.

Kayıp güncelleme ile yazma çarpıklığının farkı mülakatta ayırt edici sorudur: **kayıp güncelleme aynı satırda**, **yazma çarpıklığı farklı satırlarda** oluşur. Bu yüzden birincisini pek çok veritabanı kendiliğinden yakalar, ikincisini yakalamaz.

Aşağıdaki akış, kredi limiti düşümünün "önce oku, karar ver, sonra yaz" biçiminde yazıldığında `READ COMMITTED` altında neden yanlış sonuç verdiğini gösterir. Dikkat: burada kirli okuma **yoktur** — iki işlem de commit edilmiş, o an geçerli veriyi okumuştur. Yine de sonuç yanlıştır, çünkü ikisi de **kararını okuduğu anın fotoğrafına** dayandırmıştır.

```svg
<svg viewBox="0 0 660 256" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İki eşzamanlı kredi başvurusunda kayıp güncelleme oluşumu">
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kayıp güncelleme: oku → karar ver → yaz (READ COMMITTED)</text>
  <text x="188" y="40" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İşlem A — Başvuru #1 (8.000)</text>
  <text x="462" y="40" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">İşlem B — Başvuru #2 (6.000)</text>
  <text x="30" y="72" text-anchor="start" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">t1</text>
  <rect x="56" y="52" width="264" height="32" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="188" y="72" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">oku: kullanılabilir limit = 10.000</text>
  <text x="30" y="110" text-anchor="start" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">t2</text>
  <rect x="330" y="90" width="264" height="32" rx="7" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="462" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">oku: kullanılabilir limit = 10.000</text>
  <text x="30" y="148" text-anchor="start" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">t3</text>
  <rect x="56" y="128" width="264" height="32" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="188" y="148" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yaz: 10.000 − 8.000 = 2.000 → COMMIT</text>
  <text x="30" y="186" text-anchor="start" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">t4</text>
  <rect x="330" y="166" width="264" height="32" rx="7" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="462" y="186" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yaz: 10.000 − 6.000 = 4.000 → COMMIT</text>
  <rect x="56" y="206" width="538" height="42" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="325" y="224" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Sonuç: 14.000 kullandırıldı, satırda 4.000 yazıyor</text>
  <text x="325" y="240" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">A'nın düşümü sessizce kayboldu — hiçbir hata mesajı yok</text>
</svg>
```

### Nasıl çalışır?

SQL standardı dört izolasyon seviyesi tanımlar ve bunları hangi anomaliye izin verdiklerine göre sıralar:

1. **READ UNCOMMITTED** — hiçbir şeyi engellemez; commit edilmemiş veriyi bile okursun. Pratikte finansal sistemde yeri yoktur.
2. **READ COMMITTED** — yalnız commit edilmiş veriyi okursun. Her **cümle** (statement) kendi taze görüntüsünü alır; bu yüzden aynı işlem içinde iki okuma farklı çıkabilir.
3. **REPEATABLE READ** — işlem boyunca okuduğun satırlar değişmez. Standardın kilit tabanlı tanımında hayalet okuma hâlâ mümkündür.
4. **SERIALIZABLE** — sonuç, işlemlerin bir sırayla çalıştırılmış hâline eşdeğerdir; hiçbir anomali kalmaz.

```svg
<svg viewBox="0 0 660 276" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İzolasyon seviyeleri ve engelledikleri anomaliler tablosu">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hangi seviye hangi anomaliyi engeller?</text>
  <rect x="8" y="36" width="644" height="44" rx="5" fill="#1C3247"/>
  <text x="92" y="63" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">Seviye</text>
  <text x="223" y="56" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Kirli</text>
  <text x="223" y="70" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">okuma</text>
  <text x="318" y="56" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Tekrarlanamaz</text>
  <text x="318" y="70" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">okuma</text>
  <text x="413" y="56" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Hayalet</text>
  <text x="413" y="70" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">okuma</text>
  <text x="508" y="56" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Kayıp</text>
  <text x="508" y="70" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">güncelleme</text>
  <text x="603" y="56" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Yazma</text>
  <text x="603" y="70" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">çarpıklığı</text>
  <rect x="8" y="80" width="644" height="36" fill="#FBE9E9" stroke="#5D7789" stroke-width="0.6"/>
  <text x="92" y="103" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">READ UNCOMMITTED</text>
  <text x="223" y="103" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">VAR</text>
  <text x="318" y="103" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">VAR</text>
  <text x="413" y="103" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">VAR</text>
  <text x="508" y="103" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">VAR</text>
  <text x="603" y="103" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">VAR</text>
  <rect x="8" y="116" width="644" height="36" fill="#FDF3E0" stroke="#5D7789" stroke-width="0.6"/>
  <text x="92" y="139" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">READ COMMITTED</text>
  <text x="223" y="139" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yok</text>
  <text x="318" y="139" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">VAR</text>
  <text x="413" y="139" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">VAR</text>
  <text x="508" y="139" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">VAR</text>
  <text x="603" y="139" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">VAR</text>
  <rect x="8" y="152" width="644" height="36" fill="#EAF1F7" stroke="#5D7789" stroke-width="0.6"/>
  <text x="92" y="175" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">REPEATABLE READ</text>
  <text x="223" y="175" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yok</text>
  <text x="318" y="175" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yok</text>
  <text x="413" y="175" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">VAR *</text>
  <text x="508" y="175" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yok</text>
  <text x="603" y="175" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yok *</text>
  <rect x="8" y="188" width="644" height="36" fill="#E9F4EE" stroke="#5D7789" stroke-width="0.6"/>
  <text x="92" y="211" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">SERIALIZABLE</text>
  <text x="223" y="211" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yok</text>
  <text x="318" y="211" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yok</text>
  <text x="413" y="211" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yok</text>
  <text x="508" y="211" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yok</text>
  <text x="603" y="211" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yok **</text>
  <text x="8" y="242" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">* Standardın kilit tabanlı tanımı. Anlık görüntü (snapshot) ile gerçeklenmiş REPEATABLE READ'de tam tersi olur:</text>
  <text x="8" y="256" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">hayalet okuma engellenir, yazma çarpıklığı ORTAYA ÇIKAR. ** Oracle'ın SERIALIZABLE'ı anlık görüntüdür:</text>
  <text x="8" y="270" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">yazma çarpıklığına açıktır. PostgreSQL'in SERIALIZABLE'ı (SSI) ise gerçekten serileştirilebilirdir.</text>
</svg>
```

Bu tablo standardı anlatır; **gerçek veritabanları standarttan sapar** ve mülakatta ayırt edici bilgi tam burada saklıdır:

- **Oracle**: Yalnız **iki** seviye vardır — `READ COMMITTED` (varsayılan) ve `SERIALIZABLE`. `REPEATABLE READ` ve `READ UNCOMMITTED` **yoktur**; sürücü bu seviyeleri kabul etmez, işlem açılırken hata alırsın. Dahası Oracle'ın `SERIALIZABLE`'ı, akademik anlamda gerçek serileştirilebilirlik değil **anlık görüntü izolasyonudur** (snapshot isolation): aynı satıra iki yazma olursa ikinciyi `ORA-08177` ile reddeder, ama **yazma çarpıklığına açıktır**.
- **PostgreSQL**: Varsayılan `READ COMMITTED`. `REPEATABLE READ` anlık görüntüyle gerçeklenir (hayaletleri de keser, ama yazma çarpıklığı kalır). `SERIALIZABLE` ise **SSI** (serileştirilebilir anlık görüntü izolasyonu) ile gerçek serileştirilebilirlik verir. SSI'nin çalışma biçimi kritik: çakışmayı **bloklayarak değil, iptal ederek** çözer — işlem `40001` (serialization_failure) hatasıyla düşer. Bu yüzden PostgreSQL'de `SERIALIZABLE` kullanmak **uygulamaya yeniden deneme (retry) yazma zorunluluğu getirir**; "seviyeyi yükselttim, kod aynı kalır" demek yanlıştır.
- **MySQL/InnoDB**: Varsayılan `REPEATABLE READ`. Kilitsiz **tutarlı okumalar** MVCC anlık görüntüsünden okur. Kilitli okumalarda (`FOR UPDATE`) ise araya yeni satır girmesi engellenir. Bunun için **boşluk kilidi** (gap lock) ve onun bileşimi olan next-key kilit kullanılır. Sonuç: hayalet okuma bu seviyede de engellenmiş olur.

```svg
<svg viewBox="0 0 660 238" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Oracle PostgreSQL ve MySQL InnoDB izolasyon davranışları ile MVCC özeti">
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Gerçek veritabanları standarttan sapar</text>
  <rect x="10" y="34" width="206" height="140" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="113" y="54" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">Oracle</text>
  <text x="24" y="76" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">Sadece 2 seviye:</text>
  <text x="24" y="92" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">READ COMMITTED (varsayılan)</text>
  <text x="24" y="108" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">ve SERIALIZABLE</text>
  <text x="24" y="126" text-anchor="start" font-size="10.5" fill="#B23B3B" font-family="system-ui">REPEATABLE READ / READ</text>
  <text x="24" y="140" text-anchor="start" font-size="10.5" fill="#B23B3B" font-family="system-ui">UNCOMMITTED YOK</text>
  <text x="24" y="160" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">SERIALIZABLE = anlık görüntü</text>
  <rect x="226" y="34" width="206" height="140" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="329" y="54" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">PostgreSQL</text>
  <text x="240" y="76" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">Varsayılan: READ COMMITTED</text>
  <text x="240" y="92" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">REPEATABLE READ = anlık</text>
  <text x="240" y="106" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">görüntü izolasyonu</text>
  <text x="240" y="126" text-anchor="start" font-size="10.5" fill="#186399" font-family="system-ui">SERIALIZABLE = SSI (gerçek)</text>
  <text x="240" y="142" text-anchor="start" font-size="10.5" fill="#B23B3B" font-family="system-ui">Bloklamaz, İPTAL eder: 40001</text>
  <text x="240" y="160" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">→ yeniden deneme zorunlu</text>
  <rect x="442" y="34" width="208" height="140" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="546" y="54" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">MySQL / InnoDB</text>
  <text x="456" y="76" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">Varsayılan: REPEATABLE READ</text>
  <text x="456" y="94" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">Tutarlı okuma: MVCC anlık</text>
  <text x="456" y="108" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">görüntüsünden</text>
  <text x="456" y="128" text-anchor="start" font-size="10.5" fill="#B07D20" font-family="system-ui">Kilitli okumada boşluk kilidi</text>
  <text x="456" y="142" text-anchor="start" font-size="10.5" fill="#B07D20" font-family="system-ui">(gap lock) / next-key</text>
  <text x="456" y="160" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">→ hayalet okuma engellenir</text>
  <rect x="10" y="184" width="640" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="330" y="203" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">MVCC: okuyucu satırın eski sürümünü görür — okuyucular yazarları, yazarlar okuyucuları BLOKLAMAZ.</text>
  <text x="330" y="220" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Ama iki YAZAR aynı satırda hâlâ çakışır: MVCC tek başına kayıp güncellemeyi çözmez.</text>
</svg>
```

**MVCC**'nin mülakattaki değeri şudur: raporlama sorgusu saatlerce sürse bile yazma trafiğini durdurmaz, çünkü okuyucu satırın **eski sürümünü** okur. Bu, "izolasyonu yükseltmek her zaman kilitlenme demektir" sanısını çürütür. Ama madalyonun diğer yüzü şudur: MVCC okuma tarafını rahatlatır, **yazma tarafındaki çakışmayı çözmez**. İki işlem aynı satıra yazmaya kalkarsa biri diğerini bekler ya da anlık görüntü ihlali hatası alır.

Spring tarafında `@Transactional(isolation = Isolation.REPEATABLE_READ)` yazdığında Spring, işlem başlarken **JDBC bağlantısı üzerinde** `Connection.setTransactionIsolation(...)` çağırır; işlem bitince havuza dönen bağlantı eski değerine geri alınır. Seviyeyi uygulayan Spring değil **veritabanı motorudur**; motor o seviyeyi desteklemiyorsa (Oracle + `REPEATABLE READ`) hata alırsın. `Isolation.DEFAULT` ise hiçbir seviye dayatmaz, veritabanının kendi varsayılanına bırakır.

Sık düşülen bir tuzak var: **JPA/Hibernate izolasyon seviyesini kendiliğinden yükseltmez.** Ancak **kalıcı bağlam** (persistence context) bir birinci seviye önbellektir. Bu yüzden aynı işlem içinde aynı kimliği (`id`) ikinci kez `find` ettiğinde veri tabanına gidip taze değeri getirmez; elindeki nesneyi verir. Bu, `READ COMMITTED` altında bile "tekrarlanamayan okuma yokmuş" izlenimi yaratır — testte sorun görünmez, gerçek eşzamanlılıkta ortaya çıkar. Uygulama düzeyindeki bu tekrarlanabilirlik veritabanı garantisi değildir.

İzolasyonu yükseltmenin bir bedeli vardır ve bu bedel iki biçimde ödenir. Kilit tabanlı motorlarda **kilit ve çekişme** olarak ödenir: işlemler bekler, yanıt süreleri uzar, kilitlenme riski artar. Anlık görüntü tabanlı motorlarda ise **iptal ve yeniden deneme** olarak ödenir: başarısız işlem oranı yükselir ve yük arttıkça daha çok iş boşa gider. Her iki durumda da ölçeklenme kaybı vardır ve bedel **tüm sisteme** yayılır, oysa sorun genelde birkaç satırdadır.

Bu yüzden pratikte tercih edilen yol çoğu zaman izolasyonu yükseltmek değil, **uygulama düzeyinde nokta atışı çözümdür**:

- **Sürüm sütunu** (`@Version`) ile iyimser kilitleme: çakışmayı yazma anında yakala, yakalarsan yeniden dene.
- **Atomik koşullu güncelleme**: okuma ile yazmayı tek cümlede birleştir — `UPDATE kredi SET kullanilan = kullanilan + :tutar WHERE id = :id AND kullanilan + :tutar <= limit` — ve **etkilenen satır sayısını** kontrol et. 0 satır ise limit yetmemiştir. Burada "oku, karar ver, yaz" arasındaki boşluk hiç oluşmaz.
- Gerçekten sıraya sokman gereken dar bir bölge varsa **kötümser kilit** (`SELECT ... FOR UPDATE`) ile yalnız o satırı kilitle.

### Mülakatta ne sorulur / tuzaklar

- **"Kayıp güncelleme ile yazma çarpıklığı arasındaki fark ne?"** → Kayıp güncelleme **aynı satırda** olur ve anlık görüntü izolasyonu bunu ilk-commit-eden-kazanır kuralıyla yakalar. Yazma çarpıklığı **farklı satırlarda** olur, aralarındaki kuralı veritabanı bilmediği için **yakalanmaz**. "Snapshot isolation her şeyi çözer" derseniz elenirsiniz.
- **"Oracle'da REPEATABLE READ nasıl ayarlanır?"** → Ayarlanamaz; Oracle'da o seviye **yoktur**. Cevabı "READ COMMITTED'a düşer" ya da "SERIALIZABLE'a yükselir" demek yanlıştır — desteklenmeyen seviye sessizce dönüştürülmez, hata alırsın.
- **"PostgreSQL'de SERIALIZABLE'a geçtim, kodum aynı kalabilir mi?"** → Hayır. SSI çakışmayı iptal ederek çözer; `40001` alan işlemi **baştan yeniden denemek** uygulamanın sorumluluğudur. Yeniden deneme yoksa üretimde rastgele hata görürsün.
- **"MVCC izolasyon problemini çözer mi?"** → Okuma tarafını çözer (okuyucu yazarı bloklamaz), **yazma çakışmasını çözmez**. Kayıp güncelleme MVCC'li veritabanlarında da olur.
- **"Kirli okuma mı yaşıyorsun?"** tuzağı → "İki istek aynı limiti okuyup ikisi de onayladı" senaryosunda kirli okuma **yoktur**; ikisi de commit edilmiş veri okumuştur. Doğru teşhis **kayıp güncellemedir**.
- **"Çözüm için izolasyonu SERIALIZABLE yapalım mı?"** → Genellikle hayır. Bedeli tüm sisteme yayılır; sorun tek satırdaysa atomik koşullu güncelleme ya da sürüm sütunu hem daha ucuz hem daha öngörülebilirdir.
- **"Testlerde sorun çıkmıyor, üretimde çıkıyor"** → Tek iş parçacıklı testte eşzamanlılık yoktur; ayrıca Hibernate'in kalıcı bağlamı aynı işlem içinde aynı nesneyi verdiği için tekrarlanamayan okuma gizlenir. Eşzamanlılık hataları eşzamanlı testle aranır.

### Tek cümlede

İzolasyon şu vaadin ne kadarını satın aldığını belirler: "eşzamanlı işlemler sanki sırayla çalışmış gibi sonuç versin." Standardın dört seviyesi gerçek veri tabanlarında farklı gerçeklenir. Para hesabı yapan kodda doğru refleks, seviyeyi körü körüne yükseltmek değildir; "oku–karar ver–yaz" boşluğunu atomik güncelleme ya da sürüm sütunuyla kapatmaktır.

# txizol-1 | Orta
## Soru
Kredi limiti düşümü "önce kullanılabilir limiti oku, yeterli mi diye karar ver, sonra yeni değeri yaz" biçiminde kodlanmış. Aynı müşteri için iki başvuru aynı anda gelir ve veritabanı `READ COMMITTED` seviyesinde çalışır. Ortaya çıkan anomali hangisidir?
## A
Hiçbir anomali oluşmaz; `READ COMMITTED` commit edilmiş veriyi okumayı garanti ettiği için ikinci istek birincinin yazdığı güncel değeri görür ve doğru karar verir.
🤖 ikinci istek okumasını birinci commit etmeden ÖNCE yapmıştır; READ COMMITTED okuma anını geriye taşımaz.
## B
Kirli okuma (dirty read) oluşur; ikinci istek, birinci isteğin henüz commit etmediği ara bakiyeyi okuyup ona göre karar verdiği için limit aşılır.
🤖 READ COMMITTED zaten kirli okumayı engeller, iki istek de commit edilmiş veri okumuştur.
## C
Kayıp güncelleme (lost update) oluşur; iki istek de aynı eski değeri okur, kendi hesabını yapıp üzerine yazar ve önce yazanın düşümü sessizce silinir.
🤖 Doğru: aynı satırda "oku–karar ver–yaz" boşluğu, hata vermeden bir güncellemenin kaybolmasına yol açar.
## D
Yazma çarpıklığı (write skew) oluşur; iki işlem birbirinden farklı satırları güncellediği için veritabanı çakışmayı fark edemez ve toplam kısıt ihlal edilir.
🤖 burada iki işlem AYNI satırı günceller; yazma çarpıklığı farklı satırlarda oluşan anomalidir.
## Doğru
C
## Açıklama
Bu klasik **kayıp güncelleme** (lost update) senaryosudur: iki işlem de aynı satırı okur, okuduğu fotoğrafa göre hesap yapar ve sonucu geri yazar; ikinci yazma birincinin etkisini **sessizce** siler. Tehlikeli yanı hiçbir hata mesajı üretmemesidir — sadece para tutmaz. Burada **kirli okuma yoktur** (B), çünkü `READ COMMITTED` altında iki işlem de commit edilmiş veriyi okumuştur; anomali "yanlış veri okumak" değil, "okuma ile yazma arasındaki boşluk"tur. A, `READ COMMITTED`'in vaadini abartır: seviye, okumadan sonra başkasının araya girmesini engellemez. D ise anomaliyi karıştırır — **yazma çarpıklığı farklı satırlarda**, kayıp güncelleme **aynı satırda** olur. Doğru çözüm okuma ile yazmayı tek cümlede birleştiren **atomik koşullu güncelleme** ya da bir **sürüm sütunudur**.

# txizol-2 | Orta
## Soru
Oracle üzerinde çalışan bir Spring Boot uygulamasında bir servis metoduna `@Transactional(isolation = Isolation.REPEATABLE_READ)` yazdın. Uygulamayı çalıştırınca ne olmasını beklersin?
## A
Oracle bu seviyeyi desteklemez; yalnız `READ COMMITTED` ve `SERIALIZABLE` vardır, bu yüzden bağlantıya seviye atanırken hata alırsın ve işlem açılamaz.
🤖 Doğru: desteklenmeyen izolasyon seviyesi sürücü tarafından kabul edilmez.
## B
Oracle isteği sessizce bir üst seviyeye, `SERIALIZABLE`'a yükseltir; kod sorunsuz çalışır, yalnız işlemler beklenenden daha katı ve daha yavaş olur.
🤖 desteklenmeyen seviye sessizce yükseltilmez; böyle bir otomatik dönüşüm yoktur.
## C
Oracle isteği sessizce varsayılanı olan `READ COMMITTED`'e düşürür; kod çalışır ama tekrarlanamayan okumalar beklediğinin aksine engellenmemiş olur.
🤖 sessizce düşürme de olmaz; istek reddedilir, sessiz bir geri çekilme yaşanmaz.
## D
Sorun çıkmaz; `REPEATABLE READ` SQL standardında zorunlu olduğu için tüm ilişkisel veritabanlarında bulunur ve zaten Oracle'ın varsayılan seviyesidir.
🤖 Oracle'da REPEATABLE READ yoktur ve varsayılanı READ COMMITTED'dir.
## Doğru
A
## Açıklama
Oracle, SQL standardındaki dört seviyenin yalnız **ikisini** gerçekler: `READ COMMITTED` (varsayılan) ve `SERIALIZABLE`. `REPEATABLE READ` ve `READ UNCOMMITTED` **yoktur**. Spring, işlem başlarken JDBC bağlantısı üzerinde `Connection.setTransactionIsolation(...)` çağırır; sürücü desteklemediği seviyeyi **sessizce dönüştürmez**, reddeder. Bu yüzden B ve C'deki "sessizce yükseltir/düşürür" beklentisi yanlıştır — bu tür sessiz dönüşümler veri bütünlüğü açısından tehlikeli olurdu ve standart da bunu şart koşmaz. D ise iki hata birden yapar: seviye Oracle'da yoktur ve varsayılan `READ COMMITTED`'dir. Ek olarak bilinmesi gereken incelik: Oracle'ın `SERIALIZABLE`'ı akademik anlamda gerçek serileştirilebilirlik değil **anlık görüntü izolasyonudur** (snapshot isolation) ve **yazma çarpıklığına açıktır**.

# txizol-3 | Orta
## Soru
Gün sonu mutabakat işini PostgreSQL üzerinde `SERIALIZABLE` izolasyonuyla çalıştırmaya karar verdin. Bu kararın uygulama koduna getirdiği ek sorumluluk nedir?
## A
Hiçbir ek sorumluluk yoktur; `SERIALIZABLE` tüm çakışmaları kilitleyerek çözdüğü için işlemler yalnız birbirini bekler ve mevcut kod hiç değişmeden çalışmaya devam eder.
🤖 PostgreSQL'in SSI'ı çakışmayı bloklayarak değil, işlemi iptal ederek çözer.
## B
Her işlemin başında ilgili tabloları `LOCK TABLE` ile açıkça kilitlemek gerekir; `SERIALIZABLE` yalnız bu açık kilitlerle birlikte kullanıldığında anlamlı bir garanti verir.
🤖 SERIALIZABLE elle tablo kilitlemeyi gerektirmez, garantiyi kendisi sağlar.
## C
İşlemleri `readOnly = true` işaretlemek yeterlidir; salt okunur işlemler serileştirme hatası alamayacağı için mutabakat işi bu şekilde güvenceye alınmış olur.
🤖 readOnly bir Hibernate optimizasyon ipucudur, serileştirme hatasına karşı bağışıklık vermez.
## D
Serileştirme hatası (`40001`) alan işlemi yakalayıp baştan yeniden denemek gerekir; SSI çakışmayı bloklamak yerine işlemi iptal ederek çözer.
🤖 Doğru: SSI'da çakışan işlem düşer, yeniden deneme uygulamanın sorumluluğudur.
## Doğru
D
## Açıklama
PostgreSQL, `SERIALIZABLE` seviyesini **SSI** (serileştirilebilir anlık görüntü izolasyonu) ile gerçekler. SSI'ın ayırt edici davranışı şudur: çakışan işlemleri sıraya sokup **bekletmez**, çakışmayı tespit ettiğinde işlemlerden birini `40001` (serialization_failure) hatasıyla **iptal eder**. Dolayısıyla seviyeyi yükseltmek "kodu değiştirmeden güvenlik kazanmak" değildir; uygulamanın hatayı yakalayıp işlemi **baştan yeniden denemesi** (retry) zorunlu hâle gelir — yeniden deneme yoksa üretimde rastgele başarısızlıklar görürsün (A yanlış). `LOCK TABLE` gerekmez; garantiyi motorun kendisi verir ve elle tablo kilidi ölçeklenmeyi öldürür (B). `readOnly = true` ise Hibernate'e verilen bir optimizasyon ipucudur, izolasyonla ilgisi yoktur ve serileştirme hatasına karşı koruma sağlamaz (C).

# txizol-4 | İleri
## Soru
Bir müşterinin toplam riski iki ayrı satırda tutuluyor (ihtiyaç kredisi ve taşıt kredisi) ve kural şu: iki satırın toplamı 100.000'i geçemez. Her işlem önce iki satırı da okuyup toplamı doğruluyor, sonra yalnız **kendi** satırını güncelliyor. Anlık görüntü izolasyonu (snapshot isolation) altında iki işlem eşzamanlı çalışırsa ne olur?
## A
Sorun çıkmaz; anlık görüntü izolasyonu her işleme tutarlı bir görüntü verdiği için bilinen bütün serileştirme anomalilerini engeller ve kısıt her koşulda korunur.
🤖 anlık görüntü izolasyonu tutarlı okuma verir ama yazma çarpıklığını engellemez.
## B
Yazma çarpıklığı (write skew) oluşur; ikisi de kısıtın sağlandığı aynı görüntüyü okur, farklı satırları günceller ve çakışma tespit edilemediği için toplam 100.000'i aşar.
🤖 Doğru: farklı satırlara yazıldığı için motor çakışmayı göremez, kural uygulama düzeyinde ihlal edilir.
## C
Kayıp güncelleme (lost update) oluşur; ikinci işlemin yazdığı değer birincinin yazdığı değeri ezer ve iki güncellemeden biri sonuçta hiç uygulanmamış olur.
🤖 iki işlem farklı satırlara yazar, hiçbir güncelleme ezilmez; ikisi de kalıcı olur.
## D
İkinci işlem birincinin bitmesini bekler; anlık görüntü izolasyonunda okuma da satırları kilitlediği için iki işlem kendiliğinden sıraya girer ve kural korunur.
🤖 anlık görüntü izolasyonunda okuyucular kilit tutmaz, dolayısıyla ikinci işlem beklemez.
## Doğru
B
## Açıklama
Bu **yazma çarpıklığının** (write skew) ders kitabı örneğidir. Her iki işlem de başlangıç anlık görüntüsünde toplamın 100.000'in altında olduğunu görür ve kendi açısından kuralı sağlayan bir güncelleme yapar; ancak **farklı satırlara** yazdıkları için veritabanı ortada bir çakışma olduğunu göremez — motor satırlar arasındaki iş kuralını bilmez. İkisi de commit eder, toplam kısıt ihlal edilir. Anlık görüntü izolasyonu kirli, tekrarlanamayan ve hayalet okumayı keser ama bu anomaliyi **kesmez** (A yanlış) — Oracle'ın `SERIALIZABLE`'ı ve PostgreSQL'in `REPEATABLE READ`'i tam olarak bu boşluğa sahiptir. **Kayıp güncelleme değildir** (C), çünkü aynı satır üzerinde ezme yoktur; iki güncelleme de kalıcı olur. Okuyucuların kilit tutması da söz konusu değildir (D): MVCC'de okuma kilitsizdir. Çözüm ya PostgreSQL'in SSI destekli `SERIALIZABLE`'ı (ve yeniden deneme), ya iki satırı da kötümser kilitle okumak, ya da toplamı tek bir satırda tutup atomik koşullu güncelleme yapmaktır.

# txizol-5 | İleri
## Soru
MVCC (çok sürümlü eşzamanlılık denetimi) kullanan bir veritabanında (PostgreSQL, Oracle, MySQL/InnoDB) tutarlı okuma (consistent read) hakkında hangisi doğrudur?
## A
MVCC, okuyucuların satır üzerinde paylaşımlı kilit tutmasını sağlar; bu yüzden uzun süren raporlama sorguları o satırlara yazmak isteyen işlemleri bekletir ve yazma verimini düşürür.
🤖 MVCC'nin amacı tam tersidir; okuyucu kilit tutmaz, yazarı bekletmez.
## B
MVCC izolasyon seviyesini kendiliğinden `SERIALIZABLE`'a yükseltir; motor MVCC kullanıyorsa ayrıca bir izolasyon ayarı yapmaya gerek kalmaz.
🤖 MVCC bir gerçekleme tekniğidir, izolasyon seviyesini yükseltmez; seviye ayrıca belirlenir.
## C
MVCC sayesinde kayıp güncelleme (lost update) imkânsız hâle gelir; "oku, karar ver, yaz" akışı bu motorlarda ek bir önlem almadan güvenlidir.
🤖 MVCC okuma tarafını çözer, iki yazarın aynı satırdaki çakışmasını çözmez.
## D
Okuyucu, satırın işlem başlangıcına ait eski sürümünü görür; okuyucular yazarları, yazarlar okuyucuları bloklamaz — ancak iki yazar aynı satırda hâlâ çakışır.
🤖 Doğru: MVCC okuma-yazma engellemesini kaldırır ama yazma-yazma çakışması sürer.
## Doğru
D
## Açıklama
**MVCC**, bir satırın birden çok sürümünü saklayarak okuyucuya işleminin görebileceği **eski sürümü** göstermeye dayanır. Sonucu şudur: okuyucular yazarları, yazarlar okuyucuları **bloklamaz** — saatlerce süren bir gün sonu raporu, yazma trafiğini durdurmadan tutarlı bir fotoğraf üzerinde çalışabilir. Bu, A'daki "okuma kilidi yazmayı bekletir" beklentisinin tersidir ve MVCC'nin varlık sebebidir. Ama madalyonun öteki yüzü kritiktir: MVCC yalnız **okuma tarafını** çözer. İki işlem aynı satıra yazmaya kalkarsa biri diğerini bekler ya da anlık görüntü ihlali hatası alır; dolayısıyla **kayıp güncelleme MVCC'li motorlarda da mümkündür** ve "oku–karar ver–yaz" akışı ek önlem olmadan güvenli değildir (C). MVCC bir **gerçekleme tekniğidir**, bir izolasyon seviyesi değil: seviyeyi yükseltmez, ayrıca belirlemen gerekir (B).

# txizol-6 | Uzman
## Soru
Kredi limiti düşümündeki kayıp güncellemeyi çözmek için ekip "bütün servis metotlarını `@Transactional(isolation = Isolation.SERIALIZABLE)` yapalım, iş biter" diyor. En doğru değerlendirme hangisidir?
## A
Doğru yaklaşım; `SERIALIZABLE` bütün anomalileri kesin olarak keser ve MVCC kullanan modern veritabanlarında ölçülebilir bir başarım maliyeti getirmediği için sistem geneline güvenle uygulanabilir.
🤖 bedeli vardır — kilit tabanlı motorlarda çekişme, anlık görüntü tabanlılarda iptal ve yeniden deneme.
## B
Genelde yanlış; bedeli tüm sisteme yayılır, oysa sorun tek satırdadır — atomik koşullu güncelleme ya da sürüm sütunu daha ucuz ve daha öngörülebilir bir çözümdür.
🤖 Doğru: nokta atışı çözüm, sistem geneline yayılan izolasyon maliyetinden iyidir.
## C
Gereksiz bir değişiklik; JPA'nın kalıcı bağlamı (persistence context) çakışma tespit ettiğinde izolasyon seviyesini kendiliğinden yükselttiği için Hibernate kullanan uygulamalar bu anomaliden zaten korunmaktadır.
🤖 Hibernate izolasyon seviyesini yükseltmez; kalıcı bağlam yalnız bir birinci seviye önbellektir.
## D
Yetersiz bir değişiklik; hiçbir izolasyon seviyesi kayıp güncellemeyi engelleyemediği için tek geçerli çözüm veritabanı dışında tutulan bir dağıtık kilit mekanizmasıdır.
🤖 SERIALIZABLE kayıp güncellemeyi engeller; sorun engelleyip engellememesi değil, bedelidir.
## Doğru
B
## Açıklama
`SERIALIZABLE` kayıp güncellemeyi gerçekten engeller (D yanlış), ama bedeli **bedava değildir** (A yanlış) ve bedel iki biçimde ödenir: kilit tabanlı motorlarda **çekişme ve bekleme**, anlık görüntü tabanlı motorlarda (PostgreSQL SSI) **iptal ve yeniden deneme** — her iki durumda da yük arttıkça verim düşer. Kritik nokta şudur: bu maliyet **tüm sisteme** yayılır, oysa çakışma genelde birkaç sıcak satırda yaşanır. Bu yüzden refleks, seviyeyi kör bir şekilde yükseltmek değil, "oku–karar ver–yaz" boşluğunu **yerel olarak** kapatmaktır: ya `UPDATE ... WHERE ... AND kullanilan + :tutar <= limit` biçiminde **atomik koşullu güncelleme** yapıp etkilenen satır sayısını kontrol etmek, ya bir **sürüm sütunuyla** (`@Version`) çakışmayı yazma anında yakalayıp yeniden denemek, ya da gerçekten dar bir bölge varsa o satırı **kötümser kilitle** sıraya sokmak. C ise yaygın bir yanlış inanıştır: Hibernate izolasyon seviyesini kendiliğinden yükseltmez; kalıcı bağlam sadece aynı işlem içinde aynı nesneyi geri vererek sorunu **gizler**, çözmez.
