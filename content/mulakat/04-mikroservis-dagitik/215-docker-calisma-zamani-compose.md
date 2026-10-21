---
slug: docker-calisma-zamani-compose
baslik: "Docker Çalışma Zamanı: Süreç, Sinyal, Ağ, Volume, Kaynak Sınırı ve Compose"
kademe: Orta
sira: 215
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

İmajı yazmak tarifi yazmaktır; asıl iş, o yemeğin serviste nasıl davrandığıdır. Konteyner çalışmaya başladığı andan itibaren bir **süreçtir**: kendine ait bir dosya sistemi görüntüsü, kendi ağ adresi ve kendine ayrılmış bir kaynak payı vardır. Bir otel odası gibi düşün — oda senindir, ama kapı zili (sinyal), telefon hattı (ağ), kasa (volume) ve elektrik sigortası (kaynak sınırı) otelin kurallarına tabidir. Kredili sistemlerde bu ayrıntılar doğrudan paraya dokunur: gün sonu toplu işi yarıda kesilen bir konteyner, yarım kalmış tahakkuk demektir; parolası imaja gömülmüş bir servis, denetimde doğrudan bulgudur.

### Temel kavramlar

- **Konteyner yaşam döngüsü:** `created` → `running` → (`paused`) → `exited` → `removed`. Konteyner, **ana süreci (PID 1) bittiğinde ölür**; arka planda başka bir şey çalışıyor olması onu ayakta tutmaz. Çıkış kodu önemlidir: `137` genellikle SIGKILL/bellek limiti, `143` SIGTERM ile sonlanmayı gösterir.
- **PID 1 ve sinyaller:** Konteynerin ana süreci, işletim sistemi açısından **PID 1**'dir ve özel muamele görür: açıkça bir işleyici (handler) kaydetmediyse kendisine gelen sinyaller **varsayılan olarak uygulanmaz**. `docker stop` önce **`SIGTERM`** gönderir, varsayılan **10 saniye** bekler, süre dolarsa **`SIGKILL`** gönderir. `SIGKILL` yakalanamaz ve engellenemez; uygulamanın yakalayabileceği sinyal `SIGTERM`'dir.
- **Exec biçimi ile kabuk biçimi:** `ENTRYPOINT ["java","-jar","app.jar"]` (exec biçimi) JVM'i doğrudan PID 1 yapar ve `SIGTERM` doğrudan uygulamaya ulaşır. `ENTRYPOINT java -jar app.jar` (kabuk biçimi) ise komutu `/bin/sh -c` içine sarmalar. Kabuk, sinyali kendi çocuğuna iletmeyebilir. O zaman uygulama **nazik kapanma (graceful shutdown)** fırsatı bulamaz ve 10 saniye sonra sertçe öldürülür. Spring Boot tarafında karşılığı `server.shutdown=graceful` ayarıdır: yeni istek alınmaz, süregelen istekler bitirilir, sonra çıkılır. Çocuk süreç doğuran senaryolarda `--init` bayrağı (küçük bir başlangıç süreci) sinyalleri iletir ve artık (zombie) süreçleri toplar.

```svg
<svg viewBox="0 0 660 235" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kabuk biçimi ve exec biçiminde PID 1 ve sinyal davranışı">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">PID 1 kim? Sinyal oraya gider</text>
  <rect x="22" y="44" width="302" height="176" rx="12" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="173" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Kabuk biçimi (shell form)</text>
  <text x="40" y="92" font-size="10.5" fill="#1C3247" font-family="system-ui">ENTRYPOINT java -jar app.jar</text>
  <text x="40" y="116" font-size="10.5" fill="#5D7789" font-family="system-ui">PID 1 = /bin/sh -c ...</text>
  <text x="40" y="133" font-size="10.5" fill="#5D7789" font-family="system-ui">SIGTERM önce kabuğa gider</text>
  <text x="40" y="157" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">uygulamaya iletilmeyebilir</text>
  <text x="40" y="176" font-size="10.5" fill="#5D7789" font-family="system-ui">10 sn sonra SIGKILL</text>
  <text x="40" y="200" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">süregelen istekler kaybolur</text>
  <rect x="336" y="44" width="302" height="176" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="487" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Exec biçimi (exec form)</text>
  <text x="354" y="92" font-size="10" fill="#1C3247" font-family="system-ui">ENTRYPOINT ["java","-jar","app.jar"]</text>
  <text x="354" y="116" font-size="10.5" fill="#5D7789" font-family="system-ui">PID 1 = java süreci</text>
  <text x="354" y="133" font-size="10.5" fill="#5D7789" font-family="system-ui">SIGTERM doğrudan JVM'e</text>
  <text x="354" y="157" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Spring Boot nazik kapanır</text>
  <text x="354" y="176" font-size="10.5" fill="#5D7789" font-family="system-ui">server.shutdown=graceful</text>
  <text x="354" y="200" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">istekler bitirilir, sonra çıkılır</text>
</svg>
```

