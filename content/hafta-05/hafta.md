---
hafta: 5
baslik: "Mimari Düşünme I: Nitelikler ve Trade-off"
hedef: "Fundamentals of Software Architecture (2e) Bölüm 1–8: mimari karakteristikler, trade-off analizi, modülerlik."
kaynak: "Kitap: Fundamentals of Software Architecture 2e (Richards & Ford) — ilk yarı"
---

## Quiz
### S1: Yazılım mimarisinin 'birinci yasası' nedir?
- [ ] İyi mimari, sistemin tüm niteliklerini aynı anda en üst seviyeye çıkarır
- [x] Her mimari karar bir takastır; kazancının mutlaka bir de bedeli vardır
- [ ] En yeni teknolojiyi seçen mimari, uzun vadede daima daha az maliyetlidir
- [ ] Doğru mimari başta bir kez kurulur, sonrasında uzun süre değiştirilmeden yaşatılır
> Açıklama: Richards & Ford'un birinci yasası: yazılım mimarisinde her şey bir takastır (trade-off). Mimarlık, bağlamdan bağımsız "en iyi"yi bulmak değil; bu bağlamda "en az kötü"yü seçip bedelini açıkça söylemektir. Bedelsiz görünen çözümde bedeli henüz görmemişsindir.

### S2: 'Neden, nasıldan önemlidir' ilkesinin pratik karşılığı hangisidir?
- [ ] Kodun her satırına ne yaptığını anlatan bir yorum satırı eklemek
- [ ] Tüm tasarımı ayrıntılı UML diyagramlarıyla baştan sona belgelemek
- [ ] Karar öncesinde daha uzun ve daha kalabalık toplantılar düzenlemek
- [x] Kararın gerekçesini ve feda edilenleri bir ADR'ye yazıp saklamak
> Açıklama: "Nasıl" zaten kodun kendisinde yazılıdır; zamanla kaybolan şey "neden"dir. Gerekçe kayıtlı değilse gelecekteki ekip (ve ajan) bilinçli bir takası "hata" sanıp geri alır. ADR tam olarak bunun için vardır.

### S3: 'Kampanya günlerinde trafik 10 katına çıkar' cümlesi en doğrudan hangi mimari niteliği ima eder?
- [ ] Portability (taşınabilirlik): sistemi başka bir platforma taşıyıp orada çalıştırabilmek
- [ ] Scalability (ölçeklenebilirlik): kullanıcı sayısının yıllar içinde kademeli olarak büyümesi
- [x] Elasticity (esneklik): ani yük sıçramasına kısa sürede uyum sağlayabilmek
- [ ] Maintainability (bakım kolaylığı): değişikliği ucuz ve güvenli biçimde yapabilmek
> Açıklama: Anahtar sözcük "kampanya günü": yük kalıcı değil, ani ve kısa sürelidir. Kademeli büyüme scalability ister, ani sıçrama elasticity ister. İş birimi niteliğin adını hiç söylemez; adını koymak mimarın işidir — adlandırılmayan nitelik ölçülemez, ölçülemeyen nitelik korunamaz.

### S4: Mimari fitness function (uygunluk fonksiyonu) nedir?
- [ ] Kod stilini ve girintileri otomatik düzelten biçimlendirme aracı
- [ ] Sürüm çıkmadan önce bir kez koşulan büyük yük ve performans testi
- [x] Bir mimari niteliği sürekli ve mekanik biçimde ölçen otomatik kontrol
- [ ] Mimarın her sprint sonunda elle doldurduğu kalite değerlendirme formu
> Açıklama: Kilit sözcükler "sürekli" ve "mekanik": kural insan dikkatine değil, her derlemede koşan bir teste bağlanır. Bu projedeki ArchUnit katman testi (MimariTestleri) bir fitness function'dır; tek seferlik yük testi ya da elle doldurulan form değildir.

### S5: Dinamik connascence (ör. iki çağrının yürütme sırası) neden statik connascence'tan (ör. ad veya tip bağı) daha tehlikelidir?
- [x] Derleyici ve mimari testler dinamik bağı göremez; hata ancak çalışma anında çıkar
- [ ] Dinamik bağ her zaman daha çok sınıfı etkiler, statik bağ tek sınıfta kalır
- [ ] Dinamik bağ yalnızca nesne yönelimli dillerde, statik bağ her dilde ortaya çıkar
- [ ] Dinamik bağ programı yavaşlatır, statik bağın çalışma anı maliyeti sıfırdır
> Açıklama: Statik bağ kodun kendisinde durur; bozulunca derleme veya test kırmızı yanar — ucuz hata. Dinamik bağ kodda görünmez, canlıda günler sonra tuhaf bir davranış olarak patlar — pahalı hata. Kural: dinamik bağı statik bağa çevir, yani gizli sözleşmeyi imzaya taşı. Ajanlar da yalnızca görünür bağları okuyabilir.
