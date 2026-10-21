---
baslik: "Temeller Tazeleme: Developer Testing (Test Piramidi + TDD)"
yazar: "Faz 0 — Mühendislik Temelleri"
tarih: "2026-07-10"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 4
---

### Bu metin ne anlatıyor?
Bu metin, yazdığın kodun doğru çalıştığını **elle denemek yerine** yine kodla nasıl kontrol
edeceğini anlatıyor. Bu kontrol programcıklarına "test" denir; bir kez yazılır, her
değişiklikten sonra saniyeler içinde tekrar çalıştırılır. Metin sırasıyla şunları görüyor:
test türleri ve aralarındaki fark, hangi türden ne kadar yazılacağı, testi önce yazma
yöntemi (TDD), gerçek bağımlılıkların yerine konan sahte nesneler ve iyi bir testin
nitelikleri.

## Neden bu özet
Test yazmak "fazladan iş" değil, **değişim güvenliğidir**: kodu yarın bozmadan
değiştirebilmenin sigortasıdır. Bu programda yapay zekâ ajanlarına kod yazdırırken de aynısı
geçerli: burada **ajan (agent)**, verilen görevi kendi başına adım adım yürüten yapay zekâ
programıdır; **harness** ise o ajanı çalıştıran, ona araç ve geri bildirim sağlayan çerçeve
düzenektir. Bu düzenekte testler ajanın **sensörüdür** (sensor) — bir değişikliğin doğru mu
yanlış mı olduğunu ajana **okunur (legible)**, yani makinece anlaşılır ve **zorlanabilir
(enforceable)**, yani geçilmeden ilerlenemez biçimde söyleyen geri-besleme kanalı.

## 1. Test türleri: birim (unit) · entegrasyon (integration) · uçtan-uca (E2E)
Üç tür test, sistemi farklı büyüklükte "dilimler" halinde sınar; kapsam büyüdükçe
gerçekçilik artar ama hız düşer, maliyet yükselir:

- **Birim testi (unit test):** Tek bir sınıfı/fonksiyonu **izole** sınar. Bağımlılıklar
  sahte nesnelerle (test double — 4. bölüm) değiştirilir; disk, ağ, veritabanı yoktur.
  Milisaniyelerle ölçülür. "Bu hesap doğru mu?" sorusunu yanıtlar.
- **Entegrasyon testi (integration test):** Birkaç bileşenin **birlikte** doğru
  konuştuğunu sınar — örneğin servis + repository + gerçek veritabanı (DB). Daha
  gerçekçi ama daha yavaş; kurulum (test veritabanı, container) gerektirir.
- **Uçtan-uca test (end-to-end / E2E):** Sistemi kullanıcı gibi baştan sona sınar
  (HTTP isteği → tüm katmanlar → yanıt). En gerçekçi ama en yavaş, en kırılgan
  (flaky) ve en pahalı olanı.

Aşağıdaki diyagram, aynı davranışın birim testinde tek sınıfla, entegrasyon testinde
ise birden çok bileşen artı veritabanıyla sarıldığını gösteriyor:

```svg
<svg viewBox="0 0 640 260" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kapsam farkı: birim testi tek sınıfı sarar, entegrasyon testi birkaç bileşen ve veritabanını sarar">
  <defs>
    <marker id="dt3-ok" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kapsam farkı: birim vs entegrasyon</text>
  <rect x="30" y="58" width="250" height="182" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6" stroke-dasharray="6 5"/>
  <text x="155" y="84" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Birim testi (unit)</text>
  <rect x="90" y="120" width="130" height="56" rx="9" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.5"/>
  <text x="155" y="145" text-anchor="middle" font-size="13" font-weight="600" fill="#1C3247" font-family="system-ui">Hesaplayıcı</text>
  <text x="155" y="163" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(tek sınıf)</text>
  <text x="155" y="214" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bağımlılıklar sahte (mock)</text>
  <rect x="330" y="58" width="280" height="182" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6" stroke-dasharray="6 5"/>
  <text x="470" y="82" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Entegrasyon testi (integration)</text>
  <rect x="360" y="100" width="96" height="44" rx="8" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.5"/>
  <text x="408" y="127" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Servis</text>
  <rect x="498" y="100" width="96" height="44" rx="8" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.5"/>
  <text x="546" y="127" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Repo (DAO)</text>
  <line x1="456" y1="122" x2="494" y2="122" stroke="#186399" stroke-width="1.8" marker-end="url(#dt3-ok)"/>
  <path d="M454,160 a46,10 0 0,0 92,0 v-40 a46,10 0 0,0 -92,0 v40" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <path d="M454,120 a46,10 0 0,0 92,0" fill="none" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="500" y="146" text-anchor="middle" font-size="12" font-weight="600" fill="#4F46E5" font-family="system-ui">Veritabanı</text>
  <line x1="546" y1="144" x2="512" y2="158" stroke="#186399" stroke-width="1.8" marker-end="url(#dt3-ok)"/>
  <text x="470" y="228" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">gerçek bileşenler · gerçek DB</text>
</svg>
```