- **Ağ (network):** Konteyner kendi ağ ad alanına sahiptir; içerideki **`localhost` o konteynerin kendisidir**, ana makine değil. Varsayılan `bridge` ağında konteynerler birbirini isimle bulamaz. **Kullanıcı tanımlı bridge ağında** ise gömülü DNS, **servis/konteyner adını** IP'ye çözer; Compose her proje için böyle bir ağı otomatik oluşturur. `host` ağı ana makinenin ağ yığınını paylaşır (yalıtımı kaldırır, yalnız Linux'ta çalışır). Dışarıdan erişim için **port yayını** gerekir: `-p 8080:8080` ya da Compose'daki `ports`. Port yayını konteynerler arası trafiği etkilemez.
- **Volume ile bind mount:** Konteynerin yazılabilir katmanı **geçicidir**; kalıcı veri oraya konmaz. **Adlandırılmış volume**, Docker'ın yönettiği ve konteyner ömründen bağımsız olan alandır — üretimde doğru seçimdir. **Bind mount**, ana makinenin belirli bir dizinini konteynere bağlar; geliştirmede pratiktir ama ana makinenin dizin yapısına bağımlı olduğu için taşınabilir değildir. **tmpfs** ise yalnız bellekte durur, diske hiç yazılmaz (geçici gizli dosyalar için uygundur).

```svg
<svg viewBox="0 0 660 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yazılabilir katman, adlandırılmış volume ve bind mount karşılaştırması">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Veri nerede durur?</text>
  <rect x="22" y="48" width="196" height="150" rx="12" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="120" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">Yazılabilir katman</text>
  <text x="36" y="100" font-size="10.5" fill="#5D7789" font-family="system-ui">konteynerin kendi katmanı</text>
  <text x="36" y="120" font-size="10.5" fill="#5D7789" font-family="system-ui">konteyner silinince</text>
  <text x="36" y="137" font-size="10.5" fill="#5D7789" font-family="system-ui">veri de silinir</text>
  <text x="36" y="162" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">kalıcı veri için</text>
  <text x="36" y="179" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">kullanılmaz</text>
  <rect x="232" y="48" width="196" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Adlandırılmış volume</text>
  <text x="246" y="100" font-size="10.5" fill="#5D7789" font-family="system-ui">Docker'ın yönettiği alan</text>
  <text x="246" y="120" font-size="10.5" fill="#5D7789" font-family="system-ui">konteyner ömründen</text>
  <text x="246" y="137" font-size="10.5" fill="#5D7789" font-family="system-ui">bağımsız, kalıcı</text>
  <text x="246" y="162" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">üretimde doğru</text>
  <text x="246" y="179" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">seçim</text>
  <rect x="442" y="48" width="196" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="540" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Bind mount</text>
  <text x="456" y="100" font-size="10.5" fill="#5D7789" font-family="system-ui">ana makinedeki bir dizin</text>
  <text x="456" y="120" font-size="10.5" fill="#5D7789" font-family="system-ui">yol ana makineye</text>
  <text x="456" y="137" font-size="10.5" fill="#5D7789" font-family="system-ui">bağımlıdır</text>
  <text x="456" y="162" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">geliştirmede pratik,</text>
  <text x="456" y="179" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">üretimde taşınmaz</text>
  <text x="330" y="218" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Uygulama durumsuz kalır; kalıcı durum veritabanında ya da volume'da tutulur</text>
</svg>
```

