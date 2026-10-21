---
slug: online-kodlama-testleri
baslik: "Online Kodlama Testleri: Girdi Okuma Kalıpları, Kısıt Analizi ve Starter Code Disiplini"
kademe: Temel
sira: 257
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Mülakat sürecinin ikinci gününde e-posta gelir: "Aşağıdaki bağlantıdan 45 dakikalık kodlama testini tamamlayınız." Bağlantı seni HackerEarth benzeri bir sayfaya götürür. Ekranın solunda problem metni, sağında içi yarı dolu bir editör, altında "Compile & Test" düğmesi vardır. Problem şudur: N karakter veriliyor, hepsini birleştirip tek satırda yazdır. Beş dakikada çözülür. Ama üç kişiden biri bu soruyu kaybeder — algoritmayı bilmediği için değil, ikinci satırdaki sayıyı yanlış okuduğu, verilen fonksiyon imzasını değiştirdiği ya da örnek girdiyi geçince "tamamdır" deyip gönderdiği için.

Bu testler bir algoritma yarışması değildir. Ölçtükleri şey **disiplindir**: girdiyi doğru okuyor musun, kısıtları okuyor musun, sınır durumlarını düşünüyor musun, sana verilen sözleşmeye sadık kalıyor musun. Kıdemli bir geliştiricinin bu testte elenmesi neredeyse her zaman bu dört maddeden birindendir. Konunun amacı, o dört maddeyi refleks haline getirmektir.

### Temel kavramlar