## 2. Test piramidi: neden çok birim, az E2E?
Sağlıklı bir test takımı **piramit** biçimindedir: geniş bir birim testi tabanı, daha
az entegrasyon testi, tepede çok az E2E testi. Neden? Çünkü aşağı indikçe testler
**daha çok, daha hızlı ve daha ucuz**; yukarı çıktıkça **daha az, daha yavaş, daha
pahalı ve daha kırılgan** olur. Ters piramit (çoğu E2E) yavaş ve güvenilmez bir yapı
üretir — "dondurma külahı" (ice-cream cone) anti-deseni.

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Test piramidi: geniş taban birim testi, orta entegrasyon, ince tepe uçtan-uca; aşağı çok hızlı ucuz, yukarı az yavaş pahalı">
  <defs>
    <marker id="dt1-oku" markerWidth="9" markerHeight="9" refX="6" refY="3" orient="auto"><path d="M0,0 L6,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="dt1-oka" markerWidth="9" markerHeight="9" refX="6" refY="3" orient="auto"><path d="M6,0 L0,3 L6,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Test Piramidi</text>
  <polygon points="320,58 386.7,128.7 253.3,128.7" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.6"/>
  <polygon points="253.3,128.7 386.7,128.7 453.3,199.3 186.7,199.3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <polygon points="186.7,199.3 453.3,199.3 520,270 120,270" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="320" y="114" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">Uçtan-uca (E2E)</text>
  <text x="320" y="160" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">Entegrasyon</text>
  <text x="320" y="178" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">(integration)</text>
  <text x="320" y="236" text-anchor="middle" font-size="15" font-weight="700" fill="#186399" font-family="system-ui">Birim (unit)</text>
  <text x="320" y="255" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">en çok · en hızlı</text>
  <line x1="558" y1="90" x2="558" y2="240" stroke="#5D7789" stroke-width="1.6" marker-start="url(#dt1-oka)" marker-end="url(#dt1-oku)"/>
  <text x="574" y="82" text-anchor="start" font-size="11" font-weight="600" fill="#C77A17" font-family="system-ui">yukarı:</text>
  <text x="574" y="98" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">az · yavaş</text>
  <text x="574" y="112" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">pahalı</text>
  <text x="574" y="236" text-anchor="start" font-size="11" font-weight="600" fill="#186399" font-family="system-ui">aşağı:</text>
  <text x="574" y="252" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">çok · hızlı</text>
  <text x="574" y="266" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">ucuz</text>