- **Kaynak sınırları:** `--memory`, konteynerin bellek tavanını belirler; bu tavanı çekirdeğin kaynak sınırlama düzeneği (cgroup) uygular. Tavan aşılınca çekirdek süreci öldürür (çıkış kodu `137`). `--cpus` ise CPU **kotası** verir; kota aşımı öldürmez, yalnız **kısıtlar (throttling)**. Modern JVM **konteyner farkındadır** ve bu davranış Java 10'dan beri varsayılan olarak açıktır. JVM cgroup değerlerini okur, `Runtime.availableProcessors()` sonucunu CPU kotasına göre bildirir ve `-XX:MaxRAMPercentage` ile **yığın belleğini (heap)** bellek limitine oranlar. Bu yüzden CPU kotasını çok düşük vermenin bir bedeli vardır: çöp toplayıcı seçimi, anlık derleyicinin (JIT) iş parçacıkları ve paralel havuzların (ör. ortak ForkJoin havuzu) boyutu doğrudan küçülür. Gerekirse `-XX:ActiveProcessorCount` ile bu değer elle geçersiz kılınabilir.
- **`HEALTHCHECK`:** Dockerfile'a yazılan periyodik komut; konteyneri `healthy` / `unhealthy` olarak işaretler. Docker tek başına sağlıksız konteyneri yeniden başlatmaz. Bu bilgiyi orkestratör kullanır: Kubernetes'in sağlık yoklamaları (probe) ya da Compose'daki `depends_on: condition: service_healthy` gibi.
- **Loglama:** 12-factor kuralı gereği uygulama log dosyası yönetmez; **standart çıktıya (stdout/stderr)** yazar. Toplama ve döndürme (rotation) işini **log sürücüsü (log driver)** yapar. Varsayılanı `json-file`'dır; `local`, `journald`, `fluentd` gibi başka sürücüler de vardır. Konteyner içine log dosyası yazmak, dosyaları geçici katmanda bırakır ve diski doldurur.
- **Yapılandırma ve gizli bilgi:** Aynı imaj her ortamda çalışmalıdır. Ortam farkı, **ortam değişkeni** ya da dışarıdan bağlanan bir yapılandırma dosyasıyla verilir. **Gizli bilgi (secret) imaja veya Dockerfile'a gömülmez.** Sebebi şudur: imaj katmanları **eklemelidir**. Bir katmanda yazılan parola, sonraki katmanda silinse bile imajda ve `docker history` çıktısında durur. `ENV` değeri ayrıca `docker inspect` ile, `ARG` değeri de imaj geçmişiyle görülebilir. Doğru yollar sayılıdır: çalışma zamanında ortam değişkeni ya da dosya olarak vermek, Compose'un `secrets` desteği, derleme sırasında BuildKit'in gizli bağlama özelliği (`--mount=type=secret`), kurumsal ortamda gizli bilgi yöneticisi (vault). Bir parola imaja girdiyse imaj yeniden üretilmeli **ve parola değiştirilmelidir**.
- **`docker compose`:** Birden çok konteyneri tek bir bildirim dosyasıyla ayağa kaldırır: `services`, `networks`, `volumes` ve sağlık koşuluna bağlı `depends_on`. Yerel geliştirmede veritabanı, Kafka gibi bağımlılıkları gerçek hâlleriyle çalıştırmak için kullanılır. **Testcontainers** ise aynı fikrin **otomatik test** karşılığıdır. Testler Java kodundan konteyner başlatır, test bitince siler. Böylece test ortamı elle kurulmaz ve her koşuda temiz başlar; Spring Boot tarafında bağlantı ayarları otomatik bağlanabilir. Kabaca şöyle ayrılır: Compose geliştiricinin makinesi içindir, Testcontainers ise boru hattındaki (CI) testler için.
- **Kayıt defteri ve kapalı ortam:** Bankalarda üretim düğümleri genel kayıt defterine (Docker Hub) erişemez. Onaylı imajlar **kurumun iç kayıt defterine** alınır ve oradan dağıtılır; Harbor, Nexus ve Artifactory bunun örnekleridir. Dağıtımda imaj **içerik parmak iziyle (digest) sabitlenir**; digest, imajın içeriğinden hesaplanan ve değişmeyen kimliktir. Boru hattında üç adım tedarik zinciri güvenliğinin standardıdır: **imaj güvenlik taraması** (Trivy, Grype gibi araçlarla katmanlardaki paket açıklarını bulmak), **SBOM** üretimi (yazılım malzeme listesi — imajın içindeki bileşenlerin makine okunur envanteri) ve imaj imzalama.

```svg
<svg viewBox="0 0 660 255" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Compose ağında servislerin birbirini servis adıyla bulması">
  <defs>
    <marker id="dockerrt-net-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">docker compose ağı: servis adı = DNS adı</text>
  <rect x="22" y="44" width="616" height="160" rx="12" fill="none" stroke="#5D7789" stroke-width="1.3" stroke-dasharray="5 4"/>
  <text x="34" y="62" font-size="10.5" fill="#5D7789" font-family="system-ui">compose ağı (kullanıcı tanımlı bridge)</text>
  <rect x="44" y="86" width="150" height="54" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="119" y="110" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">kredi-servis</text>
  <text x="119" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">port 8080</text>
  <rect x="280" y="76" width="150" height="44" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="355" y="95" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">postgres</text>
  <text x="355" y="111" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">port 5432</text>
  <rect x="280" y="140" width="150" height="44" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="355" y="159" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">kafka</text>
  <text x="355" y="175" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">port 9092</text>
  <path d="M198 104 L276 98" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#dockerrt-net-arw)"/>
  <path d="M198 124 L276 158" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#dockerrt-net-arw)"/>
  <text x="442" y="102" font-size="10.5" fill="#25804C" font-family="system-ui">jdbc:postgresql://postgres:5432</text>
  <text x="442" y="166" font-size="10.5" fill="#B07D20" font-family="system-ui">bootstrap-servers: kafka:9092</text>
  <text x="330" y="226" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Konteyner içinde localhost = o konteynerin kendisi, ana makine değil</text>
  <text x="330" y="245" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Ana makineden erişim yalnız port yayınıyla olur: 8080:8080 → localhost:8080</text>
</svg>
```

