---
slug: java-ai-ekosistem
baslik: "JVM'de AI: Spring AI vs LangChain4j vs Direkt API, Streaming ve Yapılandırılmış Çıktı"
kademe: Orta
sira: 185
etiketler: 7
---

## Konu anlatımı

### Bu ne işe yarar?

**LLM** (Large Language Model — büyük dil modeli), verdiğin metni okuyup metin üreten bir servistir. Ona gönderdiğin metnin tamamına **istem (prompt)**, metnin bölündüğü küçük parçalara **jeton (token)** denir. Bu bölüm, o servisi bir Java/JVM uygulamasına bağlamanın yollarını ve hangisini ne zaman seçeceğini anlatır.

Soru "hangi kütüphane daha havalı" değildir. Soru şudur: bağımlılık, kontrol ve sağlayıcı değiştirme özgürlüğü arasındaki dengeyi nerede kuracaksın?

### Temel kavramlar

- **Direkt sağlayıcı API'si**: sağlayıcının HTTP ucunu veya resmî SDK'sını doğrudan çağırmak. En az bağımlılık ve tam kontrol verir; ama **yeniden deneme (retry)**, akıtma, yapılandırılmış çıktı ve RAG gibi her şeyi kendin yazarsın.
- **Spring AI**: Spring ekosistemine yerleşik soyutlama. `ChatClient` tek bir arayüz sunar. Starter'lar ve **auto-config** sayesinde sağlayıcı, yapılandırma dosyasından seçilir. `advisor`'lar RAG ve bellek ekler. Yeniden deneme, metrikler ve **izleme kayıtları (tracing)** Spring'in **gözlemlenebilirlik (observability)** altyapısına bağlanır.
- **LangChain4j**: Spring'e bağlı olmayan, çatı-bağımsız (framework-bağımsız) kütüphane. `ChatModel` düşük seviyeli arayüzdür. `AiServices` ile açıklama (annotation) taşıyan bir Java arayüzü yazarak bildirimsel kullanım sağlanır; RAG ve araç çağrısı hazır gelir.
- **Sağlayıcı soyutlaması**: LLM erişimini tek bir arayüz (port) arkasına koymak. İş mantığı sağlayıcıyı tanımaz, değişim tek bir adaptöre izole olur ve satıcıya kilitlenme (vendor lock-in) azalır.
- **Akıtma (streaming)**: jetonlar üretildikçe parça parça istemciye göndermek.
- **Yapılandırılmış çıktı (structured output)**: modeli bir JSON şemasına veya Java record'una uydurup tip-güvenli nesne almak.
- **RAG (Retrieval-Augmented Generation)**: modelin eğitiminde olmayan bilgiyi, soruya benzeyen belgeleri geri getirip isteme ekleyerek yanıtlatmak.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Direkt API, Spring AI ve LangChain4j karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç yol: kontrol ve soyutlama dengesi</text>
  <rect x="24" y="44" width="192" height="146" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="120" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Direkt API</text>
  <text x="120" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">en az bağımlılık</text>
  <text x="120" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tam kontrol</text>
  <text x="120" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her şeyi elle yazarsın</text>
  <text x="120" y="162" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">basit tek çağrı</text>
  <rect x="224" y="44" width="192" height="146" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Spring AI</text>
  <text x="320" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Spring'e yerleşik</text>
  <text x="320" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ChatClient + advisors</text>
  <text x="320" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">auto-config, observability</text>
  <text x="320" y="162" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Spring Boot projesi</text>
  <rect x="424" y="44" width="192" height="146" rx="11" fill="#F4EDF7" stroke="#7A3E9D" stroke-width="1.5"/>
  <text x="520" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#7A3E9D" font-family="system-ui">LangChain4j</text>
  <text x="520" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">framework-bağımsız</text>
  <text x="520" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">AiServices, RAG, tool</text>
  <text x="520" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bildirimsel arayüz</text>
  <text x="520" y="162" text-anchor="middle" font-size="10.5" fill="#7A3E9D" font-family="system-ui">Spring dışı / taşınabilir</text>
