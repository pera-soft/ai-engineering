---
slug: spring-ai-temel
baslik: "Spring AI 1.0/1.1: ChatClient, Advisors, Tool Calling"
kademe: İleri
sira: 93
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Farklı ülkelerden gelen müşterilerle çalışan bir şirket düşün. Her müşterinin diline ayrı çalışan tutmak yerine tek bir tercüman bürosu ile anlaşırsın: sen Türkçe konuşursun, büro hangi dile çevireceğini kendi halleder ve büroyu değiştirmen iş akışını bozmaz. Spring AI bu bürodur. Uygulaman tek bir arayüzle "modele" konuşur; arkadaki sağlayıcının Anthropic mi OpenAI mı Ollama mı olduğu bir bağımlılık ve yapılandırma meselesine iner. Fikir tanıdıktır: `JdbcTemplate` veritabanı sağlayıcısını nasıl soyutluyorsa, Spring AI de model sağlayıcısını öyle soyutlar.

### Temel kavramlar

- **LLM (Large Language Model / büyük dil modeli)**: Çok büyük metin yığınından öğrenmiş, verilen metnin devamını olasılıklarla üreten model. Sohbet, özetleme, sınıflandırma bu üretimin biçimleridir.
- **Belirteç (token)**: Modelin metni işlerken kullandığı en küçük parça (kabaca bir hece ya da kelime parçası). Fiyatlandırma ve sınırlar bu birim üzerinden ölçülür.
- **Prompt (istem)**: Modele gönderilen girdi metni.
- **Prompt rolleri**: Bir prompt, rollerine göre **mesajlardan** oluşur — **sistem** (system: modelin davranış yönergesi), **kullanıcı** (user: asıl soru), **asistan** (assistant: modelin önceki cevabı). Sohbet geçmişi bu rollerle taşınır. Model istekler arasında **durum tutmaz**, bu yüzden sistem mesajı her istekte yeniden gönderilir.
- **Sayısal vektör temsili (embedding / gömme)**: Bir metnin anlamını temsil eden sayı dizisi. Benzer anlamlı metinler birbirine yakın vektörlere düşer.
- **Sağlayıcı (provider)**: Modeli sunan servis (Anthropic, OpenAI, Ollama, Azure, Vertex).

Spring AI'ın merkezindeki soyutlamalar:

