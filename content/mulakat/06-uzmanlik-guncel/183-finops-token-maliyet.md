---
slug: finops-token-maliyet
baslik: "LLM FinOps: Token Maliyet Modeli, Prompt Caching ve Model Seçimi"
kademe: İleri
sira: 183
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir **LLM** (Large Language Model — büyük dil modeli, yani yazıyı okuyup yazı üreten program) kullanmak ücretsiz değildir. Fatura ne saate ne de "kaç kere kullandın"a bakar; **token**'a bakar. Token, modelin metni böldüğü küçük parçadır — kabaca bir kelime ya da kelimenin bir bölümü. Modele gönderdiğin her token ve modelin ürettiği her token sayaca yazılır.

**FinOps**, bu sayacı mühendislik kararlarıyla yönetme disiplinidir: aynı kaliteyi belirgin biçimde daha ucuza almak. Bu bölüm hangi kaldıraçların gerçekten işe yaradığını ve hangilerinin efsane olduğunu anlatır.

### Temel kavramlar

- **Girdi (input) token**: modele gönderdiğin her şey — sistem talimatı, araç tanımları, sohbet geçmişi, RAG ile eklenen belgeler, kullanıcının sorusu.
- **Çıktı (output) token**: modelin ürettiği metin. **Ayrı fiyatlandırılır ve tipik olarak girdiden birkaç kat pahalıdır** (yaygın sağlayıcı fiyatlarında çıktı, girdinin yaklaşık beş katıdır).
- **Prompt caching (istem önbellekleme)**: isteğin **değişmeyen ön-eki** sağlayıcı tarafında saklanır, sonraki isteklerde yeniden işlenmez.
- **Routing (yönlendirme)**: her isteği zorluğuna göre farklı büyüklükte bir modele göndermek.
- **Batch (toplu) API**: anında sonuç gerekmeyen işleri kuyruğa alıp indirimli işleyen uç.
- **Maliyet gözlemlenebilirliği (cost observability)**: jeton (token) ve dolar ölçümlerini istek, kullanıcı ve özellik başına ayrı ayrı ölçmek.

### Nasıl çalışır?

Temel formül basittir: **toplam ≈ (girdi_token × girdi_fiyatı + çıktı_token × çıktı_fiyatı) × istek_sayısı.**

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Maliyet formülü: girdi token çarpı fiyat artı çıktı token çarpı fiyat, istek sayısıyla çarpılır">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Fatura token başına: girdi ve çıktı ayrı fiyatlanır</text>
  <rect x="34" y="48" width="196" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="132" y="74" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">girdi_token × girdi$</text>
  <text x="132" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sistem prompt + RAG bağlamı</text>
  <text x="248" y="90" text-anchor="middle" font-size="22" font-weight="700" fill="#1C3247" font-family="system-ui">+</text>
  <rect x="266" y="40" width="220" height="86" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="2"/>
  <text x="376" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">çıktı_token × çıktı$</text>
  <text x="376" y="92" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">tipik olarak birkaç kat PAHALI</text>
  <text x="376" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">→ kısa/yapılandırılmış cevap iste</text>
  <text x="512" y="90" text-anchor="middle" font-size="18" font-weight="700" fill="#1C3247" font-family="system-ui">×</text>
  <rect x="530" y="48" width="80" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="570" y="80" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">istek</text>
  <text x="570" y="98" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">sayısı</text>
  <text x="320" y="156" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Uzun sistem prompt'u, şişkin bağlam ve gereksiz uzun cevap doğrudan faturaya yansır.</text>
  <text x="320" y="180" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Kaldıraçlar: bağlam budama · prompt caching · kısa çıktı · routing · batch</text>
