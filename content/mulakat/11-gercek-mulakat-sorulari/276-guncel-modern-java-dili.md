---
slug: guncel-modern-java-dili
baslik: "Sözlü Prova · Modern Java Dili: Record, Sealed ve Pattern Matching Üçlüsü"
kademe: Orta
sira: 276
etiketler: 11
---

## Konu anlatımı

Bu dosya `37`, `38` ve `39`'daki bilgiyi tekrar etmez. Oradaki konular dili öğretir; burada aynı bilgi sözlü mülakat cevabına dönüştürülür. Beş açık uçlu soru var, hepsi şıksız. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, cevabın konuşma sırası, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı, çünkü senin adına deneyim uydurulmaz.

Bu üç konunun 2026 mülakatlarında özel bir yeri var. Çoğu aday bunları duymuştur ama üretimde kullanmamıştır; görüşmeci de bunu bilir. O yüzden ölçtüğü şey genelde şudur: ezber mi, yoksa neyi çözdüğünü anlamış mı. Dosyanın ekseni tek bir cümlede toplanır: bu üçlünün asıl değeri tek tek özellikler değil, birlikte verdikleri **tam kapsama (exhaustiveness)** güvencesidir.

### "Record nedir, ne zaman kullanırsın? DTO yerine geçer mi?"

**Soru aslında ne ölçüyor?**

Bu soru tanım gibi görünür ama tanımı herkes söyler. Görüşmecinin ayrım aradığı yer ikinci yarıdır: adayın record'u nereye koyup nereye koymadığı. Zayıf cevap "daha az kod yazarım" der ve orada biter. Güçlü cevap record'un bir **değer taşıyıcı** olduğunu, dolayısıyla kimliği ve yaşam döngüsü olan nesnelerin yerine geçemeyeceğini söyler. Ağırlık muhakemededir; sığ değişmezlik tuzağını kendiliğinden anan aday burada öne çıkar.

**Sözlü cevap iskeleti**

1. **Amacı ilk cümlede söyle.** Record, bir değeri kimliğiyle değil taşıdığı değerle temsil eder. Az kod yazmak amaç değil, sonuçtur (`37`).
2. **Derleyicinin ne ürettiğini say.** Asıl kurucu, bileşen adıyla erişimci, değere dayalı `equals` ve `hashCode`, okunur `toString`. Alanlar örtük `private final`; setter yoktur (`37`).
3. **Değişmezliğin sığ olduğunu sen söyle.** Bileşen referansı yeniden atanamaz. Ama bileşen `List` ya da dizi ise içi dışarıdan değişebilir, çünkü record gelen referansı kopyalamaz. Derin değişmezlik için compact constructor'da savunmacı kopya (defensive copy) alınır (`42`).
4. **DTO sorusunu ikiye ayır.** Sınır dışına çıkan veri için record güçlü bir seçimdir: istek gövdesi, yanıt gövdesi, olay yükü. Kalıcılık katmanı ayrı bir dünyadır.
5. **JPA varlığı olmama gerekçesini teknik ver.** Hibernate parametresiz bir yapıcı ister ve tembel yükleme için sınıftan bir vekil nesne (proxy) türetir; record örtük `final`'dır ve alanları yeniden atanamaz. Ayrıca varlıkta eşitlik satır kimliğine bağlıdır, record'da ise tüm bileşenlere (`81`).
6. **Sınırı da söyle.** Record başka bir sınıftan `extends` yapamaz. Buna karşılık arayüz uygulayabilir, `static` ve örnek metot içerebilir, jenerik olabilir (`37`).

```svg
<svg viewBox="0 0 660 312" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Record ne verir, nerede durur, nerede durmaz">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Record: ne verir, nerede durur, nerede durmaz</text>
  <rect x="14" y="36" width="200" height="196" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="114" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Derleyici üretir</text>
  <text x="114" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">asıl kurucu (canonical)</text>
  <text x="114" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bileşen adıyla erişimci: x()</text>
  <text x="114" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">değere dayalı equals/hashCode</text>
  <text x="114" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">okunur toString</text>
  <text x="114" y="166" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">alanlar örtük private final</text>
  <text x="114" y="186" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">setter üretilmez</text>
  <text x="114" y="212" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">Java 16'da kalıcılaştı</text>
  <rect x="228" y="36" width="204" height="196" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Sığ değişmezlik tuzağı</text>
  <text x="330" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">referans yeniden atanamaz</text>
  <text x="330" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ama listenin içi değişebilir</text>
  <text x="330" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">gelen referans kopyalanmaz</text>
  <text x="330" y="146" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">çözüm: compact constructor'da</text>
  <text x="330" y="164" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">List.copyOf ya da clone</text>
  <text x="330" y="190" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">dizi bileşeninde equals içeriğe</text>
  <text x="330" y="208" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">değil referansa bakar</text>
  <rect x="446" y="36" width="200" height="196" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="546" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">JPA varlığı olmaz</text>
  <text x="546" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">parametresiz yapıcı yok</text>
  <text x="546" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">örtük final: vekil türetilemez</text>
  <text x="546" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">alanlar final: yazılamaz</text>
  <text x="546" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eşitlik satır kimliğine bağlı</text>
  <text x="546" y="168" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">DTO ve olay yükünde güçlü</text>
  <text x="546" y="188" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">API sözleşmesinde yerinde</text>
  <text x="546" y="212" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">kalıcılıkta yanlış araç</text>
  <rect x="14" y="244" width="632" height="58" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="266" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Ayrım tek cümlede</text>
  <text x="330" y="288" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Record bir değer taşıyıcıdır; kimliği olan ve yaşam döngüsü yönetilen nesnenin yerine geçmez.</text>
</svg>
```

