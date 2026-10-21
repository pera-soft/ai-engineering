---
slug: sozlu-prova-java-cekirdegi
baslik: "Sözlü Prova · Java Çekirdeği: JVM Belleği, Eşzamanlılık Tercihleri ve N+1"
kademe: İleri
sira: 262
etiketler: 11
---

## Konu anlatımı

Bu dosyada üç gerçek mülakat sorusu var. Üçü de açık uçlu; şık yok, anlatım var. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı. Senin adına deneyim uydurulmaz; onu kendi geçmişinden çıkarman için sorular sorulur.

### "JVM bellek yönetimi, heap ve stack farkı nedir; yaşadığınız bir performans sorunuyla anlatır mısınız?"

**Soru aslında ne ölçüyor?**

Soru iki parçalı ve ağırlık ikinci parçadadır. Birinci parça tanım ister; bunu kitaptan okuyan herkes verir. İkinci parça deneyim ister; bunu yalnız yaşamış olan verir. Görüşmecinin asıl aradığı şey şudur: bu aday tahminle mi çalışıyor, ölçerek mi. Bu yüzden hikâyenin içinde **ölçüm** geçmezse cevap yarım sayılır. Tanım kısmı seni elemez, ama seçtirmez de.

**Sözlü cevap iskeleti**

1. **Ayrımı tek cümlede ver.** Yığın (heap) nesneleri tutar, yığıt (stack) metot çerçevelerini ve yerel değişkenleri tutar. Uzatma; tek cümle yeter.
2. **Sahipliği söyle.** Her iş parçacığının (thread) kendi yığıtı vardır, yığın hepsi tarafından paylaşılır. Bu cümle çok iş parçacıklı düşündüğünü gösterir.
3. **Hata ayrımını ver.** Çağrı derinliği biterse `StackOverflowError` gelir, yığında yer kalmazsa `OutOfMemoryError` gelir. Sınıf üstverisi ayrı bir alanda, Metaspace'te tutulur (`59`, `60`).
4. **Çöp toplamayı tek cümlede geç.** Varsayım şudur: nesnelerin çoğu genç ölür. Genç kuşak sık ve ucuz toplanır, yaşlı kuşak seyrek ve pahalı (`61`, `62`). Ayrıntıya girme; görüşmeci merak ederse sorar.
5. **Geçiş cümlesini kur.** "Bunun üretimde bana dokunduğu yer şuydu" diyerek hikâyeye geç. Adayların çoğu bu geçişi yapamaz ve tanımda dönüp durur.
6. **Hikâyeyi beş parçada anlat.** Belirti, ölçüm, kök neden, düzeltme, ölçülen sonuç. Ölçüm parçasını atlarsan cevap bir iddia olarak kalır (`64`, `65`).

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JVM bellek yerlesimi: is parcacigi basina yigit, paylasilan yigin, metaspace ve iki farkli hata">
  <defs>
    <marker id="sozlu-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yığıt iş parçacığı başına ayrılır, yığın paylaşılır</text>
  <rect x="14" y="36" width="196" height="98" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="55" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İş parçacığı A · yığıt</text>
  <rect x="26" y="63" width="172" height="21" rx="4" fill="#FFFFFF" stroke="#5D7789" stroke-width="1"/>
  <text x="112" y="77" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">çerçeve: hesapla · yerel değişkenler</text>
  <rect x="26" y="88" width="172" height="21" rx="4" fill="#FFFFFF" stroke="#5D7789" stroke-width="1"/>
  <text x="112" y="102" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">çerçeve: servis · nesne başvuruları</text>
  <text x="112" y="125" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">çerçeveler üst üste yığılır</text>
  <rect x="14" y="142" width="196" height="98" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="161" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İş parçacığı B · yığıt</text>
  <rect x="26" y="169" width="172" height="21" rx="4" fill="#FFFFFF" stroke="#5D7789" stroke-width="1"/>
  <text x="112" y="183" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kendi çerçeveleri · ayrı alan</text>
  <text x="112" y="207" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">A ile B birbirinin</text>
  <text x="112" y="222" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yığıtını göremez</text>
  <rect x="14" y="248" width="196" height="42" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="112" y="266" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">derinlik biterse</text>
  <text x="112" y="282" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">StackOverflowError</text>
  <line x1="212" y1="74" x2="238" y2="86" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu-ok)"/>
  <line x1="212" y1="180" x2="238" y2="150" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu-ok)"/>
  <rect x="242" y="36" width="222" height="204" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="353" y="55" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Yığın · tüm iş parçacıkları paylaşır</text>
  <rect x="258" y="66" width="190" height="26" rx="5" fill="#FFFFFF" stroke="#25804C" stroke-width="1"/>
  <text x="353" y="83" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">nesneler · diziler · koleksiyonlar</text>
  <rect x="258" y="98" width="190" height="26" rx="5" fill="#FFFFFF" stroke="#25804C" stroke-width="1"/>
  <text x="353" y="115" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">genç kuşak · sık ve ucuz toplanır</text>
  <rect x="258" y="130" width="190" height="26" rx="5" fill="#FFFFFF" stroke="#25804C" stroke-width="1"/>
  <text x="353" y="147" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yaşlı kuşak · seyrek ve pahalı</text>
  <text x="353" y="176" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yığıtta başvuru durur,</text>
  <text x="353" y="191" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">nesnenin kendisi burada</text>
  <rect x="258" y="200" width="190" height="30" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="353" y="219" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">yer kalmazsa: OutOfMemoryError</text>
  <rect x="476" y="36" width="170" height="86" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="561" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Metaspace</text>
  <text x="561" y="76" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sınıf üstverisi</text>
  <text x="561" y="92" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yığından ayrı alan</text>
  <text x="561" y="110" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kendi sınırı vardır</text>
  <rect x="476" y="130" width="170" height="160" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="561" y="150" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Hikâyenin beş parçası</text>
  <text x="561" y="172" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">1 · belirti</text>
  <text x="561" y="192" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">2 · ölçüm</text>
  <text x="561" y="212" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">3 · kök neden</text>
  <text x="561" y="232" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">4 · düzeltme</text>
  <text x="561" y="252" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">5 · ölçülen sonuç</text>
  <text x="561" y="274" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">2 ve 5 atlanırsa cevap yarım</text>
