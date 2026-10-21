---
slug: prompt-engineering-dev
baslik: "Geliştirici İçin Prompt Mühendisliği: Yapı, Few-shot, Rol, Determinizm ve Çıktı Şeması"
kademe: Orta
sira: 176
etiketler: 7
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kuryeye "paketi bırak" dersen kapının önüne bırakır. "Paketi 3. kata çıkar, komşuya teslim et, teslim fişini fotoğraflayıp gönder" dersen istediğin olur. Yapay zekâ modeliyle çalışmak da böyledir: model senin aklından geçeni bilmez, yalnızca yazdığını görür. **Prompt mühendisliği**, modelden güvenilir ve tekrarlanabilir çıktı almak için bu talimatı tasarlama işidir. Sihirli bir cümle aramak değil; iki taraf arasında bir sözleşme yazmaktır. Çıktıyı bir insan değil de bir **program** tüketecekse bu sözleşme daha da katılaşır.

### Temel kavramlar

- **Büyük dil modeli (LLM)**: Çok büyük metin yığınlarıyla eğitilmiş, "bundan sonra en olası parça ne?" sorusunu cevaplayan program. Kesin bir fonksiyon değil, **olasılıksal** bir üreteçtir.
- **Token (jeton)**: Metnin en küçük işleme parçası; kabaca bir kelime ya da kelime parçası. Gönderdiğin ve aldığın her şey token sayılır, maliyet buradan çıkar.
- **Prompt (istem)**: Modele gönderdiğin metnin tamamı. İki katmanı vardır: **system (sistem/rol) mesajı** kalıcı davranışı ve kimliği sabitler; **user (kullanıcı) mesajı** o anki veriyi ve isteği taşır. Çatışma olursa system önceliklidir.
- **Örneksiz / örnekli isteme (zero-shot / few-shot)**: Hiç örnek vermeden istemek örneksizdir (zero-shot). İstemin içine 2-5 adet **girdi→çıktı örneği** koymak örneklidir (few-shot). Örnek vermek modelin ağırlıklarını değiştirmez; yalnız o çağrı için beklenen biçimi gösterir (bağlam-içi öğrenme).
- **Sıcaklık (temperature)**: Modelin bir sonraki parçayı seçerken ne kadar "risk aldığını" ayarlayan sayı. Yüksekse çeşitli ve yaratıcı, düşükse dar ve tekrarlanabilir çıktı gelir.
- **Çıktı şeması (output schema)**: Cevabın hangi alanlardan oluşacağını tanımlayan katı biçim (genelde JSON). **Yapısal çıktı** ya da **araç çağrısı (function calling)** özelliğiyle model bu şemaya zorlanabilir.
- **Zincirleme (prompt chaining)**: Büyük bir görevi tek dev prompt yerine, her adımın çıktısı bir sonrakinin girdisi olan küçük adımlara bölmek.

### Nasıl çalışır?

1. **Rolü** system mesajında sabitlersin ("Sen bir teknik editörsün; yalnız JSON döndür").
2. **Görev ve bağlamı** user mesajında verirsin: ne isteniyor, hangi veriyle.
3. Gerekirse **few-shot** örneklerle beklenen biçimi somutlaştırırsın.
4. **Çıktı şemasını** dayatır, dönen cevabı programda şemaya karşı doğrularsın.
5. Sonucu bir test kümesiyle ölçer, prompt'u kaynak kodu gibi sürümlersin.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yapılandırılmış prompt bileşenleri">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Prompt bir sözleşmedir, dört parçalı</text>
  <rect x="30" y="48" width="140" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="100" y="76" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Rol / System</text>
  <text x="100" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kalıcı davranış</text>
  <rect x="182" y="48" width="140" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="252" y="76" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Görev + Bağlam</text>
  <text x="252" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ne, hangi veriyle</text>
  <rect x="334" y="48" width="140" height="70" rx="10" fill="#FBEEE6" stroke="#C8722B" stroke-width="1.5"/>
  <text x="404" y="76" text-anchor="middle" font-size="12.5" font-weight="700" fill="#C8722B" font-family="system-ui">Few-shot</text>
  <text x="404" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">örnekle göster</text>
  <rect x="486" y="48" width="128" height="70" rx="10" fill="#F0EAF7" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="550" y="76" text-anchor="middle" font-size="12.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Çıktı Şeması</text>
  <text x="550" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">katı JSON</text>
  <rect x="120" y="146" width="400" height="44" rx="10" fill="#FBF1F1" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="320" y="166" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">Regresyon testi: prompt = kod, değişince doğrula</text>
  <text x="320" y="183" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">girdi → beklenen çıktı kümesiyle sürekli sına</text>
