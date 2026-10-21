---
slug: serialization
baslik: "Serialization: Serializable, transient ve Güvenlik"
kademe: İleri
sira: 43
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir program çalışırken nesneleri bellekte (RAM) yaşar; program kapanınca hepsi silinir. Peki bir nesneyi yarın aynı hâliyle geri istiyorsan ya da başka bir bilgisayara göndermek istiyorsan? Nesneyi, sökülüp kutuya konabilen bir mobilya gibi düşün: parçalarını belli bir sırayla kutuya koyar, kargoya verirsin; karşı taraf aynı sırayla açıp yeniden monte eder. Serileştirme (serialization) budur — nesnenin o anki durumunu diske yazılabilir, ağdan gönderilebilir bir bayt dizisine çevirmek. Ters yön, yani kutudan mobilyayı yeniden kurmak, seri-çözmedir (deserialization). Java bunu kutudan çıkma biçimde sunar; ama bu kolaylığın ağır bir bedeli vardır.

### Temel kavramlar

- **Nesne durumu (state)**: nesnenin alanlarında o an duran değerler (`ad = "Ada"`, `yas = 20`). Serileştirme davranışı değil, bu değerleri taşır.
- **Bayt (byte)**: 8 bitlik en küçük saklama birimi. Diskte ve ağda her şey bayt dizisidir.
- **Bayt akışı (byte stream)**: baytların sırayla aktığı boru; yazma ucu `OutputStream`, okuma ucu `InputStream`.
- **İşaretçi arayüzü (marker interface)**: içinde hiç metot olmayan arayüz. `java.io.Serializable` böyledir — "bu sınıfın nesneleri yerel serileştirmeye uygundur" iznini işaretler, kendisi hiçbir iş yapmaz. İşaretlenmemiş bir nesneyi yazmaya kalkarsan `NotSerializableException` alırsın.
- **`ObjectOutputStream` / `ObjectInputStream`**: asıl işi yapan sınıflar; `writeObject(nesne)` yazar, `readObject()` okur. Mantık sınıfın içinde değil, bu akış altyapısındadır.
- **`serialVersionUID`**: sınıfın sürüm kimliği; `long` bir sayıdır ve baytların içine de yazılır.
- **`transient`**: "bu alanı akışa yazma" diyen alan işaretçisi.
- **İkili (binary) biçim**: çıktı okunabilir metin (JSON) değil, yalnızca JVM'in anladığı ikili bir düzendir.

```java
class Kullanici implements Serializable {      // metotsuz işaret
    private static final long serialVersionUID = 1L;
    String ad = "Ada";
    transient String parola = "gizli";         // akışa yazılmaz
}
// yazıp geri okuduktan sonra:  ad = "Ada"   parola = null
```

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Serileştirme akışı ve transient alan">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Nesne → bayt akışı → nesne</text>
  <rect x="30" y="52" width="150" height="110" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="105" y="78" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Nesne</text>
  <text x="105" y="102" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ad = "Ada"</text>
  <text x="105" y="122" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">parola (transient)</text>
  <text x="105" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">writeObject()</text>
  <rect x="245" y="52" width="150" height="110" rx="12" fill="#EEF3FA" stroke="#2C5B8A" stroke-width="1.5"/>
  <text x="320" y="78" text-anchor="middle" font-size="13" font-weight="700" fill="#2C5B8A" font-family="system-ui">Bayt akışı</text>
  <text x="320" y="102" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ad yazıldı</text>
  <text x="320" y="122" text-anchor="middle" font-size="11" fill="#B4322B" font-family="system-ui">parola YOK</text>
  <text x="320" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ serialVersionUID</text>
  <rect x="460" y="52" width="150" height="110" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="535" y="78" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Nesne</text>
  <text x="535" y="102" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ad = "Ada"</text>
  <text x="535" y="122" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">parola = null</text>
  <text x="535" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">readObject()</text>
  <path d="M182 107 L243 107" stroke="#5D7789" stroke-width="1.5" marker-end="url(#ser-ok)"/>
  <path d="M397 107 L458 107" stroke="#5D7789" stroke-width="1.5" marker-end="url(#ser-ok)"/>
  <defs>
    <marker id="ser-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="198" text-anchor="middle" font-size="12" font-weight="600" fill="#B4322B" font-family="system-ui">Güvenilmeyen baytı readObject ile çözme.</text>
