/* AI Öğretmen — Program sekmesi. Framework yok; veri kaynağı REST API. */
"use strict";

const durum = {
  ozetler: [],      // GET /api/program
  aktifHafta: 1,    // GÖRÜNTÜLENEN hafta (içerik paneli) — kilitli önizleme burada değişir
  aktifGun: null,   // AI/görev-barı bağlamı: son etkileşilen gün
  acikGun: null,    // accordion GÖRSEL: o an açık gün (yeni haftada null → tüm günler kapalı)
  detay: null,      // GET /api/hafta/{n} — görüntülenen hafta
  // AI ÇALIŞMA bağlamı: yalnız AÇIK haftaya geçince güncellenir; kilitli önizleme DEĞİŞTİRMEZ.
  sohbetHafta: null,
  sohbetGun: null,
  sohbetDetay: null,
  quizSecimleri: {},
  quizBitti: false,
  aiAktif: false,
  kaynaklar: [],    // GET /api/kaynaklar (bir kez yüklenir)
  aktifGorev: null, // {gun, index, metin, tip, kutu, satir, stat, g} — panelde işlenen görev
  okunanGorev: null,// okuma yan-sayfası açık görev bağlamı
  gorevBekleyen: null, // {hafta, gun, index, tip} — bir oku/tartış görevi öğretmen kapısında cevap bekliyorken dolu
  gorunum: "program", // "program" | "atolye" | "mulakat" | "mulakat-tekrar" — orta panelin modu
                      // (atölye + mülakat ayrı görünümler; hafta/kilit mantığı bozulmaz. mülakat: mulakat.js)
  atolye: null,     // GET /api/atolye (bir kez yüklenir, cache)
  atolyeKat: 0,     // seçili atölye kategorisi (sekme) indeksi
  mulakatEtkin: false, // GET /api/ozellikler → mulakat (ADR-0011; varsayılan KAPALI — bölüm/kart gizli)
  site: false,      // GET /api/ozellikler → site (ADR-0012): çok kullanıcılı sitede AI-kapalı metni öğrenciye göre
};

/** AI kapalıyken görev düğmeleri: kutucuk elle işaretlenebildiği için "AI gerekir" DEMEZ. */
const AI_KAPALI_GOREV = "AI öğretmen şu an kapalı — görevi kendin yapıp kutucuğu işaretleyebilirsin.";

// ---------- görev tipi türetici (saf frontend; veriyi değiştirmez) ----------
const GOREV_TIPLERI = {
  oku:    { ikon: "📖", btn: "📖 Oku",     sinif: "tip-oku",    etiket: "Oku" },
  yaz:    { ikon: "✍️", btn: "✍️ Yaz",     sinif: "tip-yaz",    etiket: "Yaz" },
  tartis: { ikon: "💬", btn: "💬 Tartış",  sinif: "tip-tartis", etiket: "Tartış" },
};

/** Görev metninden aksiyon tipini çıkarır. TR küçük harf; eşleşme yoksa 'yaz'. */
function gorevTipiCikar(metin) {
  const m = (metin || "").toLocaleLowerCase("tr");
  const gecer = (arr) => arr.some((k) => m.includes(k));
  if (m.includes("oku")) return "oku";
  if (gecer(["yaz", "listele", "liste", "çıkar", "oluştur", "hazırla", "taslak", "madde", "tablo", "not al"])) return "yaz";
  if (gecer(["tartış", "düşün", "karşılaştır", "analiz", "değerlendir"])) return "tartis";
  return "yaz";
}

// ---------- yardımcılar ----------
/** timeoutMs: AI uçları için uzun (varsayılan 180sn) — asılı kalan istek UI'ı kilitlemesin. */
/** site profili (ADR-0012): Spring Security'nin XSRF-TOKEN çerezi. Yerel modda çerez yoktur → "". */
function csrfToken() {
  const m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  return m ? decodeURIComponent(m[1]) : "";
}

async function api(yol, secenekler = {}, timeoutMs = 180000) {
  const iptal = new AbortController();
  const zamanlayici = setTimeout(() => iptal.abort(), timeoutMs);
  // Durum değiştiren istekler CSRF token'ını başlıkta taşır (site profili; yerelde çerez yok → eklenmez)
  const basliklar = new Headers(secenekler.headers || {});
  const metod = (secenekler.method || "GET").toUpperCase();
  const token = csrfToken();
  if (token && metod !== "GET" && metod !== "HEAD") basliklar.set("X-XSRF-TOKEN", token);
  let yanit;
  try {
    yanit = await fetch(yol, { ...secenekler, headers: basliklar, signal: iptal.signal });
  } catch (h) {
    throw h.name === "AbortError" ? new Error("İstek zaman aşımına uğradı — tekrar dene.") : h;
  } finally {
    clearTimeout(zamanlayici);
  }
  if (yanit.status === 401) {
    // site profili: oturum yok/bitmiş → giriş sayfası (yerel modda 401 oluşmaz)
    location.href = "/giris.html";
    throw new Error("Oturum gerekli — giriş sayfasına yönlendiriliyorsun.");
  }
  if (!yanit.ok) {
    const govde = await yanit.json().catch(() => ({}));
    const hata = new Error(govde.detay || govde.hata || `HTTP ${yanit.status}`);
    hata.status = yanit.status; // çağıran HTTP durumuna göre dallanabilsin (ör. 503 = AI kapalı)
    throw hata;
  }
  return yanit.json();
}

function el(etiket, sinif, icerik) {
  const e = document.createElement(etiket);
  if (sinif) e.className = sinif;
  if (icerik !== undefined) e.textContent = icerik;
  return e;
}

// ---------- lineer ilerleme kilidi (kullanıcı: adım adım) ----------
/** Hafta "tamam" = tüm görevler bitti VE quiz ≥%60 geçildi. */
function haftaTamamMi(h) {
  return h.ilerlemeYuzde >= 100 && h.sonQuizPuan != null && h.sonQuizPuan >= 60;
}
/** Hafta açık = 1. hafta ya da önceki TÜM haftalar tamam. */
function haftaAcikMi(no) {
  if (no === 1) return true;
  return durum.ozetler.filter((h) => h.no < no).every(haftaTamamMi);
}

// ---------- sol: katman haritası ----------
function haritaCiz() {
  const kok = document.getElementById("katman-haritasi");
  kok.innerHTML = "";
  // Hafta 1'den ÖNCE: kilitsiz "Başlangıç Kılavuzu" — diyagramlı program haritası (kaynak drawer'ında açılır)
  const bas0 = el("div", "layer baslangic");
  bas0.append(el("span", "no mono", "📘"), el("span", "nm", "Başlangıç Kılavuzu"),
      el("span", "pct mono", "oku →"));
  bas0.title = "Neden bu eğitim, 12 haftada ne öğreneceksin — önce bunu oku";
  bas0.addEventListener("click", () => kaynakDrawerYukle(
      { slug: "00-baslangic-kilavuzu", baslik: "Başlangıç Kılavuzu — 12 Haftada AI Çağı Yazılım Mimarı" }));
  kok.appendChild(bas0);
  // Prompt Atölyesi — kilitsiz ayrı görünüm (Başlangıç Kılavuzu'nun altında; hafta/kilit mantığına dokunmaz)
  const atolyeRow = el("div", "layer atolye" + (durum.gorunum === "atolye" ? " active" : ""));
  atolyeRow.append(el("span", "no mono", "🧪"), el("span", "nm", "Prompt Atölyesi"),
      el("span", "pct mono", "aç →"));
  atolyeRow.title = "Prompt (istem) yazma tekniklerini deneyerek öğren — kilitsiz laboratuvar";
  atolyeRow.addEventListener("click", atolyeAc);
  kok.appendChild(atolyeRow);
  // kullanıcı tercihi: hafta 1 en üstte, hafta 12 en altta (okuma sırası)
  [...durum.ozetler].sort((a, b) => a.no - b.no).forEach((h) => {
    const acik = haftaAcikMi(h.no);
    const kat = el("div", "layer" + (durum.gorunum === "program" && h.no === durum.aktifHafta ? " active" : "")
        + (h.ilerlemeYuzde >= 100 ? " done" : "") + (acik ? "" : " kilitli"));
    const dolgu = el("div", "fillbar");
    dolgu.style.width = h.ilerlemeYuzde + "%";
    kat.append(dolgu, el("span", "no mono", "H" + String(h.no).padStart(2, "0")),
        el("span", "nm", h.baslik),
        el("span", "pct mono", acik ? (h.ilerlemeYuzde + "%") : "🔒"));
    kat.addEventListener("click", () => haftaSec(h.no)); // TÜM haftalar görüntülenebilir (içerik açık)
    if (!acik) kat.title = "İçeriği görebilirsin; görevler, değerlendirme ve quiz önceki haftayı bitirince açılır";
    kok.appendChild(kat);
  });

  const toplam = durum.ozetler.reduce((t, h) => t + h.toplamGorev, 0);
  const biten = durum.ozetler.reduce((t, h) => t + h.tamamlananGorev, 0);
  document.getElementById("toplam-ilerleme").textContent =
      toplam ? Math.round((100 * biten) / toplam) + "%" : "—";

  if (durum.mulakatEtkin && window.mulakatHaritaCiz) mulakatHaritaCiz(); // mülakat sol-menüsü (mulakat.js) senkron kalsın
}

// ---------- orta: hafta detayı ----------
function haftaCiz() {
  const panel = document.getElementById("hafta-paneli");
  const d = durum.detay;
  const haftaKilitli = !haftaAcikMi(d.no); // açık değilse: içerik GÖRÜNÜR ama görev/quiz/değerlendirme YAPILAMAZ
  panel.innerHTML = "";

  const bas = el("div", "week-head");
  bas.append(el("span", "tag", "Hafta " + d.no), el("h2", null, d.baslik));
  if (durum.aiAktif && !haftaKilitli) { // rapor yalnız açık haftada anlamlı
    const raporBtn = el("button", "btn btn-ikincil", "Haftalık Rapor");
    raporBtn.addEventListener("click", () => raporGoster(d.no, raporBtn));
    bas.appendChild(raporBtn);
  }
  panel.append(bas, el("p", "week-goal", d.hedef), el("p", "week-src mono", d.kaynak));

  if (haftaKilitli) {
    panel.appendChild(el("div", "hafta-kilit-banner",
        "🔒 Bu hafta henüz açık değil. İçeriği ve kaynakları inceleyebilirsin; görevleri, "
        + "değerlendirmeyi ve quiz'i önceki haftayı (%100 görev + quiz ≥%60) bitirince yapabilirsin."));
  }

  const raporAlani = el("div");
  raporAlani.id = "rapor-alani";
  panel.appendChild(raporAlani);

  const aktifGunIdx = d.gunler.findIndex((g) => g.gorevler.some((t) => !t.tamamlandi));
  const gunlerBitti = aktifGunIdx === -1;
  if (haftaKilitli) {
    durum.aktifGun = d.gunler[0] ? d.gunler[0].gun : null; // kilitli önizleme: ilk günü göster (görüntü)
  } else {
    if (aktifGunIdx !== -1) durum.aktifGun = d.gunler[aktifGunIdx].gun; // aktif günü otomatik aç
    durum.sohbetHafta = d.no;          // AI ÇALIŞMA bağlamı = görüntülenen açık hafta
    durum.sohbetGun = durum.aktifGun;
    durum.sohbetDetay = d;
  }
  // Kilitli haftada TÜM günler okunabilir (gün-sırası kilidi uygulanmaz); ama görevler yapılamaz.
  d.gunler.forEach((g, i) => panel.appendChild(
      gunCiz(g, haftaKilitli ? false : (aktifGunIdx !== -1 && i > aktifGunIdx), haftaKilitli)));
  if (durum.acikGun) { // accordion: yalnız kullanıcının açtığı gün açık kalır (ilk açılışta hepsi kapalı)
    const acik = panel.querySelector(`.day[data-gun="${durum.acikGun}"]`);
    if (acik) acik.classList.add("open");
  }
  panel.appendChild(kaynakBolumu(d.no)); // kaynaklar görevlerin ALTINDA + katlı — okuma HER zaman açık
  panel.appendChild(quizCiz(d, "STATIK", haftaKilitli || !gunlerBitti, haftaKilitli)); // hafta kilitli VEYA günler bitmedi → quiz kilitli

  if (durum.aiAktif && gunlerBitti) panel.appendChild(aiQuizBolumu(d.no)); // AI quiz de günler bitince

  gorevBariGuncelle(); // sohbet panelindeki dinamik "görevi bitir" barını her çizimde tazele (tek güvenli nokta)
}

