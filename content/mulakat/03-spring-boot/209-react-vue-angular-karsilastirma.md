---
slug: react-vue-angular-karsilastirma
baslik: "React, Vue, Angular: Üç Yaklaşımın Karakter Farkı ve Ekip Seçimi"
kademe: Temel
sira: 209
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Bir restoran açacaksın ve mutfağı kuracaksın. Üç teklif geliyor. Birincisi sana çok iyi bir ocak veriyor; fırını, bulaşık makinesini, dolapları sen seçeceksin — özgürsün ama her seçim senin sorumluluğunda ve iki şubeni farklı kuran iki şef sonunda birbirine benzemeyen iki mutfak çıkarıyor. İkincisi ocakla birlikte dolabı ve tezgâhı da öneriyor, ama zorlamıyor; kurulum en hızlısı bu. Üçüncüsü anahtar teslim, tam donanımlı bir mutfak veriyor: her şey resmî, her şey birbirine uyumlu, karşılığında "burada işler böyle yapılır" kuralına uyacaksın.

**React**, **Vue** ve **Angular** arasındaki fark özünde budur. Bu konu sana ön yüz geliştiricisi olmayı öğretmez; senior bir backend geliştiricisi olarak mülakatta "hiç bilmiyorum" dememen için üçünün **karakterini** ve **seçim gerekçesini** verir. Tıpkı RabbitMQ ile Kafka'da olduğu gibi, buradaki doğru soru da "hangisi daha iyi?" değil, **"hangi iş ve hangi ekip için?"**dir.

### Temel kavramlar

- **Bileşen (component)**: Kendi şablonu, kendi durumu ve kendi davranışı olan, yeniden kullanılabilir arayüz parçası — ön yüzün "sınıf"ı gibidir.
- **Durum (state)**: Bileşenin hafızası; içindeki veri değiştiğinde o bileşenin ekrandaki görüntüsü yeniden çizilir.
- **Özellikler (props) ve olaylar (events)**: Veri üst bileşenden alt bileşene **props** ile aşağı akar; kullanıcı etkileşimi **olay** olarak aşağıdan yukarı bildirilir.
- **Bildirimsel (declarative)**: "DOM'u şu adımlarla değiştir" değil, "durum buysa arayüz budur" yazmak. Karşıtı **emirsel (imperative)**: güncelleme adımlarını tek tek elle yazmak.
- **Sanal DOM (virtual DOM)**: Arayüzün bellekte tutulan hafif kopyası; yeni hâl ile eski hâl karşılaştırılıp (diff) yalnız değişen kısım gerçek DOM'a yazılır.
- **Reaktivite (reactivity)**: Hangi verinin hangi ekran parçasını beslediğinin izlenmesi; veri değişince yalnız ona bağlı parçanın güncellenmesi.
- **Sinyal (signal)**: Okunduğu yerleri kendisi kaydeden değer kabı; değiştiğinde yalnız onu okuyan parçalar yeniden hesaplanır — ince taneli reaktivitenin bugünkü ortak adı.
- **Kütüphane (library) ve çerçeve (framework)**: Kütüphaneyi **sen** çağırırsın, kalan kararlar sende; çerçeve **seni** çağırır, iskeleti ve kuralları o koyar.
- **JSX ve tek dosya bileşeni (single-file component — SFC)**: JSX, JavaScript'in içine gömülen şablon söz dizimi; SFC ise şablonu, betiği ve stili tek bir dosyada toplayan bileşen biçimi.
- **Bağımlılık enjeksiyonu (dependency injection — DI)**: Bir sınıfın ihtiyaç duyduğu servisi kendisinin üretmesi yerine dışarıdan verilmesi — Spring'in çekirdek fikri.
- **Standalone bileşen (standalone component)**: Angular'da bileşenin ayrı bir modül bildirimine ihtiyaç duymadan kendi bağımlılıklarını doğrudan tanımlaması; artık varsayılan yol budur.
- **npm / package.json ve Vite**: `npm` ön yüzün paket yöneticisi, `package.json` bağımlılık listesi (Maven'ın `pom.xml`'i gibi); **Vite** ise geliştirme sunucusunu açan ve üretim paketini üreten derleme aracıdır.

### Nasıl çalışır?

Üç aracın da altında **aynı üç fikir** yatar: bileşen, durum ve bildirimsellik. Farklar bu fikirlerin nasıl paketlendiğindedir.

