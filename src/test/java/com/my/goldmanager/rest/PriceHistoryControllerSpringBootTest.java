package com.my.goldmanager.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PriceHistoryControllerSpringBootTest {

	private final SecureRandom rand = new SecureRandom();
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
		
	}

	@BeforeEach
	public void setUp() {

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
	public void testListAllforMaterial() throws Exception {
		List<Material> materials = materialRepository.findAll();
		for (Material material : materials) {

			String body = mockMvc.perform(get("/priceHistory/" + material.getId())).andExpect(status().isOk())
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
				for(int currentPrice =0; currentPrice < items.size();currentPrice++) {
					Price actualPrice = ph.getPriceList().getPrices().get(currentPrice);
					assertEquals(getPrice(items.get(currentPrice), mh.getPrice()), actualPrice.getPrice());					
				}
				
				
				 
				current++;

			}
		}

		
	}

	private float getPriceSummary(List<Item> items, float materialPrice) {
		float result = 0;
		for (Item item : items) {
			result += getPrice(item,materialPrice);
		}
		return result;
	}

	private  float getPrice(Item item, float materialPrice) {
		
		BigDecimal price = new BigDecimal(item.getAmount() * item.getUnit().getFactor() * item.getItemType().getModifier()
				* materialPrice).setScale(2, RoundingMode.HALF_DOWN);
		return price.floatValue();
		
	}
}