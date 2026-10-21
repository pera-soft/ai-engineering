---
slug: spring-ai-rag
baslik: "Spring AI ile RAG: VectorStore, ETL, MCP"
kademe: İleri
sira: 94
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Açık kitap sınavına giren bir öğrenci düşün. Her şeyi ezberlemiş olması gerekmez; soruyu okur, kitapta ilgili sayfayı bulur ve cevabı o sayfaya dayandırır. Kapalı kitap sınavında hatırlamadığını uydurma riski vardır — açık kitapta ise kaynağını gösterebilir. RAG tam olarak budur: modele kendi eğitim verisinde olmayan güncel veya kuruma özel bilgiyi, **soru sorulduğu anda ilgili belge parçalarını bularak** vermek. Bu hem güncelliği sağlar, hem halüsinasyonu azaltır, hem de kaynak göstermeyi mümkün kılar.

### Temel kavramlar

- **LLM**: Verilen metnin devamını üreten büyük dil modeli. Bilgisi eğitim kesim tarihinde donmuştur ve kurum-içi gizli verileri bilmez.
- **Halüsinasyon**: Modelin emin bir tonla yanlış bilgi uydurması.
- **Bağlam (context)**: Modele o istekle birlikte verilen metin. Erişilen belge parçaları buraya eklenir.
- **Sayısal vektör temsili (embedding / gömme)**: Bir metnin anlamını temsil eden sayı dizisi. Yakın anlamlı metinler yakın vektörlere düşer.
- **Benzerlik araması (similarity search)**: Sorgu vektörüne en yakın vektörleri bulma işlemi.
- **Parça (chunk)**: Uzun bir belgenin bölündüğü, tek başına vektörlenen anlamlı bölüm. Bölme işleminin adı **metni parçalara bölmedir** (chunking).
- **Overlap (örtüşme)**: Ardışık parçaların sınırda bir miktar ortak metin paylaşması; bağlamın kopmasını engeller.
- **Metadata (üst veri)**: Parçayla birlikte saklanan tanımlayıcı bilgi — `department`, `tenantId`, `dil`.
- **RAG (Retrieval-Augmented Generation / erişimle-zenginleştirilmiş üretim)**: Erişim + üretimi birleştiren mimari.

RAG bir donanım/performans optimizasyonu değildir, derleme süresiyle ilgisi yoktur, bağlantı havuzu yönetimiyle de alakası yoktur. Kritik nokta: cevap kalitesi doğrudan **erişim kalitesine** bağlıdır.

### Nasıl çalışır?

Spring AI'da RAG iki hattan oluşur.

**1. İndeksleme (offline, ETL pipeline).** Klasik "oku → dönüştür → yaz" hattıdır ve üç rol birbirinden ayrıdır, zincirlemedir, yeniden çalıştırılabilir:

- **`DocumentReader`**: Kaynaktan belgeleri okur ve `Document` nesnelerine dönüştürür (PDF, Markdown, HTML, JSON okuyucuları vardır).
- **`DocumentTransformer`**: Belgeleri işler — **metni parçalara bölme** (chunking), metadata ekleme/zenginleştirme, temizleme.
- **`DocumentWriter`**: Sonucu hedefe yazar; RAG'da bu tipik olarak bir `VectorStore`'dur ve yazarken parçalar `EmbeddingModel` ile vektörleştirilir.

