package com.my.goldmanager.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.entity.Item;
import com.my.goldmanager.entity.ItemType;
import com.my.goldmanager.entity.Material;
import com.my.goldmanager.entity.MaterialHistory;
import com.my.goldmanager.entity.Unit;
import com.my.goldmanager.repository.ItemRepository;
import com.my.goldmanager.repository.ItemTypeRepository;
import com.my.goldmanager.repository.MaterialHistoryRepository;
import com.my.goldmanager.repository.MaterialRepository;
import com.my.goldmanager.repository.UnitRepository;
import com.my.goldmanager.rest.entity.Price;
import com.my.goldmanager.rest.entity.PriceHistory;
import com.my.goldmanager.rest.entity.PriceHistoryList;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PriceHistoryControllerSpringBootTest {

	private static final SecureRandom rand = new SecureRandom();

	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
			.withZone(ZoneId.of("UTC"));
	
	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationService authenticationService;

	
	@Autowired
	private UnitRepository unitRepository;

	@Autowired
	private MaterialRepository materialRepository;

	@Autowired
	private MaterialHistoryRepository materialHistoryRepository;

	@Autowired
	private ItemTypeRepository itemTypeRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@AfterEach
	public void cleanUp() {
		itemRepository.deleteAll();
		itemTypeRepository.deleteAll();
		unitRepository.deleteAll();
		materialHistoryRepository.deleteAll();
		materialRepository.deleteAll();
		TestHTTPClient.cleanup();
	}

	@BeforeEach
	public void setUp() {

		TestHTTPClient.setup(userService, authenticationService);
		
		Unit oz = new Unit();

		oz.setFactor(1.0f);
		oz.setName("Oz");
		unitRepository.save(oz);

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
		int numberOftems = 20;
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

			ItemType itemType = new ItemType();
			itemType.setMaterial(m);
			itemType.setModifier(1.0f);
			itemType.setName(m.getName() + " type");

			itemType = itemTypeRepository.save(itemType);
			for (int i = 0; i < numberOftems; i++) {
				Item item = new Item();
				item.setName(m.getName() + " Item " + i);
				item.setAmount(rand.nextFloat(0.5f, 5.1f) + 0.1f);
				item.setItemType(itemType);
				item.setUnit(oz);
				itemRepository.save(item);
			}

			numberOftems = numberOftems - 10;
			historySize = historySize - 10;

		}
		materialHistoryRepository.flush();

	}

	@Test
	public void testListAllforMaterialWithStartdate() throws Exception {
		List<Material> materials = materialRepository.findAll();
		testForStartOrEndDate(materials, true);

	}

	@Test
	public void testListAllforMaterialWithEndDate() throws Exception {
		List<Material> materials = materialRepository.findAll();
		testForStartOrEndDate(materials, false);

	}
	

	@Test
	public void testListAllforMaterialInRange() throws Exception {
		List<Material> materials = materialRepository.findAll();
		testForStartAndEndDate(materials);
		testForStartAndEndDateReversed(materials);

	}

	@Test
	public void testInvalidDate() throws Exception {
		Material material = materialRepository.findAll().getFirst();
		Date endDate = new Date();
		Date startDate = new Date(System.currentTimeMillis() + 5000);

		String body = mockMvc
				.perform(TestHTTPClient.doGet("/priceHistory/" + material.getId() + "?" + "startDate="
						+ dateFormatter.format(startDate.toInstant()) + "&endDate="
						+ dateFormatter.format(endDate.toInstant())))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();
		ErrorResponse response = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals(400, response.getStatus());
		assertEquals("startDate must be before or equal to endDate", response.getMessage());
	}

	private void testForStartAndEndDate(List<Material> materials)
			throws UnsupportedEncodingException, Exception, JsonProcessingException, JsonMappingException {
		for (Material material : materials) {

			List<MaterialHistory> expectedMhs = materialHistoryRepository.findByMaterial(material.getId()).reversed();
		
			for (int currentMHNo = 0; currentMHNo < expectedMhs.size(); currentMHNo++) {

				MaterialHistory currentMH = expectedMhs.get(currentMHNo);
				MaterialHistory lastMH = expectedMhs.getLast();
				String body = mockMvc
						.perform(TestHTTPClient.doGet("/priceHistory/" + material.getId() + "?startDate=" + 
							 dateFormatter.format(currentMH.getEntryDate().toInstant())+"&endDate="+dateFormatter.format(lastMH.getEntryDate().toInstant())))
						.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
						.andReturn().getResponse().getContentAsString();

				PriceHistoryList priceHistoryList = objectMapper.readValue(body, PriceHistoryList.class);
				assertEquals(expectedMhs.size() - currentMHNo, priceHistoryList.getPriceHistories().size());

				List<Item> items = itemRepository.findByMaterialId(material.getId());
				int current = 0;
				List<PriceHistory> priceHistories = priceHistoryList.getPriceHistories().reversed();
				
				while (current < priceHistories.size()) {

					MaterialHistory mh = expectedMhs.get(current + currentMHNo);
					PriceHistory ph = priceHistories.get(current);

					assertEquals(mh.getEntryDate().toInstant().toEpochMilli(), ph.getDate().toInstant().toEpochMilli());

					float totalPrice = getPriceSummary(items, mh.getPrice());

					assertNotNull(ph.getPriceList());
					assertEquals(items.size(), ph.getPriceList().getPrices().size());
					assertEquals(totalPrice, ph.getPriceList().getTotalPrize());
					for (int currentPrice = 0; currentPrice < items.size(); currentPrice++) {
						Price actualPrice = ph.getPriceList().getPrices().get(currentPrice);
						assertEquals(getPrice(items.get(currentPrice), mh.getPrice()), actualPrice.getPrice());
					}
					current++;
				}
			}
		}
	}

	private void testForStartAndEndDateReversed(List<Material> materials)
			throws UnsupportedEncodingException, Exception, JsonProcessingException, JsonMappingException {
		for (Material material : materials) {

			List<MaterialHistory> expectedMhs = materialHistoryRepository.findByMaterial(material.getId()).reversed();
		
			for (int currentMHNo = 0; currentMHNo < expectedMhs.size(); currentMHNo++) {

				MaterialHistory firstMH = expectedMhs.getFirst();
				MaterialHistory currentMH = expectedMhs.get(currentMHNo);
				String body = mockMvc
						.perform(TestHTTPClient.doGet("/priceHistory/" + material.getId() + "?startDate=" + 
							 dateFormatter.format(firstMH.getEntryDate().toInstant())+"&endDate="+dateFormatter.format(currentMH.getEntryDate().toInstant())))
						.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
						.andReturn().getResponse().getContentAsString();

				PriceHistoryList priceHistoryList = objectMapper.readValue(body, PriceHistoryList.class);
				assertEquals(currentMHNo+1, priceHistoryList.getPriceHistories().size());

				List<Item> items = itemRepository.findByMaterialId(material.getId());
				int current = 0;
				List<PriceHistory> priceHistories = priceHistoryList.getPriceHistories().reversed();
				
				while (current < priceHistories.size()) {

					MaterialHistory mh = expectedMhs.get(current);
					PriceHistory ph = priceHistories.get(current);

					assertEquals(mh.getEntryDate().toInstant().toEpochMilli(), ph.getDate().toInstant().toEpochMilli());

					float totalPrice = getPriceSummary(items, mh.getPrice());

					assertNotNull(ph.getPriceList());
					assertEquals(items.size(), ph.getPriceList().getPrices().size());
					assertEquals(totalPrice, ph.getPriceList().getTotalPrize());
					for (int currentPrice = 0; currentPrice < items.size(); currentPrice++) {
						Price actualPrice = ph.getPriceList().getPrices().get(currentPrice);
						assertEquals(getPrice(items.get(currentPrice), mh.getPrice()), actualPrice.getPrice());
					}
					current++;
				}
			}
		}
	}
	private void testForStartOrEndDate(List<Material> materials, boolean byStartdate)
			throws UnsupportedEncodingException, Exception, JsonProcessingException, JsonMappingException {
		for (Material material : materials) {

			List<MaterialHistory> expectedMhs = materialHistoryRepository.findByMaterial(material.getId());
			if (!byStartdate) {
				expectedMhs = expectedMhs.reversed();
			}
			for (int currentMHNo = 0; currentMHNo < expectedMhs.size(); currentMHNo++) {

				MaterialHistory currentmh = expectedMhs.get(currentMHNo);

				String body = mockMvc
						.perform(TestHTTPClient.doGet("/priceHistory/" + material.getId() + "?" + (byStartdate ? "startDate" : "endDate")
								+ "=" + dateFormatter.format(currentmh.getEntryDate().toInstant())))
						.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
						.andReturn().getResponse().getContentAsString();

				PriceHistoryList priceHistoryList = objectMapper.readValue(body, PriceHistoryList.class);
				assertEquals(expectedMhs.size() - currentMHNo, priceHistoryList.getPriceHistories().size());

				List<Item> items = itemRepository.findByMaterialId(material.getId());
				int current = 0;
				List<PriceHistory> priceHistories = priceHistoryList.getPriceHistories();
				if (!byStartdate) {
					priceHistories = priceHistories.reversed();
				}
				while (current < priceHistories.size()) {

					MaterialHistory mh = expectedMhs.get(current + currentMHNo);
					PriceHistory ph = priceHistories.get(current);

					assertEquals(mh.getEntryDate().toInstant().toEpochMilli(), ph.getDate().toInstant().toEpochMilli());

					float totalPrice = getPriceSummary(items, mh.getPrice());

					assertNotNull(ph.getPriceList());
					assertEquals(items.size(), ph.getPriceList().getPrices().size());
					assertEquals(totalPrice, ph.getPriceList().getTotalPrize());
					for (int currentPrice = 0; currentPrice < items.size(); currentPrice++) {
						Price actualPrice = ph.getPriceList().getPrices().get(currentPrice);
						assertEquals(getPrice(items.get(currentPrice), mh.getPrice()), actualPrice.getPrice());
					}
					current++;
				}
			}
		}
	}

	@Test
	public void testListAllforMaterial() throws Exception {
		List<Material> materials = materialRepository.findAll();
		for (Material material : materials) {

			String body = mockMvc.perform(TestHTTPClient.doGet("/priceHistory/" + material.getId())).andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse()
					.getContentAsString();

			PriceHistoryList priceHistoryList = objectMapper.readValue(body, PriceHistoryList.class);

			List<MaterialHistory> expectedMhs = materialHistoryRepository.findByMaterial(material.getId());

			assertEquals(expectedMhs.size(), priceHistoryList.getPriceHistories().size());

			List<Item> items = itemRepository.findByMaterialId(material.getId());
			int current = 0;
			while (current < priceHistoryList.getPriceHistories().size()) {

				MaterialHistory mh = expectedMhs.get(current);
				PriceHistory ph = priceHistoryList.getPriceHistories().get(current);

				assertEquals(mh.getEntryDate().toInstant().toEpochMilli(), ph.getDate().toInstant().toEpochMilli());

				float totalPrice = getPriceSummary(items, mh.getPrice());

				assertNotNull(ph.getPriceList());
				assertEquals(items.size(), ph.getPriceList().getPrices().size());
				assertEquals(totalPrice, ph.getPriceList().getTotalPrize());
				for (int currentPrice = 0; currentPrice < items.size(); currentPrice++) {
					Price actualPrice = ph.getPriceList().getPrices().get(currentPrice);
					assertEquals(getPrice(items.get(currentPrice), mh.getPrice()), actualPrice.getPrice());
				}
				current++;
			}
		}

	}

	@Test
	public void testListAllforMaterialNotExisting() throws Exception {
		mockMvc.perform(TestHTTPClient.doGet("/priceHistory/notexist")).andExpect(status().isNotFound());

	}

	private float getPriceSummary(List<Item> items, float materialPrice) {
		float result = 0;
		for (Item item : items) {
			result += getPrice(item, materialPrice);
		}
		return result;
	}

	private float getPrice(Item item, float materialPrice) {

		BigDecimal price = new BigDecimal(
				item.getAmount() * item.getUnit().getFactor() * item.getItemType().getModifier() * materialPrice)
				.setScale(2, RoundingMode.HALF_DOWN);
		return price.floatValue();

	}
}