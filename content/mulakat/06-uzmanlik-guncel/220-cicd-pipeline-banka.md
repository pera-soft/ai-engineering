---
slug: cicd-pipeline-banka
baslik: "Kurumsal CI/CD Hattı: Ortam Promosyonu, Şema Göçü, Sır Yönetimi ve Denetim"
kademe: İleri
sira: 220
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir bankada kredi başvurusunun izlediği yolu düşün. Başvuru şubeden girer; önce evrak eksiği bakılır (en ucuz kontrol, saniyeler sürer), sonra skorlama çalışır, sonra tahsis komitesi karar verir, en sonunda kullandırım yapılır. Sıra rastgele değildir: ucuz ve eleyici kontroller **önce** koşar, pahalı olanlar sonra. Hiç kimse bir adımı atlayamaz, her adımda "kim, ne zaman, neye dayanarak onayladı" kaydı tutulur ve başvuruyu hazırlayan kişi tek başına parayı kasadan çıkaramaz. **CI/CD hattı (pipeline)** yazılımdaki aynısıdır. Kod hattın bir ucundan girer, her aşama kendi kontrolünü yapar, öbür ucundan üretime çıkacak paket çıkar. Düzenlemeye tabi (regüle) bir kurumda bu yolun her adımı, tıpkı kredi dosyası gibi, denetlenebilir olmak zorundadır.

### Temel kavramlar

- **Hat (pipeline) ve aşama (stage):** hattın sırayla koşan bölümleri. Bir aşamanın içinde birden çok **iş (job)** paralel çalışabilir; iş de **adımlardan (step)** oluşur. Bir aşama kırmızıysa sonrakiler çalışmaz.
- **Koşucu / ajan (runner / agent):** işi fiilen çalıştıran makine ya da konteyner. Jenkins'te `agent`/`node`, GitLab CI'da `runner` denir. Kavram eşlemesi: Jenkins `Jenkinsfile` + `stage {}` ≈ GitLab `.gitlab-ci.yml` + `stages`/`jobs`; Jenkins `archiveArtifacts` ≈ GitLab `artifacts`; ikisinde de bağımlılık dizini için **önbellek (cache)** ve işler arası dosya aktarımı için **artefakt** ayrı kavramlardır.
- **Artefakt (artifact):** bir aşamanın ürettiği ve sonraki aşamaya taşınan çıktı (jar, imaj etiketi, test raporu). **Önbellek** ise hızlandırma amaçlıdır; kaybolursa yalnız süre uzar, sonuç değişmez. Artefakt kaybolursa hat çalışmaz — bu ikisi karıştırılmamalıdır.
- **Kalite kapısı (quality gate):** kapsam (coverage), statik analiz bulgusu, bağımlılık zafiyeti gibi eşiklerin denetlendiği ve eşik tutmazsa hattı **durduran** aşama. Rapor üretip geçmeye devam eden şey kapı değildir.
- **Yeniden-üretilebilir derleme (reproducible build):** aynı kaynak kodun, farklı zamanda ve farklı makinede derlendiğinde **aynı davranan** çıktıyı vermesi. Şartı: bağımlılık sürümlerinin sabitlenmesi (pin), taban imajın sabit etiketle ya da içerik özetiyle (digest) belirtilmesi, `latest` yasağı.
- **Artefakt deposu (Nexus / Artifactory):** kurumun kendi paket ve imaj deposu. Aynı zamanda dış depoların **aynası (mirror)** olarak çalışır: geliştirici doğrudan internete çıkmaz, tüm bağımlılıklar aynadan gelir. Dış ağa kapalı (air-gapped) ortamda hattın çalışabilmesinin tek yolu budur; ayrıca lisans ve zafiyet denetimi tek noktadan yapılır.
- **Promosyon (promotion):** ortamdan ortama geçirilen şeyin **yeni bir derleme değil, sınanmış artefaktın kendisi** olması. Terfi eden imajın içerik özeti (digest) her ortamda aynıdır.
- **Sır (secret):** parola, anahtar, sertifika. Kasada (HashiCorp Vault, kurumsal anahtar yönetimi) ya da hattın gizli değişkenlerinde durur; çalışma anında ortam değişkeni veya bağlanan dosya olarak verilir, imaja ve depoya **girmez**, günlüğe (log) yazılmaz.
- **Şema göçü (migration):** veritabanı yapısının sürümlü değişimi. **Flyway** sürümlü SQL betikleriyle, **Liquibase** değişiklik kütüğüyle (changelog) çalışır; ikisi de uygulanan göçleri bir tabloda tutup aynı göçü iki kez uygulamaz.
- **Görevler ayrılığı (segregation of duties):** değişikliği yazan ile üretime çıkaran/onaylayanın farklı kişiler olması. **Denetim izi (audit trail):** hangi artefaktın, hangi onayla, kim tarafından, ne zaman hangi ortama gittiğinin değiştirilemez kaydı.

