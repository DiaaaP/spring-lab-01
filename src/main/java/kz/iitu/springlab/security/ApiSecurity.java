package kz.iitu.springlab.security;
import javax.crypto.*;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class ApiSecurity {
    @Bean SecretKey signingKey() throws Exception {
        var generator=KeyGenerator.getInstance("HmacSHA256");
        generator.init(256);
        return generator.generateKey();
    }
    @Bean JwtDecoder jwtDecoder(SecretKey key){return NimbusJwtDecoder.withSecretKey(key).build();}
    @Bean JwtEncoder jwtEncoder(SecretKey key){
        return new NimbusJwtEncoder(new com.nimbusds.jose.jwk.source.ImmutableSecret<>(key));
    }
    @Bean @Order(1) SecurityFilterChain api(HttpSecurity http) throws Exception {
        return http.securityMatcher("/api/books","/api/books/**","/auth/login")
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(c->c.disable())
            .authorizeHttpRequests(a->a.requestMatchers(HttpMethod.POST,"/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/books","/api/books/**").hasAuthority("SCOPE_books:read")
                .requestMatchers("/api/books","/api/books/**").hasAuthority("SCOPE_books:write").anyRequest().denyAll())
            .oauth2ResourceServer(o->o.jwt(withDefaults())).build();
    }
}
