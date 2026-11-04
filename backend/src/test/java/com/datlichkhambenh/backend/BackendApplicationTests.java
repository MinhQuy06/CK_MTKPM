package com.datlichkhambenh.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — kiểm tra Spring context load thành công.
 * Chạy với profile "test" để không cần DB/Redis thật.
 *
 * Logic nghiệp vụ sẽ được bổ sung ở bước tiếp theo.
 */
@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // Context load thành công là đủ ở giai đoạn này
    }
}
