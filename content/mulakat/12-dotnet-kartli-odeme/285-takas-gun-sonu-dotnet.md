---
slug: takas-gun-sonu-dotnet
baslik: "Takas ve Gün Sonunun .NET Tarafı: İlanın Zorunlu Maddesini Sözlü Anlatmak"
kademe: Uzman
sira: 285
etiketler: 11
---

## Konu anlatımı

İlanın iki zorunlu maddesinden biri budur: kartlı ödeme sistemlerinde takas ve gün sonu deneyimi. Bu dosya sana yeni bir alan bilgisi öğretmiyor. O bilgi zaten modülde var: kart ödeme akışı ve aktörler `250`, mutabakat ve takas `252`, belirsizlik ve tekilleştirme `253`. Burada anlatılan iki şey var. Birincisi, o bilgiyi mülakatta hangi sırayla konuşacağın. İkincisi, süreçlerin .NET tarafındaki karşılıkları — çünkü ilan C# istiyor.

Altı soru var. Beşi teknik, sonuncusu deneyim beyanı. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı. Senin adına deneyim uydurulmaz; onu kendi geçmişinden çıkarman için sorular sorulur (`256`).

Bir uyarı en başta dursun. Bu alan sayı sever ama sayı ezberlemek burada zarar verir. Kesim saati, takas günü, komisyon oranı ve tolerans eşiği kurumdan kuruma değişen **parametrelerdir**. Mülakatta doğru cevap bir sayı söylemek değil, o sayının nereden okunduğunu söylemektir.

### "Kartlı ödemede takas ve gün sonu süreçlerini anlatır mısınız?"

**Soru aslında ne ölçüyor?**

Bu alanın merkez sorusudur ve genellikle ilk sorulan sorudur. Görüşmeci tanım duymak istemiyor; sürecin içinde yaşamış birini arıyor. Ayrım ilk cümlede çıkar. Zayıf aday ödemeyi tek bir olay gibi anlatır. Güçlü aday iki ayrı yolculuğu ayırır ve gerisini o ayrımın üstüne kurar. İkinci ayrıştırıcı nokta daha incedir: aday sayı verirken kesin mi konuşuyor, yoksa parametre olduğunu biliyor mu.

**Sözlü cevap iskeleti**

1. **İki yolculuğu ilk cümlede ayır.** Mesaj yolculuğu saniyeler sürer, para yolculuğu günler. Otorizasyon bittiğinde elinde bir kod vardır, kasanda para yoktur (`250`).
2. **Kesim saatini getir.** Bir işlemin hangi takas dönemine düşeceğini kesim saati belirler. Kesimden bir dakika önceki ve bir dakika sonraki işlem farklı dosyalara gider. Saati söyleme; "kurumun parametresidir" de.
3. **Netleştirme ile takası ayır.** Netleştirmede (clearing) **bilgi** hareket eder, kayıtlar toplanır ve karşılıklı netlenir. Takasta (settlement) **para** fiilen el değiştirir. İkisini aynı kelimeyle anlatan aday süreci dışarıdan biliyor demektir.
4. **Gün sonunu bir iş olarak tarif et.** Karşı tarafın dosyası alınır, doğrulanır, kendi kayıtlarınla eşleştirilir. Eşleştirme üç yönlüdür: bizde var onlarda yok, onlarda var bizde yok, ikisinde var tutar farklı (`252`).
5. **Tutar üçlemesini söyle.** Brüt tahsilat, kesilen komisyon ve hesaba geçen net ayrı kavramlardır. Tek bir tutar alanı tutan sistem gün sonunda ne müşteriye ne muhasebeye cevap verebilir.
6. **Farkın ne olacağını söyleyerek bitir.** Fark otomatik düzeltilmez. Sınıflandırılır, istisna kuyruğuna alınır ve insan inceler. Mutabakatı tutmayan ödeme sistemi bir yazılım sorunu değil, düzenleyici sorunudur.
7. **Dil sorusunu kapat.** Bu akış dilden bağımsızdır. Java tarafında gün sonu işini Spring Batch ile kurarsın (`222`, `223`); .NET tarafında barındırılan bir servis (`BackgroundService`) ve zamanlanmış iş kütüphanesiyle kurarsın. Süreç aynı, araç farklıdır.

```svg
<svg viewBox="0 0 660 336" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Takas anlatiminin omurgasi: mesaj yolculugu, kesim saati, netlestirme, takas ve gun sonu mutabakati">
  <defs>
    <marker id="tgs-akis-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sözlü cevabın omurgası: iki yolculuk, bir kesim, bir kuyruk</text>
  <rect x="14" y="36" width="632" height="36" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="59" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">MESAJ YOLCULUĞU · saniyeler — kararı issuer verir, elde otorizasyon kodu var, para yok (250)</text>
  <line x1="34" y1="130" x2="628" y2="130" stroke="#5D7789" stroke-width="1.3" marker-end="url(#tgs-akis-arw)"/>
  <line x1="92" y1="110" x2="92" y2="150" stroke="#186399" stroke-width="2"/>
  <text x="92" y="102" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">İŞLEM</text>
  <text x="92" y="166" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">onay alındı</text>
  <line x1="252" y1="110" x2="252" y2="150" stroke="#B07D20" stroke-width="2" stroke-dasharray="4 3"/>
  <text x="252" y="102" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">KESİM SAATİ</text>
  <text x="252" y="166" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">dönem burada kapanır</text>
  <line x1="412" y1="110" x2="412" y2="150" stroke="#5D7789" stroke-width="2"/>
  <text x="412" y="102" text-anchor="middle" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">NETLEŞTİRME</text>
  <text x="412" y="166" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">bilgi netlenir</text>
  <line x1="572" y1="110" x2="572" y2="150" stroke="#25804C" stroke-width="2"/>
  <text x="572" y="102" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">TAKAS</text>
  <text x="572" y="166" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">para el değiştirir</text>
  <rect x="14" y="180" width="632" height="34" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="202" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">PARA YOLCULUĞU · günler — komisyon kesilir, brüt ile net ayrı kavramdır (252)</text>
  <rect x="14" y="226" width="204" height="98" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="116" y="247" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Kesim saati bir parametredir</text>
  <text x="116" y="268" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kuruma ve sözleşmeye göre değişir</text>
  <text x="116" y="286" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">koda gömülmez, ayardan okunur</text>
  <text x="116" y="308" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">mülakatta sayı ezberleme</text>
  <rect x="228" y="226" width="204" height="98" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="247" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Gün sonu: üç yönlü eşleştirme</text>
  <text x="330" y="268" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bizde var · onlarda yok</text>
  <text x="330" y="286" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">onlarda var · bizde yok</text>
  <text x="330" y="304" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ikisinde var · tutar farklı</text>
  <text x="330" y="319" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ayrıntı: 252</text>
  <rect x="442" y="226" width="204" height="98" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="544" y="247" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Fark bulununca</text>
  <text x="544" y="268" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sınıflandır → istisna kuyruğu</text>
  <text x="544" y="286" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">insan inceler, kod düzeltmez</text>
  <text x="544" y="304" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">düzeltme yeni kayıtla yapılır</text>
  <text x="544" y="319" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">fark tutarı bir metriktir (104)</text>
</svg>
```

**Elenme sebepleri**

