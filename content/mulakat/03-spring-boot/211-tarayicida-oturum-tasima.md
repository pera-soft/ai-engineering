---
slug: tarayicida-oturum-tasima
baslik: "Tarayıcıda Oturum: HttpOnly Cookie + SameSite mi, localStorage'da JWT mi?"
kademe: İleri
sira: 211
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Bir spor salonuna iki türlü girebilirsin. Birincisi: bileğine takılan bir **bileklik** vardır, turnikeye yaklaştığın anda okuyucu onu kendiliğinden okur; sen hiçbir şey yapmazsın. İkincisi: kimliğin cebindedir, her kapıda **sen çıkarıp gösterirsin**. İki yöntem de seni içeri alır ama zayıf noktaları farklıdır. Bileklik koluna takılıyken biri seni kapıya doğru itiverirse turnike açılır; sen istemesen bile geçmiş olursun. Cebindeki kimliği ise yankesici çalabilir ve senin olmadığın bir yerde kullanabilir.

Web'de de durum aynıdır. HTTP **durumsuzdur** (stateless): sunucu iki isteği kendiliğinden birbirine bağlamaz, bu yüzden her istekte "ben kimim" bilgisini yeniden taşımak gerekir. Bu bilgiyi taşımanın iki yolu vardır: **çerez** (cookie) — tarayıcı otomatik ekler; **`Authorization: Bearer` başlığı** — JavaScript elle ekler. Bu tek fark, "token'ı nereye koyalım?" tartışmasının tamamının kaynağıdır ve backend geliştiriciye mülakatta en çok sorulan ön yüz sınırı sorusudur.

### Temel kavramlar

- **Durumsuzluk (statelessness)**: Sunucunun iki isteği kendiliğinden ilişkilendirmemesi; kimliğin her istekte yeniden taşınması gerekliliği.
- **Çerez (cookie)**: Sunucunun `Set-Cookie` başlığıyla tarayıcıya yazdırdığı, tarayıcının eşleşen sonraki isteklere **kendiliğinden** eklediği küçük anahtar–değer verisi.
- **Oturum kimliği (session id)**: Sunucuda tutulan oturum kaydına işaret eden, kendi başına anlam taşımayan rastgele dize.
- **`Authorization: Bearer <token>` başlığı**: Token'ın istekte taşındığı standart yer; tarayıcı bu başlığı **asla kendiliğinden eklemez**, uygulama kodu ekler.
- **`HttpOnly`**: Çerezi sayfa betiğine kapatan öznitelik; `document.cookie` ile okunamaz.
- **`Secure`**: Çerezin yalnız HTTPS bağlantılarında gönderilmesini zorlayan öznitelik.
- **`SameSite`**: Çerezin **siteler arası** isteklerde gönderilip gönderilmeyeceğini belirleyen öznitelik (`Strict` / `Lax` / `None`).
- **`localStorage` ve `sessionStorage` (tarayıcı deposu)**: Betiğin okuyup yazabildiği tarayıcı depoları; `sessionStorage` sekme kapanınca silinir, `localStorage` kalıcıdır. İkisi de **isteklere otomatik eklenmez**.
- **XSS (cross-site scripting — siteler arası betik çalıştırma)**: Saldırganın senin sayfanda kendi JavaScript'ini çalıştırabilmesi.
- **CSRF (cross-site request forgery — siteler arası istek sahteciliği)**: Kötü bir sitenin, kurbanın tarayıcısına senin sitene istek attırması; çerez otomatik gittiği için istek meşru görünür.
- **Senkronizatör token deseni (synchronizer token pattern)**: Sunucunun ürettiği rastgele değerin isteğe **elle** eklenmesini şart koşan CSRF savunması (Spring Security'de `CsrfToken`; SPA'da `XSRF-TOKEN` çerezi + `X-XSRF-TOKEN` başlığı).
- **Erişim token'ı (access token) / yenileme token'ı (refresh token)**: Kısa ömürlü, API'ye giden bilet; ve onun süresi dolunca yenisini almaya yarayan, daha uzun ömürlü ikinci bilet.
- **Rotasyon (refresh token rotation)**: Yenileme token'ının her kullanımda yenisiyle değiştirilmesi; eski kopya tekrar gelirse hırsızlık sayılıp zincirin iptal edilmesi.
- **Geri alma (revocation)**: Bir kimlik bilgisinin süresi dolmadan geçersiz kılınması.
- **Sunucu-taraflı oturum (`HttpSession`)**: Kullanıcı durumunun sunucuda tutulması; istemcide yalnız oturum kimliği taşınır.
- **BFF (backend for frontend)**: Yalnız bir ön yüzü besleyen ince arka uç katmanı; token'ı sunucu tarafında tutabilir.
- **CSP (content security policy — içerik güvenliği politikası)**: Sayfada hangi kaynaktan betik çalışabileceğini kısıtlayan HTTP başlığı; XSS'e karşı yapısal savunma.

