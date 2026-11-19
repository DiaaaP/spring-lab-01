package kz.iitu.springlab.web;
import kz.iitu.springlab.catalog.*;
import kz.iitu.springlab.security.ApiSecurity;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(BookRestController.class)
@Import(ApiSecurity.class)
class BookControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean BookService service;
    @Test void existingBookExposesTitle() throws Exception {
        when(service.findById(7)).thenReturn(Optional.of(new Book(7L,"Dune","Herbert",1965)));
        mvc.perform(get("/api/books/7").with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:read"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Dune"));
    }
    @Test void missingBookHasProblemDetail() throws Exception {
        when(service.findById(7)).thenReturn(Optional.empty());
        mvc.perform(get("/api/books/7").with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:read"))))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.detail").value("Book 7 was not found"));
    }
    @Test void blankTitleIsRejected() throws Exception {
        mvc.perform(post("/api/books").with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_books:write")))
            .contentType("application/json").content("{\"title\":\" \",\"author\":\"Herbert\",\"year\":1965}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
