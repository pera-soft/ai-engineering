---
slug: k8s-container
baslik: "Kubernetes ve Konteyner: JVM-in-Container, Probe'lar ve Graceful Shutdown"
kademe: İleri
sira: 105
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir limanı düşün. Eskiden her yük kendi biçiminde taşınırdı; vinç operatörü her seferinde "bu ne, nereden tutulur?" diye uğraşırdı. Standart nakliye konteyneri her yükü aynı ölçüdeki kutuya koydu: içinde ne olduğu vinci ilgilendirmez, kancanın tutacağı yer hep aynıdır. Yazılımda **konteyner** de budur: uygulaman ve çalışması için gereken her şey tek bir standart kutuya konur. **Kubernetes** ise limanın vinç operatörüdür: hangi kutunun hangi makineye konacağına karar verir, bozulanı değiştirir, yoğunluk artınca kutu sayısını çoğaltır.

### Temel kavramlar

- **İmaj (image):** konteynerin dondurulmuş şablonu; **katmanlardan (layer)** oluşur — altta işletim sistemi ve Java çalışma ortamı, üstte kütüphaneler, en üstte kodun. Değişmeyen katman yeniden inşa edilmez, **önbellekten** gelir. İmajlar kayıt defterinde (registry) saklanır ve etiketle (tag) sürümlenir; `latest` sürümü sabitlemez.
- **Konteyner (container):** çalışan imaj. Sanal makineden farkı, kendi çekirdeğini taşımayıp ana makinenin çekirdeğini paylaşmasıdır; bu yüzden saniyeler içinde başlar.
- **Düğüm (node):** üstünde konteyner çalışan gerçek ya da sanal makine. **Küme (cluster):** düğümlerin tek bir kaynak havuzu gibi yönetilen topluluğu.
- **Pod:** Kubernetes'in **en küçük dağıtım birimi**. İçinde bir, bazen birkaç konteyner bulunur; bunlar ağ ad alanını ve IP adresini paylaşır. Ana konteynerin yanındaki yardımcı konteynere **yan konteyner (sidecar)** denir. Pod **geçicidir (ephemeral)**: ölebilir, başka bir düğümde yeniden yaratılır ve IP'si değişir.
- **Deployment:** "bu imajdan üç kopya istiyorum" diyen **kontrolcü**; kopya sayısını ReplicaSet üzerinden sürdürür, **yuvarlanan güncellemeyi (rolling update)** yürütür, gerekirse geri alır (rollback).
- **Service:** ölümlü pod'ların önündeki **kararlı sanal IP ve DNS adı**; trafiği o an sağlıklı pod'lara dağıtır. Tipleri: `ClusterIP` (küme içi), `NodePort` (düğüm portu), `LoadBalancer` (dış yük dengeleyici). **Ingress** ise alan adı ve yola göre HTTP yönlendirmesi yapar.
- **Sağlık yoklaması (probe):** kubelet'in konteynere düzenli sorduğu sorudur. Kaynak ölçümü değildir; bir HTTP/TCP isteği ya da konteyner içinde çalıştırılan bir komuttur. Üç türü vardır. **"Ayakta mısın" denetimi (liveness)** başarısızsa konteyner **yeniden başlatılır**. **"Hazır mısın" denetimi (readiness)** başarısızsa pod, Service'in **uç nokta (endpoint) listesinden çıkarılır**; trafik almaz ama yaşamaya devam eder. **Açılış denetimi (startup)** ise yavaş açılışta diğer ikisini erteler.

```svg
<svg viewBox="0 0 660 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Readiness ve liveness probe farkı">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki probe, iki farklı karar</text>
  <rect x="28" y="44" width="280" height="132" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="168" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">readiness probe</text>
  <text x="168" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">"Trafik alabilir miyim?"</text>
  <text x="168" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">başarısız → Service pod'u</text>
  <text x="168" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">endpoint'lerden ÇIKARIR</text>
  <text x="168" y="156" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">pod ölmez, trafik kesilir</text>
  <rect x="352" y="44" width="280" height="132" rx="12" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="492" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#B23B3B" font-family="system-ui">liveness probe</text>
  <text x="492" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">"Hâlâ hayatta/sağlıklı mıyım?"</text>
  <text x="492" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">başarısız → kubelet konteyneri</text>
  <text x="492" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ÖLDÜRÜP yeniden başlatır</text>
  <text x="492" y="156" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">yanlış kurgu = restart döngüsü</text>
</svg>
```

