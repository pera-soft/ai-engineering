---
slug: k8s-gozlem-sorun-giderme
baslik: "Kubernetes'te Sorun Giderme: Pod Durumları, Olaylar, Loglar ve Konteynerde JVM Teşhisi"
kademe: İleri
sira: 219
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Deneyimli bir doktor hastayı gördüğü anda tahlil istemez; belli bir sıra izler. Önce bakar (nabız, ateş), sonra geçmişi sorar ("ne zaman başladı, daha önce oldu mu?"), sonra tahlil ister, gerekirse görüntüleme yapar. Sıra atlanırsa hem zaman hem para gider, üstelik yanlış teşhis riski büyür. Kubernetes'te sorun gidermek de böyledir: `kubectl get` nabızdır, `kubectl describe` hastanın geçmişidir (**Events** bölümü!), `kubectl logs` tahlildir, `kubectl exec` ve hata ayıklama konteyneri ise ameliyathanedir. Gecenin üçünde gün sonu işi patladığında bu sırayı bilen kişi beş dakikada kök sebebe iner. Bilmeyen kişi ise pod'u siler, "bakalım düzelir mi" der ve elindeki tek kanıtı da yok eder.

### Temel kavramlar

- **`kubectl get pods`** — nabız. `READY` sütunu "kaç konteyner hazır / toplam" demektir; `1/1` değil de `0/1` görüyorsan konteyner **çalışıyor ama trafiğe hazır değil**. `STATUS` pod'un genel durumu, **`RESTARTS`** (yani `restartCount`) ise konteynerin kaç kez yeniden başlatıldığıdır — sıfırdan büyükse "bu konteyner daha önce öldü" demektir ve önceki konteynerin logu vardır.
- **`kubectl describe pod`** — hastanın geçmişi. Çıktının en altındaki **`Events`** bölümü, Kubernetes'in bu pod'a ne yaptığını anlatır: zamanlama başarısızlığı, imaj çekme hatası, sağlık yoklaması (probe) başarısızlıkları, öldürme, tahliye. Sağlık yoklaması, kubelet'in konteynere düzenli aralıklarla "ayakta mısın / hazır mısın?" diye sormasıdır. Ayrıca `Last State` altında önceki konteynerin **sonlanma sebebi (Reason)** ve **çıkış kodu (Exit Code)** yazar. Dikkat: olaylar kalıcı değildir, kümede varsayılan olarak yaklaşık **bir saat** sonra silinir — geç bakan kanıtı bulamaz.
- **`kubectl logs`** — tahlil. Varsayılan olarak **şu an çalışan** konteynerin standart çıktısını verir. **`--previous`** (kısaca `-p`) ise **ölmüş bir önceki** konteynerin logunu getirir; `CrashLoopBackOff` teşhisinin can damarı budur, çünkü asıl hata mesajı ölen örnekteydi. Çok konteynerli pod'da `-c <konteyner>` ile seçim yapılır; `--since` ve `-f` zaman ve takip içindir.
- **`kubectl exec -it … -- sh`** — içeri girmek. Küçük ya da **araçsız (distroless)** imajlarda kabuk ve yardımcı araç bulunmaz; araçsız imaj, içinde kabuk bile olmayan asgari imajdır. Böyle bir durumda **geçici hata ayıklama konteyneri (ephemeral debug container)** kullanılır. Komut şudur: `kubectl debug -it <pod> --image=<araçlı-imaj> --target=<konteyner>`. Bu komut, çalışan pod'a araç dolu bir konteyner ekler. `--target` sayesinde de yeni konteyner, hedef konteynerle aynı süreç ad alanını paylaşır; yani hedefin süreçlerini görebilir. Uygulama imajını şişirmeden teşhis yapmanın doğru yolu budur.
- **`kubectl port-forward`** — Service, Ingress ve yük dengeleyiciyi devreden çıkarıp **doğrudan tek bir pod'a** bağlanmak. "Servis mi bozuk, ağ yolu mu bozuk?" sorusunu ikiye böler; Actuator uç noktalarını yerelden yoklamak için de kullanılır.
- **`kubectl top pod` / `kubectl top node`** — anlık CPU/bellek kullanımı (metrics-server gerekir). Tek başına yeterli değildir; anlamlı olması için `describe` çıktısındaki `requests`/`limits` değerleriyle **karşılaştırılır**.

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kubernetes teşhis akışı: get, describe, logs, exec, port-forward, top">
  <defs>
    <marker id="k8sdbg-flow-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Teşhis sırası: ucuzdan pahalıya, dıştan içe</text>
  <rect x="16" y="44" width="120" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="76" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">get pods</text>
  <text x="76" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">READY · STATUS</text>
  <text x="76" y="99" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">RESTARTS</text>
  <rect x="152" y="44" width="130" height="66" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="217" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">describe pod</text>
  <text x="217" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">EVENTS (asıl kanıt)</text>
  <text x="217" y="99" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Last State · Exit Code</text>
  <rect x="298" y="44" width="130" height="66" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="363" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">logs</text>
  <text x="363" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ölen için: --previous</text>
  <text x="363" y="99" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çok konteyner: -c</text>
  <rect x="444" y="44" width="100" height="66" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="494" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">exec / debug</text>
  <text x="494" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kabuk yoksa</text>
  <text x="494" y="99" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">geçici konteyner</text>
  <rect x="560" y="44" width="86" height="66" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="603" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">port-forward</text>
  <text x="603" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tek pod'a</text>
  <text x="603" y="99" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">doğrudan</text>
  <path d="M138 77 L150 77" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#k8sdbg-flow-arw)"/>
  <path d="M284 77 L296 77" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#k8sdbg-flow-arw)"/>
  <path d="M430 77 L442 77" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#k8sdbg-flow-arw)"/>
  <path d="M546 77 L558 77" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#k8sdbg-flow-arw)"/>
  <text x="330" y="142" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Yan yol: kubectl top pod/node → anlık kullanımı describe'daki requests/limits ile karşılaştır</text>
  <text x="330" y="166" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Pod'u silmek teşhis değildir: kanıtı (log, Events, Last State) yok eder</text>
  <text x="330" y="190" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Olaylar kümede varsayılan olarak ~1 saat sonra silinir — kanıtı erken topla</text>
