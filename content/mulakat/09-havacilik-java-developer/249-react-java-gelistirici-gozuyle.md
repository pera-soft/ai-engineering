---
slug: react-java-gelistirici-gozuyle
baslik: "React Java Geliştirici Gözüyle: Bileşen, props/state, Hook'lar ve Spring Boot ile Yaşam"
kademe: Orta
sira: 249
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

İlan "React ile çalışmış" diyor. Bu cümleyi mülakatta savunmanın yolu ezber slayt değildir; React'in **çekirdek zihin modelini** kurmaktır. Model şudur: bir bileşen, veriyi alıp görünüm döndüren bir **fonksiyondur**. Java'daki saf bir metodun `hesapla(girdi) → çıktı` sözleşmesi gibi düşün: `Bilesen(props) → görünüm`.

SPA kavramı ve ekran oluşturma (render) modelleri `208` numaralı konuda anlatıldı. React/Vue/Angular karşılaştırması ise `209`'da var. Bu konu, o zeminin üstüne **React'in kendisini** koyar: JSX, props/state, hook'lar, sanal DOM. Bir de bir Java geliştiricisinin en çok merak ettiği soruyu cevaplar: bu şey Spring Boot ile aynı evde nasıl yaşar?

### Temel kavramlar

- **bileşen (component):** UI'ın yeniden kullanılabilir parçası; veri alır, görünüm döndürür. Bileşenler iç içe geçerek bir **bileşen ağacı** oluşturur.
- **JSX:** HTML görünümlü ama **JavaScript'e derlenen** sözdizimi uzantısı; her JSX ifadesi bir fonksiyon çağrısına dönüşür. Tarayıcı JSX'i hiç görmez.
- **props:** üst bileşenden gelen **salt-okunur** veri; alt bileşen onu değiştiremez.
- **state:** bileşenin **kendi sahip olduğu** değişen veri; değişimi yeniden render tetikler.
- **tek yönlü veri akışı (one-way data flow):** veri ağaçta aşağı props'la iner; değişiklik talebi yukarı geri çağrılarla (callback) çıkar.
- **hook:** fonksiyon bileşenine yetenek ekleyen çağrı (`useState`, `useEffect`); **koşul veya döngü içinde çağrılmaz**, her render'da aynı sırayla koşmalıdır.
- **sanal DOM ve uzlaştırma (reconciliation):** React önce bellekteki hafif ağacı günceller, eskisiyle karşılaştırır ve gerçek DOM'a **yalnız farkı** uygular.

### Nasıl çalışır?

Döngü hep aynıdır. Kullanıcı bir olay tetikler. `setState` çağrılır. React bileşeni **yeniden çalıştırır**, yani fonksiyon baştan koşar. Yeni sanal ağaç eskisiyle karşılaştırılır. Gerçek DOM'a yalnızca en küçük yama uygulanır.

"Arayüz = fonksiyon(veri)" cümlesinin anlamı budur. Sen DOM'u elle değiştirmezsin; **veriyi değiştirirsin** ve görünüm veriden türetilir.

İki bileşenin aynı veriyi paylaşması gerekiyorsa, state **ikisinin ortak atasına taşınır**. Buna "state'i yukarı kaldırma" (lifting state up) denir. Ata bileşen state'i tutar, ikisine props olarak indirir; değişiklik ise geri çağrıyla yukarı çıkar.

