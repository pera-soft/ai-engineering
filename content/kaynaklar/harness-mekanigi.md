---
baslik: "Harness Mekaniği — Skills, Subagent'lar ve Hook'lar Fiilen Nasıl Çalışır"
yazar: "AI Öğretmen çalışma metni — Anthropic Claude Code dokümanı + bu deponun kendi ~/.claude kurulumu vaka olarak"
tarih: "2026-07-20"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 1,3
---

### Bu metin ne anlatıyor?
Bu metin, yapay zekâ ajanının **etrafındaki düzeneğin** — harness'ın — parça parça nasıl
çalıştığını anlatıyor. Dört parça var: her seferinde okunan kalıcı kurallar, sadece gerektiğinde
yüklenen uzman talimatları (skills), işi ayrı bir pencerede yaptıran yardımcı ajanlar
(subagent'lar) ve modelden bağımsız çalışan otomatik betikler (hook'lar). Amaç şunu görmen:
modelin kendisini değiştiremezsin, ama etrafındaki bu dört parçayı sen kurarsın — ve günlük işte
sonucu asıl belirleyen budur. Sonda iki ek soru var: model ile harness üretim tarafında nasıl
birlikte evriliyor (§6) ve modeller zekileştikçe harness'a ne olacak (§7)?

## Neden bu özet
[`model-ve-caba.md`](model-ve-caba.md) **modelin içini** açtı: tokenizasyon, donmuş ağırlıklar,
inference. Oradaki tek cümle şuydu — *ağırlıklar eğitimde yazılır, kullanımda salt-okunurdur.* Model
donmuşsa, günlük işte davranışını asıl şekillendiren şey modelin kendisi değil, **etrafındaki
harness**tır: neyi görebildiği, hangi talimatı ne zaman aldığı, hangi işi kime devrettiği, hangi
adımın deterministik olarak zorlandığı. Hafta 1'in çekirdek tezi bunu tek denklemde söyler:

> **Agent = Model + Harness.** Modeli seçemezsin (donmuş, satıcının), ama harness'ı **sen** kurarsın.
> Bu özet o denklemin ikinci teriminin **mekaniğini** verir: Claude Code'da skills / subagent'lar /
> hook'lar makinede *fiilen* ne yapar?

Bu soyut bir merak değil, çünkü **elindeki kurulum bu desenin canlı örneği**: `~/.claude/rules/`
altında 12 kural dosyası, düzinelerce skill, `agents/` altında subagent tanımları, `settings.json`'da
kayıtlı hook'lar. Dahası: **bu metni üreten oturumun kendisi** bu mekanikleri gözünün önünde çalıştırdı
(§4 ve Diyagram 2). Kardeş kaynak [`harness-engineering-openai.md`](harness-engineering-openai.md)
harness'ı **kültür/ilke** düzeyinde (AGENTS.md haritası, guide/sensor, ölç-ve-buda) anlatır; bu özet
aynı harness'ın **çalışan parçalarını** söker: hangi dosya, hangi olay, hangi sıra.

Üç diyagram fikri kısaca gösterir; ayrıntı metindedir.

## Türkçe çalışma metni

### 1. Harness nedir — "Model + Harness"in ikinci kutusu
Model tek başına bir **olasılık makinesidir**: girdiyi alır, sıradaki token'ı tahmin eder, durur
(bkz. [`model-ve-caba.md`](model-ve-caba.md) §2). Onu bir **ajana** çeviren şey etrafındaki dört
parçadır — hepsi senin kurduğun, hiçbiri modelin ağırlıklarında olmayan:

Önce iki kelimeyi netleştirelim, çünkü metnin tamamı bunların üstünde duruyor. **Bağlam**
(context): modele o istekte gönderilen metnin tamamı — sistem talimatı, kurallar, konuşma
geçmişi, okunan dosyalar. **Bağlam penceresi**: bu metnin sığabileceği azami boy; sınırlıdır ve
her eklenen satır, asıl işe kalan yeri azaltır.

- **Rules (kurallar):** her oturumda **daima** bağlama giren kalıcı yönergeler (`~/.claude/rules/`,
  bu kurulumda **12 dosya**). "Her zaman şöyle davran."
- **Skills:** **koşullu** yüklenen uzman talimatı — sadece ilgili işte bağlama girer (§2).
- **Subagent'lar:** ana bağlamı yakmadan işi **izole bir pencerede** yaptıran alt-ajanlar (§3).
- **Hook'lar:** modelin değil, **kabuğun** (shell — komutları çalıştıran program) **deterministik**
  (aynı koşulda her zaman aynı şeyi yapan, rastgeleliği olmayan) biçimde çalıştırdığı betikler (§4).

Kritik ayrım en baştan konsun: **rules ve skills modele metin enjekte eder (yönlendirme/steering);
hook'lar modelden bağımsız kod çalıştırır (zorlama/enforcement).** İlki [`model-ve-caba.md`](model-ve-caba.md)
§2'nin diliyle "bağlam yönlendirir, öğretmez" — ağırlığı değiştirmez, sadece o isteğin girdisine metin
koyar. İkincisi ağırlıklara da, bağlama da değil, **sürece** müdahale eder. Bu ikiliyi §5'te guide/sensor
olarak büyüteceğiz; önce dört parçanın tek tek mekaniği.

