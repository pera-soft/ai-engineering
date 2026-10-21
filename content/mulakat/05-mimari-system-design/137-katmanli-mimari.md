---
slug: katmanli-mimari
baslik: "Katmanlı Mimari ve Alternatifleri: Layered, Pipes-Filters, Microkernel, Event-Driven"
kademe: Orta
sira: 137
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Program yazarken en sık sorulan soru şudur: "bu kodu nereye koyacağım?" Program küçükken cevabın pek önemi yoktur. On binlerce satıra çıktığında ise her şeyin her yerden çağrıldığı bir yumak oluşur. O noktada tek satırlık bir değişiklik, hiç beklemediğin bir yeri kırar. **Mimari**, bu soruya baştan verilmiş cevaptır: kodun hangi bölmeye, hangi kurala göre yerleşeceğini belirler.

**Katmanlı mimari (layered / n-tier)** bu cevabın en yaygın ve en anlaşılır biçimidir. Kod, aynı türden işi yapan bölmelere ayrılır ve bu bölmeler üst üste dizilir.

Bir lokantayı düşün. Garson müşteriyle konuşur, mutfak yemeği pişirir, depo malzemeyi saklar. Garson depoya inip malzemeyi kendi karıştırmaz; siparişi mutfağa iletir. Herkesin işi belli olduğu için mutfağın tarifi değiştiğinde garsonun eğitimi değişmez. Yazılımdaki kazanç da aynıdır; adı **değişim yalıtımıdır**: bir bölmenin içi değişince öteki bölmeler etkilenmez.

### Temel kavramlar

- **Katman (layer):** aynı türden sorumluluğu taşıyan kod parçalarının toplandığı bölmedir. Klasik dizilim şudur: **sunum** (presentation — dış dünyayla, örneğin HTTP ile konuşur) → **iş** (business — kararları verir) → **kalıcılık** (persistence — veriyi yazar ve okur) → **veritabanı**.
- **İş kuralı (business rule):** işin doğasından gelen, bilgisayar hiç olmasa da geçerli olan zorunluluk: "sipariş toplamı satır kalemlerinin toplamına eşittir", "bakiye eksiye düşemez". Teknik bir ayrıntı değil, işin kendisidir.
- **Alan modeli (domain model):** işin kavramlarını (`Siparis`, `Musteri`, `Para`) kodda temsil eden sınıflar kümesi. "Alan" (domain), yazılımın çözdüğü iş dünyasıdır.
- **Bağımlılık (dependency):** A sınıfı B'yi çağırıyorsa A, B'ye bağımlıdır ve B değişince A kırılabilir. Katmanlı mimarinin tanımlayıcı kuralı, bağımlılığın **tek yönlü ve aşağı doğru** olmasıdır. Yani üst katman alttakini tanır; alt katman üsttekini **tanımaz**.
- **Kapalı katman (closed layer):** bir istek katmanları sırayla geçer, atlamak yasaktır. Atlanması **katman kaçağıdır** (layer leakage) — sunum katmanının doğrudan SQL yazması gibi. Kaçak, katmanın sağladığı izolasyonu iptal eder.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Katmanlı mimaride bağımlılık aşağı akar">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Katmanlar ve tek yönlü bağımlılık</text>
  <rect x="180" y="40" width="280" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="60" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Sunum (presentation)</text>
  <rect x="180" y="80" width="280" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="100" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">İş (business)</text>
  <rect x="180" y="120" width="280" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="140" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Kalıcılık (persistence)</text>
  <rect x="180" y="160" width="280" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="180" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Veritabanı</text>
  <text x="150" y="120" text-anchor="middle" font-size="26" fill="#5D7789" font-family="system-ui">↓</text>
  <text x="112" y="124" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">bağımlılık</text>
