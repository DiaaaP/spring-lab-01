public ChangeResult updateName(long id, String newValue) {
    String oldValue = "Item no. " + id;
    return new ChangeResult(oldValue, newValue);
}

public record ChangeResult(String oldValue, String newValue) {}

void main() {
}

