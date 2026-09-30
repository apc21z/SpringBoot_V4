package com.apc21z.proyectv4;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "security.jwt.secret=test-secret-for-v4-integration-tests-only-123456")
class ProyectV4ApplicationTests {

	@Test
	void contextLoads() {
	}

}