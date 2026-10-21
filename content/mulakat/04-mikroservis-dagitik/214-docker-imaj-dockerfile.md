---
slug: docker-imaj-dockerfile
baslik: "Docker İmajı ve Dockerfile: Katmanlar, Önbellek, Çok-Aşamalı Derleme ve Güvenli Taban"
kademe: Orta
sira: 214
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir pastanenin tarif defterini düşün. **Dockerfile** o defterdeki tariftir: hangi malzeme, hangi sırayla, kaç derecede. Tarifi bir kez uygularsın ve ortaya dondurulmuş, paketlenmiş bir ürün çıkar — bu **imajdır (image)**; değişmez, kopyalanabilir, rafta durur. Müşteri sipariş verdiğinde paketi açıp ısıtırsın; işte o sıcak tabak **konteynerdir (container)**. Aynı paketten yüz tabak çıkarabilirsin, hepsi birbirinin aynıdır ve tabağa dökülen sos paketi değiştirmez. Bankada bunun karşılığı şudur: test ortamında doğrulanan kredi başvuru servisi imajı, üretimde bit bit aynı imajdır; "bende çalışıyordu" tartışması ortadan kalkar.

### Temel kavramlar

- **İmaj (image):** uygulamanın ve çalışması için gereken her şeyin (kütüphaneler, Java çalışma ortamı, işletim sistemi dosyaları) dondurulmuş, **salt-okunur** şablonu. Kayıt defterinde (registry) saklanır.
- **Konteyner (container):** çalışan imaj örneği. İmajın salt-okunur katmanlarının üstüne ince bir **yazılabilir katman** eklenir; konteyner içinde oluşturulan dosyalar oraya yazılır ve konteyner silinince **kaybolur**. Aynı imajdan aynı anda onlarca bağımsız konteyner çalıştırılabilir.
- **Katman (layer):** Dockerfile'daki her `FROM`, `RUN`, `COPY`, `ADD` talimatı bir katman üretir. Katmanlar **eklemelidir (additive)**: bir katmanda yazılan şey, sonraki katmanda silinse bile imajın içinde durmaya devam eder.
- **Derleme önbelleği (build cache):** bir talimatın girdisi (talimat metni ve kopyalanan dosyaların içeriği) değişmediyse Docker o katmanı yeniden üretmez, önbellekten alır. Önbellek **zincirlidir ve yukarıdan aşağı akar**: bir katman geçersizleşirse ondan sonraki bütün katmanlar da geçersizleşir. Bu yüzden **nadiren değişen adımlar yukarıya, sık değişenler aşağıya** yazılır.
- **Derleme bağlamı (build context):** `docker build .` dediğinde noktayla gösterilen dizindeki **her şey** derleyiciye gönderilir. `target/`, `.git/`, yerel anahtar dosyaları da dahil. **`.dockerignore`** bu gönderimi süzer; hem derlemeyi hızlandırır hem `COPY . .` ile gizli dosyaların imaja sızmasını engeller. Docker `.gitignore` dosyasını okumaz.

```svg
<svg viewBox="0 0 660 255" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dockerfile talimat sırasının derleme önbelleğine etkisi">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Talimat sırası önbelleği belirler</text>
  <rect x="22" y="42" width="302" height="196" rx="12" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="173" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Kötü sıra — önbellek boşa gider</text>
  <text x="40" y="92" font-size="11" fill="#1C3247" font-family="system-ui">COPY . .</text>
  <text x="40" y="109" font-size="11" fill="#1C3247" font-family="system-ui">RUN mvn package</text>
  <text x="40" y="141" font-size="10.5" fill="#5D7789" font-family="system-ui">tek satır kod değişti →</text>
  <text x="40" y="158" font-size="10.5" fill="#5D7789" font-family="system-ui">COPY katmanı geçersizleşir →</text>
  <text x="40" y="175" font-size="10.5" fill="#5D7789" font-family="system-ui">bağımlılıklar yeniden indirilir</text>
  <text x="40" y="196" font-size="10.5" fill="#5D7789" font-family="system-ui">önbellek zinciri baştan kırılır</text>
  <text x="40" y="225" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">derleme: dakikalar</text>
  <rect x="336" y="42" width="302" height="196" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="487" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">İyi sıra — önbellek tutar</text>
  <text x="354" y="90" font-size="11" fill="#1C3247" font-family="system-ui">COPY pom.xml .</text>
  <text x="354" y="107" font-size="11" fill="#1C3247" font-family="system-ui">RUN mvn dependency:go-offline</text>
  <text x="354" y="124" font-size="11" fill="#1C3247" font-family="system-ui">COPY src ./src</text>
  <text x="354" y="141" font-size="11" fill="#1C3247" font-family="system-ui">RUN mvn package</text>
  <text x="354" y="168" font-size="10.5" fill="#5D7789" font-family="system-ui">kod değişti → ilk iki katman</text>
  <text x="354" y="185" font-size="10.5" fill="#5D7789" font-family="system-ui">önbellekten gelir, yalnız son</text>
  <text x="354" y="202" font-size="10.5" fill="#5D7789" font-family="system-ui">iki adım yeniden çalışır</text>
  <text x="354" y="225" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">derleme: saniyeler</text>
</svg>
```

