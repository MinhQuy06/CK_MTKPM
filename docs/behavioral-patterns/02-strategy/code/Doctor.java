import java.math.BigDecimal;
import java.util.Objects;

/** Du lieu demo, khong phai entity JPA cua du an. */
public record Doctor(BigDecimal consultationFee) {
    public Doctor {
        Objects.requireNonNull(consultationFee, "consultationFee");
        if (consultationFee.signum() < 0) {
            throw new IllegalArgumentException("Fee must be non-negative");
        }
    }
}
