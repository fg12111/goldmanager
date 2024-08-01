package com.my.goldmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.my.goldmanager.service.entity.KeyInfo;

@SpringBootTest
@ActiveProfiles("test")
public class InMemoryAuthKeyInfoServiceTest {

	@Autowired
	private InMemoryAuthKeyInfoService service;

	@AfterEach
	void cleanUp() {
		service.clearAll();
		service.setKeyExpiration(7201000);
		service.setKeyValidity(3600000);
	}

	@Test
	void testCleanupExpiredKeys() throws InterruptedException {
		service.setKeyExpiration(12000);
		service.setKeyValidity(-12002);
		KeyInfo ki1 = service.getKeyInfoForUserName("MyUser");
		KeyInfo ki2 = service.getKeyInfoForUserName("MyUser2");

		service.setKeyValidity(-12001);

		KeyInfo ki3 = service.getKeyInfoForUserName("MyUser");
		KeyInfo ki4 = service.getKeyInfoForUserName("MyUser2");
		service.setKeyValidity(60000);
		KeyInfo ki5 = service.getKeyInfoForUserName("MyUser");

		service.cleanUpExpiredKeyInfos();
		assertNotNull(service.getKeyforKeyId(ki5.getKeyId()));
		assertNull(service.getKeyforKeyId(ki4.getKeyId()));
		assertNull(service.getKeyforKeyId(ki3.getKeyId()));
		assertNull(service.getKeyforKeyId(ki2.getKeyId()));
		assertNull(service.getKeyforKeyId(ki1.getKeyId()));

	}
	@Test
	void testGetNewKeyAfterInvalidation() throws InterruptedException {
		service.setKeyExpiration(1);
		service.setKeyValidity(-50);
		KeyInfo ki = service.getKeyInfoForUserName("MyUser");
		assertNotNull(ki);

		KeyInfo ki2 = service.getKeyInfoForUserName("MyUser");
		assertNotNull(ki2);

		assertNotEquals(ki.getKeyId(), ki2.getKeyId());

		assertEquals(ki.getUsername(), ki2.getUsername());
		assertFalse(Arrays.equals(ki.getKey().getEncoded(), ki2.getKey().getEncoded()));
	}

	@Test
	void testGetNewKey() throws InterruptedException {
		service.setKeyExpiration(10);
		service.setKeyValidity(60000);
		KeyInfo ki = service.getKeyInfoForUserName("MyUser");
		assertNotNull(ki);

		KeyInfo ki2 = service.getKeyInfoForUserName("MyUser");
		assertNotNull(ki2);

		assertEquals(ki.getKeyId(), ki2.getKeyId());

		assertEquals(ki.getUsername(), ki2.getUsername());
		assertTrue(Arrays.equals(ki.getKey().getEncoded(), ki2.getKey().getEncoded()));
	}

}
