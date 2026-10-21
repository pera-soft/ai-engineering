---
baslik: "Hafta 7 Görsel Özet: AI Engineering — Stack, Model Seçimi, Prompt"
yazar: "AI Engineering (Huyen, O'Reilly 2025) — kavram özeti"
tarih: "2026-07-11"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 7
---

### Bu metin ne anlatıyor?
Bugün yapay zekâ uygulaması yazmak, eskisi gibi "kendi modelini eğitmek" değil; başkasının
eğittiği hazır bir modeli alıp kendi işine uydurmak demek. Bu metin üç soruya cevap veriyor:
bu değişim tam olarak neyi değiştirdi, hazır modeller arasından hangisini nasıl seçersin ve
modele verdiğin metni (prompt) nasıl ciddiye alırsın. Önce hiç bilmeyen biri için kısa bir
sözlük geliyor; sonra üç bölüm.

### Bu metinde geçen temel terimler
Metnin geri kalanı bunların bilindiğini varsayıyor; hiçbiri için ön bilgi gerekmiyor.

- **Model:** Veriden örüntü öğrenmiş bir program. Kendisine bir girdi verirsin, bir çıktı üretir.
- **LLM (Large Language Model / büyük dil modeli):** Çok büyük miktarda metinle eğitilmiş,
  kendisine verilen metnin **devamını tahmin ederek** çalışan model. Sohbet, özet, kod yazma
  gibi işleri hep bu tek yetenekten türetir. "Bilir" değil, "en olası devamı üretir" —
  bu ayrım metnin geri kalanında önemli olacak.
- **Foundation model (temel model):** Belli bir işe değil, geniş ve genel bir veriyle eğitilmiş;
  üstüne pek çok farklı uygulama kurulabilen büyük model. Claude ve GPT ailesi böyledir.
- **Token (jeton):** Modelin metni işlerken kullandığı en küçük parça. Genellikle bir kelimenin
  bir bölümüdür; kabaca Türkçe bir kelime 2–3 token eder. Modeller **token başına** ücretlendirilir
  ve token başına hesap yapar — yani "uzunluk" doğrudan "para" demektir.
- **Bağlam penceresi (context window):** Modelin *tek seferde* görebildiği en fazla token sayısı.
  Masanın büyüklüğü gibi: masaya sığmayan kâğıt yok sayılır. Pencere dolduğunda en eski kısım
  düşer ya da istek reddedilir. Modelden modele değişir ve zamanla büyümektedir.
- **Prompt (istem):** Modele verdiğin metin — talimat, örnek ve varsa ilgili bilgi. Programın
  "girdisi" budur.
- **Temperature (sıcaklık):** Çıktının ne kadar çeşitli olacağını ayarlayan sayı. Düşük değer
  (ör. 0–0,2) en olası kelimeyi seçtirir: tekrar edilebilir, kararlı, sıkıcı. Yüksek değer
  (ör. 0,8–1) daha az olası seçenekleri de masaya koyar: çeşitli, yaratıcı, daha kaygan.
  Sınıflandırma ve veri çıkarma gibi işlerde düşük, fikir üretmede yüksek tutulur.
  Dikkat: sıcaklığı 0 yapmak çıktıyı "doğru" yapmaz, sadece **daha tekrar edilebilir** yapar.
- **Değerlendirme (evaluation / kısaca eval):** Modelin senin işinde ne kadar iyi çalıştığını
  ölçen düzenek — kendi sorularından ve beklenen cevaplarından oluşan bir sınav.

## Neden bu özet
Hafta 7, Chip Huyen'in *AI Engineering* kitabının ilk yarısını işliyor: yeni yığın (stack —
bir uygulamayı ayakta tutan araç ve katman takımı), hazır temel modelleri (foundation model)
seçmek ve prompt mühendisliği. Bu üç görsel, kitabın anlattığı üç ayrımın — klasik ML'den
kopuş, model seçim dengesi ve prompt disiplini — bir çırpıda bakılacak referansıdır.