```svg
<svg viewBox="0 0 660 292" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bilesen agacinda tek yonlu veri akisi: props asagi iner, olaylar yukari cikar">
  <defs>
    <marker id="reakt-akis-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
    <marker id="reakt-akis-yesil" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek yönlü veri akışı: props aşağı, olaylar yukarı</text>
  <rect x="230" y="40" width="200" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="330" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">UcusArama (ata)</text>
  <text x="330" y="80" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">state: seçili uçuş — yukarı kaldırılmış</text>
  <path d="M270 96 L160 150" fill="none" stroke="#186399" stroke-width="1.8" marker-end="url(#reakt-akis-arw)"/>
  <text x="168" y="118" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">props (salt-okunur)</text>
  <path d="M390 96 L500 150" fill="none" stroke="#186399" stroke-width="1.8" marker-end="url(#reakt-akis-arw)"/>
  <text x="496" y="118" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">props (salt-okunur)</text>
  <rect x="50" y="154" width="220" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="160" y="176" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">UcusListesi</text>
  <text x="160" y="194" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">satır seçilince olay yukarı</text>
  <rect x="390" y="154" width="220" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="500" y="176" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">SecimOzeti</text>
  <text x="500" y="194" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">aynı state'i props olarak okur</text>
  <path d="M200 154 L296 100" fill="none" stroke="#25804C" stroke-width="1.6" stroke-dasharray="5 3" marker-end="url(#reakt-akis-yesil)"/>
  <text x="212" y="136" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">geri çağrı (olay)</text>
  <rect x="80" y="234" width="500" height="44" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="330" y="252" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">İki bileşen aynı veriyi paylaşacaksa state ortak ataya taşınır</text>
  <text x="330" y="269" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">"state'i yukarı kaldırma" (lifting state up) — veri tek sahipte, görünüm türetilmiş</text>
</svg>
```

Yan etkiler render'ın içine yazılmaz; `useEffect` içine alınır. Yan etki derken API çağrısı, zamanlayıcı ve abonelik kastediliyor.

Effect'in **bağımlılık dizisi**, onun ne zaman yeniden koşacağını belirler. Boş dizi `[]` verirsen: ilk render'dan sonra bir kez koşar. `[userId]` verirsen: userId değiştikçe koşar. **Dizi hiç vermezsen: her render'da koşar.**

Klasik tuzak tam buradadır. Dizi verilmemiş bir effect içinde API'den gelen veriyi state'e yazdığını düşün. `setState` yeni bir render tetikler. Yeni render yeni effect'i çalıştırır. Yeni effect yeni bir `setState` yapar. Sonuç **sonsuz istek döngüsüdür**. Java tarafındaki karşılığı, kendi kendini tetikleyen bir olay dinleyicisidir.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="useEffect bagimlilik dizisi: bos dizi bir kez, degisken dizisi degisince, dizi yoksa sonsuz dongu riski">
  <defs>
    <marker id="reakt-dongu-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">useEffect bağımlılık dizisi: üç kurgu, bir tuzak</text>
  <rect x="20" y="42" width="195" height="64" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="117" y="64" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">useEffect(fn, [])</text>
  <text x="117" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">bir kez çalışır</text>
  <text x="117" y="97" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ilk yükleme verisi için doğru</text>
  <rect x="232" y="42" width="195" height="64" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="329" y="64" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">useEffect(fn, [userId])</text>
  <text x="329" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">userId değişince çalışır</text>
  <text x="329" y="97" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">amaçlanan tepkisel kullanım</text>
  <rect x="444" y="42" width="196" height="64" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="542" y="64" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">useEffect(fn)  — dizi yok</text>
  <text x="542" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">her render'da çalışır</text>
  <text x="542" y="97" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">içinde setState varsa: tuzak</text>
  <text x="330" y="136" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Sonsuz döngü çemberi</text>
  <rect x="90" y="150" width="130" height="36" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="155" y="172" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">render</text>
  <path d="M220 168 L280 168" fill="none" stroke="#B23B3B" stroke-width="1.7" marker-end="url(#reakt-dongu-arw)"/>
  <rect x="284" y="150" width="130" height="36" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="349" y="172" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">effect koşar</text>
  <path d="M414 168 L474 168" fill="none" stroke="#B23B3B" stroke-width="1.7" marker-end="url(#reakt-dongu-arw)"/>
  <rect x="478" y="150" width="130" height="36" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="543" y="172" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">setState</text>
  <path d="M543 186 L543 214 L155 214 L155 186" fill="none" stroke="#B23B3B" stroke-width="1.7" stroke-dasharray="5 3" marker-end="url(#reakt-dongu-arw)"/>
  <text x="349" y="208" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">state değişti → yeniden render → döngü kapanır</text>
  <text x="330" y="240" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">çıkış: doğru bağımlılık dizisi + hook'ları koşul içinde çağırmama kuralı</text>
