---
slug: k8s-ag-servis-ingress
baslik: "Kubernetes Ağı: Service Tipleri, DNS, Ingress ve NetworkPolicy"
kademe: İleri
sira: 217
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Büyük bir banka genel müdürlük binasını düşün. İçeride her odanın kendi dahili numarası vardır ve odalar birbirini santral üzerinden doğrudan arayabilir — dışarıya çıkmaya gerek yoktur. Ama "Kredi Tahsis" diye bir birim ararsan, o birimin odası taşınmış olabilir; bu yüzden telefon rehberinde odanın numarası değil, **birimin adı** yazar ve santral seni o an o birimde oturan kişiye bağlar. Dışarıdan gelen müşteriler binaya rastgele girmez: tek bir **danışma bankosu** vardır, kimin hangi kata gideceğine orası karar verir ve kimlik kontrolü de orada yapılır. Son olarak, "herkes her odaya girebilir" varsayımı bir bankada kabul edilemez; kasa dairesine yalnız belirli birimler girebilsin diye **kat kartı kuralları** yazılır. Kubernetes ağında bunların karşılıkları sırasıyla **pod IP'leri**, **Service ve CoreDNS**, **Ingress** ve **NetworkPolicy**'dir.

### Temel kavramlar

- **Küme ağ modeli:** Kubernetes'in temel şartı basittir: **her pod'un kendi IP adresi vardır**. Herhangi bir pod, kümedeki herhangi bir pod'a **adres dönüştürme (NAT) olmadan** doğrudan ulaşabilir; yani arada adresi değiştiren bir katman yoktur. Bu şartı ağ eklentisi (CNI — Calico, Cilium gibi) uygular. Yani "küme içinde her şey birbirini görür" varsayılan durumdur; kapatmak ayrı bir iştir.
- **Service:** Ölümlü ve IP'si değişen pod'ların önünde duran **kararlı adres**. Pod'ları **etiket seçicisiyle (label selector)** bulur. Tipleri şunlardır:
  - **`ClusterIP` (varsayılan):** Yalnız küme içinden erişilebilen sanal bir IP ve DNS adı verir. Servisler arası çağrılar için doğru tercih budur.
  - **`NodePort`:** Her düğümün belirli bir portunu (varsayılan aralık 30000-32767) servise açar; `ClusterIP`'i de kapsar. Küme dışına açar, bu yüzden iç servis için gereksiz yüzeydir.
  - **`LoadBalancer`:** Bulut sağlayıcısından bir **L4 yük dengeleyici** ister; `NodePort` ve `ClusterIP`'i de kapsar. L4, taşıma katmanı demektir: dengeleyici TCP bağlantısına bakar, HTTP içeriğine bakmaz. Her servis için ayrı bir yük dengeleyici açtığından maliyeti hızla büyür. Bu yüzden çok sayıda HTTP servisi genelde tek bir Ingress'in arkasına konur.
  - **`ExternalName`:** Hiç pod seçmez, yük dağıtmaz; küme içi bir ada karşılık DNS'te **CNAME** döndürerek küme dışındaki bir adresi küme içi adla temsil eder.