</svg>
```

### Nasıl çalışır?

"Siparişi onayla" isteğini izleyelim. Sunum katmanı HTTP isteğini alır, gövdesini nesneye çevirir ve kimliği doğrular. Kendisi **karar vermez**; işi iş katmanına devreder. İş katmanı siparişi kalıcılık katmanından ister, iş kuralını uygular ("ödenmemiş sipariş onaylanamaz") ve sonucu yazdırır. Kalıcılık katmanı SQL ve ORM ayrıntısını bilir, iş kuralını bilmez. Böylece ORM'i değiştirmek yalnız kalıcılık katmanını ilgilendirir; ekranın biçimini değiştirmek de yalnız sunum katmanını.

Katmanlı mimarinin klasik tuzağı şudur: iş kuralları alan nesnelerinden sızar ve servis sınıflarına yığılır. `Siparis` de yalnız getter/setter taşıyan bir veri torbasına döner. Buna **anemik alan modeli (anemic domain model)** denir. Martin Fowler bunu bir **anti-desen** sayar; çünkü nesne yönelimin özü, veriyi ve onu değiştiren davranışı **birlikte** tutmaktır. Anemik modelde sistem "sınıfları olan ama aslında prosedürel" hâle gelir. Aynı kural birden çok serviste tekrarlanır ve zamanla tutarsızlaşır. **Zengin alan modelinde** (rich domain model) kural nesnenin içindedir: `siparis.onayla()` kendi geçerliliğini kendi korur, servis yalnız akışı yönetir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Anemik alan modeli ile zengin alan modelinin karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Anemik model vs zengin model: davranış nerede?</text>
  <text x="160" y="50" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Anemik (anti-desen)</text>
  <rect x="46" y="60" width="228" height="50" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="160" y="80" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Service: tüm iş kuralları</text>
  <text x="160" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hesapla, doğrula, uygula</text>
  <rect x="46" y="148" width="228" height="48" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="160" y="168" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">Siparis: yalnız get/set</text>
  <text x="160" y="186" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">veri torbası, davranışsız</text>
  <text x="480" y="50" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Zengin (hedef)</text>
  <rect x="366" y="60" width="228" height="48" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="480" y="80" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Service: ince orkestrasyon</text>
  <text x="480" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">akışı yönetir, kural taşımaz</text>
  <rect x="366" y="146" width="228" height="50" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="480" y="166" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Siparis: veri + davranış</text>
  <text x="480" y="184" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">onayla(), toplamHesapla()</text>
</svg>
```

### Mülakatta ne sorulur?

En sık üç şey sorulur. Birincisi **bağımlılık yönü**: "katmanlı mimaride kim kimi çağırabilir?" — cevap yalnızca aşağı doğru. İkincisi **anemik model belirtisi**: "bütün mantık `@Service` sınıflarında, alan nesneleri boş; sorun ne?" Üçüncüsü **alternatif stiller** ve her birinin optimize ettiği kuvvet:

- **Borular ve süzgeçler (pipes and filters):** veri, her biri tek bir dönüşüm yapan bağımsız süzgeçlerden sırayla akar. Süzgeçler birbirini tanımaz; yalnız giriş/çıkış sözleşmesini paylaşır. Örnekleri: ETL işleri, derleyici aşamaları, akış işleme. Kuvveti **birleştirilebilirliktir**.
- **Microkernel / plugin**: en temel işi yapan **minimal bir çekirdek** ve çekirdeğin tanımladığı **genişleme noktalarına** takılan bağımsız eklentiler. IDE'ler, tarayıcılar. Kuvvet: **özelleştirilebilirlik**.
- **Olay tabanlı (event-driven):** bileşenler doğrudan çağrı yapmaz; **olay** yayınlar ve dinler, birbirini tanımazlar. Yayıncı, tüketicinin işini bitirmesini beklemez. Kuvveti **ölçeklenebilirlik ve bağımsız dağıtımdır**. Bedeli ise **gözlemlenebilirliktir**: iş akışı olaylara dağıldığı için uçtan uca izleme ve hata ayıklama zorlaşır. Ayrıca sıralama ve nihai tutarlılık yeni sorunlar doğurur.