- **requests / limits:** pod'un istediği ve asla aşamayacağı CPU/bellek. `requests` yerleştirme kararında kullanılır, `limits` tavandır: bellek tavanını aşan konteyner **`OOMKilled`** olup yeniden başlatılır, CPU tavanını aşan öldürülmez ama **kısıtlanır (throttling)**. Limitsiz pod ise düğümün belleğini tüketip komşularını etkiler; buna **gürültücü komşu (noisy neighbor)** denir.
- **HPA (Horizontal Pod Autoscaler):** bir metrik hedefine (ör. ortalama CPU %70) bakıp **kopya sayısını** artırır/azaltır. Yatay ölçekleme daha çok kopya demektir; tek pod'u büyütmek (dikey ölçekleme, VPA) ayrı bir iştir.
- **ConfigMap / Secret:** yapılandırma ve gizli bilgi imaja gömülmez, dışarıdan verilir; böylece **aynı imaj** her ortamda çalışır.

### Nasıl çalışır?

1. **İmajı oluştur.** Kodu derle, **çok-aşamalı derleme (multi-stage build)** ile yalnız çalışma zamanı çıktısını ince bir taban imaja kopyala; derleyici ve Maven/Gradle son imajda kalmaz. Bağımlılıkları koddan ayrı katmana koyarsan, kod değişince yalnız küçük üst katman yeniden inşa edilir.
2. **İstenen durumu bildir.** Deployment'a "bu imajdan üç kopya" dersin. Kubernetes bildirimseldir (declarative): sen hedefi yazarsın, sistem gerçeği sürekli hedefe yaklaştırır; pod ölürse yerine yenisi gelir.
3. **Yerleştir.** Zamanlayıcı (scheduler) pod'un `requests` değerine bakıp yeri müsait bir düğüm seçer.
4. **Kararlı adres ver.** Service pod'ları etiketlerinden (label) bulur, önlerine değişmeyen bir adres koyar; istemci pod IP'lerini hiç bilmez.

```svg
<svg viewBox="0 0 660 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Deployment, Pod ve Service ilişkisi">
  <defs>
    <marker id="k8s-obj-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Deployment → Pod'lar ← Service</text>
  <rect x="24" y="60" width="150" height="90" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.6"/>
  <text x="99" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">Deployment</text>
  <text x="99" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kontrolcü: replika</text>
  <text x="99" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ rolling update</text>
  <rect x="270" y="48" width="120" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="330" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Pod 1</text>
  <rect x="270" y="92" width="120" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="330" y="114" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Pod 2</text>
  <rect x="270" y="136" width="120" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="330" y="158" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Pod 3 (geçici)</text>
  <rect x="486" y="60" width="150" height="90" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="561" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Service</text>
  <text x="561" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kararlı IP/DNS</text>
  <text x="561" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sağlıklıya dağıtır</text>
  <path d="M174 100 L268 78" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#k8s-obj-arw)"/>
  <path d="M174 108 L268 109" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#k8s-obj-arw)"/>
  <path d="M174 116 L268 150" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#k8s-obj-arw)"/>
  <path d="M484 92 L392 78" fill="none" stroke="#186399" stroke-width="1.3" marker-end="url(#k8s-obj-arw)"/>
  <path d="M484 105 L392 109" fill="none" stroke="#186399" stroke-width="1.3" marker-end="url(#k8s-obj-arw)"/>
  <path d="M484 118 L392 150" fill="none" stroke="#186399" stroke-width="1.3" marker-end="url(#k8s-obj-arw)"/>
  <text x="99" y="176" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">oluşturur/yönetir</text>
  <text x="561" y="176" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">trafiği yönlendirir</text>
</svg>
```

