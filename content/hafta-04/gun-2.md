---
hafta: 4
gun: 2
baslik: "ArchUnit ile katman testi (Java)"
---

## Ders

### Bugün ne öğreneceksin?
Dün "kuralı mekanikleştir" dedik. Bugün bunu Java'da gerçekten yapacaksın: **ArchUnit** ile mimari kuralı normal bir birim testi hâline getireceksin. İş bitince katman diyagramın duvarda asılı bir resim değil, `./mvnw verify` her çalıştığında sınanan bir **çit** olacak.

### Temel kavramlar
- **ArchUnit:** Java kodunun **yapısını** test eden kütüphane. Normal testler davranışı ("2+2=4 mü?") sınar; ArchUnit yapıyı ("web katmanı repo'ya erişiyor mu?") sınar. JUnit ile aynı yerde, test kapsamında (test scope) çalışır.
- **Katman (layer):** benzer sorumluluğu taşıyan paketler kümesi. Bu projede: `types` → `config` → `repo` → `service` → `ai` → `web` / `mcp`.
- **Katman yönü:** bağımlılığın hangi yöne akabileceği. Üst katman alta bakar; alt katman üstü **tanımaz**.
- **Döngüsel bağımlılık (cycle):** A paketi B'ye, B de A'ya bağımlıysa oluşan halka. Test etmeyi, taşımayı ve anlamayı zorlaştırır; ajanın da bağlamını şişirir.
- **`layeredArchitecture()`:** katman yönünü zorlayan ArchUnit kuralı.
- **`slices().beFreeOfCycles()`:** paketleri dilimlere bölüp aralarındaki grafikte halka arayan kural.
- **Bloklayıcı kapı (blocking gate):** kırmızı yandığında birleştirmeyi (merge) durduran kontrol.

### Somut örnek
Bu projede kural `src/test/java/tr/velzon/aiogretmen/MimariTestleri.java` içinde yaşıyor. Çekirdeği şudur:

```java
@ArchTest
static final ArchRule katman_kurali = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .withOptionalLayers(false)
        .layer("Types").definedBy("..aiogretmen.types..")
        .layer("Repo").definedBy("..aiogretmen.repo..")
        .layer("Service").definedBy("..aiogretmen.service..")
        .layer("Web").definedBy("..aiogretmen.web..")
        .whereLayer("Web").mayNotBeAccessedByAnyLayer()
        .whereLayer("Repo").mayOnlyBeAccessedByLayers("Service", "Ai");

@ArchTest
static final ArchRule dongusuz = slices()
        .matching("tr.velzon.aiogretmen.(*)..")
        .should().beFreeOfCycles();
```

Okuması sandığından kolay: önce hangi paketin hangi katman olduğunu **tanımlarsın** (`layer(...).definedBy(...)`), sonra kimin kime erişebileceğini **söylersin** (`whereLayer(...)`). `mayNotBeAccessedByAnyLayer()` "bu en üst katmandır, kimse ona bakamaz" demektir.

Küçük ama kritik bir ayrıntı: `withOptionalLayers(false)`. Bu satır, tanımlanan bir katman **boş kalırsa** testi kırmızıya çevirir. Neden önemli? Biri paketi yeniden adlandırırsa (`repo` → `repository`), katman boşalır; bu satır olmasa kural sessizce **her şeyi geçirir** ve sen hâlâ korunduğunu sanırsın. Sessizce geçen bir kural, olmayan bir kuraldan daha tehlikelidir.

Bu dosyada başka çitler de var: `claude_yalniz_ai_paketinden` (LLM çağrısı yalnız `ai/` paketinden yapılır), `mcp_llmsiz` (MCP katmanı LLM kullanan servislere bağlanamaz, ADR-0009) ve `types_bagimsiz`. Hepsi aynı mantığın farklı kurallara uygulanmış hâlidir.

### Sık yapılan hata
İlk çalıştırmada **kırmızı** almak normaldir; mevcut kodda birikmiş ihlaller vardır. Buradaki tuzak, kuralı ihlallere uydurmaktır: erişimi gevşetmek, katmanı silmek, testi `@Disabled` yapmak. Bu, kuralı yazmamış olmakla aynı kapıya çıkar. Doğru refleks: kuralı **doğru hâliyle bırak**, ihlalleri listele, `docs/tech-debt-tracker.md`'ye kaydet ve planlı biçimde kapat. İkinci tuzak, ArchUnit'i uygulama kapsamına (main scope) eklemektir; ArchUnit yalnız **test** kapsamına girer, ürün koduyla dağıtılmaz.

### Görevlere geçmeden önce
Sırayla git: önce bağımlılığı test kapsamına ekle, sonra kuralı yaz ve çalıştır — ihlaller listelensin. Bugün ihlalleri **düzeltmeyeceksin**, yalnız kaydedeceksin; bunlar sonraki günlerin planlı işidir. Yarın aynı fikrin Python'daki karşılığını (import-linter) göreceksin ve aletin değişse bile ilkenin değişmediğini fark edeceksin.

## Görevler
- [ ] ArchUnit bağımlılığını test scope'a ekle
- [ ] Katman testini yaz ve çalıştır — ihlaller listelensin
- [ ] İhlalleri tech-debt-tracker.md'ye kaydet (hemen düzeltme)
