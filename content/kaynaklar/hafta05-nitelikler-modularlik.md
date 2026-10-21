---
baslik: "Hafta 5 Görsel Özet: Mimari Nitelikler, Modülerlik ve Trade-off"
yazar: "FoSA 2e (Richards & Ford) — kavram özeti"
tarih: "2026-07-11"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 5
---

### Bu metin ne anlatıyor?
Bir programın "çalışması" tek başına yetmez. Hızlı mı, güvenli mi, yarın değiştirmesi kolay
mı — bunlar da tasarımın parçasıdır. Bu metin dört şeyi anlatıyor: her tasarım kararının bir
bedeli olduğunu, "iyi sistem" ölçütlerinin birbiriyle yarıştığını, kodu düzenli tutmanın
ölçülebilir bir yolu olduğunu ve bu düzenin otomatik testlerle korunabildiğini. Geçen her
teknik terim ilk kullanıldığı yerde Türkçe tanımıyla ve parantez içinde İngilizcesiyle
veriliyor; önceden bilgi gerekmiyor.

### Bu metinde geçen temel terimler
- **Mimari (architecture):** Bir yazılımın büyük parçalarının ne olduğu ve birbirine nasıl
  bağlandığı. Binanın kat planı gibi; tek tek tuğlalar (kod satırları) değil, genel düzen.
- **Trade-off (ödünleşim):** Birini kazanmak için öbüründen vazgeçme. Ucuz–hızlı–kaliteli
  üçlüsünden aynı anda üçünü birden alamamak gibi.
- **Modül (module):** Bir işi birlikte yapan, tek bir isimle anılan kod topluluğu (bir sınıf,
  bir paket, bir servis).