</svg>
```

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Few-shot: birkaç örnek çıktının biçimini sabitler">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Few-shot: örnek biçimi sabitler</text>
  <rect x="28" y="46" width="264" height="140" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="160" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Zero-shot (0 örnek)</text>
  <rect x="52" y="86" width="150" height="13" rx="3" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <rect x="52" y="108" width="216" height="13" rx="3" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <rect x="52" y="130" width="96" height="13" rx="3" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <text x="160" y="170" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">biçim savrulur, tutarsız</text>
  <path d="M298 116 L338 116" stroke="#5D7789" stroke-width="2" fill="none"/>
  <path d="M338 116 l-9 -5 l0 10 z" fill="#5D7789"/>
  <rect x="344" y="46" width="268" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="478" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Few-shot (2-5 örnek)</text>
  <rect x="368" y="86" width="200" height="13" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="368" y="108" width="200" height="13" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="368" y="130" width="200" height="13" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="478" y="170" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">biçim tutarlı, tekrarlanabilir</text>
</svg>
```

### Mülakatta ne sorulur?

**"İyi prompt'un parçaları nedir?"** Rol, bağlam, görev ve format. Belirsiz bırakılan her parçayı model kendi olasılık dağılımından doldurur; sonuç savrulur. "Bu metni özetle" ile "teknik editör rolüyle, en fazla 3 madde, yalnız `{"maddeler":[...]}` JSON'u" arasındaki fark budur.

**"Örnek vermeyi ne zaman tercih edersin?"** Örneksiz istem yeterince kararlı sonuç veriyorsa örnek eklemek boşuna jeton yakar ve gecikmeyi artırır. Tutarsızlık varsa 2-5 iyi seçilmiş örnek çoğu zaman en ucuz çözümdür. Unutma: örnekler eğitim değil, biçim şablonudur.

**"`temperature=0` çıktıyı her seferinde aynı yapar mı?"** Kararlılığı çok artırır, ama **harfi harfine aynı çıktıyı garanti etmez**. Model sürümündeki değişiklikler, sunucu tarafında isteklerin toplu işlenmesi (batching), kayan noktalı sayı ve donanım farkları küçük sapmalar üretebilir. Bu yüzden doğruluğu "hep aynı çıkar" varsayımına değil, **doğrulamaya** dayandırırsın.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="temperature sıfır çıktıyı kararlı yapar ama mutlak determinizm garanti etmez">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">temperature=0: kararlı, ama mutlak değil</text>
  <text x="170" y="48" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">temperature yüksek</text>
  <line x1="44" y1="150" x2="296" y2="150" stroke="#5D7789" stroke-width="1.3"/>
  <rect x="60" y="98" width="30" height="52" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.2"/>
  <rect x="106" y="76" width="30" height="74" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.2"/>
  <rect x="152" y="90" width="30" height="60" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.2"/>
  <rect x="198" y="70" width="30" height="80" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.2"/>
  <rect x="244" y="104" width="30" height="46" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.2"/>
  <text x="170" y="168" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">geniş dağılım: çeşitli çıktı</text>
  <text x="470" y="48" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">temperature = 0</text>
  <line x1="344" y1="150" x2="596" y2="150" stroke="#5D7789" stroke-width="1.3"/>
  <rect x="360" y="140" width="30" height="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="406" y="134" width="30" height="16" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="452" y="62" width="30" height="88" fill="#25804C" stroke="#25804C" stroke-width="1.2"/>
  <rect x="498" y="136" width="30" height="14" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="544" y="142" width="30" height="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="470" y="168" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">en olası tokene daralır</text>
  <text x="320" y="196" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">sürüm · batch · donanım → küçük sapma → koda gömmeden DOĞRULA</text>