5. **Sağlığı yönet.** Varsa startup probe açılışı bekler; readiness pod'u uç nokta listesine sokar ve trafik akmaya başlar, liveness takılmış konteyneri yeniden başlatarak kurtarır.
6. **Ölçekle.** HPA metriği hedefin üstünde görürse kopya sayısını artırır, yük düşünce azaltır; yeni pod readiness'tan geçtiği anda Service ona da trafik yollar.
7. **Güncelle.** Yeni imaj verildiğinde rolling update eski pod'ları teker teker değiştirir; yeni pod hazır olmadan eskisi silinmediği için kesinti olmaz, sorun çıkarsa önceki sürüme dönülür.
8. **Kapan.** Pod silindiğinde önce uç nokta listesinden çıkarılır ve varsa `preStop` kancası çalışır; sonra ana sürece **SIGTERM** gönderilir. Sinyal (signal), işletim sisteminin bir sürece verdiği kısa mesajdır. SIGTERM "toparlan ve kapan" demektir. Uygulama `terminationGracePeriodSeconds` (varsayılan 30 saniye) boyunca süregelen istekleri bitirir; süre dolarsa yakalanamayan **SIGKILL** gelir ve süreç anında ölür.

```svg
<svg viewBox="0 0 660 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Graceful shutdown zaman çizelgesi: SIGTERM'den SIGKILL'e">
  <defs>
    <marker id="k8s-gs-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Nazik kapanış: SIGTERM → grace → SIGKILL</text>
  <line x1="40" y1="120" x2="620" y2="120" stroke="#5D7789" stroke-width="1.4" marker-end="url(#k8s-gs-arw)"/>
  <text x="612" y="142" text-anchor="end" font-size="10.5" fill="#5D7789" font-family="system-ui">zaman →</text>
  <rect x="150" y="96" width="330" height="48" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <text x="315" y="115" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">terminationGracePeriodSeconds (varsayılan 30s)</text>
  <text x="315" y="133" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">readiness düş · süregelen istekleri bitir · kapat</text>
  <line x1="150" y1="70" x2="150" y2="150" stroke="#B26B00" stroke-width="1.6"/>
  <text x="150" y="60" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">SIGTERM</text>
  <line x1="480" y1="70" x2="480" y2="150" stroke="#8A2E2E" stroke-width="1.6"/>
  <text x="480" y="60" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">SIGKILL (süre dolarsa)</text>
  <text x="330" y="178" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">preStop kancası + kısa gecikme → LB pod'u havuzdan çıkarana dek yeni istek gelmez</text>
</svg>
```

### Mülakatta ne sorulur?

- **liveness ile readiness farkı.** Beklenen cevap nettir: liveness başarısızlığı **yeniden başlatır**, readiness başarısızlığı **trafikten çıkarır**. Klasik hata, liveness'ı veritabanı gibi bir dış bağımlılığın kontrolüne bağlamaktır. Veritabanı yavaşlayınca sağlıklı konteynerler topluca yeniden başlatılır ve arıza büyür. Dış bağımlılık kontrolü readiness'ın işidir. İkinci hata, yavaş açılan uygulamaya sıkı liveness koymaktır; pod açılışı bitiremeden öldürülür ve restart döngüsüne girer, çözümü startup probe'dur.
- **HPA neyi ölçekler?** Kopya sayısını, yani yatay ölçekler; tek pod'un kaynağını büyütmez. CPU hedefinin hesaplanabilmesi için `requests` tanımlı olmalıdır.
- **Zarif kapanış sırası.** Uç noktadan çıkarma ve `preStop` → SIGTERM → bekleme → süre dolarsa SIGKILL. Uç nokta güncellemesi eşzamansız (asenkron) yayılır; bu yüzden `preStop` içine küçük bir gecikme koymak yaygın çözümdür.
- **requests/limits etkisi.** Bellek limiti aşılınca `OOMKilled`, CPU limiti aşılınca yalnız kısıtlama (throttling) olur. Modern JVM, çekirdeğin koyduğu kaynak sınırını (cgroup limiti) okur; buna konteyner farkındalığı denir. Ama **yığın belleği (heap)** dışındaki tüketimi kestiremez. Sınıf üstverisi (metaspace), iş parçacığı yığınları (thread stack) ve doğrudan tampon bellek (direct buffer) de limite dahildir. Bu yüzden yığın belleğini limite eşitleme; `-XX:MaxRAMPercentage` ile pay bırak.
- **Pod geçici ve durumsuzdur.** Oturumu bellekte veya veriyi konteyner dosya sisteminde tutan uygulama, ölçeklenince ve güncellenince veri kaybeder; durum veritabanında veya dağıtık önbellekte tutulur.

