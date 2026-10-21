# ARCHITECTURE.md — AI Öğretmen

## Katman haritası (tek yönlü, ArchUnit ile zorlanır)

```
types → config → repo → service → ai → web
```

| Katman | Paket | İçerik | Erişebilir |
|---|---|---|---|
| Types | `tr.velzon.aiogretmen.types` | record'lar, enum'lar, DTO'lar | (hiçbir şey) |
| Config | `..config` | AppProperties, model adları, content yolu | types |
| Repo | `..repo` | H2 erişimi (Spring Data JDBC) + ContentRepo (markdown okuyucu) | types, config |
| Service | `..service` | MufredatService, IlerlemeService, (Faz 2: Degerlendirme/Quiz/Rapor) | types, config, repo, ai |
| AI (runtime) | `..ai` | Claude entegrasyonu — Spring AI ChatClient BURADA yaşar, tek çıkış noktası | types, config |
| Web (UI) | `..web` | REST controller'lar + static/ frontend — SADECE service'i çağırır | types, service |

## Kurallar
- `types` hiçbir katmana bağımlı olamaz.
- `web` doğrudan `repo` veya `ai`'a erişemez — her şey `service` üzerinden.
- Claude API çağrısı SADECE `ai` paketinde (Spring AI sınıfları başka pakette import edilemez).
- Kesişen kaygılar (ileride: telemetri, feature flag) arayüz üzerinden girer.

## Veri
- H2 file DB: `./data/ogrenme.mv.db` (şema: `src/main/resources/schema.sql`)
- Müfredat: `content/hafta-NN/{hafta.md, gun-N.md}` — frontmatter'lı markdown, açılışta belleğe yüklenir.
- Öğrenci hafızası (Faz 2): `data/hafiza/` markdown ağacı — AI öğretmen yazar, prompt'lara enjekte edilir.

## Doğrulama
Bu kurallar `src/test/java/tr/velzon/aiogretmen/MimariTestleri.java` (ArchUnit) ile
CI'da bloklayıcı olarak test edilir. Kural ihlali = build kırmızı.