```svg
<svg viewBox="0 0 660 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="CI/CD hattının aşamaları ve hızlı geri bildirim sırası">
  <defs>
    <marker id="pipebank-ok" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hat anatomisi: ucuz ve eleyici olan önce</text>
  <rect x="16" y="60" width="90" height="62" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="61" y="84" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">derle</text>
  <text x="61" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~2 dk</text>
  <rect x="124" y="60" width="90" height="62" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="169" y="84" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">birim test</text>
  <text x="169" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~4 dk</text>
  <rect x="232" y="60" width="90" height="62" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="277" y="84" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">kalite kapısı</text>
  <text x="277" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~3 dk</text>
  <rect x="340" y="60" width="90" height="62" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="385" y="84" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">imaj üret</text>
  <text x="385" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~5 dk</text>
  <rect x="448" y="60" width="90" height="62" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="493" y="84" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">imaj tara</text>
  <text x="493" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~6 dk</text>
  <rect x="556" y="60" width="90" height="62" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="601" y="84" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">dağıt</text>
  <text x="601" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~2 dk</text>
  <line x1="108" y1="91" x2="121" y2="91" stroke="#5D7789" stroke-width="1.3" marker-end="url(#pipebank-ok)"/>
  <line x1="216" y1="91" x2="229" y2="91" stroke="#5D7789" stroke-width="1.3" marker-end="url(#pipebank-ok)"/>
  <line x1="324" y1="91" x2="337" y2="91" stroke="#5D7789" stroke-width="1.3" marker-end="url(#pipebank-ok)"/>
  <line x1="432" y1="91" x2="445" y2="91" stroke="#5D7789" stroke-width="1.3" marker-end="url(#pipebank-ok)"/>
  <line x1="540" y1="91" x2="553" y2="91" stroke="#5D7789" stroke-width="1.3" marker-end="url(#pipebank-ok)"/>
  <text x="330" y="150" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Derleme hatası 2. dakikada bilinir; 35. dakikada değil.</text>
  <text x="330" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Birbirine bağımlı olmayan işler (test + statik analiz) aynı aşamada paralel koşar.</text>
  <text x="330" y="190" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Testi hattan çıkarıp geceye atmak süreyi kısaltmaz, geri bildirimi geciktirir.</text>
</svg>
```

### Nasıl çalışır?

1. **Kaynak sabitlenir.** Hat, bir dal adıyla değil belirli bir **commit** ile çalışır. Sürüm numarası derleme numarası ve commit özeti ile üretilir (ör. `2.14.0-b1487+9f3c1a2`); böylece üretimdeki her kutunun hangi kaynaktan geldiği geriye doğru izlenebilir.
2. **Bağımlılıklar aynadan çekilir.** Maven/Gradle kurum aynasına (Nexus/Artifactory) bakacak biçimde yapılandırılır. Sürüm aralığı (`1.+`) ve `latest` etiketi yasaktır; her bağımlılık ve taban imaj sabittir. Aynı commit bir ay sonra derlendiğinde aynı ikili çıkmalıdır.
3. **Ucuz kontroller önce koşar.** Derleme → birim test → kalite kapısı. Bu üçü dakikalar içinde biter ve hatalı commit'i erken eler. Birbirine bağımlı olmayan işler paralelleştirilir.
4. **İmaj bir kez üretilir.** Konteyner imajı tek seferde üretilip kurum içi kayıt defterine (registry) itilir ve **içerik özetiyle (digest)** anılır. Zafiyet taraması bu imaj üzerinde yapılır.
5. **Ortamlar arasında terfi ettirilir.** Aynı imaj sırasıyla dev → test → UAT → prod ortamlarına dağıtılır. Ortamlar arasındaki tek fark **dışarıdan verilen yapılandırma ve sırlardır**: veritabanı adresi, kuyruk adı, zaman aşımı, günlük seviyesi.
6. **Şema göçü uygulamadan önce ve geriye uyumlu koşar.** Flyway/Liquibase göçü, yeni sürüm dağıtılmadan önce çalışır; ama şema hem eski hem yeni uygulama sürümüyle çalışabilecek biçimde tasarlanır.
7. **Dağıtım stratejisi seçilir.** Mavi-yeşil ya da kanarya; ikisinin operasyonel bedeli farklıdır (aşağıda).
8. **Kayıt bırakılır.** Hat, değişiklik kaydı (change record) numarasını, onaylayan kişiyi, terfi eden imaj digest'ini ve zaman damgasını denetim izine yazar.

