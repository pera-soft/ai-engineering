---
slug: legacy-dotnet-modernizasyon-teknik-borc
baslik: "Legacy .NET Modernizasyonu ve Teknik Borç: Boğan İncir, Karakterizasyon Testi, Ölçüp Önceliklendirme"
kademe: İleri
sira: 293
etiketler: 11
---

## Konu anlatımı

Bu dosya ilanın görev tanımının tam merkezindedir. İlan dört ayrı maddede aynı şeyi söylüyor: teknik borcun azaltılması, mevcut sistemlerin bakımı ve performans iyileştirmesi, gelir operasyonlarını destekleyen platformların modernizasyonu, mevcut uygulama ve web sitelerine bakım ve revizyon. Dördü tek işin dört yüzüdür. Bu ilanda seni işe alan kişi yeşil alan (greenfield) bir proje için aday aramıyor; **çalışan, eski, dokunmaya korkulan bir kod tabanını ilerletecek** birini arıyor.

Bu, kötü haber değil. Tersine, kıdemin en iyi görüldüğü yer burasıdır. Sıfırdan proje yazmak bir mühendislik becerisidir; on beş yıllık bir sistemi üretimi durdurmadan ilerletmek başka bir beceridir ve daha nadirdir. Mülakatta bu ikinci beceriyi gösteren aday, en yeni çatıyı sayan adaydan önde başlar.

Aşağıda beş açık uçlu sözlü prova sorusu var. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı; senin adına .NET modernizasyon deneyimi uydurulmaz. Onu kendi geçmişinden çıkarman için sorular sorulur, çıkmıyorsa `256`'daki dürüst köprü kurulur. Teknik borcun **iletişim** yüzü — görünür kılmak, backlog'da tutmak, sprint payı — `268`'de işlendi; burada tekrarlanmaz. Bu dosyanın farkı ölçüm ve .NET'e özgü geçiş yüzeyidir.

**Sürüm numarası hakkında bir şerh.** Aşağıda YARP, CoreWCF ve yükseltme yardımcısı (upgrade assistant) gibi araç adları geçiyor. Bu adlar ve arkalarındaki sürüm numaraları zamanla değişir; bugünün önerilen aracı iki yıl sonra bakım moduna alınmış olabilir ve satıcı belgeleri geçişi hep olduğundan kolay anlatır. Mülakatta sürüm numarası ezberlemek zayıf bir stratejidir: yanlış hatırlarsan doğrudan puan kaybedersin, doğru hatırlarsan yalnızca hafıza göstermiş olursun. Güçlü olan şey **geçiş yüzeyini** anlatmaktır — neyin karşılığı olduğunu, neyin karşılığı olmadığını ve karşılığı olmayan yerde ne yapıldığını bilmek. Araç değişir, yüzey aynı kalır. Bir araç adını hatırlamıyorsan "bu iş için bir yönlendirme katmanı kullanılır, adı şu an aklıma gelmiyor" demek, yanlış ad söylemekten çok daha güçlü duyulur.

### .NET Framework'ten .NET'e geçişin kırılma noktaları

Bu bölüm ders niteliğindedir; sorular bundan sonra başlar. Logo gibi uzun ömürlü ürün geliştiren şirketlerde kod tabanının bir kısmı .NET Framework üzerindedir ve mülakat soruları en çok bu yüzeyden gelir. Kritik nokta şu: bu bir **sürüm yükseltmesi değil, çatı değişimidir.** Bazı şeylerin karşılığı birebirdir, bazılarının karşılığı yoktur.

**En sert kırılma `System.Web`'dir.** Eski dünyada `HttpContext.Current` statik bir özelliktir; kodun herhangi bir yerinden, katman farkı gözetmeden çağrılabilir. Bir hesaplama sınıfının içinde oturumdan kullanıcı okumak mümkündür ve yaygındır. Yeni dünyada bu statik bağlam yoktur. Bağlam artık bağımlılık enjeksiyonuyla taşınır (`IHttpContextAccessor`) ve bu, mimari bir kırılmadır: eski kodda bağlama gizlice erişen her sınıf artık ya bağlamı parametre olarak alacak ya da bağlamdan tamamen kopacaktır.

```csharp
// .NET Framework: statik bağlam, her katmandan görünür, test edilemez
var kullanici = HttpContext.Current.User.Identity.Name;

// .NET: bağlam enjekte edilir; sınıf artık testte sahte bağlamla koşabilir
public sealed class SiparisServisi(IHttpContextAccessor erisimci)
{
    public string Kullanici() => erisimci.HttpContext?.User?.Identity?.Name ?? "anonim";
}
```

Bu değişikliği bir angarya gibi görme; aslında bir hediyedir. Statik bağlamı sökmek, o sınıfı ilk kez test edilebilir hâle getirir. Yani göçün kendisi teknik borcu ödemeye zorlar.

**Yapılandırma tarafında** `Web.config` gider, `appsettings.json` ve seçenekler kalıbı (options pattern) gelir. Dönüşüm (transform) dosyalarıyla ortam yönetimi yerine katmanlı sağlayıcı zinciri kurulur (`282`).

```xml
<!-- Web.config: XML, dönüşüm dosyalarıyla ortam yönetimi -->
<appSettings><add key="KesimSaati" value="17:30" /></appSettings>
```

```csharp
// appsettings.json + seçenekler kalıbı: tipli, doğrulanabilir, ortamdan gelir
public sealed class MutabakatAyarlari { public TimeSpan KesimSaati { get; set; } }
builder.Services.Configure<MutabakatAyarlari>(builder.Configuration.GetSection("Mutabakat"));
```

**Açılış tarafında** `Global.asax` ve eski `Startup` ayrımı gider, tek bir `Program.cs` gelir. **Sunum tarafında** ASP.NET MVC5'in karşılığı ASP.NET Core MVC'dir ve geçiş büyük ölçüde mekaniktir; Web Forms'un ise **birebir karşılığı yoktur**. Web Forms taşıyan bir ekip ya yeniden yazar ya da o modülü olduğu yerde bırakıp önüne yeni bir uygulama koyar. Bu ayrımı bilmek mülakatta ciddi fark yaratır, çünkü çoğu aday "MVC'ye çeviririz" der ve Web Forms'un olay modelinin taşınamayacağını fark etmez.

**WCF sunucu tarafı** modern .NET'e taşınmadı; mevcut SOAP sözleşmesini yaşatmak gerekiyorsa CoreWCF gibi yollar, yeni geliştirmede REST ya da gRPC konuşulur (`282`, `221`). **`AppDomain` yoktur**; eklenti yükleyip boşaltma senaryoları süreç ayrımına ya da yükleme bağlamına taşınır. **Senkron I/O varsayımı** en sinsi kalemdir: eski kod tabanı baştan sona bloklayan çağrılarla yazılmışsa, bunu uçtan uca `async` hâline getirmek yüzeysel bir arama-değiştirme işi değildir; yarım bırakılırsa senkron üstünde asenkron (sync-over-async) kalıbı doğar ve iş parçacığı havuzu açlığa girer (`284`).

**`.NET Standard` köprü rolü oynar.** Ortak sözleşmeleri ve alan tiplerini `.NET Standard` hedefleyen bir kitaplığa çekersen, aynı kitaplık hem eski hem yeni taraftan kullanılabilir. Bu, geçiş sırasında iki dünyanın aynı tipleri paylaşmasını sağlar ve kopyalamayı önler.

**Üçüncü parti bağımlılıklar** göçün en sık takıldığı yerdir. Kendi kodunu taşıyabilirsin ama on yıl önce alınmış, artık bakımı yapılmayan bir kitaplık hedef çerçeveyi desteklemiyorsa yol orada tıkanır. Bu yüzden göç planının ilk adımı kod değil, **bağımlılık envanteridir**: her paketin hedef çerçeveyi destekleyip desteklemediği, desteklemiyorsa alternatifi ya da o parçanın kapsam dışı bırakılıp bırakılamayacağı.

