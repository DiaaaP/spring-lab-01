package kz.iitu.springlab.web;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.http.MediaType;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Testcontainers(disabledWithoutDocker=true)
class BookApiIntegrationTest {
    @Container @ServiceConnection static final PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17-alpine");
    @Autowired RestTestClient client;
    @Test void createsAndReadsBookThroughAuthenticatedApi(){
        var login=client.post().uri("/auth/login").contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("username","admin","password","admin123")).exchange().expectStatus().isOk()
            .expectBody(Map.class).returnResult().getResponseBody();
        assertThat(login).containsKey("access_token");
        String bearer="Bearer "+login.get("access_token");
        var created=client.post().uri("/api/books").header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("title","Dune","author","Herbert","year",1965)).exchange().expectStatus().isCreated()
            .returnResult(Void.class);
        var location=created.getResponseHeaders().getLocation();
        assertThat(location).isNotNull();
        client.get().uri(location.toString()).header("Authorization",bearer).exchange().expectStatus().isOk()
            .expectBody().jsonPath("$.title").isEqualTo("Dune");
    }
}