- **`ChatModel` / `EmbeddingModel`**: Düşük seviyeli sağlayıcı nesneleri (bean — Spring'in üretip yönettiği nesne). Sırasıyla sohbet üretimi ve gömme vektörü üretimi yaparlar.
- **`ChatClient`**: Akıcı (fluent) ana giriş noktası; `WebClient`/`RestClient` benzeri bir deneyim: `chatClient.prompt().user("...").call().content()`. İstek kurma, mesajlar, seçenekler, araçlar ve advisor zinciri tek yerde toplanır. `ChatClient` alttaki `ChatModel`'i kullanır ama sağlayıcıdan bağımsız yüksek seviyeli arayüz verir; veritabanı istemcisi veya yük dengeleyici değildir.
- **Yapılandırılmış çıktı (structured output)**: `.entity(Rapor.class)`.
- **Advisors API**: İstek/yanıt hattına takılan araya-girici halkalar.
- **Tool/Function Calling (`@Tool`)**: Modele çağrılabilir araç sunma.
- **`PromptTemplate`**: `{konu}` gibi **yer tutucular (placeholder)** içeren şablon.

**Sürüm durumu:** Spring AI **1.0 GA Mayıs 2025**'te, **1.1 Kasım 2025**'te yayımlandı — 2026'da deneysel bir milestone değil, üretim için kararlı bir çerçevedir.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ChatClient akıcı API, advisor zinciri ve sağlayıcı soyutlaması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">ChatClient: tek akıcı hat, değişebilir sağlayıcı</text>
  <rect x="24" y="46" width="150" height="150" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="99" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">ChatClient</text>
  <text x="99" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">.prompt()</text>
  <text x="99" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">.user(...)</text>
  <text x="99" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">.tools(...)</text>
  <text x="99" y="150" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">.call()</text>
  <text x="99" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">.entity(Rapor.class)</text>
  <rect x="204" y="66" width="150" height="110" rx="10" fill="#FBF1E3" stroke="#B5791F" stroke-width="1.5"/>
  <text x="279" y="92" text-anchor="middle" font-size="12" font-weight="700" fill="#B5791F" font-family="system-ui">Advisor zinciri</text>
  <text x="279" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hafıza</text>
  <text x="279" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">loglama</text>
  <text x="279" y="150" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">QuestionAnswer (RAG)</text>
  <rect x="384" y="66" width="150" height="110" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="459" y="92" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">ChatModel</text>
  <text x="459" y="116" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Anthropic</text>
  <text x="459" y="134" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">OpenAI</text>
  <text x="459" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Ollama · Vertex</text>
  <rect x="560" y="86" width="56" height="70" rx="10" fill="#F3ECFA" stroke="#6B4CA3" stroke-width="1.5"/>
  <text x="588" y="116" text-anchor="middle" font-size="10.5" font-weight="700" fill="#6B4CA3" font-family="system-ui">@Tool</text>
  <text x="588" y="134" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">geri</text>
  <text x="588" y="147" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">besleme</text>
  <path d="M174 121 L200 121" stroke="#5D7789" stroke-width="1.5" marker-end="url(#saiArrow)"/>
  <path d="M354 121 L380 121" stroke="#5D7789" stroke-width="1.5" marker-end="url(#saiArrow)"/>
  <path d="M534 121 L556 121" stroke="#5D7789" stroke-width="1.5" marker-end="url(#saiArrow)"/>
  <defs>
    <marker id="saiArrow" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="222" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Sağlayıcıyı değiştirmek = bağımlılık + yapılandırma; uygulama kodu aynı kalır</text>
</svg>
```

### Nasıl çalışır?

**1. Yapılandırılmış çıktı ham metni tipe bağlar.** `chatClient.prompt().user(...).call().entity(Rapor.class)` yazdığında Spring AI modele **beklenen şema/format** yönlendirmesini ekler ve dönen içeriği hedef `record`/POJO'ya ayrıştırır. Bu, elle JSON ayrıştırma ve kırılgan string işlemeyi ortadan kaldırır, kodu tip-güvenli kılar. Jenerik koleksiyonlar için `ParameterizedTypeReference` desteklenir. Amaç şifreleme, token azaltma veya diske kaydetme değildir.

**2. Advisor zinciri hattı biçimlendirir.** Advisors API, `ChatClient` istek/yanıt akışına takılan **araya-girici (interceptor) halkalarından** oluşur — kavramsal olarak servlet filtresi veya AOP around-advice gibi. Her advisor isteği zenginleştirebilir (bağlama belge/hafıza ekleme), yanıtı işleyebilir veya çağrıyı loglayabilir. Hazır advisor'lar buradan gelir: sohbet hafızası, loglama ve RAG için **`QuestionAnswerAdvisor`** (vektör deposundan ilgili belgeleri çekip prompt'a ekler). Advisor pasif bir dinleyici, derleme-zamanı sağlayıcı seçici veya kalıcılık katmanı değildir; birinci sınıf bir genişletme noktasıdır.

**3. Tool calling modeli deterministik koda bağlar.** `@Tool` ile işaretlenen metot, açıklaması ve parametre şemasıyla modele bildirilir. Model cevap üretirken **aracın çağrılması gerektiğine karar verirse**, Spring AI metodu gerçekten yürütür ve dönen sonucu modele **geri besleyerek** nihai yanıtı ürettirir. Böylece model canlı veriye (hava durumu, DB sorgusu, hesaplama) dayanır. Bu bir önbellek, REST uç noktası (`@RequestMapping` işi) veya paralel yürütme mekanizması değildir; araç yalnız model istediğinde tetiklenir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tool calling döngüsü: model aracı çağırır, Spring AI yürütür, sonucu modele geri besler, model nihai yanıtı üretir">
  <text x="320" y="24" text-anchor="middle" font-size="13.5" font-weight="700" fill="#1C3247" font-family="system-ui">Tool calling döngüsü: model çağırır, Spring AI yürütür, sonuç geri döner</text>
  <rect x="24" y="80" width="140" height="64" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="94" y="116" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Kullanıcı</text>
  <rect x="240" y="80" width="160" height="64" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="108" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">ChatModel</text>
  <text x="320" y="126" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">araç gerekli mi?</text>
  <rect x="476" y="80" width="150" height="64" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="551" y="108" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">@Tool metodu</text>
  <text x="551" y="126" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Spring AI yürütür</text>
  <path d="M164 100 L238 100" stroke="#186399" stroke-width="1.5" marker-end="url(#sai2-ar)"/>
  <text x="201" y="93" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">soru</text>
  <path d="M238 126 L164 126" stroke="#25804C" stroke-width="1.5" marker-end="url(#sai2-ar)"/>
  <text x="201" y="140" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">nihai yanıt</text>
  <path d="M400 100 L474 100" stroke="#25804C" stroke-width="1.5" marker-end="url(#sai2-ar)"/>
  <text x="437" y="93" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">araç çağrısı</text>
  <path d="M474 126 L400 126" stroke="#6B3FA0" stroke-width="1.5" marker-end="url(#sai2-ar)"/>
  <text x="437" y="140" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">sonuç geri besleme</text>
  <text x="320" y="176" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Model deterministik kodu (DB, hava durumu, hesap) çağırıp yanıtı ona dayandırır</text>
  <defs>
    <marker id="sai2-ar" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

**4. Prompt şablonu prompt'u koddan ayırır.** `PromptTemplate`, `{konu}` yer tutucularını çalışma anında doldurur. Kazanç: prompt'lar merkezi, gözden geçirilebilir ve yeniden kullanılabilir olur; kırılgan string birleştirme ortadan kalkar (prompt'lar tipik olarak `prompts/` gibi bir kaynaktan gelir). Sağlayıcı soyutlamasıyla birleşince **aynı şablon** farklı `ChatModel` bean'lerine değişmeden verilebilir. Şablon prompt'u şifrelemez, otomatik çevirmez/kısaltmaz ve token maliyetini eşitlemez.

**5. Sağlayıcı değiştirmek.** OpenAI'dan Anthropic'e geçmek tipik olarak ilgili **starter bağımlılığını** (ör. `spring-ai-starter-model-anthropic`) eklemek ve **yapılandırmayı** (API anahtarı, model adı) ayarlamaktan ibarettir; uygulama kodu büyük ölçüde değişmez. Sağlayıcının kendi SDK'sına göre iş mantığını baştan yazmak soyutlamanın amacına aykırıdır; GraalVM native derleme veya veritabanı şeması bu konuyla ilgisizdir.

**Satıcı iddiasına şerh:** "Sağlayıcıyı tek satırla değiştir" pazarlaması temel sohbet için büyük ölçüde doğrudur. Ama **istem önbellekleme (prompt caching)**, uzun-bağlam davranışı, araç çağırma ayrıntıları ve fiyat/limitler sağlayıcıya göre değişir. Soyutlama **taşınabilirlik** verir, davranış eşitliği garanti etmez.

### Mülakatta ne sorulur?

- **`ChatClient` nedir?** LLM istekleri için akıcı API; `ChatModel`'in üstünde durur. Embedding üretimi `EmbeddingModel`'in işidir.
- **Prompt rolleri**: sistem / kullanıcı / asistan; model durumsuzdur, geçmiş her istekte yeniden gönderilir.
- **`PromptTemplate`**: yer tutuculu, koddan ayrık, sağlayıcıdan bağımsız prompt.
- **Structured output**: `.entity(...)` şema yönlendirmesi + ayrıştırma ile tip güvenliği.
- **Advisors**: interceptor zinciri — hafıza, loglama, `QuestionAnswerAdvisor` (RAG).
- **Tool calling**: model karar verir, Spring AI yürütür, sonuç modele geri beslenir.
- **Sürümler**: 1.0 GA **Mayıs 2025**, 1.1 **Kasım 2025**.
- **Prompt caching**: tekrar eden büyük bağlamın (sistem yönergesi, uzun belge) yeniden işlenmesini önleyen, **maliyet ve gecikme** optimizasyonudur. **Sağlayıcı/model-özeldir** — her sağlayıcıda birebir aynı ve otomatik değildir, yanıt **doğruluğunu artıran bir kalite özelliği de değildir**.

### Tek cümlede

Spring AI, `ChatClient` akıcı API'si, advisor zinciri, `@Tool` çağrıları ve prompt şablonlarıyla LLM entegrasyonunu sağlayıcıdan bağımsız hâle getirir. Taşınabilirlik yüksektir; ama maliyet ve önbellekleme gibi davranışlar sağlayıcıya bağlı kalır.

# sai-1 | Temel
## Soru
Spring AI'da `ChatClient` ne sağlar?
## A
Doğrudan JDBC bağlantısı açan bir veritabanı istemcisidir
🤖 `ChatClient` veritabanı değil, LLM sohbet çağrılarını kuran akıcı bir API'dir.
## B
Yalnızca embedding (gömme) vektörü üreten bir bileşendir
🤖 Gömme üretimi `EmbeddingModel`'in işidir; `ChatClient` sohbet üretimini yönetir.
## C
LLM istekleri için akıcı bir API sağlar
🤖 Doğru: prompt, mesaj, seçenek ve advisor'ları `chatClient.prompt().user(...).call().content()` zincirinde toplar.
## D
Uygulamayı otomatik ölçekleyen bir yük dengeleyicidir (load balancer)
🤖 Ölçekleme/dengelemeyle ilgisi yoktur; sohbet isteği soyutlamasıdır.
## Doğru
C
## Açıklama
`ChatClient`, Spring AI'ın **akıcı (fluent) ana giriş noktasıdır**: sistem/kullanıcı mesajlarını, model seçeneklerini, araçları (`@Tool`) ve advisor zincirini tek bir zincirlenmiş çağrıda toplar; örn. `chatClient.prompt().user("...").call().content()`. Bu, `WebClient`/`RestClient`'a benzer bir deneyim sunar. Embedding üretimi ayrı bir soyutlamadır (`EmbeddingModel`, B yanlış); JDBC (A) ve yük dengeleme (D) ise tamamen ilgisizdir. `ChatClient`, alttaki `ChatModel` bean'ini kullanır ama geliştiriciye sağlayıcıdan bağımsız yüksek seviyeli bir arayüz verir.

# sai-2 | Temel
## Soru
Spring AI'ın sağlayıcı soyutlaması sayesinde OpenAI yerine Anthropic (Claude) kullanmaya geçmek tipik olarak neyi gerektirir?
## A
Starter bağımlılığını ve yapılandırmayı değiştirmek yeterlidir
🤖 Doğru: `ChatModel`/`ChatClient` soyutlaması sağlayıcıyı bağımlılık + config meselesine indirger, kod aynı kalır.
## B
Tüm iş mantığını sağlayıcının kendi SDK'sına göre baştan yazmak gerekir
🤖 Soyutlamanın amacı tam olarak bunu önlemek; SDK'ya doğrudan bağımlılık gerekmez.
## C
Uygulamayı GraalVM ile yerel (native) ikiliye yeniden derlemek gerekir
🤖 Native derleme ayrı bir konudur; sağlayıcı değişimiyle ilgisi yoktur.
## D
Veritabanı şemasını yeni sağlayıcıya göre yeniden tasarlamak gerekir
🤖 Sağlayıcı değişimi veri şemasını etkilemez; bu tamamen ilgisizdir.
## Doğru
A
## Açıklama
Spring AI, `ChatModel`/`EmbeddingModel` arkasında sağlayıcıyı **soyutlar**: kod `ChatClient` ile konuşur, hangi modelin arkada olduğunu bilmez. Bu yüzden Anthropic'e geçiş genelde ilgili **starter bağımlılığını** (ör. `spring-ai-starter-model-anthropic`) eklemek ve **yapılandırmayı** (API anahtarı, model adı) ayarlamaktan ibarettir; uygulama kodu büyük ölçüde değişmez. Sağlayıcıya özel SDK'ya bağlanmak (B) soyutlamanın amacına aykırıdır. Native derleme (C) ve DB şeması (D) bu konuyla ilgisizdir. Not: temel sohbet taşınabilirdir, ama prompt caching/tool-calling ayrıntıları sağlayıcıya göre farklılaşabilir.

# sai-3 | Orta
## Soru
`.entity(Rapor.class)` ile "yapılandırılmış çıktı" (structured output) kullanmanın kazandırdığı nedir?
## A
Modelin yanıtını uçtan uca şifreleyerek saklar
🤖 Şifreleme yapmaz; ham metni yapılandırılmış Java nesnesine dönüştürür.
## B
Yanıtı daha hızlı üretir, çünkü token sayısını yaklaşık yarıya indirir
🤖 Amaç hız değil; çıktının tipe güvenli biçimde ayrıştırılmasıdır.
## C
Yanıtı otomatik olarak diske kaydedip sonraki isteklerde oradan okur
🤖 Kalıcılık sağlamaz; çıktıyı bir Java tipine bağlamakla ilgilidir.
## D
Model cevabını doğrudan bir Java tipine ayrıştırır
🤖 Doğru: şema/format istemi eklenir ve dönen içerik hedef `record`/POJO'ya map'lenir.
## Doğru
D
## Açıklama
**Yapılandırılmış çıktı**, LLM'in serbest-metin yanıtını doğrudan tipli bir Java nesnesine bağlar: `chatClient.prompt().user(...).call().entity(Rapor.class)`. Spring AI arka planda modele **beklenen şema/format** yönlendirmesini ekler ve dönen içeriği hedef `record`/POJO'ya ayrıştırır. Bu, elle JSON ayrıştırma ve kırılgan string işleme ihtiyacını ortadan kaldırır, kodu tip-güvenli kılar. Şifreleme (A), token azaltma (B) veya diske kaydetme (C) bu özelliğin amacı değildir. Karmaşık tipler için `ParameterizedTypeReference` ile jenerik koleksiyonlar da desteklenir.

# sai-4 | Orta
## Soru
Spring AI'da `@Tool` ile işaretlenmiş bir metot ne işe yarar (Tool/Function Calling)?
## A
Metodu uygulama başlangıcında bir kez çalıştırıp sonucunu önbelleğe alır
🤖 Bu bir önbellek deseni; `@Tool` metodu modele çağrılabilir araç olarak sunar.
## B
Modele çağrılabilir bir araç tanımlar; Spring AI onu yürütür
🤖 Doğru: model isterse aracı tetikler, Spring AI çalıştırır ve sonucu modele geri besler.
## C
Metodu otomatik olarak bir REST uç noktasına dönüştürüp yayınlar
🤖 Bu `@RequestMapping` işidir; `@Tool` modele işlevsellik sunar, HTTP uç noktası açmaz.
## D
Metodu her istekte otomatik olarak paralel biçimde çalıştırır
🤖 Paralel yürütmeyle ilgisi yok; araç yalnız model istediğinde tetiklenir.
## Doğru
B
## Açıklama
**Tool/Function Calling**, modele uygulama işlevlerini "araç" olarak sunar. `@Tool` ile işaretlenen bir metot, açıklaması ve parametre şemasıyla modele bildirilir; model bir isteğe cevap verirken **aracın çağrılması gerektiğine karar verirse**, Spring AI metodu gerçekten yürütür ve dönen sonucu modele **geri besleyerek** nihai yanıtı ürettirir. Böylece model, canlı veri (hava durumu, DB sorgusu, hesap) veya yan etkiler için deterministik kodu kullanabilir. Bu bir önbellek (A), REST uç noktası (C) veya paralel yürütme (D) mekanizması değildir; model-güdümlü, isteğe bağlı bir çağrıdır.

# sai-5 | İleri
## Soru
Spring AI'ın "Advisors API"si mimari olarak neye benzer ve ne sağlar?
## A
Yalnızca hataları loglayan pasif bir dinleyicidir
🤖 Loglama bir örnektir ama advisor'lar isteği/yanıtı değiştirebilen aktif halkalardır.
## B
Model sağlayıcısını derleme zamanında seçen bir açımlayıcıdır (annotation processor)
🤖 Sağlayıcı seçimi bağımlılık/config işidir; advisor çalışma-zamanı hattıyla ilgilidir.
## C
İstek/yanıt hattına takılan araya-girici zinciridir
🤖 Doğru: servlet filtresi/AOP benzeri bir zincirdir; sohbet hafızası, loglama ve RAG (`QuestionAnswerAdvisor`) buradan gelir.
## D
Yanıtları diske yazan bir kalıcılık (persistence) katmanıdır
🤖 Kalıcılık advisor'ın amacı değil; hattı süsleyen araya-giricilerdir.
## Doğru
C
## Açıklama
**Advisors API**, `ChatClient` istek/yanıt akışına takılan **araya-girici (interceptor) halkalarından** oluşan bir zincirdir — kavramsal olarak servlet filtresi veya AOP around-advice gibi. Her advisor isteği zenginleştirebilir (bağlama belge/hafıza ekleme), yanıtı işleyebilir veya çağrıyı loglayabilir. Spring AI'ın hazır advisor'ları buradan gelir: sohbet hafızası (konuşma geçmişini enjekte etme), loglama ve en önemlisi **`QuestionAnswerAdvisor`** (RAG için vektör deposundan ilgili belgeleri çekip prompt'a ekler). Advisor pasif bir dinleyici (A), sağlayıcı seçici (B) veya kalıcılık katmanı (D) değil; hattı biçimlendiren birinci sınıf bir genişletme noktasıdır.

# sai-6 | İleri
## Soru
Prompt şablonu (prompt template) kullanmanın string birleştirmeye göre avantajı, sağlayıcı soyutlamasıyla birlikte neyi mümkün kılar?
## A
Prompt'u şifreleyerek sağlayıcının içeriği görmesini engeller
🤖 Şablon gizleme/şifreleme yapmaz; değişken yerleştirme ve yeniden kullanım sağlar.
## B
Değişkenli prompt tanımını koddan ayırır; sağlayıcıdan bağımsız kalır
🤖 Doğru: şablon + soyutlama, aynı prompt'un farklı `ChatModel`'lere değişmeden verilmesini sağlar.
## C
Prompt'u otomatik olarak çevirip gereksiz kısımlarını kısaltır
🤖 Otomatik çeviri/kısaltma yapmaz; yapılandırılmış, parametreli metin üretir.
## D
Sağlayıcı ne olursa olsun aynı token maliyetini ve gecikmeyi garanti eder
🤖 Token maliyeti sağlayıcı/modele göre değişir; şablon bunu garanti etmez.
## Doğru
B
## Açıklama
**Prompt template**, `{konu}` gibi **yer tutucular (placeholder)** içeren metni koddan ayırıp değişkenlerle doldurur (`PromptTemplate`). Avantajı: prompt'lar merkezi, gözden geçirilebilir ve yeniden kullanılabilir olur; kırılgan string birleştirme ortadan kalkar (Spring AI'da prompt'lar tipik olarak `prompts/` gibi bir kaynaktan gelir). Sağlayıcı soyutlamasıyla birleşince, **aynı şablon** farklı `ChatModel` bean'lerine (Anthropic/OpenAI/Ollama) değişmeden verilebilir. Şablon şifreleme (A), otomatik çeviri (C) yapmaz ve token maliyetini eşitlemez (D) — maliyet/limit sağlayıcı ve modele göre farklılaşır.

# sai-7 | İleri
## Soru
Spring AI'ın olgunluğu ve prompt caching hakkında 2026 itibarıyla hangisi doğrudur?
## A
1.0 GA Mayıs 2025, 1.1 Kasım 2025'te çıktı; caching sağlayıcıya bağlıdır
🤖 Doğru: sürüm tarihleri doğru; prompt caching sağlayıcıya bağlı bir yetenektir, evrensel garanti değil.
## B
Spring AI hâlâ deneysel bir milestone'dur; henüz bir GA sürümü yayımlanmadı
🤖 1.0 GA Mayıs 2025'te yayımlandı; proje üretim için kararlı sürüme ulaştı.
## C
Prompt caching tüm sağlayıcılarda birebir aynı davranır ve daima otomatiktir
🤖 Caching model/sağlayıcıya göre değişir; kapsamı ve etkinleştirmesi farklıdır, evrensel değildir.
## D
Prompt caching, modelin yanıt doğruluğunu artıran bir kalite özelliğidir
🤖 Caching kalite değil, tekrar eden bağlam için maliyet/gecikme optimizasyonudur.
## Doğru
A
## Açıklama
Spring AI **1.0 GA Mayıs 2025**'te, **1.1 Kasım 2025**'te yayımlandı; yani 2026'da üretim için kararlı ve yaygın kullanılan bir çerçevedir (B yanlış). **Prompt caching**, tekrar eden büyük bağlamın (sistem yönergesi, uzun belge) yeniden işlenmesini önleyerek **maliyet ve gecikmeyi** azaltan, **sağlayıcı/model-özel** bir optimizasyondur — destekleyen sağlayıcılarda etkinleştirilir; her sağlayıcıda birebir aynı ve otomatik değildir (C yanlış). Yanıt doğruluğunu artıran bir kalite mekanizması da değildir (D yanlış). Bu, "sağlayıcıyı değiştir, her şey aynı kalır" pazarlamasının sınırını gösterir: taşınabilirlik yüksektir ama performans/maliyet davranışı sağlayıcıya bağlıdır.
