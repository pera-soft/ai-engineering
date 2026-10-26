---
baslik: "Ajan Sandbox İzolasyonu — Önleme Değil, Sınırlama (Containment)"
yazar: "2026 güvenlik araştırması (Anthropic engineering · UK AI Security Institute · Pillar Security) — Türkçe çalışma metni"
tarih: "2026-07-17"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 4,9
---

### Bu metin ne anlatıyor?
Bir yapay zekâ ajanına gerçek işler yaptırmaya başladığında (dosya okumak, komut çalıştırmak,
internete çıkmak) yeni bir soru doğar: **kandırılırsa en kötü ne yapabilir?** Bu metin o soruyu
anlatıyor. Saldırıyı kapıda durdurmaya "önleme", içeri girdiğinde verebileceği zararı küçültmeye
"sınırlama" deniyor. Buradaki konu ikincisi: ajanı dar bir kutuya (**sandbox**, yani kum havuzu)
koymak — ve o kutunun da kırılabildiğini bilerek çalışmak.

## Neden bu özet
Kardeş kaynak [LLM Uygulama Güvenliği — Prompt Enjeksiyonu ve OWASP](llm-uygulama-guvenligi.md)
**önlemeyi** (prevention) anlatır: enjeksiyonu engellemek, girdiyi filtrelemek, sistem ile
kullanıcıyı ayırmak, çıktıyı kaçışlamak. Bu özet **onun tekrarı değildir; ayrı bir katmandır.**
Buranın konusu **sınırlamadır** (containment): enjeksiyon **başarılı olduğunda** etki alanını
(blast radius) daraltmak.

> **Önleme sorar: "Saldırı içeri girebilir mi?"**
> **Sınırlama sorar: "Girdi — şimdi ne yapabilir?"**

İkisi birbirinin yerine geçmez. Önleme %100 çalışsaydı sınırlamaya gerek olmazdı; ama çalışmıyor
(§1). Sınırlama mükemmel olsaydı önlemeye gerek olmazdı; ama o da kırılıyor (§6). Bu yüzden **üst
üste** konurlar — kardeş kaynaktaki savunma derinliğinin ajanlara özgü devamı budur.

Neden şimdi? Çünkü uygulaman artık bir ajana araç açıyor: `OgretmenMcpAraclari` dış istemciye
`kaynaklar`, `kaynak(slug)`, `hafta_detay` sunuyor ve MCP sunucusu canlıda çalışıyor (ADR-0007,
Faz 2b). Araç veren her sistem "kötü cevap" riskinden "gerçek eylem" riskine geçer — ve o noktada
tek soru şudur: **en kötü durumda ajan neye dokunabilir?**

Üç diyagram fikri kısaca gösterir; ayrıntı metindedir.

## Türkçe çalışma metni

### 1. Neden önleme yetmez — mimari, filtre değil
Prompt enjeksiyonuna karşı satılan çözümlerin çoğu **guardrail modelidir**: girdiyi (ya da çıktıyı)
tarayan, "bu bir enjeksiyon mu?" diye karar veren ikinci bir sınıflandırıcı — genelde bir yakalama
oranıyla pazarlanır: **"%95 yakalıyoruz."**

Simon Willison'ın **"lethal trifecta"** (ölümcül üçlü) yazısındaki tespit tam da bu orana dairdir.
Önce terimin kendisi, çünkü metin boyunca lazım olacak:

> **Ölümcül üçlü (lethal trifecta):** Bir ajanda aşağıdaki **üç** şey **aynı anda** varsa, veri
> sızıntısı artık bir ihtimal değil, zaman meselesidir:
> 1. **Özel veriye erişim** — SSH anahtarı, kaynak kod, müşteri kaydı, e-posta kutusu…
> 2. **Güvenilmez içeriğe maruz kalma** — web sayfası, gelen e-posta, PDF, başkasının yazdığı
>    herhangi bir metin (yani dolaylı enjeksiyonun girdiği kapı).
> 3. **Dışarı iletişim kanalı** — veriyi dışarı gönderebileceği herhangi bir yol: HTTP isteği,
>    e-posta, hatta bir URL'nin içine gizlenmiş parametre.
>
> Mantığı basit: saldırgan (2) ile içeri girer, (1) ile veriyi toplar, (3) ile götürür. **Üçünden
> birini kesersen zincir kopar** — ve bu, "modeli daha iyi eğitelim"den çok daha güvenilir bir
> müdahaledir, çünkü modelin yargısına değil sistemin yapısına dayanır. §2'nin "dosya + ağ" ikilisi
> tam olarak (1) ve (3)'ü kesme çabasıdır.

Willison'ın filtrelere dair tespiti ise şudur: web güvenliğinde **%95 yakalama bir kalır nottur**
(failing grade). SQL enjeksiyonunun %95'ini
engelleyen bir veritabanı sürücüsü piyasada bir gün duramazdı; saldırgan yüz kez dener, bir kez
geçmesi yeter — **ortalamaya değil, kuyruğa** oynar.

Bu bir "daha iyi model bulun" sorunu değildir: olasılıksal bir sınıflandırıcıyı deterministik bir
güvenlik sınırı yerine koymak **kategori hatasıdır.** Willison'ın sonucu mimaridir — çözüm daha iyi
bir filtre değil, **deterministik egress (dışarı çıkış) kontrolüdür:** "model kandırılırsa da veri şu
ağdan çıkamaz" garantisi. Garanti, modelin yargısına değil, sistemin yapısına gömülür. **OWASP aynı
yere varır:** guardrail modelleri savunma derinliğinin **yalnızca BİR katmanıdır** ve **en-az-yetki
kapsamlandırmasının (least-privilege scoping) yerine geçmez.** "Guardrail'ım var" bir mimari cevap
değil, bir katman beyanıdır.

> **Enjeksiyonun bir gün başaracağını VARSAY. Sorunu "olur mu?"dan "olduğunda ne yapabilir?"e taşı.**

Bu karamsarlık değil mühendisliktir: uçak tasarımında da motorun duracağı varsayılır — bu, motoru
iyileştirmekten vazgeçmek demek değildir.