- "Ödeme onaylandıysa para gelmiştir." Bu tek cümle alanı hiç görmemiş bir aday izlenimi bırakır ve gerisi dinlenmez.
- Kesim saatinden hiç söz etmemek. Takas dönemini belirleyen şey odur; anmayan aday süreci uçtan uca izlememiş sayılır.
- Netleştirme ile takası aynı şey sanmak. Birinde bilgi, diğerinde para hareket eder; bu ayrım alanın sözlüğüdür.
- Kesin sayı vermek. "Kesim 23:00'tür, komisyon şu kadardır" demek yanlış olma riskini boşuna alır.
- Brüt ile neti tek tutar sanmak. Komisyon kesildiği için tahsil edilen ile hesaba geçen aynı sayı değildir.
- Farkı otomatik kapatan bir tasarım önermek. Para üzerinde otomatik düzeltme ilk hatalı dosyada gerçek zarar üretir.
- Mutabakatı "muhasebenin işi" diye dışarıda bırakmak. Bu cümle geliştiricinin süreci sahiplenmediğini söyler.

**Kendi hikâyen**

Bu soruda anlatım gücünü belirleyen şey, sürecin hangi ucundan tuttuğundur. Şu sorular onu çıkarır:

- Hangi sistemde para hareketiyle temas ettin? Kart, havale, sanal cüzdan, tahsilat, fatura, bakiye?
- Karşı taraftan dosya alan ya da karşı tarafa dosya üreten bir iş yazdın mı? Biçimi neydi, kim üretiyordu?
- Gün sonu penceresi var mıydı? İş kaçta başlıyordu, ne kadar sürüyordu, gecikince kim arıyordu?
- Kesim saati kavramı sende hangi adla geçti? Kapanış, gün sonu, dönem sonu, hesap kesimi?
- Fark çıktığında kim bakıyordu? Sen mi, operasyon mu, muhasebe mi; ekran mı vardı, tablo dosyası mı?
- Süreçte bir kez ciddi bir fark yaşandı mı? Kökü neydi ve nasıl kapandı?

Bu temas hiç yoksa uydurma. Onun yerine `256`'daki dürüst köprüyü kur: alan bilgisini kavram olarak anlat, en yakın gerçek deneyimini bağla ve farkı kendin adlandır. Sürecin sözlüğünü doğru kullanmak, uydurulmuş bir vakadan her zaman daha güçlüdür.

### "Gün sonu toplu işini nasıl tasarlarsın? .NET tarafında karşılığı ne olur?"

**Soru aslında ne ölçüyor?**

Bu soru iki şeyi birden ölçer. Birincisi toplu iş tasarımı: dayanıklılık, yeniden çalıştırılabilirlik, kısmi başarısızlık. İkincisi ekosistem bilgisi. Görüşmeci senin Java geçmişini biliyor olabilir; merak ettiği şey kavramı taşıyıp taşıyamadığındır. Burada en değerli cümle şudur: Java tarafında hazır gelen bazı şeyler .NET tarafında bir tasarım kararıdır. Bunu söyleyen aday iki ekosistemi de dürüstçe konumlandırmış olur.

**Sözlü cevap iskeleti**

1. **Tetiklemeyi ayır.** İş saate göre mi başlar, dosya gelince mi? İkisi farklı tasarımdır. Dosya geç gelebilir; saate bağlı bir iş boş dosyayla koşup "fark yok" demeyi bilmelidir.
2. **.NET karşılığını ver.** Uzun ömürlü iş bir **barındırılan servistir**: `IHostedService` ya da onun temel sınıfı `BackgroundService`. Zamanlama, yeniden deneme ve gösterge paneli için Hangfire veya Quartz.NET gibi kütüphaneler konuşulur. **Java karşılığı:** `@Scheduled` ile tetiklenen bir Spring Batch işi (`222`, `223`).
3. **Farkı dürüstçe söyle.** Spring Batch'in okuyucu–işleyici–yazıcı üçlüsü, parça (chunk) yönetimi ve `JobRepository` ile ilerleme takibi hazır gelir. .NET tarafında bunun tek bir yaygın standart karşılığı yoktur; ilerleme takibini genellikle kendin tablolarsın. Bu bir eksiklik değil, bilinmesi gereken bir karardır.
4. **Üç özelliği pazarlık dışı ilan et.** İş **yeniden çalıştırılabilir** olmalı: aynı dosya iki kez işlenirse ikinci düzeltme kaydı oluşmamalı. İş **kısmi başarısızlığa dayanmalı**: bozuk satır tüm işi düşürmemeli, atlanıp raporlanmalı. İşin çıktısı bir **istisna kuyruğu** olmalı.
5. **Tekilliği veritabanına yaptır.** Uygulamadaki "önce bak, yoksa yaz" kontrolü iki koşuda yarışa açıktır. Doğal anahtar üzerindeki benzersizlik kısıtı bu aralığı kapatır (`253`, `130`).
6. **Çok örnekli ortamı ayrıca konuş.** Üç kopya çalışan bir serviste zamanlayıcı üç kez ateşlenir. Tek koşmayı **dağıtık kilit** garanti eder: süreli kiralama (lease) ve çit numarası (fencing token) ile (`129`, `160`). Kilit tek başına da yetmez; yazma tarafı yine kısıtla korunur.
7. **Pencereyi ve ölçeği konuş.** Dosya akış hâlinde okunur, tamamı belleğe alınmaz. Eşleştirme anahtarı indekslidir (`146`). Parça boyutu ve işlem sınırı bilinçli seçilir.
8. **Gözlemlenebilirliği ekle.** İşin süresi, işlenen satır sayısı, fark tutarı ve kuyruk uzunluğu metriktir (`104`). En sık atlanan alarm şudur: iş hiç koşmadıysa kimse fark etmez.

```svg
<svg viewBox="0 0 660 344" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Gun sonu toplu isinin anatomisi: tetikleme, tek calistirma kilidi, dogrulama, parca parca isleme ve istisna kuyrugu">
  <defs>
    <marker id="tgs-batch-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
    <marker id="tgs-batch-red" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Gün sonu işinin anatomisi</text>
  <rect x="14" y="40" width="118" height="58" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="73" y="61" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">1 · TETİKLEME</text>
  <text x="73" y="77" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">saate göre mi</text>
  <text x="73" y="91" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">dosya gelince mi</text>
  <path d="M132 69 L146 69" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#tgs-batch-arw)"/>
  <rect x="150" y="40" width="118" height="58" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="209" y="61" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">2 · TEK KOŞU</text>
  <text x="209" y="77" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">dağıtık kilit · kiralama</text>
  <text x="209" y="91" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">129 · 160</text>
  <path d="M268 69 L282 69" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#tgs-batch-arw)"/>
  <rect x="286" y="40" width="118" height="58" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="345" y="61" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">3 · DOĞRULA</text>
  <text x="345" y="77" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">dosya bütünlüğü</text>
  <text x="345" y="91" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">satır ve toplam sayısı</text>
  <path d="M404 69 L418 69" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#tgs-batch-arw)"/>
  <rect x="422" y="40" width="118" height="58" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="481" y="61" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">4 · PARÇA PARÇA</text>
  <text x="481" y="77" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">akışla oku, belleğe alma</text>
  <text x="481" y="91" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ilerleme kaydedilir</text>
  <path d="M540 69 L554 69" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#tgs-batch-arw)"/>
  <rect x="558" y="40" width="88" height="58" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="602" y="63" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">5 · YAZ</text>
  <text x="602" y="80" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">eşleşen kayıtlar</text>
  <path d="M481 98 L481 122" fill="none" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#tgs-batch-red)"/>
  <rect x="360" y="126" width="286" height="42" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="503" y="145" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">İSTİSNA KUYRUĞU — bozuk ve eşleşmeyen satır</text>
  <text x="503" y="161" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iş düşmez, satır atlanır ve raporlanır</text>
  <rect x="14" y="126" width="330" height="42" rx="9" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.3"/>
  <text x="179" y="145" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Yeniden çalıştırılabilirlik doğal anahtarla korunur</text>
  <text x="179" y="161" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">benzersizlik kısıtı, uygulama kontrolünü gereksiz kılar (253)</text>
  <rect x="14" y="182" width="316" height="76" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="172" y="203" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Java tarafı</text>
  <text x="172" y="223" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Spring Batch: okuyucu · işleyici · yazıcı</text>
  <text x="172" y="240" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">JobRepository ilerlemeyi hazır tutar</text>
  <text x="172" y="253" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">222 · 223</text>
  <rect x="344" y="182" width="302" height="76" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="495" y="203" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">.NET tarafı</text>
  <text x="495" y="223" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">BackgroundService / IHostedService</text>
  <text x="495" y="240" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">zamanlama için Hangfire, Quartz.NET</text>
  <text x="495" y="253" text-anchor="middle" font-size="9" fill="#B07D20" font-family="system-ui">ilerleme takibi çoğu zaman tasarım kararıdır</text>
  <rect x="14" y="270" width="632" height="60" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="290" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Çok örnekli ortamda kilit yoksa iş kopya sayısı kadar koşar</text>
  <text x="330" y="309" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">üç kopya = üç zamanlayıcı = aynı dosyanın üç kez işlenmesi ve çift düzeltme kaydı</text>
  <text x="330" y="324" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kilit koşuyu tekler, benzersizlik kısıtı yazmayı tekler — ikisi birlikte kurulur</text>
</svg>
```

