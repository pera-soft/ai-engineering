---
slug: guncel-sanal-thread-eszamanlilik
baslik: "Sözlü Prova · Sanal Thread ve 2026 Eşzamanlılığı: Kazanç Nerede, Sabitlenme Nedir, Kapsam Ne Çözer"
kademe: İleri
sira: 277
etiketler: 11
---

## Konu anlatımı

Bu dosyada beş açık uçlu soru var. Beşi de sanal thread ekseninde ilerler: kazanç nerede doğar, nerede doğmaz, ne bozar ve neyi gereksizleştirir. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı, çünkü senin adına deneyim uydurulmaz.

Bu konunun kendine özgü bir tuzağı var: sürüm. Sanal thread, sabitlenme, yapısal eşzamanlılık ve `ScopedValue` son birkaç sürümde ayrı ayrı yol aldı. Bir kısmı kalıcılaştı, bir kısmı hâlâ önizlemede. Yanlış sürüm söylemek bu soruda doğrudan zarar verir, çünkü görüşmeci genellikle doğrusunu bilir. Kural nettir: emin değilsen numara verme, ne işe yaradığını anlat. Bu dosyadaki her sürüm ifadesi kaynak konulardan alınmıştır (`56`, `57`, `58`, `55`).

İkinci bir gerçek daha var. Bu konuları çoğu aday duymuştur ama üretimde kullanmamıştır. Görüşmeci de bunu bilir. Ölçtüğü şey ezber değil, "ne işe yaradığını anlamış mı" sorusudur. Bu yüzden cevabın gücü tanımdan değil, kazancın ve bedelin doğru yere konmasından gelir.

### "Sanal thread nedir, klasik thread'den farkı nedir?"

**Soru aslında ne ölçüyor?**

Bu soru bir tanım sorusu gibi durur ama değildir. Tanımı herkes söyler: hafif, ucuz, çok sayıda olabilir. Ayrım bir cümle sonra çıkar. Görüşmeci şunu bekler: aday kazancın **nerede** doğduğunu söyleyebiliyor mu. Sanal thread kodu hızlandırmaz; bekleyen işin taşıyıcıyı işgal etmesini engeller. Bu farkı kuran aday konuyu anlamıştır, kurmayan ise duyduğunu tekrar ediyordur. İkinci ayrım havuz sorusunda çıkar; oraya birazdan geliriz.

**Sözlü cevap iskeleti**

1. **Tek cümlelik tanımı ver.** Sanal thread (virtual thread), işletim sistemi thread'i değil, JVM'in yönettiği ve JVM'in zamanladığı hafif bir iş parçacığıdır (`56`).
2. **Taşıyıcı kavramını getir.** Sanal thread çalışmak için bir taşıyıcı thread'e (carrier thread) biner. Taşıyıcı gerçek bir platform thread'idir ve sayıları tipik olarak çekirdek sayısı kadardır.
3. **Mekanizmayı iki kelimeyle anlat.** Bloklayan bir G/Ç işinde sanal thread taşıyıcıdan iner ve onu serbest bırakır; iş bitince yeniden biner. Ölçeğin sırrı budur.
4. **Maliyet farkını söyle.** Platform thread işletim sistemi thread'iyle bire bir eşleşir ve yaklaşık bir megabayt yığın tutar. Sanal thread birkaç yüz bayttır; bu yüzden milyonlarca olabilir.
5. **Kazancı doğru yere koy.** Asıl kazanç **bloklayan girdi/çıktı** işindedir. İşlemci-yoğun işte thread hiç beklemez, inme fırsatı doğmaz ve darboğaz çekirdek sayısı olarak kalır.
6. **Havuz mantığının neden gereksizleştiğini bağla.** Havuz pahalı kaynağı paylaştırmak içindir. Sanal thread pahalı değildir; doğru kalıp görev başına bir thread'dir (`49`).
7. **Sürümü ancak eminsen söyle.** Sanal thread Java 21'de kalıcılaştı (JEP 444). Numarayı hatırlamıyorsan söyleme; hangi sorunu çözdüğünü anlatmak daha değerlidir.

```svg
<svg viewBox="0 0 660 324" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Platform thread ve sanal thread karsilastirmasi, havuzun neden gereksizlestigi ve kazancin hangi is yukunde dogdugu">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sanal thread kodu hızlandırmaz; beklemenin bedelini kaldırır</text>
  <rect x="14" y="36" width="200" height="170" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="114" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Platform thread</text>
  <text x="114" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">işletim sistemi thread'i ile 1:1</text>
  <text x="114" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yaklaşık 1 MB yığın</text>
  <text x="114" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">binlerle sınırlı</text>
  <text x="114" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">işletim sistemi zamanlar</text>
  <text x="114" y="164" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">pahalı kaynak → havuzlanır</text>
  <text x="114" y="188" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">havuz boyu = eşzamanlılık tavanı</text>
  <rect x="230" y="36" width="200" height="170" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Sanal thread</text>
  <text x="330" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">M:N — taşıyıcıya biner, iner</text>
  <text x="330" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">birkaç yüz bayt</text>
  <text x="330" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">milyonlarca olabilir</text>
  <text x="330" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">JVM zamanlar</text>
  <text x="330" y="164" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">ucuz → görev başına açılır</text>
  <text x="330" y="188" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">Java 21'de kalıcı (JEP 444)</text>
  <rect x="446" y="36" width="200" height="170" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="546" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Havuz niye gereksizleşir</text>
  <text x="546" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">havuz pahalı kaynağı paylaştırır</text>
  <text x="546" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sanal thread pahalı değildir</text>
  <text x="546" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">havuzlamak kazancı geri alır</text>
  <text x="546" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sayıyı çekirdeğe bağlamak</text>
  <text x="546" y="158" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">platform thread mantığıdır</text>
  <text x="546" y="188" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">doğru kalıp: görev başına bir thread</text>
  <rect x="14" y="218" width="316" height="92" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="172" y="240" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kazanç burada: bloklayan G/Ç</text>
  <text x="172" y="262" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">beklerken taşıyıcıdan iner, onu bırakır</text>
  <text x="172" y="282" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">taşıyıcı o boşlukta başka isteğe koşar</text>
  <text x="172" y="302" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">istek başına bir thread yeniden ölçeklenir</text>
  <rect x="344" y="218" width="302" height="92" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="240" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Kazanç yok: işlemci-yoğun iş</text>
  <text x="495" y="262" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">thread hiç beklemez, inme fırsatı doğmaz</text>
  <text x="495" y="282" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">darboğaz çekirdek sayısıdır</text>
  <text x="495" y="302" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">thread türü sonucu değiştirmez</text>
</svg>
```

