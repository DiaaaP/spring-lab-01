package kz.iitu.springlab.security;
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name="app_user")
public class AppUser {
    @Id private Long id;
    private String username;
    @Column(name="password_hash") private String passwordHash;
    private boolean enabled;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="user_role", joinColumns=@JoinColumn(name="user_id"))
    @Column(name="role") private Set<String> roles;
    public String getUsername(){return username;}
    String passwordHash(){return passwordHash;}
    boolean enabled(){return enabled;}
    public Set<String> getRoles(){return roles;}
}