- **Dockerfile talimatları:**
  - **`FROM`** taban imajı seçer ve yeni bir derleme aşaması başlatır.
  - **`WORKDIR`** sonraki talimatların çalışacağı dizini belirler (`cd` yerine kullanılır; `RUN cd ...` sonraki talimata taşınmaz).
  - **`COPY`** ile **`ADD`** farkı: `COPY` sadece dosya kopyalar; `ADD` ayrıca yerel tar arşivini otomatik açar ve uzak adresten indirebilir. Bu "sihirli" davranışlar sürpriz ürettiği için **kural: `COPY` kullan**, arşiv açman gerekiyorsa açık bir `RUN` yaz.
  - **`RUN`** derleme sırasında komut çalıştırır ve **katman üretir**.
  - **`USER`** o noktadan sonraki komutların hangi kullanıcıyla çalışacağını belirler; üretim imajı **root olmayan (non-root)** bir kullanıcıyla çalışmalıdır.
  - **`ENV` ile `ARG` farkı:** `ARG` yalnız **derleme zamanında** vardır, çalışan konteynerde görünmez; `ENV` ise imaja işlenir ve **çalışma zamanında** ortam değişkeni olarak durur. İkisi de **gizli bilgi için uygun değildir**: `ENV` değeri imajda ve `docker inspect` çıktısında, `ARG` değeri ise imaj geçmişinde (`docker history`) görülebilir.
  - **`EXPOSE`** yalnız **belgeleme** amaçlıdır: "bu imaj 8080'i dinler" der, hiçbir portu dışarı açmaz; yayın `-p 8080:8080` ile yapılır.
  - **`ENTRYPOINT` ile `CMD`:** `ENTRYPOINT` sabit çalıştırılabilir komuttur, `CMD` ona **varsayılan argümanları** verir. `docker run imaj <argümanlar>` yazdığında bu argümanlar **`CMD`'nin yerine geçer**; `ENTRYPOINT` yerinde kalır. Onu değiştirmek için `--entrypoint` bayrağı gerekir. Yalnız `CMD` yazılmışsa o, varsayılan komut olur.
  - **Exec biçimi ile kabuk biçimi (exec form / shell form):** `ENTRYPOINT ["java","-jar","app.jar"]` exec biçimidir ve JVM'i doğrudan ana süreç yapar. `ENTRYPOINT java -jar app.jar` ise kabuk biçimidir; komutu `/bin/sh -c` sarmalar. Üretim imajlarında **exec biçimi** tercih edilir, çünkü kapanış sinyali doğrudan uygulamaya ulaşır.
- **Çok-aşamalı derleme (multi-stage build):** tek Dockerfile'da birden çok `FROM`. İlk aşamada JDK ve Maven ile derlersin, son aşamada yalnız çıktıyı `COPY --from=builder` ile ince bir çalışma zamanı imajına alırsın. Derleyici, Maven deposu ve kaynak kod **son imaja girmez**.

```svg
<svg viewBox="0 0 660 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Çok aşamalı derlemede builder aşaması ve çalışma zamanı aşaması">
  <defs>
    <marker id="dockerimg-ms-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Çok-aşamalı derleme: derleyici son imaja girmez</text>
  <rect x="22" y="52" width="246" height="136" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="145" y="76" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">1. aşama — builder</text>
  <text x="38" y="100" font-size="10.5" fill="#1C3247" font-family="system-ui">FROM maven:3.9-temurin-21</text>
  <text x="38" y="117" font-size="10.5" fill="#1C3247" font-family="system-ui">AS builder</text>
  <text x="38" y="140" font-size="10.5" fill="#5D7789" font-family="system-ui">JDK + Maven + kaynak kod</text>
  <text x="38" y="157" font-size="10.5" fill="#5D7789" font-family="system-ui">yaklaşık 700 MB</text>
  <text x="38" y="177" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">son imajda YOK</text>
  <path d="M270 118 L386 118" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dockerimg-ms-arw)"/>
  <text x="328" y="104" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">COPY --from=builder</text>
  <text x="328" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yalnız jar / katmanlar</text>
  <rect x="390" y="52" width="248" height="136" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="514" y="76" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">2. aşama — çalışma zamanı</text>
  <text x="406" y="100" font-size="10.5" fill="#1C3247" font-family="system-ui">FROM eclipse-temurin:21-jre</text>
  <text x="406" y="117" font-size="10.5" fill="#1C3247" font-family="system-ui">USER 10001 (root değil)</text>
  <text x="406" y="140" font-size="10.5" fill="#5D7789" font-family="system-ui">yalnız uygulama + JRE</text>
  <text x="406" y="157" font-size="10.5" fill="#5D7789" font-family="system-ui">yaklaşık 180 MB</text>
  <text x="406" y="177" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">küçük boyut = az saldırı yüzeyi</text>
</svg>
```

