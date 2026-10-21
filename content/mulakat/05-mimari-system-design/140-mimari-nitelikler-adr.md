---
slug: mimari-nitelikler-adr
baslik: "Mimari Nitelikler, Trade-off, ADR ve Fitness Function"
kademe: İleri
sira: 140
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir sistemden iki tür şey istenir. Birincisi **ne yaptığıdır**: "kullanıcı sipariş verebilmeli". Bunlara **işlevsel gereksinim** denir. İkincisi **nasıl olduğudur**: "sipariş sayfası yarım saniyede açılmalı", "sistem yılda en çok 8 saat kapalı kalabilir", "yeni bir ödeme yöntemi bir haftada eklenebilmeli". Bunlara **mimari nitelik** (architectural characteristic) ya da işlevsel olmayan gereksinim denir. Buradaki **gereksinim** kelimesi, sistemden beklenen ve doğrulanabilir talep anlamına gelir.

Şaşırtıcı olan şudur: mimariyi asıl şekillendiren ikinci gruptur. Aynı işlevi (sipariş al) 100 kullanıcı ile 100 milyon kullanıcı için yazmak tamamen farklı sistemler doğurur. **Mimari karar** ise geri dönüşü pahalı olan, birçok bileşeni ve ekibi etkileyen seçimdir. Böyle bir karar birkaç ay sonra "biz bunu neden böyle yapmıştık?" sorusuna dönüşür. Bu konu üç şeyi anlatır: hangi niteliklerin peşinden koşulacağını, kararların **neden** alındığının nasıl kaydedileceğini (**ADR**) ve mimarinin zamanla bozulmasının nasıl engelleneceğini (**fitness function**).

### Temel kavramlar

- **Mimari nitelik:** üç öbekte toplanır. **Operasyonel** öbek çalışma zamanı davranışıdır: erişilebilirlik (availability), performans, ölçeklenebilirlik, kurtarılabilirlik. **Yapısal** öbek kodun iç yapısı ve evrimidir: modülerlik, test edilebilirlik, dağıtılabilirlik, bakım kolaylığı. **Kesişen (cross-cutting)** öbek ise sistemin her yerini keser: güvenlik, gizlilik, erişilebilirlik (accessibility).
- **7±2 kuralı:** bir sistem için açıkça hedeflenen nitelik sayısı bir avuçtan fazla olamaz; "hepsi kritik" demek pratikte "hiçbiri öncelikli değil" demektir.
- **Trade-off (ödünleşme):** bir niteliği güçlendiren kararın başka bir nitelikten ödün vermesi.
- **ADR (Architecture Decision Record):** Michael Nygard'ın önerdiği, sürüm kontrolünde kodun yanında duran kısa belge. Bölümleri: **Başlık**, **Durum** (proposed → accepted → superseded/deprecated), **Bağlam**, **Karar**, **Sonuçlar**.
- **Fitness function:** bir mimari niteliği ya da kuralı **otomatik ve sürekli** ölçen, ihlalinde derlemeyi kıran nesnel test.
- **Connascence (birlikte doğma):** iki kod öğesinin **birlikte değişmek zorunda olma** derecesini ölçen bir bağlılık (coupling) ölçüsüdür. Statik türleri vardır: name, type, meaning, position, algorithm. Bir de daha güçlü olan dinamik türleri vardır: execution/timing, value, identity.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Mimari nitelikler arasında trade-off dengesi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Nitelikler çatışır: her seçim bir ödün</text>
  <rect x="60" y="48" width="160" height="120" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="140" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Operasyonel</text>
  <text x="140" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">availability</text>
  <text x="140" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">performance</text>
  <text x="140" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">scalability</text>
  <rect x="240" y="48" width="160" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Yapısal</text>
  <text x="320" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">testability</text>
  <text x="320" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">deployability</text>
  <text x="320" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">maintainability</text>
  <rect x="420" y="48" width="160" height="120" rx="12" fill="#FFF6E5" stroke="#B8860B" stroke-width="1.5"/>
  <text x="500" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#8A6400" font-family="system-ui">Kesişen</text>
  <text x="500" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">security</text>
  <text x="500" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">privacy</text>
  <text x="500" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">accessibility</text>