```svg
<svg viewBox="0 0 660 376" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="NET Framework ile NET arasindaki kirilma noktalari haritasi ve karsiliklari">
  <defs>
    <marker id="netmod-kirilma-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
    <marker id="netmod-kirilma-yok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#B23B3B"/></marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sürüm yükseltmesi değil, çatı değişimi: neyin karşılığı var, neyin yok</text>
  <text x="150" y="41" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">.NET Framework (eski)</text>
  <text x="470" y="41" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">.NET (yeni) — karşılığı ve bedeli</text>
  <rect x="14" y="48" width="272" height="30" rx="6" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="150" y="67" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">System.Web · HttpContext.Current (statik)</text>
  <line x1="290" y1="63" x2="306" y2="63" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#netmod-kirilma-yok)"/>
  <rect x="310" y="48" width="336" height="30" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="478" y="67" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yok · bağlam DI ile taşınır (IHttpContextAccessor) — mimari kırılma</text>
  <rect x="14" y="84" width="272" height="30" rx="6" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="150" y="103" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Web.config + dönüşüm dosyaları</text>
  <line x1="290" y1="99" x2="306" y2="99" stroke="#5D7789" stroke-width="1.4" marker-end="url(#netmod-kirilma-ok)"/>
  <rect x="310" y="84" width="336" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="478" y="103" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">appsettings.json + seçenekler kalıbı — büyük ölçüde mekanik</text>
  <rect x="14" y="120" width="272" height="30" rx="6" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="150" y="139" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Global.asax · eski Startup ayrımı</text>
  <line x1="290" y1="135" x2="306" y2="135" stroke="#5D7789" stroke-width="1.4" marker-end="url(#netmod-kirilma-ok)"/>
  <rect x="310" y="120" width="336" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="478" y="139" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Program.cs · tek giriş noktası, kap ve hat orada kurulur</text>
  <rect x="14" y="156" width="272" height="30" rx="6" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="150" y="175" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ASP.NET MVC5 · Web Forms</text>
  <line x1="290" y1="171" x2="306" y2="171" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#netmod-kirilma-yok)"/>
  <rect x="310" y="156" width="336" height="30" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="478" y="175" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">MVC5 → ASP.NET Core mekanik; Web Forms'un karşılığı YOK</text>
  <rect x="14" y="192" width="272" height="30" rx="6" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="150" y="211" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">WCF sunucu tarafı (SOAP sözleşmesi)</text>
  <line x1="290" y1="207" x2="306" y2="207" stroke="#5D7789" stroke-width="1.4" marker-end="url(#netmod-kirilma-ok)"/>
  <rect x="310" y="192" width="336" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="478" y="211" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sözleşmeyi yaşat: CoreWCF · yeni geliştirme: REST ya da gRPC</text>
  <rect x="14" y="228" width="272" height="30" rx="6" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="150" y="247" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">AppDomain izolasyonu · eklenti boşaltma</text>
  <line x1="290" y1="243" x2="306" y2="243" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#netmod-kirilma-yok)"/>
  <rect x="310" y="228" width="336" height="30" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="478" y="247" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yok · süreç ayrımı ya da yükleme bağlamı ile yeniden tasarlanır</text>
  <rect x="14" y="264" width="272" height="30" rx="6" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="150" y="283" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">senkron I/O varsayımı (bloklayan çağrı)</text>
  <line x1="290" y1="279" x2="306" y2="279" stroke="#5D7789" stroke-width="1.4" marker-end="url(#netmod-kirilma-ok)"/>
  <rect x="310" y="264" width="336" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="478" y="283" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">uçtan uca async; yarım bırakılırsa havuz açlığı doğar</text>
  <rect x="14" y="304" width="632" height="62" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="324" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Göç planının ilk adımı kod değil, envanterdir</text>
  <text x="330" y="343" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">.NET Standard hedefleyen ortak kitaplık iki dünyadan da kullanılır: köprü buradan kurulur</text>
  <text x="330" y="359" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">Bakımı bırakılmış üçüncü parti paket hedef çerçeveyi desteklemiyorsa yol orada tıkanır</text>
</svg>
```

### "15 yıllık bir .NET Framework uygulamasını modernize edeceksiniz. Nereden başlarsınız?"

**Soru aslında ne ölçüyor?**

Bu soru bir teknoloji sorusu gibi görünür ama değildir. Görüşmecinin ölçtüğü şey adayın **ilk refleksidir**. Zayıf aday cevaba çatı adıyla başlar: "önce .NET 8'e çekerim". Güçlü aday cevaba ölçmeyle ve risk azaltmayla başlar. İkinci ayrım artımlılıktadır. Bu ilanda "mevcut sistemlerin bakımı" ve "modernizasyona katkı" yan yana yazılmış; yani sistem modernizasyon sırasında da çalışmaya devam edecek. Cevabın bunu kabul etmesi gerekir. Ağırlık sıradadır, araç adında değil.

**Sözlü cevap iskeleti**

1. **Önce anla ve ölç, sonra dokun.** İlk hafta kod yazılmaz. Çıkarılacak şeyler: modül haritası, hangi modülün ne iş yaptığı, çağıran sistemler, bağımlılık envanteri, üretim trafiği, en çok hata çıkaran yerler. "Nereden başlarım" sorusunun cevabı bu haritadan çıkar, önsezidan değil.
2. **İş değerini haritanın üzerine koy.** Modernizasyon teknik bir istek değil, iş isteğidir. Hangi modül gelir üretiyor, hangisi sık değişiyor, hangisi müşteriye görünüyor. Sık değişen ve iş için kritik olan yer önce gelir; ölü modül son sıradadır ya da hiç sırada değildir.
3. **Kenar (seam) bul.** Kenar, davranışı değiştirmeden araya girebileceğin noktadır: bir arayüz, bir sınır, bir çağrı yüzeyi. Kenarsız kod taşınamaz; ilk teknik iş kenar açmaktır.
4. **Dokunmadan önce ağı ör: karakterizasyon testi.** Taşınacak davranışın mevcut hâli teste alınır. Bu testler doğruyu değil **bugünkü davranışı** kaydeder; göç sırasında karşılaştırma ölçütü onlardır.
5. **Boğan incir kalıbıyla (strangler fig pattern) artımlı taşı.** Eski uygulamanın önüne ters vekil (reverse proxy) konur, rotalar tek tek yeni tarafa taşınır, her adımda geri dönüş yolu açık kalır.
6. **Büyük patlama yeniden yazımı (big bang rewrite) açıkça reddet.** Gerekçeyi tek cümlede ver: eski sistem yeniden yazım süresince donmaz, değişmeye devam eder; hedef hareket ederken vurulmaz.
7. **Her adımı ölç ve göster.** Taşınan modülün gecikmesi, hata oranı ve değişiklik süresi öncesi-sonrası karşılaştırılır. Ölçülmeyen modernizasyon, bir sonraki bütçe görüşmesinde savunulamaz.
8. **Çerçeve yükseltmesini bir sonuç, bir amaç olarak değil.** Hedef ".NET 8'e geçmek" değil, "bu modülde değişiklik süresini düşürmek ve destek riskini kapatmak"tır. Çerçeve yükseltmesi bunun aracıdır.

**Elenme sebepleri**

- Cevaba çatı ya da sürüm adıyla başlamak. Ölçmeden başlayan plan, en gürültülü şikâyete göre sıralanır.
- Baştan yazmayı önermek. Bu ilanda "bakım ve revizyon" yazıyor; sistem yeniden yazım boyunca çalışmaya devam edecek.
- Bağımlılık envanterini atlamak. Kendi kodun taşınabilir olsa da bakımı bırakılmış bir paket bütün planı durdurur.
- Karakterizasyon testinden hiç söz etmemek. Test ağı olmadan yapılan taşımanın doğruluğu kanıtlanamaz.
- Geri dönüş yolunu düşünmemek. Her göç adımı geri alınabilir olmalıdır; olmayan adım bir bahis olur.
- Yükseltme aracının işi bitireceğini varsaymak. Otomatik araçlar mekanik kısmı hızlandırır; mimari kırılmayı çözmez.
- Web Forms ya da `AppDomain` gibi karşılığı olmayan kalemleri "çeviririz" diye geçmek. Karşılığı olmayanı bilmek, olanı bilmekten değerlidir.

**Kendi hikâyen**

Buraya kendi vakan gelecek; uydurma. Şu sorular seni kendi geçmişine götürsün:

- Eski bir sistemi ya da modülü kademeli olarak değiştirdiğin bir iş oldu mu? Nasıl bölündü, ilk parça hangisiydi ve neden o seçildi?
- Bağımlılıklardan biri yeni sürümü desteklemediği için sıkıştığın oldu mu? Nasıl çözüldü: alternatif mi, kendi sarmalayıcın mı, erteleme mi?
- Dokunmadan önce test yazdığın bir modül oldu mu? Testler davranışı mı doğruluyordu, yoksa mevcut hâli mi kaydediyordu?
- Bir "baştan yazalım" önerisi duydun mu? Sonuç ne oldu, süre tahmini tuttu mu?

Köprü cümlesi: ".NET Framework göçü yapmadım; ama sürüm yükseltmesini ve eski bir modülü kademeli taşımayı Java tarafında yürüttüm. Sıra aynı: envanter, kenar, test ağı, artımlı taşıma, her adımda ölçüm."

