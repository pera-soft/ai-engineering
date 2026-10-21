---
hafta: 9
baslik: "Hexagonal / Clean Mimari (Elle Kirlen)"
hedef: "Get Your Hands Dirty on Clean Architecture ile port-adapter mimarisini Java'da uygula; ArchUnit ile mühürle."
kaynak: "Kitap: Get Your Hands Dirty on Clean Architecture 2e (Hombergs) — tamamı + uygulama"
---

## Quiz
### S1: Hexagonal mimaride bağımlılık yönü kuralı nedir?
- [ ] Dışarıya, adapter'lara doğru akar
- [x] İçeriye, domain'e doğru akar
- [ ] İki yönde de serbestçe akabilir
- [ ] Veritabanı katmanına doğru akar
> Açıklama: Port'ları domain tanımlar, adapter'lar onlara uyar; bu yüzden ok daima adapter → domain yönündedir. Domain hiçbir dış teknolojiyi bilmediği için gerçek veritabanı kurmadan, port'a sahte (fake) bir adapter takarak test edilebilir.

### S2: Katmanlı (layered) mimarinin kitapta eleştirilen ana zaafı nedir?
- [ ] Katmanlar arası çağrıların performansı düşürmesi
- [ ] Katman başına ayrı model yazmayı zorunlu kılması
- [x] İş mantığını veritabanı şemasına tabi kılması
- [ ] Arayüz sayısını artırıp kodu şişirmesi
> Açıklama: Katmanlı yığının en altı persistence'tır; her şey tablodan türetilince domain, şemanın kölesi olur ve iş kuralı sorgulara sızar. Diğer şıklar üslup şikâyetidir, kitabın itirazı değildir.

### S3: Incoming (giriş) port ile outgoing (çıkış) port arasındaki fark nedir?
- [x] Incoming'i domain uygular, outgoing'i adapter uygular
- [ ] Incoming'i adapter uygular, outgoing'i domain uygular
- [ ] Incoming HTTP'dir, outgoing veritabanı sürücüsüdür
- [ ] Incoming arayüzdür, outgoing somut sınıftır
> Açıklama: Incoming port bir use-case arayüzüdür: domain uygular, web adapter çağırır. Outgoing port domain'in dışarıdan beklediği hizmettir: domain çağırır, persistence adapter uygular. İkisi de domain'in içinde tanımlanır; ikisi de arayüzdür.

### S4: Hombergs'in "no mapping" (eşleme yok) stratejisinin bedeli nedir?
- [ ] Her katman için ayrı model yazmayı zorunlu kılar
- [ ] Domain'i çerçeveden ve veritabanından yalıtır
- [ ] Web katmanının domain'i çağırmasını engeller
- [x] Tek model her katmana yayılır, sınırlar yapışır
> Açıklama: No mapping en az kodu yazdırır ama aynı sınıf hem JSON hem tablo hem iş kuralı taşır; birinin ihtiyacı ötekini kirletir. Full mapping tam tersidir: en izole, en çok kod. Strateji seçimi bilinçli bir trade-off'tur, varsayılan değil.

### S5: Hexagonal sınırları kalıcı kılmanın ajan-first yolu nedir?
- [ ] Kuralı ekip wiki sayfasında ayrıntılı belgelemek
- [ ] Kod gözden geçirmede (review) her seferinde hatırlamak
- [x] Kuralı CI'da bloklayıcı bir teste dönüştürmek
- [ ] Ajana her istekte kuralı yeniden hatırlatmak
> Açıklama: Belgelenen kural unutulur, hatırlanan kural kişiye bağlıdır. `MimariTestleri` gibi bloklayıcı bir ArchUnit testi ise her PR'da, insan ya da ajan ayrımı yapmadan aynı sınırı zorlar — bu bir fitness function'dır.