### Nasıl çalışır?

Her şey tek bir cümleye dayanır: **çerezi tarayıcı ekler, `Authorization` başlığını sen eklersin.** Sunucu bir çerezi şöyle yazdırır:

```
Set-Cookie: SESSION=8f3c...; HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=1800
```

Bu satırdan sonra tarayıcı, `Path` ve `Domain` ile eşleşen **her** isteğe bu çerezi kendiliğinden takar. "Her istek" gerçekten her istektir: sayfa gezinmesi, resim, `fetch` çağrısı, hatta başka bir sitedeki formun gönderimi. `Max-Age`/`Expires` ömrü belirler; verilmezse çerez tarayıcı kapanınca silinir. Kolaylık burada, tehlike de burada: **otomatik gönderim CSRF'in kök nedenidir.**

```svg
<svg viewBox="0 0 640 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Çerez ile Authorization başlığının karşılaştırması: kim ekliyor ve hangi zafiyet doğuyor">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kimliği KİM ekliyor? Zafiyet oradan doğuyor</text>
  <rect x="18" y="38" width="290" height="184" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="163" y="62" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Çerez (cookie)</text>
  <text x="163" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Ekleyen: TARAYICI, otomatik</text>
  <rect x="40" y="96" width="246" height="30" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.1"/>
  <text x="163" y="115" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">HttpOnly + Secure + SameSite</text>
  <text x="163" y="146" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">✓ betik okuyamaz → XSS ile çalınamaz</text>
  <rect x="40" y="158" width="246" height="48" rx="8" fill="#FCEDEC" stroke="#B3261E" stroke-width="1.4"/>
  <text x="163" y="178" text-anchor="middle" font-size="11" font-weight="700" fill="#B3261E" font-family="system-ui">Doğan zafiyet: CSRF</text>
  <text x="163" y="196" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">otomatik gittiği için sahte istek kimlikli görünür</text>
  <rect x="332" y="38" width="290" height="184" rx="10" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.5"/>
  <text x="477" y="62" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C3247" font-family="system-ui">Authorization: Bearer</text>
  <text x="477" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Ekleyen: JAVASCRIPT, elle</text>
  <rect x="354" y="96" width="246" height="30" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.1"/>
  <text x="477" y="115" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">token bir yerde saklanmalı: localStorage?</text>
  <text x="477" y="146" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">✓ otomatik gitmez → CSRF zemini yok</text>
  <rect x="354" y="158" width="246" height="48" rx="8" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.4"/>
  <text x="477" y="178" text-anchor="middle" font-size="11" font-weight="700" fill="#B67A1A" font-family="system-ui">Doğan zafiyet: XSS ile hırsızlık</text>
  <text x="477" y="196" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">betik depoyu okur, token'ı dışarı taşır</text>
</svg>
```

**CSRF nasıl işler?** Kurban senin sitene giriş yapmış, oturum çerezi tarayıcısında duruyor. Sonra kötü bir siteye uğruyor; o sitedeki gizli bir form ya da bir `<img>` etiketi senin sunucuna istek atıyor. Tarayıcı, isteği **kimin tetiklediğine** bakmaz — hedef adres senin siten olduğu için çerezi yine otomatik ekler. Sunucun isteği meşru sanar ve para transferini, şifre değişimini, silme işlemini yapar. Saldırganın çerezi **okumasına gerek yoktur**; okumadan kullandırır.

Savunma katmanları sırasıyla şöyledir.

1. **`SameSite`** çerez özniteliği. `Lax` modern tarayıcılarda pratik varsayılandır ve çerezi siteler arası **POST**larda göndermez. `Strict` daha katıdır ama dış bağlantıdan gelen kullanıcıyı çıkış yapmış gösterir. `None` ise gerçekten siteler arası kullanım gerektirir ve `Secure` ile birlikte zorunludur.
2. **Senkronizatör token deseni.** Sunucu rastgele bir değer üretir, istemci onu **elle** başlığa koyar. Kötü niyetli site bu değeri okuyamadığı için isteği üretemez.
3. **`Origin`/`Referer` denetimi.** İsteğin hangi siteden tetiklendiğine bakmak.

`SameSite` birincil savunmadır ama **tek başına yeterli sayılmaz**. Eski tarayıcılar, alt alan adları ve yanlış yapılandırma boşluk bırakır; bu yüzden katman katman (derinlemesine) savunma önerilir.