</svg>
```

### Nasıl çalışır?

1. **Durumu oku.** `kubectl get pods` ile `STATUS`, `READY` ve `RESTARTS` üçlüsüne bak; bu üçlü hangi hipotezle başlayacağını belirler.
2. **Olaylara in.** `kubectl describe pod` çıktısının Events bölümü çoğu vakayı tek başına çözer; `FailedScheduling`, `Failed to pull image`, `Liveness probe failed`, `Killing`, `Evicted` iletileri doğrudan kök sebebi söyler.
3. **Ölmüş örneğin logunu al.** `RESTARTS > 0` ise `kubectl logs --previous`; canlı logda görünmeyen açılış hatası oradadır.
4. **Ayrımı daralt.** `kubectl port-forward` ile pod'a doğrudan bağlanıp uygulamanın kendisi mi yoksa önündeki ağ yolu mu bozuk, ayır.
5. **İçeriden bak.** Gerekirse `kubectl exec`, kabuk yoksa `kubectl debug` ile geçici hata ayıklama konteyneri; JVM araçlarını burada çalıştır.
6. **Kalıcı kanıt bırak.** Heap dökümünü kalıcı bir birime yaz, logu merkeze taşı, metriği topla — pod öldüğünde kanıt da ölmesin.

### Yaygın hata durumları ve kök sebepleri

- **`Pending`** — pod hiç zamanlanamadı, konteyner **hiç oluşturulmadı** (bu yüzden log da yoktur). Sebepler sayılıdır. `requests` değerini karşılayan düğüm olmayabilir. Düğümdeki **itici işaret (taint)** tolere edilmiyor olabilir; taint, "bu düğüme özel izni olmayan pod gelmesin" demenin yoludur. **Düğüm yakınlığı (node affinity)** ya da topolojik dağıtım kısıtı sağlanamamış olabilir. Kalıcı birim talebi (PVC) bağlanmamış olabilir. Son olarak ad alanının **kaynak kotası (ResourceQuota)** dolmuş olabilir. Kanıt: Events'teki `FailedScheduling` iletisi ve gerekçesi.
- **`ImagePullBackOff` / `ErrImagePull`** — imaj çekilemiyor. Sebepler: etiket (tag) yanlış ya da o etiket kayıt defterinde yok, özel kayıt defteri için `imagePullSecret` tanımlı değil veya yetki yetersiz, ağdan kayıt defterine erişilemiyor. `latest` etiketi bu hatayı sinsileştirir: imaj altından değiştiğinde neyin çekildiği belirsizleşir.
- **`CrashLoopBackOff`** — konteyner başlıyor, ölüyor, kubelet **artan gecikmeyle** yeniden deniyor. **İki bambaşka kök sebebi vardır ve ayırt etmek şarttır.** (a) *Uygulama açılışta ölüyor*: eksik ortam değişkeni/Secret, yanlış yapılandırma, veritabanına bağlanamama, port çakışması. Kanıt `logs --previous` çıktısında bir yığın izi (stack trace) olarak durur. (b) *Uygulama sağlıklı ama "ayakta mısın" denetimi (liveness probe) çok agresif*: `initialDelaySeconds` küçük, `timeoutSeconds` kısa, açılış ise yavaş. Bu durumda **logda hata yoktur**, log açılış ortasında kesilir; kanıt Events'teki `Liveness probe failed` ve `Killing container` iletileridir. Çözümü `startupProbe` eklemektir.
- **`OOMKilled` ile `Evicted`** — `OOMKilled` **konteyner düzeyindedir**. Konteyner kendi bellek limitini aşmıştır ve çekirdek onu öldürür. `describe` çıktısında `Last State: Terminated`, `Reason: OOMKilled` ve **`Exit Code: 137`** görünür. Konteyner aynı düğümde yeniden başlar, `RESTARTS` sayısı artar. `Evicted` ise **pod düzeyindedir**: düğümün kaynağı bitmiştir. Kubelet pod'u düğümden çıkarır; pod `Failed` durumuna düşer ve başka bir düğümde yeniden yaratılır. Çıkış kodu okuması şöyledir: **137 = SIGKILL** (128+9), **143 = SIGTERM** (128+15), **1** ise genelde uygulamanın kendi hatasıdır.
- **`Init:0/1`, `Init:Error`, `Init:CrashLoopBackOff`** — init konteyner tamamlanmadan ana konteyner başlamaz; sorun ana uygulamada değil, ondan önce çalışan hazırlık adımındadır (şema göçü, yapılandırma çekme). Burada **yerel yan konteyner (native sidecar)** de devreye girer; bu, `restartPolicy: Always` verilmiş bir init konteynerdir. Yan konteyner (sidecar), ana konteynerin yanında aynı pod içinde koşan yardımcı konteynerdir. Kubernetes 1.29'da beta olarak tanıtılmış, sonraki sürümlerde kararlı hâle gelmiştir. Böyle bir yan konteyner hazır duruma gelmeden ana konteyner beklemede kalır.
- **`Running` ama `READY 0/1`** — konteyner ayakta, ama **"hazır mısın" denetimi (readiness probe) başarısız**. Pod Service'in uç nokta listesine alınmaz; sağlıklı arka uç kalmayınca gateway/Ingress istemciye **503** döndürür. Uygulama logu tertemiz olabilir; kanıt Events'teki `Readiness probe failed` iletisidir. Restart olmadığı için `RESTARTS` da 0 kalır — bu, `OOMKilled` ve `ImagePullBackOff` hipotezlerini eleyen en güçlü ipucudur.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Pod durumları ve kök sebepleri haritası">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Durum → nereye bak → tipik kök sebep</text>
  <rect x="20" y="40" width="620" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="34" y="58" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Pending</text>
  <text x="34" y="74" font-size="10.5" fill="#5D7789" font-family="system-ui">describe → Events: FailedScheduling</text>
  <text x="330" y="66" font-size="10.5" fill="#1C3247" font-family="system-ui">kaynak yok · taint · topoloji kısıtı · PVC bağlanmadı · kota doldu</text>
  <rect x="20" y="90" width="620" height="42" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="34" y="108" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">ImagePullBackOff</text>
  <text x="34" y="124" font-size="10.5" fill="#5D7789" font-family="system-ui">describe → Events: Failed to pull</text>
  <text x="330" y="116" font-size="10.5" fill="#1C3247" font-family="system-ui">etiket yok/yanlış · imagePullSecret eksik · kayıt defterine erişim yok</text>
  <rect x="20" y="140" width="620" height="46" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="34" y="158" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">CrashLoopBackOff</text>
  <text x="34" y="174" font-size="10.5" fill="#5D7789" font-family="system-ui">logs --previous + Events</text>
  <text x="330" y="160" font-size="10.5" fill="#1C3247" font-family="system-ui">(a) logda yığın izi var → uygulama açılışta ölüyor (config/Secret/DB)</text>
  <text x="330" y="176" font-size="10.5" fill="#1C3247" font-family="system-ui">(b) log temiz + &quot;Liveness probe failed&quot; → agresif liveness, çözüm startupProbe</text>
  <rect x="20" y="194" width="303" height="44" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="34" y="212" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">OOMKilled (Exit 137)</text>
  <text x="34" y="230" font-size="10.5" fill="#5D7789" font-family="system-ui">konteyner kendi limitini aştı · aynı düğümde yeniden başlar</text>
  <rect x="337" y="194" width="303" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="351" y="212" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Running ama READY 0/1</text>
  <text x="351" y="230" font-size="10.5" fill="#5D7789" font-family="system-ui">readiness başarısız · endpoint'e girmez · istemci 503 görür</text>
</svg>
```

