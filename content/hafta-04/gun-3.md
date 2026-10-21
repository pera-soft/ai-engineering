---
hafta: 4
gun: 3
baslik: "Katman testinin Python karşılığı: import-linter (kavramsal/genişlik)"
---

## Ders

### Bugün ne öğreneceksin?
Bugün bir **genişlik dersi** var: dünkü fikrin (katman kuralını mekanik zorlamak) Java'ya özgü olmadığını göreceksin. Python tarafındaki karşılığı **import-linter**. Bunu öğrenmenin amacı Python yazmak değil; **ilkeyi araçtan ayırmak**. Yarın başka bir dile geçtiğinde arayacağın şeyin adını bileceksin.

### Temel kavramlar
- **import (içe aktarma):** bir Python dosyasının başka bir modülün kodunu kullanmak için yazdığı satır. Bağımlılık burada doğar; kural da burada zorlanır.
- **import-linter:** Python projelerinde modüller arası bağımlılıkları **sözleşmelere** karşı denetleyen araç. ArchUnit'in yaptığını, `import` grafiği üzerinde yapar.
- **Sözleşme (contract):** yazılı kural tanımı. import-linter'ın üç ana türü vardır:
  - **`layers`** — katman sırasını tanımlar; üsttekiler alttakileri kullanabilir, tersi yasaktır. ArchUnit'teki `layeredArchitecture()` karşılığı.
  - **`forbidden`** — "şu modül şuna asla bağımlı olamaz" der. ArchUnit'teki `noClasses().should().dependOnClassesThat()` karşılığı.
  - **`independence`** — birden çok modülün birbirine **hiç** dokunmamasını şart koşar; kardeş modülleri ayrı tutmak için kullanılır.
- **Yapılandırma yeri:** `pyproject.toml` ya da ayrı bir `.importlinter` dosyası.
- **`lint-imports`:** sözleşmeleri denetleyen komut. İhlalde sıfırdan farklı çıkış kodu döner — yani CI'da **bloklayıcı kapı** yapılabilir.

### Somut örnek
Bu projenin katman yönü şudur: `types` → `config` → `repo` → `service` → `ai` → `web`. Aynı kuralı Python'da kursaydın, `pyproject.toml` içine kabaca şunu yazardın:

```toml
[[tool.importlinter.contracts]]
name = "Katman yonu"
type = "layers"
layers = ["ai_ogretmen.web", "ai_ogretmen.ai", "ai_ogretmen.service", "ai_ogretmen.repo", "ai_ogretmen.types"]
```

Listede üstte olan alttakini kullanabilir; alttaki üsttekini **kullanamaz**. `web` içinde `from ai_ogretmen.repo import ...` satırı belirdiği an `lint-imports` kırmızı yanar — tıpkı `./mvnw verify` sırasında ArchUnit'in *"Layer 'Repo' may only be accessed by ['Service', 'Ai']"* demesi gibi.

Eşleştirme tablosu böylece netleşir: `layers` ↔ `layeredArchitecture()`, `forbidden` ↔ `noClasses()...dependOnClassesThat()`, `independence` ↔ dilimlerin birbirine değmemesi. İki araç da aynı üç şeyi yapar: **kuralı koda çevirir**, **CI'da bloklar**, **ihlali isimle raporlar.**

Bir fark not etmeye değer: ArchUnit derlenmiş sınıfları (bytecode) inceler, import-linter kaynak koddaki `import` satırlarını. Pratik sonuç: import-linter yalnız import ile kurulan bağımlılıkları görür; dinamik yükleme ile kaçış mümkündür. Yani araç ne kadar iyi olursa olsun, kaçış yolları hep kalır — bu yüzden mekanik kural, kod incelemesinin yerine değil, **üstüne** kurulur.

### Sık yapılan hata
En sık hata, aracı kurup **bloklayıcı yapmamaktır**. `lint-imports` yerelde çalışır, herkes çıktısını görür, kimse durmaz — böylece araç bir rapor üreticisine dönüşür ve ihlaller birikir. Kuralın gerçek olması için CI'da işi **durdurması** gerekir (Gün 1: "çiğnendiğini bana kim söyleyecek?"). İkinci hata, sözleşmeleri projenin gerçek yapısına bakmadan yazmaktır: mevcut kodla hiç örtüşmeyen bir katman listesi yüzlerce ihlal üretir, ekip de en kolay yolu seçip sözleşmeyi gevşetir — Gün 2'deki tuzağın aynısı.

### Görevlere geçmeden önce
Senin projen Java, dolayısıyla bugün bir şey **kurmak zorunda değilsin**; asıl kazanç eşleştirmeyi görmek. Önce üç sözleşme tipini oku ve her birinin ArchUnit karşılığını yaz. Sonra ikisinin CI'da nasıl bloklayıcı kapıya dönüştüğünü karşılaştır — senin projende ArchUnit bu işi zaten yapıyor. İstersen küçük bir Python örneğinde `lint-imports` çalıştırıp kırmızı yanışını kendi gözünle gör.

## Görevler
- [ ] import-linter'ın layers / forbidden / independence sözleşmelerini incele; ArchUnit karşılıklarını eşleştir
- [ ] ArchUnit ve import-linter'ın CI'da nasıl bloklayıcı kapı olduğunu karşılaştır (senin projende ArchUnit zaten var) — istersen küçük bir Python örneğinde dene
