package com.calisthenics.calisthenics;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "jwt.secret=test-secret-not-real-0123456789-abcdefghijkl",
        "app.seed.enabled=false"
})
class CalisthenicsApplicationTests {

    @Test
    void contextLoads() {
    }

}
