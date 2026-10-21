---
slug: hazirlik-haritasi-logo-ilani
baslik: "Hazırlık Haritası · Logo Kıdemli .NET İlanını Sökmek: Ne Çalışılır, Ne Atlanır, Hangi Sırayla"
kademe: Orta
sira: 291
etiketler: 11
---

## Konu anlatımı

Bu dosya bir ders değil, bir **haritadır**. Burada yeni bir teknik konu anlatılmaz; ilandaki her madde modülde zaten duran dosyalara bağlanır, sonra bu dosyalar bir sıraya ve bir takvime dizilir. Yeni bilgi arıyorsan yanlış dosyadasın; nereden başlayacağını arıyorsan doğru dosyadasın.

Önce ilanı sakin bir gözle okuyalım, çünkü hazırlığın tamamı oradan çıkar. İlan bir "Kıdemli Yazılım Geliştirme Uzmanı" arıyor ve dört küme hâlinde okunabilir. Birinci küme klasik: .NET ve .NET Core ile beş yıl üstü deneyim, C#, MS SQL Server, REST API, kurumsal uygulama geliştirme. İkinci küme mühendislik disiplini: Temiz Kod (Clean Code), SOLID, sürekli tümleştirme ve sürekli teslim (CI/CD), çevik çalışma. Üçüncü küme bu ilanı benzerlerinden ayıran kümedir: **İleri Seviye İstem Mühendisliği (prompt engineering) ve Bağlam Yönetimi (context management)**, **AI-Native yeniden düzenleme (refactoring) ve kod incelemesi (code review)**. Dördüncü küme görev tanımıdır: edinim (acquisition), sipariş, lisanslama, faturalama ve müşteri işe alma (onboarding) sistemlerini geliştirmek; teknik borcu azaltmak; mevcut sistemleri bakımda tutup performansını iyileştirmek; gelir operasyonlarını (revenue operations) destekleyen platformları modernleştirmek.

Bu dört kümeye bakınca hazırlığın asıl sorusu ortaya çıkar: **hangi kümede gerçekten boşluğun var?** Birinci kümede dil ve veri tabanı tarafında boşluk var, ama kurumsal uygulama ve REST tarafında yok. İkinci kümede boşluk yok; SOLID, temiz kod, CI/CD ve test disiplini modülde zaten derinlemesine duruyor ve senin yıllarında da duruyor. Üçüncü küme kısmen açık: AI araçlarını kullanıyorsun ama bunu bir mühendislik pratiği olarak anlatacak dil henüz kurulmadı. Dördüncü küme ise iki parçalı; teknik borç ve modernizasyon tarafı güçlü tarafın, gelir operasyonları alan bilgisi ise yeni.

Buradan çıkan strateji tek cümledir: **hazırlığın çoğu zamanı yeni öğrenmeye değil, var olanı .NET diliyle yeniden anlatmaya gider.** Aynı N+1 problemi, aynı indeks kararı, aynı eşgüçlülük (idempotency) tartışması, aynı katman ayrımı — yalnız isimleri değişir. Bu yüzden aşağıdaki harita "şunları öğren" listesi değil, "şunları hangi sırayla tercüme et" listesidir.

Bir uyarıyı baştan koyalım. Bu dosyadaki sürüm numaraları, ilan metninden alınan ifadeler ve şirketin ürün yelpazesine dair betimlemeler **zamana bağlıdır**. .NET sürümleri yılda bir değişir, ilan metni güncellenebilir, şirket içi takım yapısı ilan yayımlandıktan sonra bile değişebilir. Mülakattan önce ilanı bir kez daha aç ve okuduğunla bu dosyadakini karşılaştır; fark varsa ilan haklıdır, dosya değil. Aynı şey mevzuata bağlı ayrıntılar için de geçerlidir: faturalama ve lisanslama tarafında yasal düzenleme değişir, bu dosyadaki örnekler kavramı anlatmak içindir, güncel mevzuat beyanı değildir.

### İlanın maddeleri hangi dosyalara düşüyor

```svg
<svg viewBox="0 0 660 406" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ilan maddelerinin modul dosya numaralarina yonlendirme haritasi">
  <defs>
    <marker id="lgh-map-ok" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İlanda ne yazıyor → modülde hangi dosya karşılıyor</text>
  <text x="127" y="42" text-anchor="middle" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">İLAN MADDESİ</text>
  <text x="444" y="42" text-anchor="middle" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">ÇALIŞILACAK KONU (dosya numaraları)</text>
  <rect x="20" y="52" width="214" height="34" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="127" y="73" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">.NET / .NET Core · C# · 5+ yıl</text>
  <rect x="248" y="52" width="392" height="34" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="444" y="67" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">dil köprüsü, kap ve ara katman, async/await</text>
  <text x="444" y="81" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">281 · 282 · 284</text>
  <path d="M236 69 L246 69" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#lgh-map-ok)"/>
  <rect x="20" y="90" width="214" height="34" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="127" y="111" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">MS SQL Server · veri katmanı</text>
  <rect x="248" y="90" width="392" height="34" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="444" y="105" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">EF Core, T-SQL, plan okuma, indeks, gecikme</text>
  <text x="444" y="119" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">283 · 292 · 146 · 152 · 144</text>
  <path d="M236 107 L246 107" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#lgh-map-ok)"/>
  <rect x="20" y="128" width="214" height="34" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="127" y="149" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">REST API · kurumsal uygulama</text>
  <rect x="248" y="128" width="392" height="34" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="444" y="143" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">API tasarımı, sürümleme, katman ve modül sınırı</text>
  <text x="444" y="157" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">154 · 76 · 98 · 137 · 96</text>
  <path d="M236 145 L246 145" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#lgh-map-ok)"/>
  <rect x="20" y="166" width="214" height="34" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="127" y="187" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Temiz Kod · SOLID</text>
  <rect x="248" y="166" width="392" height="34" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="444" y="181" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">SOLID, yeniden düzenleme, kalıplar, altıgen mimari</text>
  <text x="444" y="195" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">131 · 132 · 133 · 134 · 135 · 136</text>
  <path d="M236 183 L246 183" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#lgh-map-ok)"/>
  <rect x="20" y="204" width="214" height="34" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="127" y="225" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">CI/CD · çevik · test</text>
  <rect x="248" y="204" width="392" height="34" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="444" y="219" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">hat tasarımı, dağıtım stratejisi, test piramidi, TDD</text>
  <text x="444" y="233" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">191 · 220 · 187 · 261</text>
  <path d="M236 221 L246 221" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#lgh-map-ok)"/>
  <rect x="20" y="242" width="214" height="34" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="127" y="263" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">İstem mühendisliği · bağlam yönetimi</text>
  <rect x="248" y="242" width="392" height="34" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="444" y="257" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">istem kalıpları, bağlam penceresi bütçesi, ajanlar</text>
  <text x="444" y="271" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">176 · 177 · 294 · 184</text>
  <path d="M236 259 L246 259" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#lgh-map-ok)"/>
  <rect x="20" y="280" width="214" height="34" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="127" y="301" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">AI-Native refactoring · code review</text>
  <rect x="248" y="280" width="392" height="34" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="444" y="295" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">AI destekli geliştirme, inceleme akışı, LLM güvenliği</text>
  <text x="444" y="309" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">175 · 295 · 181</text>
  <path d="M236 297 L246 297" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#lgh-map-ok)"/>
  <rect x="20" y="318" width="214" height="34" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="127" y="339" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Gelir operasyonları · teknik borç</text>
  <rect x="248" y="318" width="392" height="34" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="444" y="333" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">sipariş-lisans-fatura zinciri, modernizasyon, borç</text>
  <text x="444" y="347" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">296 · 293 · 130 · 126 · 268</text>
  <path d="M236 335 L246 335" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#lgh-map-ok)"/>
  <rect x="20" y="356" width="214" height="34" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="127" y="377" text-anchor="middle" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">Angular (tercihen)</text>
  <rect x="248" y="356" width="392" height="34" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="444" y="371" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">SPA kavramı ve karşılaştırma — yarım gün, derinleşme yok</text>
  <text x="444" y="385" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">208 · 209</text>
  <path d="M236 373 L246 373" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#lgh-map-ok)"/>
</svg>
```

Haritayı biraz açalım, çünkü her maddenin arkasındaki **beklenen cevap tonu** farklıdır.

