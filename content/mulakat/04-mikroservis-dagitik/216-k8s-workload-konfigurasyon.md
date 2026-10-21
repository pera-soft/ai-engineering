---
slug: k8s-workload-konfigurasyon
baslik: "Kubernetes İş Yükleri: Deployment, StatefulSet, Job/CronJob ve Yapılandırma Yönetimi"
kademe: İleri
sira: 216
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir bankanın kadro planını düşün. Gişedeki on memur birbirinin aynıdır: müşteri hangisine giderse gitsin işi görülür, biri izne çıkarsa yerine bir başkası oturur; kimin oturduğu hiç önemli değildir. Kasa dairesindeki görevliler ise öyle değildir. Her birinin numaralı kendi çekmecesi vardır. Sabah "kasa 1" açılmadan "kasa 2" açılmaz. Görevli değişse bile "kasa 2'nin görevlisi" kimliği ve o çekmece yerinde kalır. Her katta bir güvenlik görevlisi vardır — kat sayısı artarsa görevli sayısı da artar, iş yükü değil bina belirler. Gün sonunda bir de tek seferlik iş yapılır: mutabakat alınır, biter, dağılır; ve bu iş her gece belli bir saatte tekrarlanır. Kubernetes'in **iş yükü kontrolcüleri (workload controller)** tam olarak bu dört kadro tipidir: **Deployment**, **StatefulSet**, **DaemonSet** ve **Job/CronJob**. Prosedür kitapçığı ile kasa şifresi ise memurun üniformasına dikilmez; dışarıdan verilir — Kubernetes'te bunlar **ConfigMap** ve **Secret**'tır.

### Temel kavramlar