**Elenme sebepleri**

- "Sanal thread daha hızlı bir işletim sistemi thread'idir." Bire bir eşleme platform thread'in özelliğidir; bu cümle modeli tersine anlatır.
- Kazancın nerede doğduğunu hiç söylememek. Tanımı bilip iş yükünü söylemeyen aday ezber izlenimi bırakır.
- İşlemci-yoğun işte de hızlanma vaat etmek. Beklemesi olmayan işte inme fırsatı yoktur; bu cümle mekanizmayı anlamadığını gösterir.
- Sanal thread'i havuzlamayı doğal saymak. Havuz, ucuzluk avantajını geri alır ve eşzamanlılığa yapay bir tavan koyar.
- Taşıyıcı kavramını hiç anmamak. Taşıyıcı olmadan ne inme ne sabitlenme anlatılabilir; sonraki soruların tamamı oraya bağlanır.
- Sürüm numarasını yanlış söylemek. Hatırlamadığın bir numarayı tahmin etmek, doğru anlattığın her şeyin güvenilirliğini düşürür.
- "Artık asenkron kod yazmaya gerek yok" gibi mutlak cümle kurmak. Takas söylenmeyince cevap teknik değil, reklam gibi duyulur.

**Kendi hikâyen / dürüst köprü**

Bu konuda üretim deneyimin olmayabilir; bu normaldir ve gizlenmesi gereken bir şey değildir. Şu sorular sende gerçek bir dayanak var mı diye bakar:

- Hiç thread havuzu boyutu ayarladın mı? Hangi uygulamada, hangi belirtiyle: zaman aşımı mı, kuyruk birikmesi mi?
- Havuz dolduğunda ne oluyordu? İstekler bekliyor muydu, reddediliyor muydu, yoksa yanıt süresi mi tırmanıyordu?
- Çalıştığın uygulamada bekleme nereden geliyor? Veri tabanı, dış servis çağrısı, dosya, mesaj kuyruğu?
- Hiç sanal thread denedin mi? Küçük bir örnekte bile olsa ne ölçtün, neyi karşılaştırdın?
- Uygulamanın iş yükü hangi tarafta ağır: bekleme mi, hesap mı? Bu ayrımı yapabiliyorsan cevabın omurgası hazırdır.

Deneyimin yoksa uydurma. `256`'daki dürüst köprüyü kur: üretimde kullanmadığını açıkça söyle, en yakın gerçek deneyimini bağla, farkı adlandır ve öğrenme planını ver. Kendi projende bloklayan bir uç noktayı hem havuzla hem sanal thread'le çalıştırıp gözlemlemek gerçek bir gözlemdir; öyle anlatılırsa değer taşır.

### "Sanal thread her yerde daha mı hızlı?"

**Soru aslında ne ölçüyor?**

Bu bir tuzak sorusudur ve tuzağın adı "evet"tir. Görüşmeci burada abartıyı arar. Yeni bir teknolojiyi her derde deva gibi anlatan aday, karar veren biri olarak güvenilmez görünür. Beklenen cevap üç bacaklıdır: nerede kazanç yok, ne bozabilir, nasıl ölçersin. Üçüncü bacak en çok atlanan yerdir. Ağırlık muhakemededir; deneyim yoksa bile bu soru tam puanla cevaplanabilir.

**Sözlü cevap iskeleti**

1. **Net başla.** Hayır. Sanal thread bir hızlandırıcı değil, bekleme maliyetini kaldıran bir ölçeklenme aracıdır.
2. **İlk sınırı söyle.** İşlemci-yoğun işte kazanç yoktur. Thread hiç beklemediği için taşıyıcı hiç boşalmaz; darboğaz çekirdek sayısı olarak kalır (`56`).
3. **İkinci sınırı söyle: sabitlenme.** Sanal thread beklerken taşıyıcıdan inemezse ona sabitlenmiş (pinned) olur. Taşıyıcı sayısı az olduğu için birkaç sabitlenme bütün ölçeği çökertebilir (`57`).
4. **Sabitlenmenin bugünkü durumunu doğru ver.** JDK 21–23'te `synchronized` içinde bloklamak sabitlerdi. **JEP 491 ile JDK 24'ten itibaren sabitlemiyor.** Native/JNI çağrıları ve FFM downcall'ları ise hâlâ sabitler.
5. **Üçüncü sınırı söyle: `ThreadLocal` maliyeti.** Her thread kendi kopyasını tutar. Milyonlarca sanal thread'de bu kopyalar ciddi bellek yüküne dönüşür (`55`).
6. **Dördüncü sınırı söyle: alt sistem tavanı.** Uygulama katmanındaki thread tavanı kalkar, ama veri tabanı bağlantısı gibi alt sistemlerin tavanı yerinde kalır. Darboğaz genellikle yer değiştirir, yok olmaz.
7. **Ölçümle kapat.** Karar ölçümle verilir: yüzdelik dilimler, hata oranı ve taşıyıcı doluluğu değişmediyse geçiş kazanç getirmemiştir (`64`).

**Elenme sebepleri**