// ---------- eğitim kaynakları (Türkçe çalışma metinleri; varsayılan katlı) ----------
function kaynakBolumu(hafta) {
  const kutu = el("div", "kaynak-blok");
  const eslesen = durum.kaynaklar.filter((k) => k.etiketler.includes(hafta));
  if (eslesen.length === 0) return kutu;

  const baslik = el("button", "kaynak-katli-baslik");
  baslik.type = "button";
  baslik.append(
      el("span", "kaynak-katli-ad", `📚 Bu haftanın kaynakları (${eslesen.length})`),
      el("span", "kaynak-katli-chev", "▸"));

  const liste = el("div", "kaynak-liste");
  liste.hidden = true;
  baslik.addEventListener("click", () => {
    const ac = liste.hidden;
    liste.hidden = !ac;
    baslik.classList.toggle("acik", ac);
  });

  eslesen.forEach((k) => {
    const satir = el("div", "kaynak-satir");
    satir.append(el("span", "kaynak-ad", k.baslik),
        el("span", "kaynak-meta mono", `${k.yazar} · ${k.tarih}`));
    satir.addEventListener("click", () => { durum.okunanGorev = null; kaynakDrawerYukle(k); }); // browse (göreve bağlı değil)
    liste.appendChild(satir);
  });

  kutu.append(baslik, liste);
  return kutu;
}

// ---------- okuma yan-sayfası (side-sheet drawer) ----------
function okumaDraweriAl() {
  let d = document.getElementById("oku-drawer");
  if (d) return d;

  const backdrop = el("div", "oku-backdrop");
  backdrop.id = "oku-backdrop";
  backdrop.hidden = true;
  backdrop.addEventListener("click", okumaDraweriKapat);

  d = el("div", "oku-drawer");
  d.id = "oku-drawer";
  d.hidden = true;
  d.setAttribute("role", "dialog");
  d.setAttribute("aria-modal", "true");

  const head = el("div", "oku-drawer-head");
  head.append(el("span", "oku-drawer-ikon", "📖"), el("span", "oku-drawer-baslik"));
  const kapat = el("button", "oku-drawer-kapat", "✕");
  kapat.type = "button";
  kapat.title = "Kapat";
  kapat.addEventListener("click", okumaDraweriKapat);
  head.appendChild(kapat);

  const govde = el("div", "oku-drawer-govde");
  const alt = el("div", "oku-drawer-alt mono");
  d.append(head, govde, alt);
  document.body.append(backdrop, d);

  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !d.hidden) okumaDraweriKapat();
  });
  return d;
}

let drawerKapatTimer = null; // kapanış timeout'u — 240ms içinde yeni açılış olursa iptal (#r5)
function okumaDraweriGoster() {
  if (drawerKapatTimer) { clearTimeout(drawerKapatTimer); drawerKapatTimer = null; }
  const d = okumaDraweriAl();
  const b = document.getElementById("oku-backdrop");
  b.hidden = false;
  d.hidden = false;
  document.body.classList.add("oku-acik");
  requestAnimationFrame(() => { b.classList.add("acik"); d.classList.add("acik"); });
}

function okumaDraweriKapat() {
  const d = document.getElementById("oku-drawer");
  const b = document.getElementById("oku-backdrop");
  if (!d || d.hidden) return;
  d.classList.remove("acik");
  b.classList.remove("acik");
  document.body.classList.remove("oku-acik");
  drawerKapatTimer = setTimeout(() => { d.hidden = true; b.hidden = true; drawerKapatTimer = null; }, 240);
}

let drawerNesli = 0; // son açılan kaynak kazansın — bayat /api/kaynak yanıtı ekranı ezmesin (#7)
/** Bir kaynağı (liste nesnesi) yan-sayfada açar; başlık listeden, gövde /api/kaynak'tan. */
async function kaynakDrawerYukle(kaynak) {
  const nesil = ++drawerNesli;
  const d = okumaDraweriAl();
  const baslikEl = d.querySelector(".oku-drawer-baslik");
  const govde = d.querySelector(".oku-drawer-govde");
  const alt = d.querySelector(".oku-drawer-alt");
  baslikEl.textContent = kaynak.baslik || "Kaynak";
  govde.innerHTML = "";
  alt.innerHTML = "";
  govde.appendChild(el("p", "qmeta mono", "yükleniyor…"));
  okumaDraweriGoster();
  try {
    const k = await api("/api/kaynak/" + kaynak.slug);
    if (nesil !== drawerNesli) return; // daha yeni bir kaynak açıldı — bayat cevabı basma (#7)
    govde.innerHTML = "";
    const icerik = el("div", "oku-metin");
    icerik.innerHTML = k.html; // güvenilir backend HTML'i
    govde.appendChild(icerik);
    govde.scrollTop = 0;
    // "✓ Okudum" butonu: göreve bağlı okuma VE görev henüz bitmemişse (DOM checkbox'a bağlı DEĞİL —
    // görev barından da çağrılabilir; kutu null olabilir). Tamamlama artık öğretmen kapısından geçer.
    if (durum.okunanGorev && durum.okunanGorev.t && !durum.okunanGorev.t.tamamlandi) {
      const okudum = el("button", "btn oku-okudum-btn", "✓ Okudum");
      okudum.type = "button";
      okudum.addEventListener("click", okudumTamamla);
      alt.appendChild(okudum);
    }
    // Not: İngilizce orijinale yönlendirme linki KALDIRILDI — öğrenci İngilizce bilmiyor,
    // Türkçe çalışma metni zaten tam içerik (kullanıcı isteği).
  } catch (h) {
    if (nesil !== drawerNesli) return; // bayat hata yeni drawer'ı ezmesin (#7)
    govde.innerHTML = "";
    govde.appendChild(el("p", "quiz-sonuc kaldi", "Kaynak açılamadı: " + h.message));
  }
}

/** Eşleşme bulunamayınca: yan-sayfada haftanın kaynaklarını tıklanabilir liste olarak sun. */
function kaynakSecimDraweri() {
  ++drawerNesli; // uçuşan eski kaynak yüklemesi seçim listesini ezmesin (#7)
  const adaylar = durum.kaynaklar.filter((k) => k.etiketler.includes(durum.aktifHafta));
  if (adaylar.length === 0) {
    alert("Bu hafta için çalışma metni yok.");
    return;
  }
  const d = okumaDraweriAl();
  d.querySelector(".oku-drawer-baslik").textContent = "Hangi kaynağı okumak istersin?";
  const govde = d.querySelector(".oku-drawer-govde");
  govde.innerHTML = "";
  d.querySelector(".oku-drawer-alt").innerHTML = "";
  const liste = el("div", "oku-secim-liste");
  adaylar.forEach((k) => {
    const satir = el("div", "kaynak-satir");
    satir.append(el("span", "kaynak-ad", k.baslik),
        el("span", "kaynak-meta mono", `${k.yazar} · ${k.tarih}`));
    satir.addEventListener("click", () => kaynakDrawerYukle(k)); // aynı drawer o kaynağı yükler
    liste.appendChild(satir);
  });
  govde.appendChild(liste);
  okumaDraweriGoster();
}

/** "✓ Okudum": okuma görevini öğretmen-kapısına BAĞLAR (doğrudan bitirmez). AI kapalıysa doğrudan tamamlar. */
async function okudumTamamla() {
  const gv = durum.okunanGorev;
  const baslikEl = document.querySelector("#oku-drawer .oku-drawer-baslik");
  const baslik = (baslikEl && baslikEl.textContent) || "Yazı";
  okumaDraweriKapat();
  durum.okunanGorev = null;
  if (!gv) return; // göreve bağlı olmayan okuma (browse) — tamamlanacak görev yok
  const gun = gv.g.gun;
  if (!durum.aiAktif) { // yumuşak kapı için AI şart — AI kapalıysa eskisi gibi doğrudan tamamla
    if (await gorevTamamla(gv.g.gun, gv.t.index)) haftaCiz();
    return;
  }
  // AI açık: görevi HENÜZ tamamlama — öğretmen bir yansıtma sorusu sorup samimi cevapla geçirecek.
  // hafta = AI çalışma haftası (kilitli önizleme aktifHafta'yı kaydırsa bile yanlış haftaya toggle GİTMESİN).
  durum.gorevBekleyen = { hafta: sohbetHaftasi(), gun, index: gv.t.index, tip: "oku" };
  gorevBariGuncelle(); // bar artık "okudun, öğretmenin sorusunu cevapla" durumunu bilsin
  await okuSonrasiSohbet(baslik, gun);
}

/** Okuma sonrası: sohbete geç, geçmişi yükle, 'okudun ✓' + öğretmenin yansıtma sorusu (görev HENÜZ geçmedi). */
async function okuSonrasiSohbet(baslik, gun) {
  const nesil = haftaSecNesli;
  chatGirdi().placeholder = "Soru sor ya da 'ben şunu yaptım' de… (Enter = gönder)";
  await sohbetGecmisiYukle();
  if (nesil !== haftaSecNesli) return; // bu sırada hafta değişti — bayat okuma sorusunu basma (#4)
  // Görev HENÜZ tamamlanmadı — öğretmen samimi bir cevapla geçirecek (yumuşak kapı):
  mesajEkle("sistem", `📖 "${baslik}" yazısını okudun ✓`);
  mesajEkle("asistan", "Şimdi kısaca üstünden geçelim: en kritik 1-2 nokta neydi? Kendi cümlelerinle yaz — yazınca bu görevi geçeceğiz.");
  panelFokusla();
}