### Boğan incir, özellik bayrakları ve geriye dönük uyumluluk

Boğan incir kalıbının (strangler fig pattern) adı bir bitkiden gelir: incir eski ağacın gövdesini sarar, o ayaktayken kendi gövdesini kurar ve eski ağaç sonunda çürüyüp gider. Yazılımda karşılığı şudur: eski uygulama çalışmaya devam ederken yeni uygulama onun etrafında büyür ve işlevler tek tek el değiştirir.

**Mekanizması ters vekildir (reverse proxy).** Kullanıcı ve çağıran sistemler tek bir adrese gelir. Vekil, gelen isteğin yolunu bakar ve o yol henüz taşınmadıysa eski uygulamaya, taşındıysa yeni uygulamaya yönlendirir. .NET tarafında bu iş için YARP gibi bir ters vekil kitaplığı sık kullanılır; ama nginx de olur, bir API geçidi de olur. Önemli olan araç değil, **yönlendirmenin rota bazında ve tek yerde** olmasıdır. Vekilin varlığı sayesinde çağıranlar hiçbir şey yapmaz; onlar için adres ve sözleşme değişmemiştir.

**Oturum ve kimlik doğrulama iki sistemde birlikte yaşamak zorundadır.** Bu, boğan incirin en sık atlanan ve en çok acıtan yeridir. Kullanıcı eski tarafta giriş yapıp yeni tarafa yönlenirse ve yeni taraf onu tanımıyorsa, kullanıcı göç boyunca iki kez giriş yapar. Çözüm ya paylaşılan bir çerez alanı ve ortak anahtar, ya da iki tarafın da doğruladığı merkezî bir kimlik sağlayıcıdır. Bu karar göçün başında verilir; sonradan verilirse taşınan her rota geri gelir.

**Veritabanı bir süre paylaşılır ve bu bilinçli bir ödünleşimdir (trade-off).** Kitaplarda paylaşımlı veritabanı bir kötü kalıp olarak geçer, doğrudur; ama boğan incir sırasında veri katmanını da aynı anda bölmeye kalkmak göçü ikiye katlar ve genellikle batırır. Doğru duruş şudur: geçiş süresince veritabanı paylaşılır, bu durum **yazılı olarak geçici** ilan edilir, yeni taraf şemaya doğrudan değil kendi erişim katmanından dokunur (`283`) ve veri bölünmesi göçün son fazına konur. Mülakatta bunu böyle söylemek olgunluk göstergesidir; "veritabanını da aynı anda böleriz" demek tecrübesizlik göstergesidir.

**Özellik bayrakları (feature flags)** boğan incirin ikinci yarısıdır. Vekil rotayı yönlendirir, bayrak ise davranışı seçer. Üç iş yapar. Birincisi, yeni yol üretime **kapalı** olarak gider; kod dağıtılmıştır ama açılmamıştır, yani dağıtım (deployment) ile yayına alma (release) birbirinden ayrılır (`191`). İkincisi, karşılaştırmalı koşum (shadow run / parallel run) mümkün olur: istek her iki yola da gider, cevabı kullanıcıya eski yol verir, yeni yolun ürettiği sonuç yalnızca karşılaştırılıp farklar raporlanır. Bu, doğruluğu üretim verisiyle kanıtlamanın en ucuz yoludur. Üçüncüsü, geri alma (rollback) kapısıdır: bir sorun çıktığında yeni dağıtım beklemeden bayrak kapatılır ve trafik saniyeler içinde eski yola döner.

Bayrağın bir bedeli vardır ve bunu söylemek gerekir: her bayrak kodda bir dallanmadır ve iki yol da bakılmak zorundadır. Bu yüzden her bayrağın **bir son kullanma tarihi** olur. Göç bitince bayrak da, eski yol da silinir. Silinmeyen bayrak yeni bir teknik borçtur.

```svg
<svg viewBox="0 0 660 352" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bogan incir kalibi: ters vekil arkasinda rota rota tasinan uygulama ve gecici paylasimli veritabani">
  <defs>
    <marker id="netmod-incir-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
    <marker id="netmod-incir-eski" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#B07D20"/></marker>
    <marker id="netmod-incir-yeni" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#25804C"/></marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Çağıran hiçbir şey yapmaz; rota rota el değiştirir</text>
  <rect x="14" y="36" width="124" height="88" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="76" y="57" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kullanıcı ve</text>
  <text x="76" y="73" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">çağıran sistemler</text>
  <text x="76" y="94" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">adres aynı kalır</text>
  <text x="76" y="110" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">sözleşme aynı kalır</text>
  <line x1="140" y1="80" x2="158" y2="80" stroke="#5D7789" stroke-width="1.4" marker-end="url(#netmod-incir-ok)"/>
  <rect x="160" y="36" width="148" height="88" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <text x="234" y="57" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Ters vekil</text>
  <text x="234" y="73" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">rota bazında yönlendirir</text>
  <text x="234" y="89" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">özellik bayrağı burada okunur</text>
  <text x="234" y="105" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">geri alma kapısı da burada</text>
  <line x1="310" y1="62" x2="330" y2="56" stroke="#B07D20" stroke-width="1.4" marker-end="url(#netmod-incir-eski)"/>
  <line x1="310" y1="100" x2="330" y2="110" stroke="#25804C" stroke-width="1.4" marker-end="url(#netmod-incir-yeni)"/>
  <rect x="334" y="34" width="312" height="42" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="490" y="52" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Eski uygulama — henüz taşınmamış rotalar</text>
  <text x="490" y="68" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">.NET Framework, dokunulmadan çalışmaya devam eder</text>
  <rect x="334" y="88" width="312" height="42" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="490" y="106" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Yeni uygulama — taşınmış rotalar</text>
  <text x="490" y="122" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">.NET, kendi testleri, kendi gözlemlenebilirliği</text>
  <path d="M200 128 L200 152 L490 152 L490 140" fill="none" stroke="#B23B3B" stroke-width="1.4" stroke-dasharray="5 3"/>
  <text x="345" y="147" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">oturum ve kimlik iki tarafta birlikte yaşamak zorunda: ortak çerez alanı ya da merkezî sağlayıcı</text>
  <rect x="160" y="164" width="486" height="52" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="403" y="184" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Paylaşımlı veritabanı — geçici ve YAZILI olarak geçici</text>
  <text x="403" y="203" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">veri bölünmesi göçün son fazına konur; aynı anda bölmeye kalkmak göçü ikiye katlar</text>
  <rect x="14" y="228" width="316" height="112" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="172" y="248" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Özellik bayrağı ne kazandırır</text>
  <text x="172" y="270" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dağıtım ile yayına almayı ayırır</text>
  <text x="172" y="289" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">karşılaştırmalı koşum: iki yol, tek cevap</text>
  <text x="172" y="308" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">geri alma saniyeler sürer, dağıtım beklemez</text>
  <text x="172" y="329" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">bedeli: her bayrak bir dallanmadır, silinmezse borçtur</text>
  <rect x="344" y="228" width="302" height="112" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="248" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Büyük patlama neden batar</text>
  <text x="495" y="270" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eski sistem yeniden yazım boyunca donmaz</text>
  <text x="495" y="289" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hedef hareket eder, kapsam sürekli büyür</text>
  <text x="495" y="308" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">değer ancak en sonda görünür, ara kazanç yok</text>
  <text x="495" y="329" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kademeli göç uzun sürer ama her adımı geri alınabilir</text>
</svg>
```

### "Teknik borcu nasıl ölçer ve önceliklendirirsiniz?"

**Soru aslında ne ölçüyor?**

Bu, dosyanın en ayırt edici sorusudur ve ilanda "teknik borcun azaltılması" ayrı bir madde olarak yazıldığı için mutlaka gelir. Teknik borcun tanımını herkes yapar; ayrım iki yerde çıkar. Birincisi **ölçüm**: aday bir sayı üretebiliyor mu, yoksa kanaat mi beyan ediyor. İkincisi **çeviri**: teknik gerekçeyi iş etkisine dönüştürebiliyor mu. İlan "müşteri ve sonuç odaklı" diyor; bu cümle, "kod kirli" diyen adayın değil, "bu modüldeki teslim süresi üç günden üç saate iner" diyen adayın aranmakta olduğunu söylüyor. Borcu görünür kılmanın süreç yüzü `268`'de anlatıldı; burada ölçüm tarafı var.

**Sözlü cevap iskeleti**

