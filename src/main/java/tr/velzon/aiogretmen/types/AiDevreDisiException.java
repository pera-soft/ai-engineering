package tr.velzon.aiogretmen.types;

/** ANTHROPIC_API_KEY tanımlı değilken AI özelliği çağrılırsa fırlatılır → HTTP 503. */
public class AiDevreDisiException extends RuntimeException {

    public AiDevreDisiException() {
        super("AI özellikleri devre dışı — uygulamayı OPENROUTER_API_KEY ortam değişkeniyle başlatın.");
    }
}