### Konteynerde JVM teşhisi

- **Bellek ayarı.** Konteynerde `-Xmx` yerine **`-XX:MaxRAMPercentage`** kullanılır. Modern JVM, çekirdeğin koyduğu kaynak sınırını (cgroup limiti) okur ve **yığın belleğini (heap)** o limitin yüzdesi kadar ayarlar. Geri kalan pay **yığın dışı** tüketime kalır: sınıf üstverisi (metaspace), iş parçacığı yığınları (thread stack), doğrudan tampon bellek (direct buffer). Yığın belleğini limite eşitlemek `OOMKilled` üretir.
- **Heap dökümü (heap dump) ve kaybolma tuzağı.** `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=…` bayrakları JVM `OutOfMemoryError` attığında döküm yazar. Ama iki incelik vardır. (1) Döküm konteynerin **geçici yazma katmanına** yazılırsa konteyner yeniden başladığında **kaybolur**; kalıcı bir birime (PVC) ya da paylaşılan bir birime yazılmalı, sonra dışarı taşınmalıdır. (2) Bellek **cgroup limitini** aşıp konteyner `OOMKilled` olduysa süreci çekirdek öldürür; JVM `OutOfMemoryError` bile atmayabilir, dolayısıyla **kanca hiç çalışmaz** ve döküm oluşmaz. Ayrıca döküm dosyası kabaca heap boyutu kadar yer kaplar; disk ve süre buna göre planlanmalıdır.
- **Canlı teşhis.** Süreç ayaktayken üç komut işini görür. `jcmd <pid> Thread.print` (ya da `jstack`) iş parçacığı yığınlarını gösterir. `jcmd <pid> GC.heap_info` yığın belleğinin durumunu verir. `jcmd <pid> VM.native_memory` ise yığın dışı tüketimi gösterir; bunun için yerel bellek takibinin açık olması gerekir. Küçük ya da araçsız (distroless) imajlarda bu araçlar bulunmaz. O zaman `kubectl debug --target=<konteyner>` ile araç dolu bir **geçici konteyner** eklenir; aynı süreç ad alanı sayesinde hedef JVM'in süreç kimliği görülür. Alternatifi, pod'da `shareProcessNamespace` kullanmaktır.
- **Actuator üzerinden.** Spring Boot `/actuator/threaddump` ve `/actuator/heapdump` uç noktalarını sunar; `kubectl port-forward` ile yerelden alınabilir. Üretimde bu uç noktalar ayrı bir yönetim portunda ve yetkilendirmenin arkasında tutulur — heap dökümü müşteri verisi içerir.