</svg>
```

**"Çıktıyı programda nasıl güvenle ayrıştırırsın?"** Serbest metin ile düzenli ifade (regex) birleşimi kırılgandır; modelin ifade tarzı değişince sessizce bozulur. Doğru yol şudur: katı JSON şeması + yapısal çıktı/araç çağrısı + şema doğrulaması.

**"İstem değişince ne yaparsın?"** İstem bir kod parçasıdır; küçük bir düzenleme uç durumlarda davranışı bozabilir. Temsili girdi→beklenen-çıktı örneklerinden bir **geriye gidiş (regresyon) test paketi** kurar ve her değişiklikte CI hattında çalıştırırsın.

**"Tek dev istem mi, zincir mi?"** Çok adımlı işlerde tek istem talimat yükünü şişirir ve adımlar birbirine karışır. Zincirleme her adımı ayrı ayrı doğrulanabilir kılar. Bedeli daha çok çağrı, yani daha çok maliyet ve gecikmedir.

### Tek cümlede

İstemi bir API sözleşmesi gibi ele al: girdiyi yapılandır, çıktıyı şemayla zorla, örnekle netleştir, geriye gidiş testiyle koru — "hep aynı çıkar" varsayma, doğrula.

# prompteng-1 | Temel
## Soru
Bir geliştirici, modelin kalıcı davranışını (ör. "her zaman kısa ve JSON döndür") sabitlemek istiyor. Bu talimat en doğru nereye konur?
## A
Her kullanıcı mesajının sonuna tekrar tekrar eklenir
🤖 Tekrarlı ekleme kırılgandır ve dağınıktır; kalıcı davranış için system mesajı vardır.
## B
Sistem/rol (system) mesajına
🤖 Doğru: kalıcı davranış ve kimlik, kullanıcı girdisinden ayrı olarak system mesajında sabitlenir.
## C
Modelin ağırlıklarına (weights) gömülerek
🤖 Ağırlıkları değiştirmek fine-tune işidir; basit bir davranış talimatı için system mesajı yeterlidir.
## D
Çıktı JSON'unun içine bir alan olarak
🤖 Çıktının içi sonuçtur, talimat yeri değil; davranış girdi tarafında (system) tanımlanır.
## Doğru
B
## Açıklama
**System/rol mesajı**, modelin kalıcı davranışını, tonunu ve kısıtlarını sabitlemek için tasarlanmıştır ve kullanıcı mesajından ayrıdır (B). Talimat çatışmasında system önceliklidir; bu yüzden "her zaman JSON döndür" gibi değişmez kurallar oraya konur. Aynı talimatı her kullanıcı mesajına tekrarlamak (A) kırılgan ve dağınıktır; ağırlıklara gömmek (C) gereksiz bir fine-tune işidir; çıktının içine koymak (D) talimat ile sonucu karıştırır. Katman ayrımı prompt mühendisliğinin temelidir: davranış system'de, veri user'da, biçim şemada.

# prompteng-2 | Orta
## Soru
Sıfır-örnek (zero-shot) bir prompt tutarsız biçimde çıktı üretiyor. Few-shot yaklaşımı bunu nasıl iyileştirir?
## A
Girdiyi kısaltıp modelin daha hızlı yanıt vermesini sağlayarak
🤖 Few-shot girdiyi uzatır ve gecikmeyi artırabilir; amacı hız değil biçim netliğidir.
## B
Modeli o örneklerle yeniden eğitip (fine-tune) davranışı kalıcı olarak değiştirerek
🤖 Few-shot eğitim değildir; ağırlıklara dokunmadan yalnız o çağrının bağlamında örnek gösterir.
## C
Sıcaklığı (temperature) otomatik olarak sıfıra çekip örneklemeyi daraltarak
🤖 Few-shot sıcaklık ayarı yapmaz; bunlar birbirinden bağımsız iki kaldıraçtır.
## D
Bağlamda birkaç girdi→çıktı örneği göstererek istenen biçimi somutlaştırarak
🤖 Doğru: örnekler beklenen formatı gösterir, model bu desene uyar ve çıktı kararlılaşır.
## Doğru
D
## Açıklama
**Few-shot**, prompt'un içine birkaç **girdi→çıktı örneği** koyarak modele istenen biçim, tarz ve karar sınırlarını somut olarak gösterir; model bu deseni taklit ederek daha tutarlı üretir (D). Bu, bağlam-içi öğrenmedir (in-context learning) — ağırlıklar değişmez (B yanlış), yalnızca o çağrı için yönlendirme yapılır. Few-shot hızı artırmaz, aksine girdiyi uzatır (A yanlış) ve sıcaklıkla ilgisizdir (C yanlış). Zero-shot yeterince kararlıysa few-shot gereksiz token yakar; tutarsızlık varsa 2-5 iyi seçilmiş örnek çoğu zaman en ucuz çözümdür.

# prompteng-3 | Orta
## Soru
`temperature=0` ayarlandığında LLM çıktısı hakkında hangi ifade DOĞRUDUR?
## A
Kararlılık belirgin artar ama bit-düzeyinde mutlak determinizm garanti edilmez
🤖 Doğru: örnekleme en olası tokene daralır; kararlılık artar, mutlak tekrar garantisi verilmez.
## B
Çıktı her koşulda bit-düzeyinde birebir aynı olur; sürüm veya altyapı farkı sapma yaratamaz
🤖 Model sürümü, toplu işlem (batching) ve donanım farkları pratikte küçük sapmalar üretebilir.
## C
Rastgelelik sıfırlandığı için model hiçbir token üretemez ve boş cevap döner
🤖 Sıcaklık 0 üretimi durdurmaz; yalnızca en olası tokeni seçerek deterministik-benzeri üretir.
## D
Sıcaklık yalnızca görsel üreten modelleri etkiler; metin üretiminde hiçbir etkisi yoktur
🤖 Sıcaklık metin üretiminde örnekleme dağılımını doğrudan etkiler; etkisiz değildir.
## Doğru
A
## Açıklama
`temperature=0`, örnekleme dağılımını en olası tokene daraltarak çıktıyı **çok daha kararlı** yapar ama **mutlak/bit-düzeyi determinizm garanti etmez** (A): model sürümü değişiklikleri, sunucu tarafı toplu işlem (batching), kayan-nokta ve donanım farkları küçük sapmalar üretebilir. Bu yüzden "temperature=0 = birebir aynı" (B) tehlikeli bir varsayımdır — çıktıyı iş mantığına gömmeden önce doğrulama şarttır. Sıcaklık üretimi durdurmaz (C) ve metin örneklemesini doğrudan etkiler (D). Mühendislik dersi: kararlılık için sıcaklığı düşür, ama **doğruluğu determinizme değil, doğrulamaya** dayandır.

# prompteng-4 | Orta
## Soru
Bir program, LLM çıktısını güvenle ayrıştırıp (parse) alanları koda vermek istiyor. En sağlam yaklaşım hangisidir?
## A
Çıktıyı serbest paragraf olarak isteyip düzenli ifadelerle (regex) alanları tahmin etmek
🤖 Serbest metin + regex kırılgandır; ifade biçimi değişince ayrıştırma sessizce bozulur.
## B
Modele "elinden geleni yap" deyip çıktı biçimini hiç belirtmemek
🤖 Biçim belirtmemek en savruk sonucu verir; program çıktıyı güvenle ayrıştıramaz.
## C
Katı bir JSON şeması tanımlayıp yapısal çıktıyla zorlamak ve doğrulamak
🤖 Doğru: şemayla zorlanan yapısal çıktı, programın güvenle tüketeceği sözleşmeyi kurar.
## D
Çıktıyı her zaman ilk 100 karakterle kesip kalanını atarak işlemek
🤖 Rastgele kesme veriyi bozar ve şema garantisi vermez; ayrıştırma güvenilir olmaz.
## Doğru
C
## Açıklama
Program tarafından tüketilecek çıktıda amaç **serbest metnin belirsizliğini ortadan kaldırmaktır**: katı bir **JSON çıktı şeması** tanımlanır, model **yapısal çıktı (structured output) veya function/tool calling** ile o şemaya zorlanır ve gelen JSON şemaya karşı doğrulanır (C). Böylece alanlar güvenle ayrıştırılır ve biçim sapmaları erken yakalanır. Serbest metin + regex (A), biçimsiz istek (B) veya kör karakter kesme (D) kırılgan yaklaşımlardır; ifade tarzı en ufak değişince sessizce bozulurlar. Kural: makine tüketecekse şema zorla, doğrula, sonra kullan.

# prompteng-5 | İleri
## Soru
Bir ekip, üretimdeki bir prompt'u "biraz iyileştirmek" için değiştirdi ve bir hafta sonra bazı uç durumlarda (edge case) çıktının bozulduğunu fark etti. Bu sorunu en iyi önleyen pratik hangisidir?
## A
Prompt'u hiç değiştirmemek, ilk yazıldığı hâliyle dondurup kilitlemek
🤖 Dondurmak iyileştirmeyi imkânsız kılar; sorun değişikliğin kendisi değil, denetimsiz olmasıdır.
## B
Girdi→beklenen-çıktı örnekleriyle regresyon paketi kurup CI'da çalıştırmak
🤖 Doğru: prompt bir kod parçasıdır; regresyon paketi uç durumların bozulup bozulmadığını yakalar.
## C
Sıcaklığı artırıp daha çeşitli çıktı üreterek sorunlu durumları göz ardı etmek
🤖 Sıcaklığı artırmak kararlılığı azaltır ve sorunu gizler; hatayı çözmez, büyütür.
## D
Çıktıyı üretimde zaman zaman gözle kontrol etmeyi tek güvence olarak kabul etmek
🤖 Manuel kontrol ölçeklenmez ve uç durumları kaçırır; otomatik regresyon gerekir.
## Doğru
B
## Açıklama
**Prompt bir kod parçasıdır**: küçük bir değişiklik, görünmeyen uç durumlarda davranışı bozabilir (regresyon). Bunu yakalamanın yolu, temsili **girdi→beklenen-çıktı örneklerinden oluşan bir regresyon test paketidir**; her prompt değişikliğinde CI'da çalıştırılır ve sapma anında görülür (B). Prompt'u dondurmak (A) iyileştirmeyi engeller — sorun değişiklik değil, denetimsiz değişikliktir. Sıcaklığı artırmak (C) kararlılığı bozar; ara sıra gözle bakmak (D) ölçeklenmez. Özet: prompt'ları da kaynak kodu gibi sürümle, test et ve gözden geçir.

# prompteng-6 | İleri
## Soru
Bir ekip uzun hata raporlarını tek bir prompt'la işliyor: "oku, sınıflandır, önceliklendir, düzeltme planı yaz ve hepsini tek JSON'da döndür." Çıktı sık sık eksik alanlı veya adımları karışmış geliyor. Zincirleme (prompt chaining) bu sorunu nasıl ele alır ve bedeli nedir?
## A
Sıcaklığı yükseltip modelin adımları kendiliğinden ayırmasını bekler; bedeli yoktur
🤖 Sıcaklık adım ayırmaz; yükseltmek savrulmayı artırır ve karışıklığı kötüleştirir.
## B
Görevi küçük adımlara böler; her adımın çıktısı doğrulanıp sonrakine girdi olur
🤖 Doğru: her adım tek işten sorumlu olur ve ayrı doğrulanır; bedeli daha çok çağrı, maliyet ve gecikmedir.
## C
Aynı dev prompt'u korur, yalnızca few-shot örnek sayısını artırarak sorunu çözer
🤖 Örnek eklemek talimat yükünü daha da şişirir; adımların birbirine karışması sürer.
## D
Modeli bu görev için yeniden eğitir (fine-tune) ve adımları ağırlıklara gömer
🤖 Fine-tune pahalı ve ağır bir çözümdür; buradaki sorun eğitim eksikliği değil görev tasarımıdır.
## Doğru
B
## Açıklama
Tek bir prompt'a çok sayıda farklı görev yüklendiğinde model talimatların bir kısmını gölgede bırakır; alanlar eksilir, adımlar birbirine karışır. **Zincirleme (prompt chaining)**, görevi sırayla küçük adımlara böler — sınıflandır → önceliklendir → plan yaz — ve her adımın çıktısı doğrulanıp bir sonrakinin girdisi olur (B). Kazanç: her adımın kendi şeması, kendi few-shot örnekleri ve kendi regresyon testi olur; hata hangi adımda oluştuğu görülerek izole edilir. **Bedeli**: birden çok model çağrısı, yani daha yüksek token maliyeti ve gecikme; ayrıca adımlar arası hata birikme riski. Sıcaklığı yükseltmek (A) savrulmayı artırır, örnek eklemek (C) şişkin prompt'u daha da şişirir, fine-tune (D) bu tasarım sorununa gereksiz ağır bir cevaptır.
