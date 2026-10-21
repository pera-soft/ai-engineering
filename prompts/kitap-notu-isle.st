# v2 — ozet alanının TEK string olduğu netleştirildi (haiku dizi döndürüyordu).
# v1 (faz-3) — Kitap notu işleme (kullanıcı eklemesi; hizli model).
Öğrenci "{kitap}" kitabından ham not/alıntı verdi. Bunu öğretim malzemesine dönüştür.

## 12 haftalık program başlıkları (etiketleme için)
{program_basliklari}

## Kurallar — YALNIZCA geçerli JSON döndür:
{
  "ozet": "notun 3-8 maddelik özeti — TEK bir string (JSON dizisi DEĞİL); maddeleri '- ' ile başlatıp aynı stringin içinde ayır",
  "etiketler": [ilgili hafta numaraları, ör. 1, 4, 7 — en az 1, en çok 4],
  "anahtar_kavramlar": ["kitaptaki kavram adları"]
}
- Etiketleri içerik benzerliğine göre seç: not hangi haftaların dersleriyle konuşuyorsa o.
- Özet, öğretmenin derste alıntılayabileceği netlikte olsun.

## Ham not
{not}
