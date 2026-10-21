---
slug: service-discovery
baslik: "Servis Keşfi: Client vs Server-Side, Eureka/Consul, Kubernetes'in Eureka'yı Geriletmesi"
kademe: İleri
sira: 100
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Şehirde taksi çağırmayı düşün. Belirli bir şoförün telefon numarasını ezberlersen, o izne çıktığında elinde kullanılamaz bir numara kalır. Bunun yerine bir **çağrı merkezi** vardır: müsait şoförler kendilerini merkeze bildirir, merkez yalnız o an çalışanların listesini tutar, sen tek bir numarayı arar ve müsait olan birine bağlanırsın. Yazılımda bu çağrı merkezinin adı **servis kayıt defteri**, tüm mekanizmanın adı ise **servis keşfidir**. Mikroserviste hiçbir adres kalıcı olmadığı için bu mekanizma zorunludur.

### Temel kavramlar

- **Servis**: bağımsız çalışan, kendisine ağ üzerinden erişilen uygulama parçası.
- **Örnek (instance)**: aynı servisin çalışan bir kopyası. Bir servisin aynı anda 1 veya 50 örneği olabilir.
- **IP adresi ve port**: bir örneğe ağ üzerinden ulaşmak için gereken adres ve kapı numarası.
- **Ağ çağrısı**: iki örnek arasında ağ üzerinden yapılan istek; hedefin adresi bilinmeden yapılamaz.
- **Ölçeklenmek (scaling)**: yükü karşılamak için örnek sayısını artırmak/azaltmak. **Otomatik ölçekleme**, bunu sistemin kendiliğinden yapmasıdır.
- **Dağıtım (deployment)**: yeni sürümün kurulması. Her dağıtımda konteynerler genelde **yeni IP/portla** doğar.
- **Kayıt defteri (service registry)**: hangi servisin hangi adreslerde canlı olduğunu tutan liste. **Kayıt (register)** doğunca eklenmek, **kayıt silme (deregister)** listeden düşmektir.
- **Sağlık kontrolü (health check)**: örneğin çalışır durumda olup olmadığının düzenli sorulması — **heartbeat** (örnek "yaşıyorum" sinyali yollar), HTTP `/health`, ya da Kubernetes'te **readiness probe** ("trafik almaya hazır mıyım?").
- **Yük dengeleyici (load balancer)**: istekleri sağlıklı örnekler arasında paylaştıran bileşen.
- **DNS**: bir adı (`odeme.namespace.svc.cluster.local`) IP adresine çeviren isim servisi.
- **CAP teoremi**: ağ **bölünmesi** (partition) yaşandığında bir dağıtık sistemin ya **tutarlılığı** (C) ya **kullanılabilirliği** (A) önceleyebileceğini söyler.
- **Bayat (stale) veri**: bir süre güncellenmemiş, biraz eski liste.

Karıştırılan bir kavram: **şema kayıt defteri (schema registry)** mesaj biçimlerini saklar, **servis kayıt defteri** ise adresleri.

### Nasıl çalışır?

Mikroservis ortamı **dinamiktir**: otomatik ölçekleme örnek sayısını değiştirir, her dağıtımda konteynerler farklı IP/porta doğar, arızalı örnekler ölür. Bu yüzden "ödeme servisi hangi adreste?" sorusunun sabit, koda yazılabilecek bir yanıtı yoktur. Servis keşfi bu adres sorununu çözer; ölçekleme kararı vermek, mesaj şeması doğrulamak veya trafiği şifrelemek onun işi değildir.