</svg>
```

### Nasıl çalışır?

`writeObject` çağrıldığında akışa sırayla şunlar gider: sınıfın adı, `serialVersionUID` değeri, alan tanımları ve `transient` olmayan alanların değerleri. Bir alan başka bir nesneyse o da aynı yöntemle özyinelemeli (recursive) yazılır; böylece iç içe bütün nesne grafiği taşınır. Java'nın yerel biçimi, aynı nesneye giden birden çok referansı ve döngüleri (A → B → A) bir referans tablosuyla kayıpsız korur — JSON/protobuf'ta bu iş elle çözülmek zorundadır.

Okuma tarafı daha ilginçtir. `readObject` önce sınıf tanımını ve UID'yi okur; akıştaki UID ile sınıftaki UID uyuşmazsa `InvalidClassException` fırlatır. Uyuşuyorsa nesneye bellek ayrılır, ama **serileştirilebilir sınıfın kendi yapıcısı çalışmaz**: JVM yalnızca en yakın serileştirilemeyen üst sınıfın parametresiz yapıcısını çağırır, sonra alanları doğrudan baytlardan doldurur. `transient` alanlar için akışta veri yoktur; onlar tipin varsayılanında kalır — nesne `null`, `int` `0`, `boolean` `false`. Yapıcıda verdiğin başlangıç değerleri bu yüzden geçerli olmaz; gerekirse `readObject` içinde elle doldurursun.

UID'yi elle yazmazsan derleyici onu alan, metot ve arayüz yapısından **belirlenimci** (deterministic — rastgele değil) biçimde türetir. Sorun, bu değerin sınıf yapısına aşırı duyarlı olmasıdır: tek bir alan eklemek bile UID'yi değiştirir ve eski baytlar okunamaz olur. Bu yüzden serileştirilebilir her sınıfa açık ve sabit bir `private static final long serialVersionUID` vermek standarttır — sürüm kararını derleyici değil geliştirici verir.

Varsayılan alan-alan davranışını beğenmezsen iki kanca (hook) vardır. Birincisi, sınıfa özel `private void writeObject/readObject` metotlarıdır; bunlar varsayılanı özelleştirir. İkincisi `Externalizable` arayüzüdür: `writeExternal(ObjectOutput)`/`readExternal(ObjectInput)` uygulamayı **zorunlu** kılar. Böylece her alanı elle yazma sorumluluğu tümüyle sana geçer; seri-çözmede de sınıfın **açık parametresiz yapıcısı çağrılır**.

### Mülakatta ne sorulur?

- **`serialVersionUID` vermezsem ne olur?** Kod sorunsuz derlenir (zorunlu değil, önerilir); UID türetilir, sınıf değişince sessizce değişir ve eski veriyle `InvalidClassException` çıkar. Sürüm kontrolü devre dışı kalmaz, değer `0` da kabul edilmez.
- **Asıl güvenlik açığı**: `readObject` yalnız veri okumaz; akışta adı geçen sınıfların **kodunu** (özel `readObject`, alan atamaları, yapıcı yan etkileri) çalıştırır. Saldırgan, sınıf yolundaki (classpath) zararsız görünen sınıfları zincirleyerek (**gadget zinciri**) uzaktan kod çalıştırabilir (RCE). "Ben sadece `Kullanici` bekliyorum" korumaz: çözme, tip kontrolünden ÖNCE olur. Risk gizlilik ya da disk doluluğu değil, kod yürütmedir. Kural: güvenilmeyen baytı yerel serileştirmeyle asla çözme. Zorunluysa Java 9'la gelen `ObjectInputFilter` (JEP 290) ile izin listesi (allowlist) tanımla. Bu filtre kabul edilen sınıf adlarını, en büyük dizi boyutunu ve referans derinliğini sınırlar; listede olmayan sınıf çözülmeden reddedilir.
- **`Externalizable` farkı**: `writeExternal`/`readExternal` zorunludur. Yalnız ilkel (primitive) alan kısıtı yoktur. Disk mi ağ mı ayrımıyla da ilgisi yoktur; ikisi de aynı `ObjectOutput`/`ObjectInput` üzerinde çalışır. `serialVersionUID` ihtiyacını da kaldırmaz; tersine sürüm sorumluluğunu tümüyle sana yükler. Alan eklenip çıkarıldığında metotları elle güncellemezsen veri bozulur.
- **`transient` ne için?** Parola, oturum belirteci (token), önbellek ve kolayca yeniden hesaplanan türetilmiş alanlar.
- **Neden JSON/protobuf?** (1) birlikte çalışabilirlik: Java baytını bir Go/Python servisi okuyamaz; (2) kırılganlık: biçim sınıf yapısına bağlıdır; (3) güvenlik: `readObject` bir RCE yüzeyidir. protobuf/Avro **ikilidir** ve aynı veri için genelde JSON'dan çok daha kompakttır; alan numaralarıyla geriye/ileriye uyumlu şema evrimi sağlar. Java serileştirmesi Java 9 modül sisteminde kaldırılmadı, hâlâ çalışır — sorun destek değil, tasarımdır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="serialVersionUID uyuşmazlığı InvalidClassException hatası">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">serialVersionUID uyuşmazlığı</text>
  <rect x="30" y="48" width="250" height="110" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="155" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Yazılan bayt (eski)</text>
  <text x="155" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">serialVersionUID = 1L</text>
  <text x="155" y="122" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ad, soyad alanları</text>
  <rect x="360" y="48" width="250" height="110" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="485" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Sınıf (yeni sürüm)</text>
  <text x="485" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">türetilmiş UID = 7d2f...</text>
  <text x="485" y="122" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">+ yeni alan eklendi</text>
  <path d="M282 103 L358 103" stroke="#8A2E2E" stroke-width="2" marker-end="url(#ser2-x)"/>
  <defs>
    <marker id="ser2-x" markerWidth="9" markerHeight="9" refX="7" refY="3.5" orient="auto">
      <path d="M0 0 L7 3.5 L0 7 z" fill="#8A2E2E"/>
    </marker>
  </defs>
  <text x="320" y="182" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">UID uyuşmazsa → InvalidClassException, eski veri okunamaz.</text>
</svg>
```

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Java yerel serileştirme, JSON ve protobuf karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Veri değişimi: hangi format?</text>
  <rect x="24" y="44" width="190" height="140" rx="12" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="119" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Java yerel</text>
  <text x="119" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yalnız JVM okur</text>
  <text x="119" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sınıfa kırılgan bağlı</text>
  <text x="119" y="134" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">RCE riski (readObject)</text>
  <text x="119" y="162" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">kaçın</text>
  <rect x="226" y="44" width="188" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">JSON</text>
  <text x="320" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">dilden bağımsız</text>
  <text x="320" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">okunur, metin</text>
  <text x="320" y="134" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">şema gevşek</text>
  <text x="320" y="162" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">servis API'leri</text>
  <rect x="426" y="44" width="190" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="521" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">protobuf / Avro</text>
  <text x="521" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kompakt ikili</text>
  <text x="521" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">şema-evrimli</text>
  <text x="521" y="134" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hızlı</text>
  <text x="521" y="162" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">yüksek hacim</text>
