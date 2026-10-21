package tr.velzon.aiogretmen.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Faz 9d (ADR-0014 §2): {@code site} profili veritabanını YALNIZ açıkça verilen
 * {@code SPRING_DATASOURCE_URL}'den alır. Verilmezse uygulama — hiçbir bean kurulmadan — anlaşılır bir
 * mesajla açılmaz (Boot çözülemeyen yer tutucuyu düz metin bırakıp "url must start with jdbc" derdi).
 * Böylece canlı sessizce yerel H2 dosyasına düşemez.
 */
@Component
@Profile("site")
public class SiteVeritabaniDenetimi implements BeanFactoryPostProcessor, EnvironmentAware {

    private Environment ortam;

    @Override
    public void setEnvironment(Environment environment) {
        this.ortam = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        String url;
        try {
            url = ortam.getProperty("spring.datasource.url");
        } catch (IllegalArgumentException cozulemedi) {
            url = null;
        }
        if (url == null || !url.startsWith("jdbc:")) {
            throw new IllegalStateException("site profili veritabanı adresini ister: SPRING_DATASOURCE_URL "
                    + "(ör. jdbc:postgresql://127.0.0.1:5432/ai_ogretmen) + SPRING_DATASOURCE_USERNAME/PASSWORD "
                    + "ortam değişkenlerini ver (ADR-0014; kurulum: docs/kurulum-site.md).");
        }
    }
}