// ---------- haftalık rapor ----------
async function raporGoster(hafta, btn, yenile = false) {
  const alan = document.getElementById("rapor-alani");
  alan.innerHTML = "";
  alan.appendChild(el("p", "qmeta mono", yenile ? "rapor yeniden üretiliyor… (~30sn)" : "rapor hazırlanıyor…"));
  btn.disabled = true;
  try {
    const r = yenile
        ? await api(`/api/rapor/${hafta}/yenile`, { method: "POST" })
        : await api(`/api/rapor/${hafta}`);
    alan.innerHTML = "";
    const kutu = el("div", "rapor-kutu");
    kutu.innerHTML = r.html; // backend commonmark escapeHtml ile üretir
    const altBar = el("div", "rapor-alt mono");
    altBar.append(el("span", null, r.cacheden ? "daha önce hazırlanmıştı" : "yeni hazırlandı"));
    const yenileBtn = el("button", "btn btn-ikincil", "Yenile");
    yenileBtn.addEventListener("click", () => raporGoster(hafta, btn, true));
    const kapatBtn = el("button", "btn btn-ikincil", "Kapat");
    kapatBtn.addEventListener("click", () => (alan.innerHTML = ""));
    altBar.append(yenileBtn, kapatBtn);
    alan.append(kutu, altBar);
  } catch (h) {
    alan.innerHTML = "";
    alan.appendChild(el("p", "quiz-sonuc kaldi", "Rapor alınamadı: " + h.message));
  } finally {
    btn.disabled = false;
  }
}

// ---------- AI quiz üretimi ----------
function aiQuizBolumu(hafta) {
  const kutu = el("div", "quiz ai-quiz");
  kutu.append(el("h3", null, "AI'dan yeni sorular"),
      el("p", "qmeta mono", "Bu haftanın sabit sorularından farklı, yeni sorular üretilir"));
  const btn = el("button", "btn", "5 Yeni Soru Üret");
  btn.addEventListener("click", async () => {
    btn.disabled = true;
    btn.textContent = "üretiliyor… (~20sn)";
    try {
      const q = await api("/api/quiz/uret", {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ hafta, adet: 5 }),
      });
      const sorular = q.sorular.map((s) => ({
        soru: s.soru, secenekler: s.secenekler,
        dogruIndex: s.dogru_index, aciklama: s.aciklama,
      }));
      kutu.replaceWith(quizCiz({ no: hafta, quiz: sorular, sonQuizPuan: null }, "AI_URETIM"));
    } catch (h) {
      btn.textContent = "5 Yeni Soru Üret";
      btn.disabled = false;
      alert("Quiz üretilemedi: " + h.message);
    }
  });
  kutu.appendChild(btn);
  return kutu;
}

function gunCiz(g, kilitli = false, haftaKilitli = false) {
  const gun = el("div", "day" + (kilitli ? " gun-kilitli" : ""));
  gun.dataset.gun = g.gun; // görev barının "sonraki güne geç" butonu bu günü bulabilsin
  gun.dataset.gun = g.gun;
  const bas = el("div", "day-head");
  const biten = g.gorevler.filter((t) => t.tamamlandi).length;
  const tumBitti = biten === g.gorevler.length && g.gorevler.length > 0;
  const stat = el("span", "dstat mono" + (tumBitti ? " done" : ""), `${biten}/${g.gorevler.length}`);
  bas.append(el("span", "dno mono", "GÜN " + g.gun), el("span", "dtitle", g.baslik), stat,
      el("span", "chev", kilitli ? "🔒" : "▶"));
  if (kilitli) bas.title = "Önce sıradaki günü bitir";
  bas.addEventListener("click", () => {
    if (kilitli) return; // kilitli gün açılmaz
    const aciliyor = !gun.classList.contains("open");
    if (aciliyor && gun.parentElement) { // accordion: aynı anda tek gün açık — diğerlerini kapat
      gun.parentElement.querySelectorAll(".day.open").forEach((d) => d.classList.remove("open"));
    }
    gun.classList.toggle("open", aciliyor);
    durum.acikGun = aciliyor ? g.gun : null;
    if (aciliyor) {
      durum.aktifGun = g.gun; // görüntü günü
      if (!haftaKilitli) {    // AI günü/bağlamı yalnız AÇIK (çalışma) haftada değişir
        if (durum.aktifGorev && durum.aktifGorev.gun !== g.gun) aktifGorevTemizle();
        durum.sohbetGun = g.gun;
        chatDurumYaz();
        gorevBariGuncelle(); // gün değişti — bar yeni günün aktif görevine güncellensin
      }
    }
  });

  const govde = el("div", "day-body");
  const ders = el("div", "ders");
  ders.innerHTML = g.dersHtml; // backend commonmark ile üretir (escapeHtml açık)
  govde.appendChild(ders);

  const liste = el("div", "tasklist");
  govde.appendChild(liste);

  // Görevler SIRAYLA açılır: ilk bitmemiş = aktif, sonrakiler kilitli. Tamamlanınca yeniden çizilir.
  function tasklistCiz() {
    liste.innerHTML = "";
    liste.appendChild(el("div", "ipucu", haftaKilitli
        ? "👁 Bu hafta henüz açık değil — görevleri okuyup inceleyebilirsin ama YAPAMAZSIN. Kaynakları aşağıdan okuyabilirsin."
        : "💡 Görevleri SIRAYLA yap — birini bitirince sıradaki açılır. Sağdaki butona bas; kutucuğu sistem doldurur."));
    const aktifIdx = g.gorevler.findIndex((t) => !t.tamamlandi);
    g.gorevler.forEach((t, i) => {
      const tip = gorevTipiCikar(t.metin);
      const meta = GOREV_TIPLERI[tip];
      // Kilitli haftada görev-sırası kilidi UYGULANMAZ (hepsi salt-görüntü); zaten hepsi devre dışı.
      const kilitli = !haftaKilitli && !t.tamamlandi && aktifIdx !== -1 && i > aktifIdx;
      const aktif = !haftaKilitli && !t.tamamlandi && i === aktifIdx;
      const satir = el("div", "task" + (t.tamamlandi ? " checked" : "") + (kilitli ? " task-kilitli" : "") + (aktif ? " task-aktif" : ""));
      const kutu = document.createElement("input");
      kutu.type = "checkbox";
      kutu.checked = t.tamamlandi;
      kutu.disabled = haftaKilitli || kilitli || durum.aiAktif; // kilitli hafta → tamamlanamaz
      kutu.addEventListener("change", async () => {
        // haftaCiz: yalnız tasklistCiz değil — günün son görevi bitince SONRAKİ GÜN kilidi de kalksın (#r5)
        if (await gorevToggle(g.gun, t.index, satir, kutu, stat, g)) haftaCiz();
      });
      const rozet = el("span", "tip-rozet " + meta.sinif, kilitli ? "🔒" : meta.ikon);
      rozet.title = kilitli ? "Önce sıradaki görevi bitir" : meta.etiket;
      const metinEl = el("span", "task-metin", t.metin); // el() → textContent (XSS-güvenli)
      const aksiyon = el("button", "task-aksiyon " + meta.sinif, meta.btn);
      aksiyon.type = "button";
      aksiyon.disabled = haftaKilitli || kilitli; // kilitli hafta → görev eylemi yok
      aksiyon.addEventListener("click", () => gorevAksiyon(tip, g, t, kutu, satir, stat));
      satir.append(kutu, rozet, metinEl);
      if (aktif) satir.appendChild(el("span", "simdi-rozet", "şimdi sende"));
      satir.appendChild(aksiyon);
      liste.appendChild(satir);
    });
  }
  tasklistCiz();
  gun.append(bas, govde);
  return gun;
}

/** Görevi toggle eder. Başarılıysa true, hata olursa false döner (çağıran akış buna göre karar verir). */
async function gorevToggle(gun, gorevIndex, satir, kutu, stat, g) {
  const oncekiDisabled = kutu.disabled; // AI açıkken salt-durum checkbox'ı yeniden açılmasın (#3)
  kutu.disabled = true;
  try {
    const yanit = await api("/api/gorev/toggle", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      // Görev tamamlama AI ÇALIŞMA haftasına ait (okuma-kapısı/görev barı) — kilitli önizleme
      // durum.aktifHafta'yı kaydırsa bile YANLIŞ haftaya toggle GİTMESİN (inceleme KRİTİK bulgusu).
      body: JSON.stringify({ hafta: sohbetHaftasi(), gun, gorevIndex }),
    });
    kutu.checked = yanit.tamamlandi;
    satir.classList.toggle("checked", yanit.tamamlandi);
    g.gorevler.find((t) => t.index === gorevIndex).tamamlandi = yanit.tamamlandi;
    const biten = g.gorevler.filter((t) => t.tamamlandi).length;
    stat.textContent = `${biten}/${g.gorevler.length}`;
    stat.classList.toggle("done", biten === g.gorevler.length);
    await ozetYenile();
    return true;
  } catch (h) {
    const kayit = g.gorevler.find((t) => t.index === gorevIndex);
    kutu.checked = kayit ? kayit.tamamlandi : kutu.checked; // gerçek persisted duruma sabitle — flip DEĞİL (aksiyon çağrılarında yanlış işaret bırakmasın)
    alert("Kaydedilemedi: " + h.message);
    return false;
  } finally {
    kutu.disabled = oncekiDisabled;
  }
}

/** DOM-BAĞIMSIZ görev tamamlama (bar + okuma kartı kullanır; satir/kutu/stat gerektirmez).
 *  - Doğru haftaya toggle gider: durum.sohbetHafta ?? durum.aktifHafta (kilitli önizleme kaydırsa bile).
 *  - İDEMPOTENT: yalnız BİTMEMİŞ görevi tamamlar; zaten bitmişse hiçbir POST atmaz (geri-alma OLMAZ).
 *  Görev bittiyse true, hata olursa false döner. */
const gorevIslemde = new Set(); // uçuşan toggle anahtarları — backend flip yaptığı için eşzamanlı çift-çağrı geri-almayı önler
async function gorevTamamla(gun, gorevIndex) {
  const hafta = sohbetHaftasi();
  const anahtar = `${hafta}:${gun}:${gorevIndex}`;
  // Çalışma haftasının in-memory modelini seç (kilitli önizleme durum.detay'ı kaydırsa bile sohbetDetay doğru).
  const d = (durum.sohbetDetay && durum.sohbetDetay.no === hafta) ? durum.sohbetDetay
          : (durum.detay && durum.detay.no === hafta) ? durum.detay : null;
  const g = d ? d.gunler.find((x) => x.gun === gun) : null;
  const kayit = g ? g.gorevler.find((t) => t.index === gorevIndex) : null;
  if (kayit && kayit.tamamlandi) return true; // idempotent — zaten bitmiş; tekrar toggle etmeyip geri-almayı önle
  if (gorevIslemde.has(anahtar)) return true; // aynı görev HÂLÂ işleniyor — çift toggle/geri-alma yarışını önle
  gorevIslemde.add(anahtar);
  try {
    const yanit = await api("/api/gorev/toggle", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hafta, gun, gorevIndex }),
    });
    if (kayit) kayit.tamamlandi = yanit.tamamlandi;
    await ozetYenile();
    return yanit.tamamlandi === true;
  } catch (h) {
    alert("Kaydedilemedi: " + h.message);
    return false;
  } finally {
    gorevIslemde.delete(anahtar);
  }
}