- "Her yerde daha hızlıdır" demek. Bu soruda en hızlı eleyen cevaptır; tuzak zaten bu cümleyi ölçmek için kurulmuştur.
- İşlemci-yoğun işi sınır olarak saymamak. Bekleme olmayan yerde kazanç aramak, mekanizmayı anlamadığını gösterir.
- Sabitlenmeyi hiç anmamak. Sanal thread'in tek gerçek yıkıcı arızasını bilmeyen aday üretim tarafını düşünmemiş sayılır.
- Sabitlenme durumunu güncel olmayan biçimde anlatmak. "`synchronized` her zaman sabitler" cümlesi bugün yanlıştır ve sürüm takibi yapılmadığını gösterir.
- `ThreadLocal` maliyetini yok saymak. Ölçek değişince bedava sandığın şey pahalıya döner; bu ayrım deneyimli adayı ayırır.
- Darboğazın yer değiştirdiğini görmemek. Uygulama tavanını kaldırıp veri tabanını ezmek yaygın bir geçiş hatasıdır.
- Ölçümden hiç söz etmemek. Kazancı sayıyla göstermeyen cevap teknik ama kanıtsız kalır.

**Kendi hikâyen / dürüst köprü**

Bu soruda hikâye şart değildir; muhakeme yeter. Yine de şu sorular sende gerçek bir dayanak arar:

- Hiç bir iyileştirme yapıp beklediğin kazancı alamadın mı? Neyi ölçmüştün, sonuç neden farklı çıktı?
- Bir darboğazın yer değiştirdiğine tanık oldun mu? Uygulama rahatladı, alt sistem mi tıkandı?
- Ölçüm sırasını nasıl kuruyorsun? Önce metrik mi, log mu, profilleyici mi?
- Yeni bir teknolojiyi ekibe önerdiğin oldu mu? Bedelini nasıl anlattın, hangi soruyla karşılaştın?

Sanal thread'i üretimde ölçmediysen bunu açıkça söyle. `256`'daki köprü burada çok iyi çalışır: "Üretimde ölçmedim; ama şu uygulamada havuz doluluğu yüzünden zaman aşımı yaşadık ve sanal thread tam o sınırı hedefliyor" gibi bir cümle, uydurulmuş bir vakadan güçlüdür.

### "`synchronized` yerine `ReentrantLock` neden öneriliyordu? Bugün hâlâ geçerli mi?"

**Soru aslında ne ölçüyor?**

Bu soru bilgi tazeliğini ölçer. Cevabın doğrusu son birkaç sürümde değişti ve internette hâlâ eski tavsiye dolaşıyor. Görüşmeci iki şeye bakar: aday sabitlenmenin mekanizmasını biliyor mu ve durumun değiştiğini takip etmiş mi. Ezberden "sanal thread'de `synchronized` kullanma" diyen aday, iki yıl önceki bir yazıyı tekrar ediyordur. Doğru cevap hem eski gerekçeyi hem bugünkü durumu anlatır.

**Sözlü cevap iskeleti**

1. **Önce mekanizmayı kur.** Sanal thread'in bütün kazancı beklerken taşıyıcıyı bırakabilmesidir. Sabitlenme (pinning), bu bırakmanın olamaması, yani taşıyıcının işgal edilmeye devam etmesidir (`57`).
2. **Neden yıkıcı olduğunu söyle.** Taşıyıcı sayısı tipik olarak çekirdek sayısı kadardır. Birkaç sabitlenmiş thread taşıyıcıları tüketirse kuyruktaki sanal thread'ler sıra bulamaz.
3. **Eski gerekçeyi anlat.** JDK 21–23'te sanal thread `synchronized` içindeyken G/Ç'de bloklarsa taşıyıcıya sabitlenirdi. Sıcak yollarda `ReentrantLock`'a geçmenin asıl sebebi buydu (`47`).
4. **Bugünkü durumu net ver.** JEP 491, JDK 24 ile monitör sahipliğini inme ile uyumlu hâle getirdi. Artık `synchronized` içinde bloklamak sabitlemiyor; `synchronized` ne kaldırıldı ne yasaklandı.
5. **Neyin sürdüğünü söyle.** Native/JNI çağrıları ve FFM downcall'ları hâlâ sabitler. Sebebi yapısaldır: JVM, Java olmayan bir yığın çerçevesi üzerindeki thread'i güvenle askıya alıp devam ettiremez.
6. **Göç kararını doğru çerçevele.** Yalnız sabitlenmeden kaçınmak için yapılacak `synchronized` → `ReentrantLock` göçü artık şart değildir. `tryLock`, zaman aşımı ve adillik gibi ihtiyaçlar için `ReentrantLock` hâlâ değerlidir; bunlar ayrı gerekçelerdir.
7. **Teşhisi ekle.** Sabitlenme JFR'nin `jdk.VirtualThreadPinned` olayıyla yakalanır; olay, sabitlenmenin olduğu andaki çağrı yığınını verir.

```svg
<svg viewBox="0 0 660 320" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sabitlenmenin JDK 24 oncesi ve sonrasi durumu, hala sabitleyen cagrilar ve JFR ile teshis sirasi">
  <defs>
    <marker id="gncvt-pin-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sabitlenme: neyin değiştiği, neyin sürdüğü</text>
  <rect x="14" y="36" width="200" height="150" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="114" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">JDK 21–23</text>
  <text x="114" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">synchronized + G/Ç → SABİTLER</text>
  <text x="114" y="104" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">native/JNI çağrısı → SABİTLER</text>
  <text x="114" y="126" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">FFM downcall → SABİTLER</text>
  <text x="114" y="156" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">dönemin önerisi:</text>
  <text x="114" y="172" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sıcak yolda ReentrantLock</text>
  <line x1="216" y1="111" x2="228" y2="111" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncvt-pin-ok)"/>
  <rect x="230" y="36" width="200" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">JDK 24+ (JEP 491)</text>
  <text x="330" y="82" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">synchronized + G/Ç → SABİTLEMEZ</text>
  <text x="330" y="104" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">native/JNI çağrısı → HÂLÂ SABİTLER</text>
  <text x="330" y="126" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">FFM downcall → HÂLÂ SABİTLER</text>
  <text x="330" y="156" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">yalnız sabitlenme için yapılacak</text>
  <text x="330" y="172" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">göç artık şart değil</text>
  <rect x="446" y="36" width="200" height="150" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="546" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Neden bu kadar yıkıcı</text>
  <text x="546" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">taşıyıcı sayısı ≈ çekirdek sayısı</text>
  <text x="546" y="104" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sabitlenen taşıyıcıyı bırakmaz</text>
  <text x="546" y="126" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kuyruktakiler sıra bulamaz</text>
  <text x="546" y="156" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">CPU düşük görünebilir;</text>
  <text x="546" y="172" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">grafiğe bakmak yanıltır</text>
  <rect x="14" y="198" width="632" height="110" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="220" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Teşhis sırası: tahmin değil, kayıt</text>
  <rect x="30" y="234" width="180" height="44" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="120" y="252" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yük altında JFR kaydı aç</text>
  <text x="120" y="268" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">üretim benzeri yük şart</text>
  <line x1="212" y1="256" x2="236" y2="256" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncvt-pin-ok)"/>
  <rect x="240" y="234" width="190" height="44" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="335" y="252" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">jdk.VirtualThreadPinned olayı</text>
  <text x="335" y="268" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">çağrı yığınıyla birlikte gelir</text>
  <line x1="432" y1="256" x2="456" y2="256" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncvt-pin-ok)"/>
  <rect x="460" y="234" width="180" height="44" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="550" y="252" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sabitleyen çağrıyı bul</text>
  <text x="550" y="268" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">genellikle native/FFM sınırı</text>
  <text x="330" y="298" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">Bellek ayarını büyütmek işe yaramaz: bu bir eşzamanlılık sorunudur, bellek sorunu değil.</text>
</svg>
```

