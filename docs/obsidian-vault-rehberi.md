# Obsidian Vault Rehberi — İkinci Beyin (git dışı)

> `AGENTS.md`'den taşındı (2026-07-19, "talimat şişmesi" dersinin uygulaması —
> bkz. `content/kaynaklar/harness-engineering-openai.md` §4.1). Bu dosya, vault'a
> yazan HER oturumun ilk vault işleminden önce okuması gereken bağlayıcı rehberdir.
> Buradaki her madde bir oturumda ödenmiş bir bedelin karşılığıdır — kısaltma.

**Yol:** `~/Documents/Obsidian Vault/AI ÖĞRETMEN/`

- **ALWAYS:** iş bitince vault'a yaz — *ne yapıldı · ne var · ne kaldı*.
  Vault kökündeki `CLAUDE.md` **bağlayıcıdır** — önce onu oku, şema orada.
- **NEVER:** vault'a şifre/token/secret/JWT/API key yazma. Ortam adı ve URL olur,
  kimlik bilgisi olmaz.
- Vault reponun **kopyası değil**. Kod, ADR ve karar detayı repoda kalır;
  vault "ne var ne yok"un tek bakışta görüldüğü yerdir.

## Önce eşik — her iş değil, ANLAMLI iş
Ölçüt tek soru: **3 ay sonraki okuyucu (muhtemelen sen) bunu bilmek ister mi?**

- **Yaz:** ne var/ne kaldı değişti (yeni özellik, açılan/kapanan iş, canlıya çıkan şey) ·
  bir karar verildi · bir tuzağa düşülüp çözüldü · kalıcı bir kavram öğrenildi (terfi sorusu).
- **Yazma:** yazım/biçim düzeltmesi, tek satır refactor, bağımlılık bump'ı, yeşil testi
  tekrar çalıştırmak. Ve en önemlisi: **değişmeyen sayfaya tarih atma** — "güncellendi" görünen
  ama içeriği aynı sayfa, okuyucuyu yanıltır; gürültü, bilgi değildir.
- Sadece **gerçekten değişen sayfayı** güncelle; iş bir sayfayı ilgilendirmiyorsa ona dokunma.

## İş bitince — sırayla (eşiği geçtiyse)
1. `AI ÖĞRETMEN/04-Aktif-Durum/` güncelle — **ne var / ne kaldı**. En çabuk eskiyen yer burası.
2. Yeni sayfa açtıysan `AI ÖĞRETMEN/README.md` (MOC) satırını ekle.
   Kural: **her sayfa bir MOC'tan link ALMALI** (orphan = *gelen* linki olmayan sayfa;
   giden linki olmaması sorun değil).
3. `Memory.md` — aktif proje / açık görev değiştiyse.
4. `index.md` — yeni sayfa eklendiyse.
5. `log.md` — **EN ÜSTE** tarihli kayıt ekle.
   ⚠️ Dosya **TERS KRONOLOJİK**. `tail` çekip en alta bakma; orası kurulum günü.
6. **Terfi sorusu — ATLAMA:**
   *"Bu iş, sadece bu projeye ait OLMAYAN bir kavram öğretti mi?"*
   → **Evet** ise `wiki/concepts/<kavram>.md` yaz veya güncelle, proje sayfasından link ver.
   → **Hayır** ise geç. **Zorlama — yazmamak da geçerli bir cevaptır.**
7. Doğrula: `python3 ~/Documents/Obsidian\ Vault/.scripts/vault-lint.py`
   **Kırık link ARTMAMALI, yeni orphan ÇIKMAMALI.**

## Link disiplini
- `[[wikilink]]`, Türkçe, **tam yol tercih et**: `[[SC/Takas/README]]`.
  Vault'ta 6 tane `README.md` var — çıplak `[[README]]` yanlış dosyaya çözülebilir.
- **Gövde içine link ver, footer'a değil.** `## İlgili Sayfalar` listesine toplu
  link atmak **sahte bağ** üretir: sayfalar "aynı konu" diye bağlanır, kavramı
  paylaştıkları için değil. **Metin o şeyden söz etmiyorsa link verme.**
- Başka projenin dosyasını **TAŞIMA / SİLME / yeniden adlandırma**.

## Yeni proje açıyorsan — standart iskelet
README.md (MOC, ZORUNLU) · 01-Genel-Bakış · 02-Yapılan-İşler · 03-Mimari ·
**04-Aktif-Durum (ZORUNLU)** · 05-Teknik-Referans · 07-Kararlar-ADR · 08-Hatalar-Çözümler