/** gorevBekleyen için görev kaydını doğru haftanın in-memory modelinden bulur (yumuşak kapı bayat-kontrolü). */
function gorevKaydiBul(hafta, gun, index) {
  const d = (durum.sohbetDetay && durum.sohbetDetay.no === hafta) ? durum.sohbetDetay
          : (durum.detay && durum.detay.no === hafta) ? durum.detay : null;
  const g = d ? d.gunler.find((x) => x.gun === gun) : null;
  return g ? g.gorevler.find((t) => t.index === index) : null;
}

/** Sohbet panelindeki dinamik "görevi bitir" barı: çalışma haftası/gününün ilk BİTMEMİŞ görevini gösterir.
 *  haftaCiz sonunda + gün değişiminde çağrılır (tek güvenli nokta). */
function gorevBariGuncelle() {
  const bar = document.getElementById("gorev-bar");
  if (!bar) return;
  bar.innerHTML = "";
  bar.hidden = true;
  bar.classList.remove("onizleme", "suruyor", "bitti"); // her çizimde modifikatörleri sıfırla

  const hafta = sohbetHaftasi();
  if (hafta == null || !haftaAcikMi(hafta)) return; // kilitli/önizleme hafta → bar GÖRÜNMEZ
  // Başka bir haftayı ÖNİZLİYORSAN (görüntülenen hafta çalışma haftandan farklı): görev butonu
  // yerine AÇIKLAYICI NOT göster — kullanıcı "AI neden Hafta X'te takıldı?" diye şaşırmasın.
  // Böylece yanlış-hafta bağlamında görev de açılmaz.
  if (durum.aktifHafta != null && durum.aktifHafta !== hafta) {
    bar.hidden = false;
    bar.classList.add("onizleme");
    bar.appendChild(el("span", "gorev-bar-onizleme",
      `📎 Hafta ${durum.aktifHafta}'yi önizliyorsun (kilitli). AI Öğretmen çalıştığın haftada: Hafta ${hafta}. Devam etmek için Hafta ${hafta}'e dön.`));
    return;
  }

  const gun = durum.sohbetGun ?? durum.aktifGun;
  const d = (durum.sohbetDetay && durum.sohbetDetay.no === hafta) ? durum.sohbetDetay
          : (durum.detay && durum.detay.no === hafta) ? durum.detay : null;
  const g = d && gun != null ? d.gunler.find((x) => x.gun === gun) : null;
  if (!g) return;

  const aktifIdx = g.gorevler.findIndex((t) => !t.tamamlandi);

  // GÜNÜN TÜM GÖREVLERİ BİTTİ → kullanıcıyı ortada bırakma, SIRADAKİ ADIMI söyle.
  // ("Sonrakini söyle" ancak burada anlamlı: görev üstündeyken sıradaki KİLİTLİ, kullanılamaz.)
  if (aktifIdx === -1) {
    bar.hidden = false;
    bar.classList.add("bitti");
    const sonrakiGun = d.gunler.find((x) => x.gun > g.gun && x.gorevler.some((t) => !t.tamamlandi));
    if (sonrakiGun) {
      bar.appendChild(el("span", "gorev-bar-metin", `🎯 Gün ${g.gun} tamam — sıradaki: Gün ${sonrakiGun.gun}`));
      const b = el("button", "gorev-bar-btn", `→ Gün ${sonrakiGun.gun}'e geç`);
      b.type = "button";
      b.addEventListener("click", () => gunKartinaGit(sonrakiGun.gun));
      bar.appendChild(b);
    } else {
      bar.appendChild(el("span", "gorev-bar-metin", "🎯 Tüm günler tamam — sıradaki: haftalık quiz"));
      const b = el("button", "gorev-bar-btn", "→ Haftalık quiz'e geç");
      b.type = "button";
      b.addEventListener("click", () => quizeGit());
      bar.appendChild(b);
    }
    return;
  }

  const t = g.gorevler[aktifIdx];
  const tip = gorevTipiCikar(t.metin);
  const N = aktifIdx + 1;

  bar.hidden = false;
  bar.appendChild(el("span", "gorev-bar-metin", `${N}. görev: ${t.metin}`)); // el() → textContent (XSS-güvenli)

  // ZATEN GÖREVİN YÜZEYİNDE / SÜRECİNDE MİSİN? Öyleyse "göreve geç" demek yanlış
  // (durum körlüğü) — ne yapman gerektiğini SÖYLE, butonu "geri dön/odaklan"a çevir.
  const yazAlaniAcik = tip === "yaz" && document.getElementById("calisma-girdi")
      && durum.aktifGorev && durum.aktifGorev.gun === g.gun && durum.aktifGorev.index === t.index;
  // Öğretmen kapısı kurulu mu? (oku: "✓ Okudum"a bastın, öğretmen yansıtma bekliyor;
  //  tartis: tartışma başladı.) gorevBekleyen TİPTEN BAĞIMSIZ kontrol edilir — oku'yu
  //  atlamak, "okudum ama bar hâlâ 'göreve geç' diyor" hatasına yol açıyordu.
  const kapidaBekliyor = durum.gorevBekleyen
      && durum.gorevBekleyen.gun === g.gun && durum.gorevBekleyen.index === t.index;

  if (yazAlaniAcik || kapidaBekliyor) {
    bar.classList.add("suruyor");
    const metin = bar.querySelector(".gorev-bar-metin");
    if (yazAlaniAcik) {
      metin.textContent = `✍️ ${N}. görevi yazıyorsun — cevabını ortadaki alana yaz, sonra "değerlendirt"e bas.`;
      const b = el("button", "gorev-bar-btn", "↓ Yazma alanına dön");
      b.type = "button";
      b.addEventListener("click", () => {
        const ta = document.getElementById("calisma-girdi");
        if (ta) { ta.scrollIntoView({ behavior: "smooth", block: "center" }); ta.focus(); }
      });
      bar.appendChild(b);
    } else if (durum.gorevBekleyen.tip === "oku") {
      metin.textContent = `📖 ${N}. görevi okudun ✓ — şimdi öğretmenin sorusuna kendi cümlelerinle cevap yaz (aşağıdaki kutu). Cevabını yazınca görev geçer.`;
      const b = el("button", "gorev-bar-btn", "↓ Yazı kutusuna odaklan");
      b.type = "button";
      b.addEventListener("click", () => panelFokusla());
      bar.appendChild(b);
    } else {
      metin.textContent = `💬 ${N}. görevi tartışıyorsun — görüşünü aşağıdaki kutuya yaz.`;
      const b = el("button", "gorev-bar-btn", "↓ Yazı kutusuna odaklan");
      b.type = "button";
      b.addEventListener("click", () => panelFokusla());
      bar.appendChild(b);
    }
    return;
  }

  // Yüzeyinde DEĞİLSİN → fırlatıcı (launcher). Tamamlama öğretmen kapısında (oku/tartış)
  // ya da değerlendirmede (yaz) olur; bar düz "bitir" YAPMAZ.
  const btn = el("button", "gorev-bar-btn", `→ ${g.gun}. gün ${N}. göreve geç`);
  btn.type = "button";
  btn.addEventListener("click", () => {
    if (tip === "yaz") {
      if (!durum.aiAktif) { alert(AI_KAPALI_GOREV); return; }
      // DOM-bağımsız aktif görev bağlamı kur; orta kolonda çalışma kartını aç. Görev, yazılıp
      // /api/degerlendir başarıyla dönünce tamamlanır (calismaDegerlendir).
      durum.aktifGorev = { gun: g.gun, index: t.index, metin: t.metin, tip: "yaz", g, kutu: null, satir: null, stat: null };
      calismaGorunumuAc();
      return;
    }
    if (tip === "oku") {
      okuAksiyonu(g, t, null, null, null); // okuma drawer'ı; "✓ Okudum" → öğretmen kapısı
      return;
    }
    // tartis: sohbete odaklan + tartış görevini öğretmen kapısına bağla (açılış sorusu + gorevBekleyen)
    if (!durum.aiAktif) { alert(AI_KAPALI_GOREV); return; }
    durum.aktifGorev = { gun: g.gun, index: t.index, metin: t.metin, tip: "tartis", g, kutu: null, satir: null, stat: null };
    aktifGorevKartiCiz();
    tartisBaslat(g, t);
    panelFokusla();
  });
  bar.appendChild(btn);
}

/** Görev barındaki "→ Gün N'e geç": o günün kartına kaydırır ve kapalıysa açar.
 *  Çalışma kartı açıkken orta panel değişmiş olabilir → gün listesi DOM'da yoksa önce geri çiz. */
function gunKartinaGit(gunNo) {
  let kart = document.querySelector(`.day[data-gun="${gunNo}"]`);
  if (!kart) { haftaCiz(); kart = document.querySelector(`.day[data-gun="${gunNo}"]`); }
  if (!kart) return;
  if (!kart.classList.contains("open")) {
    const bas = kart.querySelector(".day-head");
    if (bas) bas.click(); // mevcut gün-açma akışını kullan (aktifGun + bar tazelenir)
  }
  kart.scrollIntoView({ behavior: "smooth", block: "start" });
}

/** Görev barındaki "→ Haftalık quiz'e geç": quiz bloğuna kaydırır (yoksa önce hafta panelini geri çiz). */
function quizeGit() {
  let q = document.querySelector(".quiz") || document.querySelector(".quiz-kilit");
  if (!q) { haftaCiz(); q = document.querySelector(".quiz") || document.querySelector(".quiz-kilit"); }
  if (q) q.scrollIntoView({ behavior: "smooth", block: "center" });
}

/** Tartış görevini öğretmen-kapısına bağlar: açılış sorusu bas + gorevBekleyen kur.
 *  Yumuşak kapı: kullanıcı samimi bir görüş yazınca gonder() görevi geçirir. */
function tartisBaslat(g, t) {
  durum.gorevBekleyen = { hafta: sohbetHaftasi(), gun: g.gun, index: t.index, tip: "tartis" };
  // t.metin kullanıcı/görev metni → mesajEkle el()/textContent ile basar (XSS-güvenli).
  mesajEkle("asistan", `💬 "${t.metin}" — bu konuda ne düşünüyorsun? Kendi görüşünü birkaç cümleyle yaz; birlikte üstünden geçip görevi geçelim.`);
  gorevBariGuncelle(); // bar artık "tartışma SÜRÜYOR" durumunu bilsin (→ "tartışıyorsun")
}

// ---------- görev aksiyonları (tipe göre iş başlatır) ----------
function gorevAksiyon(tip, g, t, kutu, satir, stat) {
  if (tip === "oku") { okuAksiyonu(g, t, kutu, satir, stat); return; }

  // yaz / tartis: AI paneliyle entegre
  if (!durum.aiAktif) {
    alert(AI_KAPALI_GOREV);
    return;
  }
  durum.aktifGorev = { gun: g.gun, index: t.index, metin: t.metin, tip, kutu, satir, stat, g };

  if (tip === "yaz") {
    calismaGorunumuAc();          // orta kolonda geniş çalışma kartı (ferah yazma alanı)
  } else { // tartis
    aktifGorevKartiCiz();
    tartisBaslat(g, t);           // öğretmen kapısı: açılış sorusu + gorevBekleyen
    panelFokusla();
  }
}

