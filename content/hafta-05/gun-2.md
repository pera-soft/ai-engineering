---
hafta: 5
gun: 2
baslik: "Mimari karakteristikler (-ility'ler)"
---

## Ders

### Bugün ne öğreneceksin?
Bugün bir sistemin "ne yaptığı" ile "nasıl yaptığı"nı ayırmayı, iş biriminin cümlesinin arkasındaki **söylenmemiş** nitelikleri adlandırmayı ve her nitelik için bedelini masaya koymayı öğreneceksin.

### Temel kavramlar
- **Fonksiyonel gereksinim (functional requirement):** Sistemin *ne yapacağı*. "Öğrenci quiz çözebilsin." Bunlar menüdeki yemeklerdir.
- **Mimari nitelik / karakteristik (architecture characteristic):** Sistemin o işi *nasıl* yapacağı. "Quiz 200 milisaniyede açılsın", "veriler sızmasın", "yeni soru tipi eklemek yarım gün sürsün". Türkçede çoğu "-lik/-lık" ile biter, İngilizcede "-ility" ile — bu yüzden kısaca **"-ility'ler"** denir.
- **Örtük (implicit) nitelik:** İş biriminin **söylemediği ama kastettiği** nitelik. Benzetmesi: müşteri terziye *"düğüne gideceğim"* der; "kumaş şu ağırlıkta olsun" demez. Ama *düğün* sözcüğü bunların hepsini ima eder. Terzinin işi, cümlenin arkasındaki gerçek gereksinimi çıkarmaktır.
- **Öncelik ve çatışma:** Nitelikler bir liste değil, bir **sıralamadır**. Hepsini aynı anda en üste çıkaramazsın; birini yükseltmek çoğu zaman ötekini indirmek demektir (Gün 1: her şey bir takastır). Deneyimli mimar en fazla **üç** niteliği "birinci sınıf" ilan eder.

Senin işin üç adımlıdır: **iş cümlesini duy → arkasındaki niteliği adlandır → bedelini söyle.**

### Somut örnek
Aynı üründen dört cümle, dört farklı örtük nitelik:

| İş biriminin cümlesi | Örtük nitelik | Bedeli (feda edilen) |
|---|---|---|
| "Kampanya günü trafik **10x** olur" | **elasticity** (esneklik: ani yüke otomatik uyum) + **availability** (kullanılabilirlik) | **basitlik** ve **maliyet** — tek sunuculu kurulum yetmez; ölçeklenen, dolayısıyla daha karmaşık ve pahalı bir yapı gerekir |
| "Rakip bu özelliği çıkarmadan yetişmeliyiz" | **deployability** (dağıtılabilirlik) + **maintainability** | **performans** ve **cilalı tasarım** — hızlı çıkmak için bazı optimizasyonlardan vazgeçilir |
| "Müşteri verisi asla sızmasın" | **security** (güvenlik) | **performans** (şifreleme maliyeti) + **kullanım kolaylığı** (ek doğrulama adımları) |
| "Sistem çökerse hangi işlem kaybolduğunu bilmeliyiz" | **auditability** (denetlenebilirlik) + **recoverability** (kurtarılabilirlik) | **performans** ve **depolama maliyeti** (her işlem kayda geçer) |

- **Dikkat:** Hiçbir cümlede niteliğin **adı geçmiyor**. İş birimi "elasticity istiyorum" demez; **"kampanya günü"** der. Adı sen koyacaksın.
- **Sonuç:** Niteliği adlandırdığın an **bedelini de masaya koyabilirsin**: *"10x trafiği kaldıracaksak basit tek-sunuculu kurulumdan vazgeçiyoruz — onayınız var mı?"* İşte bu cümle bir **mimarlık** cümlesidir.
- **Bu projeden:** AI Öğretmen'in birinci sınıf nitelikleri *basitlik*, *maintainability* ve *güvenlik*tir (uygulama yalnız 127.0.0.1'e bağlanır, ADR-0007). Elasticity bilinçli olarak listenin dışındadır: tek kullanıcılı bir eğitim uygulamasında ani yük sıçraması diye bir olgu yoktur.

### Sık yapılan hata
- **Her niteliği "kritik" ilan etmek.** Sekiz niteliğin hepsi birinci sınıfsa hiçbiri birinci sınıf değildir; öncelik listesi ancak kısa olduğunda karar verdirir.
- **Ölçüsüz nitelik yazmak.** "Sistem hızlı olsun" bir gereksinim değildir. "Quiz sayfası 200 ms'de açılsın" gereksinimdir — çünkü sınanabilir.
- **Niteliği adlandırmadan tasarıma atlamak.** "Kuyruk ekleyelim" bir çözümdür; hangi niteliği koruduğunu söyleyemiyorsan hangi bedeli ödediğini de bilmiyorsundur.

### Görevlere geçmeden önce
Adlandırılmamış nitelik = **ölçülemeyen** nitelik = **korunamayan** nitelik. Hafta 4'ün ArchUnit dersinin kuralı burada da geçerli: bir kural ancak mekanik olarak sınanabiliyorsa yaşar (Gün 4'te buna *fitness function* diyeceğiz). Bu yüzden ilk görevde seçtiğin üç nitelik için **nasıl ölçüleceğini** de bir cümleyle yaz. İkinci görevde çatışmayı ararken ipucu: *basitlik* neredeyse her nitelikle çatışır.

## Görevler
- [ ] Kendi ürünün için en kritik 3 karakteristiği seç ve gerekçele
- [ ] Bu 3'ün hangi ikisinin birbiriyle çatıştığını yaz