</svg>
```

**Elenme sebepleri**

- "Yığın nesneler için, yığıt ilkel tipler içindir." Yanlış; nesnenin içindeki ilkel alan da yığında durur. Ezber okunur.
- İki hatayı karıştırmak. `StackOverflowError` derinlik sorunu, `OutOfMemoryError` alan sorunudur; karıştıran aday temel ayrımı bilmiyor demektir.
- "Bellek yetmiyordu, `-Xmx` değerini büyüttük." Kök neden aranmamış, belirti ötelenmiş görünür.
- Ölçüm anlatmamak. "Sanırım şu yüzdendi" cümlesi hikâyeyi tahmine çevirir.
- Sonucu söylememek. Düzeltmeyi anlatıp neyin ne kadar değiştiğini söylememek en sık kaçırılan puandır.
- Süreyi çöp toplama algoritmalarını sayarak doldurmak. Soru deneyim istedi, ders istemedi.
- "Sızıntı olmasın diye `System.gc()` çağırıyoruz." Bu cümle tek başına eleyicidir.

**Kendi hikâyen**

Buraya kendi vakan gelecek. Uydurma; aşağıdaki sorular seni kendi geçmişine götürsün:

- Hangi uygulamada bellek konuşuldu? Düzenli yeniden başlatmayla ayakta tutulan bir servis var mıydı?
- Belirti neydi? Yavaşlama mı, tekrarlayan yeniden başlatma mı, kabın `OOMKilled` ile ölmesi mi?
- Nasıl fark edildi? Alarm mı, kullanıcı şikâyeti mi, gösterge panosunda tırmanan çizgi mi?
- Nasıl ölçtün? Yığın dökümü (heap dump) aldın mı, GC logunu okudun mu, profilleyici (profiler) bağladın mı?
- Baskın nesne neydi? Büyüyen önbellek, kapanmayan kaynak, temizlenmeyen `ThreadLocal`, tek seferde çekilen büyük sonuç kümesi?
- Ne değiştirdin? Sorguyu sayfaladın mı, önbelleğe sınır koydun mu, veriyi akış hâline mi getirdin?
- Ne ölçüldü? Tepe bellek, yeniden başlatma sıklığı, duraklama süresi, yanıt süresi. Bir sayı seç ve cümlenin sonunda onu söyle.

Bu deneyim yoksa uydurma. `256`'daki dürüst köprüyü kur: sınırı söyle, komşu deneyimini somutla, farkı say, kapatma planını ver. Yerelde bir yığın dökümü alıp incelemiş olmak da gerçek bir cevaptır. Yeter ki onu üretim vakası gibi anlatma.

### "synchronized, Lock ve ExecutorService ne zaman tercih edilir; bir yarış koşulunu nasıl çözdünüz?"

**Soru aslında ne ölçüyor?**

Bu soru bir tuzak taşır, ama tuzağı görüşmeci kurmamıştır. Üç isim yan yana konmuştur; oysa üçü aynı kategoride değildir. Bunu ilk cümlede söyleyen aday hemen ayrışır. Ağırlık burada muhakemededir. İkinci yarıda ise yine deneyim aranır ve şu ölçülür: paylaşılan durumu görebiliyor musun.

**Sözlü cevap iskeleti**

1. **Kategori ayrımını en başta söyle.** `synchronized` ve `Lock` karşılıklı dışlama (mutual exclusion) araçlarıdır. `ExecutorService` ise görev çalıştırma modelidir. İlk ikisi "kim girsin", üçüncüsü "iş nerede koşsun" sorusunu cevaplar.
2. **`synchronized` ne zaman yeter, söyle.** Kritik bölge kısa, tek kilit var, özel bir ihtiyaç yok. Dile gömülüdür ve blok bitince kilit kendiliğinden bırakılır (`46`). Varsayılan tercih budur.
3. **`ReentrantLock` neyi ekliyor, say.** Süre sınırlı deneme (`tryLock`), kesilebilir bekleme, adil kilit seçeneği, birden fazla koşul kuyruğu. Bedeli de var: kilidi `finally` içinde bırakmak senin sorumluluğun (`47`).
4. **Bir güncel not düş.** Sanal iş parçacıklarında (virtual thread) `synchronized` içindeki bloklayan çağrı taşıyıcıyı sabitleyebilir. Bu ortamda genellikle `ReentrantLock` önerilir (`56`).
5. **`ExecutorService`'i ayrı anlat.** İş parçacığını elle üretmek yerine havuz ve kuyruk kullanırsın. Kararlar sende: havuz boyu, kuyruk türü, reddetme politikası, düzgün kapanış (`49`).
6. **En iyi kilidin kilitsizlik olduğunu ekle.** Paylaşımı kaldırabiliyorsan kaldır: atomik sınıflar, eşzamanlı koleksiyonlar, değişmez nesneler (`48`, `51`). Sonra vakaya geç: paylaşılan durum, belirti, doğrulama, çözüm, kanıt.

```svg
<svg viewBox="0 0 660 288" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="synchronized ve Lock karsilikli dislama araci, ExecutorService gorev calistirma modeli">
  <defs>
    <marker id="sozlu-kat" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üçü aynı kategoride değil — soru ilk cümlede ayrışır</text>
  <rect x="14" y="36" width="306" height="164" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="167" y="56" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Karşılıklı dışlama · "kim girsin?"</text>
  <rect x="28" y="66" width="136" height="122" rx="8" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="96" y="84" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">synchronized</text>
  <text x="96" y="102" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dile gömülü</text>
  <text x="96" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">blok bitince bırakılır</text>
  <text x="96" y="136" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kısa kritik bölge</text>
  <text x="96" y="156" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">varsayılan tercih</text>
  <text x="96" y="176" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">sanal thread'te sabitleme riski</text>
  <rect x="172" y="66" width="136" height="122" rx="8" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="240" y="84" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">ReentrantLock</text>
  <text x="240" y="102" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tryLock · süre sınırı</text>
  <text x="240" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kesilebilir bekleme</text>
  <text x="240" y="136" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">adil kilit · koşul kuyruğu</text>
  <text x="240" y="156" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">unlock senin işin</text>
  <text x="240" y="176" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">finally bloğu zorunlu</text>
  <rect x="336" y="36" width="310" height="164" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="491" y="56" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Görev çalıştırma · "iş nerede koşsun?"</text>
  <rect x="350" y="66" width="282" height="122" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="491" y="84" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">ExecutorService</text>
  <text x="491" y="103" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">havuz boyu · kuyruk türü</text>
  <text x="491" y="121" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">reddetme politikası</text>
  <text x="491" y="139" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">shutdown ve bekleme</text>
  <text x="491" y="160" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kilit yerine geçmez; havuzda koşan</text>
  <text x="491" y="176" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">görev de kilide ihtiyaç duyabilir</text>
  <line x1="322" y1="118" x2="334" y2="118" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu-kat)"/>
  <rect x="14" y="212" width="632" height="62" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="232" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">En iyi kilit, kaldırılabilen paylaşımdır</text>
  <text x="330" y="251" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">atomik sınıflar · eşzamanlı koleksiyonlar · değişmez nesneler · iş parçacığına özel durum</text>
  <text x="330" y="267" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">uygulama çok kopya koşuyorsa süreç içi kilit yetmez: koruma veritabanı seviyesinde kurulur</text>
