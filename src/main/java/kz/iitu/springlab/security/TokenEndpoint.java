package kz.iitu.springlab.security;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.time.Instant;
import java.util.Map;
@RestController
public class TokenEndpoint {
    private final DaoAuthenticationProvider provider;
    private final JwtEncoder encoder;
    public TokenEndpoint(UserDetailsService users,PasswordEncoder passwords,JwtEncoder encoder){
        provider=new DaoAuthenticationProvider(users);provider.setPasswordEncoder(passwords);this.encoder=encoder;
    }
    record Credentials(String username,String password){}
    @PostMapping(path="/auth/login",consumes="application/json")
    ResponseEntity<?> login(@RequestBody Credentials credentials){
        Authentication a;
        try { a=provider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(credentials.username(),credentials.password())); }
        catch(AuthenticationException ex){return ResponseEntity.status(401).build();}
        boolean admin=a.getAuthorities().stream().anyMatch(g->g.getAuthority().equals("ROLE_ADMIN"));
        var now=Instant.now();
        var claims=JwtClaimsSet.builder().subject(a.getName()).issuedAt(now).expiresAt(now.plusSeconds(900))
            .claim("scope",admin?"books:read books:write":"books:read").build();
        var jwt=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("access_token",jwt.getTokenValue()));
    }
}