## 1. Klasik ML → AI Engineering farkı
Klasik makine öğrenmesinde (machine learning — veriden örüntü öğrenen model kurma alanı) kendi
modelini sıfırdan eğitirsin: veri topla, eğit, **deploy** et (yani modeli gerçek kullanıcıların
erişebileceği bir sunucuya kur). AI Engineering'de ise BAŞKASININ eğittiği hazır bir temel modeli
(foundation model) alıp **adapte edersin** (uydurursun). Adaptasyonun üç yolu vardır ve Hafta 8'de
ayrıntılanır: **prompt** (yalnız talimatı değiştirmek), **RAG** (Retrieval-Augmented Generation —
gerekli bilgiyi dışarıdan bulup prompt'a eklemek) ve **fine-tune** (ince ayar — hazır modeli kendi
örneklerinle bir miktar daha eğitmek). Sonra değerlendirir ve servis edersin.

Giriş bariyeri düştü; buna karşılık modeli kendin eğitmediğin için **değerlendirme (evaluation)**
işin en kritik parçası hâline geldi. Sebebi basit: modelin içinde ne olduğunu göremiyorsun, o
yüzden ancak *çıktısına bakarak* iyi olup olmadığını anlayabilirsin.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Klasik ML akışı veri topla eğit deploy ile AI Engineering akışı hazır model adapte değerlendir servis karşılaştırması">
  <defs>
    <marker id="h7ok1" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="h7ok2" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Klasik ML vs AI Engineering</text>
  <text x="30" y="50" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Klasik ML — kendi modelini eğit</text>
  <rect x="30" y="58" width="150" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="105" y="80" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Veri topla</text>
  <text x="105" y="96" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(etiketli veri)</text>
  <rect x="245" y="58" width="150" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="80" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Model eğit</text>
  <text x="320" y="96" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(sıfırdan)</text>
  <rect x="460" y="58" width="150" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="535" y="80" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Deploy</text>
  <text x="535" y="96" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(servis et)</text>
  <line x1="182" y1="81" x2="243" y2="81" stroke="#186399" stroke-width="1.8" marker-end="url(#h7ok1)"/>
  <line x1="397" y1="81" x2="458" y2="81" stroke="#186399" stroke-width="1.8" marker-end="url(#h7ok1)"/>
  <text x="30" y="132" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">AI Engineering — hazır modeli adapte et</text>
  <rect x="30" y="140" width="118" height="46" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="89" y="162" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Hazır model</text>
  <text x="89" y="178" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">(foundation)</text>
  <rect x="184" y="140" width="118" height="46" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="243" y="162" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Adapte et</text>
  <text x="243" y="178" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">prompt·RAG·tune</text>
  <rect x="338" y="140" width="118" height="46" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="397" y="162" text-anchor="middle" font-size="11.5" font-weight="700" fill="#C77A17" font-family="system-ui">Değerlendir</text>
  <text x="397" y="178" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">(eval — kritik)</text>
  <rect x="492" y="140" width="118" height="46" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="551" y="162" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Servis et</text>
  <text x="551" y="178" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">(deploy)</text>
  <line x1="150" y1="163" x2="182" y2="163" stroke="#25804C" stroke-width="1.8" marker-end="url(#h7ok2)"/>
  <line x1="304" y1="163" x2="336" y2="163" stroke="#25804C" stroke-width="1.8" marker-end="url(#h7ok2)"/>
  <line x1="458" y1="163" x2="490" y2="163" stroke="#25804C" stroke-width="1.8" marker-end="url(#h7ok2)"/>
  <text x="320" y="216" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Giriş bariyeri düştü: modeli sen eğitmiyorsun. Bunun bedeli —</text>
  <text x="320" y="232" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">değerlendirme (evaluation) artık işin en kritik adımı.</text>
