import java.math.BigDecimal;

/** Thiet ke de xuat, doc lap voi backend. */
public interface ConsultationPricingStrategy {
    BigDecimal calculatePrice(Doctor doctor, Patient patient);
}
