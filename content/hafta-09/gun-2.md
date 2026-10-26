---
hafta: 9
gun: 2
baslik: "Port ve adapter'lar"
---

## Ders

### Bugün ne öğreneceksin?
Dün "oklar içeri baksın" dedik. Bugün bunun **nasıl** yapıldığını öğreneceksin: port (kapı) ve adapter (bağdaştırıcı). Günün sonunda incoming ile outgoing port'u karıştırmadan ayırabilecek ve "bağımlılığı tersine çevirmek" cümlesinin kodda karşılığını gösterebileceksin.

### Temel kavramlar
Elektrikli tıraş makineni düşün: cihazın tek bir standart fişi vardır, ama priz ülkeye göre değişir. Cihazı değiştirmezsin, araya bir adaptör takarsın.

- **Cihaz = domain.** Değişmemesi gereken değerli kısım.
- **Fiş = PORT = arayüz (interface).** Dış dünyaya sunulan tek, sabit şekil.
- **Priz = dış dünya.** Veritabanı, web çatısı, ödeme sağlayıcısı — hepsi değişken.
- **Adaptör = ADAPTER.** Farkı yutan parça.

Kilit fikir: cihaz prizin şeklini **bilmez** — bilmemesi de gerekir. Bilseydi her yeni ülke için cihazı yeniden üretmen gerekirdi.

İki port türü vardır ve yönleri zıttır:

1. **Incoming port (içeri gelen / driving):** bir iş kuralı arayüzüdür ("para transferi yap"). Domain onu **uygular**, dışarıdaki web adapter **çağırır**. Sorusu: "Bu uygulama ne yapabilir?"
2. **Outgoing port (dışarı giden / driven):** bir altyapı ihtiyacı arayüzüdür ("hesabı kaydet"). Domain onu **çağırır**, persistence adapter **uygular**. Sorusu: "Bu uygulamanın dışarıdan neye ihtiyacı var?"

İkisi de domain'in içinde tanımlanır. Outgoing port'un domain'de yaşaması şaşırtıcı gelir; sihir tam oradadır.

```java
// INCOMING PORT — domain paketinde. "Uygulama NE yapabilir?"
public interface ParaTransferiUseCase {
    boolean transferEt(Long kaynak, Long hedef, long kurus);
}

// OUTGOING PORT — bu da domain paketinde! İhtiyacı domain tanımlar.
public interface HesapKaydetPort {
    void kaydet(Hesap hesap);
}

// DOMAIN (cihaz): incoming'i UYGULAR, outgoing'i ÇAĞIRIR.
// Ne Spring'i, ne JPA'yı, ne HTTP'yi bilir; hiçbir adapter'ı import etmez.
public class ParaTransferiService implements ParaTransferiUseCase {
    private final HesapKaydetPort port;            // ARAYÜZ — sınıf değil
    public ParaTransferiService(HesapKaydetPort port) { this.port = port; }

    @Override public boolean transferEt(Long kaynak, Long hedef, long kurus) {
        // bakiye yeter mi, limit aşıldı mı: İŞ KURALI burada
        port.kaydet(guncelKaynak);                 // hangi veritabanı? BİLMİYOR
        port.kaydet(guncelHedef);
        return true;
    }
}

// WEB ADAPTER: incoming port'u ÇAĞIRIR (fişi HTTP prizine takar)
@RestController
public class WebAdapter {
    private final ParaTransferiUseCase useCase;    // ARAYÜZ
    public WebAdapter(ParaTransferiUseCase u) { this.useCase = u; }
    @PostMapping("/transfer") public ResponseEntity<Void> transfer(@RequestBody TransferIstegi i) {
        return useCase.transferEt(i.kaynak(), i.hedef(), i.kurus())
                ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
}

// PERSISTENCE ADAPTER: outgoing port'u UYGULAR. JPA burada yaşar, domain'e sızmaz.
@Component
public class JpaHesapAdapter implements HesapKaydetPort {
    private final HesapJpaRepository repo;
    public JpaHesapAdapter(HesapJpaRepository repo) { this.repo = repo; }
    @Override public void kaydet(Hesap hesap) { repo.save(HesapJpaEntity.of(hesap)); }
}
```