- **Taban imaj seçimi:** tam JDK gereksiz büyüktür; çalışma zamanında **JRE** yeter. `-jre` biçimindeki ince (**slim**) imajlar makul bir varsayılandır. **Araçsız (distroless)** imajlarda kabuk ve paket yöneticisi bile yoktur. Saldırı yüzeyi en azdır, ama içine girip hata ayıklamak zordur. **alpine** en küçüğüdür; ancak glibc yerine **musl** adlı C kütüphanesini kullanır. Bu yüzden glibc'ye bağlı **yerel (native) kütüphaneler** kırılabilir. JVM tarafında alpine'ın kazandırdığı birkaç on megabayt, taşıdığı uyumluluk riskine çoğu zaman değmez.
- **İmaj boyutu ile saldırı yüzeyi ilişkisi:** imajdaki her paket, potansiyel bir güvenlik açığı kaynağıdır; bu açıklar **CVE** numarasıyla kayda geçer. Küçük imaj hem daha hızlı çekilir hem taranacak/yamalanacak daha az bileşen içerir.

```svg
<svg viewBox="0 0 660 270" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Taban imaj seçenekleri: boyut ve risk karşılaştırması">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Taban imaj seçimi: boyut, saldırı yüzeyi ve risk</text>
  <rect x="215" y="48" width="330" height="22" rx="5" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.2"/>
  <text x="207" y="64" text-anchor="end" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">JDK tam</text>
  <text x="553" y="64" font-size="10.5" fill="#5D7789" font-family="system-ui">~450 MB</text>
  <text x="215" y="86" font-size="10" fill="#5D7789" font-family="system-ui">derleyici ve derleme araçları çalışma zamanında gereksiz</text>
  <rect x="215" y="100" width="150" height="22" rx="5" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.2"/>
  <text x="207" y="116" text-anchor="end" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">JRE slim</text>
  <text x="373" y="116" font-size="10.5" fill="#5D7789" font-family="system-ui">~180 MB</text>
  <text x="215" y="138" font-size="10" fill="#5D7789" font-family="system-ui">makul varsayılan: Debian tabanlı, glibc uyumlu</text>
  <rect x="215" y="152" width="95" height="22" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="207" y="168" text-anchor="end" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">alpine (musl)</text>
  <text x="318" y="168" font-size="10.5" fill="#5D7789" font-family="system-ui">~90 MB</text>
  <text x="215" y="190" font-size="10" fill="#B23B3B" font-family="system-ui">musl, glibc değildir: glibc'ye bağlı yerel kütüphaneler kırılabilir</text>
  <rect x="215" y="204" width="80" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="207" y="220" text-anchor="end" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">distroless</text>
  <text x="303" y="220" font-size="10.5" fill="#5D7789" font-family="system-ui">~80 MB</text>
  <text x="215" y="242" font-size="10" fill="#5D7789" font-family="system-ui">kabuk ve paket yöneticisi yok: en az saldırı yüzeyi, hata ayıklaması zor</text>
  <text x="330" y="262" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">boyut küçüldükçe saldırı yüzeyi azalır, uyumluluk ve hata ayıklama zorlaşır</text>
</svg>
```