- **".NET / .NET Core, 5+ yıl" dendiğinde** sorulan şey sürüm tarihçesi değildir. Kap (container) ve bağımlılık enjeksiyonu yaşam süreleri, ara katman (middleware) hattının sırası, istisna yönetimi ve yapılandırma düzeni sorulur (`282`). Dilin kendisi için `281` yeter; eşzamanlılık ve `async/await` tarafı ayrı ve pahalı bir başlıktır (`284`). Buradaki ton **kesin bilgi** tonudur; "sanırım" cevabı pahalıya patlar.
- **"MS SQL Server" dendiğinde** ilişkisel veri tabanı genel kültürü değil, bu ürünün kendi tarafı sorulur: sorgu planını okumak, kümelenmiş (clustered) indeks ile kümelenmemiş indeks farkı, kilitleme ve engellenme (blocking), saklı yordam (stored procedure) alışkanlıkları. Bu kademede `292` tam bunun için var. Altındaki genel kavramlar zaten sende: indeksleme (`146`), gecikme ve performans (`152`), önbellekleme (`144`).
- **"REST API ve kurumsal uygulama"** maddesinde boşluk yok. API tasarımı ve sözleşme (`154`, `76`, `98`), katmanlı yapı (`137`), modüler monolit (`96`) ve altıgen mimari (`136`) modülde duruyor. Yapılacak tek iş, aynı fikirleri ASP.NET Core sözcükleriyle söyleyebilmektir (`282`).
- **"Temiz Kod ve SOLID"** maddesi bu ilanda dekoratif değil; görev tanımında "teknik borcu azaltmak" yazdığı için işin merkezinde. `131` ve `132` çekirdek, `133`-`135` kalıplar. Beklenen ton takas (trade-off) tonudur: hangi ilkeyi ne zaman esnettiğini anlatabilen aday kıdemli sayılır.
- **"CI/CD ve çevik"** maddesinde hat tasarımı (`191`), dağıtım stratejileri (`220`), test piramidi (`187`) ve TDD (`261`) yeterlidir. Burada sorulan şey araç adı değil, **üretime çıkma sıklığı ve geri alma (rollback) refleksidir**.
- **"İleri Seviye İstem Mühendisliği ve Bağlam Yönetimi"** maddesi bu ilanın imzasıdır ve en çok hazırlıksız yakalanan yer burasıdır. Modülde temel var (`176`, `177`, `184`), ama mülakat dilini `294` kuruyor. Beklenen ton "hangi aracı kullanıyorsun" değil, "modele neyi verip neyi vermiyorsun, niye" tonudur.
- **"AI-Native yeniden düzenleme ve kod incelemesi"** maddesi bir öncekinin uygulama tarafıdır. `175` genel çerçeve, `295` bu ilana özgü akış, `181` ise gözden kaçan taraf: modele kurum kodu verirken sızıntı ve istem enjeksiyonu (prompt injection) riski. Bu riski kendiliğinden anan aday, maddeyi tek cümlede karşılar.
- **"Gelir operasyonları platformları"** maddesi alan bilgisi maddesidir. Edinim, sipariş, lisanslama, faturalama ve müşteri işe alma zinciri `296`'da duruyor. Zincirin teknik omurgası ise sende zaten var: eşgüçlülük ve giden kutusu (outbox) (`130`), saga ve telafi işlemi (`126`), Kafka üstünde giden kutusu (`116`).
- **"Angular (tercihen)"** maddesi bilerek en sona bırakıldı ve ağırlığı düşük tutuldu; gerekçesi aşağıda "Atlanacaklar" bölümünde.

### Çalışma sırası ve zaman dağıtımı

Sıralamanın mantığı şudur. **Önce dil, sonra veri, sonra ilanın imza maddesi, sonra alan bilgisi, en sonda prova.** Dil önce gelir çünkü sonraki her konuyu onun sözcükleriyle anlatacaksın; `292`'yi okurken kafanda EF Core oturmuş olmalı. Veri ikinci gelir çünkü kurumsal .NET mülakatlarının ağırlık merkezi orasıdır. Yapay zekâ maddeleri üçüncü gelir çünkü bu ilanı benzerlerinden ayıran madde odur ve hazırlıksız aday orada elenir. Alan bilgisi dördüncü gelir. Prova en sona bırakılır ki taze kalsın.

```svg
<svg viewBox="0 0 660 316" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yedi oturumluk calisma sirasi ve saat dagilimi">
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Çalışma sırası — hangi oturum, hangi dosya, kaç saat</text>
  <rect x="20" y="34" width="104" height="30" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="72" y="53" text-anchor="middle" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">Oturum 0 · 2 sa</text>
  <text x="136" y="48" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Harita ve ilan sökümü</text>
  <text x="136" y="62" font-size="10" fill="#5D7789" font-family="system-ui">291 (bu dosya) — ilanı yanına aç, maddeleri numaralara işaretle</text>
  <rect x="20" y="70" width="104" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="72" y="89" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Oturum 1 · 6 sa</text>
  <text x="136" y="84" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Dil ve platform tabanı</text>
  <text x="136" y="98" font-size="10" fill="#5D7789" font-family="system-ui">281 (2 sa) · 282 (2 sa) · 284 (2 sa) — sözlü anlatarak oku</text>
  <rect x="20" y="106" width="104" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="72" y="125" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Oturum 2 · 6 sa</text>
  <text x="136" y="120" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Veri katmanı ve SQL Server</text>
  <text x="136" y="134" font-size="10" fill="#5D7789" font-family="system-ui">283 (2 sa) · 292 (3 sa) · 146 tazeleme (1 sa)</text>
  <rect x="20" y="142" width="104" height="30" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="72" y="161" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Oturum 3 · 6 sa</text>
  <text x="136" y="156" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">İlanın imza maddesi: AI-Native</text>
  <text x="136" y="170" font-size="10" fill="#5D7789" font-family="system-ui">294 (2,5 sa) · 295 (2 sa) · 176 + 177 tazeleme (1,5 sa)</text>
  <rect x="20" y="178" width="104" height="30" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="72" y="197" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Oturum 4 · 6 sa</text>
  <text x="136" y="192" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Alan bilgisi ve modernizasyon</text>
  <text x="136" y="206" font-size="10" fill="#5D7789" font-family="system-ui">296 (2,5 sa) · 293 (2,5 sa) · 130 + 126 tazeleme (1 sa)</text>
  <rect x="20" y="214" width="104" height="30" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="72" y="233" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Oturum 5 · 5 sa</text>
  <text x="136" y="228" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Disiplin maddeleri — hepsi tazeleme</text>
  <text x="136" y="242" font-size="10" fill="#5D7789" font-family="system-ui">131 · 132 · 136 · 137 · 96 · 154 · 191 · 220 · 187 · 261</text>
  <rect x="20" y="250" width="104" height="30" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="72" y="269" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Oturum 6 · 5 sa</text>
  <text x="136" y="264" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Sözlü prova — yeni konu açma</text>
  <text x="136" y="278" font-size="10" fill="#5D7789" font-family="system-ui">256 · 263-270 · 266 · 268 · 195 + bu dosyadaki beş soru</text>
  <rect x="20" y="288" width="620" height="22" rx="7" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="330" y="303" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Toplam ≈ 36 saat · yarısı yeni konu, yarısı bildiğini .NET diliyle yeniden anlatma</text>
</svg>
```

Oturumları biraz açalım, çünkü her oturumun **çıktısı** farklıdır ve çıktısı belli olmayan oturum boşa geçer.

1. **Oturum 0 — harita (2 saat).** Bu dosyayı ilanın yanında aç. İlanın çıktısını al ya da ekranda tut, her maddenin yanına modül numarasını yaz. Bu oturumun çıktısı bir okuma değil, **işaretlenmiş bir ilan metnidir**. Yanına ikinci bir sütun daha aç: her madde için "sende var / kısmen var / yok". Bu sütun, beşinci sözlü prova sorusunun cevabını üretecek.
2. **Oturum 1 — dil ve platform (6 saat).** `281` ile başla; bu dosya seni C# uzmanı yapmaz, kavram eşlemesi kurar ve dil sorusuna dürüst cevabı verir. Sonra `282`: kap, kapsam (scope) seçimi, ara katman hattı, istisna yönetimi. Sonra `284`: `async/await`, senkron bloklamanın kilitlenme riski, `Task` ile `ValueTask`. Çıktısı: Java'daki her günlük refleksinin .NET karşılığını sesli söyleyebilmek.
3. **Oturum 2 — veri (6 saat).** `283` EF Core ve izleme (change tracking), sonra `292` SQL Server'ın kendi tarafı. `146` yalnız tazeleme; indeks bilgisi zaten sende, eksik olan bu ürünün terminolojisi. Çıktısı: "bu sorgu neden yavaş" sorusuna SQL Server sözcükleriyle cevap verebilmek.
4. **Oturum 3 — AI-Native (6 saat).** Bu oturumu erteleme; ilanı benzerlerinden ayıran madde budur ve hazırlıksız aday burada elenir. `294` bağlam mühendisliği, `295` AI destekli kod incelemesi. `176` ve `177` yalnız tazeleme. Çıktısı: "AI araçlarını nasıl kullanıyorsunuz?" sorusuna iki dakikalık, somut, ölçülü bir cevap.
5. **Oturum 4 — alan ve modernizasyon (6 saat).** `296` gelir operasyonları zinciri, `293` eski sistem (legacy) modernizasyonu ve teknik borç. `130` ve `126` tazeleme; faturalama ve lisanslama zincirinin teknik omurgası oradadır. Çıktısı: zinciri baştan sona, kendi cümlelerinle, kâğıda bakmadan anlatabilmek.
6. **Oturum 5 — disiplin maddeleri (5 saat).** Buradaki dosyaların hiçbiri yeni değil. Amaç hatırlamak değil, **örnek seçmek**: her ilke için kendi geçmişinden bir örnek belirle ve o örneği bir cümleye indir. Çıktısı: SOLID'in beş ilkesi için beş cümle, CI/CD için bir cümle, test stratejisi için bir cümle.
7. **Oturum 6 — prova (5 saat).** Yeni konu açma. `256` dürüst köprü kalıbı, `263`-`270` senaryo provası, `266` liderlik ve karar, `268` teknik borç, `195` davranışsal. Sonra bu dosyadaki beş soruyu **yüksek sesle** cevapla ve süre tut. İki dakikayı aşan cevap uzundur; kırk saniyenin altında kalan cevap zayıftır.