</svg>
```

Liste çizerken kullanılan `key` prop'u, uzlaştırmanın **kimlik sözleşmesidir**. Java'daki `equals`/`hashCode` çiftine benzer: React, key'e bakarak hangi satırın aynı kaldığını, hangisinin değiştiğini anlar.

Key kararlı ve benzersiz olmalıdır; örneğin uçuş numarası iyi bir key'dir. **Dizi indeksi** ise kötü bir key'dir, çünkü araya ekleme veya silme yapıldığında kayar. Böyle olunca eski satırın state'i yanlış satıra yapışabilir. Yani yanlış key sadece performans sorunu değil, **davranış hatası** üretir.

### Spring Boot ile birlikte yaşama

Geliştirmede iki süreç ayrı çalışır: React geliştirme sunucusu (ör. 3000) ve Spring Boot (ör. 8090). Farklı kaynak (origin) olmanın sancısını çekmemek için geliştirme sunucusuna bir **vekil (proxy)** tanımlanır. `/api` istekleri 8090'a aktarılır ve tarayıcı tek kaynak görür.

Üretimde iki model vardır. Birincisi **ayrı dağıtımdır**: ön yüz statik barındırmada durur, API ayrı kaynaktadır. Bu durumda CORS kalıcı bir sunucu sorumluluğu olur (`210`). İkincisi **tek jar** modelidir: React derleme çıktısı Spring Boot'un statik kaynaklarına konur. Tek kaynak kalır ve CORS ihtiyacı ortadan kalkar.

Her iki modelde de React senin API sözleşmeni tüketir. Sözleşmede aranan şeyler bellidir: az turda yeterli veri, sayfalama ve `ProblemDetail` hata gövdesi. Sözleşmenin ön yüz gözüyle tasarımı `212`'de anlatıldı.

```svg
<svg viewBox="0 0 660 280" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Gelistirmede proxy, uretimde iki model: ayri dagitim arti CORS veya tek jar icinde statik sunum">
  <defs>
    <marker id="reakt-dagitim-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">React + Spring Boot: geliştirme ve iki üretim modeli</text>
  <rect x="20" y="42" width="620" height="64" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="110" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">GELİŞTİRME</text>
  <text x="230" y="66" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">dev server :3000</text>
  <path d="M300 62 L392 62" fill="none" stroke="#5D7789" stroke-width="1.7" marker-end="url(#reakt-dagitim-arw)"/>
  <text x="346" y="54" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">proxy /api</text>
  <text x="470" y="66" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Spring Boot :8090</text>
  <text x="330" y="92" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tarayıcı tek origin görür — CORS sancısı geliştirmede proxy ile gizlenir</text>
  <rect x="20" y="126" width="302" height="106" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="171" y="148" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">ÜRETİM MODEL 1: ayrı dağıtım</text>
  <text x="171" y="168" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ön yüz statik barındırma · API ayrı origin</text>
  <text x="171" y="186" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">CORS sunucuda kalıcı gereksinim (210)</text>
  <text x="171" y="212" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">artı: bağımsız dağıtım/ölçek · eksi: CORS + iki hat</text>
  <rect x="338" y="126" width="302" height="106" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="489" y="148" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">ÜRETİM MODEL 2: tek jar</text>
  <text x="489" y="168" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">build çıktısı jar içinde statik sunulur</text>
  <text x="489" y="186" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">tek origin → CORS ihtiyacı yok</text>
  <text x="489" y="212" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">artı: basit dağıtım · eksi: ön yüz-arka yüz birlikte sürümlenir</text>
  <text x="330" y="260" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Her iki modelde de React senin API sözleşmeni tüketir (212)</text>