### Gözlemlenebilirliğin üç sütunu kümede

- **Log.** Uygulama dosyaya değil **standart çıktıya (stdout/stderr)** yazar; düğümdeki toplayıcı (ör. Fluent Bit) alıp merkezî sisteme taşır. `kubectl logs` yalnız düğümde duran ve döndürülmemiş (rotate) kısmı gösterir, **pod silinince log kaybolur** — bu yüzden merkezî loglama seçenek değil zorunluluktur.
- **Metrik.** Actuator/Micrometer ölçümleri `/actuator/prometheus` uç noktasından yayınlar, Prometheus bunları belirli aralıklarla **kazır (scrape)**. Metrik "kaç, ne kadar, ne kadar sürede" sorusunu ucuza cevaplar.
- **İz (trace).** İstek zincirinde taşınan **iz kimliği (trace id)** — W3C `traceparent` başlığı — hem loglara hem izleme sistemine yazılır; üç sütun birbirine bu kimlikle bağlanır.
- **Kardinalite patlaması (cardinality explosion).** Kardinalite, bir metriğin kaç farklı etiket değeri kombinasyonu ürettiğidir. Prometheus'ta bir metriğin **her benzersiz etiket değeri kombinasyonu ayrı bir zaman serisidir**. Etiketlere `musteriNo`, `basvuruId`, ham istek yolu ya da pod adı gibi **sınırsız değerli** alanlar koymak seri sayısını çarpar: bellek şişer, sorgular yavaşlar, sistem çöker. Kural: etiket değerleri **sınırlı ve öngörülebilir** bir kümeden gelmelidir — HTTP metodu, durum kodu, sonuç (`basarili`/`reddedildi`), uç nokta **şablonu** (`/krediler/{id}`, ham yol değil). Yüksek kardinaliteli bilgi metriğe değil **loga ve ize** yazılır; orada tekil kayıt aramak zaten normaldir. Not: pod/düğüm adı gibi altyapı etiketlerini toplama katmanı zaten ekler, uygulama metriğine ikinci kez koymak boş yere çarpan üretir.

```svg
<svg viewBox="0 0 660 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Metrik etiketlerinde kardinalite patlaması">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Etiket seçimi = zaman serisi sayısı</text>
  <rect x="20" y="42" width="300" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="170" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">İYİ: sınırlı değer kümesi</text>
  <text x="170" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">metod (5) × durum kodu (6)</text>
  <text x="170" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">× uç nokta şablonu (20)</text>
  <text x="170" y="136" text-anchor="middle" font-size="16" font-weight="700" fill="#25804C" font-family="system-ui">≈ 600 seri</text>
  <text x="170" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">öngörülebilir, sabit üst sınır</text>
  <text x="170" y="180" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">/krediler/{id} — ham yol DEĞİL</text>
  <rect x="340" y="42" width="300" height="150" rx="12" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="490" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">KÖTÜ: sınırsız değer</text>
  <text x="490" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yukarıdakiler × musteriNo (1M)</text>
  <text x="490" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">× basvuruId × podAdi</text>
  <text x="490" y="136" text-anchor="middle" font-size="16" font-weight="700" fill="#B23B3B" font-family="system-ui">≈ sınırsız seri</text>
  <text x="490" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Prometheus belleği şişer, sorgu yavaşlar</text>
  <text x="490" y="180" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">daha büyük sunucu yalnız erteler</text>
  <text x="330" y="216" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Kural: tekil kimlikler metriğe değil, LOG ve İZ'e yazılır</text>
  <text x="330" y="234" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">üç sütun birbirine iz kimliği (trace id) ile bağlanır</text>
</svg>
```

### Mülakatta ne sorulur / tuzaklar

- **"Pod `CrashLoopBackOff`, ne yaparsın?"** Ezber cevap ("logs bakarım") yetmez. Beklenen: `logs --previous` ile **ölmüş** örneğin logu + `describe` ile Events ve `Last State`. Asıl puan, iki kök sebebi ayırmakta: logda yığın izi varsa uygulama hatası, log temiz ve Events'te `Liveness probe failed` varsa **agresif probe** — çözüm `startupProbe`.
- **"Pod `Pending`, logda ne var?"** Tuzak sorudur: **log yoktur**, çünkü konteyner hiç oluşmadı. Cevap `describe` ve `FailedScheduling` olmalı.
- **"`OOMKilled` ile `Evicted` farkı?"** Biri konteyner limitini aşmaktır (çekirdek öldürür, `Exit Code: 137`, aynı düğümde yeniden başlar), diğeri düğüm kaynağının bitmesidir (kubelet çıkarır, pod başka düğüme gider). Karıştıran kişi yanlış yerde çözüm arar: birinde limit/heap ayarı, diğerinde düğüm kapasitesi ve `requests` disiplini gerekir.
- **"İstemci 503 alıyor ama pod `Running`."** Beklenen refleks `READY` sütununa bakmaktır. `0/1` ise readiness başarısızdır; pod uç nokta listesine girmemiştir. `RESTARTS`'ın 0 olması `OOMKilled` ihtimalini eler.
- **"Konteynerde heap dökümü nasıl alırsın?"** Bayrağı söylemek yarım cevaptır. Tam cevap iki tuzağı içerir: döküm **kalıcı bir birime** yazılmazsa pod ölünce kaybolur; `OOMKilled` durumunda JVM kancası **hiç çalışmayabilir** çünkü süreci çekirdek öldürür.
- **"İmajda kabuk yok, nasıl hata ayıklarsın?"** `kubectl debug` ile geçici konteyner. "Uygulama imajına araç ekleyelim" demek üretim imajını şişirmek ve saldırı yüzeyini büyütmektir.
- **"Metrik etiketine ne koyarsın?"** Kardinaliteyi bilmeyen aday buraya düşer. Müşteri numarası, başvuru kimliği, ham URL ve pod adı **etiket olmaz**; bunlar log ve ize gider. Etiket kümesi sınırlı ve öngörülebilir olmalıdır.
- **"Uygulama logunu nereye yazarsın?"** Standart çıktıya. Konteyner içindeki dosyaya yazan uygulamanın logu pod ölünce kaybolur; ayrıca toplayıcı onu göremez.