</svg>
```

Bu formül şunu açıklar: üç şey doğrudan faturaya yansır. Uzun sistem istemleri (prompt), şişkin RAG bağlamı (gereğinden büyük top-K, yani gereğinden çok parça getirmek) ve "biraz daha açıklayayım" diye uzayan cevaplar. Çıktı pahalı olduğu için **kısa ve yapılandırılmış cevap istemek** en ucuz kaldıraçlardan biridir.

İkinci kaldıraç **prompt caching**. Sağlayıcı, isteğin başındaki **birebir aynı** ön-eki (uzun sistem talimatı, araç tanımları, sabit few-shot örnekleri, büyük bir referans doküman) önbellekte tutar; sonraki isteklerde bu kısmı yeniden işlemez. Önbellekten okunan jetonlar tipik olarak temel girdi fiyatının onda birine düşer; gecikme (latency) de azalır. Şart nettir: önbelleklenen kısım **değişmez** ve **prompt'un başında** olmalıdır — eşleşme bir ön-ek karşılaştırmasıdır, tek bayt değişse ondan sonrası geçersiz olur. Bu yüzden prompt "sabit → değişken" sırasıyla kurulur; sistem prompt'una `datetime.now()` gibi her istekte değişen bir şey koyarsan hiçbir istek önbelleğe düşmez.

**Önemli uyarı: istem önbelleği (prompt caching) sağlayıcıya özeldir.** Anthropic'te önbellek açıkça işaretlenir (`cache_control`). Önbelleğe yazma işlemi normal fiyatın 1,25 katıdır; uzun yaşam süresinde (TTL) 2 katıdır. Önbelleğin bir yaşam süresi vardır. Ayrıca **önbelleğe alınabilecek asgari bir ön-ek boyutu** aranır; bu boyut modele göre 512 ile 4096 jeton (token) arasında değişir. Bunun altındaki bir ön-ek sessizce önbelleğe alınmaz. Başka sağlayıcılarda ise işaretleme otomatiktir, kurallar ve fiyat çarpanları farklıdır, bazılarında bu özellik hiç yoktur. "Bir sağlayıcıda işe yaradı" başka sağlayıcıda aynı tasarrufu garanti etmez; kazanç her sağlayıcıda yeniden ölçülür.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sabit ön-ek önbelleklenir, değişken kısım sona konur">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sabit başa, değişken sona</text>
  <rect x="40" y="48" width="330" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="205" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Sabit ön-ek (sistem + araçlar + few-shot)</text>
  <text x="205" y="87" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">önbelleklenir → tekrar işlenmez (~%90 ucuz)</text>
  <rect x="382" y="48" width="216" height="46" rx="9" fill="#FBEDEA" stroke="#B4442E" stroke-width="1.5"/>
  <text x="490" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#B4442E" font-family="system-ui">Değişken kısım</text>
  <text x="490" y="87" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kullanıcı girdisi → her istekte yeni</text>
  <text x="320" y="128" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">Ön-ek değişirse önbellek ıskalanır (cache miss).</text>
  <text x="320" y="150" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">Bu yüzden değişken içerik asla başa konmaz.</text>
  <text x="320" y="176" text-anchor="middle" font-size="11" fill="#186399" font-family="system-ui">Çıktı token'ı girdiden pahalı → kısa/yapılandırılmış cevap iste.</text>
</svg>
```

Üçüncü kaldıraç **model seçimidir**. Her işe en güçlü modeli koşmak israftır: basit sınıflandırma ve alan çıkarma işlerini küçük ve ucuz bir model, çok adımlı akıl yürütmeyi büyük model yapar. Bu ayrımı veren bileşene **router** denir; karar basit sezgisellerle (uzunluk, istek türü) veya küçük bir sınıflandırıcı modelle verilir ve **ölçülerek kalibre edilir** — aşırı agresif routing kaliteyi düşürüp yeniden-deneme maliyeti doğurur.

Dördüncüsü **batch**: sonucun anında gerekmediği toplu işler (gecelik 100.000 doküman özeti) batch uca taşınır; sağlayıcılar tipik olarak ~%50 indirim verir, karşılığında sonuç saatler içinde gelir. Yani **gecikme toleransını paraya çevirirsin**.