Okları oku: `WebAdapter` ve `JpaHesapAdapter` domain'i **import eder**; `ParaTransferiService` hiçbir adapter'ı import etmez. Ok hep aynı yönde: **adapter → domain.** Normalde iş mantığı veritabanına bağımlı olurdu (`Service → JpaRepository`); port'u domain'in içine koyup adapter'a uygulattığın an ok **tersine döner**. "Dependency Inversion" (SOLID'in D'si) tam olarak budur.

### Somut örnek (bu proje)
- **Port:** `ai/AnlamArama` — bir arayüz: `indexle`, `benzerleri(sorgu, k)`, `temizle`, `sayac`. "Anlam araması" ihtiyacının şekli budur.
- **Adapter:** `ai/SpringAiAnlamArama implements AnlamArama` — Spring AI'nın `VectorStore`, `EmbeddingModel`, `Document` tipleri **yalnız burada** yaşar.
- **Cihaz:** `service/KitapService` sadece `AnlamArama` arayüzünü ve `NotParcasi`/`BenzerNot` tiplerini görür; deponun `SimpleVectorStore` mu yoksa yarın pgvector mı olduğunu bilmez. ADR-0005 bunu açıkça yazar: depo değişirse "`AnlamArama` arayüzü sayesinde service değişmez". Prizi değiştirmek = yeni adapter yazmak.

### Sık yapılan hata
Outgoing port'u adapter paketine koymak: o an ok tersine döner ve inversion kaybolur — port **domain'in içinde** durmalıdır. İkinci hata: her sınıfa arayüz üretip "port yaptım" sanmak; port bir sınırdır, süs değil. Üçüncüsü: domain sınıfına `@Entity` ya da `@RestController` iliştirmek — cihaz prizin şeklini öğrenmiş olur.

### Görevlere geçmeden önce
Şunu aklında tut: port varsa test bedavadır. Gerçek veritabanı yerine `class SahteHesapAdapter implements HesapKaydetPort` yazıp içine bir `Map` koyarsın; testler milisaniyede koşar ve iş kuralını ölçer, altyapıyı değil. Kağıt üstünde bölerken her kutunun altına "bunu kim çağırıyor, kim uyguluyor?" diye yaz.

**Derinleşmek istersen:** "Bu haftanın kaynakları" kutusundaki **Canlıda LLM** metninin §4 "AI gateway — giden port'un adaptörü" bölümü, bugünkü outgoing port'un bu projedeki bir başka örneğini gösterir: servisler yalnız `ai/OgretmenAiClient` arayüzünü görür, Spring AI ve sağlayıcı ayrıntısı ise adaptörde (`ai/SpringAiOgretmenClient`) kalır. Aynı kutudaki **Araç Çağrısı Güvenilirliği** metninin §3 "Yeniden deneme sınıfları — önce sınıflandır, sonra dene" bölümü de adaptör sınırında hata alınınca ne olacağını anlatır: dışarı giden bir çağrıyı yeniden denemeden (retry) önce onun güvenli bir okuma mı, idempotency anahtarlı (idempotency key — "aynı iş iki kez yapılmaz" güvencesi) bir yazma mı, yoksa güvensiz bir yazma mı olduğunu bilmen gerekir. İkisi de opsiyoneldir; kağıt üstünde bölerken "bu adaptör hata alırsa ne olur?" diye de sormak istersen işine yarar.

## Görevler
- [ ] Kitabın örnek yapısını (buckpal) incele
- [ ] Küçük bir modülü port/adapter yapısına taslak olarak böl (kağıt üstünde)
