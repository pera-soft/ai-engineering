---
slug: test-stratejisi
baslik: "Test Stratejisi: Piramit, Test Double Türleri, Contract Test ve Flaky Test"
kademe: Orta
sira: 187
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

**Test** demek, yazdığın kodun beklediğin işi yapıp yapmadığını senin yerine kontrol eden **ikinci bir kod parçası** yazmaktır. Bir hesap makinesi yazdıysan, `topla(2, 3)` çağırıp sonucun 5 olup olmadığına bakan küçücük program bir testtir. Elle tıklayıp gözle bakmanın yerine geçer: bilgisayar bu kontrolü saniyede binlerce kez, yorulmadan ve her seferinde aynı titizlikle yapar.

Neden şart? Çünkü program büyüdükçe bir yeri düzeltirken farkında olmadan başka bir yeri bozarsın — buna **regresyon** (eskiden çalışan şeyin bozulması) denir. Testler, kodu her değiştirdiğinde eski davranışın hâlâ doğru olduğunu anında haber veren alarm sistemidir. **Test stratejisi** ise "neyi, hangi seviyede, ne kadar test edelim?" sorusunun cevabıdır; çünkü her şeyi en pahalı yoldan test etmek de bir hatadır.

### Temel kavramlar

- **Birim testi** (unit test): tek bir sınıfı/fonksiyonu tek başına sınar. Milisaniye sürer, çok sayıda yazılır.
- **Entegrasyon testi** (integration test): birkaç parçayı birlikte sınar — servis + gerçek veritabanı gibi. Saniyeler sürer.
- **E2E testi** (uçtan uca, end-to-end): sistemin tamamını kullanıcı gözünden sınar. En gerçekçi, ama en yavaş, en pahalı, en kırılgan.
- **Test piramidi** (test pyramid): bu üç seviyenin sağlıklı dağılımını anlatan metafor — tabanda çok birim, ortada daha az entegrasyon, tepede çok az E2E. Ters çevrilirse ("test dondurması" anti-deseni) boru hattı yavaşlar ve durmadan sebepsiz kırmızı yanar.
- **Sahte nesne** (test double): gerçek bir bağımlılığın yerine testte geçirilen taklit nesne — sinemada oyuncunun yerine geçen dublör gibi. Beş türü vardır ve **aralarındaki ayrım mülakatta doğrudan sorulur**:
  - **dummy**: hiç kullanılmaz, yalnız parametre boşluğunu doldurur.
  - **stub**: sorulduğunda önceden belirlenmiş sabit cevabı döner.
  - **spy**: gerçek işi yapar ama kendisine gelen çağrıları kaydeder.
  - **mock**: üzerine önceden **beklenti** kurulur ("`stokDus` tam bir kez çağrılmalı") ve bu beklenti test sonunda **doğrulanır**.
  - **fake**: çalışan ama basitleştirilmiş gerçek bir gerçekleme (ör. bellek içi repository).
  Kısa ayrım: stub/fake **durumu** (sonuç ne oldu), spy/mock **etkileşimi** (kim kaç kez çağrıldı) ilgilendirir.
- **Flaky test** (kararsız test): kod hiç değişmediği hâlde bazen geçen, bazen kalan test. Güveni öldürdüğü için en tehlikeli teknik borçtur.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Test piramidi: birim, entegrasyon, E2E">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Test Piramidi — tabana yatır</text>
  <polygon points="320,44 240,104 400,104" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.5"/>
  <text x="320" y="90" text-anchor="middle" font-size="12" font-weight="700" fill="#B5504F" font-family="system-ui">E2E (az, yavaş)</text>
  <polygon points="240,108 400,108 440,168 200,168" fill="#FBF0DE" stroke="#B07D28" stroke-width="1.5"/>
  <text x="320" y="146" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D28" font-family="system-ui">Entegrasyon (orta)</text>
  <polygon points="200,172 440,172 480,204 160,204" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="194" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Birim (çok, hızlı)</text>
  <text x="560" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">↑ pahalı</text>
  <text x="560" y="188" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">↓ ucuz</text>