**Elenme sebepleri**

- "Record, kod kısaltan bir şeker." Amacı anlamadığını gösterir; değer semantiği hiç kurulmamış olur.
- Record'u tamamen değişmez sanmak. Sığ değişmezlik tuzağını bilmemek bu soruda en görünür eksiktir.
- "Entity olarak da kullanırım" demek. Parametresiz yapıcı ve vekil nesne gerekçesi bilinmiyorsa cevap havada kalır.
- Erişimciyi `getAd()` sanmak. Record'da erişimcinin adı bileşenin kendi adıdır; küçük ama tanıdık bir işaret.
- Değere dayalı eşitliği hiç anmamak. Record'u güvenli `HashMap` anahtarı yapan tam olarak bu davranıştır.
- Doğrulamayı ayrı bir metoda koymak. Compact constructor'ı bilmeyen aday geçersiz nesnenin doğmasına izin verir.
- Sürüm iddiasında ezberden konuşmak. Emin değilsen sürüm söyleme; ne işe yaradığını anlat.

**Kendi hikâyen / dürüst köprü**

Bu soruda küçük bir gözlem bile yeterlidir. Şu sorular onu çıkarır:

- Çalıştığın projede veri taşıyan sınıflar nasıl yazılmış? Elle yazılmış getter'lar mı, üreteç mi, record mu?
- Hiç record'a çevirdiğin bir sınıf oldu mu? Çevirirken hangi şey seni durdurdu?
- İçinde `List` tutan bir veri sınıfı gördün mü? O listeyi dışarıdan değiştiren bir kod var mıydı?
- Aynı veriyi hem varlık hem DTO olarak taşıyan bir yer var mı? Bu ikisini ayırmak sana ne kazandırdı?
- Eşitliği elle yazılmış bir sınıfta hata gördün mü? Belirti neydi: kümede kaybolan nesne mi, yanlış eşleşme mi?

Üretim deneyimin yoksa uydurma. `256`'daki dürüst köprüyü kur: "üretimde bir varlığı record'a çevirmedim" de, sonra en yakın gerçek deneyimi bağla. Kendi projende bir DTO'yu record'a çevirip farkı görmek de gerçek bir gözlemdir ve mülakatta dürüstçe öyle anlatılır.

### "Sealed sınıf ne işe yarar?"

**Soru aslında ne ölçüyor?**

Bu dosyanın en ayrıştırıcı sorusu budur. Çoğu aday "kalıtımı sınırlar" der ve durur. Cümle doğrudur ama yüzeysel kalır, çünkü soruyu "ne kısıtlar" düzeyinde bırakır. Görüşmecinin aradığı cevap bir adım ötededir: bu kısıt karşılığında **ne kazanılır**. Kazanç derleme zamanı bir ispattır. Bunu söyleyebilen aday, dil özelliğini bir tasarım aracı olarak gördüğünü kanıtlar.

**Sözlü cevap iskeleti**

1. **Kısıtı bir cümlede geç.** `sealed`, bir tipi kimin genişletebileceğini `permits` ile kapalı bir listeye kilitler. Paket erişimiyle ya da metot geçersiz kılmayla ilgisi yoktur (`38`).
2. **Hemen karşılığına atla.** Asıl kazanılan şey kısıt değil bilgidir: derleyici artık bu tipin kaç farklı şey olabileceğini bilir.
3. **Tam kapsamayı adlandır.** `switch` tüm izinli alt türleri kapsıyorsa eksiksiz (exhaustive) sayılır ve `default` dalına gerek kalmaz (`39`).
4. **Asıl değeri zaman içinde göster.** `permits` listesine yeni bir tür eklenip `switch` güncellenmezse derleme hatası alırsın. Yani "yeni hâl eklendi ama işlenmedi" hatası üretime çıkmadan yakalanır (`38`).
5. **Üçlüyü burada birleştir.** `sealed` kümeyi kapatır, `record` her alternatifi değişmez ve yıkımlanabilir yapar, `switch` deseni her alternatifi bileşenlerine ayırarak işler. Sonucun adı derleyici-denetimli davranış dağıtımıdır (`38`, `37`, `39`).
6. **Alt tür kuralını ekle.** İzinli her alt tür `final`, `sealed` ya da `non-sealed` olmak zorundadır. Bu şart olmasaydı kapalılık alt daldan sızar ve ispat çökerdi (`38`).
7. **Bedelini söyle.** Kapalı hiyerarşi dışarıya açık bir eklenti noktası vermez. Kütüphane kullanıcısının kendi türünü eklemesini istiyorsan `sealed` yanlış araçtır.

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Uclu: sealed kumeyi kapatir, record yikimlar, switch esler; sonuc tam kapsama">
  <defs>
    <marker id="gncdil-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üçlünün değeri tek tek değil, birlikte ortaya çıkar</text>
  <rect x="14" y="36" width="196" height="118" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">sealed</text>
  <text x="112" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">olası tür kümesini kapatır</text>
  <text x="112" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">permits listesi sözleşmedir</text>
  <text x="112" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">alt tür: final/sealed/non-sealed</text>
  <text x="112" y="142" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">Java 17'de kalıcılaştı</text>
  <line x1="212" y1="95" x2="228" y2="95" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncdil-ok)"/>
  <rect x="232" y="36" width="196" height="118" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">record</text>
  <text x="330" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her alternatif değişmezdir</text>
  <text x="330" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her hâl kendi verisini taşır</text>
  <text x="330" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bileşenler yıkımlanabilir</text>
  <text x="330" y="142" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">enum'un veremediği şey budur</text>
  <line x1="430" y1="95" x2="446" y2="95" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncdil-ok)"/>
  <rect x="450" y="36" width="196" height="118" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="548" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">switch deseni</text>
  <text x="548" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tip testi ve bağlama birlikte</text>
  <text x="548" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bileşenler doğrudan bağlanır</text>
  <text x="548" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">döküm satırı ortadan kalkar</text>
  <text x="548" y="142" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">Java 21'de kalıcılaştı</text>
  <rect x="14" y="168" width="632" height="72" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="330" y="190" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Sonuç: tam kapsama (exhaustiveness) — derleyicinin verdiği ispat</text>
  <text x="330" y="212" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Tüm alt türler kapsandıysa default dalına gerek kalmaz; küme kapalı olduğu için başka ihtimal yoktur.</text>
  <text x="330" y="230" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">permits listesine yeni tür eklenir ve switch güncellenmezse derleme hatası alınır.</text>
  <rect x="14" y="252" width="632" height="64" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="274" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Yalnız "sealed kalıtımı sınırlar" demek neden yetmez</text>
  <text x="330" y="295" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">O cümle kısıtı anlatır, karşılığını anlatmaz. Kısıtın bedeli ödenir ama kazancı söylenmemiş olur.</text>
  <text x="330" y="311" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">default yazmak da aynı kazancı siler: unutulan hâl sessizce o dala düşer.</text>
