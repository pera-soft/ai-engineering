---
slug: dagitik-kilit-fencing
baslik: "Dağıtık Kilit ve Fencing Token: Redlock Tartışması ve Correctness"
kademe: Uzman
sira: 129
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

**Kilit (lock)**, aynı anda yalnız bir kişinin/işin bir kaynağa dokunmasını sağlayan mekanizmadır. Günlük benzetme: tek kabinli bir soyunma odası. Kapıda tek anahtar vardır; anahtar sende olmadan giremezsin. Buna **karşılıklı dışlama (mutual exclusion)** denir.

Tek bir programın içinde bu iş kolaydır: Java'da `synchronized` ya da `Lock` kullanırsın. Sebebi basit — tüm **iş parçacıkları (thread)** aynı bellek üzerindedir. Tek bir veritabanına dokunan işlemlerde de dağıtık kilide gerek yoktur. Veritabanının kendi satır kilitleri ve **işlemi (transaction)** zaten bu işi görür. Ama servis yatay ölçeklenip **beş kopya** halinde beş ayrı makinede çalışıyorsa, her kopyanın kendi belleği vardır ve birbirlerinin kilidini görmezler. O zaman kilidin makinelerin **dışında**, ortak bir yerde durması gerekir: **dağıtık kilit**. Kanonik örnek, zamanlanmış bir görevin (cron) beş kopyada değil yalnız birinde çalışmasıdır.

### Temel kavramlar

- **Kiralama (lease):** dağıtık kilit süresizce verilmez, çünkü kilidi alan makine ölürse kilit sonsuza kadar takılı kalır. Bu yüzden kilit bir süre için verilir; süre dolunca kendiliğinden serbest kalır.
- **Çöp toplama duraklaması (GC pause):** Java'nın çöp toplayıcısının programı saniyelerce **durdurması**. Program durduğunu fark etmez; uyanınca hiçbir şey olmamış gibi devam eder.
- **Saat kayması (clock drift):** makinelerin saatleri birbirinden sapar, NTP düzeltmesi saati ileri/geri sıçratabilir. Yani "süre doldu mu" sorusunun cevabı her makinede aynı olmayabilir.
- **Kilit jetonu (fencing token):** kilit her verildiğinde üretilen, **monoton artan** (hep büyüyen) bir sayı. Kimin daha yeni kilit aldığını söyler.

### Nasıl çalışır?

Buradaki en derin tartışma **Kleppmann'ın Redis Redlock eleştirisidir**. Redlock, süre ve zamanlama varsayımlarına dayanır. Gerçek dünyada olan şudur: bir istemci kilidi alır, sonra uzun bir çöp toplama duraklamasına girer. (Aynı sonucu saatin kayması ya da ağın gecikmesi de doğurur.) Bu sırada kiralama süresi dolar ve kilit başka bir istemciye verilir. İlk istemci uyanır, durduğunu fark etmez ve **hâlâ kilit bende** varsayımıyla yazar. Sonuç: iki eşzamanlı yazıcı ve bozulmuş veri. Kilit sunucusu bunu göremez, çünkü korunan kaynağa yapılan yazımın arasına girmez. Yani **zamana dayalı bir kilit tek başına doğruluk (correctness) garanti edemez**.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="GC pause ile stale kilit ve fencing token reddi">
  <defs>
    <marker id="dlock-ar" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Fencing token stale yazımı reddeder</text>
  <text x="120" y="52" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İstemci 1</text>
  <text x="120" y="70" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kilit + token=33</text>
  <rect x="60" y="80" width="120" height="30" rx="6" fill="#FBECEA" stroke="#B0453B" stroke-width="1.3"/>
  <text x="120" y="100" text-anchor="middle" font-size="10" fill="#B0453B" font-family="system-ui">uzun GC pause…</text>
  <text x="120" y="130" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">uyanır, yazmaya çalışır</text>
  <text x="360" y="52" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">İstemci 2</text>
  <text x="360" y="70" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">lease dolunca kilit + token=34</text>
  <rect x="300" y="80" width="120" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="360" y="100" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">yazar: token=34 ✓</text>
  <rect x="470" y="70" width="150" height="120" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="545" y="96" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Korunan Kaynak</text>
  <text x="545" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">gördüğü max token = 34</text>
  <text x="545" y="148" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">token=34 → kabul</text>
  <text x="545" y="170" text-anchor="middle" font-size="10.5" fill="#B0453B" font-family="system-ui">token=33 → RED</text>
  <line x1="180" y1="128" x2="468" y2="150" stroke="#B0453B" stroke-width="1.6" stroke-dasharray="5 3" marker-end="url(#dlock-ar)"/>
  <line x1="420" y1="98" x2="468" y2="128" stroke="#25804C" stroke-width="1.6" marker-end="url(#dlock-ar)"/>
  <text x="300" y="220" text-anchor="middle" font-size="10.5" fill="#B0453B" font-family="system-ui">geç kalmış token=33 yazımı reddedilir — çakışma önlenir</text>
