/* AI Öğretmen — Mülakat Hazırlığı sekmesi. app.js ile AYNI desenler (el()/api()/durum);
 * görev/sohbet mekanizmasına DOKUNMAZ. Orta panel (#hafta-paneli) atölye gibi devralınır.
 * Sol-menü senkronu: app.js haritaCiz() sonunda `if (window.mulakatHaritaCiz) mulakatHaritaCiz()`
 * çağırır (açık hook — monkey-patch yok). */
"use strict";

// durum'a mülakat alanları EKLE (ayrı const değil — app.js'in global durum'unu genişletir).
durum.mulakatAlanlar = [];      // GET /api/mulakat (dogruSayisi ile — DB ilerlemesi)
durum.mulakatAktifAlan = null;  // seçili alan slug'ı (sol menü active)
durum.mulakatHataliSayi = 0;    // GET /api/mulakat/tekrar uzunluğu (rozet)
durum.mulakatDogruSayi = 0;     // GET /api/mulakat/dogrular uzunluğu (rozet — takip; gün geçtikçe artar)
durum.mulakatBitenKonu = {};    // "alan/konu" → true (DB'den türetilir: dogruSayisi >= soruSayisi)

let mulakatNesli = 0; // yarış-koruması: hızlı alan/tekrar değişiminde bayat yanıt ekranı ezmesin

const MULAKAT_TEKRAR_BOS = "🎉 Tekrar edilecek soru yok — hepsini doğru cevapladın.";
const MULAKAT_TEKRAR_DUSTU = "✓ Doğru — bu soru tekrar listesinden çıktı.";

// ---------- sol: mülakat haritası ----------
function mulakatHaritaCiz() {
  if (!durum.mulakatEtkin) return; // modül kapalı (ADR-0011) — bölüm gizli kalır
  const kok = document.getElementById("mulakat-haritasi");
  if (!kok) return;
  kok.innerHTML = "";

  // En üstte kilitsiz "Hatalı Cevaplar" satırı (app.js .layer.atolye deseni).
  const tekrar = el("div", "layer atolye" + (durum.gorunum === "mulakat-tekrar" ? " active" : ""));
  tekrar.append(el("span", "no mono", "❌"),
      el("span", "nm", `Hatalı Cevaplar (${durum.mulakatHataliSayi})`),
      el("span", "pct mono", "tekrar →"));
  tekrar.title = "Son denemende yanlış cevapladığın soruları yeniden çöz";
  tekrar.addEventListener("click", mulakatTekrarAc);
  kok.appendChild(tekrar);

  // "Doğru Cevaplar" takip satırı — doğru cevapladıkça büyür (Hatalı'nın simetriği, yeşil).
  const dogrular = el("div", "layer atolye dogrular" + (durum.gorunum === "mulakat-dogru" ? " active" : ""));
  dogrular.append(el("span", "no mono", "✅"),
      el("span", "nm", `Doğru Cevaplar (${durum.mulakatDogruSayi})`),
      el("span", "pct mono", "göster →"));
  dogrular.title = "Doğru cevapladığın sorular — gün geçtikçe artar, ilerlemeni takip et";
  dogrular.addEventListener("click", mulakatDogrularAc);
  kok.appendChild(dogrular);

  // 6 Kademe'yi kendi katlanabilir alt-menüsüne al (genel menü — native <details>, CSP-güvenli).
  // Açık: bir kademe aktifse ya da bir görünüm seçili değilse (ilk açılışta kademeler görünür).
  const kademeGrup = el("details", "menu-bolum kademe-grup");
  kademeGrup.open = durum.gorunum === "mulakat" || (durum.gorunum !== "mulakat-tekrar" && durum.gorunum !== "mulakat-dogru");
  const kbas = el("summary", "map-title");
  kbas.append(el("span", "nm", "Kademeler"), el("span", "pct mono", durum.mulakatAlanlar.length + " alan"));
  kademeGrup.appendChild(kbas);
  const kgovde = el("div", "layer-stack");
  kademeGrup.appendChild(kgovde);
  kok.appendChild(kademeGrup);

  // Her ALAN için serbest-gezinme satırı (kilit YOK; ilerlemeye göre .done — DB'den).
  durum.mulakatAlanlar.forEach((alan) => {
    const toplam = alan.konular.length;
    const biten = alan.konular.filter((k) => durum.mulakatBitenKonu[alan.slug + "/" + k.slug]).length;
    const done = toplam > 0 && biten === toplam;
    const row = el("div", "layer"
        + (durum.gorunum === "mulakat" && durum.mulakatAktifAlan === alan.slug ? " active" : "")
        + (done ? " done" : ""));
    row.append(el("span", "no mono", "💼"),
        el("span", "nm", alan.baslik),
        el("span", "pct mono", mulakatIlerlemeYazisi(alan)));
    const s = mulakatAlanSayilari(alan); // sayılar kısaltılmış; tam anlamı ipucunda yaz
    row.title = `${alan.aciklama}\n${s.konuBaslanan}/${s.konuToplam} konuya başladın`
        + ` (${s.konuBiten} tanesinde tüm sorular doğru)\n${s.soruDogru}/${s.soruToplam} soru doğru`;
    row.addEventListener("click", () => mulakatAlanSec(alan.slug));
    kgovde.appendChild(row);
  });
}

