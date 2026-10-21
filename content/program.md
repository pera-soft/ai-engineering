---
baslik: "AI Çağı Yazılım Mimarlığı — 12 Haftalık Program"
hafta_sayisi: 12
---

## Bu program sana ne kazandırır?

Yapay zekâ, kod yazmayı ucuzlattı. Değerli olan artık kodu yazmak değil; **ne yazılacağına karar vermek ve doğru yazıldığını güvence altına almak**. Bu 12 hafta seni o role taşır: satır üreten mühendisten, ortamı, niyeti ve geri beslemeyi tasarlayan mimara.

Program sonunda şunları yapabiliyor olacaksın:

- Bir yapay zekâ ajanının güvenle çalışabileceği bir repo kurmak: AGENTS.md, düzenli `docs/`, karar kayıtları (ADR).
- Mimari kararları ödünleşmeleriyle savunmak ve C4 diyagramlarıyla anlatmak.
- Katman ve güvenlik kurallarını insan dikkatine değil, otomatik kapılara (ArchUnit, SAST) zorlatmak.
- RAG, değerlendirme (evaluation), maliyet ve gecikme kararlarını gerekçeyle vermek.
- Dağıtık sistem ve veri kararlarını — servis sınırı, replikasyon, tutarlılık — bir çerçeveyle tartışmak.
- Öğrendiğini bir junior'a aktarmak ve bitirme projesiyle (capstone) hepsini tek işte birleştirmek.

## Nasıl işliyor?

Her haftanın bir teması var; tema beş güne bölünüyor. Bir gün şu ritimde geçer:

1. **Oku (~8 dakika)** — günün kısa çalışma metni. Kaynak İngilizceyse Türkçesi verilir; teknik terimlerin İngilizcesi parantez içinde durur.
2. **Uygula (~10 dakika)** — kendi projende küçük ve gerçek bir değişiklik: bir ADR yaz, bir kural ekle, bir test kur.
3. **Sına (~2 dakika)** — kısa quiz ve özet; anlamadığın yeri sonraki güne taşı.

Toplam **günde yaklaşık 20 dakika**, haftada beş gün. Kalan iki gün telafi ve tekrar içindir; haftalık yük iki saatin altında kalır.

Ritmin üç kuralı: (1) her gün küçük ama **gerçek bir çıktı** üret — sadece okumak saymaz; (2) geride kalırsan günü atlama, telafi gününü kullan; (3) hafta biterken o haftanın çıktısı kendi deponda görünür olmalı.

İlerlemen kaydedilir ve her hafta bir öncekinin çıktısının üstüne oturur — bu yüzden sıra önemlidir.

Programın yanında sırasız iki bölüm daha var: **Prompt Atölyesi** (yapay zekâya iş yaptırma tekniklerini deneyerek öğrenirsin; kilit yok) ve **Mülakat** (öğrendiğini soru-cevapla sınarsın). İkisi de haftaların akışını beklemez.

## Hafta indeksi

- Hafta 1: [Harness Mühendisliği Temelleri](hafta-01/hafta.md) — Rolün nasıl değiştiğini kavra: kod yazan değil, ortam/niyet/geri-besleme tasarlayan mühendis.
- Hafta 2: [Repo Bilgi Sistemi: AGENTS.md + docs/](hafta-02/hafta.md) — Repoyu 'kayıt sistemi' yap: içindekiler-tablosu AGENTS.md, yapılandırılmış docs/, ilk ADR'ler.
- Hafta 3: [Spec-Driven Akış ve Ajan Yönetimi](hafta-03/hafta.md) — İyi task spec yazmayı ve Research → Plan → Execute → Review → Ship akışını kurmayı öğren.
- Hafta 4: [Guardrail'lar: Mimari Zorlama + Güvenlik](hafta-04/hafta.md) — ArchUnit (Java) ve import-linter (Python) ile katman kurallarını mekanikleştir; SAST kapılarını kur.
- Hafta 5: [Mimari Düşünme I: Nitelikler ve Trade-off](hafta-05/hafta.md) — Fundamentals of Software Architecture (2e) Bölüm 1–8: mimari karakteristikler, trade-off analizi, modülerlik.
- Hafta 6: [Mimari Düşünme II: Stiller ve İletişim](hafta-06/hafta.md) — Fundamentals 2e ikinci yarı: mimari stiller (layered, hexagonal, microservices…) + C4 ile diyagramlama.
- Hafta 7: [AI Engineering I: Stack, Modeller, Prompt](hafta-07/hafta.md) — Chip Huyen'in kitabının ilk yarısı: AI stack, foundation model seçimi, prompt engineering disiplini.
- Hafta 8: [AI Engineering II: RAG, Fine-tune Kararı, Evaluation](hafta-08/hafta.md) — Kitabın ikinci yarısı: RAG mimarisi, ne zaman fine-tune, AI-as-judge ile değerlendirme, maliyet/gecikme.
- Hafta 9: [Hexagonal / Clean Mimari (Elle Kirlen)](hafta-09/hafta.md) — Get Your Hands Dirty on Clean Architecture ile port-adapter mimarisini Java'da uygula; ArchUnit ile mühürle.
- Hafta 10: [The Hard Parts: Dağıtık Kararlar](hafta-10/hafta.md) — Servis granülaritesi, coupling, dağıtık veri ve transaction — 'kolay cevabı olmayan' kararlar için çerçeve.
- Hafta 11: [Veri-Yoğun Sistemler (DDIA)](hafta-11/hafta.md) — DDIA 2e'nin çekirdeği: depolama motorları, replikasyon, partitioning, tutarlılık — veri mimarisi refleksi kazan.
- Hafta 12: [Liderlik, Mentorluk ve Capstone](hafta-12/hafta.md) — Junior mentorluk sistemini kur, ekip anlaşmasını yayınla, mimari kata ile her şeyi birleştir.

## Başlarken

Hafta 1, Gün 1 ile başla. Elinde çalışacağın gerçek bir proje olsun — alıştırmalar çıktıyı kendi deponda üretmeni ister. Temel kavramlar ("model", "prompt", "token", "bağlam penceresi", "ajan") Prompt Atölyesi'nin ilk bölümünde sıfırdan tanımlı.