```svg
<svg viewBox="0 0 640 244" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="CSRF akışı: kötü site kurbanın tarayıcısına istek attırır, çerez otomatik eklenir; SameSite bu gönderimi keser">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">CSRF akışı ve SameSite'ın kestiği nokta</text>
  <rect x="16" y="46" width="150" height="62" rx="9" fill="#FCEDEC" stroke="#B3261E" stroke-width="1.5"/>
  <text x="91" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B3261E" font-family="system-ui">Kötü site</text>
  <text x="91" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">gizli form / img</text>
  <polygon points="166,72 196,72 196,66 212,77 196,88 196,82 166,82" fill="#5D7789"/>
  <rect x="222" y="46" width="164" height="62" rx="9" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.5"/>
  <text x="304" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kurbanın tarayıcısı</text>
  <text x="304" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">oturum çerezi burada</text>
  <polygon points="386,72 424,72 424,66 440,77 424,88 424,82 386,82" fill="#5D7789"/>
  <rect x="450" y="46" width="174" height="62" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="537" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Senin sunucun</text>
  <text x="537" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">"kimlikli istek" sanır</text>
  <text x="413" y="128" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B3261E" font-family="system-ui">çerez OTOMATİK eklenir</text>
  <path d="M413 138 L413 156" stroke="#25804C" stroke-width="2" stroke-dasharray="5 4" fill="none"/>
  <rect x="304" y="158" width="220" height="30" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="414" y="177" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">SameSite=Lax burada keser</text>
  <rect x="16" y="196" width="608" height="34" rx="8" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.3"/>
  <text x="320" y="217" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Tek katman yetmez: senkronizatör token (X-XSRF-TOKEN) + Origin denetimi eklenir</text>
</svg>
```

**Peki `localStorage` tarafı?** Token'ı `localStorage`'a koyduğunda tarayıcı onu hiçbir isteğe otomatik eklemez; ekleyen senin kodundur. Bu, saf `Bearer` kurulumunu **CSRF'e yapısal olarak kapalı** yapar — token'cıların en güçlü argümanı budur. Bedeli şudur: `localStorage`'ı **JavaScript okuyabilir**, dolayısıyla sayfada tek bir XSS açığı token'ın saldırganın sunucusuna gönderilmesine yeter.

Buna karşı sık duyulan itiraz şudur: "XSS varsa zaten her şey biter, fark etmez." Dürüst cevap: **fark eder.** `HttpOnly` çerezde saldırgan token'ı **dışarı çıkaramaz**. Yapabileceği tek şey, kurbanın tarayıcısından ve kurban o sayfadayken istek atmaktır. Bunun etki alanı ve süresi dardır; sunucu tarafında olağandışı davranış olarak yakalanabilir. Çalınan bir token ise saldırganın kendi makinesinden, kurban çevrimdışıyken bile, süresi dolana dek kullanılır. Yine de asıl savunmanın **çıktı kaçışlama (escaping)** ve **CSP** olduğunu unutma; depolama seçimi ikincil, hasarı sınırlayan bir savunmadır.

**Token ömrü, yenileme ve geri alınamama.** Yaygın kurulum: dakikalarla ölçülen kısa ömürlü bir **erişim token'ı** + günlerle ölçülen bir **yenileme token'ı**. Yenileme token'ı en uzun ömürlü sırdır. Bu yüzden en korunaklı yerde durmalıdır: `HttpOnly` + `Secure` + `SameSite` bir çerezde. Ayrıca **rotasyon** uygulanır, yani her kullanımda yenisi verilir. Eski bir kopya tekrar gelirse hırsızlık varsayılır ve o zincirdeki tüm token'lar iptal edilir. Token'ı URL sorgu parametresinde taşımak ise hiçbir zaman doğru değildir: URL'ler sunucu günlüklerine, tarayıcı geçmişine ve `Referer` başlığına sızar.

Buradan mülakatın ayırt edici sorusuna geliriz: **imzalı bir JWT geri alınamaz.** Sunucu durum tutmadığı için, kullanıcı "çıkış yap"a bastığında yapılan tek şey token'ı istemciden silmektir. Token'ın bir kopyası başka yerdeyse **süresi dolana dek çalışmaya devam eder**. Çareler: ömrü kısa tutmak, iptal edilenleri bir **kara listede** (genelde Redis) tutmak, ya da baştan **sunucu-taraflı oturum** kullanmak. Kara liste eklediğin anda durumsuzluğun bir kısmından zaten vazgeçmiş olursun — bu takası bilmek kıdem göstergesidir.