/** Alan sayacı: "başlanan konu / toplam · doğru soru / toplam". Konu payı BAŞLAMAYI sayar
 *  (≥1 cevap), tamamlamayı değil: "hepsi doğru" ölçüsüyle sayınca iki konuyu baştan sona
 *  çözmüş kullanıcı hâlâ 0/30 görüyordu. Ustalık sinyali kaybolmaz — satır ancak kademenin
 *  TÜM konuları tam doğruyken yeşile (.done) döner. */
function mulakatIlerlemeYazisi(alan) {
  const s = mulakatAlanSayilari(alan);
  return `${s.konuBaslanan}/${s.konuToplam} · ${s.soruDogru}/${s.soruToplam}`;
}

function mulakatAlanSayilari(alan) {
  return {
    konuToplam: alan.konular.length,
    konuBaslanan: alan.konular.filter((k) => (k.cevaplananSayisi || 0) > 0).length,
    konuBiten: alan.konular.filter((k) => durum.mulakatBitenKonu[alan.slug + "/" + k.slug]).length,
    soruToplam: alan.konular.reduce((t, k) => t + (k.soruSayisi || 0), 0),
    soruDogru: alan.konular.reduce((t, k) => t + (k.dogruSayisi || 0), 0),
  };
}

/** GET /api/mulakat/tekrar uzunluğunu tazeler (rozet için). */
async function mulakatHataliSayiYenile() {
  const t = await api("/api/mulakat/tekrar").catch(() => []);
  durum.mulakatHataliSayi = Array.isArray(t) ? t.length : 0;
}

/** GET /api/mulakat/dogrular uzunluğunu tazeler (Doğru Cevaplar rozeti). */
async function mulakatDogruSayiYenile() {
  const d = await api("/api/mulakat/dogrular").catch(() => []);
  durum.mulakatDogruSayi = Array.isArray(d) ? d.length : 0;
}

/** .done'u DB'den türet: konu tamam = dogruSayisi >= soruSayisi. Yenilemeden sağ çıkar. */
function mulakatIlerlemeHydrate() {
  durum.mulakatAlanlar.forEach((alan) => {
    alan.konular.forEach((k) => {
      durum.mulakatBitenKonu[alan.slug + "/" + k.slug] = k.soruSayisi > 0 && k.dogruSayisi >= k.soruSayisi;
    });
  });
}

