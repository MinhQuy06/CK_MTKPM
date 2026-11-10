import java.math.BigDecimal;
import java.util.Map;

public class PricingDemo {
    static void checkPrice(String expected, BigDecimal actual) {
        if (new BigDecimal(expected).compareTo(actual) != 0) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    static void expectRejected(String label, Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException ex) {
            System.out.println("PASS reject: " + label);
            return;
        }
        throw new AssertionError("Expected rejection: " + label);
    }

    public static void main(String[] args) {
        var context = new ConsultationPricingContext(Map.of(
                "STANDARD", new StandardPricingStrategy(),
                "INSURANCE", new InsurancePricingStrategy(),
                "VIP", new VipPricingStrategy()));
        var doctor = new Doctor(new BigDecimal("350000"));
        var eligible = new Patient("Demo patient", true, true);
        for (var policy : new String[]{"STANDARD", "INSURANCE", "VIP"}) {
            BigDecimal result = context.calculatePrice(policy, doctor, eligible);
            System.out.println(policy + " = " + result.toPlainString() + " VND");
            checkPrice(switch (policy) {
                case "INSURANCE" -> "70000";
                case "VIP" -> "297500";
                default -> "350000";
            }, result);
        }
        var ordinary = new Patient("Ordinary patient", false, false);
        expectRejected("unverified insurance", () -> context.calculatePrice("INSURANCE", doctor, ordinary));
        expectRejected("unverified VIP", () -> context.calculatePrice("VIP", doctor, ordinary));
        expectRejected("unknown policy", () -> context.calculatePrice("UNKNOWN", doctor, eligible));
        expectRejected("negative base price", () -> new Doctor(new BigDecimal("-1")));
        checkPrice("0", context.calculatePrice("STANDARD", new Doctor(BigDecimal.ZERO), ordinary));
        checkPrice("70000", context.calculatePrice("INSURANCE", new Doctor(new BigDecimal("350001")), eligible));
        checkPrice("297501", context.calculatePrice("VIP", new Doctor(new BigDecimal("350001")), eligible));

        // Them hanh vi moi qua dang ky, khong sua calculatePrice cua context.
        ConsultationPricingStrategy promotion = (d, p) -> d.consultationFee()
                .multiply(new BigDecimal("0.90")).setScale(0, java.math.RoundingMode.HALF_UP);
        var extended = new ConsultationPricingContext(Map.of("SUMMER", promotion));
        checkPrice("315000", extended.calculatePrice("SUMMER", doctor, eligible));
        System.out.println("PASS: prices, eligibility, rounding, zero, invalid input, extension.");
        System.out.println("Rates are fictional exercise assumptions; no real insurance calculation.");
    }
}
