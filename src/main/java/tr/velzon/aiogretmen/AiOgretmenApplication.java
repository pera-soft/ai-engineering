package tr.velzon.aiogretmen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.Locale;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiOgretmenApplication {

    // Sınıf yüklenirken çalışır: main() (IntelliJ, spring-boot:run, java -jar) ve @SpringBootTest bağlamları.
    static {
        temelLocaleyiSabitle();
    }

    public static void main(String[] args) {
        SpringApplication.run(AiOgretmenApplication.class, args);
    }

    /**
     * Spring Data JDBC tanımlayıcıların harf boyutunu locale vermeden {@code toUpperCase()}/{@code toLowerCase()}
     * ile katlar; tr_TR JVM'de {@code "id"} → {@code "İD"} olur ve H2 "Column ... İD not found" verir.
     * Temel locale {@link Locale#ROOT}'a çekilir; FORMAT/DISPLAY kategorileri sistemin locale'inde kalır,
     * böylece kullanıcıya dönük biçimlendirme değişmez.
     */
    static void temelLocaleyiSabitle() {
        Locale bicim = Locale.getDefault(Locale.Category.FORMAT);
        Locale gorunum = Locale.getDefault(Locale.Category.DISPLAY);
        Locale.setDefault(Locale.ROOT);
        Locale.setDefault(Locale.Category.FORMAT, bicim);
        Locale.setDefault(Locale.Category.DISPLAY, gorunum);
    }
}