```svg
<svg viewBox="0 0 660 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Aynı imajın dev, test, UAT ve prod ortamları arasında terfi ettirilmesi">
  <defs>
    <marker id="pipebank-terfi" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek imaj terfi eder, yalnız yapılandırma değişir</text>
  <rect x="14" y="60" width="132" height="96" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="80" y="88" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">Tek derleme</text>
  <text x="80" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">derleme #1487</text>
  <text x="80" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">imaj: sha256:9f3c…</text>
  <text x="80" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bir kez üretilir</text>
  <line x1="148" y1="108" x2="169" y2="108" stroke="#186399" stroke-width="1.4" marker-end="url(#pipebank-terfi)"/>
  <rect x="172" y="60" width="104" height="96" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="224" y="88" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">dev</text>
  <text x="224" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">aynı digest</text>
  <text x="224" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kendi config'i</text>
  <line x1="278" y1="108" x2="285" y2="108" stroke="#186399" stroke-width="1.4" marker-end="url(#pipebank-terfi)"/>
  <rect x="288" y="60" width="104" height="96" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="340" y="88" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">test</text>
  <text x="340" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">aynı digest</text>
  <text x="340" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">maskeli veri</text>
  <line x1="394" y1="108" x2="401" y2="108" stroke="#186399" stroke-width="1.4" marker-end="url(#pipebank-terfi)"/>
  <rect x="404" y="60" width="104" height="96" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="456" y="88" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">UAT</text>
  <text x="456" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">aynı digest</text>
  <text x="456" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iş birimi onayı</text>
  <line x1="510" y1="108" x2="517" y2="108" stroke="#186399" stroke-width="1.4" marker-end="url(#pipebank-terfi)"/>
  <rect x="520" y="60" width="104" height="96" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="572" y="88" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">prod</text>
  <text x="572" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">aynı digest</text>
  <text x="572" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">onay + kayıt</text>
  <text x="330" y="182" text-anchor="middle" font-size="11" fill="#B23B3B" font-family="system-ui">Her ortamda yeniden derlemek: prod'a çıkan ikili, test edilen ikili DEĞİLDİR.</text>
  <text x="330" y="204" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Fark yalnız dışarıdan verilen yapılandırma ve sırlarda olmalıdır.</text>
</svg>
```

**Sır yönetimi.** Kural basittir: sır imaja gömülmez, depoya konmaz, günlüğe yazılmaz. Uygulamada üç yaygın hata vardır. Birincisi parolayı `application-prod.yml` içine düz metin yazmak — depoya erişen herkes onu görür, geçmişten silmek de zordur. İkincisi sırrı imaja gömmek: imaj kurum içi kayıt defterinde olsa bile katmanları okuyan herkes onu çıkarabilir, üstelik parola değişince imajı yeniden üretmek gerekir. Üçüncüsü hattın adımında `curl -u kullanici:parola ...` gibi bir komut çalıştırmak: komut satırı hattın günlüğüne düşer ve günlükleri okuyabilen herkes parolayı görür. Doğrusu, sırrın kasada tutulması, hat tarafından çalışma anında **ortam değişkeni ya da bağlanan dosya** olarak verilmesi, hattın günlük çıktısında maskelenmesi ve düzenli olarak değiştirilebilir (rotate) olmasıdır. Base64 bir şifreleme değil kodlamadır; sırrı gizlemez.

**Veritabanı şema göçü.** Yuvarlanan güncelleme (rolling update) sırasında eski ve yeni uygulama sürümü bir süre **aynı anda** çalışır ve **aynı şemayı** paylaşır. Bu yüzden uygulama ile şemayı aynı anda değiştirmek mümkün değildir; şema değişimi **geriye uyumlu** olmak zorundadır. Kullanılan desen **genişlet/daralt (expand/contract)** desenidir ve üç adımlıdır. Önce şema genişletilir: yeni kolon `NULL` kabul eder biçimde eklenir, eski kolon yerinde durur. Sonra yeni sürüm iki kolona da yazacak biçimde dağıtılır ve eski veri arka planda taşınır. En sonunda eski kolon düşürülür; bu ancak tüm eski sürümler üretimden çekildikten sonra ve **ayrı bir dağıtımda** yapılır.

`NOT NULL` bir kolonu doğrudan eklemek, kolonu yeniden adlandırmak ya da düşürmek eski sürümü anında kırar. Ayrıca göçün "geri alınabilir" olduğu yanılgısına düşülmemelidir. Liquibase geri alma tanımlayabilir; Flyway'in geri alma özelliği ise ücretli sürümdedir. Ama hiçbiri düşürülen kolonun verisini geri getirmez. Üretimdeki gerçek geri alma planı **ileriye doğru düzeltici göç** (roll-forward) ya da yedekten dönmedir.

```svg
<svg viewBox="0 0 660 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Genişlet ve daralt deseniyle geriye uyumlu şema göçü">
  <defs>
    <marker id="pipebank-goc" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Genişlet → taşı → daralt (üç ayrı dağıtım)</text>
  <rect x="16" y="48" width="188" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="110" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">1 · Genişlet</text>
  <text x="110" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yeni kolon eklenir</text>
  <text x="110" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">(NULL kabul eder)</text>
  <text x="110" y="142" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">eski kolon yerinde kalır</text>
  <text x="110" y="164" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">eski sürüm etkilenmez</text>
  <line x1="206" y1="118" x2="231" y2="118" stroke="#5D7789" stroke-width="1.3" marker-end="url(#pipebank-goc)"/>
  <rect x="236" y="48" width="188" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">2 · Çift yazım / taşıma</text>
  <text x="330" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yeni sürüm iki kolona</text>
  <text x="330" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">da yazar, yeniden okur</text>
  <text x="330" y="142" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">eski veri arka planda</text>
  <text x="330" y="164" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">taşınır, geri alma açık</text>
  <line x1="426" y1="118" x2="451" y2="118" stroke="#5D7789" stroke-width="1.3" marker-end="url(#pipebank-goc)"/>
  <rect x="456" y="48" width="188" height="140" rx="12" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="550" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">3 · Daralt</text>
  <text x="550" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tüm eski sürümler</text>
  <text x="550" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çekildikten SONRA</text>
  <text x="550" y="142" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">eski kolon düşürülür</text>
  <text x="550" y="164" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">bu adım geri alınamaz</text>
  <text x="330" y="214" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Rolling update sırasında eski ve yeni sürüm AYNI şemayı paylaşır.</text>
  <text x="330" y="232" text-anchor="middle" font-size="10.5" fill="#B07D20" font-family="system-ui">Bu yüzden uygulama ile şema aynı anda değiştirilemez; şema geriye uyumlu olmalıdır.</text>
</svg>
```

