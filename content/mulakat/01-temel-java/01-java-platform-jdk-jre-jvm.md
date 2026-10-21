---
slug: java-platform-jdk-jre-jvm
baslik: "Java Platformu: JDK / JRE / JVM ve 'Bir Kez Yaz, Her Yerde Çalıştır'"
kademe: Temel
sira: 1
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Şöyle düşün: bir kitap yazdın ve her ülkede okunmasını istiyorsun. İki yol var — kitabı her ülke için baştan yazmak, ya da tek bir ortak metin yazıp her ülkeye o metni okuyabilen bir çevirmen göndermek. Java ikinci yolu seçti. Sen programı bir kez yazarsın; her işletim sistemine kendi "çevirmeni" kurulur ve aynı program orada da çalışır. Bunun adı **"bir kez yaz, her yerde çalıştır"**tır (write once, run anywhere / WORA). JDK, JRE ve JVM ise bu düzenin sırasıyla **yazma**, **çalıştırma** ve **çeviri** katmanlarıdır.

### Temel kavramlar

- **Kaynak kod (source code)**: senin yazdığın `.java` dosyası; insanın okuduğu metin.
- **Derleyici (compiler)**: kaynağı makinenin anlayacağı biçime çeviren araç. Java'nınki `javac`'tır.
- **Bayt kodu (bytecode)**: `javac`'ın ürettiği `.class` dosyasının içeriği. Ne insan dili ne işlemci dilidir; ikisinin arasında duran bir **ara temsildir**. Hiçbir işlemciye ait olmadığı için **platformdan bağımsızdır**. Sıkıştırılmış kaynak kod DEĞİLDİR — geri okumak ayrı bir işlem (dekompilasyon) ister.
- **JVM (Java Virtual Machine — Java Sanal Makinesi)**: bytecode'u çalıştıran soyut makine; yazılımla yapılmış bir işlemci gibi düşün. Burada mülakatın sevdiği ince bir ayrım var:
  - JVM **spesifikasyonu** (specification — kural kitabı) platformdan **bağımsızdır**; herkes aynı kuralları uygular.
  - JVM **gerçeklemesi** (implementation — o kuralları uygulayan asıl program) platforma **özgüdür**: Windows, Linux ve macOS için ayrı ayrı derlenmiş ikililer vardır.
- **JRE (Java Runtime Environment — çalışma-zamanı ortamı)**: JVM + **standart kütüphaneler** (`java.lang`, `java.util` …) + çalışma-zamanı dosyaları. Java **çalıştırmak** için gereken her şeydir; içinde **derleyici yoktur**.
- **JDK (Java Development Kit — geliştirme kiti)**: JRE + **geliştirme araçları**: `javac` (derleyici), `jar` (paketleyici), `javadoc` (belge üretici), `jdb` (hata ayıklayıcı). Kod **yazıp derlemek** için gereken üst kümedir.

Kapsam ilişkisi tek satırda: **JDK ⊃ JRE ⊃ JVM**.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JDK JRE JVM iç içe kapsam">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kapsam: JDK ⊃ JRE ⊃ JVM</text>
  <rect x="60" y="40" width="520" height="160" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="150" y="62" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">JDK</text>
  <text x="150" y="80" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">javac, jar, javadoc</text>
  <rect x="110" y="72" width="430" height="112" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="185" y="94" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">JRE</text>
  <text x="185" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">standart kütüphaneler</text>
  <rect x="160" y="104" width="330" height="66" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="325" y="132" text-anchor="middle" font-size="13" font-weight="700" fill="#B26B00" font-family="system-ui">JVM</text>
  <text x="325" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bytecode çalıştırır (platforma özgü)</text>
