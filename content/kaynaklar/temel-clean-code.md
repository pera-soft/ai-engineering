---
baslik: "Temeller Tazeleme: Clean Code Disiplini"
yazar: "Faz 0 — Mühendislik Temelleri"
tarih: "2026-07-10"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 4,5
---

### Bu metin ne anlatıyor?
Bu metin, çalışan kodu **başkasının da okuyabileceği** kod hâline getiren alışkanlıkları
anlatıyor. Bir program yalnızca doğru sonuç verdiği için iyi sayılmaz; altı ay sonra ona
dönen kişi (çoğu zaman sen olursun) ne yaptığını anlayabilmelidir. Metin dört konuyu
sırayla ele alıyor: temiz kodun üç ölçütü, fonksiyonları küçük tutmak, isim seçmek ve
yorum yazmak. Sonunda da kodu davranışını bozmadan düzeltmenin adı olan "refactoring"
kısaca tanımlanıyor.

## Neden bu özet
Kod, yapay zekâ ajanları tarafından yazılsa bile insan tarafından **okunacak, doğrulanacak
ve değiştirilecek**. Değişime hazır kod, temiz koddur (clean code). Bu özet, ileride
mimari kararlar alırken (Faz 4-5) altında sağlam bir zemin kalsın diye temel
disiplinleri tazeler: kodun sade, doğru ve anlaşılır kalmasını sağlayan alışkanlıklar.
Amaç ezber değil; bir değişiklik geldiğinde kodun sana direnmemesi.

## Türkçe çalışma metni

### 1. Üç direk: sadelik, doğruluk, tamlık
Temiz kod tek bir şeyle ölçülmez; üç direğin dengesidir. **Sadelik (simplicity)**:
gereğinden fazla parça yok, her satırın bir sebebi var. **Doğruluk (correctness)**:
kod gerçekten istenen işi yapar, kenar durumları (edge cases) dahil. **Tamlık
(completeness)**: iş yarım kalmaz — hata yolları (error paths), sınır değerler ve
"olmayan veri" durumu da düşünülmüştür. Üçü aynı anda gözetilir; birini feda etmek
ötekini de zayıflatır. Aşağıdaki diyagram üç direği yan yana koyar.

```svg
<svg viewBox="0 0 640 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Clean Code üç direği: sadelik, doğruluk, tamlık kutuları ve altında tek cümle">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Temiz kodun üç direği</text>
  <text x="320" y="45" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">üçü aynı anda gözetilir · biri feda edilince öteki de zayıflar</text>
  <rect x="30" y="66" width="180" height="92" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="120" y="98" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">SADELİK</text>
  <text x="120" y="115" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">simplicity</text>
  <text x="120" y="136" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">az parça ·</text>
  <text x="120" y="151" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">her satırın sebebi</text>
  <rect x="230" y="66" width="180" height="92" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="98" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">DOĞRULUK</text>
  <text x="320" y="115" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">correctness</text>
  <text x="320" y="136" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">istenen işi yapar ·</text>
  <text x="320" y="151" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">kenar durumlar dahil</text>
  <rect x="430" y="66" width="180" height="92" rx="12" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="520" y="98" text-anchor="middle" font-size="14" font-weight="700" fill="#4F46E5" font-family="system-ui">TAMLIK</text>
  <text x="520" y="115" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">completeness</text>
  <text x="520" y="136" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">hata yolları ·</text>
  <text x="520" y="151" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sınır ve boş veri</text>
  <text x="320" y="192" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Değişime hazır kod = sade + doğru + tam.</text>
</svg>
```

### 2. Fonksiyonlar: küçük ve tek iş
Fonksiyon (function), bir işi yapan ve adı olan kod parçasıdır; başka yerlerden adıyla
çağrılır. İyi bir fonksiyon **tek bir şey yapar ve onu iyi yapar** (tek sorumluluk —
single responsibility).
Küçük olması tesadüf değil, tek işli olmasının sonucudur. Uzun, çok-işli bir
fonksiyon; okumayı zorlaştırır, test etmeyi güçleştirir ve bir yeri değiştirince
alakasız bir yeri bozar. Çözüm: her mantıksal adımı kendi adıyla anılan küçük bir
fonksiyona ayır. Böylece isim, ne yaptığını anlatır; gövde nasıl yaptığını.
Aşağıda aynı iş "önce" (tek dev fonksiyon) ve "sonra" (üç küçük fonksiyon) olarak
görülüyor — boy farkı görsel olarak da anlamlı.

