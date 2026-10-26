---
hafta: 8
gun: 3
baslik: "Evaluation: AI-as-judge"
---

## Ders

### Bugün ne öğreneceksin?
Kod için testin ne olduğunu biliyorsun: kırmızı ışık yanar, yanlış olduğunu söyler. Peki bir **özetin** ya da bir açıklamanın doğru olduğunu makine nasıl söyler? Bugün açık uçlu AI çıktısının nasıl ölçüldüğünü öğreniyorsun: altın küme, rubrik ve modelin modeli puanlaması.

### Temel kavramlar
- **Değerlendirme (evaluation):** Bir AI çıktısının ne kadar iyi olduğunu **tekrarlanabilir** biçimde ölçmek. "İzlenimim iyi" ölçüm değildir; ölçüm, aynı girdiyle yeniden çalıştırılabilen şeydir.
- **Altın küme (golden set):** Beklenen cevabı elle onaylanmış, sabit kalan örnek listesi (20-50 örnek yeter). Prompt'u, modeli ya da parçalama ayarını değiştirdiğinde aynı kümeyi koşturup **önce/sonra** karşılaştırırsın. Küme değişirse karşılaştırma anlamını kaybeder.
- **Rubrik (rubric):** Puanlama ölçütleri cetveli: maddeler, puan aralıkları ve "tam puan neye denir" tanımı.
- **AI-as-judge (yargılayan model):** Bir çıktıyı, **ikinci bir modelin** rubriğe göre puanlaması. Kritik olan model değil, **rubriktir** — rubrik yoksa judge yalnız kanaat bildirir.
- **Yayın öncesi (offline) ölçüm:** Altın küme üzerinde, kullanıcıya çıkmadan yapılan ölçüm. **Canlı (online) ölçüm:** gerçek kullanıcı davranışından gelen sinyal — cevap kopyalandı mı, kullanıcı soruyu tekrar sordu mu, başparmak yukarı/aşağı. İkisi birbirinin yerine geçmez: offline hızlı ve kontrollüdür ama gerçek kullanımı temsil etmeyebilir; online gerçektir ama yavaş, gürültülü ve zarar oluştuktan sonra gelir.

Sınav kağıdını öğrencinin kendisi okumaz. Kağıdı, elinde rubrik olan **başka biri** okur. "Bu cevap iyi mi?" diye sorarsan herkes kendi cevabına iyi der; "gerekçe var mı, 0-20 puan" diye sorarsan cevap ölçülebilir hâle gelir.

Judge kurulumunun dört parçası: **girdi** (değerlendirilecek çıktı + görev), **rubrik**, **judge** (üreten modelden bağımsız bir çağrı, ideal olarak farklı model), **çıktı** (madde madde puan + **gerekçe**; yalnız sayı isteme).

### Somut örnek
"AI Öğretmen'in gün özeti" için 5 maddelik rubrik (her biri 0-20, toplam 100):
- **Doğruluk:** Olgusal hata var mı? Tam puan = tek bir yanlış ifade yok.
- **Eksiksizlik:** Günün ana kavramı ve alt maddeleri kapsandı mı? Tam puan = kilit kavram atlanmamış.
- **Gerekçe:** "Şöyledir" demekle kalmayıp **neden** öyle olduğunu açıklıyor mu?
- **Örnek:** Somut, projeden ya da gerçek hayattan bir örnek var mı?
- **Netlik:** Jargon açıklanmış mı, taranabilir mi? Tam puan = yeni başlayan okur takılmadan anlıyor.

Tuzaklar, hepsi somut:
- **Pozisyon yanlılığı:** İki cevabı yan yana verirsen judge **önce geleni** kayırır. *Çözüm:* aynı çifti sırayı ters çevirip ikinci kez sor; kazanan değişiyorsa sonuç güvenilmezdir.
- **Kendine benzeyeni sevme:** Judge kendi ürettiğine ya da kendi üslubuna benzeyene yüksek puan verir — Hafta 1'deki "ajan kendi işini abartır" eğiliminin judge hâli. *Çözüm:* üreten ve yargılayan ayrı çağrılar, tercihen ayrı modeller.
- **Uzunluk yanlılığı:** Uzun cevap "daha çalışılmış" görünür. *Çözüm:* rubrikte uzunluk maddesi **yok**; kısa ve eksiksiz cevap tam puan alır.
- **Sayı-tek-başına:** Yalnız "78/100" isteyen judge tutarsızdır. *Çözüm:* her madde için 1 cümle gerekçe zorunlu.
- **Judge'ın sınırı:** Judge nihai hakem değildir. Öznel ve yüksek riskli kararlarda insan örneklem denetimi şarttır; judge puanlarını arada bir insan puanlarıyla karşılaştırıp kalibre et.

### Sık yapılan hata
- **Rubriksiz "bu iyi mi?" sormak** — ajanın kendini onaylaması.
- **Altın kümeyi büyütmeye çalışmak.** 30 iyi seçilmiş örnek, 500 rastgele örnekten faydalıdır ve gerçekten çalıştırılır.
- **Kümeyi sessizce değiştirmek.** Puan yükselince "iyileştik" dersin; oysa sınav kolaylaşmıştır.
- **Yalnız canlı sinyale güvenmek.** Hata kullanıcıya ulaştıktan sonra ölçmüş olursun.

### Görevlere geçmeden önce
Rubriğini yazarken her madde için "tam puan neye denir" cümlesini de yaz — bu cümle yoksa madde ölçüt değil, temennidir. Sonra rubriği bir tanıdığına ver ve aynı çıktıyı puanlatmayı düşün: iki puan birbirine yakın çıkmıyorsa rubrik yeterince net değildir.

**Derinleşmek istersen:** "Bu haftanın kaynakları" kutusundaki **Değerlendirme-Güdümlü Geliştirme (EDD) ve LLM Gözlemlenebilirliği** metninin §10 "Ölçümü ölçmek — beş ek disiplin (AIEFS ekleri)" bölümü, bugünkü judge'ın yanlış şeyi ölçmemesi için somut araçlar verir. Oradaki §10.2 (eşleştirilmiş karşılaştırma + bootstrap), küçük bir altın kümede "A, B'den iyi" demeden önce farkın gürültü mü gerçek mi olduğunu sınar; §10.3 ise bir koruma filtresini iki yönden, eksik ret ve aşırı ret olarak ölçmeyi öğretir — çünkü tek bir sayı iki farklı arızayı birbirine katar. Rubriği yazmak için şart değil; önce/sonra karşılaştırmana güvenmek istersen oku.

## Görevler
- [ ] Evaluation bölümünü oku
- [ ] Kendi AI özelliğin için 5 maddelik bir değerlendirme rubriği yaz