### Atlanacaklar ve neden atlanıyor

Hazırlığın en zor kısmı ne çalışacağını seçmek değil, **ne çalışmayacağına karar vermektir**. Sınırlı saatte her şeye dokunan aday hiçbirini anlatamaz. Aşağıdakiler bilerek dışarıda bırakılıyor ve her birinin gerekçesi var.

- **Angular derinliği — 5/10 ağırlık, yarım gün.** İlan bu maddeyi "tercihen" diye yazar; zorunlu değildir. Rol bir arka uç (backend) rolüdür ve görev tanımının tamamı sunucu tarafındadır: sipariş, lisanslama, faturalama, teknik borç, performans. Angular'a bir gün ayırmak, o günü .NET tarafından çalmak demektir ve takas kötüdür. Yapılacak olan `208` ve `209` üstünden **kavram düzeyinde** hazır olmaktır: tek sayfa uygulaması (SPA) nedir, bileşen ve durum yönetimi ne demektir, Angular ile React'ın felsefe farkı nedir, sunucu tarafı işleme (server-side rendering) neyi çözer. Soru gelirse verilecek cevap dürüst köprüdür (`256`): "Üretimde Angular yazmadım; SPA mimarisini ve arka uç sözleşmesini biliyorum, ön uç ekibiyle API tasarlarken şu kararları verdim…" Bu cevap yeterlidir, çünkü madde zorunlu değildir.
- **Mikroservis ve mesajlaşma derinliği — tekrar yok, yalnız tazeleme.** `116`, `126`, `130` modülde zaten duruyor ve sen bu konuları biliyorsun. İlan mikroservis demiyor; "gelir operasyonlarını destekleyen platformların modernizasyonu" diyor. O yüzden bu dosyalar Oturum 4'te birer saatlik tazeleme olarak geçilir, baştan çalışılmaz.
- **Redis'in tamamı — `155`-`166` arası tam okuma yok.** Önbellekleme kavramı (`144`) ve `155` girişi yeterlidir. İlanda dağıtık önbellek yazmıyor; performans iyileştirmesi yazıyor ve onun ilk adresi veri tabanı tarafıdır (`292`, `146`, `152`).
- **Konteyner ve Kubernetes derinliği — dışarıda.** İlan metninde geçmiyor. Sorulursa kavram düzeyinde cevaplanır; hazırlık saati ayrılmaz.
- **Java'ya özgü derinlikler — dışarıda.** Sanal iş parçacıkları, çöp toplayıcı ayarı, JVM iç yapısı: mülakat .NET tarafında geçecek ve bu konular sorulmayacak. Java bilgin özgeçmişte kanıt olarak durur; hazırlık saatinde yer kaplamaz. Tek istisna, `281`'deki kavram eşlemesi için gereken kadarıdır.
- **Sürüm numarası ezberi — kesinlikle dışarıda.** ".NET'in hangi özelliği hangi sürümde geldi" sorusuna ezber cevap vermeye çalışmak risklidir; yanlış bir sürüm iddiası, doğru cevapların güvenini de götürür. Bunun yerine özelliğin **hangi sorunu çözdüğünü** anlat. Bu, ilanın "5+ yıl .NET" maddesine dolaylı ama sağlam bir cevaptır.
- **Yeni bir AI aracını mülakattan önce öğrenmek — dışarıda.** Araç adı saymak bu maddeyi karşılamaz; karşılayan şey yöntemdir (`294`, `295`). Yeni bir araca üç gün ayırmak, elindeki yöntemi anlatacak dili kurmamak pahasına olur.

### Boşluk haritası: sende olan, sende olmayan, bu kademede kapanan

```svg
<svg viewBox="0 0 660 344" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Uc sutunlu bosluk haritasi: sende olan, sende olmayan, bu kademede kapanan">
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Boşluk haritası — hazırlık saatinin nereye gideceğini bu tablo söyler</text>
  <rect x="14" y="32" width="204" height="250" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="116" y="54" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">SENDE OLAN</text>
  <text x="116" y="70" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">anlatım dili kur, tekrar çalışma</text>
  <text x="116" y="92" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kurumsal geliştirme kıdemi</text>
  <text x="116" y="112" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">SOLID · temiz kod · kalıplar</text>
  <text x="116" y="132" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">REST ve API sözleşmesi</text>
  <text x="116" y="152" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ilişkisel veri, indeks, plan</text>
  <text x="116" y="172" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">CI/CD ve test disiplini</text>
  <text x="116" y="192" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eşgüçlülük · giden kutusu · saga</text>
  <text x="116" y="212" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">teknik borç yönetimi</text>
  <text x="116" y="232" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ekip içi karar ve mentorluk</text>
  <text x="116" y="258" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">131 · 132 · 154 · 146 · 191</text>
  <text x="116" y="272" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">130 · 126 · 268 · 266 · 195</text>
  <rect x="228" y="32" width="204" height="250" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="54" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">SENDE OLMAYAN</text>
  <text x="330" y="70" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">saatin çoğu buraya gider</text>
  <text x="330" y="92" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">C# ile üretim kodu</text>
  <text x="330" y="112" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ASP.NET Core çatı refleksleri</text>
  <text x="330" y="132" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">SQL Server'a özgü taraf</text>
  <text x="330" y="152" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eski .NET Framework mirası</text>
  <text x="330" y="172" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">AI pratiğini anlatacak dil</text>
  <text x="330" y="192" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">lisanslama ve faturalama alanı</text>
  <text x="330" y="212" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Angular ile üretim deneyimi</text>
  <text x="330" y="238" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">Angular hariç hepsi kapanabilir;</text>
  <text x="330" y="252" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">Angular için dürüst köprü (256)</text>
  <text x="330" y="270" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">olmayan deneyim uydurulmaz</text>
  <rect x="442" y="32" width="204" height="250" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="544" y="54" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">BU KADEMEDE KAPANAN</text>
  <text x="544" y="70" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sırayla okunur, atlanmaz</text>
  <text x="544" y="92" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">291 · ilan sökümü ve harita</text>
  <text x="544" y="112" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">292 · SQL Server derinliği</text>
  <text x="544" y="132" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">293 · legacy modernizasyon</text>
  <text x="544" y="152" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">294 · bağlam mühendisliği</text>
  <text x="544" y="172" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">295 · AI destekli kod incelemesi</text>
  <text x="544" y="192" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">296 · gelir operasyonları alanı</text>
  <text x="544" y="218" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">dil tabanı: Kademe 12</text>
  <text x="544" y="234" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">281 · 282 · 283 · 284</text>
  <text x="544" y="258" text-anchor="middle" font-size="9" fill="#186399" font-family="system-ui">önce 281, sonra bu kademe;</text>
  <text x="544" y="272" text-anchor="middle" font-size="9" fill="#186399" font-family="system-ui">ters sıra zaman kaybettirir</text>
  <rect x="14" y="292" width="632" height="42" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="330" y="311" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Kural: birinci sütuna saat harcanmaz, cümle harcanır</text>
  <text x="330" y="328" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Bildiğin konuyu tekrar okumak rahatlatır ama puan getirmez; onu .NET diliyle söylemek getirir.</text>
</svg>
```

Üç sütunun okunma biçimi farklıdır. **Birinci sütuna hazırlık saati harcanmaz**, cümle harcanır: her madde için kendi geçmişinden tek bir somut örnek seç ve o örneği bir cümleye indir. Bildiğin bir konuyu tekrar okumak rahatlatır ama mülakatta puan getirmez; onu doğru sözcüklerle söyleyebilmek getirir. **İkinci sütun saatin gittiği yerdir** ve bir maddesi bilerek kapatılmaz: Angular. Onun için plan çalışmak değil, dürüst köprü kurmaktır (`256`). **Üçüncü sütun sıralı okunur**; `292`'yi `283` olmadan okumak, `295`'i `294` olmadan okumak zaman kaybettirir.

### "Java geliştiricisisiniz, ilan 5+ yıl .NET istiyor. Neden bu pozisyon?"

**Soru aslında ne ölçüyor?**

Bu soru iki soruyu tek cümleye sıkıştırır ve çoğu aday yalnız birini duyar. Birincisi bir doğrulamadır: dil boşluğunu sen de görüyor musun, yoksa görmezden mi geliyorsun. İkincisi bir niyet sorusudur: bu başvuru bilinçli bir tercih mi, yoksa toplu başvurunun bir satırı mı. İkinci kısmı atlayan aday, birinci kısmı ne kadar iyi cevaplarsa cevaplasın eksik kalır; çünkü görüşmecinin gerçek endişesi şudur: bu aday altı ay sonra "aslında ben Java'ya döneceğim" der mi.

