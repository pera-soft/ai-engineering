---
hafta: 11
baslik: "Veri-Yoğun Sistemler (DDIA)"
hedef: "DDIA 2e (Mart 2026) konu bazlı: trade-off'lar, nonfunctional requirements, replikasyon/partitioning, tutarlılık — kapaktan kapağa değil."
kaynak: "Kitap: Designing Data-Intensive Applications 2e (Kleppmann & Riccomini, Mart 2026) — çekirdek: Trade-offs + NFR + dağıtık sistem bölümleri; yeni: object storage, AI iş yükleri"
---

## Quiz
### S1: Gecikme (latency) ölçümünde p99, ortalamaya göre neyi daha iyi gösterir?
- [ ] Ortalama tüm istekleri saklamayı gerektirir; yüzdelik dilim daha ucuza hesaplanır
- [x] Ortalama en yavaş dilimi gizler; o dilim çoğu kez en değerli kullanıcıya denk gelir
- [ ] p99 sistemin en hızlı yüzde birini ölçer; darboğaz da hep en hızlı yolda görünür
- [ ] Ortalama yalnızca başarısız istekleri kapsar; p99 ise başarılı istekleri ayrı ölçer
> Açıklama: p99, isteklerin %99'unun ALTINDA kaldığı süredir — yani en yavaş %1'in eşiği, en hızlı %1 değil. Kleppmann'ın klasik noktası: en yavaş istekler genelde en çok verisi olan, yani en değerli kullanıcılardır. Ortalama bu kuyruğu düzleyip saklar.

### S2: Yazma yükü çok yoğun bir iş yükünde LSM-tree'yi B-tree'ye tercih etmenin ana gerekçesi nedir?
- [ ] Her yazmayı diskte kaydın bulunduğu yerde günceller; fazladan yer arama yapmaz
- [ ] Okumayı tek bir sıralı dosyada bitirir; bloom filtresine hiç ihtiyaç duymaz
- [x] Yazmaları bellekte biriktirip diske ardışık blok olarak döker; rastgele yazma azalır
- [ ] Arka planda birleştirme (compaction) yapmadığı için fazladan disk trafiği üretmez
> Açıklama: LSM ardışık yazar (write-optimized). Bedeli arka plandaki compaction ve okumada birkaç katmana birden bakmaktır (bloom filter bunu hafifletir). "Yerinde güncelleme" B-tree'nin davranışıdır: okuması öngörülebilir, yazması pahalıdır. Mutlak kazanan yok; seçim iş yüküne bağlıdır.

### S3: 'Read-your-writes' (kendi yazdığını okuma) garantisi hangi sorunu çözer?
- [x] Kişinin az önce yazdığı verinin, geride kalmış bir replikadan okunup yok görünmesini
- [ ] İki kullanıcının aynı satırı aynı anda değiştirmesinden doğan yazma çakışmasını
- [ ] Her kullanıcının her okumada en güncel veriyi görmesini, yani tam tutarlılığı
- [ ] Lider çöktüğünde yeni lider seçilene kadar yazmaların durmasını (failover süresini)
> Açıklama: Replikasyon gecikmesinin en can sıkıcı belirtisi budur: kullanıcı kendi yorumunu göremez, "kayboldu" sanıp ikinci kez yazar. Garanti yalnızca KENDİ yazdıklarını kapsar; başkasının yazdığını geç görmek hâlâ mümkündür — bu yüzden tam tutarlılık değildir.

### S4: Anahtarı hash'leyerek bölümlemenin, anahtar aralığına göre bölümlemeye kıyasla bedeli nedir?
- [ ] Yükü tek bir bölüme yığdığı için sıcak nokta (hot spot) riskini belirgin biçimde artırır
- [ ] Aynı anahtar iki bölüme birden düşebildiğinden veri tekrarına ve tutarsızlığa yol açar
- [ ] Bölüm sayısı arttıkça yazma verimi düşer; yatay ölçeklenmeyi tümden engeller
- [x] Aralık sorguları artık tek bölümde bitmez; her bölüme ayrı ayrı sorulması gerekir
> Açıklama: Hash dağılımı yükü dengeler, yani hot spot'u AZALTIR — artırmaz. Ödenen bedel komşuluğun kaybolmasıdır: "1–31 Ocak arası siparişler" sorgusu bütün bölümlere dağılır (scatter-gather) ve en yavaş bölüm kadar sürer.

### S5: Serializable izolasyon seviyesini seçmenin tipik bedeli nedir?
- [ ] Anomaliler artar; hayalet okuma ve yazma eğriltmesi (write skew) daha sık görülür
- [ ] Yazının diske ulaşma garantisi zayıflar; çökmede son işlemler kaybolabilir
- [x] İşlem hacmi düşer; kilitleme ya da çakışanı iptal edip yeniden deneme gerekir
- [ ] Şemayı normalleştirmek ve tüm tablolara yabancı anahtar eklemek zorunlu olur
> Açıklama: Serializable anomalileri ARTIRMAZ, hepsini ortadan kaldırır; ödenen şey performanstır. Dayanıklılık (durability) izolasyondan bağımsız bir garantidir, zayıflamaz. Mimarın işi: hangi akış gerçekten serializable ister, hangisi snapshot izolasyonuyla yaşar?
