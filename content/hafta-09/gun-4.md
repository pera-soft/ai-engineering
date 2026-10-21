---
hafta: 9
gun: 4
baslik: "Mimarî mühürleme: ArchUnit ile hexagonal"
---

## Ders

### Bugün ne öğreneceksin?
Üç gündür bir sınır çizdin. Bugün o sınırın **kendi kendini savunmasını** sağlayacaksın. Çünkü yazılı kural unutulur, hatırlatılan kural kişiye bağlıdır; çalışan kural ise herkese — sana, yeni gelen arkadaşına ve kodu senin adına yazan ajana — aynı şekilde uygulanır. Günün sonunda "domain hiçbir adapter'a bağımlı olamaz" cümlesini bir teste dönüştürmüş olacaksın.

### Temel kavramlar
- **ArchUnit:** Java'da yazılmış, kodun kendi yapısını sınayan bir test kütüphanesi. Normal testler "bu fonksiyon doğru sonucu veriyor mu?" diye sorar; ArchUnit "bu paket şu paketi import ediyor mu?" diye sorar. İhlal varsa test kırmızı yanar, tıpkı bozuk bir hesaplama gibi.
- **Fitness function (uygunluk fonksiyonu):** mimari bir niteliği (bağımlılık yönü, döngüsüzlük, performans) sürekli ölçen otomatik kontrol. Belge değil, ölçüm.
- **Bloklayıcı (CI'da):** kural ihlal edilince birleştirmeyi (merge) gerçekten engelleyen kontrol. Uyarı verip geçen kontrol, kısa sürede görmezden gelinir.

Fikir basittir: Hafta 4'te öğrendiğin ArchUnit'i bu haftanın hexagonal kuralına bağlarsın. "Domain, adapter'ı import etmesin" cümlesi artık bir wiki maddesi değil, build'in geçme şartıdır.

### Somut örnek (bu proje)
`src/test/java/tr/velzon/aiogretmen/MimariTestleri.java` bunun canlı hâlidir; dört kural içerir:

- `katman_kurali` — `types → config → repo → service → ai → {web, mcp}` yönünü zorlar. `whereLayer("Web").mayNotBeAccessedByAnyLayer()` satırı, "web'i kimse çağıramaz" der; yani dış halka iç halka tarafından görülemez. Ayrıca `withOptionalLayers(false)` vardır: bir paket yeniden adlandırılırsa katman boşalır ve build **yine** kırmızı olur. Kitabın Bölüm 12'deki uyarısı budur — sessizce geçen bir fitness function işe yaramaz.
- `dongusuz` — paketler arası döngüsel bağımlılığı yasaklar; döngü, sınırın çoktan eridiğinin işaretidir.
- `claude_yalniz_ai_paketinden` — Spring AI/Anthropic tiplerinin `ai/` paketi dışında görünmesini yasaklar. Bu tam olarak bir **outgoing port mührüdür**: satıcı teknolojisi adapter'da kalır.
- `types_bagimsiz` — en içteki halkanın hiçbir üst katmanı tanımamasını garanti eder.

Dikkat et: kuralların Javadoc'unda `[Guidance: ...]` notları var. Test kırmızı yandığında ne yapılacağını da söylerler ("bağımlılığı ters çevir, kuralı gevşetme; gerçekten yeni katman gerekiyorsa önce ADR aç"). Kuralın yanına konan bu cümle, kuralı okuyan insanın ya da ajanın kolay yola sapmasını engeller.

### Sık yapılan hata
En yaygın hata, test kırmızı yanınca **kuralı gevşetmektir**: paketi istisna listesine eklemek, katmanı `optional` yapmak, testi `@Disabled` ile susturmak. Bu, yangın alarmını sökmeye benzer; koku kaybolur, yangın kalır. Doğrusu, bağımlılığı tersine çevirmek ya da kodu doğru katmana taşımaktır. İkinci yaygın hata: kuralı yerel makinede çalıştırıp CI'da bloklayıcı yapmamak — o zaman kural, sadece hatırlayanlar için vardır. Üçüncüsü: kuralı çok geç yazmak; sınır önce mühürlenmezse, ilk ihlal birkaç yüz satır sonra "artık düzeltmesi pahalı" olur.

### Görevlere geçmeden önce
Kuralı yazarken en küçük hâliyle başla: tek bir yasak ("domain paketi adapter paketlerini import edemez"). Sonra onu **kasten kır** — domain'e bir `@RestController` import'u ekle, `./mvnw -Dtest=MimariTestleri test` komutunu çalıştır ve testin gerçekten kırmızı yandığını gör. Yanmıyorsa kuralın yanlış yazılmıştır ve seni boş yere rahatlatır. Kırmızıyı gördükten sonra import'u geri al, kuralı CI'da bloklayıcı hâle getir ve Javadoc'una tek cümlelik bir yönlendirme notu bırak.

## Görevler
- [ ] Hexagonal sınırları ArchUnit kuralına dök
- [ ] CI'da bloklayıcı yap