**Mavi-yeşil ile kanaryanın operasyonel farkı.** **Mavi-yeşil (blue-green)** iki tam kopya ortam tutar: mavi üretimi karşılarken yeşile yeni sürüm kurulur, doğrulanır ve trafik tek hamlede yeşile çevrilir. Geri alma da tek hamledir (trafiği maviye geri al) — bu yüzden hızlıdır. Bedeli çifte kapasite ve şemanın **her iki sürümle birden** uyumlu olma zorunluluğudur; ayrıca geçiş anında tüm kullanıcı aynı anda yeni sürüme geçtiği için sorun herkesi birden bulur. **Kanarya (canary)** ise tek ortamda çalışır: yeni sürüm önce trafiğin küçük bir yüzdesini alır, metrikler (hata oranı, gecikme, iş metrikleri) izlenir, iyiyse pay büyütülür. Bedeli, istek bazında yönlendirme yeteneği ve gerçekten güvenilir bir izleme altyapısıdır; ayrıca geçiş süresince iki sürüm birlikte çalıştığı için şema ve mesaj biçimi uyumluluğu daha uzun süre gerekir. Kabaca: mavi-yeşil **hızlı geri alma**, kanarya **küçük etki alanı** satın alır.

**Banka gerçeği.** Düzenlenmiş bir kurumda hat teknik olarak "tek tuşla prod" yapabilecek durumda olsa bile, süreç bilerek durdurulur. **Görevler ayrılığı** gereği kodu yazan kişi üretime çıkışı tek başına onaylayamaz; üretim dağıtımı bir **değişiklik kaydına** (change record) ve farklı bir kişinin onayına bağlanır, hat bu onayı bekleyen bir kapı içerir. Her dağıtım için **denetim izi** tutulur: hangi commit, hangi imaj digest'i, hangi değişiklik kaydı, kim onayladı, ne zaman gitti, geri alındı mı. Bu kayıt sonradan değiştirilememelidir. Üretim verisi ise ayrı bir başlıktır: geliştiricinin üretim veritabanına erişimi yoktur; alt ortamlar **maskelenmiş (anonimleştirilmiş) test verisi** kullanır — gerçek TCKN, ad-soyad, hesap numarası ve bakiye alt ortamlara kopyalanmaz. Hata ayıklama ihtiyacı, veriye erişimle değil, günlük ve iz (trace) kayıtlarıyla — kişisel veriyi maskeleyerek — karşılanır.

### Mülakatta ne sorulur / tuzaklar

- **"Hattınız kaç dakika ve sıralaması neden böyle?"** Beklenen: ucuz ve eleyici aşamalar önde, pahalı aşamalar arkada; bağımsız işler paralel. "Testleri geceye aldık" cevabı geri bildirimi öldürür ve hattı CI olmaktan çıkarır.
- **"Prod'a çıkarken yeniden derliyor musunuz?"** Bu bir tuzak sorudur. Doğru cevap: hayır, **aynı artefakt terfi eder**. Her ortamda yeniden derlemek, üretime çıkan ikilinin test edilmiş ikili olmadığı anlamına gelir; bağımlılık aynası veya derleyici sürümü arada değişmiş olabilir.
- **"Ortam farkını nasıl yönetiyorsunuz?"** Beklenen: fark yalnız dışarıdan verilen yapılandırma ve sırlarda; imaj değişmez. Ortam adına göre içeride dallanan (`if (ortam.equals("PROD"))`) kod kokudur.
- **"Sırları nerede tutuyorsunuz?"** Beklenen: kasa/CI gizli değişkeni, çalışma anında enjeksiyon, günlükte maskeleme. "Depoda ama base64" cevabı elenme sebebidir.
- **"Kolon adını değiştirmen gerekiyor, kesinti istemiyoruz."** Beklenen: genişlet/daralt, üç ayrı dağıtım, düşürme en sona. "Flyway atomik, tek seferde yaparım" cevabı yuvarlanan güncellemeyi anlamadığını gösterir.
- **"Geliştirici prod'a tek başına basabiliyor mu?"** Beklenen: hayır — görevler ayrılığı, onay kapısı, değişiklik kaydı ve denetim izi. Otomasyon onayın yerine geçmez, onayı **kayıt altına alınabilir** hale getirir.
- **"Üretimde bir hata var, veriye bakabilir misin?"** Beklenen: hayır; üretim verisine erişim yoktur, alt ortamlarda maskeli veri kullanılır, tanı günlük ve izlerle konur.