/** Konu kartı rozeti. Üç durum, çünkü "5/7 doğru" tek başına iki farklı hâli aynı gösteriyordu:
 *  2 soruyu yanlış bilmek ile 1'ini yanlış bilip 1'ini HİÇ cevaplamamak. Boş soru ayrıca yazılır
 *  (kullanıcı "bu konuyu bitirdim" sanıp atlamasın). Hiç dokunulmamış konu sade kalır. */
function mulakatKonuRozetYaz(stat, alanSlug, konu) {
  const cevaplanan = konu.cevaplananSayisi || 0;
  const bos = konu.soruSayisi - cevaplanan;
  if (cevaplanan === 0) {
    stat.textContent = `${konu.soruSayisi} soru`;
  } else {
    stat.textContent = `${konu.dogruSayisi || 0}/${konu.soruSayisi} doğru`
        + (bos > 0 ? ` · ${bos} boş` : "");
  }
  stat.classList.toggle("done", !!durum.mulakatBitenKonu[alanSlug + "/" + konu.slug]);
}

/** Cevap sonrası: DB YETKİLİ kaynak — alan ağacını yeniden çek + .done'u tazele (oturumlar-arası
 *  drift YOK). Açık konu kartı varsa rozetini (sayı DAHİL) tazeler. */
async function mulakatIlerlemeGuncelle(baglam) {
  durum.mulakatAlanlar = await api("/api/mulakat").catch(() => durum.mulakatAlanlar);
  mulakatIlerlemeHydrate();
  if (baglam.stat && baglam.alanSlug && baglam.konuSlug) {
    // Rozeti TAZE ağaçtan yaz: eski k nesnesi bayat dogruSayisi taşır (sayaç donardı).
    const alan = durum.mulakatAlanlar.find((a) => a.slug === baglam.alanSlug);
    const konu = alan && alan.konular.find((k) => k.slug === baglam.konuSlug);
    if (konu) mulakatKonuRozetYaz(baglam.stat, baglam.alanSlug, konu);
  }
}

/** Program görev barı mülakat görünümünde bayat görünmesin (programa dönünce gorevBariGuncelle geri getirir). */
function mulakatGorevBariGizle() {
  const gb = document.getElementById("gorev-bar");
  if (gb) gb.hidden = true;
}

// ---------- orta: alan görünümü (konu akordeonu) ----------
async function mulakatAlanSec(alanSlug) {
  const nesil = ++mulakatNesli;
  durum.gorunum = "mulakat";
  durum.mulakatAktifAlan = alanSlug;
  haritaCiz();             // app.js haritaCiz → sonunda mulakatHaritaCiz (iki menü senkron)
  mulakatGorevBariGizle();

  const panel = document.getElementById("hafta-paneli");
  panel.innerHTML = "";
  const alan = durum.mulakatAlanlar.find((a) => a.slug === alanSlug);
  if (!alan) { panel.appendChild(el("p", "ipucu", "Alan bulunamadı.")); return; }

  const bas = el("div", "week-head");
  bas.append(el("span", "tag", "💼 Mülakat"), el("h2", null, alan.baslik));
  panel.append(bas, el("p", "week-goal", alan.aciklama));
  panel.appendChild(el("p", "ipucu",
      "Bir konuya tıkla — ders metnini oku, sonra soruları çöz. Şıkka tıklayınca sonuç ve açıklama açılır."));

  alan.konular.forEach((k) => panel.appendChild(mulakatKonuCiz(alanSlug, k, nesil)));
  window.scrollTo({ top: 0, behavior: "smooth" });
}