**Elenme sebepleri**

- "Sanal thread kullanıyorsan `synchronized` kullanma." Güncelliğini yitirmiş bir tavsiyedir; bugün eski bir yazıyı tekrar ettiğini gösterir.
- Sabitlenmeyi çöp toplayıcının nesneyi bellekte sabitlemesi sanmak. Bu bambaşka bir kavramdır ve tanımı karıştırdığını hemen belli eder.
- Sabitlenmeyi işlemci çekirdeğine bağlanma (affinity) ile karıştırmak. Yine farklı bir kavramdır; sabitlenme yalnız taşıyıcıdan inememektir.
- "Sabitlenme tamamen bitti" demek. Native/JNI ve FFM sınırı sürüyor; mutlak cümle kurmak eksik bilgiyi ele verir.
- Taşıyıcı sayısının azlığını hiç anmamak. Sabitlenmenin neden birkaç thread'le bütün servisi çökerttiği ancak buradan anlaşılır.
- Teşhis yöntemi söylememek. Sorunu tarif edip nasıl yakalanacağını bilmeyen aday üretim tarafında zayıf görünür.
- Sabitlenmeyi bellek ayarıyla çözmeye çalışmak. Yığın büyütmek eşzamanlılık sorununu çözmez; yanlış katmana bakmaktır.

**Kendi hikâyen / dürüst köprü**

Bu soruda deneyim beklentisi düşüktür; çoğu ekip bunu henüz yaşamamıştır. Yine de şunları kendine sor:

- Hiç kilit çekişmesi (lock contention) yaşadın mı? Nasıl fark ettin, neyi ölçtün?
- Kilit tutarken uzun süren bir çağrı yaptığın oldu mu? Kilidin içinde ağ çağrısı ya da veri tabanı sorgusu var mıydı?
- `ReentrantLock` kullandın mı? Gerekçen `tryLock` mıydı, zaman aşımı mıydı, yoksa alışkanlık mıydı?
- Hiç JFR kaydı aldın mı? Almadıysan bu, mülakat öncesi bir akşamda kapatılabilecek en kârlı boşluktur.

Sanal thread'de sabitlenme yaşamadıysan bunu söylemekten çekinme; `256`'daki köprü tam bu duruma göredir. "Yaşamadım ama mekanizmasını ve nasıl yakalanacağını biliyorum" cümlesi, uydurulmuş bir üretim vakasından çok daha güvenilir duyulur.

### "Yapısal eşzamanlılık ne çözüyor?"

**Soru aslında ne ölçüyor?**

Bu soruda görüşmeci genellikle sürümü değil, fikri sorar. Ölçtüğü şey şudur: aday eşzamanlılığı bir yaşam döngüsü sorunu olarak görebiliyor mu. Zayıf cevap özellikleri sayar. Güçlü cevap önce sorunu adlandırır: klasik çalıştırıcıyla başlatılan alt görevler, başlatan koddan bağımsız yaşar; kimin kimi iptal edeceği belirsizdir ve görevler sızabilir. Bir de sürüm boyutu var; burada abartmamak puan kazandırır.

**Sözlü cevap iskeleti**

1. **Sorunu adlandırarak başla.** Klasik `ExecutorService` ile başlatılan alt görevlerin sahibi yoktur. Hata yayılımı belirsizdir ve görev sızabilir (`49`, `50`).
2. **Çözümü tek cümlede ver.** Yapısal eşzamanlılık, alt görevlerin ömrünü onları başlatan kodun kapsamına bağlar; kapsam kapanmadan hiçbiri dışarıda kalmaz (`58`).
3. **Mekanizmayı anlat.** `StructuredTaskScope` içinde alt görevler çatallanır (fork), her biri kendi sanal thread'inde başlar. `join()` hepsinin bitmesini bekleyen senkron bariyerdir; sonuçlar ancak ondan sonra güvenle okunur.
4. **İki kalıbı ayır.** Biri düşünce kalanların iptal edilmesi "hepsi gerekli" senaryosudur. İlk başarılıda kalanların iptal edilmesi ise "ilk kazanan" senaryosudur.
5. **Kazancı somutla.** İptal yukarıdan aşağı yayılır, hata tek yerden yukarı taşınır ve yaşam döngüsü öngörülebilir olur. Amaç ham hız değildir.
6. **Bağlam taşımayı bağla.** Alt görevlere bağlam `ScopedValue` ile taşınır; değer kapsam boyunca geçerlidir ve kapsam bitince kendiliğinden geçersizleşir (`55`).
7. **Sürümü olduğu gibi söyle.** Yapısal eşzamanlılık JDK 21'den beri önizleme JEP'leriyle geliyor ve **Java 25'te hâlâ önizlemede**. `--enable-preview` gerektirir, API sürümler arasında değişebilir. Üçüncü parti bir kütüphane değildir; JDK'nin parçasıdır.