- **Pod → ReplicaSet → Deployment zinciri:** Pod en küçük çalıştırma birimidir ama tek başına yönetilmez. **ReplicaSet** belirli bir pod şablonundan kaç kopya (replika) olacağını garanti eder. **Deployment** ise ReplicaSet'lerin üstündeki katmandır: pod şablonu her değiştiğinde **yeni bir ReplicaSet** oluşturur, eskisini kademeli olarak küçültür ve yenisini büyütür. Eski ReplicaSet silinmez; `revisionHistoryLimit` (varsayılan 10) kadarı **revizyon geçmişi** olarak saklanır. `kubectl rollout undo` bir önceki revizyona **geri alma (rollback)** yaparken tam olarak bu saklanan ReplicaSet'i tekrar büyütür.
- **StatefulSet:** Kopyaların birbirinin aynı olmadığı, **kimliği olan** iş yükleri içindir. Üç şey verir. (1) **Kararlı ad ve kimlik:** pod'lar rastgele son ekle değil, `ad-0`, `ad-1`, `ad-2` diye sıralı adlandırılır. Yeniden yaratılan pod aynı adı geri alır. (2) **Sıralı başlatma ve sonlandırma:** varsayılan `OrderedReady` ilkesinde `ad-1`, ancak `ad-0` hazır olduktan sonra başlar; kapanış ters sırada olur. (3) **`volumeClaimTemplates`:** her pod için ayrı bir kalıcı disk talebi (PVC — PersistentVolumeClaim) üretilir ve `ad-0` hep kendi diskine bağlanır. Kararlı DNS adı için yanına bir **başsız (headless) Service** konur. Ölçek küçültüldüğünde PVC'ler varsayılan olarak silinmez; veri kazara kaybolmasın diye yerinde durur.
- **DaemonSet:** Uygun her **düğümde (node)** bir pod çalıştırır. Kopya sayısını sen değil, düğüm sayısı belirler; yeni düğüm eklenince pod kendiliğinden oraya da düşer. Tipik kullanım: log toplayıcı ajanı, metrik ajanı, ağ eklentisi.
- **Job:** Başlayıp **biten** iş içindir. `completions` kaç başarılı tamamlanma istendiğini, `parallelism` ise aynı anda kaç pod koşacağını söyler. **`backoffLimit`** (varsayılan 6) başarısız denemenin kaç kez tekrarlanacağını belirler; limit dolunca Job "başarısız" damgası yer. Job pod'unda `restartPolicy` yalnız `OnFailure` veya `Never` olabilir — `Always` geçersizdir, çünkü biten bir iş sonsuza kadar yeniden başlatılamaz. `ttlSecondsAfterFinished` biten Job'ları otomatik temizler.
- **CronJob:** Job'u **takvime** bağlar. `schedule` alanı cron biçimindedir, `timeZone` ile saat dilimi verilebilir. En kritik alan **`concurrencyPolicy`**'dir: `Allow` (varsayılan) önceki çalıştırma sürerken yenisini de başlatır — **çift işleme** kaynağıdır; `Forbid` önceki bitmediyse yeni çalıştırmayı atlar; `Replace` çalışanı öldürüp yenisini başlatır. `startingDeadlineSeconds` gecikmiş tetiklemenin ne kadar süreyle hâlâ geçerli sayılacağını, `suspend` ise zamanlamayı geçici durdurmayı sağlar.
- **Yuvarlanan güncelleme (rolling update) parametreleri:** `maxUnavailable` güncelleme sırasında hazır pod sayısının hedefin ne kadar altına düşebileceğini, `maxSurge` hedefin ne kadar üstüne çıkılabileceğini söyler (ikisinin de varsayılanı %25). `maxSurge: 1, maxUnavailable: 0` "önce yeni pod'u hazır et, sonra eskisini sil" demektir: kapasite hiç düşmez ama geçici olarak bir pod'luk fazladan kaynak ister. `maxUnavailable: 1, maxSurge: 0` tersidir: ek kaynak istemez ama güncelleme boyunca kapasite bir eksik çalışır. İkisi birden `0` olamaz — API bunu reddeder, çünkü güncelleme hiç ilerleyemez.
- **`Recreate` stratejisi:** Önce **tüm** eski pod'lar silinir, sonra yenileri başlatılır. Arada kaçınılmaz bir **kesinti penceresi** vardır. Eski ve yeni sürümün aynı anda çalışamayacağı durumlarda (ör. geriye uyumsuz şema değişikliği, tek yazıcıya izin veren disk) bilinçli olarak seçilir.
- **ConfigMap:** Gizli olmayan yapılandırmayı (URL, eşik değer, özellik anahtarı) imajdan ayırır; böylece **aynı imaj** test ve prod'da çalışır.
- **Secret:** Şifre, anahtar, sertifika içindir. Değeri yalnız **base64 ile kodlanır**. Kodlama **şifreleme değildir**: erişebilen herkes tek komutla çözer. Secret'ın ConfigMap'ten birkaç farkı vardır. Ayrı bir nesne tipi olduğu için **rol tabanlı erişim denetimiyle (RBAC)** ayrıca yetkilendirilir; kubelet onu düğümde belleğe (tmpfs) yazar; `kubectl describe` çıktısında değerleri gösterilmez. Ama bunların hiçbiri şifreleme anlamına gelmez. Gerçek koruma üç ayaklıdır: etcd'de **durağan şifreleme (encryption at rest)**, sıkı **RBAC** ve sırların kümede hiç durmadığı **harici sır kasası** (ör. Vault). Kasadaki sır pod'a CSI sürücüsü ya da bir sır operatörü üzerinden bağlanır.
- **Etiket (label) / seçici (selector) / not (annotation):** Etiket, **kimliklendirici** anahtar-değer verisidir ve **sorgulanabilir**. Service, Deployment ve ReplicaSet kendi pod'larını bu etiket **seçicisiyle** bulur. Not (annotation) ise **kimliklendirici olmayan** yardımcı bilgidir: araçların kullandığı üstveri, sürüm notu, içerik özeti (checksum) gibi. Seçicide kullanılamaz.
- **Namespace, ResourceQuota, LimitRange:** **Ad alanı (namespace)** nesneleri **mantıksal olarak** gruplar; tek başına bir güvenlik ya da ağ sınırı değildir. **ResourceQuota**, bir ad alanının toplamda ne kadar CPU/bellek/nesne tüketebileceğine tavan koyar. **LimitRange** ise konteyner başına varsayılan/alt/üst sınır tanımlar. Böylece kaynak talebi yazmayı unutan pod'a otomatik bir varsayılan atanır.
- **Buyurgan (imperative) vs bildirimsel (declarative):** `kubectl run`, `kubectl scale`, `kubectl edit` **buyurgandır** — "şunu şimdi yap" der, sonucu hiçbir dosyada yazmaz. `kubectl apply -f` **bildirimseldir** — istenen durumu dosyada tutar, kümedeki hâl ile karşılaştırıp farkı uygular. Elle yapılan buyurgan müdahaleler kümeyi kaynak dosyalardan uzaklaştırır; buna **sürüklenme (drift)** denir.
- **GitOps, Helm, Kustomize:** GitOps'ta istenen durumun tek kaynağı git deposudur; kümedeki bir **uzlaştırıcı (reconciler)** (Argo CD, Flux) gerçeği sürekli git'e yaklaştırır, elle yapılan değişikliği geri alır. **Helm** paket yöneticisidir: şablon + `values` dosyası ile aynı manifest'i farklı ortamlar için üretir ve sürümü (release) yükseltip geri alır. **Kustomize** şablonsuzdur: taban manifest'in üstüne ortam **katmanı (overlay)** olarak yama uygular ve `kubectl apply -k` ile doğrudan çalışır. İkisi de manifest **üretir**; kümedeki elle değişikliği git'e geri **yazmazlar**.

```svg
<svg viewBox="0 0 660 268" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dört iş yükü kontrolcüsünün karşılaştırması: Deployment, StatefulSet, DaemonSet, Job ve CronJob">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hangi iş yükü kontrolcüsü?</text>
  <rect x="24" y="42" width="300" height="94" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="174" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Deployment</text>
  <text x="174" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kopyalar birbirinin aynı, kimliksiz</text>
  <text x="174" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">durumsuz servis: kredi motoru, API</text>
  <text x="174" y="124" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">rolling update + geri alma</text>
  <rect x="336" y="42" width="300" height="94" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.6"/>
  <text x="486" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#6B3FA0" font-family="system-ui">StatefulSet</text>
  <text x="486" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kararlı ad: ad-0, ad-1, ad-2</text>
  <text x="486" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sıralı başlatma + pod başına PVC</text>
  <text x="486" y="124" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">veritabanı, dağıtık önbellek, kuyruk</text>
  <rect x="24" y="148" width="300" height="94" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="174" y="172" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">DaemonSet</text>
  <text x="174" y="194" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">her düğümde tam bir kopya</text>
  <text x="174" y="212" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sayıyı iş yükü değil düğüm belirler</text>
  <text x="174" y="230" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">log/metrik ajanı, ağ eklentisi</text>
  <rect x="336" y="148" width="300" height="94" rx="12" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="486" y="172" text-anchor="middle" font-size="13" font-weight="700" fill="#B07D20" font-family="system-ui">Job / CronJob</text>
  <text x="486" y="194" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">başlar, işi biter, sona erer</text>
  <text x="486" y="212" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">CronJob = takvime bağlı Job</text>
  <text x="486" y="230" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">gün sonu toplu iş, mutabakat dosyası</text>
  <text x="330" y="260" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Yanlış seçim: biten toplu işi Deployment'a koymak → süreç bitince sürekli yeniden başlatılır</text>
</svg>
```