### Tek cümlede

Kurumsal CI/CD hattı, ucuz kontrolleri öne alan aşamalardan geçerek **bir kez** ve yeniden üretilebilir biçimde imaj üretir. Sonra bu **tek imajı** ortamlar arasında terfi ettirir; ortam farkını yalnız dışarıdan verilen yapılandırmada ve kasadaki sırlarda tutar. Şemayı genişlet/daralt deseniyle geriye uyumlu değiştirir. Üretime çıkışı ise görevler ayrılığı, onay ve denetim izi ile kayıt altına alır.

# pipeline-1 | Orta
## Soru
CI hattınız her `push` sonrası 40 dakika sürüyor ve derleme hatası içeren bir commit bile ancak 35. dakikada başarısız oluyor; çünkü hat önce konteyner imajını üretip zafiyet taraması yapıyor, derleme ve birim testleri sona bırakıyor. Geri bildirimi hızlandırmak için ne yapmalısın?
## A
Bütün aşamaları tek bir işte birleştirip aşama ayrımını kaldırmak; tek iş daha az ek yük ürettiği için hat kısalır
🤖 aşamaları birleştirmek erken elemeyi ortadan kaldırır, hata hâlâ ancak her şey bittikten sonra görünür.
## B
Birim testleri hattan çıkarıp gecelik ayrı bir işe almak; böylece her `push` sonrası hat belirgin biçimde kısalır
🤖 test hattan çıkınca ana dalın yeşilliği garanti olmaktan çıkar; hata ertesi güne, düzeltmesi pahalı bir noktaya kayar.
## C
Ucuz ve eleyici aşamaları öne almak (derle → birim test → kalite kapısı), imaj üretimi ve taramayı arkaya koymak, bağımsız işleri paralel çalıştırmak
🤖 Doğru: hatalı commit ilk dakikalarda elenir, pahalı aşamalar yalnız temiz commit'ler için çalışır.
## D
Zafiyet taramasını hattan tamamen kaldırmak; tarama zaten en uzun süren aşama olduğu için asıl darboğaz odur
🤖 darboğaz taramanın süresi değil sıralaması; taramayı kaldırmak güvenlik kontrolünü yok eder.
## Doğru
C
## Açıklama
Hat tasarımının temel ilkesi **hızlı geri bildirimdir**: bir commit hatalıysa bunu dakikalar içinde öğrenmek gerekir. Bu yüzden **ucuz ve eleyici** aşamalar (derleme, birim test, kalite kapısı) öne, **pahalı** aşamalar (imaj üretimi, zafiyet taraması, dağıtım) arkaya konur; ayrıca birbirine bağımlı olmayan işler aynı aşamada **paralel** koşturulur. Aşamaları tek işte birleştirmek (A) tam tersini yapar: erken eleme kaybolur, hata ancak her şey bittikten sonra görünür. Birim testleri hattan çıkarıp geceye almak (B) süreyi kısaltır ama **sürekli bütünleştirmenin amacını** yok eder; ana dalın her an yeşil olduğu güvencesi kaybolur ve hata, düzeltmesi çok daha pahalı bir noktada bulunur. Zafiyet taramasını kaldırmak (D) da çözüm değildir: sorun taramanın süresi değil, hattın **başında** duruyor olmasıdır — tarama, yalnız derleme ve testten geçmiş commit'ler için çalışmalıdır.