</svg>
```

### Nasıl çalışır?

Her testi **doğrulama değeri / bakım maliyeti** oranına göre yerleştir: E2E'yi kritik kullanıcı yolculuklarıyla sınırla, iş mantığını aşağı seviyelere it. Testleri paralel koşmak toplam süreyi kısaltır ama yanlış seviyede test etmenin kök nedenini çözmez.

**TDD** (Test-Driven Development, testle güdümlü geliştirme) üç adımlı bir döngüdür ve sırası bağlayıcıdır. **Kırmızı:** henüz yazılmamış davranış için önce **başarısız olan** testi yaz — bu adım hem davranışı koddan önce tanımlar hem testin gerçekten bir şey ölçtüğünü kanıtlar. **Yeşil:** testi geçiren en yalın kodu yaz. **Refactor:** testler yeşilken tasarımı temizle. Kod önce yazılıp test sonra eklenirse buna TDD değil "test-after" denir. Akrabası **BDD**, davranışı `Given/When/Then` diliyle iş tarafıyla ortak yazar.

Servisler arası uyum için **sözleşme testi (contract test)** vardır. Çağıran tarafa **tüketici** (consumer), çağrılan tarafa **sağlayıcı** (provider) denir.

Tüketici, beklediği istek ve yanıt biçimini bir **sözleşmeye** yazar; örneğin Pact aracıyla. Sağlayıcı ise kendi boru hattında bu sözleşmeye karşı **bağımsız olarak** doğrulanır. Böylece iki servisi aynı anda ayağa kaldırmadan uyumsuzluk yakalanır.

Sağlayıcıyı yalnızca taklit etmek (mock) yetmez. Taklit nesne, tüketicinin **varsayımını** sınar. Sağlayıcının o varsayımı gerçekten karşıladığını sınamaz.

**Kararsız testin (flaky test)** kökeni neredeyse her zaman bir **belirsizlik kaynağıdır**. Dört tipik kaynak vardır.

1. **Zamana dayalı varsayım.** `Thread.sleep(200)` işin 200 milisaniyede biteceğini umar; yüklü bir makinede bitmez. Çözüm, koşulu belirli bir zaman aşımı içinde yoklayan beklemedir; örneğin Awaitility kütüphanesi.
2. **Kontrol edilemeyen saat.** `LocalDate.now()` gece yarısı sınırında testle üretimi ayırır. Çözüm, dışarıdan verilebilen bir `Clock` nesnesidir.
3. **Paylaşılan ve değişebilen durum.** `static` alanlar testleri birbirine bağlar. Koşum sırası veya paralellik değişince testler kırılır.
4. **Dış ağa veya gerçek bir sisteme bağımlılık.**

Kestirme çözümler işe yaramaz. Uyku süresini uzatmak, otomatik yeniden deneme (retry) eklemek veya testi `@Disabled` ile kapatmak yalnızca belirtiyi gizler. Üstelik yeniden deneme, gerçek yarış durumlarını (race condition) ve gerçek regresyonları da yutar.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="TDD döngüsü: kırmızı, yeşil, refactor">
  <defs>
    <marker id="teststr-flow" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">TDD: kırmızı → yeşil → refactor</text>
  <rect x="34" y="66" width="150" height="72" rx="11" fill="none" stroke="#8A2E2E" stroke-width="1.8"/>
  <text x="109" y="94" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">1. Kırmızı</text>
  <text x="109" y="113" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">önce başarısız test</text>
  <text x="109" y="129" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">davranışı tanımla</text>
  <line x1="184" y1="102" x2="240" y2="102" stroke="#5D7789" stroke-width="1.5" marker-end="url(#teststr-flow)"/>
  <rect x="244" y="66" width="150" height="72" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="319" y="94" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">2. Yeşil</text>
  <text x="319" y="113" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">geçiren en yalın kod</text>
  <text x="319" y="129" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">testi geçir</text>
  <line x1="394" y1="102" x2="450" y2="102" stroke="#5D7789" stroke-width="1.5" marker-end="url(#teststr-flow)"/>
  <rect x="454" y="66" width="150" height="72" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <text x="529" y="94" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">3. Refactor</text>
  <text x="529" y="113" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tasarımı iyileştir</text>
  <text x="529" y="129" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">testler yeşilken</text>
  <path d="M529,138 L529,176 L109,176 L109,140" fill="none" stroke="#5D7789" stroke-width="1.5" stroke-dasharray="5 4" marker-end="url(#teststr-flow)"/>
  <text x="319" y="196" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">döngü küçük adımlarla tekrarlanır</text>
</svg>
```

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Contract test: tüketici sözleşme yazar, sağlayıcı doğrular">
  <defs>
    <marker id="teststr-contract" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Contract test: iki servisi birlikte ayağa kaldırmadan uyum</text>
  <rect x="24" y="70" width="170" height="80" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="109" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Tüketici</text>
  <text x="109" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">SiparisServisi</text>
  <text x="109" y="136" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">beklentiyi yazar</text>
  <rect x="235" y="86" width="170" height="48" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="108" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Sözleşme (Pact)</text>
  <text x="320" y="125" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">istek/yanıt biçimi</text>
  <rect x="446" y="70" width="170" height="80" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="531" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Sağlayıcı</text>
  <text x="531" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">KargoServisi</text>
  <text x="531" y="136" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sözleşmeyi doğrular</text>
  <line x1="194" y1="110" x2="231" y2="110" stroke="#5D7789" stroke-width="1.5" marker-end="url(#teststr-contract)"/>
  <line x1="405" y1="110" x2="442" y2="110" stroke="#5D7789" stroke-width="1.5" marker-end="url(#teststr-contract)"/>
  <text x="320" y="176" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Uyum bozulursa boru hattı kırmızı yanar — tam ortam gerekmez.</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Boru hattı 40 dakika ve sürekli kırmızı; testlerin çoğu E2E — ne yaparsın?"** Beklenen: mantığı birim/entegrasyona indir, E2E'yi kritik yolculuklara sınırla. Paralellik süreyi kısaltır, dağılımı düzeltmez; E2E'yi çoğaltmak piramidi ters çevirir.
