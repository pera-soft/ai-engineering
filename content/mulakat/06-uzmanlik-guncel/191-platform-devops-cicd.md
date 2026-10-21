---
slug: platform-devops-cicd
baslik: "Platform & DevOps: CI/CD, GitOps, IaC, Progressive Delivery, DORA ve SBOM"
kademe: İleri
sira: 191
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bilgisayarında çalışan kodun, dünyanın öbür ucundaki sunucuda kullanıcıya hizmet eder hâle gelmesine **dağıtım** (deployment) denir. Arada birçok adım vardır: kodu derle, testleri koştur, paketle, sunucuya taşı, ayarları ver, eskisini durdurup yenisini başlat. Bu adımların **otomatik ve hep aynı sırayla** koşan zincirine **boru hattı** (pipeline) denir — fabrikadaki yürüyen bant gibi: parça bir uçtan girer, her istasyon işini yapar, diğer uçtan hazır ürün çıkar.

Neden otomatik? Çünkü elle yapılan dağıtım her seferinde biraz farklı olur; biri bir adımı unutur, bir sunucuya yanlış ayar girer. Bu bölüm, yazılımın **hızlı, sık ve güvenli** biçimde kullanıcıya ulaşmasını sağlayan pratikleri anlatır.

### Temel kavramlar

- **CI** (Continuous Integration, sürekli bütünleştirme): her `push`'ta kodu derleyip test ve statik analizden geçiren otomatik boru hattı. Amaç ana dalın (main) **her zaman yeşil** kalmasıdır.
- **CD**: iki anlamı vardır — **Continuous Delivery** yeşil yapıyı her an dağıtıma hazır tutar (çıkış kararı insanındır); **Continuous Deployment** testleri geçen her commit'i otomatik üretime çıkarır.
- **GitOps**: dağıtımın **istenen durumu** git deposunda **bildirimsel** (declarative, "ne olsun" — "nasıl yapılsın" değil) tutulur. Bir denetleyici (Argo CD/Flux) kümenin gerçek durumunu sürekli git'le karşılaştırıp ona **yakınsar** (reconcile). "Deploy et" demek, git'e commit/merge etmek demektir.
- **Sapma** (drift): kümenin gerçek durumunun, depodaki tanımdan uzaklaşması. Kökeni, elle tek tek komut vererek (imperative) yapılan ve hiçbir yere kaydedilmeyen `kubectl apply` müdahaleleridir.
- **IaC** (Infrastructure as Code — altyapının kod olarak yazılması): altyapı da (ağ, sunucu, veritabanı, kuyruk) Terraform/Pulumi ile kod olarak tanımlanır.
- **Özellik bayrağı** (feature flag): kodu üretime çıkarıp **kapalı** tutan, çalışma zamanında açılıp kapanan anahtar.
- **Kademeli yayın** (canary): yeni sürümü önce trafiğin %1'ine verip metriklere bakma, iyiyse payı adım adım büyütme.
- **DORA metrikleri**: teslimat sağlığının dört ölçüsüdür. **Deployment Frequency** (ne sıklıkta dağıtıyoruz). **Lead Time for Changes** (bir değişiklik üretime kaç günde ulaşıyor). **Change Failure Rate** (dağıtımların yüzde kaçı olaya dönüşüyor). **Recovery Time / MTTR** (bozulunca ne kadar sürede toparlıyoruz). İlk ikisi **hızı**, son ikisi **kararlılığı** ölçer.
- **SBOM** (Software Bill of Materials): bir yapının içindeki tüm bileşenleri ve **kesin sürümlerini** makine-okunur listeleyen envanter (CycloneDX/SPDX).

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="DORA: hız ve kararlılık dört metrik">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">DORA — hız + kararlılık birlikte</text>
  <rect x="30" y="46" width="270" height="128" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Hız</text>
  <text x="165" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Deployment Frequency</text>
  <text x="165" y="120" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Lead Time for Changes</text>
  <text x="165" y="148" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ne kadar sık / ne kadar hızlı</text>
  <rect x="340" y="46" width="270" height="128" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Kararlılık</text>
  <text x="475" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Change Failure Rate</text>
  <text x="475" y="120" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Recovery Time (MTTR)</text>
  <text x="475" y="148" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ne sıklıkta bozulur / ne hızla düzelir</text>