**Elenme sebepleri**

- "Bir zamanlanmış iş yazarım, olur biter." Gün sonu işi bir kod parçası değil, bir operasyon sürecidir.
- Çok örnekli ortamı hiç düşünmemek. Bugün her servis birden çok kopya koşar; kilitsiz zamanlayıcı işi çoğaltır.
- Dosyanın tamamını belleğe okumak. Hacim büyüdüğünde iş, gün sonu penceresine sığmadan çöker.
- Yeniden çalıştırmanın çift kayıt üretmesi. "İki kez koşarsa fark eder" demek işin en temel özelliğini kaybettirir.
- Tek bozuk satırın tüm işi düşürmesi. Tersi de yanlıştır: hatayı sessizce yutmak da elenme sebebidir.
- İşin hiç koşmadığını fark edecek bir alarm kurmamak. Sessiz kalan bir gün sonu, gürültülü bir hatadan tehlikelidir.
- Tekilliği yalnız uygulama kodundaki kontrolle sağlamak. Kontrol ile yazma arası yarışa açıktır.

**Kendi hikâyen**

Bu soruda toplu iş deneyimi büyük ihtimalle sende var. Onu ödeme diline çevirmek yeterlidir:

- Hangi toplu işi yazdın veya sahiplendin? Ne üretiyordu: dosya, rapor, düzeltme kaydı, bildirim?
- Ne kadar sürüyordu ve kaç satır işliyordu? Süre bir sorun oldu mu, ne yaptın?
- İş yarıda düştüğünde ne oluyordu? Baştan mı koşuyordu, kaldığı yerden mi devam ediyordu?
- Aynı iş iki kez koştu mu hiç? Nasıl fark edildi, sonucu ne oldu?
- Servis birden çok kopya çalışıyor muydu? Zamanlayıcının teklenmesi için ne yaptınız?
- Bozuk kayıtlar nereye düşüyordu? Onlara kim, ne zaman bakıyordu?
- İşin koştuğunu nereden biliyordunuz? Log, panel, alarm, yoksa sabah gelen şikâyet mi?

Deneyim gerçekten yoksa uydurma. Spring Batch ile yazılmış bir işi kavram olarak anlatmak ve .NET karşılığını doğru konumlandırmak da güçlü bir cevaptır (`256`).

### "Gün sonu mutabakatında fark çıktı. Ne yaparsın?"

**Soru aslında ne ölçüyor?**

Bu soru bilgi değil **refleks** ölçer ve en hızlı eleyen sorudur. Görüşmeci tek bir şeye bakar: aday farkı düzeltmeye mi koşuyor, anlamaya mı. Para üzerinde doğru refleks önce durmak, sonra sınıflandırmaktır. İkinci ayrıştırıcı nokta düzeltmenin biçimidir: geçmişi değiştirerek mi, yoksa yeni kayıt ekleyerek mi.

**Sözlü cevap iskeleti**

1. **Önce sınıflandır.** Fark tek bir şey değildir. Üç sınıf vardır: bizde var onlarda yok, onlarda var bizde yok, ikisinde var tutar farklı (`252`).
2. **Her sınıfın tipik kökünü söyle.** Birincisi genellikle kesim saatinin öbür tarafına düşen ya da tahsilata dönmemiş işlemdir. İkincisi zaman aşımı sonrası akıbeti bilinmeyen işlemdir (`253`). Üçüncüsü kısmi iade, komisyon eşlemesi, kur ya da yuvarlamadır.
3. **Otomatik düzeltmeyi açıkça reddet.** Fark istisna kuyruğuna alınır ve insan inceler. Otomatik kapatan bir iş, hatalı gelen ilk dosyada gerçek zarar yazar.
4. **Satır düzeyine in.** Toplamda tutan bir gün, satırda tutmayabilir. Karşı tarafın işlem referansı kendi kaydında saklanır ve indekslenir; eşleştirme onun üzerinden yapılır (`146`).
5. **Düzeltmeyi yeni kayıtla yap.** Ters kayıt, iade ya da düzeltme fişi açılır. Var olan kaydın durumunu geriye çevirmek yasaktır; geçmiş silinmez (`250`).
6. **Eşiği kendin koyma.** Tolerans, eskalasyon süresi ve kimin onayladığı iş kuralıdır. İş biriminden yazılı teyit alınır ve sistemde ayar olarak durur.
7. **Süreci kapat.** Fark tutarı, kuyruk uzunluğu ve kuyruktaki en eski kaydın yaşı izlenir (`104`). Bakılmayan bir kuyruk, kuyruk değil birikimdir.

**Java ↔ C# notu:** Eşleştirmenin kendisi veritabanı işidir; iki tarafı da tam tutan bir dış birleştirme sorgusudur (`29`). Bu yüzden milyonluk kümede satır satır nesne izleyen bir eşleme katmanı kullanılmaz. .NET tarafında EF Core'un izleme kipi kapatılır ya da doğrudan küme sorgusuna inilir (`283`). Java'da aynı gerekçeyle JPA yerine düz sorgu ya da toplu okuyucu seçilir. Kavram aynı, isim farklıdır.

**Elenme sebepleri**

- "Kodda düzeltirim." Bu cevap alanın en hızlı eleyen cümlesidir; kaynak veriyi elle değiştirmeyi normalleştirir.
- Farkı otomatik kapatan bir iş önermek. Hatalı bir dosya bir gecede tüm kayıtları bozar.
- Sınıflandırmadan tek yığın olarak bakmak. Üç sınıfın kökleri farklıdır ve tedavileri de farklıdır.
- Kuruş farkını önemsiz saymak. Tek işlemde önemsizdir; milyon işlemde mutabakat hiç tutmaz.
- Yalnız toplam tutara bakmak. İki hata birbirini götürebilir ve toplam yanıltıcı biçimde tutabilir.
- Tolerans eşiğini kendi kafandan koymak. Para üzerinde eşik teknik değil, iş ve mevzuat kararıdır.
- Kuyruğu kurup sahibini tanımlamamak. Kimsenin bakmadığı istisna kuyruğu sorunu gizler, çözmez.

**Kendi hikâyen**

Buradaki hikâye küçük olabilir; yeter ki gerçek olsun. Şu sorular onu çıkarır:

- İki kaynağın tutmadığı bir durum yaşadın mı? Hangi iki kaynak: sistem ve dosya, sistem ve rapor, iki servis?
- Farkı kim buldu? Bir iş mi, bir rapor mu, bir müşteri şikâyeti mi?
- Farkın kökü neydi? Zamanlama, yuvarlama, eksik kayıt, çift kayıt, kur, tanım farkı?
- Düzeltmeyi nasıl yaptınız? Elle güncelleme mi, düzeltme kaydı mı, yeniden koşan bir iş mi?
- O düzeltmenin izi kaldı mı? Sonradan "bunu kim, neden yaptı" sorusuna cevap verilebiliyor muydu?
- Aynı fark tekrar etti mi? Tekrarı önlemek için ne değişti: kod, süreç, alarm?

Bu tür bir vakan yoksa uydurma; bu soruda uydurma en çabuk yakalanandır. `256`'daki köprüyü kur ve tek veri kaynağı üzerinde tutarlılık yönetmiş olmanı anlat. Sonra "dağıtık iki kaynakta bu yetmez, çünkü şu değişir" diyerek muhakemeni göster.

### "Para ve tutar hesabında nelere dikkat edersin?"

**Soru aslında ne ölçüyor?**

Görünüşte basit, aslında eleyici bir sorudur. Doğru tipi söylemek eşiği geçirir ama ayrıştırmaz; onu herkes bilir. Ayrım üç yerde çıkar. Birincisi ondalık basamak sayısının para birimine bağlı olduğunu bilmek. İkincisi yuvarlamanın bir iş kararı olduğunu söylemek. Üçüncüsü brüt, komisyon ve neti üç ayrı gerçek olarak modellemek. Bu üçünü söyleyen aday para ile çalışmış demektir.

**Sözlü cevap iskeleti**

1. **Tiple başla.** .NET tarafında `decimal`, Java tarafında `BigDecimal`. Kayan noktalı tipler (`double`, `float`) para için kullanılmaz; ikili gösterim ondalık kesirleri tam tutamaz ve fark birikir (`226`).
2. **İki dildeki tuzağı ayır.** Java'da `BigDecimal.equals` ölçeği de karşılaştırır; bu yüzden karşılaştırmada `compareTo` kullanılır. .NET'te `decimal` eşitliği ölçeğe takılmaz. Aynı kavram, farklı tuzak.
3. **Ölçeği para birimine bağla.** Her para biriminin ondalık basamak sayısı aynı değildir. Basamak sayısı koda gömülmez; para birimi tanımından okunur. İki basamak varsaymak çok para birimli sistemde sessiz hata üretir.
4. **Yuvarlamayı iş kararı ilan et.** Hangi yönde yuvarlanacağı ve hesabın hangi adımında yuvarlanacağı sonucu değiştirir. Satır satır yuvarlayıp toplamak ile toplayıp yuvarlamak farklı sayılar verir. Kural iş biriminden yazılı alınır ve tek yerde uygulanır.
5. **Tutarı para biriminden ayırma.** Tutar tek başına anlamsızdır. İkisi birlikte taşınır; ilanın istediği alan odaklı tasarımda bunun adı bir **değer nesnesidir** (value object).
6. **Üç alanı ayır.** Brüt tahsilat, kesilen komisyon ve hesaba geçen net ayrı alanlardır. Takas dosyasından gelen net tutarı brütün üzerine yazmak iki bilgiyi birden yok eder (`252`, `254`).
7. **Veritabanı tarafını unutma.** MS SQL'de tutar için ondalık duyarlı tip seçilir, kayan noktalı tip değil. Basamak sayısı ve toplam duyarlık kolonda açıkça tanımlanır (`283`).
8. **Alternatifi de bil.** Tutarı en küçük birimde tamsayı tutmak da geçerli bir tercihtir. Eleyen şey tercih değil, tercihi savunamamaktır: ölçek, para birimi ve yuvarlama soruları o tasarımda da cevaplanmalıdır.

```svg
<svg viewBox="0 0 660 326" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Para hesabinda yanlis ve dogru tasarim, brut komisyon net ayrimi ve Java C sharp tip eslemesi">
  <defs>
    <marker id="tgs-para-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Para: tip, ölçek, yuvarlama ve üç ayrı tutar</text>
  <rect x="14" y="38" width="312" height="126" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="170" y="59" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Yanlış tasarım</text>
  <text x="30" y="81" font-size="10" fill="#1C3247" font-family="system-ui">· tutarlar kayan noktalı tiple taşınır</text>
  <text x="30" y="99" font-size="10" fill="#1C3247" font-family="system-ui">· tek bir tutar alanı vardır</text>
  <text x="30" y="117" font-size="10" fill="#1C3247" font-family="system-ui">· ondalık basamak her yerde iki sayılır</text>
  <text x="30" y="135" font-size="10" fill="#1C3247" font-family="system-ui">· yuvarlama kütüphane varsayılanına kalır</text>
  <text x="170" y="155" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">fark kuruş kuruş birikir, gün sonunda çıkar</text>
  <rect x="334" y="38" width="312" height="126" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="490" y="59" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Doğru tasarım</text>
  <text x="350" y="81" font-size="10" fill="#1C3247" font-family="system-ui">· ondalık duyarlı tip: decimal / BigDecimal</text>
  <text x="350" y="99" font-size="10" fill="#1C3247" font-family="system-ui">· brüt, komisyon ve net ayrı alanlarda</text>
  <text x="350" y="117" font-size="10" fill="#1C3247" font-family="system-ui">· basamak sayısı para biriminden okunur</text>
  <text x="350" y="135" font-size="10" fill="#1C3247" font-family="system-ui">· yuvarlama kuralı tek yerde ve yazılı</text>
  <text x="490" y="155" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">tutar ile para birimi birlikte taşınır</text>
  <rect x="14" y="178" width="312" height="132" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="170" y="199" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Üç ayrı gerçek, üç ayrı alan</text>
  <text x="30" y="223" font-size="10.5" fill="#1C3247" font-family="system-ui">brüt · müşteriden tahsil edilen</text>
  <text x="310" y="223" text-anchor="end" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">iade ve itirazın dayanağı</text>
  <text x="30" y="245" font-size="10.5" fill="#B23B3B" font-family="system-ui">komisyon · kesilen tutar</text>
  <text x="310" y="245" text-anchor="end" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">muhasebede gider kalemi</text>
  <line x1="30" y1="256" x2="310" y2="256" stroke="#186399" stroke-width="1.1"/>
  <text x="30" y="275" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">net · hesaba geçen</text>
  <text x="310" y="275" text-anchor="end" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">takas dosyasından gelir</text>
  <text x="170" y="296" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">net tutar brütün üzerine YAZILMAZ (252)</text>
  <rect x="334" y="178" width="312" height="132" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="490" y="199" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Java ↔ C# eşlemesi</text>
  <text x="350" y="223" font-size="10" fill="#1C3247" font-family="system-ui">BigDecimal</text>
  <text x="630" y="223" text-anchor="end" font-size="10" fill="#186399" font-family="system-ui">decimal</text>
  <text x="350" y="243" font-size="10" fill="#1C3247" font-family="system-ui">double / float yasak</text>
  <text x="630" y="243" text-anchor="end" font-size="10" fill="#186399" font-family="system-ui">double / float yasak</text>
  <text x="350" y="263" font-size="10" fill="#1C3247" font-family="system-ui">equals ölçeği karşılaştırır</text>
  <text x="630" y="263" text-anchor="end" font-size="10" fill="#186399" font-family="system-ui">eşitlik ölçeğe takılmaz</text>
  <text x="350" y="283" font-size="10" fill="#1C3247" font-family="system-ui">compareTo ile karşılaştır</text>
  <text x="630" y="283" text-anchor="end" font-size="10" fill="#186399" font-family="system-ui">karşılaştırma doğrudan</text>
  <text x="490" y="302" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kavram aynı, tuzak farklı — ayrıntı 226</text>
  <path d="M170 164 L170 174" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#tgs-para-arw)"/>
</svg>
```