- **Başsız (headless) Service:** `clusterIP: None` verilen Service. Sanal IP kurulmaz; DNS sorgusu tek bir sanal adres yerine **pod IP'lerinin listesini** döner. StatefulSet ile birlikte kullanıldığında her pod için `pod-0.servis.namespace.svc.cluster.local` biçiminde ayrı bir kayıt oluşur. Pod'ları tek tek adreslemek gerektiğinde bu şarttır: dağıtık önbellek üyeleri, veritabanı düğümleri ve istemci tarafı yük dengeleme bunun tipik örnekleridir.
- **CoreDNS ve ad çözümü:** Küme içi DNS sunucusu CoreDNS'tir. Tam ad biçimi **`servis.namespace.svc.cluster.local`**'dir. Çağıran pod **aynı ad alanındaysa (namespace)** yalnız `servis` yazmak yeter. **Farklı bir ad alanından** çağırırken en az `servis.namespace` yazmak gerekir; çünkü kısa ad önce çağıranın kendi ad alanında aranır. Pod'un `/etc/resolv.conf` dosyasındaki arama listesi ve `ndots` ayarı bu kısaltmaları mümkün kılar.
- **Endpoint / EndpointSlice:** Service'in seçicisine uyan ve **hazır (ready)** durumdaki pod'ların adres listesidir. **"Hazır mısın" denetiminden (readiness probe)** geçemeyen pod bu listeden çıkarılır ve hiç trafik almaz. Service ile sağlık yoklamalarının birbirine bağlandığı yer tam olarak burasıdır. Çok kopyalı servislerde tek dev bir `Endpoints` nesnesi ölçeklenmez; bu yüzden liste **EndpointSlice** parçalarına bölünür.
- **kube-proxy ve yük dağıtımı:** Her düğümde çalışan kube-proxy, Service'in sanal IP'sine gelen trafiği uç noktalardan birine yönlendirir; bunu iptables ya da IPVS kurallarıyla yapar. Kritik ayrıntı şudur: bu dağıtım **L4'te ve bağlantı bazındadır**. Yani karar, yeni bir TCP bağlantısı kurulurken bir kez verilir; tek tek istekler için verilmez. HTTP/1.1'de bağlantılar sık kurulup kapandığı için dağılım dengeli görünür. Ama **HTTP/2 ve gRPC** tek bir kalıcı bağlantı üzerinden çoklama (multiplexing) yapar. Bu yüzden trafik ilk seçilen pod'a **yapışır**; kopya (replika) eklesen bile dengelenmez. Üç çözüm vardır: başsız Service ile **istemci tarafı yük dengeleme**, araya bir vekil (proxy) ya da servis ağı koymak, veya bağlantı ömrünü sınırlayıp düzenli yeniden kurulmasını sağlamak.
- **Ingress:** L7 düzeyinde, yani uygulama (HTTP) katmanında **alan adı ve yola göre yönlendirme** ile **TLS sonlandırma** tanımlar. Tek başına hiçbir şey yapmaz. Kümede bir **Ingress denetleyicisi (controller)** kurulu olmalıdır; nginx, Traefik ve HAProxy bunun örnekleridir. Asıl trafiği denetleyici taşır; Ingress nesnesi ise yalnız ona verilmiş bir kural listesidir. Ingress'in daha yetenekli halefi **Gateway API**'dir ve yeni kurulumlarda giderek o tercih edilmektedir.
- **NetworkPolicy:** Pod'lar arası trafiğe izin/ret kuralı yazar. `ingress` (gelen) ve `egress` (giden) yönleri ayrı ayrı tanımlanır. İki davranışı kritiktir. **(1) Varsayılan her şey serbesttir.** Hiçbir politika yoksa her pod her pod'a erişir. Bir pod'a **herhangi bir** politika uyduğu anda ise o pod için "yalnız izin verilenler" kuralı başlar. Bu yüzden yaygın desen şudur: önce her şeyi kapatan bir **hepsini-reddet (deny-all)** taban politikası yazılır, üstüne dar izinler eklenir. **(2) Politikayı CNI eklentisi uygular.** Eklenti NetworkPolicy'yi desteklemiyorsa nesne kabul edilir ama **sessizce etkisiz** kalır. Bankacılıkta en tehlikeli tuzak budur: kural yazılmış görünür, ama hiç uygulanmaz.
- **Namespace ≠ ağ sınırı:** Namespace nesneleri mantıksal olarak gruplar ve RBAC/kota için doğal bir birimdir, ama **ağ izolasyonu sağlamaz**; farklı namespace'lerdeki pod'lar varsayılan olarak birbirine erişebilir. Segmentasyon NetworkPolicy'nin işidir.
- **Oturum yapışkanlığı (session affinity):** Service üzerindeki `sessionAffinity: ClientIP` ayarı, aynı kaynak IP'den gelen bağlantıları hep aynı pod'a yönlendirir. Ingress düzeyinde çerez tabanlı yapışkanlık da mümkündür. Ama yapışkanlık ölçeklemeyi ve güncellemeyi bozar: pod ölünce oturum kaybolur, yük dengesizleşir. Tercih edilen yol, servisi **durumsuz (stateless)** tutup oturumu Redis gibi paylaşılan bir yerde saklamaktır.
- **Çıkış (egress) ve sabit çıkış IP'si:** Pod kümeden dışarı çıkarken kaynak adresi genellikle **kaynak adres dönüştürmeyle (SNAT)** bulunduğu düğümün IP'sine çevrilir. Düğüm havuzu değiştikçe bu IP de değişir. Oysa banka güvenlik duvarları ve dış servis sağlayıcıları **beyaz listeye sabit bir IP** yazmak ister; kartlı sistemler ve kredi bürosu bunun tipik örneğidir. Üç çözüm vardır: bulutta NAT geçidi kurmak, servis ağının **çıkış geçidini (egress gateway)** kullanmak, ya da sabit IP'li ayrı bir düğüm havuzu üzerinden çıkışı zorlamak.
- **Servis ağı (service mesh):** Servisler arası trafiği devralan ayrı bir katmandır. Ya **yan konteyner (sidecar)** vekili olarak ya da düğüm düzeyinde çalışır; yan konteyner, ana konteynerin yanında aynı pod içinde koşan yardımcı konteynerdir. Uygulama kodunu hiç değiştirmeden şunları sağlar: **karşılıklı TLS (mTLS)** ile şifreleme ve kimlik doğrulama, **yeniden deneme / zaman aşımı / devre kesici**, ağırlıklı **trafik bölme (kanarya yayını — canary)** ve ayrıntılı gözlemlenebilirlik. Bedeli ek karmaşıklık ve bir miktar gecikmedir.