```svg
<svg viewBox="0 0 660 336" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yapisal kapsam icinde alt gorevlerin dogup toplanmasi ve 2026 surum gercegi tablosu">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kapsam: alt görevler birlikte doğar, birlikte biter</text>
  <rect x="14" y="36" width="632" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">StructuredTaskScope — tek sahiplik sınırı</text>
  <rect x="60" y="72" width="150" height="54" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="135" y="94" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">alt görev A</text>
  <text x="135" y="112" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">kendi sanal thread'inde</text>
  <rect x="250" y="72" width="150" height="54" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="325" y="94" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">alt görev B</text>
  <text x="325" y="112" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">kendi sanal thread'inde</text>
  <rect x="440" y="72" width="150" height="54" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="515" y="94" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">alt görev C — hata</text>
  <text x="515" y="112" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">iptal aşağı yayılır</text>
  <text x="330" y="150" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">join(): hepsi bitene ya da kapsam iptal edene kadar bekler; sonuçlar ondan sonra okunur</text>
  <text x="330" y="172" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">blok kapanmadan hiçbir alt görev dışarıda kalmaz — sızan görev yok</text>
  <rect x="14" y="198" width="632" height="126" rx="12" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="220" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">2026 sürüm gerçeği — söylemeden önce doğrula</text>
  <rect x="28" y="234" width="300" height="64" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="178" y="254" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Kalıcı (final)</text>
  <text x="178" y="273" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sanal thread — Java 21 (JEP 444)</text>
  <text x="178" y="290" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ScopedValue — Java 25</text>
  <rect x="344" y="234" width="290" height="64" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="489" y="254" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Önizleme (preview)</text>
  <text x="489" y="273" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yapısal eşzamanlılık — Java 25'te hâlâ</text>
  <text x="489" y="290" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">önizleme bayrağı gerekir, API değişebilir</text>
  <text x="330" y="314" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Sürümden emin değilsen numara verme; ne işe yaradığını ve hangi sorunu çözdüğünü anlat.</text>
</svg>
```

**Elenme sebepleri**

- "Yapısal eşzamanlılık kodu hızlandırır" demek. Amaç hız değil, yaşam döngüsünün yapısal yönetimidir; bu cümle konuyu kaçırdığını gösterir.
- Sızan görev kavramını hiç anmamak. Çözülen sorun söylenmezse cevap bir API turuna dönüşür.
- Kalıcılaştığını iddia etmek. Java 25'te hâlâ önizlemedir; "final oldu" demek doğrudan yanlış bilgidir.
- Üçüncü parti kütüphane sanmak. JDK'nin parçasıdır; bu hata konuyu hiç denemediğini belli eder.
- İptalin yönünü karıştırmak. İptal kapsamdan alt görevlere yayılır; ters anlatım mekanizmayı bilmediğini gösterir.
- İki kalıbı ayırmamak. "Biri düşerse dur" ile "ilki kazanınca dur" farklı senaryolardır ve karar bu farkla verilir.
- Önizleme özelliğini üretime hafifçe önermek. API'nin değişebileceğini söylemeden tavsiye etmek risk körlüğü olarak okunur.

**Kendi hikâyen / dürüst köprü**

Bu konuda üretim deneyimi olan aday azdır. Görüşmeci de bunu bilir; hikâye yerine muhakeme bekler. Şu sorular yine de bir dayanak bulabilir:

- Paralel çağrı yapan bir kodun oldu mu? Birden çok servisten veri çekip birleştirdiğin bir uç nokta var mıydı?
- Çağrılardan biri patladığında ne oluyordu? Diğerleri boşuna çalışmaya devam ediyor muydu?
- İptali nasıl yönetiyordun? Zaman aşımı, `Future.cancel`, yoksa hiç mi?
- Hiç sahipsiz kalan bir görev ya da kapanmayan bir çalıştırıcı yaşadın mı? Belirtisi neydi?
- Önizleme aşamasındaki bir özelliği hiç denedin mi? Ekibe önerirken hangi çekinceyi belirtirdin?

Deneyimin yoksa uydurma. `256`'daki köprüyü kur ve şunu ekle: "Üretimde kullanmadım, üstelik hâlâ önizlemede olduğu için üretime önermezdim; ama çözdüğü sorunu şu vakada yaşadım." Bu cümle hem dürüstlük hem karar olgunluğu gösterir.

### "`ThreadLocal` yerine `ScopedValue` mi kullanmalıyız?"

**Soru aslında ne ölçüyor?**

Bu soru bir ölçek sorusudur. Görüşmeci `ThreadLocal`'ın ne olduğunu sormuyor; ölçek değişince neyin bozulduğunu soruyor. İki ayrı bedel var ve çoğu aday yalnız birini biliyor. Birincisi klasik havuz sızıntısıdır: temizlenmeyen değer havuz thread'ine yapışır. İkincisi sanal thread'e özgüdür: milyonlarca thread, milyonlarca kopya demektir. İkisini birden söyleyen aday konuyu gerçekten okumuştur.

**Sözlü cevap iskeleti**

1. **`ThreadLocal`'ın ne yaptığını kısa geç.** Her thread'e değişkenin kendi kopyasını verir; paylaşmak yerine izole eder, bu yüzden kilit gerekmez (`55`).
2. **Klasik bedeli söyle.** Havuz thread'leri uzun yaşar. `remove()` çağrılmazsa değer haritada asılı kalır: hem bellek sızıntısı hem bir sonraki isteğe eski bağlamın sızması.
3. **Sanal thread'in bunu nasıl değiştirdiğini anlat.** Sanal thread havuzlanmaz; her görev için doğar ve ölür. Havuza yapışan değer sorunu bu yüzden ortadan kalkar.
4. **Yeni bedeli söyle.** Buna karşılık her thread kendi kopyasını taşır. Milyonlarca sanal thread'de bu kopyalar bellek yüküne dönüşür ve çöp toplamayı zorlar (`56`).
5. **`ScopedValue`'yu bu boşluğa yerleştir.** Değiştirilemez ve kapsam-bağlıdır: değer yalnız belirli bir çağrı ağacında görünür ve kapsam bitince kendiliğinden geçersizleşir. `remove()` diye bir adımı yoktur.
6. **Sürümü doğru ver.** `ScopedValue` Java 21'de önizleme olarak geldi ve **Java 25'te kalıcılaştı**. Bu, aynı şemsiyedeki yapısal eşzamanlılıktan farklı bir durumdur; ikisini karıştırma.
7. **Kararı ölçütle kapat.** Yeni yazılan bağlam taşıma için `ScopedValue` tercih edilir. Mevcut `ThreadLocal` kullanımları kademeli taşınır; kütüphane bağımlılıkları çoğu zaman geçişin hızını belirler.