**2. Sorgu (online).** Kullanıcı sorusu önce `EmbeddingModel` ile vektöre çevrilir. Sonra `VectorStore`'da benzerlik aramasıyla en yakın parçalar bulunur. `QuestionAnswerAdvisor` bu parçaları prompt'a bağlam olarak ekler ve `ChatModel` cevabı erişilen içeriğe dayandırarak üretir: `chatClient.prompt().advisors(new QuestionAnswerAdvisor(vectorStore)).user(...)`. Advisor bir writer değildir, cevabı bölmez, API anahtarı yönetmez — işi erişim ve enjeksiyondur.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="RAG: ETL indeksleme hattı ve sorgu hattı">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">RAG: indeksle (offline) + eriş & üret (online)</text>
  <text x="150" y="46" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B5791F" font-family="system-ui">ETL indeksleme</text>
  <rect x="24" y="56" width="86" height="48" rx="8" fill="#FBF1E3" stroke="#B5791F" stroke-width="1.4"/>
  <text x="67" y="80" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Reader</text>
  <text x="67" y="94" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">PDF/MD</text>
  <rect x="132" y="56" width="86" height="48" rx="8" fill="#FBF1E3" stroke="#B5791F" stroke-width="1.4"/>
  <text x="175" y="76" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Transformer</text>
  <text x="175" y="90" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">chunk+meta</text>
  <rect x="240" y="56" width="86" height="48" rx="8" fill="#FBF1E3" stroke="#B5791F" stroke-width="1.4"/>
  <text x="283" y="76" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Writer</text>
  <text x="283" y="90" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">embedding</text>
  <path d="M110 80 L130 80" stroke="#5D7789" stroke-width="1.4" marker-end="url(#ragArrow)"/>
  <path d="M218 80 L238 80" stroke="#5D7789" stroke-width="1.4" marker-end="url(#ragArrow)"/>
  <rect x="360" y="46" width="120" height="68" rx="10" fill="#F3ECFA" stroke="#6B4CA3" stroke-width="1.6"/>
  <text x="420" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#6B4CA3" font-family="system-ui">VectorStore</text>
  <text x="420" y="94" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">PGVector/Redis/Chroma</text>
  <path d="M326 80 L358 80" stroke="#5D7789" stroke-width="1.4" marker-end="url(#ragArrow)"/>
  <text x="150" y="146" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Sorgu hattı</text>
  <rect x="24" y="156" width="120" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="84" y="180" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Soru → Embedding</text>
  <text x="84" y="196" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">EmbeddingModel</text>
  <rect x="176" y="156" width="150" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="251" y="176" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">QuestionAnswerAdvisor</text>
  <text x="251" y="192" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">benzerlik + metadata filtre</text>
  <rect x="360" y="156" width="120" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="420" y="180" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ChatModel</text>
  <text x="420" y="196" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kaynağa dayalı cevap</text>
  <path d="M144 182 L174 182" stroke="#5D7789" stroke-width="1.4" marker-end="url(#ragArrow)"/>
  <path d="M326 182 L358 182" stroke="#5D7789" stroke-width="1.4" marker-end="url(#ragArrow)"/>
  <path d="M420 156 C420 130 420 130 420 116" stroke="#6B4CA3" stroke-width="1.4" stroke-dasharray="4 3" marker-end="url(#ragArrow)"/>
  <defs>
    <marker id="ragArrow" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="234" text-anchor="middle" font-size="11" font-weight="600" fill="#1C3247" font-family="system-ui">VectorStore soyutlaması: aynı kod, değişen depo (PGVector ↔ Redis ↔ Chroma)</text>