/** OKU: görev bağlamını saklar, haftanın kaynak adaylarını skorlar, en iyisini yan-sayfada açar. */
function okuAksiyonu(g, t, kutu, satir, stat) {
  durum.okunanGorev = { g, t, kutu, satir, stat };
  const m = (t.metin || "").toLocaleLowerCase("tr");
  const adaylar = durum.kaynaklar.filter((k) => k.etiketler.includes(durum.aktifHafta));
  if (adaylar.length === 0) { alert("Bu hafta için çalışma metni yok."); return; }

  let enIyi = null, enSkor = 0;
  adaylar.forEach((k) => {
    let skor = 0;
    // slug-token esas sinyal (Türkçe başlık çeviri farkından ötürü güvenilmez)
    k.slug.split("-").forEach((tok) => { if (tok.length >= 3 && m.includes(tok)) skor++; });
    // yazar kelimeleri ek sinyal (özel isimler metinde geçebilir)
    (k.yazar || "").toLocaleLowerCase("tr").split(/\s+/).forEach((w) => { if (w.length >= 3 && m.includes(w)) skor++; });
    if (skor > enSkor) { enSkor = skor; enIyi = k; }
  });

  if (enIyi && enSkor > 0) kaynakDrawerYukle(enIyi);   // doğrudan doğru makaleyi yan-sayfada aç
  else kaynakSecimDraweri();                            // eşleşme yok → yan-sayfada listeden seçtir
}

// ---------- orta kolon: geniş çalışma görünümü (yazma görevleri) ----------
/** Aktif yaz görevi için #hafta-paneli'ni odaklı, ferah bir çalışma kartıyla değiştirir. */
function calismaGorunumuAc() {
  const gv = durum.aktifGorev;
  if (!gv) return;
  const panel = document.getElementById("hafta-paneli");
  panel.innerHTML = "";

  const geri = el("button", "btn-ikincil calisma-geri", "← Görevlere dön");
  geri.type = "button";
  geri.addEventListener("click", () => haftaCiz());
  panel.appendChild(geri);

  const kart = el("div", "calisma-kart");
  kart.appendChild(el("div", "calisma-ust mono", `HAFTA ${sohbetHaftasi()} · GÜN ${gv.gun}`));

  const bas = el("div", "calisma-baslik");
  bas.append(el("span", "tip-rozet tip-yaz", "✍️"), el("span", "calisma-etiket", "Görev"));
  const cip = madeCipUret(gv.metin);
  if (cip) bas.appendChild(cip);
  kart.appendChild(bas);
  kart.appendChild(el("h3", "calisma-gorev", gv.metin)); // el() → textContent (XSS-güvenli)

  // BAĞLAM: workspace takeover o günün dersini gizliyor → görevin "neye göre" olduğunu burada hatırlat.
  if (gv.g && gv.g.dersHtml) {
    const bag = el("details", "calisma-baglam");
    bag.open = true;
    bag.appendChild(el("summary", null, "📎 Bu görev neye göre? — bu günün konusu"));
    const ic = el("div", "calisma-baglam-ic");
    ic.innerHTML = gv.g.dersHtml; // güvenilir backend HTML'i
    bag.appendChild(ic);
    kart.appendChild(bag);
  }

  // Kullanıcıyı butonlara YÖNLENDİR: terim/görev anlaşılmadıysa ne yapacağını söyle.
  const ipucu = el("p", "calisma-arac-ipucu");
  ipucu.append(
    el("span", null, "Görevi ya da bir terimi (ör. spec, harness, doğrulama) anlamadın mı? "),
    el("strong", null, "🔍 Soruyu aç"),
    el("span", null, "'a bas — sade Türkçeyle açıklar. Bu göreve nasıl bir cevap yazılır görmek istersen "),
    el("strong", null, "💡 Örnek cevap göster"),
    el("span", null, "'e bas (hazır bir taslak yazar; sen kendi cümlelerinle yeniden yaz)."),
  );

  const arac = el("div", "calisma-arac");
  const acBtn = el("button", "btn-ikincil", "🔍 Soruyu aç");
  acBtn.type = "button";
  const acKutu = el("div", "calisma-ornek");
  acKutu.hidden = true;
  acBtn.addEventListener("click", () => soruyuAc(acKutu, acBtn));
  const ornekBtn = el("button", "btn-ikincil", "💡 Örnek cevap göster");
  ornekBtn.type = "button";
  const ornekKutu = el("div", "calisma-ornek");
  ornekKutu.hidden = true;
  ornekBtn.addEventListener("click", () => calismaOrnek(ornekKutu, ornekBtn));
  arac.append(acBtn, ornekBtn);
  kart.append(ipucu, arac, acKutu, ornekKutu);

  const ta = el("textarea", "calisma-girdi");
  ta.id = "calisma-girdi";
  ta.placeholder = "Her maddeyi yeni satıra yaz…";
  ta.rows = 9;
  ta.addEventListener("input", () => {
    ta.style.height = "auto";
    ta.style.height = Math.max(220, ta.scrollHeight) + "px";
  });
  kart.appendChild(ta);

  const gonderBtn = el("button", "btn calisma-gonder", "Cevabımı AI öğretmene değerlendirt");
  gonderBtn.type = "button";
  const sonuc = el("div", "calisma-sonuc");
  gonderBtn.addEventListener("click", () => calismaDegerlendir(ta, gonderBtn, sonuc));
  kart.append(gonderBtn, sonuc);

  panel.appendChild(kart);
  window.scrollTo({ top: 0, behavior: "smooth" });
  ta.focus();
  gorevBariGuncelle(); // bar artık "yazma alanı AÇIK" durumunu bilsin (→ "yazıyorsun")
}

/** "N madde"/"N iş" ipucu çipi (textarea'yı DOLDURMAZ — yalnız bilgi amaçlı). */
function madeCipUret(metin) {
  const m = (metin || "").toLocaleLowerCase("tr");
  let e = m.match(/(\d+)\s*madde/);
  if (e) return el("span", "calisma-cip", `${e[1]} madde bekleniyor`);
  e = m.match(/(\d+)\s*(iş|şey)/);
  if (e) return el("span", "calisma-cip", `${e[1]} ${e[2]} bekleniyor`);
  return null;
}

/** Örneği çalışma kartının içinde gösterir (mevcut /api/sohbet; yeni endpoint yok). */
/** "Soruyu aç": AYRI uç (/api/aciklama) — görevi sade Türkçeyle açar, terim tanımlar; cevap/örnek vermez. */
async function soruyuAc(kutu, btn) {
  const gv = durum.aktifGorev;
  if (!gv) return;
  btn.disabled = true;
  kutu.hidden = false;
  kutu.innerHTML = "";
  kutu.appendChild(el("p", "qmeta mono", "soru açılıyor…"));
  try {
    const yanit = await api("/api/aciklama", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hafta: sohbetHaftasi(), gun: gv.gun, gorevMetni: gv.metin }),
    });
    kutu.innerHTML = "";
    kutu.appendChild(el("div", "calisma-ornek-baslik mono", "🔍 Soru açıklaması"));
    kutu.appendChild(el("p", "calisma-ornek-metin", yanit.aciklama)); // textContent
  } catch (h) {
    kutu.innerHTML = "";
    kutu.appendChild(el("p", "ipucu", "Açıklama alınamadı: " + h.message));
  } finally {
    btn.disabled = false;
  }
}

async function calismaOrnek(kutu, btn) {
  const gv = durum.aktifGorev;
  if (!gv) return;
  btn.disabled = true;
  kutu.hidden = false;
  kutu.innerHTML = "";
  kutu.appendChild(el("p", "qmeta mono", "örnek taslak hazırlanıyor…"));
  try {
    // AYRI uç: Sokratik sohbet değil — reddetmez, doğrudan örnek verir (prompts/ornek.st).
    const yanit = await api("/api/ornek", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hafta: sohbetHaftasi(), gun: gv.gun, gorevMetni: gv.metin }),
    });
    kutu.innerHTML = "";
    kutu.appendChild(el("div", "calisma-ornek-baslik mono", "💡 Bu göreve örnek cevap (kendi cümlelerinle yeniden yaz)"));
    kutu.appendChild(el("p", "calisma-ornek-metin", yanit.ornek)); // textContent; prompt "💡 Örnek" çerçevesi koyar
  } catch (h) {
    kutu.innerHTML = "";
    kutu.appendChild(el("p", "ipucu", "Örnek alınamadı: " + h.message));
  } finally {
    btn.disabled = false;
  }
}

/** Çalışma kartındaki cevabı GÖREV bağlamıyla değerlendirir; sonucu kartın içinde gösterir. */
async function calismaDegerlendir(ta, btn, sonuc) {
  const metin = ta.value.trim();
  if (!metin) { alert("Önce cevabını yaz."); return; }
  const gv = durum.aktifGorev;
  btn.disabled = true;
  sonuc.innerHTML = "";
  sonuc.appendChild(el("p", "qmeta mono", "değerlendiriliyor…"));
  try {
    const yanit = await api("/api/degerlendir", {
      method: "POST", headers: { "Content-Type": "application/json" },
      // YANLIŞ haftaya gitmesin: çalışma bağlamı sohbetHafta'ya ait (kilitli önizleme aktifHafta'yı kaydırsa bile).
      body: JSON.stringify({ hafta: sohbetHaftasi(), gun: gv.gun, tur: "OZET", icerik: metin, gorevMetni: gv.metin }),
    });
    sonuc.innerHTML = "";
    const kart = degerlendirmeKart(yanit.degerlendirme);
    sonuc.appendChild(kart);
    // görevi OTOMATİK tamamla (kullanıcı elle işaretlemesin; DOM-bağımsız — bar/görev-satırı fark etmez).
    const kayit = gv && gv.g ? gv.g.gorevler.find((t) => t.index === gv.index) : null;
    const oncedenBitti = kayit ? kayit.tamamlandi : false;
    if (gv && !oncedenBitti && await gorevTamamla(gv.gun, gv.index)) {
      sonuc.appendChild(el("div", "calisma-tamam-not", "✓ Görev tamamlandı olarak işaretlendi."));
      gorevBariGuncelle(); // değerlendirme sonrası bar sıradaki göreve ilerlesin
    }
    const sor = el("button", "btn-ikincil calisma-sohbet-link", "Bu konuyu sohbette sor");
    sor.type = "button";
    sor.addEventListener("click", () => { haftaCiz(); aktifGorevKartiCiz(); panelFokusla(); });
    kart.appendChild(sor);
  } catch (h) {
    sonuc.innerHTML = "";
    sonuc.appendChild(el("p", "quiz-sonuc kaldi", "Değerlendirilemedi: " + h.message));
  } finally {
    btn.disabled = false;
  }
}

// ---------- aktif görev bağlam kartı (mesaj listesi dışında; mod değişiminde kaybolmaz) ----------
function aktifGorevBariAl() {
  let bar = document.getElementById("aktif-gorev");
  if (!bar) {
    bar = el("div", "aktif-gorev");
    bar.id = "aktif-gorev";
    bar.hidden = true;
    const mesajlar = chatMesajlar();
    mesajlar.parentNode.insertBefore(bar, mesajlar);
  }
  return bar;
}

