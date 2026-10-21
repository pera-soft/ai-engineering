---
slug: config-yonetimi-dagitik
baslik: "Dağıtık Config: Config Server, ConfigMap/Secret ve Vault"
kademe: Orta
sira: 102
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir çamaşır makinesinin sıcaklığını ve devrini düğmeden seçersin; makineyi söküp içindeki devreyi değiştirmen gerekmez. Yazılımın da dışarıdan çevrilen düğmeleri vardır: veritabanının adresi, bir isteğin kaç saniye bekleyeceği, bir özelliğin açık mı kapalı mı olduğu. Bu düğmelerin tümüne yapılandırma denir. Onlarca küçük servisin ve birden çok ortamın olduğu bir sistemde bu değerleri kodun içine yazmak yönetilemez hale gelir. Tek bir veritabanı adresi değişse bile bütün servisleri baştan derleyip yeniden dağıtman gerekir. Bu yüzden yapılandırma koddan ayrılır ve uygulamaya dışarıdan verilir. Buna **dışsallaştırılmış yapılandırma (externalized configuration)** denir.

### Temel kavramlar

- **Yapılandırma (configuration, kısaca config):** Kodun kendisi değil, kodun davranışını belirleyen değerler; adres, süre, boyut, açık/kapalı bayrağı.
- **Ortam (environment):** Uygulamanın çalıştığı ayrı dünyalar: geliştirme (dev), test, canlı (production, kısaca prod). Kod aynıdır, değerler farklıdır.
- **Ortam değişkeni (environment variable):** İşletim sisteminin sürece açılışta verdiği ad=değer çifti, örneğin `SPRING_DATASOURCE_URL=...`.
- **Profil (profile):** Bir ortama ait ayar kümesine verilen isim. Spring'de `application-prod.yml` dosyası, `prod` profili etkinken devreye girer.
- **İmaj (image):** Uygulamanın kurulu haldeki dondurulmuş paketi. Aynı imaj her ortamda çalışır; farkı yalnızca dışarıdan verilen yapılandırma yaratır.
- **Dağıtım (deployment):** Bu imajın bir ortamda çalıştırılması işlemi.
- **Yeniden başlatma (restart):** Sürecin kapatılıp yeniden açılması. Bazı ayarlar yalnızca bu anda okunur.
- **Gizli bilgi (secret):** Şifre, jeton (token), API anahtarı gibi ifşa olmaması gereken değerler; sıradan ayarlardan farklı muamele görür.
- **Merkezî yapılandırma sunucusu (config server):** Tüm servislerin ayarlarını tek yerden dağıtan bileşen; Spring Cloud Config, Consul, etcd gibi.

```svg
<svg viewBox="0 0 660 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Config kaynakları uygulamaya nasıl akar">
  <defs>
    <marker id="dcfg-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Config uygulamaya nereden akar</text>
  <rect x="30" y="45" width="170" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="115" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Config Server (git)</text>
  <text x="115" y="83" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">versiyonlu, denetim izi</text>
  <rect x="30" y="100" width="170" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="115" y="123" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">ConfigMap</text>
  <text x="115" y="138" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">düz konfig, K8s-yerli</text>
  <rect x="30" y="155" width="170" height="46" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="115" y="176" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">Secret / Vault</text>
  <text x="115" y="191" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">gizli; base64 ≠ şifreleme</text>
  <rect x="400" y="95" width="230" height="60" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="515" y="120" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Uygulama (pod)</text>
  <text x="515" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">env değişkeni veya mounted dosya</text>
  <path d="M200 68 L398 112" fill="none" stroke="#186399" stroke-width="1.4" marker-end="url(#dcfg-arw)"/>
  <path d="M200 123 L398 123" fill="none" stroke="#186399" stroke-width="1.4" marker-end="url(#dcfg-arw)"/>
  <path d="M200 178 L398 138" fill="none" stroke="#186399" stroke-width="1.4" marker-end="url(#dcfg-arw)"/>
</svg>
```

### Nasıl çalışır?

**Açılışta okuma.** Uygulama başlarken tüm ayarları tek bir birleşik tabloya toplar. Bu tabloyu besleyen kaynaklar şunlardır:

- komut satırında verilen argümanlar,
- işletim sisteminin verdiği ortam değişkenleri,
- yapılandırma sunucusundan (config server) çekilen ya da diskte duran profil dosyaları,
- imajın içindeki varsayılan `application.yml`.

Kubernetes'te `ConfigMap` ve `Secret` de bu kaynaklara katılır. Değerler ya ortam değişkeni olarak ya da **bağlanmış dosya (mounted volume)** olarak sürece verilir.

**Öncelik sırası (precedence).** Aynı anahtar birden fazla kaynakta tanımlıysa değerler birleştirilmez. En yüksek öncelikli kaynak diğerinin değerini **ezer (override)**. Sıra yüksekten düşüğe şöyledir: komut satırı argümanı > ortam değişkeni > yapılandırma sunucusu veya profil dosyası > uygulama içi varsayılan.

Çakışma bir hata değildir. Hangi değerin kazanacağı önceden bellidir; uygulama açılışa devam eder. Bu yüzden canlıda kritik bir adresi ortam değişkeniyle vermek, aynı anahtarın dosyalardaki tüm karşılıklarını geçersiz kılar.

**Çalışırken değişiklik.** Normalde **Spring'in yönettiği nesne (bean)** açılışta bir kez kurulur. `@Value` ile doldurulan alan sonradan kendiliğinden değişmez, çünkü Spring yapılandırma dosyalarını çalışırken izlemez.

Bean `@RefreshScope` ile işaretlenirse durum değişir. `/actuator/refresh` ucu çağrıldığında o bean atılır, yeniden yaratılır ve güncel değerlerle kurulur. Yeniden başlatma gerekmez. Yüzlerce örneği tek tek dolaşmamak için Spring Cloud Bus kullanılır; tek bir olay mesaj kuyruğu üzerinden bütün örneklere yayılır.

Ama her ayar böyle tazelenmez. Açılışta kurulan bağlantı havuzu boyutu ya da dinlenen port ancak yeniden başlatmayla oturur.

```svg
<svg viewBox="0 0 660 226" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Spring property önceliği, yüksekten düşüğe">
  <defs>
    <marker id="dcfg-pre-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Property önceliği: en üstteki ezer (override)</text>
  <line x1="40" y1="200" x2="40" y2="48" stroke="#25804C" stroke-width="1.6" marker-end="url(#dcfg-pre-arw)"/>
  <text x="34" y="128" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui" transform="rotate(-90 34 128)">öncelik artar / kazanır</text>
  <rect x="70" y="44" width="500" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="86" y="66" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">komut-satırı argümanları  (en yüksek)</text>
  <rect x="70" y="86" width="500" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="86" y="108" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">OS ortam değişkeni  (SPRING_DATASOURCE_URL)</text>
  <rect x="70" y="128" width="500" height="34" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="86" y="150" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">profil dosyası  (application-prod.yml)</text>
  <rect x="70" y="170" width="500" height="34" rx="8" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="86" y="192" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">varsayılan application.yml  (en düşük)</text>
  <text x="592" y="128" font-size="10.5" fill="#8A2E2E" font-family="system-ui" transform="rotate(90 592 128)">birleştirme yok, tek değer kazanır</text>
</svg>
```

```svg
<svg viewBox="0 0 660 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="RefreshScope ile yeniden başlatmadan config güncelleme akışı">
  <defs>
    <marker id="dcfg-rf-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">@RefreshScope: restart'sız canlı güncelleme</text>
  <rect x="18" y="54" width="150" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="93" y="78" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">config git'te</text>
  <text x="93" y="96" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">değişti</text>
  <rect x="192" y="54" width="150" height="56" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="267" y="78" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">/actuator/refresh</text>
  <text x="267" y="96" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">veya Cloud Bus</text>
  <rect x="366" y="54" width="150" height="56" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="441" y="78" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">@RefreshScope bean</text>
  <text x="441" y="96" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">atılır+yeniden yaratılır</text>
  <rect x="540" y="54" width="104" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="592" y="78" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">yeni değer</text>
  <text x="592" y="96" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">restart YOK</text>
  <path d="M168 82 L190 82" fill="none" stroke="#186399" stroke-width="1.4" marker-end="url(#dcfg-rf-arw)"/>
  <path d="M342 82 L364 82" fill="none" stroke="#186399" stroke-width="1.4" marker-end="url(#dcfg-rf-arw)"/>
  <path d="M516 82 L538 82" fill="none" stroke="#186399" stroke-width="1.4" marker-end="url(#dcfg-rf-arw)"/>
  <text x="330" y="146" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">Dikkat: bağlantı havuzu boyutu gibi bazı ayarlar yalnız restart'ta oturur</text>
</svg>
```