</svg>
```

**3. `VectorStore` soyutlaması.** Gömme vektörlerini **saklama** ve bir sorgu vektörüne en yakın parçaları **getirme** işlemlerini tek arayüzde toplar: `add(...)` / `similaritySearch(...)`. Altta **PGVector, Redis, Chroma, Milvus** gibi farklı vektör veritabanları takılabilir; değiştirmek bir bağımlılık/config meselesidir. Yani `ChatModel` soyutlamasının vektör dünyasındaki karşılığıdır. `VectorStore` modeli yeniden **eğitmez**, yalnızca bellek-içi bir liste değildir ve prompt şablonu saklamaz.

**4. Metni parçalara bölmek (chunking) bir takastır.** Belgeler bütün hâlde değil, anlamlı parçalara bölünür. Benzerlik araması vektör uzayında yapıldığı için **parça boyutu erişim isabetini belirler**. Çok büyük parçalar tek vektöre birçok farklı konuyu sıkıştırır; gürültü katar ve isabeti düşürür. Çok küçük parçalar ise bağlamı koparıp anlamı böler. İyi bir strateji boyut, örtüşme ve sınır seçimini birlikte dengeler. Parçalama bir disk tasarrufu yöntemi değildir, "gereksiz token silme" hiç değildir ve gömme vektörü olmadan vektör araması yapılamaz.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Çok büyük chunk gürültü katar, uygun chunk isabetli erişir, çok küçük chunk bağlamı koparır">
  <text x="320" y="22" text-anchor="middle" font-size="13.5" font-weight="700" fill="#1C3247" font-family="system-ui">Chunk boyutu takası: erişim isabeti ile bağlam bütünlüğü</text>
  <rect x="18" y="38" width="192" height="158" rx="12" fill="#FBEDED" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="114" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Çok büyük parça</text>
  <rect x="38" y="72" width="152" height="60" rx="6" fill="#FFFFFF" stroke="#8A2E2E" stroke-width="1.1"/>
  <text x="114" y="92" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">konu A + konu B</text>
  <text x="114" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ konu C (karışık)</text>
  <text x="114" y="124" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tek vektöre sıkışır</text>
  <text x="114" y="152" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">− gürültü,</text>
  <text x="114" y="168" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">düşük isabet</text>
  <rect x="224" y="38" width="192" height="158" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Uygun parça (+overlap)</text>
  <rect x="244" y="72" width="152" height="60" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="320" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tek tutarlı konu</text>
  <text x="320" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sınırda örtüşme</text>
  <text x="320" y="152" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">+ isabetli erişim,</text>
  <text x="320" y="168" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">bağlam bütün</text>
  <rect x="430" y="38" width="192" height="158" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="526" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Çok küçük parça</text>
  <rect x="450" y="72" width="46" height="26" rx="4" fill="#FFFFFF" stroke="#B26B00" stroke-width="1"/>
  <rect x="503" y="72" width="46" height="26" rx="4" fill="#FFFFFF" stroke="#B26B00" stroke-width="1"/>
  <rect x="556" y="72" width="46" height="26" rx="4" fill="#FFFFFF" stroke="#B26B00" stroke-width="1"/>
  <rect x="450" y="104" width="46" height="26" rx="4" fill="#FFFFFF" stroke="#B26B00" stroke-width="1"/>
  <rect x="503" y="104" width="46" height="26" rx="4" fill="#FFFFFF" stroke="#B26B00" stroke-width="1"/>
  <rect x="556" y="104" width="46" height="26" rx="4" fill="#FFFFFF" stroke="#B26B00" stroke-width="1"/>
  <text x="526" y="152" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">− bağlam kopar,</text>
  <text x="526" y="168" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">anlam bölünür</text>
</svg>
```

**5. Üretimde sık ihmal edilen iki kural.** Birincisi **gömme modeli tutarlılığıdır**. İndeksleme sırasında kullanılan gömme modeli ile sorgu anında kullanılan model **aynı** olmalıdır, çünkü her modelin vektör uzayı farklıdır. Model değişirse koleksiyonun tamamı **yeniden indekslenmelidir**.

İkincisi **metadata filtresidir**. Her parça yazılırken kaynağını tanımlayan metadata eklenir. Sorguda ise benzerlik aramasına bir filtre ifadesi verilir (ör. `department == 'finans'`); böylece erişim yalnız ilgili kümeyle sınırlanır.