# pipeline-2 | Orta
## Soru
Aynı commit iki hafta arayla derlendiğinde farklı davranan bir jar üretiliyor; üstelik geliştiricinin makinesinde geçen test, hatta başarısız oluyor. Yeniden-üretilebilir (reproducible) derleme için doğru yaklaşım hangisidir?
## A
Tüm bağımlılık sürümlerini ve taban imajı sabitlemek (`latest` ve sürüm aralığı kullanmamak), bağımlılıkları kurum aynasından çekmek, sürümü derleme numarası ve commit özetiyle üretmek
🤖 Doğru: derlemenin girdileri sabitlenince aynı kaynak her zaman aynı davranan çıktıyı verir ve çıktı geriye izlenebilir olur.
## B
Her derlemede bağımlılıkların en güncel sürümünü çekmek; böylece güvenlik yamaları hiçbir ek iş yapılmadan sürekli uygulanmış olur
🤖 sürümü açık bırakmak derlemeyi öngörülemez kılar; yama yönetimi bilinçli sürüm yükseltmeyle, denetimli biçimde yapılır.
## C
Sorunun kaynağı bağımlılık önbelleğidir; her derlemeden önce yerel önbelleği tamamen silmek farklılığı ortadan kaldırır
🤖 önbellek yalnız hızı etkiler; sürüm sabitlenmemişse önbellek silinince farklı sürüm çekilir ve sorun büyür.
## D
Her ortam için ayrı derleme yapıp çıkan jar'ların dosya boyutlarını karşılaştırmak ve fark görülürse elle inceleme başlatmak
🤖 bu bir teşhis alışkanlığıdır, nedeni gidermez; ayrıca ortam başına ayrı derleme sorunu azaltmaz, çoğaltır.
## Doğru
A
## Açıklama
Yeniden-üretilebilirlik, derlemenin **girdilerinin** sabitlenmesiyle sağlanır. Bağımlılıklarda sürüm aralığı (`1.+`) ve konteyner tabanında `latest` etiketi kullanmak, aynı `Dockerfile` ve aynı `pom.xml`in zaman içinde **farklı** ikililer üretmesi demektir; taban imajı sabit etiketle ya da içerik özetiyle (digest) belirtmek bunu keser. Bağımlılıkların kurum aynasından (Nexus/Artifactory) çekilmesi hem dış ağa kapalı ortamda çalışmayı sağlar hem de bir sürümün kaybolması/değişmesi riskini kaldırır; sürümün derleme numarası ve commit özetiyle üretilmesi ise üretimdeki her kutuyu kaynağa kadar izlenebilir kılar. Bağımlılıkları hep güncel çekmek (B) tam tersi etkiyi yapar: derleme öngörülemez olur ve bir gün hiç dokunmadığın servis bozulur — güvenlik yamaları sürüm yükseltmesi olarak, gözden geçirmeyle uygulanır. Önbelleği silmek (C) yalnız süreyi etkiler, sürüm belirsizliğini gidermez. Ortam başına derleyip çıktıları karşılaştırmak (D) nedeni değil belirtiyi kovalar.

# pipeline-3 | İleri
## Soru
Bir ekipte hat, her ortam için ayrı bir derleme çalıştırıyor: dev'e çıkarken bir kez, test'e çıkarken bir kez, prod'a çıkarken bir kez daha derleniyor ve her seferinde ortama ait ayarlar derleme sırasında pakete gömülüyor. Bu kurgunun temel sorunu ve doğrusu nedir?
## A
Sorun yoktur; ortama özgü ayarların derleme sırasında gömülmesi, çalışma zamanında yanlış yapılandırma okunmasını engellediği için en güvenli yöntemdir
🤖 gömülü ayar, ortam başına farklı ikili demektir; üretime çıkan paket hiçbir ortamda sınanmamış olur.
## B
Sorun derleme sayısıdır; prod için ayrı bir dal açıp yalnız oradan derlemek yeterlidir, böylece prod paketi diğerlerinden yalıtılmış olur
🤖 ayrı dal ayrı ikili üretmeye devam eder; ayrıca dallar arası sapma (drift) yeni bir sorun sınıfı ekler.
## C
İmajın her ortamda aynı olması doğrudur, ancak tutarlılık için yapılandırmanın da imajın içine konması ve ortam adına göre kod içinde seçilmesi gerekir
🤖 yapılandırma imaja girerse imaj artık ortama özgüdür; bir ayar değişikliği için yeniden derleme gerekir.
## D
Sınanmış tek imaj (aynı içerik özeti/digest) ortamlar arasında terfi ettirilmeli; ortamlar arasındaki fark yalnız dışarıdan verilen yapılandırma ve sırlarda olmalıdır
🤖 Doğru: üretimde çalışan ikili, test ve UAT'ta doğrulanan ikilinin tam olarak aynısı olur.
## Doğru
D
## Açıklama
**Promosyon (promotion)** ilkesi şudur: ortamdan ortama terfi eden şey yeni bir derleme değil, **sınanmış artefaktın kendisidir**. Her ortamda yeniden derlersen prod'a çıkan ikili, test ve UAT'ta doğrulanan ikili **değildir**; arada bağımlılık aynasındaki bir sürüm, derleyici yaması ya da hattın ortamı değişmiş olabilir ve bunu fark etmenin yolu yoktur (A yanlış). Prod için ayrı dal açmak (B) sorunu çözmez, yalnız kılık değiştirir: yine ayrı bir ikili üretilir, üstüne dallar arası sapma riski eklenir. Yapılandırmayı imaja gömmek (C) ise imajı ortama özgü kılar — aynı imaj artık dört ortamda çalışamaz ve tek bir zaman aşımı değeri için yeniden derleme gerekir; ayrıca kod içinde ortam adına bakıp dallanmak, üretimde ancak üretimde ortaya çıkan hatalar üretir. Doğru kurgu: imaj **bir kez** üretilir, **içerik özetiyle** anılır ve dev → test → UAT → prod boyunca aynı digest terfi eder; ortam farkı `ConfigMap`, ortam değişkeni veya yapılandırma sunucusu üzerinden **dışarıdan** verilir, sırlar da kasadan gelir.