</svg>
```

Pratik sonuç: yalnızca çalıştıracaksan **JRE yeter**, JDK gereğinden fazladır. Çıplak JVM ise yetmez — hemen her program `java.lang`/`java.util` gibi standart kütüphanelere muhtaçtır. Kaynak kod + metin editörü ise hiç yetmez; çalıştırmak için bir çalışma-zamanı şarttır.

### Nasıl çalışır?

Somut örnek üzerinden adım adım:

```java
// Merhaba.java
public class Merhaba {
    public static void main(String[] args) {
        System.out.println("Merhaba");
    }
}
```

1. `javac Merhaba.java` → `Merhaba.class` üretilir. Bu adımda kaynak, hedef işletim sistemine özgü makine koduna (native machine code) değil, **platformdan bağımsız bytecode**'a çevrilir. Native kod üretmek C/C++ derleyicilerinin işidir.
2. `java Merhaba` → o makinedeki JVM `.class` dosyasını yükler.
3. JVM önce bytecode'u **yorumlayarak** (interpreter) adım adım işletir.
4. Sık çalışan "sıcak" bölümleri fark edince onları **JIT** (Just-In-Time) derleyicisiyle çalışma-zamanında native koda çevirir. Bu yüzden uzun süre ayakta kalan uygulamalar zamanla hızlanır.

```svg
<svg viewBox="0 0 640 215" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bir kez yaz her yerde çalıştır derleme akışı">
  <defs>
    <marker id="jdk-ok" markerWidth="9" markerHeight="9" refX="7.5" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı bytecode, her platformun kendi JVM'i</text>
  <rect x="40" y="42" width="120" height="48" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="100" y="63" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Kaynak</text>
  <text x="100" y="80" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">.java</text>
  <line x1="162" y1="66" x2="212" y2="66" stroke="#5D7789" stroke-width="1.6" marker-end="url(#jdk-ok)"/>
  <text x="187" y="58" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">javac</text>
  <rect x="215" y="42" width="150" height="48" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="290" y="63" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Bytecode</text>
  <text x="290" y="80" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">.class · platformsuz</text>
  <line x1="290" y1="90" x2="290" y2="112" stroke="#5D7789" stroke-width="1.6"/>
  <line x1="130" y1="112" x2="510" y2="112" stroke="#5D7789" stroke-width="1.6"/>
  <line x1="130" y1="112" x2="130" y2="140" stroke="#5D7789" stroke-width="1.6" marker-end="url(#jdk-ok)"/>
  <line x1="320" y1="112" x2="320" y2="140" stroke="#5D7789" stroke-width="1.6" marker-end="url(#jdk-ok)"/>
  <line x1="510" y1="112" x2="510" y2="140" stroke="#5D7789" stroke-width="1.6" marker-end="url(#jdk-ok)"/>
  <rect x="60" y="142" width="140" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="130" y="164" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Windows JVM</text>
  <text x="130" y="182" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">aynı .class çalışır</text>
  <rect x="250" y="142" width="140" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="320" y="164" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Linux JVM</text>
  <text x="320" y="182" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">aynı .class çalışır</text>
  <rect x="440" y="142" width="140" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="510" y="164" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">macOS JVM</text>
  <text x="510" y="182" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">aynı .class çalışır</text>
