package tr.velzon.aiogretmen;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import tr.velzon.aiogretmen.config.LlmKullanan;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Mimari kurallar mekanik olarak burada zorlanır (bkz ARCHITECTURE.md).
 * Kural ihlali = build kırmızı. Bu test CI'da bloklayıcıdır.
 */
@AnalyzeClasses(packages = "tr.velzon.aiogretmen")
class MimariTestleri {

    /**
     * Katman yönü: types → config → repo → service → ai → {web, mcp} (tek yönlü).
     * Web ve Mcp aynı seviyede dış-arayüz adaptörleridir; ikisi de yalnız service (ve altını) kullanır.
     * Mcp katmanı LLM'siz kalır: {@link #claude_yalniz_ai_paketinden} MCP adaptörünün Spring AI'yı
     * import etmesini yasaklar → muhakeme bağlanan istemcide (ADR-0007).
     * [Guidance: Bu kural kırıldıysa bağımlılığı ters çevirin veya işi doğru katmana
     * taşıyın — kuralı gevşetmeyin. Gerçekten yeni bir katman gerekiyorsa önce ADR açın.]
     */
    @ArchTest
    static final ArchRule katman_kurali = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .withOptionalLayers(false) // tüm katmanlar dolu; paket yeniden adlandırılırsa
            // katman boş kalır ve build KIRMIZI olur (GYHDCA Bölüm 12: sessiz geçen
            // fitness function işe yaramaz — refactoring-güvenliği)
            .layer("Types").definedBy("..aiogretmen.types..")
            .layer("Config").definedBy("..aiogretmen.config..")
            .layer("Repo").definedBy("..aiogretmen.repo..")
            .layer("Service").definedBy("..aiogretmen.service..")
            .layer("Ai").definedBy("..aiogretmen.ai..")
            .layer("Web").definedBy("..aiogretmen.web..")
            .layer("Mcp").definedBy("..aiogretmen.mcp..")
            .whereLayer("Web").mayNotBeAccessedByAnyLayer()
            .whereLayer("Mcp").mayNotBeAccessedByAnyLayer()
            .whereLayer("Ai").mayOnlyBeAccessedByLayers("Service")
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Web", "Mcp")
            .whereLayer("Repo").mayOnlyBeAccessedByLayers("Service", "Ai")
            .whereLayer("Config").mayOnlyBeAccessedByLayers("Repo", "Service", "Ai", "Web", "Mcp")
            .whereLayer("Types").mayOnlyBeAccessedByLayers("Config", "Repo", "Service", "Ai", "Web", "Mcp");

    @ArchTest
    static final ArchRule dongusuz = slices()
            .matching("tr.velzon.aiogretmen.(*)..")
            .should().beFreeOfCycles();

    /**
     * Claude/Spring AI çağrısı tek çıkış noktasından: yalnızca ai/ paketi.
     * [Guidance: AI'a ihtiyaç duyan servis, ai/ paketindeki arayüz üzerinden erişmeli.]
     */
    @ArchTest
    static final ArchRule claude_yalniz_ai_paketinden = noClasses()
            .that().resideOutsideOfPackage("..aiogretmen.ai..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework.ai..", "com.anthropic..");

    /**
     * MCP LLM-siz mührü (ADR-0009): {@code mcp/} paketi LLM çağıran ({@code @LlmKullanan}) hiçbir
     * servise bağımlı olamaz. Böylece "muhakeme bağlanan istemcide, sunucu MCP'de token yok"
     * (ADR-0007) konvansiyondan mekanik kurala döner. mcp'nin katalog gibi token-SIZ okumaları
     * işaretsiz servislerden (ör. {@code AtolyeKatalogService}) verilir.
     * [Guidance: Bu kural kırıldıysa mcp'nin ihtiyacını token-siz bir servis seam'inden karşıla —
     * @LlmKullanan servisi mcp'ye bağlama.]
     */
    @ArchTest
    static final ArchRule mcp_llmsiz = noClasses()
            .that().resideInAPackage("..aiogretmen.mcp..")
            .should().dependOnClassesThat().areAnnotatedWith(LlmKullanan.class);

    /**
     * types hiçbir uygulama katmanına bağımlı olamaz (en alt katman).
     */
    @ArchTest
    static final ArchRule types_bagimsiz = noClasses()
            .that().resideInAPackage("..aiogretmen.types..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..aiogretmen.config..", "..aiogretmen.repo..",
                    "..aiogretmen.service..", "..aiogretmen.ai..", "..aiogretmen.web..",
                    "..aiogretmen.mcp..");
}
