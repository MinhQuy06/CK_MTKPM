import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/** Context chi chon va uy quyen, khong chua cong thuc tinh gia. */
public final class ConsultationPricingContext {
    private final Map<String, ConsultationPricingStrategy> strategies;

    public ConsultationPricingContext(Map<String, ConsultationPricingStrategy> strategies) {
        this.strategies = Map.copyOf(strategies);
    }

    /** policy la ket qua lua chon phia server, khong tin truc tiep gia/quyen loi tu client. */
    public BigDecimal calculatePrice(String policy, Doctor doctor, Patient patient) {
        Objects.requireNonNull(policy, "policy");
        Objects.requireNonNull(doctor, "doctor");
        Objects.requireNonNull(patient, "patient");
        ConsultationPricingStrategy strategy = strategies.get(policy);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported pricing policy: " + policy);
        }
        return strategy.calculatePrice(doctor, patient);
    }
}
