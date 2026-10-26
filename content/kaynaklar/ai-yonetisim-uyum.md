---
baslik: "AI Yönetişimi ve Uyum — KVKK, GDPR, EU AI Act"
yazar: "2026 mevzuat ve mühendislik pratiği — Türkçe çalışma metni"
tarih: "2026-07-15"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 8,11
---

### Bu metin ne anlatıyor?
Yapay zekâ uygulaman, kullanıcının yazdığı metnin bir kısmını başka bir şirketin sunucusuna
gönderir. O metinde kullanıcının adı, notu, ailesiyle ilgili bir cümle varsa — artık **kişisel
veri** taşıyorsun demektir ve bunun kanunda karşılığı vardır. Bu metin üç şeyi anlatıyor: hangi
verinin dışarı çıktığını nasıl görürsün, çıkmaması gerekeni koda hangi noktada durdurursun, ve
kanunların (KVKK, GDPR, AB Yapay Zekâ Yasası) senden tam olarak ne istediği. Ana fikir tek cümle:
**uyum sonradan yapıştırılan bir etiket değil, bir mimari karardır.**

## Neden bu özet
Hafta 8'de bir LLM (büyük dil modeli) uygulamasında **veri akışını** çizdik: kullanıcıdan gelen
metin, uygulamanın işlediği bağlam ve model sağlayıcıya (ör. OpenRouter) giden istem (prompt).
Hafta 11'de bu akışın **kalıcı** hâlini — veritabanları, vektör depoları, öğrenci hafızası —
konuştuk. Bu iki hafta teknik olarak "veri nereye gidiyor, nerede duruyor" sorusunu sorar.

Bu özet aynı soruyu **hukuki ve mimari** yüzünden sorar: giden veri **kişisel veri** ise ne olur?
Çünkü senin gerçek uygulaman — öğrenci gönderimlerini, sohbet geçmişini ve **hafızasını** işleyen
"AI Öğretmen" — tam da bunu yapıyor. Öğrenci Türkiye'de olduğu için **KVKK** (Kişisel Verilerin
Korunması Kanunu) doğrudan geçerli; içerik AB kaynaklıysa ya da AB'li kullanıcıya dokunuyorsa
**GDPR** (Genel Veri Koruma Tüzüğü) devreye girer; ve **2 Ağustos 2026** itibarıyla **EU AI Act**
(AB Yapay Zekâ Yasası) genel yükümlülükleri tam yürürlüğe girer.

Yönetişim (governance) "sonradan eklenen bir uyum katmanı" değildir. **Bir mimari karardır** —
tıpkı katman yönü ya da sınırlı bağlam gibi. Aşağıdaki üç diyagram bu kararın üç kritik anını
gösterir; ayrıntı metindedir.

## Türkçe çalışma metni

### 1. AI veri akışı bir mimari karardır — sağlayıcıya tam olarak ne gidiyor?
LLM uygulaması yazdığın an, farkında olsan da olmasan da bir **veri işleme sözleşmesi** kuruyorsun:
kullanıcının yazdığı metnin bir kısmı senin sunucundan çıkıp **üçüncü taraf bir model sağlayıcıya**
gidiyor. Somut soralım — AI Öğretmen'de bir öğrenci şunu gönderiyor:

> *"Ben Deniz Aydoğdu, 11. sınıf. Geçen sınavda trigonometriden 40 aldım, babam çok kızdı.
> Şu soruyu çözemedim: ..."*

Bu tek gönderimde en az dört tür veri var: **kimlik** (ad-soyad), **eğitim durumu** (sınıf, not),
**aile/duygusal bağlam** (babası kızdı), ve **asıl teknik içerik** (matematik sorusu). Sağlayıcının
model çalıştırması için yalnızca sonuncusu gereklidir; gerisi **sızıntıdır**.

Bir mimar olarak üç soruyu **kod yazmadan önce** yanıtlaman gerekir:

- **Ne gidiyor?** İstemin (prompt) içinde sağlayıcıya giden her alan. Sistem istemi, kullanıcı
  metni, RAG (getirmeyle güçlendirilmiş üretim / retrieval-augmented generation) ile eklenen
  belgeler, geçmiş sohbet — hepsi gider.
- **Ne kadar saklanıyor (retention / saklama süresi)?** Sağlayıcı isteği ne kadar tutuyor?
  OpenRouter gibi bir yönlendirici (router) ile arkasındaki asıl modelin (ör. Anthropic, OpenAI)
  saklama politikaları **ayrı ayrı** geçerlidir. "Sıfır saklama" (zero retention) modu var mı,
  varsayılan mı, yoksa açıkça mı istenmeli?
- **Nerede işleniyor (veri ikameti / residency)?** İstek hangi ülkedeki sunucuda işleniyor?
  KVKK'nın **yurt dışına aktarım** hükümleri ve GDPR'ın **sınır ötesi transfer** kuralları burada
  devreye girer. Veri AB/Türkiye dışına çıkıyorsa ek koşullar (açık rıza, yeterlilik kararı,
  standart sözleşme maddeleri) gerekebilir.

Kritik nokta: bu üç soru **koda gömülü** kararlardır. "Hangi alanları isteme koyuyorum" bir
`buildPrompt()` metodudur; "sağlayıcıya sıfır-saklama başlığı gönderiyorum" bir HTTP yapılandırma
satırıdır. Yani yönetişim, `ai/` paketinin **tasarımının** parçasıdır — sonradan yapıştırılan bir
etiket değil.

### 2. Kişisel veri (PII) sınıflandırma → maskeleme → veri minimizasyonu
Sağlayıcıya giden sızıntıyı durdurmanın mühendislik yolu üç adımdır (bkz. **Diyagram 1**):

**(a) Sınıflandırma (classification).** İsteme giren her alanı etiketle: *bu alan kişisel veri
(PII / personally identifiable information) mi?* İki alt tür önemli:
- **Doğrudan tanımlayıcılar:** ad-soyad, e-posta, telefon, TC kimlik no, öğrenci numarası.
- **Özel nitelikli (hassas) veri:** KVKK'nın **6. maddesi** sağlık, din, etnik köken, biyometrik
  gibi verileri "özel nitelikli" sayar ve **daha katı** korur. AI Öğretmen'de "babası kızdı,
  motivasyonu düşük" gibi ifadeler duygusal/psikolojik bağlam taşır; bunu hassas veri gibi ele
  almak sağlıklıdır.