</svg>
```

**Şerh:** Piramit bir başparmak kuralıdır, kutsal bir oran değil. Katmanların kesin yüzdesi
projeye göre değişir; iş mantığının çoğu veritabanı sorgularında olan bir sistemde entegrasyon
testleri hakkıyla daha kalın olur. Değişmeyen fikir şudur: **hızlı ve ucuz testler çoğunlukta,
yavaş ve kırılgan testler azınlıkta olsun.** Ayrıca hiçbir test takımı hatasızlığı garanti
etmez; testler yalnızca sınadıkları davranışlar hakkında konuşur.

## 3. TDD döngüsü: Kırmızı → Yeşil → Refactor
Test-Güdümlü Geliştirme (Test-Driven Development, TDD) testi **önce** yazmaktır. Küçük
bir üç adımlı döngü döner: önce başarısız olan bir test yaz (Kırmızı), sonra onu
geçirecek **en basit** kodu yaz (Yeşil), sonra davranışı bozmadan kodu temizle
(Refactor). Sonra bir sonraki küçük davranış için baştan başla. Bu döngü hem tasarımı
sürükler hem her adımda çalışan bir test ağı bırakır.

```svg
<svg viewBox="0 0 640 270" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="TDD döngüsü: kırmızı yazamayan test, yeşil geçir, refactor temizle, sonra tekrar">
  <defs>
    <marker id="dt2-okg" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
    <marker id="dt2-okb" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="dt2-oka" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#C77A17"/></marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">TDD Döngüsü</text>
  <circle cx="150" cy="100" r="50" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.8"/>
  <text x="150" y="90" text-anchor="middle" font-size="14" font-weight="700" fill="#C77A17" font-family="system-ui">KIRMIZI</text>
  <text x="150" y="107" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">testi yaz,</text>
  <text x="150" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">düşsün</text>
  <circle cx="490" cy="100" r="50" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="490" y="90" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">YEŞİL</text>
  <text x="490" y="107" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">en basit</text>
  <text x="490" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kodla geçir</text>
  <circle cx="320" cy="208" r="48" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <text x="320" y="200" text-anchor="middle" font-size="13.5" font-weight="700" fill="#186399" font-family="system-ui">REFACTOR</text>
  <text x="320" y="217" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">temizle</text>
  <text x="320" y="230" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(davranış aynı)</text>
  <line x1="203" y1="100" x2="435" y2="100" stroke="#25804C" stroke-width="1.9" marker-end="url(#dt2-okg)"/>
  <text x="319" y="88" text-anchor="middle" font-size="11.5" font-weight="600" fill="#25804C" font-family="system-ui">geç (make it pass)</text>
  <line x1="448" y1="127" x2="360" y2="184" stroke="#186399" stroke-width="1.9" marker-end="url(#dt2-okb)"/>
  <text x="430" y="168" text-anchor="middle" font-size="11.5" font-weight="600" fill="#186399" font-family="system-ui">iyileştir</text>
  <line x1="280" y1="184" x2="192" y2="127" stroke="#C77A17" stroke-width="1.9" marker-end="url(#dt2-oka)"/>
  <text x="205" y="168" text-anchor="middle" font-size="11.5" font-weight="600" fill="#C77A17" font-family="system-ui">yeni test</text>