### Nasıl çalışır?

1. **Başlat.** `docker run` imajdan yeni bir yazılabilir katman ve ağ ad alanı yaratır, `ENTRYPOINT`/`CMD` ile ana süreci başlatır; bu süreç PID 1 olur.
2. **Bağla.** Konteyner, bulunduğu kullanıcı tanımlı ağdaki diğer konteynerleri **adlarıyla** çözer; ana makineye açılması gerekiyorsa port yayını yapılır.
3. **Yapılandır.** Ortam değişkenleri ve bağlanan yapılandırma dosyaları uygulamaya dışarıdan verilir; aynı imaj test ve üretimde farklı davranır ama içerik aynıdır.
4. **Sınırla.** `--memory` ve `--cpus` ile kaynak tavanı konur; JVM bu cgroup değerlerini okuyup heap'ini ve iş parçacığı havuzlarını ona göre boyutlandırır.
5. **Kalıcılaştır.** Kalıcı veri volume'a, geçici veri gerekiyorsa tmpfs'e yazılır; konteynerin kendi dosya sistemi tek kullanımlık kabul edilir.
6. **Gözle.** Uygulama stdout'a log yazar, log sürücüsü toplar; `HEALTHCHECK` konteynerin sağlık durumunu bildirir.
7. **Durdur.** `docker stop` SIGTERM gönderir; uygulama süregelen istekleri bitirip kapanır, süre dolarsa SIGKILL gelir. Konteyner silindiğinde yazılabilir katmanı da silinir, volume'daki veri kalır.

### Mülakatta ne sorulur / tuzaklar

- **"`docker stop` ne yapar?"** Beklenen cevap: önce SIGTERM, varsayılan 10 saniye bekleme, sonra SIGKILL. "Doğrudan öldürür" cevabı yanlıştır. "SIGKILL'i yakalayıp temizlik yaparım" cevabı ise kesin elenme sebebidir — SIGKILL yakalanamaz.
- **Kabuk biçimi tuzağı.** Uygulama sinyali almıyorsa ilk bakılacak yer `ENTRYPOINT`'in biçimidir; exec biçimi JVM'i PID 1 yapar ve sinyali doğrudan ona ulaştırır.
- **"Konteynerden `localhost:5432`'ye bağlanamıyorum."** Konteyner içindeki `localhost` o konteynerdir. Compose ağında adres, **servis adıdır** (`postgres:5432`). Port yayını eklemek konteynerler arası trafiği düzeltmez.
- **Volume ile bind mount farkı.** Volume'u Docker yönetir ve taşınabilirdir; bind mount ana makinenin yoluna bağımlıdır. "Veriyi konteynerde tutarım, silmem yeter" yaklaşımı ölçeklenince ve güncellenince veri kaybettirir.
- **Kaynak limiti etkileri.** Bellek limiti aşımı **öldürür**, CPU kotası aşımı yalnız **kısıtlar**. "JVM ana makinenin çekirdek sayısını görür" ifadesi Java 10 öncesi bilgidir ve günceli değildir.
- **Gizli bilgi.** Parolayı Dockerfile'a yazıp sonra silmek onu imajdan çıkarmaz; katmanlar eklemelidir ve `docker history` ile görülür. Bir kez sızan parola yenilenmelidir.
- **Compose ile Testcontainers karıştırılması.** Compose geliştiricinin makinesindeki bağımlılıkları ayağa kaldırır; Testcontainers testin içinden konteyner başlatıp bitince siler. Otomatik testte doğru araç ikincisidir.
- **Kapalı ortam gerçeği.** "İmajı Docker Hub'dan çekeriz" cevabı banka üretimi için yanlıştır: imajlar iç kayıt defterinden, digest ile sabitlenerek ve taranmış olarak gelir.

### Tek cümlede

Konteyner çalışırken kapanış kalitesini PID 1'e gelen sinyal belirler; servisler birbirine kullanıcı tanımlı ağdaki servis adıyla ulaşır, kalıcılığı volume verir ve JVM kendini cgroup limitlerine göre boyutlandırır. Yapılandırma ile gizli bilgi dışarıdan verilir. Bağımlılıklar yerelde Compose, testte Testcontainers ile ayağa kalkar; imajlar da taranmış ve digest ile sabitlenmiş olarak iç kayıt defterinden dağıtılır.

