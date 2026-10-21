---
hafta: 6
gun: 1
baslik: "Stil kataloğu: monolitler"
---

## Ders

### Bugün ne öğreneceksin?
Bugün "mimari stil" diye bir şeyin var olduğunu, tek parça (monolit) stillerin neler olduğunu ve bir stili **puan kartıyla** değerlendirmeyi öğreneceksin. Gün sonunda kendi sisteminin hangi stile ait olduğunu adlandırabilmelisin.

### Temel kavramlar
- **Mimari stil (architecture style):** Bir sistemin parçalarının nasıl bölündüğünü ve nasıl konuştuğunu belirleyen **genel şablon**. Ev planı benzetmesi: müstakil ev, apartman dairesi, rezidans. Üçü de "ev"dir; ama oda ilişkileri, maliyeti ve komşuluk kuralları farklıdır.
- **Monolit (monolith):** Tüm kodun **tek bir uygulama** olarak derlendiği ve tek parça dağıtıldığı stil ailesi. "Monolit = kötü" değildir; monolit = *tek dağıtım birimi*.
- **Layered (katmanlı):** Kod, sorumluluklara göre üst üste katmanlara ayrılır ve bağımlılık **tek yöne** akar. Bu projenin stili budur.
- **Modüler monolit (modular monolith):** Tek dağıtım birimi, ama içeride sınırları sıkı sıkıya korunan modüller. Mikroservisin faydasının çoğunu, bedelinin küçük bir kısmıyla verir; bu yüzden bugünün en çok önerilen başlangıç noktasıdır.
- **Pipeline (boru hattı):** Veri, birbirine bağlı filtrelerden sırayla geçer. Unix'teki `komut1 | komut2` mantığı. Dönüştürme/veri işleme işlerinde çok iyidir, etkileşimli uygulamalarda kötüdür.
- **Microkernel (çekirdek + eklentiler):** Küçük ve sabit bir çekirdek, üstüne takılan eklentiler. IDE'ler ve tarayıcılar böyle çalışır. Kazandırdığı nitelik: yeni yetenek eklerken çekirdeğe dokunmamak.
- **Puan kartı (yıldız haritası):** Bir stilin hangi nitelikte iyi, hangisinde kötü olduğunu gösteren küçük tablo. Araba benzetmesi: kamyonet yük taşımada ★★★, yakıtta ★'dır. Bu onu "kötü araba" yapmaz; **taşınacak yükün yoksa yanlış araba** yapar.

Yani: **iyi/kötü stil yoktur; senin niteliklerine (Hafta 5, Gün 2) uyan ya da uymayan stil vardır.**

### Somut örnek (bu projeden)
Layered (katmanlı) monolitin puan kartı:

| Karakteristik | Puan | Neden |
|---|---|---|
| **Basitlik** (simplicity) | ★★★ | Tek proje, tek dağıtım. Yeni gelen 1 günde anlar. |
| **Maliyet** (cost) | ★★★ | Tek sunucu yeter; kuyruk, servis keşfi, orkestrasyon gerekmez. |
| **Test edilebilirlik** (testability) | ★★ | Katmanlar ayrıksa iyi; ama her test tüm uygulamayı ayağa kaldırır. |
| **Elasticity** (ani yüke uyum) | ★ | 10x trafikte tek bir modülü ayrı ölçekleyemezsin; tüm uygulamanın kopyasını çıkarırsın. |
| **Dağıtılabilirlik** (deployability) | ★ | Tek satırlık düzeltme bile tüm uygulamanın yeniden dağıtımını gerektirir. |
| **Hata izolasyonu** (fault tolerance) | ★ | Bir katmanda patlayan hata tüm uygulamayı düşürebilir; ayrı süreç yok. |

- **Bu proje tam olarak Layered'dır.** Katman yönü bellidir: `types → config → repo → service → ai → web` — ve Hafta 4'teki ArchUnit testi (`MimariTestleri.katman_kurali`) bunu mekanik olarak zorlar. Yani stil burada bir slogan değil, **kırmızı yanan bir test**tir.
- **Doğru okuma:** Tabloda iki ★★★ ve dört zayıf hane var; bu, seçimin yanlış olduğunu göstermez. Tek kullanıcılı, yerelde (127.0.0.1) çalışan bir eğitim uygulamasında **elasticity'nin değeri yoktur**; basitliğin değeri ise çok yüksektir. ★★★'lar tam ihtiyacın olan hanelerde.
- **Sonuç:** Puan kartının sorusu "kaç yıldız topladın" değildir. Sorusu şudur: **"Zayıf olduğu haneler, benim umursadığım haneler mi?"** Cevap "hayır"sa stil doğrudur.

### Sık yapılan hata
- **Toplam puana bakmak.** Puan kartı yarışma tablosu değil, eşleştirme aracıdır.
- **"Monolit = eski" sanmak.** Monolit bir olgunluk seviyesi değil, bir dağıtım kararıdır; çoğu ürün için doğru başlangıçtır.
- **Katman kuralını belgede bırakmak.** Zorlanmayan katman, birkaç ay içinde `web`'den `repo`'ya atılan tek bir çağrıyla ölür.
- **Stili nitelik konuşmadan seçmek.** Önce hangi nitelik birinci sınıf, sonra stil. Tersi, cevabı bilip soruyu uydurmaktır.

### Görevlere geçmeden önce
İkinci görevde puan kartını yazarken her satıra üç şey koy: nitelik, yıldız, **tek cümlelik gerekçe**. Gerekçesiz yıldız işe yaramaz. Ve son satır olarak şu cümleyi ekle: *"… stilini seçtim; … kazandım, … feda ettim; bu üründe feda ettiğim şeyin değeri düşük."* Bu cümle aynı zamanda yazacağın **ADR'nin (Hafta 2) tam gövdesidir**.

## Görevler
- [ ] Monolitik stiller bölümünü oku
- [ ] Kendi sisteminin hangi stile en yakın olduğunu ve puan kartını yaz