1. **Önce borcu türlerine ayır.** Hepsi aynı şey değildir: yinelenen kod, güncelliğini yitirmiş bağımlılık, sıkı bağlaşım (tight coupling), eksik test, kötü gözlemlenebilirlik, performans darboğazı. Her birinin ödeme biçimi ve aciliyeti farklıdır.
2. **Bilinçli borç ile farkında olmadan biriken borcu ayır.** Teslim tarihi için bilerek alınmış ve bir yere yazılmış borç meşrudur; asıl tehlike, kimsenin karar vermediği, sessizce biriken borçtur. Bu ayrımı yapan aday konuyu gerçekten düşünmüştür.
3. **Ana ölçüt: değişim sıklığı × karmaşıklık kesişimi.** Sürüm geçmişinden hangi dosyanın kaç kez değiştiği çıkarılır, statik çözümlemeden karmaşıklık çıkarılır. **Sık değişen ve karmaşık olan dosya en riskli dosyadır.** Karmaşık ama iki yıldır dokunulmayan dosya risk değildir; sık değişen ama basit dosya zaten sağlıklıdır.
4. **Yan ölçütleri ekle.** Hata yoğunluğu (aynı modülden tekrar tekrar çıkan hata sayısı), teslim süresi (o modülde bir değişikliğin fikirden üretime geçme süresi), güvenlik açığı taraması (bilinen açığı olan bağımlılıklar), test kapsamının kritik akışlardaki boşluğu (`187`).
5. **Her borcu değil, akışı yavaşlatan borcu öde.** Ölçüt tek cümledir: bu borç bize her hafta ne kaybettiriyor. Kaybettirmiyorsa, çirkin olsa bile sıraya girmez.
6. **Teknik gerekçeyi iş etkisine çevir.** "Refactor için bir sprint istiyorum" cümlesi zayıftır çünkü karşı tarafa maliyet verir, kazanç vermez ve ölçülemez. "Bu modülde değişiklik süremiz üç günden üç saate iner, geri dönen iş oranı dörtte birden yirmide bire düşer" cümlesi güçlüdür çünkü kazancı sayıyla verir ve sonradan doğrulanabilir.
7. **Ödemeyi işin içine göm.** Borç genellikle ayrı bir projeyle değil, o modüle zaten dokunulacakken ödenir. Dokunduğun yeri bir tık daha iyi bırakmak, ayrı bütçe istemekten çok daha sürdürülebilirdir (`132`).
8. **Sonucu ölç ve geri getir.** Ödemeden önceki sayı ile sonraki sayı yan yana konur. Bu, bir sonraki borç kaleminin onaylanmasını sağlayan tek şeydir.

```svg
<svg viewBox="0 0 660 384" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Teknik borc onceliklendirme matrisi: degisim sikligi ile karmasiklik kesisimi ve dort bolge">
  <defs>
    <marker id="netmod-matris-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#1C3247"/></marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sık değişen ve karmaşık olan yer en riskli yerdir</text>
  <text x="330" y="38" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">değişim sıklığı sürüm geçmişinden, karmaşıklık statik çözümlemeden gelir — ikisi de kanaat değil, veri</text>
  <line x1="74" y1="298" x2="74" y2="52" stroke="#1C3247" stroke-width="1.5" marker-end="url(#netmod-matris-ok)"/>
  <line x1="74" y1="298" x2="600" y2="298" stroke="#1C3247" stroke-width="1.5" marker-end="url(#netmod-matris-ok)"/>
  <text x="40" y="175" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui" transform="rotate(-90 40 175)">Karmaşıklık →</text>
  <text x="337" y="320" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">Değişim sıklığı (son 12 ayda kaç kez dokunuldu) →</text>
  <rect x="84" y="60" width="248" height="112" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="208" y="82" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Karmaşık · seyrek değişen</text>
  <text x="208" y="104" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Çirkin ama sakin. Kimse dokunmuyorsa</text>
  <text x="208" y="122" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">faiz ödemiyorsun demektir.</text>
  <text x="208" y="144" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B07D20" font-family="system-ui">Karar: ELLEME, İZLE</text>
  <text x="208" y="162" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">yalnız güvenlik açığı varsa sıraya alınır</text>
  <rect x="342" y="60" width="248" height="112" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="2"/>
  <text x="466" y="82" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Karmaşık · sık değişen</text>
  <text x="466" y="104" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Faizi her hafta ödüyorsun. Hatalar da</text>
  <text x="466" y="122" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">teslim gecikmeleri de buradan çıkıyor.</text>
  <text x="466" y="144" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Karar: ÖNCE BURASI</text>
  <text x="466" y="162" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">karakterize et, kenar aç, böl, ölç</text>
  <rect x="84" y="180" width="248" height="110" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="208" y="202" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">Basit · seyrek değişen</text>
  <text x="208" y="224" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Ne acıtıyor ne risk üretiyor.</text>
  <text x="208" y="242" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Zaman harcamanın karşılığı yok.</text>
  <text x="208" y="264" text-anchor="middle" font-size="9.5" font-weight="700" fill="#5D7789" font-family="system-ui">Karar: GÖRMEZDEN GEL</text>
  <text x="208" y="282" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">buraya ayrılan sprint kaybedilmiş sprinttir</text>
  <rect x="342" y="180" width="248" height="110" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="466" y="202" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Basit · sık değişen</text>
  <text x="466" y="224" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Sağlıklı çekirdek. Sistem burada hızlı.</text>
  <text x="466" y="242" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Bozulmasını erken görmek gerekir.</text>
  <text x="466" y="264" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">Karar: KORU VE GÖZLE</text>
  <text x="466" y="282" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">test ve gözlemlenebilirlik burada tutulur</text>
  <rect x="14" y="334" width="632" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="352" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Son adım her zaman çeviridir: teknik gerekçe iş etkisine dönüştürülür</text>
  <text x="330" y="369" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">zayıf: "refactor için sprint istiyorum" · güçlü: "bu modülde değişiklik süresi 3 günden 3 saate iner"</text>
</svg>
```

**Elenme sebepleri**

- Hiç sayı verememek. Ölçüm sorulduğunda "koda bakınca anlaşılır" demek, kanaat beyanıdır.
- Yalnız bir statik çözümleme puanı söylemek. Karmaşıklık tek başına öncelik üretmez; değişim sıklığıyla kesiştirilmeden anlamı yoktur.
- Teknik gerekçeyi olduğu gibi sunmak. "Kod kirli" cümlesi hiçbir önceliklendirme toplantısından geçmez.
- Ayrı bir "refactor sprinti" istemek ve tek çare olarak sunmak. Bu talep bir kez onaylanır, ikincisi onaylanmaz.
- Bilinçli borç kavramını hiç anmamak. Her borcu bir hata gibi anlatan aday, teslim baskısını hiç yaşamamış gibi durur.
- Ödemeden sonra ölçmemek. Kazanç gösterilmeyen iyileştirme, bir sonraki sefer bütçe bulamaz.
- Güvenlik açığı taramasını atlamak. Bilinen açığı olan eski bağımlılık, "estetik" değil "acil" kutusundadır.

**Kendi hikâyen**

Bu soruda küçük ama gerçek bir vaka yeterlidir. Şu sorular onu çıkarır:

- Ekipte "dokunmaktan çekinilen" bir sınıf ya da modül var mı? Çekinme sebebini tek cümlede söyleyebilir misin?
- O modülde basit bir değişiklik ne kadar sürüyor? Aynı iş sağlıklı bir modülde ne kadar sürerdi? Bu farkı hiç ölçtün mü?
- Aynı yerden tekrar tekrar hata çıkıyor mu? Kaç kez, hangi aralıkla?
- Bilerek alınmış bir borç oldu mu? Ne ertelendi, bir yere yazıldı mı, sonra geri dönüldü mü?
- Bir borcu ödedikten sonra farkı ölçtün mü? Neyi ölçtün: süreyi mi, hata sayısını mı, olay sayısını mı?

Ölçmediysen ölçmediğini söyle. "Ölçmedik, kanaatle karar veriyorduk, bunun eksik olduğunu düşünüyorum" cümlesi, uydurulmuş bir metrikten çok daha güçlü duyulur.

### "Ürün sahibi acil bir özellik istiyor ama o modülde ciddi teknik borç var. Ne yaparsınız?"

**Soru aslında ne ölçüyor?**

Bu bir davranış sorusudur ve iki tuzağı vardır. Birinci tuzak "önce borcu ödeyelim, sonra özelliği yaparız" cevabıdır; bu, işi bloke eden mühendis profilidir ve ilanın "sonuç odaklı" maddesine terstir. İkinci tuzak "acil diyorsa yaparız, borç sonra" cevabıdır; bu da borcu büyüten ve hiçbir zaman geri dönmeyen profildir. Görüşmecinin aradığı **üçüncü yoldur** ve bunu bulabilen aday azdır. Ayrıca soru bir iletişim testidir: aday ürün sahibiyle ödünleşimi (trade-off) konuşabiliyor mu, yoksa kararı ya kabul ediyor ya reddediyor mu.