## Diyagram 1 — Harness anatomisi: donmuş model + dört harness parçası
Model ortada donmuş bir çekirdek (ne kadar *bilir*); etrafındaki dört parça harness'tır (ne
*yapabilir*). Rules/skills metin enjekte eder, hook'lar deterministik kod koşar, subagent'lar işi izole
pencereye devreder.

```svg
<svg viewBox="0 0 660 372" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Harness anatomisi: ortada donmuş model çekirdeği vardır ve modelin ne kadar bildiğini temsil eder; etrafını dört harness parçası sarar ve modelin ne yapabildiğini belirler; üstte skills yani koşullu yüklenen uzman talimatı, solda rules yani her oturumda daima bağlama giren on iki kalıcı kural dosyası, sağda hook'lar yani modelin değil kabuğun deterministik çalıştırdığı betikler, altta subagent'lar yani ana bağlamı yakmadan işi izole bir pencerede yaptıran alt-ajanlar; rules ve skills modele metin enjekte eder yani yönlendirir öğretmez, hook'lar ise modelden bağımsız kod çalıştırır yani zorlar; sonuç olarak model donmuş satıcının malıdır ama harness kullanıcı tarafından kurulur">
  <defs>
    <marker id="hm-1a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Harness anatomisi — model donmuş, harness senin kurgun</text>
  <text x="330" y="44" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">model ≈ ne kadar bilir · harness ≈ ne yapabilir</text>

  <rect x="230" y="62" width="200" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="330" y="82" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">SKILLS — koşullu talimat</text>
  <text x="330" y="99" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">yalnız ilgili işte bağlama yüklenir (§2)</text>

  <rect x="24" y="176" width="196" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="122" y="198" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">RULES — daima bağlamda</text>
  <text x="122" y="214" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">~/.claude/rules/ · bu kurulumda 12 dosya</text>
  <text x="122" y="227" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">"her zaman şöyle davran"</text>

  <rect x="440" y="176" width="196" height="56" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="538" y="198" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">HOOK'LAR — kabuk koşar</text>
  <text x="538" y="214" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">model değil harness çalıştırır (§4)</text>
  <text x="538" y="227" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">deterministik — atlanamaz</text>

  <rect x="230" y="294" width="200" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="330" y="314" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">SUBAGENT'LAR — izole bağlam</text>
  <text x="330" y="331" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">işi devret, ~200 token özet dön (§3)</text>

  <rect x="252" y="168" width="156" height="72" rx="14" fill="#EEF0FF" stroke="#4F46E5" stroke-width="2.2"/>
  <text x="330" y="196" text-anchor="middle" font-size="12.5" font-weight="700" fill="#4F46E5" font-family="system-ui">MODEL</text>
  <text x="330" y="213" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">donmuş çekirdek</text>
  <text x="330" y="227" text-anchor="middle" font-size="8.5" font-weight="600" fill="#B3402F" font-family="system-ui">satıcının · salt-okunur</text>

  <line x1="330" y1="114" x2="330" y2="166" stroke="#5D7789" stroke-width="1.6" marker-end="url(#hm-1a)"/>
  <line x1="330" y1="240" x2="330" y2="292" stroke="#5D7789" stroke-width="1.6" marker-end="url(#hm-1a)"/>
  <line x1="220" y1="204" x2="250" y2="204" stroke="#5D7789" stroke-width="1.6" marker-end="url(#hm-1a)"/>
  <line x1="440" y1="204" x2="410" y2="204" stroke="#5D7789" stroke-width="1.6" marker-end="url(#hm-1a)"/>

  <text x="330" y="362" text-anchor="middle" font-size="9.5" font-style="italic" fill="#5D7789" font-family="system-ui">rules/skills = metin enjekte eder (yönlendirir) · hook'lar = kod koşar (zorlar)</text>
</svg>
```

### 2. Skills — koşullu yüklenen uzman talimatı
Bir **skill** (beceri), `SKILL.md` adlı bir dosyadır (kendi klasöründe). En tepesinde
**frontmatter** (dosyanın en başında `---` çizgileri arasında duran künye bilgisi; ad, açıklama
gibi alanlar), altında düz talimat metni bulunur:

```
---
description: Create git commits with user approval and no Claude attribution
---
# Commit Changes
1. Think about what changed ...
```

Mekaniğin özü **koşulluluktur**: bu talimat metni **daima** bağlamda değildir (rules öyledir); sadece
**işe uygunken** modelin bağlamına yüklenir. Yani skill, [`model-ve-caba.md`](model-ve-caba.md) §2'nin
dilinde **doğru yönlendirmeyi (steering) doğru anda enjekte etme** mekanizmasıdır — modele hiçbir şey
*öğretmez* (ağırlık değişmez), o istekte önüne uzman bir prosedür *koyar*.

Peki "işe uygun" olduğuna kim, nasıl karar verir? İki katman:

- **`description` alanı (model tarafı):** frontmatter'daki tek cümle, modelin skill'i ne zaman
  çağıracağını anlatan tetikleyicidir. Model bu cümleyi okuyup uygun gördüğünde skill'i açar.