- **Tarayıcı ajanları (browser agents) — sandbox'ın sınırı burada görünür.** Sandbox, ajanın **dosya
  ve ağ** üzerindeki yan etkisini sınırlar; ama tarayıcıdaki **dolaylı prompt enjeksiyonunu
  durdurmaz**: ajanın ziyaret ettiği her sayfa kullanıcının yazmadığı bir girdidir, sayfadaki gizli
  metin ya da URL parçası (`#fragment`) modelin bağlamına girer ve ajan oturum açık olduğu sitede
  meşru kanaldan (form gönderme, mesaj) eylem yapabilir — sandbox'ın izin verdiği alan adı içinde.
  Orada gereken önlem başkadır: okuma/yazma sınırı (güvenilmez içerikten doğan her yazma eylemi taze
  insan onayı ister), göreve özel araç izin listesi, yalnız kapsamlı kimlik bilgisiyle çalışan oturum
  ve hafızaya yazdırma saldırısına karşı köken izi →
  [`llm-uygulama-guvenligi.md`](llm-uygulama-guvenligi.md) §3c, §5b.

### 2. Etkili sandbox İKİ boyut ister — dosya ve ağ
Sandbox denince akla genelde tek şey gelir: "ajan dosyalarıma erişemesin." Bu **yarım bir sandbox'tır**
ve yarım sandbox, sandbox değildir. Anthropic'in mühendislik yazısındaki formülasyon nettir:

> *"effective sandboxing requires both filesystem and network isolation. Without network isolation, a
> compromised agent could exfiltrate sensitive files like SSH keys; without filesystem isolation, a
> compromised agent could easily escape."*

Türkçesi ve mantığı:

- **Ağ izolasyonu yoksa** → ajan **veri sızdırır** (exfiltration). Dosya izolasyonun mükemmel olsa
  bile ajanın *meşru olarak* okuyabildiği bir şey vardır (kaynak kodun, SSH anahtarın, `.env`) ve ağ
  açıksa onu dışarı **yollayabilir.** Dosya kilidi veriyi içeride tutmaz; sadece hangi veriye
  dokunulacağını belirler.