# pipeline-4 | İleri
## Soru
Kredi servisinin hattında veritabanı parolası `application-prod.yml` içinde düz metin duruyor; bir adım da `curl -u servis:$PAROLA ...` komutunu çalıştırıyor ve bu komut hattın günlüğüne (log) olduğu gibi düşüyor. Kurumsal sır yönetimi açısından doğru düzeltme nedir?
## A
Parolayı depodan çıkarıp konteyner imajının içine bir dosya olarak gömmek; imaj kurum içi kayıt defterinde tutulduğu için dışarıya sızmaz
🤖 imaj katmanlarını çekebilen herkes gömülü dosyayı okuyabilir; ayrıca parola değişince imajı yeniden üretmek gerekir.
## B
Sırları kasada (Vault) veya hattın gizli değişkenlerinde tutmak, çalışma anında ortam değişkeni ya da bağlanan dosya olarak vermek, günlükte maskelemek ve düzenli olarak değiştirmek
🤖 Doğru: sır ne depoda ne imajda durur; yalnız çalışma anında, erişimi kayıtlı biçimde verilir ve günlüğe sızmaz.
## C
Parolayı base64 ile kodlayıp depoda tutmak; okunabilir metin ortadan kalktığı için kod incelemesinde ve günlükte fark edilmez hâle gelir
🤖 base64 bir şifreleme değil kodlamadır; tek komutla çözülür ve hiçbir gizlilik sağlamaz.
## D
Parolayı `ConfigMap` içinde açık tutup güvenliği ağ katmanında sağlamak; kümeye erişemeyen zaten değeri okuyamaz
🤖 `ConfigMap` gizli veri için tasarlanmamıştır, değeri açık tutar ve kümedeki geniş bir kitleye okunur kılar.
## Doğru
B
## Açıklama
Sır yönetiminin üç kuralı vardır: sır **depoya girmez**, **imaja gömülmez**, **günlüğe yazılmaz**. Parolayı imaja gömmek (A) sızıntıyı önlemez — imajı çekebilen herkes katmanları açıp dosyayı okuyabilir — ve parola değişimini imaj yeniden üretimine bağlar. Base64 (C) bir **kodlamadır**, şifreleme değildir; tek komutla geri çözülür ve depoda düz metinden anlamlı biçimde daha güvenli değildir. `ConfigMap` (D) gizli veri için tasarlanmamıştır; ağ kısıtı da kümenin içindeki geniş erişimi ortadan kaldırmaz. Doğru desen, sırrın bir **kasada** (HashiCorp Vault ya da kurumsal anahtar yönetimi) veya hattın gizli değişkenlerinde durması, hatta yalnız **çalışma anında** ortam değişkeni ya da bağlanan dosya olarak enjekte edilmesi, hattın günlük çıktısında **maskelenmesi** ve düzenli olarak değiştirilebilmesidir. Sorudaki `curl -u servis:$PAROLA` örneği ayrı bir tuzağa işaret eder: değer gizli değişkenden gelse bile komut satırı günlüğe düşerse sır açığa çıkar; bu yüzden sırlar komut satırı yerine ortam değişkeni veya dosya üzerinden verilir ve hattın maskeleme özelliği açılır.

# pipeline-5 | İleri
## Soru
`musteri_no` kolonunu `musteri_numarasi` olarak yeniden adlandırman gerekiyor. Servis Kubernetes'te üç kopya çalışıyor ve yuvarlanan güncelleme (rolling update) sırasında eski ve yeni sürüm bir süre birlikte hizmet verecek. Kesintisiz geçiş için doğru göç stratejisi nedir?
## A
Genişlet/daralt: önce yeni kolonu `NULL` kabul eder biçimde ekle, yeni sürüm iki kolona da yazsın ve veri arka planda taşınsın, eski kolonu ancak tüm eski sürümler çekildikten sonra ayrı bir dağıtımda düşür
🤖 Doğru: her ara adımda şema hem eski hem yeni sürümle uyumlu kalır, bu yüzden hiçbir anda kesinti oluşmaz.
## B
Göçü ve yeni sürümü aynı dağıtımda çıkar; Flyway göçü tek işlemde çalıştırdığı için kolon adı değişimi anlık ve atomiktir, iki sürüm arasında boşluk kalmaz
🤖 göç atomik olsa bile hâlâ ayakta olan eski sürüm o andan itibaren olmayan kolonu sorgular ve hata verir.
## C
Göç betiklerini uygulama kapanırken (`shutdown`) çalıştır; böylece şema değişimi trafiğin en düşük olduğu ana denk gelir ve etkilenen istek sayısı azalır
🤖 kapanan kopya şemayı değiştirirse hâlâ çalışan kopyalar anında bozulur; ayrıca göç sırası öngörülemez hale gelir.
## D
Her göç için ters (geri alma) betiği yaz ve sorun çıkarsa şemayı önceki haline döndür; bu, kesintisiz geçişin güvencesidir
🤖 düşürülen kolonun verisi geri gelmez ve geri alınan şema, hâlâ ayakta olan yeni sürümü bozar.
## Doğru
A
## Açıklama
Yuvarlanan güncellemenin tanımı gereği eski ve yeni sürüm bir süre **aynı anda** çalışır ve **aynı şemayı** paylaşır; bu yüzden uygulama ile şema aynı anda değiştirilemez ve şema değişimi **geriye uyumlu** olmak zorundadır. Çözüm **genişlet/daralt (expand/contract)** desenidir ve üç ayrı dağıtıma yayılır: (1) yeni kolon `NULL` kabul eder biçimde eklenir, eski kolon yerinde kalır — eski sürüm hiç etkilenmez; (2) yeni sürüm her iki kolona da yazar, okurken yeniyi tercih eder, eski satırlar arka planda taşınır; (3) tüm eski sürümler üretimden çekildikten sonra, ayrı bir dağıtımda eski kolon düşürülür. Göçü ve sürümü aynı anda çıkarmak (B) yaygın bir yanılgıdır: göç işlemsel olsa bile hâlâ ayakta olan eski kopyalar o andan itibaren var olmayan kolonu sorgular. Göçü kapanış anında çalıştırmak (C) sırayı öngörülemez kılar ve ayakta kalan kopyaları anında bozar. Geri alma betiğine güvenmek (D) ise sahte bir güvencedir: Liquibase geri alma tanımlayabilir, Flyway'in geri alma özelliği ücretli sürümdedir, ama **düşürülen kolonun verisi geri gelmez**; üretimdeki gerçek geri alma planı ileriye doğru düzeltici göç ya da yedekten dönmedir.

