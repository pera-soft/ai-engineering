---
hafta: 10
baslik: "Mimari Kararları Kolaylaştırmak (Advice Process)"
hedef: "Facilitating Software Architecture: Advice Process, ADR ve merkeziyetsiz mimari karar verme — insan VE ajan kararlarını hizalayan liderlik."
kaynak: "Kitap: Facilitating Software Architecture (Harmel-Law, O'Reilly 2024) — Advice Process, ADR, technology radar"
---

## Quiz
### S1: Advice Process'te karar veren kişinin zorunluluğu nedir?
- [ ] Ekipten çoğunluk oyu toplamak
- [x] Etkilenenlere ve uzmanlara danışmak
- [ ] En kıdemli mimarın onayını almak
- [ ] Alınan tavsiyelerin hepsini uygulamak
> Açıklama: Zorunlu olan danışmadır, uymak değil: karar veren, karardan etkilenenleri ve konunun uzmanlarını samimiyetle dinler, son sözü yine kendisi söyler. Oy/onay arayan şıklar süreci konsensüse çevirir; Advice Process tam olarak bunu reddeder.

### S2: Advice Process ile verilen bir karar sonradan yanlış çıkarsa ne yapılır?
- [ ] Danışılan uzmanlar sonuçtan sorumlu tutulur
- [ ] Karar dokunulmazdır, sonuna kadar taşınır
- [x] Karar yenilenir ve ADR güncellenir
- [ ] Karar yetkisi baş mimara geri alınır
> Açıklama: Kararlar kalıcı değil tarihlidir; bağlam değişince eski ADR "superseded" (yerine geçildi) işaretlenir ve yeni bir kayıt açılır. Danışanı suçlamak ya da yetkiyi geri toplamak, bir daha kimsenin karar vermemesine yol açar.

### S3: Architecture Advice Forum'un temel amacı nedir?
- [ ] Kararları oylayarak resmen onaylamak
- [ ] Mimari standartları merkezî olarak belirlemek
- [ ] Karar yetkisini bir komiteye devretmek
- [x] Kararları ve danışmaları görünür kılmak
> Açıklama: Forum bir onay kapısı değil, ortak öğrenme alanıdır: karar sahibi kararını ve aldığı danışmayı açıkça anlatır, herkes bağlamı görür. Oylama/komite şıkları darboğazı geri getirir.

### S4: ADR'nin Advice Process içindeki rolü nedir?
- [ ] Kararı ekibe dayatmanın resmî yolu olmak
- [x] Kararı, bağlamı ve danışmayı kaydetmek
- [ ] Kod ve dosya biçimi kurallarını belgelemek
- [ ] Toplantı katılımını ve tarihlerini tutmak
> Açıklama: ADR bir emir değil hafızadır: ne karar verdik, hangi bağlamda, kime danıştık, hangi tavsiyeyi neden almadık. Bu projedeki `docs/adr/` dizini tam olarak bunun için vardır.

### S5: Technology Radar'ın dört halkası (ring) hangileridir?
- [ ] Öner / Onayla / Reddet / Beklet
- [ ] Adopt / Trial / Deprecate / Retire
- [x] Adopt / Trial / Assess / Hold
- [ ] Assess / Approve / Audit / Adopt
> Açıklama: Adopt (benimse, varsayılan seçim), Trial (riski kaldırılabilir bir işte dene), Assess (araştır, henüz üretime koyma), Hold (yeni yerde kullanma). Halkalar bir onay/ret makamı değil, ekibin ortak duruş beyanıdır.