- **`skill-rules.json` + hook (harness tarafı):** salt modelin sezgisine bırakılmaz. Bu kurulumda
  her prompt'ta `skill-activation-prompt.sh` hook'u çalışır (§4), prompt metnini `skill-rules.json`
  içindeki **anahtar kelime** (keyword) listeleri ve **regex** (düzenli ifade — metin içinde kalıp
  arayan küçük bir arama dili) niyet kalıplarıyla eşleştirir ve bir *"SKILL ACTIVATION CHECK"*
  hatırlatması basar. Kayıttaki `enforcement` alanı `suggest` (öner) ya da `block`
  (zorla) olabilir. **Bu metni üreten oturumda tam olarak bu oldu:** ilk mesajda hook, `create_handoff`
  / `continuity_ledger` / `test-driven-development` skill'lerini "kritik" diye önümüze koydu.

Yani skill tetikleme, saf model kararı değil; **model sezgisi + deterministik kural eşleşmesi**nin
birleşimidir — guide (öneri) ile sensor (zorlama) arasında ayarlanabilir bir kadran (§5). İnce nokta:
skill *yüklenince* onun talimatı da o turun geri kalanında **bağlamda kalır** — tıpkı okunan bir dosya
gibi girdinin parçası olur ([`model-ve-caba.md`](model-ve-caba.md) §3'ün "düşünme token'ları bağlamda
kalır" mekaniğinin aynısı).

### 3. Subagent'lar — izole bağlam penceresi
Bir **subagent** (alt-ajan), ana konuşmanın bağlamından **ayrı, temiz bir pencerede** çalışan ikinci
bir ajandır. Bu kurulumda tipleri `~/.claude/agents/*.md` dosyalarında tanımlıdır (frontmatter'da ad,
açıklama, erişebileceği araçlar, hatta model/effort). Ana ajan `Task`/`Agent` aracıyla birini
başlatır; alt-ajan **kendi bağlamını kurar** (dosyaları kendi okur, düzenler, testi kendi koşar) ve
işi bitince ana ajana **kısa bir özet** döner — tipik olarak ~200 **token** (metnin modele
gönderilirken bölündüğü küçük parçalar; kabaca 200 token ≈ yarım sayfa metin).

Mekaniğin değeri iki yönlüdür:

- **Bağlam ekonomisi.** Ana pencereye 2000 token'lık dosya okuması sığdırmak yerine, o yükü alt-ajanın
  penceresinde bırakır, ana pencere sadece özeti alır. `agent-orchestration.md` kuralının mekaniği
  budur: "ajanlar kendi bağlamını okur; anlamak için ana sohbette dosya okuma — görevi ver, o çözer."
  Maliyeti de var: kurulum yükü olduğundan **tek satırlık düzeltme** için subagent açmak zarardır.
- **Ajan okunabilirliği (agent legibility).** Hafta 1 (gün-3) dersi: *"ajanın çalışırken erişebildiği
  yer bağlamdır; orada göremediği bilgi fiilen yoktur."* Subagent için bu çifter geçerlidir: alt-ajan
  yalnız **kendisine verdiğin** talimatı görür, ana ajan yalnız **dönen özeti** görür. Aradaki her şey
  kayıptır. `explicit-identity.md` kuralı ("session_id'yi sınırlar arası açıkça geçir; 'en son' bir
  yarış koşuludur") tam bu sınır yüzünden vardır — bu deponun [`ajan-sandbox-izolasyonu.md`](ajan-sandbox-izolasyonu.md)
  kaynağı da aynı izolasyon fikrinin güvenlik yüzüdür.

### 4. Hook'lar — deterministik kabuk
Skills ve subagent'lar **modele** iş yaptırır; hook'lar ise **modelden bağımsız**, belirli
olaylarda kabuğun (shell) koşturduğu betiklerdir. Kayıt `settings.json` içindedir:

```
hooks → <OlayAdı> → [ { matcher?, hooks: [ { type:"command", command, timeout? } ] } ]
```

Bu kurulumda kayıtlı olay tipleri: **UserPromptSubmit** (prompt işlenmeden önce — bağlam enjekte
edebilir veya bloke edebilir), **PostToolUse** (araç çalıştıktan sonra), **PreCompact** (bağlam
sıkıştırmadan önce), **SessionStart** (oturum başında/devamında), **Stop** (ajan işi bitirince),
**SubagentStop** (alt-ajan bitince). Olay setinde ayrıca **PreToolUse** (araçtan önce — bloke
edebilir), **SessionEnd** (oturum kapanınca) ve **Notification** (araç bir bildirim ürettiğinde)
vardır; bu kurulumda hepsi kullanılmıyor. *(Olay listesi ürünle birlikte değişir — güncel liste
için Claude Code dokümanına bak; ezberlenecek şey liste değil, "olay → betik" deseninin
kendisidir.)* Desen `hooks.md` kuralında sabittir: **kısa bir shell sarmalayıcı → `npx tsx` (TypeScript)
ya da `python3`** işleyiciye borular. Kilit nitelik şudur: **karar modelin değil, harness'ındır** —
hook her seferinde, model "gerek yok" dese bile çalışır. Guide/sensor ayrımının **sensor** tarafı budur.

Bunun soyut olmadığının kanıtı: **bu metni üreten oturum**, sen hiçbir şey yapmadan şu hook'ları
çalıştırdı —

- **SessionStart → `session-start-continuity.sh`:** oturum açılışında `superpowers` skill talimatını
  ve devamlılık defteri bağlamını yükledi.
- **UserPromptSubmit → `skill-activation-prompt.sh`** (skill tetik kontrolü, §2) **+
  `smart-context-inject.py`** (AwesomeDesign tasarım standardı ve Firecrawl/Codex hatırlatmalarını
  bağlama enjekte etti). Sen bunları istemedin; harness deterministik olarak koydu.
- **Stop → `codex-review-stop.sh`:** iş bitince değişen dosyalara karşıt (adversarial) Codex incelemesi
  koşturmaya hazır bekliyor.

## Diyagram 2 — Bir isteğin harness'tan geçişi (bu oturumun kendi akışı)
Bir prompt modele "çıplak" ulaşmaz: önce hook kapıları, sonra model muhakemesi, gerekirse izole
subagent, ardından araç + PostToolUse hook, en sonda Stop hook. Aşağıdaki akış bu oturumda fiilen
işledi.

```svg
<svg viewBox="0 0 660 320" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bir isteğin harness'tan geçişi: kullanıcı prompt'u önce UserPromptSubmit hook kapısından geçer, burada skill-activation-prompt betiği skill tetiklerini kontrol eder ve smart-context-inject betiği tasarım standardı ve araç hatırlatmalarını bağlama enjekte eder; sonra model muhakeme eder; gerekirse iş izole bağlamlı bir subagent'a devredilir ve yaklaşık iki yüz token özet geri döner; ardından model araç kullanır yani dosya okur veya düzenler ve her araçtan sonra PostToolUse hook çalışır; iş bitince Stop hook karşıt Codex incelemesini koşturur; kilit nokta prompt modele çıplak ulaşmaz ve hook kapıları model gerek yok dese bile deterministik olarak çalışır">
  <defs>
    <marker id="hm-2a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="hm-2b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İstek modele "çıplak" ulaşmaz — hook kapıları arada</text>
  <text x="330" y="41" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">Amber = hook (deterministik, kabuk koşar) · mavi = model işi</text>

  <rect x="20" y="58" width="120" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="80" y="78" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Prompt</text>
  <text x="80" y="94" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">kullanıcı mesajı</text>
  <line x1="140" y1="80" x2="168" y2="80" stroke="#5D7789" stroke-width="1.6" marker-end="url(#hm-2a)"/>

  <rect x="170" y="52" width="200" height="56" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.7"/>
  <text x="270" y="70" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">HOOK · UserPromptSubmit</text>
  <text x="270" y="85" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">skill-activation-prompt.sh → skill tetik kontrolü</text>
  <text x="270" y="99" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">smart-context-inject.py → standart/araç hatırlat</text>
  <line x1="370" y1="80" x2="398" y2="80" stroke="#5D7789" stroke-width="1.6" marker-end="url(#hm-2a)"/>

  <rect x="400" y="52" width="240" height="56" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.8"/>
  <text x="520" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#4F46E5" font-family="system-ui">MODEL muhakeme</text>
  <text x="520" y="88" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">artık zenginleştirilmiş bağlamla düşünür</text>
  <text x="520" y="101" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">skill'ler yüklendi, standartlar bağlamda</text>
  <line x1="520" y1="108" x2="520" y2="132" stroke="#186399" stroke-width="1.6" marker-end="url(#hm-2b)"/>

  <rect x="360" y="134" width="280" height="50" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="500" y="153" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">(gerekirse) SUBAGENT — izole bağlam</text>
  <text x="500" y="169" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">işi devret · yükü kendi penceresinde bırak · ~200 tok özet dön</text>
  <line x1="360" y1="159" x2="300" y2="159" stroke="#5D7789" stroke-width="1.6" marker-end="url(#hm-2a)"/>

  <rect x="60" y="134" width="240" height="50" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="180" y="153" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Araç kullan (Read / Edit / Bash)</text>
  <text x="180" y="169" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">her araçtan sonra ↓</text>
  <line x1="180" y1="184" x2="180" y2="208" stroke="#25804C" stroke-width="1.6" marker-end="url(#hm-2b)"/>

  <rect x="60" y="210" width="240" height="44" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.7"/>
  <text x="180" y="229" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">HOOK · PostToolUse</text>
  <text x="180" y="245" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">araç izlenir / kaydedilir</text>

  <line x1="300" y1="232" x2="360" y2="232" stroke="#5D7789" stroke-width="1.6" marker-end="url(#hm-2a)"/>
  <rect x="362" y="210" width="278" height="44" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.7"/>
  <text x="501" y="229" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">HOOK · Stop → codex-review</text>
  <text x="501" y="245" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">iş bitince karşıt (adversarial) inceleme</text>

  <rect x="60" y="278" width="580" height="28" rx="8" fill="#1C3247"/>
  <text x="350" y="297" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Kilit: hook kapıları model "gerek yok" dese bile çalışır — sensor deterministiktir</text>
</svg>
```

### 5. Guide vs Sensor — dördünü tek çerçevede
Önce bir uyarı, çünkü aynı iki kelime iki farklı eksende kullanılıyor:

> **İki eksen var, karıştırma.**
> **(a) Zaman ekseni** — kaynak yazının ([`harness-engineering-openai.md`](harness-engineering-openai.md) §2)
> ayrımı budur: **guide işten ÖNCE** devreye girer ve "ne yapılacağını" söyler (ileri besleme);
> **sensor işten SONRA** devreye girer ve sonucu **ölçer** (geri besleme).
> **(b) Bağlayıcılık ekseni** — bu bölümün kullandığı ayrım: **guide önerir** (model atlayabilir),
> **sensor zorlar** (model atlayamaz).
> İkisi çoğu zaman örtüşür: bir kural dosyası hem önce gelir hem atlanabilir; bir test hem sonra
> koşar hem atlanamaz. Ama **her zaman değil**: `UserPromptSubmit` hook'u işten *önce* koşar (zaman
> ekseninde guide tarafı) ama deterministiktir (bağlayıcılık ekseninde sensor tarafı). Bu yüzden
> bir harness parçasını sınıflarken **iki soruyu ayrı sor:** *ne zaman koşuyor?* ve *atlanabilir mi?*

Hafta 1'in çerçevesi harness parçalarını bağlayıcılığa göre iki sınıfa böler:

- **Guide (yönlendirir):** modele **metin** verir; model okur, uyabilir de **atlayabilir** de.
  Buraya **rules, skills, AGENTS.md/CLAUDE.md** girer. Esnektir; ama yalnız guide'a güvenmek, modelin
  uzun bağlamda bir kuralı **unutmasına/atlamasına** açıktır.
- **Sensor (zorlar):** modelden **bağımsız** deterministik kontrol; model atlayamaz. Buraya
  **hook'lar** ve kod tarafında **testler / ArchUnit / tip denetimi** girer. Katıdır; ama tek başına
  sensor esnekliği öldürür — her nüansı önceden betikleyemezsin.

Sağlam harness **ikisinin bileşimidir**: guide niyeti söyler, sensor kritik olanı garanti eder. Bu
deponun kendisi bunu iki düzlemde birden yapar — hem *ajan* harness'ında (rules = guide,
`codex-review-stop.sh` = sensor), hem *uygulama* mimarisinde (AGENTS.md = guide, **ArchUnit**
(Java'da mimari kuralları test olarak yazmayı sağlayan kütüphane) ile yazılmış **`MimariTestleri`**
= sensor: katman yönünü model değil, derleme zorlar).

## Diyagram 3 — Guide × Sensor: dört parça nereye düşer
Sol sütun yönlendirir (model okur, uyabilir/atlayabilir); sağ sütun zorlar (deterministik, atlanamaz).
Harness'ı sağlam yapan, ikisinin birlikte kurulmasıdır.

```svg
<svg viewBox="0 0 660 360" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Guide ve sensor ekseni: sol sütun guide yani yönlendirir, model metni okur ve uyabilir de atlayabilir de; buraya rules ve AGENTS.md yani kalıcı kurallar ve harita ile skills yani koşullu uzman talimatı düşer; sağ sütun sensor yani zorlar, model atlayamaz ve deterministiktir; buraya hook'lar yani kabuğun koşturduğu betikler ile testler ve ArchUnit ve tip denetimi düşer; ortadaki uyarı yalnız guide'a güvenmek modelin kuralı unutmasına açıktır ve yalnız sensor esnekliği öldürür; sonuç sağlam harness guide ile sensor'un birleşimidir, niyeti guide söyler kritik olanı sensor garanti eder">
  <defs>
    <marker id="hm-3a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Guide × Sensor — harness parçaları nereye düşer</text>
  <text x="330" y="44" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">niyeti guide söyler · kritik olanı sensor garanti eder</text>

  <rect x="30" y="62" width="290" height="34" rx="9" fill="#25804C"/>
  <text x="175" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">GUIDE — yönlendirir (atlanabilir)</text>
  <rect x="340" y="62" width="290" height="34" rx="9" fill="#C77A17"/>
  <text x="485" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">SENSOR — zorlar (atlanamaz)</text>

  <rect x="30" y="106" width="290" height="58" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="175" y="128" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">RULES + AGENTS.md / CLAUDE.md</text>
  <text x="175" y="144" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">daima bağlamda · kalıcı yönerge + harita</text>
  <text x="175" y="157" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">model okur — uyabilir/unutabilir</text>

  <rect x="30" y="172" width="290" height="58" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="175" y="194" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">SKILLS</text>
  <text x="175" y="210" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">koşullu uzman talimatı</text>
  <text x="175" y="223" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">skill-rules eşleşmesi öneri/zorlama ayarlı</text>

  <rect x="340" y="106" width="290" height="58" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="485" y="128" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">HOOK'LAR</text>
  <text x="485" y="144" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">settings.json'da kayıtlı · kabuk koşar</text>
  <text x="485" y="157" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">model "gerek yok" dese bile çalışır</text>

  <rect x="340" y="172" width="290" height="58" rx="10" fill="#FBEDEA" stroke="#B3402F" stroke-width="1.6"/>
  <text x="485" y="194" text-anchor="middle" font-size="11" font-weight="700" fill="#B3402F" font-family="system-ui">TESTLER · ArchUnit · tip denetimi</text>
  <text x="485" y="210" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">kod tarafı sensor — derleme zorlar</text>
  <text x="485" y="223" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">bu depoda: MimariTestleri katman yönü</text>

  <rect x="90" y="252" width="480" height="50" rx="10" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.4" stroke-dasharray="5 4"/>
  <text x="330" y="272" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">Tek başına GUIDE → model unutur/atlar · Tek başına SENSOR → esneklik ölür</text>
  <text x="330" y="290" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">yalnız birini kurmak harness'ı topal bırakır</text>

  <rect x="60" y="316" width="540" height="30" rx="8" fill="#1C3247"/>
  <text x="330" y="336" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">Sağlam harness = GUIDE + SENSOR — biri niyeti söyler, öteki garanti eder</text>
</svg>
```

### 6. Model–harness ortak evrimi — döngü ve aşırı uyum riski
Buraya kadar model ile harness'ı iki ayrı kutu gibi anlattık: model donmuş, harness senin. Bu
günlük kullanım için doğrudur; ama **üretim tarafında** iki kutu sanıldığı kadar bağımsız değildir.
Bugünün ajan ürünleri (Claude Code, Codex) **post-training** (ön-eğitimden sonra, modeli belirli
davranışlara yöneltmek için yapılan ek eğitim) aşamasında **harness döngüye dahil edilerek**
eğitilir: model, eğitim sırasında tam da o harness'ın araçlarıyla — dosya sistemi işlemleri, bash
çalıştırma, planlama, subagent'a iş devretme — çalıştırılır ve bunlarda iyileşmesi hedeflenir.

Bu bir **geri besleme döngüsü** kurar: sahada işe yarayan bir harness parçası keşfedilir → harness'a
eklenir → bir sonraki model o parçayla eğitilir → model o harness'ın içinde daha yetenekli hâle
gelir → döngü tekrar eder. [`model-ve-caba.md`](model-ve-caba.md)'nin "ağırlıklar eğitimde yazılır"
cümlesiyle çelişki yok: bağlanma **donmadan önce**, eğitim sırasında olur; sana ulaşan model yine
salt-okunurdur — ama içine, belirli bir harness'ın alışkanlıkları gömülmüş olarak gelir.

Bunun sana bakan iki sonucu var:

- **Aşırı uyum (overfitting — modelin eğitildiği koşula fazla bağlanıp koşul değişince
  bocalaması).** Örnek: OpenAI'ın kendi prompt kılavuzu, Codex'in dosya düzenlemede alıştığı
  `apply_patch` aracının mantığı değiştirilince model performansının düştüğünü söyler. "Gerçekten
  zeki" bir model için yama biçimi değiştirmek önemsiz olmalıydı; harness'la birlikte eğitim bu
  bağımlılığı yaratır. Ders: bir modelin araç tanımlarını değiştirirken **eğitildiği kalıba yakın
  kalmak** genelde bedavaya performans kazandırır.
- **"Eğitildiği harness = senin için en iyi harness" DEĞİLDİR.** Aynı model, farklı harness'larda
  çok farklı sonuç verebilir: Terminal Bench gibi ajan kıyaslamalarında (benchmark — modelleri aynı
  görev setinde yarıştıran ölçüm) aynı modelin kendi ürün harness'ındaki skoru, başka harness'lardaki
  skorunun belirgin altında kalabiliyor. Yani denklemdeki ikinci terim gerçek bir serbestlik
  derecesidir: **modeli değiştirmeden, yalnız harness'ı değiştirerek** kayda değer fark
  yaratılabilir. (Bu tür "sadece harness'ı değiştirdik, sıralamada şu kadar yükseldik" iddialarının
  çoğu, kendi harness kütüphanesini satan firmalardan gelir — §8'deki şerh refleksi burada da
  geçerli: iddianın yönü makul, sayısı satıcı beyanıdır.)

### 7. Harness'ın geleceği — eriyecek mi, kalacak mı?
Madem model her nesilde daha yetenekli, harness'taki parçaların bir kısmı zamanla **modelin içine
emilecek**: planlama, öz-doğrulama, uzun işte tutarlılık gibi bugün bağlam enjeksiyonuyla
desteklenen davranışlar giderek daha az dış destek isteyecek. Bu, "harness zamanla önemsizleşir"
sonucunu akla getirir.

O sonuç **yanlış çıkarımdır** — iki gerekçeyle:

- **Harness yalnız eksik yamamaz; ortam kurar.** Bugünkü harness'ın bir kısmı gerçekten model
  zaafını telafi eder (ve o kısım eriyebilir). Ama iyi kurulmuş ortam — doğru araçlar, kalıcı durum
  (dosya sistemi, git), doğrulama döngüleri (test, hook) — **her seviyedeki modeli** daha etkili
  yapar. §5'in diliyle: model ne kadar zekileşirse zekileşsin **sensor** ihtiyacı kaybolmaz, çünkü
  sensor'un işi modelin zekâsına güvenmemektir. Prompt mühendisliği nasıl "modeller anlayışlı oldu"
  diye ölmediyse, harness mühendisliği de ölmez; **seviye değiştirir**.
- **Sınır yukarı taşınır, yok olmaz.** Bugün araştırılan sorular bile harness sorusu: yüzlerce
  ajanın aynı depoda paralel çalışmasını kim düzenler? Ajan kendi **iz kayıtlarını** (trace —
  oturumun adım adım kaydı) analiz edip harness'taki arıza kalıplarını kendisi bulabilir mi? Araçlar
  ve bağlam, önceden sabitlenmek yerine işe göre **tam zamanında** derlenebilir mi? Bunların hiçbiri
  "model yeterince zekileşince" kendiliğinden çözülmez; hepsi ortam tasarımıdır.

Senin için pratik çeviri: bu depoda kurduğun hiçbir şey — rules, hook'lar, ArchUnit, devamlılık
defterleri — "model düzelince çöpe gidecek" geçici iskele değildir. Model yükseldikçe bazı *guide*
metinlerin gereksizleşebilir (ölç-ve-buda, [`harness-engineering-openai.md`](harness-engineering-openai.md)
§4); *sensor* katmanın ve ortam kurgun kalır.

### 8. ⚠️ ZORUNLU ŞERH — satıcı-birincil kaynak + kurulum tercihi ayrımı
Bu metin iki tür bilgiyi karıştırmamalı; okurken ikisini ayır:

- **Mekanik anlatım güvenilirdir.** "SKILL.md frontmatter'ı", "hook olay tipleri", "subagent'ın izole
  bağlamı", "settings.json kayıt şekli" — bunlar Anthropic'in **kendi ürününün nasıl çalıştığına** dair
  doğrulanabilir bilgidir; satıcının kendi iç mekaniği hakkında yanlış söylemesi için sebep azdır. Aynı
  refleks [`model-ve-caba.md`](model-ve-caba.md) §5'te vardı: **mekanik anlatım ≠ performans iddiası.**
- **"En iyi pratik" tavsiyeleri satıcı yönergesidir.** "Kuralları kısa tut", "tek satırlık işe subagent
  açma", "skill'i şu şekilde tetikle" gibi öneriler bağımsız ölçüm değil, üreticinin (ve bu kurulumda
  **senin kendi**) tercihidir. Doğrulaması **telemetriyledir** (telemetry — sistemin çalışırken
  kendisi hakkında otomatik tuttuğu ölçüm kaydı; kimsenin beyanı değil, makinenin sayısı):
  kural sayısını ölç (`wc -l` +
  [`harness-engineering-openai.md`](harness-engineering-openai.md) §4'ün "ölç-ve-buda" laboratuvarı),
  hook'ların gerçekten ateşlenip ateşlenmediğini **çıktılardan** doğrula (`observe-before-editing`
  kuralı: "çıktılar yalan söylemez, kod söyleyebilir").
