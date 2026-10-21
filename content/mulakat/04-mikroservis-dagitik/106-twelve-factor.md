---
slug: twelve-factor
baslik: "On İki Faktör: Config-in-Env, Durumsuzluk ve Log-as-Stream"
kademe: Orta
sira: 106
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Elindeki şarj aletini düşün: fişi evde, okulda, kafede aynı prize giriyor. Alet nerede olduğunu bilmez; elektrik dışarıdan gelir, o yalnız işini yapar. On İki Faktör (Twelve-Factor App) yazılım için tam olarak bunu ister: uygulama, çalıştığı yeri (kendi bilgisayarın, test sunucusu, gerçek müşteri sunucusu) bilmesin; yerden yere değişen ne varsa dışarıdan verilsin. Böylece aynı program hiç değiştirilmeden her yerde çalışır; kopyasını çoğaltmak, taşımak ve bozulanı atıp yenisini açmak sıradan bir iş olur.

### Temel kavramlar

On İki Faktör'ü, Heroku adlı bulut şirketinin mühendisleri ortaya koydu (Adam Wiggins, 2011). Binlerce uygulamayı işletirken gördükleri ortak hataları on iki ilkeye döktüler.

Bu ilkeler hiçbir dile, çerçeveye veya veritabanına bağlı değildir. Kaç satır kod yazacağın ya da veritabanı şemanı nasıl kuracağın hakkında tek kelime etmezler. Yalnız uygulamanın nasıl paketleneceğini ve nasıl işletileceğini anlatırlar. Bugün hâlâ geçerlidirler, çünkü konteyner (container) ve Kubernetes dünyası aynı varsayımlar üzerine kuruludur. Dikkat: "12-factor" ile "mikroservis" aynı şey değildir; tek parça (monolit) bir uygulama da bu ilkelere uyabilir.

Terimler:

- **Süreç (process)**: işletim sisteminde çalışan program kopyası. Aynı uygulamayı beş kez başlatırsan beş süreç olur.
- **Durum (state)**: sürecin belleğinde tuttuğu ve sonraki isteklerde gereken bilgi — kimin giriş yaptığı, sepetin içi gibi.
- **Durumsuz (stateless)**: böyle bilgi biriktirmeyen süreç. Her istek kendi başına anlaşılır; hangi kopyaya düştüğü fark etmez.
- **Yapılandırma (config)**: ortamdan ortama değişen ayarlar — veritabanı adresi, parola, dış servis adresi.
- **Ortam değişkeni (environment variable)**: işletim sisteminin programa çalışma anında verdiği ad=değer çifti. Kodun içinde değil, dışında durur.
- **Destek servisi (backing service)**: uygulamanın ağ üzerinden kullandığı dış kaynak — veritabanı, Redis, kuyruk.
- **Yatay ölçekleme**: tek makineyi büyütmek değil, aynı sürecin kopya sayısını artırmak.
- **Olay akışı (event stream)**: başı sonu belli bir dosya değil, zaman sırasıyla akıp giden satırlar dizisi.

On iki madde sırasıyla: I kod deposu, II bağımlılıklar, III yapılandırma, IV destek servisleri, V derle/yayımla/çalıştır, VI süreçler, VII port bağlama, VIII eşzamanlılık, IX atılabilirlik, X dev/prod eşliği, XI loglar, XII yönetim görevleri.

### Nasıl çalışır?

Bir uygulama **tek bir kod deposunda** yaşar (I); aynı depodan birçok dağıtım çıkar. Kullandığı kütüphaneler **açıkça bir dosyada bildirilir** (II); "makinede zaten kuruludur" varsayımı yasaktır.

**Yapılandırma ortam değişkeninden okunur** (III): koda gömülmez, git'e girmez. Ölçüt nettir — depo bu an herkese açılsa hiçbir parola sızmamalı. Şu üç yol anti-kalıptır: dev için bir ayar dosyası prod için başka bir dosya tutmak, ortam başına ayrı dal (branch) açmak, ortam başına ayrı imaj derlemek. Ortam sayısı arttıkça bunlar yönetilemez hale gelir. Üstelik aynı paketin her yerde çalıştığından emin olamazsın. Elle düzenlenen, sürüm kontrolü dışındaki paylaşımlı ayar tabloları da güvenilmezdir: kimin ne zaman ne değiştirdiği bilinmez ve değişiklik geri alınamaz.

