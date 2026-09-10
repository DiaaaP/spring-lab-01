package kz.iitu.springlab.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Value("${app.owner:unknown}")
    private String owner;

    @GetMapping("/hello")
    public Greeting hello(@RequestParam(defaultValue = "world") String name) {
        return new Greeting("Hello, " + name + "!", owner, LocalDateTime.now());
    }

    @GetMapping("/info")
    public Info info() {
        return new Info(
                owner,
                System.getProperty("java.version"),
                Runtime.getRuntime().availableProcessors()
        );
    }
    //Individual Assignment. Task 1
    @GetMapping("/sum")
    public Map<String, Integer> sum(
            @RequestParam int a,
            @RequestParam int b) {

        return Map.of(
                "sum", a + b,
                "difference", a - b,
                "product", a * b
        );
    }

    //Individual Assignment. Task 2
    @GetMapping("/reverse")
    public Map<String, Object> reverse(
            @RequestParam(defaultValue = "") String text) {

        String reversed = new StringBuilder(text).reverse().toString();

        return Map.of(
                "reversed", reversed,
                "length", text.length()
        );
    }

    //Individual Assignment. Task 3
    @GetMapping("/palindrome")
    public Map<String, Object> palindrome(
            @RequestParam(defaultValue = "") String text) {

        String clean = text.replaceAll("\\s+", "").toLowerCase();
        String reversed = new StringBuilder(clean).reverse().toString();

        return Map.of(
                "text", text,
                "palindrome", clean.equals(reversed)
        );
    }

    //Individual Assignment. Task 4
    @GetMapping("/factorial")
    public Map<String, Object> factorial(@RequestParam int n) {

        if (n < 0 || n > 20) {
            return Map.of(
                    "error", "n must be between 0 and 20"
            );
        }

        long result = 1;

        for (int i = 2; i <= n; i++) {
            result *= i;
        }

        return Map.of(
                "n", n,
                "factorial", result
        );
    }

    //Individual Assignment. Task 5
    @GetMapping("/temperature")
    public Map<String, Double> temperature(@RequestParam double f) {

        double celsius = (f - 32) * 5 / 9;
        double kelvin = celsius + 273.15;

        return Map.of(
                "fahrenheit", f,
                "celsius", celsius,
                "kelvin", kelvin
        );
    }

    //Individual Assignment. Task 6
    @GetMapping("/stats")
    public Map<String, Double> stats(@RequestParam String numbers) {

        String[] parts = numbers.split(",");

        double min = Double.parseDouble(parts[0]);
        double max = min;
        double sum = 0;

        for (String part : parts) {
            double number = Double.parseDouble(part.trim());

            if (number < min) {
                min = number;
            }

            if (number > max) {
                max = number;
            }

            sum += number;
        }

        double average = sum / parts.length;

        return Map.of(
                "min", min,
                "max", max,
                "average", average
        );
    }

    //Individual Assignment. Task 7
    @GetMapping("/wordcount")
    public Map<String, Object> wordcount(
            @RequestParam(defaultValue = "") String text) {

        String trimmed = text.trim();

        if (trimmed.isEmpty()) {
            return Map.of(
                    "words", 0,
                    "characters", 0,
                    "longestWord", ""
            );
        }

        String[] words = trimmed.split("\\s+");

        String longestWord = "";

        for (String word : words) {
            if (word.length() > longestWord.length()) {
                longestWord = word;
            }
        }

        return Map.of(
                "words", words.length,
                "characters", text.length(),
                "longestWord", longestWord
        );
    }

    //Individual Assignment. Task 8
    @GetMapping("/case")
    public Map<String, String> textCase(
            @RequestParam(defaultValue = "") String text) {

        String[] words = text.toLowerCase().split(" ");
        String title = "";

        for (String word : words) {
            if (!word.isEmpty()) {
                title += Character.toUpperCase(word.charAt(0))
                        + word.substring(1) + " ";
            }
        }

        title = title.trim();

        return Map.of(
                "upper", text.toUpperCase(),
                "lower", text.toLowerCase(),
                "title", title
        );
    }

    //Individual Assignment. Task 9
    @GetMapping("/fibonacci")
    public Map<String, Object> fibonacci(@RequestParam int n) {

        if (n < 1 || n > 50) {
            return Map.of(
                    "error", "n must be between 1 and 50"
            );
        }

        List<Long> numbers = new ArrayList<>();

        long a = 0;
        long b = 1;

        for (int i = 0; i < n; i++) {
            numbers.add(a);

            long next = a + b;
            a = b;
            b = next;
        }

        return Map.of(
                "fibonacci", numbers
        );
    }

    //Individual Assignment. Task 10
    @GetMapping("/prime")
    public Map<String, Object> prime(@RequestParam int n) {

        List<Integer> divisors = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            if (n % i == 0) {
                divisors.add(i);
            }
        }

        boolean isPrime = n > 1 && divisors.size() == 2;

        return Map.of(
                "number", n,
                "prime", isPrime,
                "divisors", divisors
        );
    }

    //Individual Assignment. Task 11
    @GetMapping("/time")
    public Map<String, String> time(
            @RequestParam(defaultValue = "UTC") String zone) {

        ZoneId zoneId = ZoneId.of(zone);
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        return Map.of(
                "zone", zone,
                "time", now.toLocalDateTime().toString(),
                "offset", now.getOffset().toString()
        );
    }

    //Individual Assignment. Task 12
    @GetMapping("/bmi")
    public Map<String, Object> bmi(
            @RequestParam double weight,
            @RequestParam double height) {

        double bmi = weight / (height * height);

        String category;

        if (bmi < 18.5) {
            category = "Underweight";
        } else if (bmi < 25) {
            category = "Normal";
        } else if (bmi < 30) {
            category = "Overweight";
        } else {
            category = "Obesity";
        }

        return Map.of(
                "bmi", bmi,
                "category", category
        );
    }

    public record Greeting(String message, String owner, LocalDateTime timestamp) { }

    public record Info(String owner, String javaVersion, int cpuCores) { }
}