**Elenme sebepleri**

- "`ScopedValue`, `ThreadLocal`'ın yeni adıdır" demek. Farklı ve daha kısıtlı bir modeldir; bu cümle iki kavramı da bilmediğini gösterir.
- `ThreadLocal`'ın sanal thread'de çalışmadığını söylemek. Çalışır; sorun yasak değil, ölçekteki bellek maliyetidir.
- `remove()` disiplinini hiç anmamak. Havuz tabanlı kodun en klasik sızıntısıdır ve bilinmemesi deneyimsizlik olarak okunur.
- Sanal thread'e geçince tüm bağlam sorunlarının çözüldüğünü sanmak. Bir bedel gider, başka bir bedel gelir; takas cümlesi kurulmalıdır.
- `ScopedValue`'yu değiştirilebilir bir kutu gibi anlatmak. Değişmezlik onun tasarım kararıdır; bunu kaçırmak modeli ters anlatmaktır.
- Sürüm durumlarını birbirine karıştırmak. `ScopedValue` kalıcı, yapısal eşzamanlılık önizleme; ikisini aynı sepete koymak yanlış bilgidir.
- "Hemen hepsini değiştiririz" demek. Kütüphane ve çerçeve bağımlılıkları düşünülmeden verilen göç sözü gerçekçi bulunmaz.

**Kendi hikâyen / dürüst köprü**

Bu soruda gerçek bir dayanağın olma ihtimali yüksektir, çünkü `ThreadLocal` yaygındır. Şu sorular onu çıkarır:

- Projende istek bağlamını nasıl taşıyorsunuz? İzleme kimliği (trace-id), oturum açan kullanıcı ya da kiracı bilgisi nerede duruyor?
- Bir kütüphane `ThreadLocal` kullanıyor muydu? Günlük bağlamı (MDC) ya da işlem yönetimi tarafında fark ettin mi?
- Hiç eski bağlamın yeni isteğe sızdığını gördün mü? Belirti neydi: yanlış kullanıcı adı, karışan izleme kimliği, tutmayan log?
- Temizliği nasıl garanti ediyorsunuz? `finally` bloğu var mı, yoksa umuda mı bağlı?
- Asenkron bir çağrıya geçtiğinde bağlam kayboldu mu? Nasıl çözdün?

`ScopedValue`'yu üretimde kullanmadıysan söyle. `256`'daki köprü şöyle kurulur: `ThreadLocal` sızıntısını gerçekten yaşadıysan onu anlat, sonra "`ScopedValue` tam bu sınıf hatayı yapısal olarak imkânsızlaştırıyor" diyerek muhakemeni göster. Yaşanmış küçük bir hata, uydurulmuş büyük bir vakadan her zaman güçlüdür.

### Bu bölümden çıkan ders

Bu beş sorunun tamamı tek bir cümlenin etrafında dönüyor: sanal thread kodu hızlandırmaz, beklemenin bedelini kaldırır. Bu cümleyi kurabilen aday kazancın bloklayan girdi/çıktı işinde doğduğunu, işlemci-yoğun işte doğmadığını, havuz mantığının neden gereksizleştiğini ve sabitlenmenin neden yıkıcı olduğunu aynı zeminden türetebilir. İkinci disiplin sürümle ilgilidir ve bu konuda diğer her konudan daha keskindir: `synchronized` kaynaklı sabitlenme JDK 24 ile bitti, `ScopedValue` Java 25'te kalıcılaştı, yapısal eşzamanlılık ise Java 25'te hâlâ önizlemede. Bu üçünü karıştırmak, doğru anlattığın her şeyin güvenilirliğini düşürür; emin değilsen numara verme ve mekanizmayı anlat. Üçüncü disiplin ise dürüstlüktür: bu konuları üretimde kullanmamış olmak normaldir ve `256`'daki köprü, uydurulmuş bir üretim hikâyesinden her zaman daha ikna edicidir.

# gncvt-1 | Orta
## Soru
Bir ekip sanal thread'e (virtual thread) geçtikten sonra hangi iş yükünde belirgin kazanç bekleyebilir?
## A
Bellek içi büyük bir diziyi sıralayan, hiçbir beklemesi olmayan saf hesap yükünde
🤖 bu işlemci-yoğun iştir; thread beklemediği için taşıyıcı hiç boşalmaz.
## B
Tek thread'de dönen ve hiç dış çağrı yapmayan bir toplu iş (batch) döngüsünde
🤖 beklemesiz tek thread'li iş, eşzamanlılık modelinden etkilenmez.
## C
Çok sayıda ağ ve veri tabanı çağrısında bloklayan, girdi/çıktı ağırlıklı istek yükünde
🤖 Doğru: bloklama anında thread taşıyıcıdan iner, taşıyıcı başka işe koşar.
## D
Her iş yükünde; sanal thread kodun türünden bağımsız olarak yürütmeyi hızlandırır
🤖 sanal thread kodu hızlandırmaz; yalnız bekleme maliyetini kaldırır.
## Doğru
C
## Açıklama
Sanal thread'in tek mekanizması şudur: bloklayan bir işlemde taşıyıcı thread'den (carrier thread) **iner** ve onu serbest bırakır. Taşıyıcı o boşlukta başka bir sanal thread'e hizmet eder. Dolayısıyla kazanç yalnız **bekleme olan** yerde doğar: çok sayıda ağ ve veri tabanı çağrısı yapan istek yükü tam olarak budur. Böyle bir serviste "istek başına bir thread" gibi yalın ve senkron kod, çok daha yüksek eşzamanlılığa ölçeklenir (`56`). A ve B'de bekleme yoktur; thread hiç inmediği için darboğaz çekirdek sayısı olarak kalır ve thread türü sonucu değiştirmez. D ise en tehlikeli cevaptır, çünkü mülakatta abartı olarak duyulur: sanal thread bir hızlandırıcı değildir. Sözlü cevapta bu ayrım tek cümleyle kurulur — kazanç hesapta değil, beklemededir (`49`).