</svg>
```

### Nasıl çalışır?

Seçim kuralı sadedir. Uygulama zaten **Spring Boot** ise Spring AI en az sürtünmeyi verir: `ChatClient`, auto-config ve observability hazır gelir. Uygulama Spring kullanmıyorsa veya taşınabilir bir kütüphane yazıyorsan **LangChain4j** doğal seçimdir. İhtiyaç tek bir basit çağrıysa ve yeni bağımlılık istemiyorsan direkt API yeterlidir. Hangi yolu seçersen seç **çağrıyı tek bir soyutlama arkasında tut**: bu projede `ai/` paketi ve ArchUnit kuralları LLM çağrısını tek yere hapseder, sağlayıcı OpenRouter üzerinden Spring AI ile soyutlanır (ADR-0004).

**Akıtma (streaming)** uzun yanıtlarda vazgeçilmezdir. Toplam süre aynı kalsa bile kullanıcı ilk jetonu hemen gördüğü için **algılanan gecikme (perceived latency)** çarpıcı biçimde düşer. JVM'de tipik yol reaktif bir akıştır: Spring AI'da `chatClient…stream()` bir `Flux<String>` döner ve Server-Sent Events ile tarayıcıya akıtılır. LangChain4j'nin akıtma API'si de aynı işi yapar.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Streaming ilk token'ı erken gösterir, algılanan gecikme düşer">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Streaming: algılanan gecikme neden düşer</text>
  <text x="24" y="66" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">Tüm yanıtı bekle</text>
  <rect x="160" y="52" width="330" height="22" rx="5" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="325" y="67" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kullanıcı boş ekrana bakar…</text>
  <circle cx="490" cy="63" r="6" fill="#25804C"/>
  <text x="325" y="94" text-anchor="middle" font-size="10" fill="#8A2E2E" font-family="system-ui">ilk görüntü ancak sonda gelir</text>
  <text x="24" y="132" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Streaming (Flux)</text>
  <rect x="160" y="118" width="330" height="22" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <circle cx="184" cy="129" r="6" fill="#186399"/>
  <circle cx="240" cy="129" r="5" fill="#186399" opacity="0.7"/>
  <circle cx="296" cy="129" r="5" fill="#186399" opacity="0.7"/>
  <circle cx="352" cy="129" r="5" fill="#186399" opacity="0.7"/>
  <text x="184" y="160" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">ilk token erken</text>
  <text x="360" y="133" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">token'lar akar →</text>
  <text x="320" y="188" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Toplam süre ≈ aynı; değişen, ilk çıktının ne zaman göründüğü.</text>
</svg>
```

**Yapılandırılmış çıktı**, LLM cevabını kodda güvenle kullanmanın standart yoludur. Yanıtı `String` alıp regex veya `split` ile ayrıştırmak kırılgandır; biçim en ufak saptığında patlar. Bunun yerine modele hedef şema verilir ve yanıt doğrudan nesneye eşlenir: Spring AI'da `…entity(Fatura.class)`, LangChain4j'de `AiServices` arayüzünün dönüş tipini bir record yapmak. Sıcaklığı (temperature) 0'a çekmek geçerli JSON garantisi vermez — garanti şema kısıtından gelir.