```svg
<svg viewBox="0 0 640 288" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Fonksiyon önce ve sonra: solda uzun çok-işli tek fonksiyon, sağda küçük tek-işli üç fonksiyon">
  <defs>
    <marker id="cc-ok1" markerWidth="11" markerHeight="11" refX="8" refY="3.5" orient="auto"><path d="M0,0 L8,3.5 L0,7 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bir dev fonksiyon → üç küçük tek-işli fonksiyon</text>
  <text x="150" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">ÖNCE · tek fonksiyon, çok iş</text>
  <rect x="40" y="62" width="220" height="196" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="56" y="84" font-size="11" font-weight="600" fill="#C77A17" font-family="system-ui">islemiYap() {</text>
  <rect x="60" y="94" width="176" height="8" rx="2" fill="#C77A17" opacity="0.35"/>
  <rect x="60" y="108" width="184" height="8" rx="2" fill="#C77A17" opacity="0.35"/>
  <rect x="60" y="122" width="168" height="8" rx="2" fill="#C77A17" opacity="0.35"/>
  <rect x="60" y="136" width="180" height="8" rx="2" fill="#C77A17" opacity="0.35"/>
  <rect x="60" y="150" width="160" height="8" rx="2" fill="#C77A17" opacity="0.35"/>
  <rect x="60" y="164" width="184" height="8" rx="2" fill="#C77A17" opacity="0.35"/>
  <rect x="60" y="178" width="150" height="8" rx="2" fill="#C77A17" opacity="0.35"/>
  <rect x="60" y="192" width="176" height="8" rx="2" fill="#C77A17" opacity="0.35"/>
  <rect x="60" y="206" width="164" height="8" rx="2" fill="#C77A17" opacity="0.35"/>
  <rect x="60" y="220" width="182" height="8" rx="2" fill="#C77A17" opacity="0.35"/>
  <text x="56" y="248" font-size="11" font-weight="600" fill="#C77A17" font-family="system-ui">}</text>
  <line x1="266" y1="160" x2="316" y2="160" stroke="#5D7789" stroke-width="1.8" marker-end="url(#cc-ok1)"/>
  <text x="490" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">SONRA · üç fonksiyon, tek iş</text>
  <rect x="330" y="62" width="270" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="346" y="84" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">girdiyiDogrula()</text>
  <rect x="350" y="94" width="150" height="7" rx="2" fill="#25804C" opacity="0.35"/>
  <rect x="330" y="126" width="270" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="346" y="148" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">hesabiUygula()</text>
  <rect x="350" y="158" width="170" height="7" rx="2" fill="#25804C" opacity="0.35"/>
  <rect x="330" y="190" width="270" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="346" y="212" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">sonucuKaydet()</text>
  <rect x="350" y="222" width="140" height="7" rx="2" fill="#25804C" opacity="0.35"/>
  <text x="320" y="278" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Küçük = tek işli olmanın sonucu · her isim ne yaptığını söyler</text>
</svg>
```

### 3. İsimlendirme: niyet açık, tahmin edilebilir
İyi isim yorumun yerini alır. Bir değişken, fonksiyon ya da sınıf adı **niyeti**
söylemeli: neyi tutuyor, ne döndürüyor, ne zaman kullanılır. `d`, `tmp`, `data2`
gibi adlar okuyucuyu tahmine zorlar; `kalanGunSayisi`, `aktifKullanicilar`,
`fiyatiKdvDahilHesapla` gibi adlar cümle gibi okunur. İkinci kural
**tahmin edilebilirlik (predictability)**: aynı kavram her yerde aynı adla,
benzer işler benzer kalıpla adlandırılır. Böylece okuyucu bir yeri öğrenince
gerisini de öğrenmiş olur. İsim iyi olduğunda çoğu yorum gereksizleşir.

### 4. Yorum yerine anlaşılır kod
Yorum (comment), kodun içine yazılan ve programın çalışmasına karışmayan açıklama
satırıdır. Yorum bir borçtur: kod değişir, yorum güncellenmezse **yalan söylemeye** başlar.
İlk tercih kodun kendini anlatmasıdır — açık isimler, küçük fonksiyonlar,
sihirli sayılar yerine adlandırılmış sabitler. **Sihirli sayı (magic number)**, kodun
ortasında açıklamasız duran çıplak bir değerdir (`if (gun > 30)`); onu
`const int AZAMI_KIRA_GUNU = 30;` gibi adı olan bir sabite çevirirsen sayı kendini
anlatır. Yorumu, kodun söyleyemediği şey için
sakla: **neden** böyle yapıldığı (bir iş kuralı, bir dış kısıt, bir bilinçli takas).
"Ne yaptığını" anlatan yorum, genellikle kötü isimlendirmenin üstünü örten bir
yamadır. Aşağıdaki karşılaştırma bunu gösteriyor: solda kötü isim + açıklayıcı
yorum, sağda niyeti kendi söyleyen kod.