### Nasıl çalışır?

1. **İstenen durumu bildir.** Bir YAML dosyasına "şu imajdan dört kopya, şu kaynak istekleriyle" yazarsın ve `kubectl apply -f` ile kümeye verirsin. Kubernetes bildirimseldir: sen hedefi yazarsın, kontrolcüler gerçeği sürekli hedefe yaklaştırır.
2. **Kontrolcü zinciri kurulur.** Deployment bir ReplicaSet üretir, ReplicaSet pod'ları yaratır. Pod'lar Deployment'ın seçicisine uyan **etiketleri** taşır; kontrolcü kendi pod'unu bu etiketten tanır.
3. **Şablon değişince yeni revizyon doğar.** İmaj etiketini değiştirdiğinde Deployment **yeni bir ReplicaSet** açar ve `maxSurge`/`maxUnavailable` sınırları içinde yeniyi büyütüp eskiyi küçültür. Sorun çıkarsa `kubectl rollout undo` eski ReplicaSet'i geri büyütür — bu yüzden revizyon geçmişi değerlidir.
4. **Yapılandırma dışarıdan bağlanır.** ConfigMap ve Secret pod'a iki yoldan bağlanabilir: **ortam değişkeni (env)** olarak ya da **disk birimi (volume)** olarak, yani dosya biçiminde. Ortam değişkeni olarak bağlanan değer, pod başlarken **bir kez** enjekte edilir. Disk birimi olarak bağlanan dosyayı ise kubelet belirli aralıklarla tazeler. Bu yüzden değişiklik anında değil, kısa bir gecikmeyle görünür. Üstelik `subPath` ile bağlanan dosya hiç güncellenmez.
5. **Uygulamanın yeni değeri fark etmesi ayrı bir iştir.** Dosya güncellense bile uygulama onu yeniden okumazsa hiçbir şey değişmez. İki yaygın çözüm vardır. Birincisi, ConfigMap'in **içerik özetini (checksum) pod şablonuna bir not (annotation) olarak** koymaktır. İçerik değişince şablon da değişir; yeni ReplicaSet doğar ve yuvarlanan güncelleme pod'ları yeniler. İkincisi, **Spring Cloud Kubernetes**'in yeniden yükleme (reload) desteğiyle uygulamanın ilgili nesnelerini (bean) tazelemektir. En kaba ama en garantili yol `kubectl rollout restart deployment/...` komutudur.
6. **Sınırlar namespace düzeyinde uygulanır.** ResourceQuota namespace toplamına, LimitRange konteyner başına sınır koyar; kotayı aşan yeni pod hiç kabul edilmez.
7. **Toplu iş ayrı yoldan gider.** Gün sonu tahakkuk gibi biten işler CronJob'a bağlanır; `concurrencyPolicy` çakışmayı, `backoffLimit` başarısız denemenin tekrar sayısını yönetir.

```svg
<svg viewBox="0 0 660 268" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="RollingUpdate ile Recreate stratejilerinin karşılaştırması">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Güncelleme stratejisi: kapasite ne olur?</text>
  <rect x="20" y="40" width="308" height="212" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="174" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">RollingUpdate · maxSurge 1 · maxUnavailable 0</text>
  <text x="40" y="86" font-size="10.5" fill="#5D7789" font-family="system-ui">başlangıç: 4 hazır pod (v1)</text>
  <rect x="40" y="94" width="36" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="58" y="109" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">v1</text>
  <rect x="82" y="94" width="36" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="100" y="109" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">v1</text>
  <rect x="124" y="94" width="36" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="142" y="109" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">v1</text>
  <rect x="166" y="94" width="36" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="184" y="109" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">v1</text>
  <text x="40" y="140" font-size="10.5" fill="#5D7789" font-family="system-ui">geçiş: önce 5. pod hazır olur, sonra biri silinir</text>
  <rect x="40" y="148" width="36" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="58" y="163" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">v1</text>
  <rect x="82" y="148" width="36" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="100" y="163" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">v1</text>
  <rect x="124" y="148" width="36" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="142" y="163" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">v1</text>
  <rect x="166" y="148" width="36" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="184" y="163" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">v1</text>
  <rect x="208" y="148" width="36" height="22" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="226" y="163" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">v2</text>
  <text x="40" y="194" font-size="10.5" fill="#5D7789" font-family="system-ui">bitiş: 4 hazır pod (v2)</text>
  <rect x="40" y="202" width="36" height="22" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="58" y="217" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">v2</text>
  <rect x="82" y="202" width="36" height="22" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="100" y="217" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">v2</text>
  <rect x="124" y="202" width="36" height="22" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="142" y="217" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">v2</text>
  <rect x="166" y="202" width="36" height="22" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="184" y="217" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">v2</text>
  <text x="174" y="244" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">hazır pod sayısı hiç 4'ün altına inmedi</text>
  <rect x="332" y="40" width="308" height="212" rx="12" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="486" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">Recreate</text>
  <text x="352" y="86" font-size="10.5" fill="#5D7789" font-family="system-ui">adım 1: TÜM v1 pod'ları silinir</text>
  <rect x="352" y="94" width="36" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3" opacity="0.45"/>
  <text x="370" y="109" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">v1</text>
  <rect x="394" y="94" width="36" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3" opacity="0.45"/>
  <text x="412" y="109" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">v1</text>
  <rect x="436" y="94" width="36" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3" opacity="0.45"/>
  <text x="454" y="109" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">v1</text>
  <rect x="478" y="94" width="36" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3" opacity="0.45"/>
  <text x="496" y="109" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">v1</text>
  <text x="352" y="140" font-size="10.5" fill="#5D7789" font-family="system-ui">adım 2: hiç hazır pod yok</text>
  <rect x="352" y="148" width="204" height="22" rx="5" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6" stroke-dasharray="5 3"/>
  <text x="454" y="163" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">KESİNTİ PENCERESİ</text>
  <text x="352" y="194" font-size="10.5" fill="#5D7789" font-family="system-ui">adım 3: 4 pod (v2) sıfırdan başlar</text>
  <rect x="352" y="202" width="36" height="22" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="370" y="217" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">v2</text>
  <rect x="394" y="202" width="36" height="22" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="412" y="217" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">v2</text>
  <rect x="436" y="202" width="36" height="22" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="454" y="217" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">v2</text>
  <rect x="478" y="202" width="36" height="22" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="496" y="217" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">v2</text>
  <text x="486" y="244" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">iki sürüm asla yan yana çalışmaz — kesinti bedeli</text>
</svg>
```