**RAG** ile model, eğitiminde hiç görmediği güncel veya şirkete özel bilgiyi yanıtlayabilir. Akış şöyledir: soru bir **sayısal vektör temsiline (embedding)** çevrilir, **vektör deposunda** benzerliğe göre en yakın K metin parçası çekilir ve bu parçalar bağlam olarak isteme eklenir. Spring AI'da `VectorStore` ve `QuestionAnswerAdvisor`, LangChain4j'de RAG bileşenleri bunu sağlar. Modeli her gece yeniden eğitmeye (fine-tune) gerek yoktur.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="RAG akışı: soru gömülür, vektör deposundan bağlam çekilir, LLM yanıtlar">
  <defs>
    <marker id="javaai-flow" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">RAG: gömme + vektör deposu ile ilgili bağlamı getir</text>
  <rect x="16" y="78" width="96" height="56" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="64" y="104" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Soru</text>
  <text x="64" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">metin</text>
  <line x1="112" y1="106" x2="140" y2="106" stroke="#5D7789" stroke-width="1.5" marker-end="url(#javaai-flow)"/>
  <rect x="144" y="78" width="120" height="56" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="204" y="102" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">Embedding</text>
  <text x="204" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">vektöre çevir</text>
  <line x1="264" y1="106" x2="292" y2="106" stroke="#5D7789" stroke-width="1.5" marker-end="url(#javaai-flow)"/>
  <rect x="296" y="78" width="140" height="56" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="366" y="102" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Vektör deposu</text>
  <text x="366" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">benzerlik → top-K</text>
  <line x1="436" y1="106" x2="464" y2="106" stroke="#5D7789" stroke-width="1.5" marker-end="url(#javaai-flow)"/>
  <rect x="468" y="78" width="120" height="56" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="528" y="102" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">LLM</text>
  <text x="528" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">soru + bağlam</text>
  <text x="320" y="168" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Model, eğitiminde olmayan güncel/özel bilgiyi getirilen bağlamdan yanıtlar.</text>