function aktifGorevKartiCiz() {
  const gv = durum.aktifGorev;
  if (!gv) return;
  const bar = aktifGorevBariAl();
  bar.innerHTML = "";
  bar.hidden = false;
  const meta = GOREV_TIPLERI[gv.tip];

  bar.append(el("span", "tip-rozet " + meta.sinif, meta.ikon),
      el("span", "aktif-gorev-etiket", "Görev"),
      el("span", "aktif-gorev-metin", gv.metin)); // el() → textContent

  const arac = el("div", "aktif-gorev-arac");
  if (gv.tip === "yaz") {
    const ornek = el("button", "aktif-gorev-ornek", "💡 Örnek iste");
    ornek.type = "button";
    ornek.addEventListener("click", ornekIste);
    arac.appendChild(ornek);
  }
  if (gv.kutu && !gv.kutu.checked) {
    const tamam = el("button", "aktif-gorev-tamam", "✓ Tamamladım");
    tamam.type = "button";
    tamam.addEventListener("click", async () => {
      tamam.disabled = true; // çift-tık koruması: çift POST toggle'ı geri almasın (#r6)
      if (!(await gorevToggle(gv.gun, gv.index, gv.satir, gv.kutu, gv.stat, gv.g))) { tamam.disabled = false; return; }
      tamam.textContent = "✓ Tamamlandı";
      haftaCiz(); // görev listesini tazele — sıradaki açılsın
    });
    arac.appendChild(tamam);
  }
  const kapat = el("button", "aktif-gorev-kapat", "✕");
  kapat.type = "button";
  kapat.title = "Görev bağlamını kapat";
  kapat.addEventListener("click", aktifGorevTemizle);
  arac.appendChild(kapat);
  bar.appendChild(arac);
}

function aktifGorevTemizle() {
  durum.aktifGorev = null;
  const bar = document.getElementById("aktif-gorev");
  if (bar) { bar.hidden = true; bar.innerHTML = ""; }
}

function panelFokusla() {
  chatGirdi().focus();
  const aside = document.querySelector(".chat-aside");
  if (aside) aside.scrollIntoView({ behavior: "smooth", block: "start" });
}

/** Görev için örnek taslak — AYRI uç (/api/ornek); Sokratik sohbet reddetmesin. */
async function ornekIste() {
  const gv = durum.aktifGorev;
  if (!gv) return;
  const bekleyen = mesajEkle("asistan bekliyor", "örnek taslak hazırlanıyor…");
  try {
    const yanit = await api("/api/ornek", {
      method: "POST", headers: { "Content-Type": "application/json" },
      // görev barı AI çalışma haftasına ait → sohbetHafta (kilitli önizlemede aktifHafta kayar)
      body: JSON.stringify({ hafta: sohbetHaftasi(), gun: gv.gun, gorevMetni: gv.metin }),
    });
    bekleyen.remove();
    mesajEkle("asistan", yanit.ornek);
  } catch (h) {
    bekleyen.textContent = "Örnek alınamadı: " + h.message;
  }
}

// ---------- quiz (statik + AI üretimi aynı arayüz; her kutu kendi durumunu taşır) ----------
function quizCiz(d, tur = "STATIK", kilitli = false, haftaKilitli = false) {
  const quiz = el("div", "quiz");
  quiz.append(el("h3", null, tur === "STATIK" ? "Haftalık Quiz" : "AI'nın Ürettiği Quiz"));
  if (kilitli) {
    quiz.appendChild(el("p", "quiz-kilit", haftaKilitli
        ? "🔒 Bu hafta henüz açık değil — quiz'i önceki haftayı bitirince çözebilirsin (içeriği inceleyebilirsin)."
        : "🔒 Önce bu haftanın TÜM günlerini bitir — quiz o zaman açılır."));
    return quiz;
  }
  const meta = el("p", "qmeta mono",
      d.quiz.length + " soru · geçme eşiği %60" + (d.sonQuizPuan != null ? ` · son puan: %${d.sonQuizPuan}` : ""));
  quiz.appendChild(meta);

  const yerel = { secimler: {}, bitti: false };

  d.quiz.forEach((soru, si) => {
    const q = el("div", "q");
    q.appendChild(el("div", "qt", `S${si + 1}. ${soru.soru}`));
    soru.secenekler.forEach((secenek, oi) => {
      const opt = el("div", "opt", secenek);
      opt.addEventListener("click", () => {
        if (yerel.bitti) return;
        yerel.secimler[si] = oi;
        q.querySelectorAll(".opt").forEach((o, i) => o.classList.toggle("secili", i === oi));
      });
      q.appendChild(opt);
    });
    q.appendChild(el("div", "aciklama", "Açıklama: " + soru.aciklama));
    quiz.appendChild(q);
  });

  const btn = el("button", "btn", "Quiz'i Bitir");
  btn.addEventListener("click", () => quizBitir(d, quiz, btn, yerel, tur));
  quiz.appendChild(btn);
  return quiz;
}

async function quizBitir(d, quizEl, btn, yerel, tur) {
  if (yerel.bitti) return;
  const cevaplanan = Object.keys(yerel.secimler).length;
  if (cevaplanan < d.quiz.length) {
    alert(`${d.quiz.length - cevaplanan} soru cevapsız.`);
    return;
  }
  yerel.bitti = true;
  quizEl.classList.add("bitti");
  btn.disabled = true;

  let dogru = 0;
  quizEl.querySelectorAll(".q").forEach((q, si) => {
    const secim = yerel.secimler[si];
    const hedef = d.quiz[si].dogruIndex;
    if (secim === hedef) dogru++;
    q.querySelectorAll(".opt").forEach((o, oi) => {
      if (oi === hedef) o.classList.add("dogru");
      else if (oi === secim) o.classList.add("yanlis");
    });
  });

  const puan = Math.round((100 * dogru) / d.quiz.length);
  const sonuc = el("div", "quiz-sonuc " + (puan >= 60 ? "gecti" : "kaldi"),
      `Puan: %${puan} — ${puan >= 60 ? "Geçtin! Bu hafta tamamlandı, sonraki hafta açıldı." : "Geçme eşiği %60. Dersleri tekrar edip yeniden dene."}`);
  quizEl.appendChild(sonuc);

  try {
    await api("/api/quiz/statik-sonuc", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hafta: d.no, puan, tur,
        detay: JSON.stringify({ dogru, toplam: d.quiz.length, secimler: yerel.secimler }) }),
    });
    await ozetYenile();
  } catch (h) {
    sonuc.textContent += " (kayıt hatası: " + h.message + ")";
  }
}

// ---------- akış ----------
async function ozetYenile() {
  durum.ozetler = await api("/api/program");
  haritaCiz();
}

let haftaSecNesli = 0; // inceleme bulgusu: hızlı hafta değiştirmede geç dönen yanıt eskiyi basmasın

async function haftaSec(no) {
  const nesil = ++haftaSecNesli;
  durum.gorunum = "program";    // bir haftaya tıklamak atölye görünümünden program görünümüne döner
  const acik = haftaAcikMi(no); // kilitli hafta = yalnız GÖRÜNTÜLEME; AI çalışma bağlamı DEĞİŞMEZ
  durum.aktifHafta = no;        // görüntülenen hafta (içerik)
  durum.aktifGun = null;
  durum.acikGun = null;         // yeni hafta: tüm günler kapalı başlar (accordion)
  if (acik) { // yalnız gerçek çalışma haftası değişiminde working-state temizle
    aktifGorevTemizle();
    durum.okunanGorev = null;
    durum.gorevBekleyen = null; // bekleyen öğretmen-kapısı önceki haftaya aitti — taşıma
  }
  const detay = await api("/api/hafta/" + no);
  if (nesil !== haftaSecNesli) return; // bu arada başka haftaya geçildi — bayat yanıtı at
  durum.detay = detay;
  haritaCiz();
  haftaCiz(); // haftaCiz açık haftada sohbet* bağlamını senkronlar
  chatDurumYaz();
  if (acik && durum.aiAktif) await sohbetGecmisiYukle(nesil); // kilitli önizlemede sohbet YENİDEN yüklenmez
}

// ---------- orta panel: Prompt Atölyesi görünümü ----------
/** Atölye kategorilerini bir kez yükler (cache). GET /api/atolye anahtarsız da çalışır. */
async function atolyeYukle() {
  if (!durum.atolye) durum.atolye = await api("/api/atolye");
  return durum.atolye;
}

/** Sol menü "🧪 Prompt Atölyesi" satırı → orta paneli atölye görünümüne çevirir. */
async function atolyeAc() {
  durum.gorunum = "atolye";
  haritaCiz(); // sol menü aktif durumunu tazele (atölye satırı active, hafta active kalkar)
  const panel = document.getElementById("hafta-paneli");
  panel.innerHTML = "";
  panel.appendChild(el("p", "qmeta mono", "atölye yükleniyor…"));
  try {
    const kategoriler = await atolyeYukle();
    if (durum.gorunum !== "atolye") return; // bu arada haftaya dönülmüş — bayat çizim yapma
    atolyeCiz(kategoriler);
  } catch (h) {
    panel.innerHTML = "";
    panel.appendChild(el("p", "quiz-sonuc kaldi", "Atölye yüklenemedi: " + h.message));
  }
}

/** Atölye görünümünü çizer (sekmeler + seçili kategorinin kartları). Sekme tıklaması bunu sync çağırır. */
function atolyeCiz(kategoriler) {
  const panel = document.getElementById("hafta-paneli");
  panel.innerHTML = "";

  const bas = el("div", "week-head");
  bas.append(el("span", "tag", "🧪 Atölye"), el("h2", null, "Prompt Atölyesi"));
  panel.appendChild(bas);
  panel.appendChild(el("p", "week-goal",
      "Prompt (istem) yazma tekniklerini tek tek deneyerek öğren. Kilit yok, sıra yok — bir kartı seç, "
      + "kendi prompt'unu yaz, ideal çözümle kıyasla. Bazı alıştırmalarda AI Öğretmen yazdığını değerlendirir."));

  if (!kategoriler || kategoriler.length === 0) {
    panel.appendChild(el("p", "ipucu", "Henüz alıştırma yok."));
    return;
  }
  if (durum.atolyeKat == null || durum.atolyeKat >= kategoriler.length) durum.atolyeKat = 0;

  const sekmeler = el("div", "atolye-sekmeler");
  kategoriler.forEach((k, i) => {
    const t = el("button", "atolye-sekme" + (i === durum.atolyeKat ? " aktif" : ""), k.baslik);
    t.type = "button";
    t.addEventListener("click", () => { durum.atolyeKat = i; atolyeCiz(kategoriler); window.scrollTo({ top: 0, behavior: "smooth" }); });
    sekmeler.appendChild(t);
  });
  panel.appendChild(sekmeler);

  const kat = kategoriler[durum.atolyeKat];
  if (kat.aciklama) panel.appendChild(el("p", "atolye-kat-aciklama", kat.aciklama));

  const kartlar = el("div", "atolye-kartlar");
  kat.alistirmalar.forEach((a) => kartlar.appendChild(atolyeKartCiz(a)));
  panel.appendChild(kartlar);
}