```svg
<svg viewBox="0 0 660 272" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Service tipleri: ClusterIP, NodePort, LoadBalancer ve Ingress katmanları">
  <defs>
    <marker id="k8snet-svc-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dışarıdan içeriye: hangi Service tipi nereye kadar açar?</text>
  <rect x="20" y="44" width="150" height="200" rx="12" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="95" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">Küme dışı</text>
  <text x="95" y="96" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">internet / kurum ağı</text>
  <rect x="34" y="112" width="122" height="52" rx="8" fill="#FFFFFF" stroke="#B07D20" stroke-width="1.3"/>
  <text x="95" y="132" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Ingress (L7)</text>
  <text x="95" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">host/yol + TLS</text>
  <text x="95" y="186" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">LoadBalancer (L4)</text>
  <text x="95" y="206" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">NodePort: 30000-32767</text>
  <text x="95" y="228" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">her ikisi de dışa açar</text>
  <rect x="196" y="44" width="440" height="200" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="416" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Küme içi</text>
  <rect x="216" y="86" width="180" height="66" rx="10" fill="#FFFFFF" stroke="#186399" stroke-width="1.4"/>
  <text x="306" y="108" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">ClusterIP (varsayılan)</text>
  <text x="306" y="126" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kararlı sanal IP + DNS</text>
  <text x="306" y="142" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">dışarıdan erişilemez</text>
  <rect x="216" y="166" width="180" height="60" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="306" y="188" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Headless (clusterIP: None)</text>
  <text x="306" y="206" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sanal IP yok, pod IP listesi</text>
  <rect x="440" y="86" width="176" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="528" y="108" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Pod (kendi IP'si)</text>
  <rect x="440" y="126" width="176" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="528" y="148" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Pod (kendi IP'si)</text>
  <rect x="440" y="166" width="176" height="60" rx="8" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.3" stroke-dasharray="4 3"/>
  <text x="528" y="188" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">EndpointSlice: yalnız</text>
  <text x="528" y="206" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hazır (ready) pod'lar</text>
  <path d="M398 106 L438 100" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#k8snet-svc-arw)"/>
  <path d="M398 128 L438 140" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#k8snet-svc-arw)"/>
  <path d="M398 196 L438 196" fill="none" stroke="#6B3FA0" stroke-width="1.3" marker-end="url(#k8snet-svc-arw)"/>
  <path d="M170 138 L214 118" fill="none" stroke="#B07D20" stroke-width="1.4" marker-end="url(#k8snet-svc-arw)"/>
  <text x="330" y="264" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ExternalName: pod seçmez, yük dağıtmaz — küme dışı bir adrese DNS CNAME döndürür</text>
</svg>
```

### Nasıl çalışır?

1. **Pod ayağa kalkar ve bir IP alır.** Bu IP kümeye özeldir ve pod her yeniden yaratıldığında değişir; hiçbir yapılandırmaya elle yazılmaz.
2. **Service pod'ları etiketinden bulur.** Seçiciye uyan pod'ların adresleri EndpointSlice'a yazılır — ama yalnız **readiness yoklamasından geçmiş** olanlar.
3. **CoreDNS ada karşılık adres döndürür.** İstemci `limit-servisi.kredi.svc.cluster.local` sorar, cevap `ClusterIP` tipinde bir Service için tek bir sanal IP'dir; başsız Service için doğrudan pod IP'lerinin listesidir.
4. **kube-proxy bağlantıyı bir pod'a yönlendirir.** Karar yeni bağlantı kurulurken verilir. Kalıcı bağlantı kullanan protokollerde (HTTP/2, gRPC, veritabanı havuzları) bu karar uzun süre değişmez — dengesizliğin kaynağı budur.
5. **Dışarıdan gelen HTTP trafiği Ingress'ten girer.** Ingress denetleyicisi genelde tek bir `LoadBalancer` Service ile dışarı açılır, sonra alan adı ve yola bakıp isteği doğru küme içi `ClusterIP` Service'ine yollar; TLS burada sonlandırılır.
6. **NetworkPolicy trafiği daraltır.** Politika yazılmadan önce her şey serbesttir. Hedef pod'u seçen bir hepsini-reddet politikası konur, sonra "yalnız kredi motorundan 5432 portuna" gibi dar izinler eklenir.
7. **Küme dışına çıkarken SNAT devreye girer.** Pod IP'si düğüm IP'sine dönüşür; sabit çıkış IP'si gerekiyorsa NAT geçidi veya çıkış geçidi kurulur.