**Elenme sebepleri**

- Kayan noktalı tiple para tutmak. Bu, alanla ilgili en bilinen kural olduğu için affedilmeyen bir hatadır.
- "Sonunda yuvarlarız, sorun kalmaz." Bozulma kaynakta olur; sunumda yuvarlamak farkı gizler, yok etmez.
- Yuvarlama kuralını hiç anmamak. Yön ve sıra sonucu değiştirir; varsayılana bırakmak bir karar değildir.
- Bütün para birimlerini iki basamak saymak. Çok para birimli sistemde bu varsayım sessizce yanlış tutar üretir.
- Brüt ile neti tek alanda toplamak. İade brüt üzerinden yürür; kaynak kaybolunca iade de yanlış hesaplanır.
- Tutarı para biriminden ayrı taşımak. Aynı sayı iki para biriminde toplanırsa hata görünmeden geçer.
- Ezberden komisyon oranı söylemek. Oran işlem tipine ve sözleşmeye göre değişir; kesin konuşmak boşuna risktir.

**Kendi hikâyen**

Bu soruda deneyim çıkarmak kolaydır; para hesabı ödeme dışında da her yerdedir:

- Hangi sistemde tutar hesabı yaptın? Fatura, indirim, komisyon, vergi, prim, bakiye?
- Tip olarak ne kullanıldı? Seçimi sen mi yaptın, hazır mı geldi, hiç sorgulandı mı?
- Yuvarlama tartışması yaşandı mı? Kural nereden geldi: iş birimi mi, mevzuat mı, geliştirici kararı mı?
- Toplamın tutmadığı bir şikâyet aldın mı? Kökü neydi: yuvarlama, sıra, tip, kur, tanım?
- Birden fazla para birimi var mıydı? Kur hangi ana göre alınıyordu ve nerede saklanıyordu?
- Brüt ile net ayrımı modelde var mıydı? Yoksa sonradan eklendi mi, nasıl eklendi?

Bu deneyim yoksa `226`'daki kuralları kavram olarak anlat ve dürüst ol (`256`). Bu soruda kavramı doğru anlatmak, zayıf bir vaka anlatmaktan güçlüdür.

### "RabbitMQ gibi bir kuyruk yapısını bu akışta nerede kullanırsın?"

**Soru aslında ne ölçüyor?**

İlan kuyruk deneyimi istiyor, ama bu soru araç bilgisi sormuyor. Ölçtüğü şey **yerindelik**. Zayıf aday her adıma kuyruk koyar. Güçlü aday kuyruğun nerede yerinde olduğunu ve nerede yanlış araç olduğunu söyler. İkinci ayrıştırıcı nokta teslim garantisidir: aday kuyruğu bir güvence sanıyor mu, yoksa en az bir kez teslimin sonuçlarını biliyor mu.

**Sözlü cevap iskeleti**

1. **Yerinde olduğu yerleri say.** Gün sonu adımlarının tetiklenmesi, işlenen dosyadan doğan aşağı akış bildirimleri, istisna kayıtlarının operasyon sistemine taşınması, iade ve ters kayıt gibi tekrar denenebilir adımlar.
2. **Yerinde olmadığı yeri de söyle.** Mutabakatın kendisi kuyrukla yapılmaz. Yüz binlerce satırı karşılaştırmak bir küme işidir; mesaj mesaj yapılırsa hem yavaşlar hem bütünlük kaybolur. Bu ayrımı yapan aday hemen ayrışır.
3. **Teslim garantisini doğru kur.** Yaygın garanti **en az bir kezdir**. Yani aynı mesaj iki kez gelebilir. Bu yüzden tüketici tekrar edilse de sonucu değişmeyen biçimde yazılır ve tekillik veritabanı kısıtına dayanır (`130`, `121`).
4. **Çift yazmayı giden kutusuyla kapat.** Veriyi yazıp sonra olay yayımlamak, arada süreç düşerse iki kaynağı ayırır. Olay, iş verisiyle **aynı işlemde** giden kutusu tablosuna yazılır; ayrı bir süreç yayımlar (`116`).
5. **Başarısızlığın yerini tanımla.** Kalıcı hatalar ölü mektup kuyruğuna düşer, raporlanır ve incelenir (`122`). Sonsuz yeniden deneme zehirli mesaj döngüsü üretir ve kuyruğu kilitler.
6. **Para işinde yeniden denemenin şartını söyle.** Tekrar yalnız isteğin karşı tarafta tekilleşen bir anahtar taşıması hâlinde güvenlidir. Şart yoksa tekrar değil, sorgulama yapılır (`253`).
7. **Araç seçimini ölçüte bağla.** İş dağıtımı ve yönlendirme gerekiyorsa kuyruk uygundur (`119`, `120`). Olayları geçmişe dönük yeniden oynatmak gerekiyorsa günlük tabanlı bir yapı daha uygundur (`124`).
8. **.NET karşılığını ver.** Kuyruk sunucusu ve protokol aynıdır; değişen istemci kütüphanesidir. Java tarafında Spring AMQP soyutlaması ne yapıyorsa, .NET tarafında resmî istemci ya da MassTransit gibi bir soyutlama benzerini yapar. Kavramlar — değişim noktası (exchange), kuyruk, onay, ölü mektup — birebir aynıdır.

**Elenme sebepleri**

- "Kuyruğa attım, gerisi garanti." Teslim garantisi tekilleştirmenin yerini tutmaz; aynı mesaj iki kez gelir.
- Giden kutusu olmadan yaz-sonra-yayımla yapmak. Süreç arada düşerse kayıt var, olay yok durumu kalıcılaşır.
- Ölü mektup kuyruğu tanımlamamak. Başarısız mesaj sessizce kaybolur ve kimse kaybı fark etmez.
- Mutabakatın tamamını mesajlaşmayla kurmak. Küme karşılaştırması için yanlış araçtır; hem yavaş hem eksiktir.
- Para işleminde körlemesine yeniden deneme. Tekilleşmeyen bir tekrar, çifte tahsilat demektir.
- Sonsuz yeniden deneme politikası. Zehirli tek bir mesaj tüm tüketiciyi meşgul eder ve kuyruğu tıkar.
- Kuyruk uzunluğunu ve en eski mesajın yaşını izlememek. Birikme, üretimde en geç fark edilen arızadır.

**Kendi hikâyen**

Kuyruk deneyimin muhtemelen var; onu bu akışa bağlamak yeterlidir:

- Hangi projede kuyruk kullandın? Ne taşıyordu: olay, iş emri, bildirim, dosya işleme tetiği?
- Aynı mesajın iki kez işlendiği oldu mu? Nasıl anlaşıldı, nasıl önlendi?
- Tekilliği neyle sağladınız? Uygulama kontrolü mü, veritabanı kısıtı mı, yoksa hiç mi?
- Ölü mektup kuyruğu var mıydı? Oraya düşen mesajlara kim bakıyordu?
- Veri yazma ile olay yayımlama aynı işlemde miydi? Giden kutusu kullanıldı mı?
- Kuyruk hiç doldu mu? Belirti neydi, ne yaptınız: tüketici artırma, hız sınırı, geçici durdurma?
- Sıra önemli miydi? Aynı hesabın işlemlerinin sırası korunmak zorunda mıydı, nasıl çözdünüz?

Bu deneyim yoksa uydurma; `256`'daki köprüyü kur ve mesajlaşmayı kavram olarak anlat.

### "Kartlı ödeme takas ve gün sonu tarafında deneyiminiz nedir?"

**Soru aslında ne ölçüyor?**