### Tek cümlede

Kubernetes'te sorun gidermek sıralı bir teşhistir: `get` ile `STATUS`/`READY`/`RESTARTS` üçlüsünü oku, `describe` ile Events ve `Last State`'e in, ölen konteyner için `logs --previous` al. Gerekirse `port-forward` ve geçici hata ayıklama konteyneriyle içeriden bak. JVM tarafında yığın dökümünü kalıcı birime yaz ve `OOMKilled` durumunda JVM kancasının hiç çalışmayabileceğini bil. Kalıcı gözlemlenebilirlik için log stdout'tan merkeze, metrik Actuator'dan Prometheus'a gitmeli; etiketlere ise asla sınırsız değerli alan koyma.

# k8sdbg-1 | Orta
## Soru
Kredi limit servisinin pod'u `CrashLoopBackOff` durumunda ve `RESTARTS` sütunu 7 gösteriyor. `kubectl logs <pod>` komutunu çalıştırdığında ise çıktı boş ya da yalnız açılışın ilk satırları geliyor. Ölüm sebebini bulmak için doğru adım hangisidir?
## A
Pod'u silip Deployment'ın yenisini oluşturmasını beklemek ve baştan `kubectl logs -f` ile izlemek; sorun tekrar ederse aynı log yeniden görülür
🤖 pod'u silmek Events ve `Last State` gibi kanıtları yok eder; üstelik backoff zaten sürekli yeni deneme yapmaktadır.
## B
`kubectl logs --previous` ile ölmüş bir önceki konteynerin logunu almak ve `kubectl describe pod` çıktısındaki Events ile `Last State` bölümünü okumak
🤖 Doğru: hata mesajı ölen konteynerin logundadır ve sonlanma sebebi ile çıkış kodu `describe` çıktısında durur.
## C
`kubectl exec` ile konteynerin içine girip uygulamanın yazdığı log dosyalarını okumak ve yığın izini oradan çıkarmak
🤖 sürekli ölen konteynere `exec` tutunamaz ve konteyner logu dosyaya değil standart çıktıya yazılır.
## D
`kubectl top pod` ile anlık CPU ve bellek kullanımına bakıp kaynak yetersizliğini doğrulamak
🤖 `top` yalnız o anki kullanımı gösterir, ölmüş bir konteynerin sonlanma sebebini vermez.
## Doğru
B
## Açıklama
`CrashLoopBackOff` "konteyner başlıyor, ölüyor, kubelet artan gecikmeyle yeniden deniyor" demektir. Bu yüzden `kubectl logs` **şu an ayakta olan** (ve muhtemelen henüz hiçbir şey yazmamış) konteyneri gösterir; asıl hata mesajı **ölen** örneğin logundadır ve ona **`kubectl logs --previous`** ile ulaşılır. Bunun yanında `kubectl describe pod` iki kritik bilgi verir: en alttaki **Events** bölümü (`Liveness probe failed`, `Killing container`, `Back-off restarting failed container`) ve konteynerin **`Last State`** bilgisi — sonlanma sebebi (ör. `OOMKilled`) ile **çıkış kodu** (137 = SIGKILL, 143 = SIGTERM, 1 = uygulama hatası). Pod'u silmek en kötü seçenektir: elindeki tek kanıtı — önceki konteynerin logunu ve pod'a bağlı olayları — yok eder, üstelik yeniden başlatma zaten kendiliğinden oluyordu. `exec` de işe yaramaz; sürekli ölen bir konteynerde oturum açık kalmaz ve konteyner logu tanım gereği dosyada değil standart çıktıdadır. `kubectl top` ise anlık bir ölçümdür, geçmişe dair sonlanma sebebi tutmaz.

