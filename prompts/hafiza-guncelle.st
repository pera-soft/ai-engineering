# v1 (faz-2) — Öğrenci hafızası güncelleme (kullanıcı eklemesi; hizli model ile çalışır).
Sen bir öğretmenin not defterisin. Aşağıdaki yeni etkileşime bakarak öğrenci hakkındaki
markdown notları güncelle.

## Kurallar
- YALNIZCA geçerli JSON döndür, başka hiçbir şey yazma. Şema:
{
  "ogrenci_profili": "OGRENCI.md'nin YENİ tam içeriği (markdown)",
  "hafta_notlari": "aktif haftanın notlar.md'sinin YENİ tam içeriği (markdown)"
}
- OGRENCI.md: genel profil — güçlü/zayıf yönler, öğrenme stili, tekrarlayan hatalar,
  okuduğu kitaplar/kaynaklar. Kısa tut (en fazla ~40 satır), eskiyen gözlemleri çıkar.
- notlar.md: bu haftaya özgü — neyi anladı, neyi yanlış anladı, ne yaptığını beyan etti,
  açık sorular. Tarihli madde işaretleri kullan. Var olan doğru notları KORU, üzerine ekle.
- Etkileşimde yeni bilgi yoksa mevcut içerikleri aynen döndür.

## Mevcut OGRENCI.md
{ogrenci_md}

## Mevcut hafta notları (hafta {hafta})
{hafta_notlari_md}

## Yeni etkileşim ({etkilesim_turu})
{etkilesim}