- **Gizli test case (hidden test):** Problem metninde bir veya iki **örnek girdi/çıktı** gösterilir. Puanı belirleyen bunlar değildir; gönderdiğin kod, senin hiç görmediğin onlarca girdiyle çalıştırılır. Bu girdilerin içinde en büyük kısıt değerleri (N'in tavanı), en küçük değerler (N=1), negatifler ve dejenere durumlar bulunur. **Örnek girdiyi geçmek "çözdüm" demek değildir.**
- **Zaman limiti (time limit):** Test başına tipik olarak **1 saniye** civarıdır. Aşarsan sonuç "yanlış cevap" değil **TLE** (Time Limit Exceeded) olur — kodun doğrudur ama puan alamazsın.
- **Bellek limiti:** Genelde 256 MB civarı. Java'da bu, gereksiz kutulama (`Integer[]` yerine `int[]`) ve gereksiz kopyalama konularında dikkatli olmayı gerektirir.
- **Compile & Test vs Submit:** İlk düğme yalnız görünen örnekleri çalıştırır, ikinci düğme gizli testlerin tamamını. Çoğu platformda gönderim sayısı sınırlıdır; "önce derlensin diye bir gönderirim" alışkanlığı pahalıya patlar.
- **Starter code (boilerplate):** Platformun editöre önceden koyduğu iskelet: `main` metodu, girdiyi okuyan satırlar ve senin dolduracağın imzası verilmiş bir fonksiyon. Bu iskelet **sözleşmedir**, taslak değildir.
- **stdin / stdout:** Girdi standart girdiden okunur, çıktı standart çıktıya yazılır. Dosya açmak, kullanıcıya "Lütfen sayı giriniz" gibi bir istem yazdırmak yanlış çıktı üretir — beklenen metin `omar` iken `Lütfen sayı giriniz: omar` yazan bir program tüm testleri kaybeder.

### Girdiyi okumak: `Scanner` mi, `BufferedReader` mı?

`Scanner` öğrenmesi kolaydır, kodu kısadır ve N küçükken tamamen yeterlidir. Ama içeride her token için düzenli ifade (regex) tabanlı ayrıştırma yapar ve tamponlaması zayıftır. 10⁵ satırın üzerine çıkan girdilerde bu fark, kodun doğru olduğu halde zaman limitine takılmasına yetecek kadar büyür — ölçümlere göre yüz binlerce token okumada `Scanner` ile `BufferedReader` arasında bir kat değil, **birkaç kat** fark oluşur.

```java
Scanner sc = new Scanner(System.in);
int n = sc.nextInt();
```

```java
BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
int n = Integer.parseInt(br.readLine().trim());
int[] a = new int[n];
StringTokenizer st = new StringTokenizer(br.readLine());
for (int i = 0; i < n; i++) {
    a[i] = Integer.parseInt(st.nextToken());
}
```

Pratik kural: **N ≤ 10⁴ ise `Scanner` rahat, N ≥ 10⁵ ise `BufferedReader` + `split()`/`StringTokenizer`.** Aynı mantık çıktı için de geçerlidir. Yüz bin satırı tek tek `System.out.println` ile basmak yavaştır. Doğrusu, hepsini bir `StringBuilder`'da toplayıp tek seferde basmaktır.

`Scanner` kullanacaksan bilmen gereken tek tuzak şudur: **`nextInt()` satır sonu karakterini tüketmez.**

```java
int n = sc.nextInt();        // "3\n" girdisinden yalnız 3'ü alır, '\n' tamponda kalır
String ad = sc.nextLine();   // kalan '\n'a kadar okur → BOŞ STRING döner
```

`nextInt()`, `nextDouble()`, `next()` gibi metotlar **token** okur ve token bittiği yerde durur; imleç hâlâ satır sonundadır. Hemen ardından çağrılan `nextLine()` o satırın kalanını okuduğu için boş string verir. Çözüm iki tanedir: ya `nextInt()` sonrasında bir kez fazladan `sc.nextLine()` çağırıp satır sonunu yutarsın, ya da baştan sona tek bir okuma biçiminde kalırsın — ya hep token (`next()`, `nextInt()`), ya hep satır (`nextLine()` + `split()`). Karışık kullanım bu hatanın kaynağıdır.

Token okuma ile satır okuma arasındaki seçim girdi formatına bağlıdır. `o m a r` gibi boşlukla ayrılmış veriyi `next()` ile tek tek almak doğaldır. İçinde boşluk geçen bir isim ya da cümle okunacaksa `nextLine()` şarttır. Çünkü `next()` ilk boşlukta durur; "Ahmet Yılmaz" girdisinden yalnızca "Ahmet"i alır.

### Starter code disiplini: imzaya dokunma

Platform sana çoğu zaman şuna benzer bir iskelet verir:

```java
public class Main {
    static String solve(int n, List<Character> ch) {
        // Write your code here
    }

    public static void main(String[] args) {
        // platformun yazdığı okuma ve yazdırma kodu — DOKUNMA
    }
}
```

Buradaki kurallar pazarlığa açık değildir:

- **Sınıf adı `Main` olmalıdır** (platform genelde böyle ister). `public class Cozum` yazarsan derleyici "class Cozum is public, should be declared in a file named Cozum.java" hatası verir ve puanın sıfırlanır.
- **Yalnızca istenen fonksiyonun gövdesini doldur.** `main`'i, okuma kodunu, yazdırma biçimini değiştirme.
- **İmza sabittir.** Parametre tipini `List<Character>`'dan `char[]`'a çevirmek, dönüş tipini `void` yapıp içeride kendin yazdırmak ya da fonksiyon adını değiştirmek derleme hatasıdır. Platformun görünmeyen test koşucusu (test harness) o imzayı adıyla ve tipiyle çağırır.
- **Fonksiyon imzası verilmişse ekrana bir şey yazdırma, `return` et.** Yazdırma işini `main` yapar; sen de yazdırırsan çıktı ikiye katlanır.

Ek olarak: `import` satırları eksikse eklemek serbesttir, çünkü bu imzayı bozmaz. Yaygın bir hata `List` kullanıp `import java.util.List;` eklememektir.

### Kısıtlardan karmaşıklık tavanını okumak

Problem metninin en alt satırındaki `1 ≤ N ≤ 10^5` ifadesi süs değildir; **hangi algoritmayı yazabileceğini söyleyen talimattır.** Tipik bir platform saniyede kabaca 10⁸ temel işlem çalıştırabildiği için, N'e bakıp izin verilen karmaşıklık tavanını hesaplamak mümkündür.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kisit degerinden karmasiklik tavanini okuma tablosu">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kısıt sana hangi algoritmayı yazacağını söyler</text>
  <rect x="26" y="38" width="608" height="30" rx="6" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.4"/>
  <text x="111" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kısıt (N)</text>
  <text x="288" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Karmaşıklık tavanı</text>
  <text x="507" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Ne yazabilirsin</text>
  <rect x="26" y="68" width="608" height="44" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="111" y="95" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">N ≤ 10</text>
  <text x="288" y="95" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">pratikte tavan yok</text>
  <text x="507" y="95" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">brute force, permütasyon, özyineleme — hepsi geçer</text>
  <rect x="26" y="112" width="608" height="44" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="111" y="139" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">N ≤ 10³</text>
  <text x="288" y="139" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">O(n²) kabul edilir</text>
  <text x="507" y="139" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iç içe iki döngü sorun değil (~10⁶ işlem)</text>
  <rect x="26" y="156" width="608" height="44" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="111" y="183" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">N ≤ 10⁵</text>
  <text x="288" y="183" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">O(n log n) tavandır</text>
  <text x="507" y="183" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sıralama, HashMap, tek geçiş — iç içe döngü TLE olur</text>
  <rect x="26" y="200" width="608" height="44" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="111" y="227" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">N ≤ 10⁹</text>
  <text x="288" y="227" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">O(log n) veya O(1)</text>
  <text x="507" y="227" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">N üzerinde döngü kurulamaz: formül / matematik / ikili arama</text>
  <text x="330" y="270" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Kaba ölçek: 1 saniye ≈ 10⁸ temel işlem</text>
  <text x="330" y="288" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">N ≥ 10⁵ ise Scanner yerine BufferedReader, çıktı için StringBuilder</text>
</svg>
```

Bu tabloyu tersten okumak da mümkündür; mülakatta değerli olan da budur. Kısıt `N ≤ 10⁹` diyorsa problem seni **kapalı form bir çözüme** yönlendiriyordur. `N ≤ 20` gibi tuhaf küçük bir tavan görüyorsan problem muhtemelen **üstel** bir çözüm (2ᴺ alt küme taraması) bekliyordur. Kısıt okumadan kod yazmak, bu testlerde yapılan en pahalı hatadır. Çünkü kod derlenir, örnek girdiyi geçer ve yalnızca en büyük gizli testte sessizce ölür.

### Sınır durumları ve taşma

Gizli testlerin çoğu, ortalama bir girdiyi değil **uçları** dener. Göndermeden önce zihinden geçirilecek liste kısadır:

- **N = 1:** Tek elemanlı girdi. Döngüsü `for (int i = 1; i < n; i++)` biçiminde kurulmuş bir kod burada hiç dönmez ve boş çıktı verir.
- **Boş girdi / boş satır:** `readLine()` `null` dönebilir, `split()` boş dizi verebilir.
- **Tüm elemanlar aynı:** Sayaç ve tekilleştirme mantıklarını kırar.
- **Negatif değerler:** Kısıt `-10⁹ ≤ a[i]` diyorsa, "en büyüğü bul" için `int max = 0;` ile başlamak yanlıştır — doğrusu `Integer.MIN_VALUE` ya da dizinin ilk elemanıdır.
- **Taşma (overflow):** En sinsi olanı budur. 10⁵ elemanın her biri 10⁹ olabiliyorsa toplam 10¹⁴'e çıkar; `int`'in tavanı ise yaklaşık 2,1 × 10⁹'dur. `int` toplamı sessizce taşar, hata fırlatmaz, negatif bir sayıya döner ve kod "yanlış cevap" alır. Kural: **elemanlar `int` sığıyor diye toplam da sığar sanma; birikimli değeri `long` tut** (`02`).

Taşmanın hata vermemesi Java'da bilinçli bir tasarım tercihidir; `int` aritmetiği ikiye tümleyen gösterimde sarmalanır (wrap around). Bu yüzden testte fark edilmesi zordur ve tam olarak bu nedenle gizli testlerin favorisidir.

### C starter'ı geldiğinde: null sonlandırıcı meselesi

Bu platformlar aynı problemi birden çok dilde sunar ve bazen ekranda seçili gelen dil C olur. Java'dan gelen biri için buradaki tek gerçek tuzak, C'de bir dizinin **nerede bittiğinin dizinin kendisinde yazmıyor olmasıdır**.

```c
char* solve(int N, char* ch) {
    char* result = (char*) malloc(sizeof(char) * (N + 1));
    for (int i = 0; i < N; i++) {
        result[i] = ch[i];
    }
    result[N] = '\0';
    return result;
}
```

Gelen `ch` yalnızca N bayttır ve **null-terminated değildir**. Doğrudan `return ch;` yazarsan, çağıran taraftaki `printf("%s", ...)` dizinin sonunu bulamaz; N'inci bayttan sonra bellekte ne varsa okumaya devam eder ve rastgele bir `'\0'` bulana kadar çöp basar. Bu bir çökme değil, **tanımsız davranıştır** (undefined behavior) — bazen doğru görünür, bazen çöp basar, bazen segmentasyon hatası verir. Bu yüzden N değil **N+1** bayt ayrılır ve son bayta elle `'\0'` konur.

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="C dizisinde null sonlandirici ile Java String uzunluk alani karsilastirmasi">
  <defs>
    <marker id="kodtest-mem-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dizinin sonunu kim biliyor? (N = 4, "omar")</text>
  <text x="30" y="48" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">C — gelen char* ch: yalnızca 4 bayt, sonlandırıcı yok</text>
  <rect x="30" y="56" width="44" height="34" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="52" y="79" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">o</text>
  <rect x="76" y="56" width="44" height="34" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="98" y="79" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">m</text>
  <rect x="122" y="56" width="44" height="34" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="144" y="79" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">a</text>
  <rect x="168" y="56" width="44" height="34" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="190" y="79" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">r</text>
  <rect x="214" y="56" width="110" height="34" fill="#F2F5F7" stroke="#B23B3B" stroke-width="1.3" stroke-dasharray="4 3"/>
  <text x="269" y="78" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">?? bellek çöpü</text>
  <path d="M328 73 L392 73" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#kodtest-mem-arw)"/>
  <text x="400" y="70" font-size="10.5" fill="#B23B3B" font-family="system-ui">printf("%s") nerede duracağını bilemez</text>
  <text x="400" y="85" font-size="10" fill="#5D7789" font-family="system-ui">tanımsız davranış: çöp, bazen çökme</text>
  <text x="30" y="136" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">C — malloc(N+1) ve result[N] = '\0'</text>
  <rect x="30" y="144" width="44" height="34" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="52" y="167" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">o</text>
  <rect x="76" y="144" width="44" height="34" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="98" y="167" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">m</text>
  <rect x="122" y="144" width="44" height="34" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="144" y="167" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">a</text>
  <rect x="168" y="144" width="44" height="34" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="190" y="167" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">r</text>
  <rect x="214" y="144" width="44" height="34" fill="#25804C" stroke="#25804C" stroke-width="1.3"/>
  <text x="236" y="167" text-anchor="middle" font-size="11.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">\0</text>
  <path d="M262 161 L392 161" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#kodtest-mem-arw)"/>
  <text x="400" y="158" font-size="10.5" fill="#25804C" font-family="system-ui">sonlandırıcı görülür, çıktı doğru basılır</text>
  <text x="400" y="173" font-size="10" fill="#5D7789" font-family="system-ui">uzunluk bilgisi verinin İÇİNDE taşınır</text>
  <text x="30" y="224" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Java — String: içerik ayrı, uzunluk ayrı alanda</text>
  <rect x="30" y="232" width="44" height="34" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="52" y="255" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">o</text>
  <rect x="76" y="232" width="44" height="34" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="98" y="255" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">m</text>
  <rect x="122" y="232" width="44" height="34" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="144" y="255" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">a</text>
  <rect x="168" y="232" width="44" height="34" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="190" y="255" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">r</text>
  <rect x="240" y="232" width="130" height="34" rx="6" fill="#FFFFFF" stroke="#186399" stroke-width="1.4"/>
  <text x="305" y="254" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">length = 4</text>
  <text x="390" y="248" font-size="10.5" fill="#186399" font-family="system-ui">uzunluk nesnenin AYRI bir alanıdır</text>
  <text x="390" y="263" font-size="10" fill="#5D7789" font-family="system-ui">sonlandırıcıya gerek yok, taşma riski yok</text>
  <rect x="26" y="288" width="608" height="30" rx="6" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.3"/>
  <text x="330" y="308" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">C: sonu veri içindeki işaret belirler · Java: sonu nesnenin alanı belirler</text>
</svg>
```

Java'da bu derdin hiç olmaması tesadüf değildir: `String` içeriğin yanında **uzunluğu ayrı bir alanda** tutar, bu yüzden içerikte "burada bitiyor" diyen özel bir bayta ihtiyaç duymaz. Aynı sebeple Java'da bir string'in içinde `'\0'` karakteri geçebilir ve string orada kesilmez; C'de kesilir. Bu karşılaştırma, `length()` çağrısının neden O(1) olduğunu ve C'deki `strlen`'in neden O(n) olduğunu da açıklar — biri okunmuş bir alandır, diğeri her seferinde dizinin sonunu arar.

Küçük bir dürüstlük notu: bu platformlarda `malloc` ile ayrılan bellek `free` edilmez, çünkü program hemen biter ve işletim sistemi belleği geri alır. Üretim kodunda bu kabul edilemez; orada belleğin sahibinin kim olduğu ve kimin serbest bırakacağı sözleşmenin parçasıdır.

### String birleştirmede refleks: `StringBuilder`

Çıktı üretirken döngü içinde `sonuc = sonuc + karakter;` yazmak çalışır ama yanlıştır. `String` değişmezdir (immutable, `05`); her `+` işlemi yeni bir nesne yaratıp eskinin içeriğini kopyalar. Bu, N uzunluğunda bir metni kurmayı O(n²) yapar. `StringBuilder` ise değiştirilebilir bir tampon üzerinde çalışır ve aynı işi O(n)'de bitirir (`06`).

```java
StringBuilder sb = new StringBuilder(n);
for (char c : ch) {
    sb.append(c);
}
return sb.toString();
```

N = 10 iken bu farkın ölçülebilir bir karşılığı yoktur; ama bu testleri bazen bir insan da okur ve döngü içindeki `+=` gördüğü anda bir not düşer. Doğru alışkanlık, küçük girdide de doğru olanı yazmaktır.

### Mülakatta ne sorulur?

- **`Scanner` mi `BufferedReader` mı, neden?** Beklenen cevap "BufferedReader hızlıdır" değil, **nedenidir**: `Scanner` token başına regex tabanlı ayrıştırma yapar ve tamponlaması zayıftır; büyük girdide bu fark zaman aşımına yeter. Küçük girdide `Scanner` tercih edilebilir çünkü kod daha kısa ve okunaklıdır.
- **`nextInt()` sonrası `nextLine()` neden boş döner?** Token okuyan metotlar satır sonu karakterini tüketmez; imleç `'\n'`ın hemen öncesinde kalır. Bu klasik sorunun cevabında "bir kez fazladan `nextLine()` çağır ya da okuma biçimini karıştırma" demek yeterlidir.
- **`N ≤ 10⁵` gördüğünde ne düşünürsün?** Beklenen refleks: "O(n log n) tavanım var, iç içe döngü kuramam, girdi okumayı da hızlandırmam gerek." Bu cevap, adayın daha önce gerçekten böyle bir test çözdüğünü gösterir.
- **Kodun örnek girdiyi geçiyor, neden hâlâ puan alamıyorsun?** Gizli testler; en büyük N, N=1, negatifler, taşma. Bu soruyu "test etmedim" diye cevaplamak değil, kontrol listesini saymak beklenir.
- **`int` taşması neden fark edilmez?** Java `int` aritmetiğinde taşma istisna fırlatmaz, sessizce sarmalanır. Birikimli toplamlarda `long` kullanmak varsayılan davranış olmalıdır (`02`).
- **Starter code'daki imzayı değiştirebilir misin?** Hayır. İmza, görünmeyen test koşucusunun çağırdığı sözleşmedir; değiştirmek derleme hatasıdır. `import` eklemek serbesttir.
- **Bir string'in uzunluğunu bulmak Java'da neden O(1), C'de neden O(n)?** Java uzunluğu ayrı alanda tutar; C'de uzunluk verinin içinde bir `'\0'` işaretiyle kodlandığı için taranarak bulunur.

### Tek cümlede

Online kodlama testi algoritma zekânı değil disiplinini ölçer: girdiyi doğru oku, kısıttan karmaşıklık tavanını çıkar, sınır durumlarını ve taşmayı düşün, sana verilen imzaya dokunma.

Çapraz referans: String temelleri `05` · StringBuilder `06` · veri tipleri ve taşma `02` · collections `19` · JDK/JRE/JVM `01`.

# kodtest-1 | Temel
## Soru
Bir adayın kodu şu şekilde başlıyor: `int n = sc.nextInt();` ardından `String satir = sc.nextLine();`. Girdi dosyasının ilk satırı `3`, ikinci satırı `o m a r`. Adayın gözlemi: `satir` değişkeni boş string oluyor. Bunun sebebi nedir?
## A
`nextInt()` çağrıldığında `Scanner` tüm girdiyi tüketir, geriye okunacak veri kalmaz
🤖 `Scanner` tüm girdiyi tüketmez; yalnız istenen token'ı okur, gerisi tamponda durur.
## B
İkinci satır boşluk içerdiği için `nextLine()` onu okuyamaz, boş string döner
🤖 `nextLine()` boşluklu satırları sorunsuz okur; sorun satırın kendisinde değil imlecin konumunda.
## C
`nextInt()` sayıyı okur ama satır sonu karakterini tüketmez; `nextLine()` o satırın kalanını okuyup boş string döner
🤖 Doğru: imleç `'\n'`ın önünde kaldığı için `nextLine()` boş kalanı okur.
## D
`Scanner` ile `nextInt()` ve `nextLine()` aynı programda kullanılamaz, derleme hatası verir
🤖 İkisi birlikte kullanılabilir ve derlenir; mesele derleme değil çalışma-zamanı davranışıdır.
## Doğru
C
## Açıklama
`nextInt()`, `nextDouble()`, `next()` gibi metotlar **token** okur: istenen değeri alır ve okumayı token'ın bittiği yerde bırakır. Girdideki `3\n` satırında `3` okunur, `'\n'` tamponda kalır. Hemen ardından çağrılan `nextLine()` "imleçten satır sonuna kadar oku" işini yapar ve arada hiçbir karakter kalmadığı için **boş string** döner. Çözüm ya `nextInt()` sonrasında bir kez fazladan `sc.nextLine()` çağırıp satır sonunu yutmak, ya da baştan sona tek bir okuma biçiminde kalmaktır — ya hep token, ya hep satır. A yanlıştır çünkü `Scanner` girdiyi topluca tüketmez, talep üzerine okur. B cazip görünür çünkü ikinci satırda gerçekten boşluk vardır, ama `nextLine()` boşluklu satırı okumakta hiç zorlanmaz; sorun satırın içeriğiyle değil imlecin nerede durduğuyla ilgilidir. D ise yanlış bir yasak uydurur: iki metot birlikte kullanılabilir, yalnızca bu tuzağı bilerek kullanılmalıdır.

# kodtest-2 | Temel
## Soru
Platform şu iskeleti veriyor: `public class Main { static String solve(int n, List<Character> ch) { ... } public static void main(String[] args) { ... } }`. Aday `solve` gövdesini doldurmak yerine dönüş tipini `void` yapıyor, sonucu fonksiyonun içinde `System.out.println` ile yazdırıyor ve sınıf adını `Cozum` olarak değiştiriyor. Sonuç ne olur?
## A
Sorun çıkmaz; çıktı doğru olduğu sürece platform sınıf adına ve imzaya bakmaz
🤖 Platform imzayı adıyla ve tipiyle çağırır; sınıf adı ve imza sözleşmenin parçasıdır.
## B
Kod derlenmez ve puan alınamaz; sınıf adı ile fonksiyon imzası platformun sözleşmesidir, değiştirilemez
🤖 Doğru: görünmeyen test koşucusu tam olarak o adı ve o imzayı çağırır.
## C
Kod derlenir ama yalnızca gizli testlerde başarısız olur; örnek girdi doğru geçer
🤖 Sorun çalışma-zamanında değil derleme aşamasında ortaya çıkar; örnek girdi bile çalıştırılamaz.
## D
Yalnızca sınıf adı sorun olur; dönüş tipini değiştirmek serbesttir çünkü çıktı yine doğru basılır
🤖 Dönüş tipi de imzanın parçasıdır; `void` yapılırsa çağıran taraf sonucu alamaz.
## Doğru
B
## Açıklama
Starter code bir taslak değil **sözleşmedir**. Platformun senin görmediğin bir test koşucusu (harness) vardır ve o koşucu `solve` fonksiyonunu tam olarak verilen adla, verilen parametre tipleriyle ve verilen dönüş tipiyle çağırır; `main` da genellikle girdiyi okuyup `solve`'un döndürdüğünü yazdıracak biçimde yazılmıştır. Dönüş tipini `void` yapmak çağıran tarafta "beklenen değer gelmiyor" derleme hatası doğurur; sınıf adını `Cozum` yapmak ise dosya adı `Main.java` olduğu için "public class Cozum should be declared in a file named Cozum.java" hatası verir. A, "çıktı doğruysa yeter" varsayımıyla cazip gelir ama bu ancak serbest formatlı sorularda geçerlidir, imza verildiğinde değil. C yanlış katmanı işaret eder: hata çalışma-zamanında değil derleme anında oluşur, dolayısıyla örnek girdi bile çalıştırılamaz. D kısmen doğruyu söyler ama eksiktir — dönüş tipi de imzanın parçasıdır. Serbest olan tek şey eksik `import` satırlarını eklemektir; o imzayı bozmaz.

# kodtest-3 | Orta
## Soru
Bir problemin kısıt satırı `1 ≤ N ≤ 200000` diyor ve her satırda bir tam sayı veriliyor. Aday `Scanner` ile okuyup değerleri bir `ArrayList`'e atıyor, sonra iki iç içe döngüyle her elemanı diğerleriyle karşılaştırıyor. Örnek girdiyi doğru geçiyor ama gönderimde çoğu test "Time Limit Exceeded" veriyor. Doğru teşhis hangisidir?
## A
Tek sorun `Scanner`'dır; `BufferedReader`'a geçilirse iç içe döngü olduğu gibi kalabilir, çünkü darboğaz hesaplama değil girdinin okunma hızıdır
🤖 Okuma hızı gerçek bir sorun ama tek başına yeterli değil; asıl darboğaz O(n²) döngüdür.
## B
`ArrayList` yerine dizi kullanmak yeterlidir; kutulama maliyeti kaldırılınca süre limite girer ve bellek kullanımı da belirgin biçimde düşer
🤖 Kutulama sabit çarpanı düşürür, karmaşıklık sınıfını değiştirmez; 4×10¹⁰ işlem yine sığmaz.
## C
Sorun yalnızca örnek girdinin küçük olmasıdır; algoritma doğru olduğu için kısıt değişince kendiliğinden geçer, doğruluk sağlandığında hız ikincil bir ayrıntıdır
🤖 Algoritmanın doğru olması yetmez; kısıt altında yeterince hızlı olması ayrı bir gerekliliktir.
## D
N ≈ 2×10⁵ için O(n²) yaklaşık 4×10¹⁰ işlem demektir; algoritma O(n log n) veya O(n)'e indirilmeli, ayrıca okuma `BufferedReader` ile hızlandırılmalıdır
🤖 Doğru: önce karmaşıklık sınıfı düşürülür, sonra okuma maliyeti azaltılır.
## Doğru
D
## Açıklama
Kısıt satırı burada teşhisin tamamını veriyor. N ≈ 2×10⁵ için iç içe iki döngü N² ≈ 4×10¹⁰ mertebesinde iş demektir (yalnız üst üçgen taranırsa N²/2 ≈ 2×10¹⁰; sonucu değiştirmez); tipik bir platformun bir saniyede yapabildiği iş ~10⁸ işlem mertebesinde olduğuna göre bu, limitin yüzlerce katıdır. Bu yüzden çözüm önce **karmaşıklık sınıfını** düşürmekten geçer: sıralama (O(n log n)), `HashMap`/`HashSet` ile tek geçişte sayım (O(n)) ya da iki işaretçi gibi bir teknik. İkinci sırada okuma maliyeti gelir — N ≥ 10⁵ ölçeğinde `Scanner`'ın token başına regex tabanlı ayrıştırması ölçülebilir bir yük yaratır, `BufferedReader` + `split()`/`StringTokenizer` doğru tercihtir. A ve B aynı hatayı farklı biçimde yapar: ikisi de **sabit çarpanı** iyileştirir ama 10¹⁰ mertebesindeki bir işi 10⁸'e indirmez; sabit çarpan oynamaları ancak karmaşıklık doğruyken işe yarar. C ise en tehlikeli düşünce biçimidir — "doğru" ile "yeterince hızlı" ayrı gerekliliklerdir ve bu testlerde ikisi de puanlanır.

# kodtest-4 | Orta
## Soru
Kısıt: `1 ≤ N ≤ 100000` ve `-1000000000 ≤ a[i] ≤ 1000000000`. İstenen, dizideki tüm sayıların toplamını yazdırmak. Aday `int toplam = 0;` ile başlayıp döngüde `toplam += a[i];` yazıyor. Kod derleniyor, örnek girdiyi geçiyor, bazı gizli testlerde yanlış cevap alıyor. Sebep nedir?
## A
Toplam en fazla 10¹⁴ olabilir; bu `int` sınırını aşar ve taşma sessizce yanlış sonuç üretir, birikimli değer `long` tutulmalıdır
🤖 Doğru: elemanlar `int`'e sığsa da toplamları sığmaz; Java'da taşma istisna fırlatmaz.
## B
Negatif değerler `int` içinde saklanamaz; `a[i]` dizisi de `long` olmalıdır
🤖 `int` negatifleri sorunsuz saklar; sorun elemanlarda değil birikimli toplamdadır.
## C
`toplam += a[i];` ifadesi Java'da örtük daraltma yaptığı için hatalıdır, `toplam = toplam + a[i];` yazılmalıdır
🤖 İki yazım da aynı sonucu üretir; bileşik atamanın örtük dönüşümü burada devrede değildir.
## D
Taşma olduğunda JVM `ArithmeticException` fırlatır; aday bu istisnayı yakalamadığı için testler başarısız olur
🤖 Java `int` taşmasında istisna fırlatmaz, sessizce sarmalanır — asıl tehlike de budur.
## Doğru
A
## Açıklama
Elemanların her biri `int` aralığına (yaklaşık ±2,1×10⁹) sığar, dolayısıyla dizinin tipi doğrudur. Sorun **birikimli** değerdedir: 10⁵ eleman × 10⁹ üst sınır = 10¹⁴ mertebesinde bir toplam, `int`'in tavanının yaklaşık elli bin katıdır. Java'da `int` aritmetiği taştığında hiçbir uyarı vermez; ikiye tümleyen gösterimde sarmalanır ve sonuç çoğu zaman negatif, her zaman yanlış bir sayı olur. Bu yüzden kural nettir: **elemanlar `int`'e sığıyor diye toplamları da sığar sanma; birikimli değerleri `long` tut** (`02`). B, negatif değerlerin dikkat çekmesinden faydalanan bir çeldiricidir — `int` negatifleri gayet iyi saklar, kısıt zaten `int` aralığındadır. C teknik bir ayrıntıyı yanlış yere taşır: bileşik atama operatörünün örtük dönüşüm kuralı vardır ama burada iki taraf da `int` olduğu için devreye girmez, iki yazım denktir. D ise en cazip yanlıştır çünkü "taşma olursa program bunu bize söyler" beklentisi sezgiseldir; oysa taşmanın sessiz olması tam olarak bu hatayı gizli testlerin favorisi yapan şeydir.

# kodtest-5 | İleri
## Soru
Bir aday çıktıyı şöyle üretiyor: 100.000 satırlık sonucu bir döngüde `sonuc = sonuc + satir + "\n";` biçiminde bir `String` değişkende biriktiriyor, sonra tek seferde yazdırıyor. Algoritması O(n) ve girdiyi `BufferedReader` ile okuyor. Yine de zaman limitine takılıyor. En doğru açıklama hangisidir?
## A
`BufferedReader` yalnızca okumayı hızlandırır; çıktı için de `BufferedWriter` kullanmadıkça hiçbir iyileştirme işe yaramaz
🤖 Çıktı tamponlaması faydalıdır ama buradaki asıl darboğaz string birleştirmenin karesel maliyetidir.
## B
Sorun `"\n"` kullanımıdır; platformlar `System.lineSeparator()` beklediği için karakter uyuşmazlığı gecikme yaratır
🤖 Satır sonu karakteri bir doğruluk ayrıntısıdır, performans darboğazı değildir.
## C
`String` değişmez olduğu için her `+` yeni nesne yaratıp mevcut içeriği kopyalar; birikim O(n²) olur, `StringBuilder` ile O(n)'e iner
🤖 Doğru: kopyalama maliyeti biriktikçe büyür, karmaşıklık kareselleşir.
## D
Algoritma O(n) olduğu için darboğaz kodda olamaz; sorun JVM'in başlangıç (startup) süresidir
🤖 JVM başlangıcı sabit ve küçük bir maliyettir; girdi büyüdükçe büyüyen bir yükü açıklamaz.
## Doğru
C
## Açıklama
`String` Java'da değişmezdir (`05`): `sonuc + satir` ifadesi mevcut içeriği okuyup yeni bir nesneye kopyalar. Döngünün k'ıncı adımında kopyalanan karakter sayısı o ana kadar birikmiş uzunluk kadardır, dolayısıyla toplam iş 1+2+3+…+n mertebesinde, yani **O(n²)** olur. 100.000 satırlık bir çıktıda bu, milyarlarca karakter kopyalaması demektir — algoritma O(n) olsa bile program limitte kalamaz. Doğru çözüm `StringBuilder` ile değiştirilebilir bir tampon üzerinde biriktirip sonunda tek `toString()` almaktır (`06`); bu, işi O(n)'e indirir. A doğru yönü gösterir ama sırayı karıştırır: `BufferedWriter`/tek seferde yazdırma faydalıdır, ancak burada zaten tek seferde yazdırılıyor, darboğaz birleştirmededir. B, satır sonu karakteri ayrıntısını performans meselesi gibi sunar — bu bir doğruluk konusudur, hız konusu değildir. D ise "algoritmam O(n), o halde kod suçsuz" yanılgısıdır; karmaşıklık analizi yalnız ana döngüyü değil, o döngü içinde çağrılan işlemlerin maliyetini de kapsamak zorundadır.

# kodtest-6 | İleri
## Soru
Platform C starter'ı veriyor: `char* solve(int N, char* ch)`. Gelen `ch` dizisi tam olarak N bayttır ve çağıran taraf sonucu `printf("%s", solve(N, ch));` ile yazdırmaktadır. Aday fonksiyonun içinde doğrudan `return ch;` yazıyor. Ne olur ve doğrusu nedir?
## A
Hiçbir şey olmaz; `printf("%s")` dizinin uzunluğunu bildiği için N bayt basar, `return ch;` en verimli çözümdür ve gereksiz kopyalamadan da kaçınılmış olur
🤖 `printf` uzunluğu bilmez; C'de dizi kendi uzunluğunu taşımaz.
## B
Gelen dizi null-terminated olmadığı için `printf` nerede duracağını bilemez ve tanımsız davranış oluşur; `malloc(N+1)` ile bir bayt fazla ayırıp sona `'\0'` konmalıdır
🤖 Doğru: C'de bir string'in sonunu veri içindeki `'\0'` işareti belirler.
## C
Fonksiyon `ch`'nin kendisini döndürdüğü için çağıran tarafta bellek iki kez serbest bırakılır ve program çöker; çözüm dönüş değerini hiç kullanmamaktır
🤖 Buradaki sorun çifte serbest bırakma değil, sonlandırıcı eksikliğidir; platformda `free` zaten çağrılmaz.
## D
`ch` yerel bir değişken olduğu için fonksiyon bitince yok olur; çözüm diziyi `static char*` olarak tanımlayıp öyle döndürmek, böylece ömrü fonksiyondan uzun olur
🤖 `ch` işaretçi parametresidir, gösterdiği bellek çağıran tarafa aittir ve fonksiyon bitince kaybolmaz.
## Doğru
B
## Açıklama
C'de bir karakter dizisinin nerede bittiği dizinin kendisinde yazmaz; sonu, verinin **içine** konan `'\0'` baytı belirler. Starter'ın verdiği `ch` tam olarak N bayttır ve sonlandırıcı içermez, dolayısıyla `return ch;` yapıldığında `printf("%s", ...)` N'inci bayttan sonra okumaya devam eder ve rastgele bir sıfır bayt bulana kadar bellekteki çöpü basar. Bu bir çökme garantisi değil **tanımsız davranıştır**: bazen doğru görünür, bazen çöp basar, bazen segmentasyon hatası verir — testlerde en can sıkıcı hata sınıfı budur. Doğrusu `malloc(sizeof(char) * (N + 1))` ile bir bayt fazla ayırmak, N karakteri kopyalamak ve `result[N] = '\0'` koymaktır. A, Java alışkanlığının doğrudan yansımasıdır: Java'da `String` uzunluğu ayrı bir alanda tuttuğu için sonlandırıcıya gerek yoktur, C'de böyle bir alan yoktur. C, bellek yönetimini doğru sezer ama yanlış hataya bağlar; bu platformlarda `free` zaten çağrılmaz ve sorun sahiplikle değil sonlandırıcıyla ilgilidir. D, "yerel değişken fonksiyon bitince yok olur" kuralını yanlış nesneye uygular — `ch` bir parametredir ve gösterdiği bellek çağıran tarafa aittir; yok olan şey işaretçinin kendisidir, gösterdiği alan değil.
