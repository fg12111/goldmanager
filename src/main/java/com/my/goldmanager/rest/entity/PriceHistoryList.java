package com.my.goldmanager.rest.entity;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class PriceHistoryList {
	@Getter
	@Setter
	private List<PriceHistory> priceHistories;
}