### Tek cümlede

Konteyner, uygulamayı standart bir kutuya koyar. Kubernetes bu kutuları pod olarak çalıştırır: Deployment ile istenen kopya sayısını sürdürür, Service ile kararlı bir adres verir, sağlık yoklamalarıyla sağlığı yönetir. HPA ile yatay ölçekler; kapanışta ise SIGTERM ile süre tanır ve gerekirse SIGKILL ile bitirir.

# k8s-1 | İleri
## Soru
Spring Boot uygulaman için Docker imajını küçültmek ve yeniden derlemeleri hızlandırmak istiyorsun. En etkili yaklaşım hangisidir?
## A
Bütün bağımlılıkları, JDK'yı ve uygulama kodunu tek bir büyük katmanda toplamak; böylece imajda tek katman kalır ve inşa adımı sadeleşir
🤖 tek katmanda en küçük kod değişikliği bile her şeyi yeniden inşa ettirir; katman önbelleği kullanılamaz ve imaj şişer.
## B
Çok-aşamalı derleme (multi-stage build), ince/distroless JRE taban imajı ve katmanlı (layered) jar ile bağımlılıkları koddan ayrı katmanlara koymak
🤖 Doğru: derleme araçları son imaja girmez, taban küçülür, kod değişince yalnız küçük üst katman yeniden inşa edilir.
## C
Son imaja tam JDK ile Maven/Gradle'ı da koymak; böylece gerekirse konteynerin içinde yeniden derleyip hata ayıklamak mümkün olur
🤖 çalışma zamanında derleme aracı gerekmez; imajı büyütür ve saldırı yüzeyini gereksiz yere genişletir.
## D
Taban imaj olarak her aracı içinde barındıran `latest` etiketli büyük bir imaj seçmek; böylece eksik bağımlılıkla hiç uğraşılmaz
🤖 büyük taban hem boyutu hem saldırı yüzeyini artırır; `latest` sürümü sabitlemediği için inşa yeniden-üretilebilir olmaz.
## Doğru
B
## Açıklama
İmaj optimizasyonunun üç ayağı vardır. (1) **Çok-aşamalı derleme**: derleme aşamasında JDK ve Maven/Gradle kullanılır, ama son imaja yalnız çalıştırılabilir çıktı kopyalanır; derleme araçları ve kaynak kod son imaja sızmaz — bu yüzden derleme araçlarını imajda tutmayı öneren şık yanlıştır. (2) **İnce taban imaj**: tam JDK yerine slim/distroless bir JRE; küçük boyut, az saldırı yüzeyi. Her aracı barındıran büyük bir taban seçmek bunun tam tersidir; üstelik `latest` etiketi sürümü sabitlemediği için aynı Dockerfile farklı zamanlarda farklı imaj üretir (yeniden-üretilebilirlik kaybı). (3) **Katmanlı (layered) jar**: bağımlılıklar (nadiren değişir) ile uygulama kodu (sık değişir) ayrı Docker katmanlarına konur; kod değişince yalnız küçük kod katmanı yeniden inşa edilir, büyük bağımlılık katmanı **önbellekten** gelir. Her şeyi tek katmanda toplamak bu önbelleği tamamen yok eder.

