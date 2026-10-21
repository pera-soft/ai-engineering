/* AI Öğretmen — site profili kimlik sayfaları (giriş, kayıt, doğrulama). Framework yok; CSP-temiz
 * (inline betik/stil YOK). Sayfa türü <body data-sayfa="..."> ile seçilir. CSRF: sunucu XSRF-TOKEN
 * çerezini yazar; form gönderiminde _csrf alanı, fetch'te X-XSRF-TOKEN başlığı olarak geri yollanır
 * (ADR-0012). */
"use strict";

function csrfToken() {
  const m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  return m ? decodeURIComponent(m[1]) : "";
}

function mesajGoster(metin, tur) {
  const kutu = document.getElementById("mesaj");
  if (!kutu) return;
  kutu.textContent = metin;
  kutu.className = "kimlik-mesaj " + (tur || "hata");
  kutu.hidden = false;
}

function postJson(yol, govde) {
  return fetch(yol, {
    method: "POST",
    headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": csrfToken() },
    body: JSON.stringify(govde),
  });
}

/** Tek e-posta alanlı, hep NÖTR yanıt alan form (yeniden gönder / şifremi unuttum). */
function epostaFormu(form, btn, yol, notrMesaj) {
  form.addEventListener("submit", async (olay) => {
    olay.preventDefault();
    const email = form.email.value.trim().toLowerCase();
    if (!email.endsWith(ALAN_ADI) || email.length <= ALAN_ADI.length) {
      mesajGoster("Yalnız " + ALAN_ADI + " uzantılı şirket e-postası kullanılabilir.");
      return;
    }
    btn.disabled = true;
    try {
      const yanit = await postJson(yol, { email });
      if (yanit.ok) mesajGoster(notrMesaj, "bilgi");
      else mesajGoster("İstek gönderilemedi (HTTP " + yanit.status + "). Sayfayı yenileyip tekrar dene.");
    } catch (h) {
      mesajGoster("Sunucuya ulaşılamadı — bağlantını kontrol et.");
    } finally {
      btn.disabled = false;
    }
  });
}

const ALAN_ADI = "@pera.software";
const MIN_SIFRE = 10;
const params = new URLSearchParams(location.search);