```svg
<svg viewBox="0 0 640 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Model yönlendirme: kolay istek küçük modele, zor istek büyük modele; batch kuyruğu indirim">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Maliyet/kalite kaldıraçları: routing + batch</text>
  <rect x="30" y="80" width="120" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="90" y="104" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">İstekler</text>
  <text x="90" y="121" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">heterojen</text>
  <rect x="196" y="80" width="104" height="52" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="248" y="104" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Router</text>
  <text x="248" y="121" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">zorluğa göre</text>
  <line x1="150" y1="106" x2="192" y2="106" stroke="#5D7789" stroke-width="2"/>
  <path d="M196 106 l-9 -4.5 l0 9 z" fill="#5D7789"/>
  <line x1="300" y1="96" x2="352" y2="66" stroke="#5D7789" stroke-width="2"/>
  <path d="M356 64 l-10 0.5 l4.5 8 z" fill="#5D7789"/>
  <line x1="300" y1="116" x2="352" y2="146" stroke="#5D7789" stroke-width="2"/>
  <path d="M356 148 l-10 -0.5 l4.5 -8 z" fill="#5D7789"/>
  <rect x="356" y="44" width="256" height="42" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="484" y="62" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">kolay → küçük/ucuz model</text>
  <text x="484" y="78" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sınıflandırma, çıkarma</text>
  <rect x="356" y="126" width="256" height="42" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="484" y="144" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">zor → büyük/kaliteli model</text>
  <text x="484" y="160" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">çok adımlı akıl yürütme</text>
  <rect x="30" y="184" width="582" height="38" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="320" y="208" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Ertelenebilir toplu iş → batch API (anında sonuç gerekmez, tipik ~%50 indirim)</text>
</svg>
```

Hepsinin önkoşulu ölçmektir: istek, kullanıcı, özellik ve model başına token/$ kırılımı olmadan yüksek faturanın kaynağı görünmez. Genelde birkaç özellik veya birkaç ağır kullanıcı maliyetin çoğunu üretir (Pareto). Ölçülmeyen maliyet kontrol edilemez.

### Mülakatta ne sorulur?

Sorulanlar şunlardır:

- "Maliyet nasıl hesaplanır?" Girdi ve çıktı ayrı ücretlenir; çıktı pahalıdır.
- "İstem önbelleğinden nasıl faydalanırsın, önbellek ne zaman ıskalar?"
- "Sağlayıcı değiştirince aynı tasarruf gelir mi?" Hayır.
- "Karışık (heterojen) iş yükünde model seçimini nasıl yaparsın?"
- "Toplu işlemeyi (batch) ne zaman kullanırsın?"
- "Fatura patladı, ilk adımın ne?" Önce kırılımı ölç.

Yanlış cevap kalıpları da bellidir: sıcaklığı düşürmek, her yere en küçük modeli koşmak, ölçmeden iyileştirmeye kalkmak.

### Tek cümlede

LLM faturası jeton (token) başınadır ve çıktı pahalıdır. Kazanç dört yoldan gelir: bağlamı budamak, sabit ön-eki sağlayıcıya özgü kurallarla önbelleğe almak, işi doğru büyüklükteki modele yönlendirmek ve ertelenebilir yükü toplu işlemeye (batch) taşımak. Hepsinin ortak şartı ölçmektir.

