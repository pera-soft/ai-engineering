---
baslik: "Hafta 8 Görsel Özet: RAG, Fine-tune Kararı, Değerlendirme"
yazar: "AI Engineering (Huyen, O'Reilly 2025) — kavram özeti"
tarih: "2026-07-11"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 8
---

### Bu metin ne anlatıyor?
Hazır bir dil modeli senin şirketinin belgelerini, dünkü fiyat listesini veya iç kurallarını
bilmez — çünkü onlarla eğitilmedi. Bu metin üç soruya cevap veriyor: modele bu bilgiyi nasıl
verirsin (RAG), modelin *davranışını* değiştirmek gerektiğinde ne yaparsın (fine-tune) ve
ürettiği cevabın iyi olup olmadığını nasıl ölçersin (değerlendirme). Hafta 7'deki temel
terimler (LLM, token, bağlam penceresi, prompt) burada bilindiği varsayılıyor; bu haftanın
yeni terimleri geçtikleri yerde tanımlanıyor.

### Bu metnin yeni terimleri
- **Vektör (vector):** Bir metni temsil eden sayı dizisi. Anlamca yakın iki metnin sayıları da
  birbirine yakın çıkar; böylece "anlam" ölçülebilir bir mesafeye dönüşür.
- **Embedding (gömme):** Metni vektöre çeviren işlem — ve bu işi yapan modele **embedding modeli**
  denir. Metin girer, sabit uzunlukta bir sayı dizisi çıkar.
- **Chunk (parça):** Uzun bir belgenin, ayrı ayrı aranabilsin diye bölündüğü küçük metin parçası
  (tipik olarak birkaç yüz kelime).
- **Vektör veritabanı (vector store):** Bu vektörleri saklayan ve "bana şuna en benzeyen 5 taneyi
  getir" sorusunu hızlı cevaplayan veritabanı.
- **Cosine benzerliği (cosine similarity):** İki vektörün ne kadar aynı yöne baktığını ölçen sayı.
  1'e yakınsa çok benzer, 0'a yakınsa alakasız.
- **Halüsinasyon (hallucination):** Modelin, doğruymuş gibi ve kendinden emin bir dille uydurduğu
  bilgi. Model "bilmiyorum" demek yerine olası görünen bir devam ürettiği için ortaya çıkar.

## Neden bu özet
Hafta 8, Huyen'in kitabının ikinci yarısını işliyor: modele nasıl bilgi eklenir (RAG),
ne zaman fine-tune yapılır, ve çıktı nasıl değerlendirilir (evaluation). Bu üç görsel;
"bilgi eklemek fine-tune değildir", "önce ucuz olanı dene" ve "yargıç modelin de
yanlılıkları vardır" ayrımlarının kafada netleşmesi için bir çırpıda bakılacak referans
niyetinedir.

## 1. RAG (Retrieval-Augmented Generation) boru hattı
RAG (Retrieval-Augmented Generation — "getirilen bilgiyle zenginleştirilmiş üretim"), modele
**bilgi eklemenin** yoludur; fine-tune değil. Fikir basittir: modelin bilmediği bilgiyi ona
cevap yazmadan hemen önce, prompt'un içinde elden veririz. Açık kitap sınavı gibi: model bilgiyi
ezberlemez, doğru sayfayı önüne koyarız.

Zincir dört adımdır — **parçala → gömle → getir → yeniden sırala** (chunk → embed → retrieve →
rerank). Kaynak belgeler küçük parçalara bölünüp vektöre çevrilir ve bir vektör store'da tutulur;
sorgu geldiğinde en benzer parçalar (cosine benzerliği ile, en iyi **top-K** yani ilk K tanesi —
K genelde 3–20 arası bir sayıdır) getirilir, yeniden sıralanır (re-ranking) ve prompt'un bağlamına
(context) yerleştirilir, ardından LLM bu bağlamla cevap üretir.