/** Konu akordeon kartı (app.js gunCiz .day/.day-head/.day-body deseni). İçerik lazy yüklenir. */
function mulakatKonuCiz(alanSlug, k, nesil) {
  const kart = el("div", "day");
  kart.dataset.konu = k.slug;
  const bas = el("div", "day-head");
  const stat = el("span", "dstat mono");
  mulakatKonuRozetYaz(stat, alanSlug, k);
  // Konu numarası (frontmatter `sira`) başlıkta görünür: çalışma planları konuya numarayla
  // atıf yapıyor ("Gün 1: 68, 83, 84"), numara ekranda yoksa konu bulunamıyor.
  bas.append(el("span", "dno mono", (k.kademe || "").toString()),
      el("span", "dtitle", `${k.sira} · ${k.baslik}`), stat, el("span", "chev", "▶"));
  const govde = el("div", "day-body");
  let yuklendi = false;

  bas.addEventListener("click", async () => {
    const aciliyor = !kart.classList.contains("open");
    if (aciliyor && kart.parentElement) { // accordion: aynı anda tek konu açık
      kart.parentElement.querySelectorAll(".day.open").forEach((d) => d.classList.remove("open"));
    }
    kart.classList.toggle("open", aciliyor);
    if (!aciliyor || yuklendi) return;
    yuklendi = true;
    govde.innerHTML = "";
    govde.appendChild(el("p", "qmeta mono", "yükleniyor…"));
    try {
      const konu = await api(`/api/mulakat/${alanSlug}/${k.slug}`);
      if (nesil !== mulakatNesli) return; // başka alana geçildi — bayat çizme
      govde.innerHTML = "";
      const ders = el("div", "ders");
      ders.innerHTML = konu.dersHtml; // güvenilir backend HTML'i (commonmark + svg) — tek innerHTML noktası
      govde.appendChild(ders);
      const arac = el("div", "soru-araclar");
      arac.appendChild(mulakatTumunuAcBtn(govde));
      govde.appendChild(arac);
      konu.sorular.forEach((s, i) => govde.appendChild(
          mulakatSoruCiz(s, { alanSlug, konuSlug: k.slug, stat, toplam: k.soruSayisi }, i + 1)));
    } catch (h) {
      if (nesil !== mulakatNesli) return;
      govde.innerHTML = "";
      govde.appendChild(el("p", "quiz-sonuc kaldi", "Konu açılamadı: " + h.message));
      yuklendi = false; // tekrar denenebilsin
    }
  });

  kart.append(bas, govde);
  return kart;
}

/** "Tümünü aç / Tümünü kapat" düğmesi (soru listelerinin başına konur).
 *  NEDEN: kapalı <details> içeriğini tarayıcı hiç render etmez (content-visibility: hidden) —
 *  yukarıdan aşağı sürükleyerek kopyalarken kapalı soruların şıkları seçime GİRMEZ. Tek tıkla
 *  hepsi açılınca soru + şık + açıklama tek hamlede seçilip AI'ya yapıştırılabilir.
 *  kapsayici: içindeki details.mulakat-soru'ları yöneten eleman (canlı sorgulanır — düğme
 *  sorular çizilmeden önce eklenebilir). */
function mulakatTumunuAcBtn(kapsayici) {
  const btn = el("button", "btn-ikincil tumunu-ac");
  btn.type = "button"; // form içinde submit tetiklemesin
  const sorular = () => kapsayici.querySelectorAll("details.mulakat-soru");
  const etiketle = () => {
    const kapaliVar = Array.from(sorular()).some((d) => !d.open);
    btn.textContent = kapaliVar ? "▼ Tümünü aç" : "▲ Tümünü kapat";
    return kapaliVar;
  };
  btn.addEventListener("click", () => {
    const ac = etiketle(); // kapalı kalan varsa AÇ, hepsi açıksa KAPAT
    sorular().forEach((d) => { d.open = ac; });
    etiketle();
  });
  // <details> toggle olayı KABARMAZ → yakalama evresinde dinle; tek tek açıp kapatınca da
  // düğme etiketi gerçeği söylesin.
  kapsayici.addEventListener("toggle", etiketle, true);
  etiketle();
  return btn;
}

