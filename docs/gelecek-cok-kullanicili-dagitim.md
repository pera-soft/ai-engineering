# Gelecek: Çok-Kullanıcılı / Ekip Dağıtımı ve Kimlik Doğrulama

> **Durum: TARİHSEL — Faz 9'da uygulandı.** Erteleme 2026-10-22'de kullanıcı isteğiyle kalktı;
> çok kullanıcılı site `site` profili olarak hayata geçirildi. Güncel kararlar için:
> [docs/exec-plans/active/faz-9-cok-kullanicili-site.md](exec-plans/active/faz-9-cok-kullanicili-site.md)
> ve [ADR-0012](adr/0012-cok-kullanicili-site-profili.md) (e-posta: ADR-0013, PostgreSQL: ADR-0014;
> kurulum: [kurulum-site.md](kurulum-site.md)). Aşağıdaki metin 11 Tem 2026 tartışmasının notu
> olarak olduğu gibi bırakıldı; "bugünkü durum" ve seçenekler o tarihe aittir ve artık güncel değildir.
>
> (Eski durum satırı: "ERTELENDİ (bilinçli). Önce tüm eğitim/müfredat bitirilecek, sonra sistemdeki
> açıklar kapatılacak; dağıtım ve çok-kullanıcı işi ondan sonra değerlendirilecek.")

## Soru
"Yalnız yerelde değil, ekip içinde (hatta internete açık) kullanmak istesek nasıl
bir sistem olur? Her kullanıcıya login/şifre mi, ortak mı, tümüne açık mı? Ama AI
Öğretmen için token yetmez — nasıl çözeriz?"

## Bugünkü durum (başlangıç noktası)
- Tek kullanıcı; **sıfır kimlik doğrulama**.
- H2 **dosya** veritabanı (`data/ogrenme.mv.db`) — tek süreç, tek kullanıcı varsayımı.
- Kişisel veri tabloları (`gorev_ilerleme`, `quiz_sonuc`, `sohbet_mesaj`, kullanım)
  **`kullanici_id` içermiyor**.
- Öğrenci hafızası `data/hafiza/` — **tek öğrenciye** özel markdown ağacı.
- AI çağrıları tek OpenRouter anahtarıyla (env; git'te değil).
- Sunucu **127.0.0.1**'e bağlı (bilinçli güvenlik freni).

## Bu, iç içe ÜÇ ayrı karardır

### 1) Erişim modeli
- **Ortak/genel (tek hesap) — UYMAZ.** Ürünün özü kişisel takip: `data/hafiza/`,
  ilerleme, quiz, sohbet kişiye özel. Ortak hesapta hepsi karışır. Sadece
  salt-okunur demo için mantıklı.
- **Kişi başı login — ÖNERİLEN.** Öğrenme aracının doğası bunu zorunlu kılar.
- **Tümüne açık internet — asıl risk token.** Auth'suz internete açık "LLM'e bağlı
  sohbet kutusu" = herkesin senin anahtarınla bedava Claude kullanması ("token
  yetmez"in saldırı hâli). Ayrıca KVKK yükümlülüğü (öğrenme verisi, sohbet, kitap
  fotoğrafları).

### 2) Token / maliyet (ASIL MESELE)
Sorun: **tek anahtar herkesi finanse ediyor.** Her AI eylemi (sohbet, değerlendirme,
quiz üretimi, hafıza güncelleme, kitap OCR) token yakar. 1 kişi sınırlı; N kişi ~N
kat; internet = sınırsız.

İki temel yaklaşım:
- **A) BYOK (Bring Your Own Key):** her üye kendi OpenRouter/Anthropic anahtarını
  girer (şifreli saklanır). ✅ Maliyet dağılır, sınırsız ölçek. ❌ Kurulum
  sürtünmesi + anahtar güvenliği (at-rest encryption).
- **B) Merkezi anahtar + sıkı kota:** tek kurumsal anahtar + **kişi başı günlük
  token bütçesi** + hız sınırı + sert aylık tavan. ✅ Sıfır sürtünme. ❌ Parayı
  sen/şirket öder; kotasız patlar.

