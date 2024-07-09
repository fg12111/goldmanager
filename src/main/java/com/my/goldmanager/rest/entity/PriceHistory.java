package com.my.goldmanager.rest.entity;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class PriceHistory {

	@Getter
	@Setter
	private Date date;
	
	@Getter
	@Setter
	private PriceList priceList;

}