</svg>
```

Çözüm **artan sıra numaralı kilit jetonudur (fencing token)**: kilit her verildiğinde monoton artan bir belirteç üretilir (33, 34, 35…). İstemci **korunan kaynağa** her yazımda bu belirteci ekler. Kaynak da o ana dek gördüğü **en yüksek** belirteci hatırlar ve daha küçük belirteç taşıyan yazımı **reddeder**. Uykuya dalıp geç uyanan istemci 33 numaralı jetonla yazmaya kalkarsa, kaynak artık 34'ü gördüğü için onu reddeder.

Kritik nokta, belirtecin **monoton artan** olmasıdır. Rastgele bir UUID sıralanamaz; bu yüzden eski/yeni ayrımını yapamaz. Sabit bir istemci kimliği ise yalnız "kim" sorusunu yanıtlar, "hangisi daha yeni" sorusunu değil. Fencing'i, **kiralama süresini uzatmakla (lease renewal)** de karıştırma; o bambaşka bir mekanizmadır.

Bu yüzden kilidin iki farklı amacını ayırmak gerekir:

- **Verimlilik (efficiency):** aynı pahalı işi (bir raporu yeniden üretmek, bir e-postayı yeniden göndermek) gereksiz yere iki kez yapmayı **çoğunlukla** önlemek. Nadir bir çift çalıştırma yalnızca biraz israftır, felaket değildir. Burada Redis kilidi (`SET NX PX`) **yeterlidir**.
- **Doğruluk (correctness):** iki eşzamanlı aktörün veriyi **asla** bozmaması gereken durum (para transferi, benzersizlik garantisi). Burada zamana dayalı kilit tek başına **yetmez**; korunan kaynağın **fencing token** ile geç yazımları reddetmesi şarttır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Redis kilidi efficiency için yeter, correctness için fencing gerekir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kilidin iki amacı ayrı ele alınır</text>
  <rect x="30" y="46" width="270" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Efficiency (verimlilik)</text>
  <text x="50" y="100" font-size="10.5" fill="#1C3247" font-family="system-ui">amaç: aynı işi 2 kez yapma</text>
  <text x="50" y="124" font-size="10.5" fill="#1C3247" font-family="system-ui">nadir çift-çalıştırma = biraz israf</text>
  <text x="50" y="148" font-size="10.5" fill="#5D7789" font-family="system-ui">ör. raporu yeniden üretme</text>
  <text x="50" y="174" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Redis SET NX PX YETER</text>
  <rect x="340" y="46" width="270" height="140" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#B26B00" font-family="system-ui">Correctness (doğruluk)</text>
  <text x="360" y="100" font-size="10.5" fill="#1C3247" font-family="system-ui">amaç: iki yazıcı asla bozamaz</text>
  <text x="360" y="124" font-size="10.5" fill="#1C3247" font-family="system-ui">GC pause/saat kayması riski</text>
  <text x="360" y="148" font-size="10.5" fill="#5D7789" font-family="system-ui">ör. para transferi</text>
  <text x="360" y="174" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">FENCING TOKEN şart</text>
</svg>
```

