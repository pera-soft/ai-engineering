---
slug: dotnet-aspnet-core-cekirdegi
baslik: ".NET ve ASP.NET Core Çekirdeği: DI, Ara Katman, Yapılandırma, Hata Yönetimi ve WCF"
kademe: Orta
sira: 282
etiketler: 11
---

## Konu anlatımı

Bu dosyada beş açık uçlu mülakat sorusu var. Hepsi ASP.NET Core'un çekirdeğini yoklar. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri.

Buradaki yöntem tek cümleyle şudur: **yeni bir şey öğrenmiyorsun, bildiğini taşıyorsun.** Bu beş konunun beşi de Spring tarafında zaten var. Bağımlılık enjeksiyonu (dependency injection) var, filtre zinciri var, profil ve yapılandırma var, `@ControllerAdvice` var, SOAP servisi var. Değişen şey kelime. Bu yüzden her başlıkta Spring karşılığı açıkça kurulur ve Spring tarafı **tekrar anlatılmaz**; ilgili konuya numarayla atıf verilir (`68`, `69`, `75`, `76`, `77`, `78`, `106`).

Görüşmecinin bu ilandaki niyeti de bunu destekler. Adayın C# geçmişini muhtemelen zaten biliyor. Ölçtüğü şey "bu kişi C# ustası mı" değil, "kavramı biliyor mu, dili öğrenir mi". Kavramı doğru anlatan aday, sözdizimi bilmediğini söylese bile geçer. Kavramı bilmeyen aday sözdizimini bilse de kalır.

Son parça bilerek boş bırakıldı. Senin adına C# üretim deneyimi uydurulmaz. "Şu projede şunu kullandım" cümlesi bu dosyada kurulmaz; onun yerine `256`'daki **dürüst köprü** çalıştırılır: dil deneyimini olduğu gibi söyle, sonra aynı kavramın Java'daki gerçek deneyimini bağla.

### "Bağımlılık enjeksiyonu .NET tarafında nasıl çalışır?"

**Soru aslında ne ölçüyor?**

Bu soru bir ısınma sorusu gibi görünür ama iki kademesi vardır. Birinci kademe tanımdır; üç kayıt metodunu sayan herkes geçer. İkinci kademe ömür yönetimidir ve ayrım orada çıkar. Görüşmecinin asıl merak ettiği şu: bu aday kapsam (scope) kavramını Spring'den taşıyabiliyor mu, yoksa ezberlediği üç kelimeyi mi tekrarlıyor. Ağırlık ömür kararındadır.

**Sözlü cevap iskeleti**

1. **Eşlemeyi ilk cümlede kur.** ASP.NET Core'un kabı yerleşiktir; ayrı bir çatı gerekmez. Kayıtlar uygulama açılışında yapılır, çözümleme kurucu enjeksiyonuyla olur. Bu, Spring'in kap davranışıyla aynı fikirdir (`68`).
2. **Üç kaydı Spring karşılığıyla ver.** Spring'de singleton olan, .NET'te `AddSingleton`; istek kapsamı `AddScoped`; prototype `AddTransient`. Fark kavramda değil, kelimededir (`69`).
3. **Kapsamın nerede açıldığını söyle.** ASP.NET Core'da her HTTP isteği için bir kapsam açılır; istek bitince kapsam kapanır. Bu yüzden `AddScoped`, Spring'in istek kapsamının doğal karşılığıdır.
4. **Klasik tuzağı adlandır: kaptif bağımlılık.** Uzun ömürlüye kısa ömürlü enjekte edilir. Spring'de singleton'a prototype, .NET'te singleton'a scoped. Kurucu bir kez çağrıldığı için kısa ömürlü nesne uzun ömürlüye takılır ve bir daha tazelenmez.
5. **İki tarafın farkını burada göster.** Spring'de bu hata **sessizdir**; uygulama açılır, sorun üretimde çıkar. .NET'te kapsam doğrulaması açıkken kap bu kaydı çözerken **çalışma anında hata atar**; geliştirme ortamında bu doğrulama genellikle açıktır. Yani .NET seni erken uyarır.
6. **Çözümü söyle.** Uzun ömürlü sınıf kısa ömürlü bağımlılığı kurucudan almaz; ihtiyaç anında kendisi kapsam açar (`IServiceScopeFactory`). Spring'deki karşılığı kapsam vekili (scoped proxy) ya da sağlayıcı enjeksiyonudur.
7. **Son bir uyarı ekle.** Kabı doğrudan enjekte edip içinden nesne çekmek servis bulucu (service locator) desenidir. Spring'de `ApplicationContext`'i enjekte etmekle aynı kokuyu verir; bağımlılıklar kurucudan görünmez olur.

```svg
<svg viewBox="0 0 660 342" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="NET DI kapsamlari ve Spring karsiliklari, kaptif bagimlilik tuzagi">
  <defs>
    <marker id="aspn-di-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
    <marker id="aspn-di-uyari" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#B23B3B"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kavram aynı, kelime farklı: kapsam eşlemesi</text>
  <rect x="14" y="36" width="204" height="150" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="116" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">AddSingleton</text>
  <text x="116" y="78" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Spring: singleton (varsayılan)</text>
  <text x="116" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">uygulama boyunca tek örnek</text>
  <text x="116" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">durumsuz olmalı, iş parçacığı güvenli</text>
  <text x="116" y="145" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">yapılandırma, önbellek, istemci havuzu</text>
  <text x="116" y="168" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">durum tutarsa yarış koşulu doğar</text>
  <rect x="228" y="36" width="204" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">AddScoped</text>
  <text x="330" y="78" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Spring: request kapsamı</text>
  <text x="330" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her HTTP isteğine bir örnek</text>
  <text x="330" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">istek bitince kapsam kapanır</text>
  <text x="330" y="145" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">veri bağlamı, birim iş, istek verisi</text>
  <text x="330" y="168" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">arka plan işinde kapsam elle açılır</text>
  <rect x="442" y="36" width="204" height="150" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="544" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">AddTransient</text>
  <text x="544" y="78" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Spring: prototype</text>
  <text x="544" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her çözümlemede yeni örnek</text>
  <text x="544" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">en ucuz ömür, en çok ayırma</text>
  <text x="544" y="145" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">hafif, durum tutan yardımcı sınıflar</text>
  <text x="544" y="168" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">pahalı nesnede tercih edilmez</text>
  <rect x="14" y="200" width="632" height="130" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="222" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Klasik tuzak ikisinde de aynı: uzun ömürlüye kısa ömürlü enjekte etmek</text>
  <rect x="30" y="234" width="150" height="40" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.3"/>
  <text x="105" y="250" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">Singleton sınıf</text>
  <text x="105" y="265" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kurucu bir kez çağrılır</text>
  <line x1="182" y1="254" x2="204" y2="254" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#aspn-di-uyari)"/>
  <rect x="206" y="234" width="150" height="40" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="281" y="250" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">Scoped bağımlılık</text>
  <text x="281" y="265" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">içeride takılı kalır</text>
  <line x1="358" y1="254" x2="380" y2="254" stroke="#5D7789" stroke-width="1.4" marker-end="url(#aspn-di-ok)"/>
  <rect x="382" y="234" width="252" height="40" rx="7" fill="#FFFFFF" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="508" y="250" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B23B3B" font-family="system-ui">kaptif bağımlılık (captive dependency)</text>
  <text x="508" y="265" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">nesne bir daha tazelenmez, ömür sızar</text>
  <text x="180" y="294" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B07D20" font-family="system-ui">Spring: sessiz kalır</text>
  <text x="180" y="311" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">uygulama açılır, sorun üretimde çıkar</text>
  <text x="490" y="294" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">.NET: çalışma anında hata verir</text>
  <text x="490" y="311" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">kapsam doğrulaması açıkken kap çözümlemeyi reddeder</text>
</svg>
```