// ---------- soru kartı (katlanabilir <details>; tıkla-değerlendir) ----------
/** baglam: { alanSlug, konuSlug, stat, toplam } (konu görünümü) VEYA { tekrar:true } (tekrar görünümü).
 *  sira: 1-tabanlı soru numarası (özet başlığı "Soru N"). Soru varsayılan KAPALIDIR — menü gibi
 *  ok (▶) ile açılır; şıklar ancak açınca görünür. Native <details> (CSP-güvenli, JS toggle yok). */
function mulakatSoruCiz(soru, baglam = {}, sira) {
  const wrap = el("details", "quiz mulakat-soru"); // <details>: .quiz.bitti → .aciklama görünür
  const ozet = el("summary", "qsum");
  if (sira != null) ozet.appendChild(el("span", "qsum-no mono", "Soru " + sira));
  if (soru.zorluk) ozet.appendChild(el("span", "qsum-zor mono", soru.zorluk));
  ozet.appendChild(el("span", "qsum-t", soru.soru)); // el() → textContent (metin innerHTML ALMAZ)
  const durumEl = el("span", "qsum-durum mono"); // cevap sonrası ✓/✗ (özet başlığında)
  ozet.appendChild(durumEl);
  // Soru metni seçilebilir (.qsum-t user-select:text). Ama <summary> üzerinde sürükleyerek
  // seçim yapmak da "tıklama" sayılır ve soruyu açıp kapatırdı → metin seçiliyken toggle iptal.
  ozet.addEventListener("click", (ev) => {
    const sec = window.getSelection && window.getSelection();
    if (sec && !sec.isCollapsed && String(sec).trim() && ozet.contains(sec.anchorNode)) {
      ev.preventDefault(); // seçim korunur; aç/kapa yalnız düz tıklamada olur
    }
  });
  wrap.appendChild(ozet);

  const q = el("div", "q");
  const sikElemanlari = [];
  (soru.secenekler || []).forEach((secenek, oi) => {
    const opt = el("div", "opt", secenek);
    sikElemanlari.push(opt);
    // aciklamaEl aşağıda tanımlı; tıklama SONRADAN çalışır (TDZ yok) → o an atanmış olur.
    opt.addEventListener("click", () => mulakatCevapla(soru, oi, wrap, sikElemanlari, aciklamaEl, baglam, durumEl));
    q.appendChild(opt);
  });

  // SORUYA ADANMIŞ açıklama div'i — referansla doldurulur (querySelector ilk .aciklama'yı, yani
  // A şıkkının aiNot span'ini bulup üzerine yazardı: #1 çekirdek bug). Referans = kesin hedef.
  const aciklamaEl = el("div", "aciklama");
  q.appendChild(aciklamaEl);
  wrap.appendChild(q);

  // GERİ YÜKLEME: sunucu bu soruyu daha önce cevapladığımızı söylüyorsa ekranı o hâle boya.
  // (Kayıt hep DB'deydi; istemciye hiç gönderilmediği için başka sayfaya geçince "kaybolmuş"
  //  görünüyordu — asıl hata buydu.) Kip, sonDeneme'nin doğru cevabı taşıyıp taşımamasıdır.
  if (soru.sonDeneme) {
    if (soru.sonDeneme.dogruIndex != null) { // AÇIK kip (konu / doğrular): tam sonuç + kilit
      mulakatSonucBoya(soru.sonDeneme, wrap, sikElemanlari, aciklamaEl, durumEl);
      if (!soru.sonDeneme.dogru) {
        wrap.appendChild(mulakatAiNedenBtn(soru.id, soru.sonDeneme.secilenIndex));
      }
    } else { // GİZLİ kip (tekrar listesi): yalnız önceki seçim; soru çözülebilir kalır
      mulakatOncekiSecimBoya(soru.sonDeneme, sikElemanlari, durumEl);
    }
  }
  return wrap;
}

/** Cevap-sonrası boyama — CANLI gönderim ve sayfa GERİ YÜKLEMESİ aynı koddan geçer; iki yolun
 *  görünümü drift edemez. sonuc: { secilenIndex, dogru, dogruIndex, aciklama, siklar }.
 *  Alan kümesi POST /cevap yanıtı ile aynıdır (MulakatSonDeneme.acik bilerek öyle tasarlandı). */