**(b) Maskeleme / anonimleştirme (masking / anonymization).** Sağlayıcıya gitmesi gereken **teknik
öz** ile tanımlayıcıyı ayır. Örnek dönüşüm:

> Ham: *"Ben Deniz Aydoğdu, trigonometriden 40 aldım..."*
> Maskeli: *"Öğrenci [ÖĞRENCI_1], [DERS] konusunda düşük not aldı..."*

Burada **takma adlaştırma (pseudonymization)** ile **anonimleştirme (anonymization)** farkı kritik:
- **Takma adlaştırma**: `Deniz Aydoğdu → ÖĞRENCI_1` eşlemesini bir yerde tutarsın; geri
  çevrilebilir. GDPR açısından **hâlâ kişisel veridir** (çünkü eşleme tablosuyla kimlik geri
  kurulabilir).
- **Anonimleştirme**: geri dönüşü **imkânsız** kılar; artık kişisel veri sayılmaz. Ama gerçek
  anonimlik zordur — birleştirme (linkage) saldırılarıyla "anonim" veri sık sık yeniden
  kimliklendirilir.

**(c) Veri minimizasyonu (data minimization) / minimum veri ilkesi.** En güçlü koruma, veriyi
**hiç göndermemektir**. Soru: *bu alan modelin görevini yapması için gerçekten gerekli mi?* Öğrenci
adı bir matematik sorusunu çözmek için gereksizdir → gönderme. Bu ilke hem KVKK hem GDPR'ın
**temel taşıdır** ve mühendislikte en ucuz korumadır: göndermediğin veri sızmaz, saklanmaz,
soyağacı gerektirmez.

## Diyagram 1 — AI veri akışı ve PII sınırı: sağlayıcıya ne gidiyor?
Öğrenci verisi uygulamana girer; **PII sınırında** tanımlayıcılar maskelenir; yalnızca teknik öz
sağlayıcıya geçer. Sınırı geçen ile geçmeyen net biçimde ayrışır.

```svg
<svg viewBox="0 0 640 392" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="AI veri akışı: soldaki öğrenci verisi kutusundan uygulamaya, orada PII sınırında maskeleme, sağdaki model sağlayıcıya yalnızca maskelenmiş teknik öz gider; altta saklama ve veri ikameti notları">
  <defs>
    <marker id="gov-a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="gov-b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">AI veri akışı ve PII sınırı</text>
  <text x="320" y="44" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">Öğrenci verisi → uygulaman → [PII sınırı: maskele] → model sağlayıcı</text>

  <rect x="16" y="72" width="168" height="150" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="100" y="94" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Öğrenci verisi</text>
  <text x="100" y="108" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">(kaynak — AI Öğretmen)</text>
  <text x="28" y="130" text-anchor="start" font-size="9.5" fill="#1C3247" font-family="system-ui">• ad-soyad · öğrenci no</text>
  <text x="28" y="147" text-anchor="start" font-size="9.5" fill="#1C3247" font-family="system-ui">• sınıf · not (eğitim)</text>
  <text x="28" y="164" text-anchor="start" font-size="9.5" fill="#1C3247" font-family="system-ui">• duygusal bağlam</text>
  <text x="28" y="181" text-anchor="start" font-size="9.5" fill="#1C3247" font-family="system-ui">• sohbet hafızası</text>
  <text x="28" y="205" text-anchor="start" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">• SORU metni (teknik öz)</text>

  <rect x="236" y="72" width="176" height="150" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.6"/>
  <text x="324" y="94" text-anchor="middle" font-size="11.5" font-weight="700" fill="#4F46E5" font-family="system-ui">Uygulaman (ai/ katmanı)</text>
  <text x="324" y="112" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">1) Sınıflandır: PII mi?</text>
  <text x="324" y="130" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">2) Maskele / takma adlaştır</text>
  <text x="324" y="148" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">3) Minimize et (gereksizi at)</text>
  <rect x="252" y="162" width="144" height="46" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.3"/>
  <text x="324" y="180" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">Deniz Aydoğdu → [ÖĞRENCI_1]</text>
  <text x="324" y="197" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">eşleme tablosu SENDE kalır</text>

  <line x1="184" y1="147" x2="234" y2="147" stroke="#186399" stroke-width="2" marker-end="url(#gov-a)"/>

  <line x1="430" y1="60" x2="430" y2="312" stroke="#BE4433" stroke-width="1.8" stroke-dasharray="6 4"/>
  <text x="430" y="72" text-anchor="middle" font-size="10.5" font-weight="700" fill="#BE4433" font-family="system-ui">PII SINIRI</text>
  <line x1="412" y1="147" x2="462" y2="147" stroke="#25804C" stroke-width="2" marker-end="url(#gov-b)"/>
  <text x="447" y="138" text-anchor="middle" font-size="8.5" fill="#25804C" font-family="system-ui">yalnız öz</text>

  <rect x="464" y="72" width="160" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="544" y="94" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Model sağlayıcı</text>
  <text x="544" y="108" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">(OpenRouter → asıl model)</text>
  <text x="476" y="132" text-anchor="start" font-size="9.5" fill="#25804C" font-family="system-ui">✓ [ÖĞRENCI_1] gider</text>
  <text x="476" y="149" text-anchor="start" font-size="9.5" fill="#25804C" font-family="system-ui">✓ soru metni gider</text>
  <text x="476" y="170" text-anchor="start" font-size="9.5" fill="#BE4433" font-family="system-ui">✗ ad-soyad GİTMEZ</text>
  <text x="476" y="187" text-anchor="start" font-size="9.5" fill="#BE4433" font-family="system-ui">✗ TC / no GİTMEZ</text>
  <text x="476" y="204" text-anchor="start" font-size="9.5" fill="#BE4433" font-family="system-ui">✗ aile bağlamı GİTMEZ</text>

  <line x1="16" y1="248" x2="624" y2="248" stroke="#D6DEE5" stroke-width="1"/>
  <text x="16" y="268" text-anchor="start" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Sınırın ötesinde iki soru daha:</text>
  <rect x="16" y="278" width="298" height="52" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="165" y="298" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">SAKLAMA (retention)</text>
  <text x="165" y="315" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">Sağlayıcı isteği ne kadar tutar?</text>
  <text x="165" y="327" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sıfır-saklama modu var mı, varsayılan mı?</text>
  <rect x="326" y="278" width="298" height="52" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="475" y="298" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">VERİ İKAMETİ (residency)</text>
  <text x="475" y="315" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">Hangi ülkede işleniyor?</text>
  <text x="475" y="327" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">yurt dışı aktarım kuralı geçerli mi?</text>

  <text x="320" y="356" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">En ucuz koruma: veriyi hiç göndermemek (veri minimizasyonu). Göndermediğin veri sızmaz.</text>
  <text x="320" y="374" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Maskeleme ve sınıflandırma kararları koda gömülüdür — buildPrompt() tasarımının parçası.</text>
</svg>
```