```svg
<svg viewBox="0 0 660 268" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ConfigMap ortam değişkeni ve volume olarak bağlandığında güncelleme davranışı">
  <defs>
    <marker id="k8swl-cm-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">ConfigMap değişti — pod bunu ne zaman görür?</text>
  <rect x="20" y="66" width="132" height="118" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.6"/>
  <text x="86" y="112" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">ConfigMap</text>
  <text x="86" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">/ Secret</text>
  <text x="86" y="156" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">güncellendi</text>
  <rect x="196" y="52" width="184" height="72" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="288" y="76" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">env değişkeni olarak</text>
  <text x="288" y="96" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">pod başlarken bir kez</text>
  <text x="288" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">enjekte edilir</text>
  <rect x="196" y="140" width="184" height="72" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="288" y="164" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">volume (dosya) olarak</text>
  <text x="288" y="184" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kubelet dosyayı periyodik</text>
  <text x="288" y="200" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">olarak tazeler (gecikmeli)</text>
  <rect x="424" y="52" width="212" height="72" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="530" y="80" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">pod yeniden yaratılmadan</text>
  <text x="530" y="98" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">ASLA değişmez</text>
  <text x="530" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">eski değerle çalışmaya devam eder</text>
  <rect x="424" y="140" width="212" height="72" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="530" y="164" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">dosya yenilenir ama</text>
  <text x="530" y="182" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">uygulama okumazsa etkisiz</text>
  <text x="530" y="200" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">subPath ile bağlıysa hiç yenilenmez</text>
  <path d="M152 100 L194 88" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#k8swl-cm-arw)"/>
  <path d="M152 150 L194 176" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#k8swl-cm-arw)"/>
  <path d="M382 88 L422 88" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#k8swl-cm-arw)"/>
  <path d="M382 176 L422 176" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#k8swl-cm-arw)"/>
  <rect x="20" y="222" width="616" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="330" y="244" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Çözüm: pod şablonuna içerik özeti (checksum) annotation'ı koy → yeni rollout · ya da Spring Cloud Kubernetes reload · en kaba yol: kubectl rollout restart</text>
</svg>
```

### Mülakatta ne sorulur / tuzaklar

- **"Deployment yerine ne zaman StatefulSet?"** Beklenen cevap üç maddedir: kararlı ad/kimlik, sıralı başlatma-sonlandırma, pod başına ayrı PVC (`volumeClaimTemplates`). "Kalıcı disk lazımsa StatefulSet" demek eksik bir cevaptır — Deployment da tek bir PVC'ye bağlanabilir; asıl fark kopyaların **birbirinden ayırt edilebilir olması**dır. Ayrıca StatefulSet'e kararlı DNS için başsız Service eşlik eder.
- **"Gün sonu toplu işini nasıl çalıştırırsın?"** Doğru araç CronJob'dur. En sık düşülen tuzak `concurrencyPolicy`'yi varsayılan (`Allow`) bırakmaktır: iş beklenenden uzun sürünce bir sonraki tetikleme başlar, aynı kayıtlar iki kez işlenir ve para hareketinde **çift kayıt** doğar. Para işinde genellikle `Forbid` seçilir; `Replace` çalışan işi ortasında öldürdüğü için yarım kalmış tahakkuk riski taşır. Ayrıca toplu işin kendisi de **tekrar edilse de sonucu değişmeyen (idempotent)** biçimde yazılmalıdır; zamanlayıcı "tam bir kez" garantisi vermez.
- **"ConfigMap'i güncelledim, pod niye eski değeri kullanıyor?"** Ortam değişkeni olarak bağlanan değer pod ömrü boyunca sabittir; volume olarak bağlanan dosya tazelenir ama uygulama yeniden okumazsa fark etmez. Yanıtta checksum annotation'ı, `rollout restart` ve Spring Cloud Kubernetes reload'ından en az birini söylemelisin.
- **"Secret güvenli mi?"** "Secret kullandık, şifre artık şifreli" demek elenme sebebidir. Base64 **kodlamadır**, şifreleme değildir. Doğru cevap: etcd durağan şifreleme + RBAC + harici sır kasası.
- **`maxSurge`/`maxUnavailable`.** Kapasitenin hiç düşmemesi isteniyorsa `maxUnavailable: 0` ve `maxSurge` en az 1 olmalıdır; ikisini birden 0 yapmak API tarafından reddedilir. `Recreate` ise bilinçli kesinti demektir.
- **Sürüklenme (drift).** GitOps kurulu bir kümede `kubectl edit` ile yapılan müdahale kalıcı olmaz; uzlaştırıcı kümeyi git'teki hâle geri çeker. Doğru refleks, değişikliği manifest'e (Helm `values` ya da Kustomize katmanı) yazmaktır.

