package kz.iitu.springlab.security;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public record DatabaseUserDetailsService(AppUserRepository users) implements UserDetailsService {
    public UserDetails loadUserByUsername(String username) {
        AppUser u = users.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
        return User.withUsername(u.getUsername()).password(u.passwordHash())
            .roles(u.getRoles().toArray(String[]::new)).disabled(!u.enabled()).build();
    }
}