Seçim kuralı şudur: stil moda ya da alışkanlıkla seçilmez. Sistemin **öncelikli mimari niteliklerine** göre seçilir; bunlar ölçeklenebilirlik, dağıtılabilirlik, test edilebilirlik, performans ve maliyettir. Hiçbir stil hepsini birden veremez; her biri bir kuvveti alıp başkasından ödün verir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Alternatif mimari stiller ve optimize ettikleri kuvvetler">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Alternatif stiller: her biri farklı kuvvet</text>
  <rect x="24" y="44" width="188" height="140" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="118" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Pipes &amp; filters</text>
  <text x="118" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bağımsız süzgeç</text>
  <text x="118" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">zinciri (ETL)</text>
  <text x="118" y="164" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">birleştirilebilirlik</text>
  <rect x="226" y="44" width="188" height="140" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Microkernel</text>
  <text x="320" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çekirdek + eklenti</text>
  <text x="320" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">(IDE, tarayıcı)</text>
  <text x="320" y="164" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">özelleştirilebilirlik</text>
  <rect x="428" y="44" width="188" height="140" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="522" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">Event-driven</text>
  <text x="522" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">olayla gevşek bağ</text>
  <text x="522" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bağımsız ölçek</text>
  <text x="522" y="156" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">ölçeklenebilirlik</text>
  <text x="522" y="175" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">bedel: izleme zor</text>