**Destek servisleri takılıp çıkarılabilir kaynaklardır** (IV): yerel veritabanından bulut veritabanına geçmek bir kod değişikliği değil, adres (URL) değişikliğidir.

**Derleme, yayım ve çalıştırma katı biçimde ayrılır** (V). Derleme (build) kodu çalıştırılabilir bir pakete çevirir. Yayım (release) o paketi o ortamın config'iyle birleştirir ve **değişmez**, benzersiz kimliği olan bir sürüm üretir. Çalıştırma (run) ise o sürümü başlatır. Yayım bir kez oluştu mu değiştirilemez: değişiklik yeni bir yayım demektir. **Geri alma (rollback)** da eski yayımı yeniden başlatmaktan ibarettir.

```svg
<svg viewBox="0 0 660 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Aynı imaj her ortamda; durum ve config dışarıda">
  <defs>
    <marker id="tf-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek imaj + dışsal config/durum → her ortam</text>
  <rect x="255" y="42" width="150" height="46" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="330" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">aynı imaj (build)</text>
  <text x="330" y="79" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">stateless süreç</text>
  <rect x="40" y="130" width="150" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="115" y="150" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">dev</text>
  <text x="115" y="166" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">env: dev config</text>
  <rect x="255" y="130" width="150" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="330" y="150" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">test</text>
  <text x="330" y="166" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">env: test config</text>
  <rect x="470" y="130" width="150" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="545" y="150" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">prod</text>
  <text x="545" y="166" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">env: prod config</text>
  <path d="M300 88 L140 128" fill="none" stroke="#186399" stroke-width="1.3" marker-end="url(#tf-arw)"/>
  <path d="M330 88 L330 128" fill="none" stroke="#186399" stroke-width="1.3" marker-end="url(#tf-arw)"/>
  <path d="M360 88 L520 128" fill="none" stroke="#186399" stroke-width="1.3" marker-end="url(#tf-arw)"/>
</svg>
```

**Süreçler durumsuz ve paylaşımsızdır** (VI): oturum, yüklenen dosya, sayaç gibi paylaşılan durum sürecin belleğinde değil, harici destek servisinde (Redis, veritabanı, nesne deposu) durur. Oturumu bellekte tutup kullanıcıyı hep aynı kopyaya yapıştırmak (yapışkan oturum, sticky session) anti-kalıptır: o kopya ölünce oturum kaybolur, yük dengesiz dağılır. Uygulama kendi web sunucusunu içerir ve bir **porta bağlanır** (VII); yük artınca **kopya sayısı artırılır** (VIII) — yatay ölçekleme ancak süreçler durumsuzsa mümkündür.

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Durumsuz süreçler: durum harici destek servisinde">
  <defs>
    <marker id="tf-st-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Durumsuzluk (VI): durum dışarıda, her istek herhangi kopyaya</text>
  <rect x="30" y="54" width="140" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="100" y="78" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">replika 1 (stateless)</text>
  <rect x="30" y="104" width="140" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="100" y="128" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">replika 2 (stateless)</text>
  <rect x="30" y="154" width="140" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="100" y="178" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">replika 3 (stateless)</text>
  <rect x="410" y="90" width="220" height="70" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="520" y="118" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">harici destek servisi</text>
  <text x="520" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Redis / DB / nesne deposu</text>
  <path d="M170 74 L408 108" fill="none" stroke="#186399" stroke-width="1.3" marker-end="url(#tf-st-arw)"/>
  <path d="M170 124 L408 124" fill="none" stroke="#186399" stroke-width="1.3" marker-end="url(#tf-st-arw)"/>
  <path d="M170 174 L408 142" fill="none" stroke="#186399" stroke-width="1.3" marker-end="url(#tf-st-arw)"/>
  <text x="288" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">oturum/durum</text>
  <text x="330" y="200" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">bellekte oturum + sticky session bu ilkeyi bozar</text>
