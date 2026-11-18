package kz.iitu.springlab.security;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public record SecurityPages(AppUserRepository users) {
    @GetMapping("/login") public String login(){ return "login"; }
    @GetMapping("/admin") public String admin(Model model){
        model.addAttribute("users",users.findAll());
        return "admin";
    }
}