</svg>
```

### Tek cümlede

Katmanlı mimari, kodu sorumluluk bölmelerine ayırır ve bağımlılığı tek yöne (aşağı) zorlayarak değişimi yalıtır. Asıl tehlikesi, iş kurallarının alan nesnelerinden servise sızdığı anemik modeldir. Stil seçimi de her zaman öncelikli niteliklerin ödünleşme analizine dayanır.

# layer-1 | Temel
## Soru
Klasik katmanlı (layered) mimaride bileşenler arasındaki bağımlılık yönü için hangisi doğrudur?
## A
Her katman diğer bütün katmanları serbestçe çağırabilir; bir yön kısıtı yoktur
🤖 Serbest çağrı katmanlamayı çökertir; katmanlı mimarinin özü yönlü kısıttır.
## B
Bağımlılık tek yönde aşağı akar; üst katman alttakini tanır, alt katman üstü tanımaz
🤖 Doğru: yönlü ve tek taraflı bağımlılık, değişimi katman içinde izole eder.
## C
Veritabanı katmanı sunum katmanını çağırarak veriyi doğrudan yukarı iter
🤖 Bu ters yön; alt katman üst katmanı tanımamalıdır.
## D
Katmanlar birbirini yalnız olaylarla tetikler; doğrudan metot çağrısı yasaktır
🤖 O event-driven stildir; klasik katmanlıda doğrudan, aşağı yönlü çağrı vardır.
## Doğru
B
## Açıklama
Katmanlı mimarinin tanımlayıcı özelliği **tek yönlü, aşağıya akan bağımlılıktır**: sunum → iş → kalıcılık → veritabanı. Üst katman alttakinin **soyutlamasına** dayanır, alt katman üsttekini **tanımaz**. Bu yönlü kısıt sayesinde bir katmanın içi (örneğin ORM'den JDBC'ye geçmek) değiştiğinde üst katmanlar etkilenmez; buna **değişim izolasyonu** denir. Serbest çağrı (A) veya ters yön (C) bu izolasyonu bozar; olayla haberleşme (D) ise farklı bir mimari stildir. Aynı kısıtın ihlaline, yani bir katmanı atlayıp aşağıya doğrudan uzanmaya **katman kaçağı** (layer leakage) denir.

# layer-2 | Orta
## Soru
Bir ekip katmanlı mimari kurdu ama tüm iş kuralları `@Service` sınıflarında; alan nesneleri (`Siparis`, `Musteri`) yalnız getter/setter içeriyor. Bu belirti hangi anti-desene işaret eder?
## A
Anemik alan modeli: davranış alan nesnelerinden servis katmanına sızmış
🤖 Doğru: veri ile davranışın ayrılması bu anti-desenin tam tanımıdır.
## B
God object: tek bir sınıf sistemin bütün sorumluluklarını üstlenmiş durumda
🤖 Sorun tek dev sınıf değil; davranışın alan nesnelerinden servise dağılmış olması.
## C
Sızdıran soyutlama: alt katmanın iç ayrıntısı üstteki arayüzden görünüyor
🤖 O farklı bir kavram; buradaki sorun iş mantığının konumu, arayüz sızıntısı değil.
## D
Döngüsel bağımlılık: iki katman birbirini karşılıklı olarak çağırıyor
🤖 Belirtide döngü yok; sorun davranışın anemik nesnelerden dışarı taşınması.
## Doğru
A
## Açıklama
**Anemik alan modeli**, alan nesnelerinin veri taşıyıp davranış taşımadığı, iş kurallarının servis katmanına yığıldığı durumdur. Martin Fowler bunu bir **anti-desen** sayar, çünkü nesne yönelimin özünü — veri ile onu değiştiren davranışın birlikte durmasını — ihlal eder; sistem "sınıfları olan ama prosedürel" hale gelir ve aynı kural farklı servislerde tekrarlanıp tutarsızlaşır. Katmanlı mimaride sık görülür, çünkü servis katmanı iş mantığını kendine çekmeye eğilimlidir. Çözüm, değişmezleri (invariant) ve kuralları ilgili alan nesnesine geri taşımaktır: `siparis.onayla()`. God object (B) ve döngüsel bağımlılık (D) başka sorunlardır.

# layer-3 | Orta
## Soru
Aşağıdaki iş yüklerinden hangisi **pipes and filters** mimari stiline en doğal biçimde uyar?
## A
Kullanıcıların birbirine anlık mesaj gönderdiği, bağlantıyı açık tutan sohbet uygulaması
🤖 Bu düşük gecikmeli, durum-yoğun etkileşim; süzgeç zinciri modeline uymaz.
## B
Karmaşık iş kurallarıyla kredi başvurusu değerlendirip onaylayan işlem sistemi
🤖 Bu zengin alan mantığı ister; katmanlı/DDD daha uygun, akış-süzgeç değil.
## C
Anahtar-değer önbelleğinden tek adımda okuma yapan düşük gecikmeli servis
🤖 Tek adımlık okuma; çok aşamalı dönüşüm zinciri yok, süzgeç modeli gereksiz.
## D
Log dosyalarını okuyup ayrıştıran, zenginleştiren ve depoya yazan ETL boru hattı
🤖 Doğru: bağımsız dönüşüm adımlarından veri akışı pipes-filters'ın klasik örneğidir.
## Doğru
D
## Açıklama
**Pipes and filters**, veriyi birbirine **borularla** (pipe) bağlı, her biri tek bir dönüşüm yapan bağımsız **süzgeçlerden** (filter) geçiren stildir. Süzgeçler birbirini tanımaz, yalnız giriş/çıkış sözleşmesini paylaşır; bu da **birleştirilebilirlik** ve **yeniden kullanım** sağlar — adımların sırası değiştirilebilir, araya yeni adım eklenebilir. ETL (oku → ayrıştır → zenginleştir → yaz), derleyici aşamaları ve akış işleme klasik örneklerdir. Anlık etkileşim (A), zengin iş mantığı (B) veya tek adımlık okuma (C) çok aşamalı bir dönüşüm zinciri barındırmadığı için bu stilden fayda görmez.

# layer-4 | Orta
## Soru
**Microkernel (plugin)** mimarisinin temel yapısı nedir?
## A
Eşit yetkili birçok servisin ağ üzerinden olaylarla haberleşip bağımsız ölçeklenmesi
🤖 O event-driven/mikroservis tarifidir; microkernel'in çekirdek+eklenti yapısı değil.
## B
Minimal bir çekirdek ve tanımlı genişleme noktalarına takılan bağımsız eklentiler
🤖 Doğru: küçük çekirdek ve takılıp çıkarılabilir eklentiler microkernel'in özüdür.
## C
Katmanların üst üste yığılıp tek bir dağıtım birimi olarak paketlenmesi
🤖 O katmanlı monolit tanımı; eklenti ve genişleme noktası fikri yok.
## D
Verinin, her biri tek dönüşüm yapan bağımsız süzgeç zincirinden akması
🤖 O pipes and filters; çekirdek+eklenti kavramı bulunmaz.
## Doğru
B
## Açıklama
**Microkernel** (plugin) mimarisi, en temel işlevi taşıyan **minimal bir çekirdek** ile bu çekirdeğin tanımladığı **genişleme noktalarına** (extension point) takılan **bağımsız eklentilerden** oluşur. Çekirdek eklentileri tek tek tanımaz; yalnız bir sözleşme ve kayıt mekanizması sunar. IDE'ler (Eclipse, IntelliJ), tarayıcılar ve derleme araçları bu stili kullanır; çekirdek hiç değişmeden yeni yetenek eklenebilir. Bu yüzden güçlü olduğu nitelik **özelleştirilebilirlik** ve **evrimsel genişlemedir**. Diğer şıklar event-driven (A), katmanlı monolit (C) ve pipes-filters (D) stillerini tarif eder.

# layer-5 | Orta
## Soru
Event-driven (olay-tabanlı) mimarinin katmanlı mimariye kıyasla en belirgin **zayıf yönü** hangisidir?
## A
Bileşenler sıkı bağlı olduğundan bağımsız ölçeklenemez, hepsi birlikte büyütülür
🤖 Tersine; event-driven gevşek bağlılıkla bağımsız ölçeklenmeyi kolaylaştırır.
## B
Yayıncı her tüketicinin işini bitirmesini beklemek zorundadır, gecikme birikir
🤖 Yayıncı tüketiciyi beklemez; olayı yayınlar ve yoluna devam eder (asenkron).
## C
Uçtan uca iş akışını izlemek ve hata ayıklamak zordur; akış olaylara dağılmıştır
🤖 Doğru: dolaylı olay akışı gözlemlenebilirliği ve hata ayıklamayı zorlaştırır.
## D
Bileşenler tek bir dağıtım biriminde toplanmak zorundadır, ayrı dağıtılamaz
🤖 Tersine; olaylarla gevşek bağlılık ayrı ayrı dağıtımı kolaylaştırır.
## Doğru
C
## Açıklama
Event-driven mimari, bileşenleri **olaylar** üzerinden **gevşek bağlı** haberleştirir; bu, **ölçeklenebilirlik**, **bağımsız dağıtım** ve esneklik kazandırır. Bedeli **gözlemlenebilirliktir**: bir iş akışı çok sayıda bağımsız yayınlama/tüketme adımına dağıldığından uçtan uca izleme, hata ayıklama ve akışın bütününü anlamak zorlaşır; olay sırası ve **nihai tutarlılık** (eventual consistency) yeni sorunlar doğurur. Bu yüzden dağıtık izleme (distributed tracing) ve korelasyon kimlikleri gerekir. A ve D, event-driven'ın güçlü yönlerini tersine çevirip zayıflık gibi sunar; B ise yayınla/abone ol modelini senkron çağrı sanmaktır — yayıncı tüketicinin işini bitirmesini beklemez.

# layer-6 | İleri
## Soru
Bir mimari stili seçerken en sağlam yaklaşım hangisidir?
## A
Öncelikli mimari nitelikleri belirle, stilleri ödünleşmelerine göre karşılaştır
🤖 Doğru: seçim, önceliklendirilmiş kalite niteliklerinin trade-off analizine dayanır.
## B
Her zaman katmanlı mimariyi seç; en basit seçenek her bağlamda en iyi olandır
🤖 Basitlik değerlidir ama ölçek/dağıtılabilirlik gerektiren bağlamlarda yetersiz kalır.
## C
En yüksek performansı veren stili seç; performans diğer bütün niteliklerin önündedir
🤖 Performans tek boyut; sürdürülebilirlik/ölçek/maliyet bağlama göre önce gelebilir.
## D
Sektörde en popüler olan modern stili seç; yaygın tercih en güvenli tercihtir
🤖 Popülerlik bağlamı görmez; yanlış nitelik için optimize edilmiş stil zarar verir.
## Doğru
A
## Açıklama
Mimari seçimi bir **ödünleşme (trade-off) analizidir**: hiçbir stil bütün nitelikleri birden maksimize edemez. Doğru yöntem, sistemin **öncelikli mimari niteliklerini** (-ility: ölçeklenebilirlik, dağıtılabilirlik, test edilebilirlik, performans, maliyet) bağlama göre belirlemek, sonra aday stillerin bu niteliklerdeki güçlü ve zayıf yönlerini karşılaştırmaktır. Katmanlı stil basitlik ve test edilebilirlik verir ama dağıtık ölçeklenmede zayıftır; event-driven ölçek verir ama gözlemlenebilirlikten ödün verir; microkernel özelleştirilebilirlik verir. "Her zaman katmanlı" (B), "yalnız performans" (C) veya "en popüler" (D) bağlamı görmezden gelen kör kurallardır — mimaride bağlamsız, mutlak bir "en iyi" yoktur.