</svg>
```

**Atılabilirlik** (IX) iki şey ister: hızlı açılış ve zarif kapanış. Kapatma sinyali (SIGTERM) geldiğinde uygulama yeni iş almayı bırakır. Elindeki isteği bitirir, bağlantıları kapatır ve çıkar. Beklenmedik ani ölümde de veri tutarlı kalmalıdır. Amaç "asla kapanmamak" değil, her an güvenle kapanabilmektir. Kubernetes bir kopyayı ölçekleme veya bakım yüzünden her an öldürüp başka düğüme taşıyabilir. Bu yüzden durumu geçici yerel diske yazıp yeniden başlarken oradan okumaya güvenmek yanlıştır.

**Dev/prod eşliği** (X) geliştirme, hazırlık ve üretim ortamları arasındaki boşluğu küçültmeyi ister. Üç şey beklenir: aynı tür destek servisleri (dev'de de gerçek Postgres, bellek-içi taklit değil), birbirine yakın sürümler ve kısa dağıtım aralıkları.

**Loglar olay akışıdır** (XI): uygulama log dosyası açmaz, dosya döndürme ve arşivleme yapmaz. Akışı `stdout`'a yazar, toplamayı çalışma ortamı üstlenir. Log üretmeyip yalnız hata anında e-posta göndermek de çözüm değildir: e-posta bir bildirim kanalıdır, gözlemlenebilirliğin ham verisi değil.

**Yönetim görevleri** (XII) — veri göçü (migration), tek seferlik betikler — aynı ortamda ve aynı yayımla çalışan tek seferlik süreçler olarak yürütülür.

```svg
<svg viewBox="0 0 660 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Loglar olay akışı: uygulama stdout'a yazar, ortam toplar">
  <defs>
    <marker id="tf-log-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Loglar olay akışıdır (XI): sorumluluk ayrımı</text>
  <rect x="24" y="66" width="170" height="60" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="109" y="90" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">uygulama</text>
  <text x="109" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">akışı stdout'a yazar</text>
  <rect x="246" y="66" width="170" height="60" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="331" y="90" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">çalışma ortamı</text>
  <text x="331" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Fluent Bit/Promtail toplar</text>
  <rect x="468" y="66" width="170" height="60" rx="11" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="553" y="90" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">merkezî sistem</text>
  <text x="553" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ELK / Loki</text>
  <path d="M194 96 L244 96" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#tf-log-arw)"/>
  <path d="M416 96 L466 96" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#tf-log-arw)"/>
  <text x="330" y="156" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">uygulama dosya/rotasyon yönetmez — pod diski geçicidir</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Config neden ortam değişkeninde?"** — aynı paket hiç değişmeden dev/test/prod'da çalışsın diye. Ortam başına ayrı dosya grubu, ayrı dal veya ayrı imaj derlemek klasik tuzaktır; ortamlar arası eşliği de bozar. Not: parolanın asıl kaynağı bir sır kasasıdır, ortam değişkeni yalnız taşıma yoludur.
- **"Durumsuzluk neden şart, yapışkan oturum neden çözüm değil?"** — yapışkan oturum bir yara bandıdır. Kopya ölünce oturum gider, yük de dengelenmez. Asıl çözüm oturumu dışsal depoya taşımaktır.
- **"Uygulaman SIGTERM alınca ne yapıyor?"** — beklenen cevap: yeni istek almayı bırakıp elindeki işi bitiriyor, sonra çıkıyor.
- **"Log'u neden uygulama yönetmiyor?"** — konteyner diski geçicidir. Toplama, döndürme ve saklama altyapının işidir. Log'u istek içinde eşzamanlı olarak veritabanına yazmak ise hem gecikmeyi hem arızanın etkisini büyütür.
- **"Dev/prod eşliği nedir?"** — ortamlar arasındaki araç, servis türü ve dağıtım zamanı farkını küçültmek.
- **Yayımın değişmezliği**: canlıdaki sürümü elle düzeltmek yok; her düzeltme yeni bir yayımdır.

### Tek cümlede

On İki Faktör, aynı paketi hiç değiştirmeden her ortamda çalıştırabilmek için yapılandırmayı, durumu ve log'u uygulamanın dışına çıkarma disiplinidir.