**Sunucu-taraflı oturum hâlâ meşru mu?** Evet. "JWT modern, session eski" bir yanılgıdır. `HttpSession`, tek uygulamada doğrudan; yatay ölçekte ise ya yapışkan oturumla (sticky session) ya da paylaşımlı bir depoyla (Spring Session + Redis) çalışır. Karşılaştırma nettir: JWT'nin kazancı **durumsuzluk** (doğrulama için depoya gitmeye gerek yok), bedeli **iptal edilememe** ve **boyut** (her istekte taşınan onlarca kilobayta varabilen yük; çerezler de her isteğe eklendiği için boyut maliyeti taşır). Sunucu oturumunun kazancı **anında iptal** ve küçük istemci yükü, bedeli **paylaşımlı durum** yönetimidir.

**Çıkış ve sekmeler.** Çerezli kurulumda çıkış gerçek bir sunucu işlemidir: oturum kaydı silinir, çerez `Max-Age=0` ile düşürülür — aynı tarayıcının tüm sekmeleri anında etkilenir. `localStorage`'daki token'da çıkış yalnız o tarayıcıdaki kopyayı siler; token başka bir yere kopyalandıysa çıkış onu etkilemez. Ayrıca `localStorage` sekmeler arasında paylaşıldığı için bir sekmedeki çıkışın diğerlerine duyurulması ayrıca kodlanır.

**Üçüncü yol: BFF deseni.** Tarayıcı, kendi kaynağındaki ince bir arka uca (BFF) **çerezle** konuşur; BFF token'ı sunucu tarafında tutar ve arka servislere `Bearer` ile gider. Böylece token tarayıcıya hiç inmez: XSS ile çalınacak bir şey kalmaz, CSRF savunması ise klasik yöntemlerle yapılır.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Karar ağacı: tarayıcı SPA, BFF, mobil ve servis-servis istemciler için önerilen oturum taşıma yöntemi">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İstemci kim? Kimlik oraya göre taşınır</text>
  <rect x="230" y="38" width="180" height="40" rx="9" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.5"/>
  <text x="320" y="63" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">İstemci türü nedir?</text>
  <path d="M320 78 L320 96" stroke="#5D7789" stroke-width="1.3" fill="none"/>
  <path d="M96 96 L560 96" stroke="#5D7789" stroke-width="1.3" fill="none"/>
  <path d="M96 96 L96 112" stroke="#5D7789" stroke-width="1.3" fill="none"/>
  <path d="M250 96 L250 112" stroke="#5D7789" stroke-width="1.3" fill="none"/>
  <path d="M404 96 L404 112" stroke="#5D7789" stroke-width="1.3" fill="none"/>
  <path d="M560 96 L560 112" stroke="#5D7789" stroke-width="1.3" fill="none"/>
  <rect x="16" y="112" width="160" height="112" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="96" y="134" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Tarayıcı SPA</text>
  <text x="96" y="156" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">HttpOnly + Secure</text>
  <text x="96" y="173" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">+ SameSite çerez</text>
  <text x="96" y="196" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">CSRF korumasını</text>
  <text x="96" y="211" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">AÇIK tut</text>
  <rect x="186" y="112" width="152" height="112" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="262" y="134" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">SPA + BFF</text>
  <text x="262" y="156" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">tarayıcı → çerez</text>
  <text x="262" y="173" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">BFF → Bearer</text>
  <text x="262" y="196" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">token tarayıcıya</text>
  <text x="262" y="211" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">hiç inmez</text>
  <rect x="348" y="112" width="140" height="112" rx="9" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.5"/>
  <text x="418" y="134" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B67A1A" font-family="system-ui">Mobil uygulama</text>
  <text x="418" y="156" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Authorization:</text>
  <text x="418" y="173" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Bearer</text>
  <text x="418" y="196" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">işletim sistemi</text>
  <text x="418" y="211" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">güvenli deposu</text>
  <rect x="498" y="112" width="126" height="112" rx="9" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.5"/>
  <text x="561" y="134" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Servis-servis</text>
  <text x="561" y="156" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Authorization:</text>
  <text x="561" y="173" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Bearer</text>
  <text x="561" y="196" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tarayıcı yok →</text>
  <text x="561" y="211" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">CSRF yok</text>