# dockerrt-1 | Orta
## Soru
Konteynerdeki Spring Boot servisi `docker stop` komutundan sonra süregelen istekleri bitirmeden, yaklaşık 10 saniye gecikmeyle sertçe ölüyor. Dockerfile'da `ENTRYPOINT java -jar app.jar` yazıyor. En olası sebep ve doğru çözüm nedir?
## A
`docker stop` zaten doğrudan `SIGKILL` gönderir; nazik kapanış Docker'da mümkün değildir, yalnız orkestratör kullanılırsa elde edilebilir
🤖 `docker stop` önce `SIGTERM` gönderip varsayılan 10 saniye bekler, `SIGKILL` yalnız süre dolduğunda gelir.
## B
Sorun sinyal değil bellek yetersizliğidir; konteynere `--memory` ile daha yüksek bir tavan verilmesi kapanışı düzeltir
🤖 bellek tavanı aşımı gecikmeli değil ani sonlanma üretir ve belirtiler farklıdır; buradaki sorun sinyalin işlenmemesidir.
## C
Talimat kabuk biçiminde yazıldığı için ana süreç (PID 1) `/bin/sh` olur ve `SIGTERM` uygulamaya iletilmeyebilir; talimat exec biçiminde `ENTRYPOINT ["java","-jar","app.jar"]` yazılmalıdır
🤖 Doğru: exec biçimi JVM'i doğrudan PID 1 yapar ve `SIGTERM` uygulamaya ulaşıp nazik kapanışı tetikler.
## D
Uygulamada `SIGKILL` için bir sinyal işleyici (handler) kaydedilmesi gerekir; Spring Boot bunu kendiliğinden yapmaz
🤖 `SIGKILL` yakalanamaz ve engellenemez; uygulamanın işleyebileceği sinyal `SIGTERM`'dir.
## Doğru
C
## Açıklama
`docker stop` kademeli çalışır: konteynerin **ana sürecine (PID 1)** önce **`SIGTERM`** gönderir, varsayılan olarak **10 saniye** bekler ve süre dolarsa **`SIGKILL`** ile zorla öldürür — yani "doğrudan SIGKILL" iddiası yanlıştır. Buradaki belirti (tam 10 saniye bekleyip sert ölüm) sinyalin uygulamaya **hiç ulaşmadığını** gösterir. Sebep `ENTRYPOINT`'in **kabuk biçiminde (shell form)** yazılmasıdır: komut `/bin/sh -c` içine sarmalanır, PID 1 kabuk olur ve kabuk sinyali çocuk sürece iletmeyebilir. Üstelik PID 1 özel muamele görür — açık bir işleyici kaydetmemiş süreçlere sinyalin varsayılan etkisi uygulanmaz. Çözüm **exec biçimi**dir: `ENTRYPOINT ["java","-jar","app.jar"]` yazıldığında JVM doğrudan PID 1 olur, `SIGTERM`'i alır ve `server.shutdown=graceful` ayarıyla yeni istek almayı kesip süregelen istekleri bitirir. Bellek tavanını artırmak konuyla ilgisizdir; bellek limiti aşımı gecikmeli değil ani bir sonlanma üretir. `SIGKILL` için işleyici yazmak ise mümkün değildir: bu sinyal tanım gereği yakalanamaz, engellenemez ve süreci anında sonlandırır.

# dockerrt-2 | Temel
## Soru
Yerel geliştirmede konteynerde çalışan PostgreSQL'in verisi, konteyner silinip yeniden yaratıldığında kayboluyor. Verinin nerede durduğu ve doğru çözüm nedir?
## A
Veriyi konteynerin kendi dosya sisteminde tutmak yeterlidir; sorun yalnızca konteyneri silmektir, bu yüzden konteyner hiç silinmemelidir
🤖 konteyner tek kullanımlık kabul edilir; kalıcı durumu geçici katmanda tutmak sürüm yükseltmede ve taşımada veri kaybettirir.
## B
Volume ile bind mount aslında aynı şeydir; ikisi de ana makinedeki bir dizini gösterir ve aralarındaki tek fark yazım biçimidir
🤖 adlandırılmış volume'u Docker kendi yönettiği alanda tutar, bind mount ise ana makinenin belirli bir yolunu bağlar.
## C
Veri konteynerin geçici yazılabilir katmanında durduğu için konteynerle birlikte silinir; kalıcılık için adlandırılmış volume, geliştirmede istenirse bind mount kullanılmalıdır
🤖 Doğru: yazılabilir katman konteyner ömrüne bağlıdır, kalıcı veri konteyner dışındaki bir volume'da tutulur.
## D
Kalıcılık için konteyner `docker commit` ile imaja dönüştürülmelidir; böylece veri imaja işlenir ve sonraki konteynerlerde korunur
🤖 veriyi imaja işlemek imajı şişirir, sürümlemeyi bozar ve her yeni imaj sürümünde veriyi yeniden kaybettirir.
## Doğru
C
## Açıklama
Konteyner, imajın **salt-okunur** katmanlarının üstüne eklenen ince bir **yazılabilir katmanla** çalışır. Uygulamanın konteyner içine yazdığı her şey — veritabanı dosyaları dahil — bu katmanda durur ve **konteyner silindiğinde onunla birlikte gider**. Doğru çözüm veriyi konteyner dışına almaktır: **adlandırılmış volume**, Docker'ın yönettiği, konteyner ömründen bağımsız bir alandır ve üretim için doğru seçimdir; **bind mount** ise ana makinenin belirli bir dizinini bağlar, geliştirmede pratiktir ama ana makinenin yol yapısına bağımlı olduğu için taşınabilir değildir — bu ikisini aynı saymak yanlıştır. "Konteyneri hiç silmemek" bir çözüm değil, sorunun ertelenmesidir: imaj sürümü yükseltildiğinde ya da konteyner başka makineye taşındığında veri yine kaybolur; konteynerler tanımı gereği tek kullanımlıktır. `docker commit` ile veriyi imaja işlemek ise en kötüsüdür: imaj şişer, sürümleme anlamını yitirir ve bir sonraki imaj yapımında veri yeniden kaybolur. Kural nettir: **imaj değişmez, konteyner geçici, veri volume'da (ya da yönetilen bir veritabanında) durur.**