</svg>
```

### Nasıl çalışır?

Nitelikler birbiriyle **çatışır**. Yüksek performans genellikle basitlikten ve maliyetten ödün ister. Güçlü tutarlılık erişilebilirlikten ödün ister (CAP). Sıkı güvenlik kullanım kolaylığından ödün ister. Bu yüzden mimarlık "en iyi"yi değil, öncelikli nitelikler için **en az kötü ödünleşmeyi** aramaktır.

Doğru yöntem iki adımlıdır. Önce paydaşlarla somut senaryolar üzerinden konuşup küçük ve **açıkça sıralanmış** bir nitelik çekirdeği belirlersin. Sonra her kararın hangi niteliği güçlendirip hangisini zayıflattığını görünür kılarsın.

Seçilen kararın gerekçesi altı ay sonra buharlaşır. Onu tutan şey **ADR**'dir. ADR yalnız **yük taşıyan** kararlar için yazılır: geri dönüşü pahalı, geniş etkili ve bir niteliği belirgin biçimde değiştiren kararlar. Örnekler: veri deposu seçimi, eşzamanlı/eşzamansız entegrasyon tercihi, katman kuralı. Her küçük tercih için ADR yazmak belgeleri gürültüye boğar. ADR'yi değerli kılan, kararın kendisi değil **neden** alındığı ve **hangi alternatiflerin neden elendiğidir**.

ADR'ler **eklemeli bir defterdir**: bir ADR **accepted** olduktan sonra metni değiştirilmez. Karar geçersizleşince eskisine dokunulmaz; durumu **superseded** yapılır, yerine yeni bir ADR yazılır ve iki belge birbirine atıf verir. Böylece hem bugünkü durum hem de kararların **evrimi** okunabilir kalır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ADR belgesinin bölümleri ve durum yaşam döngüsü">
  <defs>
    <marker id="adr-arrow" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">ADR: özü 'neden' ve elenen alternatifler</text>
  <rect x="30" y="44" width="330" height="150" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="195" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Başlık</text>
  <text x="195" y="88" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Durum · Bağlam (hangi kuvvetler)</text>
  <text x="195" y="112" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Karar: ne + NEDEN seçildi</text>
  <text x="195" y="136" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Sonuçlar: bedel + neden ötekiler değil</text>
  <text x="195" y="168" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kısa · sürüm kontrolünde ·</text>
  <text x="195" y="184" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kodun yanında yaşar</text>
  <text x="500" y="60" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Durum döngüsü</text>
  <rect x="440" y="72" width="120" height="30" rx="7" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="500" y="92" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">proposed</text>
  <line x1="500" y1="102" x2="500" y2="118" stroke="#5D7789" stroke-width="2" marker-end="url(#adr-arrow)"/>
  <rect x="440" y="120" width="120" height="30" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="500" y="140" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">accepted</text>
  <line x1="500" y1="150" x2="500" y2="166" stroke="#5D7789" stroke-width="2" marker-end="url(#adr-arrow)"/>
  <rect x="440" y="168" width="120" height="30" rx="7" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="500" y="188" text-anchor="middle" font-size="10.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">superseded</text>
</svg>
```

### Mülakatta ne sorulur?

**"Nitelikleri nasıl sınıflandırırsın?"** — Operasyonel, yapısal ve kesişen diye. Test edilebilirlik, dağıtılabilirlik ve bakım kolaylığı yapısal öbektedir.

**"Bütün nitelikleri en üst düzeye çıkaralım"** cümlesinin sorunu sorulur. Cevap: nitelikler çatıştığı için bu imkânsızdır; önceliklendirme şarttır.

**"ADR'de en kritik kısım hangisi?"** — Bağlam, gerekçe ve elenen alternatifler. Yazarın unvanı, kod satırı ya da şablonun estetiği değil.

**"Kabul edilmiş bir ADR yanlış çıktı, ne yaparsın?"** — Metnini düzeltmem. Kaydı superseded işaretler ve yenisini yazarım.

**"Mimari erozyonu nasıl engellersin?"** — Fitness function ile. Örnekleri: ArchUnit katman kuralı testi, gecikme eşiği testi, döngüsel bağımlılık taraması. Her derlemede çalışır ve mimariyi yürütülebilir bir sözleşmeye çevirir.