**Sözlü cevap iskeleti**

1. **Önce "acil" kelimesini netleştir.** Acil olan tarih mi, kapsam mı, yoksa bir müşteri taahhüdü mü. Çoğu zaman özelliğin çekirdeği acildir, tamamı değil. Bu tek soru bazen bütün gerilimi çözer.
2. **İki maliyeti masaya koy.** Borçlu modülde bu özelliğin süresi ne, kenar açıldıktan sonra süresi ne. Aradaki fark yatırımın büyüklüğüdür ve karar ürün sahibiyle **birlikte** verilir. Senin işin karar vermek değil, kararı görünür kılmaktır.
3. **Üçüncü yolu öner: özelliği teslim ederken kenar aç.** Bütün modülü düzeltmeye çalışma. Yalnız dokunacağın yüzeyi karakterizasyon testiyle çevrele, oraya bir arayüz koy, yeni davranışı temiz tarafa yaz. Modül bir bütün olarak hâlâ borçludur ama tam dokunduğun yerde bir tutamak oluşmuştur.
4. **Bu yatırımı ayrı bir kalem olarak isteme, tahmine göm.** "Özellik iki gün, temizlik bir gün" diye bölmek, temizliğin ilk kesilen kalem olmasına yol açar. Tahmin, işin yapılabilir hâlinin tahminidir.
5. **Bilinçli borç alacaksan yaz.** Bu sefer gerçekten kestirmeden gitmek gerekiyorsa bunu bir karar olarak kaydet: ne ertelendi, neden, hangi koşulda geri dönülecek. Kaydedilmiş borç yönetilebilir, kaydedilmemiş borç birikir (`268`).
6. **Riski dürüstçe söyle, tehdit gibi değil.** "Bu modüle testsiz dokunmak, geri dönen iş riskini artırır" cümlesi bilgi verir. "Bu koda dokunmam" cümlesi kapı kapatır.
7. **Sonraki sefere hazırlık bırak.** Bir dahaki değişiklik aynı yere geldiğinde açtığın kenar hazır olur. Modernizasyon böyle ilerler: proje olarak değil, birikimli olarak.

**Elenme sebepleri**

- "Önce refactor, sonra özellik" demek. İşi bloke eden bu duruş, ürün tarafında mühendisliğe olan güveni doğrudan azaltır.
- "Acilse yaparız, sonra bakarız" demek. "Sonra" hiç gelmez; bu cevap borcun nasıl biriktiğinin tarifidir.
- Kararı tek başına vermek. Ödünleşim ürün sahibiyle konuşulmadan alınırsa, sonuç ne olursa olsun yanlış anlaşılır.
- Temizliği ayrı bir satır olarak fiyatlandırmak. O satır ilk kesilen satırdır.
- Bütün modülü düzeltmeye kalkmak. Kapsam patlar, özellik gecikir ve bir daha kimse temizliğe izin vermez.
- Alınan kestirmeyi hiçbir yere yazmamak. Yazılmayan borç, üç ay sonra kimsenin sahiplenmediği bir hataya dönüşür.

**Kendi hikâyen**

Bu soruda anlatacağın şey neredeyse kesinlikle sende var. Şu sorular onu çıkarır:

- Kötü bir modüle acil bir özellik eklemek zorunda kaldığın oldu mu? Ne yaptın, sonrasında ne oldu?
- Ürün sahibine ya da yöneticiye teknik bir riski anlatman gerekti mi? Hangi cümle işe yaradı, hangisi yaramadı?
- "Şimdilik böyle yapalım" denen bir karar oldu mu? Bir yere yazıldı mı, geri dönüldü mü?

Köprü cümlesi: "Bu durumu Java tarafında yaşadım. Modülün tamamını düzeltmeye çalışmadım; yalnız dokunduğum yüzeyi testle çevreleyip arayüz açtım, özelliği zamanında teslim ettim ve bir sonraki değişiklik çok daha ucuz oldu."

### "Testi olmayan bir sınıfı nasıl yeniden düzenlersiniz?"

**Soru aslında ne ölçüyor?**

Bu soru bir kısır döngüyü kasten önüne koyar: güvenle yeniden düzenlemek (refactoring) için teste ihtiyacın var, test yazmak içinse sınıfın test edilebilir olması gerekiyor, ama sınıf test edilebilir değil çünkü yeniden düzenlenmemiş. Görüşmeci bu döngüyü nasıl kırdığına bakar. Cevabın merkezinde **karakterizasyon testi (characterization test)** olmalıdır. Bu terimi kullanan aday, konuyu okumuş olduğunu tek kelimeyle gösterir.

**Sözlü cevap iskeleti**

1. **Karakterizasyon testinin ne olduğunu doğru tanımla.** Bu test davranışın **doğru** olduğunu doğrulamaz; davranışın **mevcut hâlini kaydeder.** Bir girdi verilir, çıktı ne geliyorsa beklenen değer olarak yazılır. Çıktı saçma bile olsa yazılır.
2. **Neden böyle olduğunu açıkla.** Legacy sistemde spesifikasyon yoktur ya da güncel değildir; **kod spesifikasyondur.** Üretim on beş yıldır o davranışa göre çalışıyorsa, o davranış artık bir sözleşmedir; bir yerlerde birileri o "hataya" bağımlıdır. Yeniden düzenlemenin amacı davranışı korumaktır, düzeltmek değil. Düzeltmek ayrı bir iştir ve ayrı karar ister.
3. **Bu, TDD'nin tersidir; ikisini karıştırma.** TDD'de test önce yazılır ve **istenen** davranışı tanımlar (`261`). Karakterizasyonda test sonra yazılır ve **var olan** davranışı fotoğraflar. İkisi farklı amaç güder; bu ayrımı söylemek olgunluk gösterir.
4. **Kenar (seam) aç.** Kenar, davranışı değiştirmeden araya girebileceğin yerdir. Sınıf içinde `new` ile üretilen bağımlılık, statik çağrı, doğrudan veritabanı erişimi ve `DateTime.Now` gibi çevresel çağrılar kenarsızlığın klasik kaynaklarıdır. Bunlar arayüz arkasına alınır ya da parametreye çevrilir. Michael Feathers'ın legacy kod çerçevesi tam olarak bu problemi tarif eder.
5. **En küçük hamleyle başla.** Bağımlılığı kurucuya taşımak, statik çağrıyı sarmalayıcı arayüzün arkasına almak, uzun metodu bölmeden önce sadece imzasını değiştirmek. Bu ilk hamleler davranışı değiştirmez ve genellikle otomatik yeniden düzenleme araçlarıyla yapılabilir; araçla yapılan adım elle yapılandan daha güvenlidir.
6. **Kenar açamıyorsan üst seviyeden çevrele.** Sınıfa hiç dokunamıyorsan testi bir katman yukarıdan yaz: uç noktaya istek at, çıktıyı kaydet. Kaba ama işe yarar bir ağdır; kenar açıldıkça testi aşağı indirirsin (`187`).
7. **Ritmi söyle: küçük adım, yeşil test, tümleştir.** Her adım sonrası testler yeşil olur ve değişiklik ana dala gider (`191`, `220`). Haftalarca açık duran yeniden düzenleme dalı, birleştirme cehennemine ve sonunda çöpe gider.
8. **Ölç-uygula-ölç kuralını performansa da uygula.** "Bu kod yavaş görünüyor" diye başlanan iyileştirme genellikle yanlış yeri hızlandırır. Önce ölç, sonra değiştir, sonra tekrar ölç.

**Elenme sebepleri**

- Testi "doğru davranışı doğrulamak" diye tanımlamak. Legacy'de doğru davranışın ne olduğunu kimse bilmiyor olabilir; ölçüt bugünkü davranıştır.
- Test yazmadan doğrudan yeniden düzenlemeye başlamak. Yaptığın değişikliğin davranışı bozup bozmadığını gösterecek hiçbir şey olmaz.
- Yeniden düzenlemeyle davranış düzeltmeyi aynı adımda yapmak. Bir şey bozulduğunda hangisinin bozduğu anlaşılmaz.
- Kenar kavramından hiç söz etmemek. Test edilebilirliğin nasıl kazanıldığını anlatmayan cevap havada kalır.
- Sınıfı baştan yazmayı önermek. Testsiz bir sınıfı baştan yazmak, kaydedilmemiş davranışları sessizce kaybetmektir.
- "Kodda hata var, düzelttim" deyip haber vermemek. Üretimdeki bir davranış on beş yıldır sözleşmeydi; sessizce değiştirilemez.
- Performans iyileştirmesine ölçmeden başlamak. Tahmine dayanan optimizasyon çoğu zaman yanlış yeri hızlandırır ve kodu da kirletir.