### Tek cümlede

Kimliksiz kopyalar için Deployment, kimlikli ve diskli kopyalar için StatefulSet, düğüm başına ajan için DaemonSet, biten iş için Job/CronJob kullanılır. Yapılandırma ConfigMap/Secret ile dışarıdan bağlanır; ama ortam değişkeni olarak bağlanan değer pod yenilenmeden değişmez ve Secret'ın base64'ü şifreleme değildir.

# k8swl-1 | İleri
## Soru
Kredi kartı hareketlerini tutan dağıtık bir veri düğümü kümesini Kubernetes'e taşıyorsun. Her kopyanın kendi kalıcı diskine sahip olması, yeniden yaratıldığında aynı diske ve aynı ağ kimliğine geri dönmesi, kopyaların da sırayla ayağa kalkması gerekiyor. Hangi kontrolcü doğrudur ve neden?
## A
Deployment kullanmak ve tüm replikaları tek bir paylaşılan kalıcı disk talebine (PVC) bağlamak
🤖 paylaşılan tek disk kopyalara ayrı kimlik vermez, çoğu depolama sınıfı zaten tek düğüme bağlanmaya izin verir ve Deployment pod adlarını her yeniden yaratımda rastgele değiştirir.
## B
DaemonSet kullanmak; her düğümde bir kopya çalıştığı için kimlik düğüm adından gelir ve disk düğüme sabitlenir
🤖 DaemonSet'in amacı düğüm başına ajan çalıştırmaktır, kopya sayısını iş yükü değil düğüm sayısı belirler; sıralı başlatma ve pod başına PVC şablonu sunmaz.
## C
StatefulSet kullanmak; sıralı adlar, başsız Service ile kararlı DNS, `volumeClaimTemplates` ile pod başına ayrı PVC ve sıralı başlatma-sonlandırma sağlar
🤖 Doğru: StatefulSet'in tanımı tam olarak kararlı kimlik, sıralı yaşam döngüsü ve pod başına kalıcı disk üçlüsüdür.
## D
Deployment kullanıp bir init container ile pod adını sabitlemek ve diski o ada göre bağlamak
🤖 pod adını Deployment'ın ReplicaSet'i üretir ve rastgele son ek taşır; init container bu adı değiştiremeyeceği için disk-pod eşleşmesi kurulamaz.
## Doğru
C
## Açıklama
**StatefulSet** tam olarak bu üç ihtiyaç için vardır: (1) **kararlı ad ve kimlik** — pod'lar `ad-0`, `ad-1` biçiminde sıralı adlandırılır ve yeniden yaratılan pod aynı adı geri alır; (2) **sıralı başlatma ve sonlandırma** — varsayılan `OrderedReady` ilkesinde bir sonraki pod, öncekinin hazır olmasını bekler, kapanış ters sırada olur; (3) **`volumeClaimTemplates`** — her pod için ayrı bir PVC üretilir ve `ad-0` her açılışta kendi diskine geri bağlanır. Kararlı DNS için yanına başsız (headless) Service konur. Tek paylaşılan PVC'li Deployment bunların hiçbirini vermez: kopyalar ayırt edilemez, pod adları rastgele son ek taşır ve tek disk çoğu depolama sınıfında zaten tek düğüme bağlanabilir. DaemonSet farklı bir soruya cevap verir — "her düğümde bir tane" der, kopya sayısını düğüm sayısı belirler ve ne sıralı başlatma ne de pod başına disk şablonu sunar. Pod adını init container ile sabitleme fikri de işlemez: adı ReplicaSet üretir, konteyner içinden değiştirilemez. Dikkat: StatefulSet ölçek küçültüldüğünde PVC'ler veri kazara kaybolmasın diye varsayılan olarak silinmez.

