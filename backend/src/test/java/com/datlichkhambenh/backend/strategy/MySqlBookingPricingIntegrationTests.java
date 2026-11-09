package com.datlichkhambenh.backend.strategy;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest(properties={
 "spring.datasource.url=${CK_MYSQL_URL}",
 "spring.datasource.username=root",
 "spring.datasource.password=ck-test-only",
 "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
 "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect",
 "spring.jpa.hibernate.ddl-auto=create-drop"
})
@EnabledIfEnvironmentVariable(named="CK_MYSQL_URL",matches=".+")
class MySqlBookingPricingIntegrationTests extends BookingPricingContract {}