**Birinci fikir — bildirimsellik.** Backend geliştiricisinin en çok kaçırdığı zihinsel sıçrama budur. jQuery çağında arayüz **emirsel** yazılırdı: bir değer değişince ilgili DOM elemanını bul, metnini değiştir, başka bir elemanı gizle, üçüncüsüne bir işaret ekle. Ekranın doğru görünmesi, senin bu adımları eksiksiz ve doğru sırada yazmana bağlıydı; her yeni kural eski adımlarla çakışabilirdi. Bildirimsel yaklaşımda ise sadece **durumu** ve "bu durumda arayüz şudur" eşlemesini yazarsın; DOM'a hangi dokunuşların yapılacağına araç karar verir.

```
// Emirsel (jQuery çağı): güncelleme adımlarını sen yazarsın
sayac = sayac + 1;
document.querySelector("#sayac").textContent = sayac;    // metni elle güncelle
document.querySelector("#uyari").hidden = (sayac < 10);  // görünürlüğü elle ayarla

// Bildirimsel: yalnız "durum buysa arayüz budur" dersin
// durum:  sayac
// arayüz: sayaç değerini gösteren bir alan + sayac >= 10 ise uyarı bileşeni
// DOM'a hangi dokunuşun yapılacağına araç karar verir
```

```svg
<svg viewBox="0 0 660 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Emirsel jQuery yaklaşımı ile bildirimsel yaklaşımın karşılaştırması">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">DOM'u kim günceller?</text>
  <text x="165" y="44" text-anchor="middle" font-size="12" font-weight="700" fill="#B67A1A" font-family="system-ui">Emirsel (jQuery çağı)</text>
  <rect x="24" y="54" width="282" height="150" rx="10" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.5"/>
  <text x="165" y="78" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">durum değişti</text>
  <text x="165" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">1. elemanı bul</text>
  <text x="165" y="116" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">2. metnini değiştir</text>
  <text x="165" y="134" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">3. diğerini gizle</text>
  <text x="165" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">4. üçüncüyü işaretle</text>
  <text x="165" y="176" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B3261E" font-family="system-ui">adımlar SENDE</text>
  <text x="165" y="194" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kural arttıkça çakışır</text>
  <text x="495" y="44" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Bildirimsel (React / Vue / Angular)</text>
  <rect x="354" y="54" width="282" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="495" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">durum: sayac = 10</text>
  <text x="495" y="104" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">arayüz = f(durum)</text>
  <text x="495" y="128" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"bu durumda ekran budur"</text>
  <text x="495" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">DOM dokunuşunu ARAÇ yapar</text>
  <text x="495" y="176" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">adımlar ARAÇTA</text>
  <text x="495" y="194" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">sen sonucu tarif edersin</text>
  <text x="330" y="224" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Fark söz dizimi değil, sorumluluk yeridir: güncelleme adımlarını kim yazıyor?</text>
</svg>
```

**İkinci fikir — değişiklik nasıl tespit edilir.** Bildirimsel yazdığında geriye tek soru kalır: durum değişti, ekranın hangi parçası yeniden çizilecek? Üçü bu soruyu farklı yanıtlar. **React**, bileşen işlevini yeniden çalıştırıp yeni sanal DOM ağacını eskisiyle **karşılaştırır** ve yalnız farkı gerçek DOM'a yazar. **Vue**, hangi bileşenin hangi veriyi okuduğunu baştan **izler** (bağımlılık takibi); veri değişince yalnız o veriye bağlı parçayı günceller — ince taneli reaktivite budur. **Angular** eskiden her olay sonrası ağacı tarayan zone tabanlı bir değişiklik denetimi kullanırdı; bugün **sinyaller** üzerinden yürüyen, hangi parçanın etkilendiğini doğrudan bilen bir modele geçmiştir. Yön ortaktır: üç tarafta da sinyal tabanlı, ince taneli reaktivite yaygınlaşıyor — ayrım giderek karakter meselesine iniyor.

**Üçüncü fikir — kütüphane mi çerçeve mi.** Asıl karakter farkı buradadır.