/** Düz metni XSS-güvenli basar: ``` kod fence'leri <pre>, aradaki metin <p> (textContent). */
function atolyeMetin(metin) {
  const frag = document.createDocumentFragment();
  const parcalar = (metin || "").split("```");
  parcalar.forEach((p, i) => {
    if (i % 2 === 1) { // fence içi (tek indeksler)
      const kod = p.replace(/^[a-zA-Z0-9_-]*\n/, "").replace(/\n$/, ""); // olası dil etiketi satırını at
      const pre = el("pre", "atolye-kod");
      pre.appendChild(el("code", null, kod)); // el() → textContent
      frag.appendChild(pre);
    } else {
      const t = p.trim();
      if (t) frag.appendChild(el("p", "atolye-p", t)); // textContent; white-space: pre-wrap satırları korur
    }
  });
  return frag;
}

function atolyeBolum(etiket, metin, etiketSinif) {
  const b = el("div", "atolye-bolum");
  b.appendChild(el("div", "atolye-etiket" + (etiketSinif ? " " + etiketSinif : ""), etiket));
  const ic = el("div", "atolye-bolum-ic");
  ic.appendChild(atolyeMetin(metin));
  b.appendChild(ic);
  return b;
}

/** Tek bir alıştırma kartı: teknik + görev + (kötü örnek) + textarea + ipucu + ideal çözüm toggle + AI değerlendir. */
function atolyeKartCiz(a) {
  const kart = el("div", "atolye-kart");

  const kbas = el("div", "atolye-kart-bas");
  kbas.append(el("h3", "atolye-teknik", a.teknik), el("span", "atolye-id mono", a.id));
  kart.appendChild(kbas);

  kart.appendChild(atolyeBolum("Görev", a.gorev));

  if (a.kotuOrnek) { // opsiyonel — bazı alıştırmalarda yok
    const kb = el("div", "atolye-bolum");
    kb.appendChild(el("div", "atolye-etiket kotu", "✕ Kötü örnek"));
    const kutu = el("div", "atolye-kotu");
    kutu.textContent = a.kotuOrnek.replace(/`/g, ""); // satır-içi backtick'leri temizle (düz göster)
    kb.appendChild(kutu);
    kart.appendChild(kb);
  }

  const ta = el("textarea", "atolye-girdi");
  ta.placeholder = "Kendi prompt'unu buraya yaz…";
  ta.rows = 5;
  kart.appendChild(ta);

  const ipucu = el("details", "atolye-ipucu");
  ipucu.appendChild(el("summary", null, "💡 İpucu"));
  const ipic = el("div", "atolye-ipucu-ic");
  ipic.appendChild(atolyeMetin(a.ipucu));
  ipucu.appendChild(ipic);
  kart.appendChild(ipucu);

  // "İdeal çözümü göster": JS toggle — backend çağrısı YOK, sınırsız pratik
  const cozumBtn = el("button", "btn-ikincil atolye-cozum-btn", "✓ İdeal çözümü göster");
  cozumBtn.type = "button";
  const cozum = el("div", "atolye-cozum");
  cozum.hidden = true;
  cozum.appendChild(atolyeBolum("İdeal çözüm", a.idealCozum));
  cozum.appendChild(atolyeBolum("Neden", a.neden));
  cozumBtn.addEventListener("click", () => {
    cozum.hidden = !cozum.hidden;
    cozumBtn.textContent = cozum.hidden ? "✓ İdeal çözümü göster" : "İdeal çözümü gizle";
  });
  kart.append(cozumBtn, cozum);

  // AI değerlendir — HER alıştırmada (AI aktifken); kullanıcı isteği: her prompt'un altında olsun
  if (durum.aiAktif) {
    const aiBtn = el("button", "btn atolye-ai-btn", "🤖 AI Öğretmen değerlendirsin");
    aiBtn.type = "button";
    const aiSonuc = el("div", "atolye-ai-sonuc");
    aiBtn.addEventListener("click", () => atolyeDegerlendirt(a, ta, aiBtn, aiSonuc));
    kart.append(aiBtn, aiSonuc);
  }
  return kart;
}

/** Öğrencinin yazdığı prompt'u POST /api/atolye/degerlendir ile değerlendirtir; puan+geriBildirim+iyilestirme basar. */
async function atolyeDegerlendirt(a, ta, btn, sonuc) {
  const prompt = ta.value.trim();
  if (!prompt) { alert("Önce kendi prompt'unu yaz."); return; }
  btn.disabled = true;
  sonuc.innerHTML = "";
  sonuc.appendChild(el("p", "qmeta mono", "AI öğretmen değerlendiriyor…"));
  try {
    const d = await api("/api/atolye/degerlendir", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ alistirmaId: a.id, ogrenciPrompt: prompt }),
    });
    sonuc.innerHTML = "";
    sonuc.appendChild(atolyeSonucKart(d));
  } catch (h) {
    sonuc.innerHTML = "";
    sonuc.appendChild(el("p", "quiz-sonuc kaldi", "Değerlendirilemedi: " + h.message));
  } finally {
    btn.disabled = false;
  }
}

/** Atölye değerlendirme kartı — mevcut .dk/.dk-puan/.dk-adim stillerini yeniden kullanır. */
function atolyeSonucKart(d) {
  const kart = el("div", "dk atolye-dk");
  if (d.puan != null) {
    const seviye = d.puan >= 70 ? "iyi" : d.puan >= 50 ? "orta" : "dusuk";
    kart.appendChild(el("div", "dk-puan " + seviye, "%" + d.puan));
  }
  if (d.geriBildirim) kart.appendChild(el("p", null, d.geriBildirim));
  if (d.iyilestirme) kart.appendChild(el("div", "dk-adim", "Şunu dene: " + d.iyilestirme));
  return kart;
}

// ---------- AI ÖĞRETMEN PANELİ ----------
const chatMesajlar = () => document.getElementById("chat-mesajlar");
const chatGirdi = () => document.getElementById("chat-girdi");
const chatGonderBtn = () => document.getElementById("chat-gonder");

/** AI çalışma haftası — panel başlığı, sohbet geçmişi, gönderim ve görev istekleri HEP bunu kullanır
 *  (eskiden ana sayfada gönderim hafta=null → 500). Açık bir hafta seçildiyse sohbetHafta; henüz
 *  seçilmediyse (ana sayfa) görüntülenen hafta açıksa o, kilitli önizlemeyse Hafta 1 (her zaman açık). */
function sohbetHaftasi() {
  if (durum.sohbetHafta != null) return durum.sohbetHafta;
  return haftaAcikMi(durum.aktifHafta) ? durum.aktifHafta : 1;
}

function chatDurumYaz() {
  const gunEki = durum.sohbetGun ? ` · GÜN ${durum.sohbetGun}` : "";
  document.getElementById("chat-durum").textContent =
      `HAFTA ${sohbetHaftasi()}${gunEki}` + (durum.aiAktif ? "" : " · AI KAPALI");
  if (durum.aiAktif) panelOdakGuncelle();
}

function mesajEkle(sinif, metin) {
  const m = el("div", "msg " + sinif, metin);
  chatMesajlar().appendChild(m);
  chatMesajlar().scrollTop = chatMesajlar().scrollHeight;
  return m;
}

// ---------- panel: görev odak bandı ----------
/** 'Şu an: Gün N — başlık' odak bandı — hangi görev bağlamında olduğunu gösterir. */
function panelOdakGuncelle() {
  let e = document.getElementById("panel-odak");
  if (!e) {
    e = el("div", "panel-odak");
    e.id = "panel-odak";
    const mesajlar = chatMesajlar();
    mesajlar.parentNode.insertBefore(e, mesajlar);
  }
  const gd = (durum.sohbetGun && durum.sohbetDetay) // AI çalışma bağlamı (görüntülenen kilitli hafta DEĞİL)
      ? durum.sohbetDetay.gunler.find((g) => g.gun === durum.sohbetGun) : null;
  if (gd) { e.hidden = false; e.textContent = `📍 Şu an: Gün ${gd.gun} — ${gd.baslik}`; }
  else { e.hidden = true; }
}

function aiPaneliKur() {
  document.getElementById("chat-dot").classList.toggle("aktif", durum.aiAktif);
  chatDurumYaz();

  if (!durum.aiAktif) {
    const bilgi = el("div", "chat-bilgi");
    if (durum.site) { // çok kullanıcılı site: öğrenciye yönelik dil (ortam değişkeni anlatılmaz)
      bilgi.textContent = "AI öğretmen henüz açılmadı (OpenRouter anahtarı bekleniyor). Dersleri okuyup "
          + "görevlerini işaretleyebilirsin; AI açılınca sohbet burada çalışacak.";
      chatGirdi().placeholder = "AI henüz açılmadı";
    } else { // yerel mod: geliştirici yönergesi
      bilgi.innerHTML = "<b>AI öğretmen şu an kapalı.</b><br>Uygulamayı <span class='mono'>OPENROUTER_API_KEY</span> ortam değişkeniyle başlatınca sohbet burada aktifleşir.";
      chatGirdi().placeholder = "AI şu an kapalı";
    }
    chatMesajlar().appendChild(bilgi);
    return;
  }

  chatGirdi().disabled = false;
  chatGonderBtn().disabled = false;
  chatGirdi().placeholder = "Soru sor ya da 'ben şunu yaptım' de… (Enter = gönder)";
  sohbetGecmisiYukle();

  chatGonderBtn().addEventListener("click", gonder);
  chatGirdi().addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); gonder(); }
  });
}

async function sohbetGecmisiYukle(nesil = haftaSecNesli) {
  const hafta = sohbetHaftasi(); // ana sayfada da başlıktaki hafta (sohbetHafta henüz null olabilir)
  if (!hafta) return;
  chatMesajlar().innerHTML = "";
  try {
    const gecmis = await api("/api/sohbet/" + hafta);
    if (nesil !== haftaSecNesli) return; // hafta değişti — bayat geçmişi basma
    if (gecmis.length === 0) {
      mesajEkle("asistan", `Hafta ${hafta} üzerinde çalışıyorsun. Takıldığın yerde sor; bir görevi bitirince "şunu yaptım" diye anlat — seni takip ediyorum.`);
    } else {
      chatMesajlar().appendChild(el("div", "chat-ayrac", "— bu hafta önceki konuşmalar —"));
      gecmis.forEach((m) => mesajEkle(m.rol === "USER" ? "user" : "asistan", m.icerik));
    }
  } catch (h) {
    mesajEkle("asistan bekliyor", "Geçmiş yüklenemedi: " + h.message);
  }
}

/** Yumuşak kapı heuristiği: samimi bir cümle geçer; boş/önemsiz cevap geçmez.
 *  Hepsi doğruysa true: uzunluk ≥20, kelime ≥4, salt önemsiz kalıba TAM eşleşmiyor. */
function mesajSubstantiveMi(s) {
  const t = (s || "").trim();
  if (t.length < 20) return false;
  if (t.split(/\s+/).filter(Boolean).length < 4) return false;
  const onemsiz = ["evet", "hayır", "hayir", "ok", "tamam", "bilmiyorum", "bilmem", "yok", "?", "??", "..."];
  if (onemsiz.includes(t.toLocaleLowerCase("tr"))) return false;
  return true;
}

async function gonder() {
  const metin = chatGirdi().value.trim();
  if (!metin || chatGonderBtn().disabled) return;
  chatGirdi().value = "";
  chatGonderBtn().disabled = true;
  const nesil = haftaSecNesli;          // (#race) gönderim anındaki hafta bağlamı
  mesajEkle("user", metin);
  const bekleyen = mesajEkle("asistan bekliyor", "düşünüyor…");

  try {
    const yanit = await api("/api/sohbet", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hafta: sohbetHaftasi(), gun: durum.sohbetGun, mesaj: metin }),
    });
    if (nesil !== haftaSecNesli) { bekleyen.remove(); return; } // hafta değişti — bayat yanıtı basma (#race)
    bekleyen.remove();
    mesajEkle("asistan", yanit.icerik);
    // YUMUŞAK KAPI: bir oku/tartış görevi öğretmen cevabı bekliyorsa, kullanıcının samimi bir
    // cevabı görevi geçirir. Yetersizse gorevBekleyen dolu kalır; öğretmenin normal yanıtı zaten
    // "biraz daha aç" diye yönlendirir (ekstra sabit nudge basma — çift olmasın).
    if (durum.gorevBekleyen && nesil === haftaSecNesli) { // hafta değiştiyse tamamlama YAPMA (#race)
      const bek = durum.gorevBekleyen;
      const suanHafta = sohbetHaftasi();
      if (bek.hafta !== suanHafta) {
        durum.gorevBekleyen = null; // başka haftaya geçilmiş — bayat kapıyı temizle (yanlış-hafta toggle YOK)
      } else {
        const kayit = gorevKaydiBul(bek.hafta, bek.gun, bek.index);
        if (kayit && kayit.tamamlandi) {
          durum.gorevBekleyen = null; // görev başka yoldan zaten bitmiş — sessizce temizle (çift duyuru yok)
        } else if (mesajSubstantiveMi(metin)) {
          durum.gorevBekleyen = null;
          const N = bek.index + 1;
          if (await gorevTamamla(bek.gun, bek.index)) {
            mesajEkle("sistem", `✓ ${N}. görev tamamlandı — sıradaki görev açıldı.`);
            haftaCiz();
          }
        }
      }
    }
  } catch (h) {
    bekleyen.textContent = "Hata: " + h.message;
  } finally {
    chatGonderBtn().disabled = false;
    chatGirdi().focus();
  }
}

/** Değerlendirme kartını KURAR ve döndürür (bir yere eklemez — çağıran yerleştirir). */
function degerlendirmeKart(d) {
  const kart = el("div", "dk");
  if (d.puan != null) {
    const seviye = d.puan >= 70 ? "iyi" : d.puan >= 50 ? "orta" : "dusuk";
    kart.appendChild(el("div", "dk-puan " + seviye, "%" + d.puan));
  }
  kart.appendChild(el("p", null, d.ozet));

  const bolum = (baslik, sinif, maddeler) => {
    if (!maddeler || maddeler.length === 0) return;
    kart.appendChild(el("h4", sinif, baslik));
    const ul = el("ul");
    maddeler.forEach((m) => ul.appendChild(el("li", null, m)));
    kart.appendChild(ul);
  };
  bolum("Doğru noktalar", "g", d.dogru_noktalar);
  bolum("Eksikler", "e", d.eksikler);
  bolum("Yanlış anlamalar", "y",
      (d.yanlis_anlamalar || []).map((y) => `${y.ne} → doğrusu: ${y.dogrusu} (${y.neden_onemli})`));

  if (d.sonraki_adim) kart.appendChild(el("div", "dk-adim", "Yarınki adımın: " + d.sonraki_adim));
  if (d.tekrar_onerisi) kart.appendChild(el("div", "dk-adim", "Tekrar et: " + d.tekrar_onerisi));
  return kart;
}

/** Temiz başlangıç: ilerleme + sohbet + gönderim + quiz siler (kitaplar/vektör/içerik korunur). */
async function sifirla() {
  if (!confirm("Tüm ilerlemen, sohbet geçmişin ve AI'ın seninle ilgili notları SİLİNECEK.\n(Kitaplığın, dersler ve müfredat korunur.)\nEmin misin?")) return;
  try {
    await api("/api/sifirla", { method: "POST" });
    location.reload();
  } catch (h) {
    alert("Sıfırlanamadı: " + h.message);
  }
}

/** Header'a "🔄 Sıfırla" butonu ekler (kılavuz butonunun yanına). */
function sifirlaButonuEkle() {
  const kilavuz = document.getElementById("kilavuz-ac");
  if (!kilavuz || !kilavuz.parentNode || document.getElementById("sifirla-ac")) return;
  const btn = el("button", "sifirla-btn", "🔄 Sıfırla");
  btn.id = "sifirla-ac";
  btn.type = "button";
  btn.title = "İlerleme ve sohbet verini siler — temiz başlangıç (kitaplığın ve dersler korunur)";
  btn.addEventListener("click", sifirla);
  kilavuz.parentNode.insertBefore(btn, kilavuz);
}

/** Açılış karşılama/bilgilendirme sayfası — Hafta 1 otomatik yüklenmez; kullanıcı buradan seçer.
 *  Butonlar mevcut akışı çağırır (haftaSec / mulakatAlanSec) — görev/sohbet mekanizmasına dokunmaz. */
function karsilamaCiz(ilkAcik) {
  durum.gorunum = "karsilama";     // hiçbir hafta/kademe aktif değil (haritaCiz nötr çizer)
  haritaCiz();                     // sol menüleri çiz (Eğitim + Mülakat senkron)
  const gb = document.getElementById("gorev-bar");
  if (gb) gb.hidden = true;

  const panel = document.getElementById("hafta-paneli");
  panel.innerHTML = "";
  const wrap = el("div", "karsilama");
  wrap.appendChild(el("h2", "karsilama-baslik", "AI Öğretmen — Mimar Defteri"));
  wrap.appendChild(el("p", "karsilama-ozet", durum.mulakatEtkin
      ? "İki bölüm var: 12 haftalık Eğitim programı ve Java Mülakat hazırlığı. "
        + "Soldaki menülerden gezinebilir ya da aşağıdan başlayabilirsin."
      : "12 haftalık Eğitim programı. Soldaki menüden gezinebilir ya da aşağıdan başlayabilirsin."));

  const kartlar = el("div", "karsilama-kartlar");

  const egKart = el("div", "karsilama-kart");
  egKart.append(el("div", "kk-ikon", "📚"), el("h3", null, "Eğitim Programı"),
      el("p", null, "12 haftada AI çağı yazılım mimarlığı — haftalık dersler, günlük görevler, "
          + "AI öğretmenle üstünden geçme ve haftalık quizler."));
  const egBtn = el("button", "btn",
      ilkAcik ? `Hafta ${ilkAcik.no}'e devam et` : "Programa başla");
  egBtn.addEventListener("click", () => haftaSec(ilkAcik ? ilkAcik.no : 1));
  egKart.appendChild(egBtn);

  const mlKart = el("div", "karsilama-kart");
  mlKart.append(el("div", "kk-ikon", "💼"), el("h3", null, "Mülakat Hazırlığı"),
      el("p", null, "7 kademe, 1000+ çoktan seçmeli soru. Konu anlatımını oku, soruları çöz; "
          + "yanlışta 🤖 butonuyla \"neden yanlış\" açıklaması al. Doğruların birikip takip edilir."));
  const mlBtn = el("button", "btn", "Mülakat'a git");
  mlBtn.addEventListener("click", () => {
    const ilk = Array.isArray(durum.mulakatAlanlar) ? durum.mulakatAlanlar[0] : null;
    if (ilk && window.mulakatAlanSec) mulakatAlanSec(ilk.slug); // mulakat.js yüklendiyse (tık anında hazır)
  });
  mlKart.appendChild(mlBtn);

  kartlar.append(egKart);
  if (durum.mulakatEtkin) kartlar.append(mlKart); // modül kapalıysa kart hiç gösterilmez (ADR-0011)
  wrap.appendChild(kartlar);
  panel.appendChild(wrap);
  window.scrollTo({ top: 0, behavior: "smooth" });
}

/** Özellik bayrakları (ADR-0011) — tek istek, app.js ve mulakat.js aynı Promise'i bekler.
 *  Hata durumunda güvenli varsayılan: mülakat KAPALI. */
const ozellikler = api("/api/ozellikler").catch(() => ({ mulakat: false }));

/** site profili (ADR-0012): üst barda oturumdaki e-posta + "Çıkış". Yerel modda hiçbir şey eklenmez. */
function oturumAlaniKur(oz) {
  if (!oz.site || !oz.kullanici) return;
  const hwrap = document.querySelector("header .hwrap");
  if (!hwrap || document.getElementById("oturum")) return;
  const kutu = el("div", "oturum");
  kutu.id = "oturum";
  const cikis = el("button", "btn btn-ikincil", "Çıkış");
  cikis.type = "button";
  cikis.addEventListener("click", async () => {
    cikis.disabled = true;
    try {
      await fetch("/api/kimlik/cikis", { method: "POST", headers: { "X-XSRF-TOKEN": csrfToken() } });
    } finally {
      location.href = "/giris.html?cikis";
    }
  });
  kutu.append(el("span", "eposta", oz.kullanici), cikis);
  hwrap.appendChild(kutu);
}

(async function basla() {
  try {
    const oz = await ozellikler;
    durum.mulakatEtkin = !!oz.mulakat;
    durum.site = !!oz.site;
    oturumAlaniKur(oz);
    const kullanim = await api("/api/kullanim").catch(() => ({ aiAktif: false }));
    durum.aiAktif = !!kullanim.aiAktif;
    durum.kaynaklar = await api("/api/kaynaklar").catch(() => []);
    await ozetYenile();
    // kaldığın yerden devam: %100 olmayan ilk hafta (karşılamadaki butona yansır)
    const ilkAcik = durum.ozetler.find((h) => h.ilerlemeYuzde < 100);
    aiPaneliKur();
    sifirlaButonuEkle();
    karsilamaCiz(ilkAcik); // Hafta 1 otomatik açılmaz — genel karşılama sayfası
  } catch (h) {
    document.getElementById("hafta-paneli").textContent = "Yüklenemedi: " + h.message;
  }
})();

// Kullanım kılavuzu (ilk ziyarette kendiliğinden açılır)
(() => {
  const dlg = document.getElementById('kilavuz');
  const ac = document.getElementById('kilavuz-ac');
  const kapat = document.getElementById('kilavuz-kapat');
  if (!dlg || !ac || !kapat) return;
  ac.addEventListener('click', () => dlg.showModal());
  kapat.addEventListener('click', () => dlg.close());
  try {
    if (!localStorage.getItem('kilavuzGoruldu')) {
      dlg.showModal();
      localStorage.setItem('kilavuzGoruldu', '1');
    }
  } catch (e) { /* localStorage kapalıysa sessizce geç */ }
})();