**Kendi hikâyen**

Şu sorular seni kendi geçmişine götürsün:

- Testi olmayan bir sınıfa dokunmak zorunda kaldın mı? İlk hamlen ne oldu?
- Mevcut davranışı kaydetmek için test yazdığın oldu mu? O testlerde beklenen değer "doğru" muydu, "var olan" mıydı?
- Yeniden düzenleme sırasında farkında olmadan bir davranışı değiştirdiğin oldu mu? Nasıl fark edildi, kim fark etti?
- Kodda "hata gibi duran ama iş tarafının bağımlı olduğu" bir davranış gördün mü? Ne yaptın?
- Bir performans iyileştirmesine ölçerek mi başladın, tahmin ederek mi? Sonuç ne oldu?

### "Modernizasyon projesini nasıl ölçersiniz, ne zaman 'bitti' dersiniz?"

**Soru aslında ne ölçüyor?**

Bu soru olgunluk sorusudur ve çoğu aday burada tökezler; çünkü modernizasyonun bir bitiş tanımı olması gerektiğini hiç düşünmemiştir. Görüşmecinin gerçek endişesi somuttur: bu şirkette daha önce başlayıp bitmemiş bir modernizasyon olma ihtimali yüksektir. Sorunun altında "bizi yine sonu gelmeyen bir göçe sokar mısın" endişesi vardır. Ayrım iki yerde çıkar: bitişi ölçütle tanımlayabilmek ve **eski sistemin kapatılmasını** bitişin parçası saymak.

**Sözlü cevap iskeleti**

1. **Bitişi baştan tanımla.** Modernizasyon başlarken "bitti" ne demek yazılır. Örneğin: şu rota kümesi yeni tarafta, eski uygulama kapatıldı, çift bakım sona erdi. Yazılmayan bitiş, hiç gelmeyen bitiştir.
2. **Süreci ilerleme yüzdesiyle değil, kapatılan parça sayısıyla ölç.** "Yüzde 70 tamamlandı" ölçülemez bir cümledir. "On dört rotanın dokuzu taşındı, eski taraftaki dört uç nokta kapatıldı" ölçülebilir bir cümledir.
3. **Sonuç metriklerini ver ve öncesi-sonrası karşılaştır.** Değişiklik öncü süresi, dağıtım sıklığı, değişiklik hata oranı, ortalama kurtarma süresi (`191`, `268`); yanına taşınan modülün gecikmesi ve hata oranı.
4. **İş metriklerini de bağla.** Gelir operasyonlarını destekleyen bir platformda anlamlı sayı teknik değil iştir: destek çağrısı sayısı, mutabakat farkı, elle müdahale gereken işlem oranı, kesinti süresi (`296`).
5. **Çıkış kapısını (kapatma adımını) planın parçası yap.** Boğan incirin son adımı eski uygulamanın kapatılmasıdır. Bu adım plana yazılmazsa iki sistem kalıcı olarak birlikte yaşar. Bu, göçün en pahalı başarısızlık biçimidir: yeni sistemin maliyetine eskisinin bakımı eklenir, kazanç hiç görünmez.
6. **Sonsuz göç tuzağını adlandır.** Belirtileri şunlardır: taşınmış rota sayısı aylardır artmıyor, hiçbir eski modül kapatılmadı, özellik bayrakları temizlenmedi, ekibin yarısı iki taraf arasında senkron tutmakla uğraşıyor. Bu belirtiler görüldüğünde plan yeniden kurulur, ısrar edilmez.
7. **Kalan borcu dürüstçe raporla.** Her modernizasyon her borcu ödemez. Ödenmeyenler bilinçli borç olarak yazılır ve sahibi belli olur. Bitiş, "hiç borç kalmadı" değil, "kalan borç bilinen ve kabul edilmiş" demektir.

**Elenme sebepleri**

- Bitiş ölçütü verememek. Ölçütsüz göç, bütçe bitene kadar süren göçtür.
- İlerlemeyi yüzdeyle raporlamak. Yüzde bir kanaattir; kapatılan parça sayısı bir olgudur.
- Eski sistemin kapatılmasını plana koymamak. Kapatılmayan eski sistem, göçün kazancını bakım maliyetiyle götürür.
- Yalnız teknik metrik vermek. Gelir operasyonlarında karşılığı olmayan sayı, iş tarafında karşılık bulmaz.
- Öncesi ölçümünü almamak. "Öncesi" yoksa "sonrası" bir şey kanıtlamaz.
- "Modernizasyon bitmez, süreklidir" gibi kaçamak bir cevap vermek. Sürekli iyileştirme ayrı şeydir; bir göç projesinin bitişi olmak zorundadır.

**Kendi hikâyen**

Şu sorular onu çıkarır:

- Uzun süren bir teknik projede bulundun mu? Bitiş tanımı yazılı mıydı, kim yazmıştı?
- Eski bir sistem gerçekten kapatıldı mı, yoksa "bir süre daha dursun" denip kaldı mı? Sonucu ne oldu?
- Bir iyileştirmenin öncesini ve sonrasını ölçtün mü? Hangi sayıyı seçtin ve neden?
- Bitmeyen bir göç gördün mü? Belirtiler ne zaman ortaya çıktı, geri dönülebildi mi?

Köprü cümlesi: ".NET göçü yürütmedim; ama bitiş ölçütü yazılmamış uzun projelerin nasıl sürüklendiğini gördüm. Bu yüzden göç planında kapatma adımını ve öncesi-sonrası ölçümünü baştan isterim."

### Ne zaman modernize ETMEMELİ

Bu bölüm kısadır ama mülakatta ağırlığı yüksektir; çünkü adayların çoğu modernizasyonu her zaman iyi bir şey sayar ve "ne zaman yapmazsın" sorusuna hazırlıksız yakalanır.

Modernize edilmeyecek yerler şunlardır. **Değişmeyen, çalışan, riski düşük modül.** Yıllardır dokunulmayan bir toplu işlem gecelik olarak sorunsuz koşuyorsa, onu yeni çatıya taşımanın kazancı yoktur; kazancın olmadığı yerde her değişiklik net risktir. **Emeklilik takvimi olan sistem.** On sekiz ay sonra kapatılacak bir uygulamayı modernize etmek, satılacak eve mutfak yaptırmaktır; doğru iş, o sistemi kapanana kadar ayakta tutmaktır. **İş tarafının kapsamı değişmek üzere olan modül.** Süreç yeniden tasarlanacaksa, mevcut davranışı özenle taşımak boşa emektir. **Ekibin o alanda bilgisinin olmadığı ve öğrenme maliyetinin kazancı aştığı yerler.**

Ancak bir istisna vardır ve söylenmesi gerekir: **güvenlik.** Değişmeyen ve sakin duran bir modül bile, bilinen açığı olan bir bağımlılık taşıyorsa ya da desteği bitmiş bir çalışma zamanında koşuyorsa sıraya girer. Orada ölçüt "acıtıyor mu" değil, "açık mı"dır.

Mülakatta bu bölümü söylemenin etkisi şudur: modernizasyonu savunan aday çoktur, modernizasyonun **ne zaman yanlış olduğunu** söyleyen aday azdır. İkincisi kaynak dağıtımı yapabilen bir kıdemli gibi duyulur.

### Bu bölümden çıkan ders

Bu dosyanın tek cümlesi şudur: **legacy modernizasyonu bir teknoloji işi değil, risk yönetimi işidir.** .NET Framework'ten .NET'e geçiş bir sürüm yükseltmesi değil çatı değişimidir; `System.Web` gitmiştir ve statik bağlam artık bağımlılık enjeksiyonuyla taşınır, `Web.config` yerini seçenekler kalıbına bırakır, Web Forms ve `AppDomain` gibi bazı kalemlerin ise karşılığı hiç yoktur — ve karşılığı olmayanı bilmek olanı bilmekten değerlidir. Yol büyük patlama yeniden yazımı değil, ters vekil arkasında rota rota taşıyan boğan incirdir; özellik bayrağı dağıtımı yayına almadan ayırır, karşılaştırmalı koşum doğruluğu üretim verisiyle kanıtlar, geri alma kapısı her adımı geri alınabilir tutar. Paylaşımlı veritabanı bu süreçte geçici olarak kabul edilir ve bunun yazılı olarak geçici olması şarttır. Dokunmadan önce ağ örülür: karakterizasyon testi doğruyu değil bugünkü davranışı kaydeder, çünkü legacy'de spesifikasyon yoktur, kod spesifikasyondur. Teknik borç ise kanaatle değil veriyle sıralanır — değişim sıklığı ile karmaşıklığın kesiştiği yer önce gelir, karmaşık ama sakin olan yer beklemeye devam eder — ve her borç değil, akışı yavaşlatan borç ödenir. Son adım her zaman çeviridir: "refactor için sprint istiyorum" cümlesi kapalı bir kapıdır, "bu modülde değişiklik süremiz üç günden üç saate iner" cümlesi açık bir kapı. Ve bitiş baştan yazılır; kapatılmayan eski sistem, göçün kazancını çift bakım maliyetiyle geri alır.