- **React** bir **kütüphanedir**; çekirdeği kasten küçüktür ve esas olarak görünüm katmanını verir. Yönlendirme (routing), durum yönetimi, form doğrulama, veri çekme gibi ihtiyaçlar için ekosistemden **sen seçersin**. Bu, özgürlük ve aynı ölçüde **karar yüküdür**: iki React projesi birbirine hiç benzemeyebilir. Şablonu JSX ile JavaScript'in içine yazar, bileşenleri işlev + hook biçiminde kurar; sunucu bileşenleri (server components) ile bileşenin bir kısmının sunucuda çalışıp tarayıcıya hazır sonuç göndermesi de artık ana akımdır. En büyük iş ilanı ve aday havuzu Reacttedir.
- **Vue** kütüphane ile çerçeve arasında durur. İmzası **tek dosya bileşenidir**: şablon, betik ve stil aynı dosyada. Composition API ile mantığı yeniden kullanılabilir parçalara ayırırsın, reaktivitesi ince tanelidir. Öğrenme eğrisi üçünün en yumuşağıdır ve yönlendirici ile durum yönetimi paketleri **resmî** olarak vardır — yani seçmek zorunda değilsin ama zorlanmıyorsun da.
- **Angular** tam anlamıyla bir **çerçevedir**: yönlendirme, HTTP istemcisi, form yönetimi, **bağımlılık enjeksiyonu** ve test altyapısı kutudan resmî olarak çıkar. **TypeScript zorunludur.** Bileşenler artık standalone yapıda kurulur, ayrı bir modül bildirimi gerekmez. Spring geliştiricisine **en tanıdık** gelen model budur: DI ile servis alma, tip disiplini, "burada işler böyle yapılır" diyen resmî bir iskelet. Bedeli daha dik bir öğrenme eğrisidir; kazancı büyük ve değişken kadrolu ekiplerde **tutarlılıktır**.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kütüphane ve çerçeve ekseninde React Vue Angular yerleşimi ve kutudan çıkan parçalar">
  <defs>
    <marker id="react-vue-angular-karsilastirma-ok" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kütüphane ↔ çerçeve ekseni</text>
  <text x="86" y="42" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ince çekirdek, sen seçersin</text>
  <text x="580" y="42" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kutudan çıkan bütün</text>
  <line x1="40" y1="58" x2="620" y2="58" stroke="#5D7789" stroke-width="1.4" marker-start="url(#react-vue-angular-karsilastirma-ok)" marker-end="url(#react-vue-angular-karsilastirma-ok)"/>
  <circle cx="120" cy="58" r="6" fill="#186399"/>
  <circle cx="320" cy="58" r="6" fill="#25804C"/>
  <circle cx="533" cy="58" r="6" fill="#B67A1A"/>
  <rect x="30" y="76" width="180" height="140" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="120" y="98" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">React — kütüphane</text>
  <text x="120" y="122" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kutudan: görünüm katmanı</text>
  <text x="120" y="142" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">JSX + işlev/hook</text>
  <text x="120" y="162" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">router/durum: sen seç</text>
  <text x="120" y="186" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">özgürlük + karar yükü</text>
  <text x="120" y="204" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">en büyük aday havuzu</text>
  <rect x="225" y="76" width="190" height="140" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="98" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Vue — arada</text>
  <text x="320" y="122" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kutudan: görünüm + resmî</text>
  <text x="320" y="140" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">router ve durum paketi</text>
  <text x="320" y="162" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">tek dosya bileşeni (SFC)</text>
  <text x="320" y="186" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">en yumuşak eğri</text>
  <text x="320" y="204" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ince taneli reaktivite</text>
  <rect x="435" y="76" width="195" height="140" rx="10" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.5"/>
  <text x="533" y="98" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B67A1A" font-family="system-ui">Angular — çerçeve</text>
  <text x="533" y="122" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kutudan: router, HTTP,</text>
  <text x="533" y="140" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">form, DI, test</text>
  <text x="533" y="162" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">TypeScript zorunlu</text>
  <text x="533" y="186" text-anchor="middle" font-size="10" font-weight="700" fill="#B67A1A" font-family="system-ui">tutarlılık + dik eğri</text>
  <text x="533" y="204" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Spring'e en tanıdık model</text>
  <text x="330" y="238" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Kütüphaneyi sen çağırırsın; çerçeve seni çağırır — iskeleti ve kuralı o koyar.</text>