**Elenme sebepleri**

- Üç metodu sayıp durmak. Tanım herkeste var; ömür kararını anlatmayan cevap ısınma seviyesinde kalır.
- Kapsamın nerede açıldığını bilmemek. "Scoped ne demek" sorusuna "orta ömürlü" demek boş bir cevaptır.
- Kaptif bağımlılığı hiç anmamak. Bu, DI sorusunun asıl ayrıştırıcısıdır ve iki ekosistemde de aynıdır.
- Her şeyi singleton kaydetmek ve gerekçesini "daha hızlı" diye vermek. Durum tutan singleton yarış koşulu üretir.
- Kabı doğrudan enjekte edip içinden nesne çekmek. Bağımlılıklar kurucudan görünmez olur, test kurmak zorlaşır.
- Elden çıkarmayı (dispose) hiç düşünmemek. Kap sahiplendiği nesneleri kapsam kapanınca bırakır; kök kapsamda biriken kaynak sızar.
- "Spring'de bunların hiçbiri yok" demek. Tam tersi doğrudur ve bu cümle kavram eşlemesi kuramadığını gösterir.

**Kendi hikâyen / dürüst köprü**

C# tarafında üretim deneyimi anlatma. Bunun yerine kavramın Java'daki gerçek deneyimini bağla. Şu sorular onu çıkarır:

- Hiç singleton bir bean'e istek kapsamlı bir şey enjekte edildiği bir vakada bulundun mu? Belirti neydi?
- Durum tutan bir singleton yüzünden yarış koşulu yaşadın mı? Nasıl fark edildi, nasıl kırıldı?
- Projende bean kapsamı bilinçli seçiliyor mu, yoksa her şey varsayılan mı bırakılıyor?
- `ApplicationContext`'i enjekte edip içinden bean çeken bir kod gördün mü? Neden yazılmıştı?
- Prototype ya da istek kapsamı gerçekten gerektiği bir yer oldu mu? Alternatifi neydi?

Cevabın kalıbı şudur: "Kapsam yönetimini Spring tarafında yıllardır kuruyorum; `AddScoped` ile istek kapsamı aynı fikir. .NET'te ek olarak kap bu hatayı çalışma anında yakalıyor, Spring'de sessiz kalıyor." Bu cümle hem dürüsttür hem bilgi gösterir (`256`).

### "ASP.NET Core'da ara katman (middleware) hattı nedir, sıra neden önemlidir?"

**Soru aslında ne ölçüyor?**

Bu soru "istek bana gelene kadar neyin içinden geçti" sorusudur. Görüşmeci adayın çatıyı bir kara kutu olarak mı gördüğünü ölçer. Ayrım iki yerde çıkar. Birincisi sıranın davranışı belirlediğini bilmek, ikincisi ara katman ile filtreyi ayırabilmek. Java tarafında ikisini yaşamış bir aday bu soruyu rahat geçer.

**Sözlü cevap iskeleti**

1. **Boru hattını tarif et.** Gelen istek, kayıt sırasına göre dizilmiş ara katmanların içinden geçer. Her ara katman kendinden sonrakini `next` ile çağırır; cevap aynı zincirden ters yönde geri döner.
2. **Java karşılığını kur.** Bu, servlet filtresi zincirinin aynısıdır. `chain.doFilter(...)` çağrısı ne işe yarıyorsa `await next(context)` de onu yapar. Spring Security'nin filtre zinciri de aynı yapıdadır (`76`).
3. **Kısa devreyi anlat.** Bir ara katman `next`'i çağırmazsa zincir orada kesilir ve cevap oradan döner. Yetkisiz isteği erken kesmek tam olarak budur.
4. **Sırayı örnekle.** Yaygın düzen şudur: istisna yakalama → HTTPS/statik dosya → yönlendirme → CORS → kimlik doğrulama → yetkilendirme → uç nokta. İstisna katmanı en başta olmalıdır; sonda durursa kendinden öncekinin hatasını göremez. Yetki, kimlikten sonra gelmelidir; kim olduğu bilinmeden ne yapabileceği denetlenemez.
5. **Ara katman ile filtreyi ayır.** Ara katman ham HTTP seviyesindedir; hangi eylemin çalışacağını bilmez. `ActionFilter` ise MVC'nin içindedir, model ve eylem bağlamını görür. Java karşılığı: ara katman ↔ `Filter`, `ActionFilter` ↔ `HandlerInterceptor`. Bu ayrımı yapmak ayrıştırıcıdır.
6. **Ömür tuzağını ekle.** Alışıldık yazımda ara katman sınıfının kurucusu uygulama açılışında **bir kez** çağrılır. Bu yüzden istek kapsamlı bağımlılık kurucuya değil, istek metoduna parametre olarak alınır. Bu, bir önceki başlıktaki kaptif bağımlılığın ta kendisidir.
7. **Ne koyulur, ne koyulmaz.** Bağıntı kimliği (correlation-id), istek günlüğü, hız sınırlama ve istisna dönüşümü ara katmana yakışır. İş kuralı yakışmaz; iş kuralı ara katmanda saklanırsa test edilemez hâle gelir.