Bu soru ilanın zorunlu maddesinin doğrudan sınavıdır ve en tehlikeli sorudur. Sebebi şudur: karşındaki kişi bu alanda çalışıyor. Abartılmış bir cümle tek bir ayrıntı sorusuyla dağılır ve o andan sonra anlattığın her şey şüpheli hâle gelir. Ölçülen şey deneyimin büyüklüğü değil, **doğrulanabilirliğidir**. Küçük ama gerçek bir temas, büyük ama boş bir iddiadan her zaman güçlüdür.

**Sözlü cevap iskeleti**

1. **Gerçek durumu süslemeden söyle.** Ne yaptıysan onu söyle; sınırı sen çiz. Sınırı kendin çizmezsen görüşmeci çizer ve o daha acı olur.
2. **Somutla.** Hangi sistem, hangi rol, sürecin hangi adımı, hangi veriyle temas. Somut cümle inandırıcıdır; genel cümle şüphelidir.
3. **Alanın sözlüğünü kullan.** Kesim saati, netleştirme, takas, üç yönlü eşleştirme, istisna kuyruğu, brüt ve net. Kelimeleri doğru yerde kullanmak deneyimin en hızlı görünen işaretidir.
4. **Komşu deneyimi bağla.** Ödeme tarafında birebir deneyim yoksa yakınını kur: finansal işlem, bakiye, dosya mutabakatı, gün sonu raporu, toplu iş, tekilleştirme, denetim izi. Bunların hepsi aynı disiplindir.
5. **Eksiği adlandır ve planla.** "Şu tarafı yapmadım" cümlesini kurabilmek güven verir. Arkasına nasıl kapatacağını koy: hangi belgeyi okursun, kime ne sorarsın, ilk hafta neyi çıkarırsın.
6. **Dil ile alanı ayır.** Bu ilanda zor olan alan bilgisidir; dil öğrenilir. Aynı çerçeve `281`'de kurulur. Kıdemin de yıl sayısıyla değil aldığın kararlarla ölçülür (`195`).
7. **Sayı sorulursa parametre de.** Kesim saati, takas günü ve komisyon kuruma özgüdür. "Bunlar yapılandırmadan okunur, değeri iş biriminden yazılı teyitle alınır" cümlesi burada doğru cevaptır.

**Kendi hikâyen — bu bölümün tamamı senindir**

Aşağıdaki sorular tek amaçla var: senin gerçek geçmişinden bu alana bağlanacak malzemeyi çıkarmak. Cevaplarını yaz, sonra iki dakikalık bir anlatıya dönüştür. Hiçbirine uydurma cevap yazma.

- Para ya da finansal değer taşıyan bir veriyle çalıştın mı? Tutar, bakiye, ücret, komisyon, ceza, prim?
- İki kaynağı karşılaştıran bir iş yazdın mı? Ne ile ne karşılaştırılıyordu ve fark çıkınca ne oluyordu?
- Karşı taraftan dosya alan ya da dosya üreten bir entegrasyonda çalıştın mı? Biçim, sıklık, doğrulama nasıldı?
- Gün sonu, dönem sonu ya da kapanış diye anılan bir süreç var mıydı? Senin sorumluluğun neydi?
- Aynı işlemin iki kez uygulanma riski yaşadın mı? Çift kayıt, çift gönderim, çift tahsilat? Nasıl kapattınız?
- Zaman aşımı sonrası sonucu bilinmeyen bir çağrı yaşadın mı? O kaydı ne yaptınız?
- Tutarın yanlış hesaplandığı bir hata gördün mü? Kökü neydi ve nasıl fark edildi?
- Denetim izi tutulan bir sistemde çalıştın mı? Kim, ne zaman, neyi değiştirdi sorusu cevaplanabiliyor muydu?
- Operasyon ekibiyle çalıştın mı? Onlara ekran, rapor ya da düzeltme aracı yazdın mı?
- Bir işin iki kez koşması ya da hiç koşmaması yaşandı mı? Sonucu ne oldu, ne değişti?

Bu listeden çıkan her "evet" gerçek bir köprüdür. Hiçbirine evet çıkmıyorsa da yol var: `250`, `252` ve `253`'teki bilgiyi kavram olarak temiz anlatmak, alanın sözlüğünü doğru kullanmak ve deneyim sınırını dürüstçe söylemek. Bu, uydurulmuş bir vakadan hem daha güvenli hem daha inandırıcıdır (`256`).

### Bu bölümden çıkan ders

Bu alanın merkezinde tek bir ayrım durur: mesaj yolculuğu saniyeler, para yolculuğu günler sürer. Kesim saati bir işlemin hangi takas dönemine düşeceğini belirler, netleştirme bilgiyi netler, takas parayı taşır ve komisyon kesildiği için brüt ile net iki ayrı gerçektir. Gün sonu bu gerçekleri karşı tarafın dosyasıyla üç yönlü eşleştiren bir toplu iştir; çıktısı düzeltilmiş kayıt değil, incelenecek bir istisna kuyruğudur. .NET tarafı bu tabloyu değiştirmez, yalnız araçları değiştirir: Spring Batch'in yerini barındırılan bir servis ve bir zamanlama kütüphanesi alır, `BigDecimal`'in yerini `decimal` alır, çok örnekli ortamda işi tekleyen dağıtık kilit ihtiyacı aynen durur. Mülakatta seni ayıran şey de araç adı olmayacak; farkı otomatik kapatmayı reddetmen, düzeltmeyi yeni kayıtla yapman, ölçek ve yuvarlamayı iş kararı saymandır. Ve sayı sorulduğunda ezber bir değer yerine "bu bir parametredir, iş biriminden yazılı teyitle alınır" diyebilmendir — çünkü bu cümle, alanı gerçekten görmüş birinin cümlesidir.

# tgs-1 | Orta

## Soru
Müşteri ödeme onayı ekranını gördü ve elde bir otorizasyon kodu var. Finans ekibi aynı gün öğleden sonra "bu satışların parası hesabımızda mı?" diye soruyor. Doğru cevap hangisidir?

## A
Otorizasyon onayı geldiği anda tutar işyeri hesabına geçer; gün sonu yalnızca bu hareketin raporlanmasıdır
🤖 otorizasyon bir karar mesajıdır; para hareketi ayrı bir takvimde, dönemsel olarak yürür.

## B
Para tahsilat işaretlendiği anda hesaba geçer; kesim saati yalnız muhasebe kaydının hangi güne yazılacağını belirler
🤖 tahsilat da para hareketi değildir; kesim saati işlemin hangi takas dönemine gireceğini belirler.

## C
Otorizasyon bir karar mesajıdır; para netleştirme ve takas dönemiyle, komisyon kesilerek sonraki günlerde geçer
🤖 Doğru: mesaj yolculuğu saniyeler, para yolculuğu günler sürer.

## D
İşlem tutarı ile hesaba geçen tutar aynıdır; komisyon dönem sonunda ayrı bir fatura ile tahsil edilir
🤖 komisyon yaygın olarak takas sırasında kesilir; brüt ile net bu yüzden iki ayrı kavramdır.

## Doğru
C

## Açıklama
Bu sorunun tamamı tek bir ayrımın üzerine kuruludur: **mesaj yolculuğu** ile **para yolculuğu** aynı şey değildir (`250`). Otorizasyon, kartı veren bankanın verdiği bir karardır ve saniyeler içinde döner; sonunda elde bir kod vardır, kasada para yoktur. Paranın hareketi ayrı bir takvimde yürür. Gün içindeki işlemler **kesim saatine** kadar birikir, kesimden sonra **netleştirmeye** gönderilir ve netleşen tutar sonraki günlerde hesaba geçer (`252`). Bu takvimin kaç gün olduğu kuruma ve sözleşmeye göre değişir; mülakatta kesin sayı vermek yerine "parametredir" demek doğru duruştur. İkinci kritik nokta tutardadır: komisyon kesildiği için müşteriden tahsil edilen brüt tutar ile hesaba geçen net tutar aynı sayı değildir. Bu ikisini tek bir alanda tutan tasarım hem iadeyi hem muhasebeyi bozar (`226`, `254`). A ve B şıkları para hareketini bir mesajın yan etkisi sanır; ödeme sistemlerinde en yaygın kavram hatası budur. B ayrıca kesim saatini muhasebe detayına indirger; oysa kesim saati işlemin hangi takas dönemine düşeceğini, dolayısıyla iptalin hangi araçla yapılacağını belirler. D şıkkı brüt ile neti eşitler ve komisyonu sürecin dışına atar.