</svg>
```

**Dördüncü fikir — seçim bir mühendislik kararıdır.** Üç aracın da tipik bir kurumsal uygulamada performansı birbirine yakındır; kıyaslama testleri kararı belirlemez. Belirleyen şey **ekiptir**. Sorulacak sorular şunlardır: Kaç kişisiniz ve kadro ne sıklıkla değişiyor? Kurumda tek bir standart dayatmanız gerekiyor mu? Şehrinizde hangi aday havuzu daha büyük? Elinizdeki bileşen kütüphanesi ve tasarım sistemi hangisiyle uyumlu? Ekip dik bir öğrenme eğrisini kaldırabilir mi? "Kararı benim yerime araç versin" mi diyorsunuz, yoksa "seçim özgürlüğü isterim" mi? Kural tek cümledir: **teknoloji seçimini ekibin bildiği ve sürdürebileceği şey belirler, moda değil.**

```svg
<svg viewBox="0 0 660 276" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="React Vue Angular seçim karar matrisi tablosu">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Karar matrisi: hangi iş, hangi ekip?</text>
  <rect x="24" y="36" width="210" height="28" fill="#1C3247"/>
  <text x="129" y="55" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">Ölçüt</text>
  <rect x="234" y="36" width="136" height="28" fill="#186399"/>
  <text x="302" y="55" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">React</text>
  <rect x="370" y="36" width="136" height="28" fill="#25804C"/>
  <text x="438" y="55" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">Vue</text>
  <rect x="506" y="36" width="130" height="28" fill="#B67A1A"/>
  <text x="571" y="55" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">Angular</text>
  <rect x="24" y="64" width="612" height="34" fill="#F4F6F8" stroke="#5D7789" stroke-width="0.8"/>
  <text x="34" y="86" font-size="10.5" fill="#1C3247" font-family="system-ui">Öğrenme eğrisi</text>
  <text x="302" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">orta</text>
  <text x="438" y="86" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">en yumuşak</text>
  <text x="571" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">en dik</text>
  <rect x="24" y="98" width="612" height="34" fill="#FFFFFF" stroke="#5D7789" stroke-width="0.8"/>
  <text x="34" y="120" font-size="10.5" fill="#1C3247" font-family="system-ui">Karar yükü (router, durum)</text>
  <text x="302" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sende</text>
  <text x="438" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">resmî öneri var</text>
  <text x="571" y="120" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B67A1A" font-family="system-ui">çerçevede</text>
  <rect x="24" y="132" width="612" height="34" fill="#F4F6F8" stroke="#5D7789" stroke-width="0.8"/>
  <text x="34" y="154" font-size="10.5" fill="#1C3247" font-family="system-ui">İşe alım havuzu</text>
  <text x="302" y="154" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">en geniş</text>
  <text x="438" y="154" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">orta</text>
  <text x="571" y="154" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kurumsalda güçlü</text>
  <rect x="24" y="166" width="612" height="34" fill="#FFFFFF" stroke="#5D7789" stroke-width="0.8"/>
  <text x="34" y="188" font-size="10.5" fill="#1C3247" font-family="system-ui">Kurumsal tek standart</text>
  <text x="302" y="188" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ekip kurar</text>
  <text x="438" y="188" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kısmen hazır</text>
  <text x="571" y="188" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B67A1A" font-family="system-ui">kutudan gelir</text>
  <rect x="24" y="200" width="612" height="34" fill="#F4F6F8" stroke="#5D7789" stroke-width="0.8"/>
  <text x="34" y="222" font-size="10.5" fill="#1C3247" font-family="system-ui">Backend'e tanıdıklık (DI, tip)</text>
  <text x="302" y="222" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">düşük</text>
  <text x="438" y="222" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">orta</text>
  <text x="571" y="222" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B67A1A" font-family="system-ui">en yüksek</text>
  <text x="330" y="256" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Kolon değil satır okunur: hangi ölçüt sizin bağlamınızda ağır basıyor?</text>
  <text x="330" y="272" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Seçimi ekibin bildiği ve sürdürebileceği şey belirler, moda değil.</text>
