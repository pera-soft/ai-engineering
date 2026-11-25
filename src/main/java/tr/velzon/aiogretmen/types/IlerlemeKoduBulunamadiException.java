package tr.velzon.aiogretmen.types;

/** İlerleme kodu biçimce geçerli ama bu kimlikle kayıtlı ziyaretçi yok (Faz 10, ADR-0017) → HTTP 422. */
public class IlerlemeKoduBulunamadiException extends RuntimeException {

    public static final String MESAJ = "Bu kodla kayıtlı ilerleme bulunamadı.";

    public IlerlemeKoduBulunamadiException() {
        super(MESAJ);
    }
}