### 3. KVKK ve GDPR — aynı ruh, iki ayrı yükümlülük
Öğrencin Türkiye'de olduğu için **KVKK** (2016, 6698 sayılı Kanun) doğrudan uygulanır; içerik ya da
kullanıcı AB'ye dokunuyorsa **GDPR** (2018) da geçerli olur. İkisi büyük ölçüde aynı ilkeleri
paylaşır — GDPR pek çok açıdan KVKK'nın da esin kaynağıdır — ama pratik ayrıntılarda ayrışırlar.
Bir mühendisin bilmesi gereken **ortak ilkeler**:

- **Açık rıza (explicit consent).** Kişisel veriyi işlemek için — özellikle bir model sağlayıcıya
  göndermek için — kullanıcının **bilgilendirilmiş ve özgür** rızası gerekir. "Verinizi yapay zekâ
  sağlayıcısına gönderiyoruz" bilgisi gizlilik politikasında değil, **kullanıcının anlayacağı**
  biçimde verilmelidir. Özel nitelikli veride rıza çıtası daha yüksektir.
- **Amaçla sınırlılık (purpose limitation).** Veriyi yalnızca topladığın amaç için kullan. Öğrenci
  sorusunu çözmek için toplanan veriyi, örneğin bir modeli **eğitmek** için kullanmak yeni bir
  amaçtır ve ayrı rıza ister. (Sağlayıcının senin verinle model eğitip eğitmediği bu yüzden kritik
  bir sözleşme sorusudur.)
- **Veri minimizasyonu (data minimization).** §2'de gördük: yalnızca gereken kadarını işle.
- **Silme hakkı / unutulma hakkı (right to erasure).** GDPR **Madde 17** kullanıcıya "verimi sil"
  deme hakkı verir; KVKK'nın **7. ve 11. maddeleri** benzer biçimde silme/yok etme hakkı tanır.
  Kullanıcı istediğinde verisini **tüm sistemlerinden** — veritabanı, yedek, **ve türetilmiş
  temsiller** — silebilmelisin. Bu son kısım (türetilmiş temsiller) RAG'de büyük bir tuzaktır; §5'e
  bakınacağız.

**KVKK'ya özgü pratik nokta — yurt dışına aktarım.** KVKK, kişisel verinin **yurt dışına
aktarılmasını** özel koşullara bağlar. Bu rejim **2024'te değişti** (7499 sayılı Kanun'la yapılan
ve 1 Haziran 2024'te yürürlüğe giren düzenleme) ve GDPR'ın kademeli yapısına yaklaştırıldı. Sıra
şudur:

1. **Yeterlilik kararı (adequacy):** Kurul, ilgili ülke/sektör için "koruma yeterli" kararı
   vermişse aktarım yapılabilir.
2. **Uygun güvenceler (appropriate safeguards):** yeterlilik kararı yoksa — **standart sözleşme**
   (Kurul'un yayımladığı hazır metin; imzalandıktan sonra Kurum'a bildirilir), **bağlayıcı şirket
   kuralları**, taahhütname veya uluslararası sözleşme.
3. **İstisnai hâller:** yukarıdakiler yoksa, yalnızca **arızi** (süreklilik taşımayan, tek
   seferlik) aktarımlarda sayılı istisnalar — **açık rıza** bunlardan biridir.

Kritik ayrım: **açık rıza artık ilk basamak değil, son çare ve "arızi" olma şartına bağlıdır.**
Her öğrenci mesajını yurt dışına gönderen bir uygulama "sürekli aktarım" yapar; bu, istisna
kapsamına girmez. OpenRouter ve arkasındaki modeller büyük olasılıkla Türkiye dışında çalıştığı
için bu hüküm seni doğrudan ilgilendirir. **Maskeleme burada da kurtarıcıdır:** sağlayıcıya giden
şey artık "kişisel veri" değilse (gerçekten anonimse), yurt dışı aktarım rejiminin dışına çıkabilir.

> **Mühendislik çıkarımı:** Rıza, amaç ve silme "hukuk ekibinin işi" değildir. Rıza bir **veri
> modeli alanıdır** (`consent_verildi_mi`, `consent_tarihi`, `consent_amaci`); silme bir
> **operasyondur** (`kullaniciyiSil(id)` gerçekten her yeri siliyor mu?); amaç sınırı bir **erişim
> kontrolüdür**. Uyum, kodun içinde yaşar.

### 4. EU AI Act — risk temelli sınıflandırma ve 2 Ağustos 2026
**EU AI Act** (AB Yapay Zekâ Yasası), yapay zekâ sistemlerini **riske göre** dört kademede
düzenleyen ilk kapsamlı yasadır (bkz. **Diyagram 2**). Mantığı basit: risk arttıkça yükümlülük
artar.

- **Yasak (unacceptable risk).** Bazı kullanımlar tümüyle yasaktır: sosyal puanlama (social
  scoring), bilinçaltı manipülasyon, belirli biyometrik gözetim türleri. Yapılamaz.
