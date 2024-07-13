package com.my.goldmanager.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.goldmanager.entity.Item;
import com.my.goldmanager.entity.MaterialHistory;
import com.my.goldmanager.repository.ItemRepository;
import com.my.goldmanager.repository.MaterialHistoryRepository;
import com.my.goldmanager.repository.MaterialRepository;
import com.my.goldmanager.rest.entity.Price;
import com.my.goldmanager.rest.entity.PriceHistory;
import com.my.goldmanager.rest.entity.PriceHistoryList;
import com.my.goldmanager.rest.entity.PriceList;
import com.my.goldmanager.service.util.PriceCalculatorUtil;

@Service

public class PriceHistoryService {

	@Autowired
	private MaterialRepository materialRepository;
	@Autowired
	private MaterialHistoryRepository materialHistoryRepository;
	@Autowired
	private ItemRepository itemRepository;

	public Optional<PriceHistoryList> listAllForMaterial(String materialId) {
		if (!materialRepository.existsById(materialId)) {
			return Optional.empty();
		}
		List<Item> items = itemRepository.findByMaterialId(materialId);
		PriceHistoryList result = new PriceHistoryList();
		result.setPriceHistories(new LinkedList<>());
		if (!items.isEmpty()) {
			List<MaterialHistory> historyList = materialHistoryRepository.findByMaterial(materialId);
			for (MaterialHistory history : historyList) {

				PriceHistory priceHistory = new PriceHistory();
				priceHistory.setDate(history.getEntryDate());
				PriceList priceList = new PriceList();
				priceHistory.setPriceList(priceList);
				result.getPriceHistories().add(priceHistory);
				priceList.setPrices(new LinkedList<>());
				for (Item item : items) {
					Price price = new Price();
					price.setItem(item);
					price.setPrice(PriceCalculatorUtil.claculatePrice(item, history.getPrice()));
					priceList.getPrices().add(price);
					priceList.setTotalPrize(priceList.getTotalPrize() + price.getPrice());
				}
			}
		}
		return Optional.of(result);
	}
}