**ZooKeeper** (ZAB) ve **etcd** (Raft) gibi **uzlaşma (consensus)** tabanlı sistemler bu işi daha sağlam yapar. Kilidin durumuna düğümlerin **çoğunluğu (quorum)** karar verir. Bu sayede "kilit kimde" sorusunun tutarlı **tek** bir yanıtı olur; küme ikiye bölünüp iki ayrı sahip çıkması (**beyin bölünmesi / split-brain**) engellenir. Kritik ek şudur: her değişiklik **monoton artan bir sürüm numarası** üretir — ZooKeeper'da `zxid`/`czxid`, etcd'de `revision`/`mod_revision`. Bu numara doğrudan bir fencing token kaynağıdır. Ayrıca istemci ölürse kilidin sonsuza kalmaması için **geçici düğüm (ephemeral node, ZooKeeper)** ya da süreli **kiralama (lease, etcd)** kullanılır. Bunun bedeli genelde ekstra gecikmedir; kazanç hız değil, doğruluk garantisidir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ZooKeeper etcd konsensüs kilidi monoton sürüm sayacını fencing token olarak verir">
  <defs>
    <marker id="dlock-cn-ar" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Konsensüs kilit: doğal fencing token kaynağı</text>
  <rect x="220" y="44" width="200" height="58" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Konsensüs (ZAB/Raft)</text>
  <text x="320" y="88" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kuorum: kilit kimde? tek yanıt</text>
  <text x="320" y="126" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">+ efemeral düğüm / lease → istemci ölürse kilit kalmaz</text>
  <rect x="60" y="140" width="220" height="48" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="170" y="162" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">ZooKeeper: zxid / czxid</text>
  <text x="170" y="180" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">monoton artan sürüm</text>
  <rect x="360" y="140" width="220" height="48" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="470" y="162" text-anchor="middle" font-size="10.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">etcd: revision / mod_revision</text>
  <text x="470" y="180" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">monoton artan sürüm</text>
  <line x1="270" y1="102" x2="190" y2="138" stroke="#186399" stroke-width="1.8" marker-end="url(#dlock-cn-ar)"/>
  <line x1="370" y1="102" x2="450" y2="138" stroke="#186399" stroke-width="1.8" marker-end="url(#dlock-cn-ar)"/>