</svg>
```

### Nasıl çalışır?

**DORA'nın merkezî bulgusu**, sezgiye aykırıdır: hız ile kararlılık **ödünleşmez** (trade-off değildir), birlikte iyileşir. "Seyrek dağıtırsak daha güvenli oluruz" yanlıştır, çünkü ayda bir çıkan dev sürüm yüzlerce değişikliği tek pakette taşır; bir şey bozulunca hangisi olduğunu bulmak zordur ve etkisi geniştir. Küçük ve sık dağıtımlar her seferinde az değişiklik taşır: hem bozulma olasılığı hem toparlanma süresi düşer. Dört metrik birlikte okunur; yalnız birine bakmak resmi bozar.

**GitOps drift'i nasıl kapatır?** Git tek doğru kaynak (single source of truth) olur; denetleyici kümede git'te olmayan bir değişiklik görürse onu **geri alır**. Ayrıca her değişiklik commit/PR'dan geçtiği için gözden geçirilebilir, denetlenebilir (audit) ve `git revert` ile geri alınabilir olur. GitOps bildirimseldir; drift'in kaynağı olan imperative komutların tam zıddıdır ve elle müdahaleyi kolaylaştırmaz, azaltır.

**IaC ne kazandırır?** Yazılım geliştirmenin disiplinini altyapıya taşır: sürümleme, PR ile gözden geçirme, tekrarlanabilir uygulama, geri alma. Aynı tanımdan dev/stage/prod ortamları tutarlı kurulur; "elle kurduğumda farklı çıktı" sınıfı sorunlar kaybolur. Bulut kilitlenmesini (lock-in) tümüyle çözmek onun işi değildir ve çalışma zamanı performansıyla ilgisi yoktur.

**Deploy ≠ release.** Bu ayrım bölümün kalbidir. **Dağıtım (deploy)** kodu üretime koymaktır; **yayınlama (release)** o kodu kullanıcıya açmaktır. Özellik bayrağı ikisini ayırır: kod üretimdedir ama bayrak kapalıdır, sonra %1, %5, %25... diye kademeli açılır. Bu kademeli açma pratiğinin adı **aşamalı teslimat (progressive delivery)**'tir. Metrikler kötüyse yeniden dağıtım yapmadan bayrak **saniyeler içinde** kapatılır. Kademeli yayın (canary) aynı amaca trafik yönlendirmeyle hizmet eder.

Bu güvenlik ağı olmadan hızlı otomatik dağıtım tehlikelidir. Testler her hatayı yakalayamaz; özellikle üretim verisine, gerçek yüke ve ortam ayarlarına bağlı hatalar testten kaçar. Kaçan kusur da **anında %100 kullanıcıya** ulaşır. Doğru yanıt hızı bırakmak değildir; hıza **kademeli açma + üretim izleme + otomatik geri alma** eklemektir.

**Tedarik zinciri.** Bağımlılıklar ve yapı süreci de saldırı yüzeyidir. Kritik bir **CVE** (Common Vulnerabilities and Exposures — bilinen bir güvenlik açığının numaralı kaydı) açıklandığında ilk ve en pahalı soru şudur: "**biz nerede etkilendik?**" Yüzlerce servisi elle taramak günler alır; saklanan **SBOM**'lar ise saniyeler içinde sorgulanır.

Üç tamamlayıcı pratik vardır. Birincisi, bağımlılık sürümlerini **sabitlemek** (pin); sabitlemeyi bırakmak aynı kodun her yapıda farklı çıkmasına yol açar. İkincisi, yapı çıktısını **imzalamak** (Sigstore/cosign). Üçüncüsü, bağımlılıkları düzenli taramaktır.

Pratik kural: küçük, sık, otomatik dağıt; sürümü **flag/canary** ile kademeli aç; sağlığı **DORA** ile ölç; tedarik zincirini **SBOM + imza** ile koru.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="GitOps reconcile döngüsü: git tek doğru kaynak">
  <defs>
    <marker id="cicd-arrow" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
    <marker id="cicd-arrow-red" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#8A2E2E"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">GitOps: git tek doğru kaynak, denetleyici yakınsar</text>
  <rect x="26" y="52" width="164" height="92" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="108" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Git deposu</text>
  <text x="108" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istenen durum</text>
  <text x="108" y="124" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bildirimsel (YAML)</text>
  <rect x="238" y="52" width="164" height="92" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="320" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Denetleyici</text>
  <text x="320" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Argo CD / Flux</text>
  <text x="320" y="124" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">karşılaştır + uygula</text>
  <rect x="450" y="52" width="164" height="92" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="532" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Küme</text>
  <text x="532" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">gerçek durum</text>
  <text x="532" y="124" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">canlı state</text>
  <line x1="192" y1="98" x2="234" y2="98" stroke="#5D7789" stroke-width="1.5" marker-end="url(#cicd-arrow)"/>
  <line x1="404" y1="98" x2="446" y2="98" stroke="#5D7789" stroke-width="1.5" marker-end="url(#cicd-arrow)"/>
  <path d="M532 144 L532 182 L320 182 L320 148" fill="none" stroke="#8A2E2E" stroke-width="1.5" marker-end="url(#cicd-arrow-red)"/>
  <text x="426" y="176" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">drift algılandı → geri al (reconcile)</text>
</svg>
```

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Deploy release ayrımı: feature flag ile kademeli açılış">
  <defs>
    <marker id="cicd-step" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#186399"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Deploy ≠ Release: feature flag ile kademeli aç</text>
  <rect x="20" y="62" width="130" height="74" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="85" y="92" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">Deploy</text>
  <text x="85" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kod üretimde</text>
  <text x="85" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bayrak kapalı</text>
  <line x1="152" y1="99" x2="182" y2="99" stroke="#186399" stroke-width="1.5" marker-end="url(#cicd-step)"/>
  <rect x="186" y="74" width="112" height="50" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="242" y="95" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">%1 canary</text>
  <text x="242" y="113" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">metrik izle</text>
  <line x1="300" y1="99" x2="328" y2="99" stroke="#186399" stroke-width="1.5" marker-end="url(#cicd-step)"/>
  <rect x="332" y="74" width="112" height="50" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="388" y="95" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">%10</text>
  <text x="388" y="113" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iyiyse büyüt</text>
  <line x1="446" y1="99" x2="474" y2="99" stroke="#186399" stroke-width="1.5" marker-end="url(#cicd-step)"/>
  <rect x="478" y="74" width="112" height="50" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="534" y="95" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">%100</text>
  <text x="534" y="113" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tam yayın</text>
  <text x="320" y="162" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">metrik kötü → bayrağı anında kapat (yeniden dağıtımsız, saniyeler)</text>