**Gizli bilgi ayrı durur.** Şifre, jeton veya anahtar koda, `application.yml`'e ve git deposuna asla düz metin yazılmaz. Git geçmişi kalıcıdır; bir kez giren sır sızmış sayılır ve değiştirilmesi gerekir. Doğru desen şudur: dosyada yalnızca `${DB_SIFRE}` gibi bir **yer tutucu (placeholder)** bulunur. Gerçek değer çalışma anında dışarıdan verilir. Kaynağı Kubernetes `Secret` ya da HashiCorp Vault gibi bir kasadır. Vault ayrıca kısa ömürlü kimlik bilgisi üretip değiştirmeyi (rotasyon) otomatikleştirebilir.

Burada sık düşülen bir tuzak var. `Secret` varsayılan olarak base64 kodlamasıyla saklanır. Bu şifreleme değildir; tek komutla geri çevrilebilen bir yazım biçimidir. Gerçek koruma için ya etcd şifrelemesi (encryption at rest) açılır ya da kasa kullanılır. `ConfigMap` sıradan ayarlar içindir; sır oraya konmaz.

Değerin pod'a nasıl bağlandığı da sonucu değiştirir. Ortam değişkeni olarak verilmişse değer pod'un ömrü boyunca dondurulur. Dosya olarak bağlanmışsa içerik bir süre sonra güncellenir, fakat uygulamanın o dosyayı yeniden okuması gerekir. Kubernetes bu değişiklikte pod'ları kendiliğinden yeniden başlatmaz; yeniden dağıtım ya elle ya da Reloader benzeri bir araçla tetiklenir.

### Mülakatta ne sorulur?

- **Öncelik sırasını sırala.** Komut satırı argümanı > ortam değişkeni > config server / profil dosyası > varsayılan. Birleştirme yoktur; tek değer kazanır.
- **`@RefreshScope` neyi yeniler, neyi yenilemez?** Bu kapsamdaki (scope) bean'ler yeniden yaratılır ve yeni değerleri alır. Havuz boyutu, port gibi açılışta bağlanan kaynaklar yenilenmez.
- **Sır config server'ın git deposunda düz metin durabilir mi?** Hayır. Ya şifrelenmiş değer saklanır ya da değer harici bir kasadan alınır; `Secret`'ın base64'ü koruma sayılmaz.
- **Config sunucusu düşerse ne olur?** Ayakta olan servisler değerleri bellekte tuttuğu için etkilenmez; asıl risk yeni açılan veya yeniden başlayan örneğin ayar çekememesidir. Çare: yerel önbellek, yedekli config sunucusu ve son bilinen iyi değere düşme.
- **Geri alma (rollback) nasıl olur?** Ayarlar versiyonlandığı için hatalı değişiklik commit geri alınıp refresh tetiklenerek düzelir; imajı geri almak gerekmez. Bu, yapılandırmanın imajdan ayrılmasının en somut kazancıdır.

### Tek cümlede

Yapılandırma imajdan ayrılır, net bir öncelik sırasıyla çözülür, gizli bilgi ayrı bir kasada tutulur ve `@RefreshScope` yalnızca bazı ayarları yeniden başlatmadan tazeler.