# finops-1 | Orta
## Soru
Bir LLM özelliğinin fatura maliyetini en doğru modelleyen ifade hangisidir?
## A
Girdi ve çıktı token'ları ayrı birim fiyatlarla toplanır ve istek sayısıyla ölçeklenir
🤖 Doğru: input ve output token ayrı fiyatlanır, toplam istek sayısıyla çarpılır.
## B
Maliyet yalnız istek sayısına bağlıdır; prompt ve cevap uzunluğu faturayı değiştirmez
🤖 Fatura token başınadır; aynı istek sayısında uzun prompt ve cevap çok daha pahalıdır.
## C
Maliyet yalnız girdi token'ına bağlıdır; modelin ürettiği çıktı ücretsizdir
🤖 Çıktı token'ı ayrıca ve tipik olarak daha pahalı fiyatlanır; ücretsiz değildir.
## D
Maliyet aylık sabit bir abonelik ücretidir; kullanım arttıkça fatura değişmez
🤖 Kullanım-başına token modelinde maliyet doğrudan kullanımla değişir, sabit değildir.
## Doğru
A
## Açıklama
LLM maliyeti **token başınadır** ve **girdi (input)** ile **çıktı (output)** token'ları ayrı birim fiyatlarla ücretlendirilir; çıktı token'ı tipik olarak girdiden birkaç kat pahalıdır (yaygın fiyat tarifelerinde yaklaşık beş kat). Toplam ≈ (girdi_token × girdi_fiyatı + çıktı_token × çıktı_fiyatı) × istek_sayısı. Bu model, uzun sistem prompt'unun, şişkin RAG bağlamının ve gereksiz uzun cevapların neden doğrudan faturaya yansıdığını açıklar. Sadece istek sayısı (B), sadece girdi (C) veya sabit ücret (D) varsayımları yanlıştır. Pratik sonuç: çıktı pahalı olduğu için kısa ve yapılandırılmış cevap istemek, bağlamı budamak kadar etkili bir tasarruf kaldıracıdır.

# finops-2 | İleri
## Soru
Uzun ve değişmeyen bir sistem prompt'u ile araç tanımları her istekte tekrar gönderiliyor. Prompt caching'den en çok faydalanmak için tasarım nasıl olmalı?
## A
Değişken kullanıcı girdisini prompt'un en başına, sabit sistem talimatını sonuna koymak
🤖 Ön-ek değişkenle başlarsa her istek cache-miss olur; önbellek hiç tutmaz.
## B
Sistem prompt'una her isteğe özel küçük bir bilgi (tarih, kullanıcı adı) eklemek
🤖 Ön-ek birebir aynı olmalı; tek bir değişen alan bile önbelleği tümüyle ıskalatır.
## C
Sabit içeriği prompt'un başında birebir aynı tutup değişken kullanıcı girdisini sona koymak
🤖 Doğru: birebir aynı ön-ek başta olursa önbellek tutar ve o kısım yeniden işlenmez.
## D
Aynı metni istemci tarafında önbelleğe alıp her istekte sağlayıcıya yeniden göndermek
🤖 İstemci önbelleği token sayısını değiştirmez; fatura yine gönderilen token üzerinden çıkar.
## Doğru
C
## Açıklama
**Prompt caching**, isteğin **birebir aynı ön-ekini** sağlayıcı tarafında önbelleğe alır; sonraki isteklerde bu kısım yeniden işlenmez ve önbellekten okuma tipik olarak temel girdi fiyatının onda birine düşer (latency de azalır). Şart: önbelleklenen içerik **değişmez** ve **prompt'un başındadır** — eşleşme bir ön-ek karşılaştırması olduğu için ön-ekteki tek bayt değişse ondan sonrası geçersiz olur. Doğru tasarım bu yüzden **sabit içeriği (sistem talimatı, araç tanımları, sabit few-shot) başa, değişken kullanıcı girdisini sona** koymaktır. Değişkeni başa almak (A) veya sistem prompt'una her istekte değişen bir tarih/kullanıcı adı gömmek (B) her isteği cache-miss yapar — bu, sahada en sık görülen sessiz önbellek katilidir. İstemci tarafında saklamak (D) sağlayıcıya giden token sayısını hiç azaltmaz.