# dockerrt-3 | Orta
## Soru
`docker compose` ile ayağa kaldırdığın kredi servisi, aynı compose dosyasındaki `postgres` servisine `jdbc:postgresql://localhost:5432/krediler` adresiyle bağlanmaya çalışıyor ve bağlantı reddediliyor. Neden ve doğru adres nedir?
## A
Konteyner içindeki `localhost` o konteynerin kendisidir; Compose'un oluşturduğu kullanıcı tanımlı ağda servisler birbirini servis adıyla bulur, adres `jdbc:postgresql://postgres:5432/krediler` olmalıdır
🤖 Doğru: kullanıcı tanımlı ağdaki gömülü DNS servis adını çözer, `localhost` ise çağıran konteyneri gösterir.
## B
Sorun port yayınının eksikliğidir; veritabanı servisine `5432:5432` yayını eklenirse `localhost` üzerinden bağlantı çalışır
🤖 port yayını konteyneri ana makineye açar, konteynerler arası trafiği değiştirmez ve `localhost` yine çağıran konteyneri gösterir.
## C
Konteynerler yalnız IP adresiyle haberleşebilir; veritabanı konteynerinin IP'si her açılışta okunup uygulama yapılandırmasına yazılmalıdır
🤖 kullanıcı tanımlı ağda gömülü DNS servis adını çözer; IP'yi elle sabitlemek hem kırılgan hem gereksizdir.
## D
Uygulama `network_mode: host` ile çalıştırılmalıdır; konteynerlerin haberleşmesinin tek yolu ana makinenin ağını paylaşmalarıdır
🤖 host ağı yalnız Linux'ta çalışır ve ağ yalıtımını kaldırır; standart yol ortak bir bridge ağı üzerinden servis adıyla erişimdir.
## Doğru
A
## Açıklama
Her konteynerin **kendi ağ ad alanı** vardır; bu yüzden konteyner içinde yazılan **`localhost` ana makineyi değil, o konteynerin kendisini** gösterir. Uygulama konteyneri kendi 5432 portunda hiçbir şey dinlemediği için bağlantı reddedilir. Compose her proje için **kullanıcı tanımlı bir bridge ağı** oluşturur ve bu ağda gömülü DNS, **servis adını** konteynerin güncel IP'sine çözer; doğru adres `postgres:5432` biçimindedir. **Port yayını (`ports`)** farklı bir amaca hizmet eder: konteyneri **ana makineye** açar, konteynerler arası trafiği hiç etkilemez — dolayısıyla `5432:5432` eklemek bu hatayı gidermez. Konteynerlerin yalnız IP ile haberleşebildiği inancı eskidir ve kırılgan bir çözüm önerir: IP'ler konteyner her yeniden yaratıldığında değişir, isim çözümü tam da bu yüzden vardır. `network_mode: host` ise ağ yalıtımını tamamen kaldırır, yalnız Linux'ta çalışır ve taşınabilirliği bozar; olağan çözüm değil, özel durumlarda başvurulan bir seçenektir. Not: geliştirme makinesinden aynı veritabanına erişmek istiyorsan port yayını gerekir — bu ikisi birbirini dışlamaz, farklı ihtiyaçlardır.