```svg
<svg viewBox="0 0 660 258" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="HTTP 1.1 ve gRPC HTTP 2 protokollerinde kube-proxy yük dağıtımı farkı">
  <defs>
    <marker id="k8snet-lb-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
    <marker id="k8snet-lb-red" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">kube-proxy bağlantı bazlı dağıtır — istek bazlı değil</text>
  <rect x="20" y="42" width="308" height="196" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="174" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">HTTP/1.1 · kısa ömürlü bağlantılar</text>
  <rect x="38" y="96" width="86" height="46" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="81" y="116" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istemci</text>
  <text x="81" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her istek yeni bağlantı</text>
  <rect x="228" y="84" width="82" height="28" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="269" y="103" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">pod 1</text>
  <rect x="228" y="118" width="82" height="28" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="269" y="137" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">pod 2</text>
  <rect x="228" y="152" width="82" height="28" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="269" y="171" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">pod 3</text>
  <path d="M126 112 L226 98" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#k8snet-lb-arw)"/>
  <path d="M126 120 L226 132" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#k8snet-lb-arw)"/>
  <path d="M126 128 L226 166" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#k8snet-lb-arw)"/>
  <text x="174" y="212" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yeni bağlantı = yeni seçim → yük dengeli dağılır</text>
  <rect x="332" y="42" width="308" height="196" rx="12" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="486" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">gRPC / HTTP-2 · tek kalıcı bağlantı</text>
  <rect x="350" y="96" width="86" height="46" rx="8" fill="#FFFFFF" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="393" y="116" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istemci</text>
  <text x="393" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tüm istekler tek bağlantı</text>
  <rect x="540" y="84" width="82" height="28" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="581" y="103" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">pod 1</text>
  <rect x="540" y="118" width="82" height="28" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.3" stroke-dasharray="4 3"/>
  <text x="581" y="137" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">pod 2 (boşta)</text>
  <rect x="540" y="152" width="82" height="28" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.3" stroke-dasharray="4 3"/>
  <text x="581" y="171" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">pod 3 (boşta)</text>
  <path d="M438 118 L538 98" fill="none" stroke="#B23B3B" stroke-width="2.2" marker-end="url(#k8snet-lb-red)"/>
  <text x="486" y="212" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">bağlantı kurulunca seçim donar → trafik tek pod'a yapışır</text>
  <text x="330" y="252" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Çözüm: headless Service + istemci tarafı dengeleme · servis ağı proxy'si · bağlantı ömrünü sınırlama</text>
</svg>
```

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="NetworkPolicy ile hepsini reddet tabanı ve dar izin kuralları">
  <defs>
    <marker id="k8snet-np-ok" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
    <marker id="k8snet-np-no" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Varsayılan serbest → hepsini-reddet tabanı + dar izin</text>
  <rect x="20" y="44" width="150" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="95" y="72" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">kredi-motoru pod</text>
  <rect x="20" y="106" width="150" height="46" rx="8" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.3"/>
  <text x="95" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">rapor-servisi pod</text>
  <rect x="20" y="168" width="150" height="46" rx="8" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.3"/>
  <text x="95" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">test namespace pod</text>
  <rect x="286" y="44" width="112" height="170" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6" stroke-dasharray="6 4"/>
  <text x="342" y="122" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">NetworkPolicy</text>
  <text x="342" y="142" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">deny-all taban</text>
  <rect x="470" y="98" width="166" height="62" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.6"/>
  <text x="553" y="124" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">kredi veritabanı</text>
  <text x="553" y="144" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">port 5432</text>
  <path d="M172 66 L284 100" fill="none" stroke="#25804C" stroke-width="1.6" marker-end="url(#k8snet-np-ok)"/>
  <path d="M400 122 L468 126" fill="none" stroke="#25804C" stroke-width="1.6" marker-end="url(#k8snet-np-ok)"/>
  <text x="228" y="60" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">izinli</text>
  <path d="M172 128 L284 130" fill="none" stroke="#B23B3B" stroke-width="1.5" stroke-dasharray="5 3" marker-end="url(#k8snet-np-no)"/>
  <path d="M172 190 L284 160" fill="none" stroke="#B23B3B" stroke-width="1.5" stroke-dasharray="5 3" marker-end="url(#k8snet-np-no)"/>
  <text x="228" y="122" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">reddedilir</text>
  <text x="228" y="184" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">reddedilir</text>
  <text x="330" y="238" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Tuzak: CNI eklentisi NetworkPolicy desteklemiyorsa kural yazılır ama sessizce hiç uygulanmaz</text>