</svg>
```

### TypeScript: Java geliştiricisi için tanıdık zemin

İlan React derken pratikte çoğu zaman TypeScript'i de kastediyor. İyi haber şu: Java bilen için TypeScript'in girişi kolaydır. Çünkü amaç aynıdır — **statik tip sistemi**, yani hatayı çalışma anında değil derleme anında yakalamak.

Eşleşmeler doğrudandır. Java'nın arayüzü (interface) TypeScript'te de `interface` olarak yazılır. Jenerik (generics) yine köşeli parantezle kurulur: `Array<Ucus>` ya da kısaca `Ucus[]`. Birleşim tipi (`"iptal" | "kalkti"`) ise Java'daki enum'un esnek akrabası gibidir.

İki nokta Java'dan köklü biçimde farklıdır.

Birincisi, **tipler derleme zamanında silinir**. TypeScript JavaScript'e derlenir; çalışma anında ortada tip diye bir şey kalmaz. Java'nın tip silmesine (type erasure) benzetilebilir, ama daha köklüdür. Java'da sınıf bilgisi çalışma anında durur, TypeScript'te hiçbir tip durmaz. Sonucu şudur: API'den gelen JSON'un tipe uyduğunu derleyici **garanti etmez**. Sınırda doğrulama yine senin işindir. Java'da `javac` ne yapıyorsa TypeScript'te `tsc` onu yapar; derleme adımı boru hattının parçasıdır ve tip hatası yapıyı kırar.

İkincisi, **yapısal tipleme (structural typing)**. Java nominal tip kullanır; "bu sınıf bu arayüzü `implements` ediyor mu?" diye sorar. TypeScript şekle bakar; "aynı alanlara sahip mi?" diye sorar. `implements` yazmasan da uygun şekildeki nesne kabul edilir.

Üç pratik kural yeter. `interface` nesne şekilleri için doğaldır ve genişletilebilir; `type` birleşim ve takma ad için kullanılır. `strict` kipi ilk günden açılmalıdır, yoksa `null` ve `undefined` denetimleri devre dışı kalır. `any` yazmak tip sistemini o noktada kapatır; bilinmeyen veri için `unknown` daha dürüst bir seçimdir.

Arka yüz geliştiricisini asıl ilgilendiren kısım sonda. TypeScript tipleri **API sözleşmesinden üretilebilir**. OpenAPI şemasından (`212`) tip üreten araçlar vardır. Sözleşme değişince ön yüz **derlenmez**. Hata üretimde değil, derleme adımında yakalanır. Bu, arka yüzde şemadan kod üretmenin ön yüzdeki tam karşılığıdır.

### Mülakatta ne sorulur?

- **"React'te ne yaptınız?"** — dürüst, somut cevap iskeleti: (1) hangi bileşenleri yazdım, (2) state'i nasıl yönettim (nerede yukarı kaldırdım), (3) API'yi nasıl bağladım (`useEffect` + sözleşme, `212`), (4) hangi tuzağa düştüm ve nasıl çözdüm (ör. bağımlılık dizisi döngüsü). Tuzak anlatmak zayıflık değil, gerçek deneyim kanıtıdır.
- **"props ile state farkı?"** Sahiplik cevabı beklenir: props üstten gelir ve salt-okunurdur; state bileşenindir, değişimi render tetikler.
- **"key neden önemli?"** `equals`/`hashCode` benzetmesi Java'cı ağzında güçlü durur: key uzlaştırmanın kimliğidir; indeks key'i kayar.
- **"CORS hatası aldınız mı?"** Cevabın `210`'daki zemine oturmalı: izni veren taraf sunucudur; geliştirmede proxy, üretimde model seçimi.
- **"TypeScript biliyor musunuz?"** Kavramsal ve dürüst cevap: statik tip, arayüz ve jenerik Java'dakiyle aynı sezgiye oturur; farkı tiplerin çalışma anında silinmesi ve yapısal tiplemedir. Sözleşmeden tip üretimini (`212`) eklemek cevabı arka yüz tarafına bağlar.
- **Bilmediğin React sorusu** (ör. ileri performans optimizasyonu): dürüst köprü — yapmadığını söyle, en yakın gerçek deneyini bağla, öğrenme planını ekle. Davranışsal soru kalıpları için `195`.

### Tek cümlede

React'in çekirdeği tek cümledir: **görünüm, verinin fonksiyonudur.** Veri props ile aşağı iner. Değişim state'te olur ve yeniden çizimi tetikler. Yan etkiler `useEffect` içinde bağımlılık dizisiyle dizginlenir. Listelerde `key`, `equals`/`hashCode` gibi bir kimlik sözleşmesidir. Spring Boot ile yaşam ise iki karardan ibarettir: geliştirmede vekil sunucu, üretimde "ayrı dağıtım + CORS (`210`)" ya da "tek jar".

# reakt-1 | Temel

## Soru
Bir Java geliştiricisi React koduna bakıp "JavaScript dosyasının içinde HTML yazılmış" diyor. JSX gerçekte nedir?

## A
Tarayıcının doğrudan çalıştırdığı yeni bir HTML sürümüdür; React onu hiçbir dönüşüm yapmadan olduğu gibi DOM'a ekler
🤖 tarayıcı JSX'i tanımaz; DOM'a giden şey, derleme sonrası fonksiyon çağrılarının ürettiği elemanlardır.

## B
JavaScript'ten ayrı dosyalarda tutulan ve ayrı derlenen bir şablon dilidir; Angular'ın şablon dosyalarıyla aynı yaklaşımdır
🤖 JSX ayrı bir şablon dosyası değildir, JavaScript kodunun içinde yaşar; şablon-dosyası yaklaşımıyla fark `209`'da anlatılır.

## C
JavaScript'e derlenen bir sözdizimi uzantısıdır; her JSX ifadesi derleme adımında bir fonksiyon çağrısına dönüşür
🤖 Doğru: HTML görünümlüdür ama JavaScript'in kendisidir; bu yüzden içinde ifade, değişken ve fonksiyon kullanılabilir.

## D
JSON ile XML'i birleştiren bir veri değişim formatıdır; API yanıtlarını bileşenlere bağlamak için ağ üzerinde taşınır
🤖 JSX bir veri formatı değildir; ağda taşınmaz, derleme zamanında koda dönüşür ve çalışma anında ortada JSX kalmaz.

## Doğru
C

## Açıklama
JSX, "JavaScript içinde HTML" gibi görünen ama gerçekte **JavaScript'e derlenen bir sözdizimi uzantısıdır**: her JSX ifadesi, derleme adımında eleman üreten bir **fonksiyon çağrısına** dönüşür. Tarayıcıya JSX gitmez; giden şey derlenmiş JavaScript'tir ve çalışma anında ortada JSX diye bir şey kalmaz. Bu yüzden JSX'in içinde süslü parantezle her türlü JavaScript ifadesi kullanılabilir — o "HTML" aslında kodun kendisidir. "Tarayıcı doğrudan çalıştırır" cevabı derleme adımını yok sayar: tarayıcı JSX'i tanımaz, build zinciri (derleyici) onu çevirir. "Ayrı şablon dosyası" cevabı Angular'ın yaklaşımını React'e yapıştırır; React'in tercihi tam tersidir — görünüm mantıkla aynı dilde, aynı dosyada yaşar (çerçeve karakter farkları `209`'da). "Veri formatı" cevabı ise isim çağrışımına kanmaktır: JSX ağ üzerinde taşınan bir şey değil, derleme zamanı sözdizimidir. Java benzetmesiyle: JSX, çalışma zamanında var olmayan bir kolaylık sözdiziminin (syntactic sugar) derleyicide gerçek çağrılara açılmasıdır.

# reakt-2 | Temel

## Soru
Uçuş arama ekranında bir bileşen üstten uçuş listesini alıyor, kendi içinde de hangi satırın seçili olduğunu tutuyor. props ile state ayrımının doğru tanımı hangisidir?

## A
state üst bileşenden gelir ve salt-okunurdur; props ise bileşenin kendi içinde serbestçe değiştirdiği veridir
🤖 tanımlar yer değiştirmiş: yukarıdan gelen salt-okunur veri props'tur, bileşenin sahip olduğu değişen veri state'tir.

## B
props üstten gelen salt-okunur veridir; state bileşenin kendi yönettiği veridir ve değişimi yeniden render tetikler
🤖 Doğru: tek yönlü veri akışının iki yarısı budur — veri aşağı props'la iner, değişim state'te gerçekleşir.

## C
İkisi aynı mekanizmanın iki adıdır; aralarındaki fark yalnızca sınıf bileşeni ile fonksiyon bileşeni tercihinden doğar
🤖 fark sözdizimsel değil kavramsaldır: verinin sahibi kim ve kim değiştirebilir sorusu iki kavramı ayırır.

## D
props performans amacıyla yalnız ilk render'da okunur; sonraki tüm güncellemeler her zaman state üzerinden yürütülür
🤖 props her render'da güncel değeriyle gelir; üst bileşen yeni props geçirdiğinde alt bileşen yeniden render olur.

## Doğru
B

## Açıklama
Ayrımın özü **sahipliktir**. **props**, üst bileşenden aşağı inen **salt-okunur** veridir: senaryodaki uçuş listesi gibi — bileşen onu gösterir ama değiştiremez; değişiklik isteği geri çağrıyla yukarı çıkar. **state** ise bileşenin **kendi sahip olduğu** veridir: seçili satır gibi — bileşen onu `setState` ile değiştirir ve her değişim **yeniden render** tetikler. Bu ikili, React'in **tek yönlü veri akışının** iki yarısıdır: veri aşağı props'la iner, değişim state'te olur; iki bileşen aynı veriyi paylaşacaksa state ortak ataya **yukarı kaldırılır** (lifting state up). "Tanımlar ters" şıkkı en sık düşülen ezber hatasıdır. "Aynı şeyin iki adı" cevabı farkı sözdizimine indirger; oysa fark sınıf/fonksiyon tercihinden bağımsız, kavramsal bir sahiplik sözleşmesidir. "props yalnız ilk render'da okunur" yanılgısı ise pratikte gerçek hatalara yol açar: props her render'da güncel değeriyle gelir — üst bileşen yeni liste geçirdiğinde alt bileşen otomatik güncellenir; bunu elle state'e kopyalamaya kalkmak çift kaynak ve tutarsızlık üretir.

# reakt-3 | Orta

## Soru
Bir bileşen `useEffect` içinde API'den uçuş verisi çekip state'e yazıyor. Aşağıdaki kurgulardan hangisi sonsuz istek döngüsü üretir?

## A
Effect'e boş bağımlılık dizisi ([]) verilmiş; API çağrısı yalnız ilk render'dan sonra bir kez çalışıyor
🤖 boş dizi "bir kez çalış" demektir; ilk yükleme verisini çekmek için doğru kurgudur, döngü üretmez.

## B
Bağımlılık dizisinde `userId` var; effect yalnız `userId` değiştiğinde yeniden çalışıp yeni kullanıcının verisini çekiyor
🤖 bu, amaçlanan tepkisel kullanımdır: bağımlılık değişmeden effect tekrarlanmaz.

## C
Effect bir temizlik (cleanup) fonksiyonu döndürüyor; bileşen ekrandan kaldırılınca devam eden istek iptal ediliyor
🤖 temizlik fonksiyonu döngü değil sızıntı önlemidir; ayrılırken kaynağı bırakmak doğru pratiktir.

## D
Bağımlılık dizisi hiç verilmemiş ve effect içinde çekilen veri state'e yazılıyor
🤖 Doğru: dizi yoksa effect her render'da koşar; setState yeni render, yeni render yeni effect tetikler.

## Doğru
D

## Açıklama
Döngünün mekaniği üç halkadır: bağımlılık dizisi verilmemiş effect **her render'dan sonra** koşar → effect içindeki `setState` state'i değiştirir → state değişimi **yeni bir render** tetikler → yeni render effect'i yeniden koşturur — çember kapanır ve API'ye sonsuz istek gider. Çıkış yolu bağımlılık dizisini doğru vermektir: boş dizi `[]` "ilk render'dan sonra bir kez" demektir ve ilk yükleme verisi için doğru kurgudur; `[userId]` ise "yalnız userId değişince" anlamına gelir — ikisi de döngü üretmez, aksine döngünün ilacıdır. Temizlik fonksiyonu da döngüyle ilgisizdir: bileşen ekrandan kalkarken zamanlayıcıyı durdurmak veya isteği iptal etmek **kaynak sızıntısı** önlemidir ve iyi pratiktir. Bu soruya bağlanan ikinci kural, hook'ların **koşul veya döngü içinde çağrılmamasıdır**: React hook'ları çağrı sırasına göre eşler; sıra render'dan render'a değişirse state yanlış hook'a bağlanır. Mülakatta bu tuzağı "başıma geldi, şöyle çözdüm" diye anlatmak, ezber cevaptan çok daha güçlü bir deneyim kanıtıdır.

# reakt-4 | Orta

## Soru
Uçuş listesi `map` ile render ediliyor ve React konsolda key uyarısı veriyor. `key` prop'unun gerçek rolü hangisidir?

## A
Uzlaştırmada satırın kimliğini belirler; kararlı ve benzersiz key ile React hangi elemanın aynı kalıp hangisinin değiştiğini bilir
🤖 Doğru: Java'daki equals/hashCode gibi bir kimlik sözleşmesidir; kimlik doğruysa DOM ve state doğru satırla eşleşir.

## B
key, DOM'a id attribute'u olarak basılır; amacı erişilebilirlik araçlarına ve test seçicilerine sabit bir kimlik sağlamaktır
🤖 key DOM'a yansımaz, React'in iç muhasebesinde kalır; erişilebilirlik ve test için ayrı attribute'lar kullanılır.

## C
Dizi indeksini key yapmak en güvenli yoldur; indeks her render'da benzersiz olduğu için hiçbir eşleşme sorunu yaşanmaz
🤖 indeks araya ekleme/silmede kayar: eski satırın state'i yanlış satıra yapışır — aranan şey benzersizlik değil kararlılıktır.

## D
key yalnızca bir performans ayarıdır; eksik veya yanlış key görsel ya da durumsal hataya yol açmaz, sadece render'ı yavaşlatır
🤖 yanlış key davranış hatası üretir: form girdisi veya seçim durumu yanlış satıra taşınabilir — sorun hızdan ibaret değildir.

## Doğru
A

## Açıklama
`key`, uzlaştırmanın (reconciliation) **kimlik sözleşmesidir**: React iki render arasındaki liste elemanlarını eşleştirirken key'e bakar — aynı key "bu aynı satır" demektir, satırın DOM'u ve iç state'i korunur; farklı key "bu yeni satır" demektir. Java benzetmesi tam oturur: bir koleksiyonda nesne kimliğini `equals`/`hashCode` nasıl tanımlıyorsa, listede satır kimliğini key öyle tanımlar — kimlik sözleşmesi bozuksa koleksiyon nasıl yanlış davranırsa, liste de öyle davranır. Bu yüzden key **kararlı ve veriye ait** olmalıdır (uçuş numarası gibi). **Dizi indeksi** bu sözleşmeyi bozar: araya satır eklendiğinde ya da silindiğinde indeksler kayar, React eski satırın DOM'unu ve state'ini (açık form girdisi, seçim işareti) **yanlış satıra** eşler — indeks "her render'da benzersiz"dir ama kararlı değildir; aranan özellik benzersizlikten önce kararlılıktır. key DOM'a basılmaz; erişilebilirlik ve test kimlikleri için ayrı attribute'lar vardır. "Sadece performans" cevabı da eksiktir: yanlış key yavaşlıktan öte, üretimde bulunması zor **davranış hataları** üretir.

# reakt-5 | Orta

## Soru
Geliştirmede React dev server 3000, Spring Boot 8090 portunda çalışıyor; ekip üretim dağıtımını planlıyor. Doğru özet hangisidir?

## A
Üretimde de her zaman iki ayrı origin gerekir; React build çıktısını Spring Boot jar'ı içinde statik olarak sunmak mümkün değildir
🤖 mümkündür ve yaygındır: build çıktısı statik kaynaklara konur, tek jar hem API'yi hem ön yüzü aynı origin'den sunar.

## B
Geliştirmede proxy origin farkını gizler; üretimde ya ayrı dağıtım + CORS ya da build çıktısının jar içinde statik sunumu seçilir
🤖 Doğru: iki üretim modeli de geçerlidir; seçim dağıtım bağımsızlığı ihtiyacına ve ekip yapısına göre yapılır.

## C
CORS yalnızca geliştirme ortamına özgü bir sorundur; üretimde tarayıcılar Same-Origin Politikası'nı zaten uygulamaz
🤖 tarayıcı politikayı her ortamda uygular; ayrı origin'li üretimde CORS, sunucunun kalıcı sorumluluğudur (`210`).

## D
Dev proxy üretimde de zorunludur; tarayıcılar SPA uygulamalarını araya bir proxy konmadan çalıştırmayı reddeder
🤖 proxy bir geliştirme konforudur; üretimde ihtiyaç ya CORS yapılandırmasıyla ya da aynı origin'den sunumla ortadan kalkar.

## Doğru
B

## Açıklama
Resmin bütünü iki aşamalıdır. **Geliştirmede** iki süreç ayrı origin'lerde koşar (3000 ve 8090); dev server'a tanımlanan **proxy**, `/api` isteklerini arka uca aktarır ve tarayıcı tek origin görür — CORS sancısı geliştirme boyunca böyle gizlenir. **Üretimde** iki geçerli model vardır: (1) **ayrı dağıtım** — ön yüz statik barındırmada, API ayrı origin'de yaşar; bu modelde CORS geçici bir ayar değil, sunucunun **kalıcı sorumluluğudur** (`210`) ve karşılığında ön yüzle arka yüz bağımsız dağıtılıp ölçeklenir; (2) **tek jar** — React build çıktısı Spring Boot'un statik kaynaklarına konur, uygulama tek origin'den sunulur, CORS ihtiyacı ortadan kalkar, dağıtım basitleşir ama iki taraf birlikte sürümlenir. "Tek jar imkânsız" iddiası yanlıştır — bu, küçük ve orta ölçekte en yaygın modellerden biridir. "CORS yalnız geliştirme sorunu" cevabı tarayıcının doğasını yanlış anlar: Same-Origin Politikası her ortamda yürürlüktedir. "Proxy üretimde zorunlu" cevabı ise geliştirme konforunu mimari gereklilik sanmaktır; SPA'nın derin bağlantı ve API tüketimi zemini `208` ve `212`'de anlatılan sözleşmelerle kurulur.

# reakt-6 | İleri

## Soru
Mülakatta "React'te render performansını nasıl optimize edersiniz — memo/useMemo stratejiniz nedir?" sorusu geldi; adayın bu derinlikte gerçek deneyimi yok. En güçlü tutum hangisidir?

## A
Genel prensiplerden makul görünen bir deneyim anlatısı kurmak; içerik teknik olarak doğruysa deneyimin gerçek olmaması sorun yaratmaz
🤖 kıdemli mülakatçı tek derinlik sorusuyla anlatıyı çökertir; güven bir kez sarsılınca doğru cevaplar da şüpheyle dinlenir.

## B
Kısa bir "bu konuda çalışmadım" deyip susmak; dürüst olmak tek başına yeterli ve olumlu bir izlenim bırakır
🤖 dürüstlük doğru başlangıçtır ama köprüsüz kalırsa görüşmede boşluk bırakır; en yakın gerçek deney bağlanmalıdır.

## C
Yapmadığını açıkça söyleyip en yakın gerçek deneyimi bağlamak: gereksiz render'ı nasıl fark edip çözdüğünü anlatmak ve öğrenme planı eklemek
🤖 Doğru: dürüst köprü budur — sınırı kabul et, gerçek deneyi bağla, öğrenme iştahını göster.

## D
Soruyu Java/JVM performans deneyimine çevirip React kısmını yanıtsız bırakmak; güçlü alana dönmek her zaman en iyi stratejidir
🤖 sorulan soruyu görmezden gelip alan değiştirmek kaçış olarak okunur; köprü kurulur ama soru yanıtsız bırakılmaz.

## Doğru
C

## Açıklama
Bu sorunun sınadığı şey memo bilgisi değil, **bilgi sınırındaki davranıştır**. En güçlü tutum **dürüst köprüdür** ve üç adımdan oluşur: (1) sınırı açıkça kabul et — "memo/useMemo'yu üretimde sistematik kullanmadım"; (2) **en yakın gerçek deneyimi** bağla — "ama şu ekranda gereksiz yeniden render fark ettim, sebebini şöyle buldum, state'i yukarı kaldırıp/parçalayıp çözdüm" gibi yaşanmış bir vaka; (3) öğrenme planını ekle — neyi, nasıl öğreneceğin. Bu kurgu hem dürüstlüğü hem mühendislik refleksini aynı anda gösterir. Deneyim uydurmak en riskli seçenektir: kıdemli mülakatçı tek bir derinlik sorusuyla ("profiler'da neye baktınız?") anlatıyı çökertir ve güven kaybı görüşmenin geri kalanını zehirler — bu modülün değişmez ilkesi, olmayan deneyimi varmış gibi göstermemektir. Yalnız "çalışmadım" deyip susmak dürüst ama eksiktir; görüşmeye köprüsüz bir boşluk bırakır. Soruyu Java'ya çevirip React kısmını yanıtsız bırakmak ise kaçış olarak okunur; güçlü alana dönüş ancak sorunun kendisi dürüstçe yanıtlandıktan sonra yapılırsa değer katar. Davranışsal cevap kalıpları için `195`.