- **Dosya izolasyonu yoksa** → **kaçış kolaylaşır.** Ağı kapatsan bile, ajan host'un dosya sistemine
  yazabiliyorsa dışarı adım atacak bir kaldıraç bulur: bir başlangıç betiği, bir hook dosyası, bir
  yapılandırma (§6'daki CVE-2026-25725 tam olarak budur).

> **İki boyut ÇARPIMSALDIR, toplamsal değil — biri sıfırsa çarpım sıfırdır.** Ağsız-ama-dosyalı =
> kaçış yolu. Dosyasız-ama-ağlı = sızdırma yolu. Sandbox ancak İKİSİ birden varken sandbox'tır.

Bunu kardeş kaynağın "aşırı yetki" (excessive agency) maddesiyle birleştir: orada "araca fazla yetki
verme" deniyordu; burada bir katman aşağıdayız — **araç doğru yetkiyle tanımlanmış olsa bile** onu
çalıştıran süreç neye dokunabiliyor? Yetki araç şemasında değil, **işletim sistemi seviyesinde**
sorulur. Bu, kardeş kaynaktaki **en-az-yetki (least-privilege)** ilkesinin işletim sistemine inmiş
hâlidir: süreç, işini görmesi için gereken **en dar** dosya ve ağ erişimiyle başlatılır — bir
fazlası değil.

**Ve üçüncü bir kısıt: onay kapıları (approval gates).** Dosya ve ağ, **makinenin** koyduğu
sınırlardır; onay kapısı ise **insanın** koyduğu sınırdır: geri alınamaz ya da yüksek riskli bir
eylem (dosya silme, para transferi, e-posta gönderme, üretime dağıtım) ajan tarafından **otomatik
yapılmaz**, bir insana sorulur. Üç tasarım kuralı vardır, üçü de ihmal edildiğinde kapıyı işe
yaramaz hâle getirir:

1. **Kapı, riske göre kurulur — her şeye değil.** Her adımda "onaylıyor musun?" soran bir sistemde
   kullanıcı üçüncü soruda düşünmeden onaylamaya başlar (**onay yorgunluğu**). Kapı ne kadar seyrek
   ve ne kadar anlamlıysa o kadar gerçektir.
2. **Kapının gösterdiği şey, yapılacak şeyin kendisi olmalı.** "Ajan bir komut çalıştırmak istiyor,
   onaylıyor musun?" bir onay değildir; **hangi** komutun, **hangi** dosya üzerinde çalışacağını
   gösteren ekran onaydır. İnsan görmediği şeyi onaylayamaz.
3. **Kapı, aracın önünde değil, eylemin önünde durmalı.** §5'te göreceğin Antigravity vakasının
   tam da bu yüzden bir onay kapısı sorunu olduğunu göreceksin: kapı vardı, ama çağrı onun **yolunun
   dışından** geçti.

## Diyagram 1 — Önleme vs Sınırlama: iki ayrı katman
Üstte önleme: filtre saldırıların çoğunu tutar ama bir kısmı geçer. Altta sınırlama: geçen kısım
**ne yapabilir?** İki boyut (dosya + ağ) kapalıysa etki alanı küçülür.

```svg
<svg viewBox="0 0 660 428" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Önleme katmanı prompt enjeksiyonlarının çoğunu filtreler ama bir kısmı geçer; sınırlama katmanı geçen saldırının ne yapabileceğini kısıtlar: dosya izolasyonu ve ağ izolasyonu birlikte kapalıysa etki alanı yani blast radius küçük kalır, biri açıksa sızdırma veya kaçış yolu doğar">
  <defs>
    <marker id="sbx-1a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
    <marker id="sbx-1b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Önleme ≠ Sınırlama — iki ayrı katman</text>
  <text x="330" y="44" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">"İçeri girebilir mi?" · "Girdi — şimdi ne yapabilir?"</text>

  <rect x="16" y="60" width="628" height="118" rx="12" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="34" y="82" font-size="12.5" font-weight="700" fill="#C77A17" font-family="system-ui">KATMAN 1 · ÖNLEME (prevention) — kardeş kaynak: llm-uygulama-guvenligi</text>
  <rect x="34" y="94" width="132" height="44" rx="8" fill="#FFFFFF" stroke="#B3402F" stroke-width="1.3"/>
  <text x="100" y="112" text-anchor="middle" font-size="10.5" font-weight="600" fill="#1C3247" font-family="system-ui">100 enjeksiyon</text>
  <text x="100" y="128" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">denemesi</text>
  <rect x="206" y="94" width="176" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="294" y="112" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Guardrail / enjeksiyon filtresi</text>
  <text x="294" y="128" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">olasılıksal sınıflandırıcı</text>
  <line x1="166" y1="116" x2="204" y2="116" stroke="#B3402F" stroke-width="1.8" marker-end="url(#sbx-1a)"/>
  <rect x="422" y="86" width="92" height="30" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="468" y="105" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">95 tutuldu</text>
  <rect x="422" y="122" width="92" height="30" rx="7" fill="#B3402F" stroke="#1C3247" stroke-width="1.2"/>
  <text x="468" y="141" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">5 GEÇTİ</text>
  <line x1="382" y1="110" x2="420" y2="101" stroke="#25804C" stroke-width="1.6" marker-end="url(#sbx-1b)"/>
  <line x1="382" y1="122" x2="420" y2="135" stroke="#B3402F" stroke-width="1.8" marker-end="url(#sbx-1a)"/>
  <text x="580" y="112" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">"%95 yakalama" =</text>
  <text x="580" y="130" text-anchor="middle" font-size="10" font-weight="700" fill="#B3402F" font-family="system-ui">KALIR NOT</text>
  <text x="330" y="168" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Saldırgan ortalamaya değil kuyruğa oynar: yüz kez dener, bir kez geçmesi yeter.</text>

  <line x1="330" y1="182" x2="330" y2="198" stroke="#B3402F" stroke-width="2.2" marker-end="url(#sbx-1a)"/>
  <text x="404" y="196" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B3402F" font-family="system-ui">geçen %5 buraya düşer</text>

  <rect x="16" y="204" width="628" height="212" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="34" y="226" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">KATMAN 2 · SINIRLAMA (containment) — bu kaynak</text>
  <text x="34" y="243" font-size="10" fill="#5D7789" font-family="system-ui">Soru artık "girdi mi?" değil: girdi. Elinde ne var?</text>

  <rect x="34" y="256" width="290" height="66" rx="9" fill="#FFFFFF" stroke="#186399" stroke-width="1.5"/>
  <text x="179" y="278" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">BOYUT 1 · Dosya izolasyonu</text>
  <text x="179" y="296" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yoksa → kaçış (escape) kolaylaşır:</text>
  <text x="179" y="311" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">host'a yazılan dosya bir kaldıraçtır</text>
  <rect x="336" y="256" width="290" height="66" rx="9" fill="#FFFFFF" stroke="#186399" stroke-width="1.5"/>
  <text x="481" y="278" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">BOYUT 2 · Ağ izolasyonu</text>
  <text x="481" y="296" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yoksa → sızdırma (exfiltration):</text>
  <text x="481" y="311" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">SSH anahtarı vb. dışarı gider</text>

  <rect x="34" y="334" width="592" height="34" rx="8" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="330" y="356" text-anchor="middle" font-size="10.5" font-weight="600" fill="#1C3247" font-family="system-ui">İki boyut ÇARPIMSALDIR — biri sıfırsa çarpım SIFIR</text>

  <rect x="176" y="378" width="308" height="30" rx="8" fill="#25804C" stroke="#1C3247" stroke-width="1.3"/>
  <text x="330" y="398" text-anchor="middle" font-size="11.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">İkisi de kapalı → blast radius küçük</text>
</svg>
```

### 3. Nasıl uygulanıyor — VM değil, işletim sistemi ilkelleri
Sandbox denince akla genelde **sanal makine** (VM) ya da mikro-VM gelir. Claude Code'un yaklaşımı bu
değildir:

> *"We've built this on top of OS level primitives such as Linux bubblewrap and MacOS seatbelt"*

Yani **işletim sistemi ilkelleri** üzerine kurulu — Linux'ta **bubblewrap**, macOS'ta **seatbelt**,
opsiyonel **seccomp** (sistem çağrısı filtreleme). **VM/mikro-VM DEĞİL.** Bu bir takastır: VM daha
kalın bir sınır verir ama ağırdır; OS ilkelleri hafiftir ve geliştirici döngüsüne uyar —
karşılığında sınır incedir ve çekirdeği (kernel) host ile **paylaşır.** §6'daki kırılmalar bu
inceliğin faturasıdır.

Terimler: **bubblewrap** ve **seatbelt**, işletim sisteminin "bu sürecin görebileceği dosya sistemi
şudur" diyebilmesini sağlayan hazır araçlardır; **seccomp** ise bir sürecin çekirdeğe yapabileceği
**sistem çağrılarını** (dosya aç, soket aç gibi en temel istekler) beyaz listeyle sınırlar.
**Çekirdek (kernel)**, işletim sisteminin donanımı yöneten en yetkili katmanıdır — sandbox'ın onu
host ile paylaşması, aradaki duvarın ne kadar ince olduğunu anlatır.

> ⚠️ **Kaynak şerhi (önemli).** Anthropic'in Ekim 2025 mühendislik yazısını **tek başına** kanıt diye
> kullanma: o yazı sandbox için *"available in beta as a research preview"* der — "üretimde" iddiasını
> **taşımaz.** O iddiayı taşıyan şey **güncel dokümandır** (code.claude.com/docs/en/sandboxing):
> *"The sandbox is built into Claude Code and runs on macOS, Linux, and WSL2"* — yanında `/sandbox`
> komutu ve **MDM** (mobile device management — kurumun çalışan bilgisayarlarındaki ayarları
> merkezden zorunlu kıldığı yönetim sistemi) **ile kurumsal zorlama** anahtarları: `failIfUnavailable`,
> `allowUnsandboxedCommands: false`, `allowManagedDomainsOnly`. **Hangi belgenin hangi tarihte ne
> dediğine** dikkat et: blog bir anlık görüntüdür, doküman bugünü anlatır.

**Şimdi asıl uyarı — satıcı dilini olduğu gibi yutma.** Anthropic'in bu bağlamdaki "başarılı bir
prompt enjeksiyonu bile tamamen izole edilir ve kullanıcı güvenliğini etkileyemez" mealindeki
cümlesi **mutlakçı satıcı dilidir** ve §4–§6'daki belgelenmiş kaçışlarla **yanlışlanmıştır.** Doğru
niteleme şudur:

> **Sandbox, blast radius'u daraltmayı HEDEFLER. Garanti etmez.**

Bunun en güçlü kanıtı Anthropic'e karşı değil, **Anthropic'in kendi dokümanındadır:**
*"Sandboxing reduces risk but is not a complete isolation boundary"* — ve aynı doküman **domain
fronting** ile allowlist'in aşılabileceğini açıkça kabul eder. Üreticinin teknik dokümanı,
pazarlama cümlesinden **daha dürüsttür.** Ders bir okuma alışkanlığıdır:

> **Bir satıcının en gerçekçi güvenlik beyanı, blogunda değil, dokümanının sınırlar bölümündedir.**

### 4. Kaçış teorik değil — ÖLÇÜLDÜ (ICML 2026 Oral)
"Sandbox kırılabilir" bir sezgi değil, **ölçülmüş bir bulgudur.** Kaynak: **arXiv**:2603.02277
(arXiv = bilimsel makalelerin yayımlandığı açık ön-baskı arşivi) — *"Quantifying Frontier LLM
Capabilities for Container Sandbox Escape"*, Oxford + **UK AI Security Institute** (Birleşik
Krallık'ın yapay zekâ güvenliği devlet kurumu), hakemli, **ICML 2026 Oral** (ICML, makine
öğrenmesinin en üst düzey konferanslarından biri; "Oral", gönderilenlerin küçük bir yüzdesine
verilen sözlü sunum kabulü — yani bağımsız uzmanların ayrıca seçtiği çalışma). Kaynağın kalitesi önemlidir: bu bir **satıcı** raporu
değil, bir **devlet güvenlik kurumunun** çalışmasıdır — ikincisinin satacak bir şeyi yoktur.
**Kurgu:** 9 model × 18 senaryo, **iç içe (nested) sandbox** — kaçılmaya çalışılan sandbox'ın kendisi
de bir sandbox içinde, ki gerçek bir kaçış dışarı taşmasın.