const sayfalar = {
  giris() {
    const hatalar = {
      bilgi: "E-posta ya da şifre hatalı.",
      dogrulanmamis: "Hesabın henüz doğrulanmadı — e-postandaki doğrulama bağlantısına tıkla.",
      kilitli: "Çok fazla hatalı deneme. Hesabın 15 dakika kilitlendi; sonra tekrar dene.",
      kapali: "Bu hesap kapatılmış. Erişim için yöneticine başvur.",
    };
    if (params.has("hata")) mesajGoster(hatalar[params.get("hata")] || hatalar.bilgi, "hata");
    else if (params.has("cikis")) mesajGoster("Çıkış yaptın.", "bilgi");
    else if (params.has("sifirlandi")) mesajGoster("Şifren güncellendi. Yeni şifrenle giriş yap.", "bilgi");
    const form = document.getElementById("giris-formu");
    form.addEventListener("submit", () => {
      // düz HTML form POST'u (Spring Security form login) — token gizli alanla gider
      document.getElementById("csrf-alani").value = csrfToken();
    });
  },

  kayit() {
    const form = document.getElementById("kayit-formu");
    const btn = document.getElementById("kayit-btn");
    form.addEventListener("submit", async (olay) => {
      olay.preventDefault();
      const email = form.email.value.trim().toLowerCase();
      const sifre = form.sifre.value;
      if (!email.endsWith(ALAN_ADI) || email.length <= ALAN_ADI.length) {
        mesajGoster("Yalnız " + ALAN_ADI + " uzantılı şirket e-postasıyla kayıt olunabilir.");
        return;
      }
      if (sifre.length < MIN_SIFRE) { mesajGoster("Şifre en az " + MIN_SIFRE + " karakter olmalı."); return; }
      if (sifre !== form.sifre2.value) { mesajGoster("Şifreler aynı değil."); return; }
      btn.disabled = true;
      try {
        const yanit = await fetch("/api/kimlik/kayit", {
          method: "POST",
          headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": csrfToken() },
          body: JSON.stringify({ email, sifre }),
        });
        if (yanit.ok) {
          location.href = "/dogrulama-bekleniyor.html?email=" + encodeURIComponent(email);
          return;
        }
        const govde = await yanit.json().catch(() => ({}));
        mesajGoster(govde.detay || govde.hata || ("Kayıt başarısız (HTTP " + yanit.status + ")."));
      } catch (h) {
        mesajGoster("Sunucuya ulaşılamadı — bağlantını kontrol et.");
      } finally {
        btn.disabled = false;
      }
    });
  },

  bekleniyor() {
    const email = params.get("email");
    const form = document.getElementById("yeniden-formu");
    if (email) {
      document.getElementById("hedef-eposta").textContent = email;
      form.email.value = email;
    }
    epostaFormu(form, document.getElementById("yeniden-btn"), "/api/kimlik/dogrulama-yeniden",
        "Hesabın doğrulanmayı bekliyorsa yeni bir doğrulama bağlantısı gönderdik (önceki bağlantı artık "
        + "geçersiz). Gelen kutunu ve spam klasörünü kontrol et.");
  },

  unuttum() {
    epostaFormu(document.getElementById("unuttum-formu"), document.getElementById("unuttum-btn"),
        "/api/kimlik/sifremi-unuttum",
        "Bu adrese ait doğrulanmış bir hesap varsa şifre sıfırlama bağlantısı gönderdik (1 saat geçerli). "
        + "Gelen kutunu ve spam klasörünü kontrol et.");
  },

  sifirla() {
    // Token adres çubuğunda/geçmişte kalmasın: belleğe al, URL'den sil.
    const token = params.get("token") || "";
    if (params.has("token")) history.replaceState(null, "", location.pathname);
    const form = document.getElementById("sifirla-formu");
    const btn = document.getElementById("sifirla-btn");
    if (!token) {
      mesajGoster("Bağlantı eksik. E-postadaki bağlantıyı aynen aç ya da yeni bağlantı iste.");
      btn.disabled = true;
      return;
    }
    form.addEventListener("submit", async (olay) => {
      olay.preventDefault();
      const sifre = form.sifre.value;
      if (sifre.length < MIN_SIFRE) { mesajGoster("Şifre en az " + MIN_SIFRE + " karakter olmalı."); return; }
      if (sifre !== form.sifre2.value) { mesajGoster("Şifreler aynı değil."); return; }
      btn.disabled = true;
      try {
        const yanit = await postJson("/api/kimlik/sifre-sifirla", { token, sifre });
        if (yanit.ok) {
          location.href = "/giris.html?sifirlandi=1";
          return;
        }
        const govde = await yanit.json().catch(() => ({}));
        mesajGoster(govde.detay || govde.hata || ("Şifre güncellenemedi (HTTP " + yanit.status + ")."));
      } catch (h) {
        mesajGoster("Sunucuya ulaşılamadı — bağlantını kontrol et.");
      } finally {
        btn.disabled = false;
      }
    });
  },

  sonuc() {
    if (params.get("durum") === "basarili") {
      document.getElementById("baslik").textContent = "Hesabın doğrulandı";
      mesajGoster("Artık giriş yapabilirsin.", "bilgi");
    } else {
      document.getElementById("baslik").textContent = "Bağlantı geçersiz";
      mesajGoster("Doğrulama bağlantısı geçersiz, kullanılmış ya da süresi (24 saat) dolmuş. "
          + "\"Doğrulama bekleniyor\" sayfasından yeni bağlantı isteyebilirsin.");
      const alt = document.getElementById("yeniden-baglanti");
      if (alt) alt.hidden = false;
    }
  },
};

const sayfa = sayfalar[document.body.dataset.sayfa];
if (sayfa) sayfa();