# netmod-1 | Temel
## Soru
.NET Framework'te her katmandan çağrılabilen `HttpContext.Current`, modern .NET'te yoktur. Bu kırılmanın modern .NET'teki karşılığı ve asıl sonucu nedir?
## A
Bağlam bağımlılık enjeksiyonuyla taşınır (`IHttpContextAccessor`); statik erişime bağımlı sınıflar bunu bırakmak zorunda kalır
🤖 Doğru: statik bağlam gider, bağlam enjekte edilir ve sınıf test edilebilir hâle gelir.
## B
`HttpContext.Current` aynen durur; yalnız ad alanı değiştiği için yeni bir `using` satırı eklemek yeterlidir
🤖 statik bağlam modern .NET'e taşınmadı; ad alanı meselesi değildir.
## C
Bağlam artık bir iş parçacığı yerel değişkeninde tutulur; kod hiç değişmeden aynı biçimde çalışmaya devam eder
🤖 çağrı yüzeyi kaldırıldığı için eski kod olduğu gibi çalışamaz.
## D
Bağlam yalnız denetleyicilerde okunabilir; alt katmanların bağlama erişmesi çatı tarafından tamamen engellenir
🤖 alt katman da erişebilir, ama enjekte edilen erişimci üzerinden.
## Doğru
A
## Açıklama
Bu, .NET Framework'ten .NET'e geçişin **en sert kırılmasıdır** ve bir sürüm farkı değil, bir mimari değişimdir. Eski dünyada `HttpContext.Current` statik bir özelliktir; bir hesaplama sınıfının içinden oturuma ya da kullanıcıya erişmek mümkündür ve on beş yıllık kod tabanlarında yaygındır. Modern .NET'te bu statik bağlam yoktur; bağlam bağımlılık enjeksiyonuyla taşınır ve ihtiyaç duyan sınıf `IHttpContextAccessor` alır (`282`). Sonuç iki yönlüdür. Maliyet yönü: bağlama gizlice erişen her sınıf ya bağlamı açıkça alacak ya da bağlamdan kopacaktır; bu, mekanik bir çeviri değil, tasarım işidir. Kazanç yönü: statik bağlam söküldüğü anda o sınıf ilk kez birim testine girebilir hâle gelir. Yani göçün kendisi eski borcu ödemeye zorlar. B ve C şıkları kırılmayı bir ad alanı ya da saklama biçimi meselesine indirger; ikisi de yanlıştır çünkü çağrı yüzeyi tamamen kaldırılmıştır. D şıkkı yanlış bir kısıt uydurur: alt katman da bağlama erişebilir, ama artık gizlice değil, bağımlılığı görünür biçimde.

# netmod-2 | Orta
## Soru
Boğan incir kalıbıyla (strangler fig) taşınan bir hesaplama modülünde, yeni yolun doğruluğunu üretim verisiyle kanıtlamak isteniyor. En yerinde yaklaşım hangisidir?
## A
Yeni yolu doğrudan üretimde açıp ilk hafta gelen destek çağrılarına göre düzeltmeler yapmak
🤖 doğruluğu kullanıcıya ödetir; hesaplama modülünde kabul edilemez.
## B
Özellik bayrağıyla karşılaştırmalı koşum yapmak: iki yol da çalışır, cevabı eski yol verir, farklar raporlanır
🤖 Doğru: doğruluk üretim verisiyle, kullanıcıya risk vermeden kanıtlanır.
## C
Yeni yolu yalnız test ortamında üretilmiş sentetik veriyle doğrulayıp doğrudan geçişi yapmak
🤖 sentetik veri üretimdeki uç vakaları ve tarihsel kirliliği içermez.
## D
Eski ve yeni yolu aynı anda kullanıcıya gösterip hangisinin sonucunun doğru olduğunu kullanıcıya sordurmak
🤖 doğruluk kararı kullanıcıya devredilemez; tutarsız çıktı güveni bitirir.
## Doğru
B
## Açıklama
Karşılaştırmalı koşum (shadow run / parallel run), göç sırasında doğruluğu kanıtlamanın en ucuz ve en az riskli yoludur. Kurgu şudur: gelen istek her iki yola da gider, kullanıcıya dönen cevabı **eski yol** üretir, yeni yolun ürettiği sonuç yalnızca kaydedilir ve farklar raporlanır. Bu sayede doğruluk sentetik veriyle değil, gerçek üretim verisiyle — yani gerçek uç vakalar ve on beş yıllık veri kirliliğiyle — sınanır; buna rağmen kullanıcı hiçbir risk almaz. Mekanizmayı mümkün kılan şey özellik bayrağıdır (feature flag): kod üretime **kapalı** dağıtılır, yani dağıtım ile yayına alma birbirinden ayrılır (`191`), ve bir sorun çıktığında yeni dağıtım beklemeden bayrak kapatılarak trafik saniyeler içinde eski yola döner. C şıkkındaki sentetik veri, legacy sistemlerde en çok yanıltan yaklaşımdır; asıl farklar tarihsel kayıtlarda çıkar. A ve D şıkları doğruluk maliyetini kullanıcıya yükler. Bayrağın bir bedeli olduğunu da eklemek gerekir: her bayrak bir dallanmadır ve göç bitince bayrak da eski yol da silinir; silinmeyen bayrak yeni bir teknik borçtur.

# netmod-3 | Orta
## Soru
Testi olmayan, on beş yıllık bir hesaplama sınıfına dokunmadan önce karakterizasyon testi (characterization test) yazıyorsun. Bir girdi için sınıfın ürettiği çıktının iş kuralına göre **yanlış** olduğunu fark ediyorsun. Testi nasıl yazarsın?
## A
Beklenen değer olarak mevcut (yanlış görünen) çıktıyı yazarsın; davranış değişikliği ayrı bir karar ve ayrı bir iştir
🤖 Doğru: karakterizasyon testi doğruyu değil, bugünkü davranışı kaydeder.
## B
Beklenen değer olarak iş kuralına göre doğru olan değeri yazar, testi kırmızı bırakıp yeniden düzenlemeye başlarsın
🤖 kırmızı test, yeniden düzenleme sırasında hiçbir güvenlik ağı sağlamaz.
## C
O girdiyi test kümesinden çıkarır, yalnız doğru sonuç ürettiğinden emin olduğun girdilerle çalışırsın
🤖 en riskli davranışı kapsam dışı bırakmak, ağı tam gerektiği yerde deler.
## D
Testi yazmayı erteler, önce sınıfı düzeltir, sonra düzeltilmiş hâlin testini yazarsın
🤖 testsiz düzeltme, neyin bozulduğunu gösterecek hiçbir ölçüt bırakmaz.
## Doğru
A
## Açıklama
Karakterizasyon testinin amacı davranışın **doğru** olduğunu doğrulamak değil, **mevcut hâlini kaydetmektir**. Legacy sistemde spesifikasyon yoktur ya da güncel değildir; kod spesifikasyondur. Üretim on beş yıldır bu çıktıyı ürettiyse, o çıktı artık fiilî bir sözleşmedir: aşağı akıştaki bir rapor, bir mutabakat dosyası ya da bir iş ortağı ona bağımlı olabilir. Bu yüzden yeniden düzenlemenin (refactoring) tanımı gereği davranış **korunur**, düzeltilmez. Fark ettiğin sapma kaybolmaz; ayrı bir bulgu olarak kaydedilir, iş tarafına götürülür ve düzeltme ayrı bir değişiklik olarak, kendi testiyle ve kendi duyurusuyla yapılır. İkisini aynı adımda yaparsan bir şey bozulduğunda yeniden düzenlemenin mi yoksa davranış değişikliğinin mi bozduğunu ayırt edemezsin. Bu, TDD'nin tam tersi bir yöndür ve karıştırılmamalıdır: TDD'de test önce yazılır ve **istenen** davranışı tanımlar (`261`); karakterizasyonda test sonra yazılır ve **var olan** davranışı fotoğraflar. B şıkkındaki kırmızı test hiçbir güvenlik ağı sağlamaz. C şıkkı ağı tam da en riskli girdide deler. D şıkkı kısır döngüyü hiç kırmaz.