İki temel model vardır. **İstemci tarafı keşifte (client-side discovery)** çağıran servis kayıt defterini **doğrudan sorgular**, sağlıklı örnek listesini alır ve **yük dengelemeyi kendisi yapar**. Klasik örnek: **Netflix Eureka** + **Spring Cloud LoadBalancer** (eski Ribbon'ın yerine). Artısı: araya ekstra bir atlama girmez, istemci akıllı (ör. bölge-duyarlı) seçim yapabilir. Eksisi: keşif ve dengeleme mantığı **her istemciye ve her dile** ait bir kütüphane olarak girer, yani uygulamaya sızar.

**Sunucu tarafı keşifte (server-side discovery)** çağıran, sabit bir **yönlendirici/yük dengeleyici** adresine istek atar; keşif ve dengeleme **altyapıda** yapılır. Örnek: **Kubernetes Service** (kube-proxy + DNS) ve bulut yük dengeleyicileri. Artısı: istemci basit ve dilden bağımsız kalır. Eksisi: araya bir katman girer ve keşif platforma bağımlı hale gelir.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İstemci tarafı ve sunucu tarafı servis keşfi">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki keşif modeli</text>
  <rect x="24" y="38" width="290" height="184" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="60" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">İstemci tarafı (Eureka)</text>
  <rect x="44" y="74" width="96" height="40" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="92" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">çağıran</text>
  <rect x="196" y="74" width="96" height="40" rx="7" fill="#F4EFE4" stroke="#9A7B2E" stroke-width="1.2"/>
  <text x="244" y="92" text-anchor="middle" font-size="10.5" fill="#9A7B2E" font-family="system-ui">Registry</text>
  <text x="244" y="106" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">(Eureka)</text>
  <line x1="140" y1="90" x2="196" y2="90" stroke="#186399" stroke-width="1.5"/>
  <text x="168" y="84" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">1. sorgu</text>
  <rect x="120" y="160" width="80" height="34" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="160" y="181" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">örnek A</text>
  <rect x="212" y="160" width="80" height="34" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="252" y="181" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">örnek B</text>
  <line x1="92" y1="114" x2="150" y2="160" stroke="#186399" stroke-width="1.5" stroke-dasharray="4 3"/>
  <text x="92" y="150" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">2. istemci seçer</text>
  <rect x="326" y="38" width="290" height="184" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="471" y="60" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Sunucu tarafı (K8s DNS)</text>
  <rect x="346" y="74" width="96" height="40" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="394" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">çağıran</text>
  <rect x="498" y="74" width="100" height="40" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="548" y="92" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">Service (DNS)</text>
  <text x="548" y="106" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">ClusterIP sabit</text>
  <line x1="442" y1="90" x2="498" y2="90" stroke="#25804C" stroke-width="1.5"/>
  <text x="470" y="84" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">DNS adı</text>
  <rect x="422" y="160" width="80" height="34" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="462" y="181" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">Pod A</text>
  <rect x="514" y="160" width="80" height="34" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="554" y="181" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">Pod B</text>
  <line x1="548" y1="114" x2="500" y2="160" stroke="#25804C" stroke-width="1.5" stroke-dasharray="4 3"/>
  <text x="548" y="150" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">platform dağıtır</text>
</svg>
```

**Sağlık kontrolü** her modelin ayrılmaz parçasıdır ve **sürekli** çalışır, yalnız kayıt anında bir kez değil. Kayıt defteri ya da dengeleyici düzenli olarak sağlık sinyalini izler. Yanıt vermeyen örnek listeden düşürülür; buna **kayıttan düşürme (deregister)** denir. Devam eden isteklerin bitmesini bekleyip yeni istek göndermemeye ise **trafiği boşaltma (drain)** denir. Örnek tekrar sağlıklı olunca listeye geri döner. Readiness probe ayrıca henüz hazır olmayan (önyükleme, önbellek ısıtma) bir örneğin erken trafik almasını önler. Sağlık kontrolü yalnız metrik toplamak için değildir; **doğrudan trafiğin nereye gideceğini belirler**.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Servis kayıt defteri yaşam döngüsü ve sağlık kontrolü">
  <defs>
    <marker id="service-discovery-ok" markerWidth="9" markerHeight="9" refX="7.5" refY="3" orient="auto">
      <path d="M0,0 L7.5,3 L0,6 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kayıt defteri döngüsü: sağlık sinyali listeyi canlı tutar</text>
  <rect x="60" y="46" width="120" height="44" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="120" y="66" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">örnek doğar</text>
  <text x="120" y="82" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">register</text>
  <rect x="260" y="46" width="120" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="320" y="66" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">heartbeat</text>
  <text x="320" y="82" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">/health · readiness</text>
  <rect x="460" y="46" width="120" height="44" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="520" y="66" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">sağlıklı liste</text>
  <text x="520" y="82" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">trafik alır</text>
  <line x1="180" y1="68" x2="258" y2="68" stroke="#186399" stroke-width="1.6" marker-end="url(#service-discovery-ok)"/>
  <line x1="380" y1="68" x2="458" y2="68" stroke="#186399" stroke-width="1.6" marker-end="url(#service-discovery-ok)"/>
  <rect x="260" y="120" width="120" height="46" rx="8" fill="#FFFFFF" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="320" y="140" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">yanıt yok</text>
  <text x="320" y="156" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">deregister / drain</text>
  <line x1="320" y1="90" x2="320" y2="118" stroke="#8A2E2E" stroke-width="1.6" marker-end="url(#service-discovery-ok)"/>
  <text x="366" y="108" text-anchor="middle" font-size="9.5" fill="#8A2E2E" font-family="system-ui">sağlıksız</text>
  <text x="320" y="186" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Sağlıksız örnek listeden düşer → istekler ölü örneğe gitmez</text>
</svg>
```

**Kayıt defteri seçenekleri**: **Eureka** (Netflix, AP-eğilimli, self-preservation modu), **Consul** (HashiCorp; keşif + sağlık kontrolü + anahtar-değer deposu + çok veri merkezi, tutarlılık düzeyi ayarlanabilir), **etcd/ZooKeeper** (Raft/Zab tabanlı, CP-eğilimli).

**2026 gerçeği — Kubernetes, Eureka'yı büyük ölçüde geriletti.** Uygulama Kubernetes üzerindeyse keşif **platformun işidir**. Bir **Service** nesnesi kalıcı bir sanal IP (ClusterIP) ve **kararlı bir DNS adı** verir. Arkasındaki Pod'lar gelip gitse de bu ad sabit kalır. Trafiği sağlıklı Pod'lara kube-proxy dağıtır, sağlıksız olanları readiness probe otomatik düşürür. Bu ad **namespace (ad alanı) ile sınırlı değildir**: **tam nitelikli ad (FQDN — fully qualified domain name)** kullanılarak başka ad alanlarına da çağrı yapılır, örneğin `odeme.diger-ns.svc.cluster.local`. Dolayısıyla K8s üzerine ayrıca Eureka koymak iki keşif katmanı ve gereksiz yük demektir. Eureka'nın hâlâ anlamlı olduğu yer: K8s **dışı** dağıtımlar ve mevcut Spring Cloud Netflix sistemleri. Spring tarafında modern yol `spring-cloud-kubernetes` ile platform keşfini benimsemektir.

Son katman **CAP**'tir. Kayıt defteri dağıtık bir veri deposu olduğundan, ağ bölünmesinde ya tutarlılığı ya kullanılabilirliği öncelemek zorundadır. **CP** sistemler (ZooKeeper, etcd) bölünen tarafta yazma/okumayı reddedebilir: veri asla yanlış olmaz ama defter erişilemez olabilir — ve defter durursa çağrılar durur. **AP** sistemler (Eureka) her zaman yanıt verir ama liste geçici olarak **bayat** olabilir. Keşif için genelde **AP eğilimi** tercih edilir. Sebebi şu: biraz eski ama erişilebilir bir liste, tutarlı ama erişilemez bir defterden iyidir. Çağıranlar zaten **yeniden deneme (retry)** ve **devre kesici (circuit breaker)** kullanır, üstelik hatalı bir adres yalnız birkaç isteği etkiler. Yine de bu "her zaman AP" demek değildir — lider seçimi gibi işler için CP gerekir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Servis keşfinde CAP: AP eğilimi ve CP eğilimi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kayıt defteri ve CAP: ağ bölünmesinde tercih</text>
  <rect x="30" y="42" width="270" height="124" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="165" y="64" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">AP eğilimi (Eureka)</text>
  <text x="165" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">her zaman yanıt verir</text>
  <text x="165" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">liste biraz bayat olabilir</text>
  <text x="165" y="132" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">keşif için genelde tercih</text>
  <text x="165" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">erişilebilir &gt; tutarlı</text>
  <rect x="340" y="42" width="270" height="124" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="475" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">CP eğilimi (ZooKeeper/etcd)</text>
  <text x="475" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">veri asla yanlış olmaz</text>
  <text x="475" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bölünende erişilemez olabilir</text>
  <text x="475" y="132" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">defter durursa çağrılar durur</text>
  <text x="475" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Consul: ayarlanabilir</text>
  <text x="320" y="188" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Keşifte "bayat ama erişilebilir" liste, "tutarlı ama erişilemez" defterden iyidir</text>
</svg>
```

### Mülakatta ne sorulur?

- **Hangi problem?** Dinamik ortamda canlı örneklerin ağ konumunu bulmak. Ölçekleme, şema doğrulama veya şifreleme değil.
- **Eureka hangi model?** İstemci tarafı; istemci defteri sorgular ve örneği kendisi seçer.
- **İki modelin takası**: keşif mantığı uygulamada mı (akıllı ama dile bağımlı, sızan) yoksa altyapıda mı (basit istemci, ekstra katman).
- **Sağlık kontrolü**: sürekli çalışır, listeyi canlı tutar, readiness ile hazır olmayan örneği korur.
- **Kubernetes**: Service + ClusterIP + kararlı DNS = yerleşik sunucu tarafı keşif; namespace'ler arası çağrı FQDN ile yapılır; K8s üzerinde Eureka çoğunlukla gereksiz katmandır.
- **CAP**: Eureka AP-eğilimli, ZooKeeper/etcd CP-eğilimli, Consul ayarlanabilir. Keşifte "bayat ama erişilebilir" genelde yeğdir.
- **Tuzak**: "Keşif varsa yeniden denemeye ve devre kesiciye gerek yok" — hayır. Liste bayat olabileceği için dayanıklılık desenleri yine gerekir.

### Tek cümlede

Servis keşfi, adreslerin sürekli değiştiği bir dünyada "kiminle konuşacağım" sorusunu yanıtlar; bunu canlı bir kayıt defteri ve sağlık kontrolüyle yapar. Bu alandaki iki temel karar şudur: keşif mantığı uygulamada mı (Eureka) yoksa platformda mı (Kubernetes Service) duracak, ve ağ bölünmesinde tutarlılık mı erişilebilirlik mi seçilecek.

# disc-1 | Temel
## Soru
Mikroservislerde servis keşfi (service discovery) hangi temel problemi çözer?
## A
Bir servisin kaç örneğinin çalışacağına karar verip otomatik ölçeklemeyi yönetmek
🤖 Ölçekleme kararı ayrı bir mekanizmadır; keşif yalnız mevcut örneklerin adresini bulur.
## B
Servisler arası mesajların şemasını kaydedip sürüm uyumluluğunu doğrulamak
🤖 Bu şema kayıt defterinin (schema registry) işidir; servis kayıt defteri adres tutar.
## C
Örnekler dinamik gelip gittiği için çağıranın canlı örneklerin ağ konumunu bulmasını sağlamak
🤖 Doğru: keşif, dinamik ortamda değişen IP/port adreslerini bulma problemini çözer.
## D
Servisler arası çağrıları şifreleyip çağıranın kimliğini kriptografik olarak doğrulamak
🤖 Bu mTLS/service mesh konusudur; keşif güvenlik değil adresleme problemidir.
## Doğru
C
## Açıklama
Mikroservis ortamı **dinamiktir**: otomatik ölçekleme örnek sayısını değiştirir, her dağıtımda konteynerler **farklı IP ve porta** doğar, arızalı örnekler ölür ve yenileri gelir. Bu yüzden "ödeme servisi hangi adreste?" sorusunun sabit, elle yazılabilecek bir yanıtı yoktur. **Servis keşfi**, çağıran servisin, hedef servisin o an **canlı ve sağlıklı örneklerinin ağ konumunu** (IP:port) **dinamik olarak** bulmasını sağlar. Merkezinde bir **servis kayıt defteri** vardır: örnekler doğduğunda kendilerini kaydeder, öldüklerinde veya sağlık kontrolünü geçemediklerinde listeden düşer. Karıştırılan komşu kavramlar şunlardır: örnek sayısına karar vermek ölçekleyicinin (A), mesaj biçimlerini saklamak **şema kayıt defterinin** (B), trafiği şifreleyip çağıranı doğrulamak ise mTLS/service mesh'in (D) işidir.

# disc-2 | Temel
## Soru
Netflix Eureka, aşağıdaki servis keşif modellerinden hangisinin klasik örneğidir?
## A
Sunucu tarafı keşif; çağıran sabit bir yönlendirici adresine gider, kayıt defterini hiç görmez
🤖 Bu Kubernetes Service gibi platform çözümlerini tanımlar; Eureka'da defteri istemci sorgular.
## B
DNS tabanlı platform keşfi; uygulama yalnız kararlı bir DNS adına çağrı yapar
🤖 Bu Kubernetes'in modelidir; Eureka'da adres listesi istemci kütüphanesine gelir.
## C
Bir devre kesici (circuit breaker) kütüphanesi; keşif değil hata yalıtımı sağlar
🤖 Devre kesici ayrı bir dayanıklılık desenidir; Eureka bir servis kayıt defteridir.
## D
İstemci tarafı keşif; istemci kayıt defterini sorgular ve yük dengelemeyi kendisi yapar
🤖 Doğru: Eureka + Spring Cloud LoadBalancer klasik istemci tarafı keşif örneğidir.
## Doğru
D
## Açıklama
**Netflix Eureka**, **istemci tarafı keşfin** (client-side discovery) tipik örneğidir. Bu modelde örnekler Eureka **kayıt defterine** kendini kaydeder; çağıran servis kayıt defterini **doğrudan sorgular**, hedef servisin sağlıklı örnek listesini alır ve **yük dengelemeyi kendisi** yapar (modern Spring'de **Spring Cloud LoadBalancer**, eski Ribbon'ın yerine). Avantajı: araya ekstra bir dengeleyici atlaması girmez ve istemci akıllı seçim (bölge-duyarlı vb.) yapabilir. Dezavantajı: keşif ve dengeleme mantığı **her istemciye** ve her dil için bir kütüphaneye girer. Bunun karşıtı **sunucu tarafı keşiftir** (A ve B'nin tarif ettiği Kubernetes Service modeli): orada istemci sabit bir adrese ya da kararlı bir DNS adına çağrı yapar, seçim altyapıda olur. Eureka bir devre kesici (C) de değildir; devre kesici, keşiften bağımsız bir dayanıklılık desenidir.

# disc-3 | Orta
## Soru
İstemci tarafı keşif ile sunucu tarafı keşif arasındaki temel takas nedir?
## A
İstemci tarafında keşif mantığı uygulamaya girer; sunucu tarafında altyapıya iner
🤖 Doğru: takas, akıllı ama sızan/dile bağımlı istemci ile basit istemci + ek katman arasındadır.
## B
İstemci tarafı keşifte kayıt defteri gerekmez; adresler yapılandırma dosyasına elle yazılır
🤖 Elle yazılan adres zaten keşif değildir; istemci tarafı modelde de kayıt defteri şarttır.
## C
Sunucu tarafında da kayıt defterini istemci sorgular; fark yalnız dengeleme algoritmasıdır
🤖 Sunucu tarafında istemci defteri hiç sorgulamaz; sorguyu altyapı yapar.
## D
İstemci tarafı yalnız senkron REST ile, sunucu tarafı yalnız asenkron mesajlaşma ile çalışır
🤖 İki model de iletişim biçiminden bağımsızdır; ayrım mantığın nerede durduğudur.
## Doğru
A
## Açıklama
İki modelin takası **keşif ve yük dengeleme mantığının nerede yaşadığıdır**. **İstemci tarafı** (Eureka + Spring Cloud LoadBalancer): istemci kayıt defterini sorgular ve örneği kendisi seçer. Artısı: araya ekstra bir dengeleyici atlaması girmez, istemci akıllı/bölge-duyarlı seçim yapabilir. Eksisi: keşif mantığı **her istemciye** ve **her dile** ait bir kütüphane olarak girer — uygulamaya sızar, dil çeşitliliğinde bakımı zorlaşır. **Sunucu tarafı** (Kubernetes Service, bulut yük dengeleyici): istemci sabit bir adrese/DNS'e çağrı yapar, defteri o hiç sorgulamaz (C'nin yanılgısı); keşif ve dengeleme **altyapıda** olur. Artısı: istemci **basit ve dilden bağımsız** kalır. Eksisi: araya bir yönlendirici katmanı girer ve keşif platforma bağımlı hale gelir. Her iki modelde de bir kayıt defteri vardır (B yanlış) ve ikisi de senkron/asenkron ayrımından bağımsızdır (D yanlış). Modern eğilim, özellikle Kubernetes'te, mantığı uygulamadan çıkarıp platforma indiren sunucu tarafı modeldir.

# disc-4 | Orta
## Soru
Servis keşfinde "sağlık kontrolü" (health check) neden zorunlu bir bileşendir?
## A
Sağlık kontrolü yalnızca log ve metrik üretmek içindir, keşif listesiyle ilgisi yoktur
🤖 Sağlık kontrolü doğrudan keşif listesini besler; hangi örneğin trafik alacağını belirler.
## B
Sağlıksız örnekler listeden çıkarılır; böylece istekler ölü veya hazır olmayan örneklere düşmez
🤖 Doğru: sağlık sinyali listeyi canlı tutar ve trafiğin ölü örneğe gitmesini engeller.
## C
Sağlık kontrolü yalnızca ilk kayıt anında bir kez yapılır, sonrasında gerekmez
🤖 Tek seferlik kontrol yetmez; örnek sonradan bozulabilir, bu yüzden sinyal sürekli izlenir.
## D
Sağlık kontrolü CPU ve bellek metriklerini toplar ama trafik yönlendirmesini etkilemez
🤖 Metrik toplama ayrı bir iştir; sağlık kontrolünün sonucu doğrudan yönlendirmeyi değiştirir.
## Doğru
B
## Açıklama
Servis keşfi yalnız "hangi örnekler var" değil, "hangi örnekler **sağlıklı**" sorusunu da yanıtlamalıdır — aksi hâlde çağrılar ölü veya yanıt vermeyen örneklere düşer. **Sağlık kontrolü** bunu sağlar: kayıt defteri veya dengeleyici, her örnekten **düzenli** bir sağlık sinyali izler — Eureka'da periyodik **heartbeat**, Consul'da HTTP/TCP/script kontrolleri, Kubernetes'te **readiness/liveness probe** (ör. Spring Boot Actuator'ın `/health` ucu). Kontrolü geçemeyen örnek **keşif listesinden çıkarılır** (deregister/drain), tekrar sağlıklı olunca listeye döner. Bu izleme **süreklidir**; tek seferlik bir kayıt kontrolü (C) işe yaramaz, çünkü örnek sonradan bozulabilir. Sağlık kontrolü salt gözlem amaçlı da değildir (A, D): sonucu doğrudan trafiğin nereye yönleneceğini değiştirir. Özellikle **readiness probe**, henüz hazır olmayan (önyükleme, önbellek ısıtma) bir örneğin erken trafik almasını da önler.

# disc-5 | İleri
## Soru
Bir ekip 2026'da yeni servislerini Kubernetes üzerinde çalıştırıyor. Servis keşfi için ayrıca Netflix Eureka kurmayı tartışıyorlar. En yerinde değerlendirme hangisidir?
## A
Eureka gereklidir; Kubernetes yalnız konteyner çalıştırır, servis keşfi sağlamaz
🤖 Kubernetes, Service nesnesi ve DNS ile yerleşik servis keşfi sağlar.
## B
Eureka kurulup Kubernetes'in Service nesnesi devre dışı bırakılarak keşif tek yerde toplanmalıdır
🤖 Platform keşfini kapatmak yerleşik ve bakımsız gelen bir yeteneği elle yeniden kurmaktır.
## C
Kubernetes keşfi yalnız aynı namespace içinde çalıştığından, namespace'ler arası çağrı için Eureka şarttır
🤖 tam nitelikli DNS adıyla (svc.cluster.local) namespace'ler arası çağrı yapılabilir.
## D
Kubernetes Service ve kararlı DNS adı zaten keşif sağlar; Eureka gereksiz bir katman olur
🤖 Doğru: K8s üzerinde keşif platformun işidir; Eureka çift keşif katmanı ve fazladan yük getirir.
## Doğru
D
## Açıklama
2026 gerçeği: **Kubernetes, servis keşfinde Eureka'yı büyük ölçüde geriletmiştir**. Uygulama Kubernetes üzerindeyse keşif **platformun işidir**: bir **Service** nesnesi kalıcı bir sanal IP (ClusterIP) ve **kararlı bir DNS adı** (`odeme.namespace.svc.cluster.local`) sağlar; arkasındaki Pod'lar ölçekleme/dağıtımla değişse de bu ad sabit kalır, kube-proxy trafiği sağlıklı Pod'lara dağıtır, **readiness probe** sağlıksız Pod'ları listeden düşürür. Bu ad namespace'e hapsolmuş da değildir (C'nin yanılgısı): tam nitelikli adla başka namespace'lerdeki servislere de çağrı yapılır. K8s üzerine ayrıca Eureka koymak **iki keşif katmanı**, çift kayıt, senkronizasyon sorunları ve gereksiz operasyon yükü demektir; platformun yerleşik keşfini kapatıp Eureka'ya geçmek (B) ise bakımsız gelen bir yeteneği elle yeniden kurmaktır. Eureka'nın hâlâ anlamlı olduğu yerler: Kubernetes **dışı** dağıtımlar ve mevcut Spring Cloud Netflix sistemleri. Spring tarafında modern yol `spring-cloud-kubernetes` ile platform keşfini benimsemektir.

# disc-6 | İleri
## Soru
Bir kayıt defteri seçilirken Consul ile ZooKeeper/etcd gibi güçlü tutarlı (CP) sistemler arasında CAP açısından hangi değerlendirme doğrudur?
## A
Keşifte tutarlılık her zaman önceliklidir, bu yüzden daima en katı CP sistem seçilmelidir
🤖 Aşırı genelleme: keşifte çoğu zaman bayat ama erişilebilir liste, erişilemez defterden iyidir.
## B
CAP kayıt defteri seçimiyle ilgisizdir; tutarlılık-kullanılabilirlik takası burada rol oynamaz
🤖 Kayıt defteri dağıtık bir depodur; bölünmedeki tercih davranışını doğrudan belirler.
## C
Bölünmede defter ya tutarlılığı ya kullanılabilirliği önceler; keşifte genelde AP eğilimi yeğlenir
🤖 Doğru: Eureka AP, ZooKeeper/etcd CP eğilimlidir; Consul'da tutarlılık düzeyi ayarlanabilir.
## D
Consul yalnızca CP, ZooKeeper yalnızca AP çalışır ve bu davranış değiştirilemez
🤖 Ters ve eksik: ZooKeeper/etcd CP eğilimlidir, Consul'un okuma tutarlılığı ayarlanabilir.
## Doğru
C
## Açıklama
Bir servis kayıt defteri dağıtık bir veri deposudur; dolayısıyla **CAP teoremi** onun için doğrudan geçerlidir (B yanlış). Bir **ağ bölünmesinde** sistem ya **tutarlılığı** (CP) ya **kullanılabilirliği** (AP) önceler: **CP** sistemler (ZooKeeper, etcd) bölünen tarafta yazma/okumayı reddedebilir — veri **asla yanlış** olmaz ama defter geçici olarak **erişilemez** olabilir. **AP** sistemler (Eureka) her zaman yanıt verir ama örnek listesi geçici olarak **bayat** olabilir. **Servis keşfi** için genellikle **AP eğilimi tercih edilir**: "biraz bayat ama erişilebilir" bir liste, "tutarlı ama erişilemez" bir defterden iyidir — çünkü defter erişilemezse tüm çağrılar durur, oysa hafifçe eski bir liste yalnız yeni gelen/ölen birkaç örnekte hataya yol açar ve çağıranlar zaten yeniden deneme + devre kesici kullanır. Yine de bu "her zaman CP kötüdür" demek değildir (A'nın tersi bir aşırılık olurdu): lider seçimi gibi işler CP gerektirir. Özet: **Eureka AP eğilimlidir** (self-preservation modu bunu vurgular), **ZooKeeper/etcd CP eğilimlidir**, **Consul** ise Raft tabanlıdır ve okuma tutarlılığı (stale/consistent) ayarlanabilir.
