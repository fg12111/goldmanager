package com.my.goldmanager.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.entity.Material;
import com.my.goldmanager.entity.MaterialHistory;
import com.my.goldmanager.repository.MaterialHistoryRepository;
import com.my.goldmanager.repository.MaterialRepository;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MaterialHistoryControllerSpringBootTest {

	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
	private MaterialRepository materialRepository;

	@Autowired
	private MaterialHistoryRepository materialHistoryRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@AfterEach
	public void cleanUp() {
		materialHistoryRepository.deleteAll();
		materialRepository.deleteAll();
		TestHTTPClient.cleanup();
	}

	@BeforeEach
	public void setUp() {
		TestHTTPClient.setup(userService, authenticationService);
		
		List<Material> materials = new LinkedList<Material>();
		Material gold = new Material();
		gold.setName("Gold");
		gold.setEntryDate(new Date());
		gold.setPrice(5000f);
		gold = materialRepository.save(gold);
		materials.add(gold);

		Material silver = new Material();
		silver.setEntryDate(new Date());
		silver.setPrice(500f);
		silver.setName("Silver");
		silver = materialRepository.save(silver);
		materials.add(silver);

		Material platinum = new Material();
		platinum.setEntryDate(new Date());
		platinum.setPrice(4000f);
		platinum.setName("Platinum");
		platinum = materialRepository.save(platinum);
		materials.add(platinum);
		materialRepository.flush();

		int historySize = 20;

		for (Material m : materials) {
			int number = 0;
			while (number < historySize) {
				MaterialHistory mh = new MaterialHistory();
				mh.setEntryDate(new Date(m.getEntryDate().toInstant().toEpochMilli() - (number + 1) * 1000));
				mh.setPrice(m.getPrice() - number + 1);
				mh.setMaterial(m);
				materialHistoryRepository.save(mh);
				number++;
			}
			historySize = historySize - 10;
		}
		materialHistoryRepository.flush();

	}

	@Test
	public void testList() throws Exception {

		String body = mockMvc.perform(TestHTTPClient.doGet("/materialHistory")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse()
				.getContentAsString();

		JsonNode node = objectMapper.readTree(body);
		assertFalse(node.isEmpty());
		assertEquals(materialHistoryRepository.count(), node.size());

		List<MaterialHistory> expectedMhs = materialHistoryRepository.findAll();
		int current = 0;
		while (current < node.size()) {
			MaterialHistory result = objectMapper.readValue(node.get(current).toString(), MaterialHistory.class);
			MaterialHistory expected = expectedMhs.get(current);

			assertEquals(expected.getId(), result.getId());
			assertEquals(expected.getMaterial().getId(), result.getMaterial().getId());
			assertEquals(expected.getPrice(), result.getPrice());
			assertEquals(expected.getEntryDate().toInstant().toEpochMilli(),
					result.getEntryDate().toInstant().toEpochMilli());

			current++;

		}

	}

	@Test
	public void testGetbyId() throws Exception {

		List<MaterialHistory> expectedMhs = materialHistoryRepository.findAll();
		for (MaterialHistory expected : expectedMhs) {
			String body = mockMvc.perform(TestHTTPClient.doGet("/materialHistory/" + expected.getId())).andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse()
					.getContentAsString();
			MaterialHistory result = objectMapper.readValue(body, MaterialHistory.class);
			assertEquals(expected.getId(), result.getId());
			assertEquals(expected.getMaterial().getId(), result.getMaterial().getId());
			assertEquals(expected.getPrice(), result.getPrice());
			assertEquals(expected.getEntryDate().toInstant().toEpochMilli(),
					result.getEntryDate().toInstant().toEpochMilli());

		}
	}

	@Test
	public void testGetbyIdNotFound() throws Exception {
		mockMvc.perform(TestHTTPClient.doGet("/materialHistory/unknownid")).andExpect(status().isNotFound());
	}

	@Test
	public void testGetByMaterial() throws Exception {

		List<Material> materials = materialRepository.findAll();
		for (Material material : materials) {

			String body = mockMvc.perform(TestHTTPClient.doGet("/materialHistory/byMaterial/" + material.getId()))
					.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn()
					.getResponse().getContentAsString();

			JsonNode node = objectMapper.readTree(body);
			List<MaterialHistory> expectedMhs = materialHistoryRepository.findByMaterial(material.getId());

			assertEquals(expectedMhs.size(), node.size());

			int current = 0;
			while (current < node.size()) {
				MaterialHistory result = objectMapper.readValue(node.get(current).toString(), MaterialHistory.class);
				MaterialHistory expected = expectedMhs.get(current);

				assertEquals(expected.getId(), result.getId());
				assertEquals(expected.getMaterial().getId(), result.getMaterial().getId());
				assertEquals(expected.getPrice(), result.getPrice());
				assertEquals(expected.getEntryDate().toInstant().toEpochMilli(),
						result.getEntryDate().toInstant().toEpochMilli());

				current++;

			}
		}

	}

	@Test
	public void deleteByIdNotFound() throws Exception {
		mockMvc.perform(TestHTTPClient.doDelete("/materialHistory/unknownid")).andExpect(status().isNotFound());
	}

	@Test
	public void testDeleteById() throws Exception {

		List<MaterialHistory> expectedMhs = materialHistoryRepository.findAll();
		for (MaterialHistory expected : expectedMhs) {
			mockMvc.perform(TestHTTPClient.doDelete("/materialHistory/" + expected.getId())).andExpect(status().isNoContent());
			assertFalse(materialHistoryRepository.existsById(expected.getId()));
		}
	}

	@Test
	public void testDeleteByMaterial() throws Exception {

		List<Material> materials = materialRepository.findAll();
		for (Material material : materials) {

			mockMvc.perform(TestHTTPClient.doDelete("/materialHistory/byMaterial/" + material.getId()))
					.andExpect(status().isNoContent());
			assertTrue(materialHistoryRepository.findByMaterial(material.getId()).isEmpty());
		}

	}
}