**Connascence** sorulduğunda ilke tektir: güçlü türleri (position, value, timing) **yakın** tut. Modül ve servis sınırları arasında ise **zayıf** türlere çevir. Örneğin sıraya bağlı parametreleri isimli alanlara (connascence of name) dönüştür.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Güçlü connascence yakın tutulur, sınır ötesinde zayıflatılır">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Connascence: güçlüyü yakın tut, sınırda zayıflat</text>
  <rect x="40" y="50" width="230" height="130" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="155" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Modül içi: yakın</text>
  <text x="155" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">güçlü tür kabul edilebilir</text>
  <text x="155" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">position, value, timing</text>
  <text x="155" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">birlikte değişmesi ucuz</text>
  <rect x="370" y="50" width="230" height="130" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="485" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Sınır ötesi: zayıflat</text>
  <text x="485" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">güçlü → zayıf türe çevir</text>
  <text x="485" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sıra → isimli alan (name)</text>
  <text x="485" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">uzak değişim kırılgan</text>
  <text x="320" y="118" text-anchor="middle" font-size="20" fill="#5D7789" font-family="system-ui">→</text>
</svg>
```

### Tek cümlede

Mimari nitelikler birbiriyle çatıştığı için önceliklendirilmeleri zorunludur. Seçilen ödünleşmenin gerekçesi ADR'lerde saklanır; bu kayıtlar değiştirilmez, gerektiğinde superseded edilip üstüne yenisi yazılır. Kararın zamanla aşınmaması ise fitness function'larla otomatik olarak korunur.

# nfr-1 | İleri
## Soru
`testability`, `deployability` ve `maintainability` nitelikleri hangi mimari nitelik öbeğine girer?
## A
Operasyonel nitelikler
🤖 Operasyonel öbek availability/performance/scalability gibi çalışma-zamanı nitelikleridir.
## B
Kesişen (cross-cutting) nitelikler
🤖 Kesişen öbek security/privacy/accessibility gibi her yeri kesen nitelikleri kapsar.
## C
İşlevsel gereksinimler
🤖 Bunlar işlevsel değil; sistemin ne yaptığını değil nasıl olduğunu tanımlar.
## D
Yapısal (structural) nitelikler
🤖 Doğru: bu üçü kodun yapısı ve evrimiyle ilgili yapısal niteliklerdir.
## Doğru
D
## Açıklama
Mimari nitelikler kabaca üç öbeğe ayrılır. **Yapısal** (structural) öbek kodun **iç yapısı ve evrimiyle** ilgilenir: modularity, **testability**, **deployability**, **maintainability**, extensibility. **Operasyonel** öbek **çalışma zamanı** davranışıdır: availability, performance, scalability, recoverability (A yanlış). **Kesişen** (cross-cutting) öbek sistemin her yerini kesen security, privacy, accessibility gibi nitelikleri kapsar (B yanlış). Hiçbiri **işlevsel gereksinim** değildir (C yanlış): işlevsel gereksinim sistemin *ne yaptığını* tanımlar, nitelikler *nasıl* olması gerektiğini. Bu sınıflandırma, hangi niteliklerin birlikte düşünülmesi ve hangi ölçümlerle izlenmesi gerektiğini netleştirir.

# nfr-2 | İleri
## Soru
Bir mimar "sistemimiz aynı anda maksimum performans, maksimum basitlik, maksimum ölçeklenebilirlik ve maksimum güvenlik sağlayacak" diyor. Bu iddianın temel sorunu nedir?
## A
Sorun yok; yetkin bir mimar bütün nitelikleri aynı anda maksimize edebilir
🤖 Nitelikler çatışır; hepsini birden maksimize etmek pratikte mümkün değildir.
## B
Yalnızca erişilebilirlik eksik; onu da listeye eklerse iddia eksiksiz hale gelir
🤖 Sorun eksik nitelik değil; hepsini birden maksimize etme varsayımının kendisi.
## C
Nitelikler çatışır; mimari maksimizasyon değil en az kötü ödünleşmeyi arar
🤖 Doğru: mimari maksimizasyon değil, önceliklere göre trade-off dengesidir.
## D
Sorun ölçülebilir hedef verilmemesi; her niteliğe sayısal eşik yazılırsa hepsi sağlanır
🤖 Ölçülebilirlik iyidir ama çatışmayı ortadan kaldırmaz; eşikler de birbirini keser.
## Doğru
C
## Açıklama
Mimarinin temel gerçeği, niteliklerin **çatışmasıdır**: yüksek performans genellikle basitlikten ve maliyetten ödün ister; güçlü tutarlılık erişilebilirlikten (CAP); sıkı güvenlik kullanım kolaylığı ve performanstan. Bu yüzden "hepsini maksimize et" **imkansız** bir hedeftir; her mimari karar bazı nitelikleri güçlendirirken başkalarını zayıflatır. İyi mimarlık "en iyi"yi değil, sistemin **öncelikli** nitelikleri için **en az kötü ödünleşmeyi** seçmektir. Bu da niteliklerin **açıkça önceliklendirilmesini** gerektirir (7±2 kuralı); "hepsi eşit önemli" demek pratikte "hiçbiri öncelikli değil" demektir ve bir çatışma çıktığında neyin feda edileceğine karar vermeyi imkansız kılar. Sayısal eşik yazmak (D) hedefi ölçülebilir yapar ama çatışmayı ortadan kaldırmaz.

# nfr-3 | İleri
## Soru
Bir **ADR** (Architecture Decision Record) belgesinde en kritik olan, onu basit bir "kararı duyuran not"tan ayıran içerik hangisidir?
## A
Kararı alan kişinin unvanı, iletişim bilgileri ve onay veren yöneticinin imzası
🤖 Kim aldığı ikincil; ADR'nin değeri kararın gerekçesinde, kimlik bilgisinde değil.
## B
Kararın bağlamı, gerekçesi ve elenen alternatifler: neden bu, neden ötekiler değil
🤖 Doğru: ADR'nin özü "neden" ve hangi alternatiflerin neden elendiğidir.
## C
Kararın uygulanacağı sınıf, dosya ve kod satırı numaralarının eksiksiz listesi
🤖 ADR kod düzeyi talimat değil; mimari kararın gerekçesini ve sonuçlarını kaydeder.
## D
Belgeyi biçimlendiren Markdown şablonunun görsel stili ve başlık düzeni
🤖 Biçim ikincil; asıl değer içerikte — bağlam, karar, gerekçe ve sonuçlar.
## Doğru
B
## Açıklama
**ADR**, bir mimari kararı gelecekteki okuyucu için kalıcılaştıran kısa belgedir; Nygard'ın klasik bölümleri **Başlık**, **Durum** (proposed/accepted/superseded), **Bağlam**, **Karar** ve **Sonuçlar**'dır. ADR'yi değerli kılan, kararın kendisi kadar **neden** alındığı ve **hangi alternatiflerin neden elendiğidir**; çünkü üç ay ya da üç yıl sonra "bunu neden böyle yaptık?" sorusuna cevap veren tek şey budur. Aksi halde ekip aynı tartışmaları baştan yapar veya kararı farkında olmadan bozar. Karar verenin unvanı (A), kod satır numaraları (C) ve belgenin görsel stili (D) ikincildir. ADR'ler sürüm kontrolünde, etkiledikleri kodun yanında tutulur ve yalnız **yük taşıyan** (geri dönüşü pahalı, geniş etkili) kararlar için yazılır.

# nfr-4 | İleri
## Soru
Bir ekip "katmanlar arası bağımlılık kuralı zamanla ihlal edilmesin" istiyor ve bunu her derlemede **otomatik** kontrol eden bir ArchUnit testi yazıyor. Bu mekanizma nedir?
## A
Fitness function: mimari niteliği otomatik ve sürekli ölçüp ihlalinde uyarır
🤖 Doğru: mimari kuralı otomatik doğrulayan nesnel test bir fitness function'dır.
## B
ADR: alınan mimari kararı bağlamı ve gerekçesiyle yazıya döken kayıt belgesi
🤖 ADR kararı yazıya döker ama otomatik doğrulamaz; bu ise çalıştırılabilir bir kontrol.
## C
Domain event: alanda olmuş bitmiş, iş açısından anlamlı bir olguyu bildiren mesaj
🤖 Domain event bir iş olgusudur; mimari kuralı ölçen bir test değildir.
## D
Anti-corruption layer: dış sistemin modelini kendi modeline çeviren koruyucu katman
🤖 ACL context'ler arası çeviri katmanıdır; mimari kural doğrulamasıyla ilgisi yoktur.
## Doğru
A
## Açıklama
**Fitness function**, bir mimari niteliği veya kuralı **otomatik, tekrarlanabilir ve sürekli** ölçen nesnel testtir; ihlal olduğunda derlemeyi veya pipeline'ı kırarak **mimari erozyonu** önler. ArchUnit ile "types → config → repo → service → ai → web bağımlılık yönü" gibi bir katman kuralını doğrulamak klasik örnektir; gecikme eşiği testleri, döngüsel bağımlılık taraması ve kapsam eşikleri de fitness function sayılır. Bunlar mimariyi "yaz ve unut" olmaktan çıkarıp **yürütülebilir ve korunan** bir sözleşmeye dönüştürür — çünkü yazılı bir kural, otomatik olarak denetlenmiyorsa zamanla mutlaka aşınır. ADR (B) kararı *belgeler* ama denetlemez; domain event (C) ve ACL (D) tamamen farklı kavramlardır.

# nfr-5 | İleri
## Soru
İki modül bir alan dizisinin **elemanlarının aynı sırada** gelmesine bel bağlıyor; sıra değişirse ikisi de bozuluyor. Bu tür bir bağlılığı (coupling) tanımlayan kavram ve tavsiye edilen yaklaşım nedir?
## A
Bu ideal bir tasarımdır; sıraya bağlılık iki modülü birbirine sağlamca kenetler
🤖 Sıraya bağlılık kırılgan bir coupling türüdür; sağlamlaştırmaz, sessiz hataya açar.
## B
Bu bir domain event ilişkisidir; olayların sırasını korumak her zaman güvenlidir
🤖 Konu olay değil; iki modülün sıraya birlikte bağımlı olması bir coupling ölçüsüdür.
## C
Burada connascence yoktur; ayrı modüller hiçbir zaman birlikte değişmek zorunda kalmaz
🤖 Tam tersi; birlikte değişme zorunluluğu connascence'ın tanımıdır ve burada mevcuttur.
## D
Connascence of position; güçlü bir bağlılık türüdür, sınırlar arasında zayıflatılmalıdır
🤖 Doğru: konuma bağlılık güçlü connascence'tır; sınır ötesinde daha zayıf türe çevrilmeli.
## Doğru
D
## Açıklama
**Connascence**, iki kod öğesinin **birlikte değişmek zorunda** olma derecesini ölçen bir coupling metriğidir. Öğelerin belirli bir **sıraya** bağlı olması **connascence of position**'dır: parametre sırası veya dizi eleman sırası değiştiğinde her iki taraf da bozulur ve hata çoğu zaman derleyiciden kaçıp çalışma zamanında ortaya çıkar. Genel ilke şudur: güçlü connascence türlerini (position, algorithm, timing, value) **yakın** tut — aynı sınıf ya da fonksiyon içinde, çünkü orada birlikte değiştirmek ucuzdur; modül ve servis **sınırları arasında** ise daha **zayıf** türlere çevir. Sıraya bağlı parametreleri isimli alanlara (connascence of name) dönüştürmek klasik çözümdür. Connascence'ın var olmadığını söylemek (C) veya sıraya bağlılığı ideal saymak (A) yanlıştır.

# nfr-6 | İleri
## Soru
Bir yıl önce **accepted** durumuna alınmış bir ADR'nin kararı, değişen koşullar yüzünden artık geçerli değil. Doğru davranış nedir?
## A
Eski ADR'nin metni yeni karara göre yerinde güncellenir; tek ve güncel bir belge kalır
🤖 Kabul edilmiş ADR değiştirilmez; metni ezmek kararın tarihini ve gerekçesini yok eder.
## B
Eski ADR olduğu gibi bırakılır, durumu superseded yapılır ve yerine yeni ADR yazılır
🤖 Doğru: ADR'ler eklemeli bir kayıttır; eski karar silinmez, üstüne yenisi yazılır.
## C
Eski ADR depodan silinir; geçersiz bir belge okuyucuyu yanıltacağı için saklanmaz
🤖 Silmek geçmişi yok eder; "o gün neden böyle karar verdik" bilgisi de kaybolur.
## D
ADR accepted bırakılır, değişiklik yalnız kodda yapılır, kayıt sonra düzeltilir
🤖 Kayıt ile kod ayrışır; belgelenmemiş sapma ADR'leri güvenilmez hale getirir.
## Doğru
B
## Açıklama
ADR'ler **eklemeli (append-only) bir karar defteridir**: bir ADR **accepted** olduktan sonra metni değiştirilmez, çünkü belgenin değeri "o gün, o bağlamda, şu gerekçeyle şunu seçtik" kaydını olduğu gibi korumasındadır. Koşullar değişip karar geçersizleştiğinde eski ADR'ye dokunulmaz: durumu **superseded** (yerine geçildi) yapılır, hangi ADR tarafından geçersizleştiği yazılır ve yeni karar **ayrı bir ADR** olarak eklenip eskisine atıf verir. Böylece hem güncel durum hem de kararların **evrimi** okunabilir kalır; yeni gelen biri "neden değişti?" sorusunun cevabını bulur. Metni yerinde güncellemek (A) veya belgeyi silmek (C) bu tarihi yok eder; kodu belgeden bağımsız değiştirmek (D) ise ADR'leri güvenilmez bir süse çevirir. Not: ADR yalnızca **yük taşıyan** kararlar için yazılır, her küçük tercih için değil.
