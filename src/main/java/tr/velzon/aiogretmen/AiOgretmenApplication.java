package tr.velzon.aiogretmen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiOgretmenApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiOgretmenApplication.class, args);
    }
}