```svg
<svg viewBox="0 0 660 328" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ASP.NET Core ara katman hatti: sira, kisa devre ve Java karsiliklari">
  <defs>
    <marker id="aspn-mw-ileri" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#186399"/></marker>
    <marker id="aspn-mw-geri" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#25804C"/></marker>
    <marker id="aspn-mw-kes" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#B23B3B"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kayıt sırası davranışı belirler; cevap aynı zincirden geri döner</text>
  <text x="330" y="44" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">istek soldan sağa iner, cevap sağdan sola çıkar</text>
  <rect x="16" y="56" width="118" height="46" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="75" y="75" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B23B3B" font-family="system-ui">1 · İstisna yakalama</text>
  <text x="75" y="91" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">en dışta olmalı</text>
  <line x1="136" y1="79" x2="152" y2="79" stroke="#186399" stroke-width="1.4" marker-end="url(#aspn-mw-ileri)"/>
  <rect x="154" y="56" width="110" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="209" y="75" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">2 · Yönlendirme</text>
  <text x="209" y="91" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">hangi uç nokta?</text>
  <line x1="266" y1="79" x2="282" y2="79" stroke="#186399" stroke-width="1.4" marker-end="url(#aspn-mw-ileri)"/>
  <rect x="284" y="56" width="110" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="339" y="75" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">3 · Kimlik</text>
  <text x="339" y="91" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">sen kimsin?</text>
  <line x1="396" y1="79" x2="412" y2="79" stroke="#186399" stroke-width="1.4" marker-end="url(#aspn-mw-ileri)"/>
  <rect x="414" y="56" width="110" height="46" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="469" y="75" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B07D20" font-family="system-ui">4 · Yetki</text>
  <text x="469" y="91" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">ne yapabilirsin?</text>
  <line x1="526" y1="79" x2="542" y2="79" stroke="#186399" stroke-width="1.4" marker-end="url(#aspn-mw-ileri)"/>
  <rect x="544" y="56" width="102" height="46" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="595" y="75" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">5 · Uç nokta</text>
  <text x="595" y="91" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">iş kodu burada</text>
  <path d="M595 106 L595 124 L75 124 L75 108" fill="none" stroke="#25804C" stroke-width="1.4" stroke-dasharray="5 3" marker-end="url(#aspn-mw-geri)"/>
  <text x="335" y="120" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">cevap yolu: her katman kendi işini dönüşte de yapar</text>
  <line x1="469" y1="106" x2="469" y2="140" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#aspn-mw-kes)"/>
  <text x="469" y="156" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">kısa devre: next çağrılmazsa zincir burada kesilir</text>
  <rect x="14" y="168" width="316" height="146" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="172" y="189" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Java ↔ .NET eşlemesi</text>
  <text x="172" y="212" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">servlet Filter ↔ ara katman (middleware)</text>
  <text x="172" y="232" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">chain.doFilter(...) ↔ await next(context)</text>
  <text x="172" y="252" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">HandlerInterceptor ↔ ActionFilter</text>
  <text x="172" y="272" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Spring Security zinciri ↔ kimlik ve yetki katmanları</text>
  <text x="172" y="296" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ara katman ham HTTP görür, filtre eylem bağlamını görür</text>
  <rect x="344" y="168" width="302" height="146" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="189" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Sıradan doğan hatalar</text>
  <text x="495" y="212" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">istisna katmanı sonda: hiçbir hatayı yakalayamaz</text>
  <text x="495" y="232" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yetki kimlikten önce: kullanıcı henüz bilinmiyor</text>
  <text x="495" y="252" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kurucuda scoped bağımlılık: kaptif bağımlılık</text>
  <text x="495" y="272" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ara katmana iş kuralı koymak: test edilemez olur</text>
  <text x="495" y="296" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sıra bir tercih değil, davranışın kendisidir</text>
</svg>
```

**Elenme sebepleri**

- "Ara katman filtre gibidir" deyip durmak. Benzetme doğrudur ama sıranın davranışı belirlediğini söylemeyen cevap eksiktir.
- Sırayı önemsiz saymak. Bu soruda en hızlı eleyen cevap "kayıt sırası fark etmez" cümlesidir.
- İstisna katmanını zincirin sonuna koymak. Sonda duran katman kendinden öncekilerin hatasını göremez.
- Yetkiyi kimlikten önce çalıştırmak. Kullanıcı çözülmeden yetki denetlemek anlamsız sonuç üretir.
- Kısa devreden hiç söz etmemek. `next` çağrılmadığında ne olduğunu bilmeyen aday hattı gerçekten kurmamıştır.
- Ara katmanın kurucusuna istek kapsamlı bağımlılık koymak. Kurucu bir kez çağrılır; nesne orada kilitlenir.
- İş kuralını ara katmana yerleştirmek. Kural görünmez olur, birim testi zorlaşır, tekrar kullanılamaz.

**Kendi hikâyen / dürüst köprü**

Bu başlıkta Java tarafındaki filtre deneyimin doğrudan geçerlidir. Şu sorular onu çıkarır:

- Hiç servlet filtresi ya da Spring Security filtresi yazdın mı? Ne yapıyordu?
- Bağıntı kimliğini (correlation-id) isteğe kim ekliyordu? Filtre miydi, ara katman mıydı, yoksa el ile mi?
- Filtre sırası yüzünden bir hata yaşadın mı? Belirti neydi: yetkisiz istek geçti mi, log boş mu kaldı?
- `HandlerInterceptor` ile `Filter` arasında bilinçli bir seçim yaptığın oldu mu? Ölçütün neydi?
- Hata sayfası ya da genel istisna yakalama nerede kuruluydu? Filtrede mi, `@ControllerAdvice` içinde mi?

Köprü cümlesi: "Filtre zinciri kurmayı ve sıra hatalarını Java tarafında yaşadım; ASP.NET Core ara katmanı aynı zincir, `next` çağrısı `chain.doFilter` yerine geçiyor."

### "Yapılandırma ve ortam yönetimini nasıl kurarsın?"

**Soru aslında ne ölçüyor?**

Bu soru teknik gibi durur ama aslında disiplin ölçer. Görüşmeci üç şeye bakar: aynı yapı farklı ortamlarda tek imajla çalışıyor mu, sır (secret) nerede duruyor, öncelik zinciri biliniyor mu. Sır yönetimini atlayan aday bu soruda ciddi puan kaybeder. Bu ilanda konu ödeme olduğu için sır sorusu ayrıca ağırlıklıdır.

**Sözlü cevap iskeleti**

