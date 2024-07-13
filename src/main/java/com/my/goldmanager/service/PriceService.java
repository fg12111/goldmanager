package com.my.goldmanager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.goldmanager.entity.Item;
import com.my.goldmanager.repository.ItemRepository;
import com.my.goldmanager.rest.entity.Price;
import com.my.goldmanager.rest.entity.PriceGroup;
import com.my.goldmanager.rest.entity.PriceGroupMap;
import com.my.goldmanager.rest.entity.PriceList;
import com.my.goldmanager.service.util.PriceCalculatorUtil;

/**
 * Calcualates the current Prices
 */
@Service
public class PriceService {

	@Autowired
	private ItemRepository itemRepository;

	public PriceList listAll() {

		return createPriceList(itemRepository.findAll());

	}

	public PriceGroupMap groupByMaterial() {
		Map<String, PriceGroup> result = new TreeMap<>();
		List<Item> items = itemRepository.findAll();
		items.forEach(item -> addToPriceGroupsbyMaterial(item, result));
		PriceGroupMap priceGroupMap = new PriceGroupMap();
		priceGroupMap.setPriceGroups(result);
		return priceGroupMap;
	}

	public PriceGroupMap groupByItemType() {
		Map<String, PriceGroup> result = new TreeMap<>();
		List<Item> items = itemRepository.findAll();
		items.forEach(item -> addToPriceGroupsbyItemType(item, result));
		PriceGroupMap priceGroupMap = new PriceGroupMap();
		priceGroupMap.setPriceGroups(result);
		return priceGroupMap;
	}

	public Optional<PriceList> listForMaterial(String materialId) {

		List<Item> items = itemRepository.findByMaterialId(materialId);

		if (items != null && !items.isEmpty()) {
			return Optional.of(createPriceList(items));
		}
		return Optional.empty();
	}

	private void addToPriceGroupsbyMaterial(Item item, Map<String, PriceGroup> priceGroupMap) {
		PriceGroup priceGroup = priceGroupMap.get(item.getItemType().getMaterial().getName());
		if (priceGroup == null) {
			priceGroup = new PriceGroup();
			priceGroupMap.put(item.getItemType().getMaterial().getName(), priceGroup);
		}
		if (priceGroup.getPrices() == null) {
			priceGroup.setPrices(new LinkedList<Price>());
		}
		addItemToPriceGroup(item, priceGroup);
	}

	private void addToPriceGroupsbyItemType(Item item, Map<String, PriceGroup> priceGroupMap) {
		PriceGroup priceGroup = priceGroupMap.get(item.getItemType().getName());
		if (priceGroup == null) {
			priceGroup = new PriceGroup();
			priceGroupMap.put(item.getItemType().getName(), priceGroup);
		}
		if (priceGroup.getPrices() == null) {
			priceGroup.setPrices(new LinkedList<Price>());
		}
		addItemToPriceGroup(item, priceGroup);
	}

	private void addItemToPriceGroup(Item item, PriceGroup priceGroup) {
		Price price = calculatePrice(item);

		priceGroup.getPrices().add(price);
		BigDecimal totalPrice = new BigDecimal(priceGroup.getTotalPrize() + price.getPrice()).setScale(2,
				RoundingMode.HALF_DOWN);
		priceGroup.setTotalPrize(totalPrice.floatValue());
		BigDecimal amount = new BigDecimal(priceGroup.getAmount() + (item.getAmount() * item.getUnit().getFactor()))
				.setScale(2, RoundingMode.HALF_DOWN);
		priceGroup.setAmount(amount.floatValue());
	}

	private PriceList createPriceList(List<Item> items) {
		PriceList result = new PriceList();
		result.setPrices(new LinkedList<>());

		items.stream().forEach(item -> result.getPrices().add(calculatePrice(item)));
		calculateSummaryPrice(result);

		return result;
	}

	public Optional<Price> getPriceofItem(String itemId) {
		Optional<Item> optional = itemRepository.findById(itemId);
		if (optional.isPresent()) {
			return Optional.of(calculatePrice(optional.get()));
		}
		return Optional.empty();
	}

	private static void calculateSummaryPrice(PriceList priceList) {

		priceList.getPrices().stream().forEach(p -> priceList.setTotalPrize(priceList.getTotalPrize() + p.getPrice()));
	}

	private static Price calculatePrice(Item item) {
		Price result = new Price();
		if (item != null) {
			result.setItem(item);

			result.setPrice(PriceCalculatorUtil.claculatePrice(item, item.getItemType().getMaterial().getPrice()));
		}
		return result;
	}
}