- **Yüksek risk (high risk).** Eğitim, istihdam, kredi, sağlık, hukuk gibi **insan hayatını
  etkileyen** alanlar. En ağır yükümlülük burada: risk yönetim sistemi, **veri yönetişimi**,
  dokümantasyon, insan gözetimi, kayıt tutma. **AI Öğretmen bu kademeye girme ihtimali yüksektir:
  yasa, eğitime erişimi ve öğrenci değerlendirmesini etkileyen sistemleri açıkça yüksek riskli
  sayar.** Bu, senin uygulaman için en önemli cümledir.
- **Sınırlı risk (limited risk).** Sohbet botu (chatbot) gibi sistemler için **şeffaflık**
  yükümlülüğü: kullanıcı bir yapay zekâyla konuştuğunu bilmelidir; üretilen içerik
  işaretlenmelidir.
- **Minimal risk.** Spam filtresi, oyun yapay zekâsı gibi çoğu sistem. Serbest.

**GPAI ve zaman çizelgesi.** Yasa kademeli yürürlüğe girer; kritik kilometre taşı **2 Ağustos
2026**'dır: bu tarihte **genel yükümlülükler tam yürürlüğe** girer, **genel amaçlı yapay zekâ
(GPAI / general-purpose AI)** modelleri için Komisyon'un **yaptırım yetkisi** ve **para cezaları**
işlemeye başlar. Cezalar caydırıcıdır: GPAI ihlallerinde **15 milyon euro ya da küresel yıllık
cironun %3'ü** (hangisi yüksekse). Aynı tarihte **Madde 10 (veri yönetişimi / data governance)**
yürürlüğe girer — bu madde yüksek riskli sistemlerin **eğitim, doğrulama ve test verisinin**
kalite, temsil gücü ve yanlılık (bias) açısından yönetilmesini zorunlu kılar.

> **Neden bugün önemli?** Bu metin yazılırken takvim 15 Temmuz 2026'yı gösteriyordu: genel
> yükümlülüklerin tam yürürlüğüne **üç haftadan az** kalmıştı. AI Öğretmen'i eğitim alanında
> konumlandırıyorsan, veri yönetişimini "sonra hallederiz" diye erteleyemezsin — Madde 10 tam da
> senin Hafta 8/11'de kurduğun veri hattını hedef alıyor. (Tarihleri okurken §7'nin şerhini
> unutma: mevzuat takvimi hareketli bir hedeftir.)

## Diyagram 2 — EU AI Act risk katmanları ve 2 Ağustos 2026
Risk arttıkça yükümlülük artar. Sağdaki zaman çizgisi, genel yükümlülüklerin tam yürürlük anını ve
GPAI yaptırımlarını gösterir. AI Öğretmen büyük olasılıkla "yüksek risk" (eğitim) katmanındadır.

```svg
<svg viewBox="0 0 640 404" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sol tarafta dört katmanlı risk piramidi: tepede yasak, altında yüksek risk, sonra sınırlı risk, en altta minimal risk; sağ tarafta 2 Ağustos 2026 kilometre taşı ve GPAI yaptırımları">
  <defs>
    <marker id="gov-c" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">EU AI Act: risk temelli sınıflandırma + zaman</text>
  <text x="320" y="44" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">Risk arttıkça yükümlülük artar; 2 Ağustos 2026 = genel yükümlülükler tam yürürlükte</text>

  <polygon points="194,100 206,100 239,156 161,156" fill="#FBEAE7" stroke="#BE4433" stroke-width="1.6"/>
  <text x="200" y="132" text-anchor="middle" font-size="10.5" font-weight="700" fill="#BE4433" font-family="system-ui">YASAK</text>

  <polygon points="161,156 239,156 273,212 127,212" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="200" y="182" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">YÜKSEK RİSK</text>
  <text x="200" y="197" text-anchor="middle" font-size="8" fill="#C77A17" font-family="system-ui">← AI Öğretmen (eğitim)</text>

  <polygon points="127,212 273,212 306,268 94,268" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="200" y="238" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">SINIRLI RİSK</text>
  <text x="200" y="253" text-anchor="middle" font-size="8" fill="#186399" font-family="system-ui">şeffaflık: "yapay zekâ ile konuşuyorsun"</text>

  <polygon points="94,268 306,268 340,324 60,324" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="200" y="294" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">MİNİMAL RİSK</text>
  <text x="200" y="309" text-anchor="middle" font-size="8" fill="#25804C" font-family="system-ui">çoğu sistem — serbest</text>

  <text x="200" y="348" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">Yüksek risk = risk yönetimi (Md. 9) + veri yönetişimi (Md. 10)</text>
  <text x="200" y="362" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">+ dokümantasyon (Md. 11) + insan gözetimi (Md. 14)</text>

  <line x1="392" y1="88" x2="392" y2="360" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gov-c)"/>
  <text x="392" y="80" text-anchor="middle" font-size="9.5" font-weight="700" fill="#5D7789" font-family="system-ui">zaman</text>

  <circle cx="392" cy="180" r="6" fill="#4F46E5"/>
  <rect x="410" y="150" width="214" height="66" rx="8" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.7"/>
  <text x="517" y="170" text-anchor="middle" font-size="11.5" font-weight="700" fill="#4F46E5" font-family="system-ui">2 AĞUSTOS 2026</text>
  <text x="517" y="187" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">Genel yükümlülükler TAM yürürlükte</text>
  <text x="517" y="202" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">Madde 10 (veri yönetişimi) yürürlüğe girer</text>

  <circle cx="392" cy="270" r="6" fill="#BE4433"/>
  <rect x="410" y="240" width="214" height="66" rx="8" fill="#FBEAE7" stroke="#BE4433" stroke-width="1.7"/>
  <text x="517" y="260" text-anchor="middle" font-size="11" font-weight="700" fill="#BE4433" font-family="system-ui">GPAI yaptırımları başlar</text>
  <text x="517" y="277" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">Komisyon yaptırım yetkisi + para cezası</text>
  <text x="517" y="292" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">15 M€ ya da küresel cironun %3'ü</text>

  <rect x="410" y="322" width="214" height="42" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="517" y="340" text-anchor="middle" font-size="9.5" font-weight="700" fill="#C77A17" font-family="system-ui">Bugün: 15 Temmuz 2026</text>
  <text x="517" y="355" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">Tam yürürlüğe üç haftadan az kaldı</text>

  <text x="320" y="386" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">GPAI = genel amaçlı yapay zekâ (general-purpose AI). Kademe yükseldikçe yükümlülük ağırlaşır.</text>
</svg>
```

