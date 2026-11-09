import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** He so 0.20 chi la gia dinh bai tap, khong phai quy dinh BHYT thuc te. */
public final class InsurancePricingStrategy implements ConsultationPricingStrategy {
    public BigDecimal calculatePrice(Doctor doctor, Patient patient) {
        Objects.requireNonNull(doctor, "doctor");
        Objects.requireNonNull(patient, "patient");
        if (!patient.insuranceEligible()) {
            throw new IllegalArgumentException("Patient is not insurance eligible");
        }
        return doctor.consultationFee().multiply(new BigDecimal("0.20"))
                .setScale(0, RoundingMode.HALF_UP);
    }
}