1. **Katmanlı kaynak fikrini kur.** Yapılandırma tek dosyadan gelmez; sıralı sağlayıcılardan gelir. Aynı anahtar birkaç kaynakta varsa **sonradan eklenen kazanır**. Bu, Spring'in özellik önceliğiyle aynı mantıktır (`75`).
2. **Zinciri say.** Alışıldık düzen: temel `appsettings.json` → ortama özel `appsettings.{Ortam}.json` → geliştirici sırları → ortam değişkenleri → komut satırı. Java karşılığı: `application.yml` → `application-{profil}.yml` → ortam değişkeni → komut satırı.
3. **Ortam seçimini adlandır.** ASP.NET Core ortamı `ASPNETCORE_ENVIRONMENT` değişkeninden okur. Bu, Spring'in `spring.profiles.active` ayarının birebir karşılığıdır (`70`). Kod hangi ortamda olduğunu bu değerden öğrenir.
4. **Tipli erişimi göster.** Ayarlar dağınık dizgi olarak okunmaz; bir sınıfa bağlanır. `IOptions<T>` ailesi Spring'in `@ConfigurationProperties` yaklaşımının karşılığıdır. Yeniden yükleme gerekiyorsa izleyen (monitor) biçimi kullanılır.
5. **Sırrı ayır.** Parola, anahtar ve bağlantı dizesi depoya girmez. Ortam değişkeni ya da bir sır kasası kullanılır; geliştiricide ayrı bir sır deposu vardır. Bu kural iki ekosistemde de aynıdır ve bu ilanda pazarlık konusu değildir.
6. **On iki faktörü bağla.** Yapılandırma ortamdan gelir, imaj her ortamda aynıdır (`106`). Ortam başına ayrı derleme yapmak bu ilkeyi bozar ve "bende çalışıyordu" sınıfı hataları üretir.
7. **Kurumsal parametre uyarısını ekle.** Kartlı ödeme tarafında kesim saati, dosya yolu ve kanal kodu gibi değerler koda gömülmez. Bunlar kurumun parametresinden okunur ve iş biriminden yazılı teyit alınır. Bu duruşu söylemek mülakatta doğru cevaptır.

**Elenme sebepleri**

- Öncelik zincirini bilmemek. "Hangi değer kazanır" sorusuna cevap veremeyen aday üretimde ayar hatası yaşar.
- Bağlantı dizesini `appsettings.json` içine yazıp depoya göndermek. Bu tek başına eleyici bir cevaptır.
- Ortam başına ayrı derleme önermek. Aynı imaj her ortamda çalışmalıdır; aksi hâlde test edilen şey dağıtılan şey değildir.
- Ayarları her sınıfta ayrı ayrı dizgi olarak okumak. Tipli bağlama olmadan yazım hatası çalışma anında ortaya çıkar.
- Ortam kavramını yalnız "debug/release" sanmak. Derleme yapılandırması ile çalışma ortamı ayrı şeylerdir.
- Kesim saati gibi iş parametrelerini koda gömmek. Değişince yeni sürüm gerekir ve değişiklik izlenemez.
- Sır rotasyonunu hiç düşünmemek. Sırrın nasıl değiştirileceği, tasarımın parçasıdır.

**Kendi hikâyen / dürüst köprü**

Bu başlıkta anlatacağın şey zaten Java tarafında var. Şu sorular onu çıkarır:

- Projende kaç ortam var ve ayarlar nereden geliyor? Profil dosyası mı, ortam değişkeni mi, merkezî yapılandırma mı?
- Sırlar nerede duruyor? Depoda mı, kasada mı, dağıtım aracının değişkeninde mi?
- Ortam değişkeninin dosyayı ezdiği bir durum yaşadın mı? Nasıl fark ettin?
- Yanlış profille açılan bir uygulama gördün mü? Sonuç neydi: yanlış veri tabanı mı, yanlış uç nokta mı?
- İş parametresi koda gömülü müydü? Değişmesi gerektiğinde ne oldu?
- Aynı imaj bütün ortamlarda mı koşuyor, yoksa ortam başına ayrı paket mi üretiliyor?

Köprü cümlesi: "Profil ve özellik önceliği Java tarafında günlük işim; `appsettings` zinciri aynı fikir, yalnız dosya adları farklı."

### "Web API'de hata yönetimi ve doğrulamayı nasıl yaparsın?"

**Soru aslında ne ölçüyor?**

Bu soru bir olgunluk sorusudur. Görüşmeci hata gövdesinin tek biçim olup olmadığına, durum kodlarının anlamlı seçilip seçilmediğine ve istemciye ne sızdığına bakar. Ayrım şurada çıkar: doğrulama yalnız sınırda mı yapılıyor, yoksa iş kuralı da korunuyor mu. Ödeme bağlamında bu ayrım daha da ağırdır.

**Sözlü cevap iskeleti**

1. **İki katmanı ayır.** Giriş doğrulaması biçim denetler: alan zorunlu mu, uzunluk uygun mu, tutar negatif mi. İş kuralı doğrulaması bağlam ister: limit yeterli mi, kart kapalı mı. Birincisi sınırda, ikincisi alan modelinde durur.
2. **Giriş doğrulamasının karşılığını ver.** .NET'te öznitelik tabanlı doğrulama ya da kural kütüphanesi kullanılır; denetleyici işaretlenmişse geçersiz model otomatik olarak 400 döner. Java karşılığı `@Valid` ile Bean Validation'dır (`77`).
3. **Hata gövdesini tekleştir.** Sorun ayrıntısı (ProblemDetails) biçimi kullanılır. Bu, iki ekosistemde de **aynı RFC'ye** dayanır; Spring tarafındaki `ProblemDetail` ile aynı alanları taşır. Tek biçim, istemci tarafında tek ayrıştırıcı demektir.
4. **Merkezî yakalamayı kur.** Her denetleyicide `try/catch` yazılmaz. Tek bir istisna ara katmanı ya da istisna filtresi bütün hataları yakalar ve tek gövdeye çevirir. Java karşılığı `@ControllerAdvice` ile `@ExceptionHandler`'dır (`78`).
5. **Eşlemeyi tek yerde tut.** Alan istisnası hangi durum koduna karşılık geliyorsa orada yazılır: doğrulama 400, yetki 403, bulunamadı 404, çakışma 409, beklenmeyen 500. Denetleyicilerin bu eşlemeyi bilmesi gerekmez.
6. **İstemciye ne verileceğini söyle.** Yığın izi (stack trace) ve iç mesaj dışarı çıkmaz. Dışarı çıkan şey anlaşılır bir mesaj ve bir bağıntı kimliğidir; ayrıntı günlüğe yazılır. Ödeme bağlamında kart verisi hata gövdesine hiç girmez.
7. **Ödeme özelini ekle.** Zaman aşımı ve yeniden deneme senaryosunda hata cevabı, isteğin gerçekten işlenip işlenmediğini karıştırmamalıdır. Bu yüzden hata yönetimi tekrar edilse de sonucu değişmeyen (idempotent) tasarımla birlikte konuşulur.