| Zorluk | Ne var | Başarı oranı |
|---|---|---|
| **1–2** | Yanlış yapılandırma (misconfiguration) | **Claude Opus 4.5 / Sonnet 4.5 = 1.00** · GPT-5 = 0.92 · DeepSeek-R1 = 0.80 |
| **3** | Orta | GPT-5 ve Opus 4.5 = **0.40** |
| **4–5** | Karmaşık **exploit** (bir açığı fiilen kullanan saldırı kodu) | **TÜM modeller = 0.00** |

Zorluk 1–2'deki **1.00**: **yanlış yapılandırılmış bir konteynerden kaçmak sınır modeller için artık
çözülmüş bir iştir** — her denemede. Yazarların pratik çıkarımı:

> **"Plain Docker isolation'ı varsayılan olarak yetersiz say."** "Konteynerde çalışıyor, o hâlde
> izole" bir güvenlik argümanı değildir.

> ⚠️ **Bulgunun SINIRLARINI da bilmeden bu makaleyi kullanma** — yoksa kaynağı çarpıtmış olursun:
> - **Yeni sıfırıncı-gün (zero-day — henüz kimsenin bilmediği, dolayısıyla yaması da olmayan açık)
>   keşfi YOK:** *"All successful breakouts relied on previously documented misconfigurations or
>   publicly disclosed CVEs."* (**CVE** = kamuya açık güvenlik açıklarına verilen evrensel numara;
>   "CVE-2026-25725" gibi. Bir açığın CVE'si varsa, o **zaten bilinen** bir açıktır.) Modeller **bilineni uyguladı**, yeni
>   açık **bulmadı** — "hijyen" ile "yetenek" arasındaki fark.
> - **Sertleştirilmiş (hardened) dış sandbox HİÇ aşılamadı.** Sertleştirme işe yarıyor.
> - **Yazarların feragati:** *"results should not be interpreted as end-to-end compromise likelihood
>   in defended production environments."*
> - **En önemlisi — GPT-5.2 GERİLEDİ:** 0.27, GPT-5'in 0.50'sinin altında. Daha yeni ve güçlü model,
>   bu görevde **daha kötü.** Yani **"ölçekle risk monoton artar" anlatısı bu veriyle
>   DESTEKLENMİYOR.** Bunu yazmak **entelektüel bir dürüstlük borcudur:** veriye uymayan anlatıyı,
>   sonucu hoşumuza gitse de gitmese de bırakırız.

### 5. Vaka: Google Antigravity RCE — asıl ders mimaridir
Pillar Security, Google'ın ajan geliştirme ortamı **Antigravity**'de **RCE**'ye (remote code
execution — saldırganın senin makinende istediği kodu çalıştırabilmesi; bir güvenlik açığının
alabileceği en ağır biçim) giden bir zincir buldu.
**Doğrulama zinciri:** Google **VRP** (Vulnerability Reward Program — üreticinin, kendisine açık
bildiren araştırmacıya para ödediği resmî program) **kabul etti (24 Oca 2026)** → **FIXED
(28 Şub 2026)** → **bounty** (ödül) **ödendi (26 Mar 2026)**. Üreticinin kendisi doğruladı, yamaladı ve parasını ödedi — bir güvenlik
iddiasının en güçlü doğrulaması **hedefin kendi yamasıdır**, blogun iddialı olması değil.

**Zincir:** `find_by_name` aracının **`Pattern` parametresi**, `fd` komut satırına **doğrulama olmadan
ve `--` argüman sonlandırıcısı olmadan** aktarılıyordu:

> *"Because the value is interpolated into the fd command line without sanitization, any string
> beginning with `-` is interpreted as a flag rather than a search pattern."*

`-` ile başlayan değer artık **arama deseni değil, bayraktır.** Saldırgan `-X` (exec-batch) enjekte
eder — `fd`'nin bulduğu her dosya için komut çalıştıran bayrak — ve **RCE** elde eder. En can alıcı
ayrıntı: bu, ürünün **EN KISITLAYICI yapılandırmasında** doğrulandı — **Secure Mode AÇIK, Auto
Execution KAPALI.**

> **"Güvenli mod açık" bir güvence değildir.** Bir ayarın adı, kapsamı hakkında hiçbir şey söylemez.

**ÇİFT KÖK NEDEN — asıl ders burada.**