```svg
<svg viewBox="0 0 640 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İyi isim daha az yorum: solda kötü isimli koda eklenen açıklama yorumu, sağda niyeti açık isimli kod">
  <defs>
    <marker id="cc-ok2" markerWidth="11" markerHeight="11" refX="8" refY="3.5" orient="auto"><path d="M0,0 L8,3.5 L0,7 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İyi isim → daha az yorum</text>
  <text x="150" y="50" text-anchor="middle" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">Kötü isim + yorum ile telafi</text>
  <rect x="30" y="62" width="250" height="128" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="46" y="90" font-size="11" fill="#5D7789" font-family="system-ui">// kalan gün sayısını hesapla</text>
  <text x="46" y="112" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">int d = h(x, y);</text>
  <text x="46" y="140" font-size="11" fill="#5D7789" font-family="system-ui">// negatifse süre dolmuş demek</text>
  <text x="46" y="162" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">if (d &lt; 0) uyar();</text>
  <text x="46" y="182" font-size="10.5" fill="#C77A17" font-family="system-ui">yorum kodla birlikte eskirse yalan söyler</text>
  <line x1="286" y1="126" x2="336" y2="126" stroke="#25804C" stroke-width="1.8" marker-end="url(#cc-ok2)"/>
  <text x="490" y="50" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Niyeti kendi söyleyen kod</text>
  <rect x="360" y="62" width="250" height="128" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="376" y="94" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">int kalanGun =</text>
  <text x="376" y="114" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">  gunFarki(bugun, sonTarih);</text>
  <text x="376" y="146" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">if (sureDoldu(kalanGun))</text>
  <text x="376" y="166" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">  kullaniciyiUyar();</text>
  <text x="376" y="184" font-size="10.5" fill="#25804C" font-family="system-ui">isim niyeti söyler · yoruma gerek kalmaz</text>
  <text x="320" y="220" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Yorumu "ne" için değil, "neden" için sakla.</text>
</svg>
```

**Şerh:** "Yorum kötüdür, iyi kod yorum gerektirmez" mutlak bir kural değildir; abartıldığında
zarar verir. Bir düzenli ifadenin (regex) neden o biçimde yazıldığı, bir dış sistemin tuhaf
davranışına karşı konmuş geçici çözüm, yasal/iş kaynaklı bir sınır ya da bir kütüphanenin
dışarıya açık işlevlerinin belgesi — bunlar kodun kendi başına söyleyemeyeceği şeylerdir ve
yorumla yazılır. Kural şudur: **"ne yapıyor" yorumunu isimle çöz, "neden böyle" yorumunu yaz.**

### 5. Yeniden düzenleme (refactoring) nedir?
**Refactoring**, kodun **dışarıdan görünen davranışını değiştirmeden** iç yapısını
iyileştirmektir: uzun fonksiyonu bölmek, kötü bir ismi düzeltmek, tekrar eden bloğu tek yere
almak. Tanımın kritik yarısı "davranış değişmez" kısmıdır — davranışı da değiştiriyorsan o
artık refactoring değil, yeni özellik ya da hata düzeltmesidir; ikisini aynı anda yapmak
hatanın kaynağını bulmayı zorlaştırır.

Refactoring'in ön koşulu testtir: davranışın aynı kaldığını sana ancak testler söyler. Bu
yüzden temiz kod ile test disiplini birbirine bağlıdır (bkz. Developer Testing metni). Pratik
sıra: önce testler yeşil olsun → küçük bir düzenleme yap → testleri tekrar çalıştır → devam et.

## Öz-sınav (kendini test et)
1. Temiz kodun üç direği neydi ve biri feda edilince ne olur?
2. "Fonksiyon küçük olsun" kuralı aslında hangi daha temel kuralın sonucudur?
3. Bir değişken adının "niyeti söylemesi" ne demek? `data2` neden kötü bir addır?
4. Bir yorumu ne zaman yazmalı, ne zaman yerine kodu düzeltmeli? "Ne" mi "neden" mi?
5. Elindeki 40 satırlık bir fonksiyonu tek işli parçalara bölerken hangi soruyu her adımda sorarsın?
6. Refactoring'in tanımındaki "davranış değişmez" şartı neden bu kadar önemli? Bir düzenleme
   sırasında yeni bir kural da eklersen ne kaybedersin?
