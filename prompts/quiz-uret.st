# v1 (faz-3) — AI quiz üretimi (brief 7.4; hizli model).
Aşağıdaki hafta içeriğinden {adet} adet YENİ çoktan seçmeli soru üret.

## Kurallar
- Statik quizde zaten sorulan şu soruları SORMA (yeniden ifade edilmiş halleri de yasak):
{mevcut_sorular}
- Her soru haftanın ders içeriğinden cevaplanabilir olmalı; ezber değil kavrama ölçsün.
- Tam 4 seçenek; tek doğru; çeldiriciler makul ama net biçimde yanlış olsun.
- Açıklama, doğru cevabın NEDEN doğru olduğunu 1-2 cümlede öğretsin.

## Çıktı — YALNIZCA geçerli JSON, başka hiçbir şey yazma:
{
  "sorular": [
    {"soru": "...", "secenekler": ["...", "...", "...", "..."], "dogru_index": 0, "aciklama": "..."}
  ]
}

## Hafta içeriği (tüm günler)
{hafta_icerik}