</svg>
```

**Elenme sebepleri**

- "Üçü de eşzamanlılık aracı, duruma göre seçerim." Kategori hatası; bu soruda en sık eleyen cevap.
- "`Lock` daha performanslıdır." Mesnetsiz genelleme; fark ölçüme ve senaryoya bağlıdır.
- Yarış koşulunu anlatırken paylaşılan durumu söylememek. Paylaşılan değiştirilebilir durum yoksa yarış koşulu da yoktur.
- Sayaç artırma sorununu `volatile` ile çözdüm demek. `volatile` görünürlük verir, atomiklik vermez (`48`).
- Tek kopyada kilit koyup işi bitmiş saymak. Uygulama çok kopya koşuyorsa süreç içi kilit yetmez (`225`).
- `ExecutorService`'i kapatmaktan hiç söz etmemek. Kapanmayan havuz uygulamanın düzgün durmasını engeller.
- Çözümü anlatıp nasıl kanıtladığını söylememek. Doğrulanmamış düzeltme, düzeltme sayılmaz.

**Kendi hikâyen**

Vakayı sen dolduracaksın. Sorular şunlar:

- Hangi kodda iki isteğin aynı veriye aynı anda dokunduğunu gördün? Bakiye, stok, sayaç, durum alanı, numara üretimi?
- Belirti neydi? Kayıp güncelleme mi, çift kayıt mı, ara sıra tutmayan toplam mı, yalnız yükte çıkan hata mı?
- Nasıl fark edildi? Mutabakat farkı mı, müşteri şikâyeti mi, testte kırmızı mı?
- Nasıl doğruladın? Logdan iki eşzamanlı isteği eşleştirdin mi, yükü tekrarlayıp aynı hatayı üretebildin mi?
- Çözüm ne oldu? Atomik sınıf mı, `synchronized` mi, `@Version` ile iyimser kilit mi, `SELECT ... FOR UPDATE` mi, tekil kısıt mı?
- Neden o çözüm? Uygulama tek kopya mıydı, çok kopya mı? Bu ayrım cevabının en güçlü yeri olur.
- Nasıl kanıtladın? Aynı yük tekrarlandığında hata görülmedi mi? Kaç istekte kaç hatadan kaça indi?

Böyle bir vakan yoksa uydurma. `256`'daki köprüyü kur ve komşu deneyimini anlat. Örneğin çift tıklamayı engelleyen bir tekil kısıt da paylaşılan durum hikâyesidir. Küçük ama gerçek bir örnek, büyük ama uydurma bir örnekten her zaman daha güçlüdür.

### "JPA/Hibernate ile N+1 problemine nasıl yaklaşırsınız; nasıl tespit edip iyileştirirsiniz?"

**Soru aslında ne ölçüyor?**

Çözümü söylemek kolaydır; neredeyse herkes `JOIN FETCH` der. Bu yüzden soru asıl olarak iki yerde ayrıştırır. Birincisi tespittir: sorguları görmeden mi çalışıyorsun. İkincisi bedeldir: seçtiğin çözümün maliyetini biliyor musun. Görüşmeci tek bir şeye bakar — bu aday veritabanına giden sorguları görüyor mu.

**Sözlü cevap iskeleti**

1. **Sorunu tek cümlede tarif et.** Bir sorgu ana kayıtları getirir, sonra her kayıt için ilişki ayrı sorguyla çekilir. Yüz sipariş listelenirken yüz bir sorgu koşar.
2. **Nedenini söyle.** Tembel (lazy) ilişkiye kalıcı bağlam (persistence context) içinde erişilince vekil nesne (proxy) doldurulur. Döngü içindeki her erişim yeni bir sorgu üretir (`82`).
3. **Tespiti anlat; burası cevabın en değerli yeri.** SQL logunu aç, `hibernate.generate_statistics` ile sorgu sayısını gör, isteğe düşen sorgu sayısını ölç. Sonra kalıcı hâle getir: testte sorgu sayacıyla üst sınır koy, sınır aşılınca test kırılsın.
4. **Çözümleri bedelleriyle ver.** `JOIN FETCH` tek sorguya indirir; iki koleksiyon aynı anda getirilirse sonuç kümesi çoğalır ve koleksiyonlu sorguda sayfalama bellekte yapılır. `@EntityGraph` aynı işi bildirimsel yapar ve getirme planını sorgudan ayırır. Toplu getirme (batch fetching) N sorguyu birkaç sorguya indirir, sayfalamayı bozmaz. DTO projeksiyonu yalnız gereken sütunları çeker; okuma uçlarında çoğu zaman en ucuzudur (`80`).
5. **`FetchType.EAGER`'ı ayrıca ele al.** "Hep getir" demek sorunu çözmez, maliyeti her kullanım yerine yayar. Varsayılan tembel kalmalı, getirme kararı sorgu başına verilmelidir (`81`).
6. **Kararı bağlama bağla ve sonucu söyle.** Hangi ekran, kaç kayıt, sayfalama var mı. Sonunda ölçülen sayıyı ver: sorgu sayısı kaçtan kaça indi, yanıt süresi ne oldu.

```svg
<svg viewBox="0 0 660 320" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="N+1 problemi: belirti, tespit yollari, cozumler ve her cozumun bedeli">
  <defs>
    <marker id="sozlu-nplus" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">N+1: asıl puan tespitte ve bedeli bilmekte</text>
  <rect x="14" y="36" width="182" height="196" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="105" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Belirti</text>
  <text x="105" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">1 sorgu: ana kayıtlar</text>
  <text x="105" y="96" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">N sorgu: her kaydın ilişkisi</text>
  <text x="105" y="118" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">100 sipariş = 101 sorgu</text>
  <text x="105" y="142" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">liste ekranı saniyelere çıkar</text>
  <text x="105" y="160" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">bağlantı havuzu daralır</text>
  <text x="105" y="178" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">veri tabanı yükü tırmanır</text>
  <text x="105" y="202" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">kaynak: tembel ilişkiye</text>
  <text x="105" y="217" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">döngü içinde erişim</text>
  <line x1="198" y1="134" x2="212" y2="134" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu-nplus)"/>
  <rect x="216" y="36" width="182" height="196" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="307" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Tespit</text>
  <text x="307" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">SQL logunu aç, sorguları say</text>
  <text x="307" y="98" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hibernate.generate_statistics</text>
  <text x="307" y="118" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">istek başına sorgu sayısı</text>
  <text x="307" y="138" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yavaş uçta önce sayıya bak</text>
  <rect x="230" y="152" width="154" height="66" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="307" y="171" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">kalıcı hâle getir</text>
  <text x="307" y="189" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">testte sorgu sayacı eşiği</text>
  <text x="307" y="206" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">eşik aşılırsa test kırılır</text>
  <line x1="400" y1="134" x2="414" y2="134" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu-nplus)"/>
  <rect x="418" y="36" width="228" height="196" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="532" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Çözüm ve bedeli</text>
  <rect x="430" y="64" width="204" height="36" rx="6" fill="#FFFFFF" stroke="#B07D20" stroke-width="1.2"/>
  <text x="532" y="79" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">JOIN FETCH</text>
  <text x="532" y="93" text-anchor="middle" font-size="8.5" fill="#B07D20" font-family="system-ui">kartezyen çoğalma · sayfalama bellekte</text>
  <rect x="430" y="104" width="204" height="36" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="532" y="119" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">EntityGraph</text>
  <text x="532" y="133" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">aynı birleştirme · plan sorgudan ayrı</text>
  <rect x="430" y="144" width="204" height="36" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="532" y="159" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">toplu getirme</text>
  <text x="532" y="173" text-anchor="middle" font-size="8.5" fill="#25804C" font-family="system-ui">N yerine birkaç sorgu · sayfalama bozulmaz</text>
  <rect x="430" y="184" width="204" height="36" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="532" y="199" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">DTO projeksiyonu</text>
  <text x="532" y="213" text-anchor="middle" font-size="8.5" fill="#25804C" font-family="system-ui">yalnız gereken sütun · okuma ucunda en ucuz</text>
  <rect x="14" y="244" width="632" height="62" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="264" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">FetchType.EAGER çözüm değil</text>
  <text x="330" y="283" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Sorunu kaldırmaz; maliyeti ilişkiyi kullanmayan her sorguya yayar ve görünmez kılar.</text>
  <text x="330" y="299" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Varsayılan tembel kalır; getirme kararı ekran ekran, sorgu başına verilir.</text>
