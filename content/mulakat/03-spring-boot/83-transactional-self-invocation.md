---
slug: transactional-self-invocation
baslik: "@Transactional İç İnceliği: Self-Invocation, private/final ve rollback Varsayılanı"
kademe: İleri
sira: 83
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bankada A hesabından B hesabına para gönderdiğini düşün. Aslında iki ayrı iş yapılır: A'dan para düşülür, B'ye para eklenir. Birincisi olup ikincisi olmazsa para havada kaybolur. Bu yüzden ikisi tek bir paket sayılır — ya ikisi de olur, ya hiçbiri. Spring'de bir metodun başına `@Transactional` yazmak tam olarak bunu söyler: "bu metottaki tüm veritabanı işleri tek paket olsun." Bu konu, o anotasyonun ne zaman gerçekten çalıştığını ve sessizce hiç çalışmadığı üç durumu anlatır.

### Temel kavramlar

- **İşlem (transaction)**: Ya tamamı başarılı olan ya da tamamı iptal edilen veritabanı iş paketi.
- **Commit (kalıcı yazma)**: Paketin sonunda "her şey yolunda, değişiklikleri kalıcı yap" kararı.
- **Rollback (geri alma)**: "Bir şey ters gitti; paketteki tüm değişiklikleri sil, hiç olmamış gibi yap" kararı.
- **Spring'in yönettiği nesne (bean)**: Uygulama açılırken Spring'in kendisinin üretip yaşam döngüsünü yönettiği nesne. Sen `new` demezsin, Spring verir.
- **Vekil nesne (proxy)**: Gerçek nesnenin önüne konan, dışarıdan ondan ayırt edilemeyen sarmalayıcı. Çağrı önce vekile uğrar, vekil ek işi yapar (işlemi açar), sonra gerçek nesneye devreder.
- **AOP (en-kesen programlama)**: İşlem yönetimi, güvenlik, günlükleme gibi her sınıfta tekrarlayan işleri iş mantığından ayırıp otomatik araya sokma tekniği.
- **Unchecked istisna**: `RuntimeException`, `Error` ve alt tipleri; derleyici yakalamanı zorunlu tutmaz.
- **Checked istisna**: `IOException`, `SQLException` gibi tipler; derleyici ya yakalamanı ya `throws` yazmanı ister.

### Nasıl çalışır?

Spring `@Transactional`'ı varsayılan olarak **proxy tabanlı AOP** ile uygular — derleme zamanında bytecode'a kod gömmez, ayrı bir iş parçacığı (thread) açmaz, `@Repository` şartı da aramaz. Konteyner, `@Transactional` taşıyan bean için bir **proxy** üretir: sınıf bir arayüz uyguluyorsa JDK dinamik proxy'si, uygulamıyorsa CGLIB ile üretilen bir alt sınıf.

Tek bir çağrının adımları:

1. Dışarıdan gelen çağrı önce **proxy'ye** ulaşır.
2. Proxy, metodu çağırmadan **önce** işlemi açar (bağlantıyı alır, otomatik commit'i kapatır).
3. Gerçek nesnenin metodu çalışır.
4. Metot normal dönerse proxy **commit**, uygun bir istisna fırlarsa **rollback** eder.

Bütün mesele 1. adımdadır: **çağrı proxy'den geçmezse hiçbir şey olmaz.**

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dış çağrı proxy'den geçer ve işlem açılır; iç çağrı proxy'yi atlar">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Proxy'yi kim geçiyor?</text>
  <rect x="24" y="44" width="120" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="84" y="75" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Çağıran (dış)</text>
  <text x="168" y="66" text-anchor="middle" font-size="17" fill="#25804C" font-family="system-ui">→</text>
  <rect x="196" y="44" width="130" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="261" y="68" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">PROXY</text>
  <text x="261" y="86" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">işlem açılır ✓</text>
  <text x="350" y="66" text-anchor="middle" font-size="17" fill="#25804C" font-family="system-ui">→</text>
  <rect x="378" y="44" width="238" height="52" rx="9" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.3"/>
  <text x="497" y="68" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">disMetot() @Transactional</text>
  <text x="497" y="86" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">gerçek bean</text>
  <rect x="378" y="120" width="238" height="66" rx="9" fill="#FBECEC" stroke="#B4442E" stroke-width="1.4"/>
  <text x="497" y="144" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B4442E" font-family="system-ui">this.icMetot() @Transactional</text>
  <text x="497" y="164" text-anchor="middle" font-size="10.5" fill="#B4442E" font-family="system-ui">proxy ATLANIR → işlem YOK</text>
  <text x="497" y="180" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(aynı sınıf içi çağrı)</text>
  <path d="M497 96 L497 120" stroke="#B4442E" stroke-width="1.4" fill="none" stroke-dasharray="4 3"/>
</svg>
```

İlk tuzak buradan doğar: **nesnenin kendi metodunu kendi içinden çağırması** (self-invocation). Aynı sınıftaki bir metot, `@Transactional` işaretli başka bir metodu `this.icMetot()` (ya da kısaca `icMetot()`) ile çağırırsa çağrı vekile değil doğrudan **gerçek nesneye** gider. Bu yüzden vekil devreye girmez: işlem hiç açılmaz. `propagation` (işlemin yayılma biçimi) ve `readOnly` gibi ayarlar da yok sayılır. Metodun `public` olması bunu kurtarmaz. Ayrıca bu durum `REQUIRES_NEW` gibi "yeni bir işlem" de açmaz — ortada hiç işlem yoktur.

İkinci tuzak proxy'nin araya girme biçiminden gelir. Proxy, işlem tavsiyesini (advice) sokabilmek için metodu **ezmek** (override) zorundadır: JDK proxy'si arayüz metotlarını, CGLIB alt sınıfı `public`/`protected` metotları ezer. Ezilemeyen bir metotta advice'ın tutunacağı yer yoktur.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Proxy metodu ezerek araya girer; private ve final metot ezilemez, anotasyon yok sayılır">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Proxy metodu EZEBİLİYOR mu?</text>
  <text x="320" y="40" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Advice araya girmek için metodu override etmek zorunda</text>
  <rect x="30" y="58" width="270" height="118" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="82" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">public / non-final</text>
  <text x="165" y="104" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">alt sınıf/proxy ezebilir</text>
  <text x="165" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">advice araya girer</text>
  <text x="165" y="150" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">@Transactional ÇALIŞIR ✓</text>
  <rect x="340" y="58" width="270" height="118" rx="10" fill="#FBECEC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="475" y="82" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">private / final</text>
  <text x="475" y="104" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ezilemez (override yok)</text>
  <text x="475" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">advice takılamaz</text>
  <text x="475" y="150" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">SESSİZCE yok sayılır ✗</text>
  <text x="320" y="198" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Kural: @Transactional public (ve non-final) metotlara konur</text>
</svg>
```

`private` metotlar hiçbir alt sınıftan ezilemez; CGLIB'de `final` metotlar da ezilemez. Bu yüzden `private` veya `final` bir metoda konan `@Transactional` **sessizce yok sayılır** — derleyici uyarmaz, çalışma zamanında işlem açılmaz. `static` metotlar da işlem yönetimine uygun değildir. Kural nettir: `@Transactional` **`public` ve non-final** örnek metotlarına konur.

Üçüncü tuzak, "hata olunca nasılsa geri alınır" sanılan varsayılandadır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Rollback varsayılanı: unchecked istisna geri alır, checked istisna commit eder">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Varsayılan rollback: hangi istisna geri alır?</text>
  <rect x="30" y="46" width="270" height="86" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">RuntimeException / Error</text>
  <text x="165" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(unchecked)</text>
  <text x="165" y="116" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">→ ROLLBACK ✓</text>
  <rect x="340" y="46" width="270" height="86" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="475" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">IOException / SQLException</text>
  <text x="475" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(checked)</text>
  <text x="475" y="116" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">→ COMMIT ⚠</text>
  <rect x="90" y="152" width="460" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="320" y="170" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">checked'te de geri almak için:</text>
  <text x="320" y="187" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">@Transactional(rollbackFor = Exception.class)</text>
</svg>
```

İşlem yalnız **unchecked** istisnalarda — `RuntimeException`, `Error` ve alt tipleri — geri alınır. Metot **checked** bir istisna (`IOException`, `SQLException`, kendi yazdığın bir `Exception` alt sınıfı) fırlatırsa işlem **geri alınmaz**; o ana kadarki değişiklikler **commit** edilir ve veri bütünlüğü sessizce bozulabilir. Çözüm açıkça belirtmektir: `@Transactional(rollbackFor = Exception.class)`. Tersi de mümkündür — `noRollbackFor` belirli unchecked istisnalarda commit'e zorlar.

### Mülakatta ne sorulur?

- **"`@Transactional` nasıl uygulanır?"** → Varsayılan **proxy tabanlı AOP**; JDK dinamik proxy veya CGLIB alt sınıfı. Derleme-zamanı dokuma (weaving) AspectJ moduna aittir ve ek yapılandırma ister; ayrı thread açılmaz; `@Repository` şartı yoktur.
- **"Self-invocation'da işlem açılır mı?"** → **HAYIR.** `this.metot()` proxy'yi atlar, işlem **hiç başlamaz**. Görünürlükten bağımsızdır, `REQUIRES_NEW` gibi de davranmaz.
- **"Çözüm ne?"** → Çağrıyı vekil sınırından geçirmek gerekir. Dört yol vardır.
  1. Metodu **ayrı bir bean'e** taşıyıp enjekte ederek çağırmak — en temiz ve en sürdürülebilir yol.
  2. Bean'in **kendini enjekte etmesi** (self-injection) ve o referans üzerinden çağırması.
  3. `AopContext.currentProxy()` ile geçerli vekili alıp onun üzerinden çağırmak (`exposeProxy = true` ister).
  4. Bildirimsel işlemi bırakıp `TransactionTemplate` ile **programatik** işlem açmak.

  Vekil yerine **AspectJ** dokumaya geçmek de tuzağı ortadan kaldırır; ama ek yapılandırma ve derleme/yükleme-zamanı maliyeti getirir. Metotları `static` yapmak çözüm değildir. `@Transactional` alanlara (field) konmaz ve durum "çözümsüz" de değildir.
- **"`private`/`final` metotta çalışır mı?"** → Hayır; metot ezilemediği için advice takılamaz, anotasyon sessizce yok sayılır.
- **"Varsayılan rollback kuralı?"** → **unchecked → rollback, checked → COMMIT.** `rollbackFor` ile genişletilir, `noRollbackFor` ile daraltılır.
- **"`readOnly = true` ne yapar?"** → Hibernate'e verilen bir **optimizasyon ipucudur**. Kalıcı bağlam (persistence context) `FlushMode.MANUAL`'a geçer ve varlıkların anlık görüntüsünü (snapshot) tutmaz. Böylece **kirlilik takibi (dirty checking) ile otomatik flush** — yani belleği veri tabanına boşaltma — atlanır; büyük okumalarda bellek ve CPU tasarrufu sağlar. Bazı sürücü/altyapılarda işleme "read-only" ipucu geçirip okuma replica'sına yönlendirmeye de yarar. Ama **yazmayı fiziksel olarak engelleyen bir kilit değildir**; yanlışlıkla yazma çoğu kez sessizce yok sayılır ya da beklenmedik davranır. Salt bir belge işareti de değildir, önbelleklemeyle de ilgisi yoktur.

### Tek cümlede

`@Transactional` bir proxy'nin işidir: çağrı proxy'den geçmezse (self-invocation, `private`/`final`) işlem hiç açılmaz; geçse bile varsayılanda yalnız unchecked istisnalar geri alma tetikler.

# txself-1 | Temel
## Soru
Spring, bir servis metoduna koyduğun `@Transactional`'ı varsayılan olarak nasıl uygular?
## A
Anotasyonu görüp sınıfın bytecode'una derleme zamanında (compile-time weaving) işlem yönetim kodunu doğrudan enjekte eder; ek yapılandırma gerekmez.
🤖 Varsayılan derleme-zamanı weaving değil; Spring çalışma-zamanı proxy kullanır.
## B
Her `@Transactional` metodu ayrı bir iş parçacığında (thread) çalıştırarak izole eder.
🤖 İşlem ayrı thread'de çalışmaz; aynı thread'e bağlı bir işlem bağlamı açılır.
## C
`@Transactional` yalnız `@Repository` ile işaretli sınıflarda etkindir; servis katmanında yok sayılır.
🤖 @Transactional servis katmanında yaygın kullanılır; @Repository şartı yoktur.
## D
Bean'i bir proxy ile sarar; çağrı dışarıdan proxy üzerinden geçtiğinde proxy işlemi açar, metot bitince commit veya rollback eder.
🤖 Doğru: varsayılan proxy tabanlı AOP; işlem yönetimini proxy yapar.
## Doğru
D
## Açıklama
Spring'in varsayılan işlem yönetimi **proxy tabanlı AOP**'dir. Konteyner, `@Transactional` bean için bir **proxy** üretir (arayüz varsa JDK dinamik proxy, yoksa CGLIB alt sınıfı). Bir çağrı dışarıdan bu proxy'ye geldiğinde, proxy metodu çağırmadan **önce** işlemi (transaction) açar; metot normal dönerse **commit**, uygun bir istisna fırlarsa **rollback** eder. Varsayılan mekanizma derleme-zamanı weaving (A — o AspectJ modudur, ayrıca yapılandırma gerektirir) veya ayrı thread (B) değildir; `@Repository` şartı da yoktur (C). Bu "proxy" gerçeği, sonraki tüm tuzakların (self-invocation, private/final) kökenidir.

# txself-2 | Orta
## Soru
Bir servis metodu, aynı sınıftaki başka bir `@Transactional` metodu `this.digerMetot()` ile çağırıyor. `digerMetot`'un işlemi (transaction) açılır mı?
## A
Hayır; aynı sınıf içi (self-invocation) çağrı proxy'ye uğramaz, işlem BAŞLAMAZ.
🤖 Doğru: this. üzerinden çağrı proxy'ye uğramaz; anotasyon etkisizdir.
## B
Evet; aynı sınıf içi çağrı da proxy üzerinden geçtiğinden `digerMetot`'un `@Transactional`'ı normal biçimde devreye girer ve işlem açılır.
🤖 İç çağrı gerçek nesneye gider, proxy'ye değil; işlem açılmaz.
## C
Yalnız `digerMetot` `private` ise işlem açılmaz; `public` olduğu sürece self-invocation sorun çıkarmaz, proxy çağrıyı yine yakalar.
🤖 Görünürlükten bağımsız olarak self-invocation proxy'yi atlar; public olması kurtarmaz.
## D
Evet; self-invocation her zaman `REQUIRES_NEW` gibi davranıp dış işlemi askıya alır ve bağımsız yeni bir işlem açar.
🤖 Aksine; self-invocation hiç işlem açmaz, propagation ayarı da uygulanmaz.
## Doğru
A
## Açıklama
**Self-invocation** proxy tabanlı AOP'nin en meşhur tuzağıdır. `@Transactional` yalnız çağrı **proxy'den geçtiğinde** çalışır. Bir metot aynı sınıftaki başka bir metodu `this.digerMetot()` (veya doğrudan `digerMetot()`) ile çağırdığında, bu çağrı proxy'ye değil, **gerçek nesnenin** kendisine gider — proxy devreden çıkar. Sonuç: `digerMetot` üzerindeki `@Transactional` (ve `propagation`, `readOnly` gibi tüm ayarları) **sessizce yok sayılır**, işlem açılmaz. Bu, metodun görünürlüğünden bağımsızdır — `public` olması da kurtarmaz (C). "Yeni işlem açar" (D) veya "sorunsuz açılır" (B) yanlıştır. Çözüm için çağrıyı proxy'den geçen bir yola taşımak gerekir (ayrı bean, self-injection, `AopContext`).

# txself-3 | Orta
## Soru
`@Transactional` bir `private` metoda konursa (Spring'in varsayılan proxy modunda) ne olur?
## A
`private` metotlarda `@Transactional` tam olarak çalışır; metot görünürlüğünün Spring'in işlem yönetimiyle hiçbir ilgisi yoktur.
🤖 Proxy metodu ezerek araya girer; private metot ezilemez, bu yüzden anotasyon etkisizdir.
## B
`private` veya `final` metot proxy'nin işini kolaylaştırıp çağrıyı hızlandırır; bu yüzden işlemsel metotlarda tercih edilir.
🤖 final/private, proxy'nin metodu ezmesini engeller; hızlandırmaz, işlevsizleştirir.
## C
Metot ezilemediği için proxy araya giremez; anotasyon sessizce yok sayılır ve işlem açılmaz.
🤖 Doğru: ezilemeyen metotta işlem tavsiyesi (advice) uygulanamaz.
## D
`@Transactional` yalnız `static` metotlarda anlamlıdır; örnek (instance) metotlarında Spring anotasyonu tümüyle yok sayar.
🤖 Tam tersi: static metotlarda çalışmaz; `public` örnek metotlarında çalışır.
## Doğru
C
## Açıklama
Proxy tabanlı AOP, işlem tavsiyesini (transactional advice) araya sokmak için metodu **ezer** (override): JDK proxy arayüz metotlarını, CGLIB ise alt sınıfta `public`/`protected` metotları ezer. `private` metotlar hiçbir alt sınıftan ezilemez; CGLIB'de `final` metotlar da ezilemez. Bu yüzden `private` (ve `final`) metotlara konan `@Transactional` **sessizce yok sayılır** — derleyici uyarmaz, çalışma-zamanında işlem açılmaz. Kural: `@Transactional`'ı **`public`** (ve non-final) metotlara koy. `static` metotlar da işlem yönetimine uygun değildir (D yanlış); görünürlük burada belirleyicidir (A yanlış).

# txself-4 | İleri
## Soru
Varsayılan `@Transactional` yapılandırmasında bir metot **checked** bir istisna (ör. `IOException`) fırlatırsa işleme (transaction) ne olur?
## A
Herhangi bir `Exception` fırladığında — checked olsun unchecked olsun — işlem otomatik olarak geri alınır; Spring'in varsayılan davranışı budur.
🤖 Varsayılan, checked istisnalarda geri almaz; yalnız unchecked tetikler.
## B
Varsayılanda yalnız unchecked istisnalar geri alma tetikler; checked bir istisna fırlasa bile işlem COMMIT olur.
🤖 Doğru: varsayılan yalnız unchecked'te rollback; checked için rollbackFor gerekir.
## C
Hiçbir istisna otomatik geri alma yapmaz; hangi istisna olursa olsun işlemi elle `setRollbackOnly()` çağırarak geri almak gerekir.
🤖 Unchecked istisnalar otomatik geri alır; elle çağrı her zaman gerekmez.
## D
Checked istisna geri alma tetikler, `RuntimeException` ise sessizce commit edilir; Spring bunu veri kaybını önlemek için böyle yapar.
🤖 Tam ters: unchecked geri alır, checked commit eder.
## Doğru
B
## Açıklama
Spring'in çok şaşırtan varsayılanı: işlem yalnız **unchecked** istisnalarda — `RuntimeException` ve `Error` ve alt tipleri — geri alınır. Bir metot **checked** bir istisna (`IOException`, `SQLException`, özel `Exception` alt sınıfları) fırlatırsa, işlem **geri alınmaz** ve o ana kadar yapılan değişiklikler **commit** edilir. Bu, veri bütünlüğünü sessizce bozabilir. Checked istisnalarda da geri almak için açıkça belirt: `@Transactional(rollbackFor = Exception.class)`. Tersi de mümkündür: `noRollbackFor` ile belirli unchecked istisnaları commit'e zorlayabilirsin. "Her istisna geri alır" (A), "hiçbiri" (C) ve "checked geri alır" (D) yanlıştır; varsayılan tam olarak **unchecked → rollback, checked → commit**'tir.

# txself-5 | İleri
## Soru
Bir salt-okuma (read-only) servis metoduna `@Transactional(readOnly = true)` koymanın etkisi nedir?
## A
Veritabanı düzeyinde yazmayı fiziksel olarak imkânsız kılar; metot içindeki her `INSERT`/`UPDATE` mutlaka veritabanı hatasıyla reddedilir.
🤖 readOnly garantili bir yazma engeli değil; asıl etkisi Hibernate optimizasyonudur.
## B
Hiçbir işlevsel etkisi yoktur; yalnızca kodu okuyan geliştirici için niyeti anlatan bir belge (dokümantasyon) işaretidir.
🤖 Gerçek performans etkisi vardır: flush ve dirty checking atlanır.
## C
Metot içindeki her sorgunun sonucunu otomatik olarak ikinci düzey önbelleğe (second-level cache) yazar ve sonraki çağrılarda oradan okur.
🤖 readOnly önbelleklemeyi açmaz; kirlilik takibini/flush'ı atlamayla ilgilidir.
## D
Hibernate'e ipucu verir; kirlilik takibi ve flush atlanır, ama garantili bir yazma engeli değildir.
🤖 Doğru: readOnly bir optimizasyon ipucudur; flush/dirty checking atlanır.
## Doğru
D
## Açıklama
`readOnly = true` bir **optimizasyon ipucudur**, sert bir güvenlik kilidi değil. Hibernate bunu görünce kalıcı bağlamı (persistence context) `FlushMode.MANUAL`'a alır: yönetilen varlıkların **anlık görüntüsünü (snapshot) tutmaz**, dolayısıyla **kirlilik takibi ve otomatik flush yapılmaz** — büyük okuma sorgularında ciddi bellek ve CPU tasarrufu. Ayrıca bazı sürücüler/veritabanları için işleme "read-only" ipucu geçebilir (örneğin okuma replica'sına yönlendirme altyapılarında yararlı). Ancak bu **yazmayı fiziksel olarak engellemez** (A yanlış); yanlışlıkla yazma girişimi çoğu kez sessizce yok sayılır ya da beklenmedik davranır. İşlevsizdir demek (B) ve önbelleklemeyle karıştırmak (C) yanlıştır.

# txself-6 | İleri
## Soru
Self-invocation yüzünden iç metodun `@Transactional`'ı çalışmıyor. Aşağıdakilerden hangisi geçerli bir çözümdür?
## A
Çağrılan metodu ayrı bir bean'e taşıyıp onu enjekte ederek çağırmak; böylece çağrı proxy'den geçer.
🤖 Doğru: çağrıyı proxy'den geçen bir yola taşımak self-invocation'ı çözer.
## B
Tek çözüm ilgili tüm metotları `static` yapmaktır; static metotlar proxy ihtiyacını ortadan kaldırdığı için işlem doğrudan açılır.
🤖 static metotlar işlem yönetimine hiç uygun değildir; sorunu çözmez.
## C
Self-invocation Spring'in kapatılamaz bir kısıtıdır; iç çağrılarda işlem sınırı olmadan yaşamayı kabullenmekten başka yol yoktur.
🤖 Birden çok pratik çözüm vardır; çaresiz bir durum değildir.
## D
`@Transactional`'ı metot yerine sınıfın bir alanına (field) koymak proxy'yi çağrıyı yakalamaya zorlar ve sorunu giderir.
🤖 @Transactional alanlara uygulanmaz; böyle bir çözüm yoktur.
## Doğru
A
## Açıklama
Self-invocation'ın özü, iç çağrının proxy'yi atlamasıdır; çözüm de çağrıyı **proxy'den geçirmektir**. En temiz yol: işlemsel metodu **ayrı bir bean'e** (ayrı servise) taşıyıp onu enjekte ederek çağırmak — böylece çağrı proxy sınırını geçer. Diğer geçerli seçenekler: (1) `AopContext.currentProxy()` ile mevcut proxy'yi alıp onun üzerinden çağırmak (`exposeProxy = true` gerektirir); (2) bean'in kendini enjekte etmesi (self-injection) ve o referans üzerinden çağrı; (3) bildirimsel işlemi bırakıp `TransactionTemplate` ile **programatik** işlem yönetimi. `static` yapmak (B) işlem yönetimini büsbütün bozar, "çözülemez" (C) yanlıştır ve `@Transactional` alanlara konmaz (D). Pratikte en sürdürülebilir çözüm, sorumlulukları ayrı bean'lere bölerek doğal proxy sınırları oluşturmaktır.