# tf-1 | Orta
## Soru
On İki Faktör (Twelve-Factor App) metodolojisinin temel amacı nedir?
## A
Bir uygulamanın kaç satır kod ve kaç dosya içermesi gerektiğini kurala bağlamak
🤖 satır ya da dosya sayısıyla ilgisi yoktur; işletim ve dağıtım ilkeleri bütünüdür.
## B
Yalnız veritabanı şema tasarımı için normalizasyon ve indeksleme kurallarını belirlemek
🤖 veri modeliyle değil, uygulamanın taşınabilirliği ve dağıtımıyla ilgilenir.
## C
Uygulamayı taşınabilir, yatay ölçeklenebilir ve tek adımda dağıtılabilir kılan işletim ilkeleri sunmak
🤖 Doğru: config, durumsuzluk, atılabilirlik gibi ilkelerle bulut-yerli dağıtımı hedefler.
## D
Uygulamanın yalnız Java diliyle ve tek bir çerçeve (framework) üzerinde yazılmasını zorunlu kılmak
🤖 dil ve çerçeve bağımsızdır; herhangi bir teknoloji yığınında uygulanabilen ilkelerdir.
## Doğru
C
## Açıklama
**On İki Faktör**, ölçeklenebilir ve taşınabilir bulut uygulamaları için bir **işletim ilkeleri** kümesidir (dil ve altyapı bağımsız). Kod satırı (A), veritabanı normalizasyonu (B) veya belirli bir dil (D) ile ilgili değildir. İlkeler; kod tabanının versiyonlanması, bağımlılıkların açıkça bildirilmesi, **config'in ortamda** tutulması (III), **durumsuz süreçler** (VI), port bağlama, eşzamanlılık, **atılabilirlik** (IX), **dev/prod eşliği** (X) ve **logların olay akışı** olması (XI) gibi maddelerle uygulamayı **taşınabilir, yatay ölçeklenebilir ve tek-tıkla dağıtılabilir** kılmayı amaçlar — mikroservis + Kubernetes pratiğinin çoğu bu ilkelerden türer.