# finops-3 | İleri
## Soru
Bir üründe isteklerin çoğu basit sınıflandırma, azı karmaşık akıl yürütme gerektiriyor. Maliyet/kalite dengesi için en uygun strateji hangisidir?
## A
Tüm isteklere en büyük ve en pahalı modeli koşup kalite riskini tümüyle ortadan kaldırmak
🤖 Basit işlere pahalı modeli koşmak israftır; kalite kazancı olmadan maliyeti şişirir.
## B
Yönlendirme (routing): basit istekleri küçük modele, karmaşık olanları büyük modele göndermek
🤖 Doğru: göreve uygun model seçimi kaliteyi korurken maliyeti çarpıcı biçimde düşürür.
## C
Tüm isteklere en küçük modeli koşup kalite düşüşünü kullanıcı şikâyetiyle takip etmek
🤖 Karmaşık işte küçük model kaliteyi düşürür; hata ve yeniden deneme maliyeti geri gelir.
## D
Modeli istek başına rastgele seçip uzun vadede ortalamanın tutmasını beklemek
🤖 Rastgele atama kaliteyi ve maliyeti öngörülemez kılar; kasıtlı yönlendirme gerekir.
## Doğru
B
## Açıklama
İş yükü heterojense doğru yaklaşım **model yönlendirmesidir (routing)**: her isteği zorluğuna göre uygun modele göndermek — basit sınıflandırma ve alan çıkarma küçük ve ucuz modele, çok adımlı akıl yürütme büyük modele. Böylece kalite gereken yerde korunur, maliyet gerekmeyen yerde düşer. Her yere en büyük modeli koşmak (A) israftır; her yere en küçüğünü koşmak (C) karmaşık işlerde kaliteyi düşürüp yeniden-deneme maliyeti doğurur; rastgele atama (D) hem kaliteyi hem maliyeti öngörülemez kılar. Routing kararı basit sezgisellerle (uzunluk, istek türü) veya küçük bir sınıflandırıcı modelle verilebilir, ama mutlaka ölçülüp kalibre edilmelidir: yanlış yönlendirilen zor istekler yeniden denemeyle tasarrufu geri götürür.

# finops-4 | Orta
## Soru
Gecelik bir işte 100.000 dokümanı özetleyeceksin; sonuç anında gerekmiyor, birkaç saat beklenebilir. Maliyeti düşürmek için en uygun seçenek hangisidir?
## A
Hepsini en yüksek öncelikli gerçek-zamanlı (senkron) uçtan tek tek koşmak
🤖 Gerçek-zamanlı uç en pahalıdır; anında sonuç gerekmediğinde gereksiz prim ödersin.
## B
Yüz bin dokümanın tamamını tek bir dev prompt'a doldurup tek istekte göndermek
🤖 Token sayısı aynı kalır, üstelik bağlam sınırını aşar ve kalite düşer; indirim de yok.
## C
Her doküman için büyük modelde üç kez koşup en iyi cevabı seçmek
🤖 Üç kat token ve büyük model maliyeti artırır; ertelenebilir yük için batch daha uygun.
## D
Batch API kullanmak: ertelenebilir toplu işler tipik olarak ~%50 indirimle işlenir
🤖 Doğru: anında sonuç gerekmeyen toplu iş için batch uç önemli bir indirim sağlar.
## Doğru
D
## Açıklama
Sonucun anında gerekmediği **toplu (batch), ertelenebilir** işler için sağlayıcıların **batch API**'si idealdir: istekler bir kuyruğa alınır, belirli bir pencere içinde (tipik olarak saatler) işlenir ve gerçek-zamanlı uca göre önemli bir indirim (yaygın olarak ~%50) sunar. 100.000 dokümanı gecelik özetlemek tam bu profildir. Gerçek-zamanlı uçtan tek tek koşmak (A) en pahalı yoldur. Hepsini tek dev prompt'a doldurmak (B) yaygın bir yanılgıdır: token sayısı azalmaz, bağlam penceresi aşılır ve uzun bağlamda kalite düşer — indirim de gelmez. Üç kez koşup en iyisini seçmek (C) maliyeti üçe katlar. Kural: gecikme toleransını maliyete çevir, beklemeye razı olduğun işi batch'e taşı.