# k8s-2 | Orta
## Soru
Konteynerde çalışan bir Spring Boot uygulaması, pod'a 2 GiB bellek limiti verilmesine rağmen ara sıra `OOMKilled` oluyor. JVM heap'ini nasıl ayarlamak doğrudur?
## A
`-XX:MaxRAMPercentage` ile heap'i konteyner limitinin bir yüzdesi (ör. %75) olarak vermek, kalan payı heap dışı belleğe bırakmak
🤖 Doğru: modern JVM cgroup limitini okur; heap oran olarak verilince heap dışına yer kalır ve limit değişince orantılı ölçeklenir.
## B
`-Xmx2g` vererek heap'i konteyner limitine tam olarak eşitlemek; böylece pod'a verilen belleğin tamamı uygulamaya ayrılmış olur
🤖 metaspace, thread stack ve direct buffer da limite dahildir; heap limite eşitlenince toplam kullanım taşar ve OOMKilled sürer.
## C
Varsayılan ayarlarla bırakmak; JVM konteyner-farkında olduğu için heap dışı bellek dahil her şeyi limite sığdırmayı kendisi güvence altına alır
🤖 konteyner-farkındalık yalnız limiti okumayı sağlar; heap oranı ve heap dışı pay bilinçli ayarlanmazsa toplam yine limiti aşabilir.
## D
Pod'un bellek limitini tamamen kaldırmak; limit olmayınca konteyner OOMKilled olamaz ve uygulama ihtiyacı kadar bellek kullanır
🤖 limitsiz pod düğümün belleğini tüketip komşu pod'ları etkiler (noisy neighbor); düğüm bellek baskısına girince pod yine öldürülür.
## Doğru
A
## Açıklama
Konteynerin bellek hesabı yalnız JVM **heap**'inden ibaret değildir: metaspace, thread stack'leri, kod önbelleği ve direct/native buffer'lar da pod limitine dahildir. Heap'i `-Xmx2g` ile limite **eşitlemek** heap dışına hiç yer bırakmaz; toplam kullanım 2 GiB'i aşınca çekirdek konteyneri `OOMKilled` yapar. Doğru desen **`-XX:MaxRAMPercentage`** (ör. 75.0): modern JVM (Java 11+) **konteyner-farkındadır**, cgroup bellek limitini okur ve heap'i o limitin belirtilen yüzdesi kadar ayarlar — kalan pay heap dışı belleğe kalır, limit değiştiğinde heap de orantılı ölçeklenir. Konteyner-farkındalığın tek başına yeteceğini varsaymak yanlıştır: JVM limiti okur ama heap dışı tüketimin ne kadar olacağını senin yerine bilemez. Limiti kaldırmak da çözüm değildir; limitsiz pod düğümün belleğini tüketerek komşu pod'ları etkiler ve düğüm bellek baskısına girdiğinde pod tahliye edilir/öldürülür.

# k8s-3 | İleri
## Soru
Kubernetes'te **readiness** ve **liveness** probe arasındaki temel fark nedir?
## A
İkisi de başarısız olduğunda kubelet konteyneri yeniden başlatır; aralarındaki tek fark sorulan uç noktanın adıdır
🤖 yeniden başlatma kararını yalnız liveness verir; readiness başarısızlığı pod'u öldürmez, sadece trafiğini keser.
## B
Readiness başarısız olunca konteyner yeniden başlatılır; liveness başarısız olunca pod Service'in endpoint listesinden çıkarılır
🤖 roller ters verilmiş; yeniden başlatan liveness, endpoint listesinden çıkaran readiness'tır.
## C
Readiness pod'un CPU kullanımını, liveness ise bellek kullanımını ölçer; eşik aşılınca ikisi de pod'u yeniler
🤖 probe'lar kaynak ölçmez; belirlenen uç noktaya istek atar ya da komut çalıştırıp yanıtına bakar.
## D
Readiness başarısızsa pod Service'in endpoint listesinden çıkarılır ama öldürülmez; liveness başarısızsa kubelet konteyneri öldürüp yeniden başlatır
🤖 Doğru: readiness trafiği yönetir, liveness takılmış süreci yeniden başlatarak kurtarır.
## Doğru
D
## Açıklama
İki probe farklı **kararlar** verir. **Readiness probe** "bu pod şu an trafik alabilir mi?" sorusudur; başarısız olursa pod **öldürülmez**, yalnız `Service`'in uç nokta (endpoint) listesinden **çıkarılır** — pod çalışmaya devam eder, sadece yeni istek almaz (ör. açılışta henüz hazır değilken ya da geçici olarak aşırı yüklüyken). **Liveness probe** "süreç hâlâ sağlıklı mı, takılmadı mı?" sorusudur; başarısız olursa kubelet konteyneri **öldürüp yeniden başlatır** (deadlock'tan kurtarma). Rolleri eşitlemek ya da yer değiştirmek yanlıştır; probe'ları kaynak ölçümü sanmak da yanlıştır — probe bir HTTP/TCP isteği veya konteyner içinde çalıştırılan bir komuttur, CPU/bellek metriği değil. Tehlike: yavaş açılan bir uygulamaya agresif liveness koyarsan pod açılışını tamamlayamadan sürekli yeniden başlatılır; bunun için `startupProbe` vardır. Spring Boot bu uç noktaları `/actuator/health/{readiness,liveness}` olarak sunar.

