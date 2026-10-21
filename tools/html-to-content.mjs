#!/usr/bin/env node
/**
 * Tek seferlik müfredat dönüştürücü.
 * mimar-programi_1.html içindeki `const PROGRAM = [...]` dizisini okur ve
 * content/ altına frontmatter'lı markdown üretir (format: CLAUDE_CODE_BRIEF.md §5).
 *
 * Kullanım: node tools/html-to-content.mjs [html-yolu] [cikti-dizini]
 */
import { readFileSync, writeFileSync, mkdirSync, rmSync, existsSync } from "node:fs";
import { join, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const root = join(dirname(fileURLToPath(import.meta.url)), "..");
const htmlPath = process.argv[2] ?? join(root, "mimar-programi_1.html");
const outDir = process.argv[3] ?? join(root, "content");

const html = readFileSync(htmlPath, "utf8");

// PROGRAM dizisini kes: `const PROGRAM = [` ... satır başındaki `];`
const start = html.indexOf("const PROGRAM = [");
if (start === -1) throw new Error("PROGRAM dizisi bulunamadı: " + htmlPath);
const arrStart = html.indexOf("[", start);
const endMarker = html.indexOf("\n];", arrStart);
if (endMarker === -1) throw new Error("PROGRAM dizisinin kapanışı (\\n];) bulunamadı");
const arrSource = html.slice(arrStart, endMarker + 2); // "];" nin "]" ını dahil et

// JS dizi literali — güvenli bağlamda değerlendir (dosya bizim, içerik statik)
const program = new Function(`return ${arrSource};`)();
if (!Array.isArray(program) || program.length !== 12)
  throw new Error(`Beklenen 12 hafta, bulunan: ${program?.length}`);

// --- yardımcılar ---
const stripHtml = (s) =>
  String(s ?? "")
    .replace(/<br\s*\/?>/gi, "\n")
    .replace(/<[^>]+>/g, "")
    .replace(/&amp;/g, "&").replace(/&lt;/g, "<").replace(/&gt;/g, ">")
    .replace(/&nbsp;/g, " ").replace(/&#39;|&apos;/g, "'").replace(/&quot;/g, '"')
    .trim();

const yamlStr = (s) => `"${String(s).replace(/\\/g, "\\\\").replace(/"/g, '\\"')}"`;
const nn = (n) => String(n).padStart(2, "0");

// İnceleme bulgusu (KRİTİK): content/ altında elle yazılmış kaynaklar/ da yaşıyor —
// tüm dizini silmek geri getirilemez veri kaybı olurdu. Yalnızca bu scriptin
// ÜRETTİĞİ dosyalar silinir; diğer her şey (kaynaklar/ dahil) korunur.
import { readdirSync } from "node:fs";
if (existsSync(outDir)) {
  for (const girdi of readdirSync(outDir, { withFileTypes: true })) {
    const uretilenDizin = girdi.isDirectory() && /^hafta-\d{2}$/.test(girdi.name);
    const uretilenDosya = girdi.isFile() && girdi.name === "program.md";
    if (uretilenDizin || uretilenDosya) {
      rmSync(join(outDir, girdi.name), { recursive: true });
    }
  }
}
mkdirSync(outDir, { recursive: true });

// --- program.md (genel indeks) ---
const indexLines = [
  "---",
  'baslik: "AI Çağı Yazılım Mimarlığı — 12 Haftalık Program"',
  `hafta_sayisi: ${program.length}`,
  "---",
  "",
  "## Hafta indeksi",
  "",
];
program.forEach((h, i) =>
  indexLines.push(`- Hafta ${i + 1}: [${stripHtml(h.title)}](hafta-${nn(i + 1)}/hafta.md) — ${stripHtml(h.goal)}`)
);
writeFileSync(join(outDir, "program.md"), indexLines.join("\n") + "\n");

// --- hafta ve gün dosyaları ---
let gunSayisi = 0, quizSayisi = 0;
program.forEach((h, wi) => {
  const haftaNo = wi + 1;
  const dir = join(outDir, `hafta-${nn(haftaNo)}`);
  mkdirSync(dir, { recursive: true });

  // hafta.md
  const w = [
    "---",
    `hafta: ${haftaNo}`,
    `baslik: ${yamlStr(stripHtml(h.title))}`,
    `hedef: ${yamlStr(stripHtml(h.goal))}`,
    `kaynak: ${yamlStr(stripHtml(h.src))}`,
    "---",
    "",
    "## Quiz",
  ];
  (h.quiz ?? []).forEach((q, qi) => {
    w.push(`### S${qi + 1}: ${stripHtml(q.q)}`);
    q.o.forEach((opt, oi) => w.push(`- [${oi === q.a ? "x" : " "}] ${stripHtml(opt)}`));
    w.push(`> Açıklama: ${stripHtml(q.e)}`, "");
    quizSayisi++;
  });
  writeFileSync(join(dir, "hafta.md"), w.join("\n") + "\n");

  // gun-N.md
  (h.days ?? []).forEach((d, di) => {
    const gunNo = di + 1;
    const g = [
      "---",
      `hafta: ${haftaNo}`,
      `gun: ${gunNo}`,
      `baslik: ${yamlStr(stripHtml(d.t))}`,
      "---",
      "",
      "## Ders",
      stripHtml(d.d),
      "",
      "## Görevler",
      ...(d.tasks ?? []).map((t) => `- [ ] ${stripHtml(t)}`),
    ];
    writeFileSync(join(dir, `gun-${gunNo}.md`), g.join("\n") + "\n");
    gunSayisi++;
  });
});

console.log(`OK: ${program.length} hafta, ${gunSayisi} gün dosyası, ${quizSayisi} quiz sorusu → ${outDir}`);