# tgs-2 | İleri

## Soru
Gün sonu mutabakat işi, üç kopya olarak çalışan bir .NET servisinin içinde barındırılan servis (`BackgroundService`) olarak zamanlanmış. Sabah bazı düzeltme kayıtlarının üç kez yazıldığı görülüyor. En yerinde çözüm hangisidir?

## A
İşin tek örnekte koşmasını dış bir dağıtık kilit garanti eder; yazma tarafı da doğal anahtarla benzersiz kısıtlanır
🤖 Doğru: kilit koşuyu tekler, veritabanı kısıtı yazmayı tekler.

## B
Kopya sayısı bire indirilir; gün sonu penceresinde tek örnek çalıştırmak sorunun tek kesin çözümüdür
🤖 erişilebilirliği feda eder ve dağıtım sırasında yine iki örnek oluşabilir; sorun kökten kapanmaz.

## C
Her kopyaya farklı bir başlangıç saati verilir; işler çakışmayınca aynı satır iki kez işlenmez
🤖 çakışma önlenmez, yalnız ertelenir; ilk koşu bitmemişse ikinci koşu aynı satırları yine işler.

## D
İş başlarken uygulama belleğinde bir bayrak kurulur; ikinci çalıştırma bu bayrağı görüp geri döner
🤖 bellek her kopyada ayrıdır; süreç düşerse bayrak da kaybolur, garanti üretmez.

## Doğru
A

## Açıklama
Çok örnekli dağıtımda zamanlayıcı, kopya sayısı kadar ateşlenir. Barındırılan servis içindeki bir zamanlayıcı da bundan muaf değildir; `BackgroundService` yalnızca işi çalıştırır, teklemez. Bu yüzden gün sonu gibi bir işte **dağıtık kilit** gerekir: süreli bir kiralama alınır, kiralamayı alan koşar, alamayan sessizce çıkar. Kiralamanın süresi ve yenilenmesi, ayrıca uzun duraklamalarda eski sahibin yazmasını engelleyen çit numarası önemlidir (`129`, `160`). Ama kilit tek başına yeterli sayılmaz. Ağ bölünmesinde iki örnek kısa süre birlikte kendini sahip sanabilir; bu yüzden yazma tarafı da doğal anahtar üzerinde benzersizlik kısıtıyla korunur. Böylece ikinci yazma denemesi veritabanı seviyesinde reddedilir ve iş yeniden çalıştırılabilir hâle gelir (`253`, `130`). Java tarafında aynı ihtiyaç Spring Batch'te de vardır; iş deposunun ilerleme takibi yeniden başlatmayı çözer ama tekleme yine dışarıdan gelir (`222`, `223`). B şıkkı erişilebilirliği feda eder ve dağıtım anında yine iki örnek oluşabileceği için garanti vermez. C şıkkı çakışmayı yalnız öteler. D şıkkı en zayıfıdır: uygulama belleği her kopyada ayrıdır ve süreç düştüğünde kaybolur.

# tgs-3 | İleri

## Soru
Gece koşan mutabakat işi, gelen takas dosyasıyla kendi kayıtlar arasında 37 satırlık fark buldu. Ekipten biri "iş bu farkları otomatik düzeltsin, sabaha temiz tablo kalsın" diyor. Doğru davranış hangisidir?

## A
Takas dosyasındaki net tutar kendi kayıtlarımızın üzerine yazılır; karşı taraf otorite olduğu için fark böylece kapanır
🤖 brüt tutar kaybolur, iade ve itiraz dayanağı silinir; fark kapanmaz, yalnız görünmez olur.

## B
Dosya doğrulamayı geçtiyse farklar otomatik düzeltilir; gece işi sabaha tutarlı bir tablo bırakmış olur
🤖 hatalı gelen ilk dosya, tek gecede gerçek bir zarar yazar; para üzerinde otomatik düzeltme yapılmaz.

## C
Fark tutarı küçükse bir sonraki güne devredilir; birikim belirli bir eşiği aşınca inceleme başlatılır
🤖 eşiği geliştirici koyamaz; ayrıca küçük fark küçük sorun demek değildir, kökü büyük olabilir.

## D
Farklar üç sınıfa ayrılıp istisna kuyruğuna alınır; düzeltme inceleme sonrası yeni kayıtla yapılır
🤖 Doğru: önce sınıflandırılır, sonra insan inceler, düzeltme geçmişi bozmadan eklenir.

## Doğru
D

## Açıklama
Mutabakat farkı tek bir şey değildir; üç sınıfı vardır ve kökleri farklıdır: **bizde var onlarda yok**, **onlarda var bizde yok**, **ikisinde var tutar farklı** (`252`). Birinci sınıf genellikle kesim saatinin öbür tarafına düşen ya da tahsilata dönmemiş işlemdir. İkinci sınıf zaman aşımı sonrası akıbeti bilinmeyen işlemlerdir; o kayıtların gerçek sonucunu söyleyen tek kaynak takas dosyasıdır (`253`). Üçüncü sınıf kısmi iade, komisyon eşlemesi, kur ya da yuvarlamadan doğar (`226`). Doğru davranış bu sınıflandırmayı yapmak, farkları **istisna kuyruğuna** almak ve incelemeyi insana bırakmaktır. Düzeltme geçmişi değiştirerek değil, ters kayıt ya da düzeltme fişi gibi **yeni bir kayıtla** yapılır; ödeme kaydının durumu geriye çevrilmez (`250`). A şıkkı brüt tutarı yok eder ve iadenin dayanağını siler. B şıkkı en tehlikelisidir: hatalı ya da eksik gelen bir dosya, otomatik düzeltmeyle tek gecede binlerce kaydı bozar. C şıkkı toleransı geliştiricinin belirlemesini önerir; oysa eşik, eskalasyon süresi ve onay iş kuralıdır ve iş biriminden yazılı teyitle alınır. Kapanışta iki şey metrik olarak izlenir: gün bazlı fark tutarı ve istisna kuyruğundaki en eski kaydın yaşı (`104`).

# tgs-4 | Uzman

## Soru
.NET'e taşınacak bir gün sonu modülünde tutarlar `double` ile taşınıyor, kayıtta tek bir tutar alanı var ve yuvarlama her yerde iki basamağa sabitlenmiş. En yerinde düzeltme hangisidir?

## A
Tip korunur ama tüm rapor ve ekran çıktıları iki basamağa yuvarlanır; böylece görünen tutarlar birbirini tutar
🤖 bozulma kaynakta olur; sunumda yuvarlamak biriken farkı gizler, mutabakatta yine ortaya çıkar.

## B
Tutarlar ondalık duyarlı tiple taşınır; brüt, komisyon ve net ayrı alan olur; basamak sayısı para biriminden gelir
🤖 Doğru: tip, alan ayrımı ve ölçek birlikte kararlaştırılır.

## C
Tip ondalık duyarlıya çevrilir ama tek tutar alanı korunur; komisyon gerektiğinde orandan geri hesaplanır
🤖 oran işlem tipine ve sözleşmeye göre değişir; geri hesaplanan komisyon mutabakatta kanıt değeri taşımaz.