</svg>
```

### Mülakatta ne sorulur?

- Dağıtık kilit ne zaman gerekir, ne zaman gereksiz karmaşıklıktır?
- Kleppmann'ın Redlock eleştirisinin özü nedir? (Hız değil, zamanlama varsayımı.)
- GC pause senaryosunda fencing yoksa ne olur?
- Fencing token nedir, neden monoton olmalı?
- ZooKeeper/etcd doğruluk (correctness) açısından ne fazladan verir?
- Redis kilidini neye güvenle kullanırsın? (Verimlilik için; doğruluk için değil.)

### Tek cümlede

Zamana dayalı bir dağıtık kilit yalnız **verimlilik (efficiency)** için güvenlidir. **Doğruluk (correctness)** gerektiğinde kilidin kendisi yetmez: korunan kaynağın, monoton artan **fencing token** sayesinde geç kalmış yazımı reddetmesi şarttır.

# dlock-1 | İleri
## Soru
Dağıtık bir kilit (distributed lock) hangi durumda gereklidir?
## A
Ayrı süreçlerdeki kopyaların paylaşılan bir kaynağa aynı anda dokunmasını engellemek için
🤖 Doğru: dağıtık mutual exclusion tam da bu çok düğümlü senaryo içindir.
## B
Her HTTP isteğini tek sıraya sokup istekleri baştan sona serileştirmek amacıyla
🤖 Her isteği kilitlemek iş hacmini (throughput) yok eder; kilit yalnız gerçekten paylaşılan kritik bölümler içindir.
## C
Tek bir JVM içindeki iki thread'in aynı nesneye erişimini eşzamanlamak amacıyla
🤖 Tek süreç içi eşzamanlama için yerel `synchronized`/`Lock` yeterlidir; dağıtık kilit gereksiz karmaşıklıktır.
## D
Veritabanı transaction'ının kendi satır kilitlerinin yerine geçmek ve onları kaldırmak için
🤖 Tek veritabanındaki satır kilitleri zaten atomikliği sağlar; dağıtık kilit onların yerine konmaz.
## Doğru
A
## Açıklama
Dağıtık kilit, **ayrı süreçler ve makineler** arasında **karşılıklı dışlama** gerektiğinde kullanılır: paylaşılan bir kaynağa (dosya, hesap, dış API kotası) veya kritik bir işe aynı anda yalnız bir aktörün dokunması gerektiğinde. Kanonik örnek, yatay ölçeklenmiş bir serviste bir **zamanlanmış görevin** (cron/scheduled job) tüm kopyalarda değil yalnız birinde çalışmasıdır. Tek JVM içindeki thread'ler için (C) yerel kilitler yeter; her HTTP isteğini kilitlemek (B) ölçeklenebilirliği yok eder; tek veritabanı içinde yeten satır kilitlerinin yerine dağıtık kilit koymak (D) gereksiz karmaşıklıktır.

# dlock-2 | İleri
## Soru
Kleppmann'ın Redis **Redlock** algoritmasına yönelik eleştirisinin özü nedir?
## A
Redlock her koşulda güvenlidir; tek zayıflığı birden çok Redis düğümü gerektirmesidir
🤖 Eleştiri tam da "her koşulda güvenli değil"dir; sorun düğüm sayısı değil doğruluk (correctness) garantisidir.
## B
Redlock aşırı yavaştır; yüksek gecikmesi yüzünden pratikte kullanılamaz hale gelir
🤖 Eleştirinin konusu performans değil; zamanlama varsayımlarının doğruluğu bozmasıdır.
## C
Zamanlama varsayımlarına dayanır; GC pause veya saat kayması kilidi geçersiz kılabilir
🤖 Doğru: eleştiri, zamanlama varsayımlarının doğruluğu (correctness) bozabilmesidir.
## D
Redlock tek bir Redis düğümünde çalıştırıldığında çok düğümlü kuruluma göre daha güvenlidir
🤖 Redlock zaten çok düğümlü bir algoritmadır; tartışma düğüm sayısı değil, zamana dayalı kilidin doğruluğudur.
## Doğru
C
## Açıklama
Kleppmann'ın "How to do distributed locking" eleştirisi Redlock'un **zamanlama (timing) varsayımlarına** dayanmasını hedefler. Kilit bir süre için (TTL/lease) verilir; ama istemci kilidi aldıktan sonra uzun bir **GC pause**'a girer, sistem saati **kayarsa** (NTP sıçraması) veya ağ gecikirse, o istemci kilidi hâlâ tuttuğunu **sanarken** aslında süresi dolmuş ve kilit başkasına verilmiş olabilir. Bu durumda iki istemci aynı anda kritik bölüme girer — yani zamana dayalı kilit **doğruluk (correctness)** garanti edemez. Eleştiri hızla (B) veya düğüm sayısıyla (A, D) ilgili değildir; Kleppmann çözüm olarak **fencing token** önerir.

# dlock-3 | Uzman
## Soru
Bir istemci kilidi aldı, sonra uzun bir **GC pause** yaşadı; bu sırada lease süresi doldu ve kilit başka bir istemciye verildi. İlk istemci uyanıp korunan kaynağa yazmaya çalışırsa ne olur (fencing yoksa)?
## A
Hiçbir sorun olmaz; istemci kilidi kendi bırakmadığı için kilit hâlâ ondadır
🤖 Lease süreye bağlıdır ve doldu; istemci "bırakmadım" sansa da kilit artık onun değildir.
## B
Kilit sunucusu araya girip ikinci yazımı otomatik olarak tespit edip engeller
🤖 Kilit sunucusu korunan kaynağa yapılan yazımı görmez; yazımı reddedecek olan kaynağın kendisidir.
## C
İstemci uyanır uyanmaz lease süresinin dolduğunu fark eder ve yazımdan kendiliğinden vazgeçer
🤖 İstemcinin kontrolü ile yazımı arasında yine pause olabilir; "kontrol ettim" garanti vermez.
## D
İki istemci aynı kaynağa yazabilir; bunu ancak kaynaktaki fencing kontrolü engeller
🤖 Doğru: fencing olmadan stale istemci yazar ve çakışma oluşur; çözüm fencing token'dır.
## Doğru
D
## Açıklama
Bu, Redlock eleştirisinin kanonik felaket senaryosudur. İstemci-1 kilidi alır, GC pause'a girer; lease dolar, kilit istemci-2'ye verilir ve o yazar. İstemci-1 uyanır — pause'u fark etmez — ve **hâlâ kilit bende** varsayımıyla yazar. Sonuç: **iki eşzamanlı yazıcı**, yani veri bozulması. Kilidin kendisi bunu engelleyemez, çünkü korunan kaynağa yapılan yazımın arasına girmez. Tek çözüm, **korunan kaynağın** her yazımda bir **fencing token** görmesi ve geç kalmış (küçük token'lı) yazımı reddetmesidir. Süre kontrolü yapmak (C) da yetmez: kontrol ile yazım arasında yeni bir duraklama olabilir. A ve B, kilidin veya çalışma ortamının stale istemciyi kendiliğinden durduracağını sanan yaygın yanılgılardır.

# dlock-4 | Uzman
## Soru
**Fencing token** nedir ve dağıtık kilitte neden zorunludur?
## A
Kilidin TTL süresini otomatik uzatan, arka planda çalışan bir geri sayım sayacıdır
🤖 Fencing token süreyi uzatmaz; süreden bağımsız olarak yazımın güncelliğini doğrular.
## B
Kilitle birlikte verilen monoton artan sayıdır; kaynak küçük token'lı yazımı reddeder
🤖 Doğru: monoton token + kaynağın en yüksek token kontrolü, stale yazımı engeller.
## C
Kilit alındığında üretilen rastgele bir UUID'dir; her istemciye farklı bir değer düşer
🤖 Rastgele UUID sıralanamaz; fencing için token'ın monoton artan olması şarttır ki eski/yeni ayırt edilsin.
## D
İstemciyi tanımlayan sabit bir kimliktir; yazımların birbirine göre sırasını etkilemez
🤖 Sabit kimlik "kim" sorusunu yanıtlar ama "hangi yazım daha yeni" sorusunu çözmez; fencing tam da bunu çözer.
## Doğru
B
## Açıklama
**Fencing token**, GC pause ve saat kayması senaryolarına karşı gerçek korumadır. Kilit servisi her kilit verilişinde **monoton artan** bir sayı üretir (33, 34, 35…). İstemci korunan kaynağa her yazımda bu token'ı ekler; **kaynak**, o ana dek gördüğü **en yüksek** token'ı hatırlar ve daha küçük token taşıyan yazımı **reddeder**. Böylece uyuyup geç uyanan istemci token=33 ile yazmaya kalkarsa, kaynak artık 34 gördüğü için bunu reddeder — çakışma imkânsızlaşır. Kritik nokta token'ın **monoton** olmasıdır; rastgele UUID (C) veya sabit istemci kimliği (D) sıralanamadığı için işe yaramaz; TTL uzatma (A) tümüyle farklı bir mekanizmadır.

# dlock-5 | Uzman
## Soru
**ZooKeeper/etcd** tabanlı kilitler, Redis tabanlı kilitlere göre doğruluk (correctness) açısından neyi daha iyi sağlar?
## A
Hiçbir fark yoktur; ikisi de tam olarak aynı zamanlama varsayımları üzerine kuruludur
🤖 Fark vardır: konsensüs tabanlı sistemler lineerize edilmiş kilit durumu ve doğal fencing kaynağı sunar.
## B
Yalnızca daha hızlıdırlar; doğruluk açısından Redis tabanlı kilitlerle eşdeğerdirler
🤖 Konsensüsün maliyeti genelde ekstra gecikmedir; asıl kazanç hız değil doğruluk garantileridir.
## C
Konsensüsle lineerize kilit durumu ve monoton sürüm (zxid/revision) sağlarlar
🤖 Doğru: konsensüs + monoton sürüm sayacı, doğal fencing ve güçlü tutarlılık sağlar.
## D
Saate hiç ihtiyaç duymadıkları için kilitleri süresiz tutabilirler, bu yüzden daha güvenlidirler
🤖 Süresiz kilit tehlikelidir (istemci ölürse kilit sonsuza kalır); bu sistemler tam da bunu önlemek için lease kullanır.
## Doğru
C
## Açıklama
**ZooKeeper** (ZAB) ve **etcd** (Raft) bir **konsensüs protokolü** üzerine kuruludur; kilit durumu bir çoğunluk kuorumunca lineerize edilir, yani "kilit kimde" sorusuna tutarlı tek bir yanıt vardır (split-brain'e karşı dayanıklı). Kritik ek: her değişiklik **monoton artan bir sürüm numarası** üretir (ZooKeeper'da `zxid`/`czxid`, etcd'de `revision`/`mod_revision`) — bu doğrudan bir **fencing token** kaynağıdır. Ayrıca istemci ölürse kilidin sonsuza kalmaması için **efemeral düğüm** (ZooKeeper) veya süreli **lease** (etcd) kullanılır. Böylece hem "kim tutuyor" hem de "hangi yazım daha yeni" güvenle çözülür. Fark yok (A) veya sadece hız (B) yanlıştır; süresiz kilit (D) bir güvenlik özelliği değil, kaçınılması gereken bir tehlikedir.

# dlock-6 | Uzman
## Soru
[TUZAK] Redis tabanlı dağıtık kilidi, sağlamlık açısından **ne için** güvenle kullanabilirsin?
## A
Verimlilik (efficiency) için: nadir bir çift çalıştırmanın yalnızca israf olduğu işlerde
🤖 Doğru: Redis kilidi efficiency için uygundur; correctness'i tek başına garanti etmez.
## B
Hem efficiency hem correctness için; tek başına her iki garantiyi de tam sağlar
🤖 Tuzak burada: Redis kilidi correctness'i tek başına garanti edemez (GC pause/saat kayması); fencing gerekir.
## C
Yalnız doğruluk (correctness) için: para ve veri bütünlüğü kritik işlemlerde tek başına yeterlidir
🤖 Tam tersi: correctness kritik yerde Redis kilidi tek başına yetmez; asıl güvenli kullanım efficiency'dir.
## D
Hiçbir şey için; dağıtık kilit fikri tümüyle yanlıştır ve pratikte terk edilmiştir
🤖 Aşırı yorum: dağıtık kilit meşrudur; Redis kilidi efficiency için gayet uygundur.
## Doğru
A
## Açıklama
Kleppmann'ın ayrımı tam da budur ve klasik bir mülakat tuzağıdır. Bir kilidin iki farklı amacı olabilir. **Efficiency**: aynı pahalı işi (bir raporu yeniden üretmek, bir e-postayı yeniden göndermek) gereksiz yere iki kez yapmayı **çoğunlukla** önlemek; burada **nadir** bir çift çalıştırma yalnızca biraz israftır, felaket değildir, dolayısıyla Redis kilidi (`SET NX PX`) **yeterlidir**. **Correctness**: iki eşzamanlı aktörün asla veri bozamaması gereken durum (para transferi, benzersizlik garantisi); burada zamana dayalı Redis kilidi GC pause ve saat kayması yüzünden **tek başına yetmez**, **fencing token** zorunludur. "İkisi için de tek başına yeterli" (B) tam tuzaktır; "correctness için ideal" (C) tehlikeli bir yanlıştır; dağıtık kilidi tümüyle reddetmek (D) ise aşırıdır.