- **"Mock ile stub farkı ne?"** Beklenen: mock **etkileşim beklentisi** doğrular, stub yalnız hazır cevap döner; dummy hiç kullanılmaz, fake çalışan basit gerçeklemedir.
- **"TDD'de kırmızı adımın amacı ne?"** Beklenen: davranışı koddan önce tanımlamak ve testin ölçtüğünü kanıtlamak. Kapsam ölçmek ya da mevcut testleri kırmak değildir.
- **"İki servisin API uyumunu ortam kurmadan nasıl doğrularsın?"** Beklenen: tüketici-yönlü contract test; sözleşmeyi sağlayıcı bağımsız doğrular. Elle şema karşılaştırması otomatik regresyon güvenliği vermez.
- **"Flaky testi kalıcı nasıl çözersin?"** Beklenen: kök nedeni (zaman, paylaşılan durum) kaldır; retry veya `@Disabled` çözüm değil, örtbastır.

### Tek cümlede

Test stratejisi, doğrulamayı **en ucuz ve en kararlı seviyeye** indirmek, sahte nesne türünü bilinçli seçmek, servis uyumunu **sözleşmeyle**, kararsızlığı ise **belirsizliği kaldırarak** çözmektir.

# teststr-1 | Orta
## Soru
Ekip, iş mantığının çoğunu yavaş ve kırılgan E2E testlerle doğruluyor; boru hattı (pipeline) 40 dakika sürüyor ve sık kırmızı yanıyor. Test piramidi bakışıyla en doğru düzeltme nedir?
## A
Tüm E2E testleri silip doğrulamayı sürüm öncesi manuel teste devretmek
🤖 Otomasyonu tümden atmak regresyon güvenliğini yok eder; sorun testin türü, varlığı değil.
## B
Mantığı aşağı seviyelere indirip E2E'yi kritik yolculuklarla sınırlamak
🤖 Doğru: ağırlığı tabana taşımak hızı ve kararlılığı artırır, E2E'yi en değerli akışlara saklar.
## C
E2E test sayısını artırıp her iş kuralını uçtan uca kapsayacak biçimde genişletmek
🤖 Ters yön: piramidi "dondurma külahına" çevirir; daha da yavaş ve kırılgan olur.
## D
Testleri paralel koşarak süreyi kısaltmak, seviye dağılımını olduğu gibi bırakmak
🤖 Paralellik süreyi kısaltır ama kırılganlığı (flaky) ve bakım maliyetini çözmez; kök neden dağılımdır.
## Doğru
B
## Açıklama
Sorun testlerin **seviyesi**: iş mantığı en ucuz ve en kararlı yer olan birim/entegrasyon katmanında doğrulanmalı. E2E, sistemin tamamını kullanıcı gözünden gördüğü için değerlidir ama yavaş ve kırılgandır; bu yüzden yalnız **kritik kullanıcı yolculuklarına** ayrılır. Piramidi ters çevirmek (C) "test dondurması" anti-desenidir. Paralellik (D) süreyi düşürür ama kök nedeni — yanlış katmanda test — çözmez. Manuel teste dönmek (A) regresyon güvenliğini yok eder.