</svg>
```

"Java platformdan bağımsızdır ama JVM platforma bağımlıdır" cümlesi çelişki değil, tasarımın ta kendisidir: **değişmeyen bytecode + platforma özgü JVM = taşınabilirlik**. Kaynağı her platform için yeniden yazmak gerekmez; aynı `.class`/`.jar` her yerde çalışır. Neden böyle tasarlandı? Çünkü platform farkını tek bir yere (JVM'e) hapsedersen, milyonlarca uygulamayı değil yalnızca bir avuç JVM'i taşımak yeter.

Alternatif yaklaşım **AOT**'tur (Ahead-Of-Time — önceden derleme): GraalVM'in **native image** aracı bytecode'u önceden platforma özgü tek bir yürütülebilir ikiliye derler. Kazanç: **çok hızlı başlangıç** ve **düşük bellek** — sunucusuz (serverless) ve kısa ömürlü servisler için idealdir. Bedel üç kalemdir. (1) JIT'in uzun süreli iş yükünde ulaştığı **tepe performans** kaybolur. (2) Yansıma (reflection), dinamik vekil nesne (dynamic proxy) ve kaynak yükleme gibi dinamik davranışlar derleme anında bilinmez; bu yüzden **ek yapılandırma** (reachability metadata) isterler. (3) Üretilen ikili **platforma özgüdür**, yani WORA taşınabilirliğinden ödün verilir.

### Mülakatta ne sorulur?

- **Kapsam yönü.** `JDK ⊃ JRE ⊃ JVM`. Yönü tersine çevirmek (`JVM ⊃ JRE ⊃ JDK`) ya da üçünü eşdeğer saymak klasik eleme hatasıdır. Aklında kalacak tek cümle: **derleyici JDK'da vardır, JRE'de yoktur; JVM yalnız yürütücüdür.**
- **`javac` ne üretir?** Platformdan bağımsız bytecode — native kod değil, sıkıştırılmış kaynak da değil, taşınamaz bir ikili hiç değil.
- **Salt çalıştırma için minimum ne?** JRE (yani JVM + standart kütüphaneler).
- **Sürüm gerçeği:** Java 9'dan itibaren JRE ayrı indirilebilir bir paket olarak dağıtılmıyor; ihtiyaca göre `jlink` ile küçültülmüş özel çalışma-zamanı imajı üretiliyor. Kavramsal ayrım yine geçerli ve hâlâ soruluyor.
- **LTS (Long-Term Support).** Üretimde 8, 11, 17, 21, 25 gibi LTS sürümleri tercih edilir. Java her **6 ayda bir** yeni sürüm yayımlar; **ara (non-LTS)** sürümler yalnız bir sonraki sürüm çıkana kadar güncelleme alır, LTS sürümleri ise yıllarca güvenlik yaması ve hata düzeltmesi alır. Tuzak: LTS bir **destek penceresi** etiketidir — "daha hızlı çalışır" ya da "daha çok dil özelliği içerir" demek DEĞİLDİR; yeni özellikler çoğu kez önce ara sürümlerde denenir. Ara sürümler de üretime uygundur ("deneysel" değildir), sadece daha sık yükseltme gerektirir.
- **Dağıtımlar** (Eclipse Temurin, Oracle JDK, Amazon Corretto, GraalVM): hepsi aynı **Java SE spesifikasyonunu** gerçekler ve **TCK** (Technology Compatibility Kit) uyumludur; aynı sürüm numarasında davranışsal olarak uyumludurlar. Fark **lisans** (Oracle JDK'nin ticari lisans geçmişi ile Temurin'in tamamen açık/ücretsiz oluşu), **destek modeli** ve **paketlemededir** — bytecode uyumu değil. Temurin "hafif/eksik" bir JDK değil, tam bir Java SE dağıtımıdır; dağıtım değiştirmek uygulama kodunu değiştirmez, yeniden derleme de gerektirmez. İstisna: GraalVM'in `native image`'ı gibi **standart-dışı ek yetenekler**e bağımlı olmak taşınabilirliği kısıtlayabilir.

```svg
<svg viewBox="0 0 640 195" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Farklı JDK dağıtımları aynı standardı gerçekler">
  <defs>
    <marker id="jdk-ok2" markerWidth="9" markerHeight="9" refX="7.5" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Farklı dağıtım, aynı Java SE standardı</text>
  <rect x="35" y="40" width="170" height="42" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="120" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Eclipse Temurin</text>
  <rect x="235" y="40" width="170" height="42" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="320" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Oracle JDK</text>
  <rect x="435" y="40" width="170" height="42" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="520" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Amazon Corretto</text>
  <line x1="120" y1="82" x2="120" y2="104" stroke="#5D7789" stroke-width="1.6" marker-end="url(#jdk-ok2)"/>
  <line x1="320" y1="82" x2="320" y2="104" stroke="#5D7789" stroke-width="1.6" marker-end="url(#jdk-ok2)"/>
  <line x1="520" y1="82" x2="520" y2="104" stroke="#5D7789" stroke-width="1.6" marker-end="url(#jdk-ok2)"/>
  <rect x="90" y="106" width="460" height="40" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="131" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Aynı Java SE spec + TCK uyumu → bytecode taşınabilir</text>
  <text x="320" y="172" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Fark yalnızca: lisans · destek modeli · paketleme</text>