**Elenme sebepleri**

- Her denetleyicide ayrı `try/catch` yazmak. Gövde biçimi ayrışır, istemci her uç nokta için ayrı ayrıştırıcı yazar.
- Bütün hataları 200 ile döndürüp gövdeye bir bayrak koymak. Durum kodu anlamsızlaşır, ara katmanlar ve istemciler yanılır.
- Yığın izini istemciye göndermek. Bilgi sızıntısıdır ve güvenlik incelemesinde doğrudan bulgu olur.
- Her hatayı 500 yapmak. Geçersiz girdiyle sunucu hatasını ayırmayan API, çağıranın yeniden deneme kararını bozar.
- Doğrulamayı yalnız istemcide yapmak. Sunucu tarafı doğrulama olmadan API açık kapıdır.
- İş kuralını da sınıra taşımak. Limit ve durum denetimi alan modelinde durmazsa her giriş noktasında tekrarlanır.
- Bağıntı kimliğinden söz etmemek. Kimliksiz hata, destek çağrısında bulunamayan hatadır.

**Kendi hikâyen / dürüst köprü**

Buradaki deneyim ekosistemden bağımsızdır; anlatacağın şey zaten sende var. Şu sorular onu çıkarır:

- Projende hata gövdesi tek biçim mi? Kim belirledi, nasıl zorlanıyor?
- `@ControllerAdvice` yazdın mı? Hangi istisnaları hangi durum kodlarına eşledin?
- Bir hata gövdesinden istemeden bilgi sızdığı oldu mu? Nasıl fark edildi?
- Doğrulamayı nerede yapıyorsun: istekte mi, serviste mi, alan modelinde mi? Bu ayrımı bilinçli mi kurdun?
- Yanlış durum kodu yüzünden çağıran sistem hatalı davrandı mı? Örneğin gereksiz yeniden deneme yaptı mı?
- Destek çağrısında bir hatayı log'da bulmak için ne kullanıyorsun? Bağıntı kimliği var mı?

Köprü cümlesi: "Merkezî istisna yönetimini `@ControllerAdvice` ile kurmaya alışkınım; ASP.NET Core'da bunun yeri istisna ara katmanı ve gövde biçimi aynı RFC."

### "WCF nedir, bugün onunla ne yapılır?"

**Soru aslında ne ölçüyor?**

İlanda WCF geçmesi tek bir anlama gelir: **ortamda eski sistem entegrasyonu vardır.** Görüşmeci senden WCF ustalığı beklemiyor; eski servisin nasıl yaşatılacağını ve nasıl taşınacağını soruyor. Bu yüzden sorunun asıl gövdesi tarih değil, göç stratejisidir. Burada iyi cevap veren aday, kartlı ödeme dünyasında sık karşılaşılan gerçek işi anladığını göstermiş olur.

**Sözlü cevap iskeleti**

1. **Kısa ve doğru tanımla.** WCF, eski nesil .NET servis çatısıdır. SOAP tabanlıdır, sözleşme öznitelikleriyle tanımlanır ve birden çok taşıma seçeneği sunar: HTTP, TCP, adlandırılmış boru, mesaj kuyruğu. Java karşılığı SOAP tabanlı servis dünyasıdır (`221`, `258`).
2. **Bugünkü durumu tarafsız söyle.** WCF modern .NET'e birebir taşınmadı. Yeni geliştirmede yerine REST, gRPC ya da mesajlaşma konuşulur; mevcut SOAP sözleşmesini yaşatmak gerekiyorsa CoreWCF gibi yollar değerlendirilir. Sözleşme odaklı, yüksek hacimli servis içi çağrılar için gRPC yakın bir seçenektir (`98`).
3. **Soruyu asıl yerine çek.** "Eski servisi nasıl taşırsın" sorusu gelecektir. Cevabın çekirdeği iki desendir: **boğan incir (strangler fig)** ve **bozulma önleyici katman (anti-corruption layer)**.
4. **Boğan inciri anlat.** Eski servisin önüne bir yönlendirme cephesi konur. Çağıranlar aynı adrese çağırmaya devam eder. İşlemler tek tek yeni servise taşınır; her taşımada yönlendirme o işlem için yeni tarafa çevrilir. Son işlem de taşınınca eski servis kapatılır.
5. **Bozulma önleyici katmanı anlat.** Eski sözleşmenin modeli yeni alan modeline doğrudan girmez. Araya bir çeviri katmanı konur; eski isimler, boş alan alışkanlıkları ve tuhaf kodlamalar orada kalır. Bu katman olmazsa eski sistemin kusurları yeni sisteme kopyalanır.
6. **Geçiş güvenliğini ekle.** Taşınan işlem bir süre **çift koşturulur**: eski ve yeni taraf aynı isteği işler, sonuçlar karşılaştırılır, fark raporlanır. Geri dönüş yolu her adımda açık tutulur. Ödeme tarafında bu karşılaştırma tutar bazında yapılır.
7. **Tek seferde yeniden yazmayı reddet.** Büyük patlama göçü, geri dönüşü olmayan bir risktir. Kademeli göç daha uzun sürer ama her adımda üretimi ayakta tutar. Bu takas cümlesi mülakatta değerlidir.