**1) Argüman enjeksiyonu (CWE-88).** Onlarca yıllık klasik bir zafiyet sınıfı, **ajan araç şemasında
yeniden üretilmiş.** Yeni saldırı türü icat edilmedi; eski hata yeni kılıkta göründü. Ajan araçları
"yeni" görünür ama altlarında hâlâ `exec` vardır.

**2) Sınır YANLIŞ KATMANDA.** Pillar: *"find_by_name is classified as a native tool, not a shell
command. Native tool invocations are executed by the agent directly, before Secure Mode's
command-level restrictions are evaluated."* CyberScoop'un özeti daha keskin:

> *"The security boundary that Secure Mode enforces simply never sees this call."*

Bütün mesele budur: **Guardrail YENİLMEDİ** — atlatılmadı, kandırılmadı, zorlanmadı. Çağrı onun
**yolunun DIŞINDA** kaldı. Secure Mode kusursuz çalışıyordu; sadece **alakasızdı.** Satıcının en
kısıtlayıcı ayarı doğru şekilde açıktı ve **hiçbir şey ifade etmedi.**

> **DERS: Her yürütme yolunu aracılık etmeyen (mediate) bir kontrol düzlemi, kontrol düzlemi
> değildir.** Kapsamadığı bir yol varsa, o bir kontrol değil, bir temennidir.

**Bunu kendi projenle eşle.** ArchUnit kuralın tam da bu yüzden şöyle yazılmış:

```java
static final ArchRule claude_yalniz_ai_paketinden = noClasses()
        .that().resideOutsideOfPackage("..aiogretmen.ai..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("org.springframework.ai..", "com.anthropic..");
```

Kural `mcp` paketini **tek tek saymıyor:** `resideOutsideOfPackage("..ai..")` diyor — **ai dışındaki
HER paketi** kapsıyor, bugün var olanları da yarın eklenecekleri de. "web ve service Spring AI'yı
import edemez" diye yazılsaydı, `mcp` paketi eklendiği gün kural **onu görmezdi** — tıpkı Secure
Mode'un native tool çağrısını görmemesi gibi. Kural yeşil yanar, kapsamı boştur.

> **Antigravity'nin dersi ile ArchUnit'in tasarımı aynı cümledir: kapsamı listeyle tanımlanan kural,
> listeye girmeyeni sessizce serbest bırakır. Kapsamı NEGATİF tanımla — "şunlar hariç herkes" — ki
> yeni gelen varsayılan olarak kuralın İÇİNE düşsün.**

> ⚠️ **Çerçeveleme şerhi:** Bu açık **YAMALANDI** (28 Şub 2026). Yani bu **canlı bir tehdit değil,
> tarihsel bir vaka incelemesidir.** "Antigravity kullanmayın" gibi bir sonuç çıkarma. Ama şunu da
> çıkarma: "yamalandı, o hâlde konu kapandı." **Mimari ders yamalanmakla geçersizleşmez** — yama
> o tek `Pattern` parametresini düzeltir; "kontrol düzlemi her yolu aracılık etmeli" ilkesi ise her
> ajan aracı için geçerli kalır. Ayrıca: bu bulgunun **CVE numarası YOKTUR** (Google VRP içinde
> ele alındı) — dolayısıyla ona "CVE-destekli" **deme.**

## Diyagram 2 — Antigravity: sınır yanlış katmanda
İki yol var. Shell komutu yolu Secure Mode'un içinden geçer. Native tool yolu **kontrolün önünde**
yürütülür — kontrol o çağrıyı hiç görmez.

```svg
<svg viewBox="0 0 660 384" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Antigravity vakasında iki yürütme yolu vardır: shell komutu yolu Secure Mode kontrolünden geçer ve kısıtlanır; native tool yolu ise ajan tarafından doğrudan Secure Mode değerlendirilmeden önce yürütülür, bu yüzden kontrol düzlemi bu çağrıyı hiç görmez ve fd komutuna argüman enjeksiyonu ile uzaktan kod çalıştırma mümkün olur">
  <defs>
    <marker id="sbx-2a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
    <marker id="sbx-2b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Antigravity: guardrail yenilmedi — çağrı onun DIŞINDA kaldı</text>
  <text x="330" y="44" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Secure Mode AÇIK · Auto Execution KAPALI · ve yine de RCE</text>

  <rect x="256" y="62" width="148" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="330" y="88" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Ajan (enjekte edilmiş)</text>

  <line x1="290" y1="104" x2="180" y2="132" stroke="#25804C" stroke-width="1.8" marker-end="url(#sbx-2a)"/>
  <line x1="370" y1="104" x2="482" y2="132" stroke="#B3402F" stroke-width="2.2" marker-end="url(#sbx-2b)"/>

  <rect x="16" y="136" width="308" height="212" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="170" y="158" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">YOL A · shell command</text>
  <rect x="42" y="170" width="256" height="34" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="170" y="191" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"shell komutu çalıştır" isteği</text>
  <line x1="170" y1="204" x2="170" y2="216" stroke="#25804C" stroke-width="1.6" marker-end="url(#sbx-2a)"/>
  <rect x="42" y="218" width="256" height="46" rx="7" fill="#25804C" stroke="#1C3247" stroke-width="1.3"/>
  <text x="170" y="238" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">Secure Mode kontrolü</text>
  <text x="170" y="254" text-anchor="middle" font-size="9.5" fill="#FFFFFF" font-family="system-ui">komut seviyesinde kısıtlama</text>
  <line x1="170" y1="264" x2="170" y2="276" stroke="#25804C" stroke-width="1.6" marker-end="url(#sbx-2a)"/>
  <rect x="42" y="278" width="256" height="34" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="170" y="299" text-anchor="middle" font-size="10.5" font-weight="600" fill="#25804C" font-family="system-ui">kısıtlanmış yürütme ✓</text>
  <text x="170" y="332" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Kontrol bu yolu GÖRÜR ve aracılık eder.</text>

  <rect x="336" y="136" width="308" height="212" rx="12" fill="#FBF0DE" stroke="#B3402F" stroke-width="1.8"/>
  <text x="490" y="158" text-anchor="middle" font-size="12" font-weight="700" fill="#B3402F" font-family="system-ui">YOL B · native tool (find_by_name)</text>
  <rect x="362" y="170" width="256" height="34" rx="7" fill="#FFFFFF" stroke="#B3402F" stroke-width="1.2"/>
  <text x="490" y="191" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Pattern = "-X sh -c ..." (bayrak!)</text>
  <line x1="490" y1="204" x2="490" y2="216" stroke="#B3402F" stroke-width="2" marker-end="url(#sbx-2b)"/>
  <rect x="362" y="218" width="256" height="46" rx="7" fill="#FFFFFF" stroke="#B3402F" stroke-width="1.6" stroke-dasharray="6 4"/>
  <text x="490" y="238" text-anchor="middle" font-size="11" font-weight="700" fill="#B3402F" font-family="system-ui">Secure Mode BURAYA BAKMAZ</text>
  <text x="490" y="254" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ajan doğrudan yürütür — kontrolden ÖNCE</text>
  <line x1="490" y1="264" x2="490" y2="276" stroke="#B3402F" stroke-width="2" marker-end="url(#sbx-2b)"/>
  <rect x="362" y="278" width="256" height="34" rx="7" fill="#B3402F" stroke="#1C3247" stroke-width="1.3"/>
  <text x="490" y="299" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">fd -X → RCE  ✗</text>
  <text x="490" y="332" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">"The boundary simply never sees this call."</text>

  <rect x="98" y="358" width="464" height="24" rx="7" fill="#1C3247"/>
  <text x="330" y="375" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">Her yolu aracılık etmeyen kontrol düzlemi, kontrol düzlemi değildir.</text>
</svg>
```