# tf-2 | Orta
## Soru
On İki Faktör'ün III. ilkesi (config) uyarınca, ortamdan ortama değişen ayarlar (DB URL'si, şifreler) nerede tutulmalıdır?
## A
Kaynak koda sabit (constant) olarak gömüp her ortam için ayrı bir dal (branch) açarak
🤖 kodda sabitleme ve ortam-başı dal, tek paket ilkesini bozar ve yönetilemez hale gelir.
## B
Her ortam için ayrı bir imaj (build) derleyip ayarları o imajın içine gömerek
🤖 ortam-başı imaj, "aynı build her ortamda" ilkesini ve dev/prod eşliğini bozar.
## C
Sürüm kontrolüne girmeyen, elle düzenlenen paylaşımlı bir yapılandırma tablosuna yazarak
🤖 elle yönetilen ve sürümsüz bir tablo denetlenemez, geri alınamaz ve hataya açıktır.
## D
Ortam değişkenlerinde (environment variables), yani sürecin dışında tutularak
🤖 Doğru: config-in-env, tek imajı ortamdan bağımsız kılan temel ilkedir.
## Doğru
D
## Açıklama
III. faktör config'i **koddan kesin biçimde ayırır** ve **ortam değişkeninde** tutar. Kritik testi şudur: "Kod tabanı bu an açık kaynak yapılsaydı, hiçbir kimlik bilgisi sızar mıydı?" — hayır olmalı. Config'i koda gömmek (A), ortam-başı imaj derlemek (B) veya sürümsüz bir tabloya elle yazmak (C) "aynı build, tüm ortamlarda değişmeden çalışır" ilkesini bozar. Ortam değişkeni yaklaşımıyla (D) tek bir imaj dev/test/prod'da aynen çalışır; ortamlar arası tek fark **enjekte edilen config**'tir. (Sırların kaynağı yine `Secret`/Vault'tur; ortam değişkeni yalnız taşıma yoludur.)

# tf-3 | Orta
## Soru
On İki Faktör'ün VI. ilkesi süreçlerin **durumsuz** (stateless) olmasını ister. Aşağıdaki tasarımlardan hangisi bu ilkeyi **ihlal eder**?
## A
Kullanıcı oturumunu sürecin kendi belleğinde tutup istekleri hep aynı kopyaya yönlendirmek
🤖 Doğru (ihlal budur): bellekte oturum ve yapışkan yönlendirme, yatay ölçeklemeyi ve kopyanın ölüp yeniden doğmasını bozar.
## B
Oturum durumunu harici bir Redis örneğinde tutmak, böylece her istek herhangi bir kopyaya gidebilir
🤖 Bu tasarım ilkeye uyar: durum sürecin dışında, süreç durumsuz; ihlal değildir.
## C
Kullanıcıların yüklediği dosyaları yerel diske değil, harici bir nesne deposuna (object storage) yazmak
🤖 Bu tasarım ilkeye uyar: kalıcı veri destek servisinde tutuluyor; ihlal değildir.
## D
Paylaşılan sayacı veritabanında tutup her kopyanın değeri oradan okuyup güncellemesini sağlamak
🤖 Bu tasarım ilkeye uyar: paylaşılan durum harici destek servisinde; ihlal değildir.
## Doğru
A
## Açıklama
VI. faktör: süreçler **durumsuz** ve "paylaşılmayan hiçbir şey" (share-nothing) olmalıdır; kalıcı/paylaşılan durum **harici destek servisinde** (Redis, veritabanı, nesne deposu) tutulur. Böylece **herhangi bir istek herhangi bir kopyaya** gidebilir; yatay ölçekleme ve pod'un ölüp yeniden doğması sorunsuzdur. **İhlal (A)**: oturumu sürecin belleğinde tutmak, kullanıcıyı hep aynı pod'a bağlamayı (sticky session) zorunlu kılar — o pod ölünce oturum kaybolur, ölçekleme dengesiz olur, yeni pod eski durumu bilmez. B, C ve D durumu **dışarı** taşıdığından ilkeye uyar. Doğru cevap, ilkeyi bozan tasarımı (A) işaret eder.

# tf-4 | İleri
## Soru
On İki Faktör'ün IX. ilkesi "atılabilirlik" (disposability) neyi gerektirir ve Kubernetes'te neden kritiktir?
## A
Süreçlerin asla kapanmaması; kesintisizlik için sonsuza dek ayakta tutulup hiç yeniden başlatılmaması
🤖 ilke tam tersi — süreçler her an güvenle atılabilmeli, kapanıp yeniden doğabilmelidir.
## B
Süreçler hızlı açılmalı, kapatma sinyalinde zarifçe kapanmalı, ani ölümde de tutarlı kalmalıdır
🤖 Doğru: hızlı açılış, SIGTERM'de zarif kapanış ve ani ölüme dayanıklılık, geçici pod dünyasının gereğidir.
## C
Süreçlerin bellekteki durumunu düzenli aralıklarla yerel diske yazıp yeniden başlarken oradan geri okuması
🤖 geçici pod diskine güvenmek atılabilirliğe aykırıdır; durum harici destek servisinde olmalıdır.
## D
Her sürecin kendi log dosyasını açıp boyut, rotasyon ve arşivleme politikasını kendi içinde yönetmesi
🤖 bu XI. faktörle (log akışı) çelişir; atılabilirlik açılış ve kapanış davranışıyla ilgilidir.
## Doğru
B
## Açıklama
IX. faktör **atılabilirlik**: süreçler **hızlı açılabilmeli** (yeni replika saniyeler içinde trafiğe hazır olmalı — ölçekleme ve hızlı toparlanma için) ve **nazikçe kapanabilmelidir** (SIGTERM'de yeni iş almayı durdurup süregelen işi bitirmek). Ayrıca beklenmedik ani ölümde de sistem tutarlı kalmalıdır (crash-only tasarım; yarım kalan işi kuyruk/idempotency toparlar). Bu, Kubernetes'te **kritiktir** çünkü pod'lar geçicidir: ölçekleme, düğüm bakımı, yeniden planlama nedeniyle her an öldürülüp taşınabilir. "Asla kapanmama" (A) ilkeye aykırıdır; yerel diske durum yazmak (C) geçici pod'da güvenilmezdir; log dosyası rotasyonu (D) XI. faktörle çelişir.

# tf-5 | Orta
## Soru
On İki Faktör'ün X. ilkesi "dev/prod eşliği" (parity) neyi savunur ve konteynerlerin buna katkısı nedir?
## A
Geliştirme ortamının üretimden olabildiğince farklı tutulmasını; böylece geliştiriciler kısıtsız çalışır
🤖 ilke farkı değil, benzerliği savunur; fark "bende çalışıyordu" hatalarını doğurur.
## B
Geliştirme, hazırlık ve üretim ortamlarının olabildiğince benzer tutulmasını; konteynerler bunu kolaylaştırır
🤖 Doğru: ortamları yakınsatmak sürprizi azaltır; konteyner aynı çalışma-zamanını her yere taşır.
## C
Geliştirmede her zaman bellek-içi taklit (mock) veritabanı, üretimde ise gerçek veritabanı kullanmayı zorunlu kılmayı
🤖 farklı türde destek servisleri kullanmak tam da eşliği bozar; ilke aynı tür servisleri ister.
## D
Geliştirme ile üretimin farklı işletim sistemi ve farklı kütüphane sürümleriyle çalışmasını teşvik etmeyi
🤖 işletim sistemi ve sürüm farkı eşliğe aykırıdır; ilke bu farkları küçültmeyi ister.
## Doğru
B
## Açıklama
X. faktör **dev/prod eşliği**: geliştirme, hazırlık (staging) ve üretim ortamları arasındaki **boşluğu küçük tutmayı** hedefler — aynı tür destek servisleri (dev'de gerçek Postgres, prod'da Postgres; bellek-içi taklit değil — C yanlış), yakın sürümler, benzer yapılandırma. Amaç, ortam farklarından doğan **"bende çalışıyordu ama prod'da patladı"** sınıfı sürprizleri önlemektir. **Konteynerler** bu ilkeyi güçlü kılan araçtır: uygulama + çalışma-zamanı + kütüphaneler tek imaja paketlenip her ortamda **aynen** çalıştığından ortamlar arası sapma en aza iner. Farkı savunan (A), farklı servis (C) veya farklı OS/sürüm (D) diyen şıklar ilkeyi ters çevirir.

# tf-6 | İleri
## Soru
On İki Faktör'ün XI. ilkesi "loglar olay akışıdır" uyarınca, konteynerde çalışan bir uygulama loglarını nasıl ele almalıdır?
## A
Uygulama kendi log dosyasını açmalı; boyut, rotasyon (rotation) ve arşivleme politikasını kendi içinde kurgulamalıdır
🤖 konteynerde disk geçicidir; dosya ve rotasyon yönetimi uygulamanın değil, çalışma ortamının işidir.
## B
Logları uygulama içinden doğrudan üretim veritabanındaki bir tabloya senkron biçimde yazmalıdır
🤖 senkron veritabanı yazımı gecikmeyi ve arıza etkisini artırır; log yazımı isteği yavaşlatmamalıdır.
## C
Logu `stdout`'a olay akışı olarak yazmalı; toplama ve saklamayı çalışma ortamına bırakmalıdır
🤖 Doğru: uygulama akışı üretir, ortam onu yakalar ve toplar; sorumluluklar temiz ayrılır.
## D
Log üretmemeli; yalnız bir hata oluştuğunda ilgili ekibe e-posta bildirimi göndermelidir
🤖 yapılandırılmış log gözlemlenebilirliğin temelidir; e-posta bir bildirim kanalıdır, log değil.
## Doğru
C
## Açıklama
XI. faktör: uygulama logu **zaman-sıralı bir olay akışı** olarak görmeli ve **`stdout`'a** yazmalıdır; **log dosyalarını, rotasyonu, arşivlemeyi kendisi yönetmemelidir** (A yanlış). Çalışma ortamı (Kubernetes + Fluent Bit/Promtail gibi bir ajan) bu akışı yakalar, toplar ve merkezî sisteme (ELK/Loki) yönlendirir. Bu ayrım önemlidir çünkü konteyner/pod diski **geçicidir** — uygulama içinde dosya yolu/rotasyon kurgulamak hem kaybolan log hem gereksiz bağlanım (coupling) demektir. Log'u senkron DB'ye yazmak (B) performans/dayanıklılık açısından hatalıdır; log üretmemek (D) gözlemlenebilirliği yok eder. Doğru desen: uygulama akışı **üretir**, ortam onu **toplar** — sorumluluklar temiz ayrılır.