Cevabın yasağı da nettir: olmayan bir .NET deneyimi uydurulmaz (`256`). Bu bir ahlak kuralı olduğu kadar bir risk hesabıdır; uydurmanın maliyeti asimetriktir, kazanılan tek soru, kaybedilen mülakatın tamamıdır.

**Sözlü cevap iskeleti**

1. **Sınırı ilk cümlede ver, özür dilemeden.** "Üretim kodunu C# ile yazmadım." Tek cümle. Uzatmak, savunmaya geçmek ve özür dilemek boşluğu büyütür ve mülakatın geri kalanını o boşluğun etrafında döndürür.
2. **İlanın kendisini ölçüt yap.** İlanda dokuz kalem var ve bunların yalnız üçü .NET'e özgü. Kalanı — kurumsal uygulama, SOLID, temiz kod, CI/CD, REST, teknik borç azaltma, performans iyileştirme — dilden bağımsızdır ve sende dolu. Bu cümle savunma değil, bir okuma gösterisidir: ilanı gerçekten sökmüşsün.
3. **Yakınlığı kavram düzeyinde göster, dil düzeyinde değil.** İki ekosistem de yönetilen çalışma zamanı üstünde çalışır; Spring'in kabı ile ASP.NET Core'un yerleşik kabı aynı işi görür, JPA'nın karşılığı EF Core'dur (`281`, `282`, `283`). Burada üç dört eşleme yeter; liste okumaya dönüşürse ezber izlenimi verir.
4. **Gerçekten farklı olanı adlandır.** Yakınlık saymak kolaydır, farkı adlandırmak zordur ve ayrıştırıcı olan ikincisidir: değer tipi, tip silmenin olmaması, uzantı metotları, `async/await`'in dile gömülü olması (`281`, `284`). Farkı sayabilen aday, kullanmamış olsa da üzerine düşünmüş demektir.
5. **"Neden bu pozisyon" kısmını ayrı ve somut cevapla.** Bu kısım atlanırsa cevap yarım kalır. Somut olan şey ilanın görev tanımıdır: gelir operasyonlarını taşıyan sistemlerin bakımı ve modernizasyonu, teknik borç azaltma, performans iyileştirme (`293`, `296`). Bunlar senin son yıllarda fiilen yaptığın işlerdir; dil değişir, iş değişmez.
6. **AI maddesini burada bir kez an, sonra bırak.** İlanın imza maddesi olan istem mühendisliği ve AI destekli inceleme, bu pozisyonu ilgi çekici kılan şeydir (`294`, `295`). Tek cümle yeter; soru gelirse zaten açılır.
7. **Takvimli ve ölçülebilir bir söz ver.** "Hızlı öğrenirim" ölçülemez. Ölçülebilir olan şudur: sözdizimi ve araç zinciri kısa sürede oturur, ekosistem alışkanlıkları ve tuzaklar daha uzun sürer, ilk haftalarda kod incelemesinde çok soru sorarsın. Ölçüt de ver: ilk bağımsız değişikliğin ne zaman gelir.

**Elenme sebepleri**

- "İki dil neredeyse aynı, bir haftada geçerim." Tek ayrıntı sorusuyla çöker ve çöktüğünde önceki doğru cevapların güveni de gider.
- Kuru bir "C# bilmiyorum ama öğrenirim" deyip susmak. Dürüsttür, ama geriye yalnız eksik kalır.
- "Neden bu pozisyon" kısmını hiç cevaplamamak. Sorunun yarısı cevapsız kalır ve niyet şüphesi büyür.
- Gerekçeyi kendi üzerinden kurmak: "Kendimi geliştirmek istiyorum." Bu cümle senin ihtiyacını anlatır, şirketin ihtiyacını değil.
- Java'yı ya da .NET'i küçümsemek. "C# daha modern" ya da "Java daha ciddi" cümleleri teknik değil, taraf tutmadır.
- İlanı okumamış olmak. Görev tanımına tek bir gönderme yapmayan cevap, toplu başvuru izlenimi verir.
- Küçük bir .NET temasını büyük bir deneyim gibi anlatmak. Kıdemli görüşmeci bunu tek soruda yakalar (`256`).

**Kendi hikâyen**

Burada anlatılacak şey bir C# hikâyesi değil, bir **geçiş** hikâyesidir. Uydurma; şu sorular seni kendi geçmişine götürsün:

- Daha önce hangi teknolojiye ya da çatıya sıfırdan geçtin? Ne kadar sürdü, ilk hafta neye takıldın?
- Hiç dokunmadığın büyük bir kod tabanını devraldın mı? Okumaya nereden başladın: uç noktalardan mı, testlerden mi, veri modelinden mi?
- .NET tarafıyla temasın hangi bağlamda oldu? Ne yaptın, nereye kadar gittin? Bunu tam olduğu kadar anlat, bir gram fazlasını değil.
- Son yıllarda yaptığın işlerin hangileri bu ilanın görev tanımıyla birebir örtüşüyor? Bakım mı, modernizasyon mu, performans mı, borç azaltma mı?
- Neden Java'dan uzaklaşmak istiyorsun? Bu sorunun dürüst ve iyi düşünülmüş bir cevabı olmalı; olmazsa niyet sorusu kapanmaz.
- Öğrendiğini geçmişte neyle ölçtün? İlk bağımsız katkın ne zaman geldi, kaç yorum aldı, ikincisi kaç yorum aldı?

### "Gelir operasyonları (revenue operations) deyince ne anlıyorsunuz?"

**Soru aslında ne ölçüyor?**

Bu bir alan bilgisi sorusudur ve ölçtüğü şey terim bilgisi değildir. Görüşmecinin baktığı tek şey şudur: aday **zinciri kurabiliyor mu**. Gelir operasyonları tek bir sistem değildir; bir müşterinin ilk temasından paranın hesaba geçmesine kadar uzanan bir halkalar dizisidir ve halkalar arasındaki geçişler işin asıl zorluğudur. Zinciri sayabilen aday, ürünü hiç görmeden bile "bu ekibin derdini anlıyorum" izlenimi bırakır.

Yanlış cevap ezberden gelen tanımdır: "Satış, pazarlama ve müşteri başarısının hizalanmasıdır." Doğru ama boştur; bu cümle bir geliştiricinin değil, bir sunumun cümlesidir.

**Sözlü cevap iskeleti**

1. **Zinciri baştan sona bir nefeste say.** Edinim (acquisition) → sipariş → lisanslama → faturalama → müşteri işe alma (onboarding) → yenileme ve tahsilat. Bu tek cümle sorunun yarısını kapatır (`296`).
2. **Her halkanın yazılım tarafındaki karşılığını ver.** Edinim: aday müşteri kaydı ve kampanya. Sipariş: fiyatlandırma, indirim kuralları, sözleşme. Lisanslama: hak doğurma (entitlement), anahtar üretimi, kullanım sınırı. Faturalama: fatura kesme, vergi, ödeme sağlayıcısı. İşe alma: hesap açılışı, kurulum, ilk değerin üretilmesi.
3. **Zorluğun halkalarda değil, geçişlerde olduğunu söyle.** Bu cümle cevabın omurgasıdır. Sipariş kapandı ama lisans üretilmediyse müşteri parasını ödemiş ve ürünü kullanamıyordur. Fatura kesildi ama ödeme dönmediyse muhasebe ile sistem ayrışmıştır. Bu, dağıtık tutarlılık problemidir ve teknik karşılığı bellidir: eşgüçlülük (idempotency) ve giden kutusu (outbox) kalıbı (`130`), telafi işlemli saga (`126`), olay taşıma (`116`).
4. **Paranın geri alınamazlığını an.** Bu alanda hata bedeli paradır ve çift faturalama ile eksik faturalama farklı ağırlıkta hatalardır. Denetim izi (audit trail) ve mutabakat bu yüzden zorunludur. Bu cümle, ödeme tarafındaki geçmişini alan bilgisine dönüştürür.
5. **Kendi deneyiminden köprü kur.** Fatura, lisans ve sipariş sözcükleri farklıdır ama altındaki problem tanıdıktır: durum makinesi, tekrar güvenliği, mutabakat, geri alınamaz işlem. Bu köprüyü açıkça kur; görüşmeci senin adına kurmaz.
6. **Bilmediğin yeri işaretle.** Logo'nun ürün yelpazesindeki lisans modellerini bilmiyorsun ve bunu söylemek zayıflık değil, dürüstlüktür. Ardından ne soracağını söyle: abonelik mi süresiz lisans mı, kullanıcı başına mı, iş ortağı kanalı var mı. Soru sorabilmek alan bilgisinin kendisidir.

**Elenme sebepleri**