### 5. RAG'e özgü tuzak — embedding'ler ve "bir kullanıcıyı unutmak"
Şimdi bu özetin en ince ve en çok gözden kaçan noktası. Hafta 11'de RAG için bir **vektör
veritabanı** (vector database) kurduk: belgeleri **embedding'lere (gömme vektörleri / embeddings)**
— yani her metnin anlamını temsil eden sayı dizilerine — çevirip sakladık. Sorun şu:

> **Embedding'ler klasik PII taramasının DIŞINDA kalır.**

Bir PII tarayıcısı metinde "Deniz Aydoğdu" ya da bir TC numarası arar. Ama embedding **sayılardan
oluşur** — `[0.021, -0.88, 0.13, ...]`. İçinde açık bir isim yoktur, düzenli ifade (regex)
yakalayamaz. Yine de o vektör, kaynak metnin anlamını taşır ve araştırmalar gösterdi ki
embedding'lerden orijinal metni **kısmen geri kurmak (inversion / tersine çevirme)** mümkündür.
Yani embedding, "gizlenmiş" değil, yalnızca **fark edilmesi zor** bir kişisel veri temsilidir.

Bu, **GDPR Madde 17 (silme hakkı)** ile doğrudan çarpışır (bkz. **Diyagram 3**). Bir kullanıcı
"verimi sil" dediğinde:

1. Kaynak belgeyi (öğrencinin gönderdiği metin) veritabanından silersin. ✓ Kolay.
2. **Ama o metinden türetilmiş embedding vektörü, vektör DB'de yaşamaya devam eder.** ✗

Kaynağı silip vektörü bırakmak, GDPR açısından **silme yükümlülüğünü yerine getirmemektir** —
kişisel veri türetilmiş biçimde hâlâ sistemdedir. "Bir kullanıcıyı unutmak", bir vektör
veritabanında **zor bir mimari problemdir**, çünkü:

- Vektörler genelde **yeniden indekslenmiş** (re-indexed) yapılarda gömülüdür — HNSW ve IVF,
  milyonlarca vektör arasında "buna en benzeyeni" hızlı bulmak için kurulan arama indekslerinin
  adlarıdır; tıpkı bir kitabın dizini gibi, tek bir kaydı çıkarmak indeksin yeniden kurulmasını
  gerektirebilir.
- Bir kullanıcının verisi **birden çok chunk'a** (parça) bölünmüş, her biri ayrı vektör olmuş
  olabilir; hepsini bulmak gerekir.
- Vektör başka türetmelere (özet, önbellek, ince ayar/fine-tuning veri seti) girmişse iş daha da
  karmaşıklaşır.

**Çözüm: veri soyağacı / veri kökeni (data lineage).** Her türetilmiş temsili, **kaynağına geri
bağlayan** bir izleme kur. Yani her embedding'e, hangi kaynak belgeden ve hangi kullanıcıdan
geldiğini söyleyen bir **metaveri** (metadata) iliştir: `kaynak_belge_id`, `kullanici_id`.
Böylece silme talebi geldiğinde:

> `kullaniciyiSil(id)` → lineage üzerinden **hem kaynak belgeyi HEM de ondan türeyen tüm
> vektörleri** (ve özet, önbellek gibi başka türevleri) bulup siler.

Bu, Hafta 11'in "veri sistemleri" konusunun tam kalbidir: veri **nereden geldi, nereye türedi**
sorusunu (**provenance / köken**) mühendislik olarak yanıtlayamıyorsan, GDPR/KVKK silme hakkını da
yerine getiremezsin. Lineage olmadan "unutma" imkânsızdır.

### 5.1 Ağırlıklar unutamaz — kişisel veri RAG'da kalır, ince ayar setine girmez
Vektör DB'de silme **zordur ama mümkündür**: lineage ile kaydı bulur, siler, indeksi yeniden
kurarsın. İnce ayarlanmış (fine-tuned) bir modelin **ağırlıklarında** ise bu bile yoktur. Veri bir
kez eğitimden geçip ağırlıklara karıştığında, onu oradan "cerrahi olarak" çıkarmanın pratik bir
yolu yoktur. Tek tam çözüm, o veri olmadan **baştan yeniden eğitmektir** — pahalı ve çoğu zaman
gerçekçi değil. Literatürdeki kısmi yollar (unutturma / *machine unlearning*, veriden en çok
etkilenen ağırlıkları bulup güncellemek, modele "bundan türeyen çıktıyı verme" diye ek eğitim)
**yaklaşıktır**; hiçbiri "bu öğrencinin verisi artık modelde yok" diye kanıtlanabilir bir sonuç
vermez. Yani silme hakkı (GDPR Madde 17, KVKK m.7) ağırlıkta **fiilen uygulanamaz**; uyumun tek
penceresi **toplama anıdır**.

Bu, Hafta 8'in "önce prompt, sonra RAG, en son ince ayar" merdivenine bir **veri kuralı** ekler:

> **Kişisel veri RAG'da, soy kaydıyla (lineage) tutulur; asla ince ayar setine girmez.**
> RAG'da her parça `kullanici_id` + `kaynak_belge_id` taşır, silme talebi oraya uygulanır.
> İnce ayar seti yalnız kişisel veriden **arındırılmış** (ya da gerçekten anonim) örneklerden
> kurulur ve setin kendisi de versiyonlanır — "hangi model sürümü hangi veriyle eğitildi?"
> sorusunun cevabı, AB Yapay Zekâ Yasası Madde 10'un istediği veri yönetişiminin de parçasıdır.