</svg>
```

### Tek cümlede

Serileştirme nesneyi bayta çevirir; `serialVersionUID` sürümü, `transient` neyin dışarıda kalacağını yönetir, `readObject` ise güvenilmeyen veriyle kod çalıştırma kapısıdır — bu yüzden modern sistemler veri değişiminde JSON/protobuf'u tercih eder.

# ser-1 | Temel
## Soru
`java.io.Serializable` arayüzünün rolü nedir?
## A
Sınıfın, serileştirme mantığını içeren bir `serialize()` metodunu uygulamasını zorunlu kılar
🤖 `Serializable`'ın hiç metodu yoktur; mantığı JVM/`ObjectOutputStream` yürütür.
## B
Metotsuz bir işaretçi arayüzüdür (marker interface); sınıfın yerel serileştirmeye uygun olduğunu JVM'e bildirir
🤖 Doğru: işaretçi arayüzü, davranışı değil "yetkiyi" işaretler.
## C
Sınıfın tüm alanlarını varsayılan olarak `transient` yapar; yazılmasını istediğin alanları ayrıca işaretlersin
🤖 Tam tersi; varsayılan yazılmaktır, `transient` işaretlenenler dışarıda kalır.
## D
Nesneyi otomatik olarak JSON metnine çevirip akışa yazar, böylece çıktı dilden bağımsız olur
🤖 Yerel serileştirmenin çıktısı JVM'e özgü ikili (binary) biçimdir, JSON değil.
## Doğru
B
## Açıklama
`Serializable` bir **işaretçi arayüzüdür** (marker interface): içinde metot yoktur, yalnızca sınıfın Java yerel serileştirme mekanizmasına uygun olduğunu işaretler. Nesne `ObjectOutputStream.writeObject()` ile yazılırken JVM, sınıfın işaretlenip işaretlenmediğine bakar; işaretli değilse `NotSerializableException` fırlatır. Serileştirme mantığını sınıf değil, akış (stream) altyapısı yürütür (A yanlış). `transient` işaretlemesi ayrı bir karardır (C yanlış) ve çıktı JSON değil, ikili bir bayt akışıdır (D yanlış).

# ser-2 | Orta
## Soru
`serialVersionUID` alanını hiç tanımlamazsan ne olur?
## A
Sınıf serileştirilemez sayılır ve `Serializable` uygulaması derleme hatası verir
🤖 Derleme sorunsuz geçer; UID zorunlu değil, yalnızca önerilir.
## B
Her seri-çözme işleminde rastgele bir UID üretilir, bu yüzden karşılaştırma hep başarısız olur
🤖 Rastgele değil; sınıf yapısından belirlenimci (deterministic) olarak türetilir.
## C
UID her zaman `0` kabul edilir ve sınıf sürümü kontrolü tümüyle devre dışı kalır
🤖 `0` değil; sürüm kontrolü yine yapılır, sadece değer otomatik türetilir.
## D
Derleyici sınıf yapısından bir UID türetir; sınıf değişince bu değer sessizce değişip eski verilerle `InvalidClassException`'a yol açabilir
🤖 Doğru: türetilmiş UID kırılgandır, alan eklemek bile eski veriyi okunmaz yapar.
## Doğru
D
## Açıklama
`serialVersionUID` elle verilmezse, derleyici sınıfın alanları, metotları ve arayüzlerinden **belirlenimci bir değer türetir**. Sorun şu: bu türetilmiş değer sınıfın yapısına aşırı duyarlıdır — yeni bir alan eklemek, bir metodun imzasını değiştirmek bile UID'yi değiştirir. Eski baytlarla yeni sınıf uyuşmazsa seri-çözme `InvalidClassException` ile başarısız olur. Bu yüzden serileştirilebilir her sınıfa **açık ve sabit** bir `private static final long serialVersionUID` vermek en iyi pratiktir; böylece sürüm uyumluluğunu geliştirici kontrol eder.

```java
public class Kullanici implements Serializable {
    private static final long serialVersionUID = 1L; // açık ve sabit
    private String ad;
}
```

# ser-3 | Orta
## Soru
`transient` ile işaretlenmiş bir alan seri-çözme sonrası hangi değeri alır?
## A
Tipin varsayılan değerini (nesne için `null`, `int` için `0`, `boolean` için `false`)
🤖 Doğru: transient alan yazılmadığı için seri-çözünce varsayılana döner.
## B
Serileştirme öncesindeki son değerini korur
🤖 Değer akışa hiç yazılmadı; korunacak bir şey yok.
## C
Sınıfın yapıcısında (constructor) o alana atanan değeri alır
🤖 Seri-çözmede serileştirilebilir sınıfın yapıcısı hiç çalışmaz; alan varsayılan kalır.
## D
Rastgele bir değer alır
🤖 Rastgelelik yok; belirlenimci olarak tip varsayılanıdır.
## Doğru
A
## Açıklama
`transient`, alanın serileştirme dışında tutulmasını söyler: `writeObject` o alanı akışa yazmaz. Seri-çözmede (`readObject`) alan için akışta veri olmadığından JVM alanı **tipin varsayılan değerine** bırakır — referanslar `null`, `int` `0`, `boolean` `false`. Önemli bir ince nokta: yerel seri-çözme sırasında serileştirilebilir sınıfın **kendi yapıcısı çalışmaz** — JVM yalnızca en yakın serileştirilemeyen üst sınıfın parametresiz yapıcısını çağırır — bu yüzden yapıcıdaki atamalar geçerli olmaz (C yanlış). Parola, oturum belirteci (token), önbellek veya kolayca yeniden hesaplanan türetilmiş alanlar `transient` yapılır; gerekirse `readObject` içinde elle yeniden doldurulur.

# ser-4 | İleri
## Soru
Java yerel serileştirmesinde en kritik güvenlik açığı hangisidir?
## A
Serileştirilen baytlar okunabilir metin olduğu için parolalar ve gizli alanlar düz metin sızar
🤖 Çıktı ikili biçimdedir; asıl risk gizlilik değil kod çalıştırmadır.
## B
Serileştirme çok yer kapladığı için disk dolar (kaynak tükenmesi)
🤖 Boyut bir dezavantajdır ama "en kritik güvenlik açığı" değildir.
## C
`readObject`, akıştaki sınıfların kodunu çalıştırabilir; güvenilmeyen baytlar "gadget zinciriyle" RCE'ye dönüşebilir
🤖 Doğru: seri-çözme bir kod tetikleme yüzeyidir, tip beklentin seni korumaz.
## D
Seri-çözme her zaman `null` döndürdüğü için veri kaybolur
🤖 Böyle bir davranış yok; risk veri kaybı değil, kötücül kod yürütmedir.
## Doğru
C
## Açıklama
`ObjectInputStream.readObject()` yalnızca veri okumaz; akışta belirtilen sınıfların **kodunu** (özel `readObject`, alan atamaları, yapıcı yan etkileri) çalıştırır. Saldırgan, sınıf yolunda (classpath) bulunan zararsız görünen sınıfları zincirleyerek (**gadget chain**) seri-çözme sırasında istediği kodu tetikleyebilir — sonuç uzaktan kod çalıştırma (RCE). "Ben sadece `Kullanici` bekliyorum" demek korumaz; çözme, tip kontrolünden ÖNCE olur. Savunma: güvenilmeyen kaynaktan yerel serileştirme kullanmamak; zorunluysa `ObjectInputFilter` ile izin listesi (allowlist) uygulamak. En sağlam çözüm, veri değişimi için Java serileştirmesi yerine JSON/protobuf gibi **veri-odaklı** formatlar kullanmaktır.

# ser-5 | İleri
## Soru
`Externalizable`, `Serializable`'dan nasıl ayrılır?
## A
`Externalizable` yalnızca ilkel (primitive) alanları destekler; nesne referansları yazılamaz
🤖 Böyle bir kısıt yok; `writeObject` ile her tür alanı elle yazabilirsin.
## B
`Externalizable`, `writeExternal`/`readExternal` metotlarını uygulamanı zorunlu kılar; serileştirilecek her alanı SEN elle yazarsın
🤖 Doğru: kontrol ve performans sende; karşılığında bakım yükü artar.
## C
`Externalizable`, `serialVersionUID` ihtiyacını ortadan kaldırır ve sürüm uyumluluğunu kendisi otomatik çözer
🤖 Tam tersine sürüm sorumluluğu tümüyle sana geçer; otomatik bir çözüm yoktur.
## D
`Externalizable` yalnızca ağ üzerinden aktarımda, `Serializable` yalnızca diske yazmada kullanılabilir
🤖 İkisi de aynı `ObjectOutput`/`ObjectInput` üzerinde çalışır; hedefin ne olduğuyla ilgisi yoktur.
## Doğru
B
## Açıklama
`Externalizable`, `Serializable`'ı genişletir ama iki metodu — `writeExternal(ObjectOutput)` ve `readExternal(ObjectInput)` — uygulamanı **zorunlu** kılar. Varsayılan alan-alan otomatik serileştirmenin aksine, hangi alanın hangi sırayla yazılıp okunacağına tamamen SEN karar verirsin. Avantajı: daha kompakt/hızlı biçim ve tam kontrol. Bedeli: alan eklendiğinde/çıkarıldığında `read`/`write` metotlarını elle güncellemek gerekir; unutulursa veri bozulur. Ayrıca seri-çözmede **açık no-arg yapıcı** çağrılır (Serializable'da çağrılmaz). Pratikte bu bakım yükü nedeniyle çoğu ekip ya varsayılan Serializable'ı ya da tümüyle harici formatları (JSON/protobuf) seçer.

# ser-6 | Uzman
## Soru
Servisler arası veri değişiminde Java yerel serileştirmesi yerine neden genellikle JSON veya protobuf tercih edilir?
## A
Dilden bağımsız, şema-evrimine dayanıklı ve güvenlik açısından daha güvenli oldukları için; Java serileştirmesi ise sınıf yapısına kırılgan biçimde bağlı, yavaş ve RCE riski taşır
🤖 Doğru: birlikte çalışabilirlik + evrim + güvenlik üçlüsü belirleyici.
## B
Metin tabanlı olduğu için JSON her zaman protobuf'tan daha küçük çıktı ürettiği ve bant genişliğini azalttığı için
🤖 Yanlış; protobuf ikilidir ve aynı veri için genelde JSON'dan çok daha kompakttır.
## C
Java yerel serileştirmesi Java 9 modül sisteminde kaldırıldığı ve yeni sürümlerde artık çalışmadığı için
🤖 Hâlâ desteklenir ve çalışır; sorun destek değil, tasarımdaki dezavantajlarıdır.
## D
JSON ve protobuf, nesne referans döngülerini (cyclic references) ve paylaşılan referansları otomatik ve kayıpsız koruduğu için
🤖 Tam tersi; döngüleri asıl kayıpsız koruyan Java yerel biçimidir, JSON/protobuf'ta özel işlem gerekir.
## Doğru
A
## Açıklama
Java yerel serileştirmesinin üç yapısal sorunu vardır: (1) **birlikte çalışabilirlik yok** — baytları yalnız JVM okur, bir Go/Python servisi çözemez; (2) **kırılganlık** — biçim sınıf yapısına bağlı, `serialVersionUID` ve alan değişiklikleri sürüm uyumluluğunu bozar; (3) **güvenlik** — `readObject` gadget zincirleriyle RCE yüzeyidir. JSON dilden bağımsız ve okunurdur; protobuf/Avro ise **şema tabanlıdır**: alan numaralarıyla geriye/ileriye uyumlu evrim sağlar, kompakt ikili biçim ve yüksek hız sunar. Bu yüzden mikroservis iletişiminde ve kalıcı veri formatlarında yerel serileştirme neredeyse hiç kullanılmaz; tercih veri-odaklı, şema-yönetimli formatlardan yana yapılır.