**Re-ranking neden ayrı bir adım?** İlk arama hızlı olsun diye kabadır: milyonlarca parça arasından
saniyenin altında aday çıkarır, ama sıralaması kusurludur. Re-ranker daha yavaş ve daha dikkatli
bir modeldir; yalnızca gelen 20–50 adaya bakar ve soruyla gerçekten ilgili olanları öne çeker.
Ucuz-geniş elemeyi pahalı-dar eleme izler.

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="RAG boru hattı: kaynak belgeden chunking, embedding, vektör store, sorgu ile benzerlik araması, re-ranking, bağlam ve LLM cevabına uzanan zincir">
  <defs>
    <marker id="h8ok1" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">RAG Boru Hattı — bilgi eklemenin yolu (fine-tune değil)</text>

  <rect x="20" y="50" width="140" height="54" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="90" y="74" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Kaynak belge</text>
  <text x="90" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ham kaynak metin</text>

  <rect x="180" y="50" width="140" height="54" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="250" y="74" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Chunking</text>
  <text x="250" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">küçük parçalara böl</text>

  <rect x="340" y="50" width="140" height="54" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="410" y="74" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Embedding</text>
  <text x="410" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">metni vektöre çevir</text>

  <rect x="500" y="50" width="140" height="54" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="570" y="74" text-anchor="middle" font-size="12.5" font-weight="700" fill="#4F46E5" font-family="system-ui">Vektör store</text>
  <text x="570" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">vektör veritabanı</text>

  <line x1="160" y1="77" x2="178" y2="77" stroke="#5D7789" stroke-width="1.8" marker-end="url(#h8ok1)"/>
  <line x1="320" y1="77" x2="338" y2="77" stroke="#5D7789" stroke-width="1.8" marker-end="url(#h8ok1)"/>
  <line x1="480" y1="77" x2="498" y2="77" stroke="#5D7789" stroke-width="1.8" marker-end="url(#h8ok1)"/>

  <line x1="570" y1="104" x2="570" y2="148" stroke="#5D7789" stroke-width="1.8" marker-end="url(#h8ok1)"/>
  <text x="582" y="130" font-size="9.5" fill="#5D7789" font-family="system-ui">sorgu anı</text>

  <rect x="500" y="150" width="140" height="54" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="570" y="174" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Sorgu embed + ara</text>
  <text x="570" y="190" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">cosine · top-K aday</text>

  <rect x="340" y="150" width="140" height="54" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="410" y="174" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Re-ranking</text>
  <text x="410" y="190" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">adayları sırala</text>

  <rect x="180" y="150" width="140" height="54" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="250" y="174" text-anchor="middle" font-size="12.5" font-weight="700" fill="#C77A17" font-family="system-ui">Bağlama koy</text>
  <text x="250" y="190" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">en iyi parçalar → prompt</text>

  <line x1="500" y1="177" x2="482" y2="177" stroke="#5D7789" stroke-width="1.8" marker-end="url(#h8ok1)"/>
  <line x1="340" y1="177" x2="322" y2="177" stroke="#5D7789" stroke-width="1.8" marker-end="url(#h8ok1)"/>

  <line x1="250" y1="204" x2="250" y2="248" stroke="#5D7789" stroke-width="1.8" marker-end="url(#h8ok1)"/>

  <rect x="180" y="250" width="300" height="54" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="330" y="274" text-anchor="middle" font-size="12.5" font-weight="700" fill="#4F46E5" font-family="system-ui">LLM cevabı üretir → Cevap</text>
  <text x="330" y="290" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">getirilen bağlamla yanıt verir</text>

  <text x="330" y="322" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">RAG modele BİLGİ ekler; davranışı/formatı değiştirmez — o iş fine-tune'undur.</text>