# dcfg-1 | Orta
## Soru
Bir ekip "config'i git'te versiyonlamak, denetim izi (audit trail) tutmak ve tüm servisler için tek merkezden yönetmek istiyoruz" diyor. Hangi yaklaşım bu ihtiyaca doğrudan cevap verir?
## A
Her servisin `application.yml`'ini kendi imajına (image) gömüp değişiklik gerektiğinde imajı yeniden derleyip dağıtmak
🤖 İmaja gömülü config ortam-bağımsız imajı bozar, merkezî yönetim vermez ve her ayar değişikliğini yeni bir sürüm çıkmaya bağlar.
## B
Merkezî bir Config Server (ör. Spring Cloud Config) kullanmak; tüm servislerin ayarları bir git deposunda tutulur
🤖 Doğru: git versiyonlama, denetim izi ve tek merkez tam olarak Config Server'ın sunduğu değerdir.
## C
Değerleri her servisin kendi veritabanına yazıp açılışta oradan okumak, değişiklikleri elle SQL çalıştırarak yapmak
🤖 Veritabanındaki satır git geçmişi ve commit denetimi taşımaz; ayrıca her servise ayrı bir yönetim yüzeyi ekler.
## D
Tüm ayarları kaynak kodda sabit (constant) tutup her ortam için ayrı bir derleme (build) üretmek
🤖 Kodda sabitleme dışsallaştırmayı (externalization) ortadan kaldırır ve tek imajın tüm ortamlarda çalışması ilkesini bozar.
## Doğru
B
## Açıklama
İhtiyaç **git-versiyonlama + denetim izi + merkezî yönetim** olarak tanımlanmış; bu tam olarak **Config Server** (Spring Cloud Config gibi) desenidir. Konfigürasyon bir git deposunda tutulur (her değişiklik commit'le izlenir), servisler açılışta Config Server'dan kendi `{uygulama}-{profil}` dosyalarını çeker. İmaja gömmek (A), koda sabitlemek (D) config'i dışsallaştırma (externalized config) ilkesini bozar; veritabanına yazmak (C) istenen git-tabanlı denetim modelini vermez. Alternatif olarak Kubernetes `ConfigMap` da merkezîdir ama git-versiyonlama ve denetim izini kendiliğinden getirmez (GitOps ile ayrıca kurulur).

# dcfg-2 | Orta
## Soru
Spring uygulamasında bir property değeri değişti ve servisi **yeniden başlatmadan** yeni değeri almasını istiyorsun. Hangi mekanizma bunu sağlar?
## A
Değeri sade `@Value` ile enjekte etmek yeterlidir; Spring bu alanları kaynak değişince kendiliğinden canlı günceller
🤖 sade `@Value` alanı bean kurulurken bir kez doldurulur ve sonradan izlenmez; değer bean ömrü boyunca sabit kalır.
## B
Değeri sınıf içinde `static final` bir sabit olarak tanımlayıp doğrudan oradan okumak
🤖 `static final` derleme zamanında sabitlenir; çalışırken hiçbir mekanizma onu değiştiremez, sıcak güncelleme imkânsızdır.
## C
Diskteki `application.yml` dosyasını elle değiştirmek; Spring Boot dosyayı izleyip değişikliği çalışan bean'lere yansıtır
🤖 Spring Boot çalışırken yapılandırma dosyalarını izlemez; devtools yalnız geliştirmede ve tam yeniden başlatmayla çalışır.
## D
İlgili bean'i `@RefreshScope` ile işaretleyip değişiklikten sonra `/actuator/refresh` ucunu tetiklemek
🤖 Doğru: bu scope'taki bean refresh olayında atılıp yeniden yaratılır ve güncel değerlerle kurulur.
## Doğru
D
## Açıklama
Spring bean'leri varsayılan olarak **açılışta tek kez** enjekte edilir; `@Value` alanları da (A) bu anda oturur ve sonradan kendiliğinden değişmez. **`@RefreshScope`** özel bir scope'tur: bu scope'taki bean, `/actuator/refresh` uç noktası veya `Spring Cloud Bus` (tüm örneklere yayınlayan) tetiklendiğinde **atılıp yeniden yaratılır**, böylece güncel property değerlerini alır. Bu, servisi yeniden başlatmadan bazı ayarların (özellik bayrakları, eşikler, URL'ler) canlı değişmesini sağlar. Uyarı: bağlantı havuzu boyutu gibi başlangıçta oturan bazı kaynaklar refresh ile değil, ancak yeniden başlatmayla güncellenir.