</svg>
```

## 2. Foundation model seçimi
İki büyük yol var:

- **Kapalı-API model** (ör. Claude, GPT): Modele internet üzerinden bir çağrıyla erişirsin
  (**API** = Application Programming Interface, programların birbirine "şunu yap" demesini
  sağlayan tanımlı arayüz). Hazırdır, bakımı sende değildir; buna karşılık verin sağlayıcının
  sunucusuna gider ve modelin üstünde az kontrolün olur.
- **Açık-model / self-host:** Model dosyalarını indirip **kendi sunucunda** çalıştırırsın
  (self-host = kendi kendine barındırma). Kontrol ve gizlilik sende kalır; bedeli donanım,
  kurulum ve bakım yüküdür.

Karar tek eksende değil bir dengede (trade-off) verilir. Genel eğilim şudur: aynı model ailesi
içinde model büyüdükçe kalite artar, ama **gecikme** (latency — isteği gönderdiğin an ile cevabın
geldiği an arasındaki süre) ve maliyet de artar.

**Şerh (abartıya karşı):** "Büyük her zaman iyidir" doğru değildir; bu ilişki yalnızca *aynı aile
ve aynı kuşak* içinde güvenilirdir. Yeni kuşak küçük bir model, eski kuşak büyük bir modeli pek
çok işte geçebiliyor. Dahası birçok görevde küçük model **zaten yeterlidir** — fazlası paradan
ve hızdan başka bir şey kazandırmaz. Doğru soru "hangisi en güçlü" değil, "hangisi benim işim
için yeterli ve en ucuz" sorusudur.

Ve sakın lider tablosuna (leaderboard — modelleri ortak sınavlardaki puanına göre sıralayan
tablo) kanma. İki tuzağı vardır: **benchmark kontaminasyonu** (sınav sorularının modelin eğitim
verisine karışmış olması — model soruyu çözmemiş, ezberlemiş olabilir) ve o sınavın senin kullanım
senaryonla (use-case) alakasızlığı. **Kararı kendi değerlendirme (eval) setin verir:** kendi
işinden topladığın 30–50 gerçek örnek, herhangi bir genel sıralamadan daha bilgilendiricidir.

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Model seçim ekseni kapalı-API ile açık-model arasında ve boyut gecikme maliyet kalite dengesi">
  <defs>
    <marker id="h7ok3" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto-start-reverse"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="h7ok4" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Foundation Model Seçimi — eksen ve denge</text>
  <rect x="30" y="48" width="230" height="78" rx="12" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="145" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#4F46E5" font-family="system-ui">Kapalı-API</text>
  <text x="145" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(ör. Claude, GPT)</text>
  <text x="145" y="110" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">hazır · bakımsız · veri dışarı</text>
  <rect x="380" y="48" width="230" height="78" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="495" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Açık-model</text>
  <text x="495" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(self-host)</text>
  <text x="495" y="110" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kontrol · gizlilik · altyapı yükü</text>
  <line x1="262" y1="87" x2="378" y2="87" stroke="#5D7789" stroke-width="1.8" marker-start="url(#h7ok3)" marker-end="url(#h7ok4)"/>
  <text x="320" y="80" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">seçim ekseni</text>
  <text x="320" y="160" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">Denge (trade-off): model büyüdükçe...</text>
  <rect x="30" y="175" width="130" height="54" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="95" y="199" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Boyut ↑</text>
  <text x="95" y="216" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">daha büyük model</text>
  <rect x="180" y="175" width="130" height="54" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="245" y="199" text-anchor="middle" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">Gecikme ↑</text>
  <text x="245" y="216" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">daha yavaş</text>
  <rect x="330" y="175" width="130" height="54" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="395" y="199" text-anchor="middle" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">Maliyet ↑</text>
  <text x="395" y="216" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">daha pahalı</text>
  <rect x="480" y="175" width="130" height="54" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="545" y="199" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Kalite ↑</text>
  <text x="545" y="216" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">daha iyi</text>
  <rect x="30" y="248" width="580" height="42" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="320" y="266" text-anchor="middle" font-size="10.5" font-weight="600" fill="#1C3247" font-family="system-ui">Tuzak: lider tablosu (leaderboard) değil, kendi eval setin karar verir.</text>
  <text x="320" y="282" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Benchmark kontaminasyonu ve use-case'ine uzaklık yanıltır.</text>
</svg>
```

## 3. Prompt engineering disiplini
Prompt "sihir" değil mühendisliktir. İyi bir prompt dört bileşenden kurulur:

1. **Açık talimat (instruction):** Ne istediğini, kime, hangi biçimde söyle. Modelin senin
   kafandakini tahmin etmesini bekleme; belirsiz bırakılan her şeyi kendi doldurur.
2. **Few-shot (birkaç örnekli):** Prompt'un içine 2–5 tane çözülmüş örnek koymak. Model,
   tarif ettiğin işi örneklerden anlar. Hiç örnek vermemeye **zero-shot** (sıfır örnekli) denir.
3. **Chain-of-thought (düşünce zinciri):** Modelden cevabı doğrudan değil, ara adımları
   yazarak vermesini istemek. Çok adımlı akıl yürütme ve hesap gerektiren işlerde isabeti
   artırır. *Şerh:* akıl yürütmeyi kendi içinde yapan yeni kuşak modellerde bunu ayrıca
   istemek çoğu zaman gereksizdir, hatta çıktıyı uzatıp maliyeti artırır — faydasını
   kendi eval setinde ölç, moda diye ekleme.