</svg>
```

### RAG'in üç sessiz kuralı
**1. Embedding modeli tutarlılığı (en sık yapılan hata).** Belgeleri hangi embedding modeliyle
vektöre çevirdiysen, **sorguyu da aynı modelle** çevirmek zorundasın. Sebebi şu: her embedding
modeli anlamı kendi koordinat sisteminde temsil eder; iki farklı modelin ürettiği vektörler aynı
uzayda değildir, dolayısıyla aralarındaki mesafe hiçbir şey ifade etmez. Karşılaştırma teknik
olarak *çalışır* (sayılar aynı boyuttaysa bir sonuç üretir) ama getirilen parçalar rastgeleye
yakın olur — ve sistem hata vermediği için bunu fark etmen haftalar alabilir. Aynı kural sürüm
yükseltmeleri için de geçerlidir: embedding modelini değiştirirsen **tüm arşivi yeniden vektöre
çevirmen (re-index)** gerekir. Yarısı eski, yarısı yeni bir arşiv bozuktur.

**2. Yalnız anlam araması yetmez.** Cosine benzerliği anlamı yakalar ama birebir eşleşmeleri
kaçırır: ürün kodu, hata numarası, kişi adı, kanun maddesi gibi şeylerde klasik kelime araması
(**BM25** gibi anahtar kelime yöntemleri) daha isabetlidir. Pratikte ikisi birlikte kullanılır;
buna **melez arama (hybrid search)** denir.

**3. Parça boyutu bir ayardır, sabit değil.** Çok küçük parça bağlamı koparır ("bu cümle neyden
bahsediyordu?"), çok büyük parça alakasız metni de içeri sokar ve sinyali seyreltir. Doğru boyut
belgeye göre değişir; tahminle değil, ölçerek bulunur.

> **Şerh (abartıya karşı): RAG halüsinasyonu bitirmez, azaltır.** Satıcı sunumlarında "RAG ile
> halüsinasyon sorunu çözülür" cümlesini sık duyarsın; doğru değildir. RAG en az üç yerden
> yanılabilir: (a) arama yanlış parçayı getirir, model de yanlış kaynaktan emin bir cevap yazar;
> (b) doğru parça gelir ama model onu yanlış yorumlar veya bağlamda olmayan bir ayrıntı ekler;
> (c) cevap kaynakta hiç yoktur, model yine de boşluğu doldurur. RAG'in yaptığı, modelin doğru
> bilgiye erişme *olasılığını* ciddi biçimde artırmaktır. Halüsinasyonu ölçülebilir seviyeye
> çekmek için üstüne şunlar gerekir: kaynak gösterme (cevabın hangi parçadan geldiğini yazdırma),
> "bağlamda yoksa *bilmiyorum* de" talimatı ve düzenli değerlendirme. Kısacası RAG bir çözüm
> değil, bir **risk azaltma** aracıdır.

> **Kitap dışı ek (AIEFS) — melez aramayı nasıl birleştirirsin, reranker'a kaç aday verirsin?**
> Bu kutu Huyen'in kitabından değil, *AI Engineering from Scratch* derslerinden (19-65, 19-66)
> eklendi.
> - **Puanları toplama, sıraları oylat.** BM25 puanı sınırsızdır ve belge arşivine göre değişir;
>   cosine benzerliği −1 ile 1 arasındadır. `alfa × bm25 + (1 − alfa) × cosine` gibi ağırlıklı
>   toplam, iki farklı cetveli toplamaktır: alfa her arşive ayrı ayarlanır ve yeniden indekslemede
>   bozulur. Bunun yerine **karşılıklı sıra birleştirme (Reciprocal Rank Fusion, RRF)** kullan:
>   her aday, her listedeki sırası için `1 / (k + sıra)` puan alır, puanlar toplanır, yeniden
>   sıralanır. Yaygın varsayılan **k = 60**'tır (Cormack ve ark., 2009). Sıralar iki yöntem
>   arasında karşılaştırılabilir, puanlar değildir.
> - **Reranker, ilk aşamanın kaçırdığını geri getiremez.** Çapraz kodlayıcı (cross-encoder)
>   reranker yalnız kendisine verilen N adayı yeniden dizer. Doğru parça ilk aşamanın ilk N'inde
>   yoksa, reranker ne kadar iyi olursa olsun onu bulamaz: nihai kalite, ilk aşamanın **N'deki
>   recall'u** ile sınırlıdır. Kural: **N ≥ 3 × K** (K = modele vereceğin parça sayısı). N = K
>   seçersen reranker yalnız sırayı değiştirir, yeni aday getiremez. Dersin gözlemi, kazancın
>   birkaç on adaydan sonra doyduğu yönünde; bu bir ölçüm değil, kendi eğrinde bulunacak bir
>   eşik — N'yi kendi ölçümün ve gecikme bütçen belirler.
>
> Hatalı bir cevabın hangi aşamadan (arama mı, sıralama mı, üretim mi) geldiğini nasıl ölçeceğin
> [evals-llm-observability.md](evals-llm-observability.md) §10'da.

## 2. Karar merdiveni — ne zaman hangisi
Aynı sorunu üç farklı yolla çözebilirsin ve maliyetleri çok farklıdır. Kural: **ucuzdan
başla**. Önce prompt (yönerge) dene; yetmezse RAG/bağlam ile bilgi getir; **en son**
fine-tune yap.

**Fine-tune (ince ayar)** nedir: hazır bir modeli, kendi hazırladığın örnek girdi–çıktı
çiftleriyle bir miktar daha eğitmek. Davranış, format, ton ve alan jargonu içindir — modele
**bilgi eklemek için DEĞİL**. Ayrımı şöyle tutabilirsin: fine-tune modelin *nasıl konuştuğunu*
değiştirir, RAG *neyi bildiğini* değiştirir.

En yaygın hata, fine-tune'u ilk çare sanmaktır. Bunun bedeli sadece para değildir: eğitim verisi
hazırlaman, sonucu değerlendirmen, modeli barındırman ve her yeni bilgi geldiğinde bunu yeniden
yapman gerekir. RAG'de ise yeni bilgi eklemek bir belgeyi arşive koymaktan ibarettir.

```svg
<svg viewBox="0 0 660 388" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Karar merdiveni: önce Prompt, sonra RAG, en son Fine-tune; ucuzdan pahalıya basamaklar ve fine-tune bilgi için değildir uyarısı">
  <defs>
    <marker id="h8ok2" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Karar Merdiveni — ucuzdan pahalıya</text>

  <rect x="30" y="44" width="250" height="86" rx="12" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="46" y="66" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">Yaygın hata</text>
  <text x="46" y="84" font-size="10.5" fill="#1C3247" font-family="system-ui">Fine-tune'u ilk çare sanmak.</text>
  <text x="46" y="100" font-size="10.5" fill="#1C3247" font-family="system-ui">Fine-tune BİLGİ eklemez —</text>
  <text x="46" y="116" font-size="10.5" fill="#1C3247" font-family="system-ui">onu RAG / bağlam yapar.</text>

  <rect x="60" y="250" width="180" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="150" y="276" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">1 · Prompt</text>
  <text x="150" y="294" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yönerge · örnek</text>
  <text x="224" y="266" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">$</text>

  <rect x="250" y="178" width="180" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="340" y="204" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">2 · RAG / bağlam</text>
  <text x="340" y="222" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bilgi getir</text>
  <text x="414" y="194" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">$$</text>

  <rect x="440" y="106" width="180" height="60" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="530" y="132" text-anchor="middle" font-size="13" font-weight="700" fill="#4F46E5" font-family="system-ui">3 · Fine-tune</text>
  <text x="530" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">davranış/ton eğit</text>
  <text x="602" y="122" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">$$$</text>

  <line x1="205" y1="250" x2="290" y2="240" stroke="#5D7789" stroke-width="1.8" marker-end="url(#h8ok2)"/>
  <text x="250" y="248" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yetmezse</text>
  <line x1="395" y1="178" x2="480" y2="168" stroke="#5D7789" stroke-width="1.8" marker-end="url(#h8ok2)"/>
  <text x="440" y="176" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yetmezse</text>

  <text x="150" y="330" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">İlk çare — en ucuz/hızlı</text>
  <text x="150" y="346" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">genel görevler, yönerge yeter</text>

  <text x="340" y="256" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Bilgi eksikse — güncel/özel veri</text>
  <text x="340" y="272" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">veriyi bağlama getirir</text>

  <text x="530" y="184" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Davranış · format · ton · jargon</text>
  <text x="530" y="200" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">BİLGİ eklemek için DEĞİL</text>

  <text x="330" y="378" text-anchor="middle" font-size="10.5" font-weight="600" fill="#186399" font-family="system-ui">Kural: ucuzdan başla; ancak bir basamak yetmezse üstüne çık.</text>