# dcfg-3 | Orta
## Soru
Bir veritabanı şifresini Kubernetes'te yönetiyorsun. Aşağıdakilerden hangisi doğrudur?
## A
Kubernetes `Secret` nesnesi varsayılan olarak base64 kodlamayla saklanır; bu şifreleme (encryption) değildir
🤖 Doğru: base64 tek komutla geri çevrilir; "Secret" adı içeriğin şifreli saklandığı anlamına gelmez.
## B
`Secret`'a yazılan veri kümede güçlü şifreleme ile korunduğu için ayrıca bir önlem almaya gerek yoktur
🤖 varsayılan kurulumda değer etcd'de kodlanmış ama okunabilir durur; şifreleme ayrıca etkinleştirilmelidir.
## C
Şifreyi düz metin olarak `ConfigMap`'e koymak `Secret` kullanmaktan daha güvenlidir, çünkü `ConfigMap` daha basittir
🤖 `ConfigMap` gizli veri için tasarlanmamıştır; erişim ayrımı ve maskeleme sağlamaz, sır orada daha açıktadır.
## D
En güvenlisi şifreyi imajın (image) içine gömüp değeri git deposunda versiyonlayarak saklamaktır
🤖 Yanlış ve tehlikeli: imaja ve git geçmişine giren sır kalıcıdır, silinse bile geçmişten okunabilir ve rotasyon gerektirir.
## Doğru
A
## Açıklama
Kubernetes `Secret` nesnesi verimi **base64** ile saklar — bu bir **kodlamadır, şifreleme değildir** ve `echo ... | base64 -d` ile anında çözülür. Yani `Secret`, `ConfigMap`'e göre RBAC/erişim ayrımı ve mounting kolaylığı getirir ama içeriği tek başına şifrelemez; etcd düzeyinde şifreleme (encryption at rest) açıkça etkinleştirilmeli ya da **HashiCorp Vault** gibi bir kasa kullanılmalıdır. Vault ayrıca **dinamik/kısa ömürlü** kimlik bilgileri üretir ve rotasyonu otomatikleştirir. Sırrı `ConfigMap`'e (C) veya imaja/git'e (D) koymak temel güvenlik ihlalidir — bu proje kuralında da açıkça yasaktır.

# dcfg-4 | İleri
## Soru
Spring Boot'ta hem `application.yml` hem `application-prod.yml` var, ayrıca `SPRING_DATASOURCE_URL` ortam değişkeni tanımlı. `prod` profili aktifken aynı `spring.datasource.url` anahtarı için hangi değer kazanır?
## A
Her zaman varsayılan `application.yml` dosyasındaki değer kazanır; profil ve ortam onu ezemez
🤖 varsayılan `application.yml` sıranın en altındadır; hem profil dosyası hem ortam değişkeni onu ezer.
## B
İki kaynaktaki değerler birleştirilip virgülle art arda eklenir (concatenation) ve liste olarak okunur
🤖 aynı anahtar için birleştirme yapılmaz; en yüksek öncelikli kaynağın tek değeri kazanır (override).
## C
`SPRING_DATASOURCE_URL` ortam değişkeninin değeri kazanır ve iki dosyadaki tanımların ikisini de ezer
🤖 Doğru: öncelik sırasında ortam değişkeni profil ve varsayılan dosyaların üstündedir.
## D
Aynı anahtar birden çok kaynakta tanımlı olduğu için uygulama açılışta çakışma hatası verip durur
🤖 çakışma bir hata durumu değildir; Spring tanımlı öncelik sırasını uygulayıp tek değeri seçer ve açılışa devam eder.
## Doğru
C
## Açıklama
Spring Boot property'leri **tek bir birleşik ortamdan** (Environment) belirli bir **öncelik sırasıyla** (precedence) çözer; çakışmada en yüksek öncelikli kaynağın değeri **ezer** (override eder), birleştirme (B) olmaz. Sıra (yüksekten düşüğe, ilgili kısım): komut-satırı argümanları > OS ortam değişkenleri (`SPRING_DATASOURCE_URL`) > profil dosyası (`application-prod.yml`) > varsayılan `application.yml`. Dolayısıyla ortam değişkeni hem profil dosyasını hem varsayılanı ezer (C). Bu, "aynı imaj, ortama göre değişen enjekte config" ilkesinin (Twelve-Factor) temelidir: prod'da hassas URL'yi dosyaya değil ortam değişkenine koyarsın.