- Ezber tanım vermek. "Satış ve pazarlamanın hizalanması" cümlesi doğru ama bir geliştiricinin cevabı değildir.
- Zinciri sayamamak. Halkalar bilinmiyorsa geçişler de konuşulamaz ve cevap yüzeyde kalır.
- Yalnız faturalamayı anlatmak. Zincirin bir halkasını tamamı sanmak, alanı görmediğini gösterir.
- Geçişlerdeki tutarlılık problemini hiç anmamak. Bu ilanda en değerli tek cümle orada; atlanması pahalıya patlar (`130`, `126`).
- Paranın geri alınamazlığını atlamak. Faturalamayı sıradan bir CRUD işi gibi anlatmak kıdemsizlik okunur.
- Bilmediğini bilmemek. Şirketin lisans modelini biliyormuş gibi konuşmak, bir sonraki soruda çöker.
- Soru sormamak. Alan sorusunda netleştirme sorusu sormak (`263`-`270`) zayıflık değil, kıdem işaretidir.

**Kendi hikâyen**

Bu zincirin bütün halkalarını görmemiş olabilirsin; ama halkaların altındaki problemleri gördün. Şu sorular onu çıkarır:

- Para hareketi olan bir akışta çalıştın mı? Hangi adımda tutarlılık en kritikti?
- Aynı isteğin iki kez işlenmesini nasıl engelledin? Eşgüçlülük anahtarını nereden ürettin?
- Veri tabanına yazıp mesaj kuyruğuna yazamadığın bir durumu hiç yaşadın mı? Nasıl çözdün?
- Bir durum makinesi tasarladın mı? Kaç durumu vardı, geçersiz geçişleri nasıl engelledin?
- Mutabakat ya da gün sonu işine dokundun mu? Fark çıktığında süreç neydi?
- Bir müşteri şikâyeti veri tutarsızlığından mı çıktı? Kök nedeni nasıl buldun, sonra neyi kalıcı olarak değiştirdin?
- İş tarafıyla fiyat, indirim ya da sözleşme kuralı konuştuğun oldu mu? Kuralı koda nasıl taşıdın?

### "AI araçlarını nasıl kullanıyorsunuz?"

**Soru aslında ne ölçüyor?**

Bu ilan bu maddeyi tesadüfen yazmadı. "İleri Seviye İstem Mühendisliği ve Bağlam Yönetimi" ile "AI-Native yeniden düzenleme ve kod incelemesi" ifadeleri, ekibin bu araçları günlük iş akışına gömdüğünü ve adaydan **yöntem** beklediğini söyler. Ölçülen şey araç sahipliği değil, disiplindir: modele neyi veriyorsun, neyi vermiyorsun, çıktısını neyle doğruluyorsun, hangi işi ona hiç vermiyorsun.

"ChatGPT kullanıyorum, kod yazdırıyorum" cevabı bu yüzden eleyicidir. Bu cevap herkesin verebileceği cevaptır ve hiçbir ayrım üretmez; üstelik ilanın "ileri seviye" sıfatını duymadığını gösterir. Aynı şekilde karşı uç da eler: "AI'a güvenmem, kendim yazarım." Bu cümle ilanın merkezindeki maddeyi reddetmektir.

**Sözlü cevap iskeleti**

1. **Araçla değil, işle başla.** Hangi işlerde kullandığını söyle: mevcut kodu anlama ve haritalama, yeniden düzenleme adımlarının taslağı, test üretimi, kod incelemesinde ilk geçiş, dokümantasyon. Araç adı sonra gelir ve tek cümlede geçer.
2. **Bağlam yönetimini adlandır.** Cevabın ayrıştırıcı kısmı burasıdır (`294`). Modele bütün depoyu vermezsin; ilgili sınırı, sözleşmeyi, testi ve kabul ölçütünü verirsin. Bağlam penceresi bir bütçedir; içine ne koyduğun kadar **neyi dışarıda bıraktığın** da bir karardır. Gürültülü bağlam, çıktının kalitesini eksik bağlamdan daha çok düşürür.
3. **Doğrulama adımını söyle.** Modelin çıktısı bir öneridir, bir yama değil. Üretime giden yol testten ve incelemeden geçer (`261`, `187`). "AI yazdı" bir gerekçe değildir; sorumluluk yazan kişidedir.
4. **Kod incelemesindeki rolünü ayır.** AI incelemesi ilk geçiştir: biçim, ölü kod, sınır durumları, açık güvenlik kokusu. İnsan incelemesi ikinci geçiştir: niyet, alan kuralı, mimari sınır (`295`). Bu ayrım cevabı bir anda somutlaştırır.
5. **Sınırı ve riski kendiliğinden an.** Kurum kodunu modele verirken sızıntı riski, istem enjeksiyonu (prompt injection), lisans ve gizlilik kısıtları (`181`). Riski görüşmeci sormadan anmak, bu maddede en güçlü tek hamledir.
6. **Ölçüt ver.** "Verimliliğim arttı" ölçülemez. Ölçülebilir olanlar: incelemede yakalanan hata sayısı, yeniden düzenlemede geçen süre, testin kapsadığı yol sayısı, üretime çıkan hata oranı.
7. **Nerede kullanmadığını söyle.** Bu cümle güven üretir: alan kuralının doğruluğu, mimari sınır kararı ve güvenlikle ilgili son karar modele bırakılmaz.

**Elenme sebepleri**

- "ChatGPT kullanıyorum" deyip durmak. Herkesin cevabıdır; ilanın "ileri seviye" ifadesini duymamış olmayı gösterir.
- Araç adı listelemek. Beş araç saymak yöntem anlatmaz; görüşmeci araç adı değil, karar arıyor.
- Bağlam yönetimini hiç anmamak. İlanda madde olarak yazan şeyi cevapta hiç geçirmemek doğrudan puan kaybıdır (`294`).
- Doğrulama adımını atlamak. Modelin çıktısını doğrudan üretime taşıdığını ima etmek, kıdemli bir ekipte kırmızı bayraktır.
- Riski hiç anmamak. Kurum kodunun dışarı çıkması, gizlilik ve istem enjeksiyonu bu ilanda konuşulması beklenen risklerdir (`181`).
- Karşı uçta durmak: "AI'a güvenmem." İlanın merkezindeki maddeyi reddetmektir.
- Abartmak. "Kodun yüzde sekseni AI'dan çıkıyor" gibi ölçüsüz iddialar, bir sonraki soruda doğrulanamaz.

**Kendi hikâyen**

Bu soruda uydurmaya hiç gerek yok; bu araçları fiilen kullanıyorsun. Yapılacak iş, kullandığını **yönteme çevirmektir**. Şu sorular onu çıkarır:

- Bu araçlarla hangi işi en sık yapıyorsun? Anlama mı, üretme mi, inceleme mi, dönüştürme mi?
- Modele bir görev verirken yanına neyi koyuyorsun? Testi mi, sözleşmeyi mi, örnek kodu mu, kabul ölçütünü mü?
- Hiç kötü bir çıktı aldın mı? Neden kötüydü: bağlam eksik miydi, gürültülü müydü, soru mu yanlıştı?
- Bir çıktıyı doğrulamak için ne yaptın? Test mi yazdın, elle mi denedin, ikinci bir modele mi sordun?
- İnceleme akışında AI'a hangi işi verdin, hangi işi kendine sakladın?
- Kurum kodunu paylaşma konusunda bir kural koydun mu? Neyi hiç dışarı vermiyorsun?
- Bu araçlarla çalışırken ekipteki başka birine bir şey öğrettin mi? Ne öğrettin, nasıl anlattın?

### "Bir kod tabanını ilk devraldığınızda ilk hafta ne yaparsınız?"

**Soru aslında ne ölçüyor?**

Bu soru doğrudan görev tanımından doğar. İlan "mevcut sistemlerin bakımı", "performans iyileştirmesi" ve "modernizasyon" diyor; yani bu role giren kişi sıfırdan yazmayacak, başkasının yazdığını devralacak. Görüşmecinin ölçtüğü şey teknik beceri değil, **temkindir**: bu aday ilk haftada bir şeyi kırar mı.

Cevabın omurgası tek bir ilkedir: **ölçmeden dokunma**. Bu ilkeyi söyleyebilen aday sorunun yarısını kapatır. Kalan yarısı sırayla ilgilidir: önce anlamak, sonra ölçmek, sonra en küçük değişikliği yapmak.

**Sözlü cevap iskeleti**

