package com.my.goldmanager.service.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.my.goldmanager.entity.Item;

public class PriceCalculatorUtil {

	private PriceCalculatorUtil() {
		//Static class
	}
	public static float claculatePrice(Item item, float materialPrice) {
		BigDecimal price = new BigDecimal(item.getAmount() * item.getUnit().getFactor() * item.getItemType().getModifier()
				* materialPrice).setScale(2, RoundingMode.HALF_DOWN);
		return price.floatValue();
	}
}