## Neden bu özet
Hafta 5, mimari düşünmenin temel refleksini kuruyor: "en iyi" mimari yoktur, yalnızca
verili bağlamda "en az kötü" olan vardır. Bu özet dört fikri bir çırpıda bakılacak
referans niyetiyle topluyor: iki yasa, mimari karakteristikler (nitelikler / -ility'ler),
modülerlik (cohesion · coupling · connascence) ve uygunluk fonksiyonu (fitness function).

## 1. İki yasa
FoSA 2e'nin başlangıç noktası iki cümleye sığar. Birincisi: **"Yazılım mimarisindeki her
şey bir trade-off'tur."** Mimarın işi, mükemmel kararı bulmak değil, verili bağlamda en az
kötü olan uzlaşmayı seçmektir. İkincisi: **"Neden, nasıldan önemlidir."** İki mimar aynı
çözüme (nasıl) varabilir ama biri gerekçesini (neden) bilir. İşte bu yüzden kararları
Mimari Karar Kaydı (ADR — Architecture Decision Record: bir kararı, o günkü koşullarını ve
gerekçesini anlatan bir–iki sayfalık kısa belge) ile yazıya döker: gelecekteki okuyucu
"nasıl"ı koda bakıp görür, "neden"i ise ancak kayıttan öğrenir.

## 2. Mimari karakteristikler (nitelikler / -ility'ler)
Bir sistemin **işlevsel gereksinim** (functional requirement — sistemin *ne yapacağı*, ör.
"kullanıcı sipariş verebilsin") dışındaki, "*ne kadar iyi* yapacağı" boyutlarıdır. Türkçede
kısaca **nitelik** deniyor; İngilizcede çoğu *-ility* ekiyle bittiği için "-ility'ler" diye
anılırlar. Üç öbekte toplanır:

- **Operasyonel (çalışma zamanı):** ölçeklenebilirlik (scalability — artan yükü kaldırma),
  elastikiyet (elasticity), erişilebilirlik (availability — ayakta kalma süresi).
- **Yapısal (kod / iç düzen):** sürdürülebilirlik (maintainability — değiştirme kolaylığı),
  dağıtılabilirlik (deployability — sık ve düşük riskli dağıtım).
- **Çapraz kesen (cross-cutting):** güvenlik (security) gibi tek öbeğe sığmayan, sistemin
  her yerine dokunan nitelikler.

**Kritik ayrım — scalability ≠ elasticity:** Ölçeklenebilirlik, yükün *zamanla, kademeli*
artışını (ör. kullanıcı sayısı aylar içinde 10 kat) kaldırabilmektir. Elastikiyet ise
yükün *ani sıçramasına* (ör. saniyeler içinde bilet satışı patlaması) hızla uyum
sağlayabilmektir. Biri düzgün rampa, diğeri ani maça hazırlıktır.

**Altın kural:** Hepsini birden optimize edemezsin. Nitelikler birbiriyle yarışır — biri
artınca çoğu zaman diğeri düşer (ör. yüksek güvenlik, başarımı ve kolaylığı geriletir).
Bu yüzden mimar, sistemin başarısı için gerçekten kritik olan **en fazla birkaç** karakteristik
seçer ("architecture characteristics" seçimi) ve gerisini bilinçli olarak feda eder.

Aşağıdaki görsel, tek bir sistem hedefinin etrafındaki nitelikleri ve aralarındaki gerilimi
özetliyor: dıştaki oklar "birini çekersen öbürü sarkar" trade-off'unu anlatır.

```svg
<svg viewBox="0 0 640 320" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Mimari karakteristik seçimi: ortadaki sistem hedefi etrafında ölçeklenebilirlik, elastikiyet, sürdürülebilirlik ve güvenlik nitelikleri; hepsini değil birkaçını seç">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Her şey bir trade-off — hepsini değil, birkaçını seç</text>
  <rect x="240" y="128" width="160" height="64" rx="14" fill="#EEF0FF" stroke="#4F46E5" stroke-width="2"/>
  <text x="320" y="156" text-anchor="middle" font-size="13" font-weight="700" fill="#4F46E5" font-family="system-ui">Sistem hedefi</text>
  <text x="320" y="174" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bağlama göre öncelik</text>
  <rect x="40" y="58" width="180" height="56" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="130" y="82" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Ölçeklenebilirlik</text>
  <text x="130" y="100" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(scalability) · artan yük</text>
  <rect x="420" y="58" width="180" height="56" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="510" y="82" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Elastikiyet</text>
  <text x="510" y="100" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(elasticity) · ani sıçrama</text>
  <rect x="40" y="206" width="180" height="56" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="130" y="230" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Sürdürülebilirlik</text>
  <text x="130" y="248" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(maintainability)</text>
  <rect x="420" y="206" width="180" height="56" rx="12" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="510" y="230" text-anchor="middle" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">Güvenlik</text>
  <text x="510" y="248" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(security) · çapraz kesen</text>
  <line x1="222" y1="94" x2="248" y2="132" stroke="#5D7789" stroke-width="1.6"/>
  <line x1="418" y1="94" x2="392" y2="132" stroke="#5D7789" stroke-width="1.6"/>
  <line x1="222" y1="226" x2="248" y2="188" stroke="#5D7789" stroke-width="1.6"/>
  <line x1="418" y1="226" x2="392" y2="188" stroke="#5D7789" stroke-width="1.6"/>
  <text x="320" y="292" text-anchor="middle" font-size="11" font-weight="600" fill="#1C3247" font-family="system-ui">Birini yukarı çekersen çoğu zaman öbürü sarkar — bilinçli olarak feda et.</text>
  <text x="320" y="310" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Operasyonel (üst) · Yapısal (sol-alt) · Çapraz kesen (sağ-alt)</text>
</svg>
```

## 3. Modülerlik: cohesion, coupling ve connascence
İyi bir yapının iki ölçütü vardır: **yüksek cohesion (bağlılık)** — bir modülün içindeki
parçalar aynı işe hizmet eder, dağınık değildir; ve **düşük coupling (bağlaşım)** — modüller
birbirine az bağlıdır, biri değişince öbürü kolayca kırılmaz. Hedef ikisini birlikte tutmaktır.

"Coupling" soyut kalabilir; **connascence (birlikte-değişim)** onu ölçülebilir kılar. İki
kod parçası, birinin değişmesi ötekinin de değişmesini gerektiriyorsa "connascent"tir. İki
eksende sınıflanır:

- **Statik türler (koda bakıp görülür, daha zayıf):** Ad (Name), Tür/Tip (Type), Anlam/Uzlaşı
  (Meaning), Konum/Sıra (Position), Algoritma (Algorithm).
- **Dinamik türler (çalışma zamanında ortaya çıkar, daha güçlü/tehlikeli):** Yürütme sırası
  (Execution), Zamanlama (Timing), Değer (Value), Kimlik (Identity).

Somut örnek: bir fonksiyonu `hesapla(3, "TL", true)` diye çağırıyorsan, argümanların **sırasını**
bilmek zorundasın — sıra değişirse çağıran her yer bozulur (konum connascence'ı). Aynı çağrıyı
`hesapla(tutar: 3, para: "TL", vergiDahil: true)` biçimine çevirirsen sıra artık önemli değildir;
bağımlılık isme taşınmıştır ve isim değişikliği derleyicinin/derleme aşamasının gözünden kaçmaz.

**Kural (iki adım):** connascence'ı önce **zayıflat** (güçlü/dinamik türden zayıf/statik türe
taşı, ör. yukarıdaki gibi konum bağımlılığını isimli parametreye çevir), sonra **yerelleştir**
(kalan bağımlılığı aynı modül içine hapset ki modüller arası değil, modül içi kalsın).

Bir üçüncü boyut daha vardır: **derece (degree)** — aynı bağımlılığın *kaç* yeri etkilediği.
Üç yeri etkileyen bir bağımlılık, üç yüz yeri etkileyenle aynı şey değildir. Pratikte sıra
şudur: dereceyi küçült, türü zayıflat, kalanı yerelleştir.

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Solda yüksek cohesion ve düşük coupling iyi kuralı; sağda connascence spektrumu dinamik güçlüden statik zayıfa doğru, kural sağa taşı yani zayıflat">
  <defs>
    <marker id="h5ok2" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Modülerlik: iyi yapı + connascence spektrumu</text>
  <rect x="26" y="48" width="220" height="118" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="136" y="74" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">İyi yapının hedefi</text>
  <text x="136" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Yüksek cohesion (bağlılık)</text>
  <text x="136" y="114" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">içindeki parçalar aynı işe hizmet</text>
  <text x="136" y="136" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Düşük coupling (bağlaşım)</text>
  <text x="136" y="152" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">modüller birbirine az bağlı</text>
  <text x="440" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C3247" font-family="system-ui">Connascence spektrumu</text>
  <rect x="300" y="80" width="132" height="72" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="366" y="102" text-anchor="middle" font-size="11.5" font-weight="700" fill="#C77A17" font-family="system-ui">Dinamik (güçlü)</text>
  <text x="366" y="120" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Yürütme · Zamanlama</text>
  <text x="366" y="136" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Değer · Kimlik</text>
  <text x="366" y="150" text-anchor="middle" font-size="9" fill="#C77A17" font-family="system-ui">çalışma zamanında</text>
  <rect x="470" y="80" width="144" height="72" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="542" y="102" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Statik (zayıf)</text>
  <text x="542" y="120" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Ad · Tip · Anlam</text>
  <text x="542" y="136" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Konum · Algoritma</text>
  <text x="542" y="150" text-anchor="middle" font-size="9" fill="#186399" font-family="system-ui">koda bakıp görülür</text>
  <line x1="300" y1="176" x2="614" y2="176" stroke="#186399" stroke-width="1.8" marker-end="url(#h5ok2)"/>
  <text x="366" y="194" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">güçlü / tehlikeli</text>
  <text x="560" y="194" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">zayıf / güvenli</text>
  <text x="440" y="222" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kural: sağa taşı = ZAYIFLAT, sonra YERELLEŞTİR</text>
  <text x="136" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">connascence, coupling'i</text>
  <text x="136" y="212" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ölçülebilir kılar</text>
  <text x="320" y="248" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Zayıflat: güçlü/dinamik türü zayıf/statik türe çevir (ör. konum → isimli parametre).</text>
  <text x="320" y="266" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Yerelleştir: kalan bağımlılığı aynı modülün içine hapset (modül içi &lt; modüller arası).</text>
</svg>
```

## 4. Uygunluk fonksiyonu (fitness function)
Bir mimari karakteristiği **objektif olarak ölçen** (yani kişisel görüşe değil, ölçüme dayanan)
herhangi bir kontroldür. Amaç, "sistem sürdürülebilir mi?" gibi öznel bir yargıyı otomatik bir
sınava dönüştürmektir: geçerse karakteristik korunuyor, kalırsa **mimari erozyon** (architectural
erosion — tasarımın zamanla, küçük küçük ödünlerle bozulması) başlamış demektir. Çoğu otomatiktir
(bir test gibi), ama bazıları manuel olabilir (ör. gözden geçirme adımı).

Bu projedeki **ArchUnit** bir örnektir. ArchUnit, kodun yapısı hakkında kural yazmayı sağlayan
bir Java test kütüphanesidir: "katman yönü (types → config → repo → service → ai → web)
bozulmasın" cümlesini çalışan bir teste çevirir. Bu test **CI kapısı** olarak koşar —
CI (continuous integration / sürekli entegrasyon), her kod gönderiminde testleri otomatik
çalıştıran sunucu düzenidir; "kapı" ise testler geçmeden değişikliğin ana koda katılamaması
demektir. Kural bozulursa yapı kırmızı yanar ve sapma daha birleşmeden yakalanır.

**Şerh (abartıya karşı):** Uygunluk fonksiyonu sihirli bir kalite ölçeri değildir. Yalnızca
*senin yazdığın* kuralı ölçer; yazmadığın hiçbir şeyi korumaz. Tüm testlerin yeşil olması
"mimari iyidir" demek değil, "kodladığım kurallar henüz bozulmadı" demektir. Ayrıca yanlış
kurulmuş bir kural gereksiz alarm üretip ekibi kuralı devre dışı bırakmaya iter — bu da
korumanın kendisini yok eder.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Uygunluk fonksiyonu döngüsü: mimari karakteristik, onu ölçen test ArchUnit, geç veya kal CI kapısı, sapma yakalanınca geri besleme">
  <defs>
    <marker id="h5ok3" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#4F46E5"/></marker>
    <marker id="h5ok4" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#C77A17"/></marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Uygunluk fonksiyonu döngüsü</text>
  <rect x="24" y="70" width="150" height="72" rx="12" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="99" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">Mimari</text>
  <text x="99" y="115" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">karakteristik</text>
  <text x="99" y="132" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ör. katman yönü</text>
  <rect x="246" y="70" width="150" height="72" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="321" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Ölçen test</text>
  <text x="321" y="115" text-anchor="middle" font-size="11" fill="#186399" font-family="system-ui">(ArchUnit)</text>
  <text x="321" y="132" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">objektif kontrol</text>
  <rect x="468" y="70" width="150" height="72" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="543" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">CI kapısı</text>
  <text x="543" y="115" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">geç / kal</text>
  <text x="543" y="132" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yeşil / kırmızı</text>
  <line x1="176" y1="106" x2="242" y2="106" stroke="#4F46E5" stroke-width="1.8" marker-end="url(#h5ok3)"/>
  <line x1="398" y1="106" x2="464" y2="106" stroke="#4F46E5" stroke-width="1.8" marker-end="url(#h5ok3)"/>
  <path d="M543,146 L543,196 L99,196 L99,146" fill="none" stroke="#C77A17" stroke-width="1.6" marker-end="url(#h5ok4)"/>
  <text x="321" y="214" text-anchor="middle" font-size="11" font-weight="600" fill="#C77A17" font-family="system-ui">Geri besleme: sapma yakalanır → karakteristik yeniden korunur</text>
  <text x="321" y="234" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Çoğu otomatik (test gibi), bazıları manuel olabilir · erozyonu birleşmeden önce durdurur</text>
</svg>
```

## Öz-sınav (kendini test et)
1. "Mimaride her şey bir trade-off'tur" ve "neden nasıldan önemlidir" yasaları, ADR tutma
   alışkanlığını nasıl gerekçelendiriyor?
2. Ölçeklenebilirlik (scalability) ile elastikiyet (elasticity) arasındaki fark nedir? Her
   birine gündelik bir örnek ver.
3. Bir mimar neden tüm karakteristikleri aynı anda optimize edemez? "En fazla birkaçını seç"
   kuralının arkasındaki mantık nedir?
4. Yüksek cohesion + düşük coupling neden hedeftir, ve connascence bu hedefi ölçmeye nasıl
   yardım eder? Dinamik türler neden statik türlerden daha tehlikelidir?
5. Bir uygunluk fonksiyonu (fitness function) tam olarak neyi otomatikleştirir? ArchUnit'in
   CI kapısı olarak çalışması, mimari erozyonu nasıl önler?
6. Bütün uygunluk fonksiyonları yeşil yanıyor. Buradan "mimarimiz iyi" sonucunu çıkarabilir
   misin? Çıkaramıyorsan, yeşil ışık tam olarak neyi kanıtlar?
7. `hesapla(3, "TL", true)` çağrısını `hesapla(tutar: 3, para: "TL", vergiDahil: true)`
   biçimine çevirmek hangi connascence türünü hangisine dönüştürür — ve bu neden ilerlemedir?