# gncvt-2 | İleri
## Soru
Bir ekip sanal thread'leri 200 kapasiteli sabit bir havuzda tutup görevleri oraya gönderiyor. En yerinde eleştiri hangisidir?
## A
Havuz pahalı kaynağı paylaştırmak içindir; sanal thread ucuzdur ve havuz eşzamanlılığa yapay tavan koyar
🤖 Doğru: havuzlama, sanal thread'in ucuzluk avantajını geri alır.
## B
Havuz boyu küçüktür; iki bine çıkarılırsa yaklaşım doğru biçimde çalışmaya başlar
🤖 sayı değil, yaklaşım yanlıştır; tavanı büyütmek mantığı düzeltmez.
## C
Sanal thread'ler havuz içinde çalışamaz; bu kod çalışma anında hata fırlatır
🤖 kod çalışır; sorun hata değil, kazancın boşa çıkmasıdır.
## D
Havuzlama doğrudur; sanal thread'ler de platform thread'ler gibi pahalı olduğundan yeniden kullanılmalıdır
🤖 sanal thread pahalı değildir; yeniden kullanım gerekçesi ortadan kalkar.
## Doğru
A
## Açıklama
Havuzun varlık sebebi **pahalı bir kaynağı paylaştırmaktır**. Platform thread pahalıdır: işletim sistemi thread'iyle bire bir eşleşir ve yaklaşık bir megabayt yığın tutar; bu yüzden havuzlanır. Sanal thread ise birkaç yüz bayttır, ucuz ve tek kullanımlıktır. Onu havuza koymak iki şeyi birden yapar: oluşturma maliyetinden tasarruf etmez, çünkü zaten maliyet yoktur; buna karşılık eşzamanlılığa 200'lük bir **yapay tavan** koyar. Doğru kalıp görev başına bir thread'dir (`56`, `49`). B şıkkı tavanı büyütmeyi önerir ama mantığı düzeltmez; sayıyı çekirdek ya da havuz boyuna bağlamak platform thread mantığıdır. C kavram hatasıdır: kod çalışır, yalnız kazanç doğmaz. D ise maliyet varsayımını tersine çevirir. Sözlü cevapta bu soru tek cümleyle kapatılır: sanal thread havuzlanmaz, açılır.

# gncvt-3 | İleri
## Soru
JDK 24 ve JEP 491'den sonra sanal thread'lerde sabitlenme (pinning) konusunda hangi ifade doğrudur?
## A
Sabitlenme tamamen bitti; JDK 24 sonrasında hiçbir çağrı taşıyıcıyı işgal etmez
🤖 native/JNI ve FFM sınırı sürüyor; mutlak ifade yanlıştır.
## B
`synchronized` dilden kaldırıldığı için sabitlenmenin kaynağı da tümüyle ortadan kalktı
🤖 `synchronized` kaldırılmadı; yalnız sabitleme davranışı düzeltildi.
## C
Hiçbir şey değişmedi; `synchronized` içinde bloklamak taşıyıcıyı hâlâ sabitliyor
🤖 asıl değişiklik tam olarak bu sabitlemenin kalkmasıdır.
## D
`synchronized` artık sabitlemiyor; native/JNI ve FFM downcall'ları hâlâ sabitliyor
🤖 Doğru: biri düzeldi, diğeri yapısal nedenle sürüyor.
## Doğru
D
## Açıklama
Sabitlenme, sanal thread'in bloklayan bir işlemde taşıyıcıdan **inememesi**, yani onu işgal etmeye devam etmesidir. JDK 21–23'te en yaygın kaynağı `synchronized` içinde bloklamaktı ve sıcak yollarda `ReentrantLock`'a geçmenin asıl gerekçesi buydu. **JEP 491, JDK 24 ile** monitör sahipliğini inme ile uyumlu hâle getirdi: `synchronized` içinde bloklamak artık sabitlemiyor. `synchronized` ne kaldırıldı ne yasaklandı, bu yüzden B ve C yanlıştır (`57`). A ise fazla iyimserdir: **native/JNI çağrıları ve FFM downcall'ları hâlâ sabitler**. Sebep yapısaldır; JVM, Java olmayan bir yığın çerçevesi üzerindeki thread'i güvenle askıya alıp devam ettiremez. Pratik sonuç şudur: yalnız sabitlenmeden kaçınmak amacıyla yapılacak `synchronized` → `ReentrantLock` göçü artık şart değildir. `tryLock`, zaman aşımı ve adillik gibi ihtiyaçlar `ReentrantLock` için hâlâ geçerli ama ayrı gerekçelerdir (`47`).