# dockerrt-4 | İleri
## Soru
Gün sonu (EOD) tahakkuk işini çalıştıran konteynere `--cpus=0.5` verildi ve uygulamanın çöp toplama ile paralel işleme davranışı beklenmedik biçimde değişti. Modern JVM'in konteyner kaynak sınırlarıyla ilişkisi hakkında hangisi doğrudur?
## A
CPU kotası aşıldığında konteyner çekirdek tarafından öldürülür; davranış değişikliğinin sebebi budur ve çözüm kotayı tamamen kaldırmaktır
🤖 CPU kotası aşımı öldürmez, yalnız kısıtlar (throttling); öldürülme bellek tavanı aşıldığında olur.
## B
JVM her zaman ana makinenin fiziksel çekirdek sayısını görür; `--cpus` yalnız çekirdek zamanlayıcısını ilgilendirir ve uygulama davranışını değiştirmez
🤖 Java 10'dan beri konteyner desteği varsayılan olarak açıktır ve JVM cgroup kotasını okuyup buna göre boyutlanır.
## C
Bu davranış ancak `-XX:ActiveProcessorCount` elle verildiğinde ortaya çıkar; varsayılan olarak JVM cgroup değerlerini hiç okumaz
🤖 söz konusu bayrak varsayılanı geçersiz kılmak içindir; cgroup okuma varsayılan olarak zaten yapılır.
## D
JVM konteyner farkındadır; `--cpus` kotasını okuyup `availableProcessors` değerini ona göre bildirir, bu da çöp toplayıcı seçimini, JIT iş parçacıklarını ve paralel havuz boyutlarını küçültür
🤖 Doğru: cgroup kotası JVM'in kendini boyutlandırmasını doğrudan etkiler; çok düşük kota neredeyse tek iş parçacıklı davranış üretir.
## Doğru
D
## Açıklama
Modern JVM (Java 10 ve sonrası, varsayılan olarak açık **konteyner desteği**) çalıştığı cgroup'un kaynak değerlerini okur. Bellek tarafında bu `-XX:MaxRAMPercentage` ile heap'in limite oranlanması demektir; **CPU tarafında** ise `--cpus` ile verilen kota, `Runtime.availableProcessors()` sonucunu belirler. Bu sayı JVM'in birçok kararını besler: hangi çöp toplayıcının seçileceği, JIT derleyici iş parçacıklarının sayısı, ortak ForkJoin havuzunun ve paralel akışların (parallel stream) genişliği. `--cpus=0.5` gibi çok düşük bir kotada JVM kendini neredeyse tek iş parçacıklı bir makinede sanır; toplu iş beklenenden kat kat yavaş çalışır. "JVM ana makinenin çekirdeklerini görür" ifadesi **Java 10 öncesine ait eskimiş bir bilgidir** ve mülakatta doğrudan elenme sebebidir. `-XX:ActiveProcessorCount` bayrağı bu okumayı **geçersiz kılmak** içindir, okumayı başlatmak için değil. CPU kotasının aşımı ise süreci **öldürmez**, yalnız **kısıtlar (throttling)**: öldürülme (çıkış kodu `137`) bellek tavanı aşıldığında olur. Doğru yaklaşım kotayı kaldırmak değil, işin ihtiyacına göre ölçüp yeterli CPU vermektir; limitsiz konteyner düğümdeki komşularını etkiler.