# dcfg-5 | Orta
## Soru
Bir `ConfigMap`'i güncelledin. Pod'daki uygulama bu değişikliği nasıl görür?
## A
Enjeksiyon biçimine bağlıdır: ortam değişkeninde yeniden başlatma gerekir, bağlanmış dosyada içerik tazelenir
🤖 Doğru: env enjeksiyonu pod açılışında dondurulur; volume olarak bağlanan dosya tazelenir ama uygulamanın yeniden okuması gerekir.
## B
Her iki enjeksiyon biçiminde de değişiklik anında ve otomatik olarak çalışan uygulamaya yansır, ek adım gerekmez
🤖 ortam değişkeni olarak verilen değer pod ömrü boyunca sabittir; dosya yolunda da uygulamanın yeniden okuması gerekir.
## C
Kubernetes değişikliği algılayıp o `ConfigMap`'i kullanan tüm pod'ları kendiliğinden yeniden başlatır (rolling restart)
🤖 Kubernetes böyle bir otomatik yeniden başlatma yapmaz; rollout elle veya Reloader benzeri bir araçla tetiklenir.
## D
`ConfigMap` oluşturulduktan sonra hiç değiştirilemez; güncelleme için silip yeniden oluşturmak zorunludur
🤖 `ConfigMap` güncellenebilir bir nesnedir; `immutable: true` yalnızca isteğe bağlı bir tercihtir, genel kural değildir.
## Doğru
A
## Açıklama
`ConfigMap`'in pod'a nasıl enjekte edildiği kritik ayrımdır. Ortam değişkeni olarak enjekte edildiyse, değerler pod başlangıcında dondurulur; `ConfigMap` sonradan değişse de pod yeniden başlatılana kadar eski değerler kalır. Bağlanmış dosya (mounted volume) olarak verildiyse, kubelet dosyanın içeriğini bir süre sonra günceller — ama uygulamanın bu dosyayı yeniden okuması (veya `@RefreshScope`/watch mekanizması) gerekir; dosyanın değişmesi tek başına çalışan süreci güncellemez. Bu yüzden "her iki biçimde de anında yansır" iddiası yanlıştır. Kubernetes `ConfigMap` değişince pod'ları kendiliğinden yeniden başlatmaz; ekipler ya rollout'u elle tetikler ya da Reloader gibi araçlar kullanır. `ConfigMap` değiştirilemez de değildir; `immutable: true` performans/güvenlik için seçilen isteğe bağlı bir işarettir.

# dcfg-6 | Orta
## Soru
Bir geliştirici kolaylık olsun diye `application.yml`'e prod veritabanı şifresini düz metin yazıp git'e commit'lemek istiyor. Doğru senior yanıt nedir?
## A
Sorun değil; depo private olduğu ve yalnızca ekip eriştiği sürece şifrenin git'te durması güvenlidir
🤖 private depo bile sırrı geçmişe kalıcı yazar; erişimi olan herkes, klonlar ve ileride açılan fork'lar risktir.
## B
Şifreyi base64'leyip öyle commit'lemek yeterli korumadır, çünkü değer artık okunur düz metin değildir
🤖 base64 gizleme değil yazım biçimidir; tek komutla çözülür ve commit'lenmiş sır yine sızmış sayılır.
## C
Şifre yalnızca yorum satırında (comment) tutulursa git'e girmesi sakıncasızdır, çünkü uygulama o satırı okumaz
🤖 yorumdaki değer de düz metindir ve geçmişe girer; dosyadaki konumu sırrı korumaz.
## D
Dosyada yalnız `${DB_SIFRE}` yer tutucusu bulunmalı; gerçek değer çalışma anında dışarıdan verilmeli
🤖 Doğru: yer tutucu + dışsallaştırılmış kaynak, sırrı kodun ve versiyon geçmişinin tamamen dışında tutar.
## Doğru
D
## Açıklama
Temel ilke: **sır (şifre/token/anahtar) asla kaynak koda, `application.yml`'e veya git'e girmez** — bu proje kuralında da mutlaktır. Git geçmişi kalıcıdır; bir kez commit'lenen sır repo private olsa (A) veya base64'lense (B) ya da yorumda dursa (C) da "sızmış" kabul edilir ve rotasyon gerektirir. Doğru desen: `application.yml`'de yalnız `${DB_SIFRE}` gibi bir **yer tutucu** (placeholder) bulunur; gerçek değer çalışma anında **ortam değişkeni** ile enjekte edilir ve kaynağı Kubernetes `Secret` ya da Vault'tur. Bu, Twelve-Factor'ın "config in environment" ilkesiyle ve dışsallaştırılmış gizli yönetimiyle örtüşür.