- **Katmanlı jar (layered jar):** Spring Boot'un ürettiği tek dosyalık jar'ı olduğu gibi kopyalarsan, tek satır kod değişiminde 60 MB'lık katmanı baştan üretirsin. Spring Boot bu jar'ı **katmanlara ayırmayı** destekler: bağımlılıklar, anlık görüntü (snapshot) bağımlılıkları, yükleyici ve uygulama sınıfları ayrı katmanlara gider. `jarmode` ile çıkarılan bu katmanlar ayrı `COPY` talimatlarıyla imaja konur. Nadiren değişen bağımlılık katmanı önbellekte kalır, sık değişen küçük uygulama katmanı yeniden üretilir.
- **Dockerfile yazmadan imaj üretmek:** **Jib** (Maven/Gradle eklentisi) Docker sunucusuna bile ihtiyaç duymadan katmanlı ve **yeniden-üretilebilir** imaj basar. **Cloud Native Buildpacks** (Spring Boot'ta `spring-boot:build-image`) taban imajı, JRE'yi ve katmanlamayı senin yerine seçer. İkisi de Dockerfile bakım yükünü kaldırır. Karşılığında taban imaj üzerindeki denetimin azalır. Kapalı ağdaki bir bankada onaylı taban imaj zorunluysa bu bir kısıttır.
- **Etiket (tag) ile içerik özeti (digest):** etiket, kayıt defterinde **başka bir imaja yeniden bağlanabilen taşınabilir bir işaretçidir**. Yani `kredi-servis:1.4.2` etiketi bugün başka, yarın başka bir imajı gösterebilir. **İçerik özeti (digest, `sha256:...`)** ise imajın içeriğinden hesaplanır ve **hiç değişmez**. `latest` de "en yeni" demek değildir; etiket verilmediğinde kullanılan **varsayılan addır**. Üretimde yeniden-üretilebilirlik için imaj ya digest ile ya da asla üzerine yazılmayan bir sürüm etiketiyle sabitlenir.

### Nasıl çalışır?

1. **Bağlamı süz.** `.dockerignore` dosyasına `target/`, `.git/`, `*.log`, yerel gizli dosyalar yazılır; derleyiciye yalnız gereken gider.
2. **Derleme aşamasını yaz.** `FROM maven:...-temurin-21 AS builder`; önce `COPY pom.xml .` ve bağımlılık indirme, **sonra** `COPY src ./src` ve derleme. Sıra önbelleği belirler.
3. **Çalışma zamanı aşamasını yaz.** `FROM eclipse-temurin:21-jre`; `COPY --from=builder` ile yalnız jar ya da çıkarılmış katmanlar alınır.
4. **Kullanıcıyı düşür.** İmajda root olmayan bir kullanıcı oluşturulup `USER` ile ona geçilir; konteyner kaçışı (container escape) durumunda saldırganın eline root yetkisi geçmez.
5. **Giriş noktasını exec biçiminde ver.** `ENTRYPOINT ["java","-jar","/app/app.jar"]`; varsayılan argüman gerekiyorsa `CMD` ile eklenir.
6. **Etiketle ve bas.** İmaj anlamlı bir sürüm etiketiyle kayıt defterine gönderilir; dağıtım bildiriminde digest ile sabitlenir.
7. **Aynı imajı her ortamda kullan.** Yapılandırma imaja gömülmez, dışarıdan verilir; test edilen imaj birebir üretime çıkar.

### Mülakatta ne sorulur / tuzaklar

- **"Katman önbelleği neden çalışmıyor?"** En sık sebep `COPY . .` talimatının bağımlılık indirmeden **önce** gelmesidir. Önbellek zincirlidir: üstteki bir katman geçersizleşince altındaki her şey yeniden çalışır. `--no-cache` ile derlemek çözüm değil, sorunun kalıcılaştırılmasıdır.
- **"Katmandan dosya silmek onu imajdan çıkarır mı?"** Hayır. Katmanlar eklemelidir; bir katmanda yazılan gizli dosya sonraki katmanda `rm` ile silinse bile önceki katmanda ve imaj geçmişinde durur. Gizli bilgi imaja hiç girmemelidir.
- **`ENTRYPOINT` ile `CMD` karıştırılması.** Beklenen cevap nettir: `ENTRYPOINT` sabit komut, `CMD` varsayılan argüman; `docker run` sonundaki argümanlar `CMD`'yi ezer, `ENTRYPOINT`'i ezmez.
- **`EXPOSE` port açar mı?** Açmaz, yalnız belgeler. "EXPOSE yazdım ama dışarıdan erişemiyorum" klasik yanılgıdır; yayın `-p` ile yapılır.
- **`COPY` yerine `ADD`.** `ADD`'in arşiv açma ve uzak indirme davranışı öngörülemez sonuç ve güvenlik riski üretir; varsayılan `COPY`'dir.
- **`latest` etiketi.** "En yeni sürümü çeker" cevabı elenme sebebidir. `latest` yalnız varsayılan etiket adıdır ve üzerine yazılabilir; belirleyicilik digest ile sağlanır.
- **Alpine refleksi.** "En küçük imaj alpine, hep onu kullanırım" cevabı JVM tarafında risklidir: musl ile glibc farkı yerel kütüphanelerde kırılmaya yol açabilir. Kazanılan boyut, alınan riskle karşılaştırılmalıdır.
- **Root ile çalıştırma.** Üretim imajının root ile çalışması denetimde (audit) doğrudan bulgu üretir; `USER` talimatı zorunlu kabul edilmelidir.

### Tek cümlede

Dockerfile bir tariftir. Talimat sırası derleme önbelleğini belirler; çok-aşamalı derleme ve ince taban imaj, boyutu ve saldırı yüzeyini küçültür. Katmanlı jar yeniden derleme hızını, `USER` güvenliği, digest ile sabitleme de yeniden-üretilebilirliği belirler.

# docker-1 | Orta
## Soru
Kredi başvuru servisinin imajını her kod değişikliğinde yeniden inşa ediyorsun; derleme her seferinde tüm Maven bağımlılıklarını baştan indirdiği için dakikalar sürüyor. Dockerfile'da hangi düzenleme derleme önbelleğinden (build cache) en çok yararlanır?
## A
Önce `COPY . .` yapıp hemen ardından `RUN mvn package` çalıştırmak; her şey tek adımda kopyalandığı için katman sayısı azalır ve derleme sadeleşir
🤖 tek satırlık kod değişikliği bu `COPY` katmanını geçersizleştirir ve altındaki bağımlılık indirme dahil her adım yeniden çalışır.
## B
Bağımlılık indirmeyi Dockerfile'ın en sonuna, kaynak kodu kopyalamayı en başa almak; sık değişen adım önce çalışınca derleme hızlanır
🤖 önbellek yukarıdan aşağı zincirlidir; sık değişen adımı yukarı almak altındaki bütün katmanları her derlemede geçersizleştirir.
## C
Önce `COPY pom.xml .` ve bağımlılıkları indiren `RUN` adımını, sonra `COPY src ./src` ve `RUN mvn package` adımlarını yazmak
🤖 Doğru: nadiren değişen bağımlılık adımı üstte kaldığı için kod değişiminde önbellekten gelir, yalnız alttaki iki adım yeniden çalışır.
## D
Her derlemeyi `--no-cache` ile yapmak; böylece bayat katman kalmaz ve derleme sonucu öngörülebilir olur
🤖 bu önbelleği tamamen kapatır ve her derlemeyi en yavaş hâline sabitler; sorunu çözmez, kalıcılaştırır.
## Doğru
C
## Açıklama
Docker'ın **derleme önbelleği** talimat bazında ve **zincirlidir**: bir talimatın girdisi (talimat metni ve kopyalanan dosyaların içeriği) değişmediyse katman yeniden üretilmez, ama bir katman geçersizleşirse **altındaki bütün katmanlar** da geçersizleşir. Bu yüzden Dockerfile'da **nadiren değişen adımlar yukarıya, sık değişenler aşağıya** yazılır. Java projesinde bağımlılık listesi (`pom.xml`) nadiren, kaynak kod her gün değişir; doğru düzen önce `pom.xml`'i kopyalayıp bağımlılıkları indirmek, ardından kaynağı kopyalayıp derlemektir. `COPY . .` ile başlamak tam tersini yapar: tek karakterlik değişiklik bile önbelleği en tepeden kırar ve tüm bağımlılıklar yeniden indirilir. Sık değişen adımı bilinçli olarak en üste almak aynı hatanın abartılmış hâlidir. `--no-cache` ise önbelleği kapatır; "temizlik" sağlar ama her derlemeyi kalıcı olarak en yavaş senaryoya sabitler — bu bir çözüm değil, sorunu kabullenmektir.

# docker-2 | Temel
## Soru
İmaj (image) ile konteyner (container) arasındaki fark nedir ve konteyner çalışırken oluşturulan dosyalara ne olur?
## A
İmaj salt-okunur katmanlardan oluşan değişmez şablon, konteyner ise üstüne ince bir yazılabilir katman eklenmiş çalışan örneğidir; konteyner silinince o katmandaki veriler kaybolur
🤖 Doğru: imaj değişmez şablon, konteyner ise üstüne geçici yazılabilir katman eklenmiş çalışan örnektir.
## B
İmaj ile konteyner aynı şeyin iki adıdır; tek fark imajın kayıt defterinde (registry), konteynerin ana makinede durmasıdır
🤖 aynı imajdan aynı anda birbirinden bağımsız onlarca konteyner çalıştırılabilir; ikisi ayrı kavramdır.
## C
Konteyner şablondur, imaj ise o şablonun çalışan hâlidir; çalışırken yazılan veriler imaja işlendiği için kalıcı olur
🤖 roller ters verilmiş ve imaj çalışma sırasında hiç değişmez, salt-okunurdur.
## D
Konteynerde oluşan dosyalar arka planda imaja geri yazılır; bu yüzden konteyner silinip yeniden yaratıldığında veri korunur ve ayrıca volume tanımlamaya gerek kalmaz
🤖 imaj değişmezdir ve otomatik geri yazma diye bir mekanizma yoktur; kalıcılık için volume gerekir.
## Doğru
A
## Açıklama
**İmaj**, uygulamanın ve çalışması için gereken her şeyin **salt-okunur katmanlardan** oluşan dondurulmuş şablonudur; kayıt defterinde saklanır ve çalışma sırasında **hiç değişmez**. **Konteyner**, o imajdan üretilmiş çalışan örnektir: imajın katmanlarının üstüne ince bir **yazılabilir katman** eklenir ve konteyner içinde oluşturulan/değiştirilen dosyalar oraya yazılır. Bu katman **konteynerin ömrüne bağlıdır** — konteyner silindiğinde veri de gider. Aynı imajdan aynı anda çok sayıda bağımsız konteyner çalıştırılabildiği için ikisini "aynı şeyin iki adı" saymak yanlıştır; rolleri ters çevirmek (konteyneri şablon sanmak) da yanlıştır. Konteynerdeki değişikliklerin imaja "geri yazıldığı" inancı ise en tehlikelisidir: böyle bir otomatik mekanizma yoktur, imaj değişmez kabul edilir. Bu yüzden veritabanı dosyası, yüklenen belge, mutabakat çıktısı gibi kalıcı olması gereken veriler konteyner dosya sisteminde değil, dışarıda (volume, veritabanı, nesne deposu) tutulur.

# docker-3 | Orta
## Soru
Dockerfile'da `ENTRYPOINT ["java","-jar","app.jar"]` ve `CMD ["--spring.profiles.active=test"]` birlikte yazılmış. `docker run kredi-servis --spring.profiles.active=prod` komutu çalıştırıldığında ne olur?
## A
`ENTRYPOINT` ile `CMD` aynı işi yapar; ikisi de yazıldığında Docker iki komutu sırayla çalıştırır ve iki süreç başlatılır
🤖 ikisi ayrı rollerdedir ve iki süreç başlatılmaz; `CMD`, `ENTRYPOINT`'e argüman olarak eklenir.
## B
`ENTRYPOINT` sabit komuttur ve değişmez; komut satırına yazılan argüman `CMD`'nin yerine geçer, yani uygulama `prod` profiliyle başlar
🤖 Doğru: `docker run` sonundaki argümanlar `CMD`'yi ezer, `ENTRYPOINT` yerinde kalır (onu değiştirmek `--entrypoint` gerektirir).
## C
Komut satırına yazılan argüman `ENTRYPOINT`'in yerine geçer, `CMD` ise her durumda korunur; uygulama `test` profiliyle başlar
🤖 tam tersi geçerlidir — ezilen `CMD`'dir, `ENTRYPOINT` ancak `--entrypoint` bayrağıyla değiştirilir.
## D
`CMD` yalnız belgeleme amaçlıdır ve çalışma zamanında hiçbir etkisi yoktur; argüman doğrudan yok sayılır
🤖 belgeleme amaçlı olan `EXPOSE`'dur; `CMD` gerçekten varsayılan komutu veya argümanı belirler.
## Doğru
B
## Açıklama
İki talimatın rolleri farklıdır. **`ENTRYPOINT`** konteynerin **sabit çalıştırılabilir komutunu** belirler — burada JVM'i başlatan `java -jar app.jar`. **`CMD`** ise bu komuta verilecek **varsayılan argümanları** tutar. `docker run imaj <argümanlar>` biçiminde komut satırına argüman yazıldığında bu argümanlar **`CMD`'nin yerine geçer**, `ENTRYPOINT` ise **olduğu gibi kalır**; sonuç `java -jar app.jar --spring.profiles.active=prod` olur. `ENTRYPOINT`'i değiştirmek istersen ayrıca `--entrypoint` bayrağı gerekir. Bu yüzden "argüman ENTRYPOINT'i ezer, CMD korunur" ifadesi tersine çevrilmiş bir bilgidir. İkisinin aynı olduğunu ve iki süreç başlatıldığını söylemek de yanlıştır: tek süreç başlar, `CMD` yalnız argüman listesine eklenir. `CMD`'nin belgeleme amaçlı olduğu iddiası ise **`EXPOSE`** ile karıştırmadan doğar — `EXPOSE` gerçekten yalnız belgeler ve hiçbir port açmaz, `CMD` ise çalışma zamanı davranışını doğrudan belirler. Ek not: `ENTRYPOINT`'i **exec biçiminde** (köşeli parantezli dizi) yazmak önemlidir; kabuk biçiminde yazıldığında komut `/bin/sh -c` içine sarmalanır.

# docker-4 | İleri
## Soru
`docker build .` komutu küçük bir Spring Boot projesinde bile "sending build context" adımında 1,5 GB veri gönderiyor ve derlemeler yavaşlıyor. Sebep ve doğru çözüm nedir?
## A
Derleme bağlamı yalnız Dockerfile'ın kendisidir; bu boyut taban imajdan gelir, çözüm daha küçük bir taban imaj seçmektir
🤖 bağlam, belirtilen dizindeki tüm dosyalardır; taban imaj ayrı çekilir ve gönderilen bağlamın boyutunu belirlemez.
## B
`.gitignore` dosyası Docker tarafından da okunur; oraya `target/` ve `.git/` eklemek bağlamı küçültür
🤖 Docker `.gitignore` dosyasını okumaz; bağlamı süzen dosya `.dockerignore`'dur.
## C
Sorun `RUN` talimatlarının çokluğudur; hepsini `&&` ile tek satırda birleştirmek gönderilen bağlamı küçültür
🤖 talimatları birleştirmek katman sayısını etkiler, derleyiciye gönderilen bağlamın boyutunu değil.
## D
Belirtilen dizindeki her şey (`target/`, `.git/`, yerel yapılandırma ve anahtar dosyaları dahil) derleyiciye gönderilir; bunlar `.dockerignore` ile dışlanmalıdır
🤖 Doğru: bağlam dizinin tamamıdır ve `.dockerignore` hem gönderimi küçültür hem gereksiz dosyaların imaja sızmasını engeller.
## Doğru
D
## Açıklama
`docker build .` komutundaki nokta **derleme bağlamıdır (build context)**: o dizindeki **her şey** derleyiciye gönderilir. Java projesinde bu genellikle `target/` altındaki derleme çıktıları, `.git/` geçmişi, yerel ayar ve anahtar dosyaları demektir — hem yüzlerce megabayt gereksiz transfer hem üç ayrı risk. Birincisi yavaşlık. İkincisi **önbellek kaybı**: `COPY . .` yazıldığında bağlamdaki herhangi bir dosyanın (derleme çıktısı, log, düzenleyici geçici dosyası) değişmesi katmanı geçersizleştirir. Üçüncüsü **sızıntı**: `COPY . .` bu dosyaları imaja da taşır ve imaj katmanları eklemeli olduğu için sonradan silmek onları imajdan çıkarmaz. Çözüm **`.dockerignore`** dosyasıdır; Docker `.gitignore` dosyasını okumaz, bu ikisi ayrı dosyalardır. Taban imajı küçültmek son imajın boyutunu düşürür ama gönderilen bağlamla ilgisi yoktur; `RUN` talimatlarını `&&` ile birleştirmek de katman sayısını azaltır, bağlamı değil. Doğru refleks: her Dockerfile'ın yanında bir `.dockerignore` bulunmasıdır.

# docker-5 | İleri
## Soru
Bankanın üretim dağıtım bildiriminde imaj `kredi-servis:latest` olarak yazılı. Hiçbir şey değiştirilmeden yapılan iki dağıtım farklı davranıyor. Bunun nedeni ve doğru pratik nedir?
## A
`latest` etiketi Docker tarafından her zaman en yeni sürüme çözülür; sorun ağ önbelleğidir, imajı `--pull` ile çekmek yeterlidir
🤖 `latest` "en yeni" anlamına gelmez, yalnız varsayılan etiket adıdır; asıl sorun etiketin taşınabilir bir işaretçi olmasıdır.
## B
Etiketler değişmezdir ve aynı etiket her zaman aynı içeriği gösterir; farklılık ancak dış yapılandırmadan kaynaklanabilir
🤖 aynı etiket kayıt defterinde yeni bir imaja bağlanabilir; değişmez olan etiket değil içerik özetidir (digest).
## C
Etiket, kayıt defterinde başka bir imaja yeniden bağlanabilen taşınabilir bir işaretçidir; yeniden-üretilebilirlik için imaj içerik özetiyle (digest) sabitlenmelidir
🤖 Doğru: etiket taşınabilir, digest içeriğin değişmez özetidir; belirleyicilik ancak sabitlemeyle sağlanır.
## D
Sorun imaj çekme politikasıdır; imajın hiç çekilmemesini sağlayıp düğümdeki mevcut kopyayı kullanmak dağıtımı belirleyici hâle getirir
🤖 bu, hangi düğümde hangi eski kopyanın bulunduğuna göre değişen daha belirsiz bir davranış üretir.
## Doğru
C
## Açıklama
Kayıt defterinde **etiket (tag)**, bir imajın içerik özetine işaret eden **taşınabilir bir isimdir**. Aynı isim üzerine yeni bir imaj basılabilir; dolayısıyla `kredi-servis:latest` bugün başka, yarın başka bir içeriği gösterebilir — bildirim metni değişmese bile dağıtılan yazılım değişir. **`latest` "en yeni" demek değildir**; etiket belirtilmediğinde kullanılan **varsayılan addır** ve otomatik olarak hiçbir şeye çözülmez, bu yüzden `--pull` eklemek belirleyicilik sağlamaz. Etiketlerin değişmez olduğunu varsaymak da yanlıştır: değişmez olan **digest**'tir (`sha256:...`), yani imaj içeriğinin kriptografik özeti. Doğru pratik, üretim bildirimlerinde imajı **digest ile sabitlemek** ya da kurumsal olarak "bir kez basılan sürüm etiketi asla üzerine yazılmaz" kuralını uygulamaktır; böylece aynı bildirim her zaman aynı ikili dosyayı dağıtır ve olay incelemesinde "o gün hangi imaj çalışıyordu" sorusu kesin cevaplanır. İmajı hiç çekmemek ise belirleyicilik değil, düğümden düğüme değişen bir belirsizlik üretir.

# docker-6 | İleri
## Soru
Ekip, imaj boyutunu düşürmek için Spring Boot servisinin taban imajını `eclipse-temurin:21-jre` yerine alpine tabanlı bir imaja çevirmek istiyor. Bu değişikliğin en önemli teknik riski nedir?
## A
Alpine tabanlı imajlar root olmayan kullanıcıyla çalıştırılamaz; bu yüzden değişiklik güvenlik seviyesini düşürür
🤖 root olmayan kullanıcıyla çalışma taban imajdan bağımsızdır ve `USER` talimatıyla her tabanda ayarlanabilir.
## B
Alpine, glibc yerine musl C kütüphanesini kullanır; glibc'ye göre derlenmiş yerel (native) kütüphaneler çalışma zamanında hata verebilir
🤖 Doğru: musl ile glibc arasındaki uyumsuzluk, JNI/yerel bileşen kullanan uygulamalarda çalışma zamanı hatalarına yol açabilir.
## C
Alpine imajları yalnız 32-bit mimariyi desteklediği için 64-bit JVM çalıştırılamaz ve uygulama açılmaz
🤖 alpine 64-bit mimarileri destekler; sorun mimari değil, kullanılan C kütüphanesidir.
## D
Alpine tabanında katman önbelleği devre dışı kalır; her derleme sıfırdan yapılmak zorundadır
🤖 önbellek taban imaja değil Dockerfile talimat sırasına bağlıdır ve her tabanda aynı biçimde çalışır.
## Doğru
B
## Açıklama
İmaj boyutunu küçültmek doğru bir hedeftir — küçük imaj daha hızlı çekilir ve içinde daha az paket olduğu için **saldırı yüzeyi** daralır. Ancak alpine'ın küçüklüğünün bedeli, standart Linux dağıtımlarının kullandığı **glibc** yerine **musl** C kütüphanesini kullanmasıdır. JVM'in kendisi musl için derlenmiş sürümlerle çalışabilir, fakat uygulamanın kullandığı **yerel (native) kütüphaneler** — JNI bileşenleri, bazı ağ/şifreleme kütüphaneleri, HSM veya kart sistemi sürücüleri gibi banka ortamında sık görülen bileşenler — glibc'ye göre derlenmiş olabilir ve musl ortamında yüklenemez ya da farklı davranır. Bu hatalar çoğu zaman derlemede değil **çalışma zamanında ve üretimde** ortaya çıkar. Diğer şıklar yaygın yanlış inanışlardır: root olmayan kullanıcı taban imajdan bağımsızdır ve `USER` ile her tabanda ayarlanır; alpine 64-bit mimarileri destekler; derleme önbelleği ise taban imajla değil talimat sırasıyla belirlenir. Pratik sonuç: JVM için makul varsayılan glibc tabanlı bir **slim JRE** imajıdır; daha da küçültmek gerekiyorsa **distroless** genellikle alpine'dan daha güvenli bir tercihtir, çünkü glibc uyumluluğunu korurken kabuk ve paket yöneticisini imajdan çıkarır.