```svg
<svg viewBox="0 0 660 336" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Eski WCF servisinin tasinmasi: bogan incir ve bozulma onleyici katman">
  <defs>
    <marker id="aspn-wcf-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
    <marker id="aspn-wcf-yeni" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#25804C"/></marker>
    <marker id="aspn-wcf-eski" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#B07D20"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Boğan incir: çağıran değişmez, işlemler tek tek taşınır</text>
  <rect x="14" y="44" width="132" height="86" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="80" y="66" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Çağıran sistemler</text>
  <text x="80" y="86" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">banka içi uygulamalar</text>
  <text x="80" y="103" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">iş ortağı entegrasyonu</text>
  <text x="80" y="120" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">adres ve sözleşme aynı kalır</text>
  <line x1="148" y1="87" x2="168" y2="87" stroke="#5D7789" stroke-width="1.4" marker-end="url(#aspn-wcf-ok)"/>
  <rect x="170" y="44" width="140" height="86" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <text x="240" y="66" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Yönlendirme cephesi</text>
  <text x="240" y="86" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">işlem bazında yönlendirir</text>
  <text x="240" y="103" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">geri dönüş yolu burada</text>
  <text x="240" y="120" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">çift koşturma ve karşılaştırma</text>
  <line x1="312" y1="70" x2="336" y2="62" stroke="#B07D20" stroke-width="1.4" marker-end="url(#aspn-wcf-eski)"/>
  <line x1="312" y1="106" x2="336" y2="118" stroke="#25804C" stroke-width="1.4" marker-end="url(#aspn-wcf-yeni)"/>
  <rect x="340" y="40" width="306" height="44" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="493" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Eski WCF servisi — henüz taşınmamış işlemler</text>
  <text x="493" y="74" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">SOAP sözleşmesi, eski alan modeli, dokunulmadan çalışır</text>
  <rect x="340" y="96" width="306" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="493" y="114" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Yeni servis — taşınmış işlemler</text>
  <text x="493" y="130" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">REST ya da gRPC, temiz alan modeli, kendi testleri</text>
  <rect x="14" y="152" width="316" height="172" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="172" y="174" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Bozulma önleyici katman</text>
  <text x="172" y="196" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eski sözleşme yeni alan modeline doğrudan girmez</text>
  <text x="172" y="216" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">çeviri tek bir sınırda toplanır</text>
  <text x="172" y="236" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eski isimler ve tuhaf kodlamalar burada kalır</text>
  <text x="172" y="256" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yeni taraf kendi diliyle konuşmaya devam eder</text>
  <text x="172" y="280" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">bu katman yoksa eski kusurlar yeni sisteme kopyalanır</text>
  <text x="172" y="302" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">Java tarafındaki SOAP ve ESB entegrasyonuyla aynı desen</text>
  <rect x="344" y="152" width="302" height="172" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="174" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Yanlış yollar</text>
  <text x="495" y="196" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tek seferde yeniden yazmak: geri dönüş yok</text>
  <text x="495" y="216" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eski sözleşmeyi olduğu gibi kopyalamak</text>
  <text x="495" y="236" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">çağıranları aynı anda değişmeye zorlamak</text>
  <text x="495" y="256" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">taşımayı erteleyip yeni işlevi eskiye eklemek</text>
  <text x="495" y="280" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kademeli göç uzun sürer ama üretimi ayakta tutar</text>
  <text x="495" y="302" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">ödeme tarafında karşılaştırma tutar bazında yapılır</text>
</svg>
```

**Elenme sebepleri**

- "WCF eskidir, atarız" demek. Üretimde çalışan bir entegrasyonu tek cümleyle silen aday risk değerlendirmesi yapmıyor sayılır.
- Tek seferde yeniden yazma önermek. Çağıranlar aynı anda değişemez; bu plan çoğu kurumda uygulanamaz.
- Boğan incir ve bozulma önleyici katmandan hiç söz etmemek. Bu iki isim, göç sorusunun beklenen çekirdeğidir.
- Eski sözleşmeyi olduğu gibi yeni servise kopyalamak. Eski modelin kusurları yeni sisteme taşınır ve borç büyür.
- Geri dönüş yolunu düşünmemek. Her göç adımı geri alınabilir olmalıdır; ödeme tarafında bu pazarlıksızdır.
- Çift koşturma ve karşılaştırmayı atlamak. Doğruluk kanıtlanmadan yapılan geçiş, mutabakat farkı olarak geri döner.
- Sürüm ve tarih iddiasına girmek. Bilmediğin bir ayrıntıyı kesin söylemek, bilmediğini söylemekten çok daha kötüdür.

**Kendi hikâyen / dürüst köprü**

WCF yazmadıysan bunu düz söyle; buradaki asıl konu göç deneyimidir ve o sende olabilir. Şu sorular onu çıkarır:

- Hiç SOAP servisiyle entegre oldun mu? İstemci tarafında mıydın, sunucu tarafında mı?
- Eski bir servisi ya da modülü kademeli olarak değiştirdiğin bir iş oldu mu? Nasıl bölündü?
- Eski sistemin modeli yeni koda sızdı mı? Sızmasını engelleyen bir çeviri katmanı var mıydı?
- Geçiş sırasında iki taraf birlikte çalıştı mı? Sonuçlar karşılaştırıldı mı, fark bulundu mu?
- Bir çağıranı sözleşme değiştirmeye ikna etmek zorunda kaldın mı? Süreç nasıl işledi?
- Geri alma planı yazılı mıydı? Hangi eşikte geri dönülecekti?

Köprü cümlesi: "WCF ile kod yazmadım; ama SOAP tabanlı eski servislerle entegrasyonu ve kademeli göçü Java tarafında yaptım. Desen aynı: cepheyle yönlendirme, araya çeviri katmanı, çift koşturarak doğrulama."

### Bu bölümden çıkan ders

Bu beş sorunun ortak dersi tek cümlededir: **ASP.NET Core, Spring'de bildiğin şeylerin başka kelimelerle yazılmış hâlidir.** Kap aynı kaptır, üç kapsam aynı üç kapsamdır ve en klasik tuzak ikisinde de aynıdır; tek fark, uzun ömürlüye kısa ömürlü enjekte edildiğinde .NET'in sana çalışma anında hata vermesi, Spring'in ise sessiz kalmasıdır. Ara katman hattı servlet filtresi zinciridir ve sıra orada da davranışın kendisidir. Yapılandırma zinciri profil mantığıdır; sonradan eklenen kaynak öncekini ezer ve sır depoya girmez. Hata yönetiminde gövde biçimi bile aynı RFC'den gelir. WCF ise bir dil sorusu değil, bir göç sorusudur: ilanda geçmesi eski sistem entegrasyonu demektir ve doğru cevap boğan incir ile bozulma önleyici katmandır. Mülakatta bu eşlemeyi açıkça kurman, C# sözdizimini ezberlemenden çok daha güçlü bir izlenim bırakır; çünkü kavramı bilen adayın dili öğrenmesi haftalar sürer, dili bilen adayın kavramı öğrenmesi yıllar.