AI Öğretmen için somut karşılığı: öğrencinin gönderimleri ve hafızası (`data/hafiza/`, yerel
vektör deposu) silinebilir kayıtlardır; ileride "değerlendirme modelini kendi verimizle ince
ayarlayalım" fikri gelirse, ilk soru kalite değil **"bu sette kimin verisi var ve geri
alabilir miyiz?"** olmalıdır. Cevap "geri alamayız" ise o veri o sete girmez.

*Şerh:* Kaynak ders (AIEFS 18-27) aynı bağlamda AB/İngiltere veri koruma otoritelerinin 2025
kararlarını ve "en çok kullanılan eğitim kaynaklarının yaklaşık %25'i kısıtlama ekledi" gibi
rakamları aktarıyor; bunları birincil metinden doğrulamadığımız için burada **alıntılamıyoruz**.
Buradaki kural, o rakamlara değil mekanizmaya (ağırlıktan cerrahi silme yok) dayanır.

## Diyagram 3 — RAG'de silme problemi: kaynak silinir, vektör kalır
Kaynak belge silinir ama ondan türeyen embedding vektörü vektör DB'de kalır → GDPR Madde 17 ihlali.
Veri soyağacı (lineage), vektörü kaynağa bağlayarak ikisini birlikte silmeyi mümkün kılar.

```svg
<svg viewBox="0 0 640 400" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Üstte kaynak belge embedding'e dönüşüp vektör veritabanına gider; silme talebi kaynağı siler ama vektör kalır ve bu Madde 17 ihlalidir; altta veri soyağacı çözümü kaynak ile vektörü bağlayıp ikisini birlikte siler">
  <defs>
    <marker id="gov-d" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#4F46E5"/></marker>
    <marker id="gov-e" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#BE4433"/></marker>
    <marker id="gov-f" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">RAG'de silme problemi ve veri soyağacı çözümü</text>
  <text x="320" y="44" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">Kaynağı silmek yetmez: türetilmiş vektör de kişisel veridir</text>

  <text x="20" y="70" text-anchor="start" font-size="10.5" font-weight="700" fill="#BE4433" font-family="system-ui">SORUN</text>
  <rect x="20" y="80" width="150" height="60" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="95" y="104" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Kaynak belge</text>
  <text x="95" y="121" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">öğrenci gönderimi</text>
  <text x="95" y="133" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">(açık metin — PII)</text>

  <line x1="170" y1="110" x2="238" y2="110" stroke="#4F46E5" stroke-width="2" marker-end="url(#gov-d)"/>
  <text x="204" y="100" text-anchor="middle" font-size="8.5" fill="#4F46E5" font-family="system-ui">embedding</text>
  <text x="204" y="123" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">(gömme)</text>

  <rect x="240" y="80" width="160" height="60" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.6"/>
  <text x="320" y="102" text-anchor="middle" font-size="11" font-weight="700" fill="#4F46E5" font-family="system-ui">Vektör DB</text>
  <text x="320" y="119" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">[0.021, -0.88, 0.13, ...]</text>
  <text x="320" y="132" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">regex/PII tarayıcı GÖREMEZ</text>

  <rect x="440" y="80" width="184" height="60" rx="9" fill="#FBEAE7" stroke="#BE4433" stroke-width="1.6"/>
  <text x="532" y="100" text-anchor="middle" font-size="10.5" font-weight="700" fill="#BE4433" font-family="system-ui">Silme talebi (M.17)</text>
  <text x="532" y="117" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">✓ kaynak belge silinir</text>
  <text x="532" y="132" text-anchor="middle" font-size="9" fill="#BE4433" font-family="system-ui">✗ vektör KALIR</text>

  <line x1="400" y1="110" x2="438" y2="110" stroke="#BE4433" stroke-width="2" marker-end="url(#gov-e)"/>

  <path d="M320,140 L320,160" stroke="#BE4433" stroke-width="1.6" stroke-dasharray="4 3"/>
  <rect x="150" y="160" width="340" height="40" rx="8" fill="#FBEAE7" stroke="#BE4433" stroke-width="1.7"/>
  <text x="320" y="178" text-anchor="middle" font-size="11" font-weight="700" fill="#BE4433" font-family="system-ui">GDPR Madde 17 İHLALİ</text>
  <text x="320" y="193" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">Kişisel veri türetilmiş biçimde hâlâ sistemde — "unutma" tamamlanmadı</text>

  <line x1="20" y1="216" x2="620" y2="216" stroke="#D6DEE5" stroke-width="1"/>
  <text x="20" y="238" text-anchor="start" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">ÇÖZÜM — veri soyağacı (data lineage)</text>

  <rect x="20" y="250" width="150" height="66" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="95" y="272" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Kaynak belge</text>
  <text x="95" y="289" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">kullanici_id: 42</text>
  <text x="95" y="303" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">belge_id: b-7</text>

  <rect x="240" y="250" width="160" height="66" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.6"/>
  <text x="320" y="270" text-anchor="middle" font-size="11" font-weight="700" fill="#4F46E5" font-family="system-ui">Vektör + metaveri</text>
  <text x="320" y="287" text-anchor="middle" font-size="8.5" fill="#25804C" font-family="system-ui">kaynak_belge_id: b-7</text>
  <text x="320" y="301" text-anchor="middle" font-size="8.5" fill="#25804C" font-family="system-ui">kullanici_id: 42</text>

  <line x1="170" y1="283" x2="238" y2="283" stroke="#25804C" stroke-width="2" marker-end="url(#gov-f)"/>
  <text x="204" y="275" text-anchor="middle" font-size="8" fill="#25804C" font-family="system-ui">bağ (lineage)</text>

  <rect x="440" y="250" width="184" height="66" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.7"/>
  <text x="532" y="272" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">kullaniciyiSil(42)</text>
  <text x="532" y="289" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">✓ kaynak + TÜM vektörler</text>
  <text x="532" y="303" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">✓ özet · önbellek de silinir</text>

  <line x1="400" y1="283" x2="438" y2="283" stroke="#25804C" stroke-width="2" marker-end="url(#gov-f)"/>

  <text x="320" y="342" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Her türev, kaynağına ve kullanıcıya bağlanır (köken / provenance).</text>
  <text x="320" y="360" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Lineage yoksa "bir kullanıcıyı unutmak" imkânsızdır — Hafta 11'in veri sistemleri çekirdeği.</text>
  <text x="320" y="382" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Uyarı: embedding'den kaynak metin kısmen geri kurulabilir (inversion) — vektör de PII'dir.</text>
</svg>
```