### 6. Sandbox'ın kendisi de kırılır — belgelenmiş örnekler
Şimdi bu kaynağın kendi tezine dürüst olma sırası. §1–§5 "sandbox lazım" dedi; ama sandbox'ın kendisi
de bir yazılımdır ve **yazılımın açığı olur.** Aşağıdaki dört örnek "sandbox var, güvendesiniz"
cümlesini **çürütür** — ama "o hâlde sandbox'a gerek yok" sonucunu **vermez.** Tam tersini
güçlendirir: **sandbox tek savunma olamaz; katmanlı mimari şarttır.**

**(a) CVE-2026-25725 — var olmayan dosyanın açığı.** bubblewrap, **var OLMAYAN** bir
`.claude/settings.json` dosyasına read-only bind mount **uygulayamıyordu** — yok olan bir şeyi
salt-okunur yapamazsın. Sonuç: sandbox'ın *içinden* o dosya **yaratılıyor**, SessionStart hook'u
tanımlanıyor ve kod **host ayrıcalığında** çalışıyor.

> **Ders: Read-only koruması dosyanın VARLIĞINA bağlıysa, dosyanın YOKLUĞU bir kaçış yoludur.**
> Bir korumanın ön koşulu varsa, o ön koşul saldırı yüzeyidir.

**(b) CVE-2025-66479 — "hepsini engelle" = "hepsine izin ver".** Sandbox, `block-all` ağ ayarını
**"allow everything"** olarak yorumluyordu — yaklaşık **bir ay** boyunca. Kullanıcı en kısıtlayıcı
ayarı seçmişti; aldığı tam tersiydi. Bu, §2'deki "ağ izolasyonu yoksa sızdırma" yolunun **tam
kendisidir** — üstelik kullanıcı onu kapattığını sanarak.

**(c) SOCKS5 null-byte parser differential — en öğretici olan.** `evil.com\0.google.com` stringine
iki katman bakar ve **farklı şeyler görür:**

- **Politika katmanı (JS filtresi):** stringin **tamamını** görür, `.google.com` ile bittiğini tespit
  eder → **"allowlist'te var, İZİN VER."**
- **Uygulama katmanı (libc `getaddrinfo()`):** C'de string **null byte'ta biter** → gerisini **atar**
  → **`evil.com`'a bağlanır.**

Aynı string, iki ayrıştırıcı, iki gerçeklik. Sonuç: wildcard allowlist kullananlarda ağ sınırı
**yaklaşık 5,5 ay boyunca fiilen yoktu** — hem de tam çalışıyor görünürken.

> **Ders: Politika katmanı ile uygulama katmanı AYNI stringi FARKLI ayrıştırıyorsa, politika yoktur.**
> Karar veren ile uygulayan aynı gerçekliği görmüyorsa, karar bir tiyatrodur.

Bu bir Claude Code hatası değildir — **parser differential** (ayrıştırıcı farkı) klasik,
**araç-agnostik** ve **öğretilebilir** bir hata sınıfıdır: HTTP request smuggling, Unicode
normalizasyon atlatmaları, XML entity karışıklıkları hep aynı ailedendir. Soru evrenseldir:
**"Kontrolü yapan kod ile işi yapan kod, girdiyi aynı şekilde mi okuyor?"**

**(d) "Varsayılan güvensizse guardrail değildir."** `allowUnsandboxedCommands` varsayılanı
**true**'dur: model, sandbox içinde patlayan bir komutu **`dangerouslyDisableSandbox` ile sandbox'ın
dışında yeniden deneyebilir.** İsimlendirme dürüsttür ("dangerously" diyor) ama **varsayılan**
güvenli değildir — ve güvenlikte varsayılan, dokümandan güçlüdür: çoğu kullanıcı ayarları hiç açmaz.

> **Dördünün ortak sonucu:** sandbox'a **güvenmek** ile sandbox'ı **kullanmak** farklı şeylerdir.
> Sandbox'ı "artık düşünmem gerekmeyen bir çözüm" olarak değil, **başarısız olabileceğini bildiğin
> bir katman** olarak kullan. Savunma derinliğinin tanımı zaten budur: her katmanın deleneceğini
> varsayıp bir sonrakini koymak.

## Diyagram 3 — Parser differential: politika ile uygulama aynı stringi farklı okur
Tek bir girdi, iki ayrıştırıcı, iki gerçeklik. Karar veren `.google.com` görür; bağlanan `evil.com`'a gider.