function mulakatSonucBoya(sonuc, wrap, sikElemanlari, aciklamaEl, durumEl) {
  wrap.classList.add("bitti"); // .quiz.bitti → .aciklama görünür + soru kilitlenir
  sikElemanlari.forEach((o, i) => {
    o.classList.remove("secili", "onceki");
    o.querySelectorAll(".opt-not").forEach((n) => n.remove()); // varsa "← önceki cevabın" etiketi
    if (i === sonuc.dogruIndex) o.classList.add("dogru");
    else if (i === sonuc.secilenIndex) o.classList.add("yanlis");
    else o.classList.add("pasif"); // diğer tüm şıklar sönük + tıklanamaz
    // Şıka özel statik AI notu KENDİ şıkkında kalır (textContent — innerHTML değil).
    const not = sonuc.siklar && sonuc.siklar[i] && sonuc.siklar[i].aiNot;
    if (not) o.appendChild(el("span", "aciklama", "🤖 " + not));
  });
  aciklamaEl.textContent = "Açıklama: " + sonuc.aciklama; // ADANMIŞ div (referansla)
  if (durumEl) { // özet başlığında ✓/✗ — soru kapalıyken bile sonuç görünür
    durumEl.textContent = sonuc.dogru ? "✓" : "✗";
    durumEl.classList.remove("dogru", "yanlis");
    durumEl.classList.add(sonuc.dogru ? "dogru" : "yanlis");
  }
}

/** Tekrar listesi boyaması: YALNIZ önceki yanlış seçimi işaretler. Doğru cevap gösterilmez ve
 *  soru kilitlenmez ("bitti" YOK) — listenin amacı soruyu yeniden çözdürmek. */
function mulakatOncekiSecimBoya(deneme, sikElemanlari, durumEl) {
  const opt = sikElemanlari[deneme.secilenIndex];
  if (opt) {
    opt.classList.add("onceki");
    opt.appendChild(el("span", "opt-not mono", "← önceki cevabın"));
  }
  if (durumEl) {
    durumEl.textContent = "✗";
    durumEl.classList.add("yanlis");
  }
}

async function mulakatCevapla(soru, secilenIndex, wrap, sikElemanlari, aciklamaEl, baglam, durumEl) {
  // SENKRON çift-gönderim kilidi (await'ten ÖNCE): uçuş penceresinde ikinci tık POST/span üretmesin.
  if (wrap.dataset.gonderiliyor || wrap.classList.contains("bitti")) return;
  wrap.dataset.gonderiliyor = "1";
  const nesil = mulakatNesli;
  sikElemanlari.forEach((o, i) => o.classList.toggle("secili", i === secilenIndex));
  try {
    const sonuc = await api("/api/mulakat/cevap", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ soruId: soru.id, secilenIndex }),
    });
    if (nesil !== mulakatNesli) return; // alan/tekrar değişti — bayat yanıtı basma (finally kilidi açar)
    // Geri yüklemeyle AYNI boyama (secilenIndex sunucu yanıtında yok — istekten taşınır).
    mulakatSonucBoya({ ...sonuc, secilenIndex }, wrap, sikElemanlari, aciklamaEl, durumEl);

    if (!sonuc.dogru) wrap.appendChild(mulakatAiNedenBtn(soru.id, secilenIndex));

    await mulakatIlerlemeGuncelle(baglam); // DB'den yetkili sayı → sol menü + konu-kartı rozeti
    await mulakatHataliSayiYenile();
    await mulakatDogruSayiYenile();        // "Doğru Cevaplar" rozeti de tazelensin
    mulakatHaritaCiz();

    if (baglam.tekrar && sonuc.dogru) { // tekrar görünümünde: doğru cevaplanınca listeden düş
      wrap.innerHTML = "";
      wrap.appendChild(el("div", "quiz-sonuc gecti", MULAKAT_TEKRAR_DUSTU));
      setTimeout(() => { if (wrap.parentElement) wrap.remove(); mulakatTekrarBosMu(); }, 1200);
    }
  } catch (h) {
    sikElemanlari.forEach((o) => o.classList.remove("secili"));
    alert("Cevap gönderilemedi: " + h.message);
  } finally {
    delete wrap.dataset.gonderiliyor; // başarı → 'bitti' zaten kilitler; hata → yeniden denenebilir
  }
}