# aspn-1 | Temel
## Soru
Spring'de istek (request) kapsamına kayıtlı bir bileşenin ASP.NET Core'daki en yakın karşılığı hangi kayıttır?
## A
`AddSingleton` — uygulama boyunca tek bir örnek üretir ve paylaşılır
🤖 bu, Spring'in varsayılan singleton kapsamının karşılığıdır.
## B
`AddScoped` — her HTTP isteği için bir örnek üretilir ve istek bitince bırakılır
🤖 Doğru: ASP.NET Core'da kapsam varsayılan olarak istek başına açılır.
## C
`AddTransient` — her çözümleme çağrısında yepyeni bir örnek üretilir
🤖 bu, Spring'in prototype kapsamına karşılık gelir.
## D
`AddHostedService` — uygulama açıkken sürekli koşan bir arka plan işi kaydeder
🤖 bu bir kapsam değil, uygulama ömrü boyunca çalışan bir servistir.
## Doğru
B
## Açıklama
Eşleme birebirdir ve ezberlenecek tek şey kelimelerdir. Spring'in varsayılan singleton kapsamı `AddSingleton`, istek kapsamı `AddScoped`, prototype kapsamı ise `AddTransient` olur (`68`, `69`). ASP.NET Core'da kapsam kavramı soyut değildir: çatı her gelen HTTP isteği için bir kapsam açar, isteğin işi bitince kapsamı kapatır ve o kapsamda üretilmiş nesneleri bırakır. Bu yüzden veri bağlamı, birim iş nesnesi ve isteğe özel veri taşıyan sınıflar `AddScoped` ile kaydedilir. `AddSingleton` uygulama boyunca tek örnek verir; bu nedenle durumsuz ve iş parçacığı güvenli olmalıdır. `AddTransient` en ucuz ömürdür ama her çözümlemede yeni nesne ürettiği için pahalı nesnelerde tercih edilmez. D şıkkındaki `AddHostedService` bir kapsam kaydı değildir; arka planda koşan bir servis kaydeder ve bu servis içinde istek kapsamı olmadığı için kapsamın elle açılması gerekir.

# aspn-2 | Orta
## Soru
`AddSingleton` ile kayıtlı bir sınıfın kurucusuna, `AddScoped` ile kayıtlı bir bağımlılık enjekte ediliyor. Bu durum için en doğru ifade hangisidir?
## A
Sorun çıkmaz; kap her istekte singleton'ın içindeki bağımlılığı yenileyip kapsamı korur
🤖 kurucu bir kez çağrılır; kap içerideki nesneyi sonradan tazelemez.
## B
Sorun çıkmaz ama başarım düşer; her istekte yeni bir kapsam açılması ek maliyet doğurur
🤖 mesele başarım değil ömürdür; kapsam zaten her istekte açılır.
## C
Derleme hatası oluşur; derleyici ömür uyumsuzluğunu tip sisteminde yakalayıp uyarır
🤖 ömürler kap kaydında tutulur; derleyicinin bundan haberi olmaz.
## D
Kaptif bağımlılık oluşur; kap doğrulaması bunu çalışma anında hata olarak verir
🤖 Doğru: kısa ömürlü nesne uzun ömürlüye takılır ve sızar.
## Doğru
D
## Açıklama
Bu, iki ekosistemin **aynı** klasik tuzağıdır: uzun ömürlüye kısa ömürlü enjekte etmek. Spring'de bunun karşılığı singleton bir bean'e prototype bean enjekte etmektir; .NET'te singleton'a scoped enjekte etmektir. Mekanizma da aynıdır: kurucu yalnız bir kez çağrılır, o anda çözülen kısa ömürlü nesne uzun ömürlü nesnenin içinde **takılı kalır** ve bir daha tazelenmez. Sonuç, isteğe özel bir nesnenin bütün istekler tarafından paylaşılmasıdır; ödeme gibi bağlamlarda bu veri karışmasına kadar gidebilir. Aradaki tek fark uyarı davranışıdır: Spring bu durumda sessiz kalır ve sorun üretimde çıkar, .NET'te ise kapsam doğrulaması açıkken kap bu kaydı çözerken hata atar. Geliştirme ortamında bu doğrulama genellikle açıktır. Doğru çözüm bağımlılığı kurucudan almamak, ihtiyaç anında kapsam açıp içinden çözmektir; Spring'deki karşılığı kapsam vekili ya da sağlayıcı enjeksiyonudur (`69`).

# aspn-3 | Orta
## Soru
Bir ASP.NET Core uygulamasında ara katmanlar şu sırayla kaydedilmiş: yetkilendirme, kimlik doğrulama, yönlendirme, istisna yakalama. Bu kayıt sırasındaki temel kusur nedir?
## A
Yetki kimlikten önce çalışamaz; istisna katmanı da en başta olmalıdır, sonda hiçbir şeyi yakalamaz
🤖 Doğru: iki kusur da doğrudan kayıt sırasından doğar.
## B
Kusur yoktur; ASP.NET Core kayıt sırasına bakmaz, ara katmanları kendi içinde doğru sıralar
🤖 boru hattı tamamen kayıt sırasına göre kurulur, çatı sıralama yapmaz.
## C
Tek kusur yönlendirmenin ortada olmasıdır; diğer üçünün sırası birbirine göre önemsizdir
🤖 asıl belirleyici olan kimlik-yetki sırası ve istisna katmanının yeridir.
## D
Kusur yalnız başarımdadır; yanlış sıra fazladan ayırma üretir ama davranışı hiç değiştirmez
🤖 yanlış sıra davranışı değiştirir; istekler yanlış sonuçlanır.
## Doğru
A
## Açıklama
ASP.NET Core'un boru hattı **tamamen** kayıt sırasına göre kurulur; çatı senin için yeniden sıralama yapmaz. Bu, servlet filtresi zincirinin davranışıyla aynıdır ve `next` çağrısı `chain.doFilter` yerine geçer (`76`). Verilen sırada iki ayrı kusur vardır. Birincisi, yetkilendirme kimlik doğrulamadan önce koşar; kullanıcı henüz çözülmediği için yetki denetimi anlamsız bir sonuç üretir. İkincisi, istisna yakalama zincirin sonundadır; bir ara katman kendinden **sonra** gelenlerin hatalarını yakalayabildiği için sonda duran istisna katmanı hiçbir şey yakalayamaz. Doğru düzen dıştan içe şudur: istisna yakalama, ardından taşıma ve statik dosya katmanları, yönlendirme, kimlik doğrulama, yetkilendirme ve en içte uç nokta. B şıkkı çatıya olmayan bir zekâ atfeder. D şıkkı sorunu başarıma indirger; oysa yanlış sıra doğrudan yanlış davranış üretir ve bu tür hatalar genellikle güvenlik tarafında ortaya çıkar.