# teststr-2 | Orta
## Soru
Bir `OdemeServisi` testinde, yerine taklit nesne geçirilen `EnvanterServisi`'nin `stokDus()` metodu için "**tam olarak bir kez** çağrılmalı" beklentisini önceden kurup test sonunda doğrulamak istiyorsun. Bu, hangi test double türünün tanımıdır?
## A
Stub
🤖 Stub yalnız hazır cevap döner; çağrı sayısını doğrulamaz, davranış beklentisi tutmaz.
## B
Dummy
🤖 Dummy sadece imzayı doldurmak için geçilir, hiç kullanılmaz; doğrulama yapmaz.
## C
Mock
🤖 Doğru: mock, üzerinde beklenti (kaç kez, hangi argümanla çağrıldı) tanımlanıp doğrulanan double'dır.
## D
Fake
🤖 Fake çalışan basit bir gerçeklemedir (ör. in-memory); etkileşim beklentisi doğrulamak onun işi değildir.
## Doğru
C
## Açıklama
**Mock**, üzerinde davranışsal beklenti (expectation) kurulan test double'dır: "`stokDus()` tam bir kez, şu argümanla çağrılmalı" gibi. Mockito'da `verify(envanter, times(1)).stokDus(...)` bunu yapar. **Stub** yalnız girdiye hazır cevap döndürür (durum doğrular, etkileşim değil). **Dummy** sadece parametre doldurur, kullanılmaz. **Fake** ise gerçek bir çalışan gerçeklemedir (in-memory repository gibi) — hızlıdır ama etkileşim beklentisi doğrulamaz. Ayrım kritiktir: mock **etkileşimi** (kaç kez çağrıldı), stub/fake ise **durumu** (sonuç ne oldu) doğrular.