- **İkinci ayrım — çekirdek ≠ senin katmanın.** Yukarıdaki "12 rule", "`codex-review-stop.sh`",
  `skill-rules.json` **Claude Code'un zorunlu parçası değildir**; bu deponun sahibinin kurduğu **özel
  harness**tır. Claude Code sana *olay tiplerini ve dosya biçimini* verir (mekanik); onlarla **ne**
  kuracağın senin mühendislik kararındır. Bu ayrım kritik: mekaniği satıcıdan öğren, kurgunun
  değerini **kendi çıktından** ölç.

> **Pratik sonuç:** "bu hook/skill işe yarıyor mu" sorusunun cevabını dokümandan değil, **kendi
> çalışmandan** al — bu oturumun kendisi kanıt üretti (Diyagram 2). Satıcının şeması *nasıl çalıştığını*
> söyler; senin gözlemin *sende işe yarayıp yaramadığını* söyler.

### 9. Bu projedeki bağ
Bu çerçeve senin için soyut değil, çünkü **kurulumun bu desenin ta kendisi.** Üç düzlemde bağlanır:

1. **Ajan harness'ın (bu depoyu sürdüğün araç):** 12 kural (guide) + `agents/` subagent tipleri +
   `settings.json` hook'ları (sensor). "Agent = Model + Harness" denkleminin senin makinendeki hali.