# k8s-4 | Orta
## Soru
Kubernetes'te Pod, Deployment ve Service nesnelerinin rolleri nedir?
## A
Pod ile Deployment aynı nesnenin eski ve yeni adlarıdır; Service ise pod'ları oluşturup replika sayısını koruyan kontrolcüdür
🤖 Pod ve Deployment ayrı nesnelerdir; replika sayısını koruyan Deployment'tır, Service ise bir ağ soyutlamasıdır.
## B
Deployment en küçük dağıtım birimidir; Pod bu birimlerin replikalarını yöneten kontrolcüdür; Service ise imajların saklandığı depodur
🤖 en küçük birim Pod, replikaları yöneten Deployment'tır; imajlar Service'te değil kayıt defterinde (registry) saklanır.
## C
Pod en küçük ve geçici dağıtım birimidir; Deployment replika sayısını ve yuvarlanan güncellemeyi yürütür; Service kararlı IP/DNS verip trafiği dağıtır
🤖 Doğru: birim (Pod) → kontrolcü (Deployment) → kararlı ağ girişi (Service) katmanlaması.
## D
Service pod'ları oluşturur ve sayılarını korur; Deployment trafiği pod'lara yönlendirir; Pod ise kümede saklanan bir yapılandırma dosyasıdır
🤖 pod oluşturan Deployment, trafik yönlendiren Service'tir; Pod dosya değil, çalışan bir birimdir.
## Doğru
C
## Açıklama
Üç nesne bir katmanlama oluşturur. **Pod**: bir veya birkaç (yan-araç/sidecar) konteynerin **en küçük dağıtım birimi**; aynı pod'daki konteynerler ağ ad alanını ve IP'yi paylaşır. Pod geçicidir (ephemeral) — ölebilir, başka düğüme yeniden yerleştirilebilir, IP'si değişir. **Deployment**: bir **kontrolcü**; kaç replika Pod istendiğini bildirir, bunu ReplicaSet üzerinden sürdürür, arıza veya sürüm değişiminde pod'ları oluşturup değiştirir ve **yuvarlanan güncelleme** (rolling update) ile kesintisiz geçiş, gerekirse geri alma (rollback) sağlar. **Service**: ölümlü ve IP'si değişen pod'ların önünde **kararlı bir sanal IP/DNS adı**; istemciler Service'e konuşur, o da trafiği o an sağlıklı (readiness'tan geçmiş) pod'lara dağıtır. Rolleri birbirine karıştıran şıklar yanlıştır: Pod ile Deployment aynı şey değildir, Service ne pod üretir ne de imaj saklar (imajlar kayıt defterinde/registry durur), Pod ise bir dosya değil çalışan bir birimdir.