### 6. AI FinOps köprüsü — yönetişim maliyeti de kapsar
Yönetişim yalnızca "veri kaçmasın" değildir; **kaynak yönetimi** de yönetişimin parçasıdır. AI
FinOps (yapay zekâ mali operasyonları), her isteğin **ne kadar veri işlediğini** ve **ne kadara mal
olduğunu** ölçer. Bu, uyumla iki noktada kesişir:

- **Ölçüm zaten uyum verisidir.** Hangi model, hangi kullanıcının, ne kadar verisini işledi — bunu
  maliyet için ölçtüğün an, EU AI Act'in **kayıt tutma (logging)** ve **izlenebilirlik** yükümlülüğü
  için de veri toplamış olursun. Aynı telemetri iki amaca hizmet eder.
- **"Başarılı çıktı başına maliyet" (cost per successful outcome).** Ham token maliyeti değil,
  **işe yarayan** yanıt başına maliyet. Bir öğrenciye gerçekten yardım eden yanıt başına ne
  ödüyorsun? Bu metrik, maskeleme ve minimizasyonun **bedavaya gelmediğini** de görünür kılar:
  daha az veri gönderdiğinde token maliyeti düşer — yani uyum ve maliyet çoğu zaman **aynı yöne**
  çeker.

Kısacası: veri minimizasyonu hem KVKK/GDPR ilkesidir, hem EU AI Act veri yönetişimini kolaylaştırır,
hem de faturayı düşürür. İyi yönetişim, üç hedefi tek kararla vurur.

### 7. ⚠️ ZORUNLU ŞERH — mevzuat metni nasıl okunur
Bu özet bir **mühendislik çalışma metnidir, hukuki görüş değildir.** Gerçek bir üründe karar
alırken bu metne değil, birincil kaynağa ve gerekiyorsa bir hukukçuya dayan. Üç ayrı uyarı:

- **Tarihler hareketli hedeftir.** EU AI Act'in takvimi, yasa yürürlükteyken bile tartışıldı:
  Avrupa Komisyonu Kasım 2025'te açıkladığı **"Dijital Omnibus"** paketiyle, özellikle **yüksek
  riskli** sistemlere ilişkin bazı yükümlülüklerin **ertelenmesini önerdi**. Bu metin yazıldığında
  teklif hâlâ yasama sürecindeydi — yani **kabul edilmiş bir kural değil, bir öneriydi.** Bu
  yüzden "2 Ağustos 2026" tarihini ezberleme; **kullanacağın gün yürürlük durumunu doğrula**
  (EUR-Lex ya da Komisyon'un kendi sayfası). Ezberlenecek olan takvim değil, **risk kademesi
  mantığıdır**: sistem insan hayatını ne kadar etkiliyorsa yükümlülük o kadar ağırdır.
- **"Yüksek risk" bir okuma, kesin bir tespit değildir.** Metinde "AI Öğretmen büyük olasılıkla
  yüksek risk kademesindedir" diyoruz; bu, yasanın eğitime erişimi ve öğrenci değerlendirmesini
  etkileyen sistemleri yüksek riskli sayan hükmüne dayanan **makul bir yorumdur**. Nihai
  sınıflandırma sistemin somut kullanımına bakar: tek kişinin kendi öğrenmesi için çalıştırdığı
  bir araçla, okulun not verirken dayandığı bir sistem aynı kefede değildir.
- **Rakamlar ve madde numaraları kaynağıyla taşınır.** "15 milyon euro ya da cironun %3'ü",
  "GDPR Madde 17", "KVKK Madde 6" gibi ifadeleri aktarırken **hangi metnin hangi maddesi**
  olduğunu birlikte söyle. Bir sayının internette çok tekrarlanması onu doğrulamaz; bu refleksin
  ölçüm tarafı [`ai-verimlilik-olcumu.md`](ai-verimlilik-olcumu.md) §6'daki "döngüsel doğrulama"
  şerhiyle aynıdır.

> **Pratik sonuç:** Mevzuattan **mimari kısıtı** al (ne gidiyor, ne kadar saklanıyor, nerede
> işleniyor, nasıl siliniyor); **tarihi** her kullanımda yeniden doğrula. Koda gömülen şey tarih
> değil **karardır**: maskeleme fonksiyonu, silme operasyonu, rıza alanı. Mevzuat değişirse bu
> üçünün *parametresi* değişir; *varlıkları* değişmez.

## Öz-sınav (kendini test et)
1. Bir öğrenci AI Öğretmen'e *"Ben Deniz, babam sınav notuma kızdı, şu integrali çözemedim"* yazıyor.
   Bu tek gönderimdeki dört veri türünü ayır. Sağlayıcıya **hangisi gitmeli, hangisi gitmemeli** ve
   maskeleme kararı kodun **neresinde** yaşar?
2. Saklama (retention) ve veri ikameti (residency) farklı iki sorudur. OpenRouter gibi bir
   **yönlendiricinin** arkasındaki asıl model açısından bu iki sorunun neden **ikişer kez** (router
   ve model için ayrı) sorulması gerekir?
3. Takma adlaştırma (pseudonymization) ile anonimleştirme (anonymization) arasındaki farkı açıkla.
   `Deniz Aydoğdu → ÖĞRENCI_1` eşlemesini **sende** tutarsan, GDPR açısından bu veri hâlâ kişisel
   veri midir? Neden?
4. Veri minimizasyonu "en ucuz koruma" olarak niteleniyor. Bir öğrencinin **adını** matematik
   sorusuyla birlikte göndermemek, hangi üç ayrı problemi (sızıntı, saklama, soyağacı) aynı anda
   ortadan kaldırır?
5. KVKK'nın yurt dışına aktarım hükmü, öğrenci verisini Türkiye dışındaki bir sağlayıcıya göndermeni
   nasıl etkiler? Maskeleme, bu hükmün **dışına çıkmana** hangi koşulda yardım eder — ve "gerçekten
   anonim" olmak neden zordur?
6. AI Öğretmen'i EU AI Act'in dört risk kademesinden hangisine yerleştirirsin ve **neden**? Bu kademe,
   Madde 10 (veri yönetişimi) açısından senin Hafta 8/11 veri hattına ne yükler?
7. **2 Ağustos 2026** tarihinde tam olarak ne yürürlüğe giriyor? GPAI için para cezasının iki
   ölçütünü (mutlak tutar ve ciro yüzdesi) söyle ve "hangisi uygulanır" kuralını açıkla.
8. Bir embedding vektörü `[0.021, -0.88, ...]` biçimindedir; içinde açık isim yoktur. Buna rağmen
   neden **kişisel veri** sayılır ve klasik bir PII/regex tarayıcısı onu neden yakalayamaz?
9. Bir kullanıcı "verimi sil" dedi. Kaynak belgeyi sildin ama vektör DB'deki embedding kaldı. Bu
   neden **GDPR Madde 17 ihlalidir**, ve **veri soyağacı (lineage)** bunu tam olarak nasıl çözer?
   Vektöre hangi metaveriyi iliştirmen gerekir?
10. Veri minimizasyonu aynı anda üç hedefi vurduğu söyleniyor: uyum (KVKK/GDPR), EU AI Act veri
    yönetişimi ve **maliyet** (AI FinOps). Bu üç hedefin neden **aynı yöne** çektiğini "başarılı
    çıktı başına maliyet" kavramıyla açıkla.
11. **Şerh sorusu:** bir arkadaşın "AI Act 2 Ağustos 2026'da yürürlüğe girdi, benim uygulamam
    yüksek riskli" diyor. Bu iki cümlenin her birinde neyi doğrulaman gerekir (ipucu: biri
    **hareketli takvim**, öteki **somut kullanıma bağlı sınıflandırma**)? Bir mevzuat sayısını
    aktarırken hangi iki bilgiyi yanında taşımak zorundasın — ve mevzuat değişince kodundaki
    hangi üç şey **yerinde kalır**, hangi kısmı değişir?