4. **Yapısal çıktı (structured output):** Cevabı serbest metin yerine sabit bir biçimde
   istemek; genelde **JSON** (JavaScript Object Notation — program tarafından okunması kolay,
   anahtar-değer çiftlerinden oluşan metin biçimi) ve onu tarif eden bir **şema** ile.
   Böylece çıktıyı kodun içinde ayrıştırabilirsin.

En önemlisi: **prompt = kod.** Versiyonla (git'te tut, değişikliğin izini sür), test et, ölç —
tıpkı yazılım gibi. Bir prompt'u "iyileştirdim" demek, ancak ölçtüğün bir sayı yükseldiyse
anlamlıdır; yoksa yalnızca değiştirmiş olursun.

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Prompt disiplini talimat few-shot adım adım JSON şema bileşenleri yapısal çıktıya dönüşür versiyonla ve test et">
  <defs>
    <marker id="h7ok5" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#1C3247"/></marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Prompt Disiplini — sihir değil mühendislik</text>
  <rect x="30" y="46" width="350" height="212" rx="14" fill="#F2F6F9" stroke="#5D7789" stroke-width="1.5"/>
  <text x="48" y="68" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">Prompt (girdi) — dört bileşen</text>
  <rect x="48" y="78" width="314" height="38" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="60" y="96" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Talimat</text>
  <text x="60" y="110" font-size="9.5" fill="#5D7789" font-family="system-ui">açık ve net: ne istediğini söyle</text>
  <rect x="48" y="122" width="314" height="38" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="60" y="140" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">2 · Few-shot</text>
  <text x="60" y="154" font-size="9.5" fill="#5D7789" font-family="system-ui">birkaç örnek göster</text>
  <rect x="48" y="166" width="314" height="38" rx="8" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="60" y="184" font-size="11.5" font-weight="700" fill="#4F46E5" font-family="system-ui">3 · Chain-of-thought</text>
  <text x="60" y="198" font-size="9.5" fill="#5D7789" font-family="system-ui">adım adım düşündür</text>
  <rect x="48" y="210" width="314" height="38" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="60" y="228" font-size="11.5" font-weight="700" fill="#C77A17" font-family="system-ui">4 · Yapısal çıktı</text>
  <text x="60" y="242" font-size="9.5" fill="#5D7789" font-family="system-ui">JSON şema ile biçim zorla</text>
  <line x1="382" y1="150" x2="466" y2="150" stroke="#1C3247" stroke-width="1.8" marker-end="url(#h7ok5)"/>
  <rect x="468" y="116" width="142" height="68" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="539" y="146" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Çıktı</text>
  <text x="539" y="164" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(yapısal · JSON)</text>
  <rect x="468" y="200" width="142" height="58" rx="12" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="539" y="222" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">Prompt = kod</text>
  <text x="539" y="240" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">versiyonla + test et</text>
  <text x="320" y="286" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Prompt'u yazılım gibi ele al: versiyonla, test et, ölç — sihir değil mühendislik (engineering).</text>
</svg>
```

## Öz-sınav (kendini test et)
1. Klasik ML ile AI Engineering arasındaki temel fark nedir; hangi adım (klasik ML'de
   olmayan bir yük olarak) AI Engineering'de kritikleşir?
2. Kapalı-API bir modelle (ör. Claude/GPT) açık-model / self-host arasında seçim yaparken
   hangi eksende karar verirsin; her birinin en büyük ödünü (trade-off) nedir?
3. "Büyük model her zaman daha iyi" cümlesi neden eksiktir? Boyut arttıkça hangi iki maliyet
   yükselir?
4. Bir modelin lider tablosunda (leaderboard) birinci olması onu senin için doğru seçim
   yapar mı? Neden kendi eval setin gerekir (benchmark tuzakları)?
5. İyi bir prompt'un dört bileşenini say. "Prompt = kod" ifadesi pratikte ne yapmanı ister?
6. Token ile bağlam penceresi (context window) arasındaki ilişki nedir? Bir belge bağlam
   penceresine sığmıyorsa ne olur?
7. Temperature'ı 0'a çekmek çıktıyı "doğru" yapar mı? Yapmıyorsa tam olarak neyi değiştirir,
   ve hangi tür işlerde düşük, hangilerinde yüksek tutarsın?
8. "Benchmark kontaminasyonu" nedir ve bir modelin lider tablosundaki puanını neden şüpheli
   kılar?