</svg>
```

**Üçünde ortak olan mekanik.** Bağımlılıklar `npm` ile kurulur ve `package.json` içinde listelenir — Maven'ın `pom.xml`'inin karşılığıdır. Geliştirme sunucusunu ve üretim paketini bugün ağırlıkla **Vite** üretir. **TypeScript** üçünde de fiilî standarttır: Angular'da zorunlu, React ve Vue'da neredeyse her ciddi projede tercihtir. Sebebi şudur: tip bilgisi olmadan büyük bir bileşen ağacında hangi verinin nereye aktığını izlemek imkânsızlaşır, ayrıca düzenleyicinin (IDE) yardımı kesilir. Veri akışı da disiplinlidir: **props ile yukarıdan aşağı** akar, **olaylar ile aşağıdan yukarı** bildirilir. Bu, senin katman yönü kuralınla (`types → config → repo → service → web`) aynı fikrin ön yüzdeki hâlidir: akış tek yönlü olduğu sürece hatanın nereden geldiği bulunabilir.

**Backend köprüsü — mülakattaki asıl konumun.** Üçü de sonuçta **senin JSON'unu tüketir**. Senin API'n framework'ten bağımsızdır: aynı uç nokta React de Vue da Angular da tarafından çağrılabilir; değişen yalnız istemcideki bileşen kodudur. Bu yüzden "hangisini biliyorsun?" sorusuna verilecek dürüst ve güçlü cevap şudur: **hangisiyle çalıştığını açıkça söylersin.** Ardından üçünün de tükettiği sözleşmeyi senin tasarladığını ve aralarındaki karakter farkını bildiğini gösterirsin. Sınır konularında asıl sorumlu zaten sensin: CORS, oturum ve çerez, kimlik doğrulama, sayfalama sözleşmesi, hata gövdesi biçimi. Abartılı "üçünü de ileri düzeyde bilirim" iddiası ilk teknik soruda çöker; "ön yüz benim işim değil" cevabı ise sınır konularını da reddettiği için zayıf görünür.

### Mülakatta ne sorulur?

- **"Bildirimsel arayüz ne demek?"** → "Durum buysa arayüz budur" yazmak; DOM'a hangi dokunuşun yapılacağını araç belirler. Emirsel yaklaşımda (jQuery) adımları sen yazardın. Fark söz dizimi değil, **sorumluluğun yeridir**.
- **"React ile Angular arasındaki temel fark?"** → React ince çekirdekli bir **kütüphanedir**, yönlendirme/durum/form için ekosistemden sen seçersin. Angular bir **çerçevedir**: yönlendirme, HTTP, form, DI ve test resmî olarak kutudan çıkar; TypeScript zorunludur.
- **"Vue'yu nereye koyarsın?"** → İkisinin arasına. Tek dosya bileşeni, Composition API, ince taneli reaktivite, en yumuşak öğrenme eğrisi, **resmî** yönlendirici ve durum paketi.
- **"Değişiklik ekrana nasıl yansır?"** → React yeniden çalıştırıp sanal DOM ağaçlarını karşılaştırır; Vue bağımlılık takibiyle yalnız ilgili parçayı günceller; Angular sinyaller üzerinden yürür (eskiden zone tabanlı değişiklik denetimi kullanırdı). Yön ortak: ince taneli, sinyal tabanlı reaktivite.
- **"Neden Angular kurumsalda sık görülür?"** → Kutudan çıkan bütünlük + zorunlu tipleme, değişken kadrolu büyük ekipte **tutarlılık** sağlar; herkesin aynı yolu izlemesi bakım maliyetini düşürür. Aynı özellikler küçük ve hızlı bir ekipte fazla ağır gelebilir.
- **"Yeni projede hangisini seçerdin?"** → "En yenisi" değil: ekip büyüklüğü, kadro devri, kurumsal standart ihtiyacı, işe alım havuzu, mevcut ekosistem ve öğrenme eğrisi tartılır. **Ekibin sürdürebileceği araç kazanır.**
- **"Ön yüzü hiç bilmiyor musun?"** → Doğru çerçeve: "Şu araçla çalıştım; üçü de benim tasarladığım aynı JSON sözleşmesini tüketiyor, karakter farkları şunlar." Sözleşme, CORS ve oturum tarafı zaten senin alanın.

### Tek cümlede

React ince çekirdekli bir kütüphanedir, Angular kutudan bütün çıkan bir çerçevedir, Vue ise ikisinin arasındadır. Üçü de bileşen–durum–bildirimsellik üzerine kuruludur ve üçü de senin JSON'unu tüketir. Dolayısıyla seçim "hangisi daha iyi" sorusu değildir; **ekibinin sürdürebileceği hangisi** sorusudur.

# uclu-1 | Temel
## Soru
Bildirimsel (declarative) arayüz yaklaşımının, jQuery çağındaki emirsel (imperative) yaklaşımdan temel farkı nedir?
## A
Bildirimsel yaklaşımda DOM elemanlarını sırayla bulup metnini ve görünürlüğünü elle güncelleyen adımları yazarsın; fark yalnızca söz diziminin kısalmasıdır.
🤖 Bu tam olarak emirsel yaklaşımın tarifi; bildirimselde o adımları sen yazmazsın.
## B
Durumu tarif edersin ve "bu durumda arayüz şudur" dersin; DOM'a hangi dokunuşun yapılacağına araç karar verir.
🤖 Doğru: sen sonucu tarif edersin, güncelleme adımlarını araç üretir.
## C
Bildirimsel yaklaşım tarayıcıyı devre dışı bırakır; arayüz sunucuda üretilir ve her etkileşimde sayfa baştan yüklenir.
🤖 Bildirimsellik sunucu tarafı render demek değildir; DOM'u kimin güncellediğiyle ilgilidir.
## D
İkisi arasında davranış farkı yoktur; "bildirimsel" sözcüğü modern kütüphanelerin pazarlama adlandırmasından ibarettir.
🤖 Fark gerçektir: güncelleme adımlarını yazma sorumluluğu yer değiştirir.
## Doğru
B
## Açıklama
Emirsel yaklaşımda ekranın doğru görünmesi senin yazdığın adımlara bağlıdır: elemanı bul, metnini değiştir, başkasını gizle, üçüncüsünü işaretle. Her yeni kural eski adımlarla çakışabilir. Bildirimsel yaklaşımda ise yalnızca **durumu** ve "bu durumda arayüz şudur" eşlemesini yazarsın; hangi DOM dokunuşunun yapılacağına araç karar verir. Fark söz dizimi kısalığı değil (A), **sorumluluğun yeridir**. Bildirimsellik arayüzün nerede üretildiğiyle ilgili bir kavram olmadığı için sunucu tarafı render ile eş anlamlı değildir (C) ve gerçek bir davranış farkı olduğu için pazarlama adlandırması da değildir (D). Backend geliştiricisinin ön yüzde en çok kaçırdığı zihinsel sıçrama tam olarak budur.

# uclu-2 | Temel
## Soru
React ile Angular'ı "kütüphane" ve "çerçeve" ayrımı açısından karşılaştıran ifade hangisidir?
## A
React kutudan çıkan resmî yönlendirme, HTTP ve form katmanlarıyla gelen bir çerçevedir; Angular ise yalnız görünüm katmanı sunan ince bir kütüphanedir.
🤖 Roller ters yazılmış: kutudan çıkan bütün Angular'dır, çekirdeği ince olan React.
## B
İkisi de kütüphanedir; yönlendirme ve durum yönetimi için ikisinde de topluluk paketlerinden seçim yapman gerekir.
🤖 Angular yönlendirme, HTTP ve formu resmî olarak sağlar; kütüphane değil çerçevedir.
## C
İkisi de çerçevedir ve resmî bir yönlendirici sunar; aralarındaki tek fark kullandıkları şablon söz diziminde ortaya çıkar.
🤖 React'in resmî bir yönlendiricisi yoktur; fark söz diziminden ibaret değildir.
## D
React çekirdeği ince bir kütüphanedir, çevresini sen seçersin; Angular yönlendirme, HTTP, form ve DI'ı resmî sunan bir çerçevedir.
🤖 Doğru: seçim özgürlüğü ile kutudan çıkan bütünlük ayrımı budur.
## Doğru
D
## Açıklama
Ayrımın özü şudur: **kütüphaneyi sen çağırırsın**, kalan kararlar sende kalır; **çerçeve seni çağırır**, iskeleti ve kuralları o koyar. React kasten küçük bir çekirdek sunar ve esas olarak görünüm katmanını verir; yönlendirme, durum yönetimi, form ve veri çekme için ekosistemden seçim yaparsın — bu hem özgürlük hem **karar yüküdür**. Angular ise yönlendirme, HTTP istemcisi, form yönetimi, **bağımlılık enjeksiyonu** ve test altyapısını resmî olarak kutudan verir, TypeScript'i zorunlu tutar. A şıkkı iki rolü ters çevirir; B Angular'ın resmî katmanlarını yok sayar; C ise React'e olmayan bir resmî yönlendirici atfeder ve farkı yalnızca şablon söz dizimine indirger.

# uclu-3 | Orta
## Soru
Vue'yu React ile Angular arasında konumlandıran karakteristik özellikler hangi seçenekte doğru verilmiştir?
## A
Şablon, betik ve stilin aynı dosyada durduğu bileşen biçimi, ince taneli reaktivite ve resmî yönlendirici/durum paketleri.
🤖 Doğru: tek dosya bileşeni, bağımlılık takibine dayalı reaktivite ve resmî paketler Vue'nun karakteridir.
## B
Zorunlu TypeScript, bağımlılık enjeksiyonu ve kurumsal ölçekte tek yolu dayatan resmî bir çerçeve iskeleti.
🤖 Bu Angular'ın karakteri; Vue TypeScript'i zorunlu tutmaz ve tek yol dayatmaz.
## C
Yalnız görünüm katmanı sunan, yönlendirme ve durum yönetimi için hiçbir resmî paketi bulunmayan çok ince bir çekirdek.
🤖 Bu React'in konumu; Vue'nun resmî yönlendirici ve durum yönetimi paketleri vardır.
## D
Değişikliği ekrana yansıtmak için sayfayı her etkileşimde sunucudan baştan çeken, bileşen durumu tutmayan bir mimari.
🤖 Vue durumu bileşende tutar ve güncellemeyi bağımlılık takibiyle yerel olarak yapar.
## Doğru
A
## Açıklama
Vue'nun imzası **tek dosya bileşenidir (SFC)**: şablon, betik ve stil aynı dosyada toplanır. Reaktivitesi **ince tanelidir** — hangi bileşenin hangi veriyi okuduğu izlenir, veri değişince yalnız ona bağlı parça güncellenir. Composition API mantığı yeniden kullanılabilir parçalara ayırmayı sağlar ve yönlendirici ile durum yönetimi paketleri **resmîdir**: seçmek zorunda değilsin, ama Angular gibi tek bir yola da zorlanmıyorsun. Bu yüzden öğrenme eğrisi üçünün en yumuşağıdır. B şıkkı Angular'ın zorunlu tipleme ve DI karakterini tarif eder; C React'in ince çekirdekli konumunu Vue'ya yakıştırır; D ise bildirimsel bileşen modelinin çalışma biçimini tümüyle yanlış anlatır.

# uclu-4 | Orta
## Soru
Bir mülakatta Spring geliştiricisine "bu üç araçtan hangisi sana en tanıdık gelir, neden?" diye soruluyor. Angular'ı en doğru gerekçelendiren cevap hangisidir?
## A
Angular tarayıcıda doğrudan Java çalıştırdığı için mevcut Spring bilgisi aynen aktarılır ve yeni bir dil öğrenmek gerekmez.
🤖 Tarayıcı Java çalıştırmaz; Angular TypeScript tabanlıdır, tanıdıklık dil değil model benzerliğindendir.
## B
Angular, Spring Boot dağıtımının resmî bir parçasıdır ve bağımlılıklarını aynı sürüm ağacından alır.
🤖 Angular Spring'den bağımsız ayrı bir projedir; ortak bir sürüm yönetimi yoktur.
## C
Angular'ın bağımlılık enjeksiyonu, zorunlu tiplemesi ve kutudan çıkan resmî katmanları Spring'in çalışma modeline benzer.
🤖 Doğru: DI, tip disiplini ve resmî iskelet tanıdıklığın gerçek kaynağıdır.
## D
Angular şablonlarında JPA varlıkları doğrudan kullanılabildiği için veri katmanının ön yüz için yeniden yazılması gerekmez.
🤖 Tarayıcıdaki bileşen JPA varlığı görmez; ağdan gelen JSON'u tüketir.
## Doğru
C
## Açıklama
Angular'ın Spring geliştiricisine tanıdık gelmesinin üç somut nedeni vardır: **bağımlılık enjeksiyonu** ile servislerin dışarıdan verilmesi, **TypeScript'in zorunlu** olmasıyla gelen tip disiplini ve yönlendirme, HTTP, form, test gibi katmanların **resmî olarak kutudan** çıkması — yani "burada işler böyle yapılır" diyen bir iskelet. Bu, Spring'in çalışma modelinin ön yüzdeki karşılığıdır. Tanıdıklık dil ortaklığından gelmez: tarayıcı Java çalıştırmaz (A). Angular Spring'den tümüyle bağımsız bir projedir, ortak sürüm ağacı yoktur (B). Ve bileşenler JPA varlıklarını değil, ağdan gelen **JSON**'u tüketir (D) — veri sözleşmesi zaten senin tasarladığın şeydir.

# uclu-5 | İleri
## Soru
Bir ekip yeni bir ön yüz projesi için React, Vue ve Angular arasında seçim yapacak. Kararı en sağlıklı veren yaklaşım hangisidir?
## A
Sürüm notlarında en çok yenilik duyurulan ve toplulukta en çok konuşulan aracı seçmek; ön yüzde güncellik en belirleyici ölçüttür.
🤖 Moda ve duyuru sıklığı ölçüt değildir; belirleyici olan ekibin sürdürebilmesidir.
## B
Üç aracı da aynı üründe kullanıp her alt ekibin kendi bileşenlerini kendi seçtiği araçla yazmasına izin vermek.
🤖 Karışık kullanım bakım, paket boyutu ve işe alım maliyetini artırır; kararı ertelemektir.
## C
Sentetik kıyaslama (benchmark) testlerinde en hızlı görünen aracı seçmek; ham çizim hızı diğer bütün ölçütleri gölgeler.
🤖 Üçünün performansı tipik uygulamada birbirine yakındır; tek ölçüt bu değildir.
## D
Ekibin bildiği ve sürdürebileceği aracı seçmek; ölçüt ekip büyüklüğü, kurumsal standart ve işe alım havuzudur.
🤖 Doğru: karar bağlam ölçütlerine ve sürdürülebilirliğe dayanır.
## Doğru
D
## Açıklama
Üç aracın da tipik bir kurumsal uygulamadaki performansı birbirine yakındır; bu yüzden kararı **bağlam ölçütleri** verir: ekip büyüklüğü ve kadro devri, kurumda tek bir standart dayatma ihtiyacı, bölgedeki işe alım havuzu, elinizdeki bileşen kütüphanesi/tasarım sistemiyle uyum, öğrenme eğrisini kaldırma kapasitesi ve "kararı araç versin" mi yoksa "seçim özgürlüğü isterim" mi tercihi. Büyük ve değişken kadrolu kurumsal ekipte Angular'ın kutudan çıkan bütünlüğü tutarlılık kazandırır; küçük ve hızlı bir ekipte Vue'nun yumuşak eğrisi veya React'in geniş ekosistemi öne çıkar. Sürüm notu ve popülerlik (A) mühendislik ölçütü değildir; üç aracı aynı üründe karıştırmak (B) bakım ve işe alım maliyetini artırıp kararı erteler; sentetik kıyaslama (C) ise tek başına belirleyici olamaz. İlke: **teknoloji seçimini ekibin bildiği ve sürdürebileceği şey belirler, moda değil.**

# uclu-6 | İleri
## Soru
Backend geliştiricisine "bu üçünden hangisini biliyorsun?" diye sorulduğunda en güçlü ve dürüst cevap çerçevesi nedir?
## A
"Üçünü de ileri düzeyde biliyorum" deyip ayrıntı sorulduğunda konuyu hızla kendi API tasarımına çevirmek.
🤖 Abartılmış iddia ilk teknik soruda çöker; dürüst çerçeve her zaman daha güçlüdür.
## B
Hangisiyle çalıştığını dürüstçe söylemek ve üçünün de aynı JSON sözleşmesini tükettiğini, farklarının nerede olduğunu anlatmak.
🤖 Doğru: sözleşme sahipliği + karakter farkı bilgisi backend'in gerçek konumudur.
## C
"Ön yüz benim işim değil" deyip soruyu kapatmak ve backend rolünde bu konunun sorulmaması gerektiğini belirtmek.
🤖 CORS, oturum ve API sözleşmesi gibi sınır konuları doğrudan backend'i ilgilendirir.
## D
Üç aracın en güncel sürüm numaralarını ve yayın takvimlerini sayarak konuya geniş hâkim olduğunu göstermek.
🤖 Sürüm numarası ezberi, araçların karakter farkını bilmenin yerini tutmaz.
## Doğru
B
## Açıklama
Senin gerçek konumun şudur: üç araç da sonuçta **senin tasarladığın JSON sözleşmesini tüketir**; API'n framework'ten bağımsızdır, değişen yalnız istemcideki bileşen kodudur. Bu yüzden en güçlü cevap hangisiyle çalıştığını açıkça söylemek, sözleşmenin sahibi olduğunu ortaya koymak ve üçünün karakter farkını (React ince çekirdekli kütüphane, Vue arada, Angular kutudan çıkan çerçeve) özetlemektir. Ayrıca **CORS, oturum/çerez, kimlik doğrulama, sayfalama ve hata gövdesi biçimi** gibi sınır konularının sorumlusu zaten sensin. Abartılı bilgi iddiası (A) ilk teknik soruda çöker ve güveni bitirir; soruyu tümden reddetmek (C) sınır konularını da reddetmiş göründüğü için zayıftır; sürüm numarası saymak (D) ise karakter farkını bilmenin yerini tutmaz.
