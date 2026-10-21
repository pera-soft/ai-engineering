---
slug: k8s-olceklenme-dayaniklilik
baslik: "Kubernetes'te Ölçeklenme ve Dayanıklılık: QoS, HPA, PDB, Yerleştirme ve Zarif Kapanış"
kademe: İleri
sira: 218
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir banka şubesini düşün. Her gişeye ayrılan asgari masa alanı vardır (bu, pod'un `requests` değeri), bir de kimsenin taşamayacağı fiziksel duvar (bu da `limits`). Yoğun saatte müdür gişe sayısını artırır, öğle sonrası azaltır — ama her dakika bir gişe açıp kapatmaz, çünkü personelin yerleşmesi zaman alır; işte bu "acele etmeme" kuralı **stabilizasyon penceresidir**. Şube tadilata girse bile "aynı anda en fazla bir gişe kapanabilir" kuralı vardır: bu **PodDisruptionBudget**'tır. Bütün gişeleri aynı kata koymamak (**yerleştirme denetimi**), kapanış saatinde sıradaki müşteriyi bitirip yeni müşteri almamak (**zarif kapanış**) da aynı disiplinin parçalarıdır. Kubernetes'te ölçeklenme ve dayanıklılık, bu beş kuralın birlikte kurulmasıdır; biri eksikse kesinti kapıdadır.

### Temel kavramlar

- **QoS sınıfı (Quality of Service class):** Kubernetes'in `requests`/`limits` değerlerinden **türettiği**, manifestte elle yazılmayan salt-okunur bir etikettir. Üç değeri vardır. **Guaranteed:** pod'daki *her* konteynerde hem CPU hem bellek için `requests` ile `limits` birebir eşittir. **Burstable:** en az bir `request` tanımlıdır ama Guaranteed şartı sağlanmaz. **BestEffort:** hiçbir konteynerde `request` da `limit` de yoktur.
- **Tahliye (eviction):** düğümün belleği tükenmeye başlayınca (MemoryPressure) kubelet, düğümü kurtarmak için pod öldürür. Sıralama kabaca şöyledir. Önce **kullanımı `request`'ini aşmış** pod'lar gider; `request`'i hiç olmayan **BestEffort** pod pratikte hep bu gruptadır. Sonra düşük öncelikli (priority) **Burstable** pod'lar gelir. En son **Guaranteed** pod'lara sıra gelir. Tahliye edilen pod `Evicted` durumuna düşer ve zamanlayıcı onu başka bir düğümde yeniden ayağa kaldırır.
- **`OOMKilled` ile `Evicted` farkı:** `OOMKilled`, konteyner **kendi bellek limitini** aştığında çekirdeğin (kernel) verdiği karardır; konteyner aynı düğümde yeniden başlar. `Evicted` ise **düğümün** belleği bittiğinde kubelet'in verdiği karardır; pod o düğümden tamamen çıkarılır.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="QoS sınıfları ve düğüm bellek baskısında tahliye sırası">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">requests/limits → QoS sınıfı → tahliye sırası</text>
  <rect x="24" y="42" width="196" height="150" rx="12" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="122" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#B23B3B" font-family="system-ui">BestEffort</text>
  <text x="122" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">request YOK, limit YOK</text>
  <text x="122" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">düğüm baskısında</text>
  <text x="122" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">İLK tahliye edilen</text>
  <text x="122" y="154" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">sıra: 1</text>
  <text x="122" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">üretimde kullanma</text>
  <rect x="232" y="42" width="196" height="150" rx="12" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#B07D20" font-family="system-ui">Burstable</text>
  <text x="330" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">request var, limit farklı</text>
  <text x="330" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">request'ini AŞMIŞ olan</text>
  <text x="330" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">önce gider</text>
  <text x="330" y="154" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">sıra: 2</text>
  <text x="330" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çoğu web servisi burada</text>
  <rect x="440" y="42" width="196" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="538" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Guaranteed</text>
  <text x="538" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">request == limit (CPU+bellek)</text>
  <text x="538" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">en kararlı; baskıda</text>
  <text x="538" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">EN SON tahliye edilir</text>
  <text x="538" y="154" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">sıra: 3</text>
  <text x="538" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kritik/çekirdek servisler</text>
  <text x="330" y="218" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Evicted = DÜĞÜMÜN belleği bitti (kubelet kararı, pod başka düğüme gider)</text>
  <text x="330" y="238" text-anchor="middle" font-size="11" fill="#B23B3B" font-family="system-ui">OOMKilled = KONTEYNERİN limiti aşıldı (çekirdek kararı, aynı düğümde yeniden başlar)</text>
</svg>
```

- **Sıkıştırılabilir / sıkıştırılamaz kaynak asimetrisi:** CPU **sıkıştırılabilir (compressible)** bir kaynaktır — tavana çarpan süreç öldürülmez, yalnızca **kısıtlanır (throttling)** ve yavaşlar. Bellek **sıkıştırılamaz**dır — geri alınamayacağı için tavanı aşan konteyner **öldürülür (`OOMKilled`)**. Aynı ayarın iki kaynakta bambaşka sonuç vermesinin sebebi budur.
- **CPU limitini kaldırma tartışması:** çekirdek, CPU kotasını 100 milisaniyelik dilimler hâlinde uygular. Çok iş parçacıklı (multi-threaded) bir JVM, kotasını dilimin başında tüketip dilimin kalanında bekleyebilir. Sonuç şudur: ortalama CPU düşük görünürken **p99 gecikmesi sıçrar**. Bu yüzden yaygın öneri üç maddedir. **Bellek limitini mutlaka koy**; yoksa gürültücü bir komşu düğümü çökertir. **CPU `request`'ini doğru ölç.** CPU limitini ise ya hiç koyma ya da bol koy. Bunun da bir bedeli vardır: CPU limiti olmayan pod **Guaranteed** olamaz ve komşularının CPU'sunu çalabilir. Bu yüzden karar, kümenin paylaşımlı mı yoksa tek ekibe ait mi olduğuna bağlıdır.
- **HPA (Horizontal Pod Autoscaler):** bir **hedef metriğe** bakıp Deployment'ın kopya sayısını `minReplicas` ile `maxReplicas` arasında değiştirir. Kabaca formülü şudur: `istenenKopya = ceil(mevcutKopya × (ölçülenMetrik / hedefMetrik))`. Hedefe çok yakın değerlerde boşuna oynamasın diye küçük bir **tolerans** payı bırakır; varsayılanı yaklaşık %10'dur. Denetim döngüsü varsayılan olarak 15 saniyede bir çalışır.
- **Stabilizasyon penceresi (stabilization window):** HPA'nın `behavior` alanında tanımlanır. **Ölçek-aşağıda varsayılan 300 saniyedir**: son 5 dakikanın en yüksek önerisi baz alınır, böylece kopya sayısı dakikada bir inip çıkmaz (**flapping**). Ölçek-yukarıda varsayılan 0'dır, çünkü yükü karşılamada gecikmek istenmez.
- **Özel ve harici metrikle ölçekleme:** CPU çoğu zaman dolaylı bir **vekil (proxy)** metriktir. Bir kuyruk tüketicisi için gerçek gösterge **kuyruk derinliği ya da tüketici gecikmesidir (consumer lag)**. Bir HTTP servisi içinse **saniyedeki istek sayısı** olabilir. HPA bunları özel (custom) veya harici (external) metrik olarak kullanabilir. **KEDA** bu işi kolaylaştıran, olay güdümlü bir ölçekleyicidir. Arka planda yine HPA üretir; ama metriği Kafka, RabbitMQ, veritabanı gibi kaynaklardan kendisi toplar ve gerekirse **sıfır kopyaya kadar** indirebilir.
- **VPA (Vertical Pod Autoscaler):** kopya sayısını değil, pod'un `requests` değerini ayarlar. **HPA ile aynı metrik üzerinde çalışırlarsa çakışırlar.** Şöyle olur: VPA `request`'i büyütünce ölçülen CPU yüzdesi düşer. HPA bunu görüp kopya sayısını azaltır. Yük yoğunlaşınca VPA tekrar büyütür ve sistem salınıma girer. Kural şudur: HPA CPU'yu ölçüyorsa VPA aynı kaynağı otomatik ayarlamamalıdır. Ya yalnız öneri (recommendation) kipinde çalışır, ya da farklı bir kaynağa bakar.
- **Küme otomatik ölçekleyici (Cluster Autoscaler):** HPA pod sayısını artırdığında yerleştirilecek düğüm kalmayabilir; pod `Pending` kalır. Cluster Autoscaler (bulut sağlayıcılarında Karpenter gibi alternatifleri de vardır) bu durumda **düğüm ekler**, boşalan düğümleri de kaldırır. Yani HPA pod'u, Cluster Autoscaler düğümü ölçekler; ikisi birbirinin yerine geçmez.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="HPA denetim döngüsü ve stabilizasyon penceresi">
  <defs>
    <marker id="k8sops-hpa-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">HPA döngüsü: metrik → hesap → kopya sayısı</text>
  <rect x="20" y="46" width="150" height="76" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="95" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">metrik kaynağı</text>
  <text x="95" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">CPU / bellek</text>
  <text x="95" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">özel: kuyruk gecikmesi</text>
  <rect x="206" y="46" width="248" height="76" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="330" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">HPA hesabı (her ~15 sn)</text>
  <text x="330" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">kopya = ceil(mevcut × ölçülen / hedef)</text>
  <text x="330" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hedefe ~%10 tolerans payı</text>
  <rect x="490" y="46" width="150" height="76" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="565" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Deployment</text>
  <text x="565" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">min ≤ kopya ≤ max</text>
  <text x="565" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yeni pod readiness bekler</text>
  <path d="M172 84 L204 84" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#k8sops-hpa-arw)"/>
  <path d="M456 84 L488 84" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#k8sops-hpa-arw)"/>
  <rect x="20" y="146" width="300" height="82" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="170" y="170" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">ölçek-YUKARI: pencere 0 sn</text>
  <text x="170" y="192" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yükü karşılamada gecikme istenmez</text>
  <text x="170" y="212" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hemen kopya eklenir</text>
  <rect x="340" y="146" width="300" height="82" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="490" y="170" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">ölçek-AŞAĞI: pencere 300 sn</text>
  <text x="490" y="192" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">son 5 dk'nın EN YÜKSEK önerisi baz</text>
  <text x="490" y="212" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">amaç: inip çıkma (flapping) olmasın</text>
</svg>
```

- **PodDisruptionBudget (PDB):** "bu uygulamanın aynı anda en fazla kaçı devre dışı kalabilir?" bütçesidir; `minAvailable` (en az kaçı ayakta kalsın) ya da `maxUnavailable` ile yazılır. **Yalnız gönüllü kesintileri (voluntary disruption)** kapsar: düğümü boşaltma (`kubectl drain`), düğüm yükseltmesi, küme ölçekleyicinin düğüm boşaltması. **Gönüllü olmayan kesintiyi** engellemez; düğümün çökmesi, donanım arızası, `OOMKilled` ve tahliye bu gruba girer. Yani PDB, boşaltmayı bekletebilir ama çöken bir düğümü ayakta tutamaz.
- **Yerleştirme denetimi (placement):** dört ayrı araç, dört ayrı soruyu cevaplar. **node affinity / nodeSelector:** "hangi tip düğüme gideyim?" (ör. yalnız SSD'li düğümler). **taint / toleration:** düğüm tarafından itmedir — düğüm "beni yalnız tolere eden alsın" der (ör. yalnız toplu iş düğümleri). **pod anti-affinity:** "aynı etiketli kardeşimle aynı düğümde ya da bölgede olmayayım" demektir; kopyaların tek düğüme yığılmasını önler. **topolojik dağıtım (topology spread):** `topologyKey` (düğüm, bölge/zone) ve `maxSkew` ile kopyaların dengeli dağılmasını ister. Burada `whenUnsatisfiable: DoNotSchedule` katı bir kuraldır; sağlanmazsa pod `Pending` kalır. `ScheduleAnyway` ise yalnız bir tercihtir.
- **Zarif kapanış zinciri:** pod silindiği anda **iki iş paralel başlar**. (1) Kontrol düzlemi pod'u Service'in uç nokta (endpoint) listesinden çıkarır. Bu bilgi tüm düğümlerdeki kube-proxy ve Ingress'e **eşzamansız (asenkron)** yayılır. (2) kubelet, tanımlıysa `preStop` kancasını çalıştırır; bu kanca, konteyner kapatılmadan hemen önce koşan komuttur. Kanca bitince ana sürece **SIGTERM** gönderilir. İkisi arasında bir **yarış (race)** vardır. Uç nokta güncellemesi henüz yayılmamışken uygulama SIGTERM alıp portu kapatırsa, yola çıkmış istekler `Connection refused` alır. Çözüm iki adımdır: `preStop` içine küçük bir bekleme koyup SIGTERM'i geciktirmek ve **"hazır mısın" denetimini (readiness) kapanışın en başında düşürmek**.
- **Zaman bütçesi:** `terminationGracePeriodSeconds` (varsayılan 30 sn) sayacı pod silme isteğiyle başlar ve **`preStop` süresi de bu bütçeden harcanır**; süre dolduğunda yakalanamayan SIGKILL gelir. Spring Boot tarafında `server.shutdown=graceful` yeni istek almayı durdurup süregelenleri bitirir, `spring.lifecycle.timeout-per-shutdown-phase` (varsayılan 30 sn) ise bunun için tanınan süredir. **Kural:** `preStop bekleme + uygulamanın kapanış zaman aşımı < terminationGracePeriodSeconds`. Bu eşitsizlik bozulursa uygulama işini bitiremeden SIGKILL yer.

### Nasıl çalışır?

1. **QoS türetilir.** Manifestteki `requests`/`limits` okunur, pod'a Guaranteed / Burstable / BestEffort etiketi verilir; bu etiket düğüm sıkıştığında kimin önce gideceğini belirler.
2. **Zamanlayıcı yerleştirir.** Önce `requests`'e yer bulur; sonra node affinity, taint/toleration, pod anti-affinity ve topolojik dağıtım kısıtlarını uygular. Kısıt katıysa ve uyan düğüm yoksa pod `Pending` kalır.
3. **Cluster Autoscaler devreye girer.** `Pending` pod varsa düğüm eklenir; uzun süre boş kalan düğümler (üstlerindeki pod'lar başka yere sığıyorsa) kaldırılır.
4. **HPA döngüsü döner.** Yaklaşık 15 saniyede bir metrik okunur, formül uygulanır, tolerans dışındaysa kopya sayısı değişir; aşağı yönde 300 saniyelik stabilizasyon penceresi kararı yumuşatır.
5. **Yeni kopya trafiğe girer.** Pod ayağa kalkar, readiness'tan geçince Service'in uç nokta listesine eklenir; ancak o zaman istek almaya başlar.
6. **Bakım yapılır.** `kubectl drain` düğümü boşaltmak ister; PDB bütçesi tükendiğinde drain **bekler**, bütçe açıldıkça pod'lar sırayla taşınır. Böylece bakım kesintisiz ilerler.
7. **Pod kapanır.** Uç nokta listesinden çıkarma yayılırken `preStop` bekler, SIGTERM gelir, uygulama süregelen istekleri bitirir, bütçe dolarsa SIGKILL gelir.

```svg
<svg viewBox="0 0 660 260" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kesintisiz kapanış: uç nokta yayılımı ile SIGTERM yarışı ve zaman bütçesi">
  <defs>
    <marker id="k8sops-shut-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kapanışta iki şerit paralel ilerler — aradaki yarış</text>
  <text x="14" y="62" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">kontrol düzlemi</text>
  <rect x="120" y="48" width="190" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="215" y="65" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">endpoint'ten çıkarma</text>
  <text x="215" y="80" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kube-proxy'lere yayılıyor…</text>
  <rect x="318" y="48" width="300" height="40" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="468" y="72" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">artık bu pod'a YENİ istek yönlendirilmiyor</text>
  <text x="14" y="132" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">kubelet / konteyner</text>
  <rect x="120" y="112" width="190" height="46" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="215" y="130" text-anchor="middle" font-size="10.5" fill="#B07D20" font-family="system-ui">preStop: kısa bekleme</text>
  <text x="215" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">readiness zaten düşürüldü</text>
  <rect x="318" y="112" width="240" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="438" y="130" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">SIGTERM → graceful kapanış</text>
  <text x="438" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">süregelen istekler bitirilir</text>
  <rect x="566" y="112" width="52" height="46" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="592" y="132" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">SIG</text>
  <text x="592" y="147" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">KILL</text>
  <line x1="120" y1="40" x2="120" y2="176" stroke="#5D7789" stroke-width="1.2" stroke-dasharray="4 3"/>
  <text x="120" y="192" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">pod silme isteği (t0)</text>
  <line x1="120" y1="212" x2="618" y2="212" stroke="#5D7789" stroke-width="1.4" marker-end="url(#k8sops-shut-arw)"/>
  <text x="360" y="230" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">terminationGracePeriodSeconds — preStop dahil TEK bütçe</text>
  <text x="330" y="252" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">preStop bekleme + uygulama kapanış zaman aşımı &lt; grace period olmalı</text>
</svg>
```

### Mülakatta ne sorulur / tuzaklar

- **"QoS sınıfını nasıl seçersin?"** Tuzak sorudur: seçilmez, `requests`/`limits`'ten **türetilir**. `qosClass` alanı manifeste yazılmaz, salt-okunurdur. Kritik servisi baskı altında en son tahliye edilecek gruba almanın yolu, CPU ve belleği eşitleyip Guaranteed yapmaktır.
- **"`OOMKilled` ile `Evicted` aynı şey mi?"** Değil. İlki konteynerin **kendi** limitini aşmasıdır (çekirdek öldürür, aynı düğümde yeniden başlar); ikincisi **düğümün** kaynağının bitmesidir (kubelet çıkarır, pod başka düğüme gider). Bu ayrımı yapamamak sorun gidermeyi kökten yanlış yola sokar.
- **"CPU limitini kaldırmak doğru mu?"** Cevap "duruma bağlı" ve gerekçesi olmalı: CPU kotası dilimli uygulandığı için sıkı limit p99 gecikmesini bozar; ama limitsiz pod Guaranteed olamaz ve komşusunun CPU'sunu çalar. **Bellek limitini kaldırmak** ise nadiren savunulabilir — düğümü çökertme riski vardır.
- **"CPU %40 iken kuyruk birikiyorsa ne yaparsın?"** CPU'nun **vekil metrik** olduğunu görmek beklenir; ölçekleme kuyruk derinliği/tüketici gecikmesi gibi doğrudan metriğe (özel/harici metrik ya da KEDA) bağlanır. "CPU hedefini düşürelim" demek belirtiyi tedavi etmektir.
- **"Kopya sayısı sürekli inip çıkıyor."** Beklenen kavram **stabilizasyon penceresi** ve `behavior` politikalarıdır; ölçek-aşağı varsayılanının 300 saniye olduğunu bilmek, sorunun genelde yukarı yöndeki agresiflikten ya da gürültülü metrikten geldiğini söylemek iyi cevaptır.
- **"HPA ile VPA'yı birlikte kullanır mısın?"** Aynı kaynak üzerinde **hayır** — birbirlerinin girdisini bozar, salınım üretirler. Ayrı kaynak ya da öneri kipi kabul edilebilir.
- **"Bakım sırasında servis nasıl ayakta kalır?"** PDB. Ama sınırını söylemek şart: PDB **gönüllü** kesintiyi sınırlar; düğüm çökerse devreye girmez. Yüksek erişilebilirlik için PDB tek başına yetmez, kopyaların farklı düğüm/bölgelere dağıtılmış olması da gerekir.
- **"Üç kopya aynı düğüme düşmüş, ne yaparsın?"** Doğru araç **pod anti-affinity** veya **topolojik dağıtım**tır. Node affinity ve taint/toleration bu sorunu çözmez; onlar "hangi düğüm" sorusunun cevabıdır, "kardeşlerimden ayrı dur" değil.
- **"Zarif kapanışı kurdum ama hâlâ istek kaybediyorum."** İki klasik sebep vardır. (1) "Hazır mısın" denetimi kapanışta düşürülmemiştir ya da `preStop` beklemesi yoktur; uç nokta yayılımı SIGTERM'e yetişemez. (2) Uygulamanın kapanış zaman aşımı `terminationGracePeriodSeconds`'ten uzundur; iş bitmeden SIGKILL gelir. Sürelerin **iç içe** olması gerektiğini söylemek, konuyu ezberden bilenle yaşayanı ayırır.

### Tek cümlede

Kesintisizlik tek bir ayardan değil bir zincirden gelir. `requests`/`limits` QoS sınıfını ve tahliye sırasını belirler; HPA doğru metrikle ve stabilizasyon penceresiyle kopya sayısını yönetir; Cluster Autoscaler düğümü büyütür. PDB bakımı bütçeler, anti-affinity ve topolojik dağıtım kopyaları ayrı tutar, zarif kapanış da sürelerin iç içe olduğu bir bütçeyle son isteği kaybetmeden pod'u indirir.

# k8sops-1 | Orta
## Soru
Kredi limit servisinin pod'unda her konteyner için CPU ve bellek `requests` değerleri `limits` ile birebir aynı verilmiş. Bu pod hangi QoS sınıfına girer ve düğüm bellek baskısına girdiğinde tahliye sırasında nerede durur?
## A
Burstable sınıfına girer; `requests` ile `limits` eşit olduğunda pod tavana kadar sıçrayabildiği için Kubernetes onu esnek sayar
🤖 eşitlik tam olarak sıçrama payı bırakmamak demektir ve bu, Burstable'ın değil Guaranteed'ın tanımıdır.
## B
BestEffort sınıfına girer; manifeste `limits` yazıldığı anda `requests` değerleri yok sayılır ve pod tahliyede ilk sıraya düşer
🤖 BestEffort hiç `requests` ve `limits` tanımlanmamış pod demektir; limit yazmak request'i geçersiz kılmaz.
## C
Guaranteed sınıfına girer; kullanımı `requests` değerini aşmış BestEffort ve Burstable pod'lar tahliyede ondan önce sıraya girer
🤖 Doğru: her konteynerde CPU ve belleğin request/limit eşitliği Guaranteed'ın tanımıdır ve bu sınıf baskıda en son tahliye edilir.
## D
QoS sınıfı manifestte `qosClass` alanıyla elle seçilir; bu alan yazılmadığı için tahliye sırası tamamen rastgele belirlenir
🤖 `qosClass` salt-okunur türetilmiş bir alandır ve tahliye rastgele değil, kaynak kullanımı ile önceliğe göre sıralanır.
## Doğru
C
## Açıklama
**QoS sınıfı seçilmez, türetilir.** Kubernetes pod'un `requests`/`limits` değerlerine bakar: pod'daki *her* konteynerde hem CPU hem bellek için `requests == limits` ise sınıf **Guaranteed**, en az bir `request` var ama bu eşitlik yoksa **Burstable**, hiç `request` ve `limit` yoksa **BestEffort** olur. `qosClass` alanı sonuçtur, girdi değildir — bu yüzden "elle seçilir" diyen şık yanlıştır. Eşitliği "esneklik" sanmak da yanlıştır: eşitlik pod'a sıçrama payı bırakmaz, tam tersine kaynağını garantiler. Limit yazmanın request'i geçersiz kıldığı iddiası da yanlıştır; BestEffort yalnızca **hiçbir** kaynak alanı tanımlanmadığında ortaya çıkar. Düğüm bellek baskısına girdiğinde kubelet önce **kullanımı `request`'ini aşmış** pod'ları hedefler — `request`'i hiç olmayan BestEffort pod'lar pratikte hep bu gruptadır —, ardından düşük öncelikli Burstable pod'lar gelir, **Guaranteed en son** tahliye edilir. Kritik bir kredi servisini baskı altında ayakta tutmanın en doğrudan yolu budur; bedeli, garanti edilen kaynağın kullanılmasa da rezerve kalmasıdır.

# k8sops-2 | Orta
## Soru
Gün sonu (EOD) tahakkuk işini yürüten pod CPU limitine dayandığında p99 gecikmesi sıçrıyor ama pod ölmüyor; bellek tarafında ise ara sıra `OOMKilled` görülüyor. Bu asimetrinin sebebi nedir?
## A
CPU sıkıştırılabilir bir kaynaktır, tavana çarpınca süreç yalnızca kısıtlanır (throttling) ve yavaşlar; bellek sıkıştırılamaz olduğu için tavanı aşan konteyner öldürülür
🤖 Doğru: CPU geri alınabilir bir kaynak olduğundan yavaşlatmak yeterlidir, bellek geri alınamadığı için tek çare konteyneri sonlandırmaktır.
## B
CPU limiti çekirdek tarafından uygulanmayan bir tavsiye değeridir ve yalnız zamanlayıcının kararını etkiler; bellek limiti ise gerçekten zorlayıcıdır
🤖 CPU limiti çekirdeğin dilimli kotasıyla gerçekten uygulanır; zamanlayıcının kararını etkileyen `limits` değil `requests`'tir.
## C
İki limit de çekirdek tarafından aynı biçimde uygulanır; farkı yaratan tek şey JVM'in konteynerdeki CPU limitini okuyamıyor olmasıdır
🤖 modern JVM cgroup CPU kotasını okur; fark JVM'den değil, iki kaynağın doğasındaki geri alınabilirlik farkından gelir.
## D
CPU tavanı aşılınca pod tahliye edilir (`Evicted`), bellek tavanı aşılınca `OOMKilled` olur; sonuçta ikisi de pod'u sonlandırdığı için fark yalnız durum adındadır
🤖 CPU tavanını aşmak tahliye sebebi değildir; tahliye düğümün kaynağı bittiğinde olur, konteyner kendi CPU tavanına çarptığında değil.
## Doğru
A
## Açıklama
Ayrım kaynağın doğasındadır. CPU **sıkıştırılabilir (compressible)** bir kaynaktır: süreçten geri alınabilir, bu yüzden tavana çarpıldığında çekirdek onu öldürmez, yalnızca **kısıtlar (throttling)**. Bellek **sıkıştırılamaz**dır: bir kez ayrılmış belleği zorla geri almanın yolu yoktur, bu yüzden tek yaptırım konteyneri **`OOMKilled`** ile sonlandırmaktır. CPU limitinin uygulanmadığını söylemek yanlıştır — çekirdek kotayı yaklaşık 100 milisaniyelik dilimlerde uygular; zaten p99 sıçramasının sebebi de budur: çok iş parçacıklı JVM kotayı dilimin başında tüketip kalan sürede bekler, ortalama CPU düşük görünürken kuyruk uzar. Sorunun JVM'in limiti okuyamamasından geldiği iddiası da yanlıştır; modern JVM cgroup kotasını okuyup kullanılabilir işlemci sayısını ona göre belirler. `Evicted` ile `OOMKilled`'ı eşitlemek de hatadır: **`OOMKilled` konteynerin kendi limitini aşmasıdır** (aynı düğümde yeniden başlar), **`Evicted` düğümün kaynağının bitmesidir** (pod başka düğüme taşınır). Pratik sonuç: bellek limiti mutlaka tanımlanır, CPU `request`'i doğru ölçülür, gecikmeye duyarlı işlerde CPU limiti ya bol tutulur ya da hiç konmaz — bedeli, pod'un Guaranteed olamaması ve komşularının CPU'sunu çalabilmesidir.

# k8sops-3 | İleri
## Soru
Kredi başvurusu tüketicisi Kafka'dan besleniyor. Zirve saatlerde kuyrukta birikme oluyor ama CPU hiç %40'ı geçmiyor; ayrıca HPA kopya sayısını dakikada birkaç kez aşağı-yukarı oynatıyor. En doğru yaklaşım hangisidir?
## A
HPA'nın CPU hedefini %20'ye çekmek; eşik düştüğü için ölçekleme kuyruk birikmeden önce tetiklenir ve oynama da kendiliğinden azalır
🤖 bu, yanlış metriğin eşiğini oynatmaktır; hedefi düşürmek tepkiyi daha da agresifleştirip inip çıkmayı artırır.
## B
`minReplicas` ile `maxReplicas` değerlerini eşitleyip ölçeklemeyi kapatmak ve kopya sayısını zirve yüküne göre sabitlemek
🤖 bu otomatik ölçeklemeyi tümden kaldırır; boş saatlerde kaynak israfı, beklenmedik zirvede yetersizlik üretir.
## C
HPA'yı bellek hedefiyle kurmak; JVM heap kullanımı işlenmeyi bekleyen mesaj sayısını CPU'dan çok daha doğru yansıtır
🤖 JVM heap kullanımı çöp toplama davranışına bağlı olarak yüksek seyreder ve yük azalınca hemen düşmez, bu yüzden kötü bir ölçekleme sinyalidir.
## D
Ölçeklemeyi kuyruk derinliği / tüketici gecikmesi gibi doğrudan bir metriğe bağlamak (ör. KEDA ile) ve `behavior` altında ölçek-aşağı stabilizasyon penceresini uzatmak
🤖 Doğru: gerçek iş yükünü ölçen metrik doğru kararı verir, stabilizasyon penceresi de aşağı yöndeki oynamayı bastırır.
## Doğru
D
## Açıklama
İki ayrı sorun var ve her birinin ayrı çözümü var. Birincisi **yanlış metrik**: CPU burada bir **vekil (proxy)** göstergedir ve tüketici çoğu zaman ağ/veritabanı beklediği için CPU düşük kalır — iş birikmesini doğrudan gösteren şey **kuyruk derinliği / tüketici gecikmesi (consumer lag)**'dir. HPA bunu özel (custom) veya harici (external) metrik olarak kullanabilir; **KEDA** bu kurulumu kolaylaştırır, metriği Kafka'dan kendisi toplar ve gerekirse sıfır kopyaya kadar indirir. CPU hedefini %20'ye çekmek yanlış metriği daha hassas hâle getirmektir: gürültüye daha çabuk tepki verir, inip çıkmayı **artırır**. Bellek hedefi ise daha kötüdür; JVM heap'i çöp toplama ritmine göre dolu görünür ve yük azaldığında hemen boşalmaz, dolayısıyla aşağı ölçekleme hiç tetiklenmez. İkincisi **flapping**: HPA'nın `behavior` alanındaki **stabilizasyon penceresi** tam bunun içindir; ölçek-aşağıda varsayılan 300 saniyedir ve son beş dakikanın en yüksek önerisini baz alır, gerekirse uzatılır ya da ölçek-aşağı politikasıyla adım boyu sınırlanır. `minReplicas` ile `maxReplicas`'ı eşitlemek ise ölçeklemeyi kapatmaktır; gün içi dalgalı bir başvuru akışında hem israf hem risk üretir.

# k8sops-4 | İleri
## Soru
Platform ekibi düğüm yükseltmesi için `kubectl drain` çalıştıracak. 3 kopyalı limit servisinin bu işlem sırasında tamamen kapanmayacağını garanti etmek istiyorsun. Ne yaparsın ve bu garantinin sınırı nedir?
## A
`terminationGracePeriodSeconds` değerini büyütmek yeterlidir; uzun süre tanınan pod'lar drain sırasında sırayla ve kesintisiz boşaltılır
🤖 grace period yalnız her pod'a kapanması için ne kadar süre tanınacağını belirler, aynı anda kaç pod'un boşaltılacağını sınırlamaz.
## B
`PodDisruptionBudget` ile `minAvailable: 2` tanımlamak; bu yalnız gönüllü kesintileri sınırlar, düğüm çökmesi gibi gönüllü olmayan kesintiyi engellemez
🤖 Doğru: PDB bütçesi dolduğunda drain bekler, ama çöken donanım veya tahliye karşısında hiçbir güvence vermez.
## C
Kopya sayısını 10'a çıkarmak; yeterince kopya olduğunda drain işlemi bunların hepsini aynı anda boşaltamayacağı için servis ayakta kalır
🤖 kopya sayısı tek başına bütçe koymaz; bütün kopyalar boşaltılan düğümdeyse drain hepsini birden çıkarabilir.
## D
`PodDisruptionBudget` tanımlamak; PDB düğüm çökmesi ve `OOMKilled` dahil her türlü pod kaybını sınırladığı için ayrıca bir önlem gerekmez
🤖 PDB'nin kapsamı yalnız gönüllü kesintilerdir; gönüllü olmayan kesintide devreye girmez, dolayısıyla tek başına yetmez.
## Doğru
B
## Açıklama
Doğru araç **PodDisruptionBudget**'tır: `minAvailable: 2` (ya da `maxUnavailable: 1`) dediğinde, tahliye API'si üzerinden ilerleyen `kubectl drain` bütçeyi bozacak pod'u çıkarmayı **reddeder ve bekler**; bütçe açıldıkça pod'lar sırayla taşınır, servis hiç sıfıra düşmez. `terminationGracePeriodSeconds` bambaşka bir şeydir — tek bir pod'a kapanması için tanınan süredir, eşzamanlı kaç pod'un devre dışı kalacağıyla ilgisi yoktur. Kopya sayısını artırmak da garanti değildir: yerleştirme kısıtı yoksa kopyaların hepsi aynı düğümde olabilir ve o düğüm boşaltılınca hepsi birden gider. Asıl ayrım son iki şıkta: **PDB yalnız gönüllü kesintileri (voluntary disruption)** kapsar — drain, düğüm yükseltmesi, küme ölçekleyicinin düğüm boşaltması. **Gönüllü olmayan** kesintide — donanım arızası, düğüm çökmesi, `OOMKilled`, kaynak baskısıyla tahliye — PDB hiçbir şey yapamaz; kimse ondan izin istemez. Bu yüzden gerçek yüksek erişilebilirlik, PDB ile birlikte kopyaların **farklı düğüm ve bölgelere dağıtılmasını** (pod anti-affinity / topolojik dağıtım) ve zarif kapanışın doğru kurulmasını gerektirir.

# k8sops-5 | İleri
## Soru
3 kopyalı mutabakat servisinin bütün pod'ları zamanlayıcı tarafından tesadüfen aynı düğüme yerleştirilmiş; o düğüm çökünce servis tamamen düştü. Bu yığılmayı kalıcı olarak önlemenin doğru yolu hangisidir?
## A
Node affinity kuralı ekleyip pod'ları belirli etiketlere sahip düğümlere zorlamak; hedef düğüm kümesi netleşince kopyalar da kendiliğinden dağılır
🤖 node affinity "hangi tip düğüme gideyim" sorusunu cevaplar, kopyaların birbirinden ayrı durmasını sağlamaz.
## B
Düğümlere taint ekleyip pod'a uygun toleration tanımlamak; düğüm itme kuralı devreye girince kopyalar farklı düğümlere dağılır
🤖 taint/toleration bir düğümü belirli pod'lar dışında herkese kapatır; kopyaların dağılımını hiç etkilemez.
## C
`PodDisruptionBudget` ile `minAvailable: 2` tanımlamak; bütçe kuralı zamanlayıcıyı kopyaları ayrı düğümlere koymaya zorlar
🤖 PDB yerleştirmeye karışmaz, yalnız gönüllü kesintilerde aynı anda kaç pod'un devre dışı kalacağını sınırlar.
## D
Pod anti-affinity ya da topolojik dağıtım kısıtı (`topologyKey` düğüm/bölge, `maxSkew: 1`) tanımlayıp kopyaları düğümlere ve bölgelere yaymak
🤖 Doğru: bu iki araç doğrudan "aynı etiketli kardeşimle aynı düğümde olmayayım" kuralını kurar ve yığılmayı yerleştirme anında engeller.
## Doğru
D
## Açıklama
Sorun bir **yerleştirme (placement)** sorunudur ve her aracın cevapladığı soru farklıdır. **Node affinity / nodeSelector** "hangi *tip* düğüme gideyim?" der (ör. SSD'li, belirli bölgedeki düğümler) — üç kopyanın hepsi yine aynı uygun düğüme düşebilir. **Taint / toleration** düğüm tarafından itmedir: düğüm "beni yalnız tolere eden pod alsın" der; bu, düğümü ayırmak içindir, kardeş pod'ları ayırmak için değil. **PDB** ise yerleştirmeye hiç karışmaz; yalnız gönüllü kesintilerde eşzamanlı kaç pod'un devre dışı kalabileceğini bütçeler — üç kopya aynı düğümdeyse ve o düğüm **çökerse** PDB devreye bile girmez. Doğru araçlar **pod anti-affinity** ("aynı etikete sahip pod'la aynı `topologyKey` değerinde olmayayım") ve **topolojik dağıtım kısıtıdır** (`topologySpreadConstraints`): `topologyKey` düğüm ya da bölge seçilir, `maxSkew: 1` ile kopya sayıları arasındaki en büyük fark sınırlanır. `whenUnsatisfiable` seçimi kritiktir: `DoNotSchedule` katı kuraldır, uygun yer yoksa pod `Pending` kalır (küme ölçekleyici düğüm ekleyebilir); `ScheduleAnyway` yalnız tercihtir ve kaynak sıkışınca yığılmaya yine izin verir. Uygulamada bu kısıt PDB ile birlikte kurulur: biri arızaya, diğeri bakıma karşı korur.

# k8sops-6 | Uzman
## Soru
Bir serviste `server.shutdown=graceful` açık, `spring.lifecycle.timeout-per-shutdown-phase=45s`, pod'da `terminationGracePeriodSeconds: 30` ve `preStop` kancasında 10 saniyelik bir bekleme var. Yuvarlanan güncelleme (rolling update) sırasında hâlâ yarım kalan istekler görülüyor. Sorunun kökü nedir?
## A
`preStop` içindeki bekleme grace period bütçesinden sayılmaz; asıl sorun zarif kapanışın uygulama tarafında etkin olmamasıdır
🤖 `preStop` süresi grace period bütçesinden harcanır ve zaten `server.shutdown=graceful` ile zarif kapanış etkin durumdadır.
## B
`terminationGracePeriodSeconds` 15 saniyeye düşürülmelidir; uzun tanınan süre pod'un uç nokta listesinden çıkarılmasını geciktirir
🤖 uç noktadan çıkarma grace period'a bağlı değildir ve süreyi kısaltmak süregelen istekleri daha da erken keser.
## C
Uygulamanın kapanış zaman aşımı grace period'dan uzun, üstelik `preStop`'un 10 saniyesi de aynı bütçeden harcanıyor; istekler bitmeden SIGKILL geliyor
🤖 Doğru: bütçe 30 saniyedir, 10 saniyesi `preStop`'a gider ve kalan 20 saniye 45 saniyelik kapanış zaman aşımına yetmez.
## D
`preStop` kancası tamamen kaldırılmalıdır; kanca çalışırken uygulama SIGTERM almadığı için istekler zaten o sırada kesilir
🤖 `preStop` sırasında pod hâlâ istek işlemeye devam eder; kanca uç nokta yayılımı yarışını kapatmak için vardır, kaldırmak sorunu büyütür.
## Doğru
C
## Açıklama
Kapanışta **tek bir zaman bütçesi** vardır: `terminationGracePeriodSeconds`. Sayaç pod silme isteğiyle başlar ve **`preStop` süresi de bu bütçeden harcanır**; kanca bitince ana sürece SIGTERM gider, bütçe dolduğunda ise yakalanamayan SIGKILL gelir. Buradaki kurulumda bütçe 30 saniye, `preStop` 10 saniye alıyor, uygulamaya kalan 20 saniye; ama Spring'in `spring.lifecycle.timeout-per-shutdown-phase` değeri **45 saniye**, yani uygulama daha isteklerini bitirmeye çalışırken SIGKILL yiyor. Doğru kurulum sürelerin **iç içe** olmasıdır: `preStop bekleme + kapanış zaman aşımı < terminationGracePeriodSeconds` (ör. 10 + 25 < 40). `preStop` süresinin bütçeden sayılmadığını sanmak yanlıştır ve zaten zarif kapanış burada zaten açıktır. Grace period'ı 15 saniyeye **düşürmek** durumu kötüleştirir; ayrıca uç noktadan çıkarma bu süreyle ilgili değildir, kontrol düzlemi tarafından paralel yürütülen ayrı bir iştir. `preStop`'u kaldırmak da hatadır: uç nokta güncellemesi tüm düğümlerdeki kube-proxy'lere **asenkron** yayıldığı için, kanca olmadan SIGTERM yayılımdan önce gelir ve yola çıkmış istekler `Connection refused` alır — kancanın kısa beklemesi tam olarak bu **yarışı** kapatır. Bütünün doğru sırası şudur: readiness kapanışın en başında düşürülür, `preStop` yayılımı bekler, SIGTERM sonrası süregelen istekler bitirilir ve bunların hepsi grace period'a sığar.