</svg>
```

**Elenme sebepleri**

- "Kalıtımı sınırlar" deyip durmak. Doğru ama eksik; kısıtın karşılığı söylenmediği için cevap yüzeysel duyulur.
- Tam kapsamayı hiç anmamak. Sealed'in var oluş sebebi budur; atlanırsa geri kalan her şey ayrıntı kalır.
- `sealed` ile `final`'ı karıştırmak. `final` hiçbir alt türe izin vermez, `sealed` seçili bir kümeye izin verir.
- Alt tür kuralını bilmemek. `final`, `sealed` ya da `non-sealed` şartı olmadan kapalılık alt daldan sızar.
- Sealed'i performans iyileştirmesi sanmak. Kazanç çalışma zamanında değil, derleme zamanındadır.
- Kapalılığın bedelini hiç söylememek. Dışarıya açık eklenti noktası isteyen bir tasarımda sealed yanlış tercihtir.
- `permits`'i erişim kısıtı sanmak. Kimin çağırabileceğini değil, kimin genişletebileceğini belirler.

**Kendi hikâyen / dürüst köprü**

Bu soruda hikâyeyi "unutulan hâl" ekseninde ara:

- Bir `switch` ya da `if` zincirine yeni bir hâl eklenip bir yerde işlenmediği oldu mu? Hata nasıl ortaya çıktı?
- Kod tabanında `default` dalında `throw new IllegalStateException` gördün mü? O satır neyi yakalamak için konmuştu?
- Bir durum kodunu (`String` ya da `int`) hâl olarak taşıyan bir kod var mı? Yeni bir kod eklendiğinde ne oldu?
- Visitor deseni ya da uzun `instanceof` zinciri gördün mü? Onu okumak neden zordu?
- Hiç kapalı bir hâl kümesi tasarladın mı? Kapalı tutmaya nasıl karar verdin?

Deneyimin yoksa uydurma. `256`'daki köprü burada çok iyi işler: "sealed'i üretimde kullanmadım" de, sonra "ama enum ve `switch` ile hâl yöneten bir kodda unutulan dal sorununu yaşadım" diyerek gerçek deneyimi bağla. Aradaki farkı da sen adlandır: orada hatayı çalışma zamanında gördün, sealed ile derleyici gösterirdi.

### "Pattern matching ne getirdi?"

**Soru aslında ne ölçüyor?**

Görüşmeci burada iki şeye bakar. Birincisi, adayın hangi tekrarın kalktığını tam olarak söyleyip söyleyemediği. İkincisi, okunurluk kazancını somutlayıp somutlayamadığı. "Kod daha temiz oluyor" cümlesi ölçülemez; "test, döküm ve atama üçlüsü tek ifadeye indi" cümlesi ölçülebilir. `null` davranışını kendiliğinden anmak ise bu soruda güçlü bir işarettir, çünkü çoğu aday orayı atlar.

**Sözlü cevap iskeleti**

1. **Kalkan tekrarı adlandır.** Eski kalıp üç adımdı: `instanceof` ile test, açık döküm, atama. Yeni hâli tek ifadedir ve döküm satırı tamamen kalkar (`39`).
2. **Bağlamanın ne olduğunu doğru söyle.** Desen değişkeni bir kopya değildir; aynı nesneye doğru tiple bakan bir referanstır. Test başarısızsa değişken `null` olmaz, hiç var olmaz.
3. **`switch` üzerindeki tip desenine geç.** `case String s ->` ile tür üzerinden dallanılır. `when` guard'ı, desen tuttuktan sonra değerlendirilen ek bir koşuldur (`39`).
4. **Sırayı anlat.** Dar dallar genel dallardan önce gelmelidir; aksi halde ulaşılamayan dal derleme hatası verir. Bu kural okunurluğu değil doğruluğu korur.
5. **Record ayrıştırmayı ekle.** `case Nokta(int x, int y) ->` hem tip testi yapar hem bileşenleri yerel değişkenlere bağlar. Desenler iç içe geçebilir (`37`, `39`).
6. **`null` dalını sen aç.** Tip desenleri `null` ile eşleşmez. `case null` yazılmamışsa `switch` yine `NullPointerException` fırlatır. Yani `null` denetimi bildirimsel biçimde `switch`'in içine taşınabilir, ama kendiliğinden gelmez (`39`).
7. **`sealed` ile birleştir.** Asıl kazanç burada tamamlanır: kapalı küme üzerinde desen eşleştirmesi eksiksizlik ispatına dönüşür (`38`).

**Elenme sebepleri**

- "Kod daha okunur oluyor" deyip somutlamamak. Hangi satırın kalktığı söylenmezse cevap izlenim düzeyinde kalır.
- Desen değişkenini kopya sanmak. Aynı nesneye tip bilgisiyle bağlanmadır; kopya olsaydı davranış tamamen değişirdi.
- Test başarısız olunca değişkenin `null` olduğunu söylemek. Değişken kapsam dışıdır, `null` değildir.
- `null` konusunu hiç anmamak. Tip desenlerinin `null` ile eşleşmediği bilinmezse üretimde sürpriz bir istisna doğar.
- Guard yerine `case` içine `&&` yazmak. Geçerli bir söz dizimi değildir; koşul `when` ile bağlanır.
- Dal sırasını önemsiz sanmak. Genel dal önce yazılırsa dar dal ulaşılamaz olur ve derleme kırılır.
- Record ayrıştırmanın yeni nesne ürettiğini sanmak. Var olan değer yerinde bileşenlerine ayrılır.

**Kendi hikâyen / dürüst köprü**

Bu soruda hikâye çoğu zaman eski koddan çıkar:

- Kod tabanında uzun bir `instanceof` zinciri var mı? Kaç dal, kaç döküm satırı?
- O zincirde hiç `ClassCastException` alındı mı? Nasıl fark edildi?
- Bir nesnenin alanlarını okuyup dallanan bir metot yazdın mı? Record ayrıştırma orada ne kazandırırdı?
- `switch`'e `null` geldiği bir yer oldu mu? Kontrolü nereye koymuştunuz?
- Java sürümünüz bu özellikleri destekliyor mu? Desteklemiyorsa yükseltmeyi engelleyen ne?

Deneyimin yoksa uydurma. `256`'daki köprüyle şunu diyebilirsin: "Bu söz dizimini üretim kodunda yazmadım, ama şu metotta üç dallı bir `instanceof` zinciri vardı; desen eşleştirmesi orada döküm satırlarını kaldırırdı." Bu cümle hem dürüsttür hem de konuyu anladığını kanıtlar.

### "Bu üçlüyü birlikte ne zaman kullanırsın?"

**Soru aslında ne ölçüyor?**

Bu soru artık dili değil, kararı ölçer. Görüşmeci bir kullanım örneği ister ama asıl duymak istediği şey seçim gerekçesidir: neden `enum` değil, neden klasik kalıtım hiyerarşisi değil. Kod yazmaya kalkmak bu soruda genellikle zarar verir; sözlü mülakatta kod anlatmak dinleyiciyi yorar. Modeli ve kararı anlatmak yeterlidir. En temiz örnek sonlu bir sonuç ya da olay modelidir.

**Sözlü cevap iskeleti**

1. **Somut bir model seç ve üç hâlini say.** Ödeme sonucu iyi bir örnektir: başarılı, reddedildi, beklemede. Küme kapalıdır ve dördüncü hâl yoktur.
2. **Her hâlin farklı veri taşıdığını göster.** Başarılı bir işlem numarası taşır, reddedilme bir kod ve mesaj taşır, beklemede bir tekrar anı taşır.
3. **`enum` neden yetmiyor, onu söyle.** `enum` sabitleri tek örnektir ve hepsi aynı alan kümesini paylaşır. Hâle özgü veriyi ancak boş bırakılan alanlarla taşırsın; bu da her okuyanı `null` denetimine mahkûm eder.
4. **Kalıtım hiyerarşisi neden değil, onu söyle.** Açık hiyerarşide alt tür kümesi sonsuzdur. Derleyici "kaç ihtimal var" sorusunu cevaplayamaz, dolayısıyla eksik dalı da yakalayamaz (`38`).
5. **Kararı tek cümlede bağla.** Hâller sonluysa ve her biri kendi verisini taşıyorsa `sealed` artı `record` doğru araçtır; üstüne desen eşleştirmesi eksiksizliği ispatlar.
6. **Sınırı ekle.** Hâl kümesi zamanla açık kalmalıysa ya da dışarıdaki kod kendi hâlini eklemeliyse bu desen yanlıştır. Kapalılık bir güvence değil, bir taahhüttür.

```svg
<svg viewBox="0 0 660 322" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Karar: enum mu, kalitim hiyerarsisi mi, sealed artik record mu">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kapalı bir hâl modeli için üç seçenek ve seçim ölçütü</text>
  <rect x="14" y="36" width="196" height="168" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="112" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">enum</text>
  <text x="112" y="80" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">hâller sabit ve veri taşımıyor</text>
  <text x="112" y="100" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">switch eksiksiz olabilir</text>
  <text x="112" y="124" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">her sabit tek örnektir</text>
  <text x="112" y="144" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">hâle özgü alan veremezsin</text>
  <text x="112" y="164" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">boş alanlar null denetimi doğurur</text>
  <text x="112" y="188" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">etiket için doğru, veri için değil</text>
  <rect x="232" y="36" width="196" height="168" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">açık kalıtım hiyerarşisi</text>
  <text x="330" y="80" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">dışarıdan yeni tür eklenebilir</text>
  <text x="330" y="100" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">eklenti noktası gerekiyorsa doğru</text>
  <text x="330" y="124" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">alt tür kümesi sonsuzdur</text>
  <text x="330" y="144" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">derleyici eksik dalı yakalayamaz</text>
  <text x="330" y="164" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">default her hatayı sessizce yutar</text>
  <text x="330" y="188" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">genişleme için doğru, ispat için değil</text>
  <rect x="450" y="36" width="196" height="168" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="548" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">sealed + record</text>
  <text x="548" y="80" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">hâller sonlu ve kapalı</text>
  <text x="548" y="100" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">her hâl kendi verisini taşır</text>
  <text x="548" y="120" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">switch eksiksizliği ispatlanır</text>
  <text x="548" y="140" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">yeni hâl derlemeyi kırar</text>
  <text x="548" y="164" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">bedeli: dışa açık eklenti yok</text>
  <text x="548" y="188" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kapalılık bir taahhüttür</text>
  <rect x="14" y="216" width="632" height="94" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="238" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Örnek: ödeme sonucu modeli</text>
  <text x="330" y="260" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Basarili → işlem numarası · Reddedildi → hata kodu ve mesaj · Beklemede → tekrar denenecek an</text>
  <text x="330" y="280" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Üç hâl de farklı veri taşır, bu yüzden enum yetmez; küme kapalıdır, bu yüzden açık hiyerarşi fazla gelir.</text>
  <text x="330" y="300" text-anchor="middle" font-size="10" font-weight="600" fill="#186399" font-family="system-ui">Ölçüt tek cümlede: küme kapalı mı, her hâl kendi verisini taşıyor mu.</text>