# pipeline-6 | Uzman
## Soru
Banka denetçisi hattı inceliyor ve soruyor: "Kodu yazan geliştirici, üretime tek başına dağıtım yapabiliyor mu? Kim, ne zaman, hangi paketi çıkardı — kanıtlayabiliyor musunuz?" Hat bugün tamamen otomatik ve `main` dalına merge edilen her şey doğrudan prod'a gidiyor. Düzenlenmiş bir kurumda doğru kurgu nedir?
## A
Hat tamamen otomatik olduğu için ek onaya gerek yoktur; otomasyonun kendisi insan hatasını ortadan kaldırır ve zaten her adım hattın kayıtlarında görünür
🤖 otomasyon görevler ayrılığının yerine geçmez; tek kişinin merge'ü hâlâ tek başına üretimi değiştirebiliyor demektir.
## B
Geliştiriciye üretim veritabanına okuma yetkisi verilmeli ki dağıtım sonrası göçün doğru uygulandığını kendisi doğrulayabilsin ve sorun çıkarsa hızlı müdahale etsin
🤖 üretim verisine geliştirici erişimi düzenlemelere aykırıdır; doğrulama günlük, iz ve sağlık kontrolleriyle yapılır.
## C
Üretim dağıtımı, değişiklik kaydına ve farklı bir kişinin onayına bağlı bir kapıdan geçmeli; hat kim-ne zaman-hangi imaj digest'i-hangi onayla bilgisini değiştirilemez bir denetim izine yazmalı; alt ortamlar maskeli veri kullanmalı
🤖 Doğru: görevler ayrılığı korunur, her dağıtım kanıtlanabilir hale gelir ve üretim verisi alt ortamlara sızmaz.
## D
En güvenli yol prod dağıtımını hattın dışına almak ve yetkili bir kişinin sunucuya bağlanıp elle yapmasıdır; elle işlem denetim için daha şeffaftır
🤖 elle işlem tekrarlanamaz ve kaydı zayıftır; denetlenebilirlik otomasyonla değil, otomasyona eklenen onay ve kayıtla artar.
## Doğru
C
## Açıklama
Düzenlenmiş bir kurumda teknik yetenek ile izinli süreç aynı şey değildir. **Görevler ayrılığı (segregation of duties)** gereği kodu yazan kişi üretime çıkışı tek başına tetikleyemez; üretim dağıtımı bir **değişiklik kaydına** (change record) ve **farklı bir kişinin** onayına bağlanır, hat da bu onayı bekleyen açık bir kapı içerir. Ayrıca her dağıtım için **denetim izi** tutulur: hangi commit, hangi imaj içerik özeti (digest), hangi değişiklik kaydı, kim onayladı, ne zaman gitti, geri alındı mı — ve bu kayıt sonradan değiştirilememelidir. "Otomasyon varsa onaya gerek yok" (A) yanılgısı, denetimin sorduğu sorunun ne olduğunu kaçırır: soru hattın çalışıp çalışmadığı değil, **tek bir kişinin tek başına üretimi değiştirip değiştiremediğidir**. Geliştiriciye üretim verisi erişimi vermek (B) bağımsız ve daha ağır bir ihlaldir; alt ortamlar **maskelenmiş test verisi** kullanır, gerçek kimlik ve hesap bilgisi kopyalanmaz, tanı ise günlük ve iz kayıtlarıyla konur. Dağıtımı hattın dışına alıp elle yapmak (D) denetlenebilirliği **azaltır**: elle işlem tekrarlanamaz, kaydı kişinin beyanına kalır ve hata olasılığı artar. Doğru yön otomasyonu bırakmak değil, otomasyonun içine onay kapısı ve değiştirilemez kayıt koymaktır.
