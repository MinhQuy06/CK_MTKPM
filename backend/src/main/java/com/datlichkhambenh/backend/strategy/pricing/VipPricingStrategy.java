package com.datlichkhambenh.backend.strategy.pricing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
@Component
public class VipPricingStrategy implements ConsultationPricingStrategy {
 private final BigDecimal rate;
 public VipPricingStrategy(@Value("${app.pricing.vip-discount-rate:0.1}") BigDecimal rate) {
  if(rate==null||rate.signum()<0||rate.compareTo(BigDecimal.ONE)>0)throw new IllegalArgumentException("Tỷ lệ giảm phải trong [0,1]");
  this.rate=rate;
 }
 public String key(){return "VIP";}
 public String name(){return "VIP";}
 public BigDecimal discountRate(){return rate;}
 public boolean simulated(){return false;}
 public BigDecimal calculate(BigDecimal basePrice){return basePrice.multiply(BigDecimal.ONE.subtract(rate));}
}