# k8swl-2 | Orta
## Soru
Kredi limiti güncelleme toplu işi her 15 dakikada bir CronJob olarak çalışıyor. Yoğun günlerde bir çalıştırma 25 dakika sürüyor, bu sırada bir sonraki tetikleme başlıyor ve aynı başvurular iki kez işlenerek çift limit düşümü oluyor. Kubernetes tarafında ilk yapılacak doğru ayar hangisidir?
## A
CronJob'a `concurrencyPolicy: Forbid` vermek; önceki Job hâlâ çalışıyorsa yeni tetikleme atlanır
🤖 Doğru: varsayılan `Allow` çakışan çalıştırmalara izin verir, `Forbid` ise önceki bitmeden yenisinin başlamasını engeller.
## B
Job'a `backoffLimit: 0` vererek başarısız denemenin tekrar edilmesini engellemek
🤖 `backoffLimit` yalnız başarısız pod'un kaç kez yeniden deneneceğini sınırlar; çakışan çalıştırmalarla hiçbir ilgisi yoktur.
## C
Job pod'una `restartPolicy: Always` vererek çalışmanın kesintisiz sürmesini sağlamak
🤖 Job pod'unda `Always` geçersiz bir değerdir (yalnız `OnFailure` veya `Never` kullanılır) ve zaten eşzamanlılığı etkilemez.
## D
CronJob'u Deployment'a çevirip `replicas: 1` vermek; tek kopya olduğu için iş asla çakışmaz
🤖 Deployment sürekli çalışan iş yükleri içindir, biten süreci sonsuza kadar yeniden başlatır ve zamanlama garantisi sunmaz.
## Doğru
A
## Açıklama
Çakışma sorununun doğrudan karşılığı CronJob'un **`concurrencyPolicy`** alanıdır. Varsayılan değer **`Allow`**'dur: takvim geldiğinde önceki çalıştırma sürse bile yeni Job başlatılır — bu, aynı kayıtların iki kez işlenmesinin ve para hareketinde **çift kayıt** oluşmasının klasik sebebidir. **`Forbid`** önceki Job bitmeden yeni tetiklemeyi atlar ve bu senaryodaki doğru seçimdir; üçüncü seçenek `Replace` çalışan Job'u öldürüp yenisini başlattığı için yarım kalmış tahakkuk riski taşır, para işinde tercih edilmez. `backoffLimit` bambaşka bir alandır: bir Job'un **başarısız** pod'unu kaç kez yeniden deneyeceğini söyler (varsayılan 6), eşzamanlılığı hiç ilgilendirmez. Job pod'unda `restartPolicy: Always` ise API tarafından kabul edilmeyen bir değerdir; biten bir işin sonsuza kadar yeniden başlatılması anlamsız olacağı için yalnız `OnFailure` ve `Never` geçerlidir. Toplu işi Deployment'a çevirmek de yanlış araç seçimidir: Deployment sürekli çalışan servisler içindir, süreç bitince pod'u sürekli yeniden başlatır ve takvim kavramı yoktur. Son not: Kubernetes zamanlaması "tam bir kez" garantisi vermediği için toplu işin kendisi de **idempotent** yazılmalı, aynı başvuru ikinci kez görüldüğünde limit tekrar düşülmemelidir.

# k8swl-3 | İleri
## Soru
Faiz eşiği değerini ConfigMap'ten **ortam değişkeni (env)** olarak okuyan bir servis var. ConfigMap `kubectl apply` ile güncellendi, `kubectl get configmap` yeni değeri gösteriyor, ama pod'lar hâlâ eski eşikle çalışıyor. Doğru açıklama ve çözüm hangisidir?
## A
ConfigMap güncellemesi kubelet tarafından anında tüm pod'lara yayılır; sorun yalnızca uygulamanın kendi bellek içi önbelleğidir, önbelleği kapatmak yeter
🤖 env olarak bağlanan değer pod'a hiç yeniden yayılmaz; sorun uygulama önbelleği değil, enjeksiyonun yalnız pod başlangıcında yapılmasıdır.
## B
ConfigMap'i Secret'a çevirmek sorunu çözer, çünkü Secret olarak bağlanan değerleri kubelet anında tazeler
🤖 Secret'ın tazelenme davranışı ConfigMap ile aynıdır; env olarak bağlandığında ikisi de pod yeniden yaratılmadan değişmez.
## C
ConfigMap'i volume olarak bağlamak tek başına sorunu çözer, çünkü dosya güncellenince uygulama otomatik olarak yeni değeri kullanmaya başlar
🤖 volume olarak bağlı dosya gecikmeli de olsa tazelenir ama uygulama onu yeniden okumazsa değer değişmez; üstelik `subPath` ile bağlanan dosya hiç tazelenmez.
## D
Ortam değişkeni pod başlarken bir kez enjekte edilir ve pod yeniden yaratılmadan değişmez; çözüm pod şablonuna ConfigMap içeriğinin özetini (checksum) annotation olarak koyup yeni bir rollout tetiklemektir
🤖 Doğru: env değerini değiştirmenin tek yolu pod'u yenilemektir; şablon özeti değişince Deployment yeni ReplicaSet açar ve yuvarlanan güncelleme pod'ları tazeler.
## Doğru
D
## Açıklama
ConfigMap ve Secret'ın pod'a **nasıl bağlandığı** güncelleme davranışını belirler. **Ortam değişkeni (env)** olarak bağlanan değer, konteyner süreci başlarken bir kez enjekte edilir; sonrasında ConfigMap değişse bile sürecin ortamı değişmez — dolayısıyla "kubelet anında yayar, sorun uygulama önbelleğidir" açıklaması yanlıştır. Aynı sebeple ConfigMap'i **Secret'a çevirmek** hiçbir şey değiştirmez: Secret ayrı bir nesne tipidir ve RBAC açısından ayrı yönetilir, ama tazelenme davranışı ConfigMap ile aynıdır. **Volume** olarak bağlamak resmin yarısını çözer: kubelet dosyayı periyodik olarak tazeler (bu yüzden değişiklik anında değil kısa bir gecikmeyle görünür), fakat uygulama dosyayı yeniden okumadıkça değer yine değişmez; ayrıca `subPath` ile bağlanan dosya hiç güncellenmez. Pratikte üç çözüm kullanılır: (1) pod şablonuna ConfigMap içeriğinin **özetini (checksum) annotation** olarak koymak — içerik değişince şablon değişir, Deployment yeni ReplicaSet açar ve yuvarlanan güncelleme pod'ları yeniler; (2) **Spring Cloud Kubernetes**'in yeniden yükleme (reload) desteğiyle ilgili bean'leri tazelemek; (3) en kaba ama en garantili yol olarak `kubectl rollout restart deployment/...` çalıştırmak.