1. **İlk gün: sistemi çalıştır ve testleri koştur.** Yerelde ayağa kalkıyor mu, test paketi yeşil mi, ne kadar sürüyor. Bu üç bilgi kod tabanının sağlık raporudur ve hiçbir dokümandan okunamaz.
2. **İlk günler: sınırları çıkar, satır satır okuma.** Uç noktalar, zamanlanmış işler, dış entegrasyonlar, veri tabanı şeması, dağıtım hattı. Sistemin dış yüzeyi iç yapısından daha çabuk öğretir. Katman ve modül sınırlarını çıkarmak için `137` ve `96`, mimari okuma için `136`.
3. **Değişim tarihine bak.** Sürüm geçmişi hangi dosyaların sık değiştiğini söyler; sık değişen ve karmaşık olan dosyalar borcun yoğunlaştığı yerdir (`293`, `268`). Hata kayıtları da aynı yeri işaret eder.
4. **İnsanlara sor ve yazıya dök.** Ekibin "buraya dokunma" dediği yerler vardır ve bu bilgi hiçbir yerde yazmaz. Öğrendiklerini yazılı bir nota çevirmek iki iş görür: sen öğrenirsin, ekip de yeni gelen için bir belge kazanır.
5. **Ölçüm koy, sonra dokun.** Değişiklikten önce ölçüt lazım: gecikme, hata oranı, sorgu süresi, kaynak kullanımı (`152`). Ölçüsüz iyileştirme, iyileştirme değil tahmindir. Veri tarafında ilk bakılacak yer sorgu planıdır (`292`, `146`).
6. **İlk değişiklik küçük ve geri alınabilir olsun.** Amaç değer üretmek değil, boru hattının tamamını bir kez baştan sona görmektir: kod, inceleme, test, dağıtım, geri alma (`191`, `220`).
7. **Borç listesini çıkar ama tek başına önceliklendirme.** İlk hafta bulguları listelenir, sıralanmaz; sıralama iş tarafıyla birlikte yapılır ve riske göre yapılır (`268`, `266`).
8. **Yeniden yazma teklifi etme.** İlk haftada "bunu baştan yazalım" demek, sistemi anlamadığını söylemenin en hızlı yoludur. Kademeli dönüşüm bu işin doğru yoludur (`293`).

**Elenme sebepleri**

- "Kodu baştan sona okurum" demek. Büyük bir kod tabanında bu ne mümkündür ne de verimlidir; sınırlardan girmek gerekir.
- Ölçüm adımını atlamak. Ölçmeden yapılan iyileştirme, sonucu bilinmeyen bir değişikliktir.
- İlk haftada büyük bir yeniden düzenleme başlatmak. Risk yüksek, kazanç belirsiz, ekip güveni sıfır.
- Yeniden yazmayı önermek. Kıdemli bir ekipte bu cümle neredeyse her zaman olumsuz okunur.
- Testlere ve dağıtım hattına hiç bakmamak. Sistemin gerçek durumu oradan okunur.
- Ekibe hiç soru sormamak. Yazılı olmayan bilgi yalnız insanlarda durur ve onu istemek zayıflık değildir.
- Bulunan borçları tek başına sıralayıp işe koyulmak. Öncelik iş tarafıyla kurulur; aksi hâlde doğru iş yanlış zamanda yapılır (`268`).

**Kendi hikâyen**

Bu soruda anlatılacak şey senin fiilen yaşadığın bir devralma hikâyesidir. Şu sorular onu çıkarır:

- Devraldığın en zor kod tabanı hangisiydi? Ayağa kaldırmak ne kadar sürdü?
- Anlamaya nereden başladın? Uç noktalardan mı, veri modelinden mi, testlerden mi, günlüklerden mi?
- İlk değişikliğin neydi ve ne kadar sürede üretime çıktı?
- Ölçmeden yapılmış bir değişikliğin geri teptiğini gördün mü? Ne oldu, sonra neyi değiştirdin?
- Bir performans sorununu ölçerek çözdüğün bir örnek var mı? Neyi ölçtün, ne kadar iyileşti?
- Devraldığın sistemde "dokunulmaz" ilan edilmiş bir yer var mıydı? Sonunda ona dokundun mu, nasıl?
- Öğrendiklerini yazıya döktün mü? O yazı sonradan başka birine yaradı mı?

### "Bu ilanda sizi en çok zorlayacak madde hangisi?"

**Soru aslında ne ölçüyor?**

Bu bir öz farkındalık sorusudur ve iki uçtan da elenir. Birinci uç kaçınmadır: "Zorlayacak bir madde görmüyorum." Bu cevap ya ilanı okumadığını ya da kendini tanımadığını söyler; ikisi de kötüdür. İkinci uç ise aşırı alçakgönüllülüktür: kendini gereğinden çok küçültmek, görüşmecinin zaten şüphe ettiği yeri onaylamak olur ve toparlanamaz.

Doğru cevap ikisinin arasındadır ve üç parçalıdır: **gerçek boşluğu adlandır, neden zor olduğunu bir cümleyle açıkla, kapatma planını ver.** Üçüncü parça olmadan cevap bir itiraf olur; üçüyle birlikte bir plan olur.

**Sözlü cevap iskeleti**

1. **Bir madde seç, birden fazla değil.** Birden fazla eksik saymak dürüstlük değil, dağınıklıktır. Seçilecek madde de gerçek olmalı; kimsenin inanmayacağı sahte bir eksik ("bazen fazla titizim") güveni bitirir.
2. **Doğru adayı seç: dil ve ekosistem refleksi.** Bu ilanda en dürüst cevap budur. Dikkat: eksik olan "C# sözdizimi" değil — sözdizimi kısa sürede öğrenilir. Eksik olan **ekosistem refleksidir**: hangi tuzağın nerede olduğunu bilmek, kütüphane seçiminde ekibin alışkanlığını tanımak, kod incelemesinde "burası kokuyor" diyebilmek. Bu ayrımı yapmak cevabı bir anda olgunlaştırır.
3. **Neden zor olduğunu tek cümleyle söyle.** Refleks okunarak değil, kod okuyarak ve inceleme alarak kazanılır; bu yüzden takvimi kitapla değil, ekiple belirlenir.
4. **Kapatma planını ver ve ölçüt koy.** `281`, `282`, `284` ile taban; `292` ile veri tarafı; sonra ekibin kendi kodunu okumak ve incelemeyi bir öğrenme aracına çevirmek. Ölçüt: ilk bağımsız değişiklik, ilk uçtan uca özellik, incelemede azalan yorum sayısı.
5. **İkinci bir maddeyi kısa geç: alan bilgisi.** Lisanslama ve faturalama alanının kendi kuralları var ve bunlar kitaptan öğrenilmez. Ama zincirin teknik omurgası tanıdık (`296`, `130`, `126`); bunu söylemek maddeyi hafifletir, silmez.
6. **Angular'ı burada an ve geç.** İlanda "tercihen" yazan bir maddedir; onu asıl zorluk olarak sunmak, ilanı yanlış okuduğunu gösterir.
7. **Kapanışı dengeyle yap.** Zor maddeyi söyledikten sonra güçlü tarafı bir cümleyle hatırlat: bakım, modernizasyon, teknik borç ve performans maddeleri bu ilanın hacimli kısmıdır ve orada boşluk yok. Bu cümle olmadan cevap eksik iniş yapar.

**Elenme sebepleri**

- "Beni zorlayacak bir madde yok" demek. İlanı okumamış ya da kendini tanımıyor izlenimi verir.
- Sahte bir zayıflık üretmek. "Fazla mükemmeliyetçiyim" cümlesi soruyu cevaplamaz, kaçınır.
- Kendini gereğinden çok küçültmek. Görüşmecinin şüphesini onaylamak, cevabın toparlanmasını imkânsız kılar.
- Üç dört eksik birden saymak. Dürüstlük değil dağınıklık okunur ve hiçbirine plan yetişmez.
- Zorluğu söyleyip planı vermemek. Cevap bir itiraf olarak kalır ve orada biter.
- Angular'ı asıl zorluk ilan etmek. "Tercihen" yazan bir maddeyi merkeze koymak, ilanı yanlış okuduğunu gösterir.
- Güçlü tarafı hiç anmadan bitirmek. Cevap eksik iniş yapar ve son izlenim boşluk üstünde kalır.

**Kendi hikâyen**

Bu cevabın hammaddesi Oturum 0'da çıkardığın "sende var / kısmen var / yok" sütunudur. Şu sorular onu keskinleştirir:

- O sütunda "yok" yazan kaç madde var? Hangisi bu rolün günlük işine en çok dokunuyor?
- Geçmişte bir eksiğini görüşmecide açıkça söyledin mi? Sonuç ne oldu, nasıl karşılandı?
- Bir teknolojide refleks kazanman ne kadar sürdü? Refleksin geldiğini nereden anladın?
- Bir alanı hiç bilmeden bir ekibe katıldın mı? Alan bilgisini nereden ve kimden aldın?
- Zayıf olduğunu bildiğin bir konuda plan kurup kapattığın bir örnek var mı? Plan neydi, ne kadar sürdü?
- Bu ilanın hangi maddesinde sen görüşmeciden daha güçlüsün? Bunu söylemeden mülakattan çıkma.

### Bu bölümden çıkan ders

Bu dosyanın tek bir omurgası var: **ilan bir tabeladır, harita değildir.** Haritayı sen çıkarırsın ve çıkarırken ilk sorduğun şey "ne çalışayım" değil, "nerede gerçekten boşluğum var" olur. Bu ilanda dokuz kalemin yalnız üçü .NET'e özgüdür; kalanı dilden bağımsızdır ve sende doludur. O yüzden hazırlığın çoğu zamanı yeni öğrenmeye değil, bildiğini .NET diliyle yeniden anlatmaya gider — birinci sütuna saat değil, cümle harcanır. Sıralama sabittir: önce dil ve platform (`281`, `282`, `284`), sonra veri (`283`, `292`), sonra ilanın imza maddesi olan AI-Native taraf (`294`, `295`), sonra alan bilgisi ve modernizasyon (`296`, `293`), en sonda prova (`256`, `263`-`270`). Angular bilerek 5/10 ağırlıktadır çünkü "tercihen" yazar ve rol bir arka uç rolüdür; ona ayrılacak gün .NET tarafından çalınır. Beş sözlü provanın ortak kuralı da tektir: olmayan deneyim uydurulmaz, olan deneyim ise görüşmecinin senin adına kurmayacağı köprüyle bağlanır. Ve son olarak, bu dosyadaki her sürüm numarası ile ilan alıntısı zamana bağlıdır; mülakattan önce ilanı bir kez daha aç, fark varsa ilan haklıdır.

