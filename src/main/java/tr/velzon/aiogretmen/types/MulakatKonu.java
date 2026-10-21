package tr.velzon.aiogretmen.types;

import java.util.List;

/**
 * Bir mülakat konusu (ör. "== vs equals"): ders metni ({@code konuAnlatimi}, ham markdown +
 * opsiyonel ```svg) ve çoktan seçmeli sorular. {@code sira} konu sıralamasını, {@code kademe}
 * konunun genel zorluğunu belirtir. {@code alanSlug} üst alana geri-bağdır.
 */
public record MulakatKonu(String alanSlug, String slug, String baslik, MulakatZorluk kademe,
                          int sira, String konuAnlatimi, List<MulakatSoru> sorular) {
}