2. **Uygulamanın (AI Öğretmen) kendi harness'ı:** LLM'i `ai/` paketine hapseden ArchUnit
   `MimariTestleri` = sensor; `prompts/` dizininden gelen şablonlar + `AGENTS.md` = guide. Yani
   yazdığın uygulama, seni kodlarken kuşatan harness'ın **aynı desenini** kendi içinde uygular —
   [`ajan-sandbox-izolasyonu.md`](ajan-sandbox-izolasyonu.md) bu izolasyonun güvenlik yüzüdür.
3. **Öğretim bağı:** Hafta 1 "harness mühendisliği"ni ilke olarak, Hafta 3 "ajan yönetimi"ni akış
   olarak öğretir; bu metin ikisinin altındaki **çalışan mekaniği** verir. Sokratik AI Öğretmen'ini
   kurarken de aynı sorular geçerli: hangi talimat *daima* (rule), hangisi *koşullu* (skill), hangi
   iş *izole* edilmeli (subagent), hangi kural *deterministik zorlanmalı* (hook/test)?

## Öz-sınav (kendini test et)
1. "Agent = Model + Harness" denkleminde neyi seçemezsin, neyi kurarsın? Model donmuşsa günlük işte
   davranışı asıl **ne** şekillendirir — ve bu neden [`model-ve-caba.md`](model-ve-caba.md) §2'nin
   "ağırlıklar salt-okunur" cümlesinin doğrudan sonucudur?