# lgh-1 | Temel
## Soru
İlanı söktün ve hazırlık sırasını kuruyorsun. Sınırlı sürede sıralamanın çıkış noktası ne olmalıdır?
## A
İlandaki maddeleri yazıldığı sırayla, baştan sona, eşit saat ayırarak çalışmak
🤖 ilan sırası bir öncelik sırası değildir; eşit dağıtım, dolu olan kutulara da saat harcatır.
## B
Her maddeyi "sende var / kısmen var / yok" diye işaretleyip saati "yok" ve "kısmen" sütununa yığmak
🤖 Doğru: hazırlık boşluk haritasından çıkar, ilan metninin sırasından değil.
## C
En sevdiğin ve en rahat anlattığın maddelerden başlayıp güven kazanarak ilerlemek
🤖 rahatlatır ama puan getirmez; bilinen konuyu tekrar okumak boşluğu kapatmaz.
## D
En zor görünen maddeye bütün süreyi ayırıp diğerlerini mülakat sabahına bırakmak
🤖 tek maddeye yığılmak, hacimli ama kolay maddeleri anlatılamaz hâlde bırakır.
## Doğru
B
## Açıklama
Hazırlığın ilk işi okumak değil, **işaretlemektir**. İlanın yanına ikinci bir sütun açıp her maddeyi "sende var / kısmen var / yok" diye ayırdığında, saatin nereye gideceği kendiliğinden belli olur. Bu ilanda birinci sütun kalabalıktır: kurumsal uygulama, SOLID ve temiz kod (`131`, `132`), REST ve API sözleşmesi (`154`, `76`), CI/CD ve test disiplini (`191`, `220`, `187`), dağıtık tutarlılık (`130`, `126`), teknik borç yönetimi (`268`). Bunlara **saat harcanmaz, cümle harcanır**: her biri için kendi geçmişinden tek bir somut örnek seçip bir cümleye indirirsin. Saatin gittiği yer ikinci sütundur: C# ve platform refleksleri (`281`, `282`, `284`), SQL Server'ın kendine özgü tarafı (`292`), AI pratiğini anlatacak dil (`294`, `295`), lisanslama ve faturalama alanı (`296`). A şıkkı ilan sırasını öncelik sırası sanır; ilan metni bir belge düzenidir, bir öğrenme planı değil. C şıkkı psikolojik olarak cazip ama verimsizdir; bilinen konuyu tekrar okumak rahatlatır, boşluk kapatmaz. D şıkkı ise tek maddeye yığılıp hacimli ama kolay maddeleri anlatılamaz bırakır. Bu işaretleme aynı zamanda beşinci sözlü prova sorusunun ("sizi en çok zorlayacak madde hangisi?") hammaddesidir.

# lgh-2 | Orta
## Soru
İlanda "web development, tercihen Angular" maddesi var ve sen Angular ile üretim kodu yazmadın. Bu maddeye ne kadar hazırlık ayrılmalı ve mülakatta nasıl karşılanmalı?
## A
Yarım gün ayırıp SPA kavramını ve karşılaştırmayı öğrenmek; soru gelirse dürüst köprüyle cevaplamak
🤖 Doğru: madde "tercihen" yazar ve rol bir arka uç rolüdür; ağırlık buna göre verilir.
## B
En az iki gün ayırıp küçük bir Angular uygulaması yazmak; ilanda geçen her teknoloji eşit ağırlıktadır
🤖 iki gün .NET tarafından çalınır; ilandaki maddeler eşit ağırlıkta değildir.
## C
Maddeyi hiç çalışmamak ve soru gelirse "ön uç işi yapmıyorum" deyip konuyu kapatmak
🤖 kuru ret sohbeti bitirir; kavram düzeyinde hazır olmak düşük maliyetlidir.
## D
Özgeçmişe Angular eklemek; "tercihen" yazan bir madde zaten derinlemesine sorulmaz
🤖 doğrulanabilir bir konuda abartma, mülakatın tamamının güvenini bitirir.
## Doğru
A
## Açıklama
İlan maddelerini eşit ağırlıkta okumak, hazırlıkta en pahalı hatadır. Bu maddede iki işaret birden var: ifadenin kendisi "tercihen" diyor ve görev tanımının tamamı sunucu tarafında duruyor — sipariş, lisanslama, faturalama, teknik borç, performans. O hâlde doğru ağırlık düşüktür. Yapılacak iş `208` ve `209` üstünden **kavram düzeyinde** hazır olmaktır: tek sayfa uygulaması (SPA) nedir, bileşen ve durum yönetimi ne demektir, Angular ile React'ın felsefe farkı nedir, sunucu tarafı işleme (server-side rendering) neyi çözer. Bu yarım günlük yatırım, soru geldiğinde konuşabilmeye yeter. Soru gelirse kalıp dürüst köprüdür (`256`): sınırı tek cümlede söyle, sonra komşu deneyimini somutla — ön uç ekibiyle API sözleşmesi tasarlarken verdiğin kararlar, sürümleme, hata gövdesi (`154`, `76`). B şıkkı takası kaçırır; o iki gün `292` ya da `294`'ten çalınır ve o maddeler zorunlu kümededir. C şıkkı kuru rettir; dürüsttür ama sohbeti bitirir ve düşük maliyetli bir hazırlığı gereksiz yere reddeder. D şıkkı en pahalı seçenektir: özgeçmişte yazan her satır sorulabilir ve doğrulanabilir bir konuda abartma yakalandığında, o ana kadarki doğru cevapların güveni de gider.

# lgh-3 | Orta
## Soru
İlan "gelir operasyonlarını destekleyen platformlar" diyor. Görüşmeci bu terimden ne anladığını soruyor. Hangi cevap en güçlüdür?
## A
Satış, pazarlama ve müşteri başarısı ekiplerinin hizalanmasını sağlayan yönetim yaklaşımıdır
🤖 doğru ama boş; bir geliştiricinin değil, bir sunumun cümlesidir.
## B
Faturalama sistemidir; müşteriye kesilen faturaların üretilmesi ve gönderilmesiyle ilgilenir
🤖 zincirin bir halkasını tamamı sanmak, alanı görmediğini gösterir.
## C
Edinim, sipariş, lisanslama, faturalama ve işe alma zinciridir; asıl zorluk halkalar arası geçişlerin tutarlılığıdır
🤖 Doğru: zinciri kurmak ve geçişleri işaret etmek alan kavrayışını gösterir.
## D
Müşteri ilişkileri yönetimi (CRM) ürünüdür; aday müşteri kayıtlarının tutulduğu veri tabanıdır
🤖 CRM zincirin başındaki bir araçtır; zincirin kendisi değildir.
## Doğru
C
## Açıklama
Bu soruda ölçülen şey terim bilgisi değil, **zincir kurabilmektir**. Gelir operasyonları tek bir sistem değildir; bir müşterinin ilk temasından paranın hesaba geçmesine kadar uzanan halkalar dizisidir: edinim (acquisition) → sipariş → lisanslama → faturalama → müşteri işe alma (onboarding) → yenileme ve tahsilat (`296`). Cevabın omurgası halkaları saymak değil, **geçişleri işaret etmektir**. Sipariş kapandı ama lisans üretilmediyse müşteri parasını ödemiş ve ürünü kullanamıyordur; fatura kesildi ama ödeme dönmediyse muhasebe ile sistem ayrışmıştır. Bu tam olarak dağıtık tutarlılık problemidir ve teknik karşılığı sende zaten var: eşgüçlülük (idempotency) ve giden kutusu (outbox) kalıbı (`130`), telafi işlemli saga (`126`), olay taşıma (`116`). Buna paranın geri alınamazlığı ve denetim izi eklendiğinde cevap tamamlanır. A şıkkı ezber tanımdır; doğru olabilir ama bir geliştiricinin cevabı değildir ve hiçbir teknik köprü kurmaz. B şıkkı bir halkayı tamamı sanar. D şıkkı bir aracı zincirin kendisiyle karıştırır. Cevabın sonuna bilinmeyeni işaretlemek de değer katar: şirketin lisans modelini bilmiyorsan sor — abonelik mi, süresiz lisans mı, kullanıcı başına mı, iş ortağı kanalı var mı. Alan sorusunda netleştirme sorusu sormak kıdem işaretidir (`263`-`270`).