</svg>
```

- **Native image efsanesi:** "her zaman daha hızlı" yanlıştır. Kısa ömürlü işlerde native kazanır; uzun çalışan tepe yükte JIT genelde öne geçer. Ve etkisi "sadece kaynak kodu gizlemek" değildir — asıl mesele başlangıç/bellek/tepe performans takasıdır.

### Tek cümlede

JDK ile yazıp derlersin, JRE ile çalıştırırsın; JVM'in kendisi platforma özgü olduğu için platformdan bağımsız bytecode her yerde aynı şekilde çalışır.

# jdk-1 | Temel
## Soru
JDK, JRE ve JVM arasındaki kapsam ilişkisi aşağıdakilerden hangisidir?
## A
JDK ⊃ JRE ⊃ JVM (JDK, JRE'yi; JRE, JVM'i içerir)
🤖 Doğru: JDK geliştirme araçları + JRE; JRE ise kütüphaneler + JVM içerir.
## B
JVM ⊃ JRE ⊃ JDK (JVM en geniş kümedir)
🤖 Ters: JVM en dar katmandır, JDK en geniştir; kapsam yönü yanlış.
## C
Üçü de eşdeğerdir, sadece farklı isimlerdir
🤖 Farklı kapsamları vardır; JDK'da derleyici, JRE'de yok, JVM sadece yürütücüdür.
## D
JRE ⊃ JDK ⊃ JVM (JRE en geniş kümedir)
🤖 JRE derleyici içermez; JDK'yı kapsayamaz, kapsam yönü hatalı.
## Doğru
A
## Açıklama
Kapsam iç içedir: **JVM** yalnız bytecode'u çalıştıran motordur. **JRE** = JVM + standart kütüphaneler (yalnız çalıştırma için yeter). **JDK** = JRE + geliştirme araçları (`javac`, `jar`, `javadoc`). Bu yüzden **JDK ⊃ JRE ⊃ JVM**. Çeldiriciler kapsam yönünü tersine çevirir (B, D) veya üçünü eşitler (C); mülakatta "JDK'da derleyici var, JRE'de yok" ayrımı en kritik noktadır.

# jdk-2 | Temel
## Soru
`javac` derleyicisinin ürettiği `.class` dosyası neyi içerir?
## A
Hedef işletim sistemine özgü makine kodu (native machine code)
🤖 `javac` native kod üretmez; onu JVM çalışma-zamanında yorumlar/derler.
## B
Platformdan bağımsız Java bytecode'u
🤖 Doğru: bytecode her platformun JVM'inde çalışan ara temsildir (WORA'nın temeli).
## C
Sıkıştırılmış kaynak kodu (source code)
🤖 `.class` kaynak değil; derlenmiş bytecode taşır, geri okunması dekompilasyon gerektirir.
## D
Yalnızca o makinede çalışan, taşınamaz bir ikili (binary)
🤖 Aksine; bytecode taşınabilirdir, farklı platformların JVM'lerinde aynı çalışır.
## Doğru
B
## Açıklama
`javac`, Java kaynağını (`.java`) doğrudan native makine koduna değil, **platformdan bağımsız bytecode**'a (`.class`) çevirir. Bu bytecode her işletim sistemi/işlemci için ayrı yazılmış JVM tarafından çalıştırılır — "bir kez yaz, her yerde çalıştır" tam olarak budur. Native kod üretimi (A) C/C++ derleyicilerinin işidir; JVM bytecode'u çalışma-zamanında yorumlar ve sık kullanılan yolları **JIT** (Just-In-Time) ile native koda derler. Bytecode kaynak kodu değildir (C), taşınamaz da değildir (D).

# jdk-3 | Temel
## Soru
Bir makinede yalnızca Java **çalıştırmak** (derlemek değil) istiyorsanız minimum olarak hangisi yeterlidir?
## A
JDK; başka türlü hiçbir Java programı çalışmaz
🤖 JDK çalışır ama gereğinden fazladır; salt çalıştırma için derleyici gerekmez.
## B
Sadece JVM, hiçbir kütüphane olmadan
🤖 Çıplak JVM yetmez; programlar `java.lang`/`java.util` gibi standart kütüphanelere muhtaçtır.
## C
JRE (veya JVM + standart kütüphaneler)
🤖 Doğru: çalıştırma için JVM + kütüphaneler yeter; `javac` gerekmez.
## D
Sadece kaynak kod ve bir metin editörü
🤖 Kaynağı çalıştırmak için çalışma-zamanı (runtime) şarttır; editör tek başına çalıştırmaz.
## Doğru
C
## Açıklama
Salt **çalıştırma** için gereken minimum **JRE**'dir: JVM + programların bağımlı olduğu standart kütüphaneler. `javac` (derleyici) yalnız kod **derlerken** gerekir, bu yüzden çalıştırma için tam JDK şart değildir (A gereğinden fazla). Çıplak JVM tek başına yetmez çünkü hemen her program `java.lang`, `java.util` gibi kütüphaneleri kullanır (B). Not: Java 9+ ile JRE ayrı dağıtım olarak küçülmüş, yerini `jlink` ile üretilen özel çalışma-zamanı imajları almıştır; kavramsal ayrım yine geçerlidir.

# jdk-4 | Orta
## Soru
"Java platformdan bağımsızdır ama JVM platforma bağımlıdır" ifadesi ne anlama gelir?
## A
Çelişkilidir; ikisi aynı anda doğru olamaz
🤖 Çelişki yok; taşınabilirlik tam da platforma özgü JVM katmanı sayesinde sağlanır.
## B
Bytecode her platformda aynıdır; onu çalıştıran JVM platforma özgü yazılır
🤖 Doğru: değişmeyen bytecode + platforma özgü JVM = taşınabilirlik.
## C
Java kaynağı her platform için ayrı ayrı yeniden yazılmalıdır
🤖 Kaynak tek yazılır; yeniden yazma gerekmez, taşınabilirliğin amacı budur.
## D
JVM aslında platformdan bağımsızdır, ifade yanlıştır
🤖 JVM gerçeklemesi platforma özgüdür (Linux/Windows/Mac ayrı ikililer); ifade doğrudur.
## Doğru
B
## Açıklama
Taşınabilirliğin (portability) mekanizması tam da bu iş bölümüdür: `javac` her yerde **aynı bytecode**'u üretir (platformdan bağımsız katman), ve her işletim sistemi/işlemci için **ayrı derlenmiş bir JVM** bu ortak bytecode'u kendi platformunda çalıştırır (platforma bağımlı katman). Yani taşınabilirliği sağlayan şey, platforma özgü JVM'in varlığıdır — çelişki değil, tasarımın özüdür (A yanlış). Kaynak tek kez yazılır (C yanlış); JVM gerçeklemeleri platforma özgüdür (D yanlış, JVM **spesifikasyonu** bağımsız olsa da gerçeklemesi platforma özeldir).

# jdk-5 | Orta
## Soru
Üretim (production) ortamı için sürüm seçerken "LTS" (Long-Term Support) sürümleri neden tercih edilir?
## A
LTS sürümleri ara sürümlerden daha hızlı çalışır; performans avantajı üretimde belirleyicidir
🤖 Performans farkı LTS etiketiyle ilgili değil; LTS uzun destek/güncelleme garantisidir.
## B
Yalnız LTS sürümleri üretimde kullanılabilir; ara sürümler deneysel olduğundan yasaktır
🤖 Ara sürümler de üretime uygundur; fark yalnız destek süresidir, "deneysel" değildir.
## C
LTS sürümleri daha fazla dil özelliği içerir; yeni sözdizimi önce LTS'e gelir
🤖 Yeni özellikler genelde önce ara sürümlerde gelir; LTS özellik değil destek süresi meselesidir.
## D
LTS sürümleri uzun süreli güvenlik ve hata düzeltme desteği alır; ara sürümler kısa sürede desteksiz kalır
🤖 Doğru: LTS = uzun destek penceresi, üretim için istikrar ve güvenlik yaması sürekliliği.
## Doğru
D
## Açıklama
**LTS** (Long-Term Support: 8, 11, 17, 21, 25…), uzun yıllar boyunca güvenlik yaması ve hata düzeltmesi alan sürümlerdir; üretim ortamı için **istikrar ve güncelleme sürekliliği** sağlar. Java her 6 ayda bir yeni sürüm yayımlar; ara (non-LTS) sürümler genelde yalnız bir sonraki sürüm gelene kadar desteklenir — bu da sürekli yükseltme baskısı yaratır. LTS etiketi performans (A) veya özellik zenginliği (C) değil, **destek penceresi** anlamına gelir; yeni özellikler çoğu kez önce ara sürümde denenir. Ara sürümler de üretime uygundur (B yanlış), sadece daha sık yükseltme gerektirir.

# jdk-6 | İleri
## Soru
Ekip "Oracle JDK yerine Eclipse Temurin kullanalım" diyor. Bir uygulama geliştiricisi açısından en doğru değerlendirme hangisidir?
## A
Bytecode uyumsuz olur; Temurin ile derlenen kod Oracle JVM'de çalışmaz, yeniden derleme gerekir
🤖 İkisi de aynı Java SE standardını gerçekler; bytecode taşınabilirliği korunur.
## B
Temurin bir "hafif" JDK'dir; standart kütüphanelerin bir kısmı eksiktir, bazı API'ler bulunmaz
🤖 Temurin tam bir Java SE dağıtımıdır; kütüphaneler eksik değildir.
## C
Aynı Java SE sürümünü gerçekleyen dağıtımlar davranışsal olarak uyumludur; fark lisans, destek ve paketlemededir
🤖 Doğru: Temurin/Oracle/Corretto aynı standardı gerçekler; ayrım lisans/destek/ek araçtadır.
## D
Dağıtım değişince uygulama kodunun tamamı yeni dağıtıma göre yeniden yazılmalıdır
🤖 Standart uyumlu dağıtımlar arasında geçiş kodu etkilemez; yeniden yazma gerekmez.
## Doğru
C
## Açıklama
Eclipse Temurin, Oracle JDK, Amazon Corretto gibi dağıtımlar hepsi aynı **Java SE spesifikasyonunu** gerçekler ve TCK (Technology Compatibility Kit) uyumludur; bu yüzden aynı sürüm numarasında **davranışsal olarak uyumludurlar**. Uygulama geliştiricisi için asıl fark **lisans** (Oracle JDK'nin ticari lisans geçmişi vs Temurin'in tamamen açık/ücretsiz oluşu), **destek modeli** ve paketlemedir — bytecode uyumluluğu değil (A yanlış). Temurin eksik/hafif değil tam bir SE dağıtımıdır (B yanlış); geçiş kodu değiştirmez (D yanlış). İstisna: GraalVM gibi bazı dağıtımlar **native image** gibi ek yetenekler sunar ki bunlar standart-dışı ek özelliklerdir; oraya bağımlı olmak taşınabilirliği kısıtlayabilir.

# jdk-7 | İleri
## Soru
GraalVM'in "native image" özelliği klasik JVM üzerinde çalıştırmaya göre temel takası (trade-off) nedir?
## A
Native image her platformda çalışan tek bir ikili üretir; bu yüzden taşınabilirliği JVM'den de artırır
🤖 Native image platforma özgü ikili üretir; taşınabilirliği azaltır, artırmaz.
## B
Native image ile JVM'e hiç gerek kalmaz ve her tür iş yükünde JIT'ten daha hızlı çalışır
🤖 "Her zaman daha hızlı" yanlış; uzun çalışan yükte JIT tepe performansı native'i geçebilir.
## C
Native image'in tek etkisi kaynak kodu gizlemesidir; başlangıç süresi ve bellek kullanımı değişmez
🤖 Asıl etki başlangıç/bellek/performans takasıdır; "sadece gizleme" ciddi bir eksik değerlendirmedir.
## D
Önceden derleme (AOT) hızlı başlangıç ve düşük bellek sağlar; karşılığında JIT'in tepe verimi ve tam dinamik esneklik kaybedilir
🤖 Doğru: AOT hızlı başlangıç/düşük RAM verir; JIT tepe performansı ve dinamiklik ödünü alınır.
## Doğru
D
## Açıklama
GraalVM **native image**, bytecode'u **önceden** (Ahead-Of-Time / AOT) platforma özgü tek bir yürütülebilir ikiliye derler. Kazanç: **çok hızlı başlangıç** ve **düşük bellek** — sunucusuz (serverless) ve kısa ömürlü servisler için idealdir. Bedel: çalışma-zamanı **JIT** derleyicisinin uzun süreli iş yükünde ulaştığı **tepe performansı** kaybedersin; ayrıca reflection, dinamik proxy, kaynak yükleme gibi dinamik davranışlar derleme anında bilinmediği için ek yapılandırma (reachability metadata) gerektirir. Ayrıca native image **platforma özgüdür** — WORA taşınabilirliğinden ödün verir (A yanlış). "Her zaman daha hızlı" bir efsanedir (B): kısa ömürde native kazanır, uzun çalışan tepe yükte JIT genelde öne geçer.