</svg>
```

Özetle 2026'nın yaygın kanaati: **tarayıcı istemcisi için `HttpOnly` + `Secure` + `SameSite` çerez varsayılandır**; `localStorage`'da uzun ömürlü JWT tutmak tavsiye edilmez. `Bearer` başlığı ise mobil ve servis-servis çağrılar için doğal seçimdir — orada tarayıcı olmadığı için CSRF zemini de yoktur.

### Mülakatta ne sorulur?

- **"Token'ı `localStorage`'da mı tutarsın, çerezde mi?"** → Beklenen cevap tek kelime değil; **ödünleşmeyi** kurmandır. Çerez otomatik gider: CSRF riski doğar, ama `HttpOnly` sayesinde XSS'e kapalıdır. `localStorage` otomatik gitmez: CSRF yoktur, ama XSS olursa token çalınır. Tarayıcı istemcisi için varsayılan `HttpOnly` + `Secure` + `SameSite` çerez, CSRF koruması açık. "Herkes JWT kullanıyor" gerekçesi zayıf cevaptır.
- **"`HttpOnly`, `Secure`, `SameSite` ne yapar?"** → Sırasıyla: betiğe kapatır, yalnız HTTPS'te gönderir, siteler arası gönderimi kısıtlar. Bunları karıştırmak sık görülen hatadır; `HttpOnly` şifreleme yapmaz.
- **"CSRF'i tam olarak anlat."** → Saldırganın çerezi **okumasına gerek yoktur**; tarayıcının otomatik göndermesini sömürür. Savunma: `SameSite` (birincil), senkronizatör token, `Origin`/`Referer` denetimi. "SameSite koydum, bitti" cevabı eksiktir.
- **"Neden saf Bearer kurulumunda CSRF yok?"** → `Authorization` başlığını tarayıcı kendiliğinden eklemediği için saldırının dayandığı otomatik gönderim yoktur. Dikkat: token bir çerezde taşınıyorsa bu bağışıklık **kaybolur**.
- **"XSS varsa fark eder mi?"** → Eder. `HttpOnly` çerezde saldırgan token'ı dışarı çıkaramaz, yalnız kurbanın tarayıcısından ve o oturum süresince istek atabilir; çalınan token her yerden, kurban çevrimdışıyken de kullanılır. Asıl savunma yine çıktı kaçışlama ve CSP'dir.
- **"Kullanıcı çıkış yaptı, JWT ne oldu?"** → Hâlâ geçerli. Durumsuz doğrulamada tek token geri alınamaz; çare kısa ömür, kara liste (Redis) veya sunucu-taraflı oturum. Bu soru kıdem ayıracıdır.
- **"Yenileme token'ını nerede tutarsın?"** → `HttpOnly` + `Secure` + `SameSite` çerezde, rotasyonlu ve yeniden kullanım tespitiyle. En uzun ömürlü sır en korunaklı yerde durur; URL'de asla taşınmaz.
- **"Session mı JWT mi?"** → "JWT modern, session eski" değil. JWT: durumsuzluk kazancı, iptal edilememe ve boyut bedeli. Session: anında iptal kazancı, paylaşımlı durum (Spring Session + Redis) bedeli. Doğru cevap bağlamı sormaktır.
- **"BFF nedir, burada ne işe yarar?"** → Tarayıcı BFF'e çerezle konuşur, BFF token'ı sunucuda tutup arka servislere `Bearer` ile gider; token tarayıcıya hiç inmediği için çalınacak bir şey kalmaz.

### Tek cümlede

Çerezi tarayıcı otomatik ekler; bu kolaylık getirir ama CSRF riskini de getirir. `Authorization` başlığını ise kod elle ekler; CSRF yoktur ama token bir yerde saklanmak zorundadır. Tarayıcı istemcisinde doğru varsayılan `HttpOnly` + `Secure` + `SameSite` çerezdir. `Bearer` ise mobil ve servisten servise çağrıların yeridir.

# otrm-1 | Temel
## Soru
Tarayıcıdaki bir uygulama, sunucuya "ben kimim" bilgisini çerezle ya da `Authorization: Bearer` başlığıyla taşıyabilir. İkisi arasındaki en temel işleyiş farkı nedir?
## A
Çerezi tarayıcı, eşleşen her isteğe kendiliğinden ekler; `Authorization` başlığını ise uygulama kodunun her istekte elle koyması gerekir.
🤖 Doğru: otomatik ekleme ile elle ekleme ayrımı, tüm güvenlik takasının kaynağıdır.
## B
Çerez yalnız HTTPS üzerinden, `Authorization` başlığı ise yalnız şifresiz HTTP üzerinden taşınabilir; aradaki fark taşıma katmanının şifreli olup olmamasıdır.
🤖 İkisi de her iki protokolde taşınabilir; HTTPS zorunluluğu `Secure` özniteliğiyle ayrıca istenir.
## C
Çerez sunucunun belleğinde, `Authorization` başlığı ise tarayıcıda saklanır; bu yüzden çerez sunucu yeniden başlayınca kaybolur, başlık kalıcıdır.
🤖 Çerez tarayıcıda saklanır; sunucu onu yalnız `Set-Cookie` ile yazdırır.
## D
Çerezin içeriği tarayıcı tarafından otomatik olarak şifrelenir, `Authorization` başlığı ise düz metin gider; aradaki fark yalnızca gizlilikte ortaya çıkar.
🤖 Tarayıcı çerezi şifrelemez; taşıma gizliliğini sağlayan şey TLS'tir.
## Doğru
A
## Açıklama
HTTP durumsuzdur, bu yüzden kimlik her istekte yeniden taşınır. **Çerezi tarayıcı taşır**: `Set-Cookie` ile yazıldıktan sonra, `Domain`/`Path` ile eşleşen her isteğe — sayfa gezinmesi, resim yüklemesi, hatta başka bir sitedeki formun gönderimi dahil — kendiliğinden eklenir. **`Authorization` başlığını ise kod taşır**: tarayıcı onu asla kendiliğinden eklemez, her istekte JavaScript koyar. Bu tek fark iki zafiyeti doğurur: otomatik gönderim CSRF'in zeminidir, elle gönderim ise token'ın betiğin erişebileceği bir yerde saklanmasını gerektirir. B yanlıştır, ikisi de her iki protokolde gider (HTTPS zorunluluğu `Secure` ile istenir); C saklama yerlerini ters çevirir; D ise tarayıcının çerezi şifrelediğini varsayar — şifrelemez.

# otrm-2 | Temel
## Soru
Bir oturum çerezine `HttpOnly` özniteliği eklemek pratikte neyi değiştirir?
## A
Çerez yalnızca HTTPS bağlantılarında gönderilir; şifresiz HTTP isteklerinde tarayıcı onu isteğe hiç eklemez ve ağı dinleyen biri göremez.
🤖 Bu `Secure` özniteliğinin işidir; `HttpOnly` protokolle ilgili değildir.
## B
Çerez siteler arası isteklerde gönderilmez; başka bir sitenin tetiklediği isteklerde tarayıcı onu dışarıda bırakarak sahteciliği engeller.
🤖 Bu `SameSite` özniteliğinin işidir; `HttpOnly` gönderim kuralını değiştirmez.
## C
Sayfadaki JavaScript çereze erişemez; `document.cookie` ile okunamaz, dolayısıyla bir XSS açığında içeriği okunup dışarı taşınamaz.
🤖 Doğru: `HttpOnly` çerezi betik erişimine kapatır.
## D
Çerezin içeriği sunucunun anahtarıyla şifrelenir; saldırgan çerezi ele geçirse bile içindeki oturum kimliğini çözemez ve kullanamaz.
🤖 `HttpOnly` hiçbir şifreleme yapmaz; yalnızca betik erişimini engeller.
## Doğru
C
## Açıklama
`HttpOnly` tek bir şey yapar: çerezi **sayfa betiğine kapatır**. `document.cookie` onu görmez, dolayısıyla sayfada bir XSS açığı olsa bile saldırganın betiği çerezin içeriğini okuyup kendi sunucusuna gönderemez. Bu, `localStorage`'a göre en büyük üstünlüktür. Karıştırılan diğer öznitelikler ayrı işler yapar: `Secure` çerezi yalnız HTTPS'e kısıtlar (A), `SameSite` siteler arası gönderimi kısıtlar (B). `HttpOnly` şifreleme değildir (D) — çerez düz metin olarak durur, onu koruyan şey erişim kısıtıdır. Dikkat: `HttpOnly` CSRF'e karşı hiçbir şey yapmaz; çerez betiğe kapalı olsa da tarayıcı onu otomatik göndermeye devam eder.

# otrm-3 | Orta
## Soru
"Token'ı `localStorage`'da tutmakla `HttpOnly` çerezde tutmak fark etmez; XSS varsa zaten her şey biter" itirazına verilecek en doğru cevap nedir?
## A
Doğrudur, fark yoktur; XSS varken iki kurulum da aynı sonucu verir, bu yüzden token'ın nerede saklandığı güvenlik açısından tamamen önemsiz bir ayrıntıdır.
🤖 Fark vardır: token'ın çalınabilmesiyle yalnız istek attırılabilmesi aynı şey değildir.
## B
Fark vardır: `localStorage`'daki token okunup dışarı taşınabilir; `HttpOnly` çerezde saldırgan token'ı çıkaramaz, yalnız kurbanın tarayıcısından istek atabilir.
🤖 Doğru: çalınan token her yerden kullanılır, `HttpOnly`'de etki kurbanın tarayıcısıyla sınırlı kalır.
## C
Fark vardır ama tersinedir: `HttpOnly` çerez betiğe açık olduğu için XSS'te önce o çalınır; `localStorage` ise tarayıcı tarafından şifrelenerek saklandığından korunaklıdır.
🤖 `HttpOnly` çerez betiğe kapalıdır ve `localStorage` şifrelenmez; ifade ters.
## D
Fark yalnız başarımdadır: çerez her isteğe eklendiği için bant genişliği harcar, `localStorage` harcamaz; güvenlik açısından iki seçenek tam olarak denktir.
🤖 Boyut gerçek bir ayrımdır ama güvenlik sonuçları denk değildir.
## Doğru
B
## Açıklama
İtirazın doğru yanı var — XSS ciddi bir açıktır ve tek başına savunma olarak depolama seçimine güvenilmez — ama sonucu yanlıştır. `localStorage`'ı JavaScript okuyabildiği için saldırganın betiği token'ı **kendi sunucusuna taşır**; o token bundan sonra saldırganın makinesinden, kurban çevrimdışıyken bile, süresi dolana dek kullanılır. `HttpOnly` çerezde ise token dışarı **çıkarılamaz**; saldırganın yapabileceği tek şey kurban o sayfadayken onun tarayıcısından istek attırmaktır — etki alanı ve süresi dardır, sunucuda olağandışı davranış olarak yakalanabilir. C ifadeyi tersine çevirir (`localStorage` şifrelenmez), D ise gerçek olan boyut farkını güvenlik denkliğiyle karıştırır. Not: asıl savunma çıktı kaçışlama ve CSP'dir; depolama seçimi hasarı sınırlayan ikincil katmandır.

# otrm-4 | Orta
## Soru
Oturum çerezine `SameSite=Lax` koydun. CSRF açısından bu ne sağlar, neyi sağlamaz?
## A
CSRF'i tümüyle bitirir; `SameSite` konduktan sonra senkronizatör token deseni ya da `Origin`/`Referer` denetimi tamamen gereksiz hale gelir.
🤖 `SameSite` güçlü bir savunmadır ama tek başına yeterli sayılmaz.
## B
Hiçbir şey sağlamaz; `SameSite` yalnızca çerezin ne kadar yaşayacağını belirler, isteğin nereden tetiklendiğiyle hiçbir ilgisi yoktur.
🤖 Ömrü `Max-Age`/`Expires` belirler; `SameSite` gönderim kapsamını belirler.
## C
Çerezi JavaScript'e kapatarak CSRF'i engeller; saldırganın sayfası çerezi okuyamadığı için geçerli bir sahte istek üretemez hale gelir.
🤖 Betiğe kapatan `HttpOnly`'dir; ayrıca CSRF'te saldırgan çerezi okumaya çalışmaz.
## D
Başka sitelerin tetiklediği isteklerde çerezin gönderilmesini büyük ölçüde engeller; yine de senkronizatör token ve `Origin` denetimi önerilir.
🤖 Doğru: birincil savunmadır ama tek katman olarak bırakılmaz.
## Doğru
D
## Açıklama
`SameSite`, çerezin **siteler arası** isteklerde gönderilip gönderilmeyeceğini belirler. `Lax` modern tarayıcılarda pratik varsayılandır ve çerezi başka bir sitenin tetiklediği POST isteklerinde göndermez; `Strict` daha katıdır (dış bağlantıdan gelen kullanıcı çıkış yapmış görünür), `None` ise gerçekten siteler arası kullanım için ayrılmıştır ve `Secure` ile birlikte zorunludur. Bu CSRF'in zeminini büyük ölçüde ortadan kaldırır ama **tek katman olarak bırakılmaz**: eski tarayıcılar, alt alan adları ve yanlış yapılandırma boşluk bırakır; bu yüzden senkronizatör token deseni (`XSRF-TOKEN` çerezi + `X-XSRF-TOKEN` başlığı) ve `Origin`/`Referer` denetimi eklenir. A bu derinlemesine savunmayı gereksiz sayar; B `SameSite`i ömür özniteliğiyle karıştırır; C ise `HttpOnly`nin işini `SameSite`e atfeder — üstelik CSRF'te saldırgan çerezi okumaz, okumadan kullandırır.

# otrm-5 | İleri
## Soru
Kullanıcı "çıkış yap" düğmesine bastı. Sunucuda durum tutmayan, imzalı JWT ile çalışan bir kurulumda gerçekte ne olur?
## A
Sunucu o token'ın imzasını geçersiz kılar; aynı token bundan sonra hiçbir uç noktada kabul edilmez, çıkış anında ve kesin biçimde uygulanır.
🤖 Tek bir token'ın imzası geçersiz kılınamaz; imza anahtarı tüm token'lar için ortaktır.
## B
Tarayıcı çıkışta bütün depolarını temizlediği için token her yerden silinir; başka bir yere kopyalanmış olsa bile kullanılamaz hale gelir.
🤖 Tarayıcı yalnız kendi kopyasını siler; kopyalanmış token çalışmaya devam eder.
## C
Token istemciden silinir ama sunucuda hâlâ geçerlidir; süresi dolana dek çalışır — çare kısa ömür, kara liste ya da sunucu-taraflı oturumdur.
🤖 Doğru: durumsuz doğrulamanın bedeli geri alınamamadır.
## D
JWT'ler doğaları gereği tek kullanımlıktır; her istekte tüketilip düşürüldükleri için çıkıştan sonra elde zaten geçerli bir token kalmaz.
🤖 JWT tek kullanımlık değildir; süresi dolana dek defalarca kullanılabilir.
## Doğru
C
## Açıklama
Durumsuz JWT doğrulamasında sunucu hiçbir oturum kaydı tutmaz; bir token'ın geçerliliği yalnızca **imzasından ve `exp` alanından** okunur. Bu yüzden "çıkış" gerçekte istemcideki kopyayı silmekten ibarettir — token başka bir yere kopyalanmışsa **süresi dolana dek çalışmaya devam eder**. Bu, JWT'nin durumsuzluk kazancının doğrudan bedelidir: **geri alınamama (revocation) problemi**. Çareler: ömrü dakikalarla sınırlamak, iptal edilen token'ları bir kara listede (genelde Redis) tutmak, ya da baştan sunucu-taraflı oturum kullanmak — ama kara liste eklendiği anda durumsuzluğun bir kısmından zaten vazgeçilmiş olur. A yanlıştır, imza anahtarı tüm token'lar için ortaktır ve tek token için geçersizleştirilemez; B tarayıcının silmesini evrensel bir iptal sanır; D ise JWT'yi tek kullanımlık zanneder.

# otrm-6 | İleri
## Soru
Kısa ömürlü erişim token'ı ve yenileme token'ı (refresh token) kullanan bir tarayıcı uygulamasında, yenileme token'ı için önerilen kurulum nedir?
## A
`HttpOnly`, `Secure` ve `SameSite` bir çerezde tutulur; her kullanımda yenisiyle değiştirilir ve eski kopya tekrar gelirse zincir iptal edilir.
🤖 Doğru: betiğe kapalı saklama + rotasyon + yeniden kullanım tespiti birlikte uygulanır.
## B
`localStorage`'da tutulur; erişim token'ından uzun ömürlü olduğu için betikten kolayca okunabilmesi yenilemeyi hızlandırır ve güvenliği etkilemez.
🤖 En uzun ömürlü sır en korunaklı yerde durmalı; `localStorage` betiğe açıktır.
## C
Yenileme token'ına hiç gerek yoktur; erişim token'ının ömrünü günlerce uzatmak aynı kullanım kolaylığını daha az parçayla sağlar.
🤖 Uzun ömürlü erişim token'ı geri alınamama riskini büyütür.
## D
URL sorgu parametresinde taşınır; böylece ne çerez ne başlık kurulumuna dokunmadan her sayfadan ve her sekmeden erişilebilir olur.
🤖 URL'ler günlüklere, tarayıcı geçmişine ve `Referer` başlığına sızar; sır oraya konmaz.
## Doğru
A
## Açıklama
Yenileme token'ı sistemdeki **en uzun ömürlü sırdır**, bu yüzden en korunaklı yerde durmalıdır: `HttpOnly` + `Secure` + `SameSite` bir çerez, onu sayfa betiğine tamamen kapatır. Üstüne iki uygulama gelir: **rotasyon** — her yenileme çağrısında yeni bir yenileme token'ı verilir, eskisi düşürülür; ve **yeniden kullanım tespiti** — düşürülmüş bir kopya tekrar gelirse token'ın çalındığı varsayılıp o zincirdeki tüm token'lar iptal edilir. B en uzun ömürlü sırrı betiğe açık bir depoya koyar; C uzun ömürlü erişim token'ıyla geri alınamama penceresini günlere çıkarır — kısa ömür + yenileme deseninin varlık sebebi tam olarak budur; D ise sırrı URL'e koyar, oysa URL'ler sunucu günlüklerine, tarayıcı geçmişine ve `Referer` başlığına sızar. Alternatif bir yaklaşım BFF desenidir: token tarayıcıya hiç inmez, sunucu tarafında tutulur.