</svg>
```

### Mülakatta ne sorulur / tuzaklar

- **"Hangi Service tipini seçersin?"** İç servis için doğrudan `ClusterIP` demelisin. "Dışarıdan erişilmeyecekse yine de NodePort verelim" ya da her servise ayrı `LoadBalancer` açmak, hem gereksiz saldırı yüzeyi hem maliyettir. Çok sayıda HTTP servisi tek Ingress arkasında toplanır.
- **"Servis adını nasıl çözersin?"** `servis.namespace.svc.cluster.local`. Kısa adın yalnız **aynı namespace** içinde çalıştığını bilmek beklenir; farklı namespace'ten çağırırken en az `servis.namespace` yazılır. Pod IP'sini bir yere sabitlemek elenme sebebidir.
- **"gRPC'de replika ekledim, yük dengelenmedi."** En sevilen ileri sorulardan biri. Cevap: kube-proxy **bağlantı** bazında dağıtır, gRPC/HTTP-2 tek kalıcı bağlantı kullandığı için seçim donar. Çözüm istemci tarafı yük dengeleme (başsız Service ile), servis ağı proxy'si veya bağlantı ömrünü sınırlamak. Daha çok replika açmak veya `sessionAffinity` eklemek sorunu **büyütür**.
- **"NetworkPolicy'nin varsayılanı nedir?"** Varsayılan **serbesttir**; namespace ayrımı ağ sınırı değildir. Banka ortamında beklenen cevap hepsini-reddet tabanı + dar izinler ve "CNI destekliyor mu?" doğrulamasıdır.
- **"Ingress'i kurdum ama çalışmıyor."** Ingress nesnesi tek başına trafik taşımaz; bir Ingress denetleyicisi kurulu olmalıdır. Yeni kurulumlarda Gateway API'nin Ingress'in halefi olduğunu söylemek artı puandır.
- **"Dış servise çıkarken IP'miz neden değişiyor?"** Pod IP'si düğüm IP'sine SNAT'lanır; sabit çıkış IP'si için NAT geçidi, çıkış geçidi veya sabit IP'li ayrı düğüm havuzu gerekir. Bu, kredi bürosu/kart sistemleri gibi beyaz liste isteyen entegrasyonlarda mutlaka sorulur.
- **"Servis ağı ne kazandırır?"** Uygulama kodunu değiştirmeden mTLS, yeniden deneme/zaman aşımı, trafik bölme ve gözlemlenebilirlik; bedeli karmaşıklık ve gecikmedir. "Her ortamda mutlaka kurulmalı" demek yanlış cevaptır.

### Tek cümlede

Kubernetes'te her pod'un kendi IP'si vardır ve varsayılan olarak herkes herkese erişir. Kararlı adresi Service ile CoreDNS verir: iç çağrılar için `ClusterIP`, tek tek pod kimliği gerekiyorsa başsız Service. Dışarıya açılış Ingress'ten geçer. kube-proxy bağlantı bazında dağıttığı için gRPC dengesizliği ayrı bir çözüm ister; segmentasyon ise ancak NetworkPolicy ile kurulur.

# k8snet-1 | Orta
## Soru
Kredi motoru servisine yalnız küme içindeki başvuru servisi çağrı yapacak; servis küme dışına hiçbir şekilde açılmayacak. Hangi Service tipi doğrudur ve neden?
## A
`NodePort` — küme içi erişim ancak bir düğüm portu üzerinden mümkün olduğu için iç servislerde de bu tip kullanılır
🤖 küme içi erişim için `ClusterIP` zaten yeterlidir; `NodePort` servisi tüm düğümlerin portunda dışarıya da açtığı için gereksiz saldırı yüzeyi yaratır.
## B
`ClusterIP` (varsayılan) — küme içinde kararlı bir sanal IP ve DNS adı verir, küme dışından erişilemez
🤖 Doğru: iç servisler arası çağrı için tasarlanmış tip budur; dışa hiçbir kapı açmaz.
## C
`LoadBalancer` — bulut sağlayıcısının yük dengeleyicisi trafiği daha iyi dağıttığı için iç servislerde de tercih edilir
🤖 bu tip servisi dış dünyaya açar ve servis başına ayrı (ücretli) yük dengeleyici ister; iç çağrılar için gereksizdir.
## D
`ExternalName` — servisi küme içinde bir adla temsil ettiği için iç çağrılara uygundur
🤖 `ExternalName` pod seçmez ve yük dağıtmaz; yalnız küme dışındaki bir adrese DNS CNAME döndürür.
## Doğru
B
## Açıklama
**`ClusterIP`** Service'in varsayılan tipidir ve tam olarak bu senaryo içindir: pod'ların önünde kararlı bir **sanal IP ile DNS adı** oluşturur, trafiği o an hazır (ready) pod'lara dağıtır ve **küme dışından erişilemez**. **`NodePort`** ise `ClusterIP`'i kapsamakla birlikte servisi her düğümün belirli bir portunda (varsayılan aralık 30000-32767) dışarıya açar — iç bir servis için bu, hiç istenmeyen bir saldırı yüzeyidir ve "küme içi erişim ancak düğüm portundan olur" iddiası da yanlıştır. **`LoadBalancer`** bir adım daha ileri gider: bulut sağlayıcısından servise özel bir L4 yük dengeleyici ister; hem dışa açar hem de her servis için ayrı bir yük dengeleyici maliyeti doğurur, bu yüzden çok sayıda HTTP servisi genelde tek bir Ingress arkasında toplanır. **`ExternalName`** ise bambaşka bir amaca hizmet eder: hiç pod seçmez, uç nokta listesi tutmaz, yük dağıtmaz; küme içinde bir ad sorulduğunda küme dışındaki bir alan adına **CNAME** döndürür — küme dışı bir sistemi küme içi adla temsil etmek için kullanılır.

# k8snet-2 | Orta
## Soru
`basvuru` namespace'inde çalışan bir pod, `kredi` namespace'indeki `limit-servisi` adlı Service'e çağrı yapacak. CoreDNS ile doğru ad çözümü nasıl yapılır?
## A
Yalnız `limit-servisi` yazmak yeterlidir; servis adları küme genelinde benzersizdir ve DNS her namespace'i tarar
🤖 kısa ad önce çağıranın kendi namespace'inde aranır ve adlar yalnız namespace içinde benzersizdir; başka namespace'te aynı ad bambaşka bir servise ait olabilir.
## B
`limit-servisi.cluster.local` yazmak gerekir; namespace bilgisi adın parçası değildir
🤖 küme içi tam ad `servis.namespace.svc.cluster.local` biçimindedir, namespace ve `svc` bileşenleri atlanamaz.
## C
Hedef servisin pod IP'leri bir kez öğrenilip çağıran servisin yapılandırmasına yazılır
🤖 pod IP'leri geçicidir ve pod her yeniden yaratıldığında değişir; sabitlenen IP kısa sürede geçersiz kalır.
## D
`limit-servisi.kredi.svc.cluster.local` (kısaltılmış hâli `limit-servisi.kredi`) yazılır
🤖 Doğru: küme içi tam ad biçimi `servis.namespace.svc.cluster.local`'dir ve namespace verildiği sürece kısaltılabilir.
## Doğru
D
## Açıklama
CoreDNS'in küme içi tam ad biçimi **`servis.namespace.svc.cluster.local`**'dir. Çağıran pod **aynı namespace**'te olsaydı yalnız `limit-servisi` yazmak yeterdi; ama burada çağıran `basvuru`, hedef `kredi` namespace'inde olduğu için en az **`limit-servisi.kredi`** yazılmalıdır (pod'un DNS arama listesi geri kalanını tamamlar). Kısa adın her yerde çalıştığını sanmak klasik hatadır: servis adları yalnız kendi namespace'i içinde benzersizdir, farklı namespace'lerde aynı adlı servisler olabilir ve kısa ad önce çağıranın kendi namespace'inde aranır — bu yüzden `basvuru` namespace'inde `limit-servisi` diye bir şey yoksa çözüm başarısız olur, varsa da yanlış servise gidilir. `limit-servisi.cluster.local` biçimi de geçersizdir; `namespace` ve `svc` bileşenleri atlanamaz. Pod IP'lerini elle yapılandırmaya yazmak ise Service soyutlamasının var oluş sebebini yok sayar: pod IP'leri **geçicidir**, yeniden yaratma, ölçekleme veya güncelleme sonrası değişir. Doğru refleks daima adı kullanmak, adresi Kubernetes'e bırakmaktır.

# k8snet-3 | İleri
## Soru
Başvuru servisi, kredi motoruna **gRPC** ile bağlanıyor. Kredi motorunun 6 replikası var, hepsi hazır (ready) görünüyor, ama izleme ekranında trafiğin neredeyse tamamının 1-2 pod'a gittiği görülüyor. Sebep ve doğru çözüm nedir?
## A
kube-proxy L4'te bağlantı bazında dağıtır; gRPC/HTTP-2 tek kalıcı bağlantı üzerinden çoklama yaptığı için seçim donar ve trafik ilk seçilen pod'a yapışır — çözüm istemci tarafı yük dengelemeye geçmektir
🤖 Doğru: yük dağıtım kararı yeni bağlantı kurulurken bir kez verilir, tek tek istekler için verilmez.
## B
Diğer 4 pod'un readiness yoklaması aralıklı olarak başarısızdır; uç nokta listesinden düşüp döndükleri için trafik yalnız istikrarlı kalan iki pod'a gidiyordur
🤖 hazır olmayan pod uç nokta listesinden çıkarılır ve soruda tüm pod'ların hazır olduğu belirtilmiştir; sorun hazır pod'lara hiç yeni bağlantı kurulmamasıdır.
## C
Service'e `sessionAffinity: ClientIP` eklenmelidir; böylece trafik pod'lar arasında düzgün paylaştırılır
🤖 bu ayar aynı kaynak IP'yi hep aynı pod'a bağlar, yani yapışkanlığı artırıp dengesizliği daha da büyütür.
## D
Kredi motorunun replika sayısı artırılmalı ya da otomatik ölçekleme (HPA) açılmalıdır; havuz büyüdükçe kube-proxy istekleri daha geniş bir uç nokta kümesine yayar ve dağılım kendiliğinden düzelir
🤖 yeni pod'lara da yeni bağlantı kurulmadığı için trafik yine mevcut bağlantıların bağlı olduğu pod'larda kalır, dengesizlik sürer.
## Doğru
A
## Açıklama
Kubernetes'in yerleşik yük dağıtımı **kube-proxy** tarafından, iptables/IPVS kurallarıyla ve **L4 (bağlantı) düzeyinde** yapılır: hangi uç noktaya gidileceği **yeni bir TCP bağlantısı kurulurken bir kez** seçilir, o bağlantı üzerinden akan tek tek istekler için tekrar seçilmez. HTTP/1.1'de bağlantılar sık kurulup kapandığı için dağılım dengeli görünür; **gRPC ve HTTP/2** ise tek bir kalıcı bağlantı üzerinden çoklama (multiplexing) yaptığından tüm istekler ilk seçilen pod'a gider ve trafik oraya **yapışır**. Bu yüzden replika sayısını artırmak ya da otomatik ölçekleme açmak sorunu çözmez: yeni pod'lara hiç yeni bağlantı kurulmadığı için boşta kalırlar. `sessionAffinity: ClientIP` eklemek ise durumu iyileştirmek yerine kötüleştirir; adı üstünde yapışkanlığı artırır. Readiness açıklaması da bu senaryoya uymaz: hazır olmayan pod zaten EndpointSlice'tan çıkarılır ve soruda tüm pod'ların hazır olduğu söylenmiştir. Gerçek çözümler üç başlıkta toplanır: (1) **başsız (headless) Service** kullanıp gRPC istemcisine pod IP'lerinin listesini vermek ve **istemci tarafı yük dengelemeyi** açmak; (2) araya **servis ağı (service mesh)** ya da L7 farkında bir proxy koyup dengelemeyi istek düzeyine taşımak; (3) sunucu tarafında **bağlantı ömrünü sınırlayarak** istemciyi düzenli aralıklarla yeniden bağlanmaya zorlamak.

# k8snet-4 | İleri
## Soru
StatefulSet ile çalışan 3 düğümlü bir dağıtık önbellek kuruyorsun. Düğümlerin birbirini **tek tek, adıyla** bulup küme oluşturması gerekiyor; tek bir sanal adres arkasına saklanmaları işe yaramıyor. Hangi Service yapılandırması gerekir?
## A
Normal bir `ClusterIP` Service; tek sanal IP arkasındaki pod'lar birbirini o adres üzerinden bulur
🤖 tek sanal IP tek tek pod'u adreslemeye izin vermez, istek her seferinde rastgele bir pod'a düşer ve düğümler birbirini ayırt edemez.
## B
`LoadBalancer` tipinde bir Service; dış yük dengeleyici pod'lara ayrı ayrı adres verir
🤖 bu tip küme dışına açmak içindir ve pod başına kimlik/DNS kaydı üretmez.
## C
`clusterIP: None` ile **başsız (headless)** Service; sanal IP kurulmaz, DNS sorgusu pod IP'lerini döner ve StatefulSet ile birlikte her pod için adıyla çözülen ayrı bir DNS kaydı oluşur
🤖 Doğru: başsız Service'in tanımı budur ve StatefulSet'in kararlı kimliğiyle birleşince her pod adıyla adreslenebilir hâle gelir.
## D
`NodePort` tipinde bir Service; her düğüm pod başına farklı bir port açtığı için düğümler birbirini port numarasından ayırt eder ve ayrı bir adrese ihtiyaç duyulmaz
🤖 `NodePort` tüm düğümlerde **aynı** portu açar ve pod kimliği sağlamaz; üstelik küme içi haberleşme için servisi gereksizce dışa açar.
## Doğru
C
## Açıklama
**Başsız (headless) Service**, `clusterIP: None` verilerek oluşturulur: Kubernetes ona sanal IP atamaz ve kube-proxy kuralları kurmaz. Sonuç olarak servis adı sorulduğunda DNS tek bir sanal adres yerine **uç noktalardaki pod IP'lerinin listesini** döndürür. **StatefulSet** ile birlikte kullanıldığında ek olarak her pod için ayrı bir DNS kaydı oluşur: `pod-0.servis.namespace.svc.cluster.local`. Dağıtık önbellek, veritabanı düğümleri, kuyruk üyeleri gibi birbirini **tek tek tanıması gereken** iş yüklerinde gereken tam olarak budur; aynı mekanizma gRPC istemci tarafı yük dengelemesinin de temelidir. Normal bir `ClusterIP` Service bu işi göremez: tek sanal IP arkasında istek rastgele bir pod'a düşer, çağıran hangi düğümle konuştuğunu seçemez. `LoadBalancer` küme dışına açmak içindir; pod başına kimlik ya da DNS kaydı üretmez ve iç küme haberleşmesi için gereksiz maliyet doğurur. `NodePort` da kimlik sağlamaz — tüm düğümlerde **aynı** portu açar, pod'ları ayırt etmez ve küme içi haberleşmeyi dışarıya açık hâle getirir. Not: başsız Service kullanmak yük dengelemeyi ortadan kaldırmaz, sorumluluğu **istemciye** taşır.

# k8snet-5 | İleri
## Soru
Denetim ekibi "kredi namespace'indeki veritabanı pod'una yalnız kredi motoru erişebilsin, başka hiçbir servis erişemesin" şartını getirdi. Kubernetes'in varsayılan davranışı nedir ve doğru çözüm hangisidir?
## A
Küme içinde pod'lar arası trafik varsayılan olarak kapalıdır; NetworkPolicy yalnız istisna açmak için yazılır, yani ek bir şey yapmaya gerek yoktur
🤖 varsayılan tam tersidir — hiçbir politika yokken kümedeki her pod her pod'a erişebilir.
## B
Veritabanını ayrı bir namespace'e taşımak yeterlidir; farklı namespace'lerdeki pod'lar birbirine zaten erişemez
🤖 namespace mantıksal bir gruplamadır, ağ sınırı değildir; namespace'ler arası trafik varsayılan olarak serbesttir.
## C
Veritabanı Service'ini `ClusterIP` yapmak izolasyonu sağlar, çünkü bu tip erişimi kısıtlar
🤖 `ClusterIP` yalnız küme dışına kapatır; küme içindeki her pod hâlâ hem servise hem doğrudan pod IP'sine erişebilir.
## D
Varsayılan olarak her şey serbesttir; veritabanı pod'unu seçen bir hepsini-reddet (deny-all) taban politikası yazılıp üstüne yalnız kredi motorunu seçen dar bir izin kuralı eklenir
🤖 Doğru: politika uygulanan pod için "yalnız izin verilenler" kuralı başlar ve kuralı fiilen uygulayan katman CNI eklentisidir.
## Doğru
D
## Açıklama
Kubernetes'in ağ modelinde **varsayılan her şey serbesttir**: her pod'un kendi IP'si vardır ve kümedeki herhangi bir pod herhangi bir pod'a NAT olmadan erişebilir. Bu yüzden "trafik zaten kapalı, NetworkPolicy istisna açar" ifadesi tam tersini söyler. **Namespace** de bir çözüm değildir; nesneleri mantıksal olarak gruplar, RBAC ve kota için doğal bir birimdir, ama **ağ izolasyonu sağlamaz** — farklı namespace'lerdeki pod'lar varsayılan olarak birbirine erişir. Service tipini `ClusterIP` yapmak da yetmez: bu yalnız küme **dışından** erişimi engeller, küme içindeki her pod hem servise hem de doğrudan veritabanı pod'unun IP'sine gidebilir. Doğru araç **NetworkPolicy**'dir ve davranışı şudur: bir pod'a **herhangi bir** politika uyduğu anda o pod için "yalnız açıkça izin verilenler" kuralı devreye girer. Yaygın desen önce hedef pod'u seçen bir **hepsini-reddet (deny-all)** taban politikası yazmak, sonra üstüne "yalnız kredi motoru etiketli pod'lardan, yalnız veritabanı portuna" gibi dar izinler eklemektir; gelen (`ingress`) ve giden (`egress`) yönler ayrı ayrı düşünülmelidir. Son ve en tehlikeli ayrıntı: NetworkPolicy'yi API sunucusu değil **CNI eklentisi** uygular; eklenti desteklemiyorsa nesne sorunsuzca kaydedilir ama **sessizce hiç uygulanmaz** — denetimde "kural var" görünüp gerçekte korumasız kalmamak için bu mutlaka doğrulanmalıdır.

# k8snet-6 | Uzman
## Soru
Banka, kümedeki servislerin dış kredi bürosu servisine çıkışını güvenlik duvarında **tek bir sabit IP** ile beyaz listeye almak istiyor; ayrıca küme içi servisler arası trafiğin şifreli olmasını bekliyor. Hangisi doğrudur?
## A
Her pod'un kendi IP'si olduğu için dış güvenlik duvarı doğrudan pod IP'lerini görür; kaynak adres yol boyunca değişmediğinden beyaz listeye pod ağı (CIDR) aralığı yazılır
🤖 pod IP'leri kümeye özel ve geçicidir, dışarı çıkarken SNAT ile düğüm IP'sine dönüşür; dış taraf pod IP'sini hiç görmez.
## B
Çıkış trafiği bulunduğu düğümün IP'siyle dışarı çıkar; sabit tek IP için NAT geçidi ya da çıkış geçidi (egress gateway) gerekir — servisler arası şifrelemeyi ise servis ağı (service mesh) karşılıklı TLS (mTLS) ile devralır
🤖 Doğru: kaynak adres SNAT ile düğüme bağlıdır ve mTLS servis ağının devraldığı klasik sorumluluktur.
## C
Ingress denetleyicisi kümenin tek çıkış noktası olduğundan, Ingress nesnesine bir çıkış (egress) kuralı yazılarak hem çıkış IP'si sabitlenir hem de trafik şifrelenir
🤖 Ingress yalnız kümeye **gelen** HTTP trafiğini yönetir; çıkış trafiğiyle ilgili bir yeteneği yoktur.
## D
NetworkPolicy'nin egress kuralında izin verilen hedefler sayılarak çıkış IP'si sabitlenir ve giden trafik otomatik olarak şifrelenir
🤖 NetworkPolicy yalnız izin/ret kararı verir; ne kaynak IP'yi değiştirir ne de herhangi bir şifreleme uygular.
## Doğru
B
## Açıklama
Pod IP'leri **kümeye özeldir ve geçicidir**; bir pod kümeden dışarı çıktığında kaynak adresi genellikle **SNAT** ile bulunduğu düğümün IP'sine dönüşür — dolayısıyla dış güvenlik duvarı pod IP'sini hiç görmez ve pod IP aralığını beyaz listeye yazmak işlemez. Düğüm havuzu ölçeklendikçe veya düğümler değiştikçe çıkış IP'si de değişeceğinden, kredi bürosu/kart sistemleri gibi **beyaz liste** isteyen entegrasyonlar için çıkışın sabitlenmesi gerekir: bulutta bir **NAT geçidi**, servis ağının **çıkış geçidi (egress gateway)** ya da sabit IP'li ayrı bir düğüm havuzu üzerinden çıkış zorlamak yaygın çözümlerdir. Sorunun ikinci yarısı — servisler arası şifreleme — **servis ağının (service mesh)** devraldığı klasik sorumluluktur: yan-araç (sidecar) proxy'si veya düğüm düzeyinde çalışarak **karşılıklı TLS (mTLS)** ile hem şifreleme hem karşılıklı kimlik doğrulama sağlar, ayrıca yeniden deneme, zaman aşımı ve trafik bölme gibi yetenekleri **uygulama kodunu değiştirmeden** getirir; bedeli ek karmaşıklık ve bir miktar gecikmedir. **Ingress** bu tabloda yer almaz: yalnız kümeye **gelen** HTTP trafiğini yönlendirir ve TLS'i orada sonlandırır, çıkış trafiğiyle ilgili hiçbir yeteneği yoktur. **NetworkPolicy**'nin `egress` kuralları ise sadece "hangi hedefe gidilebilir" kararını verir — bir izin/ret filtresidir; kaynak IP'yi değiştirmez ve şifreleme yapmaz.
