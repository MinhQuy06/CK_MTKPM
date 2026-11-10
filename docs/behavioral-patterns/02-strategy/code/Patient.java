import java.util.Objects;

/** Trang thai quyen loi gia lap da duoc xac minh phia server. */
public record Patient(String name, boolean insuranceEligible, boolean vipEligible) {
    public Patient {
        Objects.requireNonNull(name, "name");
    }
}