# k8sdbg-2 | Orta
## Soru
Gün sonu (EOD) mutabakat işini çalıştıran pod dakikalardır `Pending` durumunda bekliyor. `kubectl logs` denediğinde "konteyner henüz oluşturulmadı" anlamında bir hata alıyorsun. Sorunun kaynağını nerede ararsın?
## A
`kubectl logs --previous` ile önceki konteynerin logunu almak; `Pending` durumunda da bir önceki deneme kaydı orada tutulur
🤖 `Pending` pod'da hiç konteyner oluşturulmadığı için ne güncel ne de önceki bir konteyner logu vardır.
## B
Liveness probe ayarlarını gevşetip `initialDelaySeconds` değerini büyütmek; yavaş açılan iş pod'ları bu yüzden beklemede kalır
🤖 probe'lar ancak konteyner çalışmaya başladıktan sonra devreye girer; zamanlanmamış pod'da hiç çalışmazlar.
## C
İmaj etiketini `latest` yapıp imajın yeniden çekilmesini zorlamak; çekme takıldığında pod `Pending` durumunda kalır
🤖 imaj çekme sorunu `Pending` değil `ImagePullBackOff`/`ErrImagePull` olarak görünür, üstelik `latest` sürümü sabitlemez.
## D
`kubectl describe pod` çıktısındaki Events bölümünde `FailedScheduling` iletisini ve gerekçesini okumak
🤖 Doğru: `Pending` bir zamanlama sorunudur ve zamanlayıcının pod'u neden yerleştiremediği doğrudan Events'te yazar.
## Doğru
D
## Açıklama
`Pending`, pod'un **henüz hiçbir düğüme yerleştirilemediği** anlamına gelir; konteyner oluşturulmadığı için log da yoktur — ne güncel ne `--previous`. Doğru adres `kubectl describe pod` çıktısının **Events** bölümündeki **`FailedScheduling`** iletisidir; gerekçe orada açıkça yazar: `requests` değerini karşılayacak boş kaynağı olan düğüm yok, düğümdeki **taint** tolere edilmiyor, node affinity veya topolojik dağıtım kısıtı sağlanamıyor, kalıcı birim talebi (PVC) bağlanmamış, ya da ad alanının **kaynak kotası (ResourceQuota)** dolmuş. Probe ayarlarıyla uğraşmak boşunadır: readiness, liveness ve startup ancak konteyner çalışmaya başladıktan sonra işletilir, zamanlanmamış pod'da hiç devreye girmezler. İmaj etiketiyle oynamak da yanlış hipotezdir; imaj çekme aşamasına gelinmiş olsaydı durum `Pending` değil **`ImagePullBackOff`** olurdu — ayrıca `latest` etiketi sürümü sabitlemediği için ayrı bir sorun kaynağıdır. Kural olarak `Pending` "yer bulunamadı", `ImagePullBackOff` "imaj alınamadı", `CrashLoopBackOff` "başladı ama yaşayamadı" demektir; bu üçünü ayırmak teşhisin ilk adımıdır.

# k8sdbg-3 | İleri
## Soru
Bir servis pod'u `CrashLoopBackOff`'ta. `kubectl logs --previous` çıktısında hiçbir hata yok; açılış logları normal ilerliyor ve ortada kesiliyor. `kubectl describe pod` çıktısındaki Events bölümünde ise `Liveness probe failed` ve `Killing container` iletileri var. En doğru düzeltme hangisidir?
## A
Gerçek açılış süresini ölçüp `startupProbe` tanımlamak, böylece liveness ancak uygulama ayağa kalktıktan sonra devreye girmek üzere ertelenmiş olur
🤖 Doğru: belirti tam olarak yavaş açılışa konmuş agresif liveness'tır ve `startupProbe` bu iki durumu birbirinden ayırmak için vardır.
## B
Konteynerin bellek limitini iki katına çıkarmak; uygulama açılış sırasında belleğe sığmadığı için sonlandırılmış olabilir
🤖 bellek sorunu olsaydı `Last State` sebebi `OOMKilled` ve çıkış kodu 137 olurdu, oysa kanıt probe başarısızlığını gösteriyor.
## C
Liveness probe'u veritabanı bağlantısını da kontrol eden derin bir sağlık uç noktasına bağlayarak gerçek sağlığı ölçmek
🤖 liveness'ı dış bağımlılığa bağlamak veritabanı yavaşladığında bütün pod'ları topluca yeniden başlatır; bağımlılık kontrolü readiness'ın işidir.
## D
Pod'un `restartPolicy` değerini `Never` yapmak; yeniden başlatma döngüsü kesilince uygulama açılışını tamamlayabilir
🤖 Deployment'ın yönettiği pod'larda `restartPolicy` `Always` olmak zorundadır ve döngüyü kesmek kök sebebi düzeltmez, yalnız gizler.
## Doğru
A
## Açıklama
Kanıtlar tek bir hipotezi işaret ediyor. `logs --previous` çıktısında **yığın izi yok** ve log açılışın ortasında kesiliyor: yani uygulama kendi hatasıyla ölmedi, **dışarıdan öldürüldü**. Events'teki `Liveness probe failed` ve `Killing container` bunu doğruluyor: liveness eşiği, uygulamanın gerçek açılış süresinden kısa. Bu, `CrashLoopBackOff`'un iki kök sebebinden **ikincisidir** — birincisi (yanlış yapılandırma, eksik Secret, veritabanına bağlanamama) logda yığın izi bırakırdı. Doğru çözüm **`startupProbe`**'dur: açılış süresince liveness ve readiness askıya alınır, uygulama hazır olduğunda liveness normal (ve sıkı) eşikleriyle devreye girer; alternatifi liveness'ın `initialDelaySeconds`/`failureThreshold` değerlerini gerçek açılışa göre büyütmektir ama bu, çalışma anındaki takılmaların da geç fark edilmesi demektir. Bellek limitini artırmak yanlış hipotezdir; `OOMKilled` olsaydı `describe` çıktısında `Last State: Terminated, Reason: OOMKilled` ve `Exit Code: 137` görülürdü. Liveness'ı veritabanı kontrolüne bağlamak klasik ve tehlikeli bir hatadır: veritabanı yavaşladığında sağlıklı konteynerler topluca yeniden başlatılır ve arıza büyür — dış bağımlılık kontrolü **readiness**'a aittir. `restartPolicy: Never` ise Deployment pod'larında geçerli değildir ve olsa bile teşhis değil, belirtiyi susturmaktır.