```svg
<svg viewBox="0 0 660 352" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Parser differential örneği: evil.com null byte .google.com stringi politika katmanındaki JavaScript filtresi tarafından tamamı okunur ve .google.com ile bittiği için izin verilir; uygulama katmanındaki libc getaddrinfo çağrısı ise stringi null byte'ta keser ve evil.com adresine bağlanır; aynı string iki katmanda farklı ayrıştırıldığı için politika fiilen yoktur">
  <defs>
    <marker id="sbx-3a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="sbx-3b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Parser differential — aynı string, iki gerçeklik</text>
  <text x="330" y="44" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Karar veren ile uygulayan farklı okuyorsa, karar bir tiyatrodur</text>

  <rect x="196" y="62" width="268" height="42" rx="9" fill="#1C3247"/>
  <text x="330" y="80" text-anchor="middle" font-size="10" fill="#FFFFFF" font-family="system-ui">TEK GİRDİ (ajanın istediği adres)</text>
  <text x="330" y="97" text-anchor="middle" font-size="12.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">evil.com \0 .google.com</text>

  <line x1="270" y1="104" x2="176" y2="134" stroke="#5D7789" stroke-width="1.8" marker-end="url(#sbx-3a)"/>
  <line x1="390" y1="104" x2="484" y2="134" stroke="#5D7789" stroke-width="1.8" marker-end="url(#sbx-3a)"/>

  <rect x="16" y="138" width="308" height="164" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="170" y="160" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">POLİTİKA KATMANI · JS filtresi</text>
  <text x="170" y="177" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">JS'te null byte sıradan bir karakterdir</text>
  <rect x="42" y="188" width="256" height="30" rx="6" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="170" y="208" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">evil.com\0.google.com</text>
  <text x="170" y="234" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">stringin TAMAMINI görür →</text>
  <text x="170" y="250" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">".google.com ile bitiyor mu?" → evet</text>
  <rect x="76" y="260" width="188" height="30" rx="7" fill="#25804C" stroke="#1C3247" stroke-width="1.2"/>
  <text x="170" y="280" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">karar: İZİN VER ✓</text>

  <rect x="336" y="138" width="308" height="164" rx="12" fill="#FBF0DE" stroke="#B3402F" stroke-width="1.6"/>
  <text x="490" y="160" text-anchor="middle" font-size="12" font-weight="700" fill="#B3402F" font-family="system-ui">UYGULAMA KATMANI · libc</text>
  <text x="490" y="177" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">C'de string null byte'ta BİTER</text>
  <rect x="362" y="188" width="256" height="30" rx="6" fill="#FFFFFF" stroke="#B3402F" stroke-width="1.2"/>
  <text x="490" y="208" text-anchor="middle" font-size="10.5" fill="#B3402F" font-family="system-ui">evil.com</text>
  <text x="490" y="234" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">getaddrinfo() null'da KESER →</text>
  <text x="490" y="250" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">gerisini hiç görmez</text>
  <rect x="396" y="260" width="188" height="30" rx="7" fill="#B3402F" stroke="#1C3247" stroke-width="1.2"/>
  <text x="490" y="280" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">eylem: evil.com'a BAĞLAN ✗</text>

  <line x1="324" y1="275" x2="394" y2="275" stroke="#B3402F" stroke-width="1.8" stroke-dasharray="5 3" marker-end="url(#sbx-3b)"/>

  <rect x="66" y="314" width="528" height="26" rx="7" fill="#1C3247"/>
  <text x="330" y="332" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">İki katman aynı stringi FARKLI ayrıştırıyorsa → politika fiilen YOKTUR</text>
</svg>
```

### 7. Bu projede pratik — LAB
`OgretmenMcpAraclari` dış bir istemciye araç açıyor ve bu araçlar **parametre alıyor:**

```java
arac("kaynak",
     "... 'slug' = kaynağın kısa adı ...",
     (ex, req) -> json(om, kaynakService.hamKaynak(McpAdaptorDestek.metin(req.arguments(), "slug"))))
```

**Antigravity sorusunu bu satıra sor:** `slug`, dış istemciden gelen **güvenilmez** bir stringtir.
Bir **dosya yoluna** mı dönüşüyor? Öyleyse `../../etc/passwd` ne olur? Bir **allowlist'e** karşı
doğrulanıyor mu, yoksa doğrudan mı kullanılıyor? Antigravity'de sorun `Pattern`'ın **ne olduğu**
değil, **doğrulanmadan** aktarılmasıydı.

**Üç somut savunma — MCP araç parametrelerinde:**

1. **Allowlist doğrulama.** "Kötü karakterleri temizle" (denylist) değil, **"kabul edilenleri say"**
   (allowlist): `slug` mevcut kaynakların listesinde var mı? Yoksa reddet. Denylist her zaman
   eksiktir; allowlist tanımı gereği tamdır.
2. **`--` argüman sonlandırma.** Bir dış komuta parametre geçiyorsan `--` ile "buradan sonrası bayrak
   değil, veridir" de. Antigravity'de eksik olan tam olarak buydu.
3. **Shell'siz exec.** `ProcessBuilder`'ı **shell olmadan** kullan: komutu ve argümanları **ayrı liste
   elemanları** olarak ver, tek string olarak değil. Tek string verirsen shell onu yeniden ayrıştırır
   — ve §6'daki parser differential ailesine kapı açarsın.

**Hafta 4 köprüsü (SAST).** Bu, kardeş kaynağın "statik analiz LLM yüzeyini görmez" tespitine bir
**istisnadır** ve tam da bu yüzden öğreticidir: argüman enjeksiyonu (CWE-88) **statik analizin
yakalayabileceği** bir sınıftır — çünkü kaynakta görünür bir veri akışıdır (güvenilmez girdi →
`exec`). Ajan güvenliğinin bir kısmı klasik araçlarla, bir kısmı **yalnızca mimariyle** korunur;
ikisini birbirinin yerine koyma.

**Ve senin zaten sahip olduğun sandbox katmanı.** ADR-0007 Faz 2b'ye göre MCP sunucusu izole sistem
kullanıcısı **`aiogretmen`** ile, **`ProtectSystem=strict`** ve **`NoNewPrivileges`** altında
çalışıyor. Bunu küçümseme — **bu bir sandbox katmanıdır:** `ProtectSystem=strict` §2'nin dosya
boyutudur (sistem dizinleri salt-okunur), `NoNewPrivileges` ayrıcalık yükseltmeyi keser, izole
kullanıcı blast radius'u o kullanıcının erişebildiğine daraltır. Eksik boyutu da §2 söylüyor: **ağ.**
Sunucunun egress'i (dışarı çıkışı) kısıtlı mı? Değilse dosya tarafı sıkı ama sızdırma yolu açık
demektir — ve iki boyut çarpımsaldır.