# teststr-3 | Orta
## Soru
TDD'nin "kırmızı → yeşil → refactor" döngüsünde **kırmızı** adımının amacı nedir?
## A
Başarısız olan bir test yazarak istenen davranışı koddan önce tanımlamak
🤖 Doğru: test önce yazılır ve kırmızı (fail) olur; bu, testin gerçekten bir şey ölçtüğünü de kanıtlar.
## B
Mevcut tüm testleri kasıtlı kırıp test paketini sıfırdan yeniden yazmak
🤖 Döngü tek davranışa odaklıdır; mevcut yeşil testleri kırmak TDD'nin amacı değildir.
## C
Kod yazıldıktan sonra satır kapsamını ölçmek için testleri çalıştırmak
🤖 Bu "test-after"tır; TDD'de test koddan ÖNCE gelir, kırmızı adım koddan öncedir.
## D
Refactor sırasında performans darboğazını profil çıkararak bulmak
🤖 Kırmızı adım davranış tanımıyla ilgilidir; performans profillemesi ayrı bir iştir.
## Doğru
A
## Açıklama
TDD döngüsünde **kırmızı**, henüz gerçeklenmemiş davranış için **başarısız bir test** yazmaktır. Bu adımın iki değeri var: (1) istenen davranışı kodu yazmadan önce net biçimde tanımlar, (2) testin gerçekten anlamlı olduğunu kanıtlar — hiç kırmızı olmayan bir test yanlış pozitif olabilir. Ardından **yeşil** için testi geçiren en yalın kod yazılır, sonra **refactor** ile tasarım iyileştirilir (testler yeşilken). "Test-after" (C) TDD değildir; kapsam ölçümü döngünün amacı değildir.

# teststr-4 | İleri
## Soru
`SiparisServisi` (tüketici) ile `KargoServisi` (sağlayıcı) ayrı ekiplerce geliştiriliyor. İki servisi aynı ortamda birlikte ayağa kaldırmadan API uyumunu bozan değişiklikleri erken yakalamak için en uygun yaklaşım nedir?
## A
Her iki servisi de içeren tam bir E2E test paketi kurmak
🤖 E2E uyumu yakalar ama yavaş, kırılgan ve tam ortam gerektirir; sorunun "birlikte ayağa kaldırmadan" kısmını çözmez.
## B
KargoServisi'nin OpenAPI şemasını gözle inceleyip elle karşılaştırmak
🤖 Manuel şema incelemesi otomatik regresyon güvenliği vermez; her değişiklikte tekrar kırılır.
## C
KargoServisi'ni mock'layıp yalnız SiparisServisi'ni birim test etmek
🤖 Mock, tüketici tarafını test eder ama sağlayıcının o beklentiyi karşıladığını doğrulamaz; sözleşme tek taraflı kalır.
## D
Tüketici-yönlü sözleşme (contract) testi kurup sözleşmeyi sağlayıcıya doğrulatmak
🤖 Doğru: Pact gibi araçlarla tüketici beklentisi bir sözleşmeye dökülür, sağlayıcı bu sözleşmeye karşı bağımsız doğrulanır.
## Doğru
D
## Açıklama
**Contract test** (tüketici-yönlü sözleşme, ör. Pact) tam bu problemi çözer: tüketici (`SiparisServisi`) beklediği istek/yanıt biçimini bir **sözleşmeye** yazar, bu sözleşme sağlayıcı (`KargoServisi`) tarafında bağımsız olarak doğrulanır. İki servis asla aynı anda ayağa kaldırılmaz; yine de uyum bozulursa boru hattı kırmızı yanar. Salt mock (C) yalnız tüketici tarafını doğrular — sağlayıcının gerçekten uyduğunu garanti etmez. E2E (A) çalışır ama pahalı ve tam ortam ister. Elle şema karşılaştırması (B) ise otomatik regresyon güvenliği vermez — her değişiklikte yeniden gözden kaçar.