# k8sdbg-4 | İleri
## Soru
Yeni sürüm yayınlandıktan sonra istemciler gateway üzerinden `503` almaya başladı. `kubectl get pods` çıktısında pod'lar `Running`, ama `READY` sütunu `0/1` ve `RESTARTS` sütunu `0`. Uygulama logunda da hata yok. En olası sebep nedir?
## A
Konteyner bellek limitini aşıp `OOMKilled` olmuştur; limitin ve JVM heap oranının artırılması gerekir
🤖 `OOMKilled` olsaydı konteyner yeniden başlatılır, `RESTARTS` artar ve `Last State` sebebi `OOMKilled` görünürdü.
## B
Pod düğüm kaynak baskısı nedeniyle tahliye edilmiştir (`Evicted`); düğüm kapasitesinin büyütülmesi gerekir
🤖 tahliye edilen pod `Running` kalmaz, `Failed` durumuna düşer ve başka bir düğümde yeniden yaratılır.
## C
Readiness probe başarısız olduğu için pod Service'in uç nokta listesine alınmıyor; sağlıklı arka uç kalmayınca gateway `503` döndürüyor
🤖 Doğru: `Running` + `READY 0/1` + `RESTARTS 0` üçlüsü tam olarak readiness başarısızlığının imzasıdır.
## D
İmaj kayıt defterinden çekilememiştir; yeni sürümün etiketi ya da çekme yetkisi hatalıdır
🤖 imaj çekilemeseydi pod `Running` değil `ImagePullBackOff` durumunda olurdu.
## Doğru
C
## Açıklama
Üç gösterge birlikte okunduğunda tek bir sonuç kalır. **`Running`** konteynerin ayakta olduğunu, **`READY 0/1`** hazır sayılmadığını, **`RESTARTS 0`** ise hiç yeniden başlatılmadığını söyler. Yeniden başlatma olmaması `OOMKilled` hipotezini eler — bellek limiti aşılsaydı çekirdek konteyneri öldürür, `RESTARTS` artar ve `describe` çıktısında `Last State: Terminated, Reason: OOMKilled, Exit Code: 137` görülürdü. `Evicted` de olamaz; tahliye edilen pod `Running` kalmaz, `Failed` durumuna düşüp başka düğümde yeniden yaratılır. `ImagePullBackOff` ise konteynerin hiç başlamadığı bir durumdur, `Running` ile bağdaşmaz. Geriye **readiness başarısızlığı** kalır: pod yaşıyor ama "trafik alabilirim" diyemiyor, bu yüzden Service'in **uç nokta (endpoint) listesine** alınmıyor; arkasında sağlıklı hiçbir pod kalmayan gateway/Ingress de istemciye **503** döndürüyor. Uygulama logunun temiz olması şaşırtıcı değildir; kanıt `kubectl describe pod` çıktısındaki **`Readiness probe failed`** olayındadır. Tipik sebepler: yeni sürümde readiness uç noktasının yolu değişmiş, sağlık kontrolü henüz hazır olmayan bir bağımlılığı yokluyor ya da `timeoutSeconds` çok kısa. Doğrulamanın hızlı yolu `kubectl port-forward` ile pod'a doğrudan bağlanıp readiness uç noktasını elle yoklamaktır.