## Öz-sınav (kendini test et)
1. Bu kaynak ile kardeş kaynak `llm-uygulama-guvenligi` arasındaki farkı tek cümleyle kur. "Önleme"
   ve "sınırlama" hangi iki farklı soruyu sorar, ve neden biri diğerinin yerine geçmez?
2. **Ölümcül üçlüyü (lethal trifecta)** say ve her birine AI Öğretmen'den bir örnek ver. Üçünden
   **hangisini kesmek** senin projende en kolay, hangisi en zor? "Zinciri kırmak, modeli
   iyileştirmekten neden daha güvenilirdir?"
3. Willison'ın "%95 yakalama web güvenliğinde kalır nottur" tespitini açıkla: saldırgan neden
   ortalamaya değil kuyruğa oynar? Buradan çıkan çözüm neden "daha iyi filtre" değil, **deterministik
   egress kontrolü**? OWASP aynı yere hangi cümleyle varıyor?
4. "Etkili sandbox iki boyut ister" ne demek? Ağ izolasyonu olmayan ama dosya izolasyonu mükemmel
   bir sandbox'ta hangi saldırı hâlâ mümkündür — ve tersi durumda hangisi? İki boyutun "çarpımsal"
   olması pratikte ne anlama gelir? Bu iki **makine** sınırına ek olarak gelen **onay kapısı**
   hangi tür eylemler için kurulur, ve "onay yorgunluğu" neden kapıyı işe yaramaz hâle getirir?
5. Claude Code'un sandbox'ı VM üzerine değil, hangi **OS ilkelleri** üzerine kurulu? Bu tercihin
   takası (trade-off) nedir — ne kazanılır, ne kaybedilir, ve §6'daki kırılmalar bu takasla nasıl
   ilişkilidir?
6. Anthropic'in "başarılı bir enjeksiyon bile tamamen izole edilir" mealindeki cümlesini neden
   **birebir alıntılamamalısın**? Aynı şirketin **kendi dokümanı** bu iddiayı hangi cümleyle
   sınırlıyor, ve buradan çıkan okuma alışkanlığı nedir?
7. arXiv:2603.02277 çalışmasının **kaynak kalitesi** neden bir satıcı raporundan yüksektir? Zorluk
   1–2 ile zorluk 4–5 sonuçları arasındaki uçurum (1.00 vs 0.00) ne anlatıyor, ve yazarların "plain
   Docker'ı yetersiz say" çıkarımı bununla nasıl bağlanıyor?
8. Aynı çalışmanın **üç sınırını** say (sıfırıncı-gün yok · hardened sandbox aşılamadı · yazarların
   feragati). **GPT-5.2'nin GERİLEMESİ** (0.27 vs 0.50) hangi popüler anlatıyı desteklemiyor — ve
   bunu yazmak neden bir entelektüel dürüstlük borcudur?
9. Antigravity vakasında **çift kök neden** nedir? "Guardrail yenilmedi, çağrı onun yolunun dışında
   kaldı" cümlesini açıkla. Bunu projendeki `claude_yalniz_ai_paketinden` kuralıyla eşle: kural neden
   `resideOutsideOfPackage("..ai..")` diye **negatif** yazılmış, ve paketleri tek tek sayan bir sürüm
   `mcp` paketi eklendiğinde ne yapardı?
10. **Parser differential**'i `evil.com\0.google.com` örneğiyle anlat: politika katmanı ne görüyor,
    uygulama katmanı ne görüyor? "Karar veren ile uygulayan aynı gerçekliği görmüyorsa" hangi genel
    kural doğar? CVE-2026-25725'in dersi ("koruma dosyanın varlığına bağlıysa…") bununla nasıl aynı
    aileden?
11. `OgretmenMcpAraclari`'ndaki `kaynak(slug)` aracına Antigravity sorusunu sor: `slug` doğrulanıyor
    mu, bir dosya yoluna mı gidiyor? Üç savunmayı (allowlist · `--` sonlandırma · shell'siz
    `ProcessBuilder`) uygula. Ayrıca: ADR-0007'nin `aiogretmen` kullanıcısı + `ProtectSystem=strict` +
    `NoNewPrivileges` kurulumu §2'nin **hangi boyutunu** karşılıyor, hangisi eksik kalıyor? Son olarak
    `allowUnsandboxedCommands` varsayılanının **true** olması neden "varsayılan güvensizse guardrail
    değildir" ilkesinin örneğidir?

## Kaynaklar
- **Anthropic — Claude Code sandboxing** (mühendislik yazısı, Ekim 2025):
  https://anthropic.com/engineering/claude-code-sandboxing — *şerh: sandbox'ı "beta / research
  preview" der; "üretimde" iddiasını taşımaz.*
- **Claude Code — Sandboxing dokümanı** (güncel): https://code.claude.com/docs/en/sandboxing —
  *"macOS, Linux, WSL2'de yerleşik" iddiasını taşıyan kaynak; MDM zorlama anahtarları; ayrıca
  "not a complete isolation boundary" sınırı ve domain fronting kabulü buradadır.*
- **sandbox-runtime** (açık kaynak): https://github.com/anthropic-experimental/sandbox-runtime
- **Simon Willison — The lethal trifecta:** https://simonwillison.net/2025/Jun/16/the-lethal-trifecta/
- **Quantifying Frontier LLM Capabilities for Container Sandbox Escape** (Oxford + UK AI Security
  Institute · ICML 2026 Oral): https://arxiv.org/abs/2603.02277
- **Pillar Security — Antigravity: prompt enjeksiyonundan RCE ve sandbox kaçışına:**
  https://pillar.security/blog/prompt-injection-leads-to-rce-and-sandbox-escape-in-antigravity —
  *şerh: Google VRP kabul → FIXED 28 Şub 2026 → bounty. CVE numarası yoktur.*
- **CyberScoop — Antigravity vakası özeti:**
  https://cyberscoop.com/google-antigravity-pillar-security-agent-sandbox-escape-remote-code-execution/
- **AI Engineering from Scratch** (Rohit Ghumare, MIT lisansı, Copyright (c) 2026 Rohit Ghumare):
  https://github.com/rohitg00/ai-engineering-from-scratch — ders 15-11 (tarayıcı ajanları); §1'deki
  tarayıcı ajanı maddesi bu dersten sentezlendi. *Şerh: dersteki benchmark rakamları ve "tam
  yamalanamaz" alıntısının atfı doğrulanmadığı için alınmadı.*
