package tr.velzon.aiogretmen.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Faz 2a-2 güvenlik sözleşmesi (TDD): {@code mcp-http} profilinde /mcp bearer JWT ile korunur.
 * <ul>
 *   <li>Token'sız istek → 401 (BearerResourceMetadataTokenAuthenticationEntryPoint).</li>
 *   <li>Geçerli JWT (mockJwt — decoder bypass) → 200; MCP initialize yanıtı döner.</li>
 * </ul>
 * mockJwt() gerçek JwtDecoder'ı atlar → placeholder issuer'a ağ çağrısı YOK.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:mcpauth;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza",
        "app.rapor-dir=target/test-raporlar",
        // LAZY decoder → açılışta ağ çağrısı yok (mockJwt decoder'ı zaten bypass eder)
        "mcp.oauth2.jwk-set-uri=http://127.0.0.1/jwks-test"
})
@ActiveProfiles("mcp-http")
@AutoConfigureMockMvc
class McpHttpGuvenlikTest {

    private static final String INITIALIZE = """
            {"jsonrpc":"2.0","id":1,"method":"initialize","params":{\
            "protocolVersion":"2025-06-18","capabilities":{},\
            "clientInfo":{"name":"test","version":"0"}}}""";

    @Autowired
    MockMvc mvc;

    @Test
    void tokensizMcpIstegi401() throws Exception {
        mvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .content(INITIALIZE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void gecerliJwtIleMcpErisilir() throws Exception {
        mvc.perform(post("/mcp")
                        .with(jwt().jwt(j -> j.claim("email", "ogrenci@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .content(INITIALIZE))
                .andExpect(status().isOk());
    }
}
