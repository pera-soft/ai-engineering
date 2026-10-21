package tr.velzon.aiogretmen.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.stereotype.Component;

/**
 * Faz 9d (ADR-0014 §4): Spring Data JDBC tanımlayıcıları TIRNAKSIZ yazsın.
 *
 * <p>Varsayılan {@code forceQuote=true} açık {@code @Table("KULLANICI")} adını {@code "KULLANICI"} diye
 * tırnaklar. H2 tırnaksız adları büyük harfe katladığı için bu orada çalışır; PostgreSQL ise küçük harfe
 * katlar ve tırnaklı {@code "KULLANICI"} ≠ {@code kullanici} → "relation does not exist". Tırnaksız SQL'i
 * iki veritabanı da kendi kuralıyla katlar ve şemadaki (tırnaksız) adlarla eşleşir. Kolon adlarımız
 * ayrılmış sözcük değil (her iki veritabanında) — tırnağa ihtiyaç yok.
 *
 * <p>Eşleme bağlamı varlıkları {@code afterPropertiesSet}'te kurar; bu işlemci ondan ÖNCE çalışır.
 */
@Component
public class JdbcAdlandirma implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof RelationalMappingContext baglam) {
            baglam.setForceQuote(false);
        }
        return bean;
    }
}