# teststr-5 | İleri
## Soru
CI'da bir test bazen geçip bazen kalıyor (flaky). İnceleyince, test kendi thread'inde asenkron bir işlemi `Thread.sleep(200)` ile bekleyip sonra sonucu kontrol ediyor. En sağlam kalıcı çözüm nedir?
## A
`sleep` süresini 200 ms'den 2000 ms'e çıkarıp güvenlik payını genişletmek
🤖 Daha uzun uyku kırılganlığı azaltır ama yok etmez, testi de yavaşlatır; yavaş makinede yine kalır.
## B
Testi `@Disabled` ile devre dışı bırakıp ilerideki bir sprint'e borç olarak not düşmek
🤖 Kapatmak kapsamı yok eder ve borç unutulur; flaky testin ölçtüğü davranış korunmaz.
## C
Sabit uyku yerine koşulu belirli bir zaman aşımı içinde yoklayan (polling) bekleme kullanmak
🤖 Doğru: deterministik senkronizasyon — koşul sağlanınca hemen devam eder, zaman aşımıyla üst sınır korunur.
## D
Testi başarısız olduğunda 3 kez otomatik yeniden deneyip (retry) biri geçerse yeşil saymak
🤖 Retry semptomu maskeler; altındaki yarış durumunu (race) gizler ve gerçek regresyonları da yutar.
## Doğru
C
## Açıklama
Flaky'nin kök nedeni **zamana dayalı varsayım**: `Thread.sleep(200)` işlemin 200 ms'de biteceğini varsayar; yüklü CI makinesinde bazen bitmez. Sağlam çözüm **koşula dayalı bekleme**: Awaitility gibi bir araçla "şu koşul sağlanana kadar, en fazla N saniye yokla" denir — koşul erken sağlanınca hemen devam eder, sağlanmazsa deterministik olarak zaman aşımına düşer. Uykuyu uzatmak (A) olasılığı düşürür ama determinizm getirmez. Retry (D) ve `@Disabled` (B) semptomu gizler; retry gerçek yarış durumlarını ve regresyonları da maskeleyebilir.

# teststr-6 | İleri
## Soru
Aşağıdaki birim testi neden kırılgan (flaky) olma riski taşır?

```java
@Test
void raporTarihiBugunOlmali() {
    Rapor r = raporServisi.olustur();
    assertEquals(LocalDate.now(), r.getTarih());
    assertTrue(r.getId() > sonId);   // sonId: paylaşılan static alan
}
```
## A
`assertEquals` yerine AssertJ'nin `assertThat` zinciri kullanılmadığı için hata mesajı zayıf kalıyor
🤖 Assertion kütüphanesi seçimi okunabilirliktir; kırılganlığın kaynağı değildir.
## B
Kontrol edilemeyen `LocalDate.now()` ve testler arası taşınan `static sonId` — iki ayrı belirsizlik kaynağı
🤖 Doğru: kontrol edilemeyen zaman kaynağı ve paylaşılan değişebilir durum, klasik iki flaky nedenidir.
## C
`raporServisi` mock'lanmadığından test gerçek bağımlılığa gidiyor ve yavaş çalışıyor
🤖 Hız tek başına kırılganlık değildir; ayrıca sorun paylaşılan durum ve zaman, bağımlılık değil.
## D
`assertTrue` içinde karşılaştırma yapıldığı için JUnit 5 assertion API'siyle uyumsuz
🤖 `assertTrue` JUnit 5'te tamamen geçerlidir; uyumsuzluk yoktur.
## Doğru
B
## Açıklama
İki bağımsız flaky kaynağı var. Birincisi **kontrol edilemeyen zaman**: `LocalDate.now()` üretim sırasında gün değişimine denk gelirse, servis içindeki `now()` ile testteki `now()` farklı güne düşebilir — çözüm, kesilebilir bir `Clock`'u enjekte edip test etmektir. İkincisi **paylaşılan değişebilir durum**: `static sonId` testler arasında taşınır; testlerin çalışma **sırasına** bağımlılık yaratır ve paralel/rastgele sırada kırılır — her test kendi durumunu izole kurmalıdır. Assertion kütüphanesi (A), hız (C) veya sözde uyumsuzluk (D) kırılganlığın nedeni değildir.