/** Yanlış cevap sonrası "🤖 Yapay zekâ: neden?" — POST /acikla (canlı AI). Atölye buton+gizli-kutu deseni. */
function mulakatAiNedenBtn(soruId, secilenIndex) {
  const sar = el("div");
  const btn = el("button", "btn-ikincil", "🤖 Yapay zekâ: neden?");
  btn.type = "button";
  const kutu = el("div", "calisma-ornek");
  kutu.hidden = true;
  btn.addEventListener("click", async () => {
    if (!durum.aiAktif) { // AI kapalıyken (anahtarsız boot) doğrudan bilgilendir — 503 beklemeden
      kutu.hidden = false;
      kutu.innerHTML = "";
      kutu.appendChild(el("p", "ipucu", "AI öğretmen devre dışı — yukarıdaki açıklama yeterli."));
      return;
    }
    btn.disabled = true;
    kutu.hidden = false;
    kutu.innerHTML = "";
    kutu.appendChild(el("p", "qmeta mono", "AI öğretmen açıklıyor…"));
    try {
      const y = await api("/api/mulakat/acikla", {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ soruId, secilenIndex }),
      });
      kutu.innerHTML = "";
      kutu.appendChild(el("div", "calisma-ornek-baslik mono", "🤖 Yapay zekâ açıklaması"));
      kutu.appendChild(el("p", "calisma-ornek-metin", y.aciklama)); // textContent
    } catch (h) {
      kutu.innerHTML = "";
      const msg = h.status === 503 ? (h.message || "AI öğretmen devre dışı") : "Açıklama alınamadı: " + h.message; // 503: sunucunun Türkçe AI mesajı
      kutu.appendChild(el("p", "ipucu", msg));
    } finally {
      btn.disabled = false;
    }
  });
  sar.append(btn, kutu);
  return sar;
}

// ---------- orta: tekrar görünümü (hatalı cevaplar) ----------
async function mulakatTekrarAc() {
  const nesil = ++mulakatNesli;
  durum.gorunum = "mulakat-tekrar";
  durum.mulakatAktifAlan = null;
  haritaCiz();             // app.js haritaCiz → sonunda mulakatHaritaCiz (iki menü senkron)
  mulakatGorevBariGizle();

  const panel = document.getElementById("hafta-paneli");
  panel.innerHTML = "";
  const bas = el("div", "week-head");
  bas.append(el("span", "tag", "❌ Tekrar"), el("h2", null, "Hatalı Cevaplar"));
  panel.append(bas, el("p", "week-goal",
      "Son denemende yanlış cevapladığın sorular. Doğru cevaplayınca listeden düşer."));
  const yukleniyor = el("p", "qmeta mono", "yükleniyor…");
  panel.appendChild(yukleniyor);

  try {
    const sorular = await api("/api/mulakat/tekrar");
    if (nesil !== mulakatNesli) return;
    yukleniyor.remove();
    if (!sorular || sorular.length === 0) {
      panel.appendChild(el("p", "ipucu", MULAKAT_TEKRAR_BOS));
      return;
    }
    const liste = el("div", "mulakat-tekrar-liste");
    sorular.forEach((s, i) => liste.appendChild(mulakatSoruCiz(s, { tekrar: true }, i + 1)));
    const arac = el("div", "soru-araclar");
    arac.appendChild(mulakatTumunuAcBtn(liste));
    panel.append(arac, liste);
  } catch (h) {
    if (nesil !== mulakatNesli) return; // bayat başarısız istek başka görünümü ezmesin
    yukleniyor.remove();
    panel.appendChild(el("p", "quiz-sonuc kaldi", "Tekrar listesi alınamadı: " + h.message));
  }
}