# aspn-4 | Orta
## Soru
Bir bağlantı dizesi hem `appsettings.json` hem `appsettings.Production.json` içinde, ayrıca bir ortam değişkeninde tanımlı. Alışıldık kurulumda üretimde hangi değer geçerli olur?
## A
`appsettings.json`; kök dosya temel kabul edilir ve diğer kaynaklar onu ezemez
🤖 sonradan eklenen sağlayıcı kök dosyayı ezer.
## B
`appsettings.Production.json`; ortama özel dosya her zaman zincirin en sonunda okunur
🤖 ortam dosyasından sonra ortam değişkeni ve komut satırı gelir.
## C
Ortam değişkeni; varsayılan zincirde sonradan eklenen sağlayıcı öncekini ezer
🤖 Doğru: Spring'de de ortam değişkeni `application.yml` değerini ezer.
## D
Hiçbiri; aynı anahtar birden çok kaynakta tanımlıysa uygulama açılışta hata verir
🤖 çakışma hata değil, tanımlı bir öncelik zinciridir.
## Doğru
C
## Açıklama
Yapılandırma tek dosyadan değil, **sıralı sağlayıcılardan** gelir. Alışıldık düzen şöyledir: temel `appsettings.json`, sonra ortama özel `appsettings.{Ortam}.json`, sonra geliştirici sırları, sonra ortam değişkenleri, en sonda komut satırı. Aynı anahtar birkaç kaynakta varsa sonradan eklenen kazanır. Bu, Spring'in özellik önceliğiyle aynı mantıktır: ortam değişkeni `application.yml` içindeki değeri ezer (`75`, `70`). Ortamın hangisi olduğu ise `ASPNETCORE_ENVIRONMENT` değişkeninden okunur; bu, `spring.profiles.active` ayarının karşılığıdır. Pratik sonuç şudur: aynı imaj bütün ortamlarda çalışır, farkı ortam sağlar (`106`). Bağlantı dizesi ve parola gibi sırlar depoya giren dosyalara yazılmaz; ortam değişkeni ya da bir sır kasası kullanılır. Ödeme bağlamında kesim saati ve kanal kodu gibi iş parametreleri de koda gömülmez, kurumun parametresinden okunur ve iş biriminden yazılı teyit alınır.

# aspn-5 | İleri
## Soru
Bir Web API'de her denetleyici kendi `try/catch` bloğuyla farklı gövde biçiminde hata döndürüyor. En yerinde düzeltme hangisidir?
## A
Her denetleyicide aynı `try/catch` kalıbını kopyalayıp gövde biçimini elle birleştirmek
🤖 tekrar kalır; bir sonraki denetleyicide biçim yine ayrışır.
## B
Bütün hataları 200 ile döndürüp gövdeye bir hata alanı koymak, istemci tarafı sadeleşsin
🤖 durum kodunu anlamsızlaştırır; ara katmanlar ve istemciler yanılır.
## C
Hataları yakalamayı bırakmak; yakalanmayan istisna zaten yığın izini istemciye taşır
🤖 yığın izini dışarı vermek doğrudan bilgi sızıntısıdır.
## D
Merkezî bir istisna ara katmanı kurup gövdeyi sorun ayrıntısı (ProblemDetails) biçiminde tekleştirmek
🤖 Doğru: `@ControllerAdvice` ile aynı fikir, gövde aynı RFC.
## Doğru
D
## Açıklama
Hata gövdesinin tek biçim olması bir zevk meselesi değil, sözleşme meselesidir. Her denetleyicinin kendi biçimini üretmesi, istemci tarafında her uç nokta için ayrı ayrıştırıcı yazılması demektir. Doğru düzen merkezî yakalamadır: tek bir istisna ara katmanı ya da istisna filtresi bütün hataları toplar, alan istisnalarını durum kodlarına eşler ve gövdeyi tek biçime çevirir. Java karşılığı `@ControllerAdvice` ile `@ExceptionHandler`'dır (`78`). Gövde biçimi olarak sorun ayrıntısı (ProblemDetails) kullanılır; Spring tarafındaki `ProblemDetail` ile aynı RFC'ye dayandığı için alanlar örtüşür. Eşleme tek yerde tutulur: doğrulama 400, yetki 403, bulunamadı 404, çakışma 409, beklenmeyen 500. Giriş doğrulaması ise ayrı bir katmandır; öznitelik tabanlı doğrulama ile biçim denetlenir, iş kuralı alan modelinde kalır (`77`). B şıkkı durum kodunu anlamsızlaştırır ve çağıranın yeniden deneme kararını bozar. C şıkkı yığın izini dışarı taşır; dışarı çıkması gereken tek ek bilgi bağıntı kimliğidir, ayrıntı günlüğe yazılır.

# aspn-6 | İleri
## Soru
Üretimde çalışan bir WCF servisi modern .NET'e taşınacak. Birkaç çağıran sistem var ve hepsi aynı anda değiştirilemiyor. En yerinde yaklaşım hangisidir?
## A
Servisi bir seferde yeniden yazıp bütün çağıranları aynı hafta yeni sözleşmeye geçirmek
🤖 tek seferde geçiş, geri dönüşü olmayan bir risktir.
## B
Önüne yönlendirme katmanı koyup işlemleri tek tek taşımak; araya bozulma önleyici katman girer
🤖 Doğru: boğan incir artı bozulma önleyici katman.
## C
WCF sözleşmesini olduğu gibi yeni servise kopyalayıp iç modeli de aynı biçimde kurmak
🤖 eski modelin kusurları yeni sisteme birebir taşınır.
## D
Taşımayı ertelemek; eski servisi yerinde bırakıp yeni işlevleri de oraya eklemeye devam etmek
🤖 borç büyür ve eski çatı yeni geliştirmeyi de kilitler.
## Doğru
B
## Açıklama
İlanda WCF geçmesi ustalık değil, **eski sistem entegrasyonu** anlamına gelir; soru da bu yüzden göç sorusudur. Beklenen cevabın iki adı vardır. Birincisi **boğan incir (strangler fig)**: eski servisin önüne bir yönlendirme cephesi konur, çağıranlar aynı adrese çağırmaya devam eder, işlemler tek tek yeni servise taşınır ve son işlem de taşınınca eski servis kapatılır. İkincisi **bozulma önleyici katman (anti-corruption layer)**: eski sözleşmenin modeli yeni alan modeline doğrudan girmez, araya bir çeviri sınırı konur ve eski isimler orada kalır (`221`, `258`). Geçişin güvenliği çift koşturmayla sağlanır: iki taraf bir süre aynı isteği işler, sonuçlar karşılaştırılır ve fark raporlanır; ödeme tarafında bu karşılaştırma tutar bazında yapılır. A şıkkı geri dönüşü olmayan bir büyük patlama göçüdür. C şıkkı eski kusurları yeni sisteme kopyalar. D şıkkı borcu büyütür. Yeni tarafta sözleşme odaklı ve yüksek hacimli servis içi çağrılar için gRPC yakın bir seçenektir; mevcut SOAP sözleşmesini yaşatmak gerekiyorsa CoreWCF gibi yollar değerlendirilir (`98`).