# gncvt-4 | İleri
## Soru
Yapısal eşzamanlılığın (structured concurrency) çözmeyi hedeflediği asıl sorun nedir?
## A
Tek çekirdekte yapılan hesabı komut sırasını değiştirerek daha hızlı bitirmek
🤖 amaç ham hız değildir; konu görev yaşam döngüsüdür.
## B
Alt görevleri kapsama bağlayıp birlikte bitmelerini ve gerektiğinde birlikte iptal edilmelerini sağlamak
🤖 Doğru: sızan görevi ve dağınık iptali önleyen model budur.
## C
Çöp toplama duraklamalarını kısaltıp uygulamanın bellek ayak izini küçültmek
🤖 çöp toplamayla ilgisi yoktur; farklı bir katmandır.
## D
Veri tabanı sorgularını paralel çalıştıran özel bir sürücü katmanı sağlamak
🤖 veri tabanına özgü bir mekanizma değil, genel bir eşzamanlılık modelidir.
## Doğru
B
## Açıklama
Klasik `ExecutorService` ile başlatılan alt görevler, onları başlatan koddan **bağımsız yaşar**. Hata yayılımı belirsizdir, kimin kimi iptal edeceği yazılı değildir ve görevler sızabilir. Yapısal eşzamanlılık bu dağınıklığı kaldırır: alt görevler `StructuredTaskScope` içinde çatallanır, her biri kendi sanal thread'inde başlar ve `join()` hepsinin bitmesini bekleyen bir bariyer olarak durur. Blok kapanmadan hiçbir alt görev dışarıda kalmaz; iptal kapsamdan alt görevlere doğru yayılır (`58`, `50`). A, C ve D konunun dışındadır: amaç ne hız ne bellek ne de veri tabanıdır. Sözlü cevapta bir de sürüm boyutu vardır ve doğru söylenmesi gerekir: yapısal eşzamanlılık **Java 25'te hâlâ önizlemededir**, önizleme bayrağı gerektirir ve API sürümler arasında değişebilir. Üçüncü parti bir kütüphane değil, JDK'nin parçasıdır.

# gncvt-5 | Uzman
## Soru
Milyonlarca sanal thread açan bir serviste istek bağlamını taşımak için `ScopedValue`, `ThreadLocal`'a neden tercih edilir?
## A
Değişmezdir ve kapsam-bağlıdır; kapsam bitince kendiliğinden geçersizleşir, thread başına kopya yükü doğurmaz
🤖 Doğru: değişmezlik ve kapsam bağı hem sızıntıyı hem bellek yükünü kaldırır.
## B
`ThreadLocal` sanal thread'de derlenmediği için geriye tek seçenek olarak `ScopedValue` kalır
🤖 `ThreadLocal` çalışır; sorun yasak değil, ölçekteki maliyettir.
## C
`ScopedValue` değeri her katmandan değiştirilebilir kıldığından bağlamı güncellemek kolaylaşır
🤖 tam tersi; `ScopedValue` değiştirilemezdir, bu onun tasarım kararıdır.
## D
`ThreadLocal` sanal thread'de otomatik olarak tüm thread'lerin paylaştığı tek değere dönüşür
🤖 böyle bir dönüşüm yoktur; her thread yine kendi değerini tutar.
## Doğru
A
## Açıklama
`ThreadLocal` her thread'e değerin kendi kopyasını verir. Havuz tabanlı dünyada bunun klasik bedeli sızıntıdır: havuz thread'i uzun yaşar, `remove()` çağrılmazsa değer haritada asılı kalır ve bir sonraki isteğe eski bağlam sızabilir (`55`). Sanal thread'e geçince bu bedel kalkar, çünkü thread havuzlanmaz; her görev için doğar ve ölür. Ama yerine yeni bir bedel gelir: **milyonlarca thread, milyonlarca kopya demektir** ve bu ciddi bir bellek yüküdür (`56`). `ScopedValue` tam bu boşluğu doldurur; değiştirilemez (immutable) ve sınırlı ömürlüdür, değer yalnız belirli bir çağrı ağacında görünür ve kapsam bitince kendiliğinden geçersizleşir. Bu yüzden `remove()` diye bir adımı yoktur. B, C ve D üç ayrı kavram hatasıdır. Sürüm notu da doğru verilmelidir: `ScopedValue` Java 21'de önizleme olarak geldi ve **Java 25'te kalıcılaştı** — aynı şemsiyedeki yapısal eşzamanlılık ise hâlâ önizlemededir.

# gncvt-6 | Uzman
## Soru
Sanal thread'e geçen bir serviste iş hacmi beklendiği gibi artmadı. İşlemci kullanımı düşük, taşıyıcı thread'ler sürekli meşgul. İlk adım ne olmalıdır?
## A
Bellek ayarını (`-Xmx`) artırıp ölçümü aynı yük altında yeniden almak
🤖 bu bir eşzamanlılık sorunudur; yığın boyutu sonucu değiştirmez.
## B
Tüm `synchronized` bloklarını topluca `ReentrantLock` kilitlerine çevirmek
🤖 ölçmeden yapılan toplu göç tahmindir; JDK 24 sonrası gerekçesi de zayıftır.
## C
Yük altında JFR kaydı alıp `jdk.VirtualThreadPinned` olayını incelemek
🤖 Doğru: olay, sabitlenmenin olduğu andaki çağrı yığınını verir.
## D
Sanal thread sayısını çekirdek sayısıyla sınırlayan sabit bir havuz kurmak
🤖 bu platform thread mantığıdır; eşzamanlılığı daha da kısar.
## Doğru
C
## Açıklama
Belirtiler klasik bir **sabitlenme** tablosudur: taşıyıcılar meşgul ama işlemci kullanımı düşük. Bu kombinasyon önemlidir, çünkü sabitlenme çoğu zaman işlemciyi doldurmaz; thread'ler beklerken taşıyıcıları işgal eder ve kuyruktaki sanal thread'ler sıra bulamaz. Taşıyıcı sayısı tipik olarak çekirdek sayısı kadar olduğundan birkaç sabitlenme bütün ölçeği çökertebilir (`57`). Doğru teşhis aracı **JDK Flight Recorder**'dır: yük altında kayıt açılır ve `jdk.VirtualThreadPinned` olayı, sabitlenmenin olduğu andaki çağrı yığınını verir. Böylece sabitleyen çağrı — JDK 24 sonrasında genellikle bir native/JNI ya da FFM sınırı — doğrudan görünür. A yanlış katmana bakar; bellek ayarı eşzamanlılık sorununu çözmez. B ölçmeden yapılan toplu bir değişikliktir ve JDK 24 sonrasında gerekçesi de zayıflamıştır. D ise sanal thread'i platform thread gibi yönetir ve eşzamanlılığı daha da kısar. Kural aynıdır: önce ölç, sonra değiştir (`64`).
