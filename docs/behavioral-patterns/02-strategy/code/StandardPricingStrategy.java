import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class StandardPricingStrategy implements ConsultationPricingStrategy {
    public BigDecimal calculatePrice(Doctor doctor, Patient patient) {
        Objects.requireNonNull(doctor, "doctor");
        Objects.requireNonNull(patient, "patient");
        return doctor.consultationFee().setScale(0, RoundingMode.HALF_UP);
    }
}