# k8s-5 | İleri
## Soru
Trafik arttıkça servisinin otomatik olarak daha fazla kopya (replika) ile çalışmasını istiyorsun. Hangi mekanizma bunu sağlar ve nasıl ölçekler?
## A
Horizontal Pod Autoscaler (HPA): bir metrik hedefine göre Deployment'ın replika sayısını otomatik artırıp azaltır, yani yatay ölçekler
🤖 Doğru: HPA ölçülen metriği hedefle karşılaştırıp kopya sayısını değiştirir; yük daha çok kopyaya yayılır.
## B
Vertical Pod Autoscaler ile tek pod'a giderek daha fazla CPU/bellek vermek; kaynak arttıkça kopya sayısı da kendiliğinden artar
🤖 dikey ölçekleme kopya sayısını değiştirmez; tek pod büyür, tek-nokta darboğazı ve düğüm sınırı sürer.
## C
Replika sayısını zirve trafiğe göre elle sabitleyip hiç değiştirmemek; otomatik davranışa en yakın güvenli yöntem budur
🤖 bu otomatik değil elle ölçeklemedir; zirveye göre sabitlemek boş saatlerde kaynağı boşuna harcar.
## D
Service'i `LoadBalancer` tipine çevirmek; yük dengeleyici trafiği görünce arkadaki pod sayısını otomatik artırır
🤖 Service tipi yalnız erişim biçimini belirler; hiçbir Service tipi pod üretmez veya replika sayısını değiştirmez.
## Doğru
A
## Açıklama
**Horizontal Pod Autoscaler (HPA)** gözlemlenen bir metriği (en yaygın: ortalama CPU kullanımı; ayrıca bellek veya özel/uygulama metrikleri) bir hedefle karşılaştırır ve Deployment'ın **replika sayısını** otomatik artırıp azaltır — yani yükü **daha fazla kopyaya yayar** (yatay / horizontal scaling). Bu, tek pod'a daha fazla kaynak vermekten (dikey / vertical scaling, VPA'nın işi) farklıdır: dikey ölçekleme kopya sayısını hiç değiştirmez, tek-nokta darboğazı ve tek düğüme sığma sınırı sürer. Replikayı zirveye göre elle sabitlemek otomatik ölçekleme değildir; boş saatlerde kaynak israfı, beklenmedik zirvede yetersizlik üretir. Service ise yalnız ağ girişidir: `ClusterIP`, `NodePort` veya `LoadBalancer` olması sadece pod'lara nasıl erişildiğini belirler, pod üretmez. Not: HPA'nın CPU yüzdesini hesaplayabilmesi için pod'ların kaynak **request** değerleri tanımlı olmalı ve uygulama **durumsuz** (stateless) ölçeklenebilir olmalıdır.

# k8s-6 | İleri
## Soru
Rolling update sırasında bazı istekler `Connection refused` / yarıda kesiliyor. Pod'un **graceful shutdown** (nazik kapanış) davranışı hakkında hangisi doğrudur?
## A
Kubernetes sonlandırma kararını verir vermez konteyneri doğrudan `SIGKILL` ile öldürür; istek kaybını yalnızca istemci tarafında yeniden deneme önleyebilir
🤖 önce `SIGTERM` gönderilir ve `terminationGracePeriodSeconds` kadar beklenir; `SIGKILL` sadece bu süre dolarsa gelir.
## B
`SIGTERM` gelir gelmez tüm açık bağlantılar anında koparılmalıdır; devam eden istekleri beklemek kapanışı geciktirdiği için hatalıdır
🤖 nazik kapanışın özü süregelen istekleri bitirmektir; anında koparmak kullanıcıya yarım kalmış istek/hata döndürür.
## C
Nazik kapanış yalnız veritabanı ve mesaj kuyruğu bağlantılarını kapatmak içindir; devam eden HTTP istekleriyle bir ilgisi yoktur
🤖 kapsam devam eden HTTP isteklerini de içerir; asıl amaç yanıtlanmamış isteği kaybetmemektir.
## D
Önce pod endpoint listesinden çıkarılır ve `preStop` çalışır, sonra `SIGTERM` gelir; grace period içinde süregelen istekler bitirilmeli, süre dolarsa `SIGKILL` gelir
🤖 Doğru: sıra endpoint'ten çıkarma ve preStop → SIGTERM → bekleme → gerekirse SIGKILL biçimindedir.
## Doğru
D
## Açıklama
Kubernetes bir pod'u sonlandırırken kademeli davranır. Silme isteğiyle birlikte pod **Service'in uç nokta (endpoint) listesinden çıkarılır** ve tanımlıysa **`preStop`** kancası çalışır; ardından konteynerin ana sürecine **`SIGTERM`** gönderilir ve **`terminationGracePeriodSeconds`** (varsayılan 30 sn) kadar beklenir. Süre dolarsa süreç **`SIGKILL`** ile zorla öldürülür — yani "anında SIGKILL" iddiası yanlıştır. Bu pencerede uygulama **nazik** kapanmalıdır: yeni istek almayı bırakmalı ama **devam eden istekleri bitirmeli**; bağlantıları anında koparmak yanıt bekleyen istemcilere hata döndürür. Spring Boot bunu `server.shutdown=graceful` ile sağlar ve kapsam yalnız veritabanı bağlantıları değildir, öncelikle süregelen HTTP istekleridir. Yarış koşulu: endpoint güncellemesi kümede asenkron yayıldığından pod SIGTERM aldıktan sonra da kısa süre yeni istek gelebilir; bu yüzden `preStop` kancasına küçük bir gecikme konur ve yük dengeleyici pod'u havuzdan çıkarana dek trafik kesilmemiş sayılır. `Connection refused` hataları genelde uygulamanın SIGTERM'i alır almaz portu kapatmasından doğar.