## D
Tutarlar metin olarak saklanıp karşılaştırma da metin üzerinden yapılır; böylece hiçbir dönüşüm kaybı oluşmaz
🤖 metin aritmetik yapmaz; toplama ve sıralama için yine sayıya dönülür, kural hiçbir yere yazılmamış olur.

## Doğru
B

## Açıklama
Para hesabında dört karar birlikte verilir. Birincisi **tip**: .NET tarafında `decimal`, Java tarafında `BigDecimal`. Kayan noktalı tipler ikili gösterim kullandığı için ondalık kesirleri tam tutamaz; fark tek işlemde görünmez, milyon işlemde mutabakatı bozar (`226`). İkincisi **alan ayrımı**: brüt tahsilat, kesilen komisyon ve hesaba geçen net üç ayrı gerçektir. Takas dosyasından gelen net tutarı brütün üzerine yazmak hem iadenin dayanağını hem muhasebedeki gider kalemini yok eder (`252`, `254`). Üçüncüsü **ölçek**: her para biriminin ondalık basamak sayısı aynı değildir, bu yüzden basamak sayısı koda gömülmez, para birimi tanımından okunur. Dördüncüsü **yuvarlama**: yönü ve hesabın hangi adımında uygulanacağı sonucu değiştirir; satır satır yuvarlayıp toplamak ile toplayıp yuvarlamak farklı sayı verir. Kural iş biriminden yazılı alınır ve tek yerde uygulanır. Java ile C# arasında bir ince fark da vardır: Java'da `BigDecimal.equals` ölçeği karşılaştırdığı için `compareTo` kullanılır, .NET'te `decimal` eşitliği ölçeğe takılmaz (`281`). A şıkkı bozulmayı sunumda gizler. C şıkkı komisyonu tahmine indirger. D şıkkı ise hesabı hiç çözmez; veritabanı tarafında da tutar için ondalık duyarlı kolon tipi seçilir (`283`).

# tgs-5 | Uzman

## Soru
Gün sonu işi bittiğinde "ters kayıt oluştur" olayları kuyruğa yayımlanıyor. Bir tüketici yeniden başlatıldıktan sonra bazı ters kayıtların iki kez oluştuğu görülüyor. Kalıcı çözüm hangisidir?

## A
Kuyruk tam olarak bir kez teslim edecek biçimde yapılandırılır; tekrar sorunu altyapı ayarıyla kökten çözülür
🤖 yaygın garanti en az bir kezdir; uçtan uca tek teslim varsayımı üretimde tutmaz.

## B
Tüketici tek iş parçacığına indirilir; sıralı işleme yapıldığında aynı mesaj iki kez işlenemez
🤖 sıra tekrarı önlemez; onaylanmadan yeniden teslim edilen mesaj tek iş parçacığında da iki kez gelir.

## C
Mesaj alınır alınmaz kuyruktan silinir; işleme başlamadan onay verilince yeniden teslim hiç oluşmaz
🤖 tekrarı önler ama kaybı üretir: işleme sırasında düşen tüketicinin mesajı bir daha hiç gelmez.

## D
Tüketici doğal anahtar üzerinde tekilleştirilir; olay da iş verisiyle aynı işlemde giden kutusuna yazılır
🤖 Doğru: tekrar kaçınılmazdır, sonucu değişmeyen tüketici ve giden kutusu ikisini birden kapatır.

## Doğru
D

## Açıklama
Mesajlaşmada yaygın garanti **en az bir kezdir**: onay gelmeden önce düşen bir tüketicinin mesajı yeniden teslim edilir (`121`). Dolayısıyla tekrar bir arıza değil, normal davranıştır ve tasarım buna göre yapılır. Para üreten bir tüketicide çözüm iki parçalıdır. Birincisi **tekrar edilse de sonucu değişmeyen tüketici**: ters kayıt, doğal bir anahtar üzerinde benzersizlik kısıtıyla yazılır ve ikinci deneme veritabanı seviyesinde reddedilir. Uygulama kodundaki "önce bak, yoksa yaz" kontrolü iki eşzamanlı tüketicide yarışa açıktır; kısıt bu aralığı kapatır (`130`, `253`). İkincisi **giden kutusu**: olay, iş verisiyle aynı veritabanı işleminde bir tabloya yazılır, ayrı bir süreç okuyup yayımlar. Böylece "kayıt var ama olay yok" ya da tersi durumu imkânsızlaşır (`116`). Kalıcı olarak başarısız mesajlar için ölü mektup kuyruğu tanımlanır; sonsuz yeniden deneme zehirli mesaj döngüsü üretir (`122`). A şıkkı uçtan uca tek teslim varsayar; bu varsayım üretimde tutmaz. B şıkkı sırayı tekrarla karıştırır. C şıkkı tekrarı önlerken kaybı üretir ve para işinde kayıp, tekrardan daha pahalıdır.

# tgs-6 | Uzman

## Soru
Mülakatta görüşmeci soruyor: "Bizim kesim saatimiz kaçtır, kaç gün sonra takas oluruz, komisyon oranı ne kadar olur?" Aday bu kurumu tanımıyor. En güçlü cevap hangisidir?

## A
Piyasada yaygın olan kesim saati ve komisyon aralığı söylenir; yaklaşık bir değer vermek alan bilgisini gösterir
🤖 doğrulanabilir olmayan sayı, yanlış çıktığında tüm anlatımı şüpheli hâle getirir.

## B
"Bilmiyorum" denip konu kapatılır; kuruma özel bir bilgiyi tahmin etmeye çalışmak her koşulda yanlıştır
🤖 dürüst ama eksik; bu değerlerin ne olduğunu ve nereden okunduğunu söylememek bilgiyi göstermez.

## C
Bunlar kuruma ve sözleşmeye göre değişen parametrelerdir; koda gömülmez, yapılandırmadan okunur ve yazılı teyitle sabitlenir
🤖 Doğru: doğru cevap sayının kendisi değil, sayının nereden geldiğidir.

## D
Önceki iş yerindeki değerler söylenir; sektör benzer çalıştığı için bu sayıların buraya da uyması beklenir
🤖 başka kurumun değerini bu kuruma taşımak hem yanlış hem de gizlilik açısından sakıncalıdır.

## Doğru
C

## Açıklama
Bu soru bilgi sınavı gibi görünür ama aslında **duruş** sınavıdır. Kesim saati, takas günü, komisyon oranı ve mutabakat tolerans eşiği kurumdan kuruma, sözleşmeden sözleşmeye değişir. Bu yüzden doğru cevap bir sayı değildir; sayının **nereden okunduğudur**. Güçlü cevap üç parçalıdır. Birincisi, bunların parametre olduğunu söylemek. İkincisi, sistemde nasıl durduklarını söylemek: koda gömülmez, yapılandırmada tutulur, değişimi izlenir ve değiştiğinde geçmiş kayıtların anlamı bozulmaz. Üçüncüsü, değerin nasıl belirlendiğini söylemek: iş biriminden ya da ilgili anlaşmadan yazılı teyitle alınır, sözlü bilgiyle sabitlenmez. Bu cevap aynı zamanda kesim saatinin neden önemli olduğunu göstermeye de kapı açar: hangi işlemin hangi takas dönemine gireceğini, dolayısıyla iptalin hangi araçla yapılacağını o saat belirler (`252`). A ve D şıkları doğrulanamayan sayı üretir; biri tahmine, diğeri başka kurumun bilgisine dayanır ve ikisi de riskli. B şıkkı dürüsttür ama yarım kalır: neyi bilmediğini söylemek iyidir, o bilginin nereden geleceğini söylememek eksiktir. Aynı disiplin deneyim beyanında da geçerlidir: bilmediğini söyle, bildiğini somutla, eksiği kapatma planını ver (`256`, `195`).
