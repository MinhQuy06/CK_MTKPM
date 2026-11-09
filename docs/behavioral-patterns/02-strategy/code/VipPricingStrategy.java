import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Giam 15% la gia dinh minh hoa cua bai tap. */
public final class VipPricingStrategy implements ConsultationPricingStrategy {
    public BigDecimal calculatePrice(Doctor doctor, Patient patient) {
        Objects.requireNonNull(doctor, "doctor");
        Objects.requireNonNull(patient, "patient");
        if (!patient.vipEligible()) {
            throw new IllegalArgumentException("Patient is not VIP eligible");
        }
        return doctor.consultationFee().multiply(new BigDecimal("0.85"))
                .setScale(0, RoundingMode.HALF_UP);
    }
}