**Yapısal tasarruf (her iki durumda; maliyeti ~2-5x düşürür):**
- **Prompt caching (Anthropic):** sistem kimliği + müfredat bağlamı her çağrıda
  tekrar ediyor → önbelleğe al → girdi maliyeti o kısımda ~%70-90 düşer. En büyük kazanç.
- **Model yönlendirme:** çoğu iş Haiku (ucuz), yalnız derin değerlendirme Sonnet.
- **max_tokens tavanı + bağlam kırpma.**

**Mevcut avantaj:** kodda zaten `KullanimService` (kullanım takibi) ve
`BaglamKirpici` (bağlam kırpma) var → kota + tasarruf altyapısının yarısı hazır.

**Kaba maliyet sezgisi (kesin değil, fiyatlar değişir):** 10 kişi × ~20 AI eylem/gün,
prompt caching + Haiku ağırlıklı → kabaca **aylık ~50-150$** mertebesi, merkezi
anahtar + kota ile yönetilebilir. Kotasız/açık internette kötüye kullanımla bir
gecede 1000'lere çıkabilir.

### 3) Kimlik doğrulama (auth) yöntemi
- **Google/Microsoft SSO** — Workspace/365 varsa en az sürtünme + en güvenli
  (şifre yönetimi yok). Ekip zaten Google kullanıyor.
- **Kullanıcı adı + şifre** (Spring Security + BCrypt, admin hesap açar) — SSO
  altyapısı yoksa basit ve bağımsız.
- **E-posta magic link** — şifresiz; e-posta gönderimi gerektirir.

## Teknik değişiklik haritası (tek→çok kullanıcı)
| Katman | Bugün | Ekip için |
|--------|-------|-----------|
| Kimlik | Yok | Spring Security (SSO ya da user/pass) |
| Veri modeli | `kullanici_id`'siz tablolar | Her tabloya `kullanici_id` FK + tüm sorgular kullanıcıya göre filtre; `data/hafiza/` → `data/hafiza/<kullanici_id>/...` |
| Veritabanı | H2 dosya | **PostgreSQL** (Spring Data JDBC küçük ayar); H2 küçük ekipte zorlar |
| Ağ/host | 127.0.0.1 | Sunucu (VM/konteyner) + **HTTPS/TLS** (Caddy/nginx) + auth arkasında 0.0.0.0. "Sadece localhost" freni yerine **auth + TLS** |
| Sırlar | Env'de tek anahtar | Merkezi anahtar env'de; BYOK ise kişi anahtarları şifreli |
| KVKK | Konu değil | Rıza metni + veri saklama/silme politikası (özellikle kitap fotoğrafları) |

## Önerilen aşamalı yol (uygulanacağı zaman)
1. **Faz 1 — Ekip MVP:** Google SSO + Postgres + kişi-başı veri izolasyonu +
   merkezi anahtar + **kişi başı günlük token kotası** + prompt caching.
   **İnternete açık değil** — iç ağ/VPN veya davetli, HTTPS arkasında. İhtiyacın
   ~%90'ını en az riskle karşılar.
2. **Faz 2 — Gerekirse genişletme:** dışarı açılacaksa BYOK + davet/onay kapısı +
   kötüye kullanım tespiti + sert tavan.

**Özet:** kişi başı login evet; ortak hesap hayır; tüm internet ancak kota+auth
(veya BYOK) ile — yoksa mali intihar.

## Karar verilecek (o güne bırakıldı)
- [ ] Erişim kapsamı: sadece ekip (davetli/iç ağ) / herkese açık ama kayıtlı / tamamen açık
- [ ] Token maliyeti: merkezi anahtar+kota / BYOK / karma
- [ ] Kimlik: Google-Microsoft SSO / kullanıcı adı+şifre / magic link
- [ ] Ekip büyüklüğü ve kimler (iç kolega / dış)?