</svg>
```

## 4. Test sahteleri (test double): dummy · stub · spy · mock · fake

Birim testinde gerçek veritabanına ya da gerçek e-posta sunucusuna dokunmak istemeyiz.
Onların yerine, aynı arayüzü uygulayan ama test için üretilmiş nesneler koyarız; bunların
ortak adı **test sahnesi dublörü / test sahtesi (test double)**. Ad, sinemadaki dublörden
gelir: oyuncunun yerine geçer, ama yalnızca o sahne için.

Günlük konuşmada hepsine "mock" denir; oysa beşi farklı işler yapar ve sınavda ayrımı
sorulur:

- **Dummy:** Hiç kullanılmayan, sadece parametre listesini doldurmak için verilen nesne.
  Metot imzası bir `Logger` istiyor ama test o yolu hiç çalıştırmıyorsa, içi boş bir nesne
  geçersin. Davranışı yoktur.
- **Stub (hazır cevap):** Kendisine sorulduğunda **önceden belirlenmiş bir değer döndürür**.
  "Kur servisi çağrılırsa 32,5 dönsün" gibi. Testin girdisini kurar; doğrulama için
  kullanılmaz.
- **Spy (kayıt tutan):** Stub gibi cevap verir, **ek olarak kendisine ne yapıldığını kaydeder**
  — kaç kez çağrıldı, hangi argümanlarla. Test sonunda bu kayda bakarsın. Yani doğrulama
  çağrıdan **sonra** yapılır.
- **Mock (beklenti kuran):** Kendisinden **önceden ne beklendiği tarif edilen** nesnedir:
  "`gonder()` tam bir kez ve şu adresle çağrılmalı." Beklenti karşılanmazsa testi kendisi
  düşürür. Spy'dan farkı, beklentinin çağrıdan **önce** tanımlanmasıdır.
- **Fake (basit çalışan gerçeklik):** Gerçekten çalışan, ama üretime uygun olmayan basit bir
  gerçekleme; klasik örnek, veritabanı yerine `Map` ile tutulan bellek-içi repository.
  Diğerlerinden farkı, içinde gerçek bir mantık bulunmasıdır.

Pratik ayraç: **Sonucu mu doğruluyorsun, etkileşimi mi?** Dönen değere bakıyorsan stub veya
fake yeter (durum doğrulaması — state verification). "Şu metot şu argümanla çağrıldı mı?"
diye soruyorsan spy ya da mock kullanırsın (etkileşim doğrulaması — behaviour verification).

**Şerh:** Etkileşim doğrulaması dozunda kullanılmalı. Her çağrıyı mock'la sabitleyen test,
kodun *nasıl* yazıldığına kilitlenir; davranış aynı kalsa bile iç düzenlemede test kırılır.
Böyle testler refactoring'i kolaylaştırmak yerine zorlaştırır.

## 5. İyi test nasıl olur?
Bir testin değeri, hızlı ve güvenilir bir sinyal olmasındadır. İyi testin nitelikleri
İngilizce baş harfleriyle **FIRST** diye anılır:

- **F — Hızlı (Fast):** Milisaniyeler. Yavaş takım çalıştırılmaz; çalıştırılmayan test
  değersizdir.
- **I — Bağımsız / izole (Independent, isolated):** Her test kendi verisini kurar, başka
  testin sırasına ya da kalıntısına bağlı olmaz. Sırayı değiştirince kırılmamalı.
- **R — Tekrarlanabilir (Repeatable / deterministic):** Aynı girdi hep aynı sonucu verir;
  saat, rastgele sayı, ağ gibi belirsizliklerden arındırılır — yoksa "kırılgan"
  (flaky) olur.
- **S — Kendi kendini doğrulayan (Self-validating):** Test, geçti mi kaldı mı sorusunu
  kendisi yanıtlar. Ekrana döktüğü çıktıyı insanın gözle karşılaştırması gerekiyorsa o test
  otomatik değildir.
- **T — Zamanında (Timely):** Test, ilgili kodla aynı dönemde yazılır; TDD'de hemen öncesinde.
  Aylar sonra yazılan test hem eksik kalır hem de var olan hatayı "doğru davranış" sanıp
  betonlaştırabilir.

Bu beşe pratikte bir de **okunurluk** eklenir: iyi bir test adı belge gibidir —
`bosSepetToplamiSifirDoner` gibi "ne + koşul + beklenen" kalıbı. Testin gövdesi de standart
bir düzenle okunur: **Hazırla → Uygula → Doğrula** (Arrange–Act–Assert).

## Öz-sınav (kendini test et)
1. Birim, entegrasyon ve E2E testi arasındaki farkı bir cümleyle söyle; hangisi en hızlı,
   hangisi en gerçekçi?
2. Test piramidinin tabanı neden geniş, tepesi neden ince olmalı? Ters çevrilirse
   (çoğu E2E) hangi iki sorun çıkar?
3. TDD döngüsünün üç adımını sırayla say. "Yeşil" adımında neden **en basit** kodu
   yazarız?
4. "Kırılgan test" (flaky) nedir, hangi iyi-test niteliğinin eksikliğinden doğar?
5. Testler bir ajan (agent) için neden "sensör" sayılır — hangi geri-besleme boşluğunu
   doldururlar?
6. Stub ile mock arasındaki fark nedir? Hangisini "dönen değer doğru mu", hangisini "bu metot
   çağrıldı mı" sorusu için kullanırsın?
7. Veritabanı yerine bellekte `Map` tutan basit bir repository yazdın. Bu hangi test sahtesi
   türüdür ve stub'dan farkı nedir?
