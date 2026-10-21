---
hafta: 2
gun: 1
baslik: "AGENTS.md felsefesi: ansiklopedi değil, içindekiler"
---

## Ders

### Bugün ne öğreneceksin?
Bugünün tek cümlesi: **AGENTS.md bir ansiklopedi değil, içindekiler tablosudur.** Nedenini anlarsan, "kural dosyasını büyüterek iyileştirme" refleksinden kurtulursun — bu refleks, harness mühendisliğinin en yaygın ve en sinsi hatasıdır. Bu hafta, Hafta 1'de okuduğun harness metninin "repo = kayıt sistemi" bölümünün birebir uygulamasıdır.

### Temel kavramlar
- **AGENTS.md:** deponun kökünde duran, ajanın her oturumda okuduğu yönlendirme dosyası. Yaygın bir konvansiyondur; Claude Code aynı içeriği `CLAUDE.md` adıyla arar.
- **Bağlam (context):** ajanın o an önünde duran metnin tamamı — isteğin, okuduğu dosyalar, kurallar, konuşma. Benzetme: bir **valiz**. Büyük olabilir ama sonsuz değildir; kışlık montu koyarsan diş fırçasına yer kalmaz.
- **İşaretçi (pointer):** cevabın kendisi değil, cevabın **adresi**. "Kararlar: docs/adr/" bir işaretçidir.
- **Talimat şişmesi (instruction bloat) → bağlam çürümesi (context rot):** kural dosyasının sürekli büyümesi ve sonunda ajanın uzun metindeki kuralı fiilen görmemesi.

Bağlam kıtlığının **iki** yüzü var; karıştırma:
1. **Fiziksel sınır (yer bitiyor).** Modelin alabildiği metin miktarı sabittir. Dolarsa taşan kısım kırpılır ya da özetlenir — ve özetlenirken hangi ayrıntının önemli olduğu bilinemez.
2. **Dikkat sınırı (yer var ama gözden kaçıyor).** Valiz yarı doluyken bile, 45 satırlık bir metindeki kritik cümleyi bulmak 2000 satırlıktakinden çok daha kolaydır. Gürültü arttıkça sinyal kaybolur. *"Yer var, hepsini koyalım"* savunması bu yüzden yanlıştır.

### Somut örnek
Bu deponun kendi `AGENTS.md`'si: **45 satır, 4 başlık.**
- **Komutlar:** `./mvnw verify`, `./mvnw -Dtest=MimariTestleri test`. Ajan "nasıl doğrularım?" sorusunu tek satırda çözer; Maven kılavuzunu okumasına gerek kalmaz.
- **Sınırlar:** *"ALWAYS: types → config → repo → service → ai → web katman yönüne uy"*, *"ASK FIRST: yeni bağımlılık (önce docs/adr/'ye ADR)"*. Kuralın kendisi burada; kuralın **gerekçesi** burada DEĞİL.
- **Nerede ne var:** *"Mimari: ARCHITECTURE.md · Kararlar: docs/adr/ · Planlar: docs/exec-plans/"*. Bu satırlar birer işaretçidir — yol tarifi, içerik değil.
- **Sonuç:** Ajan "OpenRouter'ı neden seçmişiz?" diye merak ederse cevabı AGENTS.md'de bulamaz — ama `docs/adr/0004-openrouter-saglayici.md`'ye gitmesi gerektiğini bulur. Bu bir eksiklik değil, **tasarımdır**: 9 ADR'nin tamamı valizde değil, **rafta** durur; ajan sadece gerekeni indirir.

Aynı bilgi ansiklopedi tarzında yazılsaydı, AGENTS.md'nin içinde 9 ADR'nin, mimari tablosunun ve borç listesinin **tam metni** olurdu. Ajan her küçük görevde bu yükü taşır, asıl kodu okuyacak yeri kalmazdı.

### Sık yapılan hata
Her aksilikten sonra AGENTS.md'ye "makul" bir kural daha eklemek. Tek tek hepsi haklıdır; toplamı zehirdir. **Bu depoda gerçekten yaşandı:** `AGENTS.md` tek bir günde, üç ayrı commit'le 29 satırdan 85 satıra çıktı — yani ilkeyi öğreten metin ile onu ihlal eden davranış aynı depoda, aynı gün. Talimat şişmesi kötü niyetten değil, **iyi niyetin birikiminden** doğar ve ancak ölçülürse fark edilir: `wc -l AGENTS.md`.

İkinci hata: ajanın zaten bildiği genel iyi pratikleri yazmak ("okunaklı kod yaz", "önce düşün"). Süzgeç şudur: *bu satır, ajanın **varsayılan** davranışından sapmayı mı anlatıyor?* Sapmıyorsa sil — ajan onu zaten biliyor. Sapıyorsa kalsın, ama tek satır artı tek işaretçi olsun.

> Ölçüt cümlesi: AGENTS.md'ye eklemek istediğin satır bir **cevap** mı, yoksa **cevabın adresi** mi? Cevaplar docs/'a, adresler AGENTS.md'ye.

### Görevlere geçmeden önce
Aşağıdaki görevler sırasıyla: örneği incele, kendi haritanı yaz (satır sınırı kasıtlıdır — kısıt olmadan ansiklopedi yazarsın), sonra symlink ile CLAUDE.md'yi aynı dosyaya bağla. Üçüncüsü küçük görünür ama iki dosyanın zamanla birbirinden sapmasını (drift) fiziksel olarak imkânsız kılar.

## Görevler
- [ ] AGENTS.md şablonunu incele: ~100 satır, yalnızca 'içindekiler' (komutlar, sınırlar, nerede-ne-var)
- [ ] Kendi projen için taslak AGENTS.md yaz (<120 satır)
- [ ] ln -s AGENTS.md CLAUDE.md symlink'ini kur