</svg>
```

## 3. Değerlendirme (evaluation) — AI-as-judge ve yanlılıklar
Klasik metrikler (kelime örtüşmesi gibi — yani cevabın "doğru cevap"la kaç ortak kelimesi var)
açık uçlu üretimi ölçmeye yetmez: iki cevap tek ortak kelime içermeden ikisi de doğru olabilir.
Yaygın yol **AI-as-judge** (LLM-as-judge diye de geçer): bir model, başka bir modelin çıktısını
bir **rubrikle** (rubric — neyin kaç puan alacağını tarif eden yazılı ölçüt listesi) puanlar.

Ama yargıç modelin de yanlılıkları (bias — sistematik sapmalar) vardır: pozisyon yanlılığı,
kendini-sevme ve uzunluk yanlılığı. Azaltmak için net rubrik + örnekli **kalibrasyon**
(yargıca "bu cevap 3 puandır, bu 5" diye önceden puanlanmış örnek göstermek) + **swap**
(yer değiştirme) testi + insan referansı kullanılır; ayrıca benchmark kontaminasyonuna
dikkat edilir. Swap testi şudur: aynı iki cevabı bir de sırasını değiştirerek sorarsın;
kazanan değişiyorsa yargıcın içeriğe değil sıraya bakıyor demektir.

```svg
<svg viewBox="0 0 660 350" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="AI-as-judge akışı: aday çıktı ve rubrik yargıç modele girer, puan üretir; altında pozisyon, kendini-sevme ve uzunluk yanlılığı kartları ve swap testi ile azaltma">
  <defs>
    <marker id="h8ok3" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Değerlendirme — AI-as-judge + yanlılıklar</text>

  <rect x="30" y="46" width="140" height="50" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="100" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Aday çıktı</text>
  <text x="100" y="84" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">değerlendirilecek</text>

  <rect x="30" y="110" width="140" height="50" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="100" y="132" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Rubrik + örnek</text>
  <text x="100" y="148" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ölçüt · kalibrasyon</text>

  <rect x="225" y="74" width="180" height="64" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="315" y="102" text-anchor="middle" font-size="13" font-weight="700" fill="#4F46E5" font-family="system-ui">AI-as-judge</text>
  <text x="315" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yargıç model puanlar</text>

  <rect x="460" y="82" width="150" height="50" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="535" y="104" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Puan / sıralama</text>
  <text x="535" y="120" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">rubriğe göre skor</text>

  <line x1="170" y1="71" x2="223" y2="95" stroke="#5D7789" stroke-width="1.8" marker-end="url(#h8ok3)"/>
  <line x1="170" y1="135" x2="223" y2="116" stroke="#5D7789" stroke-width="1.8" marker-end="url(#h8ok3)"/>
  <line x1="405" y1="106" x2="458" y2="106" stroke="#5D7789" stroke-width="1.8" marker-end="url(#h8ok3)"/>

  <text x="46" y="182" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">Yargıç yanlılıkları (biases)</text>

  <rect x="20" y="192" width="196" height="92" rx="12" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="118" y="216" text-anchor="middle" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">Pozisyon yanlılığı</text>
  <text x="118" y="232" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(position bias)</text>
  <text x="118" y="254" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sıradaki konum</text>
  <text x="118" y="270" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">puanı kaydırır</text>

  <rect x="232" y="192" width="196" height="92" rx="12" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="330" y="216" text-anchor="middle" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">Kendini-sevme</text>
  <text x="330" y="232" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(self-preference)</text>
  <text x="330" y="254" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kendi ürettiğini</text>
  <text x="330" y="270" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yüksek puanlar</text>

  <rect x="444" y="192" width="196" height="92" rx="12" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="542" y="216" text-anchor="middle" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">Uzunluk yanlılığı</text>
  <text x="542" y="232" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(verbosity bias)</text>
  <text x="542" y="254" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">uzun cevabı</text>
  <text x="542" y="270" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">iyi sanır</text>

  <rect x="20" y="294" width="620" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="312" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Azalt: net rubrik + örnekli kalibrasyon + swap (yer değiştirme) testi + insan referansı</text>
  <text x="330" y="330" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Ayrıca: benchmark kontaminasyonu — test verisi eğitimde görülmüş olabilir.</text>