2. **Rule ile skill farkı:** ikisi de modele metin enjekte eder; ayrım nerede? Hangisi *daima*
   bağlamda, hangisi *koşullu*? Bir skill modele bir şey **öğretir mi** — "steering ≠ teaching"
   ayrımıyla açıkla.
3. Bir skill'in `SKILL.md` frontmatter'ındaki `description` alanı ne işe yarar? Tetikleme yalnız
   modelin sezgisine mi bırakılır — `skill-rules.json` + `skill-activation-prompt.sh` bu işe nasıl
   girer, ve `enforcement: suggest` ile `block` farkı nedir?
4. Bu metni üreten oturumda ilk mesajda hangi mekanizma hangi skill'leri önümüze koydu? Bu, saf model
   kararı mı yoksa "model sezgisi + deterministik eşleşme" birleşimi mi — cevabını guide/sensor
   kadranına yerleştir.
5. **Subagent neden bağlam ekonomisidir?** 2000 token'lık dosya okuması nereye gider, ana pencereye ne
   döner (~kaç token)? `agent-orchestration.md` kuralı neden "ana sohbette dosya okuma, görevi ver"
   der? Tek satırlık düzeltmede subagent açmak neden zarardır?
6. "Ajan okunabilirliği" ilkesini (Hafta 1, gün-3) subagent'a uygula: alt-ajan **neyi** görür, ana
   ajan **neyi** görür, aradaki her şeye ne olur? `explicit-identity.md` kuralı ("en son bir yarış
   koşuludur") bu sınır yüzünden neden vardır?
7. Bir hook'u kaydettiğin dosya hangisidir ve şema nedir (`hooks → Olay → ...`)? Şu olayları ne zaman
   ateşlendiğine göre eşle: **UserPromptSubmit, PostToolUse, PreCompact, SessionStart, Stop**. "Karar
   modelin değil harness'ındır" cümlesi hook'u skill'den nasıl ayırır?
8. Bu oturumun kendisinden **üç** canlı hook örneği ver (hangi olay → hangi betik → ne yaptı). Sen
   istemediğin hâlde çalışmaları neden guide değil **sensor** olduklarını kanıtlar?
9. **Guide vs sensor:** rules, skills, hooks, ArchUnit `MimariTestleri` — her birini doğru sütuna koy.
   "Yalnız guide" ve "yalnız sensor" kurmanın ayrı ayrı arızası nedir? Sağlam harness neden ikisinin
   *bileşimi*dir?
10. **İki eksen sorusu:** guide/sensor ayrımının **zaman** ekseni (işten önce / işten sonra) ile
    **bağlayıcılık** ekseni (önerir / zorlar) nerede örtüşür, nerede ayrışır? `UserPromptSubmit`
    hook'unu her iki eksende ayrı ayrı yerleştir ve neden tek kelimeyle etiketlenemediğini açıkla.
11. **Şerh sorusu:** "hook olay tipleri" ile "kuralları kısa tut önerisi" — hangisi güvenilir mekanik,
    hangisi satıcı/kurulum tercihi? Bu kurulumdaki 12 rule ve `codex-review-stop.sh`, Claude Code'un
    **zorunlu** parçası mı yoksa senin **özel harness'ın** mı — ve bu ayrım neden önemli
    ([`ai-verimlilik-olcumu.md`](ai-verimlilik-olcumu.md) refleksiyle)?
12. **Ortak evrim sorusu:** post-training'de harness'ın döngüye dahil edilmesi hangi geri besleme
    döngüsünü kurar? Bu, "ağırlıklar salt-okunur" cümlesiyle neden çelişmez? `apply_patch` örneği
    üzerinden **aşırı uyumu** açıkla — ve "modelin eğitildiği harness = senin için en iyi harness"
    çıkarımı neden yanlıştır?
13. **Gelecek sorusu:** "Modeller zekileştikçe harness önemsizleşir" iddiasına iki karşı gerekçe ver.
    Model yükseldikçe harness'ın hangi katmanı incelir (guide mı sensor mu), hangisi kalır — ve bu
    depodaki kurulumundan hangi parçalar hangi tarafa düşer?

## Kaynaklar
- **Anthropic — Claude Code dokümanı** (hooks, subagents, skills, settings referansı; Tem 2026'da
  erişilen mekanik) — *şerh: mekanik anlatım güvenilir; "en iyi pratik" önerileri satıcı/kurulum
  tercihidir, kendi telemetrinle doğrula (§8).*
- **Bu deponun kendi kurulumu** (`~/.claude/rules/` 12 dosya, `agents/`, `settings.json` hook'ları,
  `skills/skill-rules.json`) ve **bu metni üreten oturumun gözlemlenen akışı** — birincil, doğrulanmış
  vaka (Diyagram 2).
- **LangChain blog — "The Anatomy of an Agent Harness" (Vivek Trivedy, Mar 2026)** — §6–§7'nin
  kaynağı: ortak evrim döngüsü, Terminal Bench gözlemi, harness'ın geleceği ve açık problemler —
  *şerh: LangChain kendi harness kütüphanesini (deepagents) ve platformunu (LangSmith) satan
  taraftır; kavramsal çerçevesi sağlam, "sadece harness'la Top 30 → Top 5" gibi sayısal iddiaları
  bağımsız doğrulanmamış satıcı beyanıdır (§8 refleksi).*

### Bu programdaki ilgili kaynaklar
- [`model-ve-caba.md`](model-ve-caba.md) — modelin **içi** (inference); bu metin modelin **etrafı**
  (harness). "Steering ≠ teaching" ve "bağlamda kalır" mekaniklerini oradan alır.
- [`harness-engineering-openai.md`](harness-engineering-openai.md) — harness'ın **ilke/kültür** düzeyi:
  AGENTS.md haritası, guide/sensor, "ölç-ve-buda" laboratuvarı (§8'in telemetri tarafı).
- [`ajan-sandbox-izolasyonu.md`](ajan-sandbox-izolasyonu.md) — subagent izolasyonunun **güvenlik** yüzü.
- [`ai-verimlilik-olcumu.md`](ai-verimlilik-olcumu.md) — satıcı iddiası okuma refleksi; §8 şerhin ölçüm
  tarafı.