</svg>
```

Son bir karar var: açık ağırlıklı (open-weight) bir modeli **kendi sunucunda çalıştırmak (self-host)** mı, yönetilen bir API kullanmak mı? Bu ideoloji değil bir **takastır**. Beş şey tartılır: GPU ve donanım maliyeti, gecikme (latency), ölçeklenebilirlik, işletme yükü (güncelleme, izleme, güvenlik) ve veri gizliliği/uyumluluk gereksinimleri. Hangi yol seçilirse seçilsin, JVM tarafında erişim yine soyutlamanın arkasında durur.

### Mülakatta ne sorulur?

Sık sorulanlar: "Spring AI mı LangChain4j mi, neden?" "ChatClient neyi soyutlar?" "Algılanan gecikmeyi nasıl düşürürsün?" (akıtma). "LLM cevabını bir record'a nasıl güvenle eşlersin?" (yapılandırılmış çıktı). "Güncel şirket dokümanlarını modele nasıl bağlarsın?" (RAG). "Modeli kendi sunucunda çalıştırmaya ne zaman geçersin?"

Yanlış cevap kalıpları: sıcaklığı 0 yapıp biçim garantisi ummak, "JVM'de olmaz, Python şart" demek, sağlayıcının SDK'sını iş mantığının her yerine yaymak.

### Tek cümlede

JVM'de LLM entegrasyonu bir kütüphane savaşı değil bağlam seçimidir. Spring Boot projesinde Spring AI, çatı-bağımsız işte LangChain4j, basit ihtiyaçta direkt API kullanılır; her durumda akıtma, yapılandırılmış çıktı ve RAG tek bir sağlayıcı soyutlaması arkasında kurulur.

# javaai-1 | Orta
## Soru
Mevcut bir Spring Boot uygulamasına LLM sohbet özelliği ekleyeceksin; sağlayıcıyı ileride değiştirebilmek, auto-config ve observability entegrasyonu istiyorsun. En doğal seçim hangisidir?
## A
Her sağlayıcı için ayrı bir ham HTTP istemcisi yazıp yapılandırmayı elle yönetmek
🤖 Ham HTTP her sağlayıcı için tekrar iş demek; Spring entegrasyonunu da elle kurarsın.
## B
Uygulamayı Spring'den çıkarıp saf Java'ya taşıdıktan sonra bir AI kütüphanesi eklemek
🤖 Var olan Spring Boot uygulamasını terk etmek gereksiz maliyet; Spring AI bu bağlama oturur.
## C
Spring AI kullanmak: ChatClient soyutlaması, auto-config ve observability Spring Boot'a yerleşiktir
🤖 Doğru: Spring bağlamında ChatClient, auto-config ve provider soyutlaması en az sürtünmeyle gelir.
## D
Modeli uygulama JVM'inin içine gömüp hiçbir dış servis çağrısı yapmamak
🤖 Yerinde model barındırma ayrı bir maliyet; sohbet ihtiyacı için aşırı mühendislik olur.
## Doğru
C
## Açıklama
Var olan bir **Spring Boot** uygulamasında sağlayıcı bağımsızlığı, auto-config ve observability isteniyorsa doğal seçim **Spring AI**'dır: `ChatClient` tek bir soyutlama sunar, `spring-ai-*` starter'ları sağlayıcıyı yalnız yapılandırmayla değiştirmeyi sağlar, retry/metrik/tracing Spring'in observability altyapısına bağlanır. Her sağlayıcı için ham HTTP yazmak (A) tekrarlı emek ve eksik soyutlamadır; uygulamayı Spring'den çıkarmak (B) gereksiz yıkımdır; modeli JVM'e gömmek (D) bir sohbet özelliği için aşırı mühendisliktir ve GPU/bakım maliyeti getirir. İlke: framework zaten Spring ise, o ekosistemin AI soyutlamasını kullanmak en az sürtünmeyi verir.

# javaai-2 | Orta
## Soru
Spring kullanmayan, taşınabilir olması istenen bir Java kütüphanesine RAG ve tool-calling içeren bir ajan ekleyeceksin. Hangisi en uygun?
## A
LangChain4j: framework-bağımsız, AiServices ile bildirimsel arayüz, RAG ve tool-calling sunar
🤖 Doğru: taşınabilir ve framework-bağımsız ihtiyaçta LangChain4j doğrudan oturur.
## B
Spring AI; proje Spring kullanmasa da starter'ları zorlayarak eklemek
🤖 Spring AI Spring ekosistemine yaslanır; taşınabilir bir kütüphanede gereksiz bağımlılık olur.
## C
RAG, tool-calling ve bellek altyapısını hiç kütüphane kullanmadan sıfırdan yazmak
🤖 Sıfırdan yazmak çok emek ve geniş hata yüzeyi; olgun kütüphane varken gereksiz.
## D
Python'a geçmek; JVM'de tool-calling ve RAG olgun biçimde yapılamaz
🤖 JVM'de LangChain4j ve Spring AI bunları destekler; dil değiştirmek gerekmez.
## Doğru
A
## Açıklama
Spring'e bağlı olmayan, **taşınabilir** bir kütüphanede RAG ve tool-calling gerekiyorsa **LangChain4j** en uygundur: framework-bağımsız, sağlayıcı-bağımsız, `AiServices` ile bildirimsel LLM arayüzleri, hazır RAG ve tool/function-calling desteği sunar. Spring AI (B) Spring ekosistemine yaslanır; Spring kullanmayan bir kütüphaneye starter zorlamak gereksiz bağımlılık ve sürtünme yaratır. Her şeyi sıfırdan yazmak (C) olgun kütüphane varken gereksiz emek ve risktir. Python'a geçmek (D) yanlıştır — JVM bu yetenekleri tam destekler. Seçim ilkesi nettir: kütüphaneyi framework bağlamına (Spring var mı yok mu) ve taşınabilirlik ihtiyacına göre seç.

# javaai-3 | Orta
## Soru
Kullanıcıya uzun bir LLM yanıtı gösterirken algılanan gecikmeyi (perceived latency) azaltmanın JVM'deki tipik yolu nedir?
## A
Daha küçük bir model seçip cevabın kalitesini düşürerek toplam üretim süresini kısaltmak
🤖 Kaliteyi feda etmek çözüm değil; streaming tam yanıtı beklemeden akıtmayı sağlar.
## B
Yanıtın tamamı üretilene kadar bekleyip tek seferde ekrana basmak
🤖 Tümünü beklemek algılanan gecikmeyi maksimum yapar; streaming bunun tersidir.
## C
max_tokens sınırını düşürüp cevabı kısaltmak; kısa cevap daha erken görünür
🤖 Cevabı kesmek içerik kaybettirir; ilk token yine ancak üretim bitince görünür.
## D
Streaming: token'lar üretildikçe reaktif akışla (Flux/SSE) kullanıcıya akıtmak
🤖 Doğru: streaming ilk token'ı erken gösterir, algılanan gecikme belirgin düşer.
## Doğru
D
## Açıklama
**Streaming**, token'ları model üretir üretmez istemciye akıtmaktır; JVM'de tipik olarak reaktif bir akışla yapılır (`Flux<String>` ve Server-Sent Events). Kullanıcı ilk token'ı saniyeler sonra değil neredeyse anında görür; toplam süre aynı kalsa bile **algılanan gecikme** belirgin düşer ve deneyim akıcılaşır. Tam yanıtı beklemek (B) algılanan gecikmeyi maksimuma çıkarır; model küçültmek (A) kaliteyi gereksiz feda eder; `max_tokens` düşürmek (C) yaygın bir karıştırmadır — cevabı kısaltır, içerik kaybettirir ve ilk token'ın görünme anını değiştirmez. Hem Spring AI (`stream()`) hem LangChain4j hem de direkt API bu akışı destekler; uzun yanıtlı sohbet arayüzlerinde streaming neredeyse zorunlu bir UX kaldıracıdır.

# javaai-4 | Orta
## Soru
LLM'den gelen yanıtı `String` olarak alıp elle regex/split ile ayrıştırmak yerine, doğrudan bir Java kaydına (record) güvenilir biçimde eşlemek istiyorsun. Hangi yetenek bunu sağlar?
## A
Sıcaklığı 0 yapmak; deterministik çıktı kendiliğinden geçerli JSON ve nesne olur
🤖 Determinizm biçim garantisi vermez; deterministik ama bozuk JSON da mümkündür.
## B
Yapılandırılmış çıktı: modeli şemaya uydurup yanıtı tip-güvenli record'a eşlemek
🤖 Doğru: structured output modeli şemaya bağlar ve çıktıyı doğrudan record'a eşler.
## C
Yanıtı String alıp kendi yazdığın regex/split ayrıştırıcısıyla alanlara bölmek
🤖 Elle ayrıştırma kırılgandır ve tam da kaçınılmak istenen yöntemdir.
## D
Modelden JSON istemek ve ayrıştırma hatasında aynı isteği sınırsız kez tekrarlamak
🤖 Tekrar denemek maliyet ve latency ekler; şema kısıtı olmadan hata tekrar eder.
## Doğru
B
## Açıklama
**Yapılandırılmış çıktı (structured output)**, modeli bir JSON şemasına veya hedef Java tipine (record/POJO) uymaya zorlayıp yanıtı doğrudan **tip-güvenli nesneye** eşler. Spring AI'da `ChatClient … .entity(MyRecord.class)`, LangChain4j'de `AiServices` arayüzünün dönüş tipini record yapmak bunu sağlar; string ayrıştırmanın kırılganlığını (kaçan alan, biçim sapması) ortadan kaldırır. Sıcaklığı 0 yapmak (A) yaygın bir yanılgıdır: determinizm biçim garantisi değildir, deterministik ama şemaya uymayan bir çıktı da mümkündür. Elle ayrıştırma (C) tam da kaçınılmak istenen yöntemdir. "Bozuksa tekrar dene" (D) maliyeti ve gecikmeyi artırır, hatanın kaynağını çözmez. Şema kısıtı, doğrulamayla birlikte hataları erken yakalar.

# javaai-5 | İleri
## Soru
Sohbet asistanının şirket içi güncel dokümanlara dayanarak yanıt vermesi gerekiyor; model bu dokümanları eğitiminde hiç görmedi. JVM'de en uygun yaklaşım nedir?
## A
Tüm doküman arşivini her istekte sistem prompt'una ekleyip modele baştan sona okutmak
🤖 Arşiv bağlam penceresine sığmaz; sığsa bile her istekte devasa token maliyeti çıkar.
## B
Modeli her gece tüm dokümanlarla yeniden eğitmek (fine-tune) ve ağırlıkları güncellemek
🤖 Fine-tune pahalı ve yavaştır; üstelik belirli bir bilgiyi hatırlamayı garanti etmez.
## C
RAG: soruyu embedding'e çevirip vektör deposundan ilgili parçaları getirip bağlama eklemek
🤖 Doğru: RAG yalnız ilgili parçaları getirir; güncel bilgi eğitim gerektirmeden yanıtlanır.
## D
Sıcaklığı düşürüp modelin şirket dokümanlarını daha doğru hatırlamasını sağlamak
🤖 Sıcaklık modele yeni bilgi eklemez; hiç görmediği içeriği hatırlaması mümkün değildir.
## Doğru
C
## Açıklama
Modelin eğitiminde olmayan güncel veya şirkete özel bilgi için doğru desen **RAG (Retrieval-Augmented Generation)**'dir: dokümanlar parçalara bölünüp **embedding** (sayı vektörü) hâline getirilir ve bir **vektör deposunda** saklanır; soru geldiğinde soru da vektöre çevrilir, benzerliğe göre en yakın top-K parça çekilir ve bağlam olarak prompt'a eklenir. JVM'de bunu Spring AI (`VectorStore` + `QuestionAnswerAdvisor`) veya LangChain4j'nin RAG bileşenleri sağlar. Tüm arşivi prompt'a doldurmak (A) bağlam penceresine sığmaz ve sığsa bile maliyeti uçurur. Gecelik fine-tune (B) pahalı ve yavaştır, üstelik belirli bir gerçeği hatırlamayı garanti etmez — fine-tune davranış/biçim öğretmek içindir, bilgi tazelemek için değil. Sıcaklık (D) modele bilgi eklemez.

# javaai-6 | İleri
## Soru
Küçük, açık ağırlıklı (open-weight) bir modeli JVM tabanlı bir serviste barındırmayı (self-host) değerlendiriyorsun. Doğru mühendislik yaklaşımı nedir?
## A
Maliyet, latency, donanım, ölçek ve bakım yükünü API'ye karşı tartıp gereksinime göre seçmek
🤖 Doğru: self-host bir takas kararıdır; ölçülebilir kriterlerle karşılaştırılır.
## B
Self-host her durumda daha ucuzdur; yönetilen API asla tercih edilmemelidir
🤖 Self-host GPU, ölçek ve bakım maliyeti getirir; her durumda en iyi değildir.
## C
Model servisi JVM'den tüketilemez; bu iş için mutlaka Python'a geçmek gerekir
🤖 JVM bir çıkarım sunucusunu HTTP ile tüketebilir; Python zorunlu değildir.
## D
Barındırma kararı tamamen tercih meselesidir; maliyet ve latency ölçmeye gerek yoktur
🤖 Karar maliyet, latency ve bakım verileriyle verilir; keyfî bir tercih değildir.
## Doğru
A
## Açıklama
Açık ağırlıklı bir modeli **self-host** etmek bir **takas kararıdır**, ideoloji değil: GPU/donanım maliyeti, latency, ölçeklenebilirlik, operasyonel bakım (güncelleme, izleme, güvenlik) ve veri gizliliği/uyumluluk ihtiyaçları yönetilen API kullanımına karşı tartılır. Bazı durumlarda (gizlilik zorunluluğu, yüksek ve sabit hacim, düşük latency ihtiyacı) self-host kazanır; çoğu durumda yönetilen API daha az bakım ve daha hızlı başlangıç sunar. Hangi yol seçilirse seçilsin, JVM tarafında erişim yine bir **soyutlama** arkasında tutulur — yerel bir çıkarım sunucusunu HTTP ile tüketmek de buna dâhildir. "Her zaman self-host" (B), "JVM'de imkânsız" (C) veya "karar keyfî" (D) yanlıştır; karar ölçülebilir maliyet/latency/bakım verileriyle verilir.