# netmod-4 | İleri
## Soru
Bir kod tabanında dört dosya var: (1) çok karmaşık, son bir yılda hiç değişmemiş; (2) çok karmaşık, ayda birkaç kez değişiyor; (3) basit, sık değişiyor; (4) basit, hiç değişmiyor. Teknik borç ödemesi için hangisi önce sıraya girer ve neden?
## A
(1) — karmaşıklık puanı en yüksek olan dosya, sıralamanın tek geçerli ölçütüdür
🤖 karmaşıklık tek başına öncelik üretmez; dokunulmayan karmaşıklık faiz ödetmez.
## B
(2) — sık değişen ve karmaşık olan dosya faizi her hafta ödettiği için ilk sırada durur
🤖 Doğru: değişim sıklığı ile karmaşıklığın kesişimi en riskli bölgedir.
## C
(3) — en sık dokunulan dosya, dokunma sayısı yüksek olduğu için en yüksek riski taşır
🤖 sıklık tek başına risk değildir; basit ve sık değişen dosya sağlıklı çekirdektir.
## D
(4) — hiç değişmeyen dosya bilgi kaybı riski taşıdığı için önce elden geçirilmelidir
🤖 ne acıtan ne risk üreten bir yere ayrılan zaman kaybedilmiş zamandır.
## Doğru
B
## Açıklama
Teknik borç sıralaması kanaatle değil veriyle yapılır ve ana ölçüt **değişim sıklığı ile karmaşıklığın kesişimidir**. Değişim sıklığı sürüm geçmişinden, karmaşıklık statik çözümlemeden çıkarılır; ikisi de ölçülebilir. Dört bölgenin kararı farklıdır. Karmaşık ve sık değişen (2): faizi her hafta ödüyorsun, hatalar ve teslim gecikmeleri buradan çıkıyor — **önce burası**; karakterize et, kenar aç, böl, ölç. Karmaşık ama seyrek değişen (1): çirkin ama sakin, kimse dokunmuyorsa faiz ödemiyorsun — elleme, izle. Basit ve sık değişen (3): sağlıklı çekirdek, sistem orada hızlı — koru ve gözle, test ile gözlemlenebilirliği orada tut. Basit ve seyrek değişen (4): ne acıtıyor ne risk üretiyor — görmezden gel. Ölçüte yan veriler eklenir: hata yoğunluğu, o modüldeki değişikliğin teslim süresi ve güvenlik açığı taraması. **Tek istisna güvenliktir**: sakin bölgedeki bir dosya bile bilinen açığı olan bir bağımlılık taşıyorsa sıraya girer; orada ölçüt "acıtıyor mu" değil "açık mı"dır. Önceliklendirmenin son adımı her zaman iş etkisine çeviridir (`268`, `132`).

# netmod-5 | İleri
## Soru
Ürün sahibi, ciddi teknik borç taşıyan bir modüle acil bir özellik istiyor. Kıdemli bir geliştirici olarak en yerinde davranış hangisidir?
## A
Özelliği reddedip önce modülün tamamının yeniden düzenlenmesini bir sprint olarak talep etmek
🤖 işi bloke eder; bu talep bir kez onaylanır, ikincisi hiç onaylanmaz.
## B
Özelliği borçlu modüle olduğu gibi eklemek; temizliği daha sakin bir döneme bırakmak
🤖 "sonra" gelmez; bu tam olarak borcun nasıl biriktiğinin tarifidir.
## C
Özelliği teslim ederken yalnız dokunulan yüzeyi testle çevreleyip kenar açmak, maliyet farkını ürün sahibiyle konuşmak
🤖 Doğru: üçüncü yol — özellik zamanında çıkar, modülde bir tutamak kalır.
## D
Özelliği ayrı bir yeni serviste yazıp borçlu modülü hiç açmamak, iki taraf arasında veriyi senkron tutmak
🤖 kaçınma, borcu ödemez; üstüne dağıtık senkron sorunu ekler.
## Doğru
C
## Açıklama
Bu sorunun iki hazır tuzağı vardır. "Önce borcu ödeyelim" (A) işi bloke eden mühendis profilidir ve ilanın sonuç odaklılık maddesine terstir; ayrıca ayrı bir temizlik sprinti talebi genellikle bir kez onaylanır, ikincisi onaylanmaz. "Acilse yaparız, sonra bakarız" (B) ise borcun nasıl biriktiğinin birebir tarifidir; "sonra" hiç gelmez. Aranan cevap **üçüncü yoldur**: modülün tamamını düzeltmeye kalkma, yalnız dokunacağın yüzeyi karakterizasyon testiyle çevrele, oraya bir kenar (seam) aç ve yeni davranışı temiz tarafa yaz. Modül bütün olarak hâlâ borçludur ama tam dokunduğun yerde bir tutamak oluşmuştur; bir sonraki değişiklik aynı yere geldiğinde çok daha ucuz olur. Bu yatırım ayrı bir kalem olarak fiyatlandırılmaz, tahminin içine gömülür — ayrı satır, ilk kesilen satırdır. Kararın kendisi tek başına verilmez: iki maliyet (borçlu hâliyle süre, kenar açıldıktan sonraki süre) ürün sahibine gösterilir ve ödünleşim (trade-off) birlikte konuşulur; senin işin karar vermek değil, kararı görünür kılmaktır. Gerçekten kestirmeden gidilecekse bu bilinçli bir borç olarak yazılır: ne ertelendi, neden, hangi koşulda geri dönülecek (`268`). D şıkkı borçlu modülden kaçar ama yerine dağıtık veri senkronu koyar; borç azalmaz, tür değiştirir (`96`).

# netmod-6 | Uzman
## Soru
Bir modernizasyon on sekiz aydır sürüyor. Rotaların çoğu yeni tarafa taşınmış, iki sistem birlikte çalışıyor, veritabanı hâlâ paylaşımlı ve bugüne kadar eski taraftan hiçbir modül kapatılmadı. En yerinde teşhis hangisidir?
## A
Sorun paylaşımlı veritabanıdır; göç durdurulup önce veri katmanı tamamen bölünmeli, sonra rotalara devam edilmelidir
🤖 paylaşımlı veritabanı göç sırasında bilinçli ve geçici bir ödünleşimdir.
## B
Sorun yoktur; boğan incir doğası gereği uzun sürer ve iki sistemin birlikte yaşaması kalıcı olarak kabul edilebilir
🤖 kalıcı çift yaşam, göçün kazancını çift bakım maliyetiyle geri alır.
## C
Çıkış kapısı planlanmamış; kapatma adımı olmayan göç çift bakım üretir, plan bitiş ölçütüyle yeniden kurulmalıdır
🤖 Doğru: boğan incirin son adımı eski tarafın kapatılmasıdır.
## D
Ekip yetersizdir; aynı kaynakla devam edilirse bitmez, çözüm göç ekibini büyütmek ve taşıma hızını artırmaktır
🤖 hızı artırmak, tanımsız bir bitişe daha hızlı gitmek demektir.
## Doğru
C
## Açıklama
Buradaki belirtiler bir araç ya da kapasite sorununu değil, **planın eksik bir adımını** gösterir: boğan incirin son adımı eski uygulamanın kapatılmasıdır ve bu adım plana yazılmamışsa göç doğal olarak bitmez. Sonuç, göçün en pahalı başarısızlık biçimidir: yeni sistemin maliyetine eskisinin bakımı eklenir, ekibin bir bölümü iki tarafı senkron tutmakla uğraşır, kazanç hiç görünmez ve bir sonraki modernizasyon talebi bu yüzden reddedilir. Doğru davranış plana bir bitiş tanımı koymaktır: hangi rota kümesi taşındığında eski uygulama kapatılacak, çift bakım ne zaman sona erecek. İlerleme "yüzde 70 tamamlandı" gibi bir kanaatle değil, kapatılan parça sayısıyla raporlanır — bu, olgu üretir. B şıkkı kalıcı çift yaşamı normalleştirir. D şıkkı tanımsız bir bitişe daha hızlı gitmeyi önerir. A şıkkı en çekici yanlıştır: paylaşımlı veritabanı gerçekten bir kötü kalıptır, ama göç sırasında **bilinçli ve yazılı olarak geçici** bir ödünleşimdir; veri katmanını rotalarla aynı anda bölmeye kalkmak göçü ikiye katlar ve genellikle batırır — veri bölünmesi son faza konur (`283`, `136`). Bitiş, "hiç borç kalmadı" demek değildir; kalan borcun bilinen, yazılı ve sahiplenilmiş olması demektir.