# dockerrt-5 | İleri
## Soru
Bir geliştirici veritabanı parolasını Dockerfile'a `ENV DB_PASSWORD=...` biçiminde yazmış, sonra fark edip bir sonraki talimatta bu değişkeni temizleyen bir `RUN` eklemiş ve imajı kayıt defterine basmış. Güvenlik açısından durum nedir?
## A
Sorun çözülmüştür; son katmanda değişken boş olduğu için imajın çalışan hâlinde parola görünmez ve risk ortadan kalkar
🤖 imaj katmanları eklemelidir; önceki katmanın içeriği ve talimat metni imajda ve imaj geçmişinde durmaya devam eder.
## B
`ENV` yerine `ARG` kullanılsaydı sorun tamamen çözülürdü; derleme argümanları imajda hiçbir iz bırakmaz
🤖 `ARG` çalışan konteynerde görünmez ama değeri imaj geçmişinde (`docker history`) okunabilir; gizli bilgi için uygun değildir.
## C
Parolayı yalnız imajı doğrudan çalıştıranlar görebilir; kayıt defterine gönderilen imaj bu bilgiyi taşımaz
🤖 katmanlar imajın parçasıdır ve kayıt defterine olduğu gibi gider; imajı çekebilen herkes okuyabilir.
## D
Gizli bilgi imajda kalmıştır; imaj temiz biçimde yeniden üretilmeli, parola değiştirilmeli ve gizli bilgi çalışma zamanında verilmelidir
🤖 Doğru: sızmış gizli bilgi imajdan silinemez sayılır; hem imaj yeniden üretilir hem parola yenilenir.
## Doğru
D
## Açıklama
İmaj **katmanlardan** oluşur ve katmanlar **eklemelidir**: bir katmanda yazılan bir değer, sonraki katmanda silinse bile **önceki katmanda durmaya devam eder**. Üstelik Dockerfile talimatlarının metni imaj geçmişine işlenir; `docker history` ile hem `ENV` hem `ARG` değerleri okunabilir. Dolayısıyla "sonradan temizledim" savunması geçersizdir, `ARG` kullanmak da gizli bilgiyi güvenli yapmaz — `ARG` yalnız değerin çalışan konteynerde görünmemesini sağlar, imaj geçmişinden gizlemez. İmaj kayıt defterine basıldığı anda bu katmanlar olduğu gibi oraya gider; imajı çekme yetkisi olan herkes parolayı okuyabilir, bu yüzden "yalnız çalıştıranlar görür" iddiası da yanlıştır. Doğru müdahale iki adımlıdır: **imajı gizli bilgi içermeyecek şekilde yeniden üretmek** ve **parolayı değiştirmek** — bir kez basılmış gizli bilgi sızmış kabul edilir. Kalıcı çözüm ise gizli bilgiyi hiç imaja koymamaktır: değer **çalışma zamanında** ortam değişkeni ya da bağlanan dosya olarak verilir (Compose'un `secrets` desteği, orkestratörün gizli bilgi nesnesi), derleme sırasında gerçekten gerekiyorsa BuildKit'in gizli bağlama özelliği (`--mount=type=secret`) kullanılır; kurumsal ortamda kaynak bir gizli bilgi yöneticisidir.

# dockerrt-6 | İleri
## Soru
Dış ağa kapalı (air-gapped) bir banka ortamında konteyner imajlarının tedariki ve güvenliği nasıl yönetilmelidir?
## A
Üretim düğümleri doğrudan genel kayıt defterinden `latest` etiketiyle imaj çeker; en güncel sürüm her zaman en güvenli sürüm olduğu için ek denetime gerek kalmaz
🤖 kapalı ortamda dış kayıt defterine erişim yoktur; ayrıca `latest` sürümü sabitlemez ve "en yeni" anlamına gelmez.
## B
Onaylı imajlar kurumun iç kayıt defterine alınıp oradan digest ile sabitlenerek dağıtılır; boru hattında güvenlik taraması ve yazılım malzeme listesi (SBOM) üretimi yapılır
🤖 Doğru: iç kayıt defteri kapalı ortamın tek kaynağıdır, digest belirleyicilik sağlar, tarama ve SBOM tedarik zincirini denetlenebilir kılar.
## C
Güvenlik taraması yalnız çalışan konteynerlerde anlamlıdır; imaj katmanlarını taramak gereksizdir çünkü açıklar ancak çalışma zamanında ortaya çıkar
🤖 açıkların çoğu imajdaki paket sürümlerinden gelir ve derleme aşamasında taranarak üretime çıkmadan yakalanabilir.
## D
İmajlar geliştiricilerin makinelerinde dosyaya kaydedilip elden üretime taşınır; bu yöntem kapalı ortamda izlenebilirliği en iyi sağlayan yaklaşımdır
🤖 kaynağı ve içeriği denetlenmemiş elle taşıma izlenebilirliği yok eder ve onay sürecini atlatır.
## Doğru
B
## Açıklama
Kapalı (air-gapped) ortamda üretim düğümlerinin genel kayıt defterine (Docker Hub gibi) erişimi yoktur; bu yüzden "doğrudan çekeriz" cevabı daha en baştan geçersizdir. Üstelik **`latest` etiketi sürümü sabitlemez** — taşınabilir bir isimdir ve "en yeni" anlamına gelmez. Kurumsal desen şudur: dışarıdan gelen taban ve uygulama imajları **onay sürecinden geçirilip kurumun iç kayıt defterine** (Harbor, Nexus, Artifactory gibi) alınır ve üretim yalnız oradan besleyicilenir; dağıtım bildirimlerinde imaj **digest (`sha256:...`) ile sabitlenir**, böylece hangi dağıtımda tam olarak hangi ikili dosyanın çalıştığı sonradan kesin olarak gösterilebilir — denetim (audit) için kritik bir gerekliliktir. Güvenliğin ikinci ayağı boru hattındadır: **imaj güvenlik taraması** (Trivy, Grype gibi araçlar) katmanlardaki paket sürümlerini bilinen açık veritabanlarıyla karşılaştırır ve sorunlu imajın üretime çıkmasını engeller; taramayı yalnız çalışma zamanına bırakmak açığı çok geç yakalamak demektir. Üçüncü ayak **SBOM**'dur: imajın içindeki bileşenlerin makine okunur envanteri, yeni bir açık duyurulduğunda "bu bileşen bizde hangi imajlarda var?" sorusunu dakikalar içinde cevaplatır. Elden dosya taşımak ise onay, izlenebilirlik ve tekrarlanabilirliğin üçünü birden ortadan kaldırır.