**6. MCP (Model Context Protocol).** Modellere **araç, kaynak ve bağlam** sunmayı standartlaştıran açık bir protokoldür; farklı istemci ve sunucuların ortak bir dille araç paylaşmasını sağlar. Spring AI iki yönde destekler: uygulama bir **MCP istemcisi** olarak dış MCP sunucularının araçlarını (dosya sistemi, veritabanı, kurum-içi API'ler) `ChatClient`'ın tool-calling akışına katabilir; ya da kendi yeteneklerini bir **MCP sunucusu** olarak dışarı açabilir. Böylece araçlar tekil `@Tool` metotlarından çıkıp yeniden kullanılabilir, dilden bağımsız bir ekosisteme taşınır. MCP bir önbellek, şifreleme veya faturalama standardı değildir.

**Satıcı iddiasına şerh:** "RAG halüsinasyonu bitirir" abartıdır. Cevap **erişilen belgelerin kalitesine** bağımlıdır; kötü chunking, zayıf embedding veya alakasız erişim yanlış ama "kaynaklı görünen" cevaplar üretebilir. Erişim kalitesi (hybrid search, metadata filtre, yeniden sıralama/reranking) asıl mühendislik işidir.

### Mülakatta ne sorulur?

- **RAG neyi çözer?** Modelin bilmediği güncel/özel bilgiyi erişimle bağlama eklemek; cevabı kaynaklandırmak.
- **Boru hattı**: Reader okur → Transformer parçalar ve zenginleştirir → Writer (VectorStore) depolar; sorguda embedding → benzerlik araması → `QuestionAnswerAdvisor` → `ChatModel`.
- **Chunk takası**: çok büyük = gürültü ve düşük isabet; çok küçük = kopuk bağlam; overlap dengeler.
- **Embedding modeli tutarlılığı**: indeks ve sorgu aynı modeli kullanmalı; model değişimi **tam yeniden indeksleme** demektir.
- **Çok kiracılı (multi-tenant) izolasyon**: benzerlik aramasına **metadata filtresi** eklenir. Prompt'a "başka departmanın belgesini gösterme" yazmak güvenilmezdir — alakasız belgeler yine erişilir. Kullanıcı başına LLM eğitmek savurgandır; her şeyi tek parçada tutmak hem erişimi hem izolasyonu bozar. İzolasyon **erişim katmanında** yapılır.
- **`VectorStore`**: embedding saklama + benzerlik araması soyutlaması; depo değiştirilebilir.
- **MCP**: modele araç/bağlam sunmanın standardı; Spring AI hem istemci hem sunucu olabilir.

### Tek cümlede

RAG, belgeleri parçalayıp gömme vektörleri hâlinde bir `VectorStore`'a indeksleyen ve soru anında en ilgili parçaları prompt'a ekleyen bir mimaridir. Başarısı üç mühendislik ayrıntısına bağlıdır: parça boyutu, indeks ile sorguda **aynı gömme modelinin** kullanılması ve erişimi daraltan **metadata filtresi**.

# sairag-1 | Temel
## Soru
RAG (Retrieval-Augmented Generation) temelde hangi sorunu çözer?
## A
Modelin bilmediği güncel/özel bilgiyi erişimle bağlama eklemek
🤖 Doğru: RAG ilgili belgeleri çalışma zamanında getirir ve modelin cevabını onlara dayandırır.
## B
Modeli daha hızlı çalıştırmak için GPU ve bellek kullanımını azaltmak
🤖 RAG bir performans/donanım tekniği değil; bilgiyle zenginleştirme mimarisidir.
## C
Uygulamanın kod derleme süresini kısaltıp build'i hızlandırmak
🤖 Derlemeyle ilgisi yoktur; erişimle-zenginleştirilmiş üretimle ilgilidir.
## D
Veritabanı bağlantı havuzunu yönetip bağlantı sızıntılarını önlemek
🤖 Bağlantı havuzu ilgisiz bir konudur; RAG bilgi erişimiyle ilgilidir.
## Doğru
A
## Açıklama
**RAG**, bir LLM'in bilmediği (eğitim kesim tarihinden sonraki veya kuruma özel gizli) bilgiyi, **soruya en ilgili belge parçalarını çalışma zamanında bularak** modelin bağlamına eklemenin mimarisidir. Model cevabı bu erişilen kaynaklara dayandırır; bu hem güncelliği sağlar hem de halüsinasyonu azaltıp kaynak gösterebilmeyi mümkün kılar. RAG bir donanım/performans optimizasyonu (B), derleme (C) veya bağlantı havuzu (D) tekniği değildir. Kritik nokta: cevap kalitesi doğrudan **erişim kalitesine** bağlıdır — bu yüzden chunking ve arama tasarımı önemlidir.

# sairag-2 | Temel
## Soru
Spring AI'ın `VectorStore` soyutlaması ne sağlar?
## A
Modeli sizin verinizle otomatik olarak yeniden eğitir
🤖 Eğitim yapmaz; embedding'leri saklayıp benzerlik araması sunan bir depo soyutlamasıdır.
## B
Yalnızca bellek-içi bir listedir; kalıcı bir vektör veritabanı desteği bulunmaz
🤖 Tam tersine PGVector/Redis/Chroma gibi kalıcı depoları da tek arayüz altında toplar.
## C
Embedding'leri saklayıp benzerlik araması sunan depo soyutlamasıdır
🤖 Doğru: kod aynı kalır, altta PGVector/Redis/Chroma gibi depolar değiştirilebilir.
## D
Prompt şablonlarını dosya sisteminde saklayıp sürümler
🤖 Prompt şablonu ayrı bir kavram; `VectorStore` vektör depolama/arama içindir.
## Doğru
C
## Açıklama
`VectorStore`, gömme (embedding) vektörlerini **saklama** ve bir sorgu vektörüne en yakın parçaları **benzerlik aramasıyla getirme** işlemlerini tek bir arayüzde soyutlar. Böylece uygulama kodu `add(...)`/`similaritySearch(...)` ile konuşur; altta **PGVector, Redis, Chroma, Milvus** gibi farklı vektör veritabanları takılabilir ve değiştirmek bir bağımlılık/config meselesine iner (`ChatModel` soyutlamasının vektör dünyasındaki karşılığı). Model eğitimi (A), yalnızca bellek-içi olma (B) veya prompt şablonu saklama (D) `VectorStore`'un işi değildir. Bu soyutlama, RAG'ın "eriş" adımının taşınabilir kalmasını sağlar.

# sairag-3 | Orta
## Soru
Spring AI'da `QuestionAnswerAdvisor`'ın sorgu hattındaki rolü nedir?
## A
Belgeleri okuyup embedding olarak vektör deposuna yazan writer bileşenidir
🤖 Yazma işi `DocumentWriter`/`VectorStore`'a aittir; advisor sorgu anında erişim yapar.
## B
Sorguyla ilgili belgeleri erişip prompt'a bağlam olarak ekler
🤖 Doğru: `VectorStore`'da benzerlik aramasıyla bulunan parçaları isteme enjekte eder, cevabı kaynaklandırır.
## C
Modelin cevabını ikiye bölen bir ayrıştırıcıdır
🤖 Cevap bölme diye bir işi yoktur; bağlamı erişimle zenginleştirir.
## D
Sağlayıcının API anahtarını yönetip isteklere ekleyen bileşendir
🤖 Anahtar yönetimi yapılandırma işidir; advisor erişim/enjeksiyon yapar.
## Doğru
B
## Açıklama
`QuestionAnswerAdvisor`, Advisors zincirine takılan hazır bir RAG halkasıdır: gelen **kullanıcı sorusunu** alır, `VectorStore`'da **benzerlik araması** yaparak en ilgili belge parçalarını erişir ve bunları prompt'a **bağlam olarak enjekte eder**; ardından `ChatModel` cevabı bu erişilen içeriğe dayandırır. Böylece RAG'ın "eriş + zenginleştir" adımı tek bir advisor ile kurulur (`ChatClient.prompt().advisors(new QuestionAnswerAdvisor(vectorStore)).user(...)`). Belge yazma (A), cevap bölme (C) veya anahtar yönetimi (D) onun işi değildir; ayrıca metadata filtreleriyle erişim daraltılabilir.

# sairag-4 | Orta
## Soru
Belgeleri "chunking" (parçalama) ve ardından embedding'leme neden RAG'da kritiktir?
## A
Chunking yalnız disk yerinden tasarruf sağlar; erişim isabetini etkilemez
🤖 Tam ters: parça boyutu erişim isabetini doğrudan etkiler, salt disk meselesi değildir.
## B
Embedding olmadan da benzerlik araması yapılabilir; chunking gereksizdir
🤖 Benzerlik araması vektör (embedding) uzayında yapılır; embedding şarttır.
## C
Chunking, modeli hızlandırmak için gereksiz token'ları metinden siler
🤖 Token silme değil; anlamlı parçalara bölme ve her parçayı vektörleştirme söz konusudur.
## D
Uygun boyutlu parçalar benzerlik aramasının isabetini artırır
🤖 Doğru: çok büyük parça gürültü katar, çok küçük parça bağlamı koparır — boyut bir takastır.
## Doğru
D
## Açıklama
RAG'da belgeler bütün halde değil, **anlamlı parçalara (chunk)** bölünür ve her parça `EmbeddingModel` ile bir **vektöre** çevrilerek `VectorStore`'a yazılır. Sorgu anında benzerlik araması bu vektör uzayında yapıldığından, **parça boyutu erişim isabetini belirler**: çok büyük parçalar tek vektöre çok fazla farklı konuyu sıkıştırıp gürültü katar ve isabeti düşürür; çok küçük parçalar ise bağlamı koparıp anlamı bölebilir. İyi bir chunking stratejisi (boyut + örtüşme/overlap + sınır seçimi) bu takası dengeler. Chunking disk tasarrufu (A), token silme (C) değildir ve embedding olmadan vektör araması yapılamaz (B).

# sairag-5 | İleri
## Soru
Çok kiracılı (multi-tenant) bir RAG'da, bir kullanıcının yalnız kendi departmanının belgelerinden cevap alması nasıl sağlanır?
## A
Benzerlik aramasına metadata filtresi ekleyip erişimi sınırlamak
🤖 Doğru: her parça metadata'sıyla yazılır; sorgudaki filtre (ör. `department == 'finans'`) erişimi o kümeyle kısıtlar.
## B
Her kullanıcı veya departman için ayrı bir LLM eğitmek
🤖 Model eğitimi pahalı ve gereksiz; RAG'da izolasyon erişim katmanında yapılır.
## C
Prompt'a "başka departmanın belgesini gösterme" cümlesi eklemekle yetinmek
🤖 Yalnız talimat güvenilmez; alakasız belgeler yine erişilebilir. Filtre erişimde uygulanmalı.
## D
Tüm belgeleri tek bir parçada saklayıp modele bütün hâlde vermek
🤖 Tek parça hem erişimi hem izolasyonu bozar; ayrım metadata filtresiyle yapılır.
## Doğru
A
## Açıklama
İzolasyon ve alaka **erişim katmanında** sağlanır: indeksleme sırasında her belge parçası, kaynağını tanımlayan **metadata** ile yazılır (ör. `department`, `tenantId`, `dil`). Sorgu anında `VectorStore` benzerlik aramasına bir **metadata filtresi** (Spring AI'ın filter ifadesi, ör. `department == 'finans'`) eklenerek erişim yalnız ilgili parçalarla sınırlanır — hem güvenlik/kiracı izolasyonu hem de alaka artar. Kullanıcı başına model eğitmek (B) savurgan ve gereksizdir; prompt'a talimat yazmak (C) alakasız belgelerin erişilmesini engellemez (güvenilmez); her şeyi tek parçada tutmak (D) erişimi de bozar. Metadata filtresi, üretim RAG'ının temel taşıdır.

# sairag-6 | İleri
## Soru
Spring AI'ın ETL pipeline'ındaki üç ana rol (DocumentReader, DocumentTransformer, DocumentWriter) sırasıyla neyi yapar?
## A
Sırasıyla: modeli eğitir, cevabı üretir ve sonucu loglar
🤖 ETL bileşenleri model eğitimi/üretimi değil; belge okuma, dönüştürme ve yazma yapar.
## B
Üçü de aynı işi yapar; yalnızca isimleri ve paketleri farklıdır
🤖 Her rol farklı bir aşamadır: okuma, dönüştürme, yazma — birbirinin yerine geçmez.
## C
Sırasıyla: HTTP isteğini alır, JSON'a çevirir ve ilişkisel veritabanına yazar
🤖 Bu genel bir web akışı; ETL burada belge/RAG bağlamında okuma-dönüştürme-yazmadır.
## D
Reader okur; Transformer parçalar ve zenginleştirir; Writer depolar
🤖 Doğru: klasik "oku → dönüştür → yaz" hattı, RAG indekslemesinin belkemiğidir.
## Doğru
D
## Açıklama
Spring AI'ın **ETL (Extract-Transform-Load) pipeline'ı** RAG'ın offline indeksleme hattını üç rolle modeller: **DocumentReader** kaynaktan belgeleri okur ve `Document` nesnelerine dönüştürür (PDF, Markdown, HTML, JSON okuyucuları vardır); **DocumentTransformer** bu belgeleri işler — **chunking** (parçalama), metadata ekleme/zenginleştirme, temizleme; **DocumentWriter** ise sonucu hedefe yazar; RAG'da bu tipik olarak bir `VectorStore`'dur ve yazarken parçalar `EmbeddingModel` ile vektörleştirilir. Bu "oku → dönüştür → yaz" hattı yeniden çalıştırılabilir ve modülerdir. A, B, C bu rol ayrımını yanlış tanımlar; her aşama ayrı ve zincirlemedir.

# sairag-7 | İleri
## Soru
Spring AI ile MCP (Model Context Protocol) entegrasyonu ne sağlar?
## A
Vektör veritabanını RAM'de tutan bir önbellek katmanıdır
🤖 MCP bir önbellek değil; modele araç/bağlam sunmak için standart bir protokoldür.
## B
Modele araç ve bağlam sunmak için standart bir protokoldür
🤖 Doğru: Spring AI hem MCP istemcisi olarak dış araçları kullanabilir, hem kendi yeteneklerini MCP sunucusu olarak sunabilir.
## C
Yalnızca prompt'ları şifreleyen bir güvenlik katmanıdır
🤖 Şifreleme protokolü değil; araç/bağlam sağlama için ortak bir arayüz standardıdır.
## D
Sağlayıcılar arası fiyatları eşitleyen bir faturalama standardıdır
🤖 Faturalamayla ilgisi yok; model-araç etkileşimini standartlaştıran açık protokoldür.
## Doğru
B
## Açıklama
**MCP (Model Context Protocol)**, modellere **araçları, kaynakları ve bağlamı** standart bir arayüzle sunmak için tasarlanmış açık bir protokoldür; farklı istemci ve sunucuların ortak bir dille araç paylaşmasını sağlar. Spring AI bunu iki yönde destekler: uygulama bir **MCP istemcisi** olarak dış MCP sunucularının sunduğu araçları (dosya sistemi, veritabanı, kurum-içi API'ler) `ChatClient`'ın tool-calling akışına dahil edebilir; ya da kendi yeteneklerini bir **MCP sunucusu** olarak dışarı açabilir. Bu, tool-calling'i tekil `@Tool` metotlarından, yeniden kullanılabilir ve dilden bağımsız bir ekosisteme taşır. MCP bir önbellek (A), şifreleme (C) veya faturalama (D) standardı değildir; RAG'ı ve ajan mimarilerini besleyen bir bağlam/araç köprüsüdür.