# k8swl-4 | Orta
## Soru
Bir ekip arkadaşın "veritabanı şifresini Kubernetes Secret'ına koyduk, artık şifreli" diyor. Bu ifadenin nesi yanlıştır?
## A
Secret'lar gerçekten şifrelidir, ancak şifreleme yalnız aynı namespace içinde geçerlidir; başka namespace'ten okunursa düz metin döner
🤖 böyle bir namespace'e bağlı şifreleme mekanizması yoktur; namespace erişim yetkisiyle ilgilidir, verinin şifreliliğiyle değil.
## B
Secret'ın değeri yalnız base64 ile **kodlanır**; kodlama şifreleme değildir ve erişebilen herkes tek komutla çözer — gerçek koruma için etcd'de durağan şifreleme, sıkı RBAC ve harici sır kasası gerekir
🤖 Doğru: base64 gizlilik sağlamayan bir gösterim biçimidir; koruma etcd şifrelemesi, yetkilendirme ve harici kasa ile kurulur.
## C
Secret ile ConfigMap arasında hiçbir fark yoktur; ikisi de tamamen aynı nesnedir, sadece adları farklıdır
🤖 farklar vardır (ayrı nesne tipi olduğu için RBAC ile ayrı yetkilendirilir, kubelet düğümde belleğe yazar, `describe` çıktısında değer gizlenir) ama bu farklar şifreleme anlamına gelmez.
## D
base64 güçlü bir şifreleme algoritmasıdır; çözme anahtarı kube-apiserver'da tutulduğu için anahtarsız okunamaz
🤖 base64 bir kodlamadır, anahtarı yoktur ve herkes tarafından çözülebilir.
## Doğru
B
## Açıklama
Kubernetes **Secret**'ının içeriği **base64** ile kodlanır. Base64 bir **gösterim biçimidir**, şifreleme değildir: anahtarı yoktur, herkes tek komutla çözer — dolayısıyla "güçlü algoritma, anahtarı apiserver'da" iddiası tümüyle yanlıştır. "Şifreleme namespace'e bağlıdır" diye bir mekanizma da yoktur; namespace nesneleri mantıksal olarak gruplar, veriyi kriptografik olarak korumaz. Öte yandan Secret ile ConfigMap'in "hiçbir farkı yok" demek de doğru değildir: Secret ayrı bir nesne tipi olduğu için **RBAC** ile ayrı yetkilendirilir, kubelet düğümde onu diske kalıcı yazmak yerine bellekte tutar ve `kubectl describe` çıktısında değerler gösterilmez. Ama bu farkların hiçbiri şifreleme sağlamaz. Gerçek koruma üç ayaklıdır: (1) etcd'de **durağan şifreleme (encryption at rest)** yapılandırmak; (2) Secret okuma yetkisini **RBAC** ile gerçekten kısıtlamak — çünkü namespace'te Secret okuyabilen herkes şifreyi görür; (3) sırrı kümede hiç tutmayıp **harici bir sır kasasından** (ör. Vault) CSI sürücüsü veya sır operatörü aracılığıyla çalışma anında bağlamak. Ayrıca Secret'ı git deposundaki manifest içinde düz base64 olarak tutmak, onu tüm depoya erişen herkese açmak demektir.