</svg>
```

**Elenme sebepleri**

- "`EAGER` yaparım, biter." Bu soruda en hızlı eleyen cevap.
- Yalnız çözüm saymak, tespiti hiç anlatmamak. Sorunun ağırlığı tespittedir.
- `JOIN FETCH`'i tek doğru sanıp bedelini söylememek. Bedeli bilmeyen aday onu her yere uygular.
- Koleksiyon getirirken sayfalamayı sorunsuz sanmak. Hibernate uyarır ve sayfalamayı belleğe alır; büyük veride yığın şişer.
- Sorgu sayısını hiç görmemiş olmak. "Log'a bakmadım ama muhtemelen" cümlesi cevabı bitirir.
- Her şeyi ikinci seviye önbellekle çözmeye çalışmak. Önbellek yanlış tasarlanmış getirmeyi düzeltmez, gizler.
- Görünüm katmanında tetiklenen sorguları hiç düşünmemek. Açık kalan oturum, sorguyu senin göremediğin yere taşır.

**Kendi hikâyen**

Bu soruda hikâyenin omurgası tek bir sayıdır: sorgu sayısı kaçtan kaça indi. Onu bul:

- Hangi ekran ya da uç nokta beklenenden yavaştı? Liste ekranı mı, rapor mu, gece koşan toplu iş mi?
- Belirti neydi? Sayfa açılışında saniyeler mi, veri tabanı yükünün tırmanması mı, bağlantı havuzunun tükenmesi mi?
- Sorgu sayısını gördün mü? Nereye baktın: uygulama logu, veri tabanı tarafı, izleme aracı?
- Hangi ilişki suçluydu? Döngü tam olarak nerede kuruluyordu?
- Hangi çözümü seçtin ve diğerini neden seçmedin? Bu cümle seni ezberden ayırır.
- Sayfalama var mıydı? Varsa seçtiğin çözüm sayfalamayı bozdu mu?
- Yanıt süresi ne oldu, tekrarını nasıl engelledin? Bir test eklendi mi?

Üretimde böyle bir vakan yoksa uydurmaya gerek yok; bu soruda deneyim üretmek kolaydır. Kendi projende SQL logunu aç, bir liste uç noktasını çağır, sorguları say. Gördüğün sayı gerçek bir gözlemdir ve mülakatta dürüstçe öyle anlatılır. `256`'daki köprü kalıbı burada da geçerlidir.

### Provayı nasıl yaparsın?

Bu dosya okunmak için değil, konuşulmak için yazıldı. Kullanımı şöyledir:

- Önce iskeleti okuma. Soruyu yüksek sesle oku, sonra dosyayı kapat.
- Süre tut. Hedef doksan saniyedir. Kısası eksik kalır, uzunu görüşmecinin ilgisini kaybettirir.
- Yüksek sesle cevapla ve kaydet. İçinden geçirmek prova sayılmaz; ağız ile beyin aynı hızda çalışmaz.
- Sonra iskeletle karşılaştır. Hangi adımı atladın, işaretle.
- En sık atlanan adım geçiş cümlesidir. Tanımdan deneyime geçemeyip tanımda dönmek en yaygın hatadır.
- İkinci turda yalnız eksik adımı çalış. Baştan anlatmak zamanı harcar, eksiği kapatmaz.
- Hikâyendeki sayıyı ezberle, cümleyi ezberleme. Ezberlenmiş cümle mülakatta ezber gibi duyulur.
- Üç soruyu üst üste yapma. Arada ara ver; yorgun ses ikna etmez.
- Ölçüt şudur: altı adımı kâğıda bakmadan sırayla sayabiliyorsan o soru hazır demektir.

### Tek cümlede

Bu üç soru bilgiyi değil, bilgiyi teslim etme biçimini ölçer. Birincisinde tanım kısa tutulur, ağırlık ölçüme dayalı hikâyeye verilir. İkincisinde kazanan cümle en baştadır: `synchronized` ile `Lock` karşılıklı dışlamadır, `ExecutorService` görev çalıştırma modelidir. Üçüncüsünde çözüm listesi değil, tespit yöntemi ve her çözümün bedeli ayrıştırır. Üçünde de aynı disiplin geçerlidir: belirti, ölçüm, kök neden, düzeltme, ölçülen sonuç. Ve hiçbirinde olmayan deneyim uydurulmaz; dürüst köprü her zaman uydurmadan güçlüdür.

# sozlu-1 | Temel
## Soru
Bir uygulamada iki ayrı olay yaşandı. Birincisinde çok derin özyinelemeli (recursive) bir metot hata verdi. İkincisinde bellekte tutulan bir liste büyüdükçe uygulama hata verdi. Bu iki hata sırasıyla hangileridir?
## A
İkisi de `OutOfMemoryError`; ikisinde de bellek tükendiği için ayrım yapılmaz
🤖 iki alan ayrıdır; derinliğin bitmesiyle yığında yer kalmaması aynı hatayı üretmez.
## B
Sırasıyla `StackOverflowError` ve `OutOfMemoryError`; ilki yığıt derinliğini, ikincisi yığın alanını tüketir
🤖 Doğru: yığıt metot çerçevelerini tutar, yığın nesneleri tutar.
## C
Sırasıyla `OutOfMemoryError` ve `StackOverflowError`; özyineleme nesne ürettiği için yığını tüketir
🤖 sıralama ters; özyinelemede tükenen şey çerçeve alanıdır.
## D
İkisi de `StackOverflowError`; büyüyen liste de çağrı yığıtında tutulduğu için aynı sınırı zorlar
🤖 liste nesnesi yığında durur; yığıtta yalnız ona işaret eden başvuru bulunur.
## Doğru
B
## Açıklama
Ayrım tek cümleyle verilir: **yığıt (stack) metot çerçevelerini ve yerel değişkenleri tutar, yığın (heap) nesneleri tutar.** Özyinelemeli metot her çağrıda yeni bir çerçeve iter. Derinlik sınırı aşılınca `StackOverflowError` gelir; bu bir alan değil, derinlik sorunudur. Büyüyen liste ise nesnedir ve yığında yer kaplar. Yığında yer kalmayınca `OutOfMemoryError` gelir. C şıkkı sıralamayı ters kurar; özyinelemede tükenen şey çerçeve alanıdır. D şıkkı listeyi yığıtta sanır; yığıtta yalnız başvuru vardır, nesnenin kendisi yığındadır. A şıkkı iki alanı tek alana indirger. Sözlü cevapta bu ayrımın hemen ardından sahiplik söylenir: yığıt iş parçacığı başına ayrılır, yığın paylaşılır (`60`). Sınıf üstverisi ise üçüncü bir alanda, Metaspace'te tutulur (`59`).

# sozlu-2 | Orta
## Soru
Bir metodun içinde `new ArrayList<String>()` çağrısı var ve bu metot iki iş parçacığı tarafından aynı anda çağrılıyor. Bellek yerleşimi ve paylaşım açısından en doğru ifade hangisidir?
## A
Liste nesnesi de ona işaret eden değişken de yığıtta durur; bu yüzden iki çağrı birbirini etkilemez
🤖 nesne yığında ayrılır; yığıtta yalnız başvuru bulunur.
## B
Nesne yığında olduğu için iki çağrı aynı listeyi paylaşır ve eşzamanlılık sorunu doğar
🤖 aynı alanda durmak paylaşmak demek değildir; her çağrı ayrı nesne üretir.
## C
Yığın iş parçacığı başına ayrıldığından her çağrının kendi yığını olur
🤖 iş parçacığı başına ayrılan yığıttır; yığın paylaşılır.
## D
Nesne paylaşılan yığında ayrılır, başvuru iş parçacığının kendi yığıtındadır; iki çağrı ayrı nesne üretir
🤖 Doğru: paylaşılan alanda durmak, aynı nesneyi paylaşmak anlamına gelmez.
## Doğru
D
## Açıklama
Burada iki ayrı şey karıştırılır: **nesnenin nerede durduğu** ile **paylaşılıp paylaşılmadığı**. Nesne her zaman yığında ayrılır ve yığın tüm iş parçacıklarınca paylaşılır. Buna karşılık her çağrı `new` ile kendi nesnesini üretir, o nesneye işaret eden başvuru da çağıran iş parçacığının kendi yığıtındadır. Yani nesne paylaşılan alandadır ama paylaşılan nesne değildir. B şıkkı tam bu noktayı kaçırır. Yarış koşulu ancak aynı nesne başvurusu iki iş parçacığına ulaşırsa doğar; örneğin başvuru bir alana ya da tekil (singleton) nesneye yazılırsa. C şıkkı adları ters çevirir: iş parçacığı başına ayrılan yığıttır, yığın değil. A şıkkı nesneyi yığıtta sanır. Bu ayrımı doğru kuran aday, eşzamanlılık sorularında da paylaşılan durumu daha çabuk bulur (`60`, `46`).

# sozlu-3 | Orta
## Soru
Bir aday şunu söylüyor: "synchronized, Lock ve ExecutorService arasında duruma göre seçim yaparım." Bu cümlenin en önemli kusuru nedir?
## A
Üçü aynı kategoride değildir; ilk ikisi karşılıklı dışlama, üçüncüsü görev çalıştırma modelidir
🤖 Doğru: aralarında seçim yapılmaz; havuzda koşan görev de kilit kullanabilir.
## B
Üçü de aynı işi yapar; bu yüzden seçim yerine hep en yenisi kullanılmalıdır
🤖 "en yenisi" bir ölçüt değildir; üstelik üçü aynı işi yapmaz.
## C
Cümle doğrudur; tek eksiği hangi ölçütle seçim yapıldığının söylenmemiş olmasıdır
🤖 eksik olan ölçüt değil kategoridir; ölçüt eklemek hatayı düzeltmez.
## D
`synchronized` artık kullanılmadığı için bu listeye hiç alınmamalıydı
🤖 `synchronized` hâlâ varsayılan tercihtir; kısa kritik bölgede yeterlidir.
## Doğru
A
## Açıklama
Bu bir **kategori hatasıdır** ve sözlü mülakatta hemen duyulur. `synchronized` ile `Lock` karşılıklı dışlama (mutual exclusion) araçlarıdır; "aynı anda kim girsin" sorusunu cevaplarlar. `ExecutorService` ise bir görev çalıştırma modelidir; "iş hangi iş parçacığında koşsun" sorusunu cevaplar. Bu yüzden üçü rakip değildir: havuzda koşan bir görev pekâlâ `synchronized` blok kullanır. Doğru cevap bu ayrımı en baştan kurar, sonra ikinci soruya geçer: `synchronized` kısa kritik bölgede yeter ve blok bitince kilit kendiliğinden bırakılır (`46`). `ReentrantLock` süre sınırlı deneme, kesilebilir bekleme ve adil kilit gibi seçenekleri ekler, karşılığında kilidi bırakma sorumluluğunu sana verir (`47`). C şıkkı hatayı ölçüt eksikliği sanır; ölçüt eklemek yanlış kategoriyi düzeltmez. D şıkkı yanlış bir "artık kullanılmıyor" iddiası kurar.

# sozlu-4 | İleri
## Soru
Stok düşme işlemi `synchronized` blok içine alındı ve yerel testlerde kayıp güncelleme kayboldu. Uygulama üretimde üç kopya (instance) hâlinde koşuyor ve sorun sürüyor. En doğru açıklama ve çözüm nedir?
## A
`synchronized` yerine `ReentrantLock` kullanılmalı; adil kilit seçeneği bu sorunu çözer
🤖 adil kilit bekleme sırasını düzenler, süreç sınırını aşmaz.
## B
Sorun görünürlüktedir; stok alanını `volatile` yapmak yeterli olur
🤖 `volatile` görünürlük verir, atomiklik vermez; üstelik sorun süreçler arasındadır.
## C
Kilit yalnız kendi süreci (process) içinde geçerlidir; kopyalar arası koruma veri tabanında kurulmalıdır
🤖 Doğru: `@Version` ile iyimser kilit veya satır kilidi süreç sınırını aşar.
## D
Kopya sayısı bire indirilmeli; dağıtık ortamda kayıp güncelleme başka türlü engellenemez
🤖 engellenebilir; ölçeklemeden vazgeçmek çözüm değil, geri adımdır.
## Doğru
C
## Açıklama
Bu, sözlü mülakatta adayları en çok ayıran ayrımdır. `synchronized` de `ReentrantLock` de **tek bir JVM süreci içinde** anlam taşır. Uygulama üç kopya koşuyorsa üç ayrı kilit vardır ve üçü birbirinden habersizdir. Yerel testin geçmesi de bu yüzden yanıltıcıdır: testte tek süreç vardır. Koruma, tüm kopyaların ortaklaştığı yerde kurulmalıdır. Bu genellikle veri tabanıdır: `@Version` ile iyimser kilit çakışmayı yakalar ve işlemi yeniden denetir, satır kilidi ise sırayı zorlar (`225`). Tekil kısıt (unique constraint) da bazı senaryolarda tek satırlık ve çok ucuz bir çözümdür. A şıkkı kilit türünü değiştirir ama sınırı değiştirmez. B şıkkı görünürlük ile atomikliği karıştırır; `volatile` okuma-değiştirme-yazma dizisini korumaz (`48`). D şıkkı ölçeklemeden vazgeçer. Cevabın sonunda nasıl kanıtlandığı söylenmelidir: aynı yük tekrarlandı, sayı tuttu.

# sozlu-5 | İleri
## Soru
Yavaş bir liste uç noktasında N+1 giderildi. Ekip bu sorunun ileride sessizce geri gelmesini kalıcı olarak engellemek istiyor. Aşağıdakilerden hangisi bu amaca en uygundur?
## A
Satır kapsamı eşiğini yükseltip liste uç noktasının test edilmesini zorunlu kılmak
🤖 kapsam sorgu sayısını görmez; test yeşil yanar, N+1 yerinde kalır.
## B
Üretimde SQL logunu sürekli açık bırakıp çıktıyı günlük olarak gözden geçirmek
🤖 insana bağlı ve maliyetlidir; log hacmi şişer, gözden kaçar.
## C
İlişkileri `FetchType.EAGER` yapıp sorgu sayısının istekten isteğe artmasını baştan engellemek
🤖 EAGER N+1'i çözmez; maliyeti ilişkiyi kullanmayan sorgulara da yayar.
## D
Testte sorgu sayacıyla uç nokta için üst sınır koymak ve sınır aşılınca testi kırmak
🤖 Doğru: sayı bir sözleşmeye dönüşür, geri kayma anında yakalanır.
## Doğru
D
## Açıklama
Soru "nasıl düzeltilir" değil, "**nasıl geri gelmez**" diye soruyor; bu ayrımı gören aday hemen ayrışır. N+1 sessizce geri gelir, çünkü kod okunuşu değişmez: bir alana erişim eklenir ve sorgu sayısı sessizce artar. Bunu yakalayan tek şey sayının kendisidir. Testte sorgu sayacı kurulur, uç nokta için üst sınır verilir ve sınır aşılınca test kırılır. Böylece sorgu sayısı bir sözleşmeye dönüşür. A şıkkı kapsamı ölçüt sanır; kapsam kodun çalıştığını gösterir, kaç sorgu koştuğunu göstermez. B şıkkı gözlemi insana bırakır; ayrıca üretimde sürekli açık SQL logu hem maliyetlidir hem gürültü üretir. C şıkkı en yaygın yanlış çözümdür: `EAGER` sorunu ortadan kaldırmaz, ilişkiyi hiç kullanmayan sorgulara bile taşır ve nereden geldiğini görünmez kılar (`81`, `82`). Sözlü cevapta tespit bölümü şu üçlüyle anlatılır: SQL logu, `hibernate.generate_statistics`, testteki sayaç eşiği.

# sozlu-6 | Uzman
## Soru
Sayfalanmış bir sipariş listesinde N+1'i gidermek için koleksiyon ilişkisi `JOIN FETCH` ile getirildi ve sayfa boyutu elli verildi. Üretimde bellek kullanımı beklenmedik biçimde yükseldi. En doğru açıklama nedir?
## A
`JOIN FETCH` ikinci seviye önbelleği devre dışı bıraktığı için veri her istekte yeniden yüklenir
🤖 belirtinin kaynağı önbellek değil, sayfalamanın nerede yapıldığıdır.
## B
Koleksiyon getirilirken sayfalama veri tabanında yapılamaz; tüm sonuç çekilip bellekte sayfalanır
🤖 Doğru: satırlar çoğaldığı için sınır sorgusu doğru sayfayı veremez.
## C
Sayfa boyutu elli olduğu için tek sorguda elli satır döner; asıl kaynak bağlantı havuzudur
🤖 koleksiyon birleştirmesinde satır sayısı sipariş sayısına eşit kalmaz.
## D
`JOIN FETCH` yerine `@EntityGraph` yazılsaydı davranış değişirdi; sorun bildirim biçimindedir
🤖 ikisi de aynı birleştirmeyi üretir; sorun sözdiziminde değildir.
## Doğru
B
## Açıklama
Bu, `JOIN FETCH`'in en pahalı yan etkisidir ve mülakatta bunu bilen aday azdır. Koleksiyon birleştirildiğinde bir sipariş, alt satırı kadar kez tekrarlanır; satır sayısı artık sipariş sayısına eşit değildir. Bu yüzden veri tabanına verilecek bir satır sınırı doğru sayfayı üretemez. Hibernate bu durumda sonuç kümesini çeker ve **sayfalamayı bellekte** yapar; genellikle bir uyarı da bırakır. Küçük veride fark edilmez, üretimde yığın şişer ve duraklamalar uzar. Doğru yaklaşım bağlama göre seçilir. Toplu getirme (batch fetching) N sorguyu birkaç sorguya indirir ve sayfalamayı bozmaz; liste ekranlarına iyi uyar. DTO projeksiyonu yalnız gereken sütunları çeker ve okuma uçlarında çoğu zaman en ucuz olanıdır (`80`). İki koleksiyonu birlikte `JOIN FETCH` etmek ayrı bir sorundur: sonuç kümesi çarpım gibi büyür (`82`). A şıkkı önbelleği suçlar. C şıkkı satır sayısının sabit kaldığını varsayar. D şıkkı `@EntityGraph`'ı farklı bir mekanizma sanır; o yalnız aynı getirmeyi bildirimsel yazar.
