---
hafta: 5
gun: 3
baslik: "Modülerlik: cohesion, coupling, connascence"
---

## Ders

### Bugün ne öğreneceksin?
Bugün "bu kodu değiştirmek korkutucu" duygusunun teknik adını koyacaksın. İki parçanın **neden** birbirine bağlı olduğunu adlandırmayı ve o bağı **zayıflatmayı** öğreneceksin. Hedef, dokuz terim ezberlemek değil; bir bağa bakıp "bu ucuz mu, pahalı mı?" diyebilmek.

### Temel kavramlar
- **Modül (module):** Birlikte duran kod parçası — bir sınıf, bir paket, bir servis. Bu projede `service`, `repo`, `ai` birer modüldür.
- **Cohesion (bağlılık, iç tutarlılık):** Bir modülün parçalarının **aynı işe** hizmet etmesi. Yüksek cohesion iyidir. Benzetmesi: mutfak çekmecesinde yalnız çatal-bıçak olması.
- **Coupling (bağlaşım):** İki modülün birbirine bağlılığı. Düşük coupling iyidir; çünkü bağlıysan, ötekini değiştirmeden kendini değiştiremezsin.
- **Connascence (birlikte-değişim):** Latince *con* ("birlikte") + *nasci* ("doğmak"). Benzetmesi: **üç ayak yarışı** — iki kişinin birer bacağı bağlıdır; biri adımını değiştirirse öteki de **aynı anda** değiştirmek zorundadır, yoksa ikisi de yere kapaklanır. Kodda karşılığı: *iki parçadan birini değiştirdiğinde ötekini de değiştirmek zorundaysan, aralarında connascence vardır.*

> **Coupling ile farkı:** coupling sadece "bağlılar mı?" der (var/yok). Connascence "**nasıl** bağlılar ve bu bağ **ne kadar tehlikeli**?" der. Yani connascence, coupling'in ölçü birimidir.

Bir bağı değerlendirirken sorman gereken **üç soru**:
1. **Güç (strength) — bağı kırmak ne kadar zor?** Ad (Name) bağını kırmak kolaydır (yeniden adlandır, derleyici bulur). Yürütme sırası (Execution) bağını kırmak zordur (kimse söylemez, canlıda patlar).
2. **Derece (degree) — kaç yeri etkiliyor?** 2 sınıfı mı, 40 sınıfı mı?
3. **Yerellik (locality) — taraflar birbirine ne kadar yakın?** Aynı sınıf içindeki güçlü bir bağ **sorun değildir**; farklı paketler arasındaki *aynı* bağ **felakettir**.

Page-Jones'un dokuz türü ikiye ayrılır — ezberlemen gereken tek şey bu çizgidir:
- **Statik** (kodda görülür): Ad (Name), Tip (Type), Anlam/Uzlaşı (Meaning), Konum (Position), Algoritma (Algorithm). Derleyici, IDE ve ArchUnit bunları **görür**; bozarsan derleme/test kırmızı yanar → *ucuz hata.*
- **Dinamik** (yalnız çalışma anında ortaya çıkar): Yürütme sırası (Execution), Zamanlama (Timing), Değer (Value), Kimlik (Identity). Derleyici susar, IDE susar; hata **canlıda, günler sonra** patlar → *pahalı hata.*

Kural hep aynı: **bağı zayıflat ve yerelleştir; dinamik bağı statik bağa çevir.**

### Somut örnek
**1) Konum (Position) bağını Ad (Name) bağına taşımak — statik ama gereksiz güçlü bir bağ:**

```java
// ÖNCE — anlam SIRAYA gömülmüş
public int kampanyaHesapla(int tutar, int indirimYuzdesi, int taksit) { ... }
kampanyaHesapla(1000, 20, 3);   // "20" ne? indirim mi, taksit mi?
```
Yarın imzada `indirimYuzdesi` ile `taksit` yer değiştirirse çağıran taraf **derlenmeye devam eder** (üçü de `int`) ama **sessizce yanlış hesaplar**. Çözüm, alanları adla taşıyan bir kurucu (builder) kullanmaktır:

```java
kampanyaHesapla(new KampanyaIstegi.Kurucu()
        .indirimYuzdesi(20)   // sırayı bilerek karıştırdım…
        .tutar(1000)
        .taksit(3)
        .yap());              // …sonuç DEĞİŞMEZ. Sıra artık bir bağ değil.
```
Bağ **yok olmadı** — iki taraf hâlâ `indirimYuzdesi` adında uzlaşıyor. Ama artık adı değiştirirsen IDE'nin "rename" işlemi **her çağıran yeri** sana gösterir. *(Java'da isimli parametre yoktur; en yakın karşılık bu kurucu desenidir.)*

**2) Yürütme sırası (Execution) bağı — dinamik, yani sessiz:**

```java
// ÖNCE — iki çağrının SIRASI gizli bir sözleşme
ogretmenAi.baglamYukle(ogrenciId);
String cevap = ogretmenAi.soruSor("Connascence nedir?");

// SONRA — sırayı İMKÂNSIZ hâle getir: bağlamı argüman yap
String cevap = ogretmenAi.soruSor(baglam, "Connascence nedir?");
```
Önceki hâlde biri satırların yerini değiştirirse hiçbir şey kırmızı yanmaz; model öğrenciyi tanımadan cevap verir ve hatayı haftalar sonra "AI saçmalıyor" şikâyetiyle bulursun. Sonraki hâlde bağlamı unutmak bir **derleme hatası**dır. Yaptığın şeyin adı: **gizli sözleşmeyi imzaya taşımak.**

### Sık yapılan hata
- **Bağı yok etmeye çalışmak.** Amaç bağı silmek değil; **görünür ve ucuz** hâle getirmektir. Sıfır bağlı modüller birbiriyle konuşamaz.
- **Dokuz türü ezberleyip üç soruyu sormamak.** Türün adı önemli değil; güç/derece/yerellik önemli.
- **Uzak bağı normal saymak.** Aynı sınıf içindeki güçlü bağ zararsızdır; paketler arası aynı bağ ekip kilitler.

### Görevlere geçmeden önce
Bu kavramlar Hafta 4'teki ArchUnit testlerinin "neden"idir: `MimariTestleri` aslında paketler arası bağların **yönünü** sabitler. Düşük coupling ayrıca ajanların en verimli çalıştığı öngörülebilir yapıyı verir (Hafta 1). İkinci görevde bir örnek bulurken şu soruyu sor: *"Bu iki dosyadan birini değiştirdiğimde ötekini de açmak zorunda kalıyor muyum, ve bunu bana kim söylüyor — derleyici mi, canlı ortam mı?"*

## Görevler
- [ ] Türkçe çalışma metnini oku ("Bu haftanın kaynakları" kutusundan)
- [ ] Kod tabanında 1 yüksek-coupling örneği bul; connascence türünü adlandır (yukarıdaki listeden) ve statik mi dinamik mi olduğunu söyle