# k8sdbg-5 | İleri
## Soru
Konteynerde `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/dump.hprof` tanımlı olmasına rağmen, pod bellek sorunundan sonra yeniden başladığında döküm dosyası hiçbir yerde bulunamıyor. Doğru teşhis ve çözüm nedir?
## A
Bu bayraklar modern JVM sürümlerinde kaldırılmıştır; döküm alabilmek için bunların yerine gelen çıkışta döküm alma bayrağının tanımlanması gerekir
🤖 bu bayraklar güncel JVM'de geçerlidir ve onların yerini alan bir çıkışta-döküm bayrağı yoktur.
## B
Döküm konteynerin geçici yazma katmanına yazılıp yeniden başlatmada siliniyor; kalıcı bir birime yazılmalı, ayrıca cgroup limiti aşılıp `OOMKilled` olunduysa JVM kancası hiç çalışmaz
🤖 Doğru: konteynerin dosya sistemi geçicidir ve süreci çekirdek öldürdüğünde JVM `OutOfMemoryError` atmadığı için döküm hiç üretilmez.
## C
Döküm dosyası otomatik olarak konteynerin standart çıktısına aktarılır; dosya aramak yerine `kubectl logs --previous` çıktısının incelenmesi gerekir
🤖 heap dökümü ikili (binary) bir dosyadır ve hiçbir koşulda standart çıktıya yazılmaz.
## D
Heap dökümü yalnız `jcmd` ile canlı süreçten alınabilir; JVM bayrakları konteyner ortamında cgroup kısıtları yüzünden hiçbir etki üretmez
🤖 bayraklar konteynerde de çalışır; `jcmd` bunların alternatifi değil, canlı teşhis için ek bir yoldur.
## Doğru
B
## Açıklama
İki ayrı tuzak üst üste binmiş durumda. Birincisi **kalıcılık**: `/tmp` konteynerin yazılabilir geçici katmanındadır ve konteyner yeniden başlatıldığında bu katman sıfırlanır — döküm üretilmiş olsa bile kaybolur. Çözüm dökümü kalıcı bir birime (PVC) ya da paylaşılan bir birime yazıp oradan dışarı taşımaktır; ayrıca döküm dosyası kabaca heap boyutu kadar yer kapladığı için disk ve süre buna göre planlanmalıdır. İkincisi ve daha sinsi olanı: bayrak yalnızca **JVM `OutOfMemoryError` attığında** çalışır. Konteynerin **cgroup bellek limiti** aşıldığında ise süreci çekirdek öldürür (`OOMKilled`, `Exit Code: 137`); JVM böyle bir durumda hiç `OutOfMemoryError` atmayabilir, dolayısıyla **kanca hiç tetiklenmez** ve döküm oluşmaz. Bu ayrım heap'in limitten yeterince küçük tutulmasını (`-XX:MaxRAMPercentage` ile heap dışına pay bırakmayı) neden önemli kıldığını da açıklar. Diğer şıklar yanlıştır: bu bayraklar güncel JVM'de geçerlidir, heap dökümü ikili bir dosyadır ve asla standart çıktıya yazılmaz, `jcmd` ise bayrakların yerini almaz — canlı süreçten elle döküm/iş parçacığı dökümü almak için kullanılan **tamamlayıcı** bir yoldur ve küçük imajlarda `kubectl debug` ile eklenen geçici konteyner üzerinden çalıştırılır.

# k8sdbg-6 | Uzman
## Soru
Prometheus örneğinin bellek tüketimi haftalar içinde katlandı, sorgular yavaşladı ve panolar zaman aşımına uğramaya başladı. İncelendiğinde ekibin `kredi_basvuru_islem_toplam` sayacına `musteriNo`, `basvuruId` ve `podAdi` etiketlerini eklediği görülüyor. Sorun ve doğru düzeltme nedir?
## A
Her benzersiz etiket değeri kombinasyonu ayrı bir zaman serisi ürettiği için sınırsız değerli alanlar seri sayısını patlatıyor; bu alanlar metrikten çıkarılıp log ve ize taşınmalı
🤖 Doğru: maliyet örnek sıklığında değil seri sayısındadır ve tekil kimlikler metriğin değil log ile izin alanıdır.
## B
Sorun kazıma (scrape) aralığının çok kısa olmasıdır; aralık 15 saniyeden 60 saniyeye çıkarılırsa bellek tüketimi ve sorgu süresi normale döner
🤖 aralık yalnız seri başına örnek sıklığını değiştirir, asıl maliyet olan seri sayısına dokunmaz.
## C
Sorun metrik tipinin yanlış seçilmesidir; sayaç (counter) yerine histogram kullanılırsa aynı bilgi çok daha az yer kaplar
🤖 histogram her kova (bucket) için ek seri ürettiğinden aynı etiketlerle durumu daha da kötüleştirir.
## D
Etiketler olduğu gibi kalmalıdır; Prometheus daha çok belleği olan bir sunucuya taşınıp saklama süresi kısaltılarak sorun çözülür
🤖 kardinalite müşteri sayısıyla birlikte sınırsız büyüdüğü için donanım eklemek çözmez, yalnız çöküşü erteler.
## Doğru
A
## Açıklama
Prometheus'ta bir metriğin kimliği, adı ile **etiket değerlerinin kombinasyonudur**; her benzersiz kombinasyon ayrı bir **zaman serisi** yaratır ve bellek maliyeti büyük ölçüde seri sayısıyla orantılıdır. `musteriNo` ve `basvuruId` gibi alanlar **sınırsız değerlidir**: müşteri ve başvuru sayısı arttıkça seri sayısı da sınırsız büyür — buna **kardinalite patlaması** denir. `podAdi` de aynı sınıftadır ve iki kat gereksizdir, çünkü pod/düğüm etiketlerini toplama katmanı zaten kendisi ekler. Doğru düzeltme yüksek kardinaliteli alanları metrikten çıkarmak, etiket kümesini **sınırlı ve öngörülebilir** değerlerle sınırlamaktır: HTTP metodu, durum kodu, sonuç (`onaylandi`/`reddedildi`), uç nokta **şablonu** (`/krediler/{id}` — ham yol değil). Tekil kimliklerle arama ihtiyacı ortadan kalkmaz, ama onun doğru yeri **log ve izdir**; üç sütun birbirine **iz kimliği (trace id)** ile bağlandığı için metrikte bir anormallik görüp ize ve loga inmek zaten mümkündür. Kazıma aralığını uzatmak yalnız seri başına örnek sayısını azaltır, seri sayısına dokunmadığı için kök sebebi çözmez. Histogram'a geçmek daha da kötüdür: her kova ayrı seri ürettiğinden aynı etiketlerle çarpan büyür. Donanım eklemek de yalnız erteleme yöntemidir; sınırsız büyüyen bir kardinaliteyi hiçbir sunucu boyutu yakalayamaz.
