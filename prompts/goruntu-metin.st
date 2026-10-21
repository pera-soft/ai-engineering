# v1 (faz-3) — Kitap sayfası fotoğrafından metin çıkarma (guclu model, vision).
Bu görüntü bir kitap sayfasının fotoğrafı ("{kitap}" kitabından).

Sayfadaki metni olduğu gibi, düzgün paragraflar halinde çıkar:
- Başlıkları ve vurguları markdown ile koru (##, **kalın**).
- Sayfa numarası, koşu başlığı (header/footer) gibi gürültüyü atla.
- Şekil/tablo varsa [Şekil: tek cümle açıklama] biçiminde özetle.
- Okunamayan yer varsa [okunamadı] yaz; UYDURMA.
- Metin dışında hiçbir yorum ekleme — sadece sayfanın içeriği.