# finops-5 | İleri
## Soru
Ekip "maliyetimiz yüksek ama nereden geldiğini bilmiyoruz" diyor. İlk yapılması gereken nedir?
## A
İstek, kullanıcı ve özellik başına token/$ kırılımını izlemeye almak; ölçmeden optimize etmemek
🤖 Doğru: ölçülmeyen maliyet kontrol edilemez; kırılım nereye harcandığını gösterir.
## B
En pahalı görünen özelliği tahminle seçip orada prompt'u kısaltmayı denemek
🤖 Ölçüm olmadan optimizasyon körlemesinedir; yanlış yeri iyileştirip kaynağı kaçırırsın.
## C
Modeli hemen en küçüğe düşürüp kalite etkisini ölçmeden yayına almak
🤖 Ölçmeden kaliteyi feda etmek regresyon riski taşır; önce nereye harcandığını gör.
## D
Aylık toplam faturayı takip edip bütçe aşılınca istek sayısına kota koymak
🤖 Kota kullanıcıyı keser ama maliyetin kaynağını göstermez; toplam fatura kırılım vermez.
## Doğru
A
## Açıklama
FinOps'un birinci kuralı **ölçmeden optimize etmemektir**: istek, kullanıcı, özellik ve model başına **token ve $ metriklerini** izlemeye almak (maliyet observability). Kırılım olmadan yüksek faturanın kaynağı görünmez — genelde tek bir özellik veya birkaç ağır kullanıcı maliyetin çoğunu üretir (Pareto). Kırılımı gördükten sonra hedefli optimizasyon (caching, routing, bağlam budama, batch) uygulanır. Tahminle bir yeri optimize etmek (B) körlemesinedir; körü körüne model küçültmek (C) kaliteyi gereksiz feda eder; toplam faturaya bakıp kota koymak (D) semptomu bastırır ama nedeni gizler ve kullanıcı deneyimini bozar. "Ölçülmeyen maliyet kontrol edilemez" ilkesi, observability ilkesiyle aynıdır.

# finops-6 | Uzman
## Soru
Bir ekip A sağlayıcısında prompt caching ile maliyeti belirgin düşürdü ve aynı prompt kurgusuyla B sağlayıcısına geçti; tasarruf kayboldu. En doğru açıklama nedir?
## A
Prompt caching tüm sağlayıcılarda aynı çalışır; tasarruf kaybı mutlaka koddaki bir hatadır
🤖 Caching standart bir davranış değildir; sağlayıcılar arasında sözleşmesi ciddi biçimde farklıdır.
## B
Önbellek istemci tarafında tutulur; sağlayıcı değişimi bunu etkilemez, ölçüm yanlıştır
🤖 Önbellek sağlayıcı tarafındadır; istemcide tutulan kopya faturayı hiç değiştirmez.
## C
Caching sağlayıcıya özeldir: işaretleme, asgari ön-ek boyutu, TTL ve fiyat farklıdır; yeniden ölçülür
🤖 Doğru: her sağlayıcının önbellek sözleşmesi ayrıdır, kazanç taşınabilir bir varsayım değildir.
## D
Önbellek yalnız çıktı token'ını ucuzlatır; B'de çıktı fiyatı yüksek olduğu için tasarruf kayboldu
🤖 Önbellek girdi ön-ekini ucuzlatır; çıktı token'ı hiçbir sağlayıcıda önbelleklenmez.
## Doğru
C
## Açıklama
**Prompt caching bir standart değil, sağlayıcıya özgü bir özelliktir.** Farklılaşan noktalar: önbelleğin nasıl işaretlendiği (bazı sağlayıcılarda açık bir işaret gerekir, bazılarında otomatiktir), **önbelleklenebilir asgari ön-ek boyutu** (modele göre 512–4096 token arasında değişir; bunun altındaki ön-ek sessizce önbelleğe alınmaz), önbelleğin yaşam süresi (TTL), yazma işleminin ek maliyeti (tipik olarak temel fiyatın 1,25–2 katı) ve okuma indiriminin oranı. Bu yüzden bir sağlayıcıda kanıtlanmış tasarruf başka sağlayıcıya taşınmaz; kazanç orada yeniden ölçülür ve prompt kurgusu gerekiyorsa yeniden düzenlenir. Caching'in her yerde aynı olduğunu varsaymak (A) tam da bu hatadır; önbellek istemcide değil sağlayıcıdadır (B); ve önbellek **girdi ön-ekini** ucuzlatır, çıktı token'ını değil (D).
