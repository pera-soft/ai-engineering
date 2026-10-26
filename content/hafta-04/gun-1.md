---
hafta: 4
gun: 1
baslik: "Neden 'belgelemek yetmez, zorlamak gerekir'"
---

## Ders

### Bugün ne öğreneceksin?
Hafta 2'de kuralları yazdın. Bugün öğreneceğin şey şu: **yazılı kural, uygulanan kural değildir.** Bir kuralı belgede bırakmakla onu bir teste bağlamak arasındaki farkı ve bu farkın ajanlarla çalışırken neden katlanarak büyüdüğünü göreceksin. Bu haftanın geri kalanı, bu farkı kapatma pratiğidir.

### Temel kavramlar
- **Guardrail (koruma bandı):** yanlış yapmayı **mümkün olmaktan çıkaran** mekanizma. Uyarı değil, engel.
- **Belge kuralı:** `AGENTS.md` gibi bir dosyada yazan kural. Denetleyicisi insandır.
- **Mekanik kural:** bir test ya da linter (kod denetleyici program) hâline getirilmiş kural. Denetleyicisi makinedir.
- **Drift (sapma):** kod tabanının, yazılı kurallardan zamanla ve sessizce uzaklaşması.
- **Linter:** kodu çalıştırmadan tarayıp kural ihlallerini bildiren program.
- **Sensor:** Hafta 1'den — iş yapıldıktan **sonra** ölçen kontrol (test, mimari test, güvenlik taraması). Bu hafta sensörleri zorlayıcı hâle getiriyoruz.

Benzetme: parktaki **"Lütfen çimlere basmayınız"** tabelası. Herkes okur; yine de kestirmeyi kullanan biri çıkar, arkasından ikinci, üçüncü... ve bir süre sonra çimde **patika** oluşur. Yeni gelen kimse tabelaya bakmaz — **patikaya** bakar ve "demek ki buradan geçiliyor" der. Tabelanın yerine **çit** koysaydın patika hiç oluşmayacaktı.

Ajanlar tam olarak böyle davranır: **tabelayı değil, patikayı taklit ederler.** Bağlamlarındaki mevcut kod onlar için "burada iş böyle yapılıyor" demektir. Bir kez ihlal edilmiş kural artık bir **örnektir**; sonraki ajan onu çoğaltır. Drift budur ve sessizce büyür.

### Somut örnek
Aynı kuralın bu projedeki iki hâlini yan yana koy:

- **Tabela hâli (Hafta 2'de yazdın):** `AGENTS.md` → *"ALWAYS: types → config → repo → service → ai → web katman yönüne uy."* Ajan bu dosyayı okumadıysa ya da uzun bir görevin ortasında unuttuysa, `web/ProgramController` içine doğrudan bir `repo/IlerlemeRepo` çağrısı **girer ve geçer**. Sen kod incelemesinde yakalayana kadar orada durur — bu arada başka ajanlar o satırı **örnek alır**.
- **Çit hâli (bu hafta kuruyorsun):** `src/test/java/tr/velzon/aiogretmen/MimariTestleri.java` içindeki `katman_kurali`. Aynı ihlal artık `./mvnw verify` sırasında **kırmızı yanar**: *"Layer 'Repo' may only be accessed by ['Service', 'Ai']."* Kod merge edilemez; patika **hiç oluşmaz**.

Kural değişmedi — **denetleyici** değişti. İnsan denetleyici ölçeklenmez: 10 PR'a bakarsın, 100 PR'a bakamazsın; yorulursun, acele edersin, gözden kaçırırsın. Makine denetleyici bedavaya ölçeklenir ve **her satıra, her seferinde** aynı titizlikle bakar.

"Bu kadar katı olmak gerekli mi?" İnsan-öncelikli dünyada bu **pedantiklik** (aşırı titizlik) gibi görünür. Beş kişilik bir ekipte "biliyoruz zaten, dikkat ederiz" işe yarar. Ama ajan günde 50 dosyaya dokunuyorsa "dikkat ederiz" 50 kez sınanır; bir kez bile kırılırsa drift başlar. Mekanik kural **çarpan** etkisi yapar: bir kez yazarsın, sonsuz kere uygulanır.

### Sık yapılan hata
En sık hata, ihlali gördüğünde **kuralı gevşetmektir**. Ajan (veya yorulmuş bir geliştirici) testi kırmızı bulunca en kolay yolu seçer: testi devre dışı bırakır, katman tanımını genişletir, istisna ekler. Bu, çiti kesip patikayı resmîleştirmektir; birkaç ay sonra kuralın hiç yokmuş gibi olmasının nedeni budur. İkinci hata, kuralı yazıp **hiç ihlal ettirmeden test etmemektir**: sessizce hep yeşil yanan bir kural, aslında çalışmıyor da olabilir — kuralı bilerek bir kez kırıp kırmızı yandığını görmeden ona güvenme.

### Görevlere geçmeden önce
Bugünün pratik ölçütü tek soruda toplanır: *"Bu kural çiğnenirse, çiğnendiğini bana **kim** söyleyecek?"* Cevap "ben fark ederim" ise, o kural aslında **yoktur**. Cevap çalıştırılabilir bir komutsa (`./mvnw verify`), kural gerçekten vardır. Şimdi projendeki yazılı ama zorlanmayan kuralları çıkar ve her birinin karşısına hangi araçla çite dönüşeceğini yaz — yarın ArchUnit ile ilkini kuracaksın.

**Derinleşmek istersen:** "Bu haftanın kaynakları" kutusundaki **Ajan Çalışma-Zamanı Kontrolleri** metninin §9 "Bu depodan vaka — derleme-zamanı kapı vs çalışma-zamanı kapı" bölümü, bugünkü "tabela değil çit" fikrinin öbür yarısını gösterir: `MimariTestleri` gibi bir çit **üretilen kodu** `./mvnw verify` anında yargılar, ama ajanın çalışırken yaptığı eylemleri (bir dosyayı silmesini, para yakan bir döngüyü, bir sırrı dışarı göndermesini) görmez. O davranışları yargılayan çite çalışma-zamanı kapısı (runtime gate) denir; ikisi birbirinin yerine geçmez, birbirini tamamlar. Bugünkü görev için şart değil; "bu kural çiğnenirse bana kim söyleyecek?" sorusunu ajanın eylemlerine de sormak istersen oku.

## Görevler
- [ ] Projendeki 'yazılı ama zorlanmayan' 3 kuralı tespit et
- [ ] Her biri için hangi araçla mekanikleştirilebileceğini not et