</svg>
```

**Elenme sebepleri**

- Tahtaya kod yazmaya kalkmak. Sözlü cevapta model ve gerekçe istenir; söz dizimi zamanı yer, ayrımı göstermez.
- `enum` ile farkı söyleyememek. Bu soruda seçim gerekçesi cevabın kendisidir; örnek onun ambalajıdır.
- Kapalılığın bedelini atlamak. Her tasarım kararında olduğu gibi burada da takas cümlesi beklenir.
- Her yerde bu deseni önermek. Açık kalması gereken bir eklenti noktasında sealed doğrudan zarar verir.
- Örneği hâl sayısı belirsiz bir alandan seçmek. Kapalı küme iddiası örnekle çelişirse cevap kendini bozar.
- Hâllerin farklı veri taşıdığını hiç söylememek. `enum` yetersizliğinin tek somut gerekçesi budur.
- Eksiksizliği çalışma zamanı denetimi sanmak. İspat derleme zamanındadır; söylenmezse kazanç anlaşılmaz.

**Kendi hikâyen / dürüst köprü**

Bu soruda hikâyeyi kendi alanındaki bir sonuç modelinden çıkar:

- Çalıştığın sistemde sonlu hâlli bir akış var mı? Sipariş durumu, ödeme sonucu, başvuru aşaması?
- O hâller nasıl temsil edilmiş? `enum` mu, `String` sabit mi, tam sayı kod mu?
- Hâle özgü veri nerede duruyor? Aynı sınıfta boş bırakılan alanlar var mı?
- Yeni bir hâl eklendiğinde kaç yeri değiştirmek gerekti? Bir yer unutuldu mu?
- Bugün aynı modeli yeniden tasarlasan neyi değiştirirdin ve neden?

Deneyimin yoksa uydurma. `256`'daki köprüyle "bu deseni üretimde kurmadım" demek serbesttir. Ardından mevcut hâl modelini anlatıp "burada `enum` yetmiyor, çünkü reddedilme kendi hata kodunu taşıyor" dersen kararı gösterirsin. Görüşmecinin aradığı şey de tam olarak bu muhakemedir.

### "Sürümünden emin değilsen ne yaparsın?"

**Soru aslında ne ölçüyor?**

Bu soru genelde açıkça sorulmaz; cevaplarının içinde ölçülür. Görüşmeci bir sürüm numarası duyduğunda onu doğrular. Yanlış numara söylemenin bedeli ağırdır, çünkü aday o andan itibaren "emin konuşuyor ama emin olamıyor" kutusuna girer. Ölçülen şey bilgi değil, bilginin sınırını yönetme becerisidir. Bu, `256`'daki "bilmiyorum demek" disiplininin sürüme uygulanmış hâlidir.

**Sözlü cevap iskeleti**

1. **Numarayı atla, işlevi anlat.** "Şu sürümde geldi" demek yerine hangi sorunu çözdüğünü söyle. Görüşmeciler sürüm ezberi değil kavrayış arar.
2. **Emin olduğun kadarını söyle.** "Java 17 sonrası bir sürümde kalıcılaştı, tam numarasını şu an garanti edemem" cümlesi zayıflık değil, doğruluk işaretidir.
3. **Önizleme ile kalıcı arasını ayır.** Bir özellik önizleme (preview) ise öyle söyle. İkisini karıştırmak, projede yanlış planlamaya yol açacak bir hatadır.
4. **Bildiğin sürüm çıpalarını kullan.** Bu dosyada geçen üçü doğrulanmıştır: record Java 16'da, `sealed` Java 17'de, `switch` desenleri ile record ayrıştırma Java 21'de kalıcılaştı (`37`, `38`, `39`).
5. **Kararı sürümden bağımsız kur.** "Hangi Java sürümündesiniz" diye sormak da güçlü bir hamledir; cevap konuşmayı gerçek bağlama taşır (`40`).

**Elenme sebepleri**

- Emin olmadığı numarayı emin bir tonla söylemek. Tek bir yanlış sürüm, sonraki tüm cevapların güvenilirliğini düşürür.
- Önizlemedeki bir özelliği kalıcı gibi anlatmak. Bu hata teknik değil, planlama hatası olarak okunur.
- Her cümleyi sürümle açmak. Sürüm listesi ezberi, kavrayış eksikliğinin bilinen bir örtüsüdür.
- "Bilmiyorum" demekten kaçınıp uydurmak. Uydurulan sayı çoğu zaman ilk takip sorusunda dökülür.
- Sürüm sorusunu tamamen savuşturmak. Hiçbir çıpa veremeyen aday da konuya uzak görünür; bildiğini söyle, bilmediğini işaretle.

**Kendi hikâyen / dürüst köprü**

Burada hikâye değil, hazırlık gerekir:

- Şu an hangi Java sürümünde çalışıyorsun? Bu soru mülakatta sana da sorulur ve cevabı hazır olmalıdır.
- Kullandığın sürüm bu üç özelliği destekliyor mu? Desteklemiyorsa engel ne: kütüphane mi, sunucu mu, karar mı?
- Hiç sürüm yükseltmesi yaşadın mı? Neyi kırdı, neyi kolaylaştırdı?
- Bir özelliğin önizlemede olduğunu fark edip kullanmaktan vazgeçtin mi?

Bu soruda dürüst köprü kurmak en kolay ve en kazançlı yoldur. `256`'daki kalıp aynen geçerlidir: bildiğini söyle, sınırını işaretle, sonra konuşmayı anladığın zemine taşı.

### Bu bölümden çıkan ders

Bu beş sorunun tek bir ekseni var: dil özelliğini tek tek saymak değil, birlikte ne kazandırdıklarını söylemek. Record tek başına tekrar kodu azaltır; `sealed` tek başına kalıtımı sınırlar; desen eşleştirmesi tek başına döküm satırını kaldırır. Üçü bir araya geldiğinde ortaya çıkan şey bambaşkadır: derleyici, hiçbir hâlin unutulmadığını **ispatlar**. Cevabını bu ispat üzerine kurarsan sorunun hangi parçadan girdiği önemini kaybeder. Record'da sığ değişmezlik tuzağını, sealed'de kapalılığın bedelini, desen eşleştirmesinde `null` davranışını kendiliğinden anmak da aynı olgunluğun işaretidir. Sürüm numarasında ise kural nettir: emin değilsen söyleme, ne işe yaradığını anlat. Ve hiçbir yerde olmayan deneyimi uydurma; `256`'daki dürüst köprü her zaman uydurmadan güçlüdür.

# gncdil-1 | Temel
## Soru
Mülakatta "record'u neden kullanırsın?" sorusuna verilecek en yerinde cevap hangi gerekçeye dayanır?
## A
Kod satırı sayısını azaltmak; record'un varlık sebebi daha az yazı yazmaktır
🤖 kısalık bir sonuçtur, gerekçe değil; asıl mesele değer semantiğidir.
## B
Bir veriyi kimliğiyle değil taşıdığı değerle temsil etmek; kısalık bunun yan ürünüdür
🤖 Doğru: record bir değer taşıyıcıdır, tekrar kodun kalkması sonuçtur.
## C
Sınıfı kalıtıma kapatmak; record'un asıl işi hiyerarşiyi sınırlamaktır
🤖 kalıtıma kapalılık record'un bir sınırıdır, kullanım gerekçesi değildir.
## D
Nesne oluşturmayı hızlandırmak; record çalışma zamanında daha ucuz nesne üretir
🤖 record çalışma zamanı maliyetini değiştirmez; kazanç anlam düzeyindedir.
## Doğru
B
## Açıklama
Record'un gerekçesi **anlamdır**, kısalık değil. Bir record, taşıdığı bileşenlerle tanımlanan bir değeri temsil eder; iki record aynı türdense ve tüm bileşenleri eşitse eşittir (`37`). Bu yüzden değere dayalı `equals` ve `hashCode` derleyici tarafından üretilir ve record güvenli bir `HashMap` anahtarı olur. Asıl kurucunun, erişimcilerin ve `toString`'in otomatik gelmesi bu kararın **yan ürünüdür** (A yanlış: sonucu sebep sanar). Kalıtıma kapalı olmak record'un bir sınırıdır, kullanım gerekçesi değildir (C yanlış); record başka sınıftan `extends` yapamaz ama arayüz uygulayabilir. D yanlış, çünkü kazanç çalışma zamanı hızında değil, kodun ifade ettiği anlamdadır. Mülakatta bu ayrımı kurmak, record'u "söz dizimi şekeri" sanan cevaplardan hemen ayrılmanı sağlar.

# gncdil-2 | Orta
## Soru
Bir record'u JPA varlığı (entity) olarak kullanmak istiyorsun. Temeldeki engel hangisidir?
## A
Record arayüz uygulayamadığı için `Serializable` şartı hiçbir biçimde sağlanamaz
🤖 record arayüz uygulayabilir; ayrıca `Serializable` JPA için zorunlu değildir.
## B
Record jenerik olamadığı için depo (repository) tip parametresine verilemez
🤖 record jenerik olabilir; sorun tip parametresinde değildir.
## C
Record'da `static` metot tanımlanamadığı için sorgu yardımcıları yazılamaz
🤖 record `static` ve örnek metot içerebilir; engel burada değildir.
## D
Record'un parametresiz yapıcısı yoktur, örtük `final`'dır ve alanları yeniden atanamaz
🤖 Doğru: Hibernate'in yapıcı, vekil nesne ve alan yazma ihtiyaçlarının üçü birden karşılanmaz.
## Doğru
D
## Açıklama
JPA varlığından beklenen üç şey record'un tanımıyla doğrudan çelişir. Birincisi, Hibernate nesneyi yansımayla (reflection) üretmek için **parametresiz bir yapıcı** ister; record'da yalnız bileşenleri alan asıl kurucu vardır (`81`). İkincisi, tembel yükleme için varlıktan bir vekil nesne (proxy) türetilir; record örtük `final` olduğu için alt sınıflanamaz (`37`). Üçüncüsü, record alanları `final`'dır ve yüklemeden sonra yazılamaz. Buna ek olarak eşitlik semantiği de uyuşmaz: varlıkta kimlik satıra bağlıdır, record'da ise tüm bileşenlere. A, B ve C yanlıştır, çünkü record arayüz uygulayabilir, jenerik olabilir ve `static` metot içerebilir. Doğru kullanım record'u sınır dışına çıkan veride tutmaktır: istek ve yanıt gövdesi, olay yükü, sorgu sonucu yansıtması. Kalıcılık nesnesi ile taşıma nesnesini ayırmak, mülakatta beklenen cevaptır.

# gncdil-3 | Orta
## Soru
`sealed` bir hiyerarşinin mülakatta söylenmesi gereken asıl mühendislik kazancı hangisidir?
## A
Derleyicinin `switch`'in tüm hâlleri kapsadığını ispatlaması; eksik dal derleme hatası olur
🤖 Doğru: kapalı küme, eksiksizliğin derleme zamanında ispatlanmasını sağlar.
## B
Alt tür sayısı sabitlendiği için nesne oluşturma ve bellek kullanımının düşmesi
🤖 sealed bellek ya da hız kazancı vermez; kazanç derleme zamanındadır.
## C
`permits` listesindeki türler dışında hiçbir sınıfın bu tipi çağırmasının engellenmesi
🤖 `permits` çağırma iznini değil, genişletme iznini belirler.
## D
Alt türlerin metotları geçersiz kılmasının (override) engellenmesi
🤖 override'ı `final` metot engeller; sealed hiyerarşiyi kapatır.
## Doğru
A
## Açıklama
"Sealed kalıtımı sınırlar" cümlesi doğrudur ama yalnız **kısıtı** anlatır; mülakatta eksik kalır. Asıl kazanç kısıtın karşılığıdır: hiyerarşi kapalı olduğu için derleyici bu tipin kaç farklı şey olabileceğini bilir. Tüm izinli alt türleri kapsayan bir `switch` **eksiksiz (exhaustive)** sayılır ve `default` dalına gerek kalmaz (`38`, `39`). Değer zaman içinde daha da belirginleşir: `permits` listesine yeni bir tür eklenip `switch` güncellenmezse **derleme hatası** alınır. Yani "yeni hâl eklendi ama işlenmedi" hatası üretime çıkmadan yakalanır. B yanlıştır, çünkü kazanç çalışma zamanında değildir. C `permits`'i erişim kısıtı sanar; oysa kimin genişletebileceğini belirler, kimin çağırabileceğini değil. D ise `final` metodun işini sealed'e yükler. Cevabı bu ispat üzerine kurmak, konuyu ezberden değil tasarım açısından anladığını gösterir.

# gncdil-4 | Orta
## Soru
Sealed bir tip üzerinde tüm alt türleri kapsayan bir `switch` var. Ekip alışkanlıktan bir de `default` dalı ekledi. Bunun bedeli nedir?
## A
Bedeli yoktur; `default` yalnız okunurluğu artırır ve davranışı hiç değiştirmez
🤖 davranışı değiştirir: eksiksizlik denetiminin uyarı gücünü ortadan kaldırır.
## B
`switch` artık derlenmez; sealed bir tipte `default` yazmak derleme hatası verir
🤖 `default` yazmak yasak değildir; yalnız güvenlik ağını devre dışı bırakır.
## C
Yeni bir alt tür eklendiğinde eksik dal sessizce `default`'a düşer, derleyici uyarmaz
🤖 Doğru: `default` varken switch her hâlde eksiksiz görünür ve hata gizlenir.
## D
Desen sırası bozulur; `default` yazılınca guard'lı dallar hiçbir zaman çalışmaz
🤖 `default` en sonda düşünülür; guard'lı dalların çalışmasını engellemez.
## Doğru
C
## Açıklama
Sealed hiyerarşinin en değerli tarafı, unutulan hâli **derleme zamanında** yakalamasıdır. `permits` listesine yeni bir tür eklenir ve `switch` güncellenmezse derleyici hata verir (`38`). `default` dalı bu ağı ortadan kaldırır: artık her ihtimalin bir karşılığı vardır, çünkü yeni tür sessizce `default`'a düşer. Sorun derlemede değil, üretimde ortaya çıkar; genellikle yanlış bir varsayılan davranış olarak. Bu yüzden kapalı hiyerarşilerde `default` yazmamak bilinçli bir tercihtir. `default` yazmak derleme hatası değildir (B yanlış) ve dal sırasını bozmaz (D yanlış); `default` her zaman en sonda düşünülür ve guard'lı dallar önce değerlendirilir (`39`). A ise değişikliği zararsız sanar; oysa değişen şey söz dizimi değil, hatanın ne zaman görüleceğidir. Mülakatta bu ayrımı kurmak, adayın eksiksizliği gerçekten anladığını gösterir.

# gncdil-5 | İleri
## Soru
Bir ödeme sonucu üç hâlden biridir: başarılı (işlem numarası), reddedildi (hata kodu ve mesaj), beklemede (tekrar denenecek an). Bu model için neden `enum` yerine `sealed` artı `record` seçilir?
## A
`enum` üzerinde yazılan `switch` eksiksiz sayılmaz; `default` dalı her durumda zorunludur
🤖 `enum` üzerinde switch eksiksiz olabilir; ayrım burada değildir.
## B
`enum` sabitleri `switch` içinde desen olarak kullanılamaz, yalnız `if` ile karşılaştırılır
🤖 `enum` sabitleri switch içinde kullanılabilir; kısıt bu değildir.
## C
`enum` sınıf yüklenmesini geciktirir; üç sabit için gereksiz bir başlatma maliyeti doğar
🤖 başlatma maliyeti bu kararla ilgisizdir; sorun veri modelindedir.
## D
`enum` sabitleri tek örnektir; her hâlin kendi farklı alanlarını taşımasına elverişli değildir
🤖 Doğru: hâller farklı veri taşıyorsa enum yetmez, sealed artı record gerekir.
## Doğru
D
## Açıklama
Ayrım, hâllerin **veri taşıyıp taşımadığındadır**. `enum` sabitleri tek örnektir ve hepsi aynı alan kümesini paylaşır. Üç hâl farklı veri taşıdığı için `enum` ile modellersen işlem numarası, hata kodu ve tekrar anını aynı sınıfa koyar, kullanılmayanları boş bırakırsın. Sonuç her okuyanı `null` denetimine mahkûm eden kırılgan bir tasarımdır. `sealed interface OdemeSonucu` ile her hâl kendi record'u olur ve yalnız kendi alanlarını taşır (`38`, `37`). Desen eşleştirmesi bu alanları dal içinde doğrudan bağlar, ayrıca derleyici üç hâlin de işlendiğini ispatlar (`39`). Klasik açık kalıtım hiyerarşisi de bu ispatı veremez, çünkü alt tür kümesi sonsuzdur. A yanlıştır: `enum` üzerinde `switch` eksiksiz olabilir. B ve C ise gerçek olmayan kısıtlar öne sürer. Ölçüt tek cümledir: küme kapalı mı ve her hâl kendi verisini taşıyor mu.

# gncdil-6 | İleri
## Soru
Sealed bir `Sonuc` tipi üzerinde tüm alt türleri kapsayan, `default`'suz bir `switch` yazdın. Seçici değişken `null` gelirse ne olur?
## A
İlk tip deseni `null` ile eşleşir ve o dal çalışır; ayrı bir önlem gerekmez
🤖 tip desenleri `null` ile eşleşmez; ilk dal çalışmaz.
## B
`NullPointerException` fırlar; tip desenleri `null` ile eşleşmez, `case null` gerekir
🤖 Doğru: eksiksizlik null'ı kapsamaz, null ayrı bir desen olarak yazılmalıdır.
## C
Derleme hatası alırsın; sealed bir tipte `null` ihtimali derleme zamanında yakalanır
🤖 derleyici null ihtimalini eksiksizlik denetiminde ele almaz.
## D
Hiçbir dal çalışmaz; `switch` ifadesi sessizce `null` sonucunu döndürür
🤖 sessiz bir sonuç üretilmez; çalışma zamanında istisna fırlar.
## Doğru
B
## Açıklama
Eksiksizlik (exhaustiveness) ile `null` güvenliği **ayrı iki konudur** ve mülakatta sık karıştırılır. Sealed bir tipin tüm alt türlerini kapsamak `switch`'i eksiksiz yapar; `default` dalına gerek kalmaz (`38`). Ancak `null` bir alt tür değildir: **tip desenleri `null` ile eşleşmez** (`39`). `case null` yazılmamışsa seçici `null` geldiğinde `switch` çalışma zamanında `NullPointerException` fırlatır. Çözüm iki yoldan biridir: ya `switch`'ten önce `null` denetimi yapılır, ya da `case null ->` dalı yazılarak denetim bildirimsel biçimde `switch`'in içine taşınır. `case null, default ->` birleşimi de mümkündür, ama sealed bir tipte `default` yazmak eksiksizlik güvencesini zayıflatır. A yanlıştır, çünkü ilk tip deseni `null`'u yakalamaz. C yanlıştır, çünkü derleyici `null` ihtimalini eksiksizlik ispatına dahil etmez. D ise sessiz bir sonuç varsayar; oysa davranış açık bir istisnadır.
