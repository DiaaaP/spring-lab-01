package kz.iitu.springlab.web;
import kz.iitu.springlab.aspect.CallCounterAspect;
import kz.iitu.springlab.service.CatalogService;
import org.springframework.web.bind.annotation.*;
import org.springframework.aop.support.AopUtils;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/lab4")
public class CatalogController {


    private final CatalogService catalogService;
    private final CallCounterAspect callCounterAspect;

    public CatalogController(CatalogService catalogService,
                             CallCounterAspect callCounterAspect) {
        this.catalogService = catalogService;
        this.callCounterAspect = callCounterAspect;
    }
    @GetMapping("/stats")
    public Map<String, Integer> statistics() {
        return callCounterAspect.getStatistics();
    }
    @GetMapping("/item/{id}")
    public String findById(@PathVariable long id) {
        return catalogService.findById(id);
    }
    @GetMapping("/remove-twice/{id}")
    public String removeTwice(@PathVariable long id) {
        return catalogService.removeTwice(id);
    }

    @GetMapping("/proxy")
    public Map<String, String> proxyInfo() {
        return Map.of(
                "className", catalogService.getClass().getName(),
                "superClass", catalogService.getClass().getSuperclass().getSimpleName(),
                "isAopProxy", String.valueOf(AopUtils.isAopProxy(catalogService)),
                "isCglib", String.valueOf(AopUtils.isCglibProxy(catalogService)));
    }
    @GetMapping("/items")
    public List<String> findAll(@RequestParam(defaultValue = "5") int limit) {
        return catalogService.findAll(limit);
    }

    @DeleteMapping("/item/{id}")
    public String remove(@PathVariable long id) {
        return catalogService.remove(id);
    }
}