12. (§5.1) Vektör DB'de silme "zor ama mümkün", ince ayarlanmış ağırlıkta ise "fiilen imkânsız"
    deniyor. Bu farkın sebebi nedir, ve bu yüzden kişisel veri için hangi kural konur (nerede
    tutulur, nereye asla girmez)?
13. (§5.1) Ekip "değerlendirme modelini öğrenci gönderimleriyle ince ayarlayalım" diyor. Kalite
    sorusundan önce sorman gereken soru nedir ve "machine unlearning" bu soruyu neden kapatmaz?

**Cevap ipuçları (12–13):** 12 — vektör DB'de her kayıt ayrı ve adreslenebilir (lineage ile
bulunur, silinir, indeks yeniden kurulur); ağırlıkta veri milyarlarca parametreye dağılmıştır,
tek tam çözüm yeniden eğitimdir. Kural: kişisel veri RAG'da soy kaydıyla tutulur, ince ayar setine
girmez. 13 — "bu sette kimin verisi var, silme talebi gelirse geri alabilir miyiz?"; unutturma
yaklaşıktır, verinin modelden çıktığını kanıtlamaz — uyum penceresi toplama anıdır.

## Kaynaklar
Hepsi **birincil metindir**; ikincil blog özetleri yerine bunlara git (§7).

- **KVKK — 6698 sayılı Kişisel Verilerin Korunması Kanunu** (özellikle m.6 özel nitelikli veri,
  m.7 ve m.11 silme/haklar, m.9 yurt dışına aktarım — m.9, 7499 sayılı Kanun'la değişti,
  1 Haziran 2024'te yürürlüğe girdi): kvkk.gov.tr
- **GDPR — (AB) 2016/679 Genel Veri Koruma Tüzüğü** (özellikle Madde 17, unutulma hakkı):
  eur-lex.europa.eu
- **EU AI Act — (AB) 2024/1689 Yapay Zekâ Yasası** (risk kademeleri, Madde 10 veri yönetişimi,
  Madde 14 insan gözetimi, Madde 101 GPAI para cezaları, Madde 113 yürürlük takvimi): eur-lex.europa.eu
  — *şerh: takvim, Komisyon'un "Dijital Omnibus" önerisiyle tartışmaya açıldı; kullanmadan önce
  güncel durumu doğrula (§7).*
- **AI Engineering from Scratch (Rohit Ghumare, MIT lisansı, © 2026)** — ders
  `18-ethics-safety-alignment/27-data-provenance-training-governance` (§5.1'in "ağırlıklar
  unutamaz" mekanizması); github.com/rohitg00/ai-engineering-from-scratch. Çeviri değil, bu
  programa uyarlanmış sentezdir; dersteki otorite kararları ve yüzdeler doğrulanmadığı için
  aktarılmadı.
- **Avrupa Komisyonu — Dijital Omnibus paketi** (Kasım 2025 önerisi; bu metin yazıldığında
  **yasama sürecindeydi**, kabul edilmiş kural değildi): commission.europa.eu

### Bu programdaki ilgili kaynaklar
- [`ai-verimlilik-olcumu.md`](ai-verimlilik-olcumu.md) — kaynak tartma refleksi: döngüsel
  doğrulama, çıkar hizalanması, "sektör gerçeği" diye aktarmama kuralı.
- [`finops-ai-tokenomics.md`](finops-ai-tokenomics.md) — §6'nın maliyet tarafı: başarılı çıktı
  başına maliyet nasıl hesaplanır.
- [`llm-uygulama-guvenligi.md`](llm-uygulama-guvenligi.md) — aynı veri hattının **güvenlik** yüzü.