/** Tekrar listesi boşaldıysa (hepsi doğru) kutlama notu göster. */
function mulakatTekrarBosMu() {
  if (durum.gorunum !== "mulakat-tekrar") return;
  const liste = document.querySelector(".mulakat-tekrar-liste");
  if (liste && liste.children.length === 0) {
    liste.appendChild(el("p", "ipucu", MULAKAT_TEKRAR_BOS));
  }
}

// ---------- orta: doğru cevaplar görünümü (takip; gün geçtikçe artar) ----------
async function mulakatDogrularAc() {
  const nesil = ++mulakatNesli;
  durum.gorunum = "mulakat-dogru";
  durum.mulakatAktifAlan = null;
  haritaCiz();             // app.js haritaCiz → sonunda mulakatHaritaCiz (iki menü senkron)
  mulakatGorevBariGizle();

  const panel = document.getElementById("hafta-paneli");
  panel.innerHTML = "";
  const bas = el("div", "week-head");
  bas.append(el("span", "tag", "✅ Doğru"), el("h2", null, "Doğru Cevaplar"));
  panel.append(bas, el("p", "week-goal",
      "Son denemende DOĞRU cevapladığın sorular. Doğru cevapladıkça bu liste büyür — ilerlemeni takip et."));
  const yukleniyor = el("p", "qmeta mono", "yükleniyor…");
  panel.appendChild(yukleniyor);

  try {
    const sorular = await api("/api/mulakat/dogrular");
    if (nesil !== mulakatNesli) return;
    yukleniyor.remove();
    if (!sorular || sorular.length === 0) {
      panel.appendChild(el("p", "ipucu",
          "Henüz doğru cevapladığın soru yok. Bir konuya girip soru çözmeye başla — doğrular burada birikecek."));
      return;
    }
    panel.appendChild(el("p", "qmeta mono", sorular.length + " soru doğru cevaplandı"));
    const liste = el("div", "mulakat-dogru-liste");
    sorular.forEach((s, i) => liste.appendChild(mulakatSoruCiz(s, { dogru: true }, i + 1)));
    const arac = el("div", "soru-araclar");
    arac.appendChild(mulakatTumunuAcBtn(liste));
    panel.append(arac, liste);
  } catch (h) {
    if (nesil !== mulakatNesli) return; // bayat başarısız istek başka görünümü ezmesin
    yukleniyor.remove();
    panel.appendChild(el("p", "quiz-sonuc kaldi", "Doğru cevaplar listesi alınamadı: " + h.message));
  }
}

// ---------- açılış (app.js basla()'sından bağımsız kendi IIFE'si) ----------
(async function mulakatBasla() {
  // Modül varsayılan KAPALI (ADR-0011): bayrak false ise hiçbir /api/mulakat isteği atılmaz,
  // sol menü bölümü gizli kalır. `ozellikler` app.js'te tanımlı (aynı Promise, tek istek).
  durum.mulakatEtkin = !!(await ozellikler).mulakat;
  if (!durum.mulakatEtkin) return;
  const bolum = document.getElementById("mulakat-bolumu");
  if (bolum) bolum.hidden = false;
  try {
    durum.mulakatAlanlar = await api("/api/mulakat").catch(() => []);
    mulakatIlerlemeHydrate(); // .done DB'den (ilk durum sayfa yenilemesinden sağ çıkar)
    await mulakatHataliSayiYenile();
    await mulakatDogruSayiYenile();
    mulakatHaritaCiz();
  } catch (h) {
    // sol menü mülakat bloğu sessizce boş kalır — program akışını etkileme
  }
})();