</svg>
```

### Mülakatta ne sorulur?

- **"'Seyrek dağıtmak daha güvenli' diyene ne dersin?"** Beklenen: DORA hız ile kararlılığın birlikte iyileştiğini gösterir; küçük değişiklik riski ve toparlanma süresini düşürür.
- **"Kümede drift var, GitOps nasıl yardım eder?"** Beklenen: git bildirimsel tek doğru kaynak, denetleyici sürekli reconcile eder, elle değişikliği geri alır. Amacı CI'yi hızlandırmak değildir.
- **"Deploy ile release farkı ne?"** Beklenen: deploy kodu üretime koyar, release kullanıcıya açar; ayırıcı teknik feature flag/canary, geri alma yeniden dağıtımsızdır.
- **"CVE çıktı, hangi servisler etkilendi?"** Beklenen: SBOM envanterini sorgula. Elle tarama ölçeklenmez, yeniden başlatma zafiyeti gidermez, sürümü sabitlememek durumu kötüleştirir.
- **"Konsoldan elle kurmak yerine neden IaC?"** Beklenen: sürümlenebilir, PR ile gözden geçirilebilir, tekrarlanabilir, geri alınabilir altyapı ve ortamlar arası tutarlılık.
- **"Testler geçen her commit üretime gidiyor, riski ne?"** Beklenen: güvenlik ağı yok; kademeli açma, üretim izleme ve otomatik geri alma eklenmeli.

### Tek cümlede

Platform mühendisliği, teslimatı **otomatik bir boru hattına** almak, istenen durumu **git'te bildirimsel** tutmak, altyapıyı **kod** olarak sürümlemek, **dağıtımı yayınlamadan ayırıp** kademeli açmak, sağlığı **DORA** ile ölçmek ve tedarik zincirini **SBOM + imza** ile görünür kılmaktır.

# cicd-1 | İleri
## Soru
Bir yönetici "dağıtım sıklığını artırmayalım, her dağıtım risk demektir; ayda bir büyük sürüm daha güvenli" diyor. DORA bulguları ışığında en doğru itiraz nedir?
## A
Yönetici haklı; dağıtım sıklığı arttıkça hata oranı da doğrusal olarak artar
🤖 DORA'nın tersini bulur: yüksek performanslı ekipler hem sık dağıtır hem düşük hata oranı tutar.
## B
Yüksek performanslı ekipler sık VE küçük dağıtır; hız ile kararlılık birlikte yükselir
🤖 Doğru: DORA hız ile kararlılığın ödünleşmediğini, birlikte iyileştiğini gösterir; büyük seyrek sürüm daha risklidir.
## C
Sıklık önemsizdir; teslimat sağlığı için yalnız change failure rate ölçülmeli
🤖 DORA dört metriği birlikte değerlendirir; sıklık hız boyutunun temel ölçüsüdür, atılamaz.
## D
Doğru yaklaşım dağıtımı tümüyle durdurup her sürümü elle test etmektir
🤖 Elle sürece dönmek lead time'ı ve toparlanmayı kötüleştirir; DORA'nın önerdiğinin tam tersi.
## Doğru
B
## Açıklama
DORA araştırmasının merkezî bulgusu **hız ile kararlılığın ödünleşmediği** (trade-off olmadığı), aksine **birlikte** iyileştiğidir. Sık ve **küçük** dağıtımlar her seferinde daha az değişiklik taşır; bu, hem bir sorun çıkma olasılığını hem de çıktığında **kök nedeni bulup toparlanma** süresini düşürür. Ayda bir dev sürüm ise yüzlerce değişikliği bir arada taşır — bir şey bozulunca hangisi olduğunu bulmak zordur ve etki alanı büyüktür. Bu yüzden "seyrek = güvenli" sezgisi yanlıştır (A). Dört DORA metriği birlikte okunur; sıklık atılamaz (C) ve elle sürece dönmek (D) tüm metrikleri kötüleştirir.

# cicd-2 | İleri
## Soru
Ekip Kubernetes'e elle `kubectl apply` ile dağıtım yapıyor; kümenin gerçek durumu ile depodaki YAML'ler zamanla uyuşmuyor (drift). GitOps'a geçmenin bu soruna temel katkısı nedir?
## A
GitOps YAML yerine zorunlu (imperative) komutlar kullandığı için drift hiç oluşmaz
🤖 GitOps tam tersine bildirimseldir (declarative); imperative komutlar drift'in kaynağıydı.
## B
GitOps CI boru hattı süresini kısaltarak dağıtımı hızlandırır ve sapmayı azaltır
🤖 GitOps'un amacı süre değil; istenen durumu git'te tutup küme durumunu ona yakınsamaktır.
## C
GitOps küme erişimini herkese açarak elle müdahaleyi ve düzeltmeyi kolaylaştırır
🤖 Aksine elle müdahaleyi azaltır; değişiklik git üzerinden, gözden geçirmeyle akar.
## D
İstenen durum git'te bildirimsel tutulur; denetleyici küme durumunu sürekli yakınsar
🤖 Doğru: git tek gerçek kaynak olur; elle değişiklik reconcile ile geri sarılır, dağıtım denetlenebilir/geri alınabilir olur.
## Doğru
D
## Açıklama
**Drift'in kök nedeni**, kümeyi elle (imperative) değiştirip bu değişiklikleri hiçbir yerde kaydetmemektir. **GitOps** istenen durumu **git'te bildirimsel** tutar ve bunu **tek doğru kaynak** (single source of truth) yapar; Argo CD/Flux gibi bir denetleyici küme durumunu sürekli git ile karşılaştırıp **yakınsar** (reconcile). Elle yapılan bir değişiklik git'te yoksa denetleyici onu geri alır — drift kendiliğinden kapanır. Ayrıca her değişiklik commit/PR üzerinden gözden geçirilir, denetlenebilir (audit) ve `git revert` ile **geri alınabilir** olur. GitOps bildirimseldir (A yanlış), amacı CI hızı (B) veya erişimi açmak (C) değildir.

# cicd-3 | İleri
## Soru
Bir ekip yeni "öneri motoru"nu üretime dağıtmak istiyor ama tüm kullanıcılara açmadan önce küçük bir kesimde davranışını görmek istiyor. Deploy'u release'ten ayıran hangi teknik bunu sağlar?
## A
Feature flag ile kodu dağıtımdan ayırıp çalışma zamanında yüzde bazlı açmak
🤖 Doğru: kod üretimde ama kapalı; bayrakla kademeli açılır, gerekirse anında kapatılır — deploy ≠ release.
## B
Sürümü doğrudan tüm kullanıcılara açıp hata bildirimlerini yakından izlemek
🤖 Bu big-bang release'tir; kademeli açmanın ve hızlı geri almanın sağladığı güvenlik ağı yoktur.
## C
Her kullanıcı grubu için ayrı bir üretim ortamı kurup oraya dağıtım yapmak
🤖 Grup başına ortam ölçeklenmez ve gereksizdir; bayrak/canary aynı ortamda kademeyi sağlar.
## D
Kodu yalnız stage ortamında tutup üretime hiç çıkarmadan gözlemlemek
🤖 Amaç üretimde gerçek kesimde görmek; stage'de tutmak gerçek kullanıcı davranışını vermez.
## Doğru
A
## Açıklama
**Feature flag** (özellik bayrağı), **dağıtımı (deploy) yayınlamadan (release) ayıran** temel tekniktir: kod üretime çıkar ama bayrak **kapalıdır**; sonra çalışma zamanında kullanıcıların %1'ine, %5'ine... **kademeli** açılır (progressive delivery). Metrikler kötüyse yeniden dağıtım yapmadan bayrak **anında kapatılır** — geri alma saniyeler sürer. Canary dağıtımlar da benzer amaca trafik yönlendirmeyle hizmet eder. Big-bang release (B) bu güvenlik ağından yoksundur; kullanıcı başına ortam (C) veya stage'de tutmak (D) gerçek üretim davranışını kademeli görme ihtiyacını karşılamaz.

# cicd-4 | İleri
## Soru
Yaygın kullanılan bir günlükleme kütüphanesinde kritik bir CVE açıklandı. Yüzlerce servisin hangilerinin bu sürümü içerdiğini **dakikalar içinde** yanıtlayabilmek için tedarik zinciri açısından en değerli uygulama nedir?
## A
Her servisi tek tek klonlayıp bağımlılık ağacını elle inceleyip not almak
🤖 Yüzlerce serviste elle inceleme saatler/günler alır; SBOM bunu sorgulanabilir hale getirir.
## B
Yalnız üretim sunucularını sırayla yeniden başlatıp durumu izlemek
🤖 Yeniden başlatma zafiyetli sürümü değiştirmez; önce nerede olduğunu bilmek gerekir.
## C
Her yapı için SBOM üretip saklamak, CVE çıkınca bu envanteri sorgulamak
🤖 Doğru: SBOM tüm bileşen/sürümleri makine-okunur listeler; "kim etkilendi?" sorgusu saniyeler sürer.
## D
Bağımlılık sürümlerini sabitlemeyip her yapının en güncelini çekmesini sağlamak
🤖 Sabitlemeyi bırakmak yapıları belirsiz ve tekrarlanamaz kılar; güvenliği azaltır, envanteri de vermez.
## Doğru
C
## Açıklama
Bir CVE çıktığında ilk ve en pahalı soru "**biz nerede etkilendik?**"tir. **SBOM** (Software Bill of Materials) her yapının içerdiği tüm bileşenleri ve **kesin sürümlerini** makine-okunur biçimde (CycloneDX/SPDX) listeler; bu envanter saklandığında, yeni bir CVE için tüm SBOM'lar saniyeler içinde sorgulanıp etkilenen servisler listelenir. Elle inceleme (A) yüzlerce serviste ölçeklenmez. Yeniden başlatma (B) zafiyeti gidermez. Bağımlılıkları **sabitlememek** (D) tam tersine yapıları tekrarlanamaz ve öngörülemez kılar — SBOM ile birlikte sürümler **sabitlenmeli** (pin) ve imzalanmalıdır. SBOM, tedarik zinciri güvenliğinin temel görünürlük aracıdır.

# cicd-5 | İleri
## Soru
IaC (Infrastructure as Code) ile altyapıyı Terraform gibi araçlarla kod olarak yönetmenin, konsoldan elle kaynak oluşturmaya göre temel avantajı nedir?
## A
Elle oluşturma daha güvenlidir çünkü insan her adımı tek tek denetleyerek ilerler
🤖 Elle adımlar tekrarlanamaz ve kayıt dışıdır; denetim aslında kod incelemesiyle (PR) daha güçlüdür.
## B
Altyapı sürümlenebilir, gözden geçirilebilir, tekrarlanabilir ve geri alınabilir olur
🤖 Doğru: kod olarak altyapı kod disiplinini (versiyon, review, tekrar) altyapıya taşır.
## C
IaC yalnız bulut sağlayıcı kilitlenmesini (lock-in) tamamen ortadan kaldırmak içindir
🤖 Lock-in ayrı bir konudur ve IaC onu tümüyle çözmez; asıl kazanç tekrarlanabilir, denetlenebilir altyapıdır.
## D
IaC uygulamanın çalışma zamanı performansını ve kaynak verimliliğini artırır
🤖 IaC sağlama (provisioning) sürecidir; çalışma zamanı performansıyla doğrudan ilgisi yoktur.
## Doğru
B
## Açıklama
**IaC**, altyapıyı (ağ, sunucu, veritabanı, kuyruk) **bildirimsel kod** olarak tanımlar; böylece yazılım geliştirmenin disiplinini altyapıya taşır: değişiklikler **sürümlenir** (git), **gözden geçirilir** (PR review), **tekrarlanabilir** biçimde uygulanır ve gerektiğinde **geri alınır**. Aynı tanımdan dev/stage/prod ortamları tutarlı kurulur — "elle kurduğumda farklı çıktı" sınıfı sorunlar kaybolur. Elle konsol işlemleri (A) kayıt dışıdır, tekrarlanamaz ve drift üretir. Lock-in (C) IaC'nin birincil amacı değildir ve tümüyle çözülmez; IaC bir provisioning aracıdır, çalışma zamanı performansı (D) onun kapsamı dışındadır.

# cicd-6 | İleri
## Soru
Bir CI boru hattı testleri geçen her commit'i otomatik üretime çıkarıyor (continuous deployment) ama hiç ilerlemeli (progressive) yayın, izleme veya otomatik geri alma yok. Bu kurulumun temel riski nedir?
## A
Risk yok; testler geçtiyse üretim güvendedir ve ek bir önlem gereksizdir
🤖 Testler her regresyonu yakalayamaz; izleme ve kademeli açma olmadan hatalı sürüm tüm kullanıcıya anında ulaşır.
## B
CI boru hattı zamanla çok yavaşlar ve bunun sonucunda dağıtım sıklığı düşer
🤖 Sorun hız değil; eksik olan güvenlik ağı — kademeli açma, izleme ve hızlı geri alma.
## C
Testlerin yakalayamadığı hata anında %100 kullanıcıya ulaşır; güvenlik ağı eksik
🤖 Doğru: hızlı teslimat ancak gözlemleme ve kademeli yayınla güvenli olur; aksi halde her hata tam etkiyle çıkar.
## D
Continuous deployment bırakılıp yılda bir kez büyük sürüme dönülmelidir
🤖 Seyrek sürüm DORA'ya aykırıdır ve riski artırır; çözüm sürati bırakmak değil, güvenlik ağı eklemek.
## Doğru
C
## Açıklama
Otomatik continuous deployment değerlidir ama **testler her hatayı yakalayamaz** — özellikle üretim verisi, yük ve yapılandırmaya bağlı sorunları. Kademeli açma (canary/feature flag), **üretim izleme** (hata oranı, gecikme, iş metrikleri) ve **otomatik geri alma** olmadan, testlerin kaçırdığı bir kusur **anında %100 kullanıcıya** ulaşır ve fark edilene kadar zarar birikir. Doğru yön hızı **bırakmak değil** (D yanlış), sürate bir **güvenlik ağı** eklemektir: önce %1'e ver, metrikleri izle, kötüyse otomatik geri al. "Testler geçti = güvenli" (A) yanlış bir güvendir; sorun CI hızı (B) değildir.