</svg>
```

### Altın küme (golden set) — değerlendirmenin çıpası
Yargıç modelden önce gelen, daha basit ve daha sağlam bir araç var: **altın küme** (golden set /
golden dataset). Kendi işinden toplanmış, **insan tarafından** doğru cevabı yazılmış sabit bir
soru listesidir. 30–50 örnek başlangıç için yeterlidir; önemli olan sayı değil kapsamdır.

Neden vazgeçilmez:
- **Karşılaştırma zemini olur.** Prompt'u değiştirdin, modeli değiştirdin, chunk boyutunu
  değiştirdin — "daha iyi oldu mu?" sorusunun tek dürüst cevabı, aynı sabit küme üzerinde
  önce/sonra puanını karşılaştırmaktır.
- **Gerileme (regression) yakalar.** Bir iyileştirme çoğu zaman başka bir yeri bozar; sabit
  küme olmadan bunu göremezsin.
- **Yargıcı denetler.** Yargıç modelin puanlarını altın kümedeki insan puanlarıyla karşılaştırır,
  ne kadar uyuştuğuna bakarsın. Uyum düşükse ölçtüğün şey modelin kalitesi değil, yargıcın
  kaprisidir.

Kurma kuralları: kümeye **kolay örnekler kadar zor ve sınır örnekleri de** koy (aksi hâlde puan
hep yüksek çıkar ve hiçbir şey öğretmez); cevabı olmayan soruları da koy (doğru davranış
"bilmiyorum" demektir); kümeyi prompt'u ayarlarken kullandığın örneklerden **ayrı tut**, yoksa
kendi kopya kâğıdını kendine sınav yapmış olursun.

### Offline mi online mı — iki farklı soru
Değerlendirmenin iki yeri vardır ve birbirinin yerine geçmezler.

| | **Offline (çevrimdışı)** | **Online (canlı)** |
|---|---|---|
| Nerede | Yayına almadan önce, laboratuvarda | Gerçek kullanıcılarla, üretimde |
| Neyle | Altın küme, sabit sorular | Gerçek trafik, gerçek sorular |
| Cevapladığı soru | "Bu değişiklik bilinen örneklerde daha iyi mi?" | "Gerçek hayatta işe yarıyor mu?" |
| Ölçüler | İsabet, kaynak sadakati, biçim uyumu | Kullanıcı davranışı, terk oranı, başparmak, hata bildirimi, maliyet, gecikme |
| Güçlü yanı | Hızlı, ucuz, tekrar edilebilir | Gerçeği ölçer |
| Zayıf yanı | Gerçek kullanımın çeşitliliğini kaçırır | Yavaş, gürültülü, hata kullanıcıya çarpar |

Doğru düzen ikisini sıraya dizer: değişikliği önce offline kümede ölç, geçerse **küçük bir
kullanıcı dilimine** aç ve online göstergeleri izle, sorun yoksa yaygınlaştır. Yalnız offline'a
güvenmek "laboratuvarda çalışıyordu" tuzağıdır; yalnız online'a güvenmek ise kullanıcıyı denek
yapmaktır.

> **Şerh (abartıya karşı): yargıç model, ölçüm aracının kendisi de hatalıdır.** AI-as-judge
> ucuz ve ölçeklenebilirdir, ama bir **tahmindir** — insan yargısının yerine geçmez. Sınırları
> net: yargıç, kendisinden daha iyi bir çıktıyı güvenilir biçimde derecelendiremez; ölçmesi
> istenen şey öznelleştikçe (ör. "ne kadar yaratıcı?") tutarlılığı düşer; olgusal doğruluğu
> ancak kaynak metni de önüne koyduğunda kontrol edebilir, yoksa o da tahmin yürütür. Pratik
> kural: yargıcı **süzgeç ve erken uyarı** olarak kullan, **karar mercii** olarak değil.
> Yayına alma kararı gibi geri dönüşü pahalı yerlerde insan gözü şarttır.

## Maliyet ve gecikme (cost/latency)
Değerlendirme kadar üretim ekonomisi de önemlidir. "En iyi modeli her istekte çağır"
bir anti-pattern'dir. Pratik teknikler:
- **Prompt caching (istem önbelleği):** tekrar eden bağlamı yeniden ödemeyi önler.
- **Model kademeleme (cascading):** önce ucuz/küçük model dener; yetersizse pahalı
  modele yükseltilir.
- **Batch (toplu işleme):** aynı anda çok isteği toplu ve ucuza çalıştırma.
- **Streaming (akış):** cevabı parça parça göndererek algılanan gecikmeyi azaltma.

## Öz-sınav (kendini test et)
1. RAG boru hattında bir belge cevaba dönüşene kadar hangi adımlardan geçer (chunking'den
   LLM'e kadar sırala)?
2. "cosine benzerliği + top-K" ne işe yarar; re-ranking neden ayrı bir adımdır?
3. Karar merdiveninde neden önce prompt, en son fine-tune denenir? Fine-tune neyi
   çözer, neyi çözmez?
4. AI-as-judge'da pozisyon yanlılığı, kendini-sevme ve uzunluk yanlılığı nedir; swap
   testi hangisini azaltmaya yarar?
5. "En iyi modeli her istekte çağır" neden anti-pattern; prompt caching ve cascading
   maliyeti nasıl düşürür?
6. Belgeleri A embedding modeliyle, sorguları B embedding modeliyle vektöre çevirirsen ne
   olur — sistem hata verir mi? Vermiyorsa bunu nasıl fark edersin?
7. Bir ekip "RAG kurduk, artık halüsinasyon yok" diyor. Bu cümlenin nesi yanlış? RAG'in yine
   de yanlış cevap üretebileceği üç yolu say.
8. Altın küme (golden set) tam olarak neye yarar, ve içine neden kolay örneklerin yanında zor
   ve cevapsız örnekler de konur?
9. Offline değerlendirme ile online değerlendirme hangi farklı soruları cevaplar? Yalnızca
   birine güvenirsen ne kaçırırsın?
10. AI-as-judge'ı "karar mercii" değil "süzgeç" olarak kullanmak ne demek? Yargıcın verdiği
    puanların güvenilir olup olmadığını neyle kontrol edersin?
11. (Kitap dışı ek) BM25 ve cosine puanlarını ağırlıklı toplamak yerine neden RRF ile sıraları
    birleştirirsin? Reranker'a K = 5 parça için kaç aday vermelisin, ve N = K seçersen ne olur?

    *Cevap ipucu:* puanlar farklı cetvellerde (BM25 sınırsız, cosine −1…1), sıralar ise
    karşılaştırılabilir; RRF `1/(k+sıra)` toplar. N ≥ 3 × K → en az 15; N = K ise reranker yeni
    aday getiremez, recall değişmez.

## Kaynak ve lisans
- Ana kaynak: Chip Huyen, *AI Engineering* (O'Reilly, 2025) — bu özetin gövdesi.
- §1'deki "Kitap dışı ek (AIEFS)" kutusu: *AI Engineering from Scratch* (Rohit Ghumare, MIT
  lisansı, © 2026), dersler `19-capstone-projects/65-hybrid-retrieval-bm25-dense` ve
  `66-reranker-cross-encoder` — github.com/rohitg00/ai-engineering-from-scratch. Çeviri değil,
  uyarlanmış özettir.