# k8swl-5 | İleri
## Soru
Kredi başvuru servisi 4 replika ile çalışıyor. Yeni sürüme geçerken hazır pod sayısının **hiçbir an** 4'ün altına düşmemesi isteniyor; kümede geçici olarak bir pod'luk fazladan kaynak ayırmak sorun değil. Deployment stratejisi nasıl ayarlanmalıdır?
## A
`maxUnavailable: 1`, `maxSurge: 0`
🤖 bu ayar önce bir eski pod'u siler; güncelleme boyunca hazır pod sayısı 3'e düşer, oysa kapasitenin düşmemesi isteniyor.
## B
`strategy: Recreate`
🤖 tüm eski pod'lar silinip sonra yenileri başlatıldığı için arada hiç hazır pod bulunmayan bir kesinti penceresi oluşur.
## C
`maxSurge: 1`, `maxUnavailable: 0`
🤖 Doğru: önce fazladan bir yeni pod hazır olur, ancak ondan sonra bir eski pod silinir; hazır pod sayısı hiç 4'ün altına inmez.
## D
`maxSurge: 0`, `maxUnavailable: 0`
🤖 ikisi birden sıfır olamaz; bu kombinasyonda güncelleme hiç ilerleyemeyeceği için API isteği reddeder.
## Doğru
C
## Açıklama
Yuvarlanan güncellemede iki parametre kapasiteyi belirler. **`maxUnavailable`** hazır pod sayısının hedefin ne kadar **altına** inebileceğini, **`maxSurge`** ise hedefin ne kadar **üstüne** çıkılabileceğini söyler; ikisinin de varsayılanı %25'tir. Kapasitenin hiç düşmemesi isteniyorsa `maxUnavailable` **0** olmalı, `maxSurge` ise en az 1 olmalıdır: Kubernetes önce fazladan bir yeni pod'u ayağa kaldırıp hazır olmasını bekler, ancak ondan sonra bir eski pod'u siler — böylece hazır sayı hiç 4'ün altına inmez, bedeli geçici bir pod'luk ek kaynaktır. `maxUnavailable: 1, maxSurge: 0` bunun tersidir: ek kaynak istemez ama önce silip sonra eklediği için kapasite bir eksik çalışır. `Recreate` stratejisi tüm eski pod'ları silip sonra yenilerini başlatır; iki sürümün asla yan yana çalışamayacağı durumlarda (geriye uyumsuz şema değişikliği gibi) bilinçle seçilir ama tanımı gereği bir **kesinti penceresi** yaratır. `maxSurge` ve `maxUnavailable`'ın ikisini birden 0 yapmak ise geçersizdir: ne yeni pod eklenebilir ne de eski silinebilir, güncelleme ilerleyemez, bu yüzden API bu kombinasyonu reddeder. Not: `maxUnavailable: 0` yalnız readiness probe doğru kurulmuşsa gerçekten koruma sağlar — "hazır" kararını veren odur.

# k8swl-6 | Uzman
## Soru
Prod kümesinde acil bir sorunu gidermek için `kubectl edit deployment` ile replika sayısı ve imaj etiketi elle değiştirildi. Kümede Argo CD ile GitOps kurulu ve manifest'ler Kustomize katmanları hâlinde git'te tutuluyor. Ne olur ve doğru yaklaşım nedir?
## A
Elle yapılan değişiklik git'in dışındadır; uzlaştırıcı (reconciler) kümeyi git'teki hâle geri çeker ve değişiklik kaybolur (sürüklenme/drift) — doğru yol, değişikliği git'teki manifest'e yazmaktır
🤖 Doğru: GitOps'ta tek gerçek kaynak git'tir; uzlaştırıcı farkı görüp kümeyi sürekli git'teki istenen duruma yaklaştırır.
## B
`kubectl edit` doğrudan etcd'yi güncellediği için kalıcıdır; GitOps yalnız ilk kurulumda çalışır, sonrasında kümedeki hâli değiştirmez
🤖 uzlaştırıcı sürekli çalışır ve farkı gördükçe git'teki duruma geri çeker; ilk kuruluma özgü bir mekanizma değildir.
## C
Helm ve Kustomize bu sorunu çözer, çünkü ikisi de kümede yapılan elle değişiklikleri otomatik olarak git deposuna geri yazar
🤖 her ikisi de manifest üreten araçlardır; kümeden git'e geri yazma (ters senkron) yapmazlar.
## D
Buyurgan komutlarla bildirimsel yönetim tamamen eşdeğerdir; `kubectl edit` ile `kubectl apply` arasında sonuç farkı yoktur ve sürüklenme diye bir sorun oluşmaz
🤖 buyurgan müdahale hiçbir kaynak dosyaya yazılmaz, bu yüzden küme ile manifest'ler arasında fark birikir; sürüklenmenin tanımı budur.
## Doğru
A
## Açıklama
GitOps'un temel kuralı, istenen durumun **tek kaynağının git deposu** olmasıdır; kümede çalışan **uzlaştırıcı (reconciler)** — Argo CD veya Flux — kümenin gerçek hâlini sürekli git'tekiyle karşılaştırır ve farkı kapatır. Bu yüzden `kubectl edit` gibi **buyurgan (imperative)** bir müdahale kalıcı olmaz: bir sonraki uzlaştırma turunda küme git'teki hâle geri çekilir, değişiklik kaybolur. Kümeye elle dokunmakla manifest'ler arasında biriken bu farka **sürüklenme (drift)** denir; "buyurgan ile bildirimsel eşdeğerdir" demek tam olarak bu olguyu görmezden gelmektir — `kubectl apply` istenen durumu dosyada tutup farkı hesaplarken, `kubectl edit` hiçbir yere yazmadan anlık değişiklik yapar. `kubectl edit`'in "doğrudan etcd'yi güncellediği için kalıcı olduğu" iddiası da yanlıştır: her yazma zaten API sunucusundan geçer ve etcd'ye düşer, ama kalıcılığı belirleyen etcd değil, uzlaştırıcının hangi kaynağı doğru kabul ettiğidir. **Helm** (şablon + `values` ile manifest üreten paket yöneticisi) ve **Kustomize** (şablonsuz, taban manifest üstüne ortam katmanı/overlay uygulayan araç) bu sorunu çözmez; ikisi de manifest **üretir**, kümedeki değişikliği git'e geri yazmaz. Doğru refleks acil müdahaleyi yaptıktan hemen sonra aynı değişikliği ilgili Kustomize katmanına (ya da Helm `values` dosyasına) işleyip birleştirmek, böylece git ile kümenin tekrar aynı şeyi söylemesini sağlamaktır.