# lgh-4 | İleri
## Soru
Görüşmeci soruyor: "AI araçlarını nasıl kullanıyorsunuz?" İlan bu maddeyi "İleri Seviye İstem Mühendisliği ve Bağlam Yönetimi" diye yazmış. Hangi cevap bu maddeyi karşılar?
## A
Kullandığın araçları ve eklentileri tek tek saymak; ne kadar çok araç sayarsan o kadar deneyimli görünürsün
🤖 araç adı yöntem anlatmaz; herkesin verebileceği cevaptır ve ayrım üretmez.
## B
Modele hangi bağlamı verip neyi dışarıda bıraktığını, çıktıyı neyle doğruladığını ve hangi kararı ona hiç bırakmadığını anlatmak
🤖 Doğru: ilanın istediği şey araç sahipliği değil, disiplindir.
## C
AI çıktısına güvenmediğini, üretime giden her satırı kendin yazdığını söyleyerek titizliği vurgulamak
🤖 ilanın merkezindeki maddeyi reddetmektir; titizlik değil, uyumsuzluk okunur.
## D
Kodun büyük bölümünü AI ile ürettiğini, böylece geliştirme hızının birkaç kat arttığını söylemek
🤖 ölçüsüz iddia bir sonraki soruda doğrulanamaz ve sorumluluk sorusunu açar.
## Doğru
B
## Açıklama
Bu ilan bu maddeyi tesadüfen yazmadı; "ileri seviye" ve "bağlam yönetimi" ifadeleri ekibin bu araçları günlük akışına gömdüğünü ve adaydan **yöntem** beklediğini söyler. Güçlü cevabın omurgası dört parçadır. Birincisi bağlam kararıdır (`294`): modele bütün depoyu vermezsin, ilgili sınırı, sözleşmeyi, testi ve kabul ölçütünü verirsin — bağlam penceresi bir bütçedir ve gürültülü bağlam, eksik bağlamdan daha çok zarar verir. İkincisi doğrulamadır: çıktı bir öneridir, bir yama değil; üretime giden yol testten ve incelemeden geçer (`261`, `187`) ve "AI yazdı" bir gerekçe değildir. Üçüncüsü rol ayrımıdır (`295`): AI incelemesi ilk geçiştir — biçim, ölü kod, sınır durumları, açık güvenlik kokusu; insan incelemesi ikinci geçiştir — niyet, alan kuralı, mimari sınır. Dördüncüsü risktir (`181`): kurum kodunu modele verirken sızıntı, istem enjeksiyonu (prompt injection), gizlilik ve lisans kısıtları. Riski görüşmeci sormadan anmak bu maddede en güçlü tek hamledir. A şıkkı araç listesidir ve hiçbir karar göstermez. C şıkkı karşı uçtur ve ilanın merkezindeki maddeyi reddeder. D şıkkı ölçüsüz iddiadır; hem doğrulanamaz hem de sorumluluğun kimde olduğu sorusunu davet eder. Cevabı ölçütle kapatmak da değerlidir: incelemede yakalanan hata sayısı, yeniden düzenleme süresi, üretime çıkan hata oranı.

# lgh-5 | İleri
## Soru
Görev tanımında "mevcut sistemlerin bakımı ve performans iyileştirmesi" yazıyor. Görüşmeci soruyor: "Bir kod tabanını devraldınız, ilk hafta ne yaparsınız?" Hangi yaklaşım en güçlüdür?
## A
Sistemi ayağa kaldırıp testleri koşturmak, sınırları ve değişim tarihini çıkarmak, ölçüm koyduktan sonra küçük ve geri alınabilir bir değişiklik yapmak
🤖 Doğru: önce anla, sonra ölç, sonra en küçük adımı at.
## B
Kod tabanını baştan sona okuyup tam bir zihinsel model kurduktan sonra ilk değişikliğe geçmek
🤖 büyük bir kod tabanında ne mümkündür ne verimli; sınırlardan girmek gerekir.
## C
En göze batan mimari sorunu tespit edip ilk hafta içinde kapsamlı bir yeniden düzenleme başlatmak
🤖 riski yüksek, kazancı belirsiz; sistemi anlamadan yapılan büyük değişiklik güven kaybettirir.
## D
Sistemin eskidiğini tespit edip yeniden yazma teklifi hazırlamak ve iş tarafına sunmak
🤖 ilk haftada yeniden yazma teklifi, sistemi anlamadığını söylemenin en hızlı yoludur.
## Doğru
A
## Açıklama
Bu soru doğrudan görev tanımından doğar: bu role giren kişi sıfırdan yazmayacak, başkasının yazdığını devralacak. Ölçülen şey teknik beceri değil **temkindir** ve tek ilkeye indirgenir: ölçmeden dokunma. Doğru sıra şudur. Önce sistem yerelde ayağa kalksın ve test paketi koşsun; bu üç bilgi — kalkıyor mu, yeşil mi, ne kadar sürüyor — kod tabanının hiçbir dokümanda yazmayan sağlık raporudur. Sonra dış yüzey çıkarılır: uç noktalar, zamanlanmış işler, dış entegrasyonlar, veri tabanı şeması, dağıtım hattı; katman ve modül sınırları için `137`, `96`, `136`. Sonra değişim tarihine bakılır; sık değişen ve karmaşık olan dosyalar borcun yoğunlaştığı yerdir (`293`, `268`). Sonra ölçüm konur — gecikme, hata oranı, sorgu süresi (`152`), veri tarafında ilk durak sorgu planıdır (`292`, `146`). Ancak ondan sonra ilk değişiklik yapılır ve amacı değer üretmek değil, boru hattının tamamını bir kez görmektir: kod, inceleme, test, dağıtım, geri alma (`191`, `220`). B şıkkı gerçekçi değildir. C ve D şıkları aynı hatanın iki ölçeğidir: anlamadan büyük hamle yapmak. Özellikle D, kıdemli bir ekipte neredeyse her zaman olumsuz okunur; doğru yol kademeli dönüşümdür (`293`). Bulunan borçların **listelenmesi** ilk haftanın işidir, **sıralanması** değil; öncelik iş tarafıyla ve riske göre kurulur (`268`, `266`).

# lgh-6 | Uzman
## Soru
Sıralamayı kuruyorsun ve `292` (SQL Server derinliği) ile `294` (AI-Native bağlam mühendisliği) arasında hangisinin önce geleceğine karar veremiyorsun. Elinde altışar saatlik iki oturum ve `283` ile `281` çoktan bitmiş durumda. Hangi gerekçe doğrudur?
## A
`294` önce gelmeli, çünkü ilanın imza maddesi odur ve hazırlıksız aday orada elenir; `292` ise sende olan bilginin terminoloji çevirisidir
🤖 imza maddesi doğru ama sıra gerekçesi eksik; ikisinin bağımlılık yapısı farklıdır.
## B
`292` önce gelmeli, çünkü `283` bittiğinde veri katmanı zihinde açıktır ve terminoloji çevirisi o pencerede en ucuzdur; `294` ise önceki dosyalara bağımlı değildir ve tek başına çalışılabilir
🤖 Doğru: sıra, konunun önemine değil bağımlılık zincirine göre kurulur.
## C
Sıra fark etmez; ikisi de altı saatlik bağımsız oturumlardır ve toplam süre değişmediği için hangisiyle başlandığı önemsizdir
🤖 toplam süre aynı olsa da öğrenme maliyeti aynı değildir; bağlam kapanınca yeniden açmak pahalıdır.
## D
İkisi de bölünüp her oturuma üçer saat konmalı; böylece iki konu da mülakata eşit tazelikte girer
🤖 bölme, iki konuda da bağlam kurma maliyetini iki kez ödetir ve derinleşmeyi engeller.
## Doğru
B
## Açıklama
Bu soru sıralamanın gerçek ölçütünü sınar. Sezgisel cevap "önemli olan önce gelir"dir ve yanlıştır; sıralama **bağımlılık zincirine** göre kurulur. `292` bağımlıdır: EF Core'un izleme (change tracking) davranışı, sorgu çevirisi ve N+1 üretimi zihinde açıkken SQL Server tarafına geçmek, sorgu planını ve indeks kararını aynı bağlamda okumayı sağlar (`283` → `292`). Aynı gün içinde `146` bir saatlik tazelemeyle bağlanır ve zincir kapanır. `294` ise bağımsızdır: bağlam mühendisliği ne `283`'e ne `292`'ye dayanır, `176` ve `177` üstünden tek başına açılır. Bağımsız bir konuyu bağımlı bir zincirin ortasına sokmak, zinciri iki kez kurmayı gerektirir ve bağlam kurma maliyeti öğrenmenin en pahalı kalemidir. A şıkkının öncülü doğrudur — `294` gerçekten ilanın imza maddesidir ve `295` ile birlikte Oturum 3'te durur — ama sonucu yanlıştır; önem sırası ile bağımlılık sırası farklı şeylerdir ve çakıştıklarında bağımlılık kazanır. C şıkkı toplam süreyi ölçüt sanar; oysa aynı altı saat, doğru pencerede iki katı verim üretir. D şıkkı en pahalı seçenektir: iki konuda da bağlam kurma maliyetini iki kez ödetir, ikisinde de derinleşmeyi keser ve "eşit tazelik" iddiası da gerçekleşmez, çünkü tazeliği belirleyen tekrar sıklığıdır, bölünme değil. Genel kural şudur: **önem hangi konunun listeye gireceğini belirler, bağımlılık ise hangi sırayla çalışılacağını.